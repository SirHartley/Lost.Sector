package scripts.kissa.LOST_SECTOR.campaign.customStart;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.quests.nskr_coreDialog;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class hellSpawnJudgementFID extends FleetInteractionDialogPluginImpl {


    private Random random;
    private Color b;
    private Color tc;
    private Color r;
    private Color gr;
    private Color g;
    private Color h;
    private TextPanelAPI text;

    public hellSpawnJudgementFID(CampaignFleetAPI fleet, InteractionDialogAPI dialog) {
        this(null, fleet, dialog);
    }

    public hellSpawnJudgementFID(FIDConfig params, CampaignFleetAPI fleet, InteractionDialogAPI dialog) {
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
        g = Misc.getGrayColor();
        gr = Misc.getPositiveHighlightColor();
        r = Misc.getNegativeHighlightColor();
        tc = Misc.getTextColor();
        b = Misc.getBasePlayerColor();
        random = hellSpawnManager.getRandom();
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
            text.addPara("What? The chance to talk your way out is long gone.", g, h, "", "");
            text.addPara("You attempt to open comms but nothing happens.", tc, h, "", "");

            options.clearOptions();
            super.optionSelected(optionText, OptionId.INIT);

            openComms = true;

            dialog.getOptionPanel().removeOption(optionData);
        }

        //try to disengage
        if (optionData == OptionId.ATTEMPT_TO_DISENGAGE){
            text.addPara("Ha ha ha.", g, h, "", "");
            text.addPara("Not so fast captain.", g, h, "", "");
            text.addPara("Your attempt was quickly blocked by the opposing fleet.", tc, h, "", "");

            options.clearOptions();
            super.optionSelected(optionText, OptionId.INIT);

            disengageNormal = true;

            dialog.getOptionPanel().removeOption(optionData);
        }

        //story disengage
        if (optionData == OptionId.CLEAN_DISENGAGE){
            text.addPara("Pathetic.", g, h, "", "");
            text.addPara("There is no escape.", g, h, "", "");
            text.addPara("Despite your best efforts the opposing fleet remains one step ahead of you.", tc, h, "", "");

            options.clearOptions();
            super.optionSelected(optionText, OptionId.INIT);

            disengageStory = true;

            dialog.getOptionPanel().removeOption(optionData);
        }

        //fight disengage
        if (optionData == OptionId.DISENGAGE){
            text.addPara("You have to finish this.", g, h, "", "");
            text.addPara("The opposing fleet doesn't seem to be effected by the damage you caused.", tc, h, "", "");

            options.clearOptions();
            super.optionSelected(optionText, OptionId.INIT);

            disengageFight = true;

            dialog.getOptionPanel().removeOption(optionData);
        }

        if (optionData == OptionId.CONTINUE_LEAVE || optionData == OptionId.LEAVE || optionData == OptionId.CONTINUE_LOOT){
            otherFleet.despawn();
            questUtil.setCompleted(true, hellSpawnManager.JUDGEMENT_DEFEATED_KEY);
        }

        if (openComms) dialog.getOptionPanel().removeOption(OptionId.OPEN_COMM);
        if (disengageNormal) dialog.getOptionPanel().removeOption(OptionId.ATTEMPT_TO_DISENGAGE);
        if (disengageStory) dialog.getOptionPanel().removeOption(OptionId.CLEAN_DISENGAGE);
        if (disengageFight) dialog.getOptionPanel().removeOption(OptionId.DISENGAGE);

    }

}
