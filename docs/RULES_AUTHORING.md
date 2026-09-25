# Rules implementation guide

Practical command, memory and text-replacement integration for Starsector **0.98a-RC8**. Read the relevant sections when using, changing or debugging these mechanisms, including Java-only work on existing code. This is not limited to adding new plugins or keys.

[RULES.md](RULES.md) owns rules syntax, execution, Lost.Sector routing contracts and CSV validation. [RULES_WRITING.md](RULES_WRITING.md) owns how to structure rules content and the writing process. This guide owns the full command/key usage procedures and the source corrections below. Use all three for rules implementation; none governs prose. For player-facing text use [DIALOGUE.md](DIALOGUE.md), and for repository workflow use [CLAUDE.md](../CLAUDE.md#which-guide-to-read).

Start with the relevant dictionary's scope notes, then look up the command or key and inspect its source/context. The dictionaries are lookup references, not required cover-to-cover reading:

- [Vanilla commands](rules-reference/COMMANDS.md): common recipes and an index of the vanilla command classes, with actual CSV calls.
- [Memory and text dictionary](rules-reference/MEMORY.md): pronouns, names, generated facts, state flags and their contexts.
- [Vanilla key usage](rules-reference/KEY_USAGE.md): every literal key reference found in the active vanilla rules, including mission-specific keys. This is a lookup index, not permission to reuse mission state.
- [Project rules guide](RULES.md): CSV editing, Lost.Sector routes and fleet/bar teardown.

These are project-owned references. The preserved documents under `docs/rules/` describe an external simulator and contain approximations and conflicts with this game build. Use the source corrections below for the listed cases; this does not give the entire guide blanket precedence over unrelated technical contracts. For other discrepancies, follow [Technical references and conflicts](../CLAUDE.md#technical-references-and-conflicts). Do not change the vendored files to hide the differences.

## Choose an existing mechanism

| Need | First choice |
|---|---|
| Refer to the current speaker, player, market or faction | Existing generated token or fact in the memory dictionary |
| Add text, highlight it, change options, display a portrait, grant ordinary cargo/credits | Existing vanilla command; check its arguments and target requirements |
| Remember a choice or expose a calculated description | Namespaced memory key, with an explicit owner and lifetime |
| Offer or complete an existing quest, job or contract, or pay its rewards | Its existing owner in [ARCHITECTURE.md](ARCHITECTURE.md); `Call` on its stored mission reference where one exists, such as `Call $nskr_contracts_ref <action>` |
| Expose a Lost.Sector action or query not covered above | Extend the existing command in `lostsector.dialogue.rules` that owns the feature, delegating mechanics to their owner; see [project routing](RULES.md#project-routing) |
| Integrate an independent reusable framework | A separate namespaced command can be justified by the framework boundary; do not create one per dialogue row |
| Derive replacements for many unrelated interactions | Consider `RuleTokenReplacementGeneratorPlugin`; ordinary quest text does not need one |

Do not substitute a new helper for `AddCredits`, `Highlight`, `SetTooltip`, `ShowPersonVisual`, `MakeOtherFleetGoAway`, or another existing command just because the name was not in the old guide.

## Memory lifetime

The number is an **expiry in campaign days**, not a Boolean persistence flag.

| Rules Script | Java on the selected `MemoryAPI` | Lifetime |
|---|---|---|
| `$nskr_example = true` | `mem.set("$nskr_example", true)` | No expiry. Survives dialogue closure on a persistent owner until overwritten, unset or the owner is removed. |
| `$nskr_example = true 0` | `mem.set("$nskr_example", true, 0f)` | Removed on the next memory advance that takes its timer below zero. Normally this is after the dialogue closes and campaign time resumes. |
| `$nskr_example = true 30` | `mem.set("$nskr_example", true, 30f)` | Survives into the campaign and expires after the timer runs down. |
| `unset $nskr_example` | `mem.unset("$nskr_example")` | Remove the value now; use when another route in the same dialogue must no longer see it. |
| `expire $nskr_example 0` | `mem.expire("$nskr_example", 0f)` | Change the existing entry's expiry, without replacing its value. |

`set(key, value)` replaces the current value and removes its old expiry. It does not merge an old String, collection or saved object with the new one. `set(key, value, duration)` replaces the value and sets/replaces the expiry. A negative expiry removes the expiry timer; prefer the two-argument setter when expressing a persistent write.

Vanilla uses `0` extensively for interaction facts and text prepared for the current conversation. Zero is **not** immediate removal, a wall-clock frame counter or a guarantee that closing a particular panel performs cleanup. Memory advancement is pause-gated; a custom dialog/memory owner may have a different lifecycle. Explicitly unset a scratch key if another conversation can reuse the same owner before time advances. A newly created standalone memory object also needs its owner to advance it.

Do not refresh a timed key during every read or every `advance()` unless restarting its timer is intended. Keep saved quest targets and completion flags persistent; recompute their disposable display strings with `0`. The dialogue must not reroll the saved target when preparing those strings.

Source: `campaign/rules/Memory.java` (`set`, `expire`, `advance`) in the decompiled game; assignment execution in the same package's expression implementation. `Memory.advance` converts elapsed time to campaign days and expires entries only when the remaining time is below zero.

## Memory ownership and scopes

`local` means the memory selected for this interaction, **not** a dictionary automatically destroyed when the window closes.

| Rules prefix | Normal owner / availability |
|---|---|
| `$local.key` or unqualified `$key` in an expression | Interaction target's memory; switches to the active person's memory when talking to a person |
| `$entity.key` | The underlying target while an active person occupies `local`; not always a separate scope |
| `$player.key` | `Global.getSector().getCharacterData().getMemory()` in the standard rules dialog; not the player's fleet memory |
| `$global.key` | Sector memory |
| `$market.key` | Interaction target's market, if there is one |
| `$sourceMarket.key` | Market resolved from the target's source-market key, if available |
| `$faction.key` | Interaction target's faction |
| `$personFaction.key` | Active person's faction, when a person is active |
| `$mission.key` | Memory exposed by the active legacy campaign event, if installed; not a universal `BaseHubMission` scope |

These mappings come from `RuleBasedInteractionDialogPluginImpl.updateMemory`, `updatePersonMemory` and `setActiveMission`. Bar wrappers and custom dialogs can supply their own map. Inspect that wrapper instead of assuming every scope exists. `BaseCommandPlugin.getEntityMemory(memoryMap)` returns the entity scope when available and otherwise local; use it when a command must address the underlying entity rather than its active person.

In Java, the stored key still starts with `$`, but the scope is selected separately:

```java
MemoryAPI player = memoryMap.get(MemKeys.PLAYER);
player.set("$nskr_example", true);
```

Read that in rules as `$player.nskr_example`. Do not store a literal key named `$player.nskr_example` in local memory. Do not write to player-fleet memory and expect `$player` to find it.

Check optional scopes explicitly. `Misc.Token.getVarNameAndMemory` falls back to local when the requested scope is absent, retaining the original token as the key. Thus a misspelled or unavailable scope can accidentally address a literal `$scope.key` in local rather than fail as intended. In Java, test `memoryMap.get(scope)` before setting an owner-specific value.

Vanilla refreshes many facts through `getMemory()` and campaign-plugin fact hooks. `getMemoryWithoutUpdate()` reads the existing storage without that refresh. Facts such as `$credits`, `$size` and `$isHostile` are derived data, not the APIs for changing cargo, market size or hostility.

## Text replacements are not all memory keys

There are two paths:

1. Registered replacement generators supply context-derived Strings such as `$himOrHer` and `$playerName`.
2. `Misc.replaceTokensFromMemory` substitutes eligible values from the supplied memory map.

`RulesAPI.performTokenReplacement` runs the generators first, then the memory substitution pass. A built-in text token is therefore not proof that a corresponding memory key exists for a condition. For example, `$himOrHer` can be available to Text through the generator; do not treat it as an independently stored mission fact.

The memory pass checks local first, sorts longer keys before shorter ones, and replaces both scoped and unscoped spellings. Other scopes can supply an unqualified text replacement if an earlier pass did not replace it. This is broader than expression lookup, where an unqualified variable selects local. Use explicit scopes for memory-backed text whenever ownership matters; do not rely on collisions between identically named keys on different owners.

The built-in pass derives person tokens from the target's active person, falling back to the commander for a fleet. `BaseHubMission.setPersonTokens` also supplies a subset of person tokens in appropriate mission/bar interactions. Merely drawing a portrait with `ShowPersonVisual` does not change the active speaker. `BeginConversation` selects the person and notifies the rule-based dialog so its memory map is updated.

For a second person who is not the active speaker, use a namespaced token derived from that specific saved `PersonAPI`; do not overwrite `$himOrHer` and hope it changes the built-in generator. The dictionary lists exact capitalization variants. Arbitrary variants are not created automatically.

### Create a custom text token

No new plugin is needed for a constant or already-known value. A Script can set a namespaced String, with `0` for a temporary presentation value. For a calculated value, use the existing mission's interaction-data preparation or the owning project command's token preparation:

```java
MemoryAPI local = memoryMap.get(MemKeys.LOCAL);
local.set("$nskr_exampleName", target.getName(), 0f);
```

Use `$nskr_exampleName` in Text, or `$local.nskr_exampleName` when deliberately selecting local memory. The example key is illustrative, not an existing mod API.

Prepare the value **before the rule displays it**. `FireBest` adds collected options, displays the Text column, then runs the ordinary Script. `FireAll` builds the combined option set first, then displays each matched rule's text and runs its script. Thus a Script assignment cannot supply its own row's earlier Text or options. Use an earlier preparation step followed by a private display trigger, or an explicit later `AddText` call within Script. A preceding row inside the same `FireAll` can prepare later text, but not options that were already built.

Use `String` for display values. The memory replacement implementation handles Strings, Booleans, Floats and Integers; an arbitrary object, Long or enum is not automatically a useful text replacement. Values print with `toString()`, so a Float always shows a decimal point: `mem.set(key, 5f)` and a rules `$x++` both display as `5.0`/`6.0`, while a rules `$x = 5` stores the String `"5"`. Keep the object separately and derive a display String. `BaseHubMission.set` converts enums to names, but raw `MemoryAPI.set` does not provide that convenience.

Text replacement uses Java regex replacement internally. Literal `$` and backslashes in replacement values can be interpreted specially. Do not feed arbitrary user/imported strings or unresolved nested tokens through it without checking escaping and the exact path. Values from generators and from memory both reach `replaceAll` unescaped, so a player-chosen name must be written through `Matcher.quoteReplacement` before it is stored as a token. Do not make correctness depend on recursive replacement or an assumed order among equal-length keys.

For Java-authored text, plain `textPanel.addPara()` does not imply rules substitution. Explicitly call `performTokenReplacement(ruleId, text, target, memoryMap)`, use a Token's `getStringWithTokenReplacement`, or supply normal Java formatting arguments as appropriate. A replacement generator can be registered with `RulesAPI.addTokenReplacementGenerator`; it must return namespaced values, handle absent context, and follow the owning mod's load/registration lifecycle. Do not register a new generator every time a row fires.

## Rules text outside a dialog

Java can match rows and read their text without an open dialog, as the quest framework's `QuestText` does for intel entries ([quest framework, Intel](../jars/src/lostsector/quest/README.md#intel)). Verified in the 0.98a-RC8 source; bundle line numbers refer to `starsector-knowledge`:

- **Matching.** `RulesAPI.getBestMatching(currentRule, trigger, dialog, memoryMap)` and `getAllMatching(...)` accept a null dialog: `Rules` passes it unchanged to every condition's `isTrueFor(ruleId, dialog, memoryMap)` and reads nothing else from it (`sources-obf/campaign.rules.java` 434–478 and 480–549). A memory condition needs only the map. A command condition receives the null dialog; when the command throws, the expression rethrows the exception if the dialog is null, where with a dialog it prints the message and counts the line as failed (921–945). Rows matched this way therefore use only memory conditions and commands that never touch the dialog.
- **Order.** `getAllMatching` returns the matches in load order within the trigger and ignores scores; `getBestMatching` sums condition scores and picks at random among the highest (540–542). Both skip the row whose id equals `currentRule`.
- **Scopes.** `Misc.Token.getVarNameAndMemory` falls back to `local` when a key's scope is missing from the map and throws `RuleException` when `local` is missing too (`sources-api/util.java` 1832–1846). The map needs at least `MemKeys.LOCAL`.
- **Text.** The loader strips trailing whitespace from the Text cell and splits it on `\nOR\n` (`sources-obf/campaign.rules.java` 610–617). Whether an empty cell arrives as an empty String or as no text depends on `LoadingUtils`, which is not in the sources, so a reader handles both an empty variant and none. `RuleAPI.pickText()` picks uniformly among the variants on every call, and returns null when there are none (1211–1217). Reading text does not run the row's Script; `runScript` would (1230–1236).
- **Replacement.** `performTokenReplacement(ruleId, text, entity, memoryMap)` hands the entity, which may be null, to every generator, then runs the memory pass unless the map is null (`sources-obf/campaign.java` 5026–5050). With a null entity, vanilla's `CoreRuleTokenReplacementGeneratorImpl` supplies only the player tokens and `$shipOrFleet` family; it reads the player fleet (`sources-api/impl.campaign.java` 6639–6893). The memory pass, `Misc.replaceTokensFromMemory`, runs two regex replacements for every key of every memory in the map (`sources-api/util.java` 2102–2125), so its cost grows with the key count of sector or player memory. Pass only the memories the text reads.
- **Memory without refresh.** The sector's and `CharacterDataAPI`'s `getMemory()` run every campaign plugin's `updateGlobalFacts` or `updatePlayerFacts` before returning; `getMemoryWithoutUpdate()` does not (`sources-obf/campaign.java` 5052–5066 and 16010–16023). Derived facts read without the refresh can be stale.
- **Scratch memory.** `Global.getFactory().createMemory()` returns a new empty `Memory` (`sources-obf/campaign.java` 5067–5069), as vanilla's new-game dialog uses for its `global` scope. Nothing advances it, so expiries on it never run.
- **Reading a Script without running it.** `RuleAPI.getScriptCopy()` returns the Script lines as `ExpressionAPI`, which exposes only `execute`, `doesCommandAddOptions` and `getOptionOrder` (`sources-api/campaign.rules.java` 16–20; `sources-obf/campaign.rules.java` 1199). The engine's expression class (obfuscated name) is public and has the public methods `getCommandClass()`, the fully qualified command class resolved at load through `ruleCommandPackages` or null for a line that is not a command, and `getCommandParams()`, the argument tokens as `List<Misc.Token>` without the command name (`sources-obf/campaign.rules.java` 723, 725–727, 749–767, 777, 1149). The quest framework calls them by name through `MethodHandles.publicLookup()` (`quest/RuleScript`), because the script class loader rejects `java.lang.reflect` ([ARCHITECTURE.md](ARCHITECTURE.md#rendering-ui-reflection-and-audio)). A command's arguments then resolve as the command would: `SetTextHighlights` reads `Token.getStringWithTokenReplacement`, which is `getString(memoryMap)` followed by `performTokenReplacement` with the dialog's target, and `SetTextHighlightColors` reads `Token.getColor(memoryMap)` (`sources-api/impl.campaign.java` 160132–160184; `sources-api/util.java` 1913–1952). `Highlight` is an empty subclass of `SetTextHighlights` (155646).

## Command invocation and Java integration

Scripts contain one invocation per line. The first token is the exact command class name, followed by that command's arguments. Bare assignments are expression syntax, not a plugin named `set`.

```text
AddCredits $nskr_examplePayment
SetTooltip nskr_exampleOption "$nskr_exampleTooltip"
FireBest nskr_exampleDisplay
```

These namespaced example arguments/triggers must be provided by the calling code or rows; they are not built-in Lost.Sector actions.

Do not infer argument handling from another plugin. `getString(memoryMap)` resolves a variable argument; `getStringWithTokenReplacement(...)` also expands tokens inside text; `.string` uses the raw token. For example, `SetTooltip` expands its text, while `SetOptionText` reads its replacement text literally. Quoting preserves spaces; CSV quoting adds its own doubled-quote layer.

When used in Conditions, a plugin's Boolean result determines eligibility. Conditions may be evaluated while several candidate rows are being matched. Do not grant rewards, consume cargo, accept jobs or make a new random target there. A repeatable preparation query may refresh scratch tokens if the established project path requires it. A false Script result is not a transaction rollback or a general instruction to stop subsequent lines; validate before starting mutations.

### Reuse a mission object through Call

`Call` is an alias of `CallEvent`. Its first argument must resolve to an object implementing `CallEvent.CallableEvent`. Remaining tokens are delivered to `callEvent(...)`; they are not reflected into an arbitrary Java method.

`BaseHubMission` implements the dispatch needed by existing hub missions such as `ContractsMission`. Its `callEvent` handles `endFailure`, `makeUnimportant`, `showMap <title>`, `hideMap`, `updateStage`, `updateData` (stage check, then token preparation), `addContacts`, `repSuccess` and `repFailure` itself; any other action goes to the subclass's `callAction`, and when that returns false `callEvent` throws `Unhandled action`. Extend the existing `callAction` path for a genuinely new mission action, delegate shared behavior to the base implementation, and return true when handled. Unknown actions must not silently appear successful.

Hub mission rows follow vanilla's naming. A mission offered by a contact fires `<missionId>_blurb` and `<missionId>_option` with `FireBest ... true`; one offered at the bar fires `<missionId>_blurbBar` and `<missionId>_optionBar`, with the mission's person as `$local` during those two fires. The row that answers the offer option sets `$missionId = <missionId>`. `updateInteractionData` writes `$<missionId>_stage` (the current stage's enum name) and, for a bar event or when no `$entity` scope exists, the person tokens before the mission's rows show, then calls `updateInteractionDataImpl()`, the hook for the mission's own tokens. For a contact's missions, `BaseMissionHub.updateOfferedMissions` rebuilds the offered list on every `prepare` and calls `updateInteractionData` after each successful `create`, so those tokens are ready for `_blurb`, `_option` and the accept row without a `Call ... updateData`. A mission that ends at acceptance, as `ContractsMission` does, reaches the success path through `setSuccessStage(stage)` and `setCurrentStage(stage, null, null)`: `setCurrentStage` calls `endSuccess`, which grants the success reputation (person `HIGH`, faction `MEDIUM` unless `setNoRepChanges()`), counts a completion and calls `abort()`; a null dialog skips the end update message. Keep the mission reference in the correct owner and save-compatible; rebuild temporary interaction bindings through the existing framework.

Use `FireBest`/`FireAll` for trigger routing. `Call someRuleId` does not run that rule.

### Create a command only when needed

For Lost.Sector, first look for a suitable existing command in [`lostsector.dialogue.rules`](../jars/src/lostsector/dialogue/rules/). Its package registration already exists in [settings.json](../data/config/settings.json). Do not add a parallel command merely to set a String or call an existing reward helper.

A standalone framework's minimal Java shape is:

```java
package example.rulecmd;

import java.util.List;
import java.util.Map;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

public class ExampleCMD extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog,
                           List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || params.isEmpty()) return false;
        MemoryAPI local = memoryMap.get(MemKeys.LOCAL);
        if (local == null) return false;
        String action = params.get(0).getString(memoryMap);
        if (!"hasTarget".equals(action)) return false;
        return dialog.getInteractionTarget() != null;
    }
}
```

This is a teaching example, not a proposed new class. Add the package `example.rulecmd` to the mod's `ruleCommandPackages` settings array; merged arrays should contain only the new package, not a copied vanilla list. Then `ExampleCMD hasTarget` invokes it. The class must be public, loadable and instantiable without constructor arguments. Avoid collisions with vanilla and other mods' simple class names.

The resolver tries `<registered package>.<supplied command name>` and caches successful lookups. Use exact Java capitalization, including lowercase `unset`, `unsetAll` and `expire`. Do not rely on the simulator's case-insensitive dispatch or “unknown command = no-op” behavior; vanilla throws for an unresolved command.

Only genuine option-adding commands override `doesCommandAddOptions()` and `getOptionOrder(...)`. FireBest/FireAll collect those separately and `runScript` skips them in its ordinary command loop. Keep option construction separate from payment or quest-state mutations; the normal Options column and existing helpers usually suffice.

Use the typed Token accessors for parameters, validate required context, and delegate gameplay changes to the existing subsystem. Command instances are not saved quest state. Store long-lived state on its proper owner; do not cache a dialog, UI component or a command instance in campaign memory.

From Java, invoke a known command's `execute` with the actual memory map and `Misc.tokenize(...)`, or use the static helpers:

```java
FireBest.fire(null, dialog, memoryMap, "nskr_exampleDisplay");
FireAll.fire(null, dialog, memoryMap, "nskr_exampleOptions");
```

Import these from `com.fs.starfarer.api.impl.campaign.rulecmd`. `RulesAPI` exposes matching and replacement methods; the static `FireBest`/`FireAll` helpers apply the selected rules. Do not invent `Global.getSector().getRules().fireBest(...)` or `.fireAll(...)` methods.

## Corrections to the preserved simulator references

| Topic | Use for this game build | Source |
|---|---|---|
| CSV rows and multiline cells | Separate rows do not continue a rule. The loader skips empty ids and ids starting with `#`, and rejects a duplicate id only under the same trigger. Multiple commands belong in one quoted Script cell with embedded newlines. Lines starting with `#` in Conditions, Script and Options are skipped; a line of only spaces in Conditions or Script throws `No tokens found` and stops loading. | `Rules` CSV loading in `sources-obf/campaign.rules.java`, bundle lines 558–569 (file open and `#` id skip) and 681–686 (duplicate check per trigger); `LoadingUtils.super(List, String, boolean, boolean)` and `com.fs.starfarer.loading.G.o00000(String)` bytecode in the 0.98a-RC8 game jar |
| Assignment and comparison in CSV | The loader rejects plain `=` in Conditions and `==` in Script. Condition-result rules apply only after the CSV has loaded; they do not make assignment conditions valid. | `sources-obf/campaign.rules.java`: `Rules` CSV loading at bundle lines 580–585 and 641–652; expression `isTrueFor` at bundle lines 897–903 |
| Operators | Only `=`, `!`, `!=`, `==`, `>=`, `<`, `<=`, `>`, `++` and `--` exist. The engine workflow's `is`, `in`, `is_not`, `is_not_in`, `has`, `does_not_have`, `has_not`, `+=`, `-=`, `*=` and `/=` are not in this build: the word forms, `+=` and `-=` reduce the line to a bare variable check, `*=` and `/=` throw when evaluated. | Operator mapping in `sources-obf/campaign.rules.java`, bundle lines 870–895; `Misc.tokenize` operator characters `=<>!+-` in `sources-api/util.java` |
| Initial trigger | No `DialogStart` trigger is fired. The standard rules dialog fires `OpenInteractionDialog`, or the trigger passed to its constructor. | `RuleBasedInteractionDialogPluginImpl` constructors and `init`; no `DialogStart` in the sources or vanilla rules |
| No expiry / zero expiry | No duration is persistent; `0` expires on subsequent unpaused memory advancement. | `Memory.set/expire/advance` |
| Local/player scopes | Local can be entity/person memory; player is character-data memory. Dialogue closure does not clear all persistent local keys. | `RuleBasedInteractionDialogPluginImpl.updateMemory/updatePersonMemory` |
| `$option`, `$last`, `$optN` | The driver sets `$option` with zero expiry; this build's rule runScript does not clear it. No general `$last`/`$optN` producer was found in the standard driver, token resolver or vanilla CSV. | `RuleBasedInteractionDialogPluginImpl.optionSelected`, rule `runScript`, `Misc.Token` |
| Command naming | Exact registered class names; missing commands throw. The simulator's aliases and no-ops are not game APIs. | Expression `getCommandClass`; vanilla settings package list |
| `Highlight` | Alias of `SetTextHighlights`, operating on the last paragraph; not a separate emitted paragraph or a `<text> <color>` command. | `Highlight.java`, `SetTextHighlights.java` |
| `Call` | Dispatch to a `CallableEvent` in memory, not reflection or a rule-ID jump. | `Call.java`, `CallEvent.java` |
| Condition score default | Each condition line scores 1 unless a `score:N` token replaces it; a rule's score is the sum over its conditions. The engine workflow's "default is 0" is wrong. More passing condition lines raise a row's priority. | Condition class score field and `Rules.getBestMatching` in `sources-obf/campaign.rules.java`, bundle lines 733, 790–794, 505–527 |
| Text/option preparation | Options and Text can precede ordinary Script execution. Prepare replacements beforehand. | `FireBest.applyRule`, `FireAll.execute/applyRule` |
| `FireAll <trigger> true` | This implementation does not read a keep-options argument. `FireBest` does. Empty collected option sets do not automatically clear an existing menu. | `FireAll.execute`, `FireBest.applyRule` |
| Command existence | The corpus uses `AddOption`; do not assume `MakeOption`, `ShowDialogButton`, `hasPerson`, `hasMarket`, `setMemory` or `goto` exist without a supplied mod implementation. | Class inventory and package resolution; `$isPerson`/`$hasMarket` are facts |
| `SetLater` units | Its comment says days, but its implementation accumulates raw `EveryFrameScript` amount without converting to days. Do not use it as a campaign-day expiry recipe. | `SetLater.java` |

This table covers the listed discrepancies. Other claims in the external simulator guide still need source verification before relying on them.

## Check before shipping

1. Look up the existing key/command and its vanilla call site. Confirm game version, owner, active person, arguments and prerequisites.
2. For each new key, record whether it is persistent state, a timed flag or a disposable display value. Choose the expiry deliberately.
3. Check acceptance, cancel, reopen, another person on the same entity, hand-in and exit. A zero-expiry key may still exist between these steps while paused.
4. Verify temporary keys disappear after unpausing; persistent keys survive close/reopen and save/load; timed keys expire and are not accidentally refreshed. Verify a persistent overwrite cancels a previous timer.
5. Check Text and options after token preparation, including repeated highlights and absent optional scopes. Use `DumpMemory` in live testing; it cannot show generator-only tokens as stored memory entries.
6. Ensure the reward was not both granted in Java and granted again by a rules command.
7. Follow [RULES.md](RULES.md#editing-and-validation) and the project's build gate for runtime changes. Documentation-only reference updates do not require a Java build.

## Sources and upkeep

The dictionaries identify original source paths, not machine-specific installation paths. In `starsector-knowledge`, find an original file with `rg -n '//// FILE: .*/ClassName.java' sources-api sources-obf`; source bundles are whitespace-minified, so bundle line numbers are not original source line numbers. Vanilla rule IDs refer to `data/campaign/rules.csv`, not this mod's additive CSV.

The catalogue was built by CSV parsing, excluding blank/commented IDs, scanning Conditions and Script for invocations, and scanning Conditions/Script/Text/Options for literal `$` references. Base classes and unused command classes were additionally found by inheritance in the official API sources. Dynamic keys and subcommand vocabularies require reading their producer/dispatcher; a literal inventory cannot enumerate arbitrary tag IDs or procedurally constructed mission keys. Search the exact key, its `MemFlags` constant and its owning mission when a lookup needs more context.

When updating for another game version, repeat that crawl, resolve every command against the registered package list, and recheck the source-backed recipes and corrections. Keep this practical guide, the lookup dictionaries and `RULES.md` aligned. Preserve `docs/rules/*.md` unchanged unless deliberately updating the upstream references.
