package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;

public class nskr_bigLightMags extends BaseHullMod {

    //TODO
    //this should be an AdvanceableListener

    public static final float BIG_MAGS = 100f;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("BIGLIGHTMAGS_DATA_KEY" + ship.getId());
        if (data == null) {
            data = new ShipSpecificData();
        }

        if(!data.ammod) {
            for (WeaponAPI w : ship.getAllWeapons()) {
                if (w.getAmmo() >= Integer.MAX_VALUE) continue;
                if (w.getType()==WeaponAPI.WeaponType.DECORATIVE||w.getType()==WeaponAPI.WeaponType.STATION_MODULE||w.getType()==WeaponAPI.WeaponType.LAUNCH_BAY||w.getType()==WeaponAPI.WeaponType.SYSTEM)continue;
                if (w.getSize() != WeaponAPI.WeaponSize.SMALL) continue;

                int baseAmmo;
                if (Global.getSettings().getWeaponSpec(w.getId()) != null) {
                    baseAmmo = Global.getSettings().getWeaponSpec(w.getId()).getMaxAmmo();
                } else {
                    baseAmmo = w.getMaxAmmo();
                }
                w.setMaxAmmo(Math.round(w.getAmmo()+(baseAmmo*(BIG_MAGS/100f))));
                w.setAmmo(Math.round(w.getAmmo()+(baseAmmo*(BIG_MAGS/100f))));
            }
            data.ammod = true;
        }

        Global.getCombatEngine().getCustomData().put("BIGLIGHTMAGS_DATA_KEY" + ship.getId(), data);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) BIG_MAGS + "%";
        return null;
    }

    public static class ShipSpecificData {
        public boolean ammod = false;
    }
}
