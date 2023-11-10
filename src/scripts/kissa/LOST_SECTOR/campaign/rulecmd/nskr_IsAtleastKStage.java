//////////////////////
//Initially created by Histidine and modified from Nexelerin
//////////////////////
package scripts.kissa.LOST_SECTOR.campaign.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;

import java.util.List;
import java.util.Map;

public class nskr_IsAtleastKStage extends BaseCommandPlugin {
	
	@Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
	boolean isAtleast = false;
	String stringArg = params.get(0).getString(memoryMap);
	int stage = questUtil.getStage();
	int arg = Integer.parseInt(stringArg);

	if (stage>=arg) isAtleast = true;

	return isAtleast;
	}
}
