//////////////////////
//blendColors by theDragn from HTE
//////////////////////
package lostsector.rendering;

import org.lazywizard.lazylib.MathUtils;

import java.awt.*;

public class ColorHelper {

    public static final Color TT_ORANGE = new Color(255, 109, 31, 255);
    public static final Color BON_GREEN = new Color(142, 255, 21, 255);
    public static final Color NICE_YELLOW = new Color(255, 219, 36, 255);

    public static int clamp255(int x) {
        return Math.max(0, Math.min(255, x));
    }

    public static Color colorJitter(Color color, float amount) {
        return new Color(clamp255(color.getRed() + (int) (((float) Math.random() - 0.5f) * amount)),
                clamp255(color.getGreen() + (int) (((float) Math.random() - 0.5f) * amount)),
                clamp255(color.getBlue() + (int) (((float) Math.random() - 0.5f) * amount)),
                color.getAlpha());
    }

    public static Color blendColors(Color c1, Color c2, float ratio) {
        float iRatio = 1.0f - ratio;
        int a1 = c1.getAlpha();
        int r1 = c1.getRed();
        int g1 = c1.getGreen();
        int b1 = c1.getBlue();
        int a2 = c2.getAlpha();
        int r2 = c2.getRed();
        int g2 = c2.getGreen();
        int b2 = c2.getBlue();
        int a = (int)((float)a1 * iRatio + (float)a2 * ratio);
        int r = (int)((float)r1 * iRatio + (float)r2 * ratio);
        int g = (int)((float)g1 * iRatio + (float)g2 * ratio);
        int b = (int)((float)b1 * iRatio + (float)b2 * ratio);
        return new Color(r, g, b, a);
    }

    public static Color randomiseColor(Color inputColor, int rShift, int gShift, int bShift, int aShift, boolean addition){
        int rShift2 = rShift;
        int gShift2 = gShift;
        int bShift2 = bShift;
        int aShift2 = aShift;

        if (addition){
            rShift = 0;
            gShift = 0;
            bShift = 0;
            aShift = 0;
        } else {
            rShift = -rShift;
            gShift = -gShift;
            bShift = -bShift;
            aShift = -aShift;
        }

        int r = inputColor.getRed() + MathUtils.getRandomNumberInRange(rShift,rShift2);
        if (r>255){r = 255;} else if(r<0) r = 0;
        int g = inputColor.getGreen() + MathUtils.getRandomNumberInRange(gShift,gShift2);
        if (g>255){g = 255;} else if(g<0) g = 0;
        int b = inputColor.getBlue() + MathUtils.getRandomNumberInRange(bShift,bShift2);
        if (b>255){b = 255;} else if(b<0) b = 0;
        int a = inputColor.getAlpha() + MathUtils.getRandomNumberInRange(aShift,aShift2);
        if (a>255){a = 255;} else if(a<0) a = 0;

        return new Color(r,g,b,a);
    }

    public static Color shiftAlpha(Color color, float mult){
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = Math.min(Math.round(color.getAlpha()*mult), 255);

        return new Color(r,g,b,a);
    }

    public static Color setAlpha(Color color, int alpha) {
        return new Color(color.getRed(),color.getGreen(),color.getBlue(),clamp255(alpha));
    }
}
