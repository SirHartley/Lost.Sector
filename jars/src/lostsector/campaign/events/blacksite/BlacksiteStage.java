package lostsector.campaign.events.blacksite;

import lostsector.quest.QuestStage;

// A record quest: every site runs in the one stage; each SiteRecord has its own status.
// Saved by name in BlacksiteState; renaming or removing a constant breaks saves once 1.0.c ships.
public enum BlacksiteStage implements QuestStage {

    RUNNING;

    @Override
    public BlacksiteStage previous() {
        return null;
    }
}
