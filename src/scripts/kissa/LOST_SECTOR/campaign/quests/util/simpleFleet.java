package scripts.kissa.LOST_SECTOR.campaign.quests.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class simpleFleet {

    //base
    public SectorEntityToken loc;
    public String faction;
    public float size;
    public List<String> memKeys;
    public Random random;

    //custom
    public String type = FleetTypes.PATROL_SMALL;
    public int sMods = 0;
    public String name = null;
    public boolean noFactionInName = false;
    public int maxShipSize = 4;
    public FleetAssignment assignment = FleetAssignment.ORBIT_PASSIVE;
    public String assignmentText = "preparing";
    public boolean withOfficers = true;
    public PersonAPI commander = null;
    public simpleFleetMember flagshipInfo = null;
    public Boolean ignoreMarketFleetSizeMult = false;
    public float qualityOverride = 1f;
    public float freighterPoints = 0f;
    public float tankerPoints = 0f;
    public float transportPoints = 0f;
    public float linerPoints = 0f;
    public float utilityPoints = 0f;
    public MarketAPI source = null;
    public boolean interceptPlayer = false;
    public List<simpleFleetMember> secondaries = new ArrayList<>();
    public boolean noTransponder = false;
    public boolean dormant = false;
    public boolean aiFleetProperties = false;
    public boolean goToLocation = false;
    public SectorEntityToken goToLocationTarget = null;

    //dumb hacks
    private final HashMap<FleetMemberAPI, simpleFleetMember> secondaryMembers = new HashMap<>();

    public simpleFleet(SectorEntityToken loc, String faction, float size, List<String> memKeys, Random random) {
        this.loc = loc;
        this.faction = faction;
        this.size = size;
        this.memKeys = memKeys;
        this.random = random;
    }

    public CampaignFleetAPI create(){
        //a lot of this code modeled after Magiclib stuff made by Tartiflette

        FleetParamsV3 params = new FleetParamsV3(
                source, // source market
                loc.getLocation(),
                faction,
                1f,
                type,
                size, // combatPts
                freighterPoints, // freighterPts
                tankerPoints, // tankerPts
                transportPoints, // transportPts
                linerPoints, // linerPts
                utilityPoints, // utilityPts
                0f // qualityMod
        );

        params.withOfficers = withOfficers;
        if (sMods>0) params.averageSMods = sMods;
        params.qualityOverride = qualityOverride;
        params.maxShipSize = maxShipSize;
        params.ignoreMarketFleetSizeMult = ignoreMarketFleetSizeMult;
        params.random = random;

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

        loc.getContainingLocation().addEntity(fleet);
        fleet.setLocation(loc.getLocation().x, loc.getLocation().y);

        if (flagshipInfo!=null){
            //ADDING FLAGSHIP
            FleetMemberAPI flagship = fleetUtil.generateShip(flagshipInfo.variant, flagshipInfo.noAutofit, flagshipInfo.alwaysRecover, flagshipInfo.variantTags, flagshipInfo.hullmods);

            if (flagshipInfo.name!=null) flagship.setShipName(flagshipInfo.name);
            flagship.setFlagship(true);
            //always recover flagship
            if (flagshipInfo.alwaysRecover) flagship.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
            if (flagshipInfo.noAutofit) flagship.getVariant().addTag(Tags.TAG_NO_AUTOFIT);
            fleet.getFleetData().addFleetMember(flagship);

            for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
                //flagship fuckery
                if (m==flagship){
                    if(!m.isFlagship()){
                        m.setFlagship(true);
                    }
                } else if(m.isFlagship()){
                    m.setFlagship(false);
                }
            }
            fleet.getFlagship().setCaptain(fleet.getCommander());
        }
        if (!secondaries.isEmpty()){
            for (simpleFleetMember m : secondaries){
                FleetMemberAPI secondary = m.create();
                fleet.getFleetData().addFleetMember(secondary);
                //
                secondaryMembers.put(secondary, m);
            }
            //

        }
        if (commander!=null){
            //add to fleet
            fleet.setCommander(commander);

            //apply skills to the fleet
            FleetFactoryV3.addCommanderSkills(fleet.getCommander(), fleet, params, random);

            //add the defined captain to the flagship if needed
            fleet.getFlagship().setCaptain(commander);
        }

        if (name!=null) fleet.setName(name);
        if (noFactionInName) fleet.setNoFactionInName(true);

        for (String key : memKeys){
            fleet.getMemoryWithoutUpdate().set(key, true);
        }

        //assignment
        //intercept
        if (interceptPlayer){
            fleet.addAssignment(assignment, Global.getSector().getPlayerFleet(), Float.MAX_VALUE, assignmentText);
        }
        //goto
        else if (goToLocation && goToLocationTarget!=null){
            fleet.addAssignment(assignment, goToLocationTarget, Float.MAX_VALUE, assignmentText);
        }
        //default
        else if (assignment!=null){
            fleet.addAssignment(assignment, loc, Float.MAX_VALUE, assignmentText);
        }

        //dormant
        if (dormant){
            RemnantSeededFleetManager.initRemnantFleetProperties(random, fleet, true);
        }

        //AI properties
        if (aiFleetProperties){
            RemnantSeededFleetManager.addRemnantInteractionConfig(fleet);
        }
        //transponder
        if (!noTransponder){
            fleet.setTransponderOn(true);
        } else fleet.setTransponderOn(false);

        //UPDATE
        fleetUtil.update(fleet, random);

        return fleet;
    }

    public simpleFleetMember getFlagshipInfo() {
        return flagshipInfo;
    }
    public HashMap<FleetMemberAPI, simpleFleetMember> getSecondaryMembers() {
        return secondaryMembers;
    }
}
