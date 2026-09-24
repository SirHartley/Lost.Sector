# Starsector Rules Engine: Complete Workflow Reference

> **REVISED 2026-07-07.** This document was corrected in place after three of
> its claims were disproven against the decompiled source and the real rules
> corpus during the v2-engine-cutover planning consult: command-name spellings
> (its prose used names that occur zero times in real data), the truthiness
> gate's case-sensitivity, and assignment-condition passing. Corrections are
> integrated below and marked ⟦verified 2026-07-06⟧ where read directly from
> decompiled code or measured in the corpus. For the full per-command dispatch
> specification, `reference/command_table.md` remains normative; evidence
> trail: `openspec/changes/archive/v2-engine-cutover/dryrun_findings.md`.

This document describes the Starsector campaign rules engine from first principles. It is
intended as a definitive reference for agents working on the Rules Visualizer -- the Python
application that parses, indexes, and simulates this engine.

Source: decompiled Java at `C:\Program Files (x86)\Fractal Softworks\Starsector ref 0980\starsector java\com\fs\starfarer\`.
Key classes: `Rules.java`, `oOOO.java` (Expression), `Memory.java`,
`RuleBasedInteractionDialogPluginImpl.java`, `FireAll.java`, `FireBest.java`.

---

## 1. DATA MODEL

### 1.1 Rules

A rules file (`data/campaign/rules.csv` or mod equivalents) is a standard
7-column comma-separated CSV ⟦verified: the tool's fixtures ARE these files⟧:

```
id,trigger,conditions,script,text,options,notes
```

Multi-value cells (several conditions, several commands) hold one entry per
line inside a quoted cell.

- **id**: unique string identifier (e.g. `opt_0`, `merchant_meet_1`)
- **trigger**: string bucket that groups rules. The engine fetches all rules for a given trigger
  when matching. Not a "fires when" signal -- just a partition.
- **conditions**: optional, one per line. The rule matches only when EVERY condition passes
  the truthiness gate (§4.1). Empty means the rule always matches.
- **script**: commands executed when the rule is applied. Real command names are Java class
  simple names as they appear in the corpus ⟦verified by token inventory: `FireAll` ×1766,
  `FireBest` ×1657, `Call` ×571, `ShowDefaultVisual` ×355, `EndConversation` ×298,
  `AddTextSmall` ×231, `Highlight` ×230, `RemoveOption` ×209, `SetTextHighlights` ×184,
  `AddText` ×130, …⟧, plus lowercase memory builtins (`unset` ×253) and — the single largest
  category — bare `$var = value` / `$var++` assignment lines (4,644 = 33% of all script lines).
  Names like `setDialog`/`addDialogOptions` that earlier drafts of this doc used DO NOT EXIST
  in real data.
- **text**: the dialog display text shown when the rule fires.
- **options**: `order:id:text` or `id:text`, one per line.
- **notes**: developer commentary, ignored by the engine.

**Multiple rows with the same id** form one logical rule. The engine merges them:
- First row: id, trigger, condition, and the first command
- Subsequent rows (empty id column): additional commands appended to the script

The trigger from the first row is the rule's trigger. Subsequent rows do NOT have a trigger field.

### 1.2 Conditions

A condition is a single expression. There are two syntactic forms:

**Comparison/Assignment**: two operands with an operator
```
$option == opt_0
$player.relations == faction_X
$local.visited > 3
$local.counter = 0        (assignment, also a valid condition)
```

**Command**: a command plugin whose `execute()` returns a boolean
```
hasMarket
hasPerson
hasFleet
isFactionHostile
Nex_IsModActive "mod_id"   (Nexerelin addon, returns boolean)
(other custom CMDs)
```

### 1.3 Memory

The engine maintains a **map of named memory scopes**, keyed by scope name:

| Scope key        | Meaning                              |
|------------------|--------------------------------------|
| `global`         | Sector-wide persistent state         |
| `player`         | Player fleet state                   |
| `local`          | Per-interaction transient state      |
| `market`         | Current market state                 |
| `sourceMarket`   | Origin market (for trade dialogs)    |
| `entity`         | Current sector entity state          |
| `faction`        | Faction state                        |
| `personFaction`  | Person's faction state               |
| `mission`        | Mission state                        |

Each scope is a `Memory` instance: a `LinkedHashMap<String, Object>` (insertion-order
preserved). Values are `Object` -- typically `Boolean`, `Float`, `String`, `List<String>`,
or `null`.

For the project we want to record memory per-tree with only the memory checked within that tree.

**Variable syntax**: `$scope.key` (e.g. `$player.relations`, `$local.option_count`).
Unqualified `$var` resolves to **local** memory by default.

**Special variables**:
- `$option` -- set by the dialog driver when the player clicks an option. Contains the
  option's ID (e.g. `opt_0`). Stored in **local** memory.
- `$last` -- previous option ID. Stored in **local** memory. Cleared before each rule fires.
- `$opt0`, `$opt1`, ... -- option text labels (e.g. `$opt0` resolves to the text of option 0).

**Deep snapshot semantics**: `Memory.getAll()` returns a **copy** where null values are
converted to `Boolean.FALSE`. This means `memoryMap.get("local").get("nonexistent_key")`
via `getAll()` returns `false`, not `null`. Direct `get(key)` returns the raw value (may be
null).

**Dependency tracking**: when a value is set via `set(key, value)`, the memory tracks which
other keys "depend on" it. Dependent keys get a timer. When the timer expires, the dependent
key is removed. This is used for temporary state that should clean up automatically. 

Because the project does not simulate time, we treat these as timer-less.

**Expiry**: `Memory` has an `expire` list of `(key, timeLeft)` pairs. On each game tick,
timers are decremented and expired keys are removed. The `dependOn(key, time)` method adds a
key to this list.

---

## 2. DIALOG LIFECYCLE

The `RuleBasedInteractionDialogPluginImpl` class drives dialog interactions. It is the
**sole orchestrator** -- no other code path fires triggers during normal play.

### 2.1 Initialization (`init()`)

1. Create a fresh **local** memory: `Global.getSector().getMemoryManager().createMemory()`
2. Build the memory map (global, player, local, market, entity, faction, etc.)
3. Fire the `DialogStart` trigger:
   ```
   rules.new(null, "DialogStart", dialog, memoryMap)
   ```
   This is `getAllMatching(currentRule=null, trigger="DialogStart", ...)`.
   - `currentRule=null` means NO self-skip (no rule is excluded)
   - All rules with trigger=`DialogStart` are evaluated
   - Conditions are checked; only passing rules are returned
4. Pick the **first** matching rule (not best-matching; just the first in the list)
5. Display the rule's text
6. Collect options from the rule's script, sort by order, add to the option panel
7. Store the rule's id as `currentRule`

**Key insight**: `DialogStart` is the entry point. There is typically one `DialogStart` rule
per interaction type. The condition on that rule determines if the interaction is available.

### 2.2 Option Selection (`optionSelected(optionId)`)

1. Set `$option = optionId` in **local** memory
2. Clear the option panel
3. Fire the `DialogOptionSelected` trigger:
   ```
   rules.new(currentRule, "DialogOptionSelected", dialog, memoryMap)
   ```
4. This returns ALL matching rules for that trigger (conditions evaluated, self-skip applied)
5. Execute the **first** matching rule's script
6. Display text, collect options, add to panel
7. Update `currentRule` to the new rule's id

### 2.3 Failsafe

If `getAllMatching` returns an empty list (no rules match), the engine:
1. Executes `TerminateInteraction` trigger (if any rules match it)
2. If still no match, calls `FireAll.fire("leave", ...)` as a last resort
3. Dismisses the dialog

### 2.4 Termination

The dialog ends when:
- A rule's script calls `EndConversation` (×298 in corpus) or `DismissDialog` (×29)
- No rules match the current trigger (failsafe path)
- The player uses a built-in close/escape option (e.g. `rbid_failsafe_leave`)

---

## 3. RULE MATCHING

Entry points: `Rules.new()` (getAllMatching) and `Rules.o00000()` (getBestMatching).

### 3.1 getAllMatching(currentRuleId, trigger, dialog, memoryMap)

```
1. Look up all rules indexed by `trigger` from static map
2. For each rule:
   a. SELF-SKIP: if rule.id == currentRuleId, skip it (prevents infinite loops)
   b. Evaluate ALL conditions via Expression.isTrueFor(memoryMap, dialog)
   c. If every condition passes, add to results
