package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.kesteven.quest.QuestStageManager;
import lostsector.campaign.kesteven.quest.QuestHelper;
import lostsector.dialogue.rules.nskr_kestevenQuest;

import java.awt.*;
import java.util.Set;

public class EnemyUnknownIntel extends BaseIntelPlugin {

    CampaignFleetAPI pf;
    private int stage = 0;
    private float relation = 0;
    private String asteriaOrOutpost= "";
    private boolean sensored = false;
    private boolean delivered = false;
    private boolean deliveredData = false;
    private boolean tipped = false;
    private boolean base = false;
    static void log(final String message) {
        Global.getLogger(EnemyUnknownIntel.class).info(message);
    }

    public EnemyUnknownIntel() {
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

        if (stage>=3){
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

        delivered = QuestHelper.getCompleted(QuestStageManager.JOB1_DELIVERED_KEY);
        deliveredData = QuestHelper.getCompleted(QuestStageManager.JOB1_DELIVERED_DATA_KEY);
        sensored = QuestHelper.getCompleted(QuestStageManager.JOB1_SENSORS_KEY);
        tipped = QuestHelper.getCompleted(QuestStageManager.JOB1_TIP_KEY);
        if (QuestHelper.getJob1Tip()!=null) {
            base = QuestHelper.hasEnigmaBase(QuestHelper.getJob1Tip());
        }
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
        if(relation<=-0.50f && stage<3)info.addPara("You need to get back to non-hostile relations with Kesteven, if you want to finish this job.", initPad, g, h, "non-hostile relations", "");

        if(stage==1 && !sensored)info.addPara("Locate any of the unknown AI force, and run the custom sensors package while engaging them.", initPad, g, h, "", "");
        if(stage==1 && sensored && !deliveredData)info.addPara("With the data gathered, you should return to Jack Lapua in "+asteriaOrOutpost+" to deliver the package.", initPad, g, h, "return to Jack Lapua in "+asteriaOrOutpost, "");

        if(stage==1 && !delivered)info.addPara("Search for more AI activity, and recover the electronics once you've defeated them. Once you have "+ nskr_kestevenQuest.JOB1_ARTIFACTS +" Artifact Electronics deliver them to Jack Lapua in "+asteriaOrOutpost+".", initPad, g, h, "", nskr_kestevenQuest.JOB1_ARTIFACTS +" Artifact Electronics");

        StarSystemAPI loc = QuestHelper.getJob1Tip();
        if(stage==1 && tipped && base)info.addPara("Investigate the "+loc.getName()+".", initPad, g, h, loc.getName(), "");

        if(stage==2)info.addPara("You have completed the tasks. Talk to Jack Lapua to finish the job.", initPad, g, h, "", "");
        if(stage>=3)info.addPara("You managed to complete the job.", initPad, g, h, "", "");

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

        if(stage==1) info.addPara("Jack Lapua gave you a set of tasks to complete, involving an unknown AI threat.", opad, tc,h, "", "");
        if(stage==2) info.addPara("Return to "+asteriaOrOutpost+".", opad, tc,h, "", "");
        if(stage>=3) info.addPara("Mission complete.", opad, tc,h, "", "");

        if(stage>=3) addDeleteButton(info, width);

        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", "job1");
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
        return "Enemy Unknown";
    }

    public String getName() {
        return "Enemy Unknown";
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
        if (tipped && base) return QuestHelper.getJob1Tip().getHyperspaceAnchor();
        return QuestHelper.asteriaOrOutpost().getPrimaryEntity();
    }

    @Override
    public String getCommMessageSound() {
        return getSoundMajorPosting();
    }
}



