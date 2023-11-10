package scripts.kissa.LOST_SECTOR.campaign.quests;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.InteractionDialogImageVisual;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.nskr_kestevenQuest;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.*;

public class nskr_glacierCommsDialog implements InteractionDialogPlugin {
    //
    //
    //

    public static final String RECOVERED_KEY = "nskr_glacierCommsKeyCount";
    public static final String PERSISTENT_KEY = "nskr_glacierCommsKey";
    public static final String PERSISTENT_RANDOM_KEY = "nskr_glacierCommsKeyRandom";

    private int time = 90;
    private final float damagePercent = 50f;
    private int damageCount = 4;
    private boolean arrived = false;
    private boolean walked = false;
    private boolean blasted = false;
    private boolean shutdown = false;
    private boolean damaged = false;
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

        dialog.getVisualPanel().showLargePlanet(dialog.getInteractionTarget());

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();

        text.setFontInsignia();

        text.addPara("Your fleet approaches the tundra planet Glacier.");

        //options.addOption("Leave", OptionId.LEAVE);

        int stage = questUtil.getStage();
        boolean aliceTip2 = questUtil.getCompleted(nskr_kestevenQuest.JOB5_ALICE_TIP_KEY2);
        if (stage >= 16 && aliceTip2 && !questUtil.getCompleted(RECOVERED_KEY)) {
            options.addOption("Search for the facility", OptionId.A1);
        }
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        Color h = Misc.getHighlightColor();
        Color b = Misc.getBasePlayerColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();

        text.addPara(optionText, b, h, "", "");

