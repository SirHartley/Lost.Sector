# Kesteven questline state

Every value the questline saves, where it lives and which classes write it. Flow and meaning are in [KESTEVEN_QUESTLINE.md](KESTEVEN_QUESTLINE.md). Save-compatibility rules for the whole mod are in [ARCHITECTURE.md](../ARCHITECTURE.md#save-identity).

Java paths are relative to `src/lostsector/campaign/`.

## Storage mechanisms

| Mechanism | Location | Notes |
|---|---|---|
| `QuestUtil.getStage/setStage`, `getCompleted/setCompleted`, `getFailed/setFailed`, `getFloat/setFloat`, `getDialogStage/setDialogStage`, `getLocation/setLocation` | `Global.getSector().getPersistentData()` | Plain keys without `$`, although some constants spell one. Every getter writes its default into the map on first read. `getFailed` and `getCompleted` read the same map; the names are interchangeable. |
| Lazily picked targets | Persistent data, key prefix `nskr_kestevenQuest` | The first read picks and stores a `SectorEntityToken` or `StarSystemAPI`. The picking context is whichever caller reads first, often a dialog or intel panel. |
| `Saved<T>` fields in `QuestStageManager` | Persistent data, `nskr_` + name | Reloaded by `ModPlugin` on load and after save. |
| Quest fleet list | Sector memory `$kQuestMissionFleets`, a `List<FleetInfo>` | Read and written by `FleetUtil.getFleets/setFleets`. `FleetInfo.age` is in days. |
| Fleet, entity and person memory | The owning `MemoryAPI` | Routing flags read by `rules.csv` and `CorePlugin`. |
| Saved objects | Bar events in `PortsideBarData`, intel in the intel manager, `ElizaRaidObjectiveCreator` as a listener | Their class names and fields are serialized. |
| Per installation | `LOST_SECTOR_cfg.json` through `ModPlugin.saveToConfig/loadFromConfig` | `completedStory`, `completedStoryHard`; shared by all campaigns |

## Stage and end

| Key | Type | Written by |
|---|---|---|
| `nskr_kestevenQuest` | int stage | `nskr_kestevenQuest`, `QuestStageManager`, `KQuest3Bar`, `KQuest5Bar`, `Cache.CacheGuardFIDConfig`, `CoreDialog`, ending dialogs, `nskr_altEndingDialogLuddic.makeMad` |
| `KestevenQuestEnd` (`QUEST_END_KEY`) | boolean | `QuestStageManager` failure checks |
| `nskr_kestevenQuestSkippedStory` | boolean | Story skip |

## Randomness

Each owner keeps its own `Random` in persistent data. Most are seeded from the sector seed (`MiscLS.getSeedParsed()`).

| Key | Owner |
|---|---|
| `KestevenQuestRandomKey` | `QuestStageManager.getRandom()` (Jack's revenge roll) |
| `nskr_kestevenQuestRandom` | `nskr_kestevenQuest.getRandom()`: target pickers, fleets, rewards, Eliza bar payment |
| `nskr_artifactKeyRandom`, `nskr_elizaDialogKeyRandom`, `nskr_glacierCommsKeyRandom`, `nskr_cacheDoubtDialogRandom`, `nskr_coreDialogKeyRandom` | The dialog of the same name |
| `nskr_KestevenEndingDialogKeyRandom`, `nskr_ElizaEndingDialogKeyRandom`, `nskr_EndingAltDialogKeyRandom` | Ending dialogs; the last is shared by both alternative endings |
| `nskr_job4FleetDialogRandom`, `nskr_job4HintWreckDialogRandom`, `nskr_ttCollectorDialogRandom`, `nskr_elizaInterceptDialogRandom` | The command or dialog of the same name |

## Saved targets

| Key | Picked by | Value |
|---|---|---|
| `nskr_kestevenQuestTip1` | `QuestUtil.getJob1Tip()` | System with an Enigma base; the first read also adds a dormant Enigma fleet there |
| `nskr_kestevenQuestStart3` | `QuestUtil.getJob3Start()` | Random Tri-Tachyon market entity, not `eochu_bres` or `culann` |
| `nskr_kestevenQuestTarget3` | `QuestUtil.getJob3Target()` | Random location in a system within 27,500 units of the centre |
| `nskr_kestevenQuestTargetFriendly4` | `QuestUtil.getJob4FriendlyTarget()` | Random location in a system at least 32,500 units from the centre |
| `nskr_kestevenQuestTargetEnemy4` | `QuestFleets.spawnJob4Target()` via `QuestUtil.setJob4EnemyTarget` | Strike group location |
| `nskr_kestevenQuestJob5FrostTip` | `QuestUtil.getJob5FrostTip()` | System 7,000 to 12,000 units from Frost, used for Alice's distance hint |
| `nskr_kestevenQuestElizaJob5` | `QuestUtil.setElizaLoc()` | Eliza's market entity; re-picked on decivilization |
| `nskr_kestevenQuestCacheFleet` | `QuestUtil.setCacheFleetLoc()` | Guardian spawn point in Unknown Site |
| `nskr_kQuest5ElizaBarPaidForLocation` | `KQuest5ElizaBarMain.setPaidForInfoTarget()` | Contact market after paying the spacer |

## Conversation flags

| Key | Meaning |
|---|---|
| `nskr_jackIntro`, `nskr_aliceIntro`, `nskr_nickIntro` | One-time introduction text shown |
| `nskr_kestevenQuestJob4Intelligence` | Nicholas's dialogue stage (int) |
| `nskr_kestevenQuestJob4SkipRequirement` | Job 4 strength gate skipped; never set in practice, see the questline defects |
| `nskr_kestevenQuestJob5JackTip`, `nskr_kestevenQuestJob5AliceTip`, `nskr_kestevenQuestJob5AliceTip2` | Job 5 tips given |
| `KestevenQuestEMessengerTalkedKey`, `KestevenQuestEMessengerTalkedKeyAskAbout` | "LZ" messenger met; question available (cleared after asking Alice) |

## Job 1

| Key | Meaning | Written by |
|---|---|---|
| `KestevenQuest1HasFoughtEnigma` | Beat Enigma before accepting | `QuestStageManager.reportEncounterLootGenerated` |
| `KestevenQuest1Sensor` | Sensor task done | Same |
| `KestevenQuest1DeliverData`, `KestevenQuest1Deliver` | Sensor package and electronics delivered | `nskr_kestevenQuest` |
| `KestevenQuest1LocTip` | Jack gave the location tip | `nskr_kestevenQuest` |

## Job 3

| Key | Meaning | Written by |
|---|---|---|
| `KestevenQuestJob3Discovered` | Target coordinates from the bar | `KQuest3Bar` |
| `KestevenQuestJob3Fail` | Timeout or stealth broken | `QuestStageManager` |
| `KestevenQuestSkip3` | Job refused | `nskr_kestevenQuest.confirmSkip` |
| `KestevenQuestJob3Timer` | Float countdown from 90, reduced by 0.1 per fleet tick | `QuestStageManager.job3TargetLogic` |

## Job 4

| Key | Meaning | Written by |
|---|---|---|
| `KestevenQuestJob4WaitTimer` | 30-day wait over | `QuestStageManager` |
| `KestevenQuestJob4FoundFriendly`, `KestevenQuestJob4FoundTarget` | Fleets seen | `QuestStageManager` |
| `KestevenQuestJob4TargetHint` | Friendly fleet gave the strike group location | `QuestStageManager`, from the friendly fleet's dialogue stage |
| `KestevenQuestJob4Destroy` | Strike group below 20% strength | `QuestStageManager` |
| `KestevenQuestJob4Help` | Supplies and fuel given | `nskr_job4FleetDialog` |
| `KestevenQuestJob4Fail` | Player attacked the friendly fleet | `QuestStageManager` |
| `nskr_job4FleetDialogKey` | Friendly fleet dialogue stage (int) | `nskr_job4FleetDialog` |
| `job4HintWreckCoordinatesReceived` | Hint wreck read | `Job4HintWreck` |

## Job 5

| Key | Meaning | Written by |
|---|---|---|
| `KestevenQuestDiskCount` | Disks recovered (int) | `ArtifactDialog`, `ElizaDialog`, `ElizaRaid`, `GlacierCommsDialog` |
| `KestevenQuestAllDisks` | At least five disks | `QuestStageManager` |
| `nskr_artifactKeyCount` | Satellites salvaged (int) | `ArtifactDialog`, story skip |
| `nskr_artifactKey3Recovered`, `nskr_artifactKey4Recovered` | Satellite #3 or #4 salvaged | `ArtifactDialog`, story skip |
| `nskr_kestevenQuestJob5FoundFrost` | Frost identified | `nskr_kestevenQuest.quest()`, `QuestStageManager` |
| `nskr_glacierCommsKeyCount` | Disk #5 recovered (boolean despite the name) | `GlacierCommsDialog` |
| `nskr_kQuest5ElizaBarDialogStage` | Eliza bar chain stage: 0 to 3 | Eliza bar events |
| `nskr_kQuest5ElizaBarUsedMarket` | `List<String>` of market ids already used | Eliza bar events |
| `nskr_kQuest5ElizaBarPaidFor` | Paid the spacer | `KQuest5ElizaBarMain` |
| `KestevenQuestJob5FoundEliza` | Eliza's market known | `KQuest5ElizaBarFinal` |
| `nskr_elizaDialogKeyFinished` | `ElizaDialog` completed | `ElizaDialog` |
| `nskr_elizaDialogKeyHelp` | Disks received by agreement | `ElizaDialog` |
| `nskr_elizaDialogKeyAgreeToHelp` | Agreed sincerely | `ElizaDialog` |
| `nskr_elizaDialogKeyRaid` | Refused; raid enabled | `ElizaDialog` |
| `nskr_elizaDialogKeyFight` | Raid done | `ElizaRaid` |
| `KestevenQuestKilledEliza` | Eliza dead | `QuestStageManager.runFleetLogic` |
| `KestevenQuestFoundCache` | Cache coordinates known | `nskr_kestevenQuest`, `QuestStageManager`, story skip |
| `nskr_coreDialogFirstTimeKey`, `nskr_coreDialogRecoveredKey` | Core seen; UPC salvaged | `CoreDialog` |

## Stage 19 and endings

| Key | Meaning | Written by |
|---|---|---|
| `InterceptPlayerElizaTalkedTo` | Eliza's intercept fleet spoke to the player | `nskr_elizaInterceptDialog` |
| `$InterceptPlayerHandedUPCOver` | UPC handed to Eliza; a persistent-data key despite the `$` | `nskr_elizaInterceptDialog` |
| `InterceptPlayerElizaReturn` | Eliza back at her market | `QuestStageManager.respawnEliza` |
| `KestevenQuestJob5Fail` | Eliza killed after the handover | `QuestStageManager` |
| `nskr_KestevenEndingDialogKeyFinished` | Kesteven ending done | `EndingKestevenDialog` |
| `nskr_ElizaEndingDialogKeyFinished` | Eliza ending done | `EndingElizaDialog` |
| `nskr_EndingAltDialogKeyFinished` | Luddic or Tri-Tachyon ending done (shared) | `nskr_altEndingDialogLuddic.makeMad` |
| `nskr_altEndingDialogSecondTimeLuddic`, `nskr_altEndingDialogSecondTimeTT` | Second conversation reached | Alternative endings |
| `nskr_altEndingDialogTTPayout` | Tri-Tachyon price, at least 2,000,000 | `nskr_altEndingDialogTT` |
| `activateCommissionUnFuckerEliza`, `activateCommissionUnFuckerPirates`, `activateCommissionUnFuckerHege`, `activateCommissionUnFuckerKesteven` | Commission fix pending and its saved relationship values | `EndingElizaDialog` |
| `KestevenQuestJob5JackRevengeance` (`JACK_GONE_KEY`) | Jack left for revenge | `QuestStageManager.vengeanceJack` |
| `RevengeanceElizaBetrayByPlayer` | Player took Eliza's market after her ending | `QuestStageManager` |
| `nskr_ttCollectorDialogKey` | Collector paid | `nskr_ttCollectorDialog` |
| `nskr_starfarerFromStart` | Cleared by the story skip | `nskr_kestevenQuest` |

## `Saved` fields in `QuestStageManager`

Stored as `nskr_` + name.

| Name | Meaning |
|---|---|
| `questCounter`, `questFleetCounter` | Daily logic timer (10 s) and fleet timer (1 s) |
| `questCounterJob` | Job 4 wait counter |
| `questCacheTimer` | Seconds spent in Unknown Site before the guardian |
| `questIntelStage1`, `questIntelStage3`, `questIntelStage4`, `questIntelStage5`, `cacheIntelAddStage5` | Intel added once. `questIntelStage2` is declared and unused. |
| `questBarStage5` | Unused; `nskr_barEventFixer` replaced it |
| `questJobFleetSpawned3`, `questJobFleetsSpawned4` | Job fleets spawned once |
| `questJobCollected` | Tri-Tachyon collector spawned |
| `questJobCacheDoubt`, `questJobCacheLoc`, `questJobCacheGuardian` | Cache hint shown, guardian location picked, guardian spawned |
| `questJobVengeanced`, `questJobelizaBetray`, `questJobelizadIntercept` | Jack's revenge, Eliza's revenge and Eliza's intercept spawned |
| `commissionStage5` | Commission fix applied |

## Memory flags

| Flag | Owner memory | Set by | Read by |
|---|---|---|---|
| `$kQuestArtifact3`, `$kQuestArtifact4` | Satellite entity | `QuestUtil.spawnArtifact` | `CorePlugin` (prefix match) and `ArtifactDialog` |
| `$nskr_artifactKeyEmpty` | Satellite entity | `ArtifactDialog` | `ArtifactDialog` |
| `$job4HintWreck` + number | Entity **id** prefix, not memory | `QuestStageManager.spawnJob4Wrecks` | `CorePlugin` |
| `$KestevenQuestJob3Target` | Expedition fleet | `QuestFleets` | `QuestStageManager` |
| `$KestevenQuestJob4Target`, `$KestevenQuestJob4Friendly`, `$KestevenQuestJob4Splinter` | Job 4 fleets | `QuestFleets` | `QuestStageManager`, rules |
| `$KestevenQuestTTCollector` | Collector fleet | `QuestFleets` | `QuestStageManager`, rules |
| `$ElizaFleet` | Eliza's fleet after the raid | `QuestFleets` | `QuestStageManager`, rules |
| `$InterceptPlayerElizaFleet` | Eliza's intercept fleet | `QuestFleets` | `QuestStageManager`, rules, `nskr_elizaInterceptDialog` |
| `$RevengeanceQuestFleet`, `$RevengeanceJack` | Revenge fleets | `QuestFleets` | `QuestStageManager`, rules |
| `$CacheGuardianFleet` (`Cache.CACHE_FLEET_KEY`) | Guardian fleet | `Cache` | `QuestStageManager`, `TauntPlugin`, rules |
| `$EnigmaDormantFleet` (`DormantSpawner.DORMANT_KEY`) | Dormant fleets at quest locations | `MiscLS.addDormant` | `ArtifactDialog.makeHostile`, `QuestStageManager` |
| `$nskr_altEndingDialogLockedToPerson` | The official in either alternative ending | Alternative endings | Alternative endings |
| `$nskr_interceptManagerMessengerFleet`, `$nskr_interceptManagerMessengerTalked` | Messenger fleet | `InterceptManager`, rules | `InterceptManager` |

## Who changes the stage

The stage has no single owner. These are all the writers:

| Writer | Stage changes |
|---|---|
| `nskr_kestevenQuest.quest()` | 0→1, 2→6, 6→7, 7→8, 10→11, 11→12, 13→14, 16→17 |
| `nskr_kestevenQuest.showQuestInfoAndPrepare()` | 14→15 |
| `nskr_kestevenQuest.confirmSkip()` | 7→11 |
| `nskr_kestevenQuest.SkipStoryOptionPicked()` | any→17 (unreachable, see defects) |
| `QuestStageManager.advance()` | 1→2, 12→13, 16→17, failure→99 |
| `QuestStageManager.job3TargetLogic()` | 8 or 9→10 |
| `QuestStageManager.reportEncounterLootGenerated()` | 8 or 9→10 (stealth broken), any→14 (friendly attacked) |
| `KQuest3Bar` | 8→9 |
| `KQuest5Bar` | 15→16 |
| `Cache.CacheGuardFIDConfig` | 16 or 17→18 |
| `CoreDialog` | →19 |
| `EndingKestevenDialog`, `EndingElizaDialog`, `nskr_altEndingDialogLuddic.makeMad` | 19→20 |
