package scripts.kissa.LOST_SECTOR.campaign.customStart;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.DiplomacyManager;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.customStart.abilities.hellSpawnAbility;
import scripts.kissa.LOST_SECTOR.campaign.customStart.intel.hellSpawnEventFactors;
import scripts.kissa.LOST_SECTOR.campaign.customStart.intel.hellSpawnEventIntel;
import scripts.kissa.LOST_SECTOR.campaign.customStart.intel.hellSpawnJudgementIntel;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.fleetInfo;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.util.campaignTimer;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.ids;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.world.systems.frost.nskr_frost;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class hellSpawnManager extends BaseCampaignEventListener implements EveryFrameScript, ColonyPlayerHostileActListener {

    //welcome to hell
    public static final int PEACEFUL_MAX_POINTS = 350;
    public static final int NEUTRAL_MAX_POINTS = 2000;
    public static final float JUDGEMENT_TIMER = 40f;
    public static final float PEACEFUL_BONUS= 33f;

    public static final float FLAGSHIP_CAP_BONUS = 25f;
    public static final float FLAGSHIP_CRUISER_BONUS = 50f;
    public static final float FLAGSHIP_DD_BONUS = 20f;
    public static final float FLAGSHIP_FRIG_BONUS = 30f;

    public static final float FLAGSHIP_DP_DISCOUNT = 25f;
    public static final float AUTOMATED_BASE_CR = 40f;
    public static final float OFFICER_LEVEL_BONUS = 1f;

    public static final float DAMAGE_BONUS = 25f;
    public static final float DAMAGE_TAKEN = 20f;
    public static final float DODGE_CHANCE = 10f;

    public static final float BURN_BONUS = 3f;
    public static final float DETECTED_AT_RANGE = 50f;
    public static final float SENSOR_RANGE = 50f;

    public static final float RELATIONSHIP_REDUCTION = 15f;
    public static final float CR_REDUCTION = 5f;
    public static final float STAB_PENALTY = 1f;

    public static final float AGENT_LIGHT_POINTS = 5f;
    public static final float AGENT_SERIOUS_POINTS = 10f;
    public static final float RAID_BASE_POINTS = 10f;
    public static final float CAPTURE_BASE_POINTS = 50f;
    public static final float TACBOMB_BASE_POINTS = 60f;
    public static final float SATBOMB_BASE_POINTS = 250f;

    public static final float LAWFUL_PER_FP_POINTS = 0.25f;
    public static final float UNLAWFUL_POINTS_MULT = 0.33f;

    public static final String STAT_ID = "HellspawnBonus";
    public static final String STARTED_KEY = "HellspawnStartedKey";
    public static final String JUDGEMENT_KEY = "HellspawnJudgementKey";
    public static final String JUDGEMENT_DEFEATED_KEY = "HellspawnJudgementDefeatedKey";
    public static final String LEVEL_KEY = "hellSpawnManagerLevel";
    public static final String PERSISTENT_RANDOM_KEY = "hellSpawnManagerRandom";

    campaignTimer timer;
    private int level;
    private final List<CampaignFleetAPI> removed = new ArrayList<>();

    public hellSpawnManager() {
        super(false);
        this.timer = new campaignTimer(this.getClass().getName(), 1f);
        //init randoms
        getRandom();
        //setup
        setupFactions();
    }

    //factions not on the intel tab
    public static final ArrayList<String> LAWFUL_FACTIONS = new ArrayList<>();
    static {
        LAWFUL_FACTIONS.add(Factions.SCAVENGERS);
        LAWFUL_FACTIONS.add(Factions.MERCENARY);
        LAWFUL_FACTIONS.add(Factions.LIONS_GUARD);
    }
    //unlawful factions that are on the intel tab
    public static final ArrayList<String> UNLAWFUL_FACTIONS = new ArrayList<>();
    static {
        UNLAWFUL_FACTIONS.add(Factions.PIRATES);
        UNLAWFUL_FACTIONS.add(Factions.LUDDIC_PATH);
        UNLAWFUL_FACTIONS.add("cabal");
        UNLAWFUL_FACTIONS.add("loa_colonialpirates");
        UNLAWFUL_FACTIONS.add("tahlan_legioinfernalis");
        UNLAWFUL_FACTIONS.add("fang");
        UNLAWFUL_FACTIONS.add("draco");
        UNLAWFUL_FACTIONS.add("knights_of_eva");
        UNLAWFUL_FACTIONS.add("vass");
        UNLAWFUL_FACTIONS.add("ix_battlegroup");
        UNLAWFUL_FACTIONS.add("HIVER");

    }

    public static ArrayList<String> lawfulFactions = new ArrayList<>();
    public static ArrayList<String> unlawfulFactions = new ArrayList<>();
    private void setupFactions() {
        //make sure it's reset
        lawfulFactions = new ArrayList<>();
        unlawfulFactions = new ArrayList<>();

        ArrayList<String> temp = new ArrayList<>(LAWFUL_FACTIONS);

        for (FactionAPI f : Global.getSector().getAllFactions()){
            if (UNLAWFUL_FACTIONS.contains(f.getId())) continue;
            if (!f.isShowInIntelTab()) continue;

            if (!temp.contains(f.getId())) temp.add(f.getId());
        }

        lawfulFactions.addAll(temp);
        unlawfulFactions.addAll(UNLAWFUL_FACTIONS);
    }

    static void log(final String message) {
        Global.getLogger(hellSpawnManager.class).info(message);
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        if (gamemodeManager.getMode() != gamemodeManager.gameMode.HELLSPAWN) return;

        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        level = getLevel();

        if (level>0){

            applyStatHullmod();
        }

        //PAUSE CHECK
        if (Global.getSector().isPaused()) return;

        timer.advance(amount);
        if (timer.onTimeout()){
            MutableCharacterStatsAPI characterStats = Global.getSector().getPlayerStats();
            MutableFleetStatsAPI fleetStats = Global.getSector().getPlayerFleet().getStats();
            if (characterStats.getLevel()>=15){

                //start
                if (!questUtil.getCompleted(STARTED_KEY)) {
                    CampaignUIAPI ui = Global.getSector().getCampaignUI();
                    if (!ui.isShowingDialog() && !ui.isShowingMenu()) {
                        //judgement
                        Global.getSector().getCampaignUI().showInteractionDialog(new hellSpawnJudgementWarning(), null);

                        questUtil.setCompleted(true, STARTED_KEY);
                    }
                }

            }
            IntelInfoPlugin info = Global.getSector().getIntelManager().getFirstIntel(hellSpawnJudgementIntel.class);
            if (info!=null){
                hellSpawnJudgementIntel intel = hellSpawnJudgementIntel.get();

                float timer = Global.getSector().getClock().getElapsedDaysSince(intel.time);

                if (timer > JUDGEMENT_TIMER){

                    //NOW
                    if (!questUtil.getCompleted(JUDGEMENT_KEY)) {
                        CampaignUIAPI ui = Global.getSector().getCampaignUI();
                        if (!ui.isShowingDialog() && !ui.isShowingMenu()) {
                            //delete intel
                            intel.endImmediately();
                            //judgement
                            Global.getSector().getCampaignUI().showInteractionDialog(new hellSpawnJudgementDialog(), null);

                            questUtil.setCompleted(true, JUDGEMENT_KEY);
                        }
                    }

                }

            }
            //jank ass shit
            if (questUtil.getCompleted(JUDGEMENT_DEFEATED_KEY)) {
                hellSpawnJudgementWarning.stopMusic();
            }

            if (level==0) return;
            if (getStabPenalty()>0) {
                applyCondition();
            }
            //lvl1
            applyFleetBonus(fleetStats);
            //lvl2
            if (level>=2){
                applyCharacterBonus(characterStats);
            }

            //AI
            ////////////
            if (pf == null) return;

            List<fleetInfo> fleets = fleetUtil.getFleets(hellSpawnAbility.FLEET_ARRAY_KEY);
            for (fleetInfo f : fleets){
                CampaignFleetAPI fleet = f.fleet;
                f.age += 0.1f;
                boolean despawn = false;

                if (f.age> hellSpawnAbility.MAX_DURATION+7f) {
                    despawn = true;
                }
                if (fleet.getFleetPoints()<=0f) {
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
                //stop here when defeated
                if (despawn) continue;
                //assignment logic
                FleetAssignmentDataAPI assignment = fleet.getAI().getCurrentAssignment();
                if (assignment == null) {
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.HOLD, fleet.getContainingLocation().createToken(fleet.getLocation()), Float.MAX_VALUE, "holding");
                    log("null assignment");
                }
                //used special maneuvers
                if (assignment!=null && assignment.getAssignment()==FleetAssignment.STANDING_DOWN) {
                    CampaignFleetAIAPI ai = fleet.getAI();
                    if (ai instanceof ModularFleetAIAPI) {
                        // needed to interrupt an in-progress pursuit
                        ModularFleetAIAPI m = (ModularFleetAIAPI) ai;
                        m.getStrategicModule().getDoNotAttack().add(pf, 1f);
                        m.getTacticalModule().setTarget(null);
                    }
                }
                //LOGIC

                //RETURN
                if (f.age > hellSpawnAbility.MAX_DURATION){
                    StarSystemAPI homesys = Global.getSector().getStarSystem(nskr_frost.getName());
                    SectorEntityToken home = homesys.getCenter();
                    MarketAPI market = Global.getSector().getEconomy().getMarket("nskr_heart");
                    if (market!=null){
                        home = market.getPrimaryEntity();
                    }

                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, home, Float.MAX_VALUE, "returning");
                    continue;
                }

                boolean markets = Misc.getMarketsInLocation(pf.getContainingLocation()).isEmpty();
                float fardist = 750f;
                if (markets) fardist = 3f;
                boolean far = MathUtils.getDistance(pf, fleet) > fardist;
                if (fleet.getContainingLocation()!= pf.getContainingLocation()){
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, pf.getContainingLocation().createToken(pf.getLocation()), Float.MAX_VALUE, "seeking");
                    continue;

                }
                if (!pf.isInHyperspace()){
                    fleet.clearAssignments();
                    if (!far){
                        if (!markets) {
                            fleet.addAssignment(FleetAssignment.RAID_SYSTEM, pf, Float.MAX_VALUE, "seeking");

                        } else {
                            fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, pf, Float.MAX_VALUE, "seeking");

                        }

                    } else {
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, pf, Float.MAX_VALUE, "seeking");

                    }
                } else {
                    fleet.clearAssignments();
                    if (!far){
                        fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, pf, Float.MAX_VALUE, "seeking");

                    } else {
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, pf, Float.MAX_VALUE, "seeking");

                    }
                }

            }

            //clean the list
            fleetUtil.cleanUp(removed, fleets);
            removed.clear();
            //save to mem
            fleetUtil.setFleets(fleets, hellSpawnAbility.FLEET_ARRAY_KEY);
        }

    }

    @Override
    public void reportPlayerReputationChange(String faction, float delta) {

        if (gamemodeManager.getMode() != gamemodeManager.gameMode.HELLSPAWN) return;

        if (faction.equals(ids.ENIGMA_FACTION_ID) || faction.equals(Factions.PLAYER)) return;

        setRelation(faction);

    }

    private static void setRelation(String faction) {

        float cap = getRelationshipCap();
        if (cap>0){
            //add Nex rel cap
            float max;
            if (nskr_modPlugin.IS_NEXELERIN) {
                float maxRel = 1f - DiplomacyManager.getManager().getMaxRelationship(faction, Factions.PLAYER);
                max = Math.max(getMaxRelationship(cap) - maxRel, -1f);
            } else {
                max = getMaxRelationship(cap);
            }
            if (Global.getSector().getFaction(faction).getRelationship(Factions.PLAYER) > max){
                Global.getSector().getFaction(faction).setRelationship(Factions.PLAYER, max);
            }
        }
    }

    private static float getMaxRelationship(float cap) {
        return MathUtils.clamp(Math.abs((cap / 100f)-2f)-1f, -1f, 1f);
    }

    public static void reportLvlChanged(int level){

        //fix relations
        float cap = getRelationshipCap();
        if (cap>0){
            for (FactionAPI f : Global.getSector().getAllFactions()) {
                if (f.getId().equals(ids.ENIGMA_FACTION_ID) || f.getId().equals(Factions.PLAYER)) continue;
                setRelation(f.getId());
             }
        }
    }

    public static void applyFleetBonus(MutableFleetStatsAPI stats) {
        stats.getFleetwideMaxBurnMod().modifyFlat(hellSpawnManager.STAT_ID, hellSpawnManager.BURN_BONUS, "Hellspawn");
        stats.getDetectedRangeMod().modifyMult(hellSpawnManager.STAT_ID, 1f - hellSpawnManager.DETECTED_AT_RANGE / 100f, "Hellspawn");
        stats.getSensorRangeMod().modifyPercent(hellSpawnManager.STAT_ID, hellSpawnManager.SENSOR_RANGE, "Hellspawn");
    }

    public static void applyCharacterBonus(MutableCharacterStatsAPI stats) {
        stats.getDynamic().getMod(Stats.OFFICER_MAX_LEVEL_MOD).modifyFlat(hellSpawnManager.STAT_ID, hellSpawnManager.OFFICER_LEVEL_BONUS);
    }

    private void applyCondition() {
        for (MarketAPI m : Misc.getFactionMarkets(Factions.PLAYER)){
            if (!m.hasCondition("nskr_hellSpawnCondition")){
                m.addCondition("nskr_hellSpawnCondition");
            }
        }
    }

    public static float getCrReduction(){
        return getLevel()*CR_REDUCTION;
    }
    public static float getStabPenalty(){
        return (getLevel()-1)*STAB_PENALTY;
    }
    public static float getRelationshipCap(){
        return (getLevel()-2)*RELATIONSHIP_REDUCTION;
    }

    private void applyStatHullmod() {
        for (FleetMemberAPI m : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
            if (m.getVariant()==null) continue;
            if (m.getVariant().hasHullMod("nskr_hellSpawnStats")) continue;

            m.getVariant().addMod("nskr_hellSpawnStats");
            //if you don't call this the hullmods will exist in some fuckass limbo state
            fleetUtil.updatePlayerFleet(false);
        }
    }

    public static void increaseLevel(int level){

        setLevel(getLevel()+level);
        //listener
        reportLvlChanged(getLevel());
    }

    public static void setLevel(int level){

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(LEVEL_KEY, level);

    }

    public static int getLevel(){

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (data.containsKey(LEVEL_KEY)){
            return (int) data.get(LEVEL_KEY);
        } else {
            data.put(LEVEL_KEY, 0);
            return (int) data.get(LEVEL_KEY);
        }

    }

    public static Random getRandom() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

            data.put(PERSISTENT_RANDOM_KEY,  new Random(new Random().nextLong()));
        }
        return (Random) data.get(PERSISTENT_RANDOM_KEY);
    }

    private boolean hadAutomated = true;
    @Override
    public void reportPlayerEngagement(EngagementResultAPI result) {
        if (gamemodeManager.getMode() != gamemodeManager.gameMode.HELLSPAWN) return;

        if (level>=3){
            MutableCharacterStatsAPI characterStats = Global.getSector().getPlayerStats();

            if (characterStats.getSkillLevel(Skills.AUTOMATED_SHIPS)<=0f) {
                characterStats.setSkillLevel(Skills.AUTOMATED_SHIPS, 2f);
                hadAutomated = false;
            }
        }

        if (result.getWinnerResult()==null) return;
        if (result.getWinnerResult().getFleet()==null) return;
        if (result.getWinnerResult().getFleet().getFaction()==null) return;
        if (result.getLoserResult()==null) return;
        if (result.getLoserResult().getFleet()==null) return;
        if (result.getLoserResult().getFleet().getFaction()==null) return;

        EngagementResultForFleetAPI loser;
        if (result.getWinnerResult().isPlayer()) {
            loser = result.getLoserResult();
        } else {
            loser = result.getWinnerResult();
        }

        ArrayList<FleetMemberAPI> lost = new ArrayList<>();
        lost.addAll(loser.getDisabled());
        lost.addAll(loser.getDestroyed());
        if (lost.isEmpty()) return;

        float points = 0f;
        boolean lawful = false;
        for (FleetMemberAPI m : lost){
            if (lawfulFactions.contains(loser.getFleet().getFaction().getId())){
                points += LAWFUL_PER_FP_POINTS * m.getFleetPointCost();
                lawful = true;
            } else if (unlawfulFactions.contains(loser.getFleet().getFaction().getId())) {
                points += LAWFUL_PER_FP_POINTS * m.getFleetPointCost() * UNLAWFUL_POINTS_MULT;
            }
        }
        if (points==0f) return;

        points = Math.max(points, 1f);

        if (!lawful) hellSpawnEventIntel.get().addFactor(new hellSpawnEventFactors((int)points,
                "Destroyed "+loser.getFleet().getFaction().getDisplayName()+" ships, an unlawful faction", "Destruction of "+lost.size()+" vessels.", ""));
        if (lawful) hellSpawnEventIntel.get().addFactor(new hellSpawnEventFactors((int)points,
                "Destroyed "+loser.getFleet().getFaction().getDisplayName()+" ships, a lawful faction", "Destruction of "+lost.size()+" vessels.", "The crew's all left in orbit never to return."));
    }

    @Override
    public void reportBattleFinished(CampaignFleetAPI primaryWinner, BattleAPI battle) {

        //NOW we are cooking
        if (level>=3) {
            if (!hadAutomated) {
                MutableCharacterStatsAPI characterStats = Global.getSector().getPlayerStats();
                characterStats.setSkillLevel(Skills.AUTOMATED_SHIPS, 0f);

                hadAutomated = true;
            }
        }

    }

    @Override
    public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData, CargoAPI cargo) {
        if (market==null) return;
        if (gamemodeManager.getMode() != gamemodeManager.gameMode.HELLSPAWN) return;

        float points = (marketSizeMult(market) * RAID_BASE_POINTS) + actionData.marinesLost/10f + mathUtil.getSeededRandomNumberInRange(3,10, getRandom());

        hellSpawnEventIntel.get().addFactor(new hellSpawnEventFactors((int)points, "Raided a market",
                "Raided "+market.getName()+" a size "+market.getSize()+" market for valuables.", "A hoard worth its weight in blood."));
    }

    @Override
    public void reportRaidToDisruptFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData, Industry industry) {
        if (market==null) return;
        if (gamemodeManager.getMode() != gamemodeManager.gameMode.HELLSPAWN) return;

        float points = (marketSizeMult(market) * RAID_BASE_POINTS) + actionData.marinesLost/10f + mathUtil.getSeededRandomNumberInRange(3,10, getRandom());
        points /= actionData.objectives.size();

        hellSpawnEventIntel.get().addFactor(new hellSpawnEventFactors((int)points, "Raided a market",
                "Raided and disrupted a single structure at "+market.getName()+".", "A storm of gunfire, a pile of bodies."));
    }

    @Override
    public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData) {
        if (market==null) return;
        if (gamemodeManager.getMode() != gamemodeManager.gameMode.HELLSPAWN) return;

        float points = (marketSizeMult(market) * TACBOMB_BASE_POINTS) + mathUtil.getSeededRandomNumberInRange(5,15, getRandom());

        hellSpawnEventIntel.get().addFactor(new hellSpawnEventFactors((int)points, "Tactical bombardment of a market",
                "Bombarded "+market.getName()+" a size "+market.getSize()+" market.", "Those scarred ruins of humanity might never heal."));
    }

    @Override
    public void reportSaturationBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData) {
        if (market==null) return;
        if (gamemodeManager.getMode() != gamemodeManager.gameMode.HELLSPAWN) return;

        float points = (marketSizeMult(market) * SATBOMB_BASE_POINTS) + mathUtil.getSeededRandomNumberInRange(15,50, getRandom());

        int size = Math.max(market.getSize(), 3);
        if (market.getSize()>1) {
            hellSpawnEventIntel.get().addFactor(new hellSpawnEventFactors((int)points, "Saturation bombardment of a market",
                    "Bombarded "+market.getName()+" a size "+size+" market.", "Its all gone, its only silence not even a faintest hum. There's nothing left - not of you or them."));
        } else {
            hellSpawnEventIntel.get().addFactor(new hellSpawnEventFactors((int)points, "Saturation bombardment of a market",
                    "Destroyed "+market.getName()+".", "Its all gone, its only silence not even a faintest hum. There's nothing left - not of you or them."));
        }
    }

    public static float marketSizeMult(MarketAPI market){
        return Math.max(market.getSize() - 2f, 1f);
    }
}