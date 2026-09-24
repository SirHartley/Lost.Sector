# Vanilla memory and text dictionary

Starsector **0.98a-RC8**. [The authoring guide](../RULES_AUTHORING.md) explains ownership, expiry, custom tokens and code integration. [KEY_USAGE](KEY_USAGE.md) indexes every literal key reference in active vanilla rules; this page explains the reusable sources behind those names.

## Which dictionary to use

| Kind | Where it comes from | How to use it |
|---|---|---|
| Generated text replacement | CoreRuleTokenReplacementGeneratorImpl | Use in Text or a command that performs replacement. Availability depends on interaction context; it need not exist as a memory entry. |
| Automatic fact | CoreCampaignPluginImpl.update*Facts | Read the correct owner's memory in Conditions, Script arguments or Text. Normally refreshed with expiry 0. Do not change the game by overwriting the fact. |
| Engine state/behavior flag | MemFlags and the consuming subsystem | Read or change only through the matching subsystem's contract. Use Java constants and reason-aware helpers where provided. |
| Quest-specific key | That mission's interaction-data preparation and rules | Read only while that quest has prepared it. Do not reuse another quest's stage, reward or target key for a new quest. |
| New mod value | Existing project rule command or mission interaction-data preparation | Namespace it, choose its owner and lifetime, prepare before display. Reuse existing vanilla data when it already represents the same thing. |

## Person and player text

| Tokens | Meaning / availability |
|---|---|
| `$manOrWoman`, `$ManOrWoman` | Current person: man/woman, with the specified capitalization |
| `$heOrShe`, `$HeOrShe` | Current person's subject pronoun |
| `$himOrHer`, `$HimOrHer` | Current person's object pronoun |
| `$hisOrHer`, `$HisOrHer` | Current person's possessive determiner |
| `$himOrHerself`, `$HimOrHerself` | Current person's reflexive pronoun |
| `$brotherOrSister`, `$BrotherOrSister` | Current person's sibling/address form |
| `$sirOrMadam`, `$SirOrMadam` | Current person: sir/ma'am |
| `$personName`, `$PersonName` | Current person's full name; both preserve the name's own capitalization |
| `$personFirstName`, `$personLastName` | Current person's first/last name |
| `$personRank`, `$PersonRank`, `$personPost`, `$PersonPost` | Current person's display rank/post, when supplied |
| `$playerName` and the exact aliases below | Player's name, independent of the active speaker |
| `$playerHeOrShe`, `$PlayerHeOrShe`, `$playerHimOrHer`, `$PlayerHimOrHer`, `$playerHisOrHer`, `$PlayerHisOrHer` | Player pronouns, not quest-giver pronouns |
| `$playerSirOrMadam`, `$PlayerSirOrMadam` | Player's address form; selected honorific can override it |
| `$playerBrotherOrSister`, `$PlayerBrotherOrSister` | Player's address form; generator handles the nonstandard-honorific case |

“Current person” is the target's active person, otherwise its commander for a fleet. No applicable person means those replacements are not supplied by the generator. Vanilla uses its isMale/else branch; do not invent additional capitalization or pronoun variants that are not listed.

`ShowPersonVisual` only draws a card. Use the proper conversation/person-selection path to change who the tokens refer to. When referring to someone else, prepare namespaced strings from that saved person. In a bar mission, BaseHubMission can also put person tokens into interaction memory; check that wrapper's context. Its additional rank/post article keys include `$personRankAOrAn` and `$personPostAOrAn`, which are not universal generator outputs.

### Complete core generator vocabulary

The table contains every literal key inserted by CoreRuleTokenReplacementGeneratorImpl. Expressions are source values, not executable rules syntax; alternatives come from different branches. Locals such as `factionName`, `last`, `honorific` and `shipOrFleet` are derived inside that generator. This is an exact-name dictionary, not a promise that every row is available in every interaction.

- Market tokens require the resolved market; fleets can use their source market.
- Target/entity/custom-name tokens require the corresponding target/spec context.
- Faction tokens refer to the target's faction; personFaction tokens refer to the person's faction. Names containing “marketFaction” in this generator are aliases, not a separate market lookup.
- Player ship/fleet wording depends on player fleet composition. For target wording, inspect the otherShipOrFleet branch rather than assuming it mirrors the player's branch perfectly.
- A missing player surname falls back to the first name in the generator's `last` variable.

