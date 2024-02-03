package scripts.kissa.LOST_SECTOR.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.fleetInfo;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleFleet;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleSystem;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.*;

public class nskr_kestevenScavenger extends BaseCampaignEventListener implements EveryFrameScript {
    //
    //spawns Scav fleets for kesteven that go to a random system with remnant or derelict tag, and then return to home after some time.
    //it just worksTM
    //
    public static final float SCAV_TIMER = 3f;
    public static final float DESPAWN_TIMER = 150f;
    public static final float SPAWN_CHANCE = 0.45f;
    //per day while in range
    public static final float BASE_BETRAY_CHANCE = 0.03f;
    public static final float MAX_EXTRA_BETRAY_CHANCE = 0.12f;
    public static final int PLAYER_CHASE_TIME = 10;
    public static final int BETRAY_COOLDOWN = 21;
    public static final int MAX_COUNT = 32;
    public static final float BASE_SIZE = 10f;
    public static final float MIN_STRENGTH = 25f;
    public static final float MAX_STRENGTH = 200f;
    public static final String SCAV_KEY = "SCAV";
    public static final String SCAV_KEY_HOME = SCAV_KEY+"_HOME";
    public static final String SCAV_KEY_HOSTILE = SCAV_KEY+"_TIME";
    public static final String SCAV_KEY_RESET = SCAV_KEY+"_RESET";
    public static final String BETRAYAL_KEY = "$BetrayalFleet";
    public static final String FLEET_ARRAY_KEY = "$nskr_kestevenScavengerFleets";

    nskr_saved<Float> spawnCounter;
    nskr_saved<Float> counter;
    private final List<CampaignFleetAPI> removed = new ArrayList<>();
    //CampaignFleetAPI pf;
    Random random;

    //Weights for the different types of locations we go to
    public static final LinkedHashMap<BaseThemeGenerator.LocationType, Float> WEIGHTS = new LinkedHashMap<>();
    static {
        WEIGHTS.put(BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT, 12f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.IN_ASTEROID_BELT, 6f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.IN_ASTEROID_FIELD, 6f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 8f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.IN_SMALL_NEBULA, 4f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.JUMP_ORBIT, 4f);
    }
    public final ArrayList<String> scavNames = new ArrayList<>();
    {
        scavNames.add("Scavenger Fleet");
        scavNames.add("Recovery Operations");
        scavNames.add("Salvage Fleet");
        scavNames.add("Prospector");
        scavNames.add("Special Materials Fleet");
        scavNames.add("Retriever");
        scavNames.add("Rescue Fleet");
        scavNames.add("Research Operation");
        scavNames.add("Probing Operation");
    }
    static void log(final String message) {
        Global.getLogger(nskr_kestevenScavenger.class).info(message);
    }

    public nskr_kestevenScavenger() {
        super(false);
        //how often scavs spawn
        this.spawnCounter = new nskr_saved<>("scavengerSpawnCounter", 0.0f);
        //how often we run logic
        this.counter = new nskr_saved<>("scavengerCounter", 0.0f);
        this.random = new Random();
    }

    @Override
    public void advance(float amount) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;

