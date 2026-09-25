package lostsector.campaign.bounties;

import lostsector.quest.QuestState;
import lostsector.quest.modules.BountyEncounter;

import java.util.LinkedHashMap;
import java.util.Map;

// Saved by XStream in the quest store; renaming or removing a field breaks saves once 1.0.c ships.
public final class BountiesState extends QuestState<BountiesStage> implements BountyEncounter.Host {

    Map<String, BountyEncounter.Record> bounties = new LinkedHashMap<>();

    @Override
    public Map<String, BountyEncounter.Record> bounties() {
        return bounties;
    }
}
