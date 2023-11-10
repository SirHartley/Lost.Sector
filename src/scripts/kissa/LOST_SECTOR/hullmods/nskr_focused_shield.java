package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import org.magiclib.util.MagicIncompatibleHullmods;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class nskr_focused_shield extends BaseHullMod {

	public static final float RESISTANCE_BONUS = 0.50f;
	public static final float FOLD_BONUS = 1.00f;

	public static final String INNER_LARGE = "graphics/fx/nskr_protShield.png";
	//public static final String INNER_LARGE = "graphics/fx/shields256.png";

	public static final String MOD_ICON = "graphics/icons/hullsys/fortress_shield.png";

	public static final String MOD_BUFFID = "nskr_focused_shield";
	public static final String MOD_NAME = "Adaptive Shield Projector";
	private float baseShield = 0f;
	private boolean loaded = false;

	public static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
	static {
		// These hullmods will automatically be removed
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return false;
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if (ship.getShield()!=null){
			baseShield = ship.getShield().getArc();
		}
		if (ship.getHullSize()!=HullSize.FIGHTER) {
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
			fluxRatioRes = mathUtil.normalize(flux,0f,0.75f);
		} else fluxRatioRes = ship.getFluxTracker().getFluxLevel();

		int shield = (int)ship.getMutableStats().getShieldArcBonus().computeEffective(ship.getHullSpec().getShieldSpec().getArc());
		float sizeBonus = Math.round(360 - ((360-shield) * (fluxRatio)));
		sizeBonus = Math.min(360f, sizeBonus);
		ship.getShield().setArc((int)sizeBonus);
		//make shield color change with flux too
		final Color shieldColor = new Color(
				255,
				util.clamp255(Math.round(251 - ((250) * (fluxRatio)))),
				util.clamp255(Math.round(251 - ((250) * (fluxRatio)))),
				//util.clamp255(Math.round(-50.54f)),
				//util.clamp255(Math.round(500.65f)),
				util.clamp255(Math.round(175 + ((75 * fluxRatio)))));
		ship.getShield().setInnerColor(shieldColor);
		//tooltip stuff
		float sizeBonusTt = Math.round(sizeBonus);

		float resBonus = 100f * - ((RESISTANCE_BONUS) * (fluxRatioRes));
		float foldBonus = 100f * ((FOLD_BONUS) * (fluxRatioRes));

		//Global.getCombatEngine().addFloatingText(ship.getLocation()," size " + (int)sizeBonus + " resBonus " + (int)resBonus + " foldBonus " + (int)foldBonus, 40f, Color.RED, ship, 0.5f, 1.0f);

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
		if (ship.getVariant().hasHullMod("stabilizedshieldemitter")){
			tooltip.addPara("-Stabilized Shields installed.", pad, util.NICE_YELLOW, "");
			tooltip.addPara("-Full resistance and fold bonus achieved at 75%% flux instead.", 0.0f, util.NICE_YELLOW, "75%");
		} else {
			tooltip.addPara("-Full resistance and fold bonus achieved at 75%% flux instead, if Stabilized Shields is installed.", pad, util.NICE_YELLOW, "");
		}
		//tooltip.addPara("", 0.0f, Color.GREEN, new String[]{""});
		//tooltip.addPara("-Prevents installation of certain hullmods.", 0.0f, Color.GREEN, new String[]{""});
		//tooltip.addPara("-Shield Conversion - Front, Shield Shunt.", 0.0f, util.TT_ORANGE, new String[]{"Shield Conversion - Front, Shield Shunt"});
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + 360;
		if (index == 1) return "" + (int)baseShield;
		if (index == 2) return "" + Math.round(100f * RESISTANCE_BONUS) + "%";
		if (index == 3) return "" + Math.round(100f * FOLD_BONUS) + "%";
		if (index == 4) return "" + 0;

		return null;
	}

	@Override
	public Color getNameColor() {
		return new Color(231, 124, 138,255);
	}
}