package lostsector.campaign.enigma;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.BaseGenericPlugin;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed;
import lostsector.helper.FleetHelper;
import lostsector.helper.MathHelper;
import org.lwjgl.util.vector.Vector2f;

import java.util.Random;

public class EnigmaDefenderPlugin extends BaseGenericPlugin implements SalvageGenFromSeed.SalvageDefenderModificationPlugin {

    static void log(final String message) {
        Global.getLogger(EnigmaDefenderPlugin.class).info(message);
    }

    public float getStrength(SalvageGenFromSeed.SDMParams p, float strength, Random random, boolean withOverride) {
        // doesn't matter, just something non-zero so we end up with a fleet
        return strength;
    }
    public float getMinSize(SalvageGenFromSeed.SDMParams p, float minSize, Random random, boolean withOverride) {
        return minSize;
    }

    public float getMaxSize(SalvageGenFromSeed.SDMParams p, float maxSize, Random random, boolean withOverride) {
        return maxSize;
    }

    public float getProbability(SalvageGenFromSeed.SDMParams p, float probability, Random random, boolean withOverride) {
        return probability;
    }

    public void reportDefeated(SalvageGenFromSeed.SDMParams p, SectorEntityToken entity, CampaignFleetAPI fleet) {
    }

    public void modifyFleet(SalvageGenFromSeed.SDMParams p, CampaignFleetAPI fleet, Random random, boolean withOverride) {
        float quality = MathHelper.getSeededRandomNumberInRange(0.60f,1f, random);
        FleetParamsV3 params = new FleetParamsV3(null, new Vector2f(),
                "enigma", quality, FleetTypes.PATROL_SMALL, fleet.getFleetPoints(),
                0f,0f, 0f,0f,0f, 0f);
        FleetFactoryV3.addCommanderAndOfficersV2(fleet, params, random);
        //make the officers AI cores
        FleetHelper.setAIOfficers(fleet);

        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
            //CR update
            m.getRepairTracker().setCR(m.getRepairTracker().getMaxCR());
        }
    }

    @Override
    public int getHandlingPriority(Object params) {
        if (!(params instanceof SalvageGenFromSeed.SDMParams)) return 0;
        SalvageGenFromSeed.SDMParams p = (SalvageGenFromSeed.SDMParams) params;

        if (p.entity!=null && p.entity.getId().startsWith("nskr_enigmabase_")) {
            log("picked nskr_enigmaDefenderPlugin");
            return 2;
        }
        return 0;
    }
    public float getQuality(SalvageGenFromSeed.SDMParams p, float quality, Random random, boolean withOverride) {
        return quality;
    }
}



