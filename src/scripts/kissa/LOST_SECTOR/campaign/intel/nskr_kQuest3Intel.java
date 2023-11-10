package scripts.kissa.LOST_SECTOR.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;

import java.awt.*;
import java.util.Set;

public class nskr_kQuest3Intel extends BaseIntelPlugin {

    CampaignFleetAPI pf;
    private int stage = 0;
    private float timer = 0;
    private boolean failed = false;
    private SectorEntityToken home = null;
    private SectorEntityToken target = null;
    private float relation = 0;
    private String asteriaOrOutpost= "";
    static void log(final String message) {
        Global.getLogger(nskr_kQuest3Intel.class).info(message);
    }

    //Initializer function
    public nskr_kQuest3Intel() {
        Global.getSector().addScript(this);
    }

    @Override
    public void advance(float amount) {
        this.pf = Global.getSector().getPlayerFleet();
        if (this.pf == null) return;
        if (questUtil.getEndMissions()){
            endImmediately();
            return;
        }
        if (stage>=10) {
            if (isImportant()) {
                endAfterDelay();
            } else endImmediately();
        }
    }

    //updates variables, DO NOT do this in advance
    private void init(){
        asteriaOrOutpost = questUtil.asteriaOrOutpost().getName();
        stage = questUtil.getStage();
        relation = Global.getSector().getPlayerFaction().getRelationship("kesteven");
        timer = questUtil.getMissionTimerJob3();
        failed = questUtil.getFailed(questStageManager.JOB3_FAIL_KEY);
        home = questUtil.getJob3Start();
        target = questUtil.getJob3Target();
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
        float pad = 3f;
        float opad = 10f;

        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) initPad = opad;
        init();

        bullet(info);
        if(relation<=-0.50f && stage<=10)info.addPara("You need to get back to non-hostile relations with Kesteven, if you want to finish this job.", initPad, g, h, "non-hostile relations", "");
        if(timer>0f && stage<=9)info.addPara("You have "+(int)timer+" days to finish this job.", initPad, g, h, (int)timer+" days", "");
        if(stage==8)info.addPara("Head to "+home.getMarket().getName()+" in "+home.getStarSystem().getName()+" and figure out what the expeditions target is. Then neutralize the fleet.", initPad, g, h, home.getMarket().getName(), "");
        if(stage==9)info.addPara("Ambush the fleet on its way to, or in "+target.getStarSystem().getName()+". Leave no witness.", initPad, g, h, target.getStarSystem().getName(), "");

        if(stage==10 && !failed)info.addPara("With the fleet taken care of, you should return to Alice Lumi.", initPad, g, h, "return to Alice Lumi", "");
        if(stage==10 && failed)info.addPara("You failed the job. Return to Alice Lumi", initPad, g, h, "Return to Alice Lumi", "");

        if(stage>=11 && !failed)info.addPara("You managed to complete the job.", initPad, g, h, "", "");
        if(stage>=11 && failed)info.addPara("You failed the job.", initPad, g, h, "", "");

        unindent(info);
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

        init();

        if(stage==8) info.addPara("Figure out the target and neutralize the fleet.", opad, tc,h, "", "");
        if(stage==9) info.addPara("Neutralize the fleet.", opad, tc,h, "", "");

        if(stage==10 && !failed) info.addPara("Return to "+asteriaOrOutpost+".", opad, tc,h, "", "");
        if(stage==10 && failed) info.addPara("Mission failed, return to "+asteriaOrOutpost+".", opad, tc,h, "", "");

        if(stage>=11 && !failed) info.addPara("Mission complete", opad, tc,h, "", "");
        if(stage>=11 && failed) info.addPara("Mission failed", opad, tc,h, "", "");

        if(stage>=11) addDeleteButton(info, width);

        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    //Sets which icon the Intel screen should display. Can vary based on circumstances, but a single one often works just fine
    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", "job3");
    }

    //This sets which "tags" the even has in the Intel screen. For example, giving it the Tags.INTEL_STORY tag makes it appear in the "Story" sub-category
    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_IMPORTANT);
        tags.add(Tags.INTEL_ACCEPTED);
        tags.add(Tags.INTEL_MISSIONS);
        return tags;
    }

    //Sorting-related; see it as a form of "how important is the even" thingy. Lower number = more important
    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_2;
    }

    //What string to sort with, when sorting alphabetically
    public String getSortString() {
        return "Hostile Takeover";
    }

    //The name of the event; can vary based on circumstances. I decided to just make it say "completed" when completed
    public String getName() {
        return "Hostile Takeover";
    }

    //Here, you can set which faction's UI colors to use. The default is to use the player's faction.
    @Override
    public FactionAPI getFactionForUIColors() {
        return Global.getSector().getFaction("kesteven");
    }

    //This just seems to call back to the name again
    public String getSmallDescriptionTitle() {
        return getName();
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        init();

        SectorEntityToken loc = null;
        if(stage==8) loc = home;
        if(stage==9) loc = target;
        if(stage>=10) loc = questUtil.asteriaOrOutpost().getPrimaryEntity();

        return loc;
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



