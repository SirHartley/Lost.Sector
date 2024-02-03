package scripts.kissa.LOST_SECTOR.campaign.quests.characters;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;

import java.awt.*;
import java.util.Map;
import java.util.Random;

public class killBrainStartDialog implements InteractionDialogPlugin {

    //

    public static final String PERSISTENT_KEY = "killBrainStartDialogKey";

    private InteractionDialogAPI dialog;
    private TextPanelAPI text;
    private OptionPanelAPI options;
    private VisualPanelAPI visual;
    private PersonAPI officer;
    private Random random;
    private Color tc;
    private Color r;
    private Color gr;
    private Color g;
    private Color h;
    private Color b;

    static void log(final String message) {
        Global.getLogger(killBrainStartDialog.class).info(message);
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;

        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();
        visual = dialog.getVisualPanel();

        h = Misc.getHighlightColor();
        g = Misc.getGrayColor();
        gr = Misc.getPositiveHighlightColor();
        r = Misc.getNegativeHighlightColor();
        tc = Misc.getTextColor();
        b = Misc.getBasePlayerColor();
        random = questUtil.getRandom(killBrainManager.PERSISTENT_RANDOM_KEY);

        //gen people
        FullName.Gender gender = FullName.Gender.MALE;
        if (random.nextFloat() > 0.5f) {
            gender = FullName.Gender.FEMALE;
        }
        officer = Global.getSector().getFaction(Factions.PLAYER).createRandomPerson(gender, random);
        officer.setPostId(Ranks.POST_OFFICER);

        text.setFontInsignia();

        text.addPara("", g, h, "", "");

        options.addOption("Continue", OptionId.INITIAL);

    }

    @Override
    public void optionSelected(String optionText, Object optionData) {

        text.addPara(optionText, b, h, "", "");
        options.clearOptions();

        //initial
        if (optionData == OptionId.INITIAL) {
            text.addPara("", g, h, "", "");

            visual.showPersonInfo(officer);

            options.addOption("Continue", OptionId.INITIAL2);
        }
        if (optionData == OptionId.INITIAL2) {
            text.addPara("", g, h, "", "");


            options.addOption("Continue", OptionId.INITIAL2);
        }

        //leave
        if (optionData == OptionId.LEAVE) {
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
        INITIAL,
        INITIAL2,
        INITIAL2B,
        INITIAL2C,
        MAIN2,
        MAIN2B,
        MAIN2C,
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

}

