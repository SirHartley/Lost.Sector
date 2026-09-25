package lostsector;

import lostsector.campaign.CorePlugin;
import lostsector.campaign.enigma.DormantSpawner;
import lostsector.campaign.enigma.EnigmaAIConverter;
import lostsector.campaign.enigma.EnigmaBaseSpawner;
import lostsector.campaign.enigma.EnigmaDefenderPlugin;
import lostsector.campaign.enigma.EnigmaHullmodListener;
import lostsector.campaign.enigma.EnigmaRelations;
import lostsector.campaign.enigma.GuardSpawner;
import lostsector.campaign.enigma.HeartOccupation;
import lostsector.campaign.enigma.HyperspaceEnigmaSpawner;
import lostsector.campaign.enigma.StalkerSpawner;
import lostsector.campaign.events.DerelictTeaserSpawner;
import lostsector.campaign.events.EnvironmentalStorytelling;
import lostsector.campaign.events.blacksite.BlacksiteSpawner;
import lostsector.campaign.kesteven.BlackOpsBlueprints;
import lostsector.campaign.kesteven.BlackOpsManager;
import lostsector.campaign.kesteven.CommissionedCrewsBonus;
import lostsector.campaign.kesteven.ExileManager;
import lostsector.campaign.kesteven.KestevenBlueprints;
import lostsector.campaign.kesteven.KestevenExportManager;
import lostsector.campaign.kesteven.KestevenScavenger;
import lostsector.campaign.kesteven.LicensingFees;
import lostsector.campaign.kesteven.loans.CrushingDebt;
import lostsector.campaign.starts.GameModeManager;
import lostsector.campaign.starts.hellspawn.HellSpawnDisposableFleetSpawner;
import lostsector.campaign.starts.hellspawn.HellSpawnManager;
import lostsector.campaign.starts.hellspawn.HellSpawnNexListener;
import lostsector.campaign.starts.thronesgift.ThronesGiftDisposableFleetSpawner;
import lostsector.campaign.starts.thronesgift.ThronesGiftManager;

