//////////////////////
//blendColors by theDragn from HTE
//BiasFunction by Sebastian Lague
//////////////////////
package scripts.kissa.LOST_SECTOR.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;

import java.awt.*;
import java.util.*;
import java.util.List;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.procgen.nskr_dormantSpawner;
import scripts.kissa.LOST_SECTOR.campaign.nskr_exileManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleSystem;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.world.systems.frost.nskr_frost;

public class util {

    //

    public static final Color TT_ORANGE = new Color(255, 109, 31, 255);
    public static final Color BON_GREEN = new Color(142, 255, 21, 255);
    public static final Color NICE_YELLOW = new Color(255, 219, 36, 255);
    public static final Color EXOTICA_RED = new Color(141, 42, 38, 255);

    static void log(final String message) {
        Global.getLogger(util.class).info(message);
    }

    public static int clamp255(int x) {
        return Math.max(0, Math.min(255, x));
    }

    public static Color colorJitter(Color color, float amount) {
        return new Color(clamp255(color.getRed() + (int) (((float) Math.random() - 0.5f) * amount)),
                clamp255(color.getGreen() + (int) (((float) Math.random() - 0.5f) * amount)),
                clamp255(color.getBlue() + (int) (((float) Math.random() - 0.5f) * amount)),
                color.getAlpha());
    }

    public static Color blendColors(Color c1, Color c2, float ratio) {
        float iRatio = 1.0f - ratio;
        int a1 = c1.getAlpha();
        int r1 = c1.getRed();
        int g1 = c1.getGreen();
        int b1 = c1.getBlue();
        int a2 = c2.getAlpha();
        int r2 = c2.getRed();
        int g2 = c2.getGreen();
        int b2 = c2.getBlue();
        int a = (int)((float)a1 * iRatio + (float)a2 * ratio);
        int r = (int)((float)r1 * iRatio + (float)r2 * ratio);
        int g = (int)((float)g1 * iRatio + (float)g2 * ratio);
        int b = (int)((float)b1 * iRatio + (float)b2 * ratio);
        return new Color(r, g, b, a);
    }

    public static Color randomiseColor(Color inputColor, int rShift, int gShift, int bShift, int aShift, boolean addition){
        int rShift2 = rShift;
        int gShift2 = gShift;
        int bShift2 = bShift;
        int aShift2 = aShift;

        if (addition){
            rShift = 0;
            gShift = 0;
            bShift = 0;
            aShift = 0;
        } else {
            rShift = -rShift;
            gShift = -gShift;
            bShift = -bShift;
            aShift = -aShift;
        }

        int r = inputColor.getRed() + MathUtils.getRandomNumberInRange(rShift,rShift2);
        if (r>255){r = 255;} else if(r<0) r = 0;
        int g = inputColor.getGreen() + MathUtils.getRandomNumberInRange(gShift,gShift2);
        if (g>255){g = 255;} else if(g<0) g = 0;
        int b = inputColor.getBlue() + MathUtils.getRandomNumberInRange(bShift,bShift2);
        if (b>255){b = 255;} else if(b<0) b = 0;
        int a = inputColor.getAlpha() + MathUtils.getRandomNumberInRange(aShift,aShift2);
        if (a>255){a = 255;} else if(a<0) a = 0;

        return new Color(r,g,b,a);
    }

    public static Color shiftAlpha(Color color, float mult){
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = Math.min(Math.round(color.getAlpha()*mult), 255);

        return new Color(r,g,b,a);
    }

    public static StarSystemAPI getFrost(){
        return Global.getSector().getStarSystem(nskr_frost.getName());
    }

    public static boolean enigmaExists(){

        MarketAPI market = Global.getSector().getEconomy().getMarket("nskr_heart");
        if (market == null) return false;
        if (market.getFactionId()==null) return false;
        if (!market.getFactionId().equals("enigma")) return false;

        return true;
    }

    public static boolean kestevenExists(){
        return Misc.getFactionMarkets(ids.KESTEVEN_FACTION_ID).size() > 0;
    }

