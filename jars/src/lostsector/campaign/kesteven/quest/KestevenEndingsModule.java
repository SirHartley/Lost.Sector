package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.DiplomacyManager;
import lostsector.ModPlugin;
import lostsector.campaign.starts.GameModeManager;
import lostsector.campaign.starts.hellspawn.HellSpawnEventFactors;
import lostsector.campaign.starts.hellspawn.HellSpawnEventIntel;
import lostsector.dialogue.rules.nskr_shipSwap;
import lostsector.helper.Ids;
import lostsector.helper.MathHelper;
import lostsector.helper.SectorLookup;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;
import lostsector.settings.Setting;
import lostsector.settings.SettingsManager;
import lostsector.world.systems.asteria.Asteria;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// The Kesteven and Eliza endings at CHIP_RECOVERED (# KESTEVEN QUESTLINE: ENDINGS rows, which take over the dialog of
// the home market and of Eliza's market on OpenInteractionDialog while an ending is available): the checks of those rows,
// the rewards and relationship changes no vanilla command makes, and after the Eliza ending the commission restore and
// the relationship caps. Active in every stage: the restore and caps test only flags.
final class KestevenEndingsModule extends QuestModule<KestevenStage, KestevenState> {

    // The rows pay these with AddCredits, AddStoryPoints and AddRemoveAnyItem and the same literal amounts.
    static final int KESTEVEN_STORY_POINTS = 1;
    static final int ELIZA_STORY_POINTS = 2;
    static final float EXCHANGE_POINTS = 450000f;

    private static final String IRONSHELL_FACTION_ID = "ironshell";
    // After the Eliza ending, the highest relationship the player can reach with Kesteven, and with the Hegemony and Iron Shell.
    static final float ELIZA_MAX_RELATION_KESTEVEN = -0.50f;
    static final float ELIZA_MAX_RELATION_HEGEMONY = -0.35f;

    KestevenEndingsModule() {
        super();
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        // The old CorePlugin routes, checked when the entity's dialog opens.
        d.check("kestevenEndingHere", ctx -> kestevenEndingAvailable(ctx) && kestevenTargets().contains(ctx.target()));
        d.check("elizaEndingHere", ctx -> elizaEndingAvailable(ctx) && ctx.target() != null && KestevenQuest.atElizaMarket(ctx.target()));

        // Read before the matching action runs: a FireAll matches all its rows before it runs their scripts.
        d.check("endingTriTachyonAbove", ctx -> relationship(Factions.PLAYER, Factions.TRITACHYON) > ctx.state().kestevenEndingTriTachyonRep);
        d.check("endingKestevenTriTachyonAbove", ctx -> relationship(Ids.KESTEVEN_FACTION_ID, Factions.TRITACHYON) > ctx.state().kestevenEndingTriTachyonRep);
        d.check("elizaEndingKestevenAbove", ctx -> relationship(Factions.PLAYER, Ids.KESTEVEN_FACTION_ID) > ctx.state().commissionRepKesteven);
        d.check("elizaEndingHegemonyAbove", ctx -> relationship(Factions.PLAYER, Factions.HEGEMONY) > ctx.state().commissionRepHegemony);
        d.check("elizaEndingIronShellAbove", ctx -> ModPlugin.IS_IRONSHELL && relationship(Factions.PLAYER, IRONSHELL_FACTION_ID) > ctx.state().commissionRepHegemony);

        d.action("kestevenEnding", KestevenEndingsModule::kestevenEnding);
        d.action("endingLowerTriTachyon", ctx -> setRelationship(Factions.PLAYER, Factions.TRITACHYON, ctx.state().kestevenEndingTriTachyonRep));
        d.action("endingKestevenTriTachyonWar", ctx -> setRelationship(Ids.KESTEVEN_FACTION_ID, Factions.TRITACHYON, ctx.state().kestevenEndingTriTachyonRep));
        d.action("elizaEnding", KestevenEndingsModule::elizaEnding);
        d.action("elizaEndingLowerKesteven", ctx -> setRelationship(Factions.PLAYER, Ids.KESTEVEN_FACTION_ID, ctx.state().commissionRepKesteven));
        d.action("elizaEndingLowerHegemony", ctx -> setRelationship(Factions.PLAYER, Factions.HEGEMONY, ctx.state().commissionRepHegemony));
        d.action("elizaEndingLowerIronShell", ctx -> setRelationship(Factions.PLAYER, IRONSHELL_FACTION_ID, ctx.state().commissionRepHegemony));

        // Relationships as the old receipts printed them: times 100, rounded.
        d.token("endingTriTachyonRep", ctx -> String.valueOf(Math.round(ctx.state().kestevenEndingTriTachyonRep * 100f)));
        d.token("elizaEndingPiratesRep", ctx -> String.valueOf(Math.round(ctx.state().commissionRepPirates * 100f)));
        d.token("elizaEndingKestevenRep", ctx -> String.valueOf(Math.round(ctx.state().commissionRepKesteven * 100f)));
        d.token("elizaEndingHegemonyRep", ctx -> String.valueOf(Math.round(ctx.state().commissionRepHegemony * 100f)));
        // Eliza's word for the player; the vanilla $manOrWoman token has no form for other genders.
        d.token("endingManOrWoman", ctx -> {
            FullName.Gender gender = Global.getSector().getPlayerPerson().getName().getGender();
            if (gender == FullName.Gender.MALE) return "man";
            if (gender == FullName.Gender.FEMALE) return "woman";
            return "captain";
        });
    }

