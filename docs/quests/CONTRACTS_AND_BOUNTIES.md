# Contracts and bounties

The repeatable Kesteven contracts, the four named bounty fleets of quest `bounty` and the intercept fleets of quest `ic`, including the Kesteven debt collector. The bounties are one-off fleets with an intel entry and a reward, not quests with stages. Java paths are relative to `jars/src/lostsector/campaign/`; `dialogue/rules/` and `combat/` paths are relative to `jars/src/lostsector/`.

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

The four named bounties are records of the record quest `bounty` (`bounties/BountiesQuest`), one [`BountyEncounter`](../../jars/src/lostsector/quest/README.md#bountyencounter) module each.

| Record | Fleet | Commander | Flagship | Location | Reward |
|---|---|---|---|---|---|
| `abyss` | "Void Group" and a Greek letter, Remnant, 135 to 145 points | Lucius | Hollow-class "Piercing Darkness" (`nskr_reverie_boss`), with a Chasm (`nskr_harbinger_boss`) and two Fissures (`nskr_afflictor_boss`) | Orbiting a body in a Remnant-themed red giant system, or any procgen red giant, or any non-core system | 1 Alpha Core in the loot; the Anti-Remnant Organization pays 600,000 credits if the player's fleet holds none of the bounty ships when the loot is generated |
| `eternity` | "Commander Umbra's Fleet", Enigma, 80 to 85 points | Umbra | Eternity-class "DSRD Shadows Of Tomorrow" (`nskr_eternity_e_boss`) | Orbiting a body in a procgen nebula system without a Remnant theme, or any procgen nebula, or any non-core system | 2 Alpha Cores and 500 Artifact Electronics in the loot |
| `mothership` | "Project Helios Remnant", Remnant, 155 to 160 points, no ship recovery | "CREATOR-A3401#" | Sunburst-class "TTDS Helios" (`nskr_sunburst_boss`) | Orbiting the moonless gas giant of Helios and Polaris (`nskr_terra1`, `nskr_terra2`), which world generation places, preferably in a Remnant system; without any moonless gas giant the bounty is not placed | 1 Alpha Core in the loot, and the TTDS Helios as a wreck |
| `peacekeepers` | "Peacekeepers", 180 to 190 points, built as mercenaries and flying as Independents | Alistair Walsh | Rorqual-class "ISS White Whale" (`nskr_rorqual_boss`), with a Conquest and two Champions | Patrols a random Independent market's system and moves to another after 30 days there | 315,000 credits times the player's contribution, as an anonymous "donation"; Independent relations drop by 10 |

Point budgets are multiplied by `Difficulty.scriptedFleetMult()`. Builders, location pickers and the Peacekeepers' reinforcement are in `bounties/BountiesFleets`.

### Quest `bounty`

One stage, `RUNNING`, no flags. `BountiesState.bounties` holds one `BountyEncounter.Record` per bounty; the record id is also the fleet role, the intel key and the prefix of the bounty's tokens and checks.

1. **Placement.** When the quest state is created at load, each module picks its location with the saved random `location:<id>`, builds the fleet with `fleet:<id>` and spawns it in role `<id>`. A record whose location picker finds nothing stays `NOT_PLACED`.
   - Abyss, Eternity and the Mothership keep the assignment `SimpleFleet` gave them (`FleetOrders.none()`), patrolling or orbiting. They fight to the last, allow disengaging, ignore and are ignored by other fleets, and are `MEMORY_KEY_MISSION_IMPORTANT`.
   - The Mothership's location is `HeliosSite.base()`, the gas giant that world generation picked (`ModPlugin.onNewGameAfterProcGen` calls `HeliosSite.place()`, which stores it in sector persistent data under `nskr_mothershipKey` and adds the planets; `Asteria` avoids that system). Its role carries `MothershipInteractionConfig` as the fleet's interaction config.
   - The Peacekeepers spawn at a random Independent market. They fight to the last, are ignored by other fleets, have low reputation impact and are `MEMORY_KEY_MISSION_IMPORTANT`. A Rorqual other than the flagship becomes a Champion. Their role has `FleetOrders.patrolMarkets(independent, 30 days, "maintaining order")`: they patrol the market's system, ignore other fleets while moving, and after 30 days there move to another random Independent market; at that switch a fleet below 80% of its spawn strength is reinforced with random combat ships and carriers, S-mods and captains until it is back at that strength.
2. **Guarded planets.** While the Mothership is active, Helios and Polaris are claimed with `nskr_bountyGuard`. Opening either shows "The fleet appears to be protecting this planet, and maneuvers to prevent your approach." and starts the fleet encounter (`nskr_quest bounty engage mothership`) when the fleet is in the system; otherwise the dialog closes.
3. **First sighting.** While the player is in the fleet's location, the module checks every unpaused frame whether the fleet is visible to the player's sensors. The first time it is, it shows the intel entry and sends the update `sighted` ("Initial examinations of the … fleet shows an unusual flagship, the …-Class. Approach with extreme caution.", "Approach with caution." for the Peacekeepers). Abyss, Eternity and the Mothership first tell quest `hint` (`HintsQuest.reportBountySighted`), which ends a signal hint for the player's current system, and use their location as map location. The Peacekeepers' map location is the hyperspace anchor of the fleet's current system, none in hyperspace, refreshed daily.
4. **Defeat.** Abyss is beaten when none of its three bounty hulls is left in the fleet; Eternity and the Mothership when `getFlagship()` is null; the Peacekeepers when the ship `SimpleFleet` created as flagship has left the fleet (`FleetHelper.getOriginalFlagship`), even if escorts survive. The module checks at the fleet's loot and after every battle it fought, and counts a destroyed fleet as beaten. The beaten fleet loses `MEMORY_KEY_MISSION_IMPORTANT`, releases the guarded planets and moves to role `<id>Beaten` (`FleetOrders.withdraw()`), so its comm rows stop matching, and it despawns once out of the player's sight.
5. **Reward.** At the loot of the beaten fleet (`onLoot`, after the recovery screen), while the bounty is still active, the module grants the reward once:
   - Abyss: the Alpha Core, then 600,000 credits through `ctx.rewards()` unless the player's fleet holds an Abyss bounty hull.
   - Eternity: the loot items.
   - Mothership: the Alpha Core, and the TTDS Helios wreck next to the player (`BountiesFleets.mothershipWreck`, story point recovery half of the time with the random `mothershipWreck`, no limited tooltip), stored in `BountiesState.mothershipWreck`. When the player leaves the fleet dialog, `MothershipInteractionConfig` moves the dialog to the wreck once; otherwise it closes the dialog.
   - Peacekeepers: 315,000 credits times `computePlayerContribFraction()`, rounded, and, while the player faction's Independent relationship is above -50, a relationship change of -10 through `ctx.rewards().relationship`.
6. **Completion.** On the next unpaused frame after the defeat, the intel entry completes. Its update message shows, for Abyss, "Bounty payment received from ARO, +600,000¢" or "Since you have no proof of complete destruction, you will not receive any payments from ARO."; for the Peacekeepers looted by the player, "Donation received from an anonymous source, +…" and, when the relationship changed, "Relations with the Independents reduced by 10"; otherwise the title only. The Abyss message plays `ui_rep_raise` when paid and `ui_rep_drop` otherwise; the Peacekeepers message plays `ui_rep_raise` when looted by the player. The entry leaves the intel screen after the vanilla delay.
7. **Presentation.** All four entries sort in `TIER_2` while active, post with the major posting sound, show their bullets under the description and offer the delete button once finished, as the old intel classes did. The sighting and completion lines highlight the old phrases in `yellowTextColor` (the flagship class and "extreme caution" or "caution", the payment, "no proof of complete destruction"); "Relations with the Independents reduced by 10" colours "Independents" 150,150,150 (vanilla's Independent faction colour) and "10" `bad`.
8. **Recovery.** A recovered ship of a bounty hull (Abyss: `nskr_reverie_boss`, `nskr_harbinger_boss`, `nskr_afflictor_boss`; Eternity: `nskr_eternity_e`) loses `Tags.SHIP_LIMITED_TOOLTIP`, from any recovery the game reports.

Queries for other features: `BountiesQuest.location(bounty)`, `BountiesQuest.sighted(bounty)` and `BountiesQuest.carriesAbyssShips(fleet)`. Quest `hint` rolls 4% when the player enters a system outside the core for the first time; a hit shows a signal hint pointing to the Abyss, Eternity, Mothership or Frost system, and a bounty's hint source is dropped once it is sighted ([Exploration hints](HINTS.md)). Quest `ic` can send an ARO strike group after a player whose fleet contains Abyss bounty ships; see [Intercept fleets](#intercept-fleets). `events/DerelictTeaserSpawner` places a Rorqual derelict as a teaser.

Rules rows are in the `# BOUNTY QUEST` block inside `# BOUNTIES`:

| Rows | Trigger | Selects by |
|---|---|---|
| `nskr_bounty_<id>Open` | `OpenCommLink` | Role flag `$entity.nskr_bounty_<id>` and the fleet's faction. The Abyss and Mothership rows also need `!$ignorePlayerCommRequests`; the Abyss row sets `$entity.ignorePlayerCommRequests` for 1 day |
| `nskr_bounty_mothershipOpt*` | `nskr_bountyMothershipOptions` (`FireAll`) | "Try to shut down the AI" while the commander has no `$nskr_bounty_mothershipShutdownTried` (100 days); the attempt always fails |
| `nskr_bounty_peacekeepers*` | `DialogOptionSelected` | The Peacekeepers' three options; the cut and praise answers are shown once per 100 days (`$nskr_bounty_peacekeepersCutSelected`, `…PraiseSelected` on the commander), and the challenge makes the fleet hostile |
| `nskr_bounty_mothershipGuard`, `nskr_bounty_guardLeave` | `nskr_bountyGuard` | `nskr_quest bounty check mothershipGuards`; the fallback closes the dialog |
| Titles | `nskr_bountyIntelTitle` | `$nskr_intel_key` |
| Location, reward, sighting and completion lines | `nskr_bountyIntelBullets` | `$nskr_intel_key`, `$nskr_intel_status`, `$nskr_intel_update` (`sighted`), `check peacekeepersInHyperspace` and, for the completion lines, `$nskr_intel_mode == update` with `check abyssPaid`, `peacekeepersLooted` or `peacekeepersRelationsLost` |
| Description paragraphs | `nskr_bountyIntelDesc` | `$nskr_intel_key` and `$nskr_intel_status`; the bullets follow them (`descriptionBullets()`); the Peacekeepers' completed description uses `check peacekeepersLooted` and `peacekeepersPaidInFull` |

Tokens: `$nskr_bounty_<id>FleetName`, `<id>System`, `<id>Entity`, `$nskr_bounty_peacekeepersFleetSystem`, `$nskr_bounty_abyssPayout`, `$nskr_bounty_peacekeepersPayout` and `$nskr_bounty_peacekeepersPaidAmount`.

None of the bounties has dialogue choices that change quest state, or a failure path beyond another party beating a bounty first.

### Defects

- The Eternity comm text shows `captain.getName` literally.
- Eternity and the Mothership count as beaten only when `getFlagship()` is null. Vanilla then returns another member while escorts remain, the case the Peacekeepers fix handled with `FleetHelper.getOriginalFlagship`.
- A bounty beaten without a loot screen (another party's battle, or a player victory with empty loot) completes its intel without the reward; for Abyss the completion then reads "no proof of complete destruction".

## Intercept fleets

Record quest `ic` (`events/intercepts/InterceptsQuest`) sends four fleets after the player in hyperspace. It has one stage, `RUNNING`, no flags, and one [`InterceptEncounter`](../../jars/src/lostsector/quest/README.md#interceptencounter) module per fleet, plus a [`PayOffEncounter`](../../jars/src/lostsector/quest/README.md#payoffencounter) for the debt collector's demand. Each record in `InterceptsState.intercepts` counts its spawns; all four encounters are one-shot, so a record with a spawn never rolls again. `InterceptsState.payOffs` counts the collector's payments.

| Record and role | Daily roll, once per campaign | Fleet | Orders |
|---|---|---|---|
| `aro` | 1% while the player is in hyperspace within 25,000 of the center and carries Abyss bounty ships (`BountiesQuest.carriesAbyssShips`) | "ARO Strike Group": Luddic Church doctrine, 110 to 130 points, flies as mercenaries, hostile, no reputation impact | Intercept around the player; withdraws when beaten or after 45 days |
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
