package scripts.kissa.LOST_SECTOR.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import java.awt.*;
import java.util.*;
import java.util.List;

import org.lwjgl.input.Keyboard;
import scripts.kissa.LOST_SECTOR.campaign.quests.nskr_glacierCommsDialog;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questFleets;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.nskr_artifactDialog;
import scripts.kissa.LOST_SECTOR.campaign.quests.nskr_elizaDialog;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.util.*;
import scripts.kissa.LOST_SECTOR.world.nskr_gen;
import scripts.kissa.LOST_SECTOR.world.systems.frost.nskr_frost;

public class nskr_kestevenQuest extends PaginatedOptions {

	//handles dialogue and rules.csv for the quest line
	//Hack job of nex code, but it just worksTM
	//
	public static final String PERSISTENT_RANDOM_KEY = "nskr_kestevenQuestRandom";
	public static final String DIALOG_OPTION_PREFIX = "nskr_kestevenQuest_pick_";
	public static final String DIALOG_OPTION_PREFIX_REQ_SKIP = "nskr_kestevenQuest_story_pick_";
	public static final String DIALOG_OPTION_PREFIX_STORY_SKIP = "nskr_kestevenQuest_story_skip_pick_";
	public static final String DIALOG_OPTION_EXTRA_START_PREFIX = "nskr_kestevenQuest_extraStart_";
	public static final String DIALOG_OPTION_EXTRA_PREFIX = "nskr_kestevenQuest_extra_";
	public static final String PERSISTENT_KEY = "nskr_kestevenQuest";
	public static final String JOB4_INTELLIGENCE_DIALOG_KEY = "nskr_kestevenQuestJob4Intelligence";
	public static final String JOB4_SKIP_REQ_KEY = "nskr_kestevenQuestJob4SkipRequirement";
	public static final String JOB5_JACK_TIP_KEY = "nskr_kestevenQuestJob5JackTip";
	public static final String JOB5_ALICE_TIP_KEY = "nskr_kestevenQuestJob5AliceTip";
	public static final String JOB5_ALICE_TIP_KEY2 = "nskr_kestevenQuestJob5AliceTip2";
	public static final String SKIPPED_STORY_KEY = "nskr_kestevenQuestSkippedStory";

	public static final int JOB1_ARTIFACTS = 70;
	public static final int STAGE1_PAYOUT = 155000;
	public static final int STAGE3_PAYOUT = 205000;
	public static final int STAGE4_PAYOUT = 285000;
	public static final int STAGE5_PAYOUT = 565000;
	//TODO
	// un-debug
	public static final float JOB1_REP = 0.20f;
	public static final float JOB3_POWER = 0.65f;
	public static final float JOB3_REP = 0.40f;
	public static final float JOB4_POWER = 0.80f;
	public static final float JOB4_REP = 0.60f;
	public static final float JOB5_POWER = 0.95f;
	public static final float JOB5_REP = 0.80f;

	private int stage = 0;
	private int diskCount = 0;
	private static float relation = 0;
	private float power = 0f;
	private SectorEntityToken job4TargetLoc = null;
	private boolean job1tip = false;
	private boolean foughtEnigma = false;
	private boolean failedJob3 = false;
	private boolean eMessenger = false;
	private boolean job4wait = false;
	private boolean helped = false;
	//dumb work around for ESC shortcut
	private boolean extraEsc = false;
	private boolean cargo = false;
	private boolean sensored = false;
	private boolean delivered = false;
	private boolean deliveredData = false;
	private boolean foundEliza = false;
	private boolean jackTip = false;
	private boolean aliceTip = false;
	private boolean aliceTip2 = false;
	private boolean allDisks = false;
	private boolean storyCompleted = false;

	private PersonAPI jack;
	private PersonAPI alice;
	private PersonAPI nick;

	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected MarketAPI market;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected CargoAPI playerCargo;
	protected PersonAPI person;
	protected PersonAPI player;
	protected FactionAPI faction;
	protected ShipAPI ship;

	protected List<String> disabledOpts = new ArrayList<>();


	static void log(final String message) {
		Global.getLogger(nskr_kestevenQuest.class).info(message);
	}

