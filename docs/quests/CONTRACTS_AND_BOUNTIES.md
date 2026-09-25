# Contracts and bounties

The repeatable Kesteven contracts, the four named bounty fleets and the intercept fleets of quest `ic`, including the Kesteven debt collector. The bounties are one-off fleets with an intel entry and a reward, not quests with stages. Java paths are relative to `jars/src/lostsector/campaign/`; `dialogue/rules/` and `combat/` paths are relative to `jars/src/lostsector/`.

## Contracts

| Owner | Role |
|---|---|
| `data/campaign/person_missions.csv` | Offers mission `nskr_contracts` (plugin `lostsector.campaign.kesteven.contracts.ContractsMission`) to people tagged `Contracts` (Jack and Alice, set in `world/SectorGen`). The mission id is the prefix of the rules triggers `nskr_contracts_blurb` and `nskr_contracts_option` and the `$missionId` that vanilla's `contact_accept` row passes to the mission hub. |
| `kesteven/contracts/ContractsMission` | `BaseHubMission`: holds the pending offer, writes its text tokens and ends as a success when the offer is accepted |
| `kesteven/contracts/ContractInfo` | One contract: type, subtype, count, reward, progress, failed flag |
| `kesteven/contracts/ContractManager` | `EFS_LIST` script and listener: progress, failure and offer reset |
| `kesteven/contracts/ContractIntel` | The accepted contract; pays on completion |
| `rules.csv` `# CONTRACTS` block | The offer: blurb rows on `nskr_contracts_blurb`, the option row on `nskr_contracts_option`, the `nskr_contracts_start` handler and the offer paragraphs on `nskr_contractsOfferText` |

**Offers.** One pending offer per type is saved as a `ContractInfo` in persistent data: `nskr_contractsEliminate` and `nskr_contractsRecovery`. The `ContractsMission` constructor creates missing offers. Jack offers elimination; anyone else (Alice) offers data recovery. `create()` refuses when the player already has an accepted contract of that type (one of each).

**Offer text.** The mission hub calls `updateInteractionData` right after it creates each offered mission, every time the contact's mission list is prepared. `ContractsMission.updateInteractionDataImpl` then writes these keys to the contact's memory with expiry 0:

| Key | Value |
|---|---|
| `$nskr_contracts_type` | `ELIMINATE` or `SCAVENGE`; selects the rows |
| `$nskr_contracts_factionBounty` | True for a faction subtype; the elimination text then omits "hostile" |
| `$nskr_contracts_count` | Target count |
| `$nskr_contracts_targets` | `ContractManager.getTypeString`; for data recovery prefixed with the unit words of `getUnitsString` ("units of metals", "beta cores") |
| `$nskr_contracts_rewardPer`, `$nskr_contracts_rewardTotal` | Payout per target vessel or recovered unit and the total, formatted with `Misc.getDGSCredits` |

The blurb rows `nskr_contracts_blurbElimination` and `nskr_contracts_blurbRecovery` match on the type. The start row sets `$missionId = nskr_contracts`, fires `FireAll nskr_contractsOfferText` for the offer paragraphs (`nskr_contracts_elimination1` to `4`, `nskr_contracts_recovery1` to `5`, one paragraph per row with its highlights), binds Escape to Decline with `SetShortcut contact_decline ESCAPE false`, and offers vanilla's `contact_accept` and `contact_decline`. The accepted contract's intel text is still written in `ContractIntel`.

**Types.** `ContractInfo.randomSubType()` picks from the base weight lists and adds the optional-mod lists only while `ModPlugin.IS_TAHLAN` or `IS_INDEVO` is set.

| Type | Subtypes | Progress |
|---|---|---|
| Elimination | Hull size or role (standard, frigate, destroyer, cruiser, capital, phase, logistics, carrier), or a faction: Luddic Path, pirates, Remnants, Enigma, and Legio Infernalis when Tahlan is active | `ContractManager.reportPlayerEngagement` counts matching destroyed enemy ships |
| Data recovery | Commodities: metals, supplies, fuel, heavy machinery, Artifact Electronics (`nskr_electronics`), AI cores; IndEvo parts and Tahlan cores when those mods are active | `ContractManager.reportEncounterLootGenerated` adds loot stacks whose commodity id equals the subtype, from non-Kesteven losers |

