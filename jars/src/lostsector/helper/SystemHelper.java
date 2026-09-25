package lostsector.helper;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import lostsector.helper.fleet.SystemPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

public class SystemHelper {

    static void log(final String message) {
        Global.getLogger(SystemHelper.class).info(message);
    }

    //backup for blacklist generation, this one just returns a random non-core system
    public static StarSystemAPI getRandomNonCoreSystem(Random random) {

        SystemPicker simpleSystem = new SystemPicker(random, 1);
        simpleSystem.pickOnlyInProcgen = true;

        if (!simpleSystem.get().isEmpty()) {
            return simpleSystem.pick();
        }
        log("ERROR bruh you blacklisted every system in the game");
        return null;
    }

    //backup for blacklist generation, this one just returns a random core system with a market
    public static StarSystemAPI getRandomSystemWithMarket(Random random) {
        //pick tags
        List<String> pickTags = new ArrayList<>();
        pickTags.add(Tags.THEME_CORE);
        pickTags.add(Tags.THEME_CORE_POPULATED);

        SystemPicker simpleSystem = new SystemPicker(random, 1);
        simpleSystem.allowCore = true;
        simpleSystem.allowMarkets = true;
        simpleSystem.pickOnlyMarket = true;

        simpleSystem.pickTags = pickTags;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("ERROR no valid system");
        return SystemHelper.getRandomNonCoreSystem(random);
    }

    public static SectorEntityToken getRandomMarket(Random random, boolean allowPiratesEtc){

        ArrayList<MarketAPI> markets = new ArrayList<>();
        for (StarSystemAPI system : Global.getSector().getStarSystems()){
            for (SectorEntityToken e : system.getAllEntities()){
                if (e.getMarket() == null) continue;
                if (e.getMarket().getFactionId() == null) continue;
                if (e.getMarket().isPlanetConditionMarketOnly()) continue;
                if (e.getMarket().isHidden()) continue;
                if (e.getMarket().getFactionId().equals(Factions.NEUTRAL)) continue;
                if (!allowPiratesEtc) {
                    if (e.getMarket().getFaction().getId().equals(Factions.PIRATES) || e.getMarket().getFaction().getId().equals(Factions.LUDDIC_PATH)) continue;
                }
                markets.add(e.getMarket());
            }
        }
        if (markets.isEmpty()){
            log("ERROR no random market in the sector, what...");
            return null;
        }
        MarketAPI randomMarket = markets.get(MathHelper.getSeededRandomNumberInRange(0, markets.size()-1, random));

        return randomMarket.getPrimaryEntity();
    }

    public static SectorEntityToken getRandomFactionMarket(Random random, String faction) {
        return getRandomFactionMarket(random, faction, new ArrayList<String>());
    }

