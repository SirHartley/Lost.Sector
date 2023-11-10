package scripts.kissa.LOST_SECTOR.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.Misc;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;
import scripts.kissa.LOST_SECTOR.util.blastSpriteCreator;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class nskr_emFlak {

    public static final Vector2f ZERO = new Vector2f();

    public static final Color CORE_COLOR = new Color(182, 200, 255, 255);
    public static final Color FRINGE_COLOR = new Color(156, 43, 255, 255);
    public static final Color EXPLO_COLOR = new Color(99, 58, 71, 30);
    public static final Color EXPLO_COLOR2 = new Color(82, 72, 68, 30);
    public static final Color PARTICLE_COLOR = new Color(179, 63, 111, 175);
    public static final Color SMOKE_COLOR = new Color(75, 74, 74, 100);
    public static final String SPRITE_PATH = "graphics/fx/nskr_blast1.png";

    public static class emFlakListener implements AdvanceableListener {
        public DamagingProjectileAPI projectile;

        public emFlakListener(DamagingProjectileAPI projectile) {
            this.projectile = projectile;
        }

        @Override
        public void advance(float amount) {
            CombatEngineAPI engine = Global.getCombatEngine();

            ShipAPI ship = projectile.getSource();
            Vector2f point = projectile.getLocation();

            float emDamage, damage, mult;
            //damage radius
            java.util.List<CombatEntityAPI> entities = new ArrayList<>(CombatUtils.getEntitiesWithinRange(projectile.getLocation(),
                    nskr_emflakEffect.EXPLOSION_RADIUS));
            entities.remove(projectile);
            entities.remove(projectile.getSource());
            //fuze radius
            java.util.List<CombatEntityAPI> entitiesFuze = new ArrayList<>(CombatUtils.getEntitiesWithinRange(projectile.getLocation(),
                    nskr_emflakEffect.FUZE_RADIUS));
            entitiesFuze.remove(projectile);
            entitiesFuze.remove(projectile.getSource());

            //BYE BYE, remove stuff so we actually have a working fuze
            List<CombatEntityAPI> entitiesCopy = new ArrayList<>(entities);

            for (CombatEntityAPI removal : entitiesCopy) {
                if (removal.getOwner() == ship.getOwner() || removal.getCollisionClass() == CollisionClass.NONE || removal.isExpired() || removal.getHullLevel()<=0f) {
                    entitiesFuze.remove(removal);
                    entities.remove(removal);
                }
                //don't proc fuze on vulcan rounds
                if (removal instanceof DamagingProjectileAPI){
                    if (((DamagingProjectileAPI) removal).getDamageAmount() < nskr_emflakEffect.FUZE_MIN_DMG) entitiesFuze.remove(removal);
                }
            }

            if (!entitiesFuze.isEmpty()) {
                for (CombatEntityAPI entity : entities) {
                    //scaling on distance
                    float dist = MathUtils.getDistance(projectile.getLocation(), entity.getLocation());
                    mult = 1f;
                    mult = mathUtil.normalize(dist, 0f, nskr_emflakEffect.EXPLOSION_RADIUS);
                    mult = mathUtil.inverse(mult);

                    emDamage = projectile.getEmpAmount() * mult;
                    damage = projectile.getDamageAmount() * mult;

                    if (entity instanceof MissileAPI || entity instanceof ShipAPI){
                        engine.applyDamage(entity, entity.getLocation(), damage, DamageType.ENERGY, emDamage, false, false, ship);
                    }
                    //chance to remove proj based on distance
                    if (entity instanceof DamagingProjectileAPI) {
                        if (Math.random() < mult) {
                            if (((DamagingProjectileAPI) entity).getDamageAmount() < 1) continue;

                            engine.spawnEmpArcVisual(projectile.getLocation(), projectile, entity.getLocation(), entity, 3f, FRINGE_COLOR, CORE_COLOR);

                            engine.removeEntity(entity);
                        }
                    }
                }
                //one time
                //FX
                float mod = 1f;
                float sqrtLevel = 7.00f;
                RippleDistortion ripple = new RippleDistortion(projectile.getLocation(), ZERO);
                ripple.setSize((sqrtLevel * 15f) / mod);
                ripple.setIntensity((sqrtLevel * 1.5f) / mod);
                ripple.setFrameRate((60f) / mod);
                ripple.fadeInSize((sqrtLevel / 4f) / mod);
                ripple.fadeOutIntensity((sqrtLevel / 4f) / mod);
                DistortionShader.addDistortion(ripple);

                MagicLensFlare.createSharpFlare(engine, ship, point, 1f, 100f, projectile.getFacing()-90f, FRINGE_COLOR, CORE_COLOR);

                engine.addNebulaParticle(projectile.getLocation(), ZERO, 100f, 0.75f, 0.10f, 0.75f, MathUtils.getRandomNumberInRange(0.75f,1.25f), EXPLO_COLOR);

                for (int i = 0; i < 20; i++) {
                    float angle = (float) Math.random() * 360f;
                    float distance = (float) Math.random() * 50f + 10f;

                    Vector2f pPoint = MathUtils.getPointOnCircumference(projectile.getLocation(), distance, angle);

                    Vector2f newVel = new Vector2f(projectile.getVelocity());
                    newVel.scale((float) Math.random());

                    engine.addSmoothParticle(pPoint, newVel, 5f, 1f, MathUtils.getRandomNumberInRange(0.5f, 1.5f), PARTICLE_COLOR);
                }
                //DETONATED
                Global.getSoundPlayer().playSound("prox_charge_explosion", 1.2f, 0.70f, ship.getLocation(), ZERO);

                //blast sprite
                blastSpriteCreator.blastSpriteListener shockwave = new blastSpriteCreator.blastSpriteListener(ship, projectile.getLocation(), 0.67f, nskr_emflakEffect.EXPLOSION_RADIUS-50f,
                        Misc.setAlpha(FRINGE_COLOR, 15));
                shockwave.customSpritePath = SPRITE_PATH;
                shockwave.alphaEaseInSine = true;
                shockwave.sizeEaseOutQuad = true;
                shockwave.baseSize = 50f;
                shockwave.startSizeMult = 0f;
                ship.addListener(shockwave);

                engine.removeEntity(projectile);
                ship.removeListener(this);
            }
            //UNDETONATED
            if (projectile.isFading()){
                //FX
                engine.addNebulaParticle(projectile.getLocation(), ZERO, 75f, 0.75f, 0.10f,0.75f, MathUtils.getRandomNumberInRange(0.75f,1.25f), EXPLO_COLOR2);

                for (int i = 0; i < 1; i++) {
                    float angle = (float) Math.random() * 360f;
                    float distance = (float) Math.random() * 25f + 10f;

                    Vector2f pPoint = MathUtils.getPointOnCircumference(projectile.getLocation(), distance, angle);

                    Vector2f newVel = new Vector2f(projectile.getVelocity());
                    newVel.scale((float)Math.random());

                    engine.addSmokeParticle(pPoint, newVel, 25f, 1f, MathUtils.getRandomNumberInRange(0.5f,1.5f), SMOKE_COLOR);
                    engine.addNebulaSmokeParticle(pPoint, newVel, 25f, 1f,1f,1f, MathUtils.getRandomNumberInRange(0.5f,1.5f), SMOKE_COLOR);
                }
                //LATA
                Global.getSoundPlayer().playSound("hurricane_mirv_split", 0.8f, 0.75f, ship.getLocation(), ZERO);
                engine.removeEntity(projectile);
                ship.removeListener(this);
            }
        }
    }
}
