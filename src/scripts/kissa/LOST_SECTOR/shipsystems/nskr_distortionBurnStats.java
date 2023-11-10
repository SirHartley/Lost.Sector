package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.combatUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class nskr_distortionBurnStats extends BaseShipSystemScript {

    public static final float MAX_SPEED = 200f;
    public static final float TURN_RATE_FLAT = 25f;
    public static final float TURN_RATE_MULT = 250f;
    public static final float ARC_MAX_RANGE = 750f;
    public static final float ARC_DMG = 100f;
    private final IntervalUtil dmgInterval = new IntervalUtil(0.20f,0.20f);
    //particles
    private final int particlesPerFrame = 2;
    private final float arc = 45f;
    private final float spdMult = 3.5f;
    private final Color particleColor = new Color(57, 35, 255);
    //emp
    private final IntervalUtil arcInterval = new IntervalUtil(0.25f,0.35f);
    private final Color empColorFringe = new Color(35, 72, 255, 255);
    private final Color empColorCore = new Color(134, 35, 255, 255);
    //engine
    private final Color engineColor = new Color(57, 35, 255);

    public static final Vector2f ZERO = new Vector2f();


    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI)stats.getEntity();

        //SPEED
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
            stats.getMaxTurnRate().unmodify(id);
        } else {
            stats.getMaxSpeed().modifyFlat(id, MAX_SPEED);
            stats.getAcceleration().modifyPercent(id, TURN_RATE_MULT * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id, TURN_RATE_FLAT * effectLevel);
            stats.getTurnAcceleration().modifyPercent(id, TURN_RATE_MULT * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, TURN_RATE_FLAT);
            stats.getMaxTurnRate().modifyPercent(id, TURN_RATE_MULT);
        }

        //PARTICLES
        Vector2f particlePos, particleVel;
        for (int x = 0; x < Math.round(particlesPerFrame * (Global.getCombatEngine().getElapsedInLastFrame()*60f));) {
            particlePos = ship.getLocation();
            for (int y = 0; y < 100; y++) {
                float rArc = arc* MathUtils.getRandomNumberInRange(0.8f,1.2f);
                particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), (float) Math.max(Math.random()*ship.getCollisionRadius(), 0.50f*ship.getCollisionRadius()));
                float angle = VectorUtils.getAngle(particlePos, ship.getLocation());
                // ignore everything outside of a y degree cone
                if (Math.abs(MathUtils.getShortestRotation(angle, ship.getFacing())) < rArc) break;
            }
            particleVel = Vector2f.sub(particlePos, ship.getLocation(), null);
            mathUtil.scaleVector(particleVel, spdMult);
            Global.getCombatEngine().addSmokeParticle(particlePos, particleVel, 4f, 0.7f, MathUtils.getRandomNumberInRange(0.5f,1.5f),
                    particleColor);
            x++;
        }
        //EMP ARC
        arcInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
        if (arcInterval.intervalElapsed()) {
            Vector2f empPos1 = ship.getLocation(), empPos2 = ship.getLocation();
            for (int y = 0; y < 100; y++) {
                float rArc = arc * MathUtils.getRandomNumberInRange(0.8f, 1.2f);
                empPos1 = MathUtils.getRandomPointOnCircumference(ship.getLocation(), (float) Math.max(Math.random() * ship.getCollisionRadius(), 0.50f * ship.getCollisionRadius()));
                float angle = VectorUtils.getAngle(empPos1, ship.getLocation());
                empPos2 = MathUtils.getPointOnCircumference(ship.getLocation(), (float) Math.max((Math.random() * ship.getCollisionRadius()) * 2.5f, 0.75f * ship.getCollisionRadius()), angle+180f);
                // ignore everything outside of a y degree cone
                if (Math.abs(MathUtils.getShortestRotation(angle, ship.getFacing())) < rArc) break;
            }
            //Global.getCombatEngine().addFloatingText(empPos1, "pos1", 20f, Color.RED, null, 1f,1f);
            //Global.getCombatEngine().addFloatingText(empPos2, "pos2", 20f, Color.RED, null, 1f,1f);
            Global.getCombatEngine().spawnEmpArcVisual(empPos1, new SimpleEntity(empPos1), empPos2, new SimpleEntity(empPos2), 17f, empColorFringe, empColorCore);
        }
        //ENGINE COLOR
        ship.getEngineController().fadeToOtherColor(this, engineColor, null, 1f, 0.6f);

        //DAMAGE OTHER THINGS
        dmgInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
        if (dmgInterval.intervalElapsed()) {
            List<CombatEntityAPI> entities = new ArrayList<>(CombatUtils.getEntitiesWithinRange(ship.getLocation(), ARC_MAX_RANGE));
            Vector2f curr = ship.getLocation();
            float facing = ship.getFacing();
            for (CombatEntityAPI e : entities) {
                if (e == ship) continue;
                float angle = VectorUtils.getAngle(curr, e.getLocation());
                // ignore everything outside of a y degree cone
                if (Math.abs(MathUtils.getShortestRotation(angle, facing)) < 140f) continue;

                float chance = mathUtil.normalize(MathUtils.getDistance(ship.getLocation(), e.getLocation()), 0f, ARC_MAX_RANGE);
                chance = mathUtil.lerp(1f,0f, chance);
                if (Math.random()>chance) continue;

                float damage = ARC_DMG;
                if (e.getOwner()== ship.getOwner()){
                    damage *= 0.10f;
                    if(e instanceof MissileAPI)continue;
                }
                Vector2f arcPos = ship.getLocation();
                for (int y = 0; y < 100; y++) {
                    float rArc = arc* MathUtils.getRandomNumberInRange(0.8f,1.2f);
                    arcPos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), (float) Math.max(Math.random()*ship.getCollisionRadius(), 0.50f*ship.getCollisionRadius()));
                    float arcAngle = VectorUtils.getAngle(arcPos, ship.getLocation());
                    // ignore everything outside of a y degree cone
                    if (Math.abs(MathUtils.getShortestRotation(arcAngle, ship.getFacing())) < rArc) break;
                }
                Vector2f point = combatUtil.getNearestPointOnBounds(ship.getLocation(), e);
                if (e instanceof  MissileAPI) point = e.getLocation();
                if (e instanceof ShipAPI) {
                    //shield hit
                    if ((e.getShield() != null && e.getShield().isOn() && e.getShield().isWithinArc(
                            ship.getLocation()))) {
                        ((ShipAPI) e).getFluxTracker().increaseFlux(damage * e.getShield().getFluxPerPointOfDamage(), true);
                        Global.getCombatEngine().spawnEmpArcVisual(arcPos, new SimpleEntity(arcPos), combatUtil.getNearestPointOnCollisionRadius(ship.getLocation(),e), new SimpleEntity(point), 10f, empColorFringe, empColorCore);
                        Global.getSoundPlayer().playSound("tachyon_lance_emp_impact",1f,1f, point, ZERO);
                    } else {
                        //normal hit
                        Global.getCombatEngine().applyDamage(e, point, damage, DamageType.ENERGY, damage * 2f, false, false, ship);
                        Global.getCombatEngine().spawnEmpArcVisual(arcPos, new SimpleEntity(arcPos), point, new SimpleEntity(point), 10f, empColorFringe, empColorCore);
                        Global.getSoundPlayer().playSound("tachyon_lance_emp_impact",1f,1f, point, ZERO);
                    }
                } else if (e instanceof  MissileAPI){
                    //missile hit
                    Global.getCombatEngine().applyDamage(e, point, damage, DamageType.ENERGY, damage * 2f, false, false, ship);
                    //don't spawn arcs to flares and other nonsense
                    if (((MissileAPI) e).getDamageAmount()>=50f) {
                        Global.getCombatEngine().spawnEmpArcVisual(arcPos, new SimpleEntity(arcPos), point, new SimpleEntity(point), 10f, empColorFringe, empColorCore);
                        Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 1f, 1f, point, ZERO);
                    }
                }
            }

        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("greatly improved agility", false);
        } else if (index == 1) {
            return new StatusData("+" + (int)MAX_SPEED + " top speed", false);
        }
        return null;
    }
}
