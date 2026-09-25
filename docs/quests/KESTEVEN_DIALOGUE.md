# Kesteven questline dialogue map

Where each questline conversation is implemented, how it is entered and how control passes between `rules.csv` and Java. Use it to find the text for a change and to plan moving Java-authored dialogue into rules. How to structure rules content is in [RULES_WRITING.md](../RULES_WRITING.md); rules syntax and project routing in [RULES.md](../RULES.md); commands and memory in [RULES_AUTHORING.md](../RULES_AUTHORING.md); text standards in [DIALOGUE.md](../DIALOGUE.md).

Java paths are relative to `jars/src/lostsector/campaign/`; `dialogue/rules/` and `combat/` paths are relative to `jars/src/lostsector/`.

## Implementation styles

| Style | Used by | How it runs |
|---|---|---|
| Rules rows only | Every conversation with Jack, Alice and Nicholas (gates, values and game actions from `KestevenHubModule`); the job 3 party, a rules bar event (guests, drink count and bill from `KestevenPartyModule`); the Glacier facility (`KestevenGlacierModule`); the Eliza search at pirate bars (`KestevenElizaSearchModule`); the data-disk satellites (`KestevenSatelliteModule`); the Luddic and Tri-Tachyon endings (`KestevenAltEndingsModule`); the Delve meeting at the bar (`KestevenJob5Module`); the Cache's inner voice and command core (`KestevenCacheModule`); the meeting at Eliza's port (`KestevenElizaModule`); the Kesteven and Eliza endings (`KestevenEndingsModule`); Eliza's fleets (`KestevenElizaFleetsModule`); job 4: the Special Operations fleet, the Enigma strike group and the hint wreck (checks, actions and tokens from `KestevenJob4Module`, [below](#job-4-rows)); the Tri-Tachyon collector ([below](#tri-tachyon-collector-rows)); Jack's revenge fleet (`KestevenAftermathModule`); fleet greetings and threats: the Cache guardian, the "LZ" messenger, generic Enigma comms | Text, options and scripts live in `data/campaign/rules.csv`. |

## Jack, Alice and Nicholas

Every conversation with Jack, Alice and Nicholas is in the `# KESTEVEN QUESTLINE` block of `data/campaign/rules.csv`. `kesteven/quest/KestevenHubModule` declares the checks, actions and tokens the rows use; no Java prints its text.

### Rows and triggers

The block has sub-headers in play order: `# Entry`, `# Jack`, `# Jack: job 1`, `# Jack: jobs 3 and 5`, `# Jack: questions`, `# Alice`, `# Alice: job 3`, `# Alice: job 4`, `# Alice: job 5`, `# Alice: questions`, `# Nicholas`, `# Shared lines` and `# Hand-offs`.