    public static boolean AsteriaExists(){
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

    public static PersonAPI getJack(){
        boolean exiled = nskr_exileManager.getExiled(nskr_exileManager.EXILE_KEY);
        if (getAsteria()==null && !exiled) return null;
        if (getOutpost()==null && exiled) return null;
        return Global.getSector().getImportantPeople().getPerson("nskr_opguy");
    }

    public static PersonAPI getAlice(){
        boolean exiled = nskr_exileManager.getExiled(nskr_exileManager.EXILE_KEY);
        if (getAsteria()==null && !exiled) return null;
        if (getOutpost()==null && exiled) return null;
        return Global.getSector().getImportantPeople().getPerson("nskr_researcher");
    }

    public static PersonAPI getNick(){
        if (getOutpost()==null) return null;
        return Global.getSector().getImportantPeople().getPerson("nskr_intelligence");
    }

    public static PersonAPI getMichael(){
        boolean exiled = nskr_exileManager.getExiled(nskr_exileManager.EXILE_KEY);
        if (getAsteria()==null && !exiled) return null;
        if (getOutpost()==null && exiled) return null;
        return Global.getSector().getImportantPeople().getPerson("nskr_president");
    }

    public static PersonAPI getEliza(){
        return Global.getSector().getImportantPeople().getPerson("nskr_anarchist");
    }

    public static boolean hasCCBonus(){
        boolean CC = false;

        CampaignFleetAPI fleet =  Global.getSector().getPlayerFleet();
        if (fleet==null) return false;

        //fleet check
        for (FleetMemberAPI ship : fleet.getFleetData().getMembersListCopy()){
            Collection<String> mods = ship.getVariant().getHullMods();
            for (String mod : mods){
                if (mod.equals("CHM_kesteven")){
                    CC = true;
                    break;
                }
            }
            if (CC) break;
        }
        return CC;
    }

    public static float getDMods(ShipVariantAPI v){
       return DModManager.getNumDMods(v);
    }

    public static boolean isLogistics(EnumSet<ShipHullSpecAPI.ShipTypeHints> hints){
        boolean logi = false;
        for (ShipHullSpecAPI.ShipTypeHints hint : hints){
            if (hint == ShipHullSpecAPI.ShipTypeHints.CIVILIAN){
                logi = true;
                break;
            }
            if (hint == ShipHullSpecAPI.ShipTypeHints.FREIGHTER){
                logi = true;
                break;
            }
            if (hint == ShipHullSpecAPI.ShipTypeHints.TANKER){
                logi = true;
                break;
            }
            if (hint == ShipHullSpecAPI.ShipTypeHints.LINER){
                logi = true;
                break;
            }
            if (hint == ShipHullSpecAPI.ShipTypeHints.TRANSPORT){
                logi = true;
                break;
            }

        }
        return logi;
    }

    //public static boolean hasTag(Set<String> tags, String tag){
    //    boolean has = false;
    //    for (String t : tags){
    //        if (t.equals(tag)){
    //            has = true;
    //            break;
    //        }
    //    }
    //    return has;
    //}


    //backup for blacklist generation, this one just returns a random non-core system
    public static StarSystemAPI getRandomNonCoreSystem(Random random) {

        simpleSystem simpleSystem = new simpleSystem(random, 1);
        simpleSystem.pickOnlyInProcgen = true;

        if (!simpleSystem.get().isEmpty()) {
            return simpleSystem.pick();
        }
        log("ERROR bruh you blacklisted every system in the game");
        return null;
    }

    //backup for blacklist generation, this one just returns a random core system with a market
    public static StarSystemAPI getRandomSystemWithMarket(Random random) {
        //pick tags
        List<String> pickTags = new ArrayList<>();
        pickTags.add(Tags.THEME_CORE);
        pickTags.add(Tags.THEME_CORE_POPULATED);

        simpleSystem simpleSystem = new simpleSystem(random, 1);
        simpleSystem.allowCore = true;
        simpleSystem.allowMarkets = true;
        simpleSystem.pickOnlyMarket = true;

        simpleSystem.pickTags = pickTags;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("ERROR no valid system");
        return util.getRandomNonCoreSystem(random);
    }

    public static SectorEntityToken getRandomMarket(Random random, boolean allowPiratesEtc){

        ArrayList<MarketAPI> markets = new ArrayList<>();
        for (StarSystemAPI system : Global.getSector().getStarSystems()){
            for (SectorEntityToken e : system.getAllEntities()){
                if (e.getMarket() == null) continue;
                if (e.getMarket().getFactionId() == null) continue;
                if (e.getMarket().isPlanetConditionMarketOnly()) continue;
                if (e.getMarket().isHidden()) continue;
                if (e.getMarket().getFactionId().equals(Factions.NEUTRAL)) continue;
                if (!allowPiratesEtc) {
                    if (e.getMarket().getFaction().getId().equals(Factions.PIRATES) || e.getMarket().getFaction().getId().equals(Factions.LUDDIC_PATH)) continue;
                }
                markets.add(e.getMarket());
            }
        }
        if (markets.isEmpty()){
            log("ERROR no random market in the sector, what...");
            return null;
        }
        MarketAPI randomMarket = markets.get(mathUtil.getSeededRandomNumberInRange(0, markets.size()-1, random));

        return randomMarket.getPrimaryEntity();
    }

    public static boolean hasGate(StarSystemAPI sys){
        boolean gate = false;
        for (SectorEntityToken e : sys.getAllEntities()){
            if (e.getCustomEntityType()==null)continue;
            if (!e.getCustomEntityType().equals(Entities.INACTIVE_GATE)) continue;
            gate = true;
            log("has GATE "+e.getName());
            break;
        }
        return gate;
    }

    public static boolean hasRelay(StarSystemAPI sys){
        boolean relay = false;
        for (SectorEntityToken e : sys.getAllEntities()){
            if (e.getCustomEntityType()==null)continue;
            if (!e.getCustomEntityType().equals(Entities.COMM_RELAY)) continue;
            relay = true;
            log("has RELAY "+e.getName());
            break;
        }
        return relay;
    }

    public static SectorEntityToken getRelay(StarSystemAPI sys){
        SectorEntityToken relay = null;
        for (SectorEntityToken e : sys.getAllEntities()){
            if (e.getCustomEntityType()==null)continue;
            if (!e.getCustomEntityType().equals(Entities.COMM_RELAY)) continue;
            relay = e;
            break;
        }
        return relay;
    }

    public static SectorEntityToken addDormant(SectorEntityToken loc, String factionId, float combatPoints) {
        return addDormant(loc,factionId, combatPoints, combatPoints, 0f,1f,1f,0f,0,0);
    }

    public static SectorEntityToken addDormant(SectorEntityToken loc, String factionId, float minCombatPoints, float maxCombatPoints, float qualityChance, float minQuality, float maxQuality, float SmodChance, int minSmod, int maxSmod) {
        CampaignFleetAPI fleet;
        StarSystemAPI system = loc.getStarSystem();
        String name;

        float combatPoints = MathUtils.getRandomNumberInRange(minCombatPoints, maxCombatPoints);

        String type = "patrolSmall";
        name = "Splinter";
        if (combatPoints > 30f) {
            type = "patrolMedium";
            name = "Combine";
        }
        if (combatPoints > 60f) {
            type = "patrolLarge";
            name = "Swarm";
        }

        //apply settings
        combatPoints *= nskr_modPlugin.getScriptedFleetSizeMult();

        //fine?
        if (combatPoints<=0f) return null;

        final FleetParamsV3 params = new FleetParamsV3(
                new Vector2f(), factionId, 1f, type, combatPoints, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);

        if (Math.random()<qualityChance) params.qualityOverride = MathUtils.getRandomNumberInRange(minQuality, maxQuality);
        if (Math.random()<SmodChance) params.averageSMods = MathUtils.getRandomNumberInRange(minSmod, maxSmod);
        params.withOfficers = true;

        fleet = FleetFactoryV3.createFleet(params);
        system.addEntity(fleet);
        RemnantSeededFleetManager.initRemnantFleetProperties(params.random, fleet, true);

        fleet.setTransponderOn(true);

        float dist = loc.getRadius() * MathUtils.getRandomNumberInRange(2.00f, 2.50f);
        fleet.setCircularOrbit(loc, (float)Math.random() * 360.0f, dist, MathUtils.getRandomNumberInRange(60f,120f));
        fleet.setFacing((float)Math.random() * 360.0f);

        //enigma dormants
        if (factionId.equals("enigma")) {
            fleet.getMemoryWithoutUpdate().set(nskr_dormantSpawner.DORMANT_KEY, true);
            fleet.setName(name);

            //make the officers AI cores
            fleetUtil.setAIOfficers(fleet);
        }

        //makes sure we are not in a star
        questUtil.spawnAwayFromStarFixer(fleet, 2.0f);

        //update
        fleetUtil.update(fleet, new Random());

        log("DORMANT added in "+loc.getContainingLocation().getName()+" to "+loc.getName());

        return fleet;
    }

    public static SectorEntityToken getRandomLocationInSystem(StarSystemAPI system, boolean allowStar, boolean allowOuterSystem, Random random){
        LinkedHashMap<BaseThemeGenerator.LocationType, Float> weights = new LinkedHashMap<>();
        weights.put(BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT, 8f);
        weights.put(BaseThemeGenerator.LocationType.IN_ASTEROID_FIELD, 8f);
        weights.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 8f);
        weights.put(BaseThemeGenerator.LocationType.JUMP_ORBIT, 8f);
        weights.put(BaseThemeGenerator.LocationType.IN_SMALL_NEBULA, 8f);
        if (allowOuterSystem) {
            weights.put(BaseThemeGenerator.LocationType.OUTER_SYSTEM, 8f);
        }
        //pls stop spawning stuff into star coronas, im begging
        if (allowStar) {
            weights.put(BaseThemeGenerator.LocationType.STAR_ORBIT, 8f);
            weights.put(BaseThemeGenerator.LocationType.NEAR_STAR, 8f);
            weights.put(BaseThemeGenerator.LocationType.IN_ASTEROID_BELT, 8f);
            weights.put(BaseThemeGenerator.LocationType.IN_RING, 8f);
            weights.put(BaseThemeGenerator.LocationType.L_POINT, 8f);
        }
        //log(system.getName());

        SectorEntityToken target = null;
        int maxTries = 200;
        for (int x = 0; x < maxTries; x++) {
            //Gets a list of random locations in the system, and picks one
            WeightedRandomPicker<BaseThemeGenerator.EntityLocation> validPoints = BaseThemeGenerator.getLocations(random, system, 50f, weights);
            BaseThemeGenerator.EntityLocation tTarget = validPoints.pick();
            if (tTarget != null && tTarget.orbit != null && tTarget.orbit.getFocus() !=null) {
                //pick star anyways if we can't find other spot
                if(x>(maxTries/2)){
                    target = tTarget.orbit.getFocus();
                    break;
                }
                if (!allowStar && tTarget.orbit.getFocus().isStar()){
                    continue;
                }
                target = tTarget.orbit.getFocus();
                break;
            }
        }
        if(target==null){
            if (!allowStar || !allowOuterSystem){
                log("ERROR randomLocationInSystem target is null, RETRY");
                target = getRandomLocationInSystem(system, true, true, random);
            } else {
                log("ERROR randomLocationInSystem target is null, return centre");
                target = system.getCenter();
            }
        }
        return target;
    }



