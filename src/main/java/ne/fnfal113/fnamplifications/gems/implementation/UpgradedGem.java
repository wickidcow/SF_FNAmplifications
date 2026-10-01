package ne.fnfal113.fnamplifications.gems.implementation;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import ne.fnfal113.fnamplifications.utils.Keys;
import ne.fnfal113.fnamplifications.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.List;

public class UpgradedGem extends Gem {
    private final NamespacedKey key;

    public UpgradedGem(SlimefunItem slimefunGemItem, ItemStack itemStackToSocket, Player p) {
        super(slimefunGemItem, itemStackToSocket, p);
        this.key = Keys.createKey(slimefunGemItem.getId().toLowerCase() + "_gem_tier");
    }

    public void upgradeExistingGem(ItemStack slimefunGemItem, int gemTier) {
        ItemMeta meta = getItemStackToSocket().getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        int currentGemTier = container.getOrDefault(getKey(), PersistentDataType.INTEGER, 4);
        List<Component> lore = meta.lore();
        if(isSameGem(getItemStackToSocket())) {
            // Existing reverse tier order and cursor consumption are deliberately unchanged.
            if(currentGemTier - 1 == gemTier) {
                getPlayer().setItemOnCursor(new ItemStack(Material.AIR));
                if(lore != null) {
                    String oldName = getSlimefunGemItem().getItemName();
                    String prefix = Utils.colorTranslator(oldName.substring(0, oldName.lastIndexOf(" ") + 2));
                    meta.lore(GemLore.upgraded(lore, prefix, slimefunGemItem.getItemMeta().displayName()));
                    meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, gemTier);
                    getItemStackToSocket().setItemMeta(meta);
                    getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0F, 1.0F);
                }
            } else {
                getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0F, 1.0F);
                Utils.sendMessage("Gem tier not compatible! upgrades must be in order from levels 1 > 2 > 3 > 4)!", getPlayer());
            }
        } else {
            Utils.sendMessage("You do not have a similar gem that can be upgraded!", getPlayer());
        }
    }

    public NamespacedKey getKey() { return key; }
}
