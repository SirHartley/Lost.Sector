package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class nskr_missileSalvoStats extends BaseShipSystemScript {

	public static final float MISSILES = 6f;

	public static final Color JITTER_COLOR = new Color(255, 155, 255, 75);
	public static final Color JITTER_UNDER_COLOR = new Color(255, 155, 255, 155);
	boolean missile = false;
	private boolean updated = false;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}

		float jitterLevel = effectLevel;
		if (state == State.OUT) {
			jitterLevel *= jitterLevel;
		}
		float maxRangeBonus = 25f;
		float jitterRangeBonus = jitterLevel * maxRangeBonus;

		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 11, 0f, 3f + jitterRangeBonus);
		ship.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0f, 0 + jitterRangeBonus);

		if (!updated) {
			missile = false;

			updated = true;
		}

		if (state == State.ACTIVE && !missile) {

			for (int x = 0; x < MISSILES; x++) {
				Vector2f point = ship.getLocation();
				float angle = (float) Math.random() * 360f;
				float distance = (float) Math.random() * 25f + 150f;

				Vector2f missileLocation = MathUtils.getPointOnCircumference(point, distance, angle);

				spawnMissile(ship, missileLocation);
			}
				missile = true;
		}
	}

	public void unapply(MutableShipStatsAPI stats, String id) {

		updated = false;
	}

	public void spawnMissile(ShipAPI source, Vector2f missileLoc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		Vector2f currLoc = Misc.getPointAtRadius(missileLoc, 30f + (float) Math.random() * 30f);
		float start = (float) Math.random() * 360f;
		for (float angle = start; angle < start + 390; angle += 30f) {
			if (angle != start) {
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
				loc.scale(50f + (float) Math.random() * 30f);
				currLoc = Vector2f.add(missileLoc, loc, new Vector2f());
			}
			for (MissileAPI other : Global.getCombatEngine().getMissiles()) {
				if (!other.isMine()) continue;

				float dist = 0;
				if (currLoc != null) {
					dist = Misc.getDistance(currLoc, other.getLocation());
				}
				if (dist < other.getCollisionRadius() + 40f) {
					currLoc = null;
					break;
				}
			}
			if (currLoc != null) {
				break;
			}
		}
		if (currLoc == null) {
			currLoc = Misc.getPointAtRadius(missileLoc, 30f + (float) Math.random() * 30f);
		}

		MissileAPI mine = (MissileAPI) engine.spawnProjectile(source, null,
				"nskr_msalvo",
				currLoc,
				(float) Math.random() * 360f, null);
		if (source != null) {
			Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(
					source, WeaponAPI.WeaponType.MISSILE, false, mine.getDamage());
		}
		Global.getSoundPlayer().playSound("nskr_msalvo_fire", 1f, 1.00f, mine.getLocation(), mine.getVelocity());
	}
}

