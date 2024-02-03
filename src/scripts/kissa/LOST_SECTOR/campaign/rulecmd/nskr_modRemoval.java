//////////////////////
//Initially created by Histidine and modified from Nexelerin
//////////////////////
package scripts.kissa.LOST_SECTOR.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import java.awt.*;
import java.util.*;
import java.util.List;

import org.lwjgl.input.Keyboard;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.util.util;
import scripts.kissa.LOST_SECTOR.util.nskr_stringHelper;

public class nskr_modRemoval extends PaginatedOptions {

    public static final String DIALOG_OPTION_PREFIX = "nskr_modRemoval_pick_";
    public static final String PERSISTENT_RANDOM_KEY = "nskr_modRemovalRandom";

    public static final String SHIP_IN_MEMORY_KEY = "nskr_modRemovalShipInMemory";
    private FleetMemberAPI targetShip;

    protected CampaignFleetAPI playerFleet;
    protected SectorEntityToken entity;
    protected MarketAPI market;
    protected FactionAPI playerFaction;
    protected FactionAPI entityFaction;
    protected TextPanelAPI text;
    protected CargoAPI playerCargo;
    protected PersonAPI person;
    protected FactionAPI faction;
    protected ShipAPI ship;

    protected List<String> disabledOpts = new ArrayList<>();

    static void log(final String message) {
        Global.getLogger(nskr_modRemoval.class).info(message);
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap)
    {
        String arg = params.get(0).getString(memoryMap);
        setupVars(dialog, memoryMap);

        switch (arg)
        {
            case "init":
                break;
            case "hasOption":
                return validMarket(entity.getMarket());
            case "getHulls":
                setupDelegateDialog(dialog);
                showOptions();
                startAndShowOptions();
                break;
            case "prepareRemove":
                showOptions();
                prepareToRemove();
                break;
            case "remove":
                showOptions();
                remove();
                break;
        }
        updateOptions();
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
        market = entity.getMarket();
        text = dialog.getTextPanel();

        playerFleet = Global.getSector().getPlayerFleet();
        playerCargo = playerFleet.getCargo();

        playerFaction = Global.getSector().getPlayerFaction();
        entityFaction = entity.getFaction();

        person = dialog.getInteractionTarget().getActivePerson();
        faction = person.getFaction();

        targetShip = getShipFromMemory();
    }

