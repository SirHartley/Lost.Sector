package lostsector.settings;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import lostsector.helper.Ids;
import lostsector.settings.Setting.FieldType;
import lunalib.backend.ui.settings.LunaSettingsLoader;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lazywizard.lazylib.JSONUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads each {@link Setting}'s definition from Lost.Sector's LunaSettings.csv and caches its current
 * value: the CSV default, replaced by LunaLib's stored value when there is one.
 */
public final class SettingsManager {

    private static final Logger log = Global.getLogger(SettingsManager.class);

    private static final String CSV_PATH = "data/config/LunaSettings.csv";
    private static final String LUNA_FILE = "LunaSettings/" + Ids.LOST_SECTOR_MOD_ID + ".json";
    private static final String LUNA_DEFAULT_FILE = "data/config/LunaSettingsDefault.default";
    private static final Set<String> DISPLAY_ONLY_TYPES = Set.of("Header", "Text");

    private static Map<Setting, Definition> definitions;
    private static Map<Setting, Object> values;

    private static final class Definition {

        final Setting setting;
        final Object defaultValue;
        final double min;
        final double max;
        final List<String> options;

        Definition(Setting setting, JSONObject row) {
            this.setting = setting;
            String type = row.optString("fieldType").trim();
            if (!setting.type.lunaName.equals(type)) {
                throw invalid(setting, "has fieldType '" + type + "', expected '" + setting.type.lunaName + "'");
            }
            min = parseBound(row, "minValue", Double.NEGATIVE_INFINITY);
            max = parseBound(row, "maxValue", Double.POSITIVE_INFINITY);
            options = setting.type == FieldType.RADIO ? parseOptions(row) : List.of();
            String rawDefault = row.optString("defaultValue").trim();
            defaultValue = normalize(parseDefault(rawDefault));
            if (defaultValue == null) {
                throw invalid(setting, "has an unusable defaultValue '" + rawDefault + "'");
            }
        }

