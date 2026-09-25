# Hellspawn judgement

The judgement of the Hellspawn custom start: THRN's warning, a 40-day countdown, the judgement scene and, unless the captain was peaceful, the Final Judgement fleet. It is quest `hs` on the [quest framework](../../jars/src/lostsector/quest/README.md). Java paths are relative to `jars/src/lostsector/campaign/starts/hellspawn/`.

The Descent itself (points, levels, stat bonuses, the Gate Conduit ability and its swarms) is not part of the quest: `HellSpawnManager`, `HellSpawnNexListener` and `HellSpawnEventIntel` own it, as listed in [ARCHITECTURE.md](../ARCHITECTURE.md). The judgement reads the Descent's progress when it branches.

## Owners

| Part | Owner |
|---|---|
| Definition, queries | `HellSpawnQuest` (id `hs`), registered in `quest/QuestCatalog` |
| Stages, flags, state | `HellSpawnStage`, `HellSpawnFlag`, `HellSpawnState` (timer and random purpose names only) |
| Modules | `HellSpawnSummonsModule`, `HellSpawnThrnModule`, `HellSpawnWarningModule`, `HellSpawnCountdownModule`, `HellSpawnJudgementModule`, `HellSpawnFightModule`, in that order |
| Fleet builder | `HellSpawnFleets.judgement` |
| Portrait animation | `HellSpawnThrnAnimation`, a transient script started by the `thrnAnimate` action |
| Encounter | `HellSpawnJudgementInteraction`, a `FleetInteractionDialogPluginImpl` subclass |
| Countdown intel | `HellSpawnJudgementIntel`, Java until quest intel moves to rows (T11) |
| Text | `data/campaign/rules.csv`, block `# HELLSPAWN` |

`HellSpawnQuest.isAvailable()` is true only in a campaign whose `GameModeManager` mode is `HELLSPAWN`, which the Nexerelin background `HellSpawnBackground` sets before the load finishes. Other campaigns have no `hs` state and no `hs` events.

## Stages

| Stage | Previous | Module work | Leaves by |
|---|---|---|---|
| `DORMANT` | none (start) | `HellSpawnSummonsModule.onDay`: player level 15 or more | `advance DORMANT WARNING` from `onDay` |
| `WARNING` | `DORMANT` | `HellSpawnThrnModule.onStart` creates THRN; `HellSpawnWarningModule.onStart` opens `nskr_hsWarning` on the player fleet | Row `nskr_hs_warning2` |
| `COUNTDOWN` | `WARNING` | `HellSpawnCountdownModule`: `onStart` starts timer `judgement` and adds `HellSpawnJudgementIntel`; `onDay` checks the timer; `onStop` ends the intel at once | `advance COUNTDOWN JUDGEMENT` from `onDay` once more than 40 days have passed |
| `JUDGEMENT` | `COUNTDOWN` | `HellSpawnJudgementModule.onStart` opens `nskr_hsJudgement` on the player fleet | Row `nskr_hs_peaceful4` or `nskr_hs_fight` |
| `SPARED` | `JUDGEMENT` | None; final | |
| `FIGHT` | `JUDGEMENT` | `HellSpawnFightModule.onStart` spawns the Final Judgement fleet (role `judge`) next to the player | `HellSpawnQuest.reportJudgementLeft()` |
| `JUDGED` | `FIGHT` | `HellSpawnFightModule` stays active: during the first day it stops the THRN music on unpaused frames; the fleet is not despawned | Final |

Flag `HELL`: set by row `nskr_hs_hell2`; the judgement fleet gets 1.33 times the points.

People: `thrn` (`nskr_hs_thrn`), created from the independent faction and set up as a neutral omega-core AI named THRN with the `nskr_thrn00` portrait, because the neutral faction has no name sets. It is created when `WARNING` starts and kept for the rest of the campaign.

Timer: `judgement`, started when `COUNTDOWN` starts. Random purposes: `judgeFleet` (fleet points, the fleet build, its position and `FleetHelper.update`), `thrnAnimation` (the name flicker).

## Flow