    private FleetMemberAPI getShipFromMemory() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(SHIP_IN_MEMORY_KEY)) {
            return null;
        }
        return (FleetMemberAPI) data.get(SHIP_IN_MEMORY_KEY);
    }

    private FleetMemberAPI setShipFromMemory(FleetMemberAPI ship) {
        Map<String, Object> data = Global.getSector().getPersistentData();

        data.put(SHIP_IN_MEMORY_KEY, ship);
        return (FleetMemberAPI) data.get(SHIP_IN_MEMORY_KEY);
    }

    //@Override
    //public void showOptions() {
    //    super.showOptions();
    //    for (String optId : disabledOpts)
    //    {
    //        dialog.getOptionPanel().setEnabled(optId, false);
    //    }
    //    dialog.getOptionPanel().setShortcut("nskr_modRemovalReturn", Keyboard.KEY_ESCAPE, false, false, false, false);
    //}

    public void updateOptions() {
        for (String optId : disabledOpts)
        {
            dialog.getOptionPanel().setEnabled(optId, false);
        }
        dialog.getOptionPanel().setShortcut("nskr_modRemovalReturn", Keyboard.KEY_ESCAPE, false, false, false, false);
    }

    protected void startAndShowOptions() {
        final Color h = Misc.getHighlightColor();
        final Color g = Misc.getGrayColor();
        final Color tc = Misc.getTextColor();
        text.setFontInsignia();

        if (getShipsWithSmods().isEmpty()){
            text.addParagraph("\"Looks like you don't have any ships with Special Modifications. Quit wasting my time now, will you.\" She groans.");

            dialog.getOptionPanel().addOption("Leave", "nskr_modRemovalExit");
            return;
        }

        // prevents an IllegalAccessError
        final InteractionDialogAPI dialog = this.dialog;

        dialog.showFleetMemberPickerDialog("Pick from your fleet",
                Misc.ucFirst("confirm"),
                Misc.ucFirst("cancel"),
                5, 6, 120,true,false, getShipsWithSmods(), new FleetMemberPickerListener() {
                    @Override
                    public void pickedFleetMembers(List<FleetMemberAPI> members) {
                        //cancelled
                        if (members.isEmpty()){
                            text.addParagraph("\"Come on captain, just make up your mind already. I don't have all day.\" She mutters.");

                            dialog.getOptionPanel().addOption("Go back", "nskr_modRemovalReturn");
                            return;
                        }
                        FleetMemberAPI f = members.get(0);

                        setShipFromMemory(f);
                        text.setFontSmallInsignia();
                        text.addPara("Selected "+f.getShipName()+" "+f.getHullSpec().getHullName()+"-Class", g, h,f.getHullSpec().getHullName(),"");
                        text.setFontInsignia();

                        String sMods = "S-Mods";
                        if (f.getVariant().getSMods().size()==1) sMods = "S-Mod";

                        dialog.getOptionPanel().addOption("Select "+f.getHullSpec().getHullName()+"-Class has "+f.getVariant().getSMods().size()+" "+sMods,"nskr_modRemoval_pick_ship");
                        dialog.getOptionPanel().addOption("Go back", "nskr_modRemovalReturn");

                    }

                    @Override
                    public void cancelledFleetMemberPicking() {
                        text.addParagraph("\"Come on captain, just make up your mind already. I don't have all day.\" She mutters.");

                        dialog.getOptionPanel().addOption("Go back", "nskr_modRemovalReturn");
                    }
                });

        text.setFontInsignia();
    }

    protected void prepareToRemove() {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        Color gr = Misc.getStoryBrightColor();

        text.setFontSmallInsignia();
        for (String s : targetShip.getVariant().getSMods()){
            text.addPara("S-Modded "+Global.getSettings().getHullModSpec(s).getDisplayName(),g,gr,Global.getSettings().getHullModSpec(s).getDisplayName(),"");
        }
        text.setFontInsignia();

        text.addParagraph("Removing all the Special Modifications from the "+targetShip.getHullSpec().getHullName()+" without causing permanent damage would require significant work.");

        dialog.getOptionPanel().addOption("I'm sure you're capable enough.","nskr_modRemoval_confirm");
        dialog.makeStoryOption("nskr_modRemoval_confirm",1,0.75f,"ui_char_spent_story_point");
        //tooltip
        dialog.getOptionPanel().addOptionTooltipAppender("nskr_modRemoval_confirm", new OptionPanelAPI.OptionTooltipCreator() {
            public void createTooltip(TooltipMakerAPI tooltip, boolean hadOtherText) {
                float opad = 10f;
                float initPad = 0f;
                if (hadOtherText) initPad = opad;
                tooltip.addStoryPointUseInfo(initPad, 1, 0.75f, false);
                int sp = Global.getSector().getPlayerStats().getStoryPoints();
                String points = "points";
                if (sp == 1) points = "point";
                tooltip.addPara("You have %s " + Misc.STORY + " " + points + ".", opad,
                        Misc.getStoryOptionColor(), "" + sp);
            }
        });
        //pop up
        dialog.getOptionPanel().addOptionConfirmation("nskr_modRemoval_confirm",
                new SetStoryOption.BaseOptionStoryPointActionDelegate(dialog,
                        new SetStoryOption.StoryOptionParams("nskr_modRemoval_confirm",1,"nskr_modRemoval","ui_char_spent_story_point","Removed S-Mods from a ship")));

        dialog.getOptionPanel().addOption("Go back", "nskr_modRemovalReturn");
        text.setFontInsignia();
    }

    protected void remove() {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        Color gr = Misc.getStoryBrightColor();

        int count = targetShip.getVariant().getSMods().size();
        text.setFontSmallInsignia();
        text.setFontInsignia();

        //remove
        LinkedHashSet<String> sModsCopy = new LinkedHashSet<>(targetShip.getVariant().getSMods());
        for (String s : sModsCopy){
            HullModSpecAPI spec = Global.getSettings().getHullModSpec(s);
            if (spec!=null && spec.getTags().contains("rat_alteration")){
                count -= 1;
                continue;
            }
            targetShip.getVariant().removePermaMod(s);
        }
        text.addParagraph("The hull is offloaded at a dry-dock so the crew can begin work on it.");
        text.addParagraph("After some waiting around Alice finally gets back in contact with you.");
        text.addParagraph("\"It wasn't easy but we pulled it off captain.\" She says with a smug expression.");

        String sMods = "S-Mods";
        if (count==1) sMods = "S-Mod";

        text.setFontSmallInsignia();
        text.addPara(targetShip.getShipName()+" "+targetShip.getHullSpec().getHullName()+"-Class removed "+count+" "+sMods,g,h,count+"","");
        text.setFontInsignia();

        Global.getSoundPlayer().playUISound("ui_char_spent_story_point",1f,1f);

        dialog.getOptionPanel().addOption("Leave", "nskr_modRemovalExit");
        text.setFontInsignia();
    }

    //Alice
    public static boolean validMarket(MarketAPI market) {
        if (market==null) return false;
        if (Global.getSector().getPlayerFaction().getRelationship("kesteven")<=-0.5f) return false;
        if (questUtil.asteriaOrOutpost()==null) return false;
        if (questUtil.getCompleted(questStageManager.ELIZA_INTERCEPT_HANDED_OVER) || questUtil.getCompleted(nskr_altEndingDialogLuddic.DIALOG_FINISHED_KEY)) return false;

        return market==questUtil.asteriaOrOutpost();
    }

    public static List<FleetMemberAPI> getShipsWithSmods(){
        List<FleetMemberAPI> validShips = new ArrayList<>();
        for (FleetMemberAPI f : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()){
            if (f.isFighterWing())continue;
            if (f.getVariant()==null)continue;
            if (f.getVariant().getSMods().isEmpty())continue;
            //alteration check
            if (f.getVariant().getSMods().size()==1){
                boolean alteration = false;
                for (String smod : f.getVariant().getSMods()){
                    HullModSpecAPI spec = Global.getSettings().getHullModSpec(smod);
                    if (spec!=null && spec.getTags().contains("rat_alteration")){
                        alteration = true;
                        break;
                    }
                }
                if (alteration) continue;
            }
            //valid
            validShips.add(f);
        }

        return validShips;
    }

    public static Random getRandom() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

            data.put(PERSISTENT_RANDOM_KEY, new Random(util.getSeedParsed()));
        }
        return (Random)data.get(PERSISTENT_RANDOM_KEY);
    }
}

