package scripts.kissa.LOST_SECTOR.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_abyssSpawner;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_rorqSpawner;

import java.awt.*;
import java.util.Set;

public class nskr_abyssIntel extends BaseIntelPlugin {

    private final CampaignFleetAPI fleet;
    private final FleetMemberAPI flagship;
    private boolean gone = false;
    private boolean payout = false;
    private boolean doOnce = false;
    private float timer = 0f;
    static void log(final String message) {
        Global.getLogger(nskr_abyssIntel.class).info(message);
    }

    //Initializer function
    public nskr_abyssIntel(CampaignFleetAPI fleet) {
        this.fleet = fleet;
        this.flagship = fleet.getFlagship();
        Global.getSector().addScript(this);

        //remove hint intel on discovery
        nskr_hintManager.removeHintIntel();
    }

    @Override
    public void advance(float amount) {
        if (!nskr_abyssSpawner.hasBountyShips(fleet)) {
            gone = true;
        }


        if (gone && !nskr_abyssSpawner.hasBountyShips(Global.getSector().getPlayerFleet())){
            payout = true;
            timer += amount;
            if (!doOnce && !Global.getSector().isPaused() && timer > 3f) {
                Global.getSector().getCampaignUI().addMessage("Bounty payment received from ARO, " + "+" + Misc.getDGSCredits(nskr_abyssSpawner.BOUNTY_PAYOUT),
                        Global.getSettings().getColor("standardTextColor"),
                        "+" + Misc.getDGSCredits(nskr_abyssSpawner.BOUNTY_PAYOUT),
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
        if (gone && nskr_abyssSpawner.hasBountyShips(Global.getSector().getPlayerFleet())){
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

    //The function for adding all bullet-points in the Intel tooltip.
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) initPad = opad;

        String systemString = nskr_abyssSpawner.getLoc().getContainingLocation().getName();

        bullet(info);
        if (!gone){
            info.addPara("Found patrolling in the "+systemString+".", initPad, g, h, systemString, "");
            info.addPara(Misc.getDGSCredits(nskr_abyssSpawner.BOUNTY_PAYOUT)+" reward", initPad, g, h, Misc.getDGSCredits(nskr_abyssSpawner.BOUNTY_PAYOUT), "");
        }

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
        if (!gone){
            info.addPara("You've found a mysterious seemingly passive Remnant fleet with unusual phase ships.", opad);
            info.addPara("Your comms team finds an open bounty posting by the Anti-Remnant Organization for a fleet with a matching yet vague description. The payout is "+Misc.getDGSCredits(nskr_abyssSpawner.BOUNTY_PAYOUT)
                    +" but requires the complete destruction of the fleet (no recovery).", opad,tc,h,Misc.getDGSCredits(nskr_abyssSpawner.BOUNTY_PAYOUT)+"","");
        } if (!payout && gone){
            info.addPara("You were able to defeat the Abyss fleet, good riddance.", opad);
            addDeleteButton(info, width);
        } if (payout && gone) {
            info.addPara("You were able to defeat the Abyss fleet, good riddance.", opad);
            info.addPara("You received the payment from ARO "+"+"+Misc.getDGSCredits(nskr_abyssSpawner.BOUNTY_PAYOUT), opad,tc,h,"+"+Misc.getDGSCredits(nskr_abyssSpawner.BOUNTY_PAYOUT),"");
            addDeleteButton(info, width);
        }

        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    //Sets which icon the Intel screen should display. Can vary based on circumstances, but a single one often works just fine
    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", "umbra");
    }

    //This sets which "tags" the even has in the Intel screen. For example, giving it the Tags.INTEL_STORY tag makes it appear in the "Story" sub-category
    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_BOUNTY);
        return tags;
    }

    //Sorting-related; see it as a form of "how important is the even" thingy. Lower number = more important
    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_2;
    }

    //What string to sort with, when sorting alphabetically
    public String getSortString() {
        return fleet.getName();
    }

    //The name of the event; can vary based on circumstances. I decided to just make it say "completed" when completed
    public String getName() {
        return fleet.getName();
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
        return nskr_abyssSpawner.getLoc();
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



