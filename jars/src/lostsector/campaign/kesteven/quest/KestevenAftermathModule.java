package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import lostsector.campaign.kesteven.ExileManager;
import lostsector.helper.FleetHelper;
import lostsector.helper.Ids;
import lostsector.helper.SectorLookup;
import lostsector.quest.Declarations;
import lostsector.quest.FleetOrders;
import lostsector.quest.FleetRole;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestFleet;
import lostsector.quest.QuestModule;

import java.util.List;

// Jack's revenge after the Eliza or an alternative ending, and the failure when Kesteven has lost both mission markets.
// Jack's comm rows are the # KESTEVEN QUESTLINE: AFTERMATH rows. Active in every stage, as the old checks ran whatever
// the stage.
final class KestevenAftermathModule extends QuestModule<KestevenStage, KestevenState> {

    static final String ROLE_JACK_REVENGE = "jackRevenge";

    static final float REVENGE_CHANCE = 0.01f;

    KestevenAftermathModule() {
        super();
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        // The old revenge fleet logic despawned an emptied fleet once out of the player's sight, as Eliza's revenge role does.
        d.role(ROLE_JACK_REVENGE, FleetRole.of(FleetOrders.intercept(FleetHelper.InterceptBehaviour.AROUND)
                .withdrawWhen(FleetHelper::isEmptied)));
    }

    @Override
    protected void onDay(QuestContext<KestevenStage, KestevenState> ctx) {
        checkMarketsLost(ctx);
        // The old roll drew every day in every stage before testing the stage; drawing first keeps the seeded sequence.
        if (ctx.random(KestevenState.RANDOM_REVENGE).nextFloat() < REVENGE_CHANCE && ctx.stage() == KestevenStage.COMPLETED
                && !ctx.state().jackRevengeSpawned) {
            if (ctx.has(KestevenFlag.ELIZA_ENDING_DONE) || ctx.has(KestevenFlag.ALT_ENDING_DONE)) {
                spawnJack(ctx);
                ctx.state().jackRevengeSpawned = true;
                ctx.log("Revengeanced Jack");
            }
        }
    }

    // No vanilla callback reports a market changing hands: Market.setFactionId only stores the id (0.98a-RC8
    // sources-obf/campaign.econ.java 1161), and captures come from Nexerelin or other mods. onDecivilized alone would
    // miss them, so the check runs daily, the cadence of ExileManager, which moves Kesteven between the same two markets.
    // The old check ran every frame, paused included.
    private static void checkMarketsLost(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ExileManager.canExile() || SectorLookup.asteriaExists() || ctx.has(KestevenFlag.ENDED)) return;
        if (ctx.stage() != KestevenStage.FAILED) ctx.advance(KestevenStage.FAILED);
        ctx.set(KestevenFlag.ENDED);
        ctx.log("Kesteven holds neither Asteria nor the Outpost, questline failed");
    }

    // Jack leaves his market and the important people; ExileManager reads JACK_GONE so an exile does not bring him back.
    private static void spawnJack(QuestContext<KestevenStage, KestevenState> ctx) {
        PersonAPI jack = KestevenPeople.getJack();
        SectorEntityToken loc = SectorLookup.asteriaOrOutpost().getPrimaryEntity();
        CampaignFleetAPI fleet = ctx.fleets().spawn(ROLE_JACK_REVENGE, KestevenFleets.jackRevenge(loc, jack, ctx.random(KestevenState.RANDOM_QUEST)));
        loc.getMarket().getCommDirectory().removePerson(jack);
        loc.getMarket().removePerson(jack);
        Global.getSector().getImportantPeople().removePerson(Ids.JACK_PERSON_ID);
        ctx.set(KestevenFlag.JACK_GONE);
        if (fleet != null) {
            ctx.log("Jack FLEET, loc " + fleet.getStarSystem().getName() + " size " + fleet.getFleetPoints() + " commander "
                    + fleet.getCommander().getName().getFullName() + " flagship " + fleet.getFlagship().getHullSpec().getBaseHullId());
        }
    }

    @Override
    protected void devInfo(QuestContext<KestevenStage, KestevenState> ctx, List<String> lines) {
        lines.add("Jack's revenge spawned " + ctx.state().jackRevengeSpawned);
        for (QuestFleet fleet : ctx.fleets().get(ROLE_JACK_REVENGE)) {
            lines.add(ROLE_JACK_REVENGE + ": age " + (int) fleet.info().age + ", points " + fleet.fleet().getFleetPoints());
        }
    }
}
