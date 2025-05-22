package lostsector.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.customStart.HellSpawnManager;

public class HellSpawnCondition extends BaseMarketConditionPlugin {

    @Override
    public void apply(String id) {
        super.apply(id);

        if (!market.getFactionId().equals(Factions.PLAYER)) {
            unapply(id);
            return;
        }

        market.getStability().modifyFlat(id, -HellSpawnManager.getStabPenalty(), Misc.ucFirst(condition.getName().toLowerCase()));
    }

    @Override
    public boolean showIcon() {
        return market.getFactionId().contentEquals(Factions.PLAYER);
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        market.getStability().unmodify(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        if (market == null) {
            return;
        }

        tooltip.addPara("%s Stability",
                10f, Misc.getHighlightColor(),
                "-" + (int) HellSpawnManager.getStabPenalty());

    }

}
