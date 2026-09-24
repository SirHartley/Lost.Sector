package lostsector.campaign;

import lostsector.campaign.kesteven.quest.CacheCoreDialog;
import lostsector.campaign.kesteven.quest.DataSatelliteDialog;
import lostsector.campaign.kesteven.quest.ElizaDialog;
import lostsector.campaign.kesteven.quest.EndingElizaDialog;
import lostsector.campaign.kesteven.quest.EndingKestevenDialog;
import lostsector.campaign.kesteven.quest.GlacierCommsDialog;
import lostsector.campaign.kesteven.quest.HintWreckDialog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.*;
import lostsector.campaign.starts.hellspawn.HellSpawnAbilityInteraction;
import lostsector.campaign.starts.hellspawn.HellSpawnJudgementDialog;
import lostsector.campaign.starts.hellspawn.HellSpawnJudgementInteraction;
import lostsector.campaign.bounties.mothership.MothershipInteractionBlocker;
import lostsector.campaign.bounties.mothership.MothershipSpawner;
import lostsector.campaign.events.blacksite.BlacksiteInfo;
import lostsector.campaign.events.blacksite.BlacksiteDialog;
import lostsector.campaign.events.blacksite.BlacksiteManager;
import lostsector.campaign.kesteven.quest.QuestStageManager;
import lostsector.campaign.kesteven.quest.QuestHelper;
import lostsector.dialogue.rules.nskr_kestevenQuest;

import java.util.Collection;
import java.util.List;

public class CorePlugin extends BaseCampaignPlugin {

