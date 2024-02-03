package scripts.kissa.LOST_SECTOR.campaign.util;

import com.fs.starfarer.api.Global;

import java.util.ArrayList;
import java.util.Map;

public class campaignTimer {

    static ArrayList<campaignTimer> INSTANCES = new ArrayList();

    private float timer = 0f;
    private float timeout;
    private String id;
    private boolean timedout = false;

    public campaignTimer(String id, float timeout){

        String stringId = id+"Timer";
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(stringId)) {
            data.put(stringId, this);
        }

        this.id = stringId;
        this.timer = ((campaignTimer) data.get(stringId)).timer;
        this.timeout = timeout;
        this.timedout = ((campaignTimer) data.get(stringId)).timedout;

        INSTANCES.add(this);
    }

    public float val() {
        return timer;
    }

    public void advance(float amount) {
        if (Global.getSector().isPaused()) return;

        if (Global.getSector().isInFastAdvance()) {
            timer += 2f*amount;
        } else{
            timer += amount;
        }

        if (timeout>0f) {
            if (timer > timeout) {
                timedout = true;
                reset();
            }
        }
    }

    public boolean onTimeout(){
        if (timedout){
            timedout = false;
            return true;
        }
        return false;
    }

    public void reset(){
        timer = 0f;
    }

    public static void save(){
        for (campaignTimer timer : INSTANCES){
            Map<String, Object> data = Global.getSector().getPersistentData();
            data.put(timer.id, timer);
        }
    }

}
