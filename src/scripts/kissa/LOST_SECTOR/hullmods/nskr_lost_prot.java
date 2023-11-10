package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.awt.*;

public class nskr_lost_prot extends BaseHullMod {

	public static final float SUPPLY_USE_MULT = 2f;
	public static final float DEGRADE_INCREASE_PERCENT = 50f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSuppliesPerMonth().modifyMult(id, SUPPLY_USE_MULT);
		stats.getCRLossPerSecondPercent().modifyPercent(id, DEGRADE_INCREASE_PERCENT);

	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)((SUPPLY_USE_MULT - 1f) * 100f) + "%";
		if (index == 1) return "" + (int) DEGRADE_INCREASE_PERCENT + "%";
		return null;
	}

	@Override
	public Color getNameColor() {
		return new Color(83, 106, 237,255);
	}
}
