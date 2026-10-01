package ne.fnfal113.fnamplifications.gems.implementation;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.Test;

/** Presentation protocol tests; binding keys, chance rolls and item consumption stay in existing callers. */
public class GemLoreTest {
    private static final String GEM = "§bLife Gem I";
    private static final String OTHER = "§aOther Gem I";
    private static final Component RICH = Component.text("Existing lore", NamedTextColor.BLUE).hoverEvent(Component.text("Preserve hover"));
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Test public void firstBindingKeepsExistingRichLoreAndAppendsFourLines() {
        var result = GemLore.bound(List.of(RICH), 0, GEM);
        assertEquals(5, result.size());
        assertSame(RICH, result.getFirst());
        assertEquals(List.of("", GemLore.HEADER, "§c◬ " + GEM, GemLore.FOOTER), strings(result.subList(1, result.size())));
    }
    @Test public void noLoreFirstBindingCreatesTheExistingHeaderAndFooter() {
        assertEquals(4, GemLore.bound(null, 0, GEM).size());
    }
    @Test public void additionalBindingInsertsDirectlyAfterHeader() {
        var original = GemLore.bound(List.of(RICH), 0, GEM);
        var result = GemLore.bound(original, 1, OTHER);
        assertEquals(6, result.size());
        assertSame(RICH, result.getFirst());
        assertEquals("§c◬ " + OTHER, strings(result).get(3));
        assertEquals("§c◬ " + GEM, strings(result).get(4));
        assertEquals(5, original.size());
    }
    @Test public void missingHeaderRetainsOldNoInsertionBehaviorForExistingGems() {
        assertEquals(List.of(RICH), GemLore.bound(List.of(RICH), 2, GEM));
    }
    @Test public void multipleHeadersRetainTheOldInsertionRule() {
        var source = List.of(GemLore.line(GemLore.HEADER), RICH, GemLore.line(GemLore.HEADER));
        assertEquals(5, GemLore.bound(source, 2, GEM).size());
    }
    @Test public void removingOneGemKeepsOthersAndOriginalRichLines() {
        var source = GemLore.bound(GemLore.bound(List.of(RICH), 0, GEM), 1, OTHER);
        var result = GemLore.unbound(source, GEM, false);
        assertSame(RICH, result.getFirst());
        assertTrue(strings(result).contains("§c◬ " + OTHER));
        assertFalse(strings(result).contains("§c◬ " + GEM));
        assertTrue(strings(result).contains(GemLore.HEADER));
    }
    @Test public void lastGemRemovesItsSeparatorHeaderFooterAndTarget() {
        var result = GemLore.unbound(GemLore.bound(List.of(RICH), 0, GEM), GEM, true);
        assertEquals(List.of(RICH), result);
        assertSame(RICH, result.getFirst());
    }
    @Test public void lastGemAtFirstLineDoesNotRemoveAnImaginaryPrecedingLine() {
        var source = List.of(GemLore.line(GemLore.HEADER), GemLore.line("§c◬ " + GEM), GemLore.line(GemLore.FOOTER), RICH);
        assertEquals(List.of(RICH), GemLore.unbound(source, GEM, true));
    }
    @Test public void absentLoreUnbindingDoesNotInventPresentation() {
        assertTrue(GemLore.unbound(null, GEM, true).isEmpty());
        assertTrue(GemLore.unbound(null, GEM, false).isEmpty());
    }
    @Test public void nonLastUnbindDoesNotRemoveHeaderOrFooter() {
        var source = GemLore.bound(List.of(RICH), 0, GEM);
        var result = GemLore.unbound(source, GEM, false);
        assertTrue(strings(result).contains(GemLore.HEADER));
        assertTrue(strings(result).contains(GemLore.FOOTER));
    }
    @Test public void missingTargetPreservesNonLastLoreExactly() {
        var source = GemLore.bound(List.of(RICH), 0, GEM);
        assertEquals(source, GemLore.unbound(source, OTHER, false));
    }
    @Test public void upgradePreservesAllUntargetedComponents() {
        var source = GemLore.bound(List.of(RICH), 0, GEM);
        var result = GemLore.upgraded(source, GEM, Component.text("Life Gem II", NamedTextColor.AQUA));
        assertSame(RICH, result.getFirst());
        assertEquals(source.get(2), result.get(2));
        assertEquals(source.getLast(), result.getLast());
    }
    @Test public void upgradeKeepsRichReplacementName() {
        var source = List.of(GemLore.line("§c◬ " + GEM));
        var result = GemLore.upgraded(source, GEM, RICH);
        assertEquals(RICH, result.getFirst().children().get(1));
    }
    @Test public void upgradeMissingMatchDoesNotAppendNewLore() {
        assertEquals(List.of(RICH), GemLore.upgraded(List.of(RICH), GEM, Component.text("new")));
    }
    @Test public void helpersNeverMutateTheirInputs() {
        var source = new ArrayList<>(GemLore.bound(List.of(RICH), 0, GEM));
        var before = List.copyOf(source);
        GemLore.bound(source, 1, OTHER);
        GemLore.unbound(source, GEM, true);
        GemLore.upgraded(source, GEM, RICH);
        assertEquals(before, source);
    }
    @Test public void originalWellFormedListRulesMatchAcrossDeterministicExamples() {
        Random random = new Random(951127L);
        for (int trial = 0; trial < 500; trial++) {
            List<Component> source = new ArrayList<>();
            for (int i = random.nextInt(8); i > 0; i--) source.add(Component.text("line-" + i, NamedTextColor.BLUE));
            List<String> original = strings(source);
            List<String> bound = new ArrayList<>(original);
            bound.addAll(List.of("", GemLore.HEADER, "§c◬ " + GEM, GemLore.FOOTER));
            assertEquals(bound, strings(GemLore.bound(source, 0, GEM)));
            List<String> additional = new ArrayList<>(bound);
            for (int i = 0; i < additional.size(); i++) {
                if (additional.get(i).startsWith(GemLore.HEADER)) additional.add(i + 1, "§c◬ " + OTHER);
            }
            var newAdditional = GemLore.bound(GemLore.bound(source, 0, GEM), 1, OTHER);
            assertEquals(additional, strings(newAdditional));
            List<String> nonLast = new ArrayList<>(additional);
            nonLast.removeIf(line -> line.contains(GEM));
            assertEquals(nonLast, strings(GemLore.unbound(newAdditional, GEM, false)));
            List<String> last = new ArrayList<>(bound);
            for (int i = 0; i < last.indexOf(GemLore.HEADER) + 1; i++) {
                if (last.get(i).contains(GemLore.HEADER)) last.remove(i - 1);
            }
            last.removeIf(line -> line.contains(GEM) || line.contains(GemLore.HEADER) || line.contains(GemLore.FOOTER));
            assertEquals(last, strings(GemLore.unbound(GemLore.bound(source, 0, GEM), GEM, true)));
            assertEquals(original, strings(source));
        }
    }
    private static List<String> strings(List<Component> lore) {
        return new ArrayList<>(lore.stream().map(LEGACY::serialize).toList());
    }
}
