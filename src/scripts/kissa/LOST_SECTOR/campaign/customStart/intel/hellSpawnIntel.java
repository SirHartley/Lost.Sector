package scripts.kissa.LOST_SECTOR.campaign.customStart.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.customStart.hellSpawnManager;

import java.awt.*;
import java.util.Set;

public class hellSpawnIntel extends BaseIntelPlugin {

    private CampaignFleetAPI pf;
    private int level = 0;

    static void log(final String message) {
        Global.getLogger(hellSpawnIntel.class).info(message);
    }

    //Initializer function
    public hellSpawnIntel() {
        Global.getSector().addScript(this);
    }

    @Override
    public void advance(float amount) {
        pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;


    }

    //updates variables, DO NOT do this in advance
    public void init(){
        pf = Global.getSector().getPlayerFleet();
        level = hellSpawnManager.getLevel();

    }

    @Override
    public boolean isImportant() {
        return true;
    }

    @Override
    protected void notifyEnded() {
        super.notifyEnded();
        Global.getSector().removeScript(this);
    }

    @Override
    public void endAfterDelay() {
        super.endAfterDelay();
    }

    @Override
    protected void notifyEnding() {
        super.notifyEnding();
    }

    @Override
    public boolean shouldRemoveIntel() {
        return super.shouldRemoveIntel();
    }

    //The function for adding all bullet-points in the Intel tooltip.
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        Color r = Misc.getNegativeHighlightColor();
        float pad = 3f;
        float opad = 10f;

        init();

        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) initPad = opad;

        bullet(info);

        if (level==0) info.addPara("Does nothing, yet", g, opad);

        if (level>0) info.addPara("Reduces combat readiness of non-automated vessels in your fleet by "+(int) hellSpawnManager.getCrReduction()+"%%",
                opad,  g, r, (int) hellSpawnManager.getCrReduction()+"%");
        if (level>=2) info.addPara("Reduces stability of your owned colonies by "+(int) hellSpawnManager.getStabPenalty(), opad,  g, r, (int) hellSpawnManager.getStabPenalty()+"");
        if (level>=3) info.addPara("Reduces max relationship with all factions by "+(int) hellSpawnManager.getRelationshipCap(),
                opad,  g, r, (int) hellSpawnManager.getRelationshipCap()+"");

        unindent(info);
    }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        super.buttonPressConfirmed(buttonId, ui);
    }

    //The function for writing the detailed info in the Intel screen.
    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        info.setParaSmallInsignia();
        info.addPara(getName(), c, 0f);
        info.setParaFontDefault();
        addBulletPoints(info, mode);
    }

    //The small description for the intel screen.
    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        info.addPara("INHUMAN", hellSpawnEventIntel.BAR_COLOR, opad);

        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    //Sets which icon the Intel screen should display. Can vary based on circumstances, but a single one often works just fine
    @Override
    public String getIcon() {
        return "graphics/icons/markets/plundered.png";
    }

    //This sets which "tags" the even has in the Intel screen. For example, giving it the Tags.INTEL_STORY tag makes it appear in the "Story" sub-category
    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add("Hellspawn");
        //tags.add(StringHelper.getString("exerelin_misc", "intelTagPersonal"));
        return tags;
    }

    //Sorting-related; see it as a form of "how important is the even" thingy. Lower number = more important
    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_3;
    }

    //What string to sort with, when sorting alphabetically
    public String getSortString() {
        return "Hellspawn";
    }

    //The name of the event; can vary based on circumstances. I decided to just make it say "completed" when completed
    public String getName() {
        return "Hellspawn";
    }

    //Here, you can set which faction's UI colors to use. The default is to use the player's faction.
    @Override
    public FactionAPI getFactionForUIColors() {
        return super.getFactionForUIColors();
    }

    //This just seems to call back to the name again
    public String getSmallDescriptionTitle() {
        return getName();
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return pf;
    }

    //Which sound the Comms should make from getting the intel. Some default values include:
    //  getSoundMajorPosting();
    //  getSoundStandardUpdate();
    //  getSoundLogUpdate();
    //  getSoundColonyThreat();
    //  getSoundStandardPosting();
    //  getSoundStandardUpdate();
    //Other values can be inputted, from sounds.json
    @Override
    public String getCommMessageSound() {
        return getSoundMajorPosting();
    }
}
