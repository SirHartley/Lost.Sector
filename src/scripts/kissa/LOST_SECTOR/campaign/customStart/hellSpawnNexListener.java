package scripts.kissa.LOST_SECTOR.campaign.customStart;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.Nex_MarketCMD;
import exerelin.campaign.CovertOpsManager;
import exerelin.campaign.InvasionRound;
import exerelin.campaign.intel.agents.CovertActionIntel;
import exerelin.utilities.AgentActionListener;
import exerelin.utilities.InvasionListener;
import scripts.kissa.LOST_SECTOR.campaign.customStart.intel.hellSpawnEventFactors;
import scripts.kissa.LOST_SECTOR.campaign.customStart.intel.hellSpawnEventIntel;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

import java.util.List;

public class hellSpawnNexListener extends BaseCampaignEventListener implements EveryFrameScript, AgentActionListener, InvasionListener {



    public hellSpawnNexListener() {
        super(false);

    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void reportAgentAction(CovertActionIntel action) {
        if (action==null) return;
        if (gamemodeManager.getMode() != gamemodeManager.gameMode.HELLSPAWN) return;

        if (!action.isPlayerInvolved()) return;

        CovertOpsManager.CovertActionResult result = action.getResult();
        if (result==null) return;
        if (!result.isSuccessful()) return;

        CovertOpsManager.CovertActionDef def = action.getDef();
        if (def==null) return;
        if (def.id==null) return;
        if (def.name==null) return;

        int points = mathUtil.getSeededRandomNumberInRange(1,6, hellSpawnManager.getRandom());
        switch (def.id){
            case "destabilizeMarket":
                points += hellSpawnManager.AGENT_SERIOUS_POINTS;

                hellSpawnEventIntel.get().addFactor(new hellSpawnEventFactors(points,
                        "Agent completed a mission", "Your agent completed a "+def.name + " mission.", ""));
                break;
            case "sabotageIndustry":
                points += hellSpawnManager.AGENT_SERIOUS_POINTS;

                hellSpawnEventIntel.get().addFactor(new hellSpawnEventFactors(points,
                        "Agent completed a mission", "Your agent completed a "+def.name + " mission.", ""));

                break;
            case "destroyCommodities":
                points += hellSpawnManager.AGENT_LIGHT_POINTS;

                hellSpawnEventIntel.get().addFactor(new hellSpawnEventFactors(points,
                        "Agent completed a mission", "Your agent completed a "+def.name + " mission.", ""));

                break;
            case "instigateRebellion":
                points += hellSpawnManager.AGENT_SERIOUS_POINTS;

                hellSpawnEventIntel.get().addFactor(new hellSpawnEventFactors(points,
                        "Agent completed a mission", "Your agent completed a "+def.name + " mission.", ""));

                break;
            case "procureShip":
                points += hellSpawnManager.AGENT_LIGHT_POINTS;

                hellSpawnEventIntel.get().addFactor(new hellSpawnEventFactors(points,
                        "Agent completed a mission", "Your agent completed a "+def.name + " mission.", ""));

                break;

        }

    }

    @Override
    public void reportMarketTransfered(MarketAPI market, FactionAPI newOwner, FactionAPI oldOwner, boolean playerInvolved, boolean isCapture, List<String> factionsToNotify, float repChangeStrength) {
        if (market==null) return;
        if (newOwner==null) return;
        if (gamemodeManager.getMode() != gamemodeManager.gameMode.HELLSPAWN) return;

        if (!playerInvolved || !isCapture) return;

        float points = (hellSpawnManager.marketSizeMult(market) * hellSpawnManager.CAPTURE_BASE_POINTS) + mathUtil.getSeededRandomNumberInRange(5,15, hellSpawnManager.getRandom());

        if (newOwner.getId().equals(Factions.PLAYER)) {
            hellSpawnEventIntel.get().addFactor(new hellSpawnEventFactors((int) points, "Conquered a market",
                    "Conquered " + market.getName() + " a size " + market.getSize() + " market for yourself.", "Hail glory to the victors, they will crush us just the same."));
        } else {
            hellSpawnEventIntel.get().addFactor(new hellSpawnEventFactors((int) points, "Conquered a market",
                    "Conquered " + market.getName() + " a size " + market.getSize() + " market for "+newOwner.getDisplayNameWithArticle()+".", "Hail glory to the victors, they will crush us just the same."));
        }
    }


    //UNUSED
    @Override
    public void reportInvadeLoot(InteractionDialogAPI dialog, MarketAPI market, Nex_MarketCMD.TempDataInvasion actionData, CargoAPI cargo) {
    }

    @Override
    public void reportInvasionRound(InvasionRound.InvasionRoundResult result, CampaignFleetAPI fleet, MarketAPI defender, float atkStr, float defStr) {
    }

    @Override
    public void reportInvasionFinished(CampaignFleetAPI fleet, FactionAPI attackerFaction, MarketAPI market, float numRounds, boolean success) {
    }
}
