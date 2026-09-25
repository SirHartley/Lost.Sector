# Kesteven questline state

Every value the questline saves, where it lives and which classes write it. Flow and meaning are in [KESTEVEN_QUESTLINE.md](KESTEVEN_QUESTLINE.md). The quest framework's state contract is in the [framework README](../../jars/src/lostsector/quest/README.md#queststate); save-compatibility rules for the whole mod are in [ARCHITECTURE.md](../ARCHITECTURE.md#save-identity).

Java paths are relative to `jars/src/lostsector/campaign/`; `dialogue/rules/`, `quest/` and `combat/` paths are relative to `jars/src/lostsector/`.

## Storage

The questline is quest `kq` of the quest framework. `kesteven/quest/KestevenQuest` is its definition, registered in `quest/QuestCatalog`. `KestevenHubModule` declares the checks, actions and tokens of the rows for every conversation with Jack, Alice and Nicholas ([dialogue map](KESTEVEN_DIALOGUE.md#jack-alice-and-nicholas)); `KestevenJob1Module` runs job 1's intel, dormant fleet and move to `JOB1_DONE` ([Job 1](KESTEVEN_QUESTLINE.md#job-1-enemy-unknown-stages-0-to-6)); `KestevenJob3Module` runs job 3's intel and the objects placed at acceptance ([Job 3](KESTEVEN_QUESTLINE.md#job-3-hostile-takeover-stages-6-to-11)); `KestevenJob4Module` runs job 4's wait, intel, fleets, hint wreck, completion and failure ([Job 4](KESTEVEN_QUESTLINE.md#job-4-operation-lifesaver-stages-11-to-14)); `KestevenPartyModule` runs the job 3 party's guests, drink count and hangover bill ([The party](KESTEVEN_QUESTLINE.md#the-party)); `KestevenJob5Module` runs the Delve meeting's checks, its guard and the job 5 intel ([Job 5](KESTEVEN_QUESTLINE.md#briefing-and-meeting)); `KestevenGlacierModule` runs the Glacier facility ([Glacier](KESTEVEN_QUESTLINE.md#glacier-disk-5)). `KestevenElizaSearchModule` runs the search for Eliza at pirate bars ([Finding Eliza](KESTEVEN_QUESTLINE.md#finding-eliza)). `QuestStageManager` and the Java dialogs, bar events, intel and rules commands still run the rest of the questline, reading and writing one saved `kesteven/quest/KestevenState`. `KestevenQuest.isAvailable()` keeps the default `true`, because the old code runs the questline in every campaign and handles a missing Kesteven home as failure.

| Mechanism | Location | Notes |
|---|---|---|
| Stage | `KestevenState.stage()`, a `KestevenStage` | Changed only by `quest/QuestManager`; see [Who changes the stage](#who-changes-the-stage) |
| Flags | `KestevenFlag` constants set on the state | See [Flags](#flags) |
| Fields | Package-private fields of `KestevenState` | See [Fields](#fields) |
| Randoms | One saved `Random` per purpose on the state, from `KestevenQuest.random(purpose)` | See [Randoms](#randoms) |
| Timers | `KestevenState.TIMER_JOB3`, `TIMER_JOB4_WAIT` | Started when the job 3 expedition spawns and when `JOB4_WAITING` starts; the other old counters count frame seconds and stay fields until the modules replace them |
| Quest fleet list | Sector memory `$kQuestMissionFleets`, a `List<FleetInfo>` | Read and written by `FleetHelper.getFleets/setFleets`. `FleetInfo.age` is in days. |
| Framework fleets | The framework's `QuestFleets.KEY` list | The job 1 tip system's dormant fleet, role `job1Dormant`; the job 3 expedition, roles `job3Expedition` and `job3ExpeditionOver`; the job 4 fleets, roles `job4StrikeGroup`, `job4SpecialOps`, `job4SpecialOpsLeaving` and `job4Splinter` |
| Quest people | The state's person map, registered with the important people | The seven job 3 party guests of `KestevenPartyModule`, keys `party…`, ids `nskr_kq_party…`; the Delve meeting's escort `delveGuard` (`nskr_kq_delveGuard`) of `KestevenJob5Module`, during `JOB5_MEETING` |
| Fleet, entity and person memory | The owning `MemoryAPI` | Routing flags read by `rules.csv` and `CorePlugin`, and the conversation flags of Jack, Alice, Nicholas and the party employee; see [Memory flags](#memory-flags) |
| Saved objects | Bar events in `PortsideBarData`, intel in the intel manager, `ElizaRaidObjectiveCreator` as a listener | Their class names and fields are serialized. |
| Per installation | LunaLib settings: `settings/SettingsManager.set` and `Setting` reads | `thronesGiftUnlocked`, `hellspawnUnlocked`, `storySkipUnlocked`; shared by all campaigns |

The quest manager creates the state on the first unpaused frame of a new campaign and loads it with the save afterwards. Until then:

- `KestevenQuest.state()` returns null, and the `kesteven/quest/QuestHelper` wrappers return the old defaults: stage 0, flags off, counts 0, targets null, the job 3 timer at 90;
- the [queries for other features](#queries-for-other-features) read every flag as unset and the stage as `NOT_STARTED`;
- writes through the wrappers and `KestevenQuest.reportMessengerMet()` log an error through the quest manager and change nothing;
- `KestevenQuest.random(purpose)` throws;
- `QuestStageManager.advance()` returns at once;
- `nskr_quest kq` conditions fail and log an error, so the hub's "Chat about operations work" option does not show.

### Access from the old code

The questline's own old classes use these accessors until later tasks replace them with modules. These are the quest package and the questline dialog commands `nskr_ttCollectorDialog`, `nskr_elizaInterceptDialog`, `nskr_altEndingDialogLuddic` and `nskr_altEndingDialogTT`; other features use the [queries](#queries-for-other-features). Each is a thin wrapper over the state; the quest package's own classes also read and write fields directly where they already hold the state.

| Accessor | Reads or writes |
|---|---|
| `QuestHelper.getStage()`, `setStage(int)` | The stage, as the legacy int; `setStage` calls `QuestContext.advance` and skips a write of the current stage |
| `QuestHelper.getCompleted(KestevenFlag)`, `setCompleted(boolean, KestevenFlag)`, `getFailed`, `setFailed` | A flag; the failed pair is identical |
| `QuestHelper.getEndMissions()`, `setEndMissions(boolean)` | `ENDED` |
| `QuestHelper.getDisksRecovered`, `getNicholasDialogStage`, `getTtPayout` and their setters | The field of the same meaning |
| `QuestHelper.getJob1Tip`, `getJob3Start`, `getJob3Target`, `getJob4FriendlyTarget`, `getJob5FrostTip` | The target field; the first read after it is null picks and stores it |
| `QuestHelper.getJob4EnemyTarget`, `getElizaLoc/setElizaLoc`, `getCacheFleetLoc/setCacheFleetLoc` | The target field; the setters without an argument pick a new value |
| `DataSatelliteDialog.get/setRecoveredSatelliteCount` | `satellitesRecovered` |
| `KestevenElizaSearchModule.searchStage()`, `usedMarkets()`, `contactMarket()` (read only; `QuestHelper.pickElizaMarket`) | `elizaSearchStage`, `elizaSearchUsedMarkets`, `elizaContactMarket` |
| `dialogue/rules/nskr_ttCollectorDialog.getPaid/setPaid` | `COLLECTOR_PAID` |
| `getRandom()` on each class in [Randoms](#randoms) | `KestevenQuest.random` with that class's purpose |

`QuestHelper.getCompleted(String)` and `setCompleted(boolean, String)` still read and write sector persistent data. The bounties and the Mothership keep their own flags through them; no questline value uses them.

### Queries for other features

Classes outside the quest package and the questline dialog commands read and change questline state only through these public static methods of `KestevenQuest`, as the [framework README](../../jars/src/lostsector/quest/README.md#queries-from-other-features) requires. Stage comparisons use the legacy ints the callers used before, so "16 or later" also holds at 99.

| Query | True when | Callers |
|---|---|---|
| `stage()` | Returns the current `KestevenStage`; `NOT_STARTED` before the state exists | `dialogue/rules/nskr_isKStage`, `nskr_isAtLeastKStage`, `nskr_isAtMostKStage`, compared as legacy ints |
| `isFailed()` | `ENDED` | `kesteven/contracts/ContractManager` (fails every contract) |
| `kestevenEndingDone()` | `KESTEVEN_ENDING_DONE` | `ContractManager` (doubles the contract cap), `nskr_shipSwap` (heavy hulls in stock) |
| `elizaEndingDone()` | `ELIZA_ENDING_DONE` | `nskr_debt` (`hasOption` false) |
| `researchServicesClosed()` | `CHIP_HANDED_TO_ELIZA` or `ALT_ENDING_DONE` | `nskr_shipSwap` and `nskr_modRemoval` (`hasOption` false) |
| `isJackGone()` | `JACK_GONE` | `kesteven/ExileManager` (Jack is not moved) |
| `glacierDiskRecovered()` | `GLACIER_DISK_RECOVERED` | `enigma/StalkerSpawner` (three stalker fleets, once) |
| `inMessengerWindow()` | Stage 10 to 14 | `events/intercepts/InterceptsQuest` (the "LZ" messenger may spawn) |
| `cacheIsQuestTarget()` | Stage 16 or later | `world/systems/cache/Cache` (marks the command core important) |
| `isDataSatellite(entity)` | The entity has a memory key starting with `$kQuestArtifact` | `CorePlugin` (`DataSatelliteDialog`) |
| `elizaMeetingDone()` | `ELIZA_DIALOG_FINISHED` | `CorePlugin` (`ElizaDialog` while false) |
| `atElizaMarket(entity)` | `elizaMarket` is set and the entity is it or belongs to a market connected to it | `CorePlugin` (`ElizaDialog`, `EndingElizaDialog`) |
| `kestevenEndingAvailable()` | Stage 19, `KESTEVEN_ENDING_DONE` and `CHIP_HANDED_TO_ELIZA` unset | `CorePlugin` at `asteriaOrOutpost` or the Asteria station (`EndingKestevenDialog`) |
| `elizaEndingAvailable()` | Stage 19, `ELIZA_HELPED`, `CHIP_HANDED_TO_ELIZA` and `ELIZA_RETURNED` set, `ELIZA_ENDING_DONE` and `ELIZA_KILLED` unset | `CorePlugin` (`EndingElizaDialog`) |

| Action | Does | Caller |
|---|---|---|
| `reportMessengerMet()` | Sets `MESSENGER_MET` and `MESSENGER_QUESTION_OPEN` unless `MESSENGER_MET` is set | `events/intercepts/InterceptsQuest`, through the `messengerMet` action when the player opens the messenger's comm link |
| `reportCacheGuardianDefeated()` | Stage 18 through `QuestHelper.setStage`, unless `ENDED` is set or the stage is below 16 | `Cache.CacheGuardInteractionConfig.notifyLeave` |
| `markEmptyDataSatellite(entity, number)` | Sets `$kQuestArtifact<number>` and `$nskr_artifactKeyEmpty` on the entity; no state | `Cache.generate`, satellites 5 and 6 |

## Stages

`KestevenStage` lists the stages in story order; each names the stage before it. `FAILED` has no previous stage, because the questline fails from several stages. The legacy int is the value `QuestHelper.getStage()` returns and `setStage` accepts, and the argument of the `nskr_isKStage` family in `rules.csv`. `KestevenStage.fromLegacy(int)` and `toLegacy()` translate; they are removed with the last int caller.

| Stage | Legacy | Previous | Meaning |
|---|---|---|---|
| `NOT_STARTED` | 0 | none | Not started |
| `JOB1_ACTIVE` | 1 | `NOT_STARTED` | Job 1 active |
| `JOB1_DONE` | 2 | `JOB1_ACTIVE` | Job 1 tasks done |
| `JOB3_OFFERED` | 6 | `JOB1_DONE` | Job 3 offered by Jack |
| `JOB3_BRIEFING` | 7 | `JOB3_OFFERED` | Talk to Alice |
| `JOB3_ACTIVE` | 8 | `JOB3_BRIEFING` | Job 3 active |
| `JOB3_TARGET_KNOWN` | 9 | `JOB3_ACTIVE` | Expedition target known |
| `JOB3_DONE` | 10 | `JOB3_TARGET_KNOWN` | Job 3 over, success or failure |
| `JOB4_WAITING` | 11 | `JOB3_DONE` | Job 4 pending |
| `JOB4_ACTIVE` | 12 | `JOB4_WAITING` | Job 4 active |
| `JOB4_DONE` | 13 | `JOB4_ACTIVE` | Job 4 done |
| `JOB5_OFFERED` | 14 | `JOB4_DONE` | Job 5 offered by Jack |
| `JOB5_MEETING` | 15 | `JOB5_OFFERED` | Go to the bar |
| `JOB5_DISKS` | 16 | `JOB5_MEETING` | Job 5 active: data disks |
| `CACHE_KNOWN` | 17 | `JOB5_DISKS` | Cache location known |
| `CACHE_CLEARED` | 18 | `CACHE_KNOWN` | Cache guardian defeated |
| `CHIP_RECOVERED` | 19 | `CACHE_CLEARED` | Player holds the UPC |
| `COMPLETED` | 20 | `CHIP_RECOVERED` | Completed |
| `FAILED` | 99 | none | Questline ended by failure |

The actual path can skip stages: 8 to 10 without 9, 7 to 11 when job 3 is refused, and the story skip to 17. Stage 14 is also set from stages 12 to 16 when the player attacks the Special Operations fleet, followed at once by 99.

## Flags

| Flag | Meaning | Written by |
|---|---|---|
| `ENDED` | Questline permanently failed | `QuestStageManager` failure checks; `KestevenJob4Module` when the Special Operations fleet is attacked |
| `STORY_SKIPPED` | Story skip used; nothing reads it | Hub action `storySkip` |
| `FOUGHT_ENIGMA` | Beat Enigma before accepting job 1 | `KestevenJob1Module.onEncounterLoot` |
| `JOB1_SENSOR_DATA` | Sensor task done | Same; `KestevenJob1Module.onSkip` on a jump past `JOB1_ACTIVE` |
| `JOB1_DATA_DELIVERED`, `JOB1_ELECTRONICS_DELIVERED` | Sensor package and electronics delivered | Rules `nskr_kq_jackJob1HandIn…` |
| `JOB1_TIP_GIVEN` | Jack gave the location tip | Rules `nskr_kq_jackJob1Tip`, `nskr_kq_jackAskTipSel` |
| `COLLECTOR_PAID` | Tri-Tachyon collector paid | `nskr_ttCollectorDialog` |
| `JOB3_REFUSED` | Job 3 refused; nothing reads it | Rules `nskr_kq_aliceRefuseConfirm` |
| `JOB3_TARGET_DISCOVERED` | Target coordinates from the party | Rules `nskr_kq_partyCoordinates` |
| `JOB3_PARTY_DECLINED` | Party declined; the bar event no longer shows | Rules `nskr_kq_partyDecline` |
| `JOB3_FAILED` | Timeout or stealth broken | `KestevenJob3Module` |
| `MESSENGER_MET`, `MESSENGER_QUESTION_OPEN` | "LZ" messenger met; question available (cleared after asking Alice) | Quest `ic` through `KestevenQuest.reportMessengerMet()`; cleared by rules `nskr_kq_aliceAskLzSel` |
| `JOB4_WAIT_OVER` | 30-day wait over | `KestevenJob4Module` daily tick (timer `job4Wait`); its `onSkip` on a jump past `JOB4_WAITING` |
| `JOB4_REQUIREMENT_SKIPPED` | Job 4 strength gate bypassed with a story point | Rules `nskr_kq_hubReqSkipJob4` |
| `JOB4_HINT_WRECK_READ` | Hint wreck read | `KestevenJob4Module` action `readHintWreck` (rules `nskr_kq_hintWreckRead`) |
| `JOB4_FRIENDLY_FOUND`, `JOB4_TARGET_FOUND` | Fleets seen; the friendly fleet also when talked to | `KestevenJob4Module.onFleetDetected`, action `recordJob4FleetTalk`; `JOB4_FRIENDLY_FOUND` also by its `onSkip` past `JOB4_ACTIVE` |
| `JOB4_FRIENDLY_TALKED` | First conversation with the Special Operations fleet held | Action `recordJob4FleetTalk` (rules `nskr_kq_job4FleetAsk`) |
| `JOB4_TARGET_HINT` | Friendly fleet gave the strike group location | Action `recordJob4FleetTalk`, unless the strike group was seen or beaten |
| `JOB4_TARGET_DESTROYED` | Strike group below 20% strength or destroyed | `KestevenJob4Module` (`onBattle`, `onLoot`, `onFleetGone`); its `onSkip` past `JOB4_ACTIVE` |
| `JOB4_FRIENDLY_HELPED` | Supplies and fuel given | Rules `nskr_kq_job4FleetHelpConfirm` |
| `JOB4_FAILED` | Player attacked the friendly fleet | `KestevenJob4Module.onLoot` |
| `JOB5_JACK_TIP`, `JOB5_ALICE_TIP`, `JOB5_ALICE_TIP2` | Job 5 tips given | Rules `nskr_kq_jackLeads`, `nskr_kq_aliceLeads`, `nskr_kq_aliceFrostTip`; story skip |
| `FROST_FOUND` | Frost identified | Rules `nskr_kq_aliceFrostFound`, `QuestStageManager`, story skip |
| `SATELLITE3_RECOVERED`, `SATELLITE4_RECOVERED` | Satellite #3 or #4 salvaged | `DataSatelliteDialog`, story skip |
| `GLACIER_DISK_RECOVERED` | Disk #5 recovered | `KestevenGlacierModule` action `recoverGlacierDisk` and `onSkip` on a jump past `JOB5_DISKS`; story skip |
| `ALL_DISKS_RECOVERED` | At least five disks | `QuestStageManager` |
| `ELIZA_SPACER_PAID` | Paid the spacer | `KestevenElizaSearchModule` action `elizaSpacerPay` |
| `ELIZA_FOUND` | Eliza's market known | `KestevenElizaSearchModule` action `elizaContactLeave`, story skip |
| `ELIZA_DIALOG_FINISHED` | `ElizaDialog` completed | `ElizaDialog`, story skip |
| `ELIZA_HELPED` | Disks received by agreement | `ElizaDialog`, story skip |
| `ELIZA_AGREED_SINCERELY` | Agreed sincerely | `ElizaDialog` |
| `ELIZA_RAID_ENABLED` | Refused; raid enabled | `ElizaDialog` |
| `ELIZA_RAIDED` | Raid done | `ElizaRaid` |
| `ELIZA_KILLED` | Eliza dead | `QuestStageManager.runFleetLogic` |
| `CACHE_FOUND` | Cache coordinates known | Rules `nskr_kq_aliceCacheFound`, `QuestStageManager`, story skip |
| `CORE_SEEN`, `CHIP_SALVAGED` | Core seen; UPC salvaged | `CacheCoreDialog` |
| `ELIZA_INTERCEPT_TALKED` | Eliza's intercept fleet spoke to the player | `nskr_elizaInterceptDialog` |
| `CHIP_HANDED_TO_ELIZA` | UPC handed to Eliza | `nskr_elizaInterceptDialog` |
| `ELIZA_RETURNED` | Eliza back at her market | `QuestStageManager.respawnEliza` |
| `JOB5_FAILED` | Eliza killed after the handover | `QuestStageManager` |
| `KESTEVEN_ENDING_DONE` | Kesteven ending done | `EndingKestevenDialog` |
| `ELIZA_ENDING_DONE` | Eliza ending done | `EndingElizaDialog` |
| `ALT_ENDING_DONE` | Luddic or Tri-Tachyon ending done (shared) | `nskr_altEndingDialogLuddic.makeMad` |
| `LUDDIC_ENDING_SECOND_TALK`, `TT_ENDING_SECOND_TALK` | Second conversation reached | Alternative endings |
| `COMMISSION_RESTORE_PENDING` | Commission fix pending | `EndingElizaDialog` |
| `JACK_GONE` | Jack left for revenge | `QuestStageManager.vengeanceJack` |
| `ELIZA_BETRAYED` | Player took Eliza's market after her ending | `QuestStageManager` |

Other features read flags through the [queries](#queries-for-other-features). `nskr_starfarerFromStart` (`ModPlugin.STARFARER_MODE_FROM_START_KEY`) is not questline state: `ModPlugin.onNewGame` writes it to sector persistent data, `Difficulty.clearStarfarerFromStartUnlessStarfarer()` and the story skip clear it, and `QuestHelper.saveEnding()` reads it for `hellspawnUnlocked`.

## Fields

| Field | Type | Meaning | Written by |
|---|---|---|---|
| `job1TipSystem` | `StarSystemAPI` | System with an Enigma base; the first pick also adds a dormant Enigma fleet there, adopted as role `job1Dormant` | `QuestHelper.getJob1Tip()`, from the hub action `pickJob1Tip`, the job 1 briefing and `KestevenJob1Module` when `JOB1_ACTIVE` starts |
| `job3Start` | `SectorEntityToken` | Random Tri-Tachyon market entity, not `eochu_bres` or `culann` | `QuestHelper.getJob3Start()` |
| `job3Target` | `SectorEntityToken` | Random location in a system within 27,500 units of the centre | `QuestHelper.getJob3Target()`, first from `KestevenJob3Module` when `JOB3_ACTIVE` starts |
| `job4FriendlyTarget` | `SectorEntityToken` | Random location in a system at least 32,500 units from the centre | `QuestHelper.getJob4FriendlyTarget()` |
| `job4EnemyTarget` | `SectorEntityToken` | Strike group location | `KestevenJob4Module.spawnStrikeGroup`, from the spec `KestevenFleets.job4StrikeGroup` builds |
| `job5FrostTipSystem` | `StarSystemAPI` | System 7,000 to 12,000 units from Frost, for Alice's distance hint | `QuestHelper.getJob5FrostTip()` |
| `elizaMarket` | `SectorEntityToken` | Eliza's market entity; re-picked on decivilization | `QuestHelper.setElizaLoc()` |
| `elizaContactMarket` | `SectorEntityToken` | Contact market after paying the spacer; re-picked on decivilization | `KestevenElizaSearchModule` (`elizaPickContact`, `onDecivilized`) |
| `elizaContactFormerName` | `String` | The contact's entity name before its last move, for the move message | `KestevenElizaSearchModule.onDecivilized` |
| `elizaSpacerPrice` | `int` | The first spacer's price, 4,000 to 7,000, rolled each time that conversation opens | `KestevenElizaSearchModule` action `elizaSpacerOpen` |
| `cacheGuardianSpot` | `SectorEntityToken` | Guardian spawn point in Unknown Site | `QuestHelper.setCacheFleetLoc()` |
| `disksRecovered` | `int` | Disks recovered | `DataSatelliteDialog`, `ElizaDialog`, `ElizaRaid`, `KestevenGlacierModule` action `recoverGlacierDisk` |
| `satellitesRecovered` | `int` | Satellites salvaged, 0 to 2 | `DataSatelliteDialog`, story skip |
| `nicholasDialogStage` | `int` | Nicholas's job 4 dialogue stage; the hub reads it through `check nicholasTipGiven` | Hub action `recordNicholasTip` |
| `elizaSearchStage` | `int` | Eliza search step, 0 to 3 | `KestevenElizaSearchModule` actions |
| `partyDrinks` | `int` | Drinks at the job 3 party; checks `partyTipsy` (1 or more) and `partyDrunk` (2 or more) | Action `partyDrink` of `KestevenPartyModule` |
| `elizaSearchUsedMarkets` | `List<String>` | Market ids whose bar the Eliza search already used | `KestevenElizaSearchModule` actions |
| `glacierHit` | `FleetMemberAPI` | The ship whose barrage line is being printed, read by the tokens `glacierHitShip` and `glacierHitHull`; null outside the action | `KestevenGlacierModule` action `damageFleet` |
| `ttPayout` | `float` | Tri-Tachyon price, at least 2,000,000 | `nskr_altEndingDialogTT` |
| `commissionRepPirates`, `commissionRepKesteven`, `commissionRepHegemony` | `float` | Relationship values the commission fix re-applies | `EndingElizaDialog` |
| `dayCounter`, `fleetCounter` | `float` | Daily logic timer (10 s) and fleet timer (1 s), in frame seconds | `QuestStageManager` |
| `cacheSeconds` | `float` | Frame seconds spent in Unknown Site before the guardian | `QuestStageManager` |
| `cacheIntelAdded` | `boolean` | Intel added once | `QuestStageManager` |
| `collectorSpawned` | `boolean` | Tri-Tachyon collector spawned | `QuestStageManager` |
| `cacheGuardianSpotPicked`, `cacheDoubtShown`, `cacheGuardianSpawned` | `boolean` | Guardian location picked, Cache hint shown, guardian spawned | `QuestStageManager` |
| `elizaInterceptSpawned`, `elizaRevengeSpawned`, `jackRevengeSpawned` | `boolean` | Eliza's intercept, Eliza's revenge and Jack's revenge spawned | `QuestStageManager` |
| `commissionRestored` | `boolean` | Commission fix applied | `QuestStageManager` |

`QuestStageManager` keeps `pingTimer` (seconds between Cache pings) and `frameWait` (30-frame delay before the commission fix) as plain instance fields. They are not saved and restart on every load.

## Randoms

Each purpose is a constant on `KestevenState`, named after the persistent-data key of the saved `Random` it replaces. Every purpose continues its own sequence after a reload; the seeds come from the quest seed, not the sector seed.

| Constant | Purpose | Accessor |
|---|---|---|
| `RANDOM_QUEST` | `kestevenQuestRandom` | `KestevenQuest.random(KestevenState.RANDOM_QUEST)` and the hub actions: target pickers, fleets, the modspec reward, Eliza bar payment, Cache guardian and wrecks |
| `RANDOM_REVENGE` | `kestevenQuestRandomKey` | `QuestStageManager.getRandom()`: Jack's revenge roll |
| `RANDOM_SATELLITE` | `artifactKeyRandom` | `DataSatelliteDialog.getRandom()` |
| `RANDOM_GLACIER` | `glacierCommsKeyRandom` | `KestevenGlacierModule` action `damageFleet`: which ships the barrage hits and how hard |
| `RANDOM_ELIZA` | `elizaDialogKeyRandom` | `ElizaDialog.getRandom()`, also Eliza's fleets and raid |
| `RANDOM_CACHE_DOUBT` | `cacheDoubtDialogRandom` | `CacheDoubtDialog.getRandom()` |
| `RANDOM_CACHE_CORE` | `coreDialogKeyRandom` | `CacheCoreDialog.getRandom()` |
| `RANDOM_KESTEVEN_ENDING` | `kestevenEndingDialogKeyRandom` | `EndingKestevenDialog.getRandom()` |
| `RANDOM_ELIZA_ENDING` | `elizaEndingDialogKeyRandom` | `EndingElizaDialog.getRandom()` |
| `RANDOM_ALT_ENDING` | `endingAltDialogKeyRandom` | `nskr_altEndingDialogLuddic.getRandom()` and `nskr_altEndingDialogTT.getRandom()`, one shared sequence |
| `RANDOM_COLLECTOR` | `ttCollectorDialogRandom` | `nskr_ttCollectorDialog.getRandom()`: the daily collector roll |
| `RANDOM_ELIZA_INTERCEPT` | `elizaInterceptDialogRandom` | `nskr_elizaInterceptDialog.getRandom()` |

`KestevenPartyModule` uses `ctx.random` directly: `partyHangover` for the hangover bill, and the framework's `person:<key>` purposes for its guests.

The Kesteven bar tip is not questline content; it is quest `hint` ([Exploration hints](HINTS.md)).

## Memory flags

| Flag | Owner memory | Set by | Read by |
|---|---|---|---|
| `$kQuestArtifact3`, `$kQuestArtifact4` | Satellite entity | `QuestHelper.spawnArtifact` | `KestevenQuest.isDataSatellite` for `CorePlugin` (prefix match) and `DataSatelliteDialog` |
| `$kQuestArtifact5`, `$kQuestArtifact6` | The two Unknown Site satellites | `Cache.generate` through `KestevenQuest.markEmptyDataSatellite` | As above |
| `$nskr_artifactKeyEmpty` | Satellite entity | `DataSatelliteDialog`, `KestevenQuest.markEmptyDataSatellite` | `DataSatelliteDialog` |
| `$job4HintWreck` + number | Entity **id** prefix of the hint wreck, not memory | `KestevenJob4Module.placeWrecks` | Nothing; the wreck's dialog is a claim (`$nskr_questDialog` = `nskr_kqHintWreck`) until read |
| `$nskr_kq_job3Expedition`, `$nskr_kq_job3ExpeditionOver` | The job 3 expedition: role flags of quest `kq` | `QuestFleets` | Nothing reads them |
| `$nskr_kq_job4StrikeGroup`, `$nskr_kq_job4SpecialOps`, `$nskr_kq_job4SpecialOpsLeaving`, `$nskr_kq_job4Splinter` | Job 4 fleets: role flags of quest `kq` | `QuestFleets` | Rules `# KESTEVEN QUESTLINE: JOB 4` (strike group and Special Operations rows); `DataSatelliteDialog.makeHostile` (strike group) |
| `$KestevenQuestTTCollector` | Collector fleet | `KestevenFleets` | `QuestStageManager`, rules |
| `$ElizaFleet` | Eliza's fleet after the raid | `KestevenFleets` | `QuestStageManager`, rules |
| `$InterceptPlayerElizaFleet` | Eliza's intercept fleet | `KestevenFleets` | `QuestStageManager`, rules, `nskr_elizaInterceptDialog` |
| `$RevengeanceQuestFleet`, `$RevengeanceJack` | Revenge fleets | `KestevenFleets` | `QuestStageManager`, rules |
| `$CacheGuardianFleet` (`Cache.CACHE_FLEET_KEY`) | Guardian fleet | `Cache` | `QuestStageManager`, `CacheBossTauntPlugin`, rules |
| `$EnigmaDormantFleet` (`DormantSpawner.DORMANT_KEY`) | Dormant fleets at quest locations | `DormantSpawner.addDormant` | `DataSatelliteDialog.makeHostile`, `QuestStageManager` |
| `$nskr_kq_job1Dormant` | The job 1 tip system's dormant fleet: role flag of quest `kq` | `QuestFleets.adopt`, from `QuestHelper.getJob1Tip()` | Nothing reads it yet |
| `$nskr_altEndingDialogLockedToPerson` | The official in either alternative ending | Alternative endings | Alternative endings |
| `$nskr_ic_messenger`, `$nskr_ic_messengerLeaving` | Messenger fleet role flags of quest `ic` | `QuestFleets` | Rules `# INTERCEPTS` |
| `$missionImportant` with reason `nskr_kq` | Satellite #3, satellite #4, Glacier; the Eliza contact market (`KestevenElizaSearchModule`, scope `JOB5_DISKS`) | Hub actions `markJob3Satellite`, `markJob4Satellite`; `KestevenGlacierModule` action `markGlacier` (`ctx.mark`, scope `JOB5_DISKS` on) | Map markers; `DataSatelliteDialog` unsets the key on salvage, `recoverGlacierDisk` calls `ctx.unmark` |
| `$missionImportant` with reason `nskr_kq` | The Cache command core `nskr_cache_core`, when it exists | `KestevenJob5Module` action `markCacheCore`, from the Delve meeting's "I think I already found that place." (`ctx.mark`, scope `JOB5_MEETING` on) | Map marker; `CacheCoreDialog` unsets the key on salvage |
| `$nskr_questDialog` (`quest/QuestDialogs.CLAIM_KEY`) = `nskr_kqGlacier` | Glacier | `markGlacier` (`ctx.claimDialog`, scope `JOB5_DISKS` on); released by `recoverGlacierDisk` | `CorePlugin`, which opens the `# KESTEVEN QUESTLINE: GLACIER` rows |
| `$nskr_kq_glacierPath` (`walk`, `cut`, `blast`), `$nskr_kq_glacierShutdown`, `$nskr_kq_glacierLate` | Glacier; expiry 0, so they end with the dialog | Glacier rows: the door choice, the shutdown choice, the countdown reaching 0 | Glacier rows: countdown lines, way back, barrage and congratulations |
| `$nskr_kq_introduced` | Jack, Alice or Nicholas, each their own; no expiry | Hub introduction rows `nskr_kq_jackIntro`, `nskr_kq_aliceIntro`, `nskr_kq_nicholasIntro` | Hub greeting rows |
| `$nskr_kq_partyTechiesDone`, `$nskr_kq_partyCrowdDone`, `$nskr_kq_partyOfficersDone` | The party employee (`nskr_kq_partyEmployee`); no expiry | Party rows `nskr_kq_partyPitchDecline`, `nskr_kq_partyPitchListen`, `nskr_kq_partyToastLeave`, `nskr_kq_partyTarget` | Party group menu `nskr_kqPartyGroups` and `nskr_kqPartyLookText` |
| `$nskr_kq_asked<Topic>` (`askedRogueAi`, `askedShips`, `askedElectronics`, `askedCores`, `askedFought`, `askedEquipment`, `askedEnigma`, `askedNextJob` on Jack; `askedWhere`, `askedAlone`, `askedNecessary`, `askedTarget`, `askedNextJob`, `askedSpecOps`, `askedThreats`, `askedSupplies`, `askedNicholas`, `askedGoal`, `askedInterest`, `askedArtifact` on Alice) | The speaker; no expiry | Hub answer rows `nskr_kq_<speaker>Ask<Topic>Sel` | Hub question rows, which hide an asked question |
| `$nskr_kq_delveAsked`, `$nskr_kq_delveAskedEnigma`, `…Comms`, `…Cache`, `…Chip`; `$nskr_kq_delveAdvanceOffered` | Jack, expiry `0` (the meeting has no way out before its end) | Delve meeting answer rows `nskr_kq_delveAsk…`; `nskr_kq_delveDoubt` | The meeting's question menu `nskr_kqDelveQuestions`; the advance payment `nskr_kq_delveAdvancePaid` |

## Who changes the stage

`quest/QuestManager` is the only writer of the stage. Rows change it with `nskr_quest kq advance FROM TO` and modules with `ctx.advance`. Every old writer calls `QuestHelper.setStage(int)`, which translates the int with `KestevenStage.fromLegacy` and calls `advance` on a context from `QuestManager.get().context("kq", ...)`; its changes are logged as `[kq] FROM -> TO (rule legacy)`.

| Caller of `setStage` | Stage changes |
|---|---|
| Hub confirmation rows, `nskr_quest kq advance` | 0→1, 2→6, 6→7, 7→8, 10→11, 11→12, 13→14, 16→17 |
| Hub row `nskr_kq_jackJob5Brief` | 14→15 |
| Hub row `nskr_kq_aliceRefuseConfirm` | 7→11 |
| Hub action `storySkip` (`ctx.advance`) | 0, 6, 7, 11 or 14→17 (story skip) |
| `KestevenJob1Module` (`job1Progress` action, daily tick) | 1→2 |
| `QuestStageManager.advance()` | 16→17, failure→99 |
| `KestevenJob4Module` (both job 4 fleets taken care of; the Special Operations fleet attacked) | 12→13; 12 to 16→14, then 14→99 |
| `KestevenJob3Module` (expedition beaten, timer over, stealth broken) | 8 or 9→10 |
| Party row `nskr_kq_partyCoordinates` (`nskr_quest kq advance`) | 8→9 |
| Row `nskr_kq_delveLeave` (Delve meeting), `nskr_quest kq advance` | 15→16 |
| `Cache.CacheGuardInteractionConfig`, through `KestevenQuest.reportCacheGuardianDefeated()` | 16 or 17→18 |
| `CacheCoreDialog` | →19 |
| `EndingKestevenDialog`, `EndingElizaDialog`, `nskr_altEndingDialogLuddic.makeMad` | 19→20 |
