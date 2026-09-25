#!/bin/sh
# SessionStart (compact, resume): the summary of a guide is not the guide.
cat <<'JSON'
{"hookSpecificOutput":{"hookEventName":"SessionStart","additionalContext":"Lost.Sector: context was compacted or resumed. Before the next edit to data/campaign/rules.csv, jars/src/lostsector/dialogue/rules/, jars/src/lostsector/quest/ or any quest code, re-read in full: CLAUDE.md (current remote main), docs/RULES_WRITING.md, and for quest work jars/src/lostsector/quest/README.md. Re-read the RULES.md and RULES_AUTHORING.md sections the task relies on. Do not take engine facts, operators, commands, triggers, memory keys or framework APIs from the compaction summary; take them from those documents or the game source."}}
JSON
