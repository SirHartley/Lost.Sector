package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;
import lostsector.helper.MiscHelper;

import java.util.Map;
import java.util.Random;

public class KestevenTipBarEventCreator extends BaseBarEventCreator {

    public static final String PERSISTENT_RANDOM_KEY = "nskr_kestevenTipBarManagerRandom";

    public PortsideBarEvent createBarEvent() {
        return new KestevenTipBarEvent(getRandom().nextLong()+"");
    }

    @Override
    public boolean isPriority() {
        return false;
    }

    public float getBarEventActiveDuration() {
        return 15f + (float) Math.random() * 15f;
    }

    public float getBarEventTimeoutDuration() {
        return Math.max(0, 30f - (float) Math.random() * 40f);
    }

    @Override
    public float getBarEventAcceptedTimeoutDuration() {
        return 30f + (float) Math.random() * 30f;
    }

    public Random getRandom() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

            data.put(PERSISTENT_RANDOM_KEY, new Random(MiscHelper.getSeedParsed()));
        }
        return (Random)data.get(PERSISTENT_RANDOM_KEY);
    }
}
