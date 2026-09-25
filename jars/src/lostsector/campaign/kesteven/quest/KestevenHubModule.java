package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddShip;
import com.fs.starfarer.api.util.Misc;
import lostsector.ModPlugin;
import lostsector.campaign.enigma.DormantSpawner;
import lostsector.dialogue.rules.nskr_shipSwap;
import lostsector.helper.Ids;
import lostsector.helper.MathHelper;
import lostsector.helper.PowerLevel;
import lostsector.helper.SectorLookup;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;
import lostsector.settings.Setting;
import lostsector.world.SectorGen;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

// The conversation hub with Jack, Alice and Nicholas: the gates, display values and game actions of the rows in the
// # KESTEVEN QUESTLINE block of rules.csv (docs/quests/KESTEVEN_DIALOGUE.md). Active in every stage.
final class KestevenHubModule extends QuestModule<KestevenStage, KestevenState> {

    // The rows pay the stage payouts and take the electronics with vanilla commands and the same literal amounts.
    static final int JOB1_ARTIFACTS = 70;
    static final int STAGE1_PAYOUT = 155000;
    static final int STAGE3_PAYOUT = 205000;
    static final int STAGE4_PAYOUT = 285000;
    static final int STAGE5_PAYOUT = 565000;

    static final float JOB1_REP = 0.20f;
    static final float JOB3_REP = 0.40f;
    static final float JOB4_REP = 0.60f;
    static final float JOB5_REP = 0.80f;
    static final float JOB3_POWER = 0.65f;
    static final float JOB4_POWER = 0.80f;
    static final float JOB5_POWER = 0.95f;
    // A briefing warns when the fleet's power is below its job's gate plus this.
    private static final float DOUBT_MARGIN = 0.15f;

    private static final float EXCHANGE_POINTS_BONUS = 50000f;
    private static final String EPOCH_VARIANT = "nskr_epoch_empty";
    // CargoAPI.addHullmods adds a special item with this id and the hullmod id as its data.
    private static final String MODSPEC_ITEM = "modspec";
    private static final List<String> REWARD_MODSPECS = List.of(
            Ids.INERTIAL_SUPERCHARGER_HULLMOD_ID,
            Ids.VOLATILE_FLUX_INJECTOR_HULLMOD_ID,
            Ids.HIGH_CAPACITANCE_BANKS_HULLMOD_ID,
            Ids.CRITICAL_POINT_PROTECTION_HULLMOD_ID);

    // Alice's markers stay from job 5 on; the salvage dialogs clear the flag themselves.
    private static final KestevenStage[] JOB5_ON = {
            KestevenStage.JOB5_DISKS, KestevenStage.CACHE_KNOWN, KestevenStage.CACHE_CLEARED,
            KestevenStage.CHIP_RECOVERED, KestevenStage.COMPLETED, KestevenStage.FAILED};
    // The story skip places the job 3 and job 4 objects only in the stages before the jobs place them.
    private static final Set<KestevenStage> BEFORE_JOB3_OBJECTS = EnumSet.of(
            KestevenStage.NOT_STARTED, KestevenStage.JOB1_ACTIVE, KestevenStage.JOB1_DONE,
            KestevenStage.JOB3_OFFERED, KestevenStage.JOB3_BRIEFING);
    private static final Set<KestevenStage> BEFORE_JOB4_OBJECTS = EnumSet.of(
            KestevenStage.NOT_STARTED, KestevenStage.JOB1_ACTIVE, KestevenStage.JOB1_DONE,
            KestevenStage.JOB3_OFFERED, KestevenStage.JOB3_BRIEFING, KestevenStage.JOB3_ACTIVE,
            KestevenStage.JOB3_TARGET_KNOWN, KestevenStage.JOB3_DONE, KestevenStage.JOB4_WAITING);

