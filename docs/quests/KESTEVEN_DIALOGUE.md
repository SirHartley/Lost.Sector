# Kesteven questline dialogue map

Where each questline conversation is implemented, how it is entered and how control passes between `rules.csv` and Java. Use it to find the text for a change and to plan moving Java-authored dialogue into rules. How to structure rules content is in [RULES_WRITING.md](../RULES_WRITING.md); rules syntax and project routing in [RULES.md](../RULES.md); commands and memory in [RULES_AUTHORING.md](../RULES_AUTHORING.md); text standards in [DIALOGUE.md](../DIALOGUE.md).

Java paths are relative to `jars/src/lostsector/campaign/`; `dialogue/rules/` and `combat/` paths are relative to `jars/src/lostsector/`.

## Three implementation styles

| Style | Used by | How it runs |
|---|---|---|
| Rules rows with a multi-verb command | Briefings, hand-ins and confirmations of Jack, Alice and Nicholas (`nskr_kestevenQuest`); the Special Operations fleet (`nskr_job4FleetDialog`); the collector (`nskr_ttCollectorDialog`); Eliza's intercept (`nskr_elizaInterceptDialog`); both alternative endings | Rows select the conversation and call a verb. The verb writes most text and options from Java strings. Several commands extend `PaginatedOptions` and take over the dialog plugin (`setupDelegateDialog`). Every non-paging option then returns to rules through `FireBest DialogOptionSelected`. |
| Rules rows only | The conversation hub of Jack, Alice and Nicholas (menus, status lines, questions; gates from `KestevenHubModule` checks); fleet greetings and threats: Enigma strike group, Eliza's raided and revenge fleets, Jack's revenge fleet, the Cache guardian, the "LZ" messenger, generic Enigma comms | Text, options and scripts live in `data/campaign/rules.csv`. |
| Java `InteractionDialogPlugin` or `BaseBarEvent` | Satellites, Glacier, Cache hint, Cache core, Eliza's port, both final ending dialogs, the job 3 and job 5 bar scenes, the job 4 hint wreck | `CorePlugin.pickInteractionDialogPlugin` or `PortsideBarData` opens the class. All text, options and state changes are in the Java class, using a nested `OptionId` enum. |

## Jack, Alice and Nicholas

The conversation hub is the `# KESTEVEN QUESTLINE` block of `data/campaign/rules.csv`, with `kesteven/quest/KestevenHubModule` declaring its checks, action and tokens. Briefings, hand-ins, confirmations, the job 3 refusal and the story skip are still in `dialogue/rules/nskr_kestevenQuest`; the rows reach them through its verbs.

### Rows and triggers

The block has sub-headers in play order: `# Entry`, `# Jack`, `# Alice`, `# Nicholas`, `# Shared status lines` and `# Briefings, questions and hand-offs`.

