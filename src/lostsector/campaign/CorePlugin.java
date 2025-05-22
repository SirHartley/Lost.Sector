package lostsector.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.*;
import lostsector.campaign.customStart.abilities.HellSpawnAbilityFID;
import lostsector.campaign.customStart.HellSpawnJudgementDialog;
import lostsector.campaign.customStart.HellSpawnJudgementFID;
import lostsector.campaign.fleets.bounties.MothershipInteractionBlocker;
import lostsector.campaign.fleets.bounties.MothershipSpawner;
import lostsector.campaign.fleets.events.BlacksiteInfo;
import lostsector.campaign.fleets.events.BlacksiteDialog;
import lostsector.campaign.fleets.events.BlacksiteManager;
import lostsector.campaign.quests.*;
import lostsector.campaign.quests.util.QuestStageManager;
import lostsector.campaign.quests.util.QuestUtil;
import lostsector.campaign.rulecmd.KestevenQuest;

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
                        new HellSpawnJudgementFID((CampaignFleetAPI) interactionTarget, Global.getSector().getCampaignUI().getCurrentInteractionDialog()), CampaignPlugin.PickPriority.MOD_SET);
            }
        }
        //HELLSPAWN fleet join logic
        if (interactionTarget instanceof CampaignFleetAPI && HellSpawnAbilityFID.hellSpawnInRange()) {
            return new PluginPick<InteractionDialogPlugin>(new HellSpawnAbilityFID(), CampaignPlugin.PickPriority.MOD_SET);
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
            if (!QuestUtil.getCompleted(MothershipSpawner.MOTHERSHIP_SPAWNED_MEM_KEY) && !MothershipSpawner.getBountyCompleted()) {
                return new PluginPick<InteractionDialogPlugin>(new MothershipInteractionBlocker(), PickPriority.MOD_GENERAL);
            }
        }
        //job4hintWreck dialog
        if (interactionTarget.getId().startsWith(QuestStageManager.JOB4_HINT_WRECK_ID_KEY) && !QuestUtil.getCompleted(Job4HintWreck.HINT_RECEIVED_KEY)) {
            return new PluginPick<InteractionDialogPlugin>(new Job4HintWreck(), PickPriority.MOD_GENERAL);
        }
        //glacier comms dialog
        int stage = QuestUtil.getStage();
        boolean aliceTip2 = QuestUtil.getCompleted(KestevenQuest.JOB5_ALICE_TIP_KEY2);
        if (interactionTarget.getId().equals("nskr_glacier") && aliceTip2 && stage>=16 && !QuestUtil.getCompleted(GlacierCommsDialog.RECOVERED_KEY)) {
            return new PluginPick<InteractionDialogPlugin>(new GlacierCommsDialog(), PickPriority.MOD_GENERAL);
        }
        //satellite dialog
        if (hasMemoryKeyStartsWith(QuestStageManager.ARTIFACT_KEY, interactionTarget)) {
            return new PluginPick<InteractionDialogPlugin>(new ArtifactDialog(), PickPriority.MOD_GENERAL);
        }
        //cache recovery dialog
        if (interactionTarget.getId().equals("nskr_cache_core")) {
            return new PluginPick<InteractionDialogPlugin>(new CoreDialog(), PickPriority.MOD_GENERAL);
        }
        //job5 eliza dialog
        if (QuestUtil.getElizaLoc()!=null) {
            String loc = QuestUtil.getElizaLoc().getId();
            if (!QuestUtil.getCompleted(ElizaDialog.DIALOG_FINISHED_KEY)) {
                if (interactionTarget.getId().equals(loc)) {
                    return new PluginPick<InteractionDialogPlugin>(new ElizaDialog(), PickPriority.MOD_GENERAL);
                } else if (interactionTarget.getMarket() != null && interactionTarget.getMarket().getConnectedEntities().contains(QuestUtil.getElizaLoc())) {
                    return new PluginPick<InteractionDialogPlugin>(new ElizaDialog(), PickPriority.MOD_GENERAL);
                }
            }
        }
        //job5 end
        if (stage==19){
            //kesteven
            if (QuestUtil.asteriaOrOutpost()!=null) {
                String loc = QuestUtil.asteriaOrOutpost().getId();
                if (!QuestUtil.getCompleted(EndingKestevenDialog.DIALOG_FINISHED_KEY) && !QuestUtil.getCompleted(QuestStageManager.ELIZA_INTERCEPT_HANDED_OVER)) {
                    if (interactionTarget.getId().equals(loc)) {
                        return new PluginPick<InteractionDialogPlugin>(new EndingKestevenDialog(), PickPriority.MOD_GENERAL);
                    } else if (loc.equals("nskr_asteria") && interactionTarget.getId().equals("nskr_asteria_station")){
                        return new PluginPick<InteractionDialogPlugin>(new EndingKestevenDialog(), PickPriority.MOD_GENERAL);
                    }
                }
            }
            //eliza
            if (QuestUtil.getElizaLoc()!=null){
                String loc = QuestUtil.getElizaLoc().getId();
                if (!QuestUtil.getCompleted(EndingElizaDialog.DIALOG_FINISHED_KEY) && !QuestUtil.getCompleted(QuestStageManager.KILLED_ELIZA_KEY) &&
                        QuestUtil.getCompleted(ElizaDialog.ELIZA_HELP_KEY) && QuestUtil.getCompleted(QuestStageManager.ELIZA_INTERCEPT_HANDED_OVER) && QuestUtil.getCompleted(QuestStageManager.ELIZA_RETURNED_KEY)){
                    if (interactionTarget.getId().equals(loc)) {
                        return new PluginPick<InteractionDialogPlugin>(new EndingElizaDialog(), PickPriority.MOD_GENERAL);
                    } else if (interactionTarget.getMarket()!=null && interactionTarget.getMarket().getConnectedEntities().contains(QuestUtil.getElizaLoc())) {
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



