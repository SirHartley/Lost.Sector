# Command Table — normative command vocabulary and behavior for the v2 simulator

> **Location note (2026-07-08):** moved here from the completed (now archived)
> `openspec/changes/archive/v2-engine-cutover/` change, where it was authored
> as the normative spec for the simulator realignment (design D10, task 2.3).
> It REMAINS the live, normative reference for `v2/simulator.py`'s command
> vocabulary — any future simulator change must stay consistent with this
> table or update it with corpus evidence.

Provenance (2026-07-06):
- **Names & frequencies**: script-column token inventory over `temp_core_rules.csv` + `temp_kol_rules.csv`, parsed as CSV (14,039 script lines; 169 distinct command names; 1,633 distinct `$var` assignment targets). Counts below are from that inventory. Frequencies from raw substring grep are NOT trustworthy (`unset $…` contains `set $…`; `expression` occurs in the conditions column, not script).
- **Semantics**: `reference/engine_workflow.md` (decompiled-Java reference). CAUTION: that document's *prose command names* (`setDialog`, `addDialogOptions`, `fireAll`, 3-arg `set`) do not match the corpus — implementing those names verbatim is exactly the bug this table fixes. Trust its behavioral descriptions (§4–§7), never its name spellings.
- **Presentation behavior**: v1 `_execute_command` (`reference/`… after archiving; pre-archive `engine/preview_engine.py:394-491`). v1's preview output was accepted by the user; where the game doc is silent on display concerns, v1 behavior is the bar.

Executor rules:
1. Match command names **case-insensitively** (normalize `cmd.name.lower()` against lowercase keys). The spellings in this table are the canonical corpus forms, but `engine_workflow.md`'s prose uses different casing for the same commands — the correct inference (per the user, 2026-07-06) is that the game engine tolerates input variance, so the simulator should too.
2. Every processed line — including no-ops — is appended verbatim (`cmd.raw`) to `ChainStep.executed_commands`.
3. Unknown names get NO special casing: record and continue. Never raise on malformed args; treat missing args as empty/default and continue.
4. `_substitute_vars` (existing) applies to all user-visible text emitted here.

---

## Tier 1 — traversal-critical (memory, flow, termination)

| Command / form | Count | Behavior |
|---|---|---|
| `$var = value [expire]` → parser emits name `"="`, params `[var, "value [expire]"]` | 4,644 lines (with `++`/`--`) | Split params[1] on whitespace: first token is the value, optional second token is an expire timer — **parse and discard the timer** (doc §1.3: we simulate timer-less). Parse value via existing `_parse_value` (`true`/`false`/float/string). Write to `mem[normalize_key(var)]`. Unqualified `$var` normalizes to local scope (doc §4.3). |
| `$var++` / `$var--` → parser emits name `"+="`/`"-="`, params `[var, "1"]` | (included above) | Numeric add/subtract 1 via existing `_memory_add` logic on the normalized key; missing key starts at 0. |
| `unset $var` | 253 | Remove `normalize_key(params[0])` from memory. |
| `set` / `setMemory` | ~0 (doc-defined alias) | Keep the existing handler but ALSO accept the 2-arg `$scope.key value` form: if params[0] starts with `$`, key = `normalize_key(params[0])`, value = `_parse_value(params[1])`. |
| `add` / `multiply` / `remove` / `removeMemory` | rare | Keep existing handlers; extend to accept `$`-form first arg as above. |
| `FireAll <trigger> [keepOptions]` | 1,766 | Existing `_do_fire_all` logic, renamed match. Fix option batching per doc §5.3/§7.2: collect options from ALL matched sub-rules first; after the loop, if `keepOptions` (param[1] == "true") is false, clear previously collected options ONCE, then add the batch sorted by `order`. (Current code wrongly clears inside the per-rule loop.) Append sub-steps and dialog text as now. |
| `FireBest <trigger> [keepOptions]` | 1,657 | Existing `_do_fire_best` logic, renamed match; same keepOptions batching fix. Note: `FireBest DialogOptionSelected` occurs in the corpus — no special casing needed, self-skip already applies. |
| `EndConversation [DO_NOT_FIRE\|NO_CONTINUE]` | 298 | Set `state.terminated = True`. Record the mode arg via executed_commands only. **New — neither engine handled the corpus's dominant termination command.** |
| `DismissDialog` | 29 | Set `state.terminated = True` (rename existing `dismissDialog` match). Keep `abort`/`leave` matches as-is (harmless). |
| `Call $memRef method [args…]` | 571 | **Recorded no-op.** Verified corpus-wide: first arg is always a `$`-reference to a game object, never a rule id — this is a Java method invocation, not chain flow. v1's execute-rule-by-id interpretation was wrong and never fired. No `execute_rule` signature change, no `chain.py` change. |
| `goto` | 0 | Keep the existing handler (harmless); it simply never matches real data. |
| Bare expression lines (`$option == x`, etc. — parser emits name `"$…"`) | ~1,030 `$option`-leading + misc | Comparison expressions in script return a value but write nothing unless the operator is an assignment (doc §4.2). Recorded no-op. |

