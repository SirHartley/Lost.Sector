package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import lostsector.helper.FleetHelper.InterceptBehaviour;
import lostsector.helper.SystemHelper;
import lostsector.quest.FleetOrders;
import lostsector.quest.FleetRole;
import lostsector.quest.modules.InterceptEncounter;
import lostsector.quest.modules.InterceptEncounter.Repeat;
import lostsector.quest.modules.PayOffEncounter;

import java.util.EnumSet;
import java.util.Set;

// The Tri-Tachyon collector: a one-shot "Black Ops" fleet that hunts a player carrying Artifact Electronics while the
// questline runs and demands all of them. Its fleet and its demand are the shared modules InterceptEncounter and
// PayOffEncounter, both active in every stage, so a collector met after stage 15 can still be paid; the conversation
// is the # KESTEVEN QUESTLINE: COLLECTOR block of rules.csv.
final class KestevenCollector {

    // Record, role, random purposes and the prefix of the checks, action and token.
    static final String ID = "ttCollector";
    static final String ROLE_LEAVING = "ttCollectorLeaving";
    static final String ELECTRONICS = "nskr_electronics";

    private static final float DAILY_CHANCE = 0.03f;
    private static final float CORE_DISTANCE = 25000f;
    private static final float MIN_ELECTRONICS = 50f;
    // Both roles despawn out of the player's sight after this age.
    private static final float MAX_AGE_DAYS = 60f;
    // Legacy stages 2 to 15.
    private static final Set<KestevenStage> SPAWN_STAGES = EnumSet.range(KestevenStage.JOB1_DONE, KestevenStage.JOB5_MEETING);

    private KestevenCollector() {
    }

    // Rolls once a day; the player has to be in hyperspace near the core with at least 50 Artifact Electronics.
    static InterceptEncounter<KestevenStage, KestevenState> encounter() {
        return new InterceptEncounter<KestevenStage, KestevenState>(ID, ID,
                FleetRole.of(FleetOrders.intercept(InterceptBehaviour.AROUND).withdrawAfter(MAX_AGE_DAYS)),
                Repeat.ONCE, DAILY_CHANCE,
                ctx -> SPAWN_STAGES.contains(ctx.stage()) && InterceptEncounter.playerInHyperspaceWithin(CORE_DISTANCE)
                        && Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity(ELECTRONICS) >= MIN_ELECTRONICS,
                KestevenFleets::ttCollector)
                .switchOnAction(ID + "Leave", ROLE_LEAVING,
                        FleetRole.of(FleetOrders.leave().withdrawAfter(MAX_AGE_DAYS)),
                        random -> SystemHelper.getRandomFactionMarket(random, Factions.TRITACHYON),
                        ctx -> {
                        })
                .onSwitch(fleet -> fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true));
    }

    // Everything the player holds; one unit is enough for the hand-over.
    static PayOffEncounter<KestevenStage, KestevenState> demand() {
        return new PayOffEncounter<KestevenStage, KestevenState>(ID, ELECTRONICS, ctx -> PayOffEncounter.EVERYTHING, 1)
                .onPaid((ctx, amount) -> {
                    ctx.set(KestevenFlag.COLLECTOR_PAID);
                    calmDown(ctx.target());
                });
    }

    // The old payment cleared the fleet's whole memory; the framework's fleet keys must stay, so only the flags the
    // builder set are removed. The rows then make the fleet non-aggressive and let the player disengage.
    private static void calmDown(SectorEntityToken target) {
        if (!(target instanceof CampaignFleetAPI)) return;
        for (String flag : KestevenFleets.TT_COLLECTOR_FLAGS) {
            target.getMemoryWithoutUpdate().unset(flag);
        }
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
    }
}
