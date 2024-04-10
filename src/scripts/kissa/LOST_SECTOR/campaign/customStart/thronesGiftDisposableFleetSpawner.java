package scripts.kissa.LOST_SECTOR.campaign.customStart;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DisposableAggroAssignmentAI;
import com.fs.starfarer.api.impl.campaign.fleets.DisposableFleetManager;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.customStart.intel.thronesGiftIntel;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleFleet;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.powerLevel;

import java.util.ArrayList;
import java.util.Random;

public class thronesGiftDisposableFleetSpawner extends DisposableFleetManager {

    public static final String DISPOSABLE_FLEET_KEY = "$thronesGiftDisposableFleet";
    public static final String TIMESTAMP_KEY = "$thronesGiftDisposableFleetTimestamp";

    public static final ArrayList<String> FLEET_FACTIONS = new ArrayList<>();
    static {
        FLEET_FACTIONS.add(Factions.PIRATES);
        FLEET_FACTIONS.add(Factions.LUDDIC_CHURCH);
        FLEET_FACTIONS.add(Factions.LUDDIC_PATH);

    }

    protected Random random = new Random();
    protected Long timestamp;
    protected int maxCount;

    //
    public thronesGiftDisposableFleetSpawner() {
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
        return "thronesGift";
    }

    protected thronesGiftIntel getIntel() {
        if (currSpawnLoc == null) return null;
        return thronesGiftIntel.get();
    }

    @Override
    protected int getDesiredNumFleetsForSpawnLocation() {
        if (gamemodeManager.getMode() != gamemodeManager.gameMode.THRONESGIFT) return 0;
        //boolean debug = true;
        //if (debug) return 20;
        float level = (thronesGiftManager.getTotalDp() - thronesGiftManager.DEFAULT_DP);
        level /= 50f;
        //min auto pts unlock before we spawn fleets
        if (level<1f) return 0;
        return 1;
//
        //thronesGiftIntel intel = getIntel();
        //if (intel == null) return 0;
//
        //if (timestamp != null) {
        //    float daysSince = Global.getSector().getClock().getElapsedDaysSince(timestamp);
        //    //set maxCount for some days based on lvl, then don't spawn again
        //    if (daysSince < 14) return maxCount;
        //}

        //rng check
        //float chance = (level)/3f;
        //if (random.nextFloat() < chance) return 1;
        //else return 0;
        //maxCount = (int)level;
        ////UPDATE TIMER
        //timestamp = Global.getSector().getClock().getTimestamp();
//
        //return maxCount;
    }

    @Override
    protected boolean isOkToDespawnAssumingNotPlayerVisible(CampaignFleetAPI fleet) {
        float time = Global.getSector().getClock().getElapsedDaysSince(fleet.getMemoryWithoutUpdate().getLong(TIMESTAMP_KEY));
        //despawn timer
        return time > 30f;
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

        thronesGiftIntel intel = getIntel();
        if (intel == null) return null;

        //RNG CHECK
        float level = (thronesGiftManager.getTotalDp() - thronesGiftManager.DEFAULT_DP);
        level /= 50f;
        float chance = (level)/4f;
        //x chance to get deleted
        if (random.nextFloat() > chance) return null;

        float combatPoints = mathUtil.getSeededRandomNumberInRange(50f, 100f, random);
        //power scaling
        combatPoints += combatPoints * powerLevel.get(0.2f, 0f,1.0f);

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
        //hyper or in system
        float hyperChance = 0.75f;
        SectorEntityToken loc = currSpawnLoc.getCenter();
        if (random.nextFloat()<hyperChance) loc = system.getHyperspaceAnchor();
        simpleFleet simpleFleet = new simpleFleet(loc, Factions.LUDDIC_PATH, combatPoints, keys, random);
        simpleFleet.maxShipSize = 4;
        simpleFleet.name = "Hunters";
        simpleFleet.assignment = FleetAssignment.RAID_SYSTEM;
        simpleFleet.assignmentText = "looking for your fleet";
        //no retreat
        simpleFleet.aiFleetProperties = true;

        CampaignFleetAPI fleet = simpleFleet.create();

        if (fleet == null || fleet.isEmpty()) return null;

        fleet.getMemoryWithoutUpdate().set(KEY_SYSTEM, system.getName());
        fleet.getMemoryWithoutUpdate().set(KEY_SPAWN_FP, fleet.getFleetPoints());
        fleet.getMemoryWithoutUpdate().set(TIMESTAMP_KEY, Global.getSector().getClock().getTimestamp());

        //setLocationAndOrders(fleet, 0.75f, 0.75f);
        fleet.addScript(new DisposableAggroAssignmentAI(fleet, system, this, hyperChance));

        //HOLY SPIRIT
        for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
            if (curr.isFighterWing()) continue;
            ShipVariantAPI v = curr.getVariant();
            if (v==null) continue;

            //flagship
            if (curr.isFlagship()){
                v.addPermaMod("nskr_holySpirit");
                v.addTag(Tags.TAG_NO_AUTOFIT);
            }
            //Hunter hullmod
            if (random.nextFloat()<0.10f){
                v.addPermaMod("nskr_machineSpirit");
                v.addTag(Tags.TAG_NO_AUTOFIT);
            }

        }
        fleetUtil.update(fleet, random);

        return fleet;
    }
}
