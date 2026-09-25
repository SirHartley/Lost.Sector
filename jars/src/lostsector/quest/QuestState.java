package lostsector.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

// Saved by XStream inside QuestStore. This class, its subclasses and the nested records hold only saved-safe types
// (README "QuestState"); renaming a class or field breaks saves. Only QuestManager, QuestContext and QuestDialogs
// write the fields below, which is why they are package-private.
public abstract class QuestState<S extends Enum<S> & QuestStage> {

    String questId;
    S stage;
    long stageSince;
    Set<String> reached = new LinkedHashSet<>();
    Set<String> flags = new LinkedHashSet<>();
    Map<String, Long> timers = new LinkedHashMap<>();
    long seed;
    Map<String, Random> randoms = new LinkedHashMap<>();
    List<Mark> marks = new ArrayList<>();
    List<Claim> claims = new ArrayList<>();
    List<PendingOpen> pendingOpens = new ArrayList<>();
    Map<String, PersonAPI> people = new LinkedHashMap<>();

    // Exactly one of entity, person and market is set. Scopes hold stage names.
    static final class Mark {

        SectorEntityToken entity;
        PersonAPI person;
        MarketAPI market;
        Set<String> scope;

        Mark(SectorEntityToken entity, PersonAPI person, MarketAPI market, Set<String> scope) {
            this.entity = entity;
            this.person = person;
            this.market = market;
            this.scope = scope;
        }

        MemoryAPI memory() {
            if (entity != null) return entity.getMemoryWithoutUpdate();
            return person != null ? person.getMemoryWithoutUpdate() : market.getMemoryWithoutUpdate();
        }

        boolean is(SectorEntityToken entity, PersonAPI person, MarketAPI market) {
            if (entity != null) return this.entity == entity;
            return person != null ? this.person == person : this.market == market;
        }
    }

    static final class Claim {

        SectorEntityToken entity;
        String trigger;
        Set<String> scope;

        Claim(SectorEntityToken entity, String trigger, Set<String> scope) {
            this.entity = entity;
            this.trigger = trigger;
            this.scope = scope;
        }
    }

    static final class PendingOpen {

        SectorEntityToken target;
        String trigger;

        PendingOpen(SectorEntityToken target, String trigger) {
            this.target = target;
            this.trigger = trigger;
        }
    }

    public final S stage() {
        return stage;
    }

    public final boolean reached(S stage) {
        return stage != null && reached.contains(stage.name());
    }

    public final float daysInStage() {
        return Global.getSector().getClock().getElapsedDaysSince(stageSince);
    }

    public final boolean has(Enum<?> flag) {
        QuestManager manager = QuestManager.get();
        Quest<?, ?> quest = manager == null ? null : manager.quest(questId);
        if (quest != null && !quest.ownsFlag(flag)) {
            QuestManager.logError(questId, "unknown flag " + flag);
            return false;
        }
        return flag != null && flags.contains(flag.name());
    }

    public final long seed() {
        return seed;
    }
}
