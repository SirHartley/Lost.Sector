package scripts.kissa.LOST_SECTOR.campaign.fleets.bounties;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.impl.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.intel.nskr_hintManager;
import scripts.kissa.LOST_SECTOR.campaign.intel.nskr_mothershipIntel;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.*;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;
import scripts.kissa.LOST_SECTOR.world.systems.frost.nskr_frost;

import java.util.*;

public class nskr_mothershipSpawner  extends BaseCampaignEventListener implements EveryFrameScript {
    //
    //spawns
    //
    public static final String FLEET_NAME = "Project Helios Remnant";
    public static final String COMMANDER_NAME = "CREATOR-A3401#";
    public static final String FLAGSHIP_VARIANT = "nskr_sunburst_boss";
    public static final String FLAGSHIP_NAME = "TTDS Helios";
    public static final String FACTION = Factions.REMNANTS;
    public static final String PLANET1_ID = "nskr_terra1";
    public static final String PLANET2_ID = "nskr_terra2";
    public static final String LOOT_KEY = "$mothershipLoot";
    public static final String PORTRAIT_SPRITE = "graphics/portraits/portrait_ai2.png";
    public static final String MOTHERSHIP_LOC_MEM_KEY = "nskr_mothershipKey";
    public static final String MOTHERSHIP_COMPLETED_MEM_KEY = "nskr_mothershipKeyCompleted";
    public static final String MOTHERSHIP_SPAWNED_MEM_KEY = "nskr_mothershipKeySpawnedWreck";
    public static final String SAVED_PREFIX = "mothership";
    public static final String HINT_KEY = "HINT_MOTHERSHIP";
    public static final String FLEET_ARRAY_KEY = "$nskr_mothershipSpawnerFleets";
    public static final String PERSISTENT_RANDOM_KEY = "nskr_mothershipSpawnerRandom";

    nskr_saved<Float> counter;
    nskr_saved<Boolean> firstTime;
    private final List<CampaignFleetAPI> removed = new ArrayList<>();
    //CampaignFleetAPI pf;
    Random random;

    static void log(final String message) {
        Global.getLogger(nskr_mothershipSpawner.class).info(message);
    }

    public nskr_mothershipSpawner() {
        super(false);
        //how often we run logic
        this.counter = new nskr_saved<>(SAVED_PREFIX + "Counter", 0.0f);
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
            counter.val += 2f * amount;
        } else {
            counter.val += amount;
        }

