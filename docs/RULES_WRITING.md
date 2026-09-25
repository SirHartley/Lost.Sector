# Writing rules

How to design and write `data/campaign/rules.csv` content: conversations, menus, entry points, conditions, state, text, options and exits. Read it before writing new rows or restructuring existing ones, including when moving Java dialogue into rules.

This guide covers structure and process. [RULES.md](RULES.md) is the language reference: syntax, operators, execution and Lost.Sector routing contracts. [RULES_AUTHORING.md](RULES_AUTHORING.md) explains the mechanisms in depth: commands, memory lifetime, text replacement and Java integration. [COMMANDS.md](rules-reference/COMMANDS.md) and [MEMORY.md](rules-reference/MEMORY.md) are the vanilla lookups. [DIALOGUE.md](DIALOGUE.md) and [LORE.md](LORE.md) govern wording, voice and shared text presentation. This guide does not govern prose; it links to the references where a detail lives there.

Game version: Starsector **0.98a-RC8**. Engine statements are checked against the game source in `starsector-knowledge`. Examples cite vanilla rule ids (`starsector-core/data/campaign/rules.csv`) and Secrets of the Frontier (SotF) rule ids. SotF is the reference for dialogue structure: where SotF, vanilla and this guide differ on a convention, follow SotF, then vanilla. Lost.Sector does not copy SotF's per-speaker text colors.

## Process

Write a conversation in this order.

