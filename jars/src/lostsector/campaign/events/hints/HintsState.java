package lostsector.campaign.events.hints;

import com.fs.starfarer.api.campaign.StarSystemAPI;
import lostsector.quest.QuestState;

import java.util.LinkedHashMap;
import java.util.Map;

// Saved by XStream in the quest store; renaming or removing a field breaks saves once 1.0.c ships.
public final class HintsState extends QuestState<HintsStage> {

    static final String TIMER_OFFER = "tipOffer";
    static final String TIMER_COOLDOWN = "tipCooldown";

    // Signal sources no hint and no discovery has used yet, by source id, in the order the roll indexes them.
    Map<String, StarSystemAPI> sources = new LinkedHashMap<>();
    // Signal hints by source id, bought tips by "tip" and their number.
    Map<String, HintRecord> records = new LinkedHashMap<>();
    int tipsBought;

    // The Kesteven officer's offer; offerSystem is null between offers.
    StarSystemAPI offerSystem;
    int offerPrice;
    float offerDays;
    boolean offerBought;
    float cooldownDays;

    boolean frostShown;
    boolean frostGone;
}
