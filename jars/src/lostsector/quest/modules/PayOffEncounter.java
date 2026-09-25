package lostsector.quest.modules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;
import lostsector.quest.QuestStage;
import lostsector.quest.QuestState;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

// The game logic of a hostile fleet's payment demand: whether the player can pay all, part or nothing, the amount a
// payment takes, and the payment with its vanilla receipt. Text, options and the fleet itself belong to the rows and
// to the module that spawns the fleet. The quest's state implements Host and keeps one Record per demand (README
// "Shared modules").
public final class PayOffEncounter<S extends Enum<S> & QuestStage, T extends QuestState<S> & PayOffEncounter.Host>
        extends QuestModule<S, T> {

    public static final String CREDITS = "credits";
    // An owed amount that no holding reaches: every payment is a part payment of everything the player holds.
    public static final int EVERYTHING = Integer.MAX_VALUE;

    private final String id;
    private final String currency;
    private final ToIntFunction<QuestContext<S, T>> owed;
    private final int partMinimum;
    private Predicate<QuestContext<S, T>> demands = ctx -> true;
    private BiConsumer<QuestContext<S, T>, Integer> onPaid = (ctx, amount) -> {
    };

    public interface Host {

        // Record id to record; the state creates the map, the module fills it.
        Map<String, Record> payOffs();
    }

    // Saved inside the quest state.
    public static final class Record {

        int payments;
        long paid;

        public int payments() {
            return payments;
        }

        public long paid() {
            return paid;
        }
    }

    // currency is CREDITS or a commodity id. owed runs only in the game. partMinimum is the least the player must hold
    // for a part payment; pass EVERYTHING to allow none.
    @SafeVarargs
    public PayOffEncounter(String id, String currency, ToIntFunction<QuestContext<S, T>> owed, int partMinimum, S... stages) {
        super(stages);
        if (id == null || currency == null || owed == null) throw new IllegalArgumentException("pay-off encounter " + id + " is missing a parameter");
        if (partMinimum < 1) throw new IllegalArgumentException("part minimum of " + id + " must be at least 1");
        this.id = id;
        this.currency = currency;
        this.owed = owed;
        this.partMinimum = partMinimum;
    }

    // While the condition fails, the check <id>Demands fails, so the demand rows do not match.
    public PayOffEncounter<S, T> demandsWhile(Predicate<QuestContext<S, T>> condition) {
        if (condition == null) throw new IllegalArgumentException("demand condition of " + id + " must not be null");
        demands = condition;
        return this;
    }

    // Runs after each payment with the amount taken, which may be 0 when nothing was owed.
    public PayOffEncounter<S, T> onPaid(BiConsumer<QuestContext<S, T>, Integer> onPaid) {
        if (onPaid == null) throw new IllegalArgumentException("onPaid of " + id + " must not be null");
        this.onPaid = onPaid;
        return this;
    }

    @Override
    protected void declare(Declarations<S, T> d) {
        d.check(id + "Demands", ctx -> demands.test(ctx));
        d.check(id + "CanPayAll", ctx -> held() >= owed.applyAsInt(ctx));
        d.check(id + "CanPaySome", ctx -> {
            int held = held();
            return held < owed.applyAsInt(ctx) && held >= partMinimum;
        });
        d.token(id + "Payment", ctx -> Global.getSector().getPlayerFleet() == null ? "" : format(payment(ctx)));
        d.action(id + "Pay", this::pay);
    }

    private void pay(QuestContext<S, T> ctx) {
        int amount = payment(ctx);
        if (amount > 0) {
            if (isCredits()) {
                ctx.rewards().takeCredits(amount);
            } else {
                ctx.rewards().commodity(currency, -amount);
            }
        } else {
            ctx.log(id + " payment took nothing: nothing owed or held");
        }
        Record record = record(ctx);
        record.payments++;
        record.paid += amount;
        onPaid.accept(ctx, amount);
    }

    private int payment(QuestContext<S, T> ctx) {
        return Math.max(0, Math.min(owed.applyAsInt(ctx), held()));
    }

    // Credits are a float; a payment takes whole credits, as Misc.getDGSCredits shows them.
    private int held() {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return 0;
        if (isCredits()) return (int) pf.getCargo().getCredits().get();
        return (int) pf.getCargo().getCommodityQuantity(currency);
    }

    private String format(int amount) {
        return isCredits() ? Misc.getDGSCredits(amount) : String.valueOf(amount);
    }

    private boolean isCredits() {
        return CREDITS.equals(currency);
    }

    private Record record(QuestContext<S, T> ctx) {
        return ctx.state().payOffs().computeIfAbsent(id, key -> new Record());
    }

    @Override
    protected void devInfo(QuestContext<S, T> ctx, List<String> lines) {
        Record record = record(ctx);
        lines.add(id + ": payments " + record.payments + ", paid " + record.paid + " " + currency);
    }
}
