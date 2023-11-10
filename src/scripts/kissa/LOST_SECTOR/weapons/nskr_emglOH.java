package scripts.kissa.LOST_SECTOR.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

public class nskr_emglOH implements OnHitEffectPlugin {

    public static final String ID = "nskr_emgl_sub";

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        if (!shieldHit && target instanceof ShipAPI) {
            ((nskr_emglScript) projectile.getWeapon().getEffectPlugin()).putHIT(target);
            engine.spawnProjectile(
                    projectile.getSource(),
                    projectile.getWeapon(),
                    ID,
                    point,
                    projectile.getFacing(),
                    target.getVelocity()
            );
        }
    }
}
