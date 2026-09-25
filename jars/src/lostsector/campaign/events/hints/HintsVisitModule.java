package lostsector.campaign.events.hints;

import com.fs.starfarer.api.campaign.LocationAPI;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestIntels;
import lostsector.quest.QuestModule;

import java.util.List;
import java.util.Map;

// Arriving in the system of a signal hint or a bought tip: the entry switches to its visited text and ends three days
// later; the Frost hint ends at once, because the Frost entry takes over.
final class HintsVisitModule extends QuestModule<HintsStage, HintsState> {

    HintsVisitModule() {
        super();
    }

    @Override
    protected void declare(Declarations<HintsStage, HintsState> d) {
        d.token("recordSystem", ctx -> {
            HintRecord record = record(ctx);
            return record == null || record.system == null ? "" : record.system.getName();
        });
        d.check("recordVisited", ctx -> {
            HintRecord record = record(ctx);
            return record != null && record.visited;
        });
    }

    @Override
    protected void onLocationChanged(QuestContext<HintsStage, HintsState> ctx, LocationAPI prev, LocationAPI curr) {
        for (Map.Entry<String, HintRecord> entry : ctx.state().records.entrySet()) {
            HintRecord record = entry.getValue();
            if (record.visited || record.system != curr) continue;
            record.visited = true;
            String id = entry.getKey();
            QuestIntels intel = ctx.intel().record(id);
            if (!intel.isShown(record.intel)) continue;
            if (HintsQuest.INTEL_SIGNAL.equals(record.intel) && HintsSignalsModule.FROST.equals(id)) {
                intel.end(record.intel);
            } else {
                intel.close(record.intel);
            }
        }
    }

    @Override
    protected void devInfo(QuestContext<HintsStage, HintsState> ctx, List<String> lines) {
        for (Map.Entry<String, HintRecord> entry : ctx.state().records.entrySet()) {
            HintRecord record = entry.getValue();
            lines.add(record.intel + " " + entry.getKey() + ": " + (record.system == null ? "no system" : record.system.getName())
                    + (record.visited ? ", visited" : ""));
        }
    }

    // The record of the intel entry whose rows are being read; null outside intel text.
    static HintRecord record(QuestContext<HintsStage, HintsState> ctx) {
        String id = ctx.intelRecord();
        return id == null ? null : ctx.state().records.get(id);
    }
}
