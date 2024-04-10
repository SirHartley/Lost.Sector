package scripts.kissa.LOST_SECTOR.campaign.quests.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.util.Misc;
import org.jetbrains.annotations.Nullable;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.nskr_exileManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.nskr_kQuest5ElizaBarMain;
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.nskr_kestevenQuest;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class questUtil {

    static void log(final String message) {
        Global.getLogger(questUtil.class).info(message);
    }

    public static SectorEntityToken spawnArtifact(SectorEntityToken loc, int number) {
        Random random = nskr_kestevenQuest.getRandom();
        LocationAPI containing = loc.getContainingLocation();
        //log(""+containing.getName()+" "+containing.getId());
        BaseThemeGenerator.EntityLocation createLoc = DerelictThemeGenerator.createLocationAtRandomGap(random, loc, 0f);
        //log(""+createLoc.type.name());
        //log(""+createLoc.orbit.getFocus().getName());
        SectorEntityToken artifact = DerelictThemeGenerator.addNonSalvageEntity(containing, createLoc, "nskr_artifact", Factions.NEUTRAL).entity;
        artifact.setDiscoverable(true);
        artifact.setSensorProfile(100f);

        artifact.setCircularOrbitPointingDown(loc, random.nextFloat()*360f, MathUtils.getDistance(artifact.getLocation(), loc.getLocation()), mathUtil.getSeededRandomNumberInRange(30f,60f, random));

        artifact.getMemory().set(questStageManager.ARTIFACT_KEY+number, true);

        //makes sure we are not in a star
        questUtil.spawnAwayFromStarFixer(artifact, 2.0f);

        log("baseLoc " + loc.getName());
        log("qUtil SPAWNED artifact in " + artifact.getOrbitFocus().getName());
        return artifact;
    }

    public static MarketAPI asteriaOrOutpost(){
        MarketAPI market = null;
        if (nskr_exileManager.getExiled(nskr_exileManager.EXILE_KEY)){
            market = util.getOutpost().getMarket();
        } else market = util.getAsteria().getMarket();
        //no asteria generated workaround
        if (market==null){
            market = util.getOutpost().getMarket();
        }
        return market;
    }

    public static boolean outpostExists(){
        if (Global.getSector().getEconomy().getMarket("nskr_outpost")==null) return false;
        return Global.getSector().getEconomy().getMarket("nskr_outpost").getFaction().getId().equals("kesteven");
    }

    public static String outpostName(){
        return Global.getSector().getEconomy().getMarket("nskr_outpost").getName();
    }

    //for kesteven quest line
    //public static SectorEntityToken getRandomTTMarket(Random random, boolean allowHybrasil) {
    //    List<MarketAPI> validMarkets = new ArrayList<>();
    //    for (MarketAPI market : Misc.getFactionMarkets(Factions.TRITACHYON)) {
    //        boolean isValid = true;
    //        StarSystemAPI system = market.getStarSystem();
    //        if (system.hasTag(Tags.THEME_HIDDEN) || system.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER) ||
    //                system.getStar() == null || system.getPlanets().size()<1) {
    //            isValid = false;
    //        }
    //        if (market.isHidden() || market.isPlanetConditionMarketOnly()){
    //            isValid = false;
    //        }
    //        if (!allowHybrasil) {
    //            //ban hybrasil initially, too hard
    //            if (system.getName().toLowerCase().startsWith("hybrasil")){
    //                isValid = false;
    //            }
    //        }
    //        if (isValid) {
    //            validMarkets.add(market);
    //        }
    //    }
    //    if (validMarkets.isEmpty()){
    //        if (allowHybrasil){
    //            log("ERROR no valid TT markets picking random TT market");
    //            return getRandomFactionMarket(random, Factions.TRITACHYON);
    //        }
    //        log("ERROR no valid TT markets try again");
    //        return getRandomTTMarket(random, true);
    //    }
    //    return validMarkets.get(mathUtil.getSeededRandomNumberInRange(0,validMarkets.size()-1, random)).getPrimaryEntity();
    //}

    private static StarSystemAPI getRandomSystem(Random random) {
        simpleSystem simpleSystem = new simpleSystem(random, 1);
        return simpleSystem.pick();
    }

    //for kesteven quest line
    public static StarSystemAPI getRandomSystemNearCore(Random random) {
        //ban tags
        List<String> banTags = new ArrayList<>();
        banTags.add(Tags.THEME_REMNANT_MAIN);
        banTags.add(Tags.THEME_REMNANT_RESURGENT);
        banTags.add(Tags.THEME_UNSAFE);

        simpleSystem simpleSystem = new simpleSystem(random, 2);
        simpleSystem.maxDistance = 27500f;
        simpleSystem.blacklistTags = banTags;
        simpleSystem.pickOnlyInProcgen = true;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("ERROR no valid system");
        return util.getRandomNonCoreSystem(random);
    }

    //for kesteven quest line
    public static StarSystemAPI getRandomSystemFarCore(Random random) {

        //ban tags
        List<String> banTags = new ArrayList<>();
        banTags.add(Tags.THEME_REMNANT_MAIN);
        banTags.add(Tags.THEME_REMNANT_RESURGENT);
        banTags.add(Tags.THEME_UNSAFE);

        simpleSystem simpleSystem = new simpleSystem(random, 2);
        simpleSystem.minDistance = 32500f;
        simpleSystem.blacklistTags = banTags;
        simpleSystem.pickOnlyInProcgen = true;
        simpleSystem.minStarsInConstellation = 2;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("ERROR no valid system");
        return util.getRandomNonCoreSystem(random);
    }

    //for kesteven quest line
    public static StarSystemAPI getRandomSystemNearLocation(Vector2f loc, float minDistance, float maxDistance, StarSystemAPI ignore, Random random) {

        //banned systems
        List<StarSystemAPI> banSys = new ArrayList<>();
        banSys.add(ignore);

        simpleSystem simpleSystem = new simpleSystem(random, 1);
        simpleSystem.blacklistSystems = banSys;
        simpleSystem.pickOnlyInProcgen = true;

        //First, get all the valid systems and put them in a separate list
        List<StarSystemAPI> validSystems = new ArrayList<>();
        for (StarSystemAPI system : simpleSystem.get()) {
            boolean isValid = false;
            //any near core
            float dist = MathUtils.getDistance(loc, system.getStar().getLocationInHyperspace());
            if (dist <= maxDistance && dist > minDistance){
                isValid = true;
                //log("getRandomSystemNearLocation +1 valid, dist "+dist);
            }
            if (isValid) {
                validSystems.add(system);
            }
        }
        //If that list is empty, retry
        if (validSystems.isEmpty()) {
            log("qUtil ERROR no valid system, no nearby systems, retry");
            //increase search radius if fail
            return getRandomSystemNearLocation(loc, minDistance, maxDistance*1.5f, ignore, random);
        }
        //Otherwise, get a random element in it and return that
        else {
            //seeded random
            return validSystems.get(mathUtil.getSeededRandomNumberInRange(0,validSystems.size()-1, random));
        }
    }

    //for kesteven quest line
    public static StarSystemAPI getRandomSystemWithinConstellation(Constellation constellation, @Nullable StarSystemAPI ignore, int minPlanets, Random random) {

        //banned systems
        List<StarSystemAPI> banSys = new ArrayList<>();
        if (ignore!=null) banSys.add(ignore);

        simpleSystem simpleSystem = new simpleSystem(random, minPlanets);
        simpleSystem.blacklistSystems = banSys;
        simpleSystem.allowNeutron = true;
        simpleSystem.pickOnlyInProcgen = true;

        //First, get all the valid systems and put them in a separate list
        List<StarSystemAPI> validSystems = new ArrayList<>();
        for (StarSystemAPI system : simpleSystem.get()) {
            boolean isValid = false;
            //any within constellation
            if (system.getConstellation()==constellation){
                isValid = true;
            }
            if (isValid) {
                validSystems.add(system);
            }
        }
        //back-up
        if (validSystems.isEmpty() && ignore!=null){
            return getRandomSystemWithinConstellation(constellation, null, 1, random);
        }
        //seeded random
        if (!validSystems.isEmpty()) {
            return validSystems.get(mathUtil.getSeededRandomNumberInRange(0, validSystems.size() - 1, random));
        }
        log("ERROR no systems in constellation");
        return null;
    }

    //for kesteven quest line
    public static StarSystemAPI getRandomSystemWithEnigmaBase(Random random){

        List<String> entities = new ArrayList<>();
        entities.add("nskr_enigmabase");

        simpleSystem simpleSystem = new simpleSystem(random, 1);
        simpleSystem.pickEntities = entities;
        simpleSystem.pickOnlyInProcgen = true;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("ERROR no valid system");
        return null;
    }


    //all vanilla except kanta and umbra
    public static final ArrayList<String> VALID_ELIZA_SYSTEMS = new ArrayList<>();
    static {
        VALID_ELIZA_SYSTEMS.add("yma");
        VALID_ELIZA_SYSTEMS.add("corvus");
        VALID_ELIZA_SYSTEMS.add("isirah");
        VALID_ELIZA_SYSTEMS.add("thule");
        VALID_ELIZA_SYSTEMS.add("hybrasil");
        VALID_ELIZA_SYSTEMS.add("galatia");
        VALID_ELIZA_SYSTEMS.add("mayasura");
        VALID_ELIZA_SYSTEMS.add("kumari kandam");
    }
    public static SectorEntityToken pickElizaMarket(Random random, boolean ignoreUsedMarket) {
        List<MarketAPI> validMarkets = new ArrayList<>();
        for (MarketAPI market : Misc.getFactionMarkets(Factions.PIRATES)) {
            boolean isValid = true;
            StarSystemAPI system = market.getStarSystem();
            if (!VALID_ELIZA_SYSTEMS.contains(system.getNameWithNoType().toLowerCase())){
                isValid = false;
            }
            if (system.hasTag(Tags.THEME_HIDDEN) || system.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER) ||
                    system.getStar() == null || system.getPlanets().size()<1) {
                isValid = false;
            }
            if (market.isHidden() || market.isPlanetConditionMarketOnly()){
                isValid = false;
            }
            if (!ignoreUsedMarket) {
                if (nskr_kQuest5ElizaBarMain.getUsedMarkets(nskr_kQuest5ElizaBarMain.USED_MARKET_KEY).contains(market.getId())) {
                    isValid = false;
                }
            }
            //avoid kanta's den, since that makes no sense
            if (market.getId().equals("kantas_den")) isValid = false;
            if (isValid) {
                validMarkets.add(market);
            }
        }
        if (validMarkets.isEmpty()){
            if (ignoreUsedMarket){
                log("ERROR no valid Eliza markets picking random pirate market");
                return getRandomFactionMarket(random, Factions.PIRATES, nskr_kQuest5ElizaBarMain.getUsedMarkets(nskr_kQuest5ElizaBarMain.USED_MARKET_KEY));
            }
            log("ERROR no valid Eliza markets try again");
            return pickElizaMarket(random, true);
        }
        return validMarkets.get(mathUtil.getSeededRandomNumberInRange(0,validMarkets.size()-1, random)).getPrimaryEntity();
    }

    public static boolean hasFactionMarket(StarSystemAPI sys, String faction){
        boolean market = false;
        for (SectorEntityToken e : sys.getAllEntities()){
            if (e.getMarket() == null) continue;
            if (e.getMarket().getFactionId() == null) continue;
            if (e.getMarket().isPlanetConditionMarketOnly()) continue;
            if (e.getMarket().isHidden()) continue;
            if (e.getMarket().getFactionId().equals(Factions.NEUTRAL)) continue;
            if (e.getMarket().getFaction().getId().equals(faction)){
                market = true;
                break;
            }
        }
        return market;
    }

    public static SectorEntityToken getRandomFactionMarket(Random random, String faction) {
        return getRandomFactionMarket(random, faction, new ArrayList<String>());
    }
    public static SectorEntityToken getRandomFactionMarket(Random random, String faction, List<String> blacklist) {
        List<MarketAPI> validMarkets = new ArrayList<>();
        for (MarketAPI market : Misc.getFactionMarkets(faction)) {
            boolean isValid = true;
            StarSystemAPI system = market.getStarSystem();
            //crash on hyperspace markets
            if (system==null) continue;
            if (system.hasTag(Tags.THEME_HIDDEN) || system.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER) ||
                    system.getStar() == null || system.getPlanets().size()<1) {
                isValid = false;
            }
            if (blacklist.contains(market.getId())){
                isValid = false;
            }
            if (market.isHidden() || market.isPlanetConditionMarketOnly()){
                isValid = false;
            }
            if (isValid) {
                validMarkets.add(market);
            }
        }
        if (validMarkets.isEmpty()){
            if (!blacklist.isEmpty()){
                log("ERROR no valid " + faction + " markets with blacklist retry");
                return getRandomFactionMarket(random, faction, new ArrayList<String>());
            } else {
                log("ERROR no valid " + faction + " markets picking random market");
                return util.getRandomMarket(random, false);
            }
        }
        return validMarkets.get(mathUtil.getSeededRandomNumberInRange(0,validMarkets.size()-1, random)).getPrimaryEntity();
    }


    //for kesteven quest line
    public static boolean hasEnigmaBase(StarSystemAPI system){
        boolean base = false;
        for (SectorEntityToken e : system.getAllEntities()){
            if (e.getCustomEntityType()==null) continue;
            if (e.getCustomEntityType().equals("nskr_enigmabase")){
                base = true;
                break;
            }
        }
        return base;
    }

    public static SectorEntityToken getCacheGate(){
        StarSystemAPI sys = Global.getSector().getStarSystem("Unknown Site");
        SectorEntityToken gate = null;
        for (SectorEntityToken e : sys.getAllEntities()){
            if (e.getId()==null)continue;
            if (e.getId().equals("nsrk_cacheGate")){
                gate = e;
                break;
            }
        }
        return gate;
    }
    public static SectorEntityToken getArtifact(StarSystemAPI system){
        SectorEntityToken artifact = null;
        for (SectorEntityToken e : system.getAllEntities()){
            if (e.getCustomEntityType()==null)continue;
            if (e.getCustomEntityType().equals("nskr_artifact")){
                artifact = e;
                break;
            }
        }
        return artifact;
    }

    public static String parseConstellation(String constellation){
        if (!constellation.contains("Constellation")){
            constellation+=" Constellation";
        }
        return constellation;
    }

    public static SectorEntityToken pickCacheFleetLoc() {
        Random random = nskr_kestevenQuest.getRandom();
        StarSystemAPI sys = Global.getSector().getStarSystem("Unknown Site");

        return sys.createToken(new Vector2f(mathUtil.getSeededRandomNumberInRange(-3000f, 3000f, random), mathUtil.getSeededRandomNumberInRange(-3000f, 3000f, random)));
    }

    public static SectorEntityToken spawnAwayFromStarFixer(SectorEntityToken entity){
        return spawnAwayFromStarFixer(entity, 1f);
    }

    public static SectorEntityToken spawnAwayFromStarFixer(SectorEntityToken entity, float extraDistanceMult){
        PlanetAPI planet = getNearestPlanetEntity(entity);
        if (planet==null){
            log("ERROR no planets");
            return entity;
        }
        PlanetAPI focus = null;
        if (entity.getOrbit()!=null&&entity.getOrbitFocus()!=null){
            if (entity.getOrbitFocus() instanceof PlanetAPI){
                focus = (PlanetAPI) entity.getOrbitFocus();
                log("focus " +focus.getName());
                log("planet " +planet.getName());
            }
        }
        //move away from planet or star
        float length = MathUtils.getDistance(entity.getLocation(), planet.getLocation());
        float end_x = 0f;
        float end_y = 0f;
        if (planet.isStar()) extraDistanceMult = 2.0f;
        if (focus!=null && planet!=focus) extraDistanceMult = 1f;
        float toDistance = (planet.getRadius() * 1.25f) * extraDistanceMult;
        if (planet.isStar() && length<=0f){
            Vector2f newVector = Vector2f.add(new Vector2f(100f,100f), entity.getLocation(), null);
            entity.setLocation(newVector.getX(), newVector.getY());
            length = MathUtils.getDistance(entity.getLocation(), planet.getLocation());
            log("fixed 0 location");
        }
        log("planet "+planet.getName()+" toDist "+toDistance+" ent "+entity.getName());
        float endLength = 0f;
        if (length>0f && length < toDistance) {
            while (length < toDistance) {
                Vector2f vector = new Vector2f(mathUtil.scaleVector(Vector2f.sub(entity.getLocation(), planet.getLocation(), entity.getLocation()), 1.25f));
                length = vector.length();
                endLength = vector.length();
                end_x = planet.getLocation().getX() + length * (vector.getX() / length);
                end_y = planet.getLocation().getY() + length * (vector.getY() / length);
                log("x " + end_x + " y " + end_y + " length " + length + " goal " + toDistance + " extra " + extraDistanceMult);
                entity.setLocation(end_x, end_y);
                log("util moved " + entity.getName() + " loc " + entity.getContainingLocation().getName());
            }
        } else return entity;

        OrbitAPI newOrbit;
        if (endLength>0f) length = endLength;

        float angle = VectorUtils.getAngle(planet.getLocation(), entity.getLocation());
        float days = MathUtils.getRandomNumberInRange(1f, 1.25f) * (length/15f);
        if (entity.getOrbit()!=null && entity.getOrbitFocus()!=null) {
            newOrbit = Global.getFactory().createCircularOrbit(entity.getOrbitFocus(), angle, length, days);
        } else {
            newOrbit = Global.getFactory().createCircularOrbit(planet, angle, length, days);
        }
        entity.setOrbit(newOrbit);
        log ("util finished "+entity.getName()+" dist "+MathUtils.getDistance(entity.getLocation(), planet.getLocation())+ " target "+length);

        return entity;
    }

    private static PlanetAPI getNearestPlanetEntity(SectorEntityToken entity) {
        float dist = Float.MAX_VALUE;
        float newDist = 0f;
        PlanetAPI nearest = null;
        for (SectorEntityToken e : entity.getStarSystem().getAllEntities()){
            if (e instanceof PlanetAPI){
                newDist = MathUtils.getDistance(e.getLocation(), entity.getLocation()) - e.getRadius();
                if (newDist<dist) {
                    dist = newDist;
                    nearest = (PlanetAPI)e;
                    //log("nearest "+nearest.getName()+" dist "+newDist);
                }
            }
        }
        return nearest;
    }

    //public static void hackBrokenVariants(){
    //    //WHY THE FUCK DO I NEED TO DO THIS AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    //    //nvm this happened because I dind't setIds for custom fleetMembers
    //    for (StarSystemAPI sys : Global.getSector().getStarSystems()){
    //        for (SectorEntityToken e : sys.getAllEntities()){
    //            if (e.getMemoryWithoutUpdate().contains(simpleFleet.FLEET_TRACKER_KEY)){
    //                if (e instanceof CampaignFleetAPI) {
    //                    CampaignFleetAPI f = (CampaignFleetAPI) e;
    //                    ArrayList<simpleFleetMember> data = (ArrayList<simpleFleetMember>) f.getMemoryWithoutUpdate().get(simpleFleet.FLEET_TRACKER_KEY);
    //                    if (data.isEmpty())continue;
    //                    for (FleetMemberAPI m : f.getFleetData().getMembersListWithFightersCopy()){
    //                        if (m.isFighterWing()) continue;
    //                        ShipVariantAPI thisVariant = null;
    //                        for (simpleFleetMember info : data){
    //                            if(info.member == m){
    //                                thisVariant = Global.getSettings().getVariant(info.variant);
    //                                if (!info.variantTags.isEmpty()){
    //                                    for (String t : info.variantTags) {
    //                                        thisVariant.addTag(t);
    //                                    }
    //                                }
    //                                if (info.noAutofit) thisVariant.addTag(Tags.TAG_NO_AUTOFIT);
    //                                if (info.member.isFlagship()) thisVariant.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
    //                                thisVariant.addTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
    //                                break;
    //                            }
    //                        }
    //                        if (thisVariant!=null) {
    //                            m.setVariant(thisVariant, false, true);
    //                            log("ERROR fixed " + m.getHullSpec().getHullName());
    //                        }
    //                    }
    //                }
    //            }
    //        }
    //    }
    //}

    public static void saveEnding(){

        //default
        nskr_modPlugin.saveToConfig(nskr_modPlugin.COMPLETED_STORY_KEY, true);
        //hard
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (data.containsKey(nskr_modPlugin.STARFARER_MODE_FROM_START_KEY)) {
            if ((boolean) data.get(nskr_modPlugin.STARFARER_MODE_FROM_START_KEY)) nskr_modPlugin.saveToConfig(nskr_modPlugin.COMPLETED_STORY_HARD_KEY, true);
        }

    }

    public static int getStage() {
        String id = questStageManager.KESTEVEN_QUEST_KEY;

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, 0);

        return (int)data.get(id);
    }

    public static void setStage(int stage) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(questStageManager.KESTEVEN_QUEST_KEY, stage);
    }

    public static int getDisksRecovered() {
        String id = questStageManager.DISK_COUNT_KEY;

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, 0);

        return (int)data.get(id);
    }

    public static void setDisksRecovered(int count) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(questStageManager.DISK_COUNT_KEY, count);
    }

    public static boolean getFailed(String id) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, false);

        return (boolean)data.get(id);
    }

    public static void setFailed(boolean failed, String id) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(id, failed);
    }

    public static boolean getCompleted(String id) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, false);

        return (boolean)data.get(id);
    }

    public static void setCompleted(boolean completed, String id) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(id, completed);
    }

    public static float getMissionTimerJob3() {
        String id = questStageManager.JOB3_TIMER_KEY;

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, questStageManager.JOB3_TIME_LIMIT);

        return (float)data.get(id);
    }

    public static void setMissionTimerJob3(float timer) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(questStageManager.JOB3_TIMER_KEY, timer);
    }

    public static boolean getEndMissions() {
        String id = questStageManager.QUEST_END_KEY;

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, false);

        return (boolean)data.get(id);
    }

    public static void setEndMissions(boolean end) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(questStageManager.QUEST_END_KEY, end);
    }

    public static float getFloat(String id) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, 0.0f);

        return (float)data.get(id);
    }

    public static void setFloat(float value, String id) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(id, value);
    }

    public static StarSystemAPI getJob1Tip(){
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = nskr_kestevenQuest.PERSISTENT_KEY+"Tip1";
        if (!data.containsKey(id)) {
            StarSystemAPI sys = getRandomSystemWithEnigmaBase(nskr_kestevenQuest.getRandom());
            //NO VALID SYSTEMS
            if (sys==null) return null;

            data.put(id, sys);
            //add dormant
            util.addDormant(util.getRandomLocationInSystem(sys ,true,false, nskr_kestevenQuest.getRandom()),
                    "enigma", 20f);
        }

        return (StarSystemAPI) data.get(id);
    }

    public static SectorEntityToken getJob3Start(){
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = nskr_kestevenQuest.PERSISTENT_KEY+"Start3";
        if (!data.containsKey(id))
            data.put(id, getRandomFactionMarket(nskr_kestevenQuest.getRandom(), Factions.TRITACHYON, questStageManager.JOB3_MARKET_BLACKLIST));

        return (SectorEntityToken) data.get(id);
    }

    public static SectorEntityToken getJob3Target(){
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = nskr_kestevenQuest.PERSISTENT_KEY+"Target3";
        if (!data.containsKey(id))
            data.put(id, util.getRandomLocationInSystem(getRandomSystemNearCore(nskr_kestevenQuest.getRandom()), false, false, nskr_kestevenQuest.getRandom()));

        return (SectorEntityToken) data.get(id);
    }

    public static SectorEntityToken getJob4FriendlyTarget(){
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = nskr_kestevenQuest.PERSISTENT_KEY+"TargetFriendly4";
        if (!data.containsKey(id))
            data.put(id, util.getRandomLocationInSystem(getRandomSystemFarCore(nskr_kestevenQuest.getRandom()), false, false, nskr_kestevenQuest.getRandom()));

        return (SectorEntityToken) data.get(id);
    }

    public static SectorEntityToken getJob4EnemyTarget(){
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = nskr_kestevenQuest.PERSISTENT_KEY+"TargetEnemy4";
        if (data.containsKey(id)){
            return (SectorEntityToken) data.get(id);
        }
        return null;
    }

    public static SectorEntityToken setJob4EnemyTarget(SectorEntityToken loc){
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = nskr_kestevenQuest.PERSISTENT_KEY+"TargetEnemy4";

        data.put(id, loc);
        return (SectorEntityToken) data.get(id);
    }

    public static StarSystemAPI getJob5FrostTip(){
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = nskr_kestevenQuest.PERSISTENT_KEY+"Job5FrostTip";
        if (!data.containsKey(id))
            data.put(id, getRandomSystemNearLocation(util.getFrost().getStar().getLocationInHyperspace(),7000f,12000f, util.getFrost(), nskr_kestevenQuest.getRandom()));

        return (StarSystemAPI) data.get(id);
    }

    public static SectorEntityToken getElizaLoc(){

        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = nskr_kestevenQuest.PERSISTENT_KEY+"ElizaJob5";

        return (SectorEntityToken) data.get(id);
    }

    public static void setElizaLoc(){
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = nskr_kestevenQuest.PERSISTENT_KEY+"ElizaJob5";
        data.put(id, pickElizaMarket(nskr_kestevenQuest.getRandom(), false));
    }

    public static SectorEntityToken getCacheFleetLoc(){

        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = nskr_kestevenQuest.PERSISTENT_KEY+"CacheFleet";

        return (SectorEntityToken) data.get(id);
    }

    public static void setCacheFleetLoc(){
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = nskr_kestevenQuest.PERSISTENT_KEY+"CacheFleet";
        data.put(id, pickCacheFleetLoc());
    }

    public static int getDialogStage(String id) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, 0);

        return (int)data.get(id);
    }

    public static void setDialogStage(int stage, String id) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(id, stage);
    }

    public static SectorEntityToken getLocation(String id){
        return getLocation(id, null);
    }
    public static SectorEntityToken getLocation(String id, SectorEntityToken defaultLoc){

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, defaultLoc);

        return (SectorEntityToken) data.get(id);
    }
    public static SectorEntityToken setLocation(SectorEntityToken loc, String id){
        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(id, loc);

        return (SectorEntityToken) data.get(id);
    }
    public static Random getRandom(String id) {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) {

            data.put(id,  new Random(new Random().nextLong()));
        }
        return (Random) data.get(id);
    }
}
