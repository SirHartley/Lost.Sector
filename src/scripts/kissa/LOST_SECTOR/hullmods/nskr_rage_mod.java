//////////////////////
//Parts initially created by  Nicke535 and modified from Tahlan Shipworks
//some FX originally by Cycerin
//////////////////////
package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.dark.shaders.distortion.WaveDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.HashSet;
import java.util.List;

public class nskr_rage_mod extends BaseHullMod {

	//ABSOLUTE UNIT proceed at your own risk

	//Disappearance cooldown, minimum and maximum
	//  DOES NOT include the disappearance itself
	//  do it only once so 999999
	public static final float MAX_DISAPPEAR_COOLDOWN =9999f;
	public static final float MIN_DISAPPEAR_COOLDOWN =9999f;

	//Time to disappear, minimum and maximum
	public static final float MAX_DISAPPEAR_TIME = 2.0f;
	public static final float MIN_DISAPPEAR_TIME = 2.0f;
	public static final float HULL_REPAIR_PERCENT = 33f;

	//Alpha when "glitched out"
	public static final float GLITCH_OPACITY = 0.3f;

	//Duration to smoothly fade-in the ship after a glitch is over, in seconds
	public static final float OPACITY_FADE_TIME = 0.2f;

	public static final Color FLICKER_COLOR = new Color(179, 59, 59, 131);
	public static final Color SHIMMER_COLOR = new Color(179, 59, 95, 57);

	//enrage buffs
	public static final float ENRAGE_PPT_PENALTY = -80f;
	public static final float ENRAGE_SPEED_BONUS = 10f;
	public static final float ENRAGE_SPEED_BONUS_FLAT = 10f;
	public static final float ENRAGE_BALLISTIC_ROF_BONUS = 35f;
	public static final float ENRAGE_BALLISTIC_FLUX_BONUS = -35f;
	public static final float ENRAGE_ENERGY_DMG_BONUS = 35f;
	public static final float ENRAGE_FLUX_MULTI_BONUS = 40f;
	public static final float SHIELD_SPEED_BONUS = 100f;


	public static final Color JITTER_COLOR = new Color(179, 59, 59, 255);
	public static final Color JITTER_UNDER_COLOR = new Color(179, 59, 95, 255);

	// "Inhale" effect constants (originally by Cycerin)
	public static final int MAX_PARTICLES_PER_FRAME = 5;
	public static final Color PARTICLE_COLOR = new Color(255, 84, 135);
	public static final Color PARTICLE_COLOR2 = new Color(255, 84, 90);
	public static final float PARTICLE_OPACITY = 0.85f;
	public static final float PARTICLE_RADIUS = 130f;
	public static final float PARTICLE_SIZE = 5f;

	//dmg, smaller = more
	public static final float DAMAGE_MOD_VS_CAPITAL = 1.0f;
	public static final float DAMAGE_MOD_VS_CRUISER = 1.0f;
	public static final float DAMAGE_MOD_VS_DESTROYER = 1.0f;
	public static final float DAMAGE_MOD_VS_FIGHTER = 0.25f;
	public static final float DAMAGE_MOD_VS_FRIGATE = 1.0f;

