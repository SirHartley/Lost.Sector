package lostsector.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.quests.Job4HintWreck;
import lostsector.campaign.quests.util.QuestStageManager;
import lostsector.campaign.quests.util.QuestUtil;
import lostsector.campaign.rulecmd.nskr_job4FleetDialog;
import lostsector.campaign.rulecmd.nskr_kestevenQuest;
import lostsector.util.MiscLS;

import java.awt.*;
import java.util.Set;

public class KQuest4Intel extends BaseIntelPlugin {

    CampaignFleetAPI pf;
    private boolean destroyed = false;
    private boolean foundFriendly = false;
    private boolean hintTarget = false;
    private boolean hintFriendly = false;
    private boolean foundTarget = false;
    private boolean helped = false;
    private boolean failed = false;
    private boolean defeatedTarget = false;
    private int stage = 0;
    private int nickInfo = 0;
    private Constellation constellation = null;
    private SectorEntityToken target = null;
    private float relation = 0;
    private String asteriaOrOutpost= "";
    private boolean outpost = false;
    private SectorEntityToken locTarget = null;
    static void log(final String message) {
        Global.getLogger(KQuest4Intel.class).info(message);
    }

    //Initializer function
    public KQuest4Intel() {
        Global.getSector().addScript(this);
    }

    @Override
    public void advance(float amount) {
        this.pf = Global.getSector().getPlayerFleet();
        if (this.pf == null) return;
        if (QuestUtil.getEndMissions()){
            endImmediately();
            return;
        }

        if (stage>=13) {
            if (isImportant()) {
                endAfterDelay();
            } else endImmediately();
        }
    }

