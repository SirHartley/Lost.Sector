# Contracts and bounties

The repeatable Kesteven contracts and the four named bounty fleets. The bounties are one-off fleets with an intel entry and a reward, not quests with stages. Java paths are relative to `jars/src/lostsector/campaign/`; `dialogue/rules/` and `combat/` paths are relative to `jars/src/lostsector/`.

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
- **ARO strike group.** `events/InterceptManager` can spawn an ARO strike group when the player's fleet contains Abyss bounty ships (`AbyssSpawner.hasBountyShips`).

None of the bounties has stages, dialogue choices that change state, or a failure path beyond another party killing the Peacekeepers.
