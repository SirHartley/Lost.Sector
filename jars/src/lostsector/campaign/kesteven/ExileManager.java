package lostsector.campaign.kesteven;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import lostsector.ModPlugin;
import lostsector.persistence.Saved;
import lostsector.campaign.kesteven.quest.KestevenQuest;
import lostsector.campaign.kesteven.quest.KestevenPeople;
import lostsector.helper.SectorLookup;
import lostsector.world.SectorGen;

import java.util.Map;
import java.util.Random;

public class ExileManager extends BaseCampaignEventListener implements EveryFrameScript  {

    //how often we run
    public static final float TIMER = 1f;
    public static final String EXILE_KEY = "kestevenExiled";
    Saved<Float> counter;

    static void log(final String message) {
        Global.getLogger(ExileManager.class).info(message);
    }

    public ExileManager() {
        super(false);
        this.counter = new Saved<>("exileCounter", TIMER*10f);
    }

    public boolean isDone() {
        return false;
    }
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector().isPaused()) return;
        final Saved<Float> counter = this.counter;

        if (Global.getSector().isInFastAdvance()) {
            counter.val += 2f*amount;
        } else{
            counter.val += amount;
        }

        //
        if (counter.val>TIMER*10f) {
            counter.val = 0f;
            MarketAPI asteria = Global.getSector().getEconomy().getMarket("nskr_asteria");
            MarketAPI outpost = Global.getSector().getEconomy().getMarket("nskr_outpost");

            //fix outpost ppl no exile
            if (asteria!=null) {
                if (!getExiled(EXILE_KEY) && !canExile() && !asteria.getFaction().getId().equals("kesteven")) {
                    PersonAPI jack = KestevenPeople.getJack();
                    PersonAPI alice = KestevenPeople.getAlice();
                    PersonAPI nick = KestevenPeople.getNick();
                    PersonAPI michael = KestevenPeople.getMichael();
                    if (nick != null && outpost!=null){
                        outpost.getCommDirectory().removePerson(nick);
                        outpost.removePerson(nick);
                    }
                    if (michael != null){
                        asteria.getCommDirectory().removePerson(michael);
                        asteria.removePerson(michael);
                    }
                    if (jack != null){
                        asteria.getCommDirectory().removePerson(jack);
                        asteria.removePerson(jack);
                    }
                    if (alice != null){
                        asteria.getCommDirectory().removePerson(alice);
                        asteria.removePerson(alice);
                    }
                }
            }
            //fix outpost ppl exiled
            if (getExiled(EXILE_KEY) && !SectorLookup.outpostExists()){
                PersonAPI jack = KestevenPeople.getJack();
                PersonAPI alice = KestevenPeople.getAlice();
                PersonAPI nick = KestevenPeople.getNick();
                PersonAPI michael = KestevenPeople.getMichael();
                if (outpost!=null) {
                    if (nick != null){
                        outpost.getCommDirectory().removePerson(nick);
                        outpost.removePerson(nick);
                    }
                    if (michael != null){
                        outpost.getCommDirectory().removePerson(michael);
                        outpost.removePerson(michael);
                        outpost.setAdmin(Global.getSector().getFaction(outpost.getFactionId()).createRandomPerson());
                    }
                    if (jack != null){
                        outpost.getCommDirectory().removePerson(jack);
                        outpost.removePerson(jack);
                    }
                    if (alice != null){
                        outpost.getCommDirectory().removePerson(alice);
                        outpost.removePerson(alice);
                    }
                }
            }

            //unExile
            if (getExiled(EXILE_KEY) && outpost!=null && outpost.getFaction().getId().equals("kesteven")) {
                if (asteria!=null){
                    if (asteria.getFaction().getId().equals("kesteven")) {
                        unExile();
                        setExiled(false, EXILE_KEY);
                        //unExile popup
                        Global.getSector().getCampaignUI().addMessage("With Asteria back in Kesteven control, the leadership has returned to their home planet.",
                                Global.getSettings().getColor("standardTextColor"),
                                "Kesteven",
                                "",
                                Global.getSector().getFaction("kesteven").getColor(),
                                Global.getSettings().getColor("yellowTextColor"));
                    }
                }
            }
            //no place to exile to
            if (outpost==null || !outpost.getFaction().getId().equals("kesteven")) return;
            //asteria is fine
            if (asteria!=null && asteria.getFaction().getId().equals("kesteven")) return;

            //exile
            if (!getExiled(EXILE_KEY)) {
                exile();
                setExiled(true, EXILE_KEY);
                // Silent when Asteria was never generated: Kesteven has been at the Outpost from the start.
                if (SectorLookup.getAsteria() != null) {
                    Global.getSector().getCampaignUI().addMessage("With the loss of Asteria, the leadership of Kesteven Corporation has fled to "+ SectorLookup.outpostName()+".",
                            Global.getSettings().getColor("standardTextColor"),
                            "Kesteven Corporation",
                            "",
                            Global.getSector().getFaction("kesteven").getColor(),
                            Global.getSettings().getColor("yellowTextColor"));
                }
            }
        }
    }

    public static boolean canExile(){
        boolean exile = true;
        SectorEntityToken o = SectorLookup.getOutpost();
        if (o==null) return false;
        MarketAPI outpost = o.getMarket();
        if (outpost==null || !outpost.getFaction().getId().equals("kesteven")) return false;
        return exile;
    }

    public void exile() {
        MarketAPI outpost = SectorLookup.getOutpost().getMarket();
        boolean jackGone = KestevenQuest.isJackGone();
        //add heavy industry item
        if (outpost.getIndustry(Industries.HEAVYINDUSTRY) != null) {
            if (outpost.getIndustry(Industries.HEAVYINDUSTRY).getSpecialItem() == null) {
                outpost.getIndustry(Industries.HEAVYINDUSTRY).setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));
            }
        } else if (outpost.getIndustry(Industries.ORBITALWORKS) != null) {
            if (outpost.getIndustry(Industries.ORBITALWORKS).getSpecialItem() == null) {
                outpost.getIndustry(Industries.ORBITALWORKS).setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));
            }
        }

        //move people
        PersonAPI michael = KestevenPeople.getMichael();
        if (michael==null){
            SectorGen.genMichael(outpost, 0);
        }
        PersonAPI jack = KestevenPeople.getJack();
        if (jack==null && !jackGone){
            SectorGen.genJack(outpost, 1);
        }
        PersonAPI alice = KestevenPeople.getAlice();
        if (alice==null){
            SectorGen.genAlice(outpost, 2);
        }
        PersonAPI nick = KestevenPeople.getNick();
        if (nick==null){
            SectorGen.genNicholas(outpost, 3);
        }
        //remove ppl from asteria
        MarketAPI asteria = Global.getSector().getEconomy().getMarket("nskr_asteria");
        if (asteria!=null){
            asteria.getCommDirectory().removePerson(michael);
            asteria.removePerson(michael);
            asteria.setAdmin(Global.getSector().getFaction(asteria.getFactionId()).createRandomPerson());
            if (!jackGone) {
                asteria.getCommDirectory().removePerson(jack);
                asteria.removePerson(jack);
            }
            asteria.getCommDirectory().removePerson(alice);
            asteria.removePerson(alice);
        }
        if (nick!=null){
            outpost.getCommDirectory().removePerson(nick);
            outpost.removePerson(nick);
        }
        //re add
        if (michael!=null){
            outpost.getCommDirectory().addPerson(michael, 0);
            outpost.addPerson(michael);
            outpost.setAdmin(michael);
        }
        if (jack!=null){
            outpost.getCommDirectory().addPerson(jack, 1);
            outpost.addPerson(jack);
        }
        if (alice!=null){
            outpost.getCommDirectory().addPerson(alice, 2);
            outpost.addPerson(alice);
        }
        if (nick!=null){
            outpost.getCommDirectory().addPerson(nick, 3);
            outpost.addPerson(nick);
        }

        log("EXILED to outpost");
    }

    public void unExile() {
        PersonAPI jack = KestevenPeople.getJack();
        PersonAPI alice = KestevenPeople.getAlice();
        PersonAPI nick = KestevenPeople.getNick();
        PersonAPI michael = KestevenPeople.getMichael();

        MarketAPI asteria = Global.getSector().getEconomy().getMarket("nskr_asteria");
        MarketAPI outpost = SectorLookup.getOutpost().getMarket();
        boolean jackGone = KestevenQuest.isJackGone();
        if (outpost!=null){
            //new admin
            PersonAPI admin = Global.getSector().getFaction("kesteven").createRandomPerson(new Random());
            if (ModPlugin.IS_INDEVO){
                admin.getStats().setSkillLevel("indevo_planetary_operations", 1);
            } else admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
            outpost.setAdmin(admin);
            removeExileNanoforge(outpost);
            //remove ppl from outpost
            outpost.getCommDirectory().removePerson(michael);
            outpost.removePerson(michael);
            if (jack!=null) {
                outpost.getCommDirectory().removePerson(jack);
                outpost.removePerson(jack);
            }
            outpost.getCommDirectory().removePerson(alice);
            outpost.removePerson(alice);
            outpost.getCommDirectory().removePerson(nick);
            outpost.removePerson(nick);
            //add nick back
            outpost.getCommDirectory().addPerson(nick, 2);
            outpost.addPerson(nick);
        }
        //move people
        asteria.getCommDirectory().addPerson(michael, 0);
        asteria.addPerson(michael);
        asteria.setAdmin(michael);
        if (!jackGone) {
            asteria.getCommDirectory().addPerson(jack, 1);
            asteria.addPerson(jack);
        }
        asteria.getCommDirectory().addPerson(alice, 2);
        asteria.addPerson(alice);

        log("UNEXILED to asteria");
    }

    // exile() gives the Outpost's heavy industry or orbital works a corrupted nanoforge. The market
    // may have lost the item, or changed hands and back, while Kesteven was in exile.
    private static void removeExileNanoforge(MarketAPI outpost) {
        Industry industry = outpost.getIndustry(Industries.HEAVYINDUSTRY);
        if (industry == null) industry = outpost.getIndustry(Industries.ORBITALWORKS);
        if (industry == null) return;

        SpecialItemData item = industry.getSpecialItem();
        if (item != null && Items.CORRUPTED_NANOFORGE.equals(item.getId())) {
            industry.setSpecialItem(null);
        }
    }

    public static boolean getExiled(String id) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, false);

        return (boolean)data.get(id);
    }

    public static void setExiled(boolean exiled, String id) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(id, exiled);
    }
}
