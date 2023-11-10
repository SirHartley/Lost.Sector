package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.EnumSet;

public class nskr_absorption extends BaseHullMod {
    //
    //actually the system script
    //
    public static final float DMG_TAKEN = -90f;
    public static final float SHIELD_UNFOLD = 500f;
    public static final float SHIELD_SIZE = 360f;
    public static final float BONUS_ROF_MULT = 50f;
    public static final float SYS_TIME = 5f;
    public static final float ENERGY_PER_SECOND = 100f;
    public static final float MIN_ENERGY = 500f;
    public static final Color GLOW_COLOR = new Color(255, 143, 15);
    public static final Color SHIELD_COLOR = new Color(255, 71, 15);
    public static final String MOD_ICON = "graphics/icons/hullsys/ammo_feeder.png";
    public static final String MOD_BUFFID = "nskr_absorption";
    public static final String MOD_NAME = "ABSORB System";

    public static final Vector2f ZERO = new Vector2f();
    private Color baseColor=null;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        String id = "ABSORPTION_"+ship.getId();
        boolean player = false;
        player = ship == Global.getCombatEngine().getPlayerShip();
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        if (ship.getShield()==null){
            return;
        }
        if (baseColor==null) baseColor = ship.getShield().getInnerColor();
        ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("ABSORPTION_DATA_KEY" + ship.getId());
        if (data == null) {
            data = new ShipSpecificData();
            ship.addListener(new absorptionCounter());
        }
        MutableShipStatsAPI stats = ship.getMutableStats();

        float rof = 0f;
        float resist = 0f;
        if (data.timer>0f){

            float timerNorm = mathUtil.normalize(data.timer,0f, SYS_TIME);
            float bonus = mathUtil.lerp(0f, 1f, timerNorm);
            resist = DMG_TAKEN*bonus;
            float unfold = SHIELD_UNFOLD*bonus;

            int shield = (int)ship.getMutableStats().getShieldArcBonus().computeEffective(ship.getHullSpec().getShieldSpec().getArc());
            float sizeBonus = Math.round(shield + (SHIELD_SIZE * (bonus)));
            sizeBonus = Math.min(360f, sizeBonus);
            ship.getShield().setArc((int)sizeBonus);

            stats.getShieldAbsorptionMult().modifyPercent(id, resist);
            stats.getShieldUnfoldRateMult().modifyPercent(id, unfold);
            
            ship.getShield().setInnerColor(util.blendColors(baseColor, SHIELD_COLOR, bonus));

            data.resist = Math.abs(resist);

            data.timer -= amount;
            if (player){
                float gain = mathUtil.lerp(1f, 0f, timerNorm);
                Global.getSoundPlayer().applyLowPassFilter(1f, 0.40f*gain);
            }
        } else {
            stats.getShieldAbsorptionMult().unmodify(id);
            stats.getShieldUnfoldRateMult().unmodify(id);
            stats.getShieldArcBonus().unmodify(id);
        }

        if (data.absorbed>0f){
            float absorbed = Math.min(data.absorbed, MIN_ENERGY);
            float mult = mathUtil.normalize(absorbed,0f, MIN_ENERGY);

            rof = BONUS_ROF_MULT*mult;
            float flux = (100f-rof/(1f+(rof/100f)))/100f;
            //engine.addFloatingText(ship.getLocation(), ""+flux, 32, Color.RED, ship, 1f,1f);

            stats.getBallisticRoFMult().modifyPercent(id, rof);
            stats.getEnergyRoFMult().modifyPercent(id, rof);
            stats.getBallisticWeaponFluxCostMod().modifyMult(id, flux);
            stats.getEnergyWeaponFluxCostMod().modifyMult(id, flux);

            EnumSet<WeaponAPI.WeaponType> weaponTypes = EnumSet.of(WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.ENERGY);
            ship.setWeaponGlow(1f*mult, GLOW_COLOR, weaponTypes);

            data.absorbed -= amount*ENERGY_PER_SECOND;
        } else {
            stats.getBallisticRoFMult().unmodify(id);
            stats.getEnergyRoFMult().unmodify(id);
            stats.getBallisticWeaponFluxCostMod().unmodify(id);
            stats.getEnergyWeaponFluxCostMod().unmodify(id);
        }

