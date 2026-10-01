package ne.fnfal113.fnamplifications.gems.implementation;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import ne.fnfal113.fnamplifications.gems.RetaliateGem;
import ne.fnfal113.fnamplifications.gems.abstracts.AbstractGem;
import ne.fnfal113.fnamplifications.utils.Keys;
import ne.fnfal113.fnamplifications.utils.Utils;
import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
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

    @SuppressWarnings("ConstantConditions")
    public void showAvailableGemsUI() {
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
        Inventory inventory = Bukkit.createInventory(null, 9, Utils.colorTranslator("&cSelect a gem to unbind"));
        for (ItemStack gems: gemArray) inventory.addItem(gems);
        getPlayer().openInventory(inventory);
        getPlayer().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0F, 1.0F);
    }

    @SuppressWarnings("ConstantConditions")
    public void unbindGem(SlimefunItem gem, int chance) {
        getPlayer().getInventory().getItemInMainHand().setAmount(0);
        if(ThreadLocalRandom.current().nextInt(100) <= chance) {
            ItemMeta meta = getItemInOffhand().getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            NamespacedKey socketAmountKey = Keys.createKey(getItemInOffhand().getType().toString().toLowerCase() + "_socket_amount");
            NamespacedKey gemKey = Keys.createKey(gem.getId().toLowerCase());
            List<Component> lore = meta.lore();
            removeOtherPdc(pdc, gem);
            pdc.remove(gemKey);
            pdc.set(socketAmountKey, PersistentDataType.INTEGER, pdc.get(socketAmountKey, PersistentDataType.INTEGER) - 1);
            boolean lastGem = pdc.get(socketAmountKey, PersistentDataType.INTEGER) == 0;
            meta.lore(GemLore.unbound(lore, Utils.colorTranslator(gem.getItemName()), lastGem));
            if (lastGem) pdc.remove(socketAmountKey);
            getItemInOffhand().setItemMeta(meta);
            Utils.sendMessage("Successfully removed selected gem!", getPlayer());
            getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_VILLAGER_WORK_WEAPONSMITH, 1.0F, 1.0F);
        } else {
            Utils.sendMessage("Failed to unbind the gem from the item!", getPlayer());
            getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.0F, 1.0F);
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
