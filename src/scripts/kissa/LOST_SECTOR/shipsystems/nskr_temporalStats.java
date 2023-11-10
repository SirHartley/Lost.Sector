//////////////////////
//Initially created by Cycerin and modified from Blackrock Driveyards
//////////////////////
package scripts.kissa.LOST_SECTOR.shipsystems;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.blastSpriteCreator;
import scripts.kissa.LOST_SECTOR.util.combatUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

public class nskr_temporalStats extends BaseShipSystemScript {

    //Phase stuff
    public static final float SHIP_ALPHA_MULT = 0.0f;
    public static final float MAX_TIME_MULT = 5f;

    //DMG (for some reason smaller number = more DMG -thanks Cycerin)
    public static final float DAMAGE_MOD_VS_CAPITAL = 0.5f;
    public static final float DAMAGE_MOD_VS_CRUISER = 1.0f;
    public static final float DAMAGE_MOD_VS_DESTROYER = 1.0f;
    public static final float DAMAGE_MOD_VS_FIGHTER = 0.25f;
    public static final float DAMAGE_MOD_VS_FRIGATE = 1.0f;

    // Explosion effect constants
    public static final Color JITTER_COLOR = new Color(179, 59, 59, 255);
    public static final Color JITTER_UNDER_COLOR = new Color(179, 59, 95, 255);
    public static final Color AFTERIMAGE_COLOR = new Color(179, 59, 59, 50);
    public static final Color EXPLOSION_COLOR = new Color(160, 55, 64);
    public static final Color EMP_COLOR = new Color(112, 31, 204);
    public static final Color SHOCKWAVE_COLOR1 = new Color(245, 12, 94,30);
    public static final Color SHOCKWAVE_COLOR2 = new Color(220, 34, 77,25);
    public static final Color LENS_FLARE_CORE_COLOR = new Color(255, 122, 122, 250);

    public static final float EXPLOSION_EMP_DAMAGE_AMOUNT = 3000f;
    public static final float EXPLOSION_DAMAGE_AMOUNT = 1500f;
    public static final DamageType EXPLOSION_DAMAGE_TYPE = DamageType.HIGH_EXPLOSIVE;
    public static final float DISTORTION_BLAST_RADIUS = 850f;
    public static final float EXPLOSION_PUSH_RADIUS = 900f;
    public static final float EXPLOSION_VISUAL_RADIUS = 850f;
    public static final float FORCE_VS_ASTEROID = 150f;
    public static final float FORCE_VS_MISSILE = 50f;
    public static final float FORCE_VS_CAPITAL = 24f;
    public static final float FORCE_VS_CRUISER = 40f;
    public static final float FORCE_VS_DESTROYER = 100f;
    public static final float FORCE_VS_FIGHTER = 200f;
    public static final float FORCE_VS_FRIGATE = 150f;
    public static final float EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER = .1f;
    public static final float EXPLOSION_EMP_VS_ALLIES_MODIFIER = .2f;
    public static final float EXPLOSION_FORCE_VS_ALLIES_MODIFIER = .5f;
    //sound
    public static final String SOUND_ID = "nskr_temporall_off";
    public static final String SPRITE_PATH_SHOCKWAVE = "graphics/fx/nskr_blast_soft.png";
    public static final String SPRITE_PATH_GLOW = "graphics/fx/nskr_glow1.png";

    // Local variables, don't touch these
    private boolean Explosions = true;
    private StandardLight light;
    private WaveDistortion wave;
    float timer = 0f;
    private boolean updated = false;


