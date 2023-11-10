package scripts.kissa.LOST_SECTOR.campaign.quests;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.nskr_kestevenQuest;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.Map;
import java.util.Random;

public class nskr_job4HintWreck implements InteractionDialogPlugin {

    //

    public static final String HINT_RECEIVED_KEY = "job4HintWreckCoordinatesReceived";
    public static final String PERSISTENT_KEY = "nskr_job4HintWreckDialogKey";
    public static final String PERSISTENT_RANDOM_KEY = "nskr_job4HintWreckDialogRandom";

    private InteractionDialogAPI dialog;
    private TextPanelAPI text;
    private OptionPanelAPI options;
    private VisualPanelAPI visual;

    static void log(final String message) {
        Global.getLogger(nskr_job4HintWreck.class).info(message);
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

        //options.addOption("Leave", OptionId.LEAVE);

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
            String loc = questUtil.getJob4FriendlyTarget().getStarSystem().getName();

            text.addPara("A mysterious source on the ship is transmitting a garbled signal, most likely the result of heavy battle damage. " +
                    "After some time the comms team manages to recover something of use.");
            text.addPara("Your ops chief reports. \"It seems to be broadcasting some coordinates located in the "+loc+". That part of the broadcast was unencrypted, seems like they were in a hurry.\"");

            //already found
            if (questUtil.getCompleted(questStageManager.JOB4_FOUND_FRIENDLY_KEY)){
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
            questUtil.setCompleted(true, HINT_RECEIVED_KEY);

            dialog.setOptionOnEscape("Continue", OptionId.LEAVE);

            options.addOption("Continue", OptionId.LEAVE);
        }

        //leave
        if (optionData == OptionId.LEAVE) {
            dialog.dismiss();
            //return to default
            //Map<String, MemoryAPI> map = dialog.getPlugin().getMemoryMap();
            //MemoryAPI memory = BaseCommandPlugin.getEntityMemory(map);
            //Object specialData = memory.get(MemFlags.SALVAGE_SPECIAL_DATA);
            //SalvageSpecialInteraction.SalvageSpecialPlugin special = null;
            //if (specialData instanceof SalvageSpecialInteraction.SalvageSpecialData) {
            //    special = ((SalvageSpecialInteraction.SalvageSpecialData) specialData).createSpecialPlugin();
            //}
            //InteractionDialogPlugin plugin = new SalvageSpecialInteraction.SalvageSpecialDialogPlugin(dialog.getPlugin(), special, specialData, map);
            //dialog.setPlugin(plugin);
            //plugin.init(dialog);
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

            data.put(PERSISTENT_RANDOM_KEY, new Random(util.getSeedParsed()));
        }
        return (Random) data.get(PERSISTENT_RANDOM_KEY);
    }

}
