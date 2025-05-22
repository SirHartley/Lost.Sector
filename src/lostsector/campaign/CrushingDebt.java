package lostsector.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.rulecmd.Debt;
import lostsector.util.MathUtilLS;
import lostsector.util.MiscLS;


public class CrushingDebt implements EconomyTickListener, TooltipCreator {

	//Manages interest and missed payments for Debt

	static void log(final String message) {
		Global.getLogger(CrushingDebt.class).info(message);
	}

	protected long startTime = 0;
	public CrushingDebt() {
		Global.getSector().getListenerManager().addListener(this);
		startTime = Global.getSector().getClock().getTimestamp();
	}
	
	public void reportEconomyTick(int iterIndex) {
		int lastIterInMonth = (int) Global.getSettings().getFloat("economyIterPerMonth") - 1;
		if (iterIndex != lastIterInMonth) return;
		Debt.updateInterest();

		if (Debt.getDebt()==0) return;
		if (!MiscLS.kestevenExists()) return;

		MonthlyReport report = SharedData.getData().getCurrentReport();
		FDNode fleetNode = report.getNode(MonthlyReport.FLEET);
		FDNode stipendNode = report.getNode(fleetNode, "nskr_crushingDebt");

		int payment = 1;
		if (Global.getSector().getPlayerFaction().getRelationship("kesteven")>-0.5f) {
			payment = Math.round(Debt.getDebt() * (Debt.getInterest()/100f));

			stipendNode.upkeep = payment;
			stipendNode.name = "Monthly interest payment for your loan(s)";
			stipendNode.icon = Global.getSettings().getSpriteName("income_report", "generic_expense");
			stipendNode.tooltipCreator = this;
		} else {
			//extra debt for being hostile increases with rep loss
			float rel = (Global.getSector().getPlayerFaction().getRelationship("kesteven")*100f)+100f;
			rel = MathUtilLS.normalize(rel,0f,50f);
			float mult = MathUtilLS.lerp(2.25f,1.25f, rel);
			int withheld = Math.round(Debt.getDebt() * ((mult*Debt.getInterest())/100f));
			Debt.addDebt(withheld);
			log("Debt Hostile adding to debt instead, extra debt" + withheld + ", mult " + mult);

			stipendNode.upkeep = payment;
			stipendNode.name = "Monthly interest payment withheld";
			stipendNode.icon = Global.getSettings().getSpriteName("income_report", "overhead");
			stipendNode.tooltipCreator = this;
		}
	}

	public void reportEconomyMonthEnd() {
	}

	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
		String desc;
		if (Global.getSector().getPlayerFaction().getRelationship("kesteven")>-0.5f) {
			float rounded = Debt.getInterest();
			rounded *= 100f;
			rounded = Math.round(rounded);
			rounded /= 100f;
			desc = "Current monthly interest rate is " + rounded + "%";
			tooltip.addPara(desc, 0f);
			desc = "You have loaned " + Misc.getDGSCredits(Debt.getDebt());
			tooltip.addPara(desc, 0f);
		} else {
			desc = "Monthly interest payment added as debt due to hostilities. Current debt is " + Misc.getDGSCredits(Debt.getDebt());
			tooltip.addPara(desc, 0f);
		}
	}

	public float getTooltipWidth(Object tooltipParam) {
		return 450;
	}

	public boolean isTooltipExpandable(Object tooltipParam) {
		return false;
	}

}