**Accepting.** `accept()` adds `ContractIntel`, stores the contract in sector memory (`$contractManagerContracts`) and clears the offer. It does not call `BaseHubMission.accept()`, so the hub mission never becomes intel. It then moves the mission to its only stage, `ContractsMission.Stage.COMPLETED`, which `create()` registers with `setSuccessStage`. `setCurrentStage` ends it through vanilla's `endSuccess`, which calls `abort()`. `create()` also calls `setNoRepChanges()`, so this success changes no reputation; the null dialog passed to `setCurrentStage` keeps `endSuccess` from printing an end-of-mission update. From then on the contract lives in `ContractInfo` and `ContractIntel`.

**Completion.** When progress reaches the count, `ContractIntel` pays the total reward and raises Kesteven by 2 plus reward/100,000, and the offering person by half that.

**Failure and reset.** Every second (0.1 day), `ContractManager` fails all contracts if the questline has ended or the player's Kesteven relationship is -0.50 or lower. When its reset counter reaches 600 seconds (about 60 days), pending offers are discarded and new ones are created on the next offer.

### Defects

- `ContractsMission` writes `$nskr_contracts_ref2`, which no row reads.
- `ContractsMission.notifyEnded` is never called, and its `showPerson` action has no caller.

## Named bounties

All four spawners are `EFS_LIST` scripts in `bounties/`. Rewards are paid by `bounties/BountyLoot`, a saved script listening for encounter loot.

| Bounty | Fleet | Commander | Flagship | Location | Reward |
|---|---|---|---|---|---|
| `AbyssSpawner` | "Void Group", Remnant | Lucius | Hollow-class "Piercing Darkness" (`nskr_reverie_boss`), with a Chasm (`nskr_harbinger_boss`) and two Fissures (`nskr_afflictor_boss`) | Orbiting a body in a Remnant-themed red giant system, or any red giant | 1 Alpha Core; the Anti-Remnant Organization pays 600,000 credits if the player's fleet holds none of the bounty ships when the loot is generated |
| `EternitySpawner` | "Commander Umbra's Fleet", Enigma | Umbra | Eternity-class "DSRD Shadows Of Tomorrow" (`nskr_eternity_e_boss`) | Nebula system without a Remnant theme | 2 Alpha Cores and 500 Artifact Electronics |
| `RorqualSpawner` | "Peacekeepers", mercenary fleet shown as Independent | Alistair Walsh | Rorqual-class "ISS White Whale" (`nskr_rorqual_boss`), with a Conquest and two Champions | Patrols Independent markets and switches to another after a counter reaches 30 | 315,000 credits times the player's contribution, as an anonymous "donation" |
| `MothershipSpawner` | "Project Helios Remnant", Remnant | "CREATOR-A3401#" | Sunburst-class "TTDS Helios" (`nskr_sunburst_boss`) | Guards two habitable planets, Helios and Polaris (`nskr_terra1`, `nskr_terra2`), created at new game around a moonless gas giant, preferably in a Remnant system; without any moonless gas giant the bounty is not placed | 1 Alpha Core |

Shared structure (Abyss and Eternity in detail; the others follow the same outline):

1. **Spawn.** Once per campaign, when the spawner's fleet list is empty and its `persistence/Saved` `NewGame` flag is true, the fleet spawns at a location stored under a persistent key: `ABYSS`, `ETERNITY` or `RORQ`; the Mothership uses `nskr_mothershipKey`.
2. **Tracking.** The fleet is kept in a sector-memory list (`$nskr_<name>SpawnerFleets`) and has `MEMORY_KEY_MISSION_IMPORTANT`.
3. **First sighting.** When the fleet is first visible to the player, the intel entry (`AbyssIntel`, `UmbraIntel`, `RorqualIntel` or `MothershipIntel`) is added. A message reads: "Initial examinations of the … fleet shows an unusual flagship, the …-Class. Approach with extreme caution."
4. **Despawn.** Every 4 seconds (every 10 for the Peacekeepers), the spawner removes the fleet once it has no bounty ships left and is out of sensor range.
5. **Reward.** `BountyLoot.reportEncounterLootGenerated` recognises the fleet by its loot key (`$AbyssLoot`, `$EternityLoot`, `$RorqLoot`, `$mothershipLoot`). It adds the reward and sets `$nskr_abyssDefeated`, `$nskr_umbraDefeated`, `$nskr_rorqDefeated` or `$nskr_heliosDefeated`. These are persistent-data flags despite the `$`.
6. **Recovery.** The Abyss and Eternity spawners are `ShipRecoveryListener`s and remove the limited-tooltip tag from recovered bounty ships.

