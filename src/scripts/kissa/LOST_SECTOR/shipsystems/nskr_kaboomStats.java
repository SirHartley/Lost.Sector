//////////////////////
//Bits initially created by Cycerin and modified from Blackrock Driveyards
//////////////////////
package scripts.kissa.LOST_SECTOR.shipsystems;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.util.MagicLensFlare;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.hullmods.nskr_aed;
import scripts.kissa.LOST_SECTOR.util.combatUtil;
import scripts.kissa.LOST_SECTOR.util.util;

public class nskr_kaboomStats extends BaseShipSystemScript {

    //DMG (for some reason smaller number = more DMG -thanks Cycerin)
    public static final float DAMAGE_MOD_VS_CAPITAL = 0.50f;
    public static final float DAMAGE_MOD_VS_CRUISER = 0.75f;
    public static final float DAMAGE_MOD_VS_DESTROYER = 1.5f;
    public static final float DAMAGE_MOD_VS_FIGHTER = 0.5f;
    public static final float DAMAGE_MOD_VS_FRIGATE = 2.0f;

    // Explosion effect constants
    public static final Color EXPLOSION_COLOR = new Color(142, 29, 255);
    public static final Color EMP_COLOR = new Color(31, 68, 204);
    public static final Color LENS_FLARE_OUTER_COLOR = new Color(37, 25, 150, 255);
    public static final Color LENS_FLARE_CORE_COLOR = new Color(193, 122, 255, 250);

    public static final float EXPLOSION_EMP_DAMAGE_AMOUNT = 5000f;
    public static final float EXPLOSION_DAMAGE_AMOUNT = 2500f;
    public static final DamageType EXPLOSION_DAMAGE_TYPE = DamageType.HIGH_EXPLOSIVE;
    public static final float DISTORTION_BLAST_RADIUS = 1200f;
    public static final float EXPLOSION_PUSH_RADIUS = 1200f;
    public static final float EXPLOSION_VISUAL_RADIUS = 2400f;

    public static final float FORCE_VS_ASTEROID = 500f;
    public static final float FORCE_VS_MISSILE = 500f;
    public static final float FORCE_VS_CAPITAL = 100f;
    public static final float FORCE_VS_CRUISER = 150f;
    public static final float FORCE_VS_DESTROYER = 400f;
    public static final float FORCE_VS_FIGHTER = 600f;
    public static final float FORCE_VS_FRIGATE = 600f;

    public static final float EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER = .10f;
    public static final float EXPLOSION_EMP_VS_ALLIES_MODIFIER = .20f;
    public static final float EXPLOSION_FORCE_VS_ALLIES_MODIFIER = .5f;
    //sound
    public static final String SOUND_ID_IN = "nskr_kaboom_tick";
    public static final String SOUND_ID_EXPLO = "nskr_kaboom";
    public static final int NUM_SPLINTERS = 90;
    public static final String SPLINTER_WEAPON_ID = "nskr_kaboom_dummy";

    public static final Vector2f ZERO = new Vector2f();
    private boolean explosions = false;
    private boolean text = false;
    private boolean in = false;
    private boolean frameSkip = false;
    // Local variables, don't touch these
    private StandardLight light;
    private WaveDistortion wave;
    private final IntervalUtil sparkleInterval = new IntervalUtil(0.50f, 1.00f);
    private final IntervalUtil textInterval = new IntervalUtil(2.50f, 2.50f);
    private Vector2f point2;
    private Vector2f point1;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        CombatEngineAPI engine = Global.getCombatEngine();
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if (Global.getCombatEngine().isPaused() || !ship.isAlive()){
            return;
        }

        nskr_aed.ShipSpecificData data = (nskr_aed.ShipSpecificData) Global.getCombatEngine().getCustomData().get("KABOOM_DATA_KEY" + ship.getId());

        //init shipspecific data
        data.kaboom = false;
        data.kLoc = ZERO;
        data.timer = 0f;
        data.doOnce = true;

