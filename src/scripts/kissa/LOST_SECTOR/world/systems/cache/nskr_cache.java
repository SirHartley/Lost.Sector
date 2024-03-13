package scripts.kissa.LOST_SECTOR.world.systems.cache;

import java.awt.Color;
import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.util.MagicCampaign;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.*;
import scripts.kissa.LOST_SECTOR.campaign.quests.nskr_artifactDialog;
import scripts.kissa.LOST_SECTOR.campaign.quests.nskr_coreDialog;
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.nskr_kestevenQuest;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.util;

public class nskr_cache {

    public static final String FLEET_NAME = "Cache Guardian";
    public static final String COM_NAME = "Enigma Fragment";
    public static final String COM_LAST_NAME = "#1";
    public static final String FLAGSHIP_VARIANT = "nskr_minokawa_boss";
    public static final String SECONDARY_VARIANT_1 = "nskr_eternity_boss";
    public static final String SECONDARY_VARIANT_2 = "nskr_sovereign_boss";
    public static final String SECONDARY_VARIANT_3 = "nskr_nemesis_boss";
    public static final String SECONDARY_VARIANT_4 = "nskr_muninn_boss";
    public static final String SECONDARY_VARIANT_5 = "nskr_epochx_boss";
    public static final String SECONDARY_VARIANT_6 = "nskr_epoch_boss";
    public static final String SECONDARY_VARIANT_7 = "nskr_torpor_boss";
    public static final String SECONDARY_VARIANT_8 = "nskr_warfare_boss";
    public static final String SECONDARY_VARIANT_9 = "nskr_widow_boss";
    public static final String FS_NAME = "DSRD Epicenter";
    public static final String S1_NAME = "DSRD Terminal";
    public static final String S2_NAME = "DSRD Ultimate";
    public static final String S3_NAME = "DSRD Verge";
    public static final String S4_NAME = "DSRD Threshold";
    public static final String S5_NAME = "DSRD Salient";
    public static final String S6_NAME = "DSRD Terminus";
    public static final String S7_NAME = "DSRD Boundary";
    public static final String S8_NAME = "DSRD Brink";
    public static final String S9_NAME = "DSRD Periphery";
    public static final Map<String, Integer> OFFICER_SKILLS = new HashMap<>();
    static {
        OFFICER_SKILLS.put("combat_endurance",2);
        OFFICER_SKILLS.put("damage_control",2);
        OFFICER_SKILLS.put("field_modulation",2);
        OFFICER_SKILLS.put("target_analysis",2);
        OFFICER_SKILLS.put("systems_expertise",2);
        OFFICER_SKILLS.put("missile_specialization",2);
        OFFICER_SKILLS.put("energy_weapon_mastery",2);
        OFFICER_SKILLS.put(Skills.BALLISTIC_MASTERY,2);
        OFFICER_SKILLS.put("impact_mitigation",2);
    }

    public static final String CACHE_FLEET_KEY = "$CacheGuardianFleet";
    public static final String CORE_KEY = "$CacheCoreKey";
    public static String NASCENT_WELL_KEY = "$nskr_CacheWell";

    static void log(final String message) {
        Global.getLogger(nskr_cache.class).info(message);
    }

    public static void generate(SectorAPI sector) {
        StarSystemAPI system = sector.createStarSystem("Unknown Site");

        system.setName("Unknown Site"); // to get rid of "Star System" at the end of the name
        system.addTag(Tags.THEME_UNSAFE);
        system.addTag(Tags.THEME_HIDDEN);
        LocationAPI hyper = Global.getSector().getHyperspace();

        //system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "nskr_cache_theme");

        system.setBackgroundTextureFilename("graphics/backgrounds/nskr_cache.jpg");
        //LOC
        system.getLocation().set(createCacheLoc(50000f, 65000f, 10000f));

        HyperspaceTerrainPlugin hyperTerrain = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(hyperTerrain);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0f, 2000f, 0f, 360f);

        SectorEntityToken center = system.initNonStarCenter();

        system.generateAnchorIfNeeded();
        //GRAVITY WELL
        NascentGravityWellAPI well = Global.getSector().createNascentGravityWell(center, 50f);
        well.addTag(Tags.NO_ENTITY_TOOLTIP);
        well.setColorOverride(new Color(125, 50, 255));
        hyper.addEntity(well);
        well.autoUpdateHyperLocationBasedOnInSystemEntityAtRadius(center, 0);

