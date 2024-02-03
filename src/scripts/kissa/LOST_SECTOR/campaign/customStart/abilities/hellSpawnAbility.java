package scripts.kissa.LOST_SECTOR.campaign.customStart.abilities;

import com.fs.graphics.C;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.customStart.hellSpawnManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.fleetInfo;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleFleet;
import scripts.kissa.LOST_SECTOR.util.campaignBlastSpriteCreator;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class hellSpawnAbility extends BaseDurationAbility {

    public static final String HELL_FLEET_KEY = "$hellSpawnAbilityFleet";
    public static final String FLEET_ARRAY_KEY = "$hellSpawnAbilityFleetsArray";

    public static float COOLDOWN = 120.0f;
    public static float FLEET_SIZE_FRACTION = 0.25f;
    public static int MAX_FLEETS = 4;
    public static int MIN_FLEETS = 3;
    public static float MAX_DURATION = 30.0f;

    private final Random random;
    public hellSpawnAbility(){
        this.random = new Random();
    }

    @Override
    protected void activateImpl() {

        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;

        //Global.getSoundPlayer().playSound("nskr_gate_conduit_activate", 1f,1f, fleet.getLocation(), new Vector2f());

        fleet.getStats().getSensorRangeMod().modifyMult(getModId(), 1f + (0.5f - 1f), "Gate Conduit");
        fleet.getStats().getDetectedRangeMod().modifyMult(getModId(), 2f, "Gate Conduit");
        fleet.getStats().getFleetwideMaxBurnMod().modifyFlat(getModId(), (int)(-10f), "Gate Conduit");
        fleet.getStats().getAccelerationMult().modifyMult(getModId(), 1f + (0.5f - 1f));

        //ping
        Global.getSector().addPing(fleet, Pings.SENSOR_BURST);
        Global.getSector().addPing(fleet, Pings.INTERDICT);

    }


    @Override
    protected void applyEffect(float amount, float level) {



    }

    @Override
    protected void deactivateImpl() {
        //spawn
        spawnHellFleets();

        cleanupImpl();
    }

    private void spawnHellFleets(){
        CampaignFleetAPI pf = getFleet();
        if (pf==null) return;

        //Random random = hellSpawnManager.getRandom();
        int num = mathUtil.getSeededRandomNumberInRange(MIN_FLEETS, MAX_FLEETS, random);
        for (int x = 0; x<num;x++) {
            float combatPoints = pf.getFleetPoints() * FLEET_SIZE_FRACTION;

            ArrayList<String> keys = new ArrayList<>();
            //aggro
            keys.add(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON);
            keys.add(MemFlags.FLEET_FIGHT_TO_THE_LAST);
            keys.add(HELL_FLEET_KEY);

            simpleFleet simpleFleet = new simpleFleet(pf, "enigma", combatPoints, keys, random);
            simpleFleet.aiFleetProperties = true;
            simpleFleet.name = "Swarm" + " " + util.getRandomGreekLetter(random, true);
            simpleFleet.assignment = FleetAssignment.PATROL_SYSTEM;
            simpleFleet.assignmentText = "seeking";
            CampaignFleetAPI fleet = simpleFleet.create();

            //spawning
            final Vector2f loc = new Vector2f(MathUtils.getPointOnCircumference(pf.getLocation(), mathUtil.getSeededRandomNumberInRange(100f, 400f, random), random.nextFloat() * 360.0f));
            fleet.setLocation(loc.x, loc.y);
            fleet.setFacing(random.nextFloat() * 360.0f);
            //sound
            Global.getSoundPlayer().playSound("jump_point_open", 1f,1f, loc, new Vector2f());
            //ping
            Global.getSector().addPing(fleet, Pings.WARNING_BEACON3);
            //warp in
            fleet.getStats().addTemporaryModMult(1F, getModId(), "Warping-in", 0.1F, fleet.getStats().getAccelerationMult());

            campaignBlastSpriteCreator blast = campaignBlastSpriteCreator.setupRender(loc, fleet, random, 5f, new Color(225, 29, 66,100));
            blast.size = 500f;
            blast.customSpritePath = "graphics/fx/explosion_ring0.png";
            blast.sizeEaseOutSine = true;
            blast.alphaEaseOutSine = true;
            blast.endSizeMult = 0.8f;

            //add to mem IMPORTANT
            List<fleetInfo> fleets = fleetUtil.getFleets(FLEET_ARRAY_KEY);
            fleets.add(new fleetInfo(fleet, null, pf));
            fleetUtil.setFleets(fleets, FLEET_ARRAY_KEY);
        }
    }

    @Override
    protected void cleanupImpl() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;

        fleet.getStats().getSensorRangeMod().unmodify(getModId());
        fleet.getStats().getDetectedRangeMod().unmodify(getModId());
        fleet.getStats().getFleetwideMaxBurnMod().unmodify(getModId());
        fleet.getStats().getAccelerationMult().unmodify(getModId());

    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;

        Color h = Misc.getHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 10f;

        tooltip.addPara("Gate Conduit", pad, Misc.getBasePlayerColor(), h, "");
        if (isOnCooldown()){
            tooltip.addPara("On cooldown for "+(int)(getCooldownLeft())+" days.", pad, g, r, (int)(getCooldownLeft())+"");
        }
        tooltip.addPara("Channel for a short moment and call in a group of "+ hellSpawnAbility.MIN_FLEETS+"-"+hellSpawnAbility.MAX_FLEETS+" " +
                "Enigma swarms, they will attack any nearby targets.", pad, tc, h, hellSpawnAbility.MIN_FLEETS+"-"+hellSpawnAbility.MAX_FLEETS);
        tooltip.addPara("Swarms will assist for "+(int)(MAX_DURATION)+" days.", 3f, tc, h, (int)(MAX_DURATION)+" days");
        tooltip.addPara("Swarm size "+(int)(getFleet().getFleetPoints()*FLEET_SIZE_FRACTION)+" points.", 3f, tc, h, (int)(getFleet().getFleetPoints()*FLEET_SIZE_FRACTION)+"");
        tooltip.addPara("Reduced speed and increased sensor profile while channeling.", 3f, tc, h, "");

        addIncompatibleToTooltip(tooltip, expanded);
    }

    public boolean hasTooltip() {
        return true;
    }

    @Override
    public void fleetLeftBattle(BattleAPI battle, boolean engagedInHostilities) {
        if (engagedInHostilities) {
            deactivate();
        }
    }
    @Override
    public void fleetOpenedMarket(MarketAPI market) {
        deactivate();
    }

    @Override
    public boolean isUsable() {
        return super.isUsable() && getFleet() != null;
    }
    @Override
    public boolean showCooldownIndicator() {
        return super.showCooldownIndicator();
    }
    @Override
    public boolean isOnCooldown() {
        return super.getCooldownFraction() < 1f;
    }

}