package lostsector.campaign;

import lostsector.campaign.kesteven.quest.CacheCoreDialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.*;
import lostsector.campaign.starts.hellspawn.HellSpawnAbilityInteraction;
import lostsector.campaign.starts.hellspawn.HellSpawnJudgementInteraction;
import lostsector.quest.QuestDialogs;

public class CorePlugin extends BaseCampaignPlugin {

    static void log(final String message) {
        Global.getLogger(CorePlugin.class).info(message);
    }

    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
        //HELLSPAWN judgement fleet
        if (interactionTarget instanceof CampaignFleetAPI && interactionTarget.getMemoryWithoutUpdate().contains(HellSpawnJudgementInteraction.JUDGEMENT_FLEET_KEY)) {
            if (Global.getSector().getCampaignUI().getCurrentInteractionDialog()!=null) {
                return new PluginPick<InteractionDialogPlugin>(
                        new HellSpawnJudgementInteraction((CampaignFleetAPI) interactionTarget, Global.getSector().getCampaignUI().getCurrentInteractionDialog()), CampaignPlugin.PickPriority.MOD_SET);
            }
        }
        //HELLSPAWN fleet join logic
        if (interactionTarget instanceof CampaignFleetAPI && HellSpawnAbilityInteraction.hellSpawnInRange()) {
            return new PluginPick<InteractionDialogPlugin>(new HellSpawnAbilityInteraction(), CampaignPlugin.PickPriority.MOD_SET);
        }
        String claimedTrigger = QuestDialogs.claimedTrigger(interactionTarget);
        if (claimedTrigger != null) {
            return new PluginPick<InteractionDialogPlugin>(QuestDialogs.plugin(claimedTrigger), PickPriority.MOD_GENERAL);
        }
        //cache recovery dialog
        if (interactionTarget.getId().equals("nskr_cache_core")) {
            return new PluginPick<InteractionDialogPlugin>(new CacheCoreDialog(), PickPriority.MOD_GENERAL);
        }
        return null;
    }

}



