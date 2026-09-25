package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import lostsector.campaign.CorePlugin;
import lostsector.quest.Quest;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestManager;
import lostsector.quest.QuestModule;
import lostsector.quest.Quests;

import java.util.List;
import java.util.Random;

// KestevenHubModule serves every conversation with Jack, Alice and Nicholas, KestevenJob1Module runs job 1,
// KestevenJob3Module and KestevenPartyModule run job 3 and KestevenGlacierModule the Glacier facility;
// QuestStageManager and the old dialog classes still run the rest of the questline on this state (T19 to T35).
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
                new KestevenGlacierModule());
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
        return (QuestContext<KestevenStage, KestevenState>) manager.context(ID, "legacy", null, null, List.of());
    }

    // Throws before the state exists: an unsaved Random would reroll after a reload.
    public static Random random(String purpose) {
        QuestContext<KestevenStage, KestevenState> ctx = context();
        if (ctx == null) throw new IllegalStateException("[" + ID + "] random " + purpose + " requested before the quest state exists");
        return ctx.random(purpose);
    }

    // Queries for features outside the questline. Before the state exists, flags read as unset and the stage as NOT_STARTED.
    // Stage comparisons use the legacy ints the old callers used, so "at least 16" also holds at FAILED (99).

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
        int stage = stage().toLegacy();
        return stage >= 10 && stage <= 14;
    }

    public static boolean delveMeetingDue() {
        return stage() == KestevenStage.JOB5_MEETING;
    }

    public static boolean cacheIsQuestTarget() {
        return stage().toLegacy() >= 16;
    }

    // Dialog routes of CorePlugin, until the questline claims these entities itself.

    public static boolean isUnreadHintWreck(SectorEntityToken entity) {
        return entity.getId().startsWith(QuestStageManager.JOB4_HINT_WRECK_ID_KEY) && !Quests.has(KestevenFlag.JOB4_HINT_WRECK_READ);
    }

    public static boolean isDataSatellite(SectorEntityToken entity) {
        return CorePlugin.hasMemoryKeyStartsWith(QuestStageManager.ARTIFACT_KEY, entity);
    }

    public static boolean elizaMeetingDone() {
        return Quests.has(KestevenFlag.ELIZA_DIALOG_FINISHED);
    }

    // Eliza's market entity, or any entity of a market connected to it.
    public static boolean atElizaMarket(SectorEntityToken entity) {
        KestevenState state = state();
        SectorEntityToken market = state == null ? null : state.elizaMarket;
        if (market == null) return false;
        if (entity.getId().equals(market.getId())) return true;
        return entity.getMarket() != null && entity.getMarket().getConnectedEntities().contains(market);
    }

    public static boolean kestevenEndingAvailable() {
        return stage() == KestevenStage.CHIP_RECOVERED
                && !Quests.has(KestevenFlag.KESTEVEN_ENDING_DONE) && !Quests.has(KestevenFlag.CHIP_HANDED_TO_ELIZA);
    }

    public static boolean elizaEndingAvailable() {
        return stage() == KestevenStage.CHIP_RECOVERED
                && !Quests.has(KestevenFlag.ELIZA_ENDING_DONE) && !Quests.has(KestevenFlag.ELIZA_KILLED)
                && Quests.has(KestevenFlag.ELIZA_HELPED) && Quests.has(KestevenFlag.CHIP_HANDED_TO_ELIZA)
                && Quests.has(KestevenFlag.ELIZA_RETURNED);
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
        if (!isFailed() && stage().toLegacy() >= 16) QuestHelper.setStage(KestevenStage.CACHE_CLEARED.toLegacy());
    }

    // World generation of the Cache: satellites that open DataSatelliteDialog with nothing left to salvage.
    public static void markEmptyDataSatellite(SectorEntityToken satellite, int number) {
        satellite.getMemory().set(QuestStageManager.ARTIFACT_KEY + number, true);
        satellite.getMemory().set(DataSatelliteDialog.ARTIFACT_EMPTY_KEY, true);
    }
}
