package scripts.kissa.LOST_SECTOR.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.jetbrains.annotations.Nullable;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.shipsystems.nskr_bigFlakStats;
import scripts.kissa.LOST_SECTOR.util.blastSpriteCreator;
import scripts.kissa.LOST_SECTOR.util.combatUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;

public class nskr_bigFlak {

    public static class bigFlakProjectileVisualListener implements AdvanceableListener {

        public DamagingProjectileAPI projectile;
        public ShipAPI ship;
        private float fuzeTimer;

        public bigFlakProjectileVisualListener(DamagingProjectileAPI projectile, ShipAPI ship) {
            this.ship = ship;
            this.projectile = projectile;
        }

        @Override
        public void advance(float amount) {
            CombatEngineAPI engine = Global.getCombatEngine();
            //engine.addFloatingText(ship.getLocation(), "LMAO", 54f, Color.CYAN, null,1f,1f);
            if (projectile == null){
                //engine.addFloatingText(ship.getLocation(), "KEKW", 34f, Color.RED, null,1f,1f);
                ship.removeListener(this);
                return;
            }
            if (engine.isPaused()) {
                return;
            }
            if (projectile.didDamage()){
                ship.removeListener(this);
                return;
            }
            Vector2f point = projectile.getLocation();
            //reached max range
            if (projectile.isFading() || projectile.isExpired()){
                trigger(engine, point);
                return;
            }

            //sound
            Global.getSoundPlayer().playLoop("nskr_moab_loop", projectile, 1f, 0.55f, projectile.getLocation(), projectile.getVelocity());

            //don't trigger before fuze
            fuzeTimer += amount;
            if (fuzeTimer<1.5f) return;

            float fuse = nskr_bigFlakStats.EXPLOSION_RADIUS/2f;
            float missileCount = 0f;
            //prox fuse
            for (CombatEntityAPI c : CombatUtils.getEntitiesWithinRange(projectile.getLocation(), fuse)){
                if (c.getOwner()== projectile.getOwner()) continue;
                if (c instanceof ShipAPI) {
                    ShipAPI target = (ShipAPI) c;
                    //direct hit approaching
                    if (Math.abs(MathUtils.getShortestRotation(VectorUtils.getAngle(projectile.getLocation(), target.getLocation()), projectile.getFacing())) < 30f) continue;

                    if (MathUtils.getDistance(combatUtil.getNearestPointOnBounds(projectile.getLocation(), c), projectile.getLocation()) < fuse){
                        trigger(engine, point);
                        return;
                    }
                }
                if (c instanceof MissileAPI){
                    MissileAPI missile = (MissileAPI) c;
                    if (missile.getDamageAmount()<=0f) continue;
                    missileCount += missile.getDamageAmount();
                }
            }
            //only blow up a significant amount of missiles
            if (missileCount >= 2000f){
                trigger(engine, point);
                return;
            }

        }
        private void trigger(CombatEngineAPI engine, Vector2f point) {
            explode(point, ship, null);
            engine.removeEntity(projectile);
            ship.removeListener(this);
        }
    }

    public static final String SPRITE_PATH_1 = "graphics/fx/explosion5.png";
    public static final String SPRITE_PATH_4 = "graphics/fx/explosion6.png";
    public static final String SPRITE_PATH_2 = "graphics/fx/explosion_ring0.png";
    public static final String SPRITE_PATH_3 = "graphics/fx/nskr_blast_plasmal.png";

    public static final Color SHOCKWAVE_COLOR_1 = new Color(255, 238, 241, 200);
    public static final Color SHOCKWAVE_COLOR_5 = new Color(255, 247, 249, 255);
    public static final Color SHOCKWAVE_COLOR_4 = new Color(255, 58, 94, 100);
    public static final Color SHOCKWAVE_COLOR_2 = new Color(255, 22, 112, 20);
    public static final Color SHOCKWAVE_COLOR_3 = new Color(255, 45, 95, 15);

