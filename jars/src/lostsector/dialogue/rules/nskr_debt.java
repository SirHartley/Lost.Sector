//////////////////////
//Initially created by Histidine and modified from Nexelerin
//////////////////////
package lostsector.dialogue.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
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

import java.util.List;
import java.util.Map;
import java.util.Random;

public class nskr_debt extends BaseCommandPlugin {

	// Each id names one option of the nskr_debtLoans menu in rules.csv; positive credits take a loan, negative repay.
	// The key is the display String the option's text reads; it is written out in full so a search finds its writer.
	public enum Amount {
		LOAN_SMALL("loanSmall", "$nskr_debt_loanSmallStr"),
		LOAN_LARGE("loanLarge", "$nskr_debt_loanLargeStr"),
		LOAN_ALL("loanAll", "$nskr_debt_loanAllStr"),
		REPAY_SMALL("repaySmall", "$nskr_debt_repaySmallStr"),
		REPAY_LARGE("repayLarge", "$nskr_debt_repayLargeStr"),
		REPAY_ALL("repayAll", "$nskr_debt_repayAllStr");

		public final String id;
		public final String key;

		Amount(String id, String key) {
			this.id = id;
			this.key = key;
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
	public static final int BASE_DEBT = 8000;
	public static final int SMALL_AMOUNT = 10000;
	public static final int LARGE_AMOUNT = 100000;
	public static final float MIN_INTEREST = 2f;
	public static final float MAX_INTEREST = 6f;
	public static final float MAX_CHANGE = 0.25f;

	protected SectorEntityToken entity;
	protected TextPanelAPI text;
	protected CargoAPI playerCargo;
	protected MemoryAPI local;

	@Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap)
	{
		String arg = params.get(0).getString(memoryMap);
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
			case "available":
				return isAvailable(getAmount(params, memoryMap));
			case "isLoan":
				Amount loan = getAmount(params, memoryMap);
				return loan != null && loan.getCredits() > 0;
			case "preview":
				return updatePreviewTokens(getAmount(params, memoryMap));
			case "take":
				return take(getAmount(params, memoryMap));
		}
		return false;
	}

	protected static Amount getAmount(List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (params.size() < 2) return null;
		return Amount.fromId(params.get(1).getString(memoryMap));
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
		for (Amount amount : Amount.values()) {
			local.set(amount.key, Misc.getDGSCredits(Math.abs(amount.getCredits())), 0);
		}
	}

	protected boolean isAvailable(Amount amount)
	{
		if (amount == null) return false;
		int credits = amount.getCredits();
		int currDebt = getDebt();
		if (credits == 0) return false;
		if (credits > 0) return credits + currDebt <= getMaxDebt();
		return currDebt != 0 && -credits <= (int)playerCargo.getCredits().get() && currDebt + credits >= 0;
	}

	protected boolean updatePreviewTokens(Amount amount)
	{
		if (amount == null) return false;
		int credits = amount.getCredits();
		float cost = Math.round(credits * (getInterest() / 100f));
		local.set("$nskr_debt_amountStr", Misc.getDGSCredits(credits), 0);
		local.set("$nskr_debt_costStr", Misc.getDGSCredits(cost), 0);
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
