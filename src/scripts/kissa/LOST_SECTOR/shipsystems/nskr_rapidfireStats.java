package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.util.ArrayList;
import java.util.List;

public class nskr_rapidfireStats extends BaseShipSystemScript {

	public static final float ROF_BONUS = 2f;
	public static final float FLUX_REDUCTION = 50f;
	private float counter = 0f;
	private boolean runOnce = false;
	private List<WeaponAPI> weapons=new ArrayList<>();

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		CombatEngineAPI engine = Global.getCombatEngine();
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else return;
		if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
			return;
		}
		if(!runOnce){
			weapons=ship.getAllWeapons();
			runOnce=true;
		}
		float amount = engine.getElapsedInLastFrame();
		counter += amount;
		//random malfunctions
		if (counter>0.1) {
			if (weapons != null) {
				for (WeaponAPI w : weapons) {
					if (w.getType()== WeaponAPI.WeaponType.MISSILE) continue;
					if (Math.random()>0.99)w.setCurrHealth(-50);
				}
			counter = 0f;
			}
		}

		float mult = 1f + ROF_BONUS * effectLevel;
		stats.getBallisticRoFMult().modifyMult(id, mult);
		stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);



	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
	}

	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + ROF_BONUS * effectLevel;
		float bonusPercent = (int) ((mult - 1f) * 100f);
		if (index == 0) {
			return new StatusData("ballistic rate of fire +" + (int) bonusPercent + "%", false);
		}
		if (index == 1) {
			return new StatusData("ballistic flux use -" + (int) FLUX_REDUCTION + "%", false);
		}
		return null;
	}
}
