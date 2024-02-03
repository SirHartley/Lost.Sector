//////////////////////
//Initially created by Histidine and modified from Nexelerin
//////////////////////
package scripts.kissa.LOST_SECTOR.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;
import scripts.kissa.LOST_SECTOR.campaign.quests.nskr_EndingElizaDialog;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.util.util;
import scripts.kissa.LOST_SECTOR.util.nskr_stringHelper;

public class nskr_debt extends PaginatedOptions {

	//Hack job of nex code, but it just worksTM

	public static final String DEBT_KEY = "$nskr_debtPoints";
	public static final String INTEREST_KEY = "$nskr_debtInterest";
	public static final String PERSISTENT_RANDOM_KEY = "nskr_debtRandom";
	public static final String DIALOG_OPTION_PREFIX = "nskr_debt_pick_";
	public static final int BASE_DEBT = 8000;
	public static final float MIN_INTEREST = 2f;
	public static final float MAX_INTEREST = 6f;
	public static final float MAX_CHANGE = 0.25f;

	protected static loanInfo toLoan = null;
	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected MarketAPI market;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected CargoAPI playerCargo;
	protected PersonAPI person;
	protected FactionAPI faction;
	protected ShipAPI ship;
	protected int debt;
	protected int mDebt;
	protected float interest;
	protected List<String> disabledOpts = new ArrayList<>();

	static void log(final String message) {
		Global.getLogger(nskr_debt.class).info(message);
	}
	
