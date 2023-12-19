//////////////////////
//Initially created by Nia Tahl and modified from Tahlan Shipworks
//////////////////////
package scripts.kissa.LOST_SECTOR.campaign.procgen;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.jetbrains.annotations.Nullable;
import org.lazywizard.lazylib.MathUtils;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleSystem;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

public class nskr_environmentalStorytelling {

    /**
     * Script to generate a whole bunch of things in random orbits throughout the sector
     */
    public static int NUMBER_OF_SPAWNS_MAIN = 6;
    public static int NUMBER_OF_SPAWNS_SECONDARY = 6;

    //Weights for the different types of locations our things can spawn in
    public static final LinkedHashMap<BaseThemeGenerator.LocationType, Float> WEIGHTS = new LinkedHashMap<>();
    static {
        WEIGHTS.put(BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT, 8f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.IN_ASTEROID_BELT, 2f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.IN_ASTEROID_FIELD, 4f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.STAR_ORBIT, 2f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.IN_SMALL_NEBULA, 4f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.NEAR_STAR, 6f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.JUMP_ORBIT, 4f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 8f);
    }
    public static final ArrayList<String> DERELICT_FACTIONS = new ArrayList<>();
    static {
        DERELICT_FACTIONS.add(Factions.LUDDIC_PATH);
        DERELICT_FACTIONS.add(Factions.TRITACHYON);
        DERELICT_FACTIONS.add(Factions.INDEPENDENT);
        DERELICT_FACTIONS.add("kesteven");
    }
    public static final List<Pair<String, Float>> ROLES = new ArrayList<>();
    static {
        ROLES.add(new Pair<>(ShipRoles.COMBAT_SMALL, 14f));
        ROLES.add(new Pair<>(ShipRoles.COMBAT_MEDIUM, 12f));
        ROLES.add(new Pair<>(ShipRoles.COMBAT_LARGE, 8f));
        ROLES.add(new Pair<>(ShipRoles.COMBAT_CAPITAL, 1f));
        ROLES.add(new Pair<>(ShipRoles.CARRIER_MEDIUM, 6f));
        ROLES.add(new Pair<>(ShipRoles.CARRIER_LARGE, 2f));
        ROLES.add(new Pair<>(ShipRoles.FREIGHTER_LARGE, 2f));
        ROLES.add(new Pair<>(ShipRoles.FREIGHTER_MEDIUM, 1f));
    }

    static void log(final String message) {
        Global.getLogger(nskr_environmentalStorytelling.class).info(message);
    }

