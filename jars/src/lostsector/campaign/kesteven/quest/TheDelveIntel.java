package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.kesteven.quest.DataSatelliteDialog;
import lostsector.campaign.kesteven.quest.ElizaDialog;
import lostsector.campaign.kesteven.quest.GlacierCommsDialog;
import lostsector.campaign.kesteven.quest.ElizaSearchBarEvent;
import lostsector.campaign.kesteven.quest.QuestStageManager;
import lostsector.campaign.kesteven.quest.QuestHelper;
import lostsector.dialogue.rules.nskr_kestevenQuest;
import lostsector.helper.MiscHelper;

import java.awt.*;
import java.util.Set;

public class TheDelveIntel extends BaseIntelPlugin {

    CampaignFleetAPI pf;
    private int stage = 0;
    private float relation = 0;
    private int recovered = 0;
    private boolean jackTip = false;
    private boolean aliceTip = false;
    private boolean aliceTip2 = false;
    private boolean foundEliza = false;
    private boolean foundFrost = false;
    private boolean recoveredGlacier = false;
    private boolean foundCache = false;

    private boolean paid = false;
    private boolean failed = false;
    private boolean elizaDialog = false;
    private boolean fightEliza = false;
    private boolean helpEliza = false;
    private boolean raidEliza = false;
    private boolean killedEliza = false;
    private boolean agreeEliza = false;
    private boolean delivered = false;
    private boolean allDisks = false;
    private String asteriaOrOutpost= "";

    static void log(final String message) {
        Global.getLogger(TheDelveIntel.class).info(message);
    }

