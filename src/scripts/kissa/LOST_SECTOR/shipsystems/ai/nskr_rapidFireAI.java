package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class nskr_rapidFireAI implements ShipSystemAIScript {

    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipwideAIFlags flags;
    public static final float DEGREES = 69f; // (haha nice)
    private final IntervalUtil timer = new IntervalUtil(0.40f, 0.70f);
    private boolean runOnce = false;
    private boolean countOnce = false;
    private List<WeaponAPI> weapons=new ArrayList<>();
    private final List<WeaponAPI> countedWeapons=new ArrayList<>();

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
        if(!runOnce){
            weapons=ship.getAllWeapons();
            runOnce=true;
        }

        timer.advance(amount);
        if (timer.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }

            float activeWeapons = 0;
            int compensation = 0;
            if (weapons != null) {
                activeWeapons = weapons.size();
                for (WeaponAPI w : weapons) {
                    //system only affect ballistic
                    if (w.getType()== WeaponAPI.WeaponType.MISSILE) {
                        compensation++;
                        continue;
                    }
                    if (w.isDisabled() || w.isPermanentlyDisabled()) {
                        activeWeapons--;
                    }
                }
                activeWeapons /= weapons.size()-compensation;
            }
            //don't use if we don't have guns online
            if (activeWeapons<0.5f) return;

            // setup
            int targets = 0;
            Vector2f curr = ship.getLocation();
            float facing = ship.getFacing();
            // scan everything within x range
            List<CombatEntityAPI> consider = CombatUtils.getEntitiesWithinRange(curr, this.getAverageRange(this.ship));
            for (CombatEntityAPI test : consider) {
                float angle = VectorUtils.getAngle(curr, test.getLocation());
                // ignore everything outside of a y degree cone
                if (Math.abs(MathUtils.getShortestRotation(angle, facing)) > DEGREES) {
                    continue;
                }
                if (!(test instanceof ShipAPI)) continue;
                ShipAPI targetS = (ShipAPI)test;
                if (targetS.isFighter() || targetS.isHulk() || targetS.getOwner() == ship.getOwner()) continue;

                targets++;
            }

            //macgyver debugger
            //engine.addFloatingText(ship.getLocation(), "test " + this.getAverageRange(this.ship), 60f, Color.cyan, ship, 0.5f, 1.0f);
        if (targets>0) {
            ship.useSystem();
        }
    }
}
    private float getAverageRange(ShipAPI ship) {
        float averageRange = 0f;
        if (!countOnce) {
            for (WeaponAPI weapon : weapons) {
                if (weapon.getType() == WeaponAPI.WeaponType.MISSILE || weapon.hasAIHint(WeaponAPI.AIHints.PD))
                    continue;
                countedWeapons.add(weapon);
            }
            countOnce=true;
        }
        float totalRange = 0f;
        float totalCount = 0f;
        for (WeaponAPI weapon : countedWeapons) {
            totalRange += weapon.getRange();
            totalCount++;
        }
        averageRange = totalRange/totalCount;

        return averageRange;
    }
}



