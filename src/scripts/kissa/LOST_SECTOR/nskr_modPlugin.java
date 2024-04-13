package scripts.kissa.LOST_SECTOR;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import com.fs.starfarer.api.impl.campaign.rulecmd.Nex_TransferMarket;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import exerelin.campaign.SectorManager;
import indevo.ids.Ids;
import indevo.industries.artillery.conditions.ArtilleryStationCondition;
import indevo.industries.artillery.scripts.ArtilleryStationScript;
import indevo.industries.artillery.utils.ArtilleryStationPlacer;
import lunalib.lunaSettings.LunaSettings;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.JSONUtils;
import scripts.kissa.LOST_SECTOR.campaign.*;
import scripts.kissa.LOST_SECTOR.campaign.customStart.*;
import scripts.kissa.LOST_SECTOR.campaign.fleets.*;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_abyssSpawner;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_eternitySpawner;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_mothershipSpawner;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_rorqSpawner;
import scripts.kissa.LOST_SECTOR.campaign.fleets.events.nskr_blacksiteManager;
import scripts.kissa.LOST_SECTOR.campaign.fleets.events.nskr_loanShark;
import scripts.kissa.LOST_SECTOR.campaign.intel.nskr_hintManager;
import scripts.kissa.LOST_SECTOR.campaign.fleets.events.nskr_interceptManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.characters.killBrainManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.jobs.contractManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.campaign.loot.nskr_bountyLoot;
import scripts.kissa.LOST_SECTOR.campaign.procgen.*;
import scripts.kissa.LOST_SECTOR.campaign.quests.nskr_kestevenTipBarCreator;
import scripts.kissa.LOST_SECTOR.campaign.util.campaignTimer;
import scripts.kissa.LOST_SECTOR.campaign.util.customCampaignListener;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.ids;
import scripts.kissa.LOST_SECTOR.util.util;
import scripts.kissa.LOST_SECTOR.weapons.ai.nskr_tremorAI;
import scripts.kissa.LOST_SECTOR.campaign.loot.nskr_EnigmaFleetLootGenerator;
import scripts.kissa.LOST_SECTOR.weapons.ai.nskr_emglStuckAI;
import scripts.kissa.LOST_SECTOR.world.nskr_desertFixer;
import scripts.kissa.LOST_SECTOR.world.nskr_gen;
import scripts.kissa.LOST_SECTOR.world.systems.arcadia.nskr_arcadia;
import scripts.kissa.LOST_SECTOR.world.systems.cache.nskr_cache;
import scripts.kissa.LOST_SECTOR.world.systems.frost.nskr_frost;
import scripts.kissa.LOST_SECTOR.world.systems.outpost.nskr_outpost;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*
        ⣿⣿⠏⠁⠄⠄⠄⠄⠄⠄⠤⠤⠤⠄⠄⠄⠄⠄⠄⠄⠄⠐⠶⠄⠉⠻⣿⣿⣿
        ⠁⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⢀⡀⠄⠄⠄⠄⠄⠄⠄⠄⠄⠢⠄⡀⠄⠸⣿⣿
        ⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⣰⣾⠿⠶⠂⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠙⠿
        ⠄⠄⠄⠄⠄⠄⠄⣀⣴⠿⡓⠤⢂⣤⠄⠄⠄⣴⣢⣶⣦⠄⠄⠡⠰⠠⡀⠄⠄
        ⠄⠄⠄⠄⠄⠄⠾⠋⠄⠄⠄⠐⠋⠄⠄⣠⠾⣿⣿⣿⣿⡧⠄⠄⠉⠁⠄⠄⠄
        ⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⣠⣤⣶⣾⣿⣿⣿⣿⣿⣄⠄⠄⠄⠄⠄⠄
        ⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⢺⣿⣿⣿⠟⠛⠛⠉⠉⠭⣭⣷⠄⠄⠄⠄⠄
        ⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⢀⣼⣿⣿⣷⣶⣿⣶⣶⣿⣶⣾⣿⠄⠄⠄⠄⢀
        ⠄⠄⠄⠄⠄⠄⣀⣀⣀⣠⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇⠄⠄⠄⢀⣾
        ⠄⠄⠄⠄⠄⠄⣿⣿⣿⣿⡿⠛⠻⣿⣿⠿⢿⣿⣿⣿⣿⣿⠟⠃⠄⠄⢠⣿⣿
        ⣦⠄⠄⠄⠄⠄⠈⢿⣿⣿⣿⣶⣤⣤⣤⣤⣾⣿⣿⣿⣿⡏⠄⠄⠄⠠⣾⣿⣿
        ⢿⣷⡄⠄⢀⠄⠄⠘⣿⣦⠄⠄⠤⣤⣤⣤⣠⣭⣼⡿⠁⠄⡀⢀⣤⣰⣿⣿⣿
        ⣿⣿⣇⢠⣼⡇⠄⠄⠘⢿⣷⣶⣦⣤⣤⣶⣿⣿⡿⠁⠄⠄⠁⢸⣿⣿⣿⣿⣿
        ⠙⢻⣿⣿⡿⠇⠄⠄⠄⠄⠈⠙⠻⠿⠿⠟⠛⠋⠄⣀⣤⠄⠄⠄⠈⠻⣿⣿⣿
        this mod powered by balls
