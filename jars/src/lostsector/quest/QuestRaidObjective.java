package lostsector.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.graid.AbstractGoalGroundRaidObjectivePluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;
import java.util.Random;

// A goal objective in a raid menu whose name, tooltip and result lines are rules rows (README "Raid objectives").
// MarketCMD keeps it only while the raid menu is open; it holds names, not the quest's objects.
final class QuestRaidObjective extends AbstractGoalGroundRaidObjectivePluginImpl {

    private static final float TOOLTIP_PAD = 10f;

    private final String questId;
    private final String key;
    private final String iconCommodityId;

    QuestRaidObjective(String questId, String key, MarketAPI market, RaidDangerLevel danger, String iconCommodityId) {
        super(market, danger);
        this.questId = questId;
        this.key = key;
        this.iconCommodityId = iconCommodityId;
    }

    // AbstractGoalGroundRaidObjectivePluginImpl sorts by getName().hashCode(), so a missing name row shows the key.
    @Override
    public String getName() {
        String name = QuestText.title(QuestText.trigger(questId, QuestText.RAID_NAME), QuestText.raidMemory(key));
        if (name != null) return name;
        QuestManager.logError(questId, "raid " + key + ": no " + QuestText.trigger(questId, QuestText.RAID_NAME) + " row matches");
        return key;
    }

    @Override
    public CargoStackAPI getStackForIcon() {
        if (iconCommodityId == null) return null;
        return Global.getFactory().createCargoStack(CargoAPI.CargoItemType.RESOURCES, iconCommodityId, null);
    }

    @Override
    public boolean hasTooltip() {
        return !tooltip().isEmpty();
    }

    // addPara(String, float) takes the tooltip's paragraph color and, unlike the overloads with highlight arguments,
    // does not run String.format.
    @Override
    public void createTooltip(TooltipMakerAPI t, boolean expanded) {
        for (QuestText.Line line : tooltip()) {
            LabelAPI label = t.addPara(line.text, TOOLTIP_PAD);
            highlight(label, line);
        }
    }

    // MarketCMD.performRaid calls this in the raid dialog, after the losses, reputation and stability lines and before
    // the XP line. The action runs first, so result rows can show values it stored; the lines print in the small font
    // in gray, as vanilla receipts. A goal objective has no projected value, so the raid gives no XP for it.
    @Override
    public int performRaid(CargoAPI loot, Random random, float lootMult, TextPanelAPI text) {
        if (marinesAssigned <= 0) return 0;
        InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        Map<String, MemoryAPI> memoryMap = dialog == null || dialog.getPlugin() == null ? null : dialog.getPlugin().getMemoryMap();
        QuestManager manager = QuestManager.get();
        if (manager == null || !manager.runRaidAction(questId, key, dialog, memoryMap)) return 0;
        List<QuestText.Line> lines = QuestText.lines(QuestText.trigger(questId, QuestText.RAID_RESULT), QuestText.raidMemory(key));
        if (lines.isEmpty()) return 0;
        text.setFontSmallInsignia();
        for (QuestText.Line line : lines) {
            highlight(text.addPara(line.text, Misc.getGrayColor()), line);
        }
        text.setFontInsignia();
        return 0;
    }

    private List<QuestText.Line> tooltip() {
        return QuestText.lines(QuestText.trigger(questId, QuestText.RAID_TOOLTIP), QuestText.raidMemory(key));
    }

    private static void highlight(LabelAPI label, QuestText.Line line) {
        if (line.highlights.length == 0) return;
        label.setHighlight(line.highlights);
        label.setHighlightColors(line.colors);
    }
}
