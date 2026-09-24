package lostsector.campaign.starts.thronesgift;

import lostsector.campaign.starts.GameModeManager;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.backgrounds.BaseCharacterBackground;
import exerelin.utilities.NexFactionConfig;
import lostsector.campaign.starts.thronesgift.ThronesGiftIntel;
import lostsector.settings.Setting;

import java.awt.*;

public class ThronesGiftBackground extends BaseCharacterBackground {

    @Override
    public boolean shouldShowInSelection(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (factionSpec.getId().equals(Factions.LUDDIC_PATH) || factionSpec.getId().equals(Factions.LUDDIC_CHURCH)) return false;
        return true;
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
        if (!isUnlocked()) return spec.title+" [LOCKED]";
        return spec.title;
    }

    @Override
    public String getShortDescription(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!isUnlocked()) return "Unlocked by finishing the Kesteven questline on any difficulty, or in Mod Settings.";
        return spec.shortDescription;
    }

    @Override
    public String getLongDescription(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!isUnlocked()) return "Can't be selected.";
        return spec.longDescription;
    }

    @Override
    public float getOrder() {
        if (!isUnlocked()) return Integer.MAX_VALUE-1;
        return spec.order;
    }

    @Override
    public void onNewGameAfterEconomyLoad(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!isUnlocked()) return;
        GameModeManager.setMode(GameModeManager.GameMode.THRONESGIFT);

    }

    @Override
    public void onNewGameAfterTimePass(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!isUnlocked()) return;
        Global.getSector().getIntelManager().addIntel( new ThronesGiftIntel());

        Global.getSector().getPlayerFaction().setRelationship(Factions.LUDDIC_PATH, -0.80f);
    }

    @Override
    public void addTooltipForSelection(TooltipMakerAPI tooltip, FactionSpecAPI factionSpec, NexFactionConfig factionConfig, Boolean expanded) {
        super.addTooltipForSelection(tooltip, factionSpec, factionConfig, expanded);

        Color hl = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 10.0f;

        if (expanded && isUnlocked()) {
            tooltip.addPara("Start with "+(int) ThronesGiftManager.DEFAULT_DP+" automation points.", pad, tc, hl, (int) ThronesGiftManager.DEFAULT_DP+"");
            tooltip.addPara("Unlock "+(int) ThronesGiftManager.DP_PER_UNLOCK+" more points every "+(int) ThronesGiftManager.XP_PER_UNLOCK+" experience gained.",
                    pad, tc, hl, (int) ThronesGiftManager.DP_PER_UNLOCK+"", (int) ThronesGiftManager.XP_PER_UNLOCK+"");
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

        tooltip.addPara("You started as a "+factionSpec.getPersonNamePrefixAOrAn()+" "+factionSpec.getDisplayName()+" captain, with the Throne's Gift background. Go to the Throne's Gift tab for more information.",
                pad, tc, hl,"Throne's Gift");
    }

    private static boolean isUnlocked() {
        return Setting.THRONES_GIFT_UNLOCKED.getBoolean();
    }
}
