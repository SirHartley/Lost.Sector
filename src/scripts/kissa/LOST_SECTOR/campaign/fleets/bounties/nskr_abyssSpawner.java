package scripts.kissa.LOST_SECTOR.campaign.fleets.bounties;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.ShipRecoveryListener;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.intel.nskr_abyssIntel;
import scripts.kissa.LOST_SECTOR.campaign.intel.nskr_hintManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.*;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.*;

public class nskr_abyssSpawner extends BaseCampaignEventListener implements EveryFrameScript, ShipRecoveryListener {
    //
    //spawns a special remnant fleet to a random location in a red giant with remnants, once per game.
    //

    public static final float BOUNTY_PAYOUT = 600000f;
    public static final String FLEET_NAME = "Void Group";
    public static final String COM_NAME = "Lucius";
    public static final String FLAGSHIP_VARIANT = "nskr_reverie_boss_std";
    public static final String FLAGSHIP_SHIP = "nskr_reverie_boss";
    public static final String SECONDARY_VARIANT_1 = "nskr_harbinger_boss_std";
    public static final String SECONDARY_1_SHIP = "nskr_harbinger_boss";
    public static final String SECONDARY_VARIANT_2 = "nskr_afflictor_boss_std";
    public static final int SECONDARY_VARIANT_2_COUNT = 2;
    public static final String SECONDARY_2_SHIP = "nskr_afflictor_boss";
    public static final String FS_NAME = "Piercing Darkness";
    public static final String FACTION = "remnant";
    public static final String LOOT_KEY = "$AbyssLoot";
    public static final String PORTRAIT_SPRITE = "graphics/portraits/nskr_lucius.png";
    public static final String MEMORY_KEY = "ABYSS";
    public static final String SAVED_PREFIX = "abyss";
    public static final String LOG_PREFIX = "abyssSpawner";
    public static final String HINT_KEY = "HINT_ABYSS";
    public static final String FLEET_ARRAY_KEY = "$nskr_abyssSpawnerFleets";

    nskr_saved<Float> counter;
    nskr_saved<Boolean> newGame;
    nskr_saved<Boolean> firstTime;
    private final List<CampaignFleetAPI> removed = new ArrayList<>();
    //CampaignFleetAPI pf;
    Random random;

    //Weights for the different types of locations we can spawn to
    public static final LinkedHashMap<BaseThemeGenerator.LocationType, Float> WEIGHTS = new LinkedHashMap<>();
    static {
        WEIGHTS.put(BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT, 4f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 8f);
        WEIGHTS.put(BaseThemeGenerator.LocationType.JUMP_ORBIT, 8f);
    }
    static void log(final String message) {
        Global.getLogger(nskr_abyssSpawner.class).info(message);
    }

