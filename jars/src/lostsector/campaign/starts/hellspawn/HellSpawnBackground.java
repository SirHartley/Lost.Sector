package lostsector.campaign.starts.hellspawn;

import lostsector.campaign.starts.GameModeManager;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.backgrounds.BaseCharacterBackground;
import exerelin.utilities.NexFactionConfig;
import lostsector.campaign.starts.hellspawn.HellSpawnEventIntel;
import lostsector.campaign.starts.hellspawn.HellSpawnIntel;
import lostsector.settings.Setting;
import lostsector.helper.Ids;

import java.awt.*;

public class HellSpawnBackground extends BaseCharacterBackground {

    @Override
    public boolean shouldShowInSelection(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        return isUnlocked();
    }

    @Override
    public boolean canBeSelected(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        return isUnlocked();
    }

    @Override
    public void canNotBeSelectedReason(TooltipMakerAPI tooltip, FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!isUnlocked()) tooltip.addPara("[LOCKED]", 2f);
    }

    @Override
    public String getTitle(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!isUnlocked())  return spec.title+" [LOCKED]";
        return spec.title;
    }

    @Override
    public String getShortDescription(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!isUnlocked())  return "Unlocked by finishing the Kesteven questline on TRUE STARFARER difficulty, or in Mod Settings.";
        return spec.shortDescription;
    }

    @Override
    public String getLongDescription(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!isUnlocked())  return "Can't be selected.";
        return spec.longDescription;
    }

    @Override
    public float getOrder() {
        if (!isUnlocked())  return Integer.MAX_VALUE;
        return spec.order;
    }

    @Override
    public void onNewGameAfterEconomyLoad(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!isUnlocked())  return;
        GameModeManager.setMode(GameModeManager.GameMode.HELLSPAWN);

    }

    @Override
    public void onNewGameAfterTimePass(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!isUnlocked())  return;
        Global.getSector().getIntelManager().addIntel( new HellSpawnIntel());
        new HellSpawnEventIntel(null, true);

        Global.getSector().getFaction(Ids.ENIGMA_FACTION_ID).setRelationship(Factions.PLAYER, 0.75f);
    }

    @Override
    public void addTooltipForSelection(TooltipMakerAPI tooltip, FactionSpecAPI factionSpec, NexFactionConfig factionConfig, Boolean expanded) {
        super.addTooltipForSelection(tooltip, factionSpec, factionConfig, expanded);

        Color hl = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        Color r = Misc.getNegativeHighlightColor();
        float pad = 10.0f;

        if (expanded && isUnlocked()) {
            tooltip.addPara("Start with the Descent event active. Gain points for certain actions as you progress in the campaign. Gain new unlocks when you have enough points.", pad, tc, hl, "Descent");
            tooltip.addPara("For a moment consider what you become.", pad, r, r, "");
        }
    }

    @Override
    public void addTooltipForIntel(TooltipMakerAPI tooltip, FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        Color hl = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        Color r = Misc.getNegativeHighlightColor();
        float pad = 10.0f;

        tooltip.addPara("You started as "+factionSpec.getPersonNamePrefixAOrAn()+" "+factionSpec.getDisplayName()+" captain, with the Hellspawn background. Go to the Hellspawn tab for more information.",
                pad, tc, hl,"Hellspawn");

    }

    private static boolean isUnlocked() {
        return Setting.HELLSPAWN_UNLOCKED.getBoolean();
    }
}
