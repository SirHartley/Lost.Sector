package lostsector.campaign.kesteven.quest;

import lostsector.helper.fleet.FleetInfo;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import lostsector.ModPlugin;
import lostsector.helper.FleetHelper;
import lostsector.helper.Ids;

import java.util.ArrayList;
import java.util.List;

public class QuestStageManager extends BaseCampaignEventListener implements EveryFrameScript  {
    //
    //manages quest stage changes and mission fleets
    public static final String FLEET_ARRAY_KEY = "$kQuestMissionFleets";
    public static final ArrayList<String> JOB3_MARKET_BLACKLIST = new ArrayList<>();
    static {
        JOB3_MARKET_BLACKLIST.add("eochu_bres");
        JOB3_MARKET_BLACKLIST.add("culann");
    }

    private int stage =0;
    private int frameWait2 = 0;

    private final List<CampaignFleetAPI> removed = new ArrayList<>();

    public QuestStageManager() {
        super(false);
    }

    static void log(final String message) {
        Global.getLogger(QuestStageManager.class).info(message);
    }

    public boolean isDone() {
        return false;
    }
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;
        // QuestManager creates the state at the end of ModPlugin.onGameLoad, so it exists here unless the quest is unavailable.
        KestevenState state = KestevenQuest.state();
        if (state == null) return;

        stage = QuestHelper.getStage();

        //////////////////
        //pause check
        //////////////////
        if (Global.getSector().isPaused()) return;
        //timer
        if (Global.getSector().isInFastAdvance()) {
            state.fleetCounter += 2f*amount;
        } else{
            state.fleetCounter += amount;
        }
        //FLEETS
        List<FleetInfo> fleets = FleetHelper.getFleets(FLEET_ARRAY_KEY);

        //fleet logic once a seconds (10s is a day)
        if (state.fleetCounter>1f){
            //fleet logic
            runFleetLogic(fleets);

            //clean the list
            FleetHelper.cleanUp(removed, fleets);
            removed.clear();
            //save to mem
            FleetHelper.setFleets(fleets, FLEET_ARRAY_KEY);

            //
            state.fleetCounter = 0f;
        }
    }

    private void runFleetLogic(List<FleetInfo> fleets){
        //mission related fleets manager
        for (FleetInfo f : fleets) {
            //age update
            f.age+=0.1f;
        }
    }

}
