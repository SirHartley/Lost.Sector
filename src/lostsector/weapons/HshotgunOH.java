package lostsector.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

public class HshotgunOH implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine){

        float dam = projectile.getDamageAmount() * 1f;

        engine.applyDamage(target, point, dam, DamageType.FRAGMENTATION, 0, false, false, projectile.getSource());

    }
}