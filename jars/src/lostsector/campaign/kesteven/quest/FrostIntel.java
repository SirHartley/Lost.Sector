package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.helper.MiscHelper;
import lostsector.world.systems.frost.Frost;

import java.awt.*;
import java.util.Set;

public class FrostIntel extends BaseIntelPlugin {

    private boolean gone = false;
    static void log(final String message) {
        Global.getLogger(FrostIntel.class).info(message);
    }
    public FrostIntel() {
        Global.getSector().addScript(this);
    }

    @Override
    public void advance(float amount) {

    }

    //updates variables, DO NOT do this in advance
    private void init(){
        if (!MiscHelper.enigmaExists()){
            endAfterDelay();
            gone = true;
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
        init();

        bullet(info);

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
        init();

        String sys = Global.getSector().getStarSystem(Frost.getName()).getName();
        info.addPara("The "+sys+"", opad, h,h, sys, "");
        if (!gone){
            info.addPara("You found a strange forgotten system brimming with signals and sensor contacts. The hostile conditions make thorough exploration difficult.", opad, h, "hostile", "");
        } else{
            info.addPara("With the destruction of Frozen Heart the system falls quiet for the first time in centuries.", opad, h, "Frozen Heart", "");
        }
        addDeleteButton(info, width);

        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", "frost");
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
        return IntelSortTier.TIER_2;
    }

    public String getSortString() {
        String sys = Global.getSector().getStarSystem(Frost.getName()).getName();
        return sys;
    }

    public String getName() {
        String sys = Global.getSector().getStarSystem(Frost.getName()).getName();
        return "The "+sys;
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
        return Global.getSector().getStarSystem(Frost.getName()).getHyperspaceAnchor();
    }

    @Override
    public String getCommMessageSound() {
        return getSoundMajorPosting();
    }
}



