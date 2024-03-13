package scripts.kissa.LOST_SECTOR.campaign.fleets.events;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.fleetInfo;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleFleet;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.*;

import java.util.*;

public class nskr_blacksiteManager extends BaseCampaignEventListener implements EveryFrameScript {

    //
    //

    public static final float DESPAWN_TIMER = 30f;
    public static final float TIME_TO_DESTROY = 7f;

    public static final String DEFENDER_KEY = "$blackOpsDefender";

    public static final String PERSISTENT_RANDOM_KEY = "nskr_blacksiteManagerRandom";
    public static final String FLEET_ARRAY_KEY = "$nskr_blacksiteManagerFleets";
    public static final String SITE_ARRAY_KEY = "$nskr_blacksiteManagerSites";

    public static final String PIRATE_ENTITY_ID = "nskr_blacksite_pirate";
    public static final String PATHER_ENTITY_ID = "nskr_blacksite_pather";
    public static final String KESTEVEN_ENTITY_ID = "nskr_blacksite_kesteven";
    public static final String TRITACHYON_ENTITY_ID = "nskr_blacksite_tritachyon";
    public static final String ENIGMA_ENTITY_ID = "nskr_blacksite_enigma";
    public static final String REMNANT_ENTITY_ID = "nskr_blacksite_remnant";

    nskr_saved<Float> counter;
    private final List<CampaignFleetAPI> removedFleets = new ArrayList<>();
    private final List<blacksiteInfo> removedSites = new ArrayList<>();
    //CampaignFleetAPI pf;
    Random random;

    static void log(final String message) {
        Global.getLogger(nskr_blacksiteManager.class).info(message);
    }

    public nskr_blacksiteManager() {
        super(false);
        //how often we run logic
        this.counter = new nskr_saved<>("blacksiteManagerCounter", 0.0f);
        this.random = new Random();
        //init randoms
        getRandom();
    }

    @Override
    public void advance(float amount) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;

        if (Global.getSector().isInFastAdvance()) {
            counter.val += 2f*amount;
        } else{
            counter.val += amount;
        }

