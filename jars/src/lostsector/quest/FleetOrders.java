package lostsector.quest;

import lostsector.helper.FleetHelper;
import lostsector.helper.fleet.FleetInfo;

// A definition, never saved. A behavior FleetHelper does not provide is added there first, then here.
public final class FleetOrders {

    private enum Kind {
        NONE,
        INTERCEPT,
        GUARD
    }

    private final Kind kind;
    private final FleetHelper.InterceptBehaviour intercept;
    private final FleetHelper.GuardMovementBehaviour movement;
    private final FleetHelper.GuardAttackBehaviour attack;
    private final float playerInterceptChance;

    private FleetOrders(Kind kind, FleetHelper.InterceptBehaviour intercept, FleetHelper.GuardMovementBehaviour movement,
                        FleetHelper.GuardAttackBehaviour attack, float playerInterceptChance) {
        this.kind = kind;
        this.intercept = intercept;
        this.movement = movement;
        this.attack = attack;
        this.playerInterceptChance = playerInterceptChance;
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

    // Called by QuestManager every 0.1 days, the pace FleetHelper's AI methods are written for.
    void apply(FleetInfo info) {
        switch (kind) {
            case INTERCEPT:
                FleetHelper.gotoAndInterceptPlayerAI(info.fleet, info, intercept);
                break;
            case GUARD:
                FleetHelper.guardTargetAI(info.fleet, info, movement, attack, playerInterceptChance);
                break;
            default:
                break;
        }
    }
}
