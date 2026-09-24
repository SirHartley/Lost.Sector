package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.kesteven.quest.QuestStageManager;
import lostsector.campaign.kesteven.quest.QuestHelper;
import lostsector.dialogue.rules.nskr_kestevenQuest;
import lostsector.helper.MiscHelper;

import java.awt.*;
import java.util.Map;
import java.util.Random;

public class HintWreckDialog implements InteractionDialogPlugin {

    //

    public static final String HINT_RECEIVED_KEY = "job4HintWreckCoordinatesReceived";
    public static final String PERSISTENT_KEY = "nskr_job4HintWreckDialogKey";
    public static final String PERSISTENT_RANDOM_KEY = "nskr_job4HintWreckDialogRandom";

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
            if (QuestHelper.getCompleted(QuestStageManager.JOB4_FOUND_FRIENDLY_KEY)){
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
            QuestHelper.setCompleted(true, HINT_RECEIVED_KEY);

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
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

            data.put(PERSISTENT_RANDOM_KEY, new Random(MiscHelper.getSeedParsed()));
        }
        return (Random) data.get(PERSISTENT_RANDOM_KEY);
    }

}
