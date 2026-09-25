//////////////////////
//Initially created by Histidine and modified from Nexelerin
//////////////////////
package lostsector.dialogue.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import lostsector.campaign.kesteven.quest.KestevenQuest;
import lostsector.helper.MathHelper;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Map;
import java.util.Random;

// TODO: replace this menu with a custom UI panel (docs/UI.md).
public class nskr_debt extends BaseCommandPlugin {

	// Positive credits take a loan, negative repay. The id is the suffix of the option id in the loan list.
	public enum Amount {
		LOAN_SMALL("loanSmall"),
		LOAN_LARGE("loanLarge"),
		LOAN_ALL("loanAll"),
		REPAY_SMALL("repaySmall"),
		REPAY_LARGE("repayLarge"),
		REPAY_ALL("repayAll");

		public final String id;

		Amount(String id) {
			this.id = id;
		}

		public boolean isRepayment() {
			return this == REPAY_SMALL || this == REPAY_LARGE || this == REPAY_ALL;
		}

		public int getCredits() {
			switch (this) {
				case LOAN_SMALL: return SMALL_AMOUNT;
				case LOAN_LARGE: return LARGE_AMOUNT;
				case LOAN_ALL: return Math.max(0, getMaxDebt() - getDebt());
				case REPAY_SMALL: return -SMALL_AMOUNT;
				case REPAY_LARGE: return -LARGE_AMOUNT;
				default: return -getDebt();
			}
		}

		public static Amount fromId(String id) {
			for (Amount amount : values()) {
				if (amount.id.equals(id)) return amount;
			}
			return null;
		}
	}

	public static final String DEBT_KEY = "$nskr_debtPoints";
	public static final String INTEREST_KEY = "$nskr_debtInterest";
	public static final String PERSISTENT_RANDOM_KEY = "nskr_debtRandom";
	public static final String DIALOG_OPTION_PREFIX = "nskr_debt_pick_";
	// The amount picked in the loan list, read when the transaction is confirmed.
	public static final String PICK_KEY = "$nskr_debt_pick";
	public static final String RETURN_OPTION = "nskr_debtMenuReturn";
	public static final int BASE_DEBT = 8000;
	public static final int SMALL_AMOUNT = 10000;
	public static final int LARGE_AMOUNT = 100000;
	public static final float MIN_INTEREST = 2f;
	public static final float MAX_INTEREST = 6f;
	public static final float MAX_CHANGE = 0.25f;

	protected InteractionDialogAPI dialog;
	protected SectorEntityToken entity;
	protected TextPanelAPI text;
	protected CargoAPI playerCargo;
	protected MemoryAPI local;

	@Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap)
	{
		String arg = params.get(0).getString(memoryMap);
		this.dialog = dialog;
		entity = dialog.getInteractionTarget();
		text = dialog.getTextPanel();
		playerCargo = Global.getSector().getPlayerFleet().getCargo();
		local = memoryMap.get(MemKeys.LOCAL);

		switch (arg)
		{
			case "init":
				if (getInterest()<MIN_INTEREST){
					initInterest();
				}
				updateTokens();
				return true;
			case "hasOption":
				return validMarket(entity.getMarket());
			case "getLoans":
				updateTokens();
				showLoans();
				return true;
			case "loan":
				updateTokens();
				return showLoanPreview(getPickedOption(memoryMap));
			case "confirmLoan":
				boolean taken = take(Amount.fromId(local.getString(PICK_KEY)));
				updateTokens();
				return taken;
		}
		return false;
	}

	protected static Amount getPickedOption(Map<String, MemoryAPI> memoryMap) {
		String option = memoryMap.get(MemKeys.LOCAL).getString("$option");
		if (option == null || !option.startsWith(DIALOG_OPTION_PREFIX)) return null;
		return Amount.fromId(option.substring(DIALOG_OPTION_PREFIX.length()));
	}

	protected void updateTokens()
	{
		int debt = getDebt();
		float interest = getInterest();
		float rounded = interest;
		rounded *= 100f;
		rounded = Math.round(rounded);
		rounded /= 100f;
		local.set("$nskr_debt_points", debt, 0);
		local.set("$nskr_debt_pointsStr", Misc.getDGSCredits(debt)+"", 0);
		local.set("$nskr_debt_MaxpointsStr", Misc.getDGSCredits(getMaxDebt())+"", 0);
		local.set("$nskr_debtInterest", interest, 0);
		local.set("$nskr_debtInterestStr", rounded + "%", 0);
	}

	// Called from a handler row that has no options of its own, so the list replaces the old menu here.
	protected void showLoans()
	{
		OptionPanelAPI options = dialog.getOptionPanel();
		options.clearOptions();
		for (Amount amount : Amount.values()) {
			String optionId = DIALOG_OPTION_PREFIX + amount.id;
			String label = (amount.isRepayment() ? "Repay " : "Loan ") + Misc.getDGSCredits(Math.abs(amount.getCredits()));
			options.addOption(label, optionId);
			if (!isAvailable(amount)) options.setEnabled(optionId, false);
		}
		options.addOption("Back", RETURN_OPTION);
		options.setShortcut(RETURN_OPTION, Keyboard.KEY_ESCAPE, false, false, false, false);
	}

	protected boolean isAvailable(Amount amount)
	{
		int credits = amount.getCredits();
		int currDebt = getDebt();
		if (credits == 0) return false;
		if (credits > 0) return credits + currDebt <= getMaxDebt();
		return currDebt != 0 && -credits <= (int)playerCargo.getCredits().get() && currDebt + credits >= 0;
	}

	protected boolean showLoanPreview(Amount amount)
	{
		if (amount == null) return false;
		local.set(PICK_KEY, amount.id, 0);
		int credits = amount.getCredits();
		if (credits <= 0) return true;
		float cost = Math.round(credits * (getInterest() / 100f));
		String costStr = Misc.getDGSCredits(cost);
		text.addParagraph("A loan of " + Misc.getDGSCredits(credits) + " would cost you " + costStr + " monthly, at the current interest rate.");
		text.highlightInLastPara(costStr);
		return true;
	}

	protected boolean take(Amount amount)
	{
		if (amount == null) return false;
		int credits = amount.getCredits();
		addDebt(credits);
		if (credits > 0) {
			playerCargo.getCredits().add(credits);
			AddRemoveCommodity.addCreditsGainText(credits, text);
		} else {
			playerCargo.getCredits().subtract(-credits);
			AddRemoveCommodity.addCreditsLossText(-credits, text);
		}
		Global.getSoundPlayer().playUISound("ui_rep_raise", 1f, 1f);
		return true;
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
		if (KestevenQuest.elizaEndingDone()) return false;

		return market.getFaction().getId().equals("kesteven");
	}

	public static Random getRandom() {
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

			data.put(PERSISTENT_RANDOM_KEY, new Random(MathHelper.getSeedParsed()));
		}
		return (Random)data.get(PERSISTENT_RANDOM_KEY);
	}
}
