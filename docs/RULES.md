# Rules language and project contracts

Read this document for rules syntax, execution, Lost.Sector routing contracts and CSV validation. [RULES_WRITING.md](RULES_WRITING.md) explains how to design and structure rules content: the writing process, choosing between plain chains, `FireAll` and `FireBest`, conditions, state, text, options, exits and layout. [RULES_AUTHORING.md](RULES_AUTHORING.md) is the companion implementation guide for using and debugging commands, memory and text replacements, including their Java bindings. All three are technical guides: this one describes the language and project contracts; the writing guide owns structure and process; the authoring guide owns the full command/key integration procedures. The short examples here do not replace those.

[DIALOGUE.md](DIALOGUE.md) governs all player-facing wording, text presentation and dialogue flow, including rules-authored text and options. Use [LORE.md](LORE.md) for fiction and voice. [UI.md](UI.md) covers Java-bound custom UI only; consult it when integrating a Java panel, not as a separate standard for rules dialogue. Repository workflow and required reading are in [CLAUDE.md](../CLAUDE.md#which-guide-to-read).

The engine guidance was adapted from another modder's reference. The detailed [engine workflow](rules/engine_workflow.md) and [command table](rules/command_table.md) are preserved upstream copies. Their simulator-specific instructions describe an external tool, not a requirement to build that tool in Lost.Sector. Project constraints are listed below.

Before using, changing or debugging commands or memory/text keys, read the relevant sections of the [Rules implementation guide](RULES_AUTHORING.md). It links the [vanilla command dictionary](rules-reference/COMMANDS.md), [memory/text dictionary](rules-reference/MEMORY.md), and [complete literal-key usage index](rules-reference/KEY_USAGE.md). Look for a vanilla or existing project mechanism before writing a new plugin. Its [source corrections](RULES_AUTHORING.md#corrections-to-the-preserved-simulator-references) govern the listed differences from the preserved simulator guides for the named build; they do not certify unlisted claims. Follow the [technical conflict procedure](../CLAUDE.md#technical-references-and-conflicts) for other discrepancies.

## Rules System (`data/campaign/rules.csv`)

Rule rows drive dialogue, bar events, market interactions and other campaign text. Vanilla file: `starsector-core/data/campaign/rules.csv` (~41k lines). Mod file: `data/campaign/rules.csv` (additive).

For the preserved engine discussion, see [rules/engine_workflow.md](rules/engine_workflow.md), together with the [source corrections](RULES_AUTHORING.md#corrections-to-the-preserved-simulator-references). It is supporting reference material, not a replacement for the project contracts or the implementation guide.

### CSV columns
`id,trigger,conditions,script,text,options,notes`

- **id** — unique rule id. Prefix with `nskr_` to avoid collisions with other mods. The loader skips rows with an empty id and rows whose id starts with `#` (comment or disabled rows). It rejects a duplicate id only under the same trigger; the same id under two triggers loads without error, so keep ids unique yourself. Separate rows do not continue the previous rule.
- **trigger** — a bucket that groups rules, not "fires when." The engine fetches all rules for a trigger and filters via conditions. Common triggers:
  - Dialog flow: `OpenInteractionDialog` (default initial trigger of the standard rules dialog), `PopulateOptions`, `DialogOptionSelected`. The simulator guide's `DialogStart` is not fired anywhere in this build.
  - Fleet encounters: `BeginFleetEncounter`, `BeginFleetEncounter2`, `OpenCommLink`
  - Markets/bars: `MarketPostOpen`, `MarketPostDock`, `AddBarEvents`, `BarPrintDesc`, `TradePanelFlavorText`, `RelationshipLevelDesc`
  - Salvage/raids: `BeginSalvage`, custom triggers like `BeatDefendersContinue`
  - Custom mod-defined triggers via `FireAll nskr_shipSwapMenu` from code/script.
- **conditions** — newline-separated predicate expressions. ALL must pass for the rule to match; they are checked top to bottom and matching stops at the first failure. Empty = always matches. See Operators section below. Append `score:N` to a condition line for priority in `getBestMatching`. Lines starting with `#` are comments.
- **script** — newline-separated command invocations in one cell, executed sequentially when the rule fires. Quote the whole CSV cell when it contains multiple lines. Each line is split by `Misc.tokenize`: a token ends at a space or tab outside quotes and at an unescaped `"`, so `AddText"x"` splits the same way as `AddText "x"`; `\` escapes the next character. The first token is a `CommandPlugin` name (resolved across all mods + `api/impl/campaign/rulecmd/*`) unless the line starts with a variable. Names are resolved when `rules.csv` loads: an unknown command, or a Script line using `==`, stops the load with an error (`Rules` loader and expression constructor in `sources-obf/campaign.rules.java`). Quoted arguments preserve spaces; `""` escapes quotes inside CSV. Bare assignment lines (`$var = value`) are valid and common. Every line runs: a command that returns false does not stop the lines after it. Lines starting with `#` are comments. In Conditions and Script, a line that holds only spaces aborts loading the file (`No tokens found`); a truly empty line is skipped.
- **text** — display text shown when the rule fires, added as **one paragraph**: line breaks inside the cell stay inside that paragraph. Supports `$var` substitution at display time. `SetTextHighlights` in the same row's Script applies to this paragraph unless the Script adds another paragraph first. See [Text](RULES_WRITING.md#text).
- **options** — newline-separated option definitions: `order:id:text` or `id:text`. An `id:text` option has order 0. Lower order = displayed higher; equal orders keep collection order. Labels get token replacement; quote characters display as written. The loader splits on every colon: a colon in an `id:text` label fails loading, and in `order:id:text` everything after the third colon is dropped, so write labels without colons. A label that needs a colon is set once the option exists with `SetOptionText <id> "<label>"`, which reads its argument as written (`Token.string`, 0.98a-RC8 `rulecmd/SetOptionText.java`), as the Delve meeting's `nskr_kq_delveOptEnigma` does. An option id must not start with `$`. Selecting an option fires `DialogOptionSelected` with `$option == optionId`. FireBest/FireAll collect and add options before ordinary Script execution; prepare option text beforehand. See [display ordering](RULES_AUTHORING.md#create-a-custom-text-token).
- **notes** — free-form comments; ignored by engine.

### Memory scopes
Conditions and commands reference memory through dotted scopes:

| Scope | Meaning | Persistence |
|---|---|---|
| `$global.*` | Sector-wide state (`Global.getSector().getMemory()` in the standard rules dialog, which also refreshes global facts) | Campaign-long |
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
| Equality | `==` | String, Boolean or number comparison |
| Not equal | `!=` | Inequality |
| Less/greater | `<` `>` | Numeric comparison; both sides are parsed as numbers |
| Less/greater or equal | `<=` `>=` | Numeric comparison |
| Not | `!` | `!$flag` passes when the key is unset or not `"true"`; `!Command args` negates a command's result |
| Assignment | `=` | Writes to memory; an optional trailing number is the expiry in days |
| Increment / decrement | `++` `--` | `$count++` adds 1 and stores a Float; an optional trailing number is the expiry |

No other operators exist in this build. The operator characters are `=<>!+-`, and only the ten strings `=`, `!`, `!=`, `==`, `>=`, `<`, `<=`, `>`, `++`, `--` are recognized. The preserved simulator guide also lists `is`, `in`, `is_not`, `is_not_in`, `has`, `does_not_have`, `has_not`, `+=`, `-=`, `*=` and `/=`: the word forms, `+=` and `-=` silently reduce the line to a bare `$var` check, and `*=` and `/=` raise an error when evaluated. There is no OR; use separate rows or a command.

Unset keys in comparisons:

| Expression | Unset `$x` |
|---|---|
| `$x` | fails |
| `!$x` | passes |
| `$x == value` | fails |
| `$x != value` | passes |
| `$x > 3`, `<`, `<=`, `>=` | `$x` counts as 0 |
| `$x == $y`, both unset | passes |

`$x = 5` stores the String `"5"`; `$x = $y` copies whatever object `$y` holds; `$x++` stores a Float. Quote a String with spaces: `$x = "two words" 0`.

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
- Lives on CONDITIONS. Every condition line scores **1** by default. A `score:N` token at the end of a line replaces that line's score with N; it does not add to it.
- A rule's effective score in `getBestMatching` = sum of all its conditions' scores + optional rule-level bonus (effectively 0 for CSV rules). A row with more passing condition lines therefore beats a less specific row; a row with no conditions scores 0.
- Higher wins; exact ties are chosen randomly via `WeightedRandomPicker`.
- Score does NOT affect `getAllMatching` ordering.
- Source: the condition class's score field is initialized to 1 and overwritten only by a `score:` token; `getBestMatching` sums `getScore()` over the passing conditions (`sources-obf/campaign.rules.java` in `starsector-knowledge`: field at bundle line 733, parsing at 790–794, summation at 505–527). The preserved simulator guide's "default 0" claim is wrong; see the [source corrections](RULES_AUTHORING.md#corrections-to-the-preserved-simulator-references).

### Self-skip behavior
The rule that just fired is excluded from the next matching round (`currentRuleId`). This prevents infinite loops when a rule's condition is always true. When the dialog fires its initial trigger (`OpenInteractionDialog` by default), or Java fires a trigger with a null rule id, no rule is skipped.

### FireAll and FireBest

- `FireBest <trigger> [keepOptions]` applies the single best match by the score rules above and returns false when nothing matches. `keepOptions` (a literal or a `$variable`) adds the winner's options to the current ones instead of replacing them.
- `FireAll <trigger>` applies every match: the options of all matches are collected, sorted by order and shown first; then each match's Text and Script run in load order. Scores are ignored.
- Before matching, `FireAll` writes the requested trigger name to `$fireAllTrigger` (expiry 0, entity memory or local) and fires `FireBest FireAllIntercept`. If a `FireAllIntercept` row matches, it runs instead and the requested trigger's rows never run. Vanilla has one such row, `gaATGkantasDenHostileOverride1`. Any `FireAllIntercept` row must check `$fireAllTrigger`, or it replaces every `FireAll` in the game.
- Either command takes a `$variable` holding the trigger name.
- The option panel is cleared only when at least one option was collected, and for `FireBest` only without `keepOptions`. A trigger with no option rows leaves the old menu on screen.
- Option ids starting with `(dev)` are skipped unless the game runs in dev mode.
- Used in Conditions, either command runs in full (text, options, script) while the engine is still matching rows. Do not use them there.
- A `FireAll` or `FireBest` inside a rule's Script excludes that rule from its own matching round (self-skip), so a row can fire the trigger it belongs to.
- Trigger names are case sensitive. A trigger nothing is registered under matches nothing and reports no error.
- From Java, use the static `FireAll.fire` / `FireBest.fire` helpers; see [Firing rules from Java](#firing-rules-from-java).

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

Register the class's package in `ruleCommandPackages`, then invoke its exact simple class name in Script: `MyRuleCMD argA "quoted arg"`. The resolver tries registered packages and caches successful class lookups. Lost.Sector's commands live in `jars/src/lostsector/dialogue/rules/`, normally one multi-verb class per feature, registered through the `lostsector.dialogue.rules` package. Read [custom command integration](RULES_AUTHORING.md#create-a-command-only-when-needed) before adding another class; ordinary mod actions should extend the existing command that owns the feature.

### Integration patterns with Java code

**Command-class registration (this mod's pattern):**
Custom commands registered by simple name in the script column: `nskr_debt init`, `nskr_shipSwap sell`. The command class handles all logic; rules invoke it declaratively. See [Project routing](#project-routing).

**Nexerelin style (memory references):**
`Call $reference <action>` requires an object implementing `CallEvent.CallableEvent`; it delivers action tokens to `callEvent`, not to an arbitrary reflected Java method. Lost.Sector's `ContractsMission` stores itself in the contact's memory as `$nskr_contracts_ref` with `setPersonMissionRef`; its rows need no `Call`, because the hub prepares their tokens. Persistent referenced objects must remain save-compatible. See [Call integration](RULES_AUTHORING.md#reuse-a-mission-object-through-call).

**Quest state:**
Keep quest progress on its Java owner (a hub mission, intel or manager) and let rows ask it through a condition verb or `Call`. Memory holds conversation flags on the speaker, fleet roles and display values. See [State and memory keys](RULES_WRITING.md#state-and-memory-keys).

### Useful built-in commands (subset)
`AddText`, `AddTextSmall`, `Highlight`, `SetTextHighlights`, `SetTextHighlightColors`, `FireAll` / `FireBest`, `Call`, `BeginConversation`, `EndConversation`, `DismissDialog`, `AdjustRep`, `AddCredits`, `AddCommodity`, `SetShortcut`, `ShowDefaultVisual` / `ShowImageVisual`, `DumpMemory` (debug), `MakeOptionOpenCore`, `RemoveOption`.

Use the [vanilla command dictionary](rules-reference/COMMANDS.md) for recipes, exact classes and real call sites. The preserved `rules/command_table.md` is a simulator vocabulary with recorded no-ops, not a comprehensive game command manual. In particular, `Highlight` aliases `SetTextHighlights`; it does not emit a separate paragraph.

### Text features
- `$var` substitution happens at **display time**, not rule definition time.
- The whole Text cell, or the chosen alternative, is one paragraph.
- Multiple text alternatives separated by a line containing only `OR` are chosen at random with equal weight, again every time the row fires. The Text column splits at load time and replaces tokens in the chosen alternative; `AddText` and `AddTextSmall` replace tokens first, then split and trim the chosen alternative. Example:
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
| `lostsector.dialogue.rules` | Registered command package. Commands are multi-verb classes, normally one per feature; the first argument selects the verb. A `hasOption` verb is a Condition that the current target qualifies for that feature's option. Most dialogue commands extend `PaginatedOptions`. |
| `nskr_quest <q> <verb> [args]`, `$nskr_<q>_<name>` | The quest framework's command and tokens: verbs, errors and reserved words in [The quest command](../jars/src/lostsector/quest/README.md#the-quest-command); token format and the rows whose text receives quest tokens in [Tokens](../jars/src/lostsector/quest/README.md#tokens). |
| `nskr_questDev <verb>`, `(dev)nskr_questDev_open` | The quest dev menu, a `PaginatedOptions` command whose market option exists only in dev mode. Verbs, screens, triggers (`nskr_questDevQuest`, and `nskr_questDevQuestsOptions` and `nskr_questDevListOptions`, which the command fires with `FireBest ... true` after each page) and the local keys `$nskr_questDev_quest` and `$nskr_questDev_page` in [Dev tools](../jars/src/lostsector/quest/README.md#dev-tools). |
| `nskr_kq_*` rows, `nskr_kq<Speaker>Greeting`, `…Status`, `…Options`, `…Questions`, `nskr_kqAsk` and the other `nskr_kq` private triggers of the `# KESTEVEN QUESTLINE` block | Every conversation with Jack, Alice and Nicholas, on the quest framework: gates are `nskr_quest kq` conditions and `KestevenHubModule` checks, stage changes `nskr_quest kq advance`, game actions and targets `KestevenHubModule` actions, values its tokens; conversation flags are `$nskr_kq_introduced` and `$nskr_kq_asked<Topic>` in the speaker's memory. The stage is a `KestevenStage`, mapped to the old numbers in [KESTEVEN_STATE.md](quests/KESTEVEN_STATE.md#stages). Rows, option ids and decision tables in [KESTEVEN_DIALOGUE.md](quests/KESTEVEN_DIALOGUE.md#jack-alice-and-nicholas). |
| `nskr_kqIntelTitle`, `nskr_kqIntelBullets`, `nskr_kqIntelDesc` | Text of quest `kq`'s intel entries, read by `quest/QuestText` outside any dialog ([Intel](../jars/src/lostsector/quest/README.md#intel)). Job 1 (key `job1`) is the `# KESTEVEN QUESTLINE: JOB 1` block, job 3 (key `job3`) the `# KESTEVEN QUESTLINE: JOB 3` block, job 5 (key `job5`, "The Delve") the `# Intel` part of the `# KESTEVEN QUESTLINE: JOB 5` block; their checks and tokens are declared by `KestevenJob1Module`, `KestevenJob3Module` and `KestevenJob5Module`. [Job 1](quests/KESTEVEN_QUESTLINE.md#job-1-enemy-unknown-stages-0-to-6), [Job 3](quests/KESTEVEN_QUESTLINE.md#job-3-hostile-takeover-stages-6-to-11), [Job 5](quests/KESTEVEN_QUESTLINE.md#job-5-the-delve-stages-14-to-19) |
| `nskr_kqGlacier`, `nskr_kqGlacierHit` and the other `nskr_kqGlacier*` triggers | The Glacier facility, `# KESTEVEN QUESTLINE: GLACIER`. `KestevenGlacierModule` claims Glacier with `nskr_kqGlacier`, so `CorePlugin` opens a rules dialog that fires it; its `damageFleet` action fires `nskr_kqGlacierHit` once per damaged ship, with the ship in the tokens `$nskr_kq_glacierHitShip` and `$nskr_kq_glacierHitHull`. The other triggers are private picks and inserts of the block. [Glacier](quests/KESTEVEN_DIALOGUE.md#glacier) |
| `nskr_kq_eliza*` rows, `nskr_kqElizaSpacerOptions`, `nskr_kqElizaSlyGone` | The Eliza search, quest `kq`, block `# KESTEVEN QUESTLINE: ELIZA SEARCH`: three rules bar events on `AddBarEvents` gated by `KestevenElizaSearchModule` checks (`elizaSpacerHere`, `elizaSlySpacerHere`, `elizaContactHere`), quest people `nskr_kq_roughSpacer`, `nskr_kq_slySpacer` and `nskr_kq_pirateContact` made the speaker with `BeginConversation`, actions for the search step, the price, the contact and Eliza's market, exits with `BarCMD returnFromEvent true`. [Eliza search](quests/KESTEVEN_DIALOGUE.md#eliza-search-at-pirate-bars) |
| `nskr_isKStage N`, `nskr_isAtLeastKStage N` | Condition predicates on the legacy stage int, used by rows outside the hub. |
| `nskr_optionStartsWith "<prefix>"` | Condition: `$option` begins with the prefix. Paginated pickers add a suffix to their option ids; the quest dev menu uses it. |
| `nskr_isBaseOfficial <post>` | Condition on the active person's post: `command`, `military`, `admin`, `ttadmin`, `trade`/`op`, `research`, `intelligence`, `trader` or `any`. Gates the official menus below. |
| `nskr_debtMenu`, `nskr_shipSwapMenu`, `nskr_modRemovalMenu` | Private menus of `nskr_debt`, `nskr_shipSwap` and `nskr_modRemoval`. Entry and return rows fire the menu trigger; exit rows fire `PopulateOptions`. |
| `nskr_debt <verb>` | Kesteven debt menu, a service outside the quest framework. Conditions: `hasOption` (a Kesteven market, Kesteven relationship above -50 and `KestevenQuest.elizaEndingDone()` false), `available <amount>` (that amount can be taken or repaid now), `isLoan <amount>` (the amount takes a loan). Script: `init` writes the display tokens below and rolls the interest rate when none is set; `preview <amount>` writes `$nskr_debt_amountStr` and `$nskr_debt_costStr`; `take <amount>` changes the saved debt and the player's credits and prints the vanilla credits receipt. `<amount>` is an id of `nskr_debt.Amount`: `loanSmall`, `loanLarge`, `loanAll`, `repaySmall`, `repayLarge`, `repayAll`; the amount handler rows store it in local `$nskr_debt_pick` with expiry `0`. Queries write nothing, so rows call `init` before any row that shows a `$nskr_debt` token; the debt collector rows in `# INTERCEPTS` call it too. Triggers: `nskr_debtMenu` (status and service menu), `nskr_debtLoans` (one option row per amount, disabled by a row on `!nskr_debt available <amount>`), `nskr_debtPreview` (confirmation screen), `nskr_debtStatus` (shared debt and limit lines, fired with `FireBest`). |
| `nskr_shipSwap <verb>`, `nskr_modRemoval <verb>` | Artifact exchange and S-mod removal. The commands own the pickers, stock, points and game actions; all text and fixed options are in their rules blocks. `nskr_shipSwap` queries: `hasOption`, `hasStock SHIP` or `hasStock WEAPON` (reads the stock that `prepareStock` created), `canAfford`; actions: `prepareStock`, `getShipForSale`, `getGunForSale`, `sell`, `confirmPurchase`; token: `listPicked`. `nskr_modRemoval` queries: `hasOption`, `hasShips`; actions: `getHulls`, `remove`; token: `prepareRemove`. The only Java text left is receipts (points gained and lost, purchased items), picker titles and the pickers' own panels. |
| `nskr_shipSold`, `nskr_shipSwapPicked`, `nskr_shipSwapPickCancelled`, `nskr_modRemovalShipPicked`, `nskr_modRemovalPickCancelled` | Fired with `FireBest` from the picker callbacks of `nskr_shipSwap` and `nskr_modRemoval`. A picker returns after the command that opened it has finished, so the callback fires the rows that show the result. Picking nothing counts as cancelling. |
| `nskr_shipSwapPickedHull`, `nskr_shipSwapPickedWeapon`, `nskr_modRemovalSModLine` | Per-item lines: `nskr_shipSwap listPicked` fires one per picked hull or weapon, and `nskr_modRemoval prepareRemove` one per S-mod, after writing that item's name token. |
| `$nskr_debt_points` / `…Str`, `$nskr_debt_MaxpointsStr`, `$nskr_debtInterest` / `…Str`, `$nskr_debt_<amount>Str`, `$nskr_shipSwap_points` / `…Str`, `$nskr_shipSwap_costStr`, `$nskr_shipSwap_itemName`, `$nskr_modRemoval_shipName`, `$nskr_modRemoval_hullName`, `$nskr_modRemoval_sMods`, `$nskr_modRemoval_sModName`, `$nskr_modRemoval_removed` | Local display values written with expiry `0` by the owning command before the rows that show them. The saved values are in sector persistent data; the artifact exchange stock is in market memory. `$nskr_modRemoval_sMods` and `$nskr_modRemoval_removed` are counts as Strings, which rows compare with `== 1` to choose between "S-Mod" and "S-Mods". `$nskr_modRemoval_shipName` is a player-chosen name, so the command escapes it with `Matcher.quoteReplacement`: text replacement passes values to `String.replaceAll`, where `$` and `\` are replacement syntax and can throw. |
| `$nskr_contracts_count`, `$nskr_contracts_targets`, `$nskr_contracts_rewardPer`, `$nskr_contracts_rewardTotal` | Offer display values written with expiry `0` to the contact's memory by `ContractsMission.updateInteractionDataImpl`, which the mission hub runs when it creates the offer. The saved offer is a `ContractInfo` in sector persistent data. |
| `nskr_contracts_blurb`, `nskr_contracts_option`, `nskr_contractsOfferText` | `ContractsMission` hub mission offer. The hub fires the two `nskr_contracts_*` triggers; the start row sets `$missionId = nskr_contracts` and fires `FireAll nskr_contractsOfferText`. Rows select on `$nskr_contracts_type` and `$nskr_contracts_factionBounty`. |
| `nskr_thronesGift <verb>` | Throne's Gift automation dialog, opened by `ThronesGiftIntel` on `nskr_thronesGiftPick`. Conditions: `hasShips` (the fleet has a ship that can be automated), `canAfford`, `hasPicked` (the picked ship is still in the fleet). Script: `pick <title> <ok> <cancel>` opens the ship picker and fires `nskr_thronesGiftPicked` or `nskr_thronesGiftPickCancelled`; the automate option's prompt uses the shared `nskr_quest confirm`; `automate` automates the picked ship; `refreshIntel` redraws the intel panel after `DismissDialog`. |
| `$nskr_thronesGift_shipId`, `…_shipName`, `…_hullName`, `…_cost`, `…_points` | Local display values and the picked ship's id, written with expiry `0` by `nskr_thronesGift pick` and `automate`. Ship and hull names are stored escaped for `String.replaceAll`; quote them when passing them to `SetTextHighlights`. The points are saved in sector persistent data by `ThronesGiftManager`. |
| `nskr_hsWarning`, `nskr_hsJudgement`, `nskr_hsJudgementPath`, `nskr_hsFightComms`, `nskr_hsFightDisengage`, `nskr_hsFightStoryDisengage`, `nskr_hsFightBattleDisengage` | Hellspawn judgement, quest `hs`, block `# HELLSPAWN`. The quest opens `nskr_hsWarning` and `nskr_hsJudgement` as rules dialogs on the player fleet; `nskr_hsJudgementPath` is the private `FireBest` pick of the judgement's path. `HellSpawnJudgementInteraction`, a Java fleet encounter, fires the four `nskr_hsFight*` triggers with `FireBest` for its lines, so those rows print text only. Actions: `musicStart`, `musicStop`, `musicFarewell`, `thrnAnimate`, `grantPeacefulHeart`, `engageJudge`; `engageJudge` replaces the dialog's plugin and is the last line of its row. [Hellspawn judgement](quests/HELLSPAWN.md) |
| `nskr_hint_*` rows, `nskr_hintTipOfferText`, `nskr_hintTipOfferOptions`, `nskr_hintIntelTitle`, `nskr_hintIntelBullets`, `nskr_hintIntelDesc` | Exploration hints, quest `hint`, block `# HINTS`. The Kesteven officer's tip is a rules bar event: `nskr_hint_tipBar` on `AddBarEvents` (condition `nskr_quest hint check tipHere`), the officer `nskr_hint_officer` made the speaker with `BeginConversation`, the `FireBest` pick `nskr_hintTipOfferText` of the offer paragraph by threat word (`check offerDerelict`, `check offerRemnant`), the private offer menu `nskr_hintTipOfferOptions` (buy option gated by `check canAffordTip`), actions `buyTip` and `leaveTip`, and exits with `BarCMD returnFromEvent true`. Intel rows select on `$nskr_intel_key` (`signal`, `tip`, `frost`); signal and tip text reads its record through `$nskr_intel_record`. [Exploration hints](quests/HINTS.md) |
| `nskr_bsSite`, `nskr_bsFactionHighlight`, `nskr_quest bs ...` | Blacksite quest `bs` ([BLACKSITES.md](quests/BLACKSITES.md)). `BlacksiteModule` claims every station with `nskr_bsSite`, so `CorePlugin` opens a rules dialog that fires it; the rows pick the screen with `check dormant` or `check active`, which read the `SiteRecord` of the dialog target. `check faction <factionId>` selects the `nskr_bsFactionHighlight` row that colours the owner's name; `do activate` spawns the defenders. The tokens `$nskr_bs_factionName`, `expectedCount`, `expectedNoun`, `strength`, `remainingCount` and `remainingNoun` read the record of the entity whose text is replaced. |
| `$KestevenQuestJob4Target`, `$KestevenQuestJob4Friendly`, `$KestevenQuestTTCollector`, `$ElizaFleet`, `$RevengeanceEliza`, `$RevengeanceJack`, `$BetrayalFleet`, `$InterceptPlayerElizaFleet`, `$CacheGuardianFleet`, `$EnigmaDormantFleet` | Fleet memory flags set by the spawning Java owner. They select that fleet's `OpenCommLink` and `BeginFleetEncounter` rows. |
| `$nskr_ic_aro`, `$nskr_ic_messenger`, `$nskr_ic_messengerLeaving`, `$nskr_ic_collector`; `nskr_icMessengerMessage`, `nskr_icCollectorDemand`, `nskr_icCollectorPay` | Role flags that `QuestFleets` writes on the fleets of quest `ic`, tested by the `# INTERCEPTS` rows ([Fleets](../jars/src/lostsector/quest/README.md#fleets)). `nskr_icMessengerMessage` is the messenger's speech, fired with `FireBest` from the comm-link row of either messenger role; its script runs `nskr_quest ic do messengerMet`. The debt collector's demand is the `FireBest` pick `nskr_icCollectorDemand` (checks `collectorCanPayAll` and `collectorCanPaySome`, token `$nskr_ic_collectorPayment`), and both pay handlers fire the shared insert `nskr_icCollectorPay` (actions `collectorPay` and `collectorLeave`); see [PayOffEncounter](../jars/src/lostsector/quest/README.md#payoffencounter) and [Debt collector](quests/CONTRACTS_AND_BOUNTIES.md#debt-collector). |
| `$nskr_bounty_abyss`, `$nskr_bounty_eternity`, `$nskr_bounty_mothership`, `$nskr_bounty_peacekeepers`; `nskr_bountyMothershipOptions`, `nskr_bountyGuard`; `nskr_bountyIntelTitle`, `nskr_bountyIntelBullets`, `nskr_bountyIntelDesc` | Role flags that `QuestFleets` writes on the bounty fleets of quest `bounty`, tested by the `OpenCommLink` rows of the `# BOUNTY QUEST` block; a beaten fleet moves to role `<id>Beaten` and loses the flag. `nskr_bountyMothershipOptions` is the Mothership comm's `FireAll` menu. `nskr_bountyGuard` is the claimed trigger of Helios and Polaris: its row tests `check mothershipGuards` and hands the dialog to the fleet with `nskr_quest bounty engage mothership` as its last line; a fallback row runs `DismissDialog`. The three intel triggers hold every bounty's intel text, selected by `$nskr_intel_key` ([Intel](../jars/src/lostsector/quest/README.md#intel), [Named bounties](quests/CONTRACTS_AND_BOUNTIES.md#named-bounties)). |
| `job4TargetOptions`, `elizaRevengeanceDialogOptions`, `betrayalDialogOptions` | Private option triggers, fired with `FireBest` from the matching greeting row. |
| `nskr_kq_delve*` rows, `nskr_kqDelveEscort`, `nskr_kqDelveRoom`, `nskr_kqDelveQuestions`, `nskr_kqDelveListenGate`, `nskr_kqDelveAdvance`, `nskr_kqDelveElizaGate`, `nskr_kqDelveDeparture` | The Delve meeting, quest `kq`, `# Meeting` part of the `# KESTEVEN QUESTLINE: JOB 5` block: a rules bar event on `AddBarEvents` gated by `KestevenJob5Module`'s `delveMeetingHere`, the Asteria or Outpost scenes picked with `FireBest` on `check asteriaGenerated`, Jack made the speaker with `BeginConversation` and Alice shown with `ShowSecondPerson`, the question menu `nskr_kqDelveQuestions` with expiry-0 keys in Jack's memory, gate rows that remove an option with `RemoveOption`, the advance with `AddCredits`, and the exit with `BarCMD returnFromEvent true`. [Delve meeting](quests/KESTEVEN_DIALOGUE.md#the-delve-meeting) |
| `nskr_heartFixer` | The `nskr_heart` market menu on `MarketPostDock`/`MarketPostOpen`. |

Entities claimed through the quest framework open a rules dialog on the claimed trigger ([Dialog entry points](../jars/src/lostsector/quest/README.md#dialog-entry-points)); the other entities routed by `CorePlugin.pickInteractionDialogPlugin` open Java dialogs without rules; see [ARCHITECTURE.md](ARCHITECTURE.md#registration-and-lifecycle).

- Namespace IDs and options. Bar option IDs must begin with their mission ID; mission IDs must not prefix one another. `BarCMD` aborts a wrapper whose option prefix differs.
- Many existing rule IDs, triggers and fleet flags are unprefixed. Give new ones the `nskr_` prefix; rename an existing flag only together with every Java writer and every row that reads it.
- Conditions are evaluated before script actions. Prepare generated display tokens on an earlier row: a row cannot display a token its own script has not yet created.
- Use Boolean flags for eligibility. Strings and numbers do not pass a bare-memory condition. Every memory key written through MemoryAPI begins with `$`.
- A bare `score:` line is invalid. Put the score on a real condition. Scores sum, and each condition line counts 1 unless it carries `score:N`, so more conditions do confer priority. Check overlap with unrelated rule families on the same fleet, not just alternatives within one family.
- Explicitly fire the intended menu trigger on entry and return; do not rely on the trigger's name to schedule it. Rows with no options may retain an old panel. Check actual rules and driver behavior for the path being edited.
- `$hailing` and `$highlightComms` are consumed while vanilla builds fleet interaction. Do not treat them as lasting quest state.
- Colour an option after it has been added. Use a later, condition-matched colour row rather than relying on an earlier script.

Check displayed highlight occurrences using [DIALOGUE.md](DIALOGUE.md#shared-text-presentation).

## Fleet and bar exits

`EndConversation` ends the current person or comm conversation inside the open dialog. In a rules-based dialog it clears the active person and then rebuilds the base menu: a fleet dialog calls reinit, which can fire `BeginFleetEncounter` again; a market or person dialog fires `FireBest MarketPostOpen` when the market has not been docked yet (no `$menuState`), otherwise `FireAll PopulateOptions`. `DO_NOT_FIRE`, or `$doNotFireOnConvEnd` on local memory, skips the rebuild. `NO_CONTINUE` shows the default visual at once and, on a fleet, skips the Continue step before `BeginFleetEncounter`. Without `NO_CONTINUE` the last portrait stays on screen, which is why vanilla's comm-link exits run `ShowDefaultVisual` first. In a dialog whose plugin is not rules based it clears no person and rebuilds no menu. Use it on a fleet only when rebuilding fleet/combat options is the intended result.

`DismissDialog` alone does not clean up a fleet encounter's BattleAPI. Lost.Sector has no shared teardown verb yet. When a route must leave a fleet encounter, check that `dialog.getPlugin()` is a `FleetInteractionDialogPluginImpl`, call its `cleanUpBattle()`, then `dialog.dismiss()`. Put this on the owning command as a verb that is also safe for non-fleet interactions.

Bar-event wrappers close with `BarCMD returnFromEvent`, not `close`. It hands the dialog back to the bar, clears the active person and lists the bar events again. `BarCMD returnFromEvent true` instead clears the options and adds Continue (`barContinue`), which lists the bar again when clicked; a finished Java bar event ends the same way unless it sets `noContinue` (`BaseBarEvent.endWithContinue`, `BarEventDialogPlugin.endEvent`). Check confirm, cancel and Escape paths after a custom panel.

`AddBarEvent <id> "<option>" "<blurb>" [<colour>]` queues a blurb and an option on the market's temporary bar event list during `AddBarEvents`; `BarCMD` shows them afterwards, so the row itself has no Options column. The optional fourth argument goes through Token.getColor: `highlight` resolves to the buttonShortcut colour; faction IDs resolve to faction colour. `BarCMD.showOptions` lists these entries before the Java bar events, at most `maxBarEvents` of them, and they take slots from the Java events. The row's conditions run on every visit to the bar, so an entry appears at every market where they pass. Clicking the option stores the `BarCMD` in entity memory as `$BarCMD` (expiry `0`), gives the dialog back to the rules plugin, sets `$option` with expiry `0` and fires `FireBest DialogOptionSelected`; no person is made active, so the handler starts the conversation with `BeginConversation` (0.98a-RC8 `rulecmd/missions/BarCMD.java`).

More of the same source (0.98a-RC8 `BarCMD`, `BarEventDialogPlugin`; `VisualPanel` in `sources-obf/ui.newui.java`):

- A rules entry also takes a slot from `isAlwaysShow()` Java events, which otherwise do not count toward the random number of Java events. It has no frequency, timeout, random pick or tutorial check of its own.
- A click makes a person active only for a hub mission's bar wrapper.
- `returnFromEvent` also aborts the hub missions offered at the bar. Without `true` the list shows at once after "You unobtrusively watch the patrons of the bar...". Listing the bar restores the saved visual, so portraits and `HideVisual` inside the event do not carry over.
- `BeginConversation <id> <minimal> <showRelationship>` calls `showPersonInfo(person, minimal, showRelationship)`. The two-argument `showPersonInfo(person, minimal)` a Java event uses shows the relationship bar only when not minimal and the person's relationship to the player is at least 0.05 either way, so `BeginConversation <id> true false` reproduces `showPersonInfo(person, true)`. `ShowPersonVisual <minimal> [<id>]` calls the two-argument form and does not change the speaker.

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

If it differs, inspect the source format before editing. The loader strips carriage returns from Conditions, Script and Options, but not from Text: a CRLF inside a Text cell stops `OR` from splitting it. Commas in notes must remain inside a correctly quoted field.

## Maintenance

Update verified language and project contracts here when their implementation changes. Preserve exact syntax and source evidence. Full command/key integration procedures belong in `RULES_AUTHORING.md`; link there rather than adding a competing procedure here. Record corrections to the preserved simulator references in its [source correction table](RULES_AUTHORING.md#corrections-to-the-preserved-simulator-references), then align affected summaries and links here. Keep upstream reference files under `docs/rules/` unchanged unless intentionally updating the vendored version; simulator behavior described there is not a substitute for a live game check. Follow [CLAUDE.md](../CLAUDE.md#documentation-upkeep) for ownership and commit requirements.
