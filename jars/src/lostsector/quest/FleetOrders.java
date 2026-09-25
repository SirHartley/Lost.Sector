package lostsector.quest;

import lostsector.helper.FleetHelper;
import lostsector.helper.fleet.FleetInfo;

import java.util.Random;
import java.util.function.BiConsumer;

// A definition, never saved. A behavior FleetHelper does not provide is added there first, then here.
public final class FleetOrders {

    private enum Kind {
        NONE,
        INTERCEPT,
        GUARD,
        LEAVE,
        RAID,
        WITHDRAW,
        PATROL
    }

    private Kind kind;
    private FleetHelper.InterceptBehaviour intercept;
    private FleetHelper.GuardMovementBehaviour movement;
    private FleetHelper.GuardAttackBehaviour attack;
    private float playerInterceptChance;
    private String orbitText;
    private boolean withdrawHome;

    private String patrolFaction;
    private float patrolSwitchDays;
    private String patrolText;
    private float reinforceBelow;
    private BiConsumer<FleetInfo, Random> reinforce;

    private boolean withdrawWhenBeaten;
    private float withdrawAfterDays = Float.POSITIVE_INFINITY;

    private FleetOrders(Kind kind) {
        this.kind = kind;
    }

    private FleetOrders copy() {
        FleetOrders copy = new FleetOrders(kind);
        copy.intercept = intercept;
        copy.movement = movement;
        copy.attack = attack;
        copy.playerInterceptChance = playerInterceptChance;
        copy.orbitText = orbitText;
        copy.withdrawHome = withdrawHome;
        copy.patrolFaction = patrolFaction;
        copy.patrolSwitchDays = patrolSwitchDays;
        copy.patrolText = patrolText;
        copy.reinforceBelow = reinforceBelow;
        copy.reinforce = reinforce;
        copy.withdrawWhenBeaten = withdrawWhenBeaten;
        copy.withdrawAfterDays = withdrawAfterDays;
        return copy;
    }

    // Vanilla assignments from SimpleFleet only.
    public static FleetOrders none() {
        return new FleetOrders(Kind.NONE);
    }

    public static FleetOrders intercept(FleetHelper.InterceptBehaviour behaviour) {
        if (behaviour == null) throw new IllegalArgumentException("intercept behaviour must not be null");
        FleetOrders orders = new FleetOrders(Kind.INTERCEPT);
        orders.intercept = behaviour;
        return orders;
    }

    public static FleetOrders guard(FleetHelper.GuardMovementBehaviour movement, FleetHelper.GuardAttackBehaviour attack, float playerInterceptChance) {
        if (movement == null || attack == null) throw new IllegalArgumentException("guard behaviours must not be null");
        FleetOrders orders = new FleetOrders(Kind.GUARD);
        orders.movement = movement;
        orders.attack = attack;
        orders.playerInterceptChance = playerInterceptChance;
        return orders;
    }

    // Goes to FleetInfo.target and despawns there; the quest sets the target before giving the fleet this role.
    public static FleetOrders leave() {
        return new FleetOrders(Kind.LEAVE);
    }

    // Orbit FleetInfo.target, which the quest sets after spawning; clearing it, or the fleet falling below a fifth of
    // its spawn strength, sends the fleet home (withdrawHome) or to a random market of its faction, where it despawns.
    public static FleetOrders raid(String orbitText, boolean withdrawHome) {
        if (orbitText == null) throw new IllegalArgumentException("raid orbit text must not be null");
        FleetOrders orders = new FleetOrders(Kind.RAID);
        orders.orbitText = orbitText;
        orders.withdrawHome = withdrawHome;
        return orders;
    }

    // Keeps its last assignment and despawns once out of the player's sight, for a fleet whose part is over.
    public static FleetOrders withdraw() {
        return new FleetOrders(Kind.WITHDRAW);
    }

    // Patrols the system of FleetInfo.target (FleetInfo.home until the first switch) with patrolText; after switchDays
    // there, it moves on to a random market of the faction.
    public static FleetOrders patrolMarkets(String factionId, float switchDays, String patrolText) {
        if (factionId == null || patrolText == null || !(switchDays > 0f)) {
            throw new IllegalArgumentException("patrol orders need a faction, a text and positive switch days");
        }
        FleetOrders orders = new FleetOrders(Kind.PATROL);
        orders.patrolFaction = factionId;
        orders.patrolSwitchDays = switchDays;
        orders.patrolText = patrolText;
        return orders;
    }

    // Patrol orders only: at each switch, a fleet below the fraction of its spawn strength runs reinforce.
    public FleetOrders reinforceBelow(float fraction, BiConsumer<FleetInfo, Random> reinforce) {
        if (kind != Kind.PATROL || reinforce == null || !(fraction > 0f)) {
            throw new IllegalArgumentException("reinforcement needs patrol orders, a positive fraction and a reinforcement");
        }
        FleetOrders copy = copy();
        copy.reinforceBelow = fraction;
        copy.reinforce = reinforce;
        return copy;
    }

    // Below a quarter of its spawn strength the fleet gets no more orders and despawns once out of the player's sight.
    public FleetOrders withdrawWhenBeaten() {
        FleetOrders copy = copy();
        copy.withdrawWhenBeaten = true;
        return copy;
    }

    // Older than the given days (FleetInfo.age), the fleet gets no more orders and despawns once out of the player's sight.
    public FleetOrders withdrawAfter(float days) {
        if (!(days > 0f)) throw new IllegalArgumentException("withdraw days must be positive");
        FleetOrders copy = copy();
        copy.withdrawAfterDays = days;
        return copy;
    }

    // Called by QuestManager every 0.1 days, the pace FleetHelper's AI methods are written for. A withdrawing fleet
    // keeps its last assignment until it despawns. The random is the owning quest's saved "fleetOrders" sequence.
    void apply(FleetInfo info, Random random) {
        if ((withdrawWhenBeaten && FleetHelper.isBeaten(info)) || info.age > withdrawAfterDays) {
            FleetHelper.despawnOutOfSight(info.fleet);
            return;
        }
        switch (kind) {
            case INTERCEPT:
                FleetHelper.gotoAndInterceptPlayerAI(info.fleet, info, intercept);
                break;
            case GUARD:
                FleetHelper.guardTargetAI(info.fleet, info, movement, attack, playerInterceptChance);
                break;
            case LEAVE:
                FleetHelper.goToTargetAndDespawnAI(info.fleet, info);
                break;
            case RAID:
                FleetHelper.raidTargetAI(info.fleet, info, orbitText, withdrawHome, random);
                break;
            case WITHDRAW:
                FleetHelper.despawnOutOfSight(info.fleet);
                break;
            case PATROL:
                if (FleetHelper.patrolMarketsAI(info.fleet, info, patrolFaction, patrolSwitchDays, patrolText, random)
                        && reinforce != null && info.fleet.getFleetPoints() < info.strength * reinforceBelow) {
                    reinforce.accept(info, random);
                }
                break;
            default:
                break;
        }
    }
}
