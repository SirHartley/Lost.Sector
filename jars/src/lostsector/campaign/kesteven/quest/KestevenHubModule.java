package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import lostsector.dialogue.rules.nskr_kestevenQuest;
import lostsector.helper.Ids;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;
import lostsector.settings.Setting;

// The conversation hub with Jack, Alice and Nicholas: the gates and display values of the rows in the
// # KESTEVEN QUESTLINE block of rules.csv (docs/quests/KESTEVEN_DIALOGUE.md). Active in every stage.
// TODO T17: move the job gates and fleet power here from nskr_kestevenQuest when the briefings leave it.
final class KestevenHubModule extends QuestModule<KestevenStage, KestevenState> {

    KestevenHubModule() {
        super();
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.check("hubOpen", KestevenHubModule::hubOpen);
        d.check("storySkipUnlocked", ctx -> Setting.STORY_SKIP_UNLOCKED.getBoolean());

        d.check("job1Standing", ctx -> standing() >= nskr_kestevenQuest.JOB1_REP);
        d.check("job3Standing", ctx -> standing() >= nskr_kestevenQuest.JOB3_REP);
        d.check("job4Standing", ctx -> standing() >= nskr_kestevenQuest.JOB4_REP);
        d.check("job5Standing", ctx -> standing() >= nskr_kestevenQuest.JOB5_REP);
        d.check("job3Fleet", ctx -> nskr_kestevenQuest.fleetPower() > nskr_kestevenQuest.JOB3_POWER);
        // A story point bypass of the job 4 strength gate stays in force on later visits.
        d.check("job4Fleet", ctx -> ctx.has(KestevenFlag.JOB4_REQUIREMENT_SKIPPED)
                || nskr_kestevenQuest.fleetPower() > nskr_kestevenQuest.JOB4_POWER);
        d.check("job5Fleet", ctx -> nskr_kestevenQuest.fleetPower() > nskr_kestevenQuest.JOB5_POWER);

        d.check("job1Cargo", ctx -> hasJob1Cargo());
        d.check("job1SensorReady", ctx -> ctx.has(KestevenFlag.JOB1_SENSOR_DATA) && !ctx.has(KestevenFlag.JOB1_DATA_DELIVERED));
        d.check("job1CargoReady", ctx -> hasJob1Cargo() && !ctx.has(KestevenFlag.JOB1_ELECTRONICS_DELIVERED));
        d.check("job1TipKnown", ctx -> ctx.state().job1TipSystem != null);
        d.check("job4TargetKnown", ctx -> ctx.state().job4EnemyTarget != null);
        d.check("nicholasTipGiven", ctx -> ctx.state().nicholasDialogStage >= 1);
        d.check("twoSatellites", ctx -> ctx.state().satellitesRecovered >= 2);
        d.check("allDisks", ctx -> ctx.state().disksRecovered >= 5);

        // Picks the job 1 tip system on first use and places a dormant Enigma fleet there; rows run it where the
        // old dialog first asked for the tip, before the rows that test job1TipKnown.
        d.action("pickJob1Tip", ctx -> QuestHelper.getJob1Tip());

        d.token("playerFullName", ctx -> Global.getSector().getPlayerPerson().getName().getFullName());
        d.token("job1TipSystem", ctx -> ctx.state().job1TipSystem == null ? "" : ctx.state().job1TipSystem.getName());
        d.token("job3Start", ctx -> ctx.state().job3Start == null ? "" : ctx.state().job3Start.getName());
    }

    // The target's market belongs to Kesteven and the player's Kesteven standing is above -50.
    private static boolean hubOpen(QuestContext<KestevenStage, KestevenState> ctx) {
        SectorEntityToken target = ctx.target();
        MarketAPI market = target == null ? null : target.getMarket();
        if (market == null) return false;
        if (standing() <= -0.5f) return false;
        return market.getFaction().getId().equals(Ids.KESTEVEN_FACTION_ID);
    }

    private static float standing() {
        return Global.getSector().getPlayerFaction().getRelationship(Ids.KESTEVEN_FACTION_ID);
    }

    private static boolean hasJob1Cargo() {
        return Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity("nskr_electronics") >= nskr_kestevenQuest.JOB1_ARTIFACTS;
    }
}
