package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.renderUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.EnumSet;

public class nskr_inertial extends BaseHullMod {

    //just plain ole hullmod

    public static final float BONUS_DAMAGE_MULT = 0.08f;
    public static final float SMOD_PENALTY = 50f;
    public static final float SMOD_BONUS = 10f;
    public static final Color GLOW_COLOR = new Color(255, 100, 33);
    public static final String MOD_ICON = "graphics/icons/hullsys/temporal_shell.png";
    public static final String MOD_BUFFID = "nskr_inertial";
    public static final String MOD_NAME = "Inertial supercharger";
    private final IntervalUtil colorInterval = new IntervalUtil(0.10f, 0.15f);

    public static final Vector2f ZERO = new Vector2f();

    static void log(final String message) {
        Global.getLogger(nskr_inertial.class).info(message);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

        //smod
        boolean sMod = isSMod(ship.getMutableStats());
        if (sMod){
            if (ship.getHullSize()== ShipAPI.HullSize.FRIGATE) {
                ship.getMutableStats().getWeaponDamageTakenMult().modifyPercent(id, SMOD_PENALTY);
            }
            if (ship.getHullSize()== ShipAPI.HullSize.CRUISER || ship.getHullSize()== ShipAPI.HullSize.CAPITAL_SHIP) {
                ship.getMutableStats().getProjectileSpeedMult().modifyPercent(id, SMOD_BONUS);
            }
        }
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        String id = "INERTIAL_"+ship.getId();
        boolean player = false;
        player = ship == Global.getCombatEngine().getPlayerShip();
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }

        ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("INERTIAL_DATA_KEY" + ship.getId());
        if (data == null) {
            data = new ShipSpecificData();
        }
        MutableShipStatsAPI stats = ship.getMutableStats();

        data.velocity = ship.getVelocity().length();

        if (data.velocity>0f){
            float dmg = data.velocity*BONUS_DAMAGE_MULT;

            stats.getBallisticWeaponDamageMult().modifyPercent(id, dmg);
            stats.getEnergyWeaponDamageMult().modifyPercent(id, dmg);
            if(ship.getVariant().hasHullMod("nskr_augmented")) stats.getProjectileSpeedMult().modifyPercent(id, dmg);
            //no beams
            stats.getBeamWeaponDamageMult().modifyMult(id, (100f-dmg/(1f+(dmg/100f)))/100f);

            //fx
            float max = ship.getMaxSpeed();
            float vel = Math.min(data.velocity, max);
            float dmgNormalized = mathUtil.normalize(vel, 0f , max);
            colorInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());

            for (WeaponAPI wep : ship.getAllWeapons()){
                WeaponSpecAPI spec = wep.getSpec();
                if (spec==null) continue;
                if (spec.getType() == WeaponAPI.WeaponType.BALLISTIC || spec.getType() == WeaponAPI.WeaponType.ENERGY){
                    if (spec.isBeam()) continue;

                    Color glow = util.setAlpha(GLOW_COLOR, (int)(180*dmgNormalized));
                    if (colorInterval.intervalElapsed()) {
                        glow = util.randomiseColor(glow, (int)(20*dmgNormalized), (int)(20*dmgNormalized), (int)(50*dmgNormalized), (int)(50*dmgNormalized), true);
                    }

                    //GLOW
                    //fucking cursed as fuck, null checking isn't enough?? don't ask me how to detect if weap has no sprite then
                    SpriteAPI wSprite = null;
                    try {
                        wSprite = wep.getSprite();
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        continue;
                    }
                    if (wSprite!=null && wSprite.getHeight()>0f) {
                        renderUtil.renderGlow(wep.getLocation(), wSprite.getHeight() * 0.33f, glow);
                    }
                }
            }

            if (player) {
                String status = "weapon damage +" + (int)dmg + "%";
                if(ship.getVariant().hasHullMod("nskr_augmented")) status = "weapon damage and projectile velocity +" + (int)dmg + "%";
                Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, status, false);
            }
        } else {
            stats.getBallisticWeaponDamageMult().unmodify(id);
            stats.getEnergyWeaponDamageMult().unmodify(id);
            stats.getBeamWeaponDamageMult().unmodify(id);
        }

        Global.getCombatEngine().getCustomData().put("INERTIAL_DATA_KEY" + ship.getId(), data);
    }

    public static class ShipSpecificData {
        public float velocity=0f;
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10f;
        float opad = 10f;
        Color hl = Misc.getHighlightColor();
        Color tc = Misc.getTextColor();
        Color g = Misc.getGrayColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = Misc.setAlpha(Misc.getNegativeHighlightColor(), 90);
        Color gr = Misc.getStoryBrightColor();
        Color grg = Misc.getStoryOptionColor();
        Color grbg = Misc.getStoryDarkColor();

        boolean sMod = false;
        if (ship!=null){
            sMod = isSMod(ship.getMutableStats());
        }

        float bonus, bonusZeroFlux, zFlux, base;
        //otherwise will crash when hovering a modspec lol
        if (ship!=null) {
            zFlux = ship.getMutableStats().getZeroFluxSpeedBoost().getModifiedValue();
            base = ship.getMaxSpeed();
            bonus = (base) * BONUS_DAMAGE_MULT;
            bonusZeroFlux = (base + zFlux) * BONUS_DAMAGE_MULT;

            float col1W = 120;
            float colW = (int) ((width - col1W - 12f) / 3f);

            tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                    20f, true, true,
                    new Object [] {"Top speed", colW, "Bonus", colW, "Zero flux", colW, "Bonus", colW});

            tooltip.addRow(
                    Alignment.MID, g, (int) (base)+"su/s",
                    Alignment.MID, tc, "+" + (int) (bonus)+"%",
                    Alignment.MID, g, (int) (base+zFlux)+"su/s",
                    Alignment.MID, hl, "+" + (int) (bonusZeroFlux)+"%");

            tooltip.addTable("", 0, opad);

            tooltip.addPara("", 0.0f, util.NICE_YELLOW, "");
            tooltip.addPara("Note there is no cap to this bonus, this is simply for reference.", 0.0f, util.NICE_YELLOW, "Note");

            if (Global.getCurrentState() == GameState.CAMPAIGN){
                if (ship.getHullSize()== ShipAPI.HullSize.FRIGATE) {
                    tooltip.addSectionHeading("S-mod penalty", bad, badbg, Alignment.MID, pad);
                    tooltip.addPara("Increases the damage taken by weapons by " + (int) SMOD_PENALTY + "%%.", pad, util.NICE_YELLOW, (int) SMOD_PENALTY + "%");
                    if (!sMod)
                        tooltip.addPara("This effect only applies if this hullmod is built into the hull using a story point. Cheap hullmods have stronger effects.", pad, grg, "story point");
                }
                if (ship.getHullSize()== ShipAPI.HullSize.CRUISER || ship.getHullSize()== ShipAPI.HullSize.CAPITAL_SHIP) {
                    tooltip.addSectionHeading("S-mod bonus", grg, grbg, Alignment.MID, pad);
                    tooltip.addPara("Increases projectile velocity by " + (int) SMOD_BONUS + "%%.", pad, util.NICE_YELLOW, (int) SMOD_BONUS + "%");
                    if (!sMod)
                        tooltip.addPara("This effect only applies if this hullmod is built into the hull using a story point. Cheap hullmods have stronger effects.", pad, grg, "story point");
                }
            }
        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int)(BONUS_DAMAGE_MULT*100f) + "%";
        return null;
    }
}