package lostsector.dialogue.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.starts.thronesgift.ThronesGiftManager;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class nskr_thronesGift extends BaseCommandPlugin {

    public static final String PICK_TRIGGER = "nskr_thronesGiftPick";
    public static final String PICKED_TRIGGER = "nskr_thronesGiftPicked";
    public static final String PICK_CANCELLED_TRIGGER = "nskr_thronesGiftPickCancelled";

    public static final String SHIP_ID_KEY = "$nskr_thronesGift_shipId";
    public static final String SHIP_NAME_KEY = "$nskr_thronesGift_shipName";
    public static final String HULL_NAME_KEY = "$nskr_thronesGift_hullName";
    public static final String COST_KEY = "$nskr_thronesGift_cost";
    public static final String POINTS_KEY = "$nskr_thronesGift_points";

    static void log(final String message) {
        Global.getLogger(nskr_thronesGift.class).info(message);
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;
        MemoryAPI local = memoryMap.get(MemKeys.LOCAL);
        String verb = params.get(0).getString(memoryMap);

        switch (verb) {
            case "hasShips":
                return !ThronesGiftManager.getAutomatableShips().isEmpty();
            case "canAfford": {
                FleetMemberAPI member = getPicked(local);
                return member != null && ThronesGiftManager.getDpAvailable() >= ThronesGiftManager.getAutomationCost(member);
            }
            case "hasPicked":
                return getPicked(local) != null;
            case "pick":
                pick(dialog, memoryMap, params.get(1).getString(memoryMap), params.get(2).getString(memoryMap), params.get(3).getString(memoryMap));
                return true;
            case "automate": {
                FleetMemberAPI member = getPicked(local);
                if (member == null) {
                    log("ERROR null member");
                    return false;
                }
                ThronesGiftManager.automate(member);
                setPoints(local);
                return true;
            }
            case "refreshIntel":
                return refreshIntel(dialog);
        }
        log("ERROR unknown verb " + verb);
        return false;
    }

    private void pick(final InteractionDialogAPI dialog, final Map<String, MemoryAPI> memoryMap, String title, String ok, String cancel) {
        final MemoryAPI local = memoryMap.get(MemKeys.LOCAL);
        // the picker replaces the menu until one of the result triggers rebuilds it
        dialog.getOptionPanel().clearOptions();

        dialog.showFleetMemberPickerDialog(title, ok, cancel,
                5, 6, 120, true, false, ThronesGiftManager.getAutomatableShips(), new FleetMemberPickerListener() {
                    @Override
                    public void pickedFleetMembers(List<FleetMemberAPI> members) {
                        if (members.isEmpty()) {
                            cancelledFleetMemberPicking();
                            return;
                        }
                        FleetMemberAPI member = members.get(0);
                        local.set(SHIP_ID_KEY, member.getId(), 0f);
                        local.set(SHIP_NAME_KEY, replacementSafe(member.getShipName()), 0f);
                        local.set(HULL_NAME_KEY, replacementSafe(member.getHullSpec().getHullName()), 0f);
                        local.set(COST_KEY, (int) ThronesGiftManager.getAutomationCost(member) + "", 0f);
                        setPoints(local);
                        FireBest.fire(null, dialog, memoryMap, PICKED_TRIGGER);
                    }

                    @Override
                    public void cancelledFleetMemberPicking() {
                        local.unset(SHIP_ID_KEY);
                        FireBest.fire(null, dialog, memoryMap, PICK_CANCELLED_TRIGGER);
                    }
                });
    }

    // ThronesGiftIntel passes its intel UI in the plugin's custom1 slot. Dismissing the dialog does not redraw the
    // intel panel, so the exit row calls this after DismissDialog.
    private static boolean refreshIntel(InteractionDialogAPI dialog) {
        if (!(dialog.getPlugin() instanceof RuleBasedInteractionDialogPluginImpl)) return false;
        Object ui = ((RuleBasedInteractionDialogPluginImpl) dialog.getPlugin()).getCustom1();
        if (!(ui instanceof IntelUIAPI)) return false;
        ((IntelUIAPI) ui).recreateIntelUI();
        return true;
    }

    private static void setPoints(MemoryAPI local) {
        local.set(POINTS_KEY, (int) ThronesGiftManager.getDpAvailable() + "", 0f);
    }

    // Rules text replacement inserts memory values with String.replaceAll, which reads '$' and '\' in the value as
    // group references and escapes; player-named ships may contain either.
    private static String replacementSafe(String name) {
        return Matcher.quoteReplacement(name);
    }

    private static FleetMemberAPI getPicked(MemoryAPI local) {
        String id = local.getString(SHIP_ID_KEY);
        if (id == null) return null;
        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (id.equals(member.getId())) return member;
        }
        return null;
    }
}
