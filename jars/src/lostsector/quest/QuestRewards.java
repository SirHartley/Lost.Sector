package lostsector.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.util.MutableValue;

// Grants with computed amounts. Each applies the grant the way the vanilla command does and prints the vanilla
// receipt only when the context has a dialog; outside one the quest reports the grant through its intel.
public final class QuestRewards {

    private final QuestContext<?, ?> ctx;

    QuestRewards(QuestContext<?, ?> ctx) {
        this.ctx = ctx;
    }

    public void credits(int amount) {
        if (!positive("credits", amount)) return;
        cargo().getCredits().add(amount);
        TextPanelAPI text = ctx.textPanel();
        if (text != null) AddRemoveCommodity.addCreditsGainText(amount, text);
        ctx.log("credits +" + amount);
    }

    // Credits never drop below zero, as with AddRemoveCommodity.
    public void takeCredits(int amount) {
        if (!positive("takeCredits", amount)) return;
        MutableValue credits = cargo().getCredits();
        credits.subtract(amount);
        if (credits.get() < 0) credits.set(0);
        TextPanelAPI text = ctx.textPanel();
        if (text != null) AddRemoveCommodity.addCreditsLossText(amount, text);
        ctx.log("credits -" + amount);
    }

    // A negative quantity removes.
    public void commodity(String commodityId, int quantity) {
        if (quantity == 0) return;
        TextPanelAPI text = ctx.textPanel();
        if (quantity > 0) {
            cargo().addCommodity(commodityId, quantity);
            if (text != null) AddRemoveCommodity.addCommodityGainText(commodityId, quantity, text);
        } else {
            cargo().removeCommodity(commodityId, -quantity);
            if (text != null) AddRemoveCommodity.addCommodityLossText(commodityId, -quantity, text);
        }
        AddRemoveCommodity.updatePlayerMemoryQuantity(commodityId);
        ctx.log("commodity " + commodityId + " " + quantity);
    }

    // A negative quantity removes.
    public void item(SpecialItemData item, int quantity) {
        if (item == null || quantity == 0) return;
        TextPanelAPI text = ctx.textPanel();
        if (quantity > 0) {
            cargo().addSpecial(item, quantity);
            if (text != null) AddRemoveCommodity.addItemGainText(item, quantity, text);
        } else {
            cargo().removeItems(CargoAPI.CargoItemType.SPECIAL, item, -quantity);
            if (text != null) AddRemoveCommodity.addItemLossText(item, -quantity, text);
        }
        ctx.log("item " + item.getId() + " " + quantity);
    }

    public void storyPoints(int points) {
        if (!positive("storyPoints", points)) return;
        TextPanelAPI text = ctx.textPanel();
        if (text != null) {
            Global.getSector().getPlayerStats().addStoryPoints(points, text, false);
        } else {
            Global.getSector().getPlayerStats().addStoryPoints(points);
        }
        ctx.log("story points +" + points);
    }

    private boolean positive(String grant, int amount) {
        if (amount > 0) return true;
        ctx.error(grant + " refused: amount " + amount + " is not positive");
        return false;
    }

    private static CargoAPI cargo() {
        return Global.getSector().getPlayerFleet().getCargo();
    }
}
