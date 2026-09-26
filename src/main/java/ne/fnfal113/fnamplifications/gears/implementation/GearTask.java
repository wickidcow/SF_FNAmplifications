package ne.fnfal113.fnamplifications.gears.implementation;

import com.google.common.base.Strings;

import ne.fnfal113.fnamplifications.utils.WeaponArmorEnum;
import ne.fnfal113.fnamplifications.utils.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GearTask {

    private final NamespacedKey storageKey;

    private final NamespacedKey storageKey2;

    private final NamespacedKey storageKey3;

    private final int startingProgress;

    private final int incrementProgress;

    private final int maxLevel;

    private final ItemStack itemStack;

    private final List<UUID> uuidList = new ArrayList<>();

    public GearTask(NamespacedKey key1, NamespacedKey key2, NamespacedKey key3, ItemStack item, int startingProgress, int incrementProgress, int maxLevel) {
        this.storageKey = key1;
        this.storageKey2 = key2;
        this.storageKey3 = key3;
        this.itemStack = item;
        this.startingProgress = startingProgress;
        this.incrementProgress = incrementProgress;
        this.maxLevel = maxLevel;
    }

    public Component getProgressBar(int current, int max, int totalBars, char symbol) {
        float percent = (float) current / max; // divide the current progress to the max value to get the percent
        int progressBars = (int) (totalBars * percent); // multiply the percent value to total progress bars to get current bar amount

        return Component.text(Strings.repeat(String.valueOf(symbol), progressBars), NamedTextColor.YELLOW)
            .append(Component.text(
                Strings.repeat(String.valueOf(symbol), totalBars - progressBars),
                NamedTextColor.GRAY
            ));
    }

    public boolean onHit(EntityDamageByEntityEvent event, Player p, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer progress = meta.getPersistentDataContainer();

        int xpAmount = progress.getOrDefault(getStorageKey(), PersistentDataType.INTEGER, 0);
        int armorLevel = progress.getOrDefault(getStorageKey2(), PersistentDataType.INTEGER, 0);
        int maxXpReq = progress.getOrDefault(getStorageKey3(), PersistentDataType.INTEGER, getStartingProgress());
        int xpAmountIncremented = xpAmount + 1;

        if(isMaxLevel(armorLevel)){
            if(!uuidList.contains(p.getUniqueId())) {
                Utils.sendMessage(Utils.legacyString(meta.displayName()) + " has reached max level!", p);

                uuidList.add(p.getUniqueId());
            }

            return false;
        }

        progress.set(getStorageKey(), PersistentDataType.INTEGER, xpAmountIncremented);

        List<Component> lore = meta.lore();

        if (lore == null) {
            return false;
        }

        if (xpAmountIncremented >= 0) {
           updateArmour(armorLevel, xpAmountIncremented, maxXpReq, item, meta, lore);
        }

        if (xpAmountIncremented == maxXpReq) {
            return levelUpArmour(armorLevel, xpAmountIncremented, maxXpReq, item, meta, progress, lore, p);
        }

        return false;
    }

    public void updateArmour(int armorLevel, int xpAmountIncremented, int maxXpReq, ItemStack item, ItemMeta meta, List<Component> lore) {
        lore.set(7, Component.text("Level: " + armorLevel, NamedTextColor.YELLOW));
        lore.set(8, Component.text("Progress:", NamedTextColor.YELLOW));
        lore.set(9, Component.text("[", NamedTextColor.GRAY)
            .append(getProgressBar(xpAmountIncremented, maxXpReq, 10, '■'))
            .append(Component.text("]", NamedTextColor.GRAY)));

        if(WeaponArmorEnum.CHESTPLATE.isTagged(getItemStack().getType()) && armorLevel == 30 && xpAmountIncremented == 1) {
            lore.add(10, Component.empty());
            lore.add(11, Component.text("◬◬◬◬◬◬| ", NamedTextColor.RED)
                .append(Component.text("Effects ", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
                .append(Component.text("|◬◬◬◬◬◬", NamedTextColor.GOLD)));
            lore.add(12, Component.text("Permanent Saturation", NamedTextColor.GREEN));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
    }

    public boolean levelUpArmour(int armorLevel, int xpAmountIncremented, int maxXpReq, ItemStack item, ItemMeta meta, PersistentDataContainer progress, List<Component> lore, Player p) {
        if(isMaxLevel(armorLevel)) {
            Utils.sendMessage(Utils.legacyString(meta.displayName()) + " has reached max level!", p);

            return false;
        }

        int currentArmorLevel = armorLevel + 1;

        // reset armor pdc xp to 0
        progress.set(getStorageKey(), PersistentDataType.INTEGER, 0);
        // increase armor pdc level by 1
        progress.set(getStorageKey2(), PersistentDataType.INTEGER, currentArmorLevel);
        // increase armor pdc max xp requirement
        progress.set(getStorageKey3(), PersistentDataType.INTEGER, maxXpReq + getIncrementProgress());

        lore.set(7, Component.text("Level: " + currentArmorLevel, NamedTextColor.YELLOW));
        lore.set(8, Component.text("Progress:", NamedTextColor.YELLOW));
        lore.set(9, Component.text("[", NamedTextColor.GRAY)
            .append(getProgressBar(xpAmountIncremented, maxXpReq, 10, '■'))
            .append(Component.text("]", NamedTextColor.GRAY)));

        meta.lore(lore);
        item.setItemMeta(meta);

        sendLevelUpMessage(p);

        return true;
    }

    public boolean isMaxLevel(int armorLevel) {
        return armorLevel >= getMaxLevel();
    }

    public void sendLevelUpMessage(Player p){
        Utils.sendMessage(Utils.legacyString(getItemStack().getItemMeta().displayName()) + " leveled up!", p);

        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1 , 1);
    }

    public NamespacedKey getStorageKey() {
        return storageKey;
    }

    public NamespacedKey getStorageKey2() {
        return storageKey2;
    }

    public NamespacedKey getStorageKey3() {
        return storageKey3;
    }

    public int getStartingProgress() {
        return startingProgress;
    }

    public int getIncrementProgress() {
        return incrementProgress;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

}