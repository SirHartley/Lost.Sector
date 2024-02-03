package scripts.kissa.LOST_SECTOR.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.customStart.gamemodeManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.fleetInfo;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleFleet;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleSystem;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static scripts.kissa.LOST_SECTOR.campaign.fleets.nskr_hyperspaceEnigmaSpawner.fleetLevel.*;

public class nskr_hyperspaceEnigmaSpawner extends BaseCampaignEventListener implements EveryFrameScript {
    //
    //spawns wandering enigma fleets in hyperspace
    //
    public static final float SPAWN_CHANCE_PER_DAY = 0.04f;
    public static final float MAX_AGE = 120f;
    public static final int MAX_FLEETS = 5;

    public static final float MIN_STRENGTH = 20f;
    public static final float MAX_STRENGTH = 130f;
    public static final float MIN_POWER = 0.53f;
    public static final float MAX_POWER = 2.00f;

    public static final float MIN_DISTANCE_FOR_SPAWNS = 20000f;
    public static final float DISTANCE_FOR_MAX_SPAWNS = 45000f;

    public final ArrayList<String> AMBUSH_NAMES = new ArrayList<>();
    {
        AMBUSH_NAMES.add("Assault Force");
        AMBUSH_NAMES.add("Lurker");
        AMBUSH_NAMES.add("Decoy Ops");
        AMBUSH_NAMES.add("Excision Ops");
    }
    public final ArrayList<String> INTERCEPT_NAMES = new ArrayList<>();
    {
        INTERCEPT_NAMES.add("Hunter Fleet");
        INTERCEPT_NAMES.add("Hunter Killer");
        INTERCEPT_NAMES.add("Task Fleet");
        INTERCEPT_NAMES.add("Seeker");
    }
    public final ArrayList<String> DORMANT_NAMES = new ArrayList<>();
    {
        DORMANT_NAMES.add("Splinter");
        DORMANT_NAMES.add("Fragment");
        DORMANT_NAMES.add("Combine");
        DORMANT_NAMES.add("Fracture");
    }
    public final ArrayList<String> MOVE_NAMES = new ArrayList<>();
    {
        MOVE_NAMES.add("Courser Fleet");
        MOVE_NAMES.add("Operation");
        MOVE_NAMES.add("Black Ops");
        MOVE_NAMES.add("Action Group");
    }

    public static final String FLEET_ARRAY_KEY = "$nskr_hyperspaceEnigmaSpawnerFleets";
    public static final String PERSISTENT_RANDOM_KEY = "nskr_hyperspaceEnigmaSpawnerRandom";
    public static final String PERSISTENT_FLEET_RANDOM_KEY = "nskr_hyperspaceEnigmaSpawnerFleetRandom";
    private final List<CampaignFleetAPI> removed = new ArrayList<>();
    //CampaignFleetAPI pf;
    nskr_saved<Float> counter;
    nskr_saved<Float> fleetCounter;

    static void log(final String message) {
        Global.getLogger(nskr_hyperspaceEnigmaSpawner.class).info(message);
    }

    public nskr_hyperspaceEnigmaSpawner() {
        super(false);
        //how often we run logic
        this.counter = new nskr_saved<>("nskr_hyperspaceEnigmaSpawnerCounter", 0.0f);
        this.fleetCounter = new nskr_saved<>("nskr_hyperspaceEnigmaSpawnerFleetCounter", 0.0f);
        //init randoms
        getRandom(PERSISTENT_RANDOM_KEY);
        getRandom(PERSISTENT_FLEET_RANDOM_KEY);
    }

