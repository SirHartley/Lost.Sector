package lostsector.campaign.bounties;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import lostsector.helper.FleetHelper;
import lostsector.helper.MathHelper;
import lostsector.helper.StringHelper;
import lostsector.helper.SystemHelper;
import lostsector.helper.fleet.SimpleCaptain;
import lostsector.helper.fleet.SimpleFleet;
import lostsector.helper.fleet.SimpleFleetMember;
import lostsector.helper.fleet.SystemPicker;
import lostsector.settings.Difficulty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

// Locations and fleet builders for the bounties of BountiesQuest; each builder configures a SimpleFleet at the
// bounty's location and returns it unbuilt.
final class BountiesFleets {

    // Abyss: a Remnant group led by the Hollow-class, patrolling a red giant system.
    static final String ABYSS_FLAGSHIP_HULL = "nskr_reverie_boss";
    static final String ABYSS_CHASM_HULL = "nskr_harbinger_boss";
    static final String ABYSS_FISSURE_HULL = "nskr_afflictor_boss";
    private static final String ABYSS_FLEET_NAME = "Void Group";
    private static final String ABYSS_COMMANDER_NAME = "Lucius";
    private static final String ABYSS_FLAGSHIP_VARIANT = "nskr_reverie_boss_std";
    private static final String ABYSS_CHASM_VARIANT = "nskr_harbinger_boss_std";
    private static final String ABYSS_FISSURE_VARIANT = "nskr_afflictor_boss_std";
    private static final int ABYSS_FISSURE_COUNT = 2;
    private static final String ABYSS_FLAGSHIP_NAME = "Piercing Darkness";
    private static final String ABYSS_FACTION = "remnant";
    private static final String ABYSS_PORTRAIT = "graphics/lostsector/portraits/nskr_lucius.png";
    private static final LinkedHashMap<BaseThemeGenerator.LocationType, Float> ABYSS_WEIGHTS = new LinkedHashMap<>();

    // Eternity: an Enigma fleet led by the Eternity-class, orbiting in a nebula system.
    static final String ETERNITY_HULL = "nskr_eternity_e";
    private static final String ETERNITY_FLEET_NAME = "Commander Umbra's Fleet";
    private static final String ETERNITY_COMMANDER_NAME = "Umbra";
    private static final String ETERNITY_FLAGSHIP_VARIANT = "nskr_eternity_e_boss";
    private static final String ETERNITY_FLAGSHIP_NAME = "DSRD Shadows Of Tomorrow";
    private static final String ETERNITY_FACTION = "enigma";
    private static final String ETERNITY_PORTRAIT = "graphics/lostsector/portraits/nskr_enigma.png";
    private static final LinkedHashMap<BaseThemeGenerator.LocationType, Float> ETERNITY_WEIGHTS = new LinkedHashMap<>();

    private static final String ASSIGNMENT_TEXT = "error #506, try again?";

