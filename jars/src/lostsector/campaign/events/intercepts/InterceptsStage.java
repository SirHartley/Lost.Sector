package lostsector.campaign.events.intercepts;

import lostsector.quest.QuestStage;

// A record quest with one stage; saved by name in InterceptsState.
public enum InterceptsStage implements QuestStage {

    RUNNING;

    @Override
    public InterceptsStage previous() {
        return null;
    }
}
