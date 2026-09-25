# Blacksites

Twelve derelict stations placed at world generation. Each belongs to a faction, is rigged with alarms, and opens for salvage once the player defends it against the faction's fleets. The blacksites are the record quest `bs` of the [quest framework](../../jars/src/lostsector/quest/README.md): one stage, one record per station.

Paths are relative to `jars/src/lostsector/campaign/events/blacksite/`.

## Owners

| Class | Job |
|---|---|
| `BlacksiteSpawner` | World generation, called from `ModPlugin.onNewGameAfterTimePass()`: places up to twelve `nskr_blacksite` custom entities with ids `nskr_blacksite0` onward, discoverable, sensor profile 1000. `siteIds()` lists every id it can assign. |
| `BlacksiteQuest` | Quest `bs` in `quest/QuestCatalog`, stage enum `BlacksiteStage`, no flags. |
| `BlacksiteModule` | The only module, active in `RUNNING`: adoption, claims, the checks, the `activate` action, the tokens, the four defender roles and the daily and fleet hooks. |
| `BlacksiteState`, `SiteRecord` | Saved state: `sites`, a map from the station's entity id to its `SiteRecord`. |
| `BlacksiteFaction` | Per-owner data: faction id, picker weight, point range, station name, fleet name, loot entity and defender role. |
| `BlacksiteSites` | Builders: record creation and station naming, defender fleets, the strength estimate, the loot swap and the destruction. |
| `rules.csv` `# BLACKSITES` | All dialog text and options. |

## Stage and records

The quest has one stage, `RUNNING`, entered when the state is created at the end of the first `onGameLoad`. `onStart` adopts every station that `getEntityById` finds for `BlacksiteSpawner.siteIds()`, creates its record and claims its dialog with trigger `nskr_bsSite`.

A record holds the station entity, its id, the `BlacksiteFaction`, the defender count (1 to 3), the point budget, a status and `destroyDays`.

- **Owner.** A station in a system tagged `THEME_REMNANT` belongs to the Remnants. Otherwise a weighted pick: Luddic Path 4, pirates 4, Enigma 5, Kesteven 6, Tri-Tachyon 6. The station is renamed after its owner ("Pather Stash", "Pirate Stash", "Ancient Enigma Hangar", "Kesteven Blacksite", "Tri-Tachyon Blacksite", "Ancient Remnant Hangar").
- **Budget.** A random value in the owner's range (pirates 150 to 175, Luddic Path 125 to 150, Tri-Tachyon, Kesteven and Remnants 100 to 125, Enigma 75 to 100) times 0.67, 1.0 or 1.33 for one, two or three defenders.

| Status | Meaning | Leaves when |
|---|---|---|
| `DORMANT` | Alarms not tripped | The player trips the alarm (`activate`) |
| `ACTIVE` | Defenders spawned; countdown timer `countdown:<id>` running | 20 days pass or no unbroken defender is left: `CLEARED`. Defenders orbit the station for 7 fleet-days in total: `DESTROYED` |
| `CLEARED` | The station was swapped for its loot entity (`nskr_blacksite_<owner>` in `salvage_entity_gen_data.csv`), discoverable at any range; claim released | Defenders still orbit the removed station's last position for 7 fleet-days: `DESTROYED` (see [Defects](#defects-found-by-reading-the-source)) |
| `DESTROYED` | The station was replaced by a debris field (`nskr_debris_<id>`) on its orbit, with the sound `hit_hull_heavy`; claim released; defenders withdraw | Final |

Records are never removed.

## Defenders

`activate` spawns `count` fleets with `ctx.fleets().spawn(role, siteId, spec)`. Each gets `budget / count` points, raised by half of `PowerLevel.get(0.2, 0, 1)` and multiplied by `Difficulty.scriptedFleetMult()`, and spawns at a random point of the station's system beyond 1.5 times the player's sensor strength (500 tries, then anywhere). Fleets are `PATROL_LARGE`, 0 or 1 average S-mods, hostile, non-aggressive, avoid the player slowly, fight to the last, prevent disengage, no reputation impact. Names: "Flotilla", "Gang", "Black Ops Group", "Strike Force", "Black Ops" or "Sub-Ordo" plus a Greek letter; Remnant fleets get the Remnant interaction config.

| Role | Owners | Orbit text | Withdraws to |
|---|---|---|---|
| `looter` | Luddic Path, pirates | looting location | A random market of its faction |
| `evacuator` | Tri-Tachyon, Kesteven | evacuating location | A random market of its faction |
| `wrecker` | Enigma | destroying location | A random market of its faction |
| `remnant` | Remnants | destroying location | Its spawn point |

Each role uses `FleetOrders.raid`: every 0.1 days the fleet goes to its target (the station) when farther than 600 units and otherwise orbits it. A fleet below a fifth of its spawn strength is *broken*: it withdraws and no longer counts as a defender. Destruction clears the fleets' target, which makes them withdraw too. Withdrawing fleets despawn on arrival. A defender older than 30 days stops taking orders and despawns once it is out of the player's sight (`withdrawAfter`).

## Timing and hooks

| Check | Hook |
|---|---|
| Countdown of 20 days, and whether an unbroken defender is left | `onDay`, and `onBattle` and `onFleetGone` for the fleet's record |
| One fleet-day in `destroyDays` for each defender orbiting within 600 units and unbroken; destruction at 7 | `onDay`, for `ACTIVE` and `CLEARED` records |
| Defender movement; a defender older than 30 days gets no more orders and despawns once the player is beyond `getMaxSensorRangeHyper()` in hyperspace distance | The raid order with `withdrawAfter(30)`, run by `QuestManager` every 0.1 days |

## Dialog

`CorePlugin` opens a rules dialog on `nskr_bsSite` for a claimed station. The rows show the station's image (`ShowDefaultVisual`).

| Row | When | Shows |
|---|---|---|
| `nskr_bs_siteDormant` | `check dormant` | The ops chief's analysis: owner (highlighted in the owner's colour through `nskr_bsFactionHighlight`), defender count and the strength estimate against the player's fleet points (inferior, similar, superior, overwhelming). Options: Trip the alarm, Leave |
| `nskr_bs_siteActive` | `check active` | The number of unbroken defenders. Option: Leave |
| `nskr_bs_siteGone` | Neither | Closes the dialog |
| `nskr_bs_tripAlarmSel` | Trip the alarm | `do activate`, a sensor burst and an interdict ping on the station; Leave plays `ui_sensor_burst_on` and closes |

## Random purposes

`sites` (owners, counts, budgets), `fleets` (defender placement and composition), `loot` (the loot entity), `debris` (the debris field), and the framework's `fleetOrders` (withdrawal markets).

## Dev support

`devInfo` prints one line per site: owner, status, count, budget, unbroken and total defenders, countdown and `destroyDays`. The quest has one stage, so a jump resets it: defenders despawn, claims are released and `onStart` adopts the stations that still exist with new picks. There is no `onSkip`.

## Defects found by reading the source

- The strength estimate is empty when the ratio of defender points to the player's fleet points is exactly 0.7, 1.3 or 1.75, which prints "with their total numbers being  to ours".
- When the countdown clears a site while unbroken defenders remain, they keep orbiting the removed station's last position and, after 7 fleet-days, destroy it: a debris field and the impact sound appear next to the loot entity.
