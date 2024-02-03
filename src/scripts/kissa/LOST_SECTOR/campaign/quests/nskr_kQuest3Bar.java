package scripts.kissa.LOST_SECTOR.campaign.quests;

import java.awt.*;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

public class nskr_kQuest3Bar extends BaseBarEvent {

	//job3 bar dialog

	protected long seed;
	private final SectorEntityToken target;
	private final SectorEntityToken home;
	private boolean chatting = false;
	private boolean travelling = false;
	private boolean group1 = false;
	private boolean group2 = false;
	private boolean group3 = false;
	private boolean group1Chatted = false;
	private boolean group2Chatted = false;
	private boolean group3Chatted = false;
	private int alcoholCount = 0;
	private PersonAPI group1person1 = null;
	private PersonAPI group1person2 = null;
	private PersonAPI group2person1 = null;
	private PersonAPI group3person1 = null;
	private PersonAPI group3person2 = null;
	private PersonAPI group3person3 = null;
	private Random random;

	public boolean isAlwaysShow() {
		return true;
	}
	
	public nskr_kQuest3Bar() {
		home = questUtil.getJob3Start();
		target = questUtil.getJob3Target();
		seed = Misc.random.nextLong();
	}

	public boolean shouldShowAtMarket(MarketAPI market) {
		if (questUtil.getStage()<=7) return false;
		return market==home.getMarket();
	}
	
	@Override
	public boolean shouldRemoveEvent() {
		return questUtil.getStage()>=10;
	}

	transient protected boolean done = false;
	transient protected Gender gender;
	transient protected PersonAPI person;
	
	@Override
	public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.addPromptAndOption(dialog, memoryMap);
		
		random = new Random(seed + dialog.getInteractionTarget().getMarket().getId().hashCode());
		
		gender = Gender.MALE;
		if (random.nextFloat() > 0.5f) {
			gender = Gender.FEMALE;
		}
		person = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson(gender, random);
		person.setPostId(Ranks.POST_GENERIC_MILITARY);

		TextPanelAPI text = dialog.getTextPanel();
		text.addPara("A lone employee in a Tri-Tachyon suit is drinking at one of the tables.");
		