        //logic
        if (counter.val > 4f) {
            for (fleetInfo f : fleets) {
                CampaignFleetAPI fleet = f.fleet;
                boolean despawn = false;

                //looted
                if (!fleet.getMemoryWithoutUpdate().contains(LOOT_KEY)) {
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
                //logic
                boolean visibleToPlayer = fleet.isVisibleToSensorsOf(pf);
                if (visibleToPlayer && !despawn && firstTime.val) {
                    //Adds our intel
                    Global.getSector().getIntelManager().addIntel(new nskr_mothershipIntel(fleet), false);
                    log("added INTEL");

                    Global.getSector().getCampaignUI().addMessage("Initial examinations of the Remnant fleet shows an unusual flagship, the Sunburst-Class. Approach with extreme caution.",
                            Global.getSettings().getColor("standardTextColor"),
                            "Sunburst-Class",
                            "extreme caution",
                            Global.getSettings().getColor("yellowTextColor"),
                            Global.getSettings().getColor("yellowTextColor"));
                    firstTime.val = false;
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

    public static void spawnPlanets(SectorEntityToken loc, Random random) {

        StarSystemAPI system = loc.getStarSystem();

        PlanetAPI a1 = system.addPlanet(PLANET1_ID, loc, "Helios", getRandomHabitableType(random),
                random.nextFloat()*360f,
                mathUtil.getSeededRandomNumberInRange(30f,50f, random),
                loc.getRadius() + mathUtil.getSeededRandomNumberInRange(200f,300f, random),
                mathUtil.getSeededRandomNumberInRange(90f,120f, random));
        PlanetConditionGenerator.generateConditionsForPlanet(null, a1, system.getAge());

        //ruins
        if (!a1.hasCondition(Conditions.RUINS_SCATTERED) && !a1.hasCondition(Conditions.RUINS_WIDESPREAD) && !a1.hasCondition(Conditions.RUINS_EXTENSIVE) && !a1.hasCondition(Conditions.RUINS_VAST)){
            a1.getMarket().addCondition(nskr_frost.randomRuins());
            CoreLifecyclePluginImpl.addRuinsJunk(a1);
        }
        a1.addTag(Tags.NOT_RANDOM_MISSION_TARGET);

        PlanetAPI a2 = system.addPlanet(PLANET2_ID, loc, "Polaris", getRandomHabitableType(random),
                random.nextFloat()*360f,
                mathUtil.getSeededRandomNumberInRange(30f,50f, random),
                loc.getRadius() + mathUtil.getSeededRandomNumberInRange(400f,600f, random),
                mathUtil.getSeededRandomNumberInRange(120f,150f, random));
        PlanetConditionGenerator.generateConditionsForPlanet(null, a2, system.getAge());

        //ruins
        if (!a2.hasCondition(Conditions.RUINS_SCATTERED) && !a2.hasCondition(Conditions.RUINS_WIDESPREAD) && !a2.hasCondition(Conditions.RUINS_EXTENSIVE) && !a2.hasCondition(Conditions.RUINS_VAST)){
            a2.getMarket().addCondition(nskr_frost.randomRuins());
            CoreLifecyclePluginImpl.addRuinsJunk(a2);
        }
        a2.addTag(Tags.NOT_RANDOM_MISSION_TARGET);

        //remove ring systems since they are rendered above planets
        cleanRingBands(loc);

        system.getMemoryWithoutUpdate().set("$nex_do_not_colonize", true);
    }

    private static void cleanRingBands(SectorEntityToken loc) {
        StarSystemAPI sys = loc.getStarSystem();
        ArrayList<SectorEntityToken> entitiesCopy = new ArrayList<>(sys.getAllEntities());
        for (SectorEntityToken e : entitiesCopy){
            if (e instanceof RingBandAPI){
                RingBandAPI ring = (RingBandAPI) e;
                if (ring.getFocus()==null) continue;
                log("ring focus "+ring.getFocus().getName());
                if (ring.getFocus()==loc) {
                    float radius = ring.getMiddleRadius();
                    if (radius<=1200f){
                        ring.setExpired(true);
                        sys.removeEntity(ring);
                        log("REMOVED ring band "+radius);
                    }
                }
            }
        }
    }

    public static final ArrayList<String> TYPES = new ArrayList<>();
    static {
        TYPES.add(Planets.PLANET_TERRAN);
        TYPES.add(Planets.PLANET_TERRAN_ECCENTRIC);
        TYPES.add(Planets.PLANET_WATER);
        TYPES.add(Planets.TUNDRA);
        TYPES.add("jungle");
        TYPES.add("arid");
    }
    private static String getRandomHabitableType(Random random) {
        return TYPES.get(mathUtil.getSeededRandomNumberInRange(0, TYPES.size()-1, random));
    }

    public static void spawnMothershipFleet(SectorEntityToken loc, Random random) {

        float points = MathUtils.getRandomNumberInRange(155f, 160f);

        //apply settings
        points *= nskr_modPlugin.getScriptedFleetSizeMult();

        //skills
        Map<String, Integer> skills = new HashMap<>();
        skills.put("combat_endurance",2);
        skills.put(Skills.GUNNERY_IMPLANTS,2);
        skills.put("field_modulation",2);
        skills.put("target_analysis",2);
        skills.put("systems_expertise",2);
        skills.put("missile_specialization",2);
        skills.put("energy_weapon_mastery",2);
        skills.put("ordnance_expert",2);
        //skills com
        if (nskr_modPlugin.getStarfarerMode()) skills.put(Skills.WOLFPACK_TACTICS,1);
        skills.put(Skills.ELECTRONIC_WARFARE,1);
        skills.put(Skills.CARRIER_GROUP,1);
        skills.put(Skills.FIGHTER_UPLINK,1);

        //memkeys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE);
        keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
        keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
        keys.add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        keys.add(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY);
        keys.add(LOOT_KEY);
        //fs tags
        List<String> fsTags = new ArrayList<>();
        fsTags.add(Tags.SHIP_LIMITED_TOOLTIP);

        //flagship
        simpleFleetMember flagship = new simpleFleetMember(FLAGSHIP_VARIANT, fsTags, true);
        flagship.alwaysRecover = false;
        flagship.name = FLAGSHIP_NAME;

        //commander
        simpleCaptain simpleCaptain = new simpleCaptain("nskr_"+ COMMANDER_NAME, FACTION, skills);
        simpleCaptain.isAiCore = true;
        simpleCaptain.aiCoreID = "alpha_core";
        simpleCaptain.personality = Personalities.RECKLESS;
        simpleCaptain.portraitSpritePath = PORTRAIT_SPRITE;
        simpleCaptain.rankId = Ranks.SPACE_ADMIRAL;
        simpleCaptain.firstName = COMMANDER_NAME;
        simpleCaptain.gender = FullName.Gender.FEMALE;

        //fleet
        simpleFleet simpleFleet = new simpleFleet(loc, FACTION, points, keys, random);
        //simpleFleet.type = FleetTypes.PATROL_LARGE;
        simpleFleet.maxShipSize = 2;
        simpleFleet.sMods = 1;
        simpleFleet.name = FLEET_NAME;
        simpleFleet.noFactionInName = true;
        simpleFleet.commander = simpleCaptain.create();
        simpleFleet.flagshipInfo = flagship;
        simpleFleet.assignment = FleetAssignment.ORBIT_PASSIVE;
        simpleFleet.assignmentText = "error #446, try again?";

        //simpleFleet.aiFleetProperties = true;

        CampaignFleetAPI fleet = simpleFleet.create();

        //custom key
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, new mothershipFIDConfig());

        //makes sure we are not in a star
        questUtil.spawnAwayFromStarFixer(fleet, 2.0f);

        fleet.setFaction(FACTION, true);

        //update
        fleetUtil.update(fleet, random);

        //add to mem IMPORTANT
        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
        fleetInfo info = new fleetInfo(fleet, null, loc);
        info.flagshipSimpleMember = simpleFleet.getFlagshipInfo();
        info.secondaries = simpleFleet.getSecondaryMembers();
        fleets.add(info);
        fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

        log("SPAWNED, size " + points + " system " + loc.getContainingLocation().getName()+" loc " + loc.getName());
        log("FLEET, loc " + fleet.getStarSystem().getName() +" size "+ fleet.getFleetPoints() + " commander " + fleet.getCommander().getName().getFullName() + " flagship " + fleet.getFlagship().getHullSpec().getBaseHullId());
    }

    //location saved to memory (important)
    public static SectorEntityToken getMothershipBaseLocation() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = MOTHERSHIP_LOC_MEM_KEY;
        if (!data.containsKey(id))
            data.put(id, randomGasGiant(new Random()));

        return (SectorEntityToken)data.get(id);
    }

    private static SectorEntityToken randomGasGiant(Random random) {
        StarSystemAPI sys = getRandomSystemWithBlacklist(random);

        List<PlanetAPI> giants = new ArrayList<>();
        for (PlanetAPI p : sys.getPlanets()){
            if (p== null || !p.isGasGiant()) continue;
            giants.add(p);
        }

        if (giants.isEmpty()){
            log("ERROR no gas giants, you done fucked up");
            return null;
        }

        return giants.get(mathUtil.getSeededRandomNumberInRange(0,giants.size()-1, random));
    }

    private static StarSystemAPI getRandomSystemWithBlacklist(Random random) {
        //pick tags
        List<String> pickTags = new ArrayList<>();
        pickTags.add(Tags.THEME_REMNANT);

        //ban types
        List<String> banTypes = new ArrayList<>();
        banTypes.add(StarTypes.BLACK_HOLE);
        banTypes.add(StarTypes.WHITE_DWARF);
        banTypes.add(StarTypes.NEUTRON_STAR);
        //ban system
        List<StarSystemGenerator.StarSystemType> banSystems = new ArrayList<>();
        banSystems.add(StarSystemGenerator.StarSystemType.NEBULA);
        //ban entities

        simpleSystem simpleSystem = new simpleSystem(new Random(), 1);
        simpleSystem.pickTags = pickTags;
        simpleSystem.blacklistStars = banTypes;
        simpleSystem.blacklistSystemTypes = banSystems;
        simpleSystem.pickOnlyInProcgen = true;

        List<StarSystemAPI> systems = new ArrayList<>();
        List<StarSystemAPI> validSystems = new ArrayList<>();
        if (!simpleSystem.get().isEmpty()) {
            systems = simpleSystem.get();
        }

        getSystemsWithGasGiant(systems, validSystems);
        //add any system as back-up
        while (validSystems.isEmpty()){
            systems.add(util.getRandomNonCoreSystem(random));
            getSystemsWithGasGiant(systems, validSystems);
            log("ERROR no gas giant Remnant systems");
        }

        return validSystems.get(mathUtil.getSeededRandomNumberInRange(0,validSystems.size()-1, random));
    }

    private static List<StarSystemAPI> getSystemsWithGasGiant(List<StarSystemAPI> systems, List<StarSystemAPI> validSystems) {
        for (StarSystemAPI sys : systems){
            for (PlanetAPI p : sys.getPlanets()){
                if (p== null || !p.isGasGiant()) continue;

                //moon check
                if (hasMoons(sys, p)) continue;

                validSystems.add(sys);
                break;
            }
        }
        return validSystems;
    }

    private static boolean hasMoons(StarSystemAPI sys, PlanetAPI p) {
        for (SectorEntityToken moon : sys.getAllEntities()){
            if (moon instanceof PlanetAPI) {
                if (moon.getOrbit() == null || moon.getOrbitFocus() == null) continue;
                if (moon.getOrbitFocus().isStar()) continue;
                log(moon.getName() + " orbit focus " + moon.getOrbitFocus().getName());
                if (moon.getOrbitFocus() == p) {
                    log(moon.getName() + " is moon of " + moon.getOrbitFocus().getName());
                    return true;
                }
            }
        }
        return false;
    }

    public static class mothershipFIDConfig implements FleetInteractionDialogPluginImpl.FIDConfigGen {
        public FleetInteractionDialogPluginImpl.FIDConfig createConfig() {
            FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();

            config.showTransponderStatus = false;
            config.showEngageText = false;
            config.alwaysPursue = false;
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
                    fleetInfo info = null;
                    for (fleetInfo f : fleetUtil.getFleets(nskr_mothershipSpawner.FLEET_ARRAY_KEY)){
                        info = f;
                    }
                    if (info==null){
                        dialog.dismiss();
                        return;
                    }

                    if (fleet.getFlagship()==null && !questUtil.getCompleted(MOTHERSHIP_SPAWNED_MEM_KEY)) {

                        //spawn
                        CustomCampaignEntityAPI entity = spawnWreck(fleet, FLAGSHIP_VARIANT, FLAGSHIP_NAME);
                        questUtil.setCompleted(true, MOTHERSHIP_SPAWNED_MEM_KEY);

                        dialog.setInteractionTarget(entity);
                        RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl();
                        dialog.setPlugin(plugin);
                        plugin.init(dialog);

                        //dialog.dismiss();
                    } else {
                        dialog.dismiss();
                    }

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

    private static CustomCampaignEntityAPI spawnWreck(CampaignFleetAPI fleet, String variantId, String shipName){

        ShipRecoverySpecial.PerShipData ship = new ShipRecoverySpecial.PerShipData(variantId, ShipRecoverySpecial.ShipCondition.WRECKED, 0f);
        ship.shipName = shipName;
        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(ship, false);
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();

        CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(
                fleet.getContainingLocation(),
                Entities.WRECK, Factions.NEUTRAL, params);

        entity.getLocation().x = pf.getLocation().x + (100f - (float) Math.random() * 200f);
        entity.getLocation().y = pf.getLocation().y + (100f - (float) Math.random() * 200f);

        ShipRecoverySpecial.ShipRecoverySpecialData data = new ShipRecoverySpecial.ShipRecoverySpecialData(null);
        //random SP recovery
        data.storyPointRecovery = getRandom().nextFloat()<0.50f;
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

        //entity.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY, true);

        return entity;
    }

    public static boolean getBountyCompleted() {
        String id = MOTHERSHIP_COMPLETED_MEM_KEY;

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, false);

        return (boolean)data.get(id);
    }

    public static void setBountyCompleted(boolean completed) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(MOTHERSHIP_COMPLETED_MEM_KEY, completed);
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
