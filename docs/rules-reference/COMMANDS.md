# Vanilla command dictionary

Starsector **0.98a-RC8**. Read [the authoring guide](../RULES_AUTHORING.md) for lifetimes, token preparation, registration and source conflicts. The recipes cover common uses; the index supplies an exact source file and real call site for deeper work.

## Common recipes

Angle brackets mark required arguments; brackets mark optional ones. These go in Script, or Conditions when a Boolean query is intended. Not every command is safe as a condition.

| Command / syntax | Use and limits |
|---|---|
| `AddText "text" [color]` / `AddTextSmall "text" [color]` | Append a paragraph with token replacement. Small uses the small text style. Prepare values before display. |
| `Highlight "phrase" ...` / `SetTextHighlights "phrase" ...` | Highlight matching occurrences in the last paragraph, in order. Repeat arguments for repeated occurrences. Does not print another paragraph; no trailing color argument. |
| `SetTextHighlightColors <color> ...` | Corresponding last-paragraph highlight colors. Supply at least one. |
| `SetTooltip <optionId> "text"` | Token-expanded hover text on an existing option. |
| `SetTooltipHighlights <optionId> "phrase" ...` | Highlight tooltip phrases, not the option label. |
| `SetTooltipHighlightColors <optionId> <color> ...` | Colors corresponding to tooltip highlights. |
| `SetOptionColor <optionId> <color>` | Color an existing option using its exact ID. |
| `SetEnabled <optionId> <boolean>` | Enable/disable an option. Add a tooltip to explain a restriction. This and the other option decorators do nothing when the option does not exist yet. |
| `SetOptionText <optionId> "text"` | Replace the option label. This implementation reads raw text, not token-expanded prose. |
| `SetShortcut <optionId> ESCAPE [putLast]` | Bind a recognized keyboard name without KEY_. putLast defaults true. Option ids starting with `cutCommLink` or `defaultLeave` get Escape automatically, except `cutCommLink2` and `cutCommLinkNoText2`. |
| `AddOption <order> <id> "text"` | Script option-adding command; ordinary String arguments, not full text replacement. Prefer the Options column for ordinary narrative options. |
| `RemoveOption <optionId>` | Remove an option; does not navigate or end the conversation. |
| `SetColor $key <color>` | Store a Color in local memory without expiry. Uses the raw key, not scope resolution. Add an expiry for dialogue-only values. Colors are command inputs, not display strings. |
| `FireBest <trigger> [keepOptions]` | Apply one best matching rule; false when nothing matches. keepOptions (literal or variable) defaults false; an empty new option set does not itself clear the old menu. The trigger may be a `$variable`. |
| `FireAll <trigger>` | Apply all matching rules and combine options. This build ignores a keepOptions argument. First sets `$fireAllTrigger` and fires `FireBest FireAllIntercept`; a matching intercept row runs instead of the requested trigger. See [FireAll and FireBest](../RULES.md#fireall-and-firebest). |
| `Call $reference <action> [args...]` | Invoke a stored CallableEvent. Alias CallEvent; not arbitrary reflection or a rule-ID jump. |
| `unset $key` | Remove a key now, resolving its scope. |
| `unsetAll $prefix` | Remove keys by prefix in one scope. Use only a prefix you own; persistent state can be deleted too. |
| `expire $key <literalDays>` | Change a timer without replacing its value. This implementation parses raw duration text; supply a literal, not a memory variable. |
| `UpdateMemory` | Rebuild the active RuleBasedDialog memory map and refresh facts. Not a generic reset/clear command. |
| `AddCredits <amount>` | Change player credits, refresh credit facts and print the vanilla receipt. Do not grant again in Java. Validate affordability before charging. |
| `AddRemoveCommodity <id> <quantity> [withText]` | Positive adds, negative removes. Also supports credits. Defaults to a receipt when the absolute quantity is at least one. |
| `AddRemoveAnyItem <type> <id> [specialData] <quantity>` | RESOURCES, WEAPONS, FIGHTER_CHIP or SPECIAL with receipt. Example: `AddRemoveAnyItem SPECIAL ship_bp paragon 1`. |
| `PlayerHasCargo <id> [quantity]` | Condition; quantity defaults one. Checks commodities/weapons/fighters and special items with null data. Cannot validate a particular blueprint's data. |
| `CheckSetting <settingId>` | Condition reading an existing Boolean setting. |
| `RollProbability <probability>` | Seeded condition tied to target/rule/month inputs, not a fresh quest-target generator or saved random choice. |
| `BeginConversation <personRefOrId> [minimal] [showRelationship]` | Select the active person, refresh person memory and show their card. Resolves important people, market people, comm-directory entries or POST: IDs. Requires a rule-based dialog. |
| `ShowPersonVisual [minimal] [importantPersonId]` | Show a person card; defaults to active person/fleet commander. Does not switch active person or memory ownership. An id not registered with the important people throws. |
| `ShowSecondPerson <importantPersonId>` / `ShowThirdPerson <importantPersonId>` | Add a second or third portrait; ignored when the id is not an important person. `HideSecondPerson` / `HideThirdPerson` remove them. |
| `ShowImageVisual <key>` / `ShowImageVisual <category> <key>` | Settings sprite at its own size, default category illustrations. Not a raw arbitrary image path. `ShowPic <key>` shows an illustration at a fixed 640x400. |
| `ShowMapMarker <entityOrMarketId> [title] [text]` / `HideMapMarker` | Map beside the person card, and its removal. |
| `ShowDefaultVisual` | Restore the target's default custom/planet/fleet visual, not necessarily the bar scene. |
| `SaveCurrentVisual` / `RestoreSavedVisual` | Existing visual save/restore commands. Check their saved slot before nesting custom visuals. |
| `MakeOptionOpenCore <optionId> <tabId> <tradeMode> [onlyTargetTab]` | Wire an option to core UI. Supply tradeMode: this implementation accesses argument three when a tab is supplied. |
| `AddBarEvent <optionId> "option" "blurb" [color]` | Temporary market bar event with token expansion. No market means no event. Keep vanilla bar routing and recognizable labels. |
| `HailPlayer` | Fleet hail text and temporary $hailing flag. Does not spawn or assign a chasing fleet. |
| `HighlightComms` | Fleet-only temporary comm highlight; does not select a speaker or open comms. |
| `MakeOtherFleetGoAway [clearAssignments]` | Fleet-only standard return-to-source assignments. Use true to abandon current assignments. |
| `MakeOtherFleetAllowDisengage <reason> <boolean> [days]` | Reason-owned permission to disengage. Does not send the other fleet home. |
| `MakeOtherFleetLowRepImpact <reason> <boolean> [days]` | Reduced reputation impact, not zero. |
| `MakeOtherFleetNoRepImpact <reason> <boolean> [days]` | No reputation impact. Prefer reason-aware commands over overwriting another system's flag. |
| `EndConversation [DO_NOT_FIRE or NO_CONTINUE]` | Clear the active person and rebuild the base menu: fleet reinit, or `MarketPostOpen`/`PopulateOptions` for a market or person dialog. `DO_NOT_FIRE` skips the rebuild; `NO_CONTINUE` shows the default visual first and skips a fleet's Continue step. Outside a rules-based dialog it only resets the fleet conversation flag (and `NO_CONTINUE` still shows the default visual). Not a fleet-encounter dismissal. See [Fleet and bar exits](../RULES.md#fleet-and-bar-exits). |
| `DismissDialog` | Dismiss the window. For fleet encounters, see [fleet and bar exits](../RULES.md#fleet-and-bar-exits) for BattleAPI cleanup first. |
| `PlaySound <soundId>` | Registered UI sound at default pitch/volume; source expects stereo. |
| `PlayCustomMusic <musicId>` / `ResumeNormalMusic` | Replace the campaign music; the second restores it and must follow. |
| `SetStoryOption <optionId> <points> <bonusXPKey> [sound] ["log text"]` | Story point option: colors it, adds the cost to the label, adds the confirmation and disables it when the player cannot pay. The key is a `bonusXP` entry in `settings.json`; sounds `leadership`, `combat`, `industry`, `technology` map to the story point chimes. With exactly three arguments it reads `<optionId> <sound> <log text>`, charges one point and uses the option id as the bonus XP key; pass at least four. |
| `DumpMemory` | Debug the current memory map. Does not list generator-only replacements or supply missing state. |

For `AddShip`, `AdjustRep*`, `MarketCMD`, `BarCMD`, `MissionHubCMD` and other multi-action commands, read the source and vanilla row below. Their subcommands and state requirements are not interchangeable. A listed example is a call site, not a complete recipe for that subsystem.

Colors follow the command's Token.getColor path: a Color object in memory; an `r,g,b,a` String; `highlight`/`h` (buttonShortcut); `good`/`bad` (textFriendColor/textEnemyColor); `story`; `gray`/`grey`; a faction ID (base UI color); or an actual settings color ID. Do not invent IDs or assume every String parameter expands tokens.

## Alphabetical class and call-site index

The index includes abstract/base and example classes as well as executable commands. Paths are relative to `com/fs/starfarer/api/impl/campaign/rulecmd/`. Registered vanilla packages are the root plus `salvage`, `newgame`, `missions` and `academy`, so subpackage classes are normally invoked by their simple names.

S/C counts are invocation lines in Script/Conditions, not substring counts. Examples come from vanilla `data/campaign/rules.csv` and name their rule IDs. Read the whole row and implementation before copying. Source-only means no call was found in this CSV, not that the command is unavailable. Abstract classes are extension points, not executable commands.

230 classes; 194 command names used in the active vanilla CSV.

| Command | Source / parent | S / C | Vanilla example and rule |
|---|---|---|---|
| `AbortMission` | `AbortMission.java` → `BaseCommandPlugin` | 3 / 0 | `AbortMission` — `anhCantinaDeny` (script) |
| `AbortWait` | `AbortWait.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `AcceptMission` | `AcceptMission.java` → `BaseCommandPlugin` | 3 / 0 | `AcceptMission` — `anhCantinaAccept` (script) |
| `ActivateAbility` | `ActivateAbility.java` → `BaseCommandPlugin` | 2 / 0 | `ActivateAbility $player.fleetId transponder` — `greetingDefaultTurnOnT` (script) |
| `AddAbility` | `AddAbility.java` → `BaseCommandPlugin` | 8 / 0 | `AddAbility fracture_jump` — `StrandedInDeepSpace3c` (script) |
| `AddBarEvent` | `salvage/AddBarEvent.java` → `BaseCommandPlugin` | 16 / 0 | `AddBarEvent lpp_hesperusShrine_barEvent "Find someone bribable in local traffic control" "A motley crowd of distinctly non-Luddic spacer officers drink and talk. They can't all be honest traders. One of them might know how to sneak a shuttle past traffic control..."` — `lppHesperusBarEventAddA` (script) |
| `AddCommodity` | `AddCommodity.java` → `AddRemoveCommodity` | 73 / 0 | `AddCommodity credits $gaFC_sellOutPrice` — `gaFCIsirahMercAcceptDeal` (script) |
| `AddCredits` | `AddCredits.java` → `BaseCommandPlugin` | 92 / 0 | `AddCredits -10000` — `oyaTanaicaVambrace2` (script) |
| `AddGAOfficerToCoatl` | `academy/AddGAOfficerToCoatl.java` → `BaseCommandPlugin` | 3 / 0 | `AddGAOfficerToCoatl` — `gaMeetHegLieutenantOnCoatlSel` (script) |
| `AddOption` | `AddOption.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `AddPopGrowth` | `AddPopGrowth.java` → `BaseCommandPlugin` | 4 / 0 | `AddPopGrowth 3 90 "Rescued abandoned miners"` — `LOCRMremoveBecauseColony2` (script) |
| `AddPotentialContact` | `AddPotentialContact.java` → `BaseCommandPlugin` | 22 / 0 | `AddPotentialContact sedge` — `lkeSedgeContactCheckA` (script) |
| `AddRaidObjective` | `salvage/AddRaidObjective.java` → `BaseCommandPlugin` | 19 / 0 | `AddRaidObjective genericIcon "Raid Viren's Pather strongholds" HIGH 3000 LKEVirensRaidFinishedA true` — `lkeVirensRaidStart` (script) |
| `AddRemoveAnyItem` | `AddRemoveAnyItem.java` → `BaseCommandPlugin` | 37 / 0 | `AddRemoveAnyItem SPECIAL shrouded_substrate 10` — `ZGRdevGive2` (script) |
| `AddRemoveCommodity` | `AddRemoveCommodity.java` → `BaseCommandPlugin` | 17 / 0 | `AddRemoveCommodity credits -$entity.LP_tithe true` — `LPTithePay` (script) |
| `AddSelector` | `AddSelector.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `AddShip` | `AddShip.java` → `BaseCommandPlugin` | 3 / 0 | `AddShip $hmdf_member` — `hmdfPostAccept` (script) |
| `AddShipToOtherFleet` | `AddShipToOtherFleet.java` → `BaseCommandPlugin` | 5 / 0 | `AddShipToOtherFleet $entity.zigguratMember` — `gaPZ_ttHubPayAccept` (script) |
| `AddStoryPoints` | `AddStoryPoints.java` → `BaseCommandPlugin` | 17 / 0 | `AddStoryPoints 1` — `lppJaspisWrapUp5` (script) |
| `AddText` | `AddText.java` → `BaseCommandPlugin` | 130 / 0 | `AddText "canScanGates = $global.canScanGates"` — `devGateCanScanOnSel` (script) |
| `AddTextSmall` | `AddTextSmall.java` → `BaseCommandPlugin` | 231 / 0 | `AddTextSmall "    - Use an Active Sensor Burst to increase sensor range\n    - Use an Interdiction Pulse to increase burn level\n    - Lasts for a certain number of light-years traveled\n    - Larger increase when used near the Abyssal Light" highlight` — `abyssalLightBeginAgain` (script) |
| `AddXP` | `AddXP.java` → `BaseCommandPlugin` | 20 / 0 | `AddXP 5000` — `LOCRMoffloadMiners2` (script) |
| `AdjustRep` | `AdjustRep.java` → `BaseCommandPlugin` | 172 / 0 | `AdjustRep luddic_church HOSTILE -200` — `CGRmarketWeirdOutro` (script) |
| `AdjustRepActivePerson` | `AdjustRepActivePerson.java` → `BaseCommandPlugin` | 265 / 0 | `AdjustRepActivePerson FRIENDLY 1` — `oyaTanaicaVambrace11` (script) |
| `AdjustRepPerson` | `AdjustRepPerson.java` → `BaseCommandPlugin` | 121 / 2 | `AdjustRepPerson jaspis SUSPICIOUS -1` — `lppEnding4c` (script) |
| `AICores` | `salvage/AICores.java` → `BaseCommandPlugin` | 1 / 2 | `AICores personCanAcceptCores` — `aiCores_turnInOption` (conditions) |
| `AnyNearbyFleetsHostileAndAware` | `AnyNearbyFleetsHostileAndAware.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `ApplyCRDamage` | `ApplyCRDamage.java` → `BaseCommandPlugin` | 4 / 0 | `ApplyCRDamage $entity.fleetPoints 0.4 3 "Vindictive search"` — `anhDiktatPatrolSearch` (script) |
| `ArePatrolsNearby` | `ArePatrolsNearby.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `BarCMD` | `missions/BarCMD.java` → `BaseCommandPlugin` | 52 / 0 | `BarCMD returnFromEvent false` — `lppHesperus_barReturnToBar` (script) |
| `BaseCommandPlugin` (abstract) | `BaseCommandPlugin.java` → `CommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `BeginConversation` | `BeginConversation.java` → `BaseCommandPlugin` | 142 / 0 | `BeginConversation nanoforge_engineer` — `DEVnanoforgeEngineerOption2` (script) |
| `BeginMission` | `BeginMission.java` → `BaseCommandPlugin` | 27 / 2 | `BeginMission lpp` — `lppHookCurateUpload2` (script) |
| `BroadcastCancelPlayerAction` | `BroadcastCancelPlayerAction.java` → `BaseCommandPlugin` | 4 / 0 | `BroadcastCancelPlayerAction 5000 $sawPlayerTransponderOff` — `scanTalkYourWayOut` (script) |
| `BroadcastPlayerAction` | `BroadcastPlayerAction.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `BroadcastPlayerWaitAction` | `BroadcastPlayerWaitAction.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `Call` | `Call.java` → `CallEvent` | 571 / 237 | `Call $global.gaATG_ref scannedGateForCoureuse` — `gateScanSelFirstTime` (script) |
| `CallEvent` | `CallEvent.java` → `BaseCommandPlugin` | 46 / 26 | `CallEvent $testMissionEventRef success` — `convTMOptionYes` (script) |
| `CaresAboutTransponder` | `CaresAboutTransponder.java` → `BaseCommandPlugin` | 0 / 2 | `CaresAboutTransponder` — `tOffPatrolBegin` (conditions) |
| `CargoPods` | `salvage/CargoPods.java` → `BaseCommandPlugin` | 6 / 0 | `CargoPods printDesc` — `pods_start` (script) |
| `CargoScan` | `CargoScan.java` → `BaseCommandPlugin` | 2 / 0 | `CargoScan` — `tOffCargoScan` (script) |
| `CargoScanApplyResult` | `CargoScanApplyResult.java` → `BaseCommandPlugin` | 4 / 0 | `CargoScanApplyResult $scan_cargoScanResult` — `tOffCargoScanBoarding2` (script) |
| `CESetHidden` | `academy/CESetHidden.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `CheckSetting` | `CheckSetting.java` → `BaseCommandPlugin` | 0 / 1 | `CheckSetting enableSpacerStart` — `ngcSpacerStart` (conditions) |
| `ClearActiveMission` | `ClearActiveMission.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `ClearText` | `ClearText.java` → `BaseCommandPlugin` | 1 / 0 | `ClearText` — `gaATGkantasDenHostileOverride1` (script) |
| `Commission` | `missions/Commission.java` → `BaseCommandPlugin` | 9 / 34 | `Commission personCanGiveCommission` — `cmsn_askForCommissionOpt` (conditions) |
| `CustomsInspectionApplyRepLoss` | `CustomsInspectionApplyRepLoss.java` → `BaseCommandPlugin` | 1 / 0 | `CustomsInspectionApplyRepLoss` — `customsInspectionContinueFromResult` (script) |
| `CustomsInspectionApplyResult` | `CustomsInspectionApplyResult.java` → `BaseCommandPlugin` | 2 / 0 | `CustomsInspectionApplyResult` — `optionPayCIToll` (script) |
| `CustomsInspectionGenerateResult` | `CustomsInspectionGenerateResult.java` → `BaseCommandPlugin` | 1 / 0 | `CustomsInspectionGenerateResult` — `customsInspectionWaitFinished` (script) |
| `DeactivateAbility` | `DeactivateAbility.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `DeliveryMission` | `missions/DeliveryMission.java` → `BaseCommandPlugin` | 1 / 2 | `DeliveryMission checkCompletion score:100` — `delivery_checkCompletion` (conditions) |
| `DemandCargo` | `salvage/DemandCargo.java` → `BaseCommandPlugin` | 1 / 0 | `DemandCargo selectCargo` — `piratesGaveCargo` (script) |
| `DespawnEntity` | `DespawnEntity.java` → `BaseCommandPlugin` | 14 / 0 | `DespawnEntity` — `gaFCProbeEmpty1` (script) |
| `DismissDialog` | `DismissDialog.java` → `BaseCommandPlugin` | 27 / 0 | `DismissDialog` — `defaultLeave` (script) |
| `DistressResponse` | `salvage/DistressResponse.java` → `BaseCommandPlugin` | 15 / 2 | `DistressResponse unrespond` — `dcall_openCommsInhospitable` (script) |
| `DoCanAffordCheck` | `DoCanAffordCheck.java` → `BaseCommandPlugin` | 84 / 0 | `DoCanAffordCheck 10000 oyaTanaica_vambrace2 false` — `oyaTanaicaVambrace1` (script) |
| `DumpMemory` | `DumpMemory.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `DwellerCMD` | `DwellerCMD.java` → `BaseCommandPlugin` | 7 / 0 | `DwellerCMD largeFleet` — `abyssalLight_maxSensorsSel` (script) |
| `EndConversation` | `EndConversation.java` → `BaseCommandPlugin` | 298 / 0 | `EndConversation DO_NOT_FIRE` — `oyaTanaicaAskSampleShuttle` (script) |
| `expire` | `expire.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `FactionFleetNearbyAndAware` | `salvage/FactionFleetNearbyAndAware.java` → `BaseCommandPlugin` | 0 / 1 | `FactionFleetNearbyAndAware $faction.id score:100` — `cob_disableOpts2` (conditions) |
| `FireAll` | `FireAll.java` → `BaseCommandPlugin` | 1745 / 3 | `FireAll PopulateGateOptions` — `gateOpenDialog` (script) |
| `FireBest` | `FireBest.java` → `BaseCommandPlugin` | 1652 / 14 | `FireBest GAATGGateScanSummary` — `gateScanSelFirstTime` (script) |
| `FleetDesc` | `FleetDesc.java` → `BaseCommandPlugin` | 41 / 0 | `FleetDesc` — `lkePatherFleet` (script) |
| `GAReduceRandomRep` | `academy/GAReduceRandomRep.java` → `BaseCommandPlugin` | 1 / 0 | `GAReduceRandomRep` — `gaRequestMeeting2ConvertOption3a` (script) |
| `GateCMD` | `missions/GateCMD.java` → `BaseCommandPlugin` | 8 / 2 | `GateCMD notifyScanned` — `gateScanSelFirstTime` (script) |
| `GateHaulerCMD` | `missions/GateHaulerCMD.java` → `BaseCommandPlugin` | 6 / 2 | `GateHaulerCMD addIntel` — `gh_exploreSel` (script) |
| `GenGAIntroAcademician` | `academy/GenGAIntroAcademician.java` → `BaseCommandPlugin` | 0 / 1 | `GenGAIntroAcademician` — `goToGA_barEvent` (conditions) |
| `GiveOtherFleetAssignment` | `GiveOtherFleetAssignment.java` → `BaseCommandPlugin` | 1 / 0 | `GiveOtherFleetAssignment HOLD 3 "performing customs inspection"` — `customsInspectionAgree` (script) |
| `HA_CMD` | `HA_CMD.java` → `BaseCommandPlugin` | 26 / 45 | `HA_CMD computeMegaTithe` — `LPTitheHasColonies` (conditions) |
| `HailPlayer` | `HailPlayer.java` → `BaseCommandPlugin` | 42 / 0 | `HailPlayer` — `lkePatherFleet` (script) |
| `HasAttentionOfAuthorities` | `HasAttentionOfAuthorities.java` → `BaseCommandPlugin` | 0 / 1 | `HasAttentionOfAuthorities score:500` — `generic_refuseToDealKnownInhospitable` (conditions) |
| `HideFirstPerson` | `HideFirstPerson.java` → `BaseCommandPlugin` | 5 / 0 | `HideFirstPerson` — `lppVolturnShrineFirstVisit4SD` (script) |
| `HideMapMarker` | `HideMapMarker.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `HideSecondPerson` | `HideSecondPerson.java` → `BaseCommandPlugin` | 35 / 0 | `HideSecondPerson` — `lkeSedgeShowPic3` (script) |
| `HideThirdPerson` | `HideThirdPerson.java` → `BaseCommandPlugin` | 10 / 0 | `HideThirdPerson` — `BFFIattendPartyHub1Out2` (script) |
| `HideVisual` | `HideVisual.java` → `BaseCommandPlugin` | 24 / 0 | `HideVisual` — `lppKillaVisitStart2Follow` (script) |
| `Highlight` | `Highlight.java` → `SetTextHighlights` | 230 / 2 | `Highlight $player.shroudedSubstrateAvailable` — `shroudedSubstrateOptSelF` (script) |
| `HighlightComms` | `HighlightComms.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `HostileFleetNearbyAndAware` | `salvage/HostileFleetNearbyAndAware.java` → `BaseCommandPlugin` | 0 / 28 | `HostileFleetNearbyAndAware` — `surveyPrintHostilesText` (conditions) |
| `HT_CMD` | `HT_CMD.java` → `BaseCommandPlugin` | 6 / 2 | `HT_CMD computeDataStats` — `scavBuyHTDataOpt` (conditions) |
| `ImportanceAtLeast` | `ImportanceAtLeast.java` → `RepIsAtWorst` | 0 / 0 | Source-only; inspect implementation |
| `IncreaseSmugglingSuspicion` | `IncreaseSmugglingSuspicion.java` → `BaseCommandPlugin` | 5 / 0 | `IncreaseSmugglingSuspicion 0.9` — `BFFImenesPartySteal19` (script) |
| `InstallCommSniffer` | `InstallCommSniffer.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `IsSeenByAnyFleet` | `IsSeenByAnyFleet.java` → `BaseCommandPlugin` | 0 / 4 | `IsSeenByAnyFleet` — `gaKA_relayPrintHostilesText` (conditions) |
| `IsSeenByPatrols` | `IsSeenByPatrols.java` → `BaseCommandPlugin` | 0 / 1 | `IsSeenByPatrols $faction.id true` — `marketPostOpenToOffPatrols` (conditions) |
| `IsSoughtByPatrols` | `IsSoughtByPatrols.java` → `BaseCommandPlugin` | 0 / 2 | `IsSoughtByPatrols $faction.id score:100` — `marketPostOpenSought` (conditions) |
| `KantaCMD` | `KantaCMD.java` → `BaseCommandPlugin` | 11 / 7 | `KantaCMD loseProtection` — `gaATGkillLokeKantaCheck` (script) |
| `LPTitheCalc` | `LPTitheCalc.java` → `BaseCommandPlugin` | 0 / 1 | `LPTitheCalc` — `LPTitheCheck` (conditions) |
| `MakeFullySurveyed` | `MakeFullySurveyed.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `MakeHostileWhileTOff` | `MakeHostileWhileTOff.java` → `BaseCommandPlugin` | 2 / 0 | `MakeHostileWhileTOff tOff true 7` — `tOffPatrolBeginNoTalk` (script) |
| `MakeNearbyFleetsHostile` | `MakeNearbyFleetsHostile.java` → `BaseCommandPlugin` | 5 / 0 | `MakeNearbyFleetsHostile $personFaction.id 100000 30 true` — `lkeChalcedonMakeFleetsHostile` (script) |
| `MakeNearbyFleetsNonHostile` | `MakeNearbyFleetsNonHostile.java` → `BaseCommandPlugin` | 4 / 0 | `MakeNearbyFleetsNonHostile luddic_path LP_tithe 100000 100` — `payMegaTitheConfirmSel` (script) |
| `MakeOptionOpenCore` | `MakeOptionOpenCore.java` → `BaseCommandPlugin` | 4 / 0 | `MakeOptionOpenCore marketOpenCoreUI CARGO $tradeMode` — `marketOptTradeMulti` (script) |
| `MakeOtherFleetAggressive` | `MakeOtherFleetAggressive.java` → `BaseCommandPlugin` | 27 / 0 | `MakeOtherFleetAggressive true` — `CGRpatrolWeirdModsUnknownEnd` (script) |
| `MakeOtherFleetAggressiveOnce` | `MakeOtherFleetAggressiveOnce.java` → `BaseCommandPlugin` | 3 / 0 | `MakeOtherFleetAggressiveOnce warnAttack true` — `warnAttackEncounter1` (script) |
| `MakeOtherFleetAllowDisengage` | `MakeOtherFleetAllowDisengage.java` → `BaseCommandPlugin` | 6 / 0 | `MakeOtherFleetAllowDisengage true` — `CGRpatrolWeirdModsUnknownEndWeaker` (script) |
| `MakeOtherFleetAvoidContact` | `MakeOtherFleetAvoidContact.java` → `BaseCommandPlugin` | 14 / 0 | `MakeOtherFleetAvoidContact true` — `gaVIPWorkingForKanta2` (script) |
| `MakeOtherFleetDoThing` | `MakeOtherFleetDoThing.java` → `BaseCommandPlugin` | 2 / 0 | `MakeOtherFleetDoThing $gaData_planetId 2 "retrieving data from the ruins" true` — `gaDataGiveCoordsSel` (script) |
| `MakeOtherFleetGoAway` | `MakeOtherFleetGoAway.java` → `BaseCommandPlugin` | 98 / 0 | `MakeOtherFleetGoAway` — `CGRpatrolWeirdModsUnknownEndWeaker` (script) |
| `MakeOtherFleetHostile` | `MakeOtherFleetHostile.java` → `BaseCommandPlugin` | 30 / 0 | `MakeOtherFleetHostile true` — `CGRpatrolWeirdModsUnknownEnd` (script) |
| `MakeOtherFleetImportant` | `MakeOtherFleetImportant.java` → `BaseCommandPlugin` | 44 / 0 | `MakeOtherFleetImportant gaKA false` — `gaKAPatrolCut` (script) |
| `MakeOtherFleetLowRepImpact` | `MakeOtherFleetLowRepImpact.java` → `BaseCommandPlugin` | 3 / 0 | `MakeOtherFleetLowRepImpact distress true 1000` — `dstr_defiantSel` (script) |
| `MakeOtherFleetNonAggressive` | `MakeOtherFleetNonAggressive.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `MakeOtherFleetNonHostile` | `MakeOtherFleetNonHostile.java` → `BaseCommandPlugin` | 86 / 0 | `MakeOtherFleetNonHostile LP_tithe true 100` — `payMegaTitheConfirmSel` (script) |
| `MakeOtherFleetNoRepImpact` | `MakeOtherFleetNoRepImpact.java` → `BaseCommandPlugin` | 3 / 0 | `MakeOtherFleetNoRepImpact true` — `gaVIPReturnToHubGoHostile` (script) |
| `MakeOtherFleetPreventDisengage` | `MakeOtherFleetPreventDisengage.java` → `BaseCommandPlugin` | 30 / 0 | `MakeOtherFleetPreventDisengage cargoScan false` — `scanTalkYourWayOut` (script) |
| `MakePlayerImmediatelyAttackable` | `MakePlayerImmediatelyAttackable.java` → `BaseCommandPlugin` | 1 / 0 | `MakePlayerImmediatelyAttackable` — `customsInspectionAgree` (script) |
| `MarketCMD` | `salvage/MarketCMD.java` → `BaseCommandPlugin` | 25 / 2 | `MarketCMD showDefenses` — `marketHostileSel` (script) |
| `MarketGainRandomRep` | `MarketGainRandomRep.java` → `BaseCommandPlugin` | 7 / 0 | `MarketGainRandomRep` — `BarHRESpeechA` (script) |
| `MarketReduceRandomRep` | `MarketReduceRandomRep.java` → `BaseCommandPlugin` | 2 / 0 | `MarketReduceRandomRep` — `soeDanceEscape2` (script) |
| `MiscCMD` | `salvage/MiscCMD.java` → `BaseCommandPlugin` | 4 / 0 | `MiscCMD addCryosleeperIntel` — `sal_cryosleeperDescAddIntel` (script) |
| `MissionHubCMD` | `missions/MissionHubCMD.java` → `BaseCommandPlugin` | 0 / 12 | `MissionHubCMD hasHub` — `addMHubOption` (conditions) |
| `MovePersonToMarket` | `MovePersonToMarket.java` → `BaseCommandPlugin` | 6 / 0 | `MovePersonToMarket zal kantas_den` — `gaFCZalRecordingEnd` (script) |
| `NGC` | `newgame/NGC.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `NGCAddCargo` | `NGCAddCargo.java` → `BaseCommandPlugin` | 7 / 0 | `NGCAddCargo RESOURCES heavy_machinery 20` — `ngcShepherdPicked` (script) |
| `NGCAddCharacterPoints` | `NGCAddCharacterPoints.java` → `BaseCommandPlugin` | 4 / 0 | `NGCAddCharacterPoints 1` — `ngcSpacerContinue` (script) |
| `NGCAddCredits` | `NGCAddCredits.java` → `BaseCommandPlugin` | 5 / 0 | `NGCAddCredits 2000` — `onNewGameCreationStart` (script) |
| `NGCAddDevStartingScript` | `NGCAddDevStartingScript.java` → `BaseCommandPlugin` | 2 / 0 | `NGCAddDevStartingScript` — `ngcDevStartOption` (script) |
| `NGCAddShip` | `NGCAddShip.java` → `BaseCommandPlugin` | 17 / 0 | `NGCAddShip wolf_Starting` — `ngcWolfPicked` (script) |
| `NGCAddShipSilent` | `NGCAddShipSilent.java` → `BaseCommandPlugin` | 1 / 0 | `NGCAddShipSilent hammerhead_Balanced` — `ngcRandomPicked` (script) |
| `NGCAddStandardStartingScript` | `NGCAddStandardStartingScript.java` → `BaseCommandPlugin` | 3 / 0 | `NGCAddStandardStartingScript` — `ngcSpacerContinue` (script) |
| `NGCCanSkipTutorial` | `NGCCanSkipTutorial.java` → `BaseCommandPlugin` | 0 / 2 | `NGCCanSkipTutorial` — `ngcSkipTutorial` (conditions) |
| `NGCDone` | `NGCDone.java` → `BaseCommandPlugin` | 5 / 0 | `NGCDone` — `ngcSpacerContinue` (script) |
| `NGCRemoveCargo` | `NGCRemoveCargo.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `NGCRemoveShip` | `NGCRemoveShip.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `NGCSetAptitude` | `NGCSetAptitude.java` → `BaseCommandPlugin` | 4 / 0 | `NGCSetAptitude technology 0` — `ngcDevStartOption` (script) |
| `NGCSetCustom` | `NGCSetCustom.java` → `BaseCommandPlugin` | 1 / 0 | `NGCSetCustom customDevStart true` — `ngcDevStartOption2` (script) |
| `NGCSetDifficulty` | `NGCSetDifficulty.java` → `BaseCommandPlugin` | 5 / 0 | `NGCSetDifficulty normal` — `ngcSpacerPicked` (script) |
| `NGCSetSkill` | `NGCSetSkill.java` → `BaseCommandPlugin` | 2 / 0 | `NGCSetSkill gunnery_implants 0` — `ngcDevStartOption` (script) |
| `NGCSetStartingLocation` | `NGCSetStartingLocation.java` → `BaseCommandPlugin` | 5 / 0 | `NGCSetStartingLocation Corvus -2500 3000` — `ngcSpacerContinue` (script) |
| `NGCSetWithTimePass` | `NGCSetWithTimePass.java` → `BaseCommandPlugin` | 2 / 0 | `NGCSetWithTimePass false` — `ngcDevStartOption` (script) |
| `NPCWantsComms` | `NPCWantsComms.java` → `BaseCommandPlugin` | 0 / 1 | `NPCWantsComms` — `checkNPCWantingToTalk` (conditions) |
| `Objectives` | `salvage/Objectives.java` → `BaseCommandPlugin` | 14 / 11 | `Objectives hasWormholeAnchor` — `stable_deployWormholeOpt` (conditions) |
| `OpenCommDirectory` | `OpenCommDirectory.java` → `BaseCommandPlugin` | 2 / 0 | `OpenCommDirectory` — `marketOptionCommDir` (script) |
| `OpenComms` | `OpenComms.java` → `BaseCommandPlugin` | 3 / 0 | `OpenComms` — `customsInspectionWaitFinished` (script) |
| `OpenCoreTab` | `OpenCoreTab.java` → `BaseCommandPlugin` | 6 / 0 | `OpenCoreTab CARGO OPEN` — `surveyOptionPerformSurvey` (script) |
| `PaginatedOptions` | `PaginatedOptions.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `PaginatedOptionsExample` | `PaginatedOptionsExample.java` → `PaginatedOptions` | 0 / 0 | Source-only; inspect implementation |
| `PauseMusic` | `PauseMusic.java` → `BaseCommandPlugin` | 3 / 0 | `PauseMusic` — `BFFIarrestShotBorn0` (script) |
| `PickCommsNPC` | `PickCommsNPC.java` → `BaseCommandPlugin` | 0 / 1 | `PickCommsNPC score:1000` — `checkNPCWantingToTalk` (conditions) |
| `Ping` | `Ping.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `PK_CMD` | `PK_CMD.java` → `BaseCommandPlugin` | 8 / 9 | `PK_CMD rightPostToAcceptPK` — `LOCRLoffloadToKnights` (conditions) |
| `PLAddEntry` | `PLAddEntry.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `PlayCustomMusic` | `PlayCustomMusic.java` → `BaseCommandPlugin` | 8 / 0 | `PlayCustomMusic music_luddite_shrine` — `BFFIarrestCurate1dev` (script) |
| `PlayerFleetHasFragmentShips` | `PlayerFleetHasFragmentShips.java` → `BaseCommandPlugin` | 0 / 2 | `PlayerFleetHasFragmentShips` — `TTmarketPostWeirdHullmods1` (conditions) |
| `PlayerFleetHasShipWithBaseHull` | `PlayerFleetHasShipWithBaseHull.java` → `BaseCommandPlugin` | 0 / 27 | `!PlayerFleetHasShipWithBaseHull onslaught_mk1` — `gsVambOptHubDescriptionB` (conditions) |
| `PlayerFleetHasShipWithId` | `PlayerFleetHasShipWithId.java` → `BaseCommandPlugin` | 0 / 3 | `!PlayerFleetHasShipWithId hamatsu` — `gaRH_noHamatsuOption` (conditions) |
| `PlayerFleetHasShroudedShips` | `PlayerFleetHasShroudedShips.java` → `BaseCommandPlugin` | 0 / 6 | `PlayerFleetHasShroudedShips` — `TTmarketPostWeirdHullmods2` (conditions) |
| `PlayerHasCargo` | `PlayerHasCargo.java` → `BaseCommandPlugin` | 0 / 24 | `PlayerHasCargo alpha_core` — `zig_exploreSabotage` (conditions) |
| `PlaySound` | `PlaySound.java` → `BaseCommandPlugin` | 1 / 0 | `PlaySound hit_heavy` — `GS_ADVENTURERS_cont1SelBoom` (script) |
| `PrintDescription` | `PrintDescription.java` → `BaseCommandPlugin` | 22 / 0 | `PrintDescription 3` — `defaultOpenDialog` (script) |
| `PrintWreckDescription` | `salvage/PrintWreckDescription.java` → `BaseCommandPlugin` | 9 / 0 | `PrintWreckDescription` — `GS_AI_CORES_open` (script) |
| `RedPlanet` | `salvage/RedPlanet.java` → `BaseCommandPlugin` | 1 / 0 | `RedPlanet genLoot` — `psi_salvage` (script) |
| `ReinitDialog` | `ReinitDialog.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `RemoveBarEvent` | `salvage/RemoveBarEvent.java` → `BaseCommandPlugin` | 1 / 0 | `RemoveBarEvent jabr_barEvent` — `jabr_barStoryOptionFromBar` (script) |
| `RemoveCommodity` | `RemoveCommodity.java` → `BaseCommandPlugin` | 44 / 0 | `RemoveCommodity crew 1` — `GS_ADVENTURERS_cont1SelBoom` (script) |
| `RemoveContact` | `RemoveContact.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `RemoveOption` | `RemoveOption.java` → `BaseCommandPlugin` | 209 / 0 | `RemoveOption strandedInDeepSpace3a` — `StrandedInDeepSpace3a` (script) |
| `RemoveShip` | `RemoveShip.java` → `BaseCommandPlugin` | 5 / 0 | `RemoveShip $entity.zigguratMember` — `gaPZ_ttHubPayAccept` (script) |
| `RemoveShipWithBaseHull` | `RemoveShipWithBaseHull.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `RemoveShipWithId` | `RemoveShipWithId.java` → `BaseCommandPlugin` | 2 / 0 | `RemoveShipWithId hamatsu` — `kpHamatsuSel` (script) |
| `RepairAll` | `RepairAll.java` → `BaseCommandPlugin` | 1 / 0 | `RepairAll` — `marketOptionRepairAll` (script) |
| `RepairAvailable` | `RepairAvailable.java` → `BaseCommandPlugin` | 0 / 3 | `RepairAvailable` — `marketAddOptionRepair1` (conditions) |
| `RepairEnoughSupplies` | `RepairEnoughSupplies.java` → `BaseCommandPlugin` | 0 / 2 | `RepairEnoughSupplies` — `marketAddOptionRepair1` (conditions) |
| `RepairNeeded` | `RepairNeeded.java` → `BaseCommandPlugin` | 0 / 3 | `RepairNeeded` — `marketAddOptionRepair1` (conditions) |
| `RepGTE` | `RepGTE.java` → `RepIsAtWorst` | 0 / 31 | `RepGTE luddic_church FRIENDLY` — `lppJangalaProtestRespChurch` (conditions) |
| `RepIsAtBest` | `RepIsAtBest.java` → `BaseCommandPlugin` | 0 / 10 | `RepIsAtBest $faction.id INHOSPITABLE` — `marketPostOpenNoTrade` (conditions) |
| `RepIsAtWorst` | `RepIsAtWorst.java` → `BaseCommandPlugin` | 0 / 4 | `RepIsAtWorst $faction.id FAVORABLE` — `optionRefuseCITollPassRep` (conditions) |
| `RepLTE` | `RepLTE.java` → `RepIsAtBest` | 0 / 14 | `RepLTE luddic_church INHOSPITABLE score:50` — `lppGileadShrineStartHubTextC` (conditions) |
| `ResetActivePerson` | `ResetActivePerson.java` → `BaseCommandPlugin` | 4 / 0 | `ResetActivePerson` — `gaOpPlanetBasicCont` (script) |
| `RestoreSavedVisual` | `RestoreSavedVisual.java` → `BaseCommandPlugin` | 6 / 0 | `RestoreSavedVisual` — `gaKA_ttGoBackToBarContSel` (script) |
| `ResumeNormalMusic` | `ResumeNormalMusic.java` → `BaseCommandPlugin` | 12 / 0 | `ResumeNormalMusic` — `BFFIarrestOutro2` (script) |
| `RollProbability` | `RollProbability.java` → `BaseCommandPlugin` | 0 / 33 | `RollProbability 0.5` — `GS_ADVENTURERS_cont1SelBoom` (conditions) |
| `SalvageDefenderInteraction` | `salvage/SalvageDefenderInteraction.java` → `BaseCommandPlugin` | 10 / 0 | `SalvageDefenderInteraction` — `sal_printDefaultDefenders` (script) |
| `SalvageEntity` | `salvage/SalvageEntity.java` → `BaseCommandPlugin` | 7 / 1 | `SalvageEntity descDebris` — `sal_scavengeDebris` (script) |
| `SalvageGenFromSeed` | `salvage/SalvageGenFromSeed.java` → `BaseCommandPlugin` | 10 / 0 | `SalvageGenFromSeed` — `sal_tutorial_storyPointUse2` (script) |
| `SalvageSpecialInteraction` | `salvage/SalvageSpecialInteraction.java` → `BaseCommandPlugin` | 6 / 0 | `SalvageSpecialInteraction` — `sal_checkSpecialFound` (script) |
| `SaveCurrentVisual` | `SaveCurrentVisual.java` → `BaseCommandPlugin` | 3 / 0 | `SaveCurrentVisual` — `gaKA_ttContinueSel` (script) |
| `SetActiveMission` | `SetActiveMission.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `SetCodexEntryId` | `SetCodexEntryId.java` → `BaseCommandPlugin` | 1 / 0 | `SetCodexEntryId codex_item_orbital_fusion_lamp` — `fusionLampBeginPlayer` (script) |
| `SetColor` | `SetColor.java` → `BaseCommandPlugin` | 10 / 0 | `SetColor $core_lampGlowColor 255,50,50,255` — `fusionLampRed` (script) |
| `SetEnabled` | `SetEnabled.java` → `BaseCommandPlugin` | 154 / 0 | `SetEnabled substrate_selectWeapons false` — `CheckPlayerHasNoSubstrate` (script) |
| `SetFlagship` | `SetFlagship.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `SetHistorianBlurbShownAfterDelay` | `SetHistorianBlurbShownAfterDelay.java` → `BaseCommandPlugin` | 29 / 0 | `SetHistorianBlurbShownAfterDelay historyBlurb00` — `HistorianBackstoryBlurb00` (script) |
| `SetLater` | `SetLater.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `SetNearbyFleetsVariable` | `SetNearbyFleetsVariable.java` → `BaseCommandPlugin` | 1 / 0 | `SetNearbyFleetsVariable 5000 $faction.id $patrolAllowTOff true 10` — `scanTalkYourWayOut` (script) |
| `SetOptionColor` | `SetOptionColor.java` → `BaseCommandPlugin` | 70 / 0 | `SetOptionColor devGateScanOn gray` — `devGateCanScanOn` (script) |
| `SetOptionText` | `SetOptionText.java` → `BaseCommandPlugin` | 1 / 0 | `SetOptionText mh_open "Inquire about available jobs"` — `asebOpenMHOptionText` (script) |
| `SetOtherFleetAllowJump` | `SetOtherFleetAllowJump.java` → `BaseCommandPlugin` | 1 / 0 | `SetOtherFleetAllowJump true` — `gaProbeGiveAid` (script) |
| `SetPersonHidden` | `SetPersonHidden.java` → `BaseCommandPlugin` | 71 / 0 | `SetPersonHidden nanoforge_engineer false` — `gsVambraceSample` (script) |
| `SetPersonPortrait` | `SetPersonPortrait.java` → `BaseCommandPlugin` | 19 / 0 | `SetPersonPortrait bornanew "jethro_bornanew2"` — `lkeSedgeShowPic2` (script) |
| `SetPromptText` | `SetPromptText.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `SetShortcut` | `SetShortcut.java` → `BaseCommandPlugin` | 114 / 0 | `SetShortcut defaultLeave "ESCAPE"` — `defaultOpenDialog` (script) |
| `SetStoryColor` | `SetStoryColor.java` → `BaseCommandPlugin` | 3 / 0 | `SetStoryColor ome_askHire` — `ome_askHireMerc` (script) |
| `SetStoryOption` | `SetStoryOption.java` → `BaseCommandPlugin` | 64 / 0 | `SetStoryOption shroudedSubstrateBegin 1 analyzeSubstrate technology "Analyzed Shrouded Substrate"` — `shroudedSubstrateBegin` (script) |
| `SetTextHighlightColors` | `SetTextHighlightColors.java` → `BaseCommandPlugin` | 40 / 0 | `SetTextHighlightColors bad` — `marketPostOpenNoTradeHostile` (script) |
| `SetTextHighlights` | `SetTextHighlights.java` → `BaseCommandPlugin` | 184 / 0 | `SetTextHighlights "10,000"` — `oyaTanaicaVambrace1` (script) |
| `SetTooltip` | `SetTooltip.java` → `BaseCommandPlugin` | 152 / 0 | `SetTooltip substrate_selectWeapons "No Shrouded Substrate available."` — `CheckPlayerHasNoSubstrate` (script) |
| `SetTooltipHighlightColors` | `SetTooltipHighlightColors.java` → `BaseCommandPlugin` | 22 / 0 | `SetTooltipHighlightColors marketRepair buttonShortcut buttonShortcut` — `marketAddOptionRepair1` (script) |
| `SetTooltipHighlights` | `SetTooltipHighlights.java` → `BaseCommandPlugin` | 39 / 0 | `SetTooltipHighlights marketRepair $global.repairSupplyCost $player.supplies` — `marketAddOptionRepair1` (script) |
| `ShowDefaultVisual` | `ShowDefaultVisual.java` → `BaseCommandPlugin` | 350 / 0 | `ShowDefaultVisual` — `defaultOpenDialog` (script) |
| `ShowFirstPerson` | `ShowFirstPerson.java` → `BaseCommandPlugin` | 1 / 0 | `ShowFirstPerson` — `anhDiktatPatrolStall4` (script) |
| `ShowGAOfficer` | `academy/ShowGAOfficer.java` → `BaseCommandPlugin` | 1 / 0 | `ShowGAOfficer` — `gaWithHegOfficer` (script) |
| `ShowImageVisual` | `ShowImageVisual.java` → `BaseCommandPlugin` | 95 / 0 | `ShowImageVisual abyssal_light2` — `abyssalLightDwellerBegin` (script) |
| `ShowLargePlanet` | `ShowLargePlanet.java` → `BaseCommandPlugin` | 49 / 0 | `ShowLargePlanet` — `oyaTanaicaAskSampleShuttle` (script) |
| `ShowMapMarker` | `ShowMapMarker.java` → `BaseCommandPlugin` | 1 / 0 | `ShowMapMarker kazeron Kazeron "You can go to Kazeron to negotiate League membership and an end to the blockade."` — `plArmadaComms` (script) |
| `ShowPersonVisual` | `ShowPersonVisual.java` → `BaseCommandPlugin` | 194 / 0 | `ShowPersonVisual` — `DEVnanoforgeEngineerOption2` (script) |
| `ShowPic` | `ShowPic.java` → `BaseCommandPlugin` | 2 / 0 | `ShowPic ai_core_uninstall` — `RACA_init` (script) |
| `ShowRemainingCapacity` | `ShowRemainingCapacity.java` → `BaseCommandPlugin` | 5 / 0 | `ShowRemainingCapacity $cheapCom_commodityId` — `cheapComOfferTextLocalBar` (script) |
| `ShowResCost` | `ShowResCost.java` → `BaseCommandPlugin` | 2 / 0 | `ShowResCost supplies $stabilizeSupplies true` — `pods_stabilizeSel` (script) |
| `ShowSecondPerson` | `ShowSecondPerson.java` → `BaseCommandPlugin` | 34 / 0 | `ShowSecondPerson bornanew` — `lkeSedgeShowPic2` (script) |
| `ShowThirdPerson` | `ShowThirdPerson.java` → `BaseCommandPlugin` | 11 / 0 | `ShowThirdPerson ulmus_pond` — `BFFIattendPartyHub1Intro2` (script) |
| `ShowThreatPersonVisual` | `ShowThreatPersonVisual.java` → `BaseCommandPlugin` | 2 / 0 | `ShowThreatPersonVisual` — `threatCommLink` (script) |
| `ShrineCMD` | `missions/ShrineCMD.java` → `BaseCommandPlugin` | 33 / 0 | `ShrineCMD addIntel beholder_station` — `lppHookCurateGiveShrineIntel` (script) |
| `SubCredits` | `SubCredits.java` → `BaseCommandPlugin` | 2 / 0 | `SubCredits $ise_bribeAmount` — `ise_offerBribeAmountSel` (script) |
| `TakeRepCheck` | `TakeRepCheck.java` → `BaseCommandPlugin` | 1 / 0 | `TakeRepCheck $faction.id $repCheckResult` — `customsInspectionWaitFinished` (script) |
| `TT_CMD` | `TT_CMD.java` → `BaseCommandPlugin` | 3 / 1 | `TT_CMD isArroyoContact score:2` — `ZGRacknowledgeArroyo` (conditions) |
| `UnhideGACharacters` | `academy/UnhideGACharacters.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `UnhidePerson` | `UnhidePerson.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `UninstallCommSniffer` | `UninstallCommSniffer.java` → `BaseCommandPlugin` | 0 / 0 | Source-only; inspect implementation |
| `UniqueEncounter` | `UniqueEncounter.java` → `BaseCommandPlugin` | 8 / 0 | `UniqueEncounter setInteractedWith $gsType` — `GS_AI_CORES_cont1Sel` (script) |
| `unset` | `unset.java` → `BaseCommandPlugin` | 253 / 0 | `unset $GAATGluddicPostScan` — `gaATGgateScanSummaryLuddic` (script) |
| `unsetAll` | `unsetAll.java` → `BaseCommandPlugin` | 6 / 0 | `unsetAll $market.foodShortage` — `marketPostOpenFSEPlayerFast` (script) |
| `UpdateMemory` | `UpdateMemory.java` → `BaseCommandPlugin` | 4 / 0 | `UpdateMemory` — `greetingDefaultTurnOnT` (script) |
| `Wait` | `Wait.java` → `BaseCommandPlugin` | 1 / 0 | `Wait $global.ciWait 0.5 $global.ciFinished $global.ciInterrupted $global.ciInProgress "Scanning..."` — `customsInspectionAgree` (script) |
| `WasHistorianBlurbShown` | `WasHistorianBlurbShown.java` → `BaseCommandPlugin` | 0 / 29 | `!WasHistorianBlurbShown historyBlurb00` — `HistorianBackstoryBlurb00` (conditions) |
| `ZGRTurnIn` | `salvage/ZGRTurnIn.java` → `BaseCommandPlugin` | 1 / 2 | `!ZGRTurnIn playerHasSellableItems` — `ZGRtechReturnDisable` (conditions) |
| `ZigguratCMD` | `salvage/ZigguratCMD.java` → `BaseCommandPlugin` | 6 / 6 | `ZigguratCMD initEncounters` — `zig_PostShipRecoverySpecial` (script) |

## Source checks

Recipes were checked against execute methods rather than inferred from the simulator table. The class inventory follows command inheritance in the official API sources; aliases must be checked through their parent too. Legacy mission commands, new-game commands and faction-story dispatchers are not general-purpose quest APIs. Crawl details and source hashes are in [KEY_USAGE](KEY_USAGE.md).
