package lostsector.campaign.kesteven.tips;

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

import java.awt.*;
import java.util.Map;
import java.util.Set;

public class KestevenTipIntel extends BaseIntelPlugin {

    private boolean visited = false;
    CampaignFleetAPI pf;
    private final StarSystemAPI system;
    private final String id;
    private final String type;
    static void log(final String message) {
        Global.getLogger(KestevenTipIntel.class).info(message);
    }

    public KestevenTipIntel(StarSystemAPI system, String id, String type) {
        this.system = system;
        this.id = id;
        this.type = type;
        Global.getSector().addScript(this);
    }

    @Override
    protected void advanceImpl(float amount) {
        this.pf = Global.getSector().getPlayerFleet();
        if (this.pf == null) return;

        if (pf.getContainingLocation()==system){
            visited = true;
            endAfterDelay();
        }
    }

    @Override
    protected void notifyEnded() {
        super.notifyEnded();
        cleanTipLocation();
        log("tipIntel cleaning");
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
        info.addPara("Explore the "+sys+", it was said to contain a "+type+" threat.", opad, g,h, sys, type);

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
            info.addPara("You purchased coordinates for the "+system.getName()+" from a Kesteven officer.", opad,tc, h, "", "");
        } else{
            info.addPara("Time to see if the officer was true to their word.", opad,tc, h, "", "");
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
        return "Purchased Intel";
    }

    public String getName() {
        return "Purchased Intel";
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

    //clear from memory
    public void cleanTipLocation(){
        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(KestevenTipBarEvent.PAID_FOR_INFO_LOC+id, null);

    }
}



