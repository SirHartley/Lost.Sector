package lostsector.dialogue.rules;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions;
import com.fs.starfarer.api.util.Misc.Token;
import lostsector.quest.QuestDevTools;

import java.util.List;
import java.util.Map;

// nskr_questDev <verb>: the quest dev menu (quest README "Dev tools"); QuestDevTools reads and changes the quests.
// List verbs show a paginated list of unknown length; the fixed options of each list screen come from rows on
// QUESTS_OPTIONS or LIST_OPTIONS. Selections return through PaginatedOptions, which fires DialogOptionSelected, to
// the nskr_optionStartsWith rows, whose Script calls the verb that reads $option.
public class nskr_questDev extends PaginatedOptions {

    // Fired with keepOptions after every page, so the rows' options stay when the page changes.
    static final String QUESTS_OPTIONS = "nskr_questDevQuestsOptions";
    static final String LIST_OPTIONS = "nskr_questDevListOptions";

    // Local memory, expiry 0: the quest the menu shows, and the last list and page, so a list shown again after
    // a selection opens on the same page.
    static final String QUEST_KEY = "$nskr_questDev_quest";
    static final String PAGE_KEY = "$nskr_questDev_page";

    static final String PICK_QUEST = "nskr_questDev_pickQuest_";
    static final String PICK_STAGE = "nskr_questDev_pickStage_";
    static final String PICK_FLAG = "nskr_questDev_pickFlag_";
    static final String PICK_TIMER = "nskr_questDev_pickTimer_";
    static final String PICK_TRIGGER = "nskr_questDev_pickTrigger_";

    private String list;
    private String fixedOptions;

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || params.isEmpty()) return false;
        MemoryAPI local = memoryMap.get(MemKeys.LOCAL);
        String verb = params.get(0).getString(memoryMap);
        String quest = local.getString(QUEST_KEY);
        switch (verb) {
            case "quests":
                return show(dialog, memoryMap, "quests", QuestDevTools.quests(), PICK_QUEST, QUESTS_OPTIONS);
            case "select": {
                String picked = picked(local, PICK_QUEST);
                if (picked == null || !QuestDevTools.isQuest(picked)) return false;
                local.set(QUEST_KEY, picked, 0f);
                return true;
            }
            case "info":
                print(dialog.getTextPanel(), QuestDevTools.info(quest));
                return true;
            case "stages":
                return show(dialog, memoryMap, "stages", QuestDevTools.stages(quest), PICK_STAGE, LIST_OPTIONS);
            case "flags":
                return show(dialog, memoryMap, "flags", QuestDevTools.flags(quest), PICK_FLAG, LIST_OPTIONS);
            case "timers":
                return show(dialog, memoryMap, "timers", QuestDevTools.timers(quest), PICK_TIMER, LIST_OPTIONS);
            case "triggers":
                return show(dialog, memoryMap, "triggers", QuestDevTools.triggers(quest), PICK_TRIGGER, LIST_OPTIONS);
            case "jump": {
                String stage = picked(local, PICK_STAGE);
                return stage != null && QuestDevTools.jump(quest, stage, ruleId, dialog);
            }
            case "reset":
                return QuestDevTools.reset(quest, ruleId, dialog);
            case "toggleFlag": {
                String flag = picked(local, PICK_FLAG);
                return flag != null && QuestDevTools.toggleFlag(quest, flag, ruleId, dialog, memoryMap);
            }
            case "expireTimer": {
                String timer = picked(local, PICK_TIMER);
                return timer != null && QuestDevTools.expireTimer(quest, timer, ruleId, dialog);
            }
            case "open":
                return open(dialog, memoryMap, quest, picked(local, PICK_TRIGGER));
            default:
                return false;
        }
    }

    // A null rule id, as when a claimed entity's dialog fires its trigger on opening, so no row is skipped. The list's
    // options are cleared first: selecting one of them would reach the rules plugin, where the page options have no
    // handler. When the fired rows leave no options, the list shows again so the dialog is never stuck.
    private boolean open(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, String quest, String trigger) {
        if (trigger == null || !QuestDevTools.isDeclaredTrigger(quest, trigger)) return false;
        dialog.getOptionPanel().clearOptions();
        boolean fired = FireBest.fire(null, dialog, memoryMap, trigger);
        if (!fired) print(dialog.getTextPanel(), List.of("No row matches " + trigger + " here."));
        if (fired && dialog.getOptionPanel().hasOptions()) return true;
        return show(dialog, memoryMap, "triggers", QuestDevTools.triggers(quest), PICK_TRIGGER, LIST_OPTIONS);
    }

    // Each list gets its own instance to hold its page, because how the rules engine instantiates commands
    // (ScriptStore) is not in the 0.98a-RC8 sources. A list shown from a list keeps the first original plugin.
    private static boolean show(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, String list,
                                List<QuestDevTools.Item> items, String prefix, String fixedOptions) {
        nskr_questDev menu = new nskr_questDev();
        InteractionDialogPlugin current = dialog.getPlugin();
        menu.originalPlugin = current instanceof nskr_questDev ? ((nskr_questDev) current).originalPlugin : current;
        menu.dialog = dialog;
        menu.memoryMap = memoryMap;
        menu.list = list;
        menu.fixedOptions = fixedOptions;
        for (QuestDevTools.Item item : items) {
            menu.addOption(item.label, prefix + item.key);
        }
        menu.currPage = savedPage(memoryMap.get(MemKeys.LOCAL), list);
        dialog.setPlugin(menu);
        if (items.isEmpty()) print(dialog.getTextPanel(), List.of("No " + list + "."));
        menu.showOptions();
        return true;
    }

    @Override
    public void showOptions() {
        super.showOptions();
        FireBest.fire(null, dialog, memoryMap, fixedOptions + " true");
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        memoryMap.get(MemKeys.LOCAL).set(PAGE_KEY, list + " " + currPage, 0f);
        super.optionSelected(optionText, optionData);
    }

    private static int savedPage(MemoryAPI local, String list) {
        String saved = local.getString(PAGE_KEY);
        if (saved == null || !saved.startsWith(list + " ")) return 0;
        try {
            return Integer.parseInt(saved.substring(list.length() + 1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String picked(MemoryAPI local, String prefix) {
        String option = local.getString("$option");
        return option == null || !option.startsWith(prefix) ? null : option.substring(prefix.length());
    }

    private static void print(TextPanelAPI text, List<String> lines) {
        if (lines.isEmpty()) return;
        text.setFontSmallInsignia();
        text.addParagraph(String.join("\n", lines));
        text.setFontInsignia();
    }
}
