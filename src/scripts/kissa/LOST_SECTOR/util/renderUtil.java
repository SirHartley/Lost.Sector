package scripts.kissa.LOST_SECTOR.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;

public class renderUtil {

    public static final String SPRITE_PATH = "graphics/fx/nskr_glow1.png";
    public static void renderGlow(Vector2f point, float size, Color color) {

        SpriteAPI temp = Global.getSettings().getSprite(SPRITE_PATH);
        Vector2f sizeV = new Vector2f(size * 2f, size * 2f);

        MagicRender.singleframe(temp, point, sizeV, 0, color, true);
    }
}
