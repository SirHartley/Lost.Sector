//////////////////////
//Initially created by Nia Tahl and modified from Tahlan Shipworks
//////////////////////
package lostsector.campaign.enigma;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import lostsector.campaign.kesteven.quest.QuestHelper;
import lostsector.settings.Difficulty;
import lostsector.helper.FleetHelper;
import lostsector.helper.fleet.SystemPicker;
import lostsector.helper.SystemHelper;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * Script to generate a whole bunch of things in random orbits throughout the sector
 */
public class DormantSpawner {

    public static final String DORMANT_KEY = "$EnigmaDormantFleet";
    //List of stuff to spawn and their count
    public static final List<Pair<String, Integer>> DORMANT_SPAWNS = new ArrayList<>();
    static {
        DORMANT_SPAWNS.add(new Pair<>("enigma", 15));
    }
    //Weights for the different types of locations our things can spawn in
    public static final LinkedHashMap<LocationType, Float> WEIGHTS = new LinkedHashMap<>();
    static {
        WEIGHTS.put(LocationType.GAS_GIANT_ORBIT, 5f);
        WEIGHTS.put(LocationType.IN_ASTEROID_BELT, 3f);
        WEIGHTS.put(LocationType.IN_ASTEROID_FIELD, 3f);
        WEIGHTS.put(LocationType.STAR_ORBIT, 1f);
        WEIGHTS.put(LocationType.IN_SMALL_NEBULA, 2f);
        WEIGHTS.put(LocationType.NEAR_STAR, 1f);
        WEIGHTS.put(LocationType.JUMP_ORBIT, 5f);
        WEIGHTS.put(LocationType.PLANET_ORBIT, 8f);
    }

    static void log(final String message) {
        Global.getLogger(DormantSpawner.class).info(message);
    }

    // Functions

    public static void spawnDormant() {
        for (Pair<String, Integer> spawnData : DORMANT_SPAWNS) {
            int numberOfSpawns = 0;
            while (numberOfSpawns < spawnData.two) {
                //Continue until we've found a place to spawn
                BaseThemeGenerator.EntityLocation placeToSpawn = null;
                StarSystemAPI system = null;
                while (placeToSpawn == null) {
                    system = getRandomSystemWithBlacklist();
                    if (system == null) {
                        //We've somehow blacklisted every system in the sector: just don't spawn anything
                        return;
                    }

                    //Gets a list of random locations in the system, and picks one
                    WeightedRandomPicker<BaseThemeGenerator.EntityLocation> validPoints = BaseThemeGenerator.getLocations(new Random(), system, 50f, WEIGHTS);
                    placeToSpawn = validPoints.pick();
                }
                // The picked location's orbit or its focus can be null.
                SectorEntityToken loc = null;
                if (placeToSpawn.orbit==null || placeToSpawn.orbit.getFocus()==null){
                    loc = SystemHelper.getRandomLocationInSystem(system, true,true, new Random());
                    log("dormantSpawner ERROR placeToSpawn is null");
                } else loc = placeToSpawn.orbit.getFocus();

                addDormant(loc, spawnData.one, 5f, 100f, 0.50f, 0.25f, 0.75f, 0f, 0, 0);

                numberOfSpawns++;
            }
        }
    }

    public static SectorEntityToken addDormant(SectorEntityToken loc, String factionId, float combatPoints) {
        return addDormant(loc,factionId, combatPoints, combatPoints, 0f,1f,1f,0f,0,0);
    }

    public static SectorEntityToken addDormant(SectorEntityToken loc, String factionId, float minCombatPoints, float maxCombatPoints, float qualityChance, float minQuality, float maxQuality, float SmodChance, int minSmod, int maxSmod) {
        CampaignFleetAPI fleet;
        StarSystemAPI system = loc.getStarSystem();
        String name;

        float combatPoints = MathUtils.getRandomNumberInRange(minCombatPoints, maxCombatPoints);

        String type = "patrolSmall";
        name = "Splinter";
        if (combatPoints > 30f) {
            type = "patrolMedium";
            name = "Combine";
        }
        if (combatPoints > 60f) {
            type = "patrolLarge";
            name = "Swarm";
        }

        //apply settings
        combatPoints *= Difficulty.scriptedFleetMult();

        if (combatPoints<=0f) return null;

        final FleetParamsV3 params = new FleetParamsV3(
                new Vector2f(), factionId, 1f, type, combatPoints, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);

        if (Math.random()<qualityChance) params.qualityOverride = MathUtils.getRandomNumberInRange(minQuality, maxQuality);
        if (Math.random()<SmodChance) params.averageSMods = MathUtils.getRandomNumberInRange(minSmod, maxSmod);
        params.withOfficers = true;

        fleet = FleetFactoryV3.createFleet(params);
        system.addEntity(fleet);
        RemnantSeededFleetManager.initRemnantFleetProperties(params.random, fleet, true);

        fleet.setTransponderOn(true);

        float dist = loc.getRadius() * MathUtils.getRandomNumberInRange(2.00f, 2.50f);
        fleet.setCircularOrbit(loc, (float)Math.random() * 360.0f, dist, MathUtils.getRandomNumberInRange(60f,120f));
        fleet.setFacing((float)Math.random() * 360.0f);

        //enigma dormants
        if (factionId.equals("enigma")) {
            fleet.getMemoryWithoutUpdate().set(DormantSpawner.DORMANT_KEY, true);
            fleet.setName(name);

            //make the officers AI cores
            FleetHelper.setAIOfficers(fleet);
        }

        //makes sure we are not in a star
        QuestHelper.spawnAwayFromStarFixer(fleet, 2.0f);

        //update
        FleetHelper.update(fleet, new Random());

        log("DORMANT added in "+loc.getContainingLocation().getName()+" to "+loc.getName());

        return fleet;
    }

    private static StarSystemAPI getRandomSystemWithBlacklist() {
        //ban tags
        List<String> banTags = new ArrayList<>();
        banTags.add(Tags.THEME_REMNANT);

        SystemPicker simpleSystem = new SystemPicker(new Random(), 1);
        simpleSystem.blacklistTags = banTags;
        simpleSystem.pickOnlyInProcgen = true;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("ERROR no valid system");
        return SystemHelper.getRandomNonCoreSystem(new Random());
    }
}
