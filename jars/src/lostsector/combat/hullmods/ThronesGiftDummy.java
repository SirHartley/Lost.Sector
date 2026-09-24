package lostsector.combat.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import lostsector.campaign.starts.thronesgift.ThronesGiftHolySpiritListener;

public class ThronesGiftDummy extends BaseHullMod {

    static void log(final String message) {
        Global.getLogger(ThronesGiftDummy.class).info(message);
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

        ship.addListener(new ThronesGiftHolySpiritListener(ship));

    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

    }

    private float timer = 0f;

    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {

        timer += amount;
        // advance() runs only for a few frames while paused, so add a second to keep it running in the refit screen.
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
