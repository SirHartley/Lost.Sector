package scripts.kissa.LOST_SECTOR.campaign.quests;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
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
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class nskr_kQuest5ElizaBarFinal extends BaseBarEvent {

	//job5 eliza search bar dialog last time

	protected long seed;
	private int count = 0;
	private boolean paid = false;

	public boolean isAlwaysShow() {
		return true;
	}

	public nskr_kQuest5ElizaBarFinal() {
		seed = Misc.random.nextLong();
	}

	public boolean shouldShowAtMarket(MarketAPI market) {
		//used markets check
		if (nskr_kQuest5ElizaBarMain.getUsedMarkets(nskr_kQuest5ElizaBarMain.USED_MARKET_KEY).contains(market.getId())) return false;

		if (questUtil.getStage()<16) return false;
		paid = questUtil.getCompleted(nskr_kQuest5ElizaBarMain.PAID_FOR_INFO);
		if (paid && market!=nskr_kQuest5ElizaBarMain.getPaidForInfoTarget().getMarket())return false;

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

		paid = questUtil.getCompleted(nskr_kQuest5ElizaBarMain.PAID_FOR_INFO);

		String manOrWoman = "man";
		String himOrHerSelf = "himself";
		String himOrHer = "him";
		String hisOrHer = "his";
		String heOrShe = "he";
		if (gender == Gender.FEMALE){
			manOrWoman = "woman";
			himOrHerSelf = "herself";
			himOrHer = "her";
			hisOrHer = "her";
			heOrShe = "she";
		}

		count = nskr_kQuest5ElizaBarMain.getDialogStage(nskr_kQuest5ElizaBarMain.INTRO_DIALOG_KEY);

		TextPanelAPI text = dialog.getTextPanel();

		if (count == 2 && !paid) {
			text.addPara("A "+manOrWoman+" in a flashy uniform is signaling you to come over to "+hisOrHer+" table.");
			dialog.getOptionPanel().addOption("See if this spacer knows anything about Eliza", this);
		}
		if (count == 2 && paid) {
			text.addPara("A "+manOrWoman+" in a flashy uniform is signaling you to come over to "+hisOrHer+" table.");
			dialog.getOptionPanel().addOption("See if this spacer is the one you were told about", this);
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

		if (count == 2) {
			options.clearOptions();
			dialog.getVisualPanel().showPersonInfo(person, true);
			text.addPara("\"I hear you've been asking questions about Eliza captain. We should have a talk.\"");
			options.addOption("Continue", OptionId.A1);
		}
	}
	
	@Override
	public void optionSelected(String optionText, Object optionData) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color gr = Misc.getPositiveHighlightColor();
		Color r = Misc.getNegativeHighlightColor();
		Color tc = Misc.getTextColor();

		String manOrWoman = "man";
		String himOrHerSelf = "himself";
		String himOrHer = "him";
		String hisOrHer = "his";
		String heOrShe = "he";
		if (gender == Gender.FEMALE){
			manOrWoman = "woman";
			himOrHerSelf = "herself";
			himOrHer = "her";
			hisOrHer = "her";
			heOrShe = "she";
		}

		TextPanelAPI text = dialog.getTextPanel();
		dialog.getVisualPanel().showPersonInfo(person, true);

		//initial
		if (optionData== OptionId.A1){
			//set early
			if (count==2){
				List<String> markets = nskr_kQuest5ElizaBarMain.getUsedMarkets(nskr_kQuest5ElizaBarMain.USED_MARKET_KEY);
				markets.add(dialog.getInteractionTarget().getMarket().getId());
				nskr_kQuest5ElizaBarMain.setUsedMarkets(nskr_kQuest5ElizaBarMain.USED_MARKET_KEY, markets);
			}
			options.clearOptions();
			text.addPara("As you sit down the "+manOrWoman+" pours "+himOrHerSelf+" a drink of dark liquid from an expensive looking bottle.");
			text.addPara("\"You're in luck captain. Eliza herself wants to speak to you.\"");

			options.addOption("\"So, where is she?\"", OptionId.A2);
		}
		//a2
		if (optionData== OptionId.A2){
			//set Eliza loc
			questUtil.setElizaLoc();
			options.clearOptions();
			text.addPara("The "+manOrWoman+" grabs the ornate shot glass, looks at it intently and then chugs it before continuing. " +
					"\"You should head to "+ questUtil.getElizaLoc().getName()+" in order to speak to her.\"",tc,h, questUtil.getElizaLoc().getName(),"");
			text.addPara("\"That's all I will tell you captain. Now would you kindly leave me to my business.\" "+heOrShe+" begins pouring another glass, this business of "+hisOrHer+" does seem awfully important.");

			options.addOption("Leave", OptionId.LEAVE);
		}

		//leave
		if (optionData== OptionId.LEAVE){
			text.addPara("It's time to leave.");

			if (count==2){
				questUtil.setCompleted(true, questStageManager.JOB5_FOUND_ELIZA_KEY);
				nskr_kQuest5ElizaBarMain.setDialogStage(3, nskr_kQuest5ElizaBarMain.INTRO_DIALOG_KEY);
			}
			//remove old important
			if (paid){
				if (dialog.getInteractionTarget().getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_MISSION_IMPORTANT)){
					dialog.getInteractionTarget().getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
				}
			}
			//make new important
			questUtil.getElizaLoc().getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MISSION_IMPORTANT, true);

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
		LEAVE,
	}

	@Override
	public boolean isDialogFinished() {
		return done;
	}

	protected boolean showCargoCap() {
		return false;
	}
}