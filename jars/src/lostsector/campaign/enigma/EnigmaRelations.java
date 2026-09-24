
package lostsector.campaign.enigma;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import lostsector.campaign.starts.GameModeManager;
import lostsector.helper.Ids;
import lostsector.world.SectorGen;

public class EnigmaRelations extends BaseCampaignEventListener implements EveryFrameScript  {

    //

    static void log(final String message) {
        Global.getLogger(EnigmaRelations.class).info(message);
    }

    public EnigmaRelations() {
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
        if (faction.equals(Ids.ENIGMA_FACTION_ID)){
            //uncap on hellspawn
            if (GameModeManager.getMode() == GameModeManager.GameMode.HELLSPAWN){
                return;
            }

            SectorGen.setEnigmaRelation(Global.getSector());
        }

    }
}
