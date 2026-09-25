package lostsector.helper;

import lostsector.campaign.enigma.GuardSpawner;
import lostsector.campaign.enigma.HyperspaceEnigmaSpawner;
import lostsector.campaign.enigma.StalkerSpawner;
import lostsector.campaign.kesteven.BlackOpsManager;
import lostsector.campaign.kesteven.KestevenScavenger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import lostsector.helper.fleet.FleetInfo;
import lostsector.campaign.kesteven.quest.QuestStageManager;
import lostsector.helper.fleet.SimpleFleetMember;
import lostsector.quest.QuestFleets;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class FleetHelper {

    static void log(final String message) {
        Global.getLogger(FleetHelper.class).info(message);
    }

    public static List<FleetInfo> getFleets(String id) {
        MemoryAPI mem = Global.getSector().getMemory();
        if (mem.contains(id)){
            return (List<FleetInfo>)mem.get(id);
        } else {
            mem.set(id, new ArrayList<FleetInfo>());
        }
        return (List<FleetInfo>)mem.get(id);
    }

    public static List<FleetInfo> setFleets(List<FleetInfo> fleets, String id) {
        MemoryAPI mem = Global.getSector().getMemory();
        mem.set(id, fleets);
        return (List<FleetInfo>) mem.get(id);
    }

    public static void cleanUp(List<CampaignFleetAPI> toRemove, List<FleetInfo> fleets) {
        for (Iterator<FleetInfo> iter = fleets.listIterator(); iter.hasNext();) {
            CampaignFleetAPI a = iter.next().fleet;
            if (a == null) {
                iter.remove();
            } else if (toRemove.contains(a)) {
                log("REMOVED " + a.getName());
                iter.remove();
            }
        }
    }


    public static void update(CampaignFleetAPI fleet, Random random){

        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
            //IMPORTANT set id or random stuff breaks
            if (!m.getId().startsWith("nskr_")) m.setId("nskr_"+m.getShipName()+random.nextLong());
            if (m.isFighterWing()) continue;
            ShipVariantAPI v = m.getVariant();
            //clone
            for (String tag : m.getVariant().getTags()){
                v.addTag(tag);
            }
            //keep Smods for all ships
            v.addTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
            v.addTag(Tags.VARIANT_ALWAYS_RETAIN_SMODS_ON_SALVAGE);
            //CR update
            m.getRepairTracker().setCR(m.getRepairTracker().getMaxCR());

            v.setSource(VariantSource.REFIT);
            m.setVariant(v, false, false);
        }

        //FINISHING
        fleet.getFleetData().sort();
        fleet.getFleetData().setSyncNeeded();
        fleet.getFleetData().syncIfNeeded();
    }

    public static void updatePlayerFleet(boolean withSort){

        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        for (FleetMemberAPI m : pf.getMembersWithFightersCopy()) {
            if (m.isFighterWing()) continue;

            m.getVariant().setSource(VariantSource.REFIT);
            m.setVariant(m.getVariant(), false, false);
        }

        //FINISHING
        if (withSort) pf.getFleetData().sort();

    }

    private static void safetyCheck(CampaignFleetAPI fleet, FleetAssignmentDataAPI curr) {
        if (curr == null) {
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.HOLD, fleet.getContainingLocation().createToken(fleet.getLocation()), Float.MAX_VALUE, "holding");
            log("null assignment");
        }
    }
    private static boolean specManeuversCheck(CampaignFleetAPI fleet, CampaignFleetAPI pf, FleetAssignmentDataAPI curr) {
        if (curr !=null && curr.getAssignment()==FleetAssignment.STANDING_DOWN) {
            CampaignFleetAIAPI ai = fleet.getAI();
            if (ai instanceof ModularFleetAIAPI) {
                // needed to interrupt an in-progress pursuit
                ModularFleetAIAPI m = (ModularFleetAIAPI) ai;
                m.getStrategicModule().getDoNotAttack().add(pf, 1f);
                m.getTacticalModule().setTarget(null);
                return true;
            }
        }
        return false;
    }
    private static boolean locationCheck(CampaignFleetAPI pf) {
        if (!pf.isInHyperspace()){
            if (pf.getStarSystem()==null || pf.getStarSystem().getCenter()==null || pf.getContainingLocation()==null){
                log("ERROR pf in null location");
                return true;
            }
        }
        return false;
    }
    private static boolean defeatedCheck(CampaignFleetAPI fleet, FleetInfo info, CampaignFleetAPI pf) {
        if (fleet.getFlagship()==null){
            if (fleet.getFleetPoints()< info.strength*0.25f) {
                if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.ORBIT_PASSIVE) {
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, pf.getContainingLocation().createToken(pf.getLocation()), Float.MAX_VALUE, "standing down");
                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_AVOID_PLAYER_SLOWLY, true);
                    log("standing down, loc " + fleet.getContainingLocation().getName());
                }
                return true;
            }
        }
        return false;
    }
    public static void gotoAndInterceptPlayerAI(CampaignFleetAPI fleet, FleetInfo info, InterceptBehaviour behaviour){
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        boolean playerVisible = false;
        if (fleet.getContainingLocation()==pf.getContainingLocation()) {
            playerVisible = pf.isVisibleToSensorsOf(fleet);
        }
        if (fleet.getAI()==null) return;
        //assignment logic
        FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
        //safety
        safetyCheck(fleet, curr);
        //used special maneuvers
        if (specManeuversCheck(fleet, pf, curr)) return;
        //bad location
        if (locationCheck(pf)) return;
        //stand down after defeat
        if (defeatedCheck(fleet, info, pf)) return;

        //pick type
        switch (behaviour){
            case DIRECT:
                //add some "leading" to compensate for low tickrate
                Vector2f predictedLocation = Vector2f.add(pf.getLocation(), pf.getVelocity(), null);
                //TRAVEL HYPER OR SAME SYSTEM
                if(fleet.getContainingLocation()==pf.getContainingLocation() && !playerVisible || !fleet.isInHyperspace() && pf.isInHyperspace()){
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, pf.getContainingLocation().createToken(predictedLocation), Float.MAX_VALUE, "looking for your fleet");
                    log("looking, loc " + fleet.getContainingLocation().getName());
                    return;
                }
                //TRAVEL TO DIFFERENT SYS
                if(fleet.getContainingLocation()!=pf.getContainingLocation() && !pf.isInHyperspace()){
                    if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.GO_TO_LOCATION && !pf.getStarSystem().hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER) && !pf.getStarSystem().hasTag(Tags.THEME_HIDDEN)){
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, pf.getContainingLocation().createToken(predictedLocation), Float.MAX_VALUE, "looking for your fleet");
                        log("travel to star system, loc " + fleet.getContainingLocation().getName());
                        return;
                    }
                }
                //INTERCEPT
                if (fleet.getContainingLocation()==pf.getContainingLocation() && playerVisible) {
                    if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.INTERCEPT){
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.INTERCEPT, pf, Float.MAX_VALUE, "intercepting for your fleet");
                        log("intercepting, loc " + fleet.getContainingLocation().getName());
                        return;
                    }
                }

                break;
            case AROUND:
                //set
                if (info.target==null) {
                    createAroundInterceptTarget(info, pf);
                }
                //reached, create a new one
                else if (MathUtils.getDistance(fleet.getLocation(), info.target.getLocation()) < 300f){
                    createAroundInterceptTarget(info, pf);
                }

                //TRAVEL HYPER OR SAME SYSTEM
                if(fleet.getContainingLocation()==pf.getContainingLocation() && !playerVisible || !fleet.isInHyperspace() && pf.isInHyperspace()){
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, info.target, Float.MAX_VALUE, "looking for your fleet");
                    log("looking, loc " + fleet.getContainingLocation().getName());
                    return;
                }
                //TRAVEL TO DIFFERENT SYS
                if(fleet.getContainingLocation()!=pf.getContainingLocation() && !pf.isInHyperspace()){
                    if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.GO_TO_LOCATION && !pf.getStarSystem().hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER) && !pf.getStarSystem().hasTag(Tags.THEME_HIDDEN)){
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, info.target, Float.MAX_VALUE, "looking for your fleet");
                        log("travel to star system, loc " + fleet.getContainingLocation().getName());
                        return;
                    }
                }
                //INTERCEPT
                if (fleet.getContainingLocation()==pf.getContainingLocation() && playerVisible) {
                    if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.INTERCEPT){
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.INTERCEPT, pf, Float.MAX_VALUE, "intercepting for your fleet");
                        log("intercepting, loc " + fleet.getContainingLocation().getName());
                        return;
                    }
                }

                break;
        }
    }

    public static final float AROUND_ERROR = 4000f;
    private static void createAroundInterceptTarget(FleetInfo info, CampaignFleetAPI pf) {
        //clean up
        if (info.target!=null) info.target.setExpired(true);

        Vector2f aroundPlayer = new Vector2f(0f, 0f);
        aroundPlayer.setX(pf.getLocation().getX() + MathUtils.getRandomNumberInRange(-AROUND_ERROR, AROUND_ERROR));
        aroundPlayer.setY(pf.getLocation().getY() + MathUtils.getRandomNumberInRange(-AROUND_ERROR, AROUND_ERROR));

        info.target = pf.getContainingLocation().createToken(aroundPlayer);
    }
    public enum InterceptBehaviour {
        DIRECT,
        AROUND
    }


    public static void guardTargetAI(CampaignFleetAPI fleet, FleetInfo info, GuardMovementBehaviour movementBehaviour, GuardAttackBehaviour attackBehaviour, float playerInterceptChance) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        boolean playerVisible = false;
        if (fleet.getContainingLocation() == pf.getContainingLocation()) {
            playerVisible = pf.isVisibleToSensorsOf(fleet);
        }
        if (fleet.getAI() == null) return;
        //assignment logic
        FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
        //safety
        safetyCheck(fleet, curr);
        //used special maneuvers
        if (specManeuversCheck(fleet, pf, curr)) return;
        //bad location
        if (locationCheck(pf)) return;
        //stand down after defeat
        if (defeatedCheck(fleet, info, pf)) return;

        //
        //set
        if (info.target==null) {
            createGuardTarget(info);
        }
        //close enough check
        //MOVE AI
        if (MathUtils.getDistance(fleet.getLocation(), info.target.getLocation()) >= 1500f && fleet.getAI().getCurrentAssignmentType() != FleetAssignment.INTERCEPT){
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, info.target, Float.MAX_VALUE, "moving to location");
        } else {
            //ATTACK AI
            if (playerVisible && Math.random()<playerInterceptChance){
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
                //make visible to other fleets
                fleet.getMemoryWithoutUpdate().unset(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
                fleet.getMemoryWithoutUpdate().unset(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);

                fleet.clearAssignments();
                fleet.addAssignment(FleetAssignment.INTERCEPT, pf, Float.MAX_VALUE, "intercepting your fleet");
            }
            else if (attackBehaviour == GuardAttackBehaviour.HOSTILE){
                for (CampaignFleetAPI e : Misc.getVisibleFleets(fleet, false)){
                    if (!fleet.getFaction().isHostileTo(e.getFaction())) continue;
                    //make visible to other fleets
                    fleet.getMemoryWithoutUpdate().unset(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
                    fleet.getMemoryWithoutUpdate().unset(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.INTERCEPT, e, Float.MAX_VALUE, "intercepting fleet");
                    break;
                }
            }
            //PASSIVE AI
            if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.INTERCEPT) {
                switch (movementBehaviour) {
                    case HOLD:
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.HOLD, info.target, Float.MAX_VALUE, "laying in wait");
                        fleet.setTransponderOn(false);
                        break;
                    case ORBIT:
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, info.target, Float.MAX_VALUE, "guarding location");
                        fleet.setTransponderOn(false);
                        break;
                }
            }
        }
    }
    // The guard point is where the fleet is now, in its own location, which need not be the player's.
    private static void createGuardTarget(FleetInfo info) {
        //clean up
        if (info.target!=null) info.target.setExpired(true);

        info.target = info.fleet.getContainingLocation().createToken(info.fleet.getLocation());
    }
    public enum GuardMovementBehaviour {
        HOLD,
        ORBIT
    }
    public enum GuardAttackBehaviour {
        HOSTILE,
        PLAYER
    }

    // Heads for info.target and despawns there. The assignment is issued again whenever the fleet has another one.
    public static void goToTargetAndDespawnAI(CampaignFleetAPI fleet, FleetInfo info) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (fleet.getAI() == null || info.target == null) return;
        specManeuversCheck(fleet, pf, fleet.getAI().getCurrentAssignment());
        if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.GO_TO_LOCATION_AND_DESPAWN) {
            String name = info.target.getMarket() != null ? info.target.getMarket().getName() : info.target.getName();
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, info.target, Float.MAX_VALUE, "returning to " + name);
        }
    }

    // Below a quarter of the fleet points it spawned with, the threshold defeatedCheck also uses.
    public static boolean isBeaten(FleetInfo info) {
        return info.fleet.getFleetPoints() * 4.0f < info.strength;
    }

    // Despawns the fleet only when it is farther from the player than the maximum hyperspace sensor range, so the
    // player never sees it vanish. True when it despawned.
    public static boolean despawnOutOfSight(CampaignFleetAPI fleet) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return false;
        float dist = MathUtils.getDistance(pf.getLocationInHyperspace(), fleet.getLocationInHyperspace());
        if (dist <= Global.getSettings().getMaxSensorRangeHyper()) return false;
        fleet.despawn(FleetDespawnReason.PLAYER_FAR_AWAY, null);
        return true;
    }

    public static final float RAID_ORBIT_RANGE = 600f;
    public static final float RAID_BROKEN_STRENGTH = 0.2f;

    // Goes to info.target and orbits it with orbitText, re-issued on every call, while the target is set and the fleet
    // is not broken. Otherwise it withdraws once, to info.home when withdrawHome or to a random market of its faction
    // (info.home when there is none), and despawns there. Unlike the other AI methods it continues after the
    // standing-down check, so a standing-down fleet is sent back to its raid on the same call.
    public static void raidTargetAI(CampaignFleetAPI fleet, FleetInfo info, String orbitText, boolean withdrawHome, Random random) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (fleet.getAI() == null) return;
        FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
        safetyCheck(fleet, curr);
        specManeuversCheck(fleet, pf, curr);

        if (info.target == null || isRaidBroken(info)) {
            if (fleet.getAI().getCurrentAssignmentType() == FleetAssignment.GO_TO_LOCATION_AND_DESPAWN) return;
            SectorEntityToken market = withdrawHome ? null : SystemHelper.getRandomFactionMarket(random, fleet.getFaction().getId());
            fleet.clearAssignments();
            if (market == null) {
                fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, info.home, Float.MAX_VALUE, "standing down");
            } else {
                fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, market, Float.MAX_VALUE, "returning to " + market.getName());
            }
            return;
        }
        fleet.clearAssignments();
        if (MathUtils.getDistance(fleet.getLocation(), info.target.getLocation()) > RAID_ORBIT_RANGE) {
            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, info.target, Float.MAX_VALUE, "moving to location");
        } else {
            fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, info.target, Float.MAX_VALUE, orbitText);
        }
    }

    // Patrols the system of info.target (info.home until the first switch) with patrolText. Once the fleet has been in
    // that system for more than switchDays, it switches info.target to a random market of the faction and moves there,
    // ignoring other fleets on the way. Nothing changes while the fleet is in hyperspace. True on the call that switched.
    public static boolean patrolMarketsAI(CampaignFleetAPI fleet, FleetInfo info, String factionId, float switchDays,
                                          String patrolText, Random random) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (fleet.getAI() == null) return false;
        FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
        safetyCheck(fleet, curr);
        specManeuversCheck(fleet, pf, curr);
        if (info.target == null) info.target = info.home;
        if (info.target == null || fleet.isInHyperspace()) return false;

        boolean switched = false;
        if (fleet.getStarSystem() == info.target.getStarSystem()) {
            if (info.patrolArrivedAge < 0f) info.patrolArrivedAge = info.age;
            if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.PATROL_SYSTEM) {
                fleet.clearAssignments();
                fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, info.target, Float.MAX_VALUE, patrolText);
                fleet.getMemoryWithoutUpdate().unset(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
            }
            if (info.age - info.patrolArrivedAge > switchDays) {
                // Resets the patrol assignment before the move.
                fleet.clearAssignments();
                fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, info.target, Float.MAX_VALUE, patrolText);
                info.target = SystemHelper.getRandomFactionMarket(random, factionId);
                info.patrolArrivedAge = -1f;
                switched = true;
                log("patrol of " + fleet.getName() + " switches to " + info.target.getName());
            }
        }
        if (fleet.getStarSystem() != info.target.getStarSystem()
                && fleet.getAI().getCurrentAssignmentType() != FleetAssignment.GO_TO_LOCATION) {
            String name = info.target.getMarket() != null ? info.target.getMarket().getName() : info.target.getName();
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, info.target, Float.MAX_VALUE, "moving to " + name);
            fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        }
        return switched;
    }

    // A raiding fleet below a fifth of its spawn strength withdraws and no longer counts as a defender.
    public static boolean isRaidBroken(FleetInfo info) {
        return info.fleet.getFleetPoints() < info.strength * RAID_BROKEN_STRENGTH;
    }

    // Orbiting its target, in the sense raidTargetAI orders it.
    public static boolean isRaidingTarget(FleetInfo info) {
        return info.target != null && !isRaidBroken(info)
                && MathUtils.getDistance(info.fleet.getLocation(), info.target.getLocation()) <= RAID_ORBIT_RANGE;
    }

    // An expedition by FleetInfo.age: orbits info.home ("preparing") until prepareDays, travels to info.target, orbits it
    // ("on expedition", re-issued on every call) until returnAfterDays, then goes home and stands down there. The
    // phase checks run in this order against the assignment the fleet had at the start of the call, as the old job 3
    // expedition logic did; nothing is issued while the fleet is busy (MemFlags.FLEET_BUSY).
    public static void expeditionAI(CampaignFleetAPI fleet, FleetInfo info, float prepareDays, float returnAfterDays) {
        if (fleet.getAI() == null || info.home == null || info.target == null) return;
        if (fleet.getMemoryWithoutUpdate().contains(MemFlags.FLEET_BUSY)) return;
        if (fleet.getAI().getCurrentAssignment() == null) {
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.HOLD, fleet.getContainingLocation().createToken(fleet.getLocation()), Float.MAX_VALUE, "holding");
        }
        FleetAssignment assignment = fleet.getCurrentAssignment().getAssignment();
        boolean atHome = fleet.getContainingLocation() == info.home.getContainingLocation();
        boolean atTarget = fleet.getContainingLocation() == info.target.getContainingLocation();
        float age = info.age;
        if (age < prepareDays && atHome && assignment != FleetAssignment.ORBIT_PASSIVE) {
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, info.home, Float.MAX_VALUE, "preparing");
        }
        if (age > prepareDays && !atTarget && assignment != FleetAssignment.GO_TO_LOCATION) {
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, info.target, Float.MAX_VALUE, "moving to location");
        }
        // The fleet is never given PATROL_SYSTEM, so this orbit is issued again on every call.
        if (age < returnAfterDays && atTarget && assignment != FleetAssignment.PATROL_SYSTEM) {
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, info.target, Float.MAX_VALUE, "on expedition");
        }
        if (age > returnAfterDays && atTarget && assignment != FleetAssignment.GO_TO_LOCATION) {
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, info.home, Float.MAX_VALUE, "returning to " + info.home.getName());
        }
        if (age > returnAfterDays && atHome && assignment != FleetAssignment.ORBIT_PASSIVE) {
            fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, info.home, Float.MAX_VALUE, "standing down");
        }
    }

    public static FleetMemberAPI generateShip(String variant, boolean noAutofit, boolean alwaysRecover) {
        return generateShip(variant, noAutofit, alwaysRecover, new ArrayList<String>());
    }

    public static FleetMemberAPI generateShip(String variant, boolean noAutofit, boolean alwaysRecover, List<String> tags) {
        return generateShip(variant, noAutofit, alwaysRecover, tags, new ArrayList<String>());
    }

    public static FleetMemberAPI generateShip(String variant, boolean noAutofit, boolean alwaysRecover, List<String> tags, List<String> hullmods) {
        ShipVariantAPI thisVariant = Global.getSettings().getVariant(variant);
        // tags
        for (String t : tags){
            thisVariant.addTag(t);
        }
        // permamods
        for (String h : hullmods){
            thisVariant.addPermaMod(h, false);
        }
        if (noAutofit)thisVariant.addTag(Tags.TAG_NO_AUTOFIT);
        if (alwaysRecover)thisVariant.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
        thisVariant.addTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
        thisVariant.setSource(VariantSource.REFIT);
        FleetMemberAPI ship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, thisVariant);

        if(noAutofit)ship.setVariant(thisVariant, false, true);
        //attempt at keeping the variants intact
        if(!noAutofit)ship.setVariant(thisVariant, true, true);

        ship.getVariant().setOriginalVariant(variant);

        return ship;
    }

    public static FleetMemberAPI generateShip(ShipVariantAPI variant, boolean noAutofit, boolean alwaysRecover, List<String> tags, List<String> hullmods) {
        // tags
        for (String t : tags){
            variant.addTag(t);
        }
        // permamods
        for (String h : hullmods){
            variant.addPermaMod(h, false);
        }
        if (noAutofit)variant.addTag(Tags.TAG_NO_AUTOFIT);
        if (alwaysRecover)variant.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
        variant.addTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
        variant.setSource(VariantSource.REFIT);
        FleetMemberAPI ship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant);

        if(noAutofit)ship.setVariant(variant, false, true);
        //attempt at keeping the variants intact
        if(!noAutofit)ship.setVariant(variant, true, true);

        return ship;
    }

    public static void setAIOfficers(CampaignFleetAPI fleet){
        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()){
            setAIOfficer(m);
        }
    }

    public static void setAIOfficer(FleetMemberAPI member){

        if (member.isFighterWing()) return;
        if (member.getCaptain()==null) return;
        PersonAPI captain = member.getCaptain();
        String aiId = "";
        String portraitId = "";
        if (captain==null) return;
        int aiType = captain.getStats().getLevel();

        //gamma
        if (aiType==3 || aiType==4){
            aiId = "gamma_core";
            portraitId = "graphics/portraits/portrait_ai1b.png";
        }
        //beta
        if (aiType==5 || aiType==6) {
            aiId = "beta_core";
            portraitId = "graphics/portraits/portrait_ai3b.png";
        }
        //alpha
        if (aiType==7 || aiType==8) {
            aiId = "alpha_core";
            portraitId = "graphics/portraits/portrait_ai2b.png";
        }
        if (aiId.length()>0) captain.setAICoreId(aiId);
        if (portraitId.length()>0) captain.setPortraitSprite(portraitId);

        if (captain.getStats()==null) return;
        for (MutableCharacterStatsAPI.SkillLevelAPI skill : captain.getStats().getSkillsCopy()){
            if (skill.getSkill()==null) continue;
            if (!skill.getSkill().isCombatOfficerSkill()) continue;

            //elite
            if (skill.getLevel()<2f) {
                skill.setLevel(2f);
            }
        }
    }

    // Variants of tracked fleets can lose their tags after a reload or a rules interaction; hackBrokenVariants() restores them.

    public static final ArrayList<String> FLEET_ARRAY_KEYS = new ArrayList<>();
    static {
        FLEET_ARRAY_KEYS.add(QuestStageManager.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(HyperspaceEnigmaSpawner.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(StalkerSpawner.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(KestevenScavenger.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(GuardSpawner.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(BlackOpsManager.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(QuestFleets.KEY);
    }
    public static void hackBrokenVariants(){
        for (String key : FLEET_ARRAY_KEYS) {
            for (FleetInfo f : FleetHelper.getFleets(key)){
                FleetMemberAPI flagship = getOriginalFlagship(f);
                if (flagship!=null){
                    fix(flagship, f.flagshipSimpleMember);
                }
                if (!f.secondaries.isEmpty()){
                    for (FleetMemberAPI m : f.fleet.getMembersWithFightersCopy()){
                         if (f.secondaries.containsKey(m)){
                             fix(m, f.secondaries.get(m));
                         }
                    }
                }
            }
        }
    }
    // The ship SimpleFleet created as flagship, or null once it has left the fleet. Vanilla
    // getFlagship() then returns another member.
    public static FleetMemberAPI getOriginalFlagship(FleetInfo info){
        SimpleFleetMember flagshipInfo = info.flagshipSimpleMember;
        if (flagshipInfo==null || flagshipInfo.member==null) return null;
        if (!info.fleet.getFleetData().getMembersListCopy().contains(flagshipInfo.member)) return null;
        return flagshipInfo.member;
    }

    private static void fix(FleetMemberAPI original, SimpleFleetMember target){
        log("old tags "+original.getVariant().getTags());
        ShipVariantAPI thisVariant = Global.getSettings().getVariant(target.variant);
        // tags
        for (String t : target.variantTags){
            thisVariant.addTag(t);
        }
        // permamods
        for (String h : target.hullmods){
            thisVariant.addPermaMod(h, false);
        }
        if (target.noAutofit) thisVariant.addTag(Tags.TAG_NO_AUTOFIT);
        if (target.alwaysRecover) thisVariant.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
        thisVariant.addTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
        thisVariant.setSource(VariantSource.REFIT);

        if(target.noAutofit)original.setVariant(thisVariant, false, true);
        //attempt at keeping the variants intact
        if(!target.noAutofit)original.setVariant(thisVariant, false, true);
        log("FIXED "+original.getHullSpec().getHullName()+" tags "+thisVariant.getTags());
    }
}
