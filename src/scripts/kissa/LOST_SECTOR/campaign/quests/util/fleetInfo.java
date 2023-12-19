package scripts.kissa.LOST_SECTOR.campaign.quests.util;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import scripts.kissa.LOST_SECTOR.campaign.fleets.nskr_hyperspaceEnigmaSpawner;

import java.util.HashMap;

public class fleetInfo{

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
    public HashMap<FleetMemberAPI, simpleFleetMember> secondaries = new HashMap<>();
    public simpleFleetMember flagshipSimpleMember = null;

    //custom
    //used only for nskr_hyperspaceEnigmaSpawner
    public nskr_hyperspaceEnigmaSpawner.taskType task = null;

    public fleetInfo(CampaignFleetAPI fleet, SectorEntityToken target, SectorEntityToken home) {
        this.fleet = fleet;
        //this.flagship = fleet.getFlagship().;
        this.age = 0f;
        this.strength = fleet.getFleetPoints();
        this.target = target;
        this.home = home;
    }

}

