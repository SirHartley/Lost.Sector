package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class nskr_omniLoadStats extends BaseShipSystemScript {

    public static final Color JITTER_COLOR = new Color(255, 108, 130, 75);
    public static final Color JITTER_UNDER_COLOR = new Color(255, 91, 139, 155);
    boolean reloaded = false;
    private boolean updated = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        float jitterLevel = effectLevel;
        if (state == State.OUT) {
            jitterLevel *= jitterLevel;
        }
        float jitterRangeBonus = jitterLevel * 25f;

        ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 11, 0f, 3f + jitterRangeBonus);
        ship.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0f, 0 + jitterRangeBonus);

        if (!updated) {
            reloaded = false;

            updated = true;
        }

        if (state == State.ACTIVE && !reloaded) {
            //check for burst
            if (!canApply(ship)) return;

            for (WeaponAPI w : ship.getAllWeapons()){
                if (!validWeapon(w)) continue;
                //reload
                w.setRemainingCooldownTo(0f);
                //Global.getCombatEngine().addFloatingText(ship.getLocation(), "test", 60f, Color.cyan, ship, 0.5f, 1.0f);
                //add ammo
                if (w.getMaxAmmo()<Integer.MAX_VALUE && w.getAmmo()<w.getMaxAmmo()){
                    int ammo = Math.max(1, (int)(w.getMaxAmmo()/4f)) + w.getAmmo();
                    ammo = Math.min(ammo, w.getMaxAmmo());

                    w.setAmmo(ammo);
                }
            }
            //sound
            Global.getSoundPlayer().playSound("nskr_omniload_activate", 1.0f, 0.70f, ship.getLocation(), new Vector2f());

            ship.getSystem().forceState(ShipSystemAPI.SystemState.OUT,0f);
            reloaded = true;
        }
    }

    private boolean canApply(ShipAPI ship) {
        for (WeaponAPI w : ship.getAllWeapons()) {
            if (!validWeapon(w)) continue;
            //burst hack
            if (w.isInBurst()) return false;
        }
        return true;
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        for (WeaponAPI w : ship.getAllWeapons()){
            if (!validWeapon(w)) continue;
            if (w.getCooldownRemaining()>0f) return true;
            if (w.getMaxAmmo()<Integer.MAX_VALUE && w.getAmmo()<w.getMaxAmmo()){
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDisplayNameOverride(State state, float effectLevel) {
        return null;
    }

    public static boolean validWeapon(WeaponAPI w) {
        if (w.getType() != WeaponAPI.WeaponType.BALLISTIC && w.getType() != WeaponAPI.WeaponType.ENERGY)
            return false;
        return w.getSize() == WeaponAPI.WeaponSize.MEDIUM;
    }
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

        updated = false;
    }
}

