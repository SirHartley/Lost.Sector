# Kesteven questline

The main quest: four jobs for the Kesteven Corporation, numbered 1, 3, 4 and 5, that end with the player deciding who receives the Unlimited Production Chip (UPC). This page describes the current flow, its gates and its owners. Saved keys are in [KESTEVEN_STATE.md](KESTEVEN_STATE.md); where each conversation lives is in [KESTEVEN_DIALOGUE.md](KESTEVEN_DIALOGUE.md).

Java paths are relative to `jars/src/lostsector/campaign/`; `dialogue/rules/` and `combat/` paths are relative to `jars/src/lostsector/`. Times given as seconds are frame seconds; a campaign day is 10 seconds.

## Owners

| Owner | Role |
|---|---|
| `# KESTEVEN QUESTLINE` rows in `data/campaign/rules.csv`, `kesteven/quest/KestevenHubModule` | Every conversation with Jack, Alice and Nicholas: offers, briefings, hand-ins, rewards, questions, the job 3 refusal, the story skip and most player-driven stage changes; the job gates and payouts ([dialogue map](KESTEVEN_DIALOGUE.md#jack-alice-and-nicholas)) |
| `kesteven/quest/QuestStageManager` | `EveryFrameScript` in `EFS_LIST`: automatic stage changes, failure checks, intel, bar events, quest fleets and their AI, the Cache guardian timer, Eliza relocation, post-quest revenge fleets |
| `kesteven/quest/KestevenQuest`, `KestevenStage`, `KestevenFlag`, `KestevenState` | Framework definition of quest `kq`, with the modules `KestevenHubModule`, `KestevenJob1Module`, `KestevenJob3Module`, `KestevenJob4Module`, `KestevenJob5Module`, `KestevenGlacierModule`, `KestevenElizaSearchModule` and `KestevenSatelliteModule`; the stage enum, the flags and the saved state ([KESTEVEN_STATE.md](KESTEVEN_STATE.md)) |
| `kesteven/quest/KestevenJob1Module`, `# KESTEVEN QUESTLINE: JOB 1` rows in `data/campaign/rules.csv` | Job 1 world logic: the intel entry and its text rows, the tip system's dormant fleet, the move to stage 2 ([Job 1](#job-1-enemy-unknown-stages-0-to-6)) |
| `kesteven/quest/KestevenElizaSearchModule`, `# KESTEVEN QUESTLINE: ELIZA SEARCH` rows | The search for Eliza at pirate bars during stage 16 ([Finding Eliza](#finding-eliza)) |
| `kesteven/quest/KestevenJob3Module`, `# KESTEVEN QUESTLINE: JOB 3` rows | Job 3 world logic: the expedition and its outcome, the intel entry and its text rows, and the objects placed when the job is accepted ([Job 3](#job-3-hostile-takeover-stages-6-to-11)) |
| `kesteven/quest/KestevenJob4Module`, `# KESTEVEN QUESTLINE: JOB 4` rows | Job 4 world logic: the wait, the intel entry and its text rows, the strike group, the Special Operations fleet and its conversation, the splinter patrols, the hint wreck, completion and failure ([Job 4](#job-4-operation-lifesaver-stages-11-to-14)) |
| `kesteven/quest/KestevenJob5Module`, `# KESTEVEN QUESTLINE: JOB 5` rows | The Delve meeting at the bar, its escort guard, and the job 5 intel entry and its text rows ([Briefing and meeting](#briefing-and-meeting)) |
| `kesteven/quest/KestevenGlacierModule`, `# KESTEVEN QUESTLINE: GLACIER` rows | The Glacier comms facility: its map marker and dialog claim, the timed raid, the barrage's fleet damage and disk #5 ([Glacier](#glacier-disk-5)) |
| `kesteven/quest/KestevenSatelliteModule`, `# KESTEVEN QUESTLINE: SATELLITES` rows | The data-disk satellites' dialog, salvage and woken guards, `ALL_DISKS_RECOVERED`, and `FROST_FOUND` on entering Frost ([The five data disks](#the-five-data-disks)) |
| `kesteven/quest/QuestHelper` | Wrappers over `KestevenState` for the old callers: the stage as a legacy int, flags, fields and lazily picked target locations; `saveEnding()` |
| `kesteven/quest/KestevenFleets` | Builders for every quest fleet |
| `kesteven/quest/KestevenElizaModule`, `# KESTEVEN QUESTLINE: ELIZA` rows | The meeting at Eliza's port ([Eliza's port](#elizas-port)) |
| `CorePlugin` | Opens the Java quest dialogs when the player interacts with a quest entity, deciding through `KestevenQuest` queries |
| `kesteven/quest/*Dialog`, `kesteven/quest/*BarEvent` | Java dialogs and bar events |
| `dialogue/rules/nskr_ttCollectorDialog`, `nskr_elizaInterceptDialog`, `nskr_altEndingDialogLuddic`, `nskr_altEndingDialogTT`, `nskr_isKStage`, `nskr_isAtLeastKStage` | Rules commands for the fleet conversations, endings and stage predicates |
| `kesteven/quest/ElizaRaid`, `ElizaRaidObjectiveCreator` | Ground raid for Eliza's disks |
| `world/systems/cache/Cache` | The Cache system, guardian fleet and its fleet-interaction config |
| `kesteven/quest/CacheIntel` | Intel entry of the Cache; they read the stage and flags and never write the stage |
| `kesteven/ExileManager` | Moves the quest people between Asteria and the Outpost |

## Where the quest is offered

The questline runs at `helper/SectorLookup.asteriaOrOutpost()`: Asteria (`nskr_asteria`), or the Outpost (`nskr_outpost`) when Kesteven is exiled or Asteria was never generated. Jack Lapua (`nskr_opguy`), Alice Lumi (`nskr_researcher`) and Michael Roux (`nskr_president`, administrator) live there; Nicholas Antoine (`nskr_intelligence`) works at the Outpost.

The rules row `nskr_kq_hubChatOption` adds "Chat about operations work" to a person's options when all of these hold, checked in this order:

- the person has the `k_quest` tag (Jack, Alice, Nicholas);
- `nskr_quest kq check hubOpen`: the market belongs to Kesteven and the player's Kesteven relationship is above -0.50;
- the questline has not ended (flag `ENDED`);
- the stage is neither `COMPLETED` nor `FAILED`.

At stage 20 and after failure the option disappears.

Each job has a relationship gate and, from job 3 on, a fleet-strength gate. Strength is `KestevenHubModule.fleetPower()`: `PowerLevel.get(0.2f, 0f, 2f)`, or 2 in dev mode. The rows read the gates through the `KestevenHubModule` checks `job1Standing` … `job5Standing`, `job3Fleet` … `job5Fleet` and `fleetStretched`, which compute the strength each time a row tests them.

| Job | Offered by | Kesteven relationship | Strength |
|---|---|---|---|
| 1 Enemy Unknown | Jack | at least 0.20 | none |
| 3 Hostile Takeover | Jack, then Alice | at least 0.40 | above 0.65 |
| 4 Operation Lifesaver | Alice, after a 30-day wait | at least 0.60 | above 0.80 |
| 5 The Delve | Jack | at least 0.80 | above 0.95 |

When the relationship gate passes and only the strength gate fails, the offer shows a 1-story-point option, "I believe you'll find me more than capable." (menu rows with `SetStoryOption`, handled by `nskr_kq_hubReqSkip`, or `nskr_kq_hubReqSkipJob4` at stage 11). Spending it bypasses the strength gate and shows the job's briefing:

- job 3 (stage 6): Jack sends the player to Alice, whose offer has no strength gate;
- job 4 (stage 11): Alice's briefing, and `JOB4_REQUIREMENT_SKIPPED` keeps her offer open on later visits;
- job 5 (stage 14): Jack's briefing, which sets stage 15.

## Stages

The stage is a `KestevenStage` on the quest state, changed only by the quest manager. The old code reads and writes it as the legacy integer below through `QuestHelper.getStage/setStage`; [KESTEVEN_STATE.md](KESTEVEN_STATE.md#stages) maps each integer to its constant. Stages 3 to 5 are unused. There is no Job 2; the job numbers follow the old stage numbering.

| Stage | Meaning | Set by |
|---|---|---|
| 0 | Not started | Start stage; the quest manager creates the state on the first unpaused frame of a new campaign |
| 1 | Job 1 active | Jack, accept |
| 2 | Job 1 tasks done | `KestevenJob1Module`, when both deliveries are recorded |
| 6 | Job 3 offered by Jack | Jack, job 1 turn-in |
| 7 | Talk to Alice | Jack, job 3 briefing |
| 8 | Job 3 active | Alice, accept |
| 9 | Expedition target known | The job 3 party, leaving it |
| 10 | Job 3 over, success or failure | `QuestStageManager`: target destroyed, timeout, or stealth broken |
| 11 | Job 4 pending | Alice, job 3 turn-in; also job 3 skip |
| 12 | Job 4 active | Alice, accept |
| 13 | Job 4 done | `KestevenJob4Module`, friendly fleet found and strike group destroyed |
| 14 | Job 5 offered by Jack | Alice, job 4 turn-in; attacking the friendly fleet also sets 14 |
| 15 | Go to the bar | Jack, while showing the job 5 briefing |
| 16 | Job 5 active: data disks | Leaving the Delve meeting (row `nskr_kq_delveLeave`) |
| 17 | Cache location known | Alice after all disks; `QuestStageManager` on entering the Cache system at stage 16; story skip |
| 18 | Cache guardian defeated | `Cache.CacheGuardInteractionConfig` when no prototypes remain |
| 19 | Player holds the UPC | `CacheCoreDialog` salvage |
| 20 | Completed | Any of the four ending dialogs |
| 99 | Questline ended by failure | `QuestStageManager` failure checks |

## Job 1: Enemy Unknown (stages 0 to 6)

Jack offers two tasks for 155,000 credits:

1. Win a battle against an Enigma fleet while destroying at least one ship. `KestevenJob1Module.onEncounterLoot` takes every loot screen whose losing fleet is Enigma, adds the player's contribution share once for each of that fleet's ships that was not left intact, and sets `JOB1_SENSOR_DATA` when the sum reaches 1. It then sends the intel update `sensorData` ("You managed to gather sufficient data in battle for the task. Deliver it back to …") with the minor message sound the old campaign message played; every such win at stage 1 sends it again, delivered or not.
2. Deliver 70 Artifact Electronics (`nskr_electronics`).

The tip system is picked once by `QuestHelper.getJob1Tip()`: from the `KestevenHubModule` action `pickJob1Tip` when the player opens Jack's questions at stage 0, or his menu at stage 1 with neither task done and no tip given, and from `KestevenJob1Module` when stage 1 starts. The pick chooses a system with an Enigma base and places a dormant Enigma fleet there, which the quest adopts as role `job1Dormant` (`FleetOrders.none()`, persistent, so it outlives the job). Jack gives the system through "How am I supposed to find them?": a question at stage 0, a menu option leading to the briefing at stage 1. Either sets `JOB1_TIP_GIVEN`.

Jack takes each delivery when the player has it (`JOB1_DATA_DELIVERED`, `JOB1_ELECTRONICS_DELIVERED`). `KestevenJob1Module` then moves stage 1 to 2: its action `job1Progress` does it at once, and its daily tick catches a delivery no row reported. The hand-in rows and the tip row call `job1Progress` right after setting their flags, so the move and the new map marker after the tip happen at once. Turning in at stage 2 grants a Kesteven hullmod modspec (an unknown one of `nskr_inertial`, `nskr_volatile`, `nskr_bigBats`, `nskr_criticalArmor` if possible), 155,000 credits, Kesteven +5 and Jack +10, and sets stage 6.

Winning against Enigma the same way before accepting (stage 0) sets `FOUGHT_ENIGMA`, which changes one of Jack's answers.

### Intel

`KestevenJob1Module` is active at stages 0, 1, 2 and 6. When stage 1 starts it shows the `QuestIntel` entry `job1` (icon `job1`; tags important, accepted, missions; sort tier 2, major posting sound, Kesteven UI colours, delete button once completed) as a campaign message. When the turn-in reaches stage 6 it completes the entry, which sends a completion update and stays for the vanilla end delay of three days; on failure or a jump from stages 1 and 2 it ends the entry at once. Its map marker is the tip system's hyperspace anchor once the tip was given and the system still has an Enigma base, otherwise `asteriaOrOutpost`; the module sets it when stage 1 starts, in `job1Progress` and once a day.

The text is in the `# KESTEVEN QUESTLINE: JOB 1` block, selected by `$nskr_intel_key == job1`: the title "Enemy Unknown" (`nskr_kqIntelTitle`), one bullet per open task (`nskr_kqIntelBullets`) and the description (`nskr_kqIntelDesc`). The entry is declared with `descriptionBullets()`, so the description panel shows the same bullets after its paragraph, as the old `EnemyUnknownIntel` did. Completed, the entry shows "You managed to complete the job." and "Mission complete." (`$nskr_intel_status == completed`). The old highlighted phrases are `SetTextHighlights` lines in the rows; the sensor update's phrase is yellow (`yellowTextColor`), and the other bullets stay out of that message through `$nskr_intel_update != sensorData`. The rows use the module's checks `kestevenHostile` (Kesteven relationship at most -0.50) and `job1TipBase` (the tip system has an Enigma base), the tokens `homeName` (`asteriaOrOutpost` name, also used by the job 3 rows) and `job1ArtifactCount` (`nskr_kestevenQuest.JOB1_ARTIFACTS`), and the hub's `job1TipSystem`.

## Job 3: Hostile Takeover (stages 6 to 11)

At stage 6 Jack sends the player to Alice and mentions the artifact exchange (`nskr_shipSwap`, available from stage 7 through a research official). Continuing makes Jack a potential contact and sets stage 7.

Alice's briefing at stage 7: a Tri-Tachyon expedition leaves from the job 3 start market, a random Tri-Tachyon market other than `eochu_bres` and `culann`. The player must destroy it without being identified, within about 90 days, for 205,000 credits. Accepting sets stage 8.

When stage 8 starts, `KestevenJob3Module` (active at stages 8, 9 and 10), in this order:

- picks the start market and the job 3 target, a random location in a system near the core, if they are not picked yet;
- shows the intel entry `job3`;
- places a dormant Enigma fleet at the target. It is not a quest fleet until satellite #3 is salvaged, when `KestevenSatelliteModule` adopts it ([The five data disks](#the-five-data-disks));
- spawns the "Expedition" fleet (Tri-Tachyon, `KestevenFleets.job3Expedition`) at the start market as role `job3Expedition`, with the target as `FleetInfo.target`;
- places data-disk satellite #3 at the target;
- starts the timer `KestevenState.TIMER_JOB3`.

The expedition's role runs `FleetOrders.expedition(10, 70)`: it prepares at home for 10 days, travels to the target, orbits it until day 70, then returns home and stands down.

`KestevenJob3Module` ends the job at stage 10 in one of three ways, each while the stage is 8 or 9:

- the expedition falls below 20% of its spawn strength after a battle it took part in (`onBattle`), or is destroyed (`onFleetGone`): success, and the fleet is no longer marked important;
- the timer passes 90 days, checked once a day (`onDay`): `JOB3_FAILED`, and derelicts are left at the target (`QuestHelper.spawnEnvironmentalStorytelling`);
- the player contributes to a fight the expedition loses while it has seen the player's transponder on, checked at the loot (`onLoot`), which comes before the battle report: `JOB3_FAILED` ("failed to neutralize the fleet stealthily").

At stage 10, or when the module stops earlier (failure or a jump), the expedition moves to the persistent role `job3ExpeditionOver` (`FleetOrders.withdraw()`): it keeps its last assignment and despawns once out of the player's hyperspace sensor range.

### The party

A rules bar event at the start market, in the `# KESTEVEN QUESTLINE: JOB 3 PARTY` block, run by `KestevenPartyModule` (active at stages 8 and 9). The `AddBarEvents` row `nskr_kq_partyBlurb` adds the blurb and the option "Approach the employee" at stages 8 and 9 while neither `JOB3_TARGET_DISCOVERED` nor `JOB3_PARTY_DECLINED` is set and the module's check `partyHere` finds the bar's market to be the start market. It shows at every visit, with no random pick or timeout.

When stage 8 starts, the module creates seven Tri-Tachyon quest people: the employee (`partyEmployee`, either gender, post `genericMilitary`), the two techies (`partyEngineer`, male, post `kTechEngineer`; `partyEntrepreneur`, male), the host of the toast (`partyHost`, female, base commander) and the three officers (`partyPatrolCommander`, male; `partyFleetCommander` and `partyAgent`, either gender). It releases them when the module stops.

The scene is a drinking conversation with the employee, who is the active speaker from the first click (`BeginConversation nskr_kq_partyEmployee true false`); the group screens show the other guests' portraits with `ShowPersonVisual`, `ShowSecondPerson` and `ShowThirdPerson`.

- **Declining.** Leave, offered after the refused second round and at the party invitation, ends the event and sets `JOB3_PARTY_DECLINED`, so it never shows again; the player must then find the expedition another way.
- **Drinks.** The state counts drinks in `partyDrinks` (action `partyDrink <n>`): one for each drink at the party except the "Absynth", which counts two, and one for the toast of the second group when the player already had a drink. Checks `partyTipsy` (at least one) and `partyDrunk` (at least two) read it.
- **Groups.** After the arrival the player picks from three groups (a `FireAll` menu on `nskr_kqPartyGroups`); the employee's memory marks each one visited (`$nskr_kq_partyTechiesDone`, `$nskr_kq_partyCrowdDone`, `$nskr_kq_partyOfficersDone`). Only the officers reveal the target system; after them the menu offers Leave.
- **Leaving.** The employee offers a last drink. Sober (`partyDrunk` false), the player can leave: stage 9, `JOB3_TARGET_DISCOVERED` and the receipt "Acquired Expedition coordinates". Drunk, only drinking options remain; the hangover chain ends with the action `partyHangover`, which takes 4,000 to 7,000 credits (random `partyHangover`, capped by the credits held; vanilla loss receipt) before the same stage change, flag and receipt. Accepting the last drink while sober also leads to the hangover.

Every exit returns to the bar with `BarCMD returnFromEvent true`, which shows Continue first, as the old Java event did.

### Intel

The `QuestIntel` entry `job3` (icon `job3`; tags important, accepted, missions; sort tier 2, major posting sound, Kesteven UI colours) shows as a campaign message when stage 8 starts. When the job ends, the module moves the map marker to `asteriaOrOutpost` and completes or fails the entry with the update `done`, `timeout` or `stealth`, the old campaign message as a bullet with its yellow highlights and the minor message sound; the entry then stays for the vanilla end delay of three days, with a delete button. On failure of the questline or a jump from stages 8 and 9 the entry ends at once. Its map marker is the start market at stage 8 and the target at stage 9. The text is in the `# KESTEVEN QUESTLINE: JOB 3` block, selected by `$nskr_intel_key == job3`: the title "Hostile Takeover", bullets for hostile relations (`kestevenHostile`), the days left (check `job3TimeLeft`, token `job3DaysLeft`: 90 minus the timer's days, cut to whole days) and the current step (tokens `job3HomeMarket`, `job3HomeSystem`, `job3TargetSystem`), and the description, which shows the bullets after its own paragraph (`descriptionBullets()`). Finished, the entry shows the old stage 10 lines while the stage is 10 and the old later lines after that, for success (`$nskr_intel_status == completed`) and failure (`failed`); those rows stay out of the finishing message (`$nskr_intel_mode != update`). The old highlighted phrases are `SetTextHighlights` lines.

Alice's turn-in at stage 10: on success, a modspec, 50,000 exchange points, 205,000 credits, Kesteven +5 and Alice +10; on failure, Kesteven -5 and Alice -10. Both set stage 11.

Refusing at stage 7 ("I'm not doing this.", then "yes") costs Kesteven -5 and Alice -10 and sets stage 11 and `JOB3_REFUSED`. The derelicts, satellite #3 and the dormant fleet are still placed at the target so job 5 can use them. The rows are `nskr_kq_aliceAskRefuseSel` (the prompt) and `nskr_kq_aliceRefuseConfirm`, whose action `placeJob3Leftovers` places the leftovers.

## Job 4: Operation Lifesaver (stages 11 to 14)

`KestevenJob4Module` is active from stage 11 to stage 16, because the job's fleets stay in the world after the job. When stage 11 starts it starts the timer `job4Wait`; its daily tick sets `JOB4_WAIT_OVER` once the timer passes 30 days. The flag is set on the first daily tick after the 30 days. After the wait, Alice offers the job when Kesteven relationship is at least 0.60 and either strength is above 0.80 or `JOB4_REQUIREMENT_SKIPPED` is set. Pay is 285,000 credits. Her briefing names the constellation of the friendly fleet's location, a random point in a system far from the core. If the Outpost belongs to Kesteven she also points to Nicholas. Accepting sets stage 12.

When stage 12 starts, `KestevenJob4Module`, in the order the old code used, which keeps the questline's shared random sequence:

- shows the intel entry `job4`;
- spawns the Enigma "Strike Group" (flagship "DSRD Eye for an eye"), role `job4StrikeGroup`, and records its location as the job 4 enemy target;
- places data-disk satellite #4 at the enemy target;
- spawns the Kesteven "Special Operations" fleet at the friendly target (transponder off), role `job4SpecialOps`;
- spawns ten Enigma "Splinter" patrols, role `job4Splinter`;
- places a debris field and three Kesteven derelicts near the enemy target. The first derelict is the hint wreck; its dialog is claimed with the trigger `nskr_kqHintWreck` in every stage until it is read.

The builders are `KestevenFleets.job4StrikeGroup`, `job4SpecialOps` and `job4Splinter`. Every job 4 role is persistent and has `FleetOrders.withdrawWhen`: from stage 17 on (failure included), or from stage 14 on once satellite #4 is salvaged, the fleet keeps its last assignment and despawns once out of the player's sight. Until then:

- the splinters and the Special Operations fleet keep their spawn assignment (`FleetOrders.keep()`);
- the strike group also keeps it, and after chasing the player out of the player's location it stays aggressive to the player and patrols its home system again ("unknown", `FleetOrders.patrolHomeAfterChase`). Salvaging satellite #4 sends it after the player (`KestevenSatelliteModule` action `wakeSatelliteGuard`, which finds it by its role). Below 20% of its spawn strength it withdraws.

Three sources lead the player on:

- Nicholas describes a burst of signals from the enemy target system and records his dialogue stage.
- The hint wreck gives the friendly fleet's system (rows `nskr_kq_hintWreck*`; the action `readHintWreck` sets `JOB4_HINT_WRECK_READ` and releases the claim, so the vanilla derelict dialog opens from then on).
- The Special Operations fleet (transponder must be on) tells its story, sends the strike group coordinates, and asks for 250 supplies and 400 fuel. Its first conversation runs the action `recordJob4FleetTalk`: `JOB4_FRIENDLY_TALKED`, `JOB4_FRIENDLY_FOUND`, and `JOB4_TARGET_HINT` unless the strike group was already seen or beaten. Giving the supplies and fuel sets `JOB4_FRIENDLY_HELPED`, and the action `sendJob4FleetHome` moves the fleet to role `job4SpecialOpsLeaving`, which flies to `asteriaOrOutpost` ("travelling back to <market>") and despawns there.

The `job4` entry lists each lead the player has. Its map marker points at the most precise one: the strike group once seen or once the Special Operations fleet sent its coordinates; else the friendly fleet's coordinates from the hint wreck while that fleet is not found; else the system Nicholas named; else the found friendly fleet; else the briefing's constellation. The strike group leads apply only until it is destroyed. The module sets the marker when the job starts, whenever one of its own flags changes and once a day, so Nicholas's tip moves it on the next daily tick.

Seeing the friendly fleet or talking to it sets `JOB4_FRIENDLY_FOUND`; seeing the strike group before it is beaten sets `JOB4_TARGET_FOUND`. Sightings come from `onFleetDetected` (vanilla `DetectedEntityListener`), which reports each change of the player's view of a quest fleet. Reducing the strike group below 20% of its strength, or destroying it, sets `JOB4_TARGET_DESTROYED` and removes its mission-important marker (`onBattle`, `onLoot`, `onFleetGone`). With both flags set, the module sets stage 13 on the next unpaused frame, so a conversation that sets the last flag completes the job after it closes.

If anything other than the player destroys the Special Operations fleet before it is found, the module spawns a new one at the friendly target. It checks once a day at stage 12 and waits until the player is out of hyperspace sensor range of the friendly target's system.

If the player wins a fight in which the Special Operations fleet (either role) is the losing fleet and loses a ship, with any player contribution (`onLoot`), the module sets `JOB4_FAILED`, moves the stage to 14 and then, unless `ENDED` is already set, to 99 with `ENDED`, which ends the questline. The check runs while the module is active, up to stage 16.

### Intel

The `QuestIntel` entry `job4` (icon `job4`; tags important, accepted, missions; sort tier 2, major posting sound, Kesteven UI colors, delete button once finished, bullets in the description) shows as a campaign message when stage 12 starts. The text is in the `# KESTEVEN QUESTLINE: JOB 4` block, selected by `$nskr_intel_key == job4`: the title "Operation Lifesaver", the lead bullets, and the description, which names the search area (token `job4SearchArea`) at stage 12 and "Return to <home>." at stage 13. The module's checks `job4FriendlyNamed` and `job4TargetNamed` pick the bullet variants that name the orbited entity, as the old entry did for entities not named "Null".

When stage 13 is reached the module sets the marker to `asteriaOrOutpost` and completes the entry with the update `done` ("With the threat eliminated and the Operations fleet located, you can report back to <home> to finish the job."), and on failure fails it with the update `failed` ("You attacked the Special Operations fleet. Mission failed, better not to talk to anyone about this."), each with `ui_intel_minor_message`. A completed entry ends after the vanilla delay; the module ends the entry at once when it stops (stage 17 or failure). A failure attack at stages 14 to 16 sends no message, because the entry is no longer shown there.

Alice's turn-in at stage 13 grants 1 story point, 285,000 credits, a modspec, Kesteven +5 and Alice +10. If the player helped the fleet it also grants an Epoch-class prototype frigate (`nskr_epoch_empty`). Alice becomes a potential contact, Jack's importance rises to high, and S-mod removal (`nskr_modRemoval`) opens at research officials. Stage becomes 14.

## Job 5: The Delve (stages 14 to 19)

### Briefing and meeting

At stage 14 Jack's briefing tells the player to go to the bar; showing the briefing sets stage 15 directly.

At stage 15 the rules row `nskr_kq_delveBar` adds the bar event "Give the signal to the guard to take you to the meeting" (in the highlight colour) at every visit to the bar of `asteriaOrOutpost` (`KestevenJob5Module` check `delveMeetingHere`). It is the in-person meeting with Jack and Alice:

- the guard escorts the player; with Asteria in the sector the scenes are Asteria's (the planet, the underground city, a skyscraper), otherwise the Outpost's (the guard's card, the office modules);
- Jack offers a drink; either answer leads on;
- they explain the pre-collapse satellite network, the data disks and the Cache, with four optional questions;
- "I already have some of those disks." appears once a satellite was salvaged;
- if the player hesitates ("I'm not so sure about this."), Jack offers a 150,000-credit advance, paid with the vanilla credits receipt when the player agrees;
- Eliza is introduced as the holder of one disk; "Ah yes, that "LZ" character." appears while `MESSENGER_QUESTION_OPEN` is set.

Leaving prints "Acquired log entry for The Delve", sets stage 16 and returns to the bar with a Continue option. If the player had already entered the Cache system (`CACHE_FOUND`), the meeting offers only "I think I already found that place.", a shorter branch that marks the Cache command core when the guardian is already beaten and still sets stage 16; `QuestStageManager` then moves to 17.

The guard is the quest person `delveGuard` (`nskr_kq_delveGuard`: Kesteven, male, military post, portrait `nskr_guard`), created when stage 15 starts and released when it ends. Jack becomes the speaker when the meeting starts (`BeginConversation nskr_opguy true false`), with Alice as the second portrait; the answered questions and the advance offer are expiry-0 keys in Jack's memory ([memory flags](KESTEVEN_STATE.md#memory-flags)). The rows are the `# Meeting` part of the `# KESTEVEN QUESTLINE: JOB 5` block ([dialogue map](KESTEVEN_DIALOGUE.md#the-delve-meeting)).

### Intel

`KestevenJob5Module` is active from stage 15 to 20. When stage 16 starts, or 17 after the story skip, it shows the `QuestIntel` entry `job5` (icon `job2`; tags story, important, accepted, missions; sort tier 2, major posting sound, Kesteven UI colours, delete button once completed) as a campaign message. At stage 20 it completes the entry, which sends a completion update and stays for the vanilla end delay of three days; on failure or a jump out of stages 15 to 20 it ends the entry at once. The map marker is `asteriaOrOutpost` at stage 16, the centre of Unknown Site at 17 and 18, and at 19 Eliza's market after `ELIZA_AGREED_SINCERELY`, otherwise `asteriaOrOutpost`; the module sets it at each stage and once a day, because the home and Eliza's market move without a callback.

The text is in the `# Intel` part of the `# KESTEVEN QUESTLINE: JOB 5` block, selected by `$nskr_intel_key == job5`: the title "The Delve", one bullet per open step, and a description line per stage ("Keep searching.", "Head to the Cache.", "Recover the Chip.", "Mission complete"), followed by the bullets (`descriptionBullets()`). The bullets read the tips, the satellites, the Frost and Glacier flags, the Eliza search (`delveContactKnown`: search step 2 with a contact market), Eliza's market (`delveElizaLocated`), the disks and the Cache flags; the tokens are `KestevenJob5Module`'s `delveDisks`, `delveJob3Constellation`, `delveJob4Constellation`, `delveContactMarket`, `delveContactSystem`, `delveElizaMarket`, `delveElizaEntity` and `delveElizaSystem`, the hub's `job3TargetSystem`, `job4TargetSystem`, `frostName`, `frostTipConstellation` and `frostTipDistance`, and job 1's `homeName`. The step bullets stay out of the Eliza search's `contactMoved` update (`$nskr_intel_update != contactMoved`). Token values are highlighted, so the systems after the contact market and Eliza's market, the home in the stage 19 turn-in line and the disk count are highlighted too, which the old entry did not do.

### The five data disks

| Disk | Source |
|---|---|
| #1 and #2 | Eliza: given if the player agrees to help her, or taken by raiding her market |
| #3 | Satellite at the job 3 target system (`# KESTEVEN QUESTLINE: SATELLITES` rows) |
| #4 | Satellite at the job 4 enemy target system (the same rows) |
| #5 | Comms facility on Glacier in the Frost system ([Glacier](#glacier-disk-5)) |

The satellites exist from jobs 3 and 4 and can be salvaged at any time, also after the questline. `KestevenSatelliteModule` is active in every stage. `QuestHelper.spawnArtifact` claims each satellite it places for the trigger `nskr_kqSatellite` in every stage; the module's `onStart` claims the satellites that already exist when the quest state is created, which are the two empty ones in Unknown Site (`Cache.generate` marks them through `KestevenQuest.markEmptyDataSatellite`).

The first satellite salvaged, whichever it is, runs the long scene: approach, salvor crew, hidden compartment, anti-tampering device, the disk marked "Project : Enigma", and the comms officer's report, which from stage 16 on (and after failure) calls it the disk they are looking for. The second runs a short scene in which the satellite broadcasts on a loop; its keywords are "Enigma, Glacier, Frozen", Frost's name, "Heart" and the constellation of the system in Alice's distance hint, which the scene picks if Alice has not yet (hub action `pickJob5FrostTip`). A salvaged satellite, and the two in Unknown Site, only say that it is cold and dead. Either salvage (module action `salvageSatellite`) counts the satellite and one disk, marks the satellite empty (`$nskr_artifactKeyEmpty`), sets `SATELLITE3_RECOVERED` or `SATELLITE4_RECOVERED` and removes Alice's map marker; the rows fire a sensor-burst and an interdict ping from the satellite, print "Acquired Data Disk #3" or "#4", and play `ui_rep_raise`, and Leave plays `ui_sensor_burst_on`. Escape leaves every screen except those between the order to take the disk and the disk being taken.

The action `wakeSatelliteGuard` then turns the guard on the player. At #3 it wakes every Enigma dormant fleet in the satellite's location: gives it Emergency Burn, Sensor Burst and Go Dark, a new fleet AI, clears its memory except the dormant flag and `MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE` (`QuestFleets.clearMemory`, which keeps the quest keys of a fleet that is already a quest fleet), makes it role `satelliteGuard` (`adopt`, or `reassign` for a quest fleet such as the job 1 tip system's `job1Dormant`, whose age then restarts) and orders it to intercept the player. The role's `FleetOrders.huntInSystem().withdrawAfter(30)` keeps it intercepting while it sees the player and patrolling its system otherwise; after 30 days it despawns once out of the player's sight. At #4 it orders the job 4 strike group (role `job4StrikeGroup` of `KestevenJob4Module`) to intercept the player when it is in the satellite's location. Its role's `patrolHomeAfterChase` keeps the intercept while it shares the player's location and sends it back to patrol its home system after that; from stage 14 on the salvage also meets the role's `withdrawWhen`, so it keeps the intercept and despawns once out of the player's sight, as the old strike group logic did.

Tips unlock in this order:

1. Jack (`JOB5_JACK_TIP`): find Eliza through pirates.
2. Alice (`JOB5_ALICE_TIP`): where the two satellites are. She marks the one not yet salvaged.
3. Alice again (`JOB5_ALICE_TIP2`), after both tips and at least two satellites, while the disks are incomplete: a tundra planet around a red dwarf with a comms facility, within a stated distance of a named constellation. She marks Glacier.

If the player has visited Frost, answering "It's the Frost." sets `FROST_FOUND`. `KestevenSatelliteModule` also sets it at stage 16 after tip 2 when the player enters Frost (`onLocationChanged`), or when stage 16 starts with the player in Frost. Tip 2 marks Glacier and claims its dialog; the timed raid there gives disk #5.

### Glacier (disk #5)

Alice's Frost tip row runs `nskr_quest kq do markGlacier` after setting `JOB5_ALICE_TIP2`. The action, declared by `KestevenGlacierModule` (active from `JOB5_DISKS` on, `FAILED` included), marks `nskr_glacier` and claims its dialog with the trigger `nskr_kqGlacier`, both scoped to those stages. Interacting with Glacier then opens the rules dialog of the `# KESTEVEN QUESTLINE: GLACIER` block instead of the planet's own dialog.

The first screen offers "Search for the facility" and Leave; the next offers Continue and Leave. From the landing on, the only way out is to finish. The raid counts down from 90 minutes as a fixed sequence of choices:

| Step | Minutes after |
|---|---|
| Alarm, then the impasse | 90, 75 |
| Door: find a way around / cut through / blast | 45 / 45 / 60 |
| Server room, then the console | 35, 30 (walk or cut); 50, 45 (blast) |
| "Grab the disk and run": way back | 0 (walk), 15 (cut), 0 (blast, cave-in) |
| "Try to shut down the system" | 0 (walk or cut), 15 (blast) |

Reaching 0 is late. A late shutdown takes the barrage at the console; a late run takes it while the shuttle returns. The barrage damages up to four ships that are not fighters and are at least a quarter repaired: each loses 25 to 50% of its hull and 25 to 50% CR (`damageFleet`, random purpose `glacierCommsKeyRandom`), with one line per ship. Only the cut-and-run and the blast-and-shutdown paths escape undamaged; they get the ops chief's congratulations and the extra "I know, I'm the best." option. Every path ends with `recoverGlacierDisk`: `GLACIER_DISK_RECOVERED`, one more recovered disk, the marker and claim removed, and "Acquired Data Disk #5".

### Finding Eliza

`KestevenElizaSearchModule`, active at stage 16, runs three rules bar conversations in the `# KESTEVEN QUESTLINE: ELIZA SEARCH` block of `data/campaign/rules.csv`. They share a search step (`KestevenState.elizaSearchStage`, 0 to 3) and a list of used markets (`elizaSearchUsedMarkets`). Each `AddBarEvents` row shows at a pirate market whose bar the search has not used yet, at stage 16 and at its step. The speakers are the quest people `roughSpacer`, `slySpacer` and `pirateContact` (pirates, created when stage 16 starts, released when it ends); each is the same person at every bar.

| Conversation | When | Result |
|---|---|---|
| First spacer (`nskr_kq_elizaSpacer…`) | Step 0 | A rough spacer. Opening it rolls a price of 4,000 to 7,000 credits (`elizaSpacerPrice`, action `elizaSpacerOpen`); the offer to pay shows only while the player has more credits than that. Paying names a contact market (`elizaContactMarket`, picked like Eliza's market; marked with `ctx.mark` for stage 16), sets `ELIZA_SPACER_PAID` and step 2. Leaving without paying sets step 1. Either way the market is used. |
| Second spacer (`nskr_kq_elizaSly…`) | Step 1 | A sly spacer who refuses to talk whatever the answer; leaving uses the market and sets step 2 |
| Contact (`nskr_kq_elizaContact…`) | Step 2; only at the contact market if the player paid, with another option label | "Eliza herself wants to speak to you." Uses the market, picks Eliza's market (`QuestHelper.setElizaLoc()`), and on leaving sets `ELIZA_FOUND` and step 3, unmarks the contact market and sets the plain `$missionImportant` flag on Eliza's market, which the old Eliza classes clear |

Every conversation ends with `BarCMD returnFromEvent true`, the Continue option the old Java bar events showed. If the contact market decivilizes at step 2 before Eliza is found, `onDecivilized` picks a new contact market, moves the mark and sends the Delve intel the update `contactMoved` ("With the conditions deteriorating on …, the contact has moved their operations to …." with "the contact" in the pirate faction color and the minor message sound).

Eliza's market is a pirate market in Yma, Corvus, Isirah, Thule, Hybrasil, Galatia, Mayasura or Kumari Kandam, excluding Kanta's Den and used markets. If that market decivilizes from stage 16 on (failure included) after `ELIZA_FOUND` and before `ELIZA_KILLED`, `KestevenElizaModule.onDecivilized` picks another with the same picker and random, adds Eliza to its comm directory and people when she exists, restores a contact lost to the decivilization to priority, and sends the Delve intel the update `elizaMoved` ("With the conditions deteriorating on <old entity>, Eliza has moved her operations to <new market>.", "Eliza" in the pirate colour, minor message sound) while that entry is shown. The old campaign message came at any stage from 16 on.

### Eliza's port

Until the meeting has finished once (`ELIZA_DIALOG_FINISHED`), the rules row `nskr_kq_elizaPort` takes over the dialog of Eliza's market: it matches `OpenInteractionDialog` with the check `elizaPort` (the flag unset and `KestevenQuest.atElizaMarket(target)`) at `score:10000`, above every vanilla market opening, so the market menu does not show. `KestevenElizaModule` is active in every stage, as the old `CorePlugin` route tested only the flag and the market. Agreeing to meet runs the action `elizaMeet`, which makes Eliza (`SectorGen.genEliza()`, id `nskr_anarchist`) unless she exists; her card shows from the office on (`ShowPersonVisual false nskr_anarchist`). The rows are in the `# KESTEVEN QUESTLINE: ELIZA` block; "Leave" on the first screen and after the hand-over is `defaultLeave`.

- **Agree, sincerely:** disks #1 and #2 (action `elizaHandOver`, which also clears the market's `$missionImportant`), `ELIZA_HELPED`, and `ELIZA_AGREED_SINCERELY`.
- **Agree while lying:** disks #1 and #2 and `ELIZA_HELPED` only.
- **Refuse:** `ELIZA_RAID_ENABLED`. The action `elizaEnableRaid` registers `ElizaRaidObjectiveCreator` with the listener manager (saved), which then adds an extreme "Data Disks" raid objective at her market. The raid grants disks #1 and #2 and 30,000 to 40,000 credits, removes Eliza from the market, and spawns her "Merc Armada", which hunts the player. Destroying her flagship sets `ELIZA_KILLED` and removes her from important people.

Either way the meeting ends by adding Eliza to the market's comm directory and people (action `elizaToPort`), so she can be contacted there afterwards.

### Reaching the Cache

At stage 16 the disk count sets `ALL_DISKS_RECOVERED` once it reaches five: `KestevenSatelliteModule.checkAllDisks` runs where the count changes (the satellite salvage, the Glacier disk, and `QuestHelper.setDisksRecovered` for Eliza and her raid) and when stage 16 starts. Alice's all-disks conversation offers "Yes" and, if the player sincerely agreed to help Eliza, "Yes (lie)". Both set `CACHE_FOUND` and stage 17 and hand over the Cache coordinates. The Cache system "Unknown Site" is reached by a transverse jump.

Entering Unknown Site at stage 15 or 16 sets `CACHE_FOUND` without any disks, and stage 16 becomes 17. The Eliza search runs only at stage 16, so a player who reaches the Cache early can skip Eliza and the remaining disks.

Inside Unknown Site, `QuestStageManager`:

- adds `CacheIntel` once the Cache is found;
- after 35 seconds, picks the guardian's location and shows `CacheDoubtDialog` once (a hint, stage 16 or later);
- sends sensor-burst pings toward that location from 45 seconds on;
- at 90 seconds starts the Cache music and spawns the guardian fleet (commander "Enigma Fragment #1", flagship "DSRD Epicenter") through `Cache.spawnGuardianFleet`.

During the battle `combat/plugins/CacheBossTauntPlugin` posts taunts, plays the boss theme and spawns a second boss ship.

When the fight ends with no prototype ships left, `CacheGuardInteractionConfig.notifyLeave`:

- builds the rest of the system (`spawnEverything`) and the wrecks;
- sets stage 18 if the questline is active;
- creates the command core `nskr_cache_core` and switches to `CacheCoreDialog`.

Salvage reports the UPC, grants one Alpha Core and 50 to 100 Artifact Electronics, and sets stage 19. The UPC is not a cargo item; the stage and flags stand for it. The return target is marked: Eliza's market if the player sincerely agreed to help her, otherwise `asteriaOrOutpost`.

## Stage 19: who receives the Chip

All four endings set stage 20 and call `QuestHelper.saveEnding()`. That turns on the `thronesGiftUnlocked` and `storySkipUnlocked` settings, and `hellspawnUnlocked` while `nskr_starfarerFromStart` is true (TRUE STARFARER difficulty since the start). These LunaLib settings unlock the custom-start backgrounds and the story skip in later campaigns; the player can also switch them in LunaLib's menu.

| Ending | How | Main results |
|---|---|---|
| Kesteven | `CorePlugin` opens `EndingKestevenDialog` at `asteriaOrOutpost` (or the Asteria station) while the UPC was not handed to Eliza | Prototype Light Ships blueprint package, 565,000 credits, 450,000 exchange points, 1 story point, Kesteven +25, Jack and Alice +20 and very high importance; player and Kesteven relations with Tri-Tachyon set to about -0.65 to -0.70; Eliza -75; `nskr_upChip` on the market |
| Eliza | Requires `ELIZA_HELPED`. At stage 19 `QuestStageManager` spawns her fleet once to intercept the player. Handing over the UPC (`nskr_elizaInterceptDialog`) sets the handover flag and caps the player's Kesteven relationship at -0.35; her fleet returns home and she reappears (`ELIZA_RETURNED`). `CorePlugin` then opens `EndingElizaDialog` at her market. | Prototype Weapons and Heavy Ships blueprint packages, 2 story points, pirates at least 0.25 to 0.30, Eliza +30 and contact; Kesteven about -0.80 to -0.90 and Hegemony (and Iron Shell) about -0.65 to -0.70, also between pirates and those factions; Jack and Alice -75 with contacts suspended; `nskr_upChip`, orbital works or a heavy-industry upgrade, and a military base or patrol upgrade on her market |
| Luddic | Admin official at a Luddic Church or Path market (`nskr_altEndingDialogLuddic`): destroy the Chip | 8 story points, that faction +15, the official +10 and contact; `makeMad`: Jack, Alice and Eliza -50 with contacts suspended, Kesteven about -0.55 to -0.65 |
| Tri-Tachyon | Admin official at a Tri-Tachyon market (`nskr_altEndingDialogTT`): sell the Chip for 2,000,000 credits or a haggled price | Credits, Tri-Tachyon +15, the official +10 and contact, the same `makeMad` fallout; `nskr_upChip` on Culann if Tri-Tachyon holds it, otherwise on this market |

The two alternative endings share one finished flag and are offered only at stage 19 before a handover. If the player refuses Eliza's intercept, her fleet turns hostile. If she has talked to the player but received nothing, her fleet leaves after 60 days and she returns home.

## After the questline

- **Jack's revenge:** at stage 20, after the Eliza, Luddic or Tri-Tachyon ending, `QuestStageManager` rolls 1% per day, once, to spawn Jack's "Task Force" (flagship "K-Corp Homewrecker"). Jack is removed from his market and from important people (`JACK_GONE`).
- **Eliza's revenge:** after the Eliza ending, if the player's faction takes Eliza's market, `ELIZA_BETRAYED` spawns her fleet to hunt the player.
- **Relationship caps:** after the Eliza ending, `QuestStageManager.reportPlayerReputationChange` keeps Kesteven at or below -0.50 and the Hegemony and Iron Shell at or below -0.35, adjusted for Nexerelin's maximum relationship.
- **Commission fix:** if the player held a Kesteven, Hegemony or Iron Shell commission at the Eliza ending, `QuestStageManager` waits 30 frames and re-applies the saved relationship values.

## Failure

`QuestStageManager` sets stage 99 and `ENDED` when:

- the Outpost cannot host Kesteven and Asteria does not exist;
- stage is 19, the UPC was handed to Eliza, and Eliza has been killed (`JOB5_FAILED`).

`KestevenJob4Module` sets stage 99 and `ENDED` when the player attacks the Special Operations fleet ([Job 4](#job-4-operation-lifesaver-stages-11-to-14)).

The questline option then disappears. The Cache can still be found and fought; `CacheCoreDialog` gives no questline reward in that state.

## Side events

| Event | Owner | When | What |
|---|---|---|---|
| Tri-Tachyon collector | `QuestStageManager`, `KestevenFleets.spawnCollectorFleet`, `nskr_ttCollectorDialog` | Once; stages 2 to 15; the player carries at least 50 Artifact Electronics, is in hyperspace within 25,000 units of the centre; 3% per day | "Black Ops" demands all Artifact Electronics; paying sends it home |
| "LZ" messenger | Quest `ic` ([intercept fleets](CONTRACTS_AND_BOUNTIES.md#intercept-fleets)), rules `nskr_ic_messenger*` in `# INTERCEPTS` | Once; stages 10 to 14 (`KestevenQuest.inMessengerWindow()`); hyperspace near the core; 4% per day | Anonymous warning signed "LZ"; opening its comm link calls `KestevenQuest.reportMessengerMet()`, which unlocks "LZ" questions for Alice and in the Delve meeting |
| Exile | `kesteven/ExileManager` | Daily | If Asteria is lost while the Outpost is Kesteven's, the quest people move to the Outpost and back when Asteria returns |

## Story skip

While the `storySkipUnlocked` setting is on, the speaker menus add a 5-story-point "Skip story" option at stages 0, 6, 7 and 11 (after the job 4 wait), and at stage 14 while a job 5 gate fails (rows `nskr_kq_jackOptStorySkip…`, `nskr_kq_aliceOptStorySkip…`). Its handler rows `nskr_kq_jackStorySkip` and `nskr_kq_aliceStorySkip` print the speaker's lines and fire `nskr_kqStorySkipped`, whose `KestevenHubModule` action `storySkip`:

- places the job 3 objects (satellite #3 and the dormant fleet) at stages up to 7, and the job 4 objects (strike group, satellite #4 and wrecks) at stages up to 11;
- marks every job 5 tip and disk source as done and sets the Eliza help flags, generating Eliza if she has no market yet;
- sets `CACHE_FOUND` and stage 17;
- clears `nskr_starfarerFromStart` and sets `STORY_SKIPPED`.

## Defects found by reading the source

These follow from the code and rules as written. None has been checked in game.

1. **"Yes (lie)" to Alice.** It follows the same path as "Yes" and records nothing; no code reads a lie to Alice.
2. **Operation Lifesaver system fallbacks.** `KestevenFleets.job4StrikeGroup` picks the strike group's system inside the friendly target's constellation. When no other system there has two planets, `QuestHelper.getRandomSystemWithinConstellation` retries without excluding the friendly system, so both fleets can share one system; with no candidate at all it returns null and the spawn fails. `getRandomSystemFarCore`'s fallback can return a system outside any constellation, which the old `OperationLifesaverIntel` did not expect; the `job4` entry then has no constellation marker and an empty search area. Kept as is by the maintainer.
3. **Cache guardian report can move the stage back.** `KestevenQuest.reportCacheGuardianDefeated()` sets stage 18 whenever the questline has not ended and the stage is 16 or later, so a guardian defeat reported at stage 19 or 20 would return the questline to 18. Normal play defeats the guardian before stage 19.
4. **Failure counts as late stages.** The Glacier claim keeps the old route's `stage >= 16` on legacy numbers, so its scope includes `FAILED`, and `cacheIsQuestTarget()` keeps the comparison itself; both hold after failure (legacy stage 99).
5. **Frost guess with all disks.** Alice's confirmations at stage 16 test the disks, not the screen that offered the option. With all five disks, a Frost tip screen (both tips given, tip 2 not yet) offers "It's the <Frost>.", and choosing it gives the Cache coordinates and stage 17 (`nskr_kq_aliceCacheFound`).
6. **Empty screen at stage 16.** Alice's "Continue" with all disks leads to her Cache briefing only when both tips and tip 2 are recorded. With all disks and her tip given but Jack's missing, the briefing option shows only Back (`nskr_kq_hubBrief`).
7. **Highlight without its phrase.** Jack's job 5 briefing highlights "Go to the bar", which its text does not contain, so nothing is highlighted.
8. **Asteria scenes at the Outpost.** The Delve meeting picks its Asteria texts whenever Asteria exists (`asteriaGenerated`), not where the questline is, so after Kesteven's exile the meeting at the Outpost describes Asteria's underground city. The old `DelveMeetingBarEvent` also showed Asteria's planet there; the escort row's `ShowLargePlanet` shows the planet of the dialog target's market instead, if it has one.
9. **Repeated sensor message.** Every Enigma win that counts for the sensor task at stage 1 sends the `sensorData` update again, also after the package was delivered (`KestevenJob1Module.onEncounterLoot`, as the old `QuestStageManager` check did).
10. **Story-skip strike group.** The story skip spawns the strike group, satellite #4 and the wrecks, then sets stage 17, where every job 4 fleet withdraws; the strike group despawns as soon as the player is out of its sight, so satellite #4 is unguarded.
11. **Silent satellite after the story skip.** The story skip counts two satellites without emptying the placed ones, so their dialog shows no text and only Leave (`nskr_kq_satelliteSilent`); the old dialog showed no option at all and left on Escape.
