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
import lostsector.dialogue.rules.nskr_kestevenQuest;
import lostsector.ModPlugin;
import lostsector.settings.Setting;
import lostsector.settings.SettingsManager;
import lostsector.helper.MathHelper;
import lostsector.campaign.enigma.DormantSpawner;
import lostsector.helper.SectorLookup;
import lostsector.helper.SystemHelper;
import lostsector.quest.QuestContext;
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
                if (ElizaSearchBarEvent.getUsedMarkets().contains(market.getId())) {
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
                return SystemHelper.getRandomFactionMarket(random, Factions.PIRATES, ElizaSearchBarEvent.getUsedMarkets());
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

    // Wrappers over KestevenState, kept for the old callers until T15 and later tasks switch them. Reads return the
    // old defaults while the state does not exist yet; writes then log an error and change nothing.

    public static int getStage() {
        KestevenState state = KestevenQuest.state();
        return (state == null ? KestevenStage.NOT_STARTED : state.stage()).toLegacy();
    }

    // The quest manager is the only stage writer. The old int writes were idempotent, while the manager logs an
    // advance to the current stage as an error, so an unchanged stage is skipped here.
    public static void setStage(int stage) {
        KestevenStage to = KestevenStage.fromLegacy(stage);
        QuestContext<KestevenStage, KestevenState> ctx = KestevenQuest.context();
        if (ctx != null && ctx.stage() != to) ctx.advance(to);
    }

    public static boolean getCompleted(KestevenFlag flag) {
        KestevenState state = KestevenQuest.state();
        return state != null && state.has(flag);
    }

    public static void setCompleted(boolean completed, KestevenFlag flag) {
        QuestContext<KestevenStage, KestevenState> ctx = KestevenQuest.context();
        if (ctx == null) return;
        if (completed) {
            ctx.set(flag);
        } else {
            ctx.clear(flag);
        }
    }

    public static boolean getFailed(KestevenFlag flag) {
        return getCompleted(flag);
    }

    public static void setFailed(boolean failed, KestevenFlag flag) {
        setCompleted(failed, flag);
    }

    // Other features keep their own sector persistent-data flags through these two; questline flags use the
    // KestevenFlag overloads.
    public static boolean getCompleted(String id) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, false);

        return (boolean)data.get(id);
    }

    public static void setCompleted(boolean completed, String id) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(id, completed);
    }

    public static boolean getEndMissions() {
        return getCompleted(KestevenFlag.ENDED);
    }

    public static void setEndMissions(boolean end) {
        setCompleted(end, KestevenFlag.ENDED);
    }

    public static int getDisksRecovered() {
        KestevenState state = KestevenQuest.state();
        return state == null ? 0 : state.disksRecovered;
    }

    public static void setDisksRecovered(int count) {
        KestevenState state = writableState();
        if (state != null) state.disksRecovered = count;
    }

    public static float getMissionTimerJob3() {
        KestevenState state = KestevenQuest.state();
        return state == null ? QuestStageManager.JOB3_TIME_LIMIT : state.job3TimeLeft;
    }

    public static void setMissionTimerJob3(float timer) {
        KestevenState state = writableState();
        if (state != null) state.job3TimeLeft = timer;
    }

    public static int getNicholasDialogStage() {
        KestevenState state = KestevenQuest.state();
        return state == null ? 0 : state.nicholasDialogStage;
    }

    public static void setNicholasDialogStage(int stage) {
        KestevenState state = writableState();
        if (state != null) state.nicholasDialogStage = stage;
    }

    public static int getJob4FleetDialogStage() {
        KestevenState state = KestevenQuest.state();
        return state == null ? 0 : state.job4FleetDialogStage;
    }

    public static void setJob4FleetDialogStage(int stage) {
        KestevenState state = writableState();
        if (state != null) state.job4FleetDialogStage = stage;
    }

    public static float getTtPayout() {
        KestevenState state = KestevenQuest.state();
        return state == null ? 0f : state.ttPayout;
    }

    public static void setTtPayout(float payout) {
        KestevenState state = writableState();
        if (state != null) state.ttPayout = payout;
    }

    public static StarSystemAPI getJob1Tip(){
        KestevenState state = writableState();
        if (state == null) return null;
        if (state.job1TipSystem == null) {
            StarSystemAPI sys = getRandomSystemWithEnigmaBase(nskr_kestevenQuest.getRandom());
            //NO VALID SYSTEMS
            if (sys==null) return null;

            state.job1TipSystem = sys;
            //add dormant
            DormantSpawner.addDormant(SystemHelper.getRandomLocationInSystem(sys ,true,false, nskr_kestevenQuest.getRandom()),
                    "enigma", 20f);
        }

        return state.job1TipSystem;
    }

    public static SectorEntityToken getJob3Start(){
        KestevenState state = writableState();
        if (state == null) return null;
        if (state.job3Start == null)
            state.job3Start = SystemHelper.getRandomFactionMarket(nskr_kestevenQuest.getRandom(), Factions.TRITACHYON, QuestStageManager.JOB3_MARKET_BLACKLIST);

        return state.job3Start;
    }

    public static SectorEntityToken getJob3Target(){
        KestevenState state = writableState();
        if (state == null) return null;
        if (state.job3Target == null)
            state.job3Target = SystemHelper.getRandomLocationInSystem(getRandomSystemNearCore(nskr_kestevenQuest.getRandom()), false, false, nskr_kestevenQuest.getRandom());

        return state.job3Target;
    }

    public static SectorEntityToken getJob4FriendlyTarget(){
        KestevenState state = writableState();
        if (state == null) return null;
        if (state.job4FriendlyTarget == null)
            state.job4FriendlyTarget = SystemHelper.getRandomLocationInSystem(getRandomSystemFarCore(nskr_kestevenQuest.getRandom()), false, false, nskr_kestevenQuest.getRandom());

        return state.job4FriendlyTarget;
    }

    public static SectorEntityToken getJob4EnemyTarget(){
        KestevenState state = KestevenQuest.state();
        return state == null ? null : state.job4EnemyTarget;
    }

    public static SectorEntityToken setJob4EnemyTarget(SectorEntityToken loc){
        KestevenState state = writableState();
        if (state == null) return null;
        state.job4EnemyTarget = loc;
        return loc;
    }

    public static StarSystemAPI getJob5FrostTip(){
        KestevenState state = writableState();
        if (state == null) return null;
        if (state.job5FrostTipSystem == null)
            state.job5FrostTipSystem = getRandomSystemNearLocation(SectorLookup.getFrost().getStar().getLocationInHyperspace(),7000f,12000f, SectorLookup.getFrost(), nskr_kestevenQuest.getRandom());

        return state.job5FrostTipSystem;
    }

    public static SectorEntityToken getElizaLoc(){
        KestevenState state = KestevenQuest.state();
        return state == null ? null : state.elizaMarket;
    }

    public static void setElizaLoc(){
        KestevenState state = writableState();
        if (state != null) state.elizaMarket = pickElizaMarket(nskr_kestevenQuest.getRandom(), false);
    }

    public static SectorEntityToken getCacheFleetLoc(){
        KestevenState state = KestevenQuest.state();
        return state == null ? null : state.cacheGuardianSpot;
    }

    public static void setCacheFleetLoc(){
        KestevenState state = writableState();
        if (state != null) state.cacheGuardianSpot = pickCacheFleetLoc();
    }

    // The state for a write: logs an error through the quest manager and returns null while it does not exist.
    static KestevenState writableState() {
        return KestevenQuest.context() == null ? null : KestevenQuest.state();
    }
}
