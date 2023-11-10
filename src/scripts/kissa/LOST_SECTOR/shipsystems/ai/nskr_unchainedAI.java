
package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import scripts.kissa.LOST_SECTOR.util.combatUtil;

import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class nskr_unchainedAI implements ShipSystemAIScript {

    //avoid these projectiles
    public static final float DEGREES = 10f;
    private CombatEngineAPI engine = null;
    private ShipAPI ship;
    private ShipSystemAPI system;

    private boolean runOnce = false;

    private List<WeaponAPI> weapons=new ArrayList<>();
    private List<ShipEngineControllerAPI.ShipEngineAPI> engines=new ArrayList<>();

    private final IntervalUtil timer = new IntervalUtil(0.25f, 0.50f);

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        if (engine.isPaused() || ship.getShipAI() == null) {
            return;
        }

        if(!runOnce){
            runOnce=true;
            weapons=ship.getAllWeapons();
            engines=ship.getEngineController().getShipEngines();
        }

        timer.advance(amount);

        if (timer.intervalElapsed()) {


            float decisionLevel = 0f;
            boolean flux2 = ship.getFluxLevel() > 0.85;
            boolean flux1 = ship.getFluxLevel() > 0.70;
            boolean hasTakenHullDamage = ship.getHullLevel() < 0.97f;
            float hullRatio = ship.getHitpoints() / ship.getMaxHitpoints();

            float activeWeapons = 0;
            if (weapons != null) {
                activeWeapons = weapons.size();
                for (WeaponAPI w : weapons) {
                    if (w.isDisabled() || w.isPermanentlyDisabled()) {
                        activeWeapons--;
                    }
                }
                activeWeapons /= weapons.size();
            }

            float activeEngines = 0;
            if (engines != null) {
                activeEngines = engines.size();
                for (ShipEngineControllerAPI.ShipEngineAPI e : engines) {
                    if (e.isDisabled() || e.isPermanentlyDisabled()) {
                        activeEngines--;
                    }
                }
                activeEngines /= engines.size();
            }

            List<DamagingProjectileAPI> possibleTargets = new ArrayList<>(200);
            possibleTargets.addAll(combatUtil.getProjectilesWithinRange(ship.getLocation(), 600f));
            possibleTargets.addAll(combatUtil.getMissilesWithinRange(ship.getLocation(), 750f));
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
                    decisionLevel += (float) Math.sqrt(0.25f * possibleTarget.getDamageAmount() + possibleTarget.getEmpAmount() * 0.50f);
                }
                if (possibleTarget.getDamageType() == DamageType.KINETIC) {
                    decisionLevel += (float) Math.sqrt(0.50f * possibleTarget.getDamageAmount() + possibleTarget.getEmpAmount() * 0.50f);
                }
                else {
                    decisionLevel += (float) Math.sqrt(possibleTarget.getDamageAmount() + possibleTarget.getEmpAmount() * 0.50f);
                }
            }

            //try to avoid ship explosions
            List<ShipAPI> ships = new ArrayList<>(100);
            ships.addAll(combatUtil.getShipsWithinRange(ship.getLocation(), 400f));
            for (ShipAPI possibleShip : ships) {
                if (possibleShip.getHullLevel()>0.25f) continue;
                if (possibleShip.getOwner() != ship.getOwner() && possibleShip.getHullSize() != ShipAPI.HullSize.FIGHTER) {
                    //engine.addFloatingText(possibleShip.getLocation(), "CLOSE", 30f, Color.cyan, possibleShip, 0.1f, 0.1f);
                    if (possibleShip.getHullSize() == ShipAPI.HullSize.FRIGATE || possibleShip.getHullSize() == ShipAPI.HullSize.DESTROYER) {
                        decisionLevel += 25f;
                    } else {
                        decisionLevel += 50f;
                    }
                }
            }

            if (hasTakenHullDamage) {
                decisionLevel += 20f;
            }
            if (hullRatio < 0.70f) {
                decisionLevel += 10f;
            }
            if (hullRatio < 0.35f) {
                decisionLevel += 10f;
            }

            if (activeWeapons < 0.85f) {
                decisionLevel += 15f;
            }
            if (activeWeapons < 0.70f) {
                decisionLevel += 30f;
            }
            if (activeEngines < 0.85f) {
                decisionLevel += 15f;
            }
            if (activeEngines < 0.70f) {
                decisionLevel += 30f;
            }

            if (flux1) {
                decisionLevel -= 15f;
            }
            if (flux2) {
                decisionLevel -= 20f;
            }

            if (ship.getShield() == null){
                decisionLevel -= 9999f;
            }

            //macgyver debugger
            //engine.addFloatingText(ship.getLocation(), "test", 1f+decisionLevel, Color.cyan, ship, 0.5f, 1.0f);

            if (!system.isActive()) {
                if (decisionLevel < 45f && AIUtils.canUseSystemThisFrame(ship)) {
                    ship.useSystem();
                    timer.setElapsed(-5); //minimum time before checking again
                }
            }
            if (system.isActive()) {
                if (decisionLevel >= 45f) {
                    ship.useSystem();
                }
            }
        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
        this.system = system;
    }
}
