package scripts.kissa.LOST_SECTOR.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.List;


public class nskr_entrancePlugin extends BaseEveryFrameCombatPlugin {

    //adds fx when deploying certain ships to combat

    public static final Color ENIGMA_COLOR = new Color(255,94,118,255);
    public static final Color ENIGMA_NEB_COLOR = new Color(255, 123, 127,105);
    public static final Color ENIGMA_P_COLOR = new Color(255, 49, 121,255);
    public static final Color PROT_COLOR = new Color(87,146,255,255);
    public static final Color PROT_NEB_COLOR = new Color(132, 175, 255,105);
    public static final Color PROT_P_COLOR = new Color(85, 235, 255,255);

    public static final String SOUND_ID = "nskr_entrance";

    public static final Vector2f ZERO = new Vector2f();
    private final IntervalUtil afterImageTimer;
    private StandardLight light;
    private WaveDistortion wave;

    public nskr_entrancePlugin() {
        this.afterImageTimer = new IntervalUtil(0.01f, 0.01f);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();

        //Iterates through all ships on the map
        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (ship==null) continue;
            if (util.protOrEnigma(ship)==null) continue;

            if (util.isProtTech(ship)){
                ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("ENTRANCE_DATA_KEY" + ship.getId());
                if (data == null) {
                    data = new ShipSpecificData();
                }

                Color afColor;
                Color nebColor;
                Color pColor;
                if (util.protOrEnigma(ship).equals("enigma")){
                    afColor = ENIGMA_COLOR;
                    nebColor = ENIGMA_NEB_COLOR;
                    pColor = ENIGMA_P_COLOR;
                } else {
                    afColor = PROT_COLOR;
                    nebColor = PROT_NEB_COLOR;
                    pColor = PROT_P_COLOR;
                }
                if (ship.getTravelDrive().getState()==ShipSystemAPI.SystemState.ACTIVE){

                    ship.setExtraAlphaMult(0.15f);
                    if (!Global.getCombatEngine().isPaused()) {
                        this.afterImageTimer.advance(Global.getCombatEngine().getElapsedInLastFrame());
                        if (this.afterImageTimer.intervalElapsed()) {
                            ship.addAfterimage(afColor, 0.0f, 0.0f, ship.getVelocity().x * -0.8f, ship.getVelocity().y * -0.8f,
                                    0.0f, 0.0f, 0.0f, 0.60f, true, true, false);
                        }

                        ship.setJitter(ship, afColor, 4, 4, 0.8f);
                        data.exited = true;
                    }
                } else if (data.exited){
                    ship.setExtraAlphaMult(1f);

                    if (!Global.getCombatEngine().isPaused()) {
                        //particle fx
                        Vector2f particlePos, particleVel;
                        for (int x = 0; x < 160; x++) {
                            particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), (float) Math.random() * (ship.getCollisionRadius() + 50f));
                            particleVel = Vector2f.sub(particlePos, ship.getLocation(), null);
                            Global.getCombatEngine().addSmokeParticle(particlePos, particleVel, 5f, 0.60f, 1.50f,
                                    pColor);
                        }
                        //nebula fx
                        engine.addSwirlyNebulaParticle(ship.getLocation(), VectorUtils.clampLength(ship.getVelocity(),ship.getMaxSpeed()),
                                ship.getCollisionRadius() * 3f, 1.25f,
                                0.5f, 1, 2.5f,
                                nebColor, false);
                        engine.addNebulaParticle(ship.getLocation(), VectorUtils.clampLength(ship.getVelocity(),ship.getMaxSpeed()),
                                ship.getCollisionRadius() * 2f, 1.25f,
                                0.5f, 1, 2,
                                nebColor, false);

                        //light fx
                        light = new StandardLight();
                        light.setLocation(ship.getLocation());
                        light.setIntensity(2.5f);
                        light.setSize((ship.getCollisionRadius() - 50f) * 1.5f);
                        light.setColor(pColor);
                        light.fadeOut(3f);
                        LightShader.addLight(light);

                        //distortion fx
                        wave = new WaveDistortion();
                        wave.setLocation(ship.getLocation());
                        wave.setSize((ship.getCollisionRadius() - 50f) * 5.00f);
                        wave.setIntensity((ship.getCollisionRadius() - 50f) * 0.35f);
                        wave.fadeInSize(1.2f);
                        wave.fadeOutIntensity(0.9f);
                        DistortionShader.addDistortion(wave);

                        float volume = 1.0f;
                        switch (ship.getHullSize()){
                            case FRIGATE:
                                volume = 0.6f;
                                break;
                            case DESTROYER:
                                volume = 0.9f;
                                break;
                            case CRUISER:
                                volume = 1.1f;
                                break;
                        }
                        //Global.getCombatEngine().addFloatingText(ship.getLocation(), "vol "+volume, 40f, Color.cyan, ship, 0.5f, 1.0f);

                        //sound
                        Global.getSoundPlayer().playSound(SOUND_ID, 1.0f, 1.0f*volume, ship.getLocation(), ship.getVelocity());

                        data.exited = false;
                    }
                }

            Global.getCombatEngine().getCustomData().put("ENTRANCE_DATA_KEY" + ship.getId(), data);
            }
        }
    }

    public static class ShipSpecificData {
        boolean exited = false;
    }
}