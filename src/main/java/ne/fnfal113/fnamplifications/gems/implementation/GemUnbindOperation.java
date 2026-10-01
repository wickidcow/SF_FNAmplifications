package ne.fnfal113.fnamplifications.gems.implementation;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/** Prepares the full gem edit before a valid attempt may consume its tool. */
final class GemUnbindOperation {
    enum Result { SUCCESS, FAILED_ROLL, STALE_ITEMS, REJECTED_METADATA }

    private GemUnbindOperation() {}

    static ItemMeta prepare(ItemMeta original, NamespacedKey socketKey, NamespacedKey gemKey,
            String gemId, String gemName, Consumer<PersistentDataContainer> removeExtras) {
        Objects.requireNonNull(original, "Missing offhand metadata");
        Objects.requireNonNull(gemId, "Missing gem identity");
        Objects.requireNonNull(gemName, "Missing gem name");
        if (gemName.isEmpty()) throw new IllegalArgumentException("Missing gem name");
        Objects.requireNonNull(removeExtras, "Missing gem cleanup");
        PersistentDataContainer source = original.getPersistentDataContainer();
        Integer count = source.get(socketKey, PersistentDataType.INTEGER);
        String storedGem = source.get(gemKey, PersistentDataType.STRING);
        if (count == null || count < 1 || count > 5 || !gemId.equals(storedGem)) {
            throw new IllegalArgumentException("Invalid saved socket count or selected gem identity");
        }

        ItemMeta staged = original.clone();
        if (staged == original) {
            throw new IllegalArgumentException("Item metadata did not provide an isolated copy");
        }
        // Compute presentation before invoking cleanup; malformed text must not consume anything.
        var lore = GemLore.unbound(staged.lore(), gemName, count == 1);
        PersistentDataContainer data = staged.getPersistentDataContainer();
        removeExtras.accept(data);
        data.remove(gemKey);
        if (count == 1) {
            data.remove(socketKey);
        } else {
            data.set(socketKey, PersistentDataType.INTEGER, count - 1);
        }
        staged.lore(lore);
        return staged;
    }

    static Result attempt(ItemMeta staged, int chance, IntSupplier roll, BooleanSupplier unchanged,
            Predicate<ItemMeta> commit, Runnable consume) {
        Objects.requireNonNull(staged, "Missing staged metadata");
        Objects.requireNonNull(roll, "Missing roll");
        Objects.requireNonNull(unchanged, "Missing item check");
        Objects.requireNonNull(commit, "Missing metadata commit");
        Objects.requireNonNull(consume, "Missing tool consumption");
        if (!unchanged.getAsBoolean()) return Result.STALE_ITEMS;

        int value = roll.getAsInt();
        if (value < 0 || value >= 100) throw new IllegalArgumentException("Roll must be in [0, 100)");
        // Retain the original <= comparison, including existing boundary/config behavior.
        boolean success = value <= chance;
        if (success && !commit.test(staged)) return Result.REJECTED_METADATA;
        // A valid random failure still costs the tool. Invalid/stale/uncommitted attempts do not.
        consume.run();
        return success ? Result.SUCCESS : Result.FAILED_ROLL;
    }
}
