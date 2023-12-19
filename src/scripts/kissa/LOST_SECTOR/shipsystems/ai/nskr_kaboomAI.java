package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import scripts.kissa.LOST_SECTOR.util.combatUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class nskr_kaboomAI implements ShipSystemAIScript {

    private CombatEngineAPI engine = null;
    private ShipAPI ship;
    private final IntervalUtil tracker = new IntervalUtil(0.50f, 1.00f);
    //public static final float DEGREES = 150f;

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

            List<ShipAPI> currTargets = new ArrayList<>();
            List<ShipAPI> ships = new ArrayList<>(combatUtil.getShipsWithinRange(ship.getLocation(), 1000f));
            List<ShipAPI> friendlies = new ArrayList<>(combatUtil.getShipsWithinRange(ship.getLocation(), 1000f));

            for (ShipAPI possibleShip : ships){
                if (possibleShip.getOwner() == ship.getOwner()) continue;
                if (possibleShip.getHullSize() == ShipAPI.HullSize.FIGHTER) continue;

                switch (possibleShip.getHullSize()){
                    case FRIGATE:
                        decisionLevel += 10f;
                        break;
                    case DESTROYER:
                        decisionLevel += 15f;
                        break;
                    case CRUISER:
                        decisionLevel += 25f;
                        break;
                    case CAPITAL_SHIP:
                        decisionLevel += 50f;
                        break;
                }
                currTargets.add(possibleShip);
            }
            if (hullRatio < 0.75f) {
                decisionLevel += 25f;
            }
            if (hullRatio < 0.50f) {
                decisionLevel += 10f;
            }

            for (ShipAPI possibleShip : friendlies){
                if (possibleShip.getOwner() != ship.getOwner()) continue;
                if (possibleShip.getHullSize() == ShipAPI.HullSize.FIGHTER) continue;
                if (possibleShip.getHullSpec().getBaseHullId().equals("nskr_aed")) continue;
                decisionLevel -= 15f;
            }
            //don't use when nothing is around
            if (currTargets.isEmpty()) decisionLevel = 0;

            //macgyver debugger
            //engine.addFloatingText(ship.getLocation(), "weight " +(int)decisionLevel, 24f, Color.cyan, ship, 0.5f, 1.0f);

            if (decisionLevel >= 40f) {
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
