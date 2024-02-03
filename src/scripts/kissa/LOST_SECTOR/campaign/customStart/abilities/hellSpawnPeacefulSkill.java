package scripts.kissa.LOST_SECTOR.campaign.customStart.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.customStart.hellSpawnManager;

import java.awt.*;

public class hellSpawnPeacefulSkill {

    public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect, AfterShipCreationSkillEffect {

        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            ship.addListener(new hellSpawnPeacefulSkillListener(ship));
        }

        @Override
        public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
            ship.removeListenerOfClass(hellSpawnPeacefulSkillListener.class);
        }

        @Override
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
        }

        @Override
        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        }

        @Override
        public boolean hasCustomDescription() {
            return true;
        }

        @Override
        public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
        }

        @Override
        public ScopeDescription getScopeDescription() {
            return ScopeDescription.ALL_COMBAT_SHIPS;
        }

    }

    //dummy skill so I can show the fucking tooltip
    //THIS IS SO DUMB ALEX WHHYYYYY
    public static class Level2 extends BaseSkillEffectDescription implements ShipSkillEffect {

        @Override
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
        }

        @Override
        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        }

        @Override
        public boolean hasCustomDescription() {
            return true;
        }

        @Override
        public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
            init(stats, skill);

            float opad = 10f;
            Color h = Misc.getHighlightColor();
            Color r = Misc.getNegativeHighlightColor();
            Color g = Misc.getGrayColor();
            Color tc = Misc.getTextColor();

            info.addPara("-"+(int)hellSpawnManager.PEACEFUL_BONUS+"%% damage taken when fighting unlawful or AI factions", opad, h, h, "");
            info.addPara("Lost if the Descent progresses too far", opad, tc, r, "Lost");
        }

        @Override
        public ScopeDescription getScopeDescription() {
            return ScopeDescription.ALL_COMBAT_SHIPS;
        }

    }

    public static class hellSpawnPeacefulSkillListener implements DamageTakenModifier, AdvanceableListener {

        public ShipAPI ship;
        private boolean validFight = false;
        private boolean checked = false;

        public hellSpawnPeacefulSkillListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public void advance(float amount) {
            if (!ship.isAlive()) {
                ship.removeListener(this);
                return;
            }
            //safety
            if (Global.getSector()!=null) {
                if (!Global.getSector().getPlayerStats().hasSkill("hellSpawnPeacefulSkill")) {
                    ship.removeListener(this);
                    return;
                }
            }

            //faction check
            if (ship.getFleetMember()==null) return;
            if (ship.getFleetMember().getFleetData()==null) return;
            if (ship.getFleetMember().getFleetData().getFleet()==null) return;
            if (ship.getFleetMember().getFleetData().getFleet().getBattle()==null) return;
            if (!checked) {
                BattleAPI battle = ship.getFleetMember().getFleetData().getFleet().getBattle();
                for (CampaignFleetAPI fleet : battle.getBothSides()) {
                    if (battle.isOnPlayerSide(fleet)) continue;
                    if (fleet.getFaction()==null) continue;
                    if (hellSpawnManager.lawfulFactions.isEmpty()) continue;
                    if (hellSpawnManager.lawfulFactions.contains(fleet.getFaction().getId())) continue;
                    validFight = true;
                    break;
                }
                checked = true;
            }
            if (!validFight){
                ship.removeListener(this);
                return;
            }


        }

        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (!validFight) return null;

            float bonus = hellSpawnManager.PEACEFUL_BONUS/100f;
            if (!damage.isDps()) {
                damage.setDamage(damage.getDamage() * (1f-bonus));
            } else {
                damage.setDamage((damage.getDamage()/10f) * (1f-bonus));
            }
            return null;
        }
    }

}
