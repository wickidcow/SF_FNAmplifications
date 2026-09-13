package ne.fnfal113.fnamplifications.diagnostics;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import ne.fnfal113.fnamplifications.FNAmplifications;
import ne.fnfal113.fnamplifications.quivers.abstracts.AbstractQuiver;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Loaded-scope reconciliation for FN Amplifications item-local persistence.
 *
 * <p>Quiver arrow counts are authoritative and are never altered. Doctor only rebuilds state that current
 * FN Amplifications can derive losslessly from a valid count: the open/closed marker and a missing uniqueness
 * marker on a non-empty quiver. Invalid or ambiguous counts are reported and left untouched.</p>
 */
public final class FNDoctor {

    private static final int MAX_NESTED_DEPTH = 4;
    private static final int MAX_DETAILS = 40;

    private final FNAmplifications plugin;
    private final boolean repair;
    private final Set<Inventory> seenInventories = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<String> details = new ArrayList<>();

    private long scannedEntries;
    private long issuesFound;
    private long repairedEntries;
    private long failures;

    private FNDoctor(FNAmplifications plugin, boolean repair) {
        this.plugin = plugin;
        this.repair = repair;
    }

    public static FNDoctorReport run(boolean repair) {
        FNAmplifications plugin = FNAmplifications.getInstance();
        if (plugin == null || !plugin.isEnabled()) {
            return new FNDoctorReport(0, 0, 0, 1,
                List.of("FN Amplifications is not enabled; quiver reconciliation could not run."));
        }
        return new FNDoctor(plugin, repair).scan();
    }

