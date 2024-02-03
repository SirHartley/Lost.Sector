package scripts.kissa.LOST_SECTOR.campaign.customStart.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.customStart.thronesGiftManager;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.nskr_stringHelper;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class automateDialog implements InteractionDialogPlugin {

    public static final String SHIP_IN_MEMORY_KEY = "automateDialogShipInMemory";

    private float points;
    private TextPanelAPI text;
    private InteractionDialogAPI dialog;
    private OptionPanelAPI options;
    private IntelUIAPI info;

    public automateDialog(IntelUIAPI info) {

        this.info = info;
    }

    static void log(final String message) {
        Global.getLogger(automateDialog.class).info(message);
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        this.text = dialog.getTextPanel();
        this.options = dialog.getOptionPanel();
        this.points = thronesGiftManager.getDpAvailable();

        dialog.setOptionOnEscape("Go back", Options.LEAVE);

        openAutomateUI();
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

        options.clearOptions();

        points = thronesGiftManager.getDpAvailable();

        if (optionData==Options.CONFIRM){
            dialog.setOptionOnEscape("Go back", Options.LEAVE);
            FleetMemberAPI member = getShipFromMemory();
            if (member==null){
                dialog.dismiss();
                log("ERROR null member");
                return;
            }

            //remove captain
            PersonAPI captain = member.getCaptain();
            if (captain!=null){
                member.setCaptain(null);
            }
            //remove normal fighters
            int x = -1;
            for (String s : member.getVariant().getNonBuiltInWings()) {
                x++;
                FighterWingSpecAPI wing = member.getVariant().getWing(x);
                if (wing == null) continue;
                if (!wing.hasTag(Tags.AUTOMATED_FIGHTER)){
                    //back to cargo
                    Global.getSector().getPlayerFleet().getCargo().addItems(CargoAPI.CargoItemType.FIGHTER_CHIP, wing.getId(), 1);

                    member.getVariant().setWingId(x, null);
                }
            }

            text.setFontSmallInsignia();
            member.getVariant().addPermaMod(HullMods.AUTOMATED);
            member.getVariant().addTag(Tags.TAG_AUTOMATED_NO_PENALTY);

            //update the fleet IMPORTANT
            fleetUtil.updatePlayerFleet(true);

            float cost = member.getHullSpec().getSuppliesToRecover();

            text.addPara("Automated "+member.getShipName()+" "+member.getHullSpec().getHullName()+"-Class",g,h,member.getHullSpec().getHullName()+"","");
            text.addPara("Lost "+ (int)cost +" automation points.", g, r, (int)cost+"","");
            //deduct
            thronesGiftManager.setDpAvailable(points-cost);
            float points = thronesGiftManager.getDpAvailable();
            text.addPara("You now have "+ (int)points +" automation points.", g, g, "","");
            text.setFontInsignia();

            util.playUiStaticNoise();

            dialog.getOptionPanel().addOption("Continue", Options.LEAVE);
            dialog.getOptionPanel().addOption("Automate another ship", Options.RESELECT);
        }

        if (optionData==Options.RESELECT){
            openAutomateUI();
        }

        if (optionData==Options.LEAVE){
            dialog.dismiss();
            info.recreateIntelUI();
        }

    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {

    }

    @Override
    public void advance(float amount) {

    }

    private void openAutomateUI() {

        final Color h = Misc.getHighlightColor();
        final Color g = Misc.getGrayColor();
        final Color r = Misc.getNegativeHighlightColor();
        final Color tc = Misc.getTextColor();

        // prevents an IllegalAccessError
        final TextPanelAPI text = this.text;
        final InteractionDialogAPI dialog = this.dialog;

        text.setFontInsignia();

        if (getAutomatableShips().isEmpty()){
            text.addParagraph("You can not automate your only ship.");

            dialog.getOptionPanel().addOption("Go back", Options.LEAVE);
            return;
        }

        dialog.showFleetMemberPickerDialog("Pick from your fleet",
                Misc.ucFirst("confirm"),
                Misc.ucFirst("cancel"),
                5, 6, 120,true,false, getAutomatableShips(), new FleetMemberPickerListener() {
                    @Override
                    public void pickedFleetMembers(java.util.List<FleetMemberAPI> members) {
                        //cancelled
                        if (members.isEmpty()){
                            text.addParagraph("You selected nothing.");

                            dialog.getOptionPanel().addOption("Automate another ship", Options.RESELECT);
                            dialog.getOptionPanel().addOption("Go back", Options.LEAVE);
                            return;
                        }

                        FleetMemberAPI f = members.get(0);
                        setShipFromMemory(f);

                        text.setFontSmallInsignia();
                        float cost = f.getHullSpec().getSuppliesToRecover();
                        text.addPara("This will cost " + (int)cost + " automation points.", tc, h, (int)cost + "", "");
                        text.addPara("You have " + (int)points + " automation points available.", tc, h, (int)points + "", "");

                        text.addPara("Selected "+f.getShipName()+" "+f.getHullSpec().getHullName()+"-Class",g,h,f.getHullSpec().getHullName(),"");
                        text.setFontInsignia();

                        if (points >= cost) {
                            dialog.getOptionPanel().addOption("Automate "+f.getHullSpec().getHullName()+"-Class", Options.CONFIRM, h, "This will cost " + (int)cost + " automation points.");
                            dialog.getOptionPanel().addOption("Automate another ship", Options.RESELECT);
                            dialog.getOptionPanel().addOption("Go back", Options.LEAVE);
                            dialog.getOptionPanel().addOptionConfirmation(Options.CONFIRM, "Are you sure? This cannot be reversed. "+
                                    "It will cost " + (int)cost + " automation points.", "Confirm","Cancel");
                        } else {
                            text.addPara("You can not afford to automate this ship.", tc, r, "can not afford");
                            dialog.getOptionPanel().addOption("Automate another ship", Options.RESELECT);
                            dialog.getOptionPanel().addOption("Go back", Options.LEAVE);
                        }

                    }

                    @Override
                    public void cancelledFleetMemberPicking() {
                        text.addParagraph("You selected nothing.");

                        dialog.getOptionPanel().addOption("Automate another ship", Options.RESELECT);
                        dialog.getOptionPanel().addOption("Go back", Options.LEAVE);
                    }
                });

        text.setFontInsignia();

    }

    private java.util.List<FleetMemberAPI> getAutomatableShips() {
        List<FleetMemberAPI> validShips = new ArrayList<>();
        for (FleetMemberAPI f : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()){
            if (f.isFighterWing())continue;
            if (f.getVariant()==null)continue;
            if (f.getVariant().getHullMods().contains(HullMods.AUTOMATED) || f.getVariant().getHullMods().contains("sotf_sierrasconcord"))continue;
            validShips.add(f);
        }
        //can't automate last ship
        if (validShips.size()==1) return new ArrayList<>();

        return validShips;
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

    private enum Options{
        LEAVE,
        CONFIRM,
        RESELECT,

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
}