3. Return results (may be empty)
```

### 3.2 getBestMatching(currentRuleId, trigger, dialog, memoryMap)

Same loop as getAllMatching, but ⟦verified 2026-07-06, decompiled Rules.java⟧:
```
1. For each rule: evaluate conditions in order; on any failure, skip the rule
2. While evaluating, accumulate j += condition.getScore() per passing condition
3. After all conditions pass, j += rule.getScoreBonus()
4. Track the highest j; EXACT ties are collected and WeightedRandomPicker
   selects randomly among them
5. Return the single best match (or null if none matched)
```
Since every condition must pass for the rule to match, a matching rule's
effective score is simply the sum of all its conditions' `score:` tokens
plus the rule's score bonus.

### 3.3 Self-Skip Detail

The `currentRuleId` parameter is the id of the rule that was just applied. It is excluded
from the next matching round. This prevents a rule from matching itself immediately after
firing (which would create an infinite loop if the rule's condition is always true).

When `currentRuleId` is null (as in the initial `DialogStart` call), no rule is skipped.

### 3.4 Score

Score lives on CONDITIONS: a `score:N` token at the end of a condition line is parsed
off into that condition's score field ⟦verified 2026-07-06, decompiled oOOO.java
constructor; plain Java int field, so the default is 0 — NOT 1 per condition⟧. A rule's
effective score in `getBestMatching` = sum of its conditions' scores + the rule's
`scoreBonus` (an API-side field; effectively 0 for CSV-defined rules). Higher wins;
exact ties are chosen randomly via `WeightedRandomPicker`. Score does NOT affect
`getAllMatching` ordering. Corpus usage: 791 rules carry `score:` tokens.

---

## 4. CONDITION EVALUATION

The `oOOO` class implements `ExpressionAPI`. Each Expression represents one condition.

### 4.1 isTrueFor() -- the boolean gate

⟦verified 2026-07-06, decompiled oOOO.java:181 — this is the literal gate⟧
```java
isTrueFor(memoryMap, dialog):
    result = execute(ruleId, dialog, memoryMap)
    return (result == null) ? false
         : (result instanceof String)  ? result.toLowerCase().trim().equals("true")
         : (result instanceof Boolean) && result == Boolean.TRUE
