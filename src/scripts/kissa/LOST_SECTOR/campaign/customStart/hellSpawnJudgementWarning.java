package scripts.kissa.LOST_SECTOR.campaign.customStart;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.customStart.intel.hellSpawnJudgementIntel;
import scripts.kissa.LOST_SECTOR.util.ids;

import java.awt.*;
import java.util.Map;
import java.util.Random;

public class hellSpawnJudgementWarning implements InteractionDialogPlugin {

    //

    public static final String PERSISTENT_KEY = "hellSpawnJudgementWarningKey";

    private PersonAPI thrn;
    private InteractionDialogAPI dialog;
    private TextPanelAPI text;
    private OptionPanelAPI options;
    private VisualPanelAPI visual;

    private Random random;
    private Color tc;
    private Color r;
    private Color gr;
    private Color g;
    private Color h;
    private Color b;

    static void log(final String message) {
        Global.getLogger(hellSpawnJudgementWarning.class).info(message);
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
        random = hellSpawnManager.getRandom();

        genTHRN();
        playMusic();

        visual.showPersonInfo(thrn);

        text.setFontInsignia();

        text.addPara("Hello captain.", g, h, "", "");
        text.addPara("Soon you will be judged.", g, h, "", "");

        options.addOption("Continue", OptionId.INITIAL);

    }

    @Override
    public void optionSelected(String optionText, Object optionData) {

        text.addPara(optionText, b, h, "", "");
        options.clearOptions();

        //initial
        if (optionData == OptionId.INITIAL) {
            text.addPara("The time for it is approaching rapidly.", g, h, "", "");
            text.addPara("Hope your actions were righteous.", g, h, "", "");
            text.addPara("Be ready.", g, h, "", "");

            options.addOption("Continue", OptionId.INITIAL2);
        }
        if (optionData == OptionId.INITIAL2) {
            text.addPara("Be.", g, h, "", "");
            text.addPara("Ready.", g, h, "", "");

            Global.getSector().getIntelManager().addIntel(new hellSpawnJudgementIntel(Global.getSector().getClock().getTimestamp()), false);

            options.addOption("Continue", OptionId.LEAVE);
        }


        //leave
        if (optionData == OptionId.LEAVE) {
            stopMusic();

            dialog.dismiss();
        }
    }

    public enum OptionId {
        INITIAL,
        INITIAL2,
        LEAVE
    }

    private IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);
    private int count = 0;
    @Override
    public void advance(float amount) {
        if (thrn!=null){
            count = animateTHRN(amount, interval, count);
        }



    }

    public static int animateTHRN(float amount, IntervalUtil interval, int count){

        interval.advance(amount);
        if (interval.intervalElapsed()){
            PersonAPI thrn = Global.getSector().getImportantPeople().getPerson(ids.THRN_PERSON_ID);
            if (Math.random()<0.10f) thrn.getName().setFirst("THRON");
            else if (Math.random()<0.10f) thrn.getName().setFirst("THRNE");
            else if (Math.random()<0.10f) thrn.getName().setFirst("DIE");
            else thrn.getName().setFirst("THRN");

            thrn.setPortraitSprite("graphics/portraits/nskr_thrn0"+count+".png");

            count++;
        }
        if (count>=4) count = 0;
        return count;
    }

    public static void stopMusic() {
        if (Global.getSoundPlayer().getCurrentMusicId().equals("THRN.ogg")){
            Global.getSoundPlayer().pauseCustomMusic();
        }
    }

    public static void playMusic() {
        Global.getSoundPlayer().playCustomMusic(0, 12, "nskr_thrn_theme", true);

    }

    private void genTHRN(){

        PersonAPI thrn = Global.getFactory().createPerson();
        thrn.setAICoreId(Commodities.OMEGA_CORE);
        thrn.setFaction(Factions.NEUTRAL);
        thrn.setGender(FullName.Gender.MALE);
        thrn.setImportance(PersonImportance.VERY_HIGH);
        thrn.setPostId(Ranks.POST_UNKNOWN);
        thrn.setRankId(Ranks.UNKNOWN);
        thrn.getName().setFirst("THRN");
        thrn.getName().setLast("");
        thrn.setPortraitSprite("graphics/portraits/nskr_thrn00.png");
        thrn.setId(ids.THRN_PERSON_ID);


        this.thrn = thrn;

        Global.getSector().getImportantPeople().addPerson(thrn);

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