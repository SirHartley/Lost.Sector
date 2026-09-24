package lostsector.helper;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.world.systems.frost.Frost;

public class SectorLookup {

    public static StarSystemAPI getFrost(){
        return Global.getSector().getStarSystem(Frost.getName());
    }

    public static boolean enigmaExists(){

        MarketAPI market = Global.getSector().getEconomy().getMarket("nskr_heart");
        if (market == null) return false;
        if (market.getFactionId()==null) return false;
        if (!market.getFactionId().equals("enigma")) return false;

        return true;
    }

    public static boolean kestevenExists(){
        return Misc.getFactionMarkets(Ids.KESTEVEN_FACTION_ID).size() > 0;
    }

    public static boolean asteriaExists(){
        boolean asteria = false;
        if (Global.getSector().getStarSystem("Arcadia")!=null) {
            for (PlanetAPI p : Global.getSector().getStarSystem("Arcadia").getPlanets()) {
                if (p.getId().equals("nskr_asteria")) {
                    if (p.getMarket() == null) continue;
                    if (p.getMarket().getFactionId()==null) continue;
                    if (!p.getMarket().getFactionId().equals("kesteven")) continue;
                    asteria = true;
                    break;
                }
            }
        }
        return asteria;
    }

    public static SectorEntityToken getAsteria(){
        SectorEntityToken asteria = null;
        if (Global.getSector().getStarSystem("Arcadia")!=null) {
            for (PlanetAPI p : Global.getSector().getStarSystem("Arcadia").getPlanets()) {
                if (p.getId().equals("nskr_asteria")) {
                    asteria = p;
                    break;
                }
            }
        }
        return asteria;
    }

    public static SectorEntityToken getOutpost(){
        if (Global.getSector().getEconomy().getMarket("nskr_outpost")==null) return null;
        return Global.getSector().getEconomy().getMarket("nskr_outpost").getPrimaryEntity();
    }
}
