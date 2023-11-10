package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.*;

public class nskr_ultracaliber extends BaseHullMod {
    //
    //data collection and FX for system
    //
    public static final float ROF_MULT = 20f;
    public static final float DAMAGE_MULT = 25f;
    public static final float MAX_RANGE = 900f;
    public static final float WEAPON_HEALTH = 100f;
    public static final float RANGE_PENALTY = 0.6667f;
    public static final float SYS_ROF_MULT = 50f;
    public static final float SYS_TIME = 5f;
    public static final Color GLOW_COLOR = new Color(255, 143, 15);
    public static final String MOD_ICON = "graphics/icons/hullsys/ammo_feeder.png";
    public static final String MOD_BUFFID = "nskr_ultracaliber";
    public static final String MOD_NAME = "Ultracaliber";

    public static final Vector2f ZERO = new Vector2f();
    static void log(final String message) {
        Global.getLogger(nskr_ultracaliber.class).info(message);
    }
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().modifyPercent(id, -ROF_MULT);
        stats.getBallisticWeaponDamageMult().modifyPercent(id, DAMAGE_MULT);
        stats.getEnergyRoFMult().modifyPercent(id, -ROF_MULT);
        stats.getEnergyWeaponDamageMult().modifyPercent(id, DAMAGE_MULT);
        //beams cringe
        stats.getBeamWeaponDamageMult().modifyMult(id, (100f-DAMAGE_MULT/(1f+(DAMAGE_MULT/100f)))/100f);

        stats.getWeaponHealthBonus().modifyPercent(id, WEAPON_HEALTH);
        stats.getWeaponRangeThreshold().modifyFlat(id, MAX_RANGE);
        stats.getWeaponRangeMultPastThreshold().modifyMult(id, RANGE_PENALTY);
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        //log("Ship "+ship.getHullSpec().getHullName());
        //for (WeaponAPI w: ship.getAllWeapons()) {
        //    log("H "+w.getMaxHealth() + " N "+w.getDisplayName());
        //}
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        String id = "ULTRACALIBER_"+ship.getId();
        boolean player = false;
        player = ship == Global.getCombatEngine().getPlayerShip();
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("CALIBER_DATA_KEY" + ship.getId());
        if (data == null) {
            data = new ShipSpecificData();
        }

        if (data.timer>0f){

            float timerNorm = mathUtil.normalize(data.timer,0f, SYS_TIME);
            float bonus = mathUtil.lerp(0f, SYS_ROF_MULT, timerNorm);

            //engine.addFloatingText(ship.getLocation(), ""+timerNorm, 24f, Color.RED, ship, 1f,1f);

            ship.getMutableStats().getBallisticRoFMult().modifyPercent(id, bonus);
            ship.getMutableStats().getEnergyRoFMult().modifyPercent(id, bonus);

            EnumSet<WeaponAPI.WeaponType> weaponTypes = EnumSet.of(WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.ENERGY);
            ship.setWeaponGlow(1f*timerNorm, GLOW_COLOR, weaponTypes);

            data.timer -= amount;
            if (player){
                Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, "weapon rate of fire +"+(int)bonus+"%", true);
            }
        } else {
            ship.getMutableStats().getBallisticRoFMult().unmodify(id);
            ship.getMutableStats().getEnergyRoFMult().unmodify(id);
        }

        Global.getCombatEngine().getCustomData().put("CALIBER_DATA_KEY" + ship.getId(), data);
    }

    public static class ShipSpecificData {
            public float timer=0f;
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;

        tooltip.addSectionHeading("Details", Alignment.MID, pad);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/ammo_feeder.png", 36.0f);
        text.addPara("+"+(int)DAMAGE_MULT+"%"+"% projectile weapon damage.", 0.0f, util.BON_GREEN, (int)DAMAGE_MULT+"%");
        text.addPara("+"+(int)WEAPON_HEALTH+"%"+"% to durability of all weapons.", 0.0f, util.BON_GREEN, (int)WEAPON_HEALTH+"%");
        text.addPara("+"+(int)SYS_ROF_MULT+"%"+"% weapon rate of fire decaying over five seconds after system use.", 0.0f, util.BON_GREEN, (int)SYS_ROF_MULT+"%");
        //text.addPara("", pad);
        text.addPara("-"+(int)ROF_MULT+"%"+"% weapon rate of fire.", 0.0f, util.TT_ORANGE, (int)ROF_MULT+"%");
        text.addPara("-weapon range past "+(int)MAX_RANGE+" units is reduced by one third." , 0.0f, util.TT_ORANGE, (int)MAX_RANGE+"");
        tooltip.addImageWithText(pad);

    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }
}