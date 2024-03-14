package scripts.kissa.LOST_SECTOR.campaign.customStart;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.backgrounds.BaseCharacterBackground;
import exerelin.utilities.NexFactionConfig;
import scripts.kissa.LOST_SECTOR.campaign.customStart.intel.hellSpawnEventIntel;
import scripts.kissa.LOST_SECTOR.campaign.customStart.intel.hellSpawnIntel;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.util.ids;

import java.awt.*;

public class hellSpawnBackground extends BaseCharacterBackground {

    private final boolean unlocked;

    public hellSpawnBackground(){
        unlocked = (boolean) nskr_modPlugin.loadFromConfig(nskr_modPlugin.COMPLETED_STORY_HARD_KEY);
    }

    @Override
    public boolean shouldShowInSelection(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        return unlocked;
    }

    @Override
    public boolean canBeSelected(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        return unlocked;
    }

    @Override
    public void canNotBeSelectedReason(TooltipMakerAPI tooltip, FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!unlocked) tooltip.addPara("[LOCKED]", 2f);
    }

    @Override
    public String getTitle(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!unlocked)  return spec.title+" [LOCKED]";
        return spec.title;
    }

    @Override
    public String getShortDescription(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!unlocked)  return "Complete the Kesteven questline on TRUE STARFARER mode to unlock this background.";
        return spec.shortDescription;
    }

    @Override
    public String getLongDescription(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!unlocked)  return "Can't be selected.";
        return spec.longDescription;
    }

    @Override
    public float getOrder() {
        if (!unlocked)  return Integer.MAX_VALUE;
        return spec.order;
    }

    @Override
    public void onNewGameAfterEconomyLoad(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!unlocked)  return;
        gamemodeManager.setMode(gamemodeManager.gameMode.HELLSPAWN);

    }

    @Override
    public void onNewGameAfterTimePass(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!unlocked)  return;
        Global.getSector().getIntelManager().addIntel( new hellSpawnIntel());
        new hellSpawnEventIntel(null, true);

        Global.getSector().getFaction(ids.ENIGMA_FACTION_ID).setRelationship(Factions.PLAYER, 0.75f);
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

        if (expanded && unlocked) {
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
}