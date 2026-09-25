package lostsector.campaign.starts.thronesgift;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.input.Keyboard;

import java.awt.Color;
import java.util.List;
import java.util.Map;

// TODO: replace this menu with a custom UI panel (docs/UI.md).
public class AutomateDialog implements InteractionDialogPlugin {

    private enum Options {
        LEAVE,
        CONFIRM,
        RESELECT
    }

    private final IntelUIAPI info;
    private InteractionDialogAPI dialog;
    private TextPanelAPI text;
    private OptionPanelAPI options;
    // Kept by id: the ship may leave the fleet while the dialog is open.
    private String pickedId;

    public AutomateDialog(IntelUIAPI info) {
        this.info = info;
    }

    static void log(final String message) {
        Global.getLogger(AutomateDialog.class).info(message);
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        this.text = dialog.getTextPanel();
        this.options = dialog.getOptionPanel();

        openAutomateUI();
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (optionText != null) dialog.addOptionSelectedText(optionData);

        if (optionData == Options.CONFIRM) {
            FleetMemberAPI member = getPicked();
            if (member == null) {
                log("ERROR null member");
                dialog.dismiss();
                return;
            }
            automate(member);
        } else if (optionData == Options.RESELECT) {
            openAutomateUI();
        } else if (optionData == Options.LEAVE) {
            dialog.dismiss();
            // Dismissing the dialog does not redraw the intel panel.
            info.recreateIntelUI();
        }
    }

    private void openAutomateUI() {
        final List<FleetMemberAPI> ships = ThronesGiftManager.getAutomatableShips();
        if (ships.isEmpty()) {
            text.addParagraph("You can not automate your only ship.");
            setOptions("Go back", Options.LEAVE);
            return;
        }

        // the picker replaces the menu until its result sets new options
        options.clearOptions();
        dialog.showFleetMemberPickerDialog("Pick from your fleet", "Confirm", "Cancel",
                5, 6, 120, true, false, ships, new FleetMemberPickerListener() {
                    @Override
                    public void pickedFleetMembers(List<FleetMemberAPI> members) {
                        if (members.isEmpty()) {
                            cancelledFleetMemberPicking();
                            return;
                        }
                        showPicked(members.get(0));
                    }

                    @Override
                    public void cancelledFleetMemberPicking() {
                        pickedId = null;
                        text.addParagraph("You selected nothing.");
                        setOptions("Automate another ship", Options.RESELECT, "Go back", Options.LEAVE);
                    }
                });
    }

    private void showPicked(FleetMemberAPI member) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();

        pickedId = member.getId();
        String cost = (int) ThronesGiftManager.getAutomationCost(member) + "";
        String points = (int) ThronesGiftManager.getDpAvailable() + "";
        String hullName = member.getHullSpec().getHullName();

        text.setFontSmallInsignia();
        text.addParagraph("This will cost " + cost + " automation points.", tc);
        text.highlightInLastPara(h, cost);
        text.addParagraph("You have " + points + " automation points available.", tc);
        text.highlightInLastPara(h, points);
        text.addParagraph("Selected " + member.getShipName() + " " + hullName + "-Class", g);
        text.highlightInLastPara(h, hullName);
        text.setFontInsignia();

        if (ThronesGiftManager.getDpAvailable() >= ThronesGiftManager.getAutomationCost(member)) {
            setOptions("Automate " + hullName + "-Class", Options.CONFIRM,
                    "Automate another ship", Options.RESELECT,
                    "Go back", Options.LEAVE);
            dialog.setOptionColor(Options.CONFIRM, h);
            options.setTooltip(Options.CONFIRM, "This will cost " + cost + " automation points.");
            options.addOptionConfirmation(Options.CONFIRM,
                    "Are you sure? This cannot be reversed. It will cost " + cost + " automation points.", "Confirm", "Cancel");
        } else {
            text.addParagraph("You can not afford to automate this ship.");
            text.highlightInLastPara(Global.getSettings().getColor("textEnemyColor"), "can not afford");
            setOptions("Automate another ship", Options.RESELECT, "Go back", Options.LEAVE);
        }
    }

    private void automate(FleetMemberAPI member) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();

        String cost = (int) ThronesGiftManager.getAutomationCost(member) + "";
        String hullName = member.getHullSpec().getHullName();
        ThronesGiftManager.automate(member);

        text.setFontSmallInsignia();
        text.addParagraph("Automated " + member.getShipName() + " " + hullName + "-Class", g);
        text.highlightInLastPara(h, hullName);
        text.addParagraph("Lost " + cost + " automation points.", g);
        text.highlightInLastPara(Global.getSettings().getColor("textEnemyColor"), cost);
        text.addParagraph("You now have " + (int) ThronesGiftManager.getDpAvailable() + " automation points.", g);
        text.setFontInsignia();

        Global.getSoundPlayer().playUISound("ui_noise_static", 1f, 1f);

        setOptions("Continue", Options.LEAVE, "Automate another ship", Options.RESELECT);
    }

    // Every screen shows the leave option, which also answers Escape.
    private void setOptions(Object... labelsAndIds) {
        options.clearOptions();
        for (int i = 0; i < labelsAndIds.length; i += 2) {
            options.addOption((String) labelsAndIds[i], labelsAndIds[i + 1]);
        }
        options.setShortcut(Options.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
    }

    private FleetMemberAPI getPicked() {
        if (pickedId == null) return null;
        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (pickedId.equals(member.getId())) return member;
        }
        return null;
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
    }

    @Override
    public void advance(float amount) {
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
