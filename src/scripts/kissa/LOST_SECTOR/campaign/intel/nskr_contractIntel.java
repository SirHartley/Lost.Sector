package scripts.kissa.LOST_SECTOR.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.quests.jobs.contractInfo;
import scripts.kissa.LOST_SECTOR.campaign.quests.jobs.contractManager;
import scripts.kissa.LOST_SECTOR.util.ids;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class nskr_contractIntel extends BaseIntelPlugin {

    private boolean completed = false;
    private boolean failed = false;

    private PersonAPI person;
    private contractInfo contract;
    private MarketAPI market;

    static void log(final String message) {
        Global.getLogger(nskr_contractIntel.class).info(message);
    }

    //Initializer function
    public nskr_contractIntel(contractInfo contract, MarketAPI market, PersonAPI person) {
        Global.getSector().addScript(this);

        this.person = person;
        this.market = market;
        this.contract = contract;
    }

    @Override
    public void advance(float amount) {

        if (!completed && contract.completedCount>=contract.count){
            Color h = Misc.getHighlightColor();
            Color g = Misc.getGrayColor();
            Color tc = Misc.getTextColor();

            completed = true;
            float rep = (contract.totalReward/10000000f) + 0.02f;

            Global.getSector().getPlayerFleet().getCargo().getCredits().add(contract.totalReward);
            Global.getSector().getFaction(Factions.PLAYER).adjustRelationship("kesteven", rep);
            person.getRelToPlayer().adjustRelationship(rep/2f, RepLevel.COOPERATIVE);

            Global.getSector().getCampaignUI().addMessage(this);
            Global.getSector().getCampaignUI().addMessage("Contract complete",
                    tc, "", "", h, h);
            Global.getSector().getCampaignUI().addMessage("Received "+Misc.getDGSCredits(contract.totalReward),
                    g, Misc.getDGSCredits(contract.totalReward), "", h, h);
            Global.getSector().getCampaignUI().addMessage("Relations with Kesteven improved by "+ (int)(rep*100f),
                    g, "Kesteven",(int)(rep*100f)+"", Global.getSector().getFaction(ids.KESTEVEN_FACTION_ID).getColor(), h);

            Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);
            end();
        }

        if (!failed && contract.failed){
            failed = true;

            Color h = Misc.getHighlightColor();
            Color g = Misc.getGrayColor();
            Color tc = Misc.getTextColor();

            Global.getSector().getCampaignUI().addMessage(this);
            Global.getSector().getCampaignUI().addMessage("Contract failed",
                    tc, "", "", h, h);
            Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);
            end();
        }
    }

    private void end() {
        if (isImportant()) {
            endAfterDelay();
        } else endImmediately();

        //clean-up
        List<contractInfo> contracts = contractManager.getContracts(contractManager.CONTRACT_ARRAY_KEY);
        for (Iterator<contractInfo> iter = contracts.listIterator(); iter.hasNext();) {
            contractInfo a = iter.next();
            if (a==contract) iter.remove();
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

        bullet(info);

        int remaining = contract.count-contract.completedCount;

        if (!completed && !failed) {
            if (contract.type== contractInfo.contractType.ELIMINATE){
                String hostileStr = "";
                if (!contract.isFactionBounty) hostileStr = "enemy";

                info.addPara(remaining+" "+hostileStr+" "+contractManager.getTypeString(contract)+" remaining", opad,g, h, remaining+"", contractManager.getTypeString(contract));
                info.addPara(Misc.getDGSCredits(contract.totalReward)+" reward", initPad, g, h, Misc.getDGSCredits(contract.totalReward), "");
            } else {
                String units = contractManager.getUnitsString(contract);

                info.addPara(remaining+ units +contractManager.getTypeString(contract)+" left to recover", opad,g, h, remaining+"", contractManager.getTypeString(contract));
                info.addPara(Misc.getDGSCredits(contract.totalReward)+" reward", initPad, g, h, Misc.getDGSCredits(contract.totalReward), "");
            }
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
        if (!completed && !failed){
            if (contract.type== contractInfo.contractType.ELIMINATE){
                info.addPara("You accepted an elimination contract, for the destruction of certain assets belonging to the enemies of Kesteven.", opad,tc, h, "", "");
            } else {
                info.addPara("You accepted a recovery contract, for the salvaging of certain materials.", opad,tc, h, "", "");
            }
        } else if (!failed){
            info.addPara("You completed the contract.", opad,g, h, "", "");
            info.addPara("Received "+Misc.getDGSCredits(contract.totalReward), opad,g, h, Misc.getDGSCredits(contract.totalReward), "");
        } else {
            info.addPara("You failed the contract.", opad,g, h, "", "");
        }

        if (completed || failed) {
            addDeleteButton(info, width);
        }

        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    //Sets which icon the Intel screen should display. Can vary based on circumstances, but a single one often works just fine
    @Override
    public String getIcon() {
        if (contract.type== contractInfo.contractType.ELIMINATE) return Global.getSettings().getSpriteName("campaignMissions", "pk");
        if (contract.type== contractInfo.contractType.SCAVENGE) return Global.getSettings().getSpriteName("campaignMissions", "scav");
        return null;
    }

    //This sets which "tags" the even has in the Intel screen. For example, giving it the Tags.INTEL_STORY tag makes it appear in the "Story" sub-category
    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_ACCEPTED);
        tags.add(Tags.INTEL_MISSIONS);
        if (contract.type== contractInfo.contractType.ELIMINATE) tags.add(Tags.INTEL_BOUNTY);
        return tags;
    }

    //Sorting-related; see it as a form of "how important is the even" thingy. Lower number = more important
    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_3;
    }

    //What string to sort with, when sorting alphabetically
    public String getSortString() {
        String name = "";
        if (contract.type== contractInfo.contractType.ELIMINATE) name = "Elimination Contract";
        if (contract.type== contractInfo.contractType.SCAVENGE) name = "Recovery Contract";
        return name;
    }

    //The name of the event; can vary based on circumstances. I decided to just make it say "completed" when completed
    public String getName() {
        String name = "";
        if (contract.type== contractInfo.contractType.ELIMINATE) name = "Elimination Contract";
        if (contract.type== contractInfo.contractType.SCAVENGE) name = "Recovery Contract";
        return name;
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
        return market.getPrimaryEntity();
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