import lostsector.persistence.Saved;
import lostsector.settings.Difficulty;
import lostsector.settings.SettingsManager;

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
import indevo.industries.artillery.conditions.ArtilleryStationCondition;
import indevo.industries.artillery.scripts.ArtilleryStationScript;
import indevo.industries.artillery.utils.ArtilleryStationPlacer;
import lostsector.campaign.bounties.abyss.AbyssSpawner;
import lostsector.campaign.bounties.eternity.EternitySpawner;
import lostsector.campaign.bounties.mothership.MothershipSpawner;
import lostsector.campaign.bounties.peacekeepers.RorqualSpawner;
import lostsector.campaign.events.blacksite.BlacksiteManager;
import lostsector.campaign.events.InterceptManager;
import lostsector.campaign.kesteven.loans.LoanShark;
import lostsector.campaign.events.hints.HintManager;
import lostsector.campaign.enigma.EnigmaFleetLoot;
import lostsector.campaign.bounties.BountyLoot;
import lostsector.campaign.kesteven.contracts.ContractManager;
import lostsector.campaign.kesteven.tips.KestevenTipBarEventCreator;
import lostsector.campaign.kesteven.quest.QuestStageManager;
import lostsector.persistence.CampaignTimer;
import lostsector.helper.FleetHelper;
import lostsector.helper.Ids;
import lostsector.helper.SectorLookup;
import lostsector.combat.systems.PhaseCloakCodexLinks;
import lostsector.combat.weapons.ai.EmpGrenadeAI;
import lostsector.combat.weapons.ai.TremorAI;
import lostsector.world.DesertConditionRepair;
import lostsector.world.SectorGen;
import lostsector.world.systems.asteria.Asteria;
import lostsector.world.systems.cache.Cache;
import lostsector.world.systems.frost.Frost;
import lostsector.world.systems.outpost.Outpost;
import lunalib.lunaSettings.LunaSettings;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ModPlugin extends BaseModPlugin {

    public static final ArrayList<BaseCampaignEventListener> EFS_LIST = new ArrayList<>();

    public static final String STARFARER_MODE_FROM_START_KEY = "nskr_starfarerFromStart";

    public static boolean IS_NEXERELIN = false;
    public static boolean IS_INDEVO = false;
    public static boolean IS_CC = false;
    public static boolean IS_IRONSHELL = false;
    public static boolean IS_TAHLAN = false;
    private static final String INDEVO_MOD_ID = "IndEvo";
    public static final String EMP_GRENADE_PROJECTILE = "nskr_emglShot_sub";
    public static final String TREMOR_PROJECTILE = "nskr_tremor1";

    // A save without this key receives the mod's world generation on load.
    public static final String SAVE_KEY = "nskr_enabled";

    static void log(final String message) {
        Global.getLogger(ModPlugin.class).info(message);
    }

    @Override
        public void onApplicationLoad() throws ClassNotFoundException {
        try {
            Global.getSettings().getScriptClassLoader().loadClass("org.dark.shaders.util.ShaderLib");
            ShaderLib.init();
            TextureData.readTextureDataCSV("data/lights/nskr_bump.csv");
            LightData.readLightDataCSV("data/lights/nskr_light.csv");
        } catch (ClassNotFoundException ex) { }

        IS_NEXERELIN = Global.getSettings().getModManager().isModEnabled("nexerelin");
        IS_INDEVO = Global.getSettings().getModManager().isModEnabled(INDEVO_MOD_ID);
        IS_CC = Global.getSettings().getModManager().isModEnabled("timid_commissioned_hull_mods");
        IS_IRONSHELL = Global.getSettings().getModManager().isModEnabled("timid_xiv");
        IS_TAHLAN = Global.getSettings().getModManager().isModEnabled("tahlan");

        if (IS_NEXERELIN) {
            // Keep Enigma out of Nexerelin's market transfers.
            try {
                List<String> bannedFactions = Nex_TransferMarket.NO_TRANSFER_FACTIONS;
                if (!bannedFactions.contains(Ids.ENIGMA_FACTION_ID)) {
                    bannedFactions.add(Ids.ENIGMA_FACTION_ID);
                }
            } catch (UnsupportedOperationException ex){ }
        }

        //CONFIG
        SettingsManager.load();
    }

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case EMP_GRENADE_PROJECTILE:
                return new PluginPick<MissileAIPlugin>(new EmpGrenadeAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            case TREMOR_PROJECTILE:
                return new PluginPick<MissileAIPlugin>(new TremorAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
        }
        return null;
    }

    @Override
    public void onCodexDataGenerated() {
        PhaseCloakCodexLinks.link();
    }

    public void syncNSKRScripts() {
        if (!Global.getSector().hasScript(EnigmaFleetLoot.class)) {
            Global.getSector().addScript(new EnigmaFleetLoot());
            // Added behind the EnigmaFleetLoot check: transient registration and listener class checks do not work for these.
            Global.getSector().getListenerManager().addListener(new CrushingDebt());
            Global.getSector().getListenerManager().addListener(new LicensingFees());
            Global.getSector().getListenerManager().addListener(new CommissionedCrewsBonus());
            log("added EnigmaFleetLootGenerator");
        }
        if (!Global.getSector().hasScript(BountyLoot.class)) {
            Global.getSector().addScript(new BountyLoot());
            log("added bountyLoot");
        }
        GenericPluginManagerAPI plugins = Global.getSector().getGenericPlugins();
        if (!plugins.hasPlugin(EnigmaDefenderPlugin.class)) {
            plugins.addPlugin(new EnigmaDefenderPlugin(), true);
            log("added enigmaDefenderPlugin");
        }
    }

    // Managers keep per-save state in instance fields, Saved values and CampaignTimers, which read
    // the sector's persistent data when constructed, so every load builds new instances.
    private static void createManagers() {
        Saved.clearRegistry();
        CampaignTimer.clearInstances();
        EFS_LIST.clear();

        EFS_LIST.add(new HyperspaceEnigmaSpawner());
        EFS_LIST.add(new HintManager());
        EFS_LIST.add(new RorqualSpawner());
        EFS_LIST.add(new EternitySpawner());
        EFS_LIST.add(new HeartOccupation());
        EFS_LIST.add(new StalkerSpawner());
        EFS_LIST.add(new EnigmaRelations());
        EFS_LIST.add(new KestevenScavenger());
        EFS_LIST.add(new KestevenExportManager());
        EFS_LIST.add(new GuardSpawner());
        EFS_LIST.add(new AbyssSpawner());
        EFS_LIST.add(new QuestStageManager());
        EFS_LIST.add(new ExileManager());
        EFS_LIST.add(new LoanShark());
        EFS_LIST.add(new InterceptManager());
        EFS_LIST.add(new BlackOpsManager());
        EFS_LIST.add(new ContractManager());
        EFS_LIST.add(new EnigmaHullmodListener());
        EFS_LIST.add(new MothershipSpawner());
        EFS_LIST.add(new BlacksiteManager());
        EFS_LIST.add(new EnigmaAIConverter());
        EFS_LIST.add(new GameModeManager());
        EFS_LIST.add(new ThronesGiftManager());
        EFS_LIST.add(new HellSpawnManager());

        if (IS_NEXERELIN){
            EFS_LIST.add(new HellSpawnNexListener());
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {

        //avoid null manager crash
        if (IS_NEXERELIN && SectorManager.getManager()==null){
            IS_NEXERELIN = false;
        }

        createManagers();

        //DISPOSABLE FLEET MANAGERS
        if (!Global.getSector().hasScript(HellSpawnDisposableFleetSpawner.class)) {
            Global.getSector().addScript(new HellSpawnDisposableFleetSpawner());
        }
        if (!Global.getSector().hasScript(ThronesGiftDisposableFleetSpawner.class)) {
            Global.getSector().addScript(new ThronesGiftDisposableFleetSpawner());
        }

        KestevenBlueprints.borrowIndieBlueprints();
        BlackOpsBlueprints.scanWeaponBlueprints();
        syncNSKRScripts();

        Global.getSector().registerPlugin(new CorePlugin());

        for (BaseCampaignEventListener script : EFS_LIST){
            Global.getSector().addTransientScript((EveryFrameScript) script);
            Global.getSector().addTransientListener(script);
            Global.getSector().getListenerManager().addListener(script, true);
        }

        Saved.loadPersistentData();

        //BAR
        BarEventManager bar = BarEventManager.getInstance();
        if (!bar.hasEventCreator(KestevenTipBarEventCreator.class)) {
            bar.addEventCreator(new KestevenTipBarEventCreator());
        }

        //DATA
        Map<String, Object> data = Global.getSector().getPersistentData();
        //hard mode
        Difficulty.clearStarfarerFromStartUnlessStarfarer();
        //new save check
        if (!data.containsKey(SAVE_KEY)){
            //spawn stuff
            onNewGame();
            onNewGameAfterProcGen();
            onNewGameAfterEconomyLoad();
            onNewGameAfterTimePass();

            // Adding the mod to an existing save does not give Asteria a station commander.
            MarketAPI asteriaMarket = Global.getSector().getEconomy().getMarket("nskr_asteria");
            if (asteriaMarket!=null) {
                PersonAPI commander = Global.getSector().getFaction("kesteven").createRandomPerson(new Random());
                commander.setRankId(Ranks.SPACE_ADMIRAL);
                commander.setPostId(Ranks.POST_STATION_COMMANDER);
                asteriaMarket.getCommDirectory().addPerson(commander, 3);
                asteriaMarket.addPerson(commander);
            }
        }

        FleetHelper.hackBrokenVariants();
    }

    //Thanks to HzDev for just making this for me
    public static boolean getIndEvoBoolean(String... ids){
        for (String id : ids) {
            Boolean value = LunaSettings.getBoolean(INDEVO_MOD_ID, id);
            if (value != null) return value;
        }
        for (String id : ids) {
            try {
                return Global.getSettings().getBoolean(id);
            } catch (RuntimeException ex) {
                log("ERROR - wrong Ind.Evo version");
            }
        }
        return false;
    }

    @Override
    public void beforeGameSave() {

        Saved.updatePersistentData();
        CampaignTimer.save();

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

        Saved.loadPersistentData();

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

        if (!IS_NEXERELIN || SectorManager.getManager().isCorvusMode()) {
            Asteria.generate(Global.getSector());
        }
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("kesteven");
        SectorGen.setKestevenRelation(Global.getSector());

        //new save key
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(SAVE_KEY)){
            data.put(SAVE_KEY, "Installed");
        }
        //hard mode
        if (!data.containsKey(STARFARER_MODE_FROM_START_KEY)) {
            if (Difficulty.isStarfarer()) data.put(STARFARER_MODE_FROM_START_KEY, true);
        }
    }

    @Override
    public void onNewGameAfterProcGen() {
        if (!IS_NEXERELIN || SectorManager.getManager().isCorvusMode()) {
            Frost.generate(Global.getSector());
            Outpost.generate(Global.getSector());
            Asteria.generateInRandomSystemIfMissing(Global.getSector());
        }
        //once per campaign
        MothershipSpawner.spawnPlanets(MothershipSpawner.getMothershipBaseLocation(), new Random());
        EnigmaBaseSpawner.spawnBases();
        DormantSpawner.spawnDormant();
        EnvironmentalStorytelling.spawnStorytelling();
        //cache
        Cache.generate(Global.getSector());
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        if (!IS_NEXERELIN || SectorManager.getManager().isCorvusMode()) {
            //spawn the market, has to be done later to work correctly
            Frost.generatePt2(Global.getSector());
        }
        //needs to be done later
        DerelictTeaserSpawner.spawnRogues();
    }

    @Override
    public void onNewGameAfterTimePass() {
        //random core workaround
        if (IS_NEXERELIN && !SectorManager.getManager().isCorvusMode()) {
            Frost.generate(Global.getSector());
            Outpost.generate(Global.getSector());
            Asteria.generateInRandomSystemIfMissing(Global.getSector());
            Frost.generatePt2(Global.getSector());
        }
        //indevo
        if (IS_INDEVO) {

            MarketAPI asteria = Global.getSector().getEconomy().getMarket("nskr_asteria");
            SectorEntityToken outpost = SectorLookup.getOutpost();

            if (asteria != null && getIndEvoBoolean("IndEvo_Enable_minefields"))
                asteria.addCondition("IndEvo_mineFieldCondition");
            if (asteria != null && getIndEvoBoolean("IndEvo_dryDock"))
                asteria.addIndustry("IndEvo_dryDock");
            if (outpost != null && getIndEvoBoolean("IndEvo_PrivatePort"))
                outpost.getMarket().addIndustry("IndEvo_PrivatePort");

            if (outpost != null && getIndEvoBoolean("IndEvo_Enable_Artillery")) {
                PlanetAPI siberia = null;
                for (PlanetAPI p : Global.getSector().getStarSystem(Frost.getName()).getPlanets()) {
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
                    siberia.getMarket().addTag(indevo.ids.Ids.TAG_ARTILLERY_STATION);
                    siberia.getContainingLocation().addTag(indevo.ids.Ids.TAG_SYSTEM_HAS_ARTILLERY);

                    siberia.getMarket().addCondition(ArtilleryStationCondition.ID);

                    StarSystemAPI system = siberia.getStarSystem();
                    if (system.getEntitiesWithTag(indevo.ids.Ids.TAG_WATCHTOWER).isEmpty()) {
                        ArtilleryStationPlacer.placeWatchtowers(system, Ids.ENIGMA_FACTION_ID);
                    }
                }
            }
        }

        //ppl
        SectorGen.genPeople();
        //add ruins to frost planets, has to be done after sectorGen
        Frost.generateRuins(Global.getSector().getStarSystem(Frost.getName()));
        //fix frozen desert conditions
        DesertConditionRepair.fix();
        //blacksites, done later so we can use sector memory
        BlacksiteSpawner.spawnBases();

        //mothership fleet
        MothershipSpawner.spawnMothershipFleet(MothershipSpawner.getMothershipBaseLocation(), new Random());

        SectorGen.setEnigmaRelation(Global.getSector());
    }

}

