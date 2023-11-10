package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.util.Pair;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.*;
import java.util.List;

public class nskr_takedown extends BaseHullMod {
    //
    //code to make ships get takedowns from league when killing enemy ships or assisting
    //
    public static final float TAKEDOWN_TIMER = 3f;
    public static final Vector2f ZERO = new Vector2f();

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.addListener(new takedownCounter());
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("TAKEDOWN_DATA_KEY" + ship.getId());
        if (data == null) {
            data = new ShipSpecificData();
        }
        data.timeOut = false;
        //only needs to run when a new hit happens
        if (data.newHit) {
            //dupe removal
            for (ListIterator<CombatEntityAPI> iter = data.sShips.listIterator(); iter.hasNext() && data.sShips.size() > 1; ) {
                CombatEntityAPI a = iter.next();
                CombatEntityAPI b = iter.previous();
                if (a == b) {
                    //reset counter when hitting a ship
                    for (Pair<CombatEntityAPI, Float> c : data.tShips) {
                        if (c.one == a) {
                            c.two = TAKEDOWN_TIMER;
                            break;
                        }
                    }
                    iter.remove();
                }
            }

            //clone list
            for (CombatEntityAPI sShip : data.sShips) {
                ShipAPI vShip = (ShipAPI) sShip;
                if (((ShipAPI) sShip).isFighter() || vShip.isHulk() || sShip.getCollisionClass().equals(CollisionClass.ASTEROID) || sShip.getOwner() == ship.getOwner())
                    continue;
                //make sure we add only once
                boolean isSame = false;
                for (Pair<CombatEntityAPI, Float> c : data.tShips) {
                    if (c.one == sShip) {
                        isSame = true;
                        break;
                    }
                }
                if (isSame) continue;
                data.tShips.add(new Pair<>(sShip, TAKEDOWN_TIMER));
            }
        }
            for (Pair<CombatEntityAPI, Float> tShip : data.tShips) {
                ShipAPI vShip = (ShipAPI)tShip.one;

                tShip.two -= amount;
                //timedout
                if (tShip.two<=0)data.timeOut = true;
                //takedown on kill
                if (vShip.isHulk() || tShip.one.getHitpoints() <= 0 || tShip.one.isExpired()){
                    if (ship == engine.getPlayerShip()) {
                        engine.addFloatingText(tShip.one.getLocation(), "TAKEDOWN", 50, Color.RED, tShip.one, 0f, 0f);
                        Global.getSoundPlayer().playSound("nskr_takedown", MathUtils.getRandomNumberInRange(0.6f, 1.0f), 3.5f, tShip.one.getLocation(), ZERO);
                    }
                    //delayed reset
                    if (ship.getSystem().isActive() || ship.getSystem().isChargeup() || ship.getSystem().isChargedown()){
                        data.delayedReset = true;
                    }
                    int curAmmo = ship.getSystem().getAmmo();
                    ship.getSystem().setAmmo(curAmmo + 1);
                    if (ship.getSystem().isCoolingDown()) ship.getSystem().setCooldownRemaining(0);


                    tShip.two = -11f;
                    data.timeOut = true;
                    //engine.addFloatingText(ship.getLocation(), "ammo " + curAmmo + " takedown " + tShip.one + " timer " + tShip.two, 60, Color.RED, ship, 0.5f, 1.0f);
                }
                //engine.addFloatingText(tShip.one.getLocation(), "TAKEDOWN " + Math.round(tShip.two)+ " time " + data.tShips.size() + " size ", 60, Color.RED, tShip.one, 0.5f, 1.0f);
            }
            //only run for dead and timeout ships
            if (data.timeOut) {
                //clear dead ships
                CombatEntityAPI removalShip = null;
                for (Iterator<Pair<CombatEntityAPI, Float>> iter = data.tShips.listIterator(); iter.hasNext(); ) {
                    Pair<CombatEntityAPI, Float> a = iter.next();
                    ShipAPI vShip = (ShipAPI) a.one;
                    if (a.two < 0 || vShip.isHulk()) {
                        removalShip = a.one;
                        //engine.addFloatingText(ship.getLocation(), "removed" + removalShip + "iter" + iter, 60, Color.RED, ship, 0.5f, 1.0f);
                        iter.remove();
                    }
                }
                //clear expired and dead ships
                for (Iterator<CombatEntityAPI> iter = data.sShips.iterator(); iter.hasNext(); ) {
                    CombatEntityAPI a = iter.next();
                    ShipAPI vShip = (ShipAPI) a;
                    if (vShip.isHulk() || a == removalShip) {
                        //engine.addFloatingText(ship.getLocation(), "removed" + iterB, 60, Color.RED, ship, 0.5f, 1.0f);
                        iter.remove();
                    }
                }
            }
        //delayed CD refund
        if (ship.getSystem().isCoolingDown() && data.delayedReset){
            ship.getSystem().setCooldownRemaining(0);
            data.delayedReset = false;
        }
        Global.getCombatEngine().getCustomData().put("TAKEDOWN_DATA_KEY" + ship.getId(), data);
    }

    public static class takedownCounter implements DamageDealtModifier {
        private float damageNew = 0f;
        private float damageOld = 0f;
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            CombatEngineAPI engine = Global.getCombatEngine();
            if (engine == null) {
                return null;
            }
            if (engine.isPaused()) {
                return null;
            }
            if (damage == null || damage.getStats() == null || damage.getStats().getEntity() == null) return null;
            ShipAPI ship = (ShipAPI)damage.getStats().getEntity();
            ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("TAKEDOWN_DATA_KEY" + ship.getId());
            data.newHit = false;
            if (!(target instanceof ShipAPI)) return null;

            float amount = 0f;
            amount = engine.getElapsedInLastFrame();
            damageNew += damage.computeDamageDealt(amount);
            //only update when dealing dmg
            if (damageNew > damageOld){
                data.sShips.add(target);
                damageOld = damageNew;
                data.newHit = true;
            }
            Global.getCombatEngine().getCustomData().put("TAKEDOWN_DATA_KEY" + ship.getId(), data);
            //engine.addFloatingText(target.getLocation(), "TAKEDOWN " + sShips.toString(), 60, Color.RED , target, 0.5f, 1.0f);
            return null;
        }
    }

    public static class ShipSpecificData {
        private boolean delayedReset = false;
        private boolean newHit = false;
        private boolean timeOut = false;
        private final List<Pair<CombatEntityAPI, Float>> tShips = new ArrayList<>();
        private final List<CombatEntityAPI> sShips = new ArrayList<>();
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }
}