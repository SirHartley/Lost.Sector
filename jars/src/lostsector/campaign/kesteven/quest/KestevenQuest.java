package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import lostsector.quest.Quest;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestManager;
import lostsector.quest.QuestModule;
import lostsector.quest.Quests;

import java.util.List;
import java.util.Random;

// KestevenHubModule serves every conversation with Jack, Alice and Nicholas, KestevenJob1Module runs job 1,
// KestevenJob3Module and KestevenPartyModule run job 3, KestevenJob4Module job 4, KestevenJob5Module the job 5 meeting
// and intel, KestevenGlacierModule the Glacier facility, KestevenElizaSearchModule the Eliza search at pirate bars,
// KestevenSatelliteModule the data-disk satellites, KestevenElizaModule Eliza's port, KestevenCollector's shared
// modules the Tri-Tachyon collector, KestevenElizaFleetsModule Eliza's fleets, KestevenEndingsModule the Kesteven and
// Eliza endings, KestevenAltEndingsModule the Luddic and Tri-Tachyon endings, KestevenAftermathModule Jack's revenge
// and the failure for losing both mission markets, and KestevenCacheModule the Cache.
// isAvailable() keeps the default: the old code runs the questline in every campaign and treats a missing
// Kesteven home as failure (stage 99), so the state must always exist.
public final class KestevenQuest extends Quest<KestevenStage, KestevenState> {

    public static final String ID = "kq";

    public KestevenQuest() {
        super(ID, KestevenStage.class, KestevenFlag.class, KestevenStage.NOT_STARTED);
    }

    @Override
    protected KestevenState createState() {
        return new KestevenState();
    }

    @Override
    protected List<QuestModule<KestevenStage, KestevenState>> createModules() {
        return List.of(new KestevenHubModule(), new KestevenJob1Module(), new KestevenJob3Module(), new KestevenPartyModule(),
                new KestevenJob4Module(), new KestevenJob5Module(), new KestevenGlacierModule(), new KestevenElizaSearchModule(),
                new KestevenSatelliteModule(), new KestevenElizaModule(), KestevenCollector.encounter(), KestevenCollector.demand(),
                new KestevenElizaFleetsModule(), new KestevenEndingsModule(), new KestevenAltEndingsModule(),
                new KestevenAftermathModule(), new KestevenCacheModule());
    }

    // Null before QuestManager.startQuests() at the end of ModPlugin.onGameLoad, which includes new-campaign generation.
    public static KestevenState state() {
        return Quests.state(ID);
    }

    // Null when state() is null; QuestManager logs the reason.
    @SuppressWarnings("unchecked")
    static QuestContext<KestevenStage, KestevenState> context() {
        QuestManager manager = QuestManager.get();
        if (manager == null) return null;
        return (QuestContext<KestevenStage, KestevenState>) manager.context(ID, "outside", null, null, List.of());
    }

    // Throws before the state exists: an unsaved Random would reroll after a reload.
    public static Random random(String purpose) {
        QuestContext<KestevenStage, KestevenState> ctx = context();
        if (ctx == null) throw new IllegalStateException("[" + ID + "] random " + purpose + " requested before the quest state exists");
        return ctx.random(purpose);
    }

    // A constellation's name with type as the questline's text shows it: "Constellation" is added when the type lacks it.
    static String constellationName(String nameWithType) {
        return nameWithType.contains("Constellation") ? nameWithType : nameWithType + " Constellation";
    }

    // Queries for features outside the questline. Before the state exists, flags read as unset and the stage as NOT_STARTED.
    // Stage comparisons follow story order with FAILED last (KestevenStage.atLeast), so "from JOB5_DISKS on" also holds
    // after failure.

    public static KestevenStage stage() {
        KestevenState state = state();
        return state == null ? KestevenStage.NOT_STARTED : state.stage();
    }

    public static boolean isFailed() {
        return Quests.has(KestevenFlag.ENDED);
    }

    public static boolean kestevenEndingDone() {
        return Quests.has(KestevenFlag.KESTEVEN_ENDING_DONE);
    }

    public static boolean elizaEndingDone() {
        return Quests.has(KestevenFlag.ELIZA_ENDING_DONE);
    }

    // The artifact exchange and S-mod removal close once the chip went to Eliza or to an alternative ending.
    public static boolean researchServicesClosed() {
        return Quests.has(KestevenFlag.CHIP_HANDED_TO_ELIZA) || Quests.has(KestevenFlag.ALT_ENDING_DONE);
    }

    public static boolean isJackGone() {
        return Quests.has(KestevenFlag.JACK_GONE);
    }

    public static boolean glacierDiskRecovered() {
        return Quests.has(KestevenFlag.GLACIER_DISK_RECOVERED);
    }

    public static boolean inMessengerWindow() {
        KestevenStage stage = stage();
        return stage.atLeast(KestevenStage.JOB3_DONE) && !stage.atLeast(KestevenStage.JOB5_MEETING);
    }

    // Places of the questline, for the rows that take over their dialogs.

    // Eliza's market entity, or any entity of a market connected to it.
    public static boolean atElizaMarket(SectorEntityToken entity) {
        KestevenState state = state();
        SectorEntityToken market = state == null ? null : state.elizaMarket;
        if (market == null) return false;
        if (entity.getId().equals(market.getId())) return true;
        return entity.getMarket() != null && entity.getMarket().getConnectedEntities().contains(market);
    }

    // Actions for features outside the questline.

    // The "LZ" messenger of quest ic (campaign/events/intercepts), when the player opens its comm link.
    public static void reportMessengerMet() {
        if (Quests.has(KestevenFlag.MESSENGER_MET)) return;
        QuestContext<KestevenStage, KestevenState> ctx = context();
        if (ctx == null) return;
        ctx.set(KestevenFlag.MESSENGER_MET);
        ctx.set(KestevenFlag.MESSENGER_QUESTION_OPEN);
    }

    public static void reportCacheGuardianDefeated() {
        if (isFailed() || !stage().atLeast(KestevenStage.JOB5_DISKS)) return;
        QuestContext<KestevenStage, KestevenState> ctx = context();
        if (ctx != null && ctx.stage() != KestevenStage.CACHE_CLEARED) ctx.advance(KestevenStage.CACHE_CLEARED);
    }

    // Cache.CacheGuardInteractionConfig.notifyLeave, once the guardian has no prototypes left: the command core's rules
    // dialog continues in the encounter's window.
    public static void showCacheCore(InteractionDialogAPI dialog, SectorEntityToken core) {
        QuestManager manager = QuestManager.get();
        @SuppressWarnings("unchecked")
        QuestContext<KestevenStage, KestevenState> ctx = manager == null ? null
                : (QuestContext<KestevenStage, KestevenState>) manager.context(ID, "cacheGuardian", dialog, null, List.of());
        if (ctx != null) KestevenCacheModule.revealCore(ctx, core);
    }

    // World generation of the Cache: satellites with nothing left to salvage. KestevenSatelliteModule claims their
    // dialog when the quest state is created.
    public static void markEmptyDataSatellite(SectorEntityToken satellite, int number) {
        satellite.getMemory().set(KestevenSatelliteModule.ARTIFACT_KEY + number, true);
        satellite.getMemory().set(KestevenSatelliteModule.EMPTY_KEY, true);
    }
}