    public nskr_abyssSpawner() {
        super(false);
        //how often we run logic
        this.counter = new nskr_saved<>(SAVED_PREFIX + "Counter", 0.0f);
        this.newGame = new nskr_saved<>(SAVED_PREFIX + "NewGame", true);
        //for intel
        this.firstTime = new nskr_saved<>(SAVED_PREFIX + "FirstTime", true);
        this.random = new Random();
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
            spawnAbyssFleet(getLoc());
            newGame.val = false;
        }
        //logic
        if (counter.val>4f) {
            for (fleetInfo f : fleets) {
                CampaignFleetAPI fleet = f.fleet;
                boolean despawn = false;

                if (!hasBountyShips(fleet)){
                    despawn = true;
                    log(LOG_PREFIX+" fleet is GONE");
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
                    nskr_abyssIntel intel = new nskr_abyssIntel(fleet);
                    Global.getSector().getIntelManager().addIntel(intel, false);
                    log(LOG_PREFIX+" added INTEL");

                    Global.getSector().getCampaignUI().addMessage("Initial examinations of the Remnant fleet shows an unusual flagship, the Hollow-Class. Approach with extreme caution.",
                            Global.getSettings().getColor("standardTextColor"),
                            "Hollow-Class",
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

    void spawnAbyssFleet(SectorEntityToken loc) {

        float points = MathUtils.getRandomNumberInRange(135f,145f);

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
        skills.put("impact_mitigation",2);
        //skills com
        skills.put("electronic_warfare",1);
        skills.put("crew_training",1);
        if (nskr_modPlugin.getStarfarerMode()) skills.put("wolfpack_tactics",1);

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
        fsTags.add(Tags.TAG_AUTOMATED_NO_PENALTY);
        fsTags.add(Tags.VARIANT_UNRESTORABLE);
        fsTags.add(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
        fsTags.add(Tags.SHIP_LIMITED_TOOLTIP);
        //permamods
        List<String> permamods = new ArrayList<>();
        permamods.add(HullMods.AUTOMATED);

        //flagship
        simpleFleetMember flagship = new simpleFleetMember(FLAGSHIP_VARIANT, fsTags, true);
        flagship.alwaysRecover = true;
        flagship.hullmods = permamods;
        flagship.name = FS_NAME;

        //secondaries
        List<simpleFleetMember> secondaries = new ArrayList<>();
        simpleFleetMember secondary;
        AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
        //harbinger
        secondary = new simpleFleetMember(SECONDARY_VARIANT_1, fsTags, true);
        secondary.hullmods = permamods;
        secondary.captain = plugin.createPerson("alpha_core", FACTION, random);
        secondaries.add(secondary);
        //afflictor
        for (int x = 0; x<SECONDARY_VARIANT_2_COUNT; x++) {
            secondary = new simpleFleetMember(SECONDARY_VARIANT_2, fsTags, true);
            secondary.hullmods = permamods;
            secondary.captain = plugin.createPerson("alpha_core", FACTION, random);
            secondaries.add(secondary);
        }

        //commander
        simpleCaptain simpleCaptain = new simpleCaptain("nskr_"+COM_NAME, FACTION, skills);
        simpleCaptain.isAiCore = true;
        simpleCaptain.aiCoreID = "alpha_core";
        simpleCaptain.personality = Personalities.RECKLESS;
        simpleCaptain.portraitSpritePath = PORTRAIT_SPRITE;
        simpleCaptain.rankId = Ranks.UNKNOWN;
        simpleCaptain.firstName = COM_NAME;
        simpleCaptain.gender = FullName.Gender.FEMALE;

        //fleet
        simpleFleet simpleFleet = new simpleFleet(loc, FACTION, points, keys, random);
        //simpleFleet.type = FleetTypes.PATROL_LARGE;
        simpleFleet.maxShipSize = 3;
        simpleFleet.sMods = 3;
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.name = FLEET_NAME+" "+util.getRandomGreekLetter(random, true);
        simpleFleet.noFactionInName = true;
        simpleFleet.commander = simpleCaptain.create();
        simpleFleet.flagshipInfo = flagship;
        simpleFleet.secondaries = secondaries;
        simpleFleet.assignment = FleetAssignment.PATROL_SYSTEM;
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

        log(LOG_PREFIX+" SPAWNED, size " + points +" system " + loc.getContainingLocation().getName() + " loc " + loc.getName());
        log(LOG_PREFIX+" FLEET, loc " + fleet.getStarSystem().getName() +" size "+ fleet.getFleetPoints() + " commander " + fleet.getCommander().getName().getFullName() + " flagship " + fleet.getFlagship().getHullSpec().getBaseHullId());
    }

    //location saved to memory (important for intel))
    public static SectorEntityToken getLoc() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        String id = MEMORY_KEY;
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

        //pick tags
        List<String> pickTags = new ArrayList<>();
        pickTags.add(Tags.THEME_REMNANT);

        //pick types
        List<String> pickTypes = new ArrayList<>();
        pickTypes.add(StarTypes.RED_GIANT);

        simpleSystem simpleSystem = new simpleSystem(new Random(), 1);
        simpleSystem.pickStars = pickTypes;
        simpleSystem.pickTags = pickTags;
        simpleSystem.enforceSystemStarType = true;
        simpleSystem.pickOnlyInProcgen = true;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("picking any red giant system");
        //reset tags
        simpleSystem.pickTags = new ArrayList<>();
        //try again
        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("ERROR no valid system");
        return util.getRandomNonCoreSystem(new Random());
    }

    //check if we recovered any ships
    public static boolean hasBountyShips(CampaignFleetAPI fleet){
        boolean ships = false;

        for (FleetMemberAPI m : fleet.getFleetData().getMembersListWithFightersCopy()){
            String id = m.getHullSpec().getBaseHullId();
            if (id.equals(FLAGSHIP_SHIP) || id.equals(SECONDARY_1_SHIP) || id.equals(SECONDARY_2_SHIP)){
                ships = true;
                break;
            }
        }
        return ships;
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
            if (id.equals("nskr_reverie_boss") || id.equals("nskr_afflictor_boss") || id.equals("nskr_harbinger_boss")){

                v.removeTag(Tags.SHIP_LIMITED_TOOLTIP);

                m.setVariant(v, false, false);
            }
        }

    }
}
