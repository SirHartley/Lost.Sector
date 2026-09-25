package lostsector.campaign.events.hints;

import lostsector.quest.NoFlags;
import lostsector.quest.Quest;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestManager;
import lostsector.quest.QuestModule;
import lostsector.quest.Quests;

import java.util.List;

// Record quest "hint": exploration intel. Signal hints toward the bounty systems and Frost, the tip a Kesteven
// officer sells at Kesteven bars, and the Frost entry on the first visit to Frost.
public final class HintsQuest extends Quest<HintsStage, HintsState> {

    public static final String ID = "hint";

    static final String INTEL_SIGNAL = "signal";
    static final String INTEL_TIP = "tip";
    static final String INTEL_FROST = "frost";

    public HintsQuest() {
        super(ID, HintsStage.class, NoFlags.class, HintsStage.RUNNING);
    }

    @Override
    protected HintsState createState() {
        return new HintsState();
    }

    // On a location change, Frost first removes its signal source, the roll then adds a hint, and the visit check
    // then sees a hint for the system the player just entered.
    @Override
    protected List<QuestModule<HintsStage, HintsState>> createModules() {
        return List.of(new HintsFrostModule(), new HintsSignalsModule(), new HintsVisitModule(), new HintsTipModule());
    }

    public static HintsState state() {
        return Quests.state(ID);
    }

    // A named bounty was sighted and its intel shown: a signal hint for the system the player is in ends at once.
    public static void reportBountySighted() {
        QuestContext<HintsStage, HintsState> ctx = context("reportBountySighted");
        if (ctx != null) HintsSignalsModule.bountySighted(ctx);
    }

    @SuppressWarnings("unchecked")
    private static QuestContext<HintsStage, HintsState> context(String source) {
        QuestManager manager = QuestManager.get();
        if (manager == null || state() == null) return null;
        return (QuestContext<HintsStage, HintsState>) manager.context(ID, source, null, null, List.of());
    }
}
