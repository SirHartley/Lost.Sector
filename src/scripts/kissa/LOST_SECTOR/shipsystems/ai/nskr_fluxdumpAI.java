package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class nskr_fluxdumpAI implements ShipSystemAIScript {

    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipwideAIFlags flags;
    private final IntervalUtil timer = new IntervalUtil(0.40f, 0.70f);
    public static final ArrayList<ShipwideAIFlags.AIFlags> TOWARDS = new ArrayList<>();
    public static final ArrayList<ShipwideAIFlags.AIFlags> AWAY = new ArrayList<>();
    public static final ArrayList<ShipwideAIFlags.AIFlags> CON = new ArrayList<>();
    static {
        TOWARDS.add(ShipwideAIFlags.AIFlags.PURSUING);
        TOWARDS.add(ShipwideAIFlags.AIFlags.HARASS_MOVE_IN);
        AWAY.add(ShipwideAIFlags.AIFlags.RUN_QUICKLY);
        AWAY.add(ShipwideAIFlags.AIFlags.NEEDS_HELP);
        CON.add(ShipwideAIFlags.AIFlags.BACK_OFF);
        CON.add(ShipwideAIFlags.AIFlags.BACK_OFF_MIN_RANGE);
        CON.add(ShipwideAIFlags.AIFlags.BACKING_OFF);
    }

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
            float flux = ship.getFluxLevel();
            flux = flux*100f;
            decisionLevel += flux;
            float useLevel = 100f - (ship.getSystem().getAmmo() * 5f);

            List<ShipAPI> threats = AIUtils.getNearbyEnemies(ship, 750f);
            for (ShipAPI threat : threats){
                if ((threat.isFighter() || threat.isHulk() || threat.getCollisionClass().equals(CollisionClass.ASTEROID) || threat.getOwner() == ship.getOwner()))continue;
                if (ship.isFrigate()) decisionLevel *= 1.03f;
                else decisionLevel *= 1.06f;
            }

            for (ShipwideAIFlags.AIFlags f : CON) {
                if (flags.hasFlag(f)) {
                    decisionLevel += 4f;
                }
            }
            for (ShipwideAIFlags.AIFlags f : AWAY) {
                if (flags.hasFlag(f)) {
                    decisionLevel += 4f;
                }
            }
            for (ShipwideAIFlags.AIFlags f : TOWARDS) {
                if (flags.hasFlag(f)) {
                    decisionLevel += 2f;
                }
            }

            if (decisionLevel >= useLevel) {
                ship.useSystem();
            }
        }
    }
}
