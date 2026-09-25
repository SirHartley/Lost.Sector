#!/bin/sh
# PreToolUse (Edit, Write, MultiEdit): reminder when rules or quest files are about to change.
input=$(cat)
case "$input" in
  *rules.csv*|*dialogue/rules/*|*lostsector/quest/*|*campaign/kesteven/*|*campaign/bounties/*|*campaign/events/*|*starts/hellspawn/*|*starts/thronesgift/*|*docs/quests/*)
    cat <<'JSON'
{"hookSpecificOutput":{"hookEventName":"PreToolUse","additionalContext":"Rules or quest file: follow docs/RULES_WRITING.md and jars/src/lostsector/quest/README.md as read in full this session. Use only the framework services, commands, operators, triggers and keys those documents name; if something is missing, verify it in the game source and add it to the guide before using it. Run the rules check tool after changing rules.csv."}}
JSON
    ;;
esac
exit 0
