package lostsector.campaign.kesteven.quest;

import lostsector.quest.QuestStage;

// Saved by name in KestevenState; renaming or removing a constant breaks saves once 1.0.c ships.
public enum KestevenStage implements QuestStage {

    NOT_STARTED(null),
    JOB1_ACTIVE(NOT_STARTED),
    JOB1_DONE(JOB1_ACTIVE),
    JOB3_OFFERED(JOB1_DONE),
    JOB3_BRIEFING(JOB3_OFFERED),
    JOB3_ACTIVE(JOB3_BRIEFING),
    JOB3_TARGET_KNOWN(JOB3_ACTIVE),
    JOB3_DONE(JOB3_TARGET_KNOWN),
    JOB4_WAITING(JOB3_DONE),
    JOB4_ACTIVE(JOB4_WAITING),
    JOB4_DONE(JOB4_ACTIVE),
    JOB5_OFFERED(JOB4_DONE),
    JOB5_MEETING(JOB5_OFFERED),
    JOB5_DISKS(JOB5_MEETING),
    CACHE_KNOWN(JOB5_DISKS),
    CACHE_CLEARED(CACHE_KNOWN),
    CHIP_RECOVERED(CACHE_CLEARED),
    COMPLETED(CHIP_RECOVERED),
    FAILED(null);

    private final KestevenStage previous;

    KestevenStage(KestevenStage previous) {
        this.previous = previous;
    }

    @Override
    public KestevenStage previous() {
        return previous;
    }

    // Declaration order is story order with FAILED last, so a failed questline counts as past every stage.
    boolean atLeast(KestevenStage stage) {
        return compareTo(stage) >= 0;
    }
}
