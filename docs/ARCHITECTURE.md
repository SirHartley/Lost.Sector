# Architecture

Technical routing for the current implementation. Java paths below are relative to `jars/src/lostsector/`; inside the campaign sections they are relative to `campaign/`; data paths are repository-relative. Search the named owner before adding another implementation.

| Reference | Scope |
|---|---|
| [CLAUDE.md](../CLAUDE.md) | Workflow, build gate and document upkeep |
| [DIALOGUE.md](DIALOGUE.md) | All player-facing text, rules or Java: workflow, shared text presentation and dialogue flow; prose constraints in LORE.md |
| [LORE.md](LORE.md) | Setting facts, knowledge limits, character voices and source-labelled prose examples |
| [RULES.md](RULES.md) | Rules syntax, execution and project routing contracts |
| [RULES_WRITING.md](RULES_WRITING.md) | Writing rules content: process, structure choice, conditions, state, text, options, exits and layout |
| [RULES_AUTHORING.md](RULES_AUTHORING.md) | Using and debugging commands, memory and text replacements, including Java integration; vanilla dictionaries and source corrections |
| [UI.md](UI.md) | Java custom panels, widgets, renderers, sprites, tooltips, layout and input; shared text guidelines in DIALOGUE.md |
| [Quest implementation](quests/README.md) | Kesteven questline stages, state and dialogue map; contracts and bounties |

## Start here

