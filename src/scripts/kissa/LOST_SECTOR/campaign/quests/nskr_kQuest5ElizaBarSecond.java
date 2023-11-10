package scripts.kissa.LOST_SECTOR.campaign.quests;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class nskr_kQuest5ElizaBarSecond extends BaseBarEvent {

	//job5 eliza search bar dialog second time

	protected long seed;
	private int count = 0;

	public boolean isAlwaysShow() {
		return true;
	}

	public nskr_kQuest5ElizaBarSecond() {
		seed = Misc.random.nextLong();
	}

	public boolean shouldShowAtMarket(MarketAPI market) {
		//used markets check
		if (nskr_kQuest5ElizaBarMain.getUsedMarkets(nskr_kQuest5ElizaBarMain.USED_MARKET_KEY).contains(market.getId())) return false;

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

		count = nskr_kQuest5ElizaBarMain.getDialogStage(nskr_kQuest5ElizaBarMain.INTRO_DIALOG_KEY);

		TextPanelAPI text = dialog.getTextPanel();

		if (count == 1) {
			text.addPara("A sly looking spacer is drinking alone at the bar counter.");
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

		if (count == 1) {
			options.clearOptions();
			dialog.getVisualPanel().showPersonInfo(person, true);
			text.addPara("You approach the spacer and prepare to ask some questions.");
			options.addOption("\"Know anything about this \"Eliza\" character?\"", OptionId.A1);
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
		if (optionData== OptionId.A1){
			options.clearOptions();
			text.addPara("\"What, who sent you? You better not be another bloodhound from CommSec. And do be honest, I can spot a liar from orbit.\"");

			options.addOption("\"Kesteven\"", OptionId.A2);
			options.addOption("\"Does it matter if I'm paying you?\"", OptionId.A3);
			options.addOption("\"Eliza\"", OptionId.A4);
			options.addOption("\"COMSEC\"", OptionId.A5);
			options.addOption("\"I only work for myself.\"", OptionId.A6);
		}
		//a2
		if (optionData== OptionId.A2){
			options.clearOptions();
			text.addPara("The spacer looks surprised. \"Oh, wow would you look at captain conscience over here, being so honest and all. Yeah, I'm not telling you corporate fuckers anything.\"");
			text.addPara(HeOrShe+" quickly picks up "+hisOrHer+" drink and disappears into the crowd. The conversation is over.");

			options.addOption("Leave", OptionId.LEAVE);
		}
		if (optionData== OptionId.A3){
			options.clearOptions();
			text.addPara("\"You know some people can't be just bought. Not everything is solved by throwing more money at it, some people have morals.\" "+
					HeOrShe+" pauses for a moment as they lean forward, while pointing at you in a threatening manner. \"Not that you'd know anything about that.\"");
			text.addPara(HeOrShe+" quickly picks up "+hisOrHer+" drink and disappears into the crowd. The conversation is over.");

			options.addOption("Leave", OptionId.LEAVE);
		}
		//a4 a5
		if (optionData== OptionId.A4 || optionData== OptionId.A5){
			options.clearOptions();
			text.addPara("The spacer looks annoyed. \"Haha, very funny captain. It's pretty fucking obvious you don't work for them, I ain't going to tell you shit.\"");
			text.addPara(HeOrShe+" quickly picks up "+hisOrHer+" drink and disappears into the crowd. The conversation is over.");

			options.addOption("Leave", OptionId.LEAVE);
		}
		//a6
		if (optionData== OptionId.A6){
			options.clearOptions();
			text.addPara("The spacer looks puzzled. \"No one out here *only* works for themselves, everyone of note has connections and ties.\"");
			text.addPara(HeOrShe+" turns more serious for a moment. \"I might be able to put the word out for you, you might just find what you're looking for.\"");
			text.addPara(HeOrShe+" quickly picks up "+hisOrHer+" drink and disappears into the crowd. The conversation is over.");

			options.addOption("Leave", OptionId.LEAVE);
		}

		//leave
		if (optionData== OptionId.LEAVE){
			options.clearOptions();
			text.addPara("You decide it's best to leave.");

			dialog.getVisualPanel().fadeVisualOut();

			if (count==1){
				List<String> markets = nskr_kQuest5ElizaBarMain.getUsedMarkets(nskr_kQuest5ElizaBarMain.USED_MARKET_KEY);
				markets.add(dialog.getInteractionTarget().getMarket().getId());
				nskr_kQuest5ElizaBarMain.setUsedMarkets(nskr_kQuest5ElizaBarMain.USED_MARKET_KEY, markets);

				nskr_kQuest5ElizaBarMain.setDialogStage(2, nskr_kQuest5ElizaBarMain.INTRO_DIALOG_KEY);
			}

			done = true;
			PortsideBarData.getInstance().removeEvent(this);
		}
	}

	public enum OptionId {
		A1,
		A2,
		A3,
		A4,
		A5,
		A6,
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