        //logic
        if (counter.val>1f) {

            List<blacksiteInfo> sites = getSites(SITE_ARRAY_KEY);
            for (blacksiteInfo site : sites) {

                int neutralized = 0;
                List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY+site.id);
                for (fleetInfo f : fleets) {
                    CampaignFleetAPI fleet = f.fleet;
                    //timer
                    f.age += 0.1f;
                    boolean despawn = false;
                    boolean run = false;

                    if (site.destroyCounter > TIME_TO_DESTROY && !site.destroyed) {

                        destroy(site.entity);

                        site.destroyed = true;
                    }

                    if (fleet.getFleetPoints() <= 0f) {
                        despawn = true;
                    }
                    if (f.age > DESPAWN_TIMER) {
                        despawn = true;
                    }
                    if (fleet.getFleetPoints() < f.strength*0.20f){
                        neutralized++;
                        run = true;
                    }

                    Vector2f fp = fleet.getLocationInHyperspace();
                    Vector2f pp = pf.getLocationInHyperspace();
                    float dist = MathUtils.getDistance(pp, fp);
                    if (despawn) {
                        if (dist > Global.getSettings().getMaxSensorRangeHyper()) {
                            //tracker for cleaning the list
                            this.removedFleets.add(fleet);
                            fleet.despawn();
                        }
                    }
                    //stop here when defeated
                    if (despawn) continue;
                    //assignment logic
                    FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
                    if (curr == null) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.HOLD, fleet.getContainingLocation().createToken(fleet.getLocation()), Float.MAX_VALUE, "holding");
                        log("null assignment");
                    }
                    //used special maneuvers
                    if (curr != null && curr.getAssignment() == FleetAssignment.STANDING_DOWN) {
                        CampaignFleetAIAPI ai = fleet.getAI();
                        if (ai instanceof ModularFleetAIAPI) {
                            // needed to interrupt an in-progress pursuit
                            ModularFleetAIAPI m = (ModularFleetAIAPI) ai;
                            m.getStrategicModule().getDoNotAttack().add(pf, 1f);
                            m.getTacticalModule().setTarget(null);
                        }
                    }
                    //logic
                    float distance = MathUtils.getDistance(fleet.getLocation(), f.target.getLocation());
                    //attack
                    if (distance > 600f) {
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, f.target, Float.MAX_VALUE, "moving to location");

                    } else if (!site.destroyed && !run) {

                        String actionText = "";

                        switch (site.faction){
                            case Factions.PIRATES:
                                actionText = "looting location";
                                break;
                            case Factions.LUDDIC_PATH:
                                actionText = "looting location";
                                break;
                            case Factions.TRITACHYON:
                                actionText = "evacuating location";
                                break;
                            case ids.KESTEVEN_FACTION_ID:
                                actionText = "evacuating location";
                                break;
                            case ids.ENIGMA_FACTION_ID:
                                actionText = "destroying location";
                                break;
                            case Factions.REMNANTS:
                                actionText = "destroying location";
                                break;
                        }
                        fleet.clearAssignments();
                        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, f.target, Float.MAX_VALUE, actionText);

                        //timer
                        site.destroyCounter += 0.1f;
                    }
                    //return
                    if (site.destroyed || site.entity==null || run) {

                        if (site.faction.equals(Factions.REMNANTS)){
                            fleet.clearAssignments();
                            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, f.home, Float.MAX_VALUE, "standing down");
                        } else {
                            if (fleet.getCurrentAssignment().getAssignment() != FleetAssignment.GO_TO_LOCATION_AND_DESPAWN) {
                                SectorEntityToken market = questUtil.getRandomFactionMarket(getRandom(), fleet.getFaction().getId());
                                fleet.clearAssignments();
                                if (market==null) {
                                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, f.home, Float.MAX_VALUE, "standing down");
                                }else {
                                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, market, Float.MAX_VALUE, "returning to " + market.getName());
                                }
                            }
                        }
                    }

                    //

                }
                //clean the fleet list
                fleetUtil.cleanUp(removedFleets, fleets);
                removedFleets.clear();
                //save to mem
                fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY+site.id);

                ////////////
                //site logic
                ////////////

                //destroyed by enemy fleets
                if (site.destroyed) {
                    site.active = false;
                    //don't nuke before fleets are gone
                    if (fleets.size() <= 0) {
                        removedSites.add(site);
                        continue;
                    }
                }
                //cleared by player
                if (site.cleared && fleets.size()<=0){
                    removedSites.add(site);
                    continue;
                }

                //update data
                site.activeFleets = fleets.size() - neutralized;
                if (site.active) {
                    site.countdown -= 0.1f;

                    //time
                    if (site.countdown <= 0f) {
                        activateLoot(site);
                        site.cleared = true;
                        site.active = false;

                    }
                    //destroyed fleets
                    if (site.activeFleets <= 0 && !site.cleared) {
                        activateLoot(site);
                        site.cleared = true;
                        site.active = false;

                    }
                }

            }
            //clean the site list
            cleanUp(removedSites, sites);
            removedSites.clear();
            //save to mem
            setSites(sites, SITE_ARRAY_KEY);

            //
            counter.val = 0f;
        }
    }

    private void activateLoot(blacksiteInfo site) {

        SectorEntityToken entity = null;
        switch (site.faction){
            case Factions.PIRATES:
                entity = util.swapSalvageEntity(site.entity, PIRATE_ENTITY_ID, getRandom());
                break;
            case Factions.LUDDIC_PATH:
                entity = util.swapSalvageEntity(site.entity, PATHER_ENTITY_ID, getRandom());
                break;
            case Factions.TRITACHYON:
                entity = util.swapSalvageEntity(site.entity, TRITACHYON_ENTITY_ID, getRandom());
                break;
            case ids.KESTEVEN_FACTION_ID:
                entity = util.swapSalvageEntity(site.entity, KESTEVEN_ENTITY_ID, getRandom());
                break;
            case ids.ENIGMA_FACTION_ID:
                entity = util.swapSalvageEntity(site.entity, ENIGMA_ENTITY_ID, getRandom());
                break;
            case Factions.REMNANTS:
                entity = util.swapSalvageEntity(site.entity, REMNANT_ENTITY_ID, getRandom());
                break;
        }
        if (entity==null) {
            log("ERROR null entity");
            return;
        }
        //insta discover anywhere
        //one of these did it
        entity.setSensorProfile(Float.MAX_VALUE);
        entity.setExtendedDetectedAtRange(Float.MAX_VALUE);
        entity.setDetectionRangeDetailsOverrideMult(Float.MAX_VALUE);
        entity.setDiscoveryXP(0f);
        entity.setDiscoverable(false);
    }

    public static void activate(blacksiteInfo site) {
        spawnBlacksiteFleets(site);

        //ping
        Global.getSector().addPing(site.entity, Pings.SENSOR_BURST);
        Global.getSector().addPing(site.entity, Pings.INTERDICT);

        site.active = true;
    }

    private void destroy(SectorEntityToken target) {
        StarSystemAPI sys = target.getStarSystem();

        Global.getSoundPlayer().playSound("hit_hull_heavy", 1f, 1f, target.getLocation(), new Vector2f());

        DebrisFieldTerrainPlugin.DebrisFieldParams fieldParams = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                75f, // field radius - should not go above 1000 for performance reasons
                1.2f, // density, visual - affects number of debris pieces
                10000000f, // duration in days
                7f); // days the field will keep generating glowing pieces
        fieldParams.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
        fieldParams.baseSalvageXP = 500; // base XP for scavenging in field
        SectorEntityToken debris = Misc.addDebrisField(target.getStarSystem(), fieldParams, StarSystemGenerator.random);
        debris.setSensorProfile(1000f);
        debris.setDiscoverable(true);
        debris.setCircularOrbit(target.getStarSystem().getStar(), target.getCircularOrbitAngle(), target.getCircularOrbitRadius(), target.getCircularOrbitPeriod());
        debris.setId("nskr_debris_"+target.getId());

        target.setExpired(true);
        sys.removeEntity(target);

    }

    private static void spawnBlacksiteFleets(blacksiteInfo site) {
        Random random = site.random;
        SectorEntityToken target = site.entity;
        String faction = site.faction;
        int count = site.count;
        float points = site.points;

        for (int x = 0; x<count;x++) {
            StarSystemAPI sys = target.getStarSystem();
            SectorEntityToken from;

            from = getFleetLocation(sys, random);

            float combatPoints = points / count;
            //power scaling
            combatPoints += (combatPoints * powerLevel.get(0.2f, 0f, 1f)) / 2f;
            log("BASE " + combatPoints);

            //apply settings
            combatPoints *= nskr_modPlugin.getScriptedFleetSizeMult();

            ArrayList<String> keys = new ArrayList<>();
            keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
            keys.add(MemFlags.MEMORY_KEY_MAKE_HOSTILE);
            keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);
            keys.add(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE);
            //memflag nonsense
            keys.add(MemFlags.MEMORY_KEY_AVOID_PLAYER_SLOWLY);
            keys.add(MemFlags.MEMORY_KEY_MAKE_NON_AGGRESSIVE);
            keys.add(MemFlags.MEMORY_KEY_PATROL_ALLOW_TOFF);
            keys.add(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED);
            //key
            keys.add(DEFENDER_KEY);

            simpleFleet simpleFleet = new simpleFleet(from, faction, combatPoints, keys, random);
            simpleFleet.type = FleetTypes.PATROL_LARGE;
            simpleFleet.ignoreMarketFleetSizeMult = true;
            simpleFleet.sMods = mathUtil.getSeededRandomNumberInRange(0, 1, random);

            switch (site.faction){
                case Factions.PIRATES:
                    simpleFleet.name = "Gang";
                    break;
                case Factions.LUDDIC_PATH:
                    simpleFleet.name = "Flotilla";
                    break;
                case Factions.TRITACHYON:
                    simpleFleet.name = "Black Ops Group";
                    break;
                case ids.KESTEVEN_FACTION_ID:
                    simpleFleet.name = "Strike Force";
                    break;
                case ids.ENIGMA_FACTION_ID:
                    simpleFleet.name = "Black Ops "+util.getRandomGreekLetter(random, true);
                    break;
                case Factions.REMNANTS:
                    simpleFleet.name = "Sub-Ordo "+util.getRandomGreekLetter(random, true);
                    simpleFleet.aiFleetProperties = true;
                    break;
            }

            simpleFleet.assignment = FleetAssignment.GO_TO_LOCATION;
            simpleFleet.assignmentText = "moving to location";
            CampaignFleetAPI fleet = simpleFleet.create();

            //spawning
            Vector2f loc = from.getLocation();
            fleet.setLocation(loc.x, loc.y);
            fleet.setFacing(random.nextFloat() * 360.0f);

            //makes sure we are not in a star
            questUtil.spawnAwayFromStarFixer(fleet);

            //update
            fleetUtil.update(fleet, random);

            //add to mem IMPORTANT
            List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY+target.getId());
            fleets.add(new fleetInfo(fleet, target, from));
            fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY+target.getId());

            log("SPAWNED " + fleet.getName() + " size " + combatPoints);
        }
    }

    private static SectorEntityToken getFleetLocation(StarSystemAPI sys, Random random){
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        SectorEntityToken temp = null;

        for (int x = 0; x<500;x++) {
            temp = util.getRandomLocationInSystem(sys, false, true, random);
            //try to get location away
            if (MathUtils.getDistance(pf, temp.getLocation()) < pf.getSensorStrength() * 1.5f){
                temp = null;
            }
            if (temp!=null) break;
        }
        //backup
        if (temp==null){
            temp = util.getRandomLocationInSystem(sys, false, true, random);
        }

        return temp;
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }

    public static blacksiteInfo getInfo(String id, List<blacksiteInfo> sites){
        for (blacksiteInfo site : sites) {
            if (id.equals(site.id)){
                return site;
            }
        }
        return null;
    }

    private static void cleanUp(List<blacksiteInfo> toRemove, List<blacksiteInfo> sites) {
        for (Iterator<blacksiteInfo> iter = sites.listIterator(); iter.hasNext();) {
            blacksiteInfo a = iter.next();
            for (blacksiteInfo remove : toRemove) {
                if (a == remove) {

                    log("REMOVED " + a.id);
                    iter.remove();
                }
                if (a==null) iter.remove();
            }
        }
    }

    public static List<blacksiteInfo> getSites(String id) {
        MemoryAPI mem = Global.getSector().getMemory();
        if (mem.contains(id)){
            return (List<blacksiteInfo>)mem.get(id);
        } else {
            mem.set(id, new ArrayList<blacksiteInfo>());
        }
        return (List<blacksiteInfo>)mem.get(id);
    }

    public static List<blacksiteInfo> setSites(List<blacksiteInfo> sites, String id) {
        MemoryAPI mem = Global.getSector().getMemory();
        mem.set(id, sites);
        return (List<blacksiteInfo>) mem.get(id);
    }

    public static Random getRandom() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

            data.put(PERSISTENT_RANDOM_KEY,  new Random(new Random().nextLong()));
        }
        return (Random) data.get(PERSISTENT_RANDOM_KEY);
    }
}