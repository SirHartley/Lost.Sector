package scripts.kissa.LOST_SECTOR.campaign.quests;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.econ.nskr_upChip;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.nskr_kestevenQuest;
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.nskr_shipSwap;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.Map;
import java.util.Random;

public class nskr_EndingKestevenDialog implements InteractionDialogPlugin {
    //
    //
    //
    public static final String DIALOG_FINISHED_KEY = "nskr_KestevenEndingDialogKeyFinished";
    public static final String PERSISTENT_KEY = "nskr_KestevenEndingDialogKey";
    public static final String PERSISTENT_RANDOM_KEY = "nskr_KestevenEndingDialogKeyRandom";

    public static final float REWARD_POINTS = 450000f;

    private boolean arrived = false;
    private boolean aliceLeft = false;
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

        dialog.setOptionOnEscape("Leave", OptionId.LEAVE);

        dialog.getVisualPanel().showPlanetInfo(dialog.getInteractionTarget());
        if (dialog.getInteractionTarget().getCustomInteractionDialogImageVisual()!=null) {
            visual.showImageVisual(dialog.getInteractionTarget().getCustomInteractionDialogImageVisual());
        } else if (dialog.getInteractionTarget().getMarket().getPlanetEntity()!=null) visual.showPlanetInfo(dialog.getInteractionTarget().getMarket().getPlanetEntity());

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();

        text.setFontInsignia();

        //options.addOption("Leave", OptionId.LEAVE);

        int stage = questUtil.getStage();
        boolean killEliza = questUtil.getCompleted(questStageManager.KILLED_ELIZA_KEY);
        if (stage >= 19 && !questUtil.getCompleted(DIALOG_FINISHED_KEY)) {
            text.addPara("As you approach "+dialog.getInteractionTarget().getMarket().getName()+", you contemplate whether Kesteven should get the Unlimited Production Chip or not. It would certainly have serious consequences on your reputation and the wider sector too. This is the last chance to change your mind.");
            if(!killEliza)text.addPara("Particularly, this would upset both the Tri-Tachyon Corporation and Eliza.",g,h,"","");
            if(killEliza)text.addPara("Particularly this would upset the Tri-Tachyon Corporation.",g,h,"","");

            options.addOption("Continue", OptionId.A1);
            options.addOption("Leave", OptionId.LEAVE);
        }
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        Color h = Misc.getHighlightColor();
        Color b = Misc.getBasePlayerColor();
        Color bh = Misc.getBrightPlayerColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color s = Misc.getStoryBrightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();

        PersonAPI jack = util.getJack();
        PersonAPI alice = util.getAlice();

        text.addPara(optionText, b, h, "", "");

        //initial
        if (optionData == OptionId.A1) {
            //unset ESC
            dialog.setOptionOnEscape("", null);
            arrived = true;
            options.clearOptions();
            text.addPara("You land at the dock and are immediately greeted by Jack. \"I see you have the Chip captain. We have searched long for this, the secrets we are about to uncover are unprecedented.\" " +
                    "He gazes into the horizon, he's clearly contemplating the stakes at play.");
            text.addPara("Alice shortly shows up too \"I'm going to begin work on the Chip directly. As soon as we get it unloaded.\"");
            text.addPara("Jack continues \"We can discuss your share of the deal while Alice works on the Chip.\"");

            Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);
            //loss text
            text.setFontSmallInsignia();
            text.addPara("Lost the Unlimited Production Chip",g,h,"the Unlimited Production Chip","");
            text.setFontInsignia();

