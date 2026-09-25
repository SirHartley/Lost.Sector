package lostsector.quest.dev;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

// What vanilla fires and handles, so mod rows that join vanilla triggers are not reported. Two sources:
// ENGINE lists the triggers the 0.98a-RC8 game code fires with a literal name; the index lists what vanilla
// rules.csv rows use and fire, generated from the vanilla file by "RulesCheck --index" (README "Rules check tool").
final class VanillaRules {

    static final String INDEX = "jars/src/lostsector/quest/dev/vanilla-rules-index.txt";

    // Every call in the 0.98a-RC8 sources that fires or opens a trigger given as a string literal: FireBest.fire,
    // FireAll.fire, the dialog plugins' fireBest/fireAll, getBestMatching and RuleBasedInteractionDialogPluginImpl.
    // Found by searching sources-api/*.java and sources-obf/*.java of the starsector-knowledge skill; references
    // are bundle file and line. Triggers fired from a variable (defeat triggers, raid triggers, "<creator>OfferDesc")
    // are vanilla-specific and covered by the index, because vanilla rows use them.
    record EngineTrigger(String trigger, boolean fireAll, String source) {
    }

    static final List<EngineTrigger> ENGINE = List.of(
            new EngineTrigger("OpenInteractionDialog", false, "impl.campaign.java:20289 RuleBasedInteractionDialogPluginImpl() default initial trigger"),
            new EngineTrigger("DialogOptionSelected", false, "impl.campaign.java:20409 RuleBasedInteractionDialogPluginImpl.optionSelected"),
            new EngineTrigger("NewGameOptionSelected", false, "impl.campaign.java:19227 NewGameDialogPluginImpl"),
            new EngineTrigger("BeginNewGameCreation", false, "impl.campaign.java:19243 NewGameDialogPluginImpl"),
            new EngineTrigger("OpenCDE", false, "ui.newui.java:3988 O0Oo (comm directory entry)"),
            new EngineTrigger("TradePanelFlavorText", false, "campaign.ui.java:5462"),
            new EngineTrigger("BeginFleetEncounter", false, "impl.campaign.java:12886, 12906, 12997 FleetInteractionDialogPluginImpl"),
            new EngineTrigger("BeginFleetEncounter2", false, "impl.campaign.java:12904, 12995 FleetInteractionDialogPluginImpl"),
            new EngineTrigger("OngoingBattleEncounter", false, "impl.campaign.java:13001 FleetInteractionDialogPluginImpl"),
            new EngineTrigger("OpenCommLink", false, "impl.campaign.java:13843 FleetInteractionDialogPluginImpl"),
            new EngineTrigger("MarketPostOpen", true, "impl.campaign.java:257 AbandonMarketPluginImpl, 166180 MarketCMD (FireAll); 154093 EndConversation (FireBest)"),
            new EngineTrigger("PopulateOptions", true, "impl.campaign.java:261 AbandonMarketPluginImpl, 126015 BaseMissionHub, 154095 EndConversation, 158295 OpenCoreTab, 166196 MarketCMD"),
            new EngineTrigger("FireAllIntercept", false, "impl.campaign.java:154165 FireAll"),
            new EngineTrigger("AddBarEvents", true, "impl.campaign.java:161515 BarCMD"),
            new EngineTrigger("ReturnFromBar", false, "impl.campaign.java:161708 BarCMD"),
            new EngineTrigger("BarEventFinished", false, "impl.campaign.java:73212 BarEventDialogPlugin"),
            new EngineTrigger("BarEventFinishedNoContinue", false, "impl.campaign.java:73214 BarEventDialogPlugin"),
            new EngineTrigger("HistorianBackstoryBlurb", false, "impl.campaign.java:79256 HistorianBarEvent"),
            new EngineTrigger("AddMHCloseOption", false, "impl.campaign.java:126050 BaseMissionHub"),
            new EngineTrigger("MHPostMissionListText", false, "impl.campaign.java:126057 BaseMissionHub"),
            new EngineTrigger("SOEDuelFinished", false, "impl.campaign.java:41907 DuelDialogDelegate"),
            new EngineTrigger("SOETutorialFinished", false, "impl.campaign.java:41909 DuelDialogDelegate"),
            new EngineTrigger("FoodShortageEndedByPlayerSale", false, "impl.campaign.java:44373 FoodShortageEvent"),
            new EngineTrigger("CPCBlueprintsPicked", false, "impl.campaign.java:109694 CustomProductionContract"),
            new EngineTrigger("SubstrateWeaponsPicked", false, "impl.campaign.java:153843 DwellerCMD"),
            new EngineTrigger("SalvageSpecialFinished", false, "impl.campaign.java:169179 SalvageSpecialInteraction"),
            new EngineTrigger("SalvageSpecialFinishedNoContinue", false, "impl.campaign.java:161456 BarCMD, 169181 SalvageSpecialInteraction"),
            new EngineTrigger("AICoresTurnedIn", false, "impl.campaign.java:162737 AICores, 163550 DemandCargo"),
            new EngineTrigger("CargoPodsOptions", true, "impl.campaign.java:163365 CargoPods"),
            new EngineTrigger("CargoPodsOptionsUpdate", true, "impl.campaign.java:163366 CargoPods"),
            new EngineTrigger("PostGroundRaid", false, "impl.campaign.java:165541 MarketCMD"),
            new EngineTrigger("BeatDefendersContinue", false, "impl.campaign.java:165651 MarketCMD, 167602 SalvageDefenderInteraction"),
            new EngineTrigger("WormholeDeploymentFinished", false, "impl.campaign.java:167215 Objectives"),
            new EngineTrigger("PerformSalvage", false, "impl.campaign.java:167938 SalvageEntity"),
            new EngineTrigger("ZGRItemsTurnedIn", false, "impl.campaign.java:169364 ZGRTurnIn"),
            new EngineTrigger("ShipRecoveryCustomText", false, "impl.campaign.java:171103 ShipRecoverySpecial"),
            new EngineTrigger("PostShipRecoverySpecial", true, "impl.campaign.java:171441 ShipRecoverySpecial"),
            new EngineTrigger("AfterZigguratDefeat", false, "impl.campaign.java:207764 TTBlackSite dialog"),
            new EngineTrigger("ShroudedSubstrateRightClick", false, "campaign.impl.java:1994 ShroudedSubstratePlugin dialog"),
            new EngineTrigger("RemoveAICoreAdmin", false, "campaign.command.java:634 AdminPickerDialog dialog"),
            new EngineTrigger("OutpostEstablished", false, "campaign.ui.java:20247 PlanetSurveyPanel dialog"));

