//////////////////////
//Initially created by Nia Tahl and modified from Tahlan Shipworks
//////////////////////
package scripts.kissa.LOST_SECTOR.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class nskr_plasmalScript implements EveryFrameWeaponEffectPlugin {

    public static final Color PARTICLE_COLOR = new Color(120, 110,255);
    public static final Color GLOW_COLOR = new Color(110,207,255, 50);
    public static final Color FLASH_COLOR = new Color(227, 255, 253);
    public static final int NUM_PARTICLES = 30;

    //In-script variables
    public static final Vector2f ZERO = new Vector2f();
    private boolean hasFiredThisCharge = false;
    private boolean runOnce = true;

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused() || weapon == null) {
            return;
        }

        float chargelevel = weapon.getChargeLevel();

        if (hasFiredThisCharge && (chargelevel <= 0f || !weapon.isFiring())) {
            hasFiredThisCharge = false;
        }

        if (chargelevel <= 0f) {
            runOnce = true;
        }

        //Muzzle location calculation
        Vector2f point = new Vector2f();

        if (weapon.getSlot().isHardpoint()) {
            point.x = weapon.getSpec().getHardpointFireOffsets().get(0).x;
            point.y = weapon.getSpec().getHardpointFireOffsets().get(0).y;
        } else if (weapon.getSlot().isTurret()) {
            point.x = weapon.getSpec().getTurretFireOffsets().get(0).x;
            point.y = weapon.getSpec().getTurretFireOffsets().get(0).y;
        } else {
            point.x = weapon.getSpec().getHiddenFireOffsets().get(0).x;
            point.y = weapon.getSpec().getHiddenFireOffsets().get(0).y;
        }

        point = VectorUtils.rotate(point, weapon.getCurrAngle(), new Vector2f(0f, 0f));
        point.x += weapon.getLocation().x;
        point.y += weapon.getLocation().y;

        //Chargeup visuals
        if (chargelevel > 0f && !hasFiredThisCharge) {
            if (runOnce) {
                //Global.getSoundPlayer().playSound(CHARGE_SOUND_ID, 1f, 1f, weapon.getLocation(), weapon.getShip().getVelocity());
                runOnce = false;
            }
        }

        //Firing visuals
        if (chargelevel >= 1f && !hasFiredThisCharge) {
            hasFiredThisCharge = true;

            Global.getCombatEngine().addSwirlyNebulaParticle(point, ZERO, 100, 0.8f, 0.2f, 0.2f, 0.6f, FLASH_COLOR, false);
            engine.addSmoothParticle(point, ZERO, 70f, 0.7f, 0.1f, PARTICLE_COLOR);
            engine.addSmoothParticle(point, ZERO, 140f, 0.7f, 1f, GLOW_COLOR);
            engine.addHitParticle(point, ZERO, 260f, 1f, 0.05f, FLASH_COLOR);
            for (int x = 0; x < NUM_PARTICLES; x++) {
                engine.addHitParticle(point,
                        MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(50f, 110f), (float) Math.random() * 360f),
                        3f, 1f, MathUtils.getRandomNumberInRange(0.3f, 0.6f), PARTICLE_COLOR);
            }
        }
    }
}


