package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.IntelSortTier;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.world.MoteParticleScript;
import lostsector.helper.FleetHelper;
import lostsector.helper.Ids;
import lostsector.helper.MathHelper;
import lostsector.helper.SectorLookup;
import lostsector.quest.Declarations;
import lostsector.quest.FleetOrders;
import lostsector.quest.FleetRole;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;
import lostsector.world.systems.cache.Cache;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;
import java.util.Random;

// The Cache, the Unknown Site: finding it, its intel entry, the arrival sequence (inner voice, pings, music, guardian,
// motes), the guardian's orders and the command core. Active in every stage, because the guardian appears and the
// core can be salvaged whatever the questline's progress; the rows are the # KESTEVEN QUESTLINE: CACHE block.
final class KestevenCacheModule extends QuestModule<KestevenStage, KestevenState> {

    static final String TRIGGER_CORE = "nskr_kqCacheCore";
    static final String TRIGGER_DOUBT = "nskr_kqCacheDoubt";
    static final String INTEL = "cache";
    static final String ROLE_GUARDIAN = "cacheGuardian";
    static final String PERSON_CHIEF = "cacheChief";
    static final String PERSON_SENSORS = "cacheSensors";

    // Frame seconds in the Unknown Site, counted twice as fast during fast advance, as QuestStageManager counted them.
    private static final float DOUBT_SECONDS = 35f;
    private static final float SLOW_PINGS_SECONDS = 45f;
    private static final float FAST_PINGS_SECONDS = 75f;
    private static final float GUARDIAN_SECONDS = 90f;
    private static final float SLOW_PING_INTERVAL = 6f;
    private static final float FAST_PING_INTERVAL = 3f;
    // A ping farther than this from the player is moved onto the circle of this radius around the player, in the
    // guardian's direction give or take PING_SPREAD degrees.
    private static final float PING_DISTANCE = 1000f;
    private static final float PING_SPREAD = 20f;
    // Chance per unpaused frame.
    private static final float MOTE_CHANCE = 0.004f;
    private static final String MUSIC = "nskr_cache_theme";

    private static final int ELECTRONICS_MIN = 50;
    private static final int ELECTRONICS_MAX = 100;

    private static final String RANDOM_PINGS = "cachePings";
    private static final String RANDOM_MOTES = "cacheMotes";

    private static final KestevenStage[] ALL_STAGES = KestevenStage.values();
    // The return marker ends when an ending completes the questline, where the endings unset the old plain flag; after
    // a failure it stays, as that flag did.
    private static final KestevenStage[] RETURN_STAGES = {KestevenStage.CHIP_RECOVERED, KestevenStage.FAILED};

    // Seconds since the last ping. Not saved, so it restarts at 0 on every load, as QuestStageManager's field did.
    private float pingSeconds;

    KestevenCacheModule() {
        super();
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.trigger(TRIGGER_CORE);
        d.trigger(TRIGGER_DOUBT);

        d.intel(INTEL, "tutorial", Tags.INTEL_FLEET_LOG, Tags.INTEL_EXPLORATION)
                .tier(IntelSortTier.TIER_2).majorPosting().descriptionBullets().deletableWhen("cacheIntelDeletable");

        // Without prototypes, or emptied, the guardian gets no more orders and despawns once out of the player's sight;
        // prototypes are lost only in battle, after which the encounter's config turns the Cache into its salvage.
        d.role(ROLE_GUARDIAN, FleetRole.of(FleetOrders.defendSystem(Cache.GUARDIAN_ASSIGNMENT_TEXT)
                        .withdrawWhen(info -> !Cache.hasPrototypes(info.fleet) || FleetHelper.isEmptied(info)))
                .config(new Cache.CacheGuardInteractionConfig()));

        d.person(PERSON_CHIEF);
        d.person(PERSON_SENSORS);

        // Legacy stage 19 or later, or failure: the old entry's delete button.
        d.check("cacheIntelDeletable", ctx -> ctx.stage().toLegacy() >= 19 || ctx.has(KestevenFlag.ENDED));
        // Legacy stage 16 or later while the questline runs: the core offers the salvage.
        d.check("cacheQuestTarget", KestevenCacheModule::questTarget);

        d.action("endCacheDoubt", ctx -> {
            ctx.people().release(PERSON_CHIEF);
            ctx.people().release(PERSON_SENSORS);
        });
        d.action("salvageCacheCore", KestevenCacheModule::salvage);
    }

    // Also covers an Unknown Site entered before stage 15 or 16, or before the questline failed. A jump checks only in
    // its target stage, after onSkip past JOB5_DISKS set CACHE_FOUND, so the entry shows once the jump arrives.
    @Override
    protected void onStage(QuestContext<KestevenStage, KestevenState> ctx, KestevenStage from) {
        if (ctx.isJump() && ctx.stage() != ctx.jumpTarget()) return;
        StarSystemAPI cache = cacheSystem();
        checkFound(ctx, cache != null && cache.isEnteredByPlayer());
    }

