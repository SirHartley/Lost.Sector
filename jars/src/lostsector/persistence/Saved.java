//////////////////////
//Initially created by Sundog and modified from Ruthless Sector
//////////////////////
package lostsector.persistence;

import com.fs.starfarer.api.Global;

import java.util.HashMap;
import java.util.Map;

public class Saved<T> {
    public static final String PREFIX = "nskr_";
    static Map<String, Saved> instanceRegistry = new HashMap();

    public static void clearRegistry() {
        instanceRegistry.clear();
    }

    public static void updatePersistentData() {
        for(Saved saved : instanceRegistry.values()) {
            Global.getSector().getPersistentData().put(saved.key, saved.val);
        }
    }

    // Owners are rebuilt on every game load (ModPlugin.createManagers), so defaultVal is always a
    // fresh object. After a save, the stored object is the live value itself and must not be cleared.
    public static void loadPersistentData() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        for(Saved saved : instanceRegistry.values()) {
            Object stored = data.get(saved.key);
            saved.val = stored != null ? stored : saved.defaultVal;
        }
    }

    public T val;
    private final T defaultVal;
    private final String key;

    public Saved(String key, T defaultValue) {
        this.key = PREFIX + key;
        this.val = defaultValue;
        this.defaultVal = defaultValue;

        instanceRegistry.put(this.key, this);
    }
}

