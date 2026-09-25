package lostsector.campaign.kesteven.quest;

import lostsector.quest.QuestStage;

// Saved by name in KestevenState; renaming or removing a constant breaks saves once 1.0.c ships.
public enum KestevenStage implements QuestStage {

    NOT_STARTED(null, 0),
    JOB1_ACTIVE(NOT_STARTED, 1),
    JOB1_DONE(JOB1_ACTIVE, 2),
    JOB3_OFFERED(JOB1_DONE, 6),
    JOB3_BRIEFING(JOB3_OFFERED, 7),
    JOB3_ACTIVE(JOB3_BRIEFING, 8),
    JOB3_TARGET_KNOWN(JOB3_ACTIVE, 9),
    JOB3_DONE(JOB3_TARGET_KNOWN, 10),
    JOB4_WAITING(JOB3_DONE, 11),
    JOB4_ACTIVE(JOB4_WAITING, 12),
    JOB4_DONE(JOB4_ACTIVE, 13),
    JOB5_OFFERED(JOB4_DONE, 14),
    JOB5_MEETING(JOB5_OFFERED, 15),
    JOB5_DISKS(JOB5_MEETING, 16),
    CACHE_KNOWN(JOB5_DISKS, 17),
    CACHE_CLEARED(CACHE_KNOWN, 18),
    CHIP_RECOVERED(CACHE_CLEARED, 19),
    COMPLETED(CHIP_RECOVERED, 20),
    FAILED(null, 99);

    private final KestevenStage previous;
    private final int legacy;

    KestevenStage(KestevenStage previous, int legacy) {
        this.previous = previous;
        this.legacy = legacy;
    }

    @Override
    public KestevenStage previous() {
        return previous;
    }

    // TODO T35: remove fromLegacy and toLegacy with the last caller of the int stage accessors.
    public static KestevenStage fromLegacy(int legacy) {
        for (KestevenStage stage : values()) {
            if (stage.legacy == legacy) return stage;
        }
        throw new IllegalArgumentException("No Kesteven stage for legacy value " + legacy);
    }

    public int toLegacy() {
        return legacy;
    }
}
