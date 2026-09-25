package lostsector.quest;

import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Hooks run only while the module is active, except onSkip; QuestManager is the only caller (README "QuestModule").
public abstract class QuestModule<S extends Enum<S> & QuestStage, T extends QuestState<S>> {

    private final Set<S> stages;

    // No stages: active in every stage.
    @SafeVarargs
    protected QuestModule(S... stages) {
        this.stages = new HashSet<>();
        for (S stage : stages) {
            this.stages.add(stage);
        }
    }

    public final boolean isActiveIn(S stage) {
        return stages.isEmpty() || stages.contains(stage);
    }

    protected void declare(Declarations<S, T> d) {
    }

    protected void onStart(QuestContext<S, T> ctx) {
    }

    protected void onStage(QuestContext<S, T> ctx, S from) {
    }

    protected void onSkip(QuestContext<S, T> ctx) {
    }

    protected void onStop(QuestContext<S, T> ctx) {
    }

    protected void onDay(QuestContext<S, T> ctx) {
    }

    protected boolean wantsFrames(QuestContext<S, T> ctx) {
        return false;
    }

    protected void onFrame(QuestContext<S, T> ctx, float amount) {
    }

    protected void onLocationChanged(QuestContext<S, T> ctx, LocationAPI prev, LocationAPI curr) {
    }

    protected void onDecivilized(QuestContext<S, T> ctx, MarketAPI market, boolean fullyDestroyed) {
    }

    protected void devInfo(QuestContext<S, T> ctx, List<String> lines) {
    }
}
