package lostsector.helper;

import com.fs.starfarer.api.Global;
import org.json.JSONObject;

public class UiSounds {

    // Merged sounds.json: sound ids are its top-level keys, beside "music". It does not change while the game runs.
    private static JSONObject sounds;

    public static void playUiStaticNoise(){
        Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);
    }

    public static void playUiRepRaiseNoise(){
        Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);
    }

    public static void playUiRepDropNoise(){
        Global.getSoundPlayer().playUISound("ui_rep_drop",1f,1f);
    }

    // Sound ids are unchecked strings until playback. True when the merged sounds.json cannot be read.
    public static boolean exists(String soundId) {
        if (soundId == null || soundId.equals("music")) return false;
        if (sounds == null) {
            try {
                sounds = Global.getSettings().getMergedJSON(Music.SOUNDS_PATH);
            } catch (Exception e) {
                Global.getLogger(UiSounds.class).error("Could not read " + Music.SOUNDS_PATH, e);
                return true;
            }
        }
        return sounds.has(soundId);
    }
}
