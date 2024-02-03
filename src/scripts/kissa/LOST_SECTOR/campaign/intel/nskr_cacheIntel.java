package scripts.kissa.LOST_SECTOR.campaign.intel;

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
import scripts.kissa.LOST_SECTOR.campaign.quests.nskr_coreDialog;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;

import java.awt.*;
import java.util.Set;

public class nskr_cacheIntel extends BaseIntelPlugin {

    private final StarSystemAPI system;
    static void log(final String message) {
        Global.getLogger(nskr_cacheIntel.class).info(message);
    }

    //Initializer function
    public nskr_cacheIntel(StarSystemAPI system) {
        this.system = system;
        Global.getSector().addScript(this);
    }

    @Override
    public void advance(float amount) {

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
        boolean salvaged = questUtil.getCompleted(nskr_coreDialog.RECOVERED_KEY);

        if (!salvaged)info.addPara("Explore the location.", initPad, g, h, "", "");

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
        String sys = system.getName();
        boolean salvaged = questUtil.getCompleted(nskr_coreDialog.RECOVERED_KEY);
        if (!salvaged)info.addPara("You discovered coordinates to a hidden Domain site.", opad, tc,h, "", "");
        if (salvaged)info.addPara("The maintenance logs", opad, tc,h, "", "");

        int stage = questUtil.getStage();

        if (stage>=19)addDeleteButton(info, width);
        if (questUtil.getEndMissions()) addDeleteButton(info, width);
        //maintenance logs
        if (salvaged)info.addPara("This is a heavily edited version of the logs. A large part of the early logs are considerably older, and just actual maintenance reports. " +
                        "More recently the drone seems to have been moved to work on \"Project : Enigma\" operations.", opad, h, "", "");
        if (salvaged)info.addPara("One of the early entries \"We have to start using these crummy old drones to store stuff because the project management deemed the site unsafe for humans. " +
                "Some type of \"P-Space interferences\" or whatever. Just some new cost cutting measure, I'm pretty sure.\"", opad, h, "", "");
        if (salvaged)info.addPara("Next entry sometime later \"No, there's definitely something going with extreme radiation or something on the site. I've never seen parts fail this fast, and these old things are built like a brick.\" "+
                        "Later in this period there are multiple rants about systems that really shouldn't fail failing and what pain in the ass they were to fix.", opad, h, "", "");
        if (salvaged)info.addPara("Over time the logs seem to get more serious in tone as now there has been some issues with the AI itself. " +
                        "\"There really is something wrong with that cursed site. Now the drones wont even complete their tasks there, totally messing up our logistics. I've never even heard of malfunctions like this, it shouldn't even be possible.\"", opad, h, "", "");
        if (salvaged)info.addPara("A few months after that entry " +
                "\"Recently two of the drones managed to destroy each other while in the site. Something more than just intense radiation is going on there, and management never knows to quit while they are ahead. Hopefully I don't get spaced by one those dimwit drones, and they said it couldn't get any worse...\" This is the last entry.", opad, h, "", "");

        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    //Sets which icon the Intel screen should display. Can vary based on circumstances, but a single one often works just fine
    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", "tutorial");
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
        return IntelSortTier.TIER_2;
    }

    //What string to sort with, when sorting alphabetically
    public String getSortString() {
        return "Cache";
    }

    //The name of the event; can vary based on circumstances. I decided to just make it say "completed" when completed
    public String getName() {
        return "The Cache";
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
        return Global.getSector().getStarSystem("Unknown Site").getCenter();
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
}