    @Override
    public void advance(float amount) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) {
            return;
        }

        if (Global.getSector().isInFastAdvance()) {
            counter.val += 2f * amount;
            fleetCounter.val += 2f * amount;
        } else {
            counter.val += amount;
            fleetCounter.val += amount;
        }

        if (gamemodeManager.getMode() == gamemodeManager.gameMode.HELLSPAWN) return;

        //logic
        if (counter.val > 10f) {
            if (pf.isInHyperspace()) {
                float chance = SPAWN_CHANCE_PER_DAY;
                float dist = pf.getLocationInHyperspace().length();
                //min dist for spawns
                if (dist < MIN_DISTANCE_FOR_SPAWNS) chance = 0f;
                //keep dist in bounds
                dist = Math.min(Math.max(dist, MIN_DISTANCE_FOR_SPAWNS), DISTANCE_FOR_MAX_SPAWNS);
                float mult = mathUtil.normalize(dist, MIN_DISTANCE_FOR_SPAWNS, DISTANCE_FOR_MAX_SPAWNS);
                //max chance at max distance
                chance = mathUtil.lerp(0f, chance, mult);
                if (!util.enigmaExists()){
                    chance *= 0.25f;
                }

                //log("chance "+chance);

                //spawning
                if (fleetUtil.getFleets(FLEET_ARRAY_KEY).size() < MAX_FLEETS) {
                    float threatMult = nskr_stalkerSpawner.getEnigmaThreatScaling(false);
                    //rng check
                    if (getRandom(PERSISTENT_RANDOM_KEY).nextFloat() < chance * threatMult) {
                        Vector2f loc = new Vector2f(MathUtils.getPointOnCircumference(pf.getLocation(),
                                (pf.getSensorStrength() * 4.00f) * mathUtil.getSeededRandomNumberInRange(0.8f, 1.2f, getRandom(PERSISTENT_RANDOM_KEY)),
                                getRandom(PERSISTENT_RANDOM_KEY).nextFloat() * 360.0f));
                        float power = powerLevel.get(0.2f, MIN_POWER, MAX_POWER);
                        //strong enough check
                        if (power > MIN_POWER) {
                            spawnEnigmaFleets(loc, power, pickTask());
                        }
                    }
                }
            }
            counter.val = 0f;
        }

        //10x a day
        if (fleetCounter.val > 1f) {
            List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
            //FLEET LOGIC
            for (fleetInfo f : fleets) {
                CampaignFleetAPI fleet = f.fleet;
                boolean despawn = false;

                //update age
                f.age+=0.1f;

                //despawn logic
                if (f.age>MAX_AGE){
                    despawn=true;
                }
                if (fleet.getFleetPoints()<=0f){
                    despawn=true;
                }

                Vector2f fp = fleet.getLocationInHyperspace();
                Vector2f pp = pf.getLocationInHyperspace();
                float fleetDist = MathUtils.getDistance(pp, fp);
                if (despawn) {
                    if (fleetDist > Global.getSettings().getMaxSensorRangeHyper()) {
                        //tracker for cleaning the list
                        removed.add(fleet);
                        fleet.despawn();
                    }
                }
                //stop here when defeated
                if (despawn) continue;

                //assignment logic
                if (fleet.getAI()==null) continue;
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
                FleetAssignment assignment = fleet.getCurrentAssignment().getAssignment();
                //switch based on task type
                switch (f.task){
                    case INTERCEPT:
                        //try to attack for a while
                        if (f.age<10f || pf.isVisibleToSensorsOf(fleet)){
                            fleetUtil.gotoAndInterceptPlayerAI(fleet, f, fleetUtil.interceptBehaviour.AROUND);
                        } else {
                            fleetUtil.guardTargetAI(fleet, f, fleetUtil.guardMovementBehaviour.HOLD, fleetUtil.guardAttackBehaviour.PLAYER, 0.167f);
                        }
                        break;
                    case AMBUSH:
                        fleetUtil.guardTargetAI(fleet, f, fleetUtil.guardMovementBehaviour.HOLD, fleetUtil.guardAttackBehaviour.HOSTILE, 0.04f);
                        break;
                    case DORMANT:
                        //nothing lol
                        fleet.setTransponderOn(false);
                        continue;
                    case GO_TO_SYSTEM:
                        if (fleet.getContainingLocation()!=f.target.getContainingLocation() && assignment != FleetAssignment.GO_TO_LOCATION){
                            fleet.clearAssignments();
                            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, f.target, Float.MAX_VALUE, "travelling");
                            //log("set GO_TO_SYSTEM logic for "+fleet.getName());
                        }
                        if (fleet.getContainingLocation()==f.target.getContainingLocation()){
                            fleetUtil.guardTargetAI(fleet, f, fleetUtil.guardMovementBehaviour.ORBIT, fleetUtil.guardAttackBehaviour.PLAYER, 0.10f);
                            //log("set GO_TO_SYSTEM logic for "+fleet.getName());
                        }
                        break;
                }
                //

            }
            //clean the list
            fleetUtil.cleanUp(removed, fleets);
            removed.clear();
            //save to mem
            fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

            //
            fleetCounter.val = 0f;
        }
    }

    void spawnEnigmaFleets(Vector2f loc, float power, taskType task) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();

        Random random = getRandom(PERSISTENT_FLEET_RANDOM_KEY);

        //pick level
        fleetLevel lvl = getFleetLevel(power);

        float points = mathUtil.getSeededRandomNumberInRange(MIN_STRENGTH/2f, MAX_STRENGTH/2f, random);
        //
        points *= power;
        //
        float bias = 0.25f+(mathUtil.BiasFunction(random.nextDouble(), 0.33f));
        points *= bias;
        //cap min and max size
        points = Math.min(Math.max(points, MIN_STRENGTH), MAX_STRENGTH);

        //apply settings
        points *= nskr_modPlugin.getRandomEnigmaFleetSizeMult();

        //disabled spawn check
        if (points<=0f) return;

        String type = "patrolSmall";
        if (points > 35f) {
            type = "patrolMedium";
        }
        if (points > 70f) {
            type = "patrolLarge";
        }

        //base keys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);
        keys.add(MemFlags.MEMORY_KEY_PATROL_FLEET);
        //base
        SectorEntityToken home = pf.getContainingLocation().createToken(loc);
        simpleFleet simpleFleet = new simpleFleet(home, ids.ENIGMA_FACTION_ID, points, keys, random);
        simpleFleet.aiFleetProperties = true;
        simpleFleet.type = type;
        switch (lvl) {
            case FRIGATE:
                simpleFleet.maxShipSize = 1;
                break;
            case DESTROYER:
                simpleFleet.maxShipSize = 2;
                break;
            case CRUISER:
                simpleFleet.maxShipSize = 3;
                break;
        }

        SectorEntityToken target = null;
        //switch based on type
        switch (task){
            case INTERCEPT:
                if (nskr_stalkerSpawner.getEnigmaThreatScaling(true)>0f) {
                    keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
                    keys.add(MemFlags.MEMORY_KEY_NEVER_AVOID_PLAYER_SLOWLY);
                    keys.add(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE);

                    simpleFleet.assignmentText = "intercepting your fleet";
                    simpleFleet.interceptPlayer = true;
                    simpleFleet.assignment = FleetAssignment.INTERCEPT;
                    simpleFleet.name = INTERCEPT_NAMES.get(mathUtil.getSeededRandomNumberInRange(0, INTERCEPT_NAMES.size() - 1, random));
                    //reduce size for intercept
                    points *= 0.75f;
                } else {
                    //spawn dormant instead
                    //copy of dormant
                    keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
                    keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
                    keys.add(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE);

                    simpleFleet.qualityOverride = mathUtil.getSeededRandomNumberInRange(0.30f, 0.70f, random);
                    simpleFleet.assignmentText = "dormant";
                    simpleFleet.dormant = true;
                    simpleFleet.noTransponder = true;
                    simpleFleet.assignment = FleetAssignment.HOLD;
                    simpleFleet.name = DORMANT_NAMES.get(mathUtil.getSeededRandomNumberInRange(0, DORMANT_NAMES.size() - 1, random));
                }
                break;
            case AMBUSH:
                keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
                keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
                keys.add(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE);

                simpleFleet.sMods = mathUtil.getSeededRandomNumberInRange(0, 2, random);
                simpleFleet.assignmentText = "laying in wait";
                simpleFleet.noTransponder = true;
                simpleFleet.assignment = FleetAssignment.HOLD;
                simpleFleet.name = AMBUSH_NAMES.get(mathUtil.getSeededRandomNumberInRange(0, AMBUSH_NAMES.size() - 1, random));
                break;
            case DORMANT:
                keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
                keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);

                simpleFleet.qualityOverride = mathUtil.getSeededRandomNumberInRange(0.30f, 0.70f, random);
                simpleFleet.assignmentText = "dormant";
                simpleFleet.dormant = true;
                simpleFleet.noTransponder = true;
                simpleFleet.assignment = FleetAssignment.HOLD;
                simpleFleet.name = DORMANT_NAMES.get(mathUtil.getSeededRandomNumberInRange(0, DORMANT_NAMES.size() - 1, random));
                break;
            case GO_TO_SYSTEM:
                keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
                keys.add(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE);

                target = getGoToTarget(random);

                simpleFleet.sMods = mathUtil.getSeededRandomNumberInRange(0, 1, random);
                simpleFleet.assignmentText = "travelling";
                simpleFleet.noTransponder = true;
                simpleFleet.goToLocation = true;
                simpleFleet.goToLocationTarget = target;
                simpleFleet.assignment = FleetAssignment.GO_TO_LOCATION;
                simpleFleet.name = MOVE_NAMES.get(mathUtil.getSeededRandomNumberInRange(0, MOVE_NAMES.size() - 1, random));
                break;
        }

        //nerf fleets when gone
        if (!util.enigmaExists()){
            simpleFleet.qualityOverride = mathUtil.getSeededRandomNumberInRange(0.30f, 0.70f, random);
        }

        CampaignFleetAPI fleet = simpleFleet.create();

        //update
        fleetUtil.update(fleet, random);

        //add to mem IMPORTANT
        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
        fleetInfo info = new fleetInfo(fleet, target, home);
        info.task = task;
        fleets.add(info);
        fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

        log("SPAWNED, size " + points +" power "+power+" bias "+bias);
        //log("FLEET, loc " + fleet.getContainingLocation() +" size "+ fleet.getFleetPoints() + " commander " + fleet.getCommander().getName().getFullName() + " flagship " + fleet.getFlagship().getHullSpec().getBaseHullId());
    }

    @NotNull
    public static fleetLevel getFleetLevel(float power) {
        fleetLevel lvl;
        //hard mode
        if (nskr_modPlugin.getStarfarerMode()){
            if (power < 0.60f) {
                lvl = FRIGATE;
                log("lvl FRIGATE");
            } else if (power < 1.00f) {
                lvl = DESTROYER;
                log("lvl DESTROYER");
            } else {
                lvl = CRUISER;
                log("lvl CRUISER");
            }
        }
        //normal
        else {
            if (power < 0.70f) {
                lvl = FRIGATE;
                log("lvl FRIGATE");
            } else if (power < 1.20f) {
                lvl = DESTROYER;
                log("lvl DESTROYER");
            } else {
                lvl = CRUISER;
                log("lvl CRUISER");
            }
        }
        return lvl;
    }

    private SectorEntityToken getGoToTarget(Random random) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();

        simpleSystem simpleSystem = new simpleSystem(random, 2);
        simpleSystem.pickOnlyInProcgen = true;

        float maxDist = 8000f;
        //First, get all the valid systems and put them in a separate list
        List<StarSystemAPI> validSystems = new ArrayList<>();
        while (validSystems.isEmpty()) {
            for (StarSystemAPI system : simpleSystem.get()) {
                boolean isValid = false;
                //any near core
                float dist = MathUtils.getDistance(pf.getLocationInHyperspace(), system.getStar().getLocationInHyperspace());
                if (dist <= maxDist && dist > 0f) {
                    isValid = true;
                    //log("getRandomSystemNearLocation +1 valid, dist "+dist);
                }
                if (isValid) {
                    validSystems.add(system);
                }
            }
            //If that list is empty, retry
            if (validSystems.isEmpty()) {
                //increase search radius with every try
                maxDist *= 1.2f;
            }
        }
        //pick
        StarSystemAPI system = validSystems.get(mathUtil.getSeededRandomNumberInRange(0, validSystems.size() - 1, random));
        //random loc
        return util.getRandomLocationInSystem(system, false, true, random);
    }

    public static final List<Pair<taskType, Float>> TASKS = new ArrayList<>();
    static {
        TASKS.add(new Pair<>(taskType.DORMANT, 5f));
        TASKS.add(new Pair<>(taskType.INTERCEPT, 10f));
        TASKS.add(new Pair<>(taskType.AMBUSH, 15f));
        TASKS.add(new Pair<>(taskType.GO_TO_SYSTEM, 15f));
    }

    public taskType pickTask(){
        WeightedRandomPicker<taskType> picker = new WeightedRandomPicker<>();
        for (Pair<taskType,Float> s : TASKS){
            picker.add(s.one,s.two);
        }
        return picker.pick(getRandom(PERSISTENT_FLEET_RANDOM_KEY));
    }

    public enum taskType {
        DORMANT,
        INTERCEPT,
        AMBUSH,
        GO_TO_SYSTEM
    }

    public enum fleetLevel {
        FRIGATE,
        DESTROYER,
        CRUISER
    }

    public static Random getRandom(String id) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) {
            data.put(id, new Random(new Random().nextLong()));
        }
        return (Random)data.get(id);
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }
}
