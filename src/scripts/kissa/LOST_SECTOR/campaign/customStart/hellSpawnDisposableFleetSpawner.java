package scripts.kissa.LOST_SECTOR.campaign.customStart;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DisposableFleetManager;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.customStart.intel.hellSpawnEventIntel;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleFleet;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.ids;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.powerLevel;

import java.util.ArrayList;
import java.util.Random;

public class hellSpawnDisposableFleetSpawner extends DisposableFleetManager {

    public static final String DISPOSABLE_FLEET_KEY = "$hellSpawnDisposableFleet";
    public static final String TIMESTAMP_KEY = "$hellSpawnDisposableFleetTimestamp";

    public static final ArrayList<String> FLEET_FACTIONS = new ArrayList<>();
    static {
        FLEET_FACTIONS.add(Factions.HEGEMONY);
        FLEET_FACTIONS.add(Factions.LUDDIC_CHURCH);
        FLEET_FACTIONS.add(Factions.LUDDIC_PATH);
        FLEET_FACTIONS.add(Factions.INDEPENDENT);
        FLEET_FACTIONS.add(Factions.PERSEAN);
        FLEET_FACTIONS.add(Factions.DIKTAT);
        FLEET_FACTIONS.add(ids.KESTEVEN_FACTION_ID);

    }

    public static final ArrayList<String> EXTRA_FACTIONS = new ArrayList<>();
    static {
        FLEET_FACTIONS.add(Factions.SCAVENGERS);
        FLEET_FACTIONS.add(Factions.MERCENARY);
    }

    protected Random random = new Random();
    protected Long timestamp;
    protected int maxCount;

    //
    public hellSpawnDisposableFleetSpawner() {
        super();

        timestamp = Global.getSector().getClock().getTimestamp();
        maxCount = 0;
    }

    protected Object readResolve() {
        super.readResolve();
        return this;
    }

    @Override
    protected String getSpawnId() {
        return "hellspawn";
    }

    protected hellSpawnEventIntel getIntel() {
        if (currSpawnLoc == null) return null;
        return hellSpawnEventIntel.get();
    }

    @Override
    protected int getDesiredNumFleetsForSpawnLocation() {
        if (gamemodeManager.getMode() != gamemodeManager.gameMode.HELLSPAWN) return 0;
        float level = hellSpawnManager.getLevel();
        if (level<3) return 0;

        hellSpawnEventIntel intel = getIntel();
        if (intel == null) return 0;

        if (timestamp != null) {
            float daysSince = Global.getSector().getClock().getElapsedDaysSince(timestamp);
            //set maxCount for 30 days based on lvl, then don't spawn again
            if (daysSince < 30) return maxCount;
        }

        //rng check
        float chance = (level/100f)/10f;
        if (random.nextFloat()<chance) return 0;
        maxCount = (int)level;
        //UPDATE TIMER
        timestamp = Global.getSector().getClock().getTimestamp();

        return maxCount;
    }

    @Override
    protected boolean isOkToDespawnAssumingNotPlayerVisible(CampaignFleetAPI fleet) {
        float time = Global.getSector().getClock().getElapsedDaysSince(fleet.getMemoryWithoutUpdate().getLong(TIMESTAMP_KEY));
        //60 day despawn timer
        return time>60f;
    }

    protected StarSystemAPI pickCurrentSpawnLocation() {
        return pickNearestPopulatedSystem();
    }
    protected StarSystemAPI pickNearestPopulatedSystem() {
        if (Global.getSector().isInNewGameAdvance()) return null;
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (player == null) return null;
        StarSystemAPI nearest = null;

        float minDist = Float.MAX_VALUE;

        //get nearest protector market
        for (String f : FLEET_FACTIONS) {
            for (MarketAPI market : Misc.getFactionMarkets(f)) {
                StarSystemAPI system = market.getStarSystem();
                if (system==null) continue;

                float distToPlayerLY = Misc.getDistanceLY(player.getLocationInHyperspace(), system.getLocation());
                if (distToPlayerLY > MAX_RANGE_FROM_PLAYER_LY) continue;

                if (distToPlayerLY < minDist) {
                    nearest = system;
                    minDist = distToPlayerLY;
                }
            }
        }

        // stick with current system longer unless something else is closer
        if (nearest == null && currSpawnLoc != null) {
            float distToPlayerLY = Misc.getDistanceLY(player.getLocationInHyperspace(), currSpawnLoc.getLocation());
            if (distToPlayerLY <= DESPAWN_RANGE_LY) {
                nearest = currSpawnLoc;
            }
        }

        return nearest;
    }

    protected CampaignFleetAPI spawnFleetImpl() {
        StarSystemAPI system = currSpawnLoc;
        if (system == null) return null;

        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (player == null) return null;

        hellSpawnEventIntel intel = getIntel();
        if (intel == null) return null;

        float combatPoints = mathUtil.getSeededRandomNumberInRange(20f, 80f, random);
        //power scaling
        combatPoints += combatPoints * powerLevel.get(0.2f, 0f,1.5f);

        //apply settings
        combatPoints *= nskr_modPlugin.getScriptedFleetSizeMult();

        //keys
        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOSTILE);
        keys.add(MemFlags.MEMORY_KEY_LOW_REP_IMPACT);
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);
        keys.add(MemFlags.MEMORY_KEY_NEVER_AVOID_PLAYER_SLOWLY);
        keys.add(DISPOSABLE_FLEET_KEY);

        //fleet
        simpleFleet simpleFleet = new simpleFleet(currSpawnLoc.getCenter(), getFaction(), combatPoints, keys, random);
        simpleFleet.maxShipSize = 4;
        simpleFleet.name = "Protectors";
        simpleFleet.assignment = FleetAssignment.PATROL_SYSTEM;
        simpleFleet.assignmentText = "patrolling";

        CampaignFleetAPI fleet = simpleFleet.create();
        if (fleet == null || fleet.isEmpty()) return null;

        fleet.getMemoryWithoutUpdate().set(KEY_SYSTEM, system.getName());
        fleet.getMemoryWithoutUpdate().set(KEY_SPAWN_FP, fleet.getFleetPoints());
        fleet.getMemoryWithoutUpdate().set(TIMESTAMP_KEY, Global.getSector().getClock().getTimestamp());

        setLocationAndOrders(fleet, 0.50f, 0.50f);

        fleetUtil.update(fleet, random);

        return fleet;
    }

    private String getFaction() {
        ArrayList<String> factions = new ArrayList<>(EXTRA_FACTIONS);

        for (MarketAPI m : Misc.getMarketsInLocation(currSpawnLoc)){
            if (m.getFactionId()==null) continue;
            if (!FLEET_FACTIONS.contains(m.getFactionId())) continue;

            factions.add(m.getFactionId());
        }

        return factions.get(mathUtil.getSeededRandomNumberInRange(0, factions.size() - 1, random));
    }

}