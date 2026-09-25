package lostsector.campaign.kesteven.quest;

import lostsector.helper.fleet.FleetInfo;
import lostsector.helper.fleet.SimpleCaptain;
import lostsector.helper.fleet.SimpleFleet;
import lostsector.helper.fleet.SimpleFleetMember;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import lostsector.dialogue.rules.nskr_kestevenQuest;
import lostsector.dialogue.rules.nskr_ttCollectorDialog;
import lostsector.settings.Difficulty;
import lostsector.helper.FleetHelper;
import lostsector.helper.MathHelper;
import lostsector.helper.ShipHelper;
import lostsector.helper.StringHelper;
import lostsector.helper.SystemHelper;
import lostsector.helper.PowerLevel;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class KestevenFleets {

    static void log(final String message) {
        Global.getLogger(KestevenFleets.class).info(message);
    }

    public static final String FLEET_NAME = "Eliza's Merc Armada";
    public static final String FLAGSHIP_VARIANT = "nskr_onslaught_boss";
    public static final String FS_NAME = "Regicide";
    public static final String ELIZA_RAIDED_FLEET_KEY = "$ElizaFleet";
    //ELIZA FLEET
    public static CampaignFleetAPI spawnElizaFleet(SectorEntityToken loc, PersonAPI eliza, Random random, boolean revengeance, boolean intercept) {

        float points = MathHelper.getSeededRandomNumberInRange(190f,200f, random);

        //apply settings
        points *= Difficulty.scriptedFleetMult();

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
        ShipHelper.setOfficerSkills(eliza, skills);
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
        FleetHelper.update(fleet, random);


        //add to mem IMPORTANT
        List<FleetInfo> fleets = FleetHelper.getFleets(QuestStageManager.FLEET_ARRAY_KEY);
        FleetInfo info = new FleetInfo(fleet, null, loc);
        info.flagshipSimpleMember = simpleFleet.getFlagshipInfo();
        info.secondaries = simpleFleet.getSecondaryMembers();
        fleets.add(info);
        FleetHelper.setFleets(fleets, QuestStageManager.FLEET_ARRAY_KEY);

        log("Eliza SPAWNED, size " + points + " loc " + loc.getName() + " system " + loc.getContainingLocation().getName());
        log("Eliza FLEET, loc " + fleet.getStarSystem().getName() +" size "+ fleet.getFleetPoints() + " commander " + fleet.getCommander().getName().getFullName() + " flagship " + fleet.getFlagship().getHullSpec().getBaseHullId());
        return fleet;
    }

    //JACK FLEET
    public static CampaignFleetAPI spawnJackFleet(SectorEntityToken loc, PersonAPI jack, Random random) {

        float points = MathHelper.getSeededRandomNumberInRange(190f,200f, random);

        //apply settings
        points *= Difficulty.scriptedFleetMult();

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
        simpleFleet.maxShipSize = 3;
        simpleFleet.sMods = 3;
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.commander = ShipHelper.setOfficerSkills(jack, skills);
        simpleFleet.flagshipInfo = flagship;
        simpleFleet.name = "Task Force";
        simpleFleet.assignment = FleetAssignment.ORBIT_PASSIVE;
        simpleFleet.assignmentText = "holding";
        CampaignFleetAPI fleet = simpleFleet.create();

        //add to mem IMPORTANT
        List<FleetInfo> fleets = FleetHelper.getFleets(QuestStageManager.FLEET_ARRAY_KEY);
        FleetInfo info = new FleetInfo(fleet, null, loc);
        info.flagshipSimpleMember = simpleFleet.getFlagshipInfo();
        info.secondaries = simpleFleet.getSecondaryMembers();
        fleets.add(info);
        FleetHelper.setFleets(fleets, QuestStageManager.FLEET_ARRAY_KEY);

        log("Jack SPAWNED, size " + points + " loc " + loc.getName() + " system " + loc.getContainingLocation().getName());
        log("Jack FLEET, loc " + fleet.getStarSystem().getName() +" size "+ fleet.getFleetPoints() + " commander " + fleet.getCommander().getName().getFullName() + " flagship " + fleet.getFlagship().getHullSpec().getBaseHullId());
        return fleet;
    }

    //tt collector fleet
    public static CampaignFleetAPI spawnCollectorFleet() {
        Random random = nskr_ttCollectorDialog.getRandom();
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        float combatPoints = MathHelper.getSeededRandomNumberInRange(100f, 120f, random);
        //power scaling
        combatPoints += combatPoints * PowerLevel.get(0.2f, 0f,1f);
        log("tt collector BASE " + combatPoints);

        //apply settings
        combatPoints *= Difficulty.scriptedFleetMult();

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
        StarSystemAPI target = QuestHelper.getJob4FriendlyTarget().getStarSystem();
        //don't spawn in the same system as the friendly fleet
        StarSystemAPI origin = QuestHelper.getRandomSystemWithinConstellation(QuestHelper.getJob4FriendlyTarget().getConstellation(), target, 1, random);
        SectorEntityToken loc = SystemHelper.getRandomLocationInSystem(origin, true, true, random);

        float combatPoints = MathHelper.getSeededRandomNumberInRange(8f, 25f, random);

        //apply settings
        combatPoints *= Difficulty.scriptedFleetMult();

        ArrayList<String> keys = new ArrayList<>();
        //aggro
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(QuestStageManager.JOB4_SPLINTER_KEY);

        SimpleFleet simpleFleet = new SimpleFleet(loc, "enigma", combatPoints, keys, random);
        simpleFleet.aiFleetProperties = true;
        simpleFleet.name = "Splinter"+" "+ StringHelper.getRandomGreekLetter(random, true);
        simpleFleet.assignment = FleetAssignment.PATROL_SYSTEM;
        simpleFleet.assignmentText = "seeking";
        CampaignFleetAPI fleet = simpleFleet.create();

        //makes sure we are not in a star
        SystemHelper.spawnAwayFromStarFixer(fleet);

        //add to mem IMPORTANT
        List<FleetInfo> fleets = FleetHelper.getFleets(QuestStageManager.FLEET_ARRAY_KEY);
        fleets.add(new FleetInfo(fleet, null, loc));
        FleetHelper.setFleets(fleets, QuestStageManager.FLEET_ARRAY_KEY);

        log("splinter SPAWNED " + fleet.getName() + " size " + combatPoints +" in "+ origin.getName()+" to "+ loc.getName());
        return fleet;
    }

    //target for job 4
    public static CampaignFleetAPI spawnJob4Target(){
        Random random = nskr_kestevenQuest.getRandom();

        StarSystemAPI target = QuestHelper.getJob4FriendlyTarget().getStarSystem();
        //don't spawn in the same system as the friendly fleet
        StarSystemAPI origin = QuestHelper.getRandomSystemWithinConstellation(target.getConstellation(), target, 2, random);
        SectorEntityToken loc = SystemHelper.getRandomLocationInSystem(origin, false,false, random);

        //save loc to memory IMPORTANT
        QuestHelper.setJob4EnemyTarget(loc);

        float combatPoints = MathHelper.getSeededRandomNumberInRange(40f, 45f, random);

        //apply settings
        combatPoints *= Difficulty.scriptedFleetMult();

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
        if (Difficulty.isStarfarer()) skills.put(Skills.POLARIZED_ARMOR,2);
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
        simpleCaptain.portraitSpritePath = "graphics/lostsector/portraits/nskr_alpha_core1.png";
        simpleCaptain.rankId = Ranks.SPACE_ADMIRAL;
        simpleCaptain.firstName = "Enforcer-Unit";
        simpleCaptain.lastName = StringHelper.getRandomGreekLetter(random, true);

        //fleet
        SimpleFleet simpleFleet = new SimpleFleet(loc, "enigma", combatPoints, keys, random);
        simpleFleet.aiFleetProperties = true;
        simpleFleet.maxShipSize = 2;
        simpleFleet.commander = simpleCaptain.create();
        simpleFleet.flagshipInfo = flagship;
        simpleFleet.name = "Strike Group"+" "+ StringHelper.getRandomGreekLetter(random, true);
        simpleFleet.assignment = FleetAssignment.ORBIT_AGGRESSIVE;
        simpleFleet.assignmentText = "unknown";
        CampaignFleetAPI fleet = simpleFleet.create();

        //makes sure we are not in a star
        SystemHelper.spawnAwayFromStarFixer(fleet, 2.0f);

        //add to mem IMPORTANT
        List<FleetInfo> fleets = FleetHelper.getFleets(QuestStageManager.FLEET_ARRAY_KEY);
        FleetInfo info = new FleetInfo(fleet, null, loc);
        info.flagshipSimpleMember = simpleFleet.getFlagshipInfo();
        info.secondaries = simpleFleet.getSecondaryMembers();
        fleets.add(info);
        FleetHelper.setFleets(fleets, QuestStageManager.FLEET_ARRAY_KEY);

        log("job4Target SPAWNED " + fleet.getName() + " size " + combatPoints +" in "+ origin.getName()+" to "+ loc.getName());
        return fleet;
    }

    //friendly for job 4
    public static CampaignFleetAPI spawnJob4Friendly(){
        Random random = nskr_kestevenQuest.getRandom();

        StarSystemAPI origin = QuestHelper.getJob4FriendlyTarget().getStarSystem();
        SectorEntityToken loc = QuestHelper.getJob4FriendlyTarget();

        float combatPoints = MathHelper.getSeededRandomNumberInRange(45f, 55f, random);

        //apply settings
        combatPoints *= Difficulty.scriptedFleetMult();

        //keys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.MEMORY_KEY_FORCE_TRANSPONDER_OFF);
        keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
        keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
        keys.add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        keys.add(QuestStageManager.JOB4_FRIENDLY_KEY);
        //fleet
        SimpleFleet simpleFleet = new SimpleFleet(loc, "kesteven", combatPoints, keys, random);
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
        SystemHelper.spawnAwayFromStarFixer(fleet, 1.5f);

        log("job4Friendly SPAWNED " + fleet.getName() + " size " + combatPoints +" in "+ origin.getName()+" to "+ loc.getName());
        return fleet;
    }

    //target fleet for job 3
    public static CampaignFleetAPI spawnJob3TargetFleet(){
        Random random = nskr_kestevenQuest.getRandom();
        SectorEntityToken loc = QuestHelper.getJob3Start();

        float combatPoints = MathHelper.getSeededRandomNumberInRange(130f, 140f, random);

        //apply settings
        combatPoints *= Difficulty.scriptedFleetMult();

        //keys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
        keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
        keys.add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        keys.add(QuestStageManager.JOB3_TARGET_KEY);
        //fleet
        SimpleFleet simpleFleet = new SimpleFleet(loc, Factions.TRITACHYON, combatPoints, keys, random);
        simpleFleet.freighterPoints = combatPoints/4f;
        simpleFleet.tankerPoints = combatPoints/4f;
        simpleFleet.linerPoints = combatPoints/8f;
        simpleFleet.utilityPoints = combatPoints/8f;
        simpleFleet.maxShipSize = 3;
        // no avg Smods cause apparently it gets *weird* with civvie ships in fleet
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.name = "Expedition";
        simpleFleet.assignment = FleetAssignment.ORBIT_PASSIVE;
        simpleFleet.assignmentText = "preparing";
        CampaignFleetAPI fleet = simpleFleet.create();

        log("job3Target " + fleet.getName() + " size " + combatPoints);
        return fleet;
    }
}
