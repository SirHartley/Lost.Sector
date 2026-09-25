package lostsector.helper;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.kesteven.ExileManager;
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
        SectorEntityToken asteria = getAsteria();
        if (asteria == null || asteria.getMarket() == null) return false;
        return Ids.KESTEVEN_FACTION_ID.equals(asteria.getMarket().getFactionId());
    }

    // Asteria is in Arcadia in Corvus sectors and in a random system otherwise (world/systems/asteria/Asteria).
    public static SectorEntityToken getAsteria(){
        return Global.getSector().getEntityById(Ids.ASTERIA_ENTITY_ID);
    }

    public static SectorEntityToken getOutpost(){
        if (Global.getSector().getEconomy().getMarket("nskr_outpost")==null) return null;
        return Global.getSector().getEconomy().getMarket("nskr_outpost").getPrimaryEntity();
    }

    // Null only when neither Asteria nor the Outpost exists.
    public static MarketAPI asteriaOrOutpost(){
        SectorEntityToken asteria = getAsteria();
        SectorEntityToken outpost = getOutpost();
        MarketAPI asteriaMarket = asteria == null ? null : asteria.getMarket();
        MarketAPI outpostMarket = outpost == null ? null : outpost.getMarket();
        boolean useOutpost = asteriaMarket == null || ExileManager.getExiled(ExileManager.EXILE_KEY);
        return useOutpost && outpostMarket != null ? outpostMarket : asteriaMarket;
    }
}
