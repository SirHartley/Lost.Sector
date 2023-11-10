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

public class nskr_fluxDumpStats extends BaseShipSystemScript {

	public static final float DISSIPATION = 2500f;
	public static final float SPEED_BONUS = 150f;
	public static final float MANUEVER_BONUS = 100f;

	public static final Color AI_COLOR = new Color(255, 40, 33, 50);
	public static final Color JITTER_COLOR = new Color(255, 166, 0, 125);

	public static final int MAX_PARTICLES_PER_FRAME = 5;
	public static final Color PARTICLE_COLOR = new Color(255, 40, 33);
	public static final float PARTICLE_OPACITY = 0.85f;
	public static final float PARTICLE_RADIUS = 130f;
	public static final float PARTICLE_SIZE = 5f;
	public static final float DURATION = 2.00f;
	private boolean doOnce = false;
	private float amount = 0f;
	public static final Vector2f ZERO = new Vector2f();
	private final IntervalUtil afterImageTimer;
	public nskr_fluxDumpStats() {
		this.afterImageTimer = new IntervalUtil(0.01f, 0.01f);
	}

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		CombatEngineAPI engine = Global.getCombatEngine();
		ShipAPI ship = (ShipAPI) stats.getEntity();

		amount += engine.getElapsedInLastFrame();
		float t = mathUtil.normalize(amount, 0f, DURATION);
		//reverse parabola
		float mult = (float)Math.pow(t-1,2);

		float so = 1f;
		//SO too op
		if (ship.getVariant().hasHullMod("safetyoverrides")) so = 0.75f;

		//engine.addFloatingText(ship.getLocation(), "test " + multi, 20f, Color.cyan, ship, 0.5f, 1.0f);
		stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS * effectLevel);
		stats.getAcceleration().modifyPercent(id, 3f * MANUEVER_BONUS * effectLevel);
		stats.getTurnAcceleration().modifyPercent(id, MANUEVER_BONUS * effectLevel);
		stats.getMaxTurnRate().modifyPercent(id, MANUEVER_BONUS * effectLevel);

		//FLUX
		stats.getFluxDissipation().modifyPercent(id, (DISSIPATION*mult)*so);
		stats.getHardFluxDissipationFraction().modifyFlat(id, 0.80f);

		//sound
		if (!doOnce) {
			Global.getSoundPlayer().playSound("nskr_fluxdump", MathUtils.getRandomNumberInRange(1.4f, 1.5f), 0.7f, ship.getLocation(), ZERO);
			doOnce=true;
		}

		//jitter
		ship.setJitterUnder(ship, JITTER_COLOR, 2f, 8, 2f);
		ship.setJitterShields(false);
		//afterimage
		this.afterImageTimer.advance(Global.getCombatEngine().getElapsedInLastFrame());
		if (this.afterImageTimer.intervalElapsed()) {
			ship.addAfterimage(AI_COLOR, 0.0f, 0.0f, ship.getVelocity().x * -0.8f, ship.getVelocity().y * -0.8f, 0.0f, 0.0f, 0.0f, 0.5f, true, true, false);
		}

		//inhale
		Vector2f particlePos, particleVel;
		int numParticlesThisFrame = Math.round((MAX_PARTICLES_PER_FRAME*mult) * (engine.getElapsedInLastFrame()*60f));
		for (int x = 0; x < numParticlesThisFrame; x++) {
			particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), PARTICLE_RADIUS);
			particleVel = Vector2f.sub(ship.getLocation(), particlePos, null);
			Global.getCombatEngine().addSmokeParticle(particlePos, particleVel, PARTICLE_SIZE, PARTICLE_OPACITY, 1f,
					PARTICLE_COLOR);
		}
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
		//reset
		doOnce=false;
		amount = 0f;

		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
		stats.getFluxDissipation().unmodify(id);
		stats.getHardFluxDissipationFraction().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("improved maneuverability", false);
		} else if (index == 1) {
			return new StatusData("top speed increased by +" + (int) SPEED_BONUS, false);
		} else if (index == 2) {
			return new StatusData("flux dissipation greatly increased", false);
		}

		return null;
	}
}
