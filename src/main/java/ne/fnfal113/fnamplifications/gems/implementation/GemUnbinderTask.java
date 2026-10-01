package ne.fnfal113.fnamplifications.gems.implementation;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import ne.fnfal113.fnamplifications.gems.RetaliateGem;
import ne.fnfal113.fnamplifications.gems.abstracts.AbstractGem;
import ne.fnfal113.fnamplifications.gems.abstracts.AbstractGemUnbinder;
import ne.fnfal113.fnamplifications.utils.Keys;
import ne.fnfal113.fnamplifications.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GemUnbinderTask {
    private final Player player;
    private final ItemStack itemInOffhand;

    public GemUnbinderTask(Player player, ItemStack itemInOffhand) {
        this.player = player;
        this.itemInOffhand = itemInOffhand;
    }

    public void showAvailableGemsUI() {
        if (getItemInOffhand() == null || !getItemInOffhand().hasItemMeta()) {
            Utils.sendMessage("Offhand item doesn't have bounded gems!", getPlayer());
            return;
        }
        PersistentDataContainer pdc = getItemInOffhand().getItemMeta().getPersistentDataContainer();
        if(pdc.isEmpty()) {
            Utils.sendMessage("Offhand item doesn't have bounded gems!", getPlayer());
            return;
        }
        List<ItemStack> gemArray = new ArrayList<>();
        for(NamespacedKey key : GemKeysEnum.GEM_KEYS.getGemKeyList()) {
            if(pdc.has(key, PersistentDataType.STRING)) {
                SlimefunItem gem = SlimefunItem.getById(pdc.get(key, PersistentDataType.STRING));
                if(gem instanceof AbstractGem) gemArray.add(gem.getItem().clone());
            }
        }
        if(gemArray.isEmpty()) {
            Utils.sendMessage("Offhand item doesn't have bounded gems!", getPlayer());
            return;
        }
        Inventory inventory = Bukkit.createInventory(null, 9,
                Component.text("Select a gem to unbind", NamedTextColor.RED));
        for (ItemStack gems: gemArray) inventory.addItem(gems);
        getPlayer().openInventory(inventory);
        getPlayer().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0F, 1.0F);
    }

    public void unbindGem(SlimefunItem gem, int chance) {
        ItemStack target = getPlayer().getInventory().getItemInOffHand();
        ItemStack tool = getPlayer().getInventory().getItemInMainHand();
        SlimefunItem held = SlimefunItem.getByItem(tool);
        if (!(gem instanceof AbstractGem) || !(held instanceof AbstractGemUnbinder unbinder)
                || unbinder.getChance() != chance || target.isEmpty() || tool.isEmpty()
                || getItemInOffhand() == null || !target.equals(getItemInOffhand())) {
            Utils.sendMessage("The selected gem or held items changed. Reopen the unbinder to try again.", getPlayer());
            return;
        }

        final ItemStack originalTarget;
        final ItemStack originalTool;
        final ItemMeta staged;
        try {
            originalTarget = target.clone();
            originalTool = tool.clone();
            NamespacedKey socketKey = Keys.createKey(target.getType().toString().toLowerCase() + "_socket_amount");
            NamespacedKey gemKey = Keys.createKey(gem.getId().toLowerCase());
            staged = GemUnbindOperation.prepare(originalTarget.getItemMeta(), socketKey, gemKey,
                    gem.getId(), Utils.colorTranslator(gem.getItemName()), data -> removeOtherPdc(data, gem));
        } catch (RuntimeException failure) {
            Utils.sendMessage("Saved gem data could not be prepared. No unbinding tool was consumed; ask a server owner to inspect the item.", getPlayer());
            Bukkit.getLogger().log(java.util.logging.Level.WARNING,
                    "[FNAmplifications] Refused unbinding before consuming a tool; existing item data retained", failure);
            return;
        }

        GemUnbindOperation.Result result = GemUnbindOperation.attempt(staged, chance,
                () -> ThreadLocalRandom.current().nextInt(100),
                () -> getPlayer().getInventory().getItemInOffHand().equals(originalTarget)
                        && getPlayer().getInventory().getItemInMainHand().equals(originalTool),
                target::setItemMeta,
                // Preserve the established whole-stack cost for an accepted attempt.
                () -> tool.setAmount(0));
        switch (result) {
            case SUCCESS -> {
                Utils.sendMessage("Successfully removed selected gem!", getPlayer());
                getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_VILLAGER_WORK_WEAPONSMITH, 1.0F, 1.0F);
            }
            case FAILED_ROLL -> {
                Utils.sendMessage("Failed to unbind the gem from the item!", getPlayer());
                getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.0F, 1.0F);
            }
            case STALE_ITEMS, REJECTED_METADATA -> Utils.sendMessage(
                    "Unbinding was not applied and no tool was consumed. Reopen the unbinder after checking the item.", getPlayer());
        }
    }

    public void removeOtherPdc(PersistentDataContainer pdc, SlimefunItem gem) {
        NamespacedKey gemTierKey = Keys.createKey(gem.getId().toLowerCase() + "_gem_tier");
        if(pdc.has(gemTierKey, PersistentDataType.INTEGER)) pdc.remove(gemTierKey);
        if(gem instanceof RetaliateGem) pdc.remove(Keys.RETURN_WEAPON_KEY);
    }

    public Player getPlayer() { return player; }
    public ItemStack getItemInOffhand() { return itemInOffhand; }
}
