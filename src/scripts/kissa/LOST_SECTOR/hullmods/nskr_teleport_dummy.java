package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import org.magiclib.util.MagicRender;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.shipsystems.nskr_warpStats;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.io.IOException;

public class nskr_teleport_dummy extends BaseHullMod {

	public static final Color PROT_COLOR = new Color(175, 197, 255, 100);
	public static final Color ENIGMA_COLOR = new Color(255, 182, 188, 100);
	public static final float ACTIVE_RANGE = 50f;
	//In-script variables
	public static final Vector2f ZERO = new Vector2f();
	private boolean loaded = false;
	private SpriteAPI sprite = null;
	public static final String SPRITE_PATH = "graphics/fx/nskr_select.png";
	private Vector2f tPoint;
	float angle = 0f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		boolean player = false;
		player = ship == Global.getCombatEngine().getPlayerShip();
		CombatEngineAPI engine = Global.getCombatEngine();
		ShipSystemAPI.SystemState state = ship.getSystem().getState();

		Color color;
		if (util.protOrEnigma(ship).equals("enigma")){
			color = ENIGMA_COLOR;
		} else{
			color = PROT_COLOR;
		}

		if (engine.isPaused() || !player) return;

		if (ship.getSystem().getAmmo()>0){
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

			//engine.addFloatingText(sLoc, "test " + (int)vAngle +","+ (int)sAngle +","+ (int)diff +","+ diffMult, 30f, Color.cyan, ship, 0.5f, 1.0f);

			tPoint = nskr_warpStats.teleportPoint(ship);

			//DRAW SPRITE
				Vector2f size = new Vector2f(ACTIVE_RANGE*2f, ACTIVE_RANGE*2f);
				//speen
				angle += amount*40f;
				if (angle>360) angle = 0f;
				if (engine.isUIShowingHUD()) {
					MagicRender.singleframe(sprite, tPoint, size, angle, color, false);
				}
			}
		}

	public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
}
