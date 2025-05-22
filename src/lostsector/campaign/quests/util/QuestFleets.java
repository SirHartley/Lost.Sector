package lostsector.campaign.quests.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import lostsector.campaign.rulecmd.KestevenQuest;
import lostsector.campaign.rulecmd.TtCollectorDialog;
import lostsector.ModPlugin;
import lostsector.util.FleetUtil;
import lostsector.util.MathUtilLS;
import lostsector.util.MiscLS;
import lostsector.util.PowerLevel;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class QuestFleets {

    static void log(final String message) {
        Global.getLogger(QuestFleets.class).info(message);
    }

    public static final String FLEET_NAME = "Eliza's Merc Armada";
    public static final String FLAGSHIP_VARIANT = "nskr_onslaught_boss";
    public static final String FS_NAME = "Regicide";
    public static final String ELIZA_RAIDED_FLEET_KEY = "$ElizaFleet";
    //ELIZA FLEET
    public static CampaignFleetAPI spawnElizaFleet(SectorEntityToken loc, PersonAPI eliza, Random random, boolean revengeance, boolean intercept) {

        float points = MathUtilLS.getSeededRandomNumberInRange(190f,200f, random);

        //apply settings
        points *= ModPlugin.getScriptedFleetSizeMult();

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
        MiscLS.setOfficerSkills(eliza, skills);
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
            keys.add(QuestStageManager.REVENGEANCE_FLEET_KEY);
        }
        else if (intercept){
            keys.add(QuestStageManager.ELIZA_INTERCEPT_FLEET_KEY);
        }

        //permamods
        List<String> permamods = new ArrayList<>();
        permamods.add(HullMods.HEAVYARMOR);
        //flagship
        SimpleFleetMember flagship = new SimpleFleetMember(FLAGSHIP_VARIANT, new ArrayList<String>(), true);
        flagship.alwaysRecover = true;
        flagship.hullmods = permamods;
        flagship.name = FS_NAME;

        //fleet
        SimpleFleet simpleFleet = new SimpleFleet(loc, Factions.MERCENARY, points, keys, random);
        //SimpleFleet.type = FleetTypes.PATROL_LARGE;
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
        FleetUtil.update(fleet, random);


        //add to mem IMPORTANT
        List<FleetInfo> fleets = FleetUtil.getFleets(QuestStageManager.FLEET_ARRAY_KEY);
        FleetInfo info = new FleetInfo(fleet, null, loc);
        info.flagshipSimpleMember = simpleFleet.getFlagshipInfo();
        info.secondaries = simpleFleet.getSecondaryMembers();
        fleets.add(info);
        FleetUtil.setFleets(fleets, QuestStageManager.FLEET_ARRAY_KEY);

        log("Eliza SPAWNED, size " + points + " loc " + loc.getName() + " system " + loc.getContainingLocation().getName());
        log("Eliza FLEET, loc " + fleet.getStarSystem().getName() +" size "+ fleet.getFleetPoints() + " commander " + fleet.getCommander().getName().getFullName() + " flagship " + fleet.getFlagship().getHullSpec().getBaseHullId());
        return fleet;
    }

    //JACK FLEET
    public static CampaignFleetAPI spawnJackFleet(SectorEntityToken loc, PersonAPI jack, Random random) {

        float points = MathUtilLS.getSeededRandomNumberInRange(190f,200f, random);

        //apply settings
        points *= ModPlugin.getScriptedFleetSizeMult();

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
        keys.add(QuestStageManager.JACK_REVENGEANCE_FLEET_KEY);
        keys.add(QuestStageManager.REVENGEANCE_FLEET_KEY);

        //permamods
        List<String> permamods = new ArrayList<>();
        permamods.add(HullMods.HEAVYARMOR);
        //flagship
        SimpleFleetMember flagship = new SimpleFleetMember("nskr_prosperity_boss", new ArrayList<String>(), true);
        flagship.alwaysRecover = true;
        flagship.hullmods = permamods;
        flagship.name = "K-Corp Homewrecker";

        //fleet
        SimpleFleet simpleFleet = new SimpleFleet(loc, "kesteven", points, keys, random);
        //SimpleFleet.type = FleetTypes.PATROL_LARGE;
        simpleFleet.maxShipSize = 3;
        simpleFleet.sMods = 3;
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.commander = MiscLS.setOfficerSkills(jack, skills);
        simpleFleet.flagshipInfo = flagship;
        simpleFleet.name = "Task Force";
        simpleFleet.assignment = FleetAssignment.ORBIT_PASSIVE;
        simpleFleet.assignmentText = "holding";
        CampaignFleetAPI fleet = simpleFleet.create();

        //add to mem IMPORTANT
        List<FleetInfo> fleets = FleetUtil.getFleets(QuestStageManager.FLEET_ARRAY_KEY);
        FleetInfo info = new FleetInfo(fleet, null, loc);
        info.flagshipSimpleMember = simpleFleet.getFlagshipInfo();
        info.secondaries = simpleFleet.getSecondaryMembers();
        fleets.add(info);
        FleetUtil.setFleets(fleets, QuestStageManager.FLEET_ARRAY_KEY);

        log("Jack SPAWNED, size " + points + " loc " + loc.getName() + " system " + loc.getContainingLocation().getName());
        log("Jack FLEET, loc " + fleet.getStarSystem().getName() +" size "+ fleet.getFleetPoints() + " commander " + fleet.getCommander().getName().getFullName() + " flagship " + fleet.getFlagship().getHullSpec().getBaseHullId());
        return fleet;
    }

    //tt collector fleet
    public static CampaignFleetAPI spawnCollectorFleet() {
        Random random = TtCollectorDialog.getRandom();
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        float combatPoints = MathUtilLS.getSeededRandomNumberInRange(100f, 120f, random);
        //power scaling
        combatPoints += combatPoints * PowerLevel.get(0.2f, 0f,1f);
        log("tt collector BASE " + combatPoints);

        //apply settings
        combatPoints *= ModPlugin.getScriptedFleetSizeMult();

        SectorEntityToken loc = pf.getContainingLocation().createToken(pf.getLocation());

        //keys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOSTILE);
        keys.add(MemFlags.MEMORY_KEY_LOW_REP_IMPACT);
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);
        keys.add(MemFlags.MEMORY_KEY_NEVER_AVOID_PLAYER_SLOWLY);
        keys.add(QuestStageManager.TT_COLLECTOR_KEY);

        //fleet
        SimpleFleet simpleFleet = new SimpleFleet(loc, Factions.TRITACHYON, combatPoints, keys, random);
        //SimpleFleet.type = FleetTypes.PATROL_LARGE;
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
        Random random = KestevenQuest.getRandom();
        StarSystemAPI target = QuestUtil.getJob4FriendlyTarget().getStarSystem();
        //don't spawn in the same system as the friendly fleet
        StarSystemAPI origin = QuestUtil.getRandomSystemWithinConstellation(QuestUtil.getJob4FriendlyTarget().getConstellation(), target, 1, random);
        SectorEntityToken loc = MiscLS.getRandomLocationInSystem(origin, true, true, random);

        float combatPoints = MathUtilLS.getSeededRandomNumberInRange(8f, 25f, random);

        //apply settings
        combatPoints *= ModPlugin.getScriptedFleetSizeMult();

        ArrayList<String> keys = new ArrayList<>();
        //aggro
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(QuestStageManager.JOB4_SPLINTER_KEY);

        SimpleFleet simpleFleet = new SimpleFleet(loc, "enigma", combatPoints, keys, random);
        simpleFleet.aiFleetProperties = true;
        simpleFleet.name = "Splinter"+" "+ MiscLS.getRandomGreekLetter(random, true);
        simpleFleet.assignment = FleetAssignment.PATROL_SYSTEM;
        simpleFleet.assignmentText = "seeking";
        CampaignFleetAPI fleet = simpleFleet.create();

        //makes sure we are not in a star
        QuestUtil.spawnAwayFromStarFixer(fleet);

        //add to mem IMPORTANT
        List<FleetInfo> fleets = FleetUtil.getFleets(QuestStageManager.FLEET_ARRAY_KEY);
        fleets.add(new FleetInfo(fleet, null, loc));
        FleetUtil.setFleets(fleets, QuestStageManager.FLEET_ARRAY_KEY);

        log("splinter SPAWNED " + fleet.getName() + " size " + combatPoints +" in "+ origin.getName()+" to "+ loc.getName());
        return fleet;
    }

    //target for job 4
    public static CampaignFleetAPI spawnJob4Target(){
        Random random = KestevenQuest.getRandom();

        StarSystemAPI target = QuestUtil.getJob4FriendlyTarget().getStarSystem();
        //don't spawn in the same system as the friendly fleet
        StarSystemAPI origin = QuestUtil.getRandomSystemWithinConstellation(target.getConstellation(), target, 2, random);
        SectorEntityToken loc = MiscLS.getRandomLocationInSystem(origin, false,false, random);

        //save loc to memory IMPORTANT
        QuestUtil.setJob4EnemyTarget(loc);

        float combatPoints = MathUtilLS.getSeededRandomNumberInRange(40f, 45f, random);

        //apply settings
        combatPoints *= ModPlugin.getScriptedFleetSizeMult();

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
        if (ModPlugin.getStarfarerMode()) skills.put(Skills.POLARIZED_ARMOR,2);
        //com skills
        skills.put("electronic_warfare",1);
        skills.put("crew_training",1);
        //memkeys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
        keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
        keys.add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        keys.add(QuestStageManager.JOB4_TARGET_KEY);

        //flagship
        SimpleFleetMember flagship = new SimpleFleetMember("nskr_minokawa_e_boss", new ArrayList<String>(), true);
        flagship.alwaysRecover = true;
        flagship.name = "DSRD Eye for an eye";

        //commander
        SimpleCaptain simpleCaptain = new SimpleCaptain("nskr_" + "Enforcer-Unit", "enigma", skills);
        simpleCaptain.isAiCore = true;
        simpleCaptain.aiCoreID = "alpha_core";
        simpleCaptain.personality = Personalities.RECKLESS;
        simpleCaptain.portraitSpritePath = "graphics/portraits/nskr_alpha_core1.png";
        simpleCaptain.rankId = Ranks.SPACE_ADMIRAL;
        simpleCaptain.firstName = "Enforcer-Unit";
        simpleCaptain.lastName = MiscLS.getRandomGreekLetter(random, true);

        //fleet
        SimpleFleet simpleFleet = new SimpleFleet(loc, "enigma", combatPoints, keys, random);
        simpleFleet.aiFleetProperties = true;
        //SimpleFleet.type = FleetTypes.PATROL_LARGE;
        simpleFleet.maxShipSize = 2;
        simpleFleet.commander = simpleCaptain.create();
        simpleFleet.flagshipInfo = flagship;
        simpleFleet.name = "Strike Group"+" "+ MiscLS.getRandomGreekLetter(random, true);
        simpleFleet.assignment = FleetAssignment.ORBIT_AGGRESSIVE;
        simpleFleet.assignmentText = "unknown";
        CampaignFleetAPI fleet = simpleFleet.create();

        //makes sure we are not in a star
        QuestUtil.spawnAwayFromStarFixer(fleet, 2.0f);

        //add to mem IMPORTANT
        List<FleetInfo> fleets = FleetUtil.getFleets(QuestStageManager.FLEET_ARRAY_KEY);
        FleetInfo info = new FleetInfo(fleet, null, loc);
        info.flagshipSimpleMember = simpleFleet.getFlagshipInfo();
        info.secondaries = simpleFleet.getSecondaryMembers();
        fleets.add(info);
        FleetUtil.setFleets(fleets, QuestStageManager.FLEET_ARRAY_KEY);

        log("job4Target SPAWNED " + fleet.getName() + " size " + combatPoints +" in "+ origin.getName()+" to "+ loc.getName());
        return fleet;
    }

    //friendly for job 4
    public static CampaignFleetAPI spawnJob4Friendly(){
        Random random = KestevenQuest.getRandom();

        StarSystemAPI origin = QuestUtil.getJob4FriendlyTarget().getStarSystem();
        SectorEntityToken loc = QuestUtil.getJob4FriendlyTarget();

        float combatPoints = MathUtilLS.getSeededRandomNumberInRange(45f, 55f, random);

        //apply settings
        combatPoints *= ModPlugin.getScriptedFleetSizeMult();

        //keys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.MEMORY_KEY_FORCE_TRANSPONDER_OFF);
        keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
        keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
        keys.add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        keys.add(QuestStageManager.JOB4_FRIENDLY_KEY);
        //fleet
        SimpleFleet simpleFleet = new SimpleFleet(loc, "kesteven", combatPoints, keys, random);
        //SimpleFleet.type = FleetTypes.PATROL_MEDIUM;
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
        QuestUtil.spawnAwayFromStarFixer(fleet, 1.5f);

        log("job4Friendly SPAWNED " + fleet.getName() + " size " + combatPoints +" in "+ origin.getName()+" to "+ loc.getName());
        return fleet;
    }

    //target fleet for job 3
    public static CampaignFleetAPI spawnJob3TargetFleet(){
        Random random = KestevenQuest.getRandom();
        SectorEntityToken loc = QuestUtil.getJob3Start();

        float combatPoints = MathUtilLS.getSeededRandomNumberInRange(130f, 140f, random);

        //apply settings
        combatPoints *= ModPlugin.getScriptedFleetSizeMult();

        //keys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
        keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
        keys.add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        keys.add(QuestStageManager.JOB3_TARGET_KEY);
        //fleet
        SimpleFleet simpleFleet = new SimpleFleet(loc, Factions.TRITACHYON, combatPoints, keys, random);
        //SimpleFleet.type = FleetTypes.PATROL_LARGE;
        simpleFleet.freighterPoints = combatPoints/4f;
        simpleFleet.tankerPoints = combatPoints/4f;
        simpleFleet.linerPoints = combatPoints/8f;
        simpleFleet.utilityPoints = combatPoints/8f;
        simpleFleet.maxShipSize = 3;
        // no avg Smods cause apparently it gets *weird* with civvie ships in fleet
        // SimpleFleet.sMods = 1;
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.name = "Expedition";
        simpleFleet.assignment = FleetAssignment.ORBIT_PASSIVE;
        simpleFleet.assignmentText = "preparing";
        CampaignFleetAPI fleet = simpleFleet.create();

        log("job3Target " + fleet.getName() + " size " + combatPoints);
        return fleet;
    }
}