        //initial
        if (optionData == OptionId.A1) {
            options.clearOptions();
            text.addPara("You order the sensor team to begin tracking down the signal on the planets surface. And in no time you have confirmation on the comms facility's exact coordinates.");
            text.addPara("\"There's no sign of human activity on the site, past or present.\" The ops chief report. \"Well within the past few centuries at least. But there's no way of telling what else we could find here, we'll have to go in expecting the worst.\"");
            text.addPara("\"This is our last chance to turn back.\"");

            dialog.setOptionOnEscape("Leave", OptionId.LEAVE);

            options.addOption("Continue", OptionId.A2);
            options.addOption("Leave", OptionId.LEAVE);
        }
        //a2
        if (optionData == OptionId.A2) {
            //unset ESC
            dialog.setOptionOnEscape("", null);

            options.clearOptions();
            arrived = true;
            text.addPara("You watch the salvor teams shuttle land on the top of a large flat hill near the pole of the planet. Relentless wind and snow beats on the shuttle and crew. Far away rolling hills dot the horizon, everything else is barren.");
            text.addPara("The only evidence of the facility's existence are the tall comms towers going through the layers of packed snow and ice. The salvor crew has to use heavy machinery to start digging through the ice to even reach an entrance of any kind.");

            options.addOption("Continue", OptionId.A3);
        }
        //a3
        if (optionData == OptionId.A3) {
            options.clearOptions();
            text.addPara("After some waiting, and a lot of digging. The crew has finally reached the facility proper.");
            text.addPara("Despite being abandoned for seemingly centuries, the facility still has power. " +
                    "\"Explains how we could track down the signal so easily, everything is still lit up like no one left. It must be powered by some sort of passive decay reactor.\" your ops chief comments.");
            text.addPara("Clouds of dust get kicked up as the team makes their way through the countless hallways of the facility.");

            options.addOption("Continue", OptionId.A4);
        }
        //a4
        if (optionData == OptionId.A4) {
            options.clearOptions();
            text.addPara("Alarms go off on the bridge as multiple screens turn red. The security chief scrambles to assess the situation.");
            text.addPara("\"It appears we've tripped some security mechanism, it's brought multiple hidden anti-ship batteries around the perimeter online.\" The chief urgently reports.");
            text.addPara("They continue, making quick orders and frantically going through the info feeds \"Looks like the decades of frost are buying us some extra time, I'd say we have around an hour before they come fully online.\"");

            options.addOption("Continue", OptionId.A5);
            options.addOption("\"This *complicates* things.\"", OptionId.A5B);
            options.addOption("\"Ahh shit.\"", OptionId.A5C);
        }
        //a5
        if (optionData == OptionId.A5 || optionData == OptionId.A5B || optionData == OptionId.A5C) {
            options.clearOptions();
            text.addPara("The salvor team receives the orders to get moving, but their pace remains uncomfortably slow as they lug around the heavy EVA equipment and tools.");
            text.addPara("You watch the operations chief shout quick and precise orders across the bride, you need to reach the central command room and fast.");

            text.setFontSmallInsignia();
            text.addPara("You have around "+time+" minutes left.",g,h,""+time,"");
            text.setFontInsignia();

            options.addOption("Continue", OptionId.A6);
        }
        //a6 main1
        if (optionData == OptionId.A6) {
            options.clearOptions();
            text.addPara("The team makes steady progress deeper into the facility until they reach an impasse. A heavily reinforced door is blocking the shortest path.");
            text.addPara("The ops chief hastily makes some plans of action and delivers them to you. \"We have a few tricks we can try here, remember time is of the essence captain.\"");

            text.setFontSmallInsignia();
            time-=15;
            text.addPara("You have around "+time+" minutes left.",g,h,""+time,"");
            text.setFontInsignia();

            options.addOption("Try to find a way around", OptionId.A7);
            options.addOption("Cut trough it", OptionId.B1);
            options.addOption("Blast it", OptionId.C1);
        }
        //a7 walk
        if (optionData == OptionId.A7) {
            options.clearOptions();
            text.addPara("The team backtracks through the facility searching for an alternate route. Traversing the labyrinthine structure is quite time consuming, but eventually they discover a secondary route.");
            //text.addPara("");

            text.setFontSmallInsignia();
            time-=30;
            text.addPara("You have around "+time+" minutes left.",g,h,""+time,"");
            text.setFontInsignia();
            walked = true;

            options.addOption("Continue", OptionId.A8);
        }
        //b1 cut
        if (optionData == OptionId.B1) {
            options.clearOptions();
            text.addPara("The team sets up the plasma torch and gets to work. You watch as everyone pulls down a solid shining black visor on their helmets, suddenly a blinding stream of sparks fills the view while they cut the structure. " +
                    "It takes some time, but eventually they get through.");

            text.setFontSmallInsignia();
            time-=30;
            text.addPara("You have around "+time+" minutes left.",g,h,""+time,"");
            text.setFontInsignia();

            options.addOption("Continue", OptionId.A8);
        }
        //c1 blast
        if (optionData == OptionId.C1) {
            options.clearOptions();
            text.addPara("The team carefully install the shaped breaching charge. They take cover, and as the charges are blown the hallways get filled with a storm of dust.");
            text.addPara("Seems like structure is only barely holding it together as it creeks and bends under the newfound stress. The team quickly squeezes through the hole in the door.");
            text.addPara("That was quite fast, lets hope the structure holds on for a little while longer.");

            text.setFontSmallInsignia();
            time-=15;
            text.addPara("You have around "+time+" minutes left.",g,h,""+time,"");
            text.setFontInsignia();
            blasted = true;

            options.addOption("Continue", OptionId.A8);
        }
        //a8 main2
        if (optionData == OptionId.A8) {
            options.clearOptions();
            text.addPara("The team has finally reached what looks like the right place. A massive central server room, " +
                    "dimly lit server racks with multiple stories of intertwined walkways seem to continue on for eternity, as a thin haze of fog and dust obscures your view.");
            text.addPara("The crew on bridge quickly map out the room, after a short wait they locate the central command console. \"Seems like we found what we are looking for, let's get to work.\" Your ops chief comments.");

            text.setFontSmallInsignia();
            time-=10;
            text.addPara("You have around "+time+" minutes left.",g,h,""+time,"");
            text.setFontInsignia();

            options.addOption("Continue", OptionId.A9);
        }
        //a9 main2 pt2
        if (optionData == OptionId.A9) {
            options.clearOptions();
            text.addPara("Next to the console you see a large rack of identical disks, an intricate sprawl of wires connects it to the console. It takes a moment for your crew to figure out which is the correct one.");
            text.addPara("The console seems to be in control of most of the facility's systems, your bridge crew might be able to guide the salvor team to shutdown the defenses.");
            text.addPara("The ops chief and the comms officer don't seem to agree which course of action is the best, so they leave the choice to you.");

            text.setFontSmallInsignia();
            time-=5;
            text.addPara("You have around "+time+" minutes left.",g,h,""+time,"");
            text.setFontInsignia();

            options.addOption("Grab the disk and run", OptionId.B2);
            options.addOption("Try to shut down the system", OptionId.C2);
        }
        //b2 run
        if (optionData == OptionId.B2) {
            options.clearOptions();
            //blast
            if (blasted) {
                text.addPara("The team quickly grabs the correct disk and wastes no time to get moving.");
                text.addPara("On their way back they make a worrying discovery. A part of blown up section has caved in, a considerable amount of time is wasted as the crew tries to clear a path big enough to squeeze through.");

                text.setFontSmallInsignia();
                time -= 45;
                text.addPara("You have around " + time + " minutes left.", g, h, "" + time, "");
                text.setFontInsignia();
            }
            //walk
            if (walked) {
                text.addPara("The team quickly grabs the correct disk and wastes no time to get moving.");
                text.addPara("Taking the long way back takes a considerable amount of time, but the team manages to get out without further incident.");

                text.setFontSmallInsignia();
                time -= 30;
                text.addPara("You have around " + time + " minutes left.", g, h, "" + time, "");
                text.setFontInsignia();
            }
            //cut
            if (!walked && !blasted) {
                text.addPara("The team quickly grabs the correct disk and wastes no time to get moving.");
                text.addPara("It's only a short walk and the team manages to get out without further incident.");

                text.setFontSmallInsignia();
                time -= 15;
                text.addPara("You have around " + time + " minutes left.", g, h, "" + time, "");
                text.setFontInsignia();
            }

            options.addOption("Continue", OptionId.A10);
        }
        //c2 shutdown
        if (optionData == OptionId.C2) {
            options.clearOptions();
            text.addPara("You watch as both the bridge crew and the team on site scramble to get control of the system. As they try to coerce it to shutdown, it becomes obvious that large parts of it are corrupted or otherwise non-functional.");
            text.addPara("It takes excruciatingly long for any part of the system to respond to orders, but eventually it seems like you might be back in control.");

            text.setFontSmallInsignia();
            time-=30;
            text.addPara("You have around "+time+" minutes left.",g,h,""+time,"");
            text.setFontInsignia();
            shutdown = true;

            options.addOption("Continue", OptionId.C3);
        }
        //c3 shutdown pt2
        if (optionData == OptionId.C3) {
            options.clearOptions();
            if (time<=0) {
                text.addPara("Suddenly the bridge goes into red alert, sirens blaring as it becomes obvious the anti-ship batteries have come online.");
                text.addPara("Seems like you are barely late, as the team finally finds a way to lower the emergency defense level of the facility. A barrage from the anti-ship batteries has already fired off, some ships in your fleet suffer some damage as a result.");

                Global.getSoundPlayer().playUISound("nskr_ui_hit", 1f, 1f);

                damageFleet(dialog, text);
                damaged = true;
                text.addPara("Despite the incident the system seems to be shut down, and the team is free to grab the disk and make their way back to the shuttle without further incident.");
            } else {
                text.addPara("After some frantic back and forth the team finally finds a way to lower the emergency defense level of the facility, de-escalating the situation. After a short breather the operation is back in progress.");
                text.addPara("With the systems shut down the team is free to grab the disk and make their way back to the shuttle.");
                if(blasted){
                    text.addPara("On their way back the team discovers that a part of blown up section has caved in, good thing your not in a hurry though. " +
                            "They manage to dig and reinforce a new path through rubble without much effort, and are shortly on their way back.");
                }
            }

            options.addOption("Continue", OptionId.A10);
        }
        //a10 main3
        if (optionData == OptionId.A10) {
            options.clearOptions();
            dialog.setOptionOnEscape("Continue", OptionId.LEAVE);
            options.addOption("Continue", OptionId.LEAVE);
            //run
            if(!shutdown) {
                text.addPara("The team rushes into the shuttle, a blizzard of snow gets kicked up as the thruster go into maximum power. " +
                        "In a short moment the shuttle is on it's way back to the fleet.");
            } else {
                text.addPara("The team loads everything into the shuttle, a blizzard of snow gets kicked up as the thruster go into maximum power. " +
                        "In a short moment the shuttle is on it's way back to the fleet.");
            }
            //get hit by batteries
            if (time<=0 && !shutdown){
                text.addPara("Suddenly the bridge goes into red alert, sirens blaring as it becomes obvious the anti-ship batteries have come online.");
                text.addPara("Your fleet manages to pull out of range as the shuttle arrives just in time, but some ships in your fleet suffer some damage as a result.");

                Global.getSoundPlayer().playUISound("nskr_ui_hit",1f,1f);

                damageFleet(dialog, text);
                damaged = true;
            }
            if (!damaged){
                text.addPara("The operations chief comes to congratulate for a job well done. \"I knew I could trust you, that was about to get hairy captain, can't believe we made it out with out a scratch.\"");

                Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

                options.addOption("\"I know, I'm the best.\"", OptionId.LEAVE2);
            }

            questUtil.setCompleted(true, RECOVERED_KEY);
            questUtil.setDisksRecovered(questUtil.getDisksRecovered()+1);

            //remove important
            if (dialog.getInteractionTarget().getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_MISSION_IMPORTANT)){
                dialog.getInteractionTarget().getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
            }

