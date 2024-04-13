package scripts.kissa.LOST_SECTOR.campaign.quests.util;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.world.MoteParticleScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import exerelin.campaign.DiplomacyManager;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.procgen.nskr_dormantSpawner;
import scripts.kissa.LOST_SECTOR.campaign.intel.*;
import scripts.kissa.LOST_SECTOR.campaign.nskr_exileManager;
import scripts.kissa.LOST_SECTOR.campaign.procgen.nskr_environmentalStorytelling;
import scripts.kissa.LOST_SECTOR.campaign.quests.*;
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.*;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.ids;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;
import scripts.kissa.LOST_SECTOR.world.systems.cache.nskr_cache;

import java.util.*;

public class questStageManager extends BaseCampaignEventListener implements EveryFrameScript  {
    //
    //manages quest stage changes and mission fleets
    //1000 line efs? no im fine, this works perfectly.
    public static final String KESTEVEN_QUEST_KEY = "KestevenQuest";
    public static final String FLEET_ARRAY_KEY = "$kQuestMissionFleets";
    public static final String QUEST_END_KEY = "KestevenQuestEnd";
    public static final String PERSISTENT_RANDOM_KEY = "KestevenQuestRandomKey";
    public static final String HAS_FOUGHT_ENIGMA_KEY = "KestevenQuest1HasFoughtEnigma";
    public static final String JOB1_DELIVERED_KEY = "KestevenQuest1Deliver";
    public static final String JOB1_DELIVERED_DATA_KEY = "KestevenQuest1DeliverData";
    public static final String JOB1_SENSORED_KEY = "KestevenQuest1Sensor";
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

    nskr_saved<Boolean> intelStage1;
    nskr_saved<Boolean> intelStage2;
    nskr_saved<Boolean> intelStage3;
    nskr_saved<Boolean> intelStage4;
    nskr_saved<Boolean> intelStage5;
    nskr_saved<Boolean> barStage5;
    nskr_saved<Boolean> jobFleetSpawned3;
    nskr_saved<Boolean> jobFleetsSpawned4;
    nskr_saved<Float> counterJob;
    nskr_saved<Float> counter;
    nskr_saved<Float> fleetCounter;
    nskr_saved<Float> cacheTimer;
    nskr_saved<Boolean> collected;
    nskr_saved<Boolean> cacheGuardian;
    nskr_saved<Boolean> cacheIntelAdd;
    nskr_saved<Boolean> commission;
    nskr_saved<Boolean> doubted;
    nskr_saved<Boolean> cacheLoc;
    nskr_saved<Boolean> vengeanced;
    nskr_saved<Boolean> elizaBetray;
    nskr_saved<Boolean> elizad;
    private final List<CampaignFleetAPI> removed = new ArrayList<>();

    public questStageManager() {
        super(false);
        //logic timer
        this.counter = new nskr_saved<>("questCounter", 0.0f);
        this.fleetCounter = new nskr_saved<>("questFleetCounter", 0.0f);
        //one time intel check
        this.intelStage1 = new nskr_saved<>("questIntelStage1", false);
        this.intelStage2 = new nskr_saved<>("questIntelStage2", false);
        this.intelStage3 = new nskr_saved<>("questIntelStage3", false);
        this.intelStage4 = new nskr_saved<>("questIntelStage4", false);
        this.intelStage5 = new nskr_saved<>("questIntelStage5", false);
        this.barStage5 = new nskr_saved<>("questBarStage5", false);
        this.cacheIntelAdd = new nskr_saved<>("cacheIntelAddStage5", false);
        //fixes commission rep changes
        this.commission = new nskr_saved<>("commissionStage5", false);
        //one time fleet spawn
        this.jobFleetSpawned3 = new nskr_saved<>("questJobFleetSpawned3", false);
        this.jobFleetsSpawned4 = new nskr_saved<>("questJobFleetsSpawned4", false);
        this.collected = new nskr_saved<>("questJobCollected", false);
        this.doubted = new nskr_saved<>("questJobCacheDoubt", false);
        this.cacheLoc = new nskr_saved<>("questJobCacheLoc", false);
        this.cacheGuardian = new nskr_saved<>("questJobCacheGuardian", false);
        this.vengeanced = new nskr_saved<>("questJobVengeanced", false);
        this.elizaBetray = new nskr_saved<>("questJobelizaBetray", false);
        this.elizad = new nskr_saved<>("questJobelizadIntercept", false);
        //the one-month wait for job
        this.counterJob = new nskr_saved<>("questCounterJob", 0.0f);
        this.cacheTimer = new nskr_saved<>("questCacheTimer", 0.0f);

    }

