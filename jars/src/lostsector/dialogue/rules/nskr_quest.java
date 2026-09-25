package lostsector.dialogue.rules;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import lostsector.quest.QuestVerbs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// nskr_quest <q> <verb> [arguments]: the quest framework's rules command; QuestVerbs runs the verbs.
public class nskr_quest extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        List<String> words = new ArrayList<>(params.size());
        for (Misc.Token token : params) {
            words.add(memoryMap == null ? token.string : token.getString(memoryMap));
        }
        return QuestVerbs.execute(ruleId, dialog, words, memoryMap);
    }

    @Override
    public boolean doesCommandAddOptions() {
        return false;
    }
}
