package scripts.kissa.LOST_SECTOR.campaign.fleets.bounties;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.ShipRecoveryListener;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.intel.nskr_hintManager;
import scripts.kissa.LOST_SECTOR.campaign.intel.nskr_umbraIntel;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.*;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.*;

public class nskr_eternitySpawner extends BaseCampaignEventListener implements EveryFrameScript, ShipRecoveryListener {
    //
    //spawns a special enigma fleet to a random location in a nebula once per game.
    //
    public static final String FLEET_NAME = "Commander Umbra's Fleet";
    public static final String COM_NAME = "Umbra";
    public static final String FLAGSHIP_VARIANT = "nskr_eternity_e_boss";
    public static final String FS_NAME = "DSRD Shadows Of Tomorrow";
    public static final String FACTION = "enigma";
    public static final String LOOT_KEY = "$EternityLoot";
    public static final String PORTRAIT_SPRITE = "graphics/portraits/nskr_enigma.png";
    public static final String ETERNITY_KEY = "ETERNITY";
    public static final String SAVED_PREFIX = "eternity";
    public static final String LOG_PREFIX = "EternitySpawner";
    public static final String HINT_KEY = "HINT_ETERNITY";
    public static final String FLEET_ARRAY_KEY = "$nskr_eternitySpawnerFleets";

    nskr_saved<Float> counter;
    nskr_saved<Boolean> newGame;
    nskr_saved<Boolean> firstTime;
    private final List<CampaignFleetAPI> removed = new ArrayList<>();
    //CampaignFleetAPI pf;
    Random random;

    //Weights for the different types of locations we can spawn to
    public static final LinkedHashMap<BaseThemeGenerator.LocationType, Float> WEIGHTS = new LinkedHashMap<>();
    static {
        WEIGHTS.put(BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT, 12f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 8f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.JUMP_ORBIT, 4f);
    }
    static void log(final String message) {
        Global.getLogger(nskr_eternitySpawner.class).info(message);
    }

    public nskr_eternitySpawner() {
        super(false);
        //how often we run logic
        this.counter = new nskr_saved<>(SAVED_PREFIX + "Counter", 0.0f);
        this.newGame = new nskr_saved<>(SAVED_PREFIX + "NewGame", true);
        //for intel
        this.firstTime = new nskr_saved<>(SAVED_PREFIX + "FirstTime", true);
        this.random = new Random();
        //listener
        //Global.getSector().getListenerManager().addListener(this, true);
    }

