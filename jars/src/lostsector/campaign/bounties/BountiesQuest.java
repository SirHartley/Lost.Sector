package lostsector.campaign.bounties;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import lostsector.campaign.events.hints.HintManager;
import lostsector.quest.FleetOrders;
import lostsector.quest.FleetRole;
import lostsector.quest.NoFlags;
import lostsector.quest.Quest;
import lostsector.quest.QuestModule;
import lostsector.quest.Quests;
import lostsector.quest.modules.BountyEncounter;

import java.util.List;

// Record quest "bounty": the named bounty fleets, one BountyEncounter each. Records, roles and intel keys share the
// names below.
public final class BountiesQuest extends Quest<BountiesStage, BountiesState> {

    public static final String ID = "bounty";

    public static final String ABYSS = "abyss";
    public static final String ETERNITY = "eternity";

    private static final String ICON = "umbra";
    private static final int ABYSS_PAYOUT = 600000;

    public BountiesQuest() {
        super(ID, BountiesStage.class, NoFlags.class, BountiesStage.RUNNING);
    }

    @Override
    protected BountiesState createState() {
        return new BountiesState();
    }

    @Override
    protected List<QuestModule<BountiesStage, BountiesState>> createModules() {
        return List.of(abyss(), eternity());
    }

    // Pays only when none of its ships were recovered: the player fleet holds none of them when the loot is generated,
    // which vanilla does after the recovery screen.
    private static BountyEncounter<BountiesStage, BountiesState> abyss() {
        return new BountyEncounter<BountiesStage, BountiesState>(ABYSS, ICON, FleetRole.of(FleetOrders.none()),
                BountiesFleets::abyssLocation, BountiesFleets::abyss, fleet -> !BountiesFleets.hasAbyssShips(fleet.fleet()))
                .finish(BountiesFleets::finishAbyss)
                .reward((ctx, loot, plugin) -> loot.addCommodity(Commodities.ALPHA_CORE, 1))
                .payout(ABYSS_PAYOUT, (amount, plugin) -> carriesAbyssShips(Global.getSector().getPlayerFleet()) ? 0 : amount)
                .onSighted(ctx -> HintManager.removeHintIntel())
                .revealOnRecovery(BountiesFleets.ABYSS_FLAGSHIP_HULL, BountiesFleets.ABYSS_CHASM_HULL, BountiesFleets.ABYSS_FISSURE_HULL);
    }

    private static BountyEncounter<BountiesStage, BountiesState> eternity() {
        return new BountyEncounter<BountiesStage, BountiesState>(ETERNITY, ICON, FleetRole.of(FleetOrders.none()),
                BountiesFleets::eternityLocation, BountiesFleets::eternity, fleet -> fleet.fleet().getFlagship() == null)
                .finish(BountiesFleets::finishEternity)
                .reward((ctx, loot, plugin) -> {
                    loot.addCommodity(Commodities.ALPHA_CORE, 2);
                    loot.addCommodity("nskr_electronics", 500);
                })
                .onSighted(ctx -> HintManager.removeHintIntel())
                .revealOnRecovery(BountiesFleets.ETERNITY_HULL);
    }

    public static BountiesState state() {
        return Quests.state(ID);
    }

    // Queries

    // Whether the fleet carries a ship of the Abyss bounty fleet.
    public static boolean carriesAbyssShips(CampaignFleetAPI fleet) {
        return fleet != null && BountiesFleets.hasAbyssShips(fleet);
    }

    // Where the bounty was placed; null before load, before placement or when no location was found.
    public static SectorEntityToken location(String bounty) {
        BountyEncounter.Record record = record(bounty);
        return record == null ? null : record.location();
    }

    // Whether the player has sighted the bounty fleet, which shows its intel.
    public static boolean sighted(String bounty) {
        BountyEncounter.Record record = record(bounty);
        return record != null && record.sighted();
    }

    private static BountyEncounter.Record record(String bounty) {
        BountiesState state = state();
        return state == null ? null : state.bounties().get(bounty);
    }
}
