package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.shipsystems.nskr_projfieldStats;

public class nskr_projfieldAI implements ShipSystemAIScript {

    //avoid these projectiles
    public static final float DEGREES = 5f;
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipwideAIFlags flags;
    private final IntervalUtil timer = new IntervalUtil(0.40f, 0.70f);

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
                decisionLevel += 30f;
            }
            if ((ship.getHullLevel()<0.90f)) {
                decisionLevel += 10f;
            }
            if (ship.getSystem().getAmmo()>1){
                decisionLevel += 25f;
            }

            for (CombatEntityAPI possibleTarget : CombatUtils.getEntitiesWithinRange(ship.getLocation(), nskr_projfieldStats.getMaxRange(ship))){
                if (possibleTarget.getOwner() == ship.getOwner() || possibleTarget.getCollisionClass() == CollisionClass.NONE)
                    continue;
                if (possibleTarget instanceof DamagingProjectileAPI) {
                    DamagingProjectileAPI proj = (DamagingProjectileAPI) possibleTarget;
                    if (proj.isExpired()) continue;

                    //check whether proj are in an angle to hit us
                    float facing = VectorUtils.getFacing(possibleTarget.getVelocity());
                    Vector2f curr = possibleTarget.getLocation();
                    float angle = VectorUtils.getAngle(curr, ship.getLocation());
                    //ignore everything outside of a y degree cone
                    if (Math.abs(MathUtils.getShortestRotation(angle, facing)) > DEGREES) continue;

                    //engine.addFloatingText(possibleTarget.getLocation(), "HIT", 30f, Color.cyan, null, 0.1f, 0.1f);
                    decisionLevel += 2.5f*((float) Math.sqrt(proj.getDamageAmount() + proj.getEmpAmount() * 0.25f));
                }
                if (possibleTarget instanceof ShipAPI){
                    ShipAPI fighter = (ShipAPI) possibleTarget;
                    if (fighter.getHullSize()!=ShipAPI.HullSize.FIGHTER) continue;
                    decisionLevel += 15f;
                }
            }
            //macgyver debugger
            //engine.addFloatingText(ship.getLocation(), "test" + (int)decisionLevel, 1f+decisionLevel, Color.cyan, ship, 0.5f, 1.0f);

            if (decisionLevel >= 90f*MathUtils.getRandomNumberInRange(0.95f,1.10f)) {
                ship.useSystem();
            }
        }
    }
}
