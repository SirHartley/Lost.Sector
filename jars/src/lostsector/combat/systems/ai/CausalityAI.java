package lostsector.combat.systems.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import lostsector.combat.hullmods.CausalityCore;
import lostsector.combat.systems.CausalityStats;
import lostsector.helper.MathHelper;
import lostsector.helper.CombatHelper;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class CausalityAI implements ShipSystemAIScript {

    private CombatEngineAPI engine = null;
    private ShipAPI ship;

    private final IntervalUtil tracker = new IntervalUtil(0.30f, 0.50f);

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        if (engine.isPaused() || ship.getShipAI() == null) {
            return;
        }
        CausalityCore.ShipSpecificData data = (CausalityCore.ShipSpecificData) Global.getCombatEngine().getCustomData().get("CAUSALITY_DATA_KEY" + ship.getId());
        if (data==null)return;

        float flux = ship.getFluxTracker().getFluxLevel();
        //flag manip
        if (data.energy>ship.getMaxFlux()*0.6f && flux<0.75f){
            ship.getShipAI().getAIFlags().setFlag(ShipwideAIFlags.AIFlags.DO_NOT_BACK_OFF, 2f);
        }
        if (flux>0.90f){
            ship.getShipAI().getAIFlags().setFlag(ShipwideAIFlags.AIFlags.BACK_OFF, 5f);
        }

        tracker.advance(amount);
        if (tracker.intervalElapsed()) {

            float range = CausalityStats.getMaxRange(ship);

            List<DamagingProjectileAPI> possibleTargets = new ArrayList<>(100);
            possibleTargets.addAll(CombatHelper.getProjectilesWithinRange(ship.getLocation(), range));
            possibleTargets.addAll(CombatHelper.getMissilesWithinRange(ship.getLocation(), range));

            float decisionLevel = 0f;
            float level = 0f;
            boolean hold = false;
            for (DamagingProjectileAPI possibleTarget : possibleTargets) {
                if (!possibleTarget.isFading() && possibleTarget.getCollisionClass() != CollisionClass.NONE) {
                    if (!CausalityStats.isValid(possibleTarget, ship)) continue;

                    level += CausalityStats.getEnergy(possibleTarget);
                }
            }
            //don't overload unless dying anyway
            float minToFail = 0.85f;
            float failureChance = 0f;
            if (flux>minToFail) failureChance = MathHelper.normalize(flux, minToFail, 1f);

            if (level + data.energy > ship.getMaxFlux()) {
                hold = true;
            }
            //0 chance when in player fleet
            ShipAPI player = Global.getCombatEngine().getPlayerShip();
            if (player!=null) {
                if (ship.getOwner() == player.getOwner()) failureChance = 0f;
            }
            //chance to fail when fighting against
            if (Math.random()<failureChance/4f) {
                hold = false;
            }

            if (ship.getSystem().getAmmo()==1){
                decisionLevel += level*0.67f;
            } else decisionLevel += level;

            float fluxLevel = ship.getFluxLevel();
            decisionLevel += 1500f*fluxLevel;

            if (decisionLevel > 3000f) {
                if (!ship.getShipAI().getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.DO_NOT_BACK_OFF)) {
                    ship.getShipAI().getAIFlags().setFlag(ShipwideAIFlags.AIFlags.DO_NOT_BACK_OFF, 2f);
                }
                if (!hold){
                    this.ship.useSystem();
                }
            }

            //macgyver debugger
            //engine.addFloatingText(ship.getLocation(), "test" + (int)decisionLevel + "+" + desiredMode, 20f, Color.cyan, ship, 0.5f, 1.0f);
        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
    }
}
