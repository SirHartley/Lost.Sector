package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.CollisionUtils;
import scripts.kissa.LOST_SECTOR.util.combatUtil;

import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class nskr_emBlastAI implements ShipSystemAIScript {

    private CombatEngineAPI engine = null;
    private ShipAPI ship;
    private final IntervalUtil tracker = new IntervalUtil(0.20f, 0.40f);

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        if (engine.isPaused() || ship.getShipAI() == null) {
            return;
        }

        tracker.advance(amount);
        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }

            float decisionLevel = 0f;
            float hullRatio = ship.getHitpoints() / ship.getMaxHitpoints();

            List<ShipAPI> ships = new ArrayList<>(100);
            List<ShipAPI> currTargets = new ArrayList<>(100);
            ships.addAll(combatUtil.getShipsWithinRange(ship.getLocation(), 350f));

            for (ShipAPI possibleShip : ships){
                if (possibleShip.getOwner() == ship.getOwner()) continue;
                if (possibleShip.getHullSize()== ShipAPI.HullSize.FIGHTER) {
                    decisionLevel += 1.7f;
                } else {
                    decisionLevel += 5f;
                }

                currTargets.add(possibleShip);
                //macgyver debugger
                //engine.addFloatingText(ship.getLocation(), "test", 1f+decisionLevel, Color.cyan, ship, 0.5f, 1.0f);
            }
            if (hullRatio < 0.30f) {
                decisionLevel += 29f;
            } else if (hullRatio < 0.50f) {
                decisionLevel += 25f;
            }

            //don't use when nothing is around
            if (currTargets.isEmpty()) return;

            //don't use inside ships
            if (AIUtils.getNearestShip(ship)!=null) {
                if (CollisionUtils.isPointWithinBounds(ship.getLocation(), AIUtils.getNearestShip(ship))) return;
            }

            if (decisionLevel >= 30f) {
                ship.useSystem();
            }
        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
    }
}
