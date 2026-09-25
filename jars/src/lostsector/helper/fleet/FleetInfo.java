package lostsector.helper.fleet;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import lostsector.campaign.enigma.HyperspaceEnigmaSpawner;

import java.util.HashMap;

public class FleetInfo {

    //class saves relevant fleet info to memory

    //base
    public CampaignFleetAPI fleet;
    public SectorEntityToken target;
    public SectorEntityToken home;
    public float age;
    public float strength;
    // FleetHelper.patrolMarketsAI: the age at which the fleet reached its target's system; -1 while on the way.
    public float patrolArrivedAge = -1f;
    //custom
    public HashMap<FleetMemberAPI, SimpleFleetMember> secondaries = new HashMap<>();
    public SimpleFleetMember flagshipSimpleMember = null;

    //custom
    //used only for HyperspaceEnigmaSpawner
    public HyperspaceEnigmaSpawner.TaskType task = null;

    public FleetInfo(CampaignFleetAPI fleet, SectorEntityToken target, SectorEntityToken home) {
        this.fleet = fleet;
        this.age = 0f;
        this.strength = fleet.getFleetPoints();
        this.target = target;
        this.home = home;
    }

}

