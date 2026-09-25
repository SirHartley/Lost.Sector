package lostsector.campaign.starts.hellspawn;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import lostsector.helper.FleetHelper;
import lostsector.quest.Declarations;
import lostsector.quest.FleetOrders;
import lostsector.quest.FleetRole;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestFleet;
import lostsector.quest.QuestModule;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;
import java.util.Random;

// The Final Judgement fleet and its encounter. The module stays active in JUDGED, so the fleet is not despawned
// when the player leaves the encounter: a fleet that survives keeps hunting, as it did before the framework.
final class HellSpawnFightModule extends QuestModule<HellSpawnStage, HellSpawnState> {

    static final String ROLE_JUDGE = "judge";

    // Lines of HellSpawnJudgementInteraction, fired into the encounter with FireBest.
    static final String TRIGGER_COMMS = "nskr_hsFightComms";
    static final String TRIGGER_DISENGAGE = "nskr_hsFightDisengage";
    static final String TRIGGER_STORY_DISENGAGE = "nskr_hsFightStoryDisengage";
    static final String TRIGGER_BATTLE_DISENGAGE = "nskr_hsFightBattleDisengage";

    HellSpawnFightModule() {
        super(HellSpawnStage.FIGHT, HellSpawnStage.JUDGED);
    }

    @Override
    protected void declare(Declarations<HellSpawnStage, HellSpawnState> d) {
        d.role(ROLE_JUDGE, FleetRole.of(FleetOrders.none()));
        d.trigger(TRIGGER_COMMS);
        d.trigger(TRIGGER_DISENGAGE);
        d.trigger(TRIGGER_STORY_DISENGAGE);
        d.trigger(TRIGGER_BATTLE_DISENGAGE);
        d.action("engageJudge", HellSpawnFightModule::engage);
    }

    @Override
    protected void onStart(QuestContext<HellSpawnStage, HellSpawnState> ctx) {
        if (ctx.stage() != HellSpawnStage.FIGHT) return;
        Random random = ctx.random(HellSpawnState.RANDOM_JUDGE_FLEET);
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        CampaignFleetAPI fleet = ctx.fleets().spawn(ROLE_JUDGE, HellSpawnFleets.judgement(player, ctx.has(HellSpawnFlag.HELL), random));
        if (fleet == null) return;
        fleet.setContainingLocation(player.getContainingLocation());
        Vector2f point = MathUtils.getPointOnCircumference(player.getLocation(), player.getRadius() + fleet.getRadius(), random.nextFloat() * 360f);
        fleet.setLocation(point.x, point.y);
        FleetHelper.setAIOfficers(fleet);
        FleetHelper.update(fleet, random);
    }

    // A jump past the fight assumes it is over.
    @Override
    protected void onSkip(QuestContext<HellSpawnStage, HellSpawnState> ctx) {
        ctx.fleets().despawn(ROLE_JUDGE);
    }

    // The encounter music keeps playing until the player is back on the map, as the old HellSpawnManager check did;
    // frames run only while unpaused, so the first day in JUDGED covers that moment.
    @Override
    protected boolean wantsFrames(QuestContext<HellSpawnStage, HellSpawnState> ctx) {
        return ctx.stage() == HellSpawnStage.JUDGED && ctx.state().daysInStage() < 1f;
    }

    @Override
    protected void onFrame(QuestContext<HellSpawnStage, HellSpawnState> ctx, float amount) {
        HellSpawnThrnModule.stopMusic();
    }

    @Override
    protected void devInfo(QuestContext<HellSpawnStage, HellSpawnState> ctx, List<String> lines) {
        QuestFleet judge = ctx.fleets().first(ROLE_JUDGE);
        lines.add(judge == null ? "no judgement fleet" : "judgement fleet in " + judge.fleet().getContainingLocation().getName());
        lines.add("hell path: " + ctx.has(HellSpawnFlag.HELL));
    }

    // nskr_quest engage builds a plain FleetInteractionDialogPluginImpl; the judgement encounter is
    // HellSpawnJudgementInteraction, which blocks comms and disengaging, so the hand-off stays here.
    private static void engage(QuestContext<HellSpawnStage, HellSpawnState> ctx) {
        QuestFleet judge = ctx.fleets().first(ROLE_JUDGE);
        InteractionDialogAPI dialog = ctx.dialog();
        if (judge == null || dialog == null) {
            ctx.log("engageJudge skipped: no judgement fleet or no dialog");
            return;
        }
        HellSpawnJudgementInteraction plugin = new HellSpawnJudgementInteraction(judge.fleet(), dialog);
        dialog.setPlugin(plugin);
        plugin.showFleet();
    }
}
