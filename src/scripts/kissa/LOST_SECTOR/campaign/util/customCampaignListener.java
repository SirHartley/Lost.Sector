package scripts.kissa.LOST_SECTOR.campaign.util;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;

public class customCampaignListener extends BaseCampaignEventListener implements EveryFrameScript {

    public customCampaignListener() {
        super(false);
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

    }

}
