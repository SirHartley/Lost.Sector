package lostsector.campaign.bounties;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.util.Pair;
import lostsector.helper.ShipHelper;
import lostsector.helper.fleet.FleetInfo;
import org.magiclib.util.MagicCampaign;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
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

    // Mothership: a Remnant fleet led by the Sunburst-class, guarding Helios and Polaris.
    static final String MOTHERSHIP_FLAGSHIP_VARIANT = "nskr_sunburst_boss";
    private static final String MOTHERSHIP_FLEET_NAME = "Project Helios Remnant";
    private static final String MOTHERSHIP_COMMANDER_NAME = "CREATOR-A3401#";
    private static final String MOTHERSHIP_FLAGSHIP_NAME = "TTDS Helios";
    private static final String MOTHERSHIP_FACTION = Factions.REMNANTS;
    private static final String MOTHERSHIP_PORTRAIT = "graphics/portraits/portrait_ai2.png";

    // Peacekeepers: a mercenary fleet flying as Independents, led by the Rorqual-class, patrolling Independent markets.
    static final String PEACEKEEPERS_PATROL_TEXT = "maintaining order";
    private static final String PEACEKEEPERS_FLEET_NAME = "Peacekeepers";
    private static final String PEACEKEEPERS_COMMANDER_NAME = "Alistair";
    private static final String PEACEKEEPERS_COMMANDER_LAST_NAME = "Walsh";
    private static final String PEACEKEEPERS_FLAGSHIP_VARIANT = "nskr_rorqual_boss";
    private static final String PEACEKEEPERS_CONQUEST_VARIANT = "conquest_Elite";
    private static final String PEACEKEEPERS_CHAMPION_VARIANT = "champion_Support";
    private static final int PEACEKEEPERS_CHAMPION_COUNT = 2;
    private static final String PEACEKEEPERS_FLAGSHIP_NAME = "ISS White Whale";
    private static final String PEACEKEEPERS_FACTION = Factions.MERCENARY;
    private static final String PEACEKEEPERS_PORTRAIT = "graphics/lostsector/portraits/nskr_pkGuy.png";
    private static final List<Pair<String, Float>> PEACEKEEPER_ROLES = new ArrayList<>();

    private static final String ASSIGNMENT_TEXT = "error #506, try again?";

    static {
        ABYSS_WEIGHTS.put(BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT, 4f);
        ABYSS_WEIGHTS.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 8f);
        ABYSS_WEIGHTS.put(BaseThemeGenerator.LocationType.JUMP_ORBIT, 8f);
        ETERNITY_WEIGHTS.put(BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT, 12f);
        ETERNITY_WEIGHTS.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 8f);
        ETERNITY_WEIGHTS.put(BaseThemeGenerator.LocationType.JUMP_ORBIT, 4f);
        PEACEKEEPER_ROLES.add(new Pair<>(ShipRoles.COMBAT_SMALL, 12f));
        PEACEKEEPER_ROLES.add(new Pair<>(ShipRoles.COMBAT_MEDIUM, 10f));
        PEACEKEEPER_ROLES.add(new Pair<>(ShipRoles.COMBAT_LARGE, 8f));
        PEACEKEEPER_ROLES.add(new Pair<>(ShipRoles.CARRIER_MEDIUM, 4f));
        PEACEKEEPER_ROLES.add(new Pair<>(ShipRoles.CARRIER_LARGE, 4f));
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

    // Mothership

    static SimpleFleet mothership(SectorEntityToken at, Random random) {
        float points = MathHelper.getSeededRandomNumberInRange(155f, 160f, random);
        points *= Difficulty.scriptedFleetMult();

        Map<String, Integer> skills = new HashMap<>();
        skills.put("combat_endurance", 2);
        skills.put(Skills.GUNNERY_IMPLANTS, 2);
        skills.put("field_modulation", 2);
        skills.put("target_analysis", 2);
        skills.put("systems_expertise", 2);
        skills.put("missile_specialization", 2);
        skills.put("energy_weapon_mastery", 2);
        skills.put("ordnance_expert", 2);
        if (Difficulty.isStarfarer()) skills.put(Skills.WOLFPACK_TACTICS, 1);
        skills.put(Skills.ELECTRONIC_WARFARE, 1);
        skills.put(Skills.CARRIER_GROUP, 1);
        skills.put(Skills.FIGHTER_UPLINK, 1);

        List<String> keys = memoryKeys();
        keys.add(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY);
        List<String> tags = new ArrayList<>();
        tags.add(Tags.SHIP_LIMITED_TOOLTIP);

        SimpleFleetMember flagship = new SimpleFleetMember(MOTHERSHIP_FLAGSHIP_VARIANT, tags, true);
        flagship.alwaysRecover = false;
        flagship.name = MOTHERSHIP_FLAGSHIP_NAME;

        SimpleCaptain captain = new SimpleCaptain("nskr_" + MOTHERSHIP_COMMANDER_NAME, MOTHERSHIP_FACTION, skills);
        captain.isAiCore = true;
        captain.aiCoreID = Commodities.ALPHA_CORE;
        captain.personality = Personalities.RECKLESS;
        captain.portraitSpritePath = MOTHERSHIP_PORTRAIT;
        captain.rankId = Ranks.SPACE_ADMIRAL;
        captain.firstName = MOTHERSHIP_COMMANDER_NAME;
        captain.gender = FullName.Gender.FEMALE;

        SimpleFleet fleet = new SimpleFleet(at, MOTHERSHIP_FACTION, points, keys, random);
        fleet.maxShipSize = 2;
        fleet.sMods = 1;
        fleet.name = MOTHERSHIP_FLEET_NAME;
        fleet.noFactionInName = true;
        fleet.commander = captain.create();
        fleet.flagshipInfo = flagship;
        fleet.assignment = FleetAssignment.ORBIT_PASSIVE;
        fleet.assignmentText = "error #446, try again?";
        return fleet;
    }

    static void finishMothership(CampaignFleetAPI fleet, Random random) {
        finish(fleet, MOTHERSHIP_FACTION, random);
    }

    // The TTDS Helios as a derelict next to the player, recoverable with a story point half the time and without its
    // limited tooltip.
    static CustomCampaignEntityAPI mothershipWreck(CampaignFleetAPI fleet, Random random) {
        ShipRecoverySpecial.PerShipData ship = new ShipRecoverySpecial.PerShipData(MOTHERSHIP_FLAGSHIP_VARIANT, ShipRecoverySpecial.ShipCondition.WRECKED, 0f);
        ship.shipName = MOTHERSHIP_FLAGSHIP_NAME;
        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(ship, false);
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();

        CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(
                fleet.getContainingLocation(), Entities.WRECK, Factions.NEUTRAL, params);
        entity.getLocation().x = pf.getLocation().x + (100f - random.nextFloat() * 200f);
        entity.getLocation().y = pf.getLocation().y + (100f - random.nextFloat() * 200f);

        ShipRecoverySpecial.ShipRecoverySpecialData data = new ShipRecoverySpecial.ShipRecoverySpecialData(null);
        data.storyPointRecovery = random.nextFloat() < 0.50f;
        data.notNowOptionExits = true;
        data.noDescriptionText = true;
        DerelictShipEntityPlugin plugin = (DerelictShipEntityPlugin) entity.getCustomPlugin();
        ShipRecoverySpecial.PerShipData copy = plugin.getData().ship.clone();
        copy.variant = Global.getSettings().getVariant(copy.variantId).clone();
        copy.variantId = null;
        copy.getVariant().removeTag(Tags.SHIP_LIMITED_TOOLTIP);
        data.addShip(copy);

        Misc.setSalvageSpecial(entity, data);
        entity.setDiscoverable(true);
        entity.setSensorProfile(100f);
        return entity;
    }

    // Peacekeepers

    static SimpleFleet peacekeepers(SectorEntityToken at, Random random) {
        float points = MathHelper.getSeededRandomNumberInRange(180f, 190f, random);
        points *= Difficulty.scriptedFleetMult();

        Map<String, Integer> skills = new HashMap<>();
        skills.put("combat_endurance", 2);
        skills.put("damage_control", 2);
        skills.put("field_modulation", 2);
        skills.put("target_analysis", 2);
        skills.put("ordnance_expert", 2);
        skills.put("missile_specialization", 2);
        skills.put("energy_weapon_mastery", 2);
        skills.put("impact_mitigation", 2);
        skills.put("electronic_warfare", 1);
        skills.put("crew_training", 1);

        List<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
        keys.add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        keys.add(MemFlags.MEMORY_KEY_LOW_REP_IMPACT);

        SimpleFleetMember flagship = new SimpleFleetMember(PEACEKEEPERS_FLAGSHIP_VARIANT, new ArrayList<String>(), true);
        flagship.alwaysRecover = true;
        flagship.name = PEACEKEEPERS_FLAGSHIP_NAME;

        List<SimpleFleetMember> secondaries = new ArrayList<>();
        SimpleFleetMember conquest = new SimpleFleetMember(PEACEKEEPERS_CONQUEST_VARIANT, new ArrayList<String>(), false);
        conquest.captain = peacekeeperCaptain(7, random);
        secondaries.add(conquest);
        for (int i = 0; i < PEACEKEEPERS_CHAMPION_COUNT; i++) {
            SimpleFleetMember champion = new SimpleFleetMember(PEACEKEEPERS_CHAMPION_VARIANT, new ArrayList<String>(), false);
            champion.captain = peacekeeperCaptain(6, random);
            secondaries.add(champion);
        }

        SimpleCaptain captain = new SimpleCaptain("nskr_" + PEACEKEEPERS_COMMANDER_NAME, PEACEKEEPERS_FACTION, skills);
        captain.personality = Personalities.RECKLESS;
        captain.portraitSpritePath = PEACEKEEPERS_PORTRAIT;
        captain.postId = Ranks.POST_FLEET_COMMANDER;
        captain.rankId = Ranks.SPACE_CAPTAIN;
        captain.firstName = PEACEKEEPERS_COMMANDER_NAME;
        captain.lastName = PEACEKEEPERS_COMMANDER_LAST_NAME;
        captain.gender = FullName.Gender.MALE;

        SimpleFleet fleet = new SimpleFleet(at, PEACEKEEPERS_FACTION, points, keys, random);
        fleet.type = FleetTypes.PATROL_LARGE;
        fleet.maxShipSize = 4;
        fleet.sMods = 2;
        fleet.ignoreMarketFleetSizeMult = true;
        fleet.commander = captain.create();
        fleet.flagshipInfo = flagship;
        fleet.secondaries = secondaries;
        fleet.name = PEACEKEEPERS_FLEET_NAME;
        fleet.noFactionInName = true;
        fleet.assignment = FleetAssignment.PATROL_SYSTEM;
        fleet.assignmentText = PEACEKEEPERS_PATROL_TEXT;
        return fleet;
    }

    // Built as mercenaries, the fleet flies as Independents, with only its flagship a Rorqual.
    static void finishPeacekeepers(CampaignFleetAPI fleet, Random random) {
        fleet.setFaction(Factions.INDEPENDENT, false);
        fixRorqualCount(fleet, random);
        FleetHelper.update(fleet, random);
    }

    // The Rorqual flagship is the bounty: the Peacekeepers are beaten once it has left the fleet.
    static boolean hasRorqual(FleetInfo info) {
        return FleetHelper.getOriginalFlagship(info) != null;
    }

    // Adds ships of random roles, with S-mods and often a captain, until the fleet is back at its spawn strength.
    static void reinforcePeacekeepers(FleetInfo info, Random random) {
        CampaignFleetAPI fleet = info.fleet;
        while (fleet.getFleetPoints() < (int) info.strength) {
            FleetMemberAPI member = addToFleet(randomRole(random), random, fleet);
            if (!member.isFighterWing()) {
                addSmods(member, MathHelper.getSeededRandomNumberInRange(1, 3, random), fleet.getCommanderStats());
                if (random.nextFloat() < 0.67f) member.setCaptain(peacekeeperCaptain(7, random));
            }
            for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
                m.getVariant().addTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
                m.getVariant().addTag(Tags.VARIANT_ALWAYS_RETAIN_SMODS_ON_SALVAGE);
                m.getRepairTracker().setCR(m.getRepairTracker().getMaxCR());
            }
            FleetHelper.update(fleet, random);
            log("reinforced " + fleet.getName() + " with " + member.getHullSpec().getBaseHullId());
        }
    }

    // A Rorqual can appear among random mercenary ships; any but the flagship becomes a Champion.
    private static void fixRorqualCount(CampaignFleetAPI fleet, Random random) {
        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
            if (m.getCaptain().getId().equals("nskr_pkguy") || m.isFlagship()) continue;
            if (!m.getHullSpec().getBaseHullId().equals("nskr_rorqual")) continue;
            ShipVariantAPI variant = Global.getSettings().getVariant(PEACEKEEPERS_CHAMPION_VARIANT).clone();
            m.setVariant(variant, true, true);
            FleetHelper.update(fleet, random);
            log("fixed extra rorqual spawn");
        }
    }

    private static PersonAPI peacekeeperCaptain(int maxLevel, Random random) {
        PersonAPI base = Global.getSector().getFaction(PEACEKEEPERS_FACTION).createRandomPerson(FullName.Gender.ANY, random);

        int level = MathHelper.getSeededRandomNumberInRange(maxLevel - 2, maxLevel, random);
        Map<String, Integer> skills = ShipHelper.createRandomSkills(level, 0.67f, random);

        PersonAPI captain = MagicCampaign.createCaptainBuilder(base.getFaction().getId()).create();
        captain.setId("nskr_" + base.getId());
        captain.setPersonality(base.getPersonalityAPI().getId());
        captain.setPortraitSprite(base.getPortraitSprite());
        captain.setPostId(base.getPostId());
        captain.setRankId(base.getRankId());
        captain.setName(new FullName(base.getName().getFirst(), base.getName().getLast(), base.getGender()));
        ShipHelper.setOfficerSkills(captain, skills);
        return captain;
    }

    private static FleetMemberAPI addSmods(FleetMemberAPI member, int count, MutableCharacterStatsAPI stats){
        if (member.isFighterWing()) return null;
        ShipVariantAPI v = member.getVariant().clone();
        Collection<String> validHmods = validHullmods(v.getNonBuiltInHullmods(), v.getSMods(), v.getPermaMods());
        int vCount = count;
        if (validHmods.size()<count) vCount = validHmods.size();
        //swapping existing to smod
        while(v.getSMods().size()<vCount){
            String mod = null;
            for (String m : validHullmods(v.getNonBuiltInHullmods(), v.getSMods(), v.getPermaMods())){
                mod = m;
                if (mod.equals("heavyarmor")){
                    break;
                }
                if (mod.equals("missleracks")){
                    break;
                }
                if (mod.equals("targetingunit")){
                    break;
                }
                if (mod.equals("hardenedshieldemitter")){
                    break;
                }
                if (mod.equals("eccm")){
                    break;
                }
                if (mod.equals("unstable_injector")){
                    break;
                }
                if (mod.equals("fluxdistributor")){
                    break;
                }
                if (mod.equals("fluxcoil")){
                    break;
                }
            }
            v.addPermaMod(mod, true);
            log(" added "+mod+" to "+member.getHullSpec().getBaseHullId());
            if (mod==null)log(" ERROR "+mod+" hullmod "+member.getHullSpec().getBaseHullId());
        }
        //adding entirely new smods
        while(v.getSMods().size()<count){
            Collection<String> vm = v.getHullMods();
            if(!vm.contains("heavyarmor") && v.getSMods().size()<count){
                v.addPermaMod("heavyarmor", true);
            }
            if(!vm.contains("hardenedshieldemitter") && v.getSMods().size()<count){
                v.addPermaMod("hardenedshieldemitter", true);
            }
            if(!vm.contains("targetingunit") && !vm.contains("dedicated_targeting_core") && v.getSMods().size()<count){
                v.addPermaMod("targetingunit", true);
            }
            if(!vm.contains("fluxdistributor") && v.getSMods().size()<count){
                v.addPermaMod("fluxdistributor", true);
            }
            if(!vm.contains("fluxcoil") && v.getSMods().size()<count){
                v.addPermaMod("fluxcoil", true);
            }
            if(!vm.contains("reinforcedhull") && v.getSMods().size()<count){
                v.addPermaMod("reinforcedhull", true);
            }
        }

        //spending unused op
        int unusedOP = v.getUnusedOP(stats);
        if (unusedOP>0) {
            int maxVentsOrCaps = 0;
            if (v.getHullSize() == ShipAPI.HullSize.FRIGATE) maxVentsOrCaps = 10;
            if (v.getHullSize() == ShipAPI.HullSize.DESTROYER) maxVentsOrCaps = 20;
            if (v.getHullSize() == ShipAPI.HullSize.CRUISER) maxVentsOrCaps = 30;
            if (v.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) maxVentsOrCaps = 50;
            if (v.getNumFluxVents() < maxVentsOrCaps) {
                int ventsToAdd = maxVentsOrCaps-v.getNumFluxVents();
                if (ventsToAdd<=unusedOP){
                    v.setNumFluxVents(maxVentsOrCaps);
                    unusedOP-=ventsToAdd;
                    log(" added "+maxVentsOrCaps);
                } else {
                    v.setNumFluxVents(unusedOP+v.getNumFluxVents());
                    unusedOP-=unusedOP;
                    log(" added "+unusedOP);
                }
                log(" max vents " + maxVentsOrCaps + " for " + member.getHullSpec().getBaseHullId()+" total "+v.getNumFluxVents());
                log(" leftover "+unusedOP);
            }
            if (v.getNumFluxCapacitors() < maxVentsOrCaps) {
                int capsToAdd = maxVentsOrCaps-v.getNumFluxCapacitors();
                if (capsToAdd<=unusedOP){
                    v.setNumFluxCapacitors(maxVentsOrCaps);
                    unusedOP-=capsToAdd;
                    log(" added "+maxVentsOrCaps);
                } else {
                    v.setNumFluxCapacitors(unusedOP+v.getNumFluxCapacitors());
                    unusedOP-=unusedOP;
                    log(" added "+unusedOP);
                }
                log(" max caps " + maxVentsOrCaps + " for " + member.getHullSpec().getBaseHullId()+" total "+v.getNumFluxCapacitors());
                log(" leftover "+unusedOP);
            }
        }
        //set variant
        member.setVariant(v, false, true);

        return member;
    }
    private static Collection<String> validHullmods(Collection<String> hullmods, LinkedHashSet<String> smods, Set<String> permamods){
        Collection<String> valid = new ArrayList<>();
        for (String m : hullmods){
            if (smods.contains(m)) continue;
            if (permamods.contains(m)) continue;
            if (m.equals("safetyoverrides")) continue;
            if (m.equals("phase_anchor")) continue;
            valid.add(m);
        }
        return valid;
    }
    private static FleetMemberAPI addToFleet(String role, Random random, CampaignFleetAPI fleet) {
        FleetMemberAPI member = null;
        FactionAPI.ShipPickParams params = new FactionAPI.ShipPickParams(FactionAPI.ShipPickMode.PRIORITY_THEN_ALL);
        List<ShipRolePick> picks = fleet.getFaction().pickShip(role, params, null, random);
        for (ShipRolePick pick : picks) {
            member = addToFleet(pick, fleet, random);
        }
        return member;
    }
    private static FleetMemberAPI addToFleet(ShipRolePick pick, CampaignFleetAPI fleet, Random random) {
        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, pick.variantId);
        String name = fleet.getFleetData().pickShipName(member, random);
        member.setShipName(name);
        fleet.getFleetData().addFleetMember(member);
        return member;
    }

    private static String randomRole(Random random) {
        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
        for (Pair<String, Float> role : PEACEKEEPER_ROLES) {
            picker.add(role.one, role.two);
        }
        return picker.pick(random);
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
