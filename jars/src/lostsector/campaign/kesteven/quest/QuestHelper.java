package lostsector.campaign.kesteven.quest;

import lostsector.helper.fleet.SystemPicker;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.kesteven.quest.ElizaSearchBarEvent;
import lostsector.dialogue.rules.nskr_kestevenQuest;
import lostsector.ModPlugin;
import lostsector.settings.Setting;
import lostsector.settings.SettingsManager;
import lostsector.helper.MathHelper;
import lostsector.campaign.enigma.DormantSpawner;
import lostsector.helper.SectorLookup;
import lostsector.helper.SystemHelper;
import org.jetbrains.annotations.Nullable;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class QuestHelper {

    static void log(final String message) {
        Global.getLogger(QuestHelper.class).info(message);
    }

    public static SectorEntityToken spawnArtifact(SectorEntityToken loc, int number) {
        Random random = nskr_kestevenQuest.getRandom();
        LocationAPI containing = loc.getContainingLocation();
        BaseThemeGenerator.EntityLocation createLoc = DerelictThemeGenerator.createLocationAtRandomGap(random, loc, 0f);
        SectorEntityToken artifact = DerelictThemeGenerator.addNonSalvageEntity(containing, createLoc, "nskr_artifact", Factions.NEUTRAL).entity;
        artifact.setDiscoverable(true);
        artifact.setSensorProfile(100f);

        artifact.setCircularOrbitPointingDown(loc, random.nextFloat()*360f, MathUtils.getDistance(artifact.getLocation(), loc.getLocation()), MathHelper.getSeededRandomNumberInRange(30f,60f, random));

        artifact.getMemory().set(QuestStageManager.ARTIFACT_KEY+number, true);

        //makes sure we are not in a star
        SystemHelper.spawnAwayFromStarFixer(artifact, 2.0f);

        log("baseLoc " + loc.getName());
        log("qUtil SPAWNED artifact in " + artifact.getOrbitFocus().getName());
        return artifact;
    }

    public static boolean outpostExists(){
        if (Global.getSector().getEconomy().getMarket("nskr_outpost")==null) return false;
        return Global.getSector().getEconomy().getMarket("nskr_outpost").getFaction().getId().equals("kesteven");
    }

    public static String outpostName(){
        return Global.getSector().getEconomy().getMarket("nskr_outpost").getName();
    }

    private static StarSystemAPI getRandomSystem(Random random) {
        SystemPicker simpleSystem = new SystemPicker(random, 1);
        return simpleSystem.pick();
    }

    //for kesteven quest line
    public static StarSystemAPI getRandomSystemNearCore(Random random) {
        //ban tags
        List<String> banTags = new ArrayList<>();
        banTags.add(Tags.THEME_REMNANT_MAIN);
        banTags.add(Tags.THEME_REMNANT_RESURGENT);
        banTags.add(Tags.THEME_UNSAFE);

        SystemPicker simpleSystem = new SystemPicker(random, 2);
        simpleSystem.maxDistance = 27500f;
        simpleSystem.blacklistTags = banTags;
        simpleSystem.pickOnlyInProcgen = true;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("ERROR no valid system");
        return SystemHelper.getRandomNonCoreSystem(random);
    }

    //for kesteven quest line
    public static StarSystemAPI getRandomSystemFarCore(Random random) {

        //ban tags
        List<String> banTags = new ArrayList<>();
        banTags.add(Tags.THEME_REMNANT_MAIN);
        banTags.add(Tags.THEME_REMNANT_RESURGENT);
        banTags.add(Tags.THEME_UNSAFE);

        SystemPicker simpleSystem = new SystemPicker(random, 2);
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
        return SystemHelper.getRandomNonCoreSystem(random);
    }

    //for kesteven quest line
    public static StarSystemAPI getRandomSystemNearLocation(Vector2f loc, float minDistance, float maxDistance, StarSystemAPI ignore, Random random) {

        //banned systems
        List<StarSystemAPI> banSys = new ArrayList<>();
        banSys.add(ignore);

        SystemPicker simpleSystem = new SystemPicker(random, 1);
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
            return validSystems.get(MathHelper.getSeededRandomNumberInRange(0,validSystems.size()-1, random));
        }
    }

    //for kesteven quest line
    public static StarSystemAPI getRandomSystemWithinConstellation(Constellation constellation, @Nullable StarSystemAPI ignore, int minPlanets, Random random) {

        //banned systems
        List<StarSystemAPI> banSys = new ArrayList<>();
        if (ignore!=null) banSys.add(ignore);

        SystemPicker simpleSystem = new SystemPicker(random, minPlanets);
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
            return validSystems.get(MathHelper.getSeededRandomNumberInRange(0, validSystems.size() - 1, random));
        }
        log("ERROR no systems in constellation");
        return null;
    }

    //for kesteven quest line
    public static StarSystemAPI getRandomSystemWithEnigmaBase(Random random){

        List<String> entities = new ArrayList<>();
        entities.add("nskr_enigmabase");

        SystemPicker simpleSystem = new SystemPicker(random, 1);
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
                if (ElizaSearchBarEvent.getUsedMarkets(ElizaSearchBarEvent.USED_MARKET_KEY).contains(market.getId())) {
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
                return SystemHelper.getRandomFactionMarket(random, Factions.PIRATES, ElizaSearchBarEvent.getUsedMarkets(ElizaSearchBarEvent.USED_MARKET_KEY));
            }
            log("ERROR no valid Eliza markets try again");
            return pickElizaMarket(random, true);
        }
        return validMarkets.get(MathHelper.getSeededRandomNumberInRange(0,validMarkets.size()-1, random)).getPrimaryEntity();
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
            if (e.getId().equals("nskr_cacheGate")){
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

        return sys.createToken(new Vector2f(MathHelper.getSeededRandomNumberInRange(-3000f, 3000f, random), MathHelper.getSeededRandomNumberInRange(-3000f, 3000f, random)));
    }

    public static void saveEnding(){
        SettingsManager.set(Setting.THRONES_GIFT_UNLOCKED, true);
        SettingsManager.set(Setting.STORY_SKIP_UNLOCKED, true);
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (Boolean.TRUE.equals(data.get(ModPlugin.STARFARER_MODE_FROM_START_KEY))) {
            SettingsManager.set(Setting.HELLSPAWN_UNLOCKED, true);
        }
    }

    public static int getStage() {
        String id = QuestStageManager.KESTEVEN_QUEST_KEY;

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, 0);

        return (int)data.get(id);
    }

    public static void setStage(int stage) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(QuestStageManager.KESTEVEN_QUEST_KEY, stage);
    }

    public static int getDisksRecovered() {
        String id = QuestStageManager.DISK_COUNT_KEY;

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, 0);

        return (int)data.get(id);
    }

    public static void setDisksRecovered(int count) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(QuestStageManager.DISK_COUNT_KEY, count);
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
        String id = QuestStageManager.JOB3_TIMER_KEY;

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, QuestStageManager.JOB3_TIME_LIMIT);

        return (float)data.get(id);
    }

    public static void setMissionTimerJob3(float timer) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(QuestStageManager.JOB3_TIMER_KEY, timer);
    }

    public static boolean getEndMissions() {
        String id = QuestStageManager.QUEST_END_KEY;

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, false);

        return (boolean)data.get(id);
    }

    public static void setEndMissions(boolean end) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(QuestStageManager.QUEST_END_KEY, end);
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
            DormantSpawner.addDormant(SystemHelper.getRandomLocationInSystem(sys ,true,false, nskr_kestevenQuest.getRandom()),
                    "enigma", 20f);
        }

        return (StarSystemAPI) data.get(id);
    }

    public static SectorEntityToken getJob3Start(){
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = nskr_kestevenQuest.PERSISTENT_KEY+"Start3";
        if (!data.containsKey(id))
            data.put(id, SystemHelper.getRandomFactionMarket(nskr_kestevenQuest.getRandom(), Factions.TRITACHYON, QuestStageManager.JOB3_MARKET_BLACKLIST));

        return (SectorEntityToken) data.get(id);
    }

    public static SectorEntityToken getJob3Target(){
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = nskr_kestevenQuest.PERSISTENT_KEY+"Target3";
        if (!data.containsKey(id))
            data.put(id, SystemHelper.getRandomLocationInSystem(getRandomSystemNearCore(nskr_kestevenQuest.getRandom()), false, false, nskr_kestevenQuest.getRandom()));

        return (SectorEntityToken) data.get(id);
    }

    public static SectorEntityToken getJob4FriendlyTarget(){
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = nskr_kestevenQuest.PERSISTENT_KEY+"TargetFriendly4";
        if (!data.containsKey(id))
            data.put(id, SystemHelper.getRandomLocationInSystem(getRandomSystemFarCore(nskr_kestevenQuest.getRandom()), false, false, nskr_kestevenQuest.getRandom()));

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
            data.put(id, getRandomSystemNearLocation(SectorLookup.getFrost().getStar().getLocationInHyperspace(),7000f,12000f, SectorLookup.getFrost(), nskr_kestevenQuest.getRandom()));

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
}