    static void log(final String message) {
        Global.getLogger(questStageManager.class).info(message);
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

        stage = questUtil.getStage();

        //both mission markets are fucked failure
        if (!nskr_exileManager.canExile() && !util.AsteriaExists() && !questUtil.getEndMissions()){
            questUtil.setStage(99);
            questUtil.setEndMissions(true);
            log("ERROR sector is fucked, ending missions");
        }
        //kill Special Operations fleet failure
        if (stage == 14 && questUtil.getCompleted(JOB4_FAILED_KEY) && !questUtil.getEndMissions()) {
            questUtil.setStage(99);
            questUtil.setEndMissions(true);

        }
        //kill Eliza after handing over failure
        if (stage == 19 && questUtil.getCompleted(ELIZA_INTERCEPT_HANDED_OVER) && questUtil.getCompleted(KILLED_ELIZA_KEY) && !questUtil.getEndMissions()) {
            questUtil.setCompleted(true, JOB5_FAILED_KEY);

            questUtil.setStage(99);
            questUtil.setEndMissions(true);

        }
        //////////////////
        //done while paused
        //////////////////
        //finish job1
        if (stage ==1) {
            boolean delivered = questUtil.getCompleted(JOB1_DELIVERED_KEY);
            boolean deliveredData = questUtil.getCompleted(JOB1_DELIVERED_DATA_KEY);
            //finished tasks
            if (delivered && deliveredData) {
                questUtil.setStage(2);
            }
        }
        //start job5
        if (stage ==15) {
            //no longer used, now manually spawn by barEventFixer
            //BAR EVENT
            //if (!barStage5.val) {
                //PortsideBarData.getInstance().addEvent(new nskr_kQuest5Bar());
                //log("Qmanager added bar event for job5");
                //barStage5.val = true;
            //}
            //cache found check
            if (!questUtil.getCompleted(FOUND_CACHE_KEY) && Global.getSector().getStarSystem("Unknown Site").isEnteredByPlayer()) {
                questUtil.setCompleted(true, FOUND_CACHE_KEY);
            }
        }
        //eliza loc changer
        if (stage>=16 && questUtil.getCompleted(JOB5_FOUND_ELIZA_KEY) && !questUtil.getCompleted(KILLED_ELIZA_KEY)) {
            if(questUtil.getElizaLoc().getMarket().isPlanetConditionMarketOnly()){
                log("Qmanager eliza loc deciv, changing");
                PersonAPI eliza = util.getEliza();
                String oldLoc = questUtil.getElizaLoc().getMarket().getPrimaryEntity().getName();
                //new loc
                questUtil.setElizaLoc();
                //update
                questUtil.getElizaLoc().getMarket().getCommDirectory().addPerson(eliza,1);
                questUtil.getElizaLoc().getMarket().addPerson(eliza);
                //fix contact
                if(ContactIntel.getContactIntel(eliza)!=null && ContactIntel.getContactIntel(eliza).getState()==ContactIntel.ContactState.LOST_CONTACT_DECIV){
                    ContactIntel.getContactIntel(eliza).setState(ContactIntel.ContactState.PRIORITY);
                }
                //text
                Global.getSector().getCampaignUI().addMessage("With the conditions deteriorating on "+oldLoc+", Eliza has moved her operations to "+ questUtil.getElizaLoc().getMarket().getName()+".",
                        Global.getSettings().getColor("standardTextColor"),
                        "Eliza",
                        "",
                        Global.getSector().getFaction(Factions.PIRATES).getColor(),
                        Global.getSettings().getColor("yellowTextColor"));
            }
        }
        //paid for loc changer
        if (stage==16 && questUtil.getCompleted(nskr_kQuest5ElizaBarMain.PAID_FOR_INFO) && !questUtil.getCompleted(questStageManager.JOB5_FOUND_ELIZA_KEY) && nskr_kQuest5ElizaBarMain.getDialogStage(nskr_kQuest5ElizaBarMain.INTRO_DIALOG_KEY)==2){
            if(nskr_kQuest5ElizaBarMain.getPaidForInfoTarget().getMarket().isPlanetConditionMarketOnly()){
                //remove important
                if (nskr_kQuest5ElizaBarMain.getPaidForInfoTarget().getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_MISSION_IMPORTANT)){
                    nskr_kQuest5ElizaBarMain.getPaidForInfoTarget().getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
                }
                log("Qmanager paid for loc deciv, changing");
                String oldLoc = nskr_kQuest5ElizaBarMain.getPaidForInfoTarget().getMarket().getPrimaryEntity().getName();
                //new loc
                nskr_kQuest5ElizaBarMain.setPaidForInfoTarget();
                //make important, again
                nskr_kQuest5ElizaBarMain.getPaidForInfoTarget().getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MISSION_IMPORTANT,true);
                //text
                Global.getSector().getCampaignUI().addMessage("With the conditions deteriorating on "+oldLoc+", the contact has moved their operations to "+ nskr_kQuest5ElizaBarMain.getPaidForInfoTarget().getName()+".",
                        Global.getSettings().getColor("standardTextColor"),
                        "the contact",
                        "",
                        Global.getSector().getFaction(Factions.PIRATES).getColor(),
                        Global.getSettings().getColor("yellowTextColor"));
            }
        }
        if (stage==16) {
            //cache found, before mission
            if (questUtil.getCompleted(FOUND_CACHE_KEY)) {
                questUtil.setStage(17);
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
        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
        //log("FleetInfo size "+fleets.size());

        //log("HARD "+nskr_modPlugin.getStarfarerMode()+" ENIGMA "+nskr_modPlugin.getRandomEnigmaFleetSizeMult()+" SCRIPTED "+nskr_modPlugin.getScriptedFleetSizeMult());

        //DEBUG CODE
        //TODO
        // undo
        //questUtil.setCompleted(true, nskr_artifactDialog.RECOVERED_4_KEY);
        //questUtil.setCompleted(true, nskr_artifactDialog.RECOVERED_3_KEY);
        //nskr_artifactDialog.setRecoveredSatelliteCount(2);
        //questUtil.setCompleted(true, JOB5_FOUND_FROST_KEY);
        //questUtil.setCompleted(true, nskr_glacierCommsDialog.RECOVERED_KEY);
        //questUtil.setCompleted(true, nskr_kestevenQuest.JOB5_ALICE_TIP_KEY);
        //questUtil.setCompleted(true, nskr_kestevenQuest.JOB5_ALICE_TIP_KEY2);
        //questUtil.setCompleted(true, nskr_kestevenQuest.JOB5_JACK_TIP_KEY);
        //questUtil.setCompleted(true, JOB5_FOUND_ELIZA_KEY);
        //questUtil.setCompleted(true, nskr_elizaDialog.DIALOG_FINISHED_KEY);
        //questUtil.setCompleted(true, nskr_elizaDialog.ELIZA_HELP_KEY);

        //if(questUtil.getElizaLoc()==null) {
        //    questUtil.setElizaLoc();
        //    PersonAPI eliza = nskr_gen.genEliza();
        //    questUtil.getElizaLoc().getMarket().getCommDirectory().addPerson(eliza, 1);
        //    questUtil.getElizaLoc().getMarket().addPerson(eliza);
        //    log("Eliza loc " + questUtil.getElizaLoc().getMarket().getName());
        //}

        //questUtil.setCompleted(true, FOUND_CACHE_KEY);
        //if (questUtil.getDisksRecovered()==0) {
        //    questUtil.setDisksRecovered(5);
        //}

       //int targetStage = 15;
       //int s = stage;
       //frameWait2++;
       //if (frameWait2>50) {
       //    if (stage < targetStage) {
       //        frameWait2=0;
       //        s++;
       //        questUtil.setStage(s);
       //    }
       //}
       //if (Global.getSector().getFaction(Factions.PLAYER).getRelationship("kesteven")<0.9f){
       //    Global.getSector().getFaction(Factions.PLAYER).setRelationship("kesteven", 1f);
       //}

        //start job 1
        if (stage ==1) {
            //Adds our intel
            if (!intelStage1.val) {
                nskr_kQuest1Intel intel1 = new nskr_kQuest1Intel();
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
                questUtil.setCompleted(true, JOB4_WAIT_KEY);
                counterJob.val = 0f;
            }
        }
        //start job 3
        if (stage ==8) {
            //Adds our intel
            if (!intelStage3.val) {
                nskr_kQuest3Intel intel3 = new nskr_kQuest3Intel();
                Global.getSector().getIntelManager().addIntel(intel3, false);
                intelStage3.val = true;
                log("Qmanager added INTEL for " + "Hostile Takeover");
                //BAR EVENT
                PortsideBarData.getInstance().addEvent(new nskr_kQuest3Bar());
                //DORMANT fleet at target
                util.addDormant(questUtil.getJob3Target(), "enigma", 45f, 50f, 0f, 1f, 1f, 1f, 1, 1);
                log("Qmanager added dormant to target " + questUtil.getJob3Target().getName() +" "+ questUtil.getJob3Target().getStarSystem().getName());
            }
            //logic
            if (!jobFleetSpawned3.val) {
                CampaignFleetAPI fleet = questFleets.spawnJob3TargetFleet();
                fleets.add(new fleetInfo(fleet, questUtil.getJob3Target(), questUtil.getJob3Start()));
                questUtil.spawnArtifact(questUtil.getJob3Target(),3);
                log("Qmanager spawn job3 target");
                jobFleetSpawned3.val = true;
            }
        }
        //start job 4
        if (stage ==12) {
            //Adds our intel
            if (!intelStage4.val) {
                nskr_kQuest4Intel intel4 = new nskr_kQuest4Intel();
                Global.getSector().getIntelManager().addIntel(intel4, false);
                intelStage4.val = true;
                log("Qmanager added INTEL for " + "Operation Lifesaver");
            }
            //logic
            if (!jobFleetsSpawned4.val) {

                //this sets job4TargetLoc
                questFleets.spawnJob4Target();

                questUtil.spawnArtifact(questUtil.getJob4EnemyTarget(),4);
                CampaignFleetAPI fleet = questFleets.spawnJob4Friendly();
                fleets.add(new fleetInfo(fleet, null, questUtil.getJob4FriendlyTarget()));
                for (int x = 0; x<SPLINTER_COUNT;x++) {
                    new Pair<>(questFleets.spawnJob4Splinters(), 0f);
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
        if (nskr_job4FleetDialog.getDialogStage(nskr_job4FleetDialog.PERSISTENT_KEY)>=1 && !questUtil.getCompleted(JOB4_FOUND_TARGET_KEY) && !questUtil.getCompleted(JOB4_DESTROYED_KEY)){

            questUtil.setCompleted(true, JOB4_TARGET_HINT_KEY);
        }
        //job 4
        //found friendly
        if (nskr_job4FleetDialog.getDialogStage(nskr_job4FleetDialog.PERSISTENT_KEY)>=1 && !questUtil.getCompleted(JOB4_FOUND_FRIENDLY_KEY)){

            questUtil.setCompleted(true, JOB4_FOUND_FRIENDLY_KEY);
        }

        //job 4 completion
        if (stage ==12 && questUtil.getCompleted(JOB4_FOUND_FRIENDLY_KEY) && questUtil.getCompleted(JOB4_DESTROYED_KEY)){
            //completion text
            Global.getSector().getCampaignUI().addMessage("With the threat eliminated and the Operations fleet located, you can report back to "+ questUtil.asteriaOrOutpost().getName()+" to finish the job.",
                    Global.getSettings().getColor("standardTextColor"),
                    "report back to "+ questUtil.asteriaOrOutpost().getName(),
                    "",
                    Global.getSettings().getColor("yellowTextColor"),
                    Global.getSettings().getColor("yellowTextColor"));

            questUtil.setStage(13);
        }
        //start job 5
        if (stage==16 || stage==17) {
            //Adds our intel
            if (!intelStage5.val) {
                nskr_kQuest5Intel intel5 = new nskr_kQuest5Intel();
                Global.getSector().getIntelManager().addIntel(intel5, false);
                intelStage5.val = true;
                log("Qmanager added INTEL for " + "The Delve");
                //BAR EVENTS
                if (stage==16) PortsideBarData.getInstance().addEvent(new nskr_kQuest5ElizaBarMain());
                if (stage==16) PortsideBarData.getInstance().addEvent(new nskr_kQuest5ElizaBarSecond());
                if (stage==16) PortsideBarData.getInstance().addEvent(new nskr_kQuest5ElizaBarFinal());
            }
        }
        if (stage==16) {
           //job 5 logic
           //found frost check
           if (!questUtil.getCompleted(JOB5_FOUND_FROST_KEY) && questUtil.getCompleted(nskr_kestevenQuest.JOB5_ALICE_TIP_KEY2)){
               if (pf.getContainingLocation()!=null && pf.getContainingLocation()== util.getFrost()){
                   //found
                   questUtil.setCompleted(true, JOB5_FOUND_FROST_KEY);
               }
           }
           //all disks found check
           if (questUtil.getDisksRecovered()>=5){
                questUtil.setCompleted(true, ALL_DISKS_RECOVERED_KEY);
           }
            //cache found check
            if (!questUtil.getCompleted(FOUND_CACHE_KEY) && Global.getSector().getStarSystem("Unknown Site").isEnteredByPlayer()) {
                questUtil.setCompleted(true, FOUND_CACHE_KEY);
                questUtil.setStage(17);
            }
        }
        //cache found, no mission
        if (questUtil.getEndMissions() && Global.getSector().getStarSystem("Unknown Site").isEnteredByPlayer()){
            questUtil.setCompleted(true, FOUND_CACHE_KEY);
        }
        //add intel for cache
        if (questUtil.getCompleted(FOUND_CACHE_KEY)){
            if (!cacheIntelAdd.val) {
                nskr_cacheIntel cacheIntel = new nskr_cacheIntel(Global.getSector().getStarSystem("Unknown Site"));
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
                        questUtil.setCacheFleetLoc();
                        cacheLoc.val = true;
                    }
                    CampaignUIAPI ui = Global.getSector().getCampaignUI();
                    //stage check
                    if (stage>=16 && !questUtil.getEndMissions() && !doubted.val) {
                        //UI check
                        if (!ui.isShowingDialog() && !ui.isShowingMenu()) {
                            Global.getSector().getCampaignUI().showInteractionDialog(new nskr_cacheDoubtDialog(), null);
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
                    Global.getSector().getStarSystem(ids.CACHE_SYSTEM_NAME).getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "nskr_cache_theme");
                    //add to mission fleets so we can track it
                    CampaignFleetAPI fleet = nskr_cache.spawnGuardianFleet(pf, questUtil.getCacheFleetLoc());
                    Global.getSoundPlayer().playSound("ui_discovered_entity", 1f, 1f, fleet.getLocation(), new Vector2f());

                    fleets.add(new fleetInfo(fleet, null, questUtil.getCacheFleetLoc()));
                    cacheGuardian.val = true;
                }
            }
            //cache mote particles
            if (Math.random()<0.004f)MoteParticleScript.spawnMote(pf);
        }
        //job5 finish commission unfucker Eliza
        if (questUtil.getCompleted(nskr_EndingElizaDialog.COMMISSION_UNFUCK_KEY) && !commission.val){
            //have to wait a few frames for the vanilla commission to end
            frameWait++;
            if (frameWait==30) {
                float repPirates = questUtil.getFloat(nskr_EndingElizaDialog.REP_PIRATES_KEY);
                float repKesteven = questUtil.getFloat(nskr_EndingElizaDialog.REP_KESTEVEN_KEY);
                float repHege = questUtil.getFloat(nskr_EndingElizaDialog.REP_HEGE_KEY);
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
                if(nskr_modPlugin.IS_IRONSHELL){
                    if (Global.getSector().getFaction(Factions.PLAYER).getRelationship("ironshell") >= repHege) {
                        Global.getSector().getFaction(Factions.PLAYER).setRelationship("ironshell", repHege);
                    }
                }
                commission.val = true;
                //log("COMMISSION "+repPirates+" "+repKesteven+" "+repHege);
            }
        }
        //player betrays Eliza after completing the mission for her
        if (questUtil.getElizaLoc()!=null) {
            if (!questUtil.getElizaLoc().getMarket().isPlanetConditionMarketOnly()) {
                if (questUtil.getElizaLoc().getMarket().getFaction().getId().equals(Factions.PLAYER) &&
                        questUtil.getCompleted(nskr_elizaDialog.ELIZA_HELP_KEY) && !questUtil.getCompleted(ELIZA_BETRAY_KEY) && questUtil.getCompleted(nskr_EndingElizaDialog.DIALOG_FINISHED_KEY)) {
                    questUtil.setCompleted(true, ELIZA_BETRAY_KEY);
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

                        CampaignFleetAPI fleet = questFleets.spawnCollectorFleet();
                        fleets.add(new fleetInfo(fleet, null, fleet.getContainingLocation().createToken(fleet.getLocation())));

                        collected.val = true;
                        log("Qmanager SPAWNING");
                    }
                }
            }
            if (!elizad.val){
                //eliza intercept player for UPC
                if (stage==19 && questUtil.getCompleted(nskr_elizaDialog.ELIZA_HELP_KEY)){
                    CampaignFleetAPI fleet = vengeanceEliza(true);
                    elizad.val = true;
                    log("Intercepted Eliza");
                }
            }
            //vengeance Eliza betray by player, after questline
            if (questUtil.getCompleted(ELIZA_BETRAY_KEY) && !elizaBetray.val){
                CampaignFleetAPI fleet = vengeanceEliza(false);
                elizaBetray.val = true;
            }
            //revengeace fleet spawner
            if (getRandom().nextFloat()<REVENGEANCE_CHANCE && stage == 20 && !vengeanced.val) {
                //jack
                if (questUtil.getCompleted(nskr_EndingElizaDialog.DIALOG_FINISHED_KEY) || questUtil.getCompleted(nskr_altEndingDialogLuddic.DIALOG_FINISHED_KEY)){
                    CampaignFleetAPI fleet = vengeanceJack();
                    vengeanced.val = true;
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
            fleetUtil.cleanUp(removed, fleets);
            removed.clear();
            //save to mem
            fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

            //
            fleetCounter.val = 0f;
            //log("MANAGING " + fleets.size() + " fleets");
            //for (fleetInfo f : fleets){
            //    log(f.fleet.getName());
            //}
        }
    }

    private void respawnEliza(SectorEntityToken loc) {
        PersonAPI eliza = util.getEliza();
        //add eliza to market
        loc.getMarket().getCommDirectory().addPerson(eliza,1);
        loc.getMarket().addPerson(eliza);

        //check for ending
        questUtil.setCompleted(true, ELIZA_RETURNED_KEY);
    }

    public static void spawnJob4Wrecks(Random random) {
        SectorEntityToken loc = questUtil.getJob4EnemyTarget();
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
        float dist = mathUtil.getSeededRandomNumberInRange(150f, 250f, random);
        float days = mathUtil.getSeededRandomNumberInRange(30f, 60f, random);
        float angle = mathUtil.getSeededRandomNumberInRange(0f, 360f, random);
        debrisField.setCircularOrbit(loc, angle, dist, days);
        debrisField.setId("nskr_debrisField_"+random.nextLong());

        //ships
        float recoveryChance = 0.25f;
        int count = 3;
        for (int y=0;y<count;y++) {
            SectorEntityToken derelict = nskr_environmentalStorytelling.addDerelict(
                    system, pickRandomVariant(random), nskr_environmentalStorytelling.randomCondition(), Math.random() < recoveryChance, null
            );
            derelict.setCircularOrbit(loc,
                    angle+mathUtil.getSeededRandomNumberInRange(-45f, 45f, random),
                    dist+mathUtil.getSeededRandomNumberInRange(-125f, 125f, random),
                    days+mathUtil.getSeededRandomNumberInRange(-3f, 3f, random));
            if (y==0) {
                //mark one of the wrecks
                derelict.setId(JOB4_HINT_WRECK_ID_KEY +random.nextLong());
            }
        }

    }
    private static String pickRandomVariant(Random random) {
        FactionAPI faction = Global.getSector().getFaction(ids.KESTEVEN_FACTION_ID);
        ArrayList<String> variants = new ArrayList<>();
        String variant = "";
        while (variants.isEmpty()) {
            variants = new ArrayList<>(faction.getVariantsForRole(nskr_environmentalStorytelling.randomRole(random, false)));
            variant = variants.get(mathUtil.getSeededRandomNumberInRange(0, variants.size() - 1, random));
            //only pick kesteven ships
            if (!Global.getSettings().getVariant(variant).getHullSpec().hasTag("kesteven")){
                variants.clear();
            }
        }
        return variant;
    }

    private void spawnPing(CampaignFleetAPI pf) {
        SectorEntityToken loc = questUtil.getCacheFleetLoc();
        if (MathUtils.getDistance(pf.getLocation(), loc.getLocation()) > 1000f) {
            float angle = VectorUtils.getAngle(pf.getLocation(), loc.getLocation());
            Vector2f newLoc = MathUtils.getPointOnCircumference(pf.getLocation(), 1000f, angle + MathUtils.getRandomNumberInRange(-20f, 20f));
            loc = pf.getContainingLocation().createToken(newLoc);
        }
        Global.getSector().addPing(loc, Pings.SENSOR_BURST);
    }

    private void runFleetLogic(List<fleetInfo> fleets){
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;

        //mission related fleets manager
        for (fleetInfo f : fleets) {
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
            if (fleet.getMemoryWithoutUpdate().contains(nskr_dormantSpawner.DORMANT_KEY)){
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
                    fleetUtil.gotoAndInterceptPlayerAI(fleet, f, fleetUtil.interceptBehaviour.AROUND);
                }
                //leave
                if (paid) {
                    if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.GO_TO_LOCATION_AND_DESPAWN) {
                        fleet.clearAssignments();
                        fleet.getMemoryWithoutUpdate().clear();
                        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);

                        SectorEntityToken loc = questUtil.getRandomFactionMarket(new Random(), Factions.TRITACHYON);
                        if (loc != null && loc.getMarket() != null) {
                            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, loc, Float.MAX_VALUE, "returning to " + loc.getName());
                            log("Qmanager " + fleet.getName() + " RETURNING ");
                        }
                    }
                }
                continue;
            }
            //cache guardian fleet
            if (fleet.getMemoryWithoutUpdate().contains(nskr_cache.CACHE_FLEET_KEY)){
                boolean despawn = false;

                //no prots
                if (!nskr_cache.hasPrototypes(fleet)) {
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
            if (fleet.getMemoryWithoutUpdate().contains(questFleets.ELIZA_RAIDED_FLEET_KEY)){
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
                if (despawn && !questUtil.getCompleted(KILLED_ELIZA_KEY)){
                    //eliza is gone
                    questUtil.setCompleted(true, KILLED_ELIZA_KEY);
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

                fleetUtil.gotoAndInterceptPlayerAI(fleet, f, fleetUtil.interceptBehaviour.AROUND);
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
                if (despawn && !questUtil.getCompleted(KILLED_ELIZA_KEY)){
                    //eliza is gone
                    questUtil.setCompleted(true, KILLED_ELIZA_KEY);
                    //gone
                    Global.getSector().getImportantPeople().removePerson("nskr_anarchist");
                }


                Vector2f fp = fleet.getLocationInHyperspace();
                Vector2f pp = pf.getLocationInHyperspace();
                float dist = MathUtils.getDistance(pp, fp);
                //stop chasing eventually if not handed over
                if (questUtil.getCompleted(ELIZA_INTERCEPT_TALKED) && !questUtil.getCompleted(ELIZA_INTERCEPT_HANDED_OVER) && f.age>60f && !despawn){
                    if (dist > Global.getSettings().getMaxSensorRangeHyper()) {
                        //tracker for cleaning the list
                        removed.add(fleet);
                        fleet.despawn();
                        //add back
                        respawnEliza(questUtil.getElizaLoc());
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
                if (!questUtil.getCompleted(ELIZA_INTERCEPT_HANDED_OVER)) {
                    fleetUtil.gotoAndInterceptPlayerAI(fleet, f, fleetUtil.interceptBehaviour.DIRECT);
                } else {
                    //assignment logic
                    FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
                    if (curr == null) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.HOLD, fleet.getContainingLocation().createToken(fleet.getLocation()), Float.MAX_VALUE, "holding");
                        log("null assignment");
                    }
                    //go back to home, if handed over UPC
                    SectorEntityToken loc = questUtil.getElizaLoc();
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
                fleetUtil.gotoAndInterceptPlayerAI(fleet, f, fleetUtil.interceptBehaviour.AROUND);
                continue;
            }
        }
    }

    private void job3TargetLogic(fleetInfo f, CampaignFleetAPI fleet) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;

        SectorEntityToken target = questUtil.getJob3Target();
        SectorEntityToken home = questUtil.getJob3Start();

        boolean despawn = false;
        //job COMPLETED
        if (fleet.getFleetPoints() < (f.strength * 0.20f)) {
            despawn = true;
            //stage check
            if (questUtil.getStage() <= 9) {
                questUtil.setStage(10);
                //completion text
                Global.getSector().getCampaignUI().addMessage("You have completed your objective. Report back to "+ questUtil.asteriaOrOutpost().getName()+" to finish the job.",
                        Global.getSettings().getColor("standardTextColor"),
                        "Report back to "+ questUtil.asteriaOrOutpost().getName(),
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
        if (questUtil.getStage() >= 10 && !despawn) {
            despawn = true;
        }
        //job FAILED
        if (f.age > JOB3_TIME_LIMIT && !despawn) {
            despawn = true;
            //stage check
            if (questUtil.getStage() <= 9) {
                questUtil.setFailed(true, JOB3_FAIL_KEY);
                questUtil.setStage(10);
                nskr_kestevenQuest.spawnEnvironmentalStorytelling();
                Global.getSector().getCampaignUI().addMessage("You have ran out of time, mission failed. Report back to "+ questUtil.asteriaOrOutpost().getName()+" to finish the job.",
                        Global.getSettings().getColor("standardTextColor"),
                        "mission failed",
                        "Report back to "+ questUtil.asteriaOrOutpost().getName(),
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
        questUtil.setMissionTimerJob3(questUtil.getMissionTimerJob3() - 0.1f);

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

    private void job4TargetLogic(fleetInfo f, CampaignFleetAPI fleet) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;

        boolean despawn = false;

        //despawn
        if (questUtil.getStage()>=17 || questUtil.getStage()>=14 && questUtil.getCompleted(nskr_artifactDialog.RECOVERED_4_KEY)) {
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
                questUtil.setCompleted(true, JOB4_DESTROYED_KEY);
                //no longer important
                if (fleet.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_MISSION_IMPORTANT)){
                    fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
                }
            }
        }

        //friendly found check
        if (fleet.getMemoryWithoutUpdate().contains(JOB4_FRIENDLY_KEY)){
            if (fleet.isVisibleToPlayerFleet() && !questUtil.getCompleted(JOB4_FOUND_FRIENDLY_KEY)){

                questUtil.setCompleted(true, JOB4_FOUND_FRIENDLY_KEY);
            }
        }
        //target found check
        if (fleet.getMemoryWithoutUpdate().contains(JOB4_TARGET_KEY)){
            if (fleet.isVisibleToPlayerFleet() && !questUtil.getCompleted(JOB4_FOUND_TARGET_KEY) && !questUtil.getCompleted(JOB4_DESTROYED_KEY)){

                questUtil.setCompleted(true, JOB4_FOUND_TARGET_KEY);
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
            SectorEntityToken loc = questUtil.getJob4EnemyTarget();
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
            boolean helped = questUtil.getCompleted(JOB4_HELPED_KEY);
            SectorEntityToken target = questUtil.getJob4FriendlyTarget();
            //safety check
            if (questUtil.asteriaOrOutpost() != null) {
                SectorEntityToken home = questUtil.asteriaOrOutpost().getPrimaryEntity();
                //go back to asteria
                if (fleet.getContainingLocation() == target.getContainingLocation() && helped && fleet.getAI().getCurrentAssignmentType() == FleetAssignment.ORBIT_PASSIVE) {
                    //no longer important
                    if (fleet.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_MISSION_IMPORTANT)) {
                        fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
                    }
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, home, Float.MAX_VALUE, "travelling back to " + questUtil.asteriaOrOutpost().getName());
                }
            }
        }
    }

    private CampaignFleetAPI vengeanceEliza(boolean intercept){
        PersonAPI eliza = util.getEliza();
        SectorEntityToken loc = questUtil.getElizaLoc();
        //-rep
        if (!intercept) eliza.getRelToPlayer().adjustRelationship(-0.75f, RepLevel.VENGEFUL);
        //spawn fleet and add to list
        CampaignFleetAPI fleet;
        if (!intercept){
            fleet = questFleets.spawnElizaFleet(loc, eliza, nskr_elizaDialog.getRandom(), true, false);
        } else {
            fleet = questFleets.spawnElizaFleet(loc, eliza, nskr_elizaDialog.getRandom(), false, true);
        }
        //remove from market
        loc.getMarket().getCommDirectory().removePerson(eliza);
        loc.getMarket().removePerson(eliza);

        return fleet;
    }
    private CampaignFleetAPI vengeanceJack(){
        PersonAPI jack = util.getJack();
        SectorEntityToken loc = questUtil.asteriaOrOutpost().getPrimaryEntity();
        //spawn fleet and add to list
        CampaignFleetAPI fleet = questFleets.spawnJackFleet(loc, jack, nskr_kestevenQuest.getRandom());
        //remove from market
        loc.getMarket().getCommDirectory().removePerson(jack);
        loc.getMarket().removePerson(jack);
        //gone
        Global.getSector().getImportantPeople().removePerson("nskr_opguy");
        questUtil.setCompleted(true, JACK_GONE_KEY);
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
                if (stage==0 && !questUtil.getCompleted(HAS_FOUGHT_ENIGMA_KEY)) {
                    questUtil.setCompleted(true, HAS_FOUGHT_ENIGMA_KEY);
                }
                //job1 sensor check
                if (stage==1) {
                    questUtil.setCompleted(true, JOB1_SENSORED_KEY);
                    //completion text
                    Global.getSector().getCampaignUI().addMessage("You managed to gather sufficient data in battle for the task. Deliver it back to " + questUtil.asteriaOrOutpost().getName() + ".",
                            Global.getSettings().getColor("standardTextColor"),
                            "Deliver it back to " + questUtil.asteriaOrOutpost().getName(),
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
                    if (questUtil.getStage() <= 9 && contrib>0f) {
                        //FAIL
                        questUtil.setFailed(true, JOB3_FAIL_KEY);
                        questUtil.setStage(10);
                        Global.getSector().getCampaignUI().addMessage("You failed to neutralize the fleet stealthily. Report back to "+ questUtil.asteriaOrOutpost().getName()+" to finish the job.",
                                Global.getSettings().getColor("standardTextColor"),
                                "failed to neutralize the fleet stealthily",
                                "Report back to "+ questUtil.asteriaOrOutpost().getName(),
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
                    questUtil.setStage(14);
                    if (!questUtil.getFailed(JOB4_FAILED_KEY)) {
                        Global.getSector().getCampaignUI().addMessage("You attacked the Special Operations fleet. Mission failed, better not to talk to anyone about this.",
                                Global.getSettings().getColor("standardTextColor"),
                                "Mission failed",
                                "",
                                Global.getSettings().getColor("yellowTextColor"),
                                Global.getSettings().getColor("yellowTextColor"));

                        questUtil.setFailed(true, JOB4_FAILED_KEY);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void reportPlayerReputationChange(String faction, float delta) {

        //relations capper for Eliza ending
        if (questUtil.getCompleted(nskr_EndingElizaDialog.DIALOG_FINISHED_KEY)){
            if (faction.equals(ids.KESTEVEN_FACTION_ID)){
                //add Nex rel cap
                float max;
                if (nskr_modPlugin.IS_NEXELERIN) {
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
                if (nskr_modPlugin.IS_NEXELERIN) {
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

            data.put(PERSISTENT_RANDOM_KEY, new Random(util.getSeedParsed()));
        }
        return (Random) data.get(PERSISTENT_RANDOM_KEY);
    }

}
