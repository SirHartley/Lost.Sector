package scripts.kissa.LOST_SECTOR.campaign.quests.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.nskr_kestevenQuest;
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.nskr_ttCollectorDialog;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;
import scripts.kissa.LOST_SECTOR.util.powerLevel;

import java.util.*;

public class questFleets {

    static void log(final String message) {
        Global.getLogger(questFleets.class).info(message);
    }

    public static final String FLEET_NAME = "Eliza's Merc Armada";
    public static final String FLAGSHIP_VARIANT = "nskr_onslaught_boss";
    public static final String FS_NAME = "Regicide";
    public static final String ELIZA_RAIDED_FLEET_KEY = "$ElizaFleet";
    //ELIZA FLEET
    public static CampaignFleetAPI spawnElizaFleet(SectorEntityToken loc, PersonAPI eliza, Random random, boolean revengeance, boolean intercept) {

        float points = mathUtil.getSeededRandomNumberInRange(190f,200f, random);

        //apply settings
        points *= nskr_modPlugin.getScriptedFleetSizeMult();

        //skills
        Map<String, Integer> skills = new HashMap<>();
        skills.put("combat_endurance",2);
        skills.put(Skills.HELMSMANSHIP,2);
        skills.put("field_modulation",2);
        skills.put("target_analysis",2);
        skills.put(Skills.POLARIZED_ARMOR,2);
        skills.put("missile_specialization",2);
        skills.put(Skills.BALLISTIC_MASTERY,2);
        skills.put("impact_mitigation",2);
        //com skills
        skills.put(Skills.WOLFPACK_TACTICS,1);
        skills.put("crew_training",1);
        skills.put(Skills.COORDINATED_MANEUVERS,1);

        //commander
        util.setOfficerSkills(eliza, skills);
        eliza.setPersonality(Personalities.RECKLESS);

        //memkeys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(MemFlags.MEMORY_KEY_NO_REP_IMPACT);
        if (!intercept) keys.add(MemFlags.MEMORY_KEY_MAKE_HOSTILE);
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
        keys.add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);

        if (!revengeance && !intercept){
            keys.add(ELIZA_RAIDED_FLEET_KEY);
        }
        else if (revengeance){
            keys.add(questStageManager.REVENGEANCE_FLEET_KEY);
        }
        else if (intercept){
            keys.add(questStageManager.ELIZA_INTERCEPT_FLEET_KEY);
        }

        //permamods
        List<String> permamods = new ArrayList<>();
        permamods.add(HullMods.HEAVYARMOR);
        //flagship
        simpleFleetMember flagship = new simpleFleetMember(FLAGSHIP_VARIANT, new ArrayList<String>(), true);
        flagship.alwaysRecover = true;
        flagship.hullmods = permamods;
        flagship.name = FS_NAME;

        //fleet
        simpleFleet simpleFleet = new simpleFleet(loc, Factions.MERCENARY, points, keys, random);
        //simpleFleet.type = FleetTypes.PATROL_LARGE;
        simpleFleet.maxShipSize = 3;
        simpleFleet.sMods = 3;
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.commander = eliza;
        simpleFleet.flagshipInfo = flagship;
        simpleFleet.name = FLEET_NAME;
        simpleFleet.noFactionInName = true;
        simpleFleet.assignment = FleetAssignment.ORBIT_PASSIVE;
        simpleFleet.assignmentText = "holding";
        CampaignFleetAPI fleet = simpleFleet.create();

        fleet.setFaction(Factions.MERCENARY, false);

        //update
        fleetUtil.update(fleet, random);


        //add to mem IMPORTANT
        List<fleetInfo> fleets = fleetUtil.getFleets(questStageManager.FLEET_ARRAY_KEY);
        fleetInfo info = new fleetInfo(fleet, null, loc);
        info.flagshipSimpleMember = simpleFleet.getFlagshipInfo();
        info.secondaries = simpleFleet.getSecondaryMembers();
        fleets.add(info);
        fleetUtil.setFleets(fleets, questStageManager.FLEET_ARRAY_KEY);

