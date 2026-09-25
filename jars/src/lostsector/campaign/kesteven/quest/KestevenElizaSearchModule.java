package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import lostsector.helper.MathHelper;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;

import java.util.ArrayList;
import java.util.List;

// The search for Eliza at pirate bars during JOB5_DISKS: three bar conversations in the # KESTEVEN QUESTLINE: ELIZA
// SEARCH block of rules.csv, one per step of elizaSearchStage, each at most once per market. Paying the first spacer
// names a contact market; the contact names Eliza's market.
final class KestevenElizaSearchModule extends QuestModule<KestevenStage, KestevenState> {

    static final String PERSON_SPACER = "roughSpacer";
    static final String PERSON_SLY_SPACER = "slySpacer";
    static final String PERSON_CONTACT = "pirateContact";

    // The Delve entry of the job 5 module receives the message when the contact moves.
    static final String DELVE_INTEL = KestevenJob5Module.INTEL;
    static final String UPDATE_CONTACT_MOVED = "contactMoved";

    KestevenElizaSearchModule() {
        super(KestevenStage.JOB5_DISKS);
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.person(PERSON_SPACER);
        d.person(PERSON_SLY_SPACER);
        d.person(PERSON_CONTACT);

        d.check("elizaSpacerHere", ctx -> searchBar(ctx) && ctx.state().elizaSearchStage == 0);
        d.check("elizaSlySpacerHere", ctx -> searchBar(ctx) && ctx.state().elizaSearchStage == 1);
        d.check("elizaContactHere", KestevenElizaSearchModule::contactHere);
        d.check("elizaSpacerAffordable", ctx -> {
            CampaignFleetAPI player = Global.getSector().getPlayerFleet();
            return player != null && player.getCargo().getCredits().get() > ctx.state().elizaSpacerPrice;
        });

        d.action("elizaSpacerOpen", ctx -> ctx.state().elizaSpacerPrice =
                MathHelper.getSeededRandomNumberInRange(4000, 7000, ctx.random(KestevenState.RANDOM_QUEST)));
        d.action("elizaPickContact", KestevenElizaSearchModule::pickContact);
        d.action("elizaSpacerPay", KestevenElizaSearchModule::paySpacer);
        d.action("elizaSpacerLeave", ctx -> {
            useMarket(ctx);
            ctx.state().elizaSearchStage = 1;
        });
        d.action("elizaSpacerPaidLeave", ctx -> ctx.state().elizaSearchStage = 2);
        d.action("elizaSlySpacerLeave", ctx -> {
            useMarket(ctx);
            ctx.state().elizaSearchStage = 2;
        });
        d.action("elizaContactMeet", KestevenElizaSearchModule::useMarket);
        d.action("elizaPickMarket", ctx -> QuestHelper.setElizaLoc());
        d.action("elizaContactLeave", KestevenElizaSearchModule::leaveContact);

        d.token("elizaContactMarket", ctx -> {
            SectorEntityToken contact = ctx.state().elizaContactMarket;
            return contact == null || contact.getMarket() == null ? "" : contact.getMarket().getName();
        });
        d.token("elizaContactEntity", ctx -> ctx.state().elizaContactMarket == null ? "" : ctx.state().elizaContactMarket.getName());
        d.token("elizaContactFormer", ctx -> ctx.state().elizaContactFormerName == null ? "" : ctx.state().elizaContactFormerName);
        d.token("elizaMarketEntity", ctx -> ctx.state().elizaMarket == null ? "" : ctx.state().elizaMarket.getName());
    }

    // One person per conversation, the same at every pirate bar; the old bar events drew a new one per market.
    @Override
    protected void onStart(QuestContext<KestevenStage, KestevenState> ctx) {
        for (String key : List.of(PERSON_SPACER, PERSON_SLY_SPACER, PERSON_CONTACT)) {
            ctx.people().create(key, Factions.PIRATES, person -> person.setPostId(Ranks.POST_GENERIC_MILITARY));
        }
    }

    @Override
    protected void onStop(QuestContext<KestevenStage, KestevenState> ctx) {
        for (String key : List.of(PERSON_SPACER, PERSON_SLY_SPACER, PERSON_CONTACT)) ctx.people().release(key);
    }

