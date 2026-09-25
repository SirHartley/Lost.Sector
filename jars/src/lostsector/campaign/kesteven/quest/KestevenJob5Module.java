package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.IntelSortTier;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import lostsector.helper.Ids;
import lostsector.helper.SectorLookup;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;

import java.util.List;

// Job 5, "The Delve": the meeting with Jack and Alice at the bar (# KESTEVEN QUESTLINE: JOB 5 rows), the guard who
// escorts the player there, and the intel entry from JOB5_DISKS until the questline is completed. Tips, disks and
// hand-ins belong to the hub and the other job 5 modules. Active in COMPLETED only to complete the intel entry.
final class KestevenJob5Module extends QuestModule<KestevenStage, KestevenState> {

    static final String INTEL = "job5";
    static final String PERSON_GUARD = "delveGuard";
    // The meeting pays it with AddCredits and the same literal amount.
    static final int ADVANCE_CREDITS = 150000;

    private static final String CACHE_CORE_ID = "nskr_cache_core";
    // The old meeting set the core's important flag for good; the core salvage (KestevenCacheModule) unmarks it.
    private static final KestevenStage[] CACHE_CORE_MARK = {
            KestevenStage.JOB5_MEETING, KestevenStage.JOB5_DISKS, KestevenStage.CACHE_KNOWN, KestevenStage.CACHE_CLEARED,
            KestevenStage.CHIP_RECOVERED, KestevenStage.COMPLETED, KestevenStage.FAILED};

    KestevenJob5Module() {
        super(KestevenStage.JOB5_MEETING, KestevenStage.JOB5_DISKS, KestevenStage.CACHE_KNOWN, KestevenStage.CACHE_CLEARED,
                KestevenStage.CHIP_RECOVERED, KestevenStage.COMPLETED);
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.person(PERSON_GUARD);
        d.intel(INTEL, "job2", Tags.INTEL_STORY, Tags.INTEL_IMPORTANT, Tags.INTEL_ACCEPTED, Tags.INTEL_MISSIONS)
                .tier(IntelSortTier.TIER_2).majorPosting().faction(Ids.KESTEVEN_FACTION_ID).deletable().descriptionBullets();

        d.check("delveMeetingHere", ctx -> {
            SectorEntityToken target = ctx.target();
            MarketAPI market = target == null ? null : target.getMarket();
            return market != null && market == SectorLookup.asteriaOrOutpost();
        });
        // The old meeting chose its Asteria scenes whenever Asteria exists, even with the questline at the Outpost.
        d.check("asteriaGenerated", ctx -> SectorLookup.getAsteria() != null);
        // Step 2 of the Eliza search: the paid-for contact market is known.
        d.check("delveContactKnown", ctx -> ctx.state().elizaSearchStage == 2 && ctx.state().elizaContactMarket != null);
        d.check("delveElizaLocated", ctx -> ctx.state().elizaMarket != null);

        d.action("markCacheCore", KestevenJob5Module::markCacheCore);

        d.token("delveAdvanceCredits", ctx -> Misc.getDGSCredits(ADVANCE_CREDITS));
        d.token("delveDisks", ctx -> String.valueOf(ctx.state().disksRecovered));
        d.token("delveJob3Constellation", ctx -> constellation(ctx.state().job3Target));
        d.token("delveJob4Constellation", ctx -> constellation(ctx.state().job4EnemyTarget));
        d.token("delveContactMarket", ctx -> marketName(ctx.state().elizaContactMarket));
        d.token("delveContactSystem", ctx -> systemName(ctx.state().elizaContactMarket));
        d.token("delveElizaMarket", ctx -> marketName(ctx.state().elizaMarket));
        d.token("delveElizaEntity", ctx -> ctx.state().elizaMarket == null ? "" : ctx.state().elizaMarket.getName());
        d.token("delveElizaSystem", ctx -> systemName(ctx.state().elizaMarket));
    }

