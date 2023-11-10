package scripts.kissa.LOST_SECTOR.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;

public class nskr_bigFlakEffect  implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

        ShipAPI ship = projectile.getSource();
        ship.addListener(new nskr_bigFlak.bigFlakProjectileVisualListener(projectile, ship));
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        //init data
        ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("BIGFLAK_AI_DATA_KEY" + weapon.getShip().getId());
        if (data == null){
            data = new ShipSpecificData();
            engine.getCustomData().put("BIGFLAK_AI_DATA_KEY" + weapon.getShip().getId(), data);
        }
    }

    public static class ShipSpecificData {
        public boolean left = true;
        public boolean right = false;
    }
}