package scripts.kissa.LOST_SECTOR.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.util.ids;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class nskr_altEndingDialogLuddic extends PaginatedOptions {

    public static final String SECOND_TIME_KEY = "nskr_altEndingDialogSecondTimeLuddic";
    public static final String PERSON_LOCKED_KEY = "$nskr_altEndingDialogLockedToPerson";
    public static final String DIALOG_FINISHED_KEY = "nskr_EndingAltDialogKeyFinished";

    public static final String PERSISTENT_RANDOM_KEY = "nskr_EndingAltDialogKeyRandom";
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

    protected List<String> disabledOpts = new ArrayList<>();
    private String HisOrHer;
    private String hisOrHer;
    private String HeOrShe;
    private String heOrShe;

    static void log(final String message) {
        Global.getLogger(nskr_altEndingDialogLuddic.class).info(message);
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List< Misc.Token > params, Map<String, MemoryAPI > memoryMap)
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
                addOptions(questUtil.getCompleted(SECOND_TIME_KEY));
                showOptions();
                break;
            case "luddicAgree":
                luddicAgree();
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
                text.addPara("\"I hear you speak of a vile creation of mammon.\" " + HeOrShe + " says sharply. \"It must be destroyed, no question about it.\"", tc, h,"destroyed","");
                text.addPara("\"For these are the evils that corrupt our world - Taking us further from Providence. " +
                        "You don't deny this do you captain? For certain you knew our reaction to this object, a true walker would know to approach us.\"");
                text.addPara("\"Is this true captain?\" " + HeOrShe + " rubs " + hisOrHer + " chin with deep intent in " + hisOrHer + " eyes.");

                addOption("Destroy the Chip", "nskr_altEndingLuddicAgree");
                addOption("\"So I would get nothing?\"", "nskr_altEndingLuddicDoubt");
                addOption("\"Actually, never mind.\"", "nskr_altEndingExit");
            }
            //SECOND
            else {
                person.getMemory().set(PERSON_LOCKED_KEY, true);
                text.addPara("\"Faith is not about having something, you do not *get* your way to providence. " +
                        "When one takes a righteous action - Suddenly they can find themselves walking the right path.\"");
                text.addPara("\"So, will you help amend for the sins of the past, and destroy the Chip?\" "+ HeOrShe +" questions.", tc, h,"destroy","");

                addOption("Destroy the Chip", "nskr_altEndingLuddicAgree");
                addOption("\"Actually, never mind.\"", "nskr_altEndingExit");
            }
        } else if (!questUtil.getCompleted(DIALOG_FINISHED_KEY)){
            //person locked, came back to talk
            text.addPara("\"Have you finally seen the path captain? Or are you here to just waste my time.\"");
            text.addPara("\"You need to destroy the Chip.\" "+ HeOrShe +" commands.", tc, h,"destroy","");

            addOption("Destroy the Chip", "nskr_altEndingLuddicAgree");
            addOption("\"Nope.\"", "nskr_altEndingExit");
        }
    }

    protected void luddicAgree() {

        text.addPara("\"The tools of mammon shall be rendered to pieces, rightfully reduced to ash, returned to providence. You walk the right path now captain.\" " + HeOrShe + " says with righteous words.");
        text.addPara("You feel a wave of inspiration pass through you.", g, h, "", "");

        Global.getSector().getFaction(Factions.PLAYER).adjustRelationship(market.getFactionId(), 0.15f);
        person.getRelToPlayer().adjustRelationship(0.10f, RepLevel.COOPERATIVE);
        //add sp
        Global.getSector().getPlayerStats().setStoryPoints(Global.getSector().getPlayerStats().getStoryPoints() + 8);

        text.setFontSmallInsignia();
        //acquire text
        text.addPara("the Unlimited Production Chip is destroyed", g, r, "destroyed", "");

        text.addPara("Gained 8 Story points", g, s, "8 Story points", "");
        text.addPara("Relationship with " + market.getFaction().getDisplayNameWithArticle() + " improved by 15", g, gr, "15", "");
        text.addPara("Relationship with " + person.getName().getFullName() + " improved by 10", g, gr, "10", "");

        //contact
        if (!ContactIntel.playerHasIntelItemForContact(person)) {
            ContactIntel.addPotentialContact(1f, person, dialog.getInteractionTarget().getMarket(), text);
        }

        //sound
        util.playUiRepRaiseNoise();

        //mad
        makeMad(text, g, r);

        dialog.getOptionPanel().clearOptions();

        text.setFontInsignia();
        addOption("Leave", "nskr_altEndingExitAgree");
        showOptions();
    }

    protected static void makeMad(TextPanelAPI text, Color g , Color r){

        //remove important
        if (questUtil.asteriaOrOutpost()!=null) {
            questUtil.asteriaOrOutpost().getMemory().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        }
        if (questUtil.getElizaLoc()!=null){
            questUtil.getElizaLoc().getMemory().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        }
        //remove contact
        if(util.getAlice()!=null) {
            util.getAlice().getRelToPlayer().adjustRelationship(-0.50f, RepLevel.HOSTILE);
            if(ContactIntel.getContactIntel(util.getAlice())!=null) {
                ContactIntel.getContactIntel(util.getAlice()).setState(ContactIntel.ContactState.SUSPENDED);
            }
        }
        if(util.getJack()!=null) {
            util.getJack().getRelToPlayer().adjustRelationship(-0.50f, RepLevel.HOSTILE);
            if(ContactIntel.getContactIntel(util.getJack())!=null) {
                ContactIntel.getContactIntel(util.getJack()).setState(ContactIntel.ContactState.SUSPENDED);
            }
        }
        //-rep
        if(util.getEliza()!=null) {
            util.getEliza().getRelToPlayer().adjustRelationship(-0.50f, RepLevel.HOSTILE);
        }
        //kesteven rep
        float repKesteven = mathUtil.getSeededRandomNumberInRange(-0.65f, -0.55f, getRandom());
        if(Global.getSector().getFaction(Factions.PLAYER).getRelationship("kesteven")>repKesteven){
            Global.getSector().getFaction(Factions.PLAYER).setRelationship("kesteven", repKesteven);
            repKesteven = Math.round(repKesteven*100f);
            text.addPara("Relationship with Kesteven reduced to "+(int)repKesteven,g,r,""+(int)repKesteven,"");
        }

        //FINISH
        questUtil.setCompleted(true, DIALOG_FINISHED_KEY);
        questUtil.setStage(20);

        questUtil.saveEnding();
    }

    protected void setSecond(){

        questUtil.setCompleted(true, SECOND_TIME_KEY);
    }

    protected boolean validEntity(SectorEntityToken entity) {
        if (entity==null) return false;
        MarketAPI market = entity.getMarket();
        if (market==null) return false;
        if (market.getFactionId()==null) return false;
        //we don't have it anymore
        if (questUtil.getCompleted(questStageManager.ELIZA_INTERCEPT_HANDED_OVER)) return false;
        //person locked
        if (questUtil.getCompleted(SECOND_TIME_KEY) && !person.getMemory().contains(PERSON_LOCKED_KEY)) return false;
        //DONE
        if (questUtil.getCompleted(DIALOG_FINISHED_KEY)) return false;
        //pick correct faction market
        return market.getFactionId().equals(Factions.LUDDIC_PATH) || market.getFactionId().equals(Factions.LUDDIC_CHURCH);
    }

    public static Random getRandom() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

            data.put(PERSISTENT_RANDOM_KEY, new Random(util.getSeedParsed()));
        }
        return (Random) data.get(PERSISTENT_RANDOM_KEY);
    }
}
