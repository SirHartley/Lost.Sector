package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import scripts.kissa.LOST_SECTOR.shipsystems.ai.nskr_bigFlakAI;
import scripts.kissa.LOST_SECTOR.weapons.nskr_bigFlakEffect;

public class nskr_bigFlakStats extends BaseShipSystemScript {

    public static final float EXPLOSION_RADIUS = 500f;
    public static final float EXPLOSION_DAMAGE = 1500f;

    private WeaponAPI weaponL = null;
    private WeaponAPI weaponR = null;
    private int count = 0;
    private boolean fire = false;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        nskr_bigFlakEffect.ShipSpecificData data = (nskr_bigFlakEffect.ShipSpecificData) Global.getCombatEngine().getCustomData().get("BIGFLAK_AI_DATA_KEY" + ship.getId());

        if (weaponL==null || weaponR==null) {
            for (WeaponAPI w : ship.getAllWeapons()) {
                if (w.getId() == null) continue;
                if (w.getSlot() == null) continue;

                if (w.getSlot().getId().equals("WS0006")) weaponL = w;
                if (w.getSlot().getId().equals("WS0007")) weaponR = w;
            }
        }

        //fire
        if (!fire) {
            fire = true;
            count++;
            if (count % 2 >= 1) {
                weaponL.setForceFireOneFrame(true);
                //set AI data for next shot
                if (data != null) {
                    data.left = false;
                    data.right = true;
                }
            } else {
                weaponR.setForceFireOneFrame(true);
                //set AI data for next shot
                if (data != null) {
                    data.left = true;
                    data.right = false;
                }
            }

            //end
            ship.getSystem().forceState(ShipSystemAPI.SystemState.OUT, 0f);
        }

        if (data != null) {
            Global.getCombatEngine().getCustomData().put("BIGFLAK_AI_DATA_KEY" + ship.getId(), data);
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        if (fire){
            fire = false;
        }
    }

    @Override
    public String getDisplayNameOverride(State state, float effectLevel) {
        //default
        int tempCount = count+1;
        if (tempCount == 1) {
            return "Flak - Left Barrel";
        }

        if (tempCount % 2 >= 1) {
            return "Flak - Left Barrel";
        } else {
            return "Flak - Right Barrel";
        }
    }
}