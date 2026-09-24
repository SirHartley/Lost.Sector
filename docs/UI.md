# Custom Java UI

This guide covers Java-bound custom panels, widgets and renderers, including
intel, maps, portraits and tooltips. It is not the rules-dialogue guide.
All player-facing wording and shared text-display requirements, including those
used here, belong in [DIALOGUE.md](DIALOGUE.md#shared-text-presentation).

Engine details are checked against Starsector 0.98a-RC8; recheck them when updating
the game.

## Before editing

- Find the owning code and existing widgets in [ARCHITECTURE.md](ARCHITECTURE.md).
  Read the relevant sections here before changing a screen or its presentation.
- Use the Starsector knowledge skill to check vanilla patterns and uncertain API
  behavior. Framework-specific hooks stay in their README files.
- Follow [DIALOGUE.md](DIALOGUE.md) for text production, presentation, Editor review
  and dialogue flow, and [LORE.md](LORE.md) for prose and character constraints.
- When a Java custom panel is opened from rules, follow [RULES.md](RULES.md) and its
  required references for the caller's routing and teardown. Rules-authored text
  and options are governed by `DIALOGUE.md`, not this guide.
- Update the affected contract here in the same commit. Workflow and build
  requirements remain in [CLAUDE.md](../CLAUDE.md#documentation-upkeep).

## Text in custom controls

Use the [shared text presentation](DIALOGUE.md#shared-text-presentation) and
[surface requirements](DIALOGUE.md#surface-requirements).
Do not maintain separate wording or formatting standards for Java strings.

## Custom-dialog hosts

- Clear host options before custom panels and restore the prior menu once on
  close. Check both confirm and cancel paths.
- `showCustomDialog()` always includes a confirm button. Use
  `showCustomVisualDialog()` when the panel must have none.

Use the [project routing](RULES.md#project-routing) for panel returns and
the [fleet and bar exit paths](RULES.md#fleet-and-bar-exits) for teardown.

## Cargo pickers

The description panel passed to `CargoPickerListener.recreateTextPanel` is a
tooltip, not an interactive UI element. Its outer `setForceProcessInput(true)`
must be called through reflection, using `MethodHandle` (see
[reflection restrictions](ARCHITECTURE.md#rendering-ui-reflection-and-audio)),
before nested buttons can receive input.
The nested custom panel's normal `buttonPressed` callback then works. Set its
button to quick mode: vanilla rebuilds this tooltip every frame, so a button
waiting for mouse-up can be replaced between press and release.

To replace a picker grid's contents from a button, queue the change for the
button panel's `advance`, outside input dispatch. Find only the ancestor picker
holding the exact cargo, then that picker's cargo panel by `updateCargoViews`;
no obfuscated names or global UI search. Wait until no stack is held by the
cursor and clear the transfer handler's ledger with `resetTransaction` before
replacing either grid. Otherwise, cancel would replay transfers using obsolete
stack identities.

Sources (0.98a-RC8): cargo picker and custom-panel implementations in
`sources-obf/ui.newui.java`; `StandardTooltipV2.processInputImpl` in
`sources-obf/ui.impl.java`; cargo panel `updateCargoViews` and transfer handler
`resetTransaction/cancelTransaction` in `sources-obf/campaign.ui.java`.

## Hover tooltips

Use transparent custom-panel hotspots to attach stock tooltips to hand-drawn
controls. Vanilla then owns tooltip timing, placement, and clipping.

`addTooltipTo` and `addTooltipToPrevious` default to rebuilding every frame.
Pass `false` as the last argument for fixed help text. Vanilla still creates the
content when the tooltip is shown; this only stops rebuilding it throughout the
hover.

Use the non-rebuilding mode for fixed help text; keep live updates for text that
reads changing state. Do not cache a tooltip that reads changing state unless its
owner refreshes it.

Source: `StandardTooltipV2Expandable.addTooltipTo/addTooltipToPrevious`,
`beforeShown` and `advanceImpl` in the knowledge base's `sources-obf/ui.impl.java`.

## Rebuilding lists

Create a UI element and call `addUIElement` on the same `CustomPanelAPI`.
The creator stores its requested height and scrollbar flag in maps keyed by the
tooltip. Removing the element or clearing children does not remove those entries.

For a list rebuilt repeatedly, reuse its content or give each rebuild a new child
custom panel. Remove the old child panel, not just its tooltip or scroller. Keep
the list's `createUIElement` and `addUIElement` calls on that disposable owner.
Do not keep old owners in another cache.

After creating each replacement panel, keep the position returned by that panel's
`addUIElement` call. Use this position for drawing and mouse checks. The old panel's position is
no longer valid, and the full scrollable content is larger than the visible viewport.

Source: the `CustomPanelAPI` implementation's `createUIElement/addUIElement` in
`sources-obf/ui.newui.java`, and `UIPanel` removal in `sources-obf/ui.java`.

## Layout

- Positions use logical screen units with a bottom-left origin, not normalized
  0-to-1 coordinates. Use the panel's position when drawing; a custom plugin does
  not get an automatic translation to its panel's origin.
- A scrollable `createUIElement(width, height, true)` reserves five units of the
  requested width for its scrollbar. The supplied height is the viewport height.
  For a non-scrolling element it is a minimum content height.
- `addUIElement` returns the scroller's position when scrolling is enabled.
  Use that for row clipping and hit tests. `getExternalScroller()` gives access
  to the scroll offsets.
- `addCustom` normally places content below the previous element and follows its
  left edge. Repositioning one element can therefore shift later content too.
  `addTitle` anchors at the top-left; use a paragraph or a separate header panel
  for a heading inside the content flow.
- Relative anchors must refer to siblings and must not form a cycle.

Sources: `PositionAPI`, `CustomPanelAPI` and `TooltipMakerAPI` in
`sources-api/ui.java`; position recomputation in `sources-obf/ui.java`;
`createUIElement/addUIElement` in `sources-obf/ui.newui.java`; and
`StandardTooltipV2Expandable.addCustom/addTitle` in `sources-obf/ui.impl.java`.

## Rendering and input

For a custom panel, the order is:

| Pass | Order |
|---|---|
| Render | Plugin `renderBelow`, children in panel order, plugin `render` |
| Input | Children in reverse panel order, then plugin `processInput` |
| Advance | Children, then plugin `advance` |

Put backgrounds in `renderBelow`. Apply the supplied alpha to custom drawing.
Respect consumed input: a parent plugin may receive events already handled by a
child. Use `isLMBDownEvent()` or `isLMBUpEvent()` for a single activation;
`isLMBEvent()` matches both. `setQuickMode(true)` changes vanilla buttons to
activate on mouse-down; it does not disable their checked-state toggle.

If a click needs to rebuild a subtree, prefer queuing it for the owning panel's
`advance` rather than changing the hierarchy midway through input dispatch.

Sources: custom-panel callbacks in `sources-obf/ui.newui.java`; child dispatch
and button handling in `sources-obf/ui.java`; mouse-event predicates in
`sources-obf/util.A.java`.

### Drawing gotchas

- Stencil-buffer masking (`GL_STENCIL_TEST`) breaks campaign radar. Use a
  depth-mask pair (`glDepthMask`) instead.
- `GL_LINE_STIPPLE` restarts on each `GL_LINES` segment and is unusable for short
  campaign lines. Build dash geometry explicitly.

Campaign VFX, reflection restrictions and sound formats remain in
[ARCHITECTURE.md](ARCHITECTURE.md#rendering-ui-reflection-and-audio).

## Keep optimizations local

Reuse existing widgets and text where practical. `TooltipMakerAPI` is a supported
UI builder, not just a floating tooltip. `SettingsAPI.createLabel/createTextField/
createCheckbox` can also create standalone widgets without an extra builder.
Do not share a static tooltip builder between screens: it carries layout and
listener state.

Keep off-screen row culling and rebuild only when displayed data changes.
Profile a slow panel before imposing list-size limits or replacing its renderer.
OpenGL draw submission costs CPU time, but that does not make the rendering
CPU-only.

## Review the affected screen

Check the changed states, not just the fully unlocked dev view: unknown data,
partial progress, locked or owned gear, repeated names, and completed objectives
where applicable. Open and close custom panels through confirm, cancel and Escape;
check the restored options and sidebar. Rebuild scrollable lists repeatedly and
check clipping, hit tests and hover placement at the edges.

Apply the [shared text checks](DIALOGUE.md#shared-text-presentation) to the final
display. Report source checks separately from in-game QA. A successful compile
does not verify layout or text presentation.
