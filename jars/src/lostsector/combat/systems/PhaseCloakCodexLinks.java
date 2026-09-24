package lostsector.combat.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.codex.CodexDataV2;

import java.util.Set;

public class PhaseCloakCodexLinks {

    private static final Set<String> CLOAK_IDS = Set.of("nskr_bosscloak", "nskr_poorcloak");

    // Ship system entries stay hidden and locked until a related entry is visible and unlocked,
    // and CodexDataV2.linkRelatedEntries() never relates a phase hull to its defense system.
    public static void link() {
        for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
            String defenseId = spec.getShipDefenseId();
            if (!spec.isPhase() || !CLOAK_IDS.contains(defenseId)) continue;
            CodexDataV2.makeRelated(CodexDataV2.getShipSystemEntryId(defenseId), CodexDataV2.getShipEntryId(spec.getHullId()));
        }
    }
}
