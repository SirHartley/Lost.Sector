package scripts.kissa.LOST_SECTOR.weapons;

import java.awt.Color;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicLensFlare;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;


public class nskr_cdummy1OH implements OnHitEffectPlugin {

    public static final Color CORE_COLOR = new Color(255, 77, 154, 255);
    public static final Color FLARE_COLOR = new Color(255, 230, 241, 255);

    public static final Vector2f ZERO = new Vector2f();

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        ShipAPI ship = projectile.getSource();

        //Global.getSoundPlayer().playSound("nskr_tremor_impact", 1.0f, 1.0f, point, ZERO);

        for (int x = 0; x < 4; x++) {
            float angle = (float) Math.random() * 360f;
            float distance = (float) Math.random() * 50f + 25f;
            Vector2f point1 = MathUtils.getPointOnCircumference(point, distance, angle);

            engine.addSwirlyNebulaParticle(point1, ZERO, 5f,35f,0.35f,0.35f,0.7f, CORE_COLOR,false);
        }
        MagicLensFlare.createSharpFlare(engine, ship, point, 1f, 120f, projectile.getFacing()-90f, FLARE_COLOR, CORE_COLOR);
    }
}