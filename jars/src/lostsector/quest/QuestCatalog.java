package lostsector.quest;

import lostsector.campaign.events.intercepts.InterceptsQuest;
import lostsector.campaign.kesteven.quest.KestevenQuest;
import lostsector.campaign.starts.hellspawn.HellSpawnQuest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Every quest in delivery order. QuestManager and the rules check tool both build definitions here.
public final class QuestCatalog {

    // Verbs of nskr_quest that take no quest id, so no quest may use these ids.
    public static final Set<String> QUEST_INDEPENDENT_VERBS = Set.of("confirm", "engage");

    private QuestCatalog() {
    }

    public static List<Quest<?, ?>> create() {
        return build(List.of(new KestevenQuest(), new InterceptsQuest(), new HellSpawnQuest()));
    }

    // Definition errors throw here, during load, so the first dev run finds them.
    private static List<Quest<?, ?>> build(List<Quest<?, ?>> quests) {
        Set<Class<?>> stageEnums = new HashSet<>();
        Set<Class<?>> flagEnums = new HashSet<>();
        for (Quest<?, ?> quest : quests) {
            if (QUEST_INDEPENDENT_VERBS.contains(quest.id())) {
                throw new IllegalStateException("Quest id " + quest.id() + " is reserved for a verb of nskr_quest");
            }
            for (Quest<?, ?> other : quests) {
                if (quest != other && other.id().startsWith(quest.id())) {
                    throw new IllegalStateException("Quest id " + quest.id() + " is equal to or a prefix of " + other.id());
                }
            }
            if (!stageEnums.add(quest.stages())) {
                throw new IllegalStateException("[" + quest.id() + "] shares its stage enum " + quest.stages().getName());
            }
            if (quest.flags() != NoFlags.class && !flagEnums.add(quest.flags())) {
                throw new IllegalStateException("[" + quest.id() + "] shares its flag enum " + quest.flags().getName());
            }
            quest.build();
        }
        return quests;
    }
}
