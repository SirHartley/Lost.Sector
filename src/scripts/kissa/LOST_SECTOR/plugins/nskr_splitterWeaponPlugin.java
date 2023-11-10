//////////////////////
//Initially created by DarkRevenant and modified from Ship and Weapon Pack
//////////////////////
package scripts.kissa.LOST_SECTOR.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class nskr_splitterWeaponPlugin extends BaseEveryFrameCombatPlugin {

    //magic don't ask me how this works..

    public static final String DATA_KEY = "nskr_SplitterWeapon";

    public static final float PBCC_DEFAULT_RANGE = 1200f;
    public static final float PBCC_DEFAULT_SPEED = 600f;
    public static final Color PBCC_DETONATION_COLOR = new Color(150, 75, 255, 225);
    public static final float PBCC_DETONATION_DURATION = 0.4f;
    public static final float PBCC_DETONATION_SIZE = 40f;
    public static final String PBCC_DETONATION_SOUND_ID = "nskr_pbcc_split";
    public static final float PBCC_FUSE_DISTANCE = 100f;
    public static final Color PBCC_PARTICLE_COLOR = new Color(185, 150, 255, 200);
    public static final int PBCC_PARTICLE_COUNT = 40;
    public static final String PBCC_PROJECTILE_ID = "nskr_pbcc_shot";
    public static final float PBCC_SPLIT_DISTANCE = 300f;
    public static final float PBCC_SPREAD_FORCE_MAX = 80f;
    public static final float PBCC_SPREAD_FORCE_MIN = 30f;
    public static final int PBCC_SUBMUNITIONS = 4;
    public static final String PBCC_SUBMUNITION_WEAPON_ID = "nskr_pbcc_sub";
    public static final float ORIGINAL_PROJECTILE_DAMAGE_MULTIPLIER = 0.5f;

    private CombatEngineAPI engine;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Set<DamagingProjectileAPI> projectileSet = localData.projectileSet;

        // We can't safely decrease the polling rate, so this will run at full speed...
        List<DamagingProjectileAPI> projectiles = engine.getProjectiles();
        int size = projectiles.size();
        for (int i = 0; i < size; i++) {
            DamagingProjectileAPI proj = projectiles.get(i);
            String spec = proj.getProjectileSpecId();
            if (spec == null) {
                continue;
            }

            String newSpec;
            int submunitions;
            float fuseDistance;
            float splitDistance;
            float splitForceMin;
            float splitForceMax;
            float defaultRange;
            float defaultSpeed;
            Color detonateColor;
            float detonateSize;
            float detonateDuration;
            String detonateSound;
            Color particleColor;
            int particleCount;
            if (PBCC_PROJECTILE_ID.equals(spec)) {
                newSpec = PBCC_SUBMUNITION_WEAPON_ID;
                submunitions = PBCC_SUBMUNITIONS;
                fuseDistance = PBCC_FUSE_DISTANCE;
                splitDistance = PBCC_SPLIT_DISTANCE;
                splitForceMin = PBCC_SPREAD_FORCE_MAX;
                splitForceMax = PBCC_SPREAD_FORCE_MIN;
                defaultRange = PBCC_DEFAULT_RANGE;
                defaultSpeed = PBCC_DEFAULT_SPEED;
                detonateColor = PBCC_DETONATION_COLOR;
                detonateSize = PBCC_DETONATION_SIZE;
                detonateDuration = PBCC_DETONATION_DURATION;
                detonateSound = PBCC_DETONATION_SOUND_ID;
                particleColor = PBCC_PARTICLE_COLOR;
                particleCount = PBCC_PARTICLE_COUNT;
            } else {
                continue;
            }

            if (proj.isFading() || proj.didDamage()) {
                projectileSet.remove(proj);
                continue;
            }

            if (!projectileSet.contains(proj)) {
                projectileSet.add(proj);
                proj.getDamage().setDamage(proj.getDamage().getDamage() * ORIGINAL_PROJECTILE_DAMAGE_MULTIPLIER);
            }

            boolean shouldSplit = false;
            Vector2f loc = proj.getLocation();
            Vector2f vel = proj.getVelocity();
            float speedScalar;
            float rangeScalar;
            if (proj.getSource() != null) {
                speedScalar = proj.getSource().getMutableStats().getProjectileSpeedMult().getModifiedValue();
                rangeScalar = proj.getSource().getMutableStats().getBallisticWeaponRangeBonus().computeEffective(
                        defaultRange) / defaultRange;
            } else {
                rangeScalar = 1f;
                speedScalar = 1f;
            }
            float speed = defaultSpeed * speedScalar;

            // Real quick and dirty fuse distance that works in most cases
            float fuseTime = fuseDistance / speed;
            if (proj.getElapsed() < fuseTime) {
                continue;
            }

            splitDistance *= rangeScalar;

            // This is some bullshit to make the weapon fade sooner than normal
            float detonateTime;
            if (proj.getWeapon() != null) {
                detonateTime = (proj.getWeapon().getRange() - splitDistance) / speed;
            } else {
                detonateTime = (defaultRange - splitDistance) / speed;
            }
            if (proj.getElapsed() >= detonateTime) {
                shouldSplit = true;
            }

            if (!shouldSplit) {
                // Check to see if the projectile should detonate
                Vector2f projection = new Vector2f(splitDistance, 0f);
                VectorUtils.rotate(projection, proj.getFacing(), projection);
                Vector2f.add(loc, projection, projection);

                List<ShipAPI> checkList = engine.getShips();
                List<ShipAPI> finalList = new LinkedList<>();
                int listSize = checkList.size();
                for (int j = 0; j < listSize; j++) {
                    ShipAPI ship = checkList.get(j);
                    boolean isInShields = false;
                    if (ship.getShield() != null && ship.getShield().isOn()) {
                        if (MathUtils.isWithinRange(loc, ship.getLocation(), ship.getShield().getRadius()
                                + splitDistance)) {
                            isInShields = ship.getShield().isWithinArc(loc);
                        }
                    }

                    if (!isInShields) {
                        if (!MathUtils.isWithinRange(loc, ship.getLocation(), ship.getCollisionRadius() + splitDistance)) {
                            continue;
                        }
                    }

                    if (isInShields) {
                        if (CollisionUtils.getCollides(loc, projection, ship.getLocation(), ship.getShield().getRadius())) {
                            finalList.add(ship);
                        }
                    } else if (CollisionUtils.getCollides(loc, projection, ship.getLocation(), ship.getCollisionRadius())) {
                        Vector2f point = CollisionUtils.getCollisionPoint(loc, projection, ship);
                        if (point != null && MathUtils.getDistance(loc, point) <= splitDistance) {
                            finalList.add(ship);
                        }
                    }

                }

                ShipAPI closest = null;
                float closestSquareDistance = Float.MAX_VALUE;
                listSize = finalList.size();
                for (int j = 0; j < listSize; j++) {
                    ShipAPI ship = finalList.get(j);
                    float squareDistance = MathUtils.getDistanceSquared(loc, ship.getLocation());
                    if (squareDistance < closestSquareDistance) {
                        closestSquareDistance = squareDistance;
                        closest = ship;
                    }
                }

                if (closest != null) {
                    if ((closest.getOwner() == 1 || closest.getOwner() == 0) && closest.getOwner() != proj.getOwner()) {
                        shouldSplit = true;
                    }
                }
            }

            if (shouldSplit) {
                Vector2f scaledVel = new Vector2f(vel);
                scaledVel.scale(0.5f);
                engine.spawnExplosion(loc, scaledVel, detonateColor, detonateSize, detonateDuration);
                Global.getSoundPlayer().playSound(detonateSound, 1f, 1f, loc, scaledVel);
                float forceMultiplier = vel.length() / speed;

                for (int j = 0; j < particleCount; j++) {
                    Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, speedScalar / rangeScalar
                            * forceMultiplier
                            * MathUtils.getRandomNumberInRange(
                                    splitForceMin, splitForceMax));
                    randomVel.scale((float) Math.random() + 0.75f);
                    Vector2f.add(vel, randomVel, randomVel);
                    randomVel.scale((float) Math.random() + 0.25f);
                    engine.addHitParticle(loc, randomVel, (float) Math.random() * 2f + 6f, 1f, ((float) Math.random()
                            * 0.75f + 1.25f)
                            * detonateDuration,
                            particleColor);
                }

                Vector2f defaultVel = new Vector2f(defaultSpeed * speedScalar, 0f);
                VectorUtils.rotate(defaultVel, proj.getFacing(), defaultVel);
                Vector2f actualVel = new Vector2f();
                for (int j = 0; j < submunitions; j++) {
                    Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, speedScalar / rangeScalar
                            * forceMultiplier
                            * MathUtils.getRandomNumberInRange(
                                    splitForceMin, splitForceMax));
                    Vector2f.add(defaultVel, randomVel, actualVel);
                    Vector2f.add(vel, randomVel, randomVel);
                    DamagingProjectileAPI subProj = (DamagingProjectileAPI) engine.spawnProjectile(proj.getSource(),
                            proj.getWeapon(),
                            newSpec, loc,
                            VectorUtils.getFacing(
                                    actualVel),
                            randomVel);
                    Vector2f subVel = subProj.getVelocity();
                    Vector2f.sub(subVel, defaultVel, subVel);
                }
                projectileSet.remove(proj);
                engine.removeEntity(proj);
            }
        }

        /* Clean up */
        Iterator<DamagingProjectileAPI> iter = projectileSet.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            if (!engine.isEntityInPlay(proj)) {
                iter.remove();
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
    }

    public static class LocalData {
        final Set<DamagingProjectileAPI> projectileSet = new HashSet<>(100);
    }
}
