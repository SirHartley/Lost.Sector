package scripts.kissa.LOST_SECTOR.campaign.fleets.events;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.loading.VariantSource;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_abyssSpawner;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.fleetInfo;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleFleet;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.powerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class nskr_interceptManager extends BaseCampaignEventListener implements EveryFrameScript  {

    //spawns and manages fleets that try to intercept the player
    //
    //TT-collector and loanShark in different classes, for now

    //fleet variables
    public static final float ARO_SPAWN_CHANCE = 0.01f;
    public static final float ARO_DESPAWN_TIMER = 45f;
    public static final float MESSENGER_SPAWN_CHANCE  = 0.04f;
    public static final float MESSENGER_DESPAWN_TIMER = 20f;
    public static final float AUTO_HUNTER_SPAWN_CHANCE  = 0.01f;
    public static final float AUTO_HUNTER_DP_REQ  = 75f;

    public static final float IN_CORE_DIST = 25000f;
    public static final String FLEET_ARRAY_KEY = "$nskr_interceptManagerFleets";
    public static final String PERSISTENT_RANDOM_KEY = "nskr_interceptManagerRandomKey";
    public static final String PERSISTENT_FLEET_RANDOM_KEY = "nskr_interceptManagerKeyFleetRandom";
    public static final String ARO_FLEET_KEY = "$nskr_interceptManagerAROfleet";
    public static final String MESSENGER_FLEET_KEY = "$nskr_interceptManagerMessengerFleet";
    public static final String MESSENGER_FLEET_TALKED_KEY = "$nskr_interceptManagerMessengerTalked";
    public static final String AUTO_HUNTER_FLEET_KEY = "$nskr_interceptManagerAutoHunterFleet";
    private final List<CampaignFleetAPI> removed = new ArrayList<>();
    nskr_saved<Float> counter;
    nskr_saved<Float> fleetCounter;
    nskr_saved<Boolean> aroFleetSpawned;
    nskr_saved<Boolean> messengerFleetSpawned;
    nskr_saved<Boolean> autoHunterFleetSpawned;
    //CampaignFleetAPI pf;
    public nskr_interceptManager() {
        super(false);
        //logic timer
        this.counter = new nskr_saved<>("nskr_interceptManagerTimer", 0f);
        this.fleetCounter = new nskr_saved<>("nskr_interceptManagerFleetTimer", 0f);
        //spawn check booleans
        this.aroFleetSpawned = new nskr_saved<>("nskr_interceptManagerAROFleetSpawned", false);
        this.messengerFleetSpawned = new nskr_saved<>("nskr_interceptManagerMessengerFleetSpawned", false);
        this.autoHunterFleetSpawned = new nskr_saved<>("nskr_interceptManagerAutoHunterFleetSpawned", false);
        //init randoms
        getRandom(PERSISTENT_RANDOM_KEY);
        getRandom(PERSISTENT_FLEET_RANDOM_KEY);
    }

    static void log(final String message) {
        Global.getLogger(nskr_interceptManager.class).info(message);
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }
    @Override
    public void advance(float amount) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf==null)return;

        if (Global.getSector().isInFastAdvance()) {
            counter.val += 2f*amount;
            fleetCounter.val += 2f*amount;
        } else{
            counter.val += amount;
            fleetCounter.val += amount;
        }
        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);

        //once a day
        if (counter.val>10f){
            //don't do vals everyFrame
            float distance = pf.getLocationInHyperspace().length();
            boolean hyperSpace = pf.isInHyperspace();
            Random random = getRandom(PERSISTENT_RANDOM_KEY);
            int stage = questUtil.getStage();

            //SPAWNING LOGIC
            //ARO FLEET
            if (!aroFleetSpawned.val){
                //spawn check
                if (nskr_abyssSpawner.hasBountyShips(pf) && hyperSpace && distance < IN_CORE_DIST) {
                    //rng check
                    if (random.nextFloat()<ARO_SPAWN_CHANCE) {
                        spawnAROfleet(getRandom(PERSISTENT_FLEET_RANDOM_KEY));
                        aroFleetSpawned.val = true;
                    }
                }
            }
            //MESSENGER
            if (!messengerFleetSpawned.val){
                //spawn check
                if (stage>=10 && stage<=14 && hyperSpace && distance < IN_CORE_DIST) {
                    //rng check
                    if (random.nextFloat()<MESSENGER_SPAWN_CHANCE) {
                        spawnMessengerFleet(getRandom(PERSISTENT_FLEET_RANDOM_KEY));
                        messengerFleetSpawned.val = true;
                    }
                }
            }
            //AUTO HUNTER
            if (!autoHunterFleetSpawned.val){
                //spawn check
                if (hyperSpace && distance < IN_CORE_DIST*2f && AutoHunterCanSpawn() && Global.getSector().getFaction(Factions.PLAYER).getRelationship(Factions.LUDDIC_PATH)<0f) {
                    //rng check
                    if (random.nextFloat()<AUTO_HUNTER_SPAWN_CHANCE) {
                        spawnAutoHunterFleet(getRandom(PERSISTENT_FLEET_RANDOM_KEY));
                        autoHunterFleetSpawned.val = true;
                    }
                }
            }

            //
            counter.val=0f;
        }

        //10x a day
        if (fleetCounter.val>10f) {
            //FLEET LOGIC
            Random random = getRandom(PERSISTENT_RANDOM_KEY);
            for (fleetInfo f : fleets) {
                CampaignFleetAPI fleet = f.fleet;
                //timer
                f.age+=1f;

                //ARO fleet
                if (fleet.getMemoryWithoutUpdate().contains(ARO_FLEET_KEY)){
                    boolean despawn = false;

                    if (fleet.getFleetPoints()*4.0f<f.strength){
                        despawn = true;
                    }
                    if (f.age>ARO_DESPAWN_TIMER){
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

                    //intercept
                    fleetUtil.gotoAndInterceptPlayerAI(fleet, f, fleetUtil.interceptBehaviour.AROUND);
                }
                //MESSENGER fleet
                if (fleet.getMemoryWithoutUpdate().contains(MESSENGER_FLEET_KEY)){
                    boolean despawn = false;
                    float dist = MathUtils.getDistance(fleet.getLocationInHyperspace(), pf.getLocationInHyperspace());

                    boolean talked = fleet.getMemoryWithoutUpdate().contains(MESSENGER_FLEET_TALKED_KEY);
                    //mem key for other quest dialog
                    if (talked && !questUtil.getCompleted(questStageManager.E_MESSENGER_TALKED_KEY)){
                        questUtil.setCompleted(true ,questStageManager.E_MESSENGER_TALKED_KEY);
                        //hack
                        questUtil.setCompleted(true ,questStageManager.E_MESSENGER_TALKED_ASK_ABOUT_KEY);
                    }

                    if (fleet.getFleetPoints()*4.0f<f.strength){
                        despawn = true;
                    }
                    if (f.age>MESSENGER_DESPAWN_TIMER){
                        despawn = true;
                    }
                    //
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

                    //intercept
                    if (!talked) {
                        fleetUtil.gotoAndInterceptPlayerAI(fleet, f, fleetUtil.interceptBehaviour.DIRECT);
                    } // go back
                    else {
                        if (fleet.getAI().getCurrentAssignmentType()!=FleetAssignment.GO_TO_LOCATION_AND_DESPAWN) {
                            SectorEntityToken target = questUtil.getRandomFactionMarket(random, Factions.PIRATES);
                            fleet.clearAssignments();
                            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, target, Float.MAX_VALUE, "returning to "+target.getMarket().getName());
                        }
                    }
                }
                //Auto hunter fleet
                if (fleet.getMemoryWithoutUpdate().contains(AUTO_HUNTER_FLEET_KEY)){
                    boolean despawn = false;

                    if (fleet.getFleetPoints()*4.0f<f.strength){
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

                    //intercept
                    //chase the player for 30 days and if not defeated, then give up and guard a random path market
                    if (f.age<30f){
                        fleetUtil.gotoAndInterceptPlayerAI(fleet, f, fleetUtil.interceptBehaviour.AROUND);
                    } else {
                        //filthy check to pick new target once
                        if (f.target.getMarket()==null){
                            f.target = questUtil.getRandomFactionMarket(random, Factions.LUDDIC_PATH);
                        }
                        fleetUtil.guardTargetAI(fleet, f, fleetUtil.guardMovementBehaviour.ORBIT, fleetUtil.guardAttackBehaviour.PLAYER, 0.01f);
                    }

                }

            }

            //clean the list
            fleetUtil.cleanUp(removed, fleets);
            removed.clear();
            //save to mem
            fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

            //update
            fleetCounter.val=0f;
        }
    }

    public static final String ARO_FLEET_NAME = "ARO Strike Group";
    public void spawnAROfleet(Random random) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();

        float combatPoints = mathUtil.getSeededRandomNumberInRange(110f, 130f, random);
        //power scaling
        combatPoints += combatPoints* powerLevel.get(0.2f, 0f,2f);

        //apply settings
        combatPoints *= nskr_modPlugin.getScriptedFleetSizeMult();

        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOSTILE);
        keys.add(MemFlags.MEMORY_KEY_NO_REP_IMPACT);
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);
        keys.add(ARO_FLEET_KEY);

        simpleFleet simpleFleet = new simpleFleet(pf.getContainingLocation().createToken(pf.getLocation()), Factions.LUDDIC_CHURCH, combatPoints, keys, random);
        //simpleFleet.type = FleetTypes.PATROL_LARGE;
        simpleFleet.maxShipSize = 4;
        simpleFleet.sMods = mathUtil.getSeededRandomNumberInRange(2,3, random);
        simpleFleet.name = ARO_FLEET_NAME;
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.noFactionInName = true;
        simpleFleet.assignment = FleetAssignment.INTERCEPT;
        simpleFleet.assignmentText = "intercepting your fleet";
        simpleFleet.interceptPlayer = true;
        CampaignFleetAPI fleet = simpleFleet.create();

        //spawning
        final Vector2f loc = new Vector2f(MathUtils.getPointOnCircumference(pf.getLocation(), (pf.getSensorStrength()*0.90f)+(fleet.getSensorProfile()*0.90f), random.nextFloat() * 360.0f));
        fleet.setLocation(loc.x, loc.y);
        fleet.setFacing(random.nextFloat() * 360.0f);

        fleet.setFaction(Factions.MERCENARY, true);
        //update
        fleetUtil.update(fleet, random);

        //add to mem IMPORTANT
        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
        fleets.add(new fleetInfo(fleet, null, fleet.getContainingLocation().createToken(fleet.getLocation())));
        fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

        log("ARO fleet SPAWNED " + fleet.getName() + " size " + combatPoints);
    }

    public static final String MESSENGER_FLEET_NAME = "Merc Messenger";
    public void spawnMessengerFleet(Random random) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();

        float combatPoints = mathUtil.getSeededRandomNumberInRange(50f, 70f, random);
        //power scaling
        combatPoints += combatPoints* powerLevel.get(0.2f, 0f,2f);

        //apply settings
        combatPoints *= nskr_modPlugin.getScriptedFleetSizeMult();

        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);
        keys.add(MESSENGER_FLEET_KEY);

        simpleFleet simpleFleet = new simpleFleet(pf.getContainingLocation().createToken(pf.getLocation()), Factions.PIRATES, combatPoints, keys, random);
        //simpleFleet.type = FleetTypes.PATROL_MEDIUM;
        simpleFleet.maxShipSize = 3;
        simpleFleet.name = MESSENGER_FLEET_NAME;
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.assignment = FleetAssignment.INTERCEPT;
        simpleFleet.assignmentText = "intercepting your fleet";
        simpleFleet.interceptPlayer = true;
        CampaignFleetAPI fleet = simpleFleet.create();

        //spawning
        final Vector2f loc = new Vector2f(MathUtils.getPointOnCircumference(pf.getLocation(), (pf.getSensorStrength()*0.90f)+(fleet.getSensorProfile()*0.90f), random.nextFloat() * 360.0f));
        fleet.setLocation(loc.x, loc.y);
        fleet.setFacing(random.nextFloat() * 360.0f);

        fleet.setFaction(Factions.MERCENARY, true);
        //update
        fleetUtil.update(fleet, random);

        //add to mem IMPORTANT
        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
        fleets.add(new fleetInfo(fleet, null, fleet.getContainingLocation().createToken(fleet.getLocation())));
        fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

        log("Messenger SPAWNED " + fleet.getName() + " size " + combatPoints);
    }

    public static final String AUTO_HUNTER_FLEET_NAME = "Hunter Fanatics";
    public void spawnAutoHunterFleet(Random random) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();

        float combatPoints = mathUtil.getSeededRandomNumberInRange(70f, 80f, random);
        //power scaling
        combatPoints += combatPoints * powerLevel.get(0.2f, 0f,1.5f);

        //apply settings
        combatPoints *= nskr_modPlugin.getScriptedFleetSizeMult();

        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);
        keys.add(AUTO_HUNTER_FLEET_KEY);

        simpleFleet simpleFleet = new simpleFleet(pf.getContainingLocation().createToken(pf.getLocation()), Factions.LUDDIC_PATH, combatPoints, keys, random);

        simpleFleet.name = AUTO_HUNTER_FLEET_NAME;
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.qualityOverride = mathUtil.getSeededRandomNumberInRange(0.70f, 0.80f, random);
        simpleFleet.sMods = mathUtil.getSeededRandomNumberInRange(1, 2, random);
        simpleFleet.assignment = FleetAssignment.INTERCEPT;
        simpleFleet.assignmentText = "intercepting your fleet";
        simpleFleet.interceptPlayer = true;
        CampaignFleetAPI fleet = simpleFleet.create();

        //spawning
        final Vector2f loc = new Vector2f(MathUtils.getPointOnCircumference(pf.getLocation(), (pf.getSensorStrength()*0.90f)+(fleet.getSensorProfile()*0.90f), random.nextFloat() * 360.0f));
        fleet.setLocation(loc.x, loc.y);
        fleet.setFacing(random.nextFloat() * 360.0f);

        //ADD HULLMODS
        for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
            if (curr.isFighterWing()) continue;
            ShipVariantAPI v = curr.getVariant();
            if (v==null) continue;

            //random and flagship
            if (random.nextFloat()<0.50f || curr.isFlagship()){
                v.addPermaMod("nskr_machineSpirit");
                v.addTag(Tags.TAG_NO_AUTOFIT);
                if (curr.isFlagship()) v.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
            }
        }

        //fleet.setFaction(Factions.LUDDIC_PATH, true);
        //update
        fleetUtil.update(fleet, random);

        //add to mem IMPORTANT
        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
        fleets.add(new fleetInfo(fleet, null, fleet.getContainingLocation().createToken(fleet.getLocation())));
        fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

        log("Auto Hunter SPAWNED " + fleet.getName() + " size " + combatPoints);
    }

    public boolean AutoHunterCanSpawn() {

        float autoCount = 0f;
        for (FleetMemberAPI m : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
            if (m.isFighterWing()) continue;
            if (m.getVariant() == null) continue;
            if (m.getVariant().getHullMods().contains(HullMods.AUTOMATED) || m.getVariant().getHullMods().contains("sotf_sierrasconcord")) {

                autoCount += m.getDeploymentPointsCost();
            }
        }
        return autoCount >= AUTO_HUNTER_DP_REQ;
    }

    public static Random getRandom(String id) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) {
            data.put(id, new Random(new Random().nextLong()));
        }
        return (Random)data.get(id);
    }
}
