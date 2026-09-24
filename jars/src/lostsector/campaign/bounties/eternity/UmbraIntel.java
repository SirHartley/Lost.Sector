package lostsector.campaign.bounties.eternity;

import lostsector.campaign.events.hints.HintManager;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.bounties.eternity.EternitySpawner;

import java.awt.*;
import java.util.Set;

public class UmbraIntel extends BaseIntelPlugin {

    private final CampaignFleetAPI fleet;
    private final FleetMemberAPI flagship;
    private boolean gone = false;
    static void log(final String message) {
        Global.getLogger(UmbraIntel.class).info(message);
    }
    public UmbraIntel(CampaignFleetAPI fleet) {
        this.fleet = fleet;
        this.flagship = fleet.getFlagship();
        Global.getSector().addScript(this);

        //remove hint intel on discovery
        HintManager.removeHintIntel();
    }

    @Override
    public void advance(float amount) {
        if (fleet.getFlagship()==null || flagship != fleet.getFlagship()) {
            gone = true;
        }
        if (gone){
            if (isImportant()) {
                endAfterDelay();
            } else endImmediately();
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

        String systemString = EternitySpawner.getLoc().getContainingLocation().getName();
        String entityString = EternitySpawner.getLoc().getName();

        bullet(info);
        if (!gone){
            info.addPara("Hiding in the "+systemString+" orbiting "+entityString, initPad, g, h, systemString, entityString);
            info.addPara("Unknown reward", initPad, g, h, "", "");
        }

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
        if (!gone){
            info.addPara("You've found a mysterious seemingly malfunctioning Enigma fleet with an unusual flagship hiding in a nebula system.", opad);
        } else{
            info.addPara("You were able to defeat the commanders fleet, good riddance.", opad);
            addDeleteButton(info, width);
        }

        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", "umbra");
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_BOUNTY);
        return tags;
    }

    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_2;
    }

    public String getSortString() {
        return "Commander Umbra's Fleet";
    }

    public String getName() {
        return "Commander Umbra's Fleet";
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
        return EternitySpawner.getLoc();
    }

    @Override
    public String getCommMessageSound() {
        return getSoundMajorPosting();
    }
}



