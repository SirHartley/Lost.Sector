package scripts.kissa.LOST_SECTOR.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.quests.nskr_kQuest5Bar;

import java.util.List;
import java.util.Map;

public class nskr_barEventFixer extends BaseCommandPlugin {

    static void log(final String message) {
        Global.getLogger(nskr_barEventFixer.class).info(message);
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        int stage = questUtil.getStage();
        MarketAPI market = dialog.getInteractionTarget().getMarket();

        //job5 bar intial
        if (stage==15 && market==questUtil.asteriaOrOutpost()) {
            nskr_kQuest5Bar event = new nskr_kQuest5Bar();
            event.addPromptAndOption(dialog, memoryMap);
            log("fixer added job5bar");
        }
        //

        return false;
    }
}
