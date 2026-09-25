# Quest implementation

How the quests are implemented today, traced from the Java sources and `data/campaign/rules.csv`. Read these pages before changing, fixing or restructuring quest code, quest dialogue or quest state.

| Page | Covers |
|---|---|
| [KESTEVEN_QUESTLINE.md](KESTEVEN_QUESTLINE.md) | The main questline: owners, gates, stages, each job, the endings, failure, side events and defects found in the source |
| [KESTEVEN_STATE.md](KESTEVEN_STATE.md) | The questline's saved state: stages and the numbers these pages use for them, flags, fields, random purposes, memory flags, and every caller that changes the stage |
| [KESTEVEN_DIALOGUE.md](KESTEVEN_DIALOGUE.md) | Where each conversation is implemented: rule ids, triggers, option ids, speakers and the Java each row calls |
| [CONTRACTS_AND_BOUNTIES.md](CONTRACTS_AND_BOUNTIES.md) | The Kesteven contracts, the four named bounty fleets of quest `bounty` and the intercept fleets of quest `ic` |
| [HELLSPAWN.md](HELLSPAWN.md) | The Hellspawn judgement (quest `hs`): stages, THRN's scenes, the Final Judgement encounter and what stays in Java |
| [BLACKSITES.md](BLACKSITES.md) | The blacksite record quest `bs`: sites, statuses, defenders, timing, dialog rows and defects |
| [HINTS.md](HINTS.md) | Exploration intel (record quest `hint`): signal hints toward the bounty systems and Frost, the Kesteven officer's bar tip, the Frost entry |

These pages describe current behavior, including behavior that looks unintended. Defects are listed where they were found; they come from reading the source and have not been reproduced in game.

They do not govern:

- **how quest code is built:** the [quest framework guide](../../jars/src/lostsector/quest/README.md);
- **code owners and lifecycle:** [ARCHITECTURE.md](../ARCHITECTURE.md);
- **rules structure, syntax and routing contracts:** [RULES_WRITING.md](../RULES_WRITING.md), [RULES.md](../RULES.md) and [RULES_AUTHORING.md](../RULES_AUTHORING.md);
- **player-facing wording:** [DIALOGUE.md](../DIALOGUE.md);
- **characters, voice and setting:** [LORE.md](../LORE.md).

Conventions:

- Java paths are relative to `jars/src/lostsector/campaign/` unless stated; `dialogue/rules/` and `combat/` paths are relative to `jars/src/lostsector/`.
- "Persistent data" means `Global.getSector().getPersistentData()`.
- Times in seconds are frame seconds; vanilla runs 10 seconds per campaign day.
