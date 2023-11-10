package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class nskr_missileSalvoAI implements ShipSystemAIScript {


    public static final float MAX_RANGE = 1100f;

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

        timer.advance(amount);
        if (timer.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }

            ShipAPI missileTarget = ship.getShipTarget();
            if (missileTarget!=null && missileTarget.getHullSize()!= ShipAPI.HullSize.FIGHTER){
                //range check
                float range = MAX_RANGE;
                if (ship.getVariant().hasHullMod(HullMods.ECCM)) range = MAX_RANGE + 500f;
                if (MathUtils.getDistance(ship.getLocation(), missileTarget.getLocation()) > range) return;

                boolean flux = missileTarget.getFluxLevel() > 0.80f || missileTarget.getFluxTracker().isVenting() || missileTarget.getFluxTracker().isOverloaded();
                int currCharges = ship.getSystem().getAmmo();
                int toSave = 0;
                //try to save charges
                if (missileTarget.getHullSize() == ShipAPI.HullSize.FRIGATE){
                    if (!flux){
                        toSave = 2;
                    } else {
                        toSave = 1;
                    }
                }
                else {
                    if (!flux){
                        toSave = 1;
                    } else {
                        toSave = 0;
                    }
                }
                //panic fire
                if (ship.getFluxLevel()>0.80f) toSave = 0;

                //use
                if (toSave<currCharges) {
                    ship.useSystem();
                }
            }
        }
    }
}