    static {
        ABYSS_WEIGHTS.put(BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT, 4f);
        ABYSS_WEIGHTS.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 8f);
        ABYSS_WEIGHTS.put(BaseThemeGenerator.LocationType.JUMP_ORBIT, 8f);
        ETERNITY_WEIGHTS.put(BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT, 12f);
        ETERNITY_WEIGHTS.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 8f);
        ETERNITY_WEIGHTS.put(BaseThemeGenerator.LocationType.JUMP_ORBIT, 4f);
    }

    private BountiesFleets() {
    }

    // Abyss

    // A red giant with a Remnant theme, else any procgen red giant, else any non-core system.
    static SectorEntityToken abyssLocation(Random random) {
        return location(ABYSS_WEIGHTS, random, () -> {
            SystemPicker picker = new SystemPicker(random, 1);
            picker.pickStars.add(StarTypes.RED_GIANT);
            picker.pickTags.add(Tags.THEME_REMNANT);
            picker.enforceSystemStarType = true;
            picker.pickOnlyInProcgen = true;
            return pick(picker, random, () -> picker.pickTags = new ArrayList<>());
        });
    }

    static SimpleFleet abyss(SectorEntityToken at, Random random) {
        float points = MathHelper.getSeededRandomNumberInRange(135f, 145f, random);
        points *= Difficulty.scriptedFleetMult();

        Map<String, Integer> skills = new HashMap<>();
        skills.put("combat_endurance", 2);
        skills.put("damage_control", 2);
        skills.put("field_modulation", 2);
        skills.put("target_analysis", 2);
        skills.put("systems_expertise", 2);
        skills.put("missile_specialization", 2);
        skills.put("energy_weapon_mastery", 2);
        skills.put("impact_mitigation", 2);
        skills.put("electronic_warfare", 1);
        skills.put("crew_training", 1);
        if (Difficulty.isStarfarer()) skills.put("wolfpack_tactics", 1);

        List<String> tags = new ArrayList<>();
        tags.add(Tags.TAG_AUTOMATED_NO_PENALTY);
        tags.add(Tags.VARIANT_UNRESTORABLE);
        tags.add(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
        tags.add(Tags.SHIP_LIMITED_TOOLTIP);
        List<String> permaMods = new ArrayList<>();
        permaMods.add(HullMods.AUTOMATED);

        SimpleFleetMember flagship = new SimpleFleetMember(ABYSS_FLAGSHIP_VARIANT, tags, true);
        flagship.alwaysRecover = true;
        flagship.hullmods = permaMods;
        flagship.name = ABYSS_FLAGSHIP_NAME;

        List<SimpleFleetMember> secondaries = new ArrayList<>();
        AICoreOfficerPlugin core = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
        SimpleFleetMember chasm = new SimpleFleetMember(ABYSS_CHASM_VARIANT, tags, true);
        chasm.hullmods = permaMods;
        chasm.captain = core.createPerson(Commodities.ALPHA_CORE, ABYSS_FACTION, random);
        secondaries.add(chasm);
        for (int i = 0; i < ABYSS_FISSURE_COUNT; i++) {
            SimpleFleetMember fissure = new SimpleFleetMember(ABYSS_FISSURE_VARIANT, tags, true);
            fissure.hullmods = permaMods;
            fissure.captain = core.createPerson(Commodities.ALPHA_CORE, ABYSS_FACTION, random);
            secondaries.add(fissure);
        }

        SimpleCaptain captain = new SimpleCaptain("nskr_" + ABYSS_COMMANDER_NAME, ABYSS_FACTION, skills);
        captain.isAiCore = true;
        captain.aiCoreID = Commodities.ALPHA_CORE;
        captain.personality = Personalities.RECKLESS;
        captain.portraitSpritePath = ABYSS_PORTRAIT;
        captain.rankId = Ranks.UNKNOWN;
        captain.firstName = ABYSS_COMMANDER_NAME;
        captain.gender = FullName.Gender.FEMALE;

        SimpleFleet fleet = new SimpleFleet(at, ABYSS_FACTION, points, memoryKeys(), random);
        fleet.maxShipSize = 3;
        fleet.sMods = 3;
        fleet.ignoreMarketFleetSizeMult = true;
        fleet.name = ABYSS_FLEET_NAME + " " + StringHelper.getRandomGreekLetter(random, true);
        fleet.noFactionInName = true;
        fleet.commander = captain.create();
        fleet.flagshipInfo = flagship;
        fleet.secondaries = secondaries;
        fleet.assignment = FleetAssignment.PATROL_SYSTEM;
        fleet.assignmentText = ASSIGNMENT_TEXT;
        fleet.aiFleetProperties = true;
        return fleet;
    }

    static void finishAbyss(CampaignFleetAPI fleet, Random random) {
        finish(fleet, ABYSS_FACTION, random);
    }

    // The Anti-Remnant Organization pays only while none of these hulls is in the fleet.
    static boolean hasAbyssShips(CampaignFleetAPI fleet) {
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListWithFightersCopy()) {
            String hull = member.getHullSpec().getBaseHullId();
            if (hull.equals(ABYSS_FLAGSHIP_HULL) || hull.equals(ABYSS_CHASM_HULL) || hull.equals(ABYSS_FISSURE_HULL)) return true;
        }
        return false;
    }

    // Eternity

    // A procgen nebula without a Remnant theme, else any procgen nebula, else any non-core system.
    static SectorEntityToken eternityLocation(Random random) {
        return location(ETERNITY_WEIGHTS, random, () -> {
            SystemPicker picker = new SystemPicker(random, 1);
            picker.pickSystemTypes.add(StarSystemGenerator.StarSystemType.NEBULA);
            picker.blacklistTags.add(Tags.THEME_REMNANT);
            picker.enforceSystemStarType = true;
            picker.pickOnlyInProcgen = true;
            return pick(picker, random, () -> picker.blacklistTags = new ArrayList<>());
        });
    }

    static SimpleFleet eternity(SectorEntityToken at, Random random) {
        float points = MathHelper.getSeededRandomNumberInRange(80f, 85f, random);
        points *= Difficulty.scriptedFleetMult();

        Map<String, Integer> skills = new HashMap<>();
        skills.put("combat_endurance", 2);
        skills.put("damage_control", 2);
        skills.put("field_modulation", 2);
        skills.put("target_analysis", 2);
        skills.put("systems_expertise", 2);
        skills.put("missile_specialization", 2);
        skills.put("energy_weapon_mastery", 2);
        skills.put("ordnance_expert", 2);
        skills.put("electronic_warfare", 1);
        skills.put("wolfpack_tactics", 1);
        skills.put("crew_training", 1);

        List<String> tags = new ArrayList<>();
        tags.add(Tags.SHIP_LIMITED_TOOLTIP);

        SimpleFleetMember flagship = new SimpleFleetMember(ETERNITY_FLAGSHIP_VARIANT, tags, true);
        flagship.alwaysRecover = true;
        flagship.name = ETERNITY_FLAGSHIP_NAME;

        SimpleCaptain captain = new SimpleCaptain("nskr_" + ETERNITY_COMMANDER_NAME, ETERNITY_FACTION, skills);
        captain.isAiCore = true;
        captain.aiCoreID = Commodities.ALPHA_CORE;
        captain.personality = Personalities.RECKLESS;
        captain.portraitSpritePath = ETERNITY_PORTRAIT;
        captain.rankId = Ranks.SPACE_ADMIRAL;
        captain.firstName = ETERNITY_COMMANDER_NAME;
        captain.gender = FullName.Gender.MALE;

        SimpleFleet fleet = new SimpleFleet(at, ETERNITY_FACTION, points, memoryKeys(), random);
        fleet.maxShipSize = 2;
        fleet.sMods = 3;
        fleet.name = ETERNITY_FLEET_NAME;
        fleet.commander = captain.create();
        fleet.flagshipInfo = flagship;
        fleet.assignment = FleetAssignment.ORBIT_PASSIVE;
        fleet.assignmentText = ASSIGNMENT_TEXT;
        fleet.aiFleetProperties = true;
        return fleet;
    }

    static void finishEternity(CampaignFleetAPI fleet, Random random) {
        finish(fleet, ETERNITY_FACTION, random);
    }

    // Shared

    private static List<String> memoryKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE);
        keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
        keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
        keys.add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        return keys;
    }

    private static void finish(CampaignFleetAPI fleet, String faction, Random random) {
        SystemHelper.spawnAwayFromStarFixer(fleet, 2.0f);
        fleet.setFaction(faction, true);
        FleetHelper.update(fleet, random);
    }

    // The picker's systems, then again after relaxing it, then any non-core system.
    private static StarSystemAPI pick(SystemPicker picker, Random random, Runnable relax) {
        if (!picker.get().isEmpty()) return picker.pick();
        log("no system matches, relaxing the bounty system rules");
        relax.run();
        if (!picker.get().isEmpty()) return picker.pick();
        log("no system matches, picking any non-core system");
        return SystemHelper.getRandomNonCoreSystem(random);
    }

    // The focus of a random orbit in a picked system, or the system's star when the pick has no orbit focus. A system
    // without either, such as a nebula without a star, is dropped and another one picked; null when no system is left.
    private static SectorEntityToken location(LinkedHashMap<BaseThemeGenerator.LocationType, Float> weights, Random random,
                                              Supplier<StarSystemAPI> systems) {
        SectorEntityToken location = null;
        while (location == null) {
            StarSystemAPI system = systems.get();
            if (system == null) return null;
            WeightedRandomPicker<BaseThemeGenerator.EntityLocation> points = BaseThemeGenerator.getLocations(random, system, 50f, weights);
            BaseThemeGenerator.EntityLocation point = points.pick(random);
            location = point != null && point.orbit != null && point.orbit.getFocus() != null ? point.orbit.getFocus() : system.getStar();
        }
        return location;
    }

    private static void log(String message) {
        Global.getLogger(BountiesFleets.class).info(message);
    }
}