    private FNDoctorReport scan() {
        try {
            for (World world : plugin.getServer().getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    scanChunk(chunk);
                }
            }

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                scanInventory(player.getInventory(), 0);
                scanInventory(player.getEnderChest(), 0);
            }
        } catch (RuntimeException | LinkageError error) {
            failures++;
            addDetail("FN Doctor stopped safely after an unexpected runtime failure: "
                + error.getClass().getSimpleName() + ".");
            plugin.getLogger().warning("FN Doctor reconciliation failed safely: " + error.getMessage());
        }

        if (!repair) {
            addDetail("Read-only loaded-scope scan; no chunks were force-loaded and no arrow counts were changed.");
        } else {
            addDetail("Repair changed only derivable quiver state/uniqueness markers; arrow counts were never modified.");
        }

        return new FNDoctorReport(scannedEntries, issuesFound, repairedEntries, failures, details);
    }

    private void scanChunk(Chunk chunk) {
        for (BlockState state : chunk.getTileEntities()) {
            if (state instanceof InventoryHolder holder) {
                scanInventory(holder.getInventory(), 0);
            }
        }

        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof Player) {
                continue;
            }

            if (entity instanceof Item item) {
                ItemStack stack = item.getItemStack();
                if (inspectItem(stack, 0)) {
                    item.setItemStack(stack);
                }
            } else if (entity instanceof ItemFrame frame) {
                ItemStack stack = frame.getItem();
                if (inspectItem(stack, 0)) {
                    frame.setItem(stack);
                }
            } else if (entity instanceof ItemDisplay display) {
                ItemStack stack = display.getItemStack();
                if (inspectItem(stack, 0)) {
                    display.setItemStack(stack);
                }
            } else if (entity instanceof LivingEntity living && living.getEquipment() != null) {
                var equipment = living.getEquipment();
                ItemStack main = equipment.getItemInMainHand();
                if (inspectItem(main, 0)) {
                    equipment.setItemInMainHand(main);
                }
                ItemStack off = equipment.getItemInOffHand();
                if (inspectItem(off, 0)) {
                    equipment.setItemInOffHand(off);
                }
                ItemStack[] armor = equipment.getArmorContents();
                boolean armorChanged = false;
                for (ItemStack stack : armor) {
                    armorChanged |= inspectItem(stack, 0);
                }
                if (armorChanged) {
                    equipment.setArmorContents(armor);
                }
            }

            if (entity instanceof InventoryHolder holder) {
                scanInventory(holder.getInventory(), 0);
            }
        }
    }

    private void scanInventory(Inventory inventory, int depth) {
        if (inventory == null || !seenInventories.add(inventory)) {
            return;
        }
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (inspectItem(stack, depth)) {
                inventory.setItem(slot, stack);
            }
        }
    }

    private boolean inspectItem(ItemStack stack, int depth) {
        if (stack == null || stack.getType().isAir()) {
            return false;
        }

        boolean changed = reconcileQuiver(stack);
        if (depth >= MAX_NESTED_DEPTH || !stack.hasItemMeta()) {
            return changed;
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof BundleMeta bundleMeta) {
            List<ItemStack> items = new ArrayList<>(bundleMeta.getItems());
            boolean nestedChanged = false;
            for (ItemStack nested : items) {
                nestedChanged |= inspectItem(nested, depth + 1);
            }
            if (nestedChanged) {
                bundleMeta.setItems(items);
                stack.setItemMeta(bundleMeta);
                changed = true;
            }
        } else if (meta instanceof BlockStateMeta blockStateMeta) {
            BlockState nestedState = blockStateMeta.getBlockState();
            if (nestedState instanceof InventoryHolder holder) {
                Inventory nestedInventory = holder.getInventory();
                boolean nestedChanged = false;
                for (int slot = 0; slot < nestedInventory.getSize(); slot++) {
                    ItemStack nested = nestedInventory.getItem(slot);
                    if (inspectItem(nested, depth + 1)) {
                        nestedInventory.setItem(slot, nested);
                        nestedChanged = true;
                    }
                }
                if (nestedChanged) {
                    blockStateMeta.setBlockState(nestedState);
                    stack.setItemMeta(blockStateMeta);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean reconcileQuiver(ItemStack stack) {
        SlimefunItem slimefunItem;
        try {
            slimefunItem = SlimefunItem.getByItem(stack);
        } catch (RuntimeException | LinkageError error) {
            failures++;
            return false;
        }
        if (!(slimefunItem instanceof AbstractQuiver quiver) || !stack.hasItemMeta()) {
            return false;
        }

        scannedEntries++;
        ItemMeta meta = stack.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        boolean hasCount = pdc.has(quiver.getStoredArrowsKey(), PersistentDataType.INTEGER);
        boolean hasState = pdc.has(quiver.getStateKey(), PersistentDataType.STRING);
        boolean hasUniqueId = pdc.has(quiver.getRandomIdKey(), PersistentDataType.INTEGER);

        // A freshly crafted, never-used quiver legitimately has no runtime PDC yet.
        if (!hasCount && !hasState && !hasUniqueId) {
            return false;
        }

        if (!hasCount) {
            issuesFound++;
            addDetail(quiver.getId() + ": quiver runtime markers exist but the authoritative arrow-count PDC is missing; left untouched.");
            return false;
        }

        Integer count = pdc.get(quiver.getStoredArrowsKey(), PersistentDataType.INTEGER);
        if (count == null || count < 0 || count > quiver.getQuiverSize()) {
            issuesFound++;
            addDetail(quiver.getId() + ": invalid stored arrow count " + count + " (capacity "
                + quiver.getQuiverSize() + "); count was not changed.");
            return false;
        }

        String expectedState = count > 0 ? "open" : "closed";
        String currentState = pdc.get(quiver.getStateKey(), PersistentDataType.STRING);
        boolean stateIssue = !expectedState.equals(currentState);
        boolean idIssue = count > 0 && !hasUniqueId;

        if (!stateIssue && !idIssue) {
            return false;
        }

        if (stateIssue) {
            issuesFound++;
            addDetail(quiver.getId() + ": state marker was " + describe(currentState)
                + " but arrow count requires '" + expectedState + "'.");
        }
        if (idIssue) {
            issuesFound++;
            addDetail(quiver.getId() + ": non-empty quiver is missing its unstackability marker.");
        }

        if (!repair) {
            return false;
        }

        if (stateIssue) {
            pdc.set(quiver.getStateKey(), PersistentDataType.STRING, expectedState);
        }
        if (idIssue) {
            pdc.set(quiver.getRandomIdKey(), PersistentDataType.INTEGER,
                ThreadLocalRandom.current().nextInt(1, 1_000_000));
        }
        stack.setItemMeta(meta);
        repairedEntries++;
        return true;
    }

    private static String describe(String value) {
        return value == null ? "missing" : "'" + value + "'";
    }

    private void addDetail(String detail) {
        if (details.size() < MAX_DETAILS) {
            details.add(detail);
        }
    }
}
