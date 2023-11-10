package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.weapons.nskr_bigFlakEffect;

import java.awt.*;

public class nskr_bigFlakAI implements ShipSystemAIScript {

    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipwideAIFlags flags;
    private final IntervalUtil timer = new IntervalUtil(0.30f, 0.50f);

    private WeaponAPI weaponL = null;
    private WeaponAPI weaponR = null;
    public static final float MAX_RANGE = 1200f;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
        this.flags = flags;
        timer.randomize();
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (weaponL==null || weaponR==null) {
            for (WeaponAPI w : ship.getAllWeapons()) {
                if (w.getId() == null) continue;
                if (w.getSlot() == null) continue;

                if (w.getSlot().getId().equals("WS0006")) weaponL = w;
                if (w.getSlot().getId().equals("WS0007")) weaponR = w;
            }
        }

        nskr_bigFlakEffect.ShipSpecificData data = (nskr_bigFlakEffect.ShipSpecificData) Global.getCombatEngine().getCustomData().get("BIGFLAK_AI_DATA_KEY" + ship.getId());
        if (data == null){
            return;
        }

        if (engine.isPaused() || ship.getShipAI() == null) {
            return;
        }

        timer.advance(amount);
        if (timer.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }
            float decisionLevel = 0f;

            decisionLevel += ship.getFluxLevel() * 30f;
            if (data.left){
                decisionLevel = getDecisionLevel(decisionLevel, weaponL);
            }
            if (data.right){
                decisionLevel = getDecisionLevel(decisionLevel, weaponR);
            }
            //engine.addFloatingText(ship.getLocation(), "test " + (int)decisionLevel, 60f, Color.cyan, ship, 0.5f, 1.0f);

            if (decisionLevel>60f){
                ship.useSystem();
            }
        }
    }

    private float getDecisionLevel(float decisionLevel, WeaponAPI weapon) {
        float missileCount = 0f;
        float damagePressure = 0f;
        for (CombatEntityAPI c : CombatUtils.getEntitiesWithinRange(weapon.getLocation(), MAX_RANGE)){
            if (ship.getOwner() == c.getOwner()) continue;

            // ignore everything outside of a y degree cone
            if (Math.abs(MathUtils.getShortestRotation(VectorUtils.getAngle(weapon.getLocation(), c.getLocation()), weapon.getCurrAngle())) > 20f) continue;
            //engine.addFloatingText(c.getLocation(), "test " + decisionLevel, 60f, Color.cyan, ship, 0.5f, 1.0f);

            if (c instanceof ShipAPI) {
                ShipAPI t = (ShipAPI) c;
                if (!t.isAlive()) continue;

                if (t.getHullSize() == ShipAPI.HullSize.FIGHTER) {
                    decisionLevel += 25f;
                } else {
                    float temp = 0f;
                    //+level for fluxed or dying
                    temp += t.getFluxLevel() * 50f;
                    if (t.getFluxTracker().isOverloadedOrVenting()) temp += 40f;
                    //count highest
                    if (temp > damagePressure) damagePressure = temp;
                }
            }
            //missiles
            if (c instanceof MissileAPI){
                MissileAPI missile = (MissileAPI) c;
                if (missile.getDamageAmount()<=0f) continue;
                if (missile.isFading()) continue;

                missileCount += missile.getDamageAmount();
            }
        }
        decisionLevel += damagePressure;
        decisionLevel += missileCount / 40f;
        return decisionLevel;
    }
}