| Screen | Structure | Trigger and rows |
|---|---|---|
| Entry option | Market menu row | `nskr_kq_hubChatOption` on `PopulateOptions`: `$tag:k_quest`, `check hubOpen` (Kesteven market, Kesteven standing above -50), not `ENDED`, not `COMPLETED` or `FAILED`. Adds `nskr_kq_chat` "Chat about operations work". |
| Open a speaker | `DialogOptionSelected` pick by `$id` | `nskr_kq_jackChat`, `nskr_kq_aliceChat`, `nskr_kq_nicholasChat` fire the speaker's greeting, status and menu. `nskr_kq_jackChatJob1Tip` first runs `do pickJob1Tip`. |
| Greeting and introduction | `FireBest` pick (Jack, Alice); `FireAll` text inserts (Nicholas) | `nskr_kqJackGreeting`, `nskr_kqAliceGreeting`, `nskr_kqNicholasGreeting`. The introduction rows set `$nskr_kq_introduced` in the speaker's memory. |
| Status line | `FireBest` pick | `nskr_kqJackStatus`, `nskr_kqAliceStatus`, `nskr_kqNicholasStatus`, with the shared inserts `nskr_kqNewJob`, `nskr_kqNeedFleet`, `nskr_kqNoJobs`. |
| Speaker menu | `FireAll` menu | `nskr_kqJackOptions`, `nskr_kqAliceOptions`, `nskr_kqNicholasOptions`: story point options, the action option (`nskr_kq_brief`) and Back. |
| Briefing or hand-in | `DialogOptionSelected` pick by stage, speaker and flags | Rows on `$option == nskr_kq_brief`, one per screen (tables below); `nskr_kq_hubBrief` is the fallback with Back only. Paragraphs after the first use `AddText`, so each `SetTextHighlights` follows its own paragraph. Conditional paragraphs are inserts: `nskr_kqFleetDoubt`, `nskr_kqAliceNicholasHint`, `nskr_kqJackTipLine`; the job 5 leads are text inserts on `nskr_kqJackLeadsLines` and `nskr_kqAliceLeadsLines`, which fire `nskr_kqAliceJob3Satellite`, `nskr_kqAliceJob4Satellite` and `nskr_kqDisksLeft`; `nskr_kqDelveUpdated` prints the Delve log line and its sound. |
| Conditional options of a briefing | `FireAll` menu | `nskr_kqAliceFrostOptions` ("It's the <Frost>." when Frost was entered, Leave), `nskr_kqAliceCacheOptions` ("Yes", and "Yes" (lie) with `ELIZA_AGREED_SINCERELY`). The Nicholas briefing ends with the `FireBest` pick `nskr_kqNicholasTipEnd`. |
| Confirmation | `DialogOptionSelected` pick by stage and flags | Rows on `$option == nskr_kq_confirm`; each adds Leave. `nskr_kq_hubConfirm` is the fallback. `nskr_kq_hubConfirmLie` sets `$option` to `nskr_kq_confirm` and fires `DialogOptionSelected` again, so "Yes" (lie) does what "Yes" does. |
| Questions | `FireAll` menu | `nskr_kq_hubAsk` handles `nskr_kq_ask` (the briefing's "I have some questions."), prints "What do you need to know?" and fires `nskr_kqAsk`, whose rows (`nskr_kq_jackAsk`, `nskr_kq_jackAskJob1`, `nskr_kq_aliceAsk`) fire `nskr_kqJackQuestions` or `nskr_kqAliceQuestions`. Each answer row sets an "asked" key on the speaker and fires the questions again. |
| Job 3 refusal | Plain chain | "I'm not doing this." (`nskr_kq_aliceAskRefuse`) → `nskr_kq_aliceAskRefuseSel` ("yes" or "No") → `nskr_kq_aliceRefuseConfirm`. |
| Story skip | `DialogOptionSelected` pick by `$id`, then a shared insert | `nskr_kq_jackStorySkip`, `nskr_kq_aliceStorySkip` print the speaker's lines and fire `nskr_kqStorySkipped`, which runs `do storySkip` and prints the receipts. |
| Requirement skip | Continue chain | `nskr_kq_hubReqSkip` (at `JOB4_WAITING` `nskr_kq_hubReqSkipJob4`, which first sets `JOB4_REQUIREMENT_SKIPPED`) sets `$option` to `nskr_kq_brief` and fires `DialogOptionSelected`, which shows the briefing. |

Option ids:

| Option id | Added by | Handled by |
|---|---|---|
| `nskr_kq_chat` | `nskr_kq_hubChatOption` | `nskr_kq_jackChat`, `nskr_kq_jackChatJob1Tip`, `nskr_kq_aliceChat`, `nskr_kq_nicholasChat` |
| `nskr_kq_brief` | Menu action options, question Back, refusal "No" | Briefing rows |
| `nskr_kq_reqSkip` | Menu rows with `SetStoryOption nskr_kq_reqSkip 1 nskr_skipRequirement` | `nskr_kq_hubReqSkip`, `nskr_kq_hubReqSkipJob4` |
| `nskr_kq_storySkip` | Menu rows with `SetStoryOption nskr_kq_storySkip 5 nskr_skipStory` | `nskr_kq_jackStorySkip`, `nskr_kq_aliceStorySkip` |
| `nskr_kq_ask` | Briefings | `nskr_kq_hubAsk` |
| `nskr_kq_jackAsk…`, `nskr_kq_aliceAsk…` | Question rows | `…Sel` answer rows |
| `nskr_kq_confirm`, `nskr_kq_confirmLie` | Briefings, `nskr_kqAliceFrostOptions`, `nskr_kqAliceCacheOptions` | Confirmation rows, `nskr_kq_hubConfirmLie` |
| `nskr_kq_refuseConfirm` | `nskr_kq_aliceAskRefuseSel` | `nskr_kq_aliceRefuseConfirm` |
| `nskr_kq_exit` | Back and Leave everywhere, with Escape | `nskr_kq_hubExit`: `FireAll PopulateOptions` |

`KestevenHubModule` declarations used by the rows:

| Kind | Name | Meaning |
|---|---|---|
| check | `hubOpen` | The dialog target's market belongs to Kesteven and the player's Kesteven relationship is above -0.50 |
| check | `storySkipUnlocked` | LunaLib setting `storySkipUnlocked` |
| check | `job1Standing`, `job3Standing`, `job4Standing`, `job5Standing` | Kesteven relationship at least `JOB1_REP` … `JOB5_REP` |
| check | `job3Fleet`, `job5Fleet` | `fleetPower()` above `JOB3_POWER`, `JOB5_POWER` |
| check | `job4Fleet` | `JOB4_REQUIREMENT_SKIPPED`, or fleet power above `JOB4_POWER` |
| check | `fleetStretched` | At `JOB3_BRIEFING` or `JOB4_WAITING`, fleet power below that job's gate plus 0.15: the briefing's doubt line |
| check | `job1Cargo` | At least `JOB1_ARTIFACTS` Artifact Electronics in the player's cargo |
| check | `job1SensorReady`, `job1CargoReady` | Sensor task done and not delivered; electronics in cargo and not delivered |
| check | `job1TipKnown`, `job4TargetKnown` | `job1TipSystem`, `job4EnemyTarget` set |
| check | `nicholasTipGiven`, `outpostExists`, `frostVisited` | `nicholasDialogStage` at least 1; the Outpost market exists and is Kesteven's; the player has entered Frost |
| check | `noSatellite`, `oneSatellite`, `twoSatellites`, `disksOverTwo`, `allDisks` | Satellites salvaged 0, 1, at least 2; disks above 2, at least 5 |
| action | `pickJob1Tip`, `pickJob3Start`, `pickJob3Target`, `pickJob4FriendlyTarget`, `pickJob5FrostTip` | The picker of that target, which picks it on first use: `KestevenJob1Module.pickTip`, `KestevenJob3Module.start` and `target`, `KestevenJob4Module.friendlyTarget`, the hub's Frost tip; `pickJob1Tip` also places a dormant Enigma fleet |
| action | `recordNicholasTip` | `nicholasDialogStage` = 1 |
| action | `markJob3Satellite`, `markJob4Satellite` | `ctx.mark` on satellite #3 or satellite #4, from `JOB5_DISKS` on |
| action | `grantModspec` | A random Kesteven modspec the player does not know yet, if any (purpose `kestevenQuestRandom`), through `ctx.rewards().item` |
| action | `grantExchangePoints`, `grantEpoch`, `raiseJackImportance` | 50,000 artifact exchange points (`nskr_shipSwap.addPoints`); the `nskr_epoch_empty` frigate with the vanilla ship receipt; Jack's importance to high |
| action | `placeJob3Leftovers` | The derelicts, debris, satellite #3 and dormant fleet at the job 3 target after a refusal |
| action | `storySkip` | Jumps to `CACHE_KNOWN` with `QuestManager.jump`, then clears `nskr_starfarerFromStart` and sets `STORY_SKIPPED` ([questline](KESTEVEN_QUESTLINE.md#story-skip)) |
| token | `playerFullName`, `job1Payout`, `job3Payout`, `job4Payout` | Player's full name; the stage payouts with `Misc.getDGSCredits`. The job 1 briefing also uses `KestevenJob1Module`'s token `job1ArtifactCount` (`JOB1_ARTIFACTS`) |
| token | `job1TipSystem`, `job3Start`, `job3Market`, `job3TargetSystem`, `job4Constellation`, `job4TargetSystem`, `outpostName` | Names from the saved targets: tip system; job 3 start entity and its market; job 3 target system; friendly target constellation; strike group system; Outpost |
| token | `frostName`, `frostTipConstellation`, `frostTipDistance` | Frost's name; the hint system's constellation (`KestevenQuest.constellationName`); the distance from it to Frost times 1.5 in light-years, rounded to two decimals and printed as a Java float |

Targets are picked where the old dialog first read them: the job 1 tip when Jack's menu opens at `JOB1_ACTIVE` with neither task done and no tip given (`nskr_kq_jackChatJob1Tip`) and when his questions open at `NOT_STARTED` (`nskr_kq_jackAskJob1`); the job 3 start market by the job 3 briefing; the friendly target by the job 4 briefing; the job 3 target by Alice's leads; the hint system by Alice's Frost tip. An action runs first in the row's Script and the text follows with `AddText`, so the tokens read the picked value. Checks and tokens never pick.

`job1Progress` (declared by `KestevenJob1Module`) moves `JOB1_ACTIVE` to `JOB1_DONE` once both deliveries are recorded and refreshes the job 1 map marker; the hand-in rows and the tip row run it after setting their flags.

The stage payouts and the 70 electronics are constants of `KestevenHubModule`; the rows pay and take them with `AddCredits` and `AddRemoveCommodity` and the same literal amounts, and the notes column of those rows names the constant.

### Decision tables

Conditions are counted as `FireBest` scores: one per line, `score:10` where noted. "Standing" and "fleet" are the checks above. A stage not listed shows the fallback. No two rows tie; no row is a random variant.

**Jack** (`nskr_opguy`). Greeting: `nskr_kq_jackIntro` (1, `!$nskr_kq_introduced`, sets it) or `nskr_kq_jackGreeting` (1, `$nskr_kq_introduced`).

| Stage and state | Status row (score) | Options, in order |
|---|---|---|
| Any stage without a match | `nskr_kq_jackStatus` (0): `nskr_kqNoJobs` | Back |
| `NOT_STARTED`, no standing | `…Job1Standing` (2) | Story skip*, Back |
| `NOT_STARTED`, standing | `…Job1Offer` (2): `nskr_kqNewJob` | Story skip*, "Enemy Unknown", Back |
| `JOB1_ACTIVE`, tip given, no hand-in ready | `…Job1Tasks` (4) | Back |
| `JOB1_ACTIVE`, no sensor data, no cargo, no tip, tip known | `…Job1Tip` (5) | "How am I supposed to find them?", Back |
| `JOB1_ACTIVE`, sensor ready only | `…Job1Package` (2) | "Hand over the package", Back |
| `JOB1_ACTIVE`, cargo ready only | `…Job1Cargo` (2) | "Hand over the electronics", Back |
| `JOB1_ACTIVE`, both ready | `…Job1Both` (3) | "Hand over everything", Back |
| `JOB1_DONE` | `…Job1Done` (1) | "I've done everything.", Back |
| `JOB3_OFFERED`, no standing | `…Job3Standing` (2) | Story skip*, Back |
| `JOB3_OFFERED`, standing, fleet | `…Job3Offer` (3): `nskr_kqNewJob` | Story skip*, "Hostile Takeover", Back |
| `JOB3_OFFERED`, standing, no fleet | `…Job3Fleet` (3): `nskr_kqNeedFleet` | Requirement skip, Story skip*, Back |
| `JOB3_BRIEFING` | `…Job3Alice` (1) | Back |
| `JOB3_ACTIVE`, `JOB3_TARGET_KNOWN`, `JOB3_DONE`, `JOB4_ACTIVE`, `JOB4_DONE` | `…AliceWork` (1) | Back |
| `JOB4_WAITING`, wait over | `…Job4` (2) | Back |
| `JOB5_OFFERED`, no standing | `…Job5Standing` (2) | Story skip*, Back |
| `JOB5_OFFERED`, standing, fleet | `…Job5Offer` (3) | "I'm listening.", Back |
| `JOB5_OFFERED`, standing, no fleet | `…Job5Fleet` (3): `nskr_kqNeedFleet` | Requirement skip, Story skip*, Back |
| `JOB5_MEETING` | `…Bar` (1) | Back |
| `JOB5_DISKS`, no Jack tip, not all disks | `…Leads` (3) | "Okay", Back |
| `JOB5_DISKS` to `CHIP_RECOVERED`, Jack tip | `…NothingNew` (2) | Back |
| `JOB5_DISKS`, both tips, not tip 2, two satellites, not all disks | `…AliceTip` (6) | Back |
| `JOB5_DISKS`, all disks | `…AllDisks` (1 + 10) | Back |
| `COMPLETED` | `…Completed` (1), not reachable: the entry option is hidden | Back |

**Alice** (`nskr_researcher`). Greeting from `JOB3_BRIEFING` on: `nskr_kq_aliceIntro` (2, sets `$nskr_kq_introduced`) or `nskr_kq_aliceGreeting` (2). Before it, no greeting row matches.

| Stage and state | Status row (score) | Options, in order |
|---|---|---|
| Any stage without a match | `nskr_kq_aliceStatus` (0): `nskr_kqNoJobs` | Back |
| `NOT_STARTED` to `JOB3_OFFERED` | `…Busy` (1) | Back |
| `JOB3_BRIEFING` | `…Job3` (1) | Story skip*, "Hostile Takeover", Back |
| `JOB3_ACTIVE`, `JOB3_TARGET_KNOWN` | `…Job3Active` (1) | Back |
| `JOB3_DONE`, success or `JOB3_FAILED` | `…Job3Success` or `…Job3Failed` (2) | "Hand over your AAR", Back |
| `JOB4_WAITING`, wait over, no standing | `…Job4Standing` (3) | Story skip*, Back |
| `JOB4_WAITING`, wait over, standing, fleet | `…Job4Offer` (4): `nskr_kqNewJob` | Story skip*, "Operation Lifesaver", Back |
| `JOB4_WAITING`, wait over, standing, no fleet | `…Job4Fleet` (4): `nskr_kqNeedFleet` | Requirement skip, Story skip*, Back |
| `JOB4_ACTIVE` | `…Job4Active` (1) | Back |
| `JOB4_DONE` | `…Job4Done` (1) | "Hand over the fleets coordinates and your combat log", or with `JOB4_FRIENDLY_HELPED` "Hand over your operational report"; Back |
| `JOB5_OFFERED` | `…Job5` (1) | Back |
| `JOB5_MEETING` | `…Bar` (1) | Back |
| `JOB5_DISKS`, no Alice tip, not all disks | `…Leads` (3) | "Okay", Back |
| `JOB5_DISKS` to `CHIP_RECOVERED`, Alice tip | `…ToldAll` (2) | Back |
| `JOB5_DISKS`, both tips, not tip 2, two satellites, not all disks | `…NewLead` (6) | "Continue", Back |
| `JOB5_DISKS`, all disks | `…AllDisks` (1 + 10) | "Continue", Back |
| `COMPLETED` | `…Completed` (1), not reachable | Back |

**Nicholas** (`nskr_intelligence`). Greeting inserts: `nskr_kq_nicholasGreeting` always, then `nskr_kq_nicholasIntro` from `JOB4_ACTIVE` on while `$nskr_kq_introduced` is unset.

| Stage and state | Status row (score) | Options |
|---|---|---|
| Every stage but `JOB4_ACTIVE` | `nskr_kq_nicholasStatus` (0) | Back |
| `JOB4_ACTIVE` | `…Job4` (1) | Back |
| `JOB4_ACTIVE`, strike group location known, tip not given | `…Job4Tip` (3) | "Tell me what you know.", Back |

\* Story skip only while `storySkipUnlocked` is on. The requirement skip costs 1 story point, the story skip 5; `SetStoryOption` disables either when the player has fewer points.

**Questions.** Each question hides once its "asked" key is set on the speaker; the tip and "LZ" questions hide through their quest flags instead.

| Speaker and stage | Questions (hidden by) | Answer side effects |
|---|---|---|
| Jack, `NOT_STARTED` | "How am I supposed to find them?" (tip known; `JOB1_TIP_GIVEN`), "Rogue AI?" (`askedRogueAi`), "The ships?" (not `FOUGHT_ENIGMA`; `askedShips`), "Artifact Electronics?" (`askedElectronics`), "What about the AI Cores?" (`askedCores`), "I've already fought them." (`FOUGHT_ENIGMA`; `askedFought`) | Tip: sets `JOB1_TIP_GIVEN` |
| Jack, `JOB1_DONE` | "What are you actually doing with this equipment?" (`askedEquipment`), "Enigma AI?" (`askedEnigma`), "Next job?" (`askedNextJob`) | |
| Alice, `JOB3_BRIEFING` | "How do I know where to go?" (`askedWhere`), "So I'm on my own for this?" (`askedAlone`), "Is this really necessary?" (`askedNecessary`), "I'm not doing this." (never hidden) | Refusal prompt |
| Alice, `JOB3_DONE` | "Know anything about the target?" (`JOB3_TARGET_DISCOVERED`; `askedTarget`), "Next job?" (`askedNextJob`) | |
| Alice, `JOB4_WAITING` | "Special Operations fleet?", "Possible threats?", "Supplies and fuel?", "Nicholas Antoine?" (`askedSpecOps`, `askedThreats`, `askedSupplies`, `askedNicholas`), Ask about the "LZ" character (`MESSENGER_QUESTION_OPEN`) | "LZ": clears `MESSENGER_QUESTION_OPEN` |
| Alice, `JOB4_DONE` | "What was the Operations fleet's goal?", "Why are you so interested in this Enigma AI?", "The Artifact?" (`askedGoal`, `askedInterest`, `askedArtifact`), Ask about the "LZ" character | "LZ": clears `MESSENGER_QUESTION_OPEN` |

Every question list ends with Back (`nskr_kq_brief`, Escape), which shows the briefing again.

**Briefings and hand-ins** (rows on `$option == nskr_kq_brief`; the `$option` line is counted). The stage decides; a speaker is named where the rows test `$id`.

| Stage and state | Row (score) | Effects | Options |
|---|---|---|---|
| Any stage without a match | `nskr_kq_hubBrief` (1) | | Back |
| `NOT_STARTED` | `nskr_kq_jackJob1Brief` (2) | | Accept, "I have some questions.", Back |
| `JOB1_ACTIVE`, both hand-ins ready | `…jackJob1HandInBoth` (4) | Takes 70 electronics, "Lost sensor package", both delivered flags, `do job1Progress` | Back |
| `JOB1_ACTIVE`, electronics ready | `…jackJob1HandInCargo` (3) | Takes 70 electronics, `JOB1_ELECTRONICS_DELIVERED`, `do job1Progress` | Back |
| `JOB1_ACTIVE`, sensor data ready | `…jackJob1HandInPackage` (3) | "Lost sensor package", `JOB1_DATA_DELIVERED`, `do job1Progress` | Back |
| `JOB1_ACTIVE`, nothing delivered, no tip, tip known, nothing ready | `…jackJob1Tip` (8) | Tip line, `JOB1_TIP_GIVEN`, `do job1Progress` | Back |
| `JOB1_DONE` | `…jackJob1Done` (2) | | Continue, "I have some questions." |
| `JOB3_OFFERED` | `…jackJob3Brief` (2) | | Continue |
| `JOB3_BRIEFING` | `…aliceJob3Brief` (2) | `pickJob3Start`; doubt line when `fleetStretched` | Accept, "I have some questions.", Back |
| `JOB3_DONE`, success / `JOB3_FAILED` | `…aliceJob3Report` / `…aliceJob3Failed` (3) | | Continue, "I have some questions." |
| `JOB4_WAITING` | `…aliceJob4Brief` (2) | `pickJob4FriendlyTarget`; Nicholas line when `outpostExists`; doubt line when `fleetStretched` | Accept, "I have some questions.", Back |
| `JOB4_ACTIVE` | `…nicholasJob4Brief` (2) | `recordNicholasTip`; ends with the log line or, with `JOB4_TARGET_FOUND`, "already investigated" | Leave |
| `JOB4_DONE`, not helped / `JOB4_FRIENDLY_HELPED` | `…aliceJob4Report` / `…aliceJob4Helped` (3) | | Continue, "I have some questions." |
| `JOB5_OFFERED` | `…jackJob5Brief` (2) | `advance JOB5_OFFERED JOB5_MEETING` | Leave |
| `JOB5_DISKS`, Jack, no Jack tip | `…jackLeads` (4) | Eliza and disk lines, Delve log line, `JOB5_JACK_TIP` | Leave |
| `JOB5_DISKS`, Alice, no Alice tip | `…aliceLeads` (4) | `pickJob3Target`; satellite lines (marks the missing satellite when one is salvaged), disk lines, Delve log line, `JOB5_ALICE_TIP` | Leave |
| `JOB5_DISKS`, Alice, both tips, not tip 2 | `…aliceFrostTip` (6) | `pickJob5FrostTip`, Delve log line, `JOB5_ALICE_TIP2`, `markGlacier` | "It's the <Frost>." when Frost was entered, Leave |
| `JOB5_DISKS`, Alice, both tips, tip 2, all disks | `…aliceCacheBrief` (7) | | "Yes", "Yes" (lie) with `ELIZA_AGREED_SINCERELY` |

**Confirmations** (rows on `$option == nskr_kq_confirm`; every one adds Leave).

| Stage and state | Row (score) | Effects |
|---|---|---|
| Any stage without a match | `nskr_kq_hubConfirm` (1) | |
| `NOT_STARTED` | `nskr_kq_jackJob1Accept` (2) | Log line, `advance NOT_STARTED JOB1_ACTIVE` |
| `JOB1_DONE` | `…jackJob1Paid` (2) | 155,000 credits, Kesteven +5, Jack +10 (at most cooperative), modspec, `advance JOB1_DONE JOB3_OFFERED` |
| `JOB3_OFFERED` | `…jackJob3Handoff` (2) | `advance JOB3_OFFERED JOB3_BRIEFING`, Jack as potential contact |
| `JOB3_BRIEFING` | `…aliceJob3Accept` (2) | Log line, `advance JOB3_BRIEFING JOB3_ACTIVE` |
| `JOB3_DONE`, success | `…aliceJob3Paid` (3) | 205,000 credits, Kesteven +5, Alice +10, 50,000 exchange points, modspec, `advance JOB3_DONE JOB4_WAITING` |
| `JOB3_DONE`, `JOB3_FAILED` | `…aliceJob3Penalty` (3) | Kesteven -5, Alice -10 (at least vengeful), `advance JOB3_DONE JOB4_WAITING` |
| `JOB4_WAITING` | `…aliceJob4Accept` (2) | Log line, `advance JOB4_WAITING JOB4_ACTIVE` |
| `JOB4_DONE`, not helped / helped | `…aliceJob4Paid` / `…aliceJob4PaidHelped` (3) | 1 story point, 285,000 credits, Kesteven +5, Alice +10, the Epoch frigate if helped, modspec, `advance JOB4_DONE JOB5_OFFERED`, Alice as potential contact, Jack's importance high |
| `JOB5_DISKS`, both tips, not all disks | `…aliceFrostFound` (4) | Delve log line, `FROST_FOUND` |
| `JOB5_DISKS`, all disks | `…aliceCacheFound` (3) | `CACHE_FOUND`, `advance JOB5_DISKS CACHE_KNOWN` |

The two `JOB5_DISKS` confirmations test the disks, not which screen offered the option: "It's the <Frost>." on the Frost tip screen with all five disks gives the Cache coordinates, as the old `quest()` did.

## Data-disk satellites

The dialog of every satellite is the `# KESTEVEN QUESTLINE: SATELLITES` block of `data/campaign/rules.csv`, opened through the claim `KestevenSatelliteModule` places on each satellite (trigger `nskr_kqSatellite`). Behavior is in [The five data disks](KESTEVEN_QUESTLINE.md#the-five-data-disks).

| Screen | Structure | Trigger and rows |
|---|---|---|
| Opening | `FireBest` pick on `nskr_kqSatellite`; each row shows the satellite's image (`ShowDefaultVisual`) | `nskr_kq_satelliteEmpty` (check `satelliteEmpty`): "cold and dead", Leave. `nskr_kq_satelliteFirst` (not empty, hub check `noSatellite`): Leave, "Take a closer look". `nskr_kq_satelliteSecond` (not empty, hub check `oneSatellite`): "See if this one also has a disk to recover", Leave. `nskr_kq_satelliteSilent`, the fallback: Leave only |
| First satellite | Plain chain | `nskr_kq_satelliteLookSel`, `…SendSel`, `…InspectSel`, `…PrepareSel`, `…ProceedSel`, `…UnplugSel`, `…ReportSel`. The report paragraph is a `FireBest` pick on `nskr_kqSatelliteDiskReport`: `nskr_kq_satelliteDiskReportJob5` from `JOB5_DISKS` on and at `FAILED`, `nskr_kq_satelliteDiskReport` before |
| Second satellite | Plain chain | `nskr_kq_satelliteSearchSel`, `…ScanSel`, `…RetrieveSel`, `…BroadcastSel`. The last runs the hub action `pickJob5FrostTip` before the keyword line, which uses the tokens `keywordFrost` and `keywordConstellation` |
| Salvage | Shared insert on `nskr_kqSatelliteSalvaged`, fired with `FireBest` by both last screens | `nskr_kq_satelliteSalvaged`: actions `salvageSatellite` and `wakeSatelliteGuard`, `Ping sensor_burst` and `Ping interdict`, the receipt "Acquired Data Disk #<n>" (token `satelliteDisk`), `ui_rep_raise`; adds Leave with Escape |
| Leave after the salvage | `DialogOptionSelected` | `nskr_kq_satelliteLeaveSel`: `ui_sensor_burst_on`, `DismissDialog` |

Every other Leave is vanilla's `defaultLeave`, which also takes Escape. Paragraphs after the first use `AddText`, as the Java dialog printed one paragraph per call; grey paragraphs use `AddText … gray`. The Java dialog called `setFontInsignia` once at the start; the rows need no equivalent, because vanilla's `AddTextSmall` also switches back to the insignia font after its line (0.98a-RC8 `AddTextSmall.execute`).

`KestevenSatelliteModule` declarations used by the rows:

| Kind | Name | Meaning |
|---|---|---|
| trigger | `nskr_kqSatellite` | Fired by the claimed satellite's dialog when it opens |
| check | `satelliteEmpty` | The dialog target holds `$nskr_artifactKeyEmpty` |
| action | `salvageSatellite` | Counts the satellite and one disk (then `checkAllDisks`), marks the target empty, sets `SATELLITE3_RECOVERED` or `SATELLITE4_RECOVERED`, removes its map marker |
| action | `wakeSatelliteGuard` | Satellite #3: wakes and adopts the location's Enigma dormant fleets as role `satelliteGuard`; #4: the strike group. Both then intercept the player |
| token | `satelliteDisk` | `3` when the target holds `$kQuestArtifact3`, otherwise `4` |
| token | `keywordFrost`, `keywordConstellation` | `Frost.getName()`; the constellation name of `job5FrostTipSystem`, empty without one |

## Glacier

The comms facility raid for disk #5 is the `# KESTEVEN QUESTLINE: GLACIER` block of `data/campaign/rules.csv`. Alice's Frost tip row runs `do markGlacier`, which `KestevenGlacierModule` declares: it marks Glacier and claims its dialog with `nskr_kqGlacier`, so `CorePlugin` opens a rules dialog that fires that trigger ([flow and countdown](KESTEVEN_QUESTLINE.md#glacier-disk-5)). The speaker is the player's own crew; no person is shown.

| Screen | Structure | Rows |
|---|---|---|
| Approach | Entry row | `nskr_kq_glacierOpen` on `nskr_kqGlacier`: `ShowLargePlanet`, "Search for the facility" and `defaultLeave` Leave |
| Search to the alarm | Plain chain | `nskr_kq_glacierSearchSel` (Continue and Leave), `…LandSel`, `…DigSel`, `…HallsSel`. From the landing on, every screen runs `ShowPic nskr_glacier`. The alarm's two spoken replies (`…ComplicatesSel`, `…CurseSel`) set `$option` to `nskr_kq_glacierRush` and fire `DialogOptionSelected`, so all three options show the same next screen |
| Countdown screens | Plain chain; the minutes are literal `AddTextSmall … gray` lines with `SetTextHighlights` on the number | `…RushSel`, `…ImpasseSel`, the door handlers `…WalkSel`, `…CutSel`, `…BlastSel` (they write `$nskr_kq_glacierPath`), `…ServersSel`, `…ConsoleSel` |
| Minutes that depend on the door | `FireBest` pick, fallback for walk and cut | `nskr_kqGlacierServersTime`, `nskr_kqGlacierConsoleTime`, `nskr_kqGlacierDefuseTime` |
| Way back after running | `FireBest` pick by `$nskr_kq_glacierPath`, fallback for the cut | `nskr_kqGlacierRunBack`; the walk and blast rows set `$nskr_kq_glacierLate` |
| Shutdown result | `DialogOptionSelected` pick | `nskr_kq_glacierDefusedSel` (in time, blast only) and `…DefusedLateSel` (`$nskr_kq_glacierLate`: barrage, `do damageFleet`) |
| Return to the fleet | Handler with `defaultLeave` Continue, then picks | `nskr_kq_glacierReturnSel` fires `nskr_kqGlacierShuttle` (run or shutdown line) and `FireBest nskr_kqGlacierEscape true` (barrage after a late run; congratulations with "I know, I'm the best." when not late; an empty fallback after a late shutdown), then runs `do recoverGlacierDisk` and prints "Acquired Data Disk #5" |
| Shared lines | Shared insert; line trigger | `nskr_kqGlacierRedAlert`, the first line of both barrages; `nskr_kqGlacierHit`, fired by `damageFleet` once per damaged ship with the tokens `$nskr_kq_glacierHitShip` and `$nskr_kq_glacierHitHull` |

`KestevenGlacierModule` declarations: the triggers `nskr_kqGlacier` and `nskr_kqGlacierHit`; the actions `markGlacier`, `damageFleet` and `recoverGlacierDisk`; the tokens `glacierHitShip` and `glacierHitHull`.

## Eliza search at pirate bars

Block `# KESTEVEN QUESTLINE: ELIZA SEARCH` of `data/campaign/rules.csv`, with `kesteven/quest/KestevenElizaSearchModule` declaring its checks, actions, tokens and people. Flow and state are in [Finding Eliza](KESTEVEN_QUESTLINE.md#finding-eliza).

| Conversation | Entry row (`AddBarEvents`) | Speaker | Screens and handlers |
|---|---|---|---|
| First spacer | `nskr_kq_elizaSpacerBar` (`check elizaSpacerHere`) | `nskr_kq_roughSpacer` | `nskr_kq_elizaSpacer` (runs `elizaSpacerOpen`) → `…Ask`, a `FireAll` menu `nskr_kqElizaSpacerOptions` whose credits option needs `check elizaSpacerAffordable` → `…Hello` or `…Credits` → `…Pay` (`elizaPickContact`, the contact line, `elizaSpacerPay` with the credits receipt, the shared insert `nskr_kqDelveUpdated`) → `…PaidLeave`; or `…Leave` |
| Second spacer | `nskr_kq_elizaSlyBar` (`check elizaSlySpacerHere`) | `nskr_kq_slySpacer` | `nskr_kq_elizaSly` → `…Ask` with five answers (`…Kesteven`, `…Paying`, `…Eliza`, `…Comsec` as a continue chain into `…Eliza`, `…Myself`), each ending with the shared insert `nskr_kqElizaSlyGone` → `…Leave` |
| Contact | `nskr_kq_elizaContactBar` / `…BarPaid` (`check elizaContactHere`, and `flag ELIZA_SPACER_PAID` for the second option label) | `nskr_kq_pirateContact` | `nskr_kq_elizaContact` → `…Sit` (`elizaContactMeet`) → `…Where` (`elizaPickMarket`, then the line naming the market) → `…Leave` (`elizaContactLeave`) |

The blurb of the contact uses the person tokens `$nskr_kq_pirateContact_manOrWoman` and `_hisOrHer`, because no person is active while the bar lists its events; the conversations use the vanilla pronoun tokens of the active speaker. Every exit runs `HideVisual` and `BarCMD returnFromEvent true`. The intel row `nskr_kq_elizaContactMovedBullet` is the Delve update `contactMoved`.

## The Delve meeting

The in-person meeting of job 5 is a rules bar event in the `# Meeting` part of the `# KESTEVEN QUESTLINE: JOB 5` block; [the questline page](KESTEVEN_QUESTLINE.md#briefing-and-meeting) describes what it does. `KestevenJob5Module` declares its checks, its action, its token and the guard.

| Screen | Structure | Rows |
|---|---|---|
| Bar entry | `AddBarEvents` row | `nskr_kq_delveBar`: stage `JOB5_MEETING` and `check delveMeetingHere` (the bar's market is `asteriaOrOutpost`); `AddBarEvent nskr_kq_delveSignal` with the option in the highlight colour. No person is active when the option is clicked. |
| The escort | Plain chain with a `FireBest` pick | `nskr_kq_delveSignal` fires `nskr_kqDelveEscort`: `nskr_kq_delveEscortAsteria` (`check asteriaGenerated`: `ShowLargePlanet`, the city) or the fallback `nskr_kq_delveEscort` (the guard's card, `ShowPersonVisual true nskr_kq_delveGuard`). |
| Arrival | Plain chain with a `FireBest` pick | `nskr_kq_delveArrive`: `HideSecondPerson`, `ShowPic nskr_crib`, the room from `nskr_kqDelveRoom` (`nskr_kq_delveRoomAsteria` or `nskr_kq_delveRoom`), then Jack and Alice. |
| The drink | Plain chain | `nskr_kq_delveGreet` makes Jack the speaker (`BeginConversation nskr_opguy true false`) and shows Alice (`ShowSecondPerson nskr_researcher`); `nskr_kq_delveDrink`, `nskr_kq_delveDrinkIt`, `nskr_kq_delveGreat` (`nskr_kq_delveHate` continues into it), `nskr_kq_delveNoDrink`. |
| Business and questions | `FireAll` menu | `nskr_kq_delveBusiness` fires `nskr_kqDelveQuestions`: "Keep listening" (`nskr_kq_delveOptListen`; "Continue" after a question, `nskr_kq_delveOptContinue`), the four questions while unasked and `CACHE_FOUND` unset, or only "I think I already found that place." (`nskr_kq_delveOptFound`) with `CACHE_FOUND`. The answers `nskr_kq_delveAsk…` set `$nskr_kq_delveAsked…` in Jack's memory with expiry `0` and fire the menu again. `nskr_kq_delveOptEnigma` sets its label with `SetOptionText`, because the Options column cannot hold its colon. |
| The job | Plain chains; a gate row removes one option | `nskr_kq_delveListen` (its gate `nskr_kqDelveListenGate` removes "I already have some of those disks." while `check noSatellite`), `nskr_kq_delveDoubt` (the advance offer: token `delveAdvanceCredits`, key `$nskr_kq_delveAdvanceOffered`), `nskr_kq_delvePrizes`, `nskr_kq_delveHaveDisks`. |
| Eliza | Plain chain with a text insert and a gate row | `nskr_kq_delveAgree` fires `nskr_kqDelveAdvance` first (`nskr_kq_delveAdvancePaid`: `AddCredits 150000` and `ui_noise_static` after the offer), then its lines in `AddText`; its gate `nskr_kqDelveElizaGate` removes the "LZ" option unless `MESSENGER_QUESTION_OPEN`. `nskr_kq_delveTerrorist` answers; `nskr_kq_delveLowlife` and `nskr_kq_delveLz` continue into it. `nskr_kq_delveElizaNoted` closes the talk. |
| Cache already found | Plain chain | `nskr_kq_delveFound` (action `markCacheCore`), `nskr_kq_delveFoundMore`. |
| Leave | Exit row with a `FireBest` pick | `nskr_kq_delveLeave`: the log line and its sound, the departure from `nskr_kqDelveDeparture` (`nskr_kq_delveDepartureAsteria` or `nskr_kq_delveDeparture`), `HideSecondPerson`, `ShowPic nskr_crib`, `advance JOB5_MEETING JOB5_DISKS`, `BarCMD returnFromEvent true`. |

## The Cache

The `# KESTEVEN QUESTLINE: CACHE` block of `data/campaign/rules.csv` holds the Cache intel text, the inner voice and the command core; `KestevenCacheModule` declares their triggers, checks, actions and people ([flow](KESTEVEN_QUESTLINE.md#reaching-the-cache)).

| Part | Structure | Rows |
|---|---|---|
| Intel entry `cache` | Intel rows | `nskr_kq_cacheIntelTitle`, `…IntelExplore` (bullet until `CHIP_SALVAGED`), `…IntelDescFound` before the salvage, `…IntelDescLogs` and `…IntelLog1` to `5` after it |
| Inner voice, entry | Plain chain | `nskr_kq_doubtOpen` on `nskr_kqCacheDoubt`, which `KestevenCacheModule` opens on the player fleet. The voice's lines are gray `AddText`. "I'll figure it out." and "It's puzzling." (`…FigureSel`, `…PuzzlingSel`) set `$option` to `nskr_kq_doubtTakeNote` and fire `DialogOptionSelected`; "Shut up" leads to `…ShutUpSel` and `…AskSel` |
| Talking to the crew | Plain chain | `…TakeNoteSel`, `…ChiefSel` and `…SensorsSel` (`ShowPersonVisual true nskr_kq_cacheChief` or `nskr_kq_cacheSensors`; they set `$nskr_kq_doubtAskedChief` or `$nskr_kq_doubtAskedSensors`), `…SomethingSel` (`$nskr_kq_doubtTip`), `…RecheckSel` |
| Back to the thought | Handlers with `HideVisual`, then a `FireAll` menu | `…BackSel` (with the `FireBest` pick `nskr_kqDoubtThought`), `…MatterSel` (`…InterestingSel` chains to it), `…DoomedSel` (sets `$nskr_kq_doubtGaveUp`, which the other two unset) fire `nskr_kqDoubtOptions`: the crew member not asked yet, "I just need to look harder." and "I don't give up so easily." once both were asked without the tip or giving up, "Lets get to work." with the tip, "I give up." after "It's doomed", and "Dismiss thought". `…GiveUpSel` offers "Maybe it's not that bad." back to the menu |
| Exits | Handlers | `…DismissSel`, `…HarderSel`, `…PersistSel`, `…WorkSel`, `…RightSel` run `do endCacheDoubt`, which releases the two crew members, and `DismissDialog`. Escape takes "Dismiss thought" on every screen that offers it |
| Command core, entry | `FireBest` pick on `nskr_kqCacheCore` | `nskr_kq_coreFirst` (the first visit: `CORE_SEEN`, "Approach the command core"), `…coreQuest` (`check cacheQuestTarget`, not salvaged), `…coreSalvaged`, `…coreWreckage` (fallback) |
| Core screens | Shared inserts and a plain chain | `nskr_kqCoreSalvage` (the salvage offer and Leave) and `nskr_kqCoreWreckage` (one line and Leave), fired by the entry rows and by `…coreApproachSel` or `…coreApproachWreckageSel`; `…coreSalvageSel`, then `…coreChipSel`: the chip line, `AddRemoveCommodity alpha_core 1`, `do salvageCacheCore` (Artifact Electronics with the vanilla receipt, `CHIP_SALVAGED`, stage 19, the markers) and `ui_rep_raise`. The screens after the first run `ShowDefaultVisual`, the core's image |

`KestevenCacheModule` declarations used by the rows: the triggers `nskr_kqCacheCore` and `nskr_kqCacheDoubt`; the checks `cacheQuestTarget` (from `JOB5_DISKS` on while the questline runs) and `cacheIntelDeletable` (the intel's delete button); the actions `endCacheDoubt` and `salvageCacheCore`; the people `cacheChief` and `cacheSensors`.

## Fleet conversations

| Fleet | Rows | Command verbs |
|---|---|---|
| Special Operations and strike group (job 4) | `# KESTEVEN QUESTLINE: JOB 4`; see [Job 4 rows](#job-4-rows) | `nskr_quest kq` |
| Tri-Tachyon collector | `# KESTEVEN QUESTLINE: COLLECTOR`; see [Tri-Tachyon collector rows](#tri-tachyon-collector-rows) | `nskr_quest kq` |
| Eliza after the raid | `nskr_kq_elizaRaidedInit`, `…Greeting`, `…End`, on role flag `$nskr_kq_elizaRaided` | none |
| Eliza's intercept (stage 19) | `nskr_kq_elizaInterceptInit` and `…Greeting`, each also for the returning role (`…InitReturning`, `…GreetingReturning`), `…Talk`, the `FireAll` menu `nskr_kqElizaInterceptOptions` (`…OptHandOver` and `…OptRefuse` unless `check elizaChipGone`, `…OptNoChip` with it), `…HandOver`, `…Refuse`, `…NoChip`, `…Exit` | `nskr_quest kq do elizaAggro`, `elizaTalked`, `elizaChipHandOver`, `elizaHostile`; token `elizaHomeMarket` |
| Eliza's revenge | `nskr_kq_elizaRevengeInit`, `…Greeting`, `nskr_kqElizaRevengeOptions`, `…Misunderstanding`, `…Bootlicker`, `…Die`, `…End`, on role flag `$nskr_kq_elizaRevenge` | none |
| Jack's revenge | `# KESTEVEN QUESTLINE: AFTERMATH`: `nskr_kq_jackRevengeInit`, `…Greeting`, `…End`, on role flag `$nskr_kq_jackRevenge` | none |
| Cache guardian | `cacheDialogInit`, `cacheDialogInitial`, `cacheDialogEnd` | none |
| "LZ" messenger | Quest `ic`, not the questline: `nskr_ic_messengerHail`, `…Open`, `…LeavingOpen`, `…Message`, `…Cut` in `# INTERCEPTS` ([intercept fleets](CONTRACTS_AND_BOUNTIES.md#intercept-fleets)); the message runs `nskr_quest ic do messengerMet`, which calls `KestevenQuest.reportMessengerMet()` | `nskr_quest ic` |

### Job 4 rows

The `# KESTEVEN QUESTLINE: JOB 4` block holds the job's conversations and intel text; `KestevenJob4Module` declares what they use.

| Conversation | Entry | Rows | Structure |
|---|---|---|---|
| Special Operations fleet | `BeginFleetEncounter` and `OpenCommLink` on the role flag `$nskr_kq_job4SpecialOps`, at `JOB4_ACTIVE` or `JOB4_DONE`, Kesteven relationship above -0.50, transponder on | `nskr_kq_job4FleetHail` (hail until `JOB4_FRIENDLY_TALKED`), `…Open` (first talk), `…Ask` with the insert `nskr_kqJob4FleetStrikeGroup` (strike group coordinates unless `JOB4_TARGET_FOUND`), `…OpenAgain` (talked, not helped), `…Help` with the pick `nskr_kqJob4FleetHelp` (`…HelpShort` fallback, `…HelpReady` on `check job4CanHelp`), `…HelpConfirm`, `…Exit` | Plain chain; the help screen is a `FireBest` pick |
| Strike group | `BeginFleetEncounter` and `OpenCommLink` on `$nskr_kq_job4StrikeGroup` | `nskr_kq_job4StrikeGroupHail`, `…Open`, `…Listen`, `…Ask` and `…Hello` (continue chains to `…Listen`), `…Cut` | Plain chain |
| Hint wreck | The claimed trigger `nskr_kqHintWreck` on the first derelict of `KestevenJob4Module.placeWrecks` | `nskr_kq_hintWreckOpen`, `…Read` with the pick `nskr_kqHintWreckResult` (`…Coordinates` fallback, `…Found` on `JOB4_FRIENDLY_FOUND`), `…Leave` | Plain chain |

| Kind | Name | Does |
|---|---|---|
| check | `job4CanHelp` | The player has at least 250 supplies and 400 fuel (`HELP_SUPPLIES`, `HELP_FUEL`) |
| check | `job4FriendlyNamed`, `job4TargetNamed` | The friendly or enemy target entity has a name other than "Null" (intel bullet variants) |
| action | `recordJob4FleetTalk` | Sets `JOB4_FRIENDLY_TALKED` and `JOB4_FRIENDLY_FOUND`, and `JOB4_TARGET_HINT` unless the strike group was seen or beaten |
| action | `sendJob4FleetHome` | Moves the Special Operations fleet to role `job4SpecialOpsLeaving`, bound for `asteriaOrOutpost` |
| action | `readHintWreck` | Sets `JOB4_HINT_WRECK_READ` and releases the wreck's claim |
| token | `job4FriendlySystem`, `job4FriendlyEntity`, `job4TargetEntity`, `job4OutpostSystem`, `job4SearchArea`, `job4Supplies`, `job4Fuel` | Names from the job 4 targets and the Outpost; the constellation with its type (`KestevenQuest.constellationName`); the player's supplies and fuel in whole units |

The hand-over uses `AddRemoveCommodity`, `AdjustRep kesteven 5` and `AdjustRepActivePerson COOPERATIVE 10`, whose vanilla receipts replace the old custom receipt lines. The rows also use the hub's `outpostExists`, `nicholasTipGiven` and `job4TargetKnown`, the job 1 module's `kestevenHostile` and `homeName`, and the hub token `job4TargetSystem`.

### Tri-Tachyon collector rows

The `# KESTEVEN QUESTLINE: COLLECTOR` block holds the collector's conversation; its checks, action and token come from the shared modules `KestevenCollector` configures ([Tri-Tachyon collector](KESTEVEN_QUESTLINE.md#tri-tachyon-collector)).

| Row | Entry or condition | Does |
|---|---|---|
| `nskr_kq_ttCollectorHail` | `BeginFleetEncounter`: Tri-Tachyon, hostile, role flag `$nskr_kq_ttCollector`, no `$ignorePlayerCommRequests` | Hails the player |
| `nskr_kq_ttCollectorOpen` | `OpenCommLink`: Tri-Tachyon, hostile, `$entity.nskr_kq_ttCollector`, `COLLECTOR_PAID` unset | The demand; Continue |
| `nskr_kq_ttCollectorDemand` | Continue | Fires the `FireBest` pick `nskr_kqTtCollectorDemand`, then sets `$entity.ignorePlayerCommRequests` for 10 days |
| `nskr_kq_ttCollectorDemandSome` | `check ttCollectorCanPaySome`: at least one unit held | "Hand over the $nskr_kq_ttCollectorPayment units …" or refuse |
| `nskr_kq_ttCollectorDemandNone` | Fallback | Admit it, which leads to the fight exit |
| `nskr_kq_ttCollectorPay` | Hand over | `do ttCollectorPay` (vanilla receipt, `COLLECTOR_PAID`, hostile flags removed), `AdjustRep tritachyon 5`, `AdjustRepActivePerson COOPERATIVE 10`, `ui_rep_raise`, removes `$entity.ignorePlayerCommRequests`, `do ttCollectorLeave`, then allows disengaging and makes the fleet non-aggressive; Leave |
| `nskr_kq_ttCollectorNoPay`, `nskr_kq_ttCollectorFight`, `nskr_kq_ttCollectorExit` | Admit, refuse, cut | The fight line; the fight exit repeats it and ends with "Cut the comm link"; the exit ends the conversation |

The demand's `ttCollectorCanPayAll` never passes, because the demand is everything held. The comm link row tests `!$ignorePlayerCommRequests` on the speaker's memory, while the Continue row sets the key on the fleet (`$entity`), so the test never fails.

## Alternative endings

The Luddic and Tri-Tachyon endings are the `# KESTEVEN QUESTLINE: ALTERNATIVE ENDINGS` block of `data/campaign/rules.csv`; `kesteven/quest/KestevenAltEndingsModule` declares their checks, actions and tokens. Behavior is in [Stage 19](KESTEVEN_QUESTLINE.md#stage-19-who-receives-the-chip).

| Screen | Structure | Rows |
|---|---|---|
| Entry option | `PopulateOptions` rows, "Talk about the Unlimited Production Chip you have" | `nskr_kq_altEndingLuddicOption` and `nskr_kq_altEndingTtOption` while the ending's second-talk flag is unset; `…OptionLocked` after it, only for the official with `$nskr_kq_altEndingLocked`. All need `nskr_isBaseOfficial admin`, `is CHIP_RECOVERED`, the market check `altEndingLuddicMarket` or `altEndingTtMarket`, and neither `CHIP_HANDED_TO_ELIZA` nor `ALT_ENDING_DONE` |
| First talk | `DialogOptionSelected` pick | `nskr_kq_altEndingLuddicPickSel`, `nskr_kq_altEndingTtPickSel`; the locked official gets `…PickLocked` (one more condition line). The Tri-Tachyon options come from `nskr_kq_altEndingTtOfferOptions` on `nskr_kqAltEndingTtOfferOptions`, fired with `FireBest`, because the counter-offer label shows a quest token |
| Second talk | Plain chain | `nskr_kq_altEndingLuddicDoubtSel` ("So I would get nothing?") and `nskr_kq_altEndingTtIncreaseSel` (the counter-offer, action `altEndingRaisePrice`) set `LUDDIC_ENDING_SECOND_TALK` or `TT_ENDING_SECOND_TALK` and lock the official (`$nskr_kq_altEndingLocked`, no expiry) |
| Agreement | Plain chain | `nskr_kq_altEndingLuddicAgreeSel`: "the Unlimited Production Chip is destroyed", `AddStoryPoints 8`, `AdjustRep $faction.id 15`, `AdjustRepActivePerson COOPERATIVE 10`, `AddPotentialContact`, `ui_rep_raise`, action `altEndingFallout`. `nskr_kq_altEndingTtAgreeSel`: "Lost the Unlimited Production Chip", action `altEndingPay`, `AdjustRep tritachyon 15`, the same person, contact and sound lines, `altEndingFallout`, `altEndingPlaceChip` |
| Exits | `DialogOptionSelected` | `nskr_kq_altEndingExitSel` ("Do come back if you change your mind.") and `nskr_kq_altEndingExitAgreeSel`, both `FireAll PopulateOptions` |

The vanilla receipts of `AddStoryPoints`, `AdjustRep`, `AdjustRepActivePerson`, `QuestRewards.credits` and `QuestRewards.relationship` replace the old hand-written receipt lines. The speaker's pronouns are vanilla's `$HeOrShe` and `$hisOrHer`; the Tri-Tachyon official's greeting uses the hub token `playerFullName`.

`KestevenAltEndingsModule` (active at `CHIP_RECOVERED` and `COMPLETED`) declarations:

| Kind | Name | Meaning |
|---|---|---|
| check | `altEndingLuddicMarket`, `altEndingTtMarket` | The dialog target's market belongs to the Luddic Path or Luddic Church, or to Tri-Tachyon |
| action | `altEndingRaisePrice` | `ttPayout` = 2,500,000 |
| action | `altEndingPay` | Raises `ttPayout` to at least 2,000,000 and pays it through `QuestRewards.credits` |
| action | `altEndingFallout` | Unmarks `asteriaOrOutpost` and Eliza's market, Alice and Jack -50 (at worst hostile) with their contacts suspended, Eliza -50, Kesteven set to a random -0.55 to -0.65 when that is lower (purpose `endingAltDialogKeyRandom`, receipt through `QuestRewards.relationship`), `ALT_ENDING_DONE`, stage `COMPLETED`, `KestevenEndingsModule.unlockSettings()` |
| action | `altEndingPlaceChip` | `nskr_upChip` on Culann while Hybrasil exists and Tri-Tachyon holds Culann, otherwise on the dialog target's market |
| token | `altEndingTtOffer`, `altEndingTtRaised` | 2,000,000 and 2,500,000 with `Misc.getDGSCredits` |

## Job 3 party

The party at the job 3 start market is in the `# KESTEVEN QUESTLINE: JOB 3 PARTY` block. Flow, gates and rewards are in [The party](KESTEVEN_QUESTLINE.md#the-party).

| Screen | Structure | Trigger and rows |
|---|---|---|
| Blurb and option | `AddBarEvents` row | `nskr_kq_partyBlurb`: `AddBarEvent nskr_kq_partyApproach`. |
| Meeting the employee to the drink choice | Plain chain | `nskr_kq_partyApproach` (`BeginConversation nskr_kq_partyEmployee true false`), `…Offer`, `…Round`, `…Disagree`, `…Agree`, `…Invite`, `…Travel` (`ShowDefaultVisual`), `…Arrive` (`ShowPersonVisual true`). |
| A drink | Handlers with shared inserts | `nskr_kq_partyRum`, `…Liqueur`, `…Absynth`, `…Wine`, `…Spirit` fire `nskr_kqPartyCheers`, run `do partyDrink`, add the drink's paragraph and fire `nskr_kqPartyMingle`; `nskr_kq_partyNothing` fires only `nskr_kqPartyMingle`. |
| Looking around | `FireBest` pick, then `FireAll` menu | `nskr_kq_partyLook` (`HideVisual`) fires `nskr_kqPartyLookText` (two versions) and `nskr_kqPartyGroups` (one text and option row per unvisited group; Leave once the officers are visited). |
| The techies | Plain chain | `nskr_kq_partyTechies` (portraits), `…Pitch`, `…PitchDecline`, `…PitchListen`. |
| The toast | Plain chain with shared inserts | `nskr_kq_partyToast`, `…ToastDrink` and `…ToastSkip` fire `nskr_kqPartyToastText`; the drink also fires `nskr_kqPartyToastTipsy`; `…ToastLeave`. |
| The officers | Plain chain with a shared insert | `nskr_kq_partyOfficers` (portraits), `…OfficersJoin`, `…StoryPrompt`; the four story options fire `nskr_kqPartyStory`; `…Target` names the system. |
| The last drink | `FireBest` pick | `nskr_kq_partyLeave` fires `nskr_kqPartyLastDrink`: sober (Agree, Leave) or drunk (`check partyDrunk`: Agree and two drunk answers). |
| The hangover | Shared inserts and a plain chain | `nskr_kq_partyWasted1` to `3` fire `nskr_kqPartyBlackout` (`HideVisual`); `nskr_kq_partySlur1` to `4` fire `nskr_kqPartyHangover`; `…Wake` (`ShowDefaultVisual`), `…Recall`, `…Damages` (`do partyHangover`). |
| Exits | `BarCMD returnFromEvent true` | `nskr_kq_partyDecline` sets `JOB3_PARTY_DECLINED`; `nskr_kq_partyExit` and `nskr_kq_partyHome` fire `nskr_kqPartyCoordinates` (stage 9, `JOB3_TARGET_DISCOVERED`, receipt). |

## Eliza's port

The meeting at Eliza's market is in the `# KESTEVEN QUESTLINE: ELIZA` block. Flow and outcomes are in [Eliza's port](KESTEVEN_QUESTLINE.md#elizas-port).

| Screen | Structure | Trigger and rows |
|---|---|---|
| Arrival | `OpenInteractionDialog` row | `nskr_kq_elizaPort` (`check elizaPort score:10000`): the market dialog is replaced; `nskr_kq_elizaMeet` or `defaultLeave`. |
| Meeting to the first question | Plain chain | `nskr_kq_elizaMeet` (`do elizaMeet`), `…Escort`, `…Office` (`ShowPersonVisual false nskr_anarchist`); `…Sit` and `…Stand` (sets `$nskr_kq_elizaStood`, expiry 0) fire `nskr_kqElizaCache`. |
| Who should wield the power | Handlers with a shared insert | `nskr_kq_elizaChip`, `…NotReally`, `…NotTelling` fire `nskr_kqElizaPower`. |
| Neutral, dislike and her plans | Plain chains with shared inserts | `nskr_kq_elizaUnsure`; `…NoPolitics` and `…SayNothing` fire `nskr_kqElizaKillingMachine`; `…Kesteven` and `…NotYou` fire `nskr_kqElizaPropaganda`; `…WhatPower`, `…HowGood`, `…Violence`; `…NoDifferent` and `…NoTerrorists` fire `nskr_kqElizaComsec`. Each handler's own Options column holds its option set; one option id serves every label the old dialog gave the same outcome. |
| Refusal | Handlers with a shared insert | `nskr_kq_elizaRefuse` and `…Silent` fire `nskr_kqElizaFarGone`; `nskr_kq_elizaDismissed` fires `nskr_kqElizaStoodLine`, sets `ELIZA_DIALOG_FINISHED` and `ELIZA_RAID_ENABLED`, runs `elizaToPort`; `…ThrownOut` (`HideVisual`) ends with `defaultLeave`. |
| Agreement | Handlers with shared inserts | `nskr_kq_elizaJoin` and `…BackHer` fire `nskr_kqElizaRight`; `…ReallyRight` and `…Together` fire `nskr_kqElizaOffer`; `…Agree` and `…AgreeLie` fire `nskr_kqElizaMonitoring` and `nskr_kqElizaDisks` (flags, `elizaHandOver`, receipts, `elizaToPort`, `defaultLeave`). |
| Her move | Delve update | `nskr_kq_elizaMovedBullet` on `nskr_kqIntelBullets` for update `elizaMoved`; the other job 5 bullets exclude that update. |
| The raid | Raid objective rows | `nskr_kq_elizaRaidName` (`nskr_kqRaidName`), `nskr_kq_elizaRaidTooltip` (`nskr_kqRaidTooltip`) and the result lines `…RaidDisk2`, `…RaidDisk1`, `…RaidCredits` (`nskr_kqRaidResult`), selected by `$nskr_raid_key == elizaDisks`. |

## The Kesteven and Eliza endings

Both endings are rows in the `# KESTEVEN QUESTLINE: ENDINGS` block that take over the dialog of their place on `OpenInteractionDialog`; [the questline page](KESTEVEN_QUESTLINE.md#the-kesteven-and-eliza-endings) describes when and the effects. `KestevenEndingsModule` declares the checks, actions and tokens.

| Screen | Structure | Rows |
|---|---|---|
| Kesteven: the choice | `OpenInteractionDialog` row, `score:10000` | `nskr_kq_kestevenEndingOpen` (`check kestevenEndingHere`): `ShowDefaultVisual`, the market's name through `$marketName`, the gray line from the `FireBest` pick `nskr_kqKestevenEndingUpset` (`nskr_kq_kestevenEndingUpsetKilled` with `ELIZA_KILLED`), Continue and `defaultLeave` (Escape). |
| Kesteven: arrival, share, end | Plain chain | `nskr_kq_kestevenEndingStart` (the chip's loss line, Jack's card and Alice as second portrait with `ShowPersonVisual true nskr_opguy` and `ShowSecondPerson`), `nskr_kq_kestevenEndingShare` (`ShowPersonVisual false nskr_opguy`), `nskr_kq_kestevenEndingDone` (flag, stage 20, action `kestevenEnding`, the rewards, the `FireAll` inserts `nskr_kqKestevenEndingWar`: `nskr_kq_kestevenEndingTriTachyon`, `nskr_kq_kestevenEndingWar`), then `defaultLeave`. |
| Eliza: the call | `OpenInteractionDialog` row, `score:10000` | `nskr_kq_elizaEndingOpen` (`check elizaEndingHere`): `ShowDefaultVisual`, Continue only. |
| Eliza: talk, equipment, end | Plain chain | `nskr_kq_elizaEndingCall` (Eliza's card, `ShowPersonVisual false nskr_anarchist`; token `endingManOrWoman`: "man", "woman", or "captain" for another gender), `nskr_kq_elizaEndingEquipment`, `nskr_kq_elizaEndingDone` (flag, stage 20, action `elizaEnding`, the rewards, the `FireAll` inserts `nskr_kqElizaEndingWar`: `nskr_kq_elizaEndingKesteven`, `…Hegemony`, `…IronShell`, then the contact with `AddPotentialContact nskr_anarchist`), then `defaultLeave`. |

The insert rows' checks read a relationship before their own action lowers it, because a `FireAll` matches all its rows before it runs their scripts ([FireAll and FireBest](../RULES.md#fireall-and-firebest)). The "reduced to" and "improved to" values are tokens (`endingTriTachyonRep`, `elizaEndingPiratesRep`, `elizaEndingKestevenRep`, `elizaEndingHegemonyRep`), rounded as the old lines were.

## Notes for moving dialogue into rules

- **Speaker branching:** a Java branch on the active person and on flags becomes a row keyed on the person (`$id`) and the conditions it tests, as the hub rows above do.
- **Highlights:** Java highlights use `addPara(text, color, highlight, …)`. The highlighted phrases and colours move with the text.
- **Values in text:** job 1 electronics, payouts, the job 4 constellation, the Frost distance and target names are computed in Java. They must be prepared as tokens before a row displays them; see [RULES_AUTHORING.md](../RULES_AUTHORING.md#create-a-custom-text-token).
- **Stage writes:** the full list is in [KESTEVEN_STATE.md](KESTEVEN_STATE.md#who-changes-the-stage).
- **Bar events:** every questline bar event is a rules bar event, which saves nothing of its own; the job 3 party, the Eliza search and the Delve meeting are examples.