    // The system is not marked entered yet when the location changes (README "Events"), so arriving counts itself.
    @Override
    protected void onLocationChanged(QuestContext<KestevenStage, KestevenState> ctx, LocationAPI prev, LocationAPI curr) {
        if (curr != null && curr == cacheSystem()) checkFound(ctx, true);
    }

    // Only while the player is in the Unknown Site.
    @Override
    protected boolean wantsFrames(QuestContext<KestevenStage, KestevenState> ctx) {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        return player != null && player.getStarSystem() != null && player.getStarSystem() == cacheSystem();
    }

    @Override
    protected void onFrame(QuestContext<KestevenStage, KestevenState> ctx, float amount) {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        KestevenState s = ctx.state();
        if (!s.cacheGuardianSpawned) {
            float seconds = Global.getSector().isInFastAdvance() ? 2f * amount : amount;
            s.cacheSeconds += seconds;
            pingSeconds += seconds;
            if (s.cacheSeconds > DOUBT_SECONDS) {
                if (!s.cacheGuardianSpotPicked) {
                    s.cacheGuardianSpot = pickGuardianSpot(ctx);
                    s.cacheGuardianSpotPicked = true;
                }
                if (questTarget(ctx) && !s.cacheDoubtShown) openDoubt(ctx, player);
            }
            if (s.cacheSeconds > SLOW_PINGS_SECONDS && s.cacheSeconds < FAST_PINGS_SECONDS && pingSeconds > SLOW_PING_INTERVAL) {
                ping(ctx, player);
            }
            if (s.cacheSeconds >= FAST_PINGS_SECONDS && pingSeconds > FAST_PING_INTERVAL) {
                ping(ctx, player);
            }
            if (s.cacheSeconds > GUARDIAN_SECONDS) spawnGuardian(ctx);
        }
        if (ctx.random(RANDOM_MOTES).nextFloat() < MOTE_CHANCE) MoteParticleScript.spawnMote(player);
    }

