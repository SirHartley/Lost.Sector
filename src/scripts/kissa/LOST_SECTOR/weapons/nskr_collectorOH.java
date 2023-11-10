package scripts.kissa.LOST_SECTOR.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class nskr_collectorOH implements OnHitEffectPlugin {

    public static final float EXECUTE_PERCENTAGE = 0.15f;
    public static final Vector2f ZERO = new Vector2f();

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        ShipAPI ship = projectile.getSource();

        if (!shieldHit && target instanceof ShipAPI && target.getHullLevel() < EXECUTE_PERCENTAGE && ((ShipAPI) target).isAlive()) {
            if (((ShipAPI) target).getHullSize() != ShipAPI.HullSize.FIGHTER) {
                if (ship == Global.getCombatEngine().getPlayerShip()) {
                    Global.getCombatEngine().addFloatingText(point, "COLLECTED", 24f, Color.RED, null, 1f, 3f);
                }
                Global.getSoundPlayer().playSound("nskr_collected", 1.0f, 1.0f, point, ZERO);
            }

            float dmg = Math.max(9999f, (target.getMaxHitpoints()*0.30f));
            engine.applyDamage(target, point, dmg, DamageType.HIGH_EXPLOSIVE, 0f, true, false, ship);
        }
    }
}
