package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import scripts.kissa.LOST_SECTOR.campaign.customStart.abilities.thronesGiftHolySpiritListener;

public class nskr_holySpirit extends BaseHullMod {

    static void log(final String message) {
        Global.getLogger(nskr_holySpirit.class).info(message);
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

        ship.addListener(new thronesGiftHolySpiritListener(ship));

    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

    }

    private float timer = 0f;

    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {

        timer += amount;
        //advance only runs for a few frames when paused??? so just add a second to always run. this is just to make this work in refit
        if (Global.getSector().isPaused()) timer += 1f;

        if (timer > 1f) {
            timer = 0f;

            if (member == null) return;
            if (member.getFleetData() == null) {
                return;
            }
            if (member.getFleetData().getFleet() == null) {
                return;
            }
            if (member.getFleetData().getFleet() == Global.getSector().getPlayerFleet()) {
                remove(member);
            }
        }
    }

    private void remove(FleetMemberAPI member) {
        if (member.getVariant() != null) {
            member.getVariant().removeMod("nskr_holySpirit");
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
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

}