		dialog.getOptionPanel().addOption(
				"Approach the employee, and see what you can find out", this);
	}

	@Override
	public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.init(dialog, memoryMap);
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color gr = Misc.getPositiveHighlightColor();
		Color r = Misc.getNegativeHighlightColor();
		Color tc = Misc.getTextColor();

		options.clearOptions();
		dialog.getVisualPanel().showPersonInfo(person, true);
		text.addPara("The employee seems to be busy in thought. There are multiple empty glasses on the table already.");
		options.addOption("Offer to buy the next round of drinks",this);

	}
	
	@Override
	public void optionSelected(String optionText, Object optionData) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color gr = Misc.getPositiveHighlightColor();
		Color r = Misc.getNegativeHighlightColor();
		Color tc = Misc.getTextColor();
		//gen people
		if (group3person1==null && group3person2==null && group3person3==null) {
			group3person1 = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson(Gender.MALE, random);
			group3person1.setPostId(Ranks.POST_PATROL_COMMANDER);
			group3person2 = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson(Gender.ANY, random);
			group3person2.setPostId(Ranks.POST_FLEET_COMMANDER);
			group3person3 = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson(Gender.ANY, random);
			group3person3.setPostId(Ranks.POST_AGENT);
		}
		if (group2person1==null) {
			group2person1 = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson(Gender.FEMALE, random);
			group2person1.setPostId(Ranks.POST_BASE_COMMANDER);
		}
		if (group1person1==null && group1person2==null) {
			group1person1 = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson(Gender.MALE, random);
			group1person1.setPostId("kTechEngineer");
			group1person2 = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson(Gender.MALE, random);
			group1person2.setPostId(Ranks.POST_ENTREPRENEUR);
		}

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
		VisualPanelAPI visual = dialog.getVisualPanel();

		//initial
		if (optionData==this){
			chatting = true;
			options.clearOptions();
			text.addPara("You can see "+hisOrHer+" mood immediately improve on the prospect of free drinks.");
			text.addPara("\"Thanks captain, it's been a particularly tedious work week for me.\" "+HeOrShe+" accepts your offer.");
			text.addPara("You have some inconsequential smalltalk with the employee, and then proceed to order the drinks.");

			options.addOption("Continue",OptionId.A1);
		}
		//intro pt2
		if (optionData==OptionId.A1){
			options.clearOptions();
			text.addPara("The drinks arrive shortly and the employee starts drinking at an impressive pace. You try to keep up with "+himOrHer+", but it is rather difficult.");
			text.addPara("\"I'm feeling another round captain, you in?\" "+HeOrShe+" asks.");

			options.addOption("Agree",OptionId.A2);
			options.addOption("Disagree",OptionId.A3);
		}
		//disagree
		if (optionData==OptionId.A3){
			options.clearOptions();
			text.addPara("\"Oh come one captain, just one more drink. I know you can handle more liqueur than that.\" "+HeOrShe+" tries to point out.");
			text.addPara( HeOrShe+" does make a good point, when has one more drink ever hurt anybody?",g,h,"","");

			options.addOption("Agree",OptionId.A2);
			options.addOption("Leave",OptionId.LEAVE_A);
		}
		//agree
		if (optionData==OptionId.A2){
			options.clearOptions();
			text.addPara("\"That's the spirit captain.\" "+HeOrShe+" seems eager to get drinking.");
			text.addPara("Another drink, and another set of smalltalk later, you finally hear something interesting.");
			text.addPara("\"Someone from the higher management is having a party today, and you seem like the fun type. I could get you in if you're interested in joining me.\" "+HeOrShe+" is waiting for your response.");

			options.addOption("Accept the offer",OptionId.A4);
			options.addOption("Leave",OptionId.LEAVE_A);
		}
		//agree again
		if (optionData==OptionId.A4){
			options.clearOptions();
			text.addPara("\"Ah yes, but first let us get *another* drink.\" "+HeOrShe+" says cheerfully.");
			text.addPara("During this round of drinks you realize that you feel a little dizzy, better slow down. You're supposed to be on duty.",g,h,"","");
			text.addPara("You drink up some more, and then get ready to move on with the employee.");

			options.addOption("Continue",OptionId.A5);
		}
		//travel
		if (optionData==OptionId.A5){
			chatting = false;
			travelling = true;
			options.clearOptions();
			text.addPara("You make your way around the local transport system. You suspect that you might have gotten lost multiple times, as you try to follow "+himOrHer+" around. The alcohol is not helping.");
			text.addPara("Finally you arrive in what looks like the right spot. There is a bouncer at the front door, they are busy handling what you presume are other guests.");
			text.addPara("Somehow your bodyguards managed to not only keep up with your bumbling, but also blended into the local crowd. but it seems like they'll have to wait outside for this one.");
			text.addPara("The employee has a short chat with the bouncer, he flashes some papers - and somewhat surprisingly you are let in. First you do have to hand over your sidearm, of course.");

			options.addOption("Continue",OptionId.A6);
		}
		//arrived
		if (optionData==OptionId.A6){
			chatting = true;
			travelling = false;
			options.clearOptions();
			text.addPara("The establishment is filled with people, a large and ornate chandelier lights up the people in expensive suits, with an intricate dance of colors and shadows. The noise of chatter fills the room.");
			text.addPara("\"I hope you aren't disappointed my friend. Now let's grab something to drink.\" The employee says with a slick smile.");
			text.addPara("You weave through the crowd of people, then you see vast array of different drinks from all over the sector, ready to be served. \"So, what will you be having captain?\" "+HeOrShe+" is motioning you to grab one of the drinks.");

			options.addOption("\"Sweet Freedom\"",OptionId.D1);
			options.addOption("\"Kings Favour\"",OptionId.D2);
			options.addOption("\"Absynth\"",OptionId.D3);
			options.addOption("\"Askonia Sunshine\"",OptionId.D4);
			options.addOption("\"Tears of Ludd\"",OptionId.D5);
			options.addOption("\"Nothing\"",OptionId.A7);
		}
		//arrived take a drink (again)
		if (optionData==OptionId.A7||optionData==OptionId.D1||optionData==OptionId.D2||optionData==OptionId.D3||optionData==OptionId.D4||optionData==OptionId.D5){
			options.clearOptions();
			if(optionData!=OptionId.A7){
				text.addPara(HeOrShe+" grabs a drink too. \"Bottoms up captain.\" "+HeOrShe+" chants and starts chugging.");
			}
			if(optionData==OptionId.D1){
				alcoholCount++;
				text.addPara("Quite sweet and with a bit of complexity, yet the taste is lacking something special. This rum is a decent enough drink.");
			}
			if(optionData==OptionId.D2){
				alcoholCount++;
				text.addPara("An excessively sweet taste attacks your mouth, although the burn is not too bad, you are not a fan of this liqueur.");
			}
			if(optionData==OptionId.D3){
				alcoholCount += 2;
				text.addPara("The sickly green liquid shines like a glowing toxin. You try to brace yourself, but it's not enough, you can only taste the burn of this horrible concoction. You feel sick for a significant period after.");
			}
			if(optionData==OptionId.D4){
				alcoholCount++;
				text.addPara("It's not too sweet or bitter, yet the wine has peculiar aftertaste, this blue wine is alright.");
			}
			if(optionData==OptionId.D5){
				alcoholCount++;
				text.addPara("A complex yet pleasant taste of herbs fills your mouth, you can visualise the green forests of Gilead from the aroma, this herb infused spirit is not too bad.");
			}
			if(optionData==OptionId.A7){
				text.addPara("\"Don't be such a downer captain... just relax for a bit, maybe.\" "+HeOrShe+" blurts out.");
			}
			text.addPara("The employee is starting to get visibly drunk at this point, and "+heOrShe+" goes to chat up some colleagues. This is your chance to snoop around. You are here for work, remember?");

			options.addOption("Look for anything of interest",OptionId.MAIN);
		}
		//arrived pt3
		if (optionData==OptionId.MAIN){
			visual.fadeVisualOut();
			group1 = false;
			group2 = false;
			group3 = false;
			chatting = false;
			options.clearOptions();
			if(!group1Chatted || !group2Chatted || !group3Chatted){
				text.addPara("As you begin to look around, you spot three distinct groups of people that could be of interest.");
			} else {
				text.addPara("Seems like you have seen everything there is to see, it's time to move on.");
			}
			if(!group1Chatted)text.addPara("The first is a group of two men chatting, they have a certain techie vibe to them. Certainly there is a chance they know something of use.");
			if(!group2Chatted)text.addPara("The second is a larger group centered around what appears to be someone from this - higher management. Eavesdropping on them could yield some results.");
			if(!group3Chatted)text.addPara("The third is a collection that appears to be three officers in uniforms, one of them could even be an admiral. A promising lead for sure.");

			if(!group1Chatted)options.addOption("Approach the first group",OptionId.G1);
			if(!group2Chatted)options.addOption("Approach the second group",OptionId.G2);
			if(!group3Chatted)options.addOption("Approach the third group",OptionId.G3);
			if(group3Chatted)options.addOption("Leave",OptionId.MAIN2);
		}
		//GROUP 1
		if (optionData==OptionId.G1){
			group1 = true;
			options.clearOptions();
			text.addPara("You approach the techies to find out what they're talking about. They are deep in some kind of tech related marketing scheme.");
			text.addPara("You struggle to follow along, as countless technical terms get thrown around like nothing. They seem to be illustrating some grand plan, that will *definitely* make them rich.");
			text.addPara("After a while of listening they seem to finally notice you.");

			options.addOption("Continue",OptionId.B1);
		}
		//GROUP 1 pt2
		if (optionData==OptionId.B1){
			options.clearOptions();
			text.addPara("\"Oh hello there, interested in our little scheme are you?\" One of them says.");
			text.addPara("\"Yeah, we're looking for investors here. We have a very unique opportunity available here.\" The other guy chimes in.");
			text.addPara("They wait for your response.");

			options.addOption("Politely decline",OptionId.B2);
			options.addOption("Try to entertain the idea",OptionId.B3);
		}
		//GROUP 1 decline
		if (optionData==OptionId.B2){
			group1Chatted = true;
			options.clearOptions();
			text.addPara("\"It's your loss for sure. Once we get the credits rolling, it's going to be smooth sailing. I mean, there's almost *no risk* here.\" He says.");
			text.addPara("one of them quickly hands you a flashy business card, and gives you a firm handshake as a goodbye.");
			text.addPara("You leave before you get sucked into some godawful pyramid scheme. You're not losing your hard earned credits this easily.");

			options.addOption("Continue",OptionId.MAIN);
		}
		//GROUP 1 try to agree
		if (optionData==OptionId.B3){
			group1Chatted = true;
			options.clearOptions();
			text.addPara("\"...The profit vector only multiplies with time, a fungible investment now only becomes exponential with time. Our reported margins are about to spike with the introduction of generative price... \"");
			text.addPara("As you listen to them explain their plans in more detail, it becomes rather obvious they're trying to sucker in clueless investors with flashy new tech.");
			text.addPara("You decide there are much better ways to spend your credits, than on some pyramid scheme.");
			text.addPara("As you leave one of them quickly hands you a flashy business card, and gives you a firm handshake as a goodbye.");

			options.addOption("Continue",OptionId.MAIN);
		}
		//GROUP 2
		if (optionData==OptionId.G2){
			group2 = true;
			options.clearOptions();
			text.addPara("As you make your way over, it appears they're ready to have a toast. Before you even have a chance to react there's already a new drink in your hands.");
			text.addPara("\"For another successful operation. For greater profits!\" The commander cheers the crowd on.");

			options.addOption("Drink",OptionId.C1);
			options.addOption("Don't drink",OptionId.C2);
		}
		//GROUP 2 pt2
		if (optionData==OptionId.C1||optionData==OptionId.C2){
			options.clearOptions();
			if (optionData==OptionId.C1)text.addPara("You down the drink in one fell swoop.");
			text.addPara("You watch everyone as they finish their drinks. There are too many people to get a clear picture of anything in particular.");
			text.addPara("You do hear something about a special fleet operation of sorts, but you quickly lose them.");
			if (optionData==OptionId.C1 && alcoholCount>=1){
				alcoholCount++;
				text.addPara("You are really starting to feel it, you're quite intoxicated now.",g,h,"","");
			}

			options.addOption("Continue",OptionId.C3);
		}
		//GROUP 2 pt3
		if (optionData==OptionId.C3){
			group2Chatted = true;
			options.clearOptions();
			text.addPara("You decide to leave the crowd alone, there are just too many people to discover anything of note.");

			options.addOption("Continue",OptionId.MAIN);
		}

		//GROUP 3
		if (optionData==OptionId.G3){
			group3 = true;
			options.clearOptions();
			text.addPara("The officers are sitting at a corner table, its shiny marble like surface is like a mirror. You spot a TriPad, and multiple glasses of drink placed on it.");
			text.addPara("Surely you can use your experience as a fleet commander to impress them. Seems like they're the most likely group to know something of use.");

			options.addOption("Continue",OptionId.E1);
		}
		//GROUP 3 pt2
		if (optionData==OptionId.E1){
			options.clearOptions();
			text.addPara("One of the officers notices you \"I can see you're a captain, come join us.\" the officer shouts for you to join.");
			text.addPara("You sit down with them, and listen as one of the officers tells an anecdote from his last set of patrol duty.");
			text.addPara("\"... The whole engine section was just missing, with a big hole in his ship, This guy says he doesn't know what happened to it!\" " +
					"He is struggling to contain his laughter at this point. \"With a straight fucking face, he says that to me! Now that's why I'm never working with those geniuses from the Skathi patrol squadron ever again.\" He bursts out laughing.");

			options.addOption("Continue",OptionId.E2);
		}
		//GROUP 3 story time
		if (optionData==OptionId.E2){
			options.clearOptions();
			text.addPara("\"Why don't you tell us something captain. I'm sure you've seen your fair share of interesting events.\" He says to you.");
			text.addPara("This is your chance to earn their trust, just quickly piece something together.");
			text.addPara("You tell a story about...");

			options.addOption("Your last bounty hunt",OptionId.E3);
			options.addOption("Your last trading operation",OptionId.E4);
			options.addOption("Your last encounter with AI",OptionId.E5);
			options.addOption("Your last smuggling gig",OptionId.E6);
		}
		//GROUP 3 story time pt2
		if (optionData==OptionId.E3||optionData==OptionId.E4||optionData==OptionId.E5||optionData==OptionId.E6){
			options.clearOptions();
			text.addPara("While illustrating your story, you notice what's on the TriPad. There appears to be coordinates and orders, relating to fleet movements on display. You just need to keep these officers distracted long enough to get a good look.");
			text.addPara("You illustrate a grand story of great peril, the stakes are greater than life, then taking quick peeks at the TriPad while pretending to make some epic gestures to drive home the spectacle.");
			text.addPara("\"Woah captain, I can't believe you lived from that! That's one hell of a tale, I wouldn't even care if you made half that up.\" He says with a smug laugh.");
			text.addPara("Wow, you really are a great actor. Or maybe it's just the alcohol.");

			options.addOption("Continue",OptionId.E7);
		}
		//GROUP 3 finish
		if (optionData==OptionId.E7){
			group3Chatted = true;
			options.clearOptions();
			text.addPara("You managed to spot that a certain star system is mentioned multiple times on the TriPad's info feed.");
			String loc = questUtil.getJob3Target().getContainingLocation().getName();
			text.addPara("The "+loc+"? Certainly worth investigating, you better write that down somewhere.",g,h,loc,"");
			text.addPara("With the info gathered, you decide it's best to leave the officers alone for now, so you give them a quick goodbye.");

			options.addOption("Continue",OptionId.MAIN);
		}

		//MAIN 2
		if (optionData==OptionId.MAIN2){
			chatting = true;
			options.clearOptions();
			text.addPara("As you try to leave, the employee finally finds you again.");
			text.addPara("\"There you are capitain, I fnound you...\" There's a brief pause as "+heOrShe+" struggles to maintain their composure \" We - totally need to get one last drink before you leave!\" "+
					HeOrShe+" is in an even worse condition than when you last saw "+himOrHer+".");
			text.addPara("With a sloppy motion "+heOrShe+" points towards the drinks.");
			if(alcoholCount>=2)text.addPara("Oh no, it appears you are too intoxicated to remember that you could just say no.",g,h,"","");

			options.addOption("Agree",OptionId.H1);
			if(alcoholCount>=2)options.addOption("\"You're totally right, I could use more drink.\"",OptionId.H2);
			if(alcoholCount>=2)options.addOption("\"What! Just one more? I'm drinking way more than that!\"",OptionId.H3);
			if(alcoholCount<2)options.addOption("Leave",OptionId.LEAVE);
		}

		//GETTING WASTED
		if (optionData==OptionId.H1||optionData==OptionId.H2||optionData==OptionId.H3){
			options.clearOptions();
			dialog.getVisualPanel().fadeVisualOut();
			text.addPara("The rest of the night, very quickly becomes a blur in your head. You vaguely remember talking to dozens of people, and downing even more drinks than that.");

			options.addOption("\"Whhere's that damn drink...\"",OptionId.H4A);
			options.addOption("\"So I went in gungs blazing! Sure showed em' who's bsoss, hah!\"",OptionId.H4B);
			options.addOption("\"No, seruoislkly I just had it in my hadns!\"",OptionId.H4C);
			options.addOption("\"Yea, it's a tuogh job, but I wouldn't have it any othe way.\"",OptionId.H4D);
		}
		//GETTING WASTED hangover
		if (optionData==OptionId.H4A || optionData==OptionId.H4B || optionData==OptionId.H4C || optionData==OptionId.H4D){
			options.clearOptions();
			text.addPara("You and the employee got separated at some point, after finally leaving the party. Although you can't remember exactly why.");
			text.addPara("Now you have woken up somewhere, your head, arms, and legs hurt quite a lot. You could really use a tall glass of water.");
			text.addPara("A vaguely unpleasant smell fills the room, you hear some muffled chatter coming from the neighboring room and the distant hum of machinery.");

			options.addOption("Open your eyes",OptionId.H5);
		}
		//GETTING WASTED hangover pt2
		if (optionData==OptionId.H5){
			travelling = true;
			options.clearOptions();
			text.addPara("You are surrounded by cold dark grey concrete surfaces. There is only basic furniture in the room, made of cheap plastic. There are no windows, the only light is the sickly off green glow of the cheap light fixtures.");
			text.addPara("There are empty glass bottles in various locations around the room, not all of them quite empty. A large stain of something sticky is on the floor, next to a felled bottle. Your sidearm - a cold metal hunk is with you in bed, at least you didn't lose it.");
			text.addPara("You appear to have ended up in some cheap hab-block motel.");

			options.addOption("Continue",OptionId.H6);
		}
		//GETTING WASTED hangover pt3
		if (optionData==OptionId.H6){
			options.clearOptions();
			text.addPara("Thinking about what happened, you somehow managed to avoid your own bodyguards on your way out - which is rather impressive. " +
					"You then realize that your security officer is going to kill you once they realize you were not in fact *kidnapped*, but instead just got black out drunk - it happens.");
			text.addPara("Thankfully it appears you wrote down the coordinates on your TriPad. In fact it seems the notepad function is the last hing you had open on it. " +
					"You wrote an order of synthPizza on it in text last night - it appears to not have worked.");
			text.addPara("You also remember that you haven't checked how many credits you spent last night.");

			options.addOption("Assess the damages",OptionId.H7);
		}
		//GETTING WASTED hangover pt4
		if (optionData==OptionId.H7){
			options.clearOptions();
			text.addPara("You quickly check your credits account, it certainly was an expensive night out.");

			text.setFontSmallInsignia();
			//acquire text
			//money
			float money = mathUtil.getSeededRandomNumberInRange(4000, 7000, random);
			if(Global.getSector().getPlayerFleet().getCargo().getCredits().get() < money) money = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
			//remove
			Global.getSector().getPlayerFleet().getCargo().getCredits().add(-money);
			String creds = Misc.getDGSCredits(money);
			text.addPara("Lost " + creds, g, r, creds, "");
			text.addPara("Acquired hangover",g,r,"hangover","");
			text.setFontInsignia();

			Global.getSoundPlayer().playUISound("ui_rep_drop",1f,1f);

			text.addPara("It's time to leave, and forget that this happened.");

			options.addOption("Leave",OptionId.LEAVE_B);
		}

		//visuals
		if (chatting){
			dialog.getVisualPanel().showPersonInfo(person, true);
		}
		if (travelling){
			if (dialog.getInteractionTarget().getCustomInteractionDialogImageVisual()!=null) {
				visual.showImageVisual(dialog.getInteractionTarget().getCustomInteractionDialogImageVisual());
			} else if (dialog.getInteractionTarget().getMarket().getPlanetEntity()!=null) visual.showPlanetInfo(dialog.getInteractionTarget().getMarket().getPlanetEntity());
		}
		if (group1){
			dialog.getVisualPanel().showPersonInfo(group1person1, false);
			dialog.getVisualPanel().showSecondPerson(group1person2);
		}
		if (group2){
			dialog.getVisualPanel().showPersonInfo(group2person1, false);
		}
		if (group3){
			dialog.getVisualPanel().showPersonInfo(group3person1, false);
			dialog.getVisualPanel().showSecondPerson(group3person2);
			dialog.getVisualPanel().showThirdPerson(group3person3);
		}

		//early exit
		if (optionData==OptionId.LEAVE_A){
			text.addPara("You decide it's best to leave, and not waste time getting drunk for no good reason.");
			dialog.getVisualPanel().fadeVisualOut();

			done = true;
			PortsideBarData.getInstance().removeEvent(this);
		}
		//main exit
		if (optionData==OptionId.LEAVE){
			text.addPara("You manage to convince the employee that you're in a hurry. You then quickly leave the party, and make your way back to your ship.");
			dialog.getVisualPanel().fadeVisualOut();

			questUtil.setStage(9);
			questUtil.setCompleted(true, questStageManager.JOB3_TARGET_DISCOVERED);

			text.setFontSmallInsignia();
			//acquire text
			text.addPara("Acquired Expedition coordinates",g,h,"Expedition coordinates","");
			text.setFontInsignia();

			Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

			done = true;
			PortsideBarData.getInstance().removeEvent(this);
		}
		//hangover exit
		if (optionData==OptionId.LEAVE_B){
			text.addPara("You leave, calmly making your way back to your ship, as you try not upset your headache.");

			questUtil.setStage(9);
			questUtil.setCompleted(true, questStageManager.JOB3_TARGET_DISCOVERED);

			text.setFontSmallInsignia();
			//acquire text
			text.addPara("Acquired Expedition coordinates",g,h,"Expedition coordinates","");
			text.setFontInsignia();

			Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

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
		A7,
		B1,
		B2,
		B3,
		MAIN,
		MAIN2,
		LEAVE,
		LEAVE_A,
		LEAVE_B,
		C1,
		C2,
		C3,
		D1,
		D2,
		D3,
		D4,
		D5,
		G1,
		G2,
		G3,
		E1,
		E2,
		E3,
		E4,
		E5,
		E6,
		E7,
		H1,
		H2,
		H3,
		H4A,
		H4B,
		H4C,
		H4D,
		H4E,
		H5,
		H6,
		H7,
		H8,

	}

	@Override
	public boolean isDialogFinished() {
		return done;
	}

	protected boolean showCargoCap() {
		return false;
	}
}