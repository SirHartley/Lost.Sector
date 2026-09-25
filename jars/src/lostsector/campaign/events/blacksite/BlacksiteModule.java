package lostsector.campaign.events.blacksite;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import lostsector.helper.FleetHelper;
import lostsector.helper.fleet.SimpleFleet;
import lostsector.quest.Declarations;
import lostsector.quest.FleetOrders;
import lostsector.quest.FleetRole;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestFleet;
import lostsector.quest.QuestModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Every site: adoption and dialog claim, the alarm, the defenders' countdown and destruction timers, loot and cleanup.
// Site timers are checked daily and the defender count after every battle and despawn of a defender; the defenders'
// movement and their timeout are the raid order that QuestManager applies every 0.1 days (README "Fleets").
final class BlacksiteModule extends QuestModule<BlacksiteStage, BlacksiteState> {

    static final String TRIGGER_SITE = "nskr_bsSite";

    static final String ROLE_LOOTER = "looter";
    static final String ROLE_EVACUATOR = "evacuator";
    static final String ROLE_WRECKER = "wrecker";
    static final String ROLE_REMNANT = "remnant";

    // Days after the alarm until the site opens even with defenders left.
    static final float COUNTDOWN_DAYS = 20f;
    // Summed days of defenders orbiting the site until they finish it.
    static final float DESTROY_FLEET_DAYS = 7f;
    // Age after which a defender gets no more orders and despawns once out of the player's sight (FleetOrders.withdrawAfter).
    static final float DEFENDER_TIMEOUT_DAYS = 30f;

    BlacksiteModule() {
        super(BlacksiteStage.RUNNING);
    }

    @Override
    protected void declare(Declarations<BlacksiteStage, BlacksiteState> d) {
        d.trigger(TRIGGER_SITE);

        d.role(ROLE_LOOTER, FleetRole.of(FleetOrders.raid("looting location", false).withdrawAfter(DEFENDER_TIMEOUT_DAYS)));
        d.role(ROLE_EVACUATOR, FleetRole.of(FleetOrders.raid("evacuating location", false).withdrawAfter(DEFENDER_TIMEOUT_DAYS)));
        d.role(ROLE_WRECKER, FleetRole.of(FleetOrders.raid("destroying location", false).withdrawAfter(DEFENDER_TIMEOUT_DAYS)));
        d.role(ROLE_REMNANT, FleetRole.of(FleetOrders.raid("destroying location", true).withdrawAfter(DEFENDER_TIMEOUT_DAYS)));

        d.check("dormant", ctx -> hasStatus(ctx, SiteRecord.Status.DORMANT));
        d.check("active", ctx -> hasStatus(ctx, SiteRecord.Status.ACTIVE));
        // check faction <factionId>: the site's owner.
        d.check("faction", ctx -> {
            SiteRecord site = site(ctx);
            return site != null && ctx.args().size() == 1 && site.faction.factionId.equals(ctx.args().get(0));
        });

        d.action("activate", this::activate);

        d.token("factionName", ctx -> {
            SiteRecord site = site(ctx);
            return site == null ? "" : Global.getSector().getFaction(site.faction.factionId).getDisplayName();
        });
        d.token("expectedCount", ctx -> {
            SiteRecord site = site(ctx);
            return site == null ? "" : String.valueOf(site.count);
        });
        d.token("expectedNoun", ctx -> {
            SiteRecord site = site(ctx);
            return site == null ? "" : site.count > 1 ? "fleets" : "fleet";
        });
        d.token("strength", ctx -> {
            SiteRecord site = site(ctx);
            return site == null ? "" : BlacksiteSites.strength(site.points);
        });
        d.token("remainingCount", ctx -> {
            SiteRecord site = site(ctx);
            return site == null ? "" : String.valueOf(activeDefenders(ctx, site));
        });
        d.token("remainingNoun", ctx -> {
            SiteRecord site = site(ctx);
            return site == null ? "" : activeDefenders(ctx, site) > 1 ? "fleets" : "fleet";
        });
    }

    // BlacksiteSpawner placed the stations during world generation, before the quest state existed.
    @Override
    protected void onStart(QuestContext<BlacksiteStage, BlacksiteState> ctx) {
        Random random = ctx.random(BlacksiteState.RANDOM_SITES);
        for (String id : BlacksiteSpawner.siteIds()) {
            SectorEntityToken entity = Global.getSector().getEntityById(id);
            if (entity == null) continue;
            SiteRecord site = BlacksiteSites.adopt(entity, random);
            ctx.state().sites.put(site.id, site);
            ctx.claimDialog(entity, TRIGGER_SITE);
            ctx.log("site " + site.id + " " + site.faction + " in " + entity.getStarSystem().getName());
        }
    }

    // Spawns the defenders of the dialog's site; the rows ping the site and print the text.
    private void activate(QuestContext<BlacksiteStage, BlacksiteState> ctx) {
        SiteRecord site = site(ctx);
        if (site == null || site.status != SiteRecord.Status.DORMANT) {
            ctx.log("activate skipped: no dormant site at " + (ctx.target() == null ? null : ctx.target().getId()));
            return;
        }
        Random random = ctx.random(BlacksiteState.RANDOM_FLEETS);
        for (int i = 0; i < site.count; i++) {
            SimpleFleet spec = BlacksiteSites.defender(site, random);
            CampaignFleetAPI fleet = ctx.fleets().spawn(site.faction.role, site.id, spec);
            if (fleet == null) continue;
            BlacksiteSites.settle(fleet, spec.loc, random);
            QuestFleet defender = find(ctx, site, fleet);
            if (defender != null) defender.info().target = site.entity;
        }
        site.status = SiteRecord.Status.ACTIVE;
        ctx.startTimer(BlacksiteState.TIMER_COUNTDOWN + site.id);
    }