    // The entry shows when JOB5_DISKS starts, or CACHE_KNOWN after the story skip. A jump acts only in its target
    // stage, so the posting message describes that stage, as the story skip's did, and a passed meeting gets no guard.
    @Override
    protected void onStage(QuestContext<KestevenStage, KestevenState> ctx, KestevenStage from) {
        if (ctx.isJump() && ctx.stage() != ctx.jumpTarget()) return;
        switch (ctx.stage()) {
            case JOB5_MEETING:
                ctx.people().create(PERSON_GUARD, Ids.KESTEVEN_FACTION_ID, FullName.Gender.MALE, guard -> {
                    guard.setPostId(Ranks.POST_GENERIC_MILITARY);
                    guard.setPortraitSprite(Global.getSettings().getSpriteName("characters", "nskr_guard"));
                });
                break;
            // The old entry had no map location at COMPLETED.
            case COMPLETED:
                if (!ctx.intel().isShown(INTEL)) break;
                ctx.intel().setMapLocation(INTEL, null);
                ctx.intel().complete(INTEL);
                break;
            default:
                ctx.people().release(PERSON_GUARD);
                ctx.intel().show(INTEL);
                ctx.intel().setMapLocation(INTEL, mapLocation(ctx));
                break;
        }
    }

    // Failure or a jump: the entry ends at once, as the old one did on failure.
    @Override
    protected void onStop(QuestContext<KestevenStage, KestevenState> ctx) {
        ctx.people().release(PERSON_GUARD);
        if (ctx.intel().isShown(INTEL)) ctx.intel().end(INTEL);
    }

    // The old entry computed its map location on every display. The marker follows the questline's home, which
    // ExileManager moves in its own daily check, and Eliza's market, which KestevenElizaModule moves when it
    // decivilizes; neither reports the change, so the marker is refreshed once a day.
    @Override
    protected void onDay(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.intel().isShown(INTEL)) ctx.intel().setMapLocation(INTEL, mapLocation(ctx));
    }

    @Override
    protected void devInfo(QuestContext<KestevenStage, KestevenState> ctx, List<String> lines) {
        SectorEntityToken location = ctx.intel().isShown(INTEL) ? mapLocation(ctx) : null;
        lines.add("guard: " + (ctx.people().get(PERSON_GUARD) == null ? "none" : ctx.people().get(PERSON_GUARD).getNameString()));
        lines.add("intel shown: " + ctx.intel().isShown(INTEL) + ", map location: " + (location == null ? "none" : location.getName()));
        lines.add("disks: " + ctx.state().disksRecovered + ", satellites: " + ctx.state().satellitesRecovered);
    }

    private static SectorEntityToken mapLocation(QuestContext<KestevenStage, KestevenState> ctx) {
        switch (ctx.stage()) {
            case JOB5_DISKS:
                return home();
            case CACHE_KNOWN:
            case CACHE_CLEARED:
                StarSystemAPI cache = Global.getSector().getStarSystem(Ids.CACHE_SYSTEM_NAME);
                return cache == null ? null : cache.getCenter();
            case CHIP_RECOVERED:
                return ctx.has(KestevenFlag.ELIZA_AGREED_SINCERELY) ? ctx.state().elizaMarket : home();
            default:
                return null;
        }
    }

    private static SectorEntityToken home() {
        MarketAPI home = SectorLookup.asteriaOrOutpost();
        return home == null ? null : home.getPrimaryEntity();
    }

    // Only when the player already defeated the Cache guardian, which creates the core.
    private static void markCacheCore(QuestContext<KestevenStage, KestevenState> ctx) {
        StarSystemAPI cache = Global.getSector().getStarSystem(Ids.CACHE_SYSTEM_NAME);
        if (cache == null) return;
        for (SectorEntityToken entity : cache.getAllEntities()) {
            if (CACHE_CORE_ID.equals(entity.getId())) ctx.mark(entity, CACHE_CORE_MARK);
        }
    }

    private static String constellation(SectorEntityToken location) {
        StarSystemAPI system = location == null ? null : location.getStarSystem();
        if (system == null || system.getConstellation() == null) return "";
        return KestevenQuest.constellationName(system.getConstellation().getNameWithType());
    }

    private static String marketName(SectorEntityToken entity) {
        return entity == null || entity.getMarket() == null ? "" : entity.getMarket().getName();
    }

    private static String systemName(SectorEntityToken entity) {
        return entity == null || entity.getStarSystem() == null ? "" : entity.getStarSystem().getName();
    }
}
