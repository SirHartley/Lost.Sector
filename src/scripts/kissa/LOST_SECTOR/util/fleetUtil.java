package scripts.kissa.LOST_SECTOR.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
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
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.fleets.*;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_abyssSpawner;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_eternitySpawner;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_mothershipSpawner;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_rorqSpawner;
import scripts.kissa.LOST_SECTOR.campaign.fleets.events.nskr_interceptManager;
import scripts.kissa.LOST_SECTOR.campaign.fleets.events.nskr_loanShark;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.fleetInfo;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleFleetMember;

import java.util.*;

public class fleetUtil {

    static void log(final String message) {
        Global.getLogger(fleetUtil.class).info(message);
    }

    public static List<fleetInfo> getFleets(String id) {
        MemoryAPI mem = Global.getSector().getMemory();
        if (mem.contains(id)){
            return (List<fleetInfo>)mem.get(id);
        } else {
            mem.set(id, new ArrayList<fleetInfo>());
        }
        return (List<fleetInfo>)mem.get(id);
    }

    public static List<fleetInfo> setFleets(List<fleetInfo> fleets, String id) {
        MemoryAPI mem = Global.getSector().getMemory();
        mem.set(id, fleets);
        return (List<fleetInfo>) mem.get(id);
    }

    //don't use, broken
    //public static fleetInfo getFleet(String id) {
    //    MemoryAPI mem = Global.getSector().getMemory();
    //    if (mem.contains(id)){
    //        return (fleetInfo)mem.get(id);
    //    } else {
    //        mem.set(id, new fleetInfo(null,null,null));
    //    }
    //    return  (fleetInfo)mem.get(id);
    //}
    //public static fleetInfo setFleet(fleetInfo fleet, String id) {
    //    MemoryAPI mem = Global.getSector().getMemory();
    //    mem.set(id, fleet);
    //    return (fleetInfo) mem.get(id);
    //}

    public static void cleanUp(List<CampaignFleetAPI> toRemove, List<fleetInfo> fleets) {
        for (Iterator<fleetInfo> iter = fleets.listIterator(); iter.hasNext();) {
            CampaignFleetAPI a = iter.next().fleet;
            for (CampaignFleetAPI remove : toRemove) {
                if (a == remove) {

                    log("REMOVED " + a.getName());
                    iter.remove();
                }
                if (a==null) iter.remove();
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

        //for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
        //    log(fleet.getName()+" ship "+m.getHullSpec().getHullName()+" tags "+m.getVariant().getTags().toString());
        //}
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
    private static boolean defeatedCheck(CampaignFleetAPI fleet, fleetInfo info, CampaignFleetAPI pf) {
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
    public static void gotoAndInterceptPlayerAI(CampaignFleetAPI fleet, fleetInfo info, interceptBehaviour behaviour){
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
    private static void createAroundInterceptTarget(fleetInfo info, CampaignFleetAPI pf) {
        //clean up
        if (info.target!=null) info.target.setExpired(true);

        Vector2f aroundPlayer = new Vector2f(0f, 0f);
        aroundPlayer.setX(pf.getLocation().getX() + MathUtils.getRandomNumberInRange(-AROUND_ERROR, AROUND_ERROR));
        aroundPlayer.setY(pf.getLocation().getY() + MathUtils.getRandomNumberInRange(-AROUND_ERROR, AROUND_ERROR));

        info.target = pf.getContainingLocation().createToken(aroundPlayer);
    }
    public enum interceptBehaviour {
        DIRECT,
        AROUND
    }


    public static void guardTargetAI(CampaignFleetAPI fleet, fleetInfo info, guardMovementBehaviour movementBehaviour, guardAttackBehaviour attackBehaviour, float playerInterceptChance) {
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
            createGuardTarget(info, pf);
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
            else if (attackBehaviour == guardAttackBehaviour.HOSTILE){
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
    private static void createGuardTarget(fleetInfo info, CampaignFleetAPI pf) {
        //clean up
        if (info.target!=null) info.target.setExpired(true);

        info.target = pf.getContainingLocation().createToken(info.fleet.getLocation());
    }
    public enum guardMovementBehaviour {
        HOLD,
        ORBIT
    }
    public enum guardAttackBehaviour {
        HOSTILE,
        PLAYER
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

        //no wait this breaks everything
        //
        //just in case
        //if (aiType==1 || aiType==2){
        //    //actually just make them level 4
        //    Map<String, Integer> skills = util.createRandomSkills(4, 1f, new Random());
//
        //    util.setOfficerSkills(captain, skills);
//
        //    aiType = captain.getStats().getLevel();
        //}
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

    //I really don't know why variants sometimes lose their tags on reload?
    //by sometimes I mean after stuff like Rules interactions???!
    //TODO one day I will figure this shit out

    public static final ArrayList<String> FLEET_ARRAY_KEYS = new ArrayList<>();
    static {
        FLEET_ARRAY_KEYS.add(questStageManager.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(nskr_hyperspaceEnigmaSpawner.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(nskr_stalkerSpawner.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(nskr_eternitySpawner.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(nskr_kestevenScavenger.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(nskr_guardSpawner.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(nskr_abyssSpawner.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(nskr_rorqSpawner.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(nskr_interceptManager.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(nksr_blackOpsManager.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(nskr_loanShark.FLEET_ARRAY_KEY);
        FLEET_ARRAY_KEYS.add(nskr_mothershipSpawner.FLEET_ARRAY_KEY);
    }
    public static void hackBrokenVariants(){
        for (String key : FLEET_ARRAY_KEYS) {
            for (fleetInfo f : fleetUtil.getFleets(key)){
                FleetMemberAPI flagship = f.fleet.getFlagship();
                if (flagship!=null && f.flagshipSimpleMember!=null){
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
    private static void fix(FleetMemberAPI original, simpleFleetMember target){
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
