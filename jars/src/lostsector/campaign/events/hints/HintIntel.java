package lostsector.campaign.events.hints;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.world.systems.frost.Frost;

import java.awt.*;
import java.util.Set;

public class HintIntel extends BaseIntelPlugin {

    private boolean visited = false;
    CampaignFleetAPI pf;
    public final StarSystemAPI system;
    static void log(final String message) {
        Global.getLogger(HintIntel.class).info(message);
    }

    public HintIntel(StarSystemAPI system) {
        this.system = system;
        Global.getSector().addScript(this);
    }

    @Override
    protected void advanceImpl(float amount) {
        this.pf = Global.getSector().getPlayerFleet();
        if (this.pf == null) return;

        if (pf.getContainingLocation()==system){
            visited = true;
            //end immediately for frost
            if (system==Global.getSector().getStarSystem(Frost.getName())){
                endImmediately();
            } else {
                endAfterDelay();
            }
        }
    }

    @Override
    protected void notifyEnded() {
        super.notifyEnded();
        Global.getSector().removeScript(this);
    }

    @Override
    public void endAfterDelay() {
        super.endAfterDelay();
    }

    @Override
    protected void notifyEnding() {
        super.notifyEnding();
    }

    @Override
    public boolean shouldRemoveIntel() {
        return super.shouldRemoveIntel();
    }

    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) initPad = opad;

        bullet(info);

        String sys = system.getName();
        info.addPara("Explore the "+sys+".", opad, g,h, sys, "");

        unindent(info);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        info.setParaSmallInsignia();
        info.addPara(getName(), c, 0f);
        info.setParaFontDefault();
        addBulletPoints(info, mode);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;
        if (!visited){
            info.addPara("Your comms team reported a strange burst of signals coming from the "+system.getName()+". Exploration of the location should yield more answers.", opad,tc, h, "", "");
        } else{
            info.addPara("You've reached the origin of the signals, now to just find the source.", opad,tc, h, "source", "");
        }
        if (visited) {
            addDeleteButton(info, width);
        }

        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", "hint");
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_FLEET_LOG);
        tags.add(Tags.INTEL_EXPLORATION);
        return tags;
    }

    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_3;
    }

    public String getSortString() {
        return "Mysterious signals";
    }

    public String getName() {
        return "Mysterious signals";
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return super.getFactionForUIColors();
    }

    public String getSmallDescriptionTitle() {
        return getName();
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return system.getHyperspaceAnchor();
    }

    @Override
    public String getCommMessageSound() {
        return getSoundMajorPosting();
    }
}