    public static OrbitAPI createRandomNearOrbit(SectorEntityToken loc){
        return Global.getFactory().createCircularOrbit(loc, (float)Math.random() * 360.0f, MathUtils.getRandomNumberInRange(150f, 550f), MathUtils.getRandomNumberInRange(12,24));
    }

    public static PersonAPI setOfficerSkills(PersonAPI officer, Map<String, Integer> skills){

        //reset
        List<MutableCharacterStatsAPI.SkillLevelAPI> ogSkills = officer.getStats().getSkillsCopy();
        for (MutableCharacterStatsAPI.SkillLevelAPI s : ogSkills){
            officer.getStats().setSkillLevel(s.getSkill().getId(), 0.0f);
        }
        //set
        for (String s : skills.keySet()) {
            officer.getStats().setSkillLevel(s, (float)skills.get(s));
            //log("OFFICER skill "+ s + " lvl " + (float)skills.get(s));
        }

        officer.getStats().setLevel(skills.size());
        officer.getStats().refreshCharacterStatsEffects();

        return officer;
    }

    public static Map<String, Integer> createRandomSkills(int level, float eliteSkillChance, Random random){
        Set<String> allSkills = new HashSet<>();
        allSkills.add(Skills.POLARIZED_ARMOR);
        allSkills.add(Skills.ENERGY_WEAPON_MASTERY);
        allSkills.add(Skills.DAMAGE_CONTROL);
        allSkills.add(Skills.HELMSMANSHIP);
        allSkills.add(Skills.BALLISTIC_MASTERY);
        allSkills.add(Skills.ORDNANCE_EXPERTISE);
        allSkills.add(Skills.IMPACT_MITIGATION);
        allSkills.add(Skills.GUNNERY_IMPLANTS);
        allSkills.add(Skills.TARGET_ANALYSIS);
        allSkills.add(Skills.SYSTEMS_EXPERTISE);
        allSkills.add(Skills.MISSILE_SPECIALIZATION);
        allSkills.add(Skills.COMBAT_ENDURANCE);
        allSkills.add(Skills.FIELD_MODULATION);
        allSkills.add(Skills.POINT_DEFENSE);

        Map<String, Integer> skills = new HashMap<>();
        while (skills.size()<level) {
            float size = allSkills.size();
            for (String s : allSkills) {
                if (!skills.containsKey(s) && random.nextFloat()<1f/size){
                    skills.put(s, getEliteSkillChance(random, eliteSkillChance));
                    log("util added skill "+s);
                }
            }
        }
        return skills;
    }
    public static int getEliteSkillChance(Random random, float chance){
        if (random.nextFloat()<chance) return 1;
        return 2;
    }

