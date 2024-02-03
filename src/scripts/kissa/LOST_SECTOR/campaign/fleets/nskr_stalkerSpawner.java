
package scripts.kissa.LOST_SECTOR.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.customStart.gamemodeManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.fleetInfo;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleFleet;
import scripts.kissa.LOST_SECTOR.campaign.quests.nskr_glacierCommsDialog;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.powerLevel;
import scripts.kissa.LOST_SECTOR.util.util;
import scripts.kissa.LOST_SECTOR.world.systems.frost.nskr_frost;

import java.util.*;

import static scripts.kissa.LOST_SECTOR.campaign.fleets.nskr_hyperspaceEnigmaSpawner.*;

public class nskr_stalkerSpawner extends BaseCampaignEventListener implements EveryFrameScript {
    //
    //spawns groups of fleets that follow the player around by going to the most recently visited system
    //

    public static final float STALKER_TIMER = 240f;
    public static final float DESPAWN_TIMER = 240f;
    public static final float BASE_SIZE_MULT = 6f;
    public static final float MIN_STRENGTH = 15f;
    public static final float MAX_STRENGTH = 100f;
    public static final float MIN_POWER = 0.70f;
    public static final float MAX_POWER = 2.00f;
    public static final float AGGRESSIVE_FRACTION = 0.33f;
    public static final float ENGAGE_CHANCE = 0.10f;
    public static final int baseMinFleets = 1;
    public static final int baseMaxFleets = 1;
    public static final String FLEET_KEY = "$StalkerFleet";
    public static final String FLEET_ARRAY_KEY = "$nskr_stalkerSpawnerFleets";
    public static final String PERSISTENT_FLEET_RANDOM_KEY = "nskr_stalkerSpawnerFleetRandom";
    nskr_saved<Float> spawnCounter;
    nskr_saved<Float> counter;
    nskr_saved<SectorEntityToken> token;
    nskr_saved<Boolean> questSpawn;
    nskr_saved<LinkedList<StarSystemAPI>> locations;
    private final List<CampaignFleetAPI> removed = new ArrayList<>();
    private float threatMult = 0f;
    private boolean updated = false;
    //CampaignFleetAPI pf;
    Random random;

    //Weights for the different types of locations we go to
    public static final LinkedHashMap<BaseThemeGenerator.LocationType, Float> WEIGHTS = new LinkedHashMap<>();
    static {
        WEIGHTS.put(BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT, 4f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.IN_ASTEROID_BELT, 4f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.IN_ASTEROID_FIELD, 4f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 12f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.IN_SMALL_NEBULA, 4f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.JUMP_ORBIT, 12f);
    }
    public final ArrayList<String> edgyNames = new ArrayList<>();
    {
        edgyNames.add("Stalker");
        edgyNames.add("Pursuer");
        edgyNames.add("Hunter");
        edgyNames.add("Vanguard");
        edgyNames.add("Advance");
        edgyNames.add("Spy");
        edgyNames.add("Watcher");
        edgyNames.add("Operative");
        edgyNames.add("Executor");
    }
    static void log(final String message) {
        Global.getLogger(nskr_stalkerSpawner.class).info(message);
    }
    public nskr_stalkerSpawner() {
        super(false);
        //how often stalkers spawn
        this.spawnCounter = new nskr_saved<>("stalkerSpawnCounter", 0.0f);
        //how often we run logic
        this.counter = new nskr_saved<>("stalkerCounter", 0.0f);
        this.locations = new nskr_saved<>("stalkerLocations", new LinkedList<StarSystemAPI>());
        this.token = new nskr_saved<>("stalkerToken", null);
        this.random = new Random();
        //quest
        this.questSpawn = new nskr_saved<>("stalkerQuestSpawn", false);
        //init randoms
        getRandom(PERSISTENT_FLEET_RANDOM_KEY);
    }

    @Override
    public void advance(float amount) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;
        StarSystemAPI target;
        if (locations.val.size() != 0) {
            target = locations.val.get(0);
        } else{
            StarSystemAPI defLoc = Global.getSector().getStarSystem(nskr_frost.getName());
            target = defLoc;
            locations.val.add(defLoc);
        }
        //jank
        //update threat mult
        if (!updated){
            threatMult = getEnigmaThreatScaling(true);
            updated = true;
        }

        if (Global.getSector().isInFastAdvance()) {
            spawnCounter.val += 2f * amount * threatMult;
            counter.val += 2f*amount;
        } else{
            spawnCounter.val += amount * threatMult;
            counter.val += amount;
        }
        //log("threat "+threatMult);

