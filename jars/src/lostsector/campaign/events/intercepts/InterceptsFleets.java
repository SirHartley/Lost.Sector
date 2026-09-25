package lostsector.campaign.events.intercepts;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import lostsector.helper.Ids;
import lostsector.helper.MathHelper;
import lostsector.helper.PowerLevel;
import lostsector.helper.fleet.SimpleFleet;
import lostsector.settings.Difficulty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Builders for InterceptEncounter: each configures a SimpleFleet at the spawn point and returns it unbuilt.
final class InterceptsFleets {

    private static final String ARO_NAME = "ARO Strike Group";
    private static final String MESSENGER_NAME = "Merc Messenger";
    private static final String AUTO_HUNTER_NAME = "Hunter Fanatics";
    private static final String COLLECTOR_NAME = "Debt Collector";
    private static final String INTERCEPT_TEXT = "intercepting your fleet";

    private InterceptsFleets() {
    }

    static SimpleFleet aro(SectorEntityToken at, Random random) {
        float combatPoints = MathHelper.getSeededRandomNumberInRange(110f, 130f, random);
        combatPoints += combatPoints * PowerLevel.get(0.2f, 0f, 2f);
        combatPoints *= Difficulty.scriptedFleetMult();

        List<String> keys = new ArrayList<>();
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOSTILE);
        keys.add(MemFlags.MEMORY_KEY_NO_REP_IMPACT);
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);

        SimpleFleet fleet = new SimpleFleet(at, Factions.LUDDIC_CHURCH, combatPoints, keys, random);
        fleet.maxShipSize = 4;
        fleet.sMods = MathHelper.getSeededRandomNumberInRange(2, 3, random);
        fleet.name = ARO_NAME;
        fleet.ignoreMarketFleetSizeMult = true;
        fleet.noFactionInName = true;
        interceptPlayer(fleet);
        return fleet;
    }

    static SimpleFleet messenger(SectorEntityToken at, Random random) {
        float combatPoints = MathHelper.getSeededRandomNumberInRange(50f, 70f, random);
        combatPoints += combatPoints * PowerLevel.get(0.2f, 0f, 2f);
        combatPoints *= Difficulty.scriptedFleetMult();

        List<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);

        SimpleFleet fleet = new SimpleFleet(at, Factions.PIRATES, combatPoints, keys, random);
        fleet.maxShipSize = 3;
        fleet.name = MESSENGER_NAME;
        fleet.ignoreMarketFleetSizeMult = true;
        interceptPlayer(fleet);
        return fleet;
    }

    static SimpleFleet autoHunter(SectorEntityToken at, Random random) {
        float combatPoints = MathHelper.getSeededRandomNumberInRange(70f, 80f, random);
        combatPoints += combatPoints * PowerLevel.get(0.2f, 0f, 1.5f);
        combatPoints *= Difficulty.scriptedFleetMult();

        List<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);

        SimpleFleet fleet = new SimpleFleet(at, Factions.LUDDIC_PATH, combatPoints, keys, random);
        fleet.name = AUTO_HUNTER_NAME;
        fleet.ignoreMarketFleetSizeMult = true;
        fleet.qualityOverride = MathHelper.getSeededRandomNumberInRange(0.70f, 0.80f, random);
        fleet.sMods = MathHelper.getSeededRandomNumberInRange(1, 2, random);
        interceptPlayer(fleet);
        return fleet;
    }

    static SimpleFleet collector(SectorEntityToken at, Random random) {
        float combatPoints = MathHelper.getSeededRandomNumberInRange(100f, 110f, random);
        combatPoints += combatPoints * PowerLevel.get(0.2f, 0f, 2f);
        combatPoints *= Difficulty.scriptedFleetMult();

        List<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);

        SimpleFleet fleet = new SimpleFleet(at, Ids.KESTEVEN_FACTION_ID, combatPoints, keys, random);
        fleet.ignoreMarketFleetSizeMult = true;
        fleet.maxShipSize = 3;
        fleet.sMods = MathHelper.getSeededRandomNumberInRange(2, 3, random);
        fleet.name = COLLECTOR_NAME;
        interceptPlayer(fleet);
        return fleet;
    }

    private static void interceptPlayer(SimpleFleet fleet) {
        fleet.assignment = FleetAssignment.INTERCEPT;
        fleet.assignmentText = INTERCEPT_TEXT;
        fleet.interceptPlayer = true;
    }

    // The ARO group and the messenger are built from Luddic Church and pirate doctrine but fly as mercenaries.
    static void flyAsMercenaries(CampaignFleetAPI fleet, Random random) {
        fleet.setFaction(Factions.MERCENARY, true);
    }

    // A collector that stops hunting ignores other fleets on its way home.
    static void ignoreOtherFleets(CampaignFleetAPI fleet) {
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
    }

    // Half of the ships and always the flagship get the machine spirit hullmod.
    static void addMachineSpirits(CampaignFleetAPI fleet, Random random) {
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (member.isFighterWing()) continue;
            ShipVariantAPI variant = member.getVariant();
            if (variant == null) continue;
            if (random.nextFloat() < 0.50f || member.isFlagship()) {
                variant.addPermaMod("nskr_machineSpirit");
                variant.addTag(Tags.TAG_NO_AUTOFIT);
                if (member.isFlagship()) variant.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
            }
        }
    }
}
