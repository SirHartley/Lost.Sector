# Vanilla rules key usage index

Starsector **0.98a-RC8**. This is a searchable corpus index, not a list of globally available keys. Start with [the memory dictionary](MEMORY.md) for reusable facts/pronouns/flags and [the authoring guide](../RULES_AUTHORING.md) for ownership and lifetime.

## Coverage

This table contains literal keys from vanilla rules only. It covers Conditions, Script, Text and Options in named records, excluding blank IDs and IDs starting with `#`. Scoped spellings remain separate, including colon-bearing families such as `$market.ind:heavyindustry`.

Lost.Sector keys are not included. For the mod's public tokens and dialogue routing, see [RULES.md](../RULES.md#project-routing).

The lexical key pattern is `\$[A-Za-z_][A-Za-z0-9_]*(?:[.:][A-Za-z_][A-Za-z0-9_]*)*`. It finds literal references, not arbitrary dynamically assembled Java keys. Dynamic fact families and the separately extracted generator/fact/MemFlags dictionaries are in MEMORY.md. A token found in Text need not be stored memory. A token found only in Conditions is not automatically useful prose.

C = Conditions, S = Script, T = Text, O = Options. Counts are distinct rule IDs containing the spelling, not runtime frequency. Example IDs belong to vanilla `data/campaign/rules.csv`, not the mod's additive file. The assignment column records a direct CSV assignment when one exists; excerpts ending in `...` are shortened lookup clues, not executable recipes. Absence of a direct assignment does not prove a key has no writer: commands, mission updates and fact generators write keys too.

API hints are literal String mentions in source files, not verified setter/type/lifetime declarations. Search the key and its MemFlags constant, then read the relevant method and its callers. Do not infer permission to overwrite a key from an entry in this table. In particular, Academy/story/legacy mission prefixes are owned by those missions and are not a reusable state namespace.

## Source provenance

Bundle hashes below identify the exact local `starsector-knowledge` inputs. The extracted rules-text hash uses the bundle's rules section after universal-newline decoding (including its separating whitespace), not the original installation file's bytes. Hashes establish reproducibility, not semantic correctness.

| Input | SHA-256 |
|---|---|
| `data/campaign.txt` | `28804a214e6958ef5ab1b960f1c60bd0f092ba6b413643e4bce30c01a24eea3a` |
| `sources-api/impl.campaign.java` | `0caa97cda38376087833775a82c88526cbe6c9e33d7949b3a74dd651369c4dc5` |
| `sources-api/util.java` | `a1debe2cf90f3f7edc778b80ff7ef85bcd53b53b10448ffdc4df8bf5a6fda393` |
| `sources-obf/campaign.rules.java` | `d812130893200b6c59d94673dc93fc40f873d7ae9a2dd6a7d545c7f6a134a2bf` |
| `sources-obf/campaign.java` | `02dfd1c04f0474b88105103b63eba2911832f1f40b54761004c8bb7409bb201b` |
| Extracted normalized rules section | `648075eaff6c32e30fff882d62beac12ee17d82ba80018ab5167f585b8d49d53` |

## Index

Use exact-key search. Entries are alphabetic by their full spelling, so scoped keys stay together.

| Key | Columns / rules | Example consumer | Direct assignment / API search hint |
|---|---|---|---|
| `$BrotherOrSister` | OT / 5 | `gaCOPatherConvince1` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$COB_burstRange` | ST / 1 | `cob_neutrinoBurst` (T) | `impl/campaign/rulecmd/salvage/Objectives.java` |
| `$Faction` | T / 2 | `relLevelHostile` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/events/BaseEventPlugin.java` |
| `$GAATGhegSendScan` | CS / 2 | `gaATGhegFleetScanTransmit` (C) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$GAATGhegemonyScanFleet` | C / 1 | `gaATGhegGateOpenDialog` (C) | `impl/campaign/missions/academy/GAAtTheGates.java`, `impl/campaign/missions/luddic/BornanewFilesFalseIdols.java` |
| `$GAATGluddicPostScan` | CS / 1 | `gaATGgateScanSummaryLuddic` (C) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$GAATGluddicScanGate` | C / 1 | `gaATGluddicScanGate` (C) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$GAATGpirateScanGate` | C / 1 | `gaATGpirateScanGate` (C) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$GAATGscanAlarm` | CS / 1 | `gaATGscanTripwireAlarm` (C) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$GAATGscanJammer` | CS / 3 | `gaATGscanJammerOpenDialog` (C) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$GAATGttScanGate` | C / 1 | `gaATGttScanGate` (C) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$HeOrShe` | ST / 695 | `TTmarketWeirdMods1a` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/missions/hub/BaseHubMission.java` |
| `$HisOrHer` | T / 80 | `TTmarketPostWeirdHMbegin` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/missions/hub/BaseHubMission.java` |
| `$KOLT_target` | OT / 6 | `KOLTHolyArmadaComms` (T) | `impl/campaign/rulecmd/HA_CMD.java` |
| `$LP_megaTithe` | S / 2 | `payMegaTitheSel` (S) | `impl/campaign/rulecmd/HA_CMD.java` |
| `$LP_megaTitheDGS` | ST / 1 | `payMegaTitheSel` (T) | `impl/campaign/rulecmd/HA_CMD.java` |
| `$LP_megaTitheDuration` | ST / 1 | `payMegaTitheSel` (T) | `impl/campaign/rulecmd/HA_CMD.java` |
| `$LP_titheAskedFor` | CS / 1 | `LPTitheCheck` (C) | `$LP_titheAskedFor = true 7` — `LPTitheCheck` |
| `$LP_titheConv` | S / 1 | `LPTitheCheck` (S) | `$LP_titheConv = true 0` — `LPTitheCheck` |
| `$LP_tithePaid` | C / 1 | `LPTitheCheck` (C) | Trace owning rule/command or generated interaction data |
| `$PersonName` | ST / 37 | `convDefaultGreetingSpacer` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/missions/hub/BaseHubMission.java` |
| `$PersonPost` | T / 5 | `lppHookCurateSeeyaAgnostic` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/missions/hub/BaseHubMission.java` |
| `$PersonRank` | T / 39 | `greetingDefaultTOffNormal` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/missions/hub/BaseHubMission.java` |
| `$PlayerHeOrShe` | T / 1 | `BFFIulmusTalkPcBad1b` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/events/BaseEventPlugin.java` |
| `$PlayerName` | T / 22 | `lppEnding1` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$PlayerSirOrMadam` | T / 74 | `abyssalLightBegin` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$Post` | T / 47 | `PathFleetWeirdModsEnd4` (T) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$Rank` | OT / 29 | `lppJangalaProtestMarinesHelpHeg2l` (T) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$SP_supplies` | COST / 6 | `lcSacredProtectorsComms` (T) | `impl/campaign/rulecmd/HA_CMD.java` |
| `$ShipOrFleet` | T / 4 | `abyssalLight_dwellerContSel` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$SirOrMadam` | OT / 4 | `gaKAPatrolLie` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$TTMA_command` | C / 1 | `ttma_commandHail` (C) | `impl/campaign/intel/group/TTMercenaryAttack.java` |
| `$TheFaction` | T / 7 | `CMSNResignAskToConfirm_default` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/events/BaseEventPlugin.java` |
| `$TheFactionLong` | T / 3 | `marketPostOpenNoTrade` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/events/BaseEventPlugin.java` |
| `$ZGRbigUps` | S / 1 | `ZGRacknowledgeContinue` (S) | `$ZGRbigUps = 0` — `ZGRacknowledgeContinue` |
| `$aOrAn` | T / 11 | `GS_AI_CORES_open` (T) | `impl/campaign/CoreCampaignPluginImpl.java`, `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$abandonedStation` | C / 1 | `flavorTextAbandonedStation` (C) | `impl/MusicPlayerPluginImpl.java`, `util/Misc.java` |
| `$abortedHeistDiscussion` | CS / 2 | `BFFImmBornOptStealAgainB` (C) | `$abortedHeistDiscussion = true 0` — `BFFImeetMenesBornOptSteal4d` |
| `$abyssalGasGiantTurbulence` | C / 2 | `abyssalGasGiantTurbulence` (C) | `impl/campaign/enc/AbyssalRogueStellarObjectDireHintsEPEC.java` |
| `$abyssalPlanetoidMiningOp` | C / 2 | `abyssalPlanetoidMiningOp` (C) | `impl/campaign/enc/AbyssalRogueStellarObjectDireHintsEPEC.java` |
| `$activated` | CS / 6 | `gateHaulerStart1` (C) | `$activated = true` — `gh_activateSel` |
| `$activating` | CS / 2 | `gateHaulerStart3` (C) | `$activating = true 1` — `gh_activateSel` |
| `$aem_eventRef` | S / 1 | `aem_runSensorPackageOptionSel` (S) | `impl/campaign/intel/AnalyzeEntityMissionIntel.java` |
| `$aem_target` | C / 2 | `aem_runSensorPackageOptionHostile` (C) | `impl/campaign/intel/AnalyzeEntityMissionIntel.java` |
| `$affk_marketName` | OST / 2 | `KantaFavorCourierHail2` (T) | `impl/campaign/missions/AFavorForKanta.java` |
| `$agreedWithMurder` | CS / 3 | `lkeChalcedonSedgeOptMurder` (C) | `$agreedWithMurder = true 0` — `LKEchalcedonSedgeKillOptD` |
| `$allegationsMade` | S / 1 | `PKSentinelConfrontPKTrust` (S) | `$allegationsMade++` — `PKSentinelConfrontPKTrust` |
| `$anh_diktatPatrol` | C / 1 | `anhDiktatPatrolEncounter` (C) | `impl/campaign/missions/ANewHope.java` |
| `$anh_noCompleteShown` | CS / 2 | `anhCheckCompletionCont` (C) | `$anh_noCompleteShown = true 0` — `anhCompletedButNoDocking` |
| `$another` | CS / 2 | `gaATGzalMissionStartHub3b` (C) | `$another = true 0` — `gaATGzalMissionHubC2` |
| `$answered` | CS / 11 | `gaATGdaudMeetingHub1` (C) | `$answered = 0` — `gaATGdaudMeeting15` |
| `$answeredPOL` | CS / 6 | `LKEjethroAngelsTalkTellZigPoL1` (C) | `$answeredPOL = true 0` — `LKEjethroAngelsPOLa` |
| `$answersDone` | CS / 20 | `sdtuHyderTalk3Answers1` (C) | `$answersDone++` — `sdtuHyderTalk3HegemonyA` |
| `$areHonestPirates` | S / 1 | `LOCRPareYouPiratesHonest` (S) | `$areHonestPirates = true` — `LOCRPareYouPiratesHonest` |
| `$arrayFirstAskedForHazard` | CS / 8 | `gaDHOendingDataTransferOptHaz` (C) | `$arrayFirstAskedForHazard = true 0` — `gaDHOendingRewardFirstB` |
| `$aseb_givePlayerNeutrinoDetector` | CS / 7 | `asebGivePlayerNDetectorOnAccept` (C) | `$aseb_givePlayerNeutrinoDetector = true 0` — `gaDAMissionTextBasic` |
| `$aseb_printedFirstReturnGreeting` | CS / 2 | `asebMissionReturnGreeting` (C) | `$aseb_printedFirstReturnGreeting = true 0` — `asebMissionReturnGreeting` |
| `$askDoTutorial` | CS / 1 | `soeDuelPrepTutorialAsk` (C) | `$askDoTutorial = true 0` — `soeDuelPrepTutorialAsk` |
| `$askNicely` | CS / 4 | `gaATGzalMissionStartHub2e` (C) | `$askNicely = true 0` — `gaATGzalMissionBetterPlan2` |
| `$asked1` | CS / 2 | `gaATGttScanFleetHub1` (C) | `$asked1 = true 0` — `gaATGttScanFleet1` |
| `$askedA` | CS / 2 | `gaATGdaudMeetingHubOptionA` (C) | `$askedA = true 0` — `gaATGdaudMeetingHubA1` |
| `$askedAboutAcademy` | CS / 2 | `gaAddOptionInfodump` (C) | `$askedAboutAcademy = true 0` — `gaInfoDump` |
| `$askedAboutAgent` | S / 2 | `sdtuUmbraAskAgent` (S) | `$askedAboutAgent = true 1` — `sdtuUmbraAskAgent` |
| `$askedAboutArchon` | CS / 6 | `postGAFCretiredArchon` (C) | `$askedAboutArchon = true` — `postGAFCretiredArchon1` |
| `$askedAboutBaird` | CS / 3 | `gaKACoureuseBairdOpt` (C) | `$askedAboutBaird = true` — `gaKACoureuseBairdOptSel` |
| `$askedAboutCavin` | CS / 2 | `gaKACoureuseCavinOpt` (C) | `$askedAboutCavin = true` — `gaKACoureuseAskAbtCavin1` |
| `$askedAboutCotton` | S / 1 | `lppHookCurateCottonAsk` (S) | `$askedAboutCotton = true 0` — `lppHookCurateCottonAsk` |
| `$askedAboutCoureuse` | CS / 14 | `gaFCFikenhildBotherAdministrator` (C) | `$askedAboutCoureuse = true` — `gaFCfikenhildBotherAdminAlready` |
| `$askedAboutCoureuseAgain` | CS / 2 | `gaFCArchonSearchHubOption` (C) | `$askedAboutCoureuseAgain = true 0` — `gaFCArchonAskCoureuseAgain` |
| `$askedAboutDiktatMerc` | CS / 2 | `raoAskDiktatMerc` (C) | `$askedAboutDiktatMerc = true` — `raoAskDiktatMerc2` |
| `$askedAboutFactions` | CS / 3 | `BFINconvOptFactions` (C) | `$askedAboutFactions = true 0` — `BFINaskConflicts` |
| `$askedAboutGargoyle` | CS / 2 | `gaKACoureuseGargoyleOpt` (C) | `$askedAboutGargoyle = true` — `gaKACoureuseGargoyleOptSel` |
| `$askedAboutJanusPrototype` | CS / 3 | `gaATGaskSCJanusPrototypeStart` (C) | `$askedAboutJanusPrototype = true` — `gaATGaskSCJanusPrototype1` |
| `$askedAboutJob` | CS / 2 | `shrineFleetConvAskAboutJob` (C) | `$askedAboutJob = true 0` — `shrineConvJobResponse` |
| `$askedAboutJoining` | CS / 2 | `plEnforcerJoinLeague` (C) | `$askedAboutJoining = true 0` — `plEnforcerJoinLeagueSel` |
| `$askedAboutKelise` | CS / 2 | `gaPZAskGargoyleKelise` (C) | `$askedAboutKelise = true 10` — `gaPZAskGargoyleKeliseSel` |
| `$askedAboutOMKIalready` | CS / 4 | `ZGRtechPlayerOMKIreturnB` (C) | `$askedAboutOMKIalready = true 0` — `ZGRtechPlayerHasOptionOMKI` |
| `$askedAboutOfferServices` | CS / 2 | `RHannanMsgFollowupOfferServices` (C) | `$askedAboutOfferServices = true` — `RHannanMsgFollowOfferServices` |
| `$askedAboutPayment` | S / 1 | `LOCRMaskPayment` (S) | `$askedAboutPayment = true` — `LOCRMaskPayment` |
| `$askedAboutPenance` | CS / 4 | `BFINconvOptD` (C) | `$askedAboutPenance = true 0` — `BFINhowPenance` |
| `$askedAboutRecentInspection` | CS / 2 | `plEnforcerAgain` (C) | `$askedAboutRecentInspection = true 0` — `plEnforcerAgainSel` |
| `$askedAboutScylla` | CS / 8 | `gaFCLaicailleCommanderBother` (C) | `$askedAboutScylla = true` — `gaFCLaicailleCommanderBother3` |
| `$askedAboutStarships` | CS / 6 | `pkSentinelRemoveQuestions` (C) | `$askedAboutStarships = true` — `PKSentinelAskAboutStarships` |
| `$askedAboutTheology1` | CS / 2 | `BFINconvOptDontConsider` (C) | `$askedAboutTheology1 = true 0` — `BFINdontConsider` |
| `$askedAboutYourProfit` | CS / 2 | `gaFCAskAroundKapteynBar4Option2` (C) | `$askedAboutYourProfit = true 0` — `gaFCAskAroundKapteynBarProfit` |
| `$askedAboutZal` | CS / 2 | `gaKACoureuseZalOpt` (C) | `$askedAboutZal = true` — `gaKACoureuseAskAbtZal1` |
| `$askedAboutZig` | CS / 2 | `gaPZAskGargoyleZig` (C) | `$askedAboutZig = true 10` — `paPZ_gargZigStory1` |
| `$askedAmbition` | CS / 2 | `sdtuMacrioIntroHubA5` (C) | `$askedAmbition = true 0` — `sdtuMacIntroAndradaAmbition` |
| `$askedAndradaKnow` | CS / 2 | `sdtuMacrioIntroHubA3` (C) | `$askedAndradaKnow = true 0` — `sdtuMacIntroAndradaKnow` |
| `$askedAnyQuestion` | CS / 8 | `ZGRthreatTechAskDone` (C) | `$askedAnyQuestion = true 0` — `ZGRthreatTechAskThreatDo1` |
| `$askedArePirates` | CS / 2 | `LOCRPhubAskArePirate` (C) | `$askedArePirates = true` — `LOCRPareYouPirates` |
| `$askedAreYouNexus` | CS / 3 | `PKHackStoryOptC` (C) | `$askedAreYouNexus = true 0` — `PKHackStoryAreYouNexus` |
| `$askedB` | CS / 2 | `gaATGdaudMeetingHubOptionB` (C) | `$askedB = true 0` — `gaATGdaudMeetingHubB1` |
| `$askedBaird` | CS / 2 | `lppEndingJaspisHub3` (C) | `$askedBaird = true 0` — `lppJaspisBaird1` |
| `$askedBarCotton` | CS / 2 | `gaATGAskEpiphanyBarPrompt` (C) | `$askedBarCotton = true` — `gaATGAskEpiphanyBarStart` |
| `$askedBarCoureuse` | CS / 6 | `gaFCAskAroundFikenhildBarStart` (C) | `$askedBarCoureuse = true` — `gaFCAskAroundFikenhildBar1` |
| `$askedBarTechnicians` | CS / 2 | `gaFCAskAroundLaicailleBarStartB` (C) | `$askedBarTechnicians = true` — `gaFCAskAroundLaicailleBar2` |
| `$askedBlessedService` | CS / 2 | `lppEndingJaspisHub5` (C) | `$askedBlessedService = true 0` — `lppJaspisBlessedService` |
| `$askedBornanew` | CS / 12 | `lkeJaspisStartHubOp6` (C) | `$askedBornanew = true 0` — `lkeJaspisStartBornanew` |
| `$askedC` | CS / 2 | `gaATGdaudMeetingHubOptionC` (C) | `$askedC = true 0` — `gaATGdaudMeetingHubC1` |
| `$askedChalcedon` | CS / 3 | `BFFIonBoardMenesOptYou` (C) | `$askedChalcedon = true 0` — `BFFIonBoardAskChalcedon` |
| `$askedClone` | CS / 3 | `gaATGmeetCottonTeaHubD` (C) | `$askedClone = true 0` — `gaATGmeetCottonClone` |
| `$askedCommission` | CS / 2 | `defaultRaoCommission` (C) | `$askedCommission = true 0` — `defaultRaoCommission2` |
| `$askedCotton` | CS / 5 | `lkeJaspisStartHubOp5` (C) | `$askedCotton = true 0` — `lkeJaspisStartCotton` |
| `$askedCotton2` | CS / 2 | `lkeVirensRaidOptionG2` (C) | `$askedCotton2 = true 0` — `LKEvirensRaidCotton2a` |
| `$askedCotton3` | C / 2 | `lkeVirensRaidOptionG2` (C) | Trace owning rule/command or generated interaction data |
| `$askedCotton4` | C / 1 | `lkeVirensRaidOptionG3` (C) | Trace owning rule/command or generated interaction data |
| `$askedCottonBook` | CS / 2 | `lppEndingJaspisHub9` (C) | `$askedCottonBook = true 0` — `lppJaspisCotton2` |
| `$askedCydonia` | CS / 2 | `gaATGmeetCottonTeaHubC` (C) | `$askedCydonia = true 0` — `gaATGmeetCottonCydonia2` |
| `$askedD` | CS / 3 | `gaATGdaudMeetingHubOptionD` (C) | `$askedD = true 0` — `gaATGdaudMeetingHubD1` |
| `$askedD2` | CS / 2 | `gaATGdaudMeetingHubOptionD2` (C) | `$askedD2 = true 0` — `gaATGdaudMeetingHubD2` |
| `$askedDardan` | CS / 3 | `LKEmazalotAskPortmasterDardan` (C) | `$askedDardan = true 30` — `LKEmazalotAskPMdardan` |
| `$askedDoYouEvenReadYourMessages` | CS / 4 | `RHannanMsgFollowup` (C) | `$askedDoYouEvenReadYourMessages = true` — `RHannanMsgDoYouEven` |
| `$askedDoYouHave` | CS / 2 | `gaKAGargoyle2DoYouHaveArchive` (C) | `$askedDoYouHave = true 0` — `gaKAGargoyle2DoYouHaveArchiveSel` |
| `$askedE` | CS / 2 | `gaATGdaudMeetingHubOptionE` (C) | `$askedE = true 0` — `gaATGdaudMeetingHubE1` |
| `$askedElekHowKnow` | CS / 3 | `ZGRpostGADHOelekHubOptB` (C) | `$askedElekHowKnow = true` — `ZGRpostGADHOelekHowKnow` |
| `$askedEndOfWorld` | CS / 2 | `PKPatherComOptZ` (C) | `$askedEndOfWorld = true 0` — `PKPatherAskEndOfWorld` |
| `$askedFamiliar` | CS / 2 | `postGAFCNewArchonC` (C) | `$askedFamiliar = true` — `postGAFCnewArchonAskFamiliar` |
| `$askedFeelings` | CS / 2 | `anhCantinaAskFeelings` (C) | `$askedFeelings = true 1` — `anhCantinaFeelings` |
| `$askedFenius` | CS / 7 | `LOCRPhubAskFenius` (C) | `$askedFenius = true` — `LOCRPwhoIsFenius` |
| `$askedForKanta` | CS / 2 | `kdPortmasterAskForKanta` (C) | `$askedForKanta = true 1` — `kdPortmasterAskForKanta` |
| `$askedForMe` | CS / 3 | `sdtuMacrioIntroHubG` (C) | `$askedForMe = true 0` — `sdtuMacIntroForMe` |
| `$askedForShrineDetails` | CS / 2 | `shrineFleetConvAskShrineDetails` (C) | `$askedForShrineDetails = true 0` — `shrineConvShrineDetailsResponse` |
| `$askedGalatiaIncident` | CS / 2 | `gaFCArchonSearchHubOption2` (C) | `$askedGalatiaIncident = true 0` — `gaFCArchonAskGalatia` |
| `$askedGazeOut` | CS / 3 | `sdtuMacrioIntroHubA2` (C) | `$askedGazeOut = true 0` — `sdtuMacIntroGazeOut` |
| `$askedHacked` | CS / 2 | `PKHackStoryOptE` (C) | `$askedHacked = true 0` — `PKHackStoryDidYouHack` |
| `$askedHammerFall` | CS / 3 | `lppEndingJaspisHub6` (C) | `$askedHammerFall = true 0` — `lppJaspisHammerFall` |
| `$askedHazard` | CS / 9 | `gaDHOendingAskRewardPostOptA` (C) | `$askedHazard = true 0` — `gaDHOendingAskRewardHazard` |
| `$askedHelpFight` | CS / 2 | `umbraWarbossHelpFightOption` (C) | `$askedHelpFight = true 0` — `umbraWarbossHelpFight1` |
| `$askedHisMission` | CS / 2 | `lkeJaspisStartHubOp3` (C) | `$askedHisMission = true 0` — `lkeJaspisStartHisMission` |
| `$askedHow` | CS / 2 | `extr_barStoryOptionHubC` (C) | `$askedHow = true 0` — `extr_barStoryOption3b` |
| `$askedHowKnow` | CS / 2 | `PKPatherComOptC` (C) | `$askedHowKnow = true 0` — `PKPatherAskHowInfo` |
| `$askedHowKnowBaird` | CS / 4 | `GAFCSiyavongFikenhildRevealOpt1` (C) | `$askedHowKnowBaird = true 0` — `GAFCSiyavongFikenhildSerious` |
| `$askedHowKnowName` | CS / 2 | `PKHackStoryOptA` (C) | `$askedHowKnowName = true 0` — `PKHackStoryHowName` |
| `$askedHowMany` | CS / 7 | `LOCRLhubAskHowManyOpt` (C) | `$askedHowMany = true` — `LOCRLaskHowMany` |
| `$askedIdolators` | CS / 2 | `BFFIidolTalkHubOptE` (C) | `$askedIdolators = true 0` — `BFFIidolTalkIdolators` |
| `$askedIfFamObjects` | CS / 2 | `ImoinuKatoUmbraOptObject` (C) | `$askedIfFamObjects = true` — `ImoinuKatoUmbraObject1` |
| `$askedIfPath` | CS / 2 | `lppVolturnCurateResponses2c` (C) | `$askedIfPath = true 0` — `lppVolturnCurateLuddicPath` |
| `$askedIfTerrorists` | CS / 3 | `anhCantinaAskTerroristA` (C) | `$askedIfTerrorists = true 1` — `anhCantinaKnowMore` |
| `$askedIneffablist` | CS / 3 | `BFFIidolTalkHubOptD` (C) | `$askedIneffablist = true 0` — `BFFIidolTalkIneffablistMore` |
| `$askedJaspis` | CS / 11 | `lppEndingJaspisHub8` (C) | `$askedJaspis++ 0` — `lppJaspisCotton2` |
| `$askedJethro` | CS / 9 | `lkeChalcedonBarSedgeOptA` (C) | `$askedJethro = true 0` — `lkeChalcedonBarSedgeRespA` |
| `$askedJobOffer` | CS / 2 | `lppEndingJaspisHub7` (C) | `$askedJobOffer = true 0` — `lppJaspisJobOffer` |
| `$askedJustTellingMe` | CS / 3 | `GAFCSiyavongFikenhildRevealOpt2` (C) | `$askedJustTellingMe = true 0` — `GAFCSiyavongFikenhildJustTellingMe` |
| `$askedKnight` | CS / 2 | `gaATGmeetCottonTeaHubZ` (C) | `$askedKnight = true 0` — `gaATGmeetCottonTeaKnight3` |
| `$askedKnights` | CS / 2 | `lppEndingJaspisHub2` (C) | `$askedKnights = true 0` — `lppJaspisKnights` |
| `$askedLastMetAndrada` | CS / 4 | `sdtuHyderTalk3OptionsH` (C) | `$askedLastMetAndrada = true 0` — `sdtuGyderTalk3BossTalkAndrada` |
| `$askedMairaath` | CS / 4 | `greetingsScavMairaathNeutral` (C) | `$askedMairaath = true 0` — `greetingsMairaathQ1A1` |
| `$askedMarlowe` | CS / 3 | `LOCRPhubAskMarlowe` (C) | `$askedMarlowe = true` — `LOCRPwhoIsMarlowe` |
| `$askedMenes` | CS / 2 | `BFFIonBoardMenesOpt1` (C) | `$askedMenes = true 0` — `BFFIonBoardAskMenes` |
| `$askedMining` | CS / 5 | `LOCRMhubOptWhatMining` (C) | `$askedMining = true` — `LOCRMsurveyOre` |
| `$askedMonsterAre` | CS / 2 | `ZGRmonsterTechAskMonsterAreOpt` (C) | `$askedMonsterAre = true` — `ZGRmonsterTechAskMonsterAre1` |
| `$askedMonsterDo` | CS / 2 | `ZGRmonsterTechAskMonsterDoOpt` (C) | `$askedMonsterDo = true` — `ZGRmonsterTechAskMonsterDo1` |
| `$askedMove` | CS / 3 | `gaATGhegFleetDialogHub3` (C) | `$askedMove = true 0` — `gaATGhegFleetMove1` |
| `$askedMundanian` | CS / 4 | `BFFIidolTalkHubOptB` (C) | `$askedMundanian = true 0` — `BFFIidolTalkMundanianMore` |
| `$askedNotDirectly` | CS / 2 | `gaATGIntroHub1option3` (C) | `$askedNotDirectly = true 0` — `gaATGIntroAsk3` |
| `$askedOlinadu` | CS / 2 | `BFFIonBoardMenesOpt2` (C) | `$askedOlinadu = true 0` — `BFFIonBoardAskInCharge` |
| `$askedOmega` | CS / 4 | `PKHackStoryOptOmega` (C) | `$askedOmega = true 0` — `PKHackStoryAreYouOmega` |
| `$askedOrElse` | CS / 4 | `gaVIPMercOption3` (C) | `$askedOrElse = true 0` — `gaVIPMercOption3Sel` |
| `$askedOtherPeople` | CS / 2 | `GAFCSiyavongFikenhildHubB3` (C) | `$askedOtherPeople = true 0` — `GAFCSiyavongFikenhildOtherPeople` |
| `$askedPath` | CS / 5 | `LKEmazalotAskQuartermaster2` (C) | `$askedPath = true` — `LKEmazalotAskQMPath` |
| `$askedPayment` | CS / 4 | `LOCRLhubAskPaymentOpt` (C) | `$askedPayment = true` — `LOCRLaskPaymentA` |
| `$askedPetitionAndrada` | CS / 2 | `defaultAndradaOfficeReplies2` (C) | `$askedPetitionAndrada = true 1` — `andradaOfficeSpeak2` |
| `$askedProtestInfo` | CS / 2 | `lppJangalaProtestRespInfo` (C) | `$askedProtestInfo = true 0` — `lppJangalaProtestInfo` |
| `$askedProtocols` | CS / 6 | `skironAskProtocols` (C) | `$askedProtocols = true` — `skironAskProtocols2` |
| `$askedProvostThrown` | CS / 2 | `gaATGpostGargoyleAsk` (C) | `$askedProvostThrown = true` — `gaATGpostGargoyleAsk3` |
| `$askedProvostUpset` | CS / 6 | `gaATGpostSebestyenProvost` (C) | `$askedProvostUpset = true` — `gaATGpostSebestyenGoodJob` |
| `$askedReadMind` | CS / 2 | `PKHackStoryOptF` (C) | `$askedReadMind = true 0` — `PKHackStoryCanYouRead` |
| `$askedRecognize` | CS / 2 | `extr_barStoryOptionHubB` (C) | `$askedRecognize = true 0` — `extr_barStoryOption3a` |
| `$askedRedemption` | CS / 2 | `lppExcubitorResponseRedemption` (C) | `$askedRedemption = true 0` — `lppExcubitorRedemptionCheckNo` |
| `$askedRelic` | CS / 4 | `BFFIonBoardMenesOpt3` (C) | `$askedRelic = true 0` — `BFFIonBoardAskRelic` |
| `$askedReynardC` | CS / 2 | `HYaribayAskReynardC` (C) | `$askedReynardC = true` — `HYaribayAskReynardRespC` |
| `$askedReynardD` | CS / 2 | `HYaribayAskReynardD` (C) | `$askedReynardD = true` — `HYaribayAskReynardRespD` |
| `$askedRobot` | CS / 2 | `anhCantinaAskRobot` (C) | `$askedRobot = true 1` — `anhCantinaRobot` |
| `$askedSayThink` | CS / 2 | `PKPatherComOptD` (C) | `$askedSayThink = true 0` — `PKPatherSayThink` |
| `$askedServeAndrada` | CS / 3 | `defaultAndradaOfficeReplies4` (C) | `$askedServeAndrada = true 1` — `andradaOfficeBook` |
| `$askedServeMe` | CS / 2 | `defaultAndradaOfficeReplies3` (C) | `$askedServeMe = true 1` — `andradaOfficeServe` |
| `$askedShrine` | CS / 2 | `lppGileadShrineStartHubResponse2` (C) | `$askedShrine = true 0` — `lppGileadShrineInfo1` |
| `$askedSkills` | CS / 3 | `ome_askSkills` (C) | `$askedSkills = true 0` — `ome_askSkillsSel` |
| `$askedSomething` | CS / 3 | `LKEmazalotAskQuartermaster3` (C) | `$askedSomething = true 0` — `LKEmazalotAskQMBornanew` |
| `$askedStayHere` | CS / 5 | `pkSentinelRemoveQuestions` (C) | `$askedStayHere = true` — `PKSentinelStayHere` |
| `$askedStipend` | CS / 2 | `gaRequestMeeting5c` (C) | `$askedStipend = true 0` — `gaMeetingStipend` |
| `$askedTalkAndrada` | CS / 2 | `defaultAndradaOfficeReplies1` (C) | `$askedTalkAndrada = true 1` — `andradaOfficeSpeak` |
| `$askedTea` | CS / 2 | `gaATGmeetCottonTeaHubA` (C) | `$askedTea = true 0` — `gaATGmeetCottonTeaWar` |
| `$askedThreatAIwar` | CS / 3 | `ZGRthreatTechAskThreatAIwarOpt` (C) | `$askedThreatAIwar = true` — `ZGRthreatTechAskThreatAI1` |
| `$askedThreatAlive` | CS / 2 | `ZGRthreatTechAskThreatAliveOpt` (C) | `$askedThreatAlive = true` — `ZGRthreatTechAskThreatAlive1` |
| `$askedThreatAre` | CS / 2 | `ZGRthreatTechAskThreatAreOpt` (C) | `$askedThreatAre = true` — `ZGRthreatTechAskThreatAre1` |
| `$askedThreatDo` | CS / 3 | `ZGRthreatTechAskThreatDoOpt` (C) | `$askedThreatDo = true` — `ZGRthreatTechAskThreatDo1` |
| `$askedToDiscussFuture` | CS / 3 | `RHannanMsgFollowupDiscussFuture` (C) | `$askedToDiscussFuture = true` — `RHannanMsgFollowDiscussFuture` |
| `$askedToDiscussFuture2` | CS / 2 | `RHannanMsgFollowupDiscussFuture2` (C) | `$askedToDiscussFuture2 = true` — `RHannanMsgFollowDiscussFuture2` |
| `$askedToMeet` | CS / 2 | `defaultDaudInquiry` (C) | `$askedToMeet = true 0` — `defaultDaudInquirySee` |
| `$askedTreasure` | CS / 2 | `LOCRPhubAskTreasure` (C) | `$askedTreasure = true` — `LOCRPaskTreasure` |
| `$askedUngodly` | CS / 3 | `gaATGmeetCottonTeaHubB` (C) | `$askedUngodly = true 0` — `gaATGmeetCottonUngodly` |
| `$askedVastFortune` | CS / 3 | `gaDHOvisitElekHubOptA` (C) | `$askedVastFortune = true` — `gaDHOvisitElekHubSelA` |
| `$askedVastFortune2` | CS / 2 | `gaDHOvisitElekHubOptA2` (C) | `$askedVastFortune2 = true` — `gaDHOvisitElekHubSelA2` |
| `$askedWhat` | CS / 2 | `gaATGhegFleetDialogHub2` (C) | `$askedWhat = true 0` — `gaATGhegFleetWhat1` |
| `$askedWhatAbout` | CS / 7 | `hegInvestigatorInspectQstrong` (C) | `$askedWhatAbout = true 0` — `hegInvestigatorQstrongSel` |
| `$askedWhatAuthority` | CS / 4 | `gaVIPMercOption2` (C) | `$askedWhatAuthority = true 0` — `gaVIPMercOption2Sel` |
| `$askedWhatCrime` | CS / 4 | `gaVIPMercOption1` (C) | `$askedWhatCrime = true 0` — `gaVIPMercOption1Sel` |
| `$askedWhatDo` | CS / 7 | `lppEndingJaspisHub1` (C) | `$askedWhatDo = true 0` — `lppJaspisWhatDo` |
| `$askedWhatDoYouDo` | CS / 3 | `GAFCSiyavongFikenhildHubAopt1` (C) | `$askedWhatDoYouDo = true 0` — `GAFCSiyavongFikenhildWhatDo` |
| `$askedWhatDoing` | CS / 7 | `LOCRLaskWhatDoingOpt` (C) | `$askedWhatDoing = true` — `LOCRLaskWhatDoing` |
| `$askedWhatGoal` | CS / 2 | `lppEndingJaspisHub1b` (C) | `$askedWhatGoal = true 0` — `lppJaspisWhatGoal` |
| `$askedWhatIf` | CS / 2 | `gaKAGargoyle2WhatIfIJustLeaveYou` (C) | `$askedWhatIf = true 0` — `gaKAGargoyle2WhatIfIJustLeaveYouSel` |
| `$askedWhatInfo` | CS / 2 | `gaFCIsirahMercHubOption1` (C) | `$askedWhatInfo = true` — `gaFCIsirahMercWhatInfo` |
| `$askedWhatLookingFor` | CS / 2 | `gaDHOvisitElekHubOptC` (C) | `$askedWhatLookingFor = true 0` — `gaDHOvisitElekHubSelC2c` |
| `$askedWhatPlan` | CS / 8 | `sdtuMacrioIntroHubG` (C) | `$askedWhatPlan = true 0` — `sdtuMacIntroWhatPlan` |
| `$askedWhereYouFrom` | CS / 5 | `pkSentinelRemoveQuestions` (C) | `$askedWhereYouFrom = true` — `PKSentinelAskWhereFromNotTrust` |
| `$askedWho` | CS / 2 | `gaKAGargoyle2WhoIsAfterYou` (C) | `$askedWho = true 0` — `gaKAGargoyle2WhoIsAfterYouSel` |
| `$askedWhoAreYou` | CS / 2 | `PKHackStoryOptG` (C) | `$askedWhoAreYou = true 0` — `PKHackStoryWhoAreYou` |
| `$askedWhoFriend` | CS / 2 | `gaDHOvisitElekHubOptB` (C) | `$askedWhoFriend = true 0` — `gaDHOvisitElekAskPatron` |
| `$askedWhoFriend2` | S / 1 | `gaDHOvisitElekAskPatron2a` (S) | `$askedWhoFriend2 = true 0` — `gaDHOvisitElekAskPatron2a` |
| `$askedWhoLeftYou` | CS / 2 | `LOCRMhubOptWhoLeftYou` (C) | `$askedWhoLeftYou = true` — `LOCRMaskWhoLeftYou` |
| `$askedWhoSold` | CS / 3 | `BFFImpBuyerHub2OptA` (C) | `$askedWhoSold = true 0` — `BFFImpBuyerWhoSold` |
| `$askedWhoSold2` | CS / 2 | `BFFImpBuyerHub2OptB` (C) | `$askedWhoSold2 = true 0` — `BFFImpBuyerWhoSold2` |
| `$askedWhoWorkingFor` | CS / 2 | `gaATGscavScanHub4` (C) | `$askedWhoWorkingFor = true 0` — `gaATGscavScanDialog4` |
| `$askedWhoYaribay` | CS / 4 | `gaATGIntroHub1option1` (C) | `$askedWhoYaribay = true 0` — `gaATGIntroAsk1` |
| `$askedWhy` | CS / 5 | `lppExcubitorResponseWhy` (C) | `$askedWhy = true 0` — `lppExcubitorWhyDenyFallback` |
| `$askedWhyHelp` | CS / 3 | `gaATGIntroHub1option2` (C) | `$askedWhyHelp = true 0` — `gaATGIntroAsk2` |
| `$askedWhyHere` | CS / 7 | `LOCRPhubAskWhyHere` (C) | `$askedWhyHere = true` — `LOCRPwhyAreYouHere` |
| `$askedWhyInvite` | CS / 2 | `LKEvirensConvOptB` (C) | `$askedWhyInvite = true 0` — `LKEvirensWhyInvite` |
| `$askedWhyKillSedge` | CS / 5 | `LKEvirensConvOptC` (C) | `$askedWhyKillSedge = true 0` — `LKEvirensPoliteSedgeMoreD` |
| `$askedWhyNot` | CS / 2 | `PKPatherComOptB` (C) | `$askedWhyNot = true 0` — `PKPatherAskWhyYouNot` |
| `$askedWhyNotMe` | CS / 2 | `gaATGIntroHub1optionPL` (C) | `$askedWhyNotMe = true 0` — `gaATGintroAskMePL` |
| `$askedWhySedgeAlly` | CS / 2 | `LKEvirensConvOptE` (C) | `$askedWhySedgeAlly = true 0` — `LKEvirensAskSedgeAlly` |
| `$askedWhySidearm` | CS / 2 | `LKEvirensConvOptC` (C) | `$askedWhySidearm = true 0` — `LKEvirensAskSidearm` |
| `$askedWhyStay` | CS / 2 | `gaATGscavScanHub22` (C) | `$askedWhyStay = true 0` — `gaATGscavScanDialog21` |
| `$askedWhyWant` | CS / 4 | `sdtuMacrioIntroHubG` (C) | `$askedWhyWant = true 0` — `sdtuMacIntroWhyHelp` |
| `$askedYouTraitor` | CS / 2 | `sdtuMacrioIntroHubA4` (C) | `$askedYouTraitor = true 0` — `sdtuMacIntroYouTraitor` |
| `$askedYoung` | CS / 2 | `postGAFCNewArchonB` (C) | `$askedYoung = true` — `postGAFCnewArchonAskYoung` |
| `$askedZalExplode` | CS / 2 | `gaATGMagecGateOptUse2` (C) | `$askedZalExplode = true 0` — `gaATGMagecGateExplode` |
| `$atPitchEnd` | CS / 5 | `ZGRtechPlayerOMKIreturnB` (C) | `$atPitchEnd = true 0` — `ZGRstartPitch12` |
| `$attackReversible` | C / 4 | `ttma_commandHail` (C) | `impl/campaign/rulecmd/HA_CMD.java` |
| `$avipt_didEnding` | CS / 2 | `AVIPTcheckCompletion` (C) | `$avipt_didEnding = true 0` — `AVIPTcheckCompletionCont` |
| `$avipt_marketName` | ST / 2 | `AVIPTtextBar2` (T) | `impl/campaign/missions/AngryVIPTransport.java` |
| `$avipt_noCompleteShown` | CS / 2 | `AVIPTcheckCompletion` (C) | `$avipt_noCompleteShown = true 0` — `AVIPTcompletedButNoDocking` |
| `$avipt_ref` | S / 2 | `AVIPTtextBar2` (S) | `impl/campaign/missions/AngryVIPTransport.java` |
| `$avipt_reward` | ST / 1 | `AVIPTtextBar2` (T) | `impl/campaign/missions/AngryVIPTransport.java` |
| `$avipt_timelimit` | ST / 1 | `AVIPTtextBar2` (T) | `impl/campaign/missions/AngryVIPTransport.java` |
| `$awkwardAskForCotton` | CS / 4 | `gaATGEpiphanySearch3` (C) | `$awkwardAskForCotton = true 3` — `gaATGEpiphanyAskLoke` |
| `$bairdIntro_archive` | CS / 3 | `bairdIntro_archiveCheckOutro2` (C) | `$bairdIntro_archive = true 0` — `bairdIntroArchive2` |
| `$bairdIntro_scylla` | CS / 4 | `bairdIntro_archiveCheckOutro1` (C) | `$bairdIntro_scylla = true 0` — `bairdIntroScylla2` |
| `$bairdIntro_whyMe` | CS / 3 | `bairdIntroHubB1` (C) | `$bairdIntro_whyMe = true 0` — `bairdIntroHubWhyMe` |
| `$bairdIntro_whyShould` | CS / 7 | `bairdIntroHubA1` (C) | `$bairdIntro_whyShould = true 0` — `bairdIntroHubWhyShould` |
| `$bairdIntro_willSee` | CS / 2 | `bairdIntroHubD` (C) | `$bairdIntro_willSee = true 0` — `bairdIntroHubWillSee` |
| `$bcb_TargetHeOrShe` | T / 5 | `CBPirateAristo` (T) | `impl/campaign/missions/cb/BaseCustomBounty.java` |
| `$bcb_days` | ST / 18 | `CBPirate` (T) | `impl/campaign/missions/cb/BaseCustomBounty.java` |
| `$bcb_dist` | ST / 19 | `CBPirate` (T) | `impl/campaign/missions/cb/BaseCustomBounty.java` |
| `$bcb_fleetName` | T / 1 | `CBEnemyStation` (T) | `impl/campaign/missions/cb/BaseCustomBounty.java` |
| `$bcb_patrolFaction` | ST / 1 | `CBPatrol` (T) | `impl/campaign/missions/cb/CBPatrol.java` |
| `$bcb_patrolFactionColor` | S / 1 | `CBPatrol` (S) | `impl/campaign/missions/cb/CBPatrol.java` |
| `$bcb_reward` | ST / 19 | `CBPirate` (T) | `impl/campaign/missions/cb/BaseCustomBounty.java` |
| `$bcb_systemName` | T / 19 | `CBPirate` (T) | `impl/campaign/missions/cb/BaseCustomBounty.java` |
| `$bcb_targetHeOrShe` | T / 4 | `CBDeserter` (T) | `impl/campaign/missions/cb/BaseCustomBounty.java` |
| `$bcb_targetHimOrHer` | T / 4 | `CBPather1` (T) | `impl/campaign/missions/cb/BaseCustomBounty.java` |
| `$bcb_targetMarketName` | T / 1 | `CBPatrol` (T) | `impl/campaign/missions/cb/BaseCustomBounty.java` |
| `$bcb_targetName` | T / 1 | `CBDeserter` (T) | `impl/campaign/missions/cb/BaseCustomBounty.java` |
| `$beingRepaired` | CS / 3 | `cTap_infoTextRepaired` (C) | `$beingRepaired = true 5` — `cTapRepairSel` |
| `$bffi_didAsk` | CS / 2 | `BFFIaskJaspisOpt` (C) | `$bffi_didAsk = true` — `BFFIaskJaspis1` |
| `$bffi_meetMenesYaribay` | C / 1 | `BFFImeetMenesStart` (C) | `impl/campaign/missions/luddic/BornanewFilesFalseIdols.java` |
| `$bffi_patherStationSystem` | OST / 7 | `BFFIulmusSedge8` (T) | `impl/campaign/missions/luddic/BornanewFilesFalseIdols.java` |
| `$bffi_patherStationTarget` | C / 2 | `BFFIstationApproach` (C) | `impl/campaign/missions/luddic/BornanewFilesFalseIdols.java` |
| `$bffi_stage` | C / 33 | `BFFItalkHorusParty` (C) | `impl/campaign/missions/luddic/BornanewFilesFalseIdols.java` |
| `$bffi_talkToHorusAboutMenesParty` | C / 1 | `BFFItalkHorusParty` (C) | `impl/campaign/missions/luddic/BornanewFilesFalseIdols.java` |
| `$boughtAPlanetkillerFromPlayer` | S / 3 | `pkTurnInArroyoSel` (S) | Trace owning rule/command or generated interaction data |
| `$brotherCottonSaid` | CS / 2 | `lkePatherFleetRespF` (C) | `$brotherCottonSaid = true 0` — `lkePatherFleetStartCotton` |
| `$brotherOrSister` | OT / 11 | `gaCOPatherConvince2` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$broughtUpBaird` | S / 1 | `hegTalkInspectDaudBaird1` (S) | `$broughtUpBaird = true 0` — `hegTalkInspectDaudBaird1` |
| `$broughtUpOMKI` | S / 1 | `ZGRpostGADHOelekPrefer3` (S) | `$broughtUpOMKI = true 0` — `ZGRpostGADHOelekPrefer3` |
| `$buyerArgsMade` | CS / 15 | `BFFImpBuyerHub3SumA` (C) | `$buyerArgsMade = 0` — `BFFImpBuyerNeedTested3` |
| `$cacheLie` | CS / 4 | `gaATGscavScanHub1` (C) | `$cacheLie = true 0` — `gaATGscavScanDialog2` |
| `$cacheLiePressIssue` | CS / 2 | `gaATGscavScanHub21` (C) | `$cacheLiePressIssue = true 0` — `gaATGscavScanDialog21` |
| `$cameToYouFirstWithPK` | S / 1 | `pkTurnInArroyoSel3` (S) | `$cameToYouFirstWithPK = true` — `pkTurnInArroyoSel3` |
| `$cameToYouSecondWithPK` | CS / 3 | `pk_turnInArroyoSel1again` (C) | `$cameToYouSecondWithPK = true` — `pkTurnInArroyoSel4` |
| `$canAfford` | CS / 2 | `cTapDisableRepair` (C) | `$canAfford = true 0` — `cTapCheckCanAfford` |
| `$canConfront` | CS / 13 | `PKSentinelHubConfrontCheckE` (C) | `$canConfront = false 0` — `PKSentinelHubConfrontCheck0` |
| `$canLeave` | CS / 5 | `lppVolturnCurateResponses2i` (C) | `$canLeave = true 0` — `lppVolturnCurateTroublingShrine` |
| `$canNotSalvage` | CS / 4 | `sal_showRatingAndCostUnable` (C) | `$canNotSalvage = true 0` — `gaPZ_hamatsuWreckOpen` |
| `$canSayGA` | CS / 4 | `shroudedSubstrateOptGA` (C) | `$canSayGA = true 0` — `shroudedSubstrateOptSelA` |
| `$canUnlock` | C / 2 | `pods_break` (C) | `impl/campaign/CargoPodsEntityPlugin.java`, `impl/campaign/rulecmd/salvage/CargoPods.java` (more mentions) |
| `$cargoScanConv` | S / 1 | `cargoScanInitial` (S) | `$cargoScanConv = true 0` — `cargoScanInitial` |
| `$cargoScan_didAlready` | CS / 1 | `cargoScanInitial` (C) | `$cargoScan_didAlready = true 0` — `cargoScanInitial` |
| `$chargeMoreTerror` | CS / 5 | `anhCantinaAskTerroristA` (C) | `$chargeMoreTerror = true 1` — `anhCantinaChargeMore` |
| `$cheapCom_barEvent` | C / 4 | `cheapComTextLocal` (C) | `impl/campaign/missions/CheapCommodityMission.java` |
| `$cheapCom_commodityId` | S / 5 | `cheapComOfferTextLocalBar` (S) | `impl/campaign/missions/CheapCommodityMission.java` |
| `$cheapCom_commodityName` | OT / 7 | `cheapComBlurb` (T) | `impl/campaign/missions/CheapCommodityMission.java` |
| `$cheapCom_completed` | S / 1 | `cheapComPickupOptionSel` (S) | `$cheapCom_completed = true` — `cheapComPickupOptionSel` |
| `$cheapCom_dist` | ST / 2 | `cheapComTextRemote` (T) | `impl/campaign/missions/CheapCommodityMission.java` |
| `$cheapCom_hasCommodity` | C / 1 | `cheapComPickupGreeting` (C) | `impl/campaign/missions/CheapCommodityMission.java` |
| `$cheapCom_manOrWoman` | OT / 2 | `cheapComBlurbBar` (T) | `impl/campaign/missions/CheapCommodityMission.java` |
| `$cheapCom_marketName` | ST / 2 | `cheapComTextRemote` (T) | `impl/campaign/missions/CheapCommodityMission.java` |
| `$cheapCom_marketOnOrAt` | T / 2 | `cheapComTextRemote` (T) | `impl/campaign/missions/CheapCommodityMission.java` |
| `$cheapCom_personName` | T / 2 | `cheapComTextRemote` (T) | `impl/campaign/missions/CheapCommodityMission.java` |
| `$cheapCom_personPost` | T / 2 | `cheapComTextRemote` (T) | `impl/campaign/missions/CheapCommodityMission.java` |
| `$cheapCom_pricePerUnit` | ST / 5 | `cheapComTextLocal` (T) | `impl/campaign/missions/CheapCommodityMission.java` |
| `$cheapCom_quantity` | ST / 7 | `cheapComTextLocal` (T) | `impl/campaign/missions/CheapCommodityMission.java` |
| `$cheapCom_ref` | CS / 4 | `cheapComPickupGreeting` (C) | `impl/campaign/missions/CheapCommodityMission.java` |
| `$cheapCom_ref2` | S / 1 | `cheapComPostAccept` (S) | `impl/campaign/missions/CheapCommodityMission.java` |
| `$cheapCom_totalPrice` | ST / 7 | `cheapComTextRemote` (T) | `impl/campaign/missions/CheapCommodityMission.java` |
| `$cheapCom_variation` | C / 9 | `cheapComOfferTextLocalBar` (C) | `impl/campaign/missions/CheapCommodityMission.java` |
| `$churchThink` | CS / 2 | `lppVolturnCurateResponses2d` (C) | `$churchThink = true 0` — `lppVolturnCurateChurchThink` |
| `$cloneQuestion` | CS / 4 | `gaATGmeetCottonTeaHubB2` (C) | `$cloneQuestion = true 0` — `gaATGmeetCottonUngodly2` |
| `$cob_action` | CS / 30 | `cob_confirmOptsBurstNo` (C) | `$cob_action = burst 0` — `cob_neutrinoBurstSel` |
| `$cob_didBurst` | CS / 2 | `cob_neutrinoBurstOptDisable` (C) | `$cob_didBurst = true 30` — `cob_neutrinoBurstDescPre` |
| `$cob_hacked` | C / 5 | `cob_snifferOpt` (C) | `impl/campaign/BaseCampaignObjectivePlugin.java`, `impl/campaign/rulecmd/salvage/Objectives.java` |
| `$cob_reset` | C / 1 | `cob_alreadyReset` (C) | `impl/campaign/BaseCampaignObjectivePlugin.java` |
| `$commentedOnWeapons` | CS / 5 | `lppVolturnCurateResponses2a` (C) | `$commentedOnWeapons = true 0` — `lppVolturnCurateTroublingShrine` |
| `$committedToChurchTransport` | CS / 4 | `LOCRLhubOfferTransportOpt` (C) | `$committedToChurchTransport = true` — `LOCRLsayChurch` |
| `$committedToPathTransport` | CS / 4 | `LOCRLsayLuddicOpt` (C) | `$committedToPathTransport = true` — `LOCRLsayPatherYes` |
| `$confirmedInsurgent` | CS / 4 | `lppVolturnCurateResponses2a` (C) | `$confirmedInsurgent = true 0` — `lppVolturnCurateTroublingHeavier` |
| `$confrontedAboutAIFleet` | CS / 12 | `PKSentinelHubConfrontCheckB` (C) | `$confrontedAboutAIFleet = true` — `PKSentinelConfrontAIFleet` |
| `$confrontedAboutAIUse` | CS / 5 | `PKSentinelHubConfrontCheckD` (C) | `$confrontedAboutAIUse = true` — `PKSentinelConfrontFoundFleetAI` |
| `$confrontedAboutGantry` | CS / 6 | `PKSentinelHubConfrontCheckA` (C) | `$confrontedAboutGantry = true` — `PKSentinelConfrontGantry` |
| `$confrontedAboutPK` | CS / 6 | `PKSentinelHubConfrontCheckC` (C) | `$confrontedAboutPK = true` — `PKSentinelConfrontPK` |
| `$confrontsDone` | CS / 13 | `gaFCArchonPLexcuseOptDone` (C) | `$confrontsDone++ 0` — `gaFCArchonPLtellPathers` |
| `$contact_printedFirstReturnGreeting` | CS / 37 | `convDefaultGreeting` (C) | `$contact_printedFirstReturnGreeting = true 0` — `convDefaultGreeting` |
| `$contacted` | CS / 5 | `LOCRMcontact1again` (C) | `$contacted = true` — `LOCRMcontact2` |
| `$contactedPlayerRecently` | CS / 2 | `playerOptionsVSContactingNPC` (C) | `$contactedPlayerRecently = true 2` — `playerOptionsVSContactingNPC` |
| `$core_lampGlowColor` | S / 6 | `fusionLampOrange` (S) | `impl/campaign/FusionLampEntityPlugin.java` |
| `$core_lampLightColor` | S / 6 | `fusionLampOrange` (S) | `impl/campaign/FusionLampEntityPlugin.java` |
| `$core_pkCache` | C / 4 | `pkCacheDefenderPreEmpt` (C) | `impl/campaign/procgen/themes/MiscellaneousThemeGenerator.java` |
| `$core_pkPlanet` | C / 2 | `PKSentinelInteraction` (C) | `impl/campaign/procgen/themes/MiscellaneousThemeGenerator.java` |
| `$cottonOption` | CS / 7 | `gaPZ_cottonCont3A` (C) | `$cottonOption = warning 0` — `gaPZ_cottonKnight` |
| `$cpc_armsDealer` | C / 6 | `cpcBlurb4` (C) | `impl/campaign/missions/CustomProductionContract.java` |
| `$cpc_barEvent` | C / 1 | `cpcBPPickedBar` (C) | `impl/campaign/missions/CustomProductionContract.java` |
| `$cpc_costPercent` | ST / 7 | `cpcOfferTextContact1` (T) | `impl/campaign/missions/CustomProductionContract.java` |
| `$cpc_days` | ST / 7 | `cpcOfferTextContact2` (T) | `impl/campaign/missions/CustomProductionContract.java` |
| `$cpc_manOrWoman` | OT / 5 | `cpcBlurbBar1` (T) | `impl/campaign/missions/CustomProductionContract.java` |
| `$cpc_maxCapacity` | ST / 7 | `cpcOfferTextContact1` (T) | `impl/campaign/missions/CustomProductionContract.java` |
| `$cpc_military` | C / 6 | `cpcBlurb2` (C) | `impl/campaign/missions/CustomProductionContract.java` |
| `$cpc_ref` | S / 4 | `cpcPickPlayerBP` (S) | `impl/campaign/missions/CustomProductionContract.java` |
| `$cpc_trade` | C / 6 | `cpcBlurb1` (C) | `impl/campaign/missions/CustomProductionContract.java` |
| `$cpm_commodityName` | OT / 9 | `cpmBlurb` (T) | `impl/campaign/missions/CommodityProductionMission.java` |
| `$cpm_contractCycles` | ST / 4 | `cpmOfferTextBar2` (T) | `impl/campaign/missions/CommodityProductionMission.java` |
| `$cpm_manOrWoman` | OT / 8 | `cpmBlurbBar` (T) | `impl/campaign/missions/CommodityProductionMission.java` |
| `$cpm_missionCycles` | ST / 4 | `cpmOfferTextBar2` (T) | `impl/campaign/missions/CommodityProductionMission.java` |
| `$cpm_monthlyPayment` | ST / 4 | `cpmOfferTextBar2` (T) | `impl/campaign/missions/CommodityProductionMission.java` |
| `$cpm_needed` | ST / 4 | `cpmOfferTextBar2` (T) | `impl/campaign/missions/CommodityProductionMission.java` |
| `$cpm_totalPayment` | ST / 4 | `cpmOfferTextBar2` (T) | `impl/campaign/missions/CommodityProductionMission.java` |
| `$cpm_underworld` | C / 7 | `cpmBlurbUW` (C) | `impl/campaign/missions/CommodityProductionMission.java` |
| `$credits` | S / 2 | `gaATGzalMissionCreditsTake` (S) | `$credits = true 0` — `gaATGzalMissionCreditsTake` |
| `$creditsHelp` | CS / 2 | `gaATGzalMissionStartHub2b` (C) | `$creditsHelp = true 0` — `gaATGzalMissionCredits` |
| `$crewReq` | CS / 2 | `cTapCheckCanAfford` (C) | `$crewReq = 1000 0` — `cTap_infoText` |
| `$customType` | C / 53 | `abyssalLightBegin` (C) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$customsInspectionStage` | S / 2 | `customsInspectionScan` (S) | `$customsInspectionStage = 0 4` — `customsInspectionScan` |
| `$cutCommLinkPolite` | CS / 9 | `convOptionLeave` (C) | `$cutCommLinkPolite = true 0` — `dstr_aidOptions` |
| `$daedaleon` | C / 1 | `beaconOpenDialogDaedaleon` (C) | Trace owning rule/command or generated interaction data |
| `$damagedStation` | C / 1 | `remnantStationFleetOpenDamaged` (C) | `impl/campaign/intel/events/RemnantHostileActivityFactor.java`, `impl/campaign/intel/events/RemnantNexusActivityCause.java` (more mentions) |
| `$daysLeft` | CS / 2 | `pods_stabilizeDisableDidAlready` (C) | `impl/campaign/rulecmd/missions/Commission.java`, `impl/campaign/rulecmd/salvage/CargoPods.java` |
| `$dcom_addedRaidObjective` | CS / 1 | `dcomOpenMarket` (C) | `$dcom_addedRaidObjective = true 0` — `dcomOpenMarket` |
| `$dcom_danger` | S / 1 | `dcomOpenMarket` (S) | `impl/campaign/missions/DisruptCompetitorMission.java` |
| `$dcom_factionColor` | S / 2 | `dcomOfferTextBar2` (S) | `impl/campaign/missions/DisruptCompetitorMission.java` |
| `$dcom_manOrWoman` | OT / 2 | `dcomBlurbBar` (T) | `impl/campaign/missions/DisruptCompetitorMission.java` |
| `$dcom_marines` | ST / 2 | `dcomOfferTextBar2` (T) | `impl/campaign/missions/DisruptCompetitorMission.java` |
| `$dcom_marketFaction` | ST / 2 | `dcomOfferTextBar2` (T) | `impl/campaign/missions/DisruptCompetitorMission.java` |
| `$dcom_marketFactionArticle` | T / 2 | `dcomOfferTextBar2` (T) | `impl/campaign/missions/DisruptCompetitorMission.java` |
| `$dcom_marketName` | ST / 3 | `dcomOfferTextBar2` (T) | `impl/campaign/missions/DisruptCompetitorMission.java` |
| `$dcom_marketOnOrAt` | T / 3 | `dcomOfferTextBar2` (T) | `impl/campaign/missions/DisruptCompetitorMission.java` |
| `$dcom_ref` | S / 2 | `dcomOfferTextBar2` (S) | `impl/campaign/missions/DisruptCompetitorMission.java` |
| `$dcom_reward` | ST / 2 | `dcomOfferTextBar2` (T) | `impl/campaign/missions/DisruptCompetitorMission.java` |
| `$ddro_aOrAnThing` | T / 2 | `ddroOfferTextBar1` (T) | `impl/campaign/missions/DeadDropMission.java` |
| `$ddro_completed` | S / 1 | `ddroObjectScan` (S) | `$ddro_completed = true` — `ddroObjectScan` |
| `$ddro_dist` | ST / 2 | `ddroOfferTextBar1` (T) | `impl/campaign/missions/DeadDropMission.java` |
| `$ddro_manOrWoman` | OT / 3 | `ddroBlurbBar1` (T) | `impl/campaign/missions/DeadDropMission.java` |
| `$ddro_personName` | T / 1 | `ddroObjectInteraction` (T) | `impl/campaign/missions/DeadDropMission.java` |
| `$ddro_ref` | CS / 5 | `ddroPrintHostilesText` (C) | `impl/campaign/missions/DeadDropMission.java` |
| `$ddro_reward` | ST / 2 | `ddroOfferTextBar1` (T) | `impl/campaign/missions/DeadDropMission.java` |
| `$ddro_systemName` | ST / 2 | `ddroOfferTextBar1` (T) | `impl/campaign/missions/DeadDropMission.java` |
| `$ddro_target` | C / 2 | `ddroPrintHostilesText` (C) | `impl/campaign/missions/DeadDropMission.java` |
| `$ddro_thing` | T / 1 | `ddroObjectInteraction` (T) | `impl/campaign/missions/DeadDropMission.java` |
| `$dealArgumentsMade` | CS / 7 | `PLCheckArgumentsEnoughA` (C) | `$dealArgumentsMade = 0 0` — `rh_betterDealSel` |
| `$defenderFleetDefeated` | C / 9 | `salSMOLstart` (C) | `impl/campaign/intel/misc/HypershuntIntel.java`, `impl/campaign/rulecmd/PK_CMD.java` (more mentions) |
| `$delivery_noCompleteShown` | CS / 1 | `delivery_completedButNoDocking` (C) | `$delivery_noCompleteShown = true 0` — `delivery_completedButNoDocking` |
| `$demandedBornanew` | CS / 4 | `lkeChalcedonVIPoptsHostile1` (C) | `$demandedBornanew = true 0` — `LKEchalVIPdemandBornanew` |
| `$demandedExecutor` | CS / 5 | `PKGiveToDiktat` (C) | `$demandedExecutor = true` — `pk_executorInsistSel2` |
| `$demandedRecruiter` | CS / 3 | `lkeChalcedonVIPoptsHostile2` (C) | `$demandedRecruiter = true 0` — `LKEchalVIPdemandRecruiter` |
| `$demandingSupplies` | CS / 7 | `lcSacredProtectorsGiveSupplies` (C) | `$demandingSupplies = true 0` — `lcSacredProtectorsComms` |
| `$deniedRequestToTalkToReynard` | S / 2 | `PLArmadaIdealisticNo` (S) | Trace owning rule/command or generated interaction data |
| `$deploying` | C / 1 | `gateHaulerStart4` (C) | `impl/campaign/intel/misc/GateHaulerIntel.java` |
| `$dhi_disruptDays` | ST / 3 | `dhiOfferTextBar` (T) | Trace owning rule/command or generated interaction data |
| `$dhi_dist` | ST / 3 | `dhiOfferTextBar` (T) | Trace owning rule/command or generated interaction data |
| `$dhi_industry` | OT / 5 | `dhiBlurb` (T) | Trace owning rule/command or generated interaction data |
| `$dhi_marines` | ST / 3 | `dhiOfferTextBar` (T) | Trace owning rule/command or generated interaction data |
| `$dhi_marketName` | OST / 5 | `dhiBlurb` (T) | Trace owning rule/command or generated interaction data |
| `$dhi_marketOnOrAt` | OT / 5 | `dhiBlurb` (T) | Trace owning rule/command or generated interaction data |
| `$dhi_ref` | S / 3 | `dhiOfferTextBar` (S) | Trace owning rule/command or generated interaction data |
| `$dhi_reward` | ST / 3 | `dhiOfferTextBar` (T) | Trace owning rule/command or generated interaction data |
| `$did1` | CS / 2 | `gaATGluddicScanHub1` (C) | `$did1 = true 0` — `gaATGluddicScan1` |
| `$did2` | CS / 3 | `gaATGluddicScanHub2` (C) | `$did2 = true 0` — `gaATGluddicScan2` |
| `$did21` | CS / 3 | `gaATGluddicScanHub21` (C) | `$did21 = true 0` — `gaATGluddicScan21` |
| `$did22` | CS / 3 | `gaATGluddicScanHub22` (C) | `$did22 = true 0` — `gaATGluddicScan22` |
| `$didAcceptSass` | CS / 3 | `lppVolturnCurateResponses2SDc` (C) | `$didAcceptSass = true 0` — `lppVolturnCurateSDaccepted` |
| `$didAgreement` | CS / 2 | `BFFIpsRuseNiceFopt` (C) | `$didAgreement = true` — `BFFIpsRuseNiceF` |
| `$didAndThen` | CS / 2 | `gaATGMagecGateOptZalScan2` (C) | `$didAndThen = true 0` — `gaATGMagecGateAndThen` |
| `$didAngelsTalkB` | CS / 2 | `LKEjethroAngelsTalkOut2` (C) | `$didAngelsTalkB = true 0` — `LKEjethroAngelsTalkB2c` |
| `$didApproach` | CS / 1 | `BFFIstationApproach` (C) | `$didApproach = true` — `BFFIstationApproach` |
| `$didArroyo` | CS / 1 | `ZGRacknowledgeArroyo` (C) | `$didArroyo = true 0` — `ZGRacknowledgeArroyo` |
| `$didAsale` | CS / 8 | `ZGRstartPitch12didSale` (C) | `$didAsale = true 0` — `ZGRmakePitch9OMKIbuy4mil2` |
| `$didAskCredits` | CS / 3 | `BFINconvOptRemindReward` (C) | `$didAskCredits = true 0` — `BFINaskCredits` |
| `$didAskDemarchon` | CS / 2 | `gaFCarchonAskDemarchonAgain` (C) | `$didAskDemarchon = true` — `gaFCarchonAskDemarchon` |
| `$didBlessOption` | CS / 3 | `lppVolturnCurateResponses1c` (C) | `$didBlessOption = true 0` — `lppVolturnCurateAskBlessing` |
| `$didBluffResponse` | CS / 2 | `kpAudience1Bluffs` (C) | `$didBluffResponse = true` — `kpAudience1Bluffs2` |
| `$didBribe` | CS / 3 | `BFFIpsRuseNiceAopt` (C) | `$didBribe = true` — `BFFIpsRuseNiceA` |
| `$didChurch` | CS / 5 | `lppVolturnCurateResponses2SDd2` (C) | `$didChurch = true 0` — `lppVolturnCurateSDcriticism2` |
| `$didColony` | CS / 1 | `ZGRacknowledgeColony` (C) | `$didColony = true 0` — `ZGRacknowledgeColony` |
| `$didCommissionStep` | CS / 1 | `rh_handleCommission` (C) | `$didCommissionStep = true 0` — `rh_handleCommission` |
| `$didCompromise` | CS / 2 | `lppVolturnCurateResponses2SDe` (C) | `$didCompromise = true 0` — `lppVolturnCurateSDcompromise` |
| `$didCriticism` | CS / 4 | `lppVolturnCurateResponses2SDb` (C) | `$didCriticism = true 0` — `lppVolturnCurateSDcriticism` |
| `$didCryptokeyFollowUp` | CS / 3 | `YaribayFollowup` (C) | `$didCryptokeyFollowUp = true` — `YaribayFollowup1` |
| `$didDHOhook2aside` | CS / 1 | `gaDHOhook2aside` (C) | `$didDHOhook2aside = true 0` — `gaDHOhook2aside` |
| `$didDescription` | CS / 3 | `gsVambOptHubDescriptionA` (C) | `$didDescription = true 0` — `gsVambOptHubDescriptionA` |
| `$didDiktat` | CS / 2 | `ZGRacknowledgeDiktat` (C) | `$didDiktat = true 0` — `ZGRacknowledgeDiktat` |
| `$didDoubleTake` | CS / 2 | `lppJangalaProtestMarinesHelpHegDT` (C) | `$didDoubleTake = true 0` — `lppJangalaProtestMarinesHelpHegDT` |
| `$didEnemy` | CS / 3 | `BFFIpsRuseMeanDopt` (C) | `$didEnemy = true` — `BFFIpsRuseMeanD` |
| `$didEntail` | CS / 3 | `TTpatrolWeirdMods5optB` (C) | `$didEntail = true` — `TTpatrolWeirdMods5b` |
| `$didExplanation` | CS / 5 | `gaDHOvisitElekAltWhatIsItReroute` (C) | `$didExplanation = true` — `gaDHOvisitElekAltWhatIsIt2` |
| `$didFakeAsk` | CS / 4 | `BFFItalkToEngineer11c` (C) | `$didFakeAsk = true 0` — `BFFItalkToEngineer11a` |
| `$didFastOneGreeting` | CS / 2 | `raoGreetingFastOne` (C) | `$didFastOneGreeting = true` — `raoFastOneSpeak` |
| `$didFirstSale` | CS / 5 | `ZGRtechTurnInResponseDefault` (C) | `$didFirstSale = true` — `ZGRtechTurnInResponseFirst` |
| `$didFirstSaleMonster` | CS / 2 | `ZGRtechTurnInResponseMonsterAgain` (C) | `$didFirstSaleMonster = true` — `ZGRtechTurnInResponseMonster` |
| `$didFirstSaleThreat` | CS / 2 | `ZGGtechTurnInResponseThreatAgain` (C) | `$didFirstSaleThreat = true` — `ZGRtechTurnInResponseThreat` |
| `$didFleetThreat` | CS / 2 | `BFFIpsRuseMeanAopt` (C) | `$didFleetThreat = true` — `BFFIpsRuseMeanA` |
| `$didGA1` | CS / 1 | `ZGRacknowledgeGA1` (C) | `$didGA1 = true` — `ZGRacknowledgeGA1` |
| `$didGA2` | CS / 1 | `ZGRacknowledgeGA2` (C) | `$didGA2 = true` — `ZGRacknowledgeGA2` |
| `$didGADHOinterrupt` | CS / 3 | `gaDHO_mk1explore8interruptA` (C) | `$didGADHOinterrupt = true 0` — `gaDHO_mk1explore8interruptOut` |
| `$didGADHOtalk` | CS / 5 | `ZGRpostGADHOconfrontOpt` (C) | `$didGADHOtalk = true` — `ZGRpostGADHOimply2` |
| `$didGAFCarchonPLdislike` | CS / 2 | `gaFCarchonAskDemarchonAgainNo` (C) | `$didGAFCarchonPLdislike = true` — `gaFCarchonPLdislike` |
| `$didGAFCarchonPLlike` | CS / 1 | `gaFCarchonPLlike` (C) | `$didGAFCarchonPLlike = true` — `gaFCarchonPLlike` |
| `$didGreeting` | CS / 2 | `LKEmazalotAskPortmasterPath` (C) | `$didGreeting = true 0` — `LKEmazalotAskPortmasterPath` |
| `$didHeg` | CS / 1 | `ZGRacknowledgeHegemony` (C) | `$didHeg = true 0` — `ZGRacknowledgeHegemony` |
| `$didHistory` | CS / 2 | `gaATGbairdEndingHubOptC` (C) | `$didHistory = true 0` — `gaATGbairdEndingHistory` |
| `$didHolorecCheck` | CS / 2 | `sdtuRamSafehouseReaction2` (C) | `$didHolorecCheck = true 0` — `sdtuRamSafehouseReactionC` |
| `$didHumanitarian` | CS / 4 | `BFFIpsRuseNiceDoptCheck` (C) | `$didHumanitarian = true` — `BFFIpsRuseNiceE` |
| `$didHumble` | CS / 4 | `lppVolturnCurateResponses2SDa` (C) | `$didHumble = true 0` — `lppVolturnCurateSDhumble` |
| `$didInjuredTalk` | CS / 2 | `BFFIidolTalkInjuredMomentA` (C) | `$didInjuredTalk = true 0` — `BFFIidolTalkInjuredMomentA` |
| `$didIntro` | CS / 13 | `LKEjethroOptionA` (C) | `$didIntro = true 0` — `LKEjethroA` |
| `$didKnownAsk` | CS / 4 | `BFFItalkToEngineer11c` (C) | `$didKnownAsk = true 0` — `BFFItalkToEngineer11b` |
| `$didLOCRLFmissionAcknowledgement` | CS / 1 | `LOCRLFmissionFound` (C) | `$didLOCRLFmissionAcknowledgement = true` — `LOCRLFmissionFound` |
| `$didLOCRLdescription` | CS / 2 | `LOCRLstartAgain` (C) | `$didLOCRLdescription = true 0` — `LOCRLstartAgain` |
| `$didLeague` | CS / 4 | `ZGRacknowledgeLeague` (C) | `$didLeague = true 0` — `ZGRacknowledgeLeague` |
| `$didLeaveCrewInterrupt` | CS / 1 | `PKSentinelOutSequenceInterrupt` (C) | `$didLeaveCrewInterrupt = true` — `PKSentinelOutSequenceInterrupt` |
| `$didLose` | CS / 3 | `LKEjethroOptionB` (C) | `$didLose = true 0` — `LKEjethroB` |
| `$didLuddicCorrection` | CS / 3 | `LOCRPhubLuddicCorrection` (C) | `$didLuddicCorrection = true` — `LOCRPnotLuddies` |
| `$didLuddicCorrectionFollowup` | CS / 1 | `LOCRPofferWorkLuddic` (C) | `$didLuddicCorrectionFollowup = true` — `LOCRPofferWorkLuddic` |
| `$didLuddicFaithful` | CS / 2 | `BFFIpsRuseNiceBopt` (C) | `$didLuddicFaithful = true` — `BFFIpsRuseNiceB` |
| `$didLuddicPather` | CS / 2 | `BFFIpsRuseNiceCopt` (C) | `$didLuddicPather = true` — `BFFIpsRuseNiceC` |
| `$didMenesRevenge` | CS / 1 | `MenesPettyRevenge` (C) | `$didMenesRevenge = true 3` — `MenesPettyRevenge` |
| `$didMerc` | CS / 2 | `BFFIpsRuseMeanEopt` (C) | `$didMerc = true` — `BFFIpsRuseMeanE` |
| `$didMercy` | CS / 3 | `BFFIpsRuseNiceDopt` (C) | `$didMercy = true` — `BFFIpsRuseNiceD` |
| `$didNameCallOut` | CS / 2 | `BFFIidolTalkNameCallingOut2` (C) | `$didNameCallOut = true 0` — `BFFIidolTalkNameCallingOut` |
| `$didNotSit` | CS / 3 | `LKEvirensConvOptF` (C) | `$didNotSit = true 0` — `LKEvirensStartB` |
| `$didOptionA` | CS / 2 | `gaATGcontactYaribayHubA` (C) | `$didOptionA = true` — `gaATGcontactYaribayA1` |
| `$didOptionB` | CS / 2 | `gaATGcontactYaribayHubB` (C) | `$didOptionB = true` — `gaATGcontactYaribayB1` |
| `$didOptionC` | CS / 2 | `gaATGcontactYaribayHubC` (C) | `$didOptionC = true` — `gaATGcontactYaribayC1` |
| `$didPaid` | CS / 2 | `gaATGbairdEndingHubOptE` (C) | `$didPaid = true 0` — `gaATGbairdEndingPay` |
| `$didPaidYou` | S / 1 | `BFINpaidYou` (S) | `$didPaidYou = true 0` — `BFINpaidYou` |
| `$didPayment` | CS / 2 | `sdtuNewsEndOptionsPay` (C) | `$didPayment = true 0` — `sdtuNewsEndSale` |
| `$didPostPartyUlmusPondTalk` | CS / 4 | `BFFImenesPostPartyTalk` (C) | `$didPostPartyUlmusPondTalk = true` — `BFFImcHubResponseA1` |
| `$didPrisonOffer` | CS / 3 | `LOCRPhubOfferToPrison` (C) | `$didPrisonOffer = true` — `LOCRPofferToPrison` |
| `$didReap` | CS / 2 | `gaATGbairdEndingHubOptD` (C) | `$didReap = true 0` — `gaATGbairdEndingReap` |
| `$didResign` | CS / 1 | `rh_handleCommissionNoOther` (C) | `$didResign = true 0` — `rh_handleCommissionNoOther` |
| `$didReturnEver` | CS / 10 | `ZGRtechReturnOptAgain` (C) | `$didReturnEver = true` — `ZGRpostWeirdProceed` |
| `$didReward` | CS / 3 | `TTpatrolWeirdMods5optA` (C) | `$didReward = true` — `TTpatrolWeirdMods5a` |
| `$didRuthless` | CS / 3 | `BFFIpsRuseMeanCopt` (C) | `$didRuthless = true` — `BFFIpsRuseMeanC` |
| `$didSedgeKill` | CS / 4 | `BFFIpsRuseMeanBopt` (C) | `$didSedgeKill = true` — `BFFIpsRuseMeanBsaidKilled` |
| `$didShrineInfoDump` | S / 2 | `lppHookCurateHowManyA` (S) | `$didShrineInfoDump = true` — `lppHookCurateHowManyA` |
| `$didShrineJobInfo` | CS / 5 | `lppHookCurateCredits2` (C) | `$didShrineJobInfo = true` — `lppHookCurateCredits` |
| `$didSmashAsk` | CS / 4 | `lkeChalcedonBarSedgeSmashA` (C) | `$didSmashAsk = true 0` — `lkeAskSedgeSmashA` |
| `$didSpar` | CS / 5 | `kantasDenFirstVisitStationKingOptC` (C) | `$didSpar = true 0` — `kdStationKingSparA` |
| `$didSpecialExploreStory` | CS / 20 | `salSMOLstart` (C) | `$didSpecialExploreStory = true` — `salSMOLout` |
| `$didStep1` | CS / 12 | `BFFIpsaConHubOptA0` (C) | `$didStep1 = true 0` — `BFFIpsaConHubOptRespA1` |
| `$didStubborn` | CS / 2 | `lppVolturnCurateResponses2SDc` (C) | `$didStubborn = true 0` — `lppVolturnCurateSDstubborn` |
| `$didSuccinctAside` | CS / 1 | `gaATGdaudMeetingHubEndEarly` (C) | `$didSuccinctAside = true 0` — `gaATGdaudMeetingHubEndEarly` |
| `$didSwordTutorial` | C / 1 | `soeDuelPrepTutorialDo` (C) | Trace owning rule/command or generated interaction data |
| `$didTTcom` | CS / 1 | `ZGRacknowledgeTTcom` (C) | `$didTTcom = true 0` — `ZGRacknowledgeTTcom` |
| `$didTTdeal` | CS / 1 | `ZGRacknowledgeTTDeal` (C) | `$didTTdeal = true 0` — `ZGRacknowledgeTTDeal` |
| `$didTell` | CS / 5 | `adonyaOptMoreHolos` (C) | `$didTell = true 0` — `adonyaTell5` |
| `$didUps` | CS / 18 | `ZGRacknowledgeDone` (C) | `$didUps++` — `ZGRacknowledgeGA1` |
| `$didVeracity` | CS / 3 | `BFFImpBuyerHubOptA` (C) | `$didVeracity = true 0` — `BFFImpBuyerVeracityA` |
| `$didWanted` | CS / 2 | `gaATGbairdEndingHubOptB` (C) | `$didWanted = true 0` — `gaATGbairdEndingWanted` |
| `$didWork` | CS / 2 | `gaATGbairdEndingHubOptA` (C) | `$didWork = true 0` — `gaATGbairdEndingWork` |
| `$didZalResponse` | CS / 3 | `kpAudience1StoleZal` (C) | `$didZalResponse = true` — `kpAudience1StoleZal2` |
| `$did_makeshiftWeapon` | CS / 3 | `sdtuRamMakeshift0` (C) | `$did_makeshiftWeapon = true 0` — `sdtuRamSafehouseReactionB` |
| `$did_marines` | CS / 5 | `sdtuRamMakeshift0` (C) | `$did_marines = true 0` — `sdtuRamSafehouseReactionE` |
| `$did_ramSafehouse5` | CS / 7 | `sdtuRamSafehouseReaction0` (C) | `$did_ramSafehouse5 = true 0` — `sdtuRamSafehouse5` |
| `$did_weapon_prep` | CS / 4 | `sdtuRamSafehouseReaction1` (C) | `$did_weapon_prep = true 0` — `sdtuRamSafehouseReactionD` |
| `$didone` | CS / 5 | `gaATGzalMissionStartHub4` (C) | `$didone = true 0` — `gaATGzalMissionHubA` |
| `$distress` | C / 1 | `dstr_normalStart` (C) | `impl/campaign/events/nearby/DistressCallNormalAssignmentAI.java`, `impl/campaign/events/nearby/NearbyEventsEvent.java` |
| `$distressCanAfford` | C / 1 | `dcall_paymentOptionsUpdate` (C) | `impl/campaign/rulecmd/salvage/DistressResponse.java` |
| `$distressHelpAdequate` | C / 2 | `dcall_helpSel4` (C) | `impl/campaign/rulecmd/salvage/DistressResponse.java` |
| `$distressNoHail` | C / 2 | `dstr_normalStart` (C) | Trace owning rule/command or generated interaction data |
| `$distressPaymentC` | O / 1 | `dcall_paymentOptions` (O) | `impl/campaign/rulecmd/salvage/DistressResponse.java` |
| `$distressResponse` | C / 1 | `dcall_normalStart` (C) | `impl/campaign/abilities/DistressCallAbility.java`, `impl/campaign/abilities/DistressCallResponseAssignmentAI.java` (more mentions) |
| `$distressTalkedToPlayerBefore` | CS / 3 | `dstr_openComms` (C) | `$distressTalkedToPlayerBefore = true` — `dstr_openComms` |
| `$distressUsesLastCycle` | C / 5 | `dcall_helpSel` (C) | `impl/campaign/rulecmd/salvage/DistressResponse.java` |
| `$dmi_bountyHunter` | C / 1 | `dmi_bountyHunterHail` (C) | `impl/campaign/intel/bar/events/DeliveryFailureConsequences.java` |
| `$doGenericPortAuthorityCheck` | C / 4 | `genericGreetingNeedTOff` (C) | `impl/campaign/missions/hub/BaseHubMission.java` |
| `$doNotDismissDialogAfterSalvage` | S / 1 | `salRuins_noHostileNearby` (S) | `$doNotDismissDialogAfterSalvage = true 0` — `salRuins_noHostileNearby` |
| `$do_contrition` | CS / 2 | `lppHookCurateStartIntroA` (C) | `$do_contrition = true 0` — `lppHookAntiTech2` |
| `$doingCustomsInspection` | C / 2 | `customsInspectionScan` (C) | Trace owning rule/command or generated interaction data |
| `$doingGADHOconv` | CS / 3 | `ZGRpostGADHrouteFromBuy` (C) | `$doingGADHOconv = true 0` — `ZGRpostGADHOimply2` |
| `$doingPitch` | CS / 7 | `ZGRtechPlayerOMKIreturnB` (C) | `$doingPitch = true 0` — `ZGRstartPitch1` |
| `$doingTechReturn` | CS / 2 | `ZGRtechPlayerOMKIreturn` (C) | `$doingTechReturn = true 0` — `ZGRtechPlayerOMKIbother` |
| `$doingVisitAgain` | CS / 2 | `PKSentinelOutSequenceAgain` (C) | `$doingVisitAgain = true 0` — `pkSentinelVisitAgain3` |
| `$doneFirstVisit` | CS / 2 | `gaOpenDialogDuringTutorial` (C) | `$doneFirstVisit = true` — `gaOpenDialogDuringTutorial` |
| `$doneWithQuestions` | CS / 2 | `ZGRaskQuestions` (C) | `$doneWithQuestions = true 0` — `ZGRthreatTechAskNone` |
| `$dontLookLikeTerrorists` | CS / 5 | `anhCantinaAskTerroristA` (C) | `$dontLookLikeTerrorists = true 1` — `anhCantinaDontLookTerrorists` |
| `$drankTea` | CS / 2 | `lppVolturnCurateResponses1d` (C) | `$drankTea = true 0` — `lppVolturnCurateDrinkTea` |
| `$dsp_disruptDays` | ST / 2 | `dspOfferTextBar3` (T) | Trace owning rule/command or generated interaction data |
| `$dsp_dist` | ST / 2 | `dspOfferTextBar3` (T) | Trace owning rule/command or generated interaction data |
| `$dsp_industry` | OT / 5 | `dspBlurb` (T) | Trace owning rule/command or generated interaction data |
| `$dsp_marines` | ST / 2 | `dspOfferTextBar3` (T) | Trace owning rule/command or generated interaction data |
| `$dsp_marketName` | ST / 4 | `dspBlurb` (T) | Trace owning rule/command or generated interaction data |
| `$dsp_marketOnOrAt` | T / 4 | `dspBlurb` (T) | Trace owning rule/command or generated interaction data |
| `$dsp_personName` | T / 1 | `dspRaidFinished` (T) | Trace owning rule/command or generated interaction data |
| `$dsp_ref` | S / 2 | `dspOfferTextBar3` (S) | Trace owning rule/command or generated interaction data |
| `$dsp_reward` | ST / 2 | `dspOfferTextBar3` (T) | Trace owning rule/command or generated interaction data |
| `$elissaAside` | CS / 3 | `gaFCQuestionBioneElissaZal` (C) | `$elissaAside = true 0` — `gaFCFikenhildBioneAstart` |
| `$encountered` | CS / 1 | `mk1_wreckDetailsFirstTime` (C) | `$encountered = true` — `mk1_wreckDetailsFirstTime` |
| `$encounteredAlready` | CS / 12 | `gaFCProbeAmbushEncounter` (C) | `$encounteredAlready = true` — `gaFCProbeAmbushEncounter` |
| `$enforcerScanConv` | S / 1 | `plEnforcerInitial` (S) | `$enforcerScanConv = true 0` — `plEnforcerInitial` |
| `$entity.KOLT_armada` | C / 2 | `KOLTHolyArmadaComms` (C) | Trace owning rule/command or generated interaction data |
| `$entity.KOLT_isBlockading` | C / 2 | `KOLTHolyArmadaComms` (C) | Trace owning rule/command or generated interaction data |
| `$entity.LP_tithe` | S / 1 | `LPTithePay` (S) | Trace owning rule/command or generated interaction data |
| `$entity.LP_titheConv` | C / 3 | `greetingPathWeirdHMintroTitheB` (C) | Trace owning rule/command or generated interaction data |
| `$entity.LP_titheDGS` | ST / 3 | `LPTithePre` (T) | Trace owning rule/command or generated interaction data |
| `$entity.LP_tithePaid` | S / 2 | `payMegaTitheConfirmSel` (S) | `$entity.LP_tithePaid = true 100` — `payMegaTitheConfirmSel` |
| `$entity.PLB_armada` | C / 2 | `plBlockaderComms` (C) | Trace owning rule/command or generated interaction data |
| `$entity.PLB_isBlockading` | C / 2 | `plBlockaderComms` (C) | Trace owning rule/command or generated interaction data |
| `$entity.PLPE_fleet` | C / 2 | `plPunExComms` (C) | Trace owning rule/command or generated interaction data |
| `$entity.SDPE_fleet` | C / 3 | `sdPunExComms` (C) | Trace owning rule/command or generated interaction data |
| `$entity.TTMA_command` | C / 1 | `ttma_commandCommsNotHostile` (C) | Trace owning rule/command or generated interaction data |
| `$entity.TTMA_fleet` | C / 2 | `ttma_detachmentCommsNotHostile` (C) | Trace owning rule/command or generated interaction data |
| `$entity.academyFleet` | C / 2 | `academyFleetConvOption` (C) | Trace owning rule/command or generated interaction data |
| `$entity.acceptedProvost` | S / 2 | `gaMeetingAccept` (S) | `$entity.acceptedProvost = true` — `gaMeetingAccept` |
| `$entity.ahn_ref` | S / 1 | `anhDiktatPatrolGreeting` (S) | Trace owning rule/command or generated interaction data |
| `$entity.anh_diktatPatrol` | C / 1 | `anhDiktatPatrolGreeting` (C) | Trace owning rule/command or generated interaction data |
| `$entity.anh_handOverBoth` | CS / 2 | `anhDiktatPatrolAgainOptionB` (C) | `$entity.anh_handOverBoth = true 0` — `anhDiktatPatrolStall3All` |
| `$entity.anh_handOverMan` | CS / 2 | `anhDiktatPatrolAgainOptionA` (C) | `$entity.anh_handOverMan = true 0` — `anhDiktatPatrolStall3Yes` |
| `$entity.anh_noDeal` | S / 1 | `anhDiktatPatrolStall3No` (S) | `$entity.anh_noDeal = true 0` — `anhDiktatPatrolStall3No` |
| `$entity.anh_ref` | S / 5 | `anhDiktatPatrolSearch2` (S) | Trace owning rule/command or generated interaction data |
| `$entity.askedForKidnapperCotton` | CS / 5 | `gaATGEpiphanySearch1` (C) | `$entity.askedForKidnapperCotton = true` — `gaATGEpiphanyAskLoke` |
| `$entity.bffi_patherStationTarget` | C / 4 | `BFFIpatherStationTalkBuyer` (C) | Trace owning rule/command or generated interaction data |
| `$entity.boughtHTData` | CS / 7 | `scavBuyHTDataOpt` (C) | `$entity.boughtHTData = true 60` — `buyHTDataConfirmSel` |
| `$entity.cargoScanConv` | CS / 6 | `cargoScanFirstComms` (C) | Trace owning rule/command or generated interaction data |
| `$entity.customsInspectionStage` | CS / 2 | `customsInspectionConvStart` (C) | `$entity.customsInspectionStage = 3 0` — `customsInspectionAddContinue` |
| `$entity.declinedProvost` | S / 1 | `gaMeetingDecline` (S) | `$entity.declinedProvost = true` — `gaMeetingDecline` |
| `$entity.diktatRaider` | C / 1 | `sdRaiderComms` (C) | Trace owning rule/command or generated interaction data |
| `$entity.distress` | CS / 4 | `dstr_openComms` (C) | Trace owning rule/command or generated interaction data |
| `$entity.distressCredits` | S / 3 | `dstr_openComms` (S) | Trace owning rule/command or generated interaction data |
| `$entity.distressCrew` | C / 1 | `dstr_takeCrewEnoughRoom` (C) | Trace owning rule/command or generated interaction data |
| `$entity.distressCrewTakeOn` | CST / 2 | `dstr_takeCrewNotEnoughRoom` (T) | Trace owning rule/command or generated interaction data |
| `$entity.distressFuel` | CS / 5 | `dstr_showEnoughFuel` (C) | Trace owning rule/command or generated interaction data |
| `$entity.distressFuelHostileThreshold` | C / 1 | `dstr_denyTurnHostileSel` (C) | Trace owning rule/command or generated interaction data |
| `$entity.distressNoHail` | S / 4 | `dstr_acceptCrewSel` (S) | `$entity.distressNoHail = true` — `dstr_acceptCrewSel` |
| `$entity.distressResponse` | C / 3 | `dcall_openCommsInhospitable` (C) | Trace owning rule/command or generated interaction data |
| `$entity.distressTurnHostile` | C / 2 | `dstr_takeCrewNo` (C) | Trace owning rule/command or generated interaction data |
| `$entity.dmi_bountyHunter` | C / 1 | `dmi_bountyHunterText` (C) | Trace owning rule/command or generated interaction data |
| `$entity.dmi_commodity` | T / 1 | `dmi_bountyHunterText` (T) | Trace owning rule/command or generated interaction data |
| `$entity.dmi_himOrHer` | T / 1 | `dmi_bountyHunterText` (T) | Trace owning rule/command or generated interaction data |
| `$entity.dmi_hisOrHer` | T / 1 | `dmi_bountyHunterText` (T) | Trace owning rule/command or generated interaction data |
| `$entity.dmi_hunter` | T / 1 | `dmi_bountyHunterText` (T) | Trace owning rule/command or generated interaction data |
| `$entity.dmi_name` | T / 1 | `dmi_bountyHunterText` (T) | Trace owning rule/command or generated interaction data |
| `$entity.doingCustomsInspection` | CS / 15 | `customsInspectionConvStart` (C) | Trace owning rule/command or generated interaction data |
| `$entity.enforcerScanConv` | C / 2 | `plEnforcerComms` (C) | Trace owning rule/command or generated interaction data |
| `$entity.fleetPoints` | S / 4 | `anhDiktatPatrolSearch` (S) | Trace owning rule/command or generated interaction data |
| `$entity.fwt_TheThing` | T / 1 | `fwtOpenCommPatherAggroNoPay` (T) | Trace owning rule/command or generated interaction data |
| `$entity.fwt_ThingDesc` | T / 1 | `fwtOpenCommMercNoAggroPay` (T) | Trace owning rule/command or generated interaction data |
| `$entity.fwt_aggressive` | C / 12 | `fwtOpenCommGenericAggroNoPay` (C) | Trace owning rule/command or generated interaction data |
| `$entity.fwt_encounteredAlready` | CS / 12 | `fwtOpenCommGenericAggroNoPay` (C) | `$entity.fwt_encounteredAlready = true` — `fwtOpenCommGenericAggroNoPay` |
| `$entity.fwt_missionFailTrigger` | S / 9 | `fwt_acceptSelNoPay` (S) | Trace owning rule/command or generated interaction data |
| `$entity.fwt_originalFaction` | C / 3 | `fwtOpenCommMercAggroNoPay` (C) | Trace owning rule/command or generated interaction data |
| `$entity.fwt_payment` | CST / 16 | `fwtOpenCommGenericAggroPay` (T) | Trace owning rule/command or generated interaction data |
| `$entity.fwt_ref` | S / 4 | `FWTDefaultFailTrigger` (S) | Trace owning rule/command or generated interaction data |
| `$entity.fwt_theThing` | OT / 12 | `fwtOpenCommPatherAggroNoPay` (T) | Trace owning rule/command or generated interaction data |
| `$entity.fwt_thing` | S / 2 | `fwtOpenCommPirates` (S) | Trace owning rule/command or generated interaction data |
| `$entity.fwt_thingDesc` | T / 6 | `fwtOpenCommGenericAggroNoPay` (T) | Trace owning rule/command or generated interaction data |
| `$entity.fwt_wantsThing` | CS / 23 | `fwtOpenCommGenericAggroNoPay` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaATG_hegScanFleet` | C / 3 | `gaATGhegFleetDialog` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaATG_luddicScanFleet` | C / 3 | `gaATGluddicScanHubOptionsA` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaATG_pirateScanFleet` | C / 1 | `gaATGpirateScanFleetHello` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaATG_ref` | S / 1 | `gaATGsiyavongQuestioningStart` (S) | Trace owning rule/command or generated interaction data |
| `$entity.gaATG_scavScanFleet` | C / 1 | `gaATGscavScanDialog` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaATG_siyavongFleet` | CS / 2 | `gaATGsiyavongFleetOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaATG_ttScanFleet` | CS / 3 | `gaATGttScanFleetOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaCO_patherMissionInProgress` | C / 2 | `gaCOPatherOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaCO_patherPermanentFlag` | C / 1 | `gaCOPatherOpenCommAfter` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaCO_patherTithe` | OS / 2 | `gaCOPatherOpenCommAfter` (O) | Trace owning rule/command or generated interaction data |
| `$entity.gaCO_ref` | S / 1 | `gaCOPatherConvince4` (S) | Trace owning rule/command or generated interaction data |
| `$entity.gaDA_pirate` | C / 1 | `gaDAPirateOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaDA_scavenger` | C / 1 | `gaDAScavOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaData_pirateConv` | C / 1 | `gaDataPirateOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaFCProbe_scavengerPermanentFlag` | C / 2 | `gaFCScavengerGreeting` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaFC_isirahMerc` | C / 1 | `gaFCIsirahMercGreeting` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaFC_patherProbeAmbush` | C / 1 | `gaFCPatherAmbushGreeting` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaFC_ref` | S / 3 | `gaFCScavengerGreeting` (S) | Trace owning rule/command or generated interaction data |
| `$entity.gaKA_patrol` | C / 1 | `gaKAPatrolOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaPZ_diktat` | C / 1 | `gaPZDiktatEncounterComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaPZ_diktatSecond` | C / 1 | `gaPZDiktatEncounterComm2` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaPZ_hegemony` | C / 1 | `gaPZHegemonyEncounterComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaPZ_luddic_church` | C / 1 | `gaPZLCEncounterComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaPZ_persean` | C / 1 | `gaPZPLEncounterComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaPZ_ref` | S / 1 | `gaPZ_rkAgree` (S) | Trace owning rule/command or generated interaction data |
| `$entity.gaPZ_rogueKnight` | C / 1 | `gaPZKnightOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaPZ_tritachyon` | C / 1 | `gaPZTriTachEncounterComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaPZ_ttMerc` | C / 1 | `gaPZmercOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaProbe_finishedEncounter` | CS / 6 | `gaProbePatherOpenComm` (C) | `$entity.gaProbe_finishedEncounter = true` — `gaPatherEndPolite` |
| `$entity.gaProbe_patherPermanentFlag` | C / 1 | `gaProbePatherOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaProbe_ref` | S / 3 | `gaProbeGiveAid` (S) | Trace owning rule/command or generated interaction data |
| `$entity.gaProbe_scavenger` | C / 2 | `gaProbeScavCommBefore` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaRH_consequences` | C / 1 | `gaRHRevengeOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaVIP_kantaConsequences` | C / 1 | `gaVIPRevengeOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaVIP_merc` | C / 1 | `gaVIPMercOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gaVIP_pirate` | C / 1 | `gaVIPPirateOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.gavePackageToPlayer` | CS / 4 | `gaProbeScavCommBefore` (C) | `$entity.gavePackageToPlayer = true` — `gaProbeConfront2` |
| `$entity.genericHail` | CS / 5 | `genericHailOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.genericHail_isHailing` | C / 2 | `genericHailOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.genericHail_nonHostile` | C / 1 | `genericHailOpenCommNonHostile` (C) | Trace owning rule/command or generated interaction data |
| `$entity.genericHail_openComms` | S / 2 | `genericHailOpenComm` (S) | Trace owning rule/command or generated interaction data |
| `$entity.hasMarket` | C / 2 | `gaVIPWorkingForKanta` (C) | Trace owning rule/command or generated interaction data |
| `$entity.hasOnlyZiggurat` | C / 3 | `gaPZHegemonyEncounterOnlyZig` (C) | Trace owning rule/command or generated interaction data |
| `$entity.hasZiggurat` | C / 9 | `gaPZHegemonyEncounterNoZig` (C) | Trace owning rule/command or generated interaction data |
| `$entity.id` | C / 7 | `lppHookCurateHowManyA` (C) | Trace owning rule/command or generated interaction data |
| `$entity.ignorePlayerCommRequests` | CS / 150 | `greetingNoComms` (C) | `$entity.ignorePlayerCommRequests = true 0` — `CGRpatrolWeirdModsUnknownEnd` |
| `$entity.inspectionResultType` | C / 9 | `customsInspectionResultToll` (C) | Trace owning rule/command or generated interaction data |
| `$entity.investigatorConv` | C / 2 | `hegInvestigatorComms` (C) | Trace owning rule/command or generated interaction data |
| `$entity.isHostile` | C / 38 | `greetingDefaultHostileWeaker` (C) | Trace owning rule/command or generated interaction data |
| `$entity.isPatrol` | C / 2 | `greetingsLuddicPatrolCanaan` (C) | Trace owning rule/command or generated interaction data |
| `$entity.isScavenger` | C / 5 | `scavBuyHTDataOpt` (C) | Trace owning rule/command or generated interaction data |
| `$entity.isTradeFleet` | C / 5 | `CGRpatrolWeirdModsUnknownEndTrade` (C) | Trace owning rule/command or generated interaction data |
| `$entity.knowWhoPlayerIs` | C / 2 | `CGRpatrolWeirdMods0unknown` (C) | Trace owning rule/command or generated interaction data |
| `$entity.knowsWhoPlayerIs` | C / 1 | `tOffPatrolOpenCommUnknown` (C) | Trace owning rule/command or generated interaction data |
| `$entity.lke_patherIntercept` | C / 1 | `lkePatherFleetOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.lke_ref` | S / 3 | `lkePatherFleetEndPeace` (S) | Trace owning rule/command or generated interaction data |
| `$entity.locationId` | C / 8 | `greetingsScavMairaathNeutral` (C) | Trace owning rule/command or generated interaction data |
| `$entity.locr_luddic` | S / 3 | `LOCRLofferTransport` (S) | Trace owning rule/command or generated interaction data |
| `$entity.locr_miners` | S / 1 | `LOCRMofferTransport2` (S) | Trace owning rule/command or generated interaction data |
| `$entity.locr_pirate` | S / 4 | `LOCRPtransportLie` (S) | Trace owning rule/command or generated interaction data |
| `$entity.locrl_contacted` | CS / 5 | `LOCRLstartAgain` (C) | `$entity.locrl_contacted = true` — `LOCRLcontact1` |
| `$entity.lppShrineSaidBadPlace` | CS / 3 | `lppJangalaAttendantRespA` (C) | `$entity.lppShrineSaidBadPlace = true` — `lppJangalaShrineDifficult` |
| `$entity.makeNonHostileTakesPriority` | S / 1 | `LPTithePay` (S) | `$entity.makeNonHostileTakesPriority = true 30` — `LPTithePay` |
| `$entity.missionImportant_gaBA` | C / 3 | `pwiBluff1gaba` (C) | Trace owning rule/command or generated interaction data |
| `$entity.mpm_isSpawnedByMPM` | C / 1 | `mpm_pirateOpenComms` (C) | Trace owning rule/command or generated interaction data |
| `$entity.name` | OT / 5 | `mercs_leavingInitial` (T) | Trace owning rule/command or generated interaction data |
| `$entity.ne_eventRef` | S / 10 | `dstr_openComms` (S) | Trace owning rule/command or generated interaction data |
| `$entity.numSeen` | C / 2 | `warnAttackOpenComm1` (C) | Trace owning rule/command or generated interaction data |
| `$entity.offeredBribeForCotton` | CS / 2 | `gaATGEpiphanySearch2` (C) | `$entity.offeredBribeForCotton = true` — `gaATGEpiphanyAskCredits` |
| `$entity.patrolAllowTOff` | CS / 5 | `tOffPatrolOpenComm` (C) | `$entity.patrolAllowTOff = true 10` — `scanTalkYourWayOut` |
| `$entity.pkDefenderFleet` | C / 1 | `pkFleetGreeting` (C) | Trace owning rule/command or generated interaction data |
| `$entity.playerCanAffordPayment` | C / 4 | `customsInspectionPayToll` (C) | Trace owning rule/command or generated interaction data |
| `$entity.playerTookDistressCrewRecently` | CS / 3 | `dstr_sellFuelTookCrew` (C) | `$entity.playerTookDistressCrewRecently = true 7` — `dstr_acceptCrewSel` |
| `$entity.protectorScanConv` | C / 2 | `lcSacredProtectorsComms` (C) | Trace owning rule/command or generated interaction data |
| `$entity.psk_merc` | C / 1 | `pskMercGreeting` (C) | Trace owning rule/command or generated interaction data |
| `$entity.ptc_cargoConv` | C / 1 | `piratesDemandCargo` (C) | Trace owning rule/command or generated interaction data |
| `$entity.pursuePlayer_hassle` | C / 1 | `plBlockaderComms` (C) | Trace owning rule/command or generated interaction data |
| `$entity.pursuePlayer_smugglingScan` | S / 5 | `scanTalkYourWayOut` (S) | Trace owning rule/command or generated interaction data |
| `$entity.pwi2_item` | S / 1 | `pwi2OpenComm` (S) | Trace owning rule/command or generated interaction data |
| `$entity.pwi2_missionFailTrigger` | S / 1 | `pwi2_accept1` (S) | Trace owning rule/command or generated interaction data |
| `$entity.pwi2_wantsItem` | C / 1 | `pwi2OpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.pwi_credits` | ST / 1 | `pwiBluff` (T) | Trace owning rule/command or generated interaction data |
| `$entity.pwi_item` | S / 1 | `pwiOpenComm` (S) | Trace owning rule/command or generated interaction data |
| `$entity.pwi_missionFailTrigger` | S / 1 | `pwi_accept` (S) | Trace owning rule/command or generated interaction data |
| `$entity.pwi_wantsItem` | C / 1 | `pwiOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.raidContinueTrigger` | S / 4 | `GABAPatherMainOptions` (S) | `$entity.raidContinueTrigger = GABARaidFinishedContinue 0` — `GABAPatherMainOptions` |
| `$entity.raidDifficulty` | S / 4 | `GABAPatherMainOptions` (S) | `$entity.raidDifficulty = $gaBA_raidDifficulty 0` — `GABAPatherMainOptions` |
| `$entity.raidGoBackTrigger` | S / 4 | `GABAPatherMainOptions` (S) | `$entity.raidGoBackTrigger = GABAPatherMainOptions 0` — `GABAPatherMainOptions` |
| `$entity.raidRestrictToTrigger` | S / 4 | `GABAPatherMainOptions` (S) | `$entity.raidRestrictToTrigger = GABARaidFinished 0` — `GABAPatherMainOptions` |
| `$entity.relValue` | C / 4 | `gaATGsiyavongQuestioningStartB` (C) | Trace owning rule/command or generated interaction data |
| `$entity.relativeStrength` | C / 77 | `CGRpatrolWeirdModsUnknownEnd` (C) | Trace owning rule/command or generated interaction data |
| `$entity.repCheckResult` | C / 2 | `optionRefuseCITollPassRep` (C) | Trace owning rule/command or generated interaction data |
| `$entity.resumedVigil` | C / 9 | `gaATGluddicScanHubOptionsC` (C) | Trace owning rule/command or generated interaction data |
| `$entity.rogueMiner` | C / 5 | `greetingRogueMinerFriendly` (C) | Trace owning rule/command or generated interaction data |
| `$entity.sawPlayerTransponderOff` | S / 2 | `tOffComply` (S) | Trace owning rule/command or generated interaction data |
| `$entity.sawPlayerWithTOffCount` | CS / 2 | `tOffPatrolOpenCommNotFirstTime` (C) | `$entity.sawPlayerWithTOffCount = 0` — `scanTalkYourWayOut` |
| `$entity.sdtu_antisFleet` | C / 2 | `sdtuARCfleetGreetingA` (C) | Trace owning rule/command or generated interaction data |
| `$entity.sdtu_antisRevengeFleet` | C / 2 | `sdtuARCfleetGreetingB` (C) | Trace owning rule/command or generated interaction data |
| `$entity.sdtu_interceptFleet` | CS / 8 | `sdtuTraitorPatrolOptionConfront` (C) | Trace owning rule/command or generated interaction data |
| `$entity.sdtu_merc` | C / 3 | `sdtuMercGreeting` (C) | Trace owning rule/command or generated interaction data |
| `$entity.sdtu_ref` | S / 4 | `sdtuPatrolLeave` (S) | Trace owning rule/command or generated interaction data |
| `$entity.setUpCottonMeeting` | CS / 6 | `gaATGEpiphanySearch1` (C) | `$entity.setUpCottonMeeting = true` — `gaATGEpiphanyAskCottonAccept` |
| `$entity.shrinePilgrimFleet` | C / 1 | `shrineFleetConvOption` (C) | Trace owning rule/command or generated interaction data |
| `$entity.smugglingScanComplete` | S / 5 | `scanTalkYourWayOut` (S) | `$entity.smugglingScanComplete = true 1` — `scanTalkYourWayOut` |
| `$entity.tabo_marketName` | T / 2 | `TABOPatrolHail1` (T) | Trace owning rule/command or generated interaction data |
| `$entity.tollAmount` | S / 2 | `customsInspectionResultToll` (S) | Trace owning rule/command or generated interaction data |
| `$entity.tradeMode` | C / 3 | `genericGreetingNeedClearance` (C) | Trace owning rule/command or generated interaction data |
| `$entity.transponderOffConv` | CS / 8 | `tOffPatrolOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.triTachCommerceRaider` | C / 2 | `triTachCommerceRaiderComms` (C) | Trace owning rule/command or generated interaction data |
| `$entity.ttZigBuyPrice` | ST / 2 | `gaPZ_ttHubPay` (T) | Trace owning rule/command or generated interaction data |
| `$entity.ttZigLowBuyPrice` | ST / 1 | `gaPZ_ttHubAccept` (T) | Trace owning rule/command or generated interaction data |
| `$entity.ttcr_derelict` | C / 4 | `TTCR_BountyHunterOptionsButDer` (C) | Trace owning rule/command or generated interaction data |
| `$entity.ttcr_phase` | C / 3 | `TTCR_BountyHunterOptionsButPhase` (C) | Trace owning rule/command or generated interaction data |
| `$entity.ttcr_wolfpack` | C / 3 | `TTCR_BountyHunterOptionsButWolf` (C) | Trace owning rule/command or generated interaction data |
| `$entity.ttli_bountyHunter` | C / 1 | `ttli_bountyHunterText` (C) | Trace owning rule/command or generated interaction data |
| `$entity.ttli_hisOrHer` | T / 1 | `ttli_bountyHunterText` (T) | Trace owning rule/command or generated interaction data |
| `$entity.ttwi_credits` | ST / 2 | `ttwiOpenComm` (T) | Trace owning rule/command or generated interaction data |
| `$entity.ttwi_missionFailTrigger` | S / 1 | `ttwi_accept1` (S) | Trace owning rule/command or generated interaction data |
| `$entity.ttwi_wantsItem` | C / 1 | `ttwiOpenComm` (C) | Trace owning rule/command or generated interaction data |
| `$entity.waIsHailing` | CS / 2 | `warnAttackOpenComm1` (C) | Trace owning rule/command or generated interaction data |
| `$entity.wantsItem` | S / 1 | `ttwi_accept` (S) | Trace owning rule/command or generated interaction data |
| `$entity.warnAttack` | C / 2 | `warnAttackOpenComm1` (C) | Trace owning rule/command or generated interaction data |
| `$entity.warnAttack_attackComms` | S / 1 | `warnAttackOpenComm2` (S) | Trace owning rule/command or generated interaction data |
| `$entity.warnAttack_warningComms` | S / 1 | `warnAttackOpenComm1` (S) | Trace owning rule/command or generated interaction data |
| `$entity.weakerThanPlayerButHolding` | C / 8 | `greetingDefaultHostileWeakerDefiant` (C) | Trace owning rule/command or generated interaction data |
| `$entity.ziggurat` | C / 1 | `zig_commsOpen` (C) | Trace owning rule/command or generated interaction data |
| `$entity.zigguratMember` | S / 5 | `gaPZ_ttHubPayAccept` (S) | Trace owning rule/command or generated interaction data |
| `$entityName` | ST / 30 | `abyssalGasGiantTurbulence` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$explainedSupportDelay` | CS / 2 | `ImoinuKatoUmbraSupportedCheck` (C) | `$explainedSupportDelay = true` — `imoinuUmbraOfferHelpTooSoon` |
| `$explored` | CS / 23 | `GS_AI_CORES_open` (C) | `$explored = true` — `GS_AI_CORES_cont1Sel` |
| `$expressedCynicism` | S / 2 | `PLArmadaCynicalNo` (S) | `$expressedCynicism = true` — `PLArmadaCynicalNo` |
| `$expressedIdealism` | S / 2 | `PLArmadaIdealisticNo` (S) | `$expressedIdealism = true` — `PLArmadaIdealisticNo` |
| `$expressedSupport` | CS / 5 | `sdtuUmbraImoinu2` (C) | `$expressedSupport = true` — `ImoinuKatoUmbraSalute1` |
| `$extr_addedRaidObjective` | CS / 3 | `extrOpenMarket1` (C) | `$extr_addedRaidObjective = true 0` — `extrOpenMarket1` |
| `$extr_barName` | T / 2 | `extr_barStoryOption1` (T) | `impl/campaign/missions/ExtractionMission.java` |
| `$extr_barOwner` | T / 3 | `extr_barStoryOption2` (T) | `impl/campaign/missions/ExtractionMission.java` |
| `$extr_completed` | S / 3 | `extrMissionReturn1` (S) | `$extr_completed = true` — `extrMissionReturn1` |
| `$extr_danger` | S / 3 | `extrOpenMarket1` (S) | `impl/campaign/missions/ExtractionMission.java` |
| `$extr_factionColor` | S / 5 | `extrOfferTextBar1` (S) | `impl/campaign/missions/ExtractionMission.java` |
| `$extr_manOrWoman` | OT / 6 | `extrBlurbBar1` (T) | `impl/campaign/missions/ExtractionMission.java` |
| `$extr_marines` | ST / 6 | `extrOfferTextBar1` (T) | `impl/campaign/missions/ExtractionMission.java` |
| `$extr_marketFaction` | ST / 2 | `extrOfferTextBar1` (T) | `impl/campaign/missions/ExtractionMission.java` |
| `$extr_marketFactionArticle` | T / 2 | `extrOfferTextBar1` (T) | `impl/campaign/missions/ExtractionMission.java` |
| `$extr_marketName` | ST / 6 | `extrOfferTextBar1` (T) | Trace owning rule/command or generated interaction data |
| `$extr_marketOnOrAt` | T / 5 | `extrOfferTextBar1` (T) | `impl/campaign/missions/ExtractionMission.java` |
| `$extr_ref` | CS / 9 | `extrMissionReturn1` (C) | Trace owning rule/command or generated interaction data |
| `$extr_returnHere` | C / 3 | `extrMissionReturn1` (C) | Trace owning rule/command or generated interaction data |
| `$extr_reward` | ST / 6 | `extrOfferTextBar1` (T) | `impl/campaign/missions/ExtractionMission.java` |
| `$extr_skipBarIntercept` | CS / 2 | `extr_barIntercept` (C) | `$extr_skipBarIntercept = true 0` — `extr_barIgnore` |
| `$extr_storyCost` | ST / 2 | `extr_barPickUp` (T) | `impl/campaign/missions/ExtractionMission.java` |
| `$extr_variation` | C / 27 | `extrBlurb1` (C) | `impl/campaign/missions/ExtractionMission.java` |
| `$faction` | OST / 58 | `marketPostOpenToOffPatrols` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/events/BaseEventPlugin.java` |
| `$faction.baseColor` | S / 11 | `LPTitheCheck` (S) | Trace owning rule/command or generated interaction data |
| `$faction.c:allowsTransponderOffTrade` | C / 6 | `marketPostOpenNoTrade` (C) | Trace owning rule/command or generated interaction data |
| `$faction.c:offersCommissions` | C / 3 | `bqfsAskForSuppliesOptionDEV` (C) | Trace owning rule/command or generated interaction data |
| `$faction.didHighRepBarEncounter` | CS / 3 | `BarHighRepEventStart` (C) | `$faction.didHighRepBarEncounter = true` — `BarHighRepEvent0` |
| `$faction.didLowRepBarEncounter` | CS / 2 | `BarLREstart` (C) | `$faction.didLowRepBarEncounter = true` — `BarLREstart2` |
| `$faction.friendlyToPlayer` | C / 13 | `greetingDefaultFriendly` (C) | Trace owning rule/command or generated interaction data |
| `$faction.id` | CS / 411 | `marketPostOpenGilead` (C) | Trace owning rule/command or generated interaction data |
| `$faction.isHostile` | C / 26 | `marketPostOpenGilead` (C) | Trace owning rule/command or generated interaction data |
| `$faction.isNeutralFaction` | C / 11 | `marketPostOpenDefault` (C) | Trace owning rule/command or generated interaction data |
| `$faction.neutralToPlayer` | C / 10 | `greetingDefaultNeutral` (C) | Trace owning rule/command or generated interaction data |
| `$faction.playerReceivedCommissionResupply` | CS / 4 | `bqfsAskForSuppliesOptAgainCheck` (C) | `$faction.playerReceivedCommissionResupply = true 365` — `bqfsAskedForSuppliesScamOut` |
| `$faction.playerReceivedCommissionResupplyOn` | S / 2 | `bqfsAskedForSuppliesScamOut` (S) | `$faction.playerReceivedCommissionResupplyOn = $global.daysSinceStart` — `bqfsAskedForSuppliesScamOut` |
| `$faction.rel` | C / 86 | `relLevelCooperative` (C) | Trace owning rule/command or generated interaction data |
| `$faction.relValue` | C / 3 | `gaFCKapteynBossAppealFail` (C) | Trace owning rule/command or generated interaction data |
| `$faction.ttProblemsAsked` | CS / 2 | `ttVIPCounterRaiding` (C) | `$faction.ttProblemsAsked = true 90` — `ttVIPCounterRaidingSel0` |
| `$faction.turnedIn_allCores` | C / 1 | `relLevelTriTachyonCores` (C) | Trace owning rule/command or generated interaction data |
| `$factionAOrAn` | O / 1 | `cmsn_meetsCriteria2` (O) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$factionIsOrAre` | T / 2 | `marketPostOpenNoTrade` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/events/BaseEventPlugin.java` |
| `$failedToRefuse` | CS / 8 | `plEnforcerRefuse` (C) | `$failedToRefuse = true 0` — `plEnforcerRefuseSel` |
| `$failedWeirdHullmodCheck` | CS / 13 | `TTmarketPostWeirdHullmods` (C) | `$failedWeirdHullmodCheck = true 1` — `TTmarketPostWeirdHullmods4` |
| `$fidpi_addContinue` | S / 2 | `initial_remnantGhost` (S) | `$fidpi_addContinue = true 0` — `initial_remnantGhost` |
| `$fireAllTrigger` | C / 1 | `gaATGkantasDenHostileOverride1` (C) | `impl/campaign/rulecmd/FireAll.java` |
| `$firstName` | T / 1 | `gaFC_kapteynBossInfoDump2` (T) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$fleet.destShrine` | C / 3 | `shrineConvShrineResponseKilla` (C) | Trace owning rule/command or generated interaction data |
| `$fleetName` | T / 2 | `tOffCargoScanHostile` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$flewThrough` | CS / 2 | `gateFlyThroughOpt` (C) | `$flewThrough = true 0` — `gateFlyThrough` |
| `$followedUpOnAnyMessage` | CS / 5 | `RHannanMsgFollowupDoYouEven` (C) | `$followedUpOnAnyMessage = true` — `RHannanMsgFollowJoinLeague` |
| `$foundContactFaction` | O / 1 | `kpContact` (O) | `impl/campaign/rulecmd/KantaCMD.java` |
| `$foundContactName` | O / 1 | `kpContact` (O) | `impl/campaign/rulecmd/KantaCMD.java` |
| `$foundContactPost` | O / 1 | `kpContact` (O) | `impl/campaign/rulecmd/KantaCMD.java` |
| `$foundShipBPId` | S / 1 | `kpShipBPSel2` (S) | `impl/campaign/rulecmd/KantaCMD.java` |
| `$foundShipBPName` | O / 1 | `kpShipBP` (O) | `impl/campaign/rulecmd/KantaCMD.java` |
| `$foundShipClass` | O / 1 | `kpRemnantCap` (O) | `impl/campaign/rulecmd/KantaCMD.java` |
| `$foundShipId` | S / 1 | `kpRemnantCapSel` (S) | `impl/campaign/rulecmd/KantaCMD.java` |
| `$foundShipName` | OS / 2 | `kpRemnantCap` (O) | `impl/campaign/rulecmd/KantaCMD.java` |
| `$foundStuff` | CS / 3 | `gaPZ_hamatsuWreckOpen1` (C) | `$foundStuff = true` — `gaPZ_hamatsuWreckOpen1` |
| `$foundWeaponId` | S / 1 | `kpOmegaSel` (S) | `impl/campaign/rulecmd/KantaCMD.java` |
| `$foundWeaponName` | O / 1 | `kpOmega` (O) | `impl/campaign/rulecmd/KantaCMD.java` |
| `$fromGhost` | CS / 2 | `sal_wreckDetailsFromGhost` (C) | `impl/campaign/enc/AbyssalRogueStellarObjectDireHintsEPEC.java`, `impl/campaign/ghosts/types/RemnantGhost.java` (more mentions) |
| `$fullName` | T / 2 | `PKSentinelInteraction` (T) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$fwt_hailedAlready` | CS / 1 | `fwtEncounter` (C) | `$fwt_hailedAlready = true` — `fwtEncounter` |
| `$fwt_itOrThem` | T / 3 | `fwtOpenCommPirates` (T) | `impl/campaign/missions/DelayedFleetEncounter.java`, `impl/campaign/missions/hub/HubMissionWithTriggers.java` |
| `$fwt_payment` | T / 4 | `fwtOpenCommMercAggroPay` (T) | `impl/campaign/missions/DelayedFleetEncounter.java`, `impl/campaign/missions/hub/HubMissionWithTriggers.java` |
| `$fwt_piratePayment` | ST / 2 | `fwtPirateBluff` (T) | `$fwt_piratePayment = 10,000 0` — `fwtOpenCommPirates` |
| `$fwt_theThing` | OT / 2 | `fwt_acceptPather` (T) | `impl/campaign/missions/DelayedFleetEncounter.java`, `impl/campaign/missions/hub/HubMissionWithTriggers.java` |
| `$fwt_thingDesc` | ST / 4 | `fwtOpenCommPirates` (T) | `impl/campaign/missions/DelayedFleetEncounter.java`, `impl/campaign/missions/hub/HubMissionWithTriggers.java` |
| `$fwt_wantsThing` | C / 1 | `fwtEncounter` (C) | `impl/campaign/missions/DelayedFleetEncounter.java`, `impl/campaign/missions/hub/HubMissionWithTriggers.java` |
| `$gaATG_cottonRaidDifficulty` | S / 1 | `gaATGcottonRaidStart` (S) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$gaATG_coureuseCredits` | ST / 2 | `gaATGzalMissionCredits` (T) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$gaATG_hegScanFleet` | C / 1 | `gaATGhegFleetEncounter` (C) | `impl/campaign/missions/academy/GAAtTheGates.java`, `impl/campaign/missions/luddic/BornanewFilesFalseIdols.java` |
| `$gaATG_inProgress` | C / 1 | `gaATGIntro` (C) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$gaATG_kantaRaidDifficulty` | S / 1 | `gaATGkantasDenRaidStart` (S) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$gaATG_luddicScanFleet` | C / 1 | `gaATGluddicScanFleet` (C) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$gaATG_pirateScanCost` | ST / 1 | `gaATGpirateScanFleetHello` (T) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$gaATG_pirateScanFleet` | C / 1 | `gaATGpirateScanFleet` (C) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$gaATG_pireateScanCost` | S / 1 | `gaATGpirateScanFleetHello` (S) | Trace owning rule/command or generated interaction data |
| `$gaATG_scavScanFleet` | C / 1 | `gaATGscavScanEncounter` (C) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$gaATG_showMeetBairdOption` | CS / 4 | `gaATGIntro` (C) | `$gaATG_showMeetBairdOption = true` — `gaATGIntro2` |
| `$gaATG_siyavongFleet` | C / 1 | `gaATGsiyavongFleetEncounter` (C) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$gaATG_stage` | C / 8 | `gaATGhegGateOpenDialog` (C) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$gaATG_ttScanCost` | ST / 3 | `gaATGttScanFleet23` (T) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$gaATG_ttScanFleet` | C / 1 | `gaATGttScanFleet` (C) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$gaBA_aOrAnItem` | ST / 4 | `gaBAMissionTextPirate` (T) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$gaBA_contact` | C / 2 | `gaBAPatherGreeting` (C) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$gaBA_contactName` | ST / 5 | `gaBAMissionTextPirate1` (T) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$gaBA_cost` | S / 10 | `GABAPatherMainOptions` (S) | `$gaBA_cost = $gaBA_costHigh 0` — `GABAPatherMainOptions` |
| `$gaBA_costHigh` | OST / 2 | `GABAPatherMainOptions` (T) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$gaBA_costNormal` | OST / 5 | `gaBAMissionTextPirate1` (T) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$gaBA_costVeryLow` | OST / 9 | `gaBAPatherThreaten` (T) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$gaBA_department` | T / 1 | `gaBAMissionReturn` (T) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$gaBA_item` | ST / 5 | `gaBAMissionTextPirate1` (T) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$gaBA_marketName` | ST / 5 | `gaBABlurbPirate` (T) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$gaBA_onOrAt` | T / 2 | `gaBAMissionTextPirate1` (T) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$gaBA_raidDifficulty` | S / 2 | `GABAPatherMainOptions` (S) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$gaBA_returnHere` | C / 1 | `gaBAMissionReturn` (C) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$gaBA_reward` | ST / 2 | `gaBAMissionTextPirate1` (T) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$gaBA_systemName` | T / 2 | `gaBAMissionTextPirate1` (T) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$gaBA_variation` | C / 11 | `gaBABlurbPirate` (C) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$gaCO_celestialObjectNameWithTypeLong` | T / 3 | `gaCOMissionTextBasic` (T) | `impl/campaign/missions/academy/GACelestialObject.java` |
| `$gaCO_celestialObjectNameWithTypeShort` | T / 2 | `gaCOBlurb` (T) | `impl/campaign/missions/academy/GACelestialObject.java` |
| `$gaCO_celestialObjectType` | OT / 2 | `gaCOObjectScan` (T) | `impl/campaign/missions/academy/GACelestialObject.java` |
| `$gaCO_department` | T / 6 | `gaCOBlurb` (T) | `impl/campaign/missions/academy/GACelestialObject.java` |
| `$gaCO_dist` | ST / 4 | `gaCOMissionTextBasic` (T) | `impl/campaign/missions/academy/GACelestialObject.java` |
| `$gaCO_encounteredAlready` | CS / 2 | `gaCOPatherEncounterBefore` (C) | `$gaCO_encounteredAlready = true` — `gaCOPatherEncounterBefore` |
| `$gaCO_holoarts` | C / 1 | `gaCOMissionTextBasicHoloarts` (C) | `impl/campaign/missions/academy/GACelestialObject.java` |
| `$gaCO_object` | C / 2 | `gaCOPrintHostilesText` (C) | `impl/campaign/missions/academy/GACelestialObject.java` |
| `$gaCO_patherMissionInProgress` | C / 2 | `gaCOPatherEncounterBefore` (C) | `impl/campaign/missions/academy/GACelestialObject.java` |
| `$gaCO_patherPermanentFlag` | C / 1 | `gaCOPatherEncounterAfter` (C) | `impl/campaign/missions/academy/GACelestialObject.java` |
| `$gaCO_reward` | ST / 4 | `gaCOMissionTextBasic` (T) | `impl/campaign/missions/academy/GACelestialObject.java` |
| `$gaCO_slipUp` | C / 1 | `gaCOMissionTextBasicSlipUp` (C) | `impl/campaign/missions/academy/GACelestialObject.java` |
| `$gaCO_starName` | T / 6 | `gaCOBlurb` (T) | `impl/campaign/missions/academy/GACelestialObject.java` |
| `$gaCO_systemName` | S / 4 | `gaCOMissionTextBasic` (S) | `impl/campaign/missions/academy/GACelestialObject.java` |
| `$gaCO_variation` | C / 3 | `gaCOMissionTextBasic` (C) | `impl/campaign/missions/academy/GACelestialObject.java` |
| `$gaDA_department` | T / 3 | `gaDABlurb` (T) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDA_dist` | ST / 2 | `gaDAMissionTextBasic` (T) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDA_encounteredAlready` | CS / 2 | `gaDAPirateEncounter` (C) | `$gaDA_encounteredAlready = true` — `gaDAPirateEncounter` |
| `$gaDA_entity` | C / 2 | `gaDADerelictHostilesNear` (C) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDA_fuel` | ST / 1 | `gaDAMissionTextRemnants` (T) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDA_notRemnants` | C / 2 | `gaDAMissionTextBasic` (C) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDA_pirate` | C / 1 | `gaDAPirateEncounter` (C) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDA_piratePayment` | ST / 1 | `gaDAPirateOfferToBuy` (T) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDA_piratePaymentLow` | ST / 1 | `gaDADeclineBuyAggroStronger` (T) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDA_piratePrice` | S / 6 | `gaDAPirateOfferToBuy` (S) | `$gaDA_piratePrice = $gaDA_piratePayment 0` — `gaDAPirateOfferToBuy` |
| `$gaDA_returnHere` | C / 1 | `gaDAMissionReturn` (C) | Trace owning rule/command or generated interaction data |
| `$gaDA_reward` | ST / 2 | `gaDAMissionTextBasic` (T) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDA_scavPayment` | ST / 1 | `gaDAScavBuy` (T) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDA_scavPaymentHigh` | COST / 4 | `gaDAScavThreatenScavStronger` (T) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDA_scavPrice` | CS / 8 | `gaDAScavNormalScavPayText` (C) | `$gaDA_scavPrice = $gaDA_scavPayment` — `gaDAScavBuy` |
| `$gaDA_scavenger` | C / 1 | `gaDAScavFleetEncounter` (C) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDA_starName` | T / 1 | `gaDABlurb` (T) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDA_systemName` | ST / 3 | `gaDAMissionTextBasic` (T) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDA_variation` | C / 5 | `gaDADerelictBasic1` (C) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDA_widget` | ST / 5 | `gaDAMissionTextBasic` (T) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDA_widgetNoArticle` | T / 7 | `gaDAMissionTextBasic` (T) | `impl/campaign/missions/academy/GADerelictArtifact.java` |
| `$gaDHO_arrayThenElekThenAbyss` | CS / 4 | `gaDHOendingFoundFirst0b` (C) | `$gaDHO_arrayThenElekThenAbyss = true` — `gaDHOvisitElekIntroMetz` |
| `$gaDHO_askedForPay` | CS / 16 | `gaDHOendingDataTransferOptB` (C) | `$gaDHO_askedForPay = false` — `gaDHOvisitElekStart3` |
| `$gaDHO_elekReturn` | C / 1 | `gaDHOendingStart` (C) | `impl/campaign/missions/academy/GADetectHyperspaceOddity.java` |
| `$gaDHO_object` | C / 1 | `gaDHOarrayStart` (C) | `impl/campaign/missions/academy/GADetectHyperspaceOddity.java` |
| `$gaDHO_stage` | C / 18 | `gaDHOvisitElekStart` (C) | `impl/campaign/missions/academy/GADetectHyperspaceOddity.java` |
| `$gaDHO_transferredData` | CS / 7 | `gaDHOvisitElekAltWhatIsIt3a` (C) | `$gaDHO_transferredData = true` — `gaDHOvisitElekAltTransferData` |
| `$gaData_department` | T / 1 | `gaDataBlurb` (T) | `impl/campaign/missions/academy/BasicExampleGADataFromRuins.java`, `impl/campaign/missions/academy/GADataFromRuins.java` |
| `$gaData_dist` | ST / 4 | `gaDataMissionTextBasicOrPirates` (T) | `impl/campaign/missions/academy/BasicExampleGADataFromRuins.java`, `impl/campaign/missions/academy/GADataFromRuins.java` |
| `$gaData_marinesReq` | ST / 1 | `gaDataMissionTextDeciv` (T) | `impl/campaign/missions/academy/GADataFromRuins.java` |
| `$gaData_pirate` | C / 1 | `gaDataPirateFleetEncounter` (C) | `impl/campaign/missions/academy/BasicExampleGADataFromRuins.java`, `impl/campaign/missions/academy/GADataFromRuins.java` |
| `$gaData_pirateConv` | S / 1 | `gaDataPirateFleetEncounter` (S) | `$gaData_pirateConv = true 0` — `gaDataPirateFleetEncounter` |
| `$gaData_piratePayment` | ST / 3 | `gaDataGiveDataNegotiate2` (T) | `impl/campaign/missions/RuinsDataSwapMission.java`, `impl/campaign/missions/academy/GADataFromRuins.java` |
| `$gaData_planetId` | S / 2 | `gaDataGiveCoordsSel` (S) | `impl/campaign/missions/academy/GADataFromRuins.java` |
| `$gaData_planetName` | OST / 7 | `gaDataBlurb` (T) | `impl/campaign/missions/academy/BasicExampleGADataFromRuins.java`, `impl/campaign/missions/academy/GADataFromRuins.java` |
| `$gaData_pulsarName` | T / 1 | `gaDataMissionTextPulsar` (T) | `impl/campaign/missions/academy/GADataFromRuins.java` |
| `$gaData_raidDifficulty` | S / 1 | `gaDataDecivInteractionRaidOptions` (S) | `impl/campaign/missions/academy/GADataFromRuins.java` |
| `$gaData_reward` | ST / 4 | `gaDataMissionTextBasicOrPirates` (T) | `impl/campaign/missions/academy/BasicExampleGADataFromRuins.java`, `impl/campaign/missions/academy/GADataFromRuins.java` |
| `$gaData_systemName` | T / 5 | `gaDataBlurb` (T) | `impl/campaign/missions/academy/BasicExampleGADataFromRuins.java`, `impl/campaign/missions/academy/GADataFromRuins.java` |
| `$gaData_target` | ST / 10 | `gaDataMissionTextBasicOrPirates` (T) | `impl/campaign/missions/academy/BasicExampleGADataFromRuins.java`, `impl/campaign/missions/academy/GADataFromRuins.java` |
| `$gaData_targetPlanet` | C / 1 | `gaDataPlanetInteraction` (C) | `impl/campaign/missions/academy/BasicExampleGADataFromRuins.java`, `impl/campaign/missions/academy/GADataFromRuins.java` |
| `$gaData_variation` | C / 5 | `gaDataMissionTextBasicOrPirates` (C) | `impl/campaign/missions/academy/GADataFromRuins.java` |
| `$gaFC_KBAHeOrShe` | T / 5 | `gaFCAskAroundKapteynBar3` (T) | `impl/campaign/missions/academy/GAFindingCoureuse.java` |
| `$gaFC_KBAheOrShe` | T / 1 | `gaFCAskAroundKapteynBar1` (T) | `impl/campaign/missions/academy/GAFindingCoureuse.java` |
| `$gaFC_KBAhisOrHer` | T / 2 | `gaFCAskAroundKapteynBarDone` (T) | `impl/campaign/missions/academy/GAFindingCoureuse.java` |
| `$gaFC_bribeCost` | ST / 2 | `gaFCScavengerBribe1` (T) | `impl/campaign/missions/academy/GAFindingCoureuse.java` |
| `$gaFC_confrontAdmin` | S / 2 | `gaFCSearchIsirahSummaryComms3` (S) | `$gaFC_confrontAdmin = true` — `gaFCSearchIsirahSummaryComms3` |
| `$gaFC_isirahMerc` | C / 1 | `gaFCIsirahMercEncounter` (C) | `impl/campaign/missions/academy/GAFindingCoureuse.java` |
| `$gaFC_kapteynBarBribeCost` | ST / 1 | `gaFCAskAroundKapteynBarMore` (T) | `impl/campaign/missions/academy/GAFindingCoureuse.java` |
| `$gaFC_kapteynBribeCost` | ST / 2 | `gaFCKapteynBossBribeStart` (T) | `impl/campaign/missions/academy/GAFindingCoureuse.java` |
| `$gaFC_patherProbeAmbush` | C / 1 | `gaFCProbeAmbushEncounter` (C) | `impl/campaign/missions/academy/GAFindingCoureuse.java` |
| `$gaFC_probe` | C / 3 | `gaFCProbePrintHostilesText` (C) | `impl/campaign/missions/academy/GAFindingCoureuse.java` |
| `$gaFC_sellOutPrice` | ST / 2 | `gaFCIsirahMercLetsDeal` (T) | `impl/campaign/missions/academy/GAFindingCoureuse.java` |
| `$gaFC_stage` | C / 9 | `GAFCBairdGreeting_INVESTIGATE_FIKENHILD` (C) | `impl/campaign/missions/academy/GAFindingCoureuse.java` |
| `$gaFC_starName` | ST / 3 | `bairdIntroScylla2` (T) | `impl/campaign/missions/academy/GAFindingCoureuse.java` |
| `$gaIntro2_credits` | ST / 3 | `gaIntro2returnSeb7` (T) | `impl/campaign/missions/academy/GAIntro2.java` |
| `$gaIntro2startLater` | CS / 4 | `gaIntro2surveyOpenAgain` (C) | `$gaIntro2startLater = true` — `gaIntro2leaveIt` |
| `$gaKA_askedBaird` | CS / 2 | `gaKAArroyoVisitBairdWhatOpt` (C) | `$gaKA_askedBaird = true 0` — `gaKAArroyoVisitBairdWhat` |
| `$gaKA_askedGargoyle` | CS / 2 | `gaKAArroyoVisitGargoyleOpt` (C) | `$gaKA_askedGargoyle = true 0` — `gaKAArroyoVisitGargoyle` |
| `$gaKA_bairdAlreadyYelledAtYou` | CS / 2 | `GAKABairdGreeting_RETREIVE_ARCHIVE` (C) | `$gaKA_bairdAlreadyYelledAtYou = true 10` — `GAKABairdGreeting_RETREIVE_ARCHIVE_Sel` |
| `$gaKA_contact` | C / 2 | `gaKAArroyoGreeting` (C) | `impl/campaign/missions/academy/GAKallichore.java` |
| `$gaKA_contactRetrieve` | C / 1 | `gaKAGargoyle2Greeting` (C) | `impl/campaign/missions/academy/GAKallichore.java` |
| `$gaKA_encounteredAlready` | CS / 1 | `gaKAPatrolEncounter` (C) | `$gaKA_encounteredAlready = true` — `gaKAPatrolEncounter` |
| `$gaKA_goonsFirstVersion` | CS / 3 | `gaKA_hegGoonsOpt` (C) | `$gaKA_goonsFirstVersion = true 0` — `gaKA_hegContinueSel3` |
| `$gaKA_hegArrest` | C / 1 | `gaKA_hegArrestOpt` (C) | Trace owning rule/command or generated interaction data |
| `$gaKA_hegDidntSee` | CS / 2 | `gaKA_hegDidntSeeOpt` (C) | `$gaKA_hegDidntSee = true 0` — `gaKA_hegDidntSeeSel` |
| `$gaKA_hegGoons` | CS / 3 | `gaKA_hegGoonsOpt` (C) | `$gaKA_hegGoons = true 0` — `gaKA_hegGoonsSel` |
| `$gaKA_hegHacked` | CS / 2 | `gaKA_hegHackedOpt` (C) | `$gaKA_hegHacked = true 0` — `gaKA_hegHackedSel` |
| `$gaKA_hegShowTTOpt` | CS / 2 | `gaKA_hegTriTachOpt` (C) | `$gaKA_hegShowTTOpt = true` — `gaKA_hegHackedSel` |
| `$gaKA_hegTriTach` | C / 1 | `gaKA_hegTriTachOpt` (C) | Trace owning rule/command or generated interaction data |
| `$gaKA_installHack` | C / 2 | `gaKA_relayPrintHostilesText` (C) | `impl/campaign/missions/academy/GAKallichore.java` |
| `$gaKA_patrol` | C / 1 | `gaKAPatrolEncounter` (C) | `impl/campaign/missions/academy/GAKallichore.java` |
| `$gaKA_returnThinkOfGargoyle` | CS / 2 | `GAKABairdHubB` (C) | `$gaKA_returnThinkOfGargoyle = true 0` — `GAKABairdHubBSel` |
| `$gaKA_returnWhatWillHegDo` | CS / 2 | `GAKABairdHubA` (C) | `$gaKA_returnWhatWillHegDo = true 0` — `GAKABairdHubASel` |
| `$gaKA_stage` | C / 7 | `GAKARayanArroyo_GET_HACK_HARDWARE` (C) | `impl/campaign/missions/academy/GAKallichore.java` |
| `$gaOp_bizarreProject` | C / 7 | `gaOpPlanetRogueAIProject0` (C) | `impl/campaign/missions/academy/GAOutpost.java` |
| `$gaOp_bizarreProjectStr` | ST / 8 | `gaOpPlanetRogueAICont2` (T) | `$gaOp_bizarreProjectStr = "constructing a small spacecraft, fit to carry a single AI core via hyperspace to parts unknown"` — `gaOpPlanetRogueAIProject0` |
| `$gaOp_department` | T / 4 | `gaOpBlurb` (T) | `impl/campaign/missions/academy/GAOutpost.java` |
| `$gaOp_destroyed` | C / 2 | `gaOpPlanetDestroyed` (C) | `impl/campaign/missions/academy/GAOutpost.java` |
| `$gaOp_dist` | ST / 1 | `gaOpMissionText` (T) | `impl/campaign/missions/academy/GAOutpost.java` |
| `$gaOp_fuel` | CST / 4 | `gaOpPlanetRogueAINegotiate2` (T) | `$gaOp_fuel = 30` — `gaOpPlanetRogueAIConfront1` |
| `$gaOp_leaderHeOrShe` | T / 1 | `gaOpPlanetBasic` (T) | `impl/campaign/missions/academy/GAOutpost.java` |
| `$gaOp_leaderHisOrHer` | T / 1 | `gaOpPlanetBasic` (T) | `impl/campaign/missions/academy/GAOutpost.java` |
| `$gaOp_machinery` | CST / 4 | `gaOpPlanetRogueAINegotiate2` (T) | `$gaOp_machinery = 5` — `gaOpPlanetRogueAIConfront1` |
| `$gaOp_planetName` | ST / 1 | `gaOpMissionText` (T) | `impl/campaign/missions/academy/GAOutpost.java` |
| `$gaOp_returnHere` | C / 2 | `gaOpMissionReturnDestroyed` (C) | `impl/campaign/missions/academy/GAOutpost.java` |
| `$gaOp_reward` | ST / 2 | `gaOpMissionText` (T) | `impl/campaign/missions/academy/GAOutpost.java` |
| `$gaOp_supplies` | CST / 4 | `gaOpPlanetRogueAINegotiate2` (T) | `$gaOp_supplies = 10` — `gaOpPlanetRogueAIConfront1` |
| `$gaOp_systemName` | OT / 3 | `gaOpBlurb` (T) | `impl/campaign/missions/academy/GAOutpost.java` |
| `$gaOp_targetPlanet` | C / 1 | `gaOpPlanetInteraction` (C) | `impl/campaign/missions/academy/GAOutpost.java` |
| `$gaOp_terribleEnd` | C / 4 | `gaOpPlanetDestroyedContOther1` (C) | `impl/campaign/missions/academy/GAOutpost.java` |
| `$gaOp_variation` | C / 8 | `gaOpPlanetDestroyedContPirates` (C) | `impl/campaign/missions/academy/GAOutpost.java` |
| `$gaPZ_baseRuins` | C / 1 | `gaPZ_baseRuinsOpt` (C) | `impl/campaign/missions/academy/GAProjectZiggurat.java` |
| `$gaPZ_baseRuinsExplored` | CS / 2 | `gaPZ_baseRuinsOpt` (C) | `$gaPZ_baseRuinsExplored = true` — `gaPZ_baseRuins4` |
| `$gaPZ_culannAdmin` | C / 1 | `gaPZTalkCulannAdmin` (C) | `impl/campaign/missions/academy/GAProjectZiggurat.java` |
| `$gaPZ_culannBribe` | ST / 2 | `gaPZ_adminAstraia3a` (T) | `impl/campaign/missions/academy/GAProjectZiggurat.java` |
| `$gaPZ_diktat` | C / 1 | `gaPZDiktatEncounter` (C) | `impl/campaign/missions/academy/GAPZPostEncounters.java` |
| `$gaPZ_diktatCom` | C / 1 | `GAPZDiktatHub1` (C) | Trace owning rule/command or generated interaction data |
| `$gaPZ_diktatComp` | C / 1 | `GAPZDiktatHub5` (C) | Trace owning rule/command or generated interaction data |
| `$gaPZ_diktatDefend` | CS / 2 | `GAPZDiktatHub3` (C) | Trace owning rule/command or generated interaction data |
| `$gaPZ_diktatIfRefuse` | CS / 2 | `GAPZDiktatHub2` (C) | `$gaPZ_diktatIfRefuse = true 0` — `gaPZ_diktatIfRefuse` |
| `$gaPZ_diktatSecond` | C / 1 | `gaPZDiktatEncounter2` (C) | `impl/campaign/missions/academy/GAPZPostEncounters.java` |
| `$gaPZ_diktatSurrender` | C / 1 | `GAPZDiktatHub6` (C) | Trace owning rule/command or generated interaction data |
| `$gaPZ_diktatVanish` | C / 1 | `GAPZDiktatHub4` (C) | Trace owning rule/command or generated interaction data |
| `$gaPZ_encounteredAlready` | CS / 8 | `gaPZKnightEncounter` (C) | `$gaPZ_encounteredAlready = true` — `gaPZKnightEncounter` |
| `$gaPZ_hegemony` | C / 1 | `gaPZHegemonyEncounter` (C) | `impl/campaign/missions/academy/GAPZPostEncounters.java` |
| `$gaPZ_inProgress` | C / 1 | `gaPZIntro` (C) | `impl/campaign/missions/academy/GAProjectZiggurat.java` |
| `$gaPZ_introHub1a` | CS / 2 | `PZIntroHub1a` (C) | `$gaPZ_introHub1a = true 0` — `PZIntroHub1aSel` |
| `$gaPZ_introHub1b` | CS / 2 | `PZIntroHub1b` (C) | `$gaPZ_introHub1b = true 0` — `PZIntroHub1bSel` |
| `$gaPZ_introHub1c` | CS / 2 | `PZIntroHub1c` (C) | `$gaPZ_introHub1c = true 0` — `PZIntroHub1cSel` |
| `$gaPZ_luddic_church` | C / 1 | `gaPZLCEncounter` (C) | `impl/campaign/missions/academy/GAPZPostEncounters.java` |
| `$gaPZ_paymentForCommFakes` | OS / 2 | `gaPZSellFakes1` (O) | `impl/campaign/missions/academy/GAProjectZiggurat.java` |
| `$gaPZ_paymentForCommFakesHigh` | ST / 2 | `gaPZSellFakes3b` (T) | `impl/campaign/missions/academy/GAProjectZiggurat.java` |
| `$gaPZ_persean` | C / 1 | `gaPZPLEncounter` (C) | `impl/campaign/missions/academy/GAPZPostEncounters.java` |
| `$gaPZ_relay` | C / 2 | `gaPZRelayPrintHostilesText` (C) | `impl/campaign/missions/academy/GAProjectZiggurat.java` |
| `$gaPZ_relayImportant` | C / 2 | `gaPZRelayInteractionWrongStage` (C) | `impl/campaign/missions/academy/GAProjectZiggurat.java` |
| `$gaPZ_relaySystem` | ST / 2 | `gaPZ_calHamatsu4` (T) | `impl/campaign/missions/academy/GAProjectZiggurat.java` |
| `$gaPZ_returnHere` | C / 2 | `gaPZ_return0NoZig` (C) | `impl/campaign/missions/academy/GAProjectZiggurat.java` |
| `$gaPZ_rkTithe` | ST / 2 | `gaPZ_rkFollowPath` (T) | `impl/campaign/missions/academy/GAProjectZiggurat.java` |
| `$gaPZ_rogueKnight` | C / 1 | `gaPZKnightEncounter` (C) | `impl/campaign/missions/academy/GAProjectZiggurat.java` |
| `$gaPZ_showMeetBairdOption` | CS / 4 | `gaPZIntro` (C) | `$gaPZ_showMeetBairdOption = true` — `gaPZIntro2` |
| `$gaPZ_siya1` | CS / 2 | `GAPZSiyavongHub1` (C) | `$gaPZ_siya1 = true 0` — `gaPZ_siya1` |
| `$gaPZ_siya2` | CS / 2 | `GAPZSiyavongHub2` (C) | `$gaPZ_siya2 = true 0` — `gaPZ_siya2` |
| `$gaPZ_siya3` | CS / 2 | `GAPZSiyavongHub3` (C) | `$gaPZ_siya3 = true 0` — `gaPZ_siya2` |
| `$gaPZ_siya4` | CS / 2 | `GAPZSiyavongHub4` (C) | `$gaPZ_siya4 = true 0` — `gaPZ_siya4` |
| `$gaPZ_stage` | C / 13 | `gaPZAskGargoyleKelise` (C) | `impl/campaign/missions/academy/GAProjectZiggurat.java` |
| `$gaPZ_tritachyon` | C / 1 | `gaPZTriTachEncounter` (C) | `impl/campaign/missions/academy/GAPZPostEncounters.java` |
| `$gaPZ_ttHubSal` | CS / 2 | `GAPZTTHub1` (C) | `$gaPZ_ttHubSal = true 0` — `gaPZ_ttHubSal` |
| `$gaPZ_ttMerc` | C / 1 | `gaPZmercEnc` (C) | `impl/campaign/missions/academy/GAProjectZiggurat.java` |
| `$gaProbeGeneric` | C / 1 | `gaFCProbeSwitched` (C) | `impl/campaign/missions/academy/GAFindingCoureuse.java` |
| `$gaProbe_celestialObjectNameWithType` | T / 1 | `gaProbeMissionTextBasic1` (T) | `impl/campaign/missions/academy/GAProbePackage.java` |
| `$gaProbe_department` | T / 5 | `gaProbeBlurb` (T) | `impl/campaign/missions/academy/GAProbePackage.java` |
| `$gaProbe_dist` | ST / 1 | `gaProbeMissionTextBasic2` (T) | `impl/campaign/missions/academy/GAProbePackage.java` |
| `$gaProbe_encounteredAlready` | CS / 1 | `gaProbePatherEncounter` (C) | `$gaProbe_encounteredAlready = true 0` — `gaProbePatherEncounter` |
| `$gaProbe_finishedEncounter` | C / 1 | `gaProbePatherEncounter` (C) | Trace owning rule/command or generated interaction data |
| `$gaProbe_patherPermanentFlag` | C / 1 | `gaProbePatherEncounter` (C) | `impl/campaign/missions/academy/GAProbePackage.java` |
| `$gaProbe_probe` | C / 2 | `gaProbePrintHostilesText` (C) | `impl/campaign/missions/academy/GAProbePackage.java` |
| `$gaProbe_returnHere` | C / 3 | `gaProbeMissionReturn` (C) | `impl/campaign/missions/academy/GAProbePackage.java` |
| `$gaProbe_reward` | ST / 3 | `gaProbeMissionTextBasic1` (T) | `impl/campaign/missions/academy/GAProbePackage.java` |
| `$gaProbe_stage` | C / 2 | `gaProbeScavCommBefore` (C) | `impl/campaign/missions/academy/GAProbePackage.java` |
| `$gaProbe_starName` | T / 3 | `gaProbeBlurb` (T) | `impl/campaign/missions/academy/GAProbePackage.java` |
| `$gaProbe_systemName` | S / 2 | `gaProbeMissionTextBasic1` (S) | `impl/campaign/missions/academy/GAProbePackage.java` |
| `$gaProbe_variation` | C / 7 | `gaProbeObjectInteractionBasic` (C) | `impl/campaign/missions/academy/GAProbePackage.java` |
| `$gaRH_consequences` | C / 1 | `gaRHRevengeEncounter` (C) | `impl/campaign/missions/academy/GAReturnHamatsu.java` |
| `$gaRH_encounteredAlready` | CS / 1 | `gaRHRevengeEncounter` (C) | `$gaRH_encounteredAlready = true` — `gaRHRevengeEncounter` |
| `$gaRH_returnHere` | C / 1 | `gaRH_withHamatsu` (C) | `impl/campaign/missions/academy/GAReturnHamatsu.java` |
| `$gaRH_reward` | ST / 1 | `gaRH_withHamatsu` (T) | `impl/campaign/missions/academy/GAReturnHamatsu.java` |
| `$gaRR_contact` | C / 2 | `gaRRChurchGreeting` (C) | `impl/campaign/missions/academy/GARansomResearcher.java` |
| `$gaRR_contactName` | ST / 2 | `gaRRMissionTextPirate1` (T) | `impl/campaign/missions/academy/GARansomResearcher.java` |
| `$gaRR_cost` | S / 5 | `GARRPirateMainOptions` (S) | `$gaRR_cost = $gaRR_costHigh 0` — `GARRPirateMainOptions` |
| `$gaRR_costHigh` | OST / 1 | `GARRPirateMainOptions` (T) | `impl/campaign/missions/academy/GARansomResearcher.java` |
| `$gaRR_costNormal` | OST / 6 | `gaRRMissionTextPirate1` (T) | `impl/campaign/missions/academy/GARansomResearcher.java` |
| `$gaRR_costVeryLow` | OST / 5 | `gaRRPirateThreaten` (T) | `impl/campaign/missions/academy/GARansomResearcher.java` |
| `$gaRR_department` | T / 5 | `gaRRBlurbPirate` (T) | `impl/campaign/missions/academy/GARansomResearcher.java` |
| `$gaRR_marketName` | ST / 5 | `gaRRBlurbPirate` (T) | `impl/campaign/missions/academy/GARansomResearcher.java` |
| `$gaRR_onOrAt` | T / 1 | `gaRRMissionTextPirate1` (T) | `impl/campaign/missions/academy/GARansomResearcher.java` |
| `$gaRR_raidDifficulty` | S / 1 | `GARRPirateMainOptions` (S) | `impl/campaign/missions/academy/GARansomResearcher.java` |
| `$gaRR_researcherName` | T / 3 | `gaRRMissionTextPirate` (T) | `impl/campaign/missions/academy/GARansomResearcher.java` |
| `$gaRR_returnHere` | C / 1 | `gaRRMissionReturn` (C) | `impl/campaign/missions/academy/GARansomResearcher.java` |
| `$gaRR_reward` | ST / 2 | `gaRRMissionTextPirate1` (T) | `impl/campaign/missions/academy/GARansomResearcher.java` |
| `$gaRR_variation` | C / 14 | `gaRRBlurbPirate` (C) | `impl/campaign/missions/academy/GARansomResearcher.java` |
| `$gaTJ_HeOrShe` | T / 3 | `gaTJMissionText4` (T) | `impl/campaign/missions/academy/GATransverseJump.java` |
| `$gaTJ_dist` | T / 1 | `gaTJMissionText4` (T) | `impl/campaign/missions/academy/GATransverseJump.java` |
| `$gaTJ_heOrShe` | T / 7 | `gaTJMissionText3a` (T) | `impl/campaign/missions/academy/GATransverseJump.java` |
| `$gaTJ_himOrHer` | T / 1 | `gaTJPlanetContact` (T) | `impl/campaign/missions/academy/GATransverseJump.java` |
| `$gaTJ_hisOrHer` | T / 3 | `gaTJPlanetContact` (T) | `impl/campaign/missions/academy/GATransverseJump.java` |
| `$gaTJ_needToReturn` | C / 1 | `gaTJMissionReturn` (C) | `impl/campaign/missions/academy/GATransverseJump.java` |
| `$gaTJ_planetName` | ST / 1 | `gaTJMissionText4` (T) | `impl/campaign/missions/academy/GATransverseJump.java` |
| `$gaTJ_researcherName` | T / 6 | `gaTJMissionText4` (T) | `impl/campaign/missions/academy/GATransverseJump.java` |
| `$gaTJ_reward` | ST / 1 | `gaTJMissionText3` (T) | `impl/campaign/missions/academy/GATransverseJump.java` |
| `$gaTJ_systemName` | T / 1 | `gaTJMissionText4` (T) | `impl/campaign/missions/academy/GATransverseJump.java` |
| `$gaTJ_targetPlanet` | C / 1 | `gaTJPlanetInteraction` (C) | `impl/campaign/missions/academy/GATransverseJump.java` |
| `$gaTTB_askedAlready` | CS / 3 | `asebAskedForWorkBairdTalk` (C) | `$gaTTB_askedAlready = true` — `gaDevStartBairdMissions2` |
| `$gaVIP_VIP` | S / 3 | `gaVIP_completeDelivery` (S) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_VIPName` | T / 2 | `gaVIPBlurbMissionTextBasic` (T) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_VIPPost` | T / 1 | `gaVIPBlurbMissionTextBasic` (T) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_VIP_faction` | S / 3 | `gaVIP_completeDelivery` (S) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_VIPhisOrHer` | T / 1 | `gaVIP_checkCompletion` (T) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_encounteredAlready` | CS / 3 | `gaVIPPirateEncounter` (C) | `$gaVIP_encounteredAlready = true` — `gaVIPPirateEncounter` |
| `$gaVIP_event` | T / 1 | `gaVIPBlurbMissionTextBasic` (T) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_kantaConsequences` | C / 1 | `gaVIPRevengeEncounter` (C) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_kantaRelationFirstName` | T / 2 | `gaVIPWorkingForKanta1` (T) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_marketName` | ST / 5 | `gaVIPBlurb` (T) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_merc` | C / 1 | `gaVIPMercEncounter` (C) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_mercFactionId` | S / 1 | `gaVIPMercRefuse` (S) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_mercPayment` | ST / 2 | `gaVIPMercVIPPaymentNegotiate` (T) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_noCompleteShown` | CS / 2 | `gaVIP_checkCompletionCont` (C) | `$gaVIP_noCompleteShown = true 0` — `gaVIP_completedButNoDocking` |
| `$gaVIP_pirate` | C / 1 | `gaVIPPirateEncounter` (C) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_piratePayment` | OS / 2 | `gaVIPPirateOpenComm` (O) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_reward` | ST / 2 | `gaVIPBlurbMissionTextBasic` (T) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_starName` | T / 2 | `gaVIPBlurb` (T) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_subjectRelation` | T / 6 | `gaVIPWorkingForKanta1` (T) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_theMercFaction` | T / 2 | `gaVIPMercOpenComm` (T) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_timeLimit` | ST / 2 | `gaVIPBlurbMissionTextBasic` (T) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaVIP_variation` | C / 3 | `gaVIPBlurbMissionTextBasic` (C) | `impl/campaign/missions/academy/GADeliverVIP.java` |
| `$gaatg_toldFindingLoke` | CS / 2 | `gaATGtellBairdAboutFindingLoke` (C) | `$gaatg_toldFindingLoke = true` — `gaATGtellBairdAboutFindingLoke2` |
| `$gaatg_toldKantaPlan` | CS / 2 | `gaATGtellBairdAboutKantaPlan` (C) | `$gaatg_toldKantaPlan = true` — `gaATGtellBairdAboutKantaPlan2` |
| `$gafcProbe_finishedEncounter` | CS / 5 | `gaFCScavengerGreetingLeaving` (C) | `$gafcProbe_finishedEncounter = true` — `gaFCScavengerDefeated` |
| `$gainedSomeTrust` | CS / 11 | `PKSentinelHubQuestionsInB` (C) | `$gainedSomeTrust = true` — `PKSentinelShowHegCom2` |
| `$gargoyle` | CS / 3 | `gaATGzalMissionStartHub3` (C) | `$gargoyle = true 0` — `gaATGzalMissionHubC` |
| `$gateExploded` | CS / 5 | `gateFlyThroughOpt` (C) | `$gateExploded = true 0` — `devGatesExplodeSel` |
| `$gateHauler` | C / 5 | `gateHaulerStart1` (C) | `impl/campaign/world/GateHaulerLocation.java` |
| `$gateHaulerIceGiant` | C / 1 | `gh_namelessIceGiant` (C) | `impl/campaign/enc/AbyssalRogueStellarObjectEPEC.java`, `impl/campaign/world/GateHaulerLocation.java` |
| `$gateScanDerelict` | C / 2 | `gaATGgateScanDerelictWreckOpen` (C) | `impl/campaign/missions/academy/GAAtTheGates.java` |
| `$gateScanned` | CS / 25 | `gateOpenDialogActive` (C) | `$gateScanned = true` — `gateScanSelFirstTime` |
| `$gaveDatapads` | CS / 2 | `PKSentinelHubShowDataPads` (C) | `$gaveDatapads = true` — `PKSentinelGiveDatapads2` |
| `$gaveDaudOpinion` | CS / 6 | `TseenKeAskDaudOptA` (C) | `$gaveDaudOpinion = true` — `TseenKeAskDaudSelC` |
| `$gaveGloveToBonranew` | CS / 2 | `BFFIpartyOutro1horus2` (C) | `$gaveGloveToBonranew = true 0` — `BFFIapHorus23a` |
| `$gaveINTSECopinion` | CS / 3 | `TseenKeAskDaudOptA` (C) | `$gaveINTSECopinion = true` — `TseenKeAskDaudSelA` |
| `$gaveOpinionOnReynard` | CS / 4 | `HYaribayAskReynardA` (C) | `$gaveOpinionOnReynard = true` — `HYaribayAskReynardRespA` |
| `$gaveSummary` | CS / 19 | `gaDHOendingAskRewardPostOptC` (C) | `$gaveSummary = true 0` — `gaDHOendingFoundFirst` |
| `$gaveSupportRecently` | CS / 5 | `ImoinuKatoUmbraSupportedCheck` (C) | `$gaveSupportRecently = true 30` — `imoinuUmbraOfferHelpCredits` |
| `$genericHail` | C / 1 | `genericHailEncounter` (C) | `impl/campaign/missions/hub/HubMissionWithTriggers.java` |
| `$genericHail_isHailing` | S / 4 | `genericHailEncounter` (S) | `$genericHail_isHailing = true 0` — `genericHailEncounter` |
| `$genericHail_nonHostile` | C / 1 | `genericHailEncounterNonHostile` (C) | `impl/campaign/missions/hub/HubMissionWithTriggers.java` |
| `$getOut` | CS / 7 | `gaATGbairdEndingHubOptA` (C) | `$getOut = true 0` — `gaATGbairdEndingReap` |
| `$global.BFFIcommittedToRaid` | CS / 4 | `BFFIraidGoBackOverride` (C) | `$global.BFFIcommittedToRaid = true 0` — `BFFIpartyBeginRaid` |
| `$global.SDTU_barRaidDelay` | CS / 2 | `sdtuStart` (C) | `$global.SDTU_barRaidDelay = true 3` — `sdBarRaid0` |
| `$global.ZGRcommentedOnArroyo` | CS / 2 | `ArroyoAskAboutZGRoptB` (C) | `$global.ZGRcommentedOnArroyo = true` — `ZGRacknowledgeArroyo` |
| `$global.affk_inProgress` | C / 1 | `KantaFavorCourierHail` (C) | Trace owning rule/command or generated interaction data |
| `$global.affk_ref` | CS / 3 | `KantaFavorCourierHail` (C) | Trace owning rule/command or generated interaction data |
| `$global.angeredDardanKato` | CS / 4 | `ImoinuKatoUmbraDardanOptA` (C) | `$global.angeredDardanKato = true` — `LKEmazDKYaribayEnd` |
| `$global.anh_chargeMore` | CS / 3 | `anhRewardHigh` (C) | `$global.anh_chargeMore = true` — `anhCantinaChargeMore` |
| `$global.anh_completed` | CS / 5 | `ImoinuUmbraGoOptD` (C) | `$global.anh_completed = true` — `anhDiktatPatrolQuestEnd` |
| `$global.anh_deliveredBoth` | CS / 2 | `ImoinuUmbraGoOptD` (C) | `$global.anh_deliveredBoth = true` — `anhCheckCompletion` |
| `$global.anh_deliveredKid` | CS / 2 | `ImoinuUmbraGoOptE` (C) | `$global.anh_deliveredKid = true` — `anhCheckCompletionOnlyKid` |
| `$global.anh_diktatPatrolBluffed` | S / 1 | `anhDiktatPatrolBluffEnd` (S) | `$global.anh_diktatPatrolBluffed = true` — `anhDiktatPatrolBluffEnd` |
| `$global.anh_diktatPatrolHostile` | S / 1 | `anhDiktatPatrolHostileEnd` (S) | `$global.anh_diktatPatrolHostile = true` — `anhDiktatPatrolHostileEnd` |
| `$global.anh_handedOverEveryone` | S / 1 | `anhDiktatPatrolBothEnd` (S) | `$global.anh_handedOverEveryone = true` — `anhDiktatPatrolBothEnd` |
| `$global.anh_handedOverRobedMan` | CS / 2 | `anhCheckCompletionOnlyKid` (C) | `$global.anh_handedOverRobedMan = true` — `anhDiktatPatrolMixedEnd` |
| `$global.anh_inProgress` | C / 2 | `devAnhStartOption` (C) | Trace owning rule/command or generated interaction data |
| `$global.anh_missionCompleted` | C / 2 | `devAnhStartOption` (C) | Trace owning rule/command or generated interaction data |
| `$global.anh_ref` | CS / 8 | `anhDiktatPatrolGreeting` (C) | Trace owning rule/command or generated interaction data |
| `$global.asebSayBairdWantsToTalk` | CS / 3 | `asebAskedForWorkBairdTalk` (C) | Trace owning rule/command or generated interaction data |
| `$global.askedVirensToSetUpCottonMeeting` | S / 1 | `LKEvirensRaidCotton3` (S) | `$global.askedVirensToSetUpCottonMeeting = true 0` — `LKEvirensRaidCotton3` |
| `$global.autoOpenBuyShipsTab` | S / 2 | `orbitalStorageInteractionFleet1` (S) | `$global.autoOpenBuyShipsTab = true 0` — `orbitalStorageInteractionFleet1` |
| `$global.bairdToldPlayerToKeepZig` | CS / 2 | `gaPZ_gargoylePost` (C) | `$global.bairdToldPlayerToKeepZig = true` — `gaPZ_return0` |
| `$global.bairdWantsToTalk` | CS / 3 | `bairdIntroGreeting` (C) | Trace owning rule/command or generated interaction data |
| `$global.bairdWillTalk` | CS / 8 | `bairdGreetingGoAway` (C) | `$global.bairdWillTalk = true` — `gaDevStartBairdMissions2` |
| `$global.bffi_arrestTheCurate` | CS / 4 | `BFFIpatherStationDefeated` (C) | `$global.bffi_arrestTheCurate = true` — `BFINdevSetStartCedra` |
| `$global.bffi_arrestedKeepfaith` | CS / 9 | `BFFIdeliverToOakBody1howAkeep` (C) | `$global.bffi_arrestedKeepfaith = true` — `BFFIarrestOutro1` |
| `$global.bffi_bornanewLocation` | S / 2 | `BFFIaccept1` (S) | `$global.bffi_bornanewLocation = $market.id` — `BFFIaccept1` |
| `$global.bffi_completed` | S / 5 | `BFFIproblemCheckOutDead` (S) | `$global.bffi_completed = true` — `BFFIproblemCheckOutDead` |
| `$global.bffi_destroyedStationEarly` | CS / 3 | `BFFIdeliverToOakKeepfaithCheckB` (C) | `$global.bffi_destroyedStationEarly = true` — `BFFIpatherStationDefeated` |
| `$global.bffi_followUlmusPond` | S / 3 | `BFFIulmusPondNotHereOut` (S) | `$global.bffi_followUlmusPond = true` — `BFFIulmusPondNotHereOut` |
| `$global.bffi_goMeetMenesYaribay` | S / 4 | `BFFIonBoardEnd` (S) | `$global.bffi_goMeetMenesYaribay = true` — `BFFIonBoardEnd` |
| `$global.bffi_goTalkToHorus` | S / 1 | `BFFImmBornDoInviteHorus` (S) | `$global.bffi_goTalkToHorus = true` — `BFFImmBornDoInviteHorus` |
| `$global.bffi_gotPartyInvite` | S / 7 | `BFFImeetMenesEnd` (S) | `$global.bffi_gotPartyInvite = true` — `BFFImeetMenesEnd` |
| `$global.bffi_horusToParty` | CS / 3 | `BFFIattendPartyStartOption` (C) | `$global.bffi_horusToParty = true` — `BFFItalkHorusPartyAttendEnd` |
| `$global.bffi_inProgress` | C / 12 | `BFINstart` (C) | Trace owning rule/command or generated interaction data |
| `$global.bffi_intendToBeBuyer` | CS / 3 | `BFFImpBuyerStart` (C) | `$global.bffi_intendToBeBuyer = true` — `BFFImmBornDoBuyer` |
| `$global.bffi_intendToRaid` | CS / 5 | `BFFIapStartOptionRaidCheck1` (C) | `$global.bffi_intendToRaid = true` — `BFFImmBornDoRaid3` |
| `$global.bffi_intendToSteal` | CS / 3 | `BFFImpStealStart` (C) | `$global.bffi_intendToSteal = true` — `BFFImmBornDoSteal` |
| `$global.bffi_intendToTalkHorus` | CS / 4 | `BFFIpartyOutro1horus` (C) | `$global.bffi_intendToTalkHorus = true` — `BFFImmBornDoInviteHorus` |
| `$global.bffi_investigatePatherStation` | S / 3 | `BFINdevSetStartStation` (S) | `$global.bffi_investigatePatherStation = true` — `BFINdevSetStartStation` |
| `$global.bffi_jethroCalledOutPond` | CS / 6 | `BFFItpostEngJethroTalk2` (C) | `$global.bffi_jethroCalledOutPond = true` — `BFFImpBuyerOutro2a` |
| `$global.bffi_jethroCalledOutPondEarly` | CS / 3 | `BFFIpartyOutro1buyerCheckA` (C) | `$global.bffi_jethroCalledOutPondEarly = true` — `BFFImpBuyerOutro2a` |
| `$global.bffi_keepfaithEscaped` | CS / 11 | `BFFIdeliverToOakJustBorn` (C) | `$global.bffi_keepfaithEscaped = true` — `BFFIpatherStationDefeated` |
| `$global.bffi_keepfaithEscapedTartessus` | CS / 9 | `BFFIoakKeepfaithEscape` (C) | `$global.bffi_keepfaithEscapedTartessus = true` — `BFFIarrestOutroInjured2escape` |
| `$global.bffi_learnedAboutKeepfaith` | S / 5 | `BFFIarrestOutro3` (S) | Trace owning rule/command or generated interaction data |
| `$global.bffi_missionCompleted` | C / 2 | `BFINstart` (C) | Trace owning rule/command or generated interaction data |
| `$global.bffi_pondFateKilledBySedge` | S / 1 | `BFFIulmusSedge3` (S) | `$global.bffi_pondFateKilledBySedge = true` — `BFFIulmusSedge3` |
| `$global.bffi_preproposedBuyerOption` | CS / 4 | `BFFImmBornBuyerOpt` (C) | `$global.bffi_preproposedBuyerOption = true` — `BFFIonBoardPlanOptsBuy` |
| `$global.bffi_raidedOlinaduForGlove` | S / 1 | `BFFIapRaidOutro3` (S) | `$global.bffi_raidedOlinaduForGlove = true` — `BFFIapRaidOutro3` |
| `$global.bffi_raidedPatherStation` | S / 1 | `BFFIpatherStationRaidOutro2` (S) | `$global.bffi_raidedPatherStation = true` — `BFFIpatherStationRaidOutro2` |
| `$global.bffi_ref` | CS / 65 | `BFFImeetMenesStart` (C) | Trace owning rule/command or generated interaction data |
| `$global.bffi_returnBodyWithoutPrisoner` | CS / 13 | `BFFIoakBornanewDead` (C) | `$global.bffi_returnBodyWithoutPrisoner = true` — `BFFIarrestOutroDead3` |
| `$global.bffi_returnBornanewBody` | CS / 30 | `BFFIoakKeepfaithEscape` (C) | `$global.bffi_returnBornanewBody = true` — `BFFIarrestOutroDead3` |
| `$global.bffi_stoleRelic` | S / 1 | `BFFIpartyOutro3steal` (S) | `$global.bffi_stoleRelic = true` — `BFFIpartyOutro3steal` |
| `$global.bffi_talkToEngineer` | S / 5 | `BFFIapRaidOutro5` (S) | `$global.bffi_talkToEngineer = true` — `BFFIapRaidOutro5` |
| `$global.bffi_talkToUlmusPond` | S / 2 | `BFFItpostEngJethroTalk4` (S) | `$global.bffi_talkToUlmusPond = true` — `BFFItpostEngJethroTalk4` |
| `$global.bfin_delay` | CS / 2 | `BFINstart` (C) | `$global.bfin_delay = true 7` — `LKEdoFinish` |
| `$global.bfin_didStart` | CS / 2 | `BFINstart` (C) | `$global.bfin_didStart = true` — `BFINstart0` |
| `$global.bfin_missionCompleted` | CS / 4 | `BFINcontactBornanew` (C) | `$global.bfin_missionCompleted = true` — `BFINdevSetStartStation` |
| `$global.bfin_saidNotNow` | CS / 9 | `BFINconvOptRemindReward` (C) | `$global.bfin_saidNotNow = true` — `BFINnotNow` |
| `$global.bornanewBadBoy` | CS / 35 | `BFFIpsaConHubOptRespA1bb` (C) | `$global.bornanewBadBoy = 4` — `BFINdevSetBornanewBadBoy` |
| `$global.bornanewChokedPond` | CS / 6 | `BFFIulmusTalkPcWrong3a` (C) | `$global.bornanewChokedPond = true` — `BFFIulmusTalkPcWrong2` |
| `$global.bornanewShotDuringBFFI` | CS / 7 | `BFFIpsApproachBornMedbay` (C) | `$global.bornanewShotDuringBFFI = true` — `BFFIulmusSedge4` |
| `$global.bornanewShotOnTartessus` | S / 1 | `BFFIarrestShotBorn0` (S) | `$global.bornanewShotOnTartessus = true` — `BFFIarrestShotBorn0` |
| `$global.bornanewWasShotBySedge` | CST / 16 | `BFFIarrest2dev` (T) | `$global.bornanewWasShotBySedge = true` — `BFFIulmusSedge6` |
| `$global.canScanGates` | CS / 17 | `gateOpenDialogCanScan1` (C) | `$global.canScanGates = true` — `devGateCanScanOnSel` |
| `$global.ciFinished` | CS / 2 | `customsInspectionWaitFinished` (C) | Trace owning rule/command or generated interaction data |
| `$global.ciInProgress` | S / 1 | `customsInspectionAgree` (S) | Trace owning rule/command or generated interaction data |
| `$global.ciInterrupted` | S / 1 | `customsInspectionAgree` (S) | Trace owning rule/command or generated interaction data |
| `$global.ciWait` | S / 1 | `customsInspectionAgree` (S) | Trace owning rule/command or generated interaction data |
| `$global.core_pkNexus` | C / 1 | `PKCommRelayOpt` (C) | Trace owning rule/command or generated interaction data |
| `$global.customsInspectionFactionId` | C / 1 | `marketPostOpenCustomsInProgress` (C) | Trace owning rule/command or generated interaction data |
| `$global.daysSinceStart` | CS / 13 | `lppJangalaShrineProtest` (C) | Trace owning rule/command or generated interaction data |
| `$global.defeatedDerelictStr` | C / 1 | `sal_derelictDefConstructionInfo` (C) | Trace owning rule/command or generated interaction data |
| `$global.defeatedZiggurat` | C / 1 | `gaPZIntro` (C) | Trace owning rule/command or generated interaction data |
| `$global.deliveredInfirmFromSentinel` | S / 2 | `PKTellAboutSentinelSelLetfCrew2` (S) | `$global.deliveredInfirmFromSentinel = true` — `PKTellAboutSentinelSelLetfCrew2` |
| `$global.didCoilGunRant` | CS / 1 | `cpcOfferTextBarCoilGun` (C) | `$global.didCoilGunRant = true` — `cpcOfferTextBarCoilGun` |
| `$global.didEventideRaoBall` | C / 1 | `soeMeetCaspianStart0` (C) | Trace owning rule/command or generated interaction data |
| `$global.didKantasDenIntro` | CS / 8 | `kantasDenVisitOptionGAATG` (C) | `$global.didKantasDenIntro = true` — `kdKantaFirstIntro5` |
| `$global.didPatherBarAssassin` | CS / 2 | `lpBarAssassinStart` (C) | `$global.didPatherBarAssassin = true` — `lpBarAssassinLeave` |
| `$global.didSDBarRaid` | CS / 4 | `sdBarRaidStart` (C) | `$global.didSDBarRaid = true` — `sdBarRaidLeave` |
| `$global.didTTrepBribe` | CS / 6 | `ttContactBribeRep1mOffer` (C) | `$global.didTTrepBribe++` — `ttContactBribeRep500kAccept` |
| `$global.discoveredSentinel` | CS / 6 | `PKSentinelDockyardFollowupB` (C) | `$global.discoveredSentinel = true` — `PKSentinelOutSequence` |
| `$global.firstGlamorRotanevVisitInvite` | CS / 3 | `ZGRfirstVisitOption` (C) | `$global.firstGlamorRotanevVisitInvite = true` — `ZGRpreVisitResp1` |
| `$global.foundCoureuse` | CS / 3 | `gaKAReturnToAcademy8NoCoureuse` (C) | `$global.foundCoureuse = true` — `gaFCReturnToAcademy` |
| `$global.foundGAabyssExpedition` | S / 1 | `GS_ACADEMY_cont1Sel` (S) | `$global.foundGAabyssExpedition = true` — `GS_ACADEMY_cont1Sel` |
| `$global.foundGargoyle` | CS / 3 | `gaFCReturnToAcademy7a` (C) | `$global.foundGargoyle = true` — `gaKAReturnToAcademy` |
| `$global.foundHamatsu` | CS / 2 | `gaRH_noHamatsuOption` (C) | `$global.foundHamatsu = true` — `gaPZ_hamatsuWreckOpen` |
| `$global.foundOneslaught` | CS / 35 | `oyaTanaicaVambrace10optsA` (C) | `$global.foundOneslaught = true` — `mk1_exploreFirstTime` |
| `$global.foundZGRabyssMercs` | CS / 15 | `RayanArroyoAskAboutZGRoptB` (C) | `$global.foundZGRabyssMercs = true` — `GS_ZGR_MERC_cont1Sel` |
| `$global.gaATG_completed` | S / 1 | `gaATGconclusion` (S) | `$global.gaATG_completed = true` — `gaATGconclusion` |
| `$global.gaATG_didRemoteFirstJanusTest` | C / 1 | `gaATGreturnAfterJanusFail5b` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaATG_findingLoke` | CS / 3 | `gaATGtellBairdAboutFindingLoke` (C) | `$global.gaATG_findingLoke = true` — `gaATGkantaWrapUp2` |
| `$global.gaATG_firstJanusResults` | CS / 3 | `gaATGreturnAfterJanusFail` (C) | `$global.gaATG_firstJanusResults = true` — `gaATGgateJanusUseResult` |
| `$global.gaATG_foundLoke` | CS / 5 | `gaATGkantaRaidFinished4a` (C) | `$global.gaATG_foundLoke = true` — `gaATGcottonRaidFinishedB` |
| `$global.gaATG_foundZal` | CS / 8 | `gaATGraidKantasDenOptionA` (C) | `$global.gaATG_foundZal = true` — `gaATGkantaRaidFinishedB` |
| `$global.gaATG_getMissionFromCoureuse` | CS / 4 | `gaATGreturnAfterJanusFail` (C) | `$global.gaATG_getMissionFromCoureuse = true` — `gaATGreturnAfterJanusFail8` |
| `$global.gaATG_goTalkToDaud` | CS / 3 | `gaATGdaudMeetingSetup` (C) | `$global.gaATG_goTalkToDaud = true` — `gaATGcontactYaribay4` |
| `$global.gaATG_goTalkToYaribay` | CS / 3 | `gaATGcontactYaribayStart` (C) | `$global.gaATG_goTalkToYaribay = true` — `gaATGgetScanner5` |
| `$global.gaATG_goToDaudMeeting` | CS / 3 | `gaATGdaudMeetingOption` (C) | `$global.gaATG_goToDaudMeeting = true` — `gaATGdaudMeetingSetupEnd` |
| `$global.gaATG_gotDaudDeal` | CS / 4 | `gaATGtalkToBairdNeedDeal` (C) | `$global.gaATG_gotDaudDeal = true` — `gaATGdaudMeeting14` |
| `$global.gaATG_gotKantaToken` | CS / 3 | `gaATGraidKantasDenOptionA` (C) | `$global.gaATG_gotKantaToken = true` — `gaATGzalMissionHubD2` |
| `$global.gaATG_gotLokeViaRaid` | CS / 4 | `lkeChalcedonGreetLokeRaid` (C) | `$global.gaATG_gotLokeViaRaid = true` — `gaATGcottonRaidFinishedB` |
| `$global.gaATG_gotZalViaRaid` | CS / 3 | `kpAudience1StoleZal` (C) | `$global.gaATG_gotZalViaRaid = true` — `gaATGkantaRaidFinishedB` |
| `$global.gaATG_inProgress` | C / 12 | `gateScanSelFirstTime` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaATG_missionCompleted` | CS / 12 | `LKEjethroAngelsTalkTellGatesA` (C) | `$global.gaATG_missionCompleted = true` — `gaATGdevComplete2` |
| `$global.gaATG_missionGiven` | CS / 2 | `gaATGIntro` (C) | `$global.gaATG_missionGiven = true` — `gaATGIntroWrap1` |
| `$global.gaATG_nowGoScan` | S / 1 | `gaATGjanusPrototype4` (S) | Trace owning rule/command or generated interaction data |
| `$global.gaATG_ref` | CS / 56 | `gaATGgateScanEndCheck1` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaATG_returnWithDeal` | CS / 3 | `YaribayFollowupNotDone` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaATG_returnWithScans` | S / 1 | `gaATGjanusPrototype4` (S) | Trace owning rule/command or generated interaction data |
| `$global.gaATG_returnedLoke` | CS / 1 | `gaATGreturnLokeStart` (C) | `$global.gaATG_returnedLoke = true` — `gaATGreturnLokeStart` |
| `$global.gaATG_scannedSixGates` | S / 2 | `gaATGgateScanEndCheck1` (S) | `$global.gaATG_scannedSixGates = true` — `gaATGgateScanEndCheck1` |
| `$global.gaATG_useJanusPrototype` | CS / 10 | `gaATGtalkToBairdNeedDeal` (C) | `$global.gaATG_useJanusPrototype = true` — `gaATGjanusPrototype4` |
| `$global.gaATG_usedMagecGateToEscape` | CS / 4 | `gaATGlastReturnStart1` (C) | `$global.gaATG_usedMagecGateToEscape = true` — `gaATGMagecGateUse2` |
| `$global.gaATG_workingForKanta` | CS / 5 | `gaATGWorkingForKanta` (C) | `$global.gaATG_workingForKanta = true` — `gaATGkantaWrapUp2` |
| `$global.gaBA_completed` | S / 1 | `gaBAMissionReturn` (S) | `$global.gaBA_completed = true` — `gaBAMissionReturn` |
| `$global.gaBA_failed` | S / 1 | `GABAHandedOverItemFailedMission` (S) | `$global.gaBA_failed = true` — `GABAHandedOverItemFailedMission` |
| `$global.gaBA_failedCredits` | CS / 1 | `gaBAMissionReturnPlayerOwes` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaBA_failedItem` | ST / 1 | `gaBAMissionReturnPlayerOwes` (T) | Trace owning rule/command or generated interaction data |
| `$global.gaBA_needToReturn` | S / 8 | `gaBAPatherRaidFinished` (S) | `$global.gaBA_needToReturn = true` — `gaBAPatherRaidFinished` |
| `$global.gaBA_playerOwes` | CS / 1 | `gaBAMissionReturnPlayerOwes` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaBA_ref` | CS / 14 | `gaBAPatherGreeting` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaCO_gaveScannerToPathers` | S / 1 | `gaCOPatherReject1` (S) | `$global.gaCO_gaveScannerToPathers = true` — `gaCOPatherReject1` |
| `$global.gaCO_ref` | CS / 10 | `gaCOPrintHostilesText` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaCO_scanCompleted` | S / 1 | `gaCOObjectScan` (S) | `$global.gaCO_scanCompleted = true` — `gaCOObjectScan` |
| `$global.gaDA_gotWidget` | CS / 7 | `gaDAFleetWithWidgetDefeated` (C) | `$global.gaDA_gotWidget = true` — `gaDADerelictBasic1` |
| `$global.gaDA_piratesTookIt` | S / 1 | `gaDADerelictContPirates` (S) | `$global.gaDA_piratesTookIt = true` — `gaDADerelictContPirates` |
| `$global.gaDA_ref` | CS / 18 | `gaDAMissionReturn` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaDA_returnedWidget` | S / 1 | `gaDAMissionReturn` (S) | `$global.gaDA_returnedWidget = true` — `gaDAMissionReturn` |
| `$global.gaDA_scavengerTookIt` | S / 1 | `gaDADerelictScavengerCont` (S) | `$global.gaDA_scavengerTookIt = true` — `gaDADerelictScavengerCont` |
| `$global.gaDHO_arrayFirstThenAbyss` | CS / 11 | `gaDHOendingHeBelieves3aAbyss` (C) | `$global.gaDHO_arrayFirstThenAbyss = true` — `gaDHOjustFoundArrayEndAbyss` |
| `$global.gaDHO_arrayFirstThenElek` | S / 1 | `gaDHOjustFoundArrayEndElek` (S) | `$global.gaDHO_arrayFirstThenElek = true` — `gaDHOjustFoundArrayEndElek` |
| `$global.gaDHO_completed` | S / 6 | `gaDHO_mk1explore8interruptOutB` (S) | `$global.gaDHO_completed = true` — `gaDHO_mk1explore8interruptOutB` |
| `$global.gaDHO_declinedTwice` | S / 1 | `gaDHOvisitElekAgainDecline` (S) | `$global.gaDHO_declinedTwice = true` — `gaDHOvisitElekAgainDecline` |
| `$global.gaDHO_didInvite` | CS / 4 | `gaDHOhookStartDev` (C) | `$global.gaDHO_didInvite= true` — `gaDHOhook0` |
| `$global.gaDHO_didInviteDEV` | CS / 2 | `gaDHOhookStartDev` (C) | `$global.gaDHO_didInviteDEV = true` — `gaDevStartGADHO2` |
| `$global.gaDHO_elekMentionedPatron` | CS / 4 | `gaDHO_mk1explore8interrupt3c` (C) | `$global.gaDHO_elekMentionedPatron = true` — `gaDHOhook2` |
| `$global.gaDHO_elekRevealedTriTachyonAsPatron` | CS / 9 | `ZGRtalkPostGADHOimplyOpt` (C) | `$global.gaDHO_elekRevealedTriTachyonAsPatron = true` — `gaDHOvisitElekHubSelA4b` |
| `$global.gaDHO_elekRevealedZGRasPatron` | CS / 11 | `ZGRpostGADHOconfrontOpt` (C) | `$global.gaDHO_elekRevealedZGRasPatron = true` — `gaDHOvisitElekHubSelA6b` |
| `$global.gaDHO_foundArrayFirst` | CS / 7 | `gaDHOvisitElekStart2b` (C) | `$global.gaDHO_foundArrayFirst = true` — `gaDHOjustFoundArray5` |
| `$global.gaDHO_foundOneslaught` | S / 2 | `gaDHO_mk1explore8interrupt2b` (S) | `$global.gaDHO_foundOneslaught = true` — `gaDHO_mk1explore8interrupt2b` |
| `$global.gaDHO_foundRock` | S / 1 | `gaDHOnamelessRock2` (S) | `$global.gaDHO_foundRock = true` — `gaDHOnamelessRock2` |
| `$global.gaDHO_gotCoordinates` | S / 2 | `gaDHOarray5` (S) | `$global.gaDHO_gotCoordinates = true` — `gaDHOarray5` |
| `$global.gaDHO_gotReward` | CS / 21 | `gaDHOendingRewardFirstB` (C) | `$global.gaDHO_gotReward = true` — `gaDHOendingAskReward` |
| `$global.gaDHO_gotRewardAgain` | C / 1 | `gaDHOendingDataTransferOptHaz` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaDHO_gotScanPackage` | S / 2 | `gaDHOvisitElekAccept` (S) | `$global.gaDHO_gotScanPackage = true` — `gaDHOvisitElekAccept` |
| `$global.gaDHO_inProgress` | C / 8 | `gaDHOvisitElekStart` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaDHO_missionCompleted` | C / 2 | `ZGRstartPitch4OMKIelek` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaDHO_playerAgreedTo100k` | CS / 5 | `gaDHOendingAskReward100k` (C) | `$global.gaDHO_playerAgreedTo100k = true` — `gaDHOvisitElekHubSelA5b` |
| `$global.gaDHO_playerAgreedTo200k` | CS / 7 | `gaDHOendingAskReward200k` (C) | `$global.gaDHO_playerAgreedTo200k = true` — `gaDHOvisitElekHubSelA6a` |
| `$global.gaDHO_playerAgreedTo20k` | CS / 4 | `gaDHOendingAskReward20k` (C) | `$global.gaDHO_playerAgreedTo20k = true` — `gaDHOvisitElekHubSelA2a` |
| `$global.gaDHO_playerAgreedTo50k` | CS / 4 | `gaDHOendingAskReward50k` (C) | `$global.gaDHO_playerAgreedTo50k = true` — `gaDHOvisitElekHubSelA4a` |
| `$global.gaDHO_playerMadeElekMad` | CS / 5 | `gaDHO_mk1explore8interruptC` (C) | `$global.gaDHO_playerMadeElekMad = true` — `gaDHOvisitElekAltIntroPiracyOutA` |
| `$global.gaDHO_ref` | CS / 33 | `gaDHOvisitElekStart` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaDHO_rejectedOffer` | CS / 5 | `gaDHOvisitElekStart` (C) | `$global.gaDHO_rejectedOffer = true` — `gaDHOvisitElekNotInterested` |
| `$global.gaDHO_turnedDownPayment` | CS / 6 | `gaDHOendingDataTransferOptB` (C) | `$global.gaDHO_turnedDownPayment = true` — `gaDHOvisitElekAltNoPayment` |
| `$global.gaData_gaveCoordsToPirates` | S / 2 | `gaDataGiveCoordsSel` (S) | `$global.gaData_gaveCoordsToPirates = true` — `gaDataGiveCoordsSel` |
| `$global.gaData_gotData` | CS / 7 | `gaDataPirateConvBeforeGotData` (C) | `$global.gaData_gotData = true` — `gaDataFinishedRaid` |
| `$global.gaData_ref` | CS / 11 | `gaDataPlanetInteraction` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaFC_archonPLin` | CS / 6 | `gaFCArchonSearchHubOptionPL` (C) | `$global.gaFC_archonPLin = true` — `gaFCarchPLexcuse` |
| `$global.gaFC_beingConspicuous` | CS / 11 | `gaFCFikenhildCavinKnock1` (C) | `$global.gaFC_beingConspicuous++` — `gaFCFikenhildBotherAdmin` |
| `$global.gaFC_calledFavorFromVIP` | CS / 3 | `gaFCFikenhildCavinStartInterruptPL2` (C) | `$global.gaFC_calledFavorFromVIP = true` — `gaFCFikenhildBotherVIPoptA4yes` |
| `$global.gaFC_completed` | S / 2 | `gaFCReturnToAcademy9a` (S) | `$global.gaFC_completed = true` — `gaFCReturnToAcademy9a` |
| `$global.gaFC_discoveredFirstProbe` | CS / 2 | `gaFCProbeObjectInteractionFirst` (C) | `$global.gaFC_discoveredFirstProbe = true` — `gaFCProbeObjectInteractionFirst` |
| `$global.gaFC_foundGroombridgeRelay` | CS / 6 | `gaFCGroombridgeSearchStart` (C) | `$global.gaFC_foundGroombridgeRelay = true` — `gaFCGroombridgeSearch5` |
| `$global.gaFC_gaveZalContactToSiyavong` | S / 1 | `GAFCSiyavongFikenhildZal` (S) | `$global.gaFC_gaveZalContactToSiyavong = true` — `GAFCSiyavongFikenhildZal` |
| `$global.gaFC_ghostStory` | CS / 4 | `gaFCGroombridgeSearchBStart` (C) | `$global.gaFC_ghostStory = true` — `gaFCAskAroundLaicailleBar6` |
| `$global.gaFC_gotAlamangForScylla` | CS / 7 | `gaFCLaicaileVisitStartAlamangYes` (C) | `$global.gaFC_gotAlamangForScylla = true` — `gaFCZalRecordingGetTheKrillPaste` |
| `$global.gaFC_gotIsirahLeadFromProbes` | CS / 3 | `gaFCGroombridgeSearchStart` (C) | `$global.gaFC_gotIsirahLeadFromProbes = true` — `gaFCProbeInvestigaitonWrapUp1` |
| `$global.gaFC_gotIsirahLeadFromSiyavong` | CS / 8 | `gaFCZalFirstGreeting_INVESTIGATE_FIKENHILD2` (C) | `$global.gaFC_gotIsirahLeadFromSiyavong = true` — `GAFCSiyavongFikenhildSerious` |
| `$global.gaFC_gotIsirahLeadFromZal` | CS / 2 | `GAFCSiyavongFirstGreeting_INVESTIGATE_FIKENHILD` (C) | `$global.gaFC_gotIsirahLeadFromZal = true` — `gaFCZalLeave` |
| `$global.gaFC_gotZalContactFromCavin` | CS / 10 | `gaFCFikenhildHubOption6b` (C) | `$global.gaFC_gotZalContactFromCavin = true` — `gaFCFikenhildCavinB2` |
| `$global.gaFC_hackedIsirahRelay` | CS / 4 | `gaFCAskLaicailleBar1Option1` (C) | `$global.gaFC_hackedIsirahRelay = true` — `gaFC_relayInstallHack4` |
| `$global.gaFC_inProgress` | C / 1 | `GAFCSiyavongStonewallGreetOption1` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaFC_isirahClues` | CS / 20 | `gaFCSearchIsirahSummaryComms2` (C) | `$global.gaFC_isirahClues++` — `GAFCSiyavongFikenhildSerious` |
| `$global.gaFC_knockedAnyway` | CS / 5 | `GAFCSiyavongFikenhildGreet1` (C) | `$global.gaFC_knockedAnyway = true` — `gaFCFikenhildCavinKnockAnyway` |
| `$global.gaFC_knowAboutLeagueOp` | CS / 5 | `gaFCKapteynBotherAdministrator` (C) | `$global.gaFC_knowAboutLeagueOp = true` — `gaFC_kapteynBossInfoDump4` |
| `$global.gaFC_knowElissasName` | CS / 9 | `gaFCQuestionBioneElissaZal` (C) | `$global.gaFC_knowElissasName = true` — `gaFCFikenhildAdonyaB2` |
| `$global.gaFC_knowFinlaysName` | CS / 8 | `GAFCQuestionBioneAgentName3` (C) | `$global.gaFC_knowFinlaysName = true` — `gaFCFikenhildAdonyaA` |
| `$global.gaFC_knowSiyavongContact` | CS / 12 | `gaFCFikenhildHubOption6b` (C) | `$global.gaFC_knowSiyavongContact = true` — `gaFCFikenhildBioneB2` |
| `$global.gaFC_knowWhereScyllaIs` | CS / 7 | `gaFCArchonSearchHubOption3` (C) | `$global.gaFC_knowWhereScyllaIs = true` — `gaFCGroombridgeSearch5` |
| `$global.gaFC_madeSiyavongAngry` | CS / 7 | `GAFCSiyavongFikenhildGreet1` (C) | `$global.gaFC_madeSiyavongAngry = true` — `gaFCFikenhildCavinC2` |
| `$global.gaFC_missionCompleted` | CS / 4 | `gaPZIntro` (C) | `$global.gaFC_missionCompleted = true` — `gaATGdevstart2` |
| `$global.gaFC_pickedBranchFikenhild` | CS / 10 | `gaFCFikenhildHubOption1` (C) | `$global.gaFC_pickedBranchFikenhild = true` — `bairdIntroScylla2choiceB` |
| `$global.gaFC_pickedBranchProbes` | S / 3 | `bairdIntroScylla2choiceA` (S) | `$global.gaFC_pickedBranchProbes = true` — `bairdIntroScylla2choiceA` |
| `$global.gaFC_probesDone` | CS / 8 | `gaFCProbe1InvestigationStart` (C) | `$global.gaFC_probesDone++` — `gaFCProbe1Investigation3` |
| `$global.gaFC_ref` | CS / 50 | `GAFCBairdGreeting_INVESTIGATE_FIKENHILD` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaFC_returnToAcademy` | S / 1 | `gaFCLaicailleEnd2` (S) | `$global.gaFC_returnToAcademy = true` — `gaFCLaicailleEnd2` |
| `$global.gaFC_scavengerDroppedProbe` | CS / 6 | `gaFCProbeEmpty1` (C) | `$global.gaFC_scavengerDroppedProbe = true` — `gaFCScavengerDefeated` |
| `$global.gaFC_searchIsirah` | CS / 19 | `gaFCGroombridgeSearchStart` (C) | `$global.gaFC_searchIsirah = true` — `gaFCZalLeave` |
| `$global.gaFC_soldOutCoureuseIsirah` | S / 1 | `gaFCIsirahMercAcceptDeal` (S) | `$global.gaFC_soldOutCoureuseIsirah = true` — `gaFCIsirahMercAcceptDeal` |
| `$global.gaFC_switchedPaths` | CS / 5 | `GAFCBairdGreeting_INVESTIGATE_FIKENHILD` (C) | `$global.gaFC_switchedPaths = true` — `GAFCBairdSwitchToProbesFinal` |
| `$global.gaFC_triedToSeeCavin` | CS / 3 | `GAFCSiyavongFirstGreeting_INVESTIGATE_FIKENHILD` (C) | `$global.gaFC_triedToSeeCavin = true` — `gaFCFikenhildCavinStart` |
| `$global.gaFC_triggerPatherAmbush` | S / 1 | `gaFCScavengerWait3` (S) | `$global.gaFC_triggerPatherAmbush = true` — `gaFCScavengerWait3` |
| `$global.gaFC_visitCoureuse` | CS / 4 | `gaFCArchonFirstContact` (C) | `$global.gaFC_visitCoureuse = true` — `gaFCArchonSuccessEnd` |
| `$global.gaIntro2_completed` | S / 2 | `gaIntro2returnElekReward` (S) | `$global.gaIntro2_completed = true 0` — `gaIntro2returnElekReward` |
| `$global.gaIntro2_ref` | CS / 4 | `gaIntro2returnStart` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaIntro2found` | CS / 3 | `gaIntro2surveyOpen` (C) | `$global.gaIntro2found = true` — `gaIntro2surveyOpen` |
| `$global.gaIntro_ref` | S / 1 | `gaRequestMeeting` (S) | Trace owning rule/command or generated interaction data |
| `$global.gaKA_completed` | CS / 3 | `relLevelIndieBaird` (C) | `$global.gaKA_completed = true` — `gaKAReturnToAcademy10a` |
| `$global.gaKA_getHackHardware` | S / 1 | `gaKAGargoyleCont8` (S) | `$global.gaKA_getHackHardware = true` — `gaKAGargoyleCont8` |
| `$global.gaKA_hegemonyVisit` | CS / 1 | `gaKA_hegemonyVisitEvent` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaKA_installHack` | S / 1 | `gaKANewMaxiosVisit5` (S) | `$global.gaKA_installHack = true` — `gaKANewMaxiosVisit5` |
| `$global.gaKA_missionCompleted` | CS / 8 | `gaKACoureuseBairdOpt` (C) | `$global.gaKA_missionCompleted = true` — `gaATGdevstart2` |
| `$global.gaKA_ref` | CS / 17 | `gaKAPatrolEncounter` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaKA_retrieveArchive` | S / 1 | `gaKA_relayInstallHack2` (S) | `$global.gaKA_retrieveArchive = true` — `gaKA_relayInstallHack2` |
| `$global.gaKA_returnToAcademy` | S / 1 | `gaKAGargoyle2OrderAShuttleSel` (S) | `$global.gaKA_returnToAcademy = true` — `gaKAGargoyle2OrderAShuttleSel` |
| `$global.gaKA_talkToGargoyle` | S / 1 | `gaKAArroyoVisitLeave1` (S) | `$global.gaKA_talkToGargoyle = true` — `gaKAArroyoVisitLeave1` |
| `$global.gaKA_triTachyonVisit` | CS / 1 | `gaKA_triTachyonVisitEvent` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaKA_visitArroyo` | S / 1 | `gaKAArroyoProvostSel2` (S) | `$global.gaKA_visitArroyo = true` — `gaKAArroyoProvostSel2` |
| `$global.gaOp_completed` | S / 3 | `gaOpPlanetBasicCont` (S) | `$global.gaOp_completed = true` — `gaOpPlanetBasicCont` |
| `$global.gaOp_needToReturn` | S / 6 | `gaOpPlanetDestroyedContLastBit` (S) | `$global.gaOp_needToReturn = true` — `gaOpPlanetDestroyedContLastBit` |
| `$global.gaOp_ref` | CS / 15 | `gaOpPlanetInteraction` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaPZ_bairdHurryUp` | CS / 2 | `gaPZ_bairdHurryUp` (C) | `$global.gaPZ_bairdHurryUp = true` — `gaPZ_tellCoureuseMods4` |
| `$global.gaPZ_brotherCottonEncounter` | CS / 1 | `gaPZ_cotton` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaPZ_completed` | CS / 2 | `sdtuMacReasonGalatia` (C) | `$global.gaPZ_completed = true` — `gaPZ_return7` |
| `$global.gaPZ_foundBaseRuins` | CS / 3 | `gaPZ_tellBairdRuins` (C) | `$global.gaPZ_foundBaseRuins = true` — `gaPZ_baseRuins1` |
| `$global.gaPZ_goToRelaySystem` | S / 1 | `gaPZ_calHamatsu6` (S) | `$global.gaPZ_goToRelaySystem = true` — `gaPZ_calHamatsu6` |
| `$global.gaPZ_goToWell` | S / 1 | `gaPZ_investigateRelay3` (S) | `$global.gaPZ_goToWell = true` — `gaPZ_investigateRelay3` |
| `$global.gaPZ_inProgress` | C / 9 | `gaPZAskGargoyleKelise` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaPZ_missionCompleted` | CS / 3 | `gaPZIntro` (C) | `$global.gaPZ_missionCompleted = true` — `gaATGdevstart2` |
| `$global.gaPZ_missionGiven` | CS / 2 | `gaPZIntro` (C) | `$global.gaPZ_missionGiven = true` — `PZIntroHub1dSel` |
| `$global.gaPZ_pointedToCulannAdmin` | CS / 2 | `gaPZKeliseBarEventAdd` (C) | `$global.gaPZ_pointedToCulannAdmin = true` — `gaPZ_keliseBarEvent1` |
| `$global.gaPZ_ref` | CS / 33 | `gaPZAskGargoyleKelise` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaPZ_scannedZiggurat` | CS / 6 | `LKEjethroAngelsTalkB` (C) | `$global.gaPZ_scannedZiggurat = true` — `zig_encounterDesc2` |
| `$global.gaPZ_sellBlackmail` | S / 1 | `GAPZArroyoKelise3` (S) | `$global.gaPZ_sellBlackmail = true` — `GAPZArroyoKelise3` |
| `$global.gaPZ_soldBlackmail` | S / 3 | `gaPZSellFakes1b` (S) | `$global.gaPZ_soldBlackmail = true` — `gaPZSellFakes1b` |
| `$global.gaPZ_talkToCallisto` | S / 2 | `gaPZ_returnArroyo4` (S) | `$global.gaPZ_talkToCallisto = true` — `gaPZ_returnArroyo4` |
| `$global.gaPZ_tellCoureuseAboutMods` | CS / 3 | `gaPZ_tellCoureuseHamatsu` (C) | `$global.gaPZ_tellCoureuseAboutMods = true` — `gaPZ_tellBairdMods` |
| `$global.gaProbe_canReturn` | CS / 6 | `gaProbeScavengerDefeated` (C) | `$global.gaProbe_canReturn = true` — `gaProbeObjectInteractionBasic` |
| `$global.gaProbe_finished` | S / 4 | `gaProbeMissionReturnFinish` (S) | `$global.gaProbe_finished = true` — `gaProbeMissionReturnFinish` |
| `$global.gaProbe_ref` | CS / 20 | `gaProbeObjectInteraction` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaProbe_scavengerTookIt` | S / 1 | `gaProbeObjectInteractionScavenger` (S) | `$global.gaProbe_scavengerTookIt = true` — `gaProbeObjectInteractionScavenger` |
| `$global.gaRH_completed` | CS / 3 | `gaRH_noHamatsuOption` (C) | `$global.gaRH_completed = true` — `gaRH_yes2` |
| `$global.gaRH_failed` | S / 1 | `gaRH_no2` (S) | `$global.gaRH_failed = true` — `gaRH_no2` |
| `$global.gaRH_ref` | CS / 3 | `gaRH_withHamatsu` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaRR_completed` | S / 1 | `gaRRMissionReturn` (S) | `$global.gaRR_completed = true` — `gaRRMissionReturn` |
| `$global.gaRR_failedCredits` | CS / 1 | `gaRRMissionReturnPlayerOwes` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaRR_needToReturn` | S / 6 | `gaRRChurchAccept1` (S) | `$global.gaRR_needToReturn = true` — `gaRRChurchAccept1` |
| `$global.gaRR_playerOwes` | CS / 1 | `gaRRMissionReturnPlayerOwes` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaRR_ref` | CS / 12 | `gaRRChurchGreeting` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaTJ_completed` | S / 1 | `gaTJReturnFinish` (S) | `$global.gaTJ_completed = true` — `gaTJReturnFinish` |
| `$global.gaTJ_needToReturn` | S / 1 | `gaTJPlanetContact3` (S) | `$global.gaTJ_needToReturn = true` — `gaTJPlanetContact3` |
| `$global.gaTJ_ref` | CS / 8 | `gaTJPlanetInteraction` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaTTB_ref` | S / 1 | `bairdIntro2` (S) | Trace owning rule/command or generated interaction data |
| `$global.gaTTS_ref` | S / 1 | `asebFirstTimeGreeting` (S) | Trace owning rule/command or generated interaction data |
| `$global.gaVIP_delivered` | S / 1 | `gaVIP_completeDelivery` (S) | `$global.gaVIP_delivered = true` — `gaVIP_completeDelivery` |
| `$global.gaVIP_failed` | S / 2 | `gaVIPPirateAgreeResult2` (S) | `$global.gaVIP_failed = true` — `gaVIPPirateAgreeResult2` |
| `$global.gaVIP_ref` | CS / 11 | `gaVIPWorkingForKanta` (C) | Trace owning rule/command or generated interaction data |
| `$global.gaVIP_workingForKanta` | C / 1 | `gaVIPWorkingForKanta` (C) | Trace owning rule/command or generated interaction data |
| `$global.gatesActive` | CS / 22 | `gateOpenDialogActive` (C) | `$global.gatesActive = true` — `devGatesActiveOnSel` |
| `$global.gaveHamatsuToKanta` | CS / 2 | `callistoGaveHamatsuToKantaOpt` (C) | `$global.gaveHamatsuToKanta = true` — `kpHamatsuSel` |
| `$global.gavePKtoDiktat` | S / 4 | `pk_diktatComplySel` (S) | `$global.gavePKtoDiktat = true` — `pk_diktatComplySel` |
| `$global.gaveZGRdwellerData` | S / 2 | `ZGRmakePitch9both2` (S) | `$global.gaveZGRdwellerData = true` — `ZGRmakePitch9both2` |
| `$global.gaveZGRoneslaughtAccess` | CS / 17 | `ZGRpostGADHOimply3a` (C) | `$global.gaveZGRoneslaughtAccess = true` — `ZGRmakePitch9OMKIbuy4mil2` |
| `$global.gaveZGRthreatData` | S / 3 | `ZGRmakePitch9both2` (S) | `$global.gaveZGRthreatData = true` — `ZGRmakePitch9both2` |
| `$global.gotHyderTrust` | CS / 2 | `postSDTUHyderGreetingTrust` (C) | `$global.gotHyderTrust = true` — `sdtuHyderTalkXPHigh` |
| `$global.gotSentinelCrewBack` | CS / 2 | `skironReturnCrewToPlayer` (C) | `$global.gotSentinelCrewBack = true` — `skironReturnCrewToPlayer2` |
| `$global.houseHannanMachinationsInEffect` | C / 2 | `RHannanBusinessNoColony` (C) | Trace owning rule/command or generated interaction data |
| `$global.initiatedCommsAlready` | CS / 1 | `checkNPCWantingToTalk` (C) | `$global.initiatedCommsAlready = true 0` — `checkNPCWantingToTalk` |
| `$global.interactedWithGABarEvent` | CS / 2 | `goToTheGABarEventOption` (C) | `$global.interactedWithGABarEvent = true` — `goToGA_barEvent4` |
| `$global.isDevMode` | C / 43 | `devGateCanScanOn` (C) | Trace owning rule/command or generated interaction data |
| `$global.isInTutorial` | C / 7 | `sal_tutorial_storyPointUse1` (C) | Trace owning rule/command or generated interaction data |
| `$global.jangalaProtestIncident` | CS / 3 | `lkeChalcedonGreetJangalaCrush` (C) | `$global.jangalaProtestIncident = true` — `lppJangalaProtestMarines3` |
| `$global.jaspisMadeOffer` | CS / 6 | `lppEnding1redux` (C) | `$global.jaspisMadeOffer = true` — `lppJaspisWrapUp5` |
| `$global.jethroName` | COST / 84 | `BFINstart4a` (T) | `$global.jethroName = Jeff` — `LKEjethroJeff` |
| `$global.knowSiyavongContact` | C / 1 | `gaFCFikenhildCavinHubResponse1` (C) | Trace owning rule/command or generated interaction data |
| `$global.knowSiyavongIsLookingForSyZal` | S / 1 | `adonyaImportantToFind2` (S) | `$global.knowSiyavongIsLookingForSyZal = true` — `adonyaImportantToFind2` |
| `$global.knowsMacarioThinksAndradaIrrelevant` | CS / 5 | `sdtuHyderTalk3OptionsI` (C) | `$global.knowsMacarioThinksAndradaIrrelevant = true` — `sdtuMacIntroAndradaKnow` |
| `$global.leftCrewOnSentinel` | CS / 11 | `defaultSkironGreetingNotToldYet` (C) | `$global.leftCrewOnSentinel = true` — `PKSentinelCrewSel` |
| `$global.leviathanCalvesPulsed` | C / 1 | `bar_descLeviathanCalvesPulsed` (C) | Trace owning rule/command or generated interaction data |
| `$global.lkeLiedToVirens` | CS / 3 | `LKEvirensRaidJethroFree3b` (C) | `$global.lkeLiedToVirens = true` — `LKEvirensRaidJethroLie` |
| `$global.lkeMaybeTriggeredMUAReconcilliation` | S / 1 | `LKEvirensRaidJethroLie2b2` (S) | `$global.lkeMaybeTriggeredMUAReconcilliation = true` — `LKEvirensRaidJethroLie2b2` |
| `$global.lkeThreatenedToBombardChalcedon` | S / 1 | `lkeChalcedonDoBombardThreat1` (S) | `$global.lkeThreatenedToBombardChalcedon = true` — `lkeChalcedonDoBombardThreat1` |
| `$global.lkeThreatenedToRaidChalcedon` | S / 1 | `lkeChalcedonDoRaidThreat1` (S) | `$global.lkeThreatenedToRaidChalcedon = true` — `lkeChalcedonDoRaidThreat1` |
| `$global.lke_bribedMUAcop` | S / 1 | `lkeMazBarAgentRespBribe2` (S) | Trace owning rule/command or generated interaction data |
| `$global.lke_completed` | S / 2 | `LKEdoFinish` (S) | `$global.lke_completed = true` — `LKEdoFinish` |
| `$global.lke_contactBornanew` | S / 4 | `LKEmazTest3a` (S) | `$global.lke_contactBornanew = true` — `LKEmazTest3a` |
| `$global.lke_contactRecruiter` | S / 5 | `LKEchalcedonThreatSedge2` (S) | `$global.lke_contactRecruiter = true` — `LKEchalcedonThreatSedge2` |
| `$global.lke_contactVirens` | CS / 12 | `lkeVirensCommGreeting` (C) | `$global.lke_contactVirens = true` — `LKEmazalotPMquietly` |
| `$global.lke_didMazBarAgentEncounter` | CS / 6 | `LKEmazConvOptionLeaveAuto` (C) | `$global.lke_didMazBarAgentEncounter = true` — `lkeMazBarAgent` |
| `$global.lke_fakeBornanewDeathButTold` | CS / 4 | `BFINstart` (C) | `$global.lke_fakeBornanewDeathButTold = true` — `LKEreturnToGileadDeadJaspis6a2` |
| `$global.lke_fakeBornanewDeathButToldWasJerk` | CS / 2 | `BFINstart` (C) | `$global.lke_fakeBornanewDeathButToldWasJerk = true` — `LKEreturnToGileadDeadJaspis6d` |
| `$global.lke_foundBornanew` | CS / 5 | `lkeJaspisGreeting` (C) | `$global.lke_foundBornanew = true` — `LKEjethroLetsGoThen` |
| `$global.lke_foundBornanew2` | CS / 3 | `lkeJaspisGreeting` (C) | `$global.lke_foundBornanew2 = true` — `LKEjethroDeadOut3c` |
| `$global.lke_gotBornanewLead` | S / 1 | `lkeSedgeShowPic2` (S) | `$global.lke_gotBornanewLead = true` — `lkeSedgeShowPic2` |
| `$global.lke_gotVirensContactFreebie` | CS / 2 | `LKEvirensConvOptB` (C) | `$global.lke_gotVirensContactFreebie = true` — `LKEmazalotFreebieAdvance3` |
| `$global.lke_helpedFakeBornanewDeath` | CS / 2 | `BFINstart` (C) | `$global.lke_helpedFakeBornanewDeath = true` — `LKEjethroDeadOut4` |
| `$global.lke_inProgress` | C / 10 | `lkeJaspisMissionOfferOption` (C) | Trace owning rule/command or generated interaction data |
| `$global.lke_missionCompleted` | C / 4 | `lkeJaspisMissionOfferOption` (C) | Trace owning rule/command or generated interaction data |
| `$global.lke_punchedMUAcop` | S / 1 | `lkeMazBarAgentRespPunch` (S) | Trace owning rule/command or generated interaction data |
| `$global.lke_raidedMazalotForVirens` | CS / 2 | `LKEjethroWhyOptB` (C) | `$global.lke_raidedMazalotForVirens = true` — `lkeVirensRaidFinishedB` |
| `$global.lke_ref` | CS / 52 | `lkeChalcedonPWgreetRaid` (C) | Trace owning rule/command or generated interaction data |
| `$global.lke_sedgeDrankYourTea` | CS / 3 | `LKEvirensAskWhyKillSedgeTfilter0` (C) | `$global.lke_sedgeDrankYourTea = true` — `lkeChalcedonBarSedge1` |
| `$global.lke_sedgeKilledMammonite` | CS / 8 | `lkeChalcedonBarSedgeOptKill2` (C) | `$global.lke_sedgeKilledMammonite = true 0` — `lkeChalcedonBarDrinkLotsMore5` |
| `$global.locr_luddicDiscovered` | CS / 3 | `LOCRLstart` (C) | `$global.locr_luddicDiscovered = true` — `LOCRLstart` |
| `$global.locr_minersDiscovered` | CS / 2 | `LOCRMstart` (C) | `$global.locr_minersDiscovered= true` — `LOCRMstart` |
| `$global.locr_pirateDiscovered` | CS / 2 | `LOCRPstart` (C) | `$global.locr_pirateDiscovered = true` — `LOCRPstart` |
| `$global.locrlf_completed` | S / 2 | `locrlfReturnStart2A` (S) | `$global.locrlf_completed = true` — `locrlfReturnStart2A` |
| `$global.locrlf_foundHeretics` | S / 1 | `LOCRLFmissionFound` (S) | `$global.locrlf_foundHeretics = true` — `LOCRLFmissionFound` |
| `$global.locrlf_inProgress` | C / 3 | `LOCRLFmissionFound` (C) | Trace owning rule/command or generated interaction data |
| `$global.locrlf_ref` | CS / 5 | `locrlfReturnContact` (C) | Trace owning rule/command or generated interaction data |
| `$global.lpp_askedVolturnCurateAboutInsurgency` | CS / 4 | `lppVolturnCurateResponses1b` (C) | `$global.lpp_askedVolturnCurateAboutInsurgency = true` — `lppVolturnCurateAskInsurgencySD` |
| `$global.lpp_completed` | CS / 8 | `relLevelLuddicShrines` (C) | `$global.lpp_completed = true` — `lppGileadShrineLeave0` |
| `$global.lpp_deniedHesperusShrineAccess` | CS / 13 | `lppHesperusShrineVisitAllowed` (C) | `$global.lpp_deniedHesperusShrineAccess = true` — `lppHesperusExcubitorDenyAccess` |
| `$global.lpp_didHesperusFirstShrineAttempt` | CS / 7 | `lppHesperusShrineVisitSneak` (C) | `$global.lpp_didHesperusFirstShrineAttempt = true` — `lppHesperusShrineFirstTime1a` |
| `$global.lpp_didHesperusShrineBribe` | CS / 3 | `lppHesperusBarEventAddA` (C) | `$global.lpp_didHesperusShrineBribe = true` — `lppHesperusShrineSneakBribe` |
| `$global.lpp_didHookStart` | CS / 2 | `lppHookStart` (C) | `$global.lpp_didHookStart = true` — `lppHookStart` |
| `$global.lpp_didJangalaProtest` | CS / 3 | `lppJangalaShrineProtest` (C) | `$global.lpp_didJangalaProtest = true` — `lppJangalaShrineProtestStart2` |
| `$global.lpp_hesperusWait` | CS / 8 | `lppHesperusShrineVisitAllowed` (C) | `$global.lpp_hesperusWait = true 7` — `lppHesperusExcubitorEndAllowWait` |
| `$global.lpp_inProgress` | C / 24 | `lppHookStart` (C) | Trace owning rule/command or generated interaction data |
| `$global.lpp_knowWhereShrineKilla` | C / 1 | `shrineConvShrineResponseKilla` (C) | Trace owning rule/command or generated interaction data |
| `$global.lpp_liedToVolturnShrineOfficial` | S / 1 | `lppVolturnOfficialLie2` (S) | `$global.lpp_liedToVolturnShrineOfficial = true` — `lppVolturnOfficialLie2` |
| `$global.lpp_madeToWaitForHesperus` | CS / 15 | `lppHesperusShrineVisitAllowed` (C) | `$global.lpp_madeToWaitForHesperus = true` — `lppHesperusExcubitorEndAllowWait` |
| `$global.lpp_missionCompleted` | C / 8 | `lppHookStart` (C) | Trace owning rule/command or generated interaction data |
| `$global.lpp_playerRejectedFirstJaspisMeeting` | CS / 4 | `lppGileadShrineJaspisRetryB` (C) | `$global.lpp_playerRejectedFirstJaspisMeeting = true` — `lppEndingAbort2` |
| `$global.lpp_playerRejectedJaspisRoute` | CS / 4 | `lppGileadShrineJaspisRetryA` (C) | `$global.lpp_playerRejectedJaspisRoute = true` — `lppGileadShrineStartEndingAbortB` |
| `$global.lpp_radicalizedKnightInitiate` | CS / 5 | `lkeChalcedonVIPreason2` (C) | `$global.lpp_radicalizedKnightInitiate = true` — `lppHesperusInitiateMetCottonYes` |
| `$global.lpp_receivedVolturnCurateBlessing` | S / 1 | `lppVolturnCurateAskBlessing` (S) | `$global.lpp_receivedVolturnCurateBlessing = true` — `lppVolturnCurateAskBlessing` |
| `$global.lpp_ref` | S / 7 | `lppGileadShrineLeave0` (S) | Trace owning rule/command or generated interaction data |
| `$global.lpp_snuckIntoHesperusShrine` | S / 1 | `lppHesperusShrineVisitFirst1sneak` (S) | `$global.lpp_snuckIntoHesperusShrine = true` — `lppHesperusShrineVisitFirst1sneak` |
| `$global.lpp_visitedShrineBeholderStation` | CS / 1 | `lppShrineGiveXPBeholder` (C) | `$global.lpp_visitedShrineBeholderStation = true` — `lppShrineGiveXPBeholder` |
| `$global.lpp_visitedShrineGilead` | CS / 5 | `lppGileadShrineVisitAgain` (C) | `$global.lpp_visitedShrineGilead = true` — `lppGileadMissionEndDevH2` |
| `$global.lpp_visitedShrineHesperus` | CS / 21 | `lppHesperusShrineOption` (C) | `$global.lpp_visitedShrineHesperus = true` — `lppShrineGiveXPHesperus` |
| `$global.lpp_visitedShrineJangala` | CS / 4 | `lppJangalaShrineDesc2Revisit` (C) | `$global.lpp_visitedShrineJangala = true` — `lppShrineGiveXPJangala` |
| `$global.lpp_visitedShrineKilla` | CS / 4 | `lppShrineKillaVisitOption` (C) | `$global.lpp_visitedShrineKilla = true` — `lppShrineGiveXPKilla` |
| `$global.lpp_visitedShrineVolturn` | CS / 1 | `lppShrineGiveXPVolturn` (C) | `$global.lpp_visitedShrineVolturn = true` — `lppShrineGiveXPVolturn` |
| `$global.lpp_volturnShrineRazed` | C / 1 | `lppVolturnShrineRevisitRazed` (C) | Trace owning rule/command or generated interaction data |
| `$global.lpp_willTalkToSomeoneOnCruor` | S / 1 | `lppVolturnCurateSDstubborn2` (S) | `$global.lpp_willTalkToSomeoneOnCruor = true` — `lppVolturnCurateSDstubborn2` |
| `$global.metArroyo` | CS / 7 | `BFFImmBornBuyerOptHubE` (C) | `$global.metArroyo = true` — `gaKAArroyoGreeting` |
| `$global.metCallisto` | CS / 6 | `gaPZCallistoTalk` (C) | `$global.metCallisto = true` — `gaPZ_calHamatsu` |
| `$global.metOrcusRao` | CS / 5 | `TseenKeAskRao` (C) | `$global.metOrcusRao = true` — `defaultRaoGreeting` |
| `$global.metTseenKeDuringTutorial` | C / 2 | `gaMeetHegLieutenantOnCoatl` (C) | Trace owning rule/command or generated interaction data |
| `$global.murderedCloneLoke` | CS / 7 | `relLevelPiratesKilledLoke` (C) | `$global.murderedCloneLoke = true` — `gaATGreturnLokeAirlock1b` |
| `$global.numGatesScanned` | CS / 16 | `gateScanSelFirstTime` (C) | `$global.numGatesScanned++` — `gateScanSelFirstTime` |
| `$global.pkCacheDefendersDefeated` | CS / 7 | `PKSentinelHubConfrontCheckD` (C) | `$global.pkCacheDefendersDefeated = true` — `PK14thDefeated` |
| `$global.pk_completed` | S / 2 | `pk_salvageSel2` (S) | `$global.pk_completed = true 0` — `pk_salvageSel2` |
| `$global.pk_exploredSentinelGantry` | CS / 6 | `PKSentinelDockyardFollowupA` (C) | `$global.pk_exploredSentinelGantry = true` — `PKSentinelDockyardFollowupA` |
| `$global.pk_gotDataFromMysteryAI` | S / 1 | `PKHackStoryEnd` (S) | Trace owning rule/command or generated interaction data |
| `$global.pk_inProgress` | C / 8 | `patherBaseCommanderGreetingDev` (C) | Trace owning rule/command or generated interaction data |
| `$global.pk_nexusDataGained` | CS / 4 | `PKNexusDefeated` (C) | `$global.pk_nexusDataGained = true` — `PKNexusDefeated` |
| `$global.pk_recovered` | CS / 14 | `patherBaseCommanderGreetingDev` (C) | `$global.pk_recovered = true` — `pk_salvageSel2` |
| `$global.pk_ref` | CS / 9 | `PKPatherRecoverMissionSel` (C) | Trace owning rule/command or generated interaction data |
| `$global.pk_startedAtNexus` | S / 1 | `PKNexusDefeatedNoMission` (S) | `$global.pk_startedAtNexus = true` — `PKNexusDefeatedNoMission` |
| `$global.pk_startedAtPather` | S / 2 | `pk_recoverMissionDevSel` (S) | `$global.pk_startedAtPather = true` — `pk_recoverMissionDevSel` |
| `$global.playerCanUseGates` | CS / 14 | `gateOpenDialogActive` (C) | `$global.playerCanUseGates = true` — `devGatesPlayerCanUseOnSel` |
| `$global.playerReceivingGAStipend` | C / 2 | `gaRequestMeeting5b` (C) | Trace owning rule/command or generated interaction data |
| `$global.preparingExecutor` | CS / 5 | `PKGiveToDiktat` (C) | `$global.preparingExecutor = true 7` — `pk_executorInsistSel2` |
| `$global.rdsm_gotData` | S / 3 | `rdsmFinishedRaid` (S) | `$global.rdsm_gotData = true` — `rdsmFinishedRaid` |
| `$global.rdsm_ref` | CS / 8 | `rdsmPlanetInteraction` (C) | Trace owning rule/command or generated interaction data |
| `$global.rdsm_returnedData` | S / 1 | `rdsmreturnToContact2` (S) | `$global.rdsm_returnedData = true 0` — `rdsmreturnToContact2` |
| `$global.repairSupplyCost` | ST / 3 | `marketOptionRepairAll` (T) | Trace owning rule/command or generated interaction data |
| `$global.rsom_raidedOutpost` | CS / 4 | `rsomStandardPlanetInteraction` (C) | `$global.rsom_raidedOutpost = true` — `rsomFinishedRaid` |
| `$global.rsom_ref` | CS / 4 | `rsomPlanetInteraction` (C) | Trace owning rule/command or generated interaction data |
| `$global.sdtuHintedRaoInvolvement` | CST / 6 | `raoDevOptionHubA2` (T) | `$global.sdtuHintedRaoInvolvement = true 0` — `raoDevOptionHubA2` |
| `$global.sdtu_HyderBlamedMacarioForResistance` | CS / 2 | `sdtuPostHyderHatesYouOptD` (C) | `$global.sdtu_HyderBlamedMacarioForResistance = true` — `sdtuHyderTalkBossB` |
| `$global.sdtu_HyderCalledMacarioWorm` | CS / 2 | `sdtuPostHyderHatesYouOptC` (C) | `$global.sdtu_HyderCalledMacarioWorm = true` — `sdtuHyderTalk3WhySmuggleA` |
| `$global.sdtu_HyderSaidMacarioFuelsWithLies` | CS / 2 | `sdtuPostHyderHatesYouOptA` (C) | `$global.sdtu_HyderSaidMacarioFuelsWithLies = true` — `sdtuHyderTalk3MacarioB` |
| `$global.sdtu_HyderSaidMacarioHasNoConcept` | CS / 2 | `sdtuPostHyderHatesYouOptB` (C) | `$global.sdtu_HyderSaidMacarioHasNoConcept = true` — `sdtuHyderTalk3MacarioC` |
| `$global.sdtu_HyderSaidMacarioOpsTreason` | CS / 2 | `sdtuPostHyderHatesYouOptE` (C) | `$global.sdtu_HyderSaidMacarioOpsTreason = true` — `sdtuHyderOfficerSmuggling` |
| `$global.sdtu_completed` | CS / 4 | `sdPunExCommsSDcomLiarPointB` (C) | `$global.sdtu_completed = true` — `sdtuReturnNewsEnd` |
| `$global.sdtu_deliverNews` | S / 2 | `sdtuDebrisData4` (S) | `$global.sdtu_deliverNews = true` — `sdtuDebrisData4` |
| `$global.sdtu_didPatrolTraitorTransfer` | CS / 3 | `sdtuReleaseOfficerOption` (C) | `$global.sdtu_didPatrolTraitorTransfer = true` — `sdtuReleaseOfficer` |
| `$global.sdtu_didUmbraRaid` | S / 1 | `sdtuUmbraRaidFinishedB` (S) | `$global.sdtu_didUmbraRaid = true` — `sdtuUmbraRaidFinishedB` |
| `$global.sdtu_emergencyIntercept` | S / 3 | `sdtuPostCadenEnd` (S) | `$global.sdtu_emergencyIntercept = true` — `sdtuPostCadenEnd` |
| `$global.sdtu_enragedCaden` | CS / 7 | `defaultCadenGreetPostSDTUcommission` (C) | `$global.sdtu_enragedCaden = true` — `sdDevOptionHubOptionD` |
| `$global.sdtu_extractAgent` | CS / 4 | `sdtuMacarioPostHyderRetalk` (C) | `$global.sdtu_extractAgent = true` — `sdtuPostHyderMission2a` |
| `$global.sdtu_foughtDiktatPatrol` | CS / 7 | `SDTUHyderTalkRound1Text1` (C) | `$global.sdtu_foughtDiktatPatrol = true` — `sdtuTraitorPatrolDefeated` |
| `$global.sdtu_fulfilledPromiseToPatrolTraitor` | CS / 8 | `sdtuHyderTalkRound2OptionsB` (C) | `$global.sdtu_fulfilledPromiseToPatrolTraitor = true` — `sdtuReleaseOfficer` |
| `$global.sdtu_gaveARCagentJob` | S / 1 | `sdtuUmbraReleaseArcAgentJob` (S) | `$global.sdtu_gaveARCagentJob = true` — `sdtuUmbraReleaseArcAgentJob` |
| `$global.sdtu_gaveDoubleAgentToSD` | S / 1 | `sdtuCadenAgentTransfer` (S) | `$global.sdtu_gaveDoubleAgentToSD = true` — `sdtuCadenAgentTransfer` |
| `$global.sdtu_gotMacarioOpinionOfCaden` | CS / 2 | `sdtuCadenMacPuppet2a` (C) | `$global.sdtu_gotMacarioOpinionOfCaden = true` — `sdtuPostHyderCaden` |
| `$global.sdtu_gotPatrolCrewToMutiny` | CS / 5 | `SDTUHyderTalkRound1Option5` (C) | `$global.sdtu_gotPatrolCrewToMutiny = true` — `sdtuPatrolConvinceMutiny` |
| `$global.sdtu_hyderBlamesMacarioForSupplyProblem` | CS / 2 | `sdtuPostHyderReportE` (C) | `$global.sdtu_hyderBlamesMacarioForSupplyProblem = true` — `sdtuHyderTalk3WhySmuggleC` |
| `$global.sdtu_inProgress` | C / 1 | `sdtuHyderAngryGreeting` (C) | Trace owning rule/command or generated interaction data |
| `$global.sdtu_interceptFleet` | S / 2 | `sdtuSafehouseWrap3` (S) | `$global.sdtu_interceptFleet = true` — `sdtuSafehouseWrap3` |
| `$global.sdtu_knowHyderHeldBackFromPromotion` | CS / 2 | `sdtuPostHyderReportB` (C) | `$global.sdtu_knowHyderHeldBackFromPromotion = true` — `sdtuHyderTalkBossC` |
| `$global.sdtu_knowHyderIsAFollower` | CS / 2 | `sdtuPostHyderReportD` (C) | `$global.sdtu_knowHyderIsAFollower = true` — `sdtuHyderTalk3Leadership` |
| `$global.sdtu_knowHyderWontBackCaden` | CS / 3 | `sdtuPostHyderReportC` (C) | `$global.sdtu_knowHyderWontBackCaden = true` — `sdtuHyderTalk3LeadershipCaden` |
| `$global.sdtu_knowMacarioWasLuddic` | CS / 6 | `sdtuCadenTalkXPHigh` (C) | `$global.sdtu_knowMacarioWasLuddic = true` — `sdtuCadenHyderBacksMac1` |
| `$global.sdtu_liedToCadenAboutAgentToMacarioAlready` | CS / 2 | `sdtuCadenAgentMacarioSecondLie3b` (C) | `$global.sdtu_liedToCadenAboutAgentToMacarioAlready = true` — `sdtuCadenAgentMacarioLieB` |
| `$global.sdtu_meetCaden` | S / 3 | `sdtuUmbraGotAgentNextStage` (S) | `$global.sdtu_meetCaden = true` — `sdtuUmbraGotAgentNextStage` |
| `$global.sdtu_meetHyder` | S / 4 | `sdtuPatrolForAPrice3d` (S) | `$global.sdtu_meetHyder = true` — `sdtuPatrolForAPrice3d` |
| `$global.sdtu_missionCompleted` | CS / 15 | `relLevelDiktatSDTU` (C) | `$global.sdtu_missionCompleted = true` — `sdDevOptionHubOptionB` |
| `$global.sdtu_offeredHyderHegemonyOut` | CS / 3 | `sdtuPostHyderReportHeg` (C) | `$global.sdtu_offeredHyderHegemonyOut = true` — `sdtuHyderTalk3HegemonyB` |
| `$global.sdtu_oweARCfavor` | S / 1 | `sdtuUmbraTrickVIPending1` (S) | `$global.sdtu_oweARCfavor = true` — `sdtuUmbraTrickVIPending1` |
| `$global.sdtu_promisedToHelpPatrolLeader` | CS / 9 | `sdtuReleaseOfficerOption` (C) | `$global.sdtu_promisedToHelpPatrolLeader = true` — `sdtuPatrolForAPrice` |
| `$global.sdtu_proposedHyderAsLeader` | S / 1 | `sdtuHyderTalk3LeadershipYou` (S) | `$global.sdtu_proposedHyderAsLeader = true` — `sdtuHyderTalk3LeadershipYou` |
| `$global.sdtu_proposedSelfAsLeaderToHyder` | S / 1 | `sdtuHyderTalk3LeadershipMe` (S) | `$global.sdtu_proposedSelfAsLeaderToHyder = true` — `sdtuHyderTalk3LeadershipMe` |
| `$global.sdtu_ramDidProposal` | CS / 2 | `sdtuStart` (C) | `$global.sdtu_ramDidProposal = true` — `sdtuRamStartMission` |
| `$global.sdtu_ref` | CS / 60 | `sdtuVolturnMeetRamOption` (C) | Trace owning rule/command or generated interaction data |
| `$global.sdtu_reportToMacario1` | S / 2 | `sdtuHyderTalkEnd` (S) | `$global.sdtu_reportToMacario1 = true` — `sdtuHyderTalkEnd` |
| `$global.sdtu_reportToMacario2` | S / 3 | `sdtuCadenTooFar` (S) | `$global.sdtu_reportToMacario2 = true` — `sdtuCadenTooFar` |
| `$global.sdtu_resolvedARCagent` | CS / 1 | `sdtuUmbraReleaseArcAgent` (C) | `$global.sdtu_resolvedARCagent = true` — `sdtuUmbraReleaseArcAgent` |
| `$global.sdtu_setCadenOffAboutMacario` | CS / 8 | `sdtuCadenExecReplies2a` (C) | `$global.sdtu_setCadenOffAboutMacario = true` — `sdtuCadenAgentMacarioSecondLie` |
| `$global.sdtu_tabledPrisonerTransfer` | CS / 3 | `sdtuPostHyderReportPrisonerB` (C) | `$global.sdtu_tabledPrisonerTransfer = true` — `sdtuHyderTalk2Promised3A` |
| `$global.sdtu_toldCadenHyderBacksHyder` | CS / 3 | `sdtuCadenHyderExec` (C) | `$global.sdtu_toldCadenHyderBacksHyder = true` — `sdtuCadenHyderExec1` |
| `$global.sdtu_toldCadenHyderBacksMacario` | CS / 2 | `sdtuPostCadenNoTrustOptsC` (C) | `$global.sdtu_toldCadenHyderBacksMacario = true` — `sdtuCadenHyderBacksMac1` |
| `$global.sdtu_toldCadenMacarioBacksHyder` | CS / 3 | `sdtuPostCadenTrustLieOptsB` (C) | `$global.sdtu_toldCadenMacarioBacksHyder = true` — `sdtuCadenMacHyder` |
| `$global.sdtu_toldCadenMacarioWantsToPuppet` | CS / 3 | `sdtuPostCadenNoTrustOptsE` (C) | `$global.sdtu_toldCadenMacarioWantsToPuppet = true` — `sdtuCadenMacPuppet1` |
| `$global.sdtu_toldCadenPlayerBacksHim` | CS / 5 | `defaultCadenGreetPostSDTUbacker` (C) | `$global.sdtu_toldCadenPlayerBacksHim = true` — `sdDevOptionHubOptionC` |
| `$global.sdtu_toldCadenPlayerBacksHimLie` | S / 1 | `sdtuCadenPledgeVsMacarioLie1` (S) | `$global.sdtu_toldCadenPlayerBacksHimLie = true` — `sdtuCadenPledgeVsMacarioLie1` |
| `$global.sdtu_toldHyderPrisonerToMacario` | CS / 2 | `sdtuPostHyderReportPrisonerA` (C) | `$global.sdtu_toldHyderPrisonerToMacario = true` — `sdtuHyderTalk2PrisonerToMacarioFinal` |
| `$global.sdtu_toldMacarioHyderBacksCaden` | CS / 3 | `sdtuPostHyderLiesOptionB` (C) | `$global.sdtu_toldMacarioHyderBacksCaden = true` — `sdtuPostHyderLieBacksCaden` |
| `$global.sdtu_toldMacarioHyderBacksHyder` | CS / 3 | `sdtuPostHyderLiesOptionD` (C) | `$global.sdtu_toldMacarioHyderBacksHyder = true` — `sdtuPostHyderLieWantsToLead` |
| `$global.sdtu_toldMacarioHyderBacksMacario` | CS / 3 | `sdtuPostHyderLiesOptionC` (C) | `$global.sdtu_toldMacarioHyderBacksMacario = true` — `sdtuPostHyderLieBacksMacario` |
| `$global.sdtu_toldMacarioHyderWantsToDefect` | CS / 3 | `sdtu_postCadenHydCadLieD` (C) | `$global.sdtu_toldMacarioHyderWantsToDefect = true` — `sdtuPostHyderHegemonyOut2` |
| `$global.sdtu_toldMacarioKnowLuddic` | S / 1 | `sdtuPostCadenLuddic` (S) | `$global.sdtu_toldMacarioKnowLuddic = true` — `sdtuPostCadenLuddic` |
| `$global.sdtu_traitorPatrolSavedOfficer` | S / 1 | `sdtuTraitorPatrolDefeated` (S) | `$global.sdtu_traitorPatrolSavedOfficer = true` — `sdtuTraitorPatrolDefeated` |
| `$global.sdtu_umbraAgentCoop` | CS / 7 | `sdtuUmbraReleaseArcAgent` (C) | `$global.sdtu_umbraAgentCoop = true` — `sdtuUmbraAskAgentQM4` |
| `$global.sdtu_umbraAgentCoopButLied` | S / 1 | `sdtuUmbraAskAgentQM4lie` (S) | `$global.sdtu_umbraAgentCoopButLied = true` — `sdtuUmbraAskAgentQM4lie` |
| `$global.sdtu_usedTrickToExtractAgent` | S / 1 | `sdtuUmbraTrickVIPending2` (S) | `$global.sdtu_usedTrickToExtractAgent = true` — `sdtuUmbraTrickVIPending2` |
| `$global.sedgeSmashedTea` | CS / 8 | `lkeChalcedonBarSedgeSmashA` (C) | `$global.sedgeSmashedTea = true 0` — `lkeChalcedonBarSedgeRespTea3` |
| `$global.shotVirens` | S / 1 | `LKEvirensRaidJethroLie2a` (S) | `$global.shotVirens = true` — `LKEvirensRaidJethroLie2a` |
| `$global.soe_completed` | S / 5 | `soeDeleteInvite` (S) | `$global.soe_completed = true` — `soeDeleteInvite` |
| `$global.soe_ref` | S / 15 | `soeDeleteInvite` (S) | Trace owning rule/command or generated interaction data |
| `$global.soldPKtoDiktat` | S / 1 | `pk_diktatCreditsSel2` (S) | `$global.soldPKtoDiktat = true` — `pk_diktatCreditsSel2` |
| `$global.soldPKtoDiktatForExecutor` | S / 1 | `PKDiktatExecutorReady` (S) | `$global.soldPKtoDiktatForExecutor = true` — `PKDiktatExecutorReady` |
| `$global.suppliedARC` | S / 3 | `imoinuUmbraOfferHelpCredits` (S) | `$global.suppliedARC++` — `imoinuUmbraOfferHelpCredits` |
| `$global.talkedtoAdonyaAfterGAATG` | CS / 2 | `adonyaAboutScyllaOpt` (C) | `$global.talkedtoAdonyaAfterGAATG = true` — `adonyaTell3` |
| `$global.toldARCplayerHatesSD` | S / 2 | `sdtuUmbraTrickVIPOptionB2` (S) | `$global.toldARCplayerHatesSD = true` — `sdtuUmbraTrickVIPOptionB2` |
| `$global.toldARCplayerIsARC` | CS / 3 | `ImoinuUmbraMacarioHubSelEb` (C) | `$global.toldARCplayerIsARC = true` — `sdtuUmbraTrickVIPOptionD2` |
| `$global.toldARCplayerIsHegemony` | S / 1 | `sdtuUmbraTrickVIPOptionA2` (S) | Trace owning rule/command or generated interaction data |
| `$global.toldARCplayerIsLuddic` | S / 1 | `sdtuUmbraTrickVIPOptionF2` (S) | `$global.toldARCplayerIsLuddic = true` — `sdtuUmbraTrickVIPOptionF2` |
| `$global.toldARCplayerIsSiyavong` | S / 1 | `sdtuUmbraTrickVIPOptionG2` (S) | `$global.toldARCplayerIsSiyavong = true` — `sdtuUmbraTrickVIPOptionG2` |
| `$global.toldAboutSentinel` | CS / 8 | `defaultSkironGreetingNotToldYet` (C) | `$global.toldAboutSentinel = true` — `PKTellAboutSentinelSelLetfCrew` |
| `$global.toldMUAplayerIsSiyavong` | S / 1 | `lkeMazBarAgentRespSiyavong` (S) | `$global.toldMUAplayerIsSiyavong = true` — `lkeMazBarAgentRespSiyavong` |
| `$global.toldPathWillGiveThemPK` | CS / 2 | `PKHackStoryWhatDoGivePath0` (C) | `$global.toldPathWillGiveThemPK = true` — `PKPatherSayYes` |
| `$global.toldSiyavongAboutAlphaSite` | S / 1 | `gaPZ_siyaExplain` (S) | `$global.toldSiyavongAboutAlphaSite = true` — `gaPZ_siyaExplain` |
| `$global.tookInfirmFromSentinel` | CS / 4 | `pkSentinelHubMedicalCases` (C) | `$global.tookInfirmFromSentinel = true` — `PKSentinelTakeMedicalCases` |
| `$global.tradePanelMode` | C / 23 | `flavorTextMarketGeneric` (C) | Trace owning rule/command or generated interaction data |
| `$global.ttBriberyContactSet` | CS / 3 | `ttContactBribeRepPreLock` (C) | `$global.ttBriberyContactSet = true` — `ttContactBribeRepIntroFirstTime` |
| `$global.ttli_unpaidEventRef` | CS / 4 | `ttli_noRepaymentNextMarketOpen` (C) | Trace owning rule/command or generated interaction data |
| `$global.tutMadeContactAtAncyra` | C / 2 | `gaWithHegOfficer` (C) | Trace owning rule/command or generated interaction data |
| `$global.tutStage` | C / 7 | `tut_mainContactBegin` (C) | Trace owning rule/command or generated interaction data |
| `$gotAIWarfleetStory` | CS / 3 | `PKSentinelHubQAskDangerVague` (C) | `$gotAIWarfleetStory = true` — `PKSentinelAskAboutStarshipsHeg3` |
| `$gotAnOpening` | CS / 10 | `RaoLookingForWorkOption` (C) | `$gotAnOpening = true` — `raoFastOneSpeak` |
| `$gotFirstScholarshipPitch` | CS / 5 | `asebScholarshipHeardAbout` (C) | `$gotFirstScholarshipPitch = true` — `asebScholarshipPitch` |
| `$gotFuel` | CS / 4 | `ZGRstartPitchEndRequestFuel` (C) | `$gotFuel = true 0` — `ZGRstartPitchEndRequestFuel2` |
| `$gotGAATGpay` | CS / 2 | `gaATGpostSebestyenGetPaid` (C) | `$gotGAATGpay = true` — `gaATGpostSebestyenGetPaid2` |
| `$gotHoloExperience` | CS / 2 | `defaultDaudHolo` (C) | `$gotHoloExperience = true` — `defaultDaudHolo2` |
| `$gotSupplies` | CS / 4 | `ZGRstartPitchEndRequestSup` (C) | `$gotSupplies = true 0` — `ZGRstartPitchEndRequestSup2` |
| `$gotTea` | CS / 5 | `lkeVirensRaidOptionB` (C) | `$gotTea = true 0` — `LKEvirensRaidGetTea` |
| `$gotTeapot` | CS / 3 | `lkeChalcedonBarSedgeOptD` (C) | `$gotTeapot = true 0` — `lkeChalcedonBarSedgeRespE` |
| `$grbh_didLaugh` | CS / 4 | `GRBHexamineAagain` (C) | `$grbh_didLaugh = true 0` — `GRBHstartRespB` |
| `$gsType` | CS / 16 | `GS_AI_CORES_open` (C) | `impl/campaign/enc/AbyssalRogueStellarObjectDireHintsEPEC.java` |
| `$hadMetBefore` | CS / 2 | `GAPZSiyavongHub1` (C) | `$hadMetBefore = $metAlready 0` — `gaPZPLEncounterComm` |
| `$hadShotTalk` | CS / 2 | `lkeChalcedonBarSedgeOptKill2` (C) | `$hadShotTalk = true 0` — `lkeChalcedonSedgeRespKill` |
| `$hailing` | S / 8 | `LPTitheCheck` (S) | `$hailing = true 0` — `LPTitheCheck` |
| `$hamatsu` | C / 7 | `gaPZ_hamatsuWreckOpen` (C) | `impl/campaign/missions/academy/GAProjectZiggurat.java`, `impl/campaign/world/TTBlackSite.java` |
| `$hamatsuSpielSaid` | CS / 4 | `gaRH_noHamatsuOption` (C) | `$hamatsuSpielSaid = true` — `gaRH_noHamatsu` |
| `$handledPrisoner` | CS / 3 | `sdtuPostHyderReportPrisonerA` (C) | `$handledPrisoner = true 0` — `sdtuPostHyderPrisonerOpt0` |
| `$hannanBribePercent` | O / 1 | `PLDealArgumentsHannanCut` (O) | `impl/campaign/rulecmd/HA_CMD.java` |
| `$hasDefenders` | C / 10 | `sal_defenders` (C) | `impl/campaign/intel/misc/CryosleeperIntel.java`, `impl/campaign/intel/misc/HypershuntIntel.java` (more mentions) |
| `$hasMarket` | C / 53 | `DEVnanoforgeEngineerOption` (C) | `impl/campaign/AbandonMarketPluginImpl.java`, `impl/campaign/CoreCampaignPluginImpl.java` (more mentions) |
| `$hasNonStation` | C / 2 | `sal_triggerMothershipDefendersBoth` (C) | `impl/campaign/rulecmd/salvage/SalvageGenFromSeed.java` |
| `$hasStation` | C / 2 | `sal_triggerMothershipDefendersBoth` (C) | `impl/campaign/CoreCampaignPluginImpl.java`, `impl/campaign/rulecmd/salvage/SalvageGenFromSeed.java` |
| `$hassleComplete` | S / 3 | `plEnforcerInitial` (S) | `$hassleComplete = true` — `plEnforcerInitial` |
| `$hassleType` | C / 3 | `plEnforcerInitial` (C) | `impl/campaign/ids/MemFlags.java` |
| `$hassle_didAlready` | CS / 3 | `plEnforcerInitial` (C) | `$hassle_didAlready = true 0` — `plEnforcerInitial` |
| `$hate` | S / 9 | `sdtu_postCadenHydCadLieB` (S) | `$hate++` — `sdtu_postCadenHydCadLieB` |
| `$heBelieves` | S / 1 | `gaDHOendingGiveSumEndBelieves` (S) | `$heBelieves = true 0` — `gaDHOendingGiveSumEndBelieves` |
| `$heOrShe` | ST / 679 | `TTmarketWeirdMods2` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/missions/hub/BaseHubMission.java` |
| `$hePoured` | S / 2 | `lkeVirensRaidTeaBcuffed` (S) | `$hePoured = true 0` — `lkeVirensRaidTeaBcuffed` |
| `$heSaidJethro` | CS / 4 | `lkeVirensRaidOptionA` (C) | `$heSaidJethro = true 0` — `LKEvirensRaidWalked` |
| `$heardScholarshipPitch` | C / 2 | `gaScholarshipPitch` (C) | Trace owning rule/command or generated interaction data |
| `$hijack_barEvent` | C / 2 | `hijackOperation3Bar` (C) | `impl/campaign/missions/HijackingMission.java` |
| `$hijack_designation` | T / 2 | `hijackOfferTextBar2` (T) | `impl/campaign/missions/HijackingMission.java` |
| `$hijack_hisOrHer` | T / 1 | `hijackBarLookAtFreighter` (T) | `impl/campaign/missions/HijackingMission.java` |
| `$hijack_hull` | T / 2 | `hijackOfferTextBar2` (T) | `impl/campaign/missions/HijackingMission.java` |
| `$hijack_manOrWoman` | OT / 4 | `hijackBlurbBar` (T) | `impl/campaign/missions/HijackingMission.java` |
| `$hijack_marines` | CST / 3 | `hijackOfferTextBar2` (T) | `impl/campaign/missions/HijackingMission.java` |
| `$hijack_member` | S / 1 | `hijackOperation2` (S) | `impl/campaign/missions/HijackingMission.java` |
| `$hijack_price` | ST / 5 | `hijackOfferTextBar2` (T) | `impl/campaign/missions/HijackingMission.java` |
| `$hijack_ref` | S / 3 | `hijackBarLookAtFreighter` (S) | `impl/campaign/missions/HijackingMission.java` |
| `$himOrHer` | OST / 55 | `TTmarketWeirdMods2OMKI` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/missions/hub/BaseHubMission.java` |
| `$himOrHerself` | T / 31 | `lkeChalcedonLookingRecruiterC2b` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/missions/hub/BaseHubMission.java` |
| `$hisOrHer` | OST / 506 | `TTmarketWeirdMods1b` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/missions/hub/BaseHubMission.java` |
| `$hmdf_hisOrHer` | T / 1 | `hmdfBlurbBar` (T) | `impl/campaign/missions/HandMeDownFreighter.java` |
| `$hmdf_hullClass` | T / 3 | `hmdfOfferTextBar` (T) | `impl/campaign/missions/HandMeDownFreighter.java` |
| `$hmdf_member` | S / 1 | `hmdfPostAccept` (S) | `impl/campaign/missions/HandMeDownFreighter.java` |
| `$hmdf_price` | OST / 7 | `hmdfOfferTextBar` (T) | `impl/campaign/missions/HandMeDownFreighter.java` |
| `$hmdf_ref` | S / 3 | `hmdfBarLookAtFreighter` (S) | `impl/campaign/missions/HandMeDownFreighter.java` |
| `$hmdf_ref2` | S / 1 | `hmdfPostAccept` (S) | `impl/campaign/missions/HandMeDownFreighter.java` |
| `$hostileToMarket` | C / 8 | `mpm_greetingNeedTOff` (C) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$howName` | CS / 2 | `gaATGscavScanHub32` (C) | `$howName = true 0` — `gaATGscavScanDialog32` |
| `$ht_dataCost` | ST / 8 | `scavBuyHTDataSel1` (T) | `impl/campaign/rulecmd/HT_CMD.java` |
| `$ht_dataRange` | ST / 2 | `scavBuyHTDataSel1` (T) | `impl/campaign/rulecmd/HT_CMD.java` |
| `$id` | CS / 309 | `GS_VAMBRACE_open` (C) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$ignorePlayer` | CS / 4 | `BFFIpatherStationTalkIgnore` (C) | Trace owning rule/command or generated interaction data |
| `$ignorePlayerCommRequests` | CS / 70 | `LPTitheCheck` (C) | `$ignorePlayerCommRequests = true 1` — `greetingDefaultHostileWeaker` |
| `$igr_bribeAmount` | CS / 2 | `igr_offerBribeSel` (C) | `impl/campaign/events/InvestigationEventGoodRepWithOther.java` |
| `$igr_bribeAmountDGS` | OST / 3 | `igr_offerBribeSel` (T) | `impl/campaign/events/InvestigationEventGoodRepWithOther.java` |
| `$igr_eventRef` | CS / 4 | `igr_greetingTOn` (C) | `impl/campaign/events/InvestigationEventGoodRepWithOther.java` |
| `$igr_investigator` | C / 4 | `igr_greetingTOn` (C) | `impl/campaign/events/InvestigationEventGoodRepWithOther.java` |
| `$importanceAtLeastHigh` | C / 3 | `locrlfToldReturningHereticsChurchB` (C) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$inHyperspace` | C / 2 | `pods_stabilizeDisable` (C) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$inTransit` | C / 2 | `gateHaulerStart4` (C) | `impl/campaign/entities/GateHaulerEntityPlugin.java`, `impl/campaign/intel/misc/GateHaulerIntel.java` |
| `$inspectedCargo` | CS / 3 | `orbitalStorageInteractionCargo1` (C) | `$inspectedCargo = true` — `marketOpenCargoSel` |
| `$inspectedShips` | CS / 3 | `orbitalStorageInteractionFleet1` (C) | `$inspectedShips = true` — `marketOpenFleetSel` |
| `$instalmentPlanProposed` | CS / 3 | `BFFImmBornBuyerOptHubFno` (C) | `$instalmentPlanProposed = true 0` — `BFFImmBornBuyerColony` |
| `$instalmentPlanProposedReaction` | CS / 2 | `BFFImmBornBuyerOptHubPayPlan` (C) | `$instalmentPlanProposedReaction = true 0` — `BFFImmBornBuyerCheat` |
| `$intuition` | C / 1 | `gaATGzalMissionStartHub2c` (C) | Trace owning rule/command or generated interaction data |
| `$investigated` | CS / 4 | `gsVambOptHubB` (C) | `$investigated = true` — `gsVambraceSalvors4` |
| `$investigatorConv` | S / 1 | `hegInvestigatorInitial` (S) | `$investigatorConv = true 0` — `hegInvestigatorInitial` |
| `$isContact` | C / 17 | `raoAskDiktatMerc` (C) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$isHostile` | C / 16 | `tOffPatrolBegin` (C) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$isMercenary` | C / 9 | `ome_greetingMerc` (C) | `util/Misc.java` |
| `$isPatrol` | C / 2 | `tOffPatrolBegin` (C) | `impl/campaign/ids/MemFlags.java` |
| `$isPerson` | C / 79 | `addMHubOption` (C) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$isStation` | C / 2 | `remnantStationFleetOpenDefault` (C) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$isThisLegal` | CS / 2 | `soeDuelResponseLegal` (C) | `$isThisLegal = true 0` — `soeDuelLegal` |
| `$ise_bribeAmount` | CS / 2 | `ise_offerBribeSel` (C) | `impl/campaign/events/InvestigationEventSmugglingV2.java` |
| `$ise_bribeAmountDGS` | OST / 4 | `ise_offerBribeSel` (T) | `impl/campaign/events/InvestigationEventSmugglingV2.java` |
| `$ise_bribeAmountDGSSetShortcut` | S / 1 | `ise_offerBribeSelNotEnough` (S) | Trace owning rule/command or generated interaction data |
| `$ise_eventRef` | CS / 4 | `ise_greetingTOn` (C) | `impl/campaign/events/InvestigationEventSmugglingV2.java` |
| `$ise_investigator` | C / 3 | `ise_greetingTOn` (C) | `impl/campaign/events/InvestigationEventSmugglingV2.java` |
| `$jabr_addedRaidObjective` | CS / 1 | `jabrOpenMarket1` (C) | `$jabr_addedRaidObjective = true 0` — `jabrOpenMarket1` |
| `$jabr_completed` | S / 1 | `jabrMissionReturn1` (S) | `$jabr_completed = true` — `jabrMissionReturn1` |
| `$jabr_danger` | S / 1 | `jabrOpenMarket1` (S) | `impl/campaign/missions/JailbreakMission.java` |
| `$jabr_dist` | ST / 2 | `jabrOfferTextBar1` (T) | `impl/campaign/missions/JailbreakMission.java` |
| `$jabr_factionColor` | S / 2 | `jabrOfferTextBar1` (S) | `impl/campaign/missions/JailbreakMission.java` |
| `$jabr_heOrShe` | T / 1 | `jabrOfferTextBar1` (T) | `impl/campaign/missions/JailbreakMission.java` |
| `$jabr_manOrWoman` | OT / 3 | `jabrBlurbBar1` (T) | `impl/campaign/missions/JailbreakMission.java` |
| `$jabr_marines` | ST / 2 | `jabrOfferTextBar1` (T) | `impl/campaign/missions/JailbreakMission.java` |
| `$jabr_marketFaction` | ST / 2 | `jabrOfferTextBar1` (T) | `impl/campaign/missions/JailbreakMission.java` |
| `$jabr_marketFactionArticle` | T / 1 | `jabrOfferTextBar1` (T) | `impl/campaign/missions/JailbreakMission.java` |
| `$jabr_marketName` | ST / 3 | `jabrOfferTextBar1` (T) | `impl/campaign/missions/JailbreakMission.java` |
| `$jabr_marketOnOrAt` | T / 2 | `jabrOfferTextBar1` (T) | `impl/campaign/missions/JailbreakMission.java` |
| `$jabr_ref` | CS / 3 | `jabrMissionReturn1` (C) | `impl/campaign/missions/JailbreakMission.java` |
| `$jabr_returnHere` | C / 1 | `jabrMissionReturn1` (C) | `impl/campaign/missions/JailbreakMission.java` |
| `$jabr_reward` | ST / 2 | `jabrOfferTextBar1` (T) | `impl/campaign/missions/JailbreakMission.java` |
| `$jabr_storyCost` | ST / 2 | `jabrBarEventSel` (T) | `impl/campaign/missions/JailbreakMission.java` |
| `$jangalaContactLastName` | T / 1 | `tut_mainContactStabilized1` (T) | `impl/campaign/tutorial/TutorialMissionEvent.java`, `impl/campaign/tutorial/TutorialMissionIntel.java` |
| `$jangalaContactPost` | T / 1 | `tut_mainContactStabilized1` (T) | `impl/campaign/tutorial/TutorialMissionEvent.java`, `impl/campaign/tutorial/TutorialMissionIntel.java` |
| `$jangalaFuel` | ST / 1 | `tut_mainContactStabilized1` (T) | `impl/campaign/tutorial/TutorialMissionEvent.java`, `impl/campaign/tutorial/TutorialMissionIntel.java` |
| `$jethroName` | O / 1 | `BFFItalkToEngineer12optD` (O) | Trace owning rule/command or generated interaction data |
| `$judged` | CS / 16 | `ImoinuKatoUmbraOptSalute` (C) | `$judged = true` — `sdtuUmbraTrickVIPOpt2imoinu` |
| `$justVisitedShrine` | CS / 14 | `lppGileadShrineVisitOption` (C) | `$justVisitedShrine = true 0` — `lppHookShrineLeave` |
| `$kanta` | CS / 2 | `gaATGzalMissionStartHub2` (C) | `$kanta = true 0` — `gaATGzalMissionHubB` |
| `$knowHyderThinksPlayerLied` | S / 1 | `sdtuHyderTalk2BattleLieA` (S) | `$knowHyderThinksPlayerLied = true 0` — `sdtuHyderTalk2BattleLieA` |
| `$knowMoreIsGoingOn` | CS / 13 | `PKSentinelHubQAskDangerVague` (C) | `$knowMoreIsGoingOn = true` — `PKSentinelAskWhereFromNotTrust2` |
| `$kpBribeAmount` | ST / 4 | `kdStationKingAudienceNormal` (T) | `$kpBribeAmount = 10,000 0` — `kdSKConvoStart` |
| `$kpNumGifts` | CS / 17 | `kp100k` (C) | `$kpNumGifts = 0` — `kpAudience2` |
| `$kpTried100k` | CS / 2 | `kp100k` (C) | `$kpTried100k = true` — `kp100k_Sel` |
| `$kpTriedCorruptedForge` | CS / 2 | `kpCorruptedForge` (C) | `$kpTriedCorruptedForge = true` — `kpCorruptedForgeSel` |
| `$kpTriedPK` | CS / 2 | `kpPlanetkiller` (C) | `$kpTriedPK = true` — `kpPlanetkillerSel` |
| `$lastTradeMode` | S / 1 | `marketPostOpenContinue` (S) | `impl/campaign/rulecmd/OpenCoreTab.java` |
| `$leaveGoesToMenu` | CS / 2 | `salRuins_leaveToMenu` (C) | `$leaveGoesToMenu = true 0` — `salRuins_noHostileNearby` |
| `$leftB` | CS / 2 | `raoNerieneSelLeftA2bb` (C) | `$leftB = true 0` — `raoNerieneSelLeftB` |
| `$leftMessageDiscussFuture` | CS / 4 | `RHannanMsgDiscussFutureOptA` (C) | `$leftMessageDiscussFuture = true` — `RHannanMsgDiscussFuture` |
| `$leftMessageGAFC` | CS / 2 | `RHannanMsgGAFC` (C) | `$leftMessageGAFC = true` — `RHannanMsgGAFCmsgOption` |
| `$leftMessageHorusYaribay` | CS / 3 | `RHannanMsgHorusYaribayOpt` (C) | `$leftMessageHorusYaribay = true` — `RHannanMsgHorusYaribay` |
| `$leftMessageJoinLeague` | CS / 3 | `RHannanMsgJoinLeagueOpt` (C) | `$leftMessageJoinLeague = true` — `RHannanMsgJoinLeague` |
| `$leftMessageOfferServices` | CS / 3 | `RHannanMsgOfferServicesOpt` (C) | `$leftMessageOfferServices = true` — `RHannanMsgOfferServices` |
| `$leftMessages` | CS / 11 | `RHannanSecretaryMessageResp1` (C) | `$leftMessages++` — `RHannanMsgGAFCmsgOption` |
| `$letsDeal` | CS / 2 | `gaFCIsirahMercHubOption2` (C) | `$letsDeal = true` — `gaFCIsirahMercLetsDeal` |
| `$level` | C / 1 | `ome_greetingMercPlayerWasJerkReject` (C) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$liedAboutOfficer` | S / 1 | `sdtuHyderTalk2Battle` (S) | `$liedAboutOfficer = true 0` — `sdtuHyderTalk2Battle` |
| `$liedAboutSDTUagent` | CS / 7 | `sdtuUmbraAdmin` (C) | `$liedAboutSDTUagent = true` — `sdtuUmbraTrickVIPwarned2` |
| `$lies` | CS / 2 | `sdtuPostHyderLiesOptionLieWarning` (C) | `$lies++` — `sdtuPostHyderPrisonerRespC` |
| `$limboMiningStation` | C / 1 | `sal_limboMiningStation` (C) | `impl/campaign/rulecmd/salvage/SalvageGenFromSeed.java`, `impl/campaign/world/Limbo.java` (more mentions) |
| `$limboStableLocation` | C / 1 | `WormholeInstabilityInfoCalibrated` (C) | `impl/campaign/shared/WormholeManager.java` |
| `$limboWormholeCache` | C / 2 | `limboWormholeCacheBegin` (C) | `impl/campaign/rulecmd/salvage/SalvageGenFromSeed.java`, `impl/campaign/world/Limbo.java` (more mentions) |
| `$litCandle` | CS / 4 | `lppVolturnCurateResponses2e` (C) | `$litCandle = true 0` — `lppVolturnCurateLightTheCandle` |
| `$lkeAskedBornanew` | CS / 8 | `lkeChalcedonVIPFriendlyResp2c` (C) | `$lkeAskedBornanew = true 7` — `lkeChalcedonLookingBornanewAb` |
| `$lkeAskedSomeone` | CS / 7 | `lkeChalcedonVIPFriendlyResp1` (C) | `$lkeAskedSomeone = true 7` — `lkeChalcedonLookingSomeoneA` |
| `$lke_encounteredAlready` | CS / 1 | `lkePatherFleet` (C) | `$lke_encounteredAlready = true` — `lkePatherFleet` |
| `$lke_ignorePlayer` | CS / 8 | `lkeChalcedonPWignore` (C) | `$lke_ignorePlayer = true 30` — `LKEchalcedonThreatSedge2` |
| `$lke_interceptLeaveBar1` | CS / 2 | `lkeChalcedonBarDrinkLotsMore2bar` (C) | `$lke_interceptLeaveBar1 = true 0` — `lkeChalcedonBarDrinkLotsMore2` |
| `$lke_interceptLeaveBar2` | CS / 2 | `lkeChalcedonBarReturnNextDay2bar` (C) | `$lke_interceptLeaveBar2 = true 0` — `lkeChalcedonBarReturnNextDay2` |
| `$lke_patherIntercept` | C / 1 | `lkePatherFleet` (C) | `impl/campaign/missions/luddic/LuddicKnightErrant.java` |
| `$lke_ref` | CS / 2 | `lkeRaidVirens` (C) | `impl/campaign/missions/luddic/LuddicKnightErrant.java` |
| `$lke_searchForBornanew` | S / 1 | `lkeChalcedonLookingBornanewA3` (S) | `impl/campaign/missions/luddic/LuddicKnightErrant.java` |
| `$lke_stage` | C / 7 | `lkeChalcedonPWgreetRaid` (C) | `impl/campaign/missions/luddic/LuddicKnightErrant.java` |
| `$lke_stoneWalling` | CS / 5 | `LKEmazalotAskPortmasterDardan` (C) | `$lke_stoneWalling = true` — `LKEmazalotAskPMYaribayB2` |
| `$lke_usedYaribayCred` | CS / 2 | `LKEmazalotAskQuartermaster3` (C) | `$lke_usedYaribayCred = true` — `LKEmazQMTryYaribay` |
| `$locationId` | C / 1 | `PKCommRelayOpt` (C) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$locked` | CS / 5 | `pods_open` (C) | `impl/campaign/CargoPodsEntityPlugin.java`, `impl/campaign/rulecmd/salvage/CargoPods.java` (more mentions) |
| `$locr_blockFirstSurvey` | CS / 1 | `LOCRblockFirstSurvey` (C) | `impl/campaign/procgen/themes/MiscellaneousThemeGenerator.java` |
| `$locr_luddic` | CS / 8 | `LOCRLremoveBecauseColonyA` (C) | `impl/campaign/missions/luddic/LostOutpostCrewReturnLuddicFind.java`, `impl/campaign/procgen/themes/MiscellaneousThemeGenerator.java` |
| `$locr_miners` | CS / 4 | `LOCRMremoveBecauseColony` (C) | `impl/campaign/procgen/themes/MiscellaneousThemeGenerator.java` |
| `$locr_pirate` | CS / 4 | `LOCRPremoveBecauseColony` (C) | `impl/campaign/procgen/themes/MiscellaneousThemeGenerator.java` |
| `$locrl_contacted` | C / 2 | `LOCRLremoveBecauseColonyA` (C) | Trace owning rule/command or generated interaction data |
| `$locrlf_HeOrShe` | T / 5 | `locrlfOfferTextBarAgain` (T) | `impl/campaign/missions/luddic/LostOutpostCrewReturnLuddicFind.java` |
| `$locrlf_HisOrHer` | T / 1 | `locrlfOfferTextBar` (T) | `impl/campaign/missions/luddic/LostOutpostCrewReturnLuddicFind.java` |
| `$locrlf_heOrShe` | T / 1 | `locrlfOfferTextBar` (T) | `impl/campaign/missions/luddic/LostOutpostCrewReturnLuddicFind.java` |
| `$locrlf_hisOrHer` | T / 6 | `locrlfOfferTextBarAgain` (T) | `impl/campaign/missions/luddic/LostOutpostCrewReturnLuddicFind.java` |
| `$locrlf_manOrWoman` | OT / 2 | `locrlfBlurbBar` (T) | `impl/campaign/missions/luddic/LostOutpostCrewReturnLuddicFind.java` |
| `$locrlf_marketName` | S / 1 | `locrlfOfferTextBarAgain` (S) | `impl/campaign/missions/luddic/LostOutpostCrewReturnLuddicFind.java` |
| `$locrlf_person` | S / 5 | `locrlfOfferBarCynicalRewardAsk` (S) | `impl/campaign/missions/luddic/LostOutpostCrewReturnLuddicFind.java` |
| `$locrlf_personName` | T / 1 | `locrlfOfferBarAcceptFirst` (T) | `impl/campaign/missions/luddic/LostOutpostCrewReturnLuddicFind.java` |
| `$locrlf_ref` | S / 4 | `locrlfOfferTextBarAgain` (S) | `impl/campaign/missions/luddic/LostOutpostCrewReturnLuddicFind.java` |
| `$locrlf_return` | C / 2 | `locrlfReturnContact` (C) | `impl/campaign/missions/luddic/LostOutpostCrewReturnLuddicFind.java` |
| `$locrlf_rewardAmount` | OST / 4 | `locrlfOfferTextBarAgain` (T) | `impl/campaign/missions/luddic/LostOutpostCrewReturnLuddicFind.java` |
| `$locrlf_rewardAmountHigher` | ST / 4 | `locrlfOfferTextBarAgainB` (T) | `impl/campaign/missions/luddic/LostOutpostCrewReturnLuddicFind.java` |
| `$locrlf_systemName` | ST / 5 | `locrlfOfferBarAcceptFirst` (T) | `impl/campaign/missions/luddic/LostOutpostCrewReturnLuddicFind.java` |
| `$lookedWindow` | CS / 2 | `lppVolturnCurateResponses2g` (C) | `$lookedWindow = true 0` — `lppVolturnShrineWindow` |
| `$lpp_finishPilgrimage` | C / 1 | `lppGileadShrineVisitAgainEnd` (C) | `impl/campaign/missions/luddic/LuddicPilgrimsPath.java` |
| `$lpt_HeOrShe` | T / 3 | `lptOfferTextBarPoor` (T) | `impl/campaign/missions/LuddicPilgrimTransport.java` |
| `$lpt_completed` | S / 2 | `LPTcheckCompletionBeholder` (S) | `$lpt_completed = true 0` — `LPTcheckCompletionBeholder` |
| `$lpt_dist` | ST / 1 | `lptOfferTextBarRich` (T) | `impl/campaign/missions/LuddicPilgrimTransport.java` |
| `$lpt_entityName` | ST / 2 | `lptOfferTextBarRich` (T) | `impl/campaign/missions/LuddicPilgrimTransport.java` |
| `$lpt_heOrShe` | T / 4 | `lptOfferTextBarRich` (T) | `impl/campaign/missions/LuddicPilgrimTransport.java` |
| `$lpt_himOrHer` | T / 2 | `LPTcheckCompletionContRichOneOff` (T) | `impl/campaign/missions/LuddicPilgrimTransport.java` |
| `$lpt_hisOrHer` | T / 4 | `lptOfferTextBarRich` (T) | `impl/campaign/missions/LuddicPilgrimTransport.java` |
| `$lpt_manOrWoman` | T / 1 | `lptBlurbBar1` (T) | `impl/campaign/missions/LuddicPilgrimTransport.java` |
| `$lpt_ref` | CS / 12 | `LPTcheckCompletion` (C) | `impl/campaign/missions/LuddicPilgrimTransport.java` |
| `$lpt_reward` | ST / 1 | `lptOfferTextBarRich` (T) | `impl/campaign/missions/LuddicPilgrimTransport.java` |
| `$lpt_sourceName` | T / 8 | `LPTcheckCompletionContGeneric` (T) | `impl/campaign/missions/LuddicPilgrimTransport.java` |
| `$lpt_systemName` | T / 2 | `lptOfferTextBarRich` (T) | `impl/campaign/missions/LuddicPilgrimTransport.java` |
| `$lpt_target` | C / 1 | `LPTcheckCompletionBeholder` (C) | `impl/campaign/missions/LuddicPilgrimTransport.java` |
| `$lpt_wealth` | C / 8 | `lptBlurbBar1` (C) | `impl/campaign/missions/LuddicPilgrimTransport.java` |
| `$mHub` | CS / 9 | `addMHubOption` (C) | `impl/campaign/missions/hub/BaseMissionHub.java` |
| `$mHub_contactSuspended` | C / 1 | `contactNoMissionsSuspended` (C) | `impl/campaign/missions/hub/BaseMissionHub.java` |
| `$madAtPC` | CS / 4 | `lkeChalcedonVIPoptsHostile5` (C) | `$madAtPC = true 0` — `lkeChalcedonGreetJangalaCrush` |
| `$madeArgArroyo` | CS / 2 | `BFFImpBuyerHub3OptF` (C) | `$madeArgArroyo = true 0` — `BFFImpBuyerHub3OptArroyo` |
| `$madeArgBornanew` | CS / 2 | `BFFImpBuyerHub3OptE` (C) | `$madeArgBornanew = true 0` — `BFFImpBuyerHub3OptE2` |
| `$madeArgChurch` | CS / 3 | `BFFImpBuyerHub3OptG` (C) | `$madeArgChurch = true` — `BFFImpBuyerConChurch1backOff` |
| `$madeArgCommission` | CS / 2 | `BFFImpBuyerHub3OptC` (C) | `$madeArgCommission = true 0` — `BFFImpBuyerHub3OptCommission` |
| `$madeArgDemarch` | CS / 4 | `BFFImpBuyerHub3OptB` (C) | `$madeArgDemarch = true 0` — `BFFImpBuyerHub3OptDemarch` |
| `$madeArgDeposit` | CS / 4 | `BFFImpBuyerHub3OptA800k` (C) | `$madeArgDeposit = true 0` — `BFFImpBuyerHub3OptDepDone` |
| `$madeArgHorus` | CS / 2 | `BFFImpBuyerHub3OptD` (C) | `$madeArgHorus = true 0` — `BFFImpBuyerHub3OptHorus` |
| `$madeArrestClaim` | CS / 7 | `sdtuTraitorPatrolOptionConfront` (C) | `$madeArrestClaim = true` — `sdtuTraitorPatrolUnderArrest1` |
| `$madeArrestThreat` | CS / 2 | `lppVolturnCurateResponses1g` (C) | `$madeArrestThreat = true 0` — `lppVolturnCurateSDReport` |
| `$madeBigThreat` | CS / 3 | `LKEvirensEndingOptI` (C) | `$madeBigThreat = true 0` — `LKEvirensThreatenWithMarines2` |
| `$madeBribeOffer` | CS / 4 | `gaATGttScanFleetHub2` (C) | `$madeBribeOffer = true` — `gaATGttScanFleet23` |
| `$madeCase` | CS / 15 | `BFFIstationTalkHubObjection2` (C) | `$madeCase = 0` — `BFFIstationBuyer1` |
| `$madeDemand` | CS / 7 | `lkeChalcedonVIPoptsHostile1` (C) | `$madeDemand = true 0` — `LKEchalVIPdemandBornanew` |
| `$madeFirstTransportOffer` | CS / 3 | `LOCRPcontact1againTransport` (C) | `$madeFirstTransportOffer = true` — `LOCRPofferTransportSpace` |
| `$madeGoodDeal` | CS / 3 | `PLSetGoodDealFlag` (C) | `$madeGoodDeal = true 0` — `rh_continueToGoodDealSel` |
| `$madeOffer` | CS / 6 | `BFFImpBuyerHubOptA` (C) | `$madeOffer = true 0` — `BFFImpBuyerOfferMillion` |
| `$madePKjoke` | CS / 3 | `pk_turnInArroyoSel1againJoke` (C) | `$madePKjoke = true` — `pk_turnInArroyoSel1joke` |
| `$madePKofferOnce` | CS / 3 | `pk_turnInArroyoSel1again` (C) | `$madePKofferOnce = true` — `pkTurnInArroyoSel2` |
| `$madeRewardDemand` | CS / 3 | `gaDHOvisitElekAltTransferDataB` (C) | `$madeRewardDemand = true 0` — `gaDHOvisitElekAltIntroBusiness` |
| `$madeStationAShrine` | CS / 2 | `salOHOGapproachAgain` (C) | `$madeStationAShrine = true` — `salOHOGoutLuddic` |
| `$madeThreat` | CS / 6 | `gaATGluddicScanHubOptionsA` (C) | `$madeThreat = true` — `gaATGluddicScan5` |
| `$makeTranverseJumpCostMoreCROnce` | S / 1 | `StrandedInDeepSpace3c` (S) | `$makeTranverseJumpCostMoreCROnce = true` — `StrandedInDeepSpace3c` |
| `$mamAnnoyedEveryone` | CS / 3 | `lkeChalcedonBarBribeWalk` (C) | `$mamAnnoyedEveryone = true 0` — `lkeChalcedonBarOffer1k` |
| `$manOrWoman` | OT / 34 | `GAKA_TTStart` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/missions/hub/BaseHubMission.java` |
| `$market.affk_bombardedColony` | CS / 1 | `affkBombardmentFinished` (C) | Trace owning rule/command or generated interaction data |
| `$market.affk_completed` | S / 1 | `affkBombardmentFinished` (S) | `$market.affk_completed = true 0` — `affkBombardmentFinished` |
| `$market.allowedToVisitShrine` | CS / 5 | `lppHesperusShrineVisitFirstFinally` (C) | `$market.allowedToVisitShrine = true` — `lppHesperusExcubitorEndAllow` |
| `$market.alreadyOnShuttle` | CS / 3 | `kdSKnowStartShuttle` (C) | `$market.alreadyOnShuttle = true 0` — `kantasDenTrafficControlBullyB` |
| `$market.anh_tookTheJob` | C / 1 | `anhCheckCompletionCont` (C) | Trace owning rule/command or generated interaction data |
| `$market.arrestedMercs` | CS / 4 | `MOTRblurbBar` (C) | `$market.arrestedMercs = true 30` — `MOTRplayerColonyNarcAllow` |
| `$market.askedVIPaboutCoureuse` | CS / 5 | `gaFCfikenhildBotherAdminAlready` (C) | `$market.askedVIPaboutCoureuse = true` — `gaFCFikenhildBotherVIPoptA3` |
| `$market.avipt_completed` | S / 1 | `AVIPTcheckCompletion` (S) | `$market.avipt_completed = true 0` — `AVIPTcheckCompletion` |
| `$market.avipt_ref` | C / 1 | `AVIPTcheckCompletion` (C) | Trace owning rule/command or generated interaction data |
| `$market.avipt_target` | C / 1 | `AVIPTcheckCompletion` (C) | Trace owning rule/command or generated interaction data |
| `$market.bffi_arrestTheCurate` | C / 3 | `BFFIarrestOptionDev` (C) | Trace owning rule/command or generated interaction data |
| `$market.bffi_attendAParty` | C / 1 | `BFFIapRaidOutro1` (C) | Trace owning rule/command or generated interaction data |
| `$market.bffi_deliverKeepfaith` | C / 2 | `BFFIdeliverOakOption` (C) | Trace owning rule/command or generated interaction data |
| `$market.bffi_followUlmusPond` | C / 1 | `BFFIulmusStartOption` (C) | Trace owning rule/command or generated interaction data |
| `$market.bffi_talkToEngineer` | C / 1 | `BFFItalkToEngineerOpt` (C) | Trace owning rule/command or generated interaction data |
| `$market.bribedIntoHesperus` | CS / 5 | `lppHesperusShrineSneakBribeReturn` (C) | `$market.bribedIntoHesperus = true 0` — `lppHesperusShrineSneakBribe` |
| `$market.bribedShrineOfficial` | CS / 2 | `lppVolturnVisitDidBribe` (C) | `$market.bribedShrineOfficial = true` — `lppVolturnOfficialBribe3` |
| `$market.buggedVIPsAboutBornanew` | CS / 14 | `LKEmazConvOptionLeaveAuto` (C) | `$market.buggedVIPsAboutBornanew++` — `LKEmazalotAskQMBornanew` |
| `$market.dardanWontTalkLKE` | CS / 4 | `LKEmazAdminWontTalk` (C) | `$market.dardanWontTalkLKE = true` — `LKEmazAdminAskBugBye` |
| `$market.daysExisted` | C / 1 | `gaDHOhookStart` (C) | Trace owning rule/command or generated interaction data |
| `$market.dcom_completed` | S / 1 | `DCOMRaidFinished` (S) | `$market.dcom_completed = true` — `DCOMRaidFinished` |
| `$market.dcom_ref` | CS / 2 | `DCOMRaidFinished` (C) | Trace owning rule/command or generated interaction data |
| `$market.dcom_target` | C / 1 | `dcomOpenMarket` (C) | Trace owning rule/command or generated interaction data |
| `$market.dhi_completed` | S / 1 | `DHIRaidFinished` (S) | `$market.dhi_completed = true 0` — `DHIRaidFinished` |
| `$market.dhi_raidedTargetIndustry` | CS / 1 | `DHIRaidFinished` (C) | Trace owning rule/command or generated interaction data |
| `$market.dhi_ref` | S / 1 | `DHIRaidFinished` (S) | Trace owning rule/command or generated interaction data |
| `$market.didFirstShrineVisit` | CS / 2 | `lppJangalaVisitAgain` (C) | `$market.didFirstShrineVisit = true` — `lppJangalaFirstVisit1` |
| `$market.didShrineIntro` | CS / 7 | `lppVolturnShrineOption` (C) | `$market.didShrineIntro = true` — `lppVolturnShrineFirstVisit2` |
| `$market.didTrafficControlBribe` | CS / 4 | `kdTrafficControlFirstTime` (C) | `$market.didTrafficControlBribe = true` — `kantasDenTrafficControlBribe` |
| `$market.displayTrophyRemnantCapital` | CS / 2 | `kpRemnantTrophyDesc` (C) | `$market.displayTrophyRemnantCapital = true` — `kpRemnantCapSel` |
| `$market.dsp_completed` | S / 1 | `dspRaidFinished` (S) | `$market.dsp_completed = true 0` — `dspRaidFinished` |
| `$market.dsp_raidedTargetIndustry` | CS / 1 | `dspRaidFinished` (C) | Trace owning rule/command or generated interaction data |
| `$market.dsp_ref` | CS / 1 | `dspRaidFinished` (C) | Trace owning rule/command or generated interaction data |
| `$market.extr_needToReturn` | S / 4 | `extrRaidFinished1` (S) | `$market.extr_needToReturn = true` — `extrRaidFinished1` |
| `$market.extr_ref` | CS / 8 | `extrOpenMarket1` (C) | Trace owning rule/command or generated interaction data |
| `$market.extr_target` | C / 4 | `extrOpenMarket1` (C) | Trace owning rule/command or generated interaction data |
| `$market.foodShortage` | S / 6 | `marketPostOpenFSEPlayerFast` (S) | `impl/campaign/events/FoodShortageEvent.java` |
| `$market.foodShortageEndedByNPC` | C / 1 | `marketPostOpenFSENonPlayer` (C) | Trace owning rule/command or generated interaction data |
| `$market.foodShortageEndedByPlayer` | C / 1 | `marketPostOpenFSEPlayer` (C) | Trace owning rule/command or generated interaction data |
| `$market.foodShortageEndedByPlayerBlack` | C / 1 | `marketPostOpenFSEPlayerBlack` (C) | Trace owning rule/command or generated interaction data |
| `$market.foodShortageEndedByPlayerFast` | C / 1 | `marketPostOpenFSEPlayerFast` (C) | Trace owning rule/command or generated interaction data |
| `$market.foodShortageExpired` | C / 1 | `marketPostOpenFSEExpired` (C) | Trace owning rule/command or generated interaction data |
| `$market.foodShortagePartiallyEndedByPlayerRemote` | C / 1 | `marketPostOpenFSEMixedIndirect` (C) | Trace owning rule/command or generated interaction data |
| `$market.gaATG_findingLoke` | C / 10 | `gaATGmeetingIsSetUpWithCotton` (C) | Trace owning rule/command or generated interaction data |
| `$market.gaATG_talkToKanta` | CS / 10 | `kdKantaGreetingGAATG` (C) | Trace owning rule/command or generated interaction data |
| `$market.gaFC_coureuseInvestigation` | C / 7 | `gaFCFikenhildOption` (C) | Trace owning rule/command or generated interaction data |
| `$market.gaFC_returnHere` | C / 1 | `gaFCReturnToAcademy` (C) | Trace owning rule/command or generated interaction data |
| `$market.gaFC_safehouse` | C / 1 | `gaFCLaicaileVisitOption` (C) | Trace owning rule/command or generated interaction data |
| `$market.gaIntro2_returnHere` | C / 3 | `gaIntro2returnStart` (C) | Trace owning rule/command or generated interaction data |
| `$market.gaIntro_completed` | S / 1 | `gaRequestMeeting` (S) | `$market.gaIntro_completed = true` — `gaRequestMeeting` |
| `$market.gaKA_getHack` | C / 1 | `gaKANewMaxiosOption` (C) | Trace owning rule/command or generated interaction data |
| `$market.gaKA_returnHere` | C / 1 | `gaKAReturnToAcademy` (C) | Trace owning rule/command or generated interaction data |
| `$market.gaKA_visitChalet` | C / 2 | `gaKAArroyoGreetingIgnoreCall` (C) | Trace owning rule/command or generated interaction data |
| `$market.gaTTB_completed` | S / 1 | `bairdIntro2` (S) | `$market.gaTTB_completed = true 0` — `bairdIntro2` |
| `$market.gaTTS_completed` | S / 1 | `asebFirstTimeGreeting` (S) | `$market.gaTTS_completed = true` — `asebFirstTimeGreeting` |
| `$market.gaVIP_target` | C / 1 | `gaVIP_checkCompletionCont` (C) | Trace owning rule/command or generated interaction data |
| `$market.gotPermissionToLandFromSK` | CS / 10 | `kdTrafficControlOptionA` (C) | `$market.gotPermissionToLandFromSK = true 3` — `kdSKgaATGSentToken1` |
| `$market.hasRuins` | C / 3 | `salRuins_alreadyExplored` (C) | Trace owning rule/command or generated interaction data |
| `$market.hasUnexploredRuins` | C / 3 | `surveySystemIsCutOffCanNotColonize` (C) | Trace owning rule/command or generated interaction data |
| `$market.id` | CS / 133 | `DEVnanoforgeEngineerOption` (C) | Trace owning rule/command or generated interaction data |
| `$market.imoinuDidWarn` | CS / 4 | `sdtuUmbraImoinuWarning` (C) | `$market.imoinuDidWarn = true` — `sdtuUmbraTrickVIPOpt2imoinu2` |
| `$market.imoinuWillWarn` | CS / 4 | `sdtuUmbraImoinuWarning` (C) | `$market.imoinuWillWarn = true 0` — `sdtuUmbraTrickVIPOpt2imoinu2` |
| `$market.ind:cryosanctum` | C / 1 | `flavorTextMarketCryosanctum` (C) | Trace owning rule/command or generated interaction data |
| `$market.ind:heavybatteries` | C / 1 | `lppHesperusShrineOptionCheck` (C) | Trace owning rule/command or generated interaction data |
| `$market.isHidden` | C / 3 | `goToTheGABarEventOption` (C) | Trace owning rule/command or generated interaction data |
| `$market.isPlanetConditionMarketOnly` | C / 29 | `abyssalGasGiantTurbulence` (C) | Trace owning rule/command or generated interaction data |
| `$market.isPlayerOwned` | C / 15 | `marketPostOpenPlayerOwnerd` (C) | Trace owning rule/command or generated interaction data |
| `$market.isSurveyed` | C / 16 | `surveyPrintFullData` (C) | Trace owning rule/command or generated interaction data |
| `$market.jabr_needToReturn` | S / 2 | `jabrRaidFinished1` (S) | `$market.jabr_needToReturn = true` — `jabrRaidFinished1` |
| `$market.jabr_ref` | CS / 4 | `jabrOpenMarket1` (C) | Trace owning rule/command or generated interaction data |
| `$market.jabr_target` | C / 4 | `jabrOpenMarket1` (C) | Trace owning rule/command or generated interaction data |
| `$market.knowAboutShrine` | CS / 1 | `lppGileadShrineVisitFirstTime` (C) | `$market.knowAboutShrine = true` — `lppGileadShrineVisitFirstTime` |
| `$market.kpAudience` | CS / 4 | `kdKPAudienceAlreadyArranged` (C) | `$market.kpAudience = true 3` — `kdKPAudienceAcceptSel` |
| `$market.liedShrineOfficial` | CS / 2 | `lppVolturnRevisitLied` (C) | `$market.liedShrineOfficial = true` — `lppVolturnOfficialLie2` |
| `$market.lkeBuggedVIPs` | CS / 8 | `lkeAskChalcedonBarPromptBugged` (C) | `$market.lkeBuggedVIPs++` — `LKEchalcedonThreatSedge2` |
| `$market.lkeMeetingSet` | CS / 4 | `lkeAskChalcedonBarPromptWaiting` (C) | `$market.lkeMeetingSet = true` — `lkeChalcedonAskBarMeetingSet` |
| `$market.lkeMeetingWait` | CS / 5 | `lkeAskChalcedonBarPromptContact` (C) | `$market.lkeMeetingWait = true 1` — `lkeChalcedonAskBarMeetingSet` |
| `$market.lkePlayerKidnapped` | CS / 5 | `lkeChalcedonBarSedgeOptKill2` (C) | `$market.lkePlayerKidnapped = true 0` — `lkeChalcedonBarDrinkLotsMore4` |
| `$market.lkeReadiedBodyguards` | CS / 2 | `lkeChalcedonBarReturnNextDay5a` (C) | `$market.lkeReadiedBodyguards = true 0` — `lkeChalcedonBarReturnNextDay2c` |
| `$market.lkeReadiedSidearm` | CS / 2 | `lkeChalcedonBarReturnNextDay5b` (C) | `$market.lkeReadiedSidearm = true 0` — `lkeChalcedonBarReturnNextDay2b` |
| `$market.lkeSetUpVirensMeeting` | CS / 8 | `LKEmazConvOptionLeaveAuto` (C) | `$market.lkeSetUpVirensMeeting = true` — `LKEmazalotFreebieAdvance3` |
| `$market.lke_askedPMChurch` | CS / 4 | `LKEmazalotAskPMresp2` (C) | `$market.lke_askedPMChurch = true` — `LKEmazalotAskPMchurch` |
| `$market.lke_contactBornanew` | C / 1 | `LKEbornanewVisitOption` (C) | Trace owning rule/command or generated interaction data |
| `$market.lke_contactVirens` | C / 2 | `LKEmazAdminWontTalk2` (C) | Trace owning rule/command or generated interaction data |
| `$market.lke_didMarines` | CS / 10 | `LKEbornanewShuttleDown2b` (C) | `$market.lke_didMarines = true 1` — `LKEbornanewPrepMarines` |
| `$market.lke_liedWithSP` | CS / 3 | `lkeChalcedonGreetJangalaCrush` (C) | `$market.lke_liedWithSP = true` — `lkeChalcedonVIPoptHostileSP3` |
| `$market.lke_returnWithBornanew` | C / 2 | `LKEreturnToGileadJaspisOption` (C) | Trace owning rule/command or generated interaction data |
| `$market.lke_returnWithBornanewNews` | C / 1 | `LKEreturnToGileadDeadJaspisOpt` (C) | Trace owning rule/command or generated interaction data |
| `$market.lke_searchForBornanew` | C / 1 | `lkeAskChalcedonBarPromptWaiting` (C) | Trace owning rule/command or generated interaction data |
| `$market.lke_searchForBornanew2` | C / 19 | `LKEmazConvOptionLeaveAuto` (C) | Trace owning rule/command or generated interaction data |
| `$market.lke_wontTellLied` | CS / 2 | `LKEmazalotAskAdminChurch` (C) | `$market.lke_wontTellLied = true` — `LKEmazalotPMwontTellLie` |
| `$market.locrlf_market` | CS / 5 | `LOCRLoffloadToChurch` (C) | Trace owning rule/command or generated interaction data |
| `$market.lpp_finishPilgrimage` | CS / 5 | `lppGileadShrineVisitOption` (C) | `$market.lpp_finishPilgrimage = true` — `lppGileadMissionEndDevH2` |
| `$market.mc:comm_relay` | C / 1 | `gaDHOhookStart` (C) | Trace owning rule/command or generated interaction data |
| `$market.mc:decivilized` | C / 2 | `surveyOpenDeciv` (C) | Trace owning rule/command or generated interaction data |
| `$market.mc:event_food_shortage` | C / 3 | `marketPostOpenFSEMixedIndirect` (C) | Trace owning rule/command or generated interaction data |
| `$market.mc:free_market` | C / 3 | `marketPostOpenNoTrade` (C) | Trace owning rule/command or generated interaction data |
| `$market.mc:ore_ultrarich` | C / 1 | `LOCRMsurveyOre` (C) | Trace owning rule/command or generated interaction data |
| `$market.mc:organics_plentiful` | C / 1 | `LOCRMsurveyOrganics` (C) | Trace owning rule/command or generated interaction data |
| `$market.mc:rare_ore_ultrarich` | C / 1 | `LOCRMsurveyRareOre` (C) | Trace owning rule/command or generated interaction data |
| `$market.mc:ruins_extensive` | C / 2 | `salRuins_extensive` (C) | Trace owning rule/command or generated interaction data |
| `$market.mc:ruins_scattered` | C / 2 | `salRuins_scattered` (C) | Trace owning rule/command or generated interaction data |
| `$market.mc:ruins_vast` | C / 2 | `salRuins_vast` (C) | Trace owning rule/command or generated interaction data |
| `$market.mc:ruins_widespread` | C / 2 | `salRuins_widespread` (C) | Trace owning rule/command or generated interaction data |
| `$market.mc:volatiles_plentiful` | C / 1 | `LOCRMsurveyVolatiles` (C) | Trace owning rule/command or generated interaction data |
| `$market.noBar` | CS / 2 | `marketOptBar` (C) | `$market.noBar = true 365` — `PKSentinelOutSequence` |
| `$market.numAudiences` | CS / 2 | `kdStationKingAudienceRepeat` (C) | `$market.numAudiences++` — `kdKPAudienceAcceptSel` |
| `$market.playerHostileTimeout` | C / 2 | `marketPostOpenNoTradeHostile` (C) | Trace owning rule/command or generated interaction data |
| `$market.playerHostileTimeoutStr` | S / 2 | `marketPostOpenNoTradeHostile` (S) | Trace owning rule/command or generated interaction data |
| `$market.printedForgeDesc` | CS / 1 | `kpKapteynHasForge` (C) | `$market.printedForgeDesc = true 0` — `kpKapteynHasForge` |
| `$market.printedRemnantCapitalDesc` | CS / 1 | `kpRemnantTrophyDesc` (C) | `$market.printedRemnantCapitalDesc = true 0` — `kpRemnantTrophyDesc` |
| `$market.ruinsExplored` | CS / 11 | `salRuins_scattered` (C) | `$market.ruinsExplored = true` — `salRuins_postSalvagePerform` |
| `$market.sdtu_meetRamOnVolturn` | S / 1 | `sdtuRamSafehouse3` (S) | Trace owning rule/command or generated interaction data |
| `$market.size` | C / 1 | `sdtuUmbraReleaseArcAgent` (C) | Trace owning rule/command or generated interaction data |
| `$market.smug_completed` | S / 1 | `smug_checkCompletionCont` (S) | `$market.smug_completed = true 0` — `smug_checkCompletionCont` |
| `$market.smug_ref` | CS / 4 | `smug_checkCompletion` (C) | Trace owning rule/command or generated interaction data |
| `$market.smug_target` | C / 3 | `smug_checkCompletion` (C) | Trace owning rule/command or generated interaction data |
| `$market.snuckIntoHesperus` | CS / 2 | `lppHesperusShrineOptionSnuckIn` (C) | `$market.snuckIntoHesperus = true 0` — `lppHesperusShrineVisitFirst1sneak` |
| `$market.soe_invitedToBall` | C / 2 | `soeArriveEventide` (C) | Trace owning rule/command or generated interaction data |
| `$market.soe_sawInvite` | CS / 2 | `soeArriveEventide` (C) | `$market.soe_sawInvite = true` — `soeArriveEventide` |
| `$market.tabo_bombardedColony` | CS / 1 | `taboBombardmentFinished` (C) | Trace owning rule/command or generated interaction data |
| `$market.tabo_completed` | S / 1 | `taboBombardmentFinished` (S) | `$market.tabo_completed = true 0` — `taboBombardmentFinished` |
| `$market.tabo_ref` | S / 1 | `taboBombardmentFinished` (S) | Trace owning rule/command or generated interaction data |
| `$market.tag:luddic_shrine` | C / 9 | `lppGileadShrineVisitFirstTime` (C) | Trace owning rule/command or generated interaction data |
| `$market.talkingAtBar` | CS / 9 | `lkeChalcedonBarSedgeOptD` (C) | `$market.talkingAtBar = true 0` — `lkeChalcedonBarSedge1` |
| `$market.talkingOutside` | CS / 10 | `lkeChalcedonBarSedgeRespA2` (C) | `$market.talkingOutside = true 0` — `lkeChalcedonBarDrinkLotsMore5` |
| `$market.toldAVIPTtoGoAway` | CS / 3 | `AVIPTblurbBar` (C) | `$market.toldAVIPTtoGoAway = true 30` — `AVIPTtextBarDeclineRude` |
| `$market.trophyRemnantCapitalName` | S / 1 | `kpRemnantCapSel` (S) | `$market.trophyRemnantCapitalName = $foundShipName` — `kpRemnantCapSel` |
| `$market.usedCommissionOnShrineOfficial` | CS / 2 | `lppVolturnVisitUsedCommission` (C) | `$market.usedCommissionOnShrineOfficial = true` — `lppVolturnOfficialCommission1` |
| `$market.wasCivilized` | C / 1 | `surveyOpenDeciv` (C) | Trace owning rule/command or generated interaction data |
| `$marketLeaveTooltip` | S / 1 | `marketAddOptionLeave` (S) | Trace owning rule/command or generated interaction data |
| `$marketName` | ST / 30 | `surveyOpenDeciv` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/econ/BaseMarketConditionPlugin.java` |
| `$marketSize` | C / 12 | `flavorTextMarketGenericSmall` (C) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$maskOff` | CS / 2 | `lppJangalaShrineExteriorLeaveB` (C) | `$maskOff = true 1` — `lppJangalaOutsideMaskOff` |
| `$mcb_barEvent` | C / 1 | `MCBShowBountyBar` (C) | Trace owning rule/command or generated interaction data |
| `$mcb_difficulty` | S / 3 | `mcbLowSel` (S) | `$mcb_difficulty = LOW 0` — `mcbLowSel` |
| `$mcb_manOrWoman` | O / 1 | `mcbOfferOptionBar` (O) | Trace owning rule/command or generated interaction data |
| `$mcb_ref` | S / 4 | `MCBShowBounty` (S) | Trace owning rule/command or generated interaction data |
| `$mentionedANHboth` | CS / 2 | `ImoinuUmbraGoOptD` (C) | `$mentionedANHboth = true` — `imoinuUmbraGoVolturnBoth` |
| `$mentionedANHkid` | CS / 2 | `ImoinuUmbraGoOptE` (C) | `$mentionedANHkid = true` — `imoinuUmbraGoVolturnKid` |
| `$mentionedIdolators` | CS / 4 | `BFFIidolTalkHubOptE` (C) | `$mentionedIdolators = true 0` — `BFFIidolTalkMundanianMoreBB` |
| `$mentionedImoinu` | CS / 2 | `DardanKatoAskImoinu` (C) | `$mentionedImoinu = true` — `DardanKatoAskImoinu2` |
| `$menuState` | CS / 117 | `DEVnanoforgeEngineerOption` (C) | `$menuState = main 0` — `marketDock` |
| `$mercContractDurStr` | S / 1 | `ome_askHireSelMerc` (S) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$mercPayment` | S / 2 | `gaVIPMercTransferVIP1` (S) | `$mercPayment = $gaVIP_mercPayment` — `gaVIPMercVIPPaymentNegotiate` |
| `$messedUpOnce` | CS / 10 | `BFFIulmusTalkPcBad2bmessedUp` (C) | `$messedUpOnce = true 0` — `BFFIulmusTalkPcBad1b` |
| `$metAlready` | CS / 14 | `gaFCZalFirstGreeting_INVESTIGATE_FIKENHILD2` (C) | `$metAlready = true` — `gaFCZalFirstGreeting_INVESTIGATE_FIKENHILD` |
| `$metalsReq` | CS / 3 | `cTapCheckCanAfford` (C) | `$metalsReq = 20000 0` — `cTap_infoText` |
| `$mh_count` | C / 11 | `asebNoMissions` (C) | `impl/campaign/missions/hub/BaseMissionHub.java` |
| `$mh_doNotPrintBlurbs` | S / 2 | `asebOneMission` (S) | `$mh_doNotPrintBlurbs = true 0` — `asebOneMission` |
| `$mh_firstInlineBlurb` | CT / 2 | `asebOneMission` (T) | `impl/campaign/missions/hub/BaseMissionHub.java` |
| `$mh_openOptionText` | O / 1 | `addMHubOption` (O) | `impl/campaign/missions/hub/BaseMissionHub.java` |
| `$missionId` | CS / 107 | `cheapComPostAccept` (C) | `$missionId = gaData` — `gaDataMissionTextBasicOrPirates` |
| `$mmBornBuyerSaidChurch` | CS / 2 | `BFFImmBornBuyerOptHubA` (C) | `$mmBornBuyerSaidChurch = true 0` — `BFFImmBornBuyerChurch` |
| `$moreHolos` | CS / 2 | `adonyaOptMoreHolos` (C) | `$moreHolos = true 0` — `adonyaMoreHolos` |
| `$moreToIt` | S / 1 | `PKSentinelAskAboutStarshipsTrust` (S) | `$moreToIt = true 0` — `PKSentinelAskAboutStarshipsTrust` |
| `$motr_numberOfMarines` | CST / 4 | `MOTRstart` (T) | `impl/campaign/missions/MercsOnTheRun.java` |
| `$motr_price` | S / 3 | `MOTRstart` (S) | `impl/campaign/missions/MercsOnTheRun.java` |
| `$motr_priceText` | ST / 2 | `MOTRstart` (T) | `impl/campaign/missions/MercsOnTheRun.java` |
| `$motr_ref` | S / 2 | `MOTRpostAccept` (S) | `impl/campaign/missions/MercsOnTheRun.java` |
| `$mpm_commodityName` | OST / 10 | `mpm_greetingNeedTOff` (T) | `impl/campaign/intel/ProcurementMissionIntel.java` |
| `$mpm_eventRef` | CS / 12 | `mpm_greetingEnough` (C) | `impl/campaign/intel/ProcurementMissionIntel.java`, `impl/campaign/intel/bar/events/TriTachLoanIncentiveScript.java` |
| `$mpm_isPlayerContact` | C / 8 | `mpm_greetingNeedTOff` (C) | `impl/campaign/intel/ProcurementMissionIntel.java` |
| `$mpm_isSpawnedByMPM` | C / 1 | `mpm_pirateEncounter` (C) | `impl/campaign/intel/ProcurementMissionIntel.java` |
| `$mpm_quantity` | S / 1 | `mpm_handOverOptionSel` (S) | `impl/campaign/intel/ProcurementMissionIntel.java` |
| `$name` | OT / 25 | `lppJangalaShrineProtestStart2` (T) | `impl/campaign/CoreCampaignPluginImpl.java`, `impl/campaign/events/BaseEventPlugin.java` |
| `$nameInText` | T / 18 | `GS_AI_CORES_open` (T) | `impl/campaign/CoreCampaignPluginImpl.java`, `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$namelessRock` | C / 5 | `mk1_namelessRockFirstTime` (C) | `impl/campaign/world/NamelessRock.java` |
| `$ngcAddOfficer` | S / 2 | `ngcMercPicked` (S) | `$ngcAddOfficer = true` — `ngcMercPicked` |
| `$ngcExplorerSelected` | S / 1 | `ngcExplorerPicked` (S) | `$ngcExplorerSelected = true` — `ngcExplorerPicked` |
| `$ngcMercSelected` | S / 1 | `ngcMercPicked` (S) | `$ngcMercSelected = true` — `ngcMercPicked` |
| `$ngcRandomSelected` | S / 1 | `ngcRandomPicked` (S) | `$ngcRandomSelected = true` — `ngcRandomPicked` |
| `$ngcSkipTutorial` | S / 2 | `ngcSpacerContinue` (S) | `$ngcSkipTutorial = true` — `ngcSpacerContinue` |
| `$ngcSpacerSelected` | S / 1 | `ngcSpacerPicked` (S) | `$ngcSpacerSelected = true` — `ngcSpacerPicked` |
| `$noDeal` | S / 1 | `gaFCIsirahMercGetNothing` (S) | `$noDeal = true` — `gaFCIsirahMercGetNothing` |
| `$noHazard` | CS / 2 | `gaDHOendingStartOptsC` (C) | `$noHazard = true 0` — `gaDHOendingRewardFirstBnm` |
| `$noPitch` | CS / 3 | `raoNerieneConvOutPitchCheckB` (C) | `$noPitch = true 0` — `raoNerieneSelLeftB` |
| `$noQuestions` | CS / 8 | `ZGRthreatTechAskNone` (C) | `$noQuestions = true 0` — `ZGRthreatTechAskQ` |
| `$notHelpful` | CS / 5 | `ImoinuUmbraGoOptNotHelpful` (C) | `$notHelpful++` — `imoinuUmbraGoCavin` |
| `$notJustCandle` | CS / 4 | `lppVolturnCurateResponses2f` (C) | `$notJustCandle = true 0` — `lppVolturnCurateNotJustCandle` |
| `$numSeen` | CS / 2 | `warnAttackEncounter1` (C) | `$numSeen++` — `warnAttackEncounter1` |
| `$objectiveNonFunctional` | C / 13 | `cob_hackOpt` (C) | `impl/campaign/ids/MemFlags.java` |
| `$offerRedux` | CS / 3 | `ZGRomkiScanOfferReduxLaterB` (C) | `$offerRedux = true 0` — `ZGRomkiScanOfferReduxReroute` |
| `$offerToTakeMedicalCasesMade` | CS / 2 | `pkSentinelHubMedicalCases` (C) | `$offerToTakeMedicalCasesMade = true 0` — `PKSentinelStayHereTrust` |
| `$offered500k` | CS / 2 | `BFFImpBuyerHubOptB` (C) | `$offered500k = true 0` — `BFFImpBuyerOffer500k` |
| `$offered700k` | S / 1 | `BFFImpBuyerOffer700k` (S) | `$offered700k = true 0` — `BFFImpBuyerOffer700k` |
| `$offered800k` | CS / 2 | `BFFImpBuyerHub3OptA800k` (C) | `$offered800k = true 0` — `BFFImpBuyerOffer800kJeffReact` |
| `$offered900k` | CS / 3 | `BFFImpBuyerHub3OptA900k` (C) | `$offered900k = true 0` — `BFFImpBuyerOffer800kdemarch` |
| `$offeredContact` | CS / 3 | `raoAskDiktatMerc` (C) | `$offeredContact = true` — `RaoPitchAccept` |
| `$offeredMillion` | CS / 2 | `BFFImpBuyerHub3OptA1m` (C) | `$offeredMillion = true 0` — `BFFImpBuyerOfferMillion` |
| `$offeredToSellOneslaught` | CS / 8 | `ZGRpostGADHOelekHubOptC` (C) | `$offeredToSellOneslaught = true` — `ZGRmakePitch9OMKIbuy` |
| `$offeringMade` | CS / 4 | `lppBeholderShrineOfferingAlreadyMade` (C) | `$offeringMade = true 60` — `lppBeholderShrineGiveOffering` |
| `$ome_adminTier` | C / 1 | `ome_askSkillsSelNoSkillAdmin` (C) | `impl/campaign/events/OfficerManagerEvent.java`, `util/Misc.java` |
| `$ome_eventRef` | CS / 9 | `ome_askHire` (C) | `impl/campaign/events/OfficerManagerEvent.java` |
| `$ome_hireable` | C / 12 | `ome_greeting` (C) | `impl/campaign/events/OfficerManagerEvent.java` |
| `$ome_hiringBonus` | S / 3 | `ome_askHireSel` (S) | `impl/campaign/events/OfficerManagerEvent.java` |
| `$ome_isAdmin` | C / 4 | `ome_greetingAdmin` (C) | `impl/campaign/events/OfficerManagerEvent.java` |
| `$ome_salary` | S / 3 | `ome_askHireSel` (S) | `impl/campaign/events/OfficerManagerEvent.java` |
| `$onOrAt` | T / 1 | `marketOpenAfterEstablishOutpost` (T) | `impl/campaign/CoreCampaignPluginImpl.java`, `impl/campaign/events/BaseEventPlugin.java` |
| `$oneslaughtSensorArray` | C / 1 | `gaDHOjustFoundArrayStart` (C) | `impl/campaign/world/NamelessRock.java` |
| `$onslaughtMkI` | C / 4 | `mk1_wreckDetailsFirstTime` (C) | `impl/campaign/missions/academy/GADetectHyperspaceOddity.java`, `impl/campaign/world/NamelessRock.java` |
| `$option` | CS / 6163 | `defaultLeave` (C) | `$option = ShroudedHMM_talk1 0` — `ShroudedHullmodItemRCMantle` |
| `$otherCommissionFaction` | T / 1 | `plReynardHannanCommissionOther` (T) | `impl/campaign/rulecmd/missions/Commission.java` |
| `$otherFleetName` | ST / 4 | `ciLongRangeCommBurst` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$otherShipOrFleet` | S / 8 | `LPTitheCheck` (S) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$patherBaseCommander` | C / 5 | `patherBaseCommanderGreetingDev` (C) | `impl/campaign/intel/bases/LuddicPathBaseIntel.java` |
| `$patherFuel` | CST / 4 | `gaProbeOfferAid` (T) | `$patherFuel = 50 0` — `gaProbePatherOpenComm` |
| `$patherSupplies` | CST / 4 | `gaProbeOfferAid` (T) | `$patherSupplies = 20 0` — `gaProbePatherOpenComm` |
| `$patrolAllowTOff` | S / 1 | `scanTalkYourWayOut` (S) | `impl/campaign/ids/MemFlags.java` |
| `$personFaction` | T / 1 | `igr_offerBribeAccepedEndWarning` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$personFaction.id` | CS / 76 | `gaPZSellBlackmailOptionA` (C) | Trace owning rule/command or generated interaction data |
| `$personFaction.numGRInvestigations` | C / 3 | `igr_greetingReject` (C) | Trace owning rule/command or generated interaction data |
| `$personFirstName` | T / 2 | `psi_pilotGreeting` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/missions/hub/BaseHubMission.java` |
| `$personLastName` | T / 98 | `greetingDefaultInfamous` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/missions/hub/BaseHubMission.java` |
| `$personName` | OST / 139 | `greetingDefaultTOffNormal` (T) | `impl/campaign/CoreCampaignPluginImpl.java`, `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` (more mentions) |
| `$personPost` | T / 6 | `lkeChalcedonPWgreetRaid` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/missions/hub/BaseHubMission.java` |
| `$personRank` | T / 51 | `greetingDefaultHostileWeakerDefiant` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/missions/hub/BaseHubMission.java` |
| `$pirateBaseCommander` | C / 4 | `pirateStationKingGreeting` (C) | `impl/campaign/intel/bases/PirateBaseIntel.java` |
| `$piratePayment` | S / 2 | `gaVIPPirateAgreeResult2` (S) | `$piratePayment = $gaVIP_piratePayment` — `gaVIPPirateDemandPaymentConfirm` |
| `$pitchApprove` | S / 2 | `RaoPitchChurchMaybe` (S) | `$pitchApprove = true 0` — `RaoPitchChurchMaybe` |
| `$pitchNope` | CS / 8 | `RaoLookingForWorkOption` (C) | `$pitchNope = true 30` — `RaoPitchTestTooSoon` |
| `$pitchYes` | S / 1 | `RaoPitchAccept` (S) | `$pitchYes = true` — `RaoPitchAccept` |
| `$pkDockyard` | C / 1 | `PKSentinelDockyardOpen` (C) | `impl/campaign/procgen/themes/MiscellaneousThemeGenerator.java` |
| `$pk_detectedCommLink` | CS / 2 | `pkCacheDefenderPreEmpt` (C) | `$pk_detectedCommLink = true` — `pkCacheDefenderPreEmpt` |
| `$pk_nexusSystemName` | T / 1 | `PKPatherRecoverMissionSel` (T) | `impl/campaign/missions/RecoverAPlanetkiller.java` |
| `$pk_stage` | C / 1 | `PKCommRelayOpt` (C) | `impl/campaign/missions/RecoverAPlanetkiller.java` |
| `$pk_triedCommLink` | CS / 2 | `pkCacheDefenderPreEmpt` (C) | `$pk_triedCommLink = true 0` — `pkCacheDefenderPreEmpt` |
| `$plDuesPercent` | ST / 2 | `plReynardHannanJoinSelYes2` (T) | `impl/campaign/rulecmd/HA_CMD.java` |
| `$player.BFFIconfessedYaribayCredAbuse` | S / 1 | `BFFIonBoardYarbRespBconfess` (S) | `$player.BFFIconfessedYaribayCredAbuse = true` — `BFFIonBoardYarbRespBconfess` |
| `$player.BFFIdidntConfessYaribayCredAbuse` | S / 1 | `BFFIonBoardYarbRespBnoConfess` (S) | `$player.BFFIdidntConfessYaribayCredAbuse = true` — `BFFIonBoardYarbRespBnoConfess` |
| `$player.BFFIgaveBornanewStateroom` | S / 2 | `BFFIonBoardingRespWork` (S) | `$player.BFFIgaveBornanewStateroom = true` — `BFFIonBoardingRespWork` |
| `$player.BFFImadeBornanewBackChurchFundingLie` | CS / 3 | `BFFIproblemCheckFundingLie` (C) | `$player.BFFImadeBornanewBackChurchFundingLie = true` — `BFFImpBuyerConChurch1commit` |
| `$player.MOTRethosGenerous` | CS / 1 | `MOTRethosGenerous` (C) | `$player.MOTRethosGenerous = true` — `MOTRethosGenerous` |
| `$player.MOTRethosOrder` | CS / 1 | `MOTRethosOrder` (C) | `$player.MOTRethosOrder = true` — `MOTRethosOrder` |
| `$player.PLAgiveMyRegards` | S / 2 | `PLArmadaIdealisticYes` (S) | `$player.PLAgiveMyRegards = true` — `PLArmadaIdealisticYes` |
| `$player.PLAgiveMyRegardsName` | S / 2 | `PLArmadaIdealisticYes` (S) | `$player.PLAgiveMyRegardsName = $personName` — `PLArmadaIdealisticYes` |
| `$player.ability:fracture_jump` | C / 3 | `gaTJMissionText5reponse1` (C) | Trace owning rule/command or generated interaction data |
| `$player.ability:gravitic_scan` | C / 1 | `asebGivePlayerNDetectorOnAccept` (C) | Trace owning rule/command or generated interaction data |
| `$player.abusedYaribayCred` | CS / 8 | `BFFIonBoardYarbRespB` (C) | `$player.abusedYaribayCred++` — `LKEmazalotAskPMYaribay` |
| `$player.acceptedRamBeating` | CS / 2 | `sdtuStartInfo2b` (C) | `$player.acceptedRamBeating = true` — `sdBarRaidBeatingHadItComing` |
| `$player.agreedToPayHouseHannanAgain` | CS / 3 | `RHannanBusinessNoColony` (C) | `$player.agreedToPayHouseHannanAgain = true` — `rh_confirmPayAgainSel` |
| `$player.agreedWithSedgeTheology` | CS / 3 | `lkeSedgeContactCheckB` (C) | `$player.agreedWithSedgeTheology = true` — `lkeSedgeFaithB` |
| `$player.askedArroyoAboutZGR` | CS / 5 | `RayanArroyoAskAboutZGRoptB` (C) | `$player.askedArroyoAboutZGR = true` — `RayanArroyoAskAboutZGR1` |
| `$player.askedOpsChiefAboutAbyssalLight` | CS / 2 | `abyssalLight_opsOpt` (C) | `$player.askedOpsChiefAboutAbyssalLight = true` — `abyssalLightAskOps` |
| `$player.askedScholarship` | CS / 4 | `gaRequestMeeting5b` (C) | `$player.askedScholarship = true 0` — `gaMeetingScholarship` |
| `$player.atrocities` | C / 9 | `greetingDefaultInfamous` (C) | Trace owning rule/command or generated interaction data |
| `$player.attemptedComSupCargoPodScamEver` | S / 1 | `bqfsAskedForSuppliesScamOut` (S) | `$player.attemptedComSupCargoPodScamEver = true` — `bqfsAskedForSuppliesScamOut` |
| `$player.bffiChangedFromBuyerToRaid` | S / 1 | `BFFImpBuyerCallRaid` (S) | `$player.bffiChangedFromBuyerToRaid = true` — `BFFImpBuyerCallRaid` |
| `$player.bffi_betrayedMenesToHorus` | CS / 6 | `BFFImenesPPtalkOptB` (C) | `$player.bffi_betrayedMenesToHorus = true` — `BFFIapHorus19` |
| `$player.bffi_boughtRelic` | CS / 5 | `BFFImenesPostPartyTalk` (C) | `$player.bffi_boughtRelic = true` — `BFFIpartyOutro1buyer` |
| `$player.bffi_carriedGloveOpenly` | CS / 3 | `BFFIapHorus21b` (C) | `$player.bffi_carriedGloveOpenly = true` — `BFFIapHorus20b` |
| `$player.bffi_caughtKeepfaithAfterShotTartessus` | CS / 4 | `BFFIarrestShotBornCaught` (C) | `$player.bffi_caughtKeepfaithAfterShotTartessus = true` — `BFFIarrestShotBorn2b` |
| `$player.bffi_didPostPartyTalk` | CS / 3 | `BFFImenesPostPartyBoughtAgain` (C) | `$player.bffi_didPostPartyTalk = true` — `BFFImenesPostPartyBoughtOpt2` |
| `$player.bffi_gotShotOnTartessus` | CS / 2 | `BFFIarrestCurateEndGotShot` (C) | Trace owning rule/command or generated interaction data |
| `$player.bffi_helpedBornanewAfterShotTartessus` | CS / 5 | `BFFIarrestShotBornEscape` (C) | `$player.bffi_helpedBornanewAfterShotTartessus = true` — `BFFIarrestShotBorn2a` |
| `$player.bffi_playerKilledSedgeAfterPond` | CS / 4 | `BFFIstationTalk2sedgeZ` (C) | `$player.bffi_playerKilledSedgeAfterPond = true` — `BFINdevSetPlayerKilledSedgeLater` |
| `$player.bffi_pondFateKilledBySedge` | C / 1 | `BFFImenesPPpondOptB` (C) | Trace owning rule/command or generated interaction data |
| `$player.bffi_pondFateKnights` | CS / 4 | `BFFImenesPPpondOptD` (C) | `$player.bffi_pondFateKnights = true` — `BFFIpondNoSedgeOutro2knightsA` |
| `$player.bffi_pondFateMenes` | CS / 6 | `BFFImenesPPpondOptC` (C) | `$player.bffi_pondFateMenes = true` — `BFFIpondNoSedgeOutro2menesFool` |
| `$player.bffi_pondFatePath` | CS / 2 | `BFFImenesPPpondOptE` (C) | `$player.bffi_pondFatePath = true` — `BFFIpondNoSedgeOutro2dealB` |
| `$player.bffi_pondFatePathInsisted` | S / 1 | `BFFIpondNoSedgeOutro2badEb` (S) | `$player.bffi_pondFatePathInsisted = true` — `BFFIpondNoSedgeOutro2badEb` |
| `$player.bffi_postMenesParty` | CS / 3 | `BFFImenesPostPartyTalk` (C) | `$player.bffi_postMenesParty = true` — `BFFIapRaidOutro9` |
| `$player.bffi_raidedForRelic` | CS / 6 | `BFFImenesPPtalkOptA` (C) | `$player.bffi_raidedForRelic = true` — `BFFIapRaidOutro9` |
| `$player.bffi_saidEpicQuest` | CS / 2 | `BFFImeetMenesBornanew2c` (C) | `$player.bffi_saidEpicQuest = true 0` — `BFFImeetMenes4c` |
| `$player.bffi_saidHardened` | CS / 2 | `BFFImeetMenesBornanew2a` (C) | `$player.bffi_saidHardened = true 0` — `BFFImeetMenes4a` |
| `$player.bffi_saidHideTerrorists` | CS / 2 | `BFFImeetMenesBornanew2b` (C) | `$player.bffi_saidHideTerrorists = true 0` — `BFFImeetMenes4b` |
| `$player.bladeSkill` | CS / 7 | `soeDuelTutorialStart` (C) | `$player.bladeSkill = true` — `soeDuelPrepStudied` |
| `$player.bornanewGavePathOfLightTalk` | CS / 5 | `LKEjethroAngelsTalkTellZigOmen` (C) | `$player.bornanewGavePathOfLightTalk = true` — `LKEjethroAngelsTalkOmen` |
| `$player.botheredDaudAboutFleet` | S / 1 | `gaATGdaudMeetingHubD2` (S) | `$player.botheredDaudAboutFleet = true` — `gaATGdaudMeetingHubD2` |
| `$player.botheredDaudAboutInspections` | S / 1 | `gaATGdaudMeetingHubE1` (S) | `$player.botheredDaudAboutInspections = true` — `gaATGdaudMeetingHubE1` |
| `$player.botheredDaudAboutTariffs` | S / 1 | `gaATGdaudMeetingHubD1` (S) | `$player.botheredDaudAboutTariffs = true` — `gaATGdaudMeetingHubD1` |
| `$player.bribedTTMercAttack` | S / 1 | `ttma_bribeConfirmSel` (S) | `$player.bribedTTMercAttack = true` — `ttma_bribeConfirmSel` |
| `$player.brokeDiktatDeal` | C / 2 | `SDMakeDealOpt` (C) | Trace owning rule/command or generated interaction data |
| `$player.brokeLuddicChurchDeal` | C / 1 | `LCMakeDealOpts` (C) | Trace owning rule/command or generated interaction data |
| `$player.brokeTriTachDeal` | C / 9 | `ZGRacknowledgeTTDeal` (C) | Trace owning rule/command or generated interaction data |
| `$player.canMakeDwellerWeapons` | CS / 3 | `shroudedSubstrateBegin` (C) | `$player.canMakeDwellerWeapons = true` — `shroudedSubstrateOptSelF` |
| `$player.commissionFactionId` | C / 15 | `lppVolturnShrineRevisitSD2` (C) | Trace owning rule/command or generated interaction data |
| `$player.counterRaidedTriTach` | C / 9 | `TTCR_BountyHunterOptionsButWolf` (C) | Trace owning rule/command or generated interaction data |
| `$player.credits` | CST / 17 | `asebScholarshipPitch` (T) | Trace owning rule/command or generated interaction data |
| `$player.creditsStr` | S / 3 | `ome_askHireSel` (S) | Trace owning rule/command or generated interaction data |
| `$player.creditsStrC` | S / 5 | `lkeChalcedonAskBarRespC` (S) | Trace owning rule/command or generated interaction data |
| `$player.crew` | C / 14 | `cTapCheckCanAfford` (C) | Trace owning rule/command or generated interaction data |
| `$player.crewRoom` | C / 8 | `dstr_updateCrewOption` (C) | Trace owning rule/command or generated interaction data |
| `$player.crewRoomStr` | ST / 4 | `LOCRLaskHowManyCa` (T) | Trace owning rule/command or generated interaction data |
| `$player.declaredWillKillCotton` | S / 1 | `lppHookCurateCottonKill` (S) | `$player.declaredWillKillCotton = true` — `lppHookCurateCottonKill` |
| `$player.declaredWillTalkToCotton` | S / 1 | `lppHookCurateCottonTalk2` (S) | `$player.declaredWillTalkToCotton = true` — `lppHookCurateCottonTalk2` |
| `$player.defeatedDiktatAttack` | C / 2 | `SDMakeDealOpt` (C) | Trace owning rule/command or generated interaction data |
| `$player.defeatedHegemony` | C / 1 | `heg_talkAboutInspectionsOpt` (C) | Trace owning rule/command or generated interaction data |
| `$player.defeatedLeagueBlockade` | C / 1 | `PLDealArgumentsDefeatedBlockade` (C) | Trace owning rule/command or generated interaction data |
| `$player.defeatedLeaguePunEx` | C / 2 | `PLDealArgumentsDefeatedBlockade` (C) | Trace owning rule/command or generated interaction data |
| `$player.defeatedLeagueSDF` | CS / 2 | `PLDealARgumentsDefeatedSDF` (C) | `$player.defeatedLeagueSDF = true` — `SDFLeagueDefeated` |
| `$player.defeatedLuddicChurchExpedition` | C / 1 | `LCMakeDealOpts` (C) | Trace owning rule/command or generated interaction data |
| `$player.demandedIndependenceFromDaud` | S / 1 | `hegTalkInspectDaudVictoryB` (S) | `$player.demandedIndependenceFromDaud = true` — `hegTalkInspectDaudVictoryB` |
| `$player.demandedReparationsFromDaud` | S / 1 | `hegTalkInspectDaudVictoryC` (S) | `$player.demandedReparationsFromDaud = true` — `hegTalkInspectDaudVictoryC` |
| `$player.didAShroudedHullmodUnlock` | CS / 8 | `ShroudedHullmodItemRCMantle1b` (C) | `$player.didAShroudedHullmodUnlock = true` — `ShroudedHullmodItemRCMantle5` |
| `$player.didCGRweirdHullmodReaction` | CS / 3 | `CGRmarketPostWeirdHullmods` (C) | `$player.didCGRweirdHullmodReaction = true 1` — `CGRmarketWeirdEnd` |
| `$player.didNotSitWithCottonCount` | S / 3 | `gaPZ_cottonStand` (S) | `$player.didNotSitWithCottonCount++` — `gaPZ_cottonStand` |
| `$player.didPLApiracyTalk` | C / 1 | `PLArmadaHubOptionA` (C) | Trace owning rule/command or generated interaction data |
| `$player.didPathWeirdHullmodReaction` | CS / 6 | `PathMarketPostWeirdHullmods` (C) | `$player.didPathWeirdHullmodReaction = true 365` — `PathMarketWeird2` |
| `$player.didPiracyTalkWithPLA` | S / 1 | `PLArmadaThisIsPiracy` (S) | `$player.didPiracyTalkWithPLA = true` — `PLArmadaThisIsPiracy` |
| `$player.didSDpunExCommsArrogant` | CS / 1 | `sdPunExCommsArrogant` (C) | `$player.didSDpunExCommsArrogant = true` — `sdPunExCommsArrogant` |
| `$player.didScholarship100k` | CS / 2 | `asebScholarshipOutroA` (C) | `$player.didScholarship100k = true` — `asebScholarship100k` |
| `$player.didScholarship200k` | CS / 2 | `asebScholarshipOutroB` (C) | `$player.didScholarship200k = true` — `asebScholarship200k` |
| `$player.didScholarship500k` | CS / 2 | `asebScholarshipOutroC` (C) | `$player.didScholarship500k = true` — `asebScholarship500k` |
| `$player.didVambraceSampleInvestigation` | CS / 3 | `oyaTanaicaAskSampleOption` (C) | `$player.didVambraceSampleInvestigation = true` — `oyaTanaica_vambraceOut1` |
| `$player.didWhyPLharassingTalk` | CS / 4 | `gaFCarchonAskDemarchonOptsB` (C) | `$player.didWhyPLharassingTalk = true` — `gaFCArchonSearchHubOptionDEV2` |
| `$player.didZGRfirstMeeting` | CS / 9 | `ZGRomkiScanOfferOpt` (C) | `$player.didZGRfirstMeeting = true` — `ZGRstartPitchOutroB` |
| `$player.discussedHegemonyDefeat` | CS / 2 | `heg_talkAboutInspectionsOpt` (C) | `$player.discussedHegemonyDefeat = true` — `hegTalkInspectDaud2` |
| `$player.encounteredDweller` | CS / 11 | `ZGRgreetingPostWeirdAll` (C) | `$player.encounteredDweller = true` — `ZGRdevGive2` |
| `$player.encounteredPKDefenderFleet` | CS / 12 | `PKSentinelHubConfrontCheckB` (C) | `$player.encounteredPKDefenderFleet = true` — `pk_ignoreSel` |
| `$player.encounteredThreat` | CS / 11 | `ZGRgreetingPostWeirdAll` (C) | `$player.encounteredThreat = true` — `ZGRdevGive2` |
| `$player.encounteredWeird` | CS / 16 | `TTmarketPostWeirdHullmods` (C) | `$player.encounteredWeird = true` — `ZGRdevGive2` |
| `$player.ethosAntiAI` | S / 4 | `gaOpPlanetRogueAIStrike1` (S) | `$player.ethosAntiAI++` — `gaOpPlanetRogueAIStrike1` |
| `$player.ethosCocky` | S / 30 | `LKEpatherFleetThreatA` (S) | `$player.ethosCocky++` — `LKEpatherFleetThreatA` |
| `$player.ethosCynical` | CS / 9 | `BFFItalkToEngineer12optDev1` (C) | `$player.ethosCynical = 2` — `BFFItalkToEngineer12optDev1sel` |
| `$player.ethosFreedom` | S / 3 | `LKEreturnToGileadDeadJaspis6b2` (S) | `$player.ethosFreedom++` — `LKEreturnToGileadDeadJaspis6b2` |
| `$player.ethosGenerous` | S / 8 | `asebScholarshipCauseAsel` (S) | `$player.ethosGenerous++` — `asebScholarshipCauseAsel` |
| `$player.ethosHonorable` | S / 9 | `gaFCArchonLetMeSeeHerAppeal` (S) | `$player.ethosHonorable++` — `gaFCArchonLetMeSeeHerAppeal` |
| `$player.ethosHumanitarian` | CS / 34 | `BFFIpsRuseNiceDoptCheck` (C) | `$player.ethosHumanitarian++` — `abyssalLightAskOpsOutA` |
| `$player.ethosHumble` | S / 2 | `BarHREHumble1` (S) | `$player.ethosHumble++` — `BarHREHumble1` |
| `$player.ethosIdealistic` | S / 6 | `BFFIpondNoSedgeOutro2menesKind` (S) | `$player.ethosIdealistic++` — `BFFIpondNoSedgeOutro2menesKind` |
| `$player.ethosKnowledge` | S / 14 | `abyssalLightAskOpsOutB` (S) | `$player.ethosKnowledge++` — `abyssalLightAskOpsOutB` |
| `$player.ethosLiar` | S / 73 | `lkeChalcedonVIPoptHostileSP3` (S) | `$player.ethosLiar++` — `lkeChalcedonVIPoptHostileSP3` |
| `$player.ethosMercenary` | CS / 34 | `BFFItalkToEngineer12optDev2` (C) | `$player.ethosMercenary = 2` — `BFFItalkToEngineer12optDev2sel` |
| `$player.ethosMercy` | S / 13 | `LKEpatherFleetServeFaithA1` (S) | `$player.ethosMercy++` — `LKEpatherFleetServeFaithA1` |
| `$player.ethosOrder` | S / 26 | `lppJangalaProtestMarinesHelpHeg2l` (S) | `$player.ethosOrder++` — `lppJangalaProtestMarinesHelpHeg2l` |
| `$player.ethosProAI` | S / 4 | `asebScholarshipCauseDsel` (S) | `$player.ethosProAI++` — `asebScholarshipCauseDsel` |
| `$player.ethosRuthless` | CS / 34 | `BFFIpsRuseMeanCoptCheck` (C) | `$player.ethosRuthless++` — `lppJangalaProtestMarines3` |
| `$player.ethosSarcastic` | S / 27 | `oyaTanaicaVambrace5a` (S) | `$player.ethosSarcastic++` — `oyaTanaicaVambrace5a` |
| `$player.ethosTruth` | S / 12 | `lkeMazBarAgentRespArchcurate` (S) | `$player.ethosTruth++` — `lkeMazBarAgentRespArchcurate` |
| `$player.ethosUseAI` | S / 4 | `asebScholarshipCauseCsel` (S) | `$player.ethosUseAI++` — `asebScholarshipCauseCsel` |
| `$player.everAskedForFreeSupplies` | CS / 6 | `bqfsAskForSuppliesOptionFirst` (C) | `$player.everAskedForFreeSupplies = true` — `bqfsAskedForSuppliesFirstNo` |
| `$player.everHadKantaProtection` | CS / 9 | `relLevelPiratesKantasProtection` (C) | `$player.everHadKantaProtection = true` — `kdDevSetHadKantaProtection2` |
| `$player.excommunicatedFromChurch` | CS / 3 | `CGRpatrolWeirdModsEnd3b` (C) | `$player.excommunicatedFromChurch = true` — `CGRmarketWeirdOutroFaith` |
| `$player.exploredStationDecapitationStrike` | CS / 2 | `salSRDSstart` (C) | `$player.exploredStationDecapitationStrike = true` — `salSRDSout` |
| `$player.exploredStationMiningDeadStrikers` | CS / 2 | `salSMDSstart` (C) | `$player.exploredStationMiningDeadStrikers = true` — `salSMDSout` |
| `$player.exploredStationMiningOutland` | CS / 2 | `salSMOLstart` (C) | `$player.exploredStationMiningOutland = true` — `salSMOLout` |
| `$player.exploredStationMiningPirateAttack` | CS / 2 | `salSMPAstart` (C) | `$player.exploredStationMiningPirateAttack = true` — `salSMPAout` |
| `$player.exploredStationOvergrown` | CS / 3 | `salOHOGstart` (C) | `$player.exploredStationOvergrown = true` — `salOHOGout` |
| `$player.exploredStationOzymandias` | CS / 2 | `salOHOZstart` (C) | `$player.exploredStationOzymandias= true` — `salOHOZout` |
| `$player.exploredStationRadiationSurge` | CS / 2 | `salSRRSstart` (C) | `$player.exploredStationRadiationSurge = true` — `salSRRSout` |
| `$player.exploredStationResearchBioweapons` | CS / 2 | `salSRBWstart` (C) | `$player.exploredStationResearchBioweapons = true` — `salSRBWout` |
| `$player.exploredStationSecurityFritz` | CS / 2 | `salSRSFstart` (C) | `$player.exploredStationSecurityFritz = true` — `salSRSFout` |
| `$player.expressedInterestInJoiningPLToEnforcer` | CS / 2 | `plEnforcerJoinLeagueGoodSel` (C) | `$player.expressedInterestInJoiningPLToEnforcer = true` — `plEnforcerJoinLeagueGoodSel` |
| `$player.fcm_faction` | C / 98 | `CGRmarketWeirdOutroCom` (C) | Trace owning rule/command or generated interaction data |
| `$player.finishedTutorial` | CS / 3 | `gaRequestMeeting4noTutorial` (C) | `$player.finishedTutorial = true` — `tut_janContactReport3` |
| `$player.firstName` | T / 1 | `RACA_answer` (T) | Trace owning rule/command or generated interaction data |
| `$player.flagshipName` | T / 1 | `gaOpPlanetRogueAINegotiate1` (T) | Trace owning rule/command or generated interaction data |
| `$player.fleetDamaged` | C / 2 | `bqfsAskedForSuppliesAcceptB` (C) | Trace owning rule/command or generated interaction data |
| `$player.fleetDamagedLots` | C / 1 | `bqfsAskedForSuppliesAcceptC` (C) | Trace owning rule/command or generated interaction data |
| `$player.fleetId` | S / 2 | `greetingDefaultTurnOnT` (S) | Trace owning rule/command or generated interaction data |
| `$player.fleetLowCR` | C / 2 | `bqfsAskedForSuppliesScam` (C) | Trace owning rule/command or generated interaction data |
| `$player.fleetPoints` | C / 13 | `relLevelHostilePiratesFleet` (C) | Trace owning rule/command or generated interaction data |
| `$player.foundUlmusPondSafehouse` | CS / 3 | `BFFImenesPPpondOptA` (C) | `$player.foundUlmusPondSafehouse = true` — `BFFIulmusStartOption` |
| `$player.fuel` | CST / 7 | `dstr_showEnoughFuel` (T) | Trace owning rule/command or generated interaction data |
| `$player.gaDHO_promisedElekNotToRevealPatron` | S / 2 | `gaDHOvisitElekAltWrapupTT2a` (S) | `$player.gaDHO_promisedElekNotToRevealPatron = true` — `gaDHOvisitElekAltWrapupTT2a` |
| `$player.gaDHO_promisedElekNotToRevealPatronLied` | S / 1 | `gaDHOvisitElekAltWrapupTT2c` (S) | `$player.gaDHO_promisedElekNotToRevealPatronLied = true` — `gaDHOvisitElekAltWrapupTT2c` |
| `$player.gaKAbluffedViaOrcusRao` | CS / 4 | `raoGreetingFastOne` (C) | `$player.gaKAbluffedViaOrcusRao = true` — `gaKAPatrolLie2` |
| `$player.gaPZ_metRogueKnight` | CS / 4 | `GAPZCottonShakeContOptions1` (C) | `$player.gaPZ_metRogueKnight = true` — `gaPZKnightOpenComm` |
| `$player.gaPZ_metRogueKnightTithed` | CS / 2 | `gaATGmeetCottonTeaKnightA` (C) | `$player.gaPZ_metRogueKnightTithed = true` — `gaPZ_rkAgree` |
| `$player.gaveDaudYaribayContact` | CS / 10 | `BFFIonBoardYarbRespC` (C) | `$player.gaveDaudYaribayContact = true` — `BFINdevForceKnowHorus` |
| `$player.gaveHegInvestigatorAIresponse` | CS / 9 | `hegInevestigatorRespAItoolOpt` (C) | `$player.gaveHegInvestigatorAIresponse = true` — `hegInevestigatorRespAItoolSelWeak` |
| `$player.gavePristineNanoforgeToKanta` | CS / 2 | `kpKapteynHasForge` (C) | `$player.gavePristineNanoforgeToKanta = true` — `kpPristineForgeSel2` |
| `$player.gaveStandfastGuns` | CS / 4 | `raoAskDiktatMerc5faithGuns` (C) | `$player.gaveStandfastGuns++` — `standfastGiveOffering` |
| `$player.gotAndradaBook` | CS / 4 | `andradaOfficeServe2` (C) | `$player.gotAndradaBook = true` — `andradaOfficeServe2` |
| `$player.gotBookOfLuddFromCotton` | CS / 5 | `lppGileadShrineVisit3bookCheck1` (C) | `$player.gotBookOfLuddFromCotton = true` — `gaPZ_cottonLight` |
| `$player.gotCutOffByBaird` | CS / 2 | `gaKAReturnToAcademy10b2` (C) | `$player.gotCutOffByBaird = true` — `gaRequestMeeting4a` |
| `$player.gotFirstAbyssalLightInfodump` | CS / 3 | `abyssalLight_whatOpt` (C) | `$player.gotFirstAbyssalLightInfodump = true` — `abyssalLight_whatSel2` |
| `$player.gotFreeDrinkFromOwnColony` | CS / 2 | `BarOwnColFreeDrinkStart` (C) | `$player.gotFreeDrinkFromOwnColony = true` — `BarOwnColFreeDrink0` |
| `$player.gotHegemonyHoloExperience` | S / 1 | `defaultDaudHolo2` (S) | `$player.gotHegemonyHoloExperience = true` — `defaultDaudHolo2` |
| `$player.gotSedgeFaithSpeech` | CS / 8 | `lkeChalcedonSedgeOptMurder` (C) | `$player.gotSedgeFaithSpeech = true` — `lkeAskSedgeSmashA` |
| `$player.gotSword` | CS / 7 | `soeDuelPrepResponseBeginDuel` (C) | `$player.gotSword = true 0` — `soeDuelPrepSword` |
| `$player.gotTTweirdHullmodPitch` | CS / 4 | `TTmarketPostWeirdHullmods` (C) | `$player.gotTTweirdHullmodPitch = true` — `TTmarketPostWeirdHMbegin` |
| `$player.gotVirensContactFromDardan` | CS / 2 | `ImoinuKatoUmbraDardanOptB` (C) | `$player.gotVirensContactFromDardan = true` — `LKEmazAdminAskBornanewPL2` |
| `$player.gotWormholeCalibrationData` | CS / 4 | `limboWormholeCacheBeginAgain` (C) | `$player.gotWormholeCalibrationData = true` — `limboWormholeCacheBegin2` |
| `$player.gotYaribayCredentials` | COS / 11 | `LKEmazTestSetting1` (O) | `$player.gotYaribayCredentials = true 0` — `LKEmazTest1a` |
| `$player.hasGoodPLMembershipDeal` | CS / 9 | `gaFCarchonAskDemChubOptC` (C) | `$player.hasGoodPLMembershipDeal = true` — `gaFCArchonSearchHubOptionDEV2` |
| `$player.hasThreatDetectionSensorMods` | CS / 4 | `gsVambOptHubDescriptionA` (C) | `$player.hasThreatDetectionSensorMods = true` — `mk1_explore7` |
| `$player.hasTransportedPilgrims` | S / 2 | `LPTcheckCompletionBeholder` (S) | `$player.hasTransportedPilgrims++` — `LPTcheckCompletionBeholder` |
| `$player.hasTriTachDeal` | C / 7 | `ZGRacknowledgeTTDeal` (C) | Trace owning rule/command or generated interaction data |
| `$player.heardAboutSkinnyDiego` | S / 1 | `rsomAvoidCombat3` (S) | `$player.heardAboutSkinnyDiego = true` — `rsomAvoidCombat3` |
| `$player.heardCottonHearsSong` | CS / 2 | `LKEjethroAngelsTalkC` (C) | `$player.heardCottonHearsSong = true` — `LKEpatherFleetSPOpt2` |
| `$player.heardCottonsTellingOfProphecy` | CS / 2 | `LKEjethroAngelsTalkTellZigPoL3` (C) | `$player.heardCottonsTellingOfProphecy = true` — `gaPZ_cottonCont4` |
| `$player.heardElekWasAJerk` | CS / 2 | `gaDHOhook2aside` (C) | `$player.heardElekWasAJerk = true` — `gaIntro2returnSeb4` |
| `$player.heardRumorsAboutCruor` | S / 3 | `lppVolturnCurateSDstubborn` (S) | `$player.heardRumorsAboutCruor = true` — `lppVolturnCurateSDstubborn` |
| `$player.helpedUmbraARC` | CS / 9 | `sdtuUmbraAskAgentQM2` (C) | `$player.helpedUmbraARC = true` — `imoinuUmbraOfferHelpCredits` |
| `$player.inDebt` | C / 1 | `mercs_checkLeavingInDebt` (C) | Trace owning rule/command or generated interaction data |
| `$player.inspectedByLeagueEnforcer` | CS / 2 | `plEnforcerAgain` (C) | `$player.inspectedByLeagueEnforcer = true 60` — `plEnforcerComplySel2` |
| `$player.isLeagueMember` | CS / 63 | `BFFImmBornBuyerOptHubDno` (C) | `$player.isLeagueMember = true` — `gaFCArchonSearchHubOptionDEV2` |
| `$player.itemValueSoldToZGRJustNowMonster` | C / 4 | `ZGRtechTurnInResponseThreat` (C) | Trace owning rule/command or generated interaction data |
| `$player.itemValueSoldToZGRJustNowThreat` | C / 4 | `ZGRtechTurnInResponseThreat` (C) | Trace owning rule/command or generated interaction data |
| `$player.itemValueSoldToZGRMonster` | C / 2 | `ZGRmonsterTechAskMonsterDoOpt` (C) | Trace owning rule/command or generated interaction data |
| `$player.itemValueSoldToZGRThreat` | C / 2 | `ZGRthreatTechAskThreatDoOpt` (C) | Trace owning rule/command or generated interaction data |
| `$player.jangalaWentMaskOff` | CS / 2 | `salSRBWstartC` (C) | `$player.jangalaWentMaskOff = true` — `lppJangalaOutsideMaskOff2` |
| `$player.jangalaWentMaskOn` | CS / 2 | `salSRBWstartB` (C) | `$player.jangalaWentMaskOn = true` — `lppJangalaOutsideMaskOn3` |
| `$player.jaspisCottonBook` | CS / 6 | `lppEndingJaspisHub9` (C) | `$player.jaspisCottonBook = true` — `lppJaspisHammerFallBook2` |
| `$player.joinedJangalaProtest` | CS / 6 | `lkeChalcedonVIPreason1` (C) | `$player.joinedJangalaProtest = true` — `lppJangalaProtestJoinSupport` |
| `$player.kantaBluffs` | CS / 8 | `kdDevSetBluffedKanta1` (C) | `$player.kantaBluffs++` — `gaDAKantaSentMe` |
| `$player.kantaProtection` | CS / 12 | `relLevelPiratesKantasProtection` (C) | `$player.kantaProtection = true` — `kdDevToggleKantaProtectionOff` |
| `$player.killedSedge` | CS / 6 | `BFFIpsRuseMeanBoptCheck` (C) | `$player.killedSedge = true` — `lkeSedgeEndShoot2bar` |
| `$player.knowsAboutFenius` | CS / 2 | `LOCRPhubAskFenius` (C) | `$player.knowsAboutFenius = true` — `LOCRPwhoIsFenius` |
| `$player.knowsAboutMarlowe` | C / 1 | `LOCRPhubAskMarlowe` (C) | Trace owning rule/command or generated interaction data |
| `$player.knowsBornanewIsAntiRelic` | S / 1 | `BFINdontConsider` (S) | `$player.knowsBornanewIsAntiRelic = true` — `BFINdontConsider` |
| `$player.knowsCottonHearsSong` | CS / 2 | `LKEjethroAngelsTalkD` (C) | `$player.knowsCottonHearsSong = true` — `gaATGmeetCottonExpected` |
| `$player.knowsMacarioThinksAndradaBraindead` | C / 1 | `ImoinuKatoUmbraSayAndrada` (C) | Trace owning rule/command or generated interaction data |
| `$player.knowsPatherLeadOnPKButNoCommit` | CS / 2 | `pkRecoveredContSelPatherNoCommit` (C) | `$player.knowsPatherLeadOnPKButNoCommit = true` — `pkPatherRcoverMissionAbortSel` |
| `$player.knowsWhereCorTap` | S / 1 | `sal_hypershuntDescAddIntel` (S) | `$player.knowsWhereCorTap = true` — `sal_hypershuntDescAddIntel` |
| `$player.knowsZalHeldBack` | S / 1 | `gaATGMagecGateExplode` (S) | `$player.knowsZalHeldBack = true` — `gaATGMagecGateExplode` |
| `$player.leftCottonBookInGilead` | CS / 4 | `lppGileadShrineVisit3bookCheck1` (C) | `$player.leftCottonBookInGilead = true` — `lppGileadMissionEndDevF2` |
| `$player.leftLeagueWhenGoodDeal` | C / 1 | `plReynardHannanJoinSelNo2` (C) | Trace owning rule/command or generated interaction data |
| `$player.leftNerieneOnDanceFloor` | S / 1 | `soeDanceEscape1` (S) | `$player.leftNerieneOnDanceFloor = true` — `soeDanceEscape1` |
| `$player.liedToBornanewAboutGatesIntention` | S / 1 | `LKEjethroAngelsTalkTellGatesB-D` (S) | `$player.liedToBornanewAboutGatesIntention = true` — `LKEjethroAngelsTalkTellGatesB-D` |
| `$player.liedToSDpunEx` | CS / 2 | `sdPunExCommsSDcomLiarPoint` (C) | `$player.liedToSDpunEx = true` — `sdPunExCommsSDcomLiarPoint` |
| `$player.liedToStrandedPirates` | CS / 5 | `LOCRPtransportHubShuttlesLie` (C) | `$player.liedToStrandedPirates = true` — `LOCRPofferToPirateLie` |
| `$player.liedToZGRaboutLuddicFaith` | S / 1 | `ZGRackChurchOptAbsurdLie` (S) | `$player.liedToZGRaboutLuddicFaith = true` — `ZGRackChurchOptAbsurdLie` |
| `$player.locTag:no_topography_scans` | C / 2 | `cob_neutrinoBurstOpt` (C) | Trace owning rule/command or generated interaction data |
| `$player.locTag:temporary_location` | C / 1 | `pods_stabilizeTempLoc` (C) | Trace owning rule/command or generated interaction data |
| `$player.locTag:theme_core` | C / 1 | `gaIntro2surveyOpen` (C) | Trace owning rule/command or generated interaction data |
| `$player.locTag:theme_core_populated` | C / 2 | `gaATGgateJanusUseInhabited` (C) | Trace owning rule/command or generated interaction data |
| `$player.locTag:theme_hidden` | C / 1 | `gaIntro2surveyOpen` (C) | Trace owning rule/command or generated interaction data |
| `$player.locationId` | C / 4 | `gaFC_relayObjectInteraction` (C) | Trace owning rule/command or generated interaction data |
| `$player.locr_hereticsToPath` | S / 1 | `LOCRLoffloadToPath3` (S) | Trace owning rule/command or generated interaction data |
| `$player.locr_luddicsOnBoard` | CS / 4 | `LOCRLoffloadToChurchSecular` (C) | `$player.locr_luddicsOnBoard = true` — `LOCRLofferTransport` |
| `$player.locr_luddicsToChurch` | CS / 4 | `LOCRLoffloadToChurch` (C) | `$player.locr_luddicsToChurch = true` — `LOCRLofferTransportChurch` |
| `$player.locr_luddicsToKnights` | CS / 2 | `LOCRLoffloadToKnights` (C) | Trace owning rule/command or generated interaction data |
| `$player.locr_luddicsToPath` | CS / 4 | `LOCRLoffloadToPathMarket` (C) | `$player.locr_luddicsToPath = true` — `LOCRLofferTransportPath` |
| `$player.locr_minersOnBoard` | CS / 3 | `LOCRMoffloadMiners` (C) | `$player.locr_minersOnBoard = true` — `LOCRMofferTransport` |
| `$player.locr_minersToPlayerColony` | CS / 3 | `LOCRMoffloadMinersOwnColony` (C) | `$player.locr_minersToPlayerColony = true` — `LOCRMofferMyColony` |
| `$player.locr_piratesOnBoard` | CS / 6 | `LOCRPoffloadPirates` (C) | `$player.locr_piratesOnBoard = true` — `LOCRPtransportShuttle` |
| `$player.locr_piratesOnBoardForWork` | S / 1 | `LOCRPtransportShuttleWork` (S) | `$player.locr_piratesOnBoardForWork = true` — `LOCRPtransportShuttleWork` |
| `$player.locr_piratesOnBoardPrisoners` | CS / 7 | `LOCRPoffloadPiratesToPrisonA` (C) | `$player.locr_piratesOnBoardPrisoners = true` — `LOCRPtransportLie` |
| `$player.locrlf_gotBarBlurb` | CS / 3 | `locrlfOfferTextBarAgain` (C) | `$player.locrlf_gotBarBlurb = true` — `locrlfOfferTextBar` |
| `$player.locrlf_liedAboutFateOfHeretics` | CS / 4 | `locrlfReturnHubOptsA` (C) | `$player.locrlf_liedAboutFateOfHeretics = true` — `locrlfReturnStartLie` |
| `$player.locrlf_negotiatedHigherRate` | CS / 6 | `locrlfOfferTextBarAgainB` (C) | `$player.locrlf_negotiatedHigherRate = true` — `locrlfOfferBarCynicalRewardAsk` |
| `$player.locrp_toldNextPort` | CS / 2 | `LOCRPoffloadPiratesNext` (C) | `$player.locrp_toldNextPort = true` — `LOCRPoffloadPiratesNo` |
| `$player.lpt_gotPilgrimShrineTip` | CS / 2 | `LPTcheckCompletionContRichOneOff` (C) | `$player.lpt_gotPilgrimShrineTip = true` — `LPTcheckCompletionContRichOneOff` |
| `$player.luddChoseBecauseItWasGood` | S / 1 | `gaATGmeetCottonTeaDie` (S) | `$player.luddChoseBecauseItWasGood = true` — `gaATGmeetCottonTeaDie` |
| `$player.luddicAttitudeAgnostic` | CST / 14 | `lppGileadMissionEndDevB2` (T) | `$player.luddicAttitudeAgnostic++` — `lppHookAttitudeAdjustment3` |
| `$player.luddicAttitudeAtheistic` | CST / 13 | `lppGileadMissionEndDevC2` (T) | `$player.luddicAttitudeAtheistic++` — `lppHookAttitudeAdjustmentB3` |
| `$player.luddicAttitudeCynical` | CST / 24 | `lppGileadMissionEndDevD2` (T) | `$player.luddicAttitudeCynical++` — `lppHookAttitudeAdjustment2` |
| `$player.luddicAttitudeCynicalOrAtheistic` | CS / 7 | `LKEjethroAngelsTalkA2` (C) | `$player.luddicAttitudeCynicalOrAtheistic = true 0` — `ZGRacknowledgeContinueDEVe` |
| `$player.luddicAttitudeFaithful` | CST / 31 | `lppGileadMissionEndDevA2` (T) | `$player.luddicAttitudeFaithful++` — `lppHookAttitudeAdjustment1` |
| `$player.luddicAttitudeFaithfulOrPather` | CS / 28 | `LKEvirensStart0a` (C) | `$player.luddicAttitudeFaithfulOrPather  = true 0` — `ZGRacknowledgeContinueDEVb` |
| `$player.luddicAttitudeLPPHookBDone` | CS / 3 | `lppHookAttitudeAdjustmentB1` (C) | `$player.luddicAttitudeLPPHookBDone = true` — `lppHookAttitudeAdjustmentB1` |
| `$player.luddicAttitudeLPPHookDone` | CS / 3 | `lppHookAttitudeAdjustment1` (C) | `$player.luddicAttitudeLPPHookDone = true` — `lppHookAttitudeAdjustment1` |
| `$player.luddicAttitudeNone` | S / 1 | `LuddicEthosRefresh7` (S) | `$player.luddicAttitudeNone = true` — `LuddicEthosRefresh7` |
| `$player.luddicAttitudePather` | CST / 29 | `lppGileadMissionEndDevE2` (T) | `$player.luddicAttitudePather++` — `lppHookCurateCottonJoin` |
| `$player.luddicAttitudePilgrimFleet` | C / 2 | `shrineFleetConvAttitudeAdjustment1` (C) | Trace owning rule/command or generated interaction data |
| `$player.luddicAttitudeSecular` | S / 3 | `lppKillaOssuaryThousands2A` (S) | `$player.luddicAttitudeSecular++` — `lppKillaOssuaryThousands2A` |
| `$player.luddicFaithfulOrChurchCom` | CS / 18 | `ShroudedHullmodItemRCMantle5l` (C) | `$player.luddicFaithfulOrChurchCom = true 0` — `ZGRacknowledgeContinueDEVa` |
| `$player.macarioMentionedOrcusRao` | S / 4 | `sdtu_postCadenHydCadLieD` (S) | `$player.macarioMentionedOrcusRao = true` — `sdtu_postCadenHydCadLieD` |
| `$player.machinery` | CST / 2 | `gaOpCanAccept` (T) | Trace owning rule/command or generated interaction data |
| `$player.madeDealToFindFenius` | S / 2 | `LOCRPofferForFenius` (S) | `$player.madeDealToFindFenius = true` — `LOCRPofferForFenius` |
| `$player.madeImmigrationDealWithLuddicChurch` | C / 2 | `LCMakeDealOpts` (C) | Trace owning rule/command or generated interaction data |
| `$player.madeSpaceGardenShrine` | S / 1 | `salOHOGoutLuddic` (S) | `$player.madeSpaceGardenShrine = true` — `salOHOGoutLuddic` |
| `$player.makeDiktatDeal` | C / 3 | `SDMakeDealOpt` (C) | Trace owning rule/command or generated interaction data |
| `$player.makingLostPiratesActLuddic` | CS / 4 | `LOCRPoffloadPiratesLuddic` (C) | `$player.makingLostPiratesActLuddic = true` — `LOCRPofferWorkLuddicPrayA` |
| `$player.marines` | CS / 10 | `lppJangalaProtestRespMarinesHelp` (C) | Trace owning rule/command or generated interaction data |
| `$player.maxCombatHullSize` | C / 10 | `lppHesperusExcubitorThreatCheck` (C) | Trace owning rule/command or generated interaction data |
| `$player.met` | S / 1 | `BFFItalkToEngineer4` (S) | Trace owning rule/command or generated interaction data |
| `$player.metANHRobedMan` | S / 2 | `anhCantinaAskStartCap` (S) | `$player.metANHRobedMan = true` — `anhCantinaAskStartCap` |
| `$player.metBaird` | CS / 12 | `lppGileadMissionEndDevG` (C) | `$player.metBaird = true` — `gaRequestMeeting` |
| `$player.metBornanew` | S / 1 | `LKEbornanewShuttle5` (S) | `$player.metBornanew = true` — `LKEbornanewShuttle5` |
| `$player.metBrotherCotton` | CS / 22 | `relLevelPatherPK` (C) | `$player.metBrotherCotton = true` — `lppGileadMissionEndDevF2` |
| `$player.metCaden` | S / 2 | `sdDevOptionHubOptionB` (S) | `$player.metCaden = true` — `sdDevOptionHubOptionB` |
| `$player.metCardona` | S / 1 | `soeDanceRound6` (S) | `$player.metCardona = true` — `soeDanceRound6` |
| `$player.metCaspianSang` | S / 1 | `soeMeetCaspianStart1` (S) | `$player.metCaspianSang = true` — `soeMeetCaspianStart1` |
| `$player.metCavin` | CS / 3 | `gaKACoureuseCavinOpt` (C) | `$player.metCavin = true` — `gaFCFikenhildCavinKnock` |
| `$player.metCloneLoke` | S / 1 | `gaKANewMaxiosVisit1` (S) | `$player.metCloneLoke = true` — `gaKANewMaxiosVisit1` |
| `$player.metCydonia` | S / 1 | `kdKantaFirstIntro4` (S) | `$player.metCydonia = true` — `kdKantaFirstIntro4` |
| `$player.metDardanKato` | CS / 6 | `LKEmazalotAskPortmasterDardan` (C) | `$player.metDardanKato = true` — `LKEmazalotAskAdministrator0` |
| `$player.metDaud` | CS / 11 | `relLevelLeagueYaribay` (C) | `$player.metDaud = true` — `gaATGdaudMeeting6` |
| `$player.metElek` | CS / 10 | `gaDHOhook0met` (C) | `$player.metElek = true` — `gaIntro2returnElek1` |
| `$player.metGargoyle` | S / 1 | `gaKAGargoyleGreeting` (S) | `$player.metGargoyle = true` — `gaKAGargoyleGreeting` |
| `$player.metGideonOak` | CS / 6 | `BFFIarrestOutroAskPenanceB` (C) | `$player.metGideonOak = true` — `lppHesperusShrineFirstTime3` |
| `$player.metHorusYaribay` | CS / 6 | `BFFIonBoardYarbRespA` (C) | `$player.metHorusYaribay = true` — `BFINdevForceKnowHorus` |
| `$player.metHyder` | S / 2 | `sdDevOptionHubOptionB` (S) | `$player.metHyder = true` — `sdDevOptionHubOptionB` |
| `$player.metImoinuKato` | CS / 3 | `DardanKatoAskImoinu` (C) | `$player.metImoinuKato = true` — `ImoinuUmbraGreetingSDcom` |
| `$player.metJaspis` | S / 1 | `lppEnding4` (S) | `$player.metJaspis = true` — `lppEnding4` |
| `$player.metKDSK` | CS / 5 | `kdKantaGreetingRouteFirstTime` (C) | `$player.metKDSK = true` — `kdKantaGreetingRouteFirstTime` |
| `$player.metKanta` | CS / 6 | `kantasDenVisitOptionGAATG` (C) | `$player.metKanta = true` — `kdKantaFirstIntro3` |
| `$player.metMacario` | CS / 5 | `RaoPitchDiktatCom2b` (C) | `$player.metMacario = true` — `sdDevOptionHubOptionB` |
| `$player.metMenesYaribay` | S / 1 | `BFFImeetMenesStart` (S) | `$player.metMenesYaribay = true` — `BFFImeetMenesStart` |
| `$player.metNeriene` | CS / 3 | `raoMetNeriene` (C) | `$player.metNeriene = true` — `soeIntroNeriene2` |
| `$player.metRam` | S / 2 | `sdBarRaid3` (S) | `$player.metRam = true` — `sdBarRaid3` |
| `$player.metReynardHannan` | CS / 10 | `gaFCarchonAskDemarchonOptsC` (C) | `$player.metReynardHannan = true` — `gaFCArchonSearchHubOptionDEV2` |
| `$player.metSebestyen` | CS / 5 | `gaIntro2sebCheck` (C) | `$player.metSebestyen = true` — `asebFirstTimeGreeting` |
| `$player.metSiyavong` | COS / 7 | `LKEmazTestSetting2` (O) | `$player.metSiyavong = true 0` — `LKEmazTest2a` |
| `$player.metStandfast` | CS / 7 | `lppVolturnShrineRazedTea` (C) | `$player.metStandfast = true` — `lppVolturnShrineFirstVisit3a` |
| `$player.metTanaica` | CS / 2 | `gsVambOptHubC` (C) | `$player.metTanaica = true` — `BFFItalkToEngineer1` |
| `$player.metTseenKe` | CS / 6 | `gaMeetHegLieutenantOnCoatl` (C) | `$player.metTseenKe = true` — `gaMeetHegLieutenantOnCoatlSel` |
| `$player.metZGR` | CS / 9 | `GS_ZGR_MERC_cont3SelB` (C) | `$player.metZGR = true` — `ZGRfirstVisitIntro6` |
| `$player.metZal` | CS / 3 | `gaKACoureuseZalOpt` (C) | `$player.metZal = true` — `gaFCZalFirstGreeting_INVESTIGATE_FIKENHILD2` |
| `$player.metals` | C / 1 | `cTapCheckCanAfford` (C) | Trace owning rule/command or generated interaction data |
| `$player.mostLuddicEthosAgnostic` | S / 6 | `LuddicEthosRefresh` (S) | `$player.mostLuddicEthosAgnostic = false` — `LuddicEthosRefresh` |
| `$player.mostLuddicEthosAtheistic` | CS / 8 | `LKEvirensEndingOptC` (C) | `$player.mostLuddicEthosAtheistic = false` — `LuddicEthosRefresh` |
| `$player.mostLuddicEthosCynical` | CS / 7 | `LOCRLjudgeOptionC` (C) | `$player.mostLuddicEthosCynical = false` — `LuddicEthosRefresh` |
| `$player.mostLuddicEthosFaithful` | CS / 28 | `CGRmarketWeirdOutroCom` (C) | `$player.mostLuddicEthosFaithful = true 0` — `raoDevOptionHubA5` |
| `$player.mostLuddicEthosPather` | CS / 32 | `lkeChalcedonVIPstartPather` (C) | `$player.mostLuddicEthosPather = true 0` — `lkeChalcedonVIPoptHostileSP3` |
| `$player.name` | T / 5 | `gaOpPlanetRogueAIConfront1` (T) | Trace owning rule/command or generated interaction data |
| `$player.nerieneStartedDance` | CS / 3 | `soeDanceRound1a` (C) | `$player.nerieneStartedDance = true 0` — `soeDanceStarting2b` |
| `$player.numColonies` | C / 23 | `relLevelNeutralLeague` (C) | Trace owning rule/command or generated interaction data |
| `$player.numShips` | CT / 5 | `gaOpPlanetRogueAINegotiate1` (T) | Trace owning rule/command or generated interaction data |
| `$player.numTimesLeftLeague` | C / 6 | `plPunExComms2` (C) | Trace owning rule/command or generated interaction data |
| `$player.offeredHelpUmbraARC` | CS / 2 | `imoinuUmbraGoBackB` (C) | `$player.offeredHelpUmbraARC = true` — `imoinuUmbraOfferHelp` |
| `$player.offeredImoinuCavinConnection` | CS / 2 | `ImoinuUmbraGoOptA` (C) | `$player.offeredImoinuCavinConnection = true` — `imoinuUmbraGoCavin` |
| `$player.offeredImoinuGargoyleConnection` | S / 1 | `imoinuUmbraGoGargoyle` (S) | `$player.offeredImoinuGargoyleConnection = true` — `imoinuUmbraGoGargoyle` |
| `$player.offeredImoinuPassage` | CS / 2 | `ImoinuUmbraGoOptC` (C) | `$player.offeredImoinuPassage = true` — `imoinuUmbraGoOfferPassage` |
| `$player.offeredLostPiratesWork` | CS / 4 | `LOCRPofferWorkSpace` (C) | `$player.offeredLostPiratesWork = true` — `LOCRPofferWork` |
| `$player.openToTurningOnAndrada` | S / 2 | `sdPunExCommsSDcomLiarPoint` (S) | `$player.openToTurningOnAndrada++` — `sdPunExCommsSDcomLiarPoint` |
| `$player.patherAgreement` | CS / 2 | `LPTitheHasColonies` (C) | `$player.patherAgreement = true` — `pk_givePKPatherSel` |
| `$player.patherAgreementPermanent` | CS / 5 | `relLevelPatherPK` (C) | `$player.patherAgreementPermanent = true` — `pk_givePKPatherSel` |
| `$player.payingHouseHannan` | CS / 15 | `gaFCarchonAskDemarchonOptsA` (C) | `$player.payingHouseHannan = true` — `gaFCArchonSearchHubOptionDEV2` |
| `$player.pkRecoveryLiedToPather` | CS / 2 | `pkRecoveredContSelPatherLie` (C) | `$player.pkRecoveryLiedToPather = true` — `PKPatherSayYesLie` |
| `$player.playerStartedDance` | CS / 2 | `soeDanceRound1b` (C) | `$player.playerStartedDance = true 0` — `soeDanceStarting2a` |
| `$player.quotedLuddAtBairdEnd` | S / 1 | `gaATGbairdEndingReap` (S) | `$player.quotedLuddAtBairdEnd = true` — `gaATGbairdEndingReap` |
| `$player.rare_metals` | C / 1 | `cTapCheckCanAfford` (C) | Trace owning rule/command or generated interaction data |
| `$player.rdsm_askedEverything` | S / 1 | `rdsmOfferAsk4` (S) | `$player.rdsm_askedEverything = true` — `rdsmOfferAsk4` |
| `$player.receivedGlamorRotanevInvite` | CS / 10 | `TTmarketPostWeirdHullmods` (C) | `$player.receivedGlamorRotanevInvite = true` — `TTmarketWeirdModsOutro` |
| `$player.returnedToZGRpostWeird` | CS / 4 | `ZGRgreetingPostWeird` (C) | `$player.returnedToZGRpostWeird = true` — `ZGRgreetingPostWeird` |
| `$player.saidSkinnyDiegoBadAndFancy` | S / 1 | `rsomAvoidCombat5a` (S) | `$player.saidSkinnyDiegoBadAndFancy = true` — `rsomAvoidCombat5a` |
| `$player.saidSkinnyDiegoBrotherFallout` | S / 1 | `rsomAvoidCombat4c` (S) | `$player.saidSkinnyDiegoBrotherFallout = true` — `rsomAvoidCombat4c` |
| `$player.saidSkinnyDiegoUnlucky` | S / 1 | `rsomAvoidCombat4b` (S) | `$player.saidSkinnyDiegoUnlucky = true` — `rsomAvoidCombat4b` |
| `$player.satWithCottonCount` | CS / 7 | `lkeChalcedonVIPreason3` (C) | `$player.satWithCottonCount++` — `gaPZ_cottonSit` |
| `$player.sawAbyssalLight` | CS / 2 | `abyssalLightBeginAgain` (C) | `$player.sawAbyssalLight = true` — `abyssalLightBegin` |
| `$player.sawBornanewGoBeastMode` | S / 1 | `BFFIonBoardAskRelic4` (S) | `$player.sawBornanewGoBeastMode = true` — `BFFIonBoardAskRelic4` |
| `$player.sawCaspian` | CS / 5 | `soeSangAppearsDrink` (C) | `$player.sawCaspian = true 0` — `soeHouseRaoRevenge1` |
| `$player.scholarshipThemeExplore` | S / 1 | `asebScholarshipCauseBsel` (S) | `$player.scholarshipThemeExplore = true` — `asebScholarshipCauseBsel` |
| `$player.scholarshipThemeGates` | S / 1 | `asebScholarshipCauseFsel` (S) | `$player.scholarshipThemeGates = true` — `asebScholarshipCauseFsel` |
| `$player.scholarshipThemeHelp` | S / 1 | `asebScholarshipCauseAsel` (S) | `$player.scholarshipThemeHelp = true` — `asebScholarshipCauseAsel` |
| `$player.scholarshipThemeLuddic` | S / 1 | `asebScholarshipCauseEsel` (S) | `$player.scholarshipThemeLuddic = true` — `asebScholarshipCauseEsel` |
| `$player.scholarshipThemeProAI` | S / 1 | `asebScholarshipCauseDsel` (S) | `$player.scholarshipThemeProAI = true` — `asebScholarshipCauseDsel` |
| `$player.scholarshipThemeUseAI` | S / 1 | `asebScholarshipCauseCsel` (S) | `$player.scholarshipThemeUseAI = true` — `asebScholarshipCauseCsel` |
| `$player.scholarshipThemeWeapons` | S / 1 | `asebScholarshipCauseGsel` (S) | `$player.scholarshipThemeWeapons = true` — `asebScholarshipCauseGsel` |
| `$player.sdtu_gotExecutorFromMacario` | CS / 2 | `ZGRacknowledgeDiktatB` (C) | `$player.sdtu_gotExecutorFromMacario = true` — `sdtuNewsEndHull` |
| `$player.sdtu_gotMarines` | CS / 5 | `sdtuRamPrepMarines` (C) | `$player.sdtu_gotMarines = true 0` — `sdtuRamPrepMarinesSel` |
| `$player.sdtu_gotWeapon` | CS / 6 | `sdtuRamPrepWeapon` (C) | `$player.sdtu_gotWeapon = true` — `sdtuRamPrepWeaponSel` |
| `$player.sdtu_gotWire` | CS / 5 | `sdtuRamPrepWire` (C) | `$player.sdtu_gotWire = true` — `sdtuRamPrepWireSel` |
| `$player.sdtu_liedToImoinuAboutAgent` | CS / 7 | `ImoinuUmbraGreetingsLiar` (C) | `$player.sdtu_liedToImoinuAboutAgent = true` — `sdtuUmbraTrickVIPimoinu` |
| `$player.sdtu_weaponGrenade` | CS / 5 | `BFFIulmusPrestartLoadout2b` (C) | `$player.sdtu_weaponGrenade = true` — `sdtuRamPrepWeaponGren2` |
| `$player.sdtu_weaponKnife` | CS / 5 | `BFFIulmusPrestartLoadout2c` (C) | `$player.sdtu_weaponKnife = true` — `sdtuRamPrepWeaponKnife2` |
| `$player.sdtu_weaponNone` | CS / 7 | `BFFIulmusPrestartLoadout2f` (C) | `$player.sdtu_weaponNone = true` — `sdtuRamPrepWeaponNone2` |
| `$player.sdtu_weaponPistol` | CS / 4 | `BFFIulmusPrestartLoadout2a` (C) | `$player.sdtu_weaponPistol = true` — `sdtuRamPrepWeaponPistol2` |
| `$player.sdtu_weaponSpray` | CS / 5 | `BFFIulmusPrestartLoadout2d` (C) | `$player.sdtu_weaponSpray = true` — `sdtuRamPrepWeaponSpray2` |
| `$player.sdtu_weaponSword` | CS / 6 | `BFFIulmusPrestartLoadout2g` (C) | `$player.sdtu_weaponSword = true` — `sdtuRamPrepWeaponSword2` |
| `$player.sdtu_weaponWire` | CS / 5 | `BFFIulmusPrestartLoadout2e` (C) | `$player.sdtu_weaponWire = true` — `sdtuRamPrepWeaponWire2` |
| `$player.setUpGAscholarship` | CS / 6 | `asebScholarshipHeardAbout` (C) | `$player.setUpGAscholarship = true` — `asebScholarship100k` |
| `$player.shotSedge` | CS / 21 | `lkeVirensCommsSedgeAside` (C) | `$player.shotSedge = true` — `lkeSedgeEndShoot2bar` |
| `$player.shotSedgeOutside` | CS / 2 | `LKEvirensRaidJethro2c` (C) | `$player.shotSedgeOutside = true` — `lkeSedgeEndShoot2outside` |
| `$player.shroudedHullmodId` | C / 3 | `ShroudedHullmodItemRCMantle` (C) | Trace owning rule/command or generated interaction data |
| `$player.shroudedSubstrateAvailable` | CST / 4 | `shroudedSubstrateOptSelF` (T) | Trace owning rule/command or generated interaction data |
| `$player.soe_goalConnections` | CS / 3 | `soeHouseRaoGoalSchmooze` (C) | `$player.soe_goalConnections = true 0` — `soeToBallConnections` |
| `$player.soe_goalDisgraceSang` | CS / 4 | `soeHouseRaoGoalRevenge` (C) | `$player.soe_goalDisgraceSang = true 0` — `soeToBallDisgraceSang` |
| `$player.soe_goalGoodTime` | CS / 3 | `soeHouseRaoGoalParty` (C) | `$player.soe_goalGoodTime = true 0` — `soeToBallGoodTime` |
| `$player.soe_goalOpenMind` | CS / 3 | `soeHouseRaoGoalUnsure` (C) | `$player.soe_goalOpenMind = true 0` — `soeToBallOpenMind` |
| `$player.soe_lostDuel` | CS / 19 | `BFFImmBornOptRaid4b` (C) | `$player.soe_lostDuel = true` — `soeFightLose` |
| `$player.soe_wasNiceToNeriene` | CS / 10 | `raoNerieneSelLeftA2a` (C) | `$player.soe_wasNiceToNeriene = true` — `soeDuelPrepLeaveDatingSim` |
| `$player.soe_wasRudeToNeriene` | CS / 9 | `raoNerieneSelLeftA2b` (C) | `$player.soe_wasRudeToNeriene = true` — `soeDuelPrepLeaveRude` |
| `$player.soe_wonDuel` | CS / 18 | `BFFImmBornOptRaid4a` (C) | `$player.soe_wonDuel = true` — `soeFightWin` |
| `$player.supplies` | CST / 23 | `gaProbeOfferAidCanDoIt` (T) | Trace owning rule/command or generated interaction data |
| `$player.sworeRevengeOnRam` | CS / 7 | `sdtuStartInfo2a` (C) | `$player.sworeRevengeOnRam = true` — `sdBarRaidBeatingRevenge` |
| `$player.talkedAboutNarcingOnHorusYaribay` | CS / 2 | `RHannanMsgFollowupHorusYaribay` (C) | `$player.talkedAboutNarcingOnHorusYaribay = true` — `RHannanMsgFollowHorusYaribay` |
| `$player.talkedSwordsWithOrcus` | CS / 3 | `RaoPitchTestSwords` (C) | `$player.talkedSwordsWithOrcus = true` — `raoNerieneSelWonD2` |
| `$player.talkedToHorusYaribayAboutReynard` | CS / 2 | `HYaribayAskReynardHubAgain` (C) | `$player.talkedToHorusYaribayAboutReynard = true` — `HYaribayAskReynardHub` |
| `$player.talkedToOrcusAboutNeriene` | CS / 4 | `raoMetNeriene` (C) | `$player.talkedToOrcusAboutNeriene = true` — `raoNerieneSelWonC3c` |
| `$player.talkedToThreat` | CS / 1 | `threatCommLinkFirst` (C) | `$player.talkedToThreat = true` — `threatCommLinkFirst` |
| `$player.theFactionName` | T / 2 | `GAFCSiyavongStonewall1mem` (T) | Trace owning rule/command or generated interaction data |
| `$player.threwDrink` | CS / 3 | `soeIntroNeriene1drink` (C) | `$player.threwDrink = true 0` — `soeSangRound2throwDrink` |
| `$player.threwPunch` | CS / 5 | `soeIntroNeriene1punch` (C) | `$player.threwPunch = true 0` — `soeSangRound2punchFace` |
| `$player.toldBairdEndBelieveInProject` | S / 1 | `gaATGbairdEndingBelieve` (S) | `$player.toldBairdEndBelieveInProject = true` — `gaATGbairdEndingBelieve` |
| `$player.toldBornanewGatesMusic` | CS / 2 | `LKEjethroAngelsTalkB2b` (C) | `$player.toldBornanewGatesMusic = true` — `LKEjethroAngelsTalkTellGates` |
| `$player.toldBornanewHeIsBadGuy` | CS / 7 | `BFFIulmusStart2a` (C) | `$player.toldBornanewHeIsBadGuy = true` — `BFFIulmusPrestartBornBad` |
| `$player.toldBornanewHeIsGoodGuy` | CS / 4 | `BFFIulmusStart2b` (C) | `$player.toldBornanewHeIsGoodGuy = true` — `BFFIulmusPrestartBornGood` |
| `$player.toldBornanewHeardMusic` | S / 1 | `LKEjethroAngelsTalkB1` (S) | `$player.toldBornanewHeardMusic = true` — `LKEjethroAngelsTalkB1` |
| `$player.toldBornanewMusicAndBackedOff` | S / 1 | `LKEjethroAngelsTalkOut` (S) | `$player.toldBornanewMusicAndBackedOff = true` — `LKEjethroAngelsTalkOut` |
| `$player.toldBornanewShouldStudyGates` | S / 1 | `LKEjethroAngelsTalkTellGatesB-B` (S) | `$player.toldBornanewShouldStudyGates = true` — `LKEjethroAngelsTalkTellGatesB-B` |
| `$player.toldBornanewShouldUseGates` | S / 1 | `LKEjethroAngelsTalkTellGatesB-A` (S) | `$player.toldBornanewShouldUseGates = true` — `LKEjethroAngelsTalkTellGatesB-A` |
| `$player.toldBornanewWillAvoidGates` | S / 1 | `LKEjethroAngelsTalkTellGatesB-C` (S) | `$player.toldBornanewWillAvoidGates = true` — `LKEjethroAngelsTalkTellGatesB-C` |
| `$player.toldBornanewZigMusic` | CS / 4 | `LKEjethroAngelsTalkB2a` (C) | `$player.toldBornanewZigMusic = true` — `LKEjethroAngelsTalkTellZig` |
| `$player.toldMenesYaribayAboutHorusCredentials` | S / 1 | `BFFImpBuyerHub3OptHorus` (S) | `$player.toldMenesYaribayAboutHorusCredentials = true` — `BFFImpBuyerHub3OptHorus` |
| `$player.toldMenesYaribayArroyoBacksGlovePurchase` | S / 1 | `BFFImpBuyerHub3OptArroyo` (S) | `$player.toldMenesYaribayArroyoBacksGlovePurchase = true` — `BFFImpBuyerHub3OptArroyo` |
| `$player.toldPLAblockadeToStickIt` | CS / 2 | `PLArmadaHubOptionC` (C) | `$player.toldPLAblockadeToStickIt = true` — `PLArmadaStickIt` |
| `$player.toldPilgrimFleetAttitude` | S / 2 | `shrineFleetConvAttitudeAdjustment1` (S) | `$player.toldPilgrimFleetAttitude = true` — `shrineFleetConvAttitudeAdjustment1` |
| `$player.toldRaoTalkToHorusBeforeWork` | S / 1 | `RaoPitchLeagueYaribayMum2b` (S) | `$player.toldRaoTalkToHorusBeforeWork = true` — `RaoPitchLeagueYaribayMum2b` |
| `$player.toldReynardHeWastesHumanLife` | CS / 4 | `gaFCarchonAskDemChubOptB` (C) | `$player.toldReynardHeWastesHumanLife = true` — `gaFCArchonSearchHubOptionDEV2` |
| `$player.toldReynardVoteHimOut` | CS / 5 | `gaFCarchonAskDemChubOptA` (C) | `$player.toldReynardVoteHimOut = true` — `gaFCArchonSearchHubOptionDEV2` |
| `$player.tookDrink` | CS / 4 | `soeSangRound1reply6` (C) | `$player.tookDrink = true 0` — `soeTakeDrink` |
| `$player.tookVambraceSamples` | CS / 4 | `gsVambOptHubC` (C) | `$player.tookVambraceSamples = true` — `gsVambraceSample` |
| `$player.transponderOn` | C / 44 | `marketPostOpenDefault` (C) | Trace owning rule/command or generated interaction data |
| `$player.turnedInPlanetkiller` | CS / 23 | `PKGiveToPatherOption` (C) | `$player.turnedInPlanetkiller = true` — `pk_givePKPatherSel` |
| `$player.turnedInPlanetkillerToKnights` | S / 1 | `pkHandInHesperus6` (S) | `$player.turnedInPlanetkillerToKnights = true` — `pkHandInHesperus6` |
| `$player.untrustworthy` | C / 10 | `pirateSKGreetingUntrusted` (C) | Trace owning rule/command or generated interaction data |
| `$player.waitedOutExcubitor` | CS / 2 | `lkeMazBarFineEyeContactEO1` (C) | `$player.waitedOutExcubitor = true` — `lppHesperusExcubitorWait3` |
| `$player.waitedOutGargoyle` | CS / 2 | `lppHesperusExcubitorWaitAside` (C) | `$player.waitedOutGargoyle = true` — `paPZ_gargZigWait2Sel` |
| `$player.wasBeatenBySD` | CS / 4 | `sdtuStartInfo1b` (C) | `$player.wasBeatenBySD = true` — `sdBarRaidBeating` |
| `$player.wasJerkToMerc` | CS / 4 | `ome_greetingMercPlayerWasJerk` (C) | `$player.wasJerkToMerc = true 1000` — `mercs_letGoOptJerk` |
| `$player.wasOfferedChurchImmigrationDeal` | CS / 2 | `lcMakeDealSelAgain` (C) | `$player.wasOfferedChurchImmigrationDeal = true` — `lcMakeDealProposal2` |
| `$playerAttemptedCargoPodScam` | S / 1 | `bqfsAskedForSuppliesScamOut` (S) | `$playerAttemptedCargoPodScam = true` — `bqfsAskedForSuppliesScamOut` |
| `$playerBrotherOrSister` | OT / 32 | `lppHookWalkAlready` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$playerContactForTestMission` | C / 2 | `convTMStart` (C) | Trace owning rule/command or generated interaction data |
| `$playerEndedDealFaceToFace` | CS / 2 | `pirateStationKingBusinessSelEndedFaceToFace` (C) | `$playerEndedDealFaceToFace = true` — `psk_cancelDealConfirm` |
| `$playerFirstName` | T / 15 | `lkeChalcedonAskBarRespA` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$playerHasDealWithPirateBaseCommander` | CS / 5 | `pirateStationKingGreeting` (C) | `$playerHasDealWithPirateBaseCommander = true` — `pirateStationKingAcceptSel` |
| `$playerHasOneslaught` | CS / 3 | `gsVambraceExplore1b` (C) | `$playerHasOneslaught = true` — `gsVambOptHubDescriptionC` |
| `$playerHeOrShe` | T / 1 | `BFFIulmusTalkPcBad2b` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/events/BaseEventPlugin.java` |
| `$playerHimOrHer` | T / 1 | `BFFIattendPartyHub1Out1` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/events/BaseEventPlugin.java` |
| `$playerHisOrHer` | T / 3 | `BFFIattendPartyHub1Out1` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/events/BaseEventPlugin.java` |
| `$playerHostileTimeoutStr` | T / 2 | `marketPostOpenNoTradeHostile` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$playerLastName` | T / 15 | `TTmarketPostWeirdHMbegin` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$playerName` | OT / 233 | `greetingDefaultFriendly` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/econ/BaseMarketConditionPlugin.java` (more mentions) |
| `$playerSirOrMadam` | T / 101 | `abyssalLight_whatSel` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$playerWantedWater` | CS / 4 | `lppEnding5waterCheck1a` (C) | `$playerWantedWater = true 0` — `lppEnding4water` |
| `$playerWasAdink` | S / 1 | `gaDHOvisitElekAltIntroPayment2a` (S) | `$playerWasAdink = true 0` — `gaDHOvisitElekAltIntroPayment2a` |
| `$playerWasJerk` | S / 1 | `lppEnding4c` (S) | `$playerWasJerk = true 0` — `lppEnding4c` |
| `$playername` | T / 14 | `lkePatherFleetOpenComm` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$post` | OST / 136 | `TTmarketPostWeirdHMbegin` (T) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$postId` | C / 66 | `ome_askSkillsSelNoSkillAdmin` (C) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$preferSpace` | CS / 2 | `soeDuelResponseSpace` (C) | `$preferSpace = true 0` — `soeDuelSpace` |
| `$prepSaidCoward` | CS / 3 | `soeDuelPrepResponseCoward` (C) | `$prepSaidCoward = true 0` — `soeDuelPrepCoward` |
| `$prepSaidCoward2` | CS / 2 | `soeDuelPrepResponseCoward2` (C) | `$prepSaidCoward2 = true 0` — `soeDuelPrepCoward2` |
| `$prepSaidQuaint` | CS / 2 | `soeDuelPrepResponseQuaint` (C) | `$prepSaidQuaint = true 0` — `soeDuelPrepQuaint` |
| `$pretendingNotPirates` | S / 1 | `LOCRPareYouPiratesNot` (S) | `$pretendingNotPirates = true` — `LOCRPareYouPiratesNot` |
| `$printedDesc` | CS / 4 | `remnantStationFleetOpenDefault` (C) | `$printedDesc = true 0` — `remnantStationFleetOpenDefault` |
| `$privacyField` | CS / 3 | `sdtuHyderTalk3WhySmugglePrivacy` (C) | `$privacyField = true 0` — `sdtuHyderTalk3WhySmugglePrivacy` |
| `$proCom_PersonPost` | T / 1 | `proComTextRemote` (T) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_barEvent` | C / 8 | `proComTextLocal` (C) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_commodityId` | S / 1 | `proComDeliverOptionSel` (S) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_commodityName` | OT / 17 | `proComBlurb` (T) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_completed` | S / 1 | `proComDeliverOptionSel` (S) | `$proCom_completed = true` — `proComDeliverOptionSel` |
| `$proCom_dist` | ST / 4 | `proComTextRemote` (T) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_manOrWoman` | OT / 4 | `proComBlurbBar` (T) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_marketName` | ST / 4 | `proComTextRemote` (T) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_marketOnOrAt` | T / 1 | `proComTextRemote` (T) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_needsCommodity` | C / 4 | `proComDeliverGreetingNotEnough` (C) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_personName` | T / 4 | `proComTextRemote` (T) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_personPost` | T / 2 | `proComTextRemoteUnderworld` (T) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_playerHasEnough` | C / 4 | `proComDeliverGreetingNotEnough` (C) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_pricePerUnit` | ST / 10 | `proComTextLocal` (T) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_quantity` | ST / 13 | `proComTextLocal` (T) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_ref` | CS / 9 | `proComDeliverGreetingNotEnough` (C) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_totalPrice` | ST / 5 | `proComTextRemote` (T) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_underworld` | C / 9 | `proComBlurbBarUnderworld` (C) | `impl/campaign/missions/ProcurementMission.java` |
| `$proCom_variation` | C / 8 | `proComTextLocal` (C) | `impl/campaign/missions/ProcurementMission.java` |
| `$promisedHonesty` | CS / 5 | `PKHackStoryOptB` (C) | `$promisedHonesty = true 0` — `PKHackStorySorryLied` |
| `$promisedWillReturn` | CS / 2 | `LOCRMcontact1againPromise` (C) | `$promisedWillReturn = true` — `LOCRMleavePromise` |
| `$proposedBuyerOption` | CS / 6 | `BFFImmBornBuyerOpt` (C) | `$proposedBuyerOption = true 0` — `BFFImmBornBuyerOptSel` |
| `$proposedHorusOption` | CS / 4 | `BFFImmBornInviteHorusOpt` (C) | `$proposedHorusOption = true 0` — `BFFImeetMenesBornOptInviteHsel` |
| `$proposedRaidOption` | CS / 3 | `BFFImmBornRaidOpt` (C) | `$proposedRaidOption = true 0` — `BFFImmBornOptRaid4d` |
| `$proposedStealOption` | CS / 5 | `BFFImmBornStealOpt` (C) | `$proposedStealOption = true 0` — `BFFImmBornOptStealFirst` |
| `$protectorScanConv` | S / 1 | `lcSacredProtectors` (S) | `$protectorScanConv = true 0` — `lcSacredProtectors` |
| `$protestHelpRejected` | CS / 2 | `lppJangalaProtestRespMarinesHelp` (C) | `$protestHelpRejected = true 0` — `lppJangalaProtestMarinesHelp` |
| `$psb_baseBounty` | ST / 2 | `psbOfferTextContact` (T) | `impl/campaign/missions/PirateSystemBounty.java` |
| `$psb_days` | ST / 2 | `psbOfferTextContact` (T) | `impl/campaign/missions/PirateSystemBounty.java` |
| `$psb_dist` | ST / 2 | `psbOfferTextContact` (T) | `impl/campaign/missions/PirateSystemBounty.java` |
| `$psb_manOrWoman` | OT / 3 | `psbBlurbBar` (T) | `impl/campaign/missions/PirateSystemBounty.java` |
| `$psb_ref` | S / 2 | `psbOfferTextContact` (S) | `impl/campaign/missions/PirateSystemBounty.java` |
| `$psb_systemName` | T / 2 | `psbOfferTextContact` (T) | `impl/campaign/missions/PirateSystemBounty.java` |
| `$psb_systemNameShort` | S / 2 | `psbOfferTextContact` (S) | `impl/campaign/missions/PirateSystemBounty.java` |
| `$psi_credits` | OST / 2 | `psi_pilotBarter` (T) | `impl/campaign/intel/bar/events/PlanetaryShieldIntel.java` |
| `$psi_eventRef` | CS / 4 | `psi_disableIfCanNotPay` (C) | `impl/campaign/intel/bar/events/PlanetaryShieldIntel.java` |
| `$psi_isPilot` | C / 2 | `psi_pilotGreeting` (C) | `impl/campaign/intel/bar/events/PlanetaryShieldIntel.java` |
| `$psi_planet` | C / 4 | `psi_redPlanetOpenDialog` (C) | `impl/campaign/procgen/themes/MiscellaneousThemeGenerator.java` |
| `$psi_playerCredits` | S / 1 | `psi_disableIfCanNotPay` (S) | `impl/campaign/intel/bar/events/PlanetaryShieldIntel.java` |
| `$psk_merc` | C / 1 | `pskMercBegin` (C) | `impl/campaign/rulecmd/HA_CMD.java` |
| `$pursuePlayer_hassle` | C / 3 | `plEnforcerInitial` (C) | Trace owning rule/command or generated interaction data |
| `$pursuePlayer_smugglingScan` | C / 1 | `cargoScanInitial` (C) | `impl/campaign/SmugglingScanScript.java` |
| `$pwi2_encounteredAlready` | CS / 1 | `pwi2Encounter` (C) | `$pwi2_encounteredAlready = true` — `pwi2Encounter` |
| `$pwi2_wantsItem` | C / 1 | `pwi2Encounter` (C) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$pwi_encounteredAlready` | CS / 1 | `pwiEncounter` (C) | `$pwi_encounteredAlready = true` — `pwiEncounter` |
| `$pwi_wantsItem` | C / 1 | `pwiEncounter` (C) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$questionedDeal` | CS / 3 | `SDMakeDealSelComOptD` (C) | `$questionedDeal = true 0` — `SDMakeDealSelComNegotiate` |
| `$raidContinueTrigger` | S / 8 | `lkeVirensRaidStart` (S) | `$raidContinueTrigger = LKEVirensRaidFinishedB 0` — `lkeVirensRaidStart` |
| `$raidDifficulty` | S / 10 | `lkeVirensRaidStart` (S) | `$raidDifficulty = 250 0` — `lkeVirensRaidStart` |
| `$raidGoBackTrigger` | S / 10 | `lkeVirensRaidStart` (S) | `$raidGoBackTrigger = PopulateOptions 0` — `lkeVirensRaidStart` |
| `$raidRestrictToTrigger` | S / 10 | `lkeVirensRaidStart` (S) | `$raidRestrictToTrigger = LKEVirensRaidFinishedA 0` — `lkeVirensRaidStart` |
| `$rank` | COT / 108 | `greetingDefaultInfamous` (T) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$ranks` | T / 2 | `gaKAPatrolRefuse` (T) | Trace owning rule/command or generated interaction data |
| `$rdsm_contact` | C / 1 | `rdsmreturnToContact` (C) | `impl/campaign/missions/RuinsDataSwapMission.java` |
| `$rdsm_dist` | ST / 5 | `rdsmOfferTextContact` (T) | `impl/campaign/missions/RuinsDataSwapMission.java` |
| `$rdsm_marinesReq` | ST / 2 | `rdsmOfferTextContactDeciv2b` (T) | `impl/campaign/missions/RuinsDataSwapMission.java` |
| `$rdsm_marketName` | T / 2 | `rdsmOfferTextContactDeciv2b` (T) | Trace owning rule/command or generated interaction data |
| `$rdsm_megacorpName` | OST / 8 | `rdsmOfferTextContact` (T) | `impl/campaign/missions/RuinsDataSwapMission.java` |
| `$rdsm_planetName` | ST / 2 | `rdsmOfferTextContact` (T) | `impl/campaign/missions/RuinsDataSwapMission.java` |
| `$rdsm_raidDifficulty` | S / 1 | `rdsmDecivInteractionRaidOptions` (S) | `impl/campaign/missions/RuinsDataSwapMission.java` |
| `$rdsm_reward` | ST / 5 | `rdsmOfferTextContact` (T) | `impl/campaign/missions/RuinsDataSwapMission.java` |
| `$rdsm_stage` | C / 1 | `rdsmreturnToContact` (C) | Trace owning rule/command or generated interaction data |
| `$rdsm_systemName` | ST / 2 | `rdsmOfferTextContact` (T) | `impl/campaign/missions/RuinsDataSwapMission.java` |
| `$rdsm_target` | ST / 8 | `rdsmOfferTextContact` (T) | `impl/campaign/missions/RuinsDataSwapMission.java` |
| `$rdsm_targetPlanet` | C / 1 | `rdsmPlanetInteraction` (C) | `impl/campaign/missions/RuinsDataSwapMission.java` |
| `$rdsm_variant` | C / 1 | `rdsmOfferTextContactDeciv` (C) | Trace owning rule/command or generated interaction data |
| `$rdsm_variation` | C / 1 | `rdsmDecivInteractionRaid` (C) | `impl/campaign/missions/RuinsDataSwapMission.java` |
| `$reasons` | CS / 31 | `BFFIpsRuseOpenNiceAlmost` (C) | `$reasons = 0` — `BFFIpsRuseOpenNice` |
| `$receivedAPlanetkillerFromPlayer` | S / 4 | `pk_givePKPatherSel` (S) | Trace owning rule/command or generated interaction data |
| `$receivedAPlanetkillerFromPlayerMercenary` | S / 1 | `pk_hegPaySel3` (S) | Trace owning rule/command or generated interaction data |
| `$recognizedAsInfamous` | S / 1 | `greetingDefaultInfamous` (S) | Trace owning rule/command or generated interaction data |
| `$rejectedRetrieval` | CS / 3 | `gaIntro2surveyOpenAgain` (C) | `$rejectedRetrieval = true 0` — `gaIntro2leaveIt` |
| `$relAdjective` | T / 2 | `marketPostOpenNoTrade` (T) | `impl/campaign/CoreCampaignPluginImpl.java`, `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$relativeStrength` | C / 3 | `LPTitheCheck` (C) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$remnantDestroyed` | C / 1 | `beaconOpenDialogRemnantsDestroyed` (C) | `impl/campaign/procgen/themes/RemnantThemeGenerator.java` |
| `$remnantResurgent` | C / 1 | `beaconOpenDialogRemnantsResurgent` (C) | `impl/campaign/procgen/themes/RemnantThemeGenerator.java` |
| `$remnantSuppressed` | C / 1 | `beaconOpenDialogRemnantsSuppressed` (C) | `impl/campaign/procgen/themes/RemnantThemeGenerator.java` |
| `$removeSpecialModifications` | S / 3 | `pk_executorInsistSel2` (S) | `$removeSpecialModifications = false` — `pk_executorInsistSel2` |
| `$removedCuffs` | CS / 6 | `lkeVirensRaidTeaBuncuffed` (C) | `$removedCuffs = true 0` — `lkeVirensRaidTeaC` |
| `$repCheckResult` | S / 1 | `customsInspectionWaitFinished` (S) | Trace owning rule/command or generated interaction data |
| `$repairSupplyCost` | S / 1 | `marketAddOptionRepair3` (S) | `impl/campaign/rulecmd/RepairAll.java`, `impl/campaign/rulecmd/RepairAvailable.java` |
| `$requiresDiscretionToDeal` | C / 1 | `generic_refuseToDealKnownInhospitable` (C) | `impl/campaign/ids/MemFlags.java` |
| `$risksTold` | CS / 1 | `gaATGreturnScansToCoureuseStart` (C) | `$risksTold = true` — `gaATGreturnScansToCoureuseStart` |
| `$rsom_contactName` | T / 2 | `rsomDidRaidOutro` (T) | `impl/campaign/missions/RaidSecretOutpostMission.java` |
| `$rsom_dist` | ST / 1 | `RSOMofferTextContact` (T) | `impl/campaign/missions/RaidSecretOutpostMission.java` |
| `$rsom_marinesReq` | ST / 1 | `RSOMofferTextContact` (T) | `impl/campaign/missions/RaidSecretOutpostMission.java` |
| `$rsom_planetName` | ST / 1 | `RSOMofferTextContact` (T) | `impl/campaign/missions/RaidSecretOutpostMission.java` |
| `$rsom_product` | T / 5 | `RSOMblurb` (T) | `impl/campaign/missions/RaidSecretOutpostMission.java` |
| `$rsom_productID` | S / 2 | `rsomDidRaidOutro` (S) | `impl/campaign/missions/RaidSecretOutpostMission.java` |
| `$rsom_quantity` | S / 2 | `rsomDidRaidOutro` (S) | `impl/campaign/missions/RaidSecretOutpostMission.java` |
| `$rsom_raidDifficulty` | S / 1 | `rsomRaidOptions` (S) | `impl/campaign/missions/RaidSecretOutpostMission.java` |
| `$rsom_systemName` | ST / 1 | `RSOMofferTextContact` (T) | `impl/campaign/missions/RaidSecretOutpostMission.java` |
| `$rsom_targetPlanet` | C / 1 | `rsomPlanetInteraction` (C) | `impl/campaign/missions/RaidSecretOutpostMission.java` |
| `$sShip_hullClass` | T / 2 | `sShipOfferTextBar` (T) | `impl/campaign/missions/SurplusShipHull.java` |
| `$sShip_hullSize` | OT / 6 | `sShipBlurb` (T) | `impl/campaign/missions/SurplusShipHull.java` |
| `$sShip_member` | S / 1 | `sShipPostAccept` (S) | `impl/campaign/missions/SurplusShipHull.java` |
| `$sShip_price` | OST / 5 | `sShipOfferTextBar` (T) | `impl/campaign/missions/SurplusShipHull.java` |
| `$sShip_rank` | OT / 2 | `sShipBlurbBar` (T) | `impl/campaign/missions/SurplusShipHull.java` |
| `$sShip_rankAOrAn` | T / 1 | `sShipBlurbBar` (T) | `impl/campaign/missions/SurplusShipHull.java` |
| `$sShip_ref` | S / 2 | `sShipBarLookAtFreighter` (S) | `impl/campaign/missions/SurplusShipHull.java` |
| `$sShip_ref2` | S / 1 | `sShipPostAccept` (S) | `impl/campaign/missions/SurplusShipHull.java` |
| `$saic_eventRef` | CS / 3 | `saic_openDialog` (C) | `impl/campaign/intel/bar/events/PlanetaryShieldIntel.java`, `impl/campaign/intel/bar/events/ScientistAICoreIntel.java` |
| `$saic_heOrShe` | T / 1 | `saic_initialText` (T) | `impl/campaign/intel/bar/events/ScientistAICoreIntel.java` |
| `$saic_marketName` | T / 1 | `saic_initialText` (T) | `impl/campaign/intel/bar/events/ScientistAICoreIntel.java` |
| `$saic_marketOnOrAt` | T / 1 | `saic_initialText` (T) | `impl/campaign/intel/bar/events/ScientistAICoreIntel.java` |
| `$saidA` | CS / 4 | `KOLTHolyArmadaCommsOptA` (C) | `$saidA = true 0` — `KOLTHolyArmadaCommsA` |
| `$saidAccept` | CS / 2 | `BFFIpsRuseOpenNiceDone` (C) | `$saidAccept = true` — `BFFIpsRuseOpenNiceDone` |
| `$saidAgnostic` | CS / 2 | `lppHookAttitudeAdjustment3` (C) | `$saidAgnostic = true 0` — `lppHookPretendPolite` |
| `$saidAndrada` | CS / 11 | `sdtuHyderTalk3OptionsJ` (C) | `$saidAndrada = true 0` — `sdtuHyderTalk3Andrada2a` |
| `$saidAndradaIsBraindead` | CS / 2 | `ImoinuKatoUmbraSayAndrada` (C) | `$saidAndradaIsBraindead = true` — `ImoinuKatoUmbraSayAndrada2` |
| `$saidAngels` | CS / 2 | `LKEjethroOptionAngels` (C) | `$saidAngels = true 0` — `LKEjethroWhyOptAResp` |
| `$saidAnswerEarly` | CS / 6 | `sdtuMacrioIntroHubD` (C) | `$saidAnswerEarly = true 0` — `sdtuMacIntroAnswerNo` |
| `$saidArmed` | CS / 4 | `lppVolturnCurateResponses1a` (C) | `$saidArmed = true 0` — `lppVolturnCurateAskArmed` |
| `$saidArroyo` | CS / 2 | `BFFImmBornBuyerOptHubE` (C) | `$saidArroyo = true 0` — `BFFImmBornArroyo` |
| `$saidAskRole` | CS / 2 | `BFFIupNotHereConvOptB` (C) | `$saidAskRole = true 0` — `BFFIupNotHereWhatRole` |
| `$saidB` | CS / 4 | `KOLTHolyArmadaCommsOptB` (C) | `$saidB = true 0` — `KOLTHolyArmadaCommsB` |
| `$saidBAgnostic` | CS / 3 | `lppHookAttitudeAdjustmentB2` (C) | `$saidBAgnostic = true 0` — `lppHookCurateHowKnow` |
| `$saidBAtheistic` | CS / 2 | `lppHookAttitudeAdjustmentB3` (C) | `$saidBAtheistic = true 0` — `lppHookCurateSecular` |
| `$saidBFaithful` | CS / 2 | `lppHookAttitudeAdjustmentB1` (C) | `$saidBFaithful = true 0` — `lppHookCurateStartPilgrimage` |
| `$saidBaird` | CS / 2 | `GAFCSiyavongFikenhildRevealOpt1` (C) | `$saidBaird = true 0` — `GAFCSiyavongFikenhildBaird` |
| `$saidBelieve` | CS / 2 | `gaATGbairdEndingHubOptBelieve` (C) | `$saidBelieve = true 0` — `gaATGbairdEndingBelieve` |
| `$saidBlast` | CS / 3 | `BFFIpsaConHubOptC` (C) | `$saidBlast = true 0` — `BFFIpsaConHubBlast` |
| `$saidBlind` | CS / 2 | `BFFIstationTalkHubConcerns` (C) | `$saidBlind = true 0` — `BFFIstationTalk1blind` |
| `$saidBlockage` | S / 1 | `PLArmadaWhyBlockade` (S) | `$saidBlockage = true 0` — `PLArmadaWhyBlockade` |
| `$saidBloodthirsty` | CS / 2 | `lkeJaspisStartHubOp2` (C) | `$saidBloodthirsty = true 0` — `lkeJaspisStartBlood1` |
| `$saidBoss` | CS / 8 | `sdtuHyderTalk3OptionsG` (C) | `$saidBoss = true 0` — `sdtuHyderTalkBossA` |
| `$saidBringNewsMission` | CS / 6 | `LOCRLFmissionBringNews` (C) | `$saidBringNewsMission = true` — `LOCRLFbringNews1` |
| `$saidBuyer` | CS / 2 | `BFFIstationTalkHubBuyer` (C) | `$saidBuyer = true 0` — `BFFIstationBuyer1` |
| `$saidBuyerColony` | CS / 2 | `BFFImmBornBuyerOptHubC` (C) | `$saidBuyerColony = true 0` — `BFFImmBornBuyerColony` |
| `$saidBuyerDemarch` | CS / 2 | `BFFImmBornBuyerOptHubD` (C) | `$saidBuyerDemarch = true 0` — `BFFImmBornBuyerLeague` |
| `$saidBuyerPoor` | CS / 6 | `BFFImmBornBuyerOptHubPayPlan` (C) | `$saidBuyerPoor = true 0` — `BFFImmBornBuyerPoor` |
| `$saidC` | CS / 5 | `KOLTHolyArmadaCommsOptC` (C) | `$saidC = true 0` — `KOLTHolyArmadaCommsC` |
| `$saidCaden` | CS / 3 | `sdtuPostHyderReportC` (C) | `$saidCaden = true 0` — `sdtuPostHyderCaden` |
| `$saidCarefully` | CS / 2 | `BFFIpsaConHubOptCarefully` (C) | `$saidCarefully = true 0` — `BFFIpsaConHubOptRespA1` |
| `$saidCarefully2` | CS / 2 | `BFFIpsaConHubOptCarefully` (C) | `$saidCarefully2 = true 0` — `BFFIpsaConHubRespCarefully` |
| `$saidCircumstances` | CS / 3 | `RaoPitchHubOptC` (C) | `$saidCircumstances = true` — `RaoPitchOptTakeCircumstances` |
| `$saidConcerns` | CS / 2 | `BFFIstationTalkHubConcerns` (C) | `$saidConcerns = true 0` — `BFFIstationConcerns1` |
| `$saidConfront` | CS / 2 | `BFFIstationTalkHubConfront` (C) | `$saidConfront = true 0` — `BFFIstationConfront1` |
| `$saidCotton3` | S / 1 | `LKEvirensRaidCotton2b` (S) | `$saidCotton3 = true 0` — `LKEvirensRaidCotton2b` |
| `$saidCotton4` | S / 1 | `LKEvirensRaidCotton3` (S) | `$saidCotton4= true 0` — `LKEvirensRaidCotton3` |
| `$saidCottonHearsMusic` | CS / 3 | `LKEjethroAngelsTalkC` (C) | `$saidCottonHearsMusic = true 0` — `LKEjethroAngelsTalkD1` |
| `$saidCourtMartial` | S / 1 | `RaoPitchOptCourtMartial` (S) | `$saidCourtMartial = true` — `RaoPitchOptCourtMartial` |
| `$saidCynical` | CS / 5 | `lppHookAttitudeAdjustment2` (C) | `$saidCynical = true 0` — `lppHookPretendPrayer` |
| `$saidD` | CS / 4 | `KOLTHolyArmadaCommsOptD` (C) | `$saidD = true 0` — `KOLTHolyArmadaCommsD` |
| `$saidDanger` | CS / 7 | `pkSentinelRemoveQuestions` (C) | `$saidDanger = true 0` — `PKSentinelAskDangerVague2` |
| `$saidDardan` | CS / 2 | `ImoinuKatoUmbraOptDardan` (C) | `$saidDardan = true` — `ImoinuKatoUmbraDardan1` |
| `$saidDaud` | CS / 2 | `TseenKeAskDaud` (C) | `$saidDaud = true` — `TseenKeAskDaud2` |
| `$saidDaudOp` | CS / 9 | `TseenKeAskDaudOptD` (C) | `$saidDaudOp = true` — `TseenKeAskDaudOpA` |
| `$saidDead` | CS / 5 | `LKEjethroOptionDead` (C) | `$saidDead = true 0` — `LKEjethroDead3a` |
| `$saidDecisively` | S / 1 | `BFFIpsaConHubOptRespA1bb` (S) | `$saidDecisively = true 0` — `BFFIpsaConHubOptRespA1bb` |
| `$saidDemand` | S / 1 | `PLArmadaWhyBlockadeDemand` (S) | `$saidDemand = true 0` — `PLArmadaWhyBlockadeDemand` |
| `$saidDestroyed` | CS / 2 | `shroudedSubstrateOptD` (C) | `$saidDestroyed = true 0` — `shroudedSubstrateOptSelD` |
| `$saidDevout` | CS / 2 | `lppVolturnOfficialResponseDevout` (C) | `$saidDevout = true 1` — `lppVolturnOfficialDevout1` |
| `$saidDoStealHub4` | CS / 3 | `BFFImmBornDoStealHubA` (C) | `$saidDoStealHub4 = true 0` — `BFFImmBornDoSteal4` |
| `$saidDoStealHub5` | CS / 2 | `BFFImmBornDoStealHubB` (C) | `$saidDoStealHub5 = true 0` — `BFFImmBornDoSteal5` |
| `$saidDoStealHubFaith` | CS / 2 | `BFFImmBornDoStealHubC` (C) | `$saidDoStealHubFaith = true 0` — `BFFImmBornDoStealSteal` |
| `$saidDoStealHubLetGo` | CS / 3 | `BFFImmBornDoStealHubE` (C) | `$saidDoStealHubLetGo = true 0` — `BFFImmBornDoStealLetGo` |
| `$saidDoStealHubTell` | CS / 2 | `BFFImmBornDoStealHubD` (C) | `$saidDoStealHubTell = true 0` — `BFFImmBornDoStealTell` |
| `$saidE` | CS / 2 | `KOLTHolyArmadaCommsOptE` (C) | `$saidE = true 0` — `KOLTHolyArmadaCommsE` |
| `$saidElekPrefer` | CS / 2 | `ZGRpostGADHOelekHubOptA` (C) | `$saidElekPrefer = true 0` — `ZGRpostGADHOelekPrefer` |
| `$saidFaithNotYourBusiness` | CS / 3 | `ZGRacknowledgeChurchOpsA` (C) | `$saidFaithNotYourBusiness = true 0` — `ZGRackChurchOptNotBusiness` |
| `$saidFaithResponse` | CS / 6 | `lkeChalcedonSedgeOptFaithA` (C) | `$saidFaithResponse = true 0` — `lkeSedgeFaithA` |
| `$saidFaithful` | CS / 6 | `lppHookAttitudeAdjustment1` (C) | `$saidFaithful = true 0` — `lppHookJoinPrayer` |
| `$saidFarm` | CS / 2 | `LKEjethroOptionFarm` (C) | `$saidFarm = true 0` — `LKEjethroFarm` |
| `$saidFight` | CS / 2 | `BFFIstationTalkHubObjection2` (C) | `$saidFight = true 0` — `BFFIstationTalk2fight` |
| `$saidFleet` | CS / 2 | `pirateStationKingBizOptC` (C) | `$saidFleet = true 0` — `pirateSKtooMuchFleet` |
| `$saidFreeWill` | CS / 2 | `lcMakeDealOptsFreeWill` (C) | `$saidFreeWill = true` — `LCmakeDealFreeWill` |
| `$saidGensKato` | CS / 2 | `ImoinuKatoUmbraAskWhat` (C) | `$saidGensKato = true` — `ImoinuKatoUmbraAskWhat2` |
| `$saidGreatStory` | CS / 3 | `BFFImenesPartySteal15a` (C) | `$saidGreatStory = true 0` — `BFFImenesPartySteal14a` |
| `$saidHates` | CS / 2 | `sdtuPostHyderReportA` (C) | `$saidHates = true 0` — `sdtuPostHyderHatesYou` |
| `$saidHaveCom` | CS / 3 | `lcSacredProtectorsChurchCom` (C) | `$saidHaveCom = true 0` — `LCP_butHaveCom` |
| `$saidHegemony` | CS / 3 | `sdtuHyderTalk3OptionsHeg` (C) | `$saidHegemony = true 0` — `sdtuHyderTalk3HegemonyA` |
| `$saidHegemonyOut` | CS / 2 | `sdtuPostHyderReportHeg` (C) | `$saidHegemonyOut = true 0` — `sdtuPostHyderHegemonyOut` |
| `$saidHonesty` | CS / 2 | `BFFIstationTalkHubBuyer` (C) | `$saidHonesty = true 0` — `BFFIstationTalk1honesty` |
| `$saidHopeless` | CS / 3 | `LKEjethroDeadHopeless` (C) | `$saidHopeless = true 0` — `LKEjethroWhyOptERespB` |
| `$saidHyder` | CS / 3 | `sdtuCadenHyderBackWarn` (C) | `$saidHyder = true 0` — `sdtuCadenHyderBackWarn1` |
| `$saidHyderBackMacario` | S / 1 | `sdtuPostHyderLieBacksMacario` (S) | `$saidHyderBackMacario = true 0` — `sdtuPostHyderLieBacksMacario` |
| `$saidHyderNoLead` | CS / 2 | `sdtuPostHyderReportD` (C) | `$saidHyderNoLead = true 0` — `sdtuPostHyderHerLead` |
| `$saidHyderSupply` | CS / 2 | `sdtuPostHyderReportE` (C) | `$saidHyderSupply = true 0` — `sdtuPostHyderSupply` |
| `$saidINTSECreally` | CS / 2 | `TseenKeAskDaudOptB` (C) | `$saidINTSECreally = true` — `TseenKeAskDaudSelB` |
| `$saidImportant` | CS / 3 | `adonyaOptImportantToFind` (C) | `$saidImportant = true 0` — `adonyaImportantToFind` |
| `$saidImportant2` | CS / 2 | `adonyaOptImportantToFind2` (C) | `$saidImportant2 = true 0` — `adonyaImportantToFind2` |
| `$saidIntentions` | CS / 2 | `BFFIupNotHereConvOptE` (C) | `$saidIntentions = true 0` — `BFFIupNotHereIntentions` |
| `$saidIonStorm` | CS / 4 | `LOCRMcontact1againIonStorm` (C) | `$saidIonStorm = true` — `LOCRMleaveLie` |
| `$saidJaspis` | CS / 2 | `LKEjethroOptionJaspis` (C) | `$saidJaspis= true 0` — `LKEjethroWhy` |
| `$saidJeff` | CS / 3 | `LKEjethroOptionF` (C) | `$saidJeff = true 0` — `LKEjethroJeff` |
| `$saidKilledSedge` | CS / 3 | `BFFIpsRuseMeanBsaidKilled` (C) | `$saidKilledSedge = true 0` — `BFFIstationTalk2sedgeB` |
| `$saidLeague` | CS / 2 | `gaFCReturnTransportHubA` (C) | `$saidLeague = true 0` — `gaFCReturnHubLeague` |
| `$saidLetsGo` | CS / 7 | `LKEjethroOptionA` (C) | `$saidLetsGo = true 0` — `LKEjethroC` |
| `$saidLiftBlockade` | S / 1 | `PLArmadaLiftBlockade` (S) | `$saidLiftBlockade = true 0` — `PLArmadaLiftBlockade` |
| `$saidLookingFor` | CS / 4 | `LOCRLFmissionFoundOpt` (C) | `$saidLookingFor = true` — `LOCRLFsayLookingFor` |
| `$saidMacario` | S / 3 | `sdtuHyderTalk3MacarioA` (S) | `$saidMacario = true 0` — `sdtuHyderTalk3MacarioA` |
| `$saidMission` | CS / 3 | `LKEjethroOptionMission` (C) | `$saidMission = true 0` — `LKEjethroWhat` |
| `$saidMoloch` | CS / 3 | `lkeVirensRaidOptionE` (C) | `$saidMoloch = true 0` — `LKEvirensRaidToBusiness` |
| `$saidMoreAlike` | S / 1 | `gaFCarchonAskDemChubOptD` (S) | `$saidMoreAlike = true` — `gaFCarchonAskDemChubOptD` |
| `$saidMother` | S / 1 | `ImoinuKantoUmbraMother1` (S) | `$saidMother = true` — `ImoinuKantoUmbraMother1` |
| `$saidMurder` | CS / 2 | `lkeChalcedonSedgeOptMurder` (C) | `$saidMurder = true 0` — `lkeAskSedgeMurder` |
| `$saidNiceThings` | CS / 4 | `lkeChalcedonVIPreasonDefault` (C) | `$saidNiceThings = true 0` — `lkeChalcedonVIPreason1` |
| `$saidNoToTT` | CS / 4 | `gaDHOvisitElekHubOptDecline` (C) | `$saidNoToTT = true 0` — `gaDHOvisitElekHubSelA6b` |
| `$saidNotPather` | CS / 2 | `lkeVirensRaidOptionF` (C) | `$saidNotPather = true 0` — `LKEvirensRaidNotPather` |
| `$saidOpening` | CS / 5 | `BFFImenesPPtalkOptA` (C) | `$saidOpening = true 0` — `BFFImenesPPinsideJob` |
| `$saidOptB` | CS / 2 | `SDMakeDealSelComOptB` (C) | `$saidOptB = true 0` — `SDMakeDealSelComNegotiate` |
| `$saidOptC` | CS / 2 | `SDMakeDealSelComOptC` (C) | `$saidOptC = true 0` — `SDMakeDealSelComMaybeNo` |
| `$saidPain` | CS / 3 | `BFFIstationMedConsultOptA` (C) | `$saidPain = true 0` — `BFFIstationMedConPain` |
| `$saidPathThing` | CS / 2 | `lcMakeDealOptsPath` (C) | `$saidPathThing = true` — `LCmakeDealPath` |
| `$saidPerhapsTalk` | S / 2 | `BFFIpsaConHubTalk` (S) | `$saidPerhapsTalk= true 0` — `BFFIpsaConHubTalk` |
| `$saidPersonal` | CS / 2 | `RaoPitchHubOptE` (C) | `$saidPersonal = true` — `RaoPitchOptPersonal` |
| `$saidPitchElek` | CS / 5 | `ZGRpostGADHOconfrontOpt` (C) | `$saidPitchElek = true` — `ZGRmakePitch9elek` |
| `$saidPitchKnow` | CS / 2 | `ZGRstartPitch8OMKIknow` (C) | `$saidPitchKnow = true 0` — `ZGRmakePitch9know` |
| `$saidPledge` | CS / 3 | `sdtuCadenPledgeVsMacario` (C) | `$saidPledge = true 0` — `sdtuCadenPledgeVsMacarioText` |
| `$saidPromotion` | CS / 5 | `sdtuHyderTalk3OptionsG` (C) | `$saidPromotion = true 0` — `sdtuHyderTalkBossPromotionA` |
| `$saidProvide` | CS / 2 | `lcMakeDealOptsProvide` (C) | `$saidProvide = true` — `LCmakeDealProvide` |
| `$saidRao` | CS / 2 | `TseenKeAskRao` (C) | `$saidRao = true` — `TseenKeAskRao2` |
| `$saidReasonable` | CS / 2 | `pirateStationKingBizOptB` (C) | `$saidReasonable = true 0` — `pirateSKtooMuchReasonable` |
| `$saidRecruiter` | CS / 6 | `lkeChalcedonBarSedgeOptB` (C) | `$saidRecruiter = true 0` — `lkeChalcedonBarSedgeRespB` |
| `$saidRelativelyHonest` | CS / 2 | `ImoinuUmbraMacarioHubOptZ` (C) | `$saidRelativelyHonest = true` — `ImoinuKatoUmbraSayAndrada3` |
| `$saidRespect` | CS / 2 | `RaoPitchHubOptB` (C) | `$saidRespect = true 0` — `RaoPitchOptRespect` |
| `$saidReward` | CS / 2 | `ZGRstartPitchEndRewardInfo` (C) | `$saidReward = true 0` — `ZGRstartPitchRewardInfo` |
| `$saidScylla` | CS / 2 | `gaFCReturnTransportHubB` (C) | `$saidScylla = true 0` — `gaFCReturnHubScylla` |
| `$saidSecular` | CS / 4 | `shrineFleetConvAskShrineQuestA` (C) | `$saidSecular = true` — `shrineFleetConvShrineQuestA1` |
| `$saidSedge` | CS / 3 | `BFFIstationTalkHubObjection3` (C) | `$saidSedge = true 0` — `BFFIstationTalk2sedge` |
| `$saidShrine` | CS / 2 | `lppKillaOssuarySeeShrine2` (C) | `$saidShrine = true 0` — `lppKillaOssuarySeeShrine` |
| `$saidSin` | CS / 2 | `BFFIstationTalkHubConfront` (C) | `$saidSin = true 0` — `BFFIstationTalk1sin` |
| `$saidSmuggling` | CS / 4 | `sdtuHyderTalk3OptionsC` (C) | `$saidSmuggling = true 0` — `sdtuHyderTalk3WhySmuggleA` |
| `$saidSourcesA` | CS / 2 | `BFFIupNotHereConvOptD` (C) | `$saidSourcesA = true 0` — `BFFIupNotHereSourcesA` |
| `$saidSourcesB` | CS / 2 | `BFFIupNotHereConvOptC` (C) | `$saidSourcesB = true 0` — `BFFIupNotHereSourcesB` |
| `$saidSpooky` | CS / 2 | `lppKillaOssuarySpooky2` (C) | `$saidSpooky = true 0` — `lppKillaOssuarySpooky` |
| `$saidStrayedFromChurch` | CS / 3 | `LOCRLsayChurchOpt` (C) | `$saidStrayedFromChurch = true` — `LOCRLsayChurch` |
| `$saidStrongarm` | S / 1 | `PLArmadaWhyBlockadeStrongarm` (S) | `$saidStrongarm = true 0` — `PLArmadaWhyBlockadeStrongarm` |
| `$saidStuff` | CS / 24 | `LKEjethroOptionF` (C) | `$saidStuff++` — `LKEjethroA` |
| `$saidSupplies` | CS / 2 | `sdtuCadenAskSupplies` (C) | `$saidSupplies = true 0` — `sdtuCadenAskSupplies2` |
| `$saidTTthen` | CS / 2 | `shroudedSubstrateOptSelAga2b` (C) | `$saidTTthen = true 0` — `shroudedSubstrateOptSelAtt` |
| `$saidTakeDiktat` | CS / 3 | `RaoPitchHubOptD` (C) | `$saidTakeDiktat = true` — `RaoPitchOptTakeDiktat` |
| `$saidTartessus` | CS / 2 | `BFFIonBoardMenesOpt4` (C) | `$saidTartessus = true 0` — `BFFIonBoardAskTart4` |
| `$saidTea` | CS / 3 | `lkeChalcedonBarSedgeOptD` (C) | `$saidTea = true 0` — `lkeChalcedonBarSedgeRespTea` |
| `$saidTea2` | CS / 2 | `lkeChalcedonBarSedgeOptD2` (C) | `$saidTea2 = true 0` — `lkeChalcedonBarSedgeRespTea2` |
| `$saidTechReturn` | CS / 11 | `ZGRtechReturn` (C) | `$saidTechReturn = true 0` — `ZGRpostWeirdProceed` |
| `$saidThank` | CS / 2 | `BFFIstationMedConsultOptC` (C) | `$saidThank = true 0` — `BFFIstationMedConThank` |
| `$saidThousands` | CS / 2 | `lppKillaOssuaryThousands2` (C) | `$saidThousands = true 0` — `lppKillaOssuaryThousands` |
| `$saidTracked` | CS / 2 | `BFFIstationTalkHubObjection1` (C) | `$saidTracked = true 0` — `BFFIstationTalk2tracked` |
| `$saidTurnips` | S / 1 | `LCmakeDealTurnips` (S) | `$saidTurnips = true` — `LCmakeDealTurnips` |
| `$saidUh` | CS / 3 | `BFFItalkToEngineer12optC` (C) | `$saidUh = true 0` — `BFFItalkToEngineer13a` |
| `$saidUseful` | CS / 2 | `shroudedSubstrateOptC` (C) | `$saidUseful = true 0` — `shroudedSubstrateOptSelC` |
| `$saidValuable` | CS / 2 | `shroudedSubstrateOptB` (C) | `$saidValuable = true 0` — `shroudedSubstrateOptSelB` |
| `$saidVictory` | CS / 2 | `hegTalkInspectDaudVictoryD2` (C) | `$saidVictory = true 0` — `hegTalkInspectDaudDictate` |
| `$saidVisit` | CS / 2 | `lppVolturnOfficialResponseVisit` (C) | `$saidVisit = true 1` — `lppVolturnOfficialVisit` |
| `$saidWalk` | CS / 2 | `lppKillaOssuaryWalkPilgrim2` (C) | `$saidWalk = true 0` — `lppKillaOssuaryWalkPilgrim` |
| `$saidWalked` | CS / 3 | `lkeVirensRaidOptionC` (C) | `$saidWalked = true 0` — `LKEvirensRaidWalked` |
| `$saidWantToHelpScylla` | CS / 2 | `GAFCSiyavongFikenhildHubB1` (C) | `$saidWantToHelpScylla = true` — `GAFCSiyavongFikenhildHelp` |
| `$saidWarHero` | CS / 2 | `raoWarHero` (C) | `$saidWarHero = true` — `raoWarHero1` |
| `$saidWeaponized` | CS / 3 | `shroudedSubstrateOptE` (C) | `$saidWeaponized = true 0` — `shroudedSubstrateOptSelE2` |
| `$saidWhatIsIt` | CS / 2 | `shroudedSubstrateOptA` (C) | `$saidWhatIsIt = true 0` — `shroudedSubstrateOptSelA` |
| `$saidWhatNeed` | CS / 4 | `RaoPitchHubOptA` (C) | `$saidWhatNeed = true` — `RaoPitchOptWhatNeed2` |
| `$saidWouldTalkToReynard` | S / 2 | `PLArmadaIdealisticYes` (S) | Trace owning rule/command or generated interaction data |
| `$salvageLeaveText` | OS / 12 | `sal_defaultLeave1` (O) | `$salvageLeaveText = Leave 0` — `GS_AI_CORES_cont2Sel` |
| `$salvageSpecId` | S / 8 | `salRuins_scattered` (S) | `$salvageSpecId = ruins_scattered` — `salRuins_scattered` |
| `$salvageSpecialData` | C / 14 | `sal_checkSpecialFound` (C) | `impl/campaign/ids/MemFlags.java`, `impl/campaign/rulecmd/salvage/SalvageGenFromSeed.java` |
| `$sample_vambrace` | S / 1 | `oyaTanaicaAskSampleVambrace` (S) | `$sample_vambrace = true 0` — `oyaTanaicaAskSampleVambrace` |
| `$satelliteStillInOrbit` | CS / 2 | `zig_alphaSiteAlreadyExploredNoSat` (C) | `$satelliteStillInOrbit = true 739` — `zig_alphaSite` |
| `$sawPlayerTransponderOff` | CS / 6 | `tOffPatrolBegin` (C) | `impl/campaign/ids/MemFlags.java` |
| `$sawPlayerTransponderOn` | S / 1 | `mpm_pirateEncounter` (S) | `$sawPlayerTransponderOn = true 5` — `mpm_pirateEncounter` |
| `$sawPlayerWithTOffCount` | CS / 2 | `tOffPatrolBeginNoTalk` (C) | `$sawPlayerWithTOffCount++ 10` — `tOffPatrolBegin` |
| `$scan_cargoScanResult` | S / 4 | `tOffCargoScanBoarding2` (S) | `impl/campaign/rulecmd/CargoScan.java` |
| `$scan_contrabandFound` | C / 4 | `tOffCargoScanClean` (C) | `impl/campaign/rulecmd/CargoScan.java` |
| `$scan_podsFound` | CS / 2 | `tOffCargoScanPods` (C) | `impl/campaign/rulecmd/CargoScan.java` |
| `$scan_suspiciousCargoFound` | C / 4 | `tOffCargoScanClean` (C) | `impl/campaign/rulecmd/CargoScan.java` |
| `$scanned` | CS / 4 | `gsVambOptHubA` (C) | `$scanned = true` — `gsVambraceExplore1` |
| `$scaredSang` | CS / 2 | `soeDuelResponseScared` (C) | `$scaredSang = true 0` — `soeDuelScared` |
| `$sdtuAgentDebris` | C / 1 | `sdtuDebrisOpen` (C) | `impl/campaign/missions/askonia/TheUsurpers.java` |
| `$sdtu_didCadenMeeting` | C / 1 | `sdtuMacarioPostCadenGreetingEnraged` (C) | `impl/campaign/missions/askonia/TheUsurpers.java` |
| `$sdtu_extractAgentRaidDifficulty` | S / 1 | `sdtuUmbraRaidStart` (S) | `impl/campaign/missions/askonia/TheUsurpers.java` |
| `$sdtu_interceptFleet` | C / 1 | `sdtuTraitorPatrolDialog` (C) | `impl/campaign/missions/askonia/TheUsurpers.java` |
| `$sdtu_marineLosses` | S / 1 | `sdtuRamMacarioMarineTry` (S) | `impl/campaign/missions/askonia/TheUsurpers.java` |
| `$sdtu_meetHyder` | C / 1 | `sdtuHyderAngryGreeting` (C) | `impl/campaign/missions/askonia/TheUsurpers.java` |
| `$sdtu_merc` | C / 2 | `sdtuMercEncounter` (C) | `impl/campaign/missions/askonia/TheUsurpers.java` |
| `$sdtu_multWeps` | CS / 4 | `sdtuRamMacarioWeaponDropOpt2s` (C) | `$sdtu_multWeps = true 0` — `sdtuRamSafehouse10wepCheckA` |
| `$sdtu_patrolSecond` | S / 2 | `sdtuPatrolForAPrice2` (S) | `impl/campaign/missions/askonia/TheUsurpers.java` |
| `$sdtu_stage` | C / 23 | `sdtuVolturnMeetRamOption` (C) | `impl/campaign/missions/askonia/TheUsurpers.java` |
| `$sdtu_xpRewardHigh` | S / 3 | `sdtuHyderTalkXPHigh` (S) | `impl/campaign/missions/askonia/TheUsurpers.java` |
| `$sdtu_xpRewardLow` | S / 2 | `sdtuHyderTalkXPLow` (S) | `impl/campaign/missions/askonia/TheUsurpers.java` |
| `$sdtu_xpRewardMedium` | S / 2 | `sdtuHyderTalkXPMed` (S) | `impl/campaign/missions/askonia/TheUsurpers.java` |
| `$seco_days` | ST / 1 | `secoOfferTextContact` (T) | `impl/campaign/missions/SecurityCodes.java` |
| `$seco_faction` | OST / 4 | `secoBlurb` (T) | `impl/campaign/missions/SecurityCodes.java` |
| `$seco_factionColor` | S / 2 | `secoOfferTextBar` (S) | `impl/campaign/missions/SecurityCodes.java` |
| `$seco_heOrShe` | T / 1 | `secoOfferTextBar` (T) | `impl/campaign/missions/SecurityCodes.java` |
| `$seco_manOrWoman` | OT / 3 | `secoBlurbBar` (T) | `impl/campaign/missions/SecurityCodes.java` |
| `$seco_price` | ST / 3 | `secoOfferTextBar` (T) | `impl/campaign/missions/SecurityCodes.java` |
| `$seco_ref` | S / 1 | `secoPostAccept` (S) | `impl/campaign/missions/SecurityCodes.java` |
| `$sedgeKillLie` | CS / 16 | `LKEvirensKillSedgeKilledMe` (C) | `$sedgeKillLie = true 0` — `LKEvirensWhyKillSedgeLies` |
| `$sentWarrant` | CS / 11 | `sdtuTraitorPatrolOptionConfront` (C) | `$sentWarrant = true` — `sdtuTraitorPatrolWarrantReaction` |
| `$setUpCottonMeeting` | CS / 8 | `gaATGmeetingIsSetUpWithCotton` (C) | `$setUpCottonMeeting = true` — `gaATGepiphanyBarEndLeave` |
| `$shipOrFleet` | OST / 301 | `gateFlyThrough` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$shoot` | CS / 3 | `gaATGzalMissionStartHub2d` (C) | `$shoot = true 0` — `gaATGzalMissionShoot` |
| `$shortName` | OST / 16 | `sal_printDefaultDefenders` (T) | `impl/campaign/CoreCampaignPluginImpl.java`, `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$showAfterScanIntro` | CS / 3 | `zig_encounterDescScanned` (C) | `$showAfterScanIntro = true` — `zig_encounterDesc2` |
| `$showHostileResponses` | CS / 10 | `shrineFleetConvAskAboutJob` (C) | `$showHostileResponses = true 0` — `shrineFleetConvOptionSelAtrocities` |
| `$showedHegCommission` | CS / 12 | `PKSentinelHubShowHegCommission` (C) | `$showedHegCommission = true` — `PKSentinelShowHegCom2` |
| `$shownFleetDescAlready` | CS / 1 | `initial_AnyFleet` (C) | `$shownFleetDescAlready = true 0` — `initial_AnyFleet` |
| `$shownReminderText` | CS / 1 | `gaIntro2surveyOpenAgainText` (C) | `$shownReminderText = true 0` — `gaIntro2surveyOpenAgainText` |
| `$shownScyllaProbesText` | CS / 1 | `gaPZ_investigateRelay2A` (C) | `$shownScyllaProbesText = true 0` — `gaPZ_investigateRelay2A` |
| `$siteExplored` | CS / 4 | `zig_alphaSite` (C) | `$siteExplored = true` — `zig_alphaSiteExplore1` |
| `$sitm_aOrAnItem` | T / 3 | `sitmBlurb1` (T) | `impl/campaign/missions/BlueprintIntel.java` |
| `$sitm_item` | O / 1 | `sitmOfferOption1` (O) | `impl/campaign/missions/BlueprintIntel.java` |
| `$sitm_price` | ST / 4 | `sitmOfferTextBar1` (T) | `impl/campaign/missions/BlueprintIntel.java` |
| `$sitm_ref` | S / 1 | `sitmPostAccept` (S) | `impl/campaign/missions/BlueprintIntel.java` |
| `$siyavongDidInterrupt` | CS / 6 | `gaFCFikenhildCavinHubResponse3` (C) | `$siyavongDidInterrupt = true` — `gaFCFikenhildCavinStartInterrupt` |
| `$siyavongStonewall` | CS / 7 | `GAFCSiyavongStonewall1mem` (C) | `$siyavongStonewall = true 0` — `GAFCSiyavongStonewallOption1` |
| `$skipTInfo` | S / 1 | `mpm_pirateEncounter` (S) | `$skipTInfo = true 0` — `mpm_pirateEncounter` |
| `$slBuildType` | CS / 6 | `stable_confirmOpts` (C) | `$slBuildType = comm_relay_makeshift 0` — `stable_buildRelay` |
| `$smug_commodityId` | S / 4 | `smugOfferTextBar` (S) | `impl/campaign/missions/SmugglingMission.java` |
| `$smug_commodityName` | T / 6 | `smugBlurb` (T) | `impl/campaign/missions/SmugglingMission.java` |
| `$smug_dist` | ST / 2 | `smugOfferTextBar` (T) | `impl/campaign/missions/SmugglingMission.java` |
| `$smug_manOrWoman` | OT / 3 | `smugBlurbBar` (T) | `impl/campaign/missions/SmugglingMission.java` |
| `$smug_marketName` | ST / 3 | `smugBlurb` (T) | `impl/campaign/missions/SmugglingMission.java` |
| `$smug_noCompleteShown` | CS / 2 | `smug_notEnough` (C) | `$smug_noCompleteShown = true 0` — `smug_checkCompletionNotSneaking` |
| `$smug_playerHasEnough` | C / 3 | `smug_checkCompletion` (C) | `impl/campaign/missions/SmugglingMission.java` |
| `$smug_quantity` | ST / 4 | `smugOfferTextBar` (T) | `impl/campaign/missions/SmugglingMission.java` |
| `$smug_ref` | S / 2 | `smugOfferTextBar` (S) | `impl/campaign/missions/SmugglingMission.java` |
| `$smug_reward` | ST / 2 | `smugOfferTextBar` (T) | `impl/campaign/missions/SmugglingMission.java` |
| `$soe_playerLostDuel` | C / 1 | `soeFightLose` (C) | `impl/campaign/eventide/DuelDialogDelegate.java` |
| `$soe_playerWonDuel` | C / 1 | `soeFightWin` (C) | `impl/campaign/eventide/DuelDialogDelegate.java` |
| `$someShipsDestroyed` | C / 4 | `gaFCScavengerDefeated` (C) | `impl/campaign/FleetInteractionDialogPluginImpl.java` |
| `$sorta_prisoner` | CS / 2 | `gaATGzalMissionStartHub1` (C) | `$sorta_prisoner = true 0` — `gaATGzalMissionHubA` |
| `$sourceMarket.id` | C / 4 | `greetingsScavMairaathNeutral` (C) | Trace owning rule/command or generated interaction data |
| `$sourceMarket.mc:free_market` | C / 2 | `tOffPatrolBegin` (C) | Trace owning rule/command or generated interaction data |
| `$sourceMarket.smugglingScanTimeout` | S / 9 | `tOffCargoScanBoarding2` (S) | `$sourceMarket.smugglingScanTimeout = true 30` — `tOffCargoScanBoarding2` |
| `$spm_target` | C / 1 | `spm_planetApproach` (C) | `impl/campaign/intel/SurveyPlanetMissionIntel.java` |
| `$spokeOpenly` | CS / 2 | `GAFCSiyavongStonewallOption1` (C) | `$spokeOpenly = true` — `GAFCSiyavongFirstGreeting_INVESTIGATE_FIKENHILD` |
| `$srs_baseHullId` | C / 1 | `zig_PostShipRecoverySpecial` (C) | `impl/campaign/rulecmd/salvage/special/ShipRecoverySpecial.java` |
| `$srs_memberId` | C / 1 | `hamatsu_PostShipRecoverySpecial` (C) | `impl/campaign/rulecmd/salvage/special/ShipRecoverySpecial.java` |
| `$ssat_completed` | S / 1 | `ssatObjectScan` (S) | `$ssat_completed = true` — `ssatObjectScan` |
| `$ssat_manOrWoman` | OT / 4 | `ssatBlurbBar` (T) | `impl/campaign/missions/SpySatDeployment.java` |
| `$ssat_marketName` | ST / 6 | `ssatBlurb` (T) | `impl/campaign/missions/SpySatDeployment.java` |
| `$ssat_personName` | T / 1 | `ssatObjectInteraction` (T) | `impl/campaign/missions/SpySatDeployment.java` |
| `$ssat_ref` | CS / 7 | `ssatPrintHostilesText` (C) | `impl/campaign/missions/SpySatDeployment.java` |
| `$ssat_reward` | ST / 4 | `ssatOfferTextBar` (T) | `impl/campaign/missions/SpySatDeployment.java` |
| `$ssat_systemName` | T / 4 | `ssatOfferTextBar2` (T) | `impl/campaign/missions/SpySatDeployment.java` |
| `$ssat_target` | C / 2 | `ssatPrintHostilesText` (C) | `impl/campaign/missions/SpySatDeployment.java` |
| `$ssat_underworld` | C / 6 | `ssatBlurbUW` (C) | `impl/campaign/missions/SpySatDeployment.java` |
| `$stability` | C / 8 | `flavorTextMarketGenericSmall` (C) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$stabilizeDays` | S / 1 | `pods_stabilizeSel` (S) | `impl/campaign/rulecmd/salvage/CargoPods.java` |
| `$stabilizeSupplies` | CS / 2 | `pods_stabilizeSuppliesNotEnough` (C) | `impl/campaign/rulecmd/salvage/CargoPods.java` |
| `$stabilized` | C / 1 | `pods_stabilizeDisableDidAlready` (C) | `impl/campaign/rulecmd/salvage/CargoPods.java` |
| `$standing` | S / 1 | `ZGRfirstVisitIntro6stand` (S) | `$standing = true 0` — `ZGRfirstVisitIntro6stand` |
| `$supplies` | S / 1 | `marketAddOptionRepair3` (S) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$sus` | S / 10 | `gaDHOendingFoundFirst1` (S) | `$sus = true 0` — `gaDHOendingFoundFirst1` |
| `$systemCutOffFromHyper` | C / 1 | `surveySystemIsCutOffCanNotColonize` (C) | `impl/campaign/CoreCampaignPluginImpl.java` |
| `$tOff_didAlready` | CS / 2 | `tOffPatrolBegin` (C) | `$tOff_didAlready = true 0` — `tOffPatrolBegin` |
| `$tabo_dist` | ST / 2 | `taboOfferTextBar` (T) | `impl/campaign/missions/TacticallyBombardColony.java` |
| `$tabo_fuel` | ST / 2 | `taboOfferTextBar` (T) | `impl/campaign/missions/TacticallyBombardColony.java` |
| `$tabo_marketName` | ST / 3 | `taboBlurb` (T) | `impl/campaign/missions/TacticallyBombardColony.java` |
| `$tabo_ref` | S / 2 | `taboOfferTextBar` (S) | `impl/campaign/missions/TacticallyBombardColony.java` |
| `$tabo_reward` | ST / 2 | `taboOfferTextBar` (T) | `impl/campaign/missions/TacticallyBombardColony.java` |
| `$tag:comm_relay` | C / 13 | `cob_hackOpt` (C) | Trace owning rule/command or generated interaction data |
| `$tag:dweller_light` | C / 2 | `abyssalLightDwellerBegin` (C) | Trace owning rule/command or generated interaction data |
| `$tag:empty` | C / 1 | `gaFCProbeInteractionStartEmpty` (C) | Trace owning rule/command or generated interaction data |
| `$tag:gaFC_lootedProbe` | C / 1 | `gaFCStolenProbePodStart` (C) | Trace owning rule/command or generated interaction data |
| `$tag:gas_giant` | C / 10 | `salRuins_scattered` (C) | Trace owning rule/command or generated interaction data |
| `$tag:gate` | C / 14 | `gateOpenDialog` (C) | Trace owning rule/command or generated interaction data |
| `$tag:involuntary_retirement` | C / 2 | `postGAFCretiredArchon` (C) | Trace owning rule/command or generated interaction data |
| `$tag:luddic_shrine` | C / 3 | `lppBeholderOpenDialog` (C) | Trace owning rule/command or generated interaction data |
| `$tag:makeshift` | C / 3 | `cob_salvageMakeshift` (C) | Trace owning rule/command or generated interaction data |
| `$tag:not_random_mission_target` | C / 1 | `gaIntro2surveyOpen` (C) | Trace owning rule/command or generated interaction data |
| `$tag:objective` | C / 1 | `cob_openDialog` (C) | Trace owning rule/command or generated interaction data |
| `$tag:replacement_archon` | C / 3 | `postGAFCNewArchonC` (C) | Trace owning rule/command or generated interaction data |
| `$tag:salvageable` | C / 20 | `GS_AI_CORES_open` (C) | Trace owning rule/command or generated interaction data |
| `$tag:sensor_array` | C / 3 | `cob_neutrinoBurstOpt` (C) | Trace owning rule/command or generated interaction data |
| `$tag:stable_location` | C / 1 | `stable_open` (C) | Trace owning rule/command or generated interaction data |
| `$tag:star` | C / 1 | `surveyStar` (C) | Trace owning rule/command or generated interaction data |
| `$tag:station` | C / 3 | `PathMarketWeirdHullmods4station` (C) | Trace owning rule/command or generated interaction data |
| `$tag:stellar_mirror` | C / 1 | `stellarMirrorOpenDialog` (C) | Trace owning rule/command or generated interaction data |
| `$tag:stellar_shade` | C / 1 | `stellarShadeDialog` (C) | Trace owning rule/command or generated interaction data |
| `$tag:story_critical` | C / 1 | `cob_sabOpt` (C) | Trace owning rule/command or generated interaction data |
| `$tag:underworld` | C / 1 | `genericGreetingNeedNoPatrolsUW` (C) | Trace owning rule/command or generated interaction data |
| `$tag:warning_beacon` | C / 6 | `beaconOpenDialog` (C) | Trace owning rule/command or generated interaction data |
| `$talkedTo` | CS / 2 | `psi_pilotGreetingTalkedTo` (C) | `$talkedTo = true` — `psi_pilotGreeting` |
| `$temporarilyIgnoreYou` | CS / 3 | `defaultGreetingIgnore` (C) | `$temporarilyIgnoreYou = true 1` — `PKGiveToChurchAgain` |
| `$testMissionEventRef` | S / 2 | `convTMOptionYes` (S) | Trace owning rule/command or generated interaction data |
| `$theFaction` | OT / 13 | `marketPostOpenToOffSneak` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/events/BaseEventPlugin.java` |
| `$theFactionLong` | T / 1 | `SECOPatrolHail` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/events/BaseEventPlugin.java` |
| `$theMarketFaction` | T / 2 | `marketPostOpenFSEMixedIndirect` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java`, `impl/campaign/econ/BaseMarketConditionPlugin.java` (more mentions) |
| `$theOtherCommissionFaction` | T / 8 | `CMSNTextHasOther_default` (T) | `impl/campaign/rulecmd/missions/Commission.java` |
| `$thePersonFaction` | T / 4 | `igr_offerBribeRejectedSel` (T) | `impl/campaign/CoreRuleTokenReplacementGeneratorImpl.java` |
| `$thereWasAProblem` | S / 2 | `BFFIproblemCheckFundingLie` (S) | `$thereWasAProblem = true 0` — `BFFIproblemCheckFundingLie` |
| `$threatsResponded` | CS / 2 | `gaFCIsirahMercHubOption4` (C) | `$threatsResponded = true` — `gaFCIsirahMercRespondThreats` |
| `$toldARCagent` | CS / 2 | `ImoinuKantoUmbraDoomed1ARClie` (C) | `$toldARCagent = true` — `sdtuUmbraTrickVIPOptionD2IK` |
| `$toldAboutMercs` | CS / 2 | `ArroyoAskAboutZGR2optC` (C) | `$toldAboutMercs = true 0` — `ArroyoAskAboutZGRoptEresp` |
| `$toldBairdAboutAstraia` | CS / 2 | `gaPZ_tellBairdAstraia` (C) | `$toldBairdAboutAstraia = true 10` — `gaPZ_tellBairdMods` |
| `$toldBigLie` | CS / 9 | `sdtuPostHyderReportLies` (C) | `$toldBigLie = true 0` — `sdtuPostHyderLieBacksCaden` |
| `$toldCallistoAboutGivingHamatsuToKanta` | CS / 4 | `callistoGaveHamatsuToKantaOpt` (C) | `$toldCallistoAboutGivingHamatsuToKanta = true` — `callistoGaveHamatsuToKantaOptSel1` |
| `$toldCavin` | CS / 4 | `gaFCArchonPLexcuseOptD` (C) | `$toldCavin = true 0` — `gaFCArchonPLtellCavin` |
| `$toldCredits` | CS / 2 | `lkeJaspisStart4credits` (C) | `$toldCredits = true 0` — `lkeJaspisInfoAsk1reply` |
| `$toldHypercomm` | CS / 6 | `gaFCArchonPLexcuseOptB` (C) | `$toldHypercomm = true 0` — `gaFCArchonPLtellRelay` |
| `$toldIntel` | CS / 4 | `gaFCArchonPLexcuseOptC` (C) | `$toldIntel = true 0` — `gaFCArchonPLtellIntel` |
| `$toldName` | CS / 2 | `PKHackStoryOptA` (C) | `$toldName = true 0` — `PKHackStoryStartB` |
| `$toldNotFleet` | CS / 2 | `adonyaOptNotFleet` (C) | `$toldNotFleet = true 0` — `adonyaNotFleet` |
| `$toldPathers` | CS / 4 | `gaFCArchonPLexcuseOptA` (C) | `$toldPathers = true 0` — `gaFCArchonPLtellPathers` |
| `$toldShotSedge` | CS / 5 | `LKEjethroWhatToldShot` (C) | `$toldShotSedge = true 0` — `LKEjethroWhat2b` |
| `$toldToVisitChalet` | S / 1 | `gaKAArroyoProvostSel2` (S) | `$toldToVisitChalet = true` — `gaKAArroyoProvostSel2` |
| `$toldZigInOrbit` | CS / 2 | `LKEjethroAngelsTalkTellZigOrbit` (C) | `$toldZigInOrbit = true 0` — `LKEjethroAngelsTalkTellZigOrbit2` |
| `$tollAmount` | OT / 4 | `customsInspectionResultToll` (T) | `impl/campaign/rulecmd/CustomsInspectionGenerateResult.java` |
| `$tookYouLongEnough` | CS / 4 | `GAFCSiyavongFikenhildHubAopt2` (C) | `$tookYouLongEnough = true 0` — `GAFCSiyavongFikenhildLongEnough` |
| `$topLEscore` | CS / 7 | `LuddicEthosRefresh1` (C) | `$topLEscore = 0 0` — `LuddicEthosRefresh` |
| `$tpReq` | CS / 3 | `cTapCheckCanAfford` (C) | `$tpReq = 5000 0` — `cTap_infoText` |
| `$tradeMode` | CS / 46 | `marketOptTradeMulti` (C) | `$tradeMode = OPEN 0` — `marketPostOpenDefault` |
| `$transmitNoun` | ST / 3 | `gaDataGiveDataNegotiate2` (T) | `$transmitNoun = "these coordinates" 0` — `gaDataPirateConvBeforeGotData` |
| `$transmittedData` | CS / 16 | `gaDHOendingAskRewardPostOptA` (C) | `$transmittedData = true 0` — `gaDHOendingDataFirst` |
| `$transmittedDataFirst` | CS / 2 | `gaDHOendingGiveSumEndBelieves` (C) | `$transmittedDataFirst = true 0` — `gaDHOendingDataFirst` |
| `$transponderOffConv` | S / 1 | `tOffPatrolBegin` (S) | `$transponderOffConv = true 0` — `tOffPatrolBegin` |
| `$trapped` | CS / 3 | `pods_breakOption` (C) | `impl/campaign/CargoPodsEntityPlugin.java`, `impl/campaign/rulecmd/salvage/CargoPods.java` (more mentions) |
| `$triTachSystem` | O / 1 | `TTMACommandOptions1` (O) | `impl/campaign/rulecmd/HA_CMD.java` |
| `$triedBribe` | S / 1 | `lppHesperusExcubitorBribe` (S) | `$triedBribe = true` — `lppHesperusExcubitorBribe` |
| `$triedSabotage` | CS / 1 | `zig_exploreSabotage` (C) | `$triedSabotage = true` — `zig_exploreSabotage` |
| `$trust` | CS / 40 | `sdtuHyderTalk3judgment1` (C) | `$trust++` — `SDTUHyderTalkStartHeg` |
| `$ttBlackSite` | C / 4 | `zig_beaconOpenDialog` (C) | `impl/campaign/world/TTBlackSite.java` |
| `$ttBriberyContact` | CS / 2 | `ttContactBribeRep` (C) | `$ttBriberyContact = true` — `ttContactBribeRepIntroFirstTime` |
| `$ttProblemsAsked` | CS / 2 | `ttArroyoCounterRaiding` (C) | `$ttProblemsAsked = true 90` — `ttArroyoProblemsSel0` |
| `$ttWeaponsCache` | C / 1 | `zig_cacheOpenDialog` (C) | `impl/campaign/intel/AnalyzeEntityIntelCreator.java`, `impl/campaign/procgen/themes/SalvageSpecialAssigner.java` (more mentions) |
| `$ttli_bountyHunter` | C / 1 | `ttli_bountyHunterHail` (C) | `impl/campaign/intel/bar/events/TriTachLoanIncentiveScript.java` |
| `$ttli_daysRemaining` | C / 2 | `ttli_greetingLater` (C) | `impl/campaign/intel/bar/events/TriTachLoanIntel.java` |
| `$ttli_eventRef` | CS / 5 | `ttli_disablePayOpt` (C) | `impl/campaign/intel/bar/events/TriTachLoanIntel.java` |
| `$ttli_extensionDays` | ST / 2 | `ttli_extendLoanText` (T) | `impl/campaign/intel/bar/events/TriTachLoanIntel.java` |
| `$ttli_isMajorLoan` | C / 5 | `ttli_greetingSoonMajor` (C) | `impl/campaign/intel/bar/events/TriTachLoanIntel.java` |
| `$ttli_isPlayerContact` | C / 1 | `ttli_greetingMain` (C) | `impl/campaign/intel/bar/events/TriTachLoanIntel.java` |
| `$ttli_loanWasExtended` | C / 1 | `ttli_extendLoanOpt` (C) | `impl/campaign/intel/bar/events/TriTachLoanIntel.java` |
| `$ttli_repaymentAmount` | OST / 4 | `ttli_greetingLater` (T) | `impl/campaign/intel/bar/events/TriTachLoanIntel.java` |
| `$ttma_commandHailedPlayer` | CS / 1 | `ttma_commandHail` (C) | `$ttma_commandHailedPlayer = true 7` — `ttma_commandHail` |
| `$ttwi_encounteredAlready` | CS / 1 | `ttwiEncounter` (C) | `$ttwi_encounteredAlready = true` — `ttwiEncounter` |
| `$ttwi_wantsItem` | C / 1 | `ttwiEncounter` (C) | `impl/campaign/missions/academy/GABuyArtifact.java` |
| `$turnOffAutoInterceptScript` | S / 1 | `warnAttackEncounter2` (S) | `$turnOffAutoInterceptScript = true` — `warnAttackEncounter2` |
| `$tut_dataContact` | C / 1 | `tut_dataContactStart` (C) | `impl/campaign/tutorial/TutorialMissionEvent.java`, `impl/campaign/tutorial/TutorialMissionIntel.java` |
| `$tut_eventRef` | S / 10 | `tut_mainGetDataAccept` (S) | `impl/campaign/tutorial/TutorialMissionEvent.java`, `impl/campaign/tutorial/TutorialMissionIntel.java` |
| `$tut_jangalaContact` | C / 1 | `tut_janContactReport` (C) | `impl/campaign/tutorial/TutorialMissionEvent.java`, `impl/campaign/tutorial/TutorialMissionIntel.java` |
| `$tut_mainContact` | C / 6 | `tut_mainContactBegin` (C) | `impl/campaign/tutorial/TutorialMissionEvent.java`, `impl/campaign/tutorial/TutorialMissionIntel.java` |
| `$tutorialUseStoryPoint` | C / 1 | `sal_tutorial_storyPointUse1` (C) | Trace owning rule/command or generated interaction data |
| `$ucb_barEvent` | C / 1 | `ucbShowBountyBar` (C) | Trace owning rule/command or generated interaction data |
| `$ucb_difficulty` | S / 3 | `ucbLowSel` (S) | `$ucb_difficulty = LOW 0` — `ucbLowSel` |
| `$ucb_manOrWoman` | OT / 2 | `ucbBlurbBar` (T) | Trace owning rule/command or generated interaction data |
| `$ucb_ref` | S / 4 | `ucbShowBounty` (S) | Trace owning rule/command or generated interaction data |
| `$usable` | CS / 5 | `cTap_infoText` (C) | `$usable = true` — `cryo_infoText` |
| `$visited` | CS / 6 | `abyssalGasGiantTurbulenceRevisit` (C) | `$visited = true` — `abyssalGasGiantTurbulence` |
| `$visitedA` | CS / 5 | `gaFCFikenhildVisit_stageA` (C) | `$visitedA = true` — `gaFCFikenhildAdonya1` |
| `$visitedB` | CS / 5 | `gaFCFikenhildVisit_stageB` (C) | `$visitedB = true` — `gaFCFikenhildBione1` |
| `$visitedC` | CS / 7 | `gaFCFikenhildOption` (C) | `$visitedC = true` — `gaFCFikenhildCavinStart` |
| `$voice` | CS / 66 | `convDefaultGreetingSoldier` (C) | `$voice = faithful` — `LKEmazalotPortmasterSetLuddic` |
| `$waHailed` | CS / 2 | `warnAttackEncounter1` (C) | `$waHailed = true 1` — `warnAttackEncounter1` |
| `$waIsHailing` | S / 2 | `warnAttackEncounter1` (S) | `$waIsHailing = true 0` — `warnAttackEncounter1` |
| `$warnAttack` | C / 2 | `warnAttackEncounter1` (C) | `impl/campaign/missions/hub/HubMissionWithTriggers.java` |
| `$wasAnnoying` | CS / 6 | `lppJangalaattendantVisitOutsideC` (C) | `$wasAnnoying = true 1` — `lppJangalaAttendantFoolish` |
| `$wasCalledLiar` | CS / 3 | `PKHackStoryOptB` (C) | `$wasCalledLiar = true 0` — `PKHackStoryStartD` |
| `$wasNice` | CS / 5 | `LKEvirensRaidJethroLie2b1` (C) | `$wasNice = 0` — `lkeVirensRaidFinished2` |
| `$wasOffended` | CS / 6 | `ImoinuKatoUmbraSupported` (C) | `$wasOffended = true` — `sdtuUmbraTrickVIPOpt2imoinu` |
| `$waterThen` | CS / 2 | `lppEnding5blindOpt3` (C) | `$waterThen = true 0` — `lppEnding4b` |
| `$weary` | CS / 3 | `rh_continueToGoodDealSelWeary` (C) | `$weary = true 0` — `rh_defeatedSDF` |
| `$whatDidHeSeeInYou` | S / 1 | `gaFCZalLookingForScylla` (S) | `$whatDidHeSeeInYou = true 0` — `gaFCZalLookingForScylla` |
| `$whoWorkFor` | CS / 2 | `gaFCIsirahMercHubOption3` (C) | `$whoWorkFor = true` — `gaFCIsirahMercWhoWorkFor` |
| `$whyAuthorities` | CS / 8 | `anhCantinaAskTransport` (C) | `$whyAuthorities = true 1` — `anhCantinaAuthorities` |
| `$whyGate` | CS / 3 | `gaATGscavScanHub31` (C) | `$whyGate = true 0` — `gaATGscavScanDialog31` |
| `$whyLingering` | CS / 3 | `gaATGscavScanHub3` (C) | `$whyLingering = true 0` — `gaATGscavScanDialog3` |
| `$willGiveNanoforge` | CS / 4 | `rh_giveNanoforge` (C) | `$willGiveNanoforge = false` — `rh_betterDealSel` |
| `$willPayHouseHannan` | CS / 4 | `rh_handleHannanPaymentDetails` (C) | `$willPayHouseHannan = false` — `rh_betterDealSel` |
| `$willingToLeave` | CS / 5 | `gaATGscavScanHub1` (C) | `$willingToLeave = true 0` — `gaATGscavScanDialog1` |
| `$wontAttack` | CS / 8 | `lkePatherFleetRespC` (C) | `$wontAttack = true 0` — `lkePatherFleetStartCotton` |
| `$wontTalkLKE` | S / 3 | `LKEmazDKYaribayEnd` (S) | `$wontTalkLKE = true` — `LKEmazDKYaribayEnd` |
| `$youAreOmega` | CS / 4 | `PKHackStoryOptOmega2` (C) | `$youAreOmega = true 0` — `PKHackStoryYouAreOmega` |
| `$youDoTheScan` | CS / 3 | `gaATGhegFleetDialog3` (C) | `$youDoTheScan = true` — `gaATGhegFleetCom6` |
| `$youPoured` | CS / 2 | `LKEvirensRaidJethro3c` (C) | `$youPoured = true 0` — `lkeVirensRaidTeaA` |
| `$zalCommentJanus` | CS / 1 | `gaATGMagecGateOptJanus1` (C) | `$zalCommentJanus = true 0` — `gaATGMagecGateOptJanus1` |
| `$zalCommentScan` | CS / 1 | `gaATGMagecGateOptZalScan1` (C) | `$zalCommentScan = true 0` — `gaATGMagecGateOptZalScan1` |
| `$zalScanCommentScan` | C / 1 | `gaATGMagecGateOptZalScan2` (C) | Trace owning rule/command or generated interaction data |
| `$ziggurat` | C / 6 | `zig_encounterDescScanned` (C) | `impl/campaign/missions/academy/GAProjectZiggurat.java`, `impl/campaign/world/TTBlackSite.java` |
| `$zigguratShipName` | T / 2 | `gaPZHegemonyEncounterStandard` (T) | `impl/campaign/rulecmd/salvage/ZigguratCMD.java` |
