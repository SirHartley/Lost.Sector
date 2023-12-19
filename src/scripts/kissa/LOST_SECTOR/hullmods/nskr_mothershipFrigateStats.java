package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class nskr_mothershipFrigateStats extends BaseHullMod {

    static void log(final String message) {
        Global.getLogger(nskr_mothershipFrigateStats.class).info(message);
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getMaxCombatReadiness().modifyFlat(id, nskr_mothership.CR_BONUS * 0.01f, "Mothership Subroutine bonus");

        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(nskr_mothership.FUEL_BASE_KEY, -1f);
        stats.getSuppliesToRecover().modifyFlat(nskr_mothership.FUEL_BASE_KEY, -1f);

    }

    private float timer = 0f;
    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {

        timer += amount;
        //advance only runs for a few frames when paused??? so just add a second to always run. this is just to make this work in refit
        if (Global.getSector().isPaused()) timer += 1f;
        //unapply from the ship if mothership is no longer there

        if (timer>1f) {
            if (member == null) return;
            if (member.getFleetData() == null) return;
            if (member.getFleetData().getFleet() == null) return;
            if (!inMothershipFleet(member.getFleetData().getFleet())) {

                if (member.getVariant() != null) {

                    member.getVariant().removeMod("nskr_mothership_frigate");
                }
            }

            timer = 0f;
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {


    }

    private boolean inMothershipFleet(CampaignFleetAPI fleet) {

        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
            if (m.getHullSpec() == null) continue;
            if (m.getHullSpec().getBaseHullId() == null) continue;
            if (m.getHullSpec().getBaseHullId().equals("nskr_sunburst")) {
                //unapply if mothballed
                return !m.isMothballed();
            }
        }

        return false;
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
