package ne.fnfal113.fnamplifications.utils;

import ne.fnfal113.fnamplifications.FNAmplifications;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Effect;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * utility class for FNAmplifications
 * @author FN_FAL113
 */
public class Utils {

    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();
    private static final char LEGACY_COLOR_CHAR = '\u00A7';
    private static final String LEGACY_COLOR_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

    public static final Effect SMOKE_EFFECT = resolveEffect("SMOKE_SHOOT", "SMOKE");

    private static Effect resolveEffect(String currentName, String legacyName) {
        try {
            return Effect.valueOf(currentName);
        } catch (IllegalArgumentException ignored) {
            return Effect.valueOf(legacyName);
        }
    }

    public static final DecimalFormat powerFormat = new DecimalFormat("###,###.##",
        DecimalFormatSymbols.getInstance(Locale.ROOT));

    public static String colorTranslator(String strings) {
        if (strings == null) {
            return null;
        }

        char[] chars = strings.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&' && LEGACY_COLOR_CODES.indexOf(chars[i + 1]) >= 0) {
                chars[i] = LEGACY_COLOR_CHAR;
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }

    public static Component colorComponent(String value) {
        return LEGACY_AMPERSAND.deserialize(value);
    }

    public static Component legacyComponent(String value) {
        return LEGACY_SECTION.deserialize(value);
    }

    public static String legacyString(Component component) {
        return component == null ? "" : LEGACY_SECTION.serialize(component);
    }

    public static void sendMessage(String message, LivingEntity livingEntity) {
        livingEntity.sendMessage(colorTranslator("&c&l[FNAmpli" + "&b&lfications] > &6" + message));
    }

    public static String[] stringSequence(String... stringSequence) {
        return stringSequence;
    }

    // Armorstand methods for setting euler angles
    public static EulerAngle setRightArmAngle(ArmorStand armorStand, int x, int y, int z) {
        double armorStandX = armorStand.getRightArmPose().getX();
        double armorStandY = armorStand.getRightArmPose().getY();
        double armorStandZ = armorStand.getRightArmPose().getZ();

        return new EulerAngle(armorStandX + Math.toRadians(x), armorStandY + Math.toRadians(y), armorStandZ + Math.toRadians(z));
    }

    public static EulerAngle setLeftArmAngle(ArmorStand armorStand, int x, int y, int z) {
        double armorStandX = armorStand.getLeftArmPose().getX();
        double armorStandY = armorStand.getLeftArmPose().getY();
        double armorStandZ = armorStand.getLeftArmPose().getZ();

        return new EulerAngle(armorStandX + Math.toRadians(x), armorStandY + Math.toRadians(y), armorStandZ + Math.toRadians(z));
    }

    public static EulerAngle setHeadAngle(ArmorStand armorStand, int x, int y, int z) {
        double armorStandX = armorStand.getHeadPose().getX();
        double armorStandY = armorStand.getHeadPose().getY();
        double armorStandZ = armorStand.getHeadPose().getZ();

        return new EulerAngle(armorStandX + Math.toRadians(x), armorStandY + Math.toRadians(y), armorStandZ + Math.toRadians(z));
    }

    /**
     * 
     * @param itemStack the itemstack involved
     * @param configSection usually the itemstack's id as config section
     * @param configSectionSetting the setting under the config section
     * @param stringToReplace serves as the index for searching and replacing the lore line
     * @param color color formatting for the lore line
     * @param suffix the end string of the config value (units, percent, etc)
     * @param tier the current gem tier
     * @param fileName the filename of the config for retrieving the value
     */
    public static void setGemTierLore(ItemStack itemStack, String configSection, String configSectionSetting, String stringToReplace, String color, String suffix, int tier, String fileName) {
        ItemMeta meta = itemStack.getItemMeta();
        List<Component> lore = meta.lore();

        if (lore == null) {
            return;
        }

        for(int i = 0; i < lore.size(); i++) {
            String legacyLine = legacyString(lore.get(i));
            String target = colorTranslator(color + stringToReplace);
            if(legacyLine.contains(target)){
                String line = legacyLine.replace(target,
                        colorTranslator(color + (FNAmplifications.getInstance().getConfigManager().getCustomConfig(fileName).getInt(configSection + "." + configSectionSetting) / tier--) + suffix));
                lore.set(i, legacyComponent(line));
            }
        }

        meta.lore(lore);
        itemStack.setItemMeta(meta);
    }

    /**
     * 
     * @param itemStack the itemstack involved
     * @param configSection usually the itemstack's id as config section
     * @param configSectionSetting the setting under the config section
     * @param stringToReplace serves as the index for searching and replacing the lore line
     * @param color color formatting for the lore line
     * @param suffix the end string of the config value (units, percent, etc)
     * @param fileName the filename of the config for retrieving the value
     */
    public static void setLoreByConfigValue(@Nonnull ItemStack itemStack, String configSection, String configSectionSetting, String stringToReplace, String color, String suffix, String fileName){
        ItemMeta meta = itemStack.getItemMeta();
        List<Component> lore = meta.lore();

        if (lore == null) {
            return;
        }

        for(int i = 0; i < lore.size(); i++){
            String legacyLine = legacyString(lore.get(i));
            String target = colorTranslator(color + stringToReplace);
            if(legacyLine.contains(target)){
                String line = legacyLine.replace(target,
                        colorTranslator(color + FNAmplifications.getInstance().getConfigManager().getCustomConfig(fileName).get(configSection + "." + configSectionSetting) + suffix));
                lore.set(i, legacyComponent(line));
            }
        }

        meta.lore(lore);
        itemStack.setItemMeta(meta);
    }

    /**
     * 
     * @param itemStack the itemstack involved
     * @param meta itemstack's item meta
     * @param value value being to set in the lore
     * @param prefix serves as the index for searching and replacing the lore line
     * @param color color formatting for the prefix
     * @param color2 color formatting for the value and suffix
     * @param suffix the end string of the pdc value (units, percent, etc)
     */
    public static void setLoreByPdc(ItemStack itemStack, ItemMeta meta, String value, String prefix, String color, String color2, String suffix){
        List<Component> lore = meta.lore();
        if (lore == null) {
            return;
        }

        for(int i = 0; i < lore.size(); i++){
            String legacyLine = legacyString(lore.get(i));
            if(legacyLine.contains(Utils.colorTranslator(color + prefix))){
                lore.set(i, colorComponent(color + prefix + color2 + value + suffix));
            }
        }

        meta.lore(lore);
        itemStack.setItemMeta(meta);
    }

    public static Long cooldownHelper(Long timeInMs){
        return (long) Math.floor((System.currentTimeMillis() - timeInMs) / 1000);
    }

}
