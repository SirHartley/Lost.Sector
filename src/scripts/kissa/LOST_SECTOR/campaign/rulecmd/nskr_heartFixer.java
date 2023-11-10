package scripts.kissa.LOST_SECTOR.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.util.ids;

import java.util.List;
import java.util.Map;

public class nskr_heartFixer extends BaseCommandPlugin {

    //removes certain entries for heart market

    static void log(final String message) {
        Global.getLogger(nskr_heartFixer.class).info(message);
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog.getInteractionTarget() == null) return false;
        MarketAPI market = dialog.getInteractionTarget().getMarket();
        if (market == null || market.getId() == null) return false;

        //Frozen Heart
        if (market.getId().equals(ids.HEART_ENTITY_ID)) {

            dialog.getOptionPanel().removeOption("marketVisitBar");
            dialog.getOptionPanel().removeOption("marketOpenCoreUI");
        }

        //

        return false;
    }
}