*/
public class nskr_modPlugin extends BaseModPlugin {

    public static ArrayList<BaseCampaignEventListener> EFS_LIST = new ArrayList<>();

    public static final String SETTINGS_FILE = "LOST_SECTOR_OPTIONS.ini";

    public static final float STARFARER_MODE_SCRIPTED_MULT = 1.2f;
    public static final float STARFARER_MODE_ENIGMA_MULT = 1.25f;

    public static final float EASY_MODE_SCRIPTED_MULT = 0.7f;
    public static final float EASY_MODE_ENIGMA_MULT = 0.6f;

    public static final String STARFARER_MODE_FROM_START_KEY = "nskr_starfarerFromStart";

    public static final String COMPLETED_STORY_KEY = "completedStory";
    public static final String COMPLETED_STORY_HARD_KEY = "completedStoryHard";

    public static boolean IS_NEXELERIN = false;
    public static boolean IS_INDEVO = false;
    public static boolean IS_CC = false;
    public static boolean IS_IRONSHELL = false;
    public static boolean IS_LUNALIB = false;
    public static boolean IS_TAHLAN = false;
    public static boolean IS_EXOTICA = false;
    public static final String EMGL = "nskr_emglShot_sub";
    public static final String TREMOR = "nskr_tremor1";

    //save compat check stuff
    public static final String SAVE_KEY = "nskr_enabled";
    //public static final String VERSION_KEY = "nskr_0.5.0";
    //public static final ArrayList<String> incompatibleVersions = new ArrayList<>();
    //static {
        //incompatibleVersions.add("nskr_0.5.0");
    //}

    static void log(final String message) {
        Global.getLogger(nskr_modPlugin.class).info(message);
    }

    @Override
        public void onApplicationLoad() throws ClassNotFoundException {
        try {
            Global.getSettings().getScriptClassLoader().loadClass("org.dark.shaders.util.ShaderLib");
            ShaderLib.init();
            TextureData.readTextureDataCSV("data/lights/nskr_bump.csv");
            LightData.readLightDataCSV("data/lights/nskr_light.csv");
        } catch (ClassNotFoundException ex) { }

        IS_NEXELERIN = Global.getSettings().getModManager().isModEnabled("nexerelin");
        IS_INDEVO = Global.getSettings().getModManager().isModEnabled("IndEvo");
        IS_CC = Global.getSettings().getModManager().isModEnabled("timid_commissioned_hull_mods");
        IS_IRONSHELL = Global.getSettings().getModManager().isModEnabled("timid_xiv");
        IS_LUNALIB = Global.getSettings().getModManager().isModEnabled("lunalib");
        IS_TAHLAN = Global.getSettings().getModManager().isModEnabled("tahlan");
        IS_EXOTICA = Global.getSettings().getModManager().isModEnabled("exoticatechnologies");

        if (!Global.getSettings().getModManager().isModEnabled("MagicLib")){
            throw new IllegalStateException("You aint got MagicLib loaded bruv, you need it to play with LOST_SECTOR");
        }
        if (!Global.getSettings().getModManager().isModEnabled("lw_lazylib")){
            throw new IllegalStateException("You aint got LazyLib loaded bruv, you need it to play with LOST_SECTOR");
        }
        if (!Global.getSettings().getModManager().isModEnabled("shaderLib")){
            throw new IllegalStateException("You aint got GraphicsLib loaded bruv, you need it to play with LOST_SECTOR");
        }

        if (IS_NEXELERIN) {
            //NEX HACKS
            try {
                List<String> baned = Nex_TransferMarket.NO_TRANSFER_FACTIONS;
                if (!baned.contains(ids.ENIGMA_FACTION_ID)) {
                    baned.add(ids.ENIGMA_FACTION_ID);
                }
            } catch (UnsupportedOperationException ex){ }
        }

        //CONFIG
        createDefaultConfig();
    }

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case EMGL:
                return new PluginPick<MissileAIPlugin>(new nskr_emglStuckAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            case TREMOR:
                return new PluginPick<MissileAIPlugin>(new nskr_tremorAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
        }
        return null;
    }

