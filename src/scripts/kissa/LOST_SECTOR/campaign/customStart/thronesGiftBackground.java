package scripts.kissa.LOST_SECTOR.campaign.customStart;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.backgrounds.BaseCharacterBackground;
import exerelin.utilities.NexFactionConfig;
import scripts.kissa.LOST_SECTOR.campaign.customStart.intel.thronesGiftIntel;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;

import java.awt.*;

public class thronesGiftBackground extends BaseCharacterBackground {

    private final boolean unlocked;

    public thronesGiftBackground(){
        unlocked = (boolean) nskr_modPlugin.loadFromConfig(nskr_modPlugin.COMPLETED_STORY_KEY);
    }

    @Override
    public boolean shouldShowInSelection(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (factionSpec.getId().equals(Factions.LUDDIC_PATH) || factionSpec.getId().equals(Factions.LUDDIC_CHURCH)) return false;
        return true;
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
        if (!unlocked) return spec.title+" [LOCKED]";
        return spec.title;
    }

    @Override
    public String getShortDescription(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!unlocked) return "Complete the Kesteven questline on any difficulty to unlock this background.";
        return spec.shortDescription;
    }

    @Override
    public String getLongDescription(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!unlocked) return "Can't be selected.";
        return spec.longDescription;
    }

    @Override
    public float getOrder() {
        if (!unlocked) return Integer.MAX_VALUE-1;
        return spec.order;
    }

    @Override
    public void onNewGameAfterEconomyLoad(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!unlocked) return;
        gamemodeManager.setMode(gamemodeManager.gameMode.THRONESGIFT);

    }

    @Override
    public void onNewGameAfterTimePass(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
        if (!unlocked) return;
        Global.getSector().getIntelManager().addIntel( new thronesGiftIntel());

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

        if (expanded && unlocked) {
            tooltip.addPara("Start with "+(int)thronesGiftManager.DEFAULT_DP+" automation points.", pad, tc, hl, (int)thronesGiftManager.DEFAULT_DP+"");
            tooltip.addPara("Unlock "+(int)thronesGiftManager.DP_PER_UNLOCK+" more points every "+(int)thronesGiftManager.XP_PER_UNLOCK+" experience gained.",
                    pad, tc, hl, (int)thronesGiftManager.DP_PER_UNLOCK+"", (int)thronesGiftManager.XP_PER_UNLOCK+"");
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
}