package lostsector.helper;

import com.fs.starfarer.api.Global;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Music {

    public static final String SOUNDS_PATH = "data/config/sounds.json";

    // sounds.json does not change while the game runs.
    private static final Map<String, List<String>> FILES_BY_SET = new HashMap<>();

    public static boolean isPlaying(String musicSetId) {
        String current = Global.getSoundPlayer().getCurrentMusicId();
        if (current == null) return false;
        // getCurrentMusicId() names the playing file with its path, not the music set.
        current = current.replace('\\', '/');
        for (String file : getFiles(musicSetId)) {
            if (current.equals(file) || current.endsWith("/" + file)) return true;
        }
        return false;
    }

    public static void stopIfPlaying(String musicSetId) {
        if (isPlaying(musicSetId)) Global.getSoundPlayer().pauseCustomMusic();
    }

    private static List<String> getFiles(String musicSetId) {
        List<String> files = FILES_BY_SET.get(musicSetId);
        if (files != null) return files;

        files = new ArrayList<>();
        try {
            JSONObject music = Global.getSettings().getMergedJSON(SOUNDS_PATH).optJSONObject("music");
            JSONArray entries = music == null ? null : music.optJSONArray(musicSetId);
            if (entries != null) {
                for (int i = 0; i < entries.length(); i++) {
                    JSONObject entry = entries.optJSONObject(i);
                    if (entry == null) continue;
                    String file = entry.optString("file", null);
                    if (file != null) files.add(file.replace('\\', '/'));
                    JSONArray list = entry.optJSONArray("files");
                    if (list == null) continue;
                    for (int j = 0; j < list.length(); j++) {
                        files.add(list.getString(j).replace('\\', '/'));
                    }
                }
            }
        } catch (Exception e) {
            Global.getLogger(Music.class).error("Could not read music set " + musicSetId + " from " + SOUNDS_PATH, e);
        }
        if (files.isEmpty()) {
            Global.getLogger(Music.class).warn("Music set " + musicSetId + " has no files in " + SOUNDS_PATH);
        }
        FILES_BY_SET.put(musicSetId, files);
        return files;
    }
}
