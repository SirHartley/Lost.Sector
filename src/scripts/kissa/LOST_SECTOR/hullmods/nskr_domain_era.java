package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.nskr_enigmaHullmodListener;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.ArrayList;

public class nskr_domain_era extends BaseHullMod {

	//buffs for enigma hulls

	public static final float SPEED_PENALTY = 5f;
	public static final float FLUX_BONUS = 15f;
	public static final float MORE_SUPPLIES = 2.5f;
	public static final float DEGRADE_INCREASE_PERCENT = 33f;
	public static final float EMP_REDUCTION = 25f;
	public static final float REPAIR_BONUS = 25f;
	public static final float COST_REDUCTION_L = 4;
	public static final float COST_REDUCTION_M = 2;
	public static final float COST_REDUCTION_S = 1;
	public static final float FUCK_CAPITALS = 20f;
	public static final float CR_PENALTY = 15f;

	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getAcceleration().modifyMult(id, 1f - (2f*SPEED_PENALTY) * 0.01f);
		stats.getDeceleration().modifyMult(id, 1f - (2f*SPEED_PENALTY) * 0.01f);
		stats.getTurnAcceleration().modifyMult(id, 1f - (2f*SPEED_PENALTY) * 0.01f);
		stats.getMaxTurnRate().modifyMult(id, 1f - (2f*SPEED_PENALTY) * 0.01f);
		stats.getMaxSpeed().modifyMult(id, 1f - SPEED_PENALTY * 0.01f);
		stats.getMaxSpeed().modifyFlat(id, -SPEED_PENALTY);

		stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - FLUX_BONUS * 0.01f);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - FLUX_BONUS * 0.01f);

		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);

		stats.getCRLossPerSecondPercent().modifyPercent(id, DEGRADE_INCREASE_PERCENT);
		stats.getSuppliesPerMonth().modifyMult(id, MORE_SUPPLIES);

		stats.getEmpDamageTakenMult().modifyMult(id, 1f - EMP_REDUCTION * 0.01f);

		stats.getDamageToCapital().modifyMult(id, 1f + FUCK_CAPITALS * 0.01f);

		stats.getDynamic().getMod(Stats.LARGE_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION_L);
		stats.getDynamic().getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION_L);
		stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_L);
		stats.getDynamic().getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION_M);
		stats.getDynamic().getMod(Stats.MEDIUM_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION_M);
		stats.getDynamic().getMod(Stats.MEDIUM_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_M);
		stats.getDynamic().getMod(Stats.SMALL_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION_S);
		stats.getDynamic().getMod(Stats.SMALL_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION_S);
		stats.getDynamic().getMod(Stats.SMALL_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_S);

		if (stats.getVariant().hasHullMod(HullMods.SAFETYOVERRIDES)){
			stats.getMaxCombatReadiness().modifyFlat(id, -CR_PENALTY * 0.01f);
		}
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

	}


	public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 12.0f;
		Color tc = Misc.getHighlightColor();
		Color y = Misc.getHighlightColor();
		Color bad = util.TT_ORANGE;

		//SO penalty
		if (ship.getMutableStats().getVariant().hasHullMod(HullMods.SAFETYOVERRIDES)) {
			tooltip.addSectionHeading("Warning", Alignment.MID, pad);

			tooltip.addPara("-Safety Overrides installed, performance decreased", pad, bad, "");
			tooltip.addPara("-Maximum combat readiness of the ship is decreased by " + (int) (CR_PENALTY) + "%" + "%", 2.0f, bad, (int) (CR_PENALTY) + "%");
		}

		if (Global.getSector()==null) return;
		if (Global.getSector().getPlayerFleet()==null) return;

		nskr_enigmaHullmodListener.update();

		ArrayList<String> unlocks = nskr_enigmaHullmodListener.getUnlocks(nskr_enigmaHullmodListener.UNLOCKS_MEM_KEY);
		//everything unlocked
		if (unlocks.size() < nskr_enigmaHullmodListener.DEFAULT_KEYS.size()){
			tooltip.addSectionHeading("Details", Alignment.MID, pad);

			tooltip.addPara("More information can be acquired if hulls of this make are deployed in battle, the more hulls you deploy the faster the results will appear.", pad, tc, "");
		}

		//tooltip.addPara("", 0.0f, tc, "");
		if (unlocks.size()>0){
			tooltip.addSectionHeading("Stats", Alignment.MID, pad);
		}

		if (unlocks.contains(nskr_enigmaHullmodListener.KEY_BASE+"#1")) {
			tooltip.addPara("-Supply use for maintenance increased by "+"150"+"%"+"%", pad, bad, "150"+"%");
		}
		if (unlocks.contains(nskr_enigmaHullmodListener.KEY_BASE+"#5")) {
			tooltip.addPara("-Combat readiness degrades "+(int)(DEGRADE_INCREASE_PERCENT)+"%"+"% faster in combat", 2.0f, bad, (int)(DEGRADE_INCREASE_PERCENT)+"%");
		}
		if (unlocks.contains(nskr_enigmaHullmodListener.KEY_BASE+"#2")) {
			tooltip.addPara("-Maneuverability is reduced by "+(int)(SPEED_PENALTY*2f)+"%"+"%", 2.0f, bad, (int)(SPEED_PENALTY*2f)+"%");
		}
		if (unlocks.contains(nskr_enigmaHullmodListener.KEY_BASE+"#3")) {
			tooltip.addPara("-Top speed is reduced both by "+(int)(SPEED_PENALTY)+"%% " +
					"and by an additional "+(int)(SPEED_PENALTY)+"su/s flat", 2.0f, bad,  (int)(SPEED_PENALTY)+"%", (int)(SPEED_PENALTY)+"su/s");
		}
		//if (unlocks.contains(nskr_enigmaHullmodListener.KEY_BASE+"#4")) {
		//	tooltip.addPara("-Top speed is reduced by an additional "+(int)(SPEED_PENALTY)+"%"+"%", 2.0f, bad, (int)(SPEED_PENALTY)+"%");
		//}
		if (unlocks.contains(nskr_enigmaHullmodListener.KEY_BASE+"#6")) {
			tooltip.addPara("-Non-missile weapon flux use reduced by "+(int)(FLUX_BONUS)+"%"+"%", 2.0f, y, (int)(FLUX_BONUS)+"%");
		}
		if (unlocks.contains(nskr_enigmaHullmodListener.KEY_BASE+"#7")) {
			tooltip.addPara("-Weapon and engine repair speed increased by "+(int)(REPAIR_BONUS)+"%"+"%", 2.0f, y, (int)(REPAIR_BONUS)+"%");
		}
		if (unlocks.contains(nskr_enigmaHullmodListener.KEY_BASE+"#8")) {
			tooltip.addPara("-EMP damage taken reduced by "+(int)(EMP_REDUCTION)+"%"+"%", 2.0f, y, (int)(EMP_REDUCTION)+"%");
		}
		if (unlocks.contains(nskr_enigmaHullmodListener.KEY_BASE+"#9")) {
			tooltip.addPara("-Damage against Capital-class vessels increased by "+(int)(FUCK_CAPITALS)+"%"+"%", 2.0f, y, (int)(FUCK_CAPITALS)+"%");
		}
		if (unlocks.contains(nskr_enigmaHullmodListener.KEY_BASE+"#10")) {
			tooltip.addPara("-Weapon ordnance point cost reduced by "+"1/2/4"+" points based on size", 2.0f, y, "1/2/4");
		}

		//Final Overdrive
		if (unlocks.size()>=nskr_enigmaHullmodListener.DEFAULT_KEYS.size()){
			tooltip.addSectionHeading("Final Overdrive", Alignment.MID, pad);

			tooltip.addPara("Upon receiving damage that would take the ship below 33%% of maximum hull, it activates an emergency overdrive that grants a second wind for the ship. " +
					"Does not activate while overloaded or venting, ship can not be destroyed before activation otherwise.", pad, y, "");
			tooltip.addPara("Regenerates "+(int)nskr_rage_mod.HULL_REPAIR_PERCENT+"%% of maximum hull and recover every armor cell to 75%% if below that value. " +
					"Dissipate 50%% of current flux, cruiser size hulls are phased for "+(int)nskr_rage_mod.MAX_DISAPPEAR_TIME+" seconds. " +
					"Lose "+(int)Math.abs(nskr_rage_mod.ENRAGE_PPT_PENALTY)+"%% of remaining peak performance time.", pad, y, "");
			tooltip.addPara("Gain major improvements to many stats", pad, y, "");

			tooltip.addPara("-Top speed is increased both by "+(int)(nskr_rage_mod.ENRAGE_SPEED_BONUS)+"%% " +
					"and by an additional "+(int)(nskr_rage_mod.ENRAGE_SPEED_BONUS_FLAT)+"su/s flat ", pad, y,  (int)(nskr_rage_mod.ENRAGE_SPEED_BONUS)+"%", (int)(nskr_rage_mod.ENRAGE_SPEED_BONUS_FLAT)+"su/s");
			tooltip.addPara("-Ballistic weapon rate of fire increased and flux use reduced by "+(int)(nskr_rage_mod.ENRAGE_BALLISTIC_ROF_BONUS)+"%"+"%", 2.0f, y, (int)(nskr_rage_mod.ENRAGE_BALLISTIC_ROF_BONUS)+"%");
			tooltip.addPara("-Energy weapon damage increased by "+(int)(nskr_rage_mod.ENRAGE_ENERGY_DMG_BONUS)+"%"+"%", 2.0f, y, (int)(nskr_rage_mod.ENRAGE_ENERGY_DMG_BONUS)+"%");
			tooltip.addPara("-Flux dissipation increased by "+(int)(nskr_rage_mod.ENRAGE_FLUX_MULTI_BONUS)+"%"+"%", 2.0f, y, (int)(nskr_rage_mod.ENRAGE_FLUX_MULTI_BONUS)+"%");
			tooltip.addPara("-Shield unfold and turn rate increased by "+(int)(nskr_rage_mod.SHIELD_SPEED_BONUS)+"%"+"%", 2.0f, y, (int)(nskr_rage_mod.SHIELD_SPEED_BONUS)+"%");
		}

	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) SPEED_PENALTY + "%";
		return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return true;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

	@Override
	public Color getNameColor() {
		return new Color(129, 227, 149,255);
	}
}

