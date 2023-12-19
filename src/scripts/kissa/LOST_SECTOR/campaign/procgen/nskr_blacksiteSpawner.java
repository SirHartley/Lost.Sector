package scripts.kissa.LOST_SECTOR.campaign.procgen;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import scripts.kissa.LOST_SECTOR.campaign.fleets.events.blacksiteInfo;
import scripts.kissa.LOST_SECTOR.campaign.fleets.events.nskr_blacksiteManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleSystem;
import scripts.kissa.LOST_SECTOR.util.ids;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

public class nskr_blacksiteSpawner {

    //List of stuff to spawn and their count
    public static final List<Pair<String, Integer>> STATION_SPAWNS = new ArrayList<>();
    static {
        STATION_SPAWNS.add(new Pair<>("nskr_blacksite", 12));
    }
    //Weights for the different types of locations our things can spawn in
    public static final LinkedHashMap<BaseThemeGenerator.LocationType, Float> WEIGHTS = new LinkedHashMap<>();
    static {
        WEIGHTS.put(BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT, 12f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.IN_ASTEROID_BELT, 8f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.IN_ASTEROID_FIELD, 8f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.STAR_ORBIT, 6f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.IN_SMALL_NEBULA, 8f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.NEAR_STAR, 2f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 12f);
    }

    static void log(final String message) {
        Global.getLogger(nskr_blacksiteSpawner.class).info(message);
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
                    if (placeToSpawn.orbit==null || placeToSpawn.type==null){
                        placeToSpawn = null;
                    }
                }

                SectorEntityToken base = addDerelict(system, spawnData.one, placeToSpawn.orbit, placeToSpawn.type);
                //makes sure we are not in a star
                questUtil.spawnAwayFromStarFixer(base);
                base.setId(ids.BLACKSITE_ENTITY_ID+numberOfSpawns);

                List<blacksiteInfo> sites = nskr_blacksiteManager.getSites(nskr_blacksiteManager.SITE_ARRAY_KEY);
                blacksiteInfo site = new blacksiteInfo(base, new Random());
                sites.add(site);
                nskr_blacksiteManager.setSites(sites, nskr_blacksiteManager.SITE_ARRAY_KEY);

                switch (site.faction){
                    case Factions.PIRATES:
                        base.setName("Pirate Stash");
                        break;
                    case Factions.LUDDIC_PATH:
                        base.setName("Pather Stash");
                        break;
                    case Factions.TRITACHYON:
                        base.setName("Tri-Tachyon Blacksite");
                        break;
                    case ids.KESTEVEN_FACTION_ID:
                        base.setName("Kesteven Blacksite");
                        break;
                    case ids.ENIGMA_FACTION_ID:
                        base.setName("Ancient Enigma Hangar");
                        break;
                    case Factions.REMNANTS:
                        base.setName("Ancient Remnant Hangar");
                        break;
                }
                base.setDiscoverable(true);
                base.setSensorProfile(1000f);

                log("location blacksite "+site.faction+" loc "+ base.getStarSystem().getName()+" "+placeToSpawn);

                numberOfSpawns++;
            }
        }
    }

    private static StarSystemAPI getRandomSystemWithBlacklist() {
        //ban entities
        List<String> banEntities = new ArrayList<>();
        banEntities.add(ids.BLACKSITE_ENTITY_ID);

        simpleSystem simpleSystem = new simpleSystem(new Random(), 1);
        simpleSystem.blacklistEntities = banEntities;
        simpleSystem.pickOnlyInProcgen = true;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("used all valid systems");
        return util.getRandomNonCoreSystem(new Random());
    }

    //Mini-function for generating derelicts
    private static SectorEntityToken addDerelict(StarSystemAPI system, String stationId, OrbitAPI orbit, BaseThemeGenerator.LocationType type) {

        BaseThemeGenerator.EntityLocation loc = new BaseThemeGenerator.EntityLocation();
        loc.location = orbit.computeCurrentLocation();
        loc.type = type;

        SectorEntityToken station = DerelictThemeGenerator.addNonSalvageEntity(system, loc, stationId, Factions.NEUTRAL).entity;
        station.setDiscoverable(true);

        station.setCircularOrbit(orbit.getFocus(), VectorUtils.getAngle(orbit.computeCurrentLocation(), orbit.getFocus().getLocation()), MathUtils.getDistance(orbit.computeCurrentLocation(), orbit.getFocus().getLocation()), orbit.getOrbitalPeriod());

        return station;
    }
}