| Change / symptom | Route |
|---|---|
| Kesteven questline stage or job | `rules.csv -> dialogue/rules/nskr_kestevenQuest -> kesteven/quest/QuestHelper.getStage/setStage -> quest/QuestManager` (quest `kq`, state `kesteven/quest/KestevenState`); automatic transitions in `kesteven/quest/QuestStageManager.advance()`; intel `kesteven/quest/*Intel`; [questline walkthrough](quests/KESTEVEN_QUESTLINE.md), [saved state](quests/KESTEVEN_STATE.md) |
| Quest entity opens the wrong dialog | `CorePlugin.pickInteractionDialogPlugin -> quests/*Dialog`, `events/blacksite/BlacksiteDialog`, `bounties/mothership/MothershipInteractionBlocker`, custom-start FIDs |
| Endings and the production chip | `kesteven/quest/EndingKestevenDialog`, `kesteven/quest/EndingElizaDialog`, `dialogue/rules/nskr_altEndingDialogLuddic/TT -> QuestHelper.saveEnding()`; `kesteven/quest/UnlimitedProductionChipCondition -> kesteven/BlackOpsManager.getUPC()` |
| Named bounties | `bounties/*/*Spawner -> events/hints/HintManager -> intel/*Intel -> bounties/BountyLoot`; [bounty structure](quests/CONTRACTS_AND_BOUNTIES.md#named-bounties) |
| Roaming Enigma fleets | `enigma/HyperspaceEnigmaSpawner`, `enigma/StalkerSpawner`, `enigma/DormantSpawner`, `enigma/EnigmaBaseSpawner` + `EnigmaDefenderPlugin`; officers `enigma/EnigmaAIConverter`; loot `enigma/EnigmaFleetLoot` |
| Event fleets | Quest `ic` in `events/intercepts` (ARO strike, "LZ" messenger, Auto-Hunter; module `quest/modules/InterceptEncounter`, rows `# INTERCEPTS`), `kesteven/loans/LoanShark`, quest fleets from `QuestStageManager` via `kesteven/quest/KestevenFleets`; dialogue in `rules.csv` or `dialogue/rules/nskr_ttCollectorDialog`, `nskr_loanSharkDialog`, `nskr_elizaInterceptDialog` |
| Debt, ship swap, S-mod removal | Official menus in `rules.csv -> dialogue/rules/nskr_debt`, `nskr_shipSwap`, `nskr_modRemoval`; monthly interest `kesteven/loans/CrushingDebt`. The debt menu's options and text are rows in `# DEBT options`, and `nskr_debt` answers availability and moves the credits. Ship swap and S-mod removal text is in their `rules.csv` blocks; the commands keep the pickers, stock, points and game actions and fire rows from picker callbacks ([project routing](RULES.md#project-routing)) |
| Contracts | `person_missions.csv -> kesteven/contracts/ContractsMission -> kesteven/contracts/ContractIntel`; `kesteven/contracts/ContractManager`; [contracts](quests/CONTRACTS_AND_BOUNTIES.md#contracts) |
| Blacksites | `events/blacksite/BlacksiteSpawner -> events/blacksite/BlacksiteManager -> CorePlugin -> BlacksiteDialog` |
| Custom starts | Nexerelin background -> `starts/GameModeManager` -> `HellSpawnManager` or `ThronesGiftManager`; unlocked by the `Setting.THRONES_GIFT_UNLOCKED` and `HELLSPAWN_UNLOCKED` [settings](#settings). Throne's Gift automation: `ThronesGiftIntel` button -> `rules.csv` `# THRONES GIFT` -> `dialogue/rules/nskr_thronesGift` -> `ThronesGiftManager.automate()` |
| New-game content, adding the mod to a save | `ModPlugin.onNewGame*` and the `SAVE_KEY` check in `onGameLoad`; [world generation](#save-identity) |
| Difficulty, fleet scaling and other settings | `settings/Difficulty.scriptedFleetMult()`, `randomEnigmaFleetMult()`, `isStarfarer()` -> `settings/Setting` -> `settings/SettingsManager` cache of `data/config/LunaSettings.csv` and LunaLib; [settings](#settings) |
| Value lost after save/load | [Save identity](#save-identity) |
| Rules menu, option routing, highlights | [Writing rules](RULES_WRITING.md), [project routing](RULES.md#project-routing), [shared text presentation](DIALOGUE.md#shared-text-presentation) |
| Command arguments, mission calls, memory lifetime, missing text replacements | [Rules implementation guide](RULES_AUTHORING.md) and its dictionaries, including for Java-only fixes |
| Hullmod, system or weapon behavior | [Combat data bindings](#combat-data-bindings) -> class in `combat/hullmods/`, `combat/systems/`, `combat/weapons/` |
| Prototype versus Enigma presentation | `helper/ShipHelper.protOrEnigma()` / `isProtTech()` |
| Missile AI | `ModPlugin.pickMissileAI()` -> `combat/weapons/ai/` |
| Combat effects | `combat/plugins/`, GraphicsLib data in `data/lights` and `data/trails`, MagicLib `MagicRender` |


## Registration and lifecycle

| Hook | Owners |
|---|---|
| `ModPlugin.onApplicationLoad()` | GraphicsLib shader, texture and light data (skipped if GraphicsLib classes are missing); the `IS_*` optional-mod flags; Nexerelin `Nex_TransferMarket.NO_TRANSFER_FACTIONS` gains `enigma`; `settings/SettingsManager.load()` |
| `ModPlugin.pickMissileAI()` | `nskr_emglShot_sub` -> `EmpGrenadeAI`, `nskr_tremor1` -> `TremorAI`. No ship, weapon or drone AI picker is overridden. |
| `ModPlugin.onCodexDataGenerated()` | `combat/systems/PhaseCloakCodexLinks.link()` relates `nskr_bosscloak` and `nskr_poorcloak` to the phase hulls that use them as their defense |
| `ModPlugin.onGameLoad()` | Order below. Runs for new games and loaded saves. |
| `ModPlugin.beforeGameSave()` | `persistence/Saved.updatePersistentData()`, `CampaignTimer.save()`, then removes every `EFS_LIST` script and listener from the sector |
| `ModPlugin.afterGameSave()` | Re-adds `EFS_LIST`, then `persistence/Saved.loadPersistentData()` |
| `ModPlugin.onNewGame()` | Reserves procgen system names; Asteria in Arcadia (Corvus mode or no Nexerelin); Kesteven bounty participation and relations; writes `SAVE_KEY` and `STARFARER_MODE_FROM_START_KEY` |
| `ModPlugin.onNewGameAfterProcGen()` | Frost part 1, Outpost and, if Arcadia was missing, Asteria in a random system (Corvus mode or no Nexerelin); Mothership planets, Enigma bases, dormant spawns, environmental storytelling, Cache system |
| `ModPlugin.onNewGameAfterEconomyLoad()` | Frost part 2 market (Corvus mode or no Nexerelin), `DerelictTeaserSpawner.spawnRogues()` |
| `ModPlugin.onNewGameAfterTimePass()` | Frost, Outpost and Asteria in a random system in Nexerelin random-core games, IndEvo features, `SectorGen.genPeople()`, Frost ruins, `DesertConditionRepair.fix()`, blacksites, Mothership fleet, Enigma relations |

`onGameLoad` order: Nexerelin null-manager guard -> `createManagers()`: clears the `persistence/Saved` registry and `CampaignTimer` instances and builds new `EFS_LIST` instances -> `HellSpawnDisposableFleetSpawner` and `ThronesGiftDisposableFleetSpawner` behind `hasScript` -> `kesteven/KestevenBlueprints.borrowIndieBlueprints()` -> `kesteven/BlackOpsBlueprints.scanWeaponBlueprints()` -> `syncNSKRScripts()` -> `registerPlugin(new CorePlugin())` -> `addTokenReplacementGenerator(new quest/QuestTokens())` -> `EFS_LIST` as transient scripts, transient listeners and listener-manager listeners -> `persistence/Saved.loadPersistentData()` -> `kesteven/tips/KestevenTipBarEventCreator` -> `Difficulty.clearStarfarerFromStartUnlessStarfarer()` -> new-save generation -> `FleetHelper.hackBrokenVariants()` -> `quest/QuestManager.startQuests()` (creates missing quest states).

A save without `ModPlugin.SAVE_KEY` (`nskr_enabled`) in sector persistent data runs all four `onNewGame*` hooks from `onGameLoad`, then adds a Kesteven station commander to `nskr_asteria`. This is how the mod is added to an existing save.

Sectors without Arcadia (Nexerelin random-core games, or Arcadia removed) get Asteria from `Asteria.generateInRandomSystemIfMissing()`, called after `Outpost.generate()`. Look Asteria up by entity ID (`helper/SectorLookup.getAsteria()`), never through Arcadia. `SectorGen.genPeople()` places Michael, Jack and Alice on Asteria and Nicholas on the Outpost. If no system qualifies, Asteria is not generated, `helper/SectorLookup.asteriaOrOutpost()` returns the Outpost, and `kesteven/ExileManager.exile()` creates the missing quest people there.

Scripts use two lifecycles:

| Tier | Members | Lifecycle |
|---|---|---|
| Transient `EFS_LIST` | Built anew by every `onGameLoad`, for new games and loaded saves: `HyperspaceEnigmaSpawner`, `HintManager`, `RorqualSpawner`, `EternitySpawner`, `enigma/HeartOccupation`, `StalkerSpawner`, `enigma/EnigmaRelations`, `KestevenScavenger`, `kesteven/KestevenExportManager`, `GuardSpawner`, `AbyssSpawner`, `QuestStageManager`, `kesteven/ExileManager`, `LoanShark`, `BlackOpsManager`, `ContractManager`, `enigma/EnigmaHullmodListener`, `MothershipSpawner`, `BlacksiteManager`, `enigma/EnigmaAIConverter`, `GameModeManager`, `ThronesGiftManager`, `HellSpawnManager`, `quest/QuestManager`; `HellSpawnNexListener` only with Nexerelin | Each is a `BaseCampaignEventListener` and `EveryFrameScript`, advanced every campaign frame (`QuestManager` skips paused frames). The loop also adds each to the listener manager, so any listener interface the class implements is called, as for `QuestManager`'s `CurrentLocationChangedListener` and `ColonyDecivListener`. Removed before each save and re-added after it. Constructors may read the loaded sector (`CampaignTimer` reads its stored value only there); `persistence/Saved` fields are loaded after construction. Keep per-save state in instance fields, `Saved` or persistent data, not in static fields. |
| Saved scripts | `EnigmaFleetLoot`, with listeners `kesteven/loans/CrushingDebt`, `kesteven/LicensingFees` and `kesteven/CommissionedCrewsBonus` registered inside its guard; `BountyLoot`; generic plugin `EnigmaDefenderPlugin` | Added once by `syncNSKRScripts()` behind `hasScript`/`hasPlugin` checks and serialized with the save. The three economy listeners react to `reportEconomyTick`. |

`CorePlugin` is a transient `BaseCampaignPlugin`: vanilla drops transient plugins when saving, so `onGameLoad` registers it again. Its `pickInteractionDialogPlugin` routes entities claimed through `quest/QuestDialogs.CLAIM_KEY` to a rules dialog, and other quest and event entities to their Java dialogs by entity ID and memory state.

Other registrations: `kesteven/tips/KestevenTipBarEventCreator` bar event creator, guarded by `hasEventCreator`; `ElizaRaidObjectiveCreator`, added by `ElizaDialog` during that dialog. Combat listeners added to ships and entities last for one battle.

| Registry | Owner / consumer |
|---|---|
| `data/config/settings.json` | Rule command package `lostsector.dialogue.rules`; combat plugins `nskr_kaboomPlugin` (`PayloadDetonationPlugin`), `nskr_entrancePlugin` (`EntrancePlugin`), `nskr_tauntPlugin` (`CacheBossTauntPlugin`) in `combat/plugins/`, and `nskr_SplitterWeaponPlugin` (`combat/weapons/PlasmaCanisterSplitter`); sprite categories; design-type colours; `bonusXP` for story-point options |
| `data/config/LunaSettings.csv` | Every setting's label, type, default, range, options and tab in LunaLib's menu; read by `settings/SettingsManager` |
| `data/config/LunaSettingsConfig.json` | LunaLib menu icon |
| `data/campaign/rules.csv` | Dialogue; see [project routing](RULES.md#project-routing) |
| `data/campaign/person_missions.csv` | Mission `nskr_contracts` -> `ContractsMission`; the id is the prefix of its rules triggers `nskr_contracts_blurb` and `nskr_contracts_option` and of the offer tokens `$nskr_contracts_*` |
| `data/campaign/abilities.csv` | `nskr_hellSpawnAbility` -> `campaign/starts/hellspawn/HellSpawnAbility`, whose `ID` constant Java uses to grant it |
| `data/characters/skills/skill_data.csv` | `nskr_hellSpawnSkill` -> `campaign/starts/hellspawn/HellSpawnSkill`, `nskr_hellSpawnPeacefulSkill` -> `HellSpawnPeacefulSkill`; Java reads each class's `ID` constant. Each `.skill` file is named after its id, as in vanilla. |
| `data/campaign/market_conditions.csv` | `nskr_enigmaPop` -> `EnigmaPopCondition`, `nskr_upChip` -> `UnlimitedProductionChipCondition`, `nskr_hellSpawnCondition` -> `HellSpawnCondition` |
| `data/campaign/commodities.csv`, `special_items.csv` | `nskr_electronics`; prototype blueprint packages `nskr_prot_wp`, `nskr_prot_light`, `nskr_prot_heavy` |
| `data/campaign/procgen/*.csv`, `sim_opponents.csv` | Planet type `nskr_ice_desert`; salvage rows `nskr_enigmabase`, `nskr_heart_wreckage`, `nskr_blacksite_*`; drop groups; simulator opponents |
| `data/config/custom_entities.json` | Entity specs for Java `addCustomEntity` calls. A `pluginClass` must implement `CustomCampaignEntityPlugin`: `nskr_blast` -> `rendering/CampaignBlastSprite`, not the combat `BlastSprite`. |
| `data/config/sounds.json` | Music and sound IDs played from Java |
| `data/world/factions/factions.csv` | `enigma`, `kesteven`, `prot_ops`, `ai_all`; the other `.faction` files add to vanilla factions |
| `data/config/modSettings.json` | An empty MagicLib `bounty_board`; AdvancedGunneryControl weapon tags |
| `data/config/exerelin/`, `exerelinFactionConfig/` | Nexerelin backgrounds `HellSpawnBackground`/`ThronesGiftBackground` and faction configs |
| `data/config/{indEvo,BetterColonyConfig,CommissionBonus,ExiledSpace,prism,ruthlesssector,starship_legends}/` | Read only by those mods |
| `data/config/version/version_files.csv` | Registers `lostsector.version` with Version Checker |

Mod assets live under `graphics/lostsector/` and `sounds/lostsector/`; paths outside those folders refer to vanilla assets. IntelliJ compiles `jars/src` to `jars/production` and builds the `jars/Lost.Sector.jar` artifact. Build procedure: [CLAUDE.md](../CLAUDE.md#building).

### Optional integrations

`ModPlugin.onApplicationLoad()` sets each flag once from `isModEnabled`. None of these mods is a `mod_info.json` dependency. LunaLib is a required dependency; see [settings](#settings).

| Flag (mod ID) | Gated owners |
|---|---|
| `IS_NEXERELIN` (`nexerelin`) | World generation mode, `HellSpawnNexListener`, diplomacy calls in `QuestStageManager`, `HellSpawnManager`, `kesteven/ExileManager`, `SectorGen`. Also cleared in `onGameLoad` when `SectorManager.getManager()` is null. `HellSpawnBackground`/`ThronesGiftBackground` extend Nexerelin's `BaseCharacterBackground` and are reached only through Nexerelin's background CSV. |
| `IS_INDEVO` (`IndEvo`) | IndEvo features in `onNewGameAfterTimePass`, `Frost`, `Outpost`, `SectorGen`, `kesteven/ExileManager`, `enigma/HeartOccupation`, `ContractInfo`; `getIndEvoBoolean` |
| `IS_TAHLAN` (`tahlan`) | `ContractInfo` reward table |
| `IS_CC` (`timid_commissioned_hull_mods`) | `kesteven/CommissionedCrewsBonus` |
| `IS_IRONSHELL` (`timid_xiv`) | `EndingElizaDialog`, `QuestStageManager` |

Keep foreign classes behind these flags or behind the foreign mod's own loader. A class that references a foreign type must not be loaded when that mod is absent.

## Save identity

| Mechanism | Stored as | Rename hazard |
|---|---|---|
| `persistence/Saved<T>` | Sector persistent data under `persistence/Saved.PREFIX` (`nskr_`) plus the constructor key. `ModPlugin` writes all instances before save and reloads them on load and after save. | Changing a key loses that value. `HyperspaceEnigmaSpawner` and `BlackOpsManager` keys already contain `nskr_`, so their stored keys start `nskr_nskr_`. |
| `CampaignTimer` | The timer object itself, in sector persistent data under the owner's fully qualified class name plus `Timer` | Moving or renaming `GameModeManager` or `HellSpawnManager` silently starts a fresh timer; renaming `CampaignTimer` breaks loading. |
| Saved scripts and plugins | The objects listed under Saved scripts above | Their class names and fields are serialized. |
| `ModPlugin.SAVE_KEY` `nskr_enabled`, `STARFARER_MODE_FROM_START_KEY` `nskr_starfarerFromStart` | Sector persistent data | Renaming `SAVE_KEY` reruns world generation on every existing save. |
| `Frost.NAME_KEY` `$nskr_frostName` | Sector persistent data, not memory, despite the `$` | Holds the generated Frost system name. |
| LunaLib `LunaSettings/lost.sector.json` | Per installation, outside saves, keyed by CSV `fieldID`; shared by all campaigns on that machine, including the unlocks from `QuestHelper.saveEnding()` | A `Setting` constant's name is its `fieldID`; renaming either drops the stored value. |

Stable IDs are in `helper/Ids`: factions `kesteven`, `enigma`, `prot_ops`, `ai_all`; people `nskr_opguy`, `nskr_researcher`, `nskr_intelligence`, `nskr_anarchist`, `nskr_president`, `nskr_enigmaAdmin`, `nskr_thrn`; entities and markets `nskr_heart`, `nskr_asteria`, `nskr_outpost`, `nskr_enigmabase`, `nskr_blacksite`, `nskr_anomalous_station`. `addMarketplace` uses the primary entity's ID as the market ID.

World generation is static calls from the `ModPlugin` hooks; `world/SectorGen.generate()` is empty.

| Owner | Creates / stable IDs |
|---|---|
| `world/systems/asteria/Asteria` | Asteria (`nskr_asteria`, `nskr_asteria_station`): in vanilla Arcadia, otherwise in a random procgen system near the core that has no market, Enigma base or Enigma fleet, preferring white dwarfs. That system becomes a core system and gets `nskr_asteria_relay` unless it has a comm relay, which then turns Kesteven. |
| `world/systems/frost/Frost` | New system with a random name from `SYS_NAME_LIST`: planets `nskr_bleak`, `nskr_glacier`, `nskr_siberia`, `nskr_shiver`, `nskr_algor`; `nskr_heart`; `nskr_frost_gate`, `nskr_frost_relay`; debris and derelicts |
| `world/systems/cache/Cache` | System `Unknown Site`: gate `nskr_cacheGate`, `nskr_cache_derelict1`-`4`, `nskr_cache_core` |
| `world/systems/outpost/Outpost` | In a random system near the core: `nskr_outpost`, `nskr_outpost_gate`, `nskr_outpost_relay` |
| `world/SectorGen` | People at fixed markets through `genPeople()`; `genEliza()` (`nskr_anarchist`) is called from `nskr_kestevenQuest` and `ElizaDialog` |
| `world/DesertConditionRepair` | Planet-condition fix after time pass |

Per-fleet and per-entity memory keys such as `$mothershipLoot`, `$StalkerFleet` and `$BetrayalFleet` are unprefixed. They belong to that fleet or entity's memory; namespace new keys with `nskr_`. Sector-scope keys use `$nskr_`. `$nex_uninvadable` and `$nex_do_not_colonize` are Nexerelin's keys, set on Lost.Sector systems.

## Source owners

Packages group code by feature. Use `rg --files jars/src/lostsector/<package>` for the complete inventory; the entries below identify state and integration owners, not every class.

| Package | Contents |
|---|---|
| `lostsector` | `ModPlugin` |
| `settings` | LunaLib-backed settings: `Setting`, `SettingsManager`, `Difficulty` |
| `persistence` | `Saved`, `CampaignTimer` |
| `quest` | Quest framework: definitions, saved quest state, `QuestManager` (stage changes, daily tick, frame hook, event routing to modules and quest fleet orders), quest fleets (`QuestFleets`, one `FleetHelper` list), dialog claims, the `nskr_quest` verbs (`QuestVerbs`), quest tokens (`QuestTokens`), generated people (`QuestPeople`), receipts (`QuestRewards`) the dev menu's reads and changes (`QuestDevTools`); shared modules in `quest/modules` (`InterceptEncounter`); specification and status in [`quest/README.md`](../jars/src/lostsector/quest/README.md) |
| `helper`, `helper/fleet` | Shared helpers; fleet, captain and system builders |
| `rendering` | Render helpers and blast sprites |
| `dialogue/rules` | Rule commands (`nskr_*`); `nskr_quest` hands its verbs to `quest/QuestVerbs`; `nskr_questDev`, the dev-mode quest menu, uses `quest/QuestDevTools` |
| `campaign` | `CorePlugin`; feature packages below |
| `campaign/enigma` | Enigma fleets, bases, relations, officers, loot, the Heart occupation and Frost intel, `EnigmaPopCondition` |
| `campaign/kesteven` | Kesteven economy, exports, blueprints, black ops, exile; `loans/`, `contracts/`, bar tips in `tips/`, and the questline in `quest/` |
| `campaign/bounties` | `BountyLoot`; one package per named bounty: `abyss/`, `eternity/`, `mothership/`, `peacekeepers/` |
| `campaign/events` | Intercept fleets, quest `ic` (`intercepts/`), hints (`hints/`), blacksites (`blacksite/`), environmental storytelling, derelict teasers |
| `campaign/starts` | `GameModeManager`; the `hellspawn/` and `thronesgift/` custom starts |
| `combat` | `hullmods/`, `systems/` (+ `ai/`), `weapons/` (+ `ai/`), `plugins/` |
| `world` | `SectorGen`, `DesertConditionRepair`, `systems/*` |
| `missions` | Custom battle generation |

### `campaign`

| File | Owner / connection |
|---|---|
| `CorePlugin` | Transient campaign plugin; `pickInteractionDialogPlugin` routes quest, blacksite, Mothership and custom-start entities to Java dialogs |
| `kesteven/ExileManager` | Kesteven exile from Asteria; moves quest people between Asteria and Outpost; its exile flag is read by `helper/SectorLookup.asteriaOrOutpost()` |
| `kesteven/KestevenExportManager`, `kesteven/LicensingFees` | Kesteven export sets and monthly licensing fees |
| `kesteven/loans/CrushingDebt` | Monthly debt interest for `dialogue/rules/nskr_debt` |
| `kesteven/CommissionedCrewsBonus` | Monthly crew stipend with Commissioned Crews |
| `kesteven/KestevenBlueprints`, `kesteven/BlackOpsBlueprints` | Static blueprint setup called from `onGameLoad` |
| `enigma/HeartOccupation` | While Enigma owns `nskr_heart`, removes every comm-directory entry except `nskr_enigmaAdmin`, every frame including while paused; adds `enigma/FrostIntel` on the first visit to Frost |
| `enigma/EnigmaHullmodListener` | Enigma tip unlock counts; logic in static `update()` |
| `enigma/EnigmaRelations`, `enigma/EnigmaAIConverter` | Enigma relationship clamp; AI-core officers on spawned Enigma fleets |

### Fleets and one-time placement

| File | Owner / connection |
|---|---|
| `enigma/HyperspaceEnigmaSpawner`, `enigma/StalkerSpawner`, `enigma/GuardSpawner`, `kesteven/KestevenScavenger`, `kesteven/BlackOpsManager` | Roaming and guard fleets. Enigma spawners skip HellSpawn mode; `BlackOpsManager` spawns from the market with `nskr_upChip`. |
| `bounties/*/*Spawner` | One named bounty fleet each, spawned once per game; loot flag `LOOT_KEY`; `MothershipSpawner` also places the planets and `MothershipInteractionBlocker` |
| `events/intercepts/InterceptsQuest`, `InterceptsStage`, `InterceptsState`, `InterceptsFleets` | Record quest `ic` in `quest/QuestCatalog`: three `quest/modules/InterceptEncounter` instances for the ARO strike group, the "LZ" messenger and the Auto-Hunter fleet; fleets through `QuestFleets`; the messenger asks `KestevenQuest.inMessengerWindow()` and reports `reportMessengerMet()`. [Intercept fleets](quests/CONTRACTS_AND_BOUNTIES.md#intercept-fleets) |
| `kesteven/loans/LoanShark` | Debt collectors |
| `events/blacksite/BlacksiteManager`, `BlacksiteDialog`, `BlacksiteInfo`, `BlacksiteSpawner` | Blacksite placement, defenders and self-destruct timer; saved `BlacksiteInfo` records |
| `enigma/EnigmaBaseSpawner`, `enigma/DormantSpawner`, `events/DerelictTeaserSpawner`, `events/EnvironmentalStorytelling` | One-time placement: Enigma bases, dormant fleets, teaser derelicts (including the Rorqual), storytelling derelicts; `enigma/EnigmaDefenderPlugin` supplies salvage defenders. `DormantSpawner.addDormant()` places one dormant fleet of any faction and is also used by the questline, Frost, the Cache and environmental storytelling. |
| `bounties/BountyLoot`, `enigma/EnigmaFleetLoot` | Encounter loot listeners for the bounties and for Enigma casualties |

### Kesteven questline, contracts and hints

| File | Owner / connection |
|---|---|
| `kesteven/quest/KestevenQuest`, `KestevenStage`, `KestevenFlag`, `KestevenState` | Quest `kq` in `quest/QuestCatalog`, with no modules yet. `KestevenState` holds every saved questline value; `QuestManager` is the only stage writer. Features outside the questline (`CorePlugin`, `ContractManager`, `ExileManager`, `StalkerSpawner`, `events/intercepts/InterceptsQuest`, `world/systems/cache/Cache`, `nskr_debt`, `nskr_shipSwap`, `nskr_modRemoval`, `nskr_barEventFixer`, the `nskr_isKStage` family) use only `KestevenQuest`'s public static [queries and actions](quests/KESTEVEN_STATE.md#queries-for-other-features). [State map](quests/KESTEVEN_STATE.md) |
| `kesteven/quest/QuestStageManager` | Kesteven questline automatic transitions, quest fleets, failure to stage 99, on `KestevenState`. Runs while paused; the state exists from the end of `onGameLoad`. |
| `kesteven/quest/QuestHelper` | Wrappers over `KestevenState` for the questline's own old classes and dialog commands (`getStage`/`setStage` with legacy ints, `getCompleted(KestevenFlag)`), target pickers, artifact spawning, `saveEnding()`. `getCompleted(String)`/`setCompleted(boolean, String)` are sector persistent-data flags that bounties, the Mothership and HellSpawn still use. |
| `kesteven/quest/KestevenPeople` | Quest person lookups: Jack, Alice, Nicholas, Michael and Eliza; the first four return null while their current market is missing |
| `kesteven/quest/KestevenFleets`; `helper/fleet/SimpleFleet`, `SimpleFleetMember`, `SimpleCaptain`, `SystemPicker`, `FleetInfo` | Quest fleet spawns; fleet and system builders shared by all spawners |
| `kesteven/quest/*Dialog` | Java `InteractionDialogPlugin`s opened by `CorePlugin`; `CacheDoubtDialog` is opened by `QuestStageManager` |
| `kesteven/quest/*BarEvent` | Questline bar events. `QuestStageManager` adds `HostileTakeoverBarEvent` and the Eliza search bar events to `PortsideBarData`; `dialogue/rules/nskr_barEventFixer` adds `DelveMeetingBarEvent` on each visit. |
| `kesteven/quest/*Intel` | Job intel: `EnemyUnknownIntel`, `HostileTakeoverIntel`, `OperationLifesaverIntel`, `TheDelveIntel`, `CacheIntel` |
| `kesteven/tips/KestevenTipBarEventCreator`, `KestevenTipBarEvent`, `KestevenTipIntel` | Bar event at Kesteven markets that sells the location of an unvisited system with a warning beacon, a derelict survey ship or a derelict mothership; the creator makes `KestevenTipBarEvent`, which adds `KestevenTipIntel` when the player pays. Not part of the questline. |
| `kesteven/quest/ElizaRaid`, `ElizaRaidObjectiveCreator` | Ground-raid objective for Eliza's data disks |
| `kesteven/quest/UnlimitedProductionChipCondition` | Market condition of the production chip |
| `kesteven/contracts/*` | `ContractsMission`, `ContractInfo`, `ContractIntel`, `ContractManager` (failure checks; offer reset when its counter reaches 600 seconds, about 60 days) |
| `events/hints/HintManager`, `HintIntel` | System hints for bounties and Frost |

Intel classes add themselves as scripts and put their per-frame logic in `advanceImpl()`. Vanilla `BaseIntelPlugin.advance()` runs the `endAfterDelay()` countdown, stops calling `advanceImpl()` once the intel is ending and reports the script done once it has ended; overriding `advance()` disables all of that.

### Custom starts

| File | Owner / connection |
|---|---|
| `starts/GameModeManager` | Active mode `DEFAULT`, `THRONESGIFT` or `HELLSPAWN`; checked by most spawners |
| `starts/hellspawn/HellSpawnManager`, `HellSpawnNexListener`, `HellSpawnEventIntel` | HellSpawn corruption points, stat hullmod level, judgement timer and warning |
| `starts/hellspawn/HellSpawnJudgement*`, `HellSpawnAbility*` | Judgement encounter and ability; the `*Interaction` classes extend `FleetInteractionDialogPluginImpl` and are picked by `CorePlugin`. Gate Conduit swarms (`HellSpawnAbility.HELL_FLEET_KEY`) are made non-hostile to the player faction at spawn and join a player battle only through `HellSpawnAbilityInteraction.pullInNearbyFleets`, never against the player or Enigma. `HellSpawnManager` reissues their orders outside battles. |
| `starts/hellspawn/HellSpawnCondition` | Market condition |
| `starts/thronesgift/ThronesGiftManager`, `ThronesGiftIntel` | XP-to-automation points, the ship list and the automation itself (`getAutomatableShips`, `getAutomationCost`, `automate`). The intel's button opens a rules dialog on `nskr_thronesGiftPick` with a null target; `dialogue/rules/nskr_thronesGift` shows the ship picker and runs the automation; see [project routing](RULES.md#project-routing). |
| `starts/*/*DisposableFleetSpawner` | Vanilla `DisposableFleetManager` subclasses, added in `onGameLoad` behind `hasScript` and saved with the game |
| `starts/*/*Background` | Nexerelin backgrounds; each reads its unlock setting (`THRONES_GIFT_UNLOCKED`, `HELLSPAWN_UNLOCKED`) whenever Nexerelin asks |

Polling: every `EFS_LIST` manager advances each frame. Most gate their work with a `persistence/Saved<Float>` counter and return while paused. Counters add the frame `amount` in seconds, and vanilla `SECONDS_PER_GAME_DAY` is 10, so a threshold of `10f` is one campaign day. Many managers add `2 * amount` while the campaign is in fast advance. `QuestStageManager`, `enigma/HeartOccupation`, `HellSpawnManager` and `ThronesGiftManager` also do work while paused; `CampaignTimer.advance()` does not count paused time. `ThronesGiftManager` compares the player's XP each frame, including while paused, because XP gain has no callback: `CharacterStats.addXP` changes the stored XP and levels up without notifying any listener, and XP is often granted in dialogs, which pause the campaign.

### Combat data bindings

| Data | Java convention |
|---|---|
| `data/hullmods/hull_mods.csv` `script` | `lostsector.combat.hullmods.<Hullmod>` |
| `data/shipsystems/*.system` `statsScript` / `aiScript` | `lostsector.combat.systems.<X>Stats` / `lostsector.combat.systems.ai.<X>AI` |
| `data/weapons/*.wpn`, `data/weapons/proj/*.proj` `everyFrameEffect` / `onFireEffect` / `onHitEffect` | `lostsector.combat.weapons.<Weapon>Effect`, `<Weapon>OnFireEffect`, `<Weapon>OnHitEffect` |
| `data/config/settings.json` `plugins` | Global combat plugins in `combat/plugins/`, and `combat/weapons/PlasmaCanisterSplitter` |
| `data/missions/mission_list.csv` | `nskr_test` source is `data/missions/nskr_test/MissionDefinition.java`, compiled by the game at runtime; `nskr_test_custom` is `jars/src/data/missions/nskr_test_custom/MissionDefinition.java`, extending `missions/BaseRandomBattle` |

Exceptions: `nskr_poorcloak` uses vanilla `PhaseCloakStats`; `nskr_animebad` and `nskr_bosscloak` have no `aiScript`; `nskr_emflak.system` is a WEAPON-type system bound through its weapon (`EmFlakEffect`, `EmFlakOnFireEffect`); `nskr_boostdrive` and `nskr_powersurge` share `ai.EngineBoostAI`; `nskr_pullback` is both a hidden hullmod (`PullbackDummy`, trail points) and the Dragontail's system (`PullbackStats`/`PullbackAI`). `nskr_mothership_frigate`, `nskr_hellSpawnStats` and `nskr_holySpirit` are never built in; campaign code adds them at runtime (`Mothership`, `HellSpawnManager`, `ThronesGiftManager`).

Codex: vanilla `CodexDataV2.populateShipSystems` gives every ship system entry `codex_require_related`, so a system is visible and unlocked when a related hull entry is. Do not tag ship systems `codex_unlockable`: that locks them until `SharedUnlockData.reportPlayerAwareOfShipSystem`, which nothing calls. Vanilla never relates a phase hull to its defense system; `PhaseCloakCodexLinks` adds that link for the mod's two cloaks.

### Ship families

| Family | Hulls | Owners |
|---|---|---|
| Kesteven | `nskr_prosperity`, `nskr_nighthawk`, `nskr_devilcatcher`, `nskr_blackbird`, `nskr_mercenary`, `nskr_dragontail`, `nskr_kingstork` | One system each; `TakedownDummy` (Blackbird), `PullbackDummy` (Dragontail). Kesteven hullmods: `InertialSupercharger`, `VolatileFluxInjector`, `HighCapacitanceBanks`, `CriticalPointProtection`, `KestevenConnections`, `HeavyWeaponsIntegration`, `HeavyMissileSpec`, `HighEnergyWeaponSystems`, `MicroweightMagazines` |
| Unknown Prototype / Project Enigma pairs | `nskr_minokawa`, `nskr_sovereign`, `nskr_nemesis`, `nskr_muninn`, `nskr_warfare`, `nskr_eternity`, `nskr_epoch`, `nskr_epochx`, `nskr_widow`, `nskr_torpor`, each with an `_e` Enigma twin sharing its system | Built-in `nskr_focused_shield` (`AdvancedShieldProjector`). `ShipHelper.protOrEnigma()` reads `nskr_lost_prot` / `nskr_domain_era`; `ShipHelper.isProtTech()` reads `nskr_focused_shield` / `nskr_kaboom`. Hull-specific: `AdaptiveProtocol` + `nskr_protocolsystem` (Nemesis), `CausalityCore` + `nskr_causality` (Eternity), `Stasis*` + `nskr_stasisp` + `combat/weapons/TorporLightsEffect` (Torpor; `StasisEffect` fires on `Setting.STASIS_FIRE_KEY`), `TeleportDummy` + `WarpStats` (Sovereign) |
| Drones and wings | `nskr_huginn`/`_e` wings; `nskr_aed` (Rupture, Project Enigma) | Huginn arm weapons (`HuginnArmOnHitEffect`); `AntimatterPayload` + `PayloadStats` + `combat/plugins/PayloadDetonationPlugin` |
| Other hulls | High Tech `nskr_borealis` (`MassTargeting*`), `nskr_malediction`, `nskr_pursuer` (also `nskr_pursuer_wing`), `nskr_reverie` (`MissileSalvo*`); Midline `nskr_verity`, `nskr_stalwart` (`nskr_emflak`); Pirate `nskr_kingslayer`, `nskr_rhea`; Rogue Co. `nskr_rorqual` | One system each |
| Bosses | `nskr_reverie_boss`, `nskr_harbinger_boss`, `nskr_afflictor_boss` | Shared `VoidCore` and `nskr_bosscloak` (`AbyssalPhaseCloakStats`); main systems `nskr_bfpulse`, `nskr_animebad`, vanilla `acausaldisruptor` |
| Remnant Sunburst | `nskr_sunburst` | `Mothership`/`MothershipFrigate`, `nskr_harmonics` (`Harmonics*`) |

Prototype weapons (`prot_wp` tag, Unknown Prototype manufacturer) have one `*Effect`/`*OnHitEffect` pair each in `combat/weapons/`. Decorative `nskr_prot*_light` weapons use `combat/weapons/PrototypeGlowEffect`.

### `combat/plugins` and rendering

| File | Owner / connection |
|---|---|
| `EntrancePlugin` | Travel-drive arrival effects for prototype/Enigma ships; GraphicsLib light and distortion; custom data `ENTRANCE_DATA_KEY` + ship ID |
| `PayloadDetonationPlugin` | Rupture post-activation effects from `AntimatterPayload`'s custom data `KABOOM_DATA_KEY` + ship ID |
| `combat/weapons/PlasmaCanisterSplitter` | Scans all projectiles for `nskr_pbcc_shot` and spawns `nskr_pbcc_sub`; custom data `nskr_SplitterWeapon` |
| `CacheBossTauntPlugin` | Cache boss fleet (`Cache.CACHE_FLEET_KEY`): taunt messages, boss music, secondary boss spawn; custom data `TAUNT_DATA_KEY` + fleet ID |

`VoidCore.IndicatorRenderer` and `CausalityCore.IndicatorRenderer` are the only `CombatLayeredRenderingPlugin`s. Other temporary sprites use MagicLib `MagicRender`, mostly through `rendering/BlastSprite` and `rendering/RenderHelper`. GraphicsLib data: `data/lights/nskr_light.csv`, `nskr_bump.csv` and `data/trails/trail_data.csv`, loaded in `ModPlugin.onApplicationLoad()`.

### `helper`

| File | Owner / connection |
|---|---|
| `Ids` | Stable faction, person, entity and hullmod IDs; mod ID `lost.sector` |
| `SectorLookup` | Fixed places (`getFrost`, `getAsteria`, `getOutpost`), the Kesteven home market (`asteriaOrOutpost`: the Outpost while exiled or without Asteria) and existence checks (`enigmaExists`, `kestevenExists`, `asteriaExists`) |
| `SystemHelper` | Random system, market, faction market (`getRandomFactionMarket`) and in-system location picks; `spawnAwayFromStarFixer` moves a placed entity or fleet out of the nearest planet or star; gate, relay and neutron-star checks; nearest system; entity swaps |
| `ShipHelper` | Prototype/Enigma identity (`isProtTech`, `protOrEnigma`), logistics and D-mod checks, officer skills, hull-size multiplier (`getLinearMod`) |
| `CombatHelper` | Range queries pinned to LazyLib 2.4b behavior; area damage including station modules |
| `MathHelper` | Easing, noise, seeded random ranges (modified from LazyLib); sector seed (`getSeedParsed`) |
| `FleetHelper` | Fleet generation and assignment AI helpers; `FleetInfo` lists in sector memory (`getFleets`/`setFleets`), one per key in `FLEET_ARRAY_KEYS`, including `quest/QuestFleets.KEY` for every quest fleet; `getOriginalFlagship()` for fleets built by `helper/fleet/SimpleFleet` (vanilla `getFlagship()` returns another member once the flagship is lost); `hackBrokenVariants()` on load, which repairs that original flagship and the secondary members `SimpleFleet` recorded; `guardTargetAI` creates its guard point at the fleet's position in the fleet's own location; `goToTargetAndDespawnAI`, `isBeaten` and `despawnOutOfSight` serve the leave and withdrawal options of `quest/FleetOrders` |
| `PowerLevel` | Player fleet strength for encounter scaling |
| `rendering/BlastSprite`, `rendering/CampaignBlastSprite` | Timed blast sprites in combat and campaign (`HellSpawnAbility`) |
| `rendering/ColorHelper` | Colour utilities and the tooltip colours `TT_ORANGE`, `BON_GREEN`, `NICE_YELLOW` |
| `StringHelper` | Token substitution helpers adapted from Nexerelin; Greek-letter names |
| `UiSounds` | UI sound shortcuts |
| `Music` | Music checks by `data/config/sounds.json` music set id: `isPlaying()`, `stopIfPlaying()`. Java names music sets, never music files. |

### Settings

Every setting is a row in `data/config/LunaSettings.csv`, which owns its label, type, default, range, option text and tab. Java holds no copy of those. To add a setting, add the row and a `Setting` constant.

| File | Owner / connection |
|---|---|
| `settings/Setting` | One constant per data row that code reads; the row's `fieldID` is the constant name in lowerCamel case. Typed reads `getBoolean()`, `getFloat()`/`getDouble()`, `getInt()`, `getKeycode()` and `getOption(Class)`, which returns the enum constant whose ordinal is the selected option's position in the row's `secondaryValue` list. |
| `settings/SettingsManager` | `load()`, from `ModPlugin.onApplicationLoad()`: reads this mod's CSV with `loadCSV(path, Ids.LOST_SECTOR_MOD_ID)`, as LunaLib does (`getMergedSpreadsheetDataForMod` would merge every mod's LunaSettings.csv). Throws when a constant has no row, a different `fieldType`, or a Radio option count that differs from its enum; logs a warning for data rows without a constant. Caches CSV defaults overlaid with LunaLib's stored values, numbers clamped to `minValue`/`maxValue`, unknown Radio labels replaced by the default. Its `Listener` reloads the cache when LunaLib's menu saves this mod. `set()`, used by `QuestHelper.saveEnding()` for the unlock settings, writes a value to LunaLib's `LunaSettings/lost.sector.json` and to `LunaSettingsLoader.getSettings()`; LunaLib has no public setter and writes its in-memory copy back when its menu is saved. |
| `settings/Difficulty` | Radio options in CSV order: `NORMAL` uses the two fleet scaling settings, `EASY` and `STARFARER` fixed multipliers. `clearStarfarerFromStartUnlessStarfarer()` clears `ModPlugin.STARFARER_MODE_FROM_START_KEY` on each load and when the menu leaves Starfarer during a campaign. |


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
- `SoundPlayerAPI.getCurrentMusicId()` returns the playing file with its path, not the music set id passed to `playCustomMusic`. Compare through `helper/Music`, which resolves a set's files from the merged `sounds.json`.
- `playUISound` expects stereo; positional `playSound` requires mono; loops should be mono.
- GraphicsLib's combat-engine storage and viewport helpers are unavailable in the campaign layer.

### Shared cross-file constraints

- Custom entity `init()` calls `super.init()`; do not shadow the inherited entity field.

## Live hazards

Quest defects are listed with their quest: [Kesteven questline](quests/KESTEVEN_QUESTLINE.md#defects-found-by-reading-the-source) and the [fleet conversations](quests/KESTEVEN_DIALOGUE.md#fleet-conversations).

| Location | Hazard |
|---|---|
| `ModPlugin.onGameLoad()` | `IS_NEXERELIN` is cleared for the rest of the session if `SectorManager.getManager()` is null on any load. |
| `data/config/modSettings.json` | `MagicLib.bounty_board` is empty and only `modFiles/magicBounty_data_example.json` exists, so no MagicLib bounty is registered. |
| Vanilla asset paths not referenced by vanilla data | `graphics/portraits/portrait_ai2.png` (`settings.json` `nskr_mother`, `MothershipSpawner.PORTRAIT_SPRITE`), `graphics/icons/markets/plundered.png` (`market_conditions.csv` `nskr_hellSpawnCondition`, Nexerelin `character_backgrounds.csv`) and the skill icon `combat_endurance.png` (`skill_data.csv` `nskr_hellSpawnPeacefulSkill`; vanilla data uses `combat_endurance3.png`). Unverified: check them against a game install. |

## Dead or dormant

| Component | State |
|---|---|
| `GameModeManager` timer branch | Empty. Its `CampaignTimer` timeout is `1f` seconds, like the other `CampaignTimer` owners. |
