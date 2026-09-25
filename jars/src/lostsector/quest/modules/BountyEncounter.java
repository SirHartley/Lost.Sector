package lostsector.quest.modules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import lostsector.helper.fleet.SimpleFleet;
import lostsector.quest.Declarations;
import lostsector.quest.FleetOrders;
import lostsector.quest.FleetRole;
import lostsector.quest.IntelSpec;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestFleet;
import lostsector.quest.QuestModule;
import lostsector.quest.QuestStage;
import lostsector.quest.QuestState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

// One named bounty: a fleet placed once per game, intel on first sighting, a reward when the player loots it beaten,
// and the intel's completion afterwards. The quest's state implements Host and keeps one Record per bounty; the id
// names the record, the fleet role, the intel key and the token and check prefix (README "Shared modules").
public final class BountyEncounter<S extends Enum<S> & QuestStage, T extends QuestState<S> & BountyEncounter.Host>
        extends QuestModule<S, T> {

    public enum Status {
        // No location was found, so nothing spawned.
        NOT_PLACED,
        ACTIVE,
        // Beaten; the intel completes on the next unpaused frame, after any open dialog.
        DEFEATED,
        DONE
    }

    // Update key of the intel message sent right after the first sighting.
    public static final String UPDATE_SIGHTED = "sighted";

    private final String id;
    private final String beatenRole;
    private final String icon;
    private final FleetRole fleetRole;
    private final Function<Random, SectorEntityToken> location;
    private final BiFunction<SectorEntityToken, Random, SimpleFleet> builder;
    private final Predicate<QuestFleet> defeated;
    private BiConsumer<CampaignFleetAPI, Random> finish = (fleet, random) -> {
    };
    private Reward<S, T> reward = (ctx, loot, plugin) -> {
    };
    private int payout;
    private Payout payoutRule;
    private Consumer<QuestContext<S, T>> onSighted = ctx -> {
    };
    private Set<String> revealedHulls = Set.of();
    private String penaltyFaction;
    private float penaltyDelta;
    private float penaltyAbove;
    private boolean mapFollowsFleet;
    private String guardTrigger;
    private Supplier<List<SectorEntityToken>> guarded;
    private Consumer<IntelSpec> intelOptions = spec -> {
    };
    private Function<Record, String> completionSound = record -> null;

    public interface Host {

        // Record id to record; the state creates the map, the module fills it.
        Map<String, Record> bounties();
    }

    // Saved inside the quest state.
    public static final class Record {

        Status status;
        SectorEntityToken location;
        CampaignFleetAPI fleet;
        String fleetName;
        boolean sighted;
        boolean looted;
        int paid;
        boolean relationsLost;
        List<SectorEntityToken> guarded = new ArrayList<>();

        public Status status() {
            return status;
        }

        // Null until placed, or when no location was found.
        public SectorEntityToken location() {
            return location;
        }

        public boolean sighted() {
            return sighted;
        }

        public int paid() {
            return paid;
        }

        public boolean looted() {
            return looted;
        }
    }

    // Runs once, at the loot of the beaten fleet: items added to loot show on the loot screen.
    public interface Reward<S extends Enum<S> & QuestStage, T extends QuestState<S>> {

        void grant(QuestContext<S, T> ctx, CargoAPI loot, FleetEncounterContextPlugin plugin);
    }

    // The credits actually paid out of the configured amount at the loot of the beaten fleet; 0 pays nothing.
    public interface Payout {

        int paid(int amount, FleetEncounterContextPlugin plugin);
    }

    // The location picker gets the saved random "location:<id>" and returns the entity to spawn at, or null; the
    // builder gets that entity and the saved random "fleet:<id>" and returns the fleet unbuilt. The fleet is beaten
    // when defeated returns true for it after a battle or at its loot, or when it is destroyed; the QuestFleet gives
    // the FleetInfo, for FleetHelper.getOriginalFlagship.
    @SafeVarargs
    public BountyEncounter(String id, String icon, FleetRole fleetRole, Function<Random, SectorEntityToken> location,
                           BiFunction<SectorEntityToken, Random, SimpleFleet> builder, Predicate<QuestFleet> defeated,
                           S... stages) {
        super(stages);
        if (id == null || icon == null || fleetRole == null || location == null || builder == null || defeated == null) {
            throw new IllegalArgumentException("bounty " + id + " is missing a parameter");
        }
        this.id = id;
        this.beatenRole = id + "Beaten";
        this.icon = icon;
        this.fleetRole = fleetRole;
        this.location = location;
        this.builder = builder;
        this.defeated = defeated;
    }

    // Runs after the fleet is built and registered, with the fleet random: placement fixes, faction, FleetHelper.update.
    public BountyEncounter<S, T> finish(BiConsumer<CampaignFleetAPI, Random> finish) {
        if (finish == null) throw new IllegalArgumentException("finish of " + id + " must not be null");
        this.finish = finish;
        return this;
    }

    public BountyEncounter<S, T> reward(Reward<S, T> reward) {
        if (reward == null) throw new IllegalArgumentException("reward of " + id + " must not be null");
        this.reward = reward;
        return this;
    }

    // Declares the token <id>Payout (the amount) and the check <id>Paid; the paid credits go through ctx.rewards().
    public BountyEncounter<S, T> payout(int amount, Payout rule) {
        if (amount <= 0 || rule == null) throw new IllegalArgumentException("payout of " + id + " needs a positive amount and a rule");
        this.payout = amount;
        this.payoutRule = rule;
        return this;
    }

    // Runs at the first sighting, before the intel is shown.
    public BountyEncounter<S, T> onSighted(Consumer<QuestContext<S, T>> onSighted) {
        if (onSighted == null) throw new IllegalArgumentException("onSighted of " + id + " must not be null");
        this.onSighted = onSighted;
        return this;
    }

    // At the loot of the beaten fleet, while the player faction's relationship with the faction is above onlyAbove, it
    // changes by delta through ctx.rewards().relationship. Declares the check <id>RelationsLost.
    public BountyEncounter<S, T> relationshipPenalty(String factionId, float delta, float onlyAbove) {
        if (factionId == null || delta == 0f) throw new IllegalArgumentException("relationship penalty of " + id + " needs a faction and a change");
        penaltyFaction = factionId;
        penaltyDelta = delta;
        penaltyAbove = onlyAbove;
        return this;
    }

    // The intel's map location is the hyperspace anchor of the fleet's star system, none while it is in hyperspace,
    // refreshed each day, instead of the placement location.
    public BountyEncounter<S, T> mapFollowsFleet() {
        mapFollowsFleet = true;
        return this;
    }

    // While the bounty is active, the entities' dialogs are claimed with the trigger (declared once per quest, so
    // bounties can share it); the claims are released at the defeat. Declares the check <id>Guards.
    public BountyEncounter<S, T> guards(String trigger, Supplier<List<SectorEntityToken>> entities) {
        if (trigger == null || entities == null) throw new IllegalArgumentException("guards of " + id + " need a trigger and entities");
        guardTrigger = trigger;
        guarded = entities;
        return this;
    }

    // Presentation options of the intel entry (README "Intel"), applied in declare.
    public BountyEncounter<S, T> intel(Consumer<IntelSpec> options) {
        if (options == null) throw new IllegalArgumentException("intel options of " + id + " must not be null");
        intelOptions = options;
        return this;
    }

    // The sounds.json id the completion message plays, from the record; null keeps the standard update sound.
    public BountyEncounter<S, T> completionSound(Function<Record, String> sound) {
        if (sound == null) throw new IllegalArgumentException("completion sound of " + id + " must not be null");
        completionSound = sound;
        return this;
    }

    // Base hull ids whose limited tooltip (Tags.SHIP_LIMITED_TOOLTIP) is lifted when the player recovers such a ship.
    public BountyEncounter<S, T> revealOnRecovery(String... hullIds) {
        revealedHulls = Set.of(hullIds);
        return this;
    }

    @Override
    protected void declare(Declarations<S, T> d) {
        d.role(id, fleetRole);
        d.role(beatenRole, FleetRole.of(FleetOrders.withdraw()));
        intelOptions.accept(d.intel(id, icon, Tags.INTEL_BOUNTY));
        d.token(id + "FleetName", ctx -> {
            Record record = ctx.state().bounties().get(id);
            return record == null || record.fleetName == null ? "" : record.fleetName;
        });
        d.token(id + "System", ctx -> {
            SectorEntityToken at = placed(ctx);
            return at == null ? "" : at.getContainingLocation().getName();
        });
        d.token(id + "Entity", ctx -> {
            SectorEntityToken at = placed(ctx);
            return at == null ? "" : at.getName();
        });
        d.token(id + "FleetSystem", ctx -> {
            CampaignFleetAPI fleet = fleet(ctx);
            return fleet == null || fleet.isInHyperspace() || fleet.getStarSystem() == null ? "" : fleet.getStarSystem().getName();
        });
        d.check(id + "InHyperspace", ctx -> {
            CampaignFleetAPI fleet = fleet(ctx);
            return fleet != null && fleet.isInHyperspace();
        });
        d.check(id + "Looted", ctx -> {
            Record record = ctx.state().bounties().get(id);
            return record != null && record.looted;
        });
        if (payoutRule != null) {
            d.token(id + "Payout", ctx -> Misc.getDGSCredits(payout));
            d.token(id + "PaidAmount", ctx -> {
                Record record = ctx.state().bounties().get(id);
                return record == null ? "" : Misc.getDGSCredits(record.paid);
            });
            d.check(id + "Paid", ctx -> {
                Record record = ctx.state().bounties().get(id);
                return record != null && record.paid > 0;
            });
            // Within a credit of the amount, as the old Peacekeepers intel compared the rounded payment.
            d.check(id + "PaidInFull", ctx -> {
                Record record = ctx.state().bounties().get(id);
                return record != null && record.paid >= payout - 1;
            });
        }
        if (penaltyFaction != null) {
            d.check(id + "RelationsLost", ctx -> {
                Record record = ctx.state().bounties().get(id);
                return record != null && record.relationsLost;
            });
        }
        if (guardTrigger != null) {
            if (!d.triggers().contains(guardTrigger)) d.trigger(guardTrigger);
            d.check(id + "Guards", this::guardsTarget);
        }
    }

    @Override
    protected void onStart(QuestContext<S, T> ctx) {
        Record record = record(ctx);
        if (record.status != null) return;
        record.status = Status.NOT_PLACED;
        record.location = location.apply(ctx.random("location:" + id));
        if (record.location == null) {
            ctx.log("bounty " + id + " not placed: no location");
            return;
        }
        Random random = ctx.random("fleet:" + id);
        CampaignFleetAPI fleet = ctx.fleets().spawn(id, id, builder.apply(record.location, random));
        if (fleet == null) return;
        finish.accept(fleet, random);
        record.fleet = fleet;
        record.fleetName = fleet.getName();
        record.status = Status.ACTIVE;
        if (guarded != null) {
            for (SectorEntityToken entity : guarded.get()) {
                if (entity == null) continue;
                record.guarded.add(entity);
                ctx.claimDialog(entity, guardTrigger);
            }
        }
        ctx.log("bounty " + id + " placed at " + record.location.getName() + " in " + record.location.getContainingLocation().getName());
    }

    @Override
    protected void onDay(QuestContext<S, T> ctx) {
        Record record = ctx.state().bounties().get(id);
        if (mapFollowsFleet && record != null && record.status == Status.ACTIVE && ctx.intel().isShown(id)) {
            ctx.intel().setMapLocation(id, fleetAnchor(record));
        }
    }

    // Only while the player shares a location with the unsighted fleet, and once after a defeat.
    @Override
    protected boolean wantsFrames(QuestContext<S, T> ctx) {
        Record record = ctx.state().bounties().get(id);
        if (record == null) return false;
        if (record.status == Status.DEFEATED) return true;
        if (record.status != Status.ACTIVE || record.sighted || record.fleet == null || !record.fleet.isAlive()) return false;
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        return player != null && player.getContainingLocation() == record.fleet.getContainingLocation();
    }

    @Override
    protected void onFrame(QuestContext<S, T> ctx, float amount) {
        Record record = record(ctx);
        if (record.status == Status.DEFEATED) {
            if (ctx.intel().isShown(id)) ctx.intel().complete(id, "", completionSound.apply(record));
            record.status = Status.DONE;
            return;
        }
        if (record.fleet.isVisibleToSensorsOf(Global.getSector().getPlayerFleet())) sight(ctx, record);
    }

    private void sight(QuestContext<S, T> ctx, Record record) {
        record.sighted = true;
        onSighted.accept(ctx);
        ctx.intel().show(id);
        ctx.intel().setMapLocation(id, mapFollowsFleet ? fleetAnchor(record) : record.location);
        ctx.intel().update(id, UPDATE_SIGHTED);
    }

    // Vanilla reports the loot before the battle (FleetInteractionDialogPluginImpl CONTINUE_LOOT, then
    // applyAfterBattleEffectsIfThereWasABattle), so a player victory is rewarded here, while the bounty is still
    // active. A bounty another party beat first pays nothing.
    @Override
    protected void onLoot(QuestContext<S, T> ctx, QuestFleet fleet, FleetEncounterContextPlugin plugin, CargoAPI loot) {
        if (!id.equals(fleet.record())) return;
        Record record = record(ctx);
        if (record.status != Status.ACTIVE || record.looted || !defeated.test(fleet)) return;
        record.looted = true;
        reward.grant(ctx, loot, plugin);
        if (payoutRule != null) {
            int paid = payoutRule.paid(payout, plugin);
            if (paid > 0) {
                ctx.rewards().credits(paid);
                record.paid = paid;
            }
        }
        if (penaltyFaction != null && Global.getSector().getPlayerFaction().getRelationship(penaltyFaction) > penaltyAbove) {
            ctx.rewards().relationship(penaltyFaction, penaltyDelta);
            record.relationsLost = true;
        }
        defeat(ctx, record, fleet);
    }

    @Override
    protected void onBattle(QuestContext<S, T> ctx, QuestFleet fleet, BattleAPI battle, CampaignFleetAPI primaryWinner) {
        if (!id.equals(fleet.record())) return;
        if (defeated.test(fleet)) defeat(ctx, record(ctx), fleet);
    }

    @Override
    protected void onFleetGone(QuestContext<S, T> ctx, QuestFleet fleet, FleetDespawnReason reason, Object param) {
        if (!id.equals(fleet.record()) || !fleet.wasDestroyed(reason)) return;
        Record record = record(ctx);
        if (record.status != Status.ACTIVE) return;
        record.status = Status.DEFEATED;
        releaseGuarded(ctx, record);
    }

    // The fleet leaves its role, so rows keyed on the role flag stop matching, and despawns once out of sight.
    private void defeat(QuestContext<S, T> ctx, Record record, QuestFleet fleet) {
        if (record.status != Status.ACTIVE) return;
        record.status = Status.DEFEATED;
        releaseGuarded(ctx, record);
        fleet.fleet().getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        if (fleet.isRole(id)) ctx.fleets().reassign(fleet, beatenRole);
        ctx.log("bounty " + id + " defeated");
    }

    private void releaseGuarded(QuestContext<S, T> ctx, Record record) {
        for (SectorEntityToken entity : record.guarded) {
            ctx.releaseDialog(entity);
        }
        record.guarded.clear();
    }

    // The dialog target is guarded and the active fleet is in the player's location, so engage can start the fight.
    private boolean guardsTarget(QuestContext<S, T> ctx) {
        Record record = ctx.state().bounties().get(id);
        SectorEntityToken target = ctx.target();
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        return record != null && record.status == Status.ACTIVE && target != null && record.guarded.contains(target)
                && record.fleet != null && record.fleet.isAlive() && player != null
                && record.fleet.getContainingLocation() == player.getContainingLocation();
    }

    // Null while the fleet is in hyperspace, as the old Peacekeepers intel showed no map location there.
    private static SectorEntityToken fleetAnchor(Record record) {
        if (record.fleet == null || record.fleet.isInHyperspace() || record.fleet.getStarSystem() == null) return null;
        return record.fleet.getStarSystem().getHyperspaceAnchor();
    }

    @Override
    protected void onShipsRecovered(QuestContext<S, T> ctx, List<FleetMemberAPI> ships) {
        if (revealedHulls.isEmpty()) return;
        for (FleetMemberAPI member : ships) {
            ShipVariantAPI variant = member.getVariant();
            if (!revealedHulls.contains(variant.getHullSpec().getBaseHullId())) continue;
            variant.removeTag(Tags.SHIP_LIMITED_TOOLTIP);
            member.setVariant(variant, false, false);
        }
    }

    private CampaignFleetAPI fleet(QuestContext<S, T> ctx) {
        Record record = ctx.state().bounties().get(id);
        return record == null || record.fleet == null || !record.fleet.isAlive() ? null : record.fleet;
    }

    private SectorEntityToken placed(QuestContext<S, T> ctx) {
        Record record = ctx.state().bounties().get(id);
        return record == null ? null : record.location;
    }

    private Record record(QuestContext<S, T> ctx) {
        return ctx.state().bounties().computeIfAbsent(id, key -> new Record());
    }

    @Override
    protected void devInfo(QuestContext<S, T> ctx, List<String> lines) {
        Record record = ctx.state().bounties().get(id);
        if (record == null) {
            lines.add(id + ": no record");
            return;
        }
        String at = record.location == null ? "nowhere" : record.location.getName() + ", " + record.location.getContainingLocation().getName();
        lines.add(id + ": " + record.status + " at " + at + ", sighted " + record.sighted + ", looted " + record.looted
                + ", paid " + record.paid + ", relations lost " + record.relationsLost + ", fleet " + (record.fleet != null && record.fleet.isAlive() ? "alive" : "gone"));
    }
}
