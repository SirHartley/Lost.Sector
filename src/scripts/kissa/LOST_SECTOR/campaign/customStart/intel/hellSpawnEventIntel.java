package scripts.kissa.LOST_SECTOR.campaign.customStart.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.customStart.abilities.hellSpawnAbility;
import scripts.kissa.LOST_SECTOR.campaign.customStart.hellSpawnManager;

import java.awt.*;
import java.util.EnumSet;
import java.util.Set;

public class hellSpawnEventIntel extends BaseEventIntel implements FleetEventListener {

    public static String MEMORY_KEY = "$hellSpawnEventIntelKey";

    public static int PROGRESS_1 = 150;
    public static int PROGRESS_2 = 500;
    public static int PROGRESS_3 = 1000;
    public static int PROGRESS_MAX = 2000;

    public static Color BAR_COLOR = new Color(218, 16, 52, 255);

    private enum Stage {
        BEGINNING,
        DESCENT_MINOR,
        DESCENT_MID,
        DESCENT_MAJOR,
        INHUMAN
    }

    public hellSpawnEventIntel(TextPanelAPI text, boolean withIntelNotification){
        super();

        Global.getSector().getMemoryWithoutUpdate().set(MEMORY_KEY, this);

        setMaxProgress(PROGRESS_MAX);

        addStage(Stage.BEGINNING, 0);
        addStage(Stage.DESCENT_MINOR, PROGRESS_1, StageIconSize.LARGE);
        addStage(Stage.DESCENT_MID, PROGRESS_2, StageIconSize.LARGE);
        addStage(Stage.DESCENT_MAJOR, PROGRESS_3, false, StageIconSize.LARGE);
        addStage(Stage.INHUMAN, PROGRESS_MAX, true, StageIconSize.LARGE);

        getDataFor(Stage.DESCENT_MINOR).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.DESCENT_MID).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.DESCENT_MAJOR).keepIconBrightWhenLaterStageReached = true;

        // now that the event is fully constructed, add it and send notification
        Global.getSector().getIntelManager().addIntel(this, !withIntelNotification, text);
    }

    public static hellSpawnEventIntel get() {
        return (hellSpawnEventIntel) Global.getSector().getMemoryWithoutUpdate().get(MEMORY_KEY);
    }

    @Override
    public void advance(float amount) {

        //addFactor(new hellSpawnEventFactors(25, "freebie", "you got points", "lol"));

    }

    public void addStageDesc(TooltipMakerAPI info, Object stageId, float initPad, boolean forTooltip) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();

        //if (stageId == Stage.BEGINNING) {
        //    info.addPara("Destruction brings you closer to your goals.", initPad);

        if (stageId == Stage.DESCENT_MINOR) {
            info.addPara("+"+(int)hellSpawnManager.BURN_BONUS+" maximum burn level", initPad, h, h, (int)hellSpawnManager.BURN_BONUS+"");
            info.addPara("+"+(int)hellSpawnManager.SENSOR_RANGE+"%% sensor range", 3f, h, h, (int)hellSpawnManager.SENSOR_RANGE+"%");
            info.addPara("-"+(int)hellSpawnManager.DETECTED_AT_RANGE+"%% detected-at range", 3f, h, h, (int)hellSpawnManager.DETECTED_AT_RANGE+"%");

            info.addPara("Affects: fleet", initPad, g, Misc.getBasePlayerColor(), "fleet");
            info.addPara(""+(int)hellSpawnManager.DODGE_CHANCE+"%% chance to avoid all damage when hit", initPad, tc, h, (int)hellSpawnManager.DODGE_CHANCE+"%");

            info.addPara("Lose some humanity", g,initPad);

        } else if (stageId == Stage.DESCENT_MID) {
            info.addPara("Ability: Gate Conduit - Call in a group of "+ hellSpawnAbility.MIN_FLEETS+"-"+hellSpawnAbility.MAX_FLEETS+" " +
                    "Enigma swarms, they will attack any nearby targets. Swarm size scales with your fleet size.", initPad, tc, h, "Gate Conduit");

            info.addPara("Affects: fleet", initPad, g, Misc.getBasePlayerColor(), "fleet");
            info.addPara("Multiplies damage dealt by "+(int)hellSpawnManager.DAMAGE_BONUS+"%%", initPad, h, h, (int)hellSpawnManager.DAMAGE_BONUS+"%");
            info.addPara("Multiplies damage taken by "+(int)hellSpawnManager.DAMAGE_TAKEN+"%%", 3f, r, r, (int)hellSpawnManager.DAMAGE_TAKEN+"%");
            info.addPara("+"+(int)hellSpawnManager.OFFICER_LEVEL_BONUS+" to maximum level of officers under your command", initPad, h, h, (int)hellSpawnManager.OFFICER_LEVEL_BONUS+"");

            info.addPara("Lose some humanity", g,initPad);

        } else if (stageId == Stage.DESCENT_MAJOR) {
            info.addPara("Can recover automated ships", initPad);
            info.addPara("Automated ships always have at least "+(int)hellSpawnManager.AUTOMATED_BASE_CR+"%% max combat readiness.",
                    3f, tc, h, (int)hellSpawnManager.AUTOMATED_BASE_CR+"%");

            info.addPara("Affects: flagship", initPad, g, Misc.getBasePlayerColor(), "flagship");
            info.addPara("-"+(int)hellSpawnManager.FLAGSHIP_DP_DISCOUNT+"%% deployment point and recovery cost",
                    initPad, tc, h, "-"+(int)hellSpawnManager.FLAGSHIP_DP_DISCOUNT+"%");
            info.addPara("If flagship is a Capital +"+(int)hellSpawnManager.FLAGSHIP_CAP_BONUS+"su/s top speed", initPad, h, h, (int)hellSpawnManager.FLAGSHIP_CAP_BONUS+"su/s");
            info.addPara("If flagship is a Cruiser +"+(int)hellSpawnManager.FLAGSHIP_CRUISER_BONUS +"%% maneuverability", 3f, h, h, (int)hellSpawnManager.FLAGSHIP_CRUISER_BONUS +"%");
            info.addPara("If flagship is a Destroyer -"+(int)hellSpawnManager.FLAGSHIP_DD_BONUS+"%% non-missile weapon flux cost", 3f, h, h, (int)hellSpawnManager.FLAGSHIP_DD_BONUS+"%");
            info.addPara("If flagship is a Frigate +"+(int)hellSpawnManager.FLAGSHIP_FRIG_BONUS+"%% non-missile weapon range", 3f, h, h, (int)hellSpawnManager.FLAGSHIP_FRIG_BONUS+"%");

            info.addPara("Lose a lot of humanity", g,initPad);

        } else if (stageId == Stage.INHUMAN) {
            int min = getHellResetMin();
            info.addPara("Permanently gain a skillpoint", initPad, tc, h, "skillpoint");

            info.addPara("Lose a lot of humanity", g, initPad);

            info.addPara("Progress will be reset to %s.", opad, h, "" + min, "");
            info.addPara("Require 500 additional points to reach this level again.", 3f, h, "500", "");

        }
    }

    @Override
    public TooltipMakerAPI.TooltipCreator getStageTooltipImpl(Object stageId) {
        final EventStageData esd = getDataFor(stageId);

        if (esd != null && EnumSet.of(Stage.BEGINNING, Stage.DESCENT_MINOR,
                Stage.DESCENT_MID, Stage.DESCENT_MAJOR, Stage.INHUMAN).contains(esd.id)) {
            return new BaseFactorTooltip() {
                @Override
                public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                    float opad = 10f;

                    if (esd.id == Stage.BEGINNING) {
                        tooltip.addTitle("The Beginning");
                    } else if (esd.id == Stage.DESCENT_MINOR) {
                        tooltip.addTitle("Subversion");
                    } else if (esd.id == Stage.DESCENT_MID) {
                        tooltip.addTitle("Wrath");
                    } else if (esd.id == Stage.DESCENT_MAJOR) {
                        tooltip.addTitle("Hubris");
                    } else if (esd.id == Stage.INHUMAN) {
                        tooltip.addTitle("Inhuman");
                    }

                    addStageDesc(tooltip, esd.id, opad, true);

                    esd.addProgressReq(tooltip, opad);
                }
            };
        }

        return null;
    }

    @Override
    protected void notifyStageReached(EventStageData stage) {

        MutableFleetStatsAPI fleetStats = Global.getSector().getPlayerFleet().getStats();
        MutableCharacterStatsAPI characterStats = Global.getSector().getPlayerStats();
        if (stage.id == Stage.DESCENT_MINOR) {
            //fleet bonus
            hellSpawnManager.applyFleetBonus(fleetStats);

            hellSpawnManager.increaseLevel(1);

        }
        if (stage.id == Stage.DESCENT_MID) {
            //ability
            Global.getSector().getCharacterData().addAbility("hellSpawnAbility");

            hellSpawnManager.applyCharacterBonus(characterStats);

            hellSpawnManager.increaseLevel(1);

        }
        if (stage.id == Stage.DESCENT_MAJOR) {

            hellSpawnManager.increaseLevel(1);

            characterStats.setSkillLevel("hellSpawnSkill", 1f);
            characterStats.decreaseSkill("hellSpawnPeacefulSkill");
            characterStats.refreshCharacterStatsEffects();

        }
        if (stage.id == Stage.INHUMAN) {
            setProgress(getHellResetMin());

            hellSpawnManager.increaseLevel(1);

            //BONUS FOR LAST STAGE
            Global.getSector().getPlayerPerson().getStats().setPoints(Global.getSector().getPlayerPerson().getStats().getPoints()+1);

            //increase point req by 500
            for (BaseEventIntel.EventStageData s : getStages()){
                if (s.id==Stage.DESCENT_MINOR) s.progress += (int)(500/13.33f);
                if (s.id==Stage.DESCENT_MID) s.progress += 500/4;
                if (s.id==Stage.DESCENT_MAJOR) s.progress += 500/2;
                if (s.id==Stage.INHUMAN) s.progress += 500;
            }
            setMaxProgress(getMaxProgress()+500);
        }
    }

    public int getHellResetMin() {
        EventStageData stage = getDataFor(Stage.DESCENT_MAJOR);
        return stage.progress;
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add("Hellspawn");
        return tags;
    }

    @Override
    public boolean autoAddCampaignMessage() {
        return false;
    }

    @Override
    public boolean isImportant() {
        return true;
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {

    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {

    }

    @Override
    public Color getBarColor() {
        Color color = BAR_COLOR;
        //color = Misc.getBasePlayerColor();
        color = Misc.interpolateColor(color, Color.black, 0.25f);
        return color;
    }

    @Override
    public Color getBarProgressIndicatorColor() {
        return super.getBarProgressIndicatorColor();
    }

    @Override
    protected int getStageImportance(Object stageId) {
        return super.getStageImportance(stageId);
    }

    @Override
    protected String getName() {
        return "The Descent";
    }

    @Override
    public String getIcon() {
        return "graphics/icons/intel/damage.png";
    }

    @Override
    protected String getStageIconImpl(Object stageId) {
        EventStageData esd = getDataFor(stageId);
        if (esd == null) return null;

        //if (esd.id==Stage.BEGINNING){
        //    return "graphics/icons/intel/damage.png";
        //}
        if (esd.id==Stage.DESCENT_MINOR){
            return "graphics/icons/intel/damage.png";
        }
        if (esd.id==Stage.DESCENT_MID){
            return "graphics/icons/intel/distress_call.png";
        }
        if (esd.id==Stage.DESCENT_MAJOR){
            return "graphics/icons/intel/war.png";
        }
        if (esd.id==Stage.INHUMAN){
            return "graphics/icons/markets/pollution.png";
        }

        // should not happen - the above cases should handle all possibilities - but just in case
        return getIcon();
    }


}
