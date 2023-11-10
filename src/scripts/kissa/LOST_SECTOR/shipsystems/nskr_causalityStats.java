//////////////////////
//Initially created by DarkRevenant and modified from Ship and Weapon Pack
//Afterimage stuff by TheDragn
//////////////////////
package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.ProjectileSpawnType;
import org.magiclib.util.MagicLensFlare;
import org.lazywizard.lazylib.MathUtils;
import scripts.kissa.LOST_SECTOR.hullmods.nskr_causality;
import scripts.kissa.LOST_SECTOR.plugins.nskr_teleporterPlugin;
import scripts.kissa.LOST_SECTOR.util.combatUtil;

import java.awt.Color;
import java.util.*;

import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class nskr_causalityStats extends BaseShipSystemScript {

    //PROJECTILE REMOVAL FX
    public static final Color COLOR1 = new Color(255, 125, 223);
    public static final Color COLOR2 = new Color(153, 0, 219);
    public static final Color COLOR3 = new Color(160, 150, 255);
    public static final Color TEXT_COLOR = new Color(255, 150, 233, 255);

    //succ
    private static final float SUCCRANGE = 600f;

    //Explosion effect constants
    public static final Color LENS_FLARE_OUTER_COLOR = new Color(123, 41, 255, 255);
    public static final Color LENS_FLARE_CORE_COLOR = new Color(255, 122, 175, 250);

    public final String ENERGY_WEAPON2 = "nskr_causality2_dummy";
    public final String ENERGY_WEAPON1 = "nskr_causality1_dummy";

    //VARIABLES
    public static final Vector2f ZERO = new Vector2f();
    float maxEnergy;
    private Vector2f point2;
    private Vector2f point3;
    private boolean doOnce;
    private boolean soundFx;
    private boolean doRand;
    boolean activated;
    boolean sideEffect;
    boolean teleported;
    private double rand;
    private boolean type1 = false;
    private boolean type2 = false;
    private boolean updated = false;


    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused() || stats.getEntity() == null) return;

        nskr_causality.ShipSpecificData data = (nskr_causality.ShipSpecificData) Global.getCombatEngine().getCustomData().get("CAUSALITY_DATA_KEY" + ship.getId());

        boolean player = false;
        player = ship == Global.getCombatEngine().getPlayerShip();

        //make sure variables are correct
        if (!updated) {
            activated = false;
            sideEffect = false;
            teleported = false;
            doOnce = false;
            doRand = false;
            soundFx = false;

            updated = true;
        }

        if (state == State.ACTIVE || state == State.OUT) {

            if (!activated) {

                float range = getMaxRange(ship);

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
                List<CombatEntityAPI> targets = new ArrayList<>(Global.getCombatEngine().getProjectiles().size());
                targets.addAll(combatUtil.getProjectilesWithinRange(ship.getLocation(), range));
                targets.addAll(combatUtil.getMissilesWithinRange(ship.getLocation(), range));
                targets.addAll(combatUtil.getAsteroidsWithinRange(ship.getLocation(), range));
                //get projectiles and blow them up
                for (CombatEntityAPI target : targets) {
                    float level = 0f;

                    if (!isValid(target,ship)) continue;
                    level = getEnergy(target);

                    float sqrtLevel = (float) Math.sqrt(level);
                    float sqrtLevel2 = (float) Math.sqrt(sqrtLevel);

                    //FX
                    float mod = 3f;
                    RippleDistortion ripple = new RippleDistortion(target.getLocation(), ZERO);
                    ripple.setSize((sqrtLevel * 15f)/mod);
                    ripple.setIntensity((sqrtLevel * 1.5f)/mod);
                    ripple.setFrameRate((240f / sqrtLevel2)/mod);
                    ripple.fadeInSize((sqrtLevel2 / 4f)/mod);
                    ripple.fadeOutIntensity((sqrtLevel2 / 4f)/mod);
                    DistortionShader.addDistortion(ripple);

                    Global.getCombatEngine().spawnEmpArc(ship, new Vector2f(target.getLocation()), new SimpleEntity(target.getLocation()), ship,
                    DamageType.ENERGY, 0f, 0f, 500f, null, sqrtLevel / 10f, COLOR2, COLOR3);

                    //spawn projs
                    if (target instanceof DamagingProjectileAPI) {
                        DamagingProjectileAPI proj = (DamagingProjectileAPI) target;

                        float angle = ship.getFacing()+(180f*MathUtils.getRandomNumberInRange(-0.05f, 0.05f));
                        Vector2f loca = proj.getLocation();


                        float misDir = proj.getFacing();
                        List<ShipAPI> possibleTargets = new ArrayList<>(200);
                        possibleTargets.addAll(combatUtil.getShipsWithinRange(proj.getLocation(), 2500f));
                        float closest = 9999f;
                        //distance
                        for (ShipAPI pt : possibleTargets){
                            if (pt.isHulk() || pt.isPhased() || pt.isFighter() || pt.getOwner() == ship.getOwner()) continue;
                            closest = Math.min(closest, MathUtils.getDistance(proj.getLocation(),pt.getLocation()));
                        }
                        //set angle of spawned missile blobs towards the nearest hostile ship
                        for (ShipAPI pt : possibleTargets) {
                            if (pt.isHulk() || pt.isPhased() || pt.isFighter() || pt.getOwner() == ship.getOwner()) continue;
                            if (MathUtils.getDistance(proj.getLocation(), pt.getLocation()) == closest){
                                misDir = (float)Math.toDegrees(Math.atan2(pt.getLocation().getY()-proj.getLocation().getY(), pt.getLocation().getX()-proj.getLocation().getX()));
                                //engine.addFloatingText(proj.getLocation(), "" + misDir, 36f, TEXT_COLOR, proj, 0, 0);
                            }
                        }

                        WeaponAPI wep1 = engine.createFakeWeapon(ship, ENERGY_WEAPON1);
                        WeaponAPI wep2 = engine.createFakeWeapon(ship, ENERGY_WEAPON2);
                        //blob
                        type1 = proj.getDamageAmount() > 200 && proj instanceof MissileAPI;
                        //bolt
                        type2 = proj.getDamageAmount() >= 50 && !(proj instanceof MissileAPI) || proj.getDamageAmount() <= 200 && proj instanceof MissileAPI && proj.getDamageAmount() >= 50;

                            //uno reverse
                        if (type1) {
                            DamagingProjectileAPI p1;
                            p1 = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(ship, wep1, ENERGY_WEAPON1, loca, misDir, ZERO);
                            p1.setDamageAmount(proj.getDamageAmount());
                            type1 = false;
                        }
                        else if (type2){
                            DamagingProjectileAPI p2;
                            p2 = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(ship, wep2, ENERGY_WEAPON2, loca, angle, ZERO);
                            p2.setDamageAmount(proj.getDamageAmount()*1.5f);
                            type2 = false;
                        }
                    }

                    //bye bye
                    Global.getCombatEngine().removeEntity(target);

                    //total dmg we absorbed (important)
                    data.energy += level;

                    //cap volume so we don't blast our ears
                    float baseVolume = (0f);
                    float realVolume = (0f);
                    baseVolume = 1.0f + (float) Math.sqrt(data.energy) / 30f;
                    if (baseVolume < 4) {
                        realVolume = baseVolume;
                    } else {
                        realVolume = (4f);
                    }
                    Global.getSoundPlayer().playSound("nskr_bf_blast", 1f, realVolume, ship.getLocation(), ZERO);
                }
                //text
                if (player) {
                    engine.addFloatingText(ship.getLocation(), ""+(int)data.energy, 36f, TEXT_COLOR, ship, 0, 0);
                }
            activated = true;
            }

            maxEnergy = ship.getMaxFlux();

            if (player&&!soundFx&&data.energy>(maxEnergy/4)&&data.energy<maxEnergy) {
                Global.getSoundPlayer().playSound("nskr_time_stop", 0.7f, 1.75f, ship.getLocation(), ZERO);
                soundFx = true;
            }

            //SIDE EFFECTS
            if (data.energy>maxEnergy) {
                if (!doRand) {
                    rand = Math.random();
                    doRand = true;
                }

                if (player&&!soundFx) {
                    Global.getSoundPlayer().playSound("nskr_time_stop", 0.7f, 1.00f, ship.getLocation(), ZERO);
                    soundFx = true;
                }

                if (!sideEffect){
                    //flux you
                    ship.getFluxTracker().increaseFlux((data.energy/2f)*(float)Math.random(), true);
                    ship.getFluxTracker().forceOverload(4f);

                    if (player) {
                        engine.addFloatingText(ship.getLocation(), "DANGER", 30f, Color.RED, ship, 0.1f, 0.5f);
                        Global.getSoundPlayer().playSound("nskr_eternity_warning", 0.7f, 3.0f, ship.getLocation(), ZERO);
                    }
                }

                //TELEPORT
                if (!sideEffect && rand>0.80f){

                    if (!doOnce) {
                        Vector2f sLoc2 = ship.getLocation();
                        float angle2 = (float) Math.random() * 360f;
                        float distance2 = (float) Math.random() * 800f + 200f;
                        point2 = MathUtils.getPointOnCircumference(sLoc2, distance2, angle2);
                        doOnce = true;
                    }
                    if (!teleported){
                        //nskr_teleporterPlugin.addTeleportation(ship, point2);
                        ship.getLocation().set(point2);
                        // engine.addFloatingText(point2, "test ", 20f, Color.cyan, ship, 0.5f, 1.0f);
                        teleported = true;
                    }

                    sideEffect = true;
                }
                //TELEPORT SHORT
                if (!sideEffect && rand>0.60f && rand<0.80f){

                    if (!doOnce) {
                        Vector2f sLoc3 = ship.getLocation();
                        float angle3 = (float) Math.random() * 360f;
                        float distance3 = (float) Math.random() * 400f + 100f;
                        point3 = MathUtils.getPointOnCircumference(sLoc3, distance3, angle3);
                        doOnce = true;
                    }
                    if (!teleported){
                        //nskr_teleporterPlugin.addTeleportation(ship, point3);
                        ship.getLocation().set(point3);
                        // engine.addFloatingText(point2, "test ", 20f, Color.cyan, ship, 0.5f, 1.0f);
                        teleported = true;
                    }
                    sideEffect = true;
                }

                //DAMAGE
                if (!sideEffect && rand<0.20f){

                    Vector2f point1 = combatUtil.getRandomPointOnShip(ship);

                    engine.applyDamage(ship, point1, (ship.getMaxHitpoints()/15f), DamageType.HIGH_EXPLOSIVE, (ship.getMaxHitpoints()/5f), true, false, ship);

                    sideEffect = true;
                }

                //SPEEN
                if (!sideEffect && rand>0.40f&&rand<0.60f){

                    float angle3 = (float) Math.random() * 360f;
                    ship.setFacing(angle3);

                    sideEffect = true;
                }

                //OVERLOAD
                if (!sideEffect && rand>0.20f&&rand<0.40f){

                    ship.getFluxTracker().forceOverload(8f);

                    sideEffect = true;
                }


            }


        }
        if (state == State.OUT) {
        }

        Global.getCombatEngine().getCustomData().put("CAUSALITY_DATA_KEY" + ship.getId(), data);
}

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

        updated = false;
    }
    public StatusData getStatusData(int index, State state, float effectLevel) {

        return null;
    }

    public static boolean isValid(CombatEntityAPI target, ShipAPI ship){
        if (target instanceof DamagingProjectileAPI) {
            DamagingProjectileAPI proj = (DamagingProjectileAPI) target;
            if (proj.getBaseDamageAmount() <= 0) return false;
            if (proj.getOwner() == ship.getOwner() && !(proj instanceof MissileAPI)) return false;
            //mmmm.... null
            if ((proj.getProjectileSpecId() != null) && proj.getProjectileSpecId().startsWith("nskr_causality") && (proj.getOwner() == ship.getOwner()))
                return false;
        }
        return true;
    }
    public static float getEnergy(CombatEntityAPI target){
        float level = 0;
        if (target instanceof DamagingProjectileAPI) {
            DamagingProjectileAPI proj = (DamagingProjectileAPI) target;
            if (proj.getDamageType() == DamageType.FRAGMENTATION && proj.getSpawnType() != ProjectileSpawnType.MISSILE) {
                level = proj.getDamageAmount() * 0.375f + proj.getEmpAmount() * 0.50f;
            } else if (proj.getSpawnType() != ProjectileSpawnType.MISSILE) {
                level = proj.getDamageAmount() * 1.5f + proj.getEmpAmount() * 0.50f;
                //less from missiles
            } else if (proj.getDamageType() == DamageType.FRAGMENTATION) {
                level = proj.getDamageAmount() * 0.25f + proj.getEmpAmount() * 0.25f;
            } else if (proj != null) {
                level = proj.getDamageAmount() * 1.0f + proj.getEmpAmount() * 0.25f;
            }
        } else {
            level = target.getMass();
        }
    return level;
    }

    public static float getMaxRange(ShipAPI ship){
        if (ship==null) return 0f;
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(SUCCRANGE);
    }
}