    KestevenHubModule() {
        super();
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.check("hubOpen", KestevenHubModule::hubOpen);
        d.check("storySkipUnlocked", ctx -> Setting.STORY_SKIP_UNLOCKED.getBoolean());

        d.check("job1Standing", ctx -> standing() >= JOB1_REP);
        d.check("job3Standing", ctx -> standing() >= JOB3_REP);
        d.check("job4Standing", ctx -> standing() >= JOB4_REP);
        d.check("job5Standing", ctx -> standing() >= JOB5_REP);
        d.check("job3Fleet", ctx -> fleetPower() > JOB3_POWER);
        // A story point bypass of the job 4 strength gate stays in force on later visits.
        d.check("job4Fleet", ctx -> ctx.has(KestevenFlag.JOB4_REQUIREMENT_SKIPPED) || fleetPower() > JOB4_POWER);
        d.check("job5Fleet", ctx -> fleetPower() > JOB5_POWER);
        d.check("fleetStretched", KestevenHubModule::fleetStretched);

        d.check("job1Cargo", ctx -> hasJob1Cargo());
        d.check("job1SensorReady", ctx -> ctx.has(KestevenFlag.JOB1_SENSOR_DATA) && !ctx.has(KestevenFlag.JOB1_DATA_DELIVERED));
        d.check("job1CargoReady", ctx -> hasJob1Cargo() && !ctx.has(KestevenFlag.JOB1_ELECTRONICS_DELIVERED));
        d.check("job1TipKnown", ctx -> ctx.state().job1TipSystem != null);
        d.check("job4TargetKnown", ctx -> ctx.state().job4EnemyTarget != null);
        d.check("nicholasTipGiven", ctx -> ctx.state().nicholasDialogStage >= 1);
        d.check("outpostExists", ctx -> QuestHelper.outpostExists());
        d.check("noSatellite", ctx -> ctx.state().satellitesRecovered == 0);
        d.check("oneSatellite", ctx -> ctx.state().satellitesRecovered == 1);
        d.check("twoSatellites", ctx -> ctx.state().satellitesRecovered >= 2);
        d.check("disksOverTwo", ctx -> ctx.state().disksRecovered > 2);
        d.check("allDisks", ctx -> ctx.state().disksRecovered >= 5);
        d.check("frostVisited", ctx -> SectorLookup.getFrost().isEnteredByPlayer());

        // Targets are picked on first use, where the old dialog first read them, before the rows that show them.
        d.action("pickJob1Tip", ctx -> QuestHelper.getJob1Tip());
        d.action("pickJob3Start", ctx -> QuestHelper.getJob3Start());
        d.action("pickJob3Target", ctx -> QuestHelper.getJob3Target());
        d.action("pickJob4FriendlyTarget", ctx -> QuestHelper.getJob4FriendlyTarget());
        d.action("pickJob5FrostTip", ctx -> QuestHelper.getJob5FrostTip());
        d.action("recordNicholasTip", ctx -> ctx.state().nicholasDialogStage = 1);
        d.action("markJob3Satellite", ctx -> markSatellite(ctx, ctx.state().job3Target));
        d.action("markJob4Satellite", ctx -> markSatellite(ctx, ctx.state().job4EnemyTarget));
        d.action("grantModspec", KestevenHubModule::grantModspec);
        d.action("grantExchangePoints", ctx -> nskr_shipSwap.addPoints(EXCHANGE_POINTS_BONUS));
        d.action("grantEpoch", KestevenHubModule::grantEpoch);
        d.action("raiseJackImportance", KestevenHubModule::raiseJackImportance);
        d.action("placeJob3Leftovers", KestevenHubModule::placeJob3Leftovers);
        d.action("storySkip", KestevenHubModule::storySkip);

        d.token("playerFullName", ctx -> Global.getSector().getPlayerPerson().getName().getFullName());
        d.token("job1Payout", ctx -> Misc.getDGSCredits(STAGE1_PAYOUT));
        d.token("job1TipSystem", ctx -> ctx.state().job1TipSystem == null ? "" : ctx.state().job1TipSystem.getName());
        d.token("job3Start", ctx -> ctx.state().job3Start == null ? "" : ctx.state().job3Start.getName());
        d.token("job3Market", KestevenHubModule::job3Market);
        d.token("job3Payout", ctx -> Misc.getDGSCredits(STAGE3_PAYOUT));
        d.token("job3TargetSystem", ctx -> systemName(ctx.state().job3Target));
        d.token("job4Constellation", KestevenHubModule::job4Constellation);
        d.token("job4Payout", ctx -> Misc.getDGSCredits(STAGE4_PAYOUT));
        d.token("job4TargetSystem", ctx -> systemName(ctx.state().job4EnemyTarget));
        d.token("outpostName", ctx -> SectorLookup.getOutpost() == null ? "" : SectorLookup.getOutpost().getName());
        d.token("frostName", ctx -> SectorLookup.getFrost().getName());
        d.token("frostTipConstellation", KestevenHubModule::frostTipConstellation);
        d.token("frostTipDistance", KestevenHubModule::frostTipDistance);
    }

