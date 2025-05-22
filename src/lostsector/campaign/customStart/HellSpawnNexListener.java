package lostsector.campaign.customStart;

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
import lostsector.campaign.customStart.intel.HellSpawnEventFactors;
import lostsector.campaign.customStart.intel.HellSpawnEventIntel;
import lostsector.util.MathUtilLS;

import java.util.List;

public class HellSpawnNexListener extends BaseCampaignEventListener implements EveryFrameScript, AgentActionListener, InvasionListener {



    public HellSpawnNexListener() {
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
        if (GamemodeManager.getMode() != GamemodeManager.gameMode.HELLSPAWN) return;

        if (!action.isPlayerInvolved()) return;

        CovertOpsManager.CovertActionResult result = action.getResult();
        if (result==null) return;
        if (!result.isSuccessful()) return;

        CovertOpsManager.CovertActionDef def = action.getDef();
        if (def==null) return;
        if (def.id==null) return;
        if (def.name==null) return;

        int points = MathUtilLS.getSeededRandomNumberInRange(1,6, HellSpawnManager.getRandom());
        switch (def.id){
            case "destabilizeMarket":
                points += HellSpawnManager.AGENT_SERIOUS_POINTS;

                HellSpawnEventIntel.get().addFactor(new HellSpawnEventFactors(points,
                        "Agent completed a mission", "Your agent completed a "+def.name + " mission.", ""));
                break;
            case "sabotageIndustry":
                points += HellSpawnManager.AGENT_SERIOUS_POINTS;

                HellSpawnEventIntel.get().addFactor(new HellSpawnEventFactors(points,
                        "Agent completed a mission", "Your agent completed a "+def.name + " mission.", ""));

                break;
            case "destroyCommodities":
                points += HellSpawnManager.AGENT_LIGHT_POINTS;

                HellSpawnEventIntel.get().addFactor(new HellSpawnEventFactors(points,
                        "Agent completed a mission", "Your agent completed a "+def.name + " mission.", ""));

                break;
            case "instigateRebellion":
                points += HellSpawnManager.AGENT_SERIOUS_POINTS;

                HellSpawnEventIntel.get().addFactor(new HellSpawnEventFactors(points,
                        "Agent completed a mission", "Your agent completed a "+def.name + " mission.", ""));

                break;
            case "procureShip":
                points += HellSpawnManager.AGENT_LIGHT_POINTS;

                HellSpawnEventIntel.get().addFactor(new HellSpawnEventFactors(points,
                        "Agent completed a mission", "Your agent completed a "+def.name + " mission.", ""));

                break;

        }

    }

    @Override
    public void reportMarketTransfered(MarketAPI market, FactionAPI newOwner, FactionAPI oldOwner, boolean playerInvolved, boolean isCapture, List<String> factionsToNotify, float repChangeStrength) {
        if (market==null) return;
        if (newOwner==null) return;
        if (GamemodeManager.getMode() != GamemodeManager.gameMode.HELLSPAWN) return;

        if (!playerInvolved || !isCapture) return;

        float points = (HellSpawnManager.marketSizeMult(market) * HellSpawnManager.CAPTURE_BASE_POINTS) + MathUtilLS.getSeededRandomNumberInRange(5,15, HellSpawnManager.getRandom());

        if (newOwner.getId().equals(Factions.PLAYER)) {
            HellSpawnEventIntel.get().addFactor(new HellSpawnEventFactors((int) points, "Conquered a market",
                    "Conquered " + market.getName() + " a size " + market.getSize() + " market for yourself.", "Hail glory to the victors, they will crush us just the same."));
        } else {
            HellSpawnEventIntel.get().addFactor(new HellSpawnEventFactors((int) points, "Conquered a market",
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
