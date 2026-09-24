package lostsector.campaign.starts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import lostsector.persistence.CampaignTimer;

import java.util.Map;

public class GameModeManager extends BaseCampaignEventListener implements EveryFrameScript {

    public static final String MODE_KEY = "gamemodeManagerMode";

    CampaignTimer timer;

    public GameModeManager() {
        super(false);
        this.timer = new CampaignTimer(this.getClass().getName(), 1f);

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

    public enum GameMode{
        DEFAULT,
        THRONESGIFT,
        HELLSPAWN
    }

    public static void setMode(GameMode mode){

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(MODE_KEY, mode);

    }

    public static GameMode getMode(){

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (data.containsKey(MODE_KEY)){
            return(GameMode) data.get(MODE_KEY);
        } else {
            data.put(MODE_KEY, GameMode.DEFAULT);
            return (GameMode) data.get(MODE_KEY);
        }

    }
}