    // Hub missions fire "<missionId>_blurb" and "_option" (BaseMissionHub, impl.campaign.java:126026-126045),
    // "_blurbBar" and "_optionBar" (HubMissionBarEventWrapper 126407, HubMissionWithBarEvent 126490) and
    // "_startBar" (HubMissionWithBarEvent 126474). The mission id is not known here, so the suffix is accepted.
    static final List<String> ENGINE_SUFFIXES = List.of("_blurb", "_option", "_blurbBar", "_optionBar", "_startBar");

    // ruleCommandPackages in starsector-core data/config/settings.json (skill data/config.txt:3924).
    static final List<String> COMMAND_PACKAGES = List.of(
            "com.fs.starfarer.api.impl.campaign.rulecmd",
            "com.fs.starfarer.api.impl.campaign.rulecmd.salvage",
            "com.fs.starfarer.api.impl.campaign.rulecmd.newgame",
            "com.fs.starfarer.api.impl.campaign.rulecmd.missions",
            "com.fs.starfarer.api.impl.campaign.rulecmd.academy");

    // Triggers whose rows option clicks select; PaginatedOptions fires one or the other (impl.campaign.java:158610).
    static final Set<String> OPTION_TRIGGERS = Set.of("DialogOptionSelected", "NewGameOptionSelected");

    private static final String[] SECTIONS = {"triggers", "fireAll", "fireBest", "options", "keys"};

    // triggers: triggers of vanilla rows. fireAll, fireBest: literal targets of those commands in vanilla rows.
    // options: option ids with a vanilla "$option == <id>" handler. keys: memory keys in Conditions and Script.
    final Map<String, Set<String>> sets = new TreeMap<>();

