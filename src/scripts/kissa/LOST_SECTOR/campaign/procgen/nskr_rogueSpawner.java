//////////////////////
//Initially created by Nia Tahl and modified from Tahlan Shipworks
//////////////////////
package scripts.kissa.LOST_SECTOR.campaign.procgen;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.jetbrains.annotations.Nullable;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleSystem;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * Script to generate a whole bunch of teaser ships in random orbits throughout the sector
 */
public class nskr_rogueSpawner {


    //List of teaser ships to spawn and their count
    public static final List<Pair<String, Integer>> SHIP_SPAWNS = new ArrayList<>();
    static {
       SHIP_SPAWNS.add(new Pair<>("nskr_rorqual_std", 1));}

    //Weights for the different types of locations our teasers can spawn in
    public static final LinkedHashMap<LocationType, Float> WEIGHTS = new LinkedHashMap<>();
    static {
        WEIGHTS.put(LocationType.IN_ASTEROID_BELT, 4f);
        WEIGHTS.put(LocationType.IN_ASTEROID_FIELD, 8f);
        WEIGHTS.put(LocationType.STAR_ORBIT, 4f);
        WEIGHTS.put(LocationType.IN_SMALL_NEBULA, 6f);
        WEIGHTS.put(LocationType.NEAR_STAR, 8f);
    }

    static void log(final String message) {
        Global.getLogger(nskr_rogueSpawner.class).info(message);
    }

    // Functions

    public static void spawnRogues() {
        for (Pair<String, Integer> spawnData : SHIP_SPAWNS) {
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

                    //dumb null check
                    //randomly needed after working for months
                    SectorEntityToken entity = null;
                    if (placeToSpawn.orbit==null || placeToSpawn.orbit.getFocus()==null){
                        entity = util.getRandomLocationInSystem(system, true,true, new Random());
                        log("ERROR placeToSpawn is null");
                    } else entity = placeToSpawn.orbit.getFocus();

                    //if we are orbiting a market, or the thing we orbit is orbiting a market, skip
                    if (entity.getMarket()!=null && !entity.getMarket().isPlanetConditionMarketOnly()){
                        placeToSpawn = null;
                    }
                    if (entity.getOrbit()!=null && entity.getOrbit().getFocus()!=null && entity.getOrbit().getFocus().getMarket()!=null && !entity.getOrbit().getFocus().getMarket().isPlanetConditionMarketOnly()){
                        placeToSpawn = null;
                    }
                }

                //Now, simply spawn the ship in the spawn location
                boolean recoverable = Math.random()<0.25f;

                addDerelict(system, spawnData.one, placeToSpawn.orbit, ShipRecoverySpecial.ShipCondition.BATTERED, recoverable, null);

                numberOfSpawns++;
            }
        }
    }

    private static StarSystemAPI getRandomSystemWithBlacklist() {
        //pick tags
        List<String> pickTags = new ArrayList<>();
        pickTags.add(Tags.THEME_CORE);
        pickTags.add(Tags.THEME_CORE_POPULATED);

        simpleSystem simpleSystem = new simpleSystem(new Random(), 1);
        simpleSystem.allowCore = true;
        simpleSystem.allowMarkets = true;
        simpleSystem.pickTags = pickTags;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("ERROR no valid system");
        return util.getRandomNonCoreSystem(new Random());
    }


    //Mini-function for generating derelicts
    private static SectorEntityToken addDerelict(StarSystemAPI system, String variantId, OrbitAPI orbit,
                                                 ShipRecoverySpecial.ShipCondition condition, boolean recoverable,
                                                 @Nullable DefenderDataOverride defenders) {

        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), false);
        SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);

        ship.setOrbit(orbit);

        if (recoverable) {
            SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
            Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
        }
        if (defenders != null) {
            Misc.setDefenderOverride(ship, defenders);
        }
        return ship;
    }
}
