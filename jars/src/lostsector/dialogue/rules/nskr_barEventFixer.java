package lostsector.dialogue.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.kesteven.quest.DelveMeetingBarEvent;
import lostsector.campaign.kesteven.quest.QuestHelper;
import lostsector.helper.SectorLookup;

import java.util.List;
import java.util.Map;

public class nskr_barEventFixer extends BaseCommandPlugin {

    static void log(final String message) {
        Global.getLogger(nskr_barEventFixer.class).info(message);
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        int stage = QuestHelper.getStage();
        MarketAPI market = dialog.getInteractionTarget().getMarket();

        //job5 bar intial
        if (stage==15 && market== SectorLookup.asteriaOrOutpost()) {
            DelveMeetingBarEvent event = new DelveMeetingBarEvent();
            event.addPromptAndOption(dialog, memoryMap);
            log("fixer added job5bar");
        }
        //

        return false;
    }
}