        if (state == State.IN) {
            // SPEED
            ship.getMutableStats().getMaxSpeed().modifyMult(id, 1.00f);

            if (!in) {
                Global.getSoundPlayer().playSound(SOUND_ID_IN, 1.0f, 0.8f, ship.getLocation(), ship.getVelocity());

            }

            textInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if (!text && textInterval.intervalElapsed()) {

                Vector2f loc = new Vector2f(ship.getLocation());
                Color color3 = new Color(255, 31, 31, 255);
                engine.addFloatingText(loc, "Have A Nice Day :)", 32.0f, color3, ship, 6.00f, 1.00f);
                text = true;
            }

            explosions = false;
            in = true;

            Color color4 = new Color(255, 31, 65, 255);
            ship.setJitterShields(false);
            ship.setJitterUnder(ship, color4, 1.0f*effectLevel, Math.round(20*effectLevel), 1f, 5f*effectLevel);
            ship.setJitter(ship, color4, 0.2f*effectLevel, Math.round(5*effectLevel), 1f, 1.5f*effectLevel);

            Vector2f sloc = ship.getLocation();
            sparkleInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if (sparkleInterval.intervalElapsed()) {
                for (int x2 = 0; x2 < 3; x2++) {
                    float angle = (float) Math.random() * 360f;
                    float distance = (float) Math.random() * 150f + 30f;
                    float angle2 = (float) Math.random() * 360f;
                    float distance2 = (float) Math.random() * 30f + 30f;
                    Vector2f point1 = MathUtils.getPointOnCircumference(sloc, distance, angle);
                    Vector2f point2 = MathUtils.getPointOnCircumference(sloc, distance2, angle2);
                    if (ship != null) {
                        Color color1 = util.randomiseColor(new Color(150,100,200,255),100,50,50,0,true);
                        Color color6 = util.randomiseColor(new Color(150,100,200,155),100,50,50,0,true);
                        Global.getCombatEngine().spawnEmpArcVisual(point1, ship, point2, ship,
                                MathUtils.getRandomNumberInRange(5f, 20f), // thickness of the lightning bolt
                                color1, //Central color
                                color6 //Fringe Color
                        );
                        MagicLensFlare.createSharpFlare(
                                engine,
                                ship,
                                point1,
                                MathUtils.getRandomNumberInRange(5f, 20f),
                                MathUtils.getRandomNumberInRange(25f, 75f),
                                VectorUtils.getAngle(ship.getLocation(), point1),
                                color1,
                                color6
                        );
                        engine.addSmoothParticle(
                                point1,
                                ship.getVelocity(),
                                MathUtils.getRandomNumberInRange(10, 80),
                                0.25f,
                                2,
                                color1
                        );
                        engine.addHitParticle(
                                point1,
                                ship.getVelocity(),
                                MathUtils.getRandomNumberInRange(5, 40),
                                2f,
                                0.4f,
                                color1
                        );
                    }
                }
            }
        }

        //EXPLODE
        if (state == State.ACTIVE) {
            //need to delay explosion by 1 frame to allow protExplosion to remove itself
            //kinda a hack
            data.remove = true;

            //kaboom
            if (!explosions && frameSkip) {
                //init shipspecific data
                data.kaboom = true;
                data.kLoc = ship.getLocation();
                data.doOnce = false;

                //AOE DAMAGE
                combatUtil.applyAOEDamage(ship, null, ship.getLocation(), EXPLOSION_DAMAGE_AMOUNT, DamageType.HIGH_EXPLOSIVE, EXPLOSION_PUSH_RADIUS, true);

                point2 = new Vector2f(ship.getLocation());
                float angle = (float) Math.random() * 360f;
                float distance = (float) Math.random() * 700f + 100f;
                point1 = MathUtils.getPointOnCircumference(ship.getLocation(), distance, angle);
                //emp
                for (int x5 = 0; x5 < 10; x5++) {
                    engine.spawnEmpArcVisual(point2, new SimpleEntity(point2), point1, new SimpleEntity(point1), 25f, EXPLOSION_COLOR, LENS_FLARE_OUTER_COLOR);
                }

                // This spawns the frag, also distributing them in a nice even 360 degree arc
                for (int i = 0; i < NUM_SPLINTERS; i++) {
                    float sangle2 = (float) Math.random() * 360f;
                    float sdistance = (float) Math.random() * 25f + 50f;
                    Vector2f sPoint = MathUtils.getPointOnCircumference(ship.getLocation(), sdistance, sangle2);
                    float sangle = ship.getFacing() + i * 360f / NUM_SPLINTERS + (float) Math.random() * 180f / NUM_SPLINTERS;
                    Vector2f location = MathUtils.getPointOnCircumference(sPoint, 75f, sangle);
                    WeaponAPI wep;
                    wep = engine.createFakeWeapon(ship, SPLINTER_WEAPON_ID);

                    DamagingProjectileAPI newProj = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(ship, wep, SPLINTER_WEAPON_ID, location, sangle, new Vector2f(0, 0));
                    Vector2f newVel = new Vector2f(newProj.getVelocity());
                    newVel.scale((float) Math.random());
                    newProj.getVelocity().set(newVel.x, newVel.y);
                }

                engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS,
                        0.31f);
                engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS /
                        2f, 0.29f);

                Vector2f loc = new Vector2f(ship.getLocation());
                loc.x -= 8f * FastTrig.cos(ship.getFacing() * Math.PI / 180f);
                loc.y -= 8f * FastTrig.sin(ship.getFacing() * Math.PI / 180f);

                //lens flare fx
                MagicLensFlare.createSharpFlare(
                        engine,
                        ship,
                        loc,
                        50f,
                        750f,
                        ship.getFacing(),
                        LENS_FLARE_OUTER_COLOR,
                        LENS_FLARE_CORE_COLOR
                );

                //light fx
                light = new StandardLight();
                light.setLocation(loc);
                light.setIntensity(2.0f);
                light.setSize(EXPLOSION_VISUAL_RADIUS * 3f);
                light.setColor(EXPLOSION_COLOR);
                light.fadeOut(3f);
                LightShader.addLight(light);

