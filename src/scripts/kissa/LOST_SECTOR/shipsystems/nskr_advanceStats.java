package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Pair;
import org.magiclib.util.MagicRender;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.dark.shaders.post.PostProcessShader;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class nskr_advanceStats extends BaseShipSystemScript {

	public static final float STAT_BONUS = 20f;
	public static final Color JITTER_COLOR = new Color(255, 183, 0, 255);
	public static final Color LIGHTNING_CORE_COLOR = new Color(255, 84, 135, 255);
	public static final Color LIGHTNING_FRINGE_COLOR = new Color(39, 102, 219, 255);
	public static final Color AI_COLOR = new Color(255, 59, 33, 50);
	public static final Color SPRITE_D = new Color(146, 153, 204, 255);
	public static final Color SPRITE_C = new Color(65, 144, 208, 255);
	public static final Color SPRITE_B = new Color(84, 189, 89, 255);
	public static final Color SPRITE_A = new Color(255, 216, 60, 255);
	public static final Color SPRITE_S = new Color(232, 26, 50, 255);
	public static final int MAX_PARTICLES_PER_FRAME = 2;
	public static final Color PARTICLE_COLOR = new Color(255, 74, 33);
	public static final float PARTICLE_OPACITY = 0.75f;
	public static float PARTICLE_RADIUS = 250f;
	public static final float PARTICLE_SIZE = 4f;
	public static final float DISTORTION_BLAST_RADIUS = 200f;
	public static final Vector2f ZERO = new Vector2f();
	private int dState = 1;
	private int grade = 0;
	private boolean grade1 = false;
	private boolean grade2 = false;
	private boolean grade3 = false;
	private boolean grade4 = false;
	private boolean grade5 = false;
	private boolean state2 = false;
	private boolean state3 = false;
	private boolean doOnce = false;
	private final SpriteAPI gradeD = null;
	private final SpriteAPI gradeC = null;
	private final SpriteAPI gradeB = null;
	private final SpriteAPI gradeA = null;
	private final SpriteAPI gradeS = null;

	private final List<Pair<SpriteAPI, String>> sprites = new ArrayList<>();
	{
		sprites.add(0, new Pair<>(gradeD,"graphics/fx/nskr_d.png"));
		sprites.add(1, new Pair<>(gradeC,"graphics/fx/nskr_c.png"));
		sprites.add(2, new Pair<>(gradeB,"graphics/fx/nskr_b.png"));
		sprites.add(3, new Pair<>(gradeA,"graphics/fx/nskr_a.png"));
		sprites.add(4, new Pair<>(gradeS,"graphics/fx/nskr_s.png"));
	}
	private boolean updated = false;
	private WaveDistortion wave;
	private WaveDistortion wave2;
	private final IntervalUtil afterImageTimer;
	public nskr_advanceStats() {
		this.afterImageTimer = new IntervalUtil(0.01f, 0.01f);
	}

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine == null) {
			return;
		}
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
			return;
		}

		ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("ADVANCE_DATA_KEY" + ship.getId());
		if (data == null) {
			data = new ShipSpecificData();
			doOnce = false;
		}

		if (!doOnce) {
			for (Pair<SpriteAPI, String> sprite : sprites) {
				if (sprite.one == null){
					try {
						Global.getSettings().loadTexture(sprite.two);
					} catch (IOException ex) {
						throw new RuntimeException("Failed to load sprite '" + sprite + "'!", ex);
					}
					sprite.one = Global.getSettings().getSprite(sprite.two);
				}
			}

			ship.addListener(new damageCounter());
			//engine.addFloatingText(ship.getLocation(), "lol", 600, Color.RED, ship, 0.5f, 1.0f);
			doOnce=true;
		}

		//stages
		if (data.damageD>8000f){
			dState = 2;
		} if (data.damageD>20000f) {
			dState = 3;
		}
		//cosmetic stages
		if (data.damageD>4000f){
			grade = 1;
		} if (data.damageD>8000f) {
			grade = 2;
		} if (data.damageD>12000f) {
			grade = 3;
		} if (data.damageD>16000f) {
			grade = 4;
		} if (data.damageD>20000f) {
			grade = 5;
		}


		if (!updated) {
			//reset
			data.damageD = 0f;
			dState = 1;
			grade = 0;
			state2 = false;
			state3 = false;
			grade1 = false;
			grade2 = false;
			grade3 = false;
			grade4 = false;
			grade5 = false;

			updated = true;
		}

		if (state == State.ACTIVE || state == State.OUT) {
			//STAGE1
			if (dState>0) {
				stats.getMaxSpeed().modifyFlat(id, (STAT_BONUS * dState) * effectLevel);
				stats.getAcceleration().modifyPercent(id, (STAT_BONUS * dState) * effectLevel);
				stats.getMaxTurnRate().modifyPercent(id, (STAT_BONUS * dState) * effectLevel);

				//jitter fx
				ship.setJitterUnder(ship, JITTER_COLOR, 1.5f, 10, 5f);
				ship.setJitterShields(false);
			}
			//STAGE2
			if (dState>1) {
				stats.getBallisticRoFMult().modifyPercent(id, (STAT_BONUS * dState) * effectLevel);
				stats.getBallisticWeaponFluxCostMod().modifyPercent(id, (-STAT_BONUS * dState) * effectLevel);
				stats.getEnergyWeaponDamageMult().modifyPercent(id, (STAT_BONUS * dState) * effectLevel);
				stats.getEnergyWeaponFluxCostMod().modifyPercent(id, (-STAT_BONUS * dState) * effectLevel);

				//inhale (modified from cycerin)
				Vector2f particlePos, particleVel;
				int numParticlesThisFrame = Math.round(MAX_PARTICLES_PER_FRAME * (engine.getElapsedInLastFrame()*60f));
				for (int x = 0; x < numParticlesThisFrame; x++) {
					if (dState>2) PARTICLE_RADIUS = 100f;
					particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), PARTICLE_RADIUS);
					//flip on dir next stage
					if (dState==2) {
						particleVel = Vector2f.sub(ship.getLocation(), particlePos, null);
					} else particleVel = Vector2f.sub(particlePos, ship.getLocation(), null);
					Global.getCombatEngine().addSmokeParticle(particlePos, particleVel, PARTICLE_SIZE, PARTICLE_OPACITY, 1f, PARTICLE_COLOR);
				}
				//sound & emp
				if(!state2){
					Global.getSoundPlayer().playSound("nskr_takedown", MathUtils.getRandomNumberInRange(0.8f, 1.0f), 1.5f, ship.getLocation(), ZERO);
					//distortion fx
					wave = new WaveDistortion();
					wave.setLocation(ship.getLocation());
					wave.setSize(DISTORTION_BLAST_RADIUS * dState);
					wave.setIntensity(DISTORTION_BLAST_RADIUS * 0.05f * dState);
					wave.fadeInSize(0.5f);
					wave.fadeOutIntensity(0.5f);
					wave.setSize(DISTORTION_BLAST_RADIUS * 0.35f * dState);
					DistortionShader.addDistortion(wave);

					for (int x = 0; x < 12; x++) {
						float angle = (float) Math.random() * 360f;
						float distance = (float) Math.random() * 150f + 50f;
						float angle2 = angle*MathUtils.getRandomNumberInRange(0.25f,1.0f);

						Vector2f point1 = MathUtils.getPointOnCircumference(ship.getLocation(), distance, angle);
						Vector2f point2 = MathUtils.getPointOnCircumference(ship.getLocation(), distance, angle2);
						if (ship != null) {
							Global.getCombatEngine().spawnEmpArcVisual(point1, ship, point2, ship,
									10f*dState, // thickness of the lightning bolt
									LIGHTNING_CORE_COLOR, //Central color
									LIGHTNING_FRINGE_COLOR //Fringe Color
							);
						}
					}
					state2 = true;
				}
			}
			//STAGE3
			if (dState>2) {
				stats.getBallisticWeaponDamageMult().modifyPercent(id, (STAT_BONUS * dState) * effectLevel);
				stats.getBallisticProjectileSpeedMult().modifyPercent(id, (STAT_BONUS * dState) * effectLevel);
				stats.getEnergyWeaponDamageMult().modifyPercent(id, (STAT_BONUS * dState) * effectLevel);
				stats.getEnergyProjectileSpeedMult().modifyPercent(id, (STAT_BONUS * dState) * effectLevel);

				//afterimage
				this.afterImageTimer.advance(Global.getCombatEngine().getElapsedInLastFrame());
				if (this.afterImageTimer.intervalElapsed()) {
					ship.addAfterimage(AI_COLOR, 0.0f, 0.0f, ship.getVelocity().x * -0.8f, ship.getVelocity().y * -0.8f, 0.0f, 0.0f, 0.0f, 1.0f, true, true, false);
				}
				//sound & emp
				if(!state3){
					if (ship == engine.getPlayerShip()) {

					}
					Global.getSoundPlayer().playSound("nskr_takedown", MathUtils.getRandomNumberInRange(1.0f, 1.2f), 2.0f, ship.getLocation(), ZERO);
					//distortion fx
					wave2 = new WaveDistortion();
					wave2.setLocation(ship.getLocation());
					wave2.setSize(DISTORTION_BLAST_RADIUS * dState);
					wave2.setIntensity(DISTORTION_BLAST_RADIUS * 0.05f * dState);
					wave2.fadeInSize(0.5f);
					wave2.fadeOutIntensity(0.5f);
					wave2.setSize(DISTORTION_BLAST_RADIUS * 0.35f * dState);
					DistortionShader.addDistortion(wave2);

					for (int x = 0; x < 18; x++) {
						float angle = (float) Math.random() * 360f;
						float distance = (float) Math.random() * 200f + 50f;
						float angle2 = angle*MathUtils.getRandomNumberInRange(0.25f,1.0f);

						Vector2f point1 = MathUtils.getPointOnCircumference(ship.getLocation(), distance, angle);
						Vector2f point2 = MathUtils.getPointOnCircumference(ship.getLocation(), distance, angle2);
						if (ship != null) {
							Global.getCombatEngine().spawnEmpArcVisual(point1, ship, point2, ship,
									10f*dState, // thickness of the lightning bolt
									LIGHTNING_CORE_COLOR, //Central color
									LIGHTNING_FRINGE_COLOR //Fringe Color
							);
						}
					}
					state3 = true;
				}
			}
		//Grade graphics
		if (ship == engine.getPlayerShip()) {
			if (dState>2) {
				//shader fx
				PostProcessShader.setNoise(false, mathUtil.lerp(0f, 0.15f, effectLevel));
				PostProcessShader.setLightness(false, mathUtil.lerp(1f, 1.15f, effectLevel));
				PostProcessShader.setSaturation(false, mathUtil.lerp(1f, 0.75f, effectLevel));
			}

			float sizef = 24f;
			Vector2f size = new Vector2f(sizef*2f,sizef*2f);
			float duration = 0.50f;
			float durationOut = 0.15f;
			String sId = "nskr_level_up";
			float volume = 1.20f;

			Vector2f dir = new Vector2f();
			dir.setX((float)Math.random()*ship.getVelocity().getX());
			if (Math.random()>0.5)dir.setX(-dir.getX());
			dir.setY((float)Math.random()*ship.getVelocity().getY());
			if (Math.random()>0.5)dir.setY(-dir.getY());

			if (grade > 0) {
				SpriteAPI sprite;
				sprite = sprites.get(0).one;
				if (!grade1) {
					MagicRender.objectspace(sprite, ship, ZERO, dir, size, ZERO, 270, 0, false, SPRITE_D,
							false, 0f, duration, durationOut, false);
					Global.getSoundPlayer().playSound(sId, MathUtils.getRandomNumberInRange(0.8f, 0.9f), volume, ship.getLocation(), ZERO);
					grade1 = true;
				}
			}
			if (grade > 1) {
				SpriteAPI sprite;
				sprite = sprites.get(1).one;
				if (!grade2) {
					MagicRender.objectspace(sprite, ship, ZERO, dir, size, ZERO, 270, 0, false, SPRITE_C,
							false, 0f, duration, durationOut, false);
					Global.getSoundPlayer().playSound(sId, MathUtils.getRandomNumberInRange(1.0f, 1.1f), volume, ship.getLocation(), ZERO);
					grade2 = true;
				}
			}
			if (grade > 2) {
				SpriteAPI sprite;
				sprite = sprites.get(2).one;
				if (!grade3) {
					MagicRender.objectspace(sprite, ship, ZERO, dir, size, ZERO, 270, 0, false, SPRITE_B,
							false, 0f, duration, durationOut, false);
					Global.getSoundPlayer().playSound(sId, MathUtils.getRandomNumberInRange(1.2f, 1.3f), volume, ship.getLocation(), ZERO);
					grade3 = true;
				}
			}
			if (grade > 3) {
				SpriteAPI sprite;
				sprite = sprites.get(3).one;
				if (!grade4) {
					MagicRender.objectspace(sprite, ship, ZERO, dir, size, ZERO, 270, 0, false, SPRITE_A,
							false, 0f, duration, durationOut, false);
					Global.getSoundPlayer().playSound(sId, MathUtils.getRandomNumberInRange(1.4f, 1.5f), volume, ship.getLocation(), ZERO);
					grade4 = true;
				}
			}
			if (grade > 4) {
				SpriteAPI sprite;
				sprite = sprites.get(4).one;
				if (!grade5) {
					MagicRender.objectspace(sprite, ship, ZERO, dir, size, ZERO, 270, 0, false, SPRITE_S,
							false, 0f, duration, durationOut, false);
					Global.getSoundPlayer().playSound(sId, MathUtils.getRandomNumberInRange(1.6f, 1.7f), volume, ship.getLocation(), ZERO);
					grade5 = true;
				}
			}
		}
		} if(state == State.OUT){

		}

		//engine.addFloatingText(ship.getLocation(), "dmg" + data.damageD, 60, Color.RED, ship, 0.5f, 1.0f);
		Global.getCombatEngine().getCustomData().put("ADVANCE_DATA_KEY" + ship.getId(), data);
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getEnergyWeaponDamageMult().unmodify(id);
		stats.getEnergyWeaponFluxCostMod().unmodify(id);
		stats.getBallisticWeaponDamageMult().unmodify(id);
		stats.getBallisticProjectileSpeedMult().unmodify(id);
		stats.getEnergyWeaponDamageMult().unmodify(id);
		stats.getEnergyProjectileSpeedMult().unmodify(id);

		updated = false;
	}

	public static class damageCounter implements DamageDealtModifier {
		public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
			CombatEngineAPI engine = Global.getCombatEngine();
			if (engine == null) {
				return null;
			}
			if (engine.isPaused()) {
				return null;
			}
			if (damage == null || damage.getStats() == null || damage.getStats().getEntity() == null) return null;
			ShipAPI ship = (ShipAPI)damage.getStats().getEntity();
			if (!ship.getSystem().isOn()) return null;
			if (!(target instanceof ShipAPI)) return null;

			ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("ADVANCE_DATA_KEY" + ship.getId());

			float amount = 0f;
			amount = engine.getElapsedInLastFrame();
			data.damageD += damage.computeDamageDealt(amount);

			Global.getCombatEngine().getCustomData().put("ADVANCE_DATA_KEY" + ship.getId(), data);
			return null;
		}
	}

	public static class ShipSpecificData {
		private float damageD;
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		String rank = "F";
		if (grade > 0)rank = "D";
		if (grade > 1)rank = "C";
		if (grade > 2)rank = "B";
		if (grade > 3)rank = "A";
		if (grade > 4)rank = "S";
		if (index == 0) {
			return new StatusData("Rank " + rank, false);
		}
		if (index == 1 && dState>0) {
			return new StatusData("+"+(int)(STAT_BONUS*dState)+" top speed and agility", false);
		}
		if (index == 2 && dState>1) {
			return new StatusData("+"+(int)(STAT_BONUS*dState)+"% weapon rate of fire and flux efficiency", false);
		}
		if (index == 3 && dState>2) {
			return new StatusData("+"+(int)(STAT_BONUS*dState)+"% weapon damage and projectile velocity", false);
		}

		return null;
	}
}