| Exact token | Value expression(s) in generator |
|---|---|
| `$BrotherOrSister` | `"Brother"`; `"Sister"` |
| `$Faction` | `Misc.ucFirst(factionName)` |
| `$FactionEntityPrefix` | `Misc.ucFirst(target.getFaction().getEntityNamePrefix())` |
| `$FactionLong` | `Misc.ucFirst(target.getFaction().getDisplayNameLong())` |
| `$FleetOrShip` | `Misc.ucFirst(shipOrFleet)` |
| `$HeOrShe` | `"He"`; `"She"` |
| `$HimOrHer` | `"Him"`; `"Her"` |
| `$HimOrHerself` | `"Himself"`; `"Herself"` |
| `$HisOrHer` | `"His"`; `"Her"` |
| `$ManOrWoman` | `"Man"`; `"Woman"` |
| `$MarketFaction` | `Misc.ucFirst(factionName)` |
| `$MarketName` | `market.getName()` |
| `$OwnerFaction` | `Misc.ucFirst(factionName)` |
| `$PersonFaction` | `Misc.ucFirst(factionName)` |
| `$PersonFactionLong` | `Misc.ucFirst(personFaction.getDisplayNameLong())` |
| `$PersonName` | `person.getName().getFullName()` |
| `$PersonPost` | `Misc.ucFirst(person.getPost())` |
| `$PersonRank` | `Misc.ucFirst(person.getRank())` |
| `$PlayerBrotherOrSister` | `"Brother"`; `"Sister"`; `"Walker"` |
| `$PlayerHeOrShe` | `"He"`; `"She"` |
| `$PlayerHimOrHer` | `"Him"`; `"Her"` |
| `$PlayerHisOrHer` | `"His"`; `"Her"` |
| `$PlayerName` | `Global.getSector().getCharacterData().getName()` |
| `$PlayerSirOrMadam` | `"Sir"`; `"Ma'am"`; `honorific` |
| `$Playername` | `Global.getSector().getCharacterData().getName()` |
| `$ShipOrFleet` | `Misc.ucFirst(shipOrFleet)` |
| `$SirOrMadam` | `"Sir"`; `"Ma'am"` |
| `$TheFaction` | `Misc.ucFirst(target.getFaction().getDisplayNameWithArticle())` |
| `$TheFactionLong` | `Misc.ucFirst(target.getFaction().getDisplayNameLongWithArticle())` |
| `$TheMarketFaction` | `Misc.ucFirst(target.getFaction().getDisplayNameWithArticle())` |
| `$TheOwnerFaction` | `Misc.ucFirst(target.getFaction().getDisplayNameWithArticle())` |
| `$ThePersonFaction` | `Misc.ucFirst(personFaction.getDisplayNameWithArticle())` |
| `$ThePersonFactionLong` | `Misc.ucFirst(personFaction.getDisplayNameLongWithArticle())` |
| `$aOrAn` | `spec.getAOrAn()` |
| `$brotherOrSister` | `"brother"`; `"sister"` |
| `$entityName` | `target.getName()` |
| `$faction` | `factionName` |
| `$factionAOrAn` | `target.getFaction().getPersonNamePrefixAOrAn()` |
| `$factionEntityPrefix` | `target.getFaction().getEntityNamePrefix()` |
| `$factionIsOrAre` | `target.getFaction().getDisplayNameIsOrAre()` |
| `$factionLong` | `target.getFaction().getDisplayNameLong()` |
| `$fleetName` | `target.getName().toLowerCase()` |
| `$fleetOrShip` | `shipOrFleet` |
| `$heOrShe` | `"he"`; `"she"` |
| `$himOrHer` | `"him"`; `"her"` |
| `$himOrHerself` | `"himself"`; `"herself"` |
| `$hisOrHer` | `"his"`; `"her"` |
| `$isOrAre` | `spec.getIsOrAre()` |
| `$manOrWoman` | `"man"`; `"woman"` |
| `$market` | `market.getName()` |
| `$marketFaction` | `factionName` |
| `$marketName` | `market.getName()` |
| `$marketSystem` | `((StarSystemAPI)target.getLocation()).getBaseName() + " star system"`; `"hyperspace"` |
| `$nameInText` | `spec.getNameInText()` |
| `$otherFleetName` | `fleet.getName().toLowerCase()` |
| `$otherShipOrFleet` | `otherShipOrFleet` |
| `$ownerFaction` | `factionName` |
| `$personFaction` | `factionName` |
| `$personFactionIsOrAre` | `personFaction.getDisplayNameIsOrAre()` |
| `$personFactionLong` | `personFaction.getDisplayNameLong()` |
| `$personFirstName` | `person.getName().getFirst()` |
| `$personLastName` | `person.getName().getLast()` |
| `$personName` | `person.getName().getFullName()` |
| `$personPost` | `person.getPost().toLowerCase()` |
| `$personRank` | `person.getRank().toLowerCase()` |
| `$playerBrotherOrSister` | `"brother"`; `"sister"`; `"walker"` |
| `$playerFirstName` | `Global.getSector().getCharacterData().getPerson().getName().getFirst()` |
| `$playerFirstname` | `Global.getSector().getCharacterData().getPerson().getName().getFirst()` |
| `$playerHeOrShe` | `"he"`; `"she"` |
| `$playerHimOrHer` | `"him"`; `"her"` |
| `$playerHisOrHer` | `"his"`; `"her"` |
| `$playerHostileTimeoutStr` | `days.toLowerCase()` |
| `$playerLastName` | `last` |
| `$playerLastname` | `last` |
| `$playerName` | `Global.getSector().getCharacterData().getName()` |
| `$playerSirOrMadam` | `"sir"`; `"ma'am"`; `Misc.lcFirst(honorific)` |
| `$playername` | `Global.getSector().getCharacterData().getName()` |
| `$relAdjective` | `level.getDisplayName().toLowerCase()` |
| `$relayName` | `target.getName()` |
| `$shipOrFleet` | `shipOrFleet` |
| `$shortName` | `spec.getShortName()` |
| `$sirOrMadam` | `"sir"`; `"ma'am"` |
| `$theFaction` | `target.getFaction().getDisplayNameWithArticle()` |
| `$theFactionLong` | `target.getFaction().getDisplayNameLongWithArticle()` |
| `$theMarketFaction` | `target.getFaction().getDisplayNameWithArticle()` |
| `$theOwnerFaction` | `target.getFaction().getDisplayNameWithArticle()` |
| `$thePersonFaction` | `personFaction.getDisplayNameWithArticle()` |
| `$thePersonFactionLong` | `personFaction.getDisplayNameLongWithArticle()` |