    public void syncNSKRScripts() {
        if (!Global.getSector().hasScript(nskr_EnigmaFleetLootGenerator.class)) {
            Global.getSector().addScript(new nskr_EnigmaFleetLootGenerator());
            //dumb but has to be done this way, transient just doesn't work, and neither does check for classes lol
            Global.getSector().getListenerManager().addListener(new nskr_crushingDebt());
            Global.getSector().getListenerManager().addListener(new nskr_licensingFees());
            Global.getSector().getListenerManager().addListener(new nskr_comcrewsBonus());
            log("added EnigmaFleetLootGenerator");
        }
        if (!Global.getSector().hasScript(nskr_bountyLoot.class)) {
            Global.getSector().addScript(new nskr_bountyLoot());
            log("added bountyLoot");
        }
        GenericPluginManagerAPI plugins = Global.getSector().getGenericPlugins();
        if (!plugins.hasPlugin(nskr_enigmaDefenderPlugin.class)) {
            plugins.addPlugin(new nskr_enigmaDefenderPlugin(), true);
            log("added enigmaDefenderPlugin");
        }
    }

    private boolean init = false;
    @Override
    public void onGameLoad(boolean newGame) {

        //avoid null manager crash
        if (IS_NEXELERIN && SectorManager.getManager()==null){
            IS_NEXELERIN = false;
        }

        if (!init) {
            //ADD EFS
            //can't just add at applicationLoad because sector is null
            EFS_LIST.add(new nskr_hyperspaceEnigmaSpawner());
            EFS_LIST.add(new nskr_hintManager());
            EFS_LIST.add(new nskr_rorqSpawner());
            EFS_LIST.add(new nskr_eternitySpawner());
            EFS_LIST.add(new nskr_enigmaBlowerUpper());
            EFS_LIST.add(new nskr_stalkerSpawner());
            EFS_LIST.add(new nskr_enigmaRelationsFixer());
            EFS_LIST.add(new nskr_kestevenScavenger());
            EFS_LIST.add(new nskr_kestevenExportManager());
            EFS_LIST.add(new nskr_guardSpawner());
            EFS_LIST.add(new nskr_abyssSpawner());
            EFS_LIST.add(new questStageManager());
            EFS_LIST.add(new nskr_exileManager());
            EFS_LIST.add(new nskr_loanShark());
            EFS_LIST.add(new nskr_interceptManager());
            EFS_LIST.add(new nksr_blackOpsManager());
            EFS_LIST.add(new contractManager());
            EFS_LIST.add(new nskr_enigmaHullmodListener());
            EFS_LIST.add(new nskr_mothershipSpawner());
            EFS_LIST.add(new nskr_blacksiteManager());
            EFS_LIST.add(new nskr_enigmaAIConverter());
            EFS_LIST.add(new gamemodeManager());
            EFS_LIST.add(new thronesGiftManager());
            EFS_LIST.add(new hellSpawnManager());
            EFS_LIST.add(new customCampaignListener());
            //EFS_LIST.add(new killBrainManager());

            if (IS_NEXELERIN){
                EFS_LIST.add(new hellSpawnNexListener());
            }

            init = true;
        }

        //DISPOSABLE FLEET MANAGERS
        if (!Global.getSector().hasScript(hellSpawnDisposableFleetSpawner.class)) {
            Global.getSector().addScript(new hellSpawnDisposableFleetSpawner());
        }
        if (!Global.getSector().hasScript(thronesGiftDisposableFleetSpawner.class)) {
            Global.getSector().addScript(new thronesGiftDisposableFleetSpawner());
        }

        nskr_kestevenMirror.borrowIndieBlueprints();
        nskr_blackOpsSetup.scanWeaponBlueprints();
        syncNSKRScripts();

        Global.getSector().registerPlugin(new corePlugin());

        for (BaseCampaignEventListener script : EFS_LIST){
            Global.getSector().addTransientScript((EveryFrameScript) script);
            Global.getSector().addTransientListener(script);
            Global.getSector().getListenerManager().addListener(script, true);

            //reset Throne's gift kludge
            if (script instanceof thronesGiftManager){
                ((thronesGiftManager) script).reset();
            }
        }

        nskr_saved.loadPersistentData();

        //BAR
        BarEventManager bar = BarEventManager.getInstance();
        if (!bar.hasEventCreator(nskr_kestevenTipBarCreator.class)) {
            bar.addEventCreator(new nskr_kestevenTipBarCreator());
        }

        //DATA
        Map<String, Object> data = Global.getSector().getPersistentData();
        //hard mode
        if (data.containsKey(STARFARER_MODE_FROM_START_KEY)) {
            if (!getStarfarerMode()) data.put(STARFARER_MODE_FROM_START_KEY, false);
        }
        //new save check
        if (!data.containsKey(SAVE_KEY)){
            //spawn stuff
            onNewGame();
            onNewGameAfterProcGen();
            onNewGameAfterEconomyLoad();
            onNewGameAfterTimePass();

            //TODO fix this 4 real
            //stupid temp hack
            MarketAPI asteriaMarket = Global.getSector().getEconomy().getMarket("nskr_asteria");
            if (asteriaMarket!=null) {
                PersonAPI commander = Global.getSector().getFaction("kesteven").createRandomPerson(new Random());
                commander.setRankId(Ranks.SPACE_ADMIRAL);
                commander.setPostId(Ranks.POST_STATION_COMMANDER);
                asteriaMarket.getCommDirectory().addPerson(commander, 3);
                asteriaMarket.addPerson(commander);
            }
        } else {
            //compatible version check
            //String version = (String)data.get(SAVE_KEY);
            //if (incompatibleVersions.contains(version)){
            //    String versionParsed = version.replace("nskr_","");
            //    throw new IllegalStateException("Incompatible version of LOST_SECTOR detected, revert your install to "+versionParsed+" load this save");
            //}
        }

        //HACKS
        fleetUtil.hackBrokenVariants();

    }

