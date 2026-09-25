//////////////////////
//Initially created by Histidine and modified from Nexelerin
//////////////////////
package lostsector.dialogue.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import lostsector.campaign.kesteven.quest.QuestStageManager;
import lostsector.campaign.kesteven.quest.QuestHelper;
import lostsector.helper.MathHelper;
import lostsector.helper.SectorLookup;

import java.util.List;
import java.util.*;
import java.util.regex.Matcher;

public class nskr_modRemoval extends BaseCommandPlugin {

    public static final String PERSISTENT_RANDOM_KEY = "nskr_modRemovalRandom";

    public static final String SHIP_IN_MEMORY_KEY = "nskr_modRemovalShipInMemory";

    // Local display values for rules text, written with expiry 0.
    public static final String SHIP_NAME_KEY = "$nskr_modRemoval_shipName";
    public static final String HULL_NAME_KEY = "$nskr_modRemoval_hullName";
    public static final String SMOD_COUNT_KEY = "$nskr_modRemoval_sMods";
    public static final String SMOD_NAME_KEY = "$nskr_modRemoval_sModName";
    public static final String REMOVED_COUNT_KEY = "$nskr_modRemoval_removed";

    public static final String SHIP_PICKED_TRIGGER = "nskr_modRemovalShipPicked";
    public static final String PICK_CANCELLED_TRIGGER = "nskr_modRemovalPickCancelled";
    public static final String SMOD_LINE_TRIGGER = "nskr_modRemovalSModLine";

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
            case "hasShips":
                return !getShipsWithSmods().isEmpty();
            case "getHulls":
                showShipPicker();
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

    protected void showShipPicker() {
        dialog.showFleetMemberPickerDialog("Pick from your fleet",
                Misc.ucFirst("confirm"),
                Misc.ucFirst("cancel"),
                5, 6, 120,true,false, getShipsWithSmods(), new FleetMemberPickerListener() {
                    // Picker callbacks run after the command has returned, so they fire the rules triggers themselves.
                    @Override
                    public void pickedFleetMembers(List<FleetMemberAPI> members) {
                        if (members.isEmpty()){
                            FireBest.fire(null, dialog, memoryMap, PICK_CANCELLED_TRIGGER);
                            return;
                        }
                        FleetMemberAPI f = members.get(0);

                        setShipFromMemory(f);
                        writeShipTokens(f);
                        memoryMap.get(MemKeys.LOCAL).set(SMOD_COUNT_KEY, f.getVariant().getSMods().size() + "", 0);
                        FireBest.fire(null, dialog, memoryMap, SHIP_PICKED_TRIGGER);
                    }

                    @Override
                    public void cancelledFleetMemberPicking() {
                        FireBest.fire(null, dialog, memoryMap, PICK_CANCELLED_TRIGGER);
                    }
                });
    }

    /**
     * Shows one rules line per S-mod on the picked ship.
     */
    protected void prepareToRemove() {
        writeShipTokens(targetShip);
        MemoryAPI local = memoryMap.get(MemKeys.LOCAL);
        for (String s : targetShip.getVariant().getSMods()){
            local.set(SMOD_NAME_KEY, Global.getSettings().getHullModSpec(s).getDisplayName(), 0);
            FireBest.fire(null, dialog, memoryMap, SMOD_LINE_TRIGGER);
        }
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

        writeShipTokens(targetShip);
        memoryMap.get(MemKeys.LOCAL).set(REMOVED_COUNT_KEY, count + "", 0);

        Global.getSoundPlayer().playUISound("ui_char_spent_story_point",1f,1f);
    }

    protected void writeShipTokens(FleetMemberAPI member) {
        MemoryAPI local = memoryMap.get(MemKeys.LOCAL);
        // Rules text replacement passes values to String.replaceAll, and players name their ships.
        local.set(SHIP_NAME_KEY, Matcher.quoteReplacement(member.getShipName()), 0);
        local.set(HULL_NAME_KEY, member.getHullSpec().getHullName(), 0);
    }

    //Alice
    public static boolean validMarket(MarketAPI market) {
        if (market==null) return false;
        if (Global.getSector().getPlayerFaction().getRelationship("kesteven")<=-0.5f) return false;
        if (SectorLookup.asteriaOrOutpost()==null) return false;
        if (QuestHelper.getCompleted(QuestStageManager.ELIZA_INTERCEPT_HANDED_OVER) || QuestHelper.getCompleted(nskr_altEndingDialogLuddic.DIALOG_FINISHED_KEY)) return false;

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