## Automatic facts

Source: `com/fs/starfarer/api/impl/campaign/CoreCampaignPluginImpl.java`, the named `update*Facts` methods. All entries below are literal-key assignments using zero expiry; they are refreshed facts, not permanent campaign state. Entries under conditional branches are only present when the condition is met. The source expression records the value actually assigned and avoids guessing a meaning from the key's name.

Owner is not necessarily a rules prefix: Entity is normally local until a person takes over; Person is normally local during their conversation; Player is character-data memory, not player-fleet memory. Market/faction/global and optional sourceMarket/personFaction scopes are explained in the authoring guide.

Important distinctions:

- `$player.credits` is numeric; `$player.creditsStr` is grouped display text; `$player.creditsStrC` also includes the credits suffix. Use the numeric value for comparisons and payment, not formatted strings.
- Person `$rel` is numeric while faction `$rel` is an enum-name String. Prefer `$relValue` when a numeric comparison is intended and `$relName` when matching a named relationship level.
- Entity `$relativeStrength` is an encounter decision category (-1/0/1), not a fleet-strength ratio. `$fleetPoints` is a different fact.
- `$hasMarket` and `$isPerson` are memory facts. They are not evidence of plugins named HasMarket or HasPerson.
- Player commodity facts describe cargo totals, not special-item data.

