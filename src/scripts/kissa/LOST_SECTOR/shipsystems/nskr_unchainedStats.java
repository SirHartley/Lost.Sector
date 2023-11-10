package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class nskr_unchainedStats extends BaseShipSystemScript {

	public static final float ROF_BONUS = 0.75f;
	public static final float FLUX_REDUCTION = 42.8572f;
	public static final float SPEED_BONUS = 25f;
	public static final float MANUEVER_BONUS = 50f;
	public static final float DECELERATION_PENALTY = 65f;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
			stats.getMaxTurnRate().unmodify(id);
		} else {
			stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
			stats.getAcceleration().modifyPercent(id, MANUEVER_BONUS * effectLevel);
			stats.getDeceleration().modifyPercent(id, -DECELERATION_PENALTY * effectLevel);
			stats.getTurnAcceleration().modifyPercent(id, MANUEVER_BONUS * effectLevel);
			stats.getMaxTurnRate().modifyPercent(id, MANUEVER_BONUS);

			float mult = 1f + (ROF_BONUS * effectLevel);
			stats.getBallisticRoFMult().modifyMult(id, mult);
			stats.getEnergyRoFMult().modifyMult(id, mult);
			stats.getMissileRoFMult().modifyMult(id, mult);
			stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);
			stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);
			stats.getMissileWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);

		}
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + ROF_BONUS * effectLevel;
		float bonusPercent = (int) ((mult - 1f) * 100f);
		if (index == 0) {
			return new StatusData("improved maneuverability", false);
		} else if (index == 1) {
			return new StatusData("top speed increased by +" + (int) SPEED_BONUS, false);
		}		
		if (index == 2) {
			return new StatusData("weapon rate of fire +" + (int) bonusPercent + "%", false);
		}
		if (index == 3) {
			return new StatusData("weapon flux use -" + (int) FLUX_REDUCTION + "%", false);
		}
		return null;
	}
}
