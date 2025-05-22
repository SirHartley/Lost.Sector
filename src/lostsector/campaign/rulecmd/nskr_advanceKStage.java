//////////////////////
//Initially created by Histidine and modified from Nexelerin
//////////////////////
package lostsector.campaign.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.quests.util.QuestUtil;

import java.util.List;
import java.util.Map;

public class nskr_advanceKStage extends BaseCommandPlugin {
	
	@Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		String arg = params.get(0).getString(memoryMap);

		switch (arg)
		{
			case "init":
				break;
			case "advance":
				advance();
				break;
		}
		return true;
	}

	protected void advance(){
		int stage = QuestUtil.getStage();
		QuestUtil.setStage(stage+1);
	}
}