	//STAGE CHEAT SHEET
	//0 NO QUEST
	//1 JOB 1 STARTED
	//2 JOB 1 READY TO COMPLETE
	//3 unused lol
	//4 unused lol
	//5 unused lol
	//6 JOB 3 AVAILABLE
	//7 TALK TO ALICE
	//8 JOB 3 STARTED
	//9 JOB 3 KNOW LOCATION
	//10 JOB 3 COMPLETED
	//11 JOB 4 AVAILABLE
	//12 JOB 4 STARTED
	//13 JOB 4 COMPLETED
	//14 JOB 5 AVAILABLE
	//15 JOB 5 GO TO BAR
	//16 JOB 5 STARTED
	//17 JOB 5 CACHE FOUND
	//18 JOB 5 DEFEATED GUARDIAN
	//19 JOB 5 TURN IN RECOVERED CHIP
	//20 JOB 5 COMPLETED
	//99 END MISSIONS
	//

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
			case "getStage":
				setupDelegateDialog(dialog);
				showOptions();
				addStageOptions();
				extraEsc = false;
				break;
			case "advanceStage":
				showOptions();
				showQuestInfoAndPrepare(dialog.getTextPanel());
				extraEsc = false;
				break;
			case "advanceStageReqSkip":
				showOptions();
				SPOptionPicked();
				showQuestInfoAndPrepare(dialog.getTextPanel());
				extraEsc = false;
				break;
			case "advanceStageStorySkip":
				showOptions();
				SkipStoryOptionPicked();
				extraEsc = false;
				break;
			case "extraDialogueStart":
				showOptions();
				extraDialogueStart();
				extraEsc = true;
				break;
			case "extraDialogue":
				int index = Integer.parseInt(memoryMap.get(MemKeys.LOCAL).getString("$option").substring(DIALOG_OPTION_EXTRA_PREFIX.length()));
				extraDialogue(index);
				break;
			case "skip":
				showOptions();
				skip();
				extraEsc = true;
				break;
			case "confirmSkip":
				confirmSkip();
				break;
			case "confirmQuest":
				quest();
				break;
		}
		updateOptions();
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

		player = Global.getSector().getPlayerPerson();
		person = dialog.getInteractionTarget().getActivePerson();
		//faction = person.getFaction();

		jack = util.getJack();
		alice = util.getAlice();
		nick = util.getNick();

		power = powerLevel.get(0.2f, 0f,2f);
		stage = questUtil.getStage();
		relation = Global.getSector().getPlayerFaction().getRelationship("kesteven");
		foughtEnigma = questUtil.getCompleted(questStageManager.HAS_FOUGHT_ENIGMA_KEY);
		job1tip = questUtil.getCompleted(questStageManager.JOB1_TIP_KEY);
		failedJob3 = questUtil.getFailed(questStageManager.JOB3_FAIL_KEY);
		eMessenger = questUtil.getCompleted(questStageManager.E_MESSENGER_TALKED_ASK_ABOUT_KEY);
		job4wait = questUtil.getCompleted(questStageManager.JOB4_WAIT_KEY);
		job4TargetLoc = questUtil.getJob4EnemyTarget();
		helped = questUtil.getCompleted(questStageManager.JOB4_HELPED_KEY);

		cargo = playerCargo.getCommodityQuantity("nskr_electronics") >= JOB1_ARTIFACTS;
		delivered = questUtil.getCompleted(questStageManager.JOB1_DELIVERED_KEY);
		deliveredData = questUtil.getCompleted(questStageManager.JOB1_DELIVERED_DATA_KEY);
		sensored = questUtil.getCompleted(questStageManager.JOB1_SENSORED_KEY);

		aliceTip = questUtil.getCompleted(nskr_kestevenQuest.JOB5_ALICE_TIP_KEY);
		aliceTip2 = questUtil.getCompleted(nskr_kestevenQuest.JOB5_ALICE_TIP_KEY2);
		jackTip = questUtil.getCompleted(nskr_kestevenQuest.JOB5_JACK_TIP_KEY);

		foundEliza = questUtil.getCompleted(questStageManager.JOB5_FOUND_ELIZA_KEY);

		diskCount = questUtil.getDisksRecovered();
		allDisks = questUtil.getDisksRecovered()>=5;

		storyCompleted = (boolean) nskr_modPlugin.loadFromConfig(nskr_modPlugin.COMPLETED_STORY_KEY);
	}
	
	//@Override
	//public void showOptions() {
		//super.showOptions();
		//for (String optId : disabledOpts)
		//{
		//	dialog.getOptionPanel().setEnabled(optId, false);
		//}
		//dialog.getOptionPanel().setShortcut("nskr_kestevenQuestExit", Keyboard.KEY_ESCAPE, false, false, false, false);
		//if(extraEsc) {
		//	dialog.getOptionPanel().setShortcut(DIALOG_OPTION_PREFIX, Keyboard.KEY_ESCAPE, false, false, false, false);
		//}
	//}

	public void updateOptions() {
		for (String optId : disabledOpts)
		{
			dialog.getOptionPanel().setEnabled(optId, false);
		}
		dialog.getOptionPanel().setShortcut("nskr_kestevenQuestExit", Keyboard.KEY_ESCAPE, false, false, false, false);
		if(extraEsc) {
			dialog.getOptionPanel().setShortcut(DIALOG_OPTION_PREFIX, Keyboard.KEY_ESCAPE, false, false, false, false);
		}
	}

	/**
	 * Adds the dialog options.
	 */
	protected void addStageOptions()
	{
		dialog.getOptionPanel().clearOptions();
		String desc = "";
		String jobText = "\"There are no new jobs available at the moment.\"";
		String optId = DIALOG_OPTION_PREFIX;
		boolean aliceIntro = questUtil.getCompleted("nskr_aliceIntro");
		boolean jackIntro = questUtil.getCompleted("nskr_jackIntro");

		//Jack Lapua dialogue
		if (person==jack) {
			//top start text
			if(jackIntro)text.addPara("\"Hmmm. Let me check around.\"");
			//job 1
			if (stage == 0 && relation >= JOB1_REP) {
				desc = "Enemy Unknown";
				jobText = "\"There is a new job available at the moment, are you interested?\"";
				//skip story
				if (storyCompleted) addStorySkipOption();
			}
			//job 1 not enough rep
			if (stage == 0 && relation < JOB1_REP) {
				jobText = "\"There is a new job available at the moment, but we require someone more qualified. Come back later when I know you can be trusted.\"";
				//skip story
				if (storyCompleted) addStorySkipOption();
			}
			//job 1 accepted
			if (stage == 1 && job1tip) {
				jobText = "\"I've already given you the tasks. Finish one first, then we can talk.\"";
			}
			if (stage == 1 && !sensored && !cargo && !job1tip && questUtil.getJob1Tip()!=null) {
				jobText = "\"What's up captain. Have you had any luck with the job?\"";
				desc = "\"How am I supposed to find them?\"";
			}
			//job 1 task 1 completed
			if (stage == 1 && sensored && !deliveredData) {
				desc = "Hand over the package";
				jobText = "\"You have the sensor package? Perfect, now hand it over.\"";
			}
			//job 1 task 2 completed
			if (stage == 1 && cargo && !delivered) {
				desc = "Hand over the electronics";
				jobText = "\"You have the cargo? Excellent work.\"";
			}
			//job 1 task 1&2 completed BOTH
			if (stage == 1 && cargo && !delivered && sensored && !deliveredData) {
				desc = "Hand over everything";
				jobText = "\"You have both the cargo and the sensor package? Impressive.\"";
			}
			//job 1 ready to complete
			if (stage == 2) {
				desc = "\"I've done everything.\"";
				jobText = "\"Are you finished with the tasks?\"";
			}
			//job 3 wait
			if (stage == 3) {
				jobText = "\"There's nothing urgent at the moment. Come back later for more work.\"";
			}
			//job 3 jack
			if (stage == 6 && relation >= JOB3_REP && power > JOB3_POWER) {
				desc = "Hostile Takeover";
				jobText = "\"There is a new job available at the moment, are you interested?\"";
				//skip story
				if (storyCompleted) addStorySkipOption();
			}
			//job 3 too weak
			if (stage == 6 && power < JOB3_POWER) {
				jobText = "\"There is a new job available at the moment, but we require someone more qualified. Come back later with a proper fleet.\"";
				//SP skip
				addSkipOption();
				//skip story
				if (storyCompleted) addStorySkipOption();
			}
			//job 3 not enough rep
			if (stage == 6 && relation < JOB3_REP) {
				jobText = "\"There is a new job available at the moment, but we require someone more qualified. Come back later when I know you can be trusted.\"";
				//skip story
				if (storyCompleted) addStorySkipOption();
			}
			//job 3 in progress jack
			if (stage == 7) {
				jobText = "\"I told you to go talk to Alice.\"";
			}
			//job 3 in progress jack
			if (stage == 8 || stage == 9 || stage == 10) {
				jobText = "\"You're already working for Alice.\"";
			}
			//job 4 available
			if (stage == 11 && job4wait) {
				jobText = "\"I hear that Alice has more work available.\"";
			}
			//job 4 in progress jack
			if (stage == 12 || stage == 13) {
				jobText = "\"You're already working for Alice.\"";
			}
			//job 5 start
			if (stage == 14 && relation >= JOB5_REP && power > JOB5_POWER) {
				desc = "\"I'm listening.\"";
				jobText = "\"There is something important we need you to work on. We should discuss it in detail.\"";
			}
			//job 5 too weak
			if (stage == 14 && power < JOB5_POWER) {
				jobText = "\"There is a new job available at the moment, but we require someone more qualified. Come back later with a proper fleet.\"";
				//SP skip
				addSkipOption();
				//skip story
				if (storyCompleted) addStorySkipOption();
			}
			//job 5 not enough rep
			if (stage == 14 && relation < JOB5_REP) {
				jobText = "\"There is a new job available at the moment, but we require someone more qualified. Come back later when I know you can be trusted.\"";
				//skip story
				if (storyCompleted) addStorySkipOption();
			}
			//go to bar dumbass
			if (stage == 15) {
				jobText = "\"I said, head to the bar.\"";
			}
			//job 5 start tip
			if (stage == 16 && !jackTip && !allDisks) {
				desc = "\"Okay\"";
				jobText = "\"Let's discuss the leads we have.\"";
			}
			//job 5 in progress
			if (stage == 16 || stage == 17|| stage == 18|| stage == 19) {
				if (jackTip) {
					jobText = "\"There's nothing new to report " + player.getName().getFirst() + ".\"";
				}
			}
			//job 5 tip 2 go to Alice
			if (stage == 16 && jackTip && aliceTip && !aliceTip2 && nskr_artifactDialog.getRecoveredSatelliteCount()>=2 && !allDisks) {
				jobText = "\"I hear Alice wants to talk to you.\"";
			}
			//job5 all disks
			if (stage == 16 && allDisks) {
				jobText = "\"I see you have all the disks. You should go and talk to Alice about them.\"";
			}
			//job5 finished
			if (stage == 20){
				jobText = "\"Nothing new has popped up "+player.getName().getFirst()+".\"";
			}
		}
		//Alice Lumi dialogue
		if (person==alice){
			//not introduced yet
			if (stage <= 6) {
				jobText = "\"Why are you contacting me? I am busy with work.\"";
			}
			//top start text
			if (stage >= 7) {
				if(aliceIntro)text.addPara("\"Oh, you're here.\"");
			}
			//job 3 start alice
			if (stage == 7) {
				desc = "Hostile Takeover";
				jobText = "\"So you finally showed up. Jack has already told me all about you. He doesn't just send any spacer goon over to me, so I have high expectations for you "+player.getName().getFullName()+".\"";
				//skip story
				if (storyCompleted) addStorySkipOption();
			}
			//job 3 in progress alice
			if (stage == 8 || stage == 9) {
				jobText = "\"Why are you still here?\" She scoffs. \"Get to work, this is time sensitive remember?\"";
			}
			//job 3 completed
			if (stage == 10 && !failedJob3) {
				desc = "Hand over your AAR";
				jobText = "\"My intel suggests you were successful in sabotaging Tri-Tachyon.\"";
			}
			//job 3 failed
			if (stage == 10 && failedJob3) {
				desc = "Hand over your AAR";
				jobText = "\"My intel suggests you weren't successful in sabotaging Tri-Tachyon.\"";
			}
			//job 4 start
			if (stage == 11 && relation >= JOB4_REP && power > JOB4_POWER && job4wait || stage == 11 && relation >= JOB4_REP && questUtil.getCompleted(JOB4_SKIP_REQ_KEY)) {
				desc = "Operation Lifesaver";
				jobText = "\"There is a new job available at the moment, are you interested?\"";
				//skip story
				if (storyCompleted) addStorySkipOption();
			}
			//job 4 too weak
			if (stage == 11 && power < JOB4_POWER && job4wait && !questUtil.getCompleted(JOB4_SKIP_REQ_KEY)) {
				jobText = "\"There is a new job available at the moment, but we require someone more qualified. Come back later with a proper fleet.\"";
				//SP skip
				addSkipOption();
				//skip story
				if (storyCompleted) addStorySkipOption();
			}
			//job 4 not enough rep
			if (stage == 11 && relation < JOB4_REP && job4wait) {
				jobText = "\"There is a new job available at the moment, but we require someone more qualified. Come back later when I know you can be trusted.\"";
				//skip story
				if (storyCompleted) addStorySkipOption();
			}
			//job 4 in progress
			if (stage == 12) {
				jobText = "\"Why are you still here? Get to work.\"";
			}
			//job 4 completed no help
			if (stage == 13 && !helped) {
				desc = "Hand over the fleets coordinates and your combat log";
				jobText = "\"Did you complete the operational objectives?\"";
			}
			//job 4 completed helped
			if (stage == 13 && helped) {
				desc = "Hand over your operational report";
				jobText = "\"Did you complete the operational objectives?\"";
			}
			//job 5 go to jack
			if (stage == 14) {
				jobText = "\"Yes, there is something important. Go talk to Jack about it.\"";
			}
			//go to bar dumbass
			if (stage == 15) {
				jobText = "\"You should be heading to the bar. Do you always struggle with basic instructions?\"";
			}
			//job 5 start tip
			if (stage == 16 && !aliceTip && !allDisks) {
				desc = "\"Okay\"";
				jobText = "\"Let's talk about the leads we have.\"";
			}
			//job 5 in progress
			if (stage == 16 || stage == 17|| stage == 18|| stage == 19) {
				if (aliceTip) {
					jobText = "\"I already told you everything, get to work.\"";
				}
			}
			//job 5 tip 2
			if (stage == 16 && jackTip && aliceTip && !aliceTip2 && nskr_artifactDialog.getRecoveredSatelliteCount()>=2 && !allDisks) {
				desc = "Continue";
				jobText = "\"There's something new we should discuss.\"";
			}
			//job5 all disks
			if (stage == 16 && allDisks) {
				desc = "Continue";
				jobText = "\"I hear you have all the disks. Let's get to work then.\"";
			}
			//job5 finished
			if (stage == 20){
				jobText = "\"Sadly there's nothing new to report captain.\"";
			}
		}
		//Nicholas Antoine dialog
		if (person==nick) {
			//top start text
			text.addPara("\"Um, welcome captain.\"");
			//not introduced yet
			if (stage<=11) {
				jobText = "\"You know, I am not expecting anyone. Please leave me to my work.\"";
			}
			//job4 tip dialogue
			//make sure target exists
			if (job4TargetLoc!=null) {
				if (stage == 12 && questUtil.getDialogStage(JOB4_INTELLIGENCE_DIALOG_KEY) == 0) {
					desc = "\"Tell me what you know.\"";
					jobText = "\"Uhhh. Are you here to talk about the missing Special Operations fleet?\"";
				}
			}
			//null backup
			else if (stage == 12) {
				jobText = "\"...There is nothing new to report captain.\"";
			}
			//already tipped
			if (stage==12 && questUtil.getDialogStage(JOB4_INTELLIGENCE_DIALOG_KEY)>=1) {
				jobText = "\"...There is nothing new to report captain.\"";
			}
			//no quest
			if (stage>=13) {
				jobText = "\"You know, I am not expecting anyone. Please leave me to my work.\"";
			}
		}
		//first time descriptor text
		//one time
		//jack
		if (person==jack && stage>=0 && !jackIntro){
			text.addPara("On the holodisplay you are met with a charismatic smile from officer Lapua. A well groomed man, his manners have that corporate superficiality down to a perfection.");
			text.addPara("\"What can I do for you captain?\"");
			questUtil.setCompleted(true, "nskr_jackIntro");
		}
		//alice
		if (person==alice && stage>=7 && !aliceIntro){
			text.addPara("A woman is staring at you through the holodisplay, her expression is unchanging. Manager Lumi is intently analyzing every part of your visage.");
			text.addPara("\"Ahh, yes it's you.\"");
			questUtil.setCompleted(true, "nskr_aliceIntro");
		}
		//nick
		boolean nickIntro = questUtil.getCompleted("nskr_nickIntro");
		if (person==nick && stage>=12 && !nickIntro){
			text.addPara("Nicholas is rather reserved when it comes to talking. It is clear he works on the computer side of communications.");
			questUtil.setCompleted(true, "nskr_nickIntro");
		}

		//adds the text
		if (desc.length()>0){
			text.setFontSmallInsignia();
			text.setFontInsignia();

			String str = desc;
			dialog.getOptionPanel().addOption(str, optId);

			text.addPara(jobText);
		} else {
			text.setFontSmallInsignia();
			text.setFontInsignia();
			text.addPara(jobText);
		}

		dialog.getOptionPanel().addOption("Back", "nskr_kestevenQuestExit");
	}

	protected void addSkipOption(){

		dialog.getOptionPanel().addOption("I believe you'll find me more than capable.", DIALOG_OPTION_PREFIX_REQ_SKIP);
		dialog.makeStoryOption(DIALOG_OPTION_PREFIX_REQ_SKIP,1,1.00f,"ui_char_spent_story_point");
		//tooltip
		dialog.getOptionPanel().addOptionTooltipAppender(DIALOG_OPTION_PREFIX_REQ_SKIP, new OptionPanelAPI.OptionTooltipCreator() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean hadOtherText) {
				float opad = 10f;
				float initPad = 0f;
				if (hadOtherText) initPad = opad;
				tooltip.addStoryPointUseInfo(initPad, 1, 1f, false);
				int sp = Global.getSector().getPlayerStats().getStoryPoints();
				String points = "points";
				if (sp == 1) points = "point";
				tooltip.addPara("You have %s " + Misc.STORY + " " + points + ".", opad,
						Misc.getStoryOptionColor(), "" + sp);
			}
		});
		//pop up
		dialog.getOptionPanel().addOptionConfirmation(DIALOG_OPTION_PREFIX_REQ_SKIP,
				new SetStoryOption.BaseOptionStoryPointActionDelegate(dialog,
						new SetStoryOption.StoryOptionParams(DIALOG_OPTION_PREFIX_REQ_SKIP,1,"nskr_skipRequirement","ui_char_spent_story_point","Skipped quest reqs.")));

	}

	protected void SPOptionPicked(){
		text.setFontInsignia();

		Global.getSoundPlayer().playUISound("ui_char_spent_story_point",1f,1f);

		//save skip to mem, only required for job4
		if (stage==11){
			questUtil.setCompleted(true, JOB4_SKIP_REQ_KEY);
		}
	}

	protected void addStorySkipOption(){

		dialog.getOptionPanel().addOption("You are looking for the Cache and the UPC, right? I think I can help. (Skip story)", DIALOG_OPTION_PREFIX_STORY_SKIP);
		dialog.makeStoryOption(DIALOG_OPTION_PREFIX_STORY_SKIP,5,0.00f,"ui_char_spent_story_point");
		//tooltip
		dialog.getOptionPanel().addOptionTooltipAppender(DIALOG_OPTION_PREFIX_STORY_SKIP, new OptionPanelAPI.OptionTooltipCreator() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean hadOtherText) {
				float opad = 10f;
				float initPad = 0f;
				if (hadOtherText) initPad = opad;
				tooltip.addStoryPointUseInfo(initPad, 5, 0f, false);
				int sp = Global.getSector().getPlayerStats().getStoryPoints();
				String points = "points";
				if (sp == 1) points = "point";
				tooltip.addPara("You have %s " + Misc.STORY + " " + points + ".", opad,
						Misc.getStoryOptionColor(), "" + sp);
			}
		});
		//pop up
		dialog.getOptionPanel().addOptionConfirmation(DIALOG_OPTION_PREFIX_STORY_SKIP,
				new SetStoryOption.BaseOptionStoryPointActionDelegate(dialog,
						new SetStoryOption.StoryOptionParams(DIALOG_OPTION_PREFIX_STORY_SKIP,5,"nskr_skipStory","ui_char_spent_story_point","Skipped story.")));

	}

	protected void SkipStoryOptionPicked(){
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		Color r = Misc.getNegativeHighlightColor();

		text.setFontInsignia();

		Global.getSoundPlayer().playUISound("ui_char_spent_story_point",1f,1f);

		if (person==jack) {
			text.addParagraph("He looks almost flustered. \"What- How did you...\" He thinks for a moment.");
			text.addParagraph("\"You know we got his strange lead recently pointing to these coordinates, I think you should investigate.\" " +
					"He looks a little worried. \"Huh, I actually don't remember how we got these...\"");
		}
		if (person==alice){
			text.addParagraph("She looks confused for a moment. \"What- How did you...\" She thinks for a moment.");
			text.addParagraph("\"You know we got his strange lead recently pointing to these coordinates, I think you should investigate.\" " +
					"She looks distressed. \"Huh... I actually don't remember how we got these...\"");
		}

		//job3
		questUtil.spawnArtifact(questUtil.getJob3Target(),3);
		util.addDormant(questUtil.getJob3Target(), "enigma", 45f, 50f, 0f, 1f, 1f, 1f, 1, 1);
		//job4
		questFleets.spawnJob4Target();
		questUtil.spawnArtifact(questUtil.getJob4EnemyTarget(),4);
		questStageManager.spawnJob4Wrecks(nskr_kestevenQuest.getRandom());
		//job5
		questUtil.setCompleted(true, nskr_artifactDialog.RECOVERED_4_KEY);
		questUtil.setCompleted(true, nskr_artifactDialog.RECOVERED_3_KEY);
		nskr_artifactDialog.setRecoveredSatelliteCount(2);
		questUtil.setCompleted(true, questStageManager.JOB5_FOUND_FROST_KEY);
		questUtil.setCompleted(true, nskr_glacierCommsDialog.RECOVERED_KEY);
		questUtil.setCompleted(true, nskr_kestevenQuest.JOB5_ALICE_TIP_KEY);
		questUtil.setCompleted(true, nskr_kestevenQuest.JOB5_ALICE_TIP_KEY2);
		questUtil.setCompleted(true, nskr_kestevenQuest.JOB5_JACK_TIP_KEY);
		questUtil.setCompleted(true, questStageManager.JOB5_FOUND_ELIZA_KEY);
		questUtil.setCompleted(true, nskr_elizaDialog.DIALOG_FINISHED_KEY);
		questUtil.setCompleted(true, nskr_elizaDialog.ELIZA_HELP_KEY);
		if(questUtil.getElizaLoc()==null) {
			questUtil.setElizaLoc();
			PersonAPI eliza = nskr_gen.genEliza();
			questUtil.getElizaLoc().getMarket().getCommDirectory().addPerson(eliza, 1);
			questUtil.getElizaLoc().getMarket().addPerson(eliza);
			log("Eliza loc " + questUtil.getElizaLoc().getMarket().getName());
		}

		questUtil.setCompleted(true, questStageManager.FOUND_CACHE_KEY);
		questUtil.setStage(17);

		//ineligible for hard mode completion
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (data.containsKey(nskr_modPlugin.STARFARER_MODE_FROM_START_KEY)) {
			data.put(nskr_modPlugin.STARFARER_MODE_FROM_START_KEY, false);
		}
		questUtil.setCompleted(true, SKIPPED_STORY_KEY);


		text.setFontSmallInsignia();
		text.addPara("Added log entry for the Delve", g, h,"the Delve","");
		text.addPara("Disabled questline achievements", g, r,"Disabled","");

		Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);
		text.setFontInsignia();

		//WHY THE FUCK DOESN'T THIS WORK FROM RULES FOR THIS???????
		dialog.getOptionPanel().addOption("Leave", "nskr_kestevenQuestExit");

	}

	protected void showQuestInfoAndPrepare(TextPanelAPI text)
	{
		dialog.getOptionPanel().clearOptions();
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		Color r = Misc.getNegativeHighlightColor();
		float pad = 3f;
		float opad = 10f;
		String desc = "";
		boolean noLeave = false;

		//job 1 description
		if(stage==0) {
			text.addParagraph("\"Our intelligence has been tracking down worrying reports of a new type of rogue AI threat lurking in the outer sector. We are in need of some help, in gathering intel on this entity.\"");
			text.addParagraph("\"You would be doing a set of tasks for us.\" He starts reading out from something, probably an internal briefing.");
			text.addPara("Task 1 is to track down and engage a fleet belonging to this AI threat, destroy at least one target ship while running our sensor package. Then return it to us here.",tc,h,"track down and engage a fleet belonging to this AI threat, destroy at least one target ship","");
			text.addPara("Task 2 is to recover a quantity of electronics and other crucial components from the wrecked ships. Around "+ JOB1_ARTIFACTS +" should be enough",tc,h,"recover a quantity of electronics and other crucial components from the wrecked ships","");

			text.addParagraph("He pauses to give you a quick look, inspecting your reaction.");
			String payout = Misc.getDGSCredits(STAGE1_PAYOUT);
			desc = "\"You will be compensated of course, our payout for the job is " + payout+"\"";
			text.addPara(desc,tc,h,payout,"");

			text.addPara("\"Can you handle this captain?\" He raises his eyebrow inviting you to respond.");

			text.setFontInsignia();

			dialog.getOptionPanel().addOption("Accept", "nskr_kestevenQuestConfirmQuest");
			//Extra dialog
			dialog.getOptionPanel().addOption("\"I have some questions.\"", DIALOG_OPTION_EXTRA_START_PREFIX);
		}
		//it just works
		if (stage == 1 && cargo && !delivered && sensored && !deliveredData){
			text.addParagraph("\"We've been waiting to see some concrete data on this subject. Nice work captain.\" He makes a vaguely congratulatory gesture.");

			text.setFontInsignia();
			//complete both
			playerCargo.removeCommodity("nskr_electronics", JOB1_ARTIFACTS);
			text.setFontSmallInsignia();
			text.addPara("Lost " + JOB1_ARTIFACTS + " units of Artifact Electronics", g, r, JOB1_ARTIFACTS + " units of Artifact Electronics", "");
			text.addPara("Lost sensor package", g, r, "", "");

			Global.getSoundPlayer().playUISound("ui_rep_raise", 1f, 1f);
			text.setFontInsignia();

			questUtil.setCompleted(true, questStageManager.JOB1_DELIVERED_DATA_KEY);
			questUtil.setCompleted(true, questStageManager.JOB1_DELIVERED_KEY);

			Global.getSoundPlayer().playUISound("ui_rep_raise", 1f, 1f);
		} else if (stage == 1 && cargo && !delivered) {
			//complete task2
			text.addParagraph("\"Perfect. This will proof itself invaluable to our efforts.\"");

			text.setFontInsignia();

			playerCargo.removeCommodity("nskr_electronics", JOB1_ARTIFACTS);
			text.setFontSmallInsignia();
			text.addPara("Lost " + JOB1_ARTIFACTS + " units of Artifact Electronics", g, r, JOB1_ARTIFACTS + " units of Artifact Electronics", "");

			Global.getSoundPlayer().playUISound("ui_rep_raise", 1f, 1f);
			text.setFontInsignia();

			questUtil.setCompleted(true, questStageManager.JOB1_DELIVERED_KEY);
		} else if (stage == 1 && sensored && !deliveredData){
			//complete task1
			text.addParagraph("\"We've been waiting to see some concrete data on this subject. Nice work captain.\" He makes a vaguely congratulatory gesture.");

			text.setFontSmallInsignia();
			text.addPara("Lost sensor package", g, r, "", "");

			Global.getSoundPlayer().playUISound("ui_rep_raise", 1f, 1f);
			text.setFontInsignia();

			questUtil.setCompleted(true, questStageManager.JOB1_DELIVERED_DATA_KEY);
		} else if (stage == 1 && !deliveredData && !delivered && !job1tip && questUtil.getJob1Tip()!=null) {
			//tip
			StarSystemAPI loc = questUtil.getJob1Tip();
			String str = "\"Our best lead is the " + loc.getName() + ". You should start from there.\"";
			String hl = loc.getName();
			text.addPara(str,tc,h,hl,"");
			text.addPara("\"That should give you something to work on captain.\" He nods and shortly cuts the comm link.");

			questUtil.setCompleted(true, questStageManager.JOB1_TIP_KEY);
		}
		//job 1 complete
		if(stage==2) {
			text.addParagraph("\"With all this secured, we should learn a lot more about this \"Enigma\" entity.\" He has a self satisfied look on his face.");

			text.setFontInsignia();

			dialog.getOptionPanel().addOption("Continue", "nskr_kestevenQuestConfirmQuest");
			dialog.getOptionPanel().addOption("\"I have some questions.\"", DIALOG_OPTION_EXTRA_START_PREFIX);
			noLeave = true;
		}
		//job 3 start Jack
		if(stage==6) {
			text.addParagraph("\"You'll need to talk to Alice for this one. Your contact in R&D, I hear she needs some work done.\"");
			text.addPara("\"Oh, by the way we've set up an exchange program for any Artifact Electronics you recover. Go talk to Alice about it.\" He gives you a quick wink that you barely catch.",tc,h,"exchange program for any Artifact Electronics","");

			text.addPara("\"Hope you ready for more work.\" He seems eager to get things moving.");

			text.setFontInsignia();

			dialog.getOptionPanel().addOption("Continue", "nskr_kestevenQuestConfirmQuest");
			noLeave = true;
		}
		//job 3 description
		if(stage==7) {

			SectorEntityToken location = questUtil.getJob3Start();
			String loc = location.getMarket().getName();
			text.addPara("\"Our intelligence has tracked down a Tri-Tachyon expedition on Enigma activity, said to leave from "+loc+". We need you to figure out their destination and make sure that they don't make it back. Harsh I know, but we can't risk those fools at Tri-Tachyon getting ahead in this field.\"",tc,h,loc,"");
			text.addPara("\"I don't care how you do it, but the fleet must neutralized stealthily. This is sabotage, not a declaration of war.\"",tc,h,"neutralized stealthily","");

			text.addPara("She pauses, as if to check that you're still listening.");
			String payout = Misc.getDGSCredits(STAGE3_PAYOUT);
			desc = "\"Of course, my reward will be " + payout + ". Fair warning this mission is time sensitive, you have around 90 days until the fleet has completed its task, and we will have missed our mark. So prepare accordingly before starting this job.\"";
			text.addPara(desc,tc,h,payout,"this mission is time sensitive, you have around 90 days");

			if(power<JOB3_POWER+0.15f){
				text.addPara("\"Looking at what you currently have at your disposal. This job could be exceptionally difficult for your current fleet.\" There is a look of doubt on her face.",tc,h,"exceptionally difficult","");
			}

			text.addPara("\"I hope you won't disappoint me.\" She looks impatient waiting for your response.");

			dialog.getOptionPanel().addOption("Accept", "nskr_kestevenQuestConfirmQuest");
			//Extra dialog
			dialog.getOptionPanel().addOption("\"I have some questions.\"", DIALOG_OPTION_EXTRA_START_PREFIX);

			text.setFontInsignia();
		}
		//job 3 complete
		if(stage==10 && !failedJob3) {
			text.addParagraph("\"This will set their efforts back for quite some time, giving us ample time to progress our own study of Enigma.\"");
			text.addPara("She has a slight, but devious smile on her face.",tc,h,"","");
			text.addPara("\"Don't think I forgot about your payment.\"",tc,h,"","");

			text.setFontInsignia();

			dialog.getOptionPanel().addOption("Continue", "nskr_kestevenQuestConfirmQuest");
			noLeave = true;
			//Extra dialog
			dialog.getOptionPanel().addOption("\"I have some questions.\"", DIALOG_OPTION_EXTRA_START_PREFIX);
		}
		//job 3 failed
		if(stage==10 && failedJob3) {
			text.addParagraph("\"Let's hope they don't pull ahead in this race thanks to your little fuck up.\"");
			text.addPara("\"Don't even think you'll be getting paid for this.\"",tc,h,"","");

			text.addPara("She seems quite frustrated with you.",g,h,"","");

			text.setFontInsignia();

			dialog.getOptionPanel().addOption("Continue", "nskr_kestevenQuestConfirmQuest");
			noLeave = true;
			//Extra dialog
			dialog.getOptionPanel().addOption("\"I have some questions.\"", DIALOG_OPTION_EXTRA_START_PREFIX);
		}
		//job 4 description
		if(stage==11) {
			Constellation constellation = questUtil.getJob4FriendlyTarget().getConstellation();

			text.addParagraph("\"Our Special Operations fleet has gone silent for a worrying amount of time.\" There is a hint of genuine worry on her face. \"We need you to go and find them, and then figure out what is going on.\"" +
					" She begins to mumble to herself while looking through various data projections. \"I hope they didn't lose *that* equipment. It would be a serious blow-back...\"");
			text.addParagraph("She seems to finally have the right file open. \"As you know, it's very likely that something unusual has happened so prepare for the worst. Might be a good idea to grab some extra supplies and fuel in case they need emergency assistance.\"");

			String payout = Misc.getDGSCredits(STAGE4_PAYOUT);
			desc = "\"Their task was to analyze suspected Enigma activity in "+ constellation.getName()+" constellation" +". Your job is to locate them, establish contact, and eliminate any existing threats in the area. Pay for the job is " + payout + "\"";
			text.addPara(desc,tc,h,constellation.getName()+" constellation" ,payout);

			if (questUtil.outpostExists())text.addPara("\"Oh, by the way, you should talk to Nicholas Antoine. He works in communications and is currently stationed at "+ util.getOutpost().getName()+". He most likely has some more information.\"",tc,h, util.getOutpost().getName(),"");

			if(power<JOB4_POWER+0.15f){
				text.addPara("\"Looking at what you currently have at your disposal. This job could be exceptionally difficult for your current fleet.\" There is a look of doubt on her face.",tc,h,"exceptionally difficult","");
			}

			text.addPara("\"Are you able to help us captain?\" She tries to measure your response.");

			text.setFontInsignia();

			dialog.getOptionPanel().addOption("Accept", "nskr_kestevenQuestConfirmQuest");
			//Extra dialog
			dialog.getOptionPanel().addOption("\"I have some questions.\"", DIALOG_OPTION_EXTRA_START_PREFIX);
		}

		//job4 intelligence dialog nick
		boolean found = questUtil.getCompleted(questStageManager.JOB4_FOUND_TARGET_KEY);
		//standard
		if(stage==12 && !found){
			String hintLoc = job4TargetLoc.getStarSystem().getName();
			text.addPara("\"So, the fleet was instructed to send encrypted hyperwave signals using rather expensive Domain comms equipment. You know I hope they didn't lose that stuff... " +
					"Anyways, every few weeks they would report on their progress \" He scratches his head. \"and umm- we could pick up those transmissions here and decrypt them.\"",tc,h,"","");
			text.addPara("\"There's this one thing. uhh-\" He shifts around in his seat. \"A few days after their last known report, we picked up a burst of signals coming from a specific part of the "+hintLoc+". " +
					"\"He looks down at something.\" The um- signal was much weaker in magnitude, to the level that we could only decipher its direction...\"",tc,h,hintLoc,"");
			text.addPara("\"...That's pretty much all I know.\"",tc,h,"","");

			questUtil.setDialogStage(1, JOB4_INTELLIGENCE_DIALOG_KEY);

			text.setFontSmallInsignia();
			text.addPara("Updated log entry for Operation Lifesaver",g,h,"Operation Lifesaver","");

			Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);
			text.setFontInsignia();

			dialog.getOptionPanel().addOption("Leave", "nskr_kestevenQuestExit");

			text.setFontInsignia();
			noLeave = true;
		}
		//already found
		if(stage==12 && found){
			String hintLoc = job4TargetLoc.getStarSystem().getName();
			text.addPara("\"So, the fleet was instructed to send encrypted hyperwave signals using rather expensive Domain comms equipment. You know I hope they didn't lose that stuff... " +
					"Anyways, every few weeks they would report on their progress \" He scratches his head. \"and umm- we could pick up those transmissions here and decrypt them.\"",tc,h,"","");
			text.addPara("\"There's this one thing. uhh-\" He shifts around in his seat. \"A few days after their last known report, we picked up a burst of signals coming from a specific part of the "+hintLoc+". " +
					"\"He looks down at something.\" The um- signal was much weaker in magnitude, to the level that we could only decipher its direction...\"",tc,h,hintLoc,"");
			text.addPara("\"...Ah- but it looks like you've already investigated that location. I'm afraid I can't be of any more help then.\"",tc,h,"","");

			questUtil.setDialogStage(1, JOB4_INTELLIGENCE_DIALOG_KEY);

			dialog.getOptionPanel().addOption("Leave", "nskr_kestevenQuestExit");

			text.setFontInsignia();
			noLeave = true;
		}

		//job 4
		if(stage==13 && !helped) {
			text.addParagraph("\"Ambushed by an Enigma strike group you say? Worrying. We were hoping their attacks would stay uncoordinated, but there is worrying trend of increased precision and purpose in their activity.\" There is a bitter look on her face.");
			text.addPara("\"Jack will handle sending the rescue fleet over to get the Operations fleet back home.\"",tc,h,"","");

			text.setFontInsignia();

			dialog.getOptionPanel().addOption("Continue", "nskr_kestevenQuestConfirmQuest");
			noLeave = true;
			//Extra dialog
			dialog.getOptionPanel().addOption("\"I have some questions.\"", DIALOG_OPTION_EXTRA_START_PREFIX);
		}
		//job 4 helped
		if(stage==13 && helped) {
			text.addParagraph("\"Ambushed by an Enigma strike group you say? Worrying, we were hoping their attacks would stay uncoordinated, but there is worrying trend of increased precision and purpose in their activity.\" There is a puzzled look on her face.");
			text.addPara("\"I received a transmission from the Operations fleet that you managed to get them back to running order. Impressive initiative captain, competent people are a valued resource in this sector.\"",tc,h,"","");

			text.setFontInsignia();

			dialog.getOptionPanel().addOption("Continue", "nskr_kestevenQuestConfirmQuest");
			noLeave = true;
			//Extra dialog
			dialog.getOptionPanel().addOption("\"I have some questions.\"", DIALOG_OPTION_EXTRA_START_PREFIX);
		}
		//job 5 go to bar
		if(stage==14) {
			text.addParagraph("\"It's time we told you about what we are actually looking for here. What's the real point of going after this Enigma AI.\" His manners are more commanding than usual, this must be important.");
			text.addPara("\"Head to the bar and give the signal to our man waiting there. You will need to discuss this in person with me and Alice.\" He gestures you to get moving.",tc,h,"Go to the bar","");

			text.setFontInsignia();

			questUtil.setStage(15);

			dialog.getOptionPanel().addOption("Leave", "nskr_kestevenQuestExit");
			noLeave = true;
		}
		//job 5 tip jack
		if(stage==16 && person==jack && !jackTip) {
			boolean helpEliza = questUtil.getCompleted(nskr_elizaDialog.ELIZA_HELP_KEY);
			boolean killEliza = questUtil.getCompleted(questStageManager.KILLED_ELIZA_KEY);
			if (!foundEliza) {
				text.addPara("\"You'll need to find out where Eliza is hiding. You need to go undercover, and start asking questions from local pirates.\"", tc, h, "Eliza", "");
				text.addPara("\"That's your best bet on finding her. I'm sure you can get something out of that scum, if you loosen their lips with some free drinks.\" He gives you a quick nod.", tc, h, "", "");
			}
			if (foundEliza && !helpEliza && !killEliza) {
				text.addPara("\"Seems like you've already figured out where Eliza is hiding. Good, now get the disks from her.\"", tc, h, "", "");
			}
			if (foundEliza && helpEliza) {
				text.addPara("\"Seems like you've already figured out where Eliza is hiding, and got the disks from her. Excellent.\" He seems very pleased.", tc, h, "", "");
			}
			if (foundEliza && !helpEliza && killEliza) {
				text.addPara("\"I hear you managed to already take out Eliza for good, very impressive captain.\" There is a sinister smile on his face, you seem to have made his day.", tc, h, "", "");
			}
			if (aliceTip && diskCount<=2)text.addPara("\"As you know this is not all five disks. We'll get to that in time, just focus on getting the ones we've talked about first.\"",tc,h,"","");
			if (aliceTip && diskCount>2)text.addPara("\"As you know this is not all five disks. Come chat with Alice later about getting the rest too.\"",tc,h,"","");
			if (aliceTip)text.addPara("\"That's all captain.\"",tc,h,"","");
			if (!aliceTip)text.addPara("\"That's all, remember to talk to Alice if you haven't yet.\"",tc,h,"","");
			text.setFontInsignia();

			text.setFontSmallInsignia();
			text.addPara("Updated log entry for the Delve",g,h,"the Delve","");

			Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);
			text.setFontInsignia();

			questUtil.setCompleted(true, JOB5_JACK_TIP_KEY);

			dialog.getOptionPanel().addOption("Leave", "nskr_kestevenQuestExit");
			noLeave = true;
		}
		//job 5 tip alice
		if(stage==16 && person==alice && !aliceTip) {
			int recovered = nskr_artifactDialog.getRecoveredSatelliteCount();
			boolean discovered3 = questUtil.getCompleted(questStageManager.JOB3_TARGET_DISCOVERED);
			SectorEntityToken loc1 = job4TargetLoc;
			SectorEntityToken artifact1 = questUtil.getArtifact(loc1.getStarSystem());
			SectorEntityToken loc2 = questUtil.getJob3Target();
			SectorEntityToken artifact2 = questUtil.getArtifact(loc2.getStarSystem());
			//all recovered
			if (recovered>=2) {
				text.addParagraph("\"Looks like you have both the known satellites covered, I can't help you much more. You should talk to Jack if you haven't already.\" She nods approvingly.");
				text.addPara("\"Efficient work captain.\"", tc, h, "", "");
			}
			//1 not recovered job 3
			if (recovered==1 && questUtil.getCompleted(nskr_artifactDialog.RECOVERED_4_KEY)) {
				text.addParagraph("\"As you know those old comm satellites are the target. \"She pauses to think for a second.\" It must be that Tri-tachyon expedition was going after one, it has to be the one your looking for.\"");

				if(discovered3)text.addPara("\"It was in the "+loc2.getStarSystem().getName()+". Good thing you figured out where they were heading.\"", tc, h, loc2.getStarSystem().getName(), "");
				if(!discovered3)text.addPara("\"Despite your efforts to screw this up, by not finding out where they were heading. " +
						"We managed intercept its target from their comms. It was the "+loc2.getStarSystem().getName()+".\" Its likes she's lecturing a child.", tc, h, loc2.getStarSystem().getName(), "");
				//make important
				artifact2.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MISSION_IMPORTANT,true);
			}
			//1 not recovered job 4
			if (recovered==1 && questUtil.getCompleted(nskr_artifactDialog.RECOVERED_3_KEY)) {
				text.addParagraph("\"As you know those old comm satellites are the target. The one our Special Operations fleet went after is the one your looking for.\"");
				text.addPara("\"It was in the "+loc1.getStarSystem().getName()+".\"", tc, h, loc1.getStarSystem().getName(), "");
				//make important
				artifact1.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MISSION_IMPORTANT,true);
			}
			//default
			if (recovered==0) {
				text.addParagraph("\"Remember what I said about the old comm satellites, those are your target. The one our Special Operations fleet went after should be the easiest one to locate.\"");
				text.addPara("\"It was in the "+loc1.getStarSystem().getName()+".\"", tc, h, loc1.getStarSystem().getName(), "");
				text.addParagraph("\"The other satellite though...\" She pauses to think for a second. \"It must be that, the Tri-Tachyon expedition was going after one.\"");

				if(discovered3)text.addPara("\"It was in the "+loc2.getStarSystem().getName()+". Good thing you figured out where they were heading.\"", tc, h, loc2.getStarSystem().getName(), "");
				if(!discovered3)text.addPara("\"Despite your efforts to screw this up, by not finding out where they were heading. " +
						"We managed intercept its target from their comms. It was the "+loc2.getStarSystem().getName()+".\" Its likes she's lecturing a child.", tc, h, loc2.getStarSystem().getName(), "");
			}
			if (jackTip && diskCount<=2)text.addPara("\"As you know this is not all five disks. We'll get to that in time, just focus on getting the ones we've talked about first.\"",tc,h,"","");
			if (jackTip && diskCount>2)text.addPara("\"As you know this is not all five disks. Come chat with me later about getting the rest too.\"",tc,h,"","");
			if (!jackTip)text.addPara("\"That's all for now captain. You should also talk to Jack if you haven't already.\"", tc, h, "", "");
			text.setFontInsignia();

			text.setFontSmallInsignia();
			text.addPara("Updated log entry for the Delve",g,h,"the Delve","");

			Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);
			text.setFontInsignia();

			questUtil.setCompleted(true, JOB5_ALICE_TIP_KEY);

			dialog.getOptionPanel().addOption("Leave", "nskr_kestevenQuestExit");
			noLeave = true;
		}
		//job 5 tip 2 alice
		if(stage==16 && person==alice && jackTip && aliceTip && !aliceTip2) {
			StarSystemAPI frost = util.getFrost();
			StarSystemAPI tipSystem = questUtil.getJob5FrostTip();
			String constellation = questUtil.parseConstellation(tipSystem.getConstellation().getNameWithType());
			float distLY = Misc.getDistanceLY(tipSystem.getConstellation().getLocation(), frost.getStar().getLocationInHyperspace())*1.5f;
			distLY *= 100f;
			distLY = Math.round(distLY);
			distLY /= 100f;

			text.addParagraph("\"Excellent, you managed to recover the disks from the satellites in one piece. Now for the next task at hand.\"");
			text.addParagraph("\"We have managed to get our hands on a fascinating new lead. Our comms team decrypted a message from the network, relating to a new system of interest.\"");
			text.addPara("\"The transcript talks of a tundra planet in a red dwarf class star system, with a special comms facility. " +
					"Sadly we could not precisely locate, or name the system.\"",tc,h,"tundra planet in a red dwarf class star system","");
			text.addPara("\"Our current knowledge is that it's within "+distLY+" light-years of the "+constellation+". That's all we know for now, you should get to work straight away.\""
					,tc,h,distLY+" light-years", constellation);
			text.addPara("She eyes you up, to make sure that you actually listened to everything she said.");
			text.setFontInsignia();

			text.setFontSmallInsignia();
			text.addPara("Updated log entry for the Delve",g,h,"the Delve","");

			Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);
			text.setFontInsignia();

			questUtil.setCompleted(true, JOB5_ALICE_TIP_KEY2);

			//make important
			for (SectorEntityToken e : frost.getAllEntities()){
				if (e.getId().equals("nskr_glacier")){
					e.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MISSION_IMPORTANT, true);
				}
			}

			//visited frost
			if(frost.isEnteredByPlayer()) {
				text.addPara("A red dwarf system related to Enigma? The "+frost.getName()+", it must be.",g,h,"","");
				dialog.getOptionPanel().addOption("\"It's the " + frost.getName() + ".\"", "nskr_kestevenQuestConfirmQuest");
			}
			dialog.getOptionPanel().addOption("Leave", "nskr_kestevenQuestExit");
			noLeave = true;
		}
		//job5 all disks decrypt alice
		if(stage==16 && person==alice && jackTip && aliceTip && aliceTip2 && allDisks) {
			text.addPara("\"We will start decrypting the disks as soon as we get them unloaded from your cargo. We should have some results in a few hours. Now onto your next task.\"");
			text.addPara("\"This huge breakthrough has me on edge captain, we will finally be uncovering *it* from the Cache site. It being your next objective, the Unlimited Production Chip.\"",tc,h,"Unlimited Production Chip","");
			text.addPara("She gives you a stern look. \"This is as important as it gets, the Chip holds the key to great technologies. Do not screw this up captain.\"");
			text.addPara("\"The fierce loyalty and violence the Enigma is capable of is not to be underestimated. Their hatred of anyone with their new technology is peculiar, it's like the collapse made them think anyone else isn't meant to exist at all. They are helplessly trying to maintain some broken status quo.\" " +
					"She lets out a chuckle. \"Hah, relax, I hope I didn't scare you out of the job captain, I'm sure you are more than capable of dealing with some Enigma spooks at this point.\"");
			text.addPara("She's already busy working on multiple holofeeds while she speaks. \"Now, you will enter the site, disable whatever security systems they have left, and recover the Chip. Understood, captain?\"");
			text.addPara("She's not taking *no* as an answer.",g,h,"","");

			dialog.getOptionPanel().addOption("\"Yes\"", "nskr_kestevenQuestConfirmQuest");
			if(questUtil.getCompleted(nskr_elizaDialog.AGREED_TO_HELP_KEY))dialog.getOptionPanel().addOption("\"Yes\" (lie)", "nskr_kestevenQuestConfirmQuest"+"B");
			noLeave = true;
		}

		if (!noLeave) dialog.getOptionPanel().addOption("Back", "nskr_kestevenQuestExit");
	}
	protected void extraDialogueStart()
	{
		dialog.getOptionPanel().clearOptions();
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		String desc = "";
		String str = "";

		if (person==jack) {
			//job 1 start jack
			if (stage == 0) {
				if (questUtil.getJob1Tip()!=null) dialog.getOptionPanel().addOption("\"How am I supposed to find them?\"", DIALOG_OPTION_EXTRA_PREFIX+"0");
				dialog.getOptionPanel().addOption("\"Rogue AI?\"", DIALOG_OPTION_EXTRA_PREFIX+"1");
				if (!foughtEnigma) dialog.getOptionPanel().addOption("\"The ships?\"", DIALOG_OPTION_EXTRA_PREFIX+"2");
				dialog.getOptionPanel().addOption("\"Artifact Electronics?\"", DIALOG_OPTION_EXTRA_PREFIX+"3");
				dialog.getOptionPanel().addOption("\"What about the AI Cores?\"", DIALOG_OPTION_EXTRA_PREFIX+"4");
				if (foughtEnigma) dialog.getOptionPanel().addOption("\"I've already fought them.\"", DIALOG_OPTION_EXTRA_PREFIX+"5");
			}
			//job 1 complete jack
			if (stage == 2) {
				dialog.getOptionPanel().addOption("\"What are you actually doing with this equipment?\"", DIALOG_OPTION_EXTRA_PREFIX+"0");
				dialog.getOptionPanel().addOption("\"Enigma AI?\"", DIALOG_OPTION_EXTRA_PREFIX+"1");
				dialog.getOptionPanel().addOption("\"Next job?\"", DIALOG_OPTION_EXTRA_PREFIX + "2");
			}
		}

		if (person==alice) {
			//job 3 start alice
			if (stage == 7) {
				dialog.getOptionPanel().addOption("\"How do I know where to go?\"", DIALOG_OPTION_EXTRA_PREFIX+"0");
				dialog.getOptionPanel().addOption("\"So I'm on my own for this?\"", DIALOG_OPTION_EXTRA_PREFIX+"1");
				dialog.getOptionPanel().addOption("\"Is this really necessary?\"", DIALOG_OPTION_EXTRA_PREFIX+"2");
				dialog.getOptionPanel().addOption("\"I'm not doing this.\"", DIALOG_OPTION_EXTRA_PREFIX+"3");
			}
			//job 3 complete alice
			if (stage == 10) {
				if (questUtil.getCompleted(questStageManager.JOB3_TARGET_DISCOVERED))dialog.getOptionPanel().addOption("\"Know anything about the target?\" (Send over the coordinates)", DIALOG_OPTION_EXTRA_PREFIX + "0");
				dialog.getOptionPanel().addOption("\"Next job?\"", DIALOG_OPTION_EXTRA_PREFIX + "1");
			}
			//job 4 start alice
			if (stage == 11) {
				dialog.getOptionPanel().addOption("\"Special Operations fleet?\"", DIALOG_OPTION_EXTRA_PREFIX+"0");
				dialog.getOptionPanel().addOption("\"Possible threats?\"", DIALOG_OPTION_EXTRA_PREFIX+"1");
				dialog.getOptionPanel().addOption("\"Supplies and fuel?\"", DIALOG_OPTION_EXTRA_PREFIX+"2");
				dialog.getOptionPanel().addOption("\"Nicholas Antoine?\"", DIALOG_OPTION_EXTRA_PREFIX+"3");
				if (eMessenger) dialog.getOptionPanel().addOption("Ask about the \"LZ\" character", DIALOG_OPTION_EXTRA_PREFIX+"4");
			}
			//job 4 complete alice
			if (stage == 13) {
				dialog.getOptionPanel().addOption("\"What was the Operations fleet's goal?\"", DIALOG_OPTION_EXTRA_PREFIX+"0");
				dialog.getOptionPanel().addOption("\"Why are you so interested in this Enigma AI?\"", DIALOG_OPTION_EXTRA_PREFIX + "1");
				dialog.getOptionPanel().addOption("\"The Artifact?\"", DIALOG_OPTION_EXTRA_PREFIX+"2");
				if (eMessenger) dialog.getOptionPanel().addOption("Ask about the \"LZ\" character", DIALOG_OPTION_EXTRA_PREFIX+"3");
			}
		}

		if (str.length()>0) {
			text.addPara(str);
		}
		text.setFontSmallInsignia();
		text.setFontInsignia();

		dialog.getOptionPanel().addOption("Back", DIALOG_OPTION_PREFIX);
	}

	protected void extraDialogue(int index)
	{
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		String hl = "";
		String str = "";

		if (person==jack) {
			//job 1 start jack
			if (stage == 0) {
				if (index==0){
					if (questUtil.getJob1Tip()!=null) {
						StarSystemAPI loc = questUtil.getJob1Tip();
						str = "\"Our best lead is the " + loc.getName() + ". You should start from there.\"";
						hl = loc.getName();
						questUtil.setCompleted(true, questStageManager.JOB1_TIP_KEY);
						dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX + "0", false);
					}
				}
				if (index==1){
					str = "\"Basically all the reports claim that automated ships of some sort are behind these attacks. Usually deploying a collection of advanced technologies never before seen the sector.\" " +
							"He looks particularly skeptical.";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"1", false);
				}
				if (index==2){
					str = "\"The unmatched capabilities of these rumored ships is truly a marvel of engineering, although it's not even clear who or what is behind this technology.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"2", false);
				}
				if (index==3){
					str = "\"The ships are said to have a peculiar construction that no other ship has in the sector. Said to be real works of art...\"" +
							" A sly smile appears on his face. \"Well, when they're not trying to blow you into pieces.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"3", false);
				}
				if (index==4){
					str = "\"We are required to turn them over to the Hegemony as participants of the AI war treaties.\" There's a hint of disappointment in his words. " +
							"\"But thankfully everything else recovered is free game for us.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"4", false);
				}
				if (index==5){
					str = "\"Damn impressive captain, well if what you say is true that is.\" " +
							"He pauses then checks your composure to see if you're telling the truth. He seems to like the result and continues. \"You should have no problem completing the mission then.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"5", false);
				}
			}
			//job 1 complete jack
			if (stage == 2) {
				if (index == 0) {
					str = "\"That's classified information captain. I'm sure you'll understand.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"0", false);
				}
				if (index == 1) {
					str = "\"Enigma is one of the few words we can decipher from their transmissions. It is unclear what it actually means though.\" He pauses for a moment."+
							"\"It could be some sort of master AI core, or the name of their base of operations, or \" " +
							"He pauses again to think. \"It's just the codename of the unholy project that caused this mess.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"1", false);
				}
				if (index==2){
					str = "He nods in approval. \"We do have more work for you, come talk to me later.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"2", false);
				}
			}
		}
		if (person==alice) {
			//job 3 start alice
			if (stage == 7) {
				if (index==0){
					SectorEntityToken loc = questUtil.getJob3Start();
					str = "\"I'd suggest starting at "+loc.getName()+". You'll figure out the rest captain, you have serious talent when it comes to this type of work, or so I hear.\"";
					hl = loc.getName();
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"0", false);
				}
				if (index==1){
					str = "\"This is a covert operation. If someone asks you don't know me, and so on.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"1", false);
				}
				if (index==2){
					str = "\"Yes, we can't risk Tri-Tachyon interfering in our business. Between you and me, I know they're somehow behind this Enigma activity.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"2", false);
				}
				//job3 skip dialog
				if (index==3){
				}
			}
			//job 3 finish alice
			if (stage == 10) {
				if (index == 0) {
					str = "You send the coordinates you acquired over to her. \"Interesting...\" She makes a few quick searches on her datapad. \"The system has been marked as a possible Enigma site in our database. " +
							"Those sly bastards were definitely up to something.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"0", false);
				}
				if (index == 1) {
					str = "\"I don't have any urgent work at the moment. But in a month or so that might change.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"1", false);
				}
			}
			//job 4 start alice
			if (stage == 11) {
				if (index==0){
					str = "\"We sent a fleet with some special equipment to try and track down a certain Enigma lead, seems like something has gone wrong though.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"0", false);
				}
				if (index==1){
					str = "\"Could be an act of counter-sabotage from \" Her expression turns sour. \"Tri-Tachyon, or worse a direct Enigma attack, we can't know yet.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"1", false);
				}
				if (index==2){
					str = "\"They might have taken heavy losses and be in need of assistance. It never hurts to be prepared.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"2", false);
				}
				if (index==3){
					str = "\"He works in communications, very skilled in the field. There must something useful he knows.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"3", false);
				}
				if (index==4){
					str = "\"Ah yes, of course...\" Her eyes narrow to show disdain. \"No one important, we will deal with this \"LZ\" in time.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"4", false);
					//don't show up again
					questUtil.setCompleted(false, questStageManager.E_MESSENGER_TALKED_ASK_ABOUT_KEY);
				}
			}
			//job 4 complete alice
			if (stage == 13) {
				if (index == 0) {
					str = "\"After learning the location of a possible Enigma artifact, we sent the Operations fleet over to investigate.\" She seems vaguely regretful. \"It's clear we should have been more prepared though.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"0", false);
				}
				if (index == 1) {
					str = "Her tone turns unusually passionate. \"You do see how advanced this \"Enigma\" technology is? With even a fraction of this power unlocked, one could have total supremacy over this sector. We should not just idly wait for other factions to take the lead in this race.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"1", false);
				}
				if (index == 2) {
					str = "\"These \"Artifacts\" aren't physical objects per say, more like collections of valuable data. Hidden in old comm satellite networks, and such. This data is invaluable for anyone trying to understand what this \"Enigma\" truly is, and where it comes from.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"2", false);
				}
				if (index==3){
					str = "\"Ah yes, of course...\" Her eyes narrow to show disdain. \"No one important, we will deal with this \"LZ\" in time.\"";
					dialog.getOptionPanel().setEnabled(DIALOG_OPTION_EXTRA_PREFIX+"3", false);
					//don't show up again
					questUtil.setCompleted(false, questStageManager.E_MESSENGER_TALKED_ASK_ABOUT_KEY);
				}
			}
		}

		if (str.length()>0) {
			text.addPara(str,tc,h,hl,"");
		}
		text.setFontSmallInsignia();
		text.setFontInsignia();
	}

	protected void skip() {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		String desc = "";
		String str = "";

		if (person==alice) {
			//job 3 skip alice
			if (stage == 7) {
				text.addPara("\"Too bad. The offer stands if you change your mind.\"");
				text.addPara("\"Unless you are being serious about this.\" She gives you a very mean look.", tc, h, "", "");

				dialog.getOptionPanel().addOption("\"yes\"", "nskr_kestevenQuestConfirmSkip");
				dialog.getOptionPanel().addOption("\"No\"", DIALOG_OPTION_PREFIX);
			}
		}

		text.setFontSmallInsignia();
		text.setFontInsignia();
	}

	protected void confirmSkip() {
		text.setFontSmallInsignia();
		String str = "";
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color gr = Misc.getPositiveHighlightColor();
		Color r = Misc.getNegativeHighlightColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;

		//skip job3
		if(stage == 7) {
			text.setFontInsignia();
			text.addPara("\"Damn, I really wanted to avoid using our own black ops for this...\"");
			text.addPara("\"I am very disappointed in you captain.\" She looks eager to cut the comm link on you.");
			text.setFontSmallInsignia();
			Global.getSector().getFaction(Factions.PLAYER).adjustRelationship("kesteven",-0.05f);
			util.getAlice().getRelToPlayer().adjustRelationship(-0.10f, RepLevel.VENGEFUL);
			//penalty text
			text.addPara("Relationship with Kesteven reduced by 5",g,r,"5","");
			text.addPara("Relationship with Alice Lumi reduced by 10",g,r,"10","");

			Global.getSoundPlayer().playUISound("ui_rep_drop",1f,1f);

			questUtil.setStage(11);
			questUtil.setFailed(true, questStageManager.JOB3_SKIP_KEY);

			SectorEntityToken loc = questUtil.getJob3Target();
			spawnEnvironmentalStorytelling();
			questUtil.spawnArtifact(loc,3);
			util.addDormant(loc, "enigma", 45f, 50f, 0f, 1f, 1f, 1f, 1, 1);
		}
		text.setFontInsignia();
	}

	public static void spawnEnvironmentalStorytelling(){
		SectorEntityToken loc = questUtil.getJob3Target();

		nskr_frost.addDerelict(loc.getStarSystem(), "doom_Strike", util.createRandomNearOrbit(loc), ShipRecoverySpecial.ShipCondition.BATTERED, Math.random()<0.50f, null);
		nskr_frost.addDerelict(loc.getStarSystem(), "atlas_Standard", util.createRandomNearOrbit(loc), ShipRecoverySpecial.ShipCondition.BATTERED, Math.random()<0.50f, null);
		nskr_frost.addDerelict(loc.getStarSystem(), "shrike_Attack", util.createRandomNearOrbit(loc), ShipRecoverySpecial.ShipCondition.BATTERED, Math.random()<0.50f, null);

		DebrisFieldTerrainPlugin.DebrisFieldParams params_loc_main = new DebrisFieldTerrainPlugin.DebrisFieldParams(
				350f, // field radius - should not go above 1000 for performance reasons
				1.2f, // density, visual - affects number of debris pieces
				10000000f, // duration in days
				0f); // days the field will keep generating glowing pieces
		params_loc_main.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
		params_loc_main.baseSalvageXP = 500; // base XP for scavenging in field
		SectorEntityToken frost_main1 = Misc.addDebrisField(loc.getStarSystem(), params_loc_main, StarSystemGenerator.random);
		frost_main1.setSensorProfile(1000f);
		frost_main1.setDiscoverable(true);
		frost_main1.setOrbit(util.createRandomNearOrbit(loc));
		frost_main1.setId("nskr_loc_main_debrisBelt");
	}

	protected void quest(){
		text.setFontSmallInsignia();
		String str = "";
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color gr = Misc.getPositiveHighlightColor();
		Color r = Misc.getNegativeHighlightColor();
		Color tc = Misc.getTextColor();
		Color s = Misc.getStoryBrightColor();
		float pad = 3f;
		float opad = 10f;

		//start job 1
		if(stage == 0) {
			text.setFontSmallInsignia();
			text.addPara("Acquired log entry for Enemy Unknown",g,h,"Enemy Unknown","");

			str = "\"Good luck. I await your return.\" He starts marking down stuff on his holopad as the connection is cut.";

			Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);

			questUtil.setStage(1);
		}
		//finish job 1
		if(stage == 2) {
			text.setFontInsignia();
			text.addPara("\"Nice work captain. I hope we can continue developing this relationship further in the future.\"");
			text.addPara("Officer Lapua gives you a look like he isn't supposed to do this. \"I also threw in a little bonus for a job well done. It's a new modspec courtesy of our own R&D division.\"");
			text.setFontSmallInsignia();
			HullModSpecAPI modspec = getRewardMod();
			String mod = modspec.getId();
			String name = modspec.getDisplayName();

			playerCargo.addHullmods(mod,1);
			playerCargo.getCredits().add(STAGE1_PAYOUT);
			Global.getSector().getFaction(Factions.PLAYER).adjustRelationship("kesteven",0.05f);
			util.getJack().getRelToPlayer().adjustRelationship(0.10f, RepLevel.COOPERATIVE);
			//completion text
			String payout = Misc.getDGSCredits(STAGE1_PAYOUT);
			String desc = "Received +" + payout;
			text.addPara(desc,g,h,"+"+payout,"");
			text.addPara("Relationship with Kesteven improved by 5",g,gr,"5","");
			text.addPara("Relationship with Jack Lapua improved by 10",g,gr,"10","");
			text.addPara("Acquired "+name+" modspec",g,h, name,"");

			Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

			questUtil.setStage(6);
		}
		//go talk to Alice job 3
		if(stage == 6) {
			str = "\"Go talk to Alice now.\"";
			questUtil.setStage(7);
			//CONTACT JACK
			ContactIntel.addPotentialContact(1f,person, dialog.getInteractionTarget().getMarket(), text);
		}
		//start job 3
		if(stage == 7) {
			text.setFontSmallInsignia();
			text.addPara("Acquired log entry for Hostile Takeover",g,h,"Hostile Takeover","");

			Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);

			str = "\"Go get to work now.\" The comm link is swiftly cut.";
			questUtil.setStage(8);
		}
		//finish job 3 success
		if(stage == 10 && !failedJob3) {
			text.setFontInsignia();
			text.addPara("\"I'm giving you some exchange points as a bonus. Don't forget to spend them.\"");
			text.addPara("\"Oh, and there's a new modspec for you to test, go give it a spin.\"");
			text.setFontSmallInsignia();
			HullModSpecAPI modspec = getRewardMod();
			String mod = modspec.getId();
			String name = modspec.getDisplayName();

			playerCargo.addHullmods(mod,1);
			nskr_shipSwap.addPoints(50000f);
			playerCargo.getCredits().add(STAGE3_PAYOUT);
			Global.getSector().getFaction(Factions.PLAYER).adjustRelationship("kesteven",0.05f);
			util.getAlice().getRelToPlayer().adjustRelationship(0.10f, RepLevel.COOPERATIVE);
			//completion text
			String payout = Misc.getDGSCredits(STAGE3_PAYOUT);
			String desc = "Received +" + payout;
			text.addPara(desc,g,h,"+"+payout,"");
			text.addPara("Relationship with Kesteven improved by 5",g,gr,"5","");
			text.addPara("Relationship with Alice Lumi improved by 10",g,gr,"10","");
			text.addPara("Acquired 50,000 exchange points",g,h,"50,000 exchange points","");
			text.addPara("Acquired "+name+" modspec",g,h, name,"");

			Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

			questUtil.setStage(11);
		}
		//finish job 3 fail
		if(stage == 10 && failedJob3) {
			text.setFontInsignia();
			text.addPara("\"Just be glad I'm not firing you on the spot.\"");
			text.addPara("Okay, shes *really* frustrated with you.",g,h,"","");
			text.setFontSmallInsignia();
			Global.getSector().getFaction(Factions.PLAYER).adjustRelationship("kesteven",-0.05f);
			util.getAlice().getRelToPlayer().adjustRelationship(-0.10f, RepLevel.VENGEFUL);
			//completion text
			text.addPara("Relationship with Kesteven reduced by 5",g,r,"5","");
			text.addPara("Relationship with Alice Lumi reduced by 10",g,r,"10","");

			Global.getSoundPlayer().playUISound("ui_rep_drop",1f,1f);

			questUtil.setStage(11);
		}
		//start job 4
		if(stage == 11) {
			text.setFontSmallInsignia();
			text.addPara("Acquired log entry for Operation Lifesaver",g,h,"Operation Lifesaver","");

			Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);

			str = "\"Lets hope for the best, captain.\" She nods before cutting the comm link.";
			questUtil.setStage(12);
		}
		//finish job 4
		if(stage == 13 && !helped) {
			text.setFontInsignia();
			text.addPara("\"Here's the payment, as promised. A whole new modspec too for you to try, at least try to act grateful.\" She smirks, it seems like she's joking, but it's hard to tell.");
			text.addPara("\"Oh- by the way come talk to me later if you want to readjust some of your ships back to standard specs.\"");
			text.setFontSmallInsignia();
			//add sp
			Global.getSector().getPlayerStats().setStoryPoints(Global.getSector().getPlayerStats().getStoryPoints()+1);
			text.setFontSmallInsignia();
			text.addPara("Gained 1 Story point",g,s,"1 Story point","");
			playerCargo.getCredits().add(STAGE4_PAYOUT);
			Global.getSector().getFaction(Factions.PLAYER).adjustRelationship("kesteven",0.05f);
			util.getAlice().getRelToPlayer().adjustRelationship(0.10f, RepLevel.COOPERATIVE);
			//completion text
			HullModSpecAPI modspec = getRewardMod();
			String mod = modspec.getId();
			String name = modspec.getDisplayName();

			playerCargo.addHullmods(mod,1);
			String payout = Misc.getDGSCredits(STAGE4_PAYOUT);
			String desc = "Received +" + payout;
			text.addPara(desc,g,h,"+"+payout,"");
			text.addPara("Relationship with Kesteven improved by 5",g,gr,"5","");
			text.addPara("Relationship with Alice Lumi improved by 10",g,gr,"10","");
			text.addPara("Acquired "+name+" modspec",g,h, name,"");

			Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

			questUtil.setStage(14);
			//CONTACT ALICE
			ContactIntel.addPotentialContact(1f,person, dialog.getInteractionTarget().getMarket(), text);
			//CONTACT lvl increase
			text.addPara("Increased contact level with Kesteven contacts",g,gr,"","");
			util.getJack().setImportance(PersonImportance.HIGH);
		}
		//finish job 4 helped
		if(stage == 13 && helped) {
			text.setFontInsignia();
			text.addPara("\"You are receiving one of the prototype artifacts we managed to recover as a bonus. Treat it well, these are one of a kind.\"");
			text.addPara("\"Also, there's the payment, as promised. And a whole new modspec too for you to try, at least try to act grateful.\" She smirks, it seems like she's joking, but it's hard to tell.");
			text.addPara("\"Oh- by the way come talk to me later if you want to readjust some of your ships back to standard specs.\"");
			text.setFontSmallInsignia();
			//add sp
			Global.getSector().getPlayerStats().setStoryPoints(Global.getSector().getPlayerStats().getStoryPoints()+1);
			text.setFontSmallInsignia();
			text.addPara("Gained 1 Story point",g,s,"1 Story point","");
			HullModSpecAPI modspec = getRewardMod();
			String mod = modspec.getId();
			String name = modspec.getDisplayName();

			playerCargo.addHullmods(mod,1);
			playerFleet.getFleetData().addFleetMember("nskr_epoch_empty");
			playerCargo.getCredits().add(STAGE4_PAYOUT);
			Global.getSector().getFaction(Factions.PLAYER).adjustRelationship("kesteven",0.05f);
			util.getAlice().getRelToPlayer().adjustRelationship(0.10f, RepLevel.COOPERATIVE);
			//completion text
			String payout = Misc.getDGSCredits(STAGE4_PAYOUT);
			String desc = "Received +" + payout;
			text.addPara(desc,g,h,"+"+payout,"");
			text.addPara("Relationship with Kesteven improved by 5",g,gr,"5","");
			text.addPara("Relationship with Alice Lumi improved by 10",g,gr,"10","");
			text.addPara("Acquired Epoch-class prototype frigate",g,h,"Epoch-class","");
			text.addPara("Acquired "+name+" modspec",g,h, name,"");

			Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

			questUtil.setStage(14);
			//CONTACT ALICE
			ContactIntel.addPotentialContact(1f,person, dialog.getInteractionTarget().getMarket(), text);
			//CONTACT lvl increase
			text.addPara("Increased contact level with Kesteven contacts",g,gr,"","");
			util.getJack().setImportance(PersonImportance.HIGH);
		}
		//job 5 alice tip 2 know frost system
		if(stage == 16 && jackTip && aliceTip && !allDisks) {
			text.setFontInsignia();
			text.addPara("Alice pauses for a moment to think about what you said, and then proceeds to look up something on her datapad.");
			text.addPara("\"I think you are right, impressive. Now find the tundra planet in the "+ util.getFrost().getName()+" ASAP.\"",tc,h, util.getFrost().getName(),"");

			Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

			text.setFontSmallInsignia();
			text.addPara("Updated log entry for the Delve",g,h,"the Delve","");

			Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);
			text.setFontInsignia();

			questUtil.setCompleted(true, questStageManager.JOB5_FOUND_FROST_KEY);
		}
		//job 5 alice decryption pt2
		if(stage == 16 && allDisks) {
			text.setFontInsignia();
			text.addPara("Alice leaves to begin work on the decryption. A few hours pass as you wait in the lobby, you try to pass the time by scrolling on your TriPad. You regret not going to the local bar to pass the time instead.");
			text.addPara("Finally Alice arrives back. \"We did it "+player.getName().getFirst()+", we have the exact location of the Cache. I've handed the hyperspace coordinates over to your nav officer.\"");
			text.addPara("\"Oh by the way, you can not access the site by normal means. I hope you know how to perform a transverse jump maneuver, if not maybe someone at Galatia Academy can help you. Now get to work.\"");

			text.setFontSmallInsignia();
			text.addPara("Acquired coordinates to the Cache Site",g,h,"Cache Site","");

			Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

			questUtil.setCompleted(true, questStageManager.FOUND_CACHE_KEY);
			questUtil.setStage(17);
		}

		text.setFontInsignia();
		if (str.length()>0) {
			text.addPara(str);
		}
	}

	public static final ArrayList<String> MODS = new ArrayList<>();
	static {
		MODS.add(ids.INERTIAL_SUPERCHARGER_HULLMOD_ID);
		MODS.add(ids.VOLATILE_FLUX_INJECTOR_HULLMOD_ID);
		MODS.add(ids.HIGH_CAPACITANCE_BANKS_HULLMOD_ID);
		MODS.add(ids.CRITICAL_POINT_PROTECTION_HULLMOD_ID);
	}

	private HullModSpecAPI getRewardMod() {

		ArrayList<String> tempMods = new ArrayList<>(MODS);
		//try to give a new one
		for (String known : Global.getSector().getPlayerFaction().getKnownHullMods()){
			tempMods.remove(known);
		}
		//new one check
		if (!tempMods.isEmpty()){
			return Global.getSettings().getHullModSpec(tempMods.get(mathUtil.getSeededRandomNumberInRange(0,tempMods.size()-1, getRandom())));
		} else {
			return Global.getSettings().getHullModSpec(MODS.get(mathUtil.getSeededRandomNumberInRange(0,MODS.size()-1, getRandom())));
		}
	}

	//
	public static boolean validMarket(MarketAPI market)
	{
		if (market==null) return false;
		if (relation<=-0.5f) return false;
		if (questUtil.getEndMissions()) return false;

		return market.getFaction().getId().equals("kesteven");
	}

	public static Random getRandom() {
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

			data.put(PERSISTENT_RANDOM_KEY, new Random(util.getSeedParsed()));
		}
		return (Random)data.get(PERSISTENT_RANDOM_KEY);
	}

}

