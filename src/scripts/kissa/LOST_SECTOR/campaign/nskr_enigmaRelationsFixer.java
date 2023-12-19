
package scripts.kissa.LOST_SECTOR.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import scripts.kissa.LOST_SECTOR.util.ids;
import scripts.kissa.LOST_SECTOR.world.nskr_gen;

public class nskr_enigmaRelationsFixer extends BaseCampaignEventListener implements EveryFrameScript  {

    //

    static void log(final String message) {
        Global.getLogger(nskr_enigmaRelationsFixer.class).info(message);
    }

    public nskr_enigmaRelationsFixer() {
        super(false);
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

    @Override
    public void reportPlayerReputationChange(String faction, float delta) {

        //hard set relations
        if (faction.equals(ids.ENIGMA_FACTION_ID)){
            nskr_gen.setEnigmaRelation(Global.getSector());
        }

    }
}