        private Object parseDefault(String raw) {
            try {
                switch (setting.type) {
                    case BOOLEAN:
                        if (raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("false")) return Boolean.parseBoolean(raw);
                        return null;
                    case INT:
                    case KEYCODE:
                        return Integer.parseInt(raw);
                    case DOUBLE:
                        return Double.parseDouble(raw);
                    default:
                        return raw;
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private List<String> parseOptions(JSONObject row) {
            List<String> labels = new ArrayList<>();
            for (String label : row.optString("secondaryValue").split(",")) {
                labels.add(label.trim());
            }
            int expected = setting.optionType.getEnumConstants().length;
            if (labels.size() != expected) {
                throw invalid(setting, "lists " + labels.size() + " options, but " + setting.optionType.getSimpleName() + " has " + expected);
            }
            return labels;
        }

        /** Converts a LunaLib or code value to the cached form; null when it does not fit this setting. */
        Object normalize(Object raw) {
            switch (setting.type) {
                case BOOLEAN:
                    return raw instanceof Boolean ? raw : null;
                case INT:
                case KEYCODE:
                    return raw instanceof Number ? (int) clamp(((Number) raw).doubleValue()) : null;
                case DOUBLE:
                    return raw instanceof Number ? clamp(((Number) raw).doubleValue()) : null;
                case RADIO:
                    if (raw instanceof String) {
                        int index = options.indexOf(((String) raw).trim());
                        return index >= 0 ? index : null;
                    }
                    return setting.optionType.isInstance(raw) ? ((Enum<?>) raw).ordinal() : null;
                default:
                    return null;
            }
        }

        Object toLunaValue(Object cached) {
            return setting.type == FieldType.RADIO ? options.get((Integer) cached) : cached;
        }

        private double clamp(double value) {
            return Math.max(min, Math.min(max, value));
        }

        private static double parseBound(JSONObject row, String column, double fallback) {
            String raw = row.optString(column).trim();
            return raw.isEmpty() ? fallback : Double.parseDouble(raw);
        }
    }

    public static final class Listener implements LunaSettingsListener {

        @Override
        public void settingsChanged(String modId) {
            if (!Ids.LOST_SECTOR_MOD_ID.equals(modId)) return;
            refresh();
            if (Global.getCurrentState() == GameState.CAMPAIGN && Global.getSector() != null) {
                Difficulty.clearStarfarerFromStartUnlessStarfarer();
            }
        }
    }

    private SettingsManager() {
    }

    /** Called once from ModPlugin.onApplicationLoad; fails loudly when the CSV and {@link Setting} disagree. */
    public static void load() {
        JSONArray rows;
        try {
            // Not getMergedSpreadsheetDataForMod: that merges every enabled mod's LunaSettings.csv into ours.
            // LunaLib builds this mod's menu from this mod's own copy, loaded the same way.
            rows = Global.getSettings().loadCSV(CSV_PATH, Ids.LOST_SECTOR_MOD_ID);
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("Lost.Sector: could not read " + CSV_PATH, e);
        }

        Map<String, Setting> byFieldId = new HashMap<>();
        for (Setting setting : Setting.values()) {
            byFieldId.put(setting.fieldId, setting);
        }

        Map<Setting, Definition> loaded = new EnumMap<>(Setting.class);
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            String fieldId = row.optString("fieldID").trim();
            if (fieldId.isEmpty()) continue;
            Setting setting = byFieldId.get(fieldId);
            if (setting == null) {
                if (!DISPLAY_ONLY_TYPES.contains(row.optString("fieldType").trim())) {
                    log.warn("Lost.Sector: " + CSV_PATH + " row " + fieldId + " has no Setting constant and is never read");
                }
                continue;
            }
            if (loaded.containsKey(setting)) {
                throw invalid(setting, "appears more than once");
            }
            try {
                loaded.put(setting, new Definition(setting, row));
            } catch (NumberFormatException e) {
                throw invalid(setting, "has an unusable minValue or maxValue");
            }
        }

        for (Setting setting : Setting.values()) {
            if (!loaded.containsKey(setting)) {
                throw invalid(setting, "has no row");
            }
        }
        definitions = loaded;
        refresh();

        if (!LunaSettings.hasSettingsListenerOfClass(Listener.class)) {
            LunaSettings.addSettingsListener(new Listener());
        }
    }

    static Object value(Setting setting, FieldType expected) {
        requireLoaded(setting);
        if (setting.type != expected) {
            throw new IllegalArgumentException("Lost.Sector setting " + setting.fieldId + " is " + setting.type.lunaName + ", not " + expected.lunaName);
        }
        return values.get(setting);
    }

    /**
     * Stores a new value for {@code setting}, such as a progress unlock. The value takes effect in this
     * session even if LunaLib's file cannot be written.
     */
    public static void set(Setting setting, Object value) {
        requireLoaded(setting);
        Definition definition = definitions.get(setting);
        Object cached = definition.normalize(value);
        if (cached == null) {
            throw new IllegalArgumentException("Lost.Sector setting " + setting.fieldId + " cannot take the value " + value);
        }
        try {
            // LunaLib has no public setter. It keeps every mod's settings in memory and writes that copy to
            // this file when its menu is saved, so both the file and the in-memory copy must change.
            JSONUtils.CommonDataJSONObject stored = JSONUtils.loadCommonJSON(LUNA_FILE, LUNA_DEFAULT_FILE);
            stored.put(setting.fieldId, definition.toLunaValue(cached));
            stored.save();
            LunaSettingsLoader.getSettings().put(Ids.LOST_SECTOR_MOD_ID, stored);
        } catch (IOException | RuntimeException e) {
            log.error("Lost.Sector: could not store setting " + setting.fieldId + " in " + LUNA_FILE, e);
        }
        values.put(setting, cached);
    }

    private static void refresh() {
        Map<Setting, Object> refreshed = new EnumMap<>(Setting.class);
        for (Definition definition : definitions.values()) {
            Setting setting = definition.setting;
            Object stored = readLunaValue(setting);
            Object cached = stored == null ? null : definition.normalize(stored);
            if (stored != null && cached == null) {
                log.warn("Lost.Sector: LunaLib value '" + stored + "' of setting " + setting.fieldId + " is not valid; using the default");
            }
            refreshed.put(setting, cached != null ? cached : definition.defaultValue);
        }
        values = refreshed;
    }

    private static Object readLunaValue(Setting setting) {
        String modId = Ids.LOST_SECTOR_MOD_ID;
        switch (setting.type) {
            case BOOLEAN:
                return LunaSettings.getBoolean(modId, setting.fieldId);
            case INT:
            case KEYCODE:
                return LunaSettings.getInt(modId, setting.fieldId);
            case DOUBLE:
                return LunaSettings.getDouble(modId, setting.fieldId);
            case RADIO:
                return LunaSettings.getString(modId, setting.fieldId);
            default:
                return null;
        }
    }

    private static void requireLoaded(Setting setting) {
        if (values == null) {
            throw new IllegalStateException("Lost.Sector setting " + setting.fieldId + " used before the settings were loaded");
        }
    }

    private static RuntimeException invalid(Setting setting, String problem) {
        return new RuntimeException("Lost.Sector: " + CSV_PATH + " setting " + setting.fieldId + " " + problem);
    }
}
