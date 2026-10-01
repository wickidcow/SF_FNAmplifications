package ne.fnfal113.fnamplifications.gems.listener;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import ne.fnfal113.fnamplifications.gems.abstracts.AbstractGem;
import ne.fnfal113.fnamplifications.gems.abstracts.AbstractGemUnbinder;
import ne.fnfal113.fnamplifications.gems.implementation.GemUnbinderTask;
import ne.fnfal113.fnamplifications.utils.Utils;
import ne.fnfal113.fnamplifications.utils.compatibility.VersionedClass;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GemUnbinderListener implements Listener {
    private final Map<UUID, Integer> unbindChanceMap = new HashMap<>();

    /** Select a displayed gem only while its unbinder session and tool remain valid. */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = VersionedClass.invoke(event.getView(), "getTitle").toString();
        if (!title.equals(Utils.colorTranslator("&cSelect a gem to unbind"))) return;
        // Cancel first: malformed data must never allow a displayed gem to move into inventory.
        event.setCancelled(true);
        if (event.getClickedInventory() != event.getView().getTopInventory()
                || !(event.getWhoClicked() instanceof Player player)) return;
        SlimefunItem gem = SlimefunItem.getByItem(event.getCurrentItem());
        if (!(gem instanceof AbstractGem)) return;

        Integer chance = getUnbindChanceMap().remove(player.getUniqueId());
        SlimefunItem held = SlimefunItem.getByItem(player.getInventory().getItemInMainHand());
        try {
            if (chance == null || !(held instanceof AbstractGemUnbinder unbinder)
                    || unbinder.getChance() != chance) {
                Utils.sendMessage("The unbinder selection expired or the held tool changed. Reopen it to try again.", player);
                return;
            }
            new GemUnbinderTask(player, player.getInventory().getItemInOffHand()).unbindGem(gem, chance);
        } finally {
            player.closeInventory();
        }
    }

    /** Open the existing gem selector without spending the tool. */
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        SlimefunItem slimefunItem = SlimefunItem.getByItem(player.getInventory().getItemInMainHand());
        if(slimefunItem instanceof AbstractGemUnbinder unbinder) {
            event.setUseItemInHand(Event.Result.DENY);
            player.updateInventory();
            if(player.getInventory().getItemInOffHand().getType() == Material.AIR) {
                Utils.sendMessage("You have no item in your offhand that contain bounded gems!", player);
                return;
            }
            getUnbindChanceMap().remove(player.getUniqueId());
            new GemUnbinderTask(player, player.getInventory().getItemInOffHand()).showAvailableGemsUI();
            // Opening a replacement view closes the old one first. Record the new
            // chance afterwards so the old close event cannot clear the new session.
            if (VersionedClass.invoke(player.getOpenInventory(), "getTitle").toString()
                    .equals(Utils.colorTranslator("&cSelect a gem to unbind"))) {
                getUnbindChanceMap().put(player.getUniqueId(), unbinder.getChance());
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        getUnbindChanceMap().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        getUnbindChanceMap().remove(event.getPlayer().getUniqueId());
    }

    public Map<UUID, Integer> getUnbindChanceMap() {
        return unbindChanceMap;
    }
}