| Fact updater / owner | Key on that owner | Source value expression |
|---|---|---|
| Entity | `$abyssalDepth` | `Misc.getAbyssalDepth(entity)` |
| Entity | `$abyssalDepthUncapped` | `Misc.getAbyssalDepth(entity, true)` |
| Entity | `$onOrAt` | `onOrAt` |
| Entity | `$systemCutOffFromHyper` | `true` |
| Entity | `$locationId` | `entity.getContainingLocation().getId()` |
| Entity | `$id` | `entity.getId()` |
| Entity | `$transponderOn` | `entity.isTransponderOn()` |
| Entity | `$name` | `entity.getName()` |
| Entity | `$fullName` | `entity.getFullName()` |
| Entity | `$inHyperspace` | `entity.isInHyperspace()` |
| Entity | `$customType` | `entity.getCustomEntityType()` |
| Entity | `$nameInText` | `spec.getNameInText()` |
| Entity | `$shortName` | `spec.getShortName()` |
| Entity | `$isOrAre` | `spec.getIsOrAre()` |
| Entity | `$aOrAn` | `spec.getAOrAn()` |
| Entity | `$terrainId` | `plugin.getSpec().getId()` |
| Entity | `$relativeStrength` | `-1` |
| Entity | `$relativeStrength` | `1` |
| Entity | `$relativeStrength` | `0` |
| Entity | `$weakerThanPlayerButHolding` | `true` |
| Entity | `$relativeStrength` | `-1` |
| Entity | `$isHostile` | `ai.isHostileTo(playerFleet)` |
| Entity | `$fleetPoints` | `fleet.getFleetPoints()` |
| Entity | `$isStation` | `fleet.isStationMode()` |
| Entity | `$supplies` | `fleet.getCargo().getSupplies()` |
| Entity | `$fuel` | `fleet.getCargo().getFuel()` |
| Entity | `$knowsWhoPlayerIs` | `fleet.knowsWhoPlayerIs()` |
| Entity | `$isHostile` | `false` |
| Entity | `$isHostile` | `true` |
| Entity | `$hasMarket` | `true` |
| Entity | `$hasStation` | `true` |
| Entity | `$marketSize` | `market.getSize()` |
| Entity | `$stability` | `(int) market.getStabilityValue()` |
| Entity | `$planetType` | `planet.getTypeId()` |
| Market | `$id` | `market.getId()` |
| Market | `$size` | `market.getSize()` |
| Market | `$stability` | `(int) market.getStabilityValue()` |
| Market | `$isSurveyed` | `market.getSurveyLevel() == SurveyLevel.FULL` |
| Market | `$surveyLevel` | `market.getSurveyLevel().name()` |
| Market | `$isPlanetConditionMarketOnly` | `market.isPlanetConditionMarketOnly()` |
| Market | `$daysExisted` | `daysExisted` |
| Market | `$isHidden` | `market.isHidden()` |
| Market | `$isPlayerOwned` | `market.isPlayerOwned()` |
| Market | `$hasRuins` | `true` |
| Market | `$hasUnexploredRuins` | `Misc.hasUnexploredRuins(market)` |
| Person | `$id` | `person.getId()` |
| Person | `$relValue` | `rel` |
| Person | `$rel` | `rel` |
| Person | `$relName` | `person.getRelToPlayer().getLevel().name()` |
| Person | `$mercContractDur` | `(int)Global.getSettings().getFloat("officerMercContractDur")` |
| Person | `$mercContractDurStr` | `"" + (int)Global.getSettings().getFloat("officerMercContractDur")` |
| Person | `$isPerson` | `true` |
| Person | `$name` | `person.getName().getFullName()` |
| Person | `$personName` | `person.getName().getFullName()` |
| Person | `$isContact` | `ContactIntel.playerHasContact(person, false)` |
| Person | `$rankId` | `person.getRankId()` |
| Person | `$postId` | `person.getPostId()` |
| Person | `$aiCoreId` | `person.getAICoreId()` |
| Person | `$isAICore` | `true` |
| Person | `$rankAOrAn` | `person.getRankArticle()` |
| Person | `$postAOrAn` | `person.getPostArticle()` |
| Person | `$rank` | `person.getRank().toLowerCase()` |
| Person | `$Rank` | `Misc.ucFirst(person.getRank())` |
| Person | `$post` | `person.getPost().toLowerCase()` |
| Person | `$Post` | `Misc.ucFirst(person.getPost())` |
| Person | `$importance` | `person.getImportance().name()` |
| Person | `$importanceAtLeastHigh` | `person.getImportance().ordinal() >= PersonImportance.HIGH.ordinal()` |
| Person | `$importanceAtMostLow` | `person.getImportance().ordinal() <= PersonImportance.LOW.ordinal()` |
| Person | `$level` | `person.getStats().getLevel()` |
| Person | `$personality` | `person.getPersonalityAPI().getId()` |
| Person | `$hostileToMarket` | `person.getFaction().isHostileTo(market.getFaction())` |
| Faction | `$id` | `faction.getId()` |
| Faction | `$friendlyToPlayer` | `true` |
| Faction | `$hostileToPlayer` | `true` |
| Faction | `$neutralToPlayer` | `true` |
| Faction | `$isHostile` | `false` |
| Faction | `$isHostile` | `true` |
| Faction | `$color` | `c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + c.getAlpha()` |
| Faction | `$baseColor` | `c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + c.getAlpha()` |
| Faction | `$brightColor` | `c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + c.getAlpha()` |
| Faction | `$darkColor` | `c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + c.getAlpha()` |
| Faction | `$gridColor` | `c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + c.getAlpha()` |
| Faction | `$isNeutralFaction` | `faction.isNeutralFaction()` |
| Faction | `$relValue` | `rel` |
| Faction | `$rel` | `level.name()` |
| Faction | `$relName` | `level.name()` |
| Global | `$isDevMode` | `true` |
| Global | `$isInTutorial` | `true` |
| Global | `$daysSinceStart` | `PirateBaseManager.getInstance().getUnadjustedDaysSinceStart()` |
| Player | `$abyssalDepth` | `Misc.getAbyssalDepth(fleet)` |
| Player | `$abyssalDepthUncapped` | `Misc.getAbyssalDepth(fleet, true)` |
| Player | `$firstName` | `person.getName().getFirst()` |
| Player | `$lastName` | `person.getName().getLast()` |
| Player | `$name` | `person.getName().getFullName()` |
| Player | `$factionName` | `Global.getSector().getFaction(Factions.PLAYER).getDisplayNameOverride()` |
| Player | `$theFactionName` | `Global.getSector().getFaction(Factions.PLAYER).getDisplayNameWithArticleOverride()` |
| Player | `$commissionFactionId` | `Misc.getCommissionFactionId()` |
| Player | `$locationId` | `fleet.getContainingLocation().getId()` |
| Player | `$fleetId` | `fleet.getId()` |
| Player | `$transponderOn` | `fleet.isTransponderOn()` |
| Player | `$supplies` | `(int)fleet.getCargo().getSupplies()` |
| Player | `$fuel` | `(int)fleet.getCargo().getFuel()` |
| Player | `$machinery` | `(int)fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY)` |
| Player | `$marines` | `(int)fleet.getCargo().getMarines()` |
| Player | `$crew` | `(int)fleet.getCargo().getCrew()` |
| Player | `$crewRoom` | `(int)(fleet.getCargo().getMaxPersonnel() - fleet.getCargo().getTotalPersonnel())` |
| Player | `$fuelRoom` | `(int)fleet.getCargo().getMaxFuel() - (int)fleet.getCargo().getFuel()` |
| Player | `$cargoRoom` | `(int)fleet.getCargo().getMaxCapacity() - (int)fleet.getCargo().getSpaceUsed()` |
| Player | `$crewRoomStr` | `Misc.getWithDGS((int)(fleet.getCargo().getMaxPersonnel() - fleet.getCargo().getTotalPersonnel()))` |
| Player | `$fuelRoomStr` | `Misc.getWithDGS((int)fleet.getCargo().getMaxFuel() - (int)fleet.getCargo().getFuel())` |
| Player | `$cargoRoomStr` | `Misc.getWithDGS((int)fleet.getCargo().getMaxCapacity() - (int)fleet.getCargo().getSpaceUsed())` |
| Player | `$credits` | `(int)fleet.getCargo().getCredits().get()` |
| Player | `$creditsStr` | `Misc.getWithDGS((int)fleet.getCargo().getCredits().get())` |
| Player | `$creditsStrC` | `Misc.getWithDGS((int)fleet.getCargo().getCredits().get()) + Strings.C` |
| Player | `$inDebt` | `debt` |
| Player | `$inLongDebt` | `longDebt` |
| Player | `$maxHullSize` | `maxSize` |
| Player | `$maxCombatHullSize` | `maxCombatSize` |
| Player | `$fleetSizeCount` | `fleetSizeCount` |
| Player | `$numShips` | `fleet.getFleetData().getMembersListCopy().size()` |
| Player | `$fleetPoints` | `fleet.getFleetPoints()` |
| Player | `$numColonies` | `Misc.getPlayerMarkets(true).size()` |
| Player | `$flagshipName` | `fleet.getFlagship().getShipName()` |

