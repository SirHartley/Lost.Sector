//////////////////////
//script partially based on code by Vayra, from Kadur
//////////////////////
package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;

import java.util.ArrayList;
import java.util.Objects;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class nskr_warpAI implements ShipSystemAIScript {

    //bunch of angle math, so we use system in the right direction
    //otherwise standard movement AI

    private ShipAPI ship;
    private ShipwideAIFlags flags;
    private CombatEngineAPI engine;
    public static final Vector2f ZERO = new Vector2f();
    private float dist;
    private float vAngle;

    // only check every half-second (for optimization and, hopefully, synchronization)
    private final IntervalUtil timer = new IntervalUtil(0.5f, 0.7f);

    // setup
    public static final float DEGREES = 69f; // (haha nice)

    // list of flags to check for using TOWARDS target, using AWAY from target, and NOT USING
    public static final ArrayList<AIFlags> TOWARDS = new ArrayList<>();
    public static final ArrayList<AIFlags> AWAY = new ArrayList<>();
    public static final ArrayList<AIFlags> CON = new ArrayList<>();
    static {
        TOWARDS.add(AIFlags.PURSUING);
        TOWARDS.add(AIFlags.HARASS_MOVE_IN);
        AWAY.add(AIFlags.RUN_QUICKLY);
        AWAY.add(AIFlags.NEEDS_HELP);
        CON.add(AIFlags.BACK_OFF);
        CON.add(AIFlags.BACK_OFF_MIN_RANGE);
        CON.add(AIFlags.BACKING_OFF);
    }
    private Vector2f sLoc;
    private boolean forwards;
    private boolean backwards;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
    }

    // method to check if we're facing within X degrees of target
    private boolean rightDirection(ShipAPI ship, Vector2f targetLocation) {
        Vector2f curr = ship.getLocation();
        Vector2f sVel = ship.getVelocity();
        if (sVel == null) sVel = ZERO;
        float sAngle;
        sAngle = ship.getFacing();
        vAngle = VectorUtils.getFacing(sVel);
        dist = Objects.requireNonNull(ship.getVelocity()).length();
        if (dist == 0f) vAngle = sAngle;
        float angleToTarget = VectorUtils.getAngle(curr, targetLocation);
        return (Math.abs(MathUtils.getShortestRotation(angleToTarget, vAngle)) <= DEGREES);
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        // don't check if paused
        if (engine.isPaused()) {
            return;
        }

        // don't check if timer not up
        timer.advance(amount);
        if (timer.intervalElapsed()) {

            // don't use if can't use
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }

            //ANGLE TIME
            forwards = false;
            backwards = false;
            sLoc = ship.getLocation();
            Vector2f sVel = ship.getVelocity();
            if (sVel == null) sVel = ZERO;
            float sAngle;
            sAngle = ship.getFacing();
            vAngle = VectorUtils.getFacing(sVel);
            dist = Objects.requireNonNull(ship.getVelocity()).length();
            if (dist == 0f) vAngle = sAngle;

            float diff = vAngle - sAngle;
            if (diff<0) diff *= -1f;
            if ((sVel.length()>0f) && (diff > 0)) {

                if (diff<60) forwards = true;
                if (diff<360&&diff>300) forwards = true;
                if (diff<270&&diff>90) backwards = true;

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
                if (flags.hasFlag(f)&&forwards) {
                    useMe = true;
                }
            }

            for (AIFlags f : AWAY) {
                if (flags.hasFlag(f)&&backwards) {
                    useMe = true;
                }
            }

            for (AIFlags f : CON) {
                if (flags.hasFlag(f)&&backwards) {
                    useMe = true;
                }
            }

            if (useMe) {
                ship.useSystem();
            }
        }
    }
}
