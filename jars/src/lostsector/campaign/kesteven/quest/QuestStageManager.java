package lostsector.campaign.kesteven.quest;

import lostsector.helper.fleet.FleetInfo;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.world.MoteParticleScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import exerelin.campaign.DiplomacyManager;
import lostsector.campaign.kesteven.ExileManager;
import lostsector.campaign.enigma.DormantSpawner;
import lostsector.campaign.events.EnvironmentalStorytelling;
import lostsector.dialogue.rules.nskr_altEndingDialogLuddic;
import lostsector.dialogue.rules.nskr_job4FleetDialog;
import lostsector.dialogue.rules.nskr_kestevenQuest;
import lostsector.dialogue.rules.nskr_ttCollectorDialog;
import lostsector.ModPlugin;
import lostsector.persistence.Saved;
import lostsector.helper.FleetHelper;
import lostsector.helper.Ids;
import lostsector.helper.MathHelper;
import lostsector.helper.MiscHelper;
import lostsector.world.systems.cache.Cache;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class QuestStageManager extends BaseCampaignEventListener implements EveryFrameScript  {
    //
    //manages quest stage changes and mission fleets
    //1000 line efs? no im fine, this works perfectly.
    public static final String KESTEVEN_QUEST_KEY = "nskr_kestevenQuest";
    public static final String FLEET_ARRAY_KEY = "$kQuestMissionFleets";
    public static final String QUEST_END_KEY = "KestevenQuestEnd";
    public static final String PERSISTENT_RANDOM_KEY = "KestevenQuestRandomKey";
    public static final String HAS_FOUGHT_ENIGMA_KEY = "KestevenQuest1HasFoughtEnigma";
    public static final String JOB1_DELIVERED_KEY = "KestevenQuest1Deliver";
    public static final String JOB1_DELIVERED_DATA_KEY = "KestevenQuest1DeliverData";
    public static final String JOB1_SENSORS_KEY = "KestevenQuest1Sensor";
    public static final String JOB1_TIP_KEY = "KestevenQuest1LocTip";
    public static final String JOB3_SKIP_KEY = "KestevenQuestSkip3";
    public static final String TT_COLLECTOR_KEY = "$KestevenQuestTTCollector";
    public static final String JOB3_TARGET_KEY = "$KestevenQuestJob3Target";
    public static final String JOB3_FAIL_KEY = "KestevenQuestJob3Fail";
    public static final String JOB3_TIMER_KEY = "KestevenQuestJob3Timer";
    public static final String JOB3_TARGET_DISCOVERED = "KestevenQuestJob3Discovered";
    public static final ArrayList<String> JOB3_MARKET_BLACKLIST = new ArrayList<>();
    static {
        JOB3_MARKET_BLACKLIST.add("eochu_bres");
        JOB3_MARKET_BLACKLIST.add("culann");
    }
    public static final String E_MESSENGER_TALKED_KEY = "KestevenQuestEMessengerTalkedKey";
    public static final String E_MESSENGER_TALKED_ASK_ABOUT_KEY = "KestevenQuestEMessengerTalkedKeyAskAbout";
    public static final String JOB4_WAIT_KEY = "KestevenQuestJob4WaitTimer";
    public static final String JOB4_SPLINTER_KEY = "$KestevenQuestJob4Splinter";
    public static final String JOB4_TARGET_KEY = "$KestevenQuestJob4Target";
    public static final String JOB4_FRIENDLY_KEY = "$KestevenQuestJob4Friendly";
    public static final String JOB4_DESTROYED_KEY = "KestevenQuestJob4Destroy";
    public static final String JOB4_FOUND_FRIENDLY_KEY = "KestevenQuestJob4FoundFriendly";
    public static final String JOB4_FOUND_TARGET_KEY = "KestevenQuestJob4FoundTarget";
    public static final String JOB4_TARGET_HINT_KEY = "KestevenQuestJob4TargetHint";
    public static final String JOB4_HELPED_KEY = "KestevenQuestJob4Help";
    public static final String JOB4_FAILED_KEY = "KestevenQuestJob4Fail";
    public static final String ARTIFACT_KEY = "$kQuestArtifact";
    public static final String JOB4_HINT_WRECK_ID_KEY = "$job4HintWreck";
    public static final String JOB5_FOUND_FROST_KEY = "nskr_kestevenQuestJob5FoundFrost";
    public static final String JOB5_FOUND_ELIZA_KEY = "KestevenQuestJob5FoundEliza";
    public static final String JOB5_FAILED_KEY = "KestevenQuestJob5Fail";
    public static final String KILLED_ELIZA_KEY = "KestevenQuestKilledEliza";
    public static final String DISK_COUNT_KEY = "KestevenQuestDiskCount";
    public static final String ALL_DISKS_RECOVERED_KEY = "KestevenQuestAllDisks";
    public static final String FOUND_CACHE_KEY = "KestevenQuestFoundCache";
    public static final String JACK_REVENGEANCE_FLEET_KEY = "$RevengeanceJack";
    public static final String REVENGEANCE_FLEET_KEY = "$RevengeanceQuestFleet";
    public static final String JACK_GONE_KEY = "KestevenQuestJob5JackRevengeance";
    public static final String ELIZA_BETRAY_KEY = "RevengeanceElizaBetrayByPlayer";
    public static final String ELIZA_INTERCEPT_FLEET_KEY = "$InterceptPlayerElizaFleet";
    public static final String ELIZA_INTERCEPT_HANDED_OVER = "$InterceptPlayerHandedUPCOver";
    public static final String ELIZA_INTERCEPT_TALKED = "InterceptPlayerElizaTalkedTo";
    public static final String ELIZA_RETURNED_KEY = "InterceptPlayerElizaReturn";

    public static final float JOB3_TIME_LIMIT = 90f;
    public static final int SPLINTER_COUNT = 10;
    public static final int JOB4_HELP_SUPPLIES = 250;
    public static final int JOB4_HELP_FUEL = 400;
    //chance per day
    public static final float REVENGEANCE_CHANCE = 0.01f;
    public static final float BASE_TT_COLLECT_CHANCE = 0.03f;
    public static final float TT_COLLECTOR_DESPAWN_TIMER = 60f;
    public static final float ELIZA_MAX_RELATION_KESTEVEN = -0.50f;
    public static final float ELIZA_MAX_RELATION_HEGEMONY = -0.35f;
    private float pingTimer = 0;

    private int stage =0;
    private int frameWait = 0;
    private int frameWait2 = 0;

    Saved<Boolean> intelStage1;
    Saved<Boolean> intelStage2;
    Saved<Boolean> intelStage3;
    Saved<Boolean> intelStage4;
    Saved<Boolean> intelStage5;
    Saved<Boolean> barStage5;
    Saved<Boolean> jobFleetSpawned3;
    Saved<Boolean> jobFleetsSpawned4;
    Saved<Float> counterJob;
    Saved<Float> counter;
    Saved<Float> fleetCounter;
    Saved<Float> cacheTimer;
    Saved<Boolean> collected;
    Saved<Boolean> cacheGuardian;
    Saved<Boolean> cacheIntelAdd;
    Saved<Boolean> commission;
    Saved<Boolean> doubted;
    Saved<Boolean> cacheLoc;
    Saved<Boolean> revengeFleetSpawned;
    Saved<Boolean> elizaBetray;
    Saved<Boolean> elizaInterceptSpawned;
    private final List<CampaignFleetAPI> removed = new ArrayList<>();

    public QuestStageManager() {
        super(false);
        //logic timer
        this.counter = new Saved<>("questCounter", 0.0f);
        this.fleetCounter = new Saved<>("questFleetCounter", 0.0f);
        //one time intel check
        this.intelStage1 = new Saved<>("questIntelStage1", false);
        this.intelStage2 = new Saved<>("questIntelStage2", false);
        this.intelStage3 = new Saved<>("questIntelStage3", false);
        this.intelStage4 = new Saved<>("questIntelStage4", false);
        this.intelStage5 = new Saved<>("questIntelStage5", false);
        this.barStage5 = new Saved<>("questBarStage5", false);
        this.cacheIntelAdd = new Saved<>("cacheIntelAddStage5", false);
        //fixes commission rep changes
        this.commission = new Saved<>("commissionStage5", false);
        //one time fleet spawn
        this.jobFleetSpawned3 = new Saved<>("questJobFleetSpawned3", false);
        this.jobFleetsSpawned4 = new Saved<>("questJobFleetsSpawned4", false);
        this.collected = new Saved<>("questJobCollected", false);
        this.doubted = new Saved<>("questJobCacheDoubt", false);
        this.cacheLoc = new Saved<>("questJobCacheLoc", false);
        this.cacheGuardian = new Saved<>("questJobCacheGuardian", false);
        this.revengeFleetSpawned = new Saved<>("questJobVengeanced", false);
        this.elizaBetray = new Saved<>("questJobelizaBetray", false);
        this.elizaInterceptSpawned = new Saved<>("questJobelizadIntercept", false);
        //the one-month wait for job
        this.counterJob = new Saved<>("questCounterJob", 0.0f);
        this.cacheTimer = new Saved<>("questCacheTimer", 0.0f);

    }

    static void log(final String message) {
        Global.getLogger(QuestStageManager.class).info(message);
    }

    public boolean isDone() {
        return false;
    }
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;

        stage = QuestHelper.getStage();

        //both mission markets are fucked failure
        if (!ExileManager.canExile() && !MiscHelper.asteriaExists() && !QuestHelper.getEndMissions()){
            QuestHelper.setStage(99);
            QuestHelper.setEndMissions(true);
            log("ERROR sector is fucked, ending missions");
        }
        //kill Special Operations fleet failure
        if (stage == 14 && QuestHelper.getCompleted(JOB4_FAILED_KEY) && !QuestHelper.getEndMissions()) {
            QuestHelper.setStage(99);
            QuestHelper.setEndMissions(true);

        }
        //kill Eliza after handing over failure
        if (stage == 19 && QuestHelper.getCompleted(ELIZA_INTERCEPT_HANDED_OVER) && QuestHelper.getCompleted(KILLED_ELIZA_KEY) && !QuestHelper.getEndMissions()) {
            QuestHelper.setCompleted(true, JOB5_FAILED_KEY);

            QuestHelper.setStage(99);
            QuestHelper.setEndMissions(true);

        }
        //////////////////
        //done while paused
        //////////////////
        //finish job1
        if (stage ==1) {
            boolean delivered = QuestHelper.getCompleted(JOB1_DELIVERED_KEY);
            boolean deliveredData = QuestHelper.getCompleted(JOB1_DELIVERED_DATA_KEY);
            //finished tasks
            if (delivered && deliveredData) {
                QuestHelper.setStage(2);
            }
        }
        //start job5
        if (stage ==15) {
            //no longer used, now manually spawn by barEventFixer
            //BAR EVENT
            //if (!barStage5.val) {
                //PortsideBarData.getInstance().addEvent(new KQuest5Bar());
                //log("Qmanager added bar event for job5");
                //barStage5.val = true;
            //}
            //cache found check
            if (!QuestHelper.getCompleted(FOUND_CACHE_KEY) && Global.getSector().getStarSystem("Unknown Site").isEnteredByPlayer()) {
                QuestHelper.setCompleted(true, FOUND_CACHE_KEY);
            }
        }
        //eliza loc changer
        if (stage>=16 && QuestHelper.getCompleted(JOB5_FOUND_ELIZA_KEY) && !QuestHelper.getCompleted(KILLED_ELIZA_KEY)) {
            if(QuestHelper.getElizaLoc().getMarket().isPlanetConditionMarketOnly()){
                log("Qmanager eliza loc deciv, changing");
                PersonAPI eliza = MiscHelper.getEliza();
                String oldLoc = QuestHelper.getElizaLoc().getMarket().getPrimaryEntity().getName();
                //new loc
                QuestHelper.setElizaLoc();
                //update
                QuestHelper.getElizaLoc().getMarket().getCommDirectory().addPerson(eliza,1);
                QuestHelper.getElizaLoc().getMarket().addPerson(eliza);
                //fix contact
                if(ContactIntel.getContactIntel(eliza)!=null && ContactIntel.getContactIntel(eliza).getState()==ContactIntel.ContactState.LOST_CONTACT_DECIV){
                    ContactIntel.getContactIntel(eliza).setState(ContactIntel.ContactState.PRIORITY);
                }
                //text
                Global.getSector().getCampaignUI().addMessage("With the conditions deteriorating on "+oldLoc+", Eliza has moved her operations to "+ QuestHelper.getElizaLoc().getMarket().getName()+".",
                        Global.getSettings().getColor("standardTextColor"),
                        "Eliza",
                        "",
                        Global.getSector().getFaction(Factions.PIRATES).getColor(),
                        Global.getSettings().getColor("yellowTextColor"));
            }
        }
        //paid for loc changer
        if (stage==16 && QuestHelper.getCompleted(ElizaSearchBarEvent.PAID_FOR_INFO) && !QuestHelper.getCompleted(QuestStageManager.JOB5_FOUND_ELIZA_KEY) && ElizaSearchBarEvent.getDialogStage(ElizaSearchBarEvent.INTRO_DIALOG_KEY)==2){
            if(ElizaSearchBarEvent.getPaidForInfoTarget().getMarket().isPlanetConditionMarketOnly()){
                //remove important
                if (ElizaSearchBarEvent.getPaidForInfoTarget().getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_MISSION_IMPORTANT)){
                    ElizaSearchBarEvent.getPaidForInfoTarget().getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
                }
                log("Qmanager paid for loc deciv, changing");
                String oldLoc = ElizaSearchBarEvent.getPaidForInfoTarget().getMarket().getPrimaryEntity().getName();
                //new loc
                ElizaSearchBarEvent.setPaidForInfoTarget();
                //make important, again
                ElizaSearchBarEvent.getPaidForInfoTarget().getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MISSION_IMPORTANT,true);
                //text
                Global.getSector().getCampaignUI().addMessage("With the conditions deteriorating on "+oldLoc+", the contact has moved their operations to "+ ElizaSearchBarEvent.getPaidForInfoTarget().getName()+".",
                        Global.getSettings().getColor("standardTextColor"),
                        "the contact",
                        "",
                        Global.getSector().getFaction(Factions.PIRATES).getColor(),
                        Global.getSettings().getColor("yellowTextColor"));
            }
        }
        if (stage==16) {
            //cache found, before mission
            if (QuestHelper.getCompleted(FOUND_CACHE_KEY)) {
                QuestHelper.setStage(17);
            }
        }
        //////////////////
        //pause check
        //////////////////
        if (Global.getSector().isPaused()) return;
        //timer
        if (Global.getSector().isInFastAdvance()) {
            counter.val += 2f*amount;
            fleetCounter.val += 2f*amount;
        } else{
            counter.val += amount;
            fleetCounter.val += amount;
        }
        //FLEETS
        List<FleetInfo> fleets = FleetHelper.getFleets(FLEET_ARRAY_KEY);
        //log("FleetInfo size "+fleets.size());

        //DEBUG CODE
        //TODO
        // undo
        //QuestUtil.setCompleted(true, ArtifactDialog.RECOVERED_4_KEY);
        //QuestUtil.setCompleted(true, ArtifactDialog.RECOVERED_3_KEY);
        //ArtifactDialog.setRecoveredSatelliteCount(2);
        //QuestUtil.setCompleted(true, JOB5_FOUND_FROST_KEY);
        //QuestUtil.setCompleted(true, GlacierCommsDialog.RECOVERED_KEY);
        //QuestUtil.setCompleted(true, nskr_kestevenQuest.JOB5_ALICE_TIP_KEY);
        //QuestUtil.setCompleted(true, nskr_kestevenQuest.JOB5_ALICE_TIP_KEY2);
        //QuestUtil.setCompleted(true, nskr_kestevenQuest.JOB5_JACK_TIP_KEY);
        //QuestUtil.setCompleted(true, JOB5_FOUND_ELIZA_KEY);
        //QuestUtil.setCompleted(true, ElizaDialog.DIALOG_FINISHED_KEY);
        //QuestUtil.setCompleted(true, ElizaDialog.ELIZA_HELP_KEY);

        //if(QuestUtil.getElizaLoc()==null) {
        //    QuestUtil.setElizaLoc();
        //    PersonAPI eliza = Gen.genEliza();
        //    QuestUtil.getElizaLoc().getMarket().getCommDirectory().addPerson(eliza, 1);
        //    QuestUtil.getElizaLoc().getMarket().addPerson(eliza);
        //    log("Eliza loc " + QuestUtil.getElizaLoc().getMarket().getName());
        //}

        //QuestUtil.setCompleted(true, FOUND_CACHE_KEY);
        //if (QuestUtil.getDisksRecovered()==0) {
        //    QuestUtil.setDisksRecovered(5);
        //}

       //int targetStage = 15;
       //int s = stage;
       //frameWait2++;
       //if (frameWait2>50) {
       //    if (stage < targetStage) {
       //        frameWait2=0;
       //        s++;
       //        QuestUtil.setStage(s);
       //    }
       //}
       //if (Global.getSector().getFaction(Factions.PLAYER).getRelationship("kesteven")<0.9f){
       //    Global.getSector().getFaction(Factions.PLAYER).setRelationship("kesteven", 1f);
       //}

        //start job 1
        if (stage ==1) {
            //Adds our intel
            if (!intelStage1.val) {
                EnemyUnknownIntel intel1 = new EnemyUnknownIntel();
                Global.getSector().getIntelManager().addIntel(intel1, false);
                intelStage1.val = true;
                log("Qmanager added INTEL for " + "Enemy Unknown");
            }
            //logic
        }
        //wait for job 4
        if (stage == 11) {
            if (Global.getSector().isInFastAdvance()) {
                counterJob.val += 2f*amount;
            } else{
                counterJob.val += amount;
            }
            //one day = 10f
            if (counterJob.val>(300f)){
                QuestHelper.setCompleted(true, JOB4_WAIT_KEY);
                counterJob.val = 0f;
            }
        }
        //start job 3
        if (stage ==8) {
            //Adds our intel
            if (!intelStage3.val) {
                HostileTakeoverIntel intel3 = new HostileTakeoverIntel();
                Global.getSector().getIntelManager().addIntel(intel3, false);
                intelStage3.val = true;
                log("Qmanager added INTEL for " + "Hostile Takeover");
                //BAR EVENT
                PortsideBarData.getInstance().addEvent(new HostileTakeoverBarEvent());
                //DORMANT fleet at target
                MiscHelper.addDormant(QuestHelper.getJob3Target(), "enigma", 45f, 50f, 0f, 1f, 1f, 1f, 1, 1);
                log("Qmanager added dormant to target " + QuestHelper.getJob3Target().getName() +" "+ QuestHelper.getJob3Target().getStarSystem().getName());
            }
            //logic
            if (!jobFleetSpawned3.val) {
                CampaignFleetAPI fleet = QuestFleets.spawnJob3TargetFleet();
                fleets.add(new FleetInfo(fleet, QuestHelper.getJob3Target(), QuestHelper.getJob3Start()));
                QuestHelper.spawnArtifact(QuestHelper.getJob3Target(),3);
                log("Qmanager spawn job3 target");
                jobFleetSpawned3.val = true;
            }
        }
        //start job 4
        if (stage ==12) {
            //Adds our intel
            if (!intelStage4.val) {
                OperationLifesaverIntel intel4 = new OperationLifesaverIntel();
                Global.getSector().getIntelManager().addIntel(intel4, false);
                intelStage4.val = true;
                log("Qmanager added INTEL for " + "Operation Lifesaver");
            }
            //logic
            if (!jobFleetsSpawned4.val) {

                //this sets job4TargetLoc
                QuestFleets.spawnJob4Target();

                QuestHelper.spawnArtifact(QuestHelper.getJob4EnemyTarget(),4);
                CampaignFleetAPI fleet = QuestFleets.spawnJob4Friendly();
                fleets.add(new FleetInfo(fleet, null, QuestHelper.getJob4FriendlyTarget()));
                for (int x = 0; x<SPLINTER_COUNT;x++) {
                    new Pair<>(QuestFleets.spawnJob4Splinters(), 0f);
                    //added to mem in the spawner
                }
                //hint wrecks/environmental storytelling
                spawnJob4Wrecks(nskr_kestevenQuest.getRandom());

                log("Qmanager spawn job4 fleets");
                jobFleetsSpawned4.val = true;
            }
        }
        //job 4 logic
        //dialog reveal logic
        if (nskr_job4FleetDialog.getDialogStage(nskr_job4FleetDialog.PERSISTENT_KEY)>=1 && !QuestHelper.getCompleted(JOB4_FOUND_TARGET_KEY) && !QuestHelper.getCompleted(JOB4_DESTROYED_KEY)){

            QuestHelper.setCompleted(true, JOB4_TARGET_HINT_KEY);
        }
        //job 4
        //found friendly
        if (nskr_job4FleetDialog.getDialogStage(nskr_job4FleetDialog.PERSISTENT_KEY)>=1 && !QuestHelper.getCompleted(JOB4_FOUND_FRIENDLY_KEY)){

            QuestHelper.setCompleted(true, JOB4_FOUND_FRIENDLY_KEY);
        }

        //job 4 completion
        if (stage ==12 && QuestHelper.getCompleted(JOB4_FOUND_FRIENDLY_KEY) && QuestHelper.getCompleted(JOB4_DESTROYED_KEY)){
            //completion text
            Global.getSector().getCampaignUI().addMessage("With the threat eliminated and the Operations fleet located, you can report back to "+ QuestHelper.asteriaOrOutpost().getName()+" to finish the job.",
                    Global.getSettings().getColor("standardTextColor"),
                    "report back to "+ QuestHelper.asteriaOrOutpost().getName(),
                    "",
                    Global.getSettings().getColor("yellowTextColor"),
                    Global.getSettings().getColor("yellowTextColor"));

            QuestHelper.setStage(13);
        }
        //start job 5
        if (stage==16 || stage==17) {
            //Adds our intel
            if (!intelStage5.val) {
                TheDelveIntel intel5 = new TheDelveIntel();
                Global.getSector().getIntelManager().addIntel(intel5, false);
                intelStage5.val = true;
                log("Qmanager added INTEL for " + "The Delve");
                //BAR EVENTS
                if (stage==16) PortsideBarData.getInstance().addEvent(new ElizaSearchBarEvent());
                if (stage==16) PortsideBarData.getInstance().addEvent(new ElizaSearchSecondBarEvent());
                if (stage==16) PortsideBarData.getInstance().addEvent(new ElizaSearchFinalBarEvent());
            }
        }
        if (stage==16) {
           //job 5 logic
           //found frost check
           if (!QuestHelper.getCompleted(JOB5_FOUND_FROST_KEY) && QuestHelper.getCompleted(nskr_kestevenQuest.JOB5_ALICE_TIP_KEY2)){
               if (pf.getContainingLocation()!=null && pf.getContainingLocation()== MiscHelper.getFrost()){
                   //found
                   QuestHelper.setCompleted(true, JOB5_FOUND_FROST_KEY);
               }
           }
           //all disks found check
           if (QuestHelper.getDisksRecovered()>=5){
                QuestHelper.setCompleted(true, ALL_DISKS_RECOVERED_KEY);
           }
            //cache found check
            if (!QuestHelper.getCompleted(FOUND_CACHE_KEY) && Global.getSector().getStarSystem("Unknown Site").isEnteredByPlayer()) {
                QuestHelper.setCompleted(true, FOUND_CACHE_KEY);
                QuestHelper.setStage(17);
            }
        }
        //cache found, no mission
        if (QuestHelper.getEndMissions() && Global.getSector().getStarSystem("Unknown Site").isEnteredByPlayer()){
            QuestHelper.setCompleted(true, FOUND_CACHE_KEY);
        }
        //add intel for cache
        if (QuestHelper.getCompleted(FOUND_CACHE_KEY)){
            if (!cacheIntelAdd.val) {
                CacheIntel cacheIntel = new CacheIntel(Global.getSector().getStarSystem("Unknown Site"));
                Global.getSector().getIntelManager().addIntel(cacheIntel, false);
                cacheIntelAdd.val = true;
                log("Qmanager added INTEL for " + "The Cache");
            }
        }

        //spawn cache guardian fleet
        if (pf.getStarSystem() != null && pf.getStarSystem()==Global.getSector().getStarSystem("Unknown Site")) {
            if (!cacheGuardian.val) {
                //timer for spawn
                if (Global.getSector().isInFastAdvance()) {
                    cacheTimer.val += 2f * amount;
                } else {
                    cacheTimer.val += amount;
                }
                //timer for ping
                if (Global.getSector().isInFastAdvance()) {
                    pingTimer += 2f * amount;
                } else {
                    pingTimer += amount;
                }
                //press X to doubt
                if (cacheTimer.val > 35f) {
                    if (!cacheLoc.val) {
                        QuestHelper.setCacheFleetLoc();
                        cacheLoc.val = true;
                    }
                    CampaignUIAPI ui = Global.getSector().getCampaignUI();
                    //stage check
                    if (stage>=16 && !QuestHelper.getEndMissions() && !doubted.val) {
                        //UI check
                        if (!ui.isShowingDialog() && !ui.isShowingMenu()) {
                            Global.getSector().getCampaignUI().showInteractionDialog(new CacheDoubtDialog(), null);
                            doubted.val = true;
                        }
                    }
                }
                //PINGS
                //slow
                if (cacheTimer.val > 45f && cacheTimer.val < 75f) {
                    if (pingTimer>6f) {
                        spawnPing(pf);
                        pingTimer = 0f;
                    }
                }
                //fast
                if (cacheTimer.val >= 75f) {
                    if (pingTimer>3f) {
                        spawnPing(pf);
                        pingTimer = 0f;
                    }
                }
                if (cacheTimer.val > 90f) {
                    //start music
                    Global.getSector().getStarSystem(Ids.CACHE_SYSTEM_NAME).getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "nskr_cache_theme");
                    //add to mission fleets so we can track it
                    CampaignFleetAPI fleet = Cache.spawnGuardianFleet(pf, QuestHelper.getCacheFleetLoc());
                    Global.getSoundPlayer().playSound("ui_discovered_entity", 1f, 1f, fleet.getLocation(), new Vector2f());

                    fleets.add(new FleetInfo(fleet, null, QuestHelper.getCacheFleetLoc()));
                    cacheGuardian.val = true;
                }
            }
            //cache mote particles
            if (Math.random()<0.004f)MoteParticleScript.spawnMote(pf);
        }
        //job5 finish commission unfucker Eliza
        if (QuestHelper.getCompleted(EndingElizaDialog.COMMISSION_RESTORE_KEY) && !commission.val){
            //have to wait a few frames for the vanilla commission to end
            frameWait++;
            if (frameWait==30) {
                float repPirates = QuestHelper.getFloat(EndingElizaDialog.REP_PIRATES_KEY);
                float repKesteven = QuestHelper.getFloat(EndingElizaDialog.REP_KESTEVEN_KEY);
                float repHege = QuestHelper.getFloat(EndingElizaDialog.REP_HEGE_KEY);
                //pirates
                if (Global.getSector().getFaction(Factions.PIRATES).getRelationship(Factions.PLAYER) <= repPirates) {
                    Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.PIRATES, repPirates);
                }
                //kesteven
                if (Global.getSector().getFaction(Factions.PLAYER).getRelationship("kesteven") >= repKesteven) {
                    Global.getSector().getFaction(Factions.PLAYER).setRelationship("kesteven", repKesteven);
                }
                //hege
                if (Global.getSector().getFaction(Factions.PLAYER).getRelationship(Factions.HEGEMONY) >= repHege) {
                    Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.HEGEMONY, repHege);
                }
                //IS
                if(ModPlugin.IS_IRONSHELL){
                    if (Global.getSector().getFaction(Factions.PLAYER).getRelationship("ironshell") >= repHege) {
                        Global.getSector().getFaction(Factions.PLAYER).setRelationship("ironshell", repHege);
                    }
                }
                commission.val = true;
                //log("COMMISSION "+repPirates+" "+repKesteven+" "+repHege);
            }
        }
        //player betrays Eliza after completing the mission for her
        if (QuestHelper.getElizaLoc()!=null) {
            if (!QuestHelper.getElizaLoc().getMarket().isPlanetConditionMarketOnly()) {
                if (QuestHelper.getElizaLoc().getMarket().getFaction().getId().equals(Factions.PLAYER) &&
                        QuestHelper.getCompleted(ElizaDialog.ELIZA_HELP_KEY) && !QuestHelper.getCompleted(ELIZA_BETRAY_KEY) && QuestHelper.getCompleted(EndingElizaDialog.DIALOG_FINISHED_KEY)) {
                    QuestHelper.setCompleted(true, ELIZA_BETRAY_KEY);
                }
            }
        }
        //mission logic
        if (counter.val>10f) {
            //tt vengeance spawner
            //spawn once per campaign
            if (!collected.val) {
                //can spawn check
                if (stage>=2 && stage<=15) {
                    boolean cargo = pf.getCargo().getCommodityQuantity("nskr_electronics") >= 50f;
                    Random random = nskr_ttCollectorDialog.getRandom();
                    if (random.nextFloat() < BASE_TT_COLLECT_CHANCE && pf.isInHyperspace() && pf.getLocation().length() < 25000f && cargo) {

                        CampaignFleetAPI fleet = QuestFleets.spawnCollectorFleet();
                        fleets.add(new FleetInfo(fleet, null, fleet.getContainingLocation().createToken(fleet.getLocation())));

                        collected.val = true;
                        log("Qmanager SPAWNING");
                    }
                }
            }
            if (!elizaInterceptSpawned.val){
                //eliza intercept player for UPC
                if (stage==19 && QuestHelper.getCompleted(ElizaDialog.ELIZA_HELP_KEY)){
                    CampaignFleetAPI fleet = vengeanceEliza(true);
                    elizaInterceptSpawned.val = true;
                    log("Intercepted Eliza");
                }
            }
            //vengeance Eliza betray by player, after questline
            if (QuestHelper.getCompleted(ELIZA_BETRAY_KEY) && !elizaBetray.val){
                CampaignFleetAPI fleet = vengeanceEliza(false);
                elizaBetray.val = true;
            }
            //revengeace fleet spawner
            if (getRandom().nextFloat()<REVENGEANCE_CHANCE && stage == 20 && !revengeFleetSpawned.val) {
                //jack
                if (QuestHelper.getCompleted(EndingElizaDialog.DIALOG_FINISHED_KEY) || QuestHelper.getCompleted(nskr_altEndingDialogLuddic.DIALOG_FINISHED_KEY)){
                    CampaignFleetAPI fleet = vengeanceJack();
                    revengeFleetSpawned.val = true;
                    log("Revengeanced Jack");
                }
            }
            //END
            counter.val = 0f;
        }
        //fleet logic once a seconds (10s is a day)
        if (fleetCounter.val>1f){
            //fleet logic
            runFleetLogic(fleets);

            //clean the list
            FleetHelper.cleanUp(removed, fleets);
            removed.clear();
            //save to mem
            FleetHelper.setFleets(fleets, FLEET_ARRAY_KEY);

            //
            fleetCounter.val = 0f;
            //log("MANAGING " + fleets.size() + " fleets");
            //for (FleetInfo f : fleets){
            //    log(f.fleet.getName());
            //}
        }
    }

    private void respawnEliza(SectorEntityToken loc) {
        PersonAPI eliza = MiscHelper.getEliza();
        //add eliza to market
        loc.getMarket().getCommDirectory().addPerson(eliza,1);
        loc.getMarket().addPerson(eliza);

        //check for ending
        QuestHelper.setCompleted(true, ELIZA_RETURNED_KEY);
    }

    public static void spawnJob4Wrecks(Random random) {
        SectorEntityToken loc = QuestHelper.getJob4EnemyTarget();
        StarSystemAPI system = loc.getStarSystem();

        //debris
        DebrisFieldTerrainPlugin.DebrisFieldParams params_debrisField = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                350f, // field radius - should not go above 1000 for performance reasons
                1.2f, // density, visual - affects number of debris pieces
                10000000f, // duration in days
                0f); // days the field will keep generating glowing pieces
        params_debrisField.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
        params_debrisField.baseSalvageXP = 500; // base XP for scavenging in field
        SectorEntityToken debrisField = Misc.addDebrisField(system, params_debrisField, random);
        debrisField.setSensorProfile(1000f);
        debrisField.setDiscoverable(true);
        float dist = MathHelper.getSeededRandomNumberInRange(150f, 250f, random);
        float days = MathHelper.getSeededRandomNumberInRange(30f, 60f, random);
        float angle = MathHelper.getSeededRandomNumberInRange(0f, 360f, random);
        debrisField.setCircularOrbit(loc, angle, dist, days);
        debrisField.setId("nskr_debrisField_"+random.nextLong());

        //ships
        float recoveryChance = 0.25f;
        int count = 3;
        for (int y=0;y<count;y++) {
            SectorEntityToken derelict = EnvironmentalStorytelling.addDerelict(
                    system, pickRandomVariant(random), EnvironmentalStorytelling.randomCondition(), Math.random() < recoveryChance, null
            );
            derelict.setCircularOrbit(loc,
                    angle+ MathHelper.getSeededRandomNumberInRange(-45f, 45f, random),
                    dist+ MathHelper.getSeededRandomNumberInRange(-125f, 125f, random),
                    days+ MathHelper.getSeededRandomNumberInRange(-3f, 3f, random));
            if (y==0) {
                //mark one of the wrecks
                derelict.setId(JOB4_HINT_WRECK_ID_KEY +random.nextLong());
            }
        }

    }
    private static String pickRandomVariant(Random random) {
        FactionAPI faction = Global.getSector().getFaction(Ids.KESTEVEN_FACTION_ID);
        ArrayList<String> variants = new ArrayList<>();
        String variant = "";
        while (variants.isEmpty()) {
            variants = new ArrayList<>(faction.getVariantsForRole(EnvironmentalStorytelling.randomRole(random, false)));
            variant = variants.get(MathHelper.getSeededRandomNumberInRange(0, variants.size() - 1, random));
            //only pick kesteven ships
            if (!Global.getSettings().getVariant(variant).getHullSpec().hasTag("kesteven")){
                variants.clear();
            }
        }
        return variant;
    }

    private void spawnPing(CampaignFleetAPI pf) {
        SectorEntityToken loc = QuestHelper.getCacheFleetLoc();
        if (MathUtils.getDistance(pf.getLocation(), loc.getLocation()) > 1000f) {
            float angle = VectorUtils.getAngle(pf.getLocation(), loc.getLocation());
            Vector2f newLoc = MathUtils.getPointOnCircumference(pf.getLocation(), 1000f, angle + MathUtils.getRandomNumberInRange(-20f, 20f));
            loc = pf.getContainingLocation().createToken(newLoc);
        }
        Global.getSector().addPing(loc, Pings.SENSOR_BURST);
    }

    private void runFleetLogic(List<FleetInfo> fleets){
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;

        //mission related fleets manager
        for (FleetInfo f : fleets) {
            CampaignFleetAPI fleet = f.fleet;
            //age update
            f.age+=0.1f;

            //job 3 target fleet manager
            if (fleet.getMemoryWithoutUpdate().contains(JOB3_TARGET_KEY)) {
                //
                job3TargetLogic(f, fleet);
                continue;
            }

            //job 4 fleet manager
            if (fleet.getMemoryWithoutUpdate().contains(JOB4_SPLINTER_KEY) || fleet.getMemoryWithoutUpdate().contains(JOB4_TARGET_KEY) || fleet.getMemoryWithoutUpdate().contains(JOB4_FRIENDLY_KEY)) {
                //
                job4TargetLogic(f, fleet);
                continue;
            }
            //aggro dormant manager
            if (fleet.getMemoryWithoutUpdate().contains(DormantSpawner.DORMANT_KEY)){
                boolean despawn = false;

                //time despawn
                if (f.age>30f) {
                    despawn = true;
                }
                //destroyed
                if (fleet.getFleetPoints()<=0) {
                    despawn = true;
                }

                Vector2f fp = fleet.getLocationInHyperspace();
                Vector2f pp = pf.getLocationInHyperspace();
                float dist = MathUtils.getDistance(pp, fp);
                if (despawn) {
                    if (dist > Global.getSettings().getMaxSensorRangeHyper()) {
                        //tracker for cleaning the list
                        removed.add(fleet);
                        fleet.despawn();
                    }
                }
                if (despawn) continue;
                //assignment logic
                FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
                if (curr == null) {
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.HOLD, fleet.getContainingLocation().createToken(fleet.getLocation()), Float.MAX_VALUE, "holding");
                    log("null assignment");
                }
                //used special maneuvers
                if (curr!=null && curr.getAssignment()==FleetAssignment.STANDING_DOWN) {
                    CampaignFleetAIAPI ai = fleet.getAI();
                    if (ai instanceof ModularFleetAIAPI) {
                        // needed to interrupt an in-progress pursuit
                        ModularFleetAIAPI m = (ModularFleetAIAPI) ai;
                        m.getStrategicModule().getDoNotAttack().add(pf, 1f);
                        m.getTacticalModule().setTarget(null);
                    }
                }
                //logic
                if (pf.isVisibleToSensorsOf(fleet)){
                    if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.INTERCEPT) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.INTERCEPT, pf, Float.MAX_VALUE, "intercepting your fleet");
                    }
                } else if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.PATROL_SYSTEM){
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, fleet.getStarSystem().getCenter(), Float.MAX_VALUE, "patrolling");
                }
                continue;
            }

            //tt collector logic
            if (fleet.getMemoryWithoutUpdate().contains(TT_COLLECTOR_KEY)){
                boolean despawn = false;

                if (fleet.getFleetPoints()<=0f){
                    despawn = true;
                    log("Qmanager despawn defeated");
                }
                if (f.age>TT_COLLECTOR_DESPAWN_TIMER){
                    despawn = true;
                    log("Qmanager despawn time");
                }

                Vector2f fp = fleet.getLocationInHyperspace();
                Vector2f pp = pf.getLocationInHyperspace();
                float dist = MathUtils.getDistance(pp, fp);
                if (despawn) {
                    if (dist > Global.getSettings().getMaxSensorRangeHyper()) {
                        //tracker for cleaning the list
                        removed.add(fleet);
                        fleet.despawn();
                    }
                }
                //stop here when defeated
                if (despawn) continue;
                //assignment logic
                FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
                if (curr == null) {
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.HOLD, fleet.getContainingLocation().createToken(fleet.getLocation()), Float.MAX_VALUE, "holding");
                    log("null assignment");
                }
                //used special maneuvers
                if (curr!=null && curr.getAssignment()==FleetAssignment.STANDING_DOWN) {
                    CampaignFleetAIAPI ai = fleet.getAI();
                    if (ai instanceof ModularFleetAIAPI) {
                        // needed to interrupt an in-progress pursuit
                        ModularFleetAIAPI m = (ModularFleetAIAPI) ai;
                        m.getStrategicModule().getDoNotAttack().add(pf, 1f);
                        m.getTacticalModule().setTarget(null);
                    }
                }
                //logic
                boolean paid = nskr_ttCollectorDialog.getPaid(nskr_ttCollectorDialog.PERSISTENT_KEY);
                //AI LOGIC
                //intercept
                if (!paid) {
                    FleetHelper.gotoAndInterceptPlayerAI(fleet, f, FleetHelper.interceptBehaviour.AROUND);
                }
                //leave
                if (paid) {
                    if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.GO_TO_LOCATION_AND_DESPAWN) {
                        fleet.clearAssignments();
                        fleet.getMemoryWithoutUpdate().clear();
                        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);

                        SectorEntityToken loc = QuestHelper.getRandomFactionMarket(new Random(), Factions.TRITACHYON);
                        if (loc != null && loc.getMarket() != null) {
                            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, loc, Float.MAX_VALUE, "returning to " + loc.getName());
                            log("Qmanager " + fleet.getName() + " RETURNING ");
                        }
                    }
                }
                continue;
            }
            //cache guardian fleet
            if (fleet.getMemoryWithoutUpdate().contains(Cache.CACHE_FLEET_KEY)){
                boolean despawn = false;

                //no prots
                if (!Cache.hasPrototypes(fleet)) {
                    despawn = true;
                    log("Qmanager cache NO PROTS");
                }
                //destroyed
                if (fleet.getFleetPoints() <= 0) {
                    despawn = true;
                    log("Qmanager cache no fleet");
                }
                //despawn = true;
                //cacheGuardian.val = false;

                Vector2f fp = fleet.getLocationInHyperspace();
                Vector2f pp = pf.getLocationInHyperspace();
                float dist = MathUtils.getDistance(pp, fp);
                if (despawn) {
                    if (dist > Global.getSettings().getMaxSensorRangeHyper()) {
                        //tracker for cleaning the list
                        removed.add(fleet);
                        fleet.despawn();
                    }
                }
                //stop here when defeated
                if (despawn) continue;
                //assignment logic
                FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
                if (curr == null) {
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.HOLD, fleet.getContainingLocation().createToken(fleet.getLocation()), Float.MAX_VALUE, "holding");
                    log("null assignment");
                }
                //used special maneuvers
                if (curr!=null && curr.getAssignment()==FleetAssignment.STANDING_DOWN) {
                    CampaignFleetAIAPI ai = fleet.getAI();
                    if (ai instanceof ModularFleetAIAPI) {
                        // needed to interrupt an in-progress pursuit
                        ModularFleetAIAPI m = (ModularFleetAIAPI) ai;
                        m.getStrategicModule().getDoNotAttack().add(pf, 1f);
                        m.getTacticalModule().setTarget(null);
                    }
                }
                //logic

                if (pf.getStarSystem() != null && pf.getStarSystem() == fleet.getStarSystem()) {
                    if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.INTERCEPT) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.INTERCEPT, pf, Float.MAX_VALUE, "error #406, try again?");
                    }
                } else {
                    if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.ORBIT_PASSIVE) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, fleet.getStarSystem().getCenter(), Float.MAX_VALUE, "error #406, try again?");
                    }
                }
                continue;
            }
            //eliza fleet, after raiding her
            if (fleet.getMemoryWithoutUpdate().contains(QuestFleets.ELIZA_RAIDED_FLEET_KEY)){
                boolean despawn = false;

                //destroyed
                if (fleet.getFleetPoints()<=0) {
                    despawn = true;
                }
                //eliza check
                for (FleetMemberAPI m : fleet.getFleetData().getMembersListWithFightersCopy()){
                    if (m.getCaptain()==null) continue;
                    if (m.getCaptain().getId().equals("nskr_anarchist")){
                        despawn = false;
                        break;
                    }
                    despawn = true;
                }
                if (despawn && !QuestHelper.getCompleted(KILLED_ELIZA_KEY)){
                    //eliza is gone
                    QuestHelper.setCompleted(true, KILLED_ELIZA_KEY);
                    //gone
                    Global.getSector().getImportantPeople().removePerson("nskr_anarchist");
                }

                Vector2f fp = fleet.getLocationInHyperspace();
                Vector2f pp = pf.getLocationInHyperspace();
                float dist = MathUtils.getDistance(pp, fp);
                if (despawn) {
                    if (dist > Global.getSettings().getMaxSensorRangeHyper()) {
                        //tracker for cleaning the list
                        removed.add(fleet);
                        fleet.despawn();
                    }
                }
                //logic

                FleetHelper.gotoAndInterceptPlayerAI(fleet, f, FleetHelper.interceptBehaviour.AROUND);
                continue;
            }
            //eliza fleet, after recovering UPC
            if (fleet.getMemoryWithoutUpdate().contains(ELIZA_INTERCEPT_FLEET_KEY)){
                boolean despawn = false;

                //for (FleetMemberAPI m :   fleet.getMembersWithFightersCopy()){
                //    log("ID "+ m.getId());
                //    log("TAGS "+ m.getVariant().getTags().toString());
                //}

                //destroyed
                if (fleet.getFleetPoints()<=0) {
                    despawn = true;
                }
                //eliza check
                for (FleetMemberAPI m : fleet.getFleetData().getMembersListWithFightersCopy()){
                    if (m.getCaptain()==null) continue;
                    if (m.getCaptain().getId().equals("nskr_anarchist")){
                        despawn = false;
                        break;
                    }
                    despawn = true;
                }
                if (despawn && !QuestHelper.getCompleted(KILLED_ELIZA_KEY)){
                    //eliza is gone
                    QuestHelper.setCompleted(true, KILLED_ELIZA_KEY);
                    //gone
                    Global.getSector().getImportantPeople().removePerson("nskr_anarchist");
                }


                Vector2f fp = fleet.getLocationInHyperspace();
                Vector2f pp = pf.getLocationInHyperspace();
                float dist = MathUtils.getDistance(pp, fp);
                //stop chasing eventually if not handed over
                if (QuestHelper.getCompleted(ELIZA_INTERCEPT_TALKED) && !QuestHelper.getCompleted(ELIZA_INTERCEPT_HANDED_OVER) && f.age>60f && !despawn){
                    if (dist > Global.getSettings().getMaxSensorRangeHyper()) {
                        //tracker for cleaning the list
                        removed.add(fleet);
                        fleet.despawn();
                        //add back
                        respawnEliza(QuestHelper.getElizaLoc());
                        log("despawn timeout, talked");
                        continue;
                    }
                }
                //defeated despawn
                if (despawn) {
                    if (dist > Global.getSettings().getMaxSensorRangeHyper()) {
                        //tracker for cleaning the list
                        removed.add(fleet);
                        fleet.despawn();
                        log("despawn defeated");
                    }
                }
                //stop here when defeated
                if (despawn) continue;

                //logic
                if (!QuestHelper.getCompleted(ELIZA_INTERCEPT_HANDED_OVER)) {
                    FleetHelper.gotoAndInterceptPlayerAI(fleet, f, FleetHelper.interceptBehaviour.DIRECT);
                } else {
                    //assignment logic
                    FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
                    if (curr == null) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.HOLD, fleet.getContainingLocation().createToken(fleet.getLocation()), Float.MAX_VALUE, "holding");
                        log("null assignment");
                    }
                    //go back to home, if handed over UPC
                    SectorEntityToken loc = QuestHelper.getElizaLoc();
                    if (fleet.getContainingLocation()!=loc.getContainingLocation() && fleet.getCurrentAssignment().getAssignment()!=FleetAssignment.GO_TO_LOCATION) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, loc, Float.MAX_VALUE, "returning to "+loc.getMarket().getName());
                        log("Qmanager DESPAWNING TO " + loc.getName() + " IN " + loc.getContainingLocation().getName());
                    }
                    if (fleet.getContainingLocation()==loc.getContainingLocation() && fleet.getCurrentAssignment().getAssignment()!=FleetAssignment.ORBIT_PASSIVE) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, loc, Float.MAX_VALUE, "returning to "+loc.getMarket().getName());
                    }
                    //remove fleet once back
                    if (MathUtils.getDistance(fleet, loc)<200f+loc.getRadius()){
                        //tracker for cleaning the list
                        removed.add(fleet);
                        fleet.despawn();
                        //add back
                        respawnEliza(loc);
                        log("despawn to base, handed over");
                    }
                }
                continue;
            }
            //revengeance fleets
            if (fleet.getMemoryWithoutUpdate().contains(REVENGEANCE_FLEET_KEY)){
                boolean despawn = false;

                //destroyed
                if (fleet.getFleetPoints()<=0) {
                    despawn = true;
                }

                Vector2f fp = fleet.getLocationInHyperspace();
                Vector2f pp = pf.getLocationInHyperspace();
                float dist = MathUtils.getDistance(pp, fp);
                if (despawn) {
                    if (dist > Global.getSettings().getMaxSensorRangeHyper()) {
                        //tracker for cleaning the list
                        removed.add(fleet);
                        fleet.despawn();
                    }
                }
                //logic

                //AI LOGIC
                FleetHelper.gotoAndInterceptPlayerAI(fleet, f, FleetHelper.interceptBehaviour.AROUND);
                continue;
            }
        }
    }

    private void job3TargetLogic(FleetInfo f, CampaignFleetAPI fleet) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;

        SectorEntityToken target = QuestHelper.getJob3Target();
        SectorEntityToken home = QuestHelper.getJob3Start();

        boolean despawn = false;
        //job COMPLETED
        if (fleet.getFleetPoints() < (f.strength * 0.20f)) {
            despawn = true;
            //stage check
            if (QuestHelper.getStage() <= 9) {
                QuestHelper.setStage(10);
                //completion text
                Global.getSector().getCampaignUI().addMessage("You have completed your objective. Report back to "+ QuestHelper.asteriaOrOutpost().getName()+" to finish the job.",
                        Global.getSettings().getColor("standardTextColor"),
                        "Report back to "+ QuestHelper.asteriaOrOutpost().getName(),
                        "",
                        Global.getSettings().getColor("yellowTextColor"),
                        Global.getSettings().getColor("yellowTextColor"));
                //no longer important
                if (fleet.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_MISSION_IMPORTANT)){
                    fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
                }
            }
        }
        //quest skipped
        if (QuestHelper.getStage() >= 10 && !despawn) {
            despawn = true;
        }
        //job FAILED
        if (f.age > JOB3_TIME_LIMIT && !despawn) {
            despawn = true;
            //stage check
            if (QuestHelper.getStage() <= 9) {
                QuestHelper.setFailed(true, JOB3_FAIL_KEY);
                QuestHelper.setStage(10);
                nskr_kestevenQuest.spawnEnvironmentalStorytelling();
                Global.getSector().getCampaignUI().addMessage("You have ran out of time, mission failed. Report back to "+ QuestHelper.asteriaOrOutpost().getName()+" to finish the job.",
                        Global.getSettings().getColor("standardTextColor"),
                        "mission failed",
                        "Report back to "+ QuestHelper.asteriaOrOutpost().getName(),
                        Global.getSettings().getColor("yellowTextColor"),
                        Global.getSettings().getColor("yellowTextColor"));
            }
        }

        Vector2f fp = fleet.getLocationInHyperspace();
        Vector2f pp = pf.getLocationInHyperspace();
        float dist = MathUtils.getDistance(pp, fp);
        if (despawn) {
            if (dist > Global.getSettings().getMaxSensorRangeHyper()) {
                //tracker for cleaning the list
                removed.add(fleet);
                fleet.despawn();
            }
        }
        //logic
        //TIMER
        QuestHelper.setMissionTimerJob3(QuestHelper.getMissionTimerJob3() - 0.1f);

        //stop here when defeated
        if (despawn) return;
        //assignment logic
        FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
        //used special maneuvers
        if (fleet.getMemoryWithoutUpdate().contains(MemFlags.FLEET_BUSY)) return;

        if (curr == null) {
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.HOLD, fleet.getContainingLocation().createToken(fleet.getLocation()), Float.MAX_VALUE, "holding");
            log("null assignment");
        }
        FleetAssignment assignment = fleet.getCurrentAssignment().getAssignment();
        //prepare
        if (f.age < 10f && fleet.getContainingLocation() == home.getContainingLocation() && assignment != FleetAssignment.ORBIT_PASSIVE) {
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, home, Float.MAX_VALUE, "preparing");
            log("Qmanager PREPARING" + home.getName() + " IN " + home.getContainingLocation().getName());
        }
        //go to
        if (f.age > 10f && fleet.getContainingLocation() != target.getContainingLocation() && assignment != FleetAssignment.GO_TO_LOCATION) {
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, target, Float.MAX_VALUE, "moving to location");
            log("Qmanager MOVING TO " + target.getName() + " IN " + target.getContainingLocation().getName());
        }
        //explore
        if (f.age < 70f && fleet.getContainingLocation() == target.getContainingLocation() && assignment != FleetAssignment.PATROL_SYSTEM) {
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, target, Float.MAX_VALUE, "on expedition");
            log("Qmanager EXPEDITION IN " + target.getContainingLocation().getName());
        }
        //return
        if (f.age > 70f && fleet.getContainingLocation() == target.getContainingLocation() && assignment != FleetAssignment.GO_TO_LOCATION) {
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, home, Float.MAX_VALUE, "returning to " + home.getName());
            log("Qmanager RETURNING TO " + home.getName() + " IN " + home.getContainingLocation().getName());
        }
        //despawn
        if (f.age > 70f && fleet.getContainingLocation() == home.getContainingLocation() && assignment != FleetAssignment.ORBIT_PASSIVE) {
            fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, home, Float.MAX_VALUE, "standing down");
            log("Qmanager DESPAWNING TO " + home.getName() + " IN " + home.getContainingLocation().getName());
        }
    }

    private void job4TargetLogic(FleetInfo f, CampaignFleetAPI fleet) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;

        boolean despawn = false;

        //despawn
        if (QuestHelper.getStage()>=17 || QuestHelper.getStage()>=14 && QuestHelper.getCompleted(DataSatelliteDialog.RECOVERED_4_KEY)) {
            despawn = true;
        }
        //destroyed
        if (fleet.getFleetPoints()<=0) {
            despawn = true;
        }

        //target destroyed check
        if (fleet.getMemoryWithoutUpdate().contains(JOB4_TARGET_KEY)){

            //log("J4T tags "+fleet.getFlagship().getVariant().getTags().toString());

            if (fleet.getFleetPoints() < (f.strength * 0.20f)) {
                despawn = true;
                QuestHelper.setCompleted(true, JOB4_DESTROYED_KEY);
                //no longer important
                if (fleet.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_MISSION_IMPORTANT)){
                    fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
                }
            }
        }

        //friendly found check
        if (fleet.getMemoryWithoutUpdate().contains(JOB4_FRIENDLY_KEY)){
            if (fleet.isVisibleToPlayerFleet() && !QuestHelper.getCompleted(JOB4_FOUND_FRIENDLY_KEY)){

                QuestHelper.setCompleted(true, JOB4_FOUND_FRIENDLY_KEY);
            }
        }
        //target found check
        if (fleet.getMemoryWithoutUpdate().contains(JOB4_TARGET_KEY)){
            if (fleet.isVisibleToPlayerFleet() && !QuestHelper.getCompleted(JOB4_FOUND_TARGET_KEY) && !QuestHelper.getCompleted(JOB4_DESTROYED_KEY)){

                QuestHelper.setCompleted(true, JOB4_FOUND_TARGET_KEY);
            }
        }

        Vector2f fp = fleet.getLocationInHyperspace();
        Vector2f pp = pf.getLocationInHyperspace();
        float dist = MathUtils.getDistance(pp, fp);
        if (despawn) {
            if (dist > Global.getSettings().getMaxSensorRangeHyper()) {
                //tracker for cleaning the list
                removed.add(fleet);
                fleet.despawn();
            }
        }
        //stop here when defeated
        if (despawn) return;
        //assignment logic
        FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
        if (curr == null) {
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.HOLD, fleet.getContainingLocation().createToken(fleet.getLocation()), Float.MAX_VALUE, "holding");
            log("null assignment");
        }
        //logic

        //aggro target fleet
        if (fleet.getMemoryWithoutUpdate().contains(JOB4_TARGET_KEY)) {
            //reset
            SectorEntityToken loc = QuestHelper.getJob4EnemyTarget();
            if (fleet.getContainingLocation() != pf.getContainingLocation() && fleet.getAI().getCurrentAssignmentType() == FleetAssignment.INTERCEPT) {
                fleet.clearAssignments();
                //remain aggressive
                if (!fleet.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON)) {
                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);
                }
                fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, loc, Float.MAX_VALUE, "unknown");
            }
        }
        //helped friendly fleet
        if (fleet.getMemoryWithoutUpdate().contains(JOB4_FRIENDLY_KEY)) {
            boolean helped = QuestHelper.getCompleted(JOB4_HELPED_KEY);
            SectorEntityToken target = QuestHelper.getJob4FriendlyTarget();
            //safety check
            if (QuestHelper.asteriaOrOutpost() != null) {
                SectorEntityToken home = QuestHelper.asteriaOrOutpost().getPrimaryEntity();
                //go back to asteria
                if (fleet.getContainingLocation() == target.getContainingLocation() && helped && fleet.getAI().getCurrentAssignmentType() == FleetAssignment.ORBIT_PASSIVE) {
                    //no longer important
                    if (fleet.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_MISSION_IMPORTANT)) {
                        fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
                    }
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, home, Float.MAX_VALUE, "travelling back to " + QuestHelper.asteriaOrOutpost().getName());
                }
            }
        }
    }

    private CampaignFleetAPI vengeanceEliza(boolean intercept){
        PersonAPI eliza = MiscHelper.getEliza();
        SectorEntityToken loc = QuestHelper.getElizaLoc();
        //-rep
        if (!intercept) eliza.getRelToPlayer().adjustRelationship(-0.75f, RepLevel.VENGEFUL);
        //spawn fleet and add to list
        CampaignFleetAPI fleet;
        if (!intercept){
            fleet = QuestFleets.spawnElizaFleet(loc, eliza, ElizaDialog.getRandom(), true, false);
        } else {
            fleet = QuestFleets.spawnElizaFleet(loc, eliza, ElizaDialog.getRandom(), false, true);
        }
        //remove from market
        loc.getMarket().getCommDirectory().removePerson(eliza);
        loc.getMarket().removePerson(eliza);

        return fleet;
    }
    private CampaignFleetAPI vengeanceJack(){
        PersonAPI jack = MiscHelper.getJack();
        SectorEntityToken loc = QuestHelper.asteriaOrOutpost().getPrimaryEntity();
        //spawn fleet and add to list
        CampaignFleetAPI fleet = QuestFleets.spawnJackFleet(loc, jack, nskr_kestevenQuest.getRandom());
        //remove from market
        loc.getMarket().getCommDirectory().removePerson(jack);
        loc.getMarket().removePerson(jack);
        //gone
        Global.getSector().getImportantPeople().removePerson("nskr_opguy");
        QuestHelper.setCompleted(true, JACK_GONE_KEY);
        return fleet;
    }

    @Override
    public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {
        CampaignFleetAPI loser = plugin.getLoser();
        if (loser == null) return;
        //job 1 completion check and has fought enigma dialog check
        if (stage<=1 && loser.getFaction().getId().equals("enigma")) {
            List<FleetEncounterContextPlugin.FleetMemberData> casualties = plugin.getLoserData().getOwnCasualties();
            float kills = 0f;
            for (FleetEncounterContextPlugin.FleetMemberData memberData : casualties) {
                FleetEncounterContextPlugin.Status status = memberData.getStatus();
                if (status == FleetEncounterContextPlugin.Status.NORMAL) continue;
                float contrib = plugin.computePlayerContribFraction();
                kills += 1f*contrib;
            }
            if(kills>=1f){
                //has fought enigma dialog check key
                if (stage==0 && !QuestHelper.getCompleted(HAS_FOUGHT_ENIGMA_KEY)) {
                    QuestHelper.setCompleted(true, HAS_FOUGHT_ENIGMA_KEY);
                }
                //job1 sensor check
                if (stage==1) {
                    QuestHelper.setCompleted(true, JOB1_SENSORS_KEY);
                    //completion text
                    Global.getSector().getCampaignUI().addMessage("You managed to gather sufficient data in battle for the task. Deliver it back to " + QuestHelper.asteriaOrOutpost().getName() + ".",
                            Global.getSettings().getColor("standardTextColor"),
                            "Deliver it back to " + QuestHelper.asteriaOrOutpost().getName(),
                            "",
                            Global.getSettings().getColor("yellowTextColor"),
                            Global.getSettings().getColor("yellowTextColor"));
                }
            }
        }
        //job 3 fail check
        if (stage <=9) {
            if (loser.getMemoryWithoutUpdate().contains(JOB3_TARGET_KEY) && loser.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON)) {
                List<FleetEncounterContextPlugin.FleetMemberData> casualties = plugin.getLoserData().getOwnCasualties();

                for (FleetEncounterContextPlugin.FleetMemberData memberData : casualties) {
                    FleetEncounterContextPlugin.Status status = memberData.getStatus();
                    if (status == FleetEncounterContextPlugin.Status.NORMAL) continue;
                    float contrib = plugin.computePlayerContribFraction();
                    if (QuestHelper.getStage() <= 9 && contrib>0f) {
                        //FAIL
                        QuestHelper.setFailed(true, JOB3_FAIL_KEY);
                        QuestHelper.setStage(10);
                        Global.getSector().getCampaignUI().addMessage("You failed to neutralize the fleet stealthily. Report back to "+ QuestHelper.asteriaOrOutpost().getName()+" to finish the job.",
                                Global.getSettings().getColor("standardTextColor"),
                                "failed to neutralize the fleet stealthily",
                                "Report back to "+ QuestHelper.asteriaOrOutpost().getName(),
                                Global.getSettings().getColor("yellowTextColor"),
                                Global.getSettings().getColor("yellowTextColor"));
                        break;
                    }
                }
            }
        }
        //job 4 failure
        if (loser.getMemoryWithoutUpdate().contains(JOB4_FRIENDLY_KEY)) {
            List<FleetEncounterContextPlugin.FleetMemberData> casualties = plugin.getLoserData().getOwnCasualties();

            for (FleetEncounterContextPlugin.FleetMemberData memberData : casualties) {
                FleetEncounterContextPlugin.Status status = memberData.getStatus();
                if (status == FleetEncounterContextPlugin.Status.NORMAL) continue;
                float contrib = plugin.computePlayerContribFraction();
                if (contrib>0f) {
                    //FAIL
                    QuestHelper.setStage(14);
                    if (!QuestHelper.getFailed(JOB4_FAILED_KEY)) {
                        Global.getSector().getCampaignUI().addMessage("You attacked the Special Operations fleet. Mission failed, better not to talk to anyone about this.",
                                Global.getSettings().getColor("standardTextColor"),
                                "Mission failed",
                                "",
                                Global.getSettings().getColor("yellowTextColor"),
                                Global.getSettings().getColor("yellowTextColor"));

                        QuestHelper.setFailed(true, JOB4_FAILED_KEY);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void reportPlayerReputationChange(String faction, float delta) {

        //relations capper for Eliza ending
        if (QuestHelper.getCompleted(EndingElizaDialog.DIALOG_FINISHED_KEY)){
            if (faction.equals(Ids.KESTEVEN_FACTION_ID)){
                //add Nex rel cap
                float max;
                if (ModPlugin.IS_NEXERELIN) {
                    float maxRel = 1f - DiplomacyManager.getManager().getMaxRelationship(faction, Factions.PLAYER);
                    max = Math.max(ELIZA_MAX_RELATION_KESTEVEN - maxRel, -1f);
                } else {
                    max = ELIZA_MAX_RELATION_KESTEVEN;
                }
                if (Global.getSector().getPlayerFaction().getRelationship(faction) > max){
                    Global.getSector().getPlayerFaction().setRelationship(faction, max);
                }
            }
            if (faction.equals(Factions.HEGEMONY) || faction.equals("ironshell")){
                //add Nex rel cap
                float max;
                if (ModPlugin.IS_NEXERELIN) {
                    float maxRel = 1f - DiplomacyManager.getManager().getMaxRelationship(faction, Factions.PLAYER);
                    max = Math.max(ELIZA_MAX_RELATION_HEGEMONY - maxRel, -1f);
                } else {
                    max = ELIZA_MAX_RELATION_HEGEMONY;
                }
                if (Global.getSector().getPlayerFaction().getRelationship(faction) > max){
                    Global.getSector().getPlayerFaction().setRelationship(faction, max);
                }
            }
        }

    }

    public static Random getRandom() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

            data.put(PERSISTENT_RANDOM_KEY, new Random(MiscHelper.getSeedParsed()));
        }
        return (Random) data.get(PERSISTENT_RANDOM_KEY);
    }

}
