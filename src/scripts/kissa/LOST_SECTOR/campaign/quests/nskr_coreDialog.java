package scripts.kissa.LOST_SECTOR.campaign.quests;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.Map;
import java.util.Random;

public class nskr_coreDialog implements InteractionDialogPlugin {

    //

    public static final String RECOVERED_KEY = "nskr_coreDialogRecoveredKey";
    public static final String FIRST_TIME_KEY = "nskr_coreDialogFirstTimeKey";
    public static final String PERSISTENT_KEY = "nskr_coreDialogKey";
    public static final String PERSISTENT_RANDOM_KEY = "nskr_coreDialogKeyRandom";

    private boolean arrived = false;
    private InteractionDialogAPI dialog;
    private TextPanelAPI text;
    private OptionPanelAPI options;
    private VisualPanelAPI visual;

    static void log(final String message) {
        Global.getLogger(nskr_artifactDialog.class).info(message);
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;

        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();
        visual = dialog.getVisualPanel();

        dialog.getVisualPanel().showPlanetInfo(dialog.getInteractionTarget());

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();

        text.setFontInsignia();

        //options.addOption("Leave", OptionId.LEAVE);

        boolean recovered = questUtil.getCompleted(RECOVERED_KEY);
        boolean firstTime = questUtil.getCompleted(FIRST_TIME_KEY);
        //this is a fucking disaster
        if (!firstTime){
            text.addPara("You watch as the system materializes in front of you, gracefully out of thin air. No one aboard the bridge knows how this was possible.");
            text.addPara("The vast structures float unbothered, like they have always been here. Seemingly unknowing to what has just taken effect.");
            text.addPara("As your bridge crew tries to pick through all the new signals they spot a cluster of computer cores in the debris.");

            Global.getSoundPlayer().playUISound("ui_discovered_entity",1f,1f);

            questUtil.setCompleted(true, FIRST_TIME_KEY);

            if (questUtil.getStage()>=16 && !questUtil.getEndMissions()){
                options.addOption("Approach the command core", OptionId.INITIALA);
            } else{
                options.addOption("Approach the command core", OptionId.INITIALB);
            }
        } else if (questUtil.getStage()>=16 && !questUtil.getEndMissions()) {
            arrived = true;
            //layered elif??
            if (!recovered) {
                text.addPara("A strange cluster of computer cores are left among the wreckage, further inspection should yield results. Your bridge crew is preparing post battle operations.");
                text.addPara("For a moment the bridge seems to quiets down, and you notice a quiet hum - a distant instrument. The harder you try to focus on it to more distant it becomes.");
                text.addPara("You're then suddenly brought out of your thought by the ops chief asking you what to do next.");

                options.addOption("Begin salvage operations", OptionId.A1);
            } else {
                text.addPara("The core wreckage is now entirely picked clean, only worthless pieces of space debris remain. Yet you still hear the hum.");

            }
            dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
            options.addOption("Leave", OptionId.LEAVE);
        } else {
            arrived = true;
            text.addPara("A strange cluster of computer cores are left among the wreckage.");

            dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
            options.addOption("Leave", OptionId.LEAVE);
        }

        //visual
        if (arrived){
            visual.showImageVisual(dialog.getInteractionTarget().getCustomInteractionDialogImageVisual());
        }
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

        //initial
        if (optionData == OptionId.INITIALA) {
            options.clearOptions();
            arrived = true;
            text.addPara("A strange cluster of computer cores are left among the wreckage, further inspection should yield results. Your bridge crew is preparing post battle operations.");
            text.addPara("For a moment the bridge seems to quiets down, and you notice a quiet hum - a distant instrument. The harder you try to focus on it to more distant it becomes.");
            text.addPara("You're then suddenly brought out of your thought by the ops chief asking you what to do next.");

            options.addOption("Begin salvage operations", OptionId.A1);
            options.addOption("Leave", OptionId.LEAVE);
            dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
        }
        if (optionData == OptionId.INITIALB) {
            options.clearOptions();
            arrived = true;
            text.addPara("A strange cluster of computer cores are left among the wreckage.");

            options.addOption("Leave", OptionId.LEAVE);
            dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
        }
        //a1
        if (optionData == OptionId.A1) {
            //unset ESC
            dialog.setOptionOnEscape("", null);
            options.clearOptions();
            text.addPara("As the crew picks apart the cores, it becomes clear that almost all the data in them is lost. Instead of being garbled, or otherwise corrupted by radiation the cores are just empty. Like they have been reformatted by someone, there is almost no data left in the whole system.");
            text.addPara("There's no sign of casualties or life pods in the wreckage, yet the prototype hulls were clearly meant to be crewed. For a moment it seems pointless to even try and uncover what happened here.");
            text.addPara("Until, the crew stumbles upon a console in one of the derelict wrecks. In it they find a maintenance log, which has been used by an anonymous and disgruntled maintenance worker to journal their frustrations.");
            text.addPara("\"I'm sending a transcript of the maintenance logs to your fleet log.\" Your comms officer reports.");

            options.addOption("Continue", OptionId.A2);
        }

        //finish
        if (optionData == OptionId.A2) {
            options.clearOptions();
            text.addPara("Your ops chief reports that the salvor team managed to find the Unlimited Production Chip, and some other valuable salvage.");

            text.setFontSmallInsignia();
            //acquire text
            int amount = mathUtil.getSeededRandomNumberInRange(50,100, getRandom());
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            text.addPara("Acquired the Unlimited Production Chip",g,h,"the Unlimited Production Chip","");
            text.addPara("Acquired 1 Alpha Core",g,gr,"1 Alpha Core","");
            text.addPara("Acquired "+amount+" units of Artifact Electronics",g,gr,amount+" units of Artifact Electronics","");
            //add to cargo
            cargo.addItems(CargoAPI.CargoItemType.RESOURCES, "alpha_core", 1);
            cargo.addItems(CargoAPI.CargoItemType.RESOURCES, "nskr_electronics", amount);

            text.setFontInsignia();

            Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

            questUtil.setCompleted(true, RECOVERED_KEY);
            questUtil.setStage(19);

            //make return target important
            if (questUtil.getCompleted(nskr_elizaDialog.AGREED_TO_HELP_KEY)){
                questUtil.getElizaLoc().getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MISSION_IMPORTANT,true);
            } else {
                questUtil.asteriaOrOutpost().getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MISSION_IMPORTANT,true);
            }

            //remove important
            dialog.getInteractionTarget().getMemory().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);

            dialog.setOptionOnEscape("Leave", OptionId.LEAVE);

            options.addOption("Leave", OptionId.LEAVE);
        }

        //visual
        if (arrived){
            visual.showImageVisual(dialog.getInteractionTarget().getCustomInteractionDialogImageVisual());
        }

        //leave
        if (optionData == OptionId.LEAVE) {
            options.clearOptions();
            dialog.dismiss();
        }
    }

    public enum OptionId {
        A1,
        A2,
        A3,
        A4,
        A5,
        INITIALA,
        INITIALB,
        LEAVE,
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
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