package lostsector.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import lostsector.campaign.intel.ContractIntel;
import lostsector.campaign.quests.jobs.ContractInfo;
import lostsector.campaign.quests.jobs.ContractManager;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class Contracts extends BaseHubMission {
    //
    //Lightly based on code by Histidine
    //

    public static final String CONTRACT_KEY_ELIMINATE = "nskr_contractsEliminate";
    public static final String CONTRACT_KEY_RECOVERY = "nskr_contractsRecovery";
    public static final String PERSISTENT_RANDOM_KEY_ELIMINATE = "nskr_contractsEliminateRandomKey";
    public static final String PERSISTENT_RANDOM_KEY_RECOVERY = "nskr_contractsRecoveryRandomKey";

    private PersonAPI person;
    private MarketAPI market;
    private ContractInfo contract = null;
    private List<ContractInfo> contracts;

    static void log(final String message) {
        Global.getLogger(Contracts.class).info(message);
    }

    public Contracts(){

        //create the contracts
        if (getContract(CONTRACT_KEY_ELIMINATE)==null){
            setContract(CONTRACT_KEY_ELIMINATE, new ContractInfo(ContractInfo.contractType.ELIMINATE, ContractManager.getRandom(PERSISTENT_RANDOM_KEY_ELIMINATE)));
        }
        if (getContract(CONTRACT_KEY_RECOVERY)==null){
            setContract(CONTRACT_KEY_RECOVERY, new ContractInfo(ContractInfo.contractType.SCAVENGE, ContractManager.getRandom(PERSISTENT_RANDOM_KEY_RECOVERY)));
        }
    }

    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        person = getPerson();
        if (person == null) return false;
        if (person.getFaction().isPlayerFaction()) return false;
        market = person.getMarket();
        if (market == null) return false;

        contracts = ContractManager.getContracts(ContractManager.CONTRACT_ARRAY_KEY);

        if (!setPersonMissionRef(person, "$nskr_contracts_ref")) {
            return false;
        }
        setPostingLocation(market.getPrimaryEntity());

        if (person.getId().equals("nskr_opguy")){
            contract = getContract(CONTRACT_KEY_ELIMINATE);
            return !ContractManager.maxContracts(contracts, ContractInfo.contractType.ELIMINATE);
        } else {
            contract = getContract(CONTRACT_KEY_RECOVERY);
            return !ContractManager.maxContracts(contracts, ContractInfo.contractType.SCAVENGE);
        }
    }

    @Override
    protected void updateInteractionDataImpl() {
        // this is weird - in the accept() method, the mission is aborted, which unsets
        // $sShip_ref. So: we use $Contracts_ref2 in the ContactPostAccept rule
        // and $Contracts_ref2 has an expiration of 0, so it'll get unset on its own later.
        set("$nskr_contracts_ref2", this);
    }

    @Override
    protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
                                 Map<String, MemoryAPI> memoryMap) {

        switch (action) {
            case "showBlurb":
                showBlurb(dialog);
                return true;
            case "showContract":
                showContract(dialog);
                return true;
            case "showPerson":
                dialog.getVisualPanel().showPersonInfo(getPerson(), true);
                return true;
        }

        return super.callAction(action, ruleId, dialog, params, memoryMap);
    }

    private void showContract(InteractionDialogAPI dialog) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();

        TextPanelAPI text = dialog.getTextPanel();

        if (contract.type== ContractInfo.contractType.ELIMINATE){
            String hostileStr = " ";
            if (!contract.isFactionBounty) hostileStr = " hostile ";

            text.addPara("\"The powers that be have authorized mercenary contracts for the destruction of enemy assets.\"");
            text.addPara("\"And as it happens we have a new elimination contract available, it would require the destruction of " +
                    contract.count + hostileStr + ContractManager.getTypeString(contract)+".\"", tc, h, contract.count+"", "");
            text.addPara("\"The payout per target vessel is "+Misc.getDGSCredits(contract.rewardPer)+" for a total of "+Misc.getDGSCredits(contract.totalReward)+". " +
                    "You will be paid upon the full completion of the contract.\"", tc, h, Misc.getDGSCredits(contract.rewardPer), Misc.getDGSCredits(contract.totalReward));
            text.addPara("\"Are you interested captain?\"");

        } else {
            String units = ContractManager.getUnitsString(contract);

            text.addPara("\"Our department has great interest in salvage records and materials breakdowns, we are willing to pay for data on certain recovered resources. " +
                    "From destroyed vessels - to be specific.\"");
            text.addPara("\"And as it happens we have a new data recovery contract available, it would require the recovery of " +
                    contract.count+ units + ContractManager.getTypeString(contract)+".\"", tc, h, contract.count+"", "");
            text.addPara("\"The payout per unit recovered is "+Misc.getDGSCredits(contract.rewardPer)+" for a total of "+Misc.getDGSCredits(contract.totalReward)+". " +
                    "You will be paid upon the full completion of the contract.\"", tc, h, Misc.getDGSCredits(contract.rewardPer), Misc.getDGSCredits(contract.totalReward));
            text.addPara("\"Of course we are interested in the data only, you get to keep whatever materials you recover.\"");
            text.addPara("\"Are you willing to do this?\"");

        }

        dialog.getOptionPanel().setShortcut("contact_decline", Keyboard.KEY_ESCAPE, false, false, false, false);
    }

    private void showBlurb(InteractionDialogAPI dialog) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();

        TextPanelAPI text = dialog.getTextPanel();

        if (contract.type== ContractInfo.contractType.ELIMINATE){
            text.addPara("\"The board has authorized an elimination contract on certain enemy vessels. Looks like they want to thin out the competition.\"");

        } else {
            text.addPara("\"We are looking for someone to fulfill our new data recovery contract. The trends deduced from our existing data have already proven invaluable for our efforts.\"");

        }
    }

    @Override
    public String getBaseName() {
        return "Contract";
    }

    @Override
    public void accept(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        //intel
        Global.getSector().getIntelManager().addIntel(new ContractIntel(contract, market, person), false);

        //save to mem
        contracts.add(contract);
        ContractManager.setContracts(contracts, ContractManager.CONTRACT_ARRAY_KEY);

        if (contract.type== ContractInfo.contractType.ELIMINATE) {
            setContract(CONTRACT_KEY_ELIMINATE, null);
        } else {
            setContract(CONTRACT_KEY_RECOVERY, null);
        }

        currentStage = new Object(); // so that the abort() assumes the mission was successful
        abort();
    }

    @Override
    protected void notifyEnded(){
        super.notifyEnded();

        if (contract.type== ContractInfo.contractType.ELIMINATE) {
            setContract(CONTRACT_KEY_ELIMINATE, null);
        } else {
            setContract(CONTRACT_KEY_RECOVERY, null);
        }
    }

    public static ContractInfo getContract(String id){
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (data.containsKey(id)){
            return (ContractInfo) data.get(id);
        }
        return null;
    }
    public static ContractInfo setContract(String id, ContractInfo contract){
        Map<String, Object> data = Global.getSector().getPersistentData();

        data.put(id, contract);
        return (ContractInfo) data.get(id);
    }

}