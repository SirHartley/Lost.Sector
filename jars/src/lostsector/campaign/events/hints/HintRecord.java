package lostsector.campaign.events.hints;

import com.fs.starfarer.api.campaign.StarSystemAPI;

// One signal hint or bought tip, saved in HintsState.records under the record id of its intel entry.
final class HintRecord {

    String intel;
    StarSystemAPI system;
    // A tip's theme tag when it was bought (Tags.THEME_DERELICT or THEME_REMNANT); null for none and for signal hints.
    String theme;
    boolean visited;

    HintRecord(String intel, StarSystemAPI system, String theme) {
        this.intel = intel;
        this.system = system;
        this.theme = theme;
    }
}
