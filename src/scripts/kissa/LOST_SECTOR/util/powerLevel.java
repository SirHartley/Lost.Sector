package scripts.kissa.LOST_SECTOR.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.*;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

public class powerLevel {

    static void log(final String message) {
        Global.getLogger(powerLevel.class).info(message);
    }

    public static float get(float base, float minPower, float maxPower){
        float power = 1f;
        float totalValue = 0f;
        float levelValue = 0f;
        float officerValue = 0f;

        MutableCharacterStatsAPI stats = Global.getSector().getPlayerStats();
        CampaignFleetAPI fleet =  Global.getSector().getPlayerFleet();
        if (fleet==null) return 0;

        //fleet check
        for (FleetMemberAPI ship : fleet.getFleetData().getMembersListCopy()){
            float value = 0f;
            float wepValue = 0f;
            float wingValue = 0f;
            boolean unfit = false;
            float dMods = util.getDMods(ship.getVariant());
            ShipHullSpecAPI spec = ship.getHullSpec();
            //- for Dmods + for Smods
            value += spec.getBaseValue() * (1f - (dMods * 0.05f) * (1f + (ship.getVariant().getSMods().size()) * 0.05f));
            //weapons
            for (String wep : ship.getVariant().getNonBuiltInWeaponSlots()){
                WeaponSpecAPI weapon = ship.getVariant().getWeaponSpec(wep);
                if (weapon == null) continue;
                wepValue += weapon.getBaseValue();
            }
            //wings
            int x = -1;
            for (String s : ship.getVariant().getNonBuiltInWings()) {
                x++;
                FighterWingSpecAPI wing = ship.getVariant().getWing(x);
                if (wing==null) continue;
                wingValue += wing.getBaseValue();
            }
            //don't count logistics
            if (util.isLogistics(ship.getHullSpec().getHints())){
                value = 0f;
                //log("PowerLevel " + spec.getBaseHullId() +  " LOGISTICS");
            } else {
                //don't count unfit ships, and ships with basically no weapons since they almost always will be logi, or recovered ships with no combat value
                if (wepValue + wingValue < 750f && ship.getHullSpec().getHullSize() == ShipAPI.HullSize.FRIGATE) unfit = true;
                if (wepValue + wingValue < 1250f && ship.getHullSpec().getHullSize() == ShipAPI.HullSize.DESTROYER) unfit = true;
                if (wepValue + wingValue < 2000f && ship.getHullSpec().getHullSize() == ShipAPI.HullSize.CRUISER) unfit = true;
                if (wepValue + wingValue < 3000f && ship.getHullSpec().getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) unfit = true;
                if (unfit) {
                    value = 0f;
                    //log("PowerLevel " + spec.getBaseHullId() + " UNFIT OR LOGI, not counting, wep " + wepValue + " wing " + wingValue);
                }
            }

            totalValue += wingValue;
            totalValue += wepValue;
            totalValue += value;
            //log("PowerLevel " + spec.getBaseHullId() + " dmods " + dMods + " smods " + ship.getVariant().getSMods().size() + " weapons " + wepValue + " wings " + wingValue +" ship value " + value);
            //officers
            if (ship.getCaptain()!=Global.getSector().getPlayerPerson() && !ship.getCaptain().isAICore()) {
                officerValue += ship.getCaptain().getStats().getLevel();
            }
        }
        //value
        totalValue = Math.min(3000000f, totalValue);
        totalValue = mathUtil.normalize(totalValue, 0f, 3000000f);
        totalValue = mathUtil.lerp(0.20f, 1.0f, totalValue);
        power *= totalValue;
        //log("PowerLevel " + " totalValue " + totalValue);
        //level
        levelValue = mathUtil.normalize(stats.getLevel(), 1f, 15f);
        levelValue = mathUtil.lerp(0.25f, 1.0f, levelValue);
        power *= levelValue;
        //log("PowerLevel " + " levelValue " + levelValue);
        //officers
        officerValue = Math.min(50f, officerValue);
        officerValue = mathUtil.normalize(officerValue, 0f, 50f);
        officerValue = mathUtil.lerp(0.60f, 1.0f, officerValue);
        power *= officerValue;
        //log("PowerLevel " + " officerValue " + officerValue);

        power = mathUtil.lerp(0f, maxPower, power);
        power += base;

        log("power " + power);
        if (power<minPower){
            log("yee yee ass fleet bruh, you weak asf. NOT SPAWNING ANYTHING");
            power = 0;
        }
        return power;
    }
}	
