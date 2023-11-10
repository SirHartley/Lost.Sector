package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class nskr_missile_spec extends BaseHullMod {

	public static final float BIG_MAGS = 100f;
	public static final float MISSILE_HP_BONUS = 50f;
	public static final float MISSILE_ROF_PENALTY = 20f;
	public static final float MISSILE_SPEED_PENALTY = 20f;
	public static final float MISSILE_RANGE_MULT = 1.25f;
	public static final float MISSILE_ACCEL_PENALTY = 20f;
	public static final float MISSILE_RATE_PENALTY = 20f;
	public static final float MISSILE_TURN_ACCEL_PENALTY = 20f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getMissileAmmoBonus().modifyPercent(id, BIG_MAGS);
		stats.getMissileHealthBonus().modifyPercent(id, MISSILE_HP_BONUS);
		stats.getMissileRoFMult().modifyPercent(id, -1f*MISSILE_ROF_PENALTY);
		stats.getMissileMaxSpeedBonus().modifyPercent(id, -1f*MISSILE_SPEED_PENALTY);
		stats.getMissileWeaponRangeBonus().modifyMult(id, MISSILE_RANGE_MULT);
		stats.getMissileAccelerationBonus().modifyPercent(id, -1f*MISSILE_ACCEL_PENALTY);
		stats.getMissileMaxTurnRateBonus().modifyPercent(id, -1f*MISSILE_RATE_PENALTY);
		stats.getMissileTurnAccelerationBonus().modifyPercent(id, -1f*MISSILE_TURN_ACCEL_PENALTY);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) BIG_MAGS + "%";
		if (index == 1) return "" + (int) MISSILE_HP_BONUS + "%";
		if (index == 2) return "" + (int) MISSILE_SPEED_PENALTY + "%";
		return null;
	}
}
