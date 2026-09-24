package lostsector.campaign.kesteven.contracts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.kesteven.contracts.ContractInfo;
import lostsector.campaign.kesteven.contracts.ContractManager;
import lostsector.helper.Ids;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ContractIntel extends BaseIntelPlugin {

    private boolean completed = false;
    private boolean failed = false;

    private PersonAPI person;
    private ContractInfo contract;
    private MarketAPI market;

    static void log(final String message) {
        Global.getLogger(ContractIntel.class).info(message);
    }

    public ContractIntel(ContractInfo contract, MarketAPI market, PersonAPI person) {
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
                    g, "Kesteven",(int)(rep*100f)+"", Global.getSector().getFaction(Ids.KESTEVEN_FACTION_ID).getColor(), h);

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
        List<ContractInfo> contracts = ContractManager.getContracts(ContractManager.CONTRACT_ARRAY_KEY);
        for (Iterator<ContractInfo> iter = contracts.listIterator(); iter.hasNext();) {
            ContractInfo a = iter.next();
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
            if (contract.type== ContractInfo.contractType.ELIMINATE){
                String hostileStr = "";
                if (!contract.isFactionBounty) hostileStr = "enemy";

                info.addPara(remaining+" "+hostileStr+" "+ ContractManager.getTypeString(contract)+" remaining", opad,g, h, remaining+"", ContractManager.getTypeString(contract));
                info.addPara(Misc.getDGSCredits(contract.totalReward)+" reward", initPad, g, h, Misc.getDGSCredits(contract.totalReward), "");
            } else {
                String units = ContractManager.getUnitsString(contract);

                info.addPara(remaining+ units + ContractManager.getTypeString(contract)+" left to recover", opad,g, h, remaining+"", ContractManager.getTypeString(contract));
                info.addPara(Misc.getDGSCredits(contract.totalReward)+" reward", initPad, g, h, Misc.getDGSCredits(contract.totalReward), "");
            }
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
        if (!completed && !failed){
            if (contract.type== ContractInfo.contractType.ELIMINATE){
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

    @Override
    public String getIcon() {
        if (contract.type== ContractInfo.contractType.ELIMINATE) return Global.getSettings().getSpriteName("campaignMissions", "pk");
        if (contract.type== ContractInfo.contractType.SCAVENGE) return Global.getSettings().getSpriteName("campaignMissions", "scav");
        return null;
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_ACCEPTED);
        tags.add(Tags.INTEL_MISSIONS);
        if (contract.type== ContractInfo.contractType.ELIMINATE) tags.add(Tags.INTEL_BOUNTY);
        return tags;
    }

    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_3;
    }

    public String getSortString() {
        String name = "";
        if (contract.type== ContractInfo.contractType.ELIMINATE) name = "Elimination Contract";
        if (contract.type== ContractInfo.contractType.SCAVENGE) name = "Recovery Contract";
        return name;
    }

    public String getName() {
        String name = "";
        if (contract.type== ContractInfo.contractType.ELIMINATE) name = "Elimination Contract";
        if (contract.type== ContractInfo.contractType.SCAVENGE) name = "Recovery Contract";
        return name;
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
        return market.getPrimaryEntity();
    }

    @Override
    public String getCommMessageSound() {
        return getSoundMajorPosting();
    }
}