| Screen | Structure | Trigger and rows |
|---|---|---|
| Entry option | Market menu row | `nskr_kq_hubChatOption` on `PopulateOptions`: `$tag:k_quest`, `check hubOpen` (Kesteven market, Kesteven standing above -50), not `ENDED`, not `COMPLETED` or `FAILED`. Adds `nskr_kq_chat` "Chat about operations work". |
| Open a speaker | `DialogOptionSelected` pick by `$id` | `nskr_kq_jackChat`, `nskr_kq_aliceChat`, `nskr_kq_nicholasChat` fire the speaker's greeting, status and menu. `nskr_kq_jackChatJob1Tip` first runs `do pickJob1Tip` (see below). |
| Greeting and introduction | `FireBest` pick (Jack, Alice); `FireAll` text inserts (Nicholas) | `nskr_kqJackGreeting`, `nskr_kqAliceGreeting`, `nskr_kqNicholasGreeting`. The introduction rows set `$nskr_kq_introduced` in the speaker's memory. |
| Status line | `FireBest` pick | `nskr_kqJackStatus`, `nskr_kqAliceStatus`, `nskr_kqNicholasStatus`. Lines two speakers share are shared inserts fired with `FireBest`: `nskr_kqNewJob`, `nskr_kqNeedFleet`, `nskr_kqNoJobs`. |
| Speaker menu | `FireAll` menu | `nskr_kqJackOptions`, `nskr_kqAliceOptions`, `nskr_kqNicholasOptions`: story point options, the action option and Back. |
| Questions | `FireAll` menu | `nskr_kq_hubAsk` handles `nskr_kestevenQuest_extraStart_` (the briefing's "I have some questions."), prints "What do you need to know?" and fires `nskr_kqAsk`, whose rows (`nskr_kq_jackAsk`, `nskr_kq_jackAskJob1`, `nskr_kq_aliceAsk`) fire `nskr_kqJackQuestions` or `nskr_kqAliceQuestions`. Each answer row sets an "asked" key on the speaker and fires the questions again. |
| Hand-offs to Java | `DialogOptionSelected` | See the next table. |

Option ids and the rows that handle them:

| Option id | Added by | Handler row | Runs |
|---|---|---|---|
| `nskr_kq_chat` | `nskr_kq_hubChatOption` | `nskr_kq_jackChat`, `nskr_kq_jackChatJob1Tip`, `nskr_kq_aliceChat`, `nskr_kq_nicholasChat` | Greeting, status, menu |
| `nskr_kestevenQuest_pick_` | Menu action options, question Back, `skip()` "No" | `nskr_kq_hubBriefing` | `nskr_kestevenQuest advanceStage`: `showQuestInfoAndPrepare()` |
| `nskr_kestevenQuest_story_pick_` | Story point requirement skip, `SetStoryOption ... 1 nskr_skipRequirement` | `nskr_kq_hubReqSkip`; at `JOB4_WAITING` `nskr_kq_hubReqSkipJob4`, which first sets `JOB4_REQUIREMENT_SKIPPED` | `advanceStage` |
| `nskr_kestevenQuest_story_skip_pick_` | Story skip, `SetStoryOption ... 5 nskr_skipStory` | `nskr_kq_hubStorySkip` | `advanceStageStorySkip`: `SkipStoryOptionPicked()` |
| `nskr_kestevenQuest_extraStart_` | `showQuestInfoAndPrepare()` | `nskr_kq_hubAsk` | Questions |
| `nskr_kq_jackAsk…`, `nskr_kq_aliceAsk…` | Question rows | `…Sel` answer rows | Answer, then questions again |
| `nskr_kq_aliceAskRefuse` | `nskr_kq_aliceQRefuse` | `nskr_kq_aliceAskRefuseSel` | `skip`: refusal prompt |
| `nskr_kestevenQuestConfirmSkip` | `skip()` | `nskr_kq_hubConfirmSkip` | `confirmSkip`: refusal penalties, stage 11 |
| `nskr_kestevenQuestConfirmQuest`, `…B` | `showQuestInfoAndPrepare()` | `nskr_kq_hubConfirm`, `nskr_kq_hubConfirmLie` | `confirmQuest`: `quest()` |
| `nskr_kestevenQuestExit` | Menu Back, Java screens | `nskr_kq_hubExit` | `FireAll PopulateOptions` |

`KestevenHubModule` declarations used by the rows:

| Kind | Name | Meaning |
|---|---|---|
| check | `hubOpen` | The dialog target's market belongs to Kesteven and the player's Kesteven relationship is above -0.50 |
| check | `storySkipUnlocked` | LunaLib setting `storySkipUnlocked` |
| check | `job1Standing`, `job3Standing`, `job4Standing`, `job5Standing` | Kesteven relationship at least `nskr_kestevenQuest.JOB1_REP` … `JOB5_REP` |
| check | `job3Fleet`, `job5Fleet` | `nskr_kestevenQuest.fleetPower()` above `JOB3_POWER`, `JOB5_POWER` |
| check | `job4Fleet` | `JOB4_REQUIREMENT_SKIPPED`, or fleet power above `JOB4_POWER` |
| check | `job1Cargo` | At least `JOB1_ARTIFACTS` Artifact Electronics in the player's cargo |
| check | `job1SensorReady`, `job1CargoReady` | Sensor task done and not delivered; electronics in cargo and not delivered |
| check | `job1TipKnown`, `job4TargetKnown` | `job1TipSystem`, `job4EnemyTarget` set |
| check | `nicholasTipGiven`, `twoSatellites`, `allDisks` | `nicholasDialogStage` at least 1; at least two satellites; at least five disks |
| action | `pickJob1Tip` | `QuestHelper.getJob1Tip()`: picks the tip system on first use and places a dormant Enigma fleet there |
| token | `playerFullName`, `job1TipSystem`, `job3Start` | Player's full name; tip system name; job 3 start market entity name |

The job 1 tip system is picked where the old dialog first asked for it: opening Jack's menu at `JOB1_ACTIVE` with neither task done and no tip given (`nskr_kq_jackChatJob1Tip`), and opening Jack's questions at `NOT_STARTED` (`nskr_kq_jackAskJob1`). The checks never pick it.

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
| Alice, `JOB3_BRIEFING` | "How do I know where to go?" (`askedWhere`), "So I'm on my own for this?" (`askedAlone`), "Is this really necessary?" (`askedNecessary`), "I'm not doing this." (never hidden) | Refusal: `nskr_kestevenQuest skip` |
| Alice, `JOB3_DONE` | "Know anything about the target?" (`JOB3_TARGET_DISCOVERED`; `askedTarget`), "Next job?" (`askedNextJob`) | |
| Alice, `JOB4_WAITING` | "Special Operations fleet?", "Possible threats?", "Supplies and fuel?", "Nicholas Antoine?" (`askedSpecOps`, `askedThreats`, `askedSupplies`, `askedNicholas`), Ask about the "LZ" character (`MESSENGER_QUESTION_OPEN`) | "LZ": clears `MESSENGER_QUESTION_OPEN` |
| Alice, `JOB4_DONE` | "What was the Operations fleet's goal?", "Why are you so interested in this Enigma AI?", "The Artifact?" (`askedGoal`, `askedInterest`, `askedArtifact`), Ask about the "LZ" character | "LZ": clears `MESSENGER_QUESTION_OPEN` |

Every question list ends with Back (`nskr_kestevenQuest_pick_`, Escape), which shows the briefing again.

### Where the Java text lives

| Stage | Speaker | Briefing or hand-in (`showQuestInfoAndPrepare`) | Confirmation (`quest`) |
|---|---|---|---|
| 0 | Jack | Job 1 briefing | Accept |
| 1 | Jack | Sensor and electronics hand-ins, location tip | — |
| 2 | Jack | Wrap-up | Rewards |
| 6 | Jack | Send to Alice, exchange program | Contact, stage 7 |
| 7 | Alice | Job 3 briefing | Accept |
| 10 | Alice | Success or failure | Rewards or penalties |
| 11 | Alice | Job 4 briefing | Accept |
| 12 | Nicholas | Signal burst hint | — |
| 13 | Alice | Ambush report | Rewards |
| 14 | Jack | Go to the bar (sets 15) | — |
| 16 | Jack | Eliza tip | — |
| 16 | Alice | Satellite tip, Frost tip, Cache briefing | Frost identified; Cache coordinates |

## Fleet conversations

| Fleet | Rows | Command verbs |
|---|---|---|
| Special Operations (job 4) | `nskr_job4FleetDialogInit`, `…Initial`, `…InitialText`, `nskr_job4FleetDialog`, `…Help`, `…HelpConfirm`, `…Exit` | `hasOption`, `isDialogStage`, `setDialogStage`, `displayDialogInitial`, `help`, `confirmHelp` |
| Strike group (job 4) | `job4TargetInit`, `job4TargetInitial`, `job4TargetOptions`, `job4TargetContinue*`, `job4TargetEnd` | none |
| Tri-Tachyon collector | `nskr_ttCollectorDialogInit`, `…Initial`, `…InitialText`, `…PayAll`, `…NoPay`, `…ExitFight`, `…Exit` | `hasOption`, `canPay`, `pay` |
| Eliza after the raid | `elizaDialogInit`, `elizaDialogInitial`, `elizaDialogEnd` | none |
| Eliza's intercept (stage 19) | `nskr_elizaInterceptDialogInit`, `…Initial`, `…InitialText`, `…HandOver`, `…ExitFightDialog`, `…ExitFightNoChip`, `nskr_elizaIntercetpDialogExit` (sic) | `aggro`, `hasOption`, `addOptions`, `handOver`, `hostile` |
| Eliza's revenge | `elizaRevengeanceDialogInit`, `…Initial`, `…Options`, `…Continue*`, `…End` | none |
| Jack's revenge | `jackRevengeanceDialogInit`, `…Initial`, `…End` | none |
| Cache guardian | `cacheDialogInit`, `cacheDialogInitial`, `cacheDialogEnd` | none |
| "LZ" messenger | `MessengerFleetDialogInit`, `…Initial`, `…End` | none |

Rows for `nskr_ttCollectorDialog` never pass the `setPaid` verb; its `case` falls through into `canPay`.

## Alternative endings

Admin officials at Luddic Church, Luddic Path and Tri-Tachyon markets get "Talk about the Unlimited Production Chip you have" at stage 19 (`nskr_altEndingOptionLuddic`, `nskr_altEndingOptionTT`).

| Row | Verb |
|---|---|
| `nskr_altEndingPickLuddic`, `nskr_altEndingPickTT` | `addOptions` |
| `nskr_altEndingLuddicDoubt` | `setSecond`, then `addOptions` |
| `nskr_altEndingTTIncrease` | `setPriceIncrease`, `setSecond`, `addOptions` |
| `nskr_altEndingLuddicAgree`, `nskr_altEndingTTAgree` | `luddicAgree`, `tachAgree` |
| `nskr_altEndingExit` | "Do come back if you change your mind." and `FireAll PopulateOptions` |

The official who reaches the second conversation is locked to it through `$nskr_altEndingDialogLockedToPerson`.

## Java dialogs and bar events

| Class | Opened by | Content | State written |
|---|---|---|---|
| `kesteven/quest/HostileTakeoverBarEvent` | `PortsideBarData` at the job 3 start market, stages 8–9; added by `KestevenJob3Module` | Party with a Tri-Tachyon employee | Stage 9, target discovered |
| `kesteven/quest/DataSatelliteDialog` | `CorePlugin`, satellites #3 and #4 | Disk salvage, ping, keywords | Disk count, satellite flags, wakes the guard |
| `kesteven/quest/HintWreckDialog` | `CorePlugin`, hint wreck | Coordinates of the friendly fleet | Hint flag |
| `kesteven/quest/DelveMeetingBarEvent` | `nskr_barEventFixer` on each bar visit at stage 15 | Meeting with Jack and Alice | Advance credits, stage 16 |
| `kesteven/quest/ElizaSearchBarEvent`, `…Second`, `…Final` | `PortsideBarData` at pirate markets, stage 16 | Search for Eliza | Chain stage, used markets, Eliza's market |
| `kesteven/quest/ElizaDialog` | `CorePlugin`, Eliza's market until finished | Meeting Eliza | Disks, help or raid flags |
| `kesteven/quest/GlacierCommsDialog` | `CorePlugin`, Glacier after tip 2 | Timed facility raid | Disk #5; possible fleet damage |
| `kesteven/quest/CacheDoubtDialog` | `QuestStageManager`, once in Unknown Site | Inner-voice hint | None |
| `kesteven/quest/CacheCoreDialog` | `CorePlugin` or the guardian's fleet-interaction config | Cache core salvage | Stage 19, rewards |
| `kesteven/quest/EndingKestevenDialog` | `CorePlugin` at stage 19 | Kesteven ending | Stage 20, rewards, relations |
| `kesteven/quest/EndingElizaDialog` | `CorePlugin` at stage 19 after the handover | Eliza ending | Stage 20, rewards, relations |
| `kesteven/quest/ElizaRaid` | Raid menu at Eliza's market | Raid objective | Disks, Eliza's fleet |

`CacheCoreDialog`, the endings and `ElizaDialog` show the options they need; their text blocks are sequential `addPara` calls keyed by `OptionId`.

## Notes for moving dialogue into rules

- **Speaker branching:** `showQuestInfoAndPrepare()` and `quest()` branch on the active person and on cached flags. Each branch becomes a row keyed on the person (`$id`) and the conditions it tests, as the hub rows above do.
- **Highlights:** Java highlights use `addPara(text, color, highlight, …)`. The highlighted phrases and colours move with the text.
- **Values in text:** job 1 electronics, payouts, the job 4 constellation, the Frost distance and target names are computed in Java. They must be prepared as tokens before a row displays them; see [RULES_AUTHORING.md](../RULES_AUTHORING.md#create-a-custom-text-token).
- **Stage writes:** most stage changes happen inside `quest()` or `showQuestInfoAndPrepare()`. The full list is in [KESTEVEN_STATE.md](KESTEVEN_STATE.md#who-changes-the-stage).
- **Java-only dialogs:** those opened by `CorePlugin` have no rules entry today. Moving them means adding a rules entry route and removing the `CorePlugin` branch.
- **Bar events:** `HostileTakeoverBarEvent` and the Eliza bar events are saved in `PortsideBarData`; renaming or deleting the classes affects existing saves.
