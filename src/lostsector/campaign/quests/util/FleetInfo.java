package lostsector.campaign.quests.util;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import lostsector.campaign.fleets.HyperspaceEnigmaSpawner;

import java.util.HashMap;

public class FleetInfo {

    //class saves relevant fleet info to memory

    //base
    public CampaignFleetAPI fleet;
    //it don't work like this lmao
    //public FleetMemberAPI flagship;
    public SectorEntityToken target;
    public SectorEntityToken home;
    public float age;
    public float strength;
    //custom
    public HashMap<FleetMemberAPI, SimpleFleetMember> secondaries = new HashMap<>();
    public SimpleFleetMember flagshipSimpleMember = null;

    //custom
    //used only for HyperspaceEnigmaSpawner
    public HyperspaceEnigmaSpawner.taskType task = null;

    public FleetInfo(CampaignFleetAPI fleet, SectorEntityToken target, SectorEntityToken home) {
        this.fleet = fleet;
        //this.flagship = fleet.getFlagship().;
        this.age = 0f;
        this.strength = fleet.getFleetPoints();
        this.target = target;
        this.home = home;
    }

}

