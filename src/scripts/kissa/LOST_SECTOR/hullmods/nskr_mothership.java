package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.Map;

public class nskr_mothership extends BaseHullMod {

    //

    public static final float AMMO_BONUS = 100f;
    public static float RATE_DECREASE_MODIFIER = 33.33f;
    public static float RATE_INCREASE_MODIFIER = 50f;

    public static final float CR_BONUS = 15f;
    public static final float CR_PENALTY = 15f;

    //10 per day
    public static final float GAMMA_CORE_FUEL = 1200f;
    public static final float BETA_CORE_FUEL = 3600f;

    public static final String FUEL_BASE_KEY = "nskr_mothershipFuel";

    static void log(final String message) {
        Global.getLogger(nskr_mothership.class).info(message);
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getMissileAmmoBonus().modifyPercent(id, AMMO_BONUS);
        stats.getBallisticAmmoBonus().modifyPercent(id, AMMO_BONUS);
        stats.getEnergyAmmoBonus().modifyPercent(id, AMMO_BONUS);

        stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, 1f - RATE_DECREASE_MODIFIER / 100f);
        stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyPercent(id, RATE_INCREASE_MODIFIER);

    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

    }

    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {

        MutableShipStatsAPI stats = member.getStats();
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();

        String id = FUEL_BASE_KEY + member.getId();
        float fuel = getFuel(FUEL_BASE_KEY + member.getId()) / 10f;
        boolean player = isInPlayerFleet(member.getStats());
        if (player) {
            if (!member.isMothballed()) {
                if (fuel > 0f) {
                    unApplyPenaltyStats(FUEL_BASE_KEY + "out_of_fuel", stats);
                    //cap count for subroutine
                    if (getCapitalCount(pf) <= 1) {
                        applySubroutineStats(FUEL_BASE_KEY, pf, stats);
                    } else {
                        unApplySubroutineStats(FUEL_BASE_KEY, pf, stats);
                    }

                } else {
                    applyPenaltyStats(FUEL_BASE_KEY + "out_of_fuel", stats);

                }
            }
        } else {
            if (member.getFleetData()==null) return;
            if (member.getFleetData().getFleet()==null) return;
            applySubroutineStats(FUEL_BASE_KEY, member.getFleetData().getFleet(), stats);
        }

        /////PAUSE CHECK
        if (Global.getSector().isPaused()) return;

        if (player && !member.isMothballed()) {
            //consume fuel
            if (fuel > 0f) {
                setFuel(id, getFuel(id) - amount);
            } else {
                consumeFuel(member, id);
            }
        }

    }

    private void applySubroutineStats(String id, CampaignFleetAPI fleet, MutableShipStatsAPI stats) {
        stats.getMaxCombatReadiness().modifyFlat(id, CR_BONUS * 0.01f, "Mothership Subroutine bonus");

        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
            if (m.getVariant()==null) continue;
            if (m.getVariant().hasHullMod("nskr_mothership_frigate")) continue;
            if (!m.isFrigate()) continue;

            m.getVariant().addMod("nskr_mothership_frigate");
        }
    }

    private void unApplySubroutineStats(String id, CampaignFleetAPI fleet, MutableShipStatsAPI stats) {
        stats.getMaxCombatReadiness().unmodify(id);

        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
            if (m.getVariant()==null) continue;
            if (m.getVariant().hasHullMod("nskr_mothership_frigate")){
                m.getVariant().removeMod("nskr_mothership_frigate");
            }
        }
    }

    private void applyPenaltyStats(String id, MutableShipStatsAPI stats) {
        stats.getMaxCombatReadiness().modifyFlat(id, -CR_PENALTY * 0.01f, "Mothership out of AI Cores");

    }

    private void unApplyPenaltyStats(String id, MutableShipStatsAPI stats) {
        stats.getMaxCombatReadiness().unmodify(id);

    }

    private void consumeFuel(FleetMemberAPI member, String id) {

        float amount = 0f;
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        if (cargo.getCommodityQuantity("gamma_core")>0f){
            cargo.removeCommodity("gamma_core", 1f);

            amount = GAMMA_CORE_FUEL;

            Global.getSector().getCampaignUI().addMessage(member.getShipName()+" "+member.getHullSpec().getHullName()+"-Class consumed 1 Gamma Core",
                    Global.getSettings().getColor("standardTextColor"),
                    "1 Gamma Core",
                    "",
                    Global.getSettings().getColor("yellowTextColor"),
                    Global.getSettings().getColor("yellowTextColor"));

            util.playUiStaticNoise();
        } else if (cargo.getCommodityQuantity("beta_core")>0f) {
            cargo.removeCommodity("beta_core", 1f);

            amount = BETA_CORE_FUEL;

            Global.getSector().getCampaignUI().addMessage(member.getShipName()+" "+member.getHullSpec().getHullName()+"-Class consumed 1 Beta Core",
                    Global.getSettings().getColor("standardTextColor"),
                    "1 Beta Core",
                    "",
                    Global.getSettings().getColor("yellowTextColor"),
                    Global.getSettings().getColor("yellowTextColor"));

            util.playUiStaticNoise();
        }

        setFuel(id, amount);
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {

        float pad = 12.0f;
        Color y = Misc.getHighlightColor();
        Color bad = util.TT_ORANGE;

        tooltip.addSectionHeading("Details", Alignment.MID, pad);

        tooltip.addPara("-All weapon ammo increased by "+(int)(AMMO_BONUS)+"%"+"%", pad, y, (int)(AMMO_BONUS)+"%");
        tooltip.addPara("-Fighter replacement rate drain reduced by "+(int)(RATE_DECREASE_MODIFIER)+"%"+"%", 2.0f, y, (int)(RATE_DECREASE_MODIFIER)+"%");
        tooltip.addPara("-Fighter replacement rate recovery increased by "+(int)(RATE_INCREASE_MODIFIER)+"%"+"%", 2.0f, y, (int)(RATE_INCREASE_MODIFIER)+"%");

        tooltip.addSectionHeading("Subroutines", Alignment.MID, pad);

        tooltip.addPara("If this ship is the only non-civilian Capital-class vessel in the fleet, certain autonomous subroutines kick in increasing the ships and its fleets performance.", pad, bad, "only");
        tooltip.addPara("-Maximum combat readiness of the ship and all frigates in the fleet increased by "+(int)(CR_BONUS)+"%"+"%", pad, y, (int)(CR_BONUS)+"%");
        tooltip.addPara("-Deployment cost of all frigates in the fleet reduced by "+"1", 2.0f, y, "1");

        //CAMPAIGN
        if (Global.getSector()==null) return;
        if (Global.getSector().getPlayerFleet()==null) return;

        if (getCapitalCount(Global.getSector().getPlayerFleet())<=1){
            tooltip.addPara("-Subroutine status ACTIVE", pad, y, "ACTIVE");
        } else {
            tooltip.addPara("-Subroutine status INACTIVE", pad, bad, "INACTIVE");
        }

        float fuel = getFuel(FUEL_BASE_KEY + ship.getFleetMember().getId())/10f;

        tooltip.addSectionHeading("AI Cores", Alignment.MID, pad);

        tooltip.addPara("This ship requires a steady supply of lower level AI Cores to remain at peak performance. Maximum combat readiness is decreased by "+(int)(CR_PENALTY)+"%"+"%"+
                " and subroutines are deactivated otherwise.", pad, bad, (int)(CR_PENALTY)+"%");
        tooltip.addPara("Gamma cores function for "+(int) (GAMMA_CORE_FUEL/10f)+" days, while beta cores function for "+(int) (BETA_CORE_FUEL/10f)+" days. Gamma core are consumed before Beta cores, this happens automatically.", pad, bad, "");
        if (fuel>0f) {
            tooltip.addPara("-Current time remaining until next AI Core is consumed, " + (int) (fuel) + " Days", pad, bad, (int) (fuel) + " Days");
        } else {
            tooltip.addPara("CURRENTLY OUT OF AI CORES", 2.0f, bad, "CURRENTLY OUT OF AI CORES");
        }

    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) AMMO_BONUS + "%";
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return true;
    }

    @Override
    public boolean affectsOPCosts() {
        return false;
    }

    private int getCapitalCount(CampaignFleetAPI fleet) {
        int capitalCount = 0;
        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()){
            if (m.isCivilian()) continue;
            if (m.isCapital()){
                capitalCount++;
            }
        }
        return capitalCount;
    }

    public static float getFuel(String id) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, GAMMA_CORE_FUEL);

        return (float)data.get(id);
    }

    public static void setFuel(String id, float fuel) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(id, fuel);
    }
}

