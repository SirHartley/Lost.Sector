package lostsector.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
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
    // The sound of the message being sent; both message paths read getCommMessageSound() while they build it.
    private transient String messageSound;
    // True while a posting or update message is built. The campaign message list builds it with ListInfoMode.MESSAGES,
    // a dialog's text panel (IntelManager.addIntelToTextPanel) with ListInfoMode.INTEL, so the mode alone cannot tell.
    private transient boolean inMessage;
    private transient boolean reportedMissingState;
    private transient boolean reportedMissingTitle;
    private transient boolean reportedMissingFaction;

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
    // transient listInfoParam only while the message is built. A null sound keeps the standard update sound.
    void update(String updateKey, String sound, TextPanelAPI textPanel) {
        messageSound = sound;
        inMessage = true;
        try {
            sendUpdateIfPlayerHasIntel(updateKey == null ? "" : updateKey, textPanel);
        } finally {
            messageSound = null;
            inMessage = false;
        }
    }

    // Adds the entry; the posting message goes to the text panel when there is one, else to the campaign messages.
    void post(TextPanelAPI textPanel) {
        inMessage = true;
        try {
            Global.getSector().getIntelManager().addIntel(this, false, textPanel);
        } finally {
            inMessage = false;
        }
    }

    void finish(Status status, String updateKey, String sound, TextPanelAPI textPanel) {
        this.status = status;
        IntelSpec spec = spec();
        if (spec != null && spec.isImportant()) setImportant(false);
        update(updateKey, sound, textPanel);
        endAfterDelay();
    }

    // The declaration's options, read when displayed so that definitions stay unsaved; null when the quest or key is
    // no longer declared.
    private IntelSpec spec() {
        QuestManager manager = QuestManager.get();
        Quest<?, ?> quest = manager == null ? null : manager.quest(questId);
        return quest == null ? null : quest.declarations().intelDeclaration(key);
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

    // The small insignia title font of the old Lost.Sector intel classes and of vanilla's large mission titles
    // (BaseHubMission.createIntelInfo with setUseLargeFontInMissionList).
    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        info.setParaSmallInsignia();
        info.addPara(title(mode(mode)), getTitleColor(mode), 0f);
        info.setParaFontDefault();
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

    // Paragraphs, then the bullets of an entry declared with descriptionBullets(), then the delete button of a
    // deletable entry that is completed or failed. BaseIntelPlugin.buttonPressConfirmed handles the button
    // (endImmediately, recreateIntelUI) after its confirmation prompt.
    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        for (QuestText.Line line : lines(QuestText.DESC, QuestText.MODE_DESC)) {
            add(info, line, Misc.getTextColor(), PARAGRAPH_PAD);
        }
        IntelSpec spec = spec();
        if (spec == null) return;
        if (spec.hasDescriptionBullets()) addBulletPoints(info, ListInfoMode.IN_DESC);
        if (spec.isDeletable() && status != Status.ACTIVE) addDeleteButton(info, width);
    }

    // addPara with highlight arguments runs String.format on the text; this overload does not, so a '%' in a row
    // shows as written.
    private static void add(TooltipMakerAPI info, QuestText.Line line, Color color, float pad) {
        LabelAPI label = info.addPara(line.text, color, pad);
        if (line.highlights.length == 0) return;
        label.setHighlight(line.highlights);
        label.setHighlightColors(line.colors);
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

    // MESSAGES covers a campaign message the UI builds again later; IN_DESC is the description's bullets.
    private String mode(ListInfoMode mode) {
        if (inMessage) return QuestText.MODE_UPDATE;
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
    public IntelSortTier getSortTier() {
        IntelSpec spec = spec();
        if (spec == null || spec.tierOrNull() == null || isEnding() || isEnded()) return super.getSortTier();
        return spec.tierOrNull();
    }

    @Override
    public String getCommMessageSound() {
        if (messageSound != null) return messageSound;
        IntelSpec spec = spec();
        if (!isSendingUpdate() && spec != null && spec.isMajorPosting()) return getSoundMajorPosting();
        return super.getCommMessageSound();
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        IntelSpec spec = spec();
        String factionId = spec == null ? null : spec.factionIdOrNull();
        FactionAPI faction = factionId == null ? null : Global.getSector().getFaction(factionId);
        if (factionId != null && faction == null && !reportedMissingFaction) {
            reportedMissingFaction = true;
            QuestManager.logError(questId, "intel " + key + ": unknown faction " + factionId + "; the player's colors are used");
        }
        return faction == null ? super.getFactionForUIColors() : faction;
    }

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
