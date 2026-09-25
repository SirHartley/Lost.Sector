package lostsector.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;

import java.util.Locale;

// The quest's intel entries, one QuestIntel per declared key (README "Intel"). Bound to the context's dialog: when
// one is open, new entries and updates print in its text panel instead of the campaign message list.
public final class QuestIntels {

    private final QuestContext<?, ?> ctx;

    QuestIntels(QuestContext<?, ?> ctx) {
        this.ctx = ctx;
    }

    // No-op while the key has an active entry. A completed or failed entry still counting down its end delay does not
    // count, so the key can be shown again at once.
    public void show(String key) {
        Declarations.Intel declared = ctx.quest().declarations().intelDeclaration(key);
        if (declared == null) {
            ctx.error("intel " + key + " is not declared");
            return;
        }
        if (active(key) != null) return;
        if (!iconExists(declared.icon)) {
            ctx.error("intel " + key + " refused: icon " + declared.icon + " is not in graphics.campaignMissions");
            return;
        }
        QuestIntel intel = new QuestIntel(ctx.quest().id(), key, declared.icon, declared.tags);
        // The intel manager does not advance its entries; as a sector script the entry counts down its end delay,
        // and the sector drops the script once isDone() (the entry has ended) is true.
        Global.getSector().getIntelManager().addIntel(intel, false, ctx.textPanel());
        Global.getSector().addScript(intel);
        ctx.log("intel " + key + " shown");
    }

    public void update(String key, String updateKey) {
        QuestIntel intel = require(key, "update");
        if (intel != null) intel.update(updateKey, ctx.textPanel());
    }

    public void setMapLocation(String key, SectorEntityToken entity) {
        QuestIntel intel = require(key, "setMapLocation");
        if (intel != null) intel.setMapLocation(entity);
    }

    // Status completed, an update message, then the vanilla end delay (BaseIntelPlugin.getBaseDaysAfterEnd).
    public void complete(String key) {
        finish(key, QuestIntel.Status.COMPLETED);
    }

    public void fail(String key) {
        finish(key, QuestIntel.Status.FAILED);
    }

    // Ends every entry of the key at once, including one counting down its end delay.
    public void end(String key) {
        for (IntelInfoPlugin plugin : Global.getSector().getIntelManager().getIntel(QuestIntel.class)) {
            QuestIntel intel = (QuestIntel) plugin;
            if (!intel.isEnded() && intel.questId().equals(ctx.quest().id()) && intel.key().equals(key)) {
                intel.endImmediately();
                ctx.log("intel " + key + " ended");
            }
        }
    }

    public boolean isShown(String key) {
        return active(key) != null;
    }

    // A quest reset ends every entry of the quest.
    static void endAll(String questId) {
        for (IntelInfoPlugin plugin : Global.getSector().getIntelManager().getIntel(QuestIntel.class)) {
            QuestIntel intel = (QuestIntel) plugin;
            if (!intel.isEnded() && intel.questId().equals(questId)) intel.endImmediately();
        }
    }

    private void finish(String key, QuestIntel.Status status) {
        QuestIntel intel = require(key, status.name().toLowerCase(Locale.ROOT));
        if (intel == null) return;
        intel.finish(status, ctx.textPanel());
        ctx.log("intel " + key + " " + status.name().toLowerCase(Locale.ROOT));
    }

    private QuestIntel require(String key, String operation) {
        QuestIntel intel = active(key);
        if (intel == null) ctx.error("intel " + operation + " skipped: " + key + " is not shown");
        return intel;
    }

    // The entry of the key that is not ending; the intel manager keeps at most one, because show refuses a second.
    private QuestIntel active(String key) {
        for (IntelInfoPlugin plugin : Global.getSector().getIntelManager().getIntel(QuestIntel.class)) {
            QuestIntel intel = (QuestIntel) plugin;
            if (!intel.isEnding() && intel.questId().equals(ctx.quest().id()) && intel.key().equals(key)) return intel;
        }
        return null;
    }

    // SettingsAPI.getSpriteName throws for an id missing from the category and returns null for a missing category.
    private static boolean iconExists(String icon) {
        try {
            return Global.getSettings().getSpriteName("campaignMissions", icon) != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
