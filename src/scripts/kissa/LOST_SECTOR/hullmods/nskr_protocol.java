//////////////////////
//Initially created by theDragn and modified from HTE
//////////////////////
package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Objects;

public class nskr_protocol extends BaseHullMod {

    //effectively the adaptive protocol system script

    public static final String CHARGE_MODE_NAME = "Relocation Protocol";
    public static final float CHARGE_SPEED_FLAT = 75f;
    public static final float CHARGE_ACCEL_BONUS = 200f;
    public static final float CHARGE_PENALTY_MULT = 60f;
    public static final Color CHARGE_ENGINE_COLOR = new Color(255, 33, 65, 255);
    public static final Color CHARGE_SMOKE_COLOR = new Color(137, 33, 255, 255);

    public static final String PRECISION_MODE_NAME = "Precision Protocol";
    public static final float PRECISION_ROF_MULT = 0.25f;
    public static final float PRECISION_DMG_BOOST = 0.5f;
    public static final float PRECISION_BEAM_DMG = 0.25f;
    public static final float PRECISION_RANGE_FLAT = 150f;
    public static final Color PRECISION_WEAPON_GLOW_COLOR = new Color(255, 51, 33, 255);

    public static final String BULWARK_MODE_NAME = "Safeguard Protocol";
    public static final float BULWARK_DEFENSES_DAMAGE_REDUCTION = 0.33f;
    public static final float BULWARK_SHIELD_SPEED_BONUS = 100f;
    public static final float BULWARK_RANGE_PD_FLAT = 150f;
    public static final float BULWARK_PD_DMG_BONUS = 75f;
    public static final Color BULWARK_SHIELD_CORE_COLOR = new Color(0, 255, 225, 100);

