package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.kesteven.quest.QuestHelper;
import lostsector.dialogue.rules.nskr_kestevenQuest;

import java.awt.*;
import java.util.Map;
import java.util.Random;

public class HintWreckDialog implements InteractionDialogPlugin {

    //


    private InteractionDialogAPI dialog;
    private TextPanelAPI text;
    private OptionPanelAPI options;
    private VisualPanelAPI visual;

    static void log(final String message) {
        Global.getLogger(HintWreckDialog.class).info(message);
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;

        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();
        visual = dialog.getVisualPanel();

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();
        Random random = nskr_kestevenQuest.getRandom();

        visual.showImageVisual(dialog.getInteractionTarget().getCustomInteractionDialogImageVisual());

        text.setFontInsignia();

        text.addPara("Your sensors team report unusual signals coming from this wreck once you approach.",tc,h,"","");

        options.addOption("Continue", OptionId.INITIAL);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        Color h = Misc.getHighlightColor();
        Color b = Misc.getBasePlayerColor();
        Color bh = Misc.getBrightPlayerColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();

        text.addPara(optionText, b, h, "", "");
        options.clearOptions();

        //initial
        if (optionData == OptionId.INITIAL) {
            String loc = QuestHelper.getJob4FriendlyTarget().getStarSystem().getName();

            text.addPara("A mysterious source on the ship is transmitting a garbled signal, most likely the result of heavy battle damage. " +
                    "After some time the comms team manages to recover something of use.");
            text.addPara("Your ops chief reports. \"It seems to be broadcasting some coordinates located in the "+loc+". That part of the broadcast was unencrypted, seems like they were in a hurry.\"");

            //already found
            if (QuestHelper.getCompleted(KestevenFlag.JOB4_FRIENDLY_FOUND)){
                text.addPara("\"Ah- but that's where we found the remaining Special Operations fleet, mystery solved.\" They show a quick smirk.");
            }
            //normal
            else {
                text.setFontSmallInsignia();
                text.addPara("Acquired coordinates for the " + loc, g, h, loc, "");

                Global.getSoundPlayer().playUISound("ui_noise_static", 1f, 1f);
                text.setFontInsignia();
            }

            //complete
            QuestHelper.setCompleted(true, KestevenFlag.JOB4_HINT_WRECK_READ);

            dialog.setOptionOnEscape("Continue", OptionId.LEAVE);

            options.addOption("Continue", OptionId.LEAVE);
        }

        //leave
        if (optionData == OptionId.LEAVE) {
            dialog.dismiss();
        }
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {

    }

    public enum OptionId {
        INITIAL,
        LEAVE,
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {

    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        return null;
    }

    public static Random getRandom() {
        return KestevenQuest.random(KestevenState.RANDOM_HINT_WRECK);
    }

}
