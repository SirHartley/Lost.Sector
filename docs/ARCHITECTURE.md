# Architecture

Technical routing for the current implementation. Java paths below are relative to `src/lostsector/`; data paths are repository-relative. Search the named owner before adding another implementation.

| Reference | Scope |
|---|---|
| [CLAUDE.md](../CLAUDE.md) | Workflow, build gate and document upkeep |
| [DIALOGUE.md](DIALOGUE.md) | All player-facing text, rules or Java: workflow, shared text presentation and dialogue flow; prose constraints in LORE.md |
| [LORE.md](LORE.md) | Setting facts, knowledge limits, character voices and source-labelled prose examples |
| [RULES.md](RULES.md) | Rules syntax, execution and project routing contracts |
| [RULES_AUTHORING.md](RULES_AUTHORING.md) | Using and debugging commands, memory and text replacements, including Java integration; vanilla dictionaries and source corrections |
| [UI.md](UI.md) | Java custom panels, widgets, renderers, sprites, tooltips, layout and input; shared text guidelines in DIALOGUE.md |

## Start here

| Change / symptom | Route |
|---|---|
| Kesteven questline stage or job | `rules.csv -> rulecmd/nskr_kestevenQuest -> quests/util/QuestUtil.getStage/setStage`; automatic transitions in `quests/util/QuestStageManager.advance()`; intel `intel/KQuest*Intel` |
| Quest entity opens the wrong dialog | `CorePlugin.pickInteractionDialogPlugin -> quests/*Dialog`, `fleets/events/BlacksiteDialog`, `fleets/bounties/MothershipInteractionBlocker`, custom-start FIDs |
| Endings and the production chip | `quests/EndingKestevenDialog`, `quests/EndingElizaDialog`, `rulecmd/nskr_altEndingDialogLuddic/TT -> QuestUtil.saveEnding()`; `econ/UnlimitedProductionChipCondition -> fleets/BlackOpsManager.getUPC()` |
| Named bounties | `fleets/bounties/*Spawner -> intel/HintManager -> intel/*Intel -> loot/BountyLoot` |
| Roaming Enigma fleets | `fleets/HyperspaceEnigmaSpawner`, `fleets/StalkerSpawner`, `procgen/DormantSpawner`, `procgen/EnigmaBaseSpawner` + `EnigmaDefenderPlugin`; officers `EnigmaAIConverter`; loot `loot/EnigmaFleetLootGenerator` |
| Event fleets | `fleets/events/InterceptManager`, `fleets/events/LoanShark`, quest fleets from `QuestStageManager` via `quests/util/QuestFleets`; dialogue in `rules.csv` or `rulecmd/nskr_ttCollectorDialog`, `nskr_loanSharkDialog`, `nskr_elizaInterceptDialog` |
| Debt, ship swap, S-mod removal | Official menus in `rules.csv -> rulecmd/nskr_debt`, `nskr_shipSwap`, `nskr_modRemoval`; monthly interest `CrushingDebt` |
| Contracts | `person_missions.csv -> rulecmd/Contracts -> intel/ContractIntel`; `quests/jobs/ContractManager` |
| Blacksites | `procgen/BlacksiteSpawner -> fleets/events/BlacksiteManager -> CorePlugin -> BlacksiteDialog` |
| Custom starts | Nexerelin background -> `customStart/GamemodeManager` -> `HellSpawnManager` or `ThronesGiftManager`; unlocked by `LOST_SECTOR_cfg.json` |
| New-game content, adding the mod to a save | `ModPlugin.onNewGame*` and the `SAVE_KEY` check in `onGameLoad`; [world generation](#save-identity) |
| Difficulty and fleet scaling | `ModPlugin` getters -> LunaLib or `LOST_SECTOR_OPTIONS.ini` |
| Value lost after save/load | [Save identity](#save-identity) |
| Rules menu, option routing, highlights | [Project routing](RULES.md#project-routing), [shared text presentation](DIALOGUE.md#shared-text-presentation) |
| Command arguments, mission calls, memory lifetime, missing text replacements | [Rules implementation guide](RULES_AUTHORING.md) and its dictionaries, including for Java-only fixes |
| Hullmod, system or weapon behavior | [Combat data bindings](#combat-data-bindings) -> class in `hullmods/`, `shipsystems/`, `weapons/` |
| Prototype versus Enigma presentation | `util/MiscLS.protOrEnigma()` / `isProtTech()` |
| Missile AI | `ModPlugin.pickMissileAI()` -> `weapons/ai/` |
| Combat effects | `plugins/`, GraphicsLib data in `data/lights` and `data/trails`, MagicLib `MagicRender` |


## Registration and lifecycle

| Hook | Owners |
|---|---|
| `ModPlugin.onApplicationLoad()` | GraphicsLib shader, texture and light data (skipped if GraphicsLib classes are missing); the `IS_*` optional-mod flags; Nexerelin `Nex_TransferMarket.NO_TRANSFER_FACTIONS` gains `enigma`; `createDefaultConfig()` |
| `ModPlugin.pickMissileAI()` | `nskr_emglShot_sub` -> `EmglStuckAI`, `nskr_tremor1` -> `TremorAI`. No ship, weapon or drone AI picker is overridden. |
| `ModPlugin.onGameLoad()` | Order below. Runs for new games and loaded saves. |
| `ModPlugin.beforeGameSave()` | `Saved.updatePersistentData()`, `CampaignTimer.save()`, then removes every `EFS_LIST` script and listener from the sector |
| `ModPlugin.afterGameSave()` | Re-adds `EFS_LIST`, then `Saved.loadPersistentData()` |
| `ModPlugin.onNewGame()` | Reserves procgen system names; Arcadia/Asteria (Corvus mode or no Nexerelin); Kesteven bounty participation and relations; writes `SAVE_KEY` and `STARFARER_MODE_FROM_START_KEY` |
| `ModPlugin.onNewGameAfterProcGen()` | Frost part 1 and Outpost (Corvus mode or no Nexerelin); Mothership planets, Enigma bases, dormant spawns, environmental storytelling, Cache system |
| `ModPlugin.onNewGameAfterEconomyLoad()` | Frost part 2 market (Corvus mode or no Nexerelin), `RogueSpawner.spawnRogues()` |
| `ModPlugin.onNewGameAfterTimePass()` | Frost and Outpost in Nexerelin random-core games, IndEvo features, `Gen.genPeople()`, Frost ruins, `DesertFixer.fix()`, blacksites, Mothership fleet, Enigma relations |

`onGameLoad` order: Nexerelin null-manager guard -> `EFS_LIST` construction (once per client session, guarded by the instance field `init`) -> `HellSpawnDisposableFleetSpawner` and `ThronesGiftDisposableFleetSpawner` behind `hasScript` -> `KestevenMirror.borrowIndieBlueprints()` -> `BlackOpsSetup.scanWeaponBlueprints()` -> `syncNSKRScripts()` -> `registerPlugin(new CorePlugin())` -> `EFS_LIST` as transient scripts, transient listeners and listener-manager listeners, with `ThronesGiftManager.reset()` -> `Saved.loadPersistentData()` -> `KestevenTipBarCreator` -> Starfarer-mode check -> new-save generation -> `FleetUtil.hackBrokenVariants()`.

A save without `ModPlugin.SAVE_KEY` (`nskr_enabled`) in sector persistent data runs all four `onNewGame*` hooks from `onGameLoad`, then adds a Kesteven station commander to `nskr_asteria`. This is how the mod is added to an existing save.

Nexerelin random-core games never generate Arcadia/Asteria, and `Gen.genPeople()` places Michael, Jack and Alice only on Asteria and Nicholas only in Corvus mode. `ExileManager.exile()` creates any missing quest people on the Outpost.

Scripts use two lifecycles:

| Tier | Members | Lifecycle |
|---|---|---|
| Transient `EFS_LIST` | Built once per client session and reused for every save loaded in it: `HyperspaceEnigmaSpawner`, `HintManager`, `RorqSpawner`, `EternitySpawner`, `EnigmaBlowerUpper`, `StalkerSpawner`, `EnigmaRelationsFixer`, `KestevenScavenger`, `KestevenExportManager`, `GuardSpawner`, `AbyssSpawner`, `QuestStageManager`, `ExileManager`, `LoanShark`, `InterceptManager`, `BlackOpsManager`, `ContractManager`, `EnigmaHullmodListener`, `MothershipSpawner`, `BlacksiteManager`, `EnigmaAIConverter`, `GamemodeManager`, `ThronesGiftManager`, `HellSpawnManager`, `CustomCampaignListener`; `HellSpawnNexListener` only with Nexerelin | Each is a `BaseCampaignEventListener` and `EveryFrameScript`, advanced every campaign frame. Removed before each save and re-added after it. Only `Saved` fields are reloaded per save; other instance fields carry over from the previous save loaded in the session. |
| Saved scripts | `EnigmaFleetLootGenerator`, with listeners `CrushingDebt`, `LicensingFees` and `ComCrewsBonus` registered inside its guard; `BountyLoot`; generic plugin `EnigmaDefenderPlugin` | Added once by `syncNSKRScripts()` behind `hasScript`/`hasPlugin` checks and serialized with the save. The three economy listeners react to `reportEconomyTick`. |

`CorePlugin` is a transient `BaseCampaignPlugin`: vanilla drops transient plugins when saving, so `onGameLoad` registers it again. Its `pickInteractionDialogPlugin` routes quest and event entities to their Java dialogs by entity ID and memory state.

Other registrations: `KestevenTipBarCreator` bar event creator, guarded by `hasEventCreator`; `ElizaRaidObjectiveCreator`, added by `ElizaDialog` during that dialog. Combat listeners added to ships and entities last for one battle.

| Registry | Owner / consumer |
|---|---|
| `data/config/settings.json` | Rule command package `lostsector.campaign.rulecmd`; combat plugins `nskr_kaboomPlugin`, `nskr_entrancePlugin`, `nskr_SplitterWeaponPlugin`, `nskr_teleporterPlugin`, `nskr_tauntPlugin` (classes in `plugins/`); sprite categories; design-type colours; `bonusXP` for story-point options |
| `LOST_SECTOR_OPTIONS.ini` | Difficulty and fleet-scaling settings without LunaLib; `ModPlugin.loadSettings()` |
| `data/config/LunaSettings.csv`, `LunaSettingsConfig.json` | The same settings in LunaLib's menu, read through `ModPlugin` getters when LunaLib is enabled |
| `data/config/LOST_SECTOR_cfg.default` | Default for the per-installation `LOST_SECTOR_cfg.json` (MagicLib `JSONUtils`): `completedStory`/`completedStoryHard`, written by `QuestUtil` and read by the custom-start backgrounds and `nskr_kestevenQuest` |
| `data/campaign/rules.csv` | Dialogue; see [project routing](RULES.md#project-routing) |
| `data/campaign/person_missions.csv` | `Contracts` mission offer |
| `data/campaign/abilities.csv` | `nskr_hellSpawnAbility` -> `campaign/customStart/abilities/HellSpawnAbility` |
| `data/campaign/market_conditions.csv` | `nskr_enigmaPop` -> `EnigmaPopCondition`, `nskr_upChip` -> `UnlimitedProductionChipCondition`, `nskr_hellSpawnCondition` -> `HellSpawnCondition` (`campaign/econ/`) |
| `data/campaign/commodities.csv`, `special_items.csv` | `nskr_electronics`; prototype blueprint packages `nskr_prot_wp`, `nskr_prot_light`, `nskr_prot_heavy` |
| `data/campaign/procgen/*.csv`, `sim_opponents.csv` | Planet type `nskr_ice_desert`; salvage rows `nskr_enigmabase`, `nskr_heart_wreckage`, `nskr_blacksite_*`; drop groups; simulator opponents |
| `data/config/custom_entities.json` | Entity specs for Java `addCustomEntity` calls |
| `data/config/sounds.json` | Music and sound IDs played from Java |
| `data/world/factions/factions.csv` | `enigma`, `kesteven`, `prot_ops`, `ai_all`; the other `.faction` files add to vanilla factions |
| `data/config/modSettings.json` | Exotica faction list; an empty MagicLib `bounty_board` |
| `data/config/upgrades.json` | Exotica upgrades `temporalConduits`/`spaceTimeAnchor` -> `hullmods/exotica/` |
| `data/config/exerelin/`, `exerelinFactionConfig/` | Nexerelin backgrounds `HellSpawnBackground`/`ThronesGiftBackground` and faction configs |
| `data/config/{exoticaFactionConfig,indEvo,BetterColonyConfig,CommissionBonus,ExiledSpace,prism,ruthlesssector,starship_legends}/` | Read only by those mods |
| `data/config/version/version_files.csv` | Registers `lostsector.version` with Version Checker |

IntelliJ compiles to `jars/production` and builds the `jars/Lost.Sector.jar` artifact. Build procedure: [CLAUDE.md](../CLAUDE.md#building).

### Optional integrations

`ModPlugin.onApplicationLoad()` sets each flag once from `isModEnabled`. None of these mods is a `mod_info.json` dependency.

| Flag (mod ID) | Gated owners |
|---|---|
| `IS_NEXELERIN` (`nexerelin`) | World generation mode, `HellSpawnNexListener`, diplomacy calls in `QuestStageManager`, `HellSpawnManager`, `ExileManager`, `Gen`. Also cleared in `onGameLoad` when `SectorManager.getManager()` is null. `HellSpawnBackground`/`ThronesGiftBackground` extend Nexerelin's `BaseCharacterBackground` and are reached only through Nexerelin's background CSV. |
| `IS_INDEVO` (`IndEvo`) | IndEvo features in `onNewGameAfterTimePass`, `Frost`, `Outpost`, `Gen`, `ExileManager`, `EnigmaBlowerUpper`, `ContractInfo`; `getIndEvoBoolean` |
| `IS_EXOTICA` (`exoticatechnologies`) | `Cache`. `hullmods/exotica/*` extend Exotica's `Upgrade` and are reached only through `upgrades.json`. |
| `IS_LUNALIB` (`lunalib`) | `ModPlugin` settings getters; otherwise `LOST_SECTOR_OPTIONS.ini` |
| `IS_TAHLAN` (`tahlan`) | `ContractInfo` reward table |
| `IS_CC` (`timid_commissioned_hull_mods`) | `ComCrewsBonus` |
| `IS_IRONSHELL` (`timid_xiv`) | `EndingElizaDialog`, `QuestStageManager` |

Keep foreign classes behind these flags or behind the foreign mod's own loader. A class that references a foreign type must not be loaded when that mod is absent.

## Save identity

| Mechanism | Stored as | Rename hazard |
|---|---|---|
| `Saved<T>` | Sector persistent data under `Saved.PREFIX` (`nskr_`) plus the constructor key. `ModPlugin` writes all instances before save and reloads them on load and after save. | Changing a key loses that value. `InterceptManager` and `HyperspaceEnigmaSpawner` keys already contain `nskr_`, so their stored keys start `nskr_nskr_`; `BlackOpsManager` uses `nksr_blackOpsManagerCounter` (stored as `nskr_nksr_...`). Preserve these exact strings or migrate them. |
| `CampaignTimer` | The timer object itself, in sector persistent data under the owner's fully qualified class name plus `Timer` | Moving or renaming `GamemodeManager`, `ThronesGiftManager` or `HellSpawnManager` silently starts a fresh timer; renaming `CampaignTimer` breaks loading. `KillBrainManager` also uses one, though it is not registered. |
| Saved scripts and plugins | The objects listed under Saved scripts above | Their class names and fields are serialized. |
| `ModPlugin.SAVE_KEY` `nskr_enabled`, `STARFARER_MODE_FROM_START_KEY` `nskr_starfarerFromStart` | Sector persistent data | Renaming `SAVE_KEY` reruns world generation on every existing save. |
| `Frost.NAME_KEY` `$nskr_frostName` | Sector persistent data, not memory, despite the `$` | Holds the generated Frost system name. |
| `LOST_SECTOR_cfg.json` | Per installation, outside saves | Shared by all campaigns on that machine. |

Stable IDs are in `util/IdsLS`: factions `kesteven`, `enigma`, `prot_ops`, `ai_all`; people `nskr_opguy`, `nskr_researcher`, `nskr_intelligence`, `nskr_anarchist`, `nskr_president`, `nskr_enigmaAdmin`, `nskr_thrn`; entities and markets `nskr_heart`, `nskr_asteria`, `nskr_outpost`, `nskr_enigmabase`, `nskr_blacksite`, `nskr_anomalous_station`. `addMarketplace` uses the primary entity's ID as the market ID.

World generation is static calls from the `ModPlugin` hooks; `world/Gen.generate()` is empty.

| Owner | Creates / stable IDs |
|---|---|
| `world/systems/arcadia/Arcadia` | Asteria in vanilla Arcadia: `nskr_asteria`, `nskr_asteria_station` |
| `world/systems/frost/Frost` | New system with a random name from `SYS_NAME_LIST`: planets `nskr_bleak`, `nskr_glacier`, `nskr_siberia`, `nskr_shiver`, `nskr_algor`; `nskr_heart`; `nskr_frost_gate`, `nskr_frost_relay`; debris and derelicts |
| `world/systems/cache/Cache` | System `Unknown Site`: gate `nsrk_cacheGate` (misspelled prefix; keep for saves), `nskr_cache_derelict1`-`4`, `nskr_cache_core` |
| `world/systems/outpost/Outpost` | In a random system near the core: `nskr_outpost`, `nskr_outpost_gate`, `nskr_outpost_relay` |
| `world/Gen` | People at fixed markets through `genPeople()`; `genEliza()` (`nskr_anarchist`) is called from `nskr_kestevenQuest` and `ElizaDialog` |
| `world/DesertFixer` | Planet-condition fix after time pass |

Per-fleet and per-entity memory keys such as `$mothershipLoot`, `$StalkerFleet` and `$BetrayalFleet` are unprefixed. They belong to that fleet or entity's memory; namespace new keys with `nskr_`. Sector-scope keys use `$nskr_`. `$nex_uninvadable` and `$nex_do_not_colonize` are Nexerelin's keys, set on Lost.Sector systems.

## Source owners

Folders contain related effects, AI and helpers; use `rg --files src/lostsector/<folder>` for their complete inventory. The entries below identify state and integration owners, not every class.

### `campaign` root

| File | Owner / connection |
|---|---|
| `CorePlugin.java` | Transient campaign plugin; `pickInteractionDialogPlugin` routes quest, blacksite, Mothership and custom-start entities to Java dialogs |
| `ExileManager.java` | Kesteven exile from Asteria; moves quest people between Asteria and Outpost; `QuestUtil.asteriaOrOutpost()` |
| `KestevenExportManager.java`, `LicensingFees.java` | Kesteven export sets and monthly licensing fees |
| `CrushingDebt.java` | Monthly debt interest for `rulecmd/nskr_debt` |
| `ComCrewsBonus.java` | Monthly crew stipend with Commissioned Crews |
| `EnigmaBlowerUpper.java` | While Enigma owns `nskr_heart`, removes every comm-directory entry except `nskr_enigmaAdmin`, every frame including while paused; Frost intel bootstrap |
| `EnigmaHullmodListener.java` | Enigma tip unlock counts; logic in static `update()` |
| `EnigmaRelationsFixer.java`, `EnigmaAIConverter.java` | Enigma relationship clamp; AI-core officers on spawned Enigma fleets |
| `KestevenMirror.java`, `BlackOpsSetup.java` | Static blueprint setup called from `onGameLoad` |

### `campaign/fleets`, `fleets/bounties`, `fleets/events`, `procgen`

| File | Owner / connection |
|---|---|
| `fleets/HyperspaceEnigmaSpawner`, `StalkerSpawner`, `KestevenScavenger`, `GuardSpawner`, `BlackOpsManager` | Roaming and guard fleets. Enigma spawners skip HellSpawn mode; `BlackOpsManager` spawns from the market with `nskr_upChip`. |
| `fleets/bounties/AbyssSpawner`, `EternitySpawner`, `MothershipSpawner`, `RorqSpawner` | One named bounty fleet each, spawned once per game; loot flag `LOOT_KEY`; `MothershipSpawner` also places the planets and `MothershipInteractionBlocker` |
| `fleets/events/InterceptManager`, `LoanShark` | ARO strike, messenger and auto-hunter fleets; debt collectors |
| `fleets/events/BlacksiteManager`, `BlacksiteDialog`, `BlacksiteInfo` | Blacksite defenders and self-destruct timer; saved `BlacksiteInfo` records |
| `procgen/*` | One-time placement: Enigma bases, blacksites, dormant fleets, Rorqual teaser, storytelling derelicts; `EnigmaDefenderPlugin` supplies salvage defenders |
| `loot/BountyLoot`, `loot/EnigmaFleetLootGenerator` | Encounter loot listeners for the bounties and for Enigma casualties |

### `campaign/quests`, `quests/util`, `quests/jobs`, `graid`, `intel`

| File | Owner / connection |
|---|---|
| `quests/util/QuestStageManager` | Kesteven questline state and automatic transitions, quest fleets, failure to stage 99. Runs while paused. |
| `quests/util/QuestUtil` | Stage and flag accessors over sector persistent data (`getStage`, `getCompleted`, `getFloat`), artifact spawning, `saveEnding()` |
| `quests/util/QuestFleets`, `SimpleFleet`, `SimpleFleetMember`, `SimpleCaptain`, `SimpleSystem`, `FleetInfo` | Fleet and system builders shared by spawners |
| `quests/*Dialog`, `quests/Job4HintWreck` | Java `InteractionDialogPlugin`s opened by `CorePlugin` |
| `quests/KQuest3Bar`, `KQuest5Bar`, `KQuest5ElizaBar*`, `KestevenTipBar`, `KestevenTipBarCreator` | Bar events; `rulecmd/nskr_barEventFixer` adds `KQuest5Bar` and `KestevenTipBarCreator` creates `KestevenTipBar` |
| `quests/jobs/ContractManager`, `ContractInfo` | Contract failure checks; offer reset when its counter reaches 600 seconds (about 60 days) |
| `graid/ElizaRaid`, `ElizaRaidObjectiveCreator` | Ground-raid objective for Eliza's data disks |
| `intel/HintManager`, `HintIntel` | System hints for bounties and Frost |
| `intel/*Intel` | Quest, bounty, contract and cache intel; each adds itself as a script |

### `campaign/customStart`, `customStart/intel`, `customStart/abilities`, `econ`

| File | Owner / connection |
|---|---|
| `customStart/GamemodeManager` | Active mode `DEFAULT`, `THRONESGIFT` or `HELLSPAWN`; checked by most spawners |
| `customStart/HellSpawnManager`, `HellSpawnNexListener`, `intel/HellSpawnEventIntel` | HellSpawn corruption points, stat hullmod level, judgement timer and warning |
| `customStart/HellSpawnJudgement*`, `abilities/HellSpawnAbility*` | Judgement encounter and ability; the FIDs extend `FleetInteractionDialogPluginImpl` and are picked by `CorePlugin` |
| `customStart/ThronesGiftManager`, `intel/ThronesGiftIntel`, `intel/AutomateDialog` | XP-to-automation points and the automation dialog |
| `customStart/*DisposableFleetSpawner` | Vanilla `DisposableFleetManager` subclasses, added in `onGameLoad` behind `hasScript` and saved with the game |
| `customStart/HellSpawnBackground`, `ThronesGiftBackground` | Nexerelin backgrounds unlocked by the completed-story flags |
| `econ/EnigmaPopCondition`, `HellSpawnCondition`, `UnlimitedProductionChipCondition` | Market conditions |

Polling: every `EFS_LIST` manager advances each frame. Most gate their work with a `Saved<Float>` counter and return while paused. Counters add the frame `amount` in seconds, and vanilla `SECONDS_PER_GAME_DAY` is 10, so a threshold of `10f` is one campaign day. Many managers add `2 * amount` while the campaign is in fast advance. `QuestStageManager`, `EnigmaBlowerUpper`, `HellSpawnManager` and `ThronesGiftManager` also do work while paused; `CampaignTimer.advance()` does not count paused time.


### Combat data bindings

| Data | Java convention |
|---|---|
| `data/hullmods/hull_mods.csv` `script` | `lostsector.hullmods.<Class>` |
| `data/shipsystems/*.system` `statsScript` / `aiScript` | `lostsector.shipsystems.<X>Stats` / `lostsector.shipsystems.ai.<X>AI` |
| `data/weapons/*.wpn`, `data/weapons/proj/*.proj` `everyFrameEffect` / `onFireEffect` / `onHitEffect` | `lostsector.weapons.<X>` |
| `data/config/upgrades.json` `upgradeClass` | `lostsector.hullmods.exotica.<X>` (Exotica `Upgrade`, not `BaseHullMod`) |
| `data/config/settings.json` `plugins` | `lostsector.plugins.*` global combat plugins |
| `data/missions/mission_list.csv` | `nskr_test` source is `data/missions/nskr_test/MissionDefinition.java`, outside `src`; `nskr_test_custom` is `src/data/missions/nskr_test_custom/MissionDefinition.java`, extending `missions/BaseRandomBattle` |

Exceptions: `nskr_poorcloak` uses vanilla `PhaseCloakStats`; `nskr_animebad` and `nskr_bosscloak` have no `aiScript`; `nskr_emflak.system` is a WEAPON-type system bound through its weapon (`EmflakEffect`, `EmflakPlugin`); `nskr_boostdrive` and `nskr_powersurge` share `ai.EngineAI`; `nskr_pullback` is both a hidden hullmod (`Pullback`, trail points) and the Dragontail's system (`PullbackStats`/`PullbackAI`). `nskr_mothership_frigate`, `nskr_hellSpawnStats` and `nskr_holySpirit` are never built in; campaign code adds them at runtime (`Mothership`, `HellSpawnManager`, `ThronesGiftManager`).

### Ship families

| Family | Hulls | Owners |
|---|---|---|
| Kesteven | `nskr_prosperity`, `nskr_nighthawk`, `nskr_devilcatcher`, `nskr_blackbird`, `nskr_mercenary`, `nskr_dragontail`, `nskr_kingstork` | One system each; `Takedown` (Blackbird), `Pullback` (Dragontail). Kesteven hullmods: `Inertial`, `Volatile`, `BigBats`, `CriticalArmor`, `CHM_kesteven`, `Hwi`, `Missile_spec`, `Acoils`, `BigLightMags` |
| Unknown Prototype / Project Enigma pairs | `nskr_minokawa`, `nskr_sovereign`, `nskr_nemesis`, `nskr_muninn`, `nskr_warfare`, `nskr_eternity`, `nskr_epoch`, `nskr_epochx`, `nskr_widow`, `nskr_torpor`, each with an `_e` Enigma twin sharing its system | Built-in `nskr_focused_shield` (`Focused_shield`). `MiscLS.protOrEnigma()` reads `nskr_lost_prot` / `nskr_domain_era`; `MiscLS.isProtTech()` reads `nskr_focused_shield` / `nskr_kaboom`. Hull-specific: `Protocol` + `nskr_protocolsystem` (Nemesis), `Causality` + `nskr_causality` (Eternity), `Stasis*` + `nskr_stasisp` + `plugins/TorporSystemLights` (Torpor), `Teleport_dummy` + `WarpStats` (Sovereign) |
| Drones and wings | `nskr_huginn`/`_e` wings; `nskr_aed` (Rupture, Project Enigma) | Huginn arm weapons (`GardeOH`); `Aed` + `KaboomStats` + `plugins/KaboomPlugin` |
| Other hulls | High Tech `nskr_borealis` (`MassTargeting*`), `nskr_malediction`, `nskr_pursuer` (also `nskr_pursuer_wing`), `nskr_reverie` (`MissileSalvo*`); Midline `nskr_verity`, `nskr_stalwart` (`nskr_emflak`); Pirate `nskr_kingslayer`, `nskr_rhea`; Rogue Co. `nskr_rorqual` | One system each |
| Bosses | `nskr_reverie_boss`, `nskr_harbinger_boss`, `nskr_afflictor_boss` | Shared `Demonic_core` and `nskr_bosscloak` (`BossPhaseStats`); main systems `nskr_bfpulse`, `nskr_animebad`, vanilla `acausaldisruptor` |
| Remnant Sunburst | `nskr_sunburst` | `Mothership`/`MothershipFrigateStats`, `nskr_harmonics` (`Harmonics*`) |

Prototype weapons (`prot_wp` tag, Unknown Prototype manufacturer) have one `*Effect`/`*OH` pair each in `weapons/`. Decorative `nskr_prot*_light` weapons use `plugins/PrototypeGlows`.

### `plugins`

| File | Owner / connection |
|---|---|
| `EntrancePlugin` | Travel-drive arrival effects for prototype/Enigma ships; GraphicsLib light and distortion; custom data `ENTRANCE_DATA_KEY` + ship ID |
| `KaboomPlugin` | Rupture post-activation effects from `Aed`'s custom data `KABOOM_DATA_KEY` + ship ID |
| `SplitterWeaponPlugin` | Scans all projectiles for `nskr_pbcc_shot` and spawns `nskr_pbcc_sub`; custom data `nskr_SplitterWeapon` |
| `TauntPlugin` | Cache boss fleet (`Cache.CACHE_FLEET_KEY`): taunt messages, boss music, secondary boss spawn; custom data `TAUNT_DATA_KEY` + fleet ID |
| `TeleporterPlugin` | Static `TELEPORT` map drained every frame; see [dead or dormant](#dead-or-dormant) |
| `PrototypeGlows`, `TorporSystemLights` | Per-weapon `EveryFrameWeaponEffectPlugin`s |

`Demonic_core.IndicatorRenderer` and `Causality.IndicatorRenderer` are the only `CombatLayeredRenderingPlugin`s. Other temporary sprites use MagicLib `MagicRender`, mostly through `util/BlastSpriteCreator` and `util/RenderUtil`. GraphicsLib data: `data/lights/nskr_light.csv`, `nskr_bump.csv` and `data/trails/trail_data.csv`, loaded in `ModPlugin.onApplicationLoad()`.

### `util`

| File | Owner / connection |
|---|---|
| `IdsLS` | Stable faction, person, entity and hullmod IDs; mod ID `lost.sector` |
| `MiscLS` | Shared campaign and combat helpers: colours, prototype/Enigma identity, fixed-location and person lookups, dormant fleets |
| `CombatUtilsLS` | Range queries pinned to LazyLib 2.4b behavior; area damage including station modules |
| `MathUtilLS` | Easing, noise, seeded random ranges (modified from LazyLib) |
| `FleetUtil` | Fleet generation and assignment AI helpers; `hackBrokenVariants()` on load |
| `PowerLevel` | Player fleet strength for encounter scaling |
| `BlastSpriteCreator`, `CampaignBlastSpriteCreator` | Timed blast sprites in combat and campaign (`HellSpawnAbility`) |
| `StringHelper` | Token substitution helpers adapted from Nexerelin |


## Contracts

Rules-engine and menu routing constraints: [RULES.md](RULES.md#project-routing).

### Engine / missions

- A handled `callAction()` must return true. Vanilla treats false as an unhandled action and throws.
- `BaseHubMission` assumes `getPerson()` is non-null in many intel, reward, reputation, and distance paths. Fleet and entity missions must set a person override, usually the fleet commander.
- `setTimeLimit()` is compared with total mission elapsed time, not time in the current stage. Multi-round jobs that remain in `WANTED` must call `setClock()` for each round. Intel must use `getDaysLeft()`.

### Importance, interaction, and fleet AI

- `Misc.makeImportant(entity, reason)` takes a reason without `$`. `BaseHubMission.makeImportant(entity, flag, stages...)` takes a memory key with `$`. Pair each overload with its matching removal call.
- A memory pursuit flag makes a fleet willing to pursue; an explicit `FleetAssignment.INTERCEPT` makes it change course. Use both where immediate pursuit is required.
- Vanilla has no flee assignment. Civilian flight uses `MEMORY_KEY_AVOID_PLAYER_SLOWLY` plus Emergency Burn when available.
- A hostile fleet can still hail the player. `HailPlayer` on `BeginFleetEncounter` opens comms regardless of relationship; `MakeOtherFleetGoAway` handles a negotiated departure.

### Rendering, UI, reflection, and audio

Java custom-panel behavior, sprite state and drawing gotchas are in [UI.md](UI.md). Shared player-facing text guidelines are in [DIALOGUE.md](DIALOGUE.md#shared-text-presentation). Campaign VFX and non-UI engine constraints remain here.

- The Starsector script classloader rejects direct references to `java.lang.reflect.Field` and `Method`. Use `MethodHandle` for reflection.
- Sound IDs are unchecked strings until playback. Validate them against merged sound data. Starsector JSON supports `#` comments and trailing commas, and sound entries may be arrays or objects.
- `playUISound` expects stereo; positional `playSound` requires mono; loops should be mono.
- GraphicsLib's combat-engine storage and viewport helpers are unavailable in the campaign layer.

### Shared cross-file constraints

- Custom entity `init()` calls `super.init()`; do not shadow the inherited entity field.

## Live hazards

| Location | Hazard |
|---|---|
| `data/campaign/person_missions.csv` | The `Contracts` row names plugin `lostsector.rulecmd.campaign.Contracts`; the class is `lostsector.campaign.rulecmd.Contracts`. Vanilla `PersonMissionSpec.createMission()` instantiates this name. Runtime result not yet confirmed. |
| `data/config/LunaSettings.csv` | Rows use mod ID `lost_sector`; the `ModPlugin` getters query `IdsLS.LOST_SECTOR_MOD_ID` (`lost.sector`). |
| `ModPlugin.getRandomEnigmaFleetSizeMult()` | Without LunaLib, Starfarer and easy mode return the scripted-fleet multipliers instead of the Enigma ones. |
| `CampaignTimer` | Reads its stored value only in its constructor, once per client session. Loading a second save in the same session keeps the first save's timers, and `CampaignTimer.save()` writes them into the second save. |
| `ModPlugin.onGameLoad()` | `IS_NEXELERIN` is cleared for the rest of the session if `SectorManager.getManager()` is null on any load. |
| `rulecmd/nskr_loanSharkDialog`, `nskr_ttCollectorDialog` | `case "setPaid"` has no `break` and falls into `canPay`. No row calls `setPaid`. |
| `data/config/modSettings.json` | `MagicLib.bounty_board` is empty and only `modFiles/magicBounty_data_example.json` exists, so no MagicLib bounty is registered. |
| `data/weapons/nskr_tremors.wpn` | A commented `everyFrameEffect` names the removed `scripts.kissa.LOST_SECTOR` package. |

## Dead or dormant

| Component | State |
|---|---|
| `campaign/quests/characters/KillBrainManager`, `KillBrainStartDialog` | Not registered; `EFS_LIST.add` is commented out in `ModPlugin` |
| `rulecmd/nskr_advanceKStage`, `nskr_makeHostile`, `nskr_hasMemoryKeyStartsWith` | No rule or Java caller; `CorePlugin` has its own `hasMemoryKeyStartsWith` |
| `campaign/util/CustomCampaignListener` | Registered in `EFS_LIST`; empty |
| `plugins/TeleporterPlugin` | Registered and run every frame; every `addTeleportation()` call is commented out |
| `Saved.deletePersistantData()` | No caller |
| `KestevenMirror`, `BlackOpsSetup` `EveryFrameScript` methods | Never instantiated as scripts |
| `GamemodeManager` timer branch | Empty. Its `CampaignTimer` timeout is `1f` seconds, like the other `CampaignTimer` owners. |
| `hullmods/StupidFuckingHax` as `nskr_stupidFuckingHax` | Not built into any hull; the same class also backs `nskr_emCore` on `nskr_pursuer` |
