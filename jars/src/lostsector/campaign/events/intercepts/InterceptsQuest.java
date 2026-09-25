package lostsector.campaign.events.intercepts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import lostsector.campaign.bounties.abyss.AbyssSpawner;
import lostsector.campaign.kesteven.quest.KestevenQuest;
import lostsector.dialogue.rules.nskr_debt;
import lostsector.helper.FleetHelper.GuardAttackBehaviour;
import lostsector.helper.FleetHelper.GuardMovementBehaviour;
import lostsector.helper.FleetHelper.InterceptBehaviour;
import lostsector.helper.Ids;
import lostsector.helper.SectorLookup;
import lostsector.helper.SystemHelper;
import lostsector.quest.FleetOrders;
import lostsector.quest.FleetRole;
import lostsector.quest.NoFlags;
import lostsector.quest.Quest;
import lostsector.quest.QuestModule;
import lostsector.quest.Quests;
import lostsector.quest.modules.InterceptEncounter;
import lostsector.quest.modules.InterceptEncounter.Repeat;
import lostsector.quest.modules.PayOffEncounter;

import java.util.List;

// Record quest "ic": four one-shot fleets that hunt the player in hyperspace. Records and roles share the names below.
public final class InterceptsQuest extends Quest<InterceptsStage, InterceptsState> {

    public static final String ID = "ic";

    static final String ARO = "aro";
    static final String MESSENGER = "messenger";
    static final String MESSENGER_LEAVING = "messengerLeaving";
    static final String AUTO_HUNTER = "autoHunter";
    static final String AUTO_HUNTER_GUARD = "autoHunterGuard";
    static final String COLLECTOR = "collector";
    static final String COLLECTOR_LEAVING = "collectorLeaving";

    private static final float CORE_DISTANCE = 25000f;
    private static final float AUTO_HUNTER_AUTOMATED_DP = 75f;
    private static final int COLLECTOR_MIN_DEBT = 250000;
    // Below this many credits the collector takes no part payment.
    private static final int COLLECTOR_PART_MINIMUM = 100000;
    private static final float KESTEVEN_HOSTILE = -0.5f;

    public InterceptsQuest() {
        super(ID, InterceptsStage.class, NoFlags.class, InterceptsStage.RUNNING);
    }

    @Override
    protected InterceptsState createState() {
        return new InterceptsState();
    }

    @Override
    protected List<QuestModule<InterceptsStage, InterceptsState>> createModules() {
        return List.of(aro(), messenger(), autoHunter(), collector(), collectorDemand());
    }

    // Hunts players who carry Abyss bounty ships.
    private static InterceptEncounter<InterceptsStage, InterceptsState> aro() {
        return new InterceptEncounter<InterceptsStage, InterceptsState>(ARO, ARO,
                FleetRole.of(FleetOrders.intercept(InterceptBehaviour.AROUND).withdrawWhenBeaten().withdrawAfter(45f)),
                Repeat.ONCE, 0.01f,
                ctx -> InterceptEncounter.playerInHyperspaceWithin(CORE_DISTANCE)
                        && AbyssSpawner.hasBountyShips(Global.getSector().getPlayerFleet()),
                InterceptsFleets::aro)
                .finish(InterceptsFleets::flyAsMercenaries);
    }

    // Delivers the "LZ" warning while the Kesteven questline is in its messenger window, then leaves for a pirate market.
    private static InterceptEncounter<InterceptsStage, InterceptsState> messenger() {
        return new InterceptEncounter<InterceptsStage, InterceptsState>(MESSENGER, MESSENGER,
                FleetRole.of(FleetOrders.intercept(InterceptBehaviour.DIRECT).withdrawWhenBeaten().withdrawAfter(20f)),
                Repeat.ONCE, 0.04f,
                ctx -> KestevenQuest.inMessengerWindow() && InterceptEncounter.playerInHyperspaceWithin(CORE_DISTANCE),
                InterceptsFleets::messenger)
                .finish(InterceptsFleets::flyAsMercenaries)
                .switchOnAction("messengerMet", MESSENGER_LEAVING,
                        FleetRole.of(FleetOrders.leave().withdrawWhenBeaten().withdrawAfter(20f)),
                        random -> SystemHelper.getRandomFactionMarket(random, Factions.PIRATES),
                        ctx -> KestevenQuest.reportMessengerMet());
    }

