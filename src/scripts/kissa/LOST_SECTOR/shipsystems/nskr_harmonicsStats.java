package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class nskr_harmonicsStats extends BaseShipSystemScript {

    private static final float MAX_RANGE = 1400f;
    public static final float MAX_DURATION = 10f;
    public static final float MAX_TIMEFLOW = 200f;

    public static final Color EMP_CORE_COLOR = new Color(131, 245, 255, 250);
    public static final Color EMP_FRINGE_COLOR = new Color(83, 137, 255, 250);

    private ShipAPI target = null;
    private boolean active = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            id = id + "_" + ship.getId();
        } else {
            return;
        }
        CombatEngineAPI engine = Global.getCombatEngine();
        if (target==null) return;

        if (!active){

            ship.addListener(new nskr_harmonics.harmonicsSourceListener(ship, target));
            target.addListener(new nskr_harmonics.harmonicsTargetListener(ship, target));

            //sound
            Global.getSoundPlayer().playSound("nskr_harmonics_activate", 1.0f, 0.70f, ship.getLocation(), new Vector2f());

            //EMP arc fx
            for (int x = 0; x < 8; x++){
                Vector2f pointFrom = MathUtils.getPointOnCircumference(
                        ship.getLocation(),
                        MathUtils.getRandomNumberInRange(ship.getCollisionRadius()/2f, ship.getCollisionRadius()),
                        (float) (Math.random()*360f));
                Vector2f pointTo = MathUtils.getPointOnCircumference(
                        target.getLocation(),
                        MathUtils.getRandomNumberInRange(target.getCollisionRadius()/2f, target.getCollisionRadius()),
                        (float) (Math.random()*360f));

                engine.spawnEmpArcVisual(pointFrom, new SimpleEntity(pointFrom), pointTo, new SimpleEntity(pointTo),
                        MathUtils.getRandomNumberInRange(30f,35f),
                        EMP_FRINGE_COLOR,
                        EMP_CORE_COLOR);
                Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 0.75f, 0.60f, pointFrom, new Vector2f());

            }

            active = true;
        }

    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            id = id + "_" + ship.getId();
        } else {
            return;
        }
        if (active){

            active = false;
        }
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {

        //ship target
        if (isValidTarget(ship, ship.getShipTarget())){
            target = ship.getShipTarget();
            return true;
        }
        //mouse target
        ShipAPI nearest = null;
        float range = Float.MAX_VALUE;
        for (ShipAPI t : CombatUtils.getShipsWithinRange(ship.getMouseTarget(), 300f)){
            if (!isValidTarget(ship, t)) continue;
            float dist = MathUtils.getDistance(ship.getMouseTarget(), t.getLocation());
            if (dist < range){
                range = dist;
                nearest = t;
            }
        }
        if (nearest!=null){
            target = nearest;
            return true;
        }
        target = null;
        return false;
    }

    @Override
    public String getDisplayNameOverride(State state, float effectLevel) {
        if (state==State.COOLDOWN){
            return "Harmonics - COOLDOWN";
        }
        if (target!=null){
            return "Harmonics - "+target.getHullSpec().getHullName();
        }
        return "Harmonics - NO TARGET";
    }

    public static boolean isValidTarget(ShipAPI source, ShipAPI target){
        if (target==null) return false;
        if (target==source) return false;
        if (!target.isAlive()) return false;
        if (target.getOwner()!=source.getOwner()) return false;
        if (target.isFighter() || target.isStationModule()) return false;
        if (MathUtils.getDistance(source, target) > getMaxRange(source)) return false;
        if (target.getFluxTracker().isOverloadedOrVenting()) return false;

        return true;
    }

    public static float getMaxRange(ShipAPI ship){
        if (ship==null) return 0f;
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(MAX_RANGE);
    }
}
