package scripts.kissa.LOST_SECTOR.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.EngagementResultForFleetAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class nskr_enigmaHullmodListener extends BaseCampaignEventListener implements EveryFrameScript {

    public static final int REQUIRED_PER_UNLOCK = 16;

    public static final String COUNT_MEM_KEY = "nskr_enigmaHullmodListenerCount";
    public static final String UNLOCKS_MEM_KEY = "nskr_enigmaHullmodListenerUnlocks";

    public static final String PERSISTENT_RANDOM_KEY = "nskr_enigmaHullmodListenerRandom";

    public static final String KEY_BASE = "nskr_enigmaHullmodListenerTip";
    public static final ArrayList<String> DEFAULT_KEYS = new ArrayList<>();
    static {
        DEFAULT_KEYS.add(KEY_BASE+"#1");
        DEFAULT_KEYS.add(KEY_BASE+"#2");
        DEFAULT_KEYS.add(KEY_BASE+"#3");
        //DEFAULT_KEYS.add(KEY_BASE+"#4");
        DEFAULT_KEYS.add(KEY_BASE+"#5");
        DEFAULT_KEYS.add(KEY_BASE+"#6");
        DEFAULT_KEYS.add(KEY_BASE+"#7");
        DEFAULT_KEYS.add(KEY_BASE+"#8");
        DEFAULT_KEYS.add(KEY_BASE+"#9");
        DEFAULT_KEYS.add(KEY_BASE+"#10");
    }

    static void log(final String message) {
        Global.getLogger(nskr_enigmaHullmodListener.class).info(message);
    }

    public nskr_enigmaHullmodListener() {
        super(false);
        //init randoms
        getRandom();
    }

    public boolean isDone() {
        return false;
    }
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {

        //
        if (Global.getSector().isPaused()) return;

    }

    public static void update() {

        int count = getCount();
        //log("totalCount "+count);
        ArrayList<String> unlocks = getUnlocks(UNLOCKS_MEM_KEY);
        //everything unlocked
        if (unlocks.size()>=DEFAULT_KEYS.size()) return;
        Random random = getRandom();

        while (count >= REQUIRED_PER_UNLOCK){
            //update
            count = getCount();
            //safety
            if (unlocks.size()>=DEFAULT_KEYS.size()) break;
            //need to re-check
            if (count >= REQUIRED_PER_UNLOCK) {
                String key = DEFAULT_KEYS.get(mathUtil.getSeededRandomNumberInRange(0, DEFAULT_KEYS.size() - 1, random));
                //pick until new one
                while (unlocks.contains(key)) {
                    key = DEFAULT_KEYS.get(mathUtil.getSeededRandomNumberInRange(0, DEFAULT_KEYS.size() - 1, random));
                }
                //add
                unlocks.add(key);
                setCount(Math.max(count - REQUIRED_PER_UNLOCK, 0));
            }
        }

        //set
        setUnlocks(UNLOCKS_MEM_KEY, unlocks);
    }

    @Override
    public void reportPlayerEngagement(EngagementResultAPI result) {
        EngagementResultForFleetAPI enemy = result.getLoserResult();
        EngagementResultForFleetAPI player = result.getWinnerResult();
        if (enemy.isPlayer()){
            enemy = result.getWinnerResult();
            player = result.getLoserResult();
        }

        if (enemy == null || player == null) return;
        if (enemy.getFleet() == null || player.getFleet() == null) return;

        ArrayList<FleetMemberAPI> lost = new ArrayList<>();
        lost.addAll(enemy.getDisabled());
        lost.addAll(enemy.getDestroyed());

        int shipCount = getCount();

        if (lost.size()>0){
            for (FleetMemberAPI member : player.getDeployed()){
                if (member==null) continue;
                if (member.isFighterWing()) continue;
                if (!util.isProtTech(member)) continue;
                String type = util.protOrEnigma(member);
                if (type==null || !type.equals("enigma")) continue;

                shipCount++;
                //log(member.getHullId()+" counted");
            }
        }
        //log("count "+shipCount);
        //set
        setCount(shipCount);
    }

    public static int getCount() {
        String id = COUNT_MEM_KEY;

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, 0);

        return (int)data.get(id);
    }

    public static void setCount(int count) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(COUNT_MEM_KEY, count);
    }

    public static ArrayList<String> getUnlocks(String id) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (data.containsKey(id)){
            return (ArrayList<String>)data.get(id);
        } else {
            //init default
            data.put(id, new ArrayList<String>());
        }
        return (ArrayList<String>)data.get(id);
    }

    public static void setUnlocks(String id, ArrayList<String> unlocks) {

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(id, unlocks);
    }

    public static Random getRandom() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

            data.put(PERSISTENT_RANDOM_KEY,  new Random(new Random().nextLong()));
        }
        return (Random) data.get(PERSISTENT_RANDOM_KEY);
    }
}