1. **Warning.** Once a day in `DORMANT`, the player's level is checked. At 15 or more the quest enters `WARNING`, and the warning opens as a rules dialog on the player fleet (`HellSpawnThrnModule.openScene`, which first sets `$playLocationMusicDuringEnc` on the player fleet so the engine does not switch to encounter music); while another dialog is showing it waits in the framework's pending list. `nskr_hs_warningOpen` shows THRN (`ShowPersonVisual false nskr_hs_thrn`), starts the THRN music with a 12-second fade-in (`musicStart`) and the portrait flicker (`thrnAnimate`). The second Continue (`nskr_hs_warning2`) advances to `COUNTDOWN`, which adds the countdown intel. The last Continue stops the music (`musicStop`) and closes the dialog.
2. **Countdown.** `HellSpawnJudgementIntel` shows "soon" and, from day 33, "very soon". It reads its own timestamp, taken when the stage starts, and `HellSpawnCountdownModule.JUDGEMENT_DAYS`.
3. **Judgement.** On the first daily tick after more than 40 days the intel ends and the judgement opens on the player fleet. After the first Continue, `FireBest nskr_hsJudgementPath` offers one Continue by the Descent's progress (`HellSpawnEventIntel.getProgress()`): check `hell` (2000 or more), check `neutral` (350 to 1999), otherwise the peaceful fallback row.
4. **Peaceful.** Four screens. `nskr_hs_peaceful4` grants 8 story points (`AddStoryPoints 8`), plays `ui_noise_static`, grants the Peaceful Heart skill with its receipt and skill panel (`grantPeacefulHeart`, `QuestRewards.skill`) and advances to `SPARED`. Its Continue stops the THRN music and plays the one-shot `nskr_peace` track (`musicFarewell`).
5. **Neutral and hell.** Three or two screens, then `nskr_hs_fight`: `HideVisual`, `advance JUDGEMENT FIGHT` (spawns the fleet), then `engageJudge`, which hands the dialog to `HellSpawnJudgementInteraction` and shows the fleet. The music keeps playing through the encounter.
6. **Encounter.** Opening comms and the three disengage options are refused; each prints its lines by firing a declared trigger with `FireBest` into the encounter (`nskr_hsFightComms`, `nskr_hsFightDisengage`, `nskr_hsFightStoryDisengage`, `nskr_hsFightBattleDisengage`). Choosing Leave, Continue after the battle or Continue to loot despawns the encounter's `otherFleet` and calls `HellSpawnQuest.reportJudgementLeft()`, which advances `FIGHT` to `JUDGED`. On the first unpaused frames of `JUDGED` the THRN music stops.

Clicking the judgement fleet on the map uses the `CorePlugin` route on `HellSpawnJudgementInteraction.JUDGEMENT_FLEET_KEY` (`$hellSpawnJudgementFleet`), which builds the same encounter while a dialog is open.

## Presentation kept in Java

| Effect | Where | Why |
|---|---|---|
| THRN music with a 12-second fade-in; the one-shot `nskr_peace` track | Actions `musicStart`, `musicStop`, `musicFarewell` in `HellSpawnThrnModule` | `PlayCustomMusic` always fades in over one second and loops (`playCustomMusic(1, 1, id, true)`), and `ResumeNormalMusic` would cut the farewell track before the player is back on the map |
| Portrait and name flicker | Action `thrnAnimate`, script `HellSpawnThrnAnimation` | No rules command runs per frame and `RuleBasedInteractionDialogPluginImpl.advance` is empty; the script runs while paused, every 0.1 seconds, only while the dialog that started it is open with the same plugin |
| Hand-off to the encounter | Action `engageJudge` in `HellSpawnFightModule` | `nskr_quest engage` builds a plain `FleetInteractionDialogPluginImpl`; the judgement needs `HellSpawnJudgementInteraction` |
| Skill receipt with skill panel | `QuestRewards.skill` | No vanilla command grants a player skill or prints its receipt |
| Pull-in lines of `HellSpawnAbilityInteraction` ("supporting your forces.") | `HellSpawnAbilityInteraction.pullInNearbyFleets` | A copy of vanilla's own status lines in `FleetInteractionDialogPluginImpl.pullInNearbyFleets`, not quest prose |

## Dev support

`onSkip`: `HellSpawnFightModule` despawns the judgement fleet when a jump passes `FIGHT`. `HellSpawnJudgementModule` grants nothing, because a jump past `JUDGEMENT` does not say which path it takes. The warning and judgement scenes open only outside jumps, since a pending open cannot be withdrawn; open their triggers from the dev menu. `devInfo`: player level (`DORMANT`), days of the countdown, Descent progress and thresholds, the judgement fleet's location and the `HELL` flag.

## Behavior and defects found in the source

- The judgement fleet is not despawned when the player leaves the encounter. `otherFleet` in `HellSpawnJudgementInteraction` is the battle's combined fleet, a copy that `BattleAPI.genCombined` builds, so `otherFleet.despawn()` does not remove the real fleet; a fleet that survives keeps hunting the player. The quest keeps this: the `judge` role's module stays active in `JUDGED`.
- `HellSpawnJudgementInteraction`'s battle-disengage line says "effected" for "affected".
- The warning appears on the first daily tick at level 15 or more, and the judgement on the first daily tick after 40 days; the old manager checked every second.
