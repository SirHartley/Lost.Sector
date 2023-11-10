package scripts.kissa.LOST_SECTOR.campaign.quests;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.nskr_kestevenQuest;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.Map;
import java.util.Random;

public class nskr_cacheDoubtDialog implements InteractionDialogPlugin {

    //

    public static final String PERSISTENT_KEY = "nskr_cacheDoubtDialogKey";
    public static final String PERSISTENT_RANDOM_KEY = "nskr_cacheDoubtDialogRandom";

    private InteractionDialogAPI dialog;
    private TextPanelAPI text;
    private OptionPanelAPI options;
    private VisualPanelAPI visual;
    private PersonAPI chief;
    private PersonAPI officer;
    private boolean ops = false;
    private boolean sensors = false;
    private boolean tip = false;

    static void log(final String message) {
        Global.getLogger(nskr_artifactDialog.class).info(message);
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
        //gen people
        FullName.Gender gender = FullName.Gender.MALE;
        if (random.nextFloat() > 0.5f) {
            gender = FullName.Gender.FEMALE;
        }
        chief = Global.getSector().getFaction(Factions.PLAYER).createRandomPerson(gender, random);
        chief.setPostId(Ranks.SPACE_CHIEF);
        gender = FullName.Gender.MALE;
        if (random.nextFloat() > 0.5f) {
            gender = FullName.Gender.FEMALE;
        }
        officer = Global.getSector().getFaction(Factions.PLAYER).createRandomPerson(gender, random);
        officer.setPostId(Ranks.POST_OFFICER);

        text.setFontInsignia();

        //options.addOption("Leave", OptionId.LEAVE);

        text.addPara("You've been here for a while, haven't you captain. Maybe it's time to do something.",g,h,"","");

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
            text.addPara("You are aimlessly drifting through the void of space. Light-years from the nearest star - a level of nothing you would struggle to comprehend.",g,h,"","");

            options.addOption("Continue", OptionId.INITIAL2);
            options.addOption("\"I'll figure it out.\"", OptionId.INITIAL2B);
            options.addOption("\"It's puzzling.\"", OptionId.INITIAL2C);
            options.addOption("\"Shut up, I know what I'm doing.\"", OptionId.M1);
        }
        //m1
        if (optionData == OptionId.M1) {
            text.addPara("Woah- calm down captain, I'm just trying to help you understand the gravity of the situation.",g);

            options.addOption("\"Okay.\"", OptionId.INITIAL2);
            options.addOption("\"What do you want me to do?\"", OptionId.M2);
            options.addOption("Dismiss thought", OptionId.LEAVE);

            dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
        }
        //m2
        if (optionData == OptionId.M2) {
            text.addPara("You should try talking to your crew, maybe they can come up with a plan.",g);

            options.addOption("\"Okay then.\"", OptionId.INITIAL2);
            options.addOption("Dismiss thought", OptionId.LEAVE);
        }

