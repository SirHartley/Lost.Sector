package scripts.kissa.LOST_SECTOR.campaign.graid;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.GroundRaidObjectivesListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidType;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.quests.nskr_elizaDialog;
import scripts.kissa.LOST_SECTOR.util.util;

public class nskr_elizaRaidObjectiveCreator implements GroundRaidObjectivesListener {

    static void log(final String message) {
        Global.getLogger(nskr_elizaRaidObjectiveCreator.class).info(message);
    }

    public void modifyRaidObjectives(MarketAPI market, SectorEntityToken entity, List<GroundRaidObjectivePlugin> objectives, RaidType type, int marineTokens, int priority) {
        if (priority != 0) return;
        if (market == null) return;

        if(questUtil.getCompleted(nskr_elizaDialog.ELIZA_RAID_KEY) && !questUtil.getCompleted(nskr_elizaDialog.ELIZA_FIGHT_KEY) && market==questUtil.getElizaLoc().getMarket()) {
            nskr_elizaRaid raid = new nskr_elizaRaid(market, util.getEliza());
            objectives.add(raid);
        }
    }

    public void reportRaidObjectivesAchieved(RaidResultData data, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
    }

}
