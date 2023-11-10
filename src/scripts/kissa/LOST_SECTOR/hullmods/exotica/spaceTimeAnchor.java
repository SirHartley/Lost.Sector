package scripts.kissa.LOST_SECTOR.hullmods.exotica;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.upgrades.Upgrade;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.Color;

public class spaceTimeAnchor extends Upgrade {

    public static final float DAMAGE_THRESHOLD = 500.0f;
    public static final float REDUCTION_PER_LEVEL = 100.0f;
    public static final float MASS_PER_LEVEL = 20.0f;
    public static final float AGILITY_PER_LEVEL = 3.75f;

    public static final String ICON_PATH = "graphics/icons/upgrades/spaceTimeAnchor.png";
    public static final Color COLOR = new Color(53, 19, 175, 255);
    public static final String ID = "spaceTimeAnchorUpgrade";

    public spaceTimeAnchor(@NotNull String key, @NotNull JSONObject settings) {
        super(key, settings);
    }

    @Override
    public void applyUpgradeToStats(MutableShipStatsAPI stats, FleetMemberAPI fm, ShipModifications mods, int level) {

        if (level > 2) {
            stats.getAcceleration().modifyPercent(ID, AGILITY_PER_LEVEL * (level - 2));
            stats.getDeceleration().modifyPercent(ID, AGILITY_PER_LEVEL * (level - 2));
            stats.getTurnAcceleration().modifyPercent(ID, AGILITY_PER_LEVEL * (level - 2));
            stats.getMaxTurnRate().modifyPercent(ID, AGILITY_PER_LEVEL * (level - 2));
        }
    }

    @Override
    public void applyToShip(FleetMemberAPI member, ShipAPI ship, ShipModifications mods){

        int level = mods.getUpgrade(getKey());

        //update the listener
        if (!ship.hasListenerOfClass(spaceTimeAnchorListener.class)) {
            ship.addListener(new spaceTimeAnchorListener(ship, level));
        } else {
            for (spaceTimeAnchorListener listener : ship.getListeners(spaceTimeAnchorListener.class)) {
                ship.removeListener(listener);
            }
            ship.addListener(new spaceTimeAnchorListener(ship, level));
        }

    }

    @Override
    public void showDescriptionInShop(TooltipMakerAPI tooltip, FleetMemberAPI member, ShipModifications mods) {
        float pad = 10.0f;

        Color tc = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color y = Misc.getHighlightColor();
        Color bad = util.TT_ORANGE;

        tooltip.addPara("\"Wild rumours that the recently installed device on board is a - black hole, are only in part truthful. " +
                        "I'd like to remind all crew onboard that such hearsay is unprofessional, and will not be tolerated in the future.\" - Officer Talef - onboard the DSRD Verge", pad, g,
                " - Officer Talef - onboard the DSRD Verge");

    }

    @Override
    public TooltipMakerAPI modifyToolTip(TooltipMakerAPI tooltip, MutableShipStatsAPI stats, FleetMemberAPI member, ShipModifications mods, boolean expand) {

        TooltipMakerAPI imageText = tooltip.beginImageWithText(ICON_PATH, 64f);
        int level = mods.getUpgrade(getKey());
        imageText.addPara("Space Time Anchor (" + level + ")", 0f, COLOR, "" + level);
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
        float currentDam = level * REDUCTION_PER_LEVEL;
        float currentMass = level * MASS_PER_LEVEL;
        float currentAgil = Math.max((level - 2) * AGILITY_PER_LEVEL, 0f);
        String minusBonus = "";
        if (level > 0) minusBonus = "-";
        String plusBonus = "";
        if (level > 0) plusBonus = "+";
        String minusPenalty = "";
        if (level > 2) minusPenalty = "-";

        LabelAPI labelDam = tooltip.addPara("Reduces a portion of armor/hull damage taken above 500: " + minusBonus + (int) currentDam+ " (-" + (int) REDUCTION_PER_LEVEL + "/lvl, max: " + (int) (REDUCTION_PER_LEVEL * 10f)+ ")", 2.0f);
        labelDam.setHighlight(minusBonus + (int) currentDam+" ", "-" + (int) REDUCTION_PER_LEVEL + "", (int) (REDUCTION_PER_LEVEL * 10f) + "");
        labelDam.setHighlightColors(y, y, bonus);
        LabelAPI labelMass = tooltip.addPara("Increases mass: " + plusBonus + (int) currentMass + "%" + " (+" + (int) MASS_PER_LEVEL + "%" + "/lvl, max: " + (int) (MASS_PER_LEVEL * 10f) + "%" + ")", 2.0f);
        labelMass.setHighlight(plusBonus + (int) currentMass + "%", "+" + (int) MASS_PER_LEVEL + "%", (int) (MASS_PER_LEVEL * 10f) + "%");
        labelMass.setHighlightColors(y, y, bonus);
        tooltip.addPara("", 0.0f, y, "");
        tooltip.addPara("Starting at level 3:", 2.0f, bad, "level 3");
        LabelAPI labelAgil = tooltip.addPara("Reduces maneuverability: " + minusPenalty + currentAgil + "%" + " (-" + AGILITY_PER_LEVEL + "%" + "/lvl, max: " + (int) (AGILITY_PER_LEVEL * 8f) + "%" + ")", 2.0f);
        labelAgil.setHighlight(minusPenalty + currentAgil + "%", "-" + AGILITY_PER_LEVEL + "%", (int) (AGILITY_PER_LEVEL * 8f) + "%");
        labelAgil.setHighlightColors(bad, bad, red);

    }
}

