package scripts.kissa.LOST_SECTOR.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.hullmods.nskr_aed;
import scripts.kissa.LOST_SECTOR.util.blastSpriteCreator;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

import java.awt.*;
import java.util.List;

public class nskr_kaboomPlugin extends BaseEveryFrameCombatPlugin {

    //handles postmortem FX for aed

    // sprite path - necessary if loaded here and not in settings.json
    public static final String SPRITE_PATH = "graphics/fx/nskr_blast1.png";
    //base
    public static final Color SHOCKWAVE_COLOR = new Color(208, 36, 76, 12);
    public static final Vector2f ZERO = new Vector2f();

    static void log(final String message) {
        Global.getLogger(nskr_kaboomPlugin.class).info(message);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        //Iterates through all ships on the map
        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            nskr_aed.ShipSpecificData data = (nskr_aed.ShipSpecificData) Global.getCombatEngine().getCustomData().get("KABOOM_DATA_KEY" + ship.getId());
            if (data == null) continue;
            //HACK
            if (ship.getSystem().getState() == ShipSystemAPI.SystemState.ACTIVE){
                kaboom(data, ship);
            }
        Global.getCombatEngine().getCustomData().put("KABOOM_DATA_KEY" + ship.getId(), data);
        }
    }

    void kaboom(nskr_aed.ShipSpecificData data, ShipAPI ship) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (Global.getCombatEngine().isPaused()) {
            return;
        }
        float amount = engine.getElapsedInLastFrame();
        data.timer += amount;
        if (data.timer < 2.5f) {
            float timeOffset = data.timer*0.4f;

            Color color = new Color(255, 31, 57, 255);
            //nebula
            if (!data.doOnce) {
                Vector2f vel = mathUtil.scaleVector(ship.getVelocity(), 0.25f);
                for (int x = 0; x < 10; x++) {

                    float angle = (float) Math.random() * 360f;
                    float distance = (float) Math.random() * 250f;
                    Vector2f point = MathUtils.getPointOnCircumference(data.kLoc, distance, angle);

                    //engine.addFloatingText(data.kLoc, "lol " + vel.length(), 48.0f, Color.CYAN, ship, 6.00f, 1.00f);

                    engine.addNebulaParticle(point, vel,
                            MathUtils.getRandomNumberInRange(75f, 150f), MathUtils.getRandomNumberInRange(0.5f, 4.0f), 0.5f, 0.5f, MathUtils.getRandomNumberInRange(4f, 8f), color);
                    engine.addNegativeSwirlyNebulaParticle(point, vel,
                            MathUtils.getRandomNumberInRange(75f, 150f), MathUtils.getRandomNumberInRange(0.5f, 4.0f), 0.5f, 0.5f, MathUtils.getRandomNumberInRange(4f, 7f), color);

                    //blast sprite
                    blastSpriteCreator.blastSpriteListener shockwave = new blastSpriteCreator.blastSpriteListener(ship, ship.getLocation(), 1.75f, 2250f, SHOCKWAVE_COLOR);
                    shockwave.customSpritePath = SPRITE_PATH;
                    shockwave.alphaEaseOutSine = true;
                    shockwave.sizeEaseOutQuad = true;
                    shockwave.baseSize = 200f;
                    shockwave.startSizeMult = 0f;
                    ship.addListener(shockwave);

                    data.doOnce = true;
                }
            }

            //every frame fx
            for (int x = 0; x < 4; x++) {
                //engine.addFloatingText(data.kLoc, "lol " + data.timer, 32.0f, Color.CYAN, ship, 6.00f, 1.00f);

                Vector2f particlePos, particleVel;
                particlePos = MathUtils.getRandomPointOnCircumference(data.kLoc, mathUtil.lerp(200, 600, timeOffset));
                particleVel = Vector2f.sub(particlePos, data.kLoc, null);
                float targetLength = mathUtil.lerp(600, 0, timeOffset);
                particleVel.setX(targetLength * particleVel.getX() / particleVel.length());
                particleVel.setY(targetLength * particleVel.getY() / particleVel.length());

                color = new Color(255, 31, 57, (int) mathUtil.lerp(200, 150, timeOffset));
                //engine.addFloatingText(data.kLoc, "lol " + (int) nskr_util.lerp(255, 155, timeOffset), 32.0f, Color.CYAN, ship, 6.00f, 1.00f);

                engine.addNebulaParticle(particlePos, particleVel,
                        MathUtils.getRandomNumberInRange(75f, 125f), MathUtils.getRandomNumberInRange(1.0f, 2.0f), 0.5f, 0.5f, MathUtils.getRandomNumberInRange(2f, 4f), color);
                engine.addNegativeSwirlyNebulaParticle(particlePos, particleVel,
                        MathUtils.getRandomNumberInRange(75f, 125f), MathUtils.getRandomNumberInRange(1.0f, 2.0f), 0.5f, 0.5f, MathUtils.getRandomNumberInRange(2f, 4f), color);
            }
        } else data.kaboom = false;
        Global.getCombatEngine().getCustomData().put("KABOOM_DATA_KEY" + ship.getId(), data);
    }
}