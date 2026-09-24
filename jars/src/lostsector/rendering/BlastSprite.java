package lostsector.rendering;

import lostsector.helper.MathHelper;
import lostsector.helper.MiscHelper;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.io.IOException;

public class BlastSprite {
    //
    //creates "blastwave" and other effects by rendering a sprite for x period
    //
    public static final String SPRITE_PATH = "graphics/fx/shields256.png";
    public static class blastSpriteListener implements AdvanceableListener {

        public float duration;
        public float elapsed;
        public float size;
        public Color color;
        public Color colorOut;
        public Vector2f point;
        public ShipAPI source;
        //custom
        public String customSpritePath = "";
        public float baseSize = 0f;
        public float startSizeMult = 1f;
        public float endSizeMult = 1f;
        //easing
        public boolean sizeEaseInCubic = false;
        public boolean alphaEaseInCubic = false;
        public boolean sizeEaseOutCubic = false;
        public boolean alphaEaseOutCubic = false;
        public boolean sizeEaseInQuint = false;
        public boolean alphaEaseInQuint = false;
        public boolean sizeEaseOutQuint = false;
        public boolean alphaEaseOutQuint = false;
        public boolean sizeEaseInSine = false;
        public boolean alphaEaseInSine = false;
        public boolean sizeEaseOutSine = false;
        public boolean alphaEaseOutSine = false;
        public boolean sizeEaseOutQuad = false;
        public boolean alphaEaseOutQuad = false;
        public boolean sizeEaseInQuad = false;
        public boolean alphaEaseInQuad = false;
        public boolean additive = false;
        //in script don't touch
        private boolean loaded = false;
        private float angle = 0f;

        public blastSpriteListener(ShipAPI source, Vector2f point, float duration, float size, Color color) {
            this.source = source;
            this.duration = duration;
            this.elapsed = duration;
            this.size = size;
            this.point = new Vector2f(point.x, point.y);
            this.color = color;
            colorOut = color;
            angle = 360f*(float)Math.random();
        }

        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused())return;

            elapsed -= amount;
            //needs to be a unique spriteAPI per shockwave, or MagicLib does some weirdness
            SpriteAPI temp;
            if (customSpritePath.length()>0){
                temp = getSprite(customSpritePath);
            } else {
                temp = getSprite(SPRITE_PATH);
            }
            Vector2f size = new Vector2f((this.size-baseSize) * 2f, (this.size-baseSize) * 2f);

            float alpha = color.getAlpha();
            float vSize = size.getX();
            float timerNorm = 0f;
            timerNorm = MathHelper.normalize(duration-elapsed, 0f, duration);
            float timerSize = timerNorm;
            float timerAlpha = timerNorm;

            //easing functions
            if (sizeEaseInCubic) timerSize = MathHelper.easeInCubic(timerSize);
            if (sizeEaseOutCubic) timerSize = MathHelper.easeOutCubic(timerSize);
            if (sizeEaseInQuint) timerSize = MathHelper.easeInQuint(timerSize);
            if (sizeEaseOutQuint) timerSize = MathHelper.easeOutQuint(timerSize);
            if (sizeEaseInSine) timerSize = MathHelper.easeInSine(timerSize);
            if (sizeEaseOutSine) timerSize = MathHelper.easeOutSine(timerSize);
            if (sizeEaseInQuad) timerSize = MathHelper.easeInQuad(timerSize);
            if (sizeEaseOutQuad) timerSize = MathHelper.easeOutQuad(timerSize);

            if (alphaEaseInCubic) timerAlpha = MathHelper.easeInCubic(timerAlpha);
            if (alphaEaseOutCubic) timerAlpha = MathHelper.easeOutCubic(timerAlpha);
            if (alphaEaseInQuint) timerAlpha = MathHelper.easeInQuint(timerAlpha);
            if (alphaEaseOutQuint) timerAlpha = MathHelper.easeOutQuint(timerAlpha);
            if (alphaEaseInSine) timerAlpha = MathHelper.easeInSine(timerAlpha);
            if (alphaEaseOutSine) timerAlpha = MathHelper.easeOutSine(timerAlpha);
            if (alphaEaseInQuad) timerAlpha = MathHelper.easeInQuad(timerAlpha);
            if (alphaEaseOutQuad) timerAlpha = MathHelper.easeOutQuad(timerAlpha);

            //color shift
            color = MiscHelper.blendColors(color, colorOut, timerAlpha);
            //lerp size
            vSize = MathHelper.lerp(vSize * startSizeMult, vSize * endSizeMult, MathHelper.smoothStep(timerSize));
            size = new Vector2f(vSize+baseSize, vSize+baseSize);
            //lerp alpha
            alpha = MiscHelper.clamp255((int) MathHelper.lerp(alpha * 1.0f, alpha * 0.0f, MathHelper.smoothStep(timerAlpha)));
            Color color2 = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) alpha);

            MagicRender.singleframe(temp, point, size, angle, color2, additive);
            //cleanup
            if (elapsed<0f){
                source.removeListener(this);
            }
        }

        private SpriteAPI getSprite(String path){
            SpriteAPI sprite;
            // Load sprite if it hasn't been loaded yet - not needed if you add it to settings.json
            if (!loaded) {
                loaded = true;
                try {
                    Global.getSettings().loadTexture(path);
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to load sprite '" + path + "'!", ex);
                }
            }
            sprite = Global.getSettings().getSprite(path);
            return sprite;
        }
    }
}
