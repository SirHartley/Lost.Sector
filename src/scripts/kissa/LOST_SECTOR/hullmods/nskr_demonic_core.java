package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Pair;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.shipsystems.nskr_bfPulseStats;
import scripts.kissa.LOST_SECTOR.util.combatUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class nskr_demonic_core extends BaseHullMod {

	public static final float SUPPLY_USE_MULT = 2f;
	public static final float FLUX_MULT = 1.25f;

	//In-script variables
	public static final String ENGINE_DATA_KEY = "nskr_demonic_coreRenderer";
	public static final String SPRITE_PATH = "graphics/fx/nskr_select.png";

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSuppliesPerMonth().modifyMult(id, SUPPLY_USE_MULT);
		stats.getFluxCapacity().modifyMult(id, FLUX_MULT);
		stats.getFluxDissipation().modifyMult(id, FLUX_MULT);

	}

	//draw for succ system
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		boolean player = false;
		player = ship == Global.getCombatEngine().getPlayerShip();
		CombatEngineAPI engine = Global.getCombatEngine();

		if (engine.isPaused()) return;

		if (player && ship.getSystem().getId().equals("nskr_bfpulse")){
			//do once per engine
			if (!engine.getCustomData().containsKey(ENGINE_DATA_KEY)) {
				engine.addLayeredRenderingPlugin(new IndicatorRenderer());
				engine.getCustomData().put(ENGINE_DATA_KEY, true);
			}
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 1) return "" + (int)((SUPPLY_USE_MULT - 1f) * 100f) + "%";
		if (index == 0) return "" + FLUX_MULT;
		return null;
	}

	@Override
	public Color getNameColor() {
		return new Color(219, 0, 66,255);
	}


	//INDICATOR RENDERING
	//yoinked from swp but mostly new code

	private static class IndicatorRenderer implements CombatLayeredRenderingPlugin {
		private final Color COLOR1 = new Color(255, 20, 50, 141);

		private boolean loaded = false;
		private SpriteAPI sprite = null;
		private float angle = 0f;
		private final java.util.List<Pair<CombatEntityAPI, Float>> newTargets = new ArrayList<>();
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
			if (!ship.getHullSpec().getBaseHullId().startsWith("nskr_reverie_boss")) {
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

			float range = nskr_bfPulseStats.getMaxRangeSucc(ship);

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
				angle += ((amount * 80f));
			}
			if (angle > 360) angle = 0f;

			float sizeDecaySpeed = 1.5f;
			for (Pair<CombatEntityAPI, Float> target : newTargets) {
				if (target.one instanceof DamagingProjectileAPI) {
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
					//cap
					float finalSize = Math.min(300, size.getX() * damageMult * sizeMult);

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