    // The paid-for contact moves to another pirate market when its market is decivilized before the player meets it.
    @Override
    protected void onDecivilized(QuestContext<KestevenStage, KestevenState> ctx, MarketAPI market, boolean fullyDestroyed) {
        KestevenState s = ctx.state();
        SectorEntityToken contact = s.elizaContactMarket;
        if (contact == null || contact.getMarket() == null || !contact.getMarket().isPlanetConditionMarketOnly()) return;
        if (!ctx.has(KestevenFlag.ELIZA_SPACER_PAID) || ctx.has(KestevenFlag.ELIZA_FOUND) || s.elizaSearchStage != 2) return;
        ctx.unmark(contact);
        s.elizaContactFormerName = contact.getMarket().getPrimaryEntity().getName();
        s.elizaContactMarket = QuestHelper.pickElizaMarket(ctx.random(KestevenState.RANDOM_QUEST), false);
        ctx.mark(s.elizaContactMarket, KestevenStage.JOB5_DISKS);
        ctx.log("Eliza contact moved from " + s.elizaContactFormerName + " to " + s.elizaContactMarket.getName());
        // The old campaign message played the minor message sound (MessageIntel.getCommMessageSound).
        if (ctx.intel().isShown(DELVE_INTEL)) ctx.intel().update(DELVE_INTEL, UPDATE_CONTACT_MOVED, BaseIntelPlugin.getSoundMinorMessage());
    }

    @Override
    protected void devInfo(QuestContext<KestevenStage, KestevenState> ctx, List<String> lines) {
        KestevenState s = ctx.state();
        lines.add("search step " + s.elizaSearchStage + ", used markets " + s.elizaSearchUsedMarkets + ", spacer price " + s.elizaSpacerPrice);
        lines.add("contact: " + (s.elizaContactMarket == null ? "none" : s.elizaContactMarket.getName())
                + ", Eliza: " + (s.elizaMarket == null ? "none" : s.elizaMarket.getName()));
    }

    // For the old callers in this package: the chain's step and markets.

    static int searchStage() {
        KestevenState state = KestevenQuest.state();
        return state == null ? 0 : state.elizaSearchStage;
    }

    static SectorEntityToken contactMarket() {
        KestevenState state = KestevenQuest.state();
        return state == null ? null : state.elizaContactMarket;
    }

    static List<String> usedMarkets() {
        KestevenState state = KestevenQuest.state();
        return state == null ? new ArrayList<>() : state.elizaSearchUsedMarkets;
    }

    // A pirate market whose bar has not held a conversation of the search yet.
    private static boolean searchBar(QuestContext<KestevenStage, KestevenState> ctx) {
        MarketAPI market = market(ctx);
        return market != null && Factions.PIRATES.equals(market.getFaction().getId())
                && !ctx.state().elizaSearchUsedMarkets.contains(market.getId());
    }

    // After paying the first spacer, only the named market.
    private static boolean contactHere(QuestContext<KestevenStage, KestevenState> ctx) {
        if (!searchBar(ctx) || ctx.state().elizaSearchStage != 2) return false;
        if (!ctx.has(KestevenFlag.ELIZA_SPACER_PAID)) return true;
        SectorEntityToken contact = ctx.state().elizaContactMarket;
        return contact != null && contact.getMarket() == market(ctx);
    }

    private static void pickContact(QuestContext<KestevenStage, KestevenState> ctx) {
        useMarket(ctx);
        ctx.state().elizaContactMarket = QuestHelper.pickElizaMarket(ctx.random(KestevenState.RANDOM_QUEST), false);
    }

    private static void paySpacer(QuestContext<KestevenStage, KestevenState> ctx) {
        ctx.set(KestevenFlag.ELIZA_SPACER_PAID);
        ctx.mark(ctx.state().elizaContactMarket, KestevenStage.JOB5_DISKS);
        ctx.rewards().takeCredits(ctx.state().elizaSpacerPrice);
    }

    // Eliza's market keeps the plain important flag: ElizaDialog, ElizaRaid, CacheCoreDialog and the endings set and
    // unset that flag directly until they move to the quest (T28 and later).
    private static void leaveContact(QuestContext<KestevenStage, KestevenState> ctx) {
        ctx.set(KestevenFlag.ELIZA_FOUND);
        ctx.state().elizaSearchStage = 3;
        if (ctx.has(KestevenFlag.ELIZA_SPACER_PAID)) ctx.unmark(ctx.state().elizaContactMarket);
        if (ctx.state().elizaMarket != null) {
            ctx.state().elizaMarket.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MISSION_IMPORTANT, true);
        }
    }

    private static void useMarket(QuestContext<KestevenStage, KestevenState> ctx) {
        MarketAPI market = market(ctx);
        if (market != null && !ctx.state().elizaSearchUsedMarkets.contains(market.getId())) {
            ctx.state().elizaSearchUsedMarkets.add(market.getId());
        }
    }

    private static MarketAPI market(QuestContext<KestevenStage, KestevenState> ctx) {
        SectorEntityToken target = ctx.target();
        return target == null ? null : target.getMarket();
    }
}
