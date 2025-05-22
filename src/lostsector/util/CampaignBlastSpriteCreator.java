package lostsector.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CustomCampaignEntityPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.Random;

public class CampaignBlastSpriteCreator extends BaseCustomEntityPlugin {
    //
    //creates "blastwave" and other effects by rendering a sprite for x period
    //campaign version, doesn't get saved so don't use for permanent effects, fine for short ones
    //
    public static final String SPRITE_PATH = "graphics/fx/shields256.png";

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
    public float angle = 0f;
    private boolean finished = false;

    @Override
    public void init(SectorEntityToken entity, Object pluginParams) {
        super.init(entity, pluginParams);
        readResolve();
    }

    Object readResolve() {
        return this;
    }

    @Override
    public float getRenderRange() {
        return entity.getRadius() + 10000f;
    }

    @Override
    public void advance(float amount) {
        if (finished) return;
        if (Global.getSector().isPaused()) return;

        elapsed -= amount;

        //cleanup
        if (elapsed < 0f) {
            finished = true;
            entity.getContainingLocation().removeEntity(entity);
        }
    }

    @Override
    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
        if (finished) return;

        //needs to be a unique spriteAPI per shockwave, or MagicLib does some weirdness
        SpriteAPI temp;
        if (customSpritePath.length() > 0) {
            temp = getSprite(customSpritePath);
        } else {
            temp = getSprite(SPRITE_PATH);
        }
        Vector2f size = new Vector2f((this.size - baseSize) * 2f, (this.size - baseSize) * 2f);

        float alpha = color.getAlpha();
        float vSize = size.getX();
        float timerNorm = 0f;
        timerNorm = MathUtilLS.normalize(duration - elapsed, 0f, duration);
        float timerSize = timerNorm;
        float timerAlpha = timerNorm;

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
        size = new Vector2f(vSize + baseSize, vSize + baseSize);
        //lerp alpha
        alpha = MiscLS.clamp255((int) MathUtilLS.lerp(alpha * 1.0f, alpha * 0.0f, MathUtilLS.smoothStep(timerAlpha)));
        Color color2 = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) alpha);

        temp.setSize(size.x, size.y);
        temp.setAngle(angle);
        temp.setColor(color2);
        temp.setAlphaMult(color2.getAlpha()/255f);
        temp.setNormalBlend();

        temp.renderAtCenter(point.x, point.y);

        super.render(layer, viewport);
    }

    private SpriteAPI getSprite(String path) {
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

    public static CampaignBlastSpriteCreator setupRender(Vector2f loc, SectorEntityToken entity, Random random, float duration, Color color){

        SectorEntityToken token = entity.getContainingLocation().addCustomEntity("nskr_blast_"+new Random().nextLong(), "", "nskr_blast", Factions.NEUTRAL, null);
        token.setLocation(loc.x, loc.y);
        CustomCampaignEntityPlugin plugin = token.getCustomPlugin();
        CampaignBlastSpriteCreator blast = (CampaignBlastSpriteCreator) plugin;
        blast.point = new Vector2f(loc.x, loc.y);
        blast.angle = 360f * random.nextFloat();

        blast.duration = duration;
        blast.elapsed = blast.duration;
        blast.color = color;
        blast.colorOut = blast.color;

        return blast;
    }
}