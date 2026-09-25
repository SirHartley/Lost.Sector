package lostsector.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// The one intel class of every quest; QuestIntels creates and changes it. Saved by the intel manager and the sector's
// script list, so it holds only saved-safe fields and reads its quest's state and text rows when displayed
// (README "Intel"). Renaming the class, a field or a Status constant breaks saves.
public final class QuestIntel extends BaseIntelPlugin {

    enum Status {
        ACTIVE,
        COMPLETED,
        FAILED
    }

    private static final float PARAGRAPH_PAD = 10f;

    private final String questId;
    private final String key;
    private final String icon;
    private final List<String> tags;
    private Status status = Status.ACTIVE;
    private SectorEntityToken mapLocation;

    // The intel list calls setTagsForSort on every listed entry right before sorting it by getSortString, so the
    // title is read once per sort instead of once per comparison.
    private transient String sortTitle;
    private transient boolean reportedMissingState;
    private transient boolean reportedMissingTitle;

    QuestIntel(String questId, String key, String icon, List<String> tags) {
        this.questId = questId;
        this.key = key;
        this.icon = icon;
        this.tags = new ArrayList<>(tags);
    }

    String questId() {
        return questId;
    }

    String key() {
        return key;
    }

    Status status() {
        return status;
    }

    void setMapLocation(SectorEntityToken entity) {
        mapLocation = entity;
    }

    // An update message; its rows see the update key in $nskr_intel_update. BaseIntelPlugin keeps the key in the
    // transient listInfoParam only while the message is built.
    void update(String updateKey, TextPanelAPI textPanel) {
        sendUpdateIfPlayerHasIntel(updateKey == null ? "" : updateKey, textPanel);
    }

    void finish(Status status, TextPanelAPI textPanel) {
        this.status = status;
        update("", textPanel);
        endAfterDelay();
    }

    // Text

    @Override
    protected String getName() {
        return title(QuestText.MODE_LIST);
    }

    @Override
    public String getSmallDescriptionTitle() {
        return title(QuestText.MODE_DESC);
    }

    @Override
    public void setTagsForSort(Set<String> tagsForSort) {
        super.setTagsForSort(tagsForSort);
        sortTitle = null;
    }

    // BaseIntelPlugin.getSortString sorts fleet log and exploration lists newest first and all others by name.
    @Override
    public String getSortString() {
        if (getTagsForSort().contains(Tags.INTEL_FLEET_LOG) || getTagsForSort().contains(Tags.INTEL_EXPLORATION)) {
            return super.getSortString();
        }
        if (sortTitle == null) sortTitle = getName();
        return sortTitle;
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        info.addPara(title(mode(mode)), getTitleColor(mode), 0f);
        addBulletPoints(info, mode);
    }

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
        float pad = initPad;
        for (QuestText.Line line : lines(QuestText.BULLETS, mode(mode))) {
            add(info, line, tc, pad);
            pad = 0f;
        }
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        for (QuestText.Line line : lines(QuestText.DESC, QuestText.MODE_DESC)) {
            add(info, line, Misc.getTextColor(), PARAGRAPH_PAD);
        }
    }

    // addPara with highlight arguments runs String.format on the text; this overload does not, so a '%' in a row
    // shows as written.
    private static void add(TooltipMakerAPI info, QuestText.Line line, Color color, float pad) {
        LabelAPI label = info.addPara(line.text, color, pad);
        if (line.highlights.length == 0) return;
        label.setHighlight(line.highlights);
        label.setHighlightColor(Misc.getHighlightColor());
    }

    private String title(String mode) {
        if (!hasState()) return key;
        String title = QuestText.title(QuestText.trigger(questId, QuestText.TITLE), memory(mode));
        if (title != null) return title;
        if (!reportedMissingTitle) {
            reportedMissingTitle = true;
            QuestManager.logError(questId, "intel " + key + ": no " + QuestText.trigger(questId, QuestText.TITLE) + " row matches");
        }
        return key;
    }

    private List<QuestText.Line> lines(String suffix, String mode) {
        if (!hasState()) return List.of();
        return QuestText.lines(QuestText.trigger(questId, suffix), memory(mode));
    }

    // Without a state, nskr_quest conditions would log an error for every row, so no rows are matched at all.
    private boolean hasState() {
        QuestManager manager = QuestManager.get();
        if (manager != null && manager.state(questId) != null) return true;
        if (!reportedMissingState) {
            reportedMissingState = true;
            QuestManager.logError(questId, "intel " + key + " shown without quest state; showing its key only");
        }
        return false;
    }

    private Map<String, MemoryAPI> memory(String mode) {
        Object update = getListInfoParam();
        return QuestText.intelMemory(key, status.name().toLowerCase(Locale.ROOT), update instanceof String ? (String) update : "", mode);
    }

    // MESSAGES covers the posting message and every update; IN_DESC is not used by the core UI.
    private static String mode(ListInfoMode mode) {
        switch (mode) {
            case MESSAGES:
                return QuestText.MODE_UPDATE;
            case MAP_TOOLTIP:
                return QuestText.MODE_TOOLTIP;
            case IN_DESC:
                return QuestText.MODE_DESC;
            default:
                return QuestText.MODE_LIST;
        }
    }

    // Presentation

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("campaignMissions", icon);
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> intelTags = super.getIntelTags(map);
        intelTags.addAll(tags);
        return intelTags;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return mapLocation;
    }
}
