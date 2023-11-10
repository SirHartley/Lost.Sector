package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.shipsystems.nskr_harmonicsStats;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class nskr_harmonicsAI  implements ShipSystemAIScript {

    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipwideAIFlags flags;
    private final IntervalUtil timer = new IntervalUtil(0.30f, 0.50f);
    private ShipAPI use = null;

    private HashMap<ShipAPI, Float> scores = new HashMap<>();

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
            List<ShipAPI> potential = new ArrayList<>();
            for (ShipAPI s : engine.getShips()){
                if (!nskr_harmonicsStats.isValidTarget(ship,s)) continue;
                potential.add(s);
            }
            evaluate(ship, potential);
            use = getHighest(potential, 100f);

            if (use!=null){
                ship.setShipTarget(use);
                ship.useSystem();
            }
        }
    }

    private ShipAPI getHighest(List<ShipAPI> targets, float minScore) {
        ShipAPI highest = null;
        float score = 0f;
        for (ShipAPI t : targets){
            if (scores.get(t)>score){
                score = scores.get(t);
                highest = t;
            }
        }
        if (score < minScore) return null;
        return highest;
    }

    private void evaluate(ShipAPI source, List<ShipAPI> targets) {

        List<ShipAPI> enemies = new ArrayList<>();
        for (ShipAPI s : CombatUtils.getShipsWithinRange(source.getLocation(), getAverageRange(source))){
            if (!s.isAlive()) continue;
            if (s.getOwner()==source.getOwner()) continue;
            if (s.isFighter()) continue;
            enemies.add(s);
        }
        boolean noShips = enemies.isEmpty();

        for (ShipAPI t : targets){
            float score = 0f;
            if (noShips) score += 100f;
            if (Global.getCombatEngine().getPlayerShip()==t) score += 75f;

            score += Math.min(t.getMutableStats().getMaxSpeed().getModifiedValue()/5f, 75f);

            for (ShipAPI enemy : CombatUtils.getShipsWithinRange(t.getLocation(), getAverageRange(t))){
                if (!enemy.isAlive()) continue;
                if (enemy.getOwner()==t.getOwner()) continue;
                if (enemy.isFighter()) continue;

                score += 25f * util.getLinearMod(enemy, 1.0f);
            }

            scores.put(t, score);
        }
    }

    public static float getAverageRange(ShipAPI ship) {
        float averageRange = 0f;
        ArrayList<WeaponAPI> countedWeapons = new ArrayList<>();
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.getType() == WeaponAPI.WeaponType.MISSILE || weapon.hasAIHint(WeaponAPI.AIHints.PD))
                continue;
            countedWeapons.add(weapon);
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
