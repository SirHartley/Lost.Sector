package lostsector.campaign;

import lostsector.campaign.kesteven.quest.CacheCoreDialog;
import lostsector.campaign.kesteven.quest.DataSatelliteDialog;
import lostsector.campaign.kesteven.quest.ElizaDialog;
import lostsector.campaign.kesteven.quest.EndingElizaDialog;
import lostsector.campaign.kesteven.quest.EndingKestevenDialog;
import lostsector.campaign.kesteven.quest.HintWreckDialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.*;
import lostsector.campaign.starts.hellspawn.HellSpawnAbilityInteraction;
import lostsector.campaign.starts.hellspawn.HellSpawnJudgementInteraction;
import lostsector.campaign.kesteven.quest.KestevenQuest;
import lostsector.helper.SectorLookup;
import lostsector.quest.QuestDialogs;

import java.util.Collection;

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
        //job4hintWreck dialog
        if (KestevenQuest.isUnreadHintWreck(interactionTarget)) {
            return new PluginPick<InteractionDialogPlugin>(new HintWreckDialog(), PickPriority.MOD_GENERAL);
        }
        //satellite dialog
        if (KestevenQuest.isDataSatellite(interactionTarget)) {
            return new PluginPick<InteractionDialogPlugin>(new DataSatelliteDialog(), PickPriority.MOD_GENERAL);
        }
        //cache recovery dialog
        if (interactionTarget.getId().equals("nskr_cache_core")) {
            return new PluginPick<InteractionDialogPlugin>(new CacheCoreDialog(), PickPriority.MOD_GENERAL);
        }
        //job5 eliza dialog
        if (!KestevenQuest.elizaMeetingDone() && KestevenQuest.atElizaMarket(interactionTarget)) {
            return new PluginPick<InteractionDialogPlugin>(new ElizaDialog(), PickPriority.MOD_GENERAL);
        }
        //job5 end
        //kesteven
        if (KestevenQuest.kestevenEndingAvailable() && SectorLookup.asteriaOrOutpost()!=null) {
            String loc = SectorLookup.asteriaOrOutpost().getId();
            if (interactionTarget.getId().equals(loc)) {
                return new PluginPick<InteractionDialogPlugin>(new EndingKestevenDialog(), PickPriority.MOD_GENERAL);
            } else if (loc.equals("nskr_asteria") && interactionTarget.getId().equals("nskr_asteria_station")){
                return new PluginPick<InteractionDialogPlugin>(new EndingKestevenDialog(), PickPriority.MOD_GENERAL);
            }
        }
        //eliza
        if (KestevenQuest.elizaEndingAvailable() && KestevenQuest.atElizaMarket(interactionTarget)) {
            return new PluginPick<InteractionDialogPlugin>(new EndingElizaDialog(), PickPriority.MOD_GENERAL);
        }

        return null;
    }

    public static boolean hasMemoryKeyStartsWith(String arg, SectorEntityToken entity){
        boolean startsWith = false;
        if (entity==null || entity.getMemory()==null) return false;

        Collection<String> mem = entity.getMemory().getKeys();
        if (mem.isEmpty()) return false;
        for (String m : mem){
            if (m==null)continue;
            if (m.startsWith(arg)){
                startsWith = true;
                break;
            }
        }
        return startsWith;
    }

}



