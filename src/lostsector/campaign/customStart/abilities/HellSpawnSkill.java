package lostsector.campaign.customStart.abilities;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import lostsector.campaign.customStart.HellSpawnManager;

public class HellSpawnSkill {

    public static class Level1 implements ShipSkillEffect {
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {

            //dp bonus
            stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyPercent(id, -HellSpawnManager.FLAGSHIP_DP_DISCOUNT);
            stats.getSuppliesToRecover().modifyPercent(id, -HellSpawnManager.FLAGSHIP_DP_DISCOUNT);

            if (hullSize==null) return;
            //hullsize bonus
            switch (hullSize){
                case CAPITAL_SHIP:
                    stats.getMaxSpeed().modifyFlat(id, HellSpawnManager.FLAGSHIP_CAP_BONUS);
                    break;
                case CRUISER:
                    stats.getAcceleration().modifyPercent(id, HellSpawnManager.FLAGSHIP_CRUISER_BONUS);
                    stats.getDeceleration().modifyPercent(id, HellSpawnManager.FLAGSHIP_CRUISER_BONUS);
                    stats.getTurnAcceleration().modifyPercent(id, HellSpawnManager.FLAGSHIP_CRUISER_BONUS);
                    stats.getMaxTurnRate().modifyPercent(id, HellSpawnManager.FLAGSHIP_CRUISER_BONUS);
                    break;
                case DESTROYER:
                    stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -HellSpawnManager.FLAGSHIP_DD_BONUS);
                    stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -HellSpawnManager.FLAGSHIP_DD_BONUS);
                    break;
                case FRIGATE:
                    stats.getBallisticWeaponRangeBonus().modifyPercent(id, HellSpawnManager.FLAGSHIP_FRIG_BONUS);
                    stats.getEnergyWeaponRangeBonus().modifyPercent(id, HellSpawnManager.FLAGSHIP_FRIG_BONUS);
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
