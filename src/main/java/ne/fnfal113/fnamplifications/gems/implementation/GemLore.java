package ne.fnfal113.fnamplifications.gems.implementation;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/** Edits only gem presentation; no randomness, consumption, keys or gem-tier state lives here. */
final class GemLore {
    static final String HEADER = "§6◤◤◤◤◤◤| §d§lGems §c|◥◥◥◥◥◥";
    static final String FOOTER = "§6◤◤◤◤◤◤◤◤◤◤◤§c◥◥◥◥◥◥◥◥◥◥◥";
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private GemLore() {}

    static List<Component> bound(List<Component> source, int existingGems, String gemName) {
        List<Component> result = copy(source);
        Component gem = line("§c◬ " + gemName);
        if (existingGems == 0) {
            result.add(Component.empty());
            result.add(line(HEADER));
            result.add(gem);
            result.add(line(FOOTER));
        } else {
            for (int i = 0; i < result.size(); i++) {
                if (LEGACY.serialize(result.get(i)).startsWith(HEADER)) result.add(++i, gem);
            }
        }
        return result;
    }

    static List<Component> upgraded(List<Component> source, String namePrefix, Component replacement) {
        List<Component> result = copy(source);
        Component name = replacement == null ? Component.empty() : replacement.colorIfAbsent(NamedTextColor.RED);
        Component gem = Component.empty().decoration(TextDecoration.ITALIC, false)
                .append(Component.text("◬ ", NamedTextColor.RED)).append(name);
        for (int i = 0; i < result.size(); i++) {
            if (LEGACY.serialize(result.get(i)).contains(namePrefix)) result.set(i, gem);
        }
        return result;
    }

    static List<Component> unbound(List<Component> source, String gemName, boolean lastGem) {
        List<Component> result = copy(source);
        // Compare legacy text only where the old protocol needs it. Preserve original
        // rich components for every retained line rather than rebuilding all lore.
        List<String> text = new ArrayList<>(result.size());
        for (Component component : result) text.add(LEGACY.serialize(component));
        if (lastGem) {
            for (int i = 0; i < text.indexOf(HEADER) + 1; i++) {
                // Missing or malformed old lore may have no preceding separator.
                if (i > 0 && text.get(i).contains(HEADER)) {
                    result.remove(i - 1);
                    text.remove(i - 1);
                }
            }
        }
        for (int i = text.size() - 1; i >= 0; i--) {
            String value = text.get(i);
            if (value.contains(gemName) || lastGem && (value.contains(HEADER) || value.contains(FOOTER))) result.remove(i);
        }
        return result;
    }

    static Component line(String legacyText) {
        return LEGACY.deserialize(legacyText).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    private static List<Component> copy(List<Component> source) {
        return source == null ? new ArrayList<>() : new ArrayList<>(source);
    }
}
