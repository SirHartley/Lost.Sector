package scripts.kissa.LOST_SECTOR.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class nskr_makeHostile extends PaginatedOptions {

    //

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

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String arg = params.get(0).getString(memoryMap);
        boolean booleanArg = false;
        if (params.size() > 1) {
            booleanArg = Boolean.parseBoolean(params.get(1).getString(memoryMap));
        }
        setupVars(dialog, memoryMap);

        switch (arg) {
            case "init":
                break;
            case "hasOption":
                return validEntity(entity);
            case "hostile":
                makeHostile();
                break;
        }
        return true;
    }

    /**
     * To be called only when paginated dialog options are required.
     * Otherwise we get nested dialogs that take multiple clicks of the exit option to actually exit.
     *
     * @param dialog
     */
    protected void setupDelegateDialog(InteractionDialogAPI dialog) {
        originalPlugin = dialog.getPlugin();

        dialog.setPlugin(this);
        init(dialog);
    }

    protected void setupVars(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
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
    }

    protected void makeHostile() {
        SectorEntityToken fleet = dialog.getInteractionTarget();

        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);

    }

    public static boolean validEntity(SectorEntityToken entity) {
        if (entity == null) return false;
        if (entity instanceof CampaignFleetAPI) return true;
        return false;
    }

}
