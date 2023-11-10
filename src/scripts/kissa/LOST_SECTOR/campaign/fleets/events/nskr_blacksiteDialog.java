package scripts.kissa.LOST_SECTOR.campaign.fleets.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.util.powerLevel;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class nskr_blacksiteDialog implements InteractionDialogPlugin {

    //
    public static final String PERSISTENT_KEY = "nskr_blacksiteDialogKey";

    private InteractionDialogAPI dialog;
    private TextPanelAPI text;
    private OptionPanelAPI options;
    private VisualPanelAPI visual;

    static void log(final String message) {
        Global.getLogger(nskr_blacksiteDialog.class).info(message);
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        List<blacksiteInfo> sites = nskr_blacksiteManager.getSites(nskr_blacksiteManager.SITE_ARRAY_KEY);
        blacksiteInfo site = nskr_blacksiteManager.getInfo(dialog.getInteractionTarget().getId(), sites);
        if (site==null) {
            dialog.dismiss();
            return;
        }

        this.dialog = dialog;

        dialog.setOptionOnEscape("Leave", OptionId.LEAVE);

        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();
        visual = dialog.getVisualPanel();

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();

        text.setFontInsignia();

        //
        visual.showImageVisual(dialog.getInteractionTarget().getCustomInteractionDialogImageVisual());

        if (!site.active){
            FactionAPI faction = Global.getSector().getFaction(site.faction);
            String factionText = Global.getSector().getFaction(site.faction).getDisplayName();
            String str = getStrengthString(site.points);
            String fleetText = "fleet";
            if (site.count>1) fleetText = "fleets";

            text.addPara("Upon closer inspection it becomes clear that the site has multiple tripwire alarms rigged to it. There is no clear way to salvage anything of worth without triggering them.",tc,h,"");
            text.addPara("\"The whole damn thing is tamper proof.\" Your ops chief scoffs. \"According to our quick analysis of the site it appears to belong to "+ factionText +". " +
                    "They'll come and try to stop us from salvaging it. I would expect "+site.count+" "+fleetText+", with their total numbers being "+str+" to ours.\"",tc, faction.getColor(),factionText, "");
            text.addPara("\"They'll most likely try and empty the station themselves - as to not risk any of the valuable equipment falling into our hands, rather than try attacking us directly. " +
                    "So be prepared to stop them. That's all captain.\" The ops chief gives you a nod of confirmation.",tc,h,"");
            text.addPara("You can't safely take further actions with the site until all the hostile fleets are defeated or enough time has passed.",tc,h,"hostile fleets are defeated");

            options.addOption("Trip the alarm", OptionId.ACTIVATE);
        } else {
            String fleetText = "fleet";
            if (site.activeFleets>1) fleetText = "fleets";

            text.addPara("You can't safely take further actions with the site until all the hostile fleets are defeated or enough time has passed.",tc,h,"");
            text.addPara("There appears to be "+site.activeFleets+" active hostile "+fleetText+" remaining.",tc,h,site.activeFleets+"");

        }

        options.addOption("Leave", OptionId.LEAVE);

        //save to mem
        nskr_blacksiteManager.setSites(sites, nskr_blacksiteManager.SITE_ARRAY_KEY);
    }

    private String getStrengthString(float points) {
        String str = "";
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();

        points += (points * powerLevel.get(0.2f, 0f, 1f)) / 2f;
        points *= nskr_modPlugin.getScriptedFleetSizeMult();

        float ratio = points / pf.getFleetPoints();

        if (ratio<0.7f){
            str = "inferior";
        }
        if (ratio<1.3f && ratio>0.7f){
            str = "similar";
        }
        if (ratio>1.3f && ratio<1.75f) {
            str = "superior";
        }
        if (ratio>1.75f) {
            str = "overwhelming";
        }

        return str;
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        List<blacksiteInfo> sites = nskr_blacksiteManager.getSites(nskr_blacksiteManager.SITE_ARRAY_KEY);
        blacksiteInfo site = nskr_blacksiteManager.getInfo(dialog.getInteractionTarget().getId(), sites);
        if (site==null) {
            dialog.dismiss();
            return;
        }

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

        //activate
        if (optionData == OptionId.ACTIVATE) {
            dialog.setOptionOnEscape("Leave", OptionId.LEAVE_WITH_SOUND);

            text.addPara("You give the order, and shortly the alarms are tripped by one carefully placed remote charge.",tc,h,"");
            text.addPara("Get ready to defend the location.",tc,h,"");

            nskr_blacksiteManager.activate(site);

            options.addOption("Leave", OptionId.LEAVE_WITH_SOUND);
        }

        //leave
        if (optionData == OptionId.LEAVE_WITH_SOUND) {

            Global.getSoundPlayer().playUISound("ui_sensor_burst_on",1f,1f);

            dialog.dismiss();
        }
        if (optionData == OptionId.LEAVE) {

            dialog.dismiss();
        }

        //save to mem
        nskr_blacksiteManager.setSites(sites, nskr_blacksiteManager.SITE_ARRAY_KEY);
    }

    public enum OptionId {
        ACTIVATE,
        LEAVE,
        LEAVE_WITH_SOUND,
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
