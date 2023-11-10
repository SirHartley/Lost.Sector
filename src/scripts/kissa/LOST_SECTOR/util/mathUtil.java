package scripts.kissa.LOST_SECTOR.util;

import com.fs.starfarer.api.Global;
import org.lwjgl.util.vector.Vector2f;

import java.util.Map;
import java.util.Random;

public class mathUtil {

    static void log(final String message) {
        Global.getLogger(mathUtil.class).info(message);
    }

    public static float BiasFunction(double x, float bias) {
        double k =  Math.pow(1-bias, 3);
        // by Sebastian Lague
        // based on shadertoy.com/view/Xd2yRd
        // MIT License
        float f = (float) ((x * k) / (x * k - x + 1f));
        if (f>1f) return 1f;
        if (f<0f) return 0f;
        return f;
    }

    public static float lerp(float x, float y, float alpha) {
        return (1f - alpha) * x + alpha * y;
    }

    public static float smoothStep(float x) {
        if (x>1f) return 1f;
        if (x<0f) return 0f;
        return (x*x)*(3f-(2f*x));
    }

    /////////////////////////////
    //Easing functions originally by Andrey Sitnik and Ivan Solovev
    //modified to java
    /////////////////////////////
    public static float easeInCubic(float x) {
        if (x>1f) return 1f;
        if (x<0f) return 0f;
        return x * x * x;
    }
    public static float easeOutCubic(float x) {
        if (x>1f) return 1f;
        if (x<0f) return 0f;
        return 1f - (float)Math.pow(1f - x, 3f);
    }
    public static float easeInQuint(float x) {
        if (x>1f) return 1f;
        if (x<0f) return 0f;
        return x * x * x * x * x;
    }
    public static float easeOutQuint(float x) {
        if (x>1f) return 1f;
        if (x<0f) return 0f;
        return 1f - (float)Math.pow(1f - x, 5f);
    }
    public static float easeInSine(float x) {
        if (x>1f) return 1f;
        if (x<0f) return 0f;
        return 1f - (float)Math.cos((x * Math.PI) / 2f);
    }
    public static float easeOutSine(float x) {
        if (x>1f) return 1f;
        if (x<0f) return 0f;
        return (float)Math.sin((x * Math.PI) / 2f);
    }
    public static float easeInQuad(float x) {
        if (x>1f) return 1f;
        if (x<0f) return 0f;
        return x * x;
    }
    public static float easeOutQuad(float x) {
        if (x>1f) return 1f;
        if (x<0f) return 0f;
        return 1f - (1f - x) * (1f - x);
    }

    public static float normalize(float value, float min, float max){
        return (value - min) / (max - min);
    }

    public static Vector2f scaleVector(Vector2f vector, float mult){
        if (vector.length()>0f) {
            float targetLength = vector.length() * mult;
            vector.setX(targetLength * vector.getX() / vector.length());
            vector.setY(targetLength * vector.getY() / vector.length());
        }
        return vector;
    }

    public static float OneDimensionalPerlinNoise(String id, Random random, int size, int octaves, float persistence){
        if (Global.getCombatEngine()==null){
            log("mUtil ERROR no engine");
            return 0f;
        }
        for (int x = 0; x < octaves;) {
            float noise = octave(id+x,random,size/(x+1));
            //first octave
            if (x==0){
                setNoise(noise,id);
            } else {
                //more octaves
                float oldNoise = getNoise(id);
                float newNoise = lerp(oldNoise,noise,0.50f*(persistence/(x)));
                setNoise(smoothStep(newNoise), id);
            }
            x++;
        }
        return getNoise(id);
    }

    //gradient noise generator
    private static float octave(String id, Random random, int size){
        //init
        size = size-1;
        float bias1 = getBias(random, id+"1");
        float bias2 = getBias(random, id+"2");
        int count = getCount(id);

        float noise = lerp(bias1, bias2, (float) count / size);
        noise = smoothStep(noise);

        if (count % size == 0 && count > 0) {
            bias1 = bias2;
            bias2 = random.nextFloat();
            count = 0;
        }
        count++;
        //save to mem
        setBias(bias1, id + "1");
        setBias(bias2, id + "2");
        setCount(count, id);
        return noise;
    }

    //temp memory for function
    private static int getCount(String id) {
        Map<String, Object> data = Global.getCombatEngine().getCustomData();
        if (!data.containsKey(id)) data.put(id, 0);
        return (int)data.get(id);
    }

    private static void setCount(int count, String id) {
        Map<String, Object> data = Global.getCombatEngine().getCustomData();
        data.put(id, count);
    }

    private static float getBias(Random random, String id) {
        Map<String, Object> data = Global.getCombatEngine().getCustomData();
        if (!data.containsKey(id)) data.put(id, random.nextFloat());
        return (float)data.get(id);
    }

    private static void setBias(float bias, String id) {
        Map<String, Object> data = Global.getCombatEngine().getCustomData();
        data.put(id, bias);
    }

    private static float getNoise(String id) {
        Map<String, Object> data = Global.getCombatEngine().getCustomData();
        if (!data.containsKey(id)) data.put(id, 0f);
        return (float)data.get(id);
    }

    private static void setNoise(float noise, String id) {
        Map<String, Object> data = Global.getCombatEngine().getCustomData();
        data.put(id, noise);
    }

    public static float getRandomNumberInRangeExcludingRange(float min, float max, float excludeMin, float excludeMax){
        float x = 0f;
        //fucked up input check
        if (min>excludeMin||max<excludeMax||min>max) return min;
        while (x==0f){
            x = (float)Math.random() * (max - min) + min;
            if (x>excludeMin&&x<excludeMax) x=0f;
        }
        return x;
    }

    //modified from LazyLib
    public static float getSeededRandomNumberInRange(float min, float max, Random random)
    {
        return random.nextFloat() * (max - min) + min;
    }

    //modified from LazyLib
    public static int getSeededRandomNumberInRange(int min, int max, Random random)
    {
        if (min >= max)
        {
            if (min == max)
            {
                return min;
            }
            return random.nextInt((min - max) + 1) + max;
        }
        return random.nextInt((max - min) + 1) + min;
    }

    public static float inverse(Float input) {
        if (input>1f) return 1f;
        if (input<0f) return 0f;
        return Math.abs(input-1f);
    }

    public static float roundToTwoDecimals(float rounded){

        rounded *= 100f;
        rounded = Math.round(rounded);
        rounded /= 100f;

        return rounded;
    }
}
