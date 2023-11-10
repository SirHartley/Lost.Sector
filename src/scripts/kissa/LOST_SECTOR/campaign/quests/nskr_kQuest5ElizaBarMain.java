package scripts.kissa.LOST_SECTOR.campaign.quests;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.campaign.rulecmd.nskr_kestevenQuest;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class nskr_kQuest5ElizaBarMain extends BaseBarEvent {

	//job5 eliza search bar dialog first time

	public static final String INTRO_DIALOG_KEY = "nskr_kQuest5ElizaBarDialogStage";
	public static final String USED_MARKET_KEY = "nskr_kQuest5ElizaBarUsedMarket";
	public static final String PAID_FOR_INFO = "nskr_kQuest5ElizaBarPaidFor";
	public static final String PAID_FOR_INFO_LOC = "nskr_kQuest5ElizaBarPaidForLocation";

	private float money = 0f;
	protected long seed;
	private int count = 0;

	public boolean isAlwaysShow() {
		return true;
	}

	public nskr_kQuest5ElizaBarMain() {
		seed = Misc.random.nextLong();
	}

	public boolean shouldShowAtMarket(MarketAPI market) {
		if (getUsedMarkets(USED_MARKET_KEY).contains(market.getId())) return false;
		if (questUtil.getStage()<16) return false;
		return market.getFaction().getId().equals(Factions.PIRATES);
	}
	
	@Override
	public boolean shouldRemoveEvent() {
		return questUtil.getStage()>=17;
	}

	transient protected boolean done = false;
	transient protected Gender gender;
	transient protected PersonAPI person;
	
	@Override
	public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.addPromptAndOption(dialog, memoryMap);
		
		Random random = new Random(seed + dialog.getInteractionTarget().getMarket().getId().hashCode());
		
		gender = Gender.MALE;
		if (random.nextFloat() > 0.5f) {
			gender = Gender.FEMALE;
		}
		person = Global.getSector().getFaction(Factions.PIRATES).createRandomPerson(gender, random);
		person.setPostId(Ranks.POST_GENERIC_MILITARY);

		count = getDialogStage(INTRO_DIALOG_KEY);

		TextPanelAPI text = dialog.getTextPanel();

		if (count == 0) {
			text.addPara("A rough looking spacer is drinking alone at a corner table.");
			dialog.getOptionPanel().addOption("See if this spacer knows anything about Eliza", this);
		}
	}

	@Override
	public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.init(dialog, memoryMap);
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color gr = Misc.getPositiveHighlightColor();
		Color r = Misc.getNegativeHighlightColor();
		Color tc = Misc.getTextColor();

		if (count == 0) {
			options.clearOptions();
			money = mathUtil.getSeededRandomNumberInRange(4000, 7000, nskr_kestevenQuest.getRandom());
			dialog.getVisualPanel().showPersonInfo(person, true);
			text.addPara("The spacer is busy in thought while drinking and doesn't seem to notice you.");
			options.addOption("\"I'm looking for Eliza.\"", OptionId.A1);
		}
	}
	
	@Override
	public void optionSelected(String optionText, Object optionData) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color gr = Misc.getPositiveHighlightColor();
		Color r = Misc.getNegativeHighlightColor();
		Color tc = Misc.getTextColor();

		String himOrHerSelf = "himself";
		String himOrHer = "him";
		String hisOrHer = "his";
		String heOrShe = "he";
		String HeOrShe = "He";
		if (gender == Gender.FEMALE){
			himOrHerSelf = "herself";
			himOrHer = "her";
			hisOrHer = "her";
			heOrShe = "she";
			HeOrShe = "She";
		}

		TextPanelAPI text = dialog.getTextPanel();
		dialog.getVisualPanel().showPersonInfo(person, true);

		//initial
		if (optionData==OptionId.A1){
			options.clearOptions();
			text.addPara("No response, "+heOrShe+" doesn't seem interested in talking to you.");

			options.addOption("\"Hello? I'm talking to you.\"", OptionId.A2);
			if(Global.getSector().getPlayerFleet().getCargo().getCredits().get()>money) options.addOption("\"Would some credits change your mind?\"", OptionId.B1);
			options.addOption("Leave", OptionId.LEAVE);
		}
		//a2
		if (optionData==OptionId.A2){
			options.clearOptions();
			text.addPara("The spacer slowly turns to you, staring at you intensely.");
			text.addPara("\"Keep your nose out of our business captain.\" "+HeOrShe+" says in a gruesome tone, and returns to drinking like nothing happened.");

			options.addOption("Leave", OptionId.LEAVE);
		}
		//b1
		if (optionData==OptionId.B1){
			options.clearOptions();
			text.addPara("The spacer slowly turns to you. \"Everything is always about the damn money to you people, but if you insist...\"");
			text.addPara(HeOrShe+" adjusts "+hisOrHer+" position on "+hisOrHer+" chair, and quickly eyes up the room. \"For a small donation I could point you towards a certain person.\"");

			options.addOption("Pay "+himOrHer, OptionId.B2);
			options.addOption("Leave", OptionId.LEAVE);
		}
		//b2
		if (optionData==OptionId.B2){
			options.clearOptions();
			//add
			List<String> markets = getUsedMarkets(USED_MARKET_KEY);
			markets.add(dialog.getInteractionTarget().getMarket().getId());
			setUsedMarkets(USED_MARKET_KEY, markets);

			setPaidForInfoTarget();
			String loc = getPaidForInfoTarget().getMarket().getName();

			text.addPara("\"You will want to talk the contact at "+loc+". You will have no trouble finding them at the bar, trust me.\"",tc,h,loc,"");
			text.addPara("The spacer doesn't seem to want to say more, and is quick to distance "+himOrHerSelf+" from you.");

			questUtil.setCompleted(true, PAID_FOR_INFO);

			//make important
			getPaidForInfoTarget().getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MISSION_IMPORTANT, true);

			//remove
			Global.getSector().getPlayerFleet().getCargo().getCredits().add(-money);
			//acquire text
			text.setFontSmallInsignia();
			String creds = Misc.getDGSCredits(money);
			text.addPara("Lost " + creds, g, r, creds, "");
			text.addPara("Updated log entry for the Delve",g,h,"the Delve","");

			Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);
			text.setFontInsignia();

			options.addOption("Leave", OptionId.LEAVE2);
		}

		//leave
		if (optionData==OptionId.LEAVE || optionData==OptionId.LEAVE2){
			if (optionData==OptionId.LEAVE)text.addPara("You decide to leave the spacer alone.");
			if (optionData==OptionId.LEAVE2)text.addPara("It's time to move on.");

			if (optionData==OptionId.LEAVE && count==0){
				//add
				List<String> markets = getUsedMarkets(USED_MARKET_KEY);
				markets.add(dialog.getInteractionTarget().getMarket().getId());
				setUsedMarkets(USED_MARKET_KEY, markets);

				setDialogStage(1,INTRO_DIALOG_KEY);
			}

			if (optionData==OptionId.LEAVE2 && count==0){
				setDialogStage(2,INTRO_DIALOG_KEY);
			}

			dialog.getVisualPanel().fadeVisualOut();

			options.clearOptions();
			done = true;
			PortsideBarData.getInstance().removeEvent(this);
		}
	}

	public enum OptionId {
		A1,
		A2,
		A3,
		B1,
		B2,
		B3,
		LEAVE,
		LEAVE2,
	}

	@Override
	public boolean isDialogFinished() {
		return done;
	}

	protected boolean showCargoCap() {
		return false;
	}

	public static int getDialogStage(String id) {

		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(id)) data.put(id, 0);

		return (int)data.get(id);
	}

	public static void setDialogStage(int stage, String id) {

		Map<String, Object> data = Global.getSector().getPersistentData();
		data.put(id, stage);
	}

	public static List<String> setUsedMarkets(String id, List<String> marketId) {
		Map<String, Object> data = Global.getSector().getPersistentData();
		data.put(id, marketId);
		return (List<String>)data.get(id);
	}
	public static List<String> getUsedMarkets(String id) {
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(id)) data.put(id, new ArrayList<String>());
		return (List<String>)data.get(id);
	}

	public static void setPaidForInfoTarget(){
		Map<String, Object> data = Global.getSector().getPersistentData();
		String id = PAID_FOR_INFO_LOC;
		data.put(id, questUtil.pickElizaMarket(nskr_kestevenQuest.getRandom(), false));
	}

	public static SectorEntityToken getPaidForInfoTarget(){
		Map<String, Object> data = Global.getSector().getPersistentData();
		String id = PAID_FOR_INFO_LOC;

		return (SectorEntityToken) data.get(id);
	}
}