    // A jump past CHIP_RECOVERED takes the ending the story is set for: Eliza's after the hand-over, Kesteven's
    // otherwise. Only the finished flag is set; the rewards, relationships and unlocked settings are the endings' own.
    @Override
    protected void onSkip(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.stage() != KestevenStage.CHIP_RECOVERED) return;
        if (ctx.has(KestevenFlag.CHIP_HANDED_TO_ELIZA) && ctx.has(KestevenFlag.ELIZA_HELPED)) ctx.set(KestevenFlag.ELIZA_ENDING_DONE);
        else ctx.set(KestevenFlag.KESTEVEN_ENDING_DONE);
    }

    // The restore waits for the commission to end. Vanilla ends it on its next unpaused advance once the relationship drops below its minimum, and reports that
    // through CommissionEndedListener (FactionCommissionIntel.endMission); Nexerelin's Nex_FactionCommissionIntel
    // overrides endMission without that report. Both unset the commission faction ($fcm_faction), which this watches
    // while the restore is pending.
    @Override
    protected boolean wantsFrames(QuestContext<KestevenStage, KestevenState> ctx) {
        return ctx.has(KestevenFlag.COMMISSION_RESTORE_PENDING) && !ctx.state().commissionRestored;
    }

    @Override
    protected void onFrame(QuestContext<KestevenStage, KestevenState> ctx, float amount) {
        if (Misc.getCommissionFactionId() != null) return;
        restoreCommissionRelationships(ctx);
    }

    // The relationship caps after the Eliza ending, lowered by Nexerelin's cap on the relationship.
    @Override
    protected void onReputationChange(QuestContext<KestevenStage, KestevenState> ctx, String factionId, float delta) {
        if (!ctx.has(KestevenFlag.ELIZA_ENDING_DONE)) return;
        if (factionId.equals(Ids.KESTEVEN_FACTION_ID)) cap(factionId, ELIZA_MAX_RELATION_KESTEVEN);
        if (factionId.equals(Factions.HEGEMONY) || factionId.equals(IRONSHELL_FACTION_ID)) cap(factionId, ELIZA_MAX_RELATION_HEGEMONY);
    }

    @Override
    protected void devInfo(QuestContext<KestevenStage, KestevenState> ctx, List<String> lines) {
        KestevenState s = ctx.state();
        lines.add("Kesteven ending open: " + kestevenEndingAvailable(ctx) + ", Eliza ending open: " + elizaEndingAvailable(ctx));
        lines.add("Tri-Tachyon: " + s.kestevenEndingTriTachyonRep + "; pirates " + s.commissionRepPirates + ", Kesteven " + s.commissionRepKesteven
                + ", Hegemony " + s.commissionRepHegemony + "; commission restore pending: " + ctx.has(KestevenFlag.COMMISSION_RESTORE_PENDING)
                + ", done: " + s.commissionRestored);
    }

    // Availability

    private static boolean kestevenEndingAvailable(QuestContext<KestevenStage, KestevenState> ctx) {
        return ctx.stage() == KestevenStage.CHIP_RECOVERED
                && !ctx.has(KestevenFlag.KESTEVEN_ENDING_DONE) && !ctx.has(KestevenFlag.CHIP_HANDED_TO_ELIZA);
    }

    private static boolean elizaEndingAvailable(QuestContext<KestevenStage, KestevenState> ctx) {
        return ctx.stage() == KestevenStage.CHIP_RECOVERED
                && !ctx.has(KestevenFlag.ELIZA_ENDING_DONE) && !ctx.has(KestevenFlag.ELIZA_KILLED)
                && ctx.has(KestevenFlag.ELIZA_HELPED) && ctx.has(KestevenFlag.CHIP_HANDED_TO_ELIZA)
                && ctx.has(KestevenFlag.ELIZA_RETURNED);
    }

    // The home market's entity, and Asteria Station while the home is Asteria. The old route compared the entity id
    // with the market id, which is the id of the market's primary entity (Asteria.addMarketplace, Outpost.addMarketplace).
    private static Set<SectorEntityToken> kestevenTargets() {
        Set<SectorEntityToken> targets = new LinkedHashSet<>();
        MarketAPI home = SectorLookup.asteriaOrOutpost();
        if (home == null) return targets;
        SectorEntityToken entity = Global.getSector().getEntityById(home.getId());
        if (entity != null) targets.add(entity);
        if (Ids.ASTERIA_ENTITY_ID.equals(home.getId())) {
            SectorEntityToken station = Global.getSector().getEntityById(Asteria.STATION_ENTITY_ID);
            if (station != null) targets.add(station);
        }
        return targets;
    }

    // Every ending unlocks the Throne's Gift start, the story skip and, for a campaign played on TRUE STARFARER from its
    // start, the Hellspawn start. The settings are per installation, not per campaign.
    static void unlockSettings() {
        SettingsManager.set(Setting.THRONES_GIFT_UNLOCKED, true);
        SettingsManager.set(Setting.STORY_SKIP_UNLOCKED, true);
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (Boolean.TRUE.equals(data.get(ModPlugin.STARFARER_MODE_FROM_START_KEY))) {
            SettingsManager.set(Setting.HELLSPAWN_UNLOCKED, true);
        }
    }

    // Kesteven ending

    // Everything of the old ending that no vanilla command does; the rows print the receipts.
    private static void kestevenEnding(QuestContext<KestevenStage, KestevenState> ctx) {
        unlockSettings();
        nskr_shipSwap.addPoints(EXCHANGE_POINTS);
        ctx.state().kestevenEndingTriTachyonRep = MathHelper.getSeededRandomNumberInRange(-0.70f, -0.65f, ctx.random(KestevenState.RANDOM_KESTEVEN_ENDING));
        PersonAPI jack = KestevenPeople.getJack();
        PersonAPI alice = KestevenPeople.getAlice();
        if (jack != null) jack.setImportance(PersonImportance.VERY_HIGH);
        if (alice != null) alice.setImportance(PersonImportance.VERY_HIGH);
        PersonAPI eliza = KestevenPeople.getEliza();
        if (eliza != null) eliza.getRelToPlayer().adjustRelationship(-0.75f, RepLevel.VENGEFUL);
        // The Cache core's return marker ends with its scope; Eliza's entity also carries plain flags of the Eliza search
        // and the hand-over, which the old ending cleared here.
        if (ctx.state().elizaMarket != null) ctx.state().elizaMarket.getMemory().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        MarketAPI market = targetMarket(ctx);
        if (market != null) market.addCondition(UnlimitedProductionChipCondition.ID);
    }

    // Eliza ending

    // The random draws keep the old order: the Hellspawn points, then the pirate, Kesteven and Hegemony values. The
    // three values stay in the state for the commission restore.
    private static void elizaEnding(QuestContext<KestevenStage, KestevenState> ctx) {
        KestevenState s = ctx.state();
        unlockSettings();
        String commission = Misc.getCommissionFactionId();
        if (commission != null) {
            if (commission.equals(Ids.KESTEVEN_FACTION_ID) || commission.equals(Factions.HEGEMONY)) ctx.set(KestevenFlag.COMMISSION_RESTORE_PENDING);
            if (ModPlugin.IS_IRONSHELL && commission.equals(IRONSHELL_FACTION_ID)) ctx.set(KestevenFlag.COMMISSION_RESTORE_PENDING);
        }
        // The Hellspawn event's factor text belongs to that event's Java UI, as in HellSpawnManager.
        if (GameModeManager.getMode() == GameModeManager.GameMode.HELLSPAWN) HellSpawnEventIntel.get().addFactor(
                new HellSpawnEventFactors(100 + MathHelper.getSeededRandomNumberInRange(3, 10, ctx.random(KestevenState.RANDOM_ELIZA_ENDING)),
                        "Gave the UPC to Eliza", "What could possibly go wrong?", ""));

        s.commissionRepPirates = MathHelper.getSeededRandomNumberInRange(0.25f, 0.30f, ctx.random(KestevenState.RANDOM_ELIZA_ENDING));
        if (relationship(Factions.PIRATES, Factions.PLAYER) < s.commissionRepPirates) setRelationship(Factions.PLAYER, Factions.PIRATES, s.commissionRepPirates);
        s.commissionRepKesteven = MathHelper.getSeededRandomNumberInRange(-0.90f, -0.80f, ctx.random(KestevenState.RANDOM_ELIZA_ENDING));
        s.commissionRepHegemony = MathHelper.getSeededRandomNumberInRange(-0.70f, -0.65f, ctx.random(KestevenState.RANDOM_ELIZA_ENDING));
        if (relationship(Factions.PIRATES, Ids.KESTEVEN_FACTION_ID) > s.commissionRepKesteven) setRelationship(Factions.PIRATES, Ids.KESTEVEN_FACTION_ID, s.commissionRepKesteven);
        if (relationship(Factions.PIRATES, Factions.HEGEMONY) > s.commissionRepHegemony) setRelationship(Factions.PIRATES, Factions.HEGEMONY, s.commissionRepHegemony);
        if (ModPlugin.IS_IRONSHELL && relationship(Factions.PIRATES, IRONSHELL_FACTION_ID) > s.commissionRepHegemony) {
            setRelationship(Factions.PIRATES, IRONSHELL_FACTION_ID, s.commissionRepHegemony);
        }

        // The Cache core's return marker ends with its scope; the hand-over set Eliza's entity's flag directly.
        if (s.elizaMarket != null) s.elizaMarket.getMemory().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);

        abandon(KestevenPeople.getAlice());
        abandon(KestevenPeople.getJack());

        MarketAPI market = targetMarket(ctx);
        if (market == null) return;
        market.addCondition(UnlimitedProductionChipCondition.ID);
        if (!market.hasIndustry(Industries.HEAVYINDUSTRY) && !market.hasIndustry(Industries.ORBITALWORKS)) {
            market.addIndustry(Industries.ORBITALWORKS);
        } else if (market.hasIndustry(Industries.HEAVYINDUSTRY)) {
            market.getIndustry(Industries.HEAVYINDUSTRY).startUpgrading();
            market.getIndustry(Industries.HEAVYINDUSTRY).finishBuildingOrUpgrading();
        }
        if (!market.hasIndustry(Industries.MILITARYBASE) && !market.hasIndustry(Industries.HIGHCOMMAND)) {
            if (market.hasIndustry(Industries.PATROLHQ)) {
                market.getIndustry(Industries.PATROLHQ).startUpgrading();
                market.getIndustry(Industries.PATROLHQ).finishBuildingOrUpgrading();
            } else market.addIndustry(Industries.MILITARYBASE);
        }
    }

    private static void abandon(PersonAPI person) {
        if (person == null) return;
        person.getRelToPlayer().adjustRelationship(-0.75f, RepLevel.VENGEFUL);
        ContactIntel contact = ContactIntel.getContactIntel(person);
        if (contact != null) contact.setState(ContactIntel.ContactState.SUSPENDED);
    }

    // The commission's own restore on ending raises relationships again; this puts back the values of the ending.
    private static void restoreCommissionRelationships(QuestContext<KestevenStage, KestevenState> ctx) {
        KestevenState s = ctx.state();
        if (relationship(Factions.PIRATES, Factions.PLAYER) <= s.commissionRepPirates) setRelationship(Factions.PLAYER, Factions.PIRATES, s.commissionRepPirates);
        if (relationship(Factions.PLAYER, Ids.KESTEVEN_FACTION_ID) >= s.commissionRepKesteven) setRelationship(Factions.PLAYER, Ids.KESTEVEN_FACTION_ID, s.commissionRepKesteven);
        if (relationship(Factions.PLAYER, Factions.HEGEMONY) >= s.commissionRepHegemony) setRelationship(Factions.PLAYER, Factions.HEGEMONY, s.commissionRepHegemony);
        if (ModPlugin.IS_IRONSHELL && relationship(Factions.PLAYER, IRONSHELL_FACTION_ID) >= s.commissionRepHegemony) {
            setRelationship(Factions.PLAYER, IRONSHELL_FACTION_ID, s.commissionRepHegemony);
        }
        s.commissionRestored = true;
        ctx.log("commission relationships restored");
    }

    // Helpers

    private static void cap(String factionId, float cap) {
        float max = cap;
        if (ModPlugin.IS_NEXERELIN) {
            float maxRel = 1f - DiplomacyManager.getManager().getMaxRelationship(factionId, Factions.PLAYER);
            max = Math.max(cap - maxRel, -1f);
        }
        if (Global.getSector().getPlayerFaction().getRelationship(factionId) > max) Global.getSector().getPlayerFaction().setRelationship(factionId, max);
    }

    private static MarketAPI targetMarket(QuestContext<KestevenStage, KestevenState> ctx) {
        return ctx.target() == null ? null : ctx.target().getMarket();
    }

    private static float relationship(String factionId, String otherId) {
        FactionAPI faction = Global.getSector().getFaction(factionId);
        return faction == null ? 0f : faction.getRelationship(otherId);
    }

    private static void setRelationship(String factionId, String otherId, float value) {
        FactionAPI faction = Global.getSector().getFaction(factionId);
        if (faction != null) faction.setRelationship(otherId, value);
    }
}
