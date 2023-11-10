package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import scripts.kissa.LOST_SECTOR.util.combatUtil;

import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class nskr_kaboomAI implements ShipSystemAIScript {

    private CombatEngineAPI engine = null;
    private ShipAPI ship;
    private final IntervalUtil tracker = new IntervalUtil(0.50f, 1.00f);
    public static final float DEGREES = 140f;

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
            List<ShipAPI> friendlies = new ArrayList<>(100);
            List<ShipAPI> currFriendlies = new ArrayList<>(100);
            ships.addAll(combatUtil.getShipsWithinRange(ship.getLocation(), 800f));
            friendlies.addAll(combatUtil.getShipsWithinRange(ship.getLocation(), 500f));

            for (ShipAPI possibleShip : ships){
            if (possibleShip.getOwner() == ship.getOwner()) continue;
            if (possibleShip.getHullSize() == ShipAPI.HullSize.FIGHTER) continue;

            if (possibleShip.getHullSize() == ShipAPI.HullSize.FRIGATE) {
                decisionLevel += (15f);
            }
            if (possibleShip.getHullSize() == ShipAPI.HullSize.DESTROYER) {
                decisionLevel += (25f);
            }
            if (possibleShip.getHullSize() != ShipAPI.HullSize.DESTROYER && possibleShip.getHullSize() != ShipAPI.HullSize.FRIGATE) {
                decisionLevel += (50f);
            }
            currTargets.add(possibleShip);
            //macgyver debugger
            //engine.addFloatingText(ship.getLocation(), "test", 1f+decisionLevel, Color.cyan, ship, 0.5f, 1.0f);
            }
            if (hullRatio < 0.75f) {
                decisionLevel += 25f;
            }
            if (hullRatio < 0.50f) {
                decisionLevel += 10f;
            }

            float facing = ship.getFacing();
            Vector2f curr = ship.getLocation();
            for (ShipAPI possibleShip : friendlies){
                //only care about allies in front
                if (possibleShip.getOwner() != ship.getOwner()) continue;
                if (possibleShip.getHullSize() == ShipAPI.HullSize.FIGHTER) continue;
                if (possibleShip.getHullSpec().getBaseHullId().equals("nskr_aed")) continue;
                float angle = VectorUtils.getAngle(curr, possibleShip.getLocation());
                // ignore everything outside of a y degree cone
                if (Math.abs(MathUtils.getShortestRotation(angle, facing)) > DEGREES) {
                    continue;
                }
                currFriendlies.add(possibleShip);
            }
            //don't use when nothing is around
            if (currTargets.isEmpty()) decisionLevel = 0;
            //don't use when allies are *that* close
            if (!currFriendlies.isEmpty()) decisionLevel = 0;

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
