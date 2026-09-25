package lostsector.quest;

import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.IntelSortTier;

import java.util.List;

// One declared intel entry: its icon and tags, and the presentation options set in declare() (README "Intel").
// A definition, never saved: QuestIntel copies the icon and tags when shown and reads the options when displayed.
public final class IntelSpec {

    final String questId;
    final String key;
    final String icon;
    final List<String> tags;

    private IntelSortTier tier;
    private boolean majorPosting;
    private boolean important;
    private boolean deletable;
    private String deletableCheck;
    private boolean descriptionBullets;
    private String factionId;
    private boolean frozen;

    IntelSpec(String questId, String key, String icon, List<String> tags) {
        this.questId = questId;
        this.key = key;
        this.icon = icon;
        this.tags = tags;
    }

    // Sort tier while the entry is active; an ending entry keeps vanilla's TIER_COMPLETED.
    public IntelSpec tier(IntelSortTier tier) {
        requireOpen();
        if (tier == null || tier == IntelSortTier.TIER_COMPLETED) {
            throw new IllegalArgumentException("[" + questId + "] intel " + key + " needs a tier other than null or TIER_COMPLETED");
        }
        this.tier = tier;
        return this;
    }

    // The posting message plays BaseIntelPlugin.getSoundMajorPosting() instead of the standard posting sound.
    public IntelSpec majorPosting() {
        requireOpen();
        majorPosting = true;
        return this;
    }

    // Marked important when shown and unmarked when completed or failed, as vanilla hub missions do on accept and end.
    public IntelSpec important() {
        requireOpen();
        important = true;
        return this;
    }

    // The description shows vanilla's delete button once the entry is completed, failed or closed.
    public IntelSpec deletable() {
        requireOpen();
        deletable = true;
        return this;
    }

    // The description shows vanilla's delete button while the named check of the quest passes, also while the entry
    // is active, for an entry the player keeps until deleting it; the check reads the entry's intel memory.
    public IntelSpec deletableWhen(String check) {
        requireOpen();
        if (check == null || check.isBlank()) {
            throw new IllegalArgumentException("[" + questId + "] intel " + key + " needs a check name");
        }
        deletableCheck = check;
        return this;
    }

    // The description panel shows the bullets, in mode desc, after its paragraphs, as the old Lost.Sector intel
    // classes and vanilla missions do (addBulletPoints with ListInfoMode.IN_DESC).
    public IntelSpec descriptionBullets() {
        requireOpen();
        descriptionBullets = true;
        return this;
    }

    // getFactionForUIColors: the faction whose UI colors the intel UI uses for this entry; the player's by default.
    public IntelSpec faction(String factionId) {
        requireOpen();
        if (factionId == null || factionId.isBlank()) {
            throw new IllegalArgumentException("[" + questId + "] intel " + key + " needs a faction id");
        }
        this.factionId = factionId;
        return this;
    }

    IntelSortTier tierOrNull() {
        return tier;
    }

    boolean isMajorPosting() {
        return majorPosting;
    }

    boolean isImportant() {
        return important;
    }

    boolean isDeletable() {
        return deletable;
    }

    String deletableCheckOrNull() {
        return deletableCheck;
    }

    boolean hasDescriptionBullets() {
        return descriptionBullets;
    }

    String factionIdOrNull() {
        return factionId;
    }

    void freeze() {
        frozen = true;
    }

    private void requireOpen() {
        if (frozen) {
            throw new IllegalStateException("[" + questId + "] intel " + key + " options are set only in QuestModule.declare");
        }
    }
}