    //I HATE JSONS SO MUCH

    public static float getScriptedFleetSizeMult(){
        if (IS_LUNALIB){
            if (LunaSettings.getBoolean(ids.LOST_SECTOR_MOD_ID, "starfarerMode")) return STARFARER_MODE_SCRIPTED_MULT;
            if (LunaSettings.getBoolean(ids.LOST_SECTOR_MOD_ID, "easyMode")) return EASY_MODE_SCRIPTED_MULT;
            return Math.max(LunaSettings.getFloat(ids.LOST_SECTOR_MOD_ID, "scriptedFleetScaling"), 0.1f);
        } else {
            if (getSettingBoolean("starfarerMode")) return STARFARER_MODE_SCRIPTED_MULT;
            if (getSettingBoolean("easyMode")) return EASY_MODE_SCRIPTED_MULT;
            return Math.max((float)getSettingDouble("scriptedFleetScaling"), 0.1f);
        }
    }

    public static float getRandomEnigmaFleetSizeMult(){
        if (IS_LUNALIB){
            if (LunaSettings.getBoolean(ids.LOST_SECTOR_MOD_ID, "starfarerMode")) return STARFARER_MODE_ENIGMA_MULT;
            if (LunaSettings.getBoolean(ids.LOST_SECTOR_MOD_ID, "easyMode")) return EASY_MODE_ENIGMA_MULT;
            return LunaSettings.getFloat(ids.LOST_SECTOR_MOD_ID, "randomEnigmaFleetScaling");
        } else {
            if (getSettingBoolean("starfarerMode")) return STARFARER_MODE_SCRIPTED_MULT;
            if (getSettingBoolean("easyMode")) return EASY_MODE_SCRIPTED_MULT;
            return (float)getSettingDouble("randomEnigmaFleetScaling");
        }
    }

