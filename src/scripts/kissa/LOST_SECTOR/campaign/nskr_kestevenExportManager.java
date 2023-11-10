package scripts.kissa.LOST_SECTOR.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionProductionAPI;
import com.fs.starfarer.api.campaign.FactionSpecAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.*;

public class nskr_kestevenExportManager extends BaseCampaignEventListener implements EveryFrameScript  {

    //how often we run
    public static final float TIMER = 3f;
    nskr_saved<Float> counter;

    public static final Set<String> WEAPONS = new HashSet<>();
    public static final Set<String> SHIPS = new HashSet<>();
    public static final Set<String> FIGHTERS = new HashSet<>();

    static void log(final String message) {
        Global.getLogger(nskr_kestevenExportManager.class).info(message);
    }

    public nskr_kestevenExportManager() {
        super(false);
        this.counter = new nskr_saved<>("exportCounter", 0.0f);
    }

    public boolean isDone() {
        return false;
    }
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector().isPaused()) return;
        final nskr_saved<Float> counter = this.counter;

        if (Global.getSector().isInFastAdvance()) {
            counter.val += 2f*amount;
        } else{
            counter.val += amount;
        }

        //licensing fees logic
        int lpFee = getTotalLicenseProduction();
        saveProduction(lpFee);

        if (counter.val>TIMER*10f) {
            //setup export lists
            if (WEAPONS.isEmpty()){
                List<WeaponSpecAPI> weapons = Global.getSettings().getAllWeaponSpecs();
                for (WeaponSpecAPI w : weapons){
                    if (!w.hasTag("kesteven")) continue;
                    WEAPONS.add(w.getWeaponId());
                    log("ExportManager ADDED " + w.getWeaponId());
                }
            }
            if (SHIPS.isEmpty()){
                List<ShipHullSpecAPI> ships = Global.getSettings().getAllShipHullSpecs();
                for (ShipHullSpecAPI s : ships){
                    if (!s.hasTag("kesteven")) continue;
                    SHIPS.add(s.getHullId());
                    log("ExportManager ADDED " + s.getHullId());
                }
            }
            if (FIGHTERS.isEmpty()){
                List<FighterWingSpecAPI> fighters = Global.getSettings().getAllFighterWingSpecs();
                for (FighterWingSpecAPI f : fighters){
                    if (!f.hasTag("kesteven")) continue;
                    FIGHTERS.add(f.getId());
                    log("ExportManager ADDED " + f.getId());
                }
            }

            List<FactionAPI> allFactions = Global.getSector().getAllFactions();
            for (FactionAPI f : allFactions)
            {
                if (f.getId().equals("kesteven")) continue;
                ExportLevel level = getLevel(f.getId());
                if (level==ExportLevel.NONE || !util.kestevenExists()) {
                    stopExportBlueprints(f.getId());
                } else {
                    exportBlueprints(f.getId(), level);
                    log("ExportManager EXPORTING TO " + f.getId() + ", level " + level.toString());
                }
            }

            counter.val = 0f;
        }
    }

    public static void exportBlueprints(String faction, ExportLevel level) {
        FactionAPI f = Global.getSector().getFaction(faction);
        FactionSpecAPI fSpec = f.getFactionSpec();

        for (String weapon : WEAPONS) {
            if (!f.knowsWeapon(weapon)) {
                f.addKnownWeapon(weapon, true);
            }
        }

        for (String ship : SHIPS) {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(ship);
            if (!f.knowsShip(ship)) {
                //low
                if (level==ExportLevel.LOW && spec.getHullSize()!=ShipAPI.HullSize.DESTROYER && spec.getHullSize()!=ShipAPI.HullSize.FRIGATE) {
                    //cleanup old levels
                    if (f.knowsShip(ship)) {
                        setFreq(ship, 0f, fSpec);
                        f.removeKnownShip(ship);
                    }
                    if (f.useWhenImportingShip(ship)) {
                        setFreq(ship, 0f, fSpec);
                        f.removeUseWhenImportingShip(ship);
                    }
                    continue;
                }
                //med
                if (level==ExportLevel.CRUISER && spec.getHullSize()!=ShipAPI.HullSize.CRUISER && spec.getHullSize()!=ShipAPI.HullSize.DESTROYER && spec.getHullSize()!=ShipAPI.HullSize.FRIGATE) {
                    //cleanup old levels
                    if (f.knowsShip(ship)) {
                        setFreq(ship, 0f, fSpec);
                        f.removeKnownShip(ship);
                    }
                    if (f.useWhenImportingShip(ship)) {
                        setFreq(ship, 0f, fSpec);
                        f.removeUseWhenImportingShip(ship);
                    }
                    continue;
                }
                //high
                f.addKnownShip(ship, true);
                f.addUseWhenImportingShip(ship);
                //freq
                //setFreq(ship, 0.1f, fSpec);
            }
        }

        for (String fighter : FIGHTERS) {
            if (!f.knowsFighter(fighter)) {
                f.addKnownFighter(fighter, true);
            }
        }
    }

    public static void stopExportBlueprints(String faction) {
        FactionAPI f = Global.getSector().getFaction(faction);
        FactionSpecAPI spec = f.getFactionSpec();

        for (String weapon : WEAPONS) {
            if (f.knowsWeapon(weapon)) {
                f.removeKnownWeapon(weapon);
            }
        }

        for (String ship : SHIPS) {
            if (f.knowsShip(ship)) {
                setFreq(ship, 0f, spec);
                f.removeKnownShip(ship);
            }
        }

        for (String baseShip : SHIPS) {
            if (f.useWhenImportingShip(baseShip)) {
                setFreq(baseShip, 0f, spec);
                f.removeUseWhenImportingShip(baseShip);
            }
        }

        for (String fighter : FIGHTERS) {
            if (f.knowsFighter(fighter)) {
                f.removeKnownFighter(fighter);
            }
        }
    }

    public static void setFreq(String ship, float value, FactionSpecAPI spec) {
        HashMap<String, Float> map = new HashMap<>();
        map.put(ship, value);
        spec.setHullFrequency(map);
    }

    private ExportLevel getLevel(String faction) {
        //player only
        if (!faction.equals(Factions.PLAYER)) return ExportLevel.NONE;

        float r = Global.getSector().getFaction("kesteven").getRelationship(faction);
        if (r>=0.60f&&r<0.75f) return ExportLevel.LOW;
        if (r>=0.75f&&r<0.90f) return ExportLevel.CRUISER;
        if (r>=0.90f) return ExportLevel.CAPITAL;

        return ExportLevel.NONE;
    }

    private enum ExportLevel {
        NONE,
        LOW,
        CRUISER,
        CAPITAL
    }

    public static int getTotalLicenseProduction(){
        int lpFee = 0;
        for (FactionProductionAPI.ItemInProductionAPI item : Global.getSector().getFaction(Factions.PLAYER).getProduction().getCurrent()){
            if (item.getShipSpec() != null && !item.getShipSpec().hasTag("kesteven")) continue;
            if (item.getWeaponSpec() != null && !item.getWeaponSpec().hasTag("kesteven")) continue;
            if (item.getWingSpec() != null && !item.getWingSpec().hasTag("kesteven")) continue;
            lpFee += item.getBaseCost() * item.getQuantity();
        }
        return lpFee;
    }

    public static int getSavedProduction(){
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(nskr_licensingFees.TOTAL_KEY))
            data.put(nskr_licensingFees.TOTAL_KEY, 0);

        return (int)data.get(nskr_licensingFees.TOTAL_KEY);
    }

    public static void saveProduction(int amount){
        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(nskr_licensingFees.TOTAL_KEY, amount);
    }


}
