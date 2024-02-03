package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.util.MagicIncompatibleHullmods;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class nskr_criticalArmor extends BaseHullMod {

    public final Map<ShipAPI.HullSize, Float> ARMOR_MAX = new HashMap<>();
    {
        ARMOR_MAX.put(ShipAPI.HullSize.FRIGATE, 400f);
        ARMOR_MAX.put(ShipAPI.HullSize.DESTROYER, 700f);
        ARMOR_MAX.put(ShipAPI.HullSize.CRUISER, 900f);
        ARMOR_MAX.put(ShipAPI.HullSize.CAPITAL_SHIP, 1200f);
    }
    public final Map<ShipAPI.HullSize, Float> ARMOR_MIN = new HashMap<>();
    {
        ARMOR_MIN.put(ShipAPI.HullSize.FRIGATE, 150f);
        ARMOR_MIN.put(ShipAPI.HullSize.DESTROYER, 300f);
        ARMOR_MIN.put(ShipAPI.HullSize.CRUISER, 400f);
        ARMOR_MIN.put(ShipAPI.HullSize.CAPITAL_SHIP, 500f);
    }
    public final Map<ShipAPI.HullSize, Float> BREAKPOINTS = new HashMap<>();
    {
        BREAKPOINTS.put(ShipAPI.HullSize.FRIGATE, 400f);
        BREAKPOINTS.put(ShipAPI.HullSize.DESTROYER, 700f);
        BREAKPOINTS.put(ShipAPI.HullSize.CRUISER, 900f);
        BREAKPOINTS.put(ShipAPI.HullSize.CAPITAL_SHIP, 1200f);
    }
    public static final float SMOD_PENALTY = 30f;

    public static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static {
        // These hullmods will automatically be removed
        BLOCKED_HULLMODS.add(HullMods.HEAVYARMOR);
        BLOCKED_HULLMODS.add("apex_armor");
        BLOCKED_HULLMODS.add("apex_cryo_armor");
        //BLOCKED_HULLMODS.add("eis_damperhull");
        BLOCKED_HULLMODS.add("tahlan_daemonarmor");
        BLOCKED_HULLMODS.add("tahlan_daemonplating");
        BLOCKED_HULLMODS.add("monjeau_armour");
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        float bonus;
        //scaling bonus, getting hull-spec before creation is not convoluted t:Alex
        bonus = getValue(hullSize, stats.getVariant().getHullSpec());
        stats.getArmorBonus().modifyFlat(id, bonus);

        //smod
        boolean sMod = isSMod(stats);
        if (sMod) {
            stats.getAcceleration().modifyMult(id, 1f - SMOD_PENALTY * 0.01f);
            stats.getDeceleration().modifyMult(id, 1f - SMOD_PENALTY * 0.01f);
            stats.getTurnAcceleration().modifyMult(id, 1f - SMOD_PENALTY * 0.01f);
            stats.getMaxTurnRate().modifyMult(id, 1f - SMOD_PENALTY * 0.01f);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {



        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                //if someone tries to install blocked hullmod, remove it
                MagicIncompatibleHullmods.removeHullmodWithWarning(
                        ship.getVariant(),
                        tmp,
                        "nskr_criticalArmor"
                );
            }
        }
    }

    private float getValue(ShipAPI.HullSize hullSize, ShipHullSpecAPI spec) {
        return mathUtil.lerp(ARMOR_MAX.get(hullSize), ARMOR_MIN.get(hullSize), mathUtil.normalize(spec.getArmorRating(), 0f, BREAKPOINTS.get(hullSize)));
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        //CombatEngineAPI engine = Global.getCombatEngine();
        //if (engine == null) {
        //    return;
        //}
        //if (engine.isPaused()) {
        //    return;
        //}
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship.getHullSpec().isPhase())return false;
        if (ship.getHullSpec().getArmorRating()>BREAKPOINTS.get(ship.getHullSize())) return false;

        return true;
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship.getHullSpec().isPhase())return "Can not be installed on phase ships.";
        if (ship.getHullSpec().getArmorRating()>BREAKPOINTS.get(ship.getHullSize())) return "Can not be installed on highly armored ships.";
        return null;
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10f;
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
        if (ship!=null && isApplicableToShip(ship)) {
            tooltip.addSectionHeading("Stats", Alignment.MID, pad);
            tooltip.addPara(
                    "-With a base armor of "+(int)ship.getHullSpec().getArmorRating()+" adds "+(int)getValue(ship.getHullSize(), ship.getHullSpec())+" bonus armor to this ship.", pad, util.NICE_YELLOW, (int)getValue(ship.getHullSize(), ship.getHullSpec())+"");
        }
        tooltip.addSectionHeading("Additional Info", Alignment.MID, pad);
        tooltip.addPara("-Incompatible with Heavy Armor.", pad, util.TT_ORANGE, "");
        tooltip.addPara("-Incompatible with phase ships.", 0.0f, util.TT_ORANGE, "");

        if (Global.getCurrentState() == GameState.CAMPAIGN){
            tooltip.addSectionHeading("S-mod penalty", bad, badbg, Alignment.MID, pad);
            tooltip.addPara("Reduces the ship's maneuverability by "+(int)SMOD_PENALTY+"%%.", pad, util.NICE_YELLOW, (int)SMOD_PENALTY+"%");
            if (!sMod) tooltip.addPara("This effect only applies if this hullmod is built into the hull using a story point. Cheap hullmods have stronger effects.", pad, grg, "story point");
        }
    }

        public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
            if (index == 0) return "" + ARMOR_MIN.get(ShipAPI.HullSize.FRIGATE).intValue() + "";
            if (index == 1) return "" + ARMOR_MIN.get(ShipAPI.HullSize.DESTROYER).intValue() + "";
            if (index == 2) return "" + ARMOR_MIN.get(ShipAPI.HullSize.CRUISER).intValue() + "";
            if (index == 3) return "" + ARMOR_MIN.get(ShipAPI.HullSize.CAPITAL_SHIP).intValue() + "";
            if (index == 4) return "" + ARMOR_MAX.get(ShipAPI.HullSize.FRIGATE).intValue() + "";
            if (index == 5) return "" + ARMOR_MAX.get(ShipAPI.HullSize.DESTROYER).intValue() + "";
            if (index == 6) return "" + ARMOR_MAX.get(ShipAPI.HullSize.CRUISER).intValue() + "";
            if (index == 7) return "" + ARMOR_MAX.get(ShipAPI.HullSize.CAPITAL_SHIP).intValue() + "";

        return null;
    }
}