	@Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) 
	{
		String arg = params.get(0).getString(memoryMap);
		setupVars(dialog, memoryMap);

		switch (arg)
		{
			case "init":
				break;
			case "hasOption":
				return validMarket(entity.getMarket());
			case "getLoans":
				setupDelegateDialog(dialog);
				addDebtOptions();
				showOptions();
				break;
			case "loan":
				int index = Integer.parseInt(memoryMap.get(MemKeys.LOCAL).getString("$option").substring(DIALOG_OPTION_PREFIX.length()));
				showDebtInfoAndPreparePurchase(index, dialog.getTextPanel());
				break;
			case "confirmLoan":
				loan();
				break;
		}
		return true;
	}
	
	/**
	 * To be called only when paginated dialog options are required. 
	 * Otherwise we get nested dialogs that take multiple clicks of the exit option to actually exit.
	 * @param dialog
	 */
	protected void setupDelegateDialog(InteractionDialogAPI dialog)
	{
		originalPlugin = dialog.getPlugin();  

		dialog.setPlugin(this);  
		init(dialog);
	}
	
	protected void setupVars(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap)
	{
		this.dialog = dialog;  
		this.memoryMap = memoryMap;
		
		entity = dialog.getInteractionTarget();
		market = entity.getMarket();
		text = dialog.getTextPanel();
		
		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();
		
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();
		
		person = dialog.getInteractionTarget().getActivePerson();
		faction = person.getFaction();
		
		updateDebtInMemory(getDebt(), getMaxDebt());
		if (getInterest()<MIN_INTEREST){
			initInterest();
		}
		updateInterestInMemory(getInterest());
	}

	protected void updateDebtInMemory(int newDebt, int maxDebt)
	{
		debt = newDebt;
		mDebt = maxDebt;
		memoryMap.get(MemKeys.LOCAL).set("$nskr_debt_points", debt, 0);
		memoryMap.get(MemKeys.LOCAL).set("$nskr_debt_pointsStr", Misc.getDGSCredits(debt)+"", 0);
		memoryMap.get(MemKeys.LOCAL).set("$nskr_debt_MaxpointsStr", Misc.getDGSCredits(mDebt)+"", 0);
	}

	protected void updateInterestInMemory(float newInterest)
	{
		interest = newInterest;
		float rounded = interest;
		rounded *= 100f;
		rounded = Math.round(rounded);
		rounded /= 100f;
		memoryMap.get(MemKeys.LOCAL).set("$nskr_debtInterest", interest, 0);
		memoryMap.get(MemKeys.LOCAL).set("$nskr_debtInterestStr", rounded + "%", 0);
	}
	
	@Override
	public void showOptions() {
		super.showOptions();
		for (String optId : disabledOpts)
		{
			dialog.getOptionPanel().setEnabled(optId, false);
		}
		dialog.getOptionPanel().setShortcut("nskr_debtMenuReturn", Keyboard.KEY_ESCAPE, false, false, false, false);
	}
	
	/**
	 * Adds the dialog options.
	 */
	protected void addDebtOptions()
	{
		dialog.getOptionPanel().clearOptions();

		List<loanInfo> loans = getLoans();

		int index = 0;
		for (loanInfo loan : loans)
		{
			addDebtOption(loan, index);
			index++;
		}

		addOptionAllPages("Back", "nskr_debtMenuReturn");
	}

	public static List<loanInfo> getLoans(){
		List<loanInfo> loans = new ArrayList<>();

		loanInfo add10k = new loanInfo(10000);
		loans.add(add10k);
		loanInfo add100k = new loanInfo(100000);
		loans.add(add100k);
		int all = getMaxDebt()-getDebt();
		all = Math.max(0,all);
		loanInfo addAll = new loanInfo(all);
		loans.add(addAll);
		loanInfo pay10k = new loanInfo(-10000);
		loans.add(pay10k);
		loanInfo pay100k = new loanInfo(-100000);
		loans.add(pay100k);
		int debt = getDebt();
		if (debt!=0) debt*=-1;
		loanInfo payAll = new loanInfo(debt);
		loans.add(payAll);

		return loans;
	}

	protected void addDebtOption(loanInfo info, int index){

		int maxDebt = getMaxDebt();
		int currDebt = getDebt();

		int amount = info.amount;

		String desc;
		if (amount>0) {
			desc = "Loan " + Misc.getDGSCredits(amount);
		} else {
			desc = "Repay " + Misc.getDGSCredits(-1*amount);
		}
		//text.setFontSmallInsignia();
		//text.addPara(desc);
		//text.setFontInsignia();

		String optId = DIALOG_OPTION_PREFIX + index;
		String str = desc;

		addOption(str, optId);
		if (amount > 0 && currDebt > maxDebt){
			log("Debt Loan unavailable, total over max: " + amount);
			disabledOpts.add(optId);
		}
		if (amount > 0 && amount+currDebt > maxDebt){
			log("Debt Loan unavailable, over max: " + amount);
			disabledOpts.add(optId);
		}
		if (amount == 0){
			log("Debt Loan would be 0: " + amount);
			disabledOpts.add(optId);
		}
		if (amount < 0 && (-1*amount) > (int)playerCargo.getCredits().get()){
			log("Debt Not enough credits: " + amount);
			disabledOpts.add(optId);
		}
		if (amount <= 0 && currDebt == 0){
			log("Debt No debt to repay: " + amount);
			disabledOpts.add(optId);
		}
		if (amount < 0 && currDebt+amount < 0){
			log("Debt Can't overpay: " + amount);
			disabledOpts.add(optId);
		}
	}

	protected void showDebtInfoAndPreparePurchase(int index, TextPanelAPI text) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();

		String desc;
		String hl;
		toLoan = getLoans().get(index);
		text.setFontInsignia();

		if(toLoan.amount>0) {
			float cost = Math.round(toLoan.amount * (getInterest() / 100f));
			desc = "A loan of " + Misc.getDGSCredits(toLoan.amount) + " would cost you " + Misc.getDGSCredits(cost) + " monthly, at the current interest rate.";
			hl = Misc.getDGSCredits(cost);
			text.addPara(desc, tc, h, hl, "");
		}

		text.setFontInsignia();
	}

	protected void loan()
	{

		int newDebt = addDebt(toLoan.amount);
		updateDebtInMemory(newDebt, getMaxDebt());
		if (toLoan.amount>0) {
			playerCargo.getCredits().add(toLoan.amount);
		} else playerCargo.getCredits().subtract(-1*toLoan.amount);

		text.setFontSmallInsignia();
		String str;
		String hl = "" + toLoan.amount;
		if (toLoan.amount > 0) {
			str = "Loan " + Misc.getDGSCredits(toLoan.amount);
		} else {
			str = "Repay " + Misc.getDGSCredits(-1*toLoan.amount);
		}
		text.addPara(str, Misc.getPositiveHighlightColor(), Misc.getHighlightColor(), hl);

		Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

		text.setFontInsignia();
	}


	public static int addDebt(int debt)
	{
		debt += getDebt();
		Global.getSector().getPersistentData().put(DEBT_KEY, debt);
		
		return debt;
	}
	
	public static int getDebt() {
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(DEBT_KEY))
			data.put(DEBT_KEY, 0);
		
		return (int)data.get(DEBT_KEY);
	}

	public static void initInterest() {
		float interest = MathUtils.getRandomNumberInRange(MIN_INTEREST+1f, MAX_INTEREST-1f);

		Global.getSector().getPersistentData().put(INTEREST_KEY, interest);
	}

	public static void updateInterest() {
		float interest = getInterest();
		float increase = MAX_CHANGE;
		float decrease = -MAX_CHANGE;

		if (interest+increase>MAX_INTEREST){
			increase = MAX_INTEREST-interest;
		}
		if (interest+decrease<MIN_INTEREST){
			decrease = MIN_INTEREST-interest;
		}

		interest += MathUtils.getRandomNumberInRange(decrease, increase);
		Global.getSector().getPersistentData().put(INTEREST_KEY, interest);
	}

	public static float getInterest(){
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(INTEREST_KEY))
			data.put(INTEREST_KEY, 0f);

		return (float)data.get(INTEREST_KEY);
	}


	public static int getMaxDebt(){

		return (int)((Global.getSector().getPlayerFaction().getRelationship("kesteven") * 100f) + 50f) * BASE_DEBT;
	}

	//all Kesteven markets
	public static boolean validMarket(MarketAPI market)
	{
		if (market==null) return false;
		if (Global.getSector().getPlayerFaction().getRelationship("kesteven")<=-0.5f) return false;
		if (questUtil.getCompleted(nskr_EndingElizaDialog.DIALOG_FINISHED_KEY)) return false;

		return market.getFaction().getId().equals("kesteven");
	}
	
	public static Random getRandom() {
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

			data.put(PERSISTENT_RANDOM_KEY, new Random(util.getSeedParsed()));
		}
		return (Random)data.get(PERSISTENT_RANDOM_KEY);
	}

	public static class loanInfo implements Comparable<loanInfo> {
		public int amount;

		public loanInfo(int amount)
		{
			this.amount = amount;
		}

		@Override
		public int compareTo(loanInfo other) {
			// descending cost order
			if (amount != other.amount) return Integer.compare(other.amount, amount);

			return amount;
		}
	}
}

