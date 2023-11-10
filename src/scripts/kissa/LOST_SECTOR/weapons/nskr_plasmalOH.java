//////////////////////
//Initially created by Nia Tahl and modified from Tahlan Shipworks
//////////////////////
package scripts.kissa.LOST_SECTOR.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.combatUtil;
import scripts.kissa.LOST_SECTOR.util.blastSpriteCreator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.fs.starfarer.api.util.Misc.ZERO;

public class nskr_plasmalOH implements OnHitEffectPlugin {

    public static final Color PARTICLE_COLOR = new Color(178, 110,255, 250);
    public static final Color HIT_PARTICLE_COLOR = new Color(255, 73, 156, 250);
    public static final Color CORE_COLOR = new Color(30, 184, 255, 250);
    public static final Color FLASH_COLOR = new Color(224, 255, 248, 200);
    //shockwave
    public static final String SPRITE_PATH = "graphics/fx/nskr_blast_plasmal.png";
    public static final float SHOCKWAVE_SIZE = 580f;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        //we pass through missiles so this would look wonky
        if (target instanceof MissileAPI) return;

        //AOE DMG
        combatUtil.applyAOEDamage(projectile.getSource(), target, point, projectile.getDamageAmount(), projectile.getDamageType(), 600f, false);
        
        // Blast visuals
        float ExplosionRadius = 280f;
        float ExplosionDuration = 2.0f;
        float CoreGlowRadius = 200f;
        float CoreGlowDuration = 2f;
        float GlowRadius = 250f;
        float GlowDuration = 2f;
        float FlashGlowRadius = 300f;
        float FlashGlowDuration = 0.05f;

        //small
        Global.getCombatEngine().addNebulaSmokeParticle(point, ZERO, ExplosionRadius/2f, 0.8f, 0.2f, 0.2f, ExplosionDuration, PARTICLE_COLOR);
        Global.getCombatEngine().addSwirlyNebulaParticle(point, ZERO, ExplosionRadius, 0.9f, 0.2f, 0.2f, ExplosionDuration, FLASH_COLOR, true);
        //large
        Global.getCombatEngine().addSwirlyNebulaParticle(
                point, ZERO, ExplosionRadius*2.5f, 0.9f, 0.0f, 0.2f, 0.75f, Misc.setAlpha(FLASH_COLOR, 150), true);

        engine.addHitParticle(point, ZERO, CoreGlowRadius, 1f, CoreGlowDuration, CORE_COLOR);
        engine.addSmoothParticle(point, ZERO, GlowRadius, 1f, GlowDuration, PARTICLE_COLOR);
        engine.addHitParticle(point, ZERO, FlashGlowRadius, 1f, FlashGlowDuration, FLASH_COLOR);

        combatUtil.createHitParticles(point, projectile.getFacing(), 45f, HIT_PARTICLE_COLOR, 25f, 5f, 0.3f, 1.0f, 50f, 3.0f);

        //light fx
        StandardLight light = new StandardLight();
        light.setLocation(point);
        light.setIntensity(0.8f);
        light.setSize(600f);
        light.setColor(FLASH_COLOR);
        light.fadeOut(1f);
        LightShader.addLight(light);

        //shockwave visuals
        //blast 1
        Color shockwave1Color = new Color(199, 244, 255, 12);
        blastSpriteCreator.blastSpriteListener shockwave1 = new blastSpriteCreator.blastSpriteListener(projectile.getSource(), point, 1.30f, SHOCKWAVE_SIZE, shockwave1Color);
        shockwave1.alphaEaseInCubic = true;
        shockwave1.sizeEaseOutSine = true;
        shockwave1.customSpritePath = SPRITE_PATH;
        shockwave1.endSizeMult = 1.05f;
        projectile.getSource().addListener(shockwave1);
        //blast 2
        Color shockwave2Color = new Color(190, 222, 255, 18);
        blastSpriteCreator.blastSpriteListener shockwave2 = new blastSpriteCreator.blastSpriteListener(projectile.getSource(), point, 0.85f, SHOCKWAVE_SIZE*0.80f, shockwave2Color);
        shockwave2.alphaEaseInCubic = true;
        shockwave2.sizeEaseOutSine = true;
        shockwave2.customSpritePath = SPRITE_PATH;
        shockwave2.endSizeMult = 1.05f;
        projectile.getSource().addListener(shockwave2);
        //blast 3
        Color shockwave3Color = new Color(182, 176, 255, 24);
        blastSpriteCreator.blastSpriteListener shockwave3 = new blastSpriteCreator.blastSpriteListener(projectile.getSource(), point, 0.40f, SHOCKWAVE_SIZE*0.60f, shockwave3Color);
        shockwave3.alphaEaseInCubic = true;
        shockwave3.sizeEaseOutSine = true;
        shockwave3.customSpritePath = SPRITE_PATH;
        shockwave3.endSizeMult = 1.05f;
        projectile.getSource().addListener(shockwave3);

        Global.getSoundPlayer().playSound("nskr_plasmal_hit",1.0f,0.80f,point,ZERO);
    }

}
