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
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.world.MoteParticleScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import exerelin.campaign.DiplomacyManager;
import lostsector.campaign.kesteven.ExileManager;
import lostsector.campaign.enigma.DormantSpawner;
import lostsector.campaign.events.EnvironmentalStorytelling;
import lostsector.dialogue.rules.nskr_job4FleetDialog;
import lostsector.dialogue.rules.nskr_ttCollectorDialog;
import lostsector.ModPlugin;
import lostsector.helper.FleetHelper;
import lostsector.helper.Ids;
import lostsector.helper.MathHelper;
import lostsector.helper.SectorLookup;
import lostsector.world.systems.cache.Cache;
import lostsector.helper.SystemHelper;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestStageManager extends BaseCampaignEventListener implements EveryFrameScript  {
    //
    //manages quest stage changes and mission fleets
    public static final String FLEET_ARRAY_KEY = "$kQuestMissionFleets";
    public static final String TT_COLLECTOR_KEY = "$KestevenQuestTTCollector";
    public static final ArrayList<String> JOB3_MARKET_BLACKLIST = new ArrayList<>();
    static {
        JOB3_MARKET_BLACKLIST.add("eochu_bres");
        JOB3_MARKET_BLACKLIST.add("culann");
    }
    public static final String JOB4_SPLINTER_KEY = "$KestevenQuestJob4Splinter";
    public static final String JOB4_TARGET_KEY = "$KestevenQuestJob4Target";
    public static final String JOB4_FRIENDLY_KEY = "$KestevenQuestJob4Friendly";
    public static final String ARTIFACT_KEY = "$kQuestArtifact";
    public static final String JOB4_HINT_WRECK_ID_KEY = "$job4HintWreck";
    public static final String JACK_REVENGEANCE_FLEET_KEY = "$RevengeanceJack";
    public static final String REVENGEANCE_FLEET_KEY = "$RevengeanceQuestFleet";
    public static final String ELIZA_INTERCEPT_FLEET_KEY = "$InterceptPlayerElizaFleet";

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

    private final List<CampaignFleetAPI> removed = new ArrayList<>();

    public QuestStageManager() {
        super(false);
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
        // QuestManager creates the state at the end of ModPlugin.onGameLoad, so it exists here unless the quest is unavailable.
        KestevenState state = KestevenQuest.state();
        if (state == null) return;

        stage = QuestHelper.getStage();

        //both mission markets lost failure
        if (!ExileManager.canExile() && !SectorLookup.asteriaExists() && !QuestHelper.getEndMissions()){
            QuestHelper.setStage(99);
            QuestHelper.setEndMissions(true);
            log("ERROR sector is fucked, ending missions");
        }
        //kill Special Operations fleet failure
        if (stage == 14 && QuestHelper.getCompleted(KestevenFlag.JOB4_FAILED) && !QuestHelper.getEndMissions()) {
            QuestHelper.setStage(99);
            QuestHelper.setEndMissions(true);

        }
        //kill Eliza after handing over failure
        if (stage == 19 && QuestHelper.getCompleted(KestevenFlag.CHIP_HANDED_TO_ELIZA) && QuestHelper.getCompleted(KestevenFlag.ELIZA_KILLED) && !QuestHelper.getEndMissions()) {
            QuestHelper.setCompleted(true, KestevenFlag.JOB5_FAILED);

            QuestHelper.setStage(99);
            QuestHelper.setEndMissions(true);

        }
        //////////////////
        //done while paused
        //////////////////
        //start job5
        if (stage ==15) {
            //cache found check
            if (!QuestHelper.getCompleted(KestevenFlag.CACHE_FOUND) && Global.getSector().getStarSystem("Unknown Site").isEnteredByPlayer()) {
                QuestHelper.setCompleted(true, KestevenFlag.CACHE_FOUND);
            }
        }
        //eliza loc changer
        if (stage>=16 && QuestHelper.getCompleted(KestevenFlag.ELIZA_FOUND) && !QuestHelper.getCompleted(KestevenFlag.ELIZA_KILLED)) {
            if(QuestHelper.getElizaLoc().getMarket().isPlanetConditionMarketOnly()){
                log("Qmanager eliza loc deciv, changing");
                PersonAPI eliza = KestevenPeople.getEliza();
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
        if (stage==16) {
            //cache found, before mission
            if (QuestHelper.getCompleted(KestevenFlag.CACHE_FOUND)) {
                QuestHelper.setStage(17);
            }
        }
        //////////////////
        //pause check
        //////////////////
        if (Global.getSector().isPaused()) return;
        //timer
        if (Global.getSector().isInFastAdvance()) {
            state.dayCounter += 2f*amount;
            state.fleetCounter += 2f*amount;
        } else{
            state.dayCounter += amount;
            state.fleetCounter += amount;
        }
        //FLEETS
        List<FleetInfo> fleets = FleetHelper.getFleets(FLEET_ARRAY_KEY);

        //wait for job 4
        if (stage == 11) {
            if (Global.getSector().isInFastAdvance()) {
                state.job4WaitCounter += 2f*amount;
            } else{
                state.job4WaitCounter += amount;
            }
            //one day = 10f
            if (state.job4WaitCounter>(300f)){
                QuestHelper.setCompleted(true, KestevenFlag.JOB4_WAIT_OVER);
                state.job4WaitCounter = 0f;
            }
        }
        //start job 4
        if (stage ==12) {
            //Adds our intel
            if (!state.job4IntelAdded) {
                OperationLifesaverIntel intel4 = new OperationLifesaverIntel();
                Global.getSector().getIntelManager().addIntel(intel4, false);
                state.job4IntelAdded = true;
                log("Qmanager added INTEL for " + "Operation Lifesaver");
            }
            //logic
            if (!state.job4FleetsSpawned) {

                //this sets job4TargetLoc
                KestevenFleets.spawnJob4Target();

                QuestHelper.spawnArtifact(QuestHelper.getJob4EnemyTarget(),4);
                CampaignFleetAPI fleet = KestevenFleets.spawnJob4Friendly();
                fleets.add(new FleetInfo(fleet, null, QuestHelper.getJob4FriendlyTarget()));
                for (int x = 0; x<SPLINTER_COUNT;x++) {
                    new Pair<>(KestevenFleets.spawnJob4Splinters(), 0f);
                    //added to mem in the spawner
                }
                //hint wrecks/environmental storytelling
                spawnJob4Wrecks(KestevenQuest.random(KestevenState.RANDOM_QUEST));

                log("Qmanager spawn job4 fleets");
                state.job4FleetsSpawned = true;
            }
        }
        //job 4 logic
        //dialog reveal logic
        if (nskr_job4FleetDialog.getDialogStage()>=1 && !QuestHelper.getCompleted(KestevenFlag.JOB4_TARGET_FOUND) && !QuestHelper.getCompleted(KestevenFlag.JOB4_TARGET_DESTROYED)){

            QuestHelper.setCompleted(true, KestevenFlag.JOB4_TARGET_HINT);
        }
        //job 4
        //found friendly
        if (nskr_job4FleetDialog.getDialogStage()>=1 && !QuestHelper.getCompleted(KestevenFlag.JOB4_FRIENDLY_FOUND)){

            QuestHelper.setCompleted(true, KestevenFlag.JOB4_FRIENDLY_FOUND);
        }

        //job 4 completion
        if (stage ==12 && QuestHelper.getCompleted(KestevenFlag.JOB4_FRIENDLY_FOUND) && QuestHelper.getCompleted(KestevenFlag.JOB4_TARGET_DESTROYED)){
            //completion text
            Global.getSector().getCampaignUI().addMessage("With the threat eliminated and the Operations fleet located, you can report back to "+ SectorLookup.asteriaOrOutpost().getName()+" to finish the job.",
                    Global.getSettings().getColor("standardTextColor"),
                    "report back to "+ SectorLookup.asteriaOrOutpost().getName(),
                    "",
                    Global.getSettings().getColor("yellowTextColor"),
                    Global.getSettings().getColor("yellowTextColor"));

            QuestHelper.setStage(13);
        }
        if (stage==16) {
           //job 5 logic
           //found frost check
           if (!QuestHelper.getCompleted(KestevenFlag.FROST_FOUND) && QuestHelper.getCompleted(KestevenFlag.JOB5_ALICE_TIP2)){
               if (pf.getContainingLocation()!=null && pf.getContainingLocation()== SectorLookup.getFrost()){
                   //found
                   QuestHelper.setCompleted(true, KestevenFlag.FROST_FOUND);
               }
           }
           //all disks found check
           if (QuestHelper.getDisksRecovered()>=5){
                QuestHelper.setCompleted(true, KestevenFlag.ALL_DISKS_RECOVERED);
           }
            //cache found check
            if (!QuestHelper.getCompleted(KestevenFlag.CACHE_FOUND) && Global.getSector().getStarSystem("Unknown Site").isEnteredByPlayer()) {
                QuestHelper.setCompleted(true, KestevenFlag.CACHE_FOUND);
                QuestHelper.setStage(17);
            }
        }
        //cache found, no mission
        if (QuestHelper.getEndMissions() && Global.getSector().getStarSystem("Unknown Site").isEnteredByPlayer()){
            QuestHelper.setCompleted(true, KestevenFlag.CACHE_FOUND);
        }
        //add intel for cache
        if (QuestHelper.getCompleted(KestevenFlag.CACHE_FOUND)){
            if (!state.cacheIntelAdded) {
                CacheIntel cacheIntel = new CacheIntel(Global.getSector().getStarSystem("Unknown Site"));
                Global.getSector().getIntelManager().addIntel(cacheIntel, false);
                state.cacheIntelAdded = true;
                log("Qmanager added INTEL for " + "The Cache");
            }
        }

        //spawn cache guardian fleet
        if (pf.getStarSystem() != null && pf.getStarSystem()==Global.getSector().getStarSystem("Unknown Site")) {
            if (!state.cacheGuardianSpawned) {
                //timer for spawn
                if (Global.getSector().isInFastAdvance()) {
                    state.cacheSeconds += 2f * amount;
                } else {
                    state.cacheSeconds += amount;
                }
                //timer for ping
                if (Global.getSector().isInFastAdvance()) {
                    pingTimer += 2f * amount;
                } else {
                    pingTimer += amount;
                }
                //cache doubt dialog
                if (state.cacheSeconds > 35f) {
                    if (!state.cacheGuardianSpotPicked) {
                        QuestHelper.setCacheFleetLoc();
                        state.cacheGuardianSpotPicked = true;
                    }
                    CampaignUIAPI ui = Global.getSector().getCampaignUI();
                    //stage check
                    if (stage>=16 && !QuestHelper.getEndMissions() && !state.cacheDoubtShown) {
                        //UI check
                        if (!ui.isShowingDialog() && !ui.isShowingMenu()) {
                            Global.getSector().getCampaignUI().showInteractionDialog(new CacheDoubtDialog(), null);
                            state.cacheDoubtShown = true;
                        }
                    }
                }
                //PINGS
                //slow
                if (state.cacheSeconds > 45f && state.cacheSeconds < 75f) {
                    if (pingTimer>6f) {
                        spawnPing(pf);
                        pingTimer = 0f;
                    }
                }
                //fast
                if (state.cacheSeconds >= 75f) {
                    if (pingTimer>3f) {
                        spawnPing(pf);
                        pingTimer = 0f;
                    }
                }
                if (state.cacheSeconds > 90f) {
                    //start music
                    Global.getSector().getStarSystem(Ids.CACHE_SYSTEM_NAME).getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "nskr_cache_theme");
                    //add to mission fleets so we can track it
                    CampaignFleetAPI fleet = Cache.spawnGuardianFleet(pf, QuestHelper.getCacheFleetLoc());
                    Global.getSoundPlayer().playSound("ui_discovered_entity", 1f, 1f, fleet.getLocation(), new Vector2f());

                    fleets.add(new FleetInfo(fleet, null, QuestHelper.getCacheFleetLoc()));
                    state.cacheGuardianSpawned = true;
                }
            }
            //cache mote particles
            if (Math.random()<0.004f)MoteParticleScript.spawnMote(pf);
        }
        //job5 finish: restore the commission after the Eliza ending
        if (QuestHelper.getCompleted(KestevenFlag.COMMISSION_RESTORE_PENDING) && !state.commissionRestored){
            //have to wait a few frames for the vanilla commission to end
            frameWait++;
            if (frameWait==30) {
                float repPirates = state.commissionRepPirates;
                float repKesteven = state.commissionRepKesteven;
                float repHege = state.commissionRepHegemony;
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
                state.commissionRestored = true;
            }
        }
        //player betrays Eliza after completing the mission for her
        if (QuestHelper.getElizaLoc()!=null) {
            if (!QuestHelper.getElizaLoc().getMarket().isPlanetConditionMarketOnly()) {
                if (QuestHelper.getElizaLoc().getMarket().getFaction().getId().equals(Factions.PLAYER) &&
                        QuestHelper.getCompleted(KestevenFlag.ELIZA_HELPED) && !QuestHelper.getCompleted(KestevenFlag.ELIZA_BETRAYED) && QuestHelper.getCompleted(KestevenFlag.ELIZA_ENDING_DONE)) {
                    QuestHelper.setCompleted(true, KestevenFlag.ELIZA_BETRAYED);
                }
            }
        }
        //mission logic
        if (state.dayCounter>10f) {
            //job 4 cannot finish without finding the Special Operations fleet, so replace it if something else destroyed it first
            if (stage == 12 && state.job4FleetsSpawned && !QuestHelper.getCompleted(KestevenFlag.JOB4_FRIENDLY_FOUND) && !hasFleetWithKey(fleets, JOB4_FRIENDLY_KEY)) {
                CampaignFleetAPI fleet = KestevenFleets.spawnJob4Friendly();
                fleets.add(new FleetInfo(fleet, null, QuestHelper.getJob4FriendlyTarget()));
                log("Qmanager respawned job4 friendly");
            }
            //tt vengeance spawner
            //spawn once per campaign
            if (!state.collectorSpawned) {
                //can spawn check
                if (stage>=2 && stage<=15) {
                    boolean cargo = pf.getCargo().getCommodityQuantity("nskr_electronics") >= 50f;
                    Random random = nskr_ttCollectorDialog.getRandom();
                    if (random.nextFloat() < BASE_TT_COLLECT_CHANCE && pf.isInHyperspace() && pf.getLocation().length() < 25000f && cargo) {

                        CampaignFleetAPI fleet = KestevenFleets.spawnCollectorFleet();
                        fleets.add(new FleetInfo(fleet, null, fleet.getContainingLocation().createToken(fleet.getLocation())));

                        state.collectorSpawned = true;
                        log("Qmanager SPAWNING");
                    }
                }
            }
            if (!state.elizaInterceptSpawned){
                //eliza intercept player for UPC
                if (stage==19 && QuestHelper.getCompleted(KestevenFlag.ELIZA_HELPED)){
                    CampaignFleetAPI fleet = vengeanceEliza(true);
                    state.elizaInterceptSpawned = true;
                    log("Intercepted Eliza");
                }
            }
            //vengeance Eliza betray by player, after questline
            if (QuestHelper.getCompleted(KestevenFlag.ELIZA_BETRAYED) && !state.elizaRevengeSpawned){
                CampaignFleetAPI fleet = vengeanceEliza(false);
                state.elizaRevengeSpawned = true;
            }
            //revengeace fleet spawner
            if (getRandom().nextFloat()<REVENGEANCE_CHANCE && stage == 20 && !state.jackRevengeSpawned) {
                //jack
                if (QuestHelper.getCompleted(KestevenFlag.ELIZA_ENDING_DONE) || QuestHelper.getCompleted(KestevenFlag.ALT_ENDING_DONE)){
                    CampaignFleetAPI fleet = vengeanceJack();
                    state.jackRevengeSpawned = true;
                    log("Revengeanced Jack");
                }
            }
            //END
            state.dayCounter = 0f;
        }
        //fleet logic once a seconds (10s is a day)
        if (state.fleetCounter>1f){
            //fleet logic
            runFleetLogic(fleets);

            //clean the list
            FleetHelper.cleanUp(removed, fleets);
            removed.clear();
            //save to mem
            FleetHelper.setFleets(fleets, FLEET_ARRAY_KEY);

            //
            state.fleetCounter = 0f;
        }
    }

    private void respawnEliza(SectorEntityToken loc) {
        PersonAPI eliza = KestevenPeople.getEliza();
        //add eliza to market
        loc.getMarket().getCommDirectory().addPerson(eliza,1);
        loc.getMarket().addPerson(eliza);

        //check for ending
        QuestHelper.setCompleted(true, KestevenFlag.ELIZA_RETURNED);
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

    private static boolean hasFleetWithKey(List<FleetInfo> fleets, String key) {
        for (FleetInfo f : fleets) {
            if (f.fleet != null && f.fleet.getMemoryWithoutUpdate().contains(key)) return true;
        }
        return false;
    }

    private void runFleetLogic(List<FleetInfo> fleets){
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;

        //mission related fleets manager
        for (FleetInfo f : fleets) {
            CampaignFleetAPI fleet = f.fleet;
            //age update
            f.age+=0.1f;


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
                    //a fleet that chased the player into hyperspace has no star system
                    SectorEntityToken patrolCenter = fleet.getStarSystem() != null
                            ? fleet.getStarSystem().getCenter()
                            : fleet.getContainingLocation().createToken(fleet.getLocation());
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, patrolCenter, Float.MAX_VALUE, "patrolling");
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
                boolean paid = nskr_ttCollectorDialog.getPaid();
                //AI LOGIC
                //intercept
                if (!paid) {
                    FleetHelper.gotoAndInterceptPlayerAI(fleet, f, FleetHelper.InterceptBehaviour.AROUND);
                }
                //leave
                if (paid) {
                    if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.GO_TO_LOCATION_AND_DESPAWN) {
                        fleet.clearAssignments();
                        fleet.getMemoryWithoutUpdate().clear();
                        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);

                        SectorEntityToken loc = SystemHelper.getRandomFactionMarket(new Random(), Factions.TRITACHYON);
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
            if (fleet.getMemoryWithoutUpdate().contains(KestevenFleets.ELIZA_RAIDED_FLEET_KEY)){
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
                if (despawn && !QuestHelper.getCompleted(KestevenFlag.ELIZA_KILLED)){
                    //eliza is gone
                    QuestHelper.setCompleted(true, KestevenFlag.ELIZA_KILLED);
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

                FleetHelper.gotoAndInterceptPlayerAI(fleet, f, FleetHelper.InterceptBehaviour.AROUND);
                continue;
            }
            //eliza fleet, after recovering UPC
            if (fleet.getMemoryWithoutUpdate().contains(ELIZA_INTERCEPT_FLEET_KEY)){
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
                if (despawn && !QuestHelper.getCompleted(KestevenFlag.ELIZA_KILLED)){
                    //eliza is gone
                    QuestHelper.setCompleted(true, KestevenFlag.ELIZA_KILLED);
                    //gone
                    Global.getSector().getImportantPeople().removePerson("nskr_anarchist");
                }


                Vector2f fp = fleet.getLocationInHyperspace();
                Vector2f pp = pf.getLocationInHyperspace();
                float dist = MathUtils.getDistance(pp, fp);
                //stop chasing eventually if not handed over
                if (QuestHelper.getCompleted(KestevenFlag.ELIZA_INTERCEPT_TALKED) && !QuestHelper.getCompleted(KestevenFlag.CHIP_HANDED_TO_ELIZA) && f.age>60f && !despawn){
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
                if (!QuestHelper.getCompleted(KestevenFlag.CHIP_HANDED_TO_ELIZA)) {
                    FleetHelper.gotoAndInterceptPlayerAI(fleet, f, FleetHelper.InterceptBehaviour.DIRECT);
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
                FleetHelper.gotoAndInterceptPlayerAI(fleet, f, FleetHelper.InterceptBehaviour.AROUND);
                continue;
            }
        }
    }

    private void job4TargetLogic(FleetInfo f, CampaignFleetAPI fleet) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;

        boolean despawn = false;

        //despawn
        if (QuestHelper.getStage()>=17 || QuestHelper.getStage()>=14 && QuestHelper.getCompleted(KestevenFlag.SATELLITE4_RECOVERED)) {
            despawn = true;
        }
        //destroyed
        if (fleet.getFleetPoints()<=0) {
            despawn = true;
        }

        //target destroyed check
        if (fleet.getMemoryWithoutUpdate().contains(JOB4_TARGET_KEY)){

            if (fleet.getFleetPoints() < (f.strength * 0.20f)) {
                despawn = true;
                QuestHelper.setCompleted(true, KestevenFlag.JOB4_TARGET_DESTROYED);
                //no longer important
                if (fleet.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_MISSION_IMPORTANT)){
                    fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
                }
            }
        }

        //friendly found check
        if (fleet.getMemoryWithoutUpdate().contains(JOB4_FRIENDLY_KEY)){
            if (fleet.isVisibleToPlayerFleet() && !QuestHelper.getCompleted(KestevenFlag.JOB4_FRIENDLY_FOUND)){

                QuestHelper.setCompleted(true, KestevenFlag.JOB4_FRIENDLY_FOUND);
            }
        }
        //target found check
        if (fleet.getMemoryWithoutUpdate().contains(JOB4_TARGET_KEY)){
            if (fleet.isVisibleToPlayerFleet() && !QuestHelper.getCompleted(KestevenFlag.JOB4_TARGET_FOUND) && !QuestHelper.getCompleted(KestevenFlag.JOB4_TARGET_DESTROYED)){

                QuestHelper.setCompleted(true, KestevenFlag.JOB4_TARGET_FOUND);
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
            boolean helped = QuestHelper.getCompleted(KestevenFlag.JOB4_FRIENDLY_HELPED);
            SectorEntityToken target = QuestHelper.getJob4FriendlyTarget();
            //safety check
            if (SectorLookup.asteriaOrOutpost() != null) {
                SectorEntityToken home = SectorLookup.asteriaOrOutpost().getPrimaryEntity();
                //go back to asteria
                if (fleet.getContainingLocation() == target.getContainingLocation() && helped && fleet.getAI().getCurrentAssignmentType() == FleetAssignment.ORBIT_PASSIVE) {
                    //no longer important
                    if (fleet.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_MISSION_IMPORTANT)) {
                        fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
                    }
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, home, Float.MAX_VALUE, "travelling back to " + SectorLookup.asteriaOrOutpost().getName());
                }
            }
        }
    }

    private CampaignFleetAPI vengeanceEliza(boolean intercept){
        PersonAPI eliza = KestevenPeople.getEliza();
        SectorEntityToken loc = QuestHelper.getElizaLoc();
        //-rep
        if (!intercept) eliza.getRelToPlayer().adjustRelationship(-0.75f, RepLevel.VENGEFUL);
        //spawn fleet and add to list
        CampaignFleetAPI fleet;
        if (!intercept){
            fleet = KestevenFleets.spawnElizaFleet(loc, eliza, ElizaDialog.getRandom(), true, false);
        } else {
            fleet = KestevenFleets.spawnElizaFleet(loc, eliza, ElizaDialog.getRandom(), false, true);
        }
        //remove from market
        loc.getMarket().getCommDirectory().removePerson(eliza);
        loc.getMarket().removePerson(eliza);

        return fleet;
    }
    private CampaignFleetAPI vengeanceJack(){
        PersonAPI jack = KestevenPeople.getJack();
        SectorEntityToken loc = SectorLookup.asteriaOrOutpost().getPrimaryEntity();
        //spawn fleet and add to list
        CampaignFleetAPI fleet = KestevenFleets.spawnJackFleet(loc, jack, KestevenQuest.random(KestevenState.RANDOM_QUEST));
        //remove from market
        loc.getMarket().getCommDirectory().removePerson(jack);
        loc.getMarket().removePerson(jack);
        //gone
        Global.getSector().getImportantPeople().removePerson("nskr_opguy");
        QuestHelper.setCompleted(true, KestevenFlag.JACK_GONE);
        return fleet;
    }

    @Override
    public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {
        CampaignFleetAPI loser = plugin.getLoser();
        if (loser == null) return;
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
                    if (!QuestHelper.getFailed(KestevenFlag.JOB4_FAILED)) {
                        Global.getSector().getCampaignUI().addMessage("You attacked the Special Operations fleet. Mission failed, better not to talk to anyone about this.",
                                Global.getSettings().getColor("standardTextColor"),
                                "Mission failed",
                                "",
                                Global.getSettings().getColor("yellowTextColor"),
                                Global.getSettings().getColor("yellowTextColor"));

                        QuestHelper.setFailed(true, KestevenFlag.JOB4_FAILED);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void reportPlayerReputationChange(String faction, float delta) {

        //relations capper for Eliza ending
        if (QuestHelper.getCompleted(KestevenFlag.ELIZA_ENDING_DONE)){
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
        return KestevenQuest.random(KestevenState.RANDOM_REVENGE);
    }

}
