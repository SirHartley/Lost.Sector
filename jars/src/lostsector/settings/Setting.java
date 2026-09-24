package lostsector.settings;

import java.util.Locale;

/**
 * One constant per data row of {@code data/config/LunaSettings.csv} that code reads. The CSV owns
 * every label, default, range and option text; each row's fieldID is its constant's name in
 * lowerCamel case (SCRIPTED_FLEET_SCALING is scriptedFleetScaling).
 */
public enum Setting {

    DIFFICULTY(Difficulty.class),
    SCRIPTED_FLEET_SCALING(FieldType.DOUBLE),
    RANDOM_ENIGMA_FLEET_SCALING(FieldType.DOUBLE);

    enum FieldType {

        BOOLEAN("Boolean"),
        INT("Int"),
        DOUBLE("Double"),
        KEYCODE("Keycode"),
        RADIO("Radio");

        final String lunaName;

        FieldType(String lunaName) {
            this.lunaName = lunaName;
        }
    }

    final FieldType type;
    final Class<? extends Enum<?>> optionType;
    final String fieldId;

    Setting(FieldType type) {
        this(type, null);
    }

    Setting(Class<? extends Enum<?>> optionType) {
        this(FieldType.RADIO, optionType);
    }

    Setting(FieldType type, Class<? extends Enum<?>> optionType) {
        this.type = type;
        this.optionType = optionType;
        this.fieldId = toFieldId(name());
    }

    public boolean getBoolean() {
        return (Boolean) SettingsManager.value(this, FieldType.BOOLEAN);
    }

    public float getFloat() {
        return (float) getDouble();
    }

    public double getDouble() {
        return (Double) SettingsManager.value(this, FieldType.DOUBLE);
    }

    public int getInt() {
        return (Integer) SettingsManager.value(this, FieldType.INT);
    }

    public int getKeycode() {
        return (Integer) SettingsManager.value(this, FieldType.KEYCODE);
    }

    /** Returns the constant of {@code type} whose ordinal is the selected option's position in the CSV list. */
    public <E extends Enum<E>> E getOption(Class<E> type) {
        if (type != optionType) {
            throw new IllegalArgumentException("Lost.Sector setting " + fieldId + " does not select a " + type.getSimpleName());
        }
        int index = (Integer) SettingsManager.value(this, FieldType.RADIO);
        return type.getEnumConstants()[index];
    }

    private static String toFieldId(String constantName) {
        StringBuilder id = new StringBuilder();
        for (String word : constantName.toLowerCase(Locale.ROOT).split("_")) {
            if (id.length() == 0) {
                id.append(word);
            } else {
                id.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return id.toString();
    }
}