    @Override
    protected void onDay(QuestContext<BlacksiteStage, BlacksiteState> ctx) {
        for (SiteRecord site : new ArrayList<>(ctx.state().sites.values())) {
            List<QuestFleet> defenders = defenders(ctx, site);
            if (!defenders.isEmpty() && (site.status == SiteRecord.Status.ACTIVE || site.status == SiteRecord.Status.CLEARED)) {
                for (QuestFleet defender : defenders) {
                    if (FleetHelper.isRaidingTarget(defender.info())) site.destroyDays += 1f;
                }
                if (site.destroyDays >= DESTROY_FLEET_DAYS) destroy(ctx, site, defenders);
            }
            checkCleared(ctx, site);
        }
    }

    @Override
    protected void onBattle(QuestContext<BlacksiteStage, BlacksiteState> ctx, QuestFleet fleet, BattleAPI battle, CampaignFleetAPI primaryWinner) {
        SiteRecord site = ctx.state().sites.get(fleet.record());
        if (site != null) checkCleared(ctx, site);
    }

    @Override
    protected void onFleetGone(QuestContext<BlacksiteStage, BlacksiteState> ctx, QuestFleet fleet, FleetDespawnReason reason, Object param) {
        SiteRecord site = ctx.state().sites.get(fleet.record());
        if (site != null) checkCleared(ctx, site);
    }

    // Opens the site when the countdown ends or no defender is left that has not broken.
    private void checkCleared(QuestContext<BlacksiteStage, BlacksiteState> ctx, SiteRecord site) {
        if (site.status != SiteRecord.Status.ACTIVE) return;
        String timer = BlacksiteState.TIMER_COUNTDOWN + site.id;
        if (ctx.days(timer) < COUNTDOWN_DAYS && activeDefenders(ctx, site) > 0) return;
        ctx.releaseDialog(site.entity);
        ctx.clearTimer(timer);
        SectorEntityToken loot = BlacksiteSites.activateLoot(site, ctx.random(BlacksiteState.RANDOM_LOOT));
        site.status = SiteRecord.Status.CLEARED;
        ctx.log("site " + site.id + " cleared" + (loot == null ? ", no loot entity" : ""));
    }

    // The defenders' raid order withdraws them once their target is cleared.
    private void destroy(QuestContext<BlacksiteStage, BlacksiteState> ctx, SiteRecord site, List<QuestFleet> defenders) {
        ctx.releaseDialog(site.entity);
        ctx.clearTimer(BlacksiteState.TIMER_COUNTDOWN + site.id);
        BlacksiteSites.destroy(site.entity, ctx.random(BlacksiteState.RANDOM_DEBRIS));
        site.status = SiteRecord.Status.DESTROYED;
        for (QuestFleet defender : defenders) {
            defender.info().target = null;
        }
        ctx.log("site " + site.id + " destroyed by its defenders");
    }

    @Override
    protected void devInfo(QuestContext<BlacksiteStage, BlacksiteState> ctx, List<String> lines) {
        for (SiteRecord site : ctx.state().sites.values()) {
            String timer = BlacksiteState.TIMER_COUNTDOWN + site.id;
            lines.add(site.id + ": " + site.faction + " " + site.status + ", " + site.count + " fleets, " + Math.round(site.points) + " points"
                    + ", defenders " + activeDefenders(ctx, site) + "/" + defenders(ctx, site).size()
                    + (ctx.hasTimer(timer) ? ", countdown " + Math.round(ctx.days(timer)) + "/" + Math.round(COUNTDOWN_DAYS) + " days" : "")
                    + ", destroy " + site.destroyDays + "/" + DESTROY_FLEET_DAYS + " fleet-days");
        }
    }

    // The site of the dialog target, or of the entity a token is replaced for.
    private static SiteRecord site(QuestContext<BlacksiteStage, BlacksiteState> ctx) {
        SectorEntityToken target = ctx.target();
        return target == null ? null : ctx.state().sites.get(target.getId());
    }

    private static boolean hasStatus(QuestContext<BlacksiteStage, BlacksiteState> ctx, SiteRecord.Status status) {
        SiteRecord site = site(ctx);
        return site != null && site.status == status;
    }

    private static List<QuestFleet> defenders(QuestContext<BlacksiteStage, BlacksiteState> ctx, SiteRecord site) {
        List<QuestFleet> defenders = new ArrayList<>();
        for (QuestFleet fleet : ctx.fleets().get(site.faction.role)) {
            if (site.id.equals(fleet.record()) && fleet.fleet().isAlive()) defenders.add(fleet);
        }
        return defenders;
    }

    // Defenders that still count: alive and not broken.
    private static int activeDefenders(QuestContext<BlacksiteStage, BlacksiteState> ctx, SiteRecord site) {
        int count = 0;
        for (QuestFleet defender : defenders(ctx, site)) {
            if (!FleetHelper.isRaidBroken(defender.info())) count++;
        }
        return count;
    }

    private static QuestFleet find(QuestContext<BlacksiteStage, BlacksiteState> ctx, SiteRecord site, CampaignFleetAPI fleet) {
        for (QuestFleet defender : ctx.fleets().get(site.faction.role)) {
            if (defender.fleet() == fleet) return defender;
        }
        return null;
    }
}
