package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.awt.*;

public class CHM_kesteven extends BaseHullMod {
    //does nothing, actual effect in nskr_comcrewsBonus
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }
    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) return "one third";
        return null;
    }
    @Override
    public Color getNameColor() {
        return new Color(240, 182, 43,255);
    }
}

