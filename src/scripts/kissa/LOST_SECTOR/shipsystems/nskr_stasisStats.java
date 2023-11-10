package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;


public class nskr_stasisStats extends BaseShipSystemScript {

    public static final float MAX_DURATION = 5f;
    public static final float MAX_ON_HIT_RANGE = 500f;

    private WeaponAPI weapon = null;
    private boolean reset = false;
    private boolean recharge = false;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
                ship = (ShipAPI) stats.getEntity();
                id = id + "_" + ship.getId();
        } else {
            return;
        }
        CombatEngineAPI engine = Global.getCombatEngine();

        if (weapon==null) {
            for (WeaponAPI w : ship.getAllWeapons()) {
                if (w.getId() == null) continue;
                if (w.getId().equals("nskr_stasis")) {
                    weapon = w;
                    break;
                }
            }
        }

        //recharge
        if (weapon.getCooldownRemaining()>0f){
            recharge = true;

            weapon.setRemainingCooldownTo(0f);
            //end
            ship.getSystem().forceState(ShipSystemAPI.SystemState.OUT,0f);
        }
        //fire
        else {
            reset = true;

            weapon.setForceFireOneFrame(true);
            //remove flux cost
            ship.getFluxTracker().decreaseFlux(ship.getHullSpec().getFluxCapacity()*0.33f);
            //end
            ship.getSystem().forceState(ShipSystemAPI.SystemState.OUT,0f);
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
        if (recharge){

            recharge = false;
        }

        if (reset){
            ship.getSystem().setCooldownRemaining(0.5f);

            reset = false;
        }
    }

    @Override
    public String getDisplayNameOverride(State state, float effectLevel) {
        if (state==State.COOLDOWN){
            return "Stasis - Cooldown";
        }

        //default
        if (weapon==null) {
            return "Stasis - Fire";
        }

        //recharge
        if (weapon.getCooldownRemaining()>0f){
            return "Stasis - Recharge";
        }
        //fire
        else {
            return "Stasis - Fire";
        }
    }
}