    public static final Color PARTICLE_COLOR = new Color(255, 27, 61, 200);

    public static void explode(Vector2f point, ShipAPI source, @Nullable CombatEntityAPI mainTarget) {
        //actual effect
        combatUtil.applyAOEDamage(source, mainTarget, point, nskr_bigFlakStats.EXPLOSION_DAMAGE, DamageType.HIGH_EXPLOSIVE, nskr_bigFlakStats.EXPLOSION_RADIUS,false);

        Global.getSoundPlayer().playSound("nskr_moab_explode", 0.8f, 0.9f, point, new Vector2f());

        //FX
        blastSpriteCreator.blastSpriteListener glow1 = new blastSpriteCreator.blastSpriteListener(source, point, 1.5f, 700f, SHOCKWAVE_COLOR_1);
        glow1.customSpritePath = SPRITE_PATH_1;
        glow1.sizeEaseOutSine = true;
        glow1.alphaEaseOutSine = true;
        glow1.endSizeMult = 0.9f;
        glow1.additive = true;
        source.addListener(glow1);

        blastSpriteCreator.blastSpriteListener glow2 = new blastSpriteCreator.blastSpriteListener(source, point, 0.75f, 150f, SHOCKWAVE_COLOR_5);
        glow2.customSpritePath = SPRITE_PATH_1;
        glow2.sizeEaseOutSine = true;
        glow2.alphaEaseOutSine = true;
        glow2.endSizeMult = 0.9f;
        glow2.additive = true;
        source.addListener(glow2);

        blastSpriteCreator.blastSpriteListener blast = new blastSpriteCreator.blastSpriteListener(source, point, 3.0f, 125f, SHOCKWAVE_COLOR_4);
        blast.customSpritePath = SPRITE_PATH_4;
        blast.sizeEaseOutSine = true;
        blast.alphaEaseInSine = true;
        blast.endSizeMult = 1.25f;
        source.addListener(blast);

        blastSpriteCreator.blastSpriteListener shockwave1 = new blastSpriteCreator.blastSpriteListener(source, point, 2.00f, 325f, SHOCKWAVE_COLOR_2);
        shockwave1.customSpritePath = SPRITE_PATH_3;
        shockwave1.sizeEaseOutSine = true;
        shockwave1.alphaEaseInSine = true;
        shockwave1.endSizeMult = 1.15f;
        source.addListener(shockwave1);

        blastSpriteCreator.blastSpriteListener shockwave2 = new blastSpriteCreator.blastSpriteListener(source, point, 2.50f, 400f, SHOCKWAVE_COLOR_3);
        shockwave2.customSpritePath = SPRITE_PATH_3;
        shockwave2.sizeEaseOutSine = true;
        shockwave2.alphaEaseInSine = true;
        shockwave2.endSizeMult = 1.15f;
        source.addListener(shockwave2);

        //exlosion
        Global.getCombatEngine().spawnExplosion(point, new Vector2f(0f,0f), SHOCKWAVE_COLOR_1, 300f, 1f);

        //light fx
        StandardLight light = new StandardLight();
        light.setLocation(point);
        light.setIntensity(2.0f);
        light.setSize(500f * 0.5f);
        light.setColor(SHOCKWAVE_COLOR_1);
        light.fadeOut(2f);
        LightShader.addLight(light);

        //particle fx
        Vector2f particlePos, particleVel;
        Color color = util.randomiseColor(PARTICLE_COLOR, 25, 0,25,25,false);
        for (int x = 0; x < 125; x++) {
            particlePos = MathUtils.getRandomPointOnCircumference(point, (float) Math.random() * 200f);
            particleVel = Vector2f.sub(particlePos, point, null);
            Global.getCombatEngine().addSmokeParticle(particlePos, mathUtil.scaleVector(particleVel, 2f), 4f, 0.50f, 2.0f,
                    color);
        }
    }
}