    //updates variables, DO NOT do this in advance
    private void init(){
        asteriaOrOutpost = QuestUtil.asteriaOrOutpost().getName();
        stage = QuestUtil.getStage();
        relation = Global.getSector().getPlayerFaction().getRelationship("kesteven");

        constellation = QuestUtil.getJob4FriendlyTarget().getConstellation();
        target = QuestUtil.getJob4FriendlyTarget();

        helped = QuestUtil.getCompleted(QuestStageManager.JOB4_HELPED_KEY);
        failed = QuestUtil.getFailed(QuestStageManager.JOB4_FAILED_KEY);
        destroyed = QuestUtil.getCompleted(QuestStageManager.JOB4_DESTROYED_KEY);
        foundFriendly = QuestUtil.getCompleted(QuestStageManager.JOB4_FOUND_FRIENDLY_KEY);
        hintTarget = QuestUtil.getCompleted(QuestStageManager.JOB4_TARGET_HINT_KEY);
        hintFriendly = QuestUtil.getCompleted(Job4HintWreck.HINT_RECEIVED_KEY);
        foundTarget = QuestUtil.getCompleted(QuestStageManager.JOB4_FOUND_TARGET_KEY);
        nickInfo = QuestUtil.getDialogStage(nskr_kestevenQuest.JOB4_INTELLIGENCE_DIALOG_KEY);
        defeatedTarget = QuestUtil.getCompleted(QuestStageManager.JOB4_DESTROYED_KEY);
        outpost = QuestUtil.outpostExists();
        //target
        locTarget = QuestUtil.getJob4EnemyTarget();
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

        //friendly
        boolean talked = nskr_job4FleetDialog.getDialogStage(nskr_job4FleetDialog.PERSISTENT_KEY)>=1;
        SectorEntityToken locFr = QuestUtil.getJob4FriendlyTarget();
        String locFriendly = locFr.getStarSystem().getName();
        String locFriendlyOrbit = "";
        String friendlyOrb = "";
        String friendlyNear = "";
        if (!locFr.getName().equals("Null")){
            friendlyOrb = " orbiting ";
            friendlyNear = " near ";
            locFriendlyOrbit =  locFr.getName();
        }

        bullet(info);
        if(relation<=-0.50f && stage<=13)info.addPara("You need to get back to non-hostile relations with Kesteven, if you want to finish this job.", initPad, g, h, "non-hostile relations", "");

        if(stage==12 && !foundFriendly) info.addPara("Figure out the location of the Operations fleet, and establish contact.", opad, g,h, "", "");
        SectorEntityToken outpostEntity = MiscLS.getOutpost();
        if (outpostEntity!=null) {
            String outpostLoc = outpostEntity.getName();
            String outpostSys = outpostEntity.getStarSystem().getName();
            if (stage == 12 && !foundFriendly && nickInfo <= 0 && outpost)
                info.addPara("You should talk to Nicholas Antoine at " + outpostLoc + " in "+outpostSys+".", opad, g, h, outpostLoc, "");
        }
        if(stage<=13 && !foundFriendly && hintFriendly) info.addPara("Reach the coordinates in "+locFriendly+friendlyNear+locFriendlyOrbit, opad, g,h, locFriendly, locFriendlyOrbit);
        if(stage<=13 && foundFriendly && !helped) info.addPara("You found the remains of the Kesteven operations fleet in "+locFriendly+friendlyOrb+locFriendlyOrbit, opad, g,h, locFriendly, locFriendlyOrbit);
        if(stage<=13 && foundFriendly && !helped && talked) info.addPara("Deliver the supplies and fuel to the Operations fleet.", opad, g,h, "", "");
        //enemy gets generated 1 frame later so we need dumb null checks
        if (locTarget !=null) {
            String hintLoc = locTarget.getStarSystem().getName();
            if(stage==12 && !foundTarget && nickInfo>=1) info.addPara("Antoine told you to check out the "+hintLoc+".", opad, g,h, hintLoc, "");
            String locTarget = this.locTarget.getStarSystem().getName();
            String locTargetOrbit = "";
            String targetOrb = "";
            if (!this.locTarget.getName().equals("Null")) {
                targetOrb = " orbiting ";
                locTargetOrbit = this.locTarget.getName();
            }
            //hint for target system
            //more string fuckery
            if(locTargetOrbit.length()>0) {
                if (stage == 12 && !foundTarget && !destroyed && hintTarget) {
                    info.addPara("Investigate the " + locTargetOrbit + " in " + locTarget, opad, g, h, locTarget, locTargetOrbit);
                }
            } else {
                if (stage == 12 && !foundTarget && !destroyed && hintTarget) {
                    info.addPara("Investigate the " + locTarget, opad, g, h, locTarget, "");
                }
            }
            //found target
            if(stage==12 && foundTarget && !destroyed) info.addPara("You found the Enigma strike group in "+locTarget+targetOrb+locTargetOrbit, opad, g,h, locTarget, locTargetOrbit);
        }
        if(stage==12 && !destroyed) info.addPara("Eliminate any possible threats in the area.", opad, g,h, "", "");
        //return
        if(stage==13)info.addPara("With both of the fleets taken care of you should report back to Alice Lumi.", initPad, g, h, "report back to Alice Lumi", "");

        if(stage>=14 && !failed)info.addPara("You managed to complete the job.", initPad, g, h, "", "");
        if(stage>=14 && failed)info.addPara("You attacked the Special Operations fleet, job failed. You wont be working with Kesteven anytime soon.", initPad, g, h, "", "");
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
        String cons = QuestUtil.parseConstellation(constellation.getNameWithType());
        if(stage==12)info.addPara("Alice told of a lost Special Operations fleet located somewhere in "+cons+".", pad, h, cons , "");
        if(stage==13) info.addPara("Return to "+asteriaOrOutpost+".", opad, tc,h, "", "");

        if(stage>=14 && !failed) info.addPara("Mission complete", opad, tc,h, "", "");
        if(stage>=14 && failed) info.addPara("Mission failed", opad, tc,h, "", "");
        if(stage>=14) addDeleteButton(info, width);

        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    //Sets which icon the Intel screen should display. Can vary based on circumstances, but a single one often works just fine
    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", "job4");
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
        return "Operation Lifesaver";
    }

    //The name of the event; can vary based on circumstances. I decided to just make it say "completed" when completed
    public String getName() {
        return "Operation Lifesaver";
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
        if (stage==12) loc = Global.getSector().getHyperspace().createToken(constellation.getLocation());
        if (stage==12 && foundFriendly) loc = QuestUtil.getJob4FriendlyTarget();
        if (stage==12 && locTarget !=null && nickInfo>=1 && !defeatedTarget) loc = locTarget.getStarSystem().getCenter();
        if (stage==12 && !foundFriendly && defeatedTarget && hintFriendly) loc = QuestUtil.getJob4FriendlyTarget();
        if (stage==12 && locTarget !=null && foundTarget && foundFriendly && !defeatedTarget) loc = locTarget;
        if (stage==12 && foundFriendly && defeatedTarget) loc = QuestUtil.getJob4FriendlyTarget();
        if (stage==13) loc = QuestUtil.asteriaOrOutpost().getPrimaryEntity();
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



