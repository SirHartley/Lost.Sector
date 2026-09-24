package lostsector.campaign.enigma;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import lostsector.helper.FleetHelper;
import lostsector.helper.Ids;
import lostsector.helper.MiscHelper;


public class EnigmaAIConverter extends BaseCampaignEventListener implements EveryFrameScript {

    public EnigmaAIConverter() {
        super(false);
    }

    static void log(final String message) {
        Global.getLogger(EnigmaAIConverter.class).info(message);
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
    }

    //ENIGMA officer hacks
    @Override
    public void reportFleetSpawned(CampaignFleetAPI fleet) {
        if (fleet.getFaction().getId().equals(Ids.ENIGMA_FACTION_ID)) {
            FleetHelper.setAIOfficers(fleet);
            return;
        }
        //check for Enigma ships
        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
            if (m.isFighterWing()) continue;
            if (MiscHelper.isProtTech(m)) {
                String protOrEnigma = MiscHelper.protOrEnigma(m);
                if (protOrEnigma!=null && protOrEnigma.equals("enigma")){
                    FleetHelper.setAIOfficer(m);
                }
            }
        }
    }
}