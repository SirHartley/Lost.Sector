//code by Vayra, kudos
//from Tahlan Shipworks
package scripts.kissa.LOST_SECTOR.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import scripts.kissa.LOST_SECTOR.util.ids;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class nskr_kestevenMirror implements EveryFrameScript {

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

        FactionAPI f = Global.getSector().getFaction(ids.KESTEVEN_FACTION_ID);
        FactionSpecAPI fSpec = f.getFactionSpec();

        //for (String weapon : Global.getSector().getFaction(Factions.INDEPENDENT).getKnownWeapons()) {
        //    if (!f.knowsWeapon(weapon)) {
        //        f.addKnownWeapon(weapon, true);
        //    }
        //}
        
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


        //can't remove ships I guess

        //remove banned hulls
        //List<String> toRemoveShip = new ArrayList<>();
        //List<String> toRemoveBase = new ArrayList<>();
        //for (String ship : f.getKnownShips()){
        //    for (String s : BANNED_SHIPS){
        //        if (ship.equals(s)){
        //            toRemoveShip.add(ship);
        //            break;
        //        }
        //    }
        //}
        //for (String baseShip : f.getAlwaysKnownShips()) {
        //    for (String s : BANNED_SHIPS){
        //        if (baseShip.equals(s)){
        //            toRemoveBase.add(baseShip);
        //            break;
        //        }
        //    }
        //}
        //for (String s : toRemoveShip){
        //    nskr_kestevenExportManager.setFreq(s, 0f, fSpec);
        //    f.removeKnownShip(s);
        //}
        //for (String s : toRemoveBase){
        //    nskr_kestevenExportManager.setFreq(s, 0f, fSpec);
        //    f.removeUseWhenImportingShip(s);
        //}
    }
}