    public static final String id = "nskr_protocol";
    public static final String NSKR_AS_ICON = "graphics/icons/hullsys/targeting_feed.png";
    public static final String NSKR_AS_BUFF_ID = "nskr_as1";
    public static int storedHashCode = 0;
    public static final Vector2f ZERO = new Vector2f();

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine().isPaused() || ship.isHulk()) {
            return;
        }

        ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("TRACKER_DATA_KEY" + ship.getId());
        if (data == null) {
            data = new ShipSpecificData();
        }

        if (Global.getCombatEngine().hashCode()!=storedHashCode) {
            ship.getSystem().setAmmo(1);
            storedHashCode = Global.getCombatEngine().hashCode();
        }

        ShipSystemAPI system = ship.getSystem();
        MutableShipStatsAPI stats = ship.getMutableStats();
        if (system == null || stats == null) {
            return;
        }
        AdaptiveMode currentMode = this.getMode(system);
        if (currentMode == null) {
            return;
        }
        //i know what a switch is I SWEAR
        if (ship.getFluxTracker().isOverloaded() || ship.getFluxTracker().isVenting()){
            data.speedEffectLevel -= amount / 2.0f;
            data.weaponEffectLevel -= amount / 2.0f;
            data.defenseEffectLevel -= amount / 2.0f;
        } else {
            switch (currentMode) {
                case SPEED: {
                    data.speedEffectLevel += amount / 2.0f;
                    data.weaponEffectLevel -= amount / 2.0f;
                    data.defenseEffectLevel -= amount / 2.0f;
                    break;
                }
                case WEAPONS: {
                    data.speedEffectLevel -= amount / 2.0f;
                    data.weaponEffectLevel += amount / 2.0f;
                    data.defenseEffectLevel -= amount / 2.0f;
                    break;
                }
                case DEFENSES: {
                    data.speedEffectLevel -= amount / 2.0f;
                    data.weaponEffectLevel -= amount / 2.0f;
                    data.defenseEffectLevel += amount / 2.0f;
                }
            }
        }

        if (data.speedEffectLevel > 1.0f) {
            data.speedEffectLevel = 1.0f;
        }
        if (data.speedEffectLevel < 0.0f) {
            data.speedEffectLevel = 0.0f;
        }
        if (data.weaponEffectLevel > 1.0f) {
            data.weaponEffectLevel = 1.0f;
        }
        if (data.weaponEffectLevel < 0.0f) {
            data.weaponEffectLevel = 0.0f;
        }
        if (data.defenseEffectLevel > 1.0f) {
            data.defenseEffectLevel = 1.0f;
        }
        if (data.defenseEffectLevel < 0.0f) {
            data.defenseEffectLevel = 0.0f;
        }

        //Global.getCombatEngine().addFloatingText(ship.getLocation(),
        //        data.speedEffectLevel +","+ data.weaponEffectLevel +","+ data.defenseEffectLevel,
        //        40f, Color.cyan, ship, 0.5f, 1.0f);

        if (data.speedEffectLevel > 0.0f) {
            //speed only when moving forwards
            //vector time
            Vector2f sVel = ship.getVelocity();
            if (sVel == null) sVel = ZERO;
            float sAngle = ship.getFacing();
            float vAngle = VectorUtils.getFacing(sVel);
            float dist = Objects.requireNonNull(sVel).length();
            if (dist == 0f) vAngle = sAngle;
            //angle fuckery
            float diff = vAngle - sAngle;
            if (diff < 0) diff *= -1f;
            //engine.addFloatingText(sLoc, "test " + (int)vAngle +","+ (int)sAngle +","+ (int)diff, 30f, Color.cyan, ship, 0.5f, 1.0f);
            if ((sVel.length() > 0f) && (diff <= 30) || sVel.length() > 0f && (diff >= 330)) {
                ship.getMutableStats().getMaxSpeed().modifyFlat(id, CHARGE_SPEED_FLAT * data.speedEffectLevel);
            } else {
                ship.getMutableStats().getMaxSpeed().unmodify(id);
            }

            stats.getAcceleration().modifyPercent(id, CHARGE_ACCEL_BONUS * data.speedEffectLevel + 100f);

            stats.getDeceleration().modifyPercent(id, -CHARGE_PENALTY_MULT * data.speedEffectLevel);
            stats.getTurnAcceleration().modifyPercent(id, -CHARGE_PENALTY_MULT * data.speedEffectLevel);
            stats.getMaxTurnRate().modifyPercent(id, -CHARGE_PENALTY_MULT * data.speedEffectLevel);

        } else {
            stats.getMaxSpeed().unmodify(id);
            stats.getAcceleration().unmodify(id);

            stats.getDeceleration().unmodify(id);
            stats.getTurnAcceleration().unmodify(id);
            stats.getMaxTurnRate().unmodify(id);
        }

        ship.getEngineController().fadeToOtherColor(this, CHARGE_ENGINE_COLOR, CHARGE_SMOKE_COLOR, data.speedEffectLevel, 0.25f * data.speedEffectLevel);
        ship.getEngineController().extendFlame(this, 1.2f * data.speedEffectLevel, data.speedEffectLevel, data.speedEffectLevel);

        if (data.weaponEffectLevel > 0.0f) {
            stats.getBeamWeaponDamageMult().modifyMult(id, (1f-((PRECISION_DMG_BOOST * data.weaponEffectLevel) / (1.0f + (PRECISION_DMG_BOOST * data.weaponEffectLevel)))) * (1f + (PRECISION_BEAM_DMG * data.weaponEffectLevel)));
            stats.getEnergyRoFMult().modifyMult(id, -PRECISION_ROF_MULT * data.weaponEffectLevel + 1.0f);
            stats.getBallisticRoFMult().modifyMult(id, -PRECISION_ROF_MULT * data.weaponEffectLevel + 1.0f);
            stats.getBallisticWeaponDamageMult().modifyMult(id, 1.0f + (PRECISION_DMG_BOOST * data.weaponEffectLevel));
            stats.getEnergyWeaponDamageMult().modifyMult(id, 1.0f + (PRECISION_DMG_BOOST * data.weaponEffectLevel));
            stats.getBallisticWeaponRangeBonus().modifyFlat(id, PRECISION_RANGE_FLAT * data.weaponEffectLevel);
            stats.getEnergyWeaponRangeBonus().modifyFlat(id, PRECISION_RANGE_FLAT * data.weaponEffectLevel);
            stats.getBeamPDWeaponRangeBonus().modifyFlat(id, -PRECISION_RANGE_FLAT * data.weaponEffectLevel);
            stats.getNonBeamPDWeaponRangeBonus().modifyFlat(id, -PRECISION_RANGE_FLAT * data.weaponEffectLevel);

        } else {
            stats.getEnergyWeaponFluxCostMod().unmodify(id);
            stats.getEnergyRoFMult().unmodify(id);
            stats.getBallisticWeaponFluxCostMod().unmodify(id);
            stats.getBallisticRoFMult().unmodify(id);
            stats.getBallisticWeaponDamageMult().unmodify(id);
            stats.getEnergyWeaponDamageMult().unmodify(id);
            stats.getBeamWeaponDamageMult().unmodify(id);
            stats.getBallisticWeaponRangeBonus().unmodify(id);
            stats.getEnergyWeaponRangeBonus().unmodify(id);
            stats.getBeamPDWeaponRangeBonus().unmodify(id);
            stats.getNonBeamPDWeaponRangeBonus().unmodify(id);
        }

        ship.setWeaponGlow(data.weaponEffectLevel, PRECISION_WEAPON_GLOW_COLOR, EnumSet.of(WeaponAPI.WeaponType.ENERGY, WeaponAPI.WeaponType.BALLISTIC));
        ship.setJitterShields(false);

        if (data.defenseEffectLevel > 0.0f) {
            stats.getShieldAbsorptionMult().modifyMult(id, 1.0f - BULWARK_DEFENSES_DAMAGE_REDUCTION * data.defenseEffectLevel);
            stats.getShieldTurnRateMult().modifyPercent(id, BULWARK_SHIELD_SPEED_BONUS * data.defenseEffectLevel + 1.0f);
            stats.getShieldUnfoldRateMult().modifyPercent(id, BULWARK_SHIELD_SPEED_BONUS * data.defenseEffectLevel + 1.0f);
            stats.getNonBeamPDWeaponRangeBonus().modifyFlat(id, BULWARK_RANGE_PD_FLAT * data.defenseEffectLevel);
            stats.getBeamPDWeaponRangeBonus().modifyFlat(id, BULWARK_RANGE_PD_FLAT * data.defenseEffectLevel);
            stats.getDamageToFighters().modifyPercent(id, BULWARK_PD_DMG_BONUS * data.defenseEffectLevel);
            stats.getDamageToMissiles().modifyPercent(id, BULWARK_PD_DMG_BONUS * data.defenseEffectLevel);

        } else {
            stats.getShieldAbsorptionMult().unmodify(id);
            stats.getShieldTurnRateMult().unmodify(id);
            stats.getShieldUnfoldRateMult().unmodify(id);
            stats.getNonBeamPDWeaponRangeBonus().unmodify(id);
            stats.getBeamPDWeaponRangeBonus().unmodify(id);
            stats.getDamageToFighters().unmodify(id);
            stats.getDamageToMissiles().unmodify(id);

        }

        ship.setJitterUnder(ship, BULWARK_SHIELD_CORE_COLOR, 0.9f*data.defenseEffectLevel, Math.round(20*data.defenseEffectLevel), 1f, 5f*data.defenseEffectLevel);

        if (ship == Global.getCombatEngine().getPlayerShip()) {
            String modeName = this.getModeName(currentMode);
            String buffText = "";
            switch (currentMode) {
                case SPEED: {
                    buffText = "top speed and acceleration increased";
                    break;
                }
                case WEAPONS: {
                    buffText = "weapon power increased";
                    break;
                }
                case DEFENSES: {
                    buffText = "defenses reinforced and PD improved";
                }
            }
            Global.getCombatEngine().maintainStatusForPlayerShip(NSKR_AS_BUFF_ID, NSKR_AS_ICON, modeName, buffText, false);
        }
        Global.getCombatEngine().getCustomData().put("TRACKER_DATA_KEY" + ship.getId(), data);
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        tooltip.addSectionHeading("Details", Alignment.MID, pad);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/burn_drive.png", 36.0f);
        text.addPara(CHARGE_MODE_NAME, 0.0f, util.NICE_YELLOW, CHARGE_MODE_NAME);
        text.addPara("+75su/s max speed when moving forwards.", 0.0f, util.BON_GREEN, "75su/s");
        text.addPara("+200%% acceleration.", 0.0f, util.BON_GREEN, "200%");
        text.addPara("-60%% maneuverability otherwise.", 0.0f, util.TT_ORANGE, "60%");
        tooltip.addImageWithText(pad);

        text = tooltip.beginImageWithText("graphics/icons/hullsys/quantum_disruptor.png", 36.0f);
        text.addPara(PRECISION_MODE_NAME, 0.0f, util.NICE_YELLOW, PRECISION_MODE_NAME);
        text.addPara("+50%% damage for non-missile projectile weapons.", 0.0f, util.BON_GREEN, "50%");
        text.addPara("+25%% damage for beam weapons.", 0.0f, util.BON_GREEN, "25%");
        text.addPara("+150 units non-PD weapon range.", 0.0f, util.BON_GREEN, "150");
        text.addPara("-25%% rate of fire for non-missile weapons.", 0.0f, util.TT_ORANGE, "25%");
        tooltip.addImageWithText(pad);

        text = tooltip.beginImageWithText("graphics/icons/hullsys/fortress_shield.png", 36.0f);
        text.addPara(BULWARK_MODE_NAME, 0.0f, util.NICE_YELLOW, BULWARK_MODE_NAME);
        text.addPara("-33%% shield damage taken.", 0.0f, util.BON_GREEN, "33%");
        text.addPara("+100%% shield rotation and deployment rate.", 0.0f, util.BON_GREEN, "100%");
        text.addPara("+150 units PD weapon range.", 0.0f, util.BON_GREEN, "150");
        text.addPara("+75%% damage to missiles and fighters.", 0.0f, util.BON_GREEN, "75%");
        tooltip.addImageWithText(pad);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) {
            return "2 seconds";
        }
        return null;
    }

    private String getModeName(AdaptiveMode mode) {
        switch (mode) {
            case SPEED: {
                return CHARGE_MODE_NAME;
            }
            case WEAPONS: {
                return PRECISION_MODE_NAME;
            }
            case DEFENSES: {
                return BULWARK_MODE_NAME;
            }
        }
        return "damn bitch, you fucked up";
    }

    private AdaptiveMode getMode(ShipSystemAPI system) {
        int i = system.getAmmo();
        switch (i) {
            case 0: {
                return AdaptiveMode.DEFENSES;
            }
            case 1: {
                return AdaptiveMode.SPEED;
            }
            case 2: {
                return AdaptiveMode.WEAPONS;
            }
            case 3: {
                return AdaptiveMode.DEFENSES;
            }
        }
        return null;
    }

    private static class ShipSpecificData {
        public float speedEffectLevel = 0.0f;
        public float weaponEffectLevel = 0.0f;
        public float defenseEffectLevel = 0.0f;

    }

    private enum AdaptiveMode {
        SPEED,
        WEAPONS,
        DEFENSES
    }
}