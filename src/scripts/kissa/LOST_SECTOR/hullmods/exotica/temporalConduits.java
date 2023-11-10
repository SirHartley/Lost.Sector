package scripts.kissa.LOST_SECTOR.hullmods.exotica;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.upgrades.Upgrade;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.Color;

public class temporalConduits extends Upgrade {

    public static final float TIME_FLOW_PER_LEVEL = 1.0f;
    public static final float DAMAGE_TAKEN_PER_LEVEL = 2.5f;

    public static final String ICON_PATH = "graphics/icons/upgrades/temporalConduits.png";
    public static final Color COLOR = new Color(232, 45, 73, 255);
    public static final String ID = "temporalConduitsUpgrade";

    private final IntervalUtil afterImageTimer;
    public temporalConduits(@NotNull String key, @NotNull JSONObject settings) {
        super(key, settings);
        this.afterImageTimer = new IntervalUtil(0.15f, 0.15f);
    }

    @Override
    public void applyUpgradeToStats(MutableShipStatsAPI stats, FleetMemberAPI fm, ShipModifications mods, int level) {
        stats.getTimeMult().modifyPercent(ID, TIME_FLOW_PER_LEVEL * level);
        if (level>2) {
            stats.getShieldDamageTakenMult().modifyPercent(ID, DAMAGE_TAKEN_PER_LEVEL * (level-2));
            stats.getArmorDamageTakenMult().modifyPercent(ID, DAMAGE_TAKEN_PER_LEVEL * (level-2));
            stats.getHullDamageTakenMult().modifyPercent(ID, DAMAGE_TAKEN_PER_LEVEL * (level-2));
        }
    }

    @Override
    public void advanceInCombatAlways(ShipAPI ship, FleetMemberAPI member, ShipModifications mods){

        if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
            return;
        }
        int level = mods.getUpgrade(getKey());

        afterImageTimer.advance(Global.getCombatEngine().getElapsedInLastFrame());
        if (afterImageTimer.intervalElapsed()) {
            Color color = util.setAlpha(COLOR, 6*level);
            ship.addAfterimage(color, 0.0f, 0.0f, ship.getVelocity().x * -0.8f, ship.getVelocity().y * -0.8f, 0.0f, 0.0f, 0.5f, 0.3f, true, true, false);
        }

    }

    @Override
    public void showDescriptionInShop(TooltipMakerAPI tooltip, FleetMemberAPI member, ShipModifications mods) {
        float pad = 10.0f;

        Color tc = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color y = Misc.getHighlightColor();
        Color bad = util.TT_ORANGE;

        tooltip.addPara("\"Long term effects of persistent temporal acceleration are largely unknown... Although it seems stable enough for practical implementation, despite past incidents.\" - Dr. Lange - Temporal physics, wing #17", pad, g,
                " - Dr. Lange - Temporal physics, wing #17");

    }

    @Override
    public TooltipMakerAPI modifyToolTip(TooltipMakerAPI tooltip, MutableShipStatsAPI stats, FleetMemberAPI member, ShipModifications mods, boolean expand){

        TooltipMakerAPI imageText = tooltip.beginImageWithText(ICON_PATH, 64f);
        int level = mods.getUpgrade(getKey());
        imageText.addPara("Temporal Conduits ("+level+")", 0f, COLOR, ""+level);
        if (expand) {

        }
        tooltip.addImageWithText(5f);

        return imageText;
    }

    @Override
    public void showStatsInShop(TooltipMakerAPI tooltip, FleetMemberAPI member, ShipModifications mods) {
        float pad = 10.0f;

        Color tc = Misc.getHighlightColor();
        Color y = Misc.getHighlightColor();
        Color bad = util.TT_ORANGE;
        Color bonus = util.BON_GREEN;
        Color red = util.EXOTICA_RED;

        int level = mods.getUpgrade(getKey());
        float currentTime = level * TIME_FLOW_PER_LEVEL;
        float currentDam = Math.max ((level-2) * DAMAGE_TAKEN_PER_LEVEL, 0f);
        String plusTime = "";
        if (level>0) plusTime = "+";
        String plusDam = "";
        if (level>2) plusDam = "+";

        LabelAPI labelTime = tooltip.addPara("Increases timeflow: "+plusTime+(int)currentTime+"%"+" (+"+(int)TIME_FLOW_PER_LEVEL+"%"+"/lvl, max: "+(int)(TIME_FLOW_PER_LEVEL*10f)+"%"+")", 2.0f);
        labelTime.setHighlight(plusTime+(int)currentTime+"%", "+"+(int)TIME_FLOW_PER_LEVEL+"%", (int)(TIME_FLOW_PER_LEVEL*10f)+"%");
        labelTime.setHighlightColors(y, y, bonus);
        tooltip.addPara("", 0.0f, y, "");
        tooltip.addPara("Starting at level 3:", 2.0f, bad, "level 3");
        LabelAPI labelDam = tooltip.addPara("Increases all damage taken: "+plusDam+currentDam+"%"+" (+"+DAMAGE_TAKEN_PER_LEVEL+"%"+"/lvl, max: "+(int)(DAMAGE_TAKEN_PER_LEVEL*8f)+"%"+")", 2.0f);
        labelDam.setHighlight(plusDam+currentDam+"%", "+"+DAMAGE_TAKEN_PER_LEVEL+"%", (int)(DAMAGE_TAKEN_PER_LEVEL*8f)+"%");
        labelDam.setHighlightColors(bad, bad, red);

    }

}
