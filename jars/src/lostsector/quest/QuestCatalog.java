package lostsector.quest;

import lostsector.campaign.kesteven.quest.KestevenQuest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Every quest in delivery order. QuestManager and the rules check tool both build definitions here.
public final class QuestCatalog {

    private QuestCatalog() {
    }

    public static List<Quest<?, ?>> create() {
        return build(List.of(new KestevenQuest()));
    }

    // Definition errors throw here, during load, so the first dev run finds them.
    private static List<Quest<?, ?>> build(List<Quest<?, ?>> quests) {
        Set<Class<?>> stageEnums = new HashSet<>();
        Set<Class<?>> flagEnums = new HashSet<>();
        for (Quest<?, ?> quest : quests) {
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