	// Explosion effect constants
	public static final Color EXPLOSION_COLOR = new Color(160, 55, 101);
	public static final Color EMP_COLOR = new Color(204, 31, 71);
	public static final Color LENS_FLARE_OUTER_COLOR = new Color(150, 25, 65, 255);
	public static final Color LENS_FLARE_CORE_COLOR = new Color(255, 122, 180, 250);
	public static final float EXPLOSION_EMP_DAMAGE_AMOUNT = 1000f;
	public static final float EXPLOSION_DAMAGE_AMOUNT = 500f;
	public static final DamageType EXPLOSION_DAMAGE_TYPE = DamageType.ENERGY;
	public static final float DISTORTION_BLAST_RADIUS = 600f;
	public static final float EXPLOSION_PUSH_RADIUS = 1000f;
	public static final float EXPLOSION_VISUAL_RADIUS = 1000f;
	public static final float FORCE_VS_ASTEROID = 500f;
	public static final float FORCE_VS_MISSILE = 500f;
	public static final float FORCE_VS_CAPITAL = 90f;
	public static final float FORCE_VS_CRUISER = 250f;
	public static final float FORCE_VS_DESTROYER = 500f;
	public static final float FORCE_VS_FIGHTER = 750f;
	public static final float FORCE_VS_FRIGATE = 750f;
	public static final float EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER = .05f;
	public static final float EXPLOSION_EMP_VS_ALLIES_MODIFIER = .1f;
	public static final float EXPLOSION_FORCE_VS_ALLIES_MODIFIER = .33f;

