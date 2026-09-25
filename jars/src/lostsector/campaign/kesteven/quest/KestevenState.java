package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import lostsector.quest.QuestState;

import java.util.ArrayList;
import java.util.List;

// Saved by XStream in the quest store; renaming or removing a field breaks saves once 1.0.c ships.
// Until T35 the old questline classes read and write these fields through QuestHelper and QuestStageManager.
public final class KestevenState extends QuestState<KestevenStage> {

    // Purposes for KestevenQuest.random, one per getRandom() accessor; the two alternative endings share one.
    public static final String RANDOM_QUEST = "kestevenQuestRandom";
    public static final String RANDOM_REVENGE = "kestevenQuestRandomKey";
    public static final String RANDOM_SATELLITE = "artifactKeyRandom";
    public static final String RANDOM_GLACIER = "glacierCommsKeyRandom";
    public static final String RANDOM_HINT_WRECK = "job4HintWreckDialogRandom";
    public static final String RANDOM_ELIZA = "elizaDialogKeyRandom";
    public static final String RANDOM_CACHE_DOUBT = "cacheDoubtDialogRandom";
    public static final String RANDOM_CACHE_CORE = "coreDialogKeyRandom";
    public static final String RANDOM_KESTEVEN_ENDING = "kestevenEndingDialogKeyRandom";
    public static final String RANDOM_ELIZA_ENDING = "elizaEndingDialogKeyRandom";
    public static final String RANDOM_ALT_ENDING = "endingAltDialogKeyRandom";
    public static final String RANDOM_JOB4_FLEET = "job4FleetDialogRandom";
    public static final String RANDOM_COLLECTOR = "ttCollectorDialogRandom";
    public static final String RANDOM_ELIZA_INTERCEPT = "elizaInterceptDialogRandom";

    // Picked on first use and kept.
    StarSystemAPI job1TipSystem;
    SectorEntityToken job3Start;
    SectorEntityToken job3Target;
    SectorEntityToken job4FriendlyTarget;
    SectorEntityToken job4EnemyTarget;
    StarSystemAPI job5FrostTipSystem;
    SectorEntityToken elizaMarket;
    SectorEntityToken elizaContactMarket;
    SectorEntityToken cacheGuardianSpot;

    int disksRecovered;
    int satellitesRecovered;
    int nicholasDialogStage;
    int job4FleetDialogStage;
    int elizaSearchStage;
    List<String> elizaSearchUsedMarkets = new ArrayList<>();

    float ttPayout;
    float commissionRepPirates;
    float commissionRepKesteven;
    float commissionRepHegemony;

    // Frame seconds, as QuestStageManager counts them: 10 seconds are one campaign day.
    float dayCounter;
    float fleetCounter;
    float job4WaitCounter;
    float cacheSeconds;
    // Days left, reduced by 0.1 on every quest fleet tick while the expedition exists.
    float job3TimeLeft = QuestStageManager.JOB3_TIME_LIMIT;

    // One-time actions of QuestStageManager.
    boolean job1IntelAdded;
    boolean job3IntelAdded;
    boolean job3FleetSpawned;
    boolean job4IntelAdded;
    boolean job4FleetsSpawned;
    boolean job5IntelAdded;
    boolean cacheIntelAdded;
    boolean cacheGuardianSpotPicked;
    boolean cacheDoubtShown;
    boolean cacheGuardianSpawned;
    boolean collectorSpawned;
    boolean elizaInterceptSpawned;
    boolean elizaRevengeSpawned;
    boolean jackRevengeSpawned;
    boolean commissionRestored;
}
