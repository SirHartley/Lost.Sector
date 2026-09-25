package lostsector.dialogue.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.kesteven.quest.QuestHelper;
import lostsector.campaign.kesteven.quest.KestevenFlag;
import lostsector.campaign.kesteven.quest.KestevenQuest;
import lostsector.campaign.kesteven.quest.KestevenState;
import lostsector.helper.Ids;
import lostsector.helper.UiSounds;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class nskr_altEndingDialogTT extends PaginatedOptions {

    public static final String PERSON_LOCKED_KEY = "$nskr_altEndingDialogLockedToPerson";

    private Color h;
    private Color g;
    private Color gr;
    private Color r;
    private Color tc;
    private Color s;
    private final float pad = 3f;
    private final float opad = 10f;

    protected CampaignFleetAPI playerFleet;
    protected SectorEntityToken entity;
    protected MarketAPI market;
    protected FactionAPI playerFaction;
    protected FactionAPI entityFaction;
    protected TextPanelAPI text;
    protected CargoAPI playerCargo;
    protected PersonAPI person;
    protected PersonAPI player;
    protected FactionAPI faction;
    protected ShipAPI ship;

    protected java.util.List<String> disabledOpts = new ArrayList<>();
    private String HisOrHer;
    private String hisOrHer;
    private String HeOrShe;
    private String heOrShe;

    static void log(final String message) {
        Global.getLogger(nskr_altEndingDialogTT.class).info(message);
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List< Misc.Token > params, Map<String, MemoryAPI> memoryMap)
    {
        String arg = params.get(0).getString(memoryMap);
        setupVars(dialog, memoryMap);

        switch (arg)
        {
            case "init":
                break;
            case "hasOption":
                return validEntity(entity);
            case "addOptions":
                addOptions(QuestHelper.getCompleted(KestevenFlag.TT_ENDING_SECOND_TALK));
                showOptions();
                break;
            case "tachAgree":
                tachAgree();
                break;
            case "setPriceIncrease":
                setPrice(2500000f);
                break;
            case "setSecond":
                setSecond();
                break;
        }
        return true;
    }

    /**
     * To be called only when paginated dialog options are required.
     * Otherwise we get nested dialogs that take multiple clicks of the exit option to actually exit.
     * @param dialog
     */
    protected void setupDelegateDialog(InteractionDialogAPI dialog)
    {
        originalPlugin = dialog.getPlugin();

        dialog.setPlugin(this);
        init(dialog);
    }

    protected void setupVars(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap)
    {
        this.dialog = dialog;
        this.memoryMap = memoryMap;

        entity = dialog.getInteractionTarget();
        text = dialog.getTextPanel();
        market = entity.getMarket();

        playerFleet = Global.getSector().getPlayerFleet();
        playerCargo = playerFleet.getCargo();

        playerFaction = Global.getSector().getPlayerFaction();
        entityFaction = entity.getFaction();

        player = Global.getSector().getPlayerPerson();
        person = dialog.getInteractionTarget().getActivePerson();

        h = Misc.getHighlightColor();
        g = Misc.getGrayColor();
        gr = Misc.getPositiveHighlightColor();
        r = Misc.getNegativeHighlightColor();
        tc = Misc.getTextColor();
        s = Misc.getStoryBrightColor();

        heOrShe = "he";
        HeOrShe = "He";
        hisOrHer = "his";
        HisOrHer = "His";
        if (person.getGender()== FullName.Gender.FEMALE){
            heOrShe = "she";
            HeOrShe = "She";
            hisOrHer = "her";
            HisOrHer = "Her";
        }

    }

    @Override
    public void showOptions() {
        super.showOptions();
        for (String optId : disabledOpts)
        {
            dialog.getOptionPanel().setEnabled(optId, false);
        }
    }

    protected void addOptions(boolean secondTime){

        //becomes person locked if you reach second stage of dialog
        if (!person.getMemory().contains(PERSON_LOCKED_KEY)) {
            if (!secondTime) {
                //FIRST
                text.addPara("\"What a fascinating object you speak of, if you are telling the truth of course.\" "+ HeOrShe +" looks skeptical");
                String creds = Misc.getDGSCredits(2000000f);
                text.addPara("\"Such an item holds great power, the Tri-Tachyon corporation is willing to grant "+creds+" for such an item.\"", tc, h,creds,"");
                text.addPara("\"Remember, such power would come with additional costs, requiring further investment. So... Take it or leave it captain.\"");

                String cash = Misc.getDGSCredits(2500000f);
                addOption("Sell the Chip", "nskr_altEndingTTAgree");
                addOption("\"How about " + cash + "\"", "nskr_altEndingTTIncrease");
                addOption("\"Actually, never mind.\"", "nskr_altEndingExit");
            }
            //SECOND
            else {
                person.getMemory().set(PERSON_LOCKED_KEY, true);
                text.addPara("\"Damn... You drive a hard bargain captain, but for such an exceptional artifact... I think we can squeeze in some extra investment.\" " +
                        ""+ HeOrShe +" takes a moment to adjust some datafeeds on "+ hisOrHer +" end.");
                String cash = Misc.getDGSCredits(2500000f);
                text.addPara("\""+cash+" for the Chip, in liquid cash. Just think of all the things you could buy.\""+ HeOrShe +" has a ravenous smile.", tc, h,cash,"");

                addOption("Sell the Chip", "nskr_altEndingTTAgree");
                addOption("\"Actually, never mind.\"", "nskr_altEndingExit");
            }
        } else if (!QuestHelper.getCompleted(KestevenFlag.ALT_ENDING_DONE)){
            //person locked, came back to talk
            text.addPara("\"Changed your fickle mind yet "+player.getName().getFullName()+"?\"");
            String cash = Misc.getDGSCredits(2500000f);
            text.addPara("\"The "+cash+" for the Chip is waiting for you.\"", tc, h,cash,"");

            addOption("Sell the Chip", "nskr_altEndingTTAgree");
            addOption("\"Nope.\"", "nskr_altEndingExit");
        }
    }

    protected void tachAgree(){

        text.addPara("\"You made the right choice captain. Enjoy the spoils.\" "+HeOrShe+" nods and gets back to work.");

        //credits
        float creds = getPrice();
        playerCargo.getCredits().add(creds);
        //+rep
        Global.getSector().getFaction(Factions.PLAYER).adjustRelationship(Factions.TRITACHYON,0.15f);
        person.getRelToPlayer().adjustRelationship(0.10f, RepLevel.COOPERATIVE);

        text.setFontSmallInsignia();
        //acquire text
        text.addPara("Lost the Unlimited Production Chip",g,r,"Lost","");

        String payout = Misc.getDGSCredits(creds);
        text.addPara("Received +" + payout,g,h,"+"+payout,"");
        text.addPara("Relationship with Tri-Tachyon improved by 15",g,gr,"15","");
        text.addPara("Relationship with "+person.getName().getFullName()+" improved by 10",g,gr,"10","");

        //contact
        if (!ContactIntel.playerHasIntelItemForContact(person)) {
            ContactIntel.addPotentialContact(1f, person, dialog.getInteractionTarget().getMarket(), text);
        }

        //sound
        UiSounds.playUiRepRaiseNoise();

        //mad
        nskr_altEndingDialogLuddic.makeMad(text, g, r);

        //add chip
        StarSystemAPI hybrasil = Global.getSector().getStarSystem("Hybrasil");
        if (hybrasil!=null) {
            MarketAPI culann = Global.getSector().getEconomy().getMarket("culann");
            if (culann != null && culann.getFactionId() != null && culann.getFactionId().equals(Factions.TRITACHYON)) {
                culann.addCondition(Ids.UNLIMITED_PRODUCTION_CHIP_CONDITION_ID);
            } else market.addCondition(Ids.UNLIMITED_PRODUCTION_CHIP_CONDITION_ID);
        } else market.addCondition(Ids.UNLIMITED_PRODUCTION_CHIP_CONDITION_ID);

        dialog.getOptionPanel().clearOptions();

        text.setFontInsignia();
        addOption("Leave", "nskr_altEndingExitAgree");
        showOptions();
    }

    protected void setPrice(float amount){

        QuestHelper.setTtPayout(amount);
    }

    protected float getPrice(){
        //init
        if (QuestHelper.getTtPayout()<2000000f) QuestHelper.setTtPayout(2000000f);

        return QuestHelper.getTtPayout();
    }

    protected void setSecond(){

        QuestHelper.setCompleted(true, KestevenFlag.TT_ENDING_SECOND_TALK);
    }

    protected boolean validEntity(SectorEntityToken entity) {
        if (entity==null) return false;
        MarketAPI market = entity.getMarket();
        if (market==null) return false;
        if (market.getFactionId()==null) return false;
        //we don't have it anymore
        if (QuestHelper.getCompleted(KestevenFlag.CHIP_HANDED_TO_ELIZA)) return false;
        //person locked
        if (QuestHelper.getCompleted(KestevenFlag.TT_ENDING_SECOND_TALK) && !person.getMemory().contains(PERSON_LOCKED_KEY)) return false;
        //DONE
        if (QuestHelper.getCompleted(KestevenFlag.ALT_ENDING_DONE)) return false;
        //pick correct faction market
        return market.getFactionId().equals(Factions.TRITACHYON);
    }

    public static Random getRandom() {
        return KestevenQuest.random(KestevenState.RANDOM_ALT_ENDING);
    }
}