```

Consequences: the string comparison is CASE-INSENSITIVE and trimmed ("True",
"TRUE ", " true" all pass); `null` fails; and any non-String, non-Boolean
result — numbers included — FAILS. An earlier draft of this doc claimed
case-sensitivity; that was wrong.

### 4.2 Two Execution Paths

The Expression has a flag `OO0000` (boolean). When true, it's a **command** expression.
When false, it's a **comparison/assignment** expression.

#### Command Expression

```java
execute():
    cmdClass = getCommandClass(this.commandName)   // lookup in class registry
    cmd = cmdClass.newInstance()
    result = cmd.execute(ruleId, dialog, this.params, memoryMap)
    return result   // boolean from the command
```

The command registry is built at startup by scanning the `com.fs.starfarer.api.impl.campaign.rulecmd`
package. Each command class is keyed by its simple class name (e.g. `HasMarket` ->
`com.fs.starfarer.api.impl.campaign.rulecmd.HasMarket`).

#### Comparison/Assignment Expression

```java
execute():
    leftValue  = resolveToken(this.leftToken, memoryMap, dialog)
    rightValue = resolveToken(this.rightToken, memoryMap, dialog)
    result = dispatchOperator(this.operator, leftValue, rightValue)
    if operator is assignment (=, +=, -=, *=, /=):
        write result to memory at leftToken's scope/key
    return result
