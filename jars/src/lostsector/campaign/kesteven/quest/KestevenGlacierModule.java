package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import lostsector.helper.MathHelper;
import lostsector.helper.SectorLookup;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestDialogs;
import lostsector.quest.QuestModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Disk #5 at the comms facility on Glacier: the marker and dialog claim from Alice's second Frost tip, the barrage's
// fleet damage and the recovery. The raid itself is the # KESTEVEN QUESTLINE: GLACIER block of rules.csv.
final class KestevenGlacierModule extends QuestModule<KestevenStage, KestevenState> {

    static final String TRIGGER = "nskr_kqGlacier";
    // Fired once per damaged ship, while glacierHit holds that ship.
    static final String TRIGGER_HIT = "nskr_kqGlacierHit";

    private static final String GLACIER_ID = "nskr_glacier";
    private static final int DAMAGED_SHIPS = 4;
    // Each damaged ship loses a random hull fraction and a random CR fraction between half this and this.
    private static final float DAMAGE_FRACTION = 0.5f;
    private static final float MIN_REPAIRED = 0.25f;

    // The old route opened the dialog at legacy stage 16 or later, which includes failure (99).
    private static final KestevenStage[] STAGES = {
            KestevenStage.JOB5_DISKS, KestevenStage.CACHE_KNOWN, KestevenStage.CACHE_CLEARED,
            KestevenStage.CHIP_RECOVERED, KestevenStage.COMPLETED, KestevenStage.FAILED};

    KestevenGlacierModule() {
        super(STAGES);
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.trigger(TRIGGER);
        d.trigger(TRIGGER_HIT);

        // Alice's Frost tip row sets JOB5_ALICE_TIP2 and runs this at JOB5_DISKS.
        d.action("markGlacier", KestevenGlacierModule::markGlacier);
        d.action("damageFleet", KestevenGlacierModule::damageFleet);
        d.action("recoverGlacierDisk", KestevenGlacierModule::recover);

        d.token("glacierHitShip", ctx -> ctx.state().glacierHit == null ? "" : ctx.state().glacierHit.getShipName());
        d.token("glacierHitHull", ctx -> ctx.state().glacierHit == null ? "" : ctx.state().glacierHit.getHullSpec().getHullName());
    }

    // A jump past JOB5_DISKS has found Frost and recovered the facility's disk.
    @Override
    protected void onSkip(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.stage() != KestevenStage.JOB5_DISKS) return;
        ctx.set(KestevenFlag.FROST_FOUND);
        if (!ctx.has(KestevenFlag.GLACIER_DISK_RECOVERED)) recover(ctx);
    }

    @Override
    protected void devInfo(QuestContext<KestevenStage, KestevenState> ctx, List<String> lines) {
        SectorEntityToken glacier = glacier();
        lines.add("glacier claimed: " + (glacier != null && TRIGGER.equals(QuestDialogs.claimedTrigger(glacier)))
                + ", disk recovered: " + ctx.has(KestevenFlag.GLACIER_DISK_RECOVERED) + ", disks: " + ctx.state().disksRecovered);
    }

    private static void markGlacier(QuestContext<KestevenStage, KestevenState> ctx) {
        SectorEntityToken glacier = glacier();
        ctx.mark(glacier, STAGES);
        ctx.claimDialog(glacier, TRIGGER, STAGES);
    }

    // Up to four ships that are not fighters and are at least a quarter repaired. Each hit fires TRIGGER_HIT for its line.
    private static void damageFleet(QuestContext<KestevenStage, KestevenState> ctx) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        List<FleetMemberAPI> valid = new ArrayList<>();
        for (FleetMemberAPI member : fleet.getMembersWithFightersCopy()) {
            if (member.isFighterWing()) continue;
            if (member.getRepairTracker().computeRepairednessFraction() < MIN_REPAIRED) continue;
            if (member.getHullSpec() == null || member.getStatus() == null) continue;
            valid.add(member);
        }
        Random random = ctx.random(KestevenState.RANDOM_GLACIER);
        for (int count = Math.min(DAMAGED_SHIPS, valid.size()); count > 0; count--) {
            FleetMemberAPI ship = valid.remove(MathHelper.getSeededRandomNumberInRange(0, valid.size() - 1, random));
            ship.getStatus().applyHullFractionDamage(MathHelper.getSeededRandomNumberInRange(DAMAGE_FRACTION / 2f, DAMAGE_FRACTION, random));
            ship.getRepairTracker().setCR(ship.getRepairTracker().getCR() - MathHelper.getSeededRandomNumberInRange(DAMAGE_FRACTION / 2f, DAMAGE_FRACTION, random));
            if (ctx.dialog() == null) continue;
            ctx.state().glacierHit = ship;
            FireBest.fire(null, ctx.dialog(), ctx.memoryMap(), TRIGGER_HIT);
        }
        ctx.state().glacierHit = null;
    }

    private static void recover(QuestContext<KestevenStage, KestevenState> ctx) {
        ctx.set(KestevenFlag.GLACIER_DISK_RECOVERED);
        ctx.state().disksRecovered++;
        KestevenSatelliteModule.checkAllDisks(ctx);
        SectorEntityToken glacier = glacier();
        ctx.unmark(glacier);
        ctx.releaseDialog(glacier);
    }

    private static SectorEntityToken glacier() {
        for (SectorEntityToken entity : SectorLookup.getFrost().getAllEntities()) {
            if (GLACIER_ID.equals(entity.getId())) return entity;
        }
        return null;
    }
}
