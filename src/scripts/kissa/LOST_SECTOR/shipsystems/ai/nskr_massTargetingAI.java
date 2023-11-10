//////////////////////
//Parts initially created by theDragn and modified from HTE
//////////////////////
package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import scripts.kissa.LOST_SECTOR.shipsystems.nskr_massTargetingStats;


public class nskr_massTargetingAI implements ShipSystemAIScript {

    private CombatEngineAPI engine = null;
    private ShipAPI ship;
    private final IntervalUtil tracker = new IntervalUtil(0.50f, 0.75f);
    private boolean runOnce = false;
    private List<WeaponAPI> weapons=new ArrayList<>();

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine.isPaused() || ship.getShipAI() == null) {
            return;
        }

        if(!runOnce){
            runOnce=true;
            weapons=ship.getAllWeapons();
        }

        boolean use = false;
        int friendlyCount = 0;
        int enemyCount = 0;
        int enemyFrigCount = 0;
        int fighterCount = 0;

        tracker.advance(amount);
        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }
            float analysisRange = this.getLongestRange(this.ship)*(1f+(nskr_massTargetingStats.TARGETING_BONUS/10f));
            List<ShipAPI> eShips = new ArrayList<>(100);
            eShips.addAll(AIUtils.getNearbyEnemies(ship, analysisRange));

            List<ShipAPI> friendlies = new ArrayList<>(100);
            friendlies.addAll(AIUtils.getNearbyAllies(ship, nskr_massTargetingStats.getMaxRange(ship)));

            for (ShipAPI eShip : eShips){
                if (eShip.getHullSize()== ShipAPI.HullSize.FIGHTER){
                    fighterCount++;
                } else if (eShip.getHullSize()== ShipAPI.HullSize.FRIGATE){
                    enemyFrigCount++;
                } else {
                    enemyCount++;
                }
            }
            for (ShipAPI fShip : friendlies){
                if (fShip.getHullSize()== ShipAPI.HullSize.FIGHTER) continue;
                if (fShip==ship) continue;
                //fuckery
                List<ShipAPI> feShips = new ArrayList<>(100);
                List<ShipAPI> rfeShips = new ArrayList<>(100);
                feShips.addAll(AIUtils.getNearbyEnemies(fShip, fShip.getCollisionRadius()*5f));
                for (ShipAPI feShip : feShips){
                    if (feShip.getHullSize()== ShipAPI.HullSize.FIGHTER) continue;
                    rfeShips.add(feShip);
                }
                if (rfeShips.size()<1) continue;
                friendlyCount++;
            }
            //macgyver debugger
            //engine.addFloatingText(ship.getLocation(), "friendlyCount " + friendlyCount + " enemyCount " + enemyCount + " enemyFrigCount " + enemyFrigCount + " fighterCount " + fighterCount, 40f, Color.cyan, ship, 0.5f, 1.0f);

            if (fighterCount>4){
                use = true;
            }
            if (enemyFrigCount>1){
                use = true;
            }
            if (enemyCount>0){
                use = true;
            }
            if (friendlyCount>0){
                use = true;
            }

            if (use) this.ship.useSystem();
        }
    }

    private float getLongestRange(ShipAPI ship) {
        float longestRange = 0f;
        WeaponAPI.WeaponSize largestWeaponSize = WeaponAPI.WeaponSize.SMALL;

        for (WeaponAPI weapon : weapons) {
            if (largestWeaponSize == WeaponAPI.WeaponSize.SMALL && weapon.getSize() != largestWeaponSize && weapon.getType() != WeaponAPI.WeaponType.MISSILE){
                largestWeaponSize = weapon.getSize();
            }
            if (largestWeaponSize != WeaponAPI.WeaponSize.MEDIUM || weapon.getSize() != WeaponAPI.WeaponSize.LARGE) continue;
            largestWeaponSize = WeaponAPI.WeaponSize.LARGE;
        }

        for (WeaponAPI weapon : weapons) {
            float range = 0f;
            if (weapon.getType() == WeaponAPI.WeaponType.MISSILE || weapon.getSize() != largestWeaponSize || weapon.hasAIHint(WeaponAPI.AIHints.PD) || !((range = weapon.getRange()) > longestRange)) continue;
            longestRange = range;
        }
        return longestRange;
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
    }
}
