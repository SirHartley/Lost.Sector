package lostsector.campaign.starts.hellspawn;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import lostsector.helper.Ids;
import lostsector.helper.MathHelper;
import lostsector.helper.fleet.SimpleFleet;
import lostsector.settings.Difficulty;

import java.util.ArrayList;
import java.util.Random;

final class HellSpawnFleets {

    private HellSpawnFleets() {
    }

    static SimpleFleet judgement(CampaignFleetAPI player, boolean hell, Random random) {
        float combatPoints = MathHelper.getSeededRandomNumberInRange(250f, 300f, random);
        if (hell) combatPoints *= 1.33f;
        combatPoints *= Difficulty.scriptedFleetMult();

        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOSTILE);
        keys.add(MemFlags.MEMORY_KEY_NO_REP_IMPACT);
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);
        keys.add(MemFlags.MEMORY_KEY_NEVER_AVOID_PLAYER_SLOWLY);
        keys.add(HellSpawnJudgementInteraction.JUDGEMENT_FLEET_KEY);

        SimpleFleet fleet = new SimpleFleet(player.getContainingLocation().createToken(player.getLocation()), Ids.AI_ALL_FACTION_ID, combatPoints, keys, random);
        fleet.aiFleetProperties = true;
        fleet.ignoreMarketFleetSizeMult = true;
        fleet.name = "Final Judgement";
        fleet.noFactionInName = true;
        fleet.assignment = FleetAssignment.INTERCEPT;
        fleet.interceptPlayer = true;
        fleet.assignmentText = "Hunting";
        return fleet;
    }
}
