package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import scripts.kissa.LOST_SECTOR.util.ids;

public class nskr_bigBats extends BaseHullMod {

    public static final int MAX_CHARGES = 1;
    public static final float AUGMENT_RECHARGE_BONUS = 10f;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSystemUsesBonus().modifyFlat(id, MAX_CHARGES);
    }
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship.getVariant().hasHullMod(ids.AUGMENTED_HULLMOD_ID)){
            ship.getMutableStats().getSystemRegenBonus().modifyPercent(id, AUGMENT_RECHARGE_BONUS);
        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "+"+MAX_CHARGES;
        return null;
    }
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        ShipSystemAPI system = ship.getSystem();
        if (system!=null){
            int uses = system.getSpecAPI().getMaxUses(ship.getMutableStats());
            //what a hack Alex
            if (uses < Integer.MAX_VALUE) {
                return true;
            }
        }
        ShipSystemSpecAPI defenseSystem = Global.getSettings().getShipSystemSpec(ship.getHullSpec().getShipDefenseId());
        if (defenseSystem!=null){
            int uses = defenseSystem.getMaxUses(ship.getMutableStats());
            if (uses < Integer.MAX_VALUE) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        ShipSystemAPI system = ship.getSystem();
        if (system!=null){
            int uses = system.getSpecAPI().getMaxUses(ship.getMutableStats());
            if (uses < Integer.MAX_VALUE) {
                return null;
            }
        }
        ShipSystemSpecAPI defenseSystem = Global.getSettings().getShipSystemSpec(ship.getHullSpec().getShipDefenseId());
        if (defenseSystem!=null){
            int uses = defenseSystem.getMaxUses(ship.getMutableStats());
            if (uses < Integer.MAX_VALUE) {
                return null;
            }
        }
        return "Can not be installed on ships without system charges.";
    }
}