	// variables
	private final IntervalUtil sparkleInterval = new IntervalUtil(1.50f, 3.00f);
	public static final Vector2f ZERO = new Vector2f();
	private CombatEntityAPI none;
	private StandardLight light;
	private WaveDistortion wave;
	// SOUND
	public static final String SOUND_ID = "nskr_prot_warning_2s";
	public static final String SOUND_ID2 = "nskr_phase2_exit";


	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return false;
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new preventOneshotScript(ship));
	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		final CombatEngineAPI engine = Global.getCombatEngine();
		if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
			return;
		}
		//Gets the custom data for our specific ship
		ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("FINAL_OVERDRIVE_DATA_KEY" + ship.getId());
		if (data == null) {
			data = new ShipSpecificData();
		}

		///////////////////////
		//Boss Enters 2nd Phase
		///////////////////////

		String id = "Rage_" + ship.getId();

		float HullRatio = (ship.getHitpoints() / ship.getMaxHitpoints());
		boolean overloadedOrVenting = ship.getFluxTracker().isOverloadedOrVenting();
		//for hull regen nonsense, so it stays active
		if (HullRatio <= 0.33 && ship.getCurrentCR()>0f && !overloadedOrVenting) {
			data.triggered = true;
		}
		if (data.triggered) {

			ship.getMutableStats().getPeakCRDuration().modifyPercent(id, ENRAGE_PPT_PENALTY);

			ship.getMutableStats().getMaxSpeed().modifyPercent(id, ENRAGE_SPEED_BONUS);
			ship.getMutableStats().getMaxSpeed().modifyFlat(id, ENRAGE_SPEED_BONUS_FLAT);
			ship.getMutableStats().getBallisticRoFMult().modifyPercent(id, ENRAGE_BALLISTIC_ROF_BONUS);
			ship.getMutableStats().getBallisticWeaponFluxCostMod().modifyPercent(id, ENRAGE_BALLISTIC_FLUX_BONUS);
			ship.getMutableStats().getEnergyWeaponDamageMult().modifyPercent(id, ENRAGE_ENERGY_DMG_BONUS);
			ship.getMutableStats().getFluxDissipation().modifyPercent(id, ENRAGE_FLUX_MULTI_BONUS);
			ship.getMutableStats().getShieldUnfoldRateMult().modifyPercent(id, SHIELD_SPEED_BONUS);
			ship.getMutableStats().getShieldTurnRateMult().modifyPercent(id, SHIELD_SPEED_BONUS);

			//FX
			ship.setJitter(id, JITTER_COLOR, 0.2f, 1, 1f);
			ship.setJitterUnder(id, JITTER_UNDER_COLOR, 4, 3, 4f);
			ship.setJitterShields(false);

			Color color = new Color(185, 100, 255, 255);
			Color color2 = new Color(110, 102, 117, 100);
			ship.getEngineController().fadeToOtherColor(this, color, color2, 0.6f, 0.6f);
			ship.getEngineController().extendFlame(this, 0.45f, 0.25f, 0.25f);


			//EMP ARCS + DISTORTION
			Vector2f sloc = ship.getLocation();
			sparkleInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
			if (sparkleInterval.intervalElapsed()) {
				for (int x = 0; x < 3; x++) {
					float angle = (float) Math.random() * 360f;
					float distance = (float) Math.random() * 150f + 150f;
					float angle2 = (float) Math.random() * 360f;
					float distance2 = (float) Math.random() * 15f;
					Vector2f point1 = MathUtils.getPointOnCircumference(sloc, distance, angle);
					Vector2f point2 = MathUtils.getPointOnCircumference(sloc, distance2, angle2);
					if (ship != null) {
						//color fuckery
						Color color1 = util.randomiseColor(new Color(255, 100, 150, 255), 0, 100, 50, 0, true);
						Color color3 = util.randomiseColor(new Color(255, 100, 150, 155), 0, 100, 50, 0, true);
						Global.getCombatEngine().spawnEmpArcVisual(point1, none, point2, ship,
								MathUtils.getRandomNumberInRange(5f, 20f), // thickness of the lightning bolt
								color1, //Central color
								color3 //Fringe Color
						);

						RippleDistortion ripple = new RippleDistortion(point1, ZERO);
						ripple.setSize(MathUtils.getRandomNumberInRange(50f, 100f));
						ripple.setIntensity(MathUtils.getRandomNumberInRange(50f, 100f));
						ripple.setFrameRate(240f / MathUtils.getRandomNumberInRange(60f, 120f));
						ripple.fadeInSize(MathUtils.getRandomNumberInRange(0.5f, 1.0f));
						ripple.fadeOutIntensity(MathUtils.getRandomNumberInRange(0.5f, 1.0f));
						DistortionShader.addDistortion(ripple);
					}
				}
			}
			//REGEN
			float duration = 2f;
			String rId = "nskr_regenPlugin_" + ship.getId();
			if (data.toRegen>0.01f) {
				//Global.getCombatEngine().addFloatingText(ship.getLocation(), "REGEN " + data.toRegen, 48, Color.RED, ship, 0.5f, 1.0f);
				if (ship.isExpired() || ship.isHulk()) {
					data.toRegen = 0;
				} else {
					ship.getMutableStats().getHullCombatRepairRatePercentPerSecond().modifyFlat(rId, data.toRegen / duration);
					ship.getMutableStats().getMaxCombatHullRepairFraction().modifyFlat(rId, data.toRegen);
					data.toRegen -= (data.toRegen / duration) * amount;
				}
			}
			if (data.toRegen<=0.01f) {
				ship.getMutableStats().getHullCombatRepairRatePercentPerSecond().unmodify(id);
				ship.getMutableStats().getMaxCombatHullRepairFraction().unmodify(id);
			}

			//spew out particles
			if (data.glitchDurationRemaining <= 0f){
				Vector2f particlePos, particleVel;
				int numParticlesThisFrame = Math.round((MAX_PARTICLES_PER_FRAME/4f * (engine.getElapsedInLastFrame()*60f)));
				for (int x = 0; x < numParticlesThisFrame; x++) {
					particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), (float)Math.random()*(ship.getCollisionRadius()-50f));
					particleVel = Vector2f.sub(particlePos, ship.getLocation(), null);
					Global.getCombatEngine().addSmokeParticle(particlePos, particleVel, PARTICLE_SIZE, PARTICLE_OPACITY/2.5f, 0.7f,
							PARTICLE_COLOR2);
				}
			}

			if (data.glitchCooldown <= 0f) {
				ship.setJitterShields(false);
				ship.setJitterUnder(ship, SHIMMER_COLOR, 0.5f, 20, 1f, 5f);

				//when in 2nd phase
				boolean shouldActivate = data.triggered;

				//Armor grid check
				int maxX = ship.getArmorGrid().getLeftOf() + ship.getArmorGrid().getRightOf();
				int maxY = ship.getArmorGrid().getAbove() + ship.getArmorGrid().getBelow();
				for (int ix = 0; ix < maxX; ix++) {
					for (int iy = 0; iy < maxY; iy++) {
						if (ship.getArmorGrid().getArmorFraction(ix, iy) > 0f) {
							data.lastFrameDestroyedGridPieces.remove(ix + (iy * maxX));
						} else {
							//If the grid piece wasn't destroyed last frame, it was lost this frame
							if (!data.lastFrameDestroyedGridPieces.contains(ix + (iy * maxX))) {
								shouldActivate = true;
								data.lastFrameDestroyedGridPieces.add(ix + (iy * maxX));
							}
						}
					}
				}

				//SOUND
				if (shouldActivate) {
					Vector2f loc = new Vector2f(ship.getLocation());
					Color color3 = new Color(255, 94, 31, 255);
					engine.addFloatingText(loc, "FINAL OVERDRIVE", 32.0f, color3, ship, 0.5f, 1.0f);
					Global.getSoundPlayer().playSound(SOUND_ID, 0.8f, 0.8f, ship.getLocation(), ship.getVelocity());

					data.toRegen = HULL_REPAIR_PERCENT;

					float level = ((ship.getFluxLevel() * 0.5f) * ship.getMaxFlux());
					ship.getFluxTracker().decreaseFlux(level);

					data.hasExitedGlitch = false;
					float disappearTime = MathUtils.getRandomNumberInRange(MIN_DISAPPEAR_TIME, MAX_DISAPPEAR_TIME);
					data.glitchCooldown = MathUtils.getRandomNumberInRange(MIN_DISAPPEAR_COOLDOWN, MAX_DISAPPEAR_COOLDOWN) + disappearTime;
					data.glitchDurationRemaining = disappearTime;
				}
			}
		}
		//If we're currently in a glitch period, phase us out and affect opacity
		//PHASE
		if (data.glitchDurationRemaining > 0f) {
			//only phase cruisers since this mod is only on destroyers and cruiser
			if (ship.getHullSize()!=HullSize.DESTROYER) {
				ship.setPhased(true);
				ship.setCollisionClass(CollisionClass.NONE);
				ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
				ship.blockCommandForOneFrame(ShipCommand.VENT_FLUX);
				ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
			}

			//INHALE EFFECT
			Vector2f particlePos, particleVel;
			int numParticlesThisFrame = Math.round(MAX_PARTICLES_PER_FRAME * (engine.getElapsedInLastFrame()*60f));
			for (int x = 0; x < numParticlesThisFrame; x++) {
				particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), PARTICLE_RADIUS);
				particleVel = Vector2f.sub(ship.getLocation(), particlePos, null);
				Global.getCombatEngine().addSmokeParticle(particlePos, particleVel, PARTICLE_SIZE, PARTICLE_OPACITY, 1f,
						PARTICLE_COLOR);
			}

			if (ship.getHullSize()!=HullSize.DESTROYER) {
				if (data.glitchDurationRemaining > OPACITY_FADE_TIME) {
					ship.setExtraAlphaMult(GLITCH_OPACITY);
					ship.setApplyExtraAlphaToEngines(true);

					ship.setJitter(ship, FLICKER_COLOR, 0.7f, 10, 25f, 50f);
				} else {
					ship.setExtraAlphaMult(Misc.interpolate(GLITCH_OPACITY, 1f, data.glitchDurationRemaining / OPACITY_FADE_TIME));
				}
			}

			data.glitchDurationRemaining -= amount;
		} else {
			if (ship.getHullSize()!=HullSize.DESTROYER) {
				ship.setPhased(false);
				ship.setCollisionClass(CollisionClass.SHIP);
				ship.setExtraAlphaMult(1f);
			}
			//Regen armor if we haven't yet

			if (!data.hasExitedGlitch) {

				regenArmor(ship);
				data.hasExitedGlitch = true;

				//EXPLOSION

				engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS,
						0.31f);
				engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS /
						2f, 0.29f);

				Vector2f loc = new Vector2f(ship.getLocation());
				loc.x -= 8f * FastTrig.cos(ship.getFacing() * Math.PI / 180f);
				loc.y -= 8f * FastTrig.sin(ship.getFacing() * Math.PI / 180f);

				//lens flare fx
				MagicLensFlare.createSharpFlare(
						engine,
						ship,
						loc,
						50f,
						750f,
						ship.getFacing(),
						LENS_FLARE_OUTER_COLOR,
						LENS_FLARE_CORE_COLOR
				);

				//light fx
				light = new StandardLight();
				light.setLocation(loc);
				light.setIntensity(2.0f);
				light.setSize(EXPLOSION_VISUAL_RADIUS * 3f);
				light.setColor(EXPLOSION_COLOR);
				light.fadeOut(3f);
				LightShader.addLight(light);

				//distortion fx
				wave = new WaveDistortion();
				wave.setLocation(loc);
				wave.setSize(DISTORTION_BLAST_RADIUS * 1.5f);
				wave.setIntensity(DISTORTION_BLAST_RADIUS * 0.15f);
				wave.fadeInSize(1.2f);
				wave.fadeOutIntensity(0.9f);
				wave.setSize(DISTORTION_BLAST_RADIUS * 0.55f);
				DistortionShader.addDistortion(wave);

				Global.getSoundPlayer().playSound(SOUND_ID2, 0.7f, 1.0f, ship.getLocation(), ship.getVelocity());

				ShipAPI victim;
				Vector2f dir;
				float force, damage, emp, mod;
				List<CombatEntityAPI> entities = CombatUtils.getEntitiesWithinRange(ship.getLocation(),
						EXPLOSION_PUSH_RADIUS);
				int size = entities.size();
				int i = 0;
				while (i < size) {
					CombatEntityAPI tmp = entities.get(i);

					mod = 1f - (MathUtils.getDistance(ship, tmp) / EXPLOSION_PUSH_RADIUS);
					force = FORCE_VS_ASTEROID * mod;
					damage = EXPLOSION_DAMAGE_AMOUNT * mod;
					emp = EXPLOSION_EMP_DAMAGE_AMOUNT * mod;

					//fuck missiles
					if (tmp instanceof MissileAPI) {
						force = FORCE_VS_MISSILE * mod;
						engine.applyDamage(tmp, loc, 400, DamageType.FRAGMENTATION, 0, false, false, ship);
					}

					if (tmp instanceof ShipAPI) {
						victim = (ShipAPI) tmp;

						// Modify push strength and dmg based on ship class
						if (victim.getHullSize() == ShipAPI.HullSize.FIGHTER) {
							force = FORCE_VS_FIGHTER * mod;
							damage /= DAMAGE_MOD_VS_FIGHTER;
						} else if (victim.getHullSize() == ShipAPI.HullSize.FRIGATE) {
							force = FORCE_VS_FRIGATE * mod;
							damage /= DAMAGE_MOD_VS_FRIGATE;
						} else if (victim.getHullSize() == ShipAPI.HullSize.DESTROYER) {
							force = FORCE_VS_DESTROYER * mod;
							damage /= DAMAGE_MOD_VS_DESTROYER;
						} else if (victim.getHullSize() == ShipAPI.HullSize.CRUISER) {
							force = FORCE_VS_CRUISER * mod;
							damage /= DAMAGE_MOD_VS_CRUISER;
						} else if (victim.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) {
							force = FORCE_VS_CAPITAL * mod;
							damage /= DAMAGE_MOD_VS_CAPITAL;
						}

						if (victim.getOwner() == ship.getOwner()) {
							damage *= EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER;
							emp *= EXPLOSION_EMP_VS_ALLIES_MODIFIER;
							force *= EXPLOSION_FORCE_VS_ALLIES_MODIFIER;
						}

						//spawn emp arcs to unshielded targets
						if ((victim.getShield() != null && victim.getShield().isOn() && victim.getShield().isWithinArc(
								ship.getLocation()))) {
							victim.getFluxTracker().increaseFlux(damage * 1, true);
						} else {
							ShipAPI empTarget = victim;
							for (int x = 0; x < 5; x++) {
								EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(ship, MathUtils.getRandomPointInCircle(victim.getLocation(), victim.getCollisionRadius()),
										empTarget,
										empTarget, EXPLOSION_DAMAGE_TYPE, damage / 10, emp / 5,
										EXPLOSION_PUSH_RADIUS, null, 2f, EMP_COLOR,
										EMP_COLOR);
							}
						}
					}

					dir = VectorUtils.getDirectionalVector(ship.getLocation(), tmp.getLocation());
					dir.scale(force);

					Vector2f.add(tmp.getVelocity(), dir, tmp.getVelocity());
					i++;
				}
			}
		}

		//Finally, write the custom data back to the engine, and update last-frame variables
		Global.getCombatEngine().getCustomData().put("FINAL_OVERDRIVE_DATA_KEY" + ship.getId(), data);
	}
	//Calculates total armor of a ship
	private float getTotalArmor(ShipAPI ship) {
		int maxX = ship.getArmorGrid().getLeftOf() + ship.getArmorGrid().getRightOf();
		int maxY = ship.getArmorGrid().getAbove() + ship.getArmorGrid().getBelow();
		float armor = 0f;
		for (int ix = 0; ix < maxX; ix++) {
			for (int iy = 0; iy < maxY; iy++) {
				armor += ship.getArmorGrid().getArmorValue(ix, iy);
			}
		}
		return armor;
	}

	//Handles regenerating armor of the ship
	private void regenArmor(ShipAPI ship) {
		//First, calculates average armor
		int maxX = ship.getArmorGrid().getLeftOf() + ship.getArmorGrid().getRightOf();
		int maxY = ship.getArmorGrid().getAbove() + ship.getArmorGrid().getBelow();
		float averageArmor = getTotalArmor(ship) / (float)(maxX*maxY);

		if (averageArmor < ship.getArmorGrid().getMaxArmorInCell() * 0.75f) {
			averageArmor = ship.getArmorGrid().getMaxArmorInCell() * 0.75f;
		}

		//Then we check all armor grid pieces again to set them to the average
		for (int ix = 0; ix < maxX; ix++) {
			for (int iy = 0; iy < maxY; iy++) {
				//if (ship.getArmorGrid().getArmorValue(ix, iy) < averageArmor) {
				ship.getArmorGrid().setArmorValue(ix, iy, averageArmor);
				//}
			}
		}
	}

	public static class preventOneshotScript implements HullDamageAboutToBeTakenListener {
		public ShipAPI ship;

		public preventOneshotScript(ShipAPI ship) {
			this.ship = ship;
		}

		public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
			ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("FINAL_OVERDRIVE_DATA_KEY" + ship.getId());
			if (data == null) {
				return false;
			}

			boolean overloadedOrVenting = ship.getFluxTracker().isOverloadedOrVenting();
			float hull = ship.getHitpoints();
			if (damageAmount >= hull && !data.triggered && ship.getCurrentCR()>0f && !overloadedOrVenting) {
				ship.setHitpoints(1f);
				data.triggered = true;

				Global.getCombatEngine().getCustomData().put("FINAL_OVERDRIVE_DATA_KEY" + ship.getId(), data);
				return true;
			}

			Global.getCombatEngine().getCustomData().put("FINAL_OVERDRIVE_DATA_KEY" + ship.getId(), data);
			return false;
		}
	}

	public static class ShipSpecificData {
		private float toRegen = 0f;
		private boolean triggered = false;
		private float glitchCooldown = 0f;
		private float glitchDurationRemaining = 0f;
		private boolean hasExitedGlitch = true;
		private final HashSet<Integer> lastFrameDestroyedGridPieces = new HashSet<>();
	}
}