        if (rof>0f || resist<0f) {
            if (player) {
                Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, "shield damage taken " + (int) resist + "%" + ", weapon rate of fire " + (int) rof + "%", true);
            }
        }

        Global.getCombatEngine().getCustomData().put("ABSORPTION_DATA_KEY" + ship.getId(), data);
    }

    public static class ShipSpecificData {
        public float timer=0f;
        public float absorbed =0f;
        public float resist=0f;
    }


    public static class absorptionCounter implements DamageTakenModifier {
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            CombatEngineAPI engine = Global.getCombatEngine();
            if (engine == null) {
                return null;
            }
            if (engine.isPaused()) {
                return null;
            }
            if (target == null) return null;
            if (!shieldHit) return null;
            ShipAPI ship = (ShipAPI)target;

            ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("ABSORPTION_DATA_KEY" + ship.getId());

            float amount = 0f;
            amount = engine.getElapsedInLastFrame();
            if (data.timer>0f) {
                float dmg = 0f;
                float rdmg = 0f;
                dmg = damage.computeDamageDealt(amount);
                if (damage.getType()==DamageType.FRAGMENTATION) dmg *= 0.25f;
                if (damage.getType()==DamageType.HIGH_EXPLOSIVE) dmg *= 0.50f;
                if (damage.getType()==DamageType.KINETIC) dmg *= 2.00f;
                rdmg = dmg/(100f/(100f-data.resist));
                rdmg -= dmg;
                rdmg = Math.abs(rdmg);
                rdmg *= (ship.getShield().getFluxPerPointOfDamage()*ship.getMutableStats().getShieldDamageTakenMult().getModifiedValue())*(100f/(100f-data.resist));
                data.absorbed += rdmg;

                //engine.addFloatingText(ship.getLocation(), "lol " + data.absorbed + " " + rdmg, 32f, Color.RED, ship, 0.5f, 1.0f);
            }
            Global.getCombatEngine().getCustomData().put("ABSORPTION_DATA_KEY" + ship.getId(), data);
            return null;
        }
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;

        tooltip.addSectionHeading("Details", Alignment.MID, pad);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/fortress_shield.png", 36.0f);
        text.addPara("activating the system greatly boosts shield performance, with the bonus decaying over "+(int)SYS_TIME+" seconds.", 0.0f, util.NICE_YELLOW, (int)SYS_TIME+"");
        text.addPara("-"+(int)Math.abs(DMG_TAKEN)+"%"+"% shield damage taken.", 0.0f, util.BON_GREEN, (int)Math.abs(DMG_TAKEN)+"%");
        text.addPara("+"+(int)SHIELD_SIZE+" shield arc and instant unfolding.", 0.0f, util.BON_GREEN, (int)SHIELD_SIZE+"");
        tooltip.addImageWithText(pad);
        text.addPara("",pad);
        TooltipMakerAPI text2 = tooltip.beginImageWithText("graphics/icons/hullsys/ammo_feeder.png", 36.0f);
        text2.addPara("damage that is blocked by the system is absorbed as energy that decays over time at "+(int)ENERGY_PER_SECOND+" units per second.", 0.0f, util.NICE_YELLOW, (int)ENERGY_PER_SECOND+"");
        text2.addPara("+"+(int)BONUS_ROF_MULT+"%"+"% non missile weapon rate of fire while having more than "+(int)MIN_ENERGY+" energy stored. having less than that scales the bonus down to zero.", 0.0f, util.BON_GREEN, (int)BONUS_ROF_MULT+"%");
        tooltip.addImageWithText(pad);


    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }
}