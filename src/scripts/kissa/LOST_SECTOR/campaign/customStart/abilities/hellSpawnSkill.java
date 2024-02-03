package scripts.kissa.LOST_SECTOR.campaign.customStart.abilities;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import scripts.kissa.LOST_SECTOR.campaign.customStart.hellSpawnManager;

public class hellSpawnSkill {

    public static class Level1 implements ShipSkillEffect {
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {

            //dp bonus
            stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyPercent(id, -hellSpawnManager.FLAGSHIP_DP_DISCOUNT);
            stats.getSuppliesToRecover().modifyPercent(id, -hellSpawnManager.FLAGSHIP_DP_DISCOUNT);

            if (hullSize==null) return;
            //hullsize bonus
            switch (hullSize){
                case CAPITAL_SHIP:
                    stats.getMaxSpeed().modifyFlat(id, hellSpawnManager.FLAGSHIP_CAP_BONUS);
                    break;
                case CRUISER:
                    stats.getAcceleration().modifyPercent(id, hellSpawnManager.FLAGSHIP_CRUISER_BONUS);
                    stats.getDeceleration().modifyPercent(id, hellSpawnManager.FLAGSHIP_CRUISER_BONUS);
                    stats.getTurnAcceleration().modifyPercent(id, hellSpawnManager.FLAGSHIP_CRUISER_BONUS);
                    stats.getMaxTurnRate().modifyPercent(id, hellSpawnManager.FLAGSHIP_CRUISER_BONUS);
                    break;
                case DESTROYER:
                    stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -hellSpawnManager.FLAGSHIP_DD_BONUS);
                    stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -hellSpawnManager.FLAGSHIP_DD_BONUS);
                    break;
                case FRIGATE:
                    stats.getBallisticWeaponRangeBonus().modifyPercent(id, hellSpawnManager.FLAGSHIP_FRIG_BONUS);
                    stats.getEnergyWeaponRangeBonus().modifyPercent(id, hellSpawnManager.FLAGSHIP_FRIG_BONUS);
                    break;
            }
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            //no removing
        }

        public String getEffectDescription(float level) {
            return "Gives bonus stats for the flagship based on hull size";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }
}
