package lostsector.helper;

import com.fs.starfarer.api.Global;

public class UiSounds {

    public static void playUiStaticNoise(){
        Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);
    }

    public static void playUiRepRaiseNoise(){
        Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);
    }

    public static void playUiRepDropNoise(){
        Global.getSoundPlayer().playUISound("ui_rep_drop",1f,1f);
    }
}
