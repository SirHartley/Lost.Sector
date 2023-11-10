package scripts.kissa.LOST_SECTOR.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

public class nskr_stasisOH implements OnHitEffectPlugin {

    public static final Vector2f ZERO = new Vector2f();

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine){

        ShipAPI ship = projectile.getSource();

        nskr_stasis.explode(point, ship);

    }
}