    public static boolean getStarfarerMode(){
        if (IS_LUNALIB){
            if (LunaSettings.getBoolean(ids.LOST_SECTOR_MOD_ID, "starfarerMode")) return true;
        } else {
            if (getSettingBoolean("starfarerMode")) return true;
        }
        return false;
    }

    //Thanks to HzDev for just making this for me
    public static boolean getIndEvoBoolean(String... ids){

        for (String id: ids) {
            try {
                if (IS_LUNALIB){
                    return LunaSettings.getBoolean("IndEvo", id);
                } else {
                    return Global.getSettings().getBoolean(id);
                }
            } catch (RuntimeException ex) {
                log("ERROR - wrong Ind.Evo version");
            }
        }
        return false;
    }

    private static JSONObject loadSettings(){
        try {
            return Global.getSettings().loadJSON(SETTINGS_FILE);
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }
    private static double getSettingDouble(String id) {
        JSONObject settings = loadSettings();
        try {
            return settings.getDouble(id);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    private static boolean getSettingBoolean(String id){
        JSONObject settings = loadSettings();
        try {
            return settings.getBoolean(id);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createDefaultConfig(){

        JSONUtils.CommonDataJSONObject config = null;
        try {
            config = JSONUtils.loadCommonJSON("LOST_SECTOR_cfg.json", "data/config/LOST_SECTOR_cfg.default");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            config.save();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void saveToConfig(String id, Object data){

        JSONUtils.CommonDataJSONObject config = null;
        try {
            config = JSONUtils.loadCommonJSON("LOST_SECTOR_cfg.json", "data/config/LOST_SECTOR_cfg.default");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            config.put(id, data);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            config.save();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static Object loadFromConfig(String id){

        JSONUtils.CommonDataJSONObject config = null;
        try {
            config = JSONUtils.loadCommonJSON("LOST_SECTOR_cfg.json", "data/config/LOST_SECTOR_cfg.default");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            return config.get(id);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beforeGameSave() {

        nskr_saved.updatePersistentData();
        campaignTimer.save();

        for (BaseCampaignEventListener script : EFS_LIST){
            Global.getSector().removeTransientScript((EveryFrameScript) script);
            Global.getSector().removeListener(script);
            Global.getSector().removeScriptsOfClass(script.getClass());
            Global.getSector().getListenerManager().removeListenerOfClass(script.getClass());
            Global.getSector().getListenerManager().removeListener(script);
        }

    }

    @Override
    public void afterGameSave() {

        for (BaseCampaignEventListener script : EFS_LIST){
            Global.getSector().addTransientScript((EveryFrameScript) script);
            Global.getSector().addTransientListener(script);
            Global.getSector().getListenerManager().addListener(script, true);
        }

        nskr_saved.loadPersistentData();

    }

    @Override
    public void onNewGame() {
        ProcgenUsedNames.notifyUsed("Frostbite");
        ProcgenUsedNames.notifyUsed("Newfoundland");
        ProcgenUsedNames.notifyUsed("Greenland");
        ProcgenUsedNames.notifyUsed("Antarctica");
        ProcgenUsedNames.notifyUsed("Permafrost");
        ProcgenUsedNames.notifyUsed("Hailstone");
        ProcgenUsedNames.notifyUsed("Archangel");
        ProcgenUsedNames.notifyUsed("Inari");

        ProcgenUsedNames.notifyUsed("Asteria");
        ProcgenUsedNames.notifyUsed("Bleak");
        ProcgenUsedNames.notifyUsed("Shiver");
        ProcgenUsedNames.notifyUsed("Glacier");
        ProcgenUsedNames.notifyUsed("Siberia");
        ProcgenUsedNames.notifyUsed("Algor");
        ProcgenUsedNames.notifyUsed("Frozen Heart");

        ProcgenUsedNames.notifyUsed("Helios");
        ProcgenUsedNames.notifyUsed("Polaris");

        if (!IS_NEXELERIN || SectorManager.getManager().isCorvusMode()) {
            nskr_arcadia.generate(Global.getSector());
        }
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("kesteven");
        nskr_gen.setKestevenRelation(Global.getSector());

        //new save key
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(SAVE_KEY)){
            //data.put(SAVE_KEY, VERSION_KEY);
            data.put(SAVE_KEY, "Installed");
        }
        //hard mode
        if (!data.containsKey(STARFARER_MODE_FROM_START_KEY)) {
            if (getStarfarerMode()) data.put(STARFARER_MODE_FROM_START_KEY, true);
        }
    }

    @Override
    public void onNewGameAfterProcGen() {
        if (!IS_NEXELERIN || SectorManager.getManager().isCorvusMode()) {
            nskr_frost.generate(Global.getSector());
            //for (int x = 0; x<100;x++)
            nskr_outpost.generate(Global.getSector());
        }
        //once per campaign
        nskr_mothershipSpawner.spawnPlanets(nskr_mothershipSpawner.getMothershipBaseLocation(), new Random());
        nskr_enigmaBaseSpawner.spawnBases();
        nskr_dormantSpawner.spawnDormant();
        nskr_environmentalStorytelling.spawnStorytelling();
        //cache
        nskr_cache.generate(Global.getSector());
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        if (!IS_NEXELERIN || SectorManager.getManager().isCorvusMode()) {
            //spawn the market, has to be done later to work correctly
            nskr_frost.generatePt2(Global.getSector());
        }
        //needs to be done later
        nskr_rogueSpawner.spawnRogues();
    }

    @Override
    public void onNewGameAfterTimePass() {
        //random core workaround
        if (IS_NEXELERIN && !SectorManager.getManager().isCorvusMode()) {
            nskr_frost.generate(Global.getSector());
            nskr_outpost.generate(Global.getSector());
            nskr_frost.generatePt2(Global.getSector());
        }
        //indevo
        if (IS_INDEVO) {

            MarketAPI asteria = Global.getSector().getEconomy().getMarket("nskr_asteria");
            SectorEntityToken outpost = util.getOutpost();

            if (asteria != null && getIndEvoBoolean("IndEvo_Enable_minefields"))
                asteria.addCondition("IndEvo_mineFieldCondition");
            if (asteria != null && getIndEvoBoolean("IndEvo_dryDock"))
                asteria.addIndustry("IndEvo_dryDock");
            if (outpost != null && getIndEvoBoolean("IndEvo_PrivatePort"))
                outpost.getMarket().addIndustry("IndEvo_PrivatePort");

            if (outpost != null && getIndEvoBoolean("IndEvo_Enable_Artillery")) {
                PlanetAPI siberia = null;
                for (PlanetAPI p : Global.getSector().getStarSystem(nskr_frost.getName()).getPlanets()) {
                    if (p.getId().equals("nskr_siberia")) {
                        siberia = p;
                        break;
                    }
                }
                //add railgun surprise
                if (siberia != null) {
                    ArtilleryStationScript script = new ArtilleryStationScript(siberia.getMarket());
                    script.setDestroyed(false);
                    siberia.getMarket().getMemoryWithoutUpdate().set(ArtilleryStationScript.TYPE_KEY, "railgun");
                    siberia.addScript(script);
                    siberia.getMemoryWithoutUpdate().set(ArtilleryStationScript.SCRIPT_KEY, script);
                    siberia.getMarket().addTag(Ids.TAG_ARTILLERY_STATION);
                    siberia.getContainingLocation().addTag(Ids.TAG_SYSTEM_HAS_ARTILLERY);

                    siberia.getMarket().addCondition(ArtilleryStationCondition.ID);

                    StarSystemAPI system = siberia.getStarSystem();
                    if (system.getEntitiesWithTag(Ids.TAG_WATCHTOWER).isEmpty()) {
                        ArtilleryStationPlacer.placeWatchtowers(system, ids.ENIGMA_FACTION_ID);
                    }
                }
            }
        }

        //ppl
        nskr_gen.genPeople();
        //add ruins to frost planets, has to be done after sectorGen
        nskr_frost.generateRuins(Global.getSector().getStarSystem(nskr_frost.getName()));
        //fix frozen desert conditions
        nskr_desertFixer.fix();
        //blacksites, done later so we can use sector memory
        nskr_blacksiteSpawner.spawnBases();

        //mothership fleet
        nskr_mothershipSpawner.spawnMothershipFleet(nskr_mothershipSpawner.getMothershipBaseLocation(), new Random());

        nskr_gen.setEnigmaRelation(Global.getSector());
    }

}

