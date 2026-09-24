package lostsector.settings;

import com.fs.starfarer.api.Global;
import lostsector.ModPlugin;

import java.util.Map;

public enum Difficulty {

    // Same order as the difficulty options in data/config/LunaSettings.csv.
    NORMAL(null, null),
    EASY(0.7f, 0.6f),
    STARFARER(1.2f, 1.25f);

    // Null means the fleet scaling settings apply.
    private final Float fixedScriptedFleetMult;
    private final Float fixedRandomEnigmaFleetMult;

    Difficulty(Float fixedScriptedFleetMult, Float fixedRandomEnigmaFleetMult) {
        this.fixedScriptedFleetMult = fixedScriptedFleetMult;
        this.fixedRandomEnigmaFleetMult = fixedRandomEnigmaFleetMult;
    }

    public static Difficulty current() {
        return Setting.DIFFICULTY.getOption(Difficulty.class);
    }

    public static float scriptedFleetMult() {
        Float fixed = current().fixedScriptedFleetMult;
        return fixed != null ? fixed : Setting.SCRIPTED_FLEET_SCALING.getFloat();
    }

    public static float randomEnigmaFleetMult() {
        Float fixed = current().fixedRandomEnigmaFleetMult;
        return fixed != null ? fixed : Setting.RANDOM_ENIGMA_FLEET_SCALING.getFloat();
    }

    public static boolean isStarfarer() {
        return current() == STARFARER;
    }

    /** A campaign that leaves Starfarer difficulty can no longer count as a Starfarer run. */
    public static void clearStarfarerFromStartUnlessStarfarer() {
        if (isStarfarer()) return;
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (data.containsKey(ModPlugin.STARFARER_MODE_FROM_START_KEY)) {
            data.put(ModPlugin.STARFARER_MODE_FROM_START_KEY, false);
        }
    }
}
