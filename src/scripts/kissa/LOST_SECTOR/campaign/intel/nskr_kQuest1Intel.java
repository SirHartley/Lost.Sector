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
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.nskr_kestevenQuest;

import java.awt.*;
import java.util.Set;

public class nskr_kQuest1Intel extends BaseIntelPlugin {

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
        Global.getLogger(nskr_kQuest1Intel.class).info(message);
    }

    //Initializer function
    public nskr_kQuest1Intel() {
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

        if (stage>=3){
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

        delivered = questUtil.getCompleted(questStageManager.JOB1_DELIVERED_KEY);
        deliveredData = questUtil.getCompleted(questStageManager.JOB1_DELIVERED_DATA_KEY);
        sensored = questUtil.getCompleted(questStageManager.JOB1_SENSORED_KEY);
        tipped = questUtil.getCompleted(questStageManager.JOB1_TIP_KEY);
        if (questUtil.getJob1Tip()!=null) {
            base = questUtil.hasEnigmaBase(questUtil.getJob1Tip());
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
        if(relation<=-0.50f && stage<3)info.addPara("You need to get back to non-hostile relations with Kesteven, if you want to finish this job.", initPad, g, h, "non-hostile relations", "");

        if(stage==1 && !sensored)info.addPara("Locate any of the unknown AI force, and run the custom sensors package while engaging them.", initPad, g, h, "", "");
        if(stage==1 && sensored && !deliveredData)info.addPara("With the data gathered, you should return to Jack Lapua in "+asteriaOrOutpost+" to deliver the package.", initPad, g, h, "return to Jack Lapua in "+asteriaOrOutpost, "");

        if(stage==1 && !delivered)info.addPara("Search for more AI activity, and recover the electronics once you've defeated them. Once you have "+ nskr_kestevenQuest.JOB1_ARTIFACTS +" Artifact Electronics deliver them to Jack Lapua in "+asteriaOrOutpost+".", initPad, g, h, "", nskr_kestevenQuest.JOB1_ARTIFACTS +" Artifact Electronics");

        StarSystemAPI loc = questUtil.getJob1Tip();
        if(stage==1 && tipped && base)info.addPara("Investigate the "+loc.getName()+".", initPad, g, h, loc.getName(), "");

        if(stage==2)info.addPara("You have completed the tasks. Talk to Jack Lapua to finish the job.", initPad, g, h, "", "");
        if(stage>=3)info.addPara("You managed to complete the job.", initPad, g, h, "", "");

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

        if(stage==1) info.addPara("Jack Lapua gave you a set of tasks to complete, involving an unknown AI threat.", opad, tc,h, "", "");
        if(stage==2) info.addPara("Return to "+asteriaOrOutpost+".", opad, tc,h, "", "");
        if(stage>=3) info.addPara("Mission complete.", opad, tc,h, "", "");

        if(stage>=3) addDeleteButton(info, width);

        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    //Sets which icon the Intel screen should display. Can vary based on circumstances, but a single one often works just fine
    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", "job1");
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
        return "Enemy Unknown";
    }

    //The name of the event; can vary based on circumstances. I decided to just make it say "completed" when completed
    public String getName() {
        return "Enemy Unknown";
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
        if (tipped && base) return questUtil.getJob1Tip().getHyperspaceAnchor();
        return questUtil.asteriaOrOutpost().getPrimaryEntity();
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