    //Initializer function
    public TheDelveIntel() {
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

        if (stage>=20) {
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

        recovered = QuestHelper.getDisksRecovered();

        aliceTip = QuestHelper.getCompleted(nskr_kestevenQuest.JOB5_ALICE_TIP_KEY);
        aliceTip2 = QuestHelper.getCompleted(nskr_kestevenQuest.JOB5_ALICE_TIP_KEY2);
        jackTip = QuestHelper.getCompleted(nskr_kestevenQuest.JOB5_JACK_TIP_KEY);

        failed = QuestHelper.getFailed(QuestStageManager.JOB5_FAILED_KEY);
        paid = QuestHelper.getCompleted(ElizaSearchBarEvent.PAID_FOR_INFO);
        foundEliza =  QuestHelper.getCompleted(QuestStageManager.JOB5_FOUND_ELIZA_KEY);
        elizaDialog =  QuestHelper.getCompleted(ElizaDialog.DIALOG_FINISHED_KEY);
        fightEliza =  QuestHelper.getCompleted(ElizaDialog.ELIZA_FIGHT_KEY);
        helpEliza =  QuestHelper.getCompleted(ElizaDialog.ELIZA_HELP_KEY);
        raidEliza = QuestHelper.getCompleted(ElizaDialog.ELIZA_RAID_KEY);
        killedEliza = QuestHelper.getCompleted(QuestStageManager.KILLED_ELIZA_KEY);
        agreeEliza = QuestHelper.getCompleted(ElizaDialog.AGREED_TO_HELP_KEY);
        delivered = QuestHelper.getCompleted(QuestStageManager.ELIZA_INTERCEPT_HANDED_OVER);

        foundFrost = QuestHelper.getCompleted(QuestStageManager.JOB5_FOUND_FROST_KEY);
        recoveredGlacier = QuestHelper.getCompleted(GlacierCommsDialog.RECOVERED_KEY);

        allDisks = QuestHelper.getCompleted(QuestStageManager.ALL_DISKS_RECOVERED_KEY);

        foundCache = QuestHelper.getCompleted(QuestStageManager.FOUND_CACHE_KEY);
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
    protected void addBulletPoints(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        float initPad = pad;
        if (mode == IntelInfoPlugin.ListInfoMode.IN_DESC) initPad = opad;
        init();

        bullet(info);
        if(relation<=-0.50f && stage<=19)info.addPara("You need to get back to non-hostile relations with Kesteven, if you want to finish this job.", initPad, g, h, "non-hostile relations", "");

        //tip
        if (stage == 16 && !aliceTip) info.addPara("Talk to Alice about leads.", initPad, g, h, "", "");
        if (stage == 16 && !jackTip) info.addPara("Talk to Jack about leads.", initPad, g, h, "", "");

        //satellites
        int satelliteCount = DataSatelliteDialog.getRecoveredSatelliteCount();
        if (stage == 16 && !aliceTip && satelliteCount<2) info.addPara("Recover disks from the comms satellites.", initPad, g, h, "", "");
        if (stage == 16 && aliceTip && !QuestHelper.getCompleted(DataSatelliteDialog.RECOVERED_3_KEY)) {
            StarSystemAPI loc = QuestHelper.getJob3Target().getStarSystem();
            String constellation = QuestHelper.parseConstellation(loc.getConstellation().getNameWithType());
            info.addPara("Head to the " + loc.getName() + " in the " + constellation +
                    " and search for the satellite.", initPad, g, h, loc.getName(), constellation);
        }
        if (stage == 16 && aliceTip && !QuestHelper.getCompleted(DataSatelliteDialog.RECOVERED_4_KEY)) {
            StarSystemAPI loc = QuestHelper.getJob4EnemyTarget().getStarSystem();
            String constellation = QuestHelper.parseConstellation(loc.getConstellation().getNameWithType());
            info.addPara("Head to the " + loc.getName() + " in the " + constellation +
                    " and recover the disk from the satellite.", initPad, g, h, loc.getName(), constellation);
        }
        if (stage == 16 && aliceTip && jackTip && DataSatelliteDialog.getRecoveredSatelliteCount() >= 2 && !aliceTip2) {
            info.addPara("Talk to Alice about new leads.", initPad, g, h, "", "");
        }

        //Frost planet
        if (stage == 16 && aliceTip2 && !foundFrost) {
            StarSystemAPI tipSystem = QuestHelper.getJob5FrostTip();
            String constellation = QuestHelper.parseConstellation(tipSystem.getConstellation().getNameWithType());
            float distLY = Misc.getDistanceLY(tipSystem.getConstellation().getLocation(), MiscHelper.getFrost().getStar().getLocationInHyperspace())*1.5f;
            distLY *= 100f;
            distLY = Math.round(distLY);
            distLY /= 100f;

            info.addPara("Find the red dwarf system that is within " + distLY + " light-years of the " + constellation + ".", initPad, g, h, distLY + " light-years", constellation);
        }
        if (stage == 16 && aliceTip2 && foundFrost && !recoveredGlacier) {
            info.addPara("Find the comms facility on the tundra planet in the " + MiscHelper.getFrost().getName() + ".", initPad, g, h, MiscHelper.getFrost().getName(), "");
        }

        //Eliza
        if (stage == 16 && !foundEliza) info.addPara("Figure out where Eliza is hiding.", initPad, g, h, "", "");
        if (stage == 16 && jackTip && !foundEliza) info.addPara("Ask some pirates if they know anything about Eliza.", initPad, g, h, "", "");
        if (stage == 16 && !foundEliza && paid && ElizaSearchBarEvent.getDialogStage(ElizaSearchBarEvent.INTRO_DIALOG_KEY)==2){
            String loc = ElizaSearchBarEvent.getPaidForInfoTarget().getMarket().getName();
            String locSys = ElizaSearchBarEvent.getPaidForInfoTarget().getStarSystem().getName();
            info.addPara("Go to "+loc+" in "+locSys+" and speak with the contact.", initPad, g, h, loc, "");
        }
        if(QuestHelper.getElizaLoc()!=null) {
            String elizaLoc = QuestHelper.getElizaLoc().getMarket().getName();
            String elizaLocSys = QuestHelper.getElizaLoc().getStarSystem().getName();
            if (stage == 16 && foundEliza && !elizaDialog) info.addPara("Head to " + elizaLoc + " in "+elizaLocSys+" and meet with Eliza.", initPad, g, h, elizaLoc, "");
            if (stage == 16 && !helpEliza && raidEliza && !fightEliza && !killedEliza) info.addPara("Get the disks from Eliza, by force if necessary.", initPad, g, h, "", "");
        }
        //disks
        if (stage == 16 && jackTip && aliceTip && !aliceTip2 && !elizaDialog) info.addPara("Learn something about the last 2 disks.", initPad, g, h, "", "");
        if (stage == 16 && jackTip && aliceTip && !elizaDialog) {
            if (aliceTip2) {
                info.addPara("Learn something about the last disk.", initPad, g, h, "", "");
            }
        }
        if (stage == 16 && elizaDialog && !aliceTip2) info.addPara("Learn something about the last disk.", initPad, g, h, "", "");

        //go to base
        if (stage == 16 && allDisks) info.addPara("Head back to "+asteriaOrOutpost, initPad, g, h, asteriaOrOutpost, "");
        //disks recovered
        if (stage == 16 && !allDisks) info.addPara("Acquire all five disks.", initPad, g, h, "", "");
        if (stage == 16) info.addPara("Acquired " + recovered + " / 5 disks", initPad, g, h, "", "5");

        //go to cache
        if (stage == 17 && !foundCache) {
            info.addPara("Travel to the Cache site.", initPad, g, h, "", "");
        }
        //visited cache
        if (stage == 17 && foundCache) {
            info.addPara("Discover what's hidden in the Cache site.", initPad, g, h, "", "");
        }
        //defeated boss
        if (stage == 18) {
            info.addPara("Recover the Unlimited Production Chip.", initPad, g, h, "", "");
        }
        //turn in quest
        if (stage == 19 && !agreeEliza && !delivered) {
            info.addPara("Turn over the Unlimited Production Chip to "+asteriaOrOutpost+".", initPad, g, h, "", "");
        }
        if (stage == 19 && agreeEliza && !delivered) {
            String elizaLocSys = QuestHelper.getElizaLoc().getStarSystem().getName();
            info.addPara("Turn over the Unlimited Production Chip to "+ QuestHelper.getElizaLoc().getName()+" in "+elizaLocSys+".", initPad, g, h, QuestHelper.getElizaLoc().getName(), "");
        }
        if (stage == 19 && delivered) {
            String elizaLocSys = QuestHelper.getElizaLoc().getStarSystem().getName();
            info.addPara("Talk to Eliza at "+ QuestHelper.getElizaLoc().getName()+" in "+elizaLocSys+" once she has returned.", initPad, g, h, QuestHelper.getElizaLoc().getName(), "");
        }
        if (stage == 20) {
            info.addPara( "You managed to complete the mission.", initPad, g, h, "", "");
        }
        if (failed){
            info.addPara("You blew up Eliza and the Unlimited Production Chip was destroyed with her, it's secrets now lost to time. Mission failed.", initPad, g, h, "", "");
        }

        unindent(info);
    }

    //The function for writing the detailed info in the Intel screen.
    @Override
    public void createIntelInfo(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode) {
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

        if(stage==16)info.addPara("Keep searching.", opad, h, "", "");
        if(stage==17)info.addPara("Head to the Cache.", opad, h, "", "");
        if(stage==18)info.addPara("Recover the Chip.", opad, h, "", "");
        if(stage>=19 && !failed)info.addPara("Mission complete", opad, h, "", "");
        if(stage>=19 && failed) info.addPara("Mission failed", opad, tc,h, "", "");

        if(stage>=20) addDeleteButton(info, width);

        addBulletPoints(info, IntelInfoPlugin.ListInfoMode.IN_DESC);
    }

    //Sets which icon the Intel screen should display. Can vary based on circumstances, but a single one often works just fine
    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", "job2");
    }

    //This sets which "tags" the even has in the Intel screen. For example, giving it the Tags.INTEL_STORY tag makes it appear in the "Story" sub-category
    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_STORY);
        tags.add(Tags.INTEL_IMPORTANT);
        tags.add(Tags.INTEL_ACCEPTED);
        tags.add(Tags.INTEL_MISSIONS);
        return tags;
    }

    //Sorting-related; see it as a form of "how important is the even" thingy. Lower number = more important
    @Override
    public IntelInfoPlugin.IntelSortTier getSortTier() {
        return IntelInfoPlugin.IntelSortTier.TIER_2;
    }

    //What string to sort with, when sorting alphabetically
    public String getSortString() {
        return "The Delve";
    }

    //The name of the event; can vary based on circumstances. I decided to just make it say "completed" when completed
    public String getName() {
        return "The Delve";
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
        if(stage==16) loc = QuestHelper.asteriaOrOutpost().getPrimaryEntity();
        if(stage==17 || stage==18) loc = Global.getSector().getStarSystem("Unknown Site").getCenter();
        if(stage==19 && !agreeEliza) loc = QuestHelper.asteriaOrOutpost().getPrimaryEntity();
        if(stage==19 && agreeEliza) loc = QuestHelper.getElizaLoc();

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