            options.addOption("Continue", OptionId.A2);
        }
        //a2
        if (optionData == OptionId.A2) {
            arrived = false;
            aliceLeft = true;
            options.clearOptions();
            text.addPara("There is a defiant look on Jack's face. \"As you know, this will have some consequences on the wider sector. Relations with Tri-Tachyon will tank, they wont take this lightly. " +
                    "This cold war of ours is about to go hot, but we will be ready.\" He regains some confidence.");
            text.addPara("\"I've authorized you to get a share of some of the equipment produced with the Unlimited Production Chip. You will not be disappointed, also there will be some new toys in the artifact exchange.\" He gives you a quick smirk.");

            options.addOption("Continue", OptionId.A3);
        }
        //a3
        if (optionData == OptionId.A3) {
            options.clearOptions();
            text.addPara("After some waiting around Alice gives the green light to pick up the equipment package.");
            text.addPara("\"Thank you for your service "+Global.getSector().getPlayerPerson().getName().getFullName()+". I hope we can again work together in the future, burn bright.\" He gives you an intense handshake as you try to leave.");

            questUtil.setCompleted(true, DIALOG_FINISHED_KEY);
            questUtil.setStage(20);
            questUtil.saveEnding();

            //rewards
            CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
            //BPs
            //playerCargo.addSpecial(new SpecialItemData("nskr_prot_wp", null), 1);
            playerCargo.addSpecial(new SpecialItemData("nskr_prot_light", null), 1);
            //credits
            playerCargo.getCredits().add(nskr_kestevenQuest.STAGE5_PAYOUT);
            //Exchange
            nskr_shipSwap.addPoints(REWARD_POINTS);
            //+rep
            Global.getSector().getFaction(Factions.PLAYER).adjustRelationship("kesteven",0.25f);
            jack.getRelToPlayer().adjustRelationship(0.20f, RepLevel.COOPERATIVE);
            alice.getRelToPlayer().adjustRelationship(0.20f, RepLevel.COOPERATIVE);
            //TT war
            float rep = mathUtil.getSeededRandomNumberInRange(-0.70f, -0.65f, getRandom());
            //completion text
            String payout = Misc.getDGSCredits(nskr_kestevenQuest.STAGE5_PAYOUT);
            text.setFontSmallInsignia();
            //add sp
            Global.getSector().getPlayerStats().setStoryPoints(Global.getSector().getPlayerStats().getStoryPoints()+1);
            text.setFontSmallInsignia();
            text.addPara("Gained 1 Story point",g,s,"1 Story point","");
            //acquire text
            text.addPara("Received +" + payout,g,h,"+"+payout,"");
            //text.addPara("Acquired Prototype Weapons blueprint chip",g,h,"Prototype Weapons","");
            text.addPara("Acquired Prototype Light Ships blueprint chip",g,h,"Prototype Light Ships","");
            //Exchange
            text.addPara("Acquired "+Misc.getWithDGS(REWARD_POINTS)+" exchange points",g,h,Misc.getWithDGS(REWARD_POINTS)+" exchange points","");
            //CONTACT lvl increase
            text.addPara("Increased contact level with Kesteven contacts",g,gr,"","");
            util.getJack().setImportance(PersonImportance.VERY_HIGH);
            util.getAlice().setImportance(PersonImportance.VERY_HIGH);

            text.addPara("Relationship with Kesteven improved by 25",g,gr,"25","");
            text.addPara("Relationship with Jack Lapua improved by 20",g,gr,"20","");
            text.addPara("Relationship with Alice Lumi improved by 20",g,gr,"20","");

            if(Global.getSector().getFaction(Factions.PLAYER).getRelationship(Factions.TRITACHYON)>rep) {
                Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.TRITACHYON, rep);
                float repText = Math.round(rep*100f);
                text.addPara("Relationship with Tri-Tachyon reduced to "+(int)repText,g,r,""+(int)repText,"");
            }
            if(Global.getSector().getFaction("kesteven").getRelationship(Factions.TRITACHYON)>rep) {
                Global.getSector().getFaction("kesteven").setRelationship(Factions.TRITACHYON, rep);
                text.addPara("Kesteven and Tri-Tachyon enter hostilities",g,r,"enter hostilities","");
            }

            if(util.getEliza()!=null) {
                util.getEliza().getRelToPlayer().adjustRelationship(-0.75f, RepLevel.VENGEFUL);
            }
            //remove important
            questUtil.asteriaOrOutpost().getMemory().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
            if (questUtil.getElizaLoc()!=null) questUtil.getElizaLoc().getMemory().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);

            //CONDITION
            dialog.getInteractionTarget().getMarket().addCondition(nskr_upChip.ID);

            text.setFontInsignia();

            Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

            dialog.setOptionOnEscape("Leave", OptionId.LEAVE);

            options.addOption("Leave", OptionId.LEAVE);
        }

        if(arrived){
            //dialog.getVisualPanel().showPersonInfo(eliza, false);
            dialog.getVisualPanel().showPersonInfo(jack, true);
            dialog.getVisualPanel().showSecondPerson(alice);
        }
        if(aliceLeft){
            dialog.getVisualPanel().showPersonInfo(jack, false);
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