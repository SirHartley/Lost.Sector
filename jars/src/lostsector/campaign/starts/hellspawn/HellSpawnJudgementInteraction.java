package lostsector.campaign.starts.hellspawn;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.ArrayList;

// The Final Judgement encounter. Its lines are rows on HellSpawnFightModule's triggers, fired into this dialog.
public class HellSpawnJudgementInteraction extends FleetInteractionDialogPluginImpl {

    // Fleet memory flag of the judgement fleet; CorePlugin routes it to this class.
    public static final String JUDGEMENT_FLEET_KEY = "$hellSpawnJudgementFleet";

    private Color b;
    private Color h;
    private TextPanelAPI text;

    public HellSpawnJudgementInteraction(CampaignFleetAPI fleet, InteractionDialogAPI dialog) {
        this(null, fleet, dialog);
    }

    public HellSpawnJudgementInteraction(FIDConfig params, CampaignFleetAPI fleet, InteractionDialogAPI dialog) {
        super();
        this.config = params;
        otherFleet = fleet;
        playerFleet = Global.getSector().getPlayerFleet();
        this.dialog = dialog;

        //BATLLE SETUP
        context = new FleetEncounterContext();
        BattleAPI battle = Global.getFactory().createBattle(playerFleet, otherFleet);
        context.setBattle(battle);
        if (origFlagship == null) {
            origFlagship = Global.getSector().getPlayerFleet().getFlagship();
        }
        if (origCaptains.isEmpty()) {
            for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
                origCaptains.put(member, member.getCaptain());
            }
            membersInOrderPreEncounter = new ArrayList<>(Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy());
        }
        BattleAPI bat = context.getBattle();
        bat.genCombined();
        bat.takeSnapshots();
        playerFleet = bat.getPlayerCombined();
        otherFleet = bat.getNonPlayerCombined();

        //DIALOG SETUP
        dialog.setInteractionTarget(otherFleet);
        init(dialog);

        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();
        visual = dialog.getVisualPanel();

        h = Misc.getHighlightColor();
        b = Misc.getBasePlayerColor();
    }

    public void showFleet(){
        showFleetInfo();
    }

    private boolean openComms = false;
    private boolean disengageNormal = false;
    private boolean disengageStory = false;
    private boolean disengageFight = false;
    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (optionData != OptionId.ATTEMPT_TO_DISENGAGE && optionData != OptionId.CLEAN_DISENGAGE && optionData != OptionId.OPEN_COMM && optionData != OptionId.DISENGAGE){
            super.optionSelected(optionText, optionData);
        }
        else text.addPara(optionText, b, h, "", "");

        //talk
        if (optionData == OptionId.OPEN_COMM){
            FireBest.fire(null, dialog, getMemoryMap(), HellSpawnFightModule.TRIGGER_COMMS);

            options.clearOptions();
            super.optionSelected(optionText, OptionId.INIT);

            openComms = true;

            dialog.getOptionPanel().removeOption(optionData);
        }

        //try to disengage
        if (optionData == OptionId.ATTEMPT_TO_DISENGAGE){
            FireBest.fire(null, dialog, getMemoryMap(), HellSpawnFightModule.TRIGGER_DISENGAGE);

            options.clearOptions();
            super.optionSelected(optionText, OptionId.INIT);

            disengageNormal = true;

            dialog.getOptionPanel().removeOption(optionData);
        }

        //story disengage
        if (optionData == OptionId.CLEAN_DISENGAGE){
            FireBest.fire(null, dialog, getMemoryMap(), HellSpawnFightModule.TRIGGER_STORY_DISENGAGE);

            options.clearOptions();
            super.optionSelected(optionText, OptionId.INIT);

            disengageStory = true;

            dialog.getOptionPanel().removeOption(optionData);
        }

        //fight disengage
        if (optionData == OptionId.DISENGAGE){
            FireBest.fire(null, dialog, getMemoryMap(), HellSpawnFightModule.TRIGGER_BATTLE_DISENGAGE);

            options.clearOptions();
            super.optionSelected(optionText, OptionId.INIT);

            disengageFight = true;

            dialog.getOptionPanel().removeOption(optionData);
        }

        if (optionData == OptionId.CONTINUE_LEAVE || optionData == OptionId.LEAVE || optionData == OptionId.CONTINUE_LOOT){
            otherFleet.despawn();
            HellSpawnQuest.reportJudgementLeft();
        }

        if (openComms) dialog.getOptionPanel().removeOption(OptionId.OPEN_COMM);
        if (disengageNormal) dialog.getOptionPanel().removeOption(OptionId.ATTEMPT_TO_DISENGAGE);
        if (disengageStory) dialog.getOptionPanel().removeOption(OptionId.CLEAN_DISENGAGE);
        if (disengageFight) dialog.getOptionPanel().removeOption(OptionId.DISENGAGE);

    }

}
