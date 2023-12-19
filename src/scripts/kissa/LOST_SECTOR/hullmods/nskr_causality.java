//////////////////////
//rendering stuff originally from SWP by DarkRevenant
//////////////////////
package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Pair;
import org.dark.shaders.post.PostProcessShader;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.shipsystems.nskr_causalityStats;
import scripts.kissa.LOST_SECTOR.util.combatUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class nskr_causality extends BaseHullMod {

	//
	//handles the energy management and stat buff side of the causality system, and rendering too
	//

	//buff
	public static final float FLUX_COST = 15f;
	public static final float GUN_BONUS = 15f;
	public static final float DMOD_PENALTY = 5f;
	public static final float BEAM_PENALTY_MULT = 2f;
	public static final float FLUXMULT = 4000f;

	public static final Color COLOR4 = new Color(255, 101, 144);
	//In-script variables
	private final IntervalUtil afterImageTimer;
	private final IntervalUtil timer = new IntervalUtil(0.20f, 0.20f);
	private float shipTimeMult;
	private float maxEnergy;
	public static final String ENGINE_DATA_KEY = "nskr_causalityRenderer";
	public static final String SPRITE_PATH = "graphics/fx/nskr_select.png";
	public static final String TEXT1 = "Weapons";
	public static final String TEXT2 = "Energy";
	public static final String TEXT3 = "Warning";
	public static final String MOD_ICON = "graphics/icons/hullsys/emp_emitter.png";
	public static final String MOD_BUFFID = "nskr_causality";
	public static final String MOD_NAME = "Causality Core";

	public nskr_causality() {
		this.afterImageTimer = new IntervalUtil(0.01f, 0.01f);
	}

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		//some buffs
		stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -FLUX_COST);
		stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -11.1f-FLUX_COST);

		stats.getBallisticRoFMult().modifyPercent(id, GUN_BONUS);
		stats.getEnergyWeaponDamageMult().modifyPercent(id, GUN_BONUS);

		//nerf
		stats.getBeamDamageTakenMult().modifyFlat(id, BEAM_PENALTY_MULT);
	}

	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

		int dmods = (int)util.getDMods(ship.getVariant());
		if(dmods>0) {
			ship.getMutableStats().getFluxDissipation().modifyPercent(id, -DMOD_PENALTY * dmods);
			ship.getMutableStats().getFluxCapacity().modifyPercent(id, -DMOD_PENALTY * dmods);
			ship.getMutableStats().getPeakCRDuration().modifyPercent(id, -DMOD_PENALTY * dmods);
		}
	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		boolean player = false;
		player = ship == Global.getCombatEngine().getPlayerShip();
		CombatEngineAPI engine = Global.getCombatEngine();

		if (engine.isPaused()) return;

		ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("CAUSALITY_DATA_KEY" + ship.getId());
		if (data == null) {
			data = new ShipSpecificData();
		}
		if (player) {
			//do once per engine
			if (!engine.getCustomData().containsKey(ENGINE_DATA_KEY)) {
				engine.addLayeredRenderingPlugin(new IndicatorRenderer());
				engine.getCustomData().put(ENGINE_DATA_KEY, true);
			}
		}

		if (data.energy<0) data.energy = 0f;
		//decay
		timer.advance((amount / shipTimeMult));
		if (timer.intervalElapsed()&&data.energy > 0f) {
			//dump energy when vent/overload
			if (ship.getFluxTracker().isVenting() || ship.getFluxTracker().isOverloaded()){
				data.energy *= 0.50f;
			} else {
				data.energy *= 0.96f;
			}
			data.energy -= 50f;
		}
		//TIME STUFF
		maxEnergy = ship.getMaxFlux();
		String id = "causality_" + ship.getId();

		float multiplier = data.energy/maxEnergy;
		float effectSqrt = (float) Math.sqrt(multiplier);

		float multiplierNrg = data.energy/maxEnergy;
		if (multiplierNrg>1) multiplierNrg = 1f;
		float baseTimeMult = maxEnergy/FLUXMULT;
		float curTimeMult = (data.energy/FLUXMULT)+(baseTimeMult*multiplierNrg);

		shipTimeMult = 1f + (curTimeMult);
		ship.getMutableStats().getTimeMult().modifyMult(id, shipTimeMult);
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}

		if (data.energy>(maxEnergy/2)) {
			ship.getMutableStats().getEnergyWeaponFluxCostMod().modifyPercent(id, -FLUX_COST);
			ship.getMutableStats().getBallisticWeaponFluxCostMod().modifyPercent(id, -11.1f-FLUX_COST);

			ship.getMutableStats().getBallisticRoFMult().modifyPercent(id, GUN_BONUS);
			ship.getMutableStats().getEnergyWeaponDamageMult().modifyPercent(id, GUN_BONUS);
		} else {
			ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodify(id);
			ship.getMutableStats().getBallisticWeaponFluxCostMod().unmodify(id);

			ship.getMutableStats().getBallisticRoFMult().unmodify(id);
			ship.getMutableStats().getEnergyWeaponDamageMult().unmodify(id);
		}


		if (data.energy>0){
			//FX TIME
			if (Global.getCombatEngine().getPlayerShip() == ship) {
				PostProcessShader.setNoise(false, mathUtil.lerp(0f, 0.25f, effectSqrt));
				PostProcessShader.setLightness(false, mathUtil.lerp(1f, 1.25f, effectSqrt));
				PostProcessShader.setSaturation(false, mathUtil.lerp(1f, 0.5f, effectSqrt));

				//engine.addFloatingText(ship.getLocation(), "test " + effectSqrt, 20f, Color.cyan, ship, 0.5f, 1.0f);

				//SOUND LOOPS
				float min = 4000f;
				float max = 8000f;
				if (data.energy>min){
					float modSynth = mathUtil.normalize(MathUtils.clamp(data.energy, min, max), min, max);
					float mult = 1.5f;
					float modNoise = mathUtil.normalize(MathUtils.clamp(data.energy, min*mult, max*mult), min*mult, max*mult);

					Global.getSoundPlayer().playLoop("nskr_eternity_loop", ship,1f, 0.60f*modSynth, ship.getLocation(), new Vector2f());
					Global.getSoundPlayer().playLoop("nskr_eternity_loop_noise", ship,0.8f, 0.75f*modNoise, ship.getLocation(), new Vector2f());
				}
			}
			int red = COLOR4.getRed();
			int green = COLOR4.getGreen();
			int blue = COLOR4.getBlue();
			int alpha = Math.round((COLOR4.getAlpha()* multiplier)/1.5f);
			if (alpha>255) alpha = 255;
			Color color1 = new Color(red,green,blue,alpha);

			this.afterImageTimer.advance(Global.getCombatEngine().getElapsedInLastFrame());
			if (this.afterImageTimer.intervalElapsed()) {
				ship.addAfterimage(color1, 0.0f, 0.0f, ship.getVelocity().x * -0.8f, ship.getVelocity().y * -0.8f, 0.0f, 0.0f, 0.0f, 0.5f, true, true, false);
			}
		} else PostProcessShader.resetDefaults();

		//2 decimal round
		float rounded = 0f;
		rounded = shipTimeMult*100f;
		rounded = Math.round(rounded);
		rounded /= 100f;
		if (ship == Global.getCombatEngine().getPlayerShip()) {
			Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, "ENERGY " + (int) data.energy + " TIME MULTIPLIER " + rounded, true);
		}

		Global.getCombatEngine().getCustomData().put("CAUSALITY_DATA_KEY" + ship.getId(), data);
	}

	public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 10.0f;
		float maxEnergy = ((ship.getMaxFlux()/FLUXMULT)*2f)+1f;
		maxEnergy *= 100f;
		maxEnergy = Math.round(maxEnergy);
		maxEnergy /= 100f;

		tooltip.addSectionHeading("Details", Alignment.MID, pad);
		TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/ammo_feeder.png", 36.0f);
		text.addPara(TEXT1, 0.0f, util.NICE_YELLOW, TEXT1);
		text.addPara("-15%% weapon flux usage.", 2.0f, util.BON_GREEN, "15%");
		text.addPara("+15%% energy weapon damage.", 2.0f, util.BON_GREEN, "15%");
		text.addPara("+15%% ballistic weapon rate of fire (with an additional matching reduction in flux usage).", 2.0f, util.BON_GREEN, "15%");
		tooltip.addImageWithText(pad);

		text = tooltip.beginImageWithText("graphics/icons/hullsys/damper_field.png", 36.0f);
		text.addPara(TEXT2, 0.0f, util.NICE_YELLOW, TEXT2);
		text.addPara("-Max energy capacity is " + (int)ship.getMaxFlux(), 2.0f, util.BON_GREEN, "" + (int)ship.getMaxFlux());
		text.addPara("-Maximum safe time dilation is " + maxEnergy, 2.0f, util.BON_GREEN, "" + maxEnergy);
		text.addPara("-Absorbing more energy dramatically increases time dilation.", 2.0f, util.NICE_YELLOW, "");
		text.addPara("-Going above 50%% energy capacity increases the weapons buff.", 2.0f, util.NICE_YELLOW, "50%");
		text.addPara("-Absorbed projectiles grant more energy than missiles.", 2.0f, util.BON_GREEN, "");
		text.addPara("-Absorbed missiles deal 1x the original damage while absorbed projectiles deal 1.5x the damage.", 2.0f, util.BON_GREEN, "");
		text.addPara("-Created bolts counts as an energy weapon for bonuses, while created homing blobs count as a missile weapon for bonuses.", 2.0f, util.BON_GREEN, "");
		tooltip.addImageWithText(pad);

		text = tooltip.beginImageWithText("graphics/icons/hullsys/entropy_amplifier.png", 36.0f);
		text.addPara(TEXT3, 0.0f, util.TT_ORANGE, TEXT3);
		text.addPara("-Going above energy capacity is dangerous.", 2.0f, util.TT_ORANGE, "dangerous");
		text.addPara("-Will cause unpredictable anomalies.", 2.0f, util.TT_ORANGE, "");
		text.addPara("-Ship receives "+(int)BEAM_PENALTY_MULT+"x"+" more damage from beams.", 2.0f, util.TT_ORANGE, (int)BEAM_PENALTY_MULT+"x");
		text.addPara("-Any D-mods on the hull will reduce flux capacity, flux dissipation, and peak performance time by "+(int)DMOD_PENALTY+"%%"+" each.", 2.0f, util.TT_ORANGE, (int)DMOD_PENALTY+"%");
		int dmods = (int)util.getDMods(ship.getVariant());
		if(dmods>0)text.addPara("-Current penalty "+(int)(dmods*DMOD_PENALTY)+"%%", 2.0f, util.TT_ORANGE, (int)(dmods*DMOD_PENALTY)+"%");
		tooltip.addImageWithText(pad);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}

	@Override
	public Color getNameColor() {
		return new Color(176, 68, 171,255);
	}

	public static class ShipSpecificData {
		public float energy = 0f;
	}

	//INDICATOR RENDERING
	//yoinked from swp but mostly new code

	private class IndicatorRenderer implements CombatLayeredRenderingPlugin {
		private final Color COLOR1 = new Color(255, 20, 50, 255);
		private boolean loaded = false;
		private SpriteAPI sprite = null;
		private float angle = 0f;
		private final List<Pair<CombatEntityAPI, Float>> newTargets = new ArrayList<>();
		@Override
		public void init(CombatEntityAPI entity) {
			if (sprite == null) {
				// Load sprite if it hasn't been loaded yet - not needed if you add it to settings.json
				if (!loaded) {
					try {
						Global.getSettings().loadTexture(SPRITE_PATH);
					} catch (IOException ex) {
						throw new RuntimeException("Failed to load sprite '" + SPRITE_PATH + "'!", ex);
					}
					loaded = true;
				}
				sprite = Global.getSettings().getSprite(SPRITE_PATH);
			}
		}

		@Override
		public void cleanup() {
		}

		@Override
		public boolean isExpired() {
			return false;
		}

		@Override
		public void advance(float amount) {
			if (Global.getCombatEngine() == null) {
				return;
			}
		}

		@Override
		public EnumSet<CombatEngineLayers> getActiveLayers() {
			return EnumSet.of(CombatEngineLayers.BELOW_SHIPS_LAYER);
		}

		@Override
		public float getRenderRadius() {
			return 99999999f;
		}

		@Override
		public void render(CombatEngineLayers layer, ViewportAPI viewport) {
			if (Global.getCombatEngine() == null) {
				return;
			}
			ShipAPI ship = Global.getCombatEngine().getPlayerShip();
			if (ship == null) {
				return;
			}
			if (!ship.getHullSpec().getBaseHullId().startsWith("nskr_eternity")) {
				return;
			}
			if (ship.getSystem() == null) {
				return;
			}

			boolean isActive = true;
			if (ship.getSystem().isOutOfAmmo()) {
				isActive = false;
			}
			if (ship.getSystem().getState() != ShipSystemAPI.SystemState.IDLE) {
				isActive = false;
			}
			if (ship.getFluxTracker().isOverloadedOrVenting()) {
				isActive = false;
			}
			if (!ship.isAlive()) {
				isActive = false;
			}

			ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("CAUSALITY_DATA_KEY" + ship.getId());

			float range = nskr_causalityStats.getMaxRange(ship);

			List<CombatEntityAPI> targets = new ArrayList<>(Global.getCombatEngine().getProjectiles().size());
			targets.addAll(combatUtil.getProjectilesWithinRange(ship.getLocation(), range));
			targets.addAll(combatUtil.getMissilesWithinRange(ship.getLocation(), range));

			for (CombatEntityAPI target : targets) {
				//make sure we add only once
				boolean isSame = false;
				for (Pair<CombatEntityAPI, Float> c : newTargets) {
					if (c.one == target) {
						isSame = true;
						break;
					}
				}
				if (isSame) continue;
				if (target.isExpired() || !Global.getCombatEngine().isEntityInPlay(target)) continue;
				DamagingProjectileAPI proj = (DamagingProjectileAPI) target;
				if (proj.didDamage() || proj.isExpired())continue;
				//Global.getCombatEngine().addFloatingText(target.getLocation(), "NEW", 24, Color.RED, null, 0.5f, 1.0f);
				newTargets.add(new Pair<>(target, 1f));
			}
			float amount = Global.getCombatEngine().getElapsedInLastFrame();

			//speeen
			if (!Global.getCombatEngine().isPaused()) {
				angle += ((amount * 40f) * (shipTimeMult + 1f)) * (1f + ((data.energy / FLUXMULT) / 2f));
			}
			if (angle > 360) angle = 0f;

			float sizeDecaySpeed = 1.5f;
			for (Pair<CombatEntityAPI, Float> target : newTargets) {
				if (target.one instanceof DamagingProjectileAPI) {
					if (!nskr_causalityStats.isValid(target.one,ship)) continue;
					DamagingProjectileAPI proj = (DamagingProjectileAPI) target.one;
					if((proj).getDamageAmount()<50) continue;

					//highlight new stuff
					float sizeMult;
					float alpha = 1f;
					sizeMult = Math.max(1,1f+(mathUtil.smoothStep(mathUtil.lerp(0f, 1f, target.two))));
					if (target.two>0.67f){
						alpha = mathUtil.smoothStep(mathUtil.lerp(0f, 3f, mathUtil.inverse(target.two)));
					}
					Color color = COLOR1;
					color = util.setAlpha(color, (int)(color.getAlpha()*alpha));

					Vector2f size = new Vector2f(25f, 25f);
					float damageMult = 1 + proj.getDamageAmount() / 250;
					if (!(proj instanceof MissileAPI)) damageMult *= 2f;
					//cap
					float finalSize = Math.min(400, size.getX() * damageMult * sizeMult);

					size.setX(finalSize);
					size.setY(finalSize);

					//Global.getCombatEngine().addFloatingText(target.one.getLocation(), "lol " + sizeMult +" "+(int)finalSize, 24, Color.RED, target.one, 0.5f, 1.0f);
					//RENDER
					if (isActive && Global.getCombatEngine().isUIShowingHUD()) {
						sprite.setAdditiveBlend();
						sprite.setColor(color);
						sprite.setWidth(finalSize);
						sprite.setHeight(finalSize);
						sprite.renderAtCenter(proj.getLocation().getX(), proj.getLocation().getY());
						sprite.setAngle(angle);
					}
				}
			}
			for (Pair<CombatEntityAPI, Float> nt : newTargets) {
				if (!Global.getCombatEngine().isPaused()) {
					nt.two -= amount*sizeDecaySpeed;
				}
			}
			//cleanup
			for (Iterator<Pair<CombatEntityAPI, Float>> iter = newTargets.listIterator(); iter.hasNext(); ) {
				Pair<CombatEntityAPI, Float> a = iter.next();
				DamagingProjectileAPI proj = (DamagingProjectileAPI)a.one;
				if (a.one == null || a.one.isExpired() || proj.didDamage() || !Global.getCombatEngine().isEntityInPlay(a.one) || MathUtils.getDistance(a.one,ship) > range) {
					iter.remove();
				}
			}
		}
	}
}