### Dynamically named fact families

These are not finite catalogues of literal keys; the suffix is an actual tag, condition, industry, custom-property, commodity or ability ID.

| Family | Owner / meaning |
|---|---|
| `$tag:<tagId>` | Entity, person or market tags on that owner |
| `$market.mc:<conditionId>` | A market condition is present |
| `$market.ind:<industryId>` | An industry is present |
| `$faction.c:<customKey>` | Faction custom JSON value converted to String |
| `$player.locTag:<tagId>` | Tag on the player's current location |
| `$player.ability:<abilityId>` | Player fleet has that ability |
| `$player.<commodityId>` | Commodity quantity populated from cargo, subject to the updater's existing-key checks |

The colon is part of the memory key. Java stores e.g. `$tag:station` in the owner's MemoryAPI, not `$entity.tag:station` in local. Use actual IDs from game/mod data. Do not interpret “not generated in this context” as proof of a persistent false value.

## State flags and subsystem keys

The following index is extracted from `com/fs/starfarer/api/impl/campaign/ids/MemFlags.java`, including aliases sharing a literal key. These constants identify state used by engine subsystems, not automatically available text. A constant alone does not specify its owner's type, the stored value's type or when it is safe to write it. Inspect the consuming class before adding a new use.

Common choices relevant to this mod:

