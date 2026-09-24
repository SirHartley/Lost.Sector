package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.kesteven.quest.CacheCoreDialog;
import lostsector.campaign.kesteven.quest.QuestHelper;

import java.awt.*;
import java.util.Set;

public class CacheIntel extends BaseIntelPlugin {

    private final StarSystemAPI system;
    static void log(final String message) {
        Global.getLogger(CacheIntel.class).info(message);
    }

    public CacheIntel(StarSystemAPI system) {
        this.system = system;
        Global.getSector().addScript(this);
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
        boolean salvaged = QuestHelper.getCompleted(CacheCoreDialog.RECOVERED_KEY);

        if (!salvaged)info.addPara("Explore the location.", initPad, g, h, "", "");

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
        String sys = system.getName();
        boolean salvaged = QuestHelper.getCompleted(CacheCoreDialog.RECOVERED_KEY);
        if (!salvaged)info.addPara("You discovered coordinates to a hidden Domain site.", opad, tc,h, "", "");
        if (salvaged)info.addPara("The maintenance logs", opad, tc,h, "", "");

        int stage = QuestHelper.getStage();

        if (stage>=19)addDeleteButton(info, width);
        if (QuestHelper.getEndMissions()) addDeleteButton(info, width);
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

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", "tutorial");
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
        return "Cache";
    }

    public String getName() {
        return "The Cache";
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
        return Global.getSector().getStarSystem("Unknown Site").getCenter();
    }

    @Override
    public String getCommMessageSound() {
        return getSoundMajorPosting();
    }
}