        Global.getSector().getMemoryWithoutUpdate().set(NASCENT_WELL_KEY, well);

        //music mem key
        for(SectorEntityToken e : system.getAllEntities()){
            e.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY, true);
        }
    }

    public static NascentGravityWellAPI getWell() {
        return (NascentGravityWellAPI) Global.getSector().getMemoryWithoutUpdate().get(NASCENT_WELL_KEY);
    }

    //spawning everything after the fight
    public static void spawnEverything(StarSystemAPI system){
        SectorEntityToken center = system.initNonStarCenter();

        //GATE
        //CENTRE OF THE SYSTEM
        CustomCampaignEntityAPI gate = system.addCustomEntity("nsrk_cacheGate", "Cache Gate", Entities.INACTIVE_GATE, Factions.NEUTRAL);

        gate.setOrbit(null);
        gate.setLocation(0, 0);

        system.setLightColor(new Color(255,170, 183,255)); // light color in entire system, affects all entities

        //LOOT
        SectorEntityToken derelict1 = DerelictThemeGenerator.addSalvageEntity(system, "weapons_cache_high", Factions.NEUTRAL);
        derelict1.setId("nskr_cache_derelict1");
        derelict1.setCircularOrbit(center, 58, 500, 100f);
        derelict1.setSensorProfile(300f);
        derelict1.setDiscoverable(true);

        SectorEntityToken derelict2 = DerelictThemeGenerator.addSalvageEntity(system, "weapons_cache_high", Factions.NEUTRAL);
        derelict2.setId("nskr_cache_derelict2");
        derelict2.setCircularOrbit(center, 50, 575, 100f);
        derelict2.setSensorProfile(300f);
        derelict2.setDiscoverable(true);

        SectorEntityToken derelict3 = DerelictThemeGenerator.addSalvageEntity(system, "nskr_enigmabase", Factions.NEUTRAL);
        derelict3.setId("nskr_cache_derelict3");
        derelict3.setCircularOrbit(center, 100, 700, 90f);
        derelict3.setSensorProfile(500f);
        derelict3.setDiscoverable(true);
        Misc.setDefenderOverride(derelict3, new DefenderDataOverride(Factions.DERELICT, 1f, 125, 150));

        SectorEntityToken derelict4 = DerelictThemeGenerator.addSalvageEntity(system, "nskr_enigmabase", Factions.NEUTRAL);
        derelict4.setId("nskr_cache_derelict4");
        derelict4.setCircularOrbit(center, 250, 1400, 120f);
        derelict4.setSensorProfile(500f);
        derelict4.setDiscoverable(true);
        Misc.setDefenderOverride(derelict4, new DefenderDataOverride(Factions.DERELICT, 1f, 125, 150));

        //SectorEntityToken derelict5 = DerelictThemeGenerator.addSalvageEntity(system, Entities.ORBITAL_HABITAT, Factions.NEUTRAL);
        //derelict5.setId("nskr_cache_derelict5");
        //derelict5.setCircularOrbit(center, 150, 1000, 100f);
        //Misc.setDefenderOverride(derelict5, new DefenderDataOverride(Factions.DERELICT, 1f, 125, 150));

        //SATELLITES
        SectorEntityToken satellite1 = DerelictThemeGenerator.addNonSalvageEntity(system, DerelictThemeGenerator.createLocationAtRandomGap(new Random(), gate, 100f), "nskr_artifact", Factions.NEUTRAL).entity;
        satellite1.setDiscoverable(true);
        satellite1.setSensorProfile(100f);
        satellite1.getMemory().set(questStageManager.ARTIFACT_KEY+5, true);
        satellite1.getMemory().set(nskr_artifactDialog.ARTIFACT_EMPTY_KEY, true);
        satellite1.setCircularOrbitPointingDown(center, 0, 400, 60f);

        SectorEntityToken satellite2 = DerelictThemeGenerator.addNonSalvageEntity(system, DerelictThemeGenerator.createLocationAtRandomGap(new Random(), gate, 100f), "nskr_artifact", Factions.NEUTRAL).entity;
        satellite2.setDiscoverable(true);
        satellite2.setSensorProfile(100f);
        satellite2.getMemory().set(questStageManager.ARTIFACT_KEY+6, true);
        satellite2.getMemory().set(nskr_artifactDialog.ARTIFACT_EMPTY_KEY, true);
        satellite2.setCircularOrbitPointingDown(center, 180, 400, 60f);

        //DORMANT
        for (int x = 0; x < 2; x++) {
            SectorEntityToken dormantFleet = util.addDormant(center, Factions.DERELICT, 125f, 175f, 1.00f, 0.40f, 0.60f, 0f, 0, 0);
            dormantFleet.setCircularOrbit(center, (float)Math.random() * 360.0f, MathUtils.getRandomNumberInRange(800f, 1300f), MathUtils.getRandomNumberInRange(90,160));
            dormantFleet.setFacing((float)Math.random() * 360.0f);
            dormantFleet.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY, true);
        }

        //MAGNETIC
        SectorEntityToken field = system.addTerrain(Terrain.MAGNETIC_FIELD,
                new MagneticFieldParams(500f, // terrain effect band width
                        1800f, // terrain effect middle radius
                        center, // entity that it's around
                        1500f, // visual band start
                        2100f, // visual band end
                        new Color(224, 27, 80, 90), // base color
                        1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                        new Color(130, 60, 150, 130),
                        new Color(30, 150, 54, 150),
                        new Color(50, 200, 160, 190),
                        new Color(250, 70, 76, 240),
                        new Color(200, 80, 108, 255),
                        new Color(75, 0, 160, 255),
                        new Color(127, 0, 255, 255)
                ));
        field.setCircularOrbit(center, 0f, 0f, 180f);
        //OUTER TERRAIN
        //StarCoronaTerrainPlugin.CoronaParams

        //DEBRIS
        DebrisFieldTerrainPlugin.DebrisFieldParams params_gate_main = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                350f, // field radius - should not go above 1000 for performance reasons
                1.2f, // density, visual - affects number of debris pieces
                999999f, // duration in days
                0f); // days the field will keep generating glowing pieces
        params_gate_main.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
        params_gate_main.baseSalvageXP = 500; // base XP for scavenging in field
        SectorEntityToken gate_main1 = Misc.addDebrisField(system, params_gate_main, StarSystemGenerator.random);
        gate_main1.setSensorProfile(1000f);
        gate_main1.setDiscoverable(true);
        gate_main1.setCircularOrbit(center, 0f, 0f, 180f);
        gate_main1.setId("nskr_gate_main_debrisBelt");

        //11111 stuff
        SectorEntityToken cache1 = BaseThemeGenerator.addSalvageEntity(system, Entities.ALPHA_SITE_WEAPONS_CACHE, Factions.NEUTRAL);
        cache1.getLocation().set(-11111*(1f+(0.03f*(float)Math.random())), -11111*(1f+(0.03f*(float)Math.random())));
        SectorEntityToken cache2 = BaseThemeGenerator.addSalvageEntity(system, Entities.WEAPONS_CACHE_REMNANT, Factions.NEUTRAL);
        cache2.getLocation().set(-11111*(1f+(0.03f*(float)Math.random())), -11111*(1f+(0.03f*(float)Math.random())));
        SectorEntityToken cache3 = BaseThemeGenerator.addSalvageEntity(system, Entities.WEAPONS_CACHE_REMNANT, Factions.NEUTRAL);
        cache3.getLocation().set(-11111*(1f+(0.03f*(float)Math.random())), -11111*(1f+(0.03f*(float)Math.random())));
        //11111 DEBRIS
        SectorEntityToken eleven_main1 = Misc.addDebrisField(system, params_gate_main, StarSystemGenerator.random);
        eleven_main1.setSensorProfile(1000f);
        eleven_main1.setDiscoverable(true);
        eleven_main1.getLocation().set(-11111, -11111);
        eleven_main1.setId("nskr_eleven_main_debrisBelt");

        //music mem key
        for(SectorEntityToken e : system.getAllEntities()){
            if (e==Global.getSector().getPlayerFleet()) continue;
            e.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY, true);
        }
    }

    //GUARDIAN FLEET
    public static CampaignFleetAPI spawnGuardianFleet(CampaignFleetAPI pf, SectorEntityToken loc) {
        float points = MathUtils.getRandomNumberInRange(45f, 50f);

        Random random = nskr_kestevenQuest.getRandom();

        //skills
        Map<String, Integer> skills = new HashMap<>(OFFICER_SKILLS);
        if (nskr_modPlugin.getStarfarerMode()) skills.put(Skills.POLARIZED_ARMOR,2);
        //admiral skills
        skills.put("electronic_warfare",1);
        skills.put("crew_training",1);
        skills.put(Skills.COORDINATED_MANEUVERS,1);

        //memkeys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOSTILE);
        keys.add(MemFlags.MEMORY_KEY_NO_REP_IMPACT);
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE);
        keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
        keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
        keys.add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        keys.add(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY);
        keys.add(CACHE_FLEET_KEY);
        keys.add(MusicPlayerPluginImpl.KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY);
        //fs tags
        ArrayList<String> fsTags = new ArrayList<>();
        fsTags.add(Tags.SHIP_LIMITED_TOOLTIP);
        fsTags.add("prot_boss");

        //flagship
        simpleFleetMember flagship = new simpleFleetMember(FLAGSHIP_VARIANT, fsTags, true);
        flagship.alwaysRecover = true;
        flagship.name = FS_NAME;

        //secondaries, whole PROT roster
        //eternity spawned mid-fight
        //secondaries
        List<simpleFleetMember> secondaries = new ArrayList<>();
        simpleFleetMember secondary;
        //sovereign
        secondary = new simpleFleetMember(SECONDARY_VARIANT_2, fsTags, true);
        secondary.captain = createSecondaryCaptain(3);
        secondary.name = S2_NAME;
        secondaries.add(secondary);
        //nemesis
        secondary = new simpleFleetMember(SECONDARY_VARIANT_3, fsTags, true);
        secondary.captain = createSecondaryCaptain(4);
        secondary.name = S3_NAME;
        secondaries.add(secondary);
        //muninn
        secondary = new simpleFleetMember(SECONDARY_VARIANT_4, fsTags, true);
        secondary.captain = createSecondaryCaptain(5);
        secondary.name = S4_NAME;
        secondaries.add(secondary);
        //epoch-x
        secondary = new simpleFleetMember(SECONDARY_VARIANT_5, fsTags, true);
        secondary.captain = createSecondaryCaptain(6);
        secondary.name = S5_NAME;
        secondaries.add(secondary);
        //epoch
        secondary = new simpleFleetMember(SECONDARY_VARIANT_6, fsTags, true);
        secondary.captain = createSecondaryCaptain(7);
        secondary.name = S6_NAME;
        secondaries.add(secondary);
        //torpor
        secondary = new simpleFleetMember(SECONDARY_VARIANT_7, fsTags, true);
        secondary.captain = createSecondaryCaptain(8);
        secondary.name = S7_NAME;
        secondaries.add(secondary);
        //warfare
        secondary = new simpleFleetMember(SECONDARY_VARIANT_8, fsTags, true);
        secondary.captain = createSecondaryCaptain(9);
        secondary.name = S8_NAME;
        secondaries.add(secondary);
        //widow
        secondary = new simpleFleetMember(SECONDARY_VARIANT_9, fsTags, true);
        secondary.captain = createSecondaryCaptain(10);
        secondary.name = S9_NAME;
        secondaries.add(secondary);

        //commander
        simpleCaptain simpleCaptain = new simpleCaptain("nskr_"+COM_NAME, "enigma", skills);
        simpleCaptain.isAiCore = true;
        simpleCaptain.aiCoreID = "alpha_core";
        simpleCaptain.personality = Personalities.RECKLESS;
        simpleCaptain.portraitSpritePath = "graphics/portraits/nskr_enigma.png";
        simpleCaptain.rankId = Ranks.UNKNOWN;
        simpleCaptain.firstName = COM_NAME;
        simpleCaptain.lastName = COM_LAST_NAME;
        simpleCaptain.gender = FullName.Gender.FEMALE;

        //fleet
        simpleFleet simpleFleet = new simpleFleet(loc, Factions.DERELICT, points, keys, random);
        //simpleFleet.type = FleetTypes.PATROL_LARGE;
        simpleFleet.maxShipSize = 3;
        simpleFleet.sMods = 3;
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.name = FLEET_NAME;
        simpleFleet.noFactionInName = true;
        simpleFleet.commander = simpleCaptain.create();
        simpleFleet.flagshipInfo = flagship;
        simpleFleet.secondaries = secondaries;
        simpleFleet.assignment = FleetAssignment.INTERCEPT;
        simpleFleet.interceptPlayer = true;
        simpleFleet.assignmentText = "error #406, try again?";
        CampaignFleetAPI fleet = simpleFleet.create();

        //drone tags
        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()){
            if (m.isFighterWing() && util.isProtTech(m)){
                m.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
            }
        }

        //custom key
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, new CacheGuardFIDConfig());

        //set faction correctly
        if (nskr_modPlugin.IS_EXOTICA) {
            //captains
            fleet.setFaction(Factions.DERELICT, true);
            //for correct upgrades and exotics
            fleet.setFaction("enigma", false);
        } else {
            fleet.setFaction(Factions.DERELICT, true);
        }



        //around player
        fleet.setLocation(loc.getLocation().x, loc.getLocation().y);
        //update
        fleetUtil.update(fleet, random);

        log("cache SPAWNED, size " + points + " system " + loc.getName());
        log("cache FLEET, loc " + fleet.getStarSystem().getName() +" size "+ fleet.getFleetPoints() + " commander " + fleet.getCommander().getName().getFullName() + " flagship " + fleet.getFlagship().getHullSpec().getBaseHullId());

        return fleet;
    }

    public static class CacheGuardFIDConfig implements FleetInteractionDialogPluginImpl.FIDConfigGen {

        public FleetInteractionDialogPluginImpl.FIDConfig createConfig() {
            FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();

            config.showTransponderStatus = false;
            config.showEngageText = false;
            config.alwaysPursue = true;
            config.dismissOnLeave = false;
            config.withSalvage = true;
            config.printXPToDialog = true;

            config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {
                public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
                    new RemnantSeededFleetManager.RemnantFleetInteractionConfigGen().createConfig().delegate.
                            postPlayerSalvageGeneration(dialog, context, salvage);
                }
                public void notifyLeave(InteractionDialogAPI dialog) {

                    SectorEntityToken other = dialog.getInteractionTarget();
                    CampaignFleetAPI fleet = (CampaignFleetAPI) other;

                    if (hasPrototypes(fleet)) {
                        dialog.dismiss();
                        return;
                    }
                    //do one time stuff
                    spawnEverything(fleet.getStarSystem());
                    spawnWrecks(fleet);
                    if (!questUtil.getEndMissions() && questUtil.getStage()>=16) questUtil.setStage(18);
                    //command core
                    SectorEntityToken entity = spawnCore(fleet);

                    //end, swap to core dialog
                    dialog.setInteractionTarget(entity);
                    InteractionDialogPlugin plugin = new nskr_coreDialog();
                    dialog.setPlugin(plugin);
                    plugin.init(dialog);
                }
                public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                    bcc.aiRetreatAllowed = false;
                    bcc.objectivesAllowed = true;
                    bcc.fightToTheLast = true;
                    bcc.enemyDeployAll = true;
                }
            };
            return config;
        }
    }

    public static boolean hasPrototypes(CampaignFleetAPI fleet){
        boolean hasProt = false;
        //note to self, ships lose all tags on save & reload ????????
        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
            if (util.isProtTech(m))hasProt = true;
            if (hasProt)break;
        }
        return hasProt;
    }

    private static void spawnWrecks(CampaignFleetAPI fleet){
        //prot ships
        spawnWreck(fleet, FLAGSHIP_VARIANT, FS_NAME);
        spawnWreck(fleet, SECONDARY_VARIANT_1, S1_NAME);
        spawnWreck(fleet, SECONDARY_VARIANT_2, S2_NAME);
        spawnWreck(fleet, SECONDARY_VARIANT_3, S3_NAME);
        spawnWreck(fleet, SECONDARY_VARIANT_4, S4_NAME);
        spawnWreck(fleet, SECONDARY_VARIANT_5, S5_NAME);
        spawnWreck(fleet, SECONDARY_VARIANT_6, S6_NAME);
        spawnWreck(fleet, SECONDARY_VARIANT_7, S7_NAME);
        spawnWreck(fleet, SECONDARY_VARIANT_8, S8_NAME);
        spawnWreck(fleet, SECONDARY_VARIANT_9, S9_NAME);
    }

    private static Vector2f createCacheLoc(float minDistanceFromCore, float maxDistanceFromCore, float maxDistanceFromNearestSystem){
        Vector2f loc = new Vector2f();
        boolean done = false;
        //for loop to avoid crashing on custom sector gen settings
        for (int x=0;x<250;x++) {
            for (StarSystemAPI system : Global.getSector().getStarSystems()) {
                if (system.getHyperspaceAnchor()==null) continue;
                if (system.hasTag(Tags.THEME_HIDDEN)) continue;
                if (system.hasTag(Tags.THEME_CORE)) continue;
                loc = MathUtils.getPointOnCircumference(system.getHyperspaceAnchor().getLocationInHyperspace(), MathUtils.getRandomNumberInRange(maxDistanceFromNearestSystem/3f,maxDistanceFromNearestSystem), (float)Math.random()*360f);
                if (loc.length()>minDistanceFromCore && loc.length()<maxDistanceFromCore && util.getDistanceFromNearestSystem(loc)<maxDistanceFromNearestSystem){
                    done=true;
                    log("cache generated successfully, dist "+(int)loc.length());
                    break;
                }
            }
            if (done)break;
        }
        if (!done)log("ERROR fucked up sector, random cache loc");
        return loc;
    }

    private static SectorEntityToken spawnCore(CampaignFleetAPI fleet){

        SectorEntityToken entity = DerelictThemeGenerator.addNonSalvageEntity(fleet.getContainingLocation(), DerelictThemeGenerator.createLocationAtRandomGap(new Random(), fleet, 100f), "nskr_core", Factions.NEUTRAL).entity;
        entity.setDiscoverable(true);
        entity.setSensorProfile(100f);

        entity.getLocation().x = fleet.getLocation().x + (10f - (float) Math.random() * 50f);
        entity.getLocation().y = fleet.getLocation().y + (10f - (float) Math.random() * 50f);

        if (questUtil.getStage()>=16) entity.getMemory().set(MemFlags.MEMORY_KEY_MISSION_IMPORTANT, true);

        entity.getMemory().set(CORE_KEY, true);
        entity.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY, true);

        entity.setId("nskr_cache_core");

        return entity;
    }

    private static CustomCampaignEntityAPI spawnWreck(CampaignFleetAPI fleet, String variantId, String shipName){

        ShipRecoverySpecial.PerShipData ship = new ShipRecoverySpecial.PerShipData(variantId, ShipRecoverySpecial.ShipCondition.WRECKED, 0f);
        ship.shipName = shipName;
        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(ship, false);

        CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(
                fleet.getContainingLocation(),
                Entities.WRECK, Factions.NEUTRAL, params);

        entity.getLocation().x = fleet.getLocation().x + (100f - (float) Math.random() * 200f);
        entity.getLocation().y = fleet.getLocation().y + (100f - (float) Math.random() * 200f);

        ShipRecoverySpecial.ShipRecoverySpecialData data = new ShipRecoverySpecial.ShipRecoverySpecialData(null);
        //random SP recovery
        data.storyPointRecovery = nskr_kestevenQuest.getRandom().nextFloat()<0.50f;
        data.notNowOptionExits = true;
        data.noDescriptionText = true;
        DerelictShipEntityPlugin dsep = (DerelictShipEntityPlugin) entity.getCustomPlugin();
        ShipRecoverySpecial.PerShipData copy = dsep.getData().ship.clone();
        copy.variant = Global.getSettings().getVariant(copy.variantId).clone();
        copy.variantId = null;
        //no hidden tooltip
        copy.getVariant().removeTag(Tags.SHIP_LIMITED_TOOLTIP);
        data.addShip(copy);

        Misc.setSalvageSpecial(entity, data);

        entity.setDiscoverable(true);
        entity.setSensorProfile(100f);

        entity.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY, true);

        return entity;
    }

    public static PersonAPI createSecondaryCaptain(int number) {
        PersonAPI commander = MagicCampaign.createCaptainBuilder(Factions.DERELICT).create();
        commander.setId("nskr_"+COM_NAME+number);
        commander.setAICoreId("alpha_core");
        commander.setPersonality(Personalities.RECKLESS);
        commander.setPortraitSprite("graphics/portraits/nskr_enigma.png");
        commander.setPostId(null);
        commander.setRankId(Ranks.UNKNOWN);
        FullName name = new FullName(COM_NAME,"#"+number,FullName.Gender.FEMALE);
        commander.setName(name);
        Map<String, Integer> skills = new HashMap<>(OFFICER_SKILLS);
        if (nskr_modPlugin.getStarfarerMode()) skills.put(Skills.POLARIZED_ARMOR,2);
        util.setOfficerSkills(commander, skills);

        return commander;
    }

}













