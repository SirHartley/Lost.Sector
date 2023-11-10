package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import scripts.kissa.LOST_SECTOR.hullmods.nskr_pullback;
import scripts.kissa.LOST_SECTOR.util.combatUtil;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;

public class nskr_pullbackAI implements ShipSystemAIScript {

    public static final float ANALYSIS_RANGE = 1200f;
    public static final float MIN_DIFF = 300f;
    public static final float DEGREES = 5f;

    private CombatEngineAPI engine = null;
    private ShipwideAIFlags flags;
    private ShipAPI ship;

    // list of flags to check for using TOWARDS target, using AWAY from target, and NOT USING
    public static final ArrayList<ShipwideAIFlags.AIFlags> TOWARDS = new ArrayList<>();
    public static final ArrayList<ShipwideAIFlags.AIFlags> AWAY = new ArrayList<>();
    public static final ArrayList<ShipwideAIFlags.AIFlags> CON = new ArrayList<>();
    static {
        TOWARDS.add(ShipwideAIFlags.AIFlags.PURSUING);
        TOWARDS.add(ShipwideAIFlags.AIFlags.HARASS_MOVE_IN);
        AWAY.add(ShipwideAIFlags.AIFlags.RUN_QUICKLY);
        //AWAY.add(ShipwideAIFlags.AIFlags.TURN_QUICKLY);
        AWAY.add(ShipwideAIFlags.AIFlags.NEEDS_HELP);
        CON.add(ShipwideAIFlags.AIFlags.BACK_OFF);
        CON.add(ShipwideAIFlags.AIFlags.BACK_OFF_MIN_RANGE);
        //CON.add(ShipwideAIFlags.AIFlags.BACKING_OFF);
        //CON.add(ShipwideAIFlags.AIFlags.DO_NOT_PURSUE);
    }
    private final IntervalUtil tracker = new IntervalUtil(0.40f, 0.60f);

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        if (engine.isPaused() || ship.getShipAI() == null) {
            return;
        }
        if (!AIUtils.canUseSystemThisFrame(ship))return;

        nskr_pullback.ShipSpecificData data = (nskr_pullback.ShipSpecificData) Global.getCombatEngine().getCustomData().get("PULLBACK_DATA_KEY" + ship.getId());
        if(data==null)return;

        List<Vector2f> points = new ArrayList<>(100);
        if(data.tPoint1!=null)points.add(data.tPoint1);
        if(data.tPoint2!=null)points.add(data.tPoint2);
        if(data.tPoint3!=null)points.add(data.tPoint3);
        //int x=0;
        if (tracker.intervalElapsed()) {
            for (Vector2f point : points) {
                //x++;
                //engine.addFloatingText(point, "X "+x, 30f, Color.cyan, null, 0.1f, 0.1f);
                boolean use = false;
                float curDist = combatUtil.getNearestEnemyShipDistance(ship, ship.getLocation(), ANALYSIS_RANGE);
                float posDist = combatUtil.getNearestEnemyShipDistance(ship, point, ANALYSIS_RANGE);

                boolean danger = ship.getFluxLevel() > 0.80f;
                if (ship.getFluxLevel() > 0.70f && ship.getHullLevel() < 0.95f) {
                    danger = true;
                }

                boolean safeF = false;
                boolean dangerF = false;
                for (ShipwideAIFlags.AIFlags f : TOWARDS) {
                    if (flags.hasFlag(f)) {
                        safeF = true;
                    }
                }
                for (ShipwideAIFlags.AIFlags f : AWAY) {
                    if (flags.hasFlag(f)) {
                        dangerF = true;
                    }
                }
                for (ShipwideAIFlags.AIFlags f : CON) {
                    if (flags.hasFlag(f)) {
                        dangerF = true;
                    }
                }

                //try to run away
                float diff = curDist - posDist;
                if (diff < -MIN_DIFF && danger || diff < -MIN_DIFF && dangerF) {
                    use = true;
                    //engine.addFloatingText(ship.getLocation(), "RUN AWAY", 30f, Color.cyan, null, 0.1f, 0.1f);
                }
                //try to engage
                if (diff > MIN_DIFF && !danger && safeF) {
                    use = true;
                    //engine.addFloatingText(ship.getLocation(), "ENGAGE", 30f, Color.cyan, null, 0.1f, 0.1f);
                }

                //damage check
                float distDiff = MathUtils.getDistance(ship.getLocation(), point);
                float dmgLevel = 0f;

                List<DamagingProjectileAPI> possibleTargets = new ArrayList<>(100);
                possibleTargets.addAll(combatUtil.getProjectilesWithinRange(ship.getLocation(), 500f));
                possibleTargets.addAll(combatUtil.getMissilesWithinRange(ship.getLocation(), 400f));

                for (DamagingProjectileAPI possibleTarget : possibleTargets) {
                    if (possibleTarget.getOwner() == ship.getOwner() || possibleTarget.isFading() || possibleTarget.getCollisionClass() == CollisionClass.NONE)
                        continue;

                    //check whether proj are in an angle to hit us
                    float facing = VectorUtils.getFacing(possibleTarget.getVelocity());
                    Vector2f curr = possibleTarget.getLocation();
                    float angle = VectorUtils.getAngle(curr, ship.getLocation());
                    //ignore everything outside of a y degree cone
                    if (Math.abs(MathUtils.getShortestRotation(angle, facing)) > DEGREES) continue;

                    //engine.addFloatingText(possibleTarget.getLocation(), "HIT", 30f, Color.cyan, null, 0.1f, 0.1f);

                    if (possibleTarget.getDamageType() == DamageType.FRAGMENTATION) {
                        dmgLevel += 0.25f * possibleTarget.getDamageAmount() + possibleTarget.getEmpAmount() * 0.25f;
                    } else {
                        dmgLevel += possibleTarget.getDamageAmount() + possibleTarget.getEmpAmount() * 0.25f;
                    }
                }

                //try to avoid damage
                if (distDiff > MIN_DIFF && dmgLevel > 1000f && diff < 100f) {
                    use = true;
                    //engine.addFloatingText(ship.getLocation(), "AVOID DMG", 30f, Color.cyan, null, 0.1f, 0.1f);
                }

                if (use){
                    this.ship.useSystem();
                    //immediately recalculate after using the system
                    tracker.setElapsed(0.90f);
                    break;
                }
            }
        }
        tracker.advance(amount);
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
    }
}