```

### 4.3 Variable Resolution (`Ø00000()`)

The resolver takes a `Misc.Token` (parsed string) and resolves it:

1. **`$var`** (variable reference): look up in memory map
   - `$option` -> local memory, key "option"
   - `$player.relations` -> player memory, key "relations"
   - `$var` (no scope prefix) -> local memory by default
2. **`$optN`** (option text): returns the option's text label (e.g. `$opt0` -> "opt_0")
3. **Number literal**: parsed via `Float.parseFloat()`. NaN becomes `0.0`.
4. **String literal**: returned as-is
5. **Unknown**: returns the raw token string

**Memory lookup resolution order**: the scope prefix determines which memory API to use.
`$player.foo` goes to the "player" memory map entry. `$foo` with no prefix goes to "local".

### 4.4 Operators

The operator enum (decompiled as a very long `OoO...` name) defines these operators:

| Operator | Symbol  | Description                                    |
|----------|---------|------------------------------------------------|
| EQ       | `==`    | Equality (value or string comparison)           |
| NEQ      | `!=`    | Not equal                                       |
| LT       | `<`     | Less than (numeric)                             |
| GT       | `>`     | Greater than (numeric)                          |
| LTE      | `<=`    | Less than or equal                              |
| GTE      | `>=`    | Greater than or equal                           |
| IS       | `is`    | Identity / exact match (same as == for primitives)|
| IN       | `in`    | Contains (list contains value)                  |
| IS_NOT   | `is_not`| Not identity                                    |
| IS_NOT_IN| `is_not_in` | List does not contain value              |
| HAS      | `has`   | Memory key exists (has value)                   |
| DOES_NOT_HAVE | `does_not_have` | Memory key does not exist         |
| HAS_NOT  | `has_not` | Alias for DOES_NOT_HAVE                   |
| `=`      | `=`     | Assignment (writes to memory, returns value)    |
| `+=`     | `+=`    | Add and assign                                  |
| `-=`     | `-=`    | Subtract and assign                             |
| `*=`     | `*=`    | Multiply and assign                             |
| `/=`     | `/=`    | Divide and assign                               |

For `HAS` / `DOES_NOT_HAVE` / `HAS_NOT`: the left operand is a variable reference, the
right operand is ignored. The check is whether the key exists in the target memory.

For assignment operators: the result is written to the memory scope of the left operand,
then the value is returned. ⟦verified 2026-07-06⟧ The returned value then faces the §4.1
gate, where a number is neither String nor Boolean — so `$local.x = 5` as a condition
performs its write but the condition FAILS. Only `$x = true` (or a "true" string) both
writes and passes. An earlier draft claimed assignment conditions always pass; that was
wrong.

---

## 5. COMMAND PLUGINS

### 5.1 Structure

Each command is a Java class implementing `CommandPlugin` (which extends `ExpressionAPI`).
Located in `com.fs.starfarer.api.impl.campaign.rulecmd`. The `execute()` method signature:

```java
boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap)
```

Returns `true` on success, `false` on failure. When used as a condition, the return value
determines if the rule matches.

### 5.2 Text Commands

⟦corrected 2026-07-06: `setDialog`/`addDialogOptions`/`addOption` occur ZERO times in
real data — earlier drafts invented them. The real text/option mechanisms are:⟧
- **the `text` column**: the rule's primary dialog text, shown when it fires.
- **the `options` column**: `order:id:text` lines; order controls sort position
  (lower = higher up). Options are collected during rule application, then batched
  to the panel (§7.2).
- **`AddText "text" [color]`** (×130) and **`AddTextSmall "text" [color]`** (×231):
  append additional text paragraphs; `$var` substitution applies.
- **`Highlight <textOrVar> [color]`** (×230): emphasized text.
- **`SetTextHighlights <substr…>`** (×184) / **`SetTextHighlightColors <color…>`** (×40):
  mark substrings of the displayed text for highlighting — the primary in-game
  highlighting mechanism.
- **`MakeOption`/`ShowDialogButton`/`RemoveOption` (×209)/`SetStoryOption` (×64)**:
  add/remove/mark options at script time.
(Full behavior per command: the cutover change's `command_table.md`.)

### 5.3 Flow Control Commands

#### FireAll

```java
fire(ruleId, trigger, dialog, memoryMap, keepOptions):
    matches = rules.getAllMatching(ruleId, trigger, dialog, memoryMap)
    if matches is empty: return false
    for each rule in matches:
        applyRule(rule, dialog, memoryMap)
        collect options from each rule
    sort options by order
    if keepOptions is false:
        clear existing options from panel
    add all collected options to panel
    return true
```

**Key properties**:
- Executes ALL matching rules (not just one)
- Returns `false` if no rules match
- Options from all rules are merged, sorted, and displayed together
- `keepOptions` parameter: if false (default), clears the panel before adding

#### FireBest

```java
fire(ruleId, trigger, dialog, memoryMap, keepOptions):
    rule = rules.getBestMatching(ruleId, trigger, dialog, memoryMap)
    if rule is null: return false
    applyRule(rule, dialog, memoryMap)
    collect options from the rule
    if keepOptions is false:
        clear existing options from panel
    add options to panel
    return true