        if (Global.getSector().isInFastAdvance()) {
            spawnCounter.val += 2f*amount;
            counter.val += 2f*amount;
        } else{
            spawnCounter.val += amount;
            counter.val += amount;
        }

        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
        if (counter.val>10f) {
            for (fleetInfo f : fleets) {
                CampaignFleetAPI fleet = f.fleet;
                boolean despawn = false;
                f.age+=1f;

                //cull
                if (f.age > DESPAWN_TIMER) {
                    despawn = true;
                }
                if (fleet.getFleetPoints() < MIN_STRENGTH - 1f) {
                    despawn = true;
                }

                Vector2f fp = fleet.getLocationInHyperspace();
                Vector2f pp = pf.getLocationInHyperspace();
                float dist = MathUtils.getDistance(pp, fp);
                if (despawn) {
                    if (dist > Global.getSettings().getMaxSensorRangeHyper()) {
                        //tracker for cleaning the list
                        this.removed.add(fleet);
                        fleet.despawn();
                        removeData(fleet);
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
                SectorEntityToken target = getTarget(fleet);
                SectorEntityToken home = getHome(fleet);
                //BETRAYAL BEHAVIOR
                //cooldown
                if (getResetTimer(fleet) > 0) setResetTimer(fleet, getResetTimer(fleet)+1);
                //scuffed reset
                //set hostile time back to 0, se we can try to betray again
                if (getResetTimer(fleet) == 1) setHostileTime(fleet, 0);

                boolean betrayal = false;
                boolean playerVisible = false;
                if (pf.isVisibleToSensorsOf(fleet)) playerVisible = true;
                if (playerVisible && Global.getSector().getPlayerFaction().getRelationship("kesteven") <= -0.15f) {
                    //increased chance with worse relations
                    float rel = (Global.getSector().getPlayerFaction().getRelationship("kesteven") * 100f) + 100f;
                    rel = Math.max(50f, rel);
                    rel = mathUtil.normalize(rel, 50f, 85f);
                    float extra = mathUtil.lerp(MAX_EXTRA_BETRAY_CHANCE, 0f, rel);
                    float betrayalChance = extra + BASE_BETRAY_CHANCE;
                    log("ScavengerSpawner betrayal chance " + betrayalChance);

                    //don't betray in core systems or near it
                    float distanceFromCore = pf.getLocation().length() - 18000.0f;
                    boolean away = false;
                    if (pf.isInHyperspace() && distanceFromCore > 0f) {
                        away = true;
                    }
                    if (fleet.getStarSystem() != home.getStarSystem() && !fleet.isInHyperspace()) {
                        away = true;
                    }

                    //only try attacking if large enough
                    boolean strong = false;
                    if (pf.getFleetData().getEffectiveStrength() < fleet.getFleetData().getEffectiveStrength() * 1.20f) {
                        strong = true;
                        //log("ScavengerSpawner "+pf.getFleetData().getEffectiveStrength()+" VS "+fleet.getFleetData().getEffectiveStrength());
                    }
                    //try
                    if (Math.random() < betrayalChance && away && strong) {
                        betrayal = true;
                        log("ScavengerSpawner BETRAYAL");
                    }
                }
                //only try to betray once and don't stack it
                if (betrayal && getHostileTime(fleet) == 0) {
                    if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.INTERCEPT) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.INTERCEPT, pf, Float.MAX_VALUE, "intercepting your fleet");
                        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
                        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);
                        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE_ONE_BATTLE_ONLY, true);
                        fleet.getMemoryWithoutUpdate().set(BETRAYAL_KEY, true);
                        log("ScavengerSpawner INTERCEPTING");
                    }
                }
                if (fleet.getAI().getCurrentAssignmentType() == FleetAssignment.INTERCEPT) {
                    //tick logic timer
                    setHostileTime(fleet, getHostileTime(fleet)+1);
                }

                //RESET TO NORMAL
                //try to find player for 10 days, then reset back to normal
                if (getHostileTime(fleet) >= PLAYER_CHASE_TIME && !playerVisible && fleet.getAI().getCurrentAssignmentType() == FleetAssignment.INTERCEPT) {
                    fleet.getMemoryWithoutUpdate().clear();
                    fleet.clearAssignments();
                    setResetTimer(fleet, BETRAY_COOLDOWN);
                    log("ScavengerSpawner RESET TO NORMAL");
                    //RESET TO GO TO
                    if (f.age > 5f) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, target, Float.MAX_VALUE, "moving to " + target.getContainingLocation().getName());
                        log("ScavengerSpawner RESET MOVING TO " + target.getName() + " IN " + target.getContainingLocation().getName());
                    }
                    //RESET TO ARRIVED
                    if (fleet.getContainingLocation() == target.getContainingLocation()) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, target, Float.MAX_VALUE, "investigating");
                        log("ScavengerSpawner RESET SCAVENGING IN " + target.getContainingLocation().getName());
                    }
                    //RESET TO RETURN
                    if (f.age > 90f) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, home, Float.MAX_VALUE, "returning to " + home.getName());
                        log("ScavengerSpawner RESET RETURNING TO " + home.getName() + " IN " + home.getContainingLocation().getName());
                    }
                    //RESET TO DESPAWN
                    if (fleet.getContainingLocation() == home.getContainingLocation() && f.age > 90f) {
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, home, Float.MAX_VALUE, "standing down");
                        log("ScavengerSpawner RESET DESPAWNING TO " + home.getName() + " IN " + home.getContainingLocation().getName());
                    }
                }
                //NORMAL BEHAVIOR
                //GO TO
                if (fleet.getAI().getCurrentAssignmentType() == FleetAssignment.ORBIT_PASSIVE && f.age > 5f) {
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, target, Float.MAX_VALUE, "moving to " + target.getContainingLocation().getName());
                    log("ScavengerSpawner MOVING TO " + target.getName() + " IN " + target.getContainingLocation().getName());
                }
                //ARRIVED
                if (fleet.getAI().getCurrentAssignmentType() == FleetAssignment.GO_TO_LOCATION && fleet.getContainingLocation() == target.getContainingLocation()) {
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, target, Float.MAX_VALUE, "investigating");
                    log("ScavengerSpawner SCAVENGING IN " + target.getContainingLocation().getName());
                }
                //RETURN
                if (fleet.getAI().getCurrentAssignmentType() == FleetAssignment.PATROL_SYSTEM && f.age > 90f) {
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, home, Float.MAX_VALUE, "returning to " + home.getName());
                    log("ScavengerSpawner RETURNING TO " + home.getName() + " IN " + home.getContainingLocation().getName());
                }
                //DESPAWN
                if (fleet.getAI().getCurrentAssignmentType() == FleetAssignment.GO_TO_LOCATION && fleet.getContainingLocation() == home.getContainingLocation() && f.age > 90f) {
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, home, Float.MAX_VALUE, "standing down");
                    log("ScavengerSpawner DESPAWNING TO " + home.getName() + " IN " + home.getContainingLocation().getName());
                }
            }
            //clean the list
            fleetUtil.cleanUp(removed, fleets);
            removed.clear();
            //save to mem
            fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

            counter.val = 0f;
        }

        //don't spawn when kesteven is gone
        if (!util.kestevenExists()) return;

        //10 = 1 day
        if (spawnCounter.val>SCAV_TIMER*10f) {
            spawnCounter.val = 0f;
            if (Math.random()<SPAWN_CHANCE) {
                //max count reached
                if (fleets.size() >= MAX_COUNT) return;

                MarketAPI market = questUtil.getRandomFactionMarket(new Random(), "kesteven").getMarket();
                this.spawnScavFleets(market);
            }
        }
    }

    void spawnScavFleets(MarketAPI market) {
        if (market == null) return;

        float combatPoints = MathUtils.getRandomNumberInRange(1, 16);
        log("ScavengerSpawner BASE " + combatPoints);
        //exponential scaling of fleet size
        float bias = 0.25f+(mathUtil.BiasFunction(Math.random(), 0.75f));
        combatPoints *= bias;

        String type = "patrolSmall";
        if (combatPoints > 6) {
            type = "patrolMedium";
        }
        if (combatPoints > 12) {
            type = "patrolLarge";
        }
        combatPoints *= BASE_SIZE;
        if (combatPoints < MIN_STRENGTH) combatPoints = MIN_STRENGTH;
        if (combatPoints > MAX_STRENGTH) combatPoints = MAX_STRENGTH;

        //apply settings
        combatPoints *= nskr_modPlugin.getScriptedFleetSizeMult();

        simpleFleet simpleFleet = new simpleFleet(market.getPrimaryEntity(), "kesteven", combatPoints, new ArrayList<String>(), random);
        simpleFleet.type = type;
        simpleFleet.maxShipSize = 3;
        if (Math.random()<0.33f) simpleFleet.sMods = MathUtils.getRandomNumberInRange(1,2);
        if (Math.random()<0.15f) simpleFleet.qualityOverride = MathUtils.getRandomNumberInRange(0.60f, 0.90f);
        simpleFleet.freighterPoints = combatPoints/3f;
        simpleFleet.tankerPoints = combatPoints/3f;
        simpleFleet.linerPoints = combatPoints/6f;
        simpleFleet.utilityPoints = combatPoints/6f;
        simpleFleet.name = scavNames.get(MathUtils.getRandomNumberInRange(0, scavNames.size() - 1));
        simpleFleet.assignment = FleetAssignment.ORBIT_PASSIVE;
        simpleFleet.assignmentText = "preparing expedition";
        CampaignFleetAPI fleet = simpleFleet.create();

        //where we return to
        setHome(fleet, market.getPrimaryEntity());

        //add to mem IMPORTANT
        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
        fleets.add(new fleetInfo(fleet, null, market.getPrimaryEntity()));
        fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

        log("ScavengerSpawner SPAWNED " + fleet.getName() + " bias " + bias + " size " + combatPoints);
    }

    //memory clean up
    void removeData(CampaignFleetAPI fleet){
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = SCAV_KEY+fleet.getId();
        String id2 = SCAV_KEY_HOME+fleet.getId();
        String id3 = SCAV_KEY_HOSTILE+fleet.getId();
        String id4 = SCAV_KEY_RESET+fleet.getId();
        data.remove(id);
        data.remove(id2);
        data.remove(id3);
        data.remove(id4);
    }

    void setHostileTime(CampaignFleetAPI fleet, int timer) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = SCAV_KEY_HOSTILE+fleet.getId();
        data.put(id, timer);
    }

    int getHostileTime(CampaignFleetAPI fleet) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = SCAV_KEY_HOSTILE+fleet.getId();
        if (!data.containsKey(id))
            data.put(id, 0);

        return (int)data.get(id);
    }

    void setResetTimer(CampaignFleetAPI fleet, int timer) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = SCAV_KEY_RESET+fleet.getId();
        data.put(id, timer);
    }

    int getResetTimer(CampaignFleetAPI fleet) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = SCAV_KEY_RESET+fleet.getId();
        if (!data.containsKey(id))
            data.put(id, 0);

        return (int)data.get(id);
    }

    SectorEntityToken getTarget(CampaignFleetAPI fleet) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = SCAV_KEY+fleet.getId();
        if (!data.containsKey(id))
            data.put(id, randomTargetLocation());

        return (SectorEntityToken)data.get(id);
    }

    void setHome(CampaignFleetAPI fleet, SectorEntityToken home) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = SCAV_KEY_HOME+fleet.getId();
        if (!data.containsKey(id))
            data.put(id, home);
    }

    SectorEntityToken getHome(CampaignFleetAPI fleet) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = SCAV_KEY_HOME+fleet.getId();

        return (SectorEntityToken)data.get(id);
    }

    SectorEntityToken randomTargetLocation(){
        SectorEntityToken target = null;
        StarSystemAPI system = null;
        while (target == null) {
            system = getRandomSystemWithBlacklist();
            if (system == null) {
                //We've somehow blacklisted every system in the sector: just don't spawn anything
                log("ScavengerSpawner ERROR no valid system, system is NULL");
                return null;
            }
            //Gets a list of random locations in the system, and picks one
            WeightedRandomPicker<BaseThemeGenerator.EntityLocation> validPoints = BaseThemeGenerator.getLocations(new Random(), system, 0f, WEIGHTS);
            BaseThemeGenerator.EntityLocation tTarget = validPoints.pick();
            if (tTarget != null && tTarget.orbit != null && tTarget.orbit.getFocus() !=null) {
                target = tTarget.orbit.getFocus();
            } else target = system.getStar();
        }
        return target;
    }

    private static StarSystemAPI getRandomSystemWithBlacklist() {
        //pick tags
        List<String> pickTags = new ArrayList<>();
        pickTags.add(Tags.THEME_REMNANT);
        pickTags.add(Tags.THEME_DERELICT);

        simpleSystem simpleSystem = new simpleSystem(new Random(), 1);
        simpleSystem.pickTags = pickTags;
        simpleSystem.pickOnlyInProcgen = true;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("ERROR no valid system");
        return util.getRandomNonCoreSystem(new Random());
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }
}
