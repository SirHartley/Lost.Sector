# Lost.Sector

Lost.Sector is a Starsector content expansion: the Enigma, Kesteven and Prototype Operations factions with their ships, weapons, ship systems and hullmods, scripted quests, bounties and fleet events, custom starts, and new star systems.

## Required workflow

These instructions apply to all coding agents. App-specific tools and Claude model assignments are scoped below.

Reviews, explanations, audits, and proposals are read-only unless the user requests changes. For implementation, finish the authorized edit, commit, validation, pull request, and merge steps. Use reasonable assumptions for routine details; ask when a missing decision would materially change the scope or behavior. A request to stop or propose only overrides the implementation workflow.

### Read the current documentation

1. Read the current remote `CLAUDE.md` in full at the start of each new task. Within the same task, reuse the version already read unless the instructions change. Read new or changed instruction files before following them.
2. Use the `starsector-knowledge` skill before answering or changing anything related to vanilla Starsector. It contains the official API, decompiled internals, and vanilla data for one exact game version. Do not rely on memory.
3. If the skill is incomplete, inspect the read-only archives under `lib/`. Extract them to a temporary directory, never into the repository.
   - `starfarer.api.zip` contains the official game API source; `starfarer.api.jar` is the compiled API.
   - `GraphicsLib.zip`, `Lazylib_lunalib.zip`, and `MagicLib.zip` contain dependency sources and jars.
   - `ExerelinCore.jar` (Nexerelin) and `IndEvo.jar` (Industrial.Evolution) are compiled jars without source. Read their signatures with `javap`; report behavior that needs their source as unverified.
   - For GraphicsLib, LazyLib, LunaLib, MagicLib, Nexerelin and Industrial.Evolution, `lib/` is the primary source because the Starsector skill covers vanilla only.
