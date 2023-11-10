package scripts.kissa.LOST_SECTOR.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.combat.entities.Ship;
import org.jetbrains.annotations.Nullable;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class combatUtil {

    static void log(final String message) {
        Global.getLogger(combatUtil.class).info(message);
    }

    /* LazyLib 2.4b revert */
    public static List<ShipAPI> getShipsWithinRange(Vector2f location, float range) {
        List<ShipAPI> ships = new ArrayList<>();

        for (ShipAPI tmp : Global.getCombatEngine().getShips()) {
            if (tmp.isShuttlePod()) {
                continue;
            }

            if (MathUtils.isWithinRange(tmp, location, range)) {
                ships.add(tmp);
            }
        }

        return ships;
    }

    /* LazyLib 2.4b revert */
    public static List<DamagingProjectileAPI> getProjectilesWithinRange(Vector2f location, float range) {
        List<DamagingProjectileAPI> projectiles = new ArrayList<>();

        for (DamagingProjectileAPI tmp : Global.getCombatEngine().getProjectiles()) {
            if (tmp instanceof MissileAPI) {
                continue;
            }

            if (MathUtils.isWithinRange(tmp.getLocation(), location, range)) {
                projectiles.add(tmp);
            }
        }

        return projectiles;
    }

    /* LazyLib 2.4b revert */
    public static List<MissileAPI> getMissilesWithinRange(Vector2f location, float range) {
        List<MissileAPI> missiles = new ArrayList<>();

        for (MissileAPI tmp : Global.getCombatEngine().getMissiles()) {
            if (MathUtils.isWithinRange(tmp.getLocation(), location, range)) {
                missiles.add(tmp);
            }
        }

        return missiles;
    }

    /* LazyLib 2.4b revert */
    public static List<CombatEntityAPI> getAsteroidsWithinRange(Vector2f location, float range) {
        List<CombatEntityAPI> asteroids = new ArrayList<>();

        for (CombatEntityAPI tmp : Global.getCombatEngine().getAsteroids()) {
            if (MathUtils.isWithinRange(tmp, location, range)) {
                asteroids.add(tmp);
            }
        }

        return asteroids;
    }

    public static float getNearestEnemyShipDistance(ShipAPI ship, Vector2f point, float range){
        float dist = range;
        List<ShipAPI> targets = CombatUtils.getShipsWithinRange(point,range);
        for (ShipAPI t : targets){
            if (t.getOwner()==ship.getOwner()) continue;
            if (t.getHullSize()== ShipAPI.HullSize.FIGHTER) continue;
            float tDist = MathUtils.getDistance(point, t.getLocation());
            if(tDist>=range) continue;
            dist = Math.min(dist, tDist);
        }
        return dist;
    }

    public static Vector2f getNearestPointOnCollisionRadius(Vector2f point, CombatEntityAPI target){
        Vector2f tPoint = target.getLocation();
        float range = 1f;
        for (int i = 0; i < 2000; i++) {
            tPoint = MathUtils.getPointOnCircumference(point, range, VectorUtils.getAngle(point, target.getLocation()));

            range += 5f;
            if (range>9999f) break;
            if (!CollisionUtils.isPointWithinCollisionCircle(tPoint, target))continue;
            break;
        }
        return tPoint;
    }
    public static Vector2f getNearestPointOnBounds(Vector2f point, CombatEntityAPI entity)
    {
        Vector2f tPoint = entity.getLocation();
        float range = 1f;
        for (int i = 0; i < 2000; i++) {
            tPoint = MathUtils.getPointOnCircumference(point, range, VectorUtils.getAngle(point, entity.getLocation()));
            //Global.getCombatEngine().addFloatingText(tPoint, "lol", 20f, Color.cyan, null, 0.5f, 0.5f);
            range += 5f;
            if (range>9999f) break;
            //collision circle check is faster so do that first
            if (!CollisionUtils.isPointWithinCollisionCircle(tPoint, entity))continue;
            if (!CollisionUtils.isPointWithinBounds(tPoint, entity))continue;
            break;
        }
        return tPoint;
    }

    public static void applyAOEDamage(ShipAPI source, @Nullable CombatEntityAPI mainTarget, Vector2f point, float baseDamage, DamageType type, float expRange, boolean friendlyFire){
        CombatEngineAPI engine = Global.getCombatEngine();
        List<CombatEntityAPI> targets = new ArrayList<>();
        targets.addAll(getShipsWithinRange(point, expRange));
        targets.addAll(getMissilesWithinRange(point, expRange));
        List<CombatEntityAPI> targetsCopy = new ArrayList<>(targets);
        //make sure to add parent ships always
        for (CombatEntityAPI c : targetsCopy){
            if (c instanceof ShipAPI) {
                ShipAPI ship = (ShipAPI) c;
                ShipAPI parent = null;
                if (ship.isStationModule()){
                    parent = ship.getParentStation();
                }
                if (parent!=null && !targets.contains(parent)){
                    targets.add(parent);
                }
            }
        }
        List<CombatEntityAPI> validTargets = new ArrayList<>();
        for(CombatEntityAPI c : targets){
            if (c == source && !friendlyFire)continue;
            if (c.getOwner() == source.getOwner() && !friendlyFire)continue;
            if (!(c instanceof ShipAPI) && !(c instanceof MissileAPI))continue;
            if (mainTarget != null && c == mainTarget)continue;
            if (c.isExpired())continue;
            if (c instanceof ShipAPI && !((ShipAPI) c).isAlive())continue;
            if (c.getCollisionClass()==CollisionClass.NONE)continue;
            //add normal targets
            if (c instanceof ShipAPI) {
                ShipAPI ship = (ShipAPI) c;
                if (!ship.isShipWithModules() && !ship.isStationModule() && !ship.isStation()){
                    validTargets.add(ship);
                }
                //only do the valid check for the main ship
                if (ship.isStationModule()) continue;
            } else {
                validTargets.add(c);
            }

            //checking for modules isn't a nightmare, why do you ask?
            //for module-ships/stations we only apply damage to a single module or the parent
            if (c instanceof ShipAPI) {
                ShipAPI station = (ShipAPI)c;
                CombatEntityAPI target = station;
                if (!station.getChildModulesCopy().isEmpty()){
                    //shield
                    float dist = Float.MAX_VALUE;
                    float newDist;
                    //bounds
                    float hullDist = Float.MAX_VALUE;
                    float newHullDist;
                    //check if parent has shield on
                    if ((station.getShield() != null && station.getShield().isOn() && station.getShield().isWithinArc(point))) {
                        dist = MathUtils.getDistance(getNearestPointOnCollisionRadius(point, station), point);
                    } else {
                        hullDist = MathUtils.getDistance(getNearestPointOnBounds(point, station), point);
                    }
                    //check if module shield are closer
                    ShipAPI closest = null;
                    ShipAPI closestHull = null;
                    for (ShipAPI module : station.getChildModulesCopy()){
                        newDist = MathUtils.getDistance(getNearestPointOnCollisionRadius(point, module), point);
                        newHullDist = MathUtils.getDistance(getNearestPointOnBounds(point, module), point);
                        if ((module.getShield() != null && module.getShield().isOn() && module.getShield().isWithinArc(point))) {
                            if (dist > newDist){
                                dist = newDist;
                                closest = module;
                            }
                        } else {
                            if (hullDist > newHullDist){
                                hullDist = newHullDist;
                                closestHull = module;
                            }
                        }
                    }
                    //target is closest module shield

                    //String main = "";
                    //if (mainTarget!=null) {
                    //    main = mainTarget.toString();
                    //}
                    if (closest!=null){
                        target = closest;
                        //engine.addFloatingText(closest.getLocation(), "CLOSEST shield " + closest.getHullSpec().getHullName() + "   " +dist+"   MainTarget "+main, 24f, Color.CYAN, null, 1f,1f);
                        //if a bounds target is closer than the shield, target is the closest bounds one
                        if (hullDist < dist && closestHull!=null){
                            target = closestHull;
                            //engine.addFloatingText(closestHull.getLocation(), "CLOSEST hull " + closestHull.getHullSpec().getHullName() + "   " +hullDist+"   MainTarget "+main, 24f, Color.CYAN, null, 1f,1f);
                        }
                    }
                    //if there are no shields, target is the closest one
                    if (closest==null && closestHull!=null){
                        target = closestHull;
                        //engine.addFloatingText(closestHull.getLocation(), "CLOSEST hull no shields " + closestHull.getHullSpec().getHullName() + "   " +hullDist+"   MainTarget "+main, 24f, Color.CYAN, null, 1f,1f);
                    }
                    //default target is the station, if its shield or bounds are closer
                }
                validTargets.add(target);
            }
        }
        //apply
        for (CombatEntityAPI c : validTargets) {
            if (c==null) continue;
            if (c==mainTarget) continue;
            applyAOE(source, point, baseDamage, type, expRange, c);
            //engine.addFloatingText(c.getLocation(), "VALID TARGET", 24f, Color.CYAN, null, 1f,1f);
        }
    }

    private static void applyAOE(ShipAPI source, Vector2f point, float baseDamage, DamageType type, float expRange, CombatEntityAPI c) {
        CombatEngineAPI engine = Global.getCombatEngine();
        float range = Math.min(expRange, MathUtils.getDistance(point, getNearestPointOnCollisionRadius(point, c)));
        float damage = baseDamage;
        damage *= mathUtil.smoothStep(1f- mathUtil.normalize(range,0f, expRange));
        if (damage<=0f) return;
        if (c instanceof ShipAPI){
            if ((c.getShield() != null && c.getShield().isOn() && c.getShield().isWithinArc(
                    point))) {

                ((ShipAPI) c).getFluxTracker().increaseFlux(damage * c.getShield().getFluxPerPointOfDamage(), true);

                //engine.addFloatingText(c.getLocation(), "F " + (int)damage + " N " + ((ShipAPI) c).getHullSpec().getHullName(), 32f, Color.cyan, null, 0.5f, 0.5f);
                //log("F " + (int)damage + " N " + ((ShipAPI) c).getHullSpec().getHullName());
            } else {
                Vector2f nPoint = getNearestPointOnBounds(point, c);
                float dist = MathUtils.getDistance(point, nPoint);
                float rangeB = Math.min(expRange, dist);
                float damageB = baseDamage;
                damageB *= 1f - mathUtil.smoothStep(mathUtil.normalize(rangeB, 0f, expRange));
                if (damageB <= 0f) return;

                //engine.addFloatingText(c.getLocation(), "D " + (int) damageB + " N " + ((ShipAPI) c).getHullSpec().getHullName(), 32f, Color.cyan, null, 0.5f, 0.5f);
                //log("D " + (int) damageB + " N " + ((ShipAPI) c).getHullSpec().getHullName());
                engine.applyDamage(c, nPoint, damageB, type, 0f, false, false, source);
            }
        } else {
            //engine.addFloatingText(c.getLocation(), "E " + (int)damage, 32f, Color.cyan, null, 0.5f, 0.5f);
            engine.applyDamage(c, c.getLocation(), damage, type, 0f, false, false, source);
        }
    }

    public static void createHitParticles(Vector2f point, float facing, float arc, Color color, float count, float size, float opacity, float duration, float range, float spdMult) {
        Vector2f particlePos, particleVel;
        for (int x = 0; x < count;) {
            particlePos = point;
            for (int y = 0; y < 100; y++) {
                float rArc = arc*MathUtils.getRandomNumberInRange(0.8f,1.2f);
                particlePos = MathUtils.getRandomPointOnCircumference(point, (float) Math.random() * range);
                float angle = VectorUtils.getAngle(particlePos, point);
                // ignore everything outside of a y degree cone
                if (Math.abs(MathUtils.getShortestRotation(angle, facing)) < rArc) break;
            }
            particleVel = Vector2f.sub(particlePos, point, null);
            mathUtil.scaleVector(particleVel, spdMult);
            Global.getCombatEngine().addSmokeParticle(particlePos, particleVel, size, opacity, duration,
                    color);
            x++;
        }
    }

    public static Vector2f getRandomPointOnShip(ShipAPI ship){
        Vector2f point = ship.getLocation();
        float radius = ship.getCollisionRadius();
        try_again:
        for (int i = 0; i < 100; i++) {
            point = MathUtils.getRandomPointInCircle(ship.getLocation(),radius);
            List<ShipAPI> toCheck = new ArrayList<>(CombatUtils.getShipsWithinRange(point, 1f));
            if (toCheck.isEmpty())continue;
            for (ShipAPI c : toCheck){
                if (c!=ship)continue try_again;
                if (!CollisionUtils.isPointWithinBounds(point, c))continue try_again;
            }
            break;
        }
        return point;
    }

}
