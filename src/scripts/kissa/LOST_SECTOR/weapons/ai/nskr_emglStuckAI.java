//////////////////////
//By Tartiflette.
//////////////////////
package scripts.kissa.LOST_SECTOR.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import scripts.kissa.LOST_SECTOR.weapons.nskr_emglScript;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class nskr_emglStuckAI implements MissileAIPlugin, GuidedMissileAI {

    public static final Color EXPLOSION_COLOR = new Color(41, 70, 255, 200);
    public static final Color PARTICLE_COLOR = new Color(128, 82, 255, 200);
    public static final Color ARC_COLOR = new Color(222, 209, 255, 200);

    public static final Vector2f ZERO = new Vector2f();

    private final MissileAPI missile;
    private final IntervalUtil accelerationCheck = new IntervalUtil(0.1f, 0.1f);
    private CombatEngineAPI engine;
    private CombatEntityAPI target;
    private CombatEntityAPI anchor;
    private Vector2f offset = new Vector2f();
    private float angle = 0;
    private boolean runOnce = false;
    private Vector2f projected = new Vector2f();
    private Vector2f previousLoc = new Vector2f();
    private boolean emp = false;

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////

    public nskr_emglStuckAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
    }

    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////

    @Override
    public void advance(float amount) {

        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }

        //skip the AI if the game is paused, the missile is engineless or fading
        if (engine.isPaused()) {
            return;
        }

        //visual effect
        Vector2f loc = new Vector2f(offset);
        if (MagicRender.screenCheck(0.25f, loc)) {
            missile.setJitter(missile,new Color (17, 0, 255,255),5 * Math.min(missile.getElapsed(), 0.5f) * 2,5,3);


        }

        if (!runOnce) {
            runOnce = true;
            List<CombatEntityAPI> list = ((nskr_emglScript) missile.getWeapon().getEffectPlugin()).getHITS();

            if (list.isEmpty()) {
                missile.flameOut();
                return;
            }

            //get the anchor
            float range = 1000000;
            for (CombatEntityAPI e : list) {
                if (MathUtils.getDistanceSquared(missile, e) < range) {
                    target = e;
                    anchor = e; //some scripts change the target so I can't really use that for the anchor
                }
            }

            if (anchor == null) {
                return;
            }

            //put the anchor in the weapon's detonation list
            ((nskr_emglScript) missile.getWeapon().getEffectPlugin()).setDetonation(anchor);

            projected = new Vector2f(anchor.getVelocity());
            projected.scale(0.1f);
            Vector2f.add(missile.getLocation(), projected, projected);
            previousLoc = new Vector2f(missile.getLocation());

            offset = new Vector2f(missile.getLocation());
            Vector2f.sub(offset, new Vector2f(anchor.getLocation()), offset);
            VectorUtils.rotate(offset, -anchor.getFacing(), offset);

            angle = MathUtils.getShortestRotation(anchor.getFacing(), missile.getFacing());

            Global.getSoundPlayer().playSound("mine_ping", 1.2f, 0.60f, anchor.getLocation(), ZERO);

            return;
        } else {
            if (anchor == null || ((nskr_emglScript) missile.getWeapon().getEffectPlugin()).getDetonation(anchor)) {
                missile.setCollisionClass(CollisionClass.MISSILE_FF);
                return;
            }
        }

        //target sanity check
        if (target==null) return;

        //acceleration check for tear off
        accelerationCheck.advance(amount);
        if (accelerationCheck.intervalElapsed()) {
            //acceleration tear off

            boolean fooled = (target != anchor);
            boolean escaped = (anchor.getCollisionClass() == CollisionClass.NONE);

            if (fooled || escaped) {
                missile.setArmingTime(missile.getElapsed() + 0.25f);
                missile.setCollisionClass(CollisionClass.MISSILE_FF);
                return;
            }

            Vector2f.sub(new Vector2f(missile.getLocation()), previousLoc, projected);
            Vector2f.add(new Vector2f(missile.getLocation()), projected, projected);
            previousLoc = new Vector2f(missile.getLocation());
        }

        //stuck effect
        VectorUtils.rotate(offset, anchor.getFacing(), loc);
        Vector2f.add(loc, anchor.getLocation(), loc);
        missile.getLocation().set(loc);
        missile.setFacing(anchor.getFacing() + angle);

        //detonation
        if (missile.getElapsed() > 1.0f) {
            missile.setCollisionClass(CollisionClass.MISSILE_FF);

            if (!emp) {
                CombatEntityAPI empTarget = target;
                //damage
                for (int x = 0; x < 5; x++) {
                    EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(missile.getSource(), loc, empTarget, empTarget,
                            DamageType.ENERGY, missile.getDamageAmount() * 0.10f, missile.getEmpAmount() * 0.25f, 100000f, "tachyon_lance_emp_impact", 20f,
                            EXPLOSION_COLOR, ARC_COLOR);
                }
                //fx
                for (int x = 0; x < 12; x++) {
                    float angle = (float) Math.random() * 360f;
                    float distance = (float) Math.random() * 50f + 50f;
                    Vector2f point1 = MathUtils.getPointOnCircumference(loc, distance, angle);
                    Vector2f point2 = new Vector2f(loc);
                    engine.spawnEmpArcVisual(point2, new SimpleEntity(point2), point1, new SimpleEntity(point1),
                            15f,
                            EXPLOSION_COLOR, PARTICLE_COLOR);
                }
                Global.getSoundPlayer().playSound("nskr_emgl_explo", 0.7f, 0.8f, loc, ZERO);

                emp = true;
            }
            //if detonation fails
            if (missile.getElapsed() > 1.1f){
                emp = false;
                engine.removeEntity(missile);
            }
        }
    }

    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }

    public void init(CombatEngineAPI engine) {
    }
}