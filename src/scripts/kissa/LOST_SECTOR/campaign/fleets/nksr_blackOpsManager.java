package scripts.kissa.LOST_SECTOR.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.econ.nskr_upChip;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.fleetInfo;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleFleet;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleSystem;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.*;

public class nksr_blackOpsManager extends BaseCampaignEventListener implements EveryFrameScript {
    //
    //spawns black ops fleets from the planet with UPC condition
    //

    public static final float SPAWN_CHANCE = 0.05f;
    public static final float DEFEND_CHANCE = 0.33f;
    public static final float MAX_AGE = 150f;
    public static final int MAX_FLEETS = 12;

    public static final String FLEET_ARRAY_KEY = "$nksr_blackOpsManagerFleets";
    public static final String PERSISTENT_RANDOM_KEY = "nksr_blackOpsManagerRandom";
    public static final ArrayList<String> FLEET_NAMES = new ArrayList<>();
    static {
        FLEET_NAMES.add("Black Ops");
        FLEET_NAMES.add("Special Operations");
        FLEET_NAMES.add("Operations Fleet");
        FLEET_NAMES.add("Strike Force");
        FLEET_NAMES.add("Task Group");
        FLEET_NAMES.add("Strategic Fleet");
    }
    public static final ArrayList<String> TARGET_ENTITIES = new ArrayList<>();
    static {
        TARGET_ENTITIES.add(Entities.COMM_RELAY);
        TARGET_ENTITIES.add(Entities.COMM_RELAY_MAKESHIFT);
        TARGET_ENTITIES.add(Entities.NAV_BUOY);
        TARGET_ENTITIES.add(Entities.NAV_BUOY_MAKESHIFT);
        TARGET_ENTITIES.add(Entities.SENSOR_ARRAY);
        TARGET_ENTITIES.add(Entities.SENSOR_ARRAY_MAKESHIFT);
    }

    nskr_saved<Float> counter;
    private final List<CampaignFleetAPI> removed = new ArrayList<>();
    //CampaignFleetAPI pf;

    static void log(final String message) {
        Global.getLogger(nksr_blackOpsManager.class).info(message);
    }

    public nksr_blackOpsManager() {
        super(false);
        //how often we run logic
        this.counter = new nskr_saved<>("nksr_blackOpsManagerCounter", 0.0f);
        //init randoms
        getRandom();
    }

