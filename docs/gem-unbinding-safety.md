# Preserve existing items when gem unbinding cannot complete

This follow-up builds on the component-preservation branch at `02f395bb8f0c669fe4b490d47d8821378a0ba134`. It does not replace that work or change binding/upgrade implementations.

## Failure corrected

The original unbinder consumes the current main-hand stack before validating the selected gem and typed socket count, then ignores the result of `setItemMeta`. Missing/wrong-typed saved fields can therefore spend a tool without completing an unbind. An expired selector can also dereference a missing chance or use a different tool.

The candidate prepares the whole edit on cloned metadata first. It requires the exact selected gem ID and an INTEGER socket count within the existing one-to-five limit. Missing or inconsistent state is rejected rather than guessed or rewritten. The existing optional tier/Retaliate cleanup callback remains invoked on the detached metadata. Only a successfully applied metadata update can consume a successful attempt's tool. A valid random failure still consumes the tool exactly as before, using the original `nextInt(100) <= chance` comparison and whole-stack cost. Normal effect rates, recipes and binding/upgrade costs are untouched.

Selector clicks are cancelled before inspecting items. The pending chance is single-use and checked against the currently held unbinder. Closing/quitting clears stale entries; opening a replacement selector records its new chance after the old close event. The task also rechecks held-item snapshots before applying the prepared operation. Inventory titles use the supported Adventure overload without changing visible text.

No item/research IDs, persistent key names/types, unknown-addon fields, stored formats, configuration defaults or recipe definitions change. No Doctor migration or automatic repair is introduced. Existing public task/listener methods remain available; malformed/expired attempts intentionally fail without cost instead of following the old failure path.

## Validation scope

`GemUnbindOperationTest` contains 22 JUnit cases and 1,000 comparisons with the original chance predicate. It runs the actual preparation/attempt helper and Adventure component editing, using explicitly declared ItemMeta/PDC interface doubles and controlled commit/consume callbacks. It checks exact typed values, original rich lore/name retention, invalid and absent fields, failure isolation, clone safety, valid costs and metadata-commit ordering. This is not a real player inventory, production persistence transaction or concurrent Folia certification.

Full plugin builds, normal Legacy/United API checks and supported Paper API matrix must pass for this candidate. Live event and real ItemStack checks are separate evidence. A nonstandard ItemMeta setter that mutates then throws, a failing consumption implementation, physical crashes or external concurrent inventory edits are not covered by the callback ordering contract. No version bump or release is authorized by tests alone.
