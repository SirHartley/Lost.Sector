package scripts.kissa.LOST_SECTOR.plugins;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class nskr_torporSystemLights implements EveryFrameWeaponEffectPlugin {

    //torpor fx

    private WeaponAPI stasisWeapon = null;
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        ShipAPI ship = weapon.getShip();
        AnimationAPI theAnim = weapon.getAnimation();

        if (!ship.isAlive()) {
            theAnim.setFrame(0);
            return;
        }

        if (stasisWeapon==null) {
            for (WeaponAPI w : ship.getAllWeapons()) {
                if (w.getId() == null) continue;
                if (w.getId().equals("nskr_stasis")) {
                    stasisWeapon = w;
                    break;
                }
            }
        }

        if (stasisWeapon.getCooldownRemaining()>0f) {
            theAnim.setFrame(1);
        } else {
            theAnim.setFrame(0);
        }
    }
}