```

**Key properties**:
- Executes ONE rule (highest score, random among ties)
- Returns `false` if no rules match
- Has `keepOptions` parameter (defaults to false)
- When used as a condition: `FireBest("trigger_name", false)` passes if a rule matched,
  fails if no rule matched

#### Goto

`goto` is implemented as a special command that changes the current rule context. In practice,
it works by firing a new trigger. ⟦corpus note 2026-07-06: `goto` occurs ZERO times in the
vanilla + KOL script columns — treat it as theoretical unless a mod introduces it.⟧

### 5.4 Memory Commands

⟦corrected 2026-07-06 with corpus reality⟧ The overwhelming majority of memory writes
are **bare assignment lines in the script column** — `$var = value [expireTime]`,
`$var++`, `$var--` — 4,644 lines, a third of all script content. The optional trailing
expire token attaches a cleanup timer (the visualizer treats these as timer-less).
Command-form memory ops that actually appear:
- **`unset $var`** (×253): removes the key.
- **`set`/`setMemory`/`add`/`multiply`/`remove`**: exist as command classes but are
  rare-to-absent in the CSV corpus (raw-grep counts were previously inflated by
  substring collisions — `unset $…` contains `set $…`). When they do appear, the
  `$scope.key value` argument form is used.
- **`expression`**: occurs in the CONDITIONS column, not script — it is condition
  machinery, not a script command.

### 5.5 Condition-Only Commands (return boolean, no side effects)

- **`hasMarket`**: Returns true if interaction is at a market.
- **`hasPerson`**: Returns true if there's an interaction target person.
- **`hasFleet`**: Returns true if there's an interaction target fleet.
- **`isFactionHostile`**: Returns true if the target faction is hostile to player.
- Many more in the `rulecmd` package.

### 5.6 Side-Effect Commands

- **`addCredits`**: Adds credits to player.
- **`addCommodity`**: Adds commodity to market or player.
- **`spawnContact`**: Spawns a fleet contact.
- **`EndConversation [DO_NOT_FIRE|NO_CONTINUE]`** (×298) / **`DismissDialog`** (×29): end the interaction.
- **`abortMission`**: Aborts the current mission.
- **`acceptMission`**: Accepts a mission.
- Hundreds more -- the full list is in the `rulecmd` package.

---

## 6. APPLYING A RULE

When a rule is selected (either by getAllMatching or getBestMatching), it is "applied":

```
applyRule(rule, dialog, memoryMap):
    1. Clear $option and $last in local memory
    2. Run the rule's script (iterate over all commands):
       for each command in rule.script:
           result = command.execute(ruleId, dialog, memoryMap)
    3. Collect options that were added during script execution
    4. Return the collected options
```

The script is a `List<ExpressionAPI>` -- each command in the rule's CSV rows (after the first,
which has the condition) is one Expression. They execute sequentially.

**Important**: `$option` is cleared BEFORE the script runs. This means the rule's own script
cannot see which option led to it firing. The option ID is available in `$last` if needed
(though `$last` is also cleared -- it's set by the dialog driver AFTER the previous rule
completes).

---

## 7. OPTION HANDLING

### 7.1 Option Creation

Options come from the rule's options column, plus script-time creation via `MakeOption`/`ShowDialogButton` (and mutation via `RemoveOption`/`SetStoryOption`).
Each option has:
- **id**: string identifier (e.g. `opt_0`, `opt_1`, `merchant_buy`)
- **text**: display text (supports `$var` substitution)
- **order**: integer sort key (lower = displayed higher)

### 7.2 Option Collection & Display

Options are NOT added to the panel immediately during script execution. Instead:
1. They are collected as `OptionAdder` objects during `applyRule`
2. After the rule completes, all options are sorted by `order`
3. If `keepOptions` is false, the panel is cleared
4. All options are added to the panel in sorted order

This batching ensures that a rule can add multiple options atomically, and that `keepOptions`
works correctly (you don't want to clear the panel mid-script).

### 7.3 Text Substitution

Option text and dialog text support token replacement. `$var` references in the text are
resolved against the memory map at display time (not at rule definition time). The engine
uses `Global.getSector().getRules().performTokenReplacement(ruleId, text, entity, memoryMap)`.

---

## 8. TRIGGER CHAIN SEMANTICS

### 8.1 How Chains Work

The "chain" in the visualizer corresponds to the sequence of rules fired as the player
interacts. The chain is NOT pre-computed by the engine -- it emerges from the interaction:

```
Player clicks option -> $option set -> DialogOptionSelected trigger fires ->
    matching rules evaluated (conditions + self-skip) ->
    best/first rule applied -> script runs -> options displayed ->
    (repeat when player clicks next option)
