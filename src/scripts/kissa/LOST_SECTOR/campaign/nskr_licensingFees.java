package scripts.kissa.LOST_SECTOR.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.Map;

public class nskr_licensingFees implements EconomyTickListener, TooltipCreator {

	public static final float LICENSING_MULT = 0.1f;
	public static final String FEES_KEY = "$nskr_feePoints";
	public static final String TOTAL_KEY = "$nskr_totalLicenses";
	private int payment;

	//Manages license payments for exported kesteven equipment
	//Kinda jank because there is seemingly no way to get stuff produced ONLY THIS economy tick

	static void log(final String message) {
		Global.getLogger(nskr_licensingFees.class).info(message);
	}

	protected long startTime = 0;
	public nskr_licensingFees() {
		Global.getSector().getListenerManager().addListener(this);
		startTime = Global.getSector().getClock().getTimestamp();
	}

	//this works because reportEconomyMonthEnd runs before everyframe scripts, I guess?
	@Override
	public void reportEconomyMonthEnd()
	{
		//licensing fees logic
		int curr = nskr_kestevenExportManager.getTotalLicenseProduction();
		int saved = nskr_kestevenExportManager.getSavedProduction();
		log("licensingFees " + curr +", " + saved);

		if (curr< saved){
			setFees(saved-curr);
		}
		nskr_kestevenExportManager.saveProduction(curr);
	}
	
	public void reportEconomyTick(int iterIndex) {
		int lastIterInMonth = (int) Global.getSettings().getFloat("economyIterPerMonth") - 1;
		if (iterIndex != lastIterInMonth) return;

		if (!util.kestevenExists()) return;

		MonthlyReport report = SharedData.getData().getCurrentReport();
		FDNode fleetNode = report.getNode(MonthlyReport.FLEET);
		FDNode stipendNode = report.getNode(fleetNode, "nskr_licensingFees");

		payment = (int)(getFees()*LICENSING_MULT);
		if (Global.getSector().getPlayerFaction().getRelationship("kesteven")>-0.5f) {

			stipendNode.upkeep = payment;
			stipendNode.name = "Licensing fees";
			stipendNode.icon = Global.getSettings().getSpriteName("income_report", "fleet");
			stipendNode.tooltipCreator = this;
		}
		//reset
		setFees(0);
	}

	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
		String desc;
		int total = (int)(payment/LICENSING_MULT);
		if (Global.getSector().getPlayerFaction().getRelationship("kesteven")>-0.5f) {
			desc = "You received a licensing bill for " + Misc.getDGSCredits(total) + " worth of equipment.";
			tooltip.addPara(desc, 0f);
		}
	}

	public static void setFees(int amount){
		Map<String, Object> data = Global.getSector().getPersistentData();
		data.put(FEES_KEY, amount);
	}

	public static int getFees(){
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(FEES_KEY))
			data.put(FEES_KEY, 0);

		return (int)data.get(FEES_KEY);
	}

	public float getTooltipWidth(Object tooltipParam) {
		return 450;
	}

	public boolean isTooltipExpandable(Object tooltipParam) {
		return false;
	}

}



