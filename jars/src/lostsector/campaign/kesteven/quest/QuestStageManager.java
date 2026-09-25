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
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.impl.campaign.world.MoteParticleScript;
import lostsector.campaign.kesteven.ExileManager;
import lostsector.ModPlugin;
import lostsector.helper.FleetHelper;
import lostsector.helper.Ids;
import lostsector.helper.SectorLookup;
import lostsector.world.systems.cache.Cache;
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
    public static final ArrayList<String> JOB3_MARKET_BLACKLIST = new ArrayList<>();
    static {
        JOB3_MARKET_BLACKLIST.add("eochu_bres");
        JOB3_MARKET_BLACKLIST.add("culann");
    }
    public static final String JACK_REVENGEANCE_FLEET_KEY = "$RevengeanceJack";
    public static final String REVENGEANCE_FLEET_KEY = "$RevengeanceQuestFleet";

    //chance per day
    public static final float REVENGEANCE_CHANCE = 0.01f;
    private float pingTimer = 0;

    private int stage =0;
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

        if (stage==16) {
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
        //mission logic
        if (state.dayCounter>10f) {
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

    // Called by KestevenElizaFleetsModule when Eliza's fleet takes her home; T28 moves it with Eliza's port.
    static void respawnEliza(SectorEntityToken loc) {
        PersonAPI eliza = KestevenPeople.getEliza();
        //add eliza to market
        loc.getMarket().getCommDirectory().addPerson(eliza,1);
        loc.getMarket().addPerson(eliza);

        //check for ending
        QuestHelper.setCompleted(true, KestevenFlag.ELIZA_RETURNED);
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

    public static Random getRandom() {
        return KestevenQuest.random(KestevenState.RANDOM_REVENGE);
    }

}