4. Select the required guides using [Which guide to read](#which-guide-to-read). Apply every matching row, including for reviews and fixes to existing behavior. Follow required skill reading as well; this table does not waive full-reference reads required by a skill.

### Which guide to read

After reading this file, use the task to choose the next document. Read the relevant sections and their prerequisites; the lookup dictionaries do not need a cover-to-cover read. A task spanning prose, rules and Java UI needs all three routes.

| Task | Required reading / order |
|---|---|
| Locate code or data, trace a feature, change an owner or lifecycle | [ARCHITECTURE.md](docs/ARCHITECTURE.md) before tracing code; follow its owner and framework links. |
| Write or revise any player-facing text, including Java strings, CSV text, labels and console messages | [DIALOGUE.md](docs/DIALOGUE.md) for production, Editor review, shared presentation and dialogue flow; full current [LORE.md](docs/LORE.md) before drafting for facts, knowledge limits, prose style and characterization. These requirements do not depend on storage format or subject matter. |
| Edit or debug rules rows, conditions, triggers, options or dialogue routing | [RULES.md](docs/RULES.md) for language, execution, project contracts and CSV validation; [RULES_AUTHORING.md](docs/RULES_AUTHORING.md) for the command, memory and replacement mechanisms involved. Add the text route above when wording changes. |
| Use, change or debug commands, mission calls, memory or text replacements, even in Java without a CSV edit | [RULES_AUTHORING.md](docs/RULES_AUTHORING.md), its relevant dictionary entries and [RULES.md project routing](docs/RULES.md#project-routing). This applies to existing mechanisms, not only new plugins or keys. |
| Find an existing vanilla command, fact, flag or text token | [COMMANDS.md](docs/rules-reference/COMMANDS.md) for command recipes/classes; [MEMORY.md](docs/rules-reference/MEMORY.md) for reusable facts and replacements; [KEY_USAGE.md](docs/rules-reference/KEY_USAGE.md) for literal-key call sites. Read each dictionary's scope notes, then search the relevant entry and inspect its source/context. |
| Investigate rules-engine behavior | [RULES.md](docs/RULES.md) and the [source corrections](docs/RULES_AUTHORING.md#corrections-to-the-preserved-simulator-references), then relevant parts of the preserved [engine workflow](docs/rules/engine_workflow.md) and [simulator command table](docs/rules/command_table.md). Apply [Technical references and conflicts](#technical-references-and-conflicts); verify uncertain behavior against the exact game's source. |
| Change, fix or restructure quest code, quest state or quest dialogue: the Kesteven questline, contracts or bounties | [Quest implementation](docs/quests/README.md) and the page for that quest, after `ARCHITECTURE.md`. Add the rules route for rules rows and the text route for wording. The quest pages describe current behavior; they do not govern lore or rules syntax. |
| Implement Java custom panels, widgets, renderers, tooltips, layout or input | [UI.md](docs/UI.md) and the owner in `ARCHITECTURE.md`. Add `DIALOGUE.md` for text work and the rules route for rules-hosted interaction changes. `UI.md` is not a second rules-dialogue standard. |
| Edit a guide or add documentation | [Documentation upkeep](#documentation-upkeep), the owning guide and its inbound links. Documentation editing and technical-only routing changes without prose changes do not require an Editor pass. |

### Technical references and conflicts

`RULES.md` is the language and project-contract reference; `RULES_AUTHORING.md` is the implementation guide for commands, memory and replacements. Use them together. Short examples in `RULES.md` introduce the mechanisms; the full integration procedures belong in `RULES_AUTHORING.md`. Neither replaces the text, lore or UI guide in that guide's own subject area.

The files under `docs/rules/` are preserved references for an external simulator. Their claims of being normative apply to that tool. For the listed discrepancies, follow the source-verified corrections in `RULES_AUTHORING.md` for the named game build. This does not certify unlisted claims. Resolve any other technical conflict against the exact source and record the evidence; do not pick a winner by filename, recency or an authority label.

The dictionaries distinguish checked recipes from extracted names, expressions and call sites. A catalogue entry is not a complete behavioral audit of a plugin or proof that a key is available in every context. Preserve those limits when quoting or updating it.

### Make and record changes

- Use one task branch for the user's message.
- Make one commit per requested change. If one message contains several changes, commit them separately in the requested order.
- Keep each commit message to one short, plain-English summary. Do not include testing notes, agent or model names, co-author trailers, session metadata, links, formatting, or session URLs.
- Update the affected documentation in the same commit, following [Documentation upkeep](#documentation-upkeep). Do this without a separate user request.
- Add a `Changelog.txt` entry for every change players can notice, in the same commit, under the top (next-release) section. Use the existing forms `Added -`, `Adjusted -`, `Fixed -` and `Removed -`, one line per entry, in player terms. Refactors, documentation, tools and build setup get no entry.
- Work only in the current task checkout. A live mod installation, including any copy under a Starsector `mods` folder, and every unrelated checkout are read-only unless the user explicitly asks you to change them.
- Do not deploy or synchronize the mod after merge unless the user explicitly requests it.
- Keep all Java tools and automated checks under `jars/src`, in the normal `Lost.Sector` module. Do not add separate test source trees or duplicate game classes. Tools may read existing game constants and types; do not change runtime classes to support them.
- Prefer listeners and existing callbacks over `EveryFrameScript` polling. Before adding or retaining a polling solution for the task, verify against the exact game source that no existing callback covers the requirement. Document the missing coverage and keep the scan scope and frequency to what the behavior needs. Run while paused only when that behavior requires it.

### Validate and merge

- Every runtime-affecting final branch must pass the full clean Java 17 build described in [Building](#building).
- Build the exact final remote task-branch revision. Earlier builds, partial builds, IDE analysis, and static checks do not satisfy the gate.
- Missing compilers or dependencies are merge blockers. Documentation-only changes do not require a Java build.
- Open a pull request and merge it when the work is complete. Do not leave finished work on an unmerged branch.
- Use the connected GitHub app to create, inspect, and merge pull requests. Use `gh` only if the app is unavailable. A broken `gh` login is not a blocker when the app works.
- Fetch current remote `main` before branching and again before merging. Integrate intervening changes, push the exact final commit, and verify the merged remote revision.

### Branch cleanup

GitHub normally deletes a pull request's head branch after merge because automatic branch deletion is enabled.

Verify that the branch is gone. Manually remove it when it was pushed without a pull request, its pull request was closed without merging, or it predates the repository setting. If deletion returns HTTP 403, report the branch name and leave it in place; do not work around a credential boundary.

After merge, fast-forward the local task branch to the new `main`. The local task branch may keep its assigned name.

## Documentation upkeep

Update documents automatically as part of each relevant change, not by a background job. Before committing, check which entries the diff makes stale and update those entries in that commit.

| Document | Owns | Update when / how |
|---|---|---|
| `Changelog.txt` | Player-facing change list for each release | Add one entry per user-affecting change in the commit that makes it, under the top (next-release) section. Do not rename released sections. |
| `AGENTS.md` | Codex discovery | Keep it a short pointer to this file. Do not copy workflow policy into it. |
| `CLAUDE.md` | Task scope, subagent instructions, tools, commits, PRs, builds, comments, document maintenance | Replace changed policies here and update links. Keep provider-specific instructions explicitly scoped. |
| `docs/ARCHITECTURE.md` | Technical routing: owners, registrations, data flow, lifecycle and cross-system constraints | Update the affected route or contract; remove obsolete owners. Use exact paths and symbols. No lore, writing advice, release history, or duplicate policy. |
| `docs/quests/*.md` | Current quest implementation: flow, gates, stages, saved state, dialogue locations and defects found in the source | Update with any change to quest code, quest rules rows or quest keys. Remove a defect entry in the commit that fixes it. Record behavior, not design proposals or lore. |
| `docs/UI.md` | Java-bound custom UI: panels, widgets, renderers, sprites, tooltips, lifetime, layout and input | Update implementation contracts and source evidence. Link to `DIALOGUE.md` for shared text guidelines; do not put rules-dialogue guidance here. |
| `docs/DIALOGUE.md` | All player-facing text, including rules and Java: Editor procedure, prose review, shared text presentation and dialogue usability | Keep shared guidelines content-agnostic and label subject-specific additions. Link to lore, Java UI implementation and technical rules instead of copying their explanations. |
| `docs/LORE.md` | Setting definitions, absolute knowledge limits, prose style, character instructions, examples, terminology and information-release order | Update only for approved fiction/characterization changes or requested meaning-preserving edits. Define concepts before using them, or link to their definitions. Preserve the force of facts and prohibitions, their reasons, and useful correct/incorrect examples. Do not turn invented dialogue into canon. |
| `docs/RULES.md` | Rules language, execution, project routing contracts and CSV validation | Update verified contracts and local links. Keep syntax and introductory examples accurate; link to the authoring guide for full command/key integration procedures. |
| `docs/RULES_AUTHORING.md` | Command/key implementation, memory lifetime, replacement preparation, Java integration and source corrections | Check exact source and CSV usage when updating. Keep full procedures here and align related summaries in `RULES.md`; reuse existing mechanisms before adding new ones. |
| `docs/rules-reference/*.md` | Vanilla lookup dictionaries: recipes, source expressions, classes and call sites | Retain game version, provenance and scope limits. Distinguish checked behavior from extracted inventory; link usage procedures to the authoring guide instead of maintaining a second manual. |
| `docs/rules/*.md` | Preserved external simulator references | Preserve upstream text and provenance. Record source-verified corrections in `docs/RULES_AUTHORING.md`'s correction section and update affected `RULES.md` summaries/links, rather than silently altering the reference. |
| Framework `README.md` files (none yet) | Integration and extension instructions for that framework | Update when its API, registration, dependencies, paths or lifecycle changes. Add a routing row to [Which guide to read](#which-guide-to-read) when the first one is created. |

Give each fact one home. Move useful detail to its proper owner and link to it; do not keep a second copy in architecture. After moving a section, repair inbound links and check that no requirement or technical constraint was lost. Keep architecture compact and readable; do not create a parallel human version to maintain.

For a new or renamed guide, update the required-reading table, this ownership table and relevant architecture/guide links in the same commit. State when to read it and what it does not govern. Keep `AGENTS.md` as the entry pointer, not a duplicate index. During navigation or wording cleanup, preserve technical bodies and examples; any intended technical correction needs separate source evidence and an explicit review of the changed contract.

Write documentation in ordinary English. Use direct verbs and concrete nouns; remove slogans, inflated claims, repeated contrasts, and commentary about how important the document is. Keep identifiers exact. Tables and short technical fragments are appropriate in architecture. Examples demonstrate a technique, not a sentence pattern to repeat everywhere. Preserve the distinction between absolute restrictions and defaults such as “normally” or “prefer.” For lore voice examples, quote useful existing mod exchanges where possible, identify their rule IDs and context, and explain what they demonstrate. Recheck those excerpts when their source changes; quoting a row does not approve its entire dialogue tree.

`LORE.md` is an explicit author reference, not player-facing fiction. Never apply the mod's ambiguity, understatement or omission of explanations to the document itself. Distinguish established facts, character knowledge, narration restrictions and deliberately unanswered questions. State absolutes directly; do not replace “cannot” or “never” with a preference to “avoid.” Explain what to write, what not to write, why the distinction exists and how to apply it. Shortening must not remove those instructions or examples. After reorganizing lore, compare the old and new versions for facts, prohibition strength, exceptions, examples and definition order; word retention alone is not a sufficient check. Keep text-production workflow in `DIALOGUE.md` and link to lore for prose and characterization rules.

## Comments and documentation

- Comment constraints that code cannot express: engine quirks, required ordering, lifecycle rules, save compatibility, serialization, reflection, obfuscation, units, invariants, non-obvious math, and cross-file coupling.
- Do not comment visible operations. Prefer clear names and structure.
- Do not add Javadoc to self-explanatory classes, methods, fields, parameters, or return values.
- Keep necessary comments short and direct. Comments are not tutorials, release notes, bug histories, or implementation narratives.
- Delete commented-out code.
- Keep a TODO only when it names a concrete unfinished action. Include the blocker when it is not obvious.
- Documentation records current architecture, contracts, entry points, and live hazards. It must not become a development diary, pull-request history, or postmortem.
- Give each fact one home. `docs/ARCHITECTURE.md` is the compact file and feature map; broader stable explanations belong in focused documents.
- Update or remove stale comments and documentation in the same commit as the behavior change.

## Java class layout

- Organize members for reading, not alphabetical sorting.
- Default order:
  1. nested enums;
  2. static fields and initializers;
  3. instance fields and initializers;
  4. other nested types;
  5. constructors;
  6. lifecycle and public entry points in execution or usage order;
  7. supporting helpers near their callers.
- Preserve initializer order when moving code could change behavior.
- Keep a cohesive subsystem together when strict declaration grouping would make the class harder to follow.
- Group fields by responsibility. Put one blank line between groups and no blank lines inside one group.
- Leave one blank line after a class, enum, interface, or record opening brace.
- Use one blank line between methods and major sections. Do not stack empty lines.
- Use short section headers such as `// Rendering` only when names and ordering do not make a long class's groups clear.

## Codex and ChatGPT app tools

These tool choices apply to Codex and ChatGPT app sessions. They do not select a model or alter Claude's assignments.

### Local and GitHub work

- Use local and pinned GPTs directly through the ChatGPT app. Do not open `chatgpt.com` or use the browser to reach them.
- Use one dedicated local task checkout for clone, fetch, branch, edit, build, commit, integration, and push operations.
- Use local Git for source work and history. Use the connected GitHub app for pull requests, merges, and remote-only fallback when the local filesystem or Git state is unreliable.
- Do not use the browser for repository work.
- If one user message contains several tasks, commit each task separately on the same branch and open one pull request after all of them are complete.
- Compile from a clean output directory in the task checkout. Never compile from or write into a live mod installation.

### Filesystem validation

Before trusting a new or suspect filesystem:

1. Clone current remote `main` into a disposable directory.
2. Confirm its HEAD matches the remote commit.
3. Require a clean `git status`.
4. Run `git fsck --full`.
5. Compare one repository file with its remote blob.
6. Test write, read, and delete in the clone.
7. Create and switch a local branch.
8. Resolve and verify the disposable path before removing it.

If any check fails, keep local PC checkouts read-only and use the GitHub-app-only workflow until the cause is fixed.

## Agent behavior

- Keep the user's selected model. In Codex/Astra sessions, the main agent owns planning, integration, and shipped code. Delegate bounded research only when the user or applicable instructions authorize it and the app supports it. Do not copy Claude-specific model names into Codex calls.
- Treat repository instructions as project requirements and skill guidance as scoped reference material. Follow explicit user changes within the app's safety and permission limits. If a skill blocks work or changes its scope, identify the exact instruction and explain the effect.
- Give brief progress updates and concise final results. Put detailed evidence in the pull request; avoid repeating the whole implementation narrative in chat.

### Subagent dispatch template

Use this template for authorized subagent work, including programming support, crawling, audits, questions and research. It applies across models. The main agent fills in the brief and sets the selected model and reasoning effort in the dispatch call. Include relevant user clarifications explicitly; do not assume the subagent received the conversation. Keep the permitted work within the ownership and model assignments above and below.

```text
Task: <specific question or result, and what counts as complete>
Checkout: <absolute path and revision; note relevant uncommitted changes>
Context: <user clarifications, constraints and work already completed>
Access: <allowed sources; permitted writes and paths, if any>
Starting points: <relevant guides, files or symbols, if known>
Return: <desired detail, format and output path, if needed>

1. Read AGENTS.md and CLAUDE.md, then follow the required-reading routes for
   this task. Starting points are navigation help, not the full reading list.
   Follow required skills and linked prerequisites. Read full documents where
   required; otherwise read the relevant sections and their context.
2. Use searches to locate material. Open the surrounding section before using
   a match as evidence. Read definitions, scope notes, exceptions and warnings;
   do not answer from filenames, headings or search snippets alone.
3. Preserve what each passage applies to: game version, vanilla or mod code,
   current implementation, example, deprecated code or external tool. An account
   of an old mistake is not a current instruction to repeat it. Keep qualifiers
   such as "normally", "only", "never" and "not" when summarizing.
4. Use lookup tables to find relevant entries and follow their source links.
   An entry's presence does not prove it is usable in every context. Do not read
   or count an entire lookup table unless the task requires that coverage.
5. For implementation work or behavioral claims, follow the documentation to
   the relevant code, callers, registration and data within the allowed sources.
   Check the actual version and applicable source corrections. Do not invent
   APIs, names or behavior to fill gaps. If access is documentation-only, say
   what the guide describes and leave implementation verification to the parent.
6. Distinguish documented facts, source-verified behavior and your inferences.
   Before claiming a contradiction, cite what both passages actually say and
   check whether they concern the same version and situation. If evidence is
   missing or still conflicts, identify the uncertainty instead of guessing.
7. Return the requested result with file/line references or source URLs for
   material claims, plus checks performed and remaining questions. For searches
   or crawls, state the scope searched; "not found there" does not prove absence
   everywhere. Do not invent findings or expand the task to fill the report.
```

The main agent checks the cited evidence before using a conclusion to change code or documentation. A subagent's report does not replace the parent's integration review or the required validation.

## Claude model assignments

This section applies to Claude sessions, not ChatGPT.

Opus 5 in the main session owns planning and shipped code. Clear names and structure carry ordinary intent; comments remain limited to hidden constraints.

| Work | Model |
|---|---|
| Planning, architecture, sequencing, and all Java changes | Opus 5 in the main session |
| Repository search, call tracing, and reading game sources | Sonnet 5 subagents using `model: sonnet` |
| UI and UI-adjacent work, including panels, dialogs, tooltips, renderers, shaders, and sprites | Fable 5 subagents using `model: fable` |

Subagents perform research and scoping, not shipped code. The UI assignment takes precedence whenever the player can see the result.

## Version names

`mod_info.json` `version`, the `modVersion` in `lostsector.version` and the release tag in its `directDownloadURL` name the released version, `1.0.b`. The release version uses a letter patch (`1.0.a`, `1.0.b`). The top `Changelog.txt` section, `1.0.c`, collects entries for the next release; the user sets the version files when publishing it.

Version Checker reads `lostsector.version` and `Changelog.txt` from `main` through `masterVersionFile` and `changelogURL`. Merging a change to those files publishes it to players. Do not change version names or release metadata unless the user asks.

## Building

### Required compile gate

After the final runtime-affecting commit and before merge:

1. Fetch and build the exact remote task-branch revision.
2. Use a clean, empty output directory.
3. Compile every `.java` file under `jars/src` with Java 17 and the complete dependency set below.
4. Require a successful exit with no compile errors.
5. Record the command, branch commit, Java version, and result in the pull request. The final reply links the PR and states the result and any untested behavior.
6. Stop before merge if the build or any dependency is unavailable.

Documentation-only changes are exempt. Changes to runtime data, including text-only CSV changes, still require this gate unless the user explicitly grants an exemption.

Run the checks appropriate to the affected behavior as well as the required build. After they pass, repeat or broaden checks only for changed inputs, failures, or a concrete unresolved concern. Do not add tests that merely restate the implementation.

The project uses Java 17; `.idea/misc.xml` sets the language level. IntelliJ compiles into `jars/production` and builds the `jars/Lost.Sector.jar` artifact named in `mod_info.json`; both are ignored by Git.

Required compile dependencies:

- `lib/starfarer.api.jar`, and `starfarer_obf.jar` from the game's `starsector-core` directory;
- `Graphics.jar` from `lib/GraphicsLib.zip`;
- `LazyLib.jar` and `jars/internal/Kotlin-Runtime.jar` from `lib/Lazylib_lunalib.zip`; the Kotlin runtime supplies the `org.jetbrains.annotations` classes used in `jars/src`;
- `MagicLib.jar` from `lib/MagicLib.zip`;
- `LunaLib.jar` from `lib/Lazylib_lunalib.zip`;
- `lib/ExerelinCore.jar` and `lib/IndEvo.jar`, used only by code behind the optional-integration flags;
- `lwjgl-2.9.3.jar`;
- `lwjgl_util-2.9.3.jar`;
- `json-20140107.jar`;
- `log4j-1.2.17.jar`.

The last four ship with the game in `starsector-core`; the same versions from Maven Central compile identically when no game install is available. These dependencies provide `org.lwjgl.util.vector`, `org.lwjgl.opengl`, `org.json`, and `Global.getLogger()`. LazyLib, MagicLib and GraphicsLib are the declared runtime dependencies in `mod_info.json`. LunaLib, Nexerelin and Industrial.Evolution are compile-time requirements for their integration classes; [ARCHITECTURE.md](docs/ARCHITECTURE.md) records how each is gated at runtime.

Reference command:

```sh
javac --release 17 -cp "<all jars above>" -d <empty-output> $(find jars/src -name '*.java')
```

## Repository navigation

After the required read of this file, follow [Which guide to read](#which-guide-to-read). [ARCHITECTURE.md](docs/ARCHITECTURE.md) is the code/data map, not a substitute for the task-specific guides.
