package scripts.kissa.LOST_SECTOR.campaign.customStart.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.customStart.thronesGiftManager;

import java.awt.*;
import java.util.Set;

public class thronesGiftIntel extends BaseIntelPlugin {

    public static String MEMORY_KEY = "$thronesGiftIntelKey";
    public static final String BUTTON_OPEN = "throneButtonOpen";

    private float points = 0f;
    private CampaignFleetAPI pf;

    static void log(final String message) {
        Global.getLogger(thronesGiftIntel.class).info(message);
    }

    //Initializer function
    public thronesGiftIntel() {
        Global.getSector().addScript(this);

        Global.getSector().getMemoryWithoutUpdate().set(MEMORY_KEY, this);
    }

    @Override
    public void advance(float amount) {
        pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;


    }

    public static thronesGiftIntel get() {
        return (thronesGiftIntel) Global.getSector().getMemoryWithoutUpdate().get(MEMORY_KEY);
    }

    //updates variables, DO NOT do this in advance
    public void init(){
        pf = Global.getSector().getPlayerFleet();
        points = thronesGiftManager.getDpAvailable();

    }

    @Override
    public boolean isImportant() {
        return true;
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

        init();

        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) initPad = opad;

        bullet(info);

        float xp = thronesGiftManager.XP_PER_UNLOCK - thronesGiftManager.getXpGained();
        info.addPara(""+(int)xp+" XP left until next automation point unlock. Gain "+(int)thronesGiftManager.DP_PER_UNLOCK+" extra automation points per unlock.",
                opad, g, h, (int)xp+"", (int)thronesGiftManager.DP_PER_UNLOCK+"");

        info.addPara("You have "+(int)points+" automation points available.", opad, g, h, (int)points+"", "");

        if (mode==ListInfoMode.IN_DESC) {
            ButtonAPI button = info.addButton("Automate Ships", BUTTON_OPEN, 120f, 24f, opad);
            if (points <= 0f) button.setEnabled(false);
        }

        unindent(info);
    }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        if (buttonId == BUTTON_OPEN) {
            ui.showDialog(pf, new automateDialog(ui));
        }

        super.buttonPressConfirmed(buttonId, ui);
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



        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    //Sets which icon the Intel screen should display. Can vary based on circumstances, but a single one often works just fine
    @Override
    public String getIcon() {
        return "graphics/icons/missions/blueprint_location.png";
    }

    //This sets which "tags" the even has in the Intel screen. For example, giving it the Tags.INTEL_STORY tag makes it appear in the "Story" sub-category
    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add("Throne's Gift");
        //tags.add(StringHelper.getString("exerelin_misc", "intelTagPersonal"));
        return tags;
    }

    //Sorting-related; see it as a form of "how important is the even" thingy. Lower number = more important
    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_3;
    }

    //What string to sort with, when sorting alphabetically
    public String getSortString() {
        return "Throne's Gift";
    }

    //The name of the event; can vary based on circumstances. I decided to just make it say "completed" when completed
    public String getName() {
        return "Throne's Gift";
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
        return pf;
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

