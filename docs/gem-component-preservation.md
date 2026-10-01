# Gem presentation modernization

Binding, upgrading and unbinding now retain the original Adventure components for unaffected item lore. Only gem lines are generated or replaced; legacy matching is used only to retain the established header/name/footer protocol. The three callers keep their original item consumption, socket limits, chance comparison, reverse tier order, persistent key names/types, messages and effects.

The new helper handles missing unbind lore or a header at the first line without an invalid preceding-line removal. This prevents a malformed legacy description from throwing during lore cleanup; it is not a generic repair for missing gem PDC or invalid tier data. Normal well-formed insertion and removal rules are retained, including their historical matching behavior.

Sixteen component tests cover retained hover/style data, absent lore, exact insertion/removal order, missing matches, duplicate headers and detached inputs. Five hundred deterministic examples compare four old list algorithms (2,000 comparisons), separate from the JUnit case count. These tests do not simulate chance rolls, player inventory transactions or live machine ticking. No schema, recipe, item/research ID, resource-pack mapping or new runtime dependency changes.

Run the normal Legacy/United API build and supported Paper matrix, then the coordinated exact-core bundle tests before release. Other deprecated call sites remain separate audited work. No version bump, merge or release is included here.