    static float fleetPower() {
        return Global.getSettings().isDevMode() ? 2f : PowerLevel.get(0.2f, 0f, 2f);
    }

    // Checks

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
        return Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity("nskr_electronics") >= JOB1_ARTIFACTS;
    }

    // Only the job 3 and job 4 briefings warn.
    private static boolean fleetStretched(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.stage() == KestevenStage.JOB3_BRIEFING) return fleetPower() < JOB3_POWER + DOUBT_MARGIN;
        if (ctx.stage() == KestevenStage.JOB4_WAITING) return fleetPower() < JOB4_POWER + DOUBT_MARGIN;
        return false;
    }

    // Tokens

    private static String job3Market(QuestContext<KestevenStage, KestevenState> ctx) {
        SectorEntityToken start = ctx.state().job3Start;
        return start == null || start.getMarket() == null ? "" : start.getMarket().getName();
    }

    private static String systemName(SectorEntityToken location) {
        return location == null || location.getStarSystem() == null ? "" : location.getStarSystem().getName();
    }

    private static String job4Constellation(QuestContext<KestevenStage, KestevenState> ctx) {
        SectorEntityToken target = ctx.state().job4FriendlyTarget;
        return target == null || target.getConstellation() == null ? "" : target.getConstellation().getName();
    }

    private static String frostTipConstellation(QuestContext<KestevenStage, KestevenState> ctx) {
        StarSystemAPI tip = ctx.state().job5FrostTipSystem;
        if (tip == null || tip.getConstellation() == null) return "";
        return QuestHelper.parseConstellation(tip.getConstellation().getNameWithType());
    }

    // The old dialog printed the float with string concatenation after rounding to two decimals.
    private static String frostTipDistance(QuestContext<KestevenStage, KestevenState> ctx) {
        StarSystemAPI tip = ctx.state().job5FrostTipSystem;
        StarSystemAPI frost = SectorLookup.getFrost();
        if (tip == null || tip.getConstellation() == null || frost == null) return "";
        float distLY = Misc.getDistanceLY(tip.getConstellation().getLocation(), frost.getStar().getLocationInHyperspace()) * 1.5f;
        distLY *= 100f;
        distLY = Math.round(distLY);
        distLY /= 100f;
        return String.valueOf(distLY);
    }

    // Actions

    private static void markSatellite(QuestContext<KestevenStage, KestevenState> ctx, SectorEntityToken location) {
        if (location == null || location.getStarSystem() == null) return;
        SectorEntityToken satellite = QuestHelper.getArtifact(location.getStarSystem());
        if (satellite != null) ctx.mark(satellite, JOB5_ON);
    }

    // A modspec the player does not know yet when there is one.
    private static void grantModspec(QuestContext<KestevenStage, KestevenState> ctx) {
        Random random = ctx.random(KestevenState.RANDOM_QUEST);
        List<String> unknown = new ArrayList<>(REWARD_MODSPECS);
        unknown.removeAll(Global.getSector().getPlayerFaction().getKnownHullMods());
        List<String> pool = unknown.isEmpty() ? REWARD_MODSPECS : unknown;
        String hullmod = pool.get(MathHelper.getSeededRandomNumberInRange(0, pool.size() - 1, random));
        ctx.rewards().item(new SpecialItemData(MODSPEC_ITEM, hullmod), 1);
    }

    private static void grantEpoch(QuestContext<KestevenStage, KestevenState> ctx) {
        FleetMemberAPI member = Global.getSector().getPlayerFleet().getFleetData().addFleetMember(EPOCH_VARIANT);
        if (member != null && ctx.textPanel() != null) AddShip.addShipGainText(member, ctx.textPanel());
    }

    private static void raiseJackImportance(QuestContext<KestevenStage, KestevenState> ctx) {
        PersonAPI jack = KestevenPeople.getJack();
        if (jack != null) jack.setImportance(PersonImportance.HIGH);
    }

    // The derelicts, satellite #3 and the dormant guard that job 3 would have left, for job 5.
    private static void placeJob3Leftovers(QuestContext<KestevenStage, KestevenState> ctx) {
        SectorEntityToken target = QuestHelper.getJob3Target();
        QuestHelper.spawnEnvironmentalStorytelling();
        QuestHelper.spawnArtifact(target, 3);
        DormantSpawner.addDormant(target, "enigma", 45f, 50f, 0f, 1f, 1f, 1f, 1, 1);
    }

    // The player's story skip, unchanged from the old dialog until it moves onto QuestManager.jump.
    private static void storySkip(QuestContext<KestevenStage, KestevenState> ctx) {
        KestevenStage stage = ctx.stage();
        if (BEFORE_JOB3_OBJECTS.contains(stage)) {
            QuestHelper.spawnArtifact(QuestHelper.getJob3Target(), 3);
            DormantSpawner.addDormant(QuestHelper.getJob3Target(), "enigma", 45f, 50f, 0f, 1f, 1f, 1f, 1, 1);
        }
        if (BEFORE_JOB4_OBJECTS.contains(stage)) {
            KestevenJob4Module.spawnStrikeGroup(ctx);
            KestevenJob4Module.placeWrecks(ctx);
        }
        ctx.set(KestevenFlag.SATELLITE4_RECOVERED);
        ctx.set(KestevenFlag.SATELLITE3_RECOVERED);
        ctx.state().satellitesRecovered = 2;
        ctx.set(KestevenFlag.FROST_FOUND);
        ctx.set(KestevenFlag.GLACIER_DISK_RECOVERED);
        ctx.set(KestevenFlag.JOB5_ALICE_TIP);
        ctx.set(KestevenFlag.JOB5_ALICE_TIP2);
        ctx.set(KestevenFlag.JOB5_JACK_TIP);
        ctx.set(KestevenFlag.ELIZA_FOUND);
        ctx.set(KestevenFlag.ELIZA_DIALOG_FINISHED);
        ctx.set(KestevenFlag.ELIZA_HELPED);
        if (QuestHelper.getElizaLoc() == null) {
            QuestHelper.setElizaLoc();
            PersonAPI eliza = SectorGen.genEliza();
            QuestHelper.getElizaLoc().getMarket().getCommDirectory().addPerson(eliza, 1);
            QuestHelper.getElizaLoc().getMarket().addPerson(eliza);
            ctx.log("Eliza loc " + QuestHelper.getElizaLoc().getMarket().getName());
        }

        ctx.set(KestevenFlag.CACHE_FOUND);
        ctx.advance(KestevenStage.CACHE_KNOWN);

        // Ineligible for the hard mode completion.
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (data.containsKey(ModPlugin.STARFARER_MODE_FROM_START_KEY)) {
            data.put(ModPlugin.STARFARER_MODE_FROM_START_KEY, false);
        }
        ctx.set(KestevenFlag.STORY_SKIPPED);
    }
}
