# Rules language and project contracts

Read this document for rules syntax, execution, Lost.Sector routing contracts and CSV validation. [RULES_AUTHORING.md](RULES_AUTHORING.md) is the companion implementation guide for using and debugging commands, memory and text replacements, including their Java bindings. Both are technical guides: this one describes the language and project contracts; the companion owns the full command/key integration procedures. The short examples here do not replace that procedure.

[DIALOGUE.md](DIALOGUE.md) governs all player-facing wording, text presentation and dialogue flow, including rules-authored text and options. Use [LORE.md](LORE.md) for fiction and voice. [UI.md](UI.md) covers Java-bound custom UI only; consult it when integrating a Java panel, not as a separate standard for rules dialogue. Repository workflow and required reading are in [CLAUDE.md](../CLAUDE.md#which-guide-to-read).

The engine guidance was adapted from another modder's reference. The detailed [engine workflow](rules/engine_workflow.md) and [command table](rules/command_table.md) are preserved upstream copies. Their simulator-specific instructions describe an external tool, not a requirement to build that tool in Lost.Sector. Project constraints are listed below.

Before using, changing or debugging commands or memory/text keys, read the relevant sections of the [Rules implementation guide](RULES_AUTHORING.md). It links the [vanilla command dictionary](rules-reference/COMMANDS.md), [memory/text dictionary](rules-reference/MEMORY.md), and [complete literal-key usage index](rules-reference/KEY_USAGE.md). Look for a vanilla or existing project mechanism before writing a new plugin. Its [source corrections](RULES_AUTHORING.md#corrections-to-the-preserved-simulator-references) govern the listed differences from the preserved simulator guides for the named build; they do not certify unlisted claims. Follow the [technical conflict procedure](../CLAUDE.md#technical-references-and-conflicts) for other discrepancies.

## Rules System (`data/campaign/rules.csv`)

Rule rows drive dialogue, bar events, market interactions and other campaign text. Vanilla file: `starsector-core/data/campaign/rules.csv` (~41k lines). Mod file: `data/campaign/rules.csv` (additive).

For the preserved engine discussion, see [rules/engine_workflow.md](rules/engine_workflow.md), together with the [source corrections](RULES_AUTHORING.md#corrections-to-the-preserved-simulator-references). It is supporting reference material, not a replacement for the project contracts or the implementation guide.

### CSV columns
`id,trigger,conditions,script,text,options,notes`

- **id** — unique rule id. Prefix with `nskr_` to avoid collisions with other mods. The loader skips rows with an empty id and rejects duplicate ids within the same CSV file. Separate rows do not continue the previous rule.
- **trigger** — a bucket that groups rules, not "fires when." The engine fetches all rules for a trigger and filters via conditions. Common triggers:
  - Dialog flow: `DialogStart`, `OpenInteractionDialog`, `PopulateOptions`, `DialogOptionSelected`
  - Fleet encounters: `BeginFleetEncounter`, `FleetEncounterResolved`, `OpenCommLink`
  - Markets/bars: `BarPrintDesc`, `BarEncounterOption`, `TradePanelFlavorText`, `RelationshipLevelDesc`
  - Salvage/raids: `BeginSalvage`, custom triggers like `BeatDefendersContinue`
  - Custom mod-defined triggers via `FireAll nskr_shipSwapMenu` from code/script.
- **conditions** — newline-separated predicate expressions. ALL must pass for the rule to match. Empty = always matches. See Operators section below. Append `score:N` to a condition line for priority in `getBestMatching`.
- **script** — newline-separated command invocations in one cell, executed sequentially when the rule fires. Quote the whole CSV cell when it contains multiple lines. Token before first space is a `CommandPlugin` name (resolved across all mods + `api/impl/campaign/rulecmd/*`). Quoted arguments preserve spaces; `""` escapes quotes inside CSV. Bare assignment lines (`$var = value`) are valid and common (~33% of real script lines).
- **text** — shortcut for dialog display text shown when the rule fires. Supports `$var` substitution at display time. For multiple paragraphs or highlights, prefer `script` with `AddText`/`Highlight`.
- **options** — newline-separated option definitions: `order:id:text` or `id:text`. Lower order = displayed higher. Selecting an option fires `DialogOptionSelected` with `$option == optionId`. FireBest/FireAll collect and add options before ordinary Script execution; prepare option text beforehand. See [display ordering](RULES_AUTHORING.md#create-a-custom-text-token).
- **notes** — free-form comments; ignored by engine.

### Memory scopes
Conditions and commands reference memory through dotted scopes:

| Scope | Meaning | Persistence |
|---|---|---|
| `$global.*` | Sector-wide state (`Global.getSector().getMemoryWithoutUpdate()`) | Campaign-long |
| `$player.*` | Player character-data memory in the standard rules dialog | Owner persists; individual keys may expire |
| `$market.*` | Current market being interacted with | Per-market |
| `$faction.*` | Faction of the interaction target | Derived at runtime |
| `$entity.*` | Underlying target when an active person occupies local; optional scope | Owner persists; individual keys may expire |
| `$local.*` | Current interaction's selected entity/person memory | Not automatically cleared on dialogue close |

Unqualified `$var` in an expression resolves to **local** memory by default. Text substitution also has generated-token and memory-replacement passes; see [ownership and replacement](RULES_AUTHORING.md#text-replacements-are-not-all-memory-keys). A scope's owner and a key's expiry are separate decisions.

Special interaction key: `$option` is written by the dialog driver into local with zero expiry when an option is selected. In this build, rule `runScript` does not clear it before executing commands. Do not treat it as persistent quest state. The simulator's `$last` and `$optN` claims are not established vanilla APIs; do not use them without an actual producer. See the [source corrections](RULES_AUTHORING.md#corrections-to-the-preserved-simulator-references).

Lifetime: no duration is persistent, including after dialogue closure on a persistent owner. `0` expires on the next advancing memory update, normally when the dialogue closes and the campaign unpauses. Positive durations are campaign-day timers. `set(key, value)` overwrites the value and cancels its previous expiry. Use the [rules/Java lifetime table](RULES_AUTHORING.md#memory-lifetime); never treat zero as permanent or assume local means disposable.
Read from Java: `mem.getBoolean("$myFlag")`, `.getString(...)`, `.getInt(...)`, `.contains(...)`.

### Operators (verified against decompiled source)

| Operator | Symbol | Description |
|---|---|---|
| Equality | `==` | Value or string comparison |
| Not equal | `!=` | Inequality |
| Less/greater | `<` `>` | Numeric comparison |
| Less/greater or equal | `<=` `>=` | Numeric comparison |
| Identity | `is` | Exact match (same as == for primitives) |
| Contains | `in` | List contains value |
| Not identity | `is_not` | Negated identity |
| Not contains | `is_not_in` | List does not contain value |
| Key exists | `has` | Memory key has a value (right operand ignored) |
| Key absent | `does_not_have` / `has_not` | Memory key does not exist (right operand ignored) |
| Assignment | `=` `+=` `-=` `*=` `/=` | Writes to memory, returns the written value |

Command plugins can also serve as conditions: their Boolean return determines pass/fail. Examples include `PlayerHasCargo supplies 10` and `CheckSetting <booleanSettingId>`. Use the command dictionary for exact class names; `$hasMarket` and `$isPerson` are facts, not plugins named hasMarket or hasPerson. Conditions must not perform acceptance/payment mutations.

### Condition results

A condition passes ONLY when its result is:
- Boolean `true`, or
- A String that equals `"true"` after `.toLowerCase().trim()` — so `"True"`, `"TRUE "`, `" true "` all pass.

`null`, numbers, and every other type **FAIL**.

The CSV loader rejects plain assignment (`=`) in Conditions before a rule can run.
This includes `$local.x = 5` and `$x = true`. Put assignments in Script and comparisons
in Conditions. The loader also rejects equality comparisons (`==`) in Script.

For example, test a value in Conditions:

```text
$local.x == 5
```

Set a value in Script:

```text
$local.x = 5
```

See the [source corrections](RULES_AUTHORING.md#corrections-to-the-preserved-simulator-references)
for the loader and condition evaluator.

### Score mechanics
- Lives on CONDITIONS: `score:N` token parsed per condition line. Default is **0** (not 1).
- A rule's effective score in `getBestMatching` = sum of all its conditions' scores + optional rule-level bonus (effectively 0 for CSV rules).
- Higher wins; exact ties are chosen randomly via `WeightedRandomPicker`.
- Score does NOT affect `getAllMatching` ordering.

### Self-skip behavior
The rule that just fired is excluded from the next matching round (`currentRuleId`). This prevents infinite loops when a rule's condition is always true. During initial `DialogStart`, no rule is skipped (`currentRuleId = null`).

### Dialog lifecycle (for writing chains)

1. **Initialization**: The dialog builds its memory map from the target/person and starts its configured trigger. The standard RuleBasedInteractionDialogPluginImpl defaults to `OpenInteractionDialog`; wrappers can use other entry paths. Local is not inherently fresh memory.
2. **Option selection**: The standard driver writes `$option` into local with zero expiry and uses FireBest for `DialogOptionSelected`. The selected rule's text/options and script follow the display ordering above.
3. **Failsafe**: The standard driver adds an error and an explicit exit option when a selection has no matching rule (except its confirmation path). Do not depend on a missing rule to close a conversation safely.
4. **Termination**: Use the explicit exit appropriate to the wrapper. EndConversation, DismissDialog, bar return and fleet teardown are different operations; see [fleet and bar exits](#fleet-and-bar-exits).

### Writing a custom rule command
Extend `BaseCommandPlugin`:

```java
public class MyRuleCMD extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog,
                           List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String arg = params.get(0).getString(memoryMap);
        // Use dialog.getTextPanel(), dialog.getInteractionTarget(), memoryMap, etc.
        return true;
    }
}
```

Register the class's package in `ruleCommandPackages`, then invoke its exact simple class name in Script: `MyRuleCMD argA "quoted arg"`. The resolver tries registered packages and caches successful class lookups. Lost.Sector's commands live in `src/lostsector/campaign/rulecmd/`, normally one multi-verb class per feature, registered through the `lostsector.campaign.rulecmd` package. Read [custom command integration](RULES_AUTHORING.md#create-a-command-only-when-needed) before adding another class; ordinary mod actions should extend the existing command that owns the feature.

### Integration patterns with Java code

**Command-class registration (this mod's pattern):**
Custom commands registered by simple name in the script column: `nskr_debt init`, `nskr_shipSwap sell`. The command class handles all logic; rules invoke it declaratively. See [Project routing](#project-routing).

**Nexerelin style (memory references):**
`Call $reference <action>` requires an object implementing `CallEvent.CallableEvent`; it delivers action tokens to `callEvent`, not to an arbitrary reflected Java method. Lost.Sector's `Contracts` mission uses the BaseHubMission dispatch through `$nskr_contracts_ref`, set by `setPersonMissionRef`. Persistent referenced objects must remain save-compatible. See [Call integration](RULES_AUTHORING.md#reuse-a-mission-object-through-call).

**Rule-driven mission chains:**
For multi-step missions without timers/map markers:
- Store stage in memory: `$missionStage == START`, `== IN_PROGRESS`, etc.
- Each step's rules check the stage, do work, advance the stage flag.
- Spawn subsequent intels from the previous intel's `endMission()` rather than building monolithic multi-stage objects.

### Useful built-in commands (subset)
`AddText`, `AddTextSmall`, `Highlight`, `SetTextHighlights`, `SetTextHighlightColors`, `FireAll` / `FireBest`, `Call`, `BeginConversation`, `EndConversation`, `DismissDialog`, `AdjustRep`, `AddCredits`, `AddCommodity`, `SetShortcut`, `ShowDefaultVisual` / `ShowImageVisual`, `DumpMemory` (debug), `MakeOptionOpenCore`, `RemoveOption`.

Use the [vanilla command dictionary](rules-reference/COMMANDS.md) for recipes, exact classes and real call sites. The preserved `rules/command_table.md` is a simulator vocabulary with recorded no-ops, not a comprehensive game command manual. In particular, `Highlight` aliases `SetTextHighlights`; it does not emit a separate paragraph.

### Text features
- `$var` substitution happens at **display time**, not rule definition time.
- Multiple text alternatives separated by `OR` on separate lines are randomly chosen (Nexerelin pattern; engine supports it). Example:
  ```
  "Option A text."
  OR
  "Option B text."
  ```

### Firing rules from Java
```java
FireAll.fire(null, dialog, memoryMap, "nskr_shipSwapMenu");
// or single best match:
FireBest.fire(null, dialog, memoryMap, "nskr_shipSold");
```

Import FireAll/FireBest from `com.fs.starfarer.api.impl.campaign.rulecmd`; pass the active interaction's memory map. RulesAPI supplies matching/replacement, not these fireAll/fireBest methods.

### Library shortcuts
- `MagicLib.MagicBountyIntel` / `MagicBountyCoordinator` — full bounty flow (intel, rule hooks, bar event) from JSON. Use before rolling custom bounty dialogs.
- `LunaLib` Kotlin extensions (`lunalib.lunaExtensions.DialogExtensions`, `MemoryExtensions`) — concise `dialog.addText(...)`, typed memory get/set, `LunaMemory` property delegates.

## Project routing

Code owners are mapped in [ARCHITECTURE.md](ARCHITECTURE.md). Shared text-presentation requirements belong in [DIALOGUE.md](DIALOGUE.md#shared-text-presentation), regardless of the content or its source.

| Entry / state | Contract |
|---|---|
| `lostsector.campaign.rulecmd` | Registered command package. Commands are multi-verb classes, normally one per feature; the first argument selects the verb. A `hasOption` verb is a Condition that the current target qualifies for that feature's option. Most dialogue commands extend `PaginatedOptions`. |
| `nskr_kestevenQuest <verb>` | Kesteven questline dialogue. The stage is an int in sector persistent data under `nskr_kestevenQuest`, read and written through `QuestUtil.getStage/setStage`; `QuestStageManager` also advances it. Stage values are listed in the "STAGE CHEAT SHEET" comment in `nskr_kestevenQuest.java`. |
| `nskr_isKStage N`, `nskr_isAtleastKStage N`, `nskr_isAtmostKStage N` | Condition predicates on the quest stage. |
| `nskr_isBaseOfficial <post>` | Condition on the active person's post: `command`, `military`, `admin`, `ttadmin`, `trade`/`op`, `research`, `intelligence`, `trader` or `any`. Gates the official menus below. |
| `nskr_optionStartsWith "<prefix>"` | Condition: `$option` begins with the prefix. Paginated pickers add an index suffix to their option IDs. |
| `nskr_debtMenu`, `nskr_shipSwapMenu`, `nskr_modRemovalMenu` | Private menus of `nskr_debt`, `nskr_shipSwap` and `nskr_modRemoval`. Entry and return rows fire the menu trigger; exit rows fire `PopulateOptions`. |
| `nskr_shipSold` | Fired by `nskr_shipSwap` from Java after a sale. |
| `$nskr_debt_points` / `…Str`, `$nskr_debt_MaxpointsStr`, `$nskr_debtInterest` / `…Str`, `$nskr_shipSwap_points` / `…Str` | Local display values written with expiry `0` by the owning command before its menu text. The saved values are in sector persistent data. |
| `Call $nskr_contracts_ref showBlurb` / `showContract` | `Contracts` hub mission offer on the `nskr_contracts_blurb` and `nskr_contracts_option` triggers; the start row sets `$missionId = nskr_contracts`. |
| `$KestevenQuestJob4Target`, `$KestevenQuestJob4Friendly`, `$KestevenQuestTTCollector`, `$ElizaFleet`, `$RevengeanceEliza`, `$RevengeanceJack`, `$BetrayalFleet`, `$InterceptPlayerElizaFleet`, `$nskr_interceptManagerAROfleet`, `$nskr_interceptManagerMessengerFleet`, `$debtCollector`, `$CacheGuardianFleet`, `$EnigmaDormantFleet`, `$AbyssLoot`, `$EternityLoot`, `$mothershipLoot`, `$RorqLoot` | Fleet memory flags set by the spawning Java owner. They select that fleet's `OpenCommLink` and `BeginFleetEncounter` rows. |
| `job4TargetOptions`, `elizaRevengeanceDialogOptions`, `pkDialogOptions`, `betrayalDialogOptions` | Private option triggers, fired with `FireBest` from the matching greeting row. |
| `nskr_barEventFixer`, `nskr_heartFixer` | Kesteven questline bar events on `AddBarEvents`; the `nskr_heart` market menu on `MarketPostDock`/`MarketPostOpen`. |

Entities routed by `CorePlugin.pickInteractionDialogPlugin` open Java dialogs without rules; see [ARCHITECTURE.md](ARCHITECTURE.md#registration-and-lifecycle).

- Namespace IDs and options. Bar option IDs must begin with their mission ID; mission IDs must not prefix one another. `BarCMD` aborts a wrapper whose option prefix differs.
- Many existing rule IDs, triggers and fleet flags are unprefixed. Give new ones the `nskr_` prefix; rename an existing flag only together with every Java writer and every row that reads it.
- Conditions are evaluated before script actions. Prepare generated display tokens on an earlier row: a row cannot display a token its own script has not yet created.
- Use Boolean flags for eligibility. Strings and numbers do not pass a bare-memory condition. Every memory key written through MemoryAPI begins with `$`.
- A bare `score:` line is invalid. Put the score on a real condition. Scores sum; more conditions do not confer priority. Check overlap with unrelated rule families on the same fleet, not just alternatives within one family.
- Explicitly fire the intended menu trigger on entry and return; do not rely on the trigger's name to schedule it. Rows with no options may retain an old panel. Check actual rules and driver behavior for the path being edited.
- `$hailing` and `$highlightComms` are consumed while vanilla builds fleet interaction. Do not treat them as lasting quest state.
- Colour an option after it has been added. Use a later, condition-matched colour row rather than relying on an earlier script.

Check displayed highlight occurrences using [DIALOGUE.md](DIALOGUE.md#shared-text-presentation).

## Fleet and bar exits

`EndConversation` returns a fleet comm conversation to its FleetInteractionDialogPluginImpl. It calls reinit, which can fire BeginFleetEncounter again. Use it only when rebuilding fleet/combat options is the intended result.

`DismissDialog` alone does not clean up a fleet encounter's BattleAPI. Lost.Sector has no shared teardown verb yet. When a route must leave a fleet encounter, check that `dialog.getPlugin()` is a `FleetInteractionDialogPluginImpl`, call its `cleanUpBattle()`, then `dialog.dismiss()`. Put this on the owning command as a verb that is also safe for non-fleet interactions.

Bar-event wrappers close with `returnFromEvent`, not `close`. Check confirm, cancel and Escape paths after a custom panel.

`AddBarEvent <id> "<option>" "<blurb>" [<colour>]` accepts an optional fourth argument via Token.getColor. `highlight` resolves to the buttonShortcut colour; faction IDs resolve to faction colour.

## Editing and validation

1. Read the relevant existing rows and required Starsector rules references. Check [the authoring guide and dictionaries](RULES_AUTHORING.md) for the commands and keys being used, changed or debugged, not only new ones. Use vanilla source for uncertain engine behavior, and read-only `lib/` archives for third-party APIs.
2. Prove a byte-identical CSV round-trip before changing parsed rows. Detect the current line endings; do not assume LF or CRLF from an old note.
3. Preserve row IDs, seven columns, quoting, embedded newlines, tokens, commands and ordering except where the technical task explicitly changes them.
4. Check every affected entry, question loop, accept/decline path, hand-in, cancellation and exit. Include overlapping flags, completed states and save/load. Follow the [dialogue route checklist](DIALOGUE.md#technical-handoff) and [shared text checks](DIALOGUE.md#shared-text-presentation). When a Java custom panel is affected, also use its [UI checks](UI.md#review-the-affected-screen).
5. Parse the edited file again, require seven fields per row, and inspect the diff. A small change must not rewrite unrelated rows.
6. Report static checks separately from in-game QA. The external Rules Visualizer is not bundled with this repository; use it only if available. Its absence is not a new tooling project or a reason to claim a test was run. DumpMemory can help during live QA.

A round-trip probe for a file that uses LF record endings:

```python
src = open(path, newline='', encoding='utf-8').read()
rows = list(csv.reader(io.StringIO(src)))
out = io.StringIO()
csv.writer(out, lineterminator='\n', quoting=csv.QUOTE_MINIMAL).writerows(rows)
assert out.getvalue() == src
```

If it differs, inspect the source format before editing. A carriage return embedded in a script command can make its name unrecognisable. Commas in notes must remain inside a correctly quoted field.

## Maintenance

Update verified language and project contracts here when their implementation changes. Preserve exact syntax and source evidence. Full command/key integration procedures belong in `RULES_AUTHORING.md`; link there rather than adding a competing procedure here. Record corrections to the preserved simulator references in its [source correction table](RULES_AUTHORING.md#corrections-to-the-preserved-simulator-references), then align affected summaries and links here. Keep upstream reference files under `docs/rules/` unchanged unless intentionally updating the vendored version; simulator behavior described there is not a substitute for a live game check. Follow [CLAUDE.md](../CLAUDE.md#documentation-upkeep) for ownership and commit requirements.
