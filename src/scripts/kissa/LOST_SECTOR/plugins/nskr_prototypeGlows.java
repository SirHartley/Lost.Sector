package scripts.kissa.LOST_SECTOR.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.awt.Color;

import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.util;


public class nskr_prototypeGlows implements EveryFrameWeaponEffectPlugin {

    //jitter and other fx for prot glows

    public static final Color BASE_COLOR = new Color(255, 180, 192, 5);
    private Color colorA = null;
    private final IntervalUtil colorInterval = new IntervalUtil(0.10f, 0.15f);

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        ShipAPI ship = weapon.getShip();
        SpriteAPI weap = weapon.getSprite();
        weapon.getAnimation().setFrame(0);
        float fluxRatio = 0f;
        fluxRatio = ship.getFluxTracker().getFluxLevel();

        colorInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
        if (colorInterval.intervalElapsed()) {
            //flux shift
            colorA = new Color(
                    ((util.clamp255(BASE_COLOR.getRed()))),
                    ((util.clamp255(BASE_COLOR.getGreen() - (int)(175 * fluxRatio)))),
                    ((util.clamp255(BASE_COLOR.getBlue() - (int)(160 * fluxRatio)))),
                    (util.clamp255(BASE_COLOR.getAlpha() + (int)(200 * fluxRatio))));
            //color jitter
            colorA = util.randomiseColor(colorA,(int)(-30 * fluxRatio), (int)(35 * fluxRatio), (int)(45 * fluxRatio), 0, false);
        }
        if (colorA!=null) {
            //engine.addFloatingText(ship.getLocation(), "ALPHA "+colorA.getAlpha(), 24f, Color.CYAN, null, 1f,1f);
            weap.setColor(colorA);
        } else {
            weap.setColor(BASE_COLOR);
        }

        Vector2f jitter = new Vector2f(weap.getWidth() / 2f, weap.getHeight() / 2f);

        jitter = MathUtils.getRandomPointInCircle(jitter, MathUtils.getRandomNumberInRange(0f, 3f)*fluxRatio);

        weap.setCenterX(jitter.getX());
        weap.setCenterY(jitter.getY());

        if (fluxRatio > 0f) {
            weapon.getAnimation().setFrame(1);
        }
    }
}
