package lostsector.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import lostsector.helper.UiSounds;

import java.util.Locale;
import java.util.Objects;

// The quest's intel entries, one QuestIntel per declared key, or per key and record for a record quest (README
// "Intel"). ctx.intel() addresses the entries without a record; record(id) returns the same methods for one record.
// Bound to the context's dialog: when one is open, new entries and updates print in its text panel instead of the
// campaign message list.
public final class QuestIntels {

    private final QuestContext<?, ?> ctx;
    private final String record;

    QuestIntels(QuestContext<?, ?> ctx) {
        this(ctx, null);
    }

    private QuestIntels(QuestContext<?, ?> ctx, String record) {
        this.ctx = ctx;
        this.record = record;
    }

    // The entries of one record: every method of the returned object addresses the entry of its key and this record.
    public QuestIntels record(String record) {
        if (record == null || record.isEmpty()) {
            ctx.error("intel record refused: the record id is empty");
            return this;
        }
        return new QuestIntels(ctx, record);
    }

    // No-op while the entry is active. A completed, failed or closed entry still counting down its end delay does not
    // count, so the entry can be shown again at once.
    public void show(String key) {
        IntelSpec declared = ctx.quest().declarations().intelDeclaration(key);
        if (declared == null) {
            ctx.error("intel " + key + " is not declared");
            return;
        }
        if (active(key) != null) return;
        if (!iconExists(declared.icon)) {
            ctx.error("intel " + key + " refused: icon " + declared.icon + " is not in graphics.campaignMissions");
            return;
        }
        QuestIntel intel = new QuestIntel(ctx.quest().id(), key, record, declared.icon, declared.tags);
        if (declared.isImportant()) intel.setImportant(true);
        // The intel manager does not advance its entries; as a sector script the entry counts down its end delay, and
        // the sector drops the script once isDone() (the entry has ended) is true.
        intel.post(ctx.textPanel());
        Global.getSector().addScript(intel);
        ctx.log("intel " + intel.label() + " shown");
    }

    public void update(String key, String updateKey) {
        update(key, updateKey, null);
    }

    // The message plays the sound (a sounds.json id, such as ui_rep_raise) instead of the standard update sound.
    public void update(String key, String updateKey, String sound) {
        QuestIntel intel = require(key, "update");
        if (intel != null) intel.update(updateKey, sound(sound), ctx.textPanel());
    }

    public void setMapLocation(String key, SectorEntityToken entity) {
        QuestIntel intel = require(key, "setMapLocation");
        if (intel != null) intel.setMapLocation(entity);
    }

    // Status completed, an update message, then the vanilla end delay (BaseIntelPlugin.getBaseDaysAfterEnd).
    public void complete(String key) {
        complete(key, "", null);
    }

    public void complete(String key, String updateKey, String sound) {
        finish(key, QuestIntel.Status.COMPLETED, updateKey, sound);
    }

    public void fail(String key) {
        fail(key, "", null);
    }

    public void fail(String key, String updateKey, String sound) {
        finish(key, QuestIntel.Status.FAILED, updateKey, sound);
    }

    // The vanilla end delay without a status change or a message.
    public void close(String key) {
        QuestIntel intel = require(key, "close");
        if (intel == null) return;
        intel.endAfterDelay();
        ctx.log("intel " + intel.label() + " closed");
    }

    // Ends the entry at once, including one counting down its end delay.
    public void end(String key) {
        for (IntelInfoPlugin plugin : Global.getSector().getIntelManager().getIntel(QuestIntel.class)) {
            QuestIntel intel = (QuestIntel) plugin;
            if (!intel.isEnded() && matches(intel, key)) {
                intel.endImmediately();
                ctx.log("intel " + intel.label() + " ended");
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

    private void finish(String key, QuestIntel.Status status, String updateKey, String sound) {
        QuestIntel intel = require(key, status.name().toLowerCase(Locale.ROOT));
        if (intel == null) return;
        intel.finish(status, updateKey, sound(sound), ctx.textPanel());
        ctx.log("intel " + intel.label() + " " + status.name().toLowerCase(Locale.ROOT));
    }

    private QuestIntel require(String key, String operation) {
        QuestIntel intel = active(key);
        if (intel == null) ctx.error("intel " + operation + " skipped: " + (record == null ? key : key + ":" + record) + " is not shown");
        return intel;
    }

    // The entry that is not ending; the intel manager keeps at most one per key and record, because show refuses a second.
    private QuestIntel active(String key) {
        for (IntelInfoPlugin plugin : Global.getSector().getIntelManager().getIntel(QuestIntel.class)) {
            QuestIntel intel = (QuestIntel) plugin;
            if (!intel.isEnding() && matches(intel, key)) return intel;
        }
        return null;
    }

    private boolean matches(QuestIntel intel, String key) {
        return intel.questId().equals(ctx.quest().id()) && intel.key().equals(key) && Objects.equals(intel.record(), record);
    }

    // Null keeps the standard sound; an unknown id is logged and replaced by it.
    private String sound(String sound) {
        if (sound == null || UiSounds.exists(sound)) return sound;
        ctx.error("intel message sound " + sound + " is not in sounds.json; the standard sound plays");
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
