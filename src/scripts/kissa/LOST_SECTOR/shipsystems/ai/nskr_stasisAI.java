package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.shipsystems.nskr_stasisStats;
import scripts.kissa.LOST_SECTOR.weapons.nskr_stasis;

import java.awt.*;

public class nskr_stasisAI implements ShipSystemAIScript {

    private WeaponAPI weapon = null;
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
        if (weapon==null) {
            for (WeaponAPI w : ship.getAllWeapons()) {
                if (w.getId() == null) continue;
                if (w.getId().equals("nskr_stasis")) {
                    weapon = w;
                    break;
                }
            }
        }
        if (engine.isPaused() || ship.getShipAI() == null) {
            return;
        }
        ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("STASIS_AI_DATA_KEY" + ship.getId());
        if (data == null){
            data = new ShipSpecificData();
        }
        if (data.sinceEffected < nskr_stasisStats.MAX_DURATION){
            data.sinceEffected += amount;
        }
        //engine.addFloatingText(ship.getLocation(), "LMAO "+data.sinceEffected, 14f, Color.RED, null,1f,1f);
        Global.getCombatEngine().getCustomData().put("STASIS_AI_DATA_KEY" + ship.getId(), data);

        timer.advance(amount);
        if (timer.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }
            boolean stop = false;
            if (weapon.getCooldownRemaining()>0f){

                if (ship.getFluxLevel()>0.75f) stop = true;
                //so AI will save charges if it just hit something
                if (data.sinceEffected < nskr_stasisStats.MAX_DURATION){
                    stop = true;
                }
                //target already stasised check
                for (ShipAPI t : CombatUtils.getShipsWithinRange(ship.getLocation(), 1000f)){
                    if (t.getOwner()==ship.getOwner()) continue;
                    if (t.getHullSize()== ShipAPI.HullSize.FIGHTER) continue;
                    if (!t.hasListenerOfClass(nskr_stasis.stasisEffectListener.class)) continue;

                    float angle = VectorUtils.getAngle(ship.getLocation(), t.getLocation());
                    // ignore everything outside of a y degree cone
                    if (Math.abs(MathUtils.getShortestRotation(angle, ship.getFacing())) > 20f) continue;
                    //engine.addFloatingText(ship.getLocation(), "LMAO ", 14f, Color.RED, null,1f,1f);
                    stop = true;
                    break;
                }

                //use
                if (!stop) {
                    ship.useSystem();
                }
            }
        }
    }

    public static class ShipSpecificData {
        public float sinceEffected = nskr_stasisStats.MAX_DURATION;
    }
}