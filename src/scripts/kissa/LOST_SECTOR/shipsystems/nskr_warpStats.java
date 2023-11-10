
package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import scripts.kissa.LOST_SECTOR.plugins.nskr_teleporterPlugin;
import scripts.kissa.LOST_SECTOR.util.combatUtil;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.lwjgl.util.vector.Vector2f;

public class nskr_warpStats extends BaseShipSystemScript {

    //base range we always move this amount, multiplier increases effect of cur speed, diff increased effect when strafing
    public static final float BASE = 100f;
    public static final float MULTIPLIER = 1.75f;
    public static final float DIFF_BASE = 1f;
    public static final float DIFF_DIAGONAL = 1.25f;
    public static final float DIFF_SIDEWAYS = 1.75f;
    public static final Color LIGHTNING_CORE_COLOR = new Color(255, 140, 190, 255);
    public static final Color LIGHTNING_FRINGE_COLOR = new Color(255, 9, 28, 255);

    //VARIABLES
    public static final Vector2f ZERO = new Vector2f();
    boolean activated;
    private Vector2f tPoint;
    private boolean sound;
    private final IntervalUtil arcInterval = new IntervalUtil(0.05f, 0.05f);
    private Vector2f oldLoc;
    private boolean updated = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused() || stats.getEntity() == null) return;

        //make sure variables are correct
        if (!updated) {
            activated = false;

            tPoint = teleportPoint(ship);
            oldLoc = MathUtils.getPointOnCircumference(ship.getLocation(), 50f, (float) Math.random() * 360f);

            if(!sound) {
                Global.getSoundPlayer().playSound("nskr_teleport", 1.0f, 1.0f, ship.getLocation(), ZERO);
                sound = true;
            }

            updated = true;
        }

        if (state == State.ACTIVE) {
            //null everything
            if (oldLoc != null && tPoint != null) {

                if (!activated) {
                    for (int x = 0; x < 12; x++) {
                        //old loc
                        float aAngle1 = VectorUtils.getAngle(oldLoc, tPoint) * ((float) Math.random() * 45f);
                        float aDistance1 = (float) Math.random() * 25f + 25f;
                        Vector2f apoint1 = MathUtils.getPointOnCircumference(oldLoc, aDistance1, aAngle1);
                        //new loc
                        float aAngle2 = VectorUtils.getAngle(tPoint, oldLoc) * ((float) Math.random() * 45f);
                        float aDistance2 = (float) Math.random() * 200f + 50f;
                        Vector2f apoint2 = MathUtils.getPointOnCircumference(tPoint, aDistance2, aAngle2);
                        //close
                        float aAngle3 = (float) Math.random() * 360f;
                        float aDistance3 = (float) Math.random() * 50f + 50f;
                        float aAngle4 = aAngle3 * ((float) Math.random() * 45f);
                        Vector2f apoint3 = MathUtils.getPointOnCircumference(ship.getLocation(), aDistance3, aAngle3);
                        Vector2f apoint4 = MathUtils.getPointOnCircumference(ship.getLocation(), aDistance3, aAngle4);

                        //long
                        Global.getCombatEngine().spawnEmpArcVisual(apoint1, new SimpleEntity(apoint1), apoint2, new SimpleEntity(apoint2),
                                MathUtils.getRandomNumberInRange(5f, 15f), // thickness of the lightning bolt
                                LIGHTNING_CORE_COLOR, //Central color
                                LIGHTNING_FRINGE_COLOR); //Fringe Color
                        //close
                        Global.getCombatEngine().spawnEmpArcVisual(apoint3, new SimpleEntity(apoint3), apoint4, new SimpleEntity(apoint4),
                                MathUtils.getRandomNumberInRange(10f, 20f), // thickness of the lightning bolt
                                LIGHTNING_CORE_COLOR, //Central color
                                LIGHTNING_FRINGE_COLOR); //Fringe Color
                    }

                    //TELEPORT
                    //nskr_teleporterPlugin.addTeleportation(ship, tPoint);
                    ship.getLocation().set(tPoint);

                    sound = false;
                    activated = true;
                }
            }
        }
        if (state == State.OUT) {

        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();

        updated = false;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }

    //for synchronization with stuff
    public static Vector2f teleportPoint(ShipAPI ship){

        Vector2f ZERO = new Vector2f();
        Vector2f sLoc = ship.getLocation();
        float dist;
        float vAngle;
        Vector2f tPoint;

        //VECTOR TIME
        Vector2f sVel = ship.getVelocity();
        if (sVel == null) sVel = ZERO;
        float sAngle;
        sAngle = ship.getFacing();
        vAngle = VectorUtils.getFacing(sVel);
        dist = Objects.requireNonNull(ship.getVelocity()).length();
        if (dist == 0f) vAngle = sAngle;
        dist = ((dist*1.50f)+BASE);
        float diffMult = DIFF_BASE;

        //angle fuckery to go more sideways
        float diff = vAngle - sAngle;
        if (diff<0) diff *= -1f;
        if ((sVel.length()>0f) && (diff > 0)) {

            if (diff>=30&&diff<=150) diffMult=DIFF_DIAGONAL;
            if (diff>=65&&diff<=115) diffMult=DIFF_SIDEWAYS;

            if (diff>=210&&diff<=330) diffMult=DIFF_DIAGONAL;
            if (diff>=245&&diff<=295) diffMult=DIFF_SIDEWAYS;

        }
        dist *= MULTIPLIER;
        dist *= diffMult;

        tPoint = MathUtils.getPointOnCircumference(sLoc, dist, vAngle);
        //collision avoidance
        List<ShipAPI> ships = new ArrayList<>(100);
        ships.addAll(combatUtil.getShipsWithinRange(tPoint, 25f));
        for (ShipAPI possibleship : ships) {
            if (possibleship == ship) continue;
            if (possibleship.getHullSize() == ShipAPI.HullSize.FIGHTER) continue;
            dist += possibleship.getShieldRadiusEvenIfNoShield()*2f;
        }

        return MathUtils.getPointOnCircumference(sLoc, dist, vAngle);
    }
}
