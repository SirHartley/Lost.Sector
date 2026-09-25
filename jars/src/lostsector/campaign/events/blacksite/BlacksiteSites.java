package lostsector.campaign.events.blacksite;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import lostsector.helper.FleetHelper;
import lostsector.helper.MathHelper;
import lostsector.helper.PowerLevel;
import lostsector.helper.StringHelper;
import lostsector.helper.SystemHelper;
import lostsector.helper.fleet.SimpleFleet;
import lostsector.settings.Difficulty;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Content builders for the blacksite quest: site records, defender fleets, and the loot and destruction swaps.
// Random draws per site and per defender follow the order of the station and fleet generation they preserve.
final class BlacksiteSites {

    private static final List<String> DEFENDER_FLAGS = List.of(
            MemFlags.FLEET_FIGHT_TO_THE_LAST,
            MemFlags.MEMORY_KEY_MAKE_HOSTILE,
            MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER,
            MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE,
            MemFlags.MEMORY_KEY_AVOID_PLAYER_SLOWLY,
            MemFlags.MEMORY_KEY_MAKE_NON_AGGRESSIVE,
            MemFlags.MEMORY_KEY_PATROL_ALLOW_TOFF,
            MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED,
            MemFlags.MEMORY_KEY_NO_REP_IMPACT);

    private BlacksiteSites() {
    }

    // Picks the owner, fleet count and point budget, and names the station after its owner.
    static SiteRecord adopt(SectorEntityToken entity, Random random) {
        BlacksiteFaction faction = pickFaction(entity, random);
        int count = MathHelper.getSeededRandomNumberInRange(1, 3, random);
        float mult = count == 1 ? 0.67f : count == 3 ? 1.33f : 1.0f;
        float points = MathHelper.getSeededRandomNumberInRange(faction.minPoints, faction.maxPoints, random) * mult;
        entity.setName(faction.siteName);
        return new SiteRecord(entity, faction, count, points);
    }

    private static BlacksiteFaction pickFaction(SectorEntityToken entity, Random random) {
        if (entity.getStarSystem().hasTag(Tags.THEME_REMNANT)) return BlacksiteFaction.REMNANTS;
        WeightedRandomPicker<BlacksiteFaction> picker = new WeightedRandomPicker<>();
        picker.setRandom(random);
        for (BlacksiteFaction faction : BlacksiteFaction.values()) {
            if (faction.weight > 0f) picker.add(faction, faction.weight);
        }
        return picker.pick();
    }

    // One of site.count defenders, spawned away from the player in the site's system.
    static SimpleFleet defender(SiteRecord site, Random random) {
        SectorEntityToken from = spawnLocation(site.entity.getStarSystem(), random);
        float combatPoints = site.points / site.count;
        combatPoints += (combatPoints * PowerLevel.get(0.2f, 0f, 1f)) / 2f;
        combatPoints *= Difficulty.scriptedFleetMult();

        SimpleFleet fleet = new SimpleFleet(from, site.faction.factionId, combatPoints, new ArrayList<>(DEFENDER_FLAGS), random);
        fleet.type = FleetTypes.PATROL_LARGE;
        fleet.ignoreMarketFleetSizeMult = true;
        fleet.sMods = MathHelper.getSeededRandomNumberInRange(0, 1, random);
        fleet.name = site.faction.greekSuffix
                ? site.faction.fleetName + StringHelper.getRandomGreekLetter(random, true)
                : site.faction.fleetName;
        fleet.aiFleetProperties = site.faction == BlacksiteFaction.REMNANTS;
        fleet.assignment = FleetAssignment.GO_TO_LOCATION;
        fleet.assignmentText = "moving to location";
        return fleet;
    }

    // After SimpleFleet.create(): facing, star clearance and the second FleetHelper.update of the replaced spawner.
    static void settle(CampaignFleetAPI fleet, SectorEntityToken from, Random random) {
        Vector2f loc = from.getLocation();
        fleet.setLocation(loc.x, loc.y);
        fleet.setFacing(random.nextFloat() * 360.0f);
        SystemHelper.spawnAwayFromStarFixer(fleet);
        FleetHelper.update(fleet, random);
    }

    // 500 tries for a point beyond 1.5 times the player's sensor strength, then any point.
    private static SectorEntityToken spawnLocation(StarSystemAPI system, Random random) {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        SectorEntityToken location = null;
        for (int i = 0; i < 500; i++) {
            location = SystemHelper.getRandomLocationInSystem(system, false, true, random);
            if (MathUtils.getDistance(player, location.getLocation()) < player.getSensorStrength() * 1.5f) {
                location = null;
            }
            if (location != null) break;
        }
        if (location == null) {
            location = SystemHelper.getRandomLocationInSystem(system, false, true, random);
        }
        return location;
    }

    // The ops chief's estimate of the defenders against the player's fleet points. The ratios 0.7, 1.3 and 1.75
    // themselves give an empty string, as in the replaced dialog.
    static String strength(float points) {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        points += (points * PowerLevel.get(0.2f, 0f, 1f)) / 2f;
        points *= Difficulty.scriptedFleetMult();
        float ratio = points / player.getFleetPoints();
        String str = "";
        if (ratio < 0.7f) str = "inferior";
        if (ratio < 1.3f && ratio > 0.7f) str = "similar";
        if (ratio > 1.3f && ratio < 1.75f) str = "superior";
        if (ratio > 1.75f) str = "overwhelming";
        return str;
    }

    // Replaces the station with its faction's salvage entity, discovered at any range.
    static SectorEntityToken activateLoot(SiteRecord site, Random random) {
        SectorEntityToken loot = SystemHelper.swapSalvageEntity(site.entity, site.faction.lootEntity, random);
        if (loot == null) return null;
        loot.setSensorProfile(Float.MAX_VALUE);
        loot.setExtendedDetectedAtRange(Float.MAX_VALUE);
        loot.setDetectionRangeDetailsOverrideMult(Float.MAX_VALUE);
        loot.setDiscoveryXP(0f);
        loot.setDiscoverable(false);
        return loot;
    }

    // Replaces the station with a debris field on its orbit.
    static void destroy(SectorEntityToken target, Random random) {
        StarSystemAPI system = target.getStarSystem();
        Global.getSoundPlayer().playSound("hit_hull_heavy", 1f, 1f, target.getLocation(), new Vector2f());

        DebrisFieldTerrainPlugin.DebrisFieldParams params = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                75f, // radius; above 1000 costs performance
                1.2f, // visual density
                10000000f, // duration in days
                7f); // days of glowing pieces
        params.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
        params.baseSalvageXP = 500;
        SectorEntityToken debris = Misc.addDebrisField(system, params, random);
        debris.setSensorProfile(1000f);
        debris.setDiscoverable(true);
        debris.setCircularOrbit(system.getStar(), target.getCircularOrbitAngle(), target.getCircularOrbitRadius(), target.getCircularOrbitPeriod());
        debris.setId("nskr_debris_" + target.getId());

        target.setExpired(true);
        system.removeEntity(target);
    }
}