**applyRule fidelity (doc §6):** at the top of `execute_rule`, after cloning memory, delete `local.option` and `local.last` from the working snapshot **after matching has already happened** — i.e., inside `execute_rule`, before `_run_script`. Matching against `$option` occurs in the caller (matcher/chain) before execution, so this is faithful: the fired rule's own script must not see `$option`.

## Tier 2 — presentation (text, options, visuals)

| Command / form | Count | Behavior |
|---|---|---|
| rule `text` column | — | Emit as the first text segment (default color) before running script — v1 `_execute_rule:380-382`. **New in v2** (currently only `rule.text` sits unused on the model during simulation). |
| `AddText "text" [color]` | 130 | Emit segment (substituted text, color = params[1] or "default"). v1 parity. |
| `AddTextSmall "text" [color]` | 231 | Same as AddText plus a small-style marker on the segment. **New** (v1 lacked it; nearest v1 behavior = AddText). |
| `Highlight <textOrVar> [color]` | 230 | v1 parity: emit as its own highlighted segment (`preview_engine.py:464-469`), color = params[1] or highlight-default. (Alternative substring interpretation deliberately rejected: v1's accepted output did this, and `SetTextHighlights` exists separately for substring marking.) |
| `SetTextHighlights <substr…>` | 184 | Record substituted substrings into a new `ChainStep.text_highlights: list[str]` (additive field). The PreviewService applies them to segments at render time. **New — this is the game's real highlighting mechanism; v1 never implemented it.** |
| `SetTextHighlightColors <color…>` | 40 | Record into `ChainStep.text_highlight_colors: list[str]` (parallel to the above). |
| `MakeOption <id> [text]` / `ShowDialogButton <id> [text]` | 5 / ~0 | Add option (id, substituted text) — v1 parity. |
| `RemoveOption <id>` | 209 | Remove any collected option with that id from `state.collected_options`. **New.** |
| `SetStoryOption <id> …` / `SetStoryColor <id>` | 64 / ~ | Set `is_story = True` on the collected option with that id (v1 `_mark_story_option` parity). Remaining args recorded only. |
| `ShowDefaultVisual` | 355 | `visual_type = "default"`, `visual_name = ""`. |
| `ShowPersonVisual [name]` | 197 | `visual_type = "person"`, `visual_name = params[0] or ""`. |
| `ShowImageVisual <name> …` | 96 | `visual_type = "image"`, `visual_name = params[0] or ""`. |
| `ShowLargePlanet [name]` | 49 | `visual_type = "planet"`, `visual_name = params[0] or ""`. (v1 lacked; harmless extension for parity of the visual panel.) |
| `HideVisual` | 24 | Reset `visual_type`/`visual_name` to "". |

Segment shape: add `ChainStep.text_segments: list[tuple[str, str, bool]]` — (text, color, is_small) — additive alongside the existing `dialog_text` (which stays plain, populated from the same emissions, so existing consumers including `chain.py`'s empty-text failsafe keep working).

## Tier 3 — recorded no-ops (everything else)

All remaining names (~140 distinct + 1,097 singletons: `AdjustRep*`, `BeginConversation` ×142, `SetShortcut` ×114, `SetEnabled`, `SetTooltip*`, `SetOptionColor` ×70, `PlaySound`, `PrintDescription`, `MakeOtherFleet*`, mod `*CMD` classes, …): append to `executed_commands`, continue. No handler, no warning spam.

Port v1's `VANILLA_COMMANDS` set (`preview_engine.py:~50-60`) into the PreviewService for `get_custom_commands()` — Tier 3 recording is what makes custom commands visible to the Java generator.

## Reconciliation notes (corrections to earlier design numbers)

- `set $` ×259 and `expression` ×155 in design D10 were raw-substring counts contaminated by `unset $…` and conditions-column text. Script-token truth: `unset` ×253; `set`/`expression` effectively absent from the script column. `expression` belongs to the conditions column and is already the (verified, working) evaluator's business.
- `Call` is NOT flow control (see Tier 1). The D10 sentence "Call → execute target rule by id" and the derived chain.py-threading concern are superseded by this table.
