package scripts.kissa.LOST_SECTOR.campaign.graid;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.graid.AbstractGoalGroundRaidObjectivePluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.fleetInfo;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questFleets;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.quests.nskr_elizaDialog;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

public class nskr_elizaRaid extends AbstractGoalGroundRaidObjectivePluginImpl {

    //raid disks from Eliza

    private PersonAPI eliza = null;
    static void log(final String message) {
        Global.getLogger(nskr_elizaRaid.class).info(message);
    }

    public nskr_elizaRaid(MarketAPI market, PersonAPI eliza) {
        super(market, RaidDangerLevel.EXTREME);
        this.market = market;
        this.eliza = eliza;
        log("raid class");
        log("raid market "+market.getName());
    }

    public RaidDangerLevel getDangerLevel() {
        return RaidDangerLevel.EXTREME;
    }

    public String getName() {
        return "Data Disks";
    }

    @Override
    public CargoStackAPI getStackForIcon() {
        return Global.getFactory().createCargoStack(CargoAPI.CargoItemType.RESOURCES, "nskr_electronics", null);
    }

    public int performRaid(CargoAPI loot, Random random, float lootMult, TextPanelAPI text) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        if (marinesAssigned <= 0) return 0;

        //get disks
        questUtil.setDisksRecovered(questUtil.getDisksRecovered()+2);

        float creds = mathUtil.getSeededRandomNumberInRange(30000f,40000f, nskr_elizaDialog.getRandom());
        Global.getSector().getPlayerFleet().getCargo().getCredits().add(creds);
        text.setFontSmallInsignia();
        //acquire text
        text.addPara("Acquired Data Disk #2",g,h,"Data Disk #2","");
        text.addPara("Acquired Data Disk #1",g,h,"Data Disk #1","");
        text.addPara("Acquired "+Misc.getDGSCredits(creds)+" from miscellaneous valuables",g,h,Misc.getDGSCredits(creds),"");
        text.setFontInsignia();

        //remove important
        if (market.getPrimaryEntity().getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_MISSION_IMPORTANT)){
            market.getPrimaryEntity().getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        }

        //remove from market
        market.getCommDirectory().removePerson(eliza);
        market.removePerson(eliza);

        questUtil.setCompleted(true, nskr_elizaDialog.ELIZA_FIGHT_KEY);
        //spawn fleet
        CampaignFleetAPI fleet = questFleets.spawnElizaFleet(market.getPrimaryEntity(), eliza, nskr_elizaDialog.getRandom(), false, false);

        //xp
        return (int) (1 * getProjectedCreditsValue() * XP_GAIN_VALUE_MULT);
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    @Override
    public void createTooltip(TooltipMakerAPI t, boolean expanded) {
        float opad = 10f;
        float pad = 3f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color good = Misc.getPositiveHighlightColor();

        t.addPara("Take the Data Disks from Eliza's compound, by force.", opad, h, "");
    }

}


