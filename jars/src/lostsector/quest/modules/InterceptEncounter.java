package lostsector.quest.modules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import lostsector.helper.FleetHelper;
import lostsector.helper.fleet.SimpleFleet;
import lostsector.quest.Declarations;
import lostsector.quest.FleetRole;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestFleet;
import lostsector.quest.QuestModule;
import lostsector.quest.QuestStage;
import lostsector.quest.QuestState;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

// One kind of fleet that appears near the player and hunts them: a daily roll while a condition holds, a spawn at the
// edge of the player's sensor range, the orders of its role, and optionally a second role it switches to later.
// The quest's state implements Host and keeps one Record per encounter (README "Shared modules").
public final class InterceptEncounter<S extends Enum<S> & QuestStage, T extends QuestState<S> & InterceptEncounter.Host>
        extends QuestModule<S, T> {

    public enum Repeat {
        ONCE,
        // Rolls again once no fleet of the encounter is left.
        REPEATING
    }

    private final String id;
    private final String role;
    private final FleetRole fleetRole;
    private final Repeat repeat;
    private final float dailyChance;
    private final Predicate<QuestContext<S, T>> condition;
    private final BiFunction<SectorEntityToken, Random, SimpleFleet> builder;
    private BiConsumer<CampaignFleetAPI, Random> finish = (fleet, random) -> {
    };

    private String nextRole;
    private FleetRole nextFleetRole;
    private Function<Random, SectorEntityToken> nextTarget;
    private float nextAfterDays = Float.NaN;
    private String nextAction;
    private Consumer<QuestContext<S, T>> onNextAction;
    private Predicate<QuestContext<S, T>> nextWhen;
    private Consumer<CampaignFleetAPI> onSwitch = fleet -> {
    };

    public interface Host {

        // Record id to record; the state creates the map, the module fills it.
        Map<String, Record> intercepts();
    }

    // Saved inside the quest state.
    public static final class Record {

        int spawns;

        public int spawns() {
            return spawns;
        }
    }

    // The builder gets the spawn point (the player's position) and the encounter's saved fleet random, and returns
    // the fleet unbuilt; the module builds it, moves it to the edge of the player's sensor range and registers it.
    @SafeVarargs
    public InterceptEncounter(String id, String role, FleetRole fleetRole, Repeat repeat, float dailyChance,
                              Predicate<QuestContext<S, T>> condition,
                              BiFunction<SectorEntityToken, Random, SimpleFleet> builder, S... stages) {
        super(stages);
        if (id == null || role == null || fleetRole == null || repeat == null || condition == null || builder == null) {
            throw new IllegalArgumentException("intercept encounter " + id + " is missing a parameter");
        }
        this.id = id;
        this.role = role;
        this.fleetRole = fleetRole;
        this.repeat = repeat;
        this.dailyChance = dailyChance;
        this.condition = condition;
        this.builder = builder;
    }

    // Runs after the fleet is placed, with the same random, before FleetHelper.update: faction changes, hullmods.
    public InterceptEncounter<S, T> finish(BiConsumer<CampaignFleetAPI, Random> finish) {
        if (finish == null) throw new IllegalArgumentException("finish of " + id + " must not be null");
        this.finish = finish;
        return this;
    }

    // Once a fleet is older than the given days (checked each day), it switches to the role. A target function sets
    // FleetInfo.target first, for guard or leave orders.
    public InterceptEncounter<S, T> switchAfter(float days, String role, FleetRole fleetRole, Function<Random, SectorEntityToken> target) {
        setNext(role, fleetRole, target);
        if (!(days > 0f)) throw new IllegalArgumentException("switch days of " + id + " must be positive");
        nextAfterDays = days;
        return this;
    }

    // Declares the action; run from the fleet's own dialog, it calls onAction and switches that fleet to the role.
    // The action does only onAction for a fleet that has already switched.
    public InterceptEncounter<S, T> switchOnAction(String action, String role, FleetRole fleetRole,
                                                   Function<Random, SectorEntityToken> target, Consumer<QuestContext<S, T>> onAction) {
        setNext(role, fleetRole, target);
        if (action == null || onAction == null) throw new IllegalArgumentException("switch action of " + id + " is missing a parameter");
        nextAction = action;
        onNextAction = onAction;
        return this;
    }

    // Also switches every fleet of the first role on the first day the condition holds. Needs the second role from
    // switchAfter or switchOnAction.
    public InterceptEncounter<S, T> switchWhen(Predicate<QuestContext<S, T>> condition) {
        if (nextRole == null) throw new IllegalStateException("intercept encounter " + id + " has no second role to switch to");
        if (condition == null) throw new IllegalArgumentException("switch condition of " + id + " must not be null");
        nextWhen = condition;
        return this;
    }

    // Runs on each fleet after it switches to the second role, for memory flags the new orders need.
    public InterceptEncounter<S, T> onSwitch(Consumer<CampaignFleetAPI> onSwitch) {
        if (onSwitch == null) throw new IllegalArgumentException("onSwitch of " + id + " must not be null");
        this.onSwitch = onSwitch;
        return this;
    }

    private void setNext(String role, FleetRole fleetRole, Function<Random, SectorEntityToken> target) {
        if (nextRole != null) throw new IllegalStateException("intercept encounter " + id + " already has a second role");
        if (role == null || fleetRole == null) throw new IllegalArgumentException("second role of " + id + " is missing a parameter");
        nextRole = role;
        nextFleetRole = fleetRole;
        nextTarget = target;
    }

    public static boolean playerInHyperspaceWithin(float distanceFromCenter) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        return pf != null && pf.isInHyperspace() && pf.getLocationInHyperspace().length() < distanceFromCenter;
    }

    @Override
    protected void declare(Declarations<S, T> d) {
        d.role(role, fleetRole);
        if (nextRole != null) d.role(nextRole, nextFleetRole);
        if (nextAction != null) d.action(nextAction, this::switchDialogFleet);
    }

    @Override
    protected void onDay(QuestContext<S, T> ctx) {
        if (Global.getSector().getPlayerFleet() == null) return;
        if (!Float.isNaN(nextAfterDays)) {
            for (QuestFleet fleet : ctx.fleets().get(role)) {
                if (fleet.info().age >= nextAfterDays) switchFleet(ctx, fleet);
            }
        }
        if (nextWhen != null && !ctx.fleets().get(role).isEmpty() && nextWhen.test(ctx)) {
            for (QuestFleet fleet : ctx.fleets().get(role)) switchFleet(ctx, fleet);
        }
        Record record = record(ctx);
        if (repeat == Repeat.ONCE ? record.spawns > 0 : hasFleet(ctx)) return;
        if (!condition.test(ctx)) return;
        if (ctx.random("roll:" + id).nextFloat() >= dailyChance) return;
        spawn(ctx, record);
    }

    private void spawn(QuestContext<S, T> ctx, Record record) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        Random random = ctx.random("fleet:" + id);
        SimpleFleet spec = builder.apply(pf.getContainingLocation().createToken(pf.getLocation()), random);
        CampaignFleetAPI fleet = ctx.fleets().spawn(role, id, spec);
        if (fleet == null) return;
        record.spawns++;

        float distance = pf.getSensorStrength() * 0.90f + fleet.getSensorProfile() * 0.90f;
        Vector2f loc = MathUtils.getPointOnCircumference(pf.getLocation(), distance, random.nextFloat() * 360.0f);
        fleet.setLocation(loc.x, loc.y);
        fleet.setFacing(random.nextFloat() * 360.0f);
        finish.accept(fleet, random);
        FleetHelper.update(fleet, random);
    }

    private void switchDialogFleet(QuestContext<S, T> ctx) {
        SectorEntityToken target = ctx.target();
        for (QuestFleet fleet : fleets(ctx)) {
            if (fleet.fleet() != target) continue;
            onNextAction.accept(ctx);
            if (fleet.isRole(role)) switchFleet(ctx, fleet);
            return;
        }
        ctx.log("action " + nextAction + " skipped: the dialog target is not a fleet of " + id);
    }

    private void switchFleet(QuestContext<S, T> ctx, QuestFleet fleet) {
        if (nextTarget != null) fleet.info().target = nextTarget.apply(ctx.random("target:" + id));
        ctx.fleets().reassign(fleet, nextRole);
        onSwitch.accept(fleet.fleet());
    }

    private boolean hasFleet(QuestContext<S, T> ctx) {
        return !fleets(ctx).isEmpty();
    }

    private List<QuestFleet> fleets(QuestContext<S, T> ctx) {
        List<QuestFleet> fleets = new ArrayList<>(ctx.fleets().get(role));
        if (nextRole != null) fleets.addAll(ctx.fleets().get(nextRole));
        return fleets;
    }

    private Record record(QuestContext<S, T> ctx) {
        return ctx.state().intercepts().computeIfAbsent(id, key -> new Record());
    }

    @Override
    protected void devInfo(QuestContext<S, T> ctx, List<String> lines) {
        List<QuestFleet> fleets = fleets(ctx);
        StringBuilder line = new StringBuilder(id + ": spawns " + record(ctx).spawns);
        for (QuestFleet fleet : fleets) {
            line.append(", ").append(fleet.role()).append(" age ").append(String.format("%.1f", fleet.info().age));
        }
        lines.add(line.toString());
    }
}
