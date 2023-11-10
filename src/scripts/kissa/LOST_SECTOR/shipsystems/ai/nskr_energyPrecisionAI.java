//////////////////////
//Parts initially created by theDragn and modified from HTE
//////////////////////
package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import scripts.kissa.LOST_SECTOR.shipsystems.nskr_energyPrecisionStats;


public class nskr_energyPrecisionAI implements ShipSystemAIScript {

    private CombatEngineAPI engine = null;
    private ShipAPI ship;
    private final IntervalUtil tracker = new IntervalUtil(0.30f, 0.50f);
    public static final float DEGREES = 69f; // (haha nice)
    private boolean runOnce = false;
    private boolean flagged = false;
    public static final ArrayList<ShipwideAIFlags.AIFlags> PURSUE = new ArrayList<>();
    static {
        PURSUE.add(ShipwideAIFlags.AIFlags.PURSUING);
        PURSUE.add(ShipwideAIFlags.AIFlags.HARASS_MOVE_IN);
    }
    private List<WeaponAPI> weapons=new ArrayList<>();

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        if (engine.isPaused() || ship.getShipAI() == null) {
            return;
        }

        if(!runOnce){
            runOnce=true;
            weapons=ship.getAllWeapons();
        }
        //flag manip
        if (ship.getSystem().isCoolingDown()){
            if (!ship.getShipAI().getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.BACK_OFF) && !flagged) {
                ship.getShipAI().getAIFlags().setFlag(ShipwideAIFlags.AIFlags.BACK_OFF, 5f);
                //engine.addFloatingText(ship.getLocation(), "test " + "added flag", 60f, Color.cyan, ship, 0.5f, 1.0f);
            }
            flagged = true;
        } else if (flagged) {
            ship.getShipAI().getAIFlags().removeFlag(ShipwideAIFlags.AIFlags.BACK_OFF);
            //engine.addFloatingText(ship.getLocation(), "test " + "removed flag", 60f, Color.cyan, ship, 0.5f, 1.0f);
            flagged = false;
        }

        tracker.advance(amount);
        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }

            float useLevel = 0f;

            float analysisRange = this.getLongestRange(this.ship) + nskr_energyPrecisionStats.RANGE_BONUS;

            //use when in range
            List<ShipAPI> ships = new ArrayList<>(100);
            ships.addAll(AIUtils.getNearbyEnemies(ship, analysisRange));
            for (ShipAPI possibleship : ships) {
                Vector2f curr = ship.getLocation();
                float facing = ship.getFacing();
                float angle = VectorUtils.getAngle(curr, possibleship.getLocation());
                //only count targets infront
                if (Math.abs(MathUtils.getShortestRotation(angle, facing)) > DEGREES) {
                    continue;
                }
                if (possibleship.getOwner() != ship.getOwner() && possibleship.getHullSize() != ShipAPI.HullSize.FIGHTER) {
                        useLevel += 50f;
                }
            }
            //don't use when close
            List<ShipAPI> shipsClose = new ArrayList<>(100);
            shipsClose.addAll(AIUtils.getNearbyEnemies(ship, this.getLongestRange(this.ship)));
            for (ShipAPI possibleshipClose : shipsClose) {
                if (possibleshipClose.getOwner() != ship.getOwner() && possibleshipClose.getHullSize() != ShipAPI.HullSize.FIGHTER){
                        useLevel -= 50f;
                }
            }

            //macgyver debugger
            //engine.addFloatingText(ship.getLocation(), "use" + Math.round(useLevel) + "range" + Math.round(analysisRange), 32f, Color.cyan, ship, 0.5f, 1.0f);

            if (useLevel >= 50f) {
                this.ship.useSystem();
            }
        }
    }

    private float getLongestRange(ShipAPI ship) {
        float longestRange = 0f;
        WeaponAPI.WeaponSize largestWeaponSize = WeaponAPI.WeaponSize.SMALL;

        for (WeaponAPI weapon : weapons) {
            if (largestWeaponSize == WeaponAPI.WeaponSize.SMALL && weapon.getSize() != largestWeaponSize && weapon.getType() != WeaponAPI.WeaponType.BALLISTIC && weapon.getType() != WeaponAPI.WeaponType.MISSILE){
                largestWeaponSize = weapon.getSize();
            }
            if (largestWeaponSize != WeaponAPI.WeaponSize.MEDIUM || weapon.getSize() != WeaponAPI.WeaponSize.LARGE) continue;
            largestWeaponSize = WeaponAPI.WeaponSize.LARGE;
        }

        for (WeaponAPI weapon : weapons) {
            float range = 0f;
            if (weapon.getType() == WeaponAPI.WeaponType.BALLISTIC || weapon.getType() == WeaponAPI.WeaponType.MISSILE || weapon.getSize() != largestWeaponSize || weapon.hasAIHint(WeaponAPI.AIHints.PD) || !((range = weapon.getRange()) > longestRange)) continue;
            longestRange = range;
        }
        return longestRange;
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
    }
}
