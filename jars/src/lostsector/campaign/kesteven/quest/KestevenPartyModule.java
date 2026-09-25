package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import lostsector.helper.MathHelper;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;

import java.util.List;

// Job 3, the party at the expedition's home market: a bar event in the `# KESTEVEN QUESTLINE: JOB 3 PARTY` rows.
// The module owns the guests, the drink count and the hangover bill; the rows own the text, the flags and the stage.
final class KestevenPartyModule extends QuestModule<KestevenStage, KestevenState> {

    static final String EMPLOYEE = "partyEmployee";
    static final String ENGINEER = "partyEngineer";
    static final String ENTREPRENEUR = "partyEntrepreneur";
    static final String HOST = "partyHost";
    static final String PATROL_COMMANDER = "partyPatrolCommander";
    static final String FLEET_COMMANDER = "partyFleetCommander";
    static final String AGENT = "partyAgent";
    private static final String[] GUESTS = {EMPLOYEE, ENGINEER, ENTREPRENEUR, HOST, PATROL_COMMANDER, FLEET_COMMANDER, AGENT};

    private static final int HANGOVER_MIN = 4000;
    private static final int HANGOVER_MAX = 7000;

    KestevenPartyModule() {
        super(KestevenStage.JOB3_ACTIVE, KestevenStage.JOB3_TARGET_KNOWN);
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        for (String guest : GUESTS) {
            d.person(guest);
        }

        d.check("partyHere", KestevenPartyModule::atHomeMarket);
        d.check("partyTipsy", ctx -> ctx.state().partyDrinks >= 1);
        d.check("partyDrunk", ctx -> ctx.state().partyDrinks >= 2);

        // Argument: the number of drinks.
        d.action("partyDrink", KestevenPartyModule::drink);
        d.action("partyHangover", KestevenPartyModule::hangover);
    }

    // Genders and posts as the old bar event gave them; the text calls the engineer and the entrepreneur "two men".
    // A jump past both stages invites no one.
    @Override
    protected void onStart(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.isJump() && !isActiveIn(ctx.jumpTarget())) return;
        guest(ctx, EMPLOYEE, Gender.ANY, Ranks.POST_GENERIC_MILITARY);
        guest(ctx, ENGINEER, Gender.MALE, "kTechEngineer");
        guest(ctx, ENTREPRENEUR, Gender.MALE, Ranks.POST_ENTREPRENEUR);
        guest(ctx, HOST, Gender.FEMALE, Ranks.POST_BASE_COMMANDER);
        guest(ctx, PATROL_COMMANDER, Gender.MALE, Ranks.POST_PATROL_COMMANDER);
        guest(ctx, FLEET_COMMANDER, Gender.ANY, Ranks.POST_FLEET_COMMANDER);
        guest(ctx, AGENT, Gender.ANY, Ranks.POST_AGENT);
    }

    // JOB3_TARGET_KNOWN follows the party's coordinates.
    @Override
    protected void onSkip(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.stage() == KestevenStage.JOB3_ACTIVE) ctx.set(KestevenFlag.JOB3_TARGET_DISCOVERED);
    }

    @Override
    protected void onStop(QuestContext<KestevenStage, KestevenState> ctx) {
        for (String guest : GUESTS) {
            ctx.people().release(guest);
        }
    }

    @Override
    protected void devInfo(QuestContext<KestevenStage, KestevenState> ctx, List<String> lines) {
        lines.add("party drinks: " + ctx.state().partyDrinks + ", declined: " + ctx.has(KestevenFlag.JOB3_PARTY_DECLINED));
    }

    private static void guest(QuestContext<KestevenStage, KestevenState> ctx, String key, Gender gender, String post) {
        ctx.people().create(key, Factions.TRITACHYON, gender, person -> person.setPostId(post));
    }

    private static boolean atHomeMarket(QuestContext<KestevenStage, KestevenState> ctx) {
        SectorEntityToken start = ctx.state().job3Start;
        SectorEntityToken target = ctx.target();
        if (start == null || target == null) return false;
        MarketAPI market = target.getMarket();
        return market != null && market == start.getMarket();
    }

    private static void drink(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.args().size() != 1) {
            ctx.log("partyDrink needs the number of drinks, got " + ctx.args());
            return;
        }
        ctx.state().partyDrinks += Integer.parseInt(ctx.args().get(0));
    }

    // Capped by the credits held, rounded up so a fractional balance ends at zero (takeCredits clamps); with no
    // credits nothing is taken and no receipt prints.
    private static void hangover(QuestContext<KestevenStage, KestevenState> ctx) {
        int bill = MathHelper.getSeededRandomNumberInRange(HANGOVER_MIN, HANGOVER_MAX, ctx.random("partyHangover"));
        int held = (int) Math.ceil(Global.getSector().getPlayerFleet().getCargo().getCredits().get());
        int lost = Math.min(bill, held);
        if (lost > 0) ctx.rewards().takeCredits(lost);
    }
}
