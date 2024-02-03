package scripts.kissa.LOST_SECTOR.campaign.quests.jobs;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import scripts.kissa.LOST_SECTOR.campaign.quests.nskr_EndingKestevenDialog;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.nskr_contracts;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.ids;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class contractManager extends BaseCampaignEventListener implements EveryFrameScript {

    public static final int MAX_ELIM_CONTRACTS = 1;
    public static final int MAX_SCAV_CONTRACTS = 1;
    public static final String CONTRACT_ARRAY_KEY = "$contractManagerContracts";
    //private List<contractInfo> contracts;

    nskr_saved<Float> counter;
    nskr_saved<Float> resetCounter;
    public contractManager() {
        super(false);
        this.counter = new nskr_saved<>("contractManagerCounter", 0.0f);
        this.resetCounter = new nskr_saved<>("contractManagerResetCounter", 0.0f);

        //init randoms
        getRandom(nskr_contracts.PERSISTENT_RANDOM_KEY_ELIMINATE);
        getRandom(nskr_contracts.PERSISTENT_RANDOM_KEY_RECOVERY);
    }

    static void log(final String message) {
        Global.getLogger(contractManager.class).info(message);
    }

    @Override
    public void advance(float amount) {


        if (Global.getSector().isPaused()) return;
        //timer
        if (Global.getSector().isInFastAdvance()) {
            counter.val += 2f*amount;
            resetCounter.val += 2f*amount;
        } else{
            counter.val += amount;
            resetCounter.val += amount;
        }

        List<contractInfo> contracts = getContracts(CONTRACT_ARRAY_KEY);

        if (counter.val>=1f) {
            //checks for contract failure
            //do here so we don't have to do it everyFrame
            for (contractInfo contract : contracts) {
                //fail
                if (questUtil.getEndMissions() || Global.getSector().getPlayerFaction().getRelationship(ids.KESTEVEN_FACTION_ID)<=-0.5f){
                    contract.failed = true;
                }
            }
            setContracts(contracts, CONTRACT_ARRAY_KEY);
            counter.val = 0f;
        }

        //reset offered contracts
        if (resetCounter.val>=600f){
            //reset
            if (nskr_contracts.getContract(nskr_contracts.CONTRACT_KEY_ELIMINATE)!=null){
                nskr_contracts.setContract(nskr_contracts.CONTRACT_KEY_ELIMINATE, null);
            }
            if (nskr_contracts.getContract(nskr_contracts.CONTRACT_KEY_RECOVERY)!=null){
                nskr_contracts.setContract(nskr_contracts.CONTRACT_KEY_RECOVERY, null);
            }

            resetCounter.val = 0f;
        }

    }

    @Override
    public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {
        CampaignFleetAPI loser = plugin.getLoser();
        if (loser == null) return;
        if (loot == null) return;

        List<contractInfo> contracts = getContracts(CONTRACT_ARRAY_KEY);
        //no data on ourselves
        if (loser.getFaction() != Global.getSector().getFaction(ids.KESTEVEN_FACTION_ID)) {
            for (contractInfo contract : contracts) {
                for (CargoStackAPI c : loot.getStacksCopy()) {
                    if (c.getCommodityId() == null) continue;
                    if (c.getCommodityId().equals(contract.subType)) {
                        contract.completedCount += c.getSize();
                    }
                }
            }
        }
    }

    @Override
    public void reportPlayerEngagement(EngagementResultAPI result) {
        EngagementResultForFleetAPI enemy = result.getLoserResult();
        if (enemy.isPlayer()) enemy = result.getWinnerResult();
        if (enemy == null) return;
        if (enemy.getFleet() == null) return;
        ArrayList<FleetMemberAPI> lost = new ArrayList<>();
        lost.addAll(enemy.getDisabled());
        lost.addAll(enemy.getDestroyed());

        List<contractInfo> contracts = getContracts(CONTRACT_ARRAY_KEY);
        for (contractInfo contract : contracts) {
            float completed = 0;
            //normal bounties
            //hostile check
            if (!contract.isFactionBounty) {
                if (enemy.getFleet().getFaction().isHostileTo(Global.getSector().getFaction(ids.KESTEVEN_FACTION_ID))) {
                    for (FleetMemberAPI m : lost) {
                        ShipHullSpecAPI mspec = m.getHullSpec();
                        if (mspec == null) continue;
                        if (mspec.getHullSize() == ShipAPI.HullSize.FIGHTER) continue;
                        switch (contract.subType) {
                            case ("standard"):
                                completed++;
                                break;
                            case ("frigate"):
                                if (mspec.getHullSize() == ShipAPI.HullSize.FRIGATE) {
                                    completed++;
                                }
                                break;
                            case ("destroyer"):
                                if (mspec.getHullSize() == ShipAPI.HullSize.DESTROYER) {
                                    completed++;
                                }
                                break;
                            case ("cruiser"):
                                if (mspec.getHullSize() == ShipAPI.HullSize.CRUISER) {
                                    completed++;
                                }
                                break;
                            case ("capital"):
                                if (mspec.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) {
                                    completed++;
                                }
                                break;
                            case ("phase"):
                                if (mspec.getHints() == null) continue;
                                if (mspec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.PHASE)) {
                                    completed++;
                                    break;
                                }
                                if (mspec.getShipDefenseId() == null) continue;
                                if (mspec.getShipDefenseId().equals("phasecloak")) {
                                    completed++;
                                    break;
                                }
                                break;
                            case ("logistics"):
                                if (mspec.getHints() == null) continue;
                                if (util.isLogistics(mspec.getHints())) {
                                    completed++;
                                }
                                break;
                            case ("carrier"):
                                if (mspec.getHints() == null) continue;
                                if (mspec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.CARRIER)) {
                                    completed++;
                                }
                                break;
                        }
                    }
                }
            }
            //faction bounties
            else {
                if (enemy.getFleet() != null && enemy.getFleet().getFaction() != null) {
                    if (enemy.getFleet().getFaction().getId().equals(contract.subType)) {
                        for (FleetMemberAPI m : lost) {
                            ShipHullSpecAPI mspec = m.getHullSpec();
                            if (mspec == null) continue;
                            if (mspec.getHullSize() == ShipAPI.HullSize.FIGHTER) continue;
                            completed++;
                        }
                    }
                }
            }
            //completed =  Math.max(completed * result.getBattle().getPlayerInvolvementFraction(), 1);
            contract.completedCount += completed;
            //
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }
    @Override
    public boolean runWhilePaused() {
        return false;
    }

    //I realized that this is too jank too late
    public static String getTypeString(contractInfo contract) {
        String typeString = "";
        int remaining = contract.count-contract.completedCount;

        switch (contract.subType) {
            case ("standard"):
                if (contract.count>1 && remaining>1) {
                    typeString = "vessels of any kind";
                } else {
                    typeString = "vessel of any kind";
                }
                break;
            case ("frigate"):
                if (contract.count>1 && remaining>1) {
                    typeString = "frigates";
                } else {
                    typeString = "frigate";
                }
                break;
            case ("destroyer"):
                if (contract.count>1 && remaining>1) {
                    typeString = "destroyers";
                } else {
                    typeString = "destroyer";
                }
                break;
            case ("cruiser"):
                if (contract.count>1 && remaining>1) {
                    typeString = "cruisers";
                } else {
                    typeString = "cruiser";
                }
                break;
            case ("capital"):
                if (contract.count>1 && remaining>1) {
                    typeString = "capitals";
                } else {
                    typeString = "capital";
                }
                break;
            case ("phase"):
                if (contract.count>1 && remaining>1) {
                    typeString = "phase vessels";
                } else {
                    typeString = "phase vessel";
                }
                break;
            case ("logistics"):
                if (contract.count>1 && remaining>1) {
                    typeString = "logistics vessels";
                } else {
                    typeString = "logistics vessel";
                }
                break;
            case ("carrier"):
                if (contract.count>1 && remaining>1) {
                    typeString = "carriers";
                } else {
                    typeString = "carrier";
                }
                break;
            case (Factions.LUDDIC_PATH):
                if (contract.count>1 && remaining>1) {
                    typeString = "Luddic Path vessels";
                } else {
                    typeString = "Luddic Path vessel";
                }
                break;
            case (Factions.PIRATES):
                if (contract.count>1 && remaining>1) {
                    typeString = "Pirate vessels";
                } else {
                    typeString = "Pirate vessel";
                }
                break;
            case (Factions.REMNANTS):
                if (contract.count>1 && remaining>1) {
                    typeString = "Remnant vessels";
                } else {
                    typeString = "Remnant vessel";
                }
                break;
            case (Factions.DERELICT):
                if (contract.count>1 && remaining>1) {
                    typeString = "Derelict vessels";
                } else {
                    typeString = "Derelict vessel";
                }
                break;
            case (ids.ENIGMA_FACTION_ID):
                if (contract.count>1 && remaining>1) {
                    typeString = "Enigma vessels";
                } else {
                    typeString = "Enigma vessel";
                }
                break;
            case ("tahlan_legioinfernalis"):
                if (contract.count>1 && remaining>1) {
                    typeString = "Legio Infernalis vessels";
                } else {
                    typeString = "Legio Infernalis vessel";
                }
                break;
            case ("metals"):
                typeString = "metals";
                break;
            case ("supplies"):
                typeString = "supplies";
                break;
            case ("fuel"):
                typeString = "fuel";
                break;
            case ("heavy_machinery"):
                typeString = "heavy machinery";
                break;
            case ("artifact_electronics"):
                typeString = "artifact electronics";
                break;
            case ("IndEvo_parts"):
                typeString = "ship components";
                break;
            case ("IndEvo_rare_parts"):
                typeString = "relic components";
                break;
            case ("gamma_core"):
                if (contract.count>1 && remaining>1) {
                    typeString = "gamma cores";
                } else {
                    typeString = "gamma core";
                }
                break;
            case ("beta_core"):
                if (contract.count>1 && remaining>1) {
                    typeString = "beta cores";
                } else {
                    typeString = "beta core";
                }
                break;
            case ("alpha_core"):
                if (contract.count>1 && remaining>1) {
                    typeString = "alpha cores";
                } else {
                    typeString = "alpha core";
                }
                break;
            case ("tahlan_daemoncore"):
                if (contract.count>1 && remaining>1) {
                    typeString = "daemon cores";
                } else {
                    typeString = "daemon core";
                }
                break;
            case ("tahlan_archdaemoncore"):
                if (contract.count>1 && remaining>1) {
                    typeString = "archdaemon cores";
                } else {
                    typeString = "archdaemon core";
                }
                break;
        }

        return typeString;
    }
    public static String getUnitsString(contractInfo contract) {
        String units = " unit of ";
        if (contract.count>1) units = " units of ";
        if (contract.subType.equals("alpha_core") || contract.subType.equals("beta_core") || contract.subType.equals("gamma_core")
        || contract.subType.equals("tahlan_daemoncore") || contract.subType.equals("tahlan_archdaemoncore")) units = " ";

        return units;
    }

    public static boolean maxContracts(List<contractInfo> contracts, contractInfo.contractType type){
        int elimCount = 0;
        int scavCount = 0;

        for (contractInfo contract : contracts) {
            if (contract.type == contractInfo.contractType.ELIMINATE) {
                elimCount++;
                continue;
            }
            if (contract.type == contractInfo.contractType.SCAVENGE) {
                scavCount++;
            }
        }
        //2 max contracts after questline
        int storyBonus = 1;
        if (questUtil.getCompleted(nskr_EndingKestevenDialog.DIALOG_FINISHED_KEY)){
            storyBonus = 2;
        }
        if (type == contractInfo.contractType.ELIMINATE && elimCount >= MAX_ELIM_CONTRACTS * storyBonus) return true;
        if (type == contractInfo.contractType.SCAVENGE && scavCount >= MAX_SCAV_CONTRACTS * storyBonus) return true;

        return false;
    }

    public static List<contractInfo> getContracts(String id) {
        MemoryAPI mem = Global.getSector().getMemory();
        if (mem.contains(id)){
            return (List<contractInfo>)mem.get(id);
        } else {
            mem.set(id, new ArrayList<contractInfo>());
        }
        return (List<contractInfo>)mem.get(id);
    }

    public static List<contractInfo> setContracts(List<contractInfo> contracts, String id) {
        MemoryAPI mem = Global.getSector().getMemory();
        mem.set(id, contracts);
        return (List<contractInfo>) mem.get(id);
    }

    public static Random getRandom(String id) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) {

            data.put(id, new Random(new Random().nextLong()));
        }
        return (Random) data.get(id);
    }
}
