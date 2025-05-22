//////////////////////
//Initially created by Nia Tahl and modified from Tahlan Shipworks
//////////////////////
package lostsector.campaign.procgen;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import lostsector.campaign.quests.util.QuestUtil;
import lostsector.campaign.quests.util.SimpleSystem;
import lostsector.util.IdsLS;
import lostsector.util.MiscLS;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * Script to generate a whole bunch of things in random orbits throughout the sector
 */
public class EnigmaBaseSpawner {

    //List of stuff to spawn and their count
    public static final List<Pair<String, Integer>> STATION_SPAWNS = new ArrayList<>();
    static {
        STATION_SPAWNS.add(new Pair<>("nskr_enigmabase", 10));
    }
    //Weights for the different types of locations our things can spawn in
    public static final LinkedHashMap<LocationType, Float> WEIGHTS = new LinkedHashMap<>();
    static {
        WEIGHTS.put(LocationType.GAS_GIANT_ORBIT, 8f);
        WEIGHTS.put(LocationType.IN_ASTEROID_BELT, 2f);
        WEIGHTS.put(LocationType.IN_ASTEROID_FIELD, 4f);
        WEIGHTS.put(LocationType.STAR_ORBIT, 2f);
        WEIGHTS.put(LocationType.IN_SMALL_NEBULA, 4f);
        WEIGHTS.put(LocationType.NEAR_STAR, 6f);
        WEIGHTS.put(LocationType.JUMP_ORBIT, 4f);
        WEIGHTS.put(LocationType.PLANET_ORBIT, 8f);
    }

    static void log(final String message) {
        Global.getLogger(EnigmaBaseSpawner.class).info(message);
    }

    // Functions

    public static void spawnBases() {
        for (Pair<String, Integer> spawnData : STATION_SPAWNS) {
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

                DefenderDataOverride defenders = new DefenderDataOverride("enigma",1f, 30f, 100f);
                SectorEntityToken base = addDerelict(system, spawnData.one, placeToSpawn.orbit, defenders);
                //makes sure we are not in a star
                QuestUtil.spawnAwayFromStarFixer(base);
                base.setId("nskr_enigmabase_"+numberOfSpawns);

                numberOfSpawns++;
            }
        }
    }

    private static StarSystemAPI getRandomSystemWithBlacklist() {
        //ban tags
        List<String> banTags = new ArrayList<>();
        banTags.add(Tags.THEME_REMNANT);
        //star types
        List<String> pickStar = new ArrayList<>();
        pickStar.add(StarTypes.BLUE_SUPERGIANT);
        pickStar.add(StarTypes.RED_SUPERGIANT);
        //ban entities
        List<String> banEntities = new ArrayList<>();
        banEntities.add(IdsLS.RD_FACILITY_ENTITY_ID);

        SimpleSystem simpleSystem = new SimpleSystem(new Random(), 1);
        simpleSystem.blacklistTags = banTags;
        simpleSystem.blacklistEntities = banEntities;
        simpleSystem.pickOnlyInProcgen = true;
        simpleSystem.pickStars = pickStar;
        simpleSystem.enforceSystemStarType = true;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        } else {
            //we have run out of preferred stars
            simpleSystem.pickStars = new ArrayList<>();
            log("used all valid systems, pick any star");
            if (!simpleSystem.get().isEmpty()) {
                StarSystemAPI pick = simpleSystem.pick();
                log("picked " + pick.getName());
                return pick;
            } else {
                log("ERROR no non remnant star systems");
                return MiscLS.getRandomNonCoreSystem(new Random());
            }
        }
    }

    //Mini-function for generating derelicts
    private static SectorEntityToken addDerelict(StarSystemAPI system, String stationId, OrbitAPI orbit,
                                                 @Nullable DefenderDataOverride defenders) {

        SectorEntityToken station = DerelictThemeGenerator.addSalvageEntity(system, stationId, Factions.NEUTRAL);
        station.setDiscoverable(true);

        station.setOrbit(orbit);

        if (defenders != null) {
            Misc.setDefenderOverride(station, defenders);
        }
        return station;
    }
}
