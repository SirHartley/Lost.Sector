package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.Automated;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.customStart.hellSpawnManager;

public class nskr_hellSpawnStats extends BaseHullMod {

    static void log(final String message) {
        Global.getLogger(nskr_hellSpawnStats.class).info(message);
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

        int level = hellSpawnManager.getLevel();
        if (level>0 && !stats.getVariant().hasHullMod(HullMods.AUTOMATED)) {
            stats.getMaxCombatReadiness().modifyFlat(id, -hellSpawnManager.getCrReduction()/100f, "Inhuman");
        }
        if (level>=2){
            stats.getMissileWeaponDamageMult().modifyMult(id, 1f+hellSpawnManager.DAMAGE_BONUS/100f);
            stats.getBallisticWeaponDamageMult().modifyMult(id, 1f+hellSpawnManager.DAMAGE_BONUS/100f);
            stats.getEnergyWeaponDamageMult().modifyMult(id, 1f+hellSpawnManager.DAMAGE_BONUS/100f);

            stats.getShieldDamageTakenMult().modifyMult(id, 1f+hellSpawnManager.DAMAGE_TAKEN/100f);
            stats.getArmorDamageTakenMult().modifyMult(id, 1f+hellSpawnManager.DAMAGE_TAKEN/100f);
            stats.getHullDamageTakenMult().modifyMult(id, 1f+hellSpawnManager.DAMAGE_TAKEN/100f);
        }
        if (level>=3){
            if (Misc.isAutomated(stats) && !Automated.isAutomatedNoPenalty(stats)) {
                float base = (stats.getMaxCombatReadiness().getModifiedValue()*100f) + 70f;
                if (base < hellSpawnManager.AUTOMATED_BASE_CR) {
                    float crBonus;
                    if (base>0f){
                        crBonus = hellSpawnManager.AUTOMATED_BASE_CR-base;
                    } else {
                        crBonus = Math.abs(base)+hellSpawnManager.AUTOMATED_BASE_CR;
                    }
                    stats.getMaxCombatReadiness().modifyFlat(id, crBonus * 0.01f, "Hellspawn");
                }
            }
        }

    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        FleetMemberAPI member = ship.getFleetMember();
        MutableShipStatsAPI stats = ship.getMutableStats();
        int level = hellSpawnManager.getLevel();

        //AAL
        if (level>0){

            ship.addListener(new nskr_hellSpawnListener(ship, level));
        }


        if (member==null) return;
        //flagship
        if (member.isFlagship()){

        }
        FleetDataAPI data = member.getFleetData();
        if (data==null) return;
        CampaignFleetAPI fleet = data.getFleet();
        if (fleet==null) return;



    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        // level = hellSpawnManager.getLevel();
        //MutableShipStatsAPI stats = ship.getMutableStats();
        //String id = "nskr_hellSpawnStats";

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
            if (member.getFleetData().getFleet() != Global.getSector().getPlayerFleet()) {
                remove(member);
            }
        }
    }

    private void remove(FleetMemberAPI member) {
        if (member.getVariant() != null) {
            member.getVariant().removeMod("nskr_hellSpawnStats");
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