package lostsector.campaign.events.blacksite;

import lostsector.quest.QuestState;

import java.util.LinkedHashMap;
import java.util.Map;

// Saved by XStream in the quest store; renaming or removing a field breaks saves once 1.0.c ships.
public final class BlacksiteState extends QuestState<BlacksiteStage> {

    // Timer name prefix; the full name ends with the site's entity id.
    static final String TIMER_COUNTDOWN = "countdown:";

    static final String RANDOM_SITES = "sites";
    static final String RANDOM_FLEETS = "fleets";
    static final String RANDOM_LOOT = "loot";
    static final String RANDOM_DEBRIS = "debris";

    // Keyed by the site's original entity id, which the dialog target and the fleets' record id carry.
    Map<String, SiteRecord> sites = new LinkedHashMap<>();
}