    @Override
    public void advance(float amount) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;
        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);

        if (Global.getSector().isInFastAdvance()) {
            counter.val += 2f*amount;
        } else{
            counter.val += amount;
        }
        //spawn once per game
        if (fleets.isEmpty() && newGame.val) {
            spawnEternityFleet(getLoc());
            newGame.val = false;
        }
        //logic
        if (counter.val>4f) {
            for (fleetInfo f : fleets) {
                CampaignFleetAPI fleet = f.fleet;
                boolean despawn = false;

                //looted
                if (!fleet.getMemoryWithoutUpdate().contains(LOOT_KEY)){
                    despawn = true;
                }

                Vector2f fp = fleet.getLocationInHyperspace();
                Vector2f pp = pf.getLocationInHyperspace();
                float dist = MathUtils.getDistance(pp, fp);
                if (despawn) {
                    if (dist > Global.getSettings().getMaxSensorRangeHyper()) {
                        //tracker for cleaning the list
                        this.removed.add(fleet);
                        fleet.despawn();
                    }
                }
                //logic
                boolean visibleToPlayer = fleet.isVisibleToSensorsOf(pf);
                if(visibleToPlayer && firstTime.val){
                    //Adds our intel
                    nskr_umbraIntel intel = new nskr_umbraIntel(fleet);
                    Global.getSector().getIntelManager().addIntel(intel, false);
                    log(LOG_PREFIX+" added INTEL");

                    Global.getSector().getCampaignUI().addMessage("Initial examinations of the Enigma fleet shows an unusual flagship, the Eternity-Class. Approach with extreme caution.",
                            Global.getSettings().getColor("standardTextColor"),
                            "Eternity-Class",
                            "extreme caution",
                            Global.getSettings().getColor("yellowTextColor"),
                            Global.getSettings().getColor("yellowTextColor"));
                    firstTime.val = false;
                }
            }

            //clean the list
            fleetUtil.cleanUp(removed, fleets);
            removed.clear();
            //save to mem
            fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

            counter.val = 0f;
        }
    }

    void spawnEternityFleet(SectorEntityToken loc) {

        float points = MathUtils.getRandomNumberInRange(80f,85f);

        //apply settings
        points *= nskr_modPlugin.getScriptedFleetSizeMult();

        Random random = new Random();

        //skills
        Map<String, Integer> skills = new HashMap<>();
        skills.put("combat_endurance",2);
        skills.put("damage_control",2);
        skills.put("field_modulation",2);
        skills.put("target_analysis",2);
        skills.put("systems_expertise",2);
        skills.put("missile_specialization",2);
        skills.put("energy_weapon_mastery",2);
        skills.put("ordnance_expert",2);
        //skills com
        skills.put("electronic_warfare",1);
        skills.put("wolfpack_tactics",1);
        skills.put("crew_training",1);

        //memkeys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE);
        keys.add(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS);
        keys.add(MemFlags.FLEET_IGNORES_OTHER_FLEETS);
        keys.add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        keys.add(LOOT_KEY);
        //fs tags
        List<String> fsTags = new ArrayList<>();
        fsTags.add(Tags.SHIP_LIMITED_TOOLTIP);

        //flagship
        simpleFleetMember flagship = new simpleFleetMember(FLAGSHIP_VARIANT, fsTags, true);
        flagship.alwaysRecover = true;
        flagship.name = FS_NAME;

        //commander
        simpleCaptain simpleCaptain = new simpleCaptain("nskr_"+COM_NAME, FACTION, skills);
        simpleCaptain.isAiCore = true;
        simpleCaptain.aiCoreID = "alpha_core";
        simpleCaptain.personality = Personalities.RECKLESS;
        simpleCaptain.portraitSpritePath = PORTRAIT_SPRITE;
        simpleCaptain.rankId = Ranks.SPACE_ADMIRAL;
        simpleCaptain.firstName = COM_NAME;
        simpleCaptain.gender = FullName.Gender.MALE;

        //fleet
        simpleFleet simpleFleet = new simpleFleet(loc, FACTION, points, keys, random);
        //simpleFleet.type = FleetTypes.PATROL_LARGE;
        simpleFleet.maxShipSize = 2;
        simpleFleet.sMods = 3;
        simpleFleet.name = FLEET_NAME;
        simpleFleet.commander = simpleCaptain.create();
        simpleFleet.flagshipInfo = flagship;
        simpleFleet.assignment = FleetAssignment.ORBIT_PASSIVE;
        simpleFleet.assignmentText = "error #506, try again?";

        simpleFleet.aiFleetProperties = true;

        CampaignFleetAPI fleet = simpleFleet.create();

        //makes sure we are not in a star
        questUtil.spawnAwayFromStarFixer(fleet, 2.0f);

        fleet.setFaction(FACTION, true);

        //update
        fleetUtil.update(fleet, random);

        //add to mem IMPORTANT
        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
        fleetInfo info = new fleetInfo(fleet, null, loc);
        info.flagshipSimpleMember = simpleFleet.getFlagshipInfo();
        info.secondaries = simpleFleet.getSecondaryMembers();
        fleets.add(info);
        fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

        log(LOG_PREFIX+" SPAWNED, size " + points + " system " + loc.getContainingLocation().getName()+" loc " + loc.getName());
        log(LOG_PREFIX+" FLEET, loc " + fleet.getStarSystem().getName() +" size "+ fleet.getFleetPoints() + " commander " + fleet.getCommander().getName().getFullName() + " flagship " + fleet.getFlagship().getHullSpec().getBaseHullId());
    }

    //location saved to memory (important for intel))
    public static SectorEntityToken getLoc() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = ETERNITY_KEY;
        if (!data.containsKey(id))
            data.put(id, randomLocation());

        return (SectorEntityToken)data.get(id);
    }

    static SectorEntityToken randomLocation(){
        SectorEntityToken loc = null;
        StarSystemAPI system = null;
        while (loc == null) {
            system = getRandomSystemWithBlacklist();
            if (system == null) {
                //We've somehow blacklisted every system in the sector: just don't spawn anything
                log(LOG_PREFIX+" ERROR system is null");
                return null;
            }
            //Gets a list of random locations in the system, and picks one
            WeightedRandomPicker<BaseThemeGenerator.EntityLocation> validPoints = BaseThemeGenerator.getLocations(new Random(), system, 50f, WEIGHTS);
            BaseThemeGenerator.EntityLocation tLoc = validPoints.pick();
            if (tLoc != null && tLoc.orbit != null && tLoc.orbit.getFocus() !=null) {
                loc = tLoc.orbit.getFocus();
            } else loc = system.getStar();
        }
        return loc;
    }

    private static StarSystemAPI getRandomSystemWithBlacklist() {
        //ban tags
        List<String> banTags = new ArrayList<>();
        banTags.add(Tags.THEME_REMNANT);

        //pick types
        List<StarSystemGenerator.StarSystemType> pickTypes = new ArrayList<>();
        pickTypes.add(StarSystemGenerator.StarSystemType.NEBULA);

        simpleSystem simpleSystem = new simpleSystem(new Random(), 1);
        simpleSystem.pickSystemTypes = pickTypes;
        simpleSystem.blacklistTags = banTags;
        simpleSystem.enforceSystemStarType = true;
        simpleSystem.pickOnlyInProcgen = true;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("picking any nebula system");
        //reset tags
        simpleSystem.blacklistTags = new ArrayList<>();
        //try again
        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("ERROR no valid system");
        return util.getRandomNonCoreSystem(new Random());
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void reportShipsRecovered(List<FleetMemberAPI> ships, InteractionDialogAPI dialog) {

        for (FleetMemberAPI m : ships) {
            ShipVariantAPI v = m.getVariant();

            String id = v.getHullSpec().getBaseHullId();
            if (id.equals("nskr_eternity_e")){

                v.removeTag(Tags.SHIP_LIMITED_TOOLTIP);

                m.setVariant(v, false, false);
            }

        }

    }
}
