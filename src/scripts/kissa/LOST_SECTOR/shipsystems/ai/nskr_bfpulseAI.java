package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import scripts.kissa.LOST_SECTOR.shipsystems.nskr_bfPulseStats;
import scripts.kissa.LOST_SECTOR.util.combatUtil;

import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class nskr_bfpulseAI implements ShipSystemAIScript {

    private CombatEngineAPI engine = null;
    private ShipAPI ship;

    private final IntervalUtil tracker = new IntervalUtil(0.30f, 0.50f);

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
            float range = nskr_bfPulseStats.getMaxRangeSucc(ship);

            List<DamagingProjectileAPI> possibleTargets = new ArrayList<>(100);
            possibleTargets.addAll(combatUtil.getProjectilesWithinRange(ship.getLocation(), range));
            possibleTargets.addAll(combatUtil.getMissilesWithinRange(ship.getLocation(), range));

            List<ShipAPI> ships = new ArrayList<>(100);
            ships.addAll(combatUtil.getShipsWithinRange(ship.getLocation(), 350f));

            float decisionLevel = 0f;
            for (DamagingProjectileAPI possibleTarget : possibleTargets) {
                if (possibleTarget.getOwner() != ship.getOwner() && !possibleTarget.isFading() && possibleTarget.getCollisionClass() != CollisionClass.NONE) {

                    if (possibleTarget.getDamageType() == DamageType.FRAGMENTATION) {
                        decisionLevel += (float) Math.sqrt(0.25f * possibleTarget.getDamageAmount() + possibleTarget.getEmpAmount() * 0.25f);
                    }
                    else {
                        decisionLevel += (float) Math.sqrt(possibleTarget.getDamageAmount() + possibleTarget.getEmpAmount() * 0.25f);
                    }
                }
            }
            for (ShipAPI possibleship : ships){
            if (possibleship.getOwner() != ship.getOwner()) {
                if (possibleship.getHullSize() == ShipAPI.HullSize.FIGHTER) {
                    decisionLevel += (25f);
                }
            }

                //macgyver debugger
                //engine.addFloatingText(ship.getLocation(), "test", 1f+decisionLevel, Color.cyan, ship, 0.5f, 1.0f);

            }
            if (decisionLevel > 40f) {
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
