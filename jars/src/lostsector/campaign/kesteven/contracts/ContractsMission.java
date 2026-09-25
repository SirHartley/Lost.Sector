package lostsector.campaign.kesteven.contracts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import lostsector.campaign.kesteven.contracts.ContractIntel;
import lostsector.campaign.kesteven.contracts.ContractInfo;
import lostsector.campaign.kesteven.contracts.ContractManager;

import java.util.List;
import java.util.Map;

public class ContractsMission extends BaseHubMission {
    //
    //Lightly based on code by Histidine
    //

    // The hub mission ends when the offer is accepted; ContractIntel tracks the contract from then on.
    public enum Stage {
        COMPLETED
    }

    public static final String CONTRACT_KEY_ELIMINATE = "nskr_contractsEliminate";
    public static final String CONTRACT_KEY_RECOVERY = "nskr_contractsRecovery";
    public static final String PERSISTENT_RANDOM_KEY_ELIMINATE = "nskr_contractsEliminateRandomKey";
    public static final String PERSISTENT_RANDOM_KEY_RECOVERY = "nskr_contractsRecoveryRandomKey";

    // Offer text tokens and row conditions for the nskr_contracts rules rows
    private static final String TYPE_KEY = "$nskr_contracts_type";
    private static final String FACTION_BOUNTY_KEY = "$nskr_contracts_factionBounty";
    private static final String COUNT_KEY = "$nskr_contracts_count";
    private static final String TARGETS_KEY = "$nskr_contracts_targets";
    private static final String REWARD_PER_KEY = "$nskr_contracts_rewardPer";
    private static final String REWARD_TOTAL_KEY = "$nskr_contracts_rewardTotal";

    private PersonAPI person;
    private MarketAPI market;
    private ContractInfo contract = null;
    private List<ContractInfo> contracts;

    static void log(final String message) {
        Global.getLogger(ContractsMission.class).info(message);
    }

    public ContractsMission(){

        //create the contracts
        if (getContract(CONTRACT_KEY_ELIMINATE)==null){
            setContract(CONTRACT_KEY_ELIMINATE, new ContractInfo(ContractInfo.ContractType.ELIMINATE, ContractManager.getRandom(PERSISTENT_RANDOM_KEY_ELIMINATE)));
        }
        if (getContract(CONTRACT_KEY_RECOVERY)==null){
            setContract(CONTRACT_KEY_RECOVERY, new ContractInfo(ContractInfo.ContractType.SCAVENGE, ContractManager.getRandom(PERSISTENT_RANDOM_KEY_RECOVERY)));
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
        setSuccessStage(Stage.COMPLETED);
        // ContractIntel grants the reputation when the contract is fulfilled; the hub's success must not add its own.
        setNoRepChanges();

        if (person.getId().equals("nskr_opguy")){
            contract = getContract(CONTRACT_KEY_ELIMINATE);
            return !ContractManager.maxContracts(contracts, ContractInfo.ContractType.ELIMINATE);
        } else {
            contract = getContract(CONTRACT_KEY_RECOVERY);
            return !ContractManager.maxContracts(contracts, ContractInfo.ContractType.SCAVENGE);
        }
    }

    @Override
    protected void updateInteractionDataImpl() {
        // this is weird - in the accept() method, the mission is aborted, which unsets
        // $sShip_ref. So: we use $Contracts_ref2 in the ContactPostAccept rule
        // and $Contracts_ref2 has an expiration of 0, so it'll get unset on its own later.
        set("$nskr_contracts_ref2", this);

        set(TYPE_KEY, contract.type);
        set(FACTION_BOUNTY_KEY, contract.isFactionBounty);
        set(COUNT_KEY, String.valueOf(contract.count));
        set(TARGETS_KEY, getTargetsText());
        set(REWARD_PER_KEY, Misc.getDGSCredits(contract.rewardPer));
        set(REWARD_TOTAL_KEY, Misc.getDGSCredits(contract.totalReward));
    }

    private String getTargetsText() {
        String targets = ContractManager.getTypeString(contract);
        if (contract.type == ContractInfo.ContractType.ELIMINATE) return targets;
        // getUnitsString starts with the space before the unit words; the row writes that space itself.
        return ContractManager.getUnitsString(contract).substring(1) + targets;
    }

    @Override
    protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
                                 Map<String, MemoryAPI> memoryMap) {

        switch (action) {
            case "showPerson":
                dialog.getVisualPanel().showPersonInfo(getPerson(), true);
                return true;
        }

        return super.callAction(action, ruleId, dialog, params, memoryMap);
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

        if (contract.type== ContractInfo.ContractType.ELIMINATE) {
            setContract(CONTRACT_KEY_ELIMINATE, null);
        } else {
            setContract(CONTRACT_KEY_RECOVERY, null);
        }

        // super.accept() is not called: it would add this mission as intel. The null dialog keeps endSuccess from
        // printing the mission's end update into the text panel.
        setCurrentStage(Stage.COMPLETED, null, null);
    }

    @Override
    protected void notifyEnded(){
        super.notifyEnded();

        if (contract.type== ContractInfo.ContractType.ELIMINATE) {
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