package lostsector.campaign.events.intercepts;

import lostsector.quest.QuestState;
import lostsector.quest.modules.InterceptEncounter;
import lostsector.quest.modules.PayOffEncounter;

import java.util.LinkedHashMap;
import java.util.Map;

// Saved by XStream in the quest store; renaming or removing a field breaks saves once 1.0.c ships.
public final class InterceptsState extends QuestState<InterceptsStage> implements InterceptEncounter.Host, PayOffEncounter.Host {

    Map<String, InterceptEncounter.Record> intercepts = new LinkedHashMap<>();
    Map<String, PayOffEncounter.Record> payOffs = new LinkedHashMap<>();

    @Override
    public Map<String, InterceptEncounter.Record> intercepts() {
        return intercepts;
    }

    @Override
    public Map<String, PayOffEncounter.Record> payOffs() {
        return payOffs;
    }
}
