package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicIncompatibleHullmods;

import java.util.HashSet;
import java.util.Set;

public class nskr_aed extends BaseHullMod {

	//some hax to make AED slightly less fucky and Data for FX

	public static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
	static {
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "";

		return null;
	}


	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return false;
	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
			return;
		}

		ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("KABOOM_DATA_KEY" + ship.getId());
		if (data == null) {
			data = new ShipSpecificData();
		}

		if (ship.getAI() != null) {

			if (ship.getCaptain() != null) {
				ship.getCaptain().setPersonality("reckless");

			}
		}

		Global.getCombatEngine().getCustomData().put("KABOOM_DATA_KEY" + ship.getId(), data);
	}
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getBreakProb().modifyFlat(id,1f);

	}
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		//explosion
		if (ship.getHullSize()!= HullSize.FIGHTER) {
			if (!ship.hasListenerOfClass(nskr_protExplosion.nskr_protExplosionListener.class)) {
				ship.addListener(new nskr_protExplosion.nskr_protExplosionListener(ship));
			}
		}

		for (String tmp : BLOCKED_HULLMODS) {
			if (ship.getVariant().getHullMods().contains(tmp)) {

					//if someone tries to install blocked hullmod, remove it
					MagicIncompatibleHullmods.removeHullmodWithWarning(
							ship.getVariant(),
							tmp,
							"nskr_aed"
					);

			}
		}
	}

	public static class ShipSpecificData {
		public boolean remove = false;
		public boolean kaboom;
		public boolean doOnce;
		public Vector2f kLoc;
		public float timer;
	}
}