package scripts.kissa.LOST_SECTOR.campaign.quests;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.graid.nskr_elizaRaidObjectiveCreator;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.util.util;
import scripts.kissa.LOST_SECTOR.world.nskr_gen;

import java.awt.*;
import java.util.Map;
import java.util.Random;

public class nskr_elizaDialog implements InteractionDialogPlugin {

    public static final String DIALOG_FINISHED_KEY = "nskr_elizaDialogKeyFinished";
    public static final String ELIZA_FIGHT_KEY = "nskr_elizaDialogKeyFight";
    public static final String AGREED_TO_HELP_KEY = "nskr_elizaDialogKeyAgreeToHelp";
    public static final String ELIZA_HELP_KEY = "nskr_elizaDialogKeyHelp";
    public static final String ELIZA_RAID_KEY = "nskr_elizaDialogKeyRaid";
    public static final String PERSISTENT_KEY = "nskr_elizaDialogKey";
    public static final String PERSISTENT_RANDOM_KEY = "nskr_elizaDialogKeyRandom";

    private InteractionDialogAPI dialog;
    private TextPanelAPI text;
    private OptionPanelAPI options;
    private VisualPanelAPI visual;
    private PersonAPI eliza = null;
    private boolean arrived = false;
    private boolean stand = false;

    static void log(final String message) {
        Global.getLogger(nskr_artifactDialog.class).info(message);
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;

        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();
        visual = dialog.getVisualPanel();

        dialog.setOptionOnEscape("Leave", OptionId.LEAVE2);

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
        //intro
        text.addPara("The port authority denies your docking access. You receive a message from Eliza, a request join her in a meeting. On the condition that you come alone, no guards or other crew of your own.");
        text.addPara("Seems like there's no clear way around this \"meeting\".");

        options.addOption("Agree to meet her", OptionId.INITIAL);
        options.addOption("Leave", OptionId.LEAVE2);
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
        PersonAPI player = Global.getSector().getPlayerPerson();

        //intial
        if (optionData == OptionId.INITIAL) {
            //unset ESC
            dialog.setOptionOnEscape("", null);
            //gen Eliza
            if (eliza==null){
                eliza = nskr_gen.genEliza();
            }
            options.clearOptions();
            text.addPara("The marine captain, with the help of the black ops team hatch a contingency plan in case things go wrong with Eliza.");
            text.addPara("\"There's going to be a delay of course, and it's going to get ugly - fast.\" The captain says. There is a sour expression, they are not happy about this. " +
                    "\"Hopefully it wont come down to this.\"");

            options.addOption("Continue", OptionId.A1);
        }
        //A1
        if (optionData == OptionId.A1) {
            options.clearOptions();
            text.addPara("You agree to the offer, and shortly after the port authority gives the green light to land at a specific dock.");
            text.addPara("You are then escorted by group of rough looking bodyguards, who of course collect your sidearm for safekeeping. One of them has a fearsome scar on their left arm, wrapping all the way from the elbow to the shoulder. " +
                    "It's accentuated by many tattoos surrounding it, in an intricate pattern of abstract symbols and shapes - you wonder what causes such damage without killing them outright. The guards take you through a beaten down section of the port, presumably to the meeting.");
            text.addPara("After a short walk you arrive in a section that seems to have been recently refurbished. It's unusually modern looking, but still maintains a makeshift look.");
            text.addPara("The old and new clash in a patchwork of exposed pipe-works, rusted out surfaces, freshly renovated walls and brand new light fixtures.");

            options.addOption("Continue", OptionId.A2);
        }
        //a2 arrived
        if (optionData == OptionId.A2) {
            arrived = true;
            options.clearOptions();
            text.addPara("The guards point you towards what looks like a small office. And immediately you spot Eliza in the room. " +
                    "She's wearing a flashy and ornate looking uniform - dark leather belts loop around and hang off of the blood red uniform, underlined by shiny gold pins and decorations - a fit for a warlord.");
            text.addPara("\"Welcome to my humble port captain.\" Eliza greets you from behind a fancy looking desk. \"Feel free to take a seat, we have a lot to discuss.\"");

            options.addOption("Take a seat", OptionId.A3);
            options.addOption("Keep standing", OptionId.A4);
        }
        //a3 a4 introduction
        if (optionData == OptionId.A3 || optionData == OptionId.A4) {
            options.clearOptions();
            if (optionData == OptionId.A4) stand = true;
            text.addPara("\"As you may know I have personally gotten quite invested in this cache mystery. I managed to get my hands on two disks so far, the data recovered from them is fascinating. The plans they had...\"");
            text.addPara("She sets down a handcrafted notebook down on the desk as she speaks.");
            text.addPara("Her tone turns inquisitive. \"I see you're also working on the same subject. Did they even tell you what you were *really* looking for in there?\"");

            options.addOption("\"Some chip I think.\"", OptionId.A5);
            options.addOption("\"Not really.\"", OptionId.A6);
            options.addOption("\"I'm not telling you.\"", OptionId.A12);
        }
        //a5 a6 a12 introduction pt2
        if (optionData == OptionId.A5 || optionData == OptionId.A6 || optionData == OptionId.A12) {
            options.clearOptions();
            if (optionData == OptionId.A5) text.addPara("\"It's not just some chip! It's the key to the old Domain's greatest technologies.\"");
            if (optionData == OptionId.A12) text.addPara("\"Would you stop being so hostile captain, I'm showing such hospitality to you. Try to just focus on the business at hand, anyways...\"");
            text.addPara("At the moment of the collapse the Cache was hosting the Unlimited Production Chip. It's the keys to construct the greatest ships ever known to mankind.\" Eliza is starting to get quite worked up.");
            text.addPara("\"What do you live for captain? To wake up at exact same minute every morning? To wear the exact same uniform every day? To only call people by Sir and Ma'am? To do exactly as your told? Even when your being slowly pushed to your death?\" She looks more intent. " +
                    "\"When I saw so many of my peers slowly lose every part their selves to the bureaucracy machine - I swore I would never become one of them.\" She sighs. " +
                    "\"It's a broken system captain - the Domain that is, are we really doomed to repeat the same cycle over and over again?\" She stops for a moment.");
            text.addPara("\"The collapse gave such a beautiful opportunity for humanity to be born a new. The Hegemony is fatally gripped by the longing for an old world - Everything will be exactly the same it was no exceptions, leading to the same failures all over again. " +
                    "There's still a chance for us to learn from the past, all it takes is a second collapse.\" She lets out a devilish little smile. \"That's where the Unlimited Production Chip comes in.\"");
            text.addPara("\"Now "+player.getName().getFullName()+", who do you think deserves to wield such power?\"");
            text.addPara("She leans back on her chair while waiting for your response, the chair made from exquisite dark leather towers over her.");

            options.addOption("\"I'm sure the Kesteven Corporation are a responsible organization.\"", OptionId.A7);
            options.addOption("\"Well, not you that's for sure.\"", OptionId.A8);
            options.addOption("\"I'm not sure.\"", OptionId.D1);
            options.addOption("\"You\"", OptionId.E1);
            options.addOption("\"And what would you do with this power?\"", OptionId.G1);
            options.addOption("\"Me\"", OptionId.B1);
        }
        //d1 neutral answer
        if (optionData == OptionId.D1) {
            options.clearOptions();
            text.addPara("\"Such indecisiveness is not characteristic for a great captain. Come on spit it out, just say what you *really* think.\" She leans forward and gives you an intense look.");
            text.addPara("She's not letting you get away with the neutral option.",g,h,"","");

            options.addOption("\"The Kesteven Corporation will bring a new era of prosperity.\"", OptionId.A7);
            options.addOption("\"Well, not a terrorist like you that's for sure.\"", OptionId.A8);
            options.addOption("\"You, the great anarchist leader.\"", OptionId.E1);
            options.addOption("\"So, what would you do with this power?\"", OptionId.G1);
            options.addOption("\"Me, the greatest captain the sector's ever seen.\"", OptionId.B1);
            options.addOption("\"I don't get involved in politics.\"", OptionId.D2);
        }
        //d2 d4 neutral answer pt2
        if (optionData == OptionId.D2 || optionData == OptionId.D4) {
            options.clearOptions();
            text.addPara("\"Damn, they really did brainwash you into the perfect killing machine huh. No strong emotions on any subject whatsoever. I'm sure you don't even flinch when you glass a colony with the flick of a switch.\" She seems to be perplexed by you.");
            if(optionData != OptionId.D4)text.addPara("This conversation is going nowhere, maybe you should feign interest.",g,h,"","");

            if(optionData != OptionId.D4)options.addOption("\"No, it's not like that at all.\"", OptionId.A8);
            options.addOption("Remain silent", OptionId.A13);
            options.addOption("\"I'm not getting into an argument with some terrorist.\"", OptionId.A9);
        }
        //a7 a8 dislike answer
        if (optionData == OptionId.A7 || optionData == OptionId.A8) {
            options.clearOptions();
            text.addPara("\"Another victim of corporate propaganda I see. Ready to die for the \"greater good\" I'm sure.\"");
            text.addPara("\"Ready to toil away and not see a fraction of the profits you create for your Kesteven higher-ups. Did you really think you were going to wield even 1% of the power this chip would create?\" She pauses for a moment, probably to cook up another political rant.");

            options.addOption("\"I'm not listening to a political rant from a wanted terrorist.\"", OptionId.A9);
            if(optionData != OptionId.A8) options.addOption("Say nothing", OptionId.D4);
            options.addOption("\"Hmm, maybe you're right.\"", OptionId.B1);
        }
        //a9 a13 disagree to help
        if (optionData == OptionId.A9 || optionData == OptionId.A13) {
            options.clearOptions();
            if (optionData == OptionId.A9) text.addPara("\"Is that what they call it now. \"When tyranny becomes law, resistance becomes duty.\" You ever hear of that captain?\"");
            if (optionData == OptionId.A9) text.addPara("\"Laws must exist to keep the people subservient, and those who show signs of disobedience will be punished.\"");
            text.addPara("\"You really are too far gone. This can't work.\" She turns on the chair to face perpendicular to you, as she tries to calm down.");
            text.addPara("You're too far gone? No, she's clearly out of her mind. You're making the right call.",g,h,"","");

            options.addOption("Continue", OptionId.A10);
        }
        //g1 criticise her
        if (optionData == OptionId.G1) {
            options.clearOptions();
            text.addPara("\"It will grant us the power to free this sector of the old world blues captain.\" Her eyes are lit up.");
            text.addPara("\"It's time for the self fulfilling prophecy of this faux-Domain to come true.\"");

            options.addOption("\"And how is this supposed to be a good thing?\"", OptionId.G2);
            options.addOption("\"I think I understand.\"", OptionId.G5);
        }
        //g2 criticise her
        if (optionData == OptionId.G2) {
            options.clearOptions();
            text.addPara("\"Because I'm the one willing to take action, the bureaucracy machine has overtaken every human action - Everything is cold and calculated to perfection. The people need to be freed from the inevitable total control of the Hegemony, by force if necessary.\" She's at the edge of her chair.");

            options.addOption("\"Even more violence solves nothing now.\"", OptionId.G3);
            options.addOption("\"I think I understand.\"", OptionId.G5);
        }
        //g3 criticise her
        if (optionData == OptionId.G3) {
            options.clearOptions();
            text.addPara("Her face quickly turns red. \"Sacrifices have to be made in the face of progress! " +
                    "Life isn't black and white, sometimes you have to get your hands dirty captain to make a change. For there are not many other options left!\"");

            options.addOption("\"You really are no different.\"", OptionId.G6);
            options.addOption("\"I don't work with terrorists.\"", OptionId.G4);
        }
        //g4 g6 disagree to help
        if (optionData == OptionId.G4 || optionData == OptionId.G6) {
            options.clearOptions();
            text.addPara("\"No captain, this isn't how this works. You don't just barge in, and start accusing me like some hound from COMSEC!\" She turns on the chair to face slightly away from you, as she tries to calm down.");
            text.addPara("She's clearly out of her mind. You're making the right call.",g,h,"","");

            options.addOption("Continue", OptionId.A10);
        }
        //a10 fight to death
        if (optionData == OptionId.A10) {
            options.clearOptions();
            if (stand)text.addPara("For the love of ludd she just keeps going. Your legs are starting to hurt a little, you really should have taken that seat.",g,h,"","");
            text.addPara("\"Now get the hell out of here captain. I'm not just gonna stab you in the back on your way out like it would be customary for you corporate bastards.\"");
            text.addPara("\"But I'll be getting those disks from you soon enough, one way or another.\" Thankfully she finishes.");
            text.addPara("She points towards the door, grabs the notebook on the desk and turns to face away from you. \"Now get out, while you still can!\" The large chair blocks your view of her.");

            questUtil.setCompleted(true, DIALOG_FINISHED_KEY);
            questUtil.setCompleted(true, ELIZA_RAID_KEY);

            //adds the raid itself
            ListenerManagerAPI listeners = Global.getSector().getListenerManager();
            if (!listeners.hasListenerOfClass(nskr_elizaRaidObjectiveCreator.class)) {
                listeners.addListener(new nskr_elizaRaidObjectiveCreator(), false);
            }

            //add eliza to market
            dialog.getInteractionTarget().getMarket().getCommDirectory().addPerson(eliza,1);
            dialog.getInteractionTarget().getMarket().addPerson(eliza);

            dialog.setOptionOnEscape("Leave", OptionId.LEAVE1);
            options.addOption("Leave", OptionId.LEAVE1);
        }
        //b1 e1 g5 like answer
        if (optionData == OptionId.B1 || optionData == OptionId.E1 || optionData == OptionId.G5) {
            options.clearOptions();
            text.addPara("\"Yes, you get it captain. This sector has suffered for far too long under corporate greed and the self appointed Hegemony. The grip of authority grows stronger every passing moment.\"");
            text.addPara("\"We will make things right, with this chip it will not only be achieved, it will be inevitable.\" She's warming up to you as you play along.");

            options.addOption("\"I'm not working with a terrorist.\"", OptionId.A9);
            options.addOption("\"Yes we will make things right, together.\" (lie)", OptionId.B5);
            options.addOption("\"You really are right.\"", OptionId.B2);
        }
        //b2 b5 agree to help
        if (optionData == OptionId.B2 || optionData == OptionId.B5) {
            options.clearOptions();
            text.addPara("\"I will hand over the disks to you. You will find the Cache with the help of those Kesteven goons. And then you will bring back the Unlimited Production Chip to us.\"");
            text.addPara("\"With this power the sector will be free. No masters - no rules, right captain?\" She waits for your response, even though she already knows the answer.");
            if (optionData == OptionId.B2)text.addPara("Be careful of what you're getting yourself into, now.",g,h,"","");
            if (optionData == OptionId.B5)text.addPara("Just tell her what she wants to hear.",g,h,"","");

            options.addOption("Agree (lie)", OptionId.B4);
            options.addOption("Agree", OptionId.B3);
        }
        //b3 b4 agree to help pt2
        if (optionData == OptionId.B3 || optionData == OptionId.B4) {
            options.clearOptions();
            if (optionData == OptionId.B3){
                questUtil.setCompleted(true, AGREED_TO_HELP_KEY);
                text.addPara("Wait, you really agree with her? Oh dear.",g,h,"","");
            }
            text.addPara("\"I'll be monitoring your progress from here captain. Good luck.\" She finishes with a sly smile.");
            if (optionData == OptionId.B4)text.addPara("Yes, make her do all the work for you.",g,h,"","");
            text.addPara("As you leave, Eliza's crew helps load the two disks into your cargo shuttle.");

            questUtil.setCompleted(true, DIALOG_FINISHED_KEY);
            questUtil.setCompleted(true, ELIZA_HELP_KEY);
            questUtil.setDisksRecovered(questUtil.getDisksRecovered()+2);

            //remove important
            if (dialog.getInteractionTarget().getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_MISSION_IMPORTANT)){
                dialog.getInteractionTarget().getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
            }
            text.setFontSmallInsignia();
            //acquire text
            text.addPara("Acquired Data Disk #2",g,h,"Data Disk #2","");
            text.addPara("Acquired Data Disk #1",g,h,"Data Disk #1","");
            text.setFontInsignia();

            Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

            //add eliza to market
            dialog.getInteractionTarget().getMarket().getCommDirectory().addPerson(eliza,1);
            dialog.getInteractionTarget().getMarket().addPerson(eliza);

            dialog.setOptionOnEscape("Leave", OptionId.LEAVE2);
            options.addOption("Leave", OptionId.LEAVE2);
        }

        if (arrived) dialog.getVisualPanel().showPersonInfo(eliza, false);

        //leave with text
        if (optionData == OptionId.LEAVE1){
            options.clearOptions();
            text.addPara("It's best to listen to her and leave.",g,h,"","");
            text.addPara("The guard with the scar shoves your sidearm back to your arms and rushes you back to port.");

            dialog.getVisualPanel().fadeVisualOut();

            options.addOption("Continue", OptionId.LEAVE2);
        }
        //leave for real
        if (optionData == OptionId.LEAVE2) {
            options.clearOptions();
            dialog.dismiss();
            dialog.getVisualPanel().fadeVisualOut();
        }
    }

    public enum OptionId {
        A1,
        A2,
        A3,
        A4,
        A5,
        A6,
        A7,
        A8,
        A9,
        A10,
        A11,
        A12,
        A13,
        B1,
        B2,
        B3,
        B4,
        B5,
        D1,
        D2,
        D3,
        D4,
        E1,
        E2,
        G1,
        G2,
        G3,
        G4,
        G5,
        G6,
        INITIAL,
        LEAVE1,
        LEAVE2,
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