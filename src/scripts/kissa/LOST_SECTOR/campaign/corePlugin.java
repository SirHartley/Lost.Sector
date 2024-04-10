package scripts.kissa.LOST_SECTOR.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.*;
import scripts.kissa.LOST_SECTOR.campaign.customStart.abilities.hellSpawnAbilityFID;
import scripts.kissa.LOST_SECTOR.campaign.customStart.hellSpawnJudgementDialog;
import scripts.kissa.LOST_SECTOR.campaign.customStart.hellSpawnJudgementFID;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_mothershipInteractionBlocker;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_mothershipSpawner;
import scripts.kissa.LOST_SECTOR.campaign.fleets.events.blacksiteInfo;
import scripts.kissa.LOST_SECTOR.campaign.fleets.events.nskr_blacksiteDialog;
import scripts.kissa.LOST_SECTOR.campaign.fleets.events.nskr_blacksiteManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.*;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.*;

import java.util.Collection;
import java.util.List;

public class corePlugin extends BaseCampaignPlugin {

    static void log(final String message) {
        Global.getLogger(corePlugin.class).info(message);
    }

    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
        //HELLSPAWN judgement fleet
        if (interactionTarget instanceof CampaignFleetAPI && interactionTarget.getMemoryWithoutUpdate().contains(hellSpawnJudgementDialog.JUDGEMENT_FLEET_KEY)) {
            if (Global.getSector().getCampaignUI().getCurrentInteractionDialog()!=null) {
                return new PluginPick<InteractionDialogPlugin>(
                        new hellSpawnJudgementFID((CampaignFleetAPI) interactionTarget, Global.getSector().getCampaignUI().getCurrentInteractionDialog()), CampaignPlugin.PickPriority.MOD_SET);
            }
        }
        //HELLSPAWN fleet join logic
        if (interactionTarget instanceof CampaignFleetAPI && hellSpawnAbilityFID.hellSpawnInRange()) {
            return new PluginPick<InteractionDialogPlugin>(new hellSpawnAbilityFID(), CampaignPlugin.PickPriority.MOD_SET);
        }
        //blacksite interaction
        List<blacksiteInfo> sites = nskr_blacksiteManager.getSites(nskr_blacksiteManager.SITE_ARRAY_KEY);
        if (!sites.isEmpty()) {
            blacksiteInfo site = nskr_blacksiteManager.getInfo(interactionTarget.getId(), sites);
            if (site != null) {
                return new PluginPick<InteractionDialogPlugin>(new nskr_blacksiteDialog(), PickPriority.MOD_GENERAL);
            }
        }
        //mothership bounty
        if (interactionTarget.getId().equals(nskr_mothershipSpawner.PLANET1_ID) || interactionTarget.getId().equals(nskr_mothershipSpawner.PLANET2_ID)) {
            if (!questUtil.getCompleted(nskr_mothershipSpawner.MOTHERSHIP_SPAWNED_MEM_KEY) && !nskr_mothershipSpawner.getBountyCompleted()) {
                return new PluginPick<InteractionDialogPlugin>(new nskr_mothershipInteractionBlocker(), PickPriority.MOD_GENERAL);
            }
        }
        //job4hintWreck dialog
        if (interactionTarget.getId().startsWith(questStageManager.JOB4_HINT_WRECK_ID_KEY) && !questUtil.getCompleted(nskr_job4HintWreck.HINT_RECEIVED_KEY)) {
            return new PluginPick<InteractionDialogPlugin>(new nskr_job4HintWreck(), PickPriority.MOD_GENERAL);
        }
        //glacier comms dialog
        int stage = questUtil.getStage();
        boolean aliceTip2 = questUtil.getCompleted(nskr_kestevenQuest.JOB5_ALICE_TIP_KEY2);
        if (interactionTarget.getId().equals("nskr_glacier") && aliceTip2 && stage>=16 && !questUtil.getCompleted(nskr_glacierCommsDialog.RECOVERED_KEY)) {
            return new PluginPick<InteractionDialogPlugin>(new nskr_glacierCommsDialog(), PickPriority.MOD_GENERAL);
        }
        //satellite dialog
        if (hasMemoryKeyStartsWith(questStageManager.ARTIFACT_KEY, interactionTarget)) {
            return new PluginPick<InteractionDialogPlugin>(new nskr_artifactDialog(), PickPriority.MOD_GENERAL);
        }
        //cache recovery dialog
        if (interactionTarget.getId().equals("nskr_cache_core")) {
            return new PluginPick<InteractionDialogPlugin>(new nskr_coreDialog(), PickPriority.MOD_GENERAL);
        }
        //job5 eliza dialog
        if (questUtil.getElizaLoc()!=null){
            String loc = questUtil.getElizaLoc().getId();
            if (interactionTarget.getId().equals(loc) && !questUtil.getCompleted(nskr_elizaDialog.DIALOG_FINISHED_KEY)){
                return new PluginPick<InteractionDialogPlugin>(new nskr_elizaDialog(), PickPriority.MOD_GENERAL);
            }
        }
        //job5 end
        if (stage==19){
            //kesteven
            if (questUtil.asteriaOrOutpost()!=null) {
                String loc = questUtil.asteriaOrOutpost().getId();
                if (!questUtil.getCompleted(nskr_EndingKestevenDialog.DIALOG_FINISHED_KEY) && !questUtil.getCompleted(questStageManager.ELIZA_INTERCEPT_HANDED_OVER)) {
                    if (interactionTarget.getId().equals(loc)) {
                        return new PluginPick<InteractionDialogPlugin>(new nskr_EndingKestevenDialog(), PickPriority.MOD_GENERAL);
                    } else if (loc.equals("nskr_asteria") && interactionTarget.getId().equals("nskr_asteria_station")){
                        return new PluginPick<InteractionDialogPlugin>(new nskr_EndingKestevenDialog(), PickPriority.MOD_GENERAL);
                    }
                }
            }
            //eliza
            if (questUtil.getElizaLoc()!=null){
                String loc = questUtil.getElizaLoc().getId();
                if (!questUtil.getCompleted(nskr_EndingElizaDialog.DIALOG_FINISHED_KEY) && !questUtil.getCompleted(questStageManager.KILLED_ELIZA_KEY) &&
                        questUtil.getCompleted(nskr_elizaDialog.ELIZA_HELP_KEY) && questUtil.getCompleted(questStageManager.ELIZA_INTERCEPT_HANDED_OVER) && questUtil.getCompleted(questStageManager.ELIZA_RETURNED_KEY)){
                    if (interactionTarget.getId().equals(loc)) {
                        return new PluginPick<InteractionDialogPlugin>(new nskr_EndingElizaDialog(), PickPriority.MOD_GENERAL);
                    } else if (interactionTarget.getMarket()!=null && interactionTarget.getMarket().getConnectedEntities().contains(questUtil.getElizaLoc())) {
                        return new PluginPick<InteractionDialogPlugin>(new nskr_EndingElizaDialog(), PickPriority.MOD_GENERAL);
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



