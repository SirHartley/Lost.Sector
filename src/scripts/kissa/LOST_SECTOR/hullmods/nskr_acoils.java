package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;

public class nskr_acoils extends BaseHullMod {

    public static final float FLUX_USE_MULT = 10f;
    public static final float RANGE_BONUS = 100f;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        //flux use
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -FLUX_USE_MULT);

    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

        ship.addListener(new aCoilRangeModifier());
    }

    public static class aCoilRangeModifier implements WeaponBaseRangeModifier {

        public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
            return 0;
        }
        public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
            return 1f;
        }
        public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
            if (weapon.getSlot() == null ||  weapon.getSlot().getWeaponType() != WeaponAPI.WeaponType.ENERGY) {
                return 0f;
            }
            float bonus = 0f;
            //range bonus
            if (weapon.getSlot().getSlotSize() == WeaponAPI.WeaponSize.MEDIUM) {
                bonus += RANGE_BONUS;
            }
            return bonus;
        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int)RANGE_BONUS;
        if (index == 1) return "" + (int)FLUX_USE_MULT + "%";
        return null;
    }
}