package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.GroundRaidObjectivesListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidType;
import lostsector.campaign.kesteven.quest.ElizaDialog;
import lostsector.campaign.kesteven.quest.QuestHelper;

import java.util.List;
import java.util.Map;

public class ElizaRaidObjectiveCreator implements GroundRaidObjectivesListener {

    static void log(final String message) {
        Global.getLogger(ElizaRaidObjectiveCreator.class).info(message);
    }

    public void modifyRaidObjectives(MarketAPI market, SectorEntityToken entity, List<GroundRaidObjectivePlugin> objectives, RaidType type, int marineTokens, int priority) {
        if (priority != 0) return;
        if (market == null) return;

        if(QuestHelper.getCompleted(ElizaDialog.ELIZA_RAID_KEY) && !QuestHelper.getCompleted(ElizaDialog.ELIZA_FIGHT_KEY) && market== QuestHelper.getElizaLoc().getMarket()) {
            ElizaRaid raid = new ElizaRaid(market, QuestPeople.getEliza());
            objectives.add(raid);
        }
    }

    public void reportRaidObjectivesAchieved(RaidResultData data, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
    }

}
