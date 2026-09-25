//////////////////////
//Initially created by Histidine and modified from Nexelerin
//////////////////////
package lostsector.dialogue.rules;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.kesteven.quest.KestevenQuest;

import java.util.List;
import java.util.Map;

public class nskr_isAtMostKStage extends BaseCommandPlugin {
	
	@Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
	boolean isAtMost = false;
	String stringArg = params.get(0).getString(memoryMap);
	int stage = KestevenQuest.stage().toLegacy();
	int arg = Integer.parseInt(stringArg);

	if (stage<=arg) isAtMost = true;

	return isAtMost;
	}
}
