package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.combatUtil;

import java.util.ArrayList;
import java.util.List;

public class nskr_zhonyaAI implements ShipSystemAIScript {

    //avoid these projectiles
    public static final float DEGREES = 5f;
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipwideAIFlags flags;
    private final IntervalUtil timer = new IntervalUtil(0.50f, 0.80f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
        this.flags = flags;
        timer.randomize();
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        if (engine.isPaused() || ship.getShipAI() == null) {
            return;
        }
        float decisionLevel = 0f;
        timer.advance(amount);
        if (timer.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }
            boolean flux = ship.getFluxLevel() > 0.50;
            if ((flux && flags.hasFlag(ShipwideAIFlags.AIFlags.BACKING_OFF))) {
                decisionLevel += 40f;
            }
            if ((ship.getHullLevel()<0.90f)) {
                decisionLevel += 20f;
            }
            decisionLevel += 120f*ship.getFluxLevel();

            List<DamagingProjectileAPI> possibleTargets = new ArrayList<>(100);
            possibleTargets.addAll(combatUtil.getProjectilesWithinRange(ship.getLocation(), 500f));
            possibleTargets.addAll(combatUtil.getMissilesWithinRange(ship.getLocation(), 400f));

            for (DamagingProjectileAPI possibleTarget : possibleTargets) {
                if (possibleTarget.getOwner() == ship.getOwner() || possibleTarget.isFading() || possibleTarget.getCollisionClass() == CollisionClass.NONE) continue;

                //check whether proj are in an angle to hit us
                float facing = VectorUtils.getFacing(possibleTarget.getVelocity());
                Vector2f curr = possibleTarget.getLocation();
                float angle = VectorUtils.getAngle(curr, ship.getLocation());
                //ignore everything outside of a y degree cone
                if (Math.abs(MathUtils.getShortestRotation(angle, facing)) > DEGREES) continue;

                //engine.addFloatingText(possibleTarget.getLocation(), "HIT", 30f, Color.cyan, null, 0.1f, 0.1f);

                if (possibleTarget.getDamageType() == DamageType.FRAGMENTATION) {
                    decisionLevel += (float) Math.sqrt(0.25f * possibleTarget.getDamageAmount() + possibleTarget.getEmpAmount() * 0.25f);
                }
                else {
                    decisionLevel += (float) Math.sqrt(possibleTarget.getDamageAmount() + possibleTarget.getEmpAmount() * 0.25f);
                }
                //macgyver debugger
                //engine.addFloatingText(ship.getLocation(), "test", 1f+decisionLevel, Color.cyan, ship, 0.5f, 1.0f);
            }
            if (decisionLevel >= 125f*MathUtils.getRandomNumberInRange(0.85f,1.15f)) {
                ship.useSystem();
            }
        }
    }
}
