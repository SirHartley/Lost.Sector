package lostsector.campaign.events.blacksite;

import lostsector.quest.NoFlags;
import lostsector.quest.Quest;
import lostsector.quest.QuestModule;
import lostsector.quest.Quests;

import java.util.List;

// A record quest: one SiteRecord per station BlacksiteSpawner placed, all in the RUNNING stage.
public final class BlacksiteQuest extends Quest<BlacksiteStage, BlacksiteState> {

    public static final String ID = "bs";

    public BlacksiteQuest() {
        super(ID, BlacksiteStage.class, NoFlags.class, BlacksiteStage.RUNNING);
    }

    @Override
    protected BlacksiteState createState() {
        return new BlacksiteState();
    }

    @Override
    protected List<QuestModule<BlacksiteStage, BlacksiteState>> createModules() {
        return List.of(new BlacksiteModule());
    }

    // Null before QuestManager.startQuests() at the end of ModPlugin.onGameLoad.
    public static BlacksiteState state() {
        return Quests.state(ID);
    }
}
