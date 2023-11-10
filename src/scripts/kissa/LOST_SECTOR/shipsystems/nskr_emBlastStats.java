package scripts.kissa.LOST_SECTOR.shipsystems;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.combatUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.blastSpriteCreator;
import scripts.kissa.LOST_SECTOR.util.util;

public class nskr_emBlastStats extends BaseShipSystemScript {

    // Explosion effect constants
    public static final float EXPLOSION_DAMAGE_AMOUNT = 500f;
    public static final float EXPLOSION_RADIUS = 500f;
    public static final float MAX_ARCS = 8f;

    public static final Color EXPLOSION_COLOR = new Color(109, 86, 255);
    public static final Color EMP_COLOR = new Color(31, 112, 255);
    public static final Color EMP_CORE_COLOR = new Color(181, 216, 255);
    public static final Color PARTICLE_COLOR = new Color(255, 72, 127, 255);
    public static final Color PARTICLE_COLOR_ALTERNATE = new Color(255, 47, 93, 255);
    public static final Color JITTER_COLOR = new Color(122, 255, 251, 255);

    private final IntervalUtil sparkleInterval = new IntervalUtil(0.40f, 0.50f);
    public static final Color SHOCKWAVE_COLOR = new Color(116, 213, 255, 50);
    //sound
    public static final String SOUND_ID_IN = "nskr_emBlast_charge";
    public static final String SOUND_ID_EXPLO = "nskr_emBlast_explode";

    public static final Vector2f ZERO = new Vector2f();
    private boolean explosions = false;
    private boolean in = false;
    // Local variables, don't touch these
    private StandardLight light;
    private WaveDistortion wave;

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

