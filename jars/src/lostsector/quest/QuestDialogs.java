package lostsector.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;

import java.util.Iterator;
import java.util.Set;

// Dialog entry points: claims routed by CorePlugin and dialogs opened from code (README "Dialog entry points").
public final class QuestDialogs {

    // Entity memory: the trigger the claimed entity's dialog fires when it opens.
    public static final String CLAIM_KEY = "$nskr_questDialog";

    private QuestDialogs() {
    }

    // The trigger claimed on the entity, or null. Read by CorePlugin.pickInteractionDialogPlugin.
    public static String claimedTrigger(SectorEntityToken entity) {
        MemoryAPI memory = entity == null ? null : entity.getMemoryWithoutUpdate();
        return memory == null ? null : memory.getString(CLAIM_KEY);
    }

    // The standard rules dialog; it fires the trigger when it opens.
    public static InteractionDialogPlugin plugin(String trigger) {
        return new RuleBasedInteractionDialogPluginImpl(trigger);
    }

    static void claim(QuestState<?> state, SectorEntityToken entity, String trigger, Set<String> scope) {
        QuestState.Claim own = find(state, entity);
        String current = claimedTrigger(entity);
        if (own == null && current != null) {
            QuestManager.logError(state.questId, "claim of " + trigger + " replaces " + current + " on " + entity.getId());
        }
        if (own != null) state.claims.remove(own);
        entity.getMemoryWithoutUpdate().set(CLAIM_KEY, trigger);
        state.claims.add(new QuestState.Claim(entity, trigger, scope));
        QuestManager.logInfo(state.questId, "claim " + trigger + " on " + entity.getId() + " for " + scope);
    }

    static void release(QuestState<?> state, SectorEntityToken entity) {
        QuestState.Claim claim = find(state, entity);
        if (claim != null) release(state, claim);
    }

    // Leaves the key alone when another claim has replaced this one.
    static void release(QuestState<?> state, QuestState.Claim claim) {
        MemoryAPI memory = claim.entity.getMemoryWithoutUpdate();
        if (claim.trigger.equals(memory.getString(CLAIM_KEY))) memory.unset(CLAIM_KEY);
        state.claims.remove(claim);
        QuestManager.logInfo(state.questId, "release " + claim.trigger + " on " + claim.entity.getId());
    }

    private static QuestState.Claim find(QuestState<?> state, SectorEntityToken entity) {
        for (QuestState.Claim claim : state.claims) {
            if (claim.entity == entity) return claim;
        }
        return null;
    }

    // Opens now when the UI is free; otherwise QuestManager retries every unpaused frame.
    static void open(QuestState<?> state, SectorEntityToken target, String trigger) {
        if (show(state, target, trigger)) return;
        state.pendingOpens.add(new QuestState.PendingOpen(target, trigger));
        QuestManager.logInfo(state.questId, "open " + trigger + " on " + target.getId() + " waits for the UI");
    }

    // Tries the oldest pending open whose target still exists. True when a dialog opened.
    static boolean openPending(QuestState<?> state) {
        Iterator<QuestState.PendingOpen> pending = state.pendingOpens.iterator();
        while (pending.hasNext()) {
            QuestState.PendingOpen open = pending.next();
            if (!open.target.isAlive()) {
                QuestManager.logError(state.questId, "open " + open.trigger + " dropped: target " + open.target.getId() + " is gone");
                pending.remove();
                continue;
            }
            if (!show(state, open.target, open.trigger)) return false;
            pending.remove();
            return true;
        }
        return false;
    }

    private static boolean show(QuestState<?> state, SectorEntityToken target, String trigger) {
        if (!Global.getSector().getCampaignUI().showInteractionDialog(plugin(trigger), target)) return false;
        QuestManager.logInfo(state.questId, "opened " + trigger + " on " + target.getId());
        return true;
    }
}
