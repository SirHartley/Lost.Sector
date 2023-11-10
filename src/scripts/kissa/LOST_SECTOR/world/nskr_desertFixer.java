package scripts.kissa.LOST_SECTOR.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;

public class nskr_desertFixer{

    public static final String FROZEN_TYPE = "nskr_ice_desert";

    static void log(final String message) {
        Global.getLogger(nskr_desertFixer.class).info(message);
    }

    public static void fix(){
        //fixes the conditions for Frozen Desert planets
        for (StarSystemAPI system : Global.getSector().getStarSystems()){
            if (system.getPlanets().isEmpty()) continue;
            for (PlanetAPI p : system.getPlanets()){
                if (!p.getTypeId().equals(FROZEN_TYPE)) continue;
                if (!p.getMarket().isPlanetConditionMarketOnly() || p.getMarket()==null) continue;

                if (p.hasCondition(Conditions.HOT)){
                    p.getMarket().removeCondition(Conditions.HOT);
                }
                if (p.hasCondition(Conditions.VERY_HOT)){
                    p.getMarket().removeCondition(Conditions.VERY_HOT);
                }
                if (p.hasCondition(Conditions.NO_ATMOSPHERE)){
                    p.getMarket().removeCondition(Conditions.NO_ATMOSPHERE);
                }
                if (p.hasCondition(Conditions.HABITABLE)){
                    p.getMarket().removeCondition(Conditions.HABITABLE);
                }
                //farm fixer
                boolean organics = false;
                if (p.hasCondition(Conditions.ORGANICS_TRACE) || p.hasCondition(Conditions.ORGANICS_COMMON) || p.hasCondition(Conditions.ORGANICS_ABUNDANT) || p.hasCondition(Conditions.ORGANICS_PLENTIFUL)){
                    organics = true;
                }
                if (p.hasCondition(Conditions.FARMLAND_POOR)){
                    p.getMarket().removeCondition(Conditions.FARMLAND_POOR);
                    if(!organics){
                        p.getMarket().addCondition(Conditions.ORGANICS_TRACE);
                    }
                }
                if (p.hasCondition(Conditions.FARMLAND_ADEQUATE)){
                    p.getMarket().removeCondition(Conditions.FARMLAND_ADEQUATE);
                    if(!organics){
                        p.getMarket().addCondition(Conditions.ORGANICS_COMMON);
                    }
                }
                if (p.hasCondition(Conditions.FARMLAND_RICH)){
                    p.getMarket().removeCondition(Conditions.FARMLAND_RICH);
                    if(!organics){
                        p.getMarket().addCondition(Conditions.ORGANICS_ABUNDANT);
                    }
                }
                if (p.hasCondition(Conditions.FARMLAND_BOUNTIFUL)){
                    p.getMarket().removeCondition(Conditions.FARMLAND_BOUNTIFUL);
                    if(!organics){
                        p.getMarket().addCondition(Conditions.ORGANICS_PLENTIFUL);
                    }
                }
                //add thin
                if (!p.hasCondition(Conditions.THIN_ATMOSPHERE)){
                    p.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
                }
                //heat fixer
                String star = "";
                if (!p.hasCondition(Conditions.VERY_COLD) && !p.hasCondition(Conditions.COLD)){
                     if (system.getStar()!=null) {
                         star = system.getStar().getTypeId();
                         //make cold if weak star
                         if (star.equals(StarTypes.RED_DWARF) || star.equals(StarTypes.WHITE_DWARF) || star.equals(StarTypes.BROWN_DWARF) || star.equals(StarTypes.BLACK_HOLE)) {
                             p.getMarket().addCondition(Conditions.COLD);
                         }
                     }
                }
                //solar fixer
                if (p.hasCondition(Conditions.SOLAR_ARRAY) && p.hasCondition(Conditions.COLD)){
                    p.getMarket().removeCondition(Conditions.COLD);
                }
                //special fixer
                if (p.hasCondition(Conditions.INIMICAL_BIOSPHERE)){
                    p.getMarket().removeCondition(Conditions.INIMICAL_BIOSPHERE);
                }
                if (p.hasCondition(Conditions.MILD_CLIMATE)){
                    p.getMarket().removeCondition(Conditions.MILD_CLIMATE);
                }

                log("Fixed "+p.getName()+" in "+system.getName() + " t " + star);
            }
        }
    }
}
