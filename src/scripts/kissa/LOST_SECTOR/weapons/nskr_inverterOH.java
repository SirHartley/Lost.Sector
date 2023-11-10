package scripts.kissa.LOST_SECTOR.weapons;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicLensFlare;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lwjgl.util.vector.Vector2f;

public class nskr_inverterOH implements OnHitEffectPlugin {

    public static final Color CORE_COLOR = new Color(255, 182, 249, 155);
    public static final Color FRINGE_COLOR = new Color(153, 43, 255, 155);
    public static final Vector2f ZERO = new Vector2f();
    private WaveDistortion wave;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine){
        ShipAPI ship = projectile.getSource();

        if (!shieldHit && target instanceof ShipAPI) {
            //ammo
            WeaponAPI wep = projectile.getWeapon();
            int ammo = wep.getAmmo();
            if (ammo< wep.getMaxAmmo()){
                wep.setAmmo(ammo+1);
            }

            Global.getSoundPlayer().playSound("nskr_tremor_impact", 0.9f, 0.8f, point, ZERO);
            MagicLensFlare.createSharpFlare(engine, ship, point, 1f,90f, projectile.getFacing()-90f, FRINGE_COLOR, CORE_COLOR);

            //distortion fx
            wave = new WaveDistortion();
            wave.setLocation(point);
            wave.setSize(30f);
            wave.setIntensity(10.0f);
            wave.setAutoFadeIntensityTime(0.25f);
            wave.setAutoFadeSizeTime(1f);
            wave.setLifetime(0.05f);
            DistortionShader.addDistortion(wave);
        }
    }
}