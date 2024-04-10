package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.util.MagicIncompatibleHullmods;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class nskr_volatile extends BaseHullMod {

    public static final float ROF_BONUS = 40f;
    public static final float FLUX_BONUS = 67f;
    public final Map<HullSize, Float> SPEED_BONUS = new HashMap<>();
    {
        SPEED_BONUS.put(HullSize.FRIGATE, 50f);
        SPEED_BONUS.put(HullSize.DESTROYER, 40f);
        SPEED_BONUS.put(HullSize.CRUISER, 30f);
        SPEED_BONUS.put(HullSize.CAPITAL_SHIP, 20f);
    }
    public static final float MAX_RANGE = 800f;
    public static final float RANGE_PENALTY = 0.5f;
    public static final float FIGHTER_RANGE_PENALTY = 0.25f;
    public static final float SMOD_PENALTY = 33f;
    public static final  Color afterImageColor = new Color(100, 42, 201, 80);
    public static final  Color engineColor = new Color(100, 42, 201, 255);

    public static final String MOD_ICON = "graphics/icons/hullsys/high_energy_focus.png";
    public static final String MOD_BUFFID = "nskr_volatile";
    public static final String MOD_NAME = "Volatile Flux Injector";

    private final IntervalUtil afterImageTimer;
    public nskr_volatile() {
        this.afterImageTimer = new IntervalUtil(0.01f, 0.01f);
    }

    public static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static {
        // These hullmods will automatically be removed
        BLOCKED_HULLMODS.add(HullMods.SAFETYOVERRIDES);
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship.getHullSpec().isPhase())return false;
        return true;
    }
    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (ship.getHullSpec().isPhase())return "Can not be installed on phase ships.";
        return null;
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

        //int capCount = ship.getVariant().getNumFluxCapacitors();
        //ship.getMutableStats().getFluxCapacity().modifyPercent(id, FLUX_BONUS);
        //ship.getMutableStats().getFluxCapacity().modifyFlat(id, capCount*(200f*(FLUX_BONUS/100f)));

        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                //if someone tries to install blocked hullmod, remove it
                MagicIncompatibleHullmods.removeHullmodWithWarning(
                        ship.getVariant(),
                        tmp,
                        "nskr_volatile"
                );
            }
        }
        //smod
        //boolean sMod = isSMod(ship.getMutableStats());
        //if (sMod) ship.getMutableStats().getEmpDamageTakenMult().modifyPercent(id, SMOD_PENALTY);
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        int capCount = stats.getVariant().getNumFluxCapacitors();
        stats.getFluxCapacity().modifyPercent(id, FLUX_BONUS);
        stats.getFluxCapacity().modifyFlat(id, capCount*(200f*(FLUX_BONUS/100f)));

        stats.getWeaponRangeThreshold().modifyFlat(id, MAX_RANGE);
        stats.getWeaponRangeMultPastThreshold().modifyMult(id, RANGE_PENALTY);

        stats.getFighterWingRange().modifyMult(id, FIGHTER_RANGE_PENALTY);

        //smod
        boolean sMod = isSMod(stats);
        if (sMod) stats.getEmpDamageTakenMult().modifyPercent(id, SMOD_PENALTY);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
            return;
        }
        String id = "nskr_volatile_"+ship.getId();
        MutableShipStatsAPI stats = ship.getMutableStats();
        float fluxRatio = ship.getFluxTracker().getFluxLevel();
        if(ship.getFluxTracker().isOverloaded() || ship.getFluxTracker().isVenting()) fluxRatio = 1f;
        float ratio = mathUtil.lerp(1f, -1f, fluxRatio);

        float mod = SPEED_BONUS.get(ship.getHullSize());

        float spdBonus = mod*ratio;
        if (ship.getVariant().hasHullMod("nskr_augmented") && ratio<0) spdBonus = (mod*ratio)/2f;
        float agilityBonus = Math.max(mod*ratio, 0f);
        float gunBonus = ROF_BONUS*ratio;
        if (ship.getVariant().hasHullMod("nskr_augmented") && ratio<0) gunBonus = (ROF_BONUS*ratio)/2f;

        stats.getMaxSpeed().modifyFlat(id, spdBonus);

        stats.getTurnAcceleration().modifyPercent(id, agilityBonus);
        stats.getAcceleration().modifyPercent(id, agilityBonus);

        stats.getBallisticRoFMult().modifyPercent(id, gunBonus);
        stats.getEnergyRoFMult().modifyPercent(id, gunBonus);
        stats.getMissileRoFMult().modifyPercent(id, gunBonus);

        String plus = "";
        if(ratio>0) plus = "+";

        if (ship == Global.getCombatEngine().getPlayerShip()) {
            Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, "Top speed "+plus+(int)spdBonus+" Rate of fire "+plus+(int)gunBonus+"%", false);
        }

        //FX
        if (ratio<0)return;

        this.afterImageTimer.advance(Global.getCombatEngine().getElapsedInLastFrame());
        if (this.afterImageTimer.intervalElapsed()) {
            Color color = util.shiftAlpha(afterImageColor, ratio);
            ship.addAfterimage(color, 0.0f, 0.0f, ship.getVelocity().x * -0.8f, ship.getVelocity().y * -0.8f, 0.0f, 0.0f, 0.0f, 0.3f, true, true, false);
        }
        ship.getEngineController().fadeToOtherColor(this, engineColor, null, 1f, 0.5f*ratio);

    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        Color g = Misc.getGrayColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color badbg = Misc.setAlpha(Misc.getNegativeHighlightColor(), 90);
        Color gr = Misc.getStoryBrightColor();
        Color grg = Misc.getStoryOptionColor();

        boolean sMod = false;
        if (ship!=null){
            sMod = isSMod(ship.getMutableStats());
        }
        float pad = 10.0f;
        tooltip.addSectionHeading("Stats", Alignment.MID, pad);
        if (ship!=null) {
            float mod = SPEED_BONUS.get(ship.getHullSize());
            tooltip.addPara("+"+(int)mod+"su/s"+" to top speed.", pad, util.BON_GREEN, (int)mod+"su/s");
        } else{
            float frig = SPEED_BONUS.get(HullSize.FRIGATE);
            float cap = SPEED_BONUS.get(HullSize.CAPITAL_SHIP);
            tooltip.addPara("+"+(int)frig+"-"+(int)cap+"su/s"+" to top speed, based on hull size.", pad, util.BON_GREEN, (int)frig+"-"+(int)cap+"su/s");
        }
        tooltip.addPara("+"+(int)ROF_BONUS+"%%"+" to all weapon rate of fire.", 0.0f, util.BON_GREEN, (int)ROF_BONUS+"%");
        tooltip.addPara("-Full bonus to speed and rate fo fire at zero flux, down to zero at 50%% flux, full penalty at 100%% flux.", 0.0f, util.NICE_YELLOW, "");
        if(ship!=null) {
            if (ship.getVariant().hasHullMod("nskr_augmented")) {
                tooltip.addPara("-Penalty is equal up to 50%% of the bonus, but as a negative.", 0.0f, util.TT_ORANGE, "Penalty is equal up to 50% of the bonus");
            } else {
                tooltip.addPara("-Penalty is equal up to the bonus, but as a negative.", 0.0f, util.TT_ORANGE, "Penalty is equal up to the bonus");
            }
        }
        tooltip.addPara("+"+(int)FLUX_BONUS+"%%"+" to flux capacity and the effectiveness of additional capacitors.", 0.0f, util.BON_GREEN, (int)FLUX_BONUS+"%");
        tooltip.addPara("-Weapon range past "+(int)MAX_RANGE+" units is reduced by "+(int)50f+"%%" + " at all times.", 0.0f, util.TT_ORANGE, (int)50f+"%");
        tooltip.addPara("-Wing engagement range is reduced by "+(int)75f+"%%" + " at all times.", 0.0f, util.TT_ORANGE, (int)75f+"%");

        tooltip.addSectionHeading("Additional Info", Alignment.MID, pad);
        tooltip.addPara("-Also grants an acceleration buff while on low flux.", pad, util.NICE_YELLOW, "");
        tooltip.addPara("-Active venting or being overloaded counts as being at full flux.", 0.0f, util.NICE_YELLOW, "");
        tooltip.addPara("-Incompatible with phase ships.", 0.0f, util.TT_ORANGE, "");

        if (Global.getCurrentState() == GameState.CAMPAIGN){
            tooltip.addSectionHeading("S-mod penalty", bad, badbg, Alignment.MID, pad);
            tooltip.addPara("Increases EMP damage taken by "+(int)SMOD_PENALTY+"%%.", pad, util.NICE_YELLOW, (int)SMOD_PENALTY+"%");
            if (!sMod) tooltip.addPara("This effect only applies if this hullmod is built into the hull using a story point. Cheap hullmods have stronger effects.", pad, grg, "story point");
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "";
        return null;
    }

    @Override
    public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) return "";
        return null;
    }

    @Override
    public boolean isSModEffectAPenalty() {
        return true;
    }
}