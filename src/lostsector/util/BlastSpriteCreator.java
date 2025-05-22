package lostsector.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.io.IOException;

public class BlastSpriteCreator {
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
            //Global.getCombatEngine().addFloatingText(source.getLocation(), "KEKW", 64f, Color.RED, null,1f,1f);

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
            timerNorm = MathUtilLS.normalize(duration-elapsed, 0f, duration);
            float timerSize = timerNorm;
            float timerAlpha = timerNorm;

            //engine.addFloatingText(ship.getLocation(), "timer " + timerNorm, 48f, Color.cyan, ship, 0.5f, 1.0f);

            //easing functions
            if (sizeEaseInCubic) timerSize = MathUtilLS.easeInCubic(timerSize);
            if (sizeEaseOutCubic) timerSize = MathUtilLS.easeOutCubic(timerSize);
            if (sizeEaseInQuint) timerSize = MathUtilLS.easeInQuint(timerSize);
            if (sizeEaseOutQuint) timerSize = MathUtilLS.easeOutQuint(timerSize);
            if (sizeEaseInSine) timerSize = MathUtilLS.easeInSine(timerSize);
            if (sizeEaseOutSine) timerSize = MathUtilLS.easeOutSine(timerSize);
            if (sizeEaseInQuad) timerSize = MathUtilLS.easeInQuad(timerSize);
            if (sizeEaseOutQuad) timerSize = MathUtilLS.easeOutQuad(timerSize);

            if (alphaEaseInCubic) timerAlpha = MathUtilLS.easeInCubic(timerAlpha);
            if (alphaEaseOutCubic) timerAlpha = MathUtilLS.easeOutCubic(timerAlpha);
            if (alphaEaseInQuint) timerAlpha = MathUtilLS.easeInQuint(timerAlpha);
            if (alphaEaseOutQuint) timerAlpha = MathUtilLS.easeOutQuint(timerAlpha);
            if (alphaEaseInSine) timerAlpha = MathUtilLS.easeInSine(timerAlpha);
            if (alphaEaseOutSine) timerAlpha = MathUtilLS.easeOutSine(timerAlpha);
            if (alphaEaseInQuad) timerAlpha = MathUtilLS.easeInQuad(timerAlpha);
            if (alphaEaseOutQuad) timerAlpha = MathUtilLS.easeOutQuad(timerAlpha);

            //color shift
            color = MiscLS.blendColors(color, colorOut, timerAlpha);
            //lerp size
            vSize = MathUtilLS.lerp(vSize * startSizeMult, vSize * endSizeMult, MathUtilLS.smoothStep(timerSize));
            size = new Vector2f(vSize+baseSize, vSize+baseSize);
            //lerp alpha
            alpha = MiscLS.clamp255((int) MathUtilLS.lerp(alpha * 1.0f, alpha * 0.0f, MathUtilLS.smoothStep(timerAlpha)));
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
