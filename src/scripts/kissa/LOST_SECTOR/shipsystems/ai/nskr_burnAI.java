//////////////////////
//script partially based on code by Vayra, from Kadur
//////////////////////
package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;

import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.shipsystems.nskr_distortionBurnStats;
import scripts.kissa.LOST_SECTOR.util.combatUtil;

public class nskr_burnAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private ShipwideAIFlags flags;
    private CombatEngineAPI engine;

    // only check every half-second (for optimization and, hopefully, synchronization)
    private final IntervalUtil timer = new IntervalUtil(0.5f, 0.7f);
    // setup
    public static final float DEGREES = 75f;
    public static final float SCAN_RANGE = 1000f; // how far ahead of us to scan for (and avoid) targets

    private List<WeaponAPI> weapons=new ArrayList<>();
    // list of flags to check for using TOWARDS target, using AWAY from target, and NOT USING
    public static final ArrayList<AIFlags> TOWARDS = new ArrayList<>();
    public static final ArrayList<AIFlags> AWAY = new ArrayList<>();
    public static final ArrayList<AIFlags> CON = new ArrayList<>();
    static {
        TOWARDS.add(AIFlags.PURSUING);
        TOWARDS.add(AIFlags.HARASS_MOVE_IN);
        AWAY.add(AIFlags.RUN_QUICKLY);
        AWAY.add(AIFlags.TURN_QUICKLY);
        AWAY.add(AIFlags.NEEDS_HELP);
        CON.add(AIFlags.BACK_OFF);
        CON.add(AIFlags.BACK_OFF_MIN_RANGE);
        CON.add(AIFlags.BACKING_OFF);
        CON.add(AIFlags.DO_NOT_PURSUE);
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
    }

    // method to check if we're facing within X degrees of target
    private boolean rightDirection(ShipAPI ship, Vector2f targetLocation) {
        Vector2f curr = ship.getLocation();
        float angleToTarget = VectorUtils.getAngle(curr, targetLocation);
        return (Math.abs(MathUtils.getShortestRotation(angleToTarget, ship.getFacing())) <= 40f);
    }

    private boolean nothingCanStopMe(ShipAPI ship) {
        // setup
        Vector2f curr = ship.getLocation();
        float facing = ship.getFacing();
        boolean lFlux = ship.getFluxLevel() > 0.30;
        boolean flux = ship.getFluxLevel() > 0.50;
        boolean safe = true;
        int ships = 0;

        float lRange = getLongestRange(ship);
        float enemy = getNearestMajorEnemyShipDistance(ship, ship.getLocation(), SCAN_RANGE);
        boolean inRange = lRange > enemy;

        // scan everything within x range
        List<ShipAPI> consider = combatUtil.getShipsWithinRange(curr, SCAN_RANGE);
        for (ShipAPI test : consider) {
            if (test == ship) continue;
            if (test.getOwner() == ship.getOwner()) continue;
            if (test.getHullSize() == ShipAPI.HullSize.FIGHTER) continue;

            float angle = VectorUtils.getAngle(curr, test.getLocation());
            // ignore everything outside of a y degree cone
            if (Math.abs(MathUtils.getShortestRotation(angle, facing)) > DEGREES) continue;

            HullSize size = ship.getHullSize();
            HullSize otherSize = test.getHullSize();
            int diff = size.compareTo(otherSize);
            //engine.addFloatingText(test.getLocation(),"size diff " + diff, 24f, Color.red, test, 1f,1f);

            //log("compareToOther "+test.getName()+" "+otherSize.compareTo(size));
            if (inRange && diff >= 0){
                safe = false;
            }
            if (inRange && lFlux){
                safe = false;
            }
            if (flux && diff >= 0){
                safe = false;
            }
            ships++;
        }

        if (ships>=3){
            safe = false;
        }
        if (ships>=1 && flux){
            safe = false;
        }

        //if (!safe) engine.addFloatingText(ship.getLocation(),"unsafe " + lRange + " , " + enemy, 24f, Color.red, ship, 1f,1f);
        return safe;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        // don't check if paused
        if (engine.isPaused()) {
            return;
        }

        if (weapons != null) {
            weapons = ship.getAllWeapons();
        }

        // don't check if timer not up
        timer.advance(amount);
        if (timer.intervalElapsed()) {


            // don't use if can't use
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }

            // don't use if unsafe
            if (!nothingCanStopMe(ship)) {
                return;
            }

            //macgyver debugger
            //engine.addFloatingText(ship.getLocation(), "nothingCanStopMe", 30f, Color.cyan, ship, 0.5f, 1.0f);

            // setup variables
            boolean useMe = false;
            Vector2f targetLocation = null;
            AssignmentInfo assignment = engine.getFleetManager(ship.getOwner()).getTaskManager(ship.isAlly()).getAssignmentFor(ship);
            float speed = 500f;

            // First priority: use to retreat if ordered to retreat. Overrides/ignores the "useMe" system and AI flag checks.
            if (assignment != null && assignment.getType() == CombatAssignmentType.RETREAT) {
                if (ship.getOwner() == 1 || (ship.getOwner() == 0 && engine.getFleetManager(FleetSide.PLAYER).getGoal() == FleetGoal.ESCAPE)) {
                    targetLocation = new Vector2f(ship.getLocation().x, ship.getLocation().y + 800f); // if ship is enemy OR in "escape" type battle, target loc is UP
                } else {
                    targetLocation = new Vector2f(ship.getLocation().x, ship.getLocation().y - 800f); // if ship is player's, target loc is DOWN
                }
                if (rightDirection(ship, targetLocation)) {
                    ship.useSystem();
                }

                return;  // prevents the AI from activating the ship's system while retreating and facing the wrong direction
                // thanks, Starsector forums user Morathar
            }

            // if we have an assignment, set our target loc to it
            // otherwise, if we have a hostile target, set our target loc to intercept it
            if (assignment != null && assignment.getTarget() != null) {
                targetLocation = assignment.getTarget().getLocation();
            } else if (target != null && target.getOwner() != ship.getOwner()) {
                targetLocation = AIUtils.getBestInterceptPoint(ship.getLocation(), ship.getVelocity().length() + speed, target.getLocation(), target.getVelocity());
            }

            if (targetLocation == null) {
                return;
            } else if (rightDirection(ship, targetLocation)) {
                useMe = true;
            }

            for (AIFlags f : TOWARDS) {
                if (flags.hasFlag(f) && rightDirection(ship, targetLocation)) {
                    useMe = true;
                }
            }

            for (AIFlags f : CON) {
                if (flags.hasFlag(f)) {
                    useMe = false;
                }
            }

            for (AIFlags f : AWAY) {
                if (flags.hasFlag(f)) {
                    useMe = false;
                }
            }
            boolean flux = ship.getFluxLevel() > 0.60;
            //anti salamander
            for (MissileAPI m : combatUtil.getMissilesWithinRange(ship.getLocation(), 350f)){
                if (m.getOwner() == ship.getOwner() || m.isFading() || m.getCollisionClass() == CollisionClass.NONE) continue;
                if (m.getWeapon()==null) continue;
                float angle = VectorUtils.getAngle(ship.getLocation(), m.getLocation());
                // ignore everything outside of a y degree cone
                if (Math.abs(MathUtils.getShortestRotation(angle, ship.getFacing())) < 140f) continue;
                if (m.getWeapon().hasAIHint(WeaponAPI.AIHints.HEATSEEKER) && !flux) useMe = true;
            }
            //anti-fighter / flank
            int targetCount = 0;
            for (ShipAPI s : combatUtil.getShipsWithinRange(ship.getLocation(), nskr_distortionBurnStats.ARC_MAX_RANGE)){
                if (s.getOwner() == ship.getOwner() || s.getCollisionClass() == CollisionClass.NONE) continue;
                float angle = VectorUtils.getAngle(ship.getLocation(), s.getLocation());
                // ignore everything outside of a y degree cone
                if (Math.abs(MathUtils.getShortestRotation(angle, ship.getFacing())) < 140f) continue;

                targetCount++;
            }
            if (targetCount>=2) useMe = true;

            if (useMe) {
                ship.useSystem();
            }
        }
    }

    private float getLongestRange(ShipAPI ship) {
        float longestRange = 0.0f;
        WeaponAPI.WeaponSize largestWeaponSize = WeaponAPI.WeaponSize.SMALL;

        for (WeaponAPI weapon : weapons) {
            WeaponAPI.WeaponSize size = weapon.getSize();
            if (largestWeaponSize == WeaponAPI.WeaponSize.SMALL && weapon.getSize() != largestWeaponSize) {
                largestWeaponSize = weapon.getSize();
            }
            if (largestWeaponSize != WeaponAPI.WeaponSize.MEDIUM || weapon.getSize() != WeaponAPI.WeaponSize.LARGE) continue;
            largestWeaponSize = WeaponAPI.WeaponSize.LARGE;
        }

        for (WeaponAPI weapon : weapons) {
            float range;
            if (weapon.getType() == WeaponAPI.WeaponType.MISSILE || weapon.getSize() != largestWeaponSize || weapon.hasAIHint(WeaponAPI.AIHints.PD) || !((range = weapon.getRange()) > longestRange)) continue;
            longestRange = range;
        }
        if (longestRange < 100.0f) {
            longestRange = 600.0f * ship.getMutableStats().getEnergyWeaponRangeBonus().computeEffective(1.0f);
        }
        return longestRange;
    }

    private float getNearestMajorEnemyShipDistance(ShipAPI ship, Vector2f point, float range){
        float dist = range;
        List<ShipAPI> targets = CombatUtils.getShipsWithinRange(point, range);
        for (ShipAPI t : targets){
            float angle = VectorUtils.getAngle(ship.getLocation(), t.getLocation());
            // ignore everything outside of a y degree cone
            if (Math.abs(MathUtils.getShortestRotation(angle, ship.getFacing())) > DEGREES) continue;

            if (t.getOwner()==ship.getOwner()) continue;
            if (t.getHullSize()==HullSize.FIGHTER) continue;
            if (t.getHullSize()==HullSize.FRIGATE) continue;

            float tDist = MathUtils.getDistance(point, t.getLocation());
            if(tDist>=range) continue;
            dist = Math.min(dist, tDist);
        }
        return dist;
    }

}
