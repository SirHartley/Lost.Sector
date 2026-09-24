package lostsector.campaign.bounties.peacekeepers;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.bounties.peacekeepers.RorqualSpawner;
import lostsector.campaign.bounties.BountyLoot;

import java.awt.*;
import java.util.Set;

public class RorqualIntel extends BaseIntelPlugin {

    private final CampaignFleetAPI fleet;
    private final FleetMemberAPI flagship;
    private boolean gone = false;
    private boolean payout = false;
    private boolean doOnce = false;
    private float timer = 0f;
    private float paid = 0f;

    static void log(final String message) {
        Global.getLogger(RorqualIntel.class).info(message);
    }

    public RorqualIntel(CampaignFleetAPI fleet) {
        this.fleet = fleet;
        this.flagship = fleet.getFlagship();
        Global.getSector().addScript(this);
    }

    @Override
    public void advance(float amount) {
        if (fleet.getFlagship()==null || flagship != fleet.getFlagship()) {
            gone = true;
        }

        if (gone && BountyLoot.getPlayerDefeated(RorqualSpawner.DEFEAT_ID)){
            payout = true;
            timer += amount;

            if (!doOnce && !Global.getSector().isPaused() && timer > 3f) {
                paid = Math.round(BountyLoot.getAmountPaid( RorqualSpawner.DEFEAT_ID_PAID));
                Global.getSector().getCampaignUI().addMessage("Donation received from an anonymous source, " + "+" + Misc.getDGSCredits(paid),
                        Global.getSettings().getColor("standardTextColor"),
                        "+" + Misc.getDGSCredits(paid),
                        "",
                        Global.getSettings().getColor("yellowTextColor"),
                        Global.getSettings().getColor("yellowTextColor"));
                //rep loss
                if (Global.getSector().getPlayerFaction().getRelationship(Factions.INDEPENDENT)>-0.5f) {
                    Global.getSector().getCampaignUI().addMessage("Relations with the Independents reduced by 10",
                            Global.getSettings().getColor("standardTextColor"),
                            "Independents",
                            "10",
                            Global.getSector().getFaction(Factions.INDEPENDENT).getColor(),
                            Misc.getNegativeHighlightColor());
                }
                //sound
                Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);
                if (isImportant()) {
                    endAfterDelay();
                } else endImmediately();
                doOnce = true;
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

        String locString = "";
        String locStringNoThe = "";
        if (fleet.isInHyperspace()){
            locString = "somewhere in hyperspace";
            locStringNoThe = locString;
        } else {
            locString = "the "+fleet.getStarSystem().getName();
            locStringNoThe = fleet.getStarSystem().getName();
        }

        bullet(info);
        if (!gone){
            info.addPara("Current rumoured location is "+locString+".", initPad, g, h, locStringNoThe, "");
            info.addPara(Misc.getDGSCredits(RorqualSpawner.BOUNTY_PAYOUT)+" reward", initPad, g, h, Misc.getDGSCredits(RorqualSpawner.BOUNTY_PAYOUT), "");
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

        paid = Math.round(BountyLoot.getAmountPaid( RorqualSpawner.DEFEAT_ID_PAID));
        String reducedPay = "";
        if (paid<(RorqualSpawner.BOUNTY_PAYOUT-1f)){
            reducedPay = " the amount was reduced due to low contribution.";
        }

        if (!gone){
            info.addPara("An infamous mercenary group patrolling the core systems.", opad);
            info.addPara("Shortly after, you were contact by an anonymous source telling you that there are many parties in the sector wishing to get rid of this fleet. Their payout is "+Misc.getDGSCredits(RorqualSpawner.BOUNTY_PAYOUT), opad,tc,h,Misc.getDGSCredits(RorqualSpawner.BOUNTY_PAYOUT)+"","");
        } if (payout && gone) {
            info.addPara("You were able to defeat the Peacekeepers, good riddance.", opad);
            info.addPara("You received the anonymous *donation* "+"+"+Misc.getDGSCredits(paid)+reducedPay, opad,tc,h,"+"+Misc.getDGSCredits(paid),"");
            addDeleteButton(info, width);
        } if (!payout && gone) {
            info.addPara("The Peacekeepers were defeated by another party. Seems like you missed your chance.", opad);
            addDeleteButton(info, width);
        }

        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", "pk");
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
        return "Peacekeepers";
    }

    public String getName() {
        return "The Peacekeepers";
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
        SectorEntityToken loc;
        if (fleet.isInHyperspace()){
            return null;
        } else {
            loc = fleet.getStarSystem().getHyperspaceAnchor();
        }
        return loc;
    }

    @Override
    public String getCommMessageSound() {
        return getSoundMajorPosting();
    }
}



