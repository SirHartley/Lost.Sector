package scripts.kissa.LOST_SECTOR.campaign.quests;

import java.awt.*;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.InteractionDialogImageVisual;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.util.util;

public class nskr_kQuest5Bar extends BaseBarEvent {

    //job5 initial bar dialog

    public static final int ADVANCE_CREDITS = 150000;
    protected long seed;
    private boolean asteria = false;
    private boolean arrived = false;
    private boolean initial = false;
    private boolean chatting = false;
    private boolean disks = false;
    private boolean advance = false;
    private boolean c1 = false;
    private boolean c2 = false;
    private boolean c3 = false;
    private boolean c4 = false;

    public nskr_kQuest5Bar() {
        seed = Misc.random.nextLong();
        log("loaded");
    }

    static void log(final String message) {
        Global.getLogger(nskr_kQuest5Bar.class).info(message);
    }

    @Override
    public boolean isAlwaysShow() {
        return true;
    }
    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        if (questUtil.getStage()<=14) return false;
        if (market!=questUtil.asteriaOrOutpost()){
            return false;
        }
        return true;
    }
    @Override
    public boolean shouldRemoveEvent() {
        if (questUtil.getStage() >= 16){
            done = true;
            return true;
        }
        return false;
    }

    InteractionDialogImageVisual defaultImage = null;
    private boolean done = false;
    private Gender gender;
    private PersonAPI person;

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.addPromptAndOption(dialog, memoryMap);
        Color h = Misc.getHighlightColor();

        Random random = new Random(seed + dialog.getInteractionTarget().getMarket().getId().hashCode());
        gender = Gender.MALE;
        person = Global.getSector().getFaction("kesteven").createRandomPerson(gender, random);
        person.setPostId(Ranks.POST_GENERIC_MILITARY);
        person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "nskr_guard"));

        TextPanelAPI text = dialog.getTextPanel();
        text.addPara("You see the man Jack told you about. A guard is leaning against the wall waiting for you.");

        dialog.getOptionPanel().addOption("Give the signal to the guard to take you to the meeting", this);
        dialog.setOptionColor(this, h);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();

        this.dialog = dialog;

        asteria = util.getAsteria()!=null;
        initial = true;
        //grab the default image
        if (defaultImage==null) {
            defaultImage = dialog.getInteractionTarget().getCustomInteractionDialogImageVisual();
        }

        options.clearOptions();
        if (asteria){
            dialog.getVisualPanel().showLargePlanet(util.getAsteria());
        } else{
            dialog.getVisualPanel().showPersonInfo(person, true);
        }
        text.addPara("You give the signal, and are then escorted through the local transport system.");
        if (asteria)text.addPara("You watch as different parts of the underground mega-city go past your window. Massive Cooling towers going all the way to the surface, with labyrinthine collections of pipes covering them. " +
                "The imposing factories churn on, no matter the time. The cube like housing blocks not far away, towering over themselves one after another covered in graffiti and dirty gray concrete. " +
                "You then pass through a tunnel and a new view opens up. You see a brand new part of the city, countless crystalline skyscrapers fill the view reaching all the way to the roof of the cavern.");
        if (asteria)text.addPara("\"We've arrived captain.\" The guard mumbles as he takes you to the lobby of one of the nearby skyscrapers, and then further into the building.");
        //alternate dialog for outpost
        if (!asteria)text.addPara("\"We've arrived captain.\" The guard mumbles as he takes you to the lobby of one of the office modules, and then further into the location.");
        options.addOption("Continue",this);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();
        asteria = util.getAsteria()!=null;

        String name = Global.getSector().getPlayerPerson().getName().getFirst();
        PersonAPI jack = util.getJack();
        PersonAPI alice = util.getAlice();
        disks = nskr_artifactDialog.getRecoveredSatelliteCount()>0;
        boolean foundCache = questUtil.getCompleted(questStageManager.FOUND_CACHE_KEY);

        TextPanelAPI text = dialog.getTextPanel();
        options.clearOptions();

        //initial
        //a0
        if (optionData==this){
            arrived = true;
            initial = false;
            options.clearOptions();
            if (asteria)text.addPara("You have arrived in one of the top floors of the building. The carefully decorated room is filled with sculpted decorations and high-end furniture. " +
                    "The hard edges of polished stone surfaces contrast the round designer furniture, no doubt the carefully curated creation of some famous interior designer.");
            //alt
            if (!asteria)text.addPara("You have arrived in one open areas of the office module. The whole location appears quite makeshift, clearly a recent addition to the station.");
            text.addPara("You see Jack and Alice chatting in the distance. They are sipping on bright red drink from an ornate bottle placed on the table. As you walk over you get to take a look a both of them, now in person.");
            text.addPara("Jack is quite a tall figure, despite his size there is something inviting about his form. He gives you a big - and mostly genuine smile once he notices you approach.");
            text.addPara("Alice has a deadpan expression she only gives you quick glance as you approach. Her expression only changing when taking a swig of the mystery drink, quite dramatically so.");

            options.addOption("Continue",OptionId.A1);
        }
        //a1
        if (optionData==OptionId.A1){
            arrived = false;
            chatting = true;
            options.clearOptions();
            text.addPara("\"Welcome "+name+". Care to take a drink?\"");
            text.addPara("Jack says, as he lifts up the bottle motioning you to grab a glass.");
            text.addPara("Alice seems to be too busy fiddling with a datapad to pay any attention to you yet.");

            options.addOption("\"Yeah, sure.\"",OptionId.A2);
            options.addOption("\"No thanks.\"",OptionId.A3);
        }
        //yes drink 1
        if (optionData==OptionId.A2){
            options.clearOptions();
            text.addPara("Jack pours you a rather generous glass of the red liquid.");
            text.addPara("\"This is good stuff you know. Artisan liqueur made with *real* fruits, none of that synth crap they'll sell you in bars.\"");
            text.addPara("You calmly grab the glass and brace yourself.");

            options.addOption("Drink it",OptionId.A4);
        }
        //yes drink 2
        if (optionData==OptionId.A4){
            options.clearOptions();
            text.addPara("At first the drink tastes quite nice, an indescribable mix of citrus and fruit very sweet too, almost overbearingly so. " +
                    "But quickly you feel the burn - it clings to your throat and it's *really* bad, you struggle to finish the drink and your eyes visibly tear up.");
            text.addPara("\"Well, what do you think captain?\" Jack looks eager to hear your opinion.");
            text.addPara("Alice seems to be paying attention now, she knows what's up.");
            text.addPara("Both of them look at you, amused at your appearance. You are clearly in pain.");

            options.addOption("\"It's great...\"",OptionId.A5);
            options.addOption("\"Thanks, I hate it.\"",OptionId.A6);
        }
        //yes drink 3
        if (optionData==OptionId.A5 || optionData==OptionId.A6 ){
            options.clearOptions();
            text.addPara("\"That means it's some good stuff.\" Jack doesn't seem surprised by your reaction.");
            text.addPara("\"I think it's time we got to business.\" Alice says.");
            text.addPara("\"Yeah, let's get to work.\" Jack adds.");
            text.addPara("He grabs his datapad in preparation, and Alice quickly finishes the rest of her drink too.");

            options.addOption("Continue",OptionId.MAIN);
        }
        //no drink
        if (optionData==OptionId.A3){
            options.clearOptions();
            text.addPara("\"Shame, this is good stuff you know. Well let's just get straight to business then.\"");
            text.addPara("His face quickly turns serious as he grabs his datapad in preparation.");
            text.addPara("Alice suddenly wakes up, nods and quickly finishes her drink too.");

            options.addOption("Continue",OptionId.MAIN);
        }
        //main
        if (optionData==OptionId.MAIN){
            options.clearOptions();
            text.addPara("\"You know the reason we brought you here.\" Jack says. \"We are trying to recover something of great importance now. The old pre-collapse comm network holds the key.\" He keeps explaining, his tone emphasizes the importance of this subject.");
            text.addPara("\"We are trying to gather enough data disks to crack some of the old encryption. The part that's related to \"Project : Enigma\". Our estimation is that around 5 more disks should be enough.\" He finishes.");
            text.addPara("\"This will lead us straight to *the Cache*.\" Alice continues the speech. \"A location that keeps being referenced in these old project communications. It is said that a particular Chip is stored there, it's extremely valuable. Which explains the great effort that went into keeping it hidden.\" She explains in a dry professor-esque tone.");

            if (!foundCache)options.addOption("Keep listening",OptionId.B1);
            if (!foundCache)options.addOption("\"Project : Enigma?\"",OptionId.C1);
            if (!foundCache)options.addOption("\"Comms network?\"",OptionId.C2);
            if (!foundCache)options.addOption("\"Cache?\"",OptionId.C3);
            if (!foundCache)options.addOption("\"Wait, a Chip?\"",OptionId.C4);
            //found cache already check
            if (foundCache)options.addOption("\"I think I already found that place.\"",OptionId.D1);
        }
        //c1 question 1
        if (optionData==OptionId.C1){
            options.clearOptions();
            c1 = true;
            text.addPara("Alice is quick to answer your question. \"This whole Enigma AI incident, it's almost certainly related to an old pre-collapse Domain project of the same name. " +
                    "It would be too much of an coincidence, based on what we've learned from the decrypted communications.\"");

            options.addOption("Continue",OptionId.B1);
            if (!c2)options.addOption("\"Comms network?\"",OptionId.C2);
            if (!c3)options.addOption("\"Cache?\"",OptionId.C3);
            if (!c4)options.addOption("\"Wait, a Chip?\"",OptionId.C4);
        }
        //c2 question 2
        if (optionData==OptionId.C2){
            options.clearOptions();
            c2 = true;
            text.addPara("Alice clarifies on the subject. \"It's not the main hyperwave network. No, they used their own covert satellites to relay information. " +
                    "Most of them are lost by now, but we're hoping there's enough surviving ones to piece the information together\"");
            text.addPara("Jack shows you a low-res image of what appears to be a satellite on his datapad. \"Here's one we managed to recover.\"");

            options.addOption("Continue",OptionId.B1);
            if (!c1)options.addOption("\"Project : Enigma?\"",OptionId.C1);
            if (!c3)options.addOption("\"Cache?\"",OptionId.C3);
            if (!c4)options.addOption("\"Wait, a Chip?\"",OptionId.C4);
        }
        //c3 question 3
        if (optionData==OptionId.C3){
            options.clearOptions();
            c3 = true;
            text.addPara("Alice continues. \"It's a well hidden storage site, somewhere in this sector. We don't know that much more yet, but it was a site of high importance. " +
                    "We are eager to learn more from the disks though.\"");

            options.addOption("Continue",OptionId.B1);
            if (!c1)options.addOption("\"Project : Enigma?\"",OptionId.C1);
            if (!c2)options.addOption("\"Comms network?\"",OptionId.C2);
            if (!c4)options.addOption("\"Wait, a Chip?\"",OptionId.C4);
        }
        //c4 question 4
        if (optionData==OptionId.C4){
            options.clearOptions();
            c4 = true;
            text.addPara("Jack quickly interjects to respond to your question. \"Yes, you are looking for a Chip of sorts, but it's not important for you to know the exact specifications yet. " +
                    "You will be briefed on the subject later, when it's more relevant.\"");

            options.addOption("Continue",OptionId.B1);
            if (!c1)options.addOption("\"Project : Enigma?\"",OptionId.C1);
            if (!c2)options.addOption("\"Comms network?\"",OptionId.C2);
            if (!c3)options.addOption("\"Cache?\"",OptionId.C3);
        }
        //b1 main
        if (optionData==OptionId.B1){
            options.clearOptions();
            text.addPara("\"We need you "+name+" to start working on recovering enough disks, to help us find this cache. You're our best chance so far.\" Jack reiterates.");
            text.addPara("He is looking at you with a serious expression, this is clearly important to him.");

            options.addOption("\"Okay, got it.\"",OptionId.MAIN2);
            //question them
            options.addOption("\"I'm not so sure about this.\"",OptionId.Q1);
            //already recovered
            if(disks)options.addOption("\"I already have some of those disks.\"",OptionId.B4);
        }
        //q1 questioning
        if (optionData==OptionId.Q1){
            options.clearOptions();
            text.addPara("Jacks manners quickly change to that of a corporate salesman, he is ready to sell you this job.");
            text.addPara("\"You know captain, we've been laying the ground work for this operation for years, it's a unique opportunity - and the key to help us solve the oncoming Enigma crisis. " +
                    "The more selfish actors in this sector would only waste this opportunity for their own gain only.\" His hands help illustrate the grand story as he leans in closer to you.");
            text.addPara("\"Think about it, Asteria is one of the last great bastions of equality and democracy. We are very much in a unique position to drive progress in this sector. " +
                    "And as such you will get your fair share of the prize.\"");
            text.addPara("\"In fact, I've been authorized to grant you and advance of "+Misc.getDGSCredits(ADVANCE_CREDITS)+" for this job.\"",tc,h,Misc.getDGSCredits(ADVANCE_CREDITS),"");

            advance = true;

            options.addOption("Agree to do the job",OptionId.MAIN2);
            options.addOption("\"What kind of prizes are we talking about?\"",OptionId.Q2);
        }
        if (optionData==OptionId.Q2){
            options.clearOptions();
            text.addPara("He gives you a quick smirk. \"You will be more than well compensated for your work, if we pull this off. And it wont only be a big pile of credits, but some seriously unique tech.\"");
            text.addPara("Alice adds to the conversation. \"Of course we don't know exactly that is inside the Cache yet, but we do know that it was important enough for the Domain to hide it well.\"");

            options.addOption("Agree to do the job",OptionId.MAIN2);
        }
        //b5 already recovered
        if (optionData==OptionId.B4){
            options.clearOptions();
            text.addPara("Jack has a surprised look on his face. \"Well that makes our work a lot easier captain. If only everyone we work with was this competent, we'd already have the location of this cache tracked down.\"");
            text.addPara("Alice comments on the matter. \"It isn't enough to finish the decryption, but this should speed up our efforts considerably.\"");

            options.addOption("Agree to do the job",OptionId.MAIN2);
            options.addOption("\"I'm not so sure about this.\"",OptionId.Q1);
        }
        //MAIN2
        if (optionData==OptionId.MAIN2){
            options.clearOptions();
            if (advance){
                Global.getSector().getPlayerFleet().getCargo().getCredits().add(ADVANCE_CREDITS);
                String payout = Misc.getDGSCredits(ADVANCE_CREDITS);
                text.setFontSmallInsignia();
                text.addPara("Received +"+payout,g,h,"+"+payout,"");
                util.playUiStaticNoise();
                text.setFontInsignia();
            }
            text.addPara("Jack leans forward towards you to, he narrows his eyes, and speaks in a disapproving tone.");
            text.addPara("\"Oh, there is one more thing.\" He says. \"Our intelligence has been tracking down a certain character, that is known to have one of these disks. Eliza, last name unknown, first name definitely not her real name either.\"");
            text.addPara("\"She is a dangerous character, a wanted terrorist and a vicious criminal. She's hiding in one of those freeport pirate heavens trying to lay low, we just haven't been able to track down which one.\" Jack pauses to grab something.");
            text.addPara("He shows you a blurry archive photo of Eliza on his datapad. She has a menacing smile in the mugshot, as if to say \"I've already won\".");
            text.addPara("Alice joins in to warn you in a condescending tone. \"By the way captain, if you ever run the her in person. *do not* listen to her nonsense, it's all lies to poison the mind.\"");

            options.addOption("\"I'll keep that in mind.\"",OptionId.MAIN3);
            if (questUtil.getCompleted(questStageManager.E_MESSENGER_TALKED_ASK_ABOUT_KEY)) options.addOption("\"Ah yes, that \"LZ\" character.\"",OptionId.B7);
            options.addOption("\"What kind of terrorist are we talking about?\"",OptionId.B5);
            options.addOption("\"It's never just some regular lowlife...\"",OptionId.B6);
        }
        //b6 eliza extra info
        if (optionData==OptionId.B5 || optionData==OptionId.B6 || optionData==OptionId.B7){
            options.clearOptions();
            text.addPara("Alice continues with her heads-up. \"She's a political extremist, and has been keeping quiet since her last attack a few years back. That's why I warned you about her.\"");
            text.addPara("\"If you're wondering about how or why she would get one these disks, we don't know either. It has to be some kind of bargaining chip with us.\" Alice finishes with a look of disgust on her face.");

            options.addOption("Continue",OptionId.MAIN3);
        }
        //MAIN3
        if (optionData==OptionId.MAIN3){
            options.clearOptions();
            text.addPara("Jack seems to be pleased with the result of this meeting.");
            text.addPara("\"Anyways that's all "+name+", hope you're not left too confused about this situation. I think it's about time we wrap up.\"");
            text.addPara("Alice is already packing up her stuff as she speaks. \"Yes, I think we're done. You should contact us later, so we can fill you in on the more minor details and leads regarding this mission.\"");

            options.addOption("Leave",OptionId.LEAVE);
        }
        //d1 already found cache
        if (optionData==OptionId.D1){
            options.clearOptions();
            //mark as important
            for (SectorEntityToken entity : Global.getSector().getStarSystem("Unknown Site").getAllEntities()){
                if (entity.getId()==null) continue;
                if (entity.getId().equals("nskr_cache_core")){
                    entity.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MISSION_IMPORTANT, true);
                }
            }

            text.addPara("Jack has a look of genuine amazement on his face, while Alice looks more skeptical.");
            text.addPara("\"That's incredible. You will be saving us a lot of work, if you are speaking the truth.\"");
            text.addPara("Alice starts explaining in the same dry tone. \"I suppose we will have to tell you more about this Chip now. At the moment of the collapse the Cache was hosting the Unlimited Production Chip. The Chip contains the keys to the old Domain's great secrets, so many technologies in one neat package.\"");
            text.addPara("\"Now, you will enter this site, disable whatever security systems they have left, and recover this Unlimited Production Chip.\"");

            options.addOption("Continue",OptionId.D2);
        }
        //d1 already found cache pt2
        if (optionData==OptionId.D2){
            options.clearOptions();
            text.addPara("\"I hope you got all that captain.\" She says.");
            text.addPara("Jack chimes in. \"Don't worry, you don't need to understand why the Chip works. Just that it's very important, and the goal of your mission.\"");
            text.addPara("Jack seems to be pleased with the result of this meeting.");
            text.addPara("\"Since you have the Cache coordinates, you should get to work as soon as possible. We will eagerly wait for your return.\"");

            options.addOption("Leave",OptionId.LEAVE);
        }
        //leave
        if (optionData==OptionId.LEAVE){
            arrived = true;
            chatting = false;
            //acquire text
            text.setFontSmallInsignia();
            text.addPara("Acquired log entry for The Delve",g,h,"The Delve","");

            Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);
            text.setFontInsignia();

            if (asteria)text.addPara("All three of you stand up and get ready to move on. Jack gives you a firm handshake as you leave. On your way back out, you try your best not to get lost in the underground maze that is Asteria.");
            //alt
            if (!asteria)text.addPara("All three of you stand up and get ready to move on. Jack gives you a firm handshake as you leave. You make your way back the docks, and try not to get lost in the maze that this hastily built station is.");

            questUtil.setStage(16);
            done = true;
            PortsideBarData.getInstance().removeEvent(this);
        }

        if (initial) {
            dialog.getVisualPanel().showPersonInfo(person, true);
        }
        if (arrived) {
            dialog.getVisualPanel().hideSecondPerson();
            dialog.getVisualPanel().showImageVisual(new InteractionDialogImageVisual("illustrations", "nskr_crib", 640, 400));
        }
        if (chatting) {
            dialog.getVisualPanel().showPersonInfo(jack, true);
            dialog.getVisualPanel().showSecondPerson(alice);
        }
    }

    public enum OptionId {
        A1,
        A2,
        A3,
        A4,
        A5,
        A6,
        B1,
        B2,
        B3,
        B4,
        B5,
        B6,
        B7,
        D1,
        D2,
        C1,
        C2,
        C3,
        C4,
        Q1,
        Q2,
        INIT3,
        INIT2,
        INIT1,
        MAIN3,
        MAIN2,
        MAIN,
        LEAVE, INIT,
    }

    @Override
    public boolean isDialogFinished() {
        return done;
    }

    protected boolean showCargoCap() {
        return false;
    }
}