        log("Eliza SPAWNED, size " + points + " loc " + loc.getName() + " system " + loc.getContainingLocation().getName());
        log("Eliza FLEET, loc " + fleet.getStarSystem().getName() +" size "+ fleet.getFleetPoints() + " commander " + fleet.getCommander().getName().getFullName() + " flagship " + fleet.getFlagship().getHullSpec().getBaseHullId());
        return fleet;
    }

    //JACK FLEET
    public static CampaignFleetAPI spawnJackFleet(SectorEntityToken loc, PersonAPI jack, Random random) {

        float points = mathUtil.getSeededRandomNumberInRange(190f,200f, random);

        //apply settings
        points *= nskr_modPlugin.getScriptedFleetSizeMult();

        //skills
        Map<String, Integer> skills = new HashMap<>();
        skills.put("combat_endurance",2);
        skills.put(Skills.DAMAGE_CONTROL,2);
        skills.put("field_modulation",2);
        skills.put("target_analysis",2);
        skills.put(Skills.POLARIZED_ARMOR,2);
        skills.put(Skills.BALLISTIC_MASTERY,2);
        skills.put(Skills.ENERGY_WEAPON_MASTERY,2);
        skills.put("impact_mitigation",2);
        //skills com
        skills.put(Skills.ELECTRONIC_WARFARE,1);
        skills.put("crew_training",1);
        skills.put(Skills.COORDINATED_MANEUVERS,1);

        //memkeys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(MemFlags.MEMORY_KEY_NO_REP_IMPACT);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOSTILE);
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
        keys.add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        keys.add(questStageManager.JACK_REVENGEANCE_FLEET_KEY);
        keys.add(questStageManager.REVENGEANCE_FLEET_KEY);

        //permamods
        List<String> permamods = new ArrayList<>();
        permamods.add(HullMods.HEAVYARMOR);
        //flagship
        simpleFleetMember flagship = new simpleFleetMember("nskr_prosperity_boss", new ArrayList<String>(), true);
        flagship.alwaysRecover = true;
        flagship.hullmods = permamods;
        flagship.name = "K-Corp Homewrecker";

        //fleet
        simpleFleet simpleFleet = new simpleFleet(loc, "kesteven", points, keys, random);
        //simpleFleet.type = FleetTypes.PATROL_LARGE;
        simpleFleet.maxShipSize = 3;
        simpleFleet.sMods = 3;
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.commander = util.setOfficerSkills(jack, skills);
        simpleFleet.flagshipInfo = flagship;
        simpleFleet.name = "Task Force";
        simpleFleet.assignment = FleetAssignment.ORBIT_PASSIVE;
        simpleFleet.assignmentText = "holding";
        CampaignFleetAPI fleet = simpleFleet.create();

        //add to mem IMPORTANT
        List<fleetInfo> fleets = fleetUtil.getFleets(questStageManager.FLEET_ARRAY_KEY);
        fleetInfo info = new fleetInfo(fleet, null, loc);
        info.flagshipSimpleMember = simpleFleet.getFlagshipInfo();
        info.secondaries = simpleFleet.getSecondaryMembers();
        fleets.add(info);
        fleetUtil.setFleets(fleets, questStageManager.FLEET_ARRAY_KEY);

        log("Jack SPAWNED, size " + points + " loc " + loc.getName() + " system " + loc.getContainingLocation().getName());
        log("Jack FLEET, loc " + fleet.getStarSystem().getName() +" size "+ fleet.getFleetPoints() + " commander " + fleet.getCommander().getName().getFullName() + " flagship " + fleet.getFlagship().getHullSpec().getBaseHullId());
        return fleet;
    }

    //tt collector fleet
    public static CampaignFleetAPI spawnCollectorFleet() {
        Random random = nskr_ttCollectorDialog.getRandom();
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        float combatPoints = mathUtil.getSeededRandomNumberInRange(100f, 120f, random);
        //power scaling
        combatPoints += combatPoints * powerLevel.get(0.2f, 0f,1f);
        log("tt collector BASE " + combatPoints);

        //apply settings
        combatPoints *= nskr_modPlugin.getScriptedFleetSizeMult();

        SectorEntityToken loc = pf.getContainingLocation().createToken(pf.getLocation());

        //keys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOSTILE);
        keys.add(MemFlags.MEMORY_KEY_LOW_REP_IMPACT);
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);
        keys.add(MemFlags.MEMORY_KEY_NEVER_AVOID_PLAYER_SLOWLY);
        keys.add(questStageManager.TT_COLLECTOR_KEY);

        //fleet
        simpleFleet simpleFleet = new simpleFleet(loc, Factions.TRITACHYON, combatPoints, keys, random);
        //simpleFleet.type = FleetTypes.PATROL_LARGE;
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.sMods = 2;
        simpleFleet.maxShipSize = 3;
        simpleFleet.name = "Black Ops";
        simpleFleet.assignment = FleetAssignment.INTERCEPT;
        simpleFleet.assignmentText = "intercepting your fleet";
        simpleFleet.interceptPlayer = true;
        simpleFleet.noTransponder = true;
        CampaignFleetAPI fleet = simpleFleet.create();

        //custom spawning
        final Vector2f fleetLoc = new Vector2f(MathUtils.getPointOnCircumference(pf.getLocation(), (pf.getSensorStrength()*0.90f)+(fleet.getSensorProfile()*0.90f), random.nextFloat() * 360.0f));
        fleet.setLocation(fleetLoc.x, fleetLoc.y);
        fleet.setFacing(random.nextFloat() * 360.0f);

        log("tt collector SPAWNED " + fleet.getName() + " size " + combatPoints);
        return fleet;
    }

    //fleets for job 4
    public static CampaignFleetAPI  spawnJob4Splinters(){
        Random random = nskr_kestevenQuest.getRandom();
        StarSystemAPI target = questUtil.getJob4FriendlyTarget().getStarSystem();
        //don't spawn in the same system as the friendly fleet
        StarSystemAPI origin = questUtil.getRandomSystemWithinConstellation(questUtil.getJob4FriendlyTarget().getConstellation(), target, 1, random);
        SectorEntityToken loc = util.getRandomLocationInSystem(origin, true, true, random);

        float combatPoints = mathUtil.getSeededRandomNumberInRange(8f, 25f, random);

        //apply settings
        combatPoints *= nskr_modPlugin.getScriptedFleetSizeMult();

        ArrayList<String> keys = new ArrayList<>();
        //aggro
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(questStageManager.JOB4_SPLINTER_KEY);

        simpleFleet simpleFleet = new simpleFleet(loc, "enigma", combatPoints, keys, random);
        simpleFleet.aiFleetProperties = true;
        simpleFleet.name = "Splinter"+" "+util.getRandomGreekLetter(random, true);
        simpleFleet.assignment = FleetAssignment.PATROL_SYSTEM;
        simpleFleet.assignmentText = "seeking";
        CampaignFleetAPI fleet = simpleFleet.create();

        //makes sure we are not in a star
        questUtil.spawnAwayFromStarFixer(fleet);

        //add to mem IMPORTANT
        List<fleetInfo> fleets = fleetUtil.getFleets(questStageManager.FLEET_ARRAY_KEY);
        fleets.add(new fleetInfo(fleet, null, loc));
        fleetUtil.setFleets(fleets, questStageManager.FLEET_ARRAY_KEY);

        log("splinter SPAWNED " + fleet.getName() + " size " + combatPoints +" in "+ origin.getName()+" to "+ loc.getName());
        return fleet;
    }

    //target for job 4
    public static CampaignFleetAPI spawnJob4Target(){
        Random random = nskr_kestevenQuest.getRandom();

        StarSystemAPI target = questUtil.getJob4FriendlyTarget().getStarSystem();
        //don't spawn in the same system as the friendly fleet
        StarSystemAPI origin = questUtil.getRandomSystemWithinConstellation(target.getConstellation(), target, 2, random);
        SectorEntityToken loc = util.getRandomLocationInSystem(origin, false,false, random);

        //save loc to memory IMPORTANT
        questUtil.setJob4EnemyTarget(loc);

        float combatPoints = mathUtil.getSeededRandomNumberInRange(40f, 45f, random);

        //apply settings
        combatPoints *= nskr_modPlugin.getScriptedFleetSizeMult();

        //skills
        Map<String, Integer> skills = new HashMap<>();
        skills.put("combat_endurance",2);
        skills.put("damage_control",2);
        skills.put("field_modulation",2);
        skills.put("target_analysis",2);
        skills.put("systems_expertise",2);
        skills.put(Skills.BALLISTIC_MASTERY,2);
        skills.put(Skills.IMPACT_MITIGATION,2);
        skills.put("ordnance_expert",2);
        if (nskr_modPlugin.getStarfarerMode()) skills.put(Skills.POLARIZED_ARMOR,2);
        //com skills
        skills.put("electronic_warfare",1);
        skills.put("crew_training",1);
        //memkeys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
        keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
        keys.add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        keys.add(questStageManager.JOB4_TARGET_KEY);

        //flagship
        simpleFleetMember flagship = new simpleFleetMember("nskr_minokawa_e_boss", new ArrayList<String>(), true);
        flagship.alwaysRecover = true;
        flagship.name = "DSRD Eye for an eye";

        //commander
        simpleCaptain simpleCaptain = new simpleCaptain("nskr_" + "Enforcer-Unit", "enigma", skills);
        simpleCaptain.isAiCore = true;
        simpleCaptain.aiCoreID = "alpha_core";
        simpleCaptain.personality = Personalities.RECKLESS;
        simpleCaptain.portraitSpritePath = "graphics/portraits/nskr_alpha_core1.png";
        simpleCaptain.rankId = Ranks.SPACE_ADMIRAL;
        simpleCaptain.firstName = "Enforcer-Unit";
        simpleCaptain.lastName = util.getRandomGreekLetter(random, true);

        //fleet
        simpleFleet simpleFleet = new simpleFleet(loc, "enigma", combatPoints, keys, random);
        simpleFleet.aiFleetProperties = true;
        //simpleFleet.type = FleetTypes.PATROL_LARGE;
        simpleFleet.maxShipSize = 2;
        simpleFleet.commander = simpleCaptain.create();
        simpleFleet.flagshipInfo = flagship;
        simpleFleet.name = "Strike Group"+" "+util.getRandomGreekLetter(random, true);
        simpleFleet.assignment = FleetAssignment.ORBIT_AGGRESSIVE;
        simpleFleet.assignmentText = "unknown";
        CampaignFleetAPI fleet = simpleFleet.create();

        //makes sure we are not in a star
        questUtil.spawnAwayFromStarFixer(fleet, 2.0f);

        //add to mem IMPORTANT
        List<fleetInfo> fleets = fleetUtil.getFleets(questStageManager.FLEET_ARRAY_KEY);
        fleetInfo info = new fleetInfo(fleet, null, loc);
        info.flagshipSimpleMember = simpleFleet.getFlagshipInfo();
        info.secondaries = simpleFleet.getSecondaryMembers();
        fleets.add(info);
        fleetUtil.setFleets(fleets, questStageManager.FLEET_ARRAY_KEY);

        log("job4Target SPAWNED " + fleet.getName() + " size " + combatPoints +" in "+ origin.getName()+" to "+ loc.getName());
        return fleet;
    }

    //friendly for job 4
    public static CampaignFleetAPI spawnJob4Friendly(){
        Random random = nskr_kestevenQuest.getRandom();

        StarSystemAPI origin = questUtil.getJob4FriendlyTarget().getStarSystem();
        SectorEntityToken loc = questUtil.getJob4FriendlyTarget();

        float combatPoints = mathUtil.getSeededRandomNumberInRange(45f, 55f, random);

        //apply settings
        combatPoints *= nskr_modPlugin.getScriptedFleetSizeMult();

        //keys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.MEMORY_KEY_FORCE_TRANSPONDER_OFF);
        keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
        keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
        keys.add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        keys.add(questStageManager.JOB4_FRIENDLY_KEY);
        //fleet
        simpleFleet simpleFleet = new simpleFleet(loc, "kesteven", combatPoints, keys, random);
        //simpleFleet.type = FleetTypes.PATROL_MEDIUM;
        simpleFleet.freighterPoints = combatPoints/4f;
        simpleFleet.tankerPoints = combatPoints/6f;
        simpleFleet.linerPoints = combatPoints/5f;
        simpleFleet.utilityPoints = combatPoints/6f;
        simpleFleet.maxShipSize = 3;
        simpleFleet.sMods = 1;
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.name = "Special Operations";
        simpleFleet.noTransponder = true;
        simpleFleet.assignment = FleetAssignment.ORBIT_PASSIVE;
        simpleFleet.assignmentText = "holding";
        CampaignFleetAPI fleet = simpleFleet.create();

        //makes sure we are not in a star
        questUtil.spawnAwayFromStarFixer(fleet, 1.5f);

        log("job4Friendly SPAWNED " + fleet.getName() + " size " + combatPoints +" in "+ origin.getName()+" to "+ loc.getName());
        return fleet;
    }

    //target fleet for job 3
    public static CampaignFleetAPI spawnJob3TargetFleet(){
        Random random = nskr_kestevenQuest.getRandom();
        SectorEntityToken loc = questUtil.getJob3Start();

        float combatPoints = mathUtil.getSeededRandomNumberInRange(130f, 140f, random);

        //apply settings
        combatPoints *= nskr_modPlugin.getScriptedFleetSizeMult();

        //keys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
        keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
        keys.add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        keys.add(questStageManager.JOB3_TARGET_KEY);
        //fleet
        simpleFleet simpleFleet = new simpleFleet(loc, Factions.TRITACHYON, combatPoints, keys, random);
        //simpleFleet.type = FleetTypes.PATROL_LARGE;
        simpleFleet.freighterPoints = combatPoints/4f;
        simpleFleet.tankerPoints = combatPoints/4f;
        simpleFleet.linerPoints = combatPoints/8f;
        simpleFleet.utilityPoints = combatPoints/8f;
        simpleFleet.maxShipSize = 3;
        // no avg Smods cause apparently it gets *weird* with civvie ships in fleet
        // simpleFleet.sMods = 1;
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.name = "Expedition";
        simpleFleet.assignment = FleetAssignment.ORBIT_PASSIVE;
        simpleFleet.assignmentText = "preparing";
        CampaignFleetAPI fleet = simpleFleet.create();

        log("job3Target " + fleet.getName() + " size " + combatPoints);
        return fleet;
    }
}
