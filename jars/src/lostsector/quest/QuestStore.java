package lostsector.quest;

import java.util.LinkedHashMap;
import java.util.Map;

// The only saved object of the framework, stored through Saved under QuestManager.STORE_KEY.
final class QuestStore {

    Map<String, QuestState<?>> states = new LinkedHashMap<>();
    // Clock timestamp of the last day delivered to onDay; saved so reloads do not restart the day.
    long lastDay;

    QuestStore(long lastDay) {
        this.lastDay = lastDay;
    }
}
