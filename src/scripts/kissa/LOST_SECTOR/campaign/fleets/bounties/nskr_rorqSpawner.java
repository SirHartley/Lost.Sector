package scripts.kissa.LOST_SECTOR.campaign.fleets.bounties;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.magiclib.util.MagicCampaign;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.intel.nskr_rorqIntel;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.*;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.*;

public class nskr_rorqSpawner extends BaseCampaignEventListener implements EveryFrameScript {
    //
    //spawns a special indy peacekeeping force with a bounty once per game, patrols the core indy planets.
    //

    public static final float BOUNTY_PAYOUT = 315000f;
    public static final String FLEET_NAME = "Peacekeepers";
    public static final String COM_NAME = "Alistair";
    public static final String COM_LAST_NAME = "Walsh";
    public static final String FLAGSHIP_VARIANT = "nskr_rorqual_boss";
    public static final String SECONDARY_VARIANT_1 = "conquest_Elite";
    public static final String SECONDARY_VARIANT_2 = "champion_Support";
    public static final int SECONDARY_VARIANT_2_COUNT = 2;
    public static final String FS_NAME = "ISS White Whale";
    public static final String FACTION = Factions.MERCENARY;
    public static final float SWITCH_LOCATION_TIMER = 30f;
    public static final String LOOT_KEY = "$RorqLoot";
    //for checking is the player defeated the bounty and contribution
    public static final String DEFEAT_ID = "RORQ_LOOT";
    public static final String DEFEAT_ID_PAID = "RORQ_LOOT_PAID";
    public static final String PORTRAIT_SPRITE = "graphics/portraits/nskr_pkGuy.png";
    public static final String MEMORY_KEY = "RORQ";
    public static final String SAVED_PREFIX = "rorq";
    public static final String LOG_PREFIX = "rorqSpawner";
    public static final String PERSISTENT_RANDOM_KEY = "rorqSpawnerRandom";
    public static final String FLEET_ARRAY_KEY = "$nskr_rorqSpawnerFleets";

    public static final List<Pair<String, Float>> roles = new ArrayList<>();
    static {
        roles.add(new Pair<>(ShipRoles.COMBAT_SMALL, 12f));
        roles.add(new Pair<>(ShipRoles.COMBAT_MEDIUM, 10f));
        roles.add(new Pair<>(ShipRoles.COMBAT_LARGE, 8f));
        roles.add(new Pair<>(ShipRoles.CARRIER_MEDIUM, 4f));
        roles.add(new Pair<>(ShipRoles.CARRIER_LARGE, 4f));
    }
    nskr_saved<Float> counter;
    nskr_saved<Float> newTargetCounter;
    nskr_saved<Boolean> newGame;
    nskr_saved<Boolean> firstTime;
    private final List<CampaignFleetAPI> removed = new ArrayList<>();
    //CampaignFleetAPI pf;
    Random random;

    static void log(final String message) {
        Global.getLogger(nskr_rorqSpawner.class).info(message);
    }

    public nskr_rorqSpawner() {
        super(false);
        //how often we run logic
        this.counter = new nskr_saved<>(SAVED_PREFIX + "Counter", 0.0f);
        this.newTargetCounter = new nskr_saved<>(SAVED_PREFIX + "newTargetCounter", 0.0f);
        this.newGame = new nskr_saved<>(SAVED_PREFIX + "NewGame", true);
        //for intel
        this.firstTime = new nskr_saved<>(SAVED_PREFIX + "FirstTime", true);
        this.random = new Random();
        //init randoms
        getRandom();
    }

