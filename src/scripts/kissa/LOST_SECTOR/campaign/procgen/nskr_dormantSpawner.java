//////////////////////
//Initially created by Nia Tahl and modified from Tahlan Shipworks
//////////////////////
package scripts.kissa.LOST_SECTOR.campaign.procgen;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleSystem;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * Script to generate a whole bunch of things in random orbits throughout the sector
 */
public class nskr_dormantSpawner {

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
        Global.getLogger(nskr_dormantSpawner.class).info(message);
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
                //dumb null check
                //randomly needed after working for months
                SectorEntityToken loc = null;
                if (placeToSpawn.orbit==null || placeToSpawn.orbit.getFocus()==null){
                    loc = util.getRandomLocationInSystem(system, true,true, new Random());
                    log("dormantSpawner ERROR placeToSpawn is null");
                } else loc = placeToSpawn.orbit.getFocus();

                util.addDormant(loc, spawnData.one, 5f, 100f, 0.50f, 0.25f, 0.75f, 0f, 0, 0);

                numberOfSpawns++;
            }
        }
    }

    private static StarSystemAPI getRandomSystemWithBlacklist() {
        //ban tags
        List<String> banTags = new ArrayList<>();
        banTags.add(Tags.THEME_REMNANT);

        simpleSystem simpleSystem = new simpleSystem(new Random(), 1);
        simpleSystem.blacklistTags = banTags;
        simpleSystem.pickOnlyInProcgen = true;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("ERROR no valid system");
        return util.getRandomNonCoreSystem(new Random());
    }
}
