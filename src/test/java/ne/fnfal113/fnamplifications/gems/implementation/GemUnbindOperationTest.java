package ne.fnfal113.fnamplifications.gems.implementation;

import static org.junit.Assert.*;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.Test;

/** Real operation/component code with explicit in-memory ItemMeta/PDC interface doubles. */
public class GemUnbindOperationTest {
    private static final NamespacedKey SOCKET = key("diamond_sword_socket_amount");
    private static final NamespacedKey GEM = key("retaliate_gem");
    private static final NamespacedKey TIER = key("retaliate_gem_gem_tier");
    private static final NamespacedKey RETURN = key("return_weapon");
    private static final NamespacedKey OTHER = key("other_gem");
    private static final NamespacedKey OWNER = key("owner_uuid");
    private static final NamespacedKey CHARGE = key("charge");
    private static final NamespacedKey BYTES = key("opaque_bytes");
    private static final String ID = "RETALIATE_GEM";
    private static final String NAME = "Retaliate";

    @Test public void missingSocketCountIsRejectedBeforeCleanup() {
        Meta state = oldItem(1); state.data.remove(SOCKET); rejected(state);
    }
    @Test public void wrongTypedSocketCountIsRejected() {
        Meta state = oldItem(1); state.put(SOCKET, PersistentDataType.LONG, 1L); rejected(state);
    }
    @Test public void invalidSocketCountsAreNotGuessedOrDecremented() {
        for (int count : new int[] {0, -1, 6, Integer.MIN_VALUE, Integer.MAX_VALUE}) rejected(oldItem(count));
    }
    @Test public void missingSelectedGemIsRejected() {
        Meta state = oldItem(1); state.data.remove(GEM); rejected(state);
    }
    @Test public void mismatchedSelectedGemIsRejected() {
        Meta state = oldItem(1); state.put(GEM, PersistentDataType.STRING, "UNRELATED_GEM"); rejected(state);
    }
    @Test public void wrongTypedSelectedGemIsRejected() {
        Meta state = oldItem(1); state.put(GEM, PersistentDataType.INTEGER, 1); rejected(state);
    }
    @Test public void emptyGemNameCannotRemoveEveryLoreLine() {
        Meta state = oldItem(1);
        assertThrows(IllegalArgumentException.class, () -> prepare(state, "", data -> {}));
        assertEquals(5, state.lore.size());
    }
    @Test public void lastGemRemovesOnlyItsExistingMarkers() {
        Meta state = oldItem(1);
        ItemMeta staged = prepare(state, NAME, data -> {data.remove(TIER); data.remove(RETURN);});
        assertFalse(staged.getPersistentDataContainer().has(GEM));
        assertFalse(staged.getPersistentDataContainer().has(SOCKET));
        assertFalse(staged.getPersistentDataContainer().has(TIER));
        assertFalse(staged.getPersistentDataContainer().has(RETURN));
        assertPreserved(state, staged);
        assertEquals(List.of(state.lore.getFirst()), staged.lore());
        assertEquals(Integer.valueOf(1), state.pdc.get(SOCKET, PersistentDataType.INTEGER));
        assertEquals(ID, state.pdc.get(GEM, PersistentDataType.STRING));
    }
    @Test public void multipleGemsRetainOtherIdentityAndTypedCount() {
        Meta state = oldItem(2); state.put(OTHER, PersistentDataType.STRING, "OTHER_GEM");
        ItemMeta staged = prepare(state, NAME, data -> data.remove(TIER));
        assertEquals(Integer.valueOf(1), staged.getPersistentDataContainer().get(SOCKET, PersistentDataType.INTEGER));
        assertEquals("OTHER_GEM", staged.getPersistentDataContainer().get(OTHER, PersistentDataType.STRING));
        assertTrue(staged.getPersistentDataContainer().has(RETURN));
        assertEquals(4, staged.lore().size());
        assertPreserved(state, staged);
    }
    @Test public void fiveSocketsRemainSupportedWithoutChangingTheLimit() {
        Meta state = oldItem(5);
        assertEquals(Integer.valueOf(4), prepare(state, NAME, data -> {}).getPersistentDataContainer()
                .get(SOCKET, PersistentDataType.INTEGER));
    }
    @Test public void absentLoreIsAllowedWhenSavedGemStateIsValid() {
        Meta state = oldItem(1); state.lore = null;
        ItemMeta staged = prepare(state, NAME, data -> {});
        assertTrue(staged.lore().isEmpty());
        assertNull(state.lore);
        assertPreserved(state, staged);
    }
    @Test public void cleanupFailureCannotChangeOriginalMetadata() {
        Meta state = oldItem(1);
        assertThrows(IllegalStateException.class, () -> prepare(state, NAME, data -> {
            data.remove(GEM); data.remove(OWNER); throw new IllegalStateException("injected cleanup failure");
        }));
        assertEquals(ID, state.pdc.get(GEM, PersistentDataType.STRING));
        assertEquals("old-owner-id", state.pdc.get(OWNER, PersistentDataType.STRING));
        assertEquals(5, state.lore.size());
    }
    @Test public void malformedComponentRefusesBeforeCleanup() {
        Meta state = oldItem(1); state.lore = new ArrayList<>(); state.lore.add(null);
        AtomicInteger cleanup = new AtomicInteger();
        assertThrows(RuntimeException.class, () -> prepare(state, NAME, data -> cleanup.incrementAndGet()));
        assertEquals(0, cleanup.get());
        assertEquals(ID, state.pdc.get(GEM, PersistentDataType.STRING));
    }
    @Test public void aNonIsolatedCloneIsRejected() {
        Meta state = oldItem(1); state.sameClone = true;
        assertThrows(IllegalArgumentException.class, () -> prepare(state, NAME, data -> fail("cleanup ran")));
    }
    @Test public void successPublishesBeforeConsumingExactlyOnce() {
        ItemMeta staged = prepare(oldItem(2), NAME, data -> {});
        List<String> events = new ArrayList<>();
        assertEquals(GemUnbindOperation.Result.SUCCESS, GemUnbindOperation.attempt(staged, 50,
                () -> {events.add("roll");return 50;}, () -> {events.add("check");return true;},
                meta -> {assertSame(staged, meta);events.add("commit");return true;}, () -> events.add("consume")));
        assertEquals(List.of("check", "roll", "commit", "consume"), events);
    }
    @Test public void validRandomFailureKeepsTheExistingToolCost() {
        ItemMeta staged = prepare(oldItem(2), NAME, data -> {});
        AtomicInteger cost = new AtomicInteger();
        assertEquals(GemUnbindOperation.Result.FAILED_ROLL, GemUnbindOperation.attempt(staged, 49,
                () -> 50, () -> true, meta -> {fail("metadata committed on failed roll");return false;}, cost::incrementAndGet));
        assertEquals(1, cost.get());
    }
    @Test public void staleItemsAreRejectedBeforeRandomnessOrConsumption() {
        assertEquals(GemUnbindOperation.Result.STALE_ITEMS, GemUnbindOperation.attempt(oldItem(1).meta, 100,
                () -> {fail("randomness used");return 0;}, () -> false,
                meta -> {fail("metadata committed");return true;}, () -> fail("tool consumed")));
    }
    @Test public void falseMetadataCommitDoesNotConsumeTheTool() {
        assertEquals(GemUnbindOperation.Result.REJECTED_METADATA, GemUnbindOperation.attempt(oldItem(1).meta, 100,
                () -> 0, () -> true, meta -> false, () -> fail("tool consumed")));
    }
    @Test public void thrownMetadataCommitDoesNotConsumeTheTool() {
        assertThrows(IllegalStateException.class, () -> GemUnbindOperation.attempt(oldItem(1).meta, 100,
                () -> 0, () -> true, meta -> {throw new IllegalStateException("injected rejection");}, () -> fail("tool consumed")));
    }
    @Test public void invalidRandomResultsNeverCommitOrConsume() {
        for (int value : new int[] {-1, 100}) {
            assertThrows(IllegalArgumentException.class, () -> GemUnbindOperation.attempt(oldItem(1).meta, 100,
                    () -> value, () -> true, meta -> {fail("commit");return true;}, () -> fail("consume")));
        }
    }
    @Test public void chanceComparisonMatchesEveryOriginalOutcome() {
        ItemMeta staged = oldItem(1).meta;
        for (int chance : new int[] {Integer.MIN_VALUE, -1, 0, 1, 25, 50, 75, 99, 100, Integer.MAX_VALUE}) {
            for (int value = 0; value < 100; value++) {
                final int roll = value;
                AtomicInteger commits = new AtomicInteger();
                AtomicInteger consumes = new AtomicInteger();
                var result = GemUnbindOperation.attempt(staged, chance, () -> roll, () -> true,
                        meta -> {commits.incrementAndGet();return true;}, consumes::incrementAndGet);
                assertEquals(value <= chance ? GemUnbindOperation.Result.SUCCESS : GemUnbindOperation.Result.FAILED_ROLL, result);
                assertEquals(value <= chance ? 1 : 0, commits.get());
                assertEquals(1, consumes.get());
            }
        }
    }
    @Test public void repeatedPreparationDoesNotEditOriginalOrAccumulateChanges() {
        Meta state = oldItem(2);
        ItemMeta first = prepare(state, NAME, data -> data.remove(TIER));
        for (int i = 0; i < 10; i++) {
            ItemMeta next = prepare(state, NAME, data -> data.remove(TIER));
            assertEquals(first.lore(), next.lore());
            assertEquals(Integer.valueOf(1), next.getPersistentDataContainer().get(SOCKET, PersistentDataType.INTEGER));
        }
        assertEquals(Integer.valueOf(2), state.pdc.get(SOCKET, PersistentDataType.INTEGER));
        assertEquals(5, state.lore.size());
    }

