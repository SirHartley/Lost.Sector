package lostsector.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import lostsector.util.fleetUtil;
import lostsector.util.ids;
import lostsector.util.util;


public class nskr_enigmaAIConverter extends BaseCampaignEventListener implements EveryFrameScript {

    public nskr_enigmaAIConverter() {
        super(false);
    }

    static void log(final String message) {
        Global.getLogger(nskr_enigmaAIConverter.class).info(message);
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
        if (fleet.getFaction().getId().equals(ids.ENIGMA_FACTION_ID)) {
            fleetUtil.setAIOfficers(fleet);
            return;
        }
        //check for Enigma ships
        for (FleetMemberAPI m : fleet.getMembersWithFightersCopy()) {
            if (m.isFighterWing()) continue;
            if (util.isProtTech(m)) {
                String protOrEnigma = util.protOrEnigma(m);
                if (protOrEnigma!=null && protOrEnigma.equals("enigma")){
                    fleetUtil.setAIOfficer(m);
                }
            }
        }
    }
}