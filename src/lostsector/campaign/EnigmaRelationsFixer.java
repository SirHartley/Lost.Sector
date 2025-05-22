
package lostsector.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import lostsector.campaign.customStart.GamemodeManager;
import lostsector.util.LSIds;
import lostsector.world.Gen;

public class EnigmaRelationsFixer extends BaseCampaignEventListener implements EveryFrameScript  {

    //

    static void log(final String message) {
        Global.getLogger(EnigmaRelationsFixer.class).info(message);
    }

    public EnigmaRelationsFixer() {
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
        if (faction.equals(LSIds.ENIGMA_FACTION_ID)){
            //uncap on hellspawn
            if (GamemodeManager.getMode() == GamemodeManager.gameMode.HELLSPAWN){
                return;
            }

            Gen.setEnigmaRelation(Global.getSector());
        }

    }
}
