package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.nskr_kestevenQuest;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;
import scripts.kissa.LOST_SECTOR.util.blastSpriteCreator;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class nskr_protExplosion {
    //
    //custom ship explosion for prot and enigma ships
    //
    //shockwave
    public static final String SPRITE_PATH = "graphics/fx/nskr_blast1.png";
    public static final Color SHOCKWAVE_COLOR = new Color(255, 43, 86, 25);
    public static final float SHOCKWAVE_SIZE = 900f;
    public static final float SHOCKWAVE_SPEED = 600f;

    public static final Color PARTICLE_COLOR = new Color(255, 6, 43, 255);

    public static final Color NEBULA_COLOR = new Color(255, 164, 184, 105);

    public static final Color LIGHTNING_CORE_COLOR = new Color(222, 7, 255, 150);
    public static final Color LIGHTNING_FRINGE_COLOR = new Color(255, 6, 6, 100);
    public static final Map<ShipAPI.HullSize, Float> SIZE_BONUS = new HashMap<>();
    static {
        SIZE_BONUS.put(ShipAPI.HullSize.FRIGATE, 1f);
        SIZE_BONUS.put(ShipAPI.HullSize.DESTROYER, 1.25f);
        SIZE_BONUS.put(ShipAPI.HullSize.CRUISER, 1.5f);
        SIZE_BONUS.put(ShipAPI.HullSize.CAPITAL_SHIP, 2f);
    }

    static void log(final String message) {
        Global.getLogger(nskr_protExplosion.class).info(message);
    }

    public static class nskr_protExplosionListener implements AdvanceableListener {

        public ShipAPI ship;

        public nskr_protExplosionListener(ShipAPI ship) {
            this.ship = ship;
        }

        public void advance(float amount) {
            //AED check
            nskr_aed.ShipSpecificData data = (nskr_aed.ShipSpecificData) Global.getCombatEngine().getCustomData().get("KABOOM_DATA_KEY" + ship.getId());
            if (data != null) {
                //we are already exploding
                if (data.remove){
                    //delete
                    ship.removeListener(this);
                }
            }

            if (Global.getCombatEngine().isPaused() || ship.isAlive()) {
                return;
            }

            //Global.getCombatEngine().addFloatingText(ship.getLocation(), "LMAO", 64f, Color.RED, null,1f,1f);
            float mag = SIZE_BONUS.get(ship.getHullSize());
            //shockwave
            float size = ((SHOCKWAVE_SIZE+ship.getCollisionRadius())*mag);
            float duration = size/SHOCKWAVE_SPEED;
            //Global.getCombatEngine().addFloatingText(ship.getLocation(), "Speed "+duration +" size "+ size, 64f, Color.CYAN, null, 1f,1f);
            blastSpriteCreator.blastSpriteListener shockwave = new blastSpriteCreator.blastSpriteListener(ship, ship.getLocation(), duration, size, SHOCKWAVE_COLOR);
            //shockwave.baseSize = Math.max(ship.getCollisionRadius()-150f, 0f);
            shockwave.customSpritePath = SPRITE_PATH;
            shockwave.sizeEaseOutSine = true;
            shockwave.startSizeMult = 0f;
            ship.addListener(shockwave);

            //particle fx
            Vector2f particlePos, particleVel;
            Color color = util.randomiseColor(PARTICLE_COLOR, 50, 25,0,25,false);
            for (int x = 0; x < 200*mag; x++) {
                particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), (float) Math.random() * (ship.getCollisionRadius() * 1.5f));
                particleVel = Vector2f.sub(particlePos, ship.getLocation(), null);
                Global.getCombatEngine().addSmokeParticle(particlePos, mathUtil.scaleVector(particleVel, 4f), 4f, 0.67f, 2.0f,
                        color);
            }
            //nebula fx
            Global.getCombatEngine().addNebulaParticle(
                    ship.getLocation(), mathUtil.scaleVector(ship.getVelocity(),0.5f), (ship.getCollisionRadius()+100f)*mag,
                    1.20f, 0.5f,1.5f,4f,
                    NEBULA_COLOR);

            //EMP fx
            for (int x = 0; x < 24*mag; x++) {
                Vector2f empPos, empPosTo;
                float radius = (float)Math.random()*(150f*mag)+(ship.getCollisionRadius()-50f);
                empPos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), radius);
                float angle = VectorUtils.getAngle(ship.getLocation(),empPos);
                empPosTo = MathUtils.getPointOnCircumference(ship.getLocation(), radius,
                        angle+mathUtil.getRandomNumberInRangeExcludingRange(-70f, 70f,-30f,30f));

                Global.getCombatEngine().spawnEmpArcVisual(empPos, new SimpleEntity(empPos), empPosTo, new SimpleEntity(empPosTo),
                        20f, // thickness of the lightning bolt
                        LIGHTNING_CORE_COLOR, //Central color
                        LIGHTNING_FRINGE_COLOR //Fringe Color
                );
            }
            //distortion fx
            WaveDistortion wave = new WaveDistortion();
            wave.setLocation(ship.getLocation());
            wave.setIntensity(ship.getCollisionRadius() * 0.08f);
            wave.setSize(ship.getCollisionRadius() * 0.75f * mag);
            wave.fadeInSize(1.2f);
            wave.fadeOutIntensity(0.9f);
            DistortionShader.addDistortion(wave);

            //delete
            ship.removeListener(this);
        }
    }
}