        if (gamemodeManager.getMode() == gamemodeManager.gameMode.HELLSPAWN) return;

        if (counter.val>10f) {
            //update threat mult
            threatMult = getEnigmaThreatScaling(true);

            //target
            //no black holes cause AI real dumb with them
            boolean bh = (pf.getStarSystem() != null && !pf.isInHyperspace() && pf.getStarSystem().getStar() != null && pf.getStarSystem().getStar().getTypeId().equals("black_hole"));
            if (pf.getStarSystem() != null && !pf.isInHyperspace() && !pf.getStarSystem().hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER) && !pf.getStarSystem().hasTag(Tags.THEME_HIDDEN) && !bh){
                locations.val.clear();
                locations.val.add(0,pf.getStarSystem());
            }
            boolean updated = false;
            //go to
            if (token.val == null || token.val.getStarSystem()!=locations.val.get(0)){
                token.val = randomTargetLocation(target);
                updated = true;
                //log("StalkerSpawner NEW TARGET " + token.val.getStarSystem().getName());
            }
            List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
            for (fleetInfo f : fleets) {
                CampaignFleetAPI fleet = f.fleet;
                boolean despawn = false;
                f.age+=1f;

                //cull
                if (f.age>DESPAWN_TIMER){
                    despawn = true;
                }
                if (fleet.getFleetPoints()<MIN_STRENGTH-1f){
                    despawn = true;
                }

                Vector2f fp = fleet.getLocationInHyperspace();
                Vector2f pp = pf.getLocationInHyperspace();
                float dist = MathUtils.getDistance(pp, fp);

                if (despawn){
                    if (dist>Global.getSettings().getMaxSensorRangeHyper()){
                        //tracker for cleaning the list
                        this.removed.add(fleet);
                        fleet.despawn();
                    }
                }
                boolean playerVisible = false;
                if (fleet.getContainingLocation().equals(pf.getContainingLocation())) {
                    playerVisible = pf.isVisibleToSensorsOf(fleet);
                }

                //assignment
                boolean sameSystem = false;
                if (fleet.getStarSystem() != null) {
                    sameSystem = fleet.getStarSystem().getName().equals(token.val.getStarSystem().getName());
                }

                //check whether system is colonised or not
                boolean colonies = false;
                List<SectorEntityToken> entities = new ArrayList<>(1000);
                entities.addAll(token.val.getStarSystem().getAllEntities());
                for (SectorEntityToken ent : entities){
                    if (ent.getMarket() != null){
                        colonies = true;
                        break;
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
                if (sameSystem) {
                    //intercept logic yoinked from Nex
                    //chance to engage player every tick, otherwise just lurk around
                    if (Math.random() < ENGAGE_CHANCE) {
                        if (playerVisible) {
                            if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.INTERCEPT) {
                                fleet.clearAssignments();
                                fleet.addAssignment(FleetAssignment.INTERCEPT, pf, Float.MAX_VALUE, "unknown");
                                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);
                                log("StalkerSpawner " + fleet.getName() + " INTERCEPTING ");
                            }
                        } else {
                            if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.ATTACK_LOCATION) {
                                fleet.clearAssignments();
                                fleet.addAssignment(FleetAssignment.ATTACK_LOCATION, token.val, Float.MAX_VALUE, "unknown");
                                ((ModularFleetAIAPI) fleet.getAI()).getTacticalModule().setPriorityTarget(pf, Float.MAX_VALUE, false);
                                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);
                                log("StalkerSpawner " + fleet.getName() + " ATTACKING ");
                            }
                        }
                    } else if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.INTERCEPT && fleet.getAI().getCurrentAssignmentType() != FleetAssignment.ATTACK_LOCATION && colonies) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.RAID_SYSTEM, token.val, Float.MAX_VALUE, "unknown");
                        log("StalkerSpawner " + fleet.getName() + " RAIDING IN " + token.val.getStarSystem().getName());
                    } else if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.INTERCEPT && fleet.getAI().getCurrentAssignmentType() != FleetAssignment.ATTACK_LOCATION && !colonies) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, token.val, Float.MAX_VALUE, "unknown");
                        log("StalkerSpawner " + fleet.getName() + " FIGHTING IN " + token.val.getStarSystem().getName());
                    }
                } else if (updated) {
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, token.val, Float.MAX_VALUE, "unknown");
                    log("StalkerSpawner " + fleet.getName() + " MOVING TO " + token.val.getStarSystem().getName());
                }
            }
            //clean the list
            fleetUtil.cleanUp(removed, fleets);
            removed.clear();
            //save to mem
            fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

            counter.val = 0f;
        }

        //for kesteven quest line
        if (questUtil.getCompleted(nskr_glacierCommsDialog.RECOVERED_KEY) && !questSpawn.val){
            float power = powerLevel.get(0.2f, 0f, MAX_POWER);
            spawnStalkerFleets(3, power, getRandom(PERSISTENT_FLEET_RANDOM_KEY));
            questSpawn.val = true;
            log("StalkerSpawner SPAWNED quest fleets");
        }

        //don't spawn when enigma is gone
        if (!util.enigmaExists()) return;

        //10 = 1 day
        if (spawnCounter.val>STALKER_TIMER*10f) {
            spawnCounter.val = 0f;
            //don't spawn if player too weak
            float power = powerLevel.get(0.2f, MIN_POWER, MAX_POWER);
            if (power<MIN_POWER) return;

            int maxFleets = baseMaxFleets;
            if(power>1.00f){
                maxFleets += 1;
            }if(power>1.33f){
                maxFleets += 1;
            } if (power>1.67f){
                maxFleets += 1;
            }

            this.spawnStalkerFleets(maxFleets, power, getRandom(PERSISTENT_FLEET_RANDOM_KEY));
        }
    }

    public static float getEnigmaThreatScaling(boolean stalker) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        float scale = 0.5f;
        float protDP = 0f;
        float protOP = 0f;
        float minDP = 16f;
        float minOP = 60f;
        for (FleetMemberAPI m : pf.getMembersWithFightersCopy()){
            if (m.isFighterWing()) continue;
            if (m.getVariant()==null) continue;
            if (m.getHullSpec()==null) continue;
            if (util.isProtTech(m)){
                protDP += m.getDeploymentPointsCost();
            }
            for (String wep : m.getVariant().getNonBuiltInWeaponSlots()) {
                WeaponSpecAPI weapon = m.getVariant().getWeaponSpec(wep);
                if (weapon == null) continue;
                if (!weapon.hasTag("enigma")) continue;
                protOP += weapon.getOrdnancePointCost(Global.getSector().getPlayerStats());
            }
        }
        if (stalker){
            if (protDP<=minDP && protOP<=minOP){
                //log("return 0 "+ " dp "+protDP+ " op "+protOP);
                return 0f;
            }
        }
        //log("protDP "+(int)protDP+" protOP "+(int)protOP);
        //dp
        protDP = Math.min(protDP, 150f);
        protDP = mathUtil.normalize(protDP, 0f, 150f);
        protDP = mathUtil.lerp(0f,0.90f, protDP);
        //op
        protOP = Math.min(protOP, 250f);
        protOP = mathUtil.normalize(protOP, 0f, 250f);
        protOP = mathUtil.lerp(0f,0.40f, protOP);
        //apply
        scale += protDP+protOP;
        //log("scale "+scale+" protDP "+protDP+" protOP "+protOP);
        return scale;
    }

    void spawnStalkerFleets(final Integer maxAmount, float power, Random random) {
        MarketAPI market = Global.getSector().getEconomy().getMarket("nskr_heart");
        if (market == null) return;
        StarSystemAPI home = market.getStarSystem();
        if (home == null) return;

        //pick level
        fleetLevel lvl = getFleetLevel(power);
        //don't do this
        power = Math.min(1.5f, power);

        int amount = mathUtil.getSeededRandomNumberInRange(baseMinFleets, maxAmount, random);
        for (int x = 0; x < amount; x++) {
            float combatPoints = mathUtil.getSeededRandomNumberInRange(2, 14, random);
            log("StalkerSpawner BASE " + combatPoints);
            //exponential scaling of random enigma fleet size
            float bias = 0.25f+(mathUtil.BiasFunction(random.nextDouble(), 0.33f));
            combatPoints *= bias;

            String type = "patrolSmall";
            if (combatPoints > 3) {
                type = "patrolMedium";
            }
            if (combatPoints > 6) {
                type = "patrolLarge";
            }
            combatPoints *= BASE_SIZE_MULT;

            combatPoints *= power;

            if (amount>=2 && !nskr_modPlugin.getStarfarerMode()) combatPoints *= 0.75f;

            if (combatPoints < MIN_STRENGTH) combatPoints = MIN_STRENGTH;
            if (combatPoints > MAX_STRENGTH) combatPoints = MAX_STRENGTH;

            //apply settings
            combatPoints *= nskr_modPlugin.getRandomEnigmaFleetSizeMult();
            if (combatPoints<=0f) return;

            ArrayList<String> keys = new ArrayList<>();
            //makes fleet aggressive
            if (Math.random()<AGGRESSIVE_FRACTION){
                keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
            } else {
                //passive
                keys.add(MemFlags.MEMORY_KEY_IGNORE_PLAYER_COMMS);
            }
            //keys.add(MemFlags.CAN_ONLY_BE_ENGAGED_WHEN_VISIBLE_TO_PLAYER);
            //keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
            keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
            keys.add(MemFlags.MEMORY_KEY_FORCE_TRANSPONDER_OFF);
            keys.add(FLEET_KEY);

            simpleFleet simpleFleet = new simpleFleet(market.getPrimaryEntity(), "enigma", combatPoints, keys, random);
            //simpleFleet.type = type;
            simpleFleet.aiFleetProperties = true;

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

            if (random.nextFloat()<0.33f) simpleFleet.sMods = mathUtil.getSeededRandomNumberInRange(1,2, random);
            if (random.nextFloat()<0.15f) simpleFleet.qualityOverride = mathUtil.getSeededRandomNumberInRange(0.60f, 0.90f, random);
            if (random.nextFloat()<0.50f) simpleFleet.noTransponder = true;
            simpleFleet.name = edgyNames.get(mathUtil.getSeededRandomNumberInRange(0, edgyNames.size() - 1, random));
            simpleFleet.assignment = FleetAssignment.ORBIT_PASSIVE;
            simpleFleet.assignmentText = "unknown";
            CampaignFleetAPI fleet = simpleFleet.create();

            //add to mem IMPORTANT
            List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
            fleets.add(new fleetInfo(fleet, null, market.getPrimaryEntity()));
            fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

            log("StalkerSpawner SPAWNED " + fleet.getName() + " size " + combatPoints);
        }
    }

    SectorEntityToken randomTargetLocation(StarSystemAPI system){
        SectorEntityToken target = null;
        while (target == null) {
            //Gets a list of random locations in the system, and picks one
            WeightedRandomPicker<BaseThemeGenerator.EntityLocation> validPoints = BaseThemeGenerator.getLocations(new Random(), system, 50f, WEIGHTS);
            BaseThemeGenerator.EntityLocation tTarget = validPoints.pick();
            if (tTarget != null && tTarget.orbit != null && tTarget.orbit.getFocus() !=null) {
                target = tTarget.orbit.getFocus();
            } else target = system.getStar();
        }
        return target;
    }

    //for delaying new stalker spawns, when they get killed by player
    @Override
    public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {
        CampaignFleetAPI loser = plugin.getLoser();
        if (loser == null) return;

        if (loser.getMemoryWithoutUpdate().contains(FLEET_KEY)) {
            List<FleetEncounterContextPlugin.FleetMemberData> casualties = plugin.getLoserData().getOwnCasualties();
            for (FleetEncounterContextPlugin.FleetMemberData memberData : casualties) {
                FleetEncounterContextPlugin.Status status = memberData.getStatus();
                if (status == FleetEncounterContextPlugin.Status.NORMAL) continue;

                float contrib = plugin.computePlayerContribFraction();
                spawnCounter.val -= (6f * contrib) * 10f;
            }
            log("StalkerSpawner Fleet destroyed, delayed next spawn, time elapsed " + spawnCounter.val / 10f + " out of " + STALKER_TIMER);
        }
    }

    //@Override
    //public void reportPlayerEngagement(EngagementResultAPI result) {
    //    if (result.getLoserResult()==null) return;
    //    CampaignFleetAPI loser = result.getLoserResult().getFleet();
    //    if (loser == null) return;
    //    for (String k : loser.getMemoryWithoutUpdate().getKeys()){
    //        log("key r "+k);
    //    }
    //    if (loser.getMemoryWithoutUpdate().contains(FLEET_KEY)) {
    //        List<FleetMemberAPI> casualties = result.getLoserResult().getDisabled();
    //        spawnCounter.val -= 4f * 10f;
    //
    //        log("StalkerSpawner Fleet destroyed, delayed next spawn, time elapsed " + spawnCounter.val / 10f + " out of " + STALKER_TIMER);
    //    }
    //}

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
