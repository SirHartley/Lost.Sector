# Quest framework

`lostsector.quest` runs every quest in Lost.Sector: the Kesteven questline, Hellspawn, blacksites, bounties and the other encounters. It owns quest state, stage changes, game events, quest fleets, quest people, quest intel, dialog entry points, display tokens and the `nskr_quest` rules command. Quest text lives in `data/campaign/rules.csv`; quest logic lives in modules that plug into this framework.

Read this file in full before changing quest code, quest rules rows, quest intel, quest bar events or quest fleets, and again after a context compaction. It is also the specification for the framework classes: while the framework is being built, a component marked *planned* in [Status](#status) does not exist yet.

This guide does not govern:

| Subject | Guide |
|---|---|
| Rules language, execution, CSV validation | [RULES.md](../../../../docs/RULES.md) |
| How to structure rules conversations: chains, `FireAll`, `FireBest`, conditions, options, exits, layout | [RULES_WRITING.md](../../../../docs/RULES_WRITING.md) |
| Vanilla commands, memory keys and text replacement mechanisms | [RULES_AUTHORING.md](../../../../docs/RULES_AUTHORING.md) and the [dictionaries](../../../../docs/rules-reference/) |
| Wording, voice and lore | [DIALOGUE.md](../../../../docs/DIALOGUE.md), [LORE.md](../../../../docs/LORE.md) |
| What each quest does: flow, gates, stages, defects | [docs/quests](../../../../docs/quests/README.md) |
| Custom Java panels | [UI.md](../../../../docs/UI.md) |
| Where other features live | [ARCHITECTURE.md](../../../../docs/ARCHITECTURE.md) |

## Contents

- [Status](#status)
- [Rules for every change](#rules-for-every-change)
- [Concepts](#concepts)
- [Package layout](#package-layout)
- [Lifecycle](#lifecycle)
- [Making a quest](#making-a-quest)
- [Reference](#reference): [Quest and stages](#quest-and-stages), [QuestState](#queststate), [QuestModule](#questmodule), [Declarations](#declarations), [QuestContext](#questcontext), [QuestManager](#questmanager), [Queries from other features](#queries-from-other-features), [Events](#events), [Fleets](#fleets), [People](#people), [Dialog entry points](#dialog-entry-points), [Bar events](#bar-events), [Intel](#intel), [Tokens](#tokens), [Rewards and receipts](#rewards-and-receipts), [Random, timers and marks](#random-timers-and-marks), [The quest command](#the-quest-command), [Dev tools](#dev-tools), [Rules check tool](#rules-check-tool)
- [Rules contract](#rules-contract)
- [Shared modules](#shared-modules)
- [Save compatibility](#save-compatibility)
- [Errors and logging](#errors-and-logging)
- [Extending the framework](#extending-the-framework)
- [What the framework replaces](#what-the-framework-replaces)
- [Outside the framework](#outside-the-framework)
- [Maintenance](#maintenance)

## Status

The framework is built on branch `quest-overhaul`; the task ids refer to the tracker pull request named in `CLAUDE.md`. A task that implements a component changes its row to *implemented* in the same commit and corrects any signature in this file that the code had to change.

| Component | Classes | Status | Task |
|---|---|---|---|
| Core: definitions, state, store, manager, transitions, marks, claims | `Quest`, `QuestStage`, `QuestState`, `QuestModule`, `Declarations`, `QuestContext`, `QuestManager`, `QuestStore`, `QuestCatalog`, `Quests`, `QuestDialogs`, `NoFlags` | implemented | T05 |
| Events: listeners, daily tick, frame hook | `QuestManager` | implemented | T06 |
| Fleets | `QuestFleets`, `QuestFleet`, `FleetRole`, `FleetOrders` | implemented | T07 |
| Rules command, tokens, people, rewards | `nskr_quest`, `QuestVerbs`, `QuestTokens`, `QuestPeople`, `QuestRewards` | implemented | T08 |
| Presentation spec (vanilla commands per effect) | [DIALOGUE.md](../../../../docs/DIALOGUE.md#presentation-in-rules) | implemented | T09 |
| Gap verbs: `confirm`, `engage` | `nskr_quest` | implemented | T10 |
| Intel and rules text outside dialogs | `QuestIntel`, `QuestIntels`, `QuestText` | implemented | T11 |
| Rules check tool | `lostsector.quest.dev.RulesCheck` | implemented | T12 |
| Dev menu and stage jumps | `nskr_questDev`, `QuestDevTools` | implemented | T13 |
| Shared modules | `lostsector.quest.modules` | `InterceptEncounter` and `PayOffEncounter` implemented; the others planned | T37 to T41 |

Until a component is implemented, do not write code against it and do not write a substitute. Implement it in its task, or stop and report.

## Rules for every change

These apply to the main session and to every subagent.

1. **Use the framework for everything it covers.** The table lists the only allowed way to do each job. Writing a second way is a defect, even when it is shorter.

   | Need | Use | Never |
   |---|---|---|
   | Store quest progress, targets, decisions, counters | Fields on the quest's `QuestState` subclass | `Saved<T>` fields, persistent data keys, `$global` keys, static fields |
   | Change the stage | `ctx.advance(...)` in Java, `nskr_quest <q> advance FROM TO` in rules | Assigning `state.stage`, stage numbers in memory |
   | React to game events | A [module hook](#questmodule) | A new `EveryFrameScript`, a new listener class, polling in intel `advanceImpl` |
   | Do something every day | `onDay` | Counting frames or seconds, `CampaignTimer` |
   | Wait a number of days | A [timer](#random-timers-and-marks) checked when needed | A counter advanced every frame |
   | Random numbers | `ctx.random(purpose)` | `new Random(...)`, `Math.random()`, `Misc.random` |
   | Spawn or track a quest fleet | `ctx.fleets().spawn(role, simpleFleet)` | Adding to `FleetHelper.getFleets` lists by hand, new fleet array keys |
   | Give a fleet orders | A [role](#fleets) with `FleetOrders` | Per-fleet AI code in a quest |
   | Create a quest person | `ctx.people().create(...)` | `createRandomPerson` in a dialog class, unregistered persons |
   | Show quest intel | `ctx.intel()` and [intel rows](#intel) | A `BaseIntelPlugin` subclass per job |
   | Open a rules dialog on an entity | `ctx.claimDialog(entity, trigger)` | A new `CorePlugin` branch |
   | Open a rules dialog from code | `ctx.open(target, trigger)` | `showInteractionDialog` calls in quest code |
   | Show a bar event | An `AddBarEvents` row with `AddBarEvent` | `PortsideBarData.addEvent`, `BaseBarEvent` subclasses, `nskr_barEventFixer` |
   | Mark an entity or person on the map | `ctx.mark(...)` | `Misc.makeImportant` without clearing it |
   | Show a computed value in text | A declared [token](#tokens) | Writing memory keys before rows, formatting in rows |
   | Let rules read quest state | A declared check, `is`, `reached` or `flag` | Memory keys mirroring Java state |
   | Let rules trigger quest logic | A declared action | A new command class per quest |
   | Grant a computed reward | `ctx.rewards()` inside an action | Hand-written receipt text |
   | Player-facing text | Rules rows | Java string literals, `strings.json`, `addPara` with prose |
   | Behavior two quests share | A [shared module](#shared-modules) | Copying a module |

   One exception is recorded: a visual effect bound to one open dialog, which must run while the game is paused, may use a transient script that stops when that dialog closes (`HellSpawnThrnAnimation`, the animated THRN portrait). Anything else that needs paused frames extends the framework first.

2. **Extend, never work around.** When the framework lacks something a quest needs, add it to the framework package following [Extending the framework](#extending-the-framework) and document it here in the same commit. A quest-local helper that duplicates or bypasses a framework job is not allowed.

3. **Use only what is documented or verified.** In Java, rules rows and conditions, use only APIs, commands, operators, triggers and memory keys that this file, the rules guides or the dictionaries describe, or that you have read in the exact game source (the `starsector-knowledge` skill, or `lib/` for dependencies). When you verify something new, add it to the guide that owns it in the same commit. Do not write a command, trigger, key or method from memory or by analogy.

4. **Stop instead of guessing.** If a needed fact is missing or two sources conflict, stop and report what is missing. A subagent returns the question to the main session; it does not choose.

5. **Rules text stays in rules.** Java never prints prose. The only Java output in a dialog is a vanilla-style receipt line from `ctx.rewards()` or a vanilla helper ([Receipts](../../../../docs/RULES_WRITING.md#receipts)).

## Concepts

- **Quest.** A definition class, rebuilt on every game load and never saved. It names the quest id, the stage enum, the flag enum, the start stage, the state class and the list of modules.
- **Stage.** One constant of the quest's stage enum. Each stage names the stage before it, which gives the path used by stage jumps. A quest is always in exactly one stage.
- **State.** One saved object per quest holding its stage, flags, timers, randoms, people, marks and the quest's own data fields. All quest states live in one saved store.
- **Module.** A class that does the work for a set of stages: world setup when the stages start, event handling while they run, cleanup when they end, and the checks, actions, tokens and fleet roles it declares.
- **Context.** The object every hook, check, action and token receives. It gives typed access to the state and to all framework services, and carries the dialog when there is one.
- **Manager.** The one transient `QuestManager`. It builds the definitions, creates missing states, is the only writer of stages, routes events to active modules and runs the fleet orders.

Two shapes of quest use the same classes:

| Shape | Stages | Instance data | Examples |
|---|---|---|---|
| Story quest | A stage enum with the whole story; modules per chapter | Fields on the state | Kesteven questline (`kq`), Hellspawn (`hs`) |
| Record quest | One running stage (plus a terminal stage if needed) | A map of records on the state, one per site, bounty or encounter kind; each record has its own status | Blacksites, bounties, intercepts |

A record quest routes events to records through the fleet's record id or the dialog target. Use it when several instances follow the same rules; use a story quest when the content is one sequence.

## Package layout

```
lostsector/quest/                 framework: definitions, state, manager, services, dev menu support
lostsector/quest/modules/         shared modules used by several quests
lostsector/quest/dev/             RulesCheck and dev support
lostsector/dialogue/rules/        nskr_quest and nskr_questDev commands
lostsector/campaign/<feature>/    one package per quest: <Name>Quest, <Name>Stage,
                                  <Name>Flag, <Name>State, modules and content builders
```

A quest package holds only content: its definition, stage and flag enums, state, modules, and builders for its fleets and entities. Anything a second quest could use belongs in the framework or in a shared module.

## Lifecycle

### Registration

| When | What | Where |
|---|---|---|
| `ModPlugin.createManagers()` | `EFS_LIST.add(new QuestManager())`, like every other transient manager. The existing `onGameLoad` loop registers it as transient script, transient campaign listener and transient listener-manager listener, and `beforeGameSave` removes it. | `ModPlugin.java` |
| `ModPlugin.onGameLoad` | `Global.getSector().getRules().addTokenReplacementGenerator(new QuestTokens())`. Generators are not saved; vanilla adds its own on every load (`CoreLifecyclePluginImpl`, source comment "the token replacement generators don't get saved"). | `ModPlugin.java`, next to `registerPlugin(new CorePlugin())` |
| `ModPlugin.onGameLoad`, last line | `QuestManager.get().startQuests()` creates missing states ([Load](#load)). | `ModPlugin.java` |
| `CorePlugin.pickInteractionDialogPlugin` | One route for [claimed entities](#dialog-entry-points). | `CorePlugin.java` |
| `FleetHelper.FLEET_ARRAY_KEYS` | `QuestFleets.KEY`, so `hackBrokenVariants()` repairs quest fleets. | `FleetHelper.java` |

No other registration exists. A quest never registers scripts, listeners or plugins itself.

### Load

1. `QuestManager` is constructed in `createManagers()`. Its constructor creates the store handle (`Saved<QuestStore>` under key `quests`, stored as `nskr_quests`) and builds every definition from `QuestCatalog`. Building definitions calls no game API.
2. `Saved.loadPersistentData()` loads the store.
3. At the end of `ModPlugin.onGameLoad`, `QuestManager.startQuests()` calls `isAvailable()` on each quest. For an available quest without state it creates the state, puts it in the start stage and calls `onStart` on the start stage's modules (not `onStage`). States are never deleted; an unavailable quest keeps its state but receives no events.

   The world is complete at that point: vanilla calls `onGameLoad(true)` after every mod's `onNewGame*` hooks (`sources-obf/campaign.save.java` 606-609), and `ModPlugin.onGameLoad` generates the mod's world itself, before this call, when the mod is added to an existing save. States therefore exist before the first frame, so scripts that run while paused (`QuestStageManager`) and dialogs opened before the game is unpaused find them. `onStart` hooks run during the load: a dialog they open with `ctx.open` waits in the pending list while the UI is busy. The manager itself does not run while paused: `runWhilePaused()` is false, as for vanilla's wait script.

### A stage change

`advance` from Java or rules runs these steps, in order, for a change from stage `X` to stage `Y`:

1. `onStop` on every module active in `X` and not in `Y`, in reverse module order.
2. Marks and claims whose scope does not include `Y` are cleared; fleets of roles owned by stopped modules are despawned unless the role is `persistent()`.
3. The state records `Y`, the change time and `Y` in its reached set. The manager logs `[<q>] X -> Y (<source>)`.
4. `onStart` on every module active in `Y` and not in `X`, in module order.
5. `onStage(ctx, X)` on every module active in `Y`, in module order.

A module that calls `advance` inside any of these hooks queues the change; the manager applies it after the current change completes. A queued guarded change is checked again when applied and skipped with an error if the stage has moved. More than 20 queued changes in a row is a loop: the manager logs an error and drops the rest. An `advance` to the current stage logs an error and changes nothing. Exceptions thrown by quest hooks are not caught, so bugs surface; the manager resets its own flags in `finally`.

### A stage jump

`QuestManager.jump(quest, target)` serves the dev menu and the player's story skip. It walks from the current stage to the target along the `previous()` chain of the target. For the current stage and each stage on the path except the target, it calls `onSkip(ctx)` on the modules active in that stage, which set what the stage's conversations would have set (default decisions, rewards the story assumes), then performs a normal stage change to the next stage on the path. Advances queued by hooks during the walk are dropped and logged, because the jump decides the path; advances queued by the target stage's hooks apply after the jump. `ctx.isJump()` is true during the jump.

A target is ahead when the current stage lies on its `previous()` chain. A jump to any other target, including the current stage, resets the quest first: every module active in the current stage stops in reverse order, marks, claims, pending opens, fleets and people of the quest are removed, its intel entries end at once, and a fresh state starts at the start stage. If the start stage is not on the target's chain (a stage such as `FAILED` whose `previous()` is null), the reset quest changes straight to the target.

### Save and reload

Only the store is saved, through `Saved`. Definitions, the manager and all lambdas are rebuilt on load. Because the manager is transient, nothing in the framework needs `readResolve` except state classes that gain fields after a release ([Save compatibility](#save-compatibility)).

## Making a quest

This is the complete procedure. Each step names the file it produces. The example quest `ex` is illustrative; it does not exist in the code.

1. **Read the quest page and plan the stages.** Write the stage list, the flags (decisions Java needs), the timers, the fleets and roles, the people, the intel entries and the dialog entry points on the quest's page in `docs/quests/` first. Every later step implements that page.

2. **Stage enum** (`ExStage.java`). One constant per stage in story order; each names its predecessor. The start stage and stages reachable from anywhere (such as a failure) use `null`.

   ```java
   public enum ExStage implements QuestStage {
       NOT_STARTED(null),
       OFFERED(NOT_STARTED),
       ACTIVE(OFFERED),
       DONE(ACTIVE),
       FAILED(null);

       private final ExStage previous;

       ExStage(ExStage previous) {
           this.previous = previous;
       }

       @Override
       public ExStage previous() {
           return previous;
       }
   }
   ```

3. **Flag enum** (`ExFlag.java`). One constant per decision that Java or several rows need. Conversation flags about one speaker ("already asked") are not flags; they go on the speaker ([Rules contract](#rules-contract)).

   ```java
   public enum ExFlag { REFUSED_ONCE, SPARED_CAPTAIN }
   ```

4. **State** (`ExState.java`). Package-private fields for the quest's data. Targets are picked when their stage starts and stored here.

   ```java
   public final class ExState extends QuestState<ExStage> {

       SectorEntityToken target;
       int payout;
   }
   ```

5. **Modules** (`ExOfferModule.java`, `ExHuntModule.java`). One module per chapter. The constructor passes the stages the module is active in; `declare` lists its checks, actions, tokens, roles and Java-fired triggers. The offer module picks the target when the offer stage starts, because the offer text already names it and a token is computed when its row is shown.

   ```java
   final class ExOfferModule extends QuestModule<ExStage, ExState> {

       static final int PAYOUT = 40000;

       ExOfferModule() {
           super(ExStage.OFFERED);
       }

       @Override
       protected void declare(Declarations<ExStage, ExState> d) {
           d.token("targetName", ctx -> ctx.state().target == null ? "" : ctx.state().target.getName());
           d.token("payout", ctx -> Misc.getDGSCredits(ctx.state().payout));
       }

       @Override
       protected void onStart(QuestContext<ExStage, ExState> ctx) {
           ExState s = ctx.state();
           s.target = ExTargets.pick(ctx.random("target"));
           s.payout = PAYOUT;
       }
   }

   final class ExHuntModule extends QuestModule<ExStage, ExState> {

       static final String ROLE_TARGET = "target";

       ExHuntModule() {
           super(ExStage.ACTIVE);
       }

       @Override
       protected void declare(Declarations<ExStage, ExState> d) {
           d.role(ROLE_TARGET, FleetRole.of(FleetOrders.guard(GuardMovementBehaviour.ORBIT, GuardAttackBehaviour.PLAYER, 0.1f)));
           d.intel("hunt", "pk", Tags.INTEL_MISSIONS, Tags.INTEL_ACCEPTED);
       }

       @Override
       protected void onStart(QuestContext<ExStage, ExState> ctx) {
           ExState s = ctx.state();
           ctx.fleets().spawn(ROLE_TARGET, ExFleets.target(s.target, ctx.random("targetFleet")));
           ctx.mark(s.target);
           ctx.intel().show("hunt");
           ctx.intel().setMapLocation("hunt", s.target);
       }

       @Override
       protected void onFleetGone(QuestContext<ExStage, ExState> ctx, QuestFleet fleet, FleetDespawnReason reason, Object param) {
           if (fleet.isRole(ROLE_TARGET) && fleet.wasDestroyed(reason)) ctx.advance(ExStage.DONE);
       }
   }
   ```

   Tokens are declared by the module that sets their data, and read in any stage, so the payout token also works in the reward conversation of `DONE`.

6. **Definition** (`ExQuest.java`), and one line in `QuestCatalog`.

   ```java
   public final class ExQuest extends Quest<ExStage, ExState> {

       public static final String ID = "ex";

       public ExQuest() {
           super(ID, ExStage.class, ExFlag.class, ExStage.NOT_STARTED);
       }

       @Override
       protected ExState createState() {
           return new ExState();
       }

       @Override
       protected List<QuestModule<ExStage, ExState>> createModules() {
           return List.of(new ExOfferModule(), new ExHuntModule(), new ExRewardModule());
       }

       public static ExState state() {
           return Quests.state(ID);
       }
   }
   ```

7. **Fleet and entity builders** (`ExFleets.java`). Static methods that configure a `SimpleFleet` and return it unbuilt; `ctx.fleets().spawn` builds and registers it. Scale points with `Difficulty.scriptedFleetMult()` and, where the encounter scales with the player, `PowerLevel.get(...)`.

8. **Rows.** Write the conversations in `rules.csv` following [RULES_WRITING.md](../../../../docs/RULES_WRITING.md), under one `# EX` block. Conditions use `nskr_quest ex is ...`, `reached`, `flag` and `check`; scripts use `advance`, `set`, `clear` and `do`; text uses `$nskr_ex_<token>`. Intel text uses the [intel triggers](#intel).

   ```
   nskr_ex_captainOpen,PickGreeting,"$id == nskr_ex_captain
   nskr_quest ex is OFFERED",FireAll nskr_exCaptainOptions,"""There's a ship near $nskr_ex_targetName I want gone. $nskr_ex_payout. Interested?""",,
   nskr_ex_captainOptAccept,nskr_exCaptainOptions,nskr_quest ex is OFFERED,,,"10:nskr_ex_captainAccept:""I'll do it.""",
   nskr_ex_captainOptLeave,nskr_exCaptainOptions,,,,100:cutCommLink:Cut the comm link,
   nskr_ex_captainAccept,DialogOptionSelected,$option == nskr_ex_captainAccept,"nskr_quest ex advance OFFERED ACTIVE
   FireAll nskr_exCaptainOptions","""Good. Don't come back until it's done.""",,
   ```

   The accept row's Text shows before its Script runs, and the second `FireAll` rebuilds the menu without the accept option because the stage has changed.

9. **Dev support.** Implement `onSkip` on every module whose stages have conversations that set flags or state, and `devInfo` for anything the dev menu should show. Jump to every stage in dev mode and check the world matches.

10. **Check and document.** Run the [rules check tool](#rules-check-tool) and the build. Update the quest page in `docs/quests/`, `ARCHITECTURE.md` if an owner changed, and `Changelog.txt` for player-visible changes, in the same commit.

## Reference

Signatures are the contract. Generic bounds are written once here and omitted below: `S extends Enum<S> & QuestStage`, `T extends QuestState<S>`.

### Quest and stages

```java
public interface QuestStage {
    QuestStage previous();   // the stage before this one on the story path; null for the start stage and for stages reachable from anywhere
}

public abstract class Quest<S, T> {
    protected Quest(String id, Class<S> stages, Class<? extends Enum<?>> flags, S start);
    public final String id();
    public final Class<S> stages();
    public final Class<? extends Enum<?>> flags();
    public final S start();
    protected abstract T createState();
    protected abstract List<QuestModule<S, T>> createModules();
    protected boolean isAvailable();   // default true; checked once per load, at the end of ModPlugin.onGameLoad
}
```

- **Quest id.** Lowercase letters and digits, unique, no prefix of another quest id: `kq`, `hs`, `bs`, `bounty`. It appears in rule ids, triggers, memory keys and tokens.
- **Stage names.** Upper snake case. Declaration order is story order; the dev menu lists stages in that order. A quest without flags passes `NoFlags.class`, an empty enum in the framework.
- **Branches.** Endings and other branches are separate stages whose `previous()` names the stage they branch from. `reached` answers "did this happen" across branches; there is no ordinal comparison.
- **Failure.** A quest that can fail from several stages declares a `FAILED` stage with `previous()` null and moves there. Modules that should stop on failure simply do not list it.
- **Definitions are pure.** Constructors, `createModules()` and `declare()` must not call `Global` or any game API, because the rules check tool builds the definitions outside the game. That includes `Misc`, whose static initializer calls `Global.getSettings()`, and building a `RuleBasedInteractionDialogPluginImpl`, whose static initializer does too. Game calls belong in hooks and in the lambdas passed to `declare`, which run only in the game.
- **Load checks.** The `Quest` constructor throws unless the id matches `[a-z][a-z0-9]*`, the start stage's `previous()` is null, and every `previous()` chain stays inside the enum without a cycle. `QuestCatalog` throws when a quest id equals or prefixes another, equals a quest-independent verb of the [quest command](#the-quest-command) (`confirm`, `engage`), or two quests share a stage enum or a flag enum other than `NoFlags`.
- **`QuestCatalog`** lists every quest in a fixed order: `static List<Quest<?, ?>> create()`. The manager and the check tool both use it. Adding a quest is one line there.

### QuestState

```java
public abstract class QuestState<S> {
    public final S stage();
    public final boolean reached(S stage);
    public final float daysInStage();
    public final boolean has(Enum<?> flag);
    public final long seed();
}
```

The framework owns these fields and writes them only through the manager and the context:

| Field | Content |
|---|---|
| `stage`, `stageSince` | Current stage and the clock timestamp of the change |
| `reached` | Names of every stage entered |
| `flags` | Names of set flags; checked against the quest's flag enum |
| `timers` | Timer name to start timestamp |
| `seed`, `randoms` | `Misc.genRandomSeed()` at creation; one saved `java.util.Random` per purpose |
| `people` | Person key to `PersonAPI` for [quest people](#people) |
| `marks`, `claims` | [Marks](#random-timers-and-marks) and [dialog claims](#dialog-entry-points) with their stage scopes |
| `pendingOpens` | [Dialogs](#dialog-entry-points) waiting for the UI to be free |

The subclass adds the quest's own data as package-private fields: targets as `SectorEntityToken`, `MarketAPI` or `PersonAPI` references, counters, amounts and chosen options. A record quest keeps a `Map<String, Record>` of its own record class and shows it through `devInfo`. Rules:

- Only saved-safe types: primitives, `String`, enums, `java.util` collections, game references (entity, fleet, market, person, faction id strings), `java.util.Random`, plain classes of the quest's own package, and the record classes of the [shared modules](#shared-modules) the quest uses. No lambdas, anonymous classes, dialogs, UI objects or transient managers.
- Pick a value once, when the stage that needs it starts, and store it. Rows and tokens only read. An offer never rerolls because a screen was opened.
- Store amounts as numbers and format them in tokens.
- A field that only one conversation reads about its own speaker is not state; see [Rules contract](#rules-contract).
- Other packages read state only through the quest's public static queries.

### QuestModule

```java
public abstract class QuestModule<S, T> {
    @SafeVarargs protected QuestModule(S... stages);   // no stages: active in every stage
    protected void declare(Declarations<S, T> d);

    protected void onStart(QuestContext<S, T> ctx);
    protected void onStage(QuestContext<S, T> ctx, S from);
    protected void onSkip(QuestContext<S, T> ctx);
    protected void onStop(QuestContext<S, T> ctx);

    protected void onDay(QuestContext<S, T> ctx);
    protected boolean wantsFrames(QuestContext<S, T> ctx);   // default false
    protected void onFrame(QuestContext<S, T> ctx, float amount);
    protected void onLocationChanged(QuestContext<S, T> ctx, LocationAPI prev, LocationAPI curr);
    protected void onFleetGone(QuestContext<S, T> ctx, QuestFleet fleet, FleetDespawnReason reason, Object param);
    protected void onBattle(QuestContext<S, T> ctx, QuestFleet fleet, BattleAPI battle, CampaignFleetAPI primaryWinner);
    protected void onLoot(QuestContext<S, T> ctx, QuestFleet fleet, FleetEncounterContextPlugin plugin, CargoAPI loot);
    protected void onDecivilized(QuestContext<S, T> ctx, MarketAPI market, boolean fullyDestroyed);

    protected void devInfo(QuestContext<S, T> ctx, List<String> lines);
}
```

| Hook | Called | Use for |
|---|---|---|
| `onStart` | The module becomes active | Picking targets, spawning fleets, marks, claims, showing intel |
| `onStage` | Every stage entry while active, after `onStart` | Changes between two stages of the same module |
| `onSkip` | A jump passes over one of the module's stages | Setting what that stage's conversations would have set |
| `onStop` | The module becomes inactive | Ending intel, releasing people, anything not scoped automatically |
| `onDay` | Once per campaign day while active | Rolls, timeouts, slow checks |
| `wantsFrames` / `onFrame` | Every unpaused frame while `wantsFrames` is true | Short scripted sequences tied to the player's position, such as an arrival scene. Return true only while needed, such as while the player is in one system |
| `onLocationChanged` | The player changes location | Arrival triggers, first sightings |
| `onFleetGone` | A fleet of one of this quest's roles despawns | Victory, loss, escape |
| `onBattle` | A battle involving a fleet of this quest's roles | Partial defeats, player participation |
| `onLoot` | Loot is generated from an encounter with this quest's fleet | Adding quest items to loot |
| `onDecivilized` | Any colony is decivilized | Losing a quest location |
| `devInfo` | The dev menu shows the quest | One line per value worth checking |

Every hook has an empty default. Hooks run only while the module is active, except `onSkip`, which runs for the skipped stage. A module handles its own events and does not call another module; shared work goes into the state, a declared action, or a shared module.

Paused frames: `onFrame` never runs while the game is paused. If a quest needs paused updates, verify in the game source that no callback covers the case, document the reason in the module, and add paused-frame support through [Extending the framework](#extending-the-framework).

### Declarations

`declare` registers names that rules and the framework look up. Names are lowerCamel and unique per kind within the quest (a check and an action may share a name), and are checked at load: a duplicate, a declaration outside `declare` or a null lambda throws. Triggers are any string without whitespace.

```java
public final class Declarations<S, T> {
    public void check(String name, Predicate<QuestContext<S, T>> check);
    public void action(String name, Consumer<QuestContext<S, T>> action);
    public void token(String name, Function<QuestContext<S, T>, String> token);
    public void role(String name, FleetRole role);
    public void person(String key);
    public void intel(String key, String icon, String... tags);
    public void trigger(String trigger);
}
```

| Kind | Rules use | Runs when | Contract |
|---|---|---|---|
| `check` | `nskr_quest <q> check <name> [args]` in Conditions | Any stage | Reads only. Returns false when its data is not set. Works with no dialog. |
| `action` | `nskr_quest <q> do <name> [args]` in Script | Only while the declaring module is active; otherwise logged and skipped | Does one game action. May call `advance`. Never prints prose. |
| `token` | `$nskr_<q>_<name>` in Text and option text | Any stage | Reads only, returns a finished display String, `""` when its data is not set. No random draws. |
| `role` | Fleet memory flag `$nskr_<q>_<role>` | While fleets of the role exist | See [Fleets](#fleets). |
| `person` | Person tokens `$nskr_<q>_<key>_name` and the pronoun tokens; person id `nskr_<q>_<key>` | While the person exists | Required for every quest person key. See [People](#people). |
| `intel` | Intel rows keyed by `$nskr_intel_key` | When shown | See [Intel](#intel). |
| `trigger` | Rows on that trigger | When Java fires it | Every trigger that Java opens, claims or reads text from. The check tool treats it as fired. |

Extra rules arguments reach the lambda through `ctx.args()`. Prefer a separate named check over arguments; use arguments only for real parameters such as an amount.

### QuestContext

```java
public final class QuestContext<S, T> {
    public Quest<S, T> quest();
    public T state();
    public S stage();
    public boolean isJump();

    public void advance(S to);
    public boolean advance(S from, S to);   // false and no change when the stage is not `from`

    public boolean has(Enum<?> flag);
    public void set(Enum<?> flag);
    public void clear(Enum<?> flag);

    public Random random(String purpose);
    public void startTimer(String name);
    public boolean hasTimer(String name);
    public float days(String name);         // days since start; 0 when not started
    public void clearTimer(String name);

    @SafeVarargs public final void mark(SectorEntityToken entity, S... scope);
    @SafeVarargs public final void mark(PersonAPI person, S... scope);
    public void unmark(SectorEntityToken entity);
    public void unmark(PersonAPI person);
    @SafeVarargs public final void claimDialog(SectorEntityToken entity, String trigger, S... scope);
    public void releaseDialog(SectorEntityToken entity);
    public void open(SectorEntityToken target, String trigger);

    public QuestFleets fleets();
    public QuestPeople people();
    public QuestIntels intel();
    public QuestRewards rewards();

    public InteractionDialogAPI dialog();        // null outside a dialog
    public Map<String, MemoryAPI> memoryMap();   // null outside a dialog
    public SectorEntityToken target();           // dialog target; in a token, the entity whose text is replaced; else null
    public TextPanelAPI textPanel();             // null outside a dialog
    public List<String> args();                  // extra rules arguments, empty from Java

    public void log(String message);
}
```

A scope of no stages means the current stage only. `S...` scopes list every stage in which the mark or claim stays.

### QuestManager

```java
public static QuestManager get();                                          // the instance in ModPlugin.EFS_LIST; null before load
public void jump(Quest<S, T> quest, S target);                              // see A stage jump
public QuestContext<?, ?> context(String questId, String ruleId, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, List<String> args);
```

`context` builds the context the rules command passes to checks, actions and tokens; it returns null for an unknown quest or one without state; it does not check availability. `QuestDialogs.claimedTrigger(entity)` and `QuestDialogs.plugin(trigger)` serve the `CorePlugin` route. Quest code uses the context, not these.

### Queries from other features

Contracts, kiosks, spawners and plugins outside a quest's package ask the quest; they never read its state fields or keys.

```java
public final class Quests {
    public static <T extends QuestState<?>> T state(String questId);   // null before load or when the quest has no state
    public static boolean is(QuestStage... stages);
    public static boolean reached(QuestStage stage);
    public static boolean has(Enum<?> flag);
}
```

Each quest adds public static methods for the questions other features ask, named after the question: `KestevenQuest.kestevenEndingDone()`, `KestevenQuest.researchServicesClosed()`. All return safe values before a campaign is loaded. A feature that must change another quest's state calls a named method on that quest (`KestevenQuest.reportMessengerMet()`), never its state fields.

### Events

The manager implements the vanilla callbacks below and routes each to the hooks of active modules. It adds no polling of its own except the daily tick and the frame hook.

| Vanilla callback | Source type | Module hook | Routed to |
|---|---|---|---|
| `advance`, once per campaign day since the saved `lastDay` (see Days below) | `EveryFrameScript` | `onDay` | All active modules |
| `advance`, every unpaused frame | `EveryFrameScript` | `onFrame` | Active modules whose `wantsFrames` is true |
| `reportCurrentLocationChanged(prev, curr)` | `CurrentLocationChangedListener` | `onLocationChanged` | All active modules |
| `reportFleetDespawned(fleet, reason, param)` | `CampaignEventListener` | `onFleetGone` | Active modules of the fleet's owning quest |
| `reportBattleOccurred(primaryWinner, battle)` | `CampaignEventListener` | `onBattle` | Owning quests of every quest fleet in the battle |
| `reportEncounterLootGenerated(plugin, loot)` | `CampaignEventListener` | `onLoot` | Owning quests of the quest fleets on the side the player fought |
| `reportColonyDecivilized(market, fullyDestroyed)` | `ColonyDecivListener` | `onDecivilized` | All active modules |

Delivery order is quest order in `QuestCatalog`, then module order. A stage change during delivery takes effect at once: each module's activity is checked when its turn comes, so a module that became inactive does not receive the rest of that event and one that became active does. Unavailable quests and quests without state receive nothing, which includes events that arrive during a load before `startQuests()`. `EconomyTickListener` is not a daily tick (it fires about every three days) and is not used.

- **Frame order.** Each unpaused frame runs, in order: the daily tick, then `onFrame`, then the retry of [pending dialogs](#dialog-entry-points).
- **Days.** The store saves the clock timestamp of the last delivered day (`lastDay`), set when the store is first created. Each unpaused frame, when `getElapsedDaysSince(lastDay)` reaches 1, the manager advances `lastDay` by one day (86,400,000 timestamp units; `CampaignClock.getElapsedDaysSince` divides by `8.64E7`) and delivers `onDay`. Reloads keep the count. At most one `onDay` is delivered per frame, so days missed during a long fast-forward arrive one per frame until caught up; there is no cap. Days count from the store's creation, not from calendar midnight.
- **Contexts.** The manager keeps one context per module and passes it to every hook call of that module; an idle frame, where no module wants frames, allocates nothing.
- **Registration.** Implementing a listener interface on `QuestManager` is enough. The `EFS_LIST` loop calls `getListenerManager().addListener(script, true)`; vanilla's `ListenerManager` files the object under every class and interface it implements (`FastIterationClassifier.classify`), and `ListenerUtil` fetches listeners by interface. `beforeGameSave` removes the object and `afterGameSave` adds it again.
- **Decivilization.** Only `reportColonyDecivilized` is routed. `DecivTracker.decivilize` fires `reportColonyAboutToBeDecivilized` earlier in the same call, before the market leaves the economy; the manager ignores it.

To add a callback, follow [Extending the framework](#extending-the-framework): implement it on `QuestManager`, add a hook with an empty default, route it through the manager's `deliver` helper (all quests) or `deliverToOwners` (each quest fleet's event to its owning quest), and add a row here.

### Fleets

`QuestFleets` is the only fleet registry for quests. It is built on the existing fleet toolkit: `SimpleFleet`, `SimpleFleetMember` and `SimpleCaptain` build the fleet, `FleetInfo` tracks it, and `FleetHelper` stores the list and runs the AI.

```java
public final class QuestFleets {
    public static final String KEY = "$nskr_questFleets";            // FleetHelper list, in FLEET_ARRAY_KEYS
    public static final String OWNER_KEY = "$nskr_questOwner";       // fleet memory: quest id
    public static final String ROLE_KEY = "$nskr_questRole";         // fleet memory: role name
    public static final String RECORD_KEY = "$nskr_questRecord";     // fleet memory: record id, record quests only

    public CampaignFleetAPI spawn(String role, SimpleFleet spec);
    public CampaignFleetAPI spawn(String role, String record, SimpleFleet spec);
    public CampaignFleetAPI adopt(String role, CampaignFleetAPI fleet);   // a fleet built elsewhere, such as a dormant guardian
    public List<QuestFleet> get(String role);
    public QuestFleet first(String role);                                 // null when none
    public void despawn(String role);
    public void reassign(QuestFleet fleet, String role);                  // another declared role of the same quest
}

public final class FleetRole {
    public static FleetRole of(FleetOrders orders);
    public FleetRole persistent();                    // survives the stop of its module
    public FleetRole config(FIDConfigGen gen);        // stored as $fidConifgGen; must be a named static class
    public FleetRole defeatTrigger(String trigger);   // Misc.addDefeatTrigger on spawn
}

public final class FleetOrders {
    public static FleetOrders none();                 // vanilla assignments from SimpleFleet only
    public static FleetOrders intercept(FleetHelper.InterceptBehaviour behaviour);
    public static FleetOrders guard(FleetHelper.GuardMovementBehaviour movement, FleetHelper.GuardAttackBehaviour attack, float playerInterceptChance);
    public static FleetOrders leave();                // go to FleetInfo.target and despawn there
    public FleetOrders withdrawWhenBeaten();          // copies; see Withdrawal below
    public FleetOrders withdrawAfter(float days);
    public static FleetOrders raid(String orbitText, boolean withdrawHome);
}

public final class QuestFleet {
    public CampaignFleetAPI fleet();
    public FleetInfo info();
    public String role();
    public String record();                           // null outside record quests
    public boolean isRole(String role);
    public boolean wasDestroyed(FleetDespawnReason reason);   // DESTROYED_BY_BATTLE or NO_MEMBERS
}
```

`spawn` calls `spec.create()`, builds the `FleetInfo` with `getFlagshipInfo()` and `getSecondaryMembers()` (home `spec.loc`, no target), writes `OWNER_KEY`, `ROLE_KEY`, `RECORD_KEY` and the role flag `$nskr_<q>_<role>` = `true` to fleet memory, applies the role's config (`MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN`) and defeat trigger (`Misc.addDefeatTrigger`), and adds the `FleetInfo` to the `KEY` list through `FleetHelper.getFleets` and `setFleets`. `adopt` does the same for a fleet built elsewhere, with no flagship data and no home; adopting a fleet that is already a quest fleet logs an error. `spawn` and `adopt` for an undeclared role log an error and return null without building. The builder only configures the `SimpleFleet`; it never registers, logs or stores the fleet. `reassign` gives a fleet of this quest another declared role, when its purpose changes (a messenger that leaves after its message, a hunter that gives up and guards): it unsets the old role flag, the old role's config and its defeat trigger (`Misc.removeDefeatTrigger`), then writes `ROLE_KEY`, the new role flag, config and defeat trigger. Owner, record, `FleetInfo` and its age stay; the next orders interval applies the new role's orders. Rows keyed on the old role flag stop matching, so a conversation that must reach the fleet in both roles tests both flags.

The `KEY` list in sector memory is the only saved fleet data. Owner, role and record live in fleet memory; `FleetRole` and `FleetOrders` are definitions, and each fleet's orders are looked up from its quest's role declaration by those keys. `QuestFleet` is a view built when needed.

- **Load checks.** `FleetRole.config` throws unless the generator is a named top-level or static nested class, because fleet memory saves it; a lambda, anonymous, local or inner class would drag its enclosing objects into the save. A role's defeat trigger must also be declared with `d.trigger`, or the definition throws at load.

- **Orders.** Every 0.1 days of campaign time (`Misc.getDays(amount)`, unpaused frames), the manager reads the list and applies each live fleet's `FleetOrders` by calling `FleetHelper.gotoAndInterceptPlayerAI`, `FleetHelper.guardTargetAI`, for `leave()` `FleetHelper.goToTargetAndDespawnAI` or, for `raid(...)`, `FleetHelper.raidTargetAI`, the cadence the old questline and the old intercept fleets use and the one those methods are written for. The list is read once per interval, not every frame, because `FleetHelper.getFleets` goes through the sector's `getMemory()`, which runs every campaign plugin's `updateGlobalFacts`. Orders do not depend on the quest being available. A fleet whose quest or role is unknown gets no orders and is logged once. The manager also advances `FleetInfo.age` in days. A behavior these two methods do not provide is added to `FleetHelper`, then to `FleetOrders`; never to a quest.
- **Withdrawal.** `withdrawWhenBeaten()` (below a quarter of the spawn strength, `FleetHelper.isBeaten`) and `withdrawAfter(days)` (`FleetInfo.age` above the days) replace the orders with `FleetHelper.despawnOutOfSight`: the fleet keeps its last assignment and despawns with `PLAYER_FAR_AWAY` once it is farther from the player than `getMaxSensorRangeHyper()`, measured between `getLocationInHyperspace()` positions. The despawn reaches `onFleetGone`. This is the rule the old spawners check before their fleet AI.
- **Leaving.** `leave()` issues `GO_TO_LOCATION_AND_DESPAWN` to `FleetInfo.target` with the text "returning to <market name>" (the entity name without a market) whenever the fleet has another assignment, after the same `STANDING_DOWN` handling as the other two methods. It does nothing while the target is null, so the quest sets the target before the fleet takes the role.
- **Raid.** `FleetOrders.raid(orbitText, withdrawHome)` runs `FleetHelper.raidTargetAI`: the fleet goes to `FleetInfo.target` when farther than `FleetHelper.RAID_ORBIT_RANGE` (600) and otherwise orbits it (`ORBIT_PASSIVE`, `orbitText`), re-issued on every order interval. `spawn` leaves the target null, so the quest sets `fleet.info().target` right after spawning. When the quest clears the target, or the fleet falls below `RAID_BROKEN_STRENGTH` (a fifth) of the fleet points it had when registered (`FleetHelper.isRaidBroken`), it withdraws once with `GO_TO_LOCATION_AND_DESPAWN`: to `FleetInfo.home` when `withdrawHome`, otherwise to a random market of its faction from `SystemHelper.getRandomFactionMarket`, or home when there is none. `FleetHelper.isRaidingTarget` answers whether a fleet is orbiting its target in that sense. The market pick draws from the owning quest's saved random `fleetOrders` (`Misc.random` for a quest without state), which `QuestManager` passes to every order. Raid fleets can also take `withdrawAfter(days)`.
- **Despawn routing.** `reportFleetDespawned` removes a registered fleet from the list and calls `onFleetGone` on its quest's active modules. Every despawn the game makes uses this path: destroyed in battle, `NO_MEMBERS`, reaching a destination, or despawned by other code.
- **Cleanup.** Fleets of a role are despawned when the module declaring the role stops, unless the role is `persistent()`, and a reset removes all of the quest's fleets. The same happens for `ctx.fleets().despawn(role)`. The framework removes these fleets from the list first and then calls `despawn(FleetDespawnReason.OTHER, null)`, so its own removals do not reach `onFleetGone`: the quest asked for them, and the stopping module is no longer active anyway. Vanilla `despawn` fades the fleet out where it is.
- **Battles and loot.** `onBattle` goes once per quest fleet in `battle.getSnapshotBothSides()`, the list the engine walks for its own fleet listeners. Vanilla reports the battle before it despawns emptied fleets (`FleetEncounterContext` for player battles, `Battle.doAutoresolveRound` before `removeEmptyFleets`), so `onBattle` comes before `onFleetGone`. `onLoot` goes once per quest fleet in `plugin.getBattle().getNonPlayerSideSnapshot()`, the side the loot comes from; `FleetInteractionDialogPluginImpl` reports the loot before `applyAfterBattleEffectsIfThereWasABattle` despawns the defeated fleets. Fleet events go to the owning quests in `QuestCatalog` order, then in the order of the fleets.
- **Fleet conversations.** Rows on `OpenCommLink` or `BeginFleetEncounter` test the role flag, as in `$nskr_kq_collector`. See [Entry points](../../../../docs/RULES_WRITING.md#entry-points).
- **After a player victory.** Use `FleetRole.defeatTrigger` for rows that must run after the player defeats the fleet through the fleet dialog; `FleetInteractionDialogPluginImpl` fires them with `FireBest`. It does not run when another fleet destroys the quest fleet; `onFleetGone` covers every case.
- **Engaging from an entity dialog.** `nskr_quest <q> engage <role>` in the Script of a row of an entity dialog (a guarded planet, a blacksite, a cache) starts the encounter with the quest's first fleet of the role, the way vanilla's `SalvageDefenderInteraction` does: `dialog.setInteractionTarget(fleet)`, `dialog.setPlugin(plugin)`, `plugin.init(dialog)` with a new `FleetInteractionDialogPluginImpl()`. That constructor reads its `FIDConfig` from the fleet's `MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN`, which `spawn` and `adopt` set from the role's `config`, so the encounter is the same one the player gets by clicking the fleet; a role without `config` gets vanilla's default `FIDConfig`. The verb refuses (error, false) an undeclared role, a role without a fleet, a fleet that is not alive or not in the player's location, and a call without a dialog.
  - **The current dialog.** The rules plugin is replaced in the open dialog; the row's Text shows first, and the Script lines after `engage` still run against the old memory map, so `engage` is the last line of the row and the row adds no options. The fleet dialog clears the options and shows its own.
  - **After the fight.** The fleet dialog runs as usual: after a player victory it fires the role's defeat trigger rows with `FireBest` inside the fleet dialog, then loot. On leaving, the default `FIDConfig` (`dismissOnLeave` true) closes the dialog and the player is back on the map; the entity dialog does not come back. A follow-up scene at the entity is opened with `ctx.open(entity, trigger)` from `onFleetGone` or an action; it waits in the pending list until the fleet dialog has closed. A role whose `FIDConfigGen` sets `dismissOnLeave` false and a delegate takes over the leave itself, as `Cache` and `MothershipSpawner` do.
  - **Fleet events.** A fleet destroyed in the fight is despawned by the encounter (`DESTROYED_BY_BATTLE`) and reaches `onFleetGone`; `onBattle` and `onLoot` come first, as for any quest fleet.
- **Scaling.** Builders multiply point budgets by `Difficulty.scriptedFleetMult()`; encounters that scale with the player also use `PowerLevel.get(...)`.

### People

```java
public final class QuestPeople {
    public PersonAPI create(String key, String factionId, Consumer<PersonAPI> setup);   // returns the existing person when the key exists
    public PersonAPI get(String key);                                                  // null when absent
    public String id(String key);                                                      // nskr_<q>_<key>
    public void release(String key);
}
```

Every key passed to `create` is declared first with `d.person(key)` in the `declare` of a module; the key follows the declaration name rules (lowerCamel, unique within the quest). `QuestPeople` refuses a key that is not declared. The declaration lets the [rules check tool](#rules-check-tool) check person tokens and their clashes with token names.

`create` makes the person with `FactionAPI.createRandomPerson(ctx.random("person:" + key))`, sets the id `nskr_<q>_<key>`, runs `setup` for portrait, name, rank, post and gender, registers the person with `ImportantPeopleAPI.addPerson` and stores it in the state. Registration is what lets the vanilla presentation commands find generated people: `BeginConversation nskr_kq_host` makes the person the active speaker (their memory becomes `$local`), and `ShowSecondPerson` and `ShowThirdPerson` add portraits. `release` removes the person from the important people and the state; call it in `onStop` for people the quest no longer needs. A quest reset releases all of its people.

- **Keys** are the ones declared with `d.person`. An undeclared key or an unknown faction id logs an error and `create` returns null.
- **Ids.** `ImportantPeople.addPerson` files the person under `getId()` at the time of the call, so the id is set before registering and must not change afterwards. `BeginConversation <id>` looks the id up with `getImportantPeople().getData(id)`, then among the target market's people, or `POST:<postId>` in its comm directory; `ShowSecondPerson` and `ShowThirdPerson` use `getData(id)` only and do nothing for an unknown id.

Fixed characters (Alice, Jack, Nicholas, Eliza) are created by world generation with ids in `helper/Ids`; quests look them up and never create them.

Each quest person also gets name and pronoun tokens for use while they are not the active speaker: `$nskr_<q>_<key>_name`, `_heOrShe`, `_HeOrShe`, `_himOrHer`, `_HimOrHer`, `_hisOrHer`, `_HisOrHer`. They follow vanilla's `CoreRuleTokenReplacementGeneratorImpl`: the name is `getName().getFullName()`, and `isMale()` picks the male forms, anything else the female forms. The active speaker uses the vanilla tokens ([Tokens in text](../../../../docs/RULES_WRITING.md#tokens-in-text)).

### Dialog entry points

| Entry | How | Java |
|---|---|---|
| A comm directory person | Vanilla `PickGreeting` and menus with `$id == <personId>` and quest conditions | None |
| A market | Options added to the market menu by rows on its triggers | None |
| A quest fleet | Rows on `OpenCommLink` or `BeginFleetEncounter` testing the role flag | Role declared |
| An entity whose whole dialog belongs to the quest: a wreck, a satellite, a quest station | `ctx.claimDialog(entity, trigger)` | Claim in `onStart` |
| A scene started by the quest: an intercept hail, an arrival monologue | `ctx.open(target, trigger)` | Call in a hook |
| An intel button | `ui.showDialog(target, trigger)` from the intel | [Intel](#intel) |
| A bar | `AddBarEvents` rows | None; see [Bar events](#bar-events) |
| A Java dialog that needs rules text, such as a battle setup screen | `FireBest.fire(null, dialog, memoryMap, trigger)` from the plugin, as vanilla's `HistorianBarEvent` does | Declared trigger |

Claims write the trigger to entity memory under `QuestDialogs.CLAIM_KEY` (`$nskr_questDialog`). `CorePlugin` has one route for all of them: when the target's memory holds that key, it returns `new RuleBasedInteractionDialogPluginImpl(trigger)` at `PickPriority.MOD_GENERAL`. Claims are cleared with their scope like marks. Claiming an entity that already holds another claim logs an error and replaces it; releasing unsets the key only while it still holds that claim's trigger. Do not claim a market's entity: that replaces the whole market dialog; add options to its menu instead.

`ctx.open` builds the same plugin and calls `CampaignUIAPI.showInteractionDialog(plugin, target)`. When the UI is busy the call returns false; the manager keeps the request in the state and retries every frame until it opens, as vanilla's wait command does. The manager tries one pending open per frame and none while a dialog is showing, and drops a pending open whose target is no longer alive, with an error. The target must not be null: use the entity the scene is about, such as the hailing fleet. For a monologue, use the entity that caused it. `CampaignState.showInteractionDialog` (0.98a-RC8) switches to the encounter music of a target with a market or a faction unless the target's or its market's memory holds `$playLocationMusicDuringEnc` (`MusicPlayerPluginImpl.KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY`); a scene that plays its own music or none sets that flag on its target first, as the Hellspawn scenes do on the player fleet.

Every trigger a quest claims, opens or fires from Java is declared with `d.trigger(...)`.

### Bar events

A quest bar event is rules only:

1. A row on `AddBarEvents` whose conditions select the market and the quest state calls `AddBarEvent <optionId> "<option>" "<blurb>"`. `BarCMD` shows the blurb and the option.
2. The handler on `DialogOptionSelected` with `$option == <optionId>` starts the conversation. `BeginConversation <personId>` makes a [quest person](#people) the speaker and shows their card.
3. Every exit calls `BarCMD returnFromEvent`, which returns to the bar list and clears the active person.

Details and exits are in [Entry points](../../../../docs/RULES_WRITING.md#entry-points) and [Exits and returns](../../../../docs/RULES_WRITING.md#exits-and-returns). A check verb decides whether the event appears at a market (`nskr_quest kq check partyHere`); the market to use is picked when the stage starts and stored in the state. The person is created in `onStart` of the module, not when the bar opens.

### Intel

One generic class shows every quest's intel entries. Its text comes from rules rows.

```java
public final class QuestIntels {
    public void show(String key);                                 // adds the entry; no-op when shown
    public void update(String key, String updateKey);             // sends an update message
    public void setMapLocation(String key, SectorEntityToken entity);
    public void complete(String key);                             // status completed, update, ends after the vanilla delay
    public void fail(String key);                                 // status failed, update, ends after the vanilla delay
    public void end(String key);                                  // ends at once
    public boolean isShown(String key);
}
```

`ctx.intel()` returns the `QuestIntels` of the context; like `ctx.rewards()` it is bound to the context's dialog. Keys are declared with `d.intel(key, icon, tags...)`: the key follows the declaration name rules, the icon is a key under `graphics.campaignMissions` in `data/config/settings.json`, and the tags are intel tags such as `Tags.INTEL_MISSIONS`. A duplicate key, a blank icon or a null tag throws at load.

- **`show`** adds a `QuestIntel` with `IntelManagerAPI.addIntel(intel, false, textPanel)`: with a dialog open, the posting message prints in its text panel; without one, the game posts a campaign message. It also adds the entry as a sector script, because the intel manager does not advance its entries: the script counts down the vanilla end delay, and the sector drops it once the entry has ended (`isDone()`). `advanceImpl` does nothing. `show` is a no-op while the key has an active entry. An undeclared key, or an icon that `SettingsAPI.getSpriteName("campaignMissions", icon)` does not find, logs an error and shows nothing.
- **Active entry.** An entry is active until `complete`, `fail` or `end`. `isShown` is true only for an active entry, so a completed entry still counting down its delay does not stop `show` from adding a new one.
- **`update`** sends an update message through `sendUpdateIfPlayerHasIntel(updateKey, textPanel)`: in the dialog's text panel when one is open, otherwise as a campaign message, which the game sends only for an entry the player has and that is not hidden. `complete` and `fail` set the status, send an update with an empty update key and call `endAfterDelay()` (`BaseIntelPlugin.getBaseDaysAfterEnd()`, 3 days). `end` calls `endImmediately()` on every entry of the key, active or ending; the intel manager removes ended entries on its next unpaused advance. `update`, `setMapLocation`, `complete` and `fail` on a key without an active entry log an error and do nothing.
- **Map location.** `setMapLocation` stores the entity on the entry; `getMapLocation` returns it, and vanilla derives the `Local` tag from it.
- **Reset.** A quest [reset](#a-stage-jump) ends every entry of the quest at once.
- **Saved fields.** The intel manager and the sector's script list save the entry, so `QuestIntel` holds only the quest id, the key, the icon, the tags, the status (`QuestIntel.Status`) and the map entity, and reads its quest's state and rows when displayed. The update key lives in `BaseIntelPlugin`'s transient `listInfoParam` while a message is built. When the quest has no state, the entry shows its key as the title, matches no rows and logs once per entry and load.

Text rows, read with `QuestText` outside any dialog:

| Trigger | Matching | Shows |
|---|---|---|
| `nskr_<q>IntelTitle` | Best match | The entry's title in the list, map tooltip, messages and description panel |
| `nskr_<q>IntelBullets` | Every matching row, one bullet each | Bullets in the list, the map tooltip and messages |
| `nskr_<q>IntelDesc` | Every matching row, one paragraph each, in file order | The description panel |

Before matching, `QuestText` writes these keys into a scratch local memory: `$nskr_intel_key` (the entry key), `$nskr_intel_status` (`active`, `completed` or `failed`), `$nskr_intel_update` (the update key, empty otherwise) and `$nskr_intel_mode`. The mode is `list` for the intel list (`ListInfoMode.INTEL`) and the sort title, `tooltip` for the map tooltip (`MAP_TOOLTIP`), `update` for every message (`MESSAGES`: the posting message, updates, completion and failure) and `desc` for the description panel and its title (`IN_DESC` too). Rows select with these keys and with `nskr_quest` conditions. Rows under these triggers must use only conditions that work without a dialog: memory keys and `nskr_quest` condition verbs. Their Script and Options columns are ignored.

- **Memory.** Matching uses `local` (the scratch memory), `player` and `global`; player and global memory are read with `getMemoryWithoutUpdate()`, without the campaign plugins' fact refresh. Token replacement gets the scratch memory only, so `$player.` and `$global.` keys are not replaced in intel text; show computed values with [tokens](#tokens).
- **Text.** Rows with blank text are skipped. The title is not highlighted; in bullets and paragraphs, each quest token value is highlighted in `Misc.getHighlightColor()`. Text goes to `addPara(text, color, pad)`, which does not run `String.format`, so `%` shows as written.
- **Stable text.** The intel UI builds entries again whenever it redraws them, and the engine picks at random among title rows with equal scores and among `OR` variants each time. Title rows must not tie, and intel rows do not use `OR` variants.
- **Cost.** The intel list reads each entry's title once per sort: it calls `setTagsForSort` on every listed entry right before sorting by `getSortString`, and `QuestIntel` keeps the title from that call until the next one. Every other call reads the rows again. The core UI calls found in the 0.98a-RC8 source build a list item (`recreate`), a map tooltip (`createImpl`) or a message; whether the map tooltip is rebuilt every frame while hovered was not determined.

```java
public final class QuestText {
    public static String trigger(String questId, String suffix);                        // nskr_<q> + TITLE, BULLETS or DESC
    public static Map<String, MemoryAPI> intelMemory(String key, String status, String update, String mode);
    public static String title(String trigger, Map<String, MemoryAPI> memoryMap);       // best match; null when none or blank
    public static List<Line> lines(String trigger, Map<String, MemoryAPI> memoryMap);   // every match in file order

    public static final class Line {
        public final String text;           // after token replacement
        public final String[] highlights;   // quest token values, once per occurrence, in text order
    }
}
```

`QuestText` also holds the scratch keys, trigger suffixes and mode values as constants, which the [rules check tool](#rules-check-tool) reads. It matches with `RulesAPI.getBestMatching` and `getAllMatching` with a null dialog, picks each row's text with `RuleAPI.pickText()` and replaces tokens with `performTokenReplacement(ruleId, text, null, memoryMap)`; [Rules text outside a dialog](../../../../docs/RULES_AUTHORING.md#rules-text-outside-a-dialog) has the verified engine behavior. A check lambda or command that throws during matching is not caught.

### Tokens

`QuestTokens` is a `RuleTokenReplacementGeneratorPlugin`. For a rule whose id starts with `nskr_<q>_`, it returns every token declared by quest `<q>` as `$nskr_<q>_<name>`, plus the person tokens of that quest's people. It returns nothing for other rule ids, so tokens work only in the quest's own rows.

- Tokens are computed when text is shown, so no row has to prepare them first. `QuestTokens` is registered once per load in `ModPlugin.onGameLoad`; the engine keeps generators in a transient list (`CampaignEngine.ruleTokenGenerators`).
- **Format.** Keys include the `$`. `performTokenReplacement` collects every generator's map, then replaces keys longest first with `text.replaceAll("(?s)\\" + key, value)`, then runs the memory pass. The value reaches `replaceAll` unescaped, so `QuestTokens` passes every value through `Matcher.quoteReplacement`; a player-chosen name with `$` or `\` shows as written.
- **Failures.** A token lambda that throws shows as empty text; the error is logged once per token per load. A null value shows as empty text.
- **Context.** Token lambdas get a context with no dialog and the memory map of the replacement, which may be null. `ctx.target()` is the entity the engine passes to the generator, which for dialog text is the dialog's interaction target (`FireBest`/`FireAll` text, `Misc.Token.getStringWithTokenReplacement` for command arguments), so a record quest's token reads the record of the dialog target.

Which rule id reaches the generator, verified in the 0.98a-RC8 source:

| Text | Rule id passed | Quest tokens |
|---|---|---|
| Text column | The row's own id (`FireBest`/`FireAll.addText(rule.getId(), ...)`) | Yes |
| Script commands that replace tokens in their arguments: `AddText`, `AddTextSmall`, `SetTextHighlights`, `SetTooltip`, `SetTooltipHighlights`, `AddBarEvent` (option and blurb), `AddSelector`, `BeginConversation` | The row's own id (a rule runs its script with its own id) | Yes |
| Options column, and options from option-adding commands | The id of the rule that fired the trigger: a row's `FireAll` or `FireBest` passes that row's id to the options it collects | Only when the firing row is a row of the same quest |
| Options of a row that the dialog applies itself: the dialog's initial trigger and every option click (`RuleBasedInteractionDialogPluginImpl` fires `DialogOptionSelected` with `FireBest.fire(null, ...)`), and triggers Java fires with a null rule id | null | No |
| Options of rows collected by a vanilla row's `FireAll`, such as `PopulateOptions` from `marketDock` | The vanilla row's id | No |
| `SetStoryOption` log text, `AddRaidObjective` tooltip | The text itself (vanilla passes the argument as the rule id) | No |
| `SetOptionText` | Reads its text literally | No |

So an option label with a quest token belongs in a row that a quest row reaches with `FireAll` or `FireBest`, as in the [example](#making-a-quest) (`nskr_ex_captainOpen` fires `nskr_exCaptainOptions`). A plain-chain handler row whose own Options column needs a token fires a private option trigger instead.
- A token cannot be a command argument: commands read memory, not tokens. A computed amount is granted by an action ([Rewards](#rewards-and-receipts)).
- In the commands above that replace tokens in their arguments, quote the token: `SetTextHighlights "$nskr_kq_job3Start"`. `Misc.tokenize` makes an unquoted `$` word a variable, and `Token.getStringWithTokenReplacement` first reads a variable from memory, which holds no token, so the argument is null and the command skips it. A quoted word is a literal even when it starts with `$`, and its text then gets token replacement (0.98a-RC8: `Misc.tokenize` and `Token.getString` in `sources-api/util.java`, `SetTextHighlights` in `sources-api/impl.campaign.java`). The check tool reports the unquoted form as a token read.
- No token name may be a prefix of another token name in the same quest (`pay` and `payout`); the check tool reports it.
- Rows never assign a key with a token's name.

### Rewards and receipts

Grants with a literal amount use the vanilla commands in rows (`AddCredits`, `AddRemoveCommodity`, `AddStoryPoints`, `AdjustRep`), which print their own receipts. Grants with a computed amount use an action and `ctx.rewards()`:

```java
public final class QuestRewards {
    public void credits(int amount);                       // AddRemoveCommodity.addCreditsGainText
    public void takeCredits(int amount);                   // addCreditsLossText
    public void commodity(String commodityId, int quantity);
    public void item(SpecialItemData item, int quantity);
    public void storyPoints(int points);                   // MutableCharacterStatsAPI.addStoryPoints(points, textPanel, false)
    public void skill(String skillId, float level);        // MutableCharacterStatsAPI.setSkillLevel; receipt with the skill panel
}
```

Each applies the grant the way `AddRemoveCommodity` and `AddRemoveAnyItem` do and prints the vanilla receipt when the context has a dialog. `credits`, `takeCredits` and `storyPoints` need a positive amount (otherwise an error is logged and nothing happens); credits never drop below zero; a negative `commodity` or `item` quantity removes, and `commodity` refreshes `$supplies`-style player memory through `AddRemoveCommodity.updatePlayerMemoryQuantity`. Without a dialog, `storyPoints` uses `addStoryPoints(points)`. `skill` sets the player's skill level; no vanilla command grants a player skill (`NGCSetSkill` is new-game only) and no vanilla helper prints its receipt, so it prints "Gained <skill name>" in small gray text with the name highlighted, then the skill's panel (`TextPanelAPI.beginTooltip().addSkillPanel(person, 10f)` for a person holding only that skill). An unknown skill id or a level of 0 or less logs an error and grants nothing. Outside a dialog the quest reports the grant through an intel update instead. Reputation changes stay in rows (`AdjustRep`) unless no dialog exists; add a method here when a quest needs one.

### Random, timers and marks

- **Random.** `ctx.random(purpose)` returns a saved `Random` for that purpose, created on first use from the quest seed as `new Random(seed + purpose.hashCode() * 181783497276652981L)`, the derivation vanilla's `BarEventManager.getSeed` uses. The same purpose continues its sequence after a reload, so reloading does not reroll. Use one purpose per decision (`"job3Target"`, `"collectorRoll"`).
- **Timers.** `ctx.startTimer(name)` stores the clock timestamp; `ctx.days(name)` is `CampaignClockAPI.getElapsedDaysSince`. Check a timer where the answer is needed (in a check, a token or `onDay`), never every frame. Name timers with constants in the state class.
- **Marks.** `ctx.mark(entity)` calls `Misc.makeImportant(entity, reason)` with the reason `nskr_<q>`, and the manager calls `Misc.makeUnimportant` when the scope ends, the pattern vanilla hub missions use. Never call `makeImportant` directly for a quest.

### The quest command

`nskr_quest <q> <verb> [arguments]`, in `lostsector.dialogue.rules`. Quest ids, stage names, flag names and declared names are checked: an unknown name logs an error and returns false.

| Verb | Column | Passes or does |
|---|---|---|
| `is <STAGE> [<STAGE> ...]` | Conditions | The current stage is one of those listed |
| `reached <STAGE>` | Conditions | The stage has been entered at some point |
| `flag <FLAG>` | Conditions | The flag is set |
| `check <name> [args]` | Conditions | The declared check returns true |
| `advance <FROM> <TO>` | Script | Changes the stage from `FROM` to `TO`; does nothing and logs when the stage is not `FROM` |
| `set <FLAG>`, `clear <FLAG>` | Script | Sets or clears a decision flag |
| `do <name> [args]` | Script | Runs the declared action |
| `engage <role>` | Script | Hands the open dialog to the fleet encounter with the quest's first fleet of that role; see [Fleets](#fleets) |
| `confirm <optionId> "<text>" "<yes>" "<no>"` (no quest id) | Script | Adds a yes/no prompt to an option that already exists |

- Negate a condition with `!`: `!nskr_quest kq reached JOB3_ACTIVE` ([Operators](../../../../docs/RULES.md#operators-verified-against-decompiled-source)).
- Condition verbs never change state and never touch the dialog, so they work in intel rows.
- `advance` names both stages so the row states which change it makes and cannot fire twice.
- The command adds no options and prints no text; `doesCommandAddOptions()` is false. The class `nskr_quest` resolves its tokens (memory variables included) and hands them to `QuestVerbs`, which runs the verbs with the framework's package access.
- **Errors.** Unknown quest ids, stages, flags, checks and actions, a quest without state, a missing argument and an action whose declaring module is not active log an error with the quest id and the rule id and return false; in dev mode the line is also printed in the dialog's text panel, condition verbs included. Exceptions thrown by a check or action lambda are not caught, as for module hooks.
- **Rule ids.** Condition commands receive the id of the row being matched, Script commands the id of their own row.
- **Arguments.** For `check` and `do`, `ctx.args()` holds the words after the name.
- Presentation (portraits, speaker changes, map markers, highlights, small text, images, sounds) uses vanilla commands; [Presentation in rules](../../../../docs/DIALOGUE.md#presentation-in-rules) names the command for each effect. `confirm` and `engage` fill the two gaps T09 found.
- **`confirm`** is quest-independent: the word after `nskr_quest` is the verb, so no quest id may be `confirm` (or `engage`, reserved with it). It calls `OptionPanelAPI.addOptionConfirmation(optionId, text, yes, no)`; the text gets token replacement with the row's rule id, as `SetTooltip` does, and the labels are used as written. Both labels are required. The engine stores the prompt on the option with that id and ignores an unknown id (`sources-obf/ui.newui.java` 4835), so the verb checks `hasOption` first and logs an error when the option does not exist yet: put it in the Script of the row that adds the option (options are added before Script runs) or later. Confirming fires `DialogOptionSelected` as a click does; declining does nothing.

### Dev tools

`nskr_questDev <verb>`, a `PaginatedOptions` command in `lostsector.dialogue.rules`, drives a dev menu for every quest. `QuestDevTools`, in the framework package for access to the state and the manager's runs, reads and changes the quests; the command shows the lists and prints the lines. Quests add nothing to the dev menu beyond `onSkip` and `devInfo`.

The entry is the option `(dev)nskr_questDev_open` in the market menu (`PopulateOptions` with `$hasMarket` and `$menuState == main`, as vanilla's own dev menus). `FireBest` and `FireAll` skip option ids starting with `(dev)` outside dev mode, so the menu exists only in dev mode ([Options](../../../../docs/RULES_WRITING.md#options)). The rows are the `# QUEST DEV` block of `rules.csv`.

| Screen | Built by | Options | Selection |
|---|---|---|---|
| Quest list | `nskr_questDev quests` | One per quest in `QuestCatalog` order, labelled with its stage; Back (`nskr_questDevQuestsOptions` row) | `select` stores the quest id in local `$nskr_questDev_quest` (expiry `0`) and fires `FireAll nskr_questDevQuest`; Back fires `FireAll PopulateOptions` |
| Quest menu | `FireAll nskr_questDevQuest` | Rows: jump, flags, timers, triggers, reset, Back to the quest list. The first row runs `info` | Each category fires its list verb; reset runs `reset` and shows the menu again |
| Stage list | `stages` | Every stage in declaration order, the current one marked | `jump` calls `QuestManager.jump`, the path of the player's story skip, then shows the quest menu |
| Flag list | `flags` | Every flag of the flag enum, `[x]` when set | `toggleFlag` sets or clears it through the context and shows the list again |
| Timer list | `timers` | Every started timer with its days | `expireTimer` moves the timer's start 1000 days back (`QuestDevTools.EXPIRED_TIMER_DAYS`), so every wait on it has passed, and shows the list again |
| Trigger list | `triggers` | Every trigger declared with `d.trigger` | `open` clears the options and fires the trigger with `FireBest.fire(null, dialog, memoryMap, trigger)` in the current dialog, on the market's entity, with a null rule id as when a claimed entity's dialog opens. The best row takes over the dialog. When no row matches (a line says so) or the fired rows leave no options, the list shows again |

- **Info.** `info` prints, in the small font: the quest's definition class, availability, stage and days in stage, set flags, started timers with their days, and every module's `devInfo` lines prefixed with the module's class name. `devInfo` is called for every module, active or not, with the module's hook context.
- **Reset** is a jump to the start stage, which always resets ([A stage jump](#a-stage-jump)).
- **Lists.** Stages, flags, timers, triggers and quests vary in length, so the command builds them with `PaginatedOptions` (five per page). Option ids are a prefix plus the stage, flag, timer, trigger or quest name: `nskr_questDev_pickQuest_`, `_pickStage_`, `_pickFlag_`, `_pickTimer_`, `_pickTrigger_`; their handlers match with `nskr_optionStartsWith` and the verb reads the name from `$option`. Each list's fixed options come from rows: after drawing every page, the command fires `FireBest nskr_questDevQuestsOptions true` or `FireBest nskr_questDevListOptions true`, which adds the Back row's option to the page and binds Escape to it. A list shown again after a selection opens on the page of that selection (local `$nskr_questDev_page`, expiry `0`).
- **PaginatedOptions**, verified in the 0.98a-RC8 source (`rulecmd/PaginatedOptions.java`): it becomes the dialog's plugin while a list shows. `showOptions` clears the option panel, adds the page, the Previous and Next page options when there is more than one page, then `DevMenuOptions` in dev mode. Selecting a page option redraws the page; any other option prints its label, restores the original plugin, writes `$option` to the target's memory and to local with expiry `0`, and fires `DialogOptionSelected` with `FireBest` and a null rule id. Each list gets its own command instance, because how the rules engine instantiates commands is not in the sources.
- **Errors.** An unknown quest, stage, flag or timer, or a quest without state, is logged with the quest id and printed in the dialog, as for the [quest command](#the-quest-command). Jump errors are logged by `QuestManager.jump`.

### Rules check tool

`lostsector.quest.dev.RulesCheck` reads `data/campaign/rules.csv` and the quest definitions from `QuestCatalog.create()`, and reports problems in the mod's rows. It runs outside the game and touches no game class that needs `Global`.

Run it after every change to `rules.csv`, with the build output and the compile jars from [Building](../../../../CLAUDE.md#building) on the class path:

```sh
java -cp "<build output>:<compile jars>" lostsector.quest.dev.RulesCheck <repository root> [<vanilla rules.csv>]
```

It prints one line per finding, sorted by CSV line, then a summary with counts per check:

```
ERROR data/campaign/rules.csv:120 nskr_ex_captainOptAccept [handler] option nskr_ex_captainAccept has no DialogOptionSelected row with $option == nskr_ex_captainAccept
```

Findings about definitions rather than rows show `-` as the line and `(quest <q>)` as the id. The exit status is 1 when there is an error, 0 when there are only warnings or none, and 2 for a usage or input problem.

**Parsing.** Records are read as RFC 4180 CSV; the line number is the physical line where the record starts. Rows with an empty id and rows whose id starts with `#` are skipped, as the loader does. Conditions, Script and Options cells are split into lines the way the 0.98a-RC8 `Rules` loader does, and each Conditions or Script line is parsed with a port of `Misc.tokenize` and the rule expression constructor (`RuleExpression`). Running the tool on vanilla `rules.csv` gives no load, command or option-format error, which matches the game loading that file. The engine's `LoadingUtils` CSV reader is not in the `starsector-knowledge` sources, so the record parsing itself is standard CSV, not a port.

**Checks.** An error is something that stops the file loading, breaks a quest contract or leaves the player stuck; a warning is likely wrong but can be deliberate or caused by legacy Java.

| Check | Severity | Reports |
|---|---|---|
| `columns` | error | A header other than `id,trigger,conditions,script,text,options,notes`, or a row without exactly seven columns |
| `csv` | error | An unterminated quote |
| `empty-id` | warning | A row with content but an empty id, which the loader skips |
| `duplicate-id` | error / warning | An id used twice under the same trigger (the file fails to load) / under different triggers |
| `whitespace` | error | A Conditions, Script or Options line that holds only spaces |
| `load` | error | A line the engine rejects at load: unmatched quotes, several operators, an operator in a command line, a bad `score:`, `=` in Conditions, `==` in Script |
| `command` | error | A command that is not a class in the vanilla `ruleCommandPackages` or the mod's `data/config/settings.json` list. Only the class file is looked up; no class is loaded |
| `option-format` | error | A colon in an option label (load failure for `id:text`, cut label for `order:id:text`), a line that is neither form, an option id starting with `$` |
| `highlight-order` | error | A `SetTextHighlightColors` line that follows `SetTextHighlights` (or `Highlight`) in one Script with only highlight commands between them. `SetTextHighlightColors` calls `highlightInLastPara(color, "")`, which replaces the paragraph's phrases, so the earlier phrases are lost ([Highlights](../../../../docs/RULES_WRITING.md#highlights-and-small-text)). Any other command may add a paragraph and ends the pair |
| `text-cr` | warning | A carriage return in Text, which stops `OR` variants from splitting |
| `fire-in-conditions` | error | `FireAll` or `FireBest` in Conditions |
| `quest-call` | error | In `nskr_quest` calls: an unknown quest id, verb, stage, flag, check, action or role (`engage`); `confirm` without an option id, a text and both labels; a verb in the wrong column; the wrong number of arguments. A `$variable` argument is a warning, because it is not checked |
| `advance` | error | `advance` from a stage to itself |
| `token` | error | An unknown `$nskr_<q>_<name>` token in Text, option labels or Script literals, including a person token whose key is not declared with `d.person`; a token in a row whose id does not start with `nskr_<q>_`; a token name read in Conditions or Script, where it is not memory |
| `option-token` | error | A quest token in an Options-column label of a row that cannot receive the quest's rule id ([Tokens](#tokens)): a row on `DialogOptionSelected`, on a declared (Java-fired) trigger, on a trigger the engine or vanilla rows fire, or on a trigger that is not fired only by `FireAll` or `FireBest` in rows of the same quest |
| `token-assign` | error | A Script line that assigns a token's name |
| `token-prefix` | error | A token name that is a prefix of another token name or of a declared person key of the same quest |
| `declared-trigger` | error | A trigger declared with `d.trigger(...)` that no row uses |
| `fire-target` | error | A literal `FireAll` or `FireBest` target that no mod or vanilla row uses |
| `unreachable` | error / warning | A trigger with mod rows that nothing fires, reported once at its first row. An error for a quest's trigger (`nskr_<q>` followed by an upper-case letter or `_`), a warning otherwise |
| `handler` | error | An option id from the Options column, an `AddBarEvent` call or a `$option = <id>` line without a `DialogOptionSelected` or `NewGameOptionSelected` row testing `$option == <id>`, in the mod or vanilla |
| `case` | warning | A trigger or memory key of a mod row that differs only by case from another trigger or key in the mod, vanilla or the engine list |
| `unwritten` | warning | A `$nskr_` key read in Conditions, Script, Text or option labels that no mod row writes, that no Java string literal under `jars/src` names, and that is not an intel scratch key, a `QuestFleets` fleet key (`OWNER_KEY`, `ROLE_KEY`, `RECORD_KEY`), the role flag `$nskr_<q>_<role>` of a declared role, or a quest token |
| `identical` | warning | Two rows on one trigger with the same condition lines in any order, unless both notes contain `variant`. Triggers fired with `FireAll` by a row, by vanilla rows or by the engine, and the `IntelBullets` and `IntelDesc` triggers, are skipped, because every match runs there |
| `intel` | error / warning | A command other than `nskr_quest` in the Conditions of an intel row / Script or Options in an intel row, which are ignored |
| `naming` | error / warning | In quest rows (id `nskr_<q>_`): a `$global.` write / a `$nskr_` write other than `$nskr_<q>_<name>` on local memory or `$player.nskr_<name>`; a row on a quest's trigger whose id does not start with `nskr_<q>_` |
| `definitions` | error | `QuestCatalog.create()` throws, including when a definition calls the game ([Definitions are pure](#quest-and-stages)) |

Quest checks use only the quests `QuestCatalog` returns. A `$nskr_<x>_` name whose `<x>` is not a quest id is treated as an ordinary memory key, because legacy keys share that shape, so the tool cannot report an unknown quest id inside a token.

**What counts as fired.** A trigger is fired when a mod row fires it with `FireAll` or `FireBest`; when the engine list in `VanillaRules.ENGINE` names it; when it ends with a hub mission suffix (`_blurb`, `_option`, `_blurbBar`, `_optionBar`, `_startBar`); when vanilla rows use or fire it; when a quest declares it, which includes every role's defeat trigger because `Declarations` refuses an undeclared one; when it is an [intel trigger](#intel) of a quest; or, for triggers that do not belong to a quest, when a Java string literal under `jars/src` equals it, which covers legacy code that fires its own triggers. A quest's own trigger counts only when declared.

**Vanilla lists.** `VanillaRules.ENGINE` lists every trigger the 0.98a-RC8 game code fires or opens with a literal name (`FireBest.fire`, `FireAll.fire`, the dialog plugins' `fireBest` and `fireAll`, `getBestMatching`, `RuleBasedInteractionDialogPluginImpl`), each with its bundle file and line in the `starsector-knowledge` sources; triggers vanilla fires from variables, such as defeat triggers, are covered because vanilla rows use them. `VanillaRules.COMMAND_PACKAGES` is vanilla's `ruleCommandPackages`. `vanilla-rules-index.txt`, next to the tool, lists the triggers vanilla rows use, the literal `FireAll` and `FireBest` targets in vanilla rows, the option ids vanilla rows handle and the memory keys vanilla rows use. The tool reads it from the repository at run time. Regenerate it from a game version's `starsector-core/data/campaign/rules.csv`:

```sh
java -cp "<build output>:<compile jars>" lostsector.quest.dev.RulesCheck --index <vanilla rules.csv> <game version> > jars/src/lostsector/quest/dev/vanilla-rules-index.txt
```

Passing a vanilla `rules.csv` as the second argument of a check builds the same lists from that file instead of the index.

**Limits.**

- Other mods' rule command packages are not known, so rows that call another mod's command get a `command` error.
- A mod row whose id equals a vanilla row id is not reported; the index holds no vanilla ids.

## Rules contract

Where each kind of value lives, for quest content. The general table is in [State and memory keys](../../../../docs/RULES_WRITING.md#state-and-memory-keys).

| Value | Lives in | Rules access |
|---|---|---|
| Stage, targets, timers, counters, decisions Java needs | The quest state | `is`, `reached`, `flag`, `check`, tokens |
| "Already asked", "already introduced", other flags about one speaker | The speaker's memory, as an unscoped key while they are active: `$nskr_<q>_<name>` | Set and test in rows |
| What a fleet is for | Fleet memory, written by `QuestFleets` | `$nskr_<q>_<role>` |
| Knowledge several quests share | `$player.nskr_<name>`, only when two quests read it | Rows |
| Computed display values | Tokens | `$nskr_<q>_<token>` |
| Intel text selection | Scratch memory written by `QuestText` | `$nskr_intel_*` |
| `$global` | Nothing | None |

Naming, extending [Layout and naming](../../../../docs/RULES_WRITING.md#layout-and-naming):

| Item | Pattern | Example |
|---|---|---|
| Rule id | `nskr_<q>_<speakerOrModule><Purpose>` | `nskr_kq_aliceJob3Brief` |
| Private trigger | `nskr_<q><Speaker><Slot>` | `nskr_kqAliceOptions` |
| Java-fired trigger | `nskr_<q><Purpose>` | `nskr_kqHintWreck` |
| Intel triggers | `nskr_<q>IntelTitle`, `nskr_<q>IntelBullets`, `nskr_<q>IntelDesc` | `nskr_kqIntelDesc` |
| Speaker memory key | `$nskr_<q>_<name>` | `$nskr_kq_askedPay` |
| Token | `$nskr_<q>_<name>` | `$nskr_kq_payout` |
| Person id | `nskr_<q>_<key>` | `nskr_kq_host` |
| Fleet role flag | `$nskr_<q>_<role>` | `$nskr_kq_expedition` |

Rows never assign quest state: stages change through `advance`, decisions through `set` and `clear`, everything else through actions.

## Shared modules

Behavior used by more than one quest lives once in `lostsector.quest.modules`, configured through its constructor. A quest adds the shared module to its module list. Before writing a module, check this list; when a second quest needs a module's behavior, move it here first.

| Module | Used by | Does | Status |
|---|---|---|---|
| [`PayOffEncounter`](#payoffencounter) | Loan collector (quest `ic`); planned for the Tri-Tachyon collector | A hostile fleet that demands payment in credits or cargo; pay, part pay or fight | implemented, T37 |
| `BountyEncounter` | Abyss, Eternity, Mothership, Peacekeepers | Spawn, first sighting, intel, completion, reward, shared text slots | planned, T39 |
| [`InterceptEncounter`](#interceptencounter) | ARO strike, "LZ" messenger, Auto-Hunter, loan collector (quest `ic`); planned for the Tri-Tachyon collector | Daily roll, spawn near the player, the orders of its role, an optional second role | implemented, T41 |

The task that builds a shared module documents its constructor and behavior here. A shared module that keeps data defines a saved record class and an interface the quest's state implements to hold the records.

### InterceptEncounter

One kind of fleet that appears near the player and hunts them. A quest adds one instance per kind; quest `ic` (`campaign/events/intercepts/InterceptsQuest`) is the example.

```java
public final class InterceptEncounter<S, T extends QuestState<S> & InterceptEncounter.Host> extends QuestModule<S, T> {
    public enum Repeat { ONCE, REPEATING }
    public interface Host { Map<String, Record> intercepts(); }   // the state creates the map; the module fills it
    public static final class Record { public int spawns(); }      // saved in the state

    @SafeVarargs public InterceptEncounter(String id, String role, FleetRole fleetRole, Repeat repeat, float dailyChance,
            Predicate<QuestContext<S, T>> condition, BiFunction<SectorEntityToken, Random, SimpleFleet> builder, S... stages);
    public InterceptEncounter<S, T> finish(BiConsumer<CampaignFleetAPI, Random> finish);
    public InterceptEncounter<S, T> switchAfter(float days, String role, FleetRole fleetRole, Function<Random, SectorEntityToken> target);
    public InterceptEncounter<S, T> switchOnAction(String action, String role, FleetRole fleetRole,
            Function<Random, SectorEntityToken> target, Consumer<QuestContext<S, T>> onAction);
    public InterceptEncounter<S, T> switchWhen(Predicate<QuestContext<S, T>> condition);   // after switchAfter or switchOnAction
    public InterceptEncounter<S, T> onSwitch(Consumer<CampaignFleetAPI> onSwitch);
    public static boolean playerInHyperspaceWithin(float distanceFromCenter);
}
```

- **Identity.** `id` names the record in `Host.intercepts()`, the random purposes `roll:<id>`, `fleet:<id>` and `target:<id>`, and the record id of its fleets. `role` and `fleetRole` are declared as a role of the quest. No stages: active in every stage.
- **Daily roll.** In `onDay`, when a player fleet exists: a `ONCE` encounter whose record has a spawn, or a `REPEATING` encounter with a live fleet in either of its roles, does nothing. Otherwise, when `condition` holds, one draw from `roll:<id>` below `dailyChance` spawns the fleet. Conditions run only in the game.
- **Spawn.** The builder gets a token at the player's position and the `fleet:<id>` random and returns the fleet unbuilt; `ctx.fleets().spawn(role, id, spec)` builds and registers it. The module then moves it to a random point at 0.9 times the sum of the player's sensor strength and the fleet's sensor profile from the player, faces it randomly, runs `finish` (faction changes, hullmods) and `FleetHelper.update`, all with the same random, and counts the spawn in the record.
- **Second role.** At most one. `switchAfter` switches every fleet of the first role whose `FleetInfo.age` has reached the days, checked in `onDay`. `switchOnAction` declares the action: run from a dialog whose target is a fleet of the encounter, it calls `onAction`, then switches the fleet if it still has the first role; any other target is logged and ignored. `switchWhen` adds a trigger to the second role set by one of the other two (it throws without one): every fleet of the first role switches on the first day the condition holds, checked in `onDay`. A switch first sets `FleetInfo.target` from `target` with the `target:<id>` random (for `guard` or `leave()` orders), then calls `ctx.fleets().reassign`, then runs `onSwitch` on the fleet, for memory flags the new orders need, such as `MemFlags.FLEET_IGNORES_OTHER_FLEETS` on a fleet that goes home.
- **Payment encounters.** A collector uses this module for its spawn and orders: its role carries the fleet's config and hostility, its rows test the role flag, and a `switchOnAction` action sends it home after payment. The payment itself is a [`PayOffEncounter`](#payoffencounter) in the same quest; the loan collector (records `collector` and `collectorLeaving` of quest `ic`) is the example.
- **Dev info.** One line per encounter: spawns, and each live fleet's role and age.

### PayOffEncounter

The game logic of a payment demand: whether the player can pay all, part or nothing, what a payment takes, and the payment with its vanilla receipt. The fleet comes from another module, normally an [`InterceptEncounter`](#interceptencounter) with the same id; the text, the options and every other consequence are rows. Quest `ic`'s loan collector is the example.

```java
public final class PayOffEncounter<S, T extends QuestState<S> & PayOffEncounter.Host> extends QuestModule<S, T> {
    public static final String CREDITS = "credits";
    public static final int EVERYTHING = Integer.MAX_VALUE;
    public interface Host { Map<String, Record> payOffs(); }   // the state creates the map; the module fills it
    public static final class Record { public int payments(); public long paid(); }   // saved in the state

    @SafeVarargs public PayOffEncounter(String id, String currency, ToIntFunction<QuestContext<S, T>> owed, int partMinimum, S... stages);
    public PayOffEncounter<S, T> demandsWhile(Predicate<QuestContext<S, T>> condition);
    public PayOffEncounter<S, T> onPaid(BiConsumer<QuestContext<S, T>, Integer> onPaid);
}
```

- **Currency.** `CREDITS` or a commodity id. The player's holding is whole credits (`(int)` of the float credits, the value `Misc.getDGSCredits` shows) or `getCommodityQuantity` of the commodity. `owed` runs in the game only, whenever a check, the token or the action needs it.
- **Declarations**, all named after the id. No stages: active in every stage.

  | Name | Kind | Passes or does |
  |---|---|---|
  | `<id>Demands` | check | The `demandsWhile` condition holds (default: always) |
  | `<id>CanPayAll` | check | Holding ≥ owed |
  | `<id>CanPaySome` | check | Holding < owed and holding ≥ `partMinimum` |
  | `<id>Payment` | token | The amount the payment takes, min(owed, holding) and never below 0: `Misc.getDGSCredits` for credits, the plain number for a commodity; `""` without a player fleet |
  | `<id>Pay` | action | Takes that amount with `ctx.rewards().takeCredits` or `ctx.rewards().commodity(id, -amount)`, which print the vanilla loss receipt; counts the payment in the record; then runs `onPaid` with the amount. An amount of 0 takes nothing, prints nothing and is logged; `onPaid` still runs |

- **Demand screen.** A `FireBest` pick on a private trigger: a fallback row for "cannot pay", and rows on `check <id>CanPayAll` and `check <id>CanPaySome`, which never pass together. Pay options show the amount with `$nskr_<q>_<id>Payment`, so the pick is fired from a row of the quest ([Tokens](#tokens)). A pay handler runs `do <id>Pay`, then the other consequences in rows (`AdjustRep`, `PlaySound`), then the fleet module's switch action.
- **Part payments.** `partMinimum` is at least 1; `EVERYTHING` as `partMinimum` allows none. A demand for everything the player holds, such as the Tri-Tachyon collector's Artifact Electronics, passes `EVERYTHING` as owed and 1 as `partMinimum`: every payment is then a part payment of everything held, `CanPayAll` never passes, and the pick's "some" row holds the hand-over option.
- **`onPaid`** does what the payment settles in the quest's own data, such as reducing the Kesteven debt. It runs inside the action, after the receipt.
- **Dev info.** One line per demand: payments and the total paid.

## Save compatibility

Version 1.0.c breaks saves, so the overhaul needs no migration. After it ships:

- Saved classes are state classes, their record classes and the stage and flag enums. XStream saves them by class and field name.
- Renaming or moving any of them, or a field, breaks existing saves. Add fields instead, and give them defaults in `readResolve()` because old saves load them as null or zero.
- Removing a stage or flag constant breaks saves that hold it.
- Definitions, modules and lambdas are never saved; change them freely.

## Errors and logging

- Definition errors (duplicate names, a flag enum mismatch) throw during load, so the first dev run finds them.
- Errors from rules or from state (an unknown name, an action whose module is inactive, a guarded `advance` from the wrong stage) never throw: they log at error level with the quest id and the rule id, and the verb returns false. In dev mode they also print a line in the text panel when a dialog is open.
- Every stage change, jump, spawn, despawn, claim and dialog open is logged at info level as `[<q>] ...` through `Global.getLogger(QuestManager.class)`.

## Extending the framework

When a quest needs something the framework does not do:

1. Check this file, the rules guides and the dictionaries again. Most needs are covered by a vanilla command or an existing service.
2. Verify the game side in the exact source: the callback, method or command, its arguments and when it runs.
3. Add the capability to the framework class that owns that job, in general form: a hook with an empty default, a context method, a `FleetOrders` or `FleetRole` option, a `nskr_quest` verb, a `QuestRewards` method.
4. Document it in this file in the same commit: signature, contract, and a row in the relevant table.
5. Use it from the quest.

Do not add a framework feature that only one quest could ever use; keep that in the quest's module.

## What the framework replaces

| Duplicate today | Replaced by |
|---|---|
| `QuestHelper.getFailed`/`setFailed` and `getCompleted`/`setCompleted`, identical bodies | Flags on the state |
| Fourteen hand-written seeded `Random` accessors (`ElizaDialog`, `CacheDoubtDialog`, `CacheCoreDialog`, `EndingKestevenDialog`, `EndingElizaDialog`, `nskr_altEndingDialogLuddic`, `nskr_altEndingDialogTT`, `nskr_job4FleetDialog`, `HintWreckDialog`, `nskr_ttCollectorDialog`, `nskr_elizaInterceptDialog`, `KestevenTipBarEventCreator`, `KestevenTipBarEvent`, `nskr_kestevenQuest`) | `ctx.random(purpose)` |
| `nskr_ttCollectorDialog`, the second copy of the loan collector's encounter | `PayOffEncounter` and rows |
| Intel classes that register themselves and poll in `advanceImpl` | `QuestIntel` and intel rows |
| The spawn-and-register tail repeated across `KestevenFleets` spawners | `ctx.fleets().spawn` |
| `QuestStageManager.runFleetLogic`, per-fleet AI | `FleetOrders` on `FleetHelper` |
| `Color` locals repeated in dialog classes | Text in rows with vanilla highlight commands |
| `QuestStageManager` polling every frame, paused or not | Module hooks |
| Nine separate fleet lists for quest-like content | One `QuestFleets` list |
| Quest branches in `CorePlugin` | One claim route |
| `nskr_barEventFixer` and Java bar events | `AddBarEvents` rows |

Migration map for the Kesteven questline and the other systems. The owning task removes the old code in its commit; `T35` removes what is left of the questline.

| Old | New |
|---|---|
| `QuestStageManager` stage ints, `Saved` flags and timers | `KestevenStage`, `KestevenFlag`, fields on `KestevenState` |
| `QuestHelper` questline getters and setters | `KestevenState` fields and `KestevenQuest` queries |
| `campaign/kesteven/quest/KestevenFleets` builders | Builders in the Kesteven quest package returning `SimpleFleet` |
| `campaign/kesteven/quest/KestevenPeople` | Fixed people stay in world generation; generated people move to `ctx.people()` |
| Java dialog classes (`ElizaDialog`, `CacheCoreDialog`, `HintWreckDialog`, endings, `nskr_kestevenQuest` and the other questline commands) | Rows, checks, actions and claims |
| `HostileTakeoverBarEvent`, `ElizaSearch*BarEvent`, `DelveMeetingBarEvent`, `KestevenTipBarEvent` | `AddBarEvents` rows and quest people |
| `EnemyUnknownIntel`, `HostileTakeoverIntel`, `OperationLifesaverIntel`, `TheDelveIntel`, `CacheIntel` | `QuestIntel` with intel rows |
| `nskr_isKStage` and other stage predicates | `nskr_quest kq is` and `reached` |
| `events/InterceptManager`, its `Saved` spawn flags, frame counters and per-fleet AI | Quest `ic` in `campaign/events/intercepts`: `InterceptEncounter` records, `onDay` rolls, roles with `FleetOrders` withdrawal and `reassign` (done in T41) |
| `kesteven/loans/LoanShark` and `dialogue/rules/nskr_loanSharkDialog`, their persistent-data keys, `$debtCollector` and the `# DEBT collector dialog` rows | Records `collector` of quest `ic`: an `InterceptEncounter` with `switchOnAction`, `switchWhen` and `onSwitch`, a `PayOffEncounter`, and rows in `# INTERCEPTS` (done in T37) |
| `BlacksiteManager`, `BlacksiteDialog`, `BlacksiteInfo` and the sector-memory site list | Record quest `bs` in `campaign/events/blacksite`; `BlacksiteSpawner` stays world generation ([BLACKSITES.md](../../../../docs/quests/BLACKSITES.md)) |
| Outside readers (`ContractManager`, the kiosk commands, `StalkerSpawner`, `InterceptManager`, `BlackOpsManager`, `Cache`, `CorePlugin`) | `Quests` and `KestevenQuest` queries (done in T15; see [KESTEVEN_STATE.md](../../../../docs/quests/KESTEVEN_STATE.md)) |

## Outside the framework

| Content | Owner | Relation to the framework |
|---|---|---|
| Kesteven contracts | Vanilla `BaseHubMission` in `campaign/kesteven/contracts` | Reads quest queries only. Its stages are a mission enum; its offer text is keyed by `$missionId` ([Hub missions](../../../../docs/RULES_AUTHORING.md#reuse-a-mission-object-through-call)). |
| Debt menu, ship swap, S-mod removal, artifact exchange, Throne's Gift automation | Their own commands in `dialogue/rules` and their managers | Services, not quests. Their text moves to rows; they may use `QuestRewards`-style receipts through vanilla helpers. |
| Hellspawn battle logic | The two fleet encounter classes in `starts/hellspawn` | Hellspawn's state, stages and text use the framework; the battle classes keep their Java and take lines from rows with `FireBest`. |
| Enigma greetings and patrol rows | Faction dialogue in `rules.csv` | Not quest content. |

## Maintenance

- Update this file in the commit that changes a framework signature, hook, verb, key, trigger pattern or service. Keep the [Status](#status) table current.
- When a task adds a verified engine fact that rows depend on, put it in the rules guide that owns it and link it from here.
- Keep examples compilable against the current signatures.
- This file is the source for a future quest-authoring skill: keep procedures in [Making a quest](#making-a-quest) and contracts in [Reference](#reference), each fact in one place.