| Need | Existing keys / API route |
|---|---|
| Determine a fleet's role | `$isPatrol`, `$isTradeFleet`, `$isScavenger`, `$isSmuggler`, `$isPirate`, `$fleetType` on the fleet |
| Resolve its source market | MEMORY_KEY_SOURCE_MARKET / `$sourceMarket`; the rules dialog uses its ID to expose sourceMarket memory |
| Mark a mission target | ENTITY_MISSION_IMPORTANT / MEMORY_KEY_MISSION_IMPORTANT; use the existing importance helpers and matching removal, not a bare guessed Boolean |
| Change hostility, pursuit or disengagement | MakeOtherFleet* commands and Misc.setFlagWithReason preserve independent reasons. Do not erase a shared flag to cancel only your own reason. |
| No reputation impact | MEMORY_KEY_NO_REP_IMPACT / MakeOtherFleetNoRepImpact, not LOW_REP_IMPACT |
| Deny repeated comm requests | MEMORY_KEY_IGNORE_PLAYER_COMMS / `$ignorePlayerCommRequests`; preserve existing fleet conversation routing |
| Fleet is occupied with a special action | FLEET_BUSY and FLEET_SPECIAL_ACTION have a paired contract; the latter distinguishes specific work on an already busy fleet |
| Salvage content or defenders | SALVAGE_SPECIAL_DATA, SALVAGE_DEFENDER_OVERRIDE and their owning salvage implementations; these are structured state, not generic text tokens |

