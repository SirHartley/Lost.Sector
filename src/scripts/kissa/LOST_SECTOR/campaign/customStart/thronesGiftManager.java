package scripts.kissa.LOST_SECTOR.campaign.customStart;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import scripts.kissa.LOST_SECTOR.campaign.util.campaignTimer;

import java.util.Map;

public class thronesGiftManager extends BaseCampaignEventListener implements EveryFrameScript {

    public static final float DEFAULT_DP = 20f;
    public static final float XP_PER_UNLOCK = 1000000f;
    public static final float DP_PER_UNLOCK = 10f;
    public static final String XP_KEY = "thronesGiftManagerXp";
    public static final String DP_KEY = "thronesGiftManagerDp";
    public static final String TOTAL_DP_KEY = "thronesGiftManagerTotalDp";

    private campaignTimer timer;
    private long xp = 0;
    private long oldXp = 0;
    private long lvl = 0;
    private long oldLvl = 0;

    public thronesGiftManager() {
        super(false);
        this.timer = new campaignTimer(this.getClass().getName(), 1f);
        //init
        xp = Global.getSector().getPlayerStats().getXP();
        oldXp = xp;
        lvl = Global.getSector().getPlayerStats().getLevel();
        oldLvl = lvl;
    }

    //reset onGameLoad
    public void reset() {

        xp = Global.getSector().getPlayerStats().getXP();
        oldXp = xp;
        lvl = Global.getSector().getPlayerStats().getLevel();
        oldLvl = lvl;
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

        if (gamemodeManager.getMode() != gamemodeManager.gameMode.THRONESGIFT) return;

        xp = Global.getSector().getPlayerStats().getXP();
        lvl = Global.getSector().getPlayerStats().getLevel();
        if (xp>oldXp){
            reportXpChanged(xp-oldXp);
        }
        //wrap around xp once you hit max lvl, 4x1 million xp per wrap
        if (xp<oldXp && lvl==15) {
            //Global.getSettings().
            reportXpChanged((4000000-oldXp) + xp);
        }

        //update
        oldXp = xp;
        oldLvl = lvl;

        //PAUSE CHECK
        if (Global.getSector().isPaused()) return;

        timer.advance(amount);
        if (timer.onTimeout()){



        }

    }

    @Override
    public void reportPlayerReputationChange(String faction, float delta) {

        if (gamemodeManager.getMode() != gamemodeManager.gameMode.THRONESGIFT) return;

        if (faction.equals(Factions.LUDDIC_PATH)) {
            if (Global.getSector().getPlayerFaction().getRelationship(faction) > -0.80f) {
                Global.getSector().getPlayerFaction().setRelationship(faction, -0.80f);
            }
        }

    }

    private void reportXpChanged(float delta) {

        float xp = getXpGained()+delta;
        setXpGained(xp);

        while (getXpGained()>=XP_PER_UNLOCK){
            float dp = getDpAvailable();
            dp += DP_PER_UNLOCK;
            setDpAvailable(dp);
            //total counter
            setTotalDp(getTotalDp() + DP_PER_UNLOCK);

            setXpGained(xp-XP_PER_UNLOCK);


            Global.getSector().getCampaignUI().addMessage("Gained "+(int)DP_PER_UNLOCK+" automation points.",
                    Global.getSettings().getColor("standardTextColor"),
                    (int)DP_PER_UNLOCK+"",
                    "",
                    Global.getSettings().getColor("yellowTextColor"),
                    Global.getSettings().getColor("yellowTextColor"));
            //update for loop
            xp = getXpGained();
        }

    }

    public static void setXpGained(float gained){

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(XP_KEY, gained);

    }

    public static float getXpGained(){

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (data.containsKey(XP_KEY)){
            return (float) data.get(XP_KEY);
        } else {
            data.put(XP_KEY, 0f);
            return (float) data.get(XP_KEY);
        }

    }

    public static void setDpAvailable(float dpAvailable){

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(DP_KEY, dpAvailable);

    }

    public static float getDpAvailable(){

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (data.containsKey(DP_KEY)){
            return (float) data.get(DP_KEY);
        } else {
            data.put(DP_KEY, DEFAULT_DP);
            return (float) data.get(DP_KEY);
        }

    }

    public static void setTotalDp(float dp){

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(TOTAL_DP_KEY, dp);

    }

    public static float getTotalDp(){

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (data.containsKey(TOTAL_DP_KEY)){
            return (float) data.get(TOTAL_DP_KEY);
        } else {
            data.put(TOTAL_DP_KEY, DEFAULT_DP);
            return (float) data.get(TOTAL_DP_KEY);
        }

    }

}
