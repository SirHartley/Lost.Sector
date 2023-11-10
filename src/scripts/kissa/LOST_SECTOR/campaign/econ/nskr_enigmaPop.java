package scripts.kissa.LOST_SECTOR.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class nskr_enigmaPop extends BaseMarketConditionPlugin {

    public static final float DEMAND_REDUCTION = 3f;
    public static final float STAB_PENALTY = 3f;

    @Override
    public void apply(String id) {
        super.apply(id);

        if (!market.getFactionId().equals("enigma")) {
            unapply(id);
            return;
        }

        for (Industry industry : market.getIndustries()) {
            if (industry.isIndustry()) {
                for (MutableCommodityQuantity supply : industry.getAllSupply()) {
                    industry.getSupply(supply.getCommodityId()).getQuantity().modifyFlat(getModId(), DEMAND_REDUCTION, getName());
                }
            }
        }
        //more fleets
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat(id, 1.00f, Misc.ucFirst(condition.getName().toLowerCase()));
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(id, 1f);
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(id, 1f);
        //industry
        market.getStability().modifyFlat(id, -STAB_PENALTY, Misc.ucFirst(condition.getName().toLowerCase()));
        market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).modifyFlat(id, 1f,Misc.ucFirst(condition.getName().toLowerCase()));

    }

    @Override
    public boolean showIcon() {
        return market.getFactionId().contentEquals("enigma");
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodify(id);
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).unmodify(id);
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).unmodify(id);

        market.getStability().unmodify(id);
        market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodify(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        if (market == null) {
            return;
        }

        tooltip.addPara("All demand reduced by %s",
                10f, Misc.getHighlightColor(),
                "" + (int) DEMAND_REDUCTION);
        tooltip.addPara("%s Stability",
                10f, Misc.getHighlightColor(),
                "-" + (int) STAB_PENALTY);

    }
}
