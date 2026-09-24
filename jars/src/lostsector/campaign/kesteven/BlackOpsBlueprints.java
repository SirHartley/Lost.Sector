//code by Vayra, kudos
//from Tahlan Shipworks
package lostsector.campaign.kesteven;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

public class BlackOpsBlueprints {

    public static final String SPEC_OPS_ID = "prot_ops";

    public static void scanWeaponBlueprints() {
        for (FactionAPI f : Global.getSector().getAllFactions()) {
            for (String fighter : Global.getSector().getFaction(f.getId()).getKnownFighters()) {
                if (Global.getSettings().getFighterWingSpec(fighter).hasTag(Tags.RESTRICTED)) continue;
                if (Global.getSettings().getFighterWingSpec(fighter).hasTag("base_bp") || Global.getSettings().getFighterWingSpec(fighter).hasTag("pirate")) continue;
                if (!Global.getSector().getFaction(SPEC_OPS_ID).knowsFighter(fighter)) {
                    Global.getSector().getFaction(SPEC_OPS_ID).addKnownFighter(fighter, true);
                }
            }
        }
    }
}
