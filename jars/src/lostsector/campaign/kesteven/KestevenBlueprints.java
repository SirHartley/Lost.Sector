//code by Vayra, kudos
//from Tahlan Shipworks
package lostsector.campaign.kesteven;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import lostsector.helper.Ids;

import java.util.HashSet;
import java.util.Set;

public class KestevenBlueprints implements EveryFrameScript {

    public static final Set<String> BANNED_SHIPS = new HashSet<>();
    static {
        BANNED_SHIPS.add("gremlin");
        BANNED_SHIPS.add("condor");
        BANNED_SHIPS.add("buffalo_mk2");
        BANNED_SHIPS.add("buffalo");
        BANNED_SHIPS.add("hound");
        BANNED_SHIPS.add("cerberus");
        BANNED_SHIPS.add("shepherd");
        BANNED_SHIPS.add("wayfarer");
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
    }

    public static void borrowIndieBlueprints() {

        FactionAPI f = Global.getSector().getFaction(Ids.KESTEVEN_FACTION_ID);
        FactionSpecAPI fSpec = f.getFactionSpec();

        for (String ship : Global.getSector().getFaction(Factions.INDEPENDENT).getKnownShips()) {
            //ignore GH content
            if (Global.getSettings().getHullSpec(ship).hasTag("tahlan_knights")) continue;
            if (BANNED_SHIPS.contains(ship)) continue;
            if (!f.knowsShip(ship)) {
                f.addKnownShip(ship, true);
            }
        }
        
        for (String baseShip : Global.getSector().getFaction(Factions.INDEPENDENT).getAlwaysKnownShips()) {
            //ignore GH content
            if (Global.getSettings().getHullSpec(baseShip).hasTag("tahlan_knights")) continue;
            if (BANNED_SHIPS.contains(baseShip)) continue;
            if (!f.useWhenImportingShip(baseShip)) {
                f.addUseWhenImportingShip(baseShip);
            }
        }

        for (String fighter : Global.getSector().getFaction(Factions.INDEPENDENT).getKnownFighters()) {
            if (!f.knowsFighter(fighter)) {
                f.addKnownFighter(fighter, true);
            }
        }

    }
}
