package lostsector.combat.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import lostsector.helper.MathHelper;
import lostsector.rendering.ColorHelper;
import org.magiclib.util.MagicIncompatibleHullmods;

import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class AdvancedShieldProjector extends BaseHullMod {

	public static final float RESISTANCE_BONUS = 0.50f;
	public static final float FOLD_BONUS = 1.00f;

	public static final String INNER_LARGE = "graphics/lostsector/fx/nskr_protShield.png";

	public static final String MOD_ICON = "graphics/icons/hullsys/fortress_shield.png";

	public static final String MOD_BUFFID = "nskr_focused_shield";
	public static final String MOD_NAME = "Adaptive Shield Projector";
	private boolean loaded = false;

	public static final Set<String> BLOCKED_HULLMODS = new HashSet<>();

    @Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return false;
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if (ship.getHullSize()!=HullSize.FIGHTER) {
			if (!ship.hasListenerOfClass(PrototypeExplosion.ProtExplosionListener.class)) {
				ship.addListener(new PrototypeExplosion.ProtExplosionListener(ship));
			}
		}

		for (String tmp : BLOCKED_HULLMODS) {
			if (ship.getVariant().getHullMods().contains(tmp)) {
				//if someone tries to install blocked hullmod, remove it
				MagicIncompatibleHullmods.removeHullmodWithWarning(
						ship.getVariant(),
						tmp,
						"nskr_focused_shield"
				);
			}
		}
	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
			return;
		}
		if (ship.getShield()==null) return;
		if (!loaded) {
			loaded = true;
			try {
				Global.getSettings().loadTexture(INNER_LARGE);
			} catch (IOException ex) {
				throw new RuntimeException("Failed to load sprite '" + INNER_LARGE + "'!", ex);
			}
		}
		ship.getShield().setRadius(ship.getShieldRadiusEvenIfNoShield(), INNER_LARGE, INNER_LARGE);

		float fluxRatioRes;
		float fluxRatio = ship.getFluxTracker().getFluxLevel();
		if (ship.getVariant().hasHullMod("stabilizedshieldemitter")){
			float flux = Math.min(ship.getFluxTracker().getFluxLevel(), 0.75f);
			fluxRatioRes = MathHelper.normalize(flux,0f,0.75f);
		} else fluxRatioRes = ship.getFluxTracker().getFluxLevel();

		int shield = (int)getBaseArc(ship);
		float sizeBonus = Math.round(360 - ((360-shield) * (fluxRatio)));
		sizeBonus = Math.min(360f, sizeBonus);
		ship.getShield().setArc((int)sizeBonus);
		//make shield color change with flux too
		final Color shieldColor = new Color(
				255,
				ColorHelper.clamp255(Math.round(251 - ((250) * (fluxRatio)))),
				ColorHelper.clamp255(Math.round(251 - ((250) * (fluxRatio)))),
				ColorHelper.clamp255(Math.round(175 + ((75 * fluxRatio)))));
		ship.getShield().setInnerColor(shieldColor);
		//tooltip stuff
		float sizeBonusTt = Math.round(sizeBonus);

		float resBonus = 100f * - ((RESISTANCE_BONUS) * (fluxRatioRes));
		float foldBonus = 100f * ((FOLD_BONUS) * (fluxRatioRes));

		ship.getMutableStats().getShieldDamageTakenMult().modifyPercent("nskr_focused_shield2", resBonus);
		ship.getMutableStats().getShieldTurnRateMult().modifyPercent("nskr_focused_shield2", foldBonus);
		ship.getMutableStats().getShieldUnfoldRateMult().modifyPercent("nskr_focused_shield2", foldBonus);
		//tooltip stuff
		float resistanceBonusTt = ship.getShield().getFluxPerPointOfDamage() * ship.getMutableStats().getShieldDamageTakenMult().getModifiedValue();
		//2 decimal round
		resistanceBonusTt = resistanceBonusTt*100f;
		resistanceBonusTt = Math.round(resistanceBonusTt);
		resistanceBonusTt /= 100f;

		if (ship == Global.getCombatEngine().getPlayerShip() && fluxRatio>0f) {
			Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, "shield flux per damage "+ resistanceBonusTt + " shield size " + (int) sizeBonusTt, true);
		}
	}

	public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 10.0f;

		tooltip.addSectionHeading("Additional Info", Alignment.MID, pad);
		if (ship != null && !Global.getSettings().isShowingCodex() && ship.getVariant().hasHullMod("stabilizedshieldemitter")){
			tooltip.addPara("-Stabilized Shields installed.", pad, ColorHelper.NICE_YELLOW, "");
			tooltip.addPara("-Full resistance and fold bonus achieved at 75%% flux instead.", 0.0f, ColorHelper.NICE_YELLOW, "75%");
		} else {
			tooltip.addPara("-Full resistance and fold bonus achieved at 75%% flux instead, if Stabilized Shields is installed.", pad, ColorHelper.NICE_YELLOW, "");
		}
	}

	@Override
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + 360;
		if (index == 1) return ship == null ? "its base size" : "" + (int)getBaseArc(ship);
		if (index == 2) return "" + Math.round(100f * RESISTANCE_BONUS) + "%";
		if (index == 3) return "" + Math.round(100f * FOLD_BONUS) + "%";
		if (index == 4) return "" + 0;

		return null;
	}

	private static float getBaseArc(ShipAPI ship) {
		return ship.getMutableStats().getShieldArcBonus().computeEffective(ship.getHullSpec().getShieldSpec().getArc());
	}

	@Override
	public Color getNameColor() {
		return new Color(231, 124, 138,255);
	}
}