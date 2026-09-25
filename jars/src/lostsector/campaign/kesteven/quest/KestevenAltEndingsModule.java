package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.util.Misc;
import lostsector.helper.Ids;
import lostsector.helper.MathHelper;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;

import java.util.List;

// The two alternative endings at stage 19: an admin official at a Luddic Church or Luddic Path market destroys the
// Unlimited Production Chip, one at a Tri-Tachyon market buys it. The conversations are the # KESTEVEN QUESTLINE:
// ALTERNATIVE ENDINGS rows; the official who reaches the second conversation is locked to it through the speaker flag
// $nskr_kq_altEndingLocked. Active at COMPLETED too, because the Tri-Tachyon row places the Chip after the fallout
// action has moved the stage there.
final class KestevenAltEndingsModule extends QuestModule<KestevenStage, KestevenState> {

    static final int TT_PRICE = 2000000;
    static final int TT_RAISED_PRICE = 2500000;

    private static final float FALLOUT_PERSON_REP = -0.50f;
    private static final float FALLOUT_KESTEVEN_MIN = -0.65f;
    private static final float FALLOUT_KESTEVEN_MAX = -0.55f;
    private static final String CULANN_SYSTEM = "Hybrasil";
    private static final String CULANN_MARKET = "culann";

    KestevenAltEndingsModule() {
        super(KestevenStage.CHIP_RECOVERED, KestevenStage.COMPLETED);
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.check("altEndingLuddicMarket", ctx -> marketFaction(ctx, Factions.LUDDIC_PATH) || marketFaction(ctx, Factions.LUDDIC_CHURCH));
        d.check("altEndingTtMarket", ctx -> marketFaction(ctx, Factions.TRITACHYON));

        d.action("altEndingRaisePrice", ctx -> ctx.state().ttPayout = TT_RAISED_PRICE);
        d.action("altEndingPay", KestevenAltEndingsModule::pay);
        d.action("altEndingFallout", KestevenAltEndingsModule::fallout);
        d.action("altEndingPlaceChip", KestevenAltEndingsModule::placeChip);

        d.token("altEndingTtOffer", ctx -> Misc.getDGSCredits(TT_PRICE));
        d.token("altEndingTtRaised", ctx -> Misc.getDGSCredits(TT_RAISED_PRICE));
    }

    @Override
    protected void devInfo(QuestContext<KestevenStage, KestevenState> ctx, List<String> lines) {
        lines.add("done: " + ctx.has(KestevenFlag.ALT_ENDING_DONE) + ", Tri-Tachyon price: " + ctx.state().ttPayout
                + ", second talk Luddic: " + ctx.has(KestevenFlag.LUDDIC_ENDING_SECOND_TALK) + ", Tri-Tachyon: " + ctx.has(KestevenFlag.TT_ENDING_SECOND_TALK));
    }

    private static boolean marketFaction(QuestContext<KestevenStage, KestevenState> ctx, String factionId) {
        SectorEntityToken target = ctx.target();
        MarketAPI market = target == null ? null : target.getMarket();
        return market != null && factionId.equals(market.getFactionId());
    }

    // The price is the raised one after the counter-offer, otherwise the first offer.
    private static void pay(QuestContext<KestevenStage, KestevenState> ctx) {
        KestevenState s = ctx.state();
        if (s.ttPayout < TT_PRICE) s.ttPayout = TT_PRICE;
        ctx.rewards().credits((int) s.ttPayout);
    }

    // Kesteven, Jack, Alice and Eliza turn on the player, and the questline completes.
    private static void fallout(QuestContext<KestevenStage, KestevenState> ctx) {
        // The Cache core's return marker ends with its scope; Eliza's entity may still carry the Eliza search's plain flag.
        if (ctx.state().elizaMarket != null) ctx.state().elizaMarket.getMemory().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        turnAway(KestevenPeople.getAlice(), true);
        turnAway(KestevenPeople.getJack(), true);
        turnAway(KestevenPeople.getEliza(), false);

        // Drawn every time, applied only when it lowers the relationship.
        float kesteven = MathHelper.getSeededRandomNumberInRange(FALLOUT_KESTEVEN_MIN, FALLOUT_KESTEVEN_MAX, ctx.random(KestevenState.RANDOM_ALT_ENDING));
        float current = Global.getSector().getPlayerFaction().getRelationship(Ids.KESTEVEN_FACTION_ID);
        if (current > kesteven) ctx.rewards().relationship(Ids.KESTEVEN_FACTION_ID, kesteven - current);

        ctx.set(KestevenFlag.ALT_ENDING_DONE);
        if (ctx.stage() != KestevenStage.COMPLETED) ctx.advance(KestevenStage.COMPLETED);
        QuestHelper.saveEnding();
    }

    private static void turnAway(PersonAPI person, boolean suspendContact) {
        if (person == null) return;
        person.getRelToPlayer().adjustRelationship(FALLOUT_PERSON_REP, RepLevel.HOSTILE);
        if (suspendContact && ContactIntel.getContactIntel(person) != null) {
            ContactIntel.getContactIntel(person).setState(ContactIntel.ContactState.SUSPENDED);
        }
    }

    // On Culann while Tri-Tachyon holds it and Hybrasil exists, otherwise on the market where the Chip was sold.
    private static void placeChip(QuestContext<KestevenStage, KestevenState> ctx) {
        StarSystemAPI hybrasil = Global.getSector().getStarSystem(CULANN_SYSTEM);
        MarketAPI culann = hybrasil == null ? null : Global.getSector().getEconomy().getMarket(CULANN_MARKET);
        if (culann != null && Factions.TRITACHYON.equals(culann.getFactionId())) {
            culann.addCondition(Ids.UNLIMITED_PRODUCTION_CHIP_CONDITION_ID);
            return;
        }
        SectorEntityToken target = ctx.target();
        MarketAPI market = target == null ? null : target.getMarket();
        if (market == null) {
            ctx.log("altEndingPlaceChip skipped: no market at the dialog target");
            return;
        }
        market.addCondition(Ids.UNLIMITED_PRODUCTION_CHIP_CONDITION_ID);
    }
}
