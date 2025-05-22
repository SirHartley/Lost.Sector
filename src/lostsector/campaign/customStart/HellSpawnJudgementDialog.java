package lostsector.campaign.customStart;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.customStart.intel.HellSpawnEventIntel;
import lostsector.campaign.quests.util.SimpleFleet;
import lostsector.ModPlugin;
import lostsector.util.FleetUtil;
import lostsector.util.IdsLS;
import lostsector.util.MathUtilLS;
import lostsector.util.MiscLS;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class HellSpawnJudgementDialog implements InteractionDialogPlugin {

    //

    public static final String PERSISTENT_KEY = "hellSpawnJudgementWarningKey";
    public static final String JUDGEMENT_FLEET_KEY = "$hellSpawnJudgementFleet";

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
    private Color s;
    private boolean hell = false;

    static void log(final String message) {
        Global.getLogger(HellSpawnJudgementDialog.class).info(message);
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
        s = Misc.getStoryBrightColor();
        random = HellSpawnManager.getRandom();

        thrn = Global.getSector().getImportantPeople().getPerson(IdsLS.THRN_PERSON_ID);

        HellSpawnJudgementWarning.playMusic();

        visual.showPersonInfo(thrn);

        text.setFontInsignia();

        text.addPara("Hello captain.", g, h, "", "");
        text.addPara("The day has come.", g, h, "", "");

        options.addOption("Continue", OptionId.INITIAL);

    }

    @Override
    public void optionSelected(String optionText, Object optionData) {

        text.addPara(optionText, b, h, "", "");
        options.clearOptions();

        //initial
        if (optionData == OptionId.INITIAL) {
            text.addPara("There is no path.", g, HellSpawnEventIntel.BAR_COLOR, "", "");
            text.addPara("Beyond the scope of justice.", g, HellSpawnEventIntel.BAR_COLOR, "justice", "");

            HellSpawnEventIntel intel = HellSpawnEventIntel.get();
            if (intel.getProgress()< HellSpawnManager.PEACEFUL_MAX_POINTS) options.addOption("Continue", OptionId.P1);
            if (intel.getProgress()>= HellSpawnManager.PEACEFUL_MAX_POINTS && intel.getProgress()< HellSpawnManager.NEUTRAL_MAX_POINTS) options.addOption("Continue", OptionId.N1);
            if (intel.getProgress()>= HellSpawnManager.NEUTRAL_MAX_POINTS) options.addOption("Continue", OptionId.H1);
        }

        //peaceful
        if (optionData == OptionId.P1) {
            text.addPara("What?", g, h, "", "");
            text.addPara("...This is most unusual.", g, h, "", "");

            options.addOption("Continue", OptionId.P2);
        }
        if (optionData == OptionId.P2) {
            text.addPara("You tried your best to maintain peace.", g, h, "", "");
            text.addPara("Despite your calling.", g, h, "", "");

            options.addOption("Continue", OptionId.P3);
        }
        if (optionData == OptionId.P3) {
            text.addPara("Maybe there is hope for this sector after all...", g, h, "", "");

            options.addOption("Continue", OptionId.P4);
        }
        if (optionData == OptionId.P4) {
            text.addPara("Have this.", g, h, "", "");
            text.addPara("You have the power for change.", g, h, "", "");

            //add reward
            Global.getSector().getPlayerStats().setSkillLevel("HellSpawnPeacefulSkill", 1f);

            //add sp
            Global.getSector().getPlayerStats().setStoryPoints(Global.getSector().getPlayerStats().getStoryPoints() + 8);

            MiscLS.playUiStaticNoise();

            text.setFontSmallInsignia();
            //acquire text
            text.addPara("Gained 8 Story points", g, s, "8 Story points", "");
            text.addPara("Gained The Peaceful Heart", g, h, "The Peaceful Heart", "");

            PersonAPI fake = Global.getFactory().createPerson();
            fake.getStats().setSkillLevel("HellSpawnPeacefulSkill", 1f);

            text.beginTooltip().addSkillPanel(fake, 10f);
            text.addTooltip();

            text.setFontInsignia();

            text.addPara("Do not waste it.", g, h, "", "");

            options.addOption("Continue", OptionId.LEAVE);
        }

        //neutral
        if (optionData == OptionId.N1) {
            text.addPara("For everyone you've killed.", g, h, "", "");
            text.addPara("For every ship you've wrecked.", g, h, "", "");
            text.addPara("For every station you've burned.", g, h, "", "");

            options.addOption("Continue", OptionId.N2);
        }
        if (optionData == OptionId.N2) {
            text.addPara("What were you trying to achieve?", g, h, "", "");
            text.addPara("...It doesn't matter.", g, h, "", "");

            options.addOption("Continue", OptionId.N3);
        }
        if (optionData == OptionId.N3) {
            text.addPara("It's time to pick on someone your own size.", g, h, "", "");
            text.addPara("Die.", g, h, "", "");

            options.addOption("Continue", OptionId.FIGHT);
        }

        //hell
        if (optionData == OptionId.H1) {
            text.addPara("Oh yes.", g, h, "", "");
            text.addPara("Let the blood run as a river.", g, h, "", "");
            text.addPara("The sky filled with radioactive smog.", g, h, "", "");
            text.addPara("The sector firmly under your boot.", g, h, "", "");

            options.addOption("Continue", OptionId.H2);
        }
        if (optionData == OptionId.H2) {
            text.addPara("Your empire of ash.", g, h, "", "");
            text.addPara("WELCOME", HellSpawnEventIntel.BAR_COLOR, h, "", "");
            text.addPara("TO", HellSpawnEventIntel.BAR_COLOR, h, "", "");
            text.addPara("HELL", HellSpawnEventIntel.BAR_COLOR, h, "", "");

            hell = true;

            options.addOption("Continue", OptionId.FIGHT);
        }

        //FIGHT
        if (optionData == OptionId.FIGHT) {
            //text.addPara("", g, h, "", "");

            CampaignFleetAPI fleet = spawnJudgementFleet();

            visual.fadeVisualOut();

            HellSpawnJudgementFID plugin = new HellSpawnJudgementFID(fleet, dialog);
            dialog.setPlugin(plugin);

            plugin.showFleet();
        }

        //leave
        if (optionData == OptionId.LEAVE) {
            HellSpawnJudgementWarning.stopMusic();

            Global.getSoundPlayer().playCustomMusic(0, 3, "nskr_peace", false);

            dialog.dismiss();
        }
    }

    public enum OptionId {
        INITIAL,
        P1,
        P2,
        P3,
        P4,
        N1,
        N2,
        N3,
        N4,
        H1,
        H2,
        H3,
        H4,
        FIGHT,
        LEAVE
    }

    private IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);
    private int count = 0;
    @Override
    public void advance(float amount) {
        if (thrn!=null){
            count = HellSpawnJudgementWarning.animateTHRN(amount, interval, count);
        }


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

    private CampaignFleetAPI spawnJudgementFleet(){

        Random random = HellSpawnManager.getRandom();
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();

        float combatPoints = MathUtilLS.getSeededRandomNumberInRange(250f, 300f, random);

        if (hell) combatPoints*= 1.33f;

        //apply settings
        combatPoints *= ModPlugin.getScriptedFleetSizeMult();

        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOSTILE);
        keys.add(MemFlags.MEMORY_KEY_NO_REP_IMPACT);
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);
        keys.add(MemFlags.MEMORY_KEY_NEVER_AVOID_PLAYER_SLOWLY);
        keys.add(JUDGEMENT_FLEET_KEY);

        SimpleFleet simpleFleet = new SimpleFleet(pf.getContainingLocation().createToken(pf.getLocation()), IdsLS.AI_ALL_FACTION_ID, combatPoints, keys, random);
        simpleFleet.aiFleetProperties = true;
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.name = "Final Judgement";
        simpleFleet.noFactionInName = true;
        simpleFleet.assignment = FleetAssignment.INTERCEPT;
        simpleFleet.interceptPlayer = true;
        simpleFleet.assignmentText = "Hunting";
        CampaignFleetAPI fleet = simpleFleet.create();

        fleet.setContainingLocation(pf.getContainingLocation());
        Vector2f point = new Vector2f(MathUtils.getPointOnCircumference(pf.getLocation(),pf.getRadius() + fleet.getRadius(), random.nextFloat()*360f));

        fleet.setLocation(point.x, point.y);

        FleetUtil.setAIOfficers(fleet);

        FleetUtil.update(fleet, random);

        return fleet;
    }
}