    public float getMaxTimeMult(MutableShipStatsAPI stats) {
        return 1f + (MAX_TIME_MULT - 1f) * stats.getDynamic().getValue(Stats.PHASE_TIME_BONUS_MULT);
    }

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        boolean player = false;
        CombatEngineAPI engine = Global.getCombatEngine();
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
            return;
        }
        float levelForAlpha = effectLevel;

        if (!updated) {
            Explosions = true;
            timer = 0f;

            updated = true;
        }

        int oobCount = 0;
        boolean OOB = false;
        List<ShipAPI> shipsInBounds = new ArrayList<>(100);
        shipsInBounds.addAll(combatUtil.getShipsWithinRange(ship.getLocation(), ship.getCollisionRadius()));
        for (ShipAPI sb : shipsInBounds){
            if(!sb.getCollisionClass().equals(CollisionClass.SHIP))continue;
            if(sb==ship)continue;
            oobCount++;
        }
        if (oobCount>0) OOB = true;

        if (state == State.ACTIVE && OOB || timer<2.5f){
            timer += Global.getCombatEngine().getElapsedInLastFrame();
            ship.setPhased(true);
            levelForAlpha = effectLevel;
            ship.setAngularVelocity(0f);

            stats.getMaxSpeed().modifyFlat(id, 200f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 2000f * effectLevel);

            //ACTIVE FX
            if (Math.random() < 1.0f * (engine.getElapsedInLastFrame()*60f)) {
                Vector2f particlePos, particleVel;

                particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), (float) Math.random() * (ship.getCollisionRadius() * 0.75f));
                particleVel = Vector2f.sub(ship.getLocation(), particlePos, null);
                particleVel = mathUtil.scaleVector(particleVel, 0.5f);
                float dispersion = 50f;
                particleVel.x += MathUtils.getRandomNumberInRange(-dispersion, dispersion);
                particleVel.y += MathUtils.getRandomNumberInRange(-dispersion, dispersion);

                engine.addNebulaParticle(particlePos, particleVel,
                        MathUtils.getRandomNumberInRange(90f,110f), 0.8f, 0.5f, 1f, 0.75f, EXPLOSION_COLOR);
                if (Math.random()<0.10f) {
                    engine.addNegativeNebulaParticle(particlePos, particleVel,
                            MathUtils.getRandomNumberInRange(90f, 110f), 0.8f, 0.5f, 1f, 0.75f, EXPLOSION_COLOR);
                }
            }

        } else ship.getSystem().forceState(ShipSystemAPI.SystemState.OUT,0.1f);


        //Global.getCombatEngine().addFloatingText(ship.getLocation(), "OOB " + oobCount + " timer " + timer, 32f, Color.cyan, ship, 0.5f, 1.0f);

        if (state == State.OUT) {
            if (effectLevel > 0.5f) {
                ship.setPhased(true);
            } else {
                ship.setPhased(false);
                //remove tidi
                stats.getTimeMult().unmodify(id);
                Global.getCombatEngine().getTimeMult().unmodify(id);
                stats.getMaxSpeed().unmodify(id);
                stats.getAcceleration().unmodify(id);
            }
            levelForAlpha = effectLevel;
        }


        ship.setExtraAlphaMult(1f - (1f - SHIP_ALPHA_MULT) * levelForAlpha);
        ship.setApplyExtraAlphaToEngines(true);

        float shipTimeMult = 1f + (getMaxTimeMult(stats) - 1f) * levelForAlpha;
        stats.getTimeMult().modifyMult(id, shipTimeMult);
        if (player) {
            Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
        } else {
            Global.getCombatEngine().getTimeMult().unmodify(id);
        }

        //extra fx
        ship.setJitter(id, JITTER_COLOR, 2, 2, 4f);
        ship.setJitterUnder(id, JITTER_UNDER_COLOR, 2, 2, 4f);
        ship.addAfterimage(AFTERIMAGE_COLOR, MathUtils.getRandomNumberInRange(40, -40), MathUtils.getRandomNumberInRange(40, -40), MathUtils.getRandomNumberInRange(40, -40), MathUtils.getRandomNumberInRange(40, -40), 1, 0.25f, 1.0f, 0.5f, false, false, false);

        //explosion on exit
        if (state == State.OUT) {

            //remove tidi
            stats.getTimeMult().unmodify(id);
            Global.getCombatEngine().getTimeMult().unmodify(id);

            ship.setPhased(false);
            ship.setExtraAlphaMult(1f);

                stats.getMaxSpeed().unmodify(id);
                stats.getAcceleration().unmodify(id);

                if (Explosions) {

                    //AOE DAMAGE
                    combatUtil.applyAOEDamage(ship, null, ship.getLocation(), EXPLOSION_DAMAGE_AMOUNT, DamageType.HIGH_EXPLOSIVE, EXPLOSION_PUSH_RADIUS, false);

                    engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS,
                            0.31f);
                    engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS /
                            2f, 0.29f);

                    Vector2f loc = new Vector2f(ship.getLocation());
                    loc.x -= 8f * FastTrig.cos(ship.getFacing() * Math.PI / 180f);
                    loc.y -= 8f * FastTrig.sin(ship.getFacing() * Math.PI / 180f);

                    //particle FX
                    Vector2f particlePos, particleVel;
                    for (int x = 0; x < 150; x++) {
                        particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), (float) Math.random() * (ship.getCollisionRadius() + 50f));
                        particleVel = Vector2f.sub(particlePos, ship.getLocation(), null);
                        Global.getCombatEngine().addSmokeParticle(particlePos, particleVel, MathUtils.getRandomNumberInRange(3f,6f), 0.85f, 2.50f,
                                LENS_FLARE_CORE_COLOR);
                    }

                    //shockwaves
                    blastSpriteCreator.blastSpriteListener shockwave1 = new blastSpriteCreator.blastSpriteListener(ship, ship.getLocation(), 2.00f, EXPLOSION_PUSH_RADIUS-175f, SHOCKWAVE_COLOR1);
                    shockwave1.customSpritePath = SPRITE_PATH_SHOCKWAVE;
                    shockwave1.sizeEaseOutSine = true;
                    shockwave1.alphaEaseInSine = true;
                    shockwave1.endSizeMult = 1.05f;
                    ship.addListener(shockwave1);

                    blastSpriteCreator.blastSpriteListener shockwave2 = new blastSpriteCreator.blastSpriteListener(ship, ship.getLocation(), 2.50f, EXPLOSION_PUSH_RADIUS, SHOCKWAVE_COLOR2);
                    shockwave2.customSpritePath = SPRITE_PATH_SHOCKWAVE;
                    shockwave2.sizeEaseOutSine = true;
                    shockwave2.alphaEaseInSine = true;
                    shockwave2.endSizeMult = 1.05f;
                    ship.addListener(shockwave2);

                    //glow
                    blastSpriteCreator.blastSpriteListener glow = new blastSpriteCreator.blastSpriteListener(ship, ship.getLocation(), 0.40f, EXPLOSION_PUSH_RADIUS, LENS_FLARE_CORE_COLOR);
                    glow.customSpritePath = SPRITE_PATH_GLOW;
                    glow.sizeEaseOutSine = true;
                    glow.alphaEaseOutSine = true;
                    glow.endSizeMult = 0.9f;
                    glow.additive = true;
                    ship.addListener(glow);

                    //light
                    light = new StandardLight();
                    light.setLocation(loc);
                    light.setIntensity(2.0f);
                    light.setSize(EXPLOSION_VISUAL_RADIUS * 0.5f);
                    light.setColor(EXPLOSION_COLOR);
                    light.fadeOut(3f);
                    LightShader.addLight(light);

                    //distortion fx
                    wave = new WaveDistortion();
                    wave.setLocation(loc);
                    wave.setIntensity(DISTORTION_BLAST_RADIUS * 0.08f);
                    wave.setSize(DISTORTION_BLAST_RADIUS * 0.75f);
                    wave.fadeInSize(1.2f);
                    wave.fadeOutIntensity(0.9f);
                    DistortionShader.addDistortion(wave);

                    Global.getSoundPlayer().playSound(SOUND_ID, 0.7f, 1.0f, ship.getLocation(), ship.getVelocity());

                    ShipAPI victim;
                    Vector2f dir;
                    float force, damage, emp, mod;

                    for (CombatEntityAPI tmp : CombatUtils.getEntitiesWithinRange(ship.getLocation(),EXPLOSION_PUSH_RADIUS)){
                        if (tmp.getCollisionClass()==CollisionClass.NONE) continue;

                        mod = 1f - (MathUtils.getDistance(ship, tmp) / EXPLOSION_PUSH_RADIUS);
                        force = FORCE_VS_ASTEROID * mod;
                        damage = EXPLOSION_DAMAGE_AMOUNT * mod;
                        emp = EXPLOSION_EMP_DAMAGE_AMOUNT * mod;

                        //fuck missiles
                        if (tmp instanceof MissileAPI) {
                            force = FORCE_VS_MISSILE * mod;
                            engine.applyDamage(tmp, loc, 400, DamageType.FRAGMENTATION, 0, false, false, ship);
                        }
                        if (tmp instanceof ShipAPI) {
                            victim = (ShipAPI) tmp;

                            // Modify push strength and dmg based on ship class
                            if (victim.getHullSize() == ShipAPI.HullSize.FIGHTER) {
                                force = FORCE_VS_FIGHTER * mod;
                                damage /= DAMAGE_MOD_VS_FIGHTER;
                            } else if (victim.getHullSize() == ShipAPI.HullSize.FRIGATE) {
                                force = FORCE_VS_FRIGATE * mod;
                                damage /= DAMAGE_MOD_VS_FRIGATE;
                            } else if (victim.getHullSize() == ShipAPI.HullSize.DESTROYER) {
                                force = FORCE_VS_DESTROYER * mod;
                                damage /= DAMAGE_MOD_VS_DESTROYER;
                            } else if (victim.getHullSize() == ShipAPI.HullSize.CRUISER) {
                                force = FORCE_VS_CRUISER * mod;
                                damage /= DAMAGE_MOD_VS_CRUISER;
                            } else if (victim.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) {
                                force = FORCE_VS_CAPITAL * mod;
                                damage /= DAMAGE_MOD_VS_CAPITAL;
                            }

                            if (victim.getOwner() == ship.getOwner()) {
                                damage *= EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER;
                                emp *= EXPLOSION_EMP_VS_ALLIES_MODIFIER;
                                force *= EXPLOSION_FORCE_VS_ALLIES_MODIFIER;
                            }

                            //spawn emp arcs to unshielded targets
                            if ((victim.getShield() != null && victim.getShield().isOn() && victim.getShield().isWithinArc(
                                    ship.getLocation()))) {
                                victim.getFluxTracker().increaseFlux(damage * 1, true);
                            } else {
                                ShipAPI empTarget = victim;
                                for (int x = 0; x < 5; x++) {
                                    EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(ship, combatUtil.getRandomPointOnShip(victim),
                                            empTarget,
                                            empTarget, EXPLOSION_DAMAGE_TYPE, damage / 10, emp / 5,
                                            EXPLOSION_PUSH_RADIUS, null, 2f, EMP_COLOR,
                                            EMP_COLOR);
                                }
                            }
                        }

                        dir = VectorUtils.getDirectionalVector(ship.getLocation(), tmp.getLocation());
                        dir.scale(force);

                        Vector2f.add(tmp.getVelocity(), dir, tmp.getVelocity());
                    }

                    ship.getSystem().deactivate();
                    Explosions = false;
            }
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            id = id + "_" + ship.getId();
        } else {
            return;
        }
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);

        //remove tidi again just to be safe
        stats.getTimeMult().unmodify(id);
        Global.getCombatEngine().getTimeMult().unmodify(id);

        ship.setPhased(false);
        ship.setExtraAlphaMult(1f);

        updated = false;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }
}