Other pieces:

- **Hints.** `events/hints/HintManager` rolls 4% when the player enters a new system outside the core. A hit adds a `HintIntel` pointing to the Abyss, Eternity, Mothership or Frost system. A hint source is dropped once that bounty's own intel exists.
- **Rules conversations.** Comm rows `abyssDialog`, `eternityDialog`, `pkDialog*` and `mothershipDialog*` hold the fleets' voice. The Mothership comm offers "Try to shut down the AI", which fails.
- **Mothership planets.** `CorePlugin` routes both planets to `MothershipInteractionBlocker` until the fleet has been beaten. The Mothership's fleet-interaction config records `nskr_mothershipKeySpawnedWreck` when its flagship is gone. `nskr_mothershipKeyCompleted` marks the bounty done.
- **Peacekeepers.** The Rorqual flagship is the bounty (`RorqualSpawner.hasRorqual`, which tracks the ship `SimpleFleet` created as flagship). `BountyLoot` pays when the Rorqual is gone after a battle the player won, even if escorts survive. Once it is gone, the spawner also clears the loot flag at its next daily check and the fleet despawns out of sensor range; if someone else destroyed it, `RorqualIntel` reports the chance missed. `events/DerelictTeaserSpawner` places a Rorqual derelict as a teaser.
- **ARO strike group.** Quest `ic` can send an ARO strike group after a player whose fleet contains Abyss bounty ships; see [Intercept fleets](#intercept-fleets).

None of the bounties has stages, dialogue choices that change state, or a failure path beyond another party killing the Peacekeepers.

## Intercept fleets

Record quest `ic` (`events/intercepts/InterceptsQuest`) sends four fleets after the player in hyperspace. It has one stage, `RUNNING`, no flags, and one [`InterceptEncounter`](../../jars/src/lostsector/quest/README.md#interceptencounter) module per fleet, plus a [`PayOffEncounter`](../../jars/src/lostsector/quest/README.md#payoffencounter) for the debt collector's demand. Each record in `InterceptsState.intercepts` counts its spawns; all four encounters are one-shot, so a record with a spawn never rolls again. `InterceptsState.payOffs` counts the collector's payments.

| Record and role | Daily roll, once per campaign | Fleet | Orders |
|---|---|---|---|
| `aro` | 1% while the player is in hyperspace within 25,000 of the center and carries Abyss bounty ships (`AbyssSpawner.hasBountyShips`) | "ARO Strike Group": Luddic Church doctrine, 110 to 130 points, flies as mercenaries, hostile, no reputation impact | Intercept around the player; withdraws when beaten or after 45 days |
| `messenger`, then `messengerLeaving` | 4% while `KestevenQuest.inMessengerWindow()` (stages 10 to 14) and the player is in hyperspace within 25,000 of the center | "Merc Messenger": pirate doctrine, 50 to 70 points, flies as mercenaries | Intercepts the player directly. Once the player has opened its comm link, it leaves for a random pirate market and despawns there. Withdraws when beaten or after 20 days, in either role |
| `collector`, then `collectorLeaving` | 1% while Kesteven has markets (`SectorLookup.kestevenExists`), the player is in hyperspace within 25,000 of the center, Kesteven's relationship with the player is -0.50 or lower, and the Kesteven debt (`nskr_debt.getDebt()`) is at least 250,000 credits | "Debt Collector": Kesteven, 100 to 110 points, ships up to size 3, 2 to 3 S-mods | Intercepts around the player. Once paid (action `collectorLeave`), or on the first day Kesteven's relationship with the player is above -0.50, it ignores other fleets (`MemFlags.FLEET_IGNORES_OTHER_FLEETS`) and leaves for a random Kesteven market, despawning there. Withdraws when beaten or after 45 days, in either role |
| `autoHunter`, then `autoHunterGuard` | 1% while the player is in hyperspace within 50,000 of the center, has at least 75 deployment points of automated ships (`automated` or SotF's `sotf_sierrasconcord`), and the player faction's relationship with the Luddic Path is below 0 | "Hunter Fanatics": Luddic Path, 70 to 80 points, half the ships and the flagship with `nskr_machineSpirit` | Intercepts around the player for 30 days, then orbits a random Luddic Path market, intercepting the player with a 1% chance per order tick while it sees them; withdraws when beaten |

Point budgets scale with `PowerLevel` and `Difficulty.scriptedFleetMult()`. A fleet spawns at the edge of the player's sensor range. A withdrawing fleet gets no more orders and despawns once it is farther from the player than the maximum hyperspace sensor range; "beaten" means below a quarter of its spawn fleet points.

Rules rows are in the `# INTERCEPTS` block. The ARO group and the messenger hail the player on `BeginFleetEncounter`; their comm links (`OpenCommLink`) show a speech, set `$entity.ignorePlayerCommRequests` for 100 days and offer "Cut the comm link". The messenger's speech is the shared insert `nskr_icMessengerMessage`, reached from both of its roles; its script runs `nskr_quest ic do messengerMet`, which calls `KestevenQuest.reportMessengerMet()` and switches the fleet to `messengerLeaving`. The Auto-Hunter has no rows and uses the vanilla Luddic Path encounter.

### Debt collector

The collector's rows follow the messenger's in `# INTERCEPTS`. `nskr_ic_collectorHail` hails the player on `BeginFleetEncounter`. The comm link row `nskr_ic_collectorOpen` needs a hostile fleet with the `collector` role, no `$ignorePlayerCommRequests` on the speaker, and `nskr_quest ic check collectorDemands` (Kesteven's relationship with the player is -0.50 or lower). Its Continue handler sets `$entity.ignorePlayerCommRequests` for 10 days, runs `nskr_debt init`, fires the `FireBest` pick `nskr_icCollectorDemand`, then shows "You have … in unpaid loans." The pick:

| Row | Condition | Options |
|---|---|---|
| `nskr_ic_collectorDemandAll` | `collectorCanPayAll`: the player's credits cover the debt | Pay the whole debt, or refuse |
| `nskr_ic_collectorDemandSome` | `collectorCanPaySome`: the credits fall short of the debt but are at least 100,000 | Pay all credits, or refuse |
| `nskr_ic_collectorDemandNone` | Fallback: less than the debt and less than 100,000 credits | Admit it, which leads to the fight exit |

Both pay handlers fire the shared insert `nskr_icCollectorPay`: `nskr_quest ic do collectorPay` takes the credits with the vanilla receipt and reduces the debt by the same amount, `nskr_debt init` and a small line show the new debt, `AdjustRep kesteven 5` and `AdjustRepActivePerson COOPERATIVE 10` raise Kesteven by 5 (no limit) and the captain by 10 (up to Cooperative), `ui_rep_raise` plays, `$entity.ignorePlayerCommRequests` is removed, and `nskr_quest ic do collectorLeave` sends the fleet home. The handlers then make the fleet allow disengaging and non-aggressive, and offer Leave. Refusing and admitting lead to `nskr_ic_collectorFight`, which ends the conversation; the fleet stays hostile. The payment options show `$nskr_ic_collectorPayment`, the amount the payment takes.

#### Defects

- The "cannot pay" reply (`nskr_ic_collectorNoPay`) prints the collector's "other means of payment" line, and its Cut the comm link option (`nskr_ic_collectorFight`) prints the same line again.
- A player who repays the whole debt at a Kesteven market before the collector catches them still meets the demand: with a debt of 0, the "pay all" option offers a payment of 0 credits, which takes nothing and still raises both relationships.
