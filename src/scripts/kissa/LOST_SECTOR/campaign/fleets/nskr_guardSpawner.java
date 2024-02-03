package scripts.kissa.LOST_SECTOR.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.fleetInfo;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleFleet;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.powerLevel;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.*;

public class nskr_guardSpawner extends BaseCampaignEventListener implements EveryFrameScript {
    //
    //spawns a fleet that guards Asteria, respawns after some time
    //
    public static final float RESPAWN_TIMER = 90f;
    public static final float DESPAWN_TIMER = 180f;
    public static final String FLEET_NAME = "Asterian People's armada";

    public static final String FLEET_ARRAY_KEY = "$nskr_guardSpawnerFleets";
    nskr_saved<Float> counter;
    nskr_saved<Float> respawnCounter;

    private final List<CampaignFleetAPI> removed = new ArrayList<>();
    //CampaignFleetAPI pf;
    Random random;

    static void log(final String message) {
        Global.getLogger(nskr_guardSpawner.class).info(message);
    }

    public nskr_guardSpawner() {
        super(false);
        //how often we run logic
        this.counter = new nskr_saved<>("guardCounter", 0.0f);
        this.respawnCounter = new nskr_saved<>("respawnCounterGuard", 0.0f);
        this.random = new Random();
    }

    @Override
    public void advance(float amount) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;
        MarketAPI market = Global.getSector().getEconomy().getMarket("nskr_asteria");

        if (Global.getSector().isInFastAdvance()) {
            counter.val += 2f*amount;
        } else{
            counter.val += amount;
        }
        //logic
        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
        if (counter.val>10f) {
            //timer
            if (respawnCounter.val>0f){
                respawnCounter.val -= 1f;
            }
            for (fleetInfo f : fleets) {
                CampaignFleetAPI fleet = f.fleet;
                //timer
                f.age+=1f;
                boolean despawn = false;

                if (fleet.getFleetPoints()*2.0f<f.strength){
                    despawn = true;
                    respawnCounter.val = RESPAWN_TIMER;
                    log("guardSpawner DEFEATED starting respawn timer");
                }
                if (f.age>DESPAWN_TIMER){
                    despawn = true;
                    log("guardSpawner OLDFLEET changing fleets");
                }

                Vector2f fp = fleet.getLocationInHyperspace();
                Vector2f pp = pf.getLocationInHyperspace();
                float dist = MathUtils.getDistance(pp, fp);
                if (despawn) {
                    if (dist > Global.getSettings().getMaxSensorRangeHyper()) {
                        //tracker for cleaning the list
                        this.removed.add(fleet);
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
                boolean hostile = Global.getSector().getFaction("kesteven").getRelationship(Factions.PLAYER) <= -0.5f;
                boolean playerVisible = false;
                if (fleet.getContainingLocation().equals(pf.getContainingLocation())) {
                    playerVisible = pf.isVisibleToSensorsOf(fleet);
                }
                //intercept
                if (playerVisible && hostile) {
                    if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.INTERCEPT) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.INTERCEPT, pf, Float.MAX_VALUE, "engaging");
                        log("guardSpawner " + fleet.getName() + " INTERCEPTING ");
                    }
                }
                //reset
                if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.DEFEND_LOCATION && !playerVisible) {
                    if (f.home != null) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, f.home, Float.MAX_VALUE, "guarding " + f.home.getName());
                    }
                }
            }

            //clean the list
            fleetUtil.cleanUp(removed, fleets);
            removed.clear();
            //save to mem
            fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

            counter.val = 0f;
        }
        if (market==null) return;
        if (market.getFaction()!=Global.getSector().getFaction("kesteven")) return;
        //log("guardSpawner RES "+ respawnCounter.val + " S " + this.guardFleets.val.size());
        //logic
        //spawn one fleet at a time
        if (fleets.isEmpty() && respawnCounter.val<=0f && util.kestevenExists()) {
            this.spawnGuardFleet();
        }
    }

    void spawnGuardFleet() {
        MarketAPI market = Global.getSector().getEconomy().getMarket("nskr_asteria");
        if (market == null) return;
        if (market.getFaction()!=Global.getSector().getFaction("kesteven")) return;
        SectorEntityToken home = market.getPrimaryEntity();
        if (home == null) return;

        float combatPoints = MathUtils.getRandomNumberInRange(120, 140);
        //power scaling
        combatPoints += combatPoints* powerLevel.get(0.2f, 0f,1f);
        log("guardSpawner BASE " + combatPoints);

        //apply settings
        combatPoints *= nskr_modPlugin.getScriptedFleetSizeMult();

        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);

        simpleFleet simpleFleet = new simpleFleet(market.getPrimaryEntity(), "kesteven", combatPoints, keys, random);
        simpleFleet.type = FleetTypes.PATROL_LARGE;
        simpleFleet.qualityOverride = MathUtils.getRandomNumberInRange(0.60f, 0.80f);
        simpleFleet.name = FLEET_NAME;
        simpleFleet.noFactionInName = true;
        simpleFleet.assignment = FleetAssignment.DEFEND_LOCATION;
        simpleFleet.assignmentText = "guarding " + market.getName();
        CampaignFleetAPI fleet = simpleFleet.create();

        //add to mem IMPORTANT
        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
        fleets.add(new fleetInfo(fleet, null, home));
        fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

        log("guardSpawner SPAWNED " + fleet.getName() + " size " + combatPoints);
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }
}
