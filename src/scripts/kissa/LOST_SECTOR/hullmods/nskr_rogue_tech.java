package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

import java.awt.*;

public class nskr_rogue_tech extends BaseHullMod {

	public static final float RANGE_BONUS = 100f;
	public static final float ROF_BONUS = 20f;
	public static final float FLUX_USE = 40f;

	public static String MOD_ICON = "graphics/icons/hullsys/emp_emitter.png";
	public static String MOD_BUFFID = "nskr_rogue_tech";
	public static String MOD_NAME = "Flux Discharge Anomaly";

	public static final Color LIGHTNING_CORE_COLOR = new Color(195, 84, 255, 150);
	public static final Color LIGHTNING_FRINGE_COLOR = new Color(219, 177, 39, 250);
	public static final Color JITTER_UNDER_COLOR = new Color(222, 145, 44, 250);

	private final IntervalUtil arcInterval = new IntervalUtil(1,1);

	@Override
	public boolean isApplicableToShip (ShipAPI ship){
		return false;
	}

	@Override
	public void advanceInCombat (ShipAPI ship,float amount) {
		if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
			return;
		}

		float fluxRatio = ship.getFluxTracker().getFluxLevel();
		float zapFx = 2.5f - (2.4f * (fluxRatio));
		float fluxUse = -FLUX_USE * fluxRatio;

		ship.getMutableStats().getEnergyWeaponRangeBonus().modifyFlat("nskr_rogue_tech", RANGE_BONUS);
		ship.getMutableStats().getEnergyRoFMult().modifyPercent("nskr_rogue_tech", ROF_BONUS);
		ship.getMutableStats().getEnergyWeaponFluxCostMod().modifyPercent("nskr_rogue_tech", fluxUse);

		float fluxuseTt = Math.round(fluxUse);

		if (ship == Global.getCombatEngine().getPlayerShip() && fluxRatio>0f) {
			Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, "flux use decreased by " + (int)fluxuseTt + "%", true);
		}

		//FX
		ship.setJitterUnder("nskr_rogue_tech", JITTER_UNDER_COLOR, 9f, 9, 0.55f);
		ship.setJitterShields(false);

		arcInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
		if (arcInterval.intervalElapsed()) {
			arcInterval.setInterval(zapFx,zapFx);
			for (int x = 0; x < 4; x++) {
				Vector2f particlePos, particlePosTo;
				float radius = (float)Math.random()*150f+(ship.getCollisionRadius()-50f);
				particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), radius);
				float angle = VectorUtils.getAngle(ship.getLocation(),particlePos);
				particlePosTo = MathUtils.getPointOnCircumference(ship.getLocation(), radius, angle+ mathUtil.getRandomNumberInRangeExcludingRange(-40f, 40f,-10f,10f));

				Global.getCombatEngine().spawnEmpArcVisual(particlePos, new SimpleEntity(particlePos), particlePosTo, new SimpleEntity(particlePosTo),
						5+(35*fluxRatio), // thickness of the lightning bolt
						LIGHTNING_CORE_COLOR, //Central color
						LIGHTNING_FRINGE_COLOR //Fringe Color
				);
			}
		}
	}

	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

		ship.addListener(new rogueRangeModifier());
	}

	public static class rogueRangeModifier implements WeaponBaseRangeModifier {
		public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			return 0;
		}
		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1f;
		}
		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			if (weapon.getSlot() == null ||  weapon.getSlot().getWeaponType() != WeaponAPI.WeaponType.ENERGY || weapon.getSlot().getSlotSize() == WeaponAPI.WeaponSize.LARGE) {
				return 0f;
			}
			return RANGE_BONUS;
		}
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + Math.round(RANGE_BONUS);
		if (index == 1) return "" + Math.round(ROF_BONUS) + "%";
		if (index == 2) return "" + Math.round(FLUX_USE) + "%";
		if (index == 3) return "" + 0 + "%";
		return null;
	}

	@Override
	public Color getNameColor() {
		return new Color(220, 156, 61,255);
	}
}

