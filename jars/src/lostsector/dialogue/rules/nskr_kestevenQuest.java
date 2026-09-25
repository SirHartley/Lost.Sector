package lostsector.dialogue.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import lostsector.campaign.kesteven.quest.DataSatelliteDialog;
import lostsector.campaign.kesteven.quest.KestevenFleets;
import lostsector.campaign.kesteven.quest.QuestStageManager;
import lostsector.campaign.kesteven.quest.QuestHelper;
import lostsector.campaign.kesteven.quest.KestevenQuest;
import lostsector.campaign.kesteven.quest.KestevenState;
import lostsector.ModPlugin;
import lostsector.helper.Ids;
import lostsector.helper.MathHelper;
import lostsector.campaign.enigma.DormantSpawner;
import lostsector.campaign.kesteven.quest.KestevenFlag;
import lostsector.campaign.kesteven.quest.KestevenPeople;
import lostsector.helper.SectorLookup;
import lostsector.helper.SystemHelper;
import lostsector.helper.PowerLevel;
import lostsector.world.SectorGen;
import lostsector.world.systems.frost.Frost;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class nskr_kestevenQuest extends PaginatedOptions {

	//handles dialogue and rules.csv for the quest line
	// Adapted from Nexerelin.
	public static final String DIALOG_OPTION_PREFIX = "nskr_kestevenQuest_pick_";
	public static final String DIALOG_OPTION_EXTRA_START_PREFIX = "nskr_kestevenQuest_extraStart_";

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
	// PowerLevel scans the whole player fleet; only the job 3 and job 4 briefings read it.
	private Float power = null;
	private SectorEntityToken job4TargetLoc = null;
	private boolean job1tip = false;
	private boolean failedJob3 = false;
	private boolean helped = false;
	// When set, ESC also selects the DIALOG_OPTION_PREFIX option.
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

	private PersonAPI jack;
	private PersonAPI alice;

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
	//3 unused
	//4 unused
	//5 unused
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
			case "advanceStage":
				showOptions();
				showQuestInfoAndPrepare(dialog.getTextPanel());
				extraEsc = false;
				break;
			case "advanceStageStorySkip":
				showOptions();
				SkipStoryOptionPicked();
				extraEsc = false;
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

		jack = KestevenPeople.getJack();
		alice = KestevenPeople.getAlice();

		power = null;
		stage = QuestHelper.getStage();
		job1tip = QuestHelper.getCompleted(KestevenFlag.JOB1_TIP_GIVEN);
		failedJob3 = QuestHelper.getFailed(KestevenFlag.JOB3_FAILED);
		job4TargetLoc = QuestHelper.getJob4EnemyTarget();
		helped = QuestHelper.getCompleted(KestevenFlag.JOB4_FRIENDLY_HELPED);

		cargo = playerCargo.getCommodityQuantity("nskr_electronics") >= JOB1_ARTIFACTS;
		delivered = QuestHelper.getCompleted(KestevenFlag.JOB1_ELECTRONICS_DELIVERED);
		deliveredData = QuestHelper.getCompleted(KestevenFlag.JOB1_DATA_DELIVERED);
		sensored = QuestHelper.getCompleted(KestevenFlag.JOB1_SENSOR_DATA);

		aliceTip = QuestHelper.getCompleted(KestevenFlag.JOB5_ALICE_TIP);
		aliceTip2 = QuestHelper.getCompleted(KestevenFlag.JOB5_ALICE_TIP2);
		jackTip = QuestHelper.getCompleted(KestevenFlag.JOB5_JACK_TIP);

		foundEliza = QuestHelper.getCompleted(KestevenFlag.ELIZA_FOUND);

		diskCount = QuestHelper.getDisksRecovered();
		allDisks = QuestHelper.getDisksRecovered()>=5;
	}
	
	private float getPower() {
		if (power == null) power = fleetPower();
		return power;
	}

	// The strength compared with the JOB*_POWER gates, here and in KestevenHubModule.
	public static float fleetPower() {
		return Global.getSettings().isDevMode() ? 2f : PowerLevel.get(0.2f, 0f, 2f);
	}

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

	protected void SkipStoryOptionPicked(){
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		Color r = Misc.getNegativeHighlightColor();

		text.setFontInsignia();

		Global.getSoundPlayer().playUISound("ui_char_spent_story_point",1f,1f);

		if (person==jack) {
			text.addParagraph("He looks almost flustered. \"What- How did you...\" He thinks for a moment.");
			text.addParagraph("\"You know we got this strange lead recently pointing to these coordinates, I think you should investigate.\" " +
					"He looks a little worried. \"Huh, I actually don't remember how we got these...\"");
		}
		if (person==alice){
			text.addParagraph("She looks confused for a moment. \"What- How did you...\" She thinks for a moment.");
			text.addParagraph("\"You know we got this strange lead recently pointing to these coordinates, I think you should investigate.\" " +
					"She looks distressed. \"Huh... I actually don't remember how we got these...\"");
		}

		//job 3 objects already exist from stage 8, or after refusing job 3 (stage 11)
		if (stage <= 7) {
			QuestHelper.spawnArtifact(QuestHelper.getJob3Target(), 3);
			DormantSpawner.addDormant(QuestHelper.getJob3Target(), "enigma", 45f, 50f, 0f, 1f, 1f, 1f, 1, 1);
		}
		//job 4 objects already exist from stage 12
		if (stage <= 11) {
			KestevenFleets.spawnJob4Target();
			QuestHelper.spawnArtifact(QuestHelper.getJob4EnemyTarget(), 4);
			QuestStageManager.spawnJob4Wrecks(nskr_kestevenQuest.getRandom());
		}
		//job5
		QuestHelper.setCompleted(true, KestevenFlag.SATELLITE4_RECOVERED);
		QuestHelper.setCompleted(true, KestevenFlag.SATELLITE3_RECOVERED);
		DataSatelliteDialog.setRecoveredSatelliteCount(2);
		QuestHelper.setCompleted(true, KestevenFlag.FROST_FOUND);
		QuestHelper.setCompleted(true, KestevenFlag.GLACIER_DISK_RECOVERED);
		QuestHelper.setCompleted(true, KestevenFlag.JOB5_ALICE_TIP);
		QuestHelper.setCompleted(true, KestevenFlag.JOB5_ALICE_TIP2);
		QuestHelper.setCompleted(true, KestevenFlag.JOB5_JACK_TIP);
		QuestHelper.setCompleted(true, KestevenFlag.ELIZA_FOUND);
		QuestHelper.setCompleted(true, KestevenFlag.ELIZA_DIALOG_FINISHED);
		QuestHelper.setCompleted(true, KestevenFlag.ELIZA_HELPED);
		if(QuestHelper.getElizaLoc()==null) {
			QuestHelper.setElizaLoc();
			PersonAPI eliza = SectorGen.genEliza();
			QuestHelper.getElizaLoc().getMarket().getCommDirectory().addPerson(eliza, 1);
			QuestHelper.getElizaLoc().getMarket().addPerson(eliza);
			log("Eliza loc " + QuestHelper.getElizaLoc().getMarket().getName());
		}

		QuestHelper.setCompleted(true, KestevenFlag.CACHE_FOUND);
		QuestHelper.setStage(17);

		//ineligible for hard mode completion
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (data.containsKey(ModPlugin.STARFARER_MODE_FROM_START_KEY)) {
			data.put(ModPlugin.STARFARER_MODE_FROM_START_KEY, false);
		}
		QuestHelper.setCompleted(true, KestevenFlag.STORY_SKIPPED);


		text.setFontSmallInsignia();
		text.addPara("Added log entry for the Delve", g, h,"the Delve","");
		text.addPara("Disabled questline achievements", g, r,"Disabled","");

		Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);
		text.setFontInsignia();

		// Adding the Leave option from rules.csv does not work for this dialog, so it is added here.
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

			QuestHelper.setCompleted(true, KestevenFlag.JOB1_DATA_DELIVERED);
			QuestHelper.setCompleted(true, KestevenFlag.JOB1_ELECTRONICS_DELIVERED);

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

			QuestHelper.setCompleted(true, KestevenFlag.JOB1_ELECTRONICS_DELIVERED);
		} else if (stage == 1 && sensored && !deliveredData){
			//complete task1
			text.addParagraph("\"We've been waiting to see some concrete data on this subject. Nice work captain.\" He makes a vaguely congratulatory gesture.");

			text.setFontSmallInsignia();
			text.addPara("Lost sensor package", g, r, "", "");

			Global.getSoundPlayer().playUISound("ui_rep_raise", 1f, 1f);
			text.setFontInsignia();

			QuestHelper.setCompleted(true, KestevenFlag.JOB1_DATA_DELIVERED);
		} else if (stage == 1 && !deliveredData && !delivered && !job1tip && QuestHelper.getJob1Tip()!=null) {
			//tip
			StarSystemAPI loc = QuestHelper.getJob1Tip();
			String str = "\"Our best lead is the " + loc.getName() + ". You should start from there.\"";
			String hl = loc.getName();
			text.addPara(str,tc,h,hl,"");
			text.addPara("\"That should give you something to work on captain.\" He nods and shortly cuts the comm link.");

			QuestHelper.setCompleted(true, KestevenFlag.JOB1_TIP_GIVEN);
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

			SectorEntityToken location = QuestHelper.getJob3Start();
			String loc = location.getMarket().getName();
			text.addPara("\"Our intelligence has tracked down a Tri-Tachyon expedition on Enigma activity, said to leave from "+loc+". We need you to figure out their destination and make sure that they don't make it back. Harsh I know, but we can't risk those fools at Tri-Tachyon getting ahead in this field.\"",tc,h,loc,"");
			text.addPara("\"I don't care how you do it, but the fleet must neutralized stealthily. This is sabotage, not a declaration of war.\"",tc,h,"neutralized stealthily","");

			text.addPara("She pauses, as if to check that you're still listening.");
			String payout = Misc.getDGSCredits(STAGE3_PAYOUT);
			desc = "\"Of course, my reward will be " + payout + ". Fair warning this mission is time sensitive, you have around 90 days until the fleet has completed its task, and we will have missed our mark. So prepare accordingly before starting this job.\"";
			text.addPara(desc,tc,h,payout,"this mission is time sensitive, you have around 90 days");

			if(getPower()<JOB3_POWER+0.15f){
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
			Constellation constellation = QuestHelper.getJob4FriendlyTarget().getConstellation();

			text.addParagraph("\"Our Special Operations fleet has gone silent for a worrying amount of time.\" There is a hint of genuine worry on her face. \"We need you to go and find them, and then figure out what is going on.\"" +
					" She begins to mumble to herself while looking through various data projections. \"I hope they didn't lose *that* equipment. It would be a serious blow-back...\"");
			text.addParagraph("She seems to finally have the right file open. \"As you know, it's very likely that something unusual has happened so prepare for the worst. Might be a good idea to grab some extra supplies and fuel in case they need emergency assistance.\"");

			String payout = Misc.getDGSCredits(STAGE4_PAYOUT);
			desc = "\"Their task was to analyze suspected Enigma activity in "+ constellation.getName()+" constellation" +". Your job is to locate them, establish contact, and eliminate any existing threats in the area. Pay for the job is " + payout + "\"";
			text.addPara(desc,tc,h,constellation.getName()+" constellation" ,payout);

			if (QuestHelper.outpostExists())text.addPara("\"Oh, by the way, you should talk to Nicholas Antoine. He works in communications and is currently stationed at "+ SectorLookup.getOutpost().getName()+". He most likely has some more information.\"",tc,h, SectorLookup.getOutpost().getName(),"");

			if(getPower()<JOB4_POWER+0.15f){
				text.addPara("\"Looking at what you currently have at your disposal. This job could be exceptionally difficult for your current fleet.\" There is a look of doubt on her face.",tc,h,"exceptionally difficult","");
			}

			text.addPara("\"Are you able to help us captain?\" She tries to measure your response.");

			text.setFontInsignia();

			dialog.getOptionPanel().addOption("Accept", "nskr_kestevenQuestConfirmQuest");
			//Extra dialog
			dialog.getOptionPanel().addOption("\"I have some questions.\"", DIALOG_OPTION_EXTRA_START_PREFIX);
		}

		//job4 intelligence dialog nick
		boolean found = QuestHelper.getCompleted(KestevenFlag.JOB4_TARGET_FOUND);
		//standard
		if(stage==12 && !found){
			String hintLoc = job4TargetLoc.getStarSystem().getName();
			text.addPara("\"So, the fleet was instructed to send encrypted hyperwave signals using rather expensive Domain comms equipment. You know I hope they didn't lose that stuff... " +
					"Anyways, every few weeks they would report on their progress \" He scratches his head. \"and umm- we could pick up those transmissions here and decrypt them.\"",tc,h,"","");
			text.addPara("\"There's this one thing. uhh-\" He shifts around in his seat. \"A few days after their last known report, we picked up a burst of signals coming from a specific part of the "+hintLoc+". " +
					"\"He looks down at something.\" The um- signal was much weaker in magnitude, to the level that we could only decipher its direction...\"",tc,h,hintLoc,"");
			text.addPara("\"...That's pretty much all I know.\"",tc,h,"","");

			QuestHelper.setNicholasDialogStage(1);

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

			QuestHelper.setNicholasDialogStage(1);

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

			QuestHelper.setStage(15);

			dialog.getOptionPanel().addOption("Leave", "nskr_kestevenQuestExit");
			noLeave = true;
		}
		//job 5 tip jack
		if(stage==16 && person==jack && !jackTip) {
			boolean helpEliza = QuestHelper.getCompleted(KestevenFlag.ELIZA_HELPED);
			boolean killEliza = QuestHelper.getCompleted(KestevenFlag.ELIZA_KILLED);
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

			QuestHelper.setCompleted(true, KestevenFlag.JOB5_JACK_TIP);

			dialog.getOptionPanel().addOption("Leave", "nskr_kestevenQuestExit");
			noLeave = true;
		}
		//job 5 tip alice
		if(stage==16 && person==alice && !aliceTip) {
			int recovered = DataSatelliteDialog.getRecoveredSatelliteCount();
			boolean discovered3 = QuestHelper.getCompleted(KestevenFlag.JOB3_TARGET_DISCOVERED);
			SectorEntityToken loc1 = job4TargetLoc;
			SectorEntityToken artifact1 = QuestHelper.getArtifact(loc1.getStarSystem());
			SectorEntityToken loc2 = QuestHelper.getJob3Target();
			SectorEntityToken artifact2 = QuestHelper.getArtifact(loc2.getStarSystem());
			//all recovered
			if (recovered>=2) {
				text.addParagraph("\"Looks like you have both the known satellites covered, I can't help you much more. You should talk to Jack if you haven't already.\" She nods approvingly.");
				text.addPara("\"Efficient work captain.\"", tc, h, "", "");
			}
			//1 not recovered job 3
			if (recovered==1 && QuestHelper.getCompleted(KestevenFlag.SATELLITE4_RECOVERED)) {
				text.addParagraph("\"As you know those old comm satellites are the target. \"She pauses to think for a second.\" It must be that Tri-tachyon expedition was going after one, it has to be the one your looking for.\"");

				if(discovered3)text.addPara("\"It was in the "+loc2.getStarSystem().getName()+". Good thing you figured out where they were heading.\"", tc, h, loc2.getStarSystem().getName(), "");
				if(!discovered3)text.addPara("\"Despite your efforts to screw this up, by not finding out where they were heading. " +
						"We managed intercept its target from their comms. It was the "+loc2.getStarSystem().getName()+".\" Its likes she's lecturing a child.", tc, h, loc2.getStarSystem().getName(), "");
				//make important
				artifact2.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MISSION_IMPORTANT,true);
			}
			//1 not recovered job 4
			if (recovered==1 && QuestHelper.getCompleted(KestevenFlag.SATELLITE3_RECOVERED)) {
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

			QuestHelper.setCompleted(true, KestevenFlag.JOB5_ALICE_TIP);

			dialog.getOptionPanel().addOption("Leave", "nskr_kestevenQuestExit");
			noLeave = true;
		}
		//job 5 tip 2 alice
		if(stage==16 && person==alice && jackTip && aliceTip && !aliceTip2) {
			StarSystemAPI frost = SectorLookup.getFrost();
			StarSystemAPI tipSystem = QuestHelper.getJob5FrostTip();
			String constellation = QuestHelper.parseConstellation(tipSystem.getConstellation().getNameWithType());
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

			QuestHelper.setCompleted(true, KestevenFlag.JOB5_ALICE_TIP2);

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
			if(QuestHelper.getCompleted(KestevenFlag.ELIZA_AGREED_SINCERELY))dialog.getOptionPanel().addOption("\"Yes\" (lie)", "nskr_kestevenQuestConfirmQuest"+"B");
			noLeave = true;
		}

		if (!noLeave) dialog.getOptionPanel().addOption("Back", "nskr_kestevenQuestExit");
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
			KestevenPeople.getAlice().getRelToPlayer().adjustRelationship(-0.10f, RepLevel.VENGEFUL);
			//penalty text
			text.addPara("Relationship with Kesteven reduced by 5",g,r,"5","");
			text.addPara("Relationship with Alice Lumi reduced by 10",g,r,"10","");

			Global.getSoundPlayer().playUISound("ui_rep_drop",1f,1f);

			QuestHelper.setStage(11);
			QuestHelper.setFailed(true, KestevenFlag.JOB3_REFUSED);

			SectorEntityToken loc = QuestHelper.getJob3Target();
			spawnEnvironmentalStorytelling();
			QuestHelper.spawnArtifact(loc,3);
			DormantSpawner.addDormant(loc, "enigma", 45f, 50f, 0f, 1f, 1f, 1f, 1, 1);
		}
		text.setFontInsignia();
	}

	public static void spawnEnvironmentalStorytelling(){
		SectorEntityToken loc = QuestHelper.getJob3Target();

		Frost.addDerelict(loc.getStarSystem(), "doom_Strike", SystemHelper.createRandomNearOrbit(loc), ShipRecoverySpecial.ShipCondition.BATTERED, Math.random()<0.50f, null);
		Frost.addDerelict(loc.getStarSystem(), "atlas_Standard", SystemHelper.createRandomNearOrbit(loc), ShipRecoverySpecial.ShipCondition.BATTERED, Math.random()<0.50f, null);
		Frost.addDerelict(loc.getStarSystem(), "shrike_Attack", SystemHelper.createRandomNearOrbit(loc), ShipRecoverySpecial.ShipCondition.BATTERED, Math.random()<0.50f, null);

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
		frost_main1.setOrbit(SystemHelper.createRandomNearOrbit(loc));
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

			QuestHelper.setStage(1);
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
			KestevenPeople.getJack().getRelToPlayer().adjustRelationship(0.10f, RepLevel.COOPERATIVE);
			//completion text
			String payout = Misc.getDGSCredits(STAGE1_PAYOUT);
			String desc = "Received +" + payout;
			text.addPara(desc,g,h,"+"+payout,"");
			text.addPara("Relationship with Kesteven improved by 5",g,gr,"5","");
			text.addPara("Relationship with Jack Lapua improved by 10",g,gr,"10","");
			text.addPara("Acquired "+name+" modspec",g,h, name,"");

			Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

			QuestHelper.setStage(6);
		}
		//go talk to Alice job 3
		if(stage == 6) {
			str = "\"Go talk to Alice now.\"";
			QuestHelper.setStage(7);
			//CONTACT JACK
			ContactIntel.addPotentialContact(1f,person, dialog.getInteractionTarget().getMarket(), text);
		}
		//start job 3
		if(stage == 7) {
			text.setFontSmallInsignia();
			text.addPara("Acquired log entry for Hostile Takeover",g,h,"Hostile Takeover","");

			Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);

			str = "\"Go get to work now.\" The comm link is swiftly cut.";
			QuestHelper.setStage(8);
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
			KestevenPeople.getAlice().getRelToPlayer().adjustRelationship(0.10f, RepLevel.COOPERATIVE);
			//completion text
			String payout = Misc.getDGSCredits(STAGE3_PAYOUT);
			String desc = "Received +" + payout;
			text.addPara(desc,g,h,"+"+payout,"");
			text.addPara("Relationship with Kesteven improved by 5",g,gr,"5","");
			text.addPara("Relationship with Alice Lumi improved by 10",g,gr,"10","");
			text.addPara("Acquired 50,000 exchange points",g,h,"50,000 exchange points","");
			text.addPara("Acquired "+name+" modspec",g,h, name,"");

			Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

			QuestHelper.setStage(11);
		}
		//finish job 3 fail
		if(stage == 10 && failedJob3) {
			text.setFontInsignia();
			text.addPara("\"Just be glad I'm not firing you on the spot.\"");
			text.addPara("Okay, shes *really* frustrated with you.",g,h,"","");
			text.setFontSmallInsignia();
			Global.getSector().getFaction(Factions.PLAYER).adjustRelationship("kesteven",-0.05f);
			KestevenPeople.getAlice().getRelToPlayer().adjustRelationship(-0.10f, RepLevel.VENGEFUL);
			//completion text
			text.addPara("Relationship with Kesteven reduced by 5",g,r,"5","");
			text.addPara("Relationship with Alice Lumi reduced by 10",g,r,"10","");

			Global.getSoundPlayer().playUISound("ui_rep_drop",1f,1f);

			QuestHelper.setStage(11);
		}
		//start job 4
		if(stage == 11) {
			text.setFontSmallInsignia();
			text.addPara("Acquired log entry for Operation Lifesaver",g,h,"Operation Lifesaver","");

			Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);

			str = "\"Lets hope for the best, captain.\" She nods before cutting the comm link.";
			QuestHelper.setStage(12);
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
			KestevenPeople.getAlice().getRelToPlayer().adjustRelationship(0.10f, RepLevel.COOPERATIVE);
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

			QuestHelper.setStage(14);
			//CONTACT ALICE
			ContactIntel.addPotentialContact(1f,person, dialog.getInteractionTarget().getMarket(), text);
			//CONTACT lvl increase
			text.addPara("Increased contact level with Kesteven contacts",g,gr,"","");
			KestevenPeople.getJack().setImportance(PersonImportance.HIGH);
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
			KestevenPeople.getAlice().getRelToPlayer().adjustRelationship(0.10f, RepLevel.COOPERATIVE);
			//completion text
			String payout = Misc.getDGSCredits(STAGE4_PAYOUT);
			String desc = "Received +" + payout;
			text.addPara(desc,g,h,"+"+payout,"");
			text.addPara("Relationship with Kesteven improved by 5",g,gr,"5","");
			text.addPara("Relationship with Alice Lumi improved by 10",g,gr,"10","");
			text.addPara("Acquired Epoch-class prototype frigate",g,h,"Epoch-class","");
			text.addPara("Acquired "+name+" modspec",g,h, name,"");

			Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

			QuestHelper.setStage(14);
			//CONTACT ALICE
			ContactIntel.addPotentialContact(1f,person, dialog.getInteractionTarget().getMarket(), text);
			//CONTACT lvl increase
			text.addPara("Increased contact level with Kesteven contacts",g,gr,"","");
			KestevenPeople.getJack().setImportance(PersonImportance.HIGH);
		}
		//job 5 alice tip 2 know frost system
		if(stage == 16 && jackTip && aliceTip && !allDisks) {
			text.setFontInsignia();
			text.addPara("Alice pauses for a moment to think about what you said, and then proceeds to look up something on her datapad.");
			text.addPara("\"I think you are right, impressive. Now find the tundra planet in the "+ SectorLookup.getFrost().getName()+" ASAP.\"",tc,h, SectorLookup.getFrost().getName(),"");

			Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

			text.setFontSmallInsignia();
			text.addPara("Updated log entry for the Delve",g,h,"the Delve","");

			Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);
			text.setFontInsignia();

			QuestHelper.setCompleted(true, KestevenFlag.FROST_FOUND);
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

			QuestHelper.setCompleted(true, KestevenFlag.CACHE_FOUND);
			QuestHelper.setStage(17);
		}

		text.setFontInsignia();
		if (str.length()>0) {
			text.addPara(str);
		}
	}

	public static final ArrayList<String> MODS = new ArrayList<>();
	static {
		MODS.add(Ids.INERTIAL_SUPERCHARGER_HULLMOD_ID);
		MODS.add(Ids.VOLATILE_FLUX_INJECTOR_HULLMOD_ID);
		MODS.add(Ids.HIGH_CAPACITANCE_BANKS_HULLMOD_ID);
		MODS.add(Ids.CRITICAL_POINT_PROTECTION_HULLMOD_ID);
	}

	private HullModSpecAPI getRewardMod() {

		ArrayList<String> tempMods = new ArrayList<>(MODS);
		//try to give a new one
		for (String known : Global.getSector().getPlayerFaction().getKnownHullMods()){
			tempMods.remove(known);
		}
		//new one check
		if (!tempMods.isEmpty()){
			return Global.getSettings().getHullModSpec(tempMods.get(MathHelper.getSeededRandomNumberInRange(0,tempMods.size()-1, getRandom())));
		} else {
			return Global.getSettings().getHullModSpec(MODS.get(MathHelper.getSeededRandomNumberInRange(0,MODS.size()-1, getRandom())));
		}
	}

	public static Random getRandom() {
		return KestevenQuest.random(KestevenState.RANDOM_QUEST);
	}

}

