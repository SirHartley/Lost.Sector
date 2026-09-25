package lostsector.campaign.bounties;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.RingBandAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.CoreLifecyclePluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Planets;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import lostsector.helper.MathHelper;
import lostsector.helper.fleet.SystemPicker;
import lostsector.world.systems.frost.Frost;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

// World generation for the Mothership bounty: the moonless gas giant it guards, and Helios and Polaris around it.
// ModPlugin places them after procgen, before Asteria is placed in Nexerelin random sectors, so the base location is
// sector persistent data from world generation; BountiesQuest reads it when the quest places the fleet.
public final class HeliosSite {

    public static final String PLANET1_ID = "nskr_terra1";
    public static final String PLANET2_ID = "nskr_terra2";
    private static final String BASE_KEY = "nskr_mothershipKey";

    private static final ArrayList<String> TYPES = new ArrayList<>();
    static {
        TYPES.add(Planets.PLANET_TERRAN);
        TYPES.add(Planets.PLANET_TERRAN_ECCENTRIC);
        TYPES.add(Planets.PLANET_WATER);
        TYPES.add(Planets.TUNDRA);
        TYPES.add("jungle");
        TYPES.add("arid");
    }

    private HeliosSite() {
    }

    static void log(final String message) {
        Global.getLogger(HeliosSite.class).info(message);
    }

    // Once per campaign, from ModPlugin.onNewGameAfterProcGen.
    public static void place() {
        spawnPlanets(getMothershipBaseLocation(), new Random());
    }

    // Null before world generation picked it, or when no procgen system has a moonless gas giant.
    public static SectorEntityToken base() {
        Object base = Global.getSector().getPersistentData().get(BASE_KEY);
        return base instanceof SectorEntityToken ? (SectorEntityToken) base : null;
    }

    // Helios and Polaris, the planets the Mothership fleet guards; none when the bounty was not placed.
    public static List<SectorEntityToken> planets() {
        List<SectorEntityToken> planets = new ArrayList<>();
        SectorEntityToken helios = Global.getSector().getEntityById(PLANET1_ID);
        SectorEntityToken polaris = Global.getSector().getEntityById(PLANET2_ID);
        if (helios != null) planets.add(helios);
        if (polaris != null) planets.add(polaris);
        return planets;
    }

    private static void spawnPlanets(SectorEntityToken loc, Random random) {
        if (loc == null) return;

        StarSystemAPI system = loc.getStarSystem();

        PlanetAPI a1 = system.addPlanet(PLANET1_ID, loc, "Helios", getRandomHabitableType(random),
                random.nextFloat()*360f,
                MathHelper.getSeededRandomNumberInRange(30f,50f, random),
                loc.getRadius() + MathHelper.getSeededRandomNumberInRange(200f,300f, random),
                MathHelper.getSeededRandomNumberInRange(90f,120f, random));
        PlanetConditionGenerator.generateConditionsForPlanet(null, a1, system.getAge());

        //ruins
        if (!a1.hasCondition(Conditions.RUINS_SCATTERED) && !a1.hasCondition(Conditions.RUINS_WIDESPREAD) && !a1.hasCondition(Conditions.RUINS_EXTENSIVE) && !a1.hasCondition(Conditions.RUINS_VAST)){
            a1.getMarket().addCondition(Frost.randomRuins());
            CoreLifecyclePluginImpl.addRuinsJunk(a1);
        }
        a1.addTag(Tags.NOT_RANDOM_MISSION_TARGET);

        PlanetAPI a2 = system.addPlanet(PLANET2_ID, loc, "Polaris", getRandomHabitableType(random),
                random.nextFloat()*360f,
                MathHelper.getSeededRandomNumberInRange(30f,50f, random),
                loc.getRadius() + MathHelper.getSeededRandomNumberInRange(400f,600f, random),
                MathHelper.getSeededRandomNumberInRange(120f,150f, random));
        PlanetConditionGenerator.generateConditionsForPlanet(null, a2, system.getAge());

        //ruins
        if (!a2.hasCondition(Conditions.RUINS_SCATTERED) && !a2.hasCondition(Conditions.RUINS_WIDESPREAD) && !a2.hasCondition(Conditions.RUINS_EXTENSIVE) && !a2.hasCondition(Conditions.RUINS_VAST)){
            a2.getMarket().addCondition(Frost.randomRuins());
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

    private static String getRandomHabitableType(Random random) {
        return TYPES.get(MathHelper.getSeededRandomNumberInRange(0, TYPES.size()-1, random));
    }

    private static SectorEntityToken getMothershipBaseLocation() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = BASE_KEY;
        if (!data.containsKey(id))
            data.put(id, randomGasGiant(new Random()));

        return (SectorEntityToken)data.get(id);
    }

    // Null when no procgen system has a moonless gas giant; the bounty is then skipped.
    private static SectorEntityToken randomGasGiant(Random random) {
        StarSystemAPI sys = getRandomSystemWithBlacklist(random);
        if (sys == null) {
            log("ERROR no system with a moonless gas giant");
            return null;
        }

        List<PlanetAPI> giants = getMoonlessGasGiants(sys);
        return giants.get(MathHelper.getSeededRandomNumberInRange(0,giants.size()-1, random));
    }

    // Helios and Polaris orbit the gas giant closely, so it must have no moons.
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

        SystemPicker simpleSystem = new SystemPicker(new Random(), 1);
        simpleSystem.pickTags = pickTags;
        simpleSystem.blacklistStars = banTypes;
        simpleSystem.blacklistSystemTypes = banSystems;
        simpleSystem.pickOnlyInProcgen = true;

        List<StarSystemAPI> validSystems = getSystemsWithMoonlessGasGiant(simpleSystem.get());
        if (validSystems.isEmpty()) {
            log("no Remnant system with a moonless gas giant, trying other systems");
            simpleSystem.pickTags = new ArrayList<>();
            validSystems = getSystemsWithMoonlessGasGiant(simpleSystem.get());
        }
        if (validSystems.isEmpty()) {
            SystemPicker anySystem = new SystemPicker(random, 1);
            anySystem.pickOnlyInProcgen = true;
            validSystems = getSystemsWithMoonlessGasGiant(anySystem.get());
        }
        if (validSystems.isEmpty()) return null;

        return validSystems.get(MathHelper.getSeededRandomNumberInRange(0,validSystems.size()-1, random));
    }

    private static List<StarSystemAPI> getSystemsWithMoonlessGasGiant(List<StarSystemAPI> systems) {
        List<StarSystemAPI> validSystems = new ArrayList<>();
        for (StarSystemAPI sys : systems){
            if (!getMoonlessGasGiants(sys).isEmpty()) validSystems.add(sys);
        }
        return validSystems;
    }

    private static List<PlanetAPI> getMoonlessGasGiants(StarSystemAPI sys) {
        List<PlanetAPI> giants = new ArrayList<>();
        for (PlanetAPI p : sys.getPlanets()){
            if (p == null || !p.isGasGiant()) continue;
            if (hasMoons(sys, p)) continue;
            giants.add(p);
        }
        return giants;
    }

    private static boolean hasMoons(StarSystemAPI sys, PlanetAPI p) {
        for (PlanetAPI moon : sys.getPlanets()){
            if (moon.getOrbitFocus() == p) return true;
        }
        return false;
    }
}