    public static SectorEntityToken getRandomFactionMarket(Random random, String faction, List<String> blacklist) {
        List<MarketAPI> validMarkets = new ArrayList<>();
        for (MarketAPI market : Misc.getFactionMarkets(faction)) {
            boolean isValid = true;
            StarSystemAPI system = market.getStarSystem();
            //crash on hyperspace markets
            if (system==null) continue;
            if (system.hasTag(Tags.THEME_HIDDEN) || system.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER) ||
                    system.getStar() == null || system.getPlanets().size()<1) {
                isValid = false;
            }
            if (blacklist.contains(market.getId())){
                isValid = false;
            }
            if (market.isHidden() || market.isPlanetConditionMarketOnly()){
                isValid = false;
            }
            if (isValid) {
                validMarkets.add(market);
            }
        }
        if (validMarkets.isEmpty()){
            if (!blacklist.isEmpty()){
                log("ERROR no valid " + faction + " markets with blacklist retry");
                return getRandomFactionMarket(random, faction, new ArrayList<String>());
            } else {
                log("ERROR no valid " + faction + " markets picking random market");
                return getRandomMarket(random, false);
            }
        }
        return validMarkets.get(MathHelper.getSeededRandomNumberInRange(0,validMarkets.size()-1, random)).getPrimaryEntity();
    }

    public static boolean hasGate(StarSystemAPI sys){
        boolean gate = false;
        for (SectorEntityToken e : sys.getAllEntities()){
            if (e.getCustomEntityType()==null)continue;
            if (!e.getCustomEntityType().equals(Entities.INACTIVE_GATE)) continue;
            gate = true;
            log("has GATE "+e.getName());
            break;
        }
        return gate;
    }

    public static boolean hasRelay(StarSystemAPI sys){
        boolean relay = false;
        for (SectorEntityToken e : sys.getAllEntities()){
            if (e.getCustomEntityType()==null)continue;
            if (!e.getCustomEntityType().equals(Entities.COMM_RELAY)) continue;
            relay = true;
            log("has RELAY "+e.getName());
            break;
        }
        return relay;
    }

    public static SectorEntityToken getRelay(StarSystemAPI sys){
        SectorEntityToken relay = null;
        for (SectorEntityToken e : sys.getAllEntities()){
            if (e.getCustomEntityType()==null)continue;
            if (!e.getCustomEntityType().equals(Entities.COMM_RELAY)) continue;
            relay = e;
            break;
        }
        return relay;
    }

    public static SectorEntityToken getRandomLocationInSystem(StarSystemAPI system, boolean allowStar, boolean allowOuterSystem, Random random){
        LinkedHashMap<BaseThemeGenerator.LocationType, Float> weights = new LinkedHashMap<>();
        weights.put(BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT, 8f);
        weights.put(BaseThemeGenerator.LocationType.IN_ASTEROID_FIELD, 8f);
        weights.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 8f);
        weights.put(BaseThemeGenerator.LocationType.JUMP_ORBIT, 8f);
        weights.put(BaseThemeGenerator.LocationType.IN_SMALL_NEBULA, 8f);
        if (allowOuterSystem) {
            weights.put(BaseThemeGenerator.LocationType.OUTER_SYSTEM, 8f);
        }
        // Star orbits only when allowed, to avoid spawning inside star coronas.
        if (allowStar) {
            weights.put(BaseThemeGenerator.LocationType.STAR_ORBIT, 8f);
            weights.put(BaseThemeGenerator.LocationType.NEAR_STAR, 8f);
            weights.put(BaseThemeGenerator.LocationType.IN_ASTEROID_BELT, 8f);
            weights.put(BaseThemeGenerator.LocationType.IN_RING, 8f);
            weights.put(BaseThemeGenerator.LocationType.L_POINT, 8f);
        }

        SectorEntityToken target = null;
        int maxTries = 200;
        for (int x = 0; x < maxTries; x++) {
            //Gets a list of random locations in the system, and picks one
            WeightedRandomPicker<BaseThemeGenerator.EntityLocation> validPoints = BaseThemeGenerator.getLocations(random, system, 50f, weights);
            BaseThemeGenerator.EntityLocation tTarget = validPoints.pick();
            if (tTarget != null && tTarget.orbit != null && tTarget.orbit.getFocus() !=null) {
                //pick star anyways if we can't find other spot
                if(x>(maxTries/2)){
                    target = tTarget.orbit.getFocus();
                    break;
                }
                if (!allowStar && tTarget.orbit.getFocus().isStar()){
                    continue;
                }
                target = tTarget.orbit.getFocus();
                break;
            }
        }
        if(target==null){
            if (!allowStar || !allowOuterSystem){
                log("ERROR randomLocationInSystem target is null, RETRY");
                target = getRandomLocationInSystem(system, true, true, random);
            } else {
                log("ERROR randomLocationInSystem target is null, return centre");
                target = system.getCenter();
            }
        }
        return target;
    }

    public static OrbitAPI createRandomNearOrbit(SectorEntityToken loc){
        return Global.getFactory().createCircularOrbit(loc, (float)Math.random() * 360.0f, MathUtils.getRandomNumberInRange(150f, 550f), MathUtils.getRandomNumberInRange(12,24));
    }

    public static SectorEntityToken spawnAwayFromStarFixer(SectorEntityToken entity){
        return spawnAwayFromStarFixer(entity, 1f);
    }

    public static SectorEntityToken spawnAwayFromStarFixer(SectorEntityToken entity, float extraDistanceMult){
        PlanetAPI planet = getNearestPlanetEntity(entity);
        if (planet==null){
            log("ERROR no planets");
            return entity;
        }
        PlanetAPI focus = null;
        if (entity.getOrbit()!=null&&entity.getOrbitFocus()!=null){
            if (entity.getOrbitFocus() instanceof PlanetAPI){
                focus = (PlanetAPI) entity.getOrbitFocus();
                log("focus " +focus.getName());
                log("planet " +planet.getName());
            }
        }
        //move away from planet or star
        float length = MathUtils.getDistance(entity.getLocation(), planet.getLocation());
        float end_x = 0f;
        float end_y = 0f;
        if (planet.isStar()) extraDistanceMult = 2.0f;
        if (focus!=null && planet!=focus) extraDistanceMult = 1f;
        float toDistance = (planet.getRadius() * 1.25f) * extraDistanceMult;
        if (planet.isStar() && length<=0f){
            Vector2f newVector = Vector2f.add(new Vector2f(100f,100f), entity.getLocation(), null);
            entity.setLocation(newVector.getX(), newVector.getY());
            length = MathUtils.getDistance(entity.getLocation(), planet.getLocation());
            log("fixed 0 location");
        }
        log("planet "+planet.getName()+" toDist "+toDistance+" ent "+entity.getName());
        float endLength = 0f;
        if (length>0f && length < toDistance) {
            while (length < toDistance) {
                Vector2f vector = new Vector2f(MathHelper.scaleVector(Vector2f.sub(entity.getLocation(), planet.getLocation(), entity.getLocation()), 1.25f));
                length = vector.length();
                endLength = vector.length();
                end_x = planet.getLocation().getX() + length * (vector.getX() / length);
                end_y = planet.getLocation().getY() + length * (vector.getY() / length);
                log("x " + end_x + " y " + end_y + " length " + length + " goal " + toDistance + " extra " + extraDistanceMult);
                entity.setLocation(end_x, end_y);
                log("MiscLS moved " + entity.getName() + " loc " + entity.getContainingLocation().getName());
            }
        } else return entity;

        OrbitAPI newOrbit;
        if (endLength>0f) length = endLength;

        float angle = VectorUtils.getAngle(planet.getLocation(), entity.getLocation());
        float days = MathUtils.getRandomNumberInRange(1f, 1.25f) * (length/15f);
        if (entity.getOrbit()!=null && entity.getOrbitFocus()!=null) {
            newOrbit = Global.getFactory().createCircularOrbit(entity.getOrbitFocus(), angle, length, days);
        } else {
            newOrbit = Global.getFactory().createCircularOrbit(planet, angle, length, days);
        }
        entity.setOrbit(newOrbit);
        log ("MiscLS finished "+entity.getName()+" dist "+MathUtils.getDistance(entity.getLocation(), planet.getLocation())+ " target "+length);

        return entity;
    }

    private static PlanetAPI getNearestPlanetEntity(SectorEntityToken entity) {
        float dist = Float.MAX_VALUE;
        float newDist = 0f;
        PlanetAPI nearest = null;
        for (SectorEntityToken e : entity.getStarSystem().getAllEntities()){
            if (e instanceof PlanetAPI){
                newDist = MathUtils.getDistance(e.getLocation(), entity.getLocation()) - e.getRadius();
                if (newDist<dist) {
                    dist = newDist;
                    nearest = (PlanetAPI)e;
                }
            }
        }
        return nearest;
    }

    public static float getDistanceFromNearestSystem(Vector2f loc){
        float shortestDist = Float.MAX_VALUE;
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (system.getHyperspaceAnchor( )== null) continue;
            if (system.hasTag(Tags.THEME_HIDDEN)) continue;
            float dist =  MathUtils.getDistance(loc, system.getHyperspaceAnchor().getLocationInHyperspace());
            if (dist>shortestDist) continue;
            shortestDist = dist;
        }
        return shortestDist;
    }

    public static StarSystemAPI getNearestSystem(Vector2f loc){
        float shortestDist = Float.MAX_VALUE;
        StarSystemAPI sys = null;
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (system.getHyperspaceAnchor() == null) continue;
            if (system.hasTag(Tags.THEME_HIDDEN)) continue;
            float dist =  MathUtils.getDistance(loc, system.getHyperspaceAnchor().getLocationInHyperspace());
            if (dist>shortestDist) continue;
            shortestDist = dist;
            sys = system;
        }
        return sys;
    }

    public static SectorEntityToken swapSalvageEntity(SectorEntityToken from, String to, Random random){
        StarSystemAPI sys = from.getStarSystem();
        SectorEntityToken focus = from.getOrbitFocus();
        float angle = from.getCircularOrbitAngle();
        float period = from.getCircularOrbitPeriod();
        float radius = from.getCircularOrbitRadius();
        float facing = from.getFacing();

        SectorEntityToken toEntity = BaseThemeGenerator.addSalvageEntity(random, from.getStarSystem().getStar().getContainingLocation(), to, Factions.NEUTRAL);
        toEntity.setCircularOrbitPointingDown(focus, angle, radius, period);
        toEntity.setFacing(facing);

        from.setExpired(true);
        sys.removeEntity(from);

        return toEntity;
    }

    public static SectorEntityToken swapEntity(SectorEntityToken from, String to){
        StarSystemAPI sys = from.getStarSystem();
        SectorEntityToken focus = from.getOrbitFocus();
        float angle = from.getCircularOrbitAngle();
        float period = from.getCircularOrbitPeriod();
        float radius = from.getCircularOrbitRadius();
        float facing = from.getFacing();

        BaseThemeGenerator.EntityLocation loc = new BaseThemeGenerator.EntityLocation();
        loc.location = from.getLocation();
        loc.type = BaseThemeGenerator.LocationType.NEAR_STAR;

        SectorEntityToken toEntity = BaseThemeGenerator.addNonSalvageEntity(from.getStarSystem().getStar().getContainingLocation(), loc, to, Factions.NEUTRAL).entity;
        toEntity.setCircularOrbitPointingDown(focus, angle, radius, period);
        toEntity.setFacing(facing);

        from.setExpired(true);
        sys.removeEntity(from);

        return toEntity;
    }

    public static boolean hasNeutronStar(StarSystemAPI sys) {
        for (SectorEntityToken e : sys.getAllEntities()){
            if (e instanceof PlanetAPI) {
                if (!e.isStar()) continue;
                if (((PlanetAPI) e).getTypeId()==null) continue;
                if (((PlanetAPI) e).getTypeId().equals(StarTypes.NEUTRON_STAR)) {
                    return true;
                }
            }
        }
        return false;
    }
}
