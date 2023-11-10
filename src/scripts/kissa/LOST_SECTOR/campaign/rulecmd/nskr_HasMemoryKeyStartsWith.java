//////////////////////
//Initially created by Histidine and modified from Nexelerin
//////////////////////
package scripts.kissa.LOST_SECTOR.campaign.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class nskr_HasMemoryKeyStartsWith extends BaseCommandPlugin
{
	@Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		String arg = params.get(0).getString(memoryMap);
		boolean startsWith = false;

		if (dialog.getInteractionTarget()==null || dialog.getInteractionTarget().getMemory()==null) return false;

		Collection<String>mem = dialog.getInteractionTarget().getMemory().getKeys();
		if (mem.isEmpty()) return false;
		for (String m : mem){
			if (m==null)continue;
			if (m.startsWith(arg)){
				startsWith = true;
				break;
			}
		}
		return startsWith;
	}
}
