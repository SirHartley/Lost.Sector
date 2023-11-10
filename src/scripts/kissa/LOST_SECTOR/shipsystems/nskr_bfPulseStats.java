//////////////////////
//Initially created by DarkRevenant and modified from Ship and Weapon Pack
//Chain lightning bit initially created by Nicke535 and modified from Tahlan Shipworks
//AOE explosion bit initially created by Cycerin and modified from Blackrock Driveyards
//////////////////////
package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.magiclib.util.MagicLensFlare;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import scripts.kissa.LOST_SECTOR.util.combatUtil;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class nskr_bfPulseStats extends BaseShipSystemScript {

    //PROJECTILE REMOVAL FX
    public static final Color COLOR1 = new Color(255, 125, 125);
    public static final Color COLOR2 = new Color(219, 0, 66);
    public static final Color COLOR3 = new Color(210, 150, 255);
    public static final Vector2f ZERO = new Vector2f();

    //SUCC
    private static final float SUCCRANGE = 550f;
    //CHAIN LIGHTNING VARIABLES
    private static final float ZAPRANGE = 1000f;
    public static float TARGET_FIND_STEP_LENGTH = 0.05f;
    public static float LIGHTNING_JUMP_RANGE_PERCENTAGE = 0.75f;

    //EMP COLORS
    public static Color LIGHTNING_CORE_COLOR = new Color(152, 31, 204, 255);
    public static Color LIGHTNING_FRINGE_COLOR = new Color(255, 61, 41, 255);
    public static Color LIGHTNING_CORE_COLOR2 = new Color(48, 31, 204, 255);
    public static Color LIGHTNING_FRINGE_COLOR2 = new Color(255, 209, 4, 255);

    //VARIABLES
    private float damageThisShot = 0f;
    private final List<CombatEntityAPI> alreadyDamagedTargets = new ArrayList<>();
    private float empFactor = 0f;
    private float zapRange = 0f;
    private final List<DamagingProjectileAPI> registeredLightningProjectiles = new ArrayList<>();
    boolean explosions = false;
    boolean dmgarc = false;
    boolean activated = false;

    //DMG
    public static final float DAMAGE_MOD_VS_FIGHTER = 1.0f;

    //Explosion effect constants
    public static final Color LENS_FLARE_OUTER_COLOR = new Color(255, 61, 41, 255);
    public static final Color LENS_FLARE_CORE_COLOR = new Color(255, 122, 122, 250);
    public static final float EXPLOSION_EMP_DAMAGE_AMOUNT = 800f;
    public static final float EXPLOSION_DAMAGE_AMOUNT = 800f;
    public static final float EXPLOSION_PUSH_RADIUS = 400f;
    public static final float FORCE_VS_ASTEROID = 50f;
    public static final float FORCE_VS_FIGHTER = 60f;

    private boolean updated = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();
        //make sure variables are correct
        if (!updated) {
            activated = false;
            explosions = false;
            dmgarc = false;

            updated = true;
        }

        if (state == State.OUT) {
            if (!activated) {
                float range = getMaxRangeSucc(ship);

                //FX
                StandardLight light = new StandardLight(ship.getLocation(), ZERO, ZERO, null);
                light.setIntensity(3f);
                light.setSize(600f);
                light.setColor(COLOR1);
                light.fadeOut(0.25f);
                LightShader.addLight(light);

                Global.getCombatEngine().spawnExplosion(ship.getLocation(), ZERO, COLOR1, range, 0.25f);

                Global.getSoundPlayer().playSound("nskr_bf_blast", 1f, 1.0f, ship.getLocation(), ZERO);

                //lens flare fx
                MagicLensFlare.createSharpFlare(
                        engine,
                        ship,
                        ship.getLocation(),
                        20f,
                        200f,
                        ship.getFacing(),
                        LENS_FLARE_OUTER_COLOR,
                        LENS_FLARE_CORE_COLOR
                );



                //ACTUAL STUFF
                List<CombatEntityAPI> targets = new ArrayList<>(Global.getCombatEngine().getProjectiles().size() / 4);
                targets.addAll(combatUtil.getProjectilesWithinRange(ship.getLocation(), range));
                targets.addAll(combatUtil.getMissilesWithinRange(ship.getLocation(), range));
                targets.addAll(combatUtil.getAsteroidsWithinRange(ship.getLocation(), range));
                //get projectiles and blow them up
                float total = 0f;
                for (CombatEntityAPI target : targets) {
                    float level;
                    if (target instanceof DamagingProjectileAPI) {
                        DamagingProjectileAPI proj = (DamagingProjectileAPI) target;
                        if (proj.getBaseDamageAmount() <= 0) continue;

                        if (proj.getDamageType() == DamageType.FRAGMENTATION) {
                            level = proj.getDamageAmount() * 0.25f + proj.getEmpAmount() * 0.25f;
                        } else {
                            level = proj.getDamageAmount() + proj.getEmpAmount() * 0.25f;
                        }
                    } else {
                        level = target.getMass();
                    }
                    float sqrtLevel = (float) Math.sqrt(level);
                    float sqrtLevel2 = (float) Math.sqrt(sqrtLevel);

                    //FX
                    RippleDistortion ripple = new RippleDistortion(target.getLocation(), ZERO);
                    ripple.setSize(sqrtLevel * 15f);
                    ripple.setIntensity(sqrtLevel * 1.5f);
                    ripple.setFrameRate(240f / sqrtLevel2);
                    ripple.fadeInSize(sqrtLevel2 / 4f);
                    ripple.fadeOutIntensity(sqrtLevel2 / 4f);
                    DistortionShader.addDistortion(ripple);

                    Global.getCombatEngine().spawnEmpArc(ship, new Vector2f(target.getLocation()), new SimpleEntity(target.getLocation()), ship,
                            DamageType.ENERGY, 0f, 0f, 500f, null, sqrtLevel / 10f, COLOR2, COLOR3);

                    Global.getCombatEngine().removeEntity(target);

                    //total dmg we absorbed (important)
                    total += level;

                    //cap volume so we don't blast our ears
                    float basevolume = (0f);
                    float realvolume = (0f);
                    basevolume = 1.0f + (float) Math.sqrt(total) / 30f;
                    if (basevolume < 4) {
                        realvolume = basevolume;
                    } else {
                        realvolume = (4f);
                    }
                    Global.getSoundPlayer().playSound("nskr_bf_blast", 1f, realvolume, ship.getLocation(), ZERO);
                }

                activated = true;

                //spawn chain lighting
                if (!dmgarc) {

                    //deal dmg according to how much projectiles we killed
                    float dam = (total * 0.50f);
                    float em = (5.0f);
                    damageThisShot = dam;
                    empFactor = em;
                    alreadyDamagedTargets.clear();
                    zapRange = getMaxRangeZap(ship);

                    //First, we find the closest target in a line
                    CombatEntityAPI firstTarget = null;
                    float iter = 0f;
                    while (firstTarget == null && iter < 1f) {
                        //Gets a point a certain distance away from the weapon
                        Vector2f pointToLookAt = Vector2f.add(ship.getLocation(), new Vector2f(
                                (float) FastTrig.cos(Math.toRadians(ship.getFacing())) * iter * zapRange,
                                (float) FastTrig.sin(Math.toRadians(ship.getFacing())) * iter * zapRange),
                                new Vector2f(0f, 0f));


                        List<CombatEntityAPI> targetList = CombatUtils.getEntitiesWithinRange(pointToLookAt, zapRange * TARGET_FIND_STEP_LENGTH * (1f + iter));
                        for (CombatEntityAPI potentialTarget : targetList) {
                            //Checks for dissallowed targets, and ignores them
                            if (!(potentialTarget instanceof ShipAPI) && !(potentialTarget instanceof MissileAPI) || potentialTarget.getOwner() == ship.getOwner()) continue;
                            if (MathUtils.getDistance(potentialTarget.getLocation(), ship.getLocation()) - (potentialTarget.getCollisionRadius() * 0.9f) > zapRange) continue;

                            //Phased targets, and targets with no collision, are ignored
                            if (potentialTarget instanceof ShipAPI) {
                                if (((ShipAPI) potentialTarget).isPhased()) continue;
                            }
                            if (potentialTarget.getCollisionClass().equals(CollisionClass.NONE)) continue;

                            //If we found any applicable targets, pick the closest one
                            if (firstTarget == null) {
                                firstTarget = potentialTarget;
                            } else if (MathUtils.getDistance(firstTarget, ship.getLocation()) > MathUtils.getDistance(potentialTarget, ship.getLocation())) {
                                firstTarget = potentialTarget;
                            }
                        }

                        iter += TARGET_FIND_STEP_LENGTH;
                    }

                    //If we didn't find a target on the line, the shot was a dud: spawn a decorative EMP arc to the end destination
                    if (firstTarget == null) {
                        Vector2f targetPoint = Vector2f.add(ship.getLocation(), new Vector2f((float)FastTrig.cos(Math.toRadians(ship.getFacing())) * zapRange, (float)FastTrig.sin(Math.toRadians(ship.getFacing())) * zapRange), new Vector2f(0f, 0f));
                        Global.getCombatEngine().spawnEmpArc(ship, ship.getLocation(), ship, new SimpleEntity(targetPoint),
                                DamageType.HIGH_EXPLOSIVE, //Damage type
                                1f, //Damage
                                1f, //Emp
                                100000f, //Max range
                                "nskr_emp_impact", //Impact sound
                                5f + (total/10f), // thickness of the lightning bolt
                                LIGHTNING_CORE_COLOR, //Central color
                                LIGHTNING_FRINGE_COLOR //Fringe Color
                        );
                        Global.getCombatEngine().spawnEmpArc(ship, ship.getLocation(), ship, new SimpleEntity(targetPoint),
                                DamageType.HIGH_EXPLOSIVE, //Damage type
                                1f, //Damage
                                1f, //Emp
                                100000f, //Max range
                                "nskr_emp_impact", //Impact sound
                                5f + (total/10f), // thickness of the lightning bolt
                                LIGHTNING_CORE_COLOR2, //Central color
                                LIGHTNING_FRINGE_COLOR2 //Fringe Color
                        );
                        return;
                    }

                    //Initializes values for our loop's first iteration
                    CombatEntityAPI currentTarget = firstTarget;
                    CombatEntityAPI previousTarget = ship;
                    Vector2f firingPoint = ship.getLocation();

                    //Run a repeating loop to find new targets and deal damage to them in a chain
                    while (damageThisShot > 1f) {
                        CombatEntityAPI nextTarget = null;

                        damageThisShot *= 0.5f;

                        //Stores how much damage we have left after this shot
                        float tempStorage = Math.max(damageThisShot - currentTarget.getHitpoints(), 0) + damageThisShot;

                        //Finds a new target, in case we are going to overkill our current one
                        List<CombatEntityAPI> targetList = CombatUtils.getEntitiesWithinRange(currentTarget.getLocation(), zapRange * LIGHTNING_JUMP_RANGE_PERCENTAGE);
                        for (CombatEntityAPI potentialTarget : targetList) {
                            //Checks for dissallowed targets, and ignores them
                            if (!(potentialTarget instanceof ShipAPI) && !(potentialTarget instanceof MissileAPI) || potentialTarget.getOwner() == ship.getOwner()) {
                                continue;
                            }
                            if (alreadyDamagedTargets.contains(potentialTarget)) {
                                continue;
                            }
                            //Phased targets, and targets with no collision, are ignored
                            if (potentialTarget instanceof ShipAPI) {
                                if (((ShipAPI)potentialTarget).isPhased()) continue;
                            }
                            if (potentialTarget.getCollisionClass().equals(CollisionClass.NONE)) continue;
                            //modules
                            if (potentialTarget instanceof ShipAPI) {
                                ShipAPI potentialShip = (ShipAPI)potentialTarget;
                                if (potentialShip.isStationModule() || potentialShip.isStation()){
                                    continue;
                                }
                            }

                            //If we found any applicable targets, pick the closest one
                            if (nextTarget == null) {
                                nextTarget = potentialTarget;
                            } else if (MathUtils.getDistance(nextTarget, currentTarget) > MathUtils.getDistance(potentialTarget, currentTarget)) {
                                nextTarget = potentialTarget;
                            }
                        }

                        //If we didn't find any targets, the lightning stops here
                        if (nextTarget == null) {
                            tempStorage = 0f;
                        }

                        //Sets our previous target to our current one (before damaging it, that is)
                        CombatEntityAPI tempPreviousTarget = previousTarget;
                        previousTarget = currentTarget;

                        //If our target is a missile, *and* our EMP is not higher than 1/3rd of the missile's HP, we don't count as an EMP arc hit; increase the EMP resistance by one before firing the arc
                        if (currentTarget instanceof MissileAPI) {
                            if (damageThisShot * empFactor < currentTarget.getHitpoints() / 3f) {
                                ((MissileAPI) currentTarget).setEmpResistance(((MissileAPI) currentTarget).getEmpResistance() + 1);
                            }
                        }
                        //Actually spawn the lightning arc
                        Global.getCombatEngine().spawnEmpArc(ship, firingPoint, tempPreviousTarget, currentTarget,
                                DamageType.HIGH_EXPLOSIVE, //Damage type
                                damageThisShot * 0.75f, //Damage
                                damageThisShot * empFactor, //Emp
                                100000f, //Max range
                                "nskr_emp_impact", //Impact sound
                                5f + (damageThisShot/10f), // thickness of the lightning bolt
                                LIGHTNING_CORE_COLOR, //Central color
                                LIGHTNING_FRINGE_COLOR //Fringe Color
                        );
                        //A second decorative arc
                        Global.getCombatEngine().spawnEmpArc(ship, firingPoint, tempPreviousTarget, currentTarget,
                                DamageType.HIGH_EXPLOSIVE, //Damage type
                                damageThisShot * 0.25f, //Damage
                                damageThisShot * empFactor * 0.25f, //Emp
                                100000f, //Max range
                                "nskr_emp_impact", //Impact sound
                                5f + (damageThisShot/10f), // thickness of the lightning bolt
                                LIGHTNING_CORE_COLOR2, //Central color
                                LIGHTNING_FRINGE_COLOR2 //Fringe Color
                        );
                        //Adjusts variables for the next iteration
                        firingPoint = previousTarget.getLocation();
                        damageThisShot = tempStorage;
                        alreadyDamagedTargets.add(nextTarget);
                        currentTarget = nextTarget;
                    }
                 dmgarc = true;
                }
                //extra explosion to kill f*ghters
                if (!explosions) {

                    Vector2f loc = new Vector2f(ship.getLocation());
                    loc.x -= 8f * FastTrig.cos(ship.getFacing() * Math.PI / 180f);
                    loc.y -= 8f * FastTrig.sin(ship.getFacing() * Math.PI / 180f);

                    ShipAPI victim;
                    Vector2f dir;
                    float force, damage, emp, mod;
                    List<CombatEntityAPI> entities = CombatUtils.getEntitiesWithinRange(ship.getLocation(),
                            EXPLOSION_PUSH_RADIUS);
                    int size = entities.size();
                    int i = 0;
                    while (i < size) {
                        CombatEntityAPI tmp = entities.get(i);

                        mod = 1f - (MathUtils.getDistance(ship, tmp) / EXPLOSION_PUSH_RADIUS);
                        force = FORCE_VS_ASTEROID * mod;
                        damage = EXPLOSION_DAMAGE_AMOUNT * mod;
                        emp = EXPLOSION_EMP_DAMAGE_AMOUNT * mod;

                        if (tmp instanceof ShipAPI) {
                            victim = (ShipAPI) tmp;
                            // Modify push strength and dmg based on ship class
                            if (victim.getHullSize() == ShipAPI.HullSize.FIGHTER && victim.getOwner() != ship.getOwner()) {
                                force = FORCE_VS_FIGHTER * mod;
                                damage /= DAMAGE_MOD_VS_FIGHTER;
                                engine.applyDamage(victim, victim.getLocation(), damage, DamageType.HIGH_EXPLOSIVE, emp, false, false, ship, true);
                            }
                        }

                        dir = VectorUtils.getDirectionalVector(ship.getLocation(), tmp.getLocation());
                        dir.scale(force);
                        Vector2f.add(tmp.getVelocity(), dir, tmp.getVelocity());
                        i++;


                    }
                    explosions = true;
                }

            }

        }
    }

    public static float getMaxRangeZap(ShipAPI ship){
        if (ship==null) return 0f;
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(ZAPRANGE);
    }

    public static float getMaxRangeSucc(ShipAPI ship){
        if (ship==null) return 0f;
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(SUCCRANGE);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

        updated = false;
    }

}
