//////////////////////
//Initially created by Nicke535 and modified from LoA
//////////////////////
package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.magiclib.util.MagicRender;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;

public class nskr_massTargetingStats extends BaseShipSystemScript {

    //The range at which the AOE benefits are applied
    private static final float ACTIVE_RANGE = 1800f;

    //as multipliers (1.3f means 30% extra range and so on)
    public static final float TARGETING_BONUS = 2.0f;

    //The name of the stat used to track which bonus level we're at. Just keep it unique and everything should work out fine
    public static final String TRACKER_STAT_ID = "NSKR_TRACKER_STAT_ID";

    //ID for the bonus stat; same as above, can be anything as long as it's unique
    public static final String BONUS_ID = "NSKR_TARGETING_BONUS_ID";

    // sprite path - necessary if loaded here and not in settings.json
    public static final String SPRITE_PATH = "graphics/fx/shields256.png";
    //base
    public static final Color COLOR = new Color(68, 207, 126, 10);

    //In-script variables
    private SpriteAPI sprite = null;
    private SpriteAPI sprite2 = null;
    private float angle = 0f;
    private float timer = 0f;
    public static final Vector2f ZERO = new Vector2f();

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        //tracks and applies our buff
        if (!ship.hasListenerOfClass(nskr_massTargeting.nskr_massTargetingListener.class)) ship.addListener(new nskr_massTargeting.nskr_massTargetingListener(ship));

        float range = getMaxRange(ship);

        CombatEngineAPI engine = Global.getCombatEngine();
        sprite = Global.getSettings().getSprite(SPRITE_PATH);
        sprite2 = Global.getSettings().getSprite(SPRITE_PATH);

        if (effectLevel <= 0.001f) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        float amount = engine.getElapsedInLastFrame();

        //jitter
        Color color1 = util.randomiseColor(COLOR, 10, 5, 15, 5, true);
        Color color2 = color1;

        //range is actually bigger
        Vector2f size = new Vector2f((range*2.1f)+150f, (range*2.1f)+150f);
        Vector2f size2 = size;

        //speen
        angle += amount;
        if (angle > 360) angle = 0f;
        //grow
        timer += amount;
        if (timer > 2f) timer = 0f;

        float alpha = color1.getAlpha()*effectLevel;
        color1 = util.setAlpha(color1,(int)alpha);
        float vSize = size2.getX();
        float nTimer = 0f;
        nTimer = mathUtil.normalize(timer,0f,2f);

        //engine.addFloatingText(ship.getLocation(), "timer " + nTimer, 48f, Color.cyan, ship, 0.5f, 1.0f);
        vSize = mathUtil.lerp(vSize*0.15f, vSize*1.25f, nTimer);
        size2 = new Vector2f(vSize, vSize);
        alpha = util.clamp255((int)mathUtil.lerp(alpha*1.0f, alpha*0.3f, nTimer));
        color2 = util.setAlpha(color1,(int)alpha);

        MagicRender.singleframe(sprite, ship.getLocation(), size, angle, color1, false);
        //second one that changes size
        MagicRender.singleframe(sprite2, ship.getLocation(), size2, angle, color2, false);

    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            id = id + "_" + ship.getId();
        } else {
            return;
        }
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Targeting array active", false);
        }
        return null;
    }

    public static float getMaxRange(ShipAPI ship){
        if (ship==null) return 0f;
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(ACTIVE_RANGE);
    }
}
