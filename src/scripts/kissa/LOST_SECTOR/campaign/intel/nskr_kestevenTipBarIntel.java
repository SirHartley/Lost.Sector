package scripts.kissa.LOST_SECTOR.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.quests.nskr_kestevenTipBar;

import java.awt.*;
import java.util.Map;
import java.util.Set;

public class nskr_kestevenTipBarIntel extends BaseIntelPlugin {

    private boolean visited = false;
    CampaignFleetAPI pf;
    private final StarSystemAPI system;
    private final String id;
    private final String type;
    static void log(final String message) {
        Global.getLogger(nskr_kestevenTipBarIntel.class).info(message);
    }

    //Initializer function
    public nskr_kestevenTipBarIntel(StarSystemAPI system, String id, String type) {
        this.system = system;
        this.id = id;
        this.type = type;
        Global.getSector().addScript(this);
    }

    @Override
    public void advance(float amount) {
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

    //The function for adding all bullet-points in the Intel tooltip.
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

    //The function for writing the detailed info in the Intel screen.
    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        info.setParaSmallInsignia();
        info.addPara(getName(), c, 0f);
        info.setParaFontDefault();
        addBulletPoints(info, mode);
    }

    //The small description for the intel screen.
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

    //Sets which icon the Intel screen should display. Can vary based on circumstances, but a single one often works just fine
    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", "hint");
    }

    //This sets which "tags" the even has in the Intel screen. For example, giving it the Tags.INTEL_STORY tag makes it appear in the "Story" sub-category
    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_FLEET_LOG);
        tags.add(Tags.INTEL_EXPLORATION);
        return tags;
    }

    //Sorting-related; see it as a form of "how important is the even" thingy. Lower number = more important
    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_3;
    }

    //What string to sort with, when sorting alphabetically
    public String getSortString() {
        return "Purchased Intel";
    }

    //The name of the event; can vary based on circumstances. I decided to just make it say "completed" when completed
    public String getName() {
        return "Purchased Intel";
    }

    //Here, you can set which faction's UI colors to use. The default is to use the player's faction.
    @Override
    public FactionAPI getFactionForUIColors() {
        return super.getFactionForUIColors();
    }

    //This just seems to call back to the name again
    public String getSmallDescriptionTitle() {
        return getName();
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return system.getHyperspaceAnchor();
    }

    //Which sound the Comms should make from getting the intel. Some default values include:
    //  getSoundMajorPosting();
    //  getSoundStandardUpdate();
    //  getSoundLogUpdate();
    //  getSoundColonyThreat();
    //  getSoundStandardPosting();
    //  getSoundStandardUpdate();
    //Other values can be inputted, from sounds.json
    @Override
    public String getCommMessageSound() {
        return getSoundMajorPosting();
    }

    //clear from memory
    public void cleanTipLocation(){
        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(nskr_kestevenTipBar.PAID_FOR_INFO_LOC+id, null);

    }
}