    private static void rejected(Meta state) {
        Map<NamespacedKey, Value> before = new HashMap<>(state.data);
        AtomicInteger cleanup = new AtomicInteger();
        assertThrows(IllegalArgumentException.class, () -> prepare(state, NAME, data -> cleanup.incrementAndGet()));
        assertEquals(0, cleanup.get());
        assertEquals(before, state.data);
    }
    private static ItemMeta prepare(Meta state, String name, Consumer<PersistentDataContainer> cleanup) {
        return GemUnbindOperation.prepare(state.meta, SOCKET, GEM, ID, name, cleanup);
    }
    private static void assertPreserved(Meta before, ItemMeta after) {
        assertEquals("old-owner-id", after.getPersistentDataContainer().get(OWNER, PersistentDataType.STRING));
        assertEquals(Float.valueOf(123.4567F), after.getPersistentDataContainer().get(CHARGE, PersistentDataType.FLOAT));
        assertArrayEquals(new byte[] {0, -1, 42}, after.getPersistentDataContainer().get(BYTES, PersistentDataType.BYTE_ARRAY));
        assertSame(before.name, after.displayName());
        assertSame(before.lore == null ? null : before.lore.getFirst(), after.lore().isEmpty() ? null : after.lore().getFirst());
    }
    private static Meta oldItem(int count) {
        Meta state = new Meta();
        state.name = Component.text("Player's item", NamedTextColor.AQUA);
        state.lore = new ArrayList<>(List.of(Component.text("Unrelated description", NamedTextColor.BLUE)
                .hoverEvent(HoverEvent.showText(Component.text("original hover"))),
                Component.empty(), GemLore.line(GemLore.HEADER), GemLore.line("§c◬ " + NAME), GemLore.line(GemLore.FOOTER)));
        state.put(SOCKET, PersistentDataType.INTEGER, count);
        state.put(GEM, PersistentDataType.STRING, ID);
        state.put(TIER, PersistentDataType.INTEGER, 2);
        state.put(RETURN, PersistentDataType.STRING, "weapon-marker");
        state.put(OWNER, PersistentDataType.STRING, "old-owner-id");
        state.put(CHARGE, PersistentDataType.FLOAT, 123.4567F);
        state.put(BYTES, PersistentDataType.BYTE_ARRAY, new byte[] {0, -1, 42});
        return state;
    }
    private static NamespacedKey key(String name) { return new NamespacedKey("fnamplifications", name); }
    private record Value(Class<?> type, Object data) {}
    private static final class Meta {
        private final Map<NamespacedKey, Value> data = new HashMap<>();
        private List<Component> lore;
        private Component name;
        private boolean sameClone;
        private final PersistentDataContainer pdc;
        private final ItemMeta meta;
        private Meta() {
            pdc = (PersistentDataContainer) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[] {PersistentDataContainer.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "get" -> {
                        Value value = data.get(args[0]);
                        if (value == null) return null;
                        Class<?> type = ((PersistentDataType<?, ?>) args[1]).getPrimitiveType();
                        if (!type.equals(value.type())) throw new IllegalArgumentException("Wrong primitive type");
                        return value.data();
                    }
                    case "has" -> {
                        Value value = data.get(args[0]);
                        return value != null && (args.length == 1 || value.type().equals(((PersistentDataType<?, ?>) args[1]).getPrimitiveType()));
                    }
                    case "set" -> {data.put((NamespacedKey) args[0], new Value(((PersistentDataType<?, ?>) args[1]).getPrimitiveType(), args[2]));return null;}
                    case "remove" -> {data.remove(args[0]);return null;}
                    case "isEmpty" -> {return data.isEmpty();}
                    case "getKeys" -> {return java.util.Set.copyOf(data.keySet());}
                    default -> throw new AssertionError("Unexpected PDC method: " + method);
                }
            });
            meta = (ItemMeta) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {ItemMeta.class},
                    (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getPersistentDataContainer" -> {return pdc;}
                    case "clone" -> {
                        if (sameClone) return proxy;
                        Meta copy = new Meta();
                        data.forEach((key, value) -> copy.data.put(key, new Value(value.type(),
                                value.data() instanceof byte[] bytes ? bytes.clone() : value.data())));
                        copy.lore = lore == null ? null : new ArrayList<>(lore);
                        copy.name = name;
                        return copy.meta;
                    }
                    case "lore" -> {
                        if (args == null || args.length == 0) return lore;
                        @SuppressWarnings("unchecked") List<Component> supplied = (List<Component>) args[0];
                        lore = supplied == null ? null : new ArrayList<>(supplied);return null;
                    }
                    case "displayName" -> {return name;}
                    default -> throw new AssertionError("Unexpected ItemMeta method: " + method);
                }
            });
        }
        private <P,C> void put(NamespacedKey key, PersistentDataType<P,C> type, C value) {
            data.put(key, new Value(type.getPrimitiveType(), value));
        }
    }
}
