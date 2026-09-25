package lostsector.campaign.bounties;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import lostsector.quest.QuestState;
import lostsector.quest.modules.BountyEncounter;

import java.util.LinkedHashMap;
import java.util.Map;

// Saved by XStream in the quest store; renaming or removing a field breaks saves once 1.0.c ships.
public final class BountiesState extends QuestState<BountiesStage> implements BountyEncounter.Host {

    Map<String, BountyEncounter.Record> bounties = new LinkedHashMap<>();

    // The TTDS Helios wreck, placed at the Mothership's loot; the fleet dialog moves to it once on leaving.
    SectorEntityToken mothershipWreck;
    boolean mothershipWreckShown;

    @Override
    public Map<String, BountyEncounter.Record> bounties() {
        return bounties;
    }
}
