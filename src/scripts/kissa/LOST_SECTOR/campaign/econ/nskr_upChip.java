package scripts.kissa.LOST_SECTOR.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class nskr_upChip extends BaseMarketConditionPlugin {

    public static final float FLEET_SIZE = 100f;
    public static final float QUALITY_BONUS = 25f;
    public static final int PROD_BONUS = 1;
    private float extraBonus = 0f;
    public static final String ID = "nskr_upChip";

    @Override
    public void apply(String id) {
        super.apply(id);
        //decived
        if (market.getFaction()==null) return;
        if (market.isPlanetConditionMarketOnly()) return;
        //hacks
        condition.getSpec().setDesc(
                "The Unlimited Production Chip allows unprecedented access to high-end ship production, greatly increasing the military presence of "+market.getFaction().getDisplayNameWithArticle()+".");

        //more ship production
        Industry industry = getIndustry();
        if (industry != null) {
            industry.getSupplyBonusFromOther().modifyFlat(id, PROD_BONUS, Misc.ucFirst(condition.getName().toLowerCase()));
            extraBonus = Math.max(6-market.getSize(),0);
            if (extraBonus>0){
                industry.getSupplyBonusFromOther().modifyFlat(id+"extra", extraBonus, Misc.ucFirst(condition.getName().toLowerCase()));
            }
        }

        //more fleets
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat(id, FLEET_SIZE/100f, Misc.ucFirst(condition.getName().toLowerCase()));
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(id, 1f);
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(id, 1f);
        //quality
        market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat(id,QUALITY_BONUS/100f, Misc.ucFirst(condition.getName().toLowerCase()));

        //so we don't bork eliza market
        if (market.getFaction().getId().equals(Factions.PIRATES)){
            market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).modifyFlat(id, 2f,Misc.ucFirst(condition.getName().toLowerCase()));
        } else {
           market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodify(Misc.ucFirst(condition.getName().toLowerCase()));
        }
    }

    private Industry getIndustry(){
        Industry industry = market.getIndustry(Industries.HEAVYINDUSTRY);
        if (industry==null){
            industry = market.getIndustry(Industries.ORBITALWORKS);
        }
        return industry;
    }

    @Override
    public boolean showIcon() {
        if (market==null || market.isPlanetConditionMarketOnly())return false;
        return super.showIcon();
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodify(id);

        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodify(id);
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).unmodify(id);
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).unmodify(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        if (market == null) {
            return;
        }
        int combined = (int)extraBonus+PROD_BONUS;

        tooltip.addPara("Fleet size increased by %s",
                10f, Misc.getHighlightColor(),
                (int) FLEET_SIZE+"%");
        tooltip.addPara("Ship quality increased by %s",
                10f, Misc.getHighlightColor(),
                (int) QUALITY_BONUS+"%");
        tooltip.addPara("Heavy Industry production increased by %s based on market size",
                10f, Misc.getHighlightColor(),
                "" + combined);

    }
}
