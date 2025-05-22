package lostsector.campaign.graid;

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
import lostsector.campaign.quests.ElizaDialog;
import lostsector.campaign.quests.util.QuestFleets;
import lostsector.campaign.quests.util.QuestUtil;
import lostsector.util.MathUtilLS;

import java.awt.*;
import java.util.Random;

public class ElizaRaid extends AbstractGoalGroundRaidObjectivePluginImpl {

    //raid disks from Eliza

    private PersonAPI eliza = null;
    static void log(final String message) {
        Global.getLogger(ElizaRaid.class).info(message);
    }

    public ElizaRaid(MarketAPI market, PersonAPI eliza) {
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
        QuestUtil.setDisksRecovered(QuestUtil.getDisksRecovered()+2);

        float creds = MathUtilLS.getSeededRandomNumberInRange(30000f,40000f, ElizaDialog.getRandom());
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

        QuestUtil.setCompleted(true, ElizaDialog.ELIZA_FIGHT_KEY);
        //spawn fleet
        CampaignFleetAPI fleet = QuestFleets.spawnElizaFleet(market.getPrimaryEntity(), eliza, ElizaDialog.getRandom(), false, false);

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