    //Really fucking cursed workarounds to get enigma and prot ships
    //Because, ships lose all tags on save & reload ????????
    public static boolean isProtTech(FleetMemberAPI member){
        if (member.getVariant()==null) return false;
        if (member.getVariant().getHullMods()==null || member.getVariant().getHullMods().isEmpty())  return false;
        boolean prot = false;
        for (String m : member.getVariant().getHullMods()){
            if (m.equals("nskr_focused_shield") || m.equals("nskr_kaboom")){
                prot = true;
                break;
            }
        }
        return prot;
    }
    public static boolean isProtTech(ShipAPI ship){
        if (ship.getVariant()==null) return false;
        if (ship.getVariant().getHullMods()==null || ship.getVariant().getHullMods().isEmpty())  return false;
        boolean prot = false;
        for (String m : ship.getVariant().getHullMods()){
            if (m.equals("nskr_focused_shield") || m.equals("nskr_kaboom")){
                prot = true;
                break;
            }
        }
        return prot;
    }
    public static String protOrEnigma(ShipAPI ship){
        if (ship.getVariant()==null) return null;
        if (ship.getVariant().getHullMods()==null || ship.getVariant().getHullMods().isEmpty())  return null;
        //prot
        for (String m : ship.getVariant().getHullMods()){
            if (m.equals("nskr_lost_prot")){
                return "prot";
            }
        }
        //enigma
        for (String m : ship.getVariant().getHullMods()){
            if (m.equals("nskr_domain_era")){
                return "enigma";
            }
        }
        return null;
    }
    public static String protOrEnigma(FleetMemberAPI member){
        if (member.getVariant()==null) return null;
        if (member.getVariant().getHullMods()==null || member.getVariant().getHullMods().isEmpty())  return null;
        //prot
        for (String m : member.getVariant().getHullMods()){
            if (m.equals("nskr_lost_prot")){
                return "prot";
            }
        }
        //enigma
        for (String m : member.getVariant().getHullMods()){
            if (m.equals("nskr_domain_era")){
                return "enigma";
            }
        }
        return null;
    }