        if (state == State.IN) {
            // SPEED
            ship.getMutableStats().getMaxSpeed().modifyMult(id, mathUtil.inverse(effectLevel/2.0f));
            // tank
            ship.getMutableStats().getHullDamageTakenMult().modifyMult(id, 0.25f);
            // collision
            ship.setCollisionClass(CollisionClass.SHIP);

            if (!in) {
                Global.getSoundPlayer().playSound(SOUND_ID_IN, 1.0f, 0.70f, ship.getLocation(), ship.getVelocity());
            }
            explosions = false;
            in = true;


            ship.setJitterShields(false);
            ship.setJitterUnder(ship, JITTER_COLOR, 1.0f*effectLevel, Math.round(20*effectLevel), 1f, 5f*effectLevel);
            ship.setJitter(ship, JITTER_COLOR, 0.2f*effectLevel, Math.round(5*effectLevel), 1f, 1.5f*effectLevel);

            //PARTICLES
            //INHALE EFFECT
            Vector2f particlePos, particleVel;
            if (Math.random()<(effectLevel/2f) * (engine.getElapsedInLastFrame()*60f)) {
                particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), 75f);
                particleVel = Vector2f.sub(ship.getLocation(), particlePos, null);
                Global.getCombatEngine().addSmokeParticle(particlePos, particleVel, 4f, 0.7f, 0.5f,
                        PARTICLE_COLOR_ALTERNATE);
            }
            //EM ARCS
            Vector2f sloc = ship.getLocation();
            sparkleInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if (sparkleInterval.intervalElapsed()) {
                for (int x = 0; x < 2; x++) {
                    float angle = (float) Math.random() * 360f;
                    float distance = (float) Math.random() * 150f + 50f;
                    float angle2 = (float) Math.random() * 360f;
                    float distance2 = (float) Math.random() * 15f;
                    Vector2f point1 = MathUtils.getPointOnCircumference(sloc, distance, angle);
                    Vector2f point2 = MathUtils.getPointOnCircumference(sloc, distance2, angle2);

                    Global.getCombatEngine().spawnEmpArcVisual(point1, new SimpleEntity(point1), point2, ship,
                            MathUtils.getRandomNumberInRange(5f, 10f), // thickness of the lightning bolt
                            EMP_CORE_COLOR, //Central color
                            PARTICLE_COLOR //Fringe Color
                    );

                }
            }

            //make the AI scared
            for (ShipAPI potentialTarget : combatUtil.getShipsWithinRange(ship.getLocation(), 450f)){
                if (potentialTarget.getOwner()==ship.getOwner()) continue;
                if (potentialTarget.getHullSize()== ShipAPI.HullSize.FIGHTER) continue;
                if (potentialTarget.getShipAI()==null) continue;
                ShipAIPlugin ai = potentialTarget.getShipAI();
                //flags
                ai.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.HAS_INCOMING_DAMAGE,2.5f);
                ai.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON,2.5f);
                ai.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.STAY_PHASED,2.5f);
                if (potentialTarget.getFluxLevel()>0.60f) ai.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.BACK_OFF, 2.5f);
            }
        }

        //EXPLODE
        if (state == State.ACTIVE) {
            //kaboom
            if (!explosions) {
                //AOE DAMAGE
                combatUtil.applyAOEDamage(ship, null, ship.getLocation(), EXPLOSION_DAMAGE_AMOUNT, DamageType.ENERGY, EXPLOSION_RADIUS, false);
                //EMP ARC THE TARGET
                for (ShipAPI target : CombatUtils.getShipsWithinRange(ship.getLocation(), EXPLOSION_RADIUS)){
                    if (target.getOwner()==ship.getOwner()) continue;
                    if (!target.isAlive()) continue;
                    float dist = Math.min(MathUtils.getDistance(ship.getLocation(), target.getLocation()), EXPLOSION_RADIUS);
                    dist = Math.max(150f, dist);
                    float mult = mathUtil.inverse(mathUtil.normalize(dist,0f, EXPLOSION_RADIUS));

                    float fluxLevel = 1f;
                    if ((target.getShield() != null && target.getShield().isOn() && target.getShield().isWithinArc(ship.getLocation()))) fluxLevel = target.getHardFluxLevel();

                    int arcs = Math.round(MAX_ARCS*mult*fluxLevel);
                    for (int x = 0; x < arcs; x++) {
                        engine.spawnEmpArcPierceShields(ship, ship.getLocation(), target, target,
                                DamageType.ENERGY,
                                (EXPLOSION_DAMAGE_AMOUNT/4f),
                                EXPLOSION_DAMAGE_AMOUNT/1.5f, // emp
                                100000f, // max range
                                "tachyon_lance_emp_impact",
                                20f, // thickness
                                new Color(94, 212, 255,255),
                                new Color(255, 229, 245,255)
                        );
                    }
                }

                engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, 350f, 0.41f);
                engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, 350f / 2f, 0.39f);
                //nebula
                Global.getCombatEngine().addSwirlyNebulaParticle(
                        ship.getLocation(), mathUtil.scaleVector(ship.getVelocity(),0.5f), 400f,
                        1.20f, 0.25f,0.5f,1f,
                        util.setAlpha(EMP_CORE_COLOR, 100),false);

                //shockwave visual
                blastSpriteCreator.blastSpriteListener shockwave = new blastSpriteCreator.blastSpriteListener(ship, ship.getLocation(), 0.33f, 400f, SHOCKWAVE_COLOR);
                shockwave.baseSize = 50f;
                shockwave.startSizeMult = 0f;
                ship.addListener(shockwave);

                //light fx
                light = new StandardLight();
                light.setLocation(ship.getLocation());
                light.setIntensity(2.0f);
                light.setSize(200f);
                light.setColor(EXPLOSION_COLOR);
                light.fadeOut(1f);
                LightShader.addLight(light);

                //distortion fx
                wave = new WaveDistortion();
                wave.setLocation(ship.getLocation());
                wave.setSize(250f);
                wave.setIntensity(1f);
                wave.fadeInSize(0.5f);
                wave.fadeOutIntensity(0.75f);
                wave.setSize(250f);
                DistortionShader.addDistortion(wave);

                //EM ARCS
                for (int x = 0; x < 12; x++) {
                    Vector2f particlePos, particlePosTo;
                    float radius = (float)Math.random()*400f+50f;
                    particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), radius);
                    float angle = VectorUtils.getAngle(ship.getLocation(),particlePos);
                    particlePosTo = MathUtils.getPointOnCircumference(ship.getLocation(), radius, angle+ mathUtil.getRandomNumberInRangeExcludingRange(-80f, 80f,-20f,20f));

                    Global.getCombatEngine().spawnEmpArcVisual(particlePos, new SimpleEntity(particlePos), particlePosTo, new SimpleEntity(particlePosTo),
                            20f, // thickness of the lightning bolt
                            EMP_CORE_COLOR, //Central color
                            EMP_COLOR //Fringe Color
                    );
                }

                Global.getSoundPlayer().playSound(SOUND_ID_EXPLO, 1.0f, 1.0f, ship.getLocation(), ship.getVelocity());


                engine.applyDamage(ship, ship.getLocation(), 99999f, DamageType.HIGH_EXPLOSIVE, 9999f, true, false, ship);
                explosions = true;
                in = false;
            }
        }
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