    static void log(final String message) {
        Global.getLogger(CorePlugin.class).info(message);
    }

    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
        //HELLSPAWN judgement fleet
        if (interactionTarget instanceof CampaignFleetAPI && interactionTarget.getMemoryWithoutUpdate().contains(HellSpawnJudgementDialog.JUDGEMENT_FLEET_KEY)) {
            if (Global.getSector().getCampaignUI().getCurrentInteractionDialog()!=null) {
                return new PluginPick<InteractionDialogPlugin>(
                        new HellSpawnJudgementInteraction((CampaignFleetAPI) interactionTarget, Global.getSector().getCampaignUI().getCurrentInteractionDialog()), CampaignPlugin.PickPriority.MOD_SET);
            }
        }
        //HELLSPAWN fleet join logic
        if (interactionTarget instanceof CampaignFleetAPI && HellSpawnAbilityInteraction.hellSpawnInRange()) {
            return new PluginPick<InteractionDialogPlugin>(new HellSpawnAbilityInteraction(), CampaignPlugin.PickPriority.MOD_SET);
        }
        //blacksite interaction
        List<BlacksiteInfo> sites = BlacksiteManager.getSites(BlacksiteManager.SITE_ARRAY_KEY);
        if (!sites.isEmpty()) {
            BlacksiteInfo site = BlacksiteManager.getInfo(interactionTarget.getId(), sites);
            if (site != null) {
                return new PluginPick<InteractionDialogPlugin>(new BlacksiteDialog(), PickPriority.MOD_GENERAL);
            }
        }
        //mothership bounty
        if (interactionTarget.getId().equals(MothershipSpawner.PLANET1_ID) || interactionTarget.getId().equals(MothershipSpawner.PLANET2_ID)) {
            if (!QuestHelper.getCompleted(MothershipSpawner.MOTHERSHIP_SPAWNED_MEM_KEY) && !MothershipSpawner.getBountyCompleted()) {
                return new PluginPick<InteractionDialogPlugin>(new MothershipInteractionBlocker(), PickPriority.MOD_GENERAL);
            }
        }
        //job4hintWreck dialog
        if (interactionTarget.getId().startsWith(QuestStageManager.JOB4_HINT_WRECK_ID_KEY) && !QuestHelper.getCompleted(HintWreckDialog.HINT_RECEIVED_KEY)) {
            return new PluginPick<InteractionDialogPlugin>(new HintWreckDialog(), PickPriority.MOD_GENERAL);
        }
        //glacier comms dialog
        int stage = QuestHelper.getStage();
        boolean aliceTip2 = QuestHelper.getCompleted(nskr_kestevenQuest.JOB5_ALICE_TIP_KEY2);
        if (interactionTarget.getId().equals("nskr_glacier") && aliceTip2 && stage>=16 && !QuestHelper.getCompleted(GlacierCommsDialog.RECOVERED_KEY)) {
            return new PluginPick<InteractionDialogPlugin>(new GlacierCommsDialog(), PickPriority.MOD_GENERAL);
        }
        //satellite dialog
        if (hasMemoryKeyStartsWith(QuestStageManager.ARTIFACT_KEY, interactionTarget)) {
            return new PluginPick<InteractionDialogPlugin>(new DataSatelliteDialog(), PickPriority.MOD_GENERAL);
        }
        //cache recovery dialog
        if (interactionTarget.getId().equals("nskr_cache_core")) {
            return new PluginPick<InteractionDialogPlugin>(new CacheCoreDialog(), PickPriority.MOD_GENERAL);
        }
        //job5 eliza dialog
        if (QuestHelper.getElizaLoc()!=null) {
            String loc = QuestHelper.getElizaLoc().getId();
            if (!QuestHelper.getCompleted(ElizaDialog.DIALOG_FINISHED_KEY)) {
                if (interactionTarget.getId().equals(loc)) {
                    return new PluginPick<InteractionDialogPlugin>(new ElizaDialog(), PickPriority.MOD_GENERAL);
                } else if (interactionTarget.getMarket() != null && interactionTarget.getMarket().getConnectedEntities().contains(QuestHelper.getElizaLoc())) {
                    return new PluginPick<InteractionDialogPlugin>(new ElizaDialog(), PickPriority.MOD_GENERAL);
                }
            }
        }
        //job5 end
        if (stage==19){
            //kesteven
            if (QuestHelper.asteriaOrOutpost()!=null) {
                String loc = QuestHelper.asteriaOrOutpost().getId();
                if (!QuestHelper.getCompleted(EndingKestevenDialog.DIALOG_FINISHED_KEY) && !QuestHelper.getCompleted(QuestStageManager.ELIZA_INTERCEPT_HANDED_OVER)) {
                    if (interactionTarget.getId().equals(loc)) {
                        return new PluginPick<InteractionDialogPlugin>(new EndingKestevenDialog(), PickPriority.MOD_GENERAL);
                    } else if (loc.equals("nskr_asteria") && interactionTarget.getId().equals("nskr_asteria_station")){
                        return new PluginPick<InteractionDialogPlugin>(new EndingKestevenDialog(), PickPriority.MOD_GENERAL);
                    }
                }
            }
            //eliza
            if (QuestHelper.getElizaLoc()!=null){
                String loc = QuestHelper.getElizaLoc().getId();
                if (!QuestHelper.getCompleted(EndingElizaDialog.DIALOG_FINISHED_KEY) && !QuestHelper.getCompleted(QuestStageManager.KILLED_ELIZA_KEY) &&
                        QuestHelper.getCompleted(ElizaDialog.ELIZA_HELP_KEY) && QuestHelper.getCompleted(QuestStageManager.ELIZA_INTERCEPT_HANDED_OVER) && QuestHelper.getCompleted(QuestStageManager.ELIZA_RETURNED_KEY)){
                    if (interactionTarget.getId().equals(loc)) {
                        return new PluginPick<InteractionDialogPlugin>(new EndingElizaDialog(), PickPriority.MOD_GENERAL);
                    } else if (interactionTarget.getMarket()!=null && interactionTarget.getMarket().getConnectedEntities().contains(QuestHelper.getElizaLoc())) {
                        return new PluginPick<InteractionDialogPlugin>(new EndingElizaDialog(), PickPriority.MOD_GENERAL);
                    }
                }
            }
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



