package lostsector.campaign.bounties.abyss;

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
import lostsector.campaign.bounties.abyss.AbyssSpawner;

import java.awt.*;
import java.util.Set;

public class AbyssIntel extends BaseIntelPlugin {

    private final CampaignFleetAPI fleet;
    private final FleetMemberAPI flagship;
    private boolean gone = false;
    private boolean payout = false;
    private boolean doOnce = false;
    private float timer = 0f;
    static void log(final String message) {
        Global.getLogger(AbyssIntel.class).info(message);
    }

    public AbyssIntel(CampaignFleetAPI fleet) {
        this.fleet = fleet;
        this.flagship = fleet.getFlagship();
        Global.getSector().addScript(this);

        //remove hint intel on discovery
        HintManager.removeHintIntel();
    }

    @Override
    protected void advanceImpl(float amount) {
        if (!AbyssSpawner.hasBountyShips(fleet)) {
            gone = true;
        }


        if (gone && !AbyssSpawner.hasBountyShips(Global.getSector().getPlayerFleet())){
            payout = true;
            timer += amount;
            if (!doOnce && !Global.getSector().isPaused() && timer > 3f) {
                Global.getSector().getCampaignUI().addMessage("Bounty payment received from ARO, " + "+" + Misc.getDGSCredits(AbyssSpawner.BOUNTY_PAYOUT),
                        Global.getSettings().getColor("standardTextColor"),
                        "+" + Misc.getDGSCredits(AbyssSpawner.BOUNTY_PAYOUT),
                        "",
                        Global.getSettings().getColor("yellowTextColor"),
                        Global.getSettings().getColor("yellowTextColor"));
                doOnce = true;
                //sound
                Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);
                if (isImportant()) {
                    endAfterDelay();
                } else endImmediately();
            }
        }
        if (gone && AbyssSpawner.hasBountyShips(Global.getSector().getPlayerFleet())){
            timer += amount;
            if (!doOnce && !Global.getSector().isPaused() && timer > 3f) {
                Global.getSector().getCampaignUI().addMessage("Since you have no proof of complete destruction, you will not receive any payments from ARO.",
                        Global.getSettings().getColor("standardTextColor"),
                        "no proof of complete destruction",
                        "",
                        Global.getSettings().getColor("yellowTextColor"),
                        Global.getSettings().getColor("yellowTextColor"));
                doOnce = true;
                //sound
                Global.getSoundPlayer().playUISound("ui_rep_drop",1f,1f);
                if (isImportant()) {
                    endAfterDelay();
                } else endImmediately();
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

        String systemString = AbyssSpawner.getLoc().getContainingLocation().getName();

        bullet(info);
        if (!gone){
            info.addPara("Found patrolling in the "+systemString+".", initPad, g, h, systemString, "");
            info.addPara(Misc.getDGSCredits(AbyssSpawner.BOUNTY_PAYOUT)+" reward", initPad, g, h, Misc.getDGSCredits(AbyssSpawner.BOUNTY_PAYOUT), "");
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
            info.addPara("You've found a mysterious seemingly passive Remnant fleet with unusual phase ships.", opad);
            info.addPara("Your comms team finds an open bounty posting by the Anti-Remnant Organization for a fleet with a matching yet vague description. The payout is "+Misc.getDGSCredits(AbyssSpawner.BOUNTY_PAYOUT)
                    +" but requires the complete destruction of the fleet (no recovery).", opad,tc,h,Misc.getDGSCredits(AbyssSpawner.BOUNTY_PAYOUT)+"","");
        } if (!payout && gone){
            info.addPara("You were able to defeat the Abyss fleet, good riddance.", opad);
            addDeleteButton(info, width);
        } if (payout && gone) {
            info.addPara("You were able to defeat the Abyss fleet, good riddance.", opad);
            info.addPara("You received the payment from ARO "+"+"+Misc.getDGSCredits(AbyssSpawner.BOUNTY_PAYOUT), opad,tc,h,"+"+Misc.getDGSCredits(AbyssSpawner.BOUNTY_PAYOUT),"");
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
        return fleet.getName();
    }

    public String getName() {
        return fleet.getName();
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
        return AbyssSpawner.getLoc();
    }

    @Override
    public String getCommMessageSound() {
        return getSoundMajorPosting();
    }
}



