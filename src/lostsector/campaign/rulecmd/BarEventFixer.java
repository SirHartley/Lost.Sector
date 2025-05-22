package lostsector.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.quests.KQuest5Bar;
import lostsector.campaign.quests.util.QuestUtil;

import java.util.List;
import java.util.Map;

public class BarEventFixer extends BaseCommandPlugin {

    static void log(final String message) {
        Global.getLogger(BarEventFixer.class).info(message);
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        int stage = QuestUtil.getStage();
        MarketAPI market = dialog.getInteractionTarget().getMarket();

        //job5 bar intial
        if (stage==15 && market== QuestUtil.asteriaOrOutpost()) {
            KQuest5Bar event = new KQuest5Bar();
            event.addPromptAndOption(dialog, memoryMap);
            log("fixer added job5bar");
        }
        //

        return false;
    }
}
