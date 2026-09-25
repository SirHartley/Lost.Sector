package lostsector.campaign.starts.hellspawn;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.Set;

public class HellSpawnJudgementIntel extends BaseIntelPlugin {

    private CampaignFleetAPI pf;
    public long time;
    private float days;

    public static final String MEMORY_KEY = "$hellSpawnJudgementIntelKey";

    static void log(final String message) {
        Global.getLogger(HellSpawnIntel.class).info(message);
    }

    public HellSpawnJudgementIntel(long time) {
        Global.getSector().addScript(this);

        this.time = time;

        Global.getSector().getMemoryWithoutUpdate().set(MEMORY_KEY, this);
    }

    @Override
    protected void advanceImpl(float amount) {
        pf = Global.getSector().getPlayerFleet();
        if (pf == null) return;

    }

    public static HellSpawnJudgementIntel get() {
        return (HellSpawnJudgementIntel) Global.getSector().getMemoryWithoutUpdate().get(MEMORY_KEY);
    }

    //updates variables, DO NOT do this in advance
    public void init(){
        pf = Global.getSector().getPlayerFleet();
        days = Global.getSector().getClock().getElapsedDaysSince(time);

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
        Color r = Misc.getNegativeHighlightColor();
        float pad = 3f;
        float opad = 10f;

        init();

        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) initPad = opad;

        bullet(info);

        if (days < HellSpawnCountdownModule.JUDGEMENT_DAYS-7f) info.addPara("You will be judged soon.", opad, g, h, "soon");
        else info.addPara("You will be judged very soon.", opad, g, h, "very soon");
        info.addPara("Prepare accordingly.", 2f, g, h, "soon");

        unindent(info);
    }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
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

        info.addPara("JUDGEMENT", HellSpawnEventIntel.BAR_COLOR, opad);

        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    @Override
    public String getIcon() {
        return "graphics/icons/markets/plundered.png";
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add("Hellspawn");
        return tags;
    }

    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_3;
    }

    public String getSortString() {
        return "Judgement";
    }

    public String getName() {
        return "Judgement";
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
