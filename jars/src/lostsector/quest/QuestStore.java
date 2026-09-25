package lostsector.quest;

import java.util.LinkedHashMap;
import java.util.Map;

// The only saved object of the framework, stored through Saved under QuestManager.STORE_KEY.
final class QuestStore {

    Map<String, QuestState<?>> states = new LinkedHashMap<>();
}