    @Override
    public void advance(float amount) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;
        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);

        if (Global.getSector().isInFastAdvance()) {
            counter.val += 2f*amount;
        } else{
            counter.val += amount;
        }
        //spawn once per game
        if (fleets.isEmpty() && newGame.val) {
            spawnRorqFleet(getTarget());
            newGame.val = false;
        }
        //logic
        if (counter.val>10f) {
            for (fleetInfo f : fleets) {
                CampaignFleetAPI fleet = f.fleet;
                //rorq GONE
                if (fleet.getFlagship()==null || fleet.getFlagship() != fleet.getFlagship()) {
                    fleet.getMemoryWithoutUpdate().unset(nskr_rorqSpawner.LOOT_KEY);
                }

                boolean despawn = false;

                //looted
                if (!fleet.getMemoryWithoutUpdate().contains(LOOT_KEY)){
                    despawn = true;
                }
                //destroyed
                if (fleet.getFleetPoints()<=0f){
                    despawn = true;
                }

                //for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()){
                //    if(!m.getHullSpec().getBaseHullId().equals("nskr_rorqual"))continue;
                //    fleet.removeFleetMemberWithDestructionFlash(m);
                //    //update
                //    fleetUtil.update(fleet, new Random());
                //}

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
                //logic
                boolean visibleToPlayer = fleet.isVisibleToSensorsOf(pf);
                if (visibleToPlayer && firstTime.val) {
                    //Adds our intel
                    nskr_rorqIntel intel = new nskr_rorqIntel(fleet);
                    Global.getSector().getIntelManager().addIntel(intel, false);
                    log(LOG_PREFIX + " added INTEL");

                    Global.getSector().getCampaignUI().addMessage("Initial examinations of the Independent fleet shows an unusual flagship, the Rorqual-Class. Approach with caution.",
                            Global.getSettings().getColor("standardTextColor"),
                            "Rorqual-Class",
                            "caution",
                            Global.getSettings().getColor("yellowTextColor"),
                            Global.getSettings().getColor("yellowTextColor"));
                    firstTime.val = false;
                }
                //stop here when defeated
                if (despawn) continue;
                //assignment logic
                FleetAssignmentDataAPI assignment = fleet.getAI().getCurrentAssignment();
                if (assignment == null) {
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.HOLD, fleet.getContainingLocation().createToken(fleet.getLocation()), Float.MAX_VALUE, "holding");
                    log("null assignment");
                }
                //used special maneuvers
                if (assignment!=null && assignment.getAssignment()==FleetAssignment.STANDING_DOWN) {
                    CampaignFleetAIAPI ai = fleet.getAI();
                    if (ai instanceof ModularFleetAIAPI) {
                        // needed to interrupt an in-progress pursuit
                        ModularFleetAIAPI m = (ModularFleetAIAPI) ai;
                        m.getStrategicModule().getDoNotAttack().add(pf, 1f);
                        m.getTacticalModule().setTarget(null);
                    }
                }
                SectorEntityToken target = getTarget();
                if (!fleet.isInHyperspace()) {
                    //PATROL
                    if (fleet.getStarSystem() == target.getStarSystem()) {
                        newTargetCounter.val += 1f;
                        if (fleet.getCurrentAssignment().getAssignment() != FleetAssignment.PATROL_SYSTEM) {
                            fleet.clearAssignments();
                            fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, target, Float.MAX_VALUE, "maintaining order");

                            if (fleet.getMemoryWithoutUpdate().contains(MemFlags.FLEET_IGNORES_OTHER_FLEETS)) {
                                fleet.getMemoryWithoutUpdate().unset(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
                            }
                            log(LOG_PREFIX + " patrolling " + target.getName());
                        }
                        //SWITCH
                        if (newTargetCounter.val > SWITCH_LOCATION_TIMER) {
                            //to make sure the assignment gets reset
                            fleet.clearAssignments();
                            fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, target, Float.MAX_VALUE, "maintaining order");

                            setTarget(questUtil.getRandomFactionMarket(getRandom(), Factions.INDEPENDENT));
                            newTargetCounter.val = 0f;
                            log(LOG_PREFIX + " new target " + getTarget().getName());
                            //reinforce
                            if (fleet.getFleetPoints() < f.strength * 0.80) {
                                reinforceFleet(fleet, f.strength);
                                log(LOG_PREFIX + " reinforced " + fleet.getName());
                            } else
                                log(LOG_PREFIX + " no reinforce, curr " + fleet.getFleetPoints() + " vs " + f.strength);
                        }
                    }
                    //MOVE
                    if (fleet.getStarSystem() != getTarget().getStarSystem()) {
                        if (fleet.getCurrentAssignment().getAssignment() != FleetAssignment.GO_TO_LOCATION) {
                            fleet.clearAssignments();
                            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, getTarget(), Float.MAX_VALUE, "moving to " + getTarget().getMarket().getName());

                            if (!fleet.getMemoryWithoutUpdate().contains(MemFlags.FLEET_IGNORES_OTHER_FLEETS)) {
                                fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
                            }
                            log(LOG_PREFIX + " moving to " + getTarget().getName());
                        }
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

    void spawnRorqFleet(SectorEntityToken loc) {

        float points = MathUtils.getRandomNumberInRange(180f, 190f);

        //apply settings
        points *= nskr_modPlugin.getScriptedFleetSizeMult();

        Random random = new Random();

        //skills
        Map<String, Integer> skills = new HashMap<>();
        skills.put("combat_endurance",2);
        skills.put("damage_control",2);
        skills.put("field_modulation",2);
        skills.put("target_analysis",2);
        skills.put("ordnance_expert",2);
        skills.put("missile_specialization",2);
        skills.put("energy_weapon_mastery",2);
        skills.put("impact_mitigation",2);
        //skills com
        skills.put("electronic_warfare",1);
        skills.put("crew_training",1);

        //memkeys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
        keys.add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        keys.add(MemFlags.MEMORY_KEY_LOW_REP_IMPACT);
        keys.add(LOOT_KEY);

        //flagship
        simpleFleetMember flagship = new simpleFleetMember(FLAGSHIP_VARIANT, new ArrayList<String>(), true);
        flagship.alwaysRecover = true;
        flagship.name = FS_NAME;

        //secondaries
        List<simpleFleetMember> secondaries = new ArrayList<>();
        simpleFleetMember secondary;
        //conq
        secondary = new simpleFleetMember(SECONDARY_VARIANT_1, new ArrayList<String>(), false);
        secondary.captain = addCaptain(7);
        secondaries.add(secondary);
        //champion
        for (int x = 0; x<SECONDARY_VARIANT_2_COUNT; x++) {
            secondary = new simpleFleetMember(SECONDARY_VARIANT_2, new ArrayList<String>(), false);
            secondary.captain = addCaptain(6);
            secondaries.add(secondary);
        }

        //commander
        simpleCaptain simpleCaptain = new simpleCaptain("nskr_"+COM_NAME, FACTION, skills);
        simpleCaptain.personality = Personalities.RECKLESS;
        simpleCaptain.portraitSpritePath = PORTRAIT_SPRITE;
        simpleCaptain.postId = Ranks.POST_FLEET_COMMANDER;
        simpleCaptain.rankId = Ranks.SPACE_CAPTAIN;
        simpleCaptain.firstName = COM_NAME;
        simpleCaptain.lastName = COM_LAST_NAME;
        simpleCaptain.gender = FullName.Gender.MALE;

        //fleet
        simpleFleet simpleFleet = new simpleFleet(loc, FACTION, points, keys, random);
        simpleFleet.type = FleetTypes.PATROL_LARGE;
        simpleFleet.maxShipSize = 4;
        simpleFleet.sMods = 2;
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.commander = simpleCaptain.create();
        simpleFleet.flagshipInfo = flagship;
        simpleFleet.secondaries = secondaries;
        simpleFleet.name = FLEET_NAME;
        simpleFleet.noFactionInName = true;
        simpleFleet.assignment = FleetAssignment.PATROL_SYSTEM;
        simpleFleet.assignmentText = "maintaining order";
        CampaignFleetAPI fleet = simpleFleet.create();

        fleet.setFaction(Factions.INDEPENDENT, false);
        //makes sure fleet only spawns with 1 rorq, since the ship can randomly spawn in merc fleets
        fixRorqCount(fleet);

        //update
        fleetUtil.update(fleet, random);

        //add to mem IMPORTANT
        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
        fleetInfo info = new fleetInfo(fleet, null, loc);
        info.flagshipSimpleMember = simpleFleet.getFlagshipInfo();
        info.secondaries = simpleFleet.getSecondaryMembers();
        fleets.add(info);
        fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

        log(LOG_PREFIX+" SPAWNED, size " + points + " system " + loc.getContainingLocation().getName()+ " loc " + loc.getName());
        log(LOG_PREFIX+" FLEET, loc " + fleet.getStarSystem().getName() +" size "+ fleet.getFleetPoints() + " commander " + fleet.getCommander().getName().getFullName() + " flagship " + fleet.getFlagship().getHullSpec().getBaseHullId());
    }
    private static CampaignFleetAPI fixRorqCount(CampaignFleetAPI fleet){
        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()){
            if(m.getCaptain().getId().equals("nskr_pkguy") || m.isFlagship())continue;
            if(!m.getHullSpec().getBaseHullId().equals("nskr_rorqual"))continue;
            ShipVariantAPI thisVariant = Global.getSettings().getVariant(SECONDARY_VARIANT_2).clone();
            m.setVariant(thisVariant, true, true);
            //update
            fleetUtil.update(fleet, new Random());

            log(LOG_PREFIX+" fixed extra rorq spawn");
        }
        return fleet;
    }

    private static CampaignFleetAPI reinforceFleet(CampaignFleetAPI fleet, float targetStrength){
        while (fleet.getFleetPoints()<(int)targetStrength){
            FleetMemberAPI member = addToFleet(randomRole(), getRandom(), fleet);
            if (!member.isFighterWing()) {
                addSmods(member, mathUtil.getSeededRandomNumberInRange(1,3, getRandom()), fleet.getCommanderStats());
                log(LOG_PREFIX+" unused " + member.getVariant().getUnusedOP(fleet.getCommanderStats())+" ship "+member.getHullSpec().getBaseHullId());
                if (getRandom().nextFloat()<0.67f) {
                    PersonAPI captain = addCaptain(7);
                    member.setCaptain(captain);
                    log(LOG_PREFIX+" captain " + captain.getName().getFullName()+ " level "+captain.getStats().getLevel()+" ship "+member.getHullSpec().getBaseHullId());
                }
            }

            for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
                //keep Smods for all ships
                m.getVariant().addTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
                m.getVariant().addTag(Tags.VARIANT_ALWAYS_RETAIN_SMODS_ON_SALVAGE);
                //CR update
                m.getRepairTracker().setCR(m.getRepairTracker().getMaxCR());
            }
            //update
            fleetUtil.update(fleet, new Random());
            log(LOG_PREFIX+" reinforcing "+member.getHullSpec().getBaseHullId());
        }
        return fleet;
    }

    private static PersonAPI addCaptain(int maxLevel){
        PersonAPI base = Global.getSector().getFaction(FACTION).createRandomPerson(FullName.Gender.ANY, getRandom());

        int level = mathUtil.getSeededRandomNumberInRange(maxLevel-2, maxLevel, getRandom());
        Map<String, Integer> skills = util.createRandomSkills(level, 0.67f, getRandom());

        PersonAPI captain = MagicCampaign.createCaptainBuilder(base.getFaction().getId()).create();
        captain.setId("nskr_"+base.getId());
        captain.setPersonality(base.getPersonalityAPI().getId());
        captain.setPortraitSprite(base.getPortraitSprite());
        captain.setPostId(base.getPostId());
        captain.setRankId(base.getRankId());
        FullName name = new FullName(base.getName().getFirst(), base.getName().getLast(), base.getGender());
        captain.setName(name);
        util.setOfficerSkills(captain, skills);

        return captain;
    }
    private static FleetMemberAPI addSmods(FleetMemberAPI member, int count, MutableCharacterStatsAPI stats){
        if (member.isFighterWing()) return null;
        ShipVariantAPI v = member.getVariant().clone();
        Collection<String> validHmods = validHullmods(v.getNonBuiltInHullmods(), v.getSMods(), v.getPermaMods());
        int vCount = count;
        if (validHmods.size()<count) vCount = validHmods.size();
        //when you just want to spawn some ships and casually start making DynaSector
        //swapping existing to smod
        while(v.getSMods().size()<vCount){
            String mod = null;
            for (String m : validHullmods(v.getNonBuiltInHullmods(), v.getSMods(), v.getPermaMods())){
                mod = m;
                if (mod.equals("heavyarmor")){
                    break;
                }
                if (mod.equals("missleracks")){
                    break;
                }
                if (mod.equals("targetingunit")){
                    break;
                }
                if (mod.equals("hardenedshieldemitter")){
                    break;
                }
                if (mod.equals("eccm")){
                    break;
                }
                if (mod.equals("unstable_injector")){
                    break;
                }
                if (mod.equals("fluxdistributor")){
                    break;
                }
                if (mod.equals("fluxcoil")){
                    break;
                }
            }
            v.addPermaMod(mod, true);
            log(LOG_PREFIX+" added "+mod+" to "+member.getHullSpec().getBaseHullId());
            if (mod==null)log(LOG_PREFIX+" ERROR "+mod+" hullmod "+member.getHullSpec().getBaseHullId());
        }
        //adding entirely new smods
        while(v.getSMods().size()<count){
            Collection<String> vm = v.getHullMods();
            if(!vm.contains("heavyarmor") && v.getSMods().size()<count){
                v.addPermaMod("heavyarmor", true);
            }
            if(!vm.contains("hardenedshieldemitter") && v.getSMods().size()<count){
                v.addPermaMod("hardenedshieldemitter", true);
            }
            if(!vm.contains("targetingunit") && !vm.contains("dedicated_targeting_core") && v.getSMods().size()<count){
                v.addPermaMod("targetingunit", true);
            }
            if(!vm.contains("fluxdistributor") && v.getSMods().size()<count){
                v.addPermaMod("fluxdistributor", true);
            }
            if(!vm.contains("fluxcoil") && v.getSMods().size()<count){
                v.addPermaMod("fluxcoil", true);
            }
            if(!vm.contains("reinforcedhull") && v.getSMods().size()<count){
                v.addPermaMod("reinforcedhull", true);
            }
        }
        //naahhh, he actually making DynaSector

        //spending unused op
        int unusedOP = v.getUnusedOP(stats);
        if (unusedOP>0) {
            int maxVentsOrCaps = 0;
            if (v.getHullSize() == ShipAPI.HullSize.FRIGATE) maxVentsOrCaps = 10;
            if (v.getHullSize() == ShipAPI.HullSize.DESTROYER) maxVentsOrCaps = 20;
            if (v.getHullSize() == ShipAPI.HullSize.CRUISER) maxVentsOrCaps = 30;
            if (v.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) maxVentsOrCaps = 50;
            if (v.getNumFluxVents() < maxVentsOrCaps) {
                int ventsToAdd = maxVentsOrCaps-v.getNumFluxVents();
                if (ventsToAdd<=unusedOP){
                    v.setNumFluxVents(maxVentsOrCaps);
                    unusedOP-=ventsToAdd;
                    log(LOG_PREFIX + " added "+maxVentsOrCaps);
                } else {
                    v.setNumFluxVents(unusedOP+v.getNumFluxVents());
                    unusedOP-=unusedOP;
                    log(LOG_PREFIX + " added "+unusedOP);
                }
                log(LOG_PREFIX + " max vents " + maxVentsOrCaps + " for " + member.getHullSpec().getBaseHullId()+" total "+v.getNumFluxVents());
                log(LOG_PREFIX + " leftover "+unusedOP);
            }
            if (v.getNumFluxCapacitors() < maxVentsOrCaps) {
                int capsToAdd = maxVentsOrCaps-v.getNumFluxCapacitors();
                if (capsToAdd<=unusedOP){
                    v.setNumFluxCapacitors(maxVentsOrCaps);
                    unusedOP-=capsToAdd;
                    log(LOG_PREFIX + " added "+maxVentsOrCaps);
                } else {
                    v.setNumFluxCapacitors(unusedOP+v.getNumFluxCapacitors());
                    unusedOP-=unusedOP;
                    log(LOG_PREFIX + " added "+unusedOP);
                }
                log(LOG_PREFIX + " max caps " + maxVentsOrCaps + " for " + member.getHullSpec().getBaseHullId()+" total "+v.getNumFluxCapacitors());
                log(LOG_PREFIX + " leftover "+unusedOP);
            }
        }
        //set variant
        member.setVariant(v, false, true);

        return member;
    }
    private static Collection<String> validHullmods(Collection<String> hullmods, LinkedHashSet<String> smods, Set<String> permamods){
        Collection<String> valid = new ArrayList<>();
        for (String m : hullmods){
            if (smods.contains(m)) continue;
            if (permamods.contains(m)) continue;
            if (m.equals("safetyoverrides")) continue;
            if (m.equals("phase_anchor")) continue;
            valid.add(m);
        }
        return valid;
    }
    private static FleetMemberAPI addToFleet(String role, Random random, CampaignFleetAPI fleet) {
        FleetMemberAPI member = null;
        FactionAPI.ShipPickParams params = new FactionAPI.ShipPickParams(FactionAPI.ShipPickMode.PRIORITY_THEN_ALL);
        List<ShipRolePick> picks = fleet.getFaction().pickShip(role, params, null, random);
        for (ShipRolePick pick : picks) {
            member = addToFleet(pick, fleet, random);
        }
        return member;
    }
    private static FleetMemberAPI addToFleet(ShipRolePick pick, CampaignFleetAPI fleet, Random random) {
        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, pick.variantId);
        String name = fleet.getFleetData().pickShipName(member, random);
        member.setShipName(name);
        fleet.getFleetData().addFleetMember(member);
        return member;
    }

    private static String randomRole() {
        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
        for (Pair<String,Float> s : roles){
            picker.add(s.one,s.two);
        }
        String role = picker.pick(getRandom());
        return role;
    }

    //location saved to memory (important for intel))
    public static SectorEntityToken getTarget() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = MEMORY_KEY;
        if (!data.containsKey(id))
            data.put(id, questUtil.getRandomFactionMarket(getRandom(), Factions.INDEPENDENT));

        return (SectorEntityToken)data.get(id);
    }

    //set a different target
    public static void setTarget(SectorEntityToken target){
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = MEMORY_KEY;
        data.put(id, target);
    }


    public static Random getRandom() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

            data.put(PERSISTENT_RANDOM_KEY,  new Random(new Random().nextLong()));
        }
        return (Random) data.get(PERSISTENT_RANDOM_KEY);
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }
}