```

The visualizer's chain model simulates this offline:
1. Start from a root rule (DialogStart or any rule with no incoming edges)
2. Harvest all option IDs from the rule's script
3. For each option, find all rules that match `DialogOptionSelected` with `$option == optionId`
4. Repeat recursively (BFS/DFS with memory propagation)

### 8.2 Memory Propagation in Chains

When the visualizer traverses a chain, it must track memory state:
- Each rule's script may modify memory (`set`, `add`, assignment conditions)
- The memory state at step N is the cumulative effect of all rules from step 0 to N-1
- Branch points (where multiple options lead to different rules) create divergent memory states
- The visualizer uses a memory snapshot at each branch point

### 8.3 Reachability

A rule is "reachable" if there exists a sequence of option selections from the root that leads
to it. The visualizer computes reachability via graph traversal:
- Build a trigger->rule mapping (all rules indexed by trigger)
- From root, BFS through option->condition->rule edges
- A rule is unreachable if no path from any root reaches it
- Unreachable rules are displayed separately in the visualizer

### 8.4 GOTO Semantics

When a rule's script calls `FireAll` or `FireBest` with a trigger other than `DialogOptionSelected`,
it creates a sub-chain. The visualizer models this as a GOTO:
- The chain jumps to the target trigger
- Rules matching that trigger are evaluated (conditions, self-skip)
- After the sub-chain completes, control returns to the parent chain
- The `keepOptions` parameter controls whether the parent's options are preserved

---

## 9. INDEXING

The engine maintains a static map `trigger -> List<Rule>` at startup. Rules are loaded from
CSV files, parsed, and indexed by their trigger field. This map is populated once and cached.

When a mod adds rules, the same indexing applies -- all rules from all sources (base game +
mods) are merged into a single index.

---

## 10. KEY BEHAVIORS TO REMEMBER

1. **Trigger is just a bucket** -- conditions do the actual filtering. A rule with trigger
   `DialogOptionSelected` and condition `$option == opt_X` only matches when the player
   selected option `opt_X`.

2. **Self-skip prevents loops** -- the rule that just fired is always excluded from the next
   matching round. Use `currentRuleId` in the visualizer's simulation.

3. **Memory is a deep snapshot** -- `getAll()` returns a copy with null->false conversion.
   Direct `get(key)` may return null.

4. **`$option` is the chain mechanism** -- set by the dialog driver, checked by conditions
   in the next trigger fire. Cleared before each rule's script runs.

5. **FireAll vs FireBest** -- FireAll executes ALL matching rules, FireBest executes ONE
   (highest score). Both return false on no match.

6. **Score zero-bleed** -- score only affects getBestMatching. It does not affect getAllMatching
   ordering and does not propagate between rules.

7. **Options are batched** -- collected during script execution, sorted, added to panel after.

8. **Text substitution is at display time** -- `$var` in text is resolved when the text is
   shown, not when the rule is defined.

9. **Condition gate = Boolean TRUE, or a String equal to "true" after lowercase+trim**
   ⟦verified 2026-07-06⟧. "True"/"TRUE " pass; numbers and every other type FAIL.

10. **Assignment conditions write but usually FAIL** ⟦verified 2026-07-06⟧ -- `$x = 5`
    sets the value, returns 5, and 5 fails the gate (not String/Boolean). Only
    boolean-true assignments (`$x = true`) both write and pass.

11. **DialogStart is the entry** -- currentRule=null (no self-skip), first match wins.

12. **DialogOptionSelected is the loop** -- currentRule=last rule's id (self-skip active),
    first/best match wins depending on how it was called.
