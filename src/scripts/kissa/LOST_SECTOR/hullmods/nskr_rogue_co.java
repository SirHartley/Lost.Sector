package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

import static com.fs.starfarer.api.impl.campaign.RepairGantry.getAdjustedGantryModifier;
import static com.fs.starfarer.api.impl.campaign.RepairGantry.getAdjustedGantryModifierForPostCombatSalvage;

public class nskr_rogue_co extends BaseHullMod {

	//campaign effect stuff

	public static final float BATTLE_SALVAGE_MULT = .15f;
	public static final float SUPPLIES = 0.33f;
	public static final float BATTLE_SALVAGE_MULT2 = .2f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSuppliesPerMonth().modifyPercent(id, SUPPLIES * 100f);
		stats.getDynamic().getMod(Stats.SALVAGE_VALUE_MULT_MOD).modifyFlat(id, BATTLE_SALVAGE_MULT);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 2) return "" + Math.round(SUPPLIES * 100f) + "%";
		if (index == 1) return "" + Math.round(BATTLE_SALVAGE_MULT2 * 100f) + "%";
		if (index == 0) return "" + Math.round(BATTLE_SALVAGE_MULT * 100f) + "%";

		return null;
	}

	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();

		tooltip.addPara("Each additional ship provides diminishing returns.", opad);

		if (isForModSpec || ship == null) return;
		if (Global.getSettings().getCurrentState() == GameState.TITLE) return;

		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		float fleetMod = getAdjustedGantryModifier(fleet, null, 0f);

		tooltip.addPara("The total resource recovery bonus for your fleet is %s.", opad, h,
				"" + Math.round(fleetMod * 100f) + "%");

		tooltip.addPara("The fleetwide post-battle salvage bonus is %s.", opad, h,
				"" + Math.round(getAdjustedGantryModifierForPostCombatSalvage(fleet) * 100f) + "%");
	}

	@Override
	public Color getNameColor() {
		return new Color(220, 156, 61,255);
	}
}

