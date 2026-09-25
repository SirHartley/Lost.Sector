# Kesteven questline

The main quest: four jobs for the Kesteven Corporation, numbered 1, 3, 4 and 5, that end with the player deciding who receives the Unlimited Production Chip (UPC). This page describes the current flow, its gates and its owners. Saved keys are in [KESTEVEN_STATE.md](KESTEVEN_STATE.md); where each conversation lives is in [KESTEVEN_DIALOGUE.md](KESTEVEN_DIALOGUE.md).

Java paths are relative to `jars/src/lostsector/campaign/`; `dialogue/rules/` and `combat/` paths are relative to `jars/src/lostsector/`. Times given as seconds are frame seconds; a campaign day is 10 seconds.

## Owners

| Owner | Role |
|---|---|
| `# KESTEVEN QUESTLINE` rows in `data/campaign/rules.csv`, `kesteven/quest/KestevenHubModule` | The conversation hub with Jack, Alice and Nicholas: entry option, greetings, introductions, status lines, menus, questions and the story point requirement skip ([dialogue map](KESTEVEN_DIALOGUE.md#jack-alice-and-nicholas)) |
| `dialogue/rules/nskr_kestevenQuest` | Briefings, hand-ins, rewards, the job 3 refusal, the story skip and most player-driven stage changes, called from the hub rows |
| `kesteven/quest/QuestStageManager` | `EveryFrameScript` in `EFS_LIST`: automatic stage changes, failure checks, intel, bar events, quest fleets and their AI, the Cache guardian timer, Eliza relocation, post-quest revenge fleets |
| `kesteven/quest/KestevenQuest`, `KestevenStage`, `KestevenFlag`, `KestevenState` | Framework definition of quest `kq`, with the modules `KestevenHubModule`, `KestevenJob1Module` and `KestevenJob3Module`; the stage enum, the flags and the saved state ([KESTEVEN_STATE.md](KESTEVEN_STATE.md)) |
| `kesteven/quest/KestevenJob1Module`, `# KESTEVEN QUESTLINE: JOB 1` rows in `data/campaign/rules.csv` | Job 1 world logic: the intel entry and its text rows, the tip system's dormant fleet, the move to stage 2 ([Job 1](#job-1-enemy-unknown-stages-0-to-6)) |
| `kesteven/quest/KestevenJob3Module`, `# KESTEVEN QUESTLINE: JOB 3` rows | Job 3 intel entry and its text rows, and the objects placed when the job is accepted ([Job 3](#job-3-hostile-takeover-stages-6-to-11)); the expedition itself is still `QuestStageManager`'s |
| `kesteven/quest/QuestHelper` | Wrappers over `KestevenState` for the old callers: the stage as a legacy int, flags, fields and lazily picked target locations; `saveEnding()` |
| `kesteven/quest/KestevenFleets` | Builders for every quest fleet |
| `CorePlugin` | Opens the Java quest dialogs when the player interacts with a quest entity, deciding through `KestevenQuest` queries |
| `kesteven/quest/*Dialog`, `kesteven/quest/*BarEvent`, `kesteven/quest/HintWreckDialog` | Java dialogs and bar events |
| `dialogue/rules/nskr_job4FleetDialog`, `nskr_ttCollectorDialog`, `nskr_elizaInterceptDialog`, `nskr_altEndingDialogLuddic`, `nskr_altEndingDialogTT`, `nskr_barEventFixer`, `nskr_isKStage`, `nskr_isAtLeastKStage` | Rules commands for the fleet conversations, endings and stage predicates |
| `kesteven/quest/ElizaRaid`, `ElizaRaidObjectiveCreator` | Ground raid for Eliza's disks |
| `world/systems/cache/Cache` | The Cache system, guardian fleet and its fleet-interaction config |
| `kesteven/quest/OperationLifesaverIntel`, `TheDelveIntel`, `CacheIntel` | Intel entries of jobs 4 and 5 and the Cache; they read the stage and flags and never write the stage |
| `kesteven/ExileManager` | Moves the quest people between Asteria and the Outpost |

## Where the quest is offered

The questline runs at `helper/SectorLookup.asteriaOrOutpost()`: Asteria (`nskr_asteria`), or the Outpost (`nskr_outpost`) when Kesteven is exiled or Asteria was never generated. Jack Lapua (`nskr_opguy`), Alice Lumi (`nskr_researcher`) and Michael Roux (`nskr_president`, administrator) live there; Nicholas Antoine (`nskr_intelligence`) works at the Outpost.

The rules row `nskr_kq_hubChatOption` adds "Chat about operations work" to a person's options when all of these hold, checked in this order:

- the person has the `k_quest` tag (Jack, Alice, Nicholas);
- `nskr_quest kq check hubOpen`: the market belongs to Kesteven and the player's Kesteven relationship is above -0.50;
- the questline has not ended (flag `ENDED`);
- the stage is neither `COMPLETED` nor `FAILED`.

At stage 20 and after failure the option disappears.

Each job has a relationship gate and, from job 3 on, a fleet-strength gate. Strength is `nskr_kestevenQuest.fleetPower()`: `PowerLevel.get(0.2f, 0f, 2f)`, or 2 in dev mode. The hub rows read the gates through the `KestevenHubModule` checks `job1Standing` … `job5Standing` and `job3Fleet` … `job5Fleet`, which compute the strength each time a row tests them; the job 3 and job 4 briefings compute it once per command call.

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

The stage is a `KestevenStage` on the quest state, changed only by the quest manager. The old code reads and writes it as the legacy integer below through `QuestHelper.getStage/setStage`; [KESTEVEN_STATE.md](KESTEVEN_STATE.md#stages) maps each integer to its constant. Stages 3 to 5 are unused. There is no Job 2; the job numbers follow the stage cheat sheet in `nskr_kestevenQuest.java`.

| Stage | Meaning | Set by |
|---|---|---|
| 0 | Not started | Start stage; the quest manager creates the state on the first unpaused frame of a new campaign |
| 1 | Job 1 active | Jack, accept (`quest()`) |
| 2 | Job 1 tasks done | `KestevenJob1Module`, when both deliveries are recorded |
| 6 | Job 3 offered by Jack | Jack, job 1 turn-in |
| 7 | Talk to Alice | Jack, job 3 briefing |
| 8 | Job 3 active | Alice, accept |
| 9 | Expedition target known | `HostileTakeoverBarEvent` |
| 10 | Job 3 over, success or failure | `QuestStageManager`: target destroyed, timeout, or stealth broken |
| 11 | Job 4 pending | Alice, job 3 turn-in; also job 3 skip |
| 12 | Job 4 active | Alice, accept |
| 13 | Job 4 done | `QuestStageManager`, friendly fleet found and strike group destroyed |
| 14 | Job 5 offered by Jack | Alice, job 4 turn-in; attacking the friendly fleet also sets 14 |
| 15 | Go to the bar | Jack, while showing the job 5 briefing |
| 16 | Job 5 active: data disks | `DelveMeetingBarEvent`, leaving the meeting |
| 17 | Cache location known | Alice after all disks; `QuestStageManager` on entering the Cache system at stage 16; story skip |
| 18 | Cache guardian defeated | `Cache.CacheGuardInteractionConfig` when no prototypes remain |
| 19 | Player holds the UPC | `CacheCoreDialog` salvage |
| 20 | Completed | Any of the four ending dialogs |
| 99 | Questline ended by failure | `QuestStageManager` failure checks |

## Job 1: Enemy Unknown (stages 0 to 6)

Jack offers two tasks for 155,000 credits:

1. Win a battle against an Enigma fleet while destroying at least one ship. `QuestStageManager.reportEncounterLootGenerated` counts Enigma casualties weighted by the player's contribution and sets `JOB1_SENSOR_DATA` when the sum reaches 1. It stays there until the quest framework routes encounter loot of fleets that are not quest fleets; `onLoot` sees only quest fleets.
2. Deliver 70 Artifact Electronics (`nskr_electronics`).

The tip system is picked once by `QuestHelper.getJob1Tip()`: from the `KestevenHubModule` action `pickJob1Tip` when the player opens Jack's questions at stage 0, or his menu at stage 1 with neither task done and no tip given, and from `KestevenJob1Module` when stage 1 starts. The pick chooses a system with an Enigma base and places a dormant Enigma fleet there, which the quest adopts as role `job1Dormant` (`FleetOrders.none()`, persistent, so it outlives the job). Jack gives the system through "How am I supposed to find them?": a question at stage 0, a menu option leading to the briefing at stage 1. Either sets `JOB1_TIP_GIVEN`.

Jack takes each delivery when the player has it (`JOB1_DATA_DELIVERED`, `JOB1_ELECTRONICS_DELIVERED`). `KestevenJob1Module` then moves stage 1 to 2: its action `job1Progress` does it at once, and its daily tick catches a delivery no row reported. Nothing calls `job1Progress` yet: the hand-ins in `nskr_kestevenQuest` only set the flags, so the move and a new map marker after the tip wait for the next campaign day. Turning in at stage 2 grants a Kesteven hullmod modspec (an unknown one of `nskr_inertial`, `nskr_volatile`, `nskr_bigBats`, `nskr_criticalArmor` if possible), 155,000 credits, Kesteven +5 and Jack +10, and sets stage 6.

Winning against Enigma before accepting sets `FOUGHT_ENIGMA`, which changes one of Jack's answers.

### Intel

`KestevenJob1Module` is active at stages 1 and 2. When stage 1 starts it shows the `QuestIntel` entry `job1` (icon `job1`; tags important, accepted, missions) as a campaign message, and it ends the entry at once when the stage leaves 2, including on failure. Its map marker is the tip system's hyperspace anchor once the tip was given and the system still has an Enigma base, otherwise `asteriaOrOutpost`; the module sets it when stage 1 starts, in `job1Progress` and once a day.

The text is in the `# KESTEVEN QUESTLINE: JOB 1` block, selected by `$nskr_intel_key == job1`: the title "Enemy Unknown" (`nskr_kqIntelTitle`), one bullet per open task (`nskr_kqIntelBullets`) and the description (`nskr_kqIntelDesc`). The description repeats the bullets as paragraphs after its first line, because `QuestIntel` shows no bullets in the description panel. The rows use the module's checks `kestevenHostile` (Kesteven relationship at most -0.50; job 3 rows use it too) and `job1TipBase` (the tip system has an Enigma base), the tokens `job1HomeName` (`asteriaOrOutpost` name) and `job1ArtifactCount` (`nskr_kestevenQuest.JOB1_ARTIFACTS`), and the hub's `job1TipSystem`.

## Job 3: Hostile Takeover (stages 6 to 11)

At stage 6 Jack sends the player to Alice and mentions the artifact exchange (`nskr_shipSwap`, available from stage 7 through a research official). Continuing makes Jack a potential contact and sets stage 7.

Alice's briefing at stage 7: a Tri-Tachyon expedition leaves from the job 3 start market, a random Tri-Tachyon market other than `eochu_bres` and `culann`. The player must destroy it without being identified, within about 90 days, for 205,000 credits. Accepting sets stage 8.

When stage 8 starts, `KestevenJob3Module` (active at stages 8 and 9):

- picks the start market and the job 3 target, a random location in a system near the core, if they are not picked yet;
- shows the intel entry `job3`;
- adds `HostileTakeoverBarEvent` to `PortsideBarData` (still a Java bar event);
- places a dormant Enigma fleet at the target. It is not a quest fleet: `DataSatelliteDialog.makeHostile` moves it to `QuestStageManager`'s fleet list when satellite #3 is salvaged.

On the first unpaused frame at stage 8, `QuestStageManager` spawns the "Expedition" fleet (Tri-Tachyon) at the start market, tracks it, and places data-disk satellite #3 at the target. The expedition, its countdown, completion and failure stay in `QuestStageManager` until `FleetOrders` can run the expedition's schedule.

The expedition prepares at home for 10 days, travels to the target, orbits it until day 70, then returns home and stands down.

`HostileTakeoverBarEvent` is a drinking scene with a Tri-Tachyon employee. Either way out of the party, sober or with a hangover that costs 4,000 to 7,000 credits, gives the target coordinates: stage 9 and `JOB3_TARGET_DISCOVERED`. Declining the first round removes the bar event permanently; the player must then find the expedition another way.

The job ends in `QuestStageManager` at stage 10 in one of three ways:

- the expedition falls below 20% of its spawn strength: success;
- the expedition is older than 90 days: `JOB3_FAILED`, and derelicts are left at the target;
- the player contributes to a battle against the expedition while it has seen the player's transponder on: `JOB3_FAILED` ("failed to neutralize the fleet stealthily").

### Intel

The `QuestIntel` entry `job3` (icon `job3`; tags important, accepted, missions) shows as a campaign message when stage 8 starts, and `KestevenJob3Module` ends it at once when the stage leaves 9, including on failure. Its map marker is the start market at stage 8 and the target at stage 9. The text is in the `# KESTEVEN QUESTLINE: JOB 3` block, selected by `$nskr_intel_key == job3`: the title "Hostile Takeover", bullets for hostile relations (`kestevenHostile`), the days left (`job3TimeLeft`, token `job3DaysLeft`: `job3TimeLeft` cut to whole days) and the current step (tokens `job3MarketName`, `job3MarketSystem`, `job3TargetSystem`), and the description with the bullets repeated as paragraphs.

Alice's turn-in at stage 10: on success, a modspec, 50,000 exchange points, 205,000 credits, Kesteven +5 and Alice +10; on failure, Kesteven -5 and Alice -10. Both set stage 11.

Refusing at stage 7 ("I'm not doing this.", then "yes") costs Kesteven -5 and Alice -10 and sets stage 11 and `JOB3_REFUSED`. The derelicts, satellite #3 and the dormant fleet are still placed at the target so job 5 can use them. The question row `nskr_kq_aliceQRefuse` has its own option id, handled by `nskr_kq_aliceAskRefuseSel`, which calls `nskr_kestevenQuest skip`.

## Job 4: Operation Lifesaver (stages 11 to 14)

At stage 11 `QuestStageManager` counts 300 seconds (30 days) and then sets `JOB4_WAIT_OVER`. After the wait, Alice offers the job when Kesteven relationship is at least 0.60 and either strength is above 0.80 or `JOB4_REQUIREMENT_SKIPPED` is set. Pay is 285,000 credits. Her briefing names the constellation of the friendly fleet's location, a random point in a system far from the core. If the Outpost belongs to Kesteven she also points to Nicholas. Accepting sets stage 12.

When stage 12 is first seen, `QuestStageManager`:

- adds `OperationLifesaverIntel`;
- spawns the Enigma "Strike Group" (flagship "DSRD Eye for an eye") and records its location as the job 4 enemy target;
- places data-disk satellite #4 at the enemy target;
- spawns the Kesteven "Special Operations" fleet at the friendly target (transponder off);
- spawns ten Enigma "Splinter" patrols;
- places a debris field and three Kesteven derelicts near the enemy target. One derelict is the hint wreck.

Three sources lead the player on:

- Nicholas describes a burst of signals from the enemy target system and records his dialogue stage.
- The hint wreck (`HintWreckDialog`, id prefix `$job4HintWreck`) gives the friendly fleet's system.
- The Special Operations fleet (`nskr_job4FleetDialog`, transponder must be on) tells its story, sends the strike group coordinates, and asks for 250 supplies and 400 fuel. Giving them sets `JOB4_FRIENDLY_HELPED`, and the fleet flies home.

`OperationLifesaverIntel` lists each lead the player has. Its map marker points at the most precise one: the strike group once seen or once the Special Operations fleet sent its coordinates; else the friendly fleet's coordinates from the hint wreck while that fleet is not found; else the system Nicholas named; else the found friendly fleet; else the briefing's constellation. The strike group leads apply only until it is destroyed.

Talking to the friendly fleet or seeing it sets `JOB4_FRIENDLY_FOUND`. Reducing the strike group below 20% of its strength sets `JOB4_TARGET_DESTROYED`. With both set, `QuestStageManager` sets stage 13.

If anything other than the player destroys the Special Operations fleet before it is found, `QuestStageManager` spawns a new one at the friendly target. It checks once a day at stage 12, after the destroyed fleet has left the quest fleet list; that happens once the player is out of hyperspace sensor range of it.

If the player contributes to a battle against the Special Operations fleet, `QuestStageManager` sets stage 14 and `JOB4_FAILED`. The next advance turns that into stage 99 and ends the questline.

Alice's turn-in at stage 13 grants 1 story point, 285,000 credits, a modspec, Kesteven +5 and Alice +10. If the player helped the fleet it also grants an Epoch-class prototype frigate (`nskr_epoch_empty`). Alice becomes a potential contact, Jack's importance rises to high, and S-mod removal (`nskr_modRemoval`) opens at research officials. Stage becomes 14.

## Job 5: The Delve (stages 14 to 19)

### Briefing and meeting

At stage 14 Jack's briefing tells the player to go to the bar; showing the briefing sets stage 15 directly.

At stage 15 the rules row `BarFixerEntered` runs `nskr_barEventFixer` on every bar visit at `asteriaOrOutpost`. It adds `DelveMeetingBarEvent`, the in-person meeting with Jack and Alice:

- they explain the pre-collapse satellite network, the data disks and the Cache;
- if the player hesitates, Jack pays a 150,000-credit advance;
- Eliza is introduced as the holder of one disk.

Leaving sets stage 16. If the player had already entered the Cache system, the meeting takes a shorter branch and still sets stage 16; `QuestStageManager` then moves to 17.

When stage 16 is first seen, `QuestStageManager` adds `TheDelveIntel` and the three Eliza bar events.

### The five data disks

| Disk | Source |
|---|---|
| #1 and #2 | Eliza: given if the player agrees to help her, or taken by raiding her market |
| #3 | Satellite at the job 3 target system (`DataSatelliteDialog`) |
| #4 | Satellite at the job 4 enemy target system (`DataSatelliteDialog`) |
| #5 | Comms facility on Glacier in the Frost system (`GlacierCommsDialog`) |

The satellites exist from jobs 3 and 4 and can be salvaged at any time. Each salvage fires a hyperwave ping and wakes the nearby guard: the dormant Enigma fleet at #3, the strike group at #4. The second satellite also yields the keywords that point to the Frost system.

Tips unlock in this order:

1. Jack (`JOB5_JACK_TIP`): find Eliza through pirates.
2. Alice (`JOB5_ALICE_TIP`): where the two satellites are. She marks the one not yet salvaged.
3. Alice again (`JOB5_ALICE_TIP2`), after both tips and at least two satellites, while the disks are incomplete: a tundra planet around a red dwarf with a comms facility, within a stated distance of a named constellation. She marks Glacier.

If the player has visited Frost, answering "It's the Frost." sets `FROST_FOUND`. `QuestStageManager` also sets it when the player enters Frost after tip 2. `CorePlugin` opens `GlacierCommsDialog` at Glacier from tip 2 on; the timed raid there gives disk #5.

### Finding Eliza

The three bar events appear at pirate markets and share a dialogue stage (`KestevenState.elizaSearchStage`) and a list of used markets:

| Event | When | Result |
|---|---|---|
| `ElizaSearchBarEvent` | Stage 0, any unused pirate market | A rough spacer. Paying 4,000 to 7,000 credits names a contact market (stage 2, marked). Pressing without paying ends the talk (stage 1). |
| `ElizaSearchSecondBarEvent` | Stage 1 | A sly spacer who refuses to talk; leads to stage 2 |
| `ElizaSearchFinalBarEvent` | Stage 2; only at the paid-for market if the player paid | "Eliza herself wants to speak to you." Picks Eliza's market and sets `ELIZA_FOUND`. |

Eliza's market is a pirate market in Yma, Corvus, Isirah, Thule, Hybrasil, Galatia, Mayasura or Kumari Kandam, excluding Kanta's Den and used markets. If that market or the paid-for market decivilizes, `QuestStageManager` picks another and moves Eliza.

At her market, `CorePlugin` opens `ElizaDialog` until it has finished once. Eliza generates on first contact (`SectorGen.genEliza()`).

- **Agree, sincerely:** disks #1 and #2, `ELIZA_HELPED`, and `ELIZA_AGREED_SINCERELY`.
- **Agree while lying:** disks #1 and #2 and `ELIZA_HELPED` only.
- **Refuse:** `ELIZA_RAID_ENABLED`. `ElizaRaidObjectiveCreator` then adds an extreme "Data Disks" raid objective at her market. The raid grants disks #1 and #2 and 30,000 to 40,000 credits, removes Eliza from the market, and spawns her "Merc Armada", which hunts the player. Destroying her flagship sets `ELIZA_KILLED` and removes her from important people.

### Reaching the Cache

With five disks `QuestStageManager` sets `ALL_DISKS_RECOVERED`. Alice's all-disks conversation offers "Yes" and, if the player sincerely agreed to help Eliza, "Yes (lie)". Both set `CACHE_FOUND` and stage 17 and hand over the Cache coordinates. The Cache system "Unknown Site" is reached by a transverse jump.

Entering Unknown Site at stage 15 or 16 sets `CACHE_FOUND` without any disks, and stage 16 becomes 17. Stage 17 removes the Eliza bar events, so a player who reaches the Cache early can skip Eliza and the remaining disks.

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
- stage is 14 and `JOB4_FAILED` is set (the player attacked the Special Operations fleet);
- stage is 19, the UPC was handed to Eliza, and Eliza has been killed (`JOB5_FAILED`).

The questline option then disappears. The Cache can still be found and fought; `CacheCoreDialog` gives no questline reward in that state.

## Side events

| Event | Owner | When | What |
|---|---|---|---|
| Tri-Tachyon collector | `QuestStageManager`, `KestevenFleets.spawnCollectorFleet`, `nskr_ttCollectorDialog` | Once; stages 2 to 15; the player carries at least 50 Artifact Electronics, is in hyperspace within 25,000 units of the centre; 3% per day | "Black Ops" demands all Artifact Electronics; paying sends it home |
| "LZ" messenger | Quest `ic` ([intercept fleets](CONTRACTS_AND_BOUNTIES.md#intercept-fleets)), rules `nskr_ic_messenger*` in `# INTERCEPTS` | Once; stages 10 to 14 (`KestevenQuest.inMessengerWindow()`); hyperspace near the core; 4% per day | Anonymous warning signed "LZ"; opening its comm link calls `KestevenQuest.reportMessengerMet()`, which unlocks "LZ" questions for Alice and in `DelveMeetingBarEvent` |
| Exile | `kesteven/ExileManager` | Daily | If Asteria is lost while the Outpost is Kesteven's, the quest people move to the Outpost and back when Asteria returns |

## Story skip

While the `storySkipUnlocked` setting is on, the speaker menus add a 5-story-point "Skip story" option at stages 0, 6, 7 and 11 (after the job 4 wait), and at stage 14 while a job 5 gate fails (rows `nskr_kq_jackOptStorySkip…`, `nskr_kq_aliceOptStorySkip…`). Its handler row `nskr_kq_hubStorySkip` runs `nskr_kestevenQuest advanceStageStorySkip`, and `SkipStoryOptionPicked()`:

- places the job 3 objects (satellite #3 and the dormant fleet) if the stage is at most 7, and the job 4 objects (strike group, satellite #4 and wrecks) if it is at most 11;
- marks every job 5 tip and disk source as done and sets the Eliza help flags, generating Eliza if she has no market yet;
- sets `CACHE_FOUND` and stage 17;
- clears `nskr_starfarerFromStart` and sets `STORY_SKIPPED`.

## Defects found by reading the source

These follow from the code and rules as written. None has been checked in game.

1. **"Yes (lie)" to Alice.** It follows the same path as "Yes" and records nothing; no code reads a lie to Alice.
2. **Missing shortcut target.** The row `nskr_kq_hubBriefing` calls `SetShortcut nskr_kestevenQuestCancel`, but no option has that id. The call does nothing; `updateOptions()` puts Escape on `nskr_kestevenQuestExit`. The row's own options are dead too: `advanceStage` clears them before adding its own.
3. **Glacier.** The first screen of `GlacierCommsDialog` has no Leave option. `CorePlugin` opens it only when its one option, "Search for the facility", is available, and the next screen offers Leave.
4. **Operation Lifesaver system fallbacks.** `KestevenFleets.spawnJob4Target` picks the strike group's system inside the friendly target's constellation. When no other system there has two planets, `QuestHelper.getRandomSystemWithinConstellation` retries without excluding the friendly system, so both fleets can share one system; with no candidate at all it returns null and the spawn fails. `getRandomSystemFarCore`'s fallback can return a system outside any constellation, which `OperationLifesaverIntel` does not expect. Kept as is by the maintainer.
5. **Cache guardian report can move the stage back.** `KestevenQuest.reportCacheGuardianDefeated()` sets stage 18 whenever the questline has not ended and the stage is 16 or later, so a guardian defeat reported at stage 19 or 20 would return the questline to 18. Normal play defeats the guardian before stage 19.
6. **Failure counts as late stages.** `glacierCommsOpen()` and `cacheIsQuestTarget()` keep the old `stage >= 16` comparison on legacy numbers, so they are also true after failure (legacy stage 99).
