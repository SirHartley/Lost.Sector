package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

public class nskr_multi_phased extends BaseHullMod {

    public static final float FLUX_USE_MULT_BASE = 10f;
    public static final float FLUX_USE_MULT_MAX = 50f;

    public static final String MOD_ICON = "graphics/icons/hullsys/high_energy_focus.png";
    public static final String MOD_BUFFID = "nskr_multi_phased";
    public static final String MOD_NAME = "Multi-Phased Weapon Coils";

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
            return;
        }

        MutableShipStatsAPI stats = ship.getMutableStats();
        String id = "nskr_multi_phased";

        float fluxRatio = ship.getFluxTracker().getFluxLevel();
        float fluxBonus = mathUtil.lerp(0f,FLUX_USE_MULT_MAX - FLUX_USE_MULT_BASE, fluxRatio);

        //flux use
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -FLUX_USE_MULT_BASE - fluxBonus);
        stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -FLUX_USE_MULT_BASE - fluxBonus);
        stats.getMissileWeaponFluxCostMod().modifyPercent(id, -FLUX_USE_MULT_BASE - fluxBonus);

        //tooltip
        if (ship == Global.getCombatEngine().getPlayerShip() && fluxRatio>0f) {
            Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, "Weapon flux use "+(int)(-FLUX_USE_MULT_BASE - fluxBonus)+"%", false);
        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) FLUX_USE_MULT_BASE + "%";
        if (index == 1) return "" + (int) FLUX_USE_MULT_MAX + "%";
        return null;
    }
}
