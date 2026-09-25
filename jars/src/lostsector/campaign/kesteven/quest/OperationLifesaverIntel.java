package lostsector.campaign.kesteven.quest;

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
import lostsector.campaign.kesteven.quest.QuestHelper;
import lostsector.dialogue.rules.nskr_job4FleetDialog;
import lostsector.helper.SectorLookup;

import java.awt.*;
import java.util.Set;

public class OperationLifesaverIntel extends BaseIntelPlugin {

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
        Global.getLogger(OperationLifesaverIntel.class).info(message);
    }

    public OperationLifesaverIntel() {
        Global.getSector().addScript(this);
    }

    @Override
    protected void advanceImpl(float amount) {
        this.pf = Global.getSector().getPlayerFleet();
        if (this.pf == null) return;
        if (QuestHelper.getEndMissions()){
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
        asteriaOrOutpost = SectorLookup.asteriaOrOutpost().getName();
        stage = QuestHelper.getStage();
        relation = Global.getSector().getPlayerFaction().getRelationship("kesteven");

        constellation = QuestHelper.getJob4FriendlyTarget().getConstellation();
        target = QuestHelper.getJob4FriendlyTarget();

        helped = QuestHelper.getCompleted(KestevenFlag.JOB4_FRIENDLY_HELPED);
        failed = QuestHelper.getFailed(KestevenFlag.JOB4_FAILED);
        destroyed = QuestHelper.getCompleted(KestevenFlag.JOB4_TARGET_DESTROYED);
        foundFriendly = QuestHelper.getCompleted(KestevenFlag.JOB4_FRIENDLY_FOUND);
        hintTarget = QuestHelper.getCompleted(KestevenFlag.JOB4_TARGET_HINT);
        hintFriendly = QuestHelper.getCompleted(KestevenFlag.JOB4_HINT_WRECK_READ);
        foundTarget = QuestHelper.getCompleted(KestevenFlag.JOB4_TARGET_FOUND);
        nickInfo = QuestHelper.getNicholasDialogStage();
        defeatedTarget = QuestHelper.getCompleted(KestevenFlag.JOB4_TARGET_DESTROYED);
        outpost = QuestHelper.outpostExists();
        //target
        locTarget = QuestHelper.getJob4EnemyTarget();
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

        //friendly
        boolean talked = nskr_job4FleetDialog.getDialogStage()>=1;
        SectorEntityToken locFr = QuestHelper.getJob4FriendlyTarget();
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
        SectorEntityToken outpostEntity = SectorLookup.getOutpost();
        if (outpostEntity!=null) {
            String outpostLoc = outpostEntity.getName();
            String outpostSys = outpostEntity.getStarSystem().getName();
            if (stage == 12 && !foundFriendly && nickInfo <= 0 && outpost)
                info.addPara("You should talk to Nicholas Antoine at " + outpostLoc + " in "+outpostSys+".", opad, g, h, outpostLoc, "");
        }
        if(stage<=13 && !foundFriendly && hintFriendly) info.addPara("Reach the coordinates in "+locFriendly+friendlyNear+locFriendlyOrbit, opad, g,h, locFriendly, locFriendlyOrbit);
        if(stage<=13 && foundFriendly && !helped) info.addPara("You found the remains of the Kesteven operations fleet in "+locFriendly+friendlyOrb+locFriendlyOrbit, opad, g,h, locFriendly, locFriendlyOrbit);
        if(stage<=13 && foundFriendly && !helped && talked) info.addPara("Deliver the supplies and fuel to the Operations fleet.", opad, g,h, "", "");
        // The enemy fleet is generated one frame later, so locTarget can still be null.
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
        if(stage>=14 && failed)info.addPara("You attacked the Special Operations fleet, job failed. You won't be working with Kesteven anytime soon.", initPad, g, h, "", "");
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
        String cons = QuestHelper.parseConstellation(constellation.getNameWithType());
        if(stage==12)info.addPara("Alice told of a lost Special Operations fleet located somewhere in "+cons+".", pad, h, cons , "");
        if(stage==13) info.addPara("Return to "+asteriaOrOutpost+".", opad, tc,h, "", "");

        if(stage>=14 && !failed) info.addPara("Mission complete", opad, tc,h, "", "");
        if(stage>=14 && failed) info.addPara("Mission failed", opad, tc,h, "", "");
        if(stage>=14) addDeleteButton(info, width);

        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", "job4");
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
        return "Operation Lifesaver";
    }

    public String getName() {
        return "Operation Lifesaver";
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

        if (stage == 13) return SectorLookup.asteriaOrOutpost().getPrimaryEntity();
        if (stage != 12) return null;

        // The leads shown by addBulletPoints, most precise first.
        boolean targetActive = locTarget != null && !defeatedTarget;
        if (targetActive && (foundTarget || hintTarget)) return locTarget;
        if (!foundFriendly && hintFriendly) return target;
        if (targetActive && nickInfo >= 1) return locTarget.getStarSystem().getCenter();
        if (foundFriendly) return target;
        return Global.getSector().getHyperspace().createToken(constellation.getLocation());
    }

    @Override
    public String getCommMessageSound() {
        return getSoundMajorPosting();
    }
}



