package lostsector.campaign.bounties;

import lostsector.quest.QuestStage;

// A record quest with one stage; saved by name in BountiesState.
public enum BountiesStage implements QuestStage {

    RUNNING;

    @Override
    public BountiesStage previous() {
        return null;
    }
}
