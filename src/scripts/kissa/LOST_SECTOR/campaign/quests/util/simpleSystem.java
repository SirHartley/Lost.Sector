package scripts.kissa.LOST_SECTOR.campaign.quests.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class simpleSystem {

    static void log(final String message) {
        Global.getLogger(simpleSystem.class).info(message);
    }

    //base
    public Random random;
    public int minPlanets;
    //custom
    public List<String> pickTags = new ArrayList<>();
    public List<String> pickStars = new ArrayList<>();
    public List<String> pickEntities = new ArrayList<>();
    public List<StarSystemAPI> pickSystems = new ArrayList<>();
    public List<StarSystemGenerator.StarSystemType> pickSystemTypes = new ArrayList<>();
    public List<String> blacklistTags = new ArrayList<>();
    public List<String> blacklistStars = new ArrayList<>();
    public List<StarSystemAPI> blacklistSystems = new ArrayList<>();
    public List<String> blacklistEntities = new ArrayList<>();
    public List<StarSystemGenerator.StarSystemType> blacklistSystemTypes = new ArrayList<>();
    public boolean allowCore = false;
    public boolean allowMarkets = false;
    public boolean allowNeutron = false;
    public float minDistance = 0f;
    public float maxDistance = Float.MAX_VALUE;
    public boolean allowEnteredByPlayer = true;
    public boolean pickOnlyMarket = false;
    public boolean enforceSystemStarType = false;
    public boolean pickOnlyInProcgen = false;
    public int minStarsInConstellation = 1;

    public simpleSystem(Random random, int minPlanets) {
        this.random = random;
        this.minPlanets = minPlanets;
    }

    public List<StarSystemAPI> get(){

        SectorAPI sector = Global.getSector();
        List<StarSystemAPI> validSystems = new ArrayList<>();
        for (StarSystemAPI system : sector.getStarSystems()) {
            boolean valid = false;
            if (system.getStar() == null) continue;
            if (!blacklistSystems.isEmpty()) {
                if (blacklistSystems.contains(system)) continue;
            }
            if (system.hasTag(Tags.THEME_HIDDEN) || system.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER) || system.getPlanets().size()<minPlanets) {
                continue;
            }
            if (pickOnlyInProcgen){
                Constellation c = system.getConstellation();
                if (c==null) continue;
                if (!system.isProcgen()) continue;
                int count = 1;
                for (StarSystemAPI s : Global.getSector().getStarSystems()){
                    if (s.getConstellation()==null) continue;
                    if (s==system) continue;
                    if (c==s.getConstellation()) count++;
                }
                if (count < minStarsInConstellation) continue;
            }
            if (!allowCore){
                if (system.hasTag(Tags.THEME_CORE) || system.hasTag(Tags.THEME_CORE_POPULATED)) continue;
            }
            if (!allowNeutron){
                if (util.hasNeutronStar(system)) continue;
            }
            if (!allowMarkets){
                if (!Misc.getMarketsInLocation(system.getCenter().getContainingLocation()).isEmpty()){
                    continue;
                }
            }
            if (!blacklistTags.isEmpty()){
                boolean bannedTag= false;
                for (String t : system.getTags()) {
                    if (blacklistTags.contains(t)) {
                        bannedTag = true;
                        break;
                    }
                }
                if (bannedTag) continue;
            }
            if (!blacklistStars.isEmpty()){
                if (blacklistStars.contains(system.getStar().getTypeId())) continue;
            }
            if (!blacklistSystemTypes.isEmpty()) {
                if (blacklistSystemTypes.contains(system.getType())) continue;
            }
            if (Math.abs(system.getStar().getLocationInHyperspace().length())>maxDistance || Math.abs(system.getStar().getLocationInHyperspace().length())<minDistance ) continue;
            if (!allowEnteredByPlayer) {
                if (system.isEnteredByPlayer()) continue;
            }
            if (pickOnlyMarket){
                if (Misc.getMarketsInLocation(system.getCenter().getContainingLocation()).isEmpty()){
                    continue;
                } else {
                    boolean bannedMarket= false;
                    for (MarketAPI m : Misc.getMarketsInLocation(system.getCenter().getContainingLocation())){
                        if (m.isHidden() || m.isPlanetConditionMarketOnly()){
                            bannedMarket = true;
                            break;
                        }
                    }
                    if (bannedMarket) continue;
                }
            }



            //dirty, but is required when we have no pick goals
            if (pickStars.isEmpty()&&pickSystemTypes.isEmpty()&&pickTags.isEmpty()&&pickEntities.isEmpty()&&pickSystems.isEmpty()) valid = true;

            //valid after here
            //false needs to be last (important)
            if (!pickTags.isEmpty()) {
                for (String t : system.getTags()) {
                    if (pickTags.contains(t)) {
                        valid = true;
                        break;
                    }
                }
            }
            if (!pickSystems.isEmpty()) {
                if (pickSystems.contains(system)) {
                    valid = true;
                }
            }
            if (!pickEntities.isEmpty()) {
                for (SectorEntityToken e : system.getAllEntities()) {
                    if (e.getCustomEntityType() == null) continue;
                    if (pickEntities.contains(e.getCustomEntityType())) {
                        valid = true;
                        break;
                    }
                }
            }
            if (!pickSystemTypes.isEmpty()) {
                if (pickSystemTypes.contains(system.getType())) {
                    valid = true;
                } else if (enforceSystemStarType){
                    valid = false;
                }
            }
            if (!pickStars.isEmpty()) {
                if (pickStars.contains(system.getStar().getTypeId())) {
                    valid = true;
                } else if (enforceSystemStarType){
                    valid = false;
                }
            }
            if (!blacklistEntities.isEmpty()) {
                for (SectorEntityToken e : system.getAllEntities()) {
                    if (e.getCustomEntityType() == null) continue;
                    if (blacklistEntities.contains(e.getCustomEntityType())) {
                        valid = false;
                        break;
                    }
                }
            }
            if (valid) {
                //log("valid "+ system.getName());
                validSystems.add(system);
            }
        }
        log("valid "+ validSystems.size());
        return validSystems;
    }

    public StarSystemAPI pick(){
        List<StarSystemAPI> systems = new ArrayList<>(get());
        if (systems.isEmpty()){
            log("ERROR you picked when empty bruh");
            return null;
        }
        return systems.get(mathUtil.getSeededRandomNumberInRange(0,systems.size()-1, random));
    }

}