                //distortion fx
                wave = new WaveDistortion();
                wave.setLocation(loc);
                wave.setSize(DISTORTION_BLAST_RADIUS);
                wave.setIntensity(DISTORTION_BLAST_RADIUS * 0.10f);
                wave.fadeInSize(1.2f);
                wave.fadeOutIntensity(0.9f);
                wave.setSize(DISTORTION_BLAST_RADIUS * 0.35f);
                DistortionShader.addDistortion(wave);

                Global.getSoundPlayer().playSound(SOUND_ID_EXPLO, 0.6f, 2.5f, ship.getLocation(), ship.getVelocity());

                //negative explosion
                NegativeExplosionVisual.NEParams p = RiftCascadeMineExplosion.createStandardRiftParams("riftcascade_minelayer", 200f);
                p.radius *= 0.75f + 0.5f * (float) Math.random();
                Vector2f neLoc = new Vector2f(ship.getLocation());
                neLoc = Misc.getPointAtRadius(neLoc, p.radius * 0.1f);
                CombatEntityAPI e = engine.addLayeredRenderingPlugin(new NegativeExplosionVisual(p));
                e.getLocation().set(neLoc);


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

                    //fuck missiles
                    if (tmp instanceof MissileAPI) {
                        force = FORCE_VS_MISSILE * mod;
                        engine.applyDamage(tmp, loc, 1000, DamageType.FRAGMENTATION, 0, false, false, ship);
                    }

                    if (tmp instanceof ShipAPI) {
                        victim = (ShipAPI) tmp;

                        // Modify push strength and dmg based on ship class
                        if (victim.getHullSize() == ShipAPI.HullSize.FIGHTER) {
                            force = FORCE_VS_FIGHTER * mod;
                            damage /= DAMAGE_MOD_VS_FIGHTER * mod;
                        } else if (victim.getHullSize() == ShipAPI.HullSize.FRIGATE) {
                            force = FORCE_VS_FRIGATE * mod;
                            damage /= DAMAGE_MOD_VS_FRIGATE * mod;
                        } else if (victim.getHullSize() == ShipAPI.HullSize.DESTROYER) {
                            force = FORCE_VS_DESTROYER * mod;
                            damage /= DAMAGE_MOD_VS_DESTROYER * mod;
                        } else if (victim.getHullSize() == ShipAPI.HullSize.CRUISER) {
                            force = FORCE_VS_CRUISER * mod;
                            damage /= DAMAGE_MOD_VS_CRUISER * mod;
                        } else if (victim.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) {
                            force = FORCE_VS_CAPITAL * mod;
                            damage /= DAMAGE_MOD_VS_CAPITAL * mod;
                        }

                        if (victim.getOwner() == ship.getOwner()) {
                            damage *= EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER;
                            emp *= EXPLOSION_EMP_VS_ALLIES_MODIFIER;
                            force *= EXPLOSION_FORCE_VS_ALLIES_MODIFIER;
                        }

                        if (victim.isPhased()) {
                            damage *= EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER;
                            emp *= EXPLOSION_EMP_VS_ALLIES_MODIFIER;
                            force *= EXPLOSION_FORCE_VS_ALLIES_MODIFIER;
                        }

                        //spawn emp arcs to unshielded targets
                        if ((victim.getShield() != null && victim.getShield().isOn() && victim.getShield().isWithinArc(
                                ship.getLocation()))) {
                            victim.getFluxTracker().increaseFlux(damage * 2, true);
                            EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(ship, MathUtils.getRandomPointInCircle(victim.getLocation(), victim.getCollisionRadius()),
                                    victim,
                                    victim, EXPLOSION_DAMAGE_TYPE, damage / 30, emp / 10,
                                    EXPLOSION_PUSH_RADIUS, null, 10f, EMP_COLOR,
                                    EMP_COLOR);

                            engine.spawnEmpArcVisual(ship.getLocation(), ship, victim.getLocation(), victim, 15f, EMP_COLOR, EMP_COLOR);

                        } else {
                            ShipAPI empTarget = victim;
                            for (int x = 0; x < 5; x++) {
                                EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(ship, combatUtil.getRandomPointOnShip(victim),
                                        empTarget,
                                        empTarget, EXPLOSION_DAMAGE_TYPE, damage / 10, emp / 5,
                                        EXPLOSION_PUSH_RADIUS, null, 10f, EMP_COLOR,
                                        EMP_COLOR);

                                engine.spawnEmpArcVisual(ship.getLocation(), ship, victim.getLocation(), victim, 15f, EMP_COLOR, EMP_COLOR);
                            }
                        }
                    }

                    dir = VectorUtils.getDirectionalVector(ship.getLocation(), tmp.getLocation());
                    dir.scale(force);

                    Vector2f.add(tmp.getVelocity(), dir, tmp.getVelocity());
                    i++;
                }
                engine.applyDamage(ship, ship.getLocation(), 99999f, DamageType.HIGH_EXPLOSIVE, 9999f, true, false, ship);

                explosions = true;
                text = false;
                in = false;
            }
            frameSkip = true;
        }
    Global.getCombatEngine().getCustomData().put("KABOOM_DATA_KEY" + ship.getId(), data);
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }
}