        //MAIN1
        if (optionData == OptionId.INITIAL2 || optionData == OptionId.INITIAL2B || optionData == OptionId.INITIAL2C) {
            text.addPara("You take note of the current situation and your options.");
            text.addPara("The sensors officer has reported nothing of interest. Your operations chief is struggling to come to any other conclusion than - it's empty.");

            options.addOption("Talk to the operations chief", OptionId.A1);
            options.addOption("Talk to the sensors officer", OptionId.B1);
            options.addOption("Dismiss thought", OptionId.LEAVE);

            dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
        }
        //a1 ops chief
        if (optionData == OptionId.A1) {
            //unset ESC
            dialog.setOptionOnEscape("", null);
            dialog.getVisualPanel().showPersonInfo(chief, true);
            ops = true;
            text.addPara("There's an open datafeed with countless open entries filled with scribbled notes, some crossed over, and all connected with criss-crossing lines.");
            text.addPara("\"I don't know captain.\" They seems a little defeated. \"It doesn't add up, the gravity well was right there. Yet all we find is empty space - " +
                    "of course the guys on the science team are busy theorising some one of a kind dark- matter/energy anomaly or whatever. " +
                    "But the center - the well, it has to be the answer...\"");

            options.addOption("Leave them be", OptionId.MAIN2);
            options.addOption("\"There must be something to it, I know it.\"", OptionId.A2);
            options.addOption("\"It's doomed isn't it.\"", OptionId.DOOMED);
        }
        //a2
        if (optionData == OptionId.A2){
            tip = true;
            text.addPara("\"Oh- you must be right captain. That center anomaly is at the core of this, it has to be answer - somehow.\" They start going through the data one more time.");

            options.addOption("Leave them be", OptionId.MATTER1);
            options.addOption("\"Interesting.\"", OptionId.MATTER2);
        }
        //b1 sensors officer
        if (optionData == OptionId.B1) {
            //unset ESC
            dialog.setOptionOnEscape("", null);
            dialog.getVisualPanel().showPersonInfo(officer, true);
            sensors = true;
            text.addPara("\"Captain, we just finished a checkup on all the sensor logs and guess what?\" They say with sarcastic enthusiasm. " +
                    "\"It's nothing - all empty, absolutely nothing - that's all we've seen since our arrival here, but I guess we'll have to keep watch. " +
                    "There must be something to this place, surely it can't just be empty.\"");

            options.addOption("Leave them be", OptionId.MAIN2);
            options.addOption("Ask them to check again, with extra care", OptionId.B2);
        }
        //b2
        if (optionData == OptionId.B2) {
            text.addPara("You tell them to check again, this time being extra careful of any minute signals that could get lost in the cosmic noise. And after a moderate wait they have a new report ready.");
            text.addPara("They seem annoyed. \"Sorry captain, we crunched all the numbers again - the result is still nothing. There's nothing I can do about that.\" They shrug.");

            options.addOption("Leave them be", OptionId.MAIN2);
        }
        //MAIN2
        if (optionData == OptionId.MAIN2 || optionData == OptionId.MATTER1 || optionData == OptionId.MATTER2 || optionData == OptionId.DOOMED) {
            dialog.getVisualPanel().fadeVisualOut();
            if (optionData == OptionId.MAIN2 && sensors) text.addPara("The situation does not look bright. Not all hope is lost though.",g,h,"","");
            if (optionData == OptionId.DOOMED) text.addPara("This is not good, maybe you should- just give up.",g,h,"","");
            if (optionData == OptionId.MATTER2 || optionData == OptionId.MATTER1) text.addPara("Dark matter anomaly at the center? Maybe you should investigate.",g,h,"","");
            if (optionData == OptionId.MAIN2 && !sensors) text.addPara("You contemplate the situation.",g,h,"","");

            //1st & 2nd
            if (!ops) options.addOption("Talk to the operations chief", OptionId.A1);
            if (!sensors) options.addOption("Talk to the sensors officer", OptionId.B1);
            //3rd time
            if (sensors && ops && optionData!=OptionId.DOOMED && !tip) {
                options.addOption("\"I just need to look harder.\"", OptionId.LEAVEB);
                options.addOption("\"I don't give up so easily.\"", OptionId.LEAVEC);
            }
            if (tip) options.addOption("\"Lets get to work.\"", OptionId.LEAVED);
            if (optionData==OptionId.DOOMED) options.addOption("\"I give up.\"", OptionId.M3);
            options.addOption("Dismiss thought", OptionId.LEAVE);

            dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
        }
        //give up
        //m3
        if (optionData == OptionId.M3) {
            text.addPara("Yes! Get out of here before it's too late. Whatever is supposed to be here is not worth dying for. Just fly back to the core - forget that this happened, and buy a nice remote farmstead on Mazalot.",g);

            options.addOption("\"Maybe it's not that bad.\"", OptionId.MAIN2);
            options.addOption("\"You're right.\"", OptionId.LEAVEB);
            options.addOption("Dismiss thought", OptionId.LEAVE);
        }

        //leave
        if (optionData == OptionId.LEAVE || optionData == OptionId.LEAVEB || optionData == OptionId.LEAVEC || optionData == OptionId.LEAVED) {
            dialog.dismiss();
        }
    }

    public enum OptionId {
        A1,
        A2,
        A3,
        A4,
        A5,
        B1,
        B2,
        B3,
        B4,
        B5,
        M1,
        M2,
        M3,
        M4,
        INITIAL,
        INITIAL2,
        INITIAL2B,
        INITIAL2C,
        MAIN2,
        MAIN2B,
        MAIN2C,
        LEAVE,
        LEAVEB,
        LEAVEC,
        LEAVED,
        MATTER1,
        MATTER2,
        DOOMED,
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
