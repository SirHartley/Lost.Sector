package scripts.kissa.LOST_SECTOR.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.util.util;

public class nskr_comcrewsBonus implements EconomyTickListener, TooltipCreator {


	public static final float DISCOUNT = 0.333f;
	private int payment;

	static void log(final String message) {
		Global.getLogger(nskr_comcrewsBonus.class).info(message);
	}

	protected long startTime = 0;
	public nskr_comcrewsBonus() {
		Global.getSector().getListenerManager().addListener(this);
		startTime = Global.getSector().getClock().getTimestamp();
	}

	@Override
	public void reportEconomyMonthEnd()
	{

	}
	
	public void reportEconomyTick(int iterIndex) {
		int lastIterInMonth = (int) Global.getSettings().getFloat("economyIterPerMonth") - 1;
		if (iterIndex != lastIterInMonth) return;

		if (!nskr_modPlugin.IS_CC) return;
		if (!util.hasCCBonus()) return;

		payment = 0;
		MonthlyReport oldReport = SharedData.getData().getPreviousReport();
		payment += total(oldReport.getNode(MonthlyReport.FLEET));

		MonthlyReport report = SharedData.getData().getCurrentReport();
		FDNode fleetNode = report.getNode(MonthlyReport.FLEET);
		FDNode stipendNode = report.getNode(fleetNode, "nskr_comcrewsBonus");

		stipendNode.income = (int)(payment*DISCOUNT);
		stipendNode.name = "Kesteven connections";
		stipendNode.icon = Global.getSettings().getSpriteName("income_report", "surplus");
		stipendNode.tooltipCreator = this;
	}

	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
		String desc;

		desc = "Money saved for past expenses thanks to the commissioned crews bonus. Total expenses were " + Misc.getDGSCredits(payment) + ".";
		tooltip.addPara(desc, 0f);

	}

	public static int total(FDNode node){
		int totalIncome = (int)node.totalIncome + (int)node.income;
		int totalUpkeep = (int)node.totalUpkeep + (int)node.upkeep;
		log("ComCrews "+totalIncome+" "+totalUpkeep);
		return totalUpkeep;
	}

	public float getTooltipWidth(Object tooltipParam) {
		return 450;
	}

	public boolean isTooltipExpandable(Object tooltipParam) {
		return false;
	}

}