    @Override
    public void advance(float amount) {

        if (Global.getSector().isInFastAdvance()) {
            counter.val += 2f*amount;
        } else{
            counter.val += amount;
        }

        //logic
        if (counter.val>10f) {
            CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
            if (pf == null) return;
            //spawning
            SectorEntityToken home = getUPC();
            if (home!=null && fleetUtil.getFleets(FLEET_ARRAY_KEY).size() < MAX_FLEETS) {
                //rng check
                if (getRandom().nextFloat() < SPAWN_CHANCE) {
                    SectorEntityToken target = createOpsTarget(home, home.getFaction());
                    spawnOpsFleet(home, target, home.getFaction().getId());
                }
            }

            List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
            for (fleetInfo f : fleets) {
                CampaignFleetAPI fleet = f.fleet;
                boolean despawn = false;

                //update age
                f.age+=1f;

                //despawn logic
                if (f.age>MAX_AGE){
                    despawn=true;
                }
                if (fleet.getFleetPoints()<=0f){
                    despawn=true;
                }
                if (getUPC()==null) {
                    despawn=true;
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
                FleetAssignment assignment = fleet.getCurrentAssignment().getAssignment();
                boolean defending = f.home.getContainingLocation()==f.target.getContainingLocation();
                //prepare
                if (f.age < 3f && fleet.getContainingLocation() == f.home.getContainingLocation() && assignment != FleetAssignment.ORBIT_PASSIVE) {
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, f.home, Float.MAX_VALUE, "preparing");
                    log("PREPARING " + f.home.getName() + " IN " + f.home.getContainingLocation().getName());
                }
                //DENFEND
                if (defending) {
                    //patrol
                    if (f.age > 3f && f.age<110f && assignment != FleetAssignment.DEFEND_LOCATION) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, f.target, Float.MAX_VALUE, "on mission");
                        log("MOVING TO " + f.target.getName() + " IN " + f.target.getContainingLocation().getName());
                    }
                    //return
                    if (f.age > 110f && f.age<120f && assignment != FleetAssignment.GO_TO_LOCATION_AND_DESPAWN) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, f.home, Float.MAX_VALUE, "returning to " + f.home.getName());
                        log("RETURNING TO " + f.home.getName() + " IN " + f.home.getContainingLocation().getName());
                    }
                    //despawn
                    if (f.age > 120f && assignment != FleetAssignment.ORBIT_PASSIVE) {
                        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, f.home, Float.MAX_VALUE, "standing down");
                        log("DESPAWNING TO " + f.home.getName() + " IN " + f.home.getContainingLocation().getName());
                    }
                } else {
                //ATTACK
                    //move
                    if (f.age > 3f && fleet.getContainingLocation()!=f.target.getContainingLocation() && assignment != FleetAssignment.GO_TO_LOCATION) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, f.target, Float.MAX_VALUE, "moving to location");
                        fleet.setTransponderOn(false);
                        log("MOVING TO " + f.target.getName() + " IN " + f.target.getContainingLocation().getName());
                    }
                    //attack
                    if (f.age < 90f && fleet.getContainingLocation() == f.target.getContainingLocation() && assignment != FleetAssignment.ATTACK_LOCATION) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.ATTACK_LOCATION, f.target, Float.MAX_VALUE, "on mission");
                        log("ATTACKING IN " + f.target.getContainingLocation().getName());
                    }
                    //return
                    if (f.age > 90f && fleet.getContainingLocation()!=f.home.getContainingLocation()  && assignment != FleetAssignment.GO_TO_LOCATION) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, f.home, Float.MAX_VALUE, "returning to " + f.home.getName());
                        log("RETURNING TO " + f.home.getName() + " IN " + f.home.getContainingLocation().getName());
                    }
                    //despawn
                    if (f.age > 90f && fleet.getContainingLocation()==f.home.getContainingLocation() && assignment != FleetAssignment.GO_TO_LOCATION_AND_DESPAWN) {
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, f.home, Float.MAX_VALUE, "standing down");
                        fleet.setTransponderOn(true);
                        log("DESPAWNING TO " + f.home.getName() + " IN " + f.home.getContainingLocation().getName());
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
    }

    void spawnOpsFleet(SectorEntityToken loc, SectorEntityToken target, String faction) {
        Random random = getRandom();

        float points = mathUtil.getSeededRandomNumberInRange(50f, 150f, random);

        //apply settings
        points *= nskr_modPlugin.getScriptedFleetSizeMult();

        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);

        simpleFleet simpleFleet = new simpleFleet(loc, "prot_ops", points, keys, random);
        simpleFleet.maxShipSize = 3;
        simpleFleet.sMods = mathUtil.getSeededRandomNumberInRange(2, 4, random);
        simpleFleet.name = FLEET_NAMES.get(mathUtil.getSeededRandomNumberInRange(0, FLEET_NAMES.size() - 1, random));
        simpleFleet.assignment = FleetAssignment.ORBIT_PASSIVE;
        simpleFleet.assignmentText = "preparing";
        //simpleFleet.noFactionInName = true;
        CampaignFleetAPI fleet = simpleFleet.create();

        fleet.setFaction(faction, true);

        //update
        fleetUtil.update(fleet, random);

        //add to mem IMPORTANT
        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
        fleets.add(new fleetInfo(fleet, target, loc));
        fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

        log("SPAWNED, size " + points + " loc " + loc.getName() + " system " + loc.getContainingLocation().getName());
        log("FLEET, loc " + fleet.getStarSystem().getName() +" size "+ fleet.getFleetPoints() + " commander " + fleet.getCommander().getName().getFullName() + " flagship " + fleet.getFlagship().getHullSpec().getBaseHullId());
    }

    public static SectorEntityToken getUPC(){
        SectorEntityToken loc = null;
        for (StarSystemAPI sys : Global.getSector().getStarSystems()){
            for (SectorEntityToken e : sys.getAllEntities()){
                if (e.getMarket()!=null){
                    if (!e.getMarket().isPlanetConditionMarketOnly()){
                        if (e.getMarket().hasCondition(nskr_upChip.ID)){
                            loc = e;
                            break;
                        }
                    }
                }
            }
            if (loc!=null) break;
        }
        return loc;
    }

    public static SectorEntityToken createOpsTarget(SectorEntityToken home, FactionAPI faction){
        Random random = getRandom();
        SectorEntityToken target = null;
        //defend
        if (random.nextFloat()<DEFEND_CHANCE){
            ArrayList<SectorEntityToken> defend = new ArrayList<>();
            //home market
            defend.add(home);
            for (SectorEntityToken e : home.getStarSystem().getAllEntities()){
                if (e.getCustomEntityType()==null) continue;
                //comm relays etc.
                if (TARGET_ENTITIES.contains(e.getCustomEntityType())){
                    defend.add(e);
                }
            }
            //pick
            target = defend.get(mathUtil.getSeededRandomNumberInRange(0, defend.size() - 1, random));
        }
        //attack
        else {
            //has hostiles check
            if (hasHostileFactions(faction)){
                while (target==null){
                    ArrayList<SectorEntityToken> attack = new ArrayList<>();
                    StarSystemAPI sys = getRandomCoreSystem(random);
                    log("SYS "+sys.getName());
                    for (MarketAPI market : Misc.getMarketsInLocation(sys.getCenter().getContainingLocation())) {
                        SectorEntityToken primary = market.getPrimaryEntity();
                        if (market.getFaction() != null && market.getFaction().isHostileTo(faction)) {
                            //market
                            attack.add(primary);
                        }
                        boolean targets = hasTargets(sys);
                        //chance to go for comm relays etc. instead
                        if (targets && random.nextFloat()<0.50f) {
                            for (SectorEntityToken e : sys.getAllEntities()) {
                                if (e.getCustomEntityType() == null) continue;
                                if (TARGET_ENTITIES.contains(e.getCustomEntityType())) {
                                    attack.add(e);
                                }
                            }
                        }
                    }
                    //pick
                    if (!attack.isEmpty()) target=attack.get(mathUtil.getSeededRandomNumberInRange(0, attack.size() - 1, random));
                }
            } else {
                //no hostiles
                //pick
                target=util.getRandomMarket(random, true);
            }
        }

        return target;
    }

    public static boolean hasHostileFactions(FactionAPI faction) {
        boolean hostile = false;
        for (FactionAPI f : Global.getSector().getAllFactions()){
            if (f.isHostileTo(faction)){
                if (!getFactionMarkets(f).isEmpty()){
                    hostile = true;
                    break;
                }
            }
        }
        return hostile;
    }

    //copy from Misc with extra checks
    public static List<MarketAPI> getFactionMarkets(FactionAPI faction) {
        List<MarketAPI> result = new ArrayList<>();
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.getFaction() == faction) {
                if (market.isPlanetConditionMarketOnly() || market.isHidden()) continue;
                if (!market.getStarSystem().hasTag(Tags.THEME_CORE)) continue;
                result.add(market);
            }
        }
        return result;
    }

    public static boolean hasTargets(StarSystemAPI sys){
        boolean targets = false;
        for (SectorEntityToken e : sys.getAllEntities()){
            if (e.getCustomEntityType()==null) continue;
            if (TARGET_ENTITIES.contains(e.getCustomEntityType())){
                targets = true;
                break;
            }
        }
        return targets;
    }

    public static StarSystemAPI getRandomCoreSystem(Random random) {
        //pick tags
        List<String> pickTags = new ArrayList<>();
        pickTags.add(Tags.THEME_CORE);
        pickTags.add(Tags.THEME_CORE_POPULATED);

        simpleSystem simpleSystem = new simpleSystem(random, 1);
        simpleSystem.allowCore = true;
        simpleSystem.allowMarkets = true;
        simpleSystem.pickTags = pickTags;
        simpleSystem.pickOnlyMarket = true;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("ERROR no valid system");
        return util.getRandomNonCoreSystem(random);
    }

    public static Random getRandom() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {
            data.put(PERSISTENT_RANDOM_KEY,  new Random(new Random().nextLong()));
        }
        return (Random)data.get(PERSISTENT_RANDOM_KEY);
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }
}