package lostsector.campaign.events.hints;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import lostsector.helper.Ids;
import lostsector.helper.MathHelper;
import lostsector.helper.SystemHelper;
import lostsector.helper.fleet.SystemPicker;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// The Kesteven officer at Kesteven bars who sells the location of an unentered procgen system. An offer runs 15 to 30
// days; after it expires, is declined or is bought, the next one follows 0 to 30 days later.
final class HintsTipModule extends QuestModule<HintsStage, HintsState> {

    static final String PERSON_OFFICER = "officer";

    HintsTipModule() {
        super();
    }

    @Override
    protected void declare(Declarations<HintsStage, HintsState> d) {
        d.person(PERSON_OFFICER);
        d.intel(HintsQuest.INTEL_TIP, "hint", Tags.INTEL_FLEET_LOG, Tags.INTEL_EXPLORATION).majorPosting().descriptionBullets().deletable();
        d.check("tipHere", HintsTipModule::tipHere);
        d.check("canAffordTip", ctx -> {
            CampaignFleetAPI player = Global.getSector().getPlayerFleet();
            return ctx.state().offerSystem != null && player != null && player.getCargo().getCredits().get() > ctx.state().offerPrice;
        });
        d.token("offerSystem", ctx -> ctx.state().offerSystem == null ? "" : ctx.state().offerSystem.getName());
        d.token("offerDistance", HintsTipModule::offerDistance);
        d.token("offerPrice", ctx -> ctx.state().offerSystem == null ? "" : Misc.getDGSCredits(ctx.state().offerPrice));
        // The threat word of the offer and of a bought tip is chosen in rows by these checks.
        d.check("offerDerelict", ctx -> ctx.state().offerSystem != null && Tags.THEME_DERELICT.equals(theme(ctx.state().offerSystem)));
        d.check("offerRemnant", ctx -> ctx.state().offerSystem != null && Tags.THEME_REMNANT.equals(theme(ctx.state().offerSystem)));
        d.check("tipDerelict", ctx -> {
            HintRecord record = HintsVisitModule.record(ctx);
            return record != null && Tags.THEME_DERELICT.equals(record.theme);
        });
        d.check("tipRemnant", ctx -> {
            HintRecord record = HintsVisitModule.record(ctx);
            return record != null && Tags.THEME_REMNANT.equals(record.theme);
        });
        d.action("buyTip", HintsTipModule::buy);
        d.action("leaveTip", HintsTipModule::leave);
    }

    @Override
    protected void onDay(QuestContext<HintsStage, HintsState> ctx) {
        HintsState s = ctx.state();
        if (s.offerSystem != null) {
            if (!s.offerBought && ctx.days(HintsState.TIMER_OFFER) > s.offerDays) {
                ctx.log("tip offer expired");
                endOffer(ctx);
            }
            return;
        }
        if (ctx.hasTimer(HintsState.TIMER_COOLDOWN) && ctx.days(HintsState.TIMER_COOLDOWN) < s.cooldownDays) return;
        newOffer(ctx);
    }

    @Override
    protected void devInfo(QuestContext<HintsStage, HintsState> ctx, List<String> lines) {
        HintsState s = ctx.state();
        if (s.offerSystem == null) {
            lines.add("no tip offer, next after " + (int) s.cooldownDays + " days, " + (int) ctx.days(HintsState.TIMER_COOLDOWN) + " passed");
        } else {
            lines.add("tip offer " + s.offerSystem.getName() + " for " + s.offerPrice + (s.offerBought ? ", bought" : "")
                    + ", " + (int) ctx.days(HintsState.TIMER_OFFER) + " of " + (int) s.offerDays + " days");
        }
        lines.add("tips bought " + s.tipsBought);
    }

    // The officer is made with each offer, so the bar shows the same person until the offer ends.
    private static void newOffer(QuestContext<HintsStage, HintsState> ctx) {
        HintsState s = ctx.state();
        StarSystemAPI system = pickSystem(ctx.random("tipSystem"));
        if (system == null) {
            ctx.log("tip offer skipped: no valid system");
            startCooldown(ctx);
            return;
        }
        s.offerSystem = system;
        s.offerBought = false;
        s.offerPrice = 1000 * MathHelper.getSeededRandomNumberInRange(10, 30, ctx.random("tipPrice"));
        s.offerDays = 15f + ctx.random("tipTiming").nextFloat() * 15f;
        ctx.startTimer(HintsState.TIMER_OFFER);
        ctx.clearTimer(HintsState.TIMER_COOLDOWN);
        ctx.people().release(PERSON_OFFICER);
        ctx.people().create(PERSON_OFFICER, Ids.KESTEVEN_FACTION_ID, officer -> officer.setPostId(Ranks.POST_GENERIC_MILITARY));
        ctx.log("tip offer " + system.getName() + " for " + s.offerPrice);
    }

