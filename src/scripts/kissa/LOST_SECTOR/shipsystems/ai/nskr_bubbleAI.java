package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.combatUtil;

import java.util.ArrayList;
import java.util.List;

public class nskr_bubbleAI implements ShipSystemAIScript {

    //avoid these projectiles
    public static final float DEGREES = 5f;
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipwideAIFlags flags;
    private final IntervalUtil timer = new IntervalUtil(0.30f, 0.50f);

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
        if (ship.getShield()==null){
            return;
        }

        float decisionLevel = 0f;
        timer.advance(amount);
        if (timer.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }
            boolean flux = ship.getFluxLevel() > 0.30;
            if ((flux && flags.hasFlag(ShipwideAIFlags.AIFlags.BACKING_OFF))) {
                decisionLevel += 20f;
            }
            if (ship.getSystem().getAmmo()>1){
                decisionLevel += 50f;
            }
            //combat check
            boolean combat = false;
            List<ShipAPI> threats = CombatUtils.getShipsWithinRange(ship.getLocation(), 900f);
            for (ShipAPI t : threats){
                if (t.getOwner()==ship.getOwner()) continue;
                if (t.getHullSize()== ShipAPI.HullSize.FIGHTER) continue;
                combat = true;
            }
            float fluxL, mult;
            if (combat){
                fluxL = 80f;
                mult = 7f;
            } else{
                fluxL = 40f;
                mult = 14f;
            }
            decisionLevel += fluxL*ship.getFluxLevel();

            List<DamagingProjectileAPI> possibleTargets = new ArrayList<>(100);
            possibleTargets.addAll(combatUtil.getProjectilesWithinRange(ship.getLocation(), 500f));
            possibleTargets.addAll(combatUtil.getMissilesWithinRange(ship.getLocation(), 400f));
            int enemyProj = 0;
            for (DamagingProjectileAPI possibleTarget : possibleTargets) {
                if (possibleTarget.getOwner() == ship.getOwner() || possibleTarget.isFading() || possibleTarget.getCollisionClass() == CollisionClass.NONE) continue;
                float damageLevel = 0f;

                //check whether proj are in an angle to hit us
                float facing = VectorUtils.getFacing(possibleTarget.getVelocity());
                Vector2f curr = possibleTarget.getLocation();
                float angle = VectorUtils.getAngle(curr, ship.getLocation());
                //ignore everything outside of a y degree cone
                if (Math.abs(MathUtils.getShortestRotation(angle, facing)) > DEGREES) continue;

                //engine.addFloatingText(possibleTarget.getLocation(), "HIT", 30f, Color.cyan, null, 0.1f, 0.1f);

                if (possibleTarget.getDamageType() == DamageType.FRAGMENTATION) {
                    damageLevel += 0.25f * possibleTarget.getDamageAmount();
                }
                if (possibleTarget.getDamageType() == DamageType.HIGH_EXPLOSIVE) {
                    damageLevel += 0.50 * possibleTarget.getDamageAmount();
                }
                if (possibleTarget.getDamageType() == DamageType.KINETIC) {
                    damageLevel += 2f * possibleTarget.getDamageAmount();
                }
                else {
                    damageLevel += possibleTarget.getDamageAmount();
                }
            decisionLevel += damageLevel/mult;
            enemyProj++;
            }
            //fuck salamanders, all my homies hate salamanders
            for (MissileAPI m : combatUtil.getMissilesWithinRange(ship.getLocation(), 250f)){
                if (m.getOwner() == ship.getOwner() || m.isFading() || m.getCollisionClass() == CollisionClass.NONE) continue;
                if (m.getWeapon()==null) continue;
                if (m.getWeapon().hasAIHint(WeaponAPI.AIHints.HEATSEEKER)) decisionLevel += 80f;
            }

            //macgyver debugger
            //engine.addFloatingText(ship.getLocation(), "test " + (int)decisionLevel, 32f, Color.cyan, ship, 0.5f, 1.0f);

            if (decisionLevel >= 110f*MathUtils.getRandomNumberInRange(0.85f,1.15f) && enemyProj>0) {
                ship.useSystem();
            }
        }
    }
}