    // Functions
    public static void spawnStorytelling() {
        //enigma won
        for (int x=0;x<NUMBER_OF_SPAWNS_MAIN;x++) {
            int numberOfSpawns = 0;
            while (numberOfSpawns < 1) {
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

                //dormant
                SectorEntityToken dormant = util.addDormant(util.getRandomLocationInSystem(system,false,true, new Random()),
                        "enigma", MathUtils.getRandomNumberInRange(80f,120f));
                //makes sure we are not in a star
                questUtil.spawnAwayFromStarFixer(dormant,2.0f);

                //debris
                DebrisFieldTerrainPlugin.DebrisFieldParams params_debrisField = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                        350f, // field radius - should not go above 1000 for performance reasons
                        1.2f, // density, visual - affects number of debris pieces
                        10000000f, // duration in days
                        0f); // days the field will keep generating glowing pieces
                params_debrisField.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
                params_debrisField.baseSalvageXP = 500; // base XP for scavenging in field
                SectorEntityToken debrisField = Misc.addDebrisField(system, params_debrisField, StarSystemGenerator.random);
                debrisField.setSensorProfile(1000f);
                debrisField.setDiscoverable(true);
                debrisField.setCircularOrbit(dormant.getOrbit().getFocus(), dormant.getCircularOrbitAngle(), dormant.getCircularOrbitRadius(), dormant.getOrbit().getOrbitalPeriod());
                debrisField.setId("nskr_debrisField_"+new Random().nextLong());

                //ships
                float recoveryChance = 0.25f;
                int count = MathUtils.getRandomNumberInRange(6,11);
                String faction = randomFaction();
                for (int y=0;y<count;y++) {
                    SectorEntityToken derelict = addDerelict(system, pickRandomVariant(faction, new Random(), false), randomCondition(), Math.random() < recoveryChance, null);
                    derelict.setCircularOrbit(dormant, (float)(Math.random()*360f), MathUtils.getRandomNumberInRange(50f,400f), MathUtils.getRandomNumberInRange(45f, 120f));
                }

                log("added storyTelling main to "+system.getName());

                numberOfSpawns++;
            }
        }
        //enigma lost
        for (int x=0;x<NUMBER_OF_SPAWNS_SECONDARY;x++) {
            int numberOfSpawns = 0;
            while (numberOfSpawns < 1) {
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

                //debris
                DebrisFieldTerrainPlugin.DebrisFieldParams params_debrisField = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                        350f, // field radius - should not go above 1000 for performance reasons
                        1.2f, // density, visual - affects number of debris pieces
                        10000000f, // duration in days
                        0f); // days the field will keep generating glowing pieces
                params_debrisField.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
                params_debrisField.baseSalvageXP = 500; // base XP for scavenging in field
                SectorEntityToken debrisField = Misc.addDebrisField(system, params_debrisField, StarSystemGenerator.random);
                debrisField.setSensorProfile(1000f);
                debrisField.setDiscoverable(true);
                SectorEntityToken loc = util.getRandomLocationInSystem(system,false,true,new Random());
                debrisField.setCircularOrbit(loc.getOrbitFocus(),loc.getCircularOrbitAngle(), loc.getCircularOrbitRadius(), loc.getCircularOrbitPeriod());
                debrisField.setId("nskr_debrisField_"+new Random().nextLong());

                //makes sure we are not in a star
                questUtil.spawnAwayFromStarFixer(debrisField);

                //ships
                float recoveryChance = 0.25f;
                int count = MathUtils.getRandomNumberInRange(2,4);
                String faction = "enigma";
                for (int y=0;y<count;y++) {
                    SectorEntityToken derelict = addDerelict(system, pickRandomVariant(faction, new Random(), true), randomCondition(), Math.random() < recoveryChance, null);
                    derelict.setCircularOrbit(debrisField, (float)(Math.random()*360f), MathUtils.getRandomNumberInRange(50f,250f), MathUtils.getRandomNumberInRange(30f, 90f));
                }

                log("added storyTelling secondary to "+system.getName());

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

    public static String pickRandomVariant(String factionId, Random random, boolean enigma) {
        FactionAPI faction = Global.getSector().getFaction(factionId);
        ArrayList<String> variants = new ArrayList<>();
        while (variants.isEmpty()) {
            variants = new ArrayList<>(faction.getVariantsForRole(randomRole(random, enigma)));
            //at least 2 ships in role
            if (variants.size()<=1) variants.clear();
        }

        String variant = variants.get(MathUtils.getRandomNumberInRange(0,variants.size()-1));
        return variant;
    }

    public static String randomRole(Random random, boolean enigma) {
        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
        picker.setRandom(random);
        String role = null;
        while (role == null) {
            for (Pair<String, Float> s : ROLES) {
                picker.add(s.one, s.two);
            }
            role = picker.pick();
            //chance to skip cruiser pick for Enigma gen
            if (enigma && role.equals(ShipRoles.COMBAT_LARGE) && random.nextFloat()<0.95f) role = null;
        }

        return role;
    }

    public static String randomFaction() {
        String faction = DERELICT_FACTIONS.get(MathUtils.getRandomNumberInRange(0, DERELICT_FACTIONS.size()-1));

        return faction;
    }

    public static ShipRecoverySpecial.ShipCondition randomCondition() {
        float condition = (float) Math.random();
        ShipRecoverySpecial.ShipCondition shipCondition;
        if (condition < 0.3f) {
            shipCondition = ShipRecoverySpecial.ShipCondition.WRECKED;
        } else if (condition < 0.6f) {
            shipCondition = ShipRecoverySpecial.ShipCondition.BATTERED;
        } else if (condition < 0.9f) {
            shipCondition = ShipRecoverySpecial.ShipCondition.AVERAGE;
        } else {
            shipCondition = ShipRecoverySpecial.ShipCondition.GOOD;
        }
        return shipCondition;
    }

    public static SectorEntityToken addDerelict(StarSystemAPI system, String variantId,
                                   ShipRecoverySpecial.ShipCondition condition, boolean recoverable,
                                   @Nullable DefenderDataOverride defenders) {

        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), false);
        SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);

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
