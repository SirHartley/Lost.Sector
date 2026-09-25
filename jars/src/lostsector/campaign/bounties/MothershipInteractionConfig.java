package lostsector.campaign.bounties;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;

// The Mothership fleet's encounter: Remnant salvage, a fight to the last with all ships deployed, and, when the
// player leaves after beating it, the TTDS Helios wreck's dialog instead of the map. Saved in the fleet's memory
// through the role config, so it must stay a named top-level class.
public final class MothershipInteractionConfig implements FleetInteractionDialogPluginImpl.FIDConfigGen {

    @Override
    public FleetInteractionDialogPluginImpl.FIDConfig createConfig() {
        FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();

        config.showTransponderStatus = false;
        config.showEngageText = false;
        config.alwaysPursue = false;
        config.dismissOnLeave = false;
        config.withSalvage = true;
        config.printXPToDialog = true;

        config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {
            @Override
            public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
                new RemnantSeededFleetManager.RemnantFleetInteractionConfigGen().createConfig().delegate.
                        postPlayerSalvageGeneration(dialog, context, salvage);
            }

            // The wreck is placed with the reward at the loot; the dialog moves to it once.
            @Override
            public void notifyLeave(InteractionDialogAPI dialog) {
                SectorEntityToken wreck = BountiesQuest.takeMothershipWreck();
                if (wreck == null) {
                    dialog.dismiss();
                    return;
                }
                dialog.setInteractionTarget(wreck);
                RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl();
                dialog.setPlugin(plugin);
                plugin.init(dialog);
            }

            @Override
            public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                bcc.aiRetreatAllowed = false;
                bcc.objectivesAllowed = true;
                bcc.fightToTheLast = true;
                bcc.enemyDeployAll = true;
            }
        };
        return config;
    }
}
