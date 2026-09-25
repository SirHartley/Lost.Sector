# Exploration hints

Three kinds of exploration intel: signal hints that point at the named bounty systems and Frost, the system tip a Kesteven officer sells at Kesteven bars, and the Frost entry shown on the first visit to Frost. They are record quest `hint` on the [quest framework](../../jars/src/lostsector/quest/README.md). Java paths are relative to `jars/src/lostsector/campaign/events/hints/`.

## Owners

| Part | Owner |
|---|---|
| Definition, queries | `HintsQuest` (id `hint`), registered in `quest/QuestCatalog` |
| Stage, state, records | `HintsStage` (`RUNNING` only), `HintsState`, `HintRecord`; no flags (`NoFlags`) |
| Modules | `HintsFrostModule`, `HintsSignalsModule`, `HintsVisitModule`, `HintsTipModule`, in that order; all active in every stage |
| Bounty reports | Quest `bounty` (`campaign/bounties/BountiesQuest`): the Abyss, Eternity and Mothership bounties call `HintsQuest.reportBountySighted()` from their `onSighted`; `HintsSignalsModule` reads `BountiesQuest.location` and `BountiesQuest.sighted` |
| Text | `data/campaign/rules.csv`, block `# HINTS` |

The Frozen Heart itself (comm directory cleanup, its destruction and wreckage) stays in `enigma/HeartOccupation`, outside the quest.

## State

| Field | Content |
|---|---|
| `sources` | Signal sources not yet used, by id: `abyss`, `eternity`, `mothership`, `frost`, in that order. The bounty ids are those of quest `bounty`. Filled by `HintsSignalsModule.onStart` from `BountiesQuest.location` of each bounty (quest `bounty` comes earlier in `QuestCatalog`, so its `onStart` has placed them) and the Frost system; a bounty without a location is left out. |
| `records` | `HintRecord` per intel entry with a record: signal hints under their source id, bought tips under `tip1`, `tip2` and so on. Each holds the intel key, the system, a tip's theme tag at purchase (`THEME_DERELICT`, `THEME_REMNANT` or none) and `visited`. |
| `tipsBought` | Number of the last bought tip |
| `offerSystem`, `offerPrice`, `offerDays`, `offerBought` | The officer's current offer; `offerSystem` is null between offers |
| `cooldownDays` | Days between the end of one offer and the next |
| `frostShown`, `frostGone` | The Frost entry was shown; the Enigma no longer holds the Frozen Heart |

Timers: `tipOffer` (start of the current offer), `tipCooldown` (end of the last offer). Random purposes: `signal` (the hint roll and the source pick), `tipSystem`, `tipPrice`, `tipTiming` (offer and cooldown durations), `person:officer`.

People: `officer` (`nskr_hint_officer`), a random Kesteven person with post `POST_GENERIC_MILITARY`. Each new offer releases the previous officer and creates a new one.

## Intel entries

| Key | Record | Icon | Title | Shown | Ends |
|---|---|---|---|---|---|
| `signal` | Source id | `hint` | "Mysterious signals" | A signal hint roll hits | Closed (three-day end delay) on arriving in the system; the Frost hint ends at once on arriving in Frost; a bounty sighting ends the hint of the system the player is in |
| `tip` | `tip<N>` | `hint` | "Purchased Intel" | The player leaves the officer after buying | Closed on arriving in the system |
| `frost` | none | `frost` | "The" and the Frost system name | First arrival in Frost | Closed once `SectorLookup.enigmaExists()` is false at a daily check |

All three carry the fleet log and exploration tags, post with the major posting sound (`majorPosting()`), are `deletable()` once closed, and point the map at the system's hyperspace anchor. `signal` and `tip` repeat their bullet under the description (`descriptionBullets()`); `frost` sorts in `TIER_2` while active. Literal highlights come from `SetTextHighlights` lines in the intel rows: "source", the tip's threat word, "hostile", "Frozen Heart" and the whole "The" line of the Frost description; quest token values (system names) are highlighted as well. Rows select on `$nskr_intel_key`; for the text of one signal or tip, the token `recordSystem` and the checks `recordVisited`, `tipDerelict` and `tipRemnant` read the record named by `$nskr_intel_record`. The tip bullet has one row per threat word, chosen by those two checks. The Frost entry's sensors-officer line is its only bullet and shows only in messages (`$nskr_intel_mode == update`), so it appears with the posting message on arrival.

## Flow

### Signal hints