    private static void endOffer(QuestContext<HintsStage, HintsState> ctx) {
        HintsState s = ctx.state();
        s.offerSystem = null;
        s.offerBought = false;
        ctx.clearTimer(HintsState.TIMER_OFFER);
        startCooldown(ctx);
    }

    private static void startCooldown(QuestContext<HintsStage, HintsState> ctx) {
        ctx.state().cooldownDays = Math.max(0f, 30f - ctx.random("tipTiming").nextFloat() * 40f);
        ctx.startTimer(HintsState.TIMER_COOLDOWN);
    }

    private static boolean tipHere(QuestContext<HintsStage, HintsState> ctx) {
        HintsState s = ctx.state();
        if (s.offerSystem == null || s.offerBought || ctx.people().get(PERSON_OFFICER) == null) return false;
        SectorEntityToken target = ctx.target();
        MarketAPI market = target == null ? null : target.getMarket();
        return market != null && Ids.KESTEVEN_FACTION_ID.equals(market.getFaction().getId());
    }

    private static void buy(QuestContext<HintsStage, HintsState> ctx) {
        HintsState s = ctx.state();
        if (s.offerSystem == null || s.offerBought) {
            ctx.log("buyTip skipped: no open offer");
            return;
        }
        ctx.rewards().takeCredits(s.offerPrice);
        s.offerBought = true;
    }

    private static void leave(QuestContext<HintsStage, HintsState> ctx) {
        HintsState s = ctx.state();
        if (s.offerSystem == null) return;
        if (s.offerBought) {
            s.tipsBought++;
            String id = "tip" + s.tipsBought;
            s.records.put(id, new HintRecord(HintsQuest.INTEL_TIP, s.offerSystem, theme(s.offerSystem)));
            ctx.intel().record(id).show(HintsQuest.INTEL_TIP);
            ctx.intel().record(id).setMapLocation(HintsQuest.INTEL_TIP, s.offerSystem.getHyperspaceAnchor());
        }
        endOffer(ctx);
    }

    // The player is docked at the market, and an entity inside a star system is located in hyperspace at that
    // system's position, so the player fleet gives the distance from the bar's market. Two decimals, as a float prints.
    private static String offerDistance(QuestContext<HintsStage, HintsState> ctx) {
        StarSystemAPI system = ctx.state().offerSystem;
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (system == null || player == null) return "";
        float distance = Misc.getDistanceLY(player, system.getCenter());
        distance *= 100f;
        distance = Math.round(distance);
        distance /= 100f;
        return distance + "";
    }

    // The derelict theme wins over the remnant theme; null for a system with neither.
    private static String theme(StarSystemAPI system) {
        if (system.hasTag(Tags.THEME_DERELICT)) return Tags.THEME_DERELICT;
        if (system.hasTag(Tags.THEME_REMNANT)) return Tags.THEME_REMNANT;
        return null;
    }

    // Half the offers pick among systems with a derelict survey ship or mothership, half among systems with a warning beacon.
    private static StarSystemAPI pickSystem(Random random) {
        SystemPicker picker = new SystemPicker(random, 0);
        if (random.nextFloat() < 0.50f) {
            List<String> entities = new ArrayList<>();
            entities.add(Entities.DERELICT_SURVEY_SHIP);
            entities.add(Entities.DERELICT_MOTHERSHIP);
            picker.pickEntities = entities;
        } else {
            List<StarSystemAPI> systems = new ArrayList<>();
            for (SectorEntityToken e : Global.getSector().getHyperspace().getAllEntities()) {
                if (e == null || e.getTags() == null) continue;
                if (e.getTags().contains(Tags.BEACON_LOW) || e.getTags().contains(Tags.BEACON_MEDIUM) || e.getTags().contains(Tags.BEACON_HIGH)) {
                    if (e.getOrbit() == null || e.getOrbit().getFocus() == null) continue;
                    systems.add(SystemHelper.getNearestSystem(e.getOrbit().getFocus().getLocation()));
                }
            }
            picker.pickSystems = systems;
        }
        picker.allowEnteredByPlayer = false;
        picker.pickOnlyInProcgen = true;
        if (picker.get().isEmpty()) return null;
        return picker.pick();
    }
}
