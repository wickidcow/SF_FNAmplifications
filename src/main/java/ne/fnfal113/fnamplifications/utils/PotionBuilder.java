package ne.fnfal113.fnamplifications.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A simple potion builder based from the given {@link PotionType}
 */
public class PotionBuilder {

    private final PotionType potionType;

    @ParametersAreNonnullByDefault
    public PotionBuilder(PotionType potionType) {
        this.potionType = potionType;
    }

    /**
     *
     * @return the potion as itemstack
     */
    public ItemStack createPotion() {
        ItemStack itemStack = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) itemStack.getItemMeta();

        meta.setBasePotionType(potionType);
        
        itemStack.setItemMeta(meta);

        return itemStack.clone();
    }

    public PotionType getPotionType() {
        return potionType;
    }

}
