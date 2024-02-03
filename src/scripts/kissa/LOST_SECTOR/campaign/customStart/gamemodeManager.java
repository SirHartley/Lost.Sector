package scripts.kissa.LOST_SECTOR.campaign.customStart;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import scripts.kissa.LOST_SECTOR.campaign.util.campaignTimer;

import java.util.Map;

public class gamemodeManager extends BaseCampaignEventListener implements EveryFrameScript {

    public static final String MODE_KEY = "gamemodeManagerMode";

    campaignTimer timer;

    public gamemodeManager() {
        super(false);
        this.timer = new campaignTimer(this.getClass().getName(), 1f);

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


        //PAUSE CHECK
        if (Global.getSector().isPaused()) return;

        timer.advance(amount);
        if (timer.onTimeout()){

            timer.val();
        }

    }

    public enum gameMode{
        DEFAULT,
        THRONESGIFT,
        HELLSPAWN
    }

    public static void setMode(gameMode mode){

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(MODE_KEY, mode);

    }

    public static gameMode getMode(){

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (data.containsKey(MODE_KEY)){
            return(gameMode) data.get(MODE_KEY);
        } else {
            data.put(MODE_KEY, gameMode.DEFAULT);
            return (gameMode) data.get(MODE_KEY);
        }

    }
}
