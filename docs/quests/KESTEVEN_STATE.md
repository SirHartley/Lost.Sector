# Kesteven questline state

Every value the questline saves, where it lives and which classes write it. Flow and meaning are in [KESTEVEN_QUESTLINE.md](KESTEVEN_QUESTLINE.md). The quest framework's state contract is in the [framework README](../../jars/src/lostsector/quest/README.md#queststate); save-compatibility rules for the whole mod are in [ARCHITECTURE.md](../ARCHITECTURE.md#save-identity).

Java paths are relative to `jars/src/lostsector/campaign/`; `dialogue/rules/`, `quest/` and `combat/` paths are relative to `jars/src/lostsector/`.

## Storage

The questline is quest `kq` of the quest framework. `kesteven/quest/KestevenQuest` is its definition, registered in `quest/QuestCatalog`. `KestevenHubModule` declares the checks, actions and tokens of the rows for every conversation with Jack, Alice and Nicholas ([dialogue map](KESTEVEN_DIALOGUE.md#jack-alice-and-nicholas)); `KestevenJob1Module` runs job 1's intel, dormant fleet and move to `JOB1_DONE` ([Job 1](KESTEVEN_QUESTLINE.md#job-1-enemy-unknown-stages-0-to-6)); `KestevenJob3Module` runs job 3's intel and the objects placed at acceptance ([Job 3](KESTEVEN_QUESTLINE.md#job-3-hostile-takeover-stages-6-to-11)); `KestevenJob4Module` runs job 4's wait, intel, fleets, hint wreck, completion and failure ([Job 4](KESTEVEN_QUESTLINE.md#job-4-operation-lifesaver-stages-11-to-14)); `KestevenPartyModule` runs the job 3 party's guests, drink count and hangover bill ([The party](KESTEVEN_QUESTLINE.md#the-party)); `KestevenJob5Module` runs the Delve meeting's checks, its guard and the job 5 intel ([Job 5](KESTEVEN_QUESTLINE.md#briefing-and-meeting)); `KestevenGlacierModule` runs the Glacier facility ([Glacier](KESTEVEN_QUESTLINE.md#glacier-disk-5)). `KestevenElizaSearchModule` runs the search for Eliza at pirate bars ([Finding Eliza](KESTEVEN_QUESTLINE.md#finding-eliza)); `KestevenEndingsModule` serves the rows of the Kesteven and Eliza endings, restores relationships after a commission ends and caps them after the Eliza ending ([endings](KESTEVEN_QUESTLINE.md#the-kesteven-and-eliza-endings)); `KestevenElizaModule` serves the rows of the meeting at Eliza's port and moves her when her market is decivilized ([Eliza's port](KESTEVEN_QUESTLINE.md#elizas-port)). `KestevenCollector` configures the Tri-Tachyon collector's `InterceptEncounter` and `PayOffEncounter` ([Tri-Tachyon collector](KESTEVEN_QUESTLINE.md#tri-tachyon-collector)). `KestevenSatelliteModule` runs the data-disk satellites, `ALL_DISKS_RECOVERED` and the Frost sighting ([The five data disks](KESTEVEN_QUESTLINE.md#the-five-data-disks)); `KestevenCacheModule` runs the Cache ([Reaching the Cache](KESTEVEN_QUESTLINE.md#reaching-the-cache)). `KestevenHintWreckModule` serves the job 4 hint wreck in any stage. `KestevenAltEndingsModule` runs the Luddic and Tri-Tachyon endings ([Stage 19](KESTEVEN_QUESTLINE.md#stage-19-who-receives-the-chip)). Together they run the whole questline on one saved `kesteven/quest/KestevenState`. `KestevenQuest.isAvailable()` keeps the default `true`: the questline runs in every campaign, and a missing Kesteven home is a failure (`KestevenAftermathModule`).

| Mechanism | Location | Notes |
|---|---|---|
| Stage | `KestevenState.stage()`, a `KestevenStage` | Changed only by `quest/QuestManager`; see [Who changes the stage](#who-changes-the-stage) |
| Flags | `KestevenFlag` constants set on the state | See [Flags](#flags) |
| Fields | Package-private fields of `KestevenState` | See [Fields](#fields) |
| Randoms | One saved `Random` per purpose on the state, from `KestevenQuest.random(purpose)` | See [Randoms](#randoms) |
| Timers | `KestevenState.TIMER_JOB3`, `TIMER_JOB4_WAIT` | Started when the job 3 expedition spawns and when `JOB4_WAITING` starts; `cacheSeconds` counts frame seconds in Unknown Site |
| Framework fleets | The framework's `QuestFleets.KEY` list | The job 1 tip system's dormant fleet, role `job1Dormant`; the job 3 expedition, roles `job3Expedition` and `job3ExpeditionOver`; the job 4 fleets, roles `job4StrikeGroup`, `job4SpecialOps`, `job4SpecialOpsLeaving` and `job4Splinter`; the Enigma dormant fleets woken at satellite #3, role `satelliteGuard`; the Cache guardian, role `cacheGuardian`; the Tri-Tachyon collector, roles `ttCollector` and `ttCollectorLeaving` (record `ttCollector`) |
| Quest people | The state's person map, registered with the important people | The seven job 3 party guests of `KestevenPartyModule`, keys `party…`, ids `nskr_kq_party…`; the Delve meeting's escort `delveGuard` (`nskr_kq_delveGuard`) of `KestevenJob5Module`, during `JOB5_MEETING`; the operations chief and sensors officer of the Cache's inner voice, keys `cacheChief` and `cacheSensors`, created when it opens and released when it closes |
| Fleet, entity and person memory | The owning `MemoryAPI` | Routing flags read by `rules.csv` and `CorePlugin`, and the conversation flags of Jack, Alice, Nicholas and the party employee; see [Memory flags](#memory-flags) |
| Saved objects | Bar events in `PortsideBarData`, intel in the intel manager | Their class names and fields are serialized. |
| Per installation | LunaLib settings: `settings/SettingsManager.set` and `Setting` reads | `thronesGiftUnlocked`, `hellspawnUnlocked`, `storySkipUnlocked`; shared by all campaigns |

The quest manager creates the state on the first unpaused frame of a new campaign and loads it with the save afterwards. Until then:

- `KestevenQuest.state()` returns null;
- the [queries for other features](#queries-for-other-features) read every flag as unset and the stage as `NOT_STARTED`;
- `KestevenQuest.reportMessengerMet()` logs an error through the quest manager and changes nothing;
- `KestevenQuest.random(purpose)` throws;
- `nskr_quest kq` conditions fail and log an error, so the hub's "Chat about operations work" option does not show.

### Queries for other features

Classes outside the quest package and the questline dialog commands read and change questline state only through these public static methods of `KestevenQuest`, as the [framework README](../../jars/src/lostsector/quest/README.md#queries-from-other-features) requires. Stage comparisons follow story order with `FAILED` last (`KestevenStage.atLeast`), so "from `JOB5_DISKS` on" also holds after failure.

| Query | True when | Callers |
|---|---|---|
| `stage()` | Returns the current `KestevenStage`; `NOT_STARTED` before the state exists | `KestevenJob4Module` (a withdraw condition that runs outside a context) |
| `isFailed()` | `ENDED` | `kesteven/contracts/ContractManager` (fails every contract) |
| `kestevenEndingDone()` | `KESTEVEN_ENDING_DONE` | `ContractManager` (doubles the contract cap), `nskr_shipSwap` (heavy hulls in stock) |
| `elizaEndingDone()` | `ELIZA_ENDING_DONE` | `nskr_debt` (`hasOption` false) |
| `researchServicesClosed()` | `CHIP_HANDED_TO_ELIZA` or `ALT_ENDING_DONE` | `nskr_shipSwap` and `nskr_modRemoval` (`hasOption` false) |
| `isJackGone()` | `JACK_GONE` | `kesteven/ExileManager` (Jack is not moved) |
| `glacierDiskRecovered()` | `GLACIER_DISK_RECOVERED` | `enigma/StalkerSpawner` (three stalker fleets, once) |
| `inMessengerWindow()` | `JOB3_DONE` to `JOB5_OFFERED` (10 to 14) | `events/intercepts/InterceptsQuest` (the "LZ" messenger may spawn) |
| `atElizaMarket(entity)` | `elizaMarket` is set and the entity is it or belongs to a market connected to it | `KestevenElizaModule` check `elizaPort`; `KestevenEndingsModule` check `elizaEndingHere` |

| Action | Does | Caller |
|---|---|---|
| `reportMessengerMet()` | Sets `MESSENGER_MET` and `MESSENGER_QUESTION_OPEN` unless `MESSENGER_MET` is set | `events/intercepts/InterceptsQuest`, through the `messengerMet` action when the player opens the messenger's comm link |
| `reportCacheGuardianDefeated()` | `CACHE_CLEARED` through `ctx.advance`, unless `ENDED` is set, the stage is before `JOB5_DISKS`, or it is `CACHE_CLEARED` already | `Cache.CacheGuardInteractionConfig.notifyLeave` |
| `showCacheCore(dialog, core)` | `KestevenCacheModule.revealCore`: claims the core's dialog for every stage, marks the core from `JOB5_DISKS` on, and continues the dialog with the core's rows | `Cache.CacheGuardInteractionConfig.notifyLeave` |
| `markEmptyDataSatellite(entity, number)` | Sets `$kQuestArtifact<number>` and `$nskr_artifactKeyEmpty` on the entity; no state. `KestevenSatelliteModule.onStart` claims the satellite's dialog when the state is created | `Cache.generate`, satellites 5 and 6 |

## Stages

`KestevenStage` lists the stages in story order; each names the stage before it. `FAILED` has no previous stage, because the questline fails from several stages, and it is declared last, so `KestevenStage.atLeast` counts it as past every stage. The numbers are a reading aid for the quest pages, which name stages by them; the code uses only the names.

| Stage | No. | Previous | Meaning |
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
| `ENDED` | Questline permanently failed | `KestevenAftermathModule` when both mission markets are lost; `KestevenJob4Module` when the Special Operations fleet is attacked; `KestevenElizaFleetsModule` when Eliza is killed after the hand-over |
| `STORY_SKIPPED` | Story skip used; nothing reads it | Hub action `storySkip` |
| `FOUGHT_ENIGMA` | Beat Enigma before accepting job 1 | `KestevenJob1Module.onEncounterLoot` |
| `JOB1_SENSOR_DATA` | Sensor task done | Same; `KestevenJob1Module.onSkip` on a jump past `JOB1_ACTIVE` |
| `JOB1_DATA_DELIVERED`, `JOB1_ELECTRONICS_DELIVERED` | Sensor package and electronics delivered | Rules `nskr_kq_jackJob1HandIn…`; `KestevenHubModule.onSkip` past `JOB1_ACTIVE` |
| `JOB1_TIP_GIVEN` | Jack gave the location tip | Rules `nskr_kq_jackJob1Tip`, `nskr_kq_jackAskTipSel` |
| `COLLECTOR_PAID` | Tri-Tachyon collector paid; the collector's comm row `nskr_kq_ttCollectorOpen` matches only while it is unset | `KestevenCollector`, action `ttCollectorPay` (rules `nskr_kq_ttCollectorPay`) |
| `JOB3_REFUSED` | Job 3 refused; nothing reads it | Rules `nskr_kq_aliceRefuseConfirm` |
| `JOB3_TARGET_DISCOVERED` | Target coordinates from the party | Rules `nskr_kq_partyCoordinates`; `KestevenPartyModule.onSkip` past `JOB3_ACTIVE` |
| `JOB3_PARTY_DECLINED` | Party declined; the bar event no longer shows | Rules `nskr_kq_partyDecline` |
| `JOB3_FAILED` | Timeout or stealth broken | `KestevenJob3Module` |
| `MESSENGER_MET`, `MESSENGER_QUESTION_OPEN` | "LZ" messenger met; question available (cleared after asking Alice) | Quest `ic` through `KestevenQuest.reportMessengerMet()`; cleared by rules `nskr_kq_aliceAskLzSel` |
| `JOB4_WAIT_OVER` | 30-day wait over | `KestevenJob4Module` daily tick (timer `job4Wait`); its `onSkip` past `JOB4_WAITING`, which also clears the timer |
| `JOB4_REQUIREMENT_SKIPPED` | Job 4 strength gate bypassed with a story point | Rules `nskr_kq_hubReqSkipJob4` |
| `JOB4_HINT_WRECK_READ` | Hint wreck read | `KestevenHintWreckModule` action `readHintWreck` (rules `nskr_kq_hintWreckRead`), in any stage |
| `JOB4_FRIENDLY_FOUND`, `JOB4_TARGET_FOUND` | Fleets seen; the friendly fleet also when talked to | `KestevenJob4Module.onFleetDetected`, action `recordJob4FleetTalk` |
| `JOB4_FRIENDLY_TALKED` | First conversation with the Special Operations fleet held | Action `recordJob4FleetTalk` (rules `nskr_kq_job4FleetAsk`) |
| `JOB4_TARGET_HINT` | Friendly fleet gave the strike group location | Action `recordJob4FleetTalk`, unless the strike group was seen or beaten |
| `JOB4_TARGET_DESTROYED` | Strike group below 20% strength or destroyed | `KestevenJob4Module` (`onBattle`, `onLoot`, `onFleetGone`); its `onSkip` past `JOB4_ACTIVE` |
| `JOB4_FRIENDLY_HELPED` | Supplies and fuel given | Rules `nskr_kq_job4FleetHelpConfirm` |
| `JOB4_FAILED` | Player attacked the friendly fleet | `KestevenJob4Module.onLoot` |
| `JOB5_JACK_TIP`, `JOB5_ALICE_TIP`, `JOB5_ALICE_TIP2` | Job 5 tips given | Rules `nskr_kq_jackLeads`, `nskr_kq_aliceLeads`, `nskr_kq_aliceFrostTip`; `KestevenHubModule.onSkip` past `JOB5_DISKS` |
| `FROST_FOUND` | Frost identified | Rules `nskr_kq_aliceFrostFound`, `KestevenSatelliteModule` (entering Frost at stage 16 after tip 2), `KestevenGlacierModule.onSkip` past `JOB5_DISKS` |
| `SATELLITE3_RECOVERED`, `SATELLITE4_RECOVERED` | Satellite #3 or #4 salvaged | `KestevenSatelliteModule` action `salvageSatellite` and `onSkip` past `JOB5_DISKS` |
| `GLACIER_DISK_RECOVERED` | Disk #5 recovered | `KestevenGlacierModule` action `recoverGlacierDisk` and `onSkip` past `JOB5_DISKS` |
| `ALL_DISKS_RECOVERED` | At least five disks, recorded at stage 16 | `KestevenSatelliteModule.checkAllDisks`, from `salvageSatellite`, `recoverGlacierDisk`, `elizaHandOver`, `elizaRaid`, the start of stage 16, and the `onSkip` past `JOB5_DISKS` of `KestevenGlacierModule`, `KestevenSatelliteModule` and `KestevenElizaModule` |
| `ELIZA_SPACER_PAID` | Paid the spacer | `KestevenElizaSearchModule` action `elizaSpacerPay` |
| `ELIZA_FOUND` | Eliza's market known | `KestevenElizaSearchModule` action `elizaContactLeave` and `onSkip` past `JOB5_DISKS` |
| `ELIZA_DIALOG_FINISHED` | Meeting at Eliza's port finished | Rules `nskr_kq_elizaDismissed`, `nskr_kq_elizaDisks`; `KestevenElizaModule.onSkip` past `JOB5_DISKS` |
| `ELIZA_HELPED` | Disks received by agreement | Rules `nskr_kq_elizaDisks`; `KestevenElizaModule.onSkip` past `JOB5_DISKS` |
| `ELIZA_AGREED_SINCERELY` | Agreed sincerely | Rules `nskr_kq_elizaAgree` |
| `ELIZA_RAID_ENABLED` | Refused; raid enabled | Rules `nskr_kq_elizaDismissed` |
| `ELIZA_RAIDED` | Raid done | `KestevenElizaModule` raid action `elizaRaid` |
| `ELIZA_KILLED` | Eliza dead | `KestevenElizaFleetsModule` (`onBattle`, `onFleetGone`) |
| `CACHE_FOUND` | Cache coordinates known | Rules `nskr_kq_aliceCacheFound`, `KestevenCacheModule` (arrival or stage change at stage 15, 16 or after failure; `onSkip` on a jump past `JOB5_DISKS`) |
| `CORE_SEEN`, `CHIP_SALVAGED` | Core seen; UPC salvaged | Rules `nskr_kq_coreFirst`; `KestevenCacheModule` action `salvageCacheCore`; both by `onSkip` on a jump past `CACHE_CLEARED` |
| `ELIZA_INTERCEPT_TALKED` | Eliza's intercept fleet spoke to the player | `KestevenElizaFleetsModule` action `elizaTalked` |
| `CHIP_HANDED_TO_ELIZA` | UPC handed to Eliza | `KestevenElizaFleetsModule` action `elizaChipHandOver` |
| `ELIZA_RETURNED` | Eliza back at her market | `KestevenElizaModule.respawnEliza`, called by `KestevenElizaFleetsModule.onFleetGone` |
| `JOB5_FAILED` | Eliza killed after the handover | `KestevenElizaFleetsModule` |
| `KESTEVEN_ENDING_DONE` | Kesteven ending done | Rules `nskr_kq_kestevenEndingDone`; `KestevenEndingsModule.onSkip` on a jump past `CHIP_RECOVERED` without the hand-over |
| `ELIZA_ENDING_DONE` | Eliza ending done | Rules `nskr_kq_elizaEndingDone`; `KestevenEndingsModule.onSkip` on a jump past `CHIP_RECOVERED` after the hand-over |
| `ALT_ENDING_DONE` | Luddic or Tri-Tachyon ending done (shared) | `KestevenAltEndingsModule` action `altEndingFallout` |
| `LUDDIC_ENDING_SECOND_TALK`, `TT_ENDING_SECOND_TALK` | Second conversation reached | Rules `nskr_kq_altEndingLuddicDoubtSel`, `nskr_kq_altEndingTtIncreaseSel` |
| `COMMISSION_RESTORE_PENDING` | Commission fix pending | `KestevenEndingsModule` action `elizaEnding` |
| `JACK_GONE` | Jack left for revenge | `KestevenAftermathModule` |
| `ELIZA_BETRAYED` | Player took Eliza's market after her ending | `KestevenElizaFleetsModule.onDay` |

Other features read flags through the [queries](#queries-for-other-features). `nskr_starfarerFromStart` (`ModPlugin.STARFARER_MODE_FROM_START_KEY`) is not questline state: `ModPlugin.onNewGame` writes it to sector persistent data, `Difficulty.clearStarfarerFromStartUnlessStarfarer()` and the story skip clear it, and `KestevenEndingsModule.unlockSettings()` reads it for `hellspawnUnlocked`.

## Fields

| Field | Type | Meaning | Written by |
|---|---|---|---|
| `job1TipSystem` | `StarSystemAPI` | System with an Enigma base; the first pick also adds a dormant Enigma fleet there, adopted as role `job1Dormant` | `KestevenJob1Module.pickTip`, from the hub action `pickJob1Tip` in Jack's rows and when `JOB1_ACTIVE` starts |
| `job3Start` | `SectorEntityToken` | Random Tri-Tachyon market entity, not `eochu_bres` or `culann` | `KestevenJob3Module.start`, from the hub action `pickJob3Start` in Alice's briefing and when `JOB3_ACTIVE` starts |
| `job3Target` | `SectorEntityToken` | Random location in a system within 27,500 units of the centre | `KestevenJob3Module.target`, first when `JOB3_ACTIVE` starts; also the hub actions `pickJob3Target` and `placeJob3Leftovers` |
| `job4FriendlyTarget` | `SectorEntityToken` | Random location in a system at least 32,500 units from the centre | `KestevenJob4Module.friendlyTarget`, from the hub action `pickJob4FriendlyTarget` in Alice's briefing or before the first job 4 fleet |
| `job4EnemyTarget` | `SectorEntityToken` | Strike group location | `KestevenJob4Module.spawnStrikeGroup`, from the spec `KestevenFleets.job4StrikeGroup` builds |
| `job5FrostTipSystem` | `StarSystemAPI` | System 7,000 to 12,000 units from Frost, for Alice's distance hint | Hub action `pickJob5FrostTip` |
| `elizaMarket` | `SectorEntityToken` | Eliza's market entity; re-picked on decivilization | `KestevenElizaSearchModule` (action `elizaPickMarket`, `onSkip` past `JOB5_DISKS`) through its `pickMarket`; `KestevenElizaModule.onDecivilized` |
| `elizaContactMarket` | `SectorEntityToken` | Contact market after paying the spacer; re-picked on decivilization | `KestevenElizaSearchModule` (`elizaPickContact`, `onDecivilized`) |
| `elizaContactFormerName` | `String` | The contact's entity name before its last move, for the move message | `KestevenElizaSearchModule.onDecivilized` |
| `elizaFormerName` | `String` | Eliza's market entity name before her last move, for the Delve update `elizaMoved` | `KestevenElizaModule.onDecivilized` |
| `elizaRaidCredits` | `float` | Credits the raid on Eliza's port took, for its result row (token `elizaRaidCredits`) | `KestevenElizaModule` raid action `elizaRaid` |
| `elizaSpacerPrice` | `int` | The first spacer's price, 4,000 to 7,000, rolled each time that conversation opens | `KestevenElizaSearchModule` action `elizaSpacerOpen` |
| `cacheGuardianSpot` | `SectorEntityToken` | Guardian spawn point in Unknown Site, picked with `RANDOM_QUEST` | `KestevenCacheModule`, after 35 seconds in the site |
| `disksRecovered` | `int` | Disks recovered | `KestevenSatelliteModule` action `salvageSatellite`, `KestevenGlacierModule` action `recoverGlacierDisk`, `KestevenElizaModule` actions `elizaHandOver` and `elizaRaid`; the `onSkip` past `JOB5_DISKS` of the Glacier (1), satellite (1 per unsalvaged satellite) and Eliza (2) modules |
| `satellitesRecovered` | `int` | Satellites salvaged, 0 to 2; the hub checks `noSatellite`, `oneSatellite`, `twoSatellites` read it | `KestevenSatelliteModule` action `salvageSatellite`; its `onSkip` past `JOB5_DISKS` sets 2 |
| `nicholasDialogStage` | `int` | Nicholas's job 4 dialogue stage; the hub reads it through `check nicholasTipGiven` | Hub action `recordNicholasTip` |
| `elizaSearchStage` | `int` | Eliza search step, 0 to 3 | `KestevenElizaSearchModule` actions; its `onSkip` past `JOB5_DISKS` sets 3 |
| `partyDrinks` | `int` | Drinks at the job 3 party; checks `partyTipsy` (1 or more) and `partyDrunk` (2 or more) | Action `partyDrink` of `KestevenPartyModule` |
| `elizaSearchUsedMarkets` | `List<String>` | Market ids whose bar the Eliza search already used | `KestevenElizaSearchModule` actions |
| `glacierHit` | `FleetMemberAPI` | The ship whose barrage line is being printed, read by the tokens `glacierHitShip` and `glacierHitHull`; null outside the action | `KestevenGlacierModule` action `damageFleet` |
| `intercepts`, `payOffs` | `Map<String, InterceptEncounter.Record>`, `Map<String, PayOffEncounter.Record>` | The Tri-Tachyon collector's spawn count and payments, record `ttCollector` (`KestevenState` implements both modules' `Host`) | `KestevenCollector`'s shared modules |
| `ttPayout` | `float` | Tri-Tachyon price, at least 2,000,000 | `KestevenAltEndingsModule` actions `altEndingRaisePrice` and `altEndingPay` |
| `kestevenEndingTriTachyonRep` | `float` | The Tri-Tachyon relationship the Kesteven ending rolls, -0.70 to -0.65 | `KestevenEndingsModule` action `kestevenEnding` |
| `commissionRepPirates`, `commissionRepKesteven`, `commissionRepHegemony` | `float` | The Eliza ending's relationship values, which the commission fix re-applies | `KestevenEndingsModule` action `elizaEnding` |
| `cacheSeconds` | `float` | Frame seconds spent in Unknown Site before the guardian, twice as fast during fast advance | `KestevenCacheModule.onFrame` |
| `cacheIntelAdded` | `boolean` | The Cache intel entry shown once | `KestevenCacheModule` |
| `cacheGuardianSpotPicked`, `cacheDoubtShown`, `cacheGuardianSpawned` | `boolean` | Guardian location picked, inner voice opened, guardian spawned | `KestevenCacheModule.onFrame` |
| `elizaInterceptSpawned`, `elizaRevengeSpawned` | `boolean` | Eliza's intercept and Eliza's revenge spawned | `KestevenElizaFleetsModule` |
| `jackRevengeSpawned` | `boolean` | Jack's revenge spawned | `KestevenAftermathModule` |
| `commissionRestored` | `boolean` | Commission fix applied | `KestevenEndingsModule` |

`KestevenCacheModule` keeps `pingSeconds` (seconds since the last Cache ping) as a plain instance field. It is not saved and restarts on every load.

## Randoms

Each purpose is a constant on `KestevenState`, named after the persistent-data key of the saved `Random` it replaces. Every purpose continues its own sequence after a reload; the seeds come from the quest seed, not the sector seed.

| Constant | Purpose | Accessor |
|---|---|---|
| `RANDOM_QUEST` | `kestevenQuestRandom` | `ctx.random(KestevenState.RANDOM_QUEST)` in the modules, and `KestevenQuest.random` for `Cache`: the target pickers (`KestevenJob1Module.pickTip`, `KestevenJob3Module.start` and `target`, `KestevenJob4Module.friendlyTarget`, the hub's Frost tip, `KestevenElizaSearchModule.pickMarket`), satellites #3 and #4 (`KestevenSatelliteModule.spawn`), the job 3 and 4 fleets and wrecks, the modspec reward, the Eliza spacer's price, the Cache guardian's location and fleet, and the Cache's story point recovery roll |
| `RANDOM_REVENGE` | `kestevenQuestRandomKey` | `KestevenAftermathModule`: Jack's revenge roll, drawn every day |
| `RANDOM_GLACIER` | `glacierCommsKeyRandom` | `KestevenGlacierModule` action `damageFleet`: which ships the barrage hits and how hard |
| `RANDOM_ELIZA` | `elizaDialogKeyRandom` | `KestevenQuest.random(KestevenState.RANDOM_ELIZA)`: Eliza's fleets and raid |
| `RANDOM_CACHE_CORE` | `coreDialogKeyRandom` | `KestevenCacheModule` action `salvageCacheCore`: the Artifact Electronics roll |
| (module constants) | `cachePings`, `cacheMotes` | `KestevenCacheModule`: the direction spread of the Cache pings and the mote roll, which the old code drew unseeded |
| `RANDOM_KESTEVEN_ENDING` | `kestevenEndingDialogKeyRandom` | `KestevenEndingsModule` action `kestevenEnding` |
| `RANDOM_ELIZA_ENDING` | `elizaEndingDialogKeyRandom` | `KestevenEndingsModule` action `elizaEnding` |
| `RANDOM_ALT_ENDING` | `endingAltDialogKeyRandom` | `KestevenAltEndingsModule` action `altEndingFallout`, for both alternative endings |

`KestevenPartyModule` uses `ctx.random` directly: `partyHangover` for the hangover bill, and the framework's `person:<key>` purposes for its guests.

The Tri-Tachyon collector's shared modules draw from `roll:ttCollector` (the daily spawn roll), `fleet:ttCollector` (the fleet and its placement) and `target:ttCollector` (the market it leaves for).

The Kesteven bar tip is not questline content; it is quest `hint` ([Exploration hints](HINTS.md)).

## Memory flags

| Flag | Owner memory | Set by | Read by |
|---|---|---|---|
| `$kQuestArtifact3`, `$kQuestArtifact4` (`KestevenSatelliteModule.ARTIFACT_KEY` + number) | Satellite entity | `KestevenSatelliteModule.spawn`, which also claims the satellite | `KestevenSatelliteModule`: `onStart` finds satellites by the prefix; `$kQuestArtifact3` selects disk #3 and the dormant guard, any other satellite disk #4 and the strike group |
| `$kQuestArtifact5`, `$kQuestArtifact6` | The two Unknown Site satellites | `Cache.generate` through `KestevenQuest.markEmptyDataSatellite` | As above |
| `$nskr_artifactKeyEmpty` (`KestevenSatelliteModule.EMPTY_KEY`) | Satellite entity | Action `salvageSatellite`, `KestevenQuest.markEmptyDataSatellite` | Check `satelliteEmpty` |
| `$nskr_questDialog` = `nskr_kqSatellite` | Every satellite: its claim, in every stage | `KestevenSatelliteModule.claim` | `CorePlugin` claim route |
| `$job4HintWreck` + number | Entity **id** prefix of the hint wreck, not memory | `KestevenJob4Module.placeWrecks` | Nothing; the wreck's dialog is a claim (`$nskr_questDialog` = `nskr_kqHintWreck`, `KestevenHintWreckModule`) in every stage until read |
| `$nskr_kq_job3Expedition`, `$nskr_kq_job3ExpeditionOver` | The job 3 expedition: role flags of quest `kq` | `QuestFleets` | Nothing reads them |
| `$nskr_kq_job4StrikeGroup`, `$nskr_kq_job4SpecialOps`, `$nskr_kq_job4SpecialOpsLeaving`, `$nskr_kq_job4Splinter` | Job 4 fleets: role flags of quest `kq` | `QuestFleets` | Rules `# KESTEVEN QUESTLINE: JOB 4` (strike group and Special Operations rows); `KestevenSatelliteModule` action `wakeSatelliteGuard` finds the strike group by its role |
| `$nskr_kq_ttCollector`, `$nskr_kq_ttCollectorLeaving` | Tri-Tachyon collector: role flags of quest `kq` | `QuestFleets` | Rules `# KESTEVEN QUESTLINE: COLLECTOR` (`ttCollector` only) |
| `$nskr_kq_elizaRaided`, `$nskr_kq_elizaIntercept`, `$nskr_kq_elizaReturning`, `$nskr_kq_elizaRevenge` | Eliza's fleets: role flags of quest `kq` | `QuestFleets` | Rules `# KESTEVEN QUESTLINE: ELIZA FLEETS` |
| `$nskr_kq_elizaFleetDone` | An Eliza fleet that lost Eliza or gave up the chase | `KestevenElizaFleetsModule` | The `withdrawWhen` condition of its role's orders |
| `$nskr_kq_elizaStood` | Local memory of the market entity during the port meeting; expiry `0` | Row `nskr_kq_elizaStand` | Row `nskr_kq_elizaStoodLine` |
| `$nskr_kq_jackRevenge` | Jack's revenge fleet: role flag of quest `kq` | `QuestFleets` | Rules `# KESTEVEN QUESTLINE: AFTERMATH` |
| `$CacheGuardianFleet` (`Cache.CACHE_FLEET_KEY`) | Guardian fleet | `Cache` | `CacheBossTauntPlugin`, rules |
| `$nskr_kq_cacheGuardian` | Guardian fleet: role flag of quest `kq` | `QuestFleets` | Nothing reads it yet |
| `$EnigmaDormantFleet` (`DormantSpawner.DORMANT_KEY`) | Dormant fleets at quest locations | `DormantSpawner.addDormant` | Action `wakeSatelliteGuard`, which keeps the flag on the fleets it wakes; rules `dormantDialog` |
| `$nskr_kq_satelliteGuard` | A dormant fleet woken at satellite #3: role flag of quest `kq` | `QuestFleets.adopt`, from action `wakeSatelliteGuard` | Nothing reads it |
| `$nskr_kq_job1Dormant` | The job 1 tip system's dormant fleet: role flag of quest `kq` | `QuestFleets.adopt`, from `KestevenJob1Module.pickTip` | Nothing reads it yet |
| `$nskr_kq_altEndingLocked` | The official who had the second talk of either alternative ending; no expiry | Rules `nskr_kq_altEndingLuddicDoubtSel`, `nskr_kq_altEndingTtIncreaseSel` | Alternative ending entry and greeting rows |
| `$nskr_ic_messenger`, `$nskr_ic_messengerLeaving` | Messenger fleet role flags of quest `ic` | `QuestFleets` | Rules `# INTERCEPTS` |
| `$missionImportant` with reason `nskr_kq` | Satellite #3, satellite #4, Glacier; the Eliza contact market (`KestevenElizaSearchModule`, scope `JOB5_DISKS`) | Hub actions `markJob3Satellite`, `markJob4Satellite`; `KestevenGlacierModule` action `markGlacier` (`ctx.mark`, scope `JOB5_DISKS` on) | Map markers; `salvageSatellite` and `recoverGlacierDisk` call `ctx.unmark` |
| `$missionImportant` with reason `nskr_kq` | The Cache command core `nskr_cache_core`, when it exists | `KestevenJob5Module` action `markCacheCore`, from the Delve meeting's "I think I already found that place." (`ctx.mark`, scope `JOB5_MEETING` on); `KestevenQuest.showCacheCore` from `JOB5_DISKS` on (every stage) | Map marker; `salvageCacheCore` unmarks it |
| `$missionImportant` with reason `nskr_kq` | Eliza's market entity or the `asteriaOrOutpost` market | `KestevenCacheModule` action `salvageCacheCore` (stages 19, 20 and failure) | Map marker; the endings unset the key directly |
| `$nskr_questDialog` (`quest/QuestDialogs.CLAIM_KEY`) = `nskr_kqGlacier` | Glacier | `markGlacier` (`ctx.claimDialog`, scope `JOB5_DISKS` on); released by `recoverGlacierDisk` | `CorePlugin`, which opens the `# KESTEVEN QUESTLINE: GLACIER` rows |
| `$nskr_questDialog` = `nskr_kqCacheCore` | The command core `nskr_cache_core` | `KestevenQuest.showCacheCore` (`ctx.claimDialog`, every stage) | `CorePlugin`, which opens the `# KESTEVEN QUESTLINE: CACHE` core rows |
| `$playLocationMusicDuringEnc` | Player fleet | `KestevenCacheModule` before opening the inner voice on it | `CampaignState.showInteractionDialog`: the location music keeps playing |
| `$nskr_kq_doubtAskedChief`, `$nskr_kq_doubtAskedSensors`, `$nskr_kq_doubtTip`, `$nskr_kq_doubtGaveUp` | Player fleet, the inner voice's target; expiry 0 | Inner voice rows: operations chief asked, sensors officer asked, the center tip taken, "It's doomed" chosen last | The inner voice's thought line and option menu |
| `$nskr_kq_glacierPath` (`walk`, `cut`, `blast`), `$nskr_kq_glacierShutdown`, `$nskr_kq_glacierLate` | Glacier; expiry 0, so they end with the dialog | Glacier rows: the door choice, the shutdown choice, the countdown reaching 0 | Glacier rows: countdown lines, way back, barrage and congratulations |
| `$nskr_kq_introduced` | Jack, Alice or Nicholas, each their own; no expiry | Hub introduction rows `nskr_kq_jackIntro`, `nskr_kq_aliceIntro`, `nskr_kq_nicholasIntro` | Hub greeting rows |
| `$nskr_kq_partyTechiesDone`, `$nskr_kq_partyCrowdDone`, `$nskr_kq_partyOfficersDone` | The party employee (`nskr_kq_partyEmployee`); no expiry | Party rows `nskr_kq_partyPitchDecline`, `nskr_kq_partyPitchListen`, `nskr_kq_partyToastLeave`, `nskr_kq_partyTarget` | Party group menu `nskr_kqPartyGroups` and `nskr_kqPartyLookText` |
| `$nskr_kq_asked<Topic>` (`askedRogueAi`, `askedShips`, `askedElectronics`, `askedCores`, `askedFought`, `askedEquipment`, `askedEnigma`, `askedNextJob` on Jack; `askedWhere`, `askedAlone`, `askedNecessary`, `askedTarget`, `askedNextJob`, `askedSpecOps`, `askedThreats`, `askedSupplies`, `askedNicholas`, `askedGoal`, `askedInterest`, `askedArtifact` on Alice) | The speaker; no expiry | Hub answer rows `nskr_kq_<speaker>Ask<Topic>Sel` | Hub question rows, which hide an asked question |
| `$nskr_kq_delveAsked`, `$nskr_kq_delveAskedEnigma`, `…Comms`, `…Cache`, `…Chip`; `$nskr_kq_delveAdvanceOffered` | Jack, expiry `0` (the meeting has no way out before its end) | Delve meeting answer rows `nskr_kq_delveAsk…`; `nskr_kq_delveDoubt` | The meeting's question menu `nskr_kqDelveQuestions`; the advance payment `nskr_kq_delveAdvancePaid` |

## Who changes the stage

`quest/QuestManager` is the only writer of the stage. Rows change it with `nskr_quest kq advance FROM TO`, modules with `ctx.advance`, and `KestevenQuest.reportCacheGuardianDefeated` with `advance` on a context from `QuestManager.get().context("kq", ...)`, whose changes are logged as `[kq] FROM -> TO (rule outside)`.

| Writer | Stage changes |
|---|---|
| Hub confirmation rows, `nskr_quest kq advance` | 0→1, 2→6, 6→7, 7→8, 10→11, 11→12, 13→14, 16→17 |
| Hub row `nskr_kq_jackJob5Brief` | 14→15 |
| Hub row `nskr_kq_aliceRefuseConfirm` | 7→11 |
| Hub action `storySkip` (`QuestManager.jump`) | 0, 6, 7, 11 or 14 to 17, through every stage between ([Stage jumps](#stage-jumps)) |
| Dev menu (`nskr_questDev`, `QuestManager.jump`) | Any stage, through every stage between or after a reset |
| `KestevenJob1Module` (`job1Progress` action, daily tick) | 1→2 |
| `KestevenAftermathModule` (`onDay`, both mission markets lost) | any→99 |
| `KestevenCacheModule` (`onStage`, `onLocationChanged`) | 16→17 when the Cache is found |
| `KestevenJob4Module` (both job 4 fleets taken care of; the Special Operations fleet attacked) | 12→13; 12 to 16→14, then 14→99 |
| `KestevenElizaFleetsModule` (Eliza killed after the hand-over: `onBattle`, `onFleetGone`, the hand-over action, `onDay`) | 19→99 |
| `KestevenJob3Module` (expedition beaten, timer over, stealth broken) | 8 or 9→10 |
| Party row `nskr_kq_partyCoordinates` (`nskr_quest kq advance`) | 8→9 |
| Row `nskr_kq_delveLeave` (Delve meeting), `nskr_quest kq advance` | 15→16 |
| `Cache.CacheGuardInteractionConfig`, through `KestevenQuest.reportCacheGuardianDefeated()` | 16 or 17→18 |
| `KestevenCacheModule` action `salvageCacheCore` | 16, 17 or 18→19 |
| Rows `nskr_kq_kestevenEndingDone` and `nskr_kq_elizaEndingDone` (`nskr_quest kq advance`) | 19→20 |
| `KestevenAltEndingsModule` action `altEndingFallout` (`ctx.advance`) | 19→20 |

## Stage jumps

`QuestManager.jump` serves the dev menu and the player's story skip ([A stage jump](../../jars/src/lostsector/quest/README.md#a-stage-jump)). For every stage it passes, the modules active there run `onSkip`, which sets what that stage's conversations and events would have set. The stage changes between run the usual `onStart`, `onStage` and `onStop` hooks. A jump grants no payouts, reputation or items.

### Passed stages

| Passed stage | `onSkip` sets (module) | Left out, and why |
|---|---|---|
| `NOT_STARTED`, `JOB1_DONE`, `JOB3_OFFERED`, `JOB3_BRIEFING`, `JOB3_DONE`, `JOB5_OFFERED`, `JOB5_MEETING` | Nothing | Optional talk (`FOUGHT_ENIGMA`, `JOB1_TIP_GIVEN`, the Delve meeting's advance and Cache core marker); the job 3 refusal (`JOB3_REFUSED`, stage 7 to 11) is not on the path |
| `JOB1_ACTIVE` | `JOB1_SENSOR_DATA` (Job 1); `JOB1_DATA_DELIVERED`, `JOB1_ELECTRONICS_DELIVERED` (hub) | |
| `JOB3_ACTIVE` | `JOB3_TARGET_DISCOVERED` (party) | The party's drinks and hangover bill |
| `JOB3_TARGET_KNOWN` | Nothing: the expedition counts as beaten, `JOB3_FAILED` stays unset | |
| `JOB4_WAITING` | `JOB4_WAIT_OVER`; the timer `job4Wait` is cleared (Job 4) | `JOB4_REQUIREMENT_SKIPPED` |
| `JOB4_ACTIVE` | `JOB4_TARGET_DESTROYED` (Job 4) | `JOB4_FRIENDLY_FOUND`: the hint wreck's result row `nskr_kq_hintWreckFound` reads it in every stage, and the story skip places the wreck without the player having found the Special Operations fleet. The optional talk and help flags (`JOB4_FRIENDLY_TALKED`, `JOB4_TARGET_HINT`, `JOB4_FRIENDLY_HELPED`) and Nicholas's tip |
| `JOB4_DONE` | Nothing | Jack's importance (hub action `raiseJackImportance` at Alice's turn-in): vanilla's `ContactIntel` shows a contact's importance, and the story skip never raised it |
| `JOB5_DISKS` | `JOB5_JACK_TIP`, `JOB5_ALICE_TIP`, `JOB5_ALICE_TIP2` (hub); `FROST_FOUND`, `GLACIER_DISK_RECOVERED` with its disk (Glacier); `ELIZA_FOUND`, `elizaSearchStage` 3, `elizaMarket` when unset (Eliza search); `SATELLITE3_RECOVERED`, `SATELLITE4_RECOVERED` with a disk for each satellite not yet salvaged, `satellitesRecovered` 2 (satellites); Eliza generated and added to her market when she does not exist, `ELIZA_DIALOG_FINISHED`, `ELIZA_HELPED` and her two disks (Eliza); `ALL_DISKS_RECOVERED` once the disks reach five; `CACHE_FOUND` (Cache) | Alice's satellite and Glacier markers, and her Frost tip system (`pickJob5FrostTip`), whose draw would move the story skip's Eliza market draw. The placed satellites stay unsalvaged ([defect 11](KESTEVEN_QUESTLINE.md#defects-found-by-reading-the-source)) |
| `CACHE_KNOWN` | Nothing | The guardian stays unfought and appears when the player arrives |
| `CACHE_CLEARED` | `CORE_SEEN`, `CHIP_SALVAGED` (Cache) | The core's rewards and the return marker |
| `CHIP_RECOVERED` | `ELIZA_ENDING_DONE` after the hand-over to Eliza, otherwise `KESTEVEN_ENDING_DONE` (endings) | The endings' rewards, relationships and unlocked settings |

A dev jump that ends in `JOB4_DONE`, `JOB5_OFFERED`, `JOB5_MEETING` or `JOB5_DISKS` starts job 4 as in play, so the strike group is still alive although `JOB4_TARGET_DESTROYED` is set, and `JOB4_FRIENDLY_FOUND` stays unset; only the dev menu reaches this state.

### Hooks during a jump

A module the jump passes whole, because the target is not one of its stages (`ctx.isJump() && !isActiveIn(ctx.jumpTarget())`), places only the objects later stages find. A module active in the target runs its hooks as in play.

| Module | Passed whole | Target in its stages |
|---|---|---|
| `KestevenJob1Module` | No tip system, intel or map update | Tip picked, intel shown, completed at `JOB3_OFFERED` |
| `KestevenJob3Module` | The target, satellite #3 and the dormant fleet there, in the old story skip's order; no start market, intel, expedition or countdown | As in play |
| `KestevenPartyModule`, `KestevenElizaSearchModule` | No quest people | People created |
| `KestevenJob4Module` | The strike group with satellite #4, then the wrecks, in the old story skip's order; no intel, Special Operations fleet or splinters | As in play |

`KestevenJob5Module` and `KestevenCacheModule` act in `onStage` only in the jump's target stage (`ctx.stage() != ctx.jumpTarget()` returns): a passed meeting gets no guard, and the Delve and `cache` entries are shown once, with the text of the target stage.

### Story skip

The hub action `storySkip` jumps to `CACHE_KNOWN`, then clears `nskr_starfarerFromStart` and sets `STORY_SKIPPED`. Compared with the old skip, a single stage change to 17, the end state has:

- the same flags the old skip set (`CACHE_FOUND` now from `KestevenCacheModule.onSkip`), the Delve and `cache` entries shown once at `CACHE_KNOWN` in that order, and Eliza generated at her market;
- the job 3 objects when the skip starts at stage 7 or earlier and the job 4 objects at 11 or earlier, with the same `kestevenQuestRandom` draws in the same order: job 3 target, satellite #3, strike group, satellite #4, wrecks, Eliza's market;
- no intel, fleets, people or timers of the passed jobs;
- in addition, for the stages it passes: `JOB1_SENSOR_DATA`, `JOB1_DATA_DELIVERED`, `JOB1_ELECTRONICS_DELIVERED`, `JOB3_TARGET_DISCOVERED`, `JOB4_WAIT_OVER`, `JOB4_TARGET_DESTROYED`, `ALL_DISKS_RECOVERED`, `disksRecovered` 5, `elizaSearchStage` 3, and the passed stages in the state's reached set.

Nothing reads the additions at `CACHE_KNOWN` or later: the job 1 flags only at `JOB1_ACTIVE` (hub rows, `job1` intel rows); `JOB3_TARGET_DISCOVERED` only in the party at stages 8 and 9, Alice's `JOB3_DONE` question and her stage 16 lead lines; `JOB4_WAIT_OVER` only at stage 11; `JOB4_TARGET_DESTROYED` only in the `job4` intel rows at stages 12 and 13 and in `KestevenJob4Module`, which stops at 17; the disk count, `ALL_DISKS_RECOVERED` and `elizaSearchStage` only in stage 16 rows, the `job5` intel rows at stage 16 and the Eliza search checks; `reached` only as `reached JOB5_OFFERED` in the `job4` intel rows. No other quest, hint or `KestevenQuest` query reads them.