            text.setFontSmallInsignia();
            //acquire text
            text.addPara("Acquired Data Disk #5",g,h,"Data Disk #5","");
            text.setFontInsignia();

            //stalker fleet spawn in stalkerSpawner
            //

        }

        if(arrived)visual.showImageVisual(new InteractionDialogImageVisual("illustrations", "nskr_glacier", 640, 400));

        //leave
        if (optionData == OptionId.LEAVE || optionData == OptionId.LEAVE2) {
            options.clearOptions();
            dialog.dismiss();
        }
    }

    private void damageFleet(InteractionDialogAPI dialog, TextPanelAPI text) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf==null)return;

        ArrayList<FleetMemberAPI> validShips = new ArrayList<>();
        for (FleetMemberAPI m : pf.getMembersWithFightersCopy()){
            if (m.isFighterWing()) continue;
            if (m.getRepairTracker().computeRepairednessFraction()<0.25f) continue;
            if (m.getHullSpec()==null || m.getStatus()==null) continue;
            validShips.add(m);
        }
        if (validShips.size()<damageCount) damageCount = validShips.size();

        while (damageCount>0){
            int index = MathUtils.getRandomNumberInRange(0,validShips.size()-1);
            FleetMemberAPI ship = validShips.get(index);
            ship.getStatus().applyHullFractionDamage(MathUtils.getRandomNumberInRange(damagePercent/2f,damagePercent)/100f);
            ship.getRepairTracker().setCR(ship.getRepairTracker().getCR()-(MathUtils.getRandomNumberInRange(damagePercent/2f,damagePercent)/100f));

            text.setFontSmallInsignia();
            //damage text
            text.addPara(""+ship.getShipName()+" "+ship.getHullSpec().getHullName()+"-class was damaged in the barrage.",Misc.getGrayColor(),Misc.getNegativeHighlightColor(),ship.getHullSpec().getHullName(),"");
            text.setFontInsignia();

            validShips.remove(index);
            damageCount--;
        }
    }

    public enum OptionId {
        A1,
        A2,
        A3,
        A4,
        A5,
        A5B,
        A5C,
        A6,
        A7,
        A8,
        A9,
        A10,
        B1,
        B2,
        B3,
        B4,
        B5,
        C1,
        C2,
        C3,
        C4,
        C5,
        LEAVE,
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