package lostsector.campaign.starts.thronesgift;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.starts.thronesgift.ThronesGiftManager;

import java.awt.*;
import java.util.Set;

public class ThronesGiftIntel extends BaseIntelPlugin {

    public static String MEMORY_KEY = "$thronesGiftIntelKey";
    public static final String BUTTON_OPEN = "throneButtonOpen";

    private float points = 0f;
    private CampaignFleetAPI pf;

    static void log(final String message) {
        Global.getLogger(ThronesGiftIntel.class).info(message);
    }

    public ThronesGiftIntel() {
        Global.getSector().addScript(this);

        Global.getSector().getMemoryWithoutUpdate().set(MEMORY_KEY, this);
    }

    @Override
    public void advance(float amount) {
        pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;


    }

    public static ThronesGiftIntel get() {
        return (ThronesGiftIntel) Global.getSector().getMemoryWithoutUpdate().get(MEMORY_KEY);
    }

    //updates variables, DO NOT do this in advance
    public void init(){
        pf = Global.getSector().getPlayerFleet();
        points = ThronesGiftManager.getDpAvailable();

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

        float xp = ThronesGiftManager.XP_PER_UNLOCK - ThronesGiftManager.getXpGained();
        info.addPara(""+(int)xp+" XP left until next automation point unlock. Gain "+(int) ThronesGiftManager.DP_PER_UNLOCK+" extra automation points per unlock.",
                opad, g, h, (int)xp+"", (int) ThronesGiftManager.DP_PER_UNLOCK+"");

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
            ui.showDialog(pf, new AutomateDialog(ui));
        }

        super.buttonPressConfirmed(buttonId, ui);
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



        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    @Override
    public String getIcon() {
        return "graphics/icons/missions/blueprint_location.png";
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add("Throne's Gift");
        return tags;
    }

    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_3;
    }

    public String getSortString() {
        return "Throne's Gift";
    }

    public String getName() {
        return "Throne's Gift";
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
        return pf;
    }

    @Override
    public String getCommMessageSound() {
        return getSoundMajorPosting();
    }
}