    // A jump past JOB5_DISKS assumes the Cache was found, which also shows its intel once the jump arrives; a jump past
    // CACHE_CLEARED, that the core was seen and salvaged. The world is left as it is: an unfought guardian still
    // appears when the player arrives.
    @Override
    protected void onSkip(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.stage() == KestevenStage.JOB5_DISKS) ctx.set(KestevenFlag.CACHE_FOUND);
        if (ctx.stage() == KestevenStage.CACHE_CLEARED) {
            ctx.set(KestevenFlag.CORE_SEEN);
            ctx.set(KestevenFlag.CHIP_SALVAGED);
        }
    }

    @Override
    protected void devInfo(QuestContext<KestevenStage, KestevenState> ctx, List<String> lines) {
        KestevenState s = ctx.state();
        lines.add("seconds in the site: " + s.cacheSeconds + ", guardian spot: " + (s.cacheGuardianSpot == null ? "none" : s.cacheGuardianSpot.getLocation())
                + ", doubt shown: " + s.cacheDoubtShown + ", guardian spawned: " + s.cacheGuardianSpawned);
        lines.add("guardian fleets: " + ctx.fleets().get(ROLE_GUARDIAN).size() + ", intel shown: " + ctx.intel().isShown(INTEL));
    }

    // KestevenQuest.showCacheCore: the guardian's encounter hands its window to the core's rules dialog. The claim
    // serves every later visit.
    static void revealCore(QuestContext<KestevenStage, KestevenState> ctx, SectorEntityToken core) {
        ctx.claimDialog(core, TRIGGER_CORE, ALL_STAGES);
        if (ctx.stage().toLegacy() >= 16) ctx.mark(core, ALL_STAGES);
        ctx.continueDialog(core, TRIGGER_CORE);
    }

    // Stage 15 and 16 and a failed questline record the find; stage 16 then moves on to the Cache.
    private static void checkFound(QuestContext<KestevenStage, KestevenState> ctx, boolean entered) {
        KestevenStage stage = ctx.stage();
        boolean ended = ctx.has(KestevenFlag.ENDED) || stage == KestevenStage.FAILED;
        if (entered && !ctx.has(KestevenFlag.CACHE_FOUND)
                && (stage == KestevenStage.JOB5_MEETING || stage == KestevenStage.JOB5_DISKS || ended)) {
            ctx.set(KestevenFlag.CACHE_FOUND);
        }
        if (!ctx.has(KestevenFlag.CACHE_FOUND)) return;
        if (!ctx.state().cacheIntelAdded) {
            StarSystemAPI cache = cacheSystem();
            ctx.intel().show(INTEL);
            if (cache != null) ctx.intel().setMapLocation(INTEL, cache.getCenter());
            ctx.state().cacheIntelAdded = true;
        }
        // During a jump this applies only when JOB5_DISKS is the target; on the way the jump drops it (README "A stage jump").
        if (stage == KestevenStage.JOB5_DISKS) ctx.advance(KestevenStage.JOB5_DISKS, KestevenStage.CACHE_KNOWN);
    }

    private static boolean questTarget(QuestContext<KestevenStage, KestevenState> ctx) {
        return ctx.stage().toLegacy() >= 16 && !ctx.has(KestevenFlag.ENDED);
    }

    private static SectorEntityToken pickGuardianSpot(QuestContext<KestevenStage, KestevenState> ctx) {
        Random random = ctx.random(KestevenState.RANDOM_QUEST);
        return cacheSystem().createToken(new Vector2f(MathHelper.getSeededRandomNumberInRange(-3000f, 3000f, random),
                MathHelper.getSeededRandomNumberInRange(-3000f, 3000f, random)));
    }

    // The inner voice opens only when no dialog or menu is showing, and is tried again on later frames in the site.
    // The scene is a rules dialog on the player fleet with location music kept, as the old null-target dialog left it.
    private static void openDoubt(QuestContext<KestevenStage, KestevenState> ctx, CampaignFleetAPI player) {
        CampaignUIAPI ui = Global.getSector().getCampaignUI();
        if (ui.isShowingDialog() || ui.isShowingMenu()) return;
        ctx.people().create(PERSON_CHIEF, Factions.PLAYER, chief -> chief.setPostId(Ranks.SPACE_CHIEF));
        ctx.people().create(PERSON_SENSORS, Factions.PLAYER, officer -> officer.setPostId(Ranks.POST_OFFICER));
        player.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY, true);
        ctx.open(player, TRIGGER_DOUBT);
        ctx.state().cacheDoubtShown = true;
    }

    private void ping(QuestContext<KestevenStage, KestevenState> ctx, CampaignFleetAPI player) {
        SectorEntityToken loc = ctx.state().cacheGuardianSpot;
        if (MathUtils.getDistance(player.getLocation(), loc.getLocation()) > PING_DISTANCE) {
            float angle = VectorUtils.getAngle(player.getLocation(), loc.getLocation());
            float spread = MathHelper.getSeededRandomNumberInRange(-PING_SPREAD, PING_SPREAD, ctx.random(RANDOM_PINGS));
            loc = player.getContainingLocation().createToken(MathUtils.getPointOnCircumference(player.getLocation(), PING_DISTANCE, angle + spread));
        }
        Global.getSector().addPing(loc, Pings.SENSOR_BURST);
        pingSeconds = 0f;
    }

    private static void spawnGuardian(QuestContext<KestevenStage, KestevenState> ctx) {
        KestevenState s = ctx.state();
        cacheSystem().getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, MUSIC);
        Random random = ctx.random(KestevenState.RANDOM_QUEST);
        CampaignFleetAPI fleet = ctx.fleets().spawn(ROLE_GUARDIAN, Cache.guardianFleet(s.cacheGuardianSpot, random));
        if (fleet != null) {
            Cache.finishGuardianFleet(fleet, s.cacheGuardianSpot, random);
            Global.getSoundPlayer().playSound("ui_discovered_entity", 1f, 1f, fleet.getLocation(), new Vector2f());
        }
        s.cacheGuardianSpawned = true;
    }

    // The electronics roll, the stage and the return marker; the rows print the chip line and grant the Alpha Core.
    private static void salvage(QuestContext<KestevenStage, KestevenState> ctx) {
        int amount = MathHelper.getSeededRandomNumberInRange(ELECTRONICS_MIN, ELECTRONICS_MAX, ctx.random(KestevenState.RANDOM_CACHE_CORE));
        ctx.rewards().commodity("nskr_electronics", amount);
        ctx.set(KestevenFlag.CHIP_SALVAGED);
        if (ctx.stage() != KestevenStage.CHIP_RECOVERED) ctx.advance(KestevenStage.CHIP_RECOVERED);
        if (ctx.has(KestevenFlag.ELIZA_AGREED_SINCERELY)) {
            ctx.mark(QuestHelper.getElizaLoc(), RETURN_STAGES);
        } else {
            MarketAPI home = SectorLookup.asteriaOrOutpost();
            ctx.mark(home, RETURN_STAGES);
        }
        // Also the mark of the job 5 meeting (KestevenJob5Module action markCacheCore), which is the same quest mark.
        ctx.unmark(ctx.target());
    }

    private static StarSystemAPI cacheSystem() {
        return Global.getSector().getStarSystem(Ids.CACHE_SYSTEM_NAME);
    }
}
