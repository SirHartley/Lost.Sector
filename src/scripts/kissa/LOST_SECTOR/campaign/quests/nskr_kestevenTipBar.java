package scripts.kissa.LOST_SECTOR.campaign.quests;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.intel.nskr_kestevenTipBarIntel;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleSystem;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class nskr_kestevenTipBar extends BaseBarEvent {

    //
    public static final String PAID_FOR_INFO_LOC = "nskr_kestevenTipBarPaidForLocation";
    public static final String PERSISTENT_RANDOM_KEY = "nskr_kestevenTipBarRandom";
    private float money = 0f;

    static void log(final String message) {
        Global.getLogger(nskr_kestevenTipBar.class).info(message);
    }

    @Override
    public boolean isAlwaysShow() {
        return true;
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        if (!market.getFaction().getId().equals("kesteven")) return false;
        return true;
    }

    @Override
    public String getBarEventId() {
        return "nskr_kestevenTipBar_"+id;
    }

    @Override
    public boolean shouldRemoveEvent() {
        return done;
    }

    private boolean done = false;
    private Gender gender;
    private PersonAPI person;
    private String id;
    private SectorEntityToken loc = null;

    public nskr_kestevenTipBar(String id) {
        this.id = id;
        StarSystemAPI temp = getRandomSystemWithBlacklist(getRandom());
        if (temp!=null) {
            this.loc = temp.getCenter();
            log("TipBar loaded");
        } else {
            done = true;
            log("TipBar no valid loc, cancel");
        }
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.addPromptAndOption(dialog, memoryMap);

        if (done) return;

        Random random = getRandom();

        gender = Gender.MALE;
        if (random.nextFloat() > 0.5f) {
            gender = Gender.FEMALE;
        }
        person = Global.getSector().getFaction("kesteven").createRandomPerson(gender, random);
        person.setPostId(Ranks.POST_GENERIC_MILITARY);

        TextPanelAPI text = dialog.getTextPanel();

        text.addPara("A Kesteven officer is busy with their datapad at one of the more secluded tables.");
        dialog.getOptionPanel().addOption("Approach the Kesteven officer", this);

    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);

        if (done) return;

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();

        String himOrHerSelf = "himself";
        String himOrHer = "him";
        String hisOrHer = "his";
        String heOrShe = "he";
        String HeOrShe = "He";
        if (gender == Gender.FEMALE){
            himOrHerSelf = "herself";
            himOrHer = "her";
            hisOrHer = "her";
            heOrShe = "she";
            HeOrShe = "She";
        }

        options.clearOptions();
        if (money==0f) money = 1000f*mathUtil.getSeededRandomNumberInRange(10, 30, getRandom());
        dialog.getVisualPanel().showPersonInfo(person, true);
        text.addPara("The officer is scrolling through various datafeeds - marking down important information while dismissing others. " +
                HeOrShe+ " is sipping on a moderately priced drink while you approach.");
        options.addOption("Offer to buy a round of drinks", OptionId.A1);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();

        String himOrHerSelf = "himself";
        String himOrHer = "him";
        String hisOrHer = "his";
        String heOrShe = "he";
        String HeOrShe = "He";
        if (gender == Gender.FEMALE){
            himOrHerSelf = "herself";
            himOrHer = "her";
            hisOrHer = "her";
            heOrShe = "she";
            HeOrShe = "She";
        }

        TextPanelAPI text = dialog.getTextPanel();
        dialog.getVisualPanel().showPersonInfo(person, true);
        options.clearOptions();

        //initial
        if (optionData==OptionId.A1){

            text.addPara("\"So much data, so little time.\" "+heOrShe+" sighs. \"There's no way we can check out all these leads on our next expedition.\"");
            text.addPara(HeOrShe+" takes a long drink from the glass. \"Ahh, but captain - you can solve my problem now. For a modest fee I could share a star system of interest to you.\"");
            text.addPara(HeOrShe+" looks directly at you. \"Are you interested?\"");

            options.addOption("\"Yes.\"", OptionId.A2);
            options.addOption("\"Maybe.\"", OptionId.B1);
            options.addOption("Politely decline", OptionId.LEAVE);
        }
        //a2
        if (optionData==OptionId.A2 || optionData==OptionId.B1){
            //set
            SectorEntityToken currLoc = setTipLocation(loc);
            float distF = Misc.getDistanceLY(dialog.getInteractionTarget(), currLoc);
            String creds = Misc.getDGSCredits(money);
            String type = getThemeType(currLoc);
            distF *= 100f;
            distF = Math.round(distF);
            distF /= 100f;
            String dist = distF+"";

            text.addPara("\"The system is located "+dist+" light-years away, and is reported to be a hotspot of "+type+" activity.\" "+heOrShe+" looks up from the datapad and eyes your response.",tc,h,dist,type);
            text.addPara("\"Of course you will have to pay for this information captain, my price is a measly "+creds+".\" "+HeOrShe+" takes a quick look around the room " +
                    "\"There are people that uh- need to be convinced of this transaction, it's a fair price.\"",tc,h,creds,"");

            if(Global.getSector().getPlayerFleet().getCargo().getCredits().get()>money) options.addOption("Buy the information for "+creds, OptionId.A3);
            options.addOption("Politely decline", OptionId.LEAVE);
        }
        //a3
        if (optionData==OptionId.A3){

            String sys = getTipLocation().getStarSystem().getName();
            text.addPara("\"The pleasure is mine captain.\"");

            //remove
            Global.getSector().getPlayerFleet().getCargo().getCredits().add(-money);
            //acquire text
            text.setFontSmallInsignia();
            String creds = Misc.getDGSCredits(money);
            text.addPara("Lost " + creds, g, r, creds, "");
            text.addPara("Added log entry for the "+sys,g,h,sys,"");
            text.setFontInsignia();

            Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);

            options.addOption("Leave", OptionId.LEAVE_BOUGHT);
        }
        //leave paid
        if (optionData==OptionId.LEAVE_BOUGHT){
            dialog.getVisualPanel().fadeVisualOut();
            text.addPara("With the transaction complete, You leave the officer to their work.");
            //intel
            Global.getSector().getIntelManager().addIntel(new nskr_kestevenTipBarIntel(getTipLocation().getStarSystem(), id, getThemeType(getTipLocation())), false);

            done = true;
            PortsideBarData.getInstance().removeEvent(this);
        }
        //leave
        if (optionData==OptionId.LEAVE){
            dialog.getVisualPanel().fadeVisualOut();
            text.addPara("You decide it's best to leave.");

            done = true;
            PortsideBarData.getInstance().removeEvent(this);
        }
    }

    public enum OptionId {
        A1,
        A2,
        A3,
        B1,
        LEAVE,
        LEAVE_BOUGHT,
    }

    @Override
    public boolean isDialogFinished() {
        return done;
    }

    public Random getRandom() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

            data.put(PERSISTENT_RANDOM_KEY, new Random(util.getSeedParsed()));
        }
        return (Random)data.get(PERSISTENT_RANDOM_KEY);
    }

    public SectorEntityToken setTipLocation(SectorEntityToken loc){
        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(PAID_FOR_INFO_LOC+id, loc);

        return (SectorEntityToken) data.get(PAID_FOR_INFO_LOC+id);
    }

    public SectorEntityToken getTipLocation(){
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (data.get(PAID_FOR_INFO_LOC+id)==null){
            //data.put(PAID_FOR_INFO_LOC+id, getRandomSystemWithBlacklist(getRandom()).getCenter());
            return null;
        }
        return (SectorEntityToken) data.get(PAID_FOR_INFO_LOC+id);
    }

    private StarSystemAPI getRandomSystemWithBlacklist(Random random) {



        simpleSystem simpleSystem = new simpleSystem(random, 0);
        //derelict
        if (random.nextFloat()<0.50f) {
            List<String> pickEntities = new ArrayList<>();
            pickEntities.add(Entities.DERELICT_SURVEY_SHIP);
            pickEntities.add(Entities.DERELICT_MOTHERSHIP);

            //add
            simpleSystem.pickEntities = pickEntities;
        }
        //remnant
        else {
            List<StarSystemAPI> pickSystems = new ArrayList<>();
            //get all beacon systems
            for (SectorEntityToken e : Global.getSector().getHyperspace().getAllEntities()){
                if (e==null) continue;
                if (e.getTags()==null) continue;
                if (e.getTags().contains(Tags.BEACON_LOW) || e.getTags().contains(Tags.BEACON_MEDIUM) || e.getTags().contains(Tags.BEACON_HIGH)){
                    if (e.getOrbit()==null || e.getOrbit().getFocus()==null) continue;
                    //hax
                    pickSystems.add(util.getNearestSystem(e.getOrbit().getFocus().getLocation()));
                    //log("added "+util.getNearestSystem(e.getOrbit().getFocus().getLocation()).getName());
                }
            }
            //add
            simpleSystem.pickSystems = pickSystems;
        }
        //default
        simpleSystem.allowEnteredByPlayer = false;
        simpleSystem.pickOnlyInProcgen = true;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("ERROR no valid system");
        return null;
    }

    private String getThemeType(SectorEntityToken loc) {
        String type = "";

        if (loc.getStarSystem().hasTag(Tags.THEME_REMNANT)) type = "Remnant";
        if (loc.getStarSystem().hasTag(Tags.THEME_DERELICT)) type = "Derelict";

        return type;
    }
}