1. **Describe the scene.** Entry point, speakers, what the player already knows, what the conversation reads (quest state, cargo, reputation), what it changes, and every way out. See [Entry points](#entry-points).
2. **Split it into screens.** A screen is what the player sees between two clicks: one or two short paragraphs and a set of options. A longer speech becomes several screens joined by a Continue option. See [Text](#text).
3. **Choose a structure for each screen.** Most screens are plain chains; lists the player returns to use `FireAll`; state or random variants use `FireBest`. See [Choosing a structure](#choosing-a-structure).
4. **Decide the state.** For each fact the conversation reads or writes, choose its owner and lifetime before writing a row. See [State and memory keys](#state-and-memory-keys).
5. **Write the rows in play order.** Entry row, each beat, each menu's option rows followed by their handlers in the same order, then exits. See [Layout and naming](#layout-and-naming).
6. **Write conditions and scripts.** See [Conditions](#conditions) and [Script](#script).
7. **Prepare tokens and presentation.** See [Text](#text), [Options](#options) and [Visuals and sound](#visuals-and-sound).
8. **Check the result.** Parse the CSV, trace every route and review the text. See [Review checklist](#review-checklist).

## How a row runs

A rule row has seven columns: `id,trigger,conditions,script,text,options,notes`. The trigger is a bucket name; a row runs only when something fires that trigger and every condition line passes. [CSV columns](RULES.md#csv-columns) defines each column.

When a row is applied, its parts run in a fixed order:

| Applied by | Order |
|---|---|
| `FireBest`, including every option click (the dialog fires `FireBest DialogOptionSelected`) | The row's options (Options column and option-adding commands), then its Text as one paragraph, then its Script |
| `FireAll` | The options of **all** matching rows are collected, sorted by order and shown first; then, row by row in load order, each row's Text and Script |

Consequences:

- A row's Script cannot prepare a token for its own Text or options. Prepare it earlier: in the row that led here, or in an entry row that then fires the display trigger.
- In a `FireAll`, a row's Script can prepare Text for a later row in the same call, but not options, which are already built.
- Every row matched by a `FireAll` runs its Script when the menu is built. An option row in a menu must not change state; put the change in the row that handles the click.
- Options replace the old menu only when at least one option was collected. A trigger that matches no option rows leaves the previous options on screen.

## Choosing a structure

Pick the structure per screen, from what the screen does. Do not force one structure on a whole conversation. SotF's 618 option handler rows split 55% plain chains, 24% `FireAll` menus, 15% `FireBest` (a third of those continue chains) and 6% exits or hand-offs to Java.

| The screen | Use |
|---|---|
| A fixed beat: each choice leads to one known next screen | [Plain chain](#plain-chain) |
| A list the player returns to, whose entries depend on state: questions, topics, offers, services | [FireAll menu](#fireall-menu) |
| Exactly one of several versions applies: greeting by stage, reaction by reputation, a random flavor line | [FireBest pick](#firebest-pick) |
| An option's result must run without a click | [Continue chain](#continue-chain) |
| Optional paragraphs that each depend on their own condition | [Text inserts](#text-inserts) |
| The same passage is reached from several places | [Shared insert](#shared-insert) |
| The next step is a game action: a picker, a battle, a trade, a payment with checks | [A command verb](#when-to-use-java) |

The examples below show cell contents without CSV quoting. In the file, a cell with line breaks, commas or quotes is quoted and its inner quotes are doubled.

### Plain chain

The default. The row that handles a click holds the next screen's text and its own options in the Options column.

```text
id: nskr_ex_brief1        trigger: DialogOptionSelected   conditions: $option == nskr_ex_brief1
text:    (first beat)
options: nskr_ex_brief2:Continue

id: nskr_ex_brief2        trigger: DialogOptionSelected   conditions: $option == nskr_ex_brief2
text:    (second beat)
options: nskr_ex_accept:"I'll do it."
         nskr_ex_decline:"Not interested."
```

Use it for scenes, briefings, cutscenes and any exchange whose next screen does not depend on state. SotF's Elysium sequence (`sotfHFinaleElysium1` to `6`) and most of its Wendigo encounter are plain chains. When one option of a fixed screen depends on state, keep the plain chain and gate that one option with `SetEnabled` or a `RemoveOption` row, or switch the screen to a `FireAll` menu.

### FireAll menu

A private trigger collects one row per option. Each option row carries its own conditions, so options appear and disappear with state. Every screen that returns to the menu fires the trigger again.

```text
id: nskr_ex_ask           trigger: DialogOptionSelected   conditions: $option == nskr_ex_ask
script:  FireAll nskr_exQuestions

id: nskr_ex_qPay          trigger: nskr_exQuestions       conditions: !$nskr_ex_askedPay
options: nskr_ex_pay:"What does it pay?"

id: nskr_ex_qRisk         trigger: nskr_exQuestions       conditions: !$nskr_ex_askedRisk
options: nskr_ex_risk:"How dangerous is it?"

id: nskr_ex_qBack         trigger: nskr_exQuestions
options: 100:nskr_ex_back:"That's all."

id: nskr_ex_pay           trigger: DialogOptionSelected   conditions: $option == nskr_ex_pay
text:    (the answer)
script:  $nskr_ex_askedPay = true
         FireAll nskr_exQuestions
```

Use it for question hubs, topic lists, service menus and bargaining menus: any list the player comes back to. SotF examples: `APomiInitialOpts` (a quest offer's questions), `SierraConvOptions` (a hub character), `sotfWendigoCHoffers` (a bargaining menu). Vanilla's `PopulateOptions` is the same pattern for market, person and fleet menus.

- Options without an order show in the order their rows are written. Give an explicit order only to move one, typically `100:defaultLeave:Leave` to the bottom; see [Options](#options).
- Keep one row that always matches, such as Back or Leave, so the menu is never empty.
- Mark read answers with a flag on the speaker ([State](#state-and-memory-keys)) or disable them with `SetEnabled`; do not rely on the player remembering.

### FireBest pick

A private trigger holds several versions of one thing; `FireBest` shows exactly one. Each condition line counts 1, so the row with more passing condition lines wins over a more general one. `score:N` replaces a line's 1 with N. Rows with equal scores are picked at random.

```text
id: nskr_ex_greet             trigger: nskr_exGreeting
text:    (general greeting)

id: nskr_ex_greetOffered      trigger: nskr_exGreeting   conditions: nskr_ex stage OFFERED
text:    (greeting while the offer is open)

id: nskr_ex_greetFailed       trigger: nskr_exGreeting   conditions: nskr_ex failed score:100
text:    (greeting after a failure, overrides the others)
```

Use it for greetings, status lines, reactions to reputation or past choices, and random flavor lines. SotF examples: `SierrasThoughts` (39 versions chosen by state and score), `sotfSierraConvDefaultGreeting` (three unconditioned greetings picked at random). Vanilla examples: `PickGreeting`, `RelationshipLevelDesc`.

- Give the fallback row fewer condition lines than every specific row, often none, so it wins only when nothing specific matches.
- Use `score:N` only to make one row win regardless of how many lines the others have, as vanilla's `defaultGreetingIgnore` does with `$temporarilyIgnoreYou score:10`.
- Two specific rows that can match together must not have the same score unless a random choice between them is wanted. Add the condition that separates them, or give one a higher score.
- Equal rows for random variety are deliberate; a note in the notes column tells the next writer so (a Lost.Sector addition; SotF leaves the column empty).
- `FireBest <trigger> true` keeps the current options and adds the winner's. Vanilla uses it to list several missions' options on one screen.

### Continue chain

Set `$option` to a handler's option id and fire `DialogOptionSelected`; that handler runs as if the player had clicked it.

```text
$option = nskr_ex_showOffer 0
FireBest DialogOptionSelected
```

Use it to run a screen's logic after a script step without adding a throwaway button, or to join a shared handler from several places. Give `$option` the expiry `0`. The row that fires the chain cannot match itself again; any other handler for the new `$option` can. Vanilla uses this over a thousand times; SotF uses it in `sotfDKCBOfferText`. For a scene the player should read at their own pace, use a real Continue option instead.

### Text inserts

Several rows on one private trigger each add a paragraph when their own condition holds; `FireAll` shows every one that applies, in load order. SotF's `sotfOmiLabInterests` adds one paragraph per officer the player has; vanilla's `RelLevelMoreDescription` adds faction-specific lines under the relationship description. Use it for status summaries or fallout descriptions built from independent facts. The rows carry text only, no options.

### Shared insert

A passage reached from several conversations gets its own private trigger and one row, fired with `FireBest`. Put the guard flag on that row, so the passage and its side effect happen once no matter who triggers it. SotF's `sotfLearnAboutDustkeepers` reveals a codex entry once, from any conversation that mentions the Dustkeepers.

### When to use Java

Rows cannot do everything. Use a command verb ([Rule commands](#rule-commands)) when the next step is:

- a picker (ships, cargo, officers), a paginated list whose length is unknown, or a custom panel;
- a plain yes/no confirmation before an irreversible action. No rules command adds one; story point options get theirs from `SetStoryOption`;
- a spawn, a battle, a fleet order, an intel update or a reward that no vanilla command grants;
- a check that needs game data rules cannot read, such as fleet strength, market lists or distances.

Java must not print prose. It may print short receipt lines in the vanilla receipt style when no vanilla command does the grant. Text, options and branching stay in rows.

Options that change with data are a Java job only when the count or labels cannot be known in advance, such as one option per ship. A fixed set of options belongs in rows, even when Java decides whether each is available: expose that decision as a condition verb.

These catch.release cases show Java doing a row's job. Avoid them:

- Removing a hard-coded list of option ids from Java instead of `RemoveOption` lines.
- Four copies of "if this cargo exists, color and describe this option" instead of four conditioned rows with `SetOptionColor` and `SetTooltip`.
- Adding menu options from a command that does not report `doesCommandAddOptions()`, so `FireAll` never clears the old menu and the code clears it by hand.

### Anti-patterns

- A `FireAll` menu for a linear scene. If the options never change, use a plain chain.
- A private trigger with a single row that only holds options or text the calling row could hold.
- `FireAll` or `FireBest` in Conditions. Either runs in full during matching: it prints text, replaces options and runs scripts while the engine is still choosing a row. SotF never does it. Vanilla does it only in a few mission-return greetings (`gaDAMissionReturn`: `Call $global.gaDA_ref updateData` and `FireBest ASEBMissionReturn` in Conditions) to run a step before the greeting text is chosen, and that works only because the fired trigger always matches.
- The same paragraph copied into several rows. Use a shared insert or a continue chain.
- A menu option row that changes state. It runs every time the menu is built.

## Entry points

A conversation starts from a trigger that vanilla or Java fires. Add rows to it; do not replace vanilla rows. Your rows join or win by their conditions, so make them specific.

| Situation | What fires | Where to add rows |
|---|---|---|
| Talking to a person from a comm directory | Java fires `OpenCDE` into the open dialog with the person active; vanilla `convDefault` fires `FireBest PickGreeting`; the greeting row calls `ShowPersonVisual` and `FireAll PopulateOptions` | A `PickGreeting` row with `$id == <personId>` for the greeting; `PopulateOptions` rows with `$id == <personId>` for the menu |
| Docking at a market | `OpenInteractionDialog`, then `FireBest MarketPostOpen`, then `FireBest MarketPostDock`; vanilla `marketDock` sets `$menuState = main 0` and fires `FireAll PopulateOptions` | `PopulateOptions` rows with `$menuState == main` for market menu options |
| Opening a comm link with a fleet | The fleet encounter's embedded rules dialog makes the commander active and fires `FireBest OpenCommLink`; if nothing matches, the player sees static | An `OpenCommLink` row conditioned on the fleet flag its spawner set, such as `$entity.nskr_x` |
| A fleet encounter | The fleet encounter fires `BeginFleetEncounter` on open, and again after `EndConversation` | A `BeginFleetEncounter` row conditioned on the fleet flag |
| A station or custom entity | The standard rules dialog fires `OpenInteractionDialog`; vanilla `defaultOpenDialog` is the fallback | An `OpenInteractionDialog` row conditioned on the entity, such as `$customType == <type>` |
| A salvageable object or campaign objective (relay, sensor array, derelict station) | Vanilla `cob_openDialog` (`OpenInteractionDialog`, `$tag:objective`) fires `FireAll COB_AddOptions`, then `FireBest COB_DisableOptionsIfNeeded` and `FireAll COB_DisableIndividualOptions` | A `COB_AddOptions` row for an extra option, as SotF's `sotfStationOptions` does; the disable triggers to block vanilla options |
| A bar event | `BarCMD` fires `FireAll AddBarEvents`; `AddBarEvent <optionId> "option" "blurb"` queues the blurb and option, which `BarCMD` then shows | An `AddBarEvents` row with `AddBarEvent`; the handler on `DialogOptionSelected`; return with `BarCMD returnFromEvent` |
| A hub mission offered by a contact | `<missionId>_blurb` and `<missionId>_option`, each fired with `FireBest ... true` | The blurb and option rows; the offer row sets `$missionId = <missionId>` and calls `Call $<ref> ...` |
| A hub mission offered at the bar | `<missionId>_blurbBar` and `<missionId>_optionBar`, with the mission's person as `$local` | The same shape as the contact offer |
| A rules dialog opened from Java | `showInteractionDialog(new RuleBasedInteractionDialogPluginImpl("<trigger>"), target)` fires `<trigger>` when the dialog opens. `FireBest.fire(null, dialog, memoryMap, "<trigger>")` fires a trigger into a rules dialog that is already open, as `OpenCDE` does | Rows on `<trigger>` |

Which memory scopes exist depends on the entry. With an active person, `$local` is the person's memory and `$entity` the target's; without one, `$local` is the target's memory and `$entity` does not exist. `$faction` stays the target's faction even while a person from another faction speaks; use `$personFaction` for theirs. See [Memory ownership and scopes](RULES_AUTHORING.md#memory-ownership-and-scopes).

## Conditions

Every line of the Conditions cell must pass. Lines are checked top to bottom and matching stops at the first line that fails. There is no OR inside a row.

- **OR** is two rows, or a condition verb that answers the combined question.
- **Line order.** Put the line that rules the row out most often first, and cheap lines before command calls.
- **Flags.** `$flag` passes only when the value is `true` or the String `"true"`. An unset key fails. `!$flag` passes when the flag is unset or not true.
- **Comparisons.** `==`, `!=`, `<`, `>`, `<=`, `>=`. The ordering comparisons parse both sides as numbers and count an unset key as 0. `==` against an unset key fails; `!=` passes. See [Operators](RULES.md#operators-verified-against-decompiled-source).
- **Nothing else.** `is`, `in`, `has`, `has_not`, `does_not_have` and `+=` are not operators in this build. Such a line silently checks only its first `$key`.
- **Scopes.** Write the scope when the key is not on `$local`: `$player.nskr_x`, `$global.nskr_x`, `$entity.nskr_x`. An absent scope falls back to local and silently reads a key literally named `$entity.nskr_x` there.
- **Commands.** A command in Conditions answers a question: `PlayerHasCargo supplies 10`, `nskr_isBaseOfficial command`. `!Command args` negates it. Condition commands run for every candidate row on every matching round, so they must be cheap and must not change state, grant anything or roll a new random target.
- **Specificity.** In a `FireBest` trigger, rows compete by their summed score; see [FireBest pick](#firebest-pick). `FireAll` ignores scores.
- **Quest state.** Ask the owner with a condition verb (`nskr_kestevenQuest ...`) or a mission reference instead of copying quest state into memory for rules to read.
- **Case.** Keys and trigger names are case sensitive. `$nskr_x` and `$nskr_X` are different keys; a `FireAll` on a misspelled trigger finds no rows and shows nothing. SotF ships this bug: two rows fire `sotfWendigoCHOffers` while the menu's rows use `sotfWendigoCHoffers`.

## Script

Script lines run top to bottom. Each line is an assignment, a command, or a `#` comment.

```text
$nskr_ex_askedPay = true               saved on the current owner
$nskr_ex_payStr = "12,000 credits" 0   display value, gone after the dialog
$nskr_ex_visits++                      count up; stores a Float
unset $nskr_ex_offerShown              remove now
AddCredits 12000                       vanilla grant with its own receipt
nskr_ex accept                         project command verb
FireAll nskr_exQuestions               rebuild the menu
```

- An assignment takes an optional expiry in days after the value; see [Memory lifetime](RULES_AUTHORING.md#memory-lifetime). `0` means "until the dialog closes and time resumes", not "immediately".
- `$x = 5` stores the String `"5"`; `$x = $y` copies what `$y` holds; `$x++` and `$x--` store a Float. There is no `+=`.
- Quote a String value that contains spaces.
- Every line runs: a command that returns false does not stop the next line. Check first (in Conditions, or in a separate row), then act.
- A line that holds only spaces stops the whole file from loading. Delete such lines; a truly empty line is harmless.
- Handler rows change state; option rows and text rows do not.
- Put a grant in the same row that shows the hand-in text, so the receipt follows the prose.

## State and memory keys

Decide where each fact lives before writing rows.

| Fact | Owner | Example |
|---|---|---|
| Quest progress, targets, timers, decisions, counters | The quest's Java owner (a mission, intel or manager), read through a command verb or a mission reference | `nskr_kestevenQuest getStage`; a hub mission's `$<missionId>_stage` |
| "Already asked", "already introduced" and other conversation flags | The speaker's own memory: an unscoped key while the person is active | `$nskr_ex_askedPay` |
| What a fleet is for | The fleet's memory, set by its spawner | `$debtCollector` |
| Something the player knows, shared by several conversations | `$player` | SotF `$player.sotf_knowDustkeepers` |
| A sector-wide fact that rows in unrelated places read | `$global`, only when no better owner exists | SotF `$global.apromise_completed` |
| A value shown in text | A String with expiry `0`, written before the row that shows it | `$nskr_debt_pointsStr` |

- Name keys `$nskr_<feature>_<name>` in lowerCamel. Give a key one spelling everywhere.
- Keys Java reads or writes get a Java constant; keys only rows use do not need one.
- Do not read another feature's or another quest's keys. Ask its owner.
- Do not use `$global` as a scratch area. A flag that only one person's conversation reads belongs on that person.
- Keep saved state and display strings separate. A dialogue must never reroll a saved target while preparing its text.

[RULES_AUTHORING.md](RULES_AUTHORING.md#memory-lifetime) covers lifetimes, overwrites and scope fallbacks in detail.

## Text

### Where text goes

- **Text column** for prose. It is the default: SotF puts 68% of its rows' text there. The whole cell is one paragraph; line breaks inside it stay inside that paragraph.
- **`AddText` in Script** when a line must appear between two commands, for example after a portrait change. `AddTextSmall` prints the same in the small font. Each call adds a new paragraph.
- A row that only dispatches (fires a trigger, sets flags) has no text.

### Length

Keep paragraphs short and screens to one or two paragraphs. SotF's median paragraph is 24 words and its median screen 43 words. Split a long speech into screens with a Continue option instead of one wall of text.

### Narration and speech

Narration is plain. Speech is in quotation marks. Lost.Sector does not color speech per speaker. Use color only for meaning, as [DIALOGUE.md](DIALOGUE.md#shared-text-presentation) defines: highlights, gains and losses, item names.

### Tokens in text

- Vanilla supplies person and player tokens (`$heOrShe`, `$personName`, `$playerName`) and facts; see the [memory and text dictionary](rules-reference/MEMORY.md). They are case sensitive: `$heOrShe` and `$HeOrShe` are separate tokens.
- A value from your own state is a display String written before the row shows it; see [Create a custom text token](RULES_AUTHORING.md#create-a-custom-text-token).
- Write scoped tokens when the key is not local: `$player.nskr_x`. An unqualified token takes local first; if two other scopes hold the same name, which one wins is not defined.
- Format numbers in the String you prepare (`Misc.getDGSCredits`, `Misc.getWithDGS`). A Float prints with a decimal point: after `$x++`, `$x` shows as `6.0`.
- A token nothing replaces stays on screen as written. Check the final text for leftover `$` tokens, internal ids and wrong capitalization.

### Variants

Versions of a line separated by a line containing only `OR` are picked at random, with equal weight, each time the row fires. Use them for lines the player sees repeatedly: greetings, refusals, idle chatter. Do not use them for lines that carry information. Keep the file's line endings LF: a carriage return inside a Text cell stops the split.

### Highlights and small text

`SetTextHighlights` and `SetTextHighlightColors` color phrases in the last paragraph shown, in the order they appear. Put them in the Script of the row whose Text they decorate, before any `AddText` in that Script. Repeat a phrase for each occurrence. See [Shared text presentation](DIALOGUE.md#shared-text-presentation).

After the prose, small gray text can state a mechanical consequence the prose does not. SotF writes it as an indented list: `AddTextSmall "    - Progress made\n    - Its scorn grows" textGrayColor` (`sotfHauntedPenult4`). Real grants use the vanilla receipt commands, which print their own receipts; see [Receipts](#receipts).

## Options

- **Options column** for every option whose label is known in advance. Format `order:id:text`. Lower order shows higher.
- **Order.** Normally leave it out, as 94% of SotF's and vanilla's options do: `id:text` has order 0, and equal orders show in the order the rows and lines are written. Write an order only to move an option away from that position, for example `100:defaultLeave:Leave` to keep Leave last in a menu assembled from several rows.
- **No colons in labels.** The loader splits the line on every colon: a colon in an `id:text` label stops the file loading, and in `order:id:text` the label is cut at the next colon.
- **Labels.** Spoken options in quotation marks, actions without; see [Dialogue and player options](LORE.md#dialogue-and-player-options). Add a bracketed note only where the words hide the consequence: `(lie)`, `(decline)`, `(attack)`. SotF does this on under 5% of its options. Labels get token replacement.
- **Ids.** `nskr_<feature>_<purpose>`, never starting with `$`. Name the handler row after the option it answers so a search finds both; SotF adds a suffix (option `sotfDKOME_askHire`, handler `sotfDKOMEaskHireSel`). The handler's condition is `$option == <optionId>`.
- **Handlers.** Every option id needs a `DialogOptionSelected` row, except the ids vanilla already handles: `defaultLeave` and the `cutCommLink` family (see [Exits and returns](#exits-and-returns)). A click with no handler prints a red error and an "Exit dialog" option, unless the option went through a confirmation such as a story point option.
- **Unavailable choices.** Keep the option and disable it when the player should see what is possible: `SetEnabled <id> false`, then `SetTooltip <id> "Requires 10 supplies."`, with `SetTooltipHighlights` and `SetTooltipHighlightColors` for the numbers. Hide it with a condition when the player should not know about it yet.
- **Decorating an option.** `SetEnabled`, `SetTooltip`, `SetOptionColor`, `SetOptionText`, `SetShortcut` and `RemoveOption` do nothing if the option does not exist yet. Put them in the Script of the row that adds the option, or later.
- **Story point options.** `SetStoryOption <id> <points> <bonusXPKey> <sound> "<log text>"`, with the key registered under `bonusXP` in `data/config/settings.json`. It colors the option, adds the cost to its label, adds the confirmation and disables it when the player cannot pay. Always pass at least four arguments: with exactly three, the command reads them as `<id> <sound> <log text>` and charges one point. `leadership`, `combat`, `industry`, `technology`, `general` and `generic` are accepted as sounds; SotF uses `general`. Do not add `SetStoryColor` to the same option.
- **Dev options.** An option id starting with `(dev)` only appears in dev mode.

## Exits and returns

| Way out | Use |
|---|---|
| Leave a person, station or market dialog | `100:defaultLeave:Leave`. Vanilla's `defaultLeave` row runs `DismissDialog`, and the id gets Escape automatically. |
| End a fleet comm link | An option id starting with `cutCommLink`. Vanilla handles `cutCommLink`, `cutCommLinkPolite` and `cutCommLinkNoText` with `ShowDefaultVisual` and `EndConversation`. They get Escape automatically, except `cutCommLink2` and `cutCommLinkNoText2`. |
| Back to the dialog's own menu | `ShowDefaultVisual`, then `EndConversation`. It clears the active person; a market or person dialog fires `MarketPostOpen` or `FireAll PopulateOptions`, and a fleet dialog runs `BeginFleetEncounter` again. |
| Back to a parent menu inside the conversation | A custom option such as `nskr_ex_back` whose handler fires the parent menu. Bind Escape with `SetShortcut nskr_ex_back ESCAPE` when it is the screen's way out. |
| Leave a bar event | `BarCMD returnFromEvent`. |

- `EndConversation` leaves the last portrait on screen; vanilla calls `ShowDefaultVisual` first. `EndConversation NO_CONTINUE` shows the default visual itself and, on a fleet, skips the Continue step. `EndConversation DO_NOT_FIRE` does not rebuild the menu. In a dialog that is not rules based, `EndConversation` neither clears the person nor rebuilds a menu.
- `DismissDialog` closes the window. Leaving a fleet encounter needs its battle cleaned up first; see [Fleet and bar exits](RULES.md#fleet-and-bar-exits).
- Every screen needs a way forward or out, including refusal, lack of money and completed hand-ins. See [Navigation](DIALOGUE.md#navigation).

## Visuals and sound

- `ShowPersonVisual [minimal] [personId]` shows the active person, or an important person by id. It does not change who is speaking. An id that is not registered with the important people crashes it.
- `BeginConversation <personId> [minimal] [showRelationship]` makes a person the active speaker: their memory becomes `$local` and person tokens follow them. The id can be an important person, a person at the target's market, or `POST:<postId>`.
- `ShowSecondPerson <id>` and `ShowThirdPerson <id>` add portraits for a scene with several characters; the ids must be important people. `HideSecondPerson` and `HideThirdPerson` remove them.
- `ShowImageVisual [category] <key>` shows a sprite from `settings.json` at its own size (category `illustrations` by default); `ShowPic <key>` shows an illustration at a fixed 640 by 400. `ShowDefaultVisual` restores the target's own visual. `SaveCurrentVisual` and `RestoreSavedVisual` return to a visual after a detour.
- `ShowMapMarker <entityOrMarketId> [title] [text]` and `HideMapMarker` show and remove a map beside the portrait.
- `PlaySound <soundId>` plays a UI sound. `PlayCustomMusic <musicId>` needs a later `ResumeNormalMusic`. SotF uses no sound in dialogue; use it for a specific event only.

## Receipts

`AddCredits`, `AddRemoveCommodity`, `AddRemoveAnyItem`, `AddStoryPoints` and the `AdjustRep` commands print their own receipt lines in the vanilla style ("Gained 3x supplies"). Do not print a second receipt for the same grant, and do not grant it again in Java. See [COMMANDS.md](rules-reference/COMMANDS.md#common-recipes) for arguments.

## Rule commands

Lost.Sector's commands live in `lostsector.dialogue.rules`, normally one class per feature with the first argument as the verb ([Project routing](RULES.md#project-routing)). When adding a verb:

- A **query** verb answers a condition and changes nothing.
- An **action** verb does one game action and is called from a handler's Script.
- A **token** verb writes display Strings with expiry `0` before the row that shows them.
- A verb that adds options must report `doesCommandAddOptions()` so `FireAll` and `FireBest` include its options and clear the old menu. Prefer the Options column.
- A verb never prints prose. Receipts only, when no vanilla command covers the grant.

A hub mission is its own command target: `Call $<ref> <action>`. Every hub mission handles `updateStage`, `updateData`, `showMap <title>`, `hideMap`, `makeUnimportant`, `addContacts`, `repSuccess`, `repFailure` and `endFailure`; anything else goes to the mission's `callAction`, and an action nobody handles crashes the dialog. See [Reuse a mission object through Call](RULES_AUTHORING.md#reuse-a-mission-object-through-call). [Create a command only when needed](RULES_AUTHORING.md#create-a-command-only-when-needed) covers adding a command class.

## Layout and naming

- **One block per feature.** A `# FEATURE NAME` row opens it and blank rows end it, as in SotF and vanilla; rows whose id starts with `#` are skipped by the loader. Existing Lost.Sector blocks also carry `#END` rows; new blocks do not need them. Long quests get sub-headers along their stages, as SotF's `# THE HAUNTED` block does.
- **Play order.** Within a block, the entry row first, then each beat in the order the player meets it. A menu's option rows sit together, directly above their handler rows, in the same order.
- **Blank rows** separate conversations and menus.
- **Rule ids** `nskr_<feature>_<purpose>`: sequential numbers for a strictly linear scene (`..._brief1`, `..._brief2`), short names for branches. The loader only rejects a duplicate id under the same trigger, so keep every id unique yourself.
- **Private triggers** `nskr_<feature><Purpose>`: `...Options` for menus, `...Greeting` or `...Text` for picks and inserts, and a plain descriptive name for a shared insert or side effect, as SotF's `sotfLearnAboutDustkeepers`.
- **Comments.** A `#` line inside Conditions, Script or Options is skipped; use it for a non-obvious line, as SotF's `# removes map` explains a bare `ShowPersonVisual`. The notes column can say why a score exists or which rows are deliberate random variants; SotF leaves it empty, so this is a Lost.Sector addition. Keep jokes and history out.

## Review checklist

- **Structure.** Each screen uses the structure that fits it. No menu for a linear scene, no copied paragraphs.
- **Conditions.** Every `FireBest` trigger has a fallback and no accidental ties. Only real operators. No `FireAll` or `FireBest` in Conditions. Condition commands change nothing.
- **Triggers and keys.** Every fired trigger has rows with exactly that spelling. Every key is read and written with the same spelling and scope.
- **Options.** Every option has a handler and no colon in its label. Every menu has an option that always shows. Every screen has a way out. Decorating commands run after their option exists.
- **State.** Quest state comes from its owner. Conversation flags sit on the speaker. Display values have expiry `0`. Menu option rows change nothing.
- **Text.** No leftover tokens, internal ids or `.0` numbers on screen. Highlights match what is shown. Paragraphs are short.
- **CSV.** The file round-trips byte for byte, every row has seven columns, and no Conditions or Script line holds only spaces; see [Editing and validation](RULES.md#editing-and-validation).
- **Routes.** Trace entry, every option, refusal, lack of money, hand-in, return and exit, plus save and load. See [Technical handoff](DIALOGUE.md#technical-handoff).

## Maintenance

Update this guide when a structural convention changes or a new pattern is adopted. Engine facts belong in [RULES.md](RULES.md) and mechanism procedures in [RULES_AUTHORING.md](RULES_AUTHORING.md); link to them rather than restating them. Replace SotF examples with Lost.Sector rows once converted conversations exist. Follow [CLAUDE.md](../CLAUDE.md#documentation-upkeep) for ownership and commit requirements.
