//////////////////////
//Initially created by Histidine and modified from Nexelerin
//////////////////////
package lostsector.dialogue.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import lostsector.campaign.kesteven.quest.KestevenQuest;
import lostsector.helper.MathHelper;
import lostsector.helper.SectorLookup;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.*;

// TODO: replace this menu with a custom UI panel (docs/UI.md).
public class nskr_modRemoval extends BaseCommandPlugin {

    public static final String PERSISTENT_RANDOM_KEY = "nskr_modRemovalRandom";

    public static final String SHIP_IN_MEMORY_KEY = "nskr_modRemovalShipInMemory";

    public static final String PICK_OPTION = "nskr_modRemoval_pick_ship";
    public static final String CONFIRM_OPTION = "nskr_modRemoval_confirm";
    public static final String RETURN_OPTION = "nskr_modRemovalReturn";
    public static final String EXIT_OPTION = "nskr_modRemovalExit";

    private FleetMemberAPI targetShip;

    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;
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
                if (getShipsWithSmods().isEmpty()) {
                    text.addParagraph("\"Looks like you don't have any ships with Special Modifications. Quit wasting my time now, will you.\" She groans.");
                    setOptions(EXIT_OPTION, "Leave", EXIT_OPTION);
                } else {
                    showShipPicker();
                }
                break;
            case "prepareRemove":
                prepareToRemove();
                break;
            case "remove":
                remove();
                break;
        }
        return true;
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

    // The handler rows of these screens have no options of their own, so the command replaces the menu.
    protected void setOptions(String escapeOption, String... labelsAndIds) {
        OptionPanelAPI options = dialog.getOptionPanel();
        options.clearOptions();
        for (int i = 0; i < labelsAndIds.length; i += 2) {
            options.addOption(labelsAndIds[i], labelsAndIds[i + 1]);
        }
        options.setShortcut(escapeOption, Keyboard.KEY_ESCAPE, false, false, false, true);
    }

    protected void showPickCancelled() {
        text.addParagraph("\"Come on captain, just make up your mind already. I don't have all day.\" She mutters.");
        setOptions(RETURN_OPTION, "Go back", RETURN_OPTION);
    }

    protected void showShipPicker() {
        dialog.showFleetMemberPickerDialog("Pick from your fleet",
                Misc.ucFirst("confirm"),
                Misc.ucFirst("cancel"),
                5, 6, 120,true,false, getShipsWithSmods(), new FleetMemberPickerListener() {
                    // Picker callbacks run after the command has returned, so they print the result and set the options themselves.
                    @Override
                    public void pickedFleetMembers(List<FleetMemberAPI> members) {
                        if (members.isEmpty()){
                            showPickCancelled();
                            return;
                        }
                        FleetMemberAPI f = members.get(0);

                        setShipFromMemory(f);
                        String hullName = f.getHullSpec().getHullName();
                        text.setFontSmallInsignia();
                        text.addParagraph("Selected " + f.getShipName() + " " + hullName + "-Class", Misc.getGrayColor());
                        text.highlightInLastPara(hullName);
                        text.setFontInsignia();

                        int sMods = f.getVariant().getSMods().size();
                        setOptions(RETURN_OPTION,
                                "Select " + hullName + "-Class has " + sMods + (sMods == 1 ? " S-Mod" : " S-Mods"), PICK_OPTION,
                                "Go back", RETURN_OPTION);
                    }

                    @Override
                    public void cancelledFleetMemberPicking() {
                        showPickCancelled();
                    }
                });
    }

    protected void prepareToRemove() {
        for (String s : targetShip.getVariant().getSMods()){
            String name = Global.getSettings().getHullModSpec(s).getDisplayName();
            text.setFontSmallInsignia();
            text.addParagraph("S-Modded " + name, Misc.getGrayColor());
            text.highlightInLastPara(Misc.getStoryOptionColor(), name);
            text.setFontInsignia();
        }
        text.addParagraph("Removing all the Special Modifications from the " + targetShip.getHullSpec().getHullName() + " without causing permanent damage would require significant work.");

        setOptions(RETURN_OPTION,
                "I'm sure you're capable enough.", CONFIRM_OPTION,
                "Go back", RETURN_OPTION);
        SetStoryOption.set(dialog, 1, CONFIRM_OPTION, "nskr_modRemoval", "ui_char_spent_story_point", "Removed S-Mods from a ship");
    }

    protected void remove() {
        int count = targetShip.getVariant().getSMods().size();

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

        Global.getSoundPlayer().playUISound("ui_char_spent_story_point",1f,1f);

        text.addParagraph("The hull is offloaded at a dry-dock so the crew can begin work on it.");
        text.addParagraph("After some waiting around Alice finally gets back in contact with you.");
        text.addParagraph("\"It wasn't easy but we pulled it off captain.\" She says with a smug expression.");
        text.setFontSmallInsignia();
        text.addParagraph(targetShip.getShipName() + " " + targetShip.getHullSpec().getHullName() + "-Class removed " + count + (count == 1 ? " S-Mod" : " S-Mods"), Misc.getGrayColor());
        text.highlightInLastPara(count + "");
        text.setFontInsignia();

        setOptions(EXIT_OPTION, "Leave", EXIT_OPTION);
    }

    //Alice
    public static boolean validMarket(MarketAPI market) {
        if (market==null) return false;
        if (Global.getSector().getPlayerFaction().getRelationship("kesteven")<=-0.5f) return false;
        if (SectorLookup.asteriaOrOutpost()==null) return false;
        if (KestevenQuest.researchServicesClosed()) return false;

        return market== SectorLookup.asteriaOrOutpost();
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

            data.put(PERSISTENT_RANDOM_KEY, new Random(MathHelper.getSeedParsed()));
        }
        return (Random)data.get(PERSISTENT_RANDOM_KEY);
    }
}

