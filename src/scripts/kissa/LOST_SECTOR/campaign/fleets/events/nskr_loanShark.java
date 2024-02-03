package scripts.kissa.LOST_SECTOR.campaign.fleets.events;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.fleetInfo;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleFleet;
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.nskr_debt;
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.nskr_loanSharkDialog;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.powerLevel;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.*;

public class nskr_loanShark extends BaseCampaignEventListener implements EveryFrameScript {
    //
    //Loan shark spawning
    //
    //chance per day
    public static final float BASE_CHANCE = 0.01f;
    public static final float DESPAWN_TIMER = 45f;
    public static final String COLLECTOR_KEY = "$debtCollector";
    public static final String FLEET_NAME = "Debt Collector";

    public static final String FLEET_ARRAY_KEY = "$nskr_loanSharkFleets";
    nskr_saved<Float> counter;
    nskr_saved<Boolean> collected;
    private final List<CampaignFleetAPI> removed = new ArrayList<>();
    //CampaignFleetAPI pf;
    Random random;

    static void log(final String message) {
        Global.getLogger(nskr_loanShark.class).info(message);
    }

    public nskr_loanShark() {
        super(false);
        //how often we run logic
        this.counter = new nskr_saved<>("sharkCounter", 0.0f);
        //once per campaign
        this.collected = new nskr_saved<>("sharkCollected", false);
        this.random = new Random();
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
        if (counter.val>10f) {
            boolean hostile = Global.getSector().getFaction("kesteven").getRelationship(Factions.PLAYER) <= -0.5f;
            boolean paid = nskr_loanSharkDialog.getPaid(nskr_loanSharkDialog.PERSISTENT_KEY);

            List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
            for (fleetInfo f : fleets) {
                CampaignFleetAPI fleet = f.fleet;
                //timer
                f.age+=1f;
                boolean despawn = false;

                if (fleet.getFleetPoints()*4.0f<f.strength){
                    despawn = true;
                }
                if (f.age>DESPAWN_TIMER){
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
                FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
                if (curr == null) {
                    fleet.clearAssignments();
                    fleet.addAssignment(FleetAssignment.HOLD, fleet.getContainingLocation().createToken(fleet.getLocation()), Float.MAX_VALUE, "holding");
                    log("null assignment");
                }
                //used special maneuvers
                if (curr!=null && curr.getAssignment()==FleetAssignment.STANDING_DOWN) {
                    CampaignFleetAIAPI ai = fleet.getAI();
                    if (ai instanceof ModularFleetAIAPI) {
                        // needed to interrupt an in-progress pursuit
                        ModularFleetAIAPI m = (ModularFleetAIAPI) ai;
                        m.getStrategicModule().getDoNotAttack().add(pf, 1f);
                        m.getTacticalModule().setTarget(null);
                    }
                }
                //logic

                //intercept
                if (hostile && !paid) {
                    fleetUtil.gotoAndInterceptPlayerAI(fleet, f, fleetUtil.interceptBehaviour.AROUND);
                }
                //leave
                if (!hostile || paid) {
                    if (fleet.getAI().getCurrentAssignmentType() != FleetAssignment.GO_TO_LOCATION_AND_DESPAWN) {
                        fleet.clearAssignments();
                        fleet.getMemoryWithoutUpdate().clear();
                        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);

                        if (questUtil.getRandomFactionMarket(new Random(), "kesteven").getMarket() != null) {
                            SectorEntityToken loc = questUtil.getRandomFactionMarket(new Random(), "kesteven");
                            if (loc != null && loc.getMarket() != null) {
                                fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, loc, Float.MAX_VALUE, "returning to " + loc.getName());
                                log("loanShark " + fleet.getName() + " RETURNING ");
                            }
                        }
                    }
                }
            }

            //clean the list
            fleetUtil.cleanUp(removed, fleets);
            removed.clear();
            //save to mem
            fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

            counter.val = 0f;

            //logic

            //spawn one fleet at a time
            if (fleets.isEmpty() && util.kestevenExists() && !collected.val) {
                //can spawn check
                Random random = nskr_loanSharkDialog.getRandom();
                if (random.nextFloat()<BASE_CHANCE && pf.isInHyperspace() && pf.getLocation().length()<25000f && hostile && nskr_debt.getDebt()>=250000) {
                    this.spawnCollectorFleet();
                    collected.val = true;
                    log("loanShark SPAWNING");
                }
            }
        }
    }

    void spawnCollectorFleet() {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();

        Random random = nskr_loanSharkDialog.getRandom();

        float combatPoints = mathUtil.getSeededRandomNumberInRange(100f, 110f, random);
        //power scaling
        combatPoints += combatPoints* powerLevel.get(0.2f, 0f,2f);
        log("loanShark BASE " + combatPoints);

        //apply settings
        combatPoints *= nskr_modPlugin.getScriptedFleetSizeMult();

        ArrayList<String> keys = new ArrayList<>();
        keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
        keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
        keys.add(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER);
        keys.add(COLLECTOR_KEY);

        simpleFleet simpleFleet = new simpleFleet(pf.getContainingLocation().createToken(pf.getLocation()), "kesteven", combatPoints, keys, random);
        //simpleFleet.type = FleetTypes.PATROL_LARGE;
        simpleFleet.ignoreMarketFleetSizeMult = true;
        simpleFleet.maxShipSize = 3;
        simpleFleet.sMods = mathUtil.getSeededRandomNumberInRange(2,3, random);
        simpleFleet.name = FLEET_NAME;
        simpleFleet.assignment = FleetAssignment.INTERCEPT;
        simpleFleet.assignmentText = "intercepting your fleet";
        simpleFleet.interceptPlayer = true;
        CampaignFleetAPI fleet = simpleFleet.create();

        //spawning
        final Vector2f loc = new Vector2f(MathUtils.getPointOnCircumference(pf.getLocation(), (pf.getSensorStrength()*0.90f)+(fleet.getSensorProfile()*0.90f), random.nextFloat() * 360.0f));
        fleet.setLocation(loc.x, loc.y);
        fleet.setFacing(random.nextFloat() * 360.0f);

        //update
        fleetUtil.update(fleet, random);

        //add to mem IMPORTANT
        List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
        fleets.add(new fleetInfo(fleet, null, fleet.getContainingLocation().createToken(fleet.getLocation())));
        fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);

        log("loanShark SPAWNED " + fleet.getName() + " size " + combatPoints);
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }
}