    // Hunts players who fly many automated ships and are hostile to the Luddic Path; after 30 days it guards a Path market.
    private static InterceptEncounter<InterceptsStage, InterceptsState> autoHunter() {
        return new InterceptEncounter<InterceptsStage, InterceptsState>(AUTO_HUNTER, AUTO_HUNTER,
                FleetRole.of(FleetOrders.intercept(InterceptBehaviour.AROUND).withdrawWhenBeaten()),
                Repeat.ONCE, 0.01f,
                ctx -> InterceptEncounter.playerInHyperspaceWithin(CORE_DISTANCE * 2f) && autoHunterCanSpawn()
                        && Global.getSector().getFaction(Factions.PLAYER).getRelationship(Factions.LUDDIC_PATH) < 0f,
                InterceptsFleets::autoHunter)
                .finish(InterceptsFleets::addMachineSpirits)
                .switchAfter(30f, AUTO_HUNTER_GUARD,
                        FleetRole.of(FleetOrders.guard(GuardMovementBehaviour.ORBIT, GuardAttackBehaviour.PLAYER, 0.01f).withdrawWhenBeaten()),
                        random -> SystemHelper.getRandomFactionMarket(random, Factions.LUDDIC_PATH));
    }

    // Collects a large Kesteven debt from a player hostile to Kesteven. It goes home once paid (action
    // collectorLeave from its comm link) or once the player is no longer hostile to Kesteven.
    private static InterceptEncounter<InterceptsStage, InterceptsState> collector() {
        return new InterceptEncounter<InterceptsStage, InterceptsState>(COLLECTOR, COLLECTOR,
                FleetRole.of(FleetOrders.intercept(InterceptBehaviour.AROUND).withdrawWhenBeaten().withdrawAfter(45f)),
                Repeat.ONCE, 0.01f,
                ctx -> SectorLookup.kestevenExists() && InterceptEncounter.playerInHyperspaceWithin(CORE_DISTANCE)
                        && kestevenHostile() && nskr_debt.getDebt() >= COLLECTOR_MIN_DEBT,
                InterceptsFleets::collector)
                .switchOnAction("collectorLeave", COLLECTOR_LEAVING,
                        FleetRole.of(FleetOrders.leave().withdrawWhenBeaten().withdrawAfter(45f)),
                        random -> SystemHelper.getRandomFactionMarket(random, Ids.KESTEVEN_FACTION_ID),
                        ctx -> {
                        })
                .switchWhen(ctx -> !kestevenHostile())
                .onSwitch(InterceptsFleets::ignoreOtherFleets);
    }

    // The collector's demand: the whole debt, or all the player's credits when they hold at least the part minimum.
    private static PayOffEncounter<InterceptsStage, InterceptsState> collectorDemand() {
        return new PayOffEncounter<InterceptsStage, InterceptsState>(COLLECTOR, PayOffEncounter.CREDITS,
                ctx -> nskr_debt.getDebt(), COLLECTOR_PART_MINIMUM)
                .demandsWhile(ctx -> kestevenHostile())
                .onPaid((ctx, amount) -> nskr_debt.addDebt(-amount));
    }

    private static boolean kestevenHostile() {
        return Global.getSector().getFaction(Ids.KESTEVEN_FACTION_ID).getRelationship(Factions.PLAYER) <= KESTEVEN_HOSTILE;
    }

    // Automated hulls, counted by deployment points, including SotF's Sierra's Concord.
    private static boolean autoHunterCanSpawn() {
        float automated = 0f;
        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
            if (member.isFighterWing() || member.getVariant() == null) continue;
            if (member.getVariant().getHullMods().contains(HullMods.AUTOMATED)
                    || member.getVariant().getHullMods().contains("sotf_sierrasconcord")) {
                automated += member.getDeploymentPointsCost();
            }
        }
        return automated >= AUTO_HUNTER_AUTOMATED_DP;
    }

    public static InterceptsState state() {
        return Quests.state(ID);
    }
}
