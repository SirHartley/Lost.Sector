package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

import java.awt.*;

public class nskr_energyPrecisionStats extends BaseShipSystemScript {

	public static final float SPEED_BONUS = 50f;
	public static final float RANGE_BONUS = 250f;
	public static final Color PARTICLE_COLOR = new Color(91, 136, 255, 105);
	public static final Color JITTER_COLOR = new Color(91, 136, 255, 105);
	private final IntervalUtil sparkleInterval = new IntervalUtil(0.10f, 0.15f);

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
			return;
		}

		CombatEngineAPI engine = Global.getCombatEngine();

			stats.getEnergyWeaponRangeBonus().modifyFlat(id, RANGE_BONUS * effectLevel);
			stats.getProjectileSpeedMult().modifyPercent(id, SPEED_BONUS * effectLevel);

		ship.setJitterUnder(this, JITTER_COLOR, 1f, 20, 1f, 5f);
		ship.setJitterShields(false);

		sparkleInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
		if (sparkleInterval.intervalElapsed()) {
			for (int x = 0; x < 3; x++) {
				Vector2f sPoint = ship.getLocation();
				float angle = (float) Math.random() * 360f;
				float distance = ((float) Math.random() * 200f) * effectLevel;
				float bias = mathUtil.BiasFunction(Math.random(),0.2f);
				distance *= bias;
				distance += 75f;

				Vector2f point = MathUtils.getPointOnCircumference(sPoint, distance, angle);

				Vector2f particleVel = Vector2f.sub(ship.getLocation(), point, null);

				engine.addSmoothParticle(
						point,
						particleVel,
						MathUtils.getRandomNumberInRange(20, 30),
						0.25f,
						1.00f,
						PARTICLE_COLOR
				);
			}
		}
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponRangeBonus().unmodify(id);
		stats.getProjectileSpeedMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("improved energy weapon range by +" + (int) RANGE_BONUS, false);
		} else if (index == 1) {
			return new StatusData("projectile velocity increased by +" + (int) SPEED_BONUS+"%%", false);
		}
		return null;
	}
}
