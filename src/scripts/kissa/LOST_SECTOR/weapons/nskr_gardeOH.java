package scripts.kissa.LOST_SECTOR.weapons;

import java.awt.Color;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicLensFlare;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.combatUtil;

public class nskr_gardeOH implements OnHitEffectPlugin {

    public static final float EXP_RANGE = 250f;
    public static final Color CORE_COLOR = new Color(255, 70, 92, 200);
    public static final Color FRINGE_COLOR = new Color(200,0, 83,255);
    public static final Color SMOKE_COLOR = new Color(196, 53, 114,105);
    public static final Vector2f ZERO = new Vector2f();

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine){
        ShipAPI ship = projectile.getSource();
        float baseDamage = projectile.getDamageAmount()*1.0f;

        combatUtil.applyAOEDamage(ship, target, point, baseDamage, DamageType.ENERGY, EXP_RANGE, false);

        MagicLensFlare.createSharpFlare(engine, ship, point, 1f, 100f, projectile.getFacing()-90f, FRINGE_COLOR, CORE_COLOR);
        engine.addNebulaParticle(point, ZERO, EXP_RANGE/4f, 2.0f, 0f, 0.3f, 0.5f, CORE_COLOR);
        engine.addSmokeParticle(point, ZERO, 100f, 0.15f, 0.50f, SMOKE_COLOR);
    }
}