//////////////////////
//Initially created by Sundog and modified from Ruthless Sector
//////////////////////
package scripts.kissa.LOST_SECTOR;

import com.fs.starfarer.api.Global;

import java.util.*;

public class nskr_saved<T> {
    public static final String PREFIX = "nskr_";
    static Map<String, nskr_saved> instanceRegistry = new HashMap();

    public static void updatePersistentData() {
        for(nskr_saved saved : instanceRegistry.values()) {
            Global.getSector().getPersistentData().put(saved.key, saved.val);
        }
    }

    public static void deletePersistantData() {
        for(nskr_saved saved : instanceRegistry.values()) {
            Global.getSector().getPersistentData().remove(saved.key);
        }

        instanceRegistry.clear();
    }

    public static void loadPersistentData() {
        for(nskr_saved saved : instanceRegistry.values()) {
            if(Global.getSector().getPersistentData().containsKey(saved.key)) {
                saved.val = Global.getSector().getPersistentData().get(saved.key);

                if(saved.val == null) saved.val = saved.defaultVal;

                if(saved.val instanceof Collection) ((Collection)saved.defaultVal).clear();
                else if(saved.val instanceof Map) ((Map)saved.defaultVal).clear();
            } else if(saved.val != null && saved.val.getClass().isPrimitive()) {
                saved.val = saved.defaultVal;
            } else if(saved.val instanceof Collection) {
                ((Collection)saved.val).clear();
                ((Collection)saved.defaultVal).clear();
            } else if(saved.val instanceof Map) {
                ((Map)saved.val).clear();
                ((Map)saved.defaultVal).clear();
            } else {
                saved.val = saved.defaultVal;
            }
        }
    }

    public T val;
    private final T defaultVal;
    private final String key;

    public nskr_saved(String key, T defaultValue) {
        this.key = PREFIX + key;
        this.val = defaultValue;
        this.defaultVal = defaultValue;

        instanceRegistry.put(this.key, this);
    }
}

