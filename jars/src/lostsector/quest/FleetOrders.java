package lostsector.quest;

import lostsector.helper.FleetHelper;
import lostsector.helper.fleet.FleetInfo;

import java.util.Random;

// A definition, never saved. A behavior FleetHelper does not provide is added there first, then here.
public final class FleetOrders {

    private enum Kind {
        NONE,
        INTERCEPT,
        GUARD,
        LEAVE,
        RAID
    }

    private final Kind kind;
    private final FleetHelper.InterceptBehaviour intercept;
    private final FleetHelper.GuardMovementBehaviour movement;
    private final FleetHelper.GuardAttackBehaviour attack;
    private final float playerInterceptChance;
    private final String orbitText;
    private final boolean withdrawHome;

    private final boolean withdrawWhenBeaten;
    private final float withdrawAfterDays;

    private FleetOrders(Kind kind, FleetHelper.InterceptBehaviour intercept, FleetHelper.GuardMovementBehaviour movement,
                        FleetHelper.GuardAttackBehaviour attack, float playerInterceptChance, String orbitText,
                        boolean withdrawHome, boolean withdrawWhenBeaten, float withdrawAfterDays) {
        this.kind = kind;
        this.intercept = intercept;
        this.movement = movement;
        this.attack = attack;
        this.playerInterceptChance = playerInterceptChance;
        this.orbitText = orbitText;
        this.withdrawHome = withdrawHome;
        this.withdrawWhenBeaten = withdrawWhenBeaten;
        this.withdrawAfterDays = withdrawAfterDays;
    }

    private FleetOrders(Kind kind, FleetHelper.InterceptBehaviour intercept, FleetHelper.GuardMovementBehaviour movement,
                        FleetHelper.GuardAttackBehaviour attack, float playerInterceptChance) {
        this(kind, intercept, movement, attack, playerInterceptChance, null, false, false, Float.POSITIVE_INFINITY);
    }

    // Vanilla assignments from SimpleFleet only.
    public static FleetOrders none() {
        return new FleetOrders(Kind.NONE, null, null, null, 0f);
    }

    public static FleetOrders intercept(FleetHelper.InterceptBehaviour behaviour) {
        if (behaviour == null) throw new IllegalArgumentException("intercept behaviour must not be null");
        return new FleetOrders(Kind.INTERCEPT, behaviour, null, null, 0f);
    }

    public static FleetOrders guard(FleetHelper.GuardMovementBehaviour movement, FleetHelper.GuardAttackBehaviour attack, float playerInterceptChance) {
        if (movement == null || attack == null) throw new IllegalArgumentException("guard behaviours must not be null");
        return new FleetOrders(Kind.GUARD, null, movement, attack, playerInterceptChance);
    }

    // Goes to FleetInfo.target and despawns there; the quest sets the target before giving the fleet this role.
    public static FleetOrders leave() {
        return new FleetOrders(Kind.LEAVE, null, null, null, 0f);
    }

    // Orbit FleetInfo.target, which the quest sets after spawning; clearing it, or the fleet falling below a fifth of
    // its spawn strength, sends the fleet home (withdrawHome) or to a random market of its faction, where it despawns.
    public static FleetOrders raid(String orbitText, boolean withdrawHome) {
        if (orbitText == null) throw new IllegalArgumentException("raid orbit text must not be null");
        return new FleetOrders(Kind.RAID, null, null, null, 0f, orbitText, withdrawHome, false, Float.POSITIVE_INFINITY);
    }

    // Below a quarter of its spawn strength the fleet gets no more orders and despawns once out of the player's sight.
    public FleetOrders withdrawWhenBeaten() {
        return new FleetOrders(kind, intercept, movement, attack, playerInterceptChance, orbitText, withdrawHome, true, withdrawAfterDays);
    }

    // Older than the given days (FleetInfo.age), the fleet gets no more orders and despawns once out of the player's sight.
    public FleetOrders withdrawAfter(float days) {
        if (!(days > 0f)) throw new IllegalArgumentException("withdraw days must be positive");
        return new FleetOrders(kind, intercept, movement, attack, playerInterceptChance, orbitText, withdrawHome, withdrawWhenBeaten, days);
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
            default:
                break;
        }
    }
}
