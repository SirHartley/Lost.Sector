package lostsector.campaign.events.hints;

import lostsector.quest.QuestStage;

// A record quest with one stage; saved by name in HintsState.
public enum HintsStage implements QuestStage {

    RUNNING;

    @Override
    public HintsStage previous() {
        return null;
    }
}
