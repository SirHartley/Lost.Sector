package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import lostsector.quest.QuestState;

import java.util.ArrayList;
import java.util.List;

// Saved by XStream in the quest store; renaming or removing a field breaks saves once 1.0.c ships.
// Until T35 the old questline classes read and write these fields through QuestHelper and QuestStageManager.
public final class KestevenState extends QuestState<KestevenStage> {

    // Purposes for KestevenQuest.random, one per getRandom() accessor; the two alternative endings share one.
    public static final String RANDOM_QUEST = "kestevenQuestRandom";
    public static final String RANDOM_REVENGE = "kestevenQuestRandomKey";
    public static final String RANDOM_GLACIER = "glacierCommsKeyRandom";
    public static final String RANDOM_ELIZA = "elizaDialogKeyRandom";
    public static final String RANDOM_CACHE_DOUBT = "cacheDoubtDialogRandom";
    public static final String RANDOM_CACHE_CORE = "coreDialogKeyRandom";
    public static final String RANDOM_KESTEVEN_ENDING = "kestevenEndingDialogKeyRandom";
    public static final String RANDOM_ELIZA_ENDING = "elizaEndingDialogKeyRandom";
    public static final String RANDOM_ALT_ENDING = "endingAltDialogKeyRandom";
    public static final String RANDOM_COLLECTOR = "ttCollectorDialogRandom";
    public static final String RANDOM_ELIZA_INTERCEPT = "elizaInterceptDialogRandom";

    // Started when the job 3 expedition spawns; the job fails when it passes KestevenJob3Module.TIME_LIMIT days.
    public static final String TIMER_JOB3 = "job3Expedition";

    // Started when job 4 is pending; Alice offers the job once it passes KestevenJob4Module.WAIT_DAYS days.
    public static final String TIMER_JOB4_WAIT = "job4Wait";

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
    // The Eliza search at pirate bars: 0 first spacer, 1 second spacer, 2 contact, 3 Eliza's market known.
    int elizaSearchStage;
    int partyDrinks;
    List<String> elizaSearchUsedMarkets = new ArrayList<>();
    // Rolled each time the first spacer's conversation opens.
    int elizaSpacerPrice;
    // The contact's entity before it moved away from a decivilized market, for the move message.
    String elizaContactFormerName;

    // The ship whose barrage line KestevenGlacierModule is printing; null outside its damageFleet action, never saved.
    transient FleetMemberAPI glacierHit;

    float ttPayout;
    float commissionRepPirates;
    float commissionRepKesteven;
    float commissionRepHegemony;

    // Frame seconds, as QuestStageManager counts them: 10 seconds are one campaign day.
    float dayCounter;
    float fleetCounter;
    float cacheSeconds;

    // One-time actions of QuestStageManager.
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
