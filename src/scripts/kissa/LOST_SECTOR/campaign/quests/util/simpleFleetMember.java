package scripts.kissa.LOST_SECTOR.campaign.quests.util;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;

import java.util.ArrayList;
import java.util.List;

public class simpleFleetMember {

    //base
    public String variant;
    public List<String> variantTags;
    public boolean noAutofit;

    //custom
    public PersonAPI captain = null;
    public String name = null;
    public boolean alwaysRecover = false;
    public FleetMemberAPI member = null;
    public List<String> hullmods = new ArrayList<>();

    public simpleFleetMember(String variant, List<String> variantTags, boolean noAutofit) {
        this.variant = variant;
        this.variantTags = variantTags;
        this.noAutofit = noAutofit;
    }

    public FleetMemberAPI create(){
        FleetMemberAPI ship;

        ship = fleetUtil.generateShip(variant, noAutofit, alwaysRecover, variantTags, hullmods);
        if (name!=null)ship.setShipName(name);
        if (captain!=null)ship.setCaptain(captain);

        member = ship;

        return ship;
    }
}
