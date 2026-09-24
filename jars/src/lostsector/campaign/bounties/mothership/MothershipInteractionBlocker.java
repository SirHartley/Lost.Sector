package lostsector.campaign.bounties.mothership;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.util.Misc;
import lostsector.helper.fleet.FleetInfo;
import lostsector.helper.FleetHelper;

import java.awt.*;
import java.util.Map;

public class MothershipInteractionBlocker implements InteractionDialogPlugin {

    //
    public static final String PERSISTENT_KEY = "nskr_mothershipInteractionBlockerKey";

    private InteractionDialogAPI dialog;
    private TextPanelAPI text;
    private OptionPanelAPI options;
    private VisualPanelAPI visual;

    static void log(final String message) {
        Global.getLogger(MothershipInteractionBlocker.class).info(message);
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;

        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();
        visual = dialog.getVisualPanel();

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();

        CampaignFleetAPI fleet = null;
        for (FleetInfo f : FleetHelper.getFleets(MothershipSpawner.FLEET_ARRAY_KEY)){
            fleet = f.fleet;
        }
        if (fleet==null){
            dialog.dismiss();
            return;
        }

        text.setFontInsignia();
        text.addPara("The fleet appears to be protecting this planet, and maneuvers to prevent your approach.",tc,h,"maneuvers to prevent your approach");

        dialog.setInteractionTarget(fleet);
        FleetInteractionDialogPluginImpl.FIDConfig params = new MothershipSpawner.MothershipInteractionConfig().createConfig();
        FleetInteractionDialogPluginImpl plugin = new FleetInteractionDialogPluginImpl(params);
        plugin.init(dialog);
        dialog.setPlugin(plugin);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        Color h = Misc.getHighlightColor();
        Color b = Misc.getBasePlayerColor();
        Color bh = Misc.getBrightPlayerColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color s = Misc.getStoryBrightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();

        text.addPara(optionText, b, h, "", "");
        options.clearOptions();

        //leave
        if (optionData == OptionId.LEAVE) {
            dialog.dismiss();

        }
    }

    public enum OptionId {
        LEAVE,
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {
    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        return null;
    }

}
