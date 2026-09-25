# Dialogue and player-facing text

Start here for dialogue, options, intel, tooltips, UI labels, messages, ship, weapon, hullmod and item descriptions, Codex entries, tips, mission copy, and console output.

These guidelines apply whether text comes from `rules.csv`, Java or another data file. This is the shared guide for player-facing wording, text presentation and dialogue flow; it is not limited to spoken dialogue. Subject matter and storage format do not change the standard of review.

- [LORE.md](LORE.md): setting definitions, absolute knowledge limits, prose style, character instructions, examples, terminology and information-release order.
- [UI.md](UI.md): Java-bound custom UI implementation, including panels, widgets, tooltips, sprites, layout and input. It uses this document's shared text guidelines.
- [RULES_WRITING.md](RULES_WRITING.md): how to structure rules content: the writing process, choosing a structure per screen, conditions, state, text placement, options and exits. [RULES.md](RULES.md): CSV format, rule execution, project routing and technical validation. [RULES_AUTHORING.md](RULES_AUTHORING.md): command/key usage, pronoun context, memory lifetime and Java integration, including fixes to existing mechanisms. Read them as applicable and the required rules skills; none is a prose guide.
- [ARCHITECTURE.md](ARCHITECTURE.md): code and data owners, registrations, and lifecycle connections.
- [CLAUDE.md](../CLAUDE.md#documentation-upkeep): task workflow and automatic documentation upkeep.

This document does not override lore or define rules-engine syntax. Preserve user-supplied dialogue verbatim unless the user asks for a rewrite.

## Starsector Editor

Every new or revised player-facing line must be drafted and audited with the pinned Starsector Editor GPT. This includes small labels and console messages, not just spoken dialogue. Reading or quoting existing text, editing project documentation, and changing technical routing without changing prose do not require an Editor request.

Use the pinned chat directly through the app; do not reach it through a browser. The coding agent owns implementation, state, and technical validation. The Editor reviews prose only.

### Prepare the request

1. Confirm manually that the Editor is set to High thinking. Do not use Auto, Standard, Fast, or a lower setting. If High is unavailable, stop the prose work and report it; continue independent technical work where possible.
2. Supply the full current `LORE.md`, not a summary. Identify its revision or content hash. Within the same Editor session, reuse an unchanged supplied version; resend it if the file changed or the session no longer has it. Recheck High after a session or model-setting change, or when its state is uncertain.
3. Name the surface, speaker, audience, current quest stage, and information the player already has. Include the exact lines and surrounding exchange.
4. Give the mechanics, required facts, tokens, highlight phrases, layout limits, option order, and intended return or exit. Separate these constraints from prose the Editor may change.
5. For a regression, include the prior and current wording. If the prior version cannot be recovered, say so rather than inventing it.
6. Use focused passes for different speakers or surfaces, followed by one integrated review of the final exchange.

Require concrete language, distinct voices, and natural sentence variety. Reject canned contrasts, repeated templates, gratuitous triplets, vague abstractions, explanations of subtext, false ominousness, filler, and habitual em dashes.

### Integrate and review

The Editor may revise only the requested prose. It may not change rule IDs, triggers, conditions, commands, tokens, row order, mechanics, facts, or layout contracts. Return weak or inaccurate lines for another High-thinking pass; do not silently replace its text with a new coding-agent draft.

Independently check the result against the full current lore and the actual display context. Check the complete final exchange once. Reopen a passed review only when an integration change or a concrete issue affects it. Technical rules and UI checks remain the coding agent's responsibility.

## Shared text presentation

Apply these requirements to all player-facing text, including rules-authored dialogue and Java-built screens. Feature-specific notes below add requirements; they do not limit these to a particular character, item or quest.

- Match vanilla's typography and spacing for the same kind of display. Body prose, compact gain/loss notices, labels and hover descriptions serve different purposes. Choose formatting by that purpose, not by the subject or the file containing the text.
- Use colour consistently for meaning: ordinary text, emphasis, item identity, availability, gains and losses are distinct roles. Use the owning data or shared colour source, not a locally invented shade. Information serving the same role must not change meaning or colour between rules dialogue and Java UI.
- Highlight arguments follow displayed occurrence order. Repeat the argument when the same name or value appears again. Check first, middle and last items, including token-expanded text. Formatting one item must not accidentally colour the next paragraph or the rest of a list.
- Review the final displayed text after substitution and composition. Check names, quantities, units, punctuation, spacing, line breaks and highlights together. Do not leak unresolved tokens, internal IDs or raw state values in place of the intended wording.
- Use established player-facing labels and saved values. Explain restrictions and consequences before a choice; distinguish an offer or preview from an actual result. Shared formatting must not conceal a condition or imply a grant that did not happen.
- Fit text to its display without shrinking ordinary prose into receipt text or stripping out its character. Shorten repetition, split information at a useful point or adjust the layout as appropriate. Shared presentation does not mean a shared voice or a repeated sentence template.

Rules text placement, options and exits are covered by [RULES_WRITING.md](RULES_WRITING.md); command ordering by [RULES.md](RULES.md#project-routing). Java custom-panel layout and rendering are covered by [UI.md](UI.md). Neither implementation path is exempt from the checks above.

## Presentation in rules

Quest dialogue is written in `rules.csv`, and its presentation uses vanilla commands. This section names the command for each effect; [Shared text presentation](#shared-text-presentation) sets the colour and highlight rules, [RULES_WRITING.md](RULES_WRITING.md#visuals-and-sound) the placement, and [COMMANDS.md](rules-reference/COMMANDS.md) the exact arguments. Java prints nothing but receipts.

### Portraits and speakers

- `BeginConversation <personId> [minimal]` makes a person the speaker: the card appears, `$local` becomes their memory and the pronoun tokens follow them. Use it for every speaking character, including quest people the framework generates, which are registered under `nskr_<quest>_<key>` when their stage starts.
- `ShowPersonVisual false <personId>` shows a card without changing the speaker; `ShowSecondPerson` and `ShowThirdPerson` add the other people in the room, and `HideSecondPerson` or `HideThirdPerson` removes them before the scene moves on. All ids must be registered people; an unregistered id is ignored by the second and third slots and crashes `ShowPersonVisual`.
- A bar event's person is generated by the quest before the bar opens, so the handler row can call `BeginConversation` at once.
- The clicked option is echoed by the engine; do not print it.

### Highlights

`SetTextHighlights "phrase" ...` colours phrases in the last paragraph, in order, one argument per occurrence; `SetTextHighlightColors highlight bad` when the colours differ. Put them in the Script of the row whose Text they decorate, before any `AddText`. Tokens are allowed in the arguments; a quest token must be quoted, `SetTextHighlights "$nskr_kq_job3Start"` ([Tokens](../jars/src/lostsector/quest/README.md#tokens)).

### Small and gray text

- Gray narration (thoughts, asides) is `AddText "..." gray`. The Text column cannot be coloured, so a screen that is entirely gray keeps its Text cell empty and prints with `AddText`.
- A consequence the prose does not state goes in small gray text after it: `AddTextSmall "Acquired the expedition coordinates" gray`, with `SetTextHighlights` for the name. Do not write a small line for a grant that prints its own receipt.
- A custom colour is an `r,g,b,a` literal that matches the Java constant it mirrors; use it only where the intel already uses that colour.

### Images and planets

`ShowDefaultVisual` restores the target's own image, planet or fleet view. `ShowImageVisual <key>` shows an illustration from `settings.json`; `HideVisual` fades the panel out for a scene with no picture; `ShowLargePlanet` shows the planet of the dialog target's market (`MarketAPI.getPlanetEntity`), and nothing for a target without one or while the `3dPlanetBGInInteractionDialog` setting is on (0.98a-RC8 `rulecmd/ShowLargePlanet.java`). Hide extra portraits before switching to an image.

### Map markers

`ShowMapMarker <entityId> ["title"] ["text"]` puts a map beside the card and `HideMapMarker` removes it. Quest destinations are marked on the map by the quest itself; use the dialog marker only to point at a place the conversation names.

### Sounds and music

`PlaySound <soundId>` plays one UI sound after a result, such as `ui_rep_raise` after a payment or `ui_noise_static` when a link is cut. `PlayCustomMusic <setId>` replaces the music for a scene and `ResumeNormalMusic` must follow before the dialog ends. Story-point options play their own sound; do not add one.

### Confirmations

An option that cannot be undone gets a prompt: `nskr_quest confirm <optionId> "Are you sure? This cannot be reversed." "Confirm" "Cancel"` in the Script of the row that adds the option, after the option exists. Both labels are required. The option's handler is unchanged: confirming fires `DialogOptionSelected` as a normal click does; declining leaves the screen as it was. If the option does not exist yet, the verb logs an error and adds nothing. Story-point options get their prompt from `SetStoryOption` and need nothing else.

### Receipts

Grants with a fixed amount use the vanilla commands, which print the receipt: `AddCredits`, `AddRemoveCommodity`, `AddRemoveAnyItem`, `AddStoryPoints`, `AdjustRep`, `AdjustRepActivePerson`, `AddPotentialContact`. Computed amounts, ship damage, skills and mod currencies go through a quest action, whose Java prints the same small gray line vanilla would. Intel shown or updated from an action prints its own line. Write the receipt in the row that shows the hand-in prose, and never twice for one grant.

## Writing

Read [LORE.md](LORE.md) for the setting's facts, absolute knowledge boundaries and character instructions. Its [Writing style](LORE.md#writing-style) section owns prose guidance, examples, dialogue cadence and player-option wording. Those requirements apply to every player-facing surface below.

For edits to the lore reference itself, follow [documentation upkeep](../CLAUDE.md#documentation-upkeep); do not apply fictional ambiguity to its instructions.

### Dialogue and options

Use the [lore's dialogue and option instructions](LORE.md#dialogue-and-player-options) for voice and wording, then the [navigation requirements](#navigation) below for availability, transitions and exits.

### Surface requirements

Use these surface requirements alongside the shared guidance above, regardless of which implementation displays the text.

| Surface | Required information |
|---|---|
| Tooltip | What it does first; flavor afterward. State restrictions and costs plainly. |
| Navigation or request hover | Explain the destination or filter and retain all requirements; showing a location is not a substitute for the full conditions. |
| Quest offer | Complete demand, destination, deadline, reward preview, and unusual conditions before acceptance. Do not repeat the whole reward section in both character speech and the shared pitch. |
| Accepted intel | Purpose, special terms, exact requirements and live progress, deadline, rewards, and navigation. |
| Receipt | Actual items, credits or knowledge granted, after the hand-in prose. Use the existing receipt path rather than a second invented payment sentence. |

Subject-specific additions: none are recorded for Lost.Sector yet. Add them here as a separate, labelled table when a subject needs required information beyond the surfaces above.

Use the terminology in [LORE.md](LORE.md#terminology) and the labels supplied by the owning UI/data definitions. Do not expose internal names such as `nskr_` IDs or class names, or maintain a second vocabulary table here.

### Review questions

Does the line fit the speaker's job and knowledge? Does it tell the player what they need to do? Does its voice belong in Starsector? Does narration explain a joke, prescribe an emotion, or reveal a mystery the lore leaves unresolved?

Apply the [common mistakes and corrections](LORE.md#common-mistakes-and-corrections) to the complete exchange. Check the speaker's knowledge separately from the player's possible interpretation; a stylistically restrained line can still reveal a forbidden fact.

## Dialogue behavior and presentation checks

Use the [shared text presentation checks](#shared-text-presentation) alongside these dialogue-flow criteria. `RULES.md` owns rules implementation; `UI.md` owns Java custom UI implementation.

### Navigation

- Every reachable state needs an intentional next step or exit, including insufficient cargo, unavailable stock, decline, cancellation, and completed hand-in.
- Reading one informational option must return to the appropriate question menu so the others remain available. It must not accept, decline, pay, or end the exchange.
- Accepted-job comms must show the active task's hand-in, reminder, not-yet, and exit choices, not the original offer's accept/decline menu.
- Keep generated targets, prices, rewards and case details stable when leaving and reopening an unaccepted offer. Display saved values; prose must not reroll them.
- Show hand-in prose and actual reward receipts before moving to a new offer or ending the interaction. Multi-stage hand-ins use a Continue handoff before the next stage's accept/decline choices.
- Peaceful fleet resolutions leave a visible Leave option. Explicit flee and cut-link choices may exit directly. Technical teardown must use the proper fleet path in `RULES.md`.

Custom-panel opening and return behavior is covered by [UI.md](UI.md#custom-dialog-hosts).

## Technical handoff

Before a rules edit, read [RULES_WRITING.md](RULES_WRITING.md), [RULES.md](RULES.md), the relevant mechanisms in [RULES_AUTHORING.md](RULES_AUTHORING.md) and the required rules references. The authoring guide also applies to Java-only changes to commands, memory and text replacements. Keep prose review separate from CSV round-trip checks, column counts, token/command validation, state tracing, and runtime tests. The Starsector Editor is never a technical rules validator.

Trace each affected route through initial contact, questions, acceptance, return, insufficient cargo, hand-in, next round, cancellation and exit as applicable. Include overlapping flags and save/load state. Do not invent a dependency on an unavailable visualizer. Report what static checks establish and what still requires in-game QA.

## Maintenance

Update the relevant section in the same commit when text workflow, shared text presentation or a reusable dialogue convention changes. Keep shared guidelines content-agnostic; label subject-specific additions separately. Keep Java custom UI contracts in `UI.md`; setting facts, knowledge limits, prose style, characterization and their explanatory examples in `LORE.md`; engine semantics and project rules contracts in `RULES.md`; rules structure and writing process in `RULES_WRITING.md`; command/key integration procedures in `RULES_AUTHORING.md`; and owner/connection information in `ARCHITECTURE.md`. Repair cross-links after moving a section. Do not append a task history or copy complete dialogue trees into this guide.
