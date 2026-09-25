package lostsector.quest;

import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;

import java.lang.reflect.Modifier;

// A definition, never saved; fleets find their role again through QuestFleets.ROLE_KEY in fleet memory.
public final class FleetRole {

    private final FleetOrders orders;
    private boolean persistent;
    private FIDConfigGen config;
    private String defeatTrigger;

    private FleetRole(FleetOrders orders) {
        this.orders = orders;
    }

    public static FleetRole of(FleetOrders orders) {
        if (orders == null) throw new IllegalArgumentException("fleet orders must not be null");
        return new FleetRole(orders);
    }

    // Survives the stop of its module.
    public FleetRole persistent() {
        persistent = true;
        return this;
    }

    // The generator is saved in fleet memory under MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, so it must
    // be a top-level or static nested class: a lambda, anonymous or inner class would drag its enclosing objects into the save.
    public FleetRole config(FIDConfigGen gen) {
        Class<?> type = gen == null ? null : gen.getClass();
        if (type == null || type.isAnonymousClass() || type.isLocalClass() || type.isSynthetic() || type.isHidden()
                || (type.isMemberClass() && !Modifier.isStatic(type.getModifiers()))) {
            throw new IllegalArgumentException("FIDConfigGen must be a named top-level or static nested class: " + type);
        }
        config = gen;
        return this;
    }

    // Misc.addDefeatTrigger on spawn; the trigger must also be declared with Declarations.trigger.
    public FleetRole defeatTrigger(String trigger) {
        if (trigger == null || trigger.isEmpty()) throw new IllegalArgumentException("defeat trigger must not be empty");
        defeatTrigger = trigger;
        return this;
    }

    FleetOrders orders() {
        return orders;
    }

    boolean isPersistent() {
        return persistent;
    }

    FIDConfigGen configGen() {
        return config;
    }

    String defeatTriggerName() {
        return defeatTrigger;
    }
}
