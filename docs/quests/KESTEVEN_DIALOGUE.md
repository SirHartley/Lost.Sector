# Kesteven questline dialogue map

Where each questline conversation is implemented, how it is entered and how control passes between `rules.csv` and Java. Use it to find the text for a change and to plan moving Java-authored dialogue into rules. How to structure rules content is in [RULES_WRITING.md](../RULES_WRITING.md); rules syntax and project routing in [RULES.md](../RULES.md); commands and memory in [RULES_AUTHORING.md](../RULES_AUTHORING.md); text standards in [DIALOGUE.md](../DIALOGUE.md).

Java paths are relative to `jars/src/lostsector/campaign/`; `dialogue/rules/` and `combat/` paths are relative to `jars/src/lostsector/`.

## Three implementation styles

| Style | Used by | How it runs |
|---|---|---|
| Rules rows with a multi-verb command | Jack, Alice and Nicholas (`nskr_kestevenQuest`); the Special Operations fleet (`nskr_job4FleetDialog`); the collector (`nskr_ttCollectorDialog`); Eliza's intercept (`nskr_elizaInterceptDialog`); both alternative endings | Rows select the conversation and call a verb. The verb writes most text and options from Java strings. Several commands extend `PaginatedOptions` and take over the dialog plugin (`setupDelegateDialog`). Every non-paging option then returns to rules through `FireBest DialogOptionSelected`. |
| Rules rows only | Fleet greetings and threats: Enigma strike group, Eliza's raided and revenge fleets, Jack's revenge fleet, the Cache guardian, the "LZ" messenger, generic Enigma comms | Text, options and scripts live in `data/campaign/rules.csv`. |
| Java `InteractionDialogPlugin` or `BaseBarEvent` | Satellites, Glacier, Cache hint, Cache core, Eliza's port, both final ending dialogs, the job 3 and job 5 bar scenes, the job 4 hint wreck | `CorePlugin.pickInteractionDialogPlugin` or `PortsideBarData` opens the class. All text, options and state changes are in the Java class, using a nested `OptionId` enum. |

## Jack, Alice and Nicholas

Entry: rules row `nskr_kestevenQuest` adds `nskr_kestevenQuestContinue` ("Chat about operations work"). Selecting it runs `nskr_kestevenQuest getStage`, which delegates the dialog and calls `addStageOptions()`.

`addStageOptions()` chooses one status line and at most one action option by speaker and stage. The action option id is `nskr_kestevenQuest_pick_`; "Back" is `nskr_kestevenQuestExit`.

| Option id or prefix | Rules row | Verb | Java method |
|---|---|---|---|
| `nskr_kestevenQuest_pick_` | `nskr_kestevenQuestOptions` | `advanceStage` | `showQuestInfoAndPrepare()`: briefings, hand-ins, job 5 tips; writes flags, and 14→15 |
| `nskr_kestevenQuest_story_pick_` | `nskr_kestevenQuestOptionsSP` | `advanceStageReqSkip` | `SPOptionPicked()`, then `showQuestInfoAndPrepare()`: strength-gate bypass |
| `nskr_kestevenQuest_story_skip_pick_` | `nskr_kestevenQuestOptionsStorySkip` | `advanceStageStorySkip` | `SkipStoryOptionPicked()`: story skip |
| `nskr_kestevenQuest_extraStart_` | `nskr_kestevenQuestExtraDialogueStart` | `extraDialogueStart` | Question list for the current stage |
| `nskr_kestevenQuest_extra_<n>` | `nskr_kestevenQuestExtraDialogue` | `extraDialogue` | One answer; the option is disabled after reading |
| `nskr_kestevenQuest_extra_3` at stage 7 | `nskr_kestevenQuestJob3Skip` (`score:10`, wins over the row above) | `skip` | Refusal prompt |
| `nskr_kestevenQuestConfirmSkip` | `nskr_kestevenQuestConfirmSkip` | `confirmSkip` | Job 3 refusal penalties, stage 11 |
| `nskr_kestevenQuestConfirmQuest…` (prefix, includes `…B`) | `nskr_kestevenQuestConfirmQuest` | `confirmQuest` | `quest()`: accept, rewards and stage changes |
| `nskr_kestevenQuestExit` | `nskr_kestevenQuestExit` | — | `FireAll PopulateOptions` |

Where the text for each stage lives:

| Stage | Speaker | Status line (`addStageOptions`) | Briefing or hand-in (`showQuestInfoAndPrepare`) | Questions (`extraDialogue`) | Confirmation (`quest`) |
|---|---|---|---|---|---|
| 0 | Jack | Offer or relationship gate | Job 1 briefing | Tip, rogue AI, the ships, Artifact Electronics, AI cores, "already fought them" | Accept |
| 1 | Jack | Progress | Sensor and electronics hand-ins, location tip | — | — |
| 2 | Jack | "I've done everything." | Wrap-up | Equipment use, Enigma, next job | Rewards |
| 6 | Jack | Offer or gates | Send to Alice, exchange program | — | Contact, stage 7 |
| 7 | Alice | Offer | Job 3 briefing | Where to go, alone, necessary?, refuse | Accept |
| 8–9 | Alice, Jack | "Get to work" | — | — | — |
| 10 | Alice | After-action report | Success or failure | Target coordinates, next job | Rewards or penalties |
| 11 | Alice, Jack | Offer or gates | Job 4 briefing | Special Operations, threats, supplies, Nicholas, "LZ" | Accept |
| 12 | Nicholas | "Tell me what you know." | Signal burst hint | — | — |
| 13 | Alice | Report | Ambush report | Fleet goal, why Enigma, artifacts, "LZ" | Rewards |
| 14 | Jack | "I'm listening." | Go to the bar (sets 15) | — | — |
| 16 | Jack | Leads | Eliza tip | — | — |
| 16 | Alice | Leads, then "something new", then all disks | Satellite tip, Frost tip, Cache briefing | — | Frost identified; Cache coordinates |
| 20 | Both | "Nothing new" | — | — | — |

One-time introductions for each person are in `addStageOptions()` and use the `JACK_INTRODUCED`, `ALICE_INTRODUCED` and `NICHOLAS_INTRODUCED` flags of `KestevenState`.

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

Rows for `nskr_ttCollectorDialog` and `nskr_loanSharkDialog` never pass the `setPaid` verb; its `case` falls through into `canPay`.

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
| `kesteven/quest/HostileTakeoverBarEvent` | `PortsideBarData` at the job 3 start market, stages 8–9 | Party with a Tri-Tachyon employee | Stage 9, target discovered |
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

- **Speaker branching:** `addStageOptions()` and `showQuestInfoAndPrepare()` branch on the active person and on cached flags. Each branch becomes a row keyed on the person (`$id`) and the conditions it tests.
- **Highlights:** Java highlights use `addPara(text, color, highlight, …)`. The highlighted phrases and colours move with the text.
- **Values in text:** job 1 electronics, payouts, the job 4 constellation, the Frost distance and target names are computed in Java. They must be prepared as tokens before a row displays them; see [RULES_AUTHORING.md](../RULES_AUTHORING.md#create-a-custom-text-token).
- **Stage writes:** most stage changes happen inside `quest()` or `showQuestInfoAndPrepare()`. The full list is in [KESTEVEN_STATE.md](KESTEVEN_STATE.md#who-changes-the-stage).
- **Java-only dialogs:** those opened by `CorePlugin` have no rules entry today. Moving them means adding a rules entry route and removing the `CorePlugin` branch.
- **Bar events:** `HostileTakeoverBarEvent` and the Eliza bar events are saved in `PortsideBarData`; renaming or deleting the classes affects existing saves.