On every location change (`HintsSignalsModule.onLocationChanged`), while sources remain: when the new location is a star system the player has not entered, without `THEME_CORE` or `SYSTEM_CUT_OFF_FROM_HYPER`, one draw from `signal` below 4% (`HINT_CHANCE`) picks a remaining source uniformly with a second draw, removes it and shows its `signal` entry. The location change arrives when the jump switches locations, before `CoreScript.reportFleetJumped` marks the system entered, so a first entry reads as unentered.

A source leaves the list when its hint is shown, when the Frost entry is shown (`frost`), or, checked in `onDay` and before each roll, once `BountiesQuest.sighted` is true for its bounty. `reportBountySighted`, called when a bounty is sighted, ends at once a signal hint for the system the player is in.

### Arrival

`HintsVisitModule.onLocationChanged` marks every record of the new location as visited. Its entry switches to the visited description and closes: the vanilla three-day end delay, with no message. The Frost signal hint ends at once, because the Frost entry takes over. Module order puts the roll first, so a hint rolled for the system just entered is visited at once.

### The Kesteven tip

`HintsTipModule.onDay` runs one offer at a time:

1. With no offer and the cooldown over, it picks an unentered procgen system: half the time among systems with a derelict survey ship or derelict mothership, otherwise among systems with a warning beacon (`SystemPicker`, random `tipSystem`). It sets the price to 10,000 to 30,000 credits in steps of 1,000, the offer's length to 15 to 30 days, and creates the officer. With no valid system it starts a cooldown instead.
2. The offer ends when its days run out unbought, when the player declines, or when the player leaves after buying. The next offer follows after `max(0, 30 - 40r)` days: none a quarter of the time, otherwise 0 to 30.

While an offer is open and unbought, row `nskr_hint_tipBar` adds the bar blurb and option at every market whose faction is Kesteven (`check tipHere`). The conversation:

| Row | Screen | Options |
|---|---|---|
| `nskr_hint_tipApproach` | `BeginConversation nskr_hint_officer true false` (minimal card, no relationship bar), the officer at the datapad | Offer to buy a round of drinks |
| `nskr_hint_tipDrinks` | The pitch, three paragraphs | "Yes.", "Maybe." (the same screen, by a continue chain), Politely decline |
| `nskr_hint_tipYes` | `FireBest nskr_hintTipOfferText` picks the distance paragraph by threat word (`check offerDerelict`, `check offerRemnant`, or the fallback); then the price paragraph; distance, threat word and price highlighted; `FireAll nskr_hintTipOfferOptions` | Buy the information for the price (only with more credits than the price, `check canAffordTip`), Politely decline |
| `nskr_hint_tipBuy` | `do buyTip` takes the credits with the vanilla receipt; "Added log entry for the" system in small gray; `ui_noise_static` | Leave |
| `nskr_hint_tipLeaveBought`, `nskr_hint_tipDecline` | `HideVisual`, `do leaveTip`, `BarCMD returnFromEvent true` | Continue, back to the bar list |

`leaveTip` adds the `tip` record and shows its entry when the offer was bought, then ends the offer. The distance is the player fleet's distance to the system in light-years with two decimals as a float prints (`12.5`, `7.13`). The threat word is "Derelict" for a `THEME_DERELICT` system, else "Remnant" for `THEME_REMNANT`, else empty; the words are in the rows, and Java only reports the theme through the checks.

### Frost

`HintsFrostModule.onLocationChanged` shows the `frost` entry on the first arrival in the Frost system and removes the `frost` signal source. Its posting message carries the sensors-officer line. Once a day, after the entry was shown, it checks `SectorLookup.enigmaExists()`: the Heart market can leave the Enigma through `HeartOccupation`'s destruction, decivilization or an invasion, and no single callback covers all three. When the Enigma is gone, the entry's description switches to the quiet text and the entry closes.

## Dev menu

No stage jumps apply. `devInfo` lists the remaining sources, every record with its system and visited state, the offer or the cooldown, the number of tips bought, and the Frost state. Reset (a jump to `RUNNING`) ends every entry, releases the officer and refills the sources.

## Defects found in the source

- The tip's threat word is empty for a system with neither theme tag, which prints "a hotspot of  activity" and "contain a  threat". The derelict pick only requires a derelict survey ship or mothership, and the beacon pick the nearest system to a beacon, so such systems can be picked.
- "Maybe." does the same as "Yes.": both lead to the offer screen.
