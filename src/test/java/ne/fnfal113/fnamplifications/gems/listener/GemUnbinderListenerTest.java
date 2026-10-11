package ne.fnfal113.fnamplifications.gems.listener;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Set;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.junit.Test;

public class GemUnbinderListenerTest {

    @Test
    public void dragHandlerIsRegisteredForInventoryDragEvents() throws Exception {
        Method handler = GemUnbinderListener.class.getDeclaredMethod("onDrag", InventoryDragEvent.class);
        assertNotNull(handler.getAnnotation(EventHandler.class));
    }

    @Test
    public void anyDragTouchingTheSelectorIsBlocked() {
        assertTrue(GemUnbinderListener.dragTouchesTopInventory(Set.of(0), 9));
        assertTrue(GemUnbinderListener.dragTouchesTopInventory(Set.of(8), 9));
        assertTrue(GemUnbinderListener.dragTouchesTopInventory(Set.of(3, 12, 20), 9));
    }

    @Test
    public void dragEntirelyInsidePlayerInventoryIsAllowed() {
        assertFalse(GemUnbinderListener.dragTouchesTopInventory(Set.of(9), 9));
        assertFalse(GemUnbinderListener.dragTouchesTopInventory(Set.of(9, 17, 35), 9));
        assertFalse(GemUnbinderListener.dragTouchesTopInventory(Set.of(), 9));
    }

    @Test
    public void malformedNegativeTopSizeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> GemUnbinderListener.dragTouchesTopInventory(Set.of(0), -1));
    }
}
