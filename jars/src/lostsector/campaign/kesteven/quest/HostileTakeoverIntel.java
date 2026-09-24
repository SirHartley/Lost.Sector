package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.kesteven.quest.QuestStageManager;
import lostsector.campaign.kesteven.quest.QuestHelper;

import java.awt.*;
import java.util.Set;

public class HostileTakeoverIntel extends BaseIntelPlugin {

    CampaignFleetAPI pf;
    private int stage = 0;
    private float timer = 0;
    private boolean failed = false;
    private SectorEntityToken home = null;
    private SectorEntityToken target = null;
    private float relation = 0;
    private String asteriaOrOutpost= "";
    static void log(final String message) {
        Global.getLogger(HostileTakeoverIntel.class).info(message);
    }

    public HostileTakeoverIntel() {
        Global.getSector().addScript(this);
    }

    @Override
    public void advance(float amount) {
        this.pf = Global.getSector().getPlayerFleet();
        if (this.pf == null) return;
        if (QuestHelper.getEndMissions()){
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
        asteriaOrOutpost = QuestHelper.asteriaOrOutpost().getName();
        stage = QuestHelper.getStage();
        relation = Global.getSector().getPlayerFaction().getRelationship("kesteven");
        timer = QuestHelper.getMissionTimerJob3();
        failed = QuestHelper.getFailed(QuestStageManager.JOB3_FAIL_KEY);
        home = QuestHelper.getJob3Start();
        target = QuestHelper.getJob3Target();
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

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        info.setParaSmallInsignia();
        info.addPara(getName(), c, 0f);
        info.setParaFontDefault();
        addBulletPoints(info, mode);
    }

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

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", "job3");
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_IMPORTANT);
        tags.add(Tags.INTEL_ACCEPTED);
        tags.add(Tags.INTEL_MISSIONS);
        return tags;
    }

    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_2;
    }

    public String getSortString() {
        return "Hostile Takeover";
    }

    public String getName() {
        return "Hostile Takeover";
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return Global.getSector().getFaction("kesteven");
    }

    public String getSmallDescriptionTitle() {
        return getName();
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        init();

        SectorEntityToken loc = null;
        if(stage==8) loc = home;
        if(stage==9) loc = target;
        if(stage>=10) loc = QuestHelper.asteriaOrOutpost().getPrimaryEntity();

        return loc;
    }

    @Override
    public String getCommMessageSound() {
        return getSoundMajorPosting();
    }
}



