package lostsector.dialogue.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import lostsector.campaign.kesteven.quest.QuestStageManager;
import lostsector.campaign.kesteven.quest.QuestHelper;
import lostsector.campaign.kesteven.quest.KestevenFlag;
import lostsector.campaign.kesteven.quest.KestevenPeople;
import lostsector.campaign.kesteven.quest.KestevenQuest;
import lostsector.campaign.kesteven.quest.KestevenState;
import lostsector.helper.SectorLookup;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class nskr_elizaInterceptDialog extends PaginatedOptions {

    //


    private boolean handed = false;
    private boolean finishedKesteven = false;
    private boolean finishedAlt = false;

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

    static void log(final String message) {
        Global.getLogger(nskr_elizaInterceptDialog.class).info(message);
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap)
    {
        String arg = params.get(0).getString(memoryMap);
        boolean booleanArg = false;
        if (params.size()>1) {
            booleanArg = Boolean.parseBoolean(params.get(1).getString(memoryMap));
        }
        setupVars(dialog, memoryMap);

        switch (arg)
        {
            case "init":
                break;
            case "hasOption":
                return validEntity(entity);
            case "addOptions":
                addOptions();
                showOptions();
                break;
            case "handOver":
                handOver();
                break;
            case "aggro":
                makeAggro();
                break;
            case "hostile":
                makeHostile();
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

        playerFleet = Global.getSector().getPlayerFleet();
        playerCargo = playerFleet.getCargo();

        playerFaction = Global.getSector().getPlayerFaction();
        entityFaction = entity.getFaction();

        player = Global.getSector().getPlayerPerson();
        person = dialog.getInteractionTarget().getActivePerson();

        handed = QuestHelper.getCompleted(KestevenFlag.CHIP_HANDED_TO_ELIZA);
        finishedKesteven = QuestHelper.getCompleted(KestevenFlag.KESTEVEN_ENDING_DONE);
        finishedAlt = QuestHelper.getCompleted(KestevenFlag.ALT_ENDING_DONE);
    }

    @Override
    public void showOptions() {
        super.showOptions();
        for (String optId : disabledOpts)
        {
            dialog.getOptionPanel().setEnabled(optId, false);
        }
    }

    protected void addOptions(){
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;
        //talked to check
        QuestHelper.setCompleted(true, KestevenFlag.ELIZA_INTERCEPT_TALKED);

        text.addPara("\"I can tell you have the UPC captain.\" She says sharply. \"I have eyes and ears all across the sector, tracking every one of your fascinating moves.\"");
        text.addPara("\"Now can you hand the Chip over, like we agreed.\"");
        text.addPara("\"You haven't had a change of heart by chance?\" She stares deeply into your eyes while waiting for a response.");

        //can hand over check
        if (!finishedKesteven && !finishedAlt) {
            addOption("Give her the UPC", "nskr_elizaInterceptDialogHandOver");
            addOption("\"No, I'd rather not.\"", "nskr_elizaInterceptDialogExitFight");
        } else {
            addOption("\"I don't have it anymore.\"", "nskr_elizaInterceptDialogExitFightNoChip");
        }
        text.setFontInsignia();
    }

    protected void handOver() {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        text.addPara("A shuttle quickly delivers the UPC over to Eliza's flagship. \"Oh yes captain, you've done an invaluable deed for the cause. Our time has come indeed.\" An unsettling grin develops on her face.");
        text.addPara("\"Now come visit me at "+ QuestHelper.getElizaLoc().getMarket().getName()+" to reap your reward.\"");

        //set completed
        QuestHelper.setCompleted(true, KestevenFlag.CHIP_HANDED_TO_ELIZA);

        //rep
        KestevenPeople.getEliza().getRelToPlayer().adjustRelationship(0.05f, RepLevel.COOPERATIVE);
        if(Global.getSector().getFaction(Factions.PLAYER).getRelationship("kesteven")>-0.35f) {
            Global.getSector().getFaction(Factions.PLAYER).setRelationship("kesteven", -0.35f);
        }
        //remove important
        dialog.getInteractionTarget().getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        if (SectorLookup.asteriaOrOutpost()!=null) SectorLookup.asteriaOrOutpost().getMemory().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        //add important to base
        QuestHelper.getElizaLoc().getMemory().set(MemFlags.MEMORY_KEY_MISSION_IMPORTANT, true);

        //make neutral
        SectorEntityToken fleet = dialog.getInteractionTarget();

        fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE);
        fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE);

        Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);
        //loss text
        text.setFontSmallInsignia();
        text.addPara("Lost the Unlimited Production Chip",g,r,"Lost","");
        text.addPara("Relationship with Eliza improved by 5",g,gr,"5","");
        text.setFontInsignia();
    }

    protected void makeHostile() {
        SectorEntityToken fleet = dialog.getInteractionTarget();

        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
        //remove important
        dialog.getInteractionTarget().getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);

        //-rep
        KestevenPeople.getEliza().getRelToPlayer().adjustRelationship(-0.75f, RepLevel.VENGEFUL);

        Global.getSoundPlayer().playUISound("ui_rep_drop",1f,1f);
    }

    protected void makeAggro() {
        SectorEntityToken fleet = dialog.getInteractionTarget();

        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
    }

    public static boolean validEntity(SectorEntityToken entity) {
        if (entity==null) return false;
        //pick correct fleet
        if (!entity.getMemory().contains(QuestStageManager.ELIZA_INTERCEPT_FLEET_KEY)) return false;

        return entity.getFaction().getId().equals(Factions.MERCENARY);
    }

    public static Random getRandom() {
        return KestevenQuest.random(KestevenState.RANDOM_ELIZA_INTERCEPT);
    }

}