| MemFlags constant | Stored key |
|---|---|
| `ACADEMY_FLEET` | `$academyFleet` |
| `AUTO_OPEN_BUY_SHIPS_TAB` | `$autoOpenBuyShipsTab` |
| `AVOIDING_ABYSSAL_HYPERSPACE` | `$avoidingAbyssalHyperspace` |
| `CAN_ONLY_BE_ENGAGED_WHEN_VISIBLE_TO_PLAYER` | `$canOnlyBeEngagedWhenVisibleToPlayer` |
| `CLAIMING_FACTION` | `$claimingFaction` |
| `DO_NOT_TRY_TO_AVOID_NEARBY_FLEETS` | `$doNotTryToAvoidNearbyFleets` |
| `ENTITY_MISSION_IMPORTANT` | `$missionImportant` |
| `EXCEPTIONAL_SLEEPER_POD_OFFICER` | `$exceptionalSleeperPodOfficer` |
| `EXTRA_SENSOR_INDICATORS` | `$extraSensorIndicators` |
| `FACTION_SATURATION_BOMBARED_BY_PLAYER` | `$numTimesSatBombardedByPlayer` |
| `FCM_EVENT` | `$fcm_eventRef` |
| `FCM_FACTION` | `$fcm_faction` |
| `FLEET_BUSY` | `$core_fleetBusy` |
| `FLEET_CHASING_GHOST` | `$core_fleetChasingGhost` |
| `FLEET_CHASING_GHOST_RANDOM` | `$core_fleetChasingGhostRandom` |
| `FLEET_DO_NOT_IGNORE_PLAYER` | `$cfai_doNotIgnorePlayer` |
| `FLEET_FIGHT_TO_THE_LAST` | `$core_fightToTheLast` |
| `FLEET_IGNORED_BY_FACTION` | `$cfai_ignoredByFaction` |
| `FLEET_IGNORED_BY_OTHER_FLEETS` | `$cfai_ignoredByOtherFleets` |
| `FLEET_IGNORES_OTHER_FLEETS` | `$cfai_ignoreOtherFleets` |
| `FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN` | `$fidConifgGen` |
| `FLEET_MILITARY_RESPONSE` | `$core_fleetMilitaryResponse` |
| `FLEET_NO_MILITARY_RESPONSE` | `$core_fleetNoMilitaryResponse` |
| `FLEET_NOT_CHASING_GHOST` | `$core_fleetNotChasingGhost` |
| `FLEET_PATROL_DISTANCE` | `$cfai_patrolDist` |
| `FLEET_SPECIAL_ACTION` | `$core_fleetSpecialAction` |
| `GLOBAL_INTERDICTION_PULSE_JUST_USED_IN_CURRENT_LOCATION` | `$interdictionPulseJustUsed` |
| `GLOBAL_SENSOR_BURST_JUST_USED_IN_CURRENT_LOCATION` | `$sensorBurstJustUsed` |
| `HASSLE_TYPE` | `$hassleType` |
| `HIDDEN_BASE_MEM_FLAG` | `$core_hiddenBase` |
| `JUST_DID_INTERDICTION_PULSE` | `$justDidInterdictionPulse` |
| `JUST_DID_SENSOR_BURST` | `$justDidSensorBurst` |
| `JUST_TOGGLED_TRANSPONDER` | `$justToggledTransponder` |
| `KANTA_BLUFFS` | `$kantaBluffs` |
| `LIGHT_SOURCE_COLOR_OVERRIDE` | `$lightColorOverride` |
| `LIGHT_SOURCE_OVERRIDE` | `$lightSourceOverride` |
| `MARKET_CAN_ALWAYS_INCENTIVIZE_GROWTH` | `$marketCanAlwaysIncentivizeGrowth` |
| `MARKET_DO_NOT_INIT_COMM_LISTINGS` | `$doNotInitCommListings` |
| `MARKET_EXTRA_SUSPICION` | `$marketExtraSuspicion` |
| `MARKET_HAS_CUSTOM_INTERACTION_OPTIONS` | `$hasCustomInteractionOptions` |
| `MARKET_MILITARY` | `$military` |
| `MARKET_PATROL` | `$patrol` |
| `MAY_GO_INTO_ABYSS` | `$mayGoIntoAbyss` |
| `MEMORY_KEY_ALLOW_LONG_PURSUIT` | `$cfai_longPursuit` |
| `MEMORY_KEY_ALLOW_PLAYER_BATTLE_JOIN_TOFF` | `$cfai_allowPlayerBattleJoinTOff` |
| `MEMORY_KEY_AVOID_PLAYER_SLOWLY` | `$cfai_avoidPlayerSlowly` |
| `MEMORY_KEY_CUSTOMS_INSPECTOR` | `$isCustomsInspector` |
| `MEMORY_KEY_DO_NOT_SHOW_FLEET_DESC` | `$shownFleetDescAlready` |
| `MEMORY_KEY_EVERYONE_JOINS_BATTLE_AGAINST` | `$everyoneJoinsBattleAgainst` |
| `MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED` | `$doNotGetSidetracked` |
| `MEMORY_KEY_FLEET_TYPE` | `$fleetType` |
| `MEMORY_KEY_FORCE_AUTOFIT_ON_NO_AUTOFIT_SHIPS` | `$overrideNoAutofit` |
| `MEMORY_KEY_FORCE_TRANSPONDER_OFF` | `$forceTOff` |
| `MEMORY_KEY_IGNORE_PLAYER_COMMS` | `$ignorePlayerCommRequests` |
| `MEMORY_KEY_LOW_REP_IMPACT` | `$lowRepImpact` |
| `MEMORY_KEY_MAKE_AGGRESSIVE` | `$cfai_makeAggressive` |
| `MEMORY_KEY_MAKE_AGGRESSIVE_ONE_BATTLE_ONLY` | `$cfai_makeAggressiveLastsOneBattle` |
| `MEMORY_KEY_MAKE_ALLOW_DISENGAGE` | `$cfai_makeAllowDisengage` |
| `MEMORY_KEY_MAKE_ALWAYS_PURSUE` | `$cfai_makeAlwaysPursue` |
| `MEMORY_KEY_MAKE_HOLD_VS_STRONGER` | `$cfai_holdVsStronger` |
| `MEMORY_KEY_MAKE_HOSTILE` | `$cfai_makeHostile` |
| `MEMORY_KEY_MAKE_HOSTILE_TO_ALL_TRADE_FLEETS` | `$cfai_makeHostileToAllTradeFleets` |
| `MEMORY_KEY_MAKE_HOSTILE_TO_PLAYER_TRADE_FLEETS` | `$cfai_makeHostileToPlayerTradeFleets` |
| `MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF` | `$cfai_makeHostileWhileTOff` |
| `MEMORY_KEY_MAKE_NON_AGGRESSIVE` | `$cfai_makeNonAggressive` |
| `MEMORY_KEY_MAKE_NON_HOSTILE` | `$cfai_makeNonHostile` |
| `MEMORY_KEY_MAKE_PREVENT_DISENGAGE` | `$cfai_makePreventDisengage` |
| `MEMORY_KEY_MISSION_IMPORTANT` | `$missionImportant` |
| `MEMORY_KEY_NEVER_AVOID_PLAYER_SLOWLY` | `$cfai_neverAvoidPlayerSlowly` |
| `MEMORY_KEY_NO_JUMP` | `$cfai_noJump` |
| `MEMORY_KEY_NO_REP_IMPACT` | `$noRepImpact` |
| `MEMORY_KEY_NO_SHIP_DERELICTS_IN_POST_BATTLE_DEBRIS` | `$noShipDerelictsPostBattle` |
| `MEMORY_KEY_NO_SHIP_RECOVERY` | `$noShipRecovery` |
| `MEMORY_KEY_NUM_GR_INVESTIGATIONS` | `$numGRInvestigations` |
| `MEMORY_KEY_PATROL_ALLOW_TOFF` | `$patrolAllowTOff` |
| `MEMORY_KEY_PATROL_FLEET` | `$isPatrol` |
| `MEMORY_KEY_PIRATE` | `$isPirate` |
| `MEMORY_KEY_PLAYER_HOSTILE_ACTIVITY_NEAR_MARKET` | `$playerHostileTimeout` |
| `MEMORY_KEY_PURSUE_PLAYER` | `$pursuePlayer` |
| `MEMORY_KEY_RAIDER` | `$isRaider` |
| `MEMORY_KEY_RECENTLY_DEFEATED_BY_PLAYER` | `$cfai_recentlyDefeatedByPlayer` |
| `MEMORY_KEY_REQUIRES_DISCRETION` | `$requiresDiscretionToDeal` |
| `MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_OFF` | `$sawPlayerTransponderOff` |
| `MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON` | `$sawPlayerTransponderOn` |
| `MEMORY_KEY_SCAVENGER` | `$isScavenger` |
| `MEMORY_KEY_SKIP_TRANSPONDER_STATUS_INFO` | `$skipTInfo` |
| `MEMORY_KEY_SMUGGLER` | `$isSmuggler` |
| `MEMORY_KEY_SOURCE_MARKET` | `$sourceMarket` |
| `MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET` | `$keepPursuingPlayer` |
| `MEMORY_KEY_TRADE_FLEET` | `$isTradeFleet` |
| `MEMORY_KEY_WAR_FLEET` | `$isWarFleet` |
| `MEMORY_MARKET_SMUGGLING_SUSPICION_LEVEL` | `$smugglingSuspicion` |
| `NO_HIGH_BURN_TOPOGRAPHY_READINGS` | `$noHighBurnTopographyReadings` |
| `NON_HOSTILE_OVERRIDES_MAKE_HOSTILE` | `$makeNonHostileTakesPriority` |
| `OBJECTIVE_NON_FUNCTIONAL` | `$objectiveNonFunctional` |
| `OFFICER_MAX_ELITE_SKILLS` | `$officerMaxEliteSkills` |
| `OFFICER_MAX_LEVEL` | `$officerMaxLevel` |
| `OFFICER_SKILL_PICKS_PER_LEVEL` | `$officerSkillPicksPerLevel` |
| `PATROL_EXTRA_SUSPICION` | `$patrolExtraSuspicion` |
| `PLAYER_ATROCITIES` | `$atrocities` |
| `PLAYER_UNTRUSTWORTHY` | `$untrustworthy` |
| `PREV_SALVAGE_SPECIAL_DATA` | `$prevSalvageSpecialData` |
| `RECENTLY_BOMBARDED` | `$recentlyBombarded` |
| `RECENTLY_PERFORMED_RAID` | `$recentlyPerformedRaid` |
| `RECENTLY_RAIDED` | `$recentlyRaided` |
| `RECENTLY_SALVAGED` | `$recentlySalvaged` |
| `SALVAGE_DEBRIS_FIELD` | `$salvageDebrisField` |
| `SALVAGE_DEFENDER_OVERRIDE` | `$salvageDOv` |
| `SALVAGE_SEED` | `$salvageSeed` |
| `SALVAGE_SPEC_ID_OVERRIDE` | `$salvageSpecId` |
| `SALVAGE_SPECIAL_DATA` | `$salvageSpecialData` |
| `SENSOR_INDICATORS_OVERRIDE` | `$sensorIndicatorsOverride` |
| `SHRINE_PILGRIM_FLEET` | `$shrinePilgrimFleet` |
| `SPREAD_TOFF_HOSTILITY_IF_LOW_IMPACT` | `$alwaysSpreadTOffHostility` |
| `STAR_SYSTEM_IN_ANCHOR_MEMORY` | `$anchor_starSystem` |
| `STATION_BASE_FLEET` | `$stationBaseFleet` |
| `STATION_FLEET` | `$stationFleet` |
| `STATION_MARKET` | `$stationMarket` |
| `STORY_CRITICAL` | `$story_critical` |
| `SUSPECTED_AI` | `$suspectedAI` |
| `TEMPORARILY_NOT_AVOIDING_ABYSSAL` | `$tempNotAvoidingAbyssal` |
| `WILL_HASSLE_PLAYER` | `$willHasslePlayer` |

## Quest-local and framework keys

`$option`, `$menuState`, mission prefixes such as `$ga...`, and every key in [KEY_USAGE](KEY_USAGE.md) need their actual caller and owner checked. A vanilla rule using a key does not make that key global, persistent or safe for a new mission. The index includes condition references and assignment examples so the producer and consumer can be traced.

## Missing-key and lifetime checks

For a missing substitution, ask in order: is it a generated token or a stored value; is the correct person active; does the expected scope exist; was the value prepared before Text/options; has its timer expired; is its type eligible for replacement? `DumpMemory` helps with stored values only. A raw unexpanded token in output is a bug, not a reason to invent a replacement with a different meaning.

For stale data, check whether a no-duration write unintentionally persisted or a timed write is refreshed whenever the conversation opens. Use [the lifetime table](../RULES_AUTHORING.md#memory-lifetime): no duration persists, zero is ordinarily dialogue-temporary, positive durations are campaign-day timers. A two-argument setter overwrites the value and cancels the old expiry.
