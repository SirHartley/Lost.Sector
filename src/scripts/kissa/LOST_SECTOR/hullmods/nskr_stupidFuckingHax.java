package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.awt.*;

public class nskr_stupidFuckingHax extends BaseHullMod {

	//does nothing lol
	//script for EM core

	Color lol = new Color(255, 20, 145);

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

	}

	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
			return;
		}

	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "";
		return null;
	}

}