    public static float getDistanceFromNearestSystem(Vector2f loc){
        float shortestDist = Float.MAX_VALUE;
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (system.getHyperspaceAnchor( )== null) continue;
            if (system.hasTag(Tags.THEME_HIDDEN)) continue;
            float dist =  MathUtils.getDistance(loc, system.getHyperspaceAnchor().getLocationInHyperspace());
            if (dist>shortestDist) continue;
            shortestDist = dist;
        }
        return shortestDist;
    }

    public static StarSystemAPI getNearestSystem(Vector2f loc){
        float shortestDist = Float.MAX_VALUE;
        StarSystemAPI sys = null;
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (system.getHyperspaceAnchor() == null) continue;
            if (system.hasTag(Tags.THEME_HIDDEN)) continue;
            float dist =  MathUtils.getDistance(loc, system.getHyperspaceAnchor().getLocationInHyperspace());
            if (dist>shortestDist) continue;
            shortestDist = dist;
            sys = system;
            //log("sys "+sys.getName()+" dist "+dist);
        }
        return sys;
    }

    public static String parseCustom(String primary, String hl){
        int loopCount = 0;
        int indexInSequence = 0;
        while (primary.contains("%s")){
            String h = "";
            if (hl.contains("|")){
                if (loopCount==0){
                    h = (String)hl.subSequence(0, hl.indexOf("|")-1);
                    indexInSequence = hl.indexOf("|")+1;
                }
                if (loopCount>=1){
                    log("index1 "+indexInSequence);
                    if(hl.indexOf("|", indexInSequence)>0) {
                        log("index2 "+indexInSequence);
                        h = (String) hl.subSequence(indexInSequence+1, hl.indexOf("|", indexInSequence) - 1);
                        indexInSequence = hl.indexOf("|", indexInSequence)+1;
                        log("index3 "+indexInSequence);
                    } else {
                        h = (String) hl.subSequence(indexInSequence+1, hl.length());
                    }
                }
            } else h= hl;
            primary = primary.replaceFirst("%s", h);
            loopCount++;
        }

        return primary;
    }

    public static long getSeedParsed(){
        String seed = Global.getSector().getSeedString();
        String prefix = seed.substring(0,2);
        seed = seed.replace(prefix, "");

        //if (Global.getSector()!=null && Global.getSector().getClock()!=null) {
        //    String clockSeed = ""+Global.getSector().getClock().getTimestamp();
        //    clockSeed = clockSeed.replaceAll("-","");
        //    clockSeed = clockSeed.replaceAll("0","");
        //    seed = seed + clockSeed;
        //    //log("TIMESTAMP "+clockSeed);
        //}
        //while (seed.length()>18){
        //    seed = seed.replace(seed.substring(0,1), "");
        //}
        return Long.parseLong(seed);
    }

    public static Color setAlpha(Color color, int alpha) {
        return new Color(color.getRed(),color.getGreen(),color.getBlue(),clamp255(alpha));
    }

    public static final ArrayList<String> GREEK_LETTERS = new ArrayList<>();
    static {
        GREEK_LETTERS.add("alpha");
        GREEK_LETTERS.add("beta");
        GREEK_LETTERS.add("gamma");
        GREEK_LETTERS.add("delta");
        GREEK_LETTERS.add("epsilon");
        GREEK_LETTERS.add("zeta");
        GREEK_LETTERS.add("eta");
        GREEK_LETTERS.add("theta");
        GREEK_LETTERS.add("iota");
        GREEK_LETTERS.add("kappa");
        GREEK_LETTERS.add("lambda");
        GREEK_LETTERS.add("mu");
        GREEK_LETTERS.add("nu");
        GREEK_LETTERS.add("xi");
        GREEK_LETTERS.add("omicron");
        GREEK_LETTERS.add("pi");
        GREEK_LETTERS.add("rho");
        GREEK_LETTERS.add("sigma");
        GREEK_LETTERS.add("tau");
        GREEK_LETTERS.add("upsilon");
        GREEK_LETTERS.add("phi");
        GREEK_LETTERS.add("chi");
        GREEK_LETTERS.add("psi");
        GREEK_LETTERS.add("omega");
    }

    public static String getRandomGreekLetter(Random random, boolean capitalized) {
        if (!capitalized) {
            return GREEK_LETTERS.get(mathUtil.getSeededRandomNumberInRange(0, GREEK_LETTERS.size() - 1, random));
        } else {
            return capitalizeFirstLetter(GREEK_LETTERS.get(mathUtil.getSeededRandomNumberInRange(0, GREEK_LETTERS.size() - 1, random)));
        }
    }

    public static String capitalizeFirstLetter(String string) {
        String first = string.substring(0,1);
        String capitalized = first.toUpperCase();
        return string.replaceFirst(first, capitalized);
    }

    public static SectorEntityToken swapSalvageEntity(SectorEntityToken from, String to, Random random){
        StarSystemAPI sys = from.getStarSystem();
        SectorEntityToken focus = from.getOrbitFocus();
        float angle = from.getCircularOrbitAngle();
        float period = from.getCircularOrbitPeriod();
        float radius = from.getCircularOrbitRadius();
        float facing = from.getFacing();

        SectorEntityToken toEntity = BaseThemeGenerator.addSalvageEntity(random, from.getStarSystem().getStar().getContainingLocation(), to, Factions.NEUTRAL);
        toEntity.setCircularOrbitPointingDown(focus, angle, radius, period);
        toEntity.setFacing(facing);

        from.setExpired(true);
        sys.removeEntity(from);

        return toEntity;
    }

    public static SectorEntityToken swapEntity(SectorEntityToken from, String to){
        StarSystemAPI sys = from.getStarSystem();
        SectorEntityToken focus = from.getOrbitFocus();
        float angle = from.getCircularOrbitAngle();
        float period = from.getCircularOrbitPeriod();
        float radius = from.getCircularOrbitRadius();
        float facing = from.getFacing();

        BaseThemeGenerator.EntityLocation loc = new BaseThemeGenerator.EntityLocation();
        loc.location = from.getLocation();
        loc.type = BaseThemeGenerator.LocationType.NEAR_STAR;

        SectorEntityToken toEntity = BaseThemeGenerator.addNonSalvageEntity(from.getStarSystem().getStar().getContainingLocation(), loc, to, Factions.NEUTRAL).entity;
        toEntity.setCircularOrbitPointingDown(focus, angle, radius, period);
        toEntity.setFacing(facing);

        from.setExpired(true);
        sys.removeEntity(from);

        return toEntity;
    }

    public static boolean hasNeutronStar(StarSystemAPI sys) {
        for (SectorEntityToken e : sys.getAllEntities()){
            if (e instanceof PlanetAPI) {
                if (!e.isStar()) continue;
                if (((PlanetAPI) e).getTypeId()==null) continue;
                if (((PlanetAPI) e).getTypeId().equals(StarTypes.NEUTRON_STAR)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void playUiStaticNoise(){
        Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);
    }
    public static void playUiRepRaiseNoise(){
        Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);
    }
    public static void playUiRepDropNoise(){
        Global.getSoundPlayer().playUISound("ui_rep_drop",1f,1f);
    }

    public static float getLinearMod(ShipAPI ship){
        return getLinearMod(ship, 1f);
    }
    public static float getLinearMod(ShipAPI ship, float mult){
        float mod = 1f;
        switch (ship.getHullSize()){
            case FIGHTER:
                mod = 0.5f;
                break;
            case FRIGATE:
                mod = 1.0f;
                break;
            case DESTROYER:
                mod = 2.0f;
                break;
            case CRUISER:
                mod = 4.0f;
                break;
            case CAPITAL_SHIP:
                mod = 8.0f;
                break;
        }
        return mod * mult;
    }
}