    private VanillaRules() {
        for (String section : SECTIONS) {
            sets.put(section, new TreeSet<>());
        }
    }

    Set<String> triggers() {
        return sets.get("triggers");
    }

    Set<String> fireAll() {
        return sets.get("fireAll");
    }

    Set<String> fireBest() {
        return sets.get("fireBest");
    }

    Set<String> options() {
        return sets.get("options");
    }

    Set<String> keys() {
        return sets.get("keys");
    }

    static boolean engineFires(String trigger) {
        for (EngineTrigger engine : ENGINE) {
            if (engine.trigger.equals(trigger)) return true;
        }
        for (String suffix : ENGINE_SUFFIXES) {
            if (trigger.endsWith(suffix) && trigger.length() > suffix.length()) return true;
        }
        return false;
    }

    static boolean engineFiresAll(String trigger) {
        for (EngineTrigger engine : ENGINE) {
            if (engine.fireAll && engine.trigger.equals(trigger)) return true;
        }
        return false;
    }

    static VanillaRules fromIndex(Path index) throws IOException {
        VanillaRules rules = new VanillaRules();
        Set<String> section = null;
        for (String line : Files.readAllLines(index, StandardCharsets.UTF_8)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.startsWith("[") && line.endsWith("]")) {
                section = rules.sets.get(line.substring(1, line.length() - 1));
                if (section == null) throw new IOException("unknown section " + line + " in " + index);
            } else if (section == null) {
                throw new IOException("entry before the first section in " + index);
            } else {
                section.add(line);
            }
        }
        return rules;
    }

    static VanillaRules fromCsv(Path csv) throws IOException {
        RulesFile file = RulesFile.read(csv);
        if (file.error != null) throw new IOException(csv + ": " + file.error);
        VanillaRules rules = new VanillaRules();
        for (RulesFile.Record record : file.records.subList(1, file.records.size())) {
            if (record.fields().size() != RulesFile.HEADER.size()) continue;
            RulesFile.Row row = RulesFile.Row.of(record);
            if (!row.isLoaded()) continue;
            rules.triggers().add(row.trigger());
            for (String line : RulesFile.lines(row.conditions())) {
                RuleExpression e = RuleExpression.parse(line);
                rules.addUses(e);
                if (OPTION_TRIGGERS.contains(row.trigger()) && isOptionTest(e)) {
                    rules.options().add(e.second.text());
                }
            }
            for (String line : RulesFile.lines(row.script())) {
                rules.addUses(RuleExpression.parse(line));
            }
        }
        return rules;
    }

    static boolean isOptionTest(RuleExpression e) {
        return e.error == null && e.operator == RuleExpression.Operator.EQUAL && e.first != null
                && e.first.text().equals("$option") && e.second != null && !e.second.isVariable();
    }

    private void addUses(RuleExpression e) {
        if (e.error != null) return;
        if ((e.isCommand("FireAll") || e.isCommand("FireBest")) && !e.params.isEmpty() && !e.params.get(0).isVariable()) {
            (e.isCommand("FireAll") ? fireAll() : fireBest()).add(e.params.get(0).text());
        }
        for (RuleExpression.Token token : tokens(e)) {
            if (token.isVariable()) keys().add(token.key());
        }
    }

    static List<RuleExpression.Token> tokens(RuleExpression e) {
        List<RuleExpression.Token> tokens = new java.util.ArrayList<>(e.params);
        if (e.first != null) tokens.add(e.first);
        if (e.second != null) tokens.add(e.second);
        return tokens;
    }

    void write(PrintStream out, String gameVersion) {
        out.println("# Vanilla rules index for lostsector.quest.dev.RulesCheck.");
        out.println("# Game version " + gameVersion + ". Generated from starsector-core/data/campaign/rules.csv with");
        out.println("#   java -cp \"<build output>:<compile jars>\" lostsector.quest.dev.RulesCheck --index <vanilla rules.csv> " + gameVersion);
        out.println("# Regenerate it for a new game version; do not edit it by hand.");
        for (String section : SECTIONS) {
            out.println();
            out.println("[" + section + "]");
            for (String entry : sets.get(section)) {
                if (!entry.isBlank()) out.println(entry);
            }
        }
    }
}
