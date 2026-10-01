package ne.fnfal113.fnamplifications.gems.implementation;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import ne.fnfal113.fnamplifications.utils.Keys;
import ne.fnfal113.fnamplifications.utils.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Locale;

public class Gem {
    private final SlimefunItem slimefunGemItem;
    private final ItemStack itemStackToSocket;
    private final String SlimefunGemItemID;
    private final Player player;
    private final NamespacedKey SlimefunGemItemIDKey;
    private final NamespacedKey socketAmountKey;

    @ParametersAreNonnullByDefault
    public Gem(SlimefunItem slimefunGemItem, ItemStack itemStackToSocket, Player player) {
        this.slimefunGemItem = slimefunGemItem;
        this.itemStackToSocket = itemStackToSocket;
        this.player = player;
        this.SlimefunGemItemID = slimefunGemItem.getId();
        this.SlimefunGemItemIDKey = Keys.createKey(slimefunGemItem.getId().toLowerCase());
        this.socketAmountKey = Keys.createKey(itemStackToSocket.getType().toString().toLowerCase() + "_socket_amount");
    }

    public void startSocket() {
        ItemMeta meta = getItemStackToSocket().getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        int itemGemAmount = checkGemAmount(pdc, getItemStackToSocket());
        if(itemGemAmount < 5) {
            if(!isSameGem(getItemStackToSocket())) {
                ItemStack cursorGemItem = getPlayer().getItemOnCursor();
                if(cursorGemItem.getAmount() > 1) {
                    cursorGemItem.setAmount(cursorGemItem.getAmount() - 1);
                } else {
                    getPlayer().setItemOnCursor(new ItemStack(Material.AIR));
                }
                socketGemToItemStack(meta, pdc, itemGemAmount);
            } else {
                Utils.sendMessage("Your item has " + getSlimefunGemItem().getItemName() + " socketed already!", getPlayer());
                getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0F, 1.0F);
            }
            return;
        }
        Utils.sendMessage("Only 5 gems per item are allowed!", getPlayer());
        getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_BLAZE_HURT, 1.0F, 1.0F);
    }

    public void socketGemToItemStack(ItemMeta meta, PersistentDataContainer pdc, int itemGemAmount) {
        String gemSlimefunItemname = getSlimefunGemItem().getItemName();
        meta.lore(GemLore.bound(meta.lore(), itemGemAmount, gemSlimefunItemname));
        pdc.set(getSlimefunGemItemIDKey(), PersistentDataType.STRING, getSlimefunGemItemID());
        pdc.set(getSocketAmountKey(), PersistentDataType.INTEGER, itemGemAmount + 1);
        getItemStackToSocket().setItemMeta(meta);
        Utils.sendMessage("Successfully bound " + gemSlimefunItemname + " to " +
            getItemStackToSocket().getType().name().replace("_", " ").toLowerCase(Locale.ROOT), getPlayer());
        getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0F, 1.0F);
    }

    public int checkGemAmount(PersistentDataContainer pdc, ItemStack itemStack) {
        return pdc.getOrDefault(Keys.createKey(itemStack.getType().toString().toLowerCase() + "_socket_amount"),
            PersistentDataType.INTEGER, 0);
    }

    public boolean isSameGem(ItemStack itemStackToSocket) {
        ItemMeta meta = itemStackToSocket.getItemMeta();
        PersistentDataContainer itemPdc = meta.getPersistentDataContainer();
        if(itemPdc.isEmpty()) return false;
        return itemPdc.has(getSlimefunGemItemIDKey(), PersistentDataType.STRING);
    }

    public SlimefunItem getSlimefunGemItem() { return slimefunGemItem; }
    public ItemStack getItemStackToSocket() { return itemStackToSocket; }
    public String getSlimefunGemItemID() { return SlimefunGemItemID; }
    public Player getPlayer() { return player; }
    public NamespacedKey getSlimefunGemItemIDKey() { return SlimefunGemItemIDKey; }
    public NamespacedKey getSocketAmountKey() { return socketAmountKey; }
}
