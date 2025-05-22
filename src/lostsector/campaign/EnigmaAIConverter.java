package lostsector.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import lostsector.util.FleetUtil;
import lostsector.util.IdsLS;
import lostsector.util.MiscLS;


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
        if (fleet.getFaction().getId().equals(IdsLS.ENIGMA_FACTION_ID)) {
            FleetUtil.setAIOfficers(fleet);
            return;
        }
        //check for Enigma ships
        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
            if (m.isFighterWing()) continue;
            if (MiscLS.isProtTech(m)) {
                String protOrEnigma = MiscLS.protOrEnigma(m);
                if (protOrEnigma!=null && protOrEnigma.equals("enigma")){
                    FleetUtil.setAIOfficer(m);
                }
            }
        }
    }
}