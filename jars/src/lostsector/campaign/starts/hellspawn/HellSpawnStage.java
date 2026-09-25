package lostsector.campaign.starts.hellspawn;

import lostsector.quest.QuestStage;

// Saved by name in HellSpawnState; renaming or removing a constant breaks saves once 1.0.c ships.
public enum HellSpawnStage implements QuestStage {

    DORMANT(null),
    WARNING(DORMANT),
    COUNTDOWN(WARNING),
    JUDGEMENT(COUNTDOWN),
    SPARED(JUDGEMENT),
    FIGHT(JUDGEMENT),
    JUDGED(FIGHT);

    private final HellSpawnStage previous;

    HellSpawnStage(HellSpawnStage previous) {
        this.previous = previous;
    }

    @Override
    public HellSpawnStage previous() {
        return previous;
    }
}
