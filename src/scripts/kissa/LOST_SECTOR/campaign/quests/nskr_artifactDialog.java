package scripts.kissa.LOST_SECTOR.campaign.quests;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.procgen.nskr_dormantSpawner;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.fleetInfo;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.util;
import scripts.kissa.LOST_SECTOR.world.systems.frost.nskr_frost;

import java.awt.*;
import java.util.*;
import java.util.List;


public class nskr_artifactDialog implements InteractionDialogPlugin {
	//
	//
	//
	public static final String RECOVERED_COUNT_KEY = "nskr_artifactKeyCount";
	public static final String ARTIFACT_EMPTY_KEY = "$nskr_artifactKeyEmpty";
	public static final String PERSISTENT_KEY = "nskr_artifactKey";
	public static final String AGGRO_KEY = "nskr_artifactAggroKey";
	public static final String RECOVERED_3_KEY = "nskr_artifactKey3Recovered";
	public static final String RECOVERED_4_KEY = "nskr_artifactKey4Recovered";
	public static final String PERSISTENT_RANDOM_KEY = "nskr_artifactKeyRandom";

	private InteractionDialogAPI dialog;
	private TextPanelAPI text;
	private OptionPanelAPI options;
	private VisualPanelAPI visual;

	static void log(final String message) {
		Global.getLogger(nskr_artifactDialog.class).info(message);
	}

	@Override
	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;

		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
		visual.showImageVisual(dialog.getInteractionTarget().getCustomInteractionDialogImageVisual());

		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color gr = Misc.getPositiveHighlightColor();
		Color r = Misc.getNegativeHighlightColor();
		Color tc = Misc.getTextColor();

		text.setFontInsignia();

		if (!dialog.getInteractionTarget().getMemory().contains(ARTIFACT_EMPTY_KEY)) {
			//satellite first time
			if (getRecoveredSatelliteCount() == 0) {
				text.addPara("This autonomous satellite emits a weak signal, the message it's trying to relay is garbled beyond any meaning. Likely due to the equipment getting blasted with radiation for countless years. There is not much else to discover here.");

				options.addOption("Leave", OptionId.LEAVE);
				options.addOption("Take a closer look", OptionId.A1);
			}
			//satellite second time
			if (getRecoveredSatelliteCount() == 1) {
				text.addPara("This autonomous satellite just like the last one emits a weak signal, the message it's trying to relay is garbled beyond any meaning. Likely due to the equipment getting blasted with radiation for countless years.");

				options.addOption("See if this one also has a disk to recover", OptionId.B1);
				options.addOption("Leave", OptionId.LEAVE);
			}

		} else {
			//recovered text
			text.addPara("This autonomous satellite is now cold and dead. There is nothing to discover here.");
			options.addOption("Leave", OptionId.LEAVE);
		}
	}

	@Override
	public void optionSelected(String optionText, Object optionData) {
		Color h = Misc.getHighlightColor();
		Color b = Misc.getBasePlayerColor();
		Color g = Misc.getGrayColor();
		Color gr = Misc.getPositiveHighlightColor();
		Color r = Misc.getNegativeHighlightColor();
		Color tc = Misc.getTextColor();

		text.addPara(optionText,b,h,"","");

		int stage = questUtil.getStage();

		//first time
		//initial
		if (optionData==OptionId.A1){
			options.clearOptions();
			text.addPara("As you fly in closer, the satellite slowly grows on your display from a tiny unrecognisable dot in space. Even once you've finished your approach it's still hard to make out any details on the zoomed in image, the satellite must be only a few metres in size.");
			text.addPara("You watch the satellite slowly rotate on your display, as it spins uncontrollably in the void of space.");
			text.addPara("The operations chief waits for your orders.");

			options.addOption("Leave", OptionId.LEAVE);
			options.addOption("Send in the salvor crew", OptionId.A2);
		}
		//a2
		if (optionData==OptionId.A2) {
			options.clearOptions();
			text.addPara("Your ops chief oversees the mission by-the-book, staging approach-and-scan to maximize safety in the event of environmental or technological hazards. And after a while the salvor team is in place to inspect the satellite.");
			text.addPara("Suddenly the holodisplay flickers to life showing a closeup of the target. The surfaces of the satellite have a desaturated sheen to them, it looks like the slightest force could make the whole structure crumble apart. A clear sign of intense radiation bombardment.");
			text.addPara("The team starts carefully disassembling the structure looking for anything worth recovering. Not much of value is discovered at first, only data drives scrubbed clean by radiation, and other malfunctioning comms equipment. Until-");

			options.addOption("Continue", OptionId.A3);
			options.addOption("Leave", OptionId.LEAVE);
		}
		//a3
		if (optionData==OptionId.A3){
			options.clearOptions();
			text.addPara("The team reports of a hidden compartment inside the satellite. Unlike the rest of the equipment it has proper radiation shielding, clearly it's somehow important for the satellites intended function.");
			text.addPara("The ops chief runs a set of different scans on the compartment. Revealing inside a shielded data disk, connected to some sort of anti-tampering device. It is unclear what this device would do once triggered.");
			text.addPara("Your ops chief looks worried. \"There's no telling from outside what that device will do if triggered.\"");

			options.addOption("Prepare to take the disk", OptionId.A4);
			options.addOption("Leave", OptionId.LEAVE);
		}
		//a4
		if (optionData==OptionId.A4) {
			options.clearOptions();
			text.addPara("The ops chief runs over the data you've gathered one last time. \"Well at least there's no sign of explosives onboard the satellite. Still the device could begin a self-destruct reactor overload, causing an intense radiation leak or an EM pulse. Alternatively it could simply send a hyperwave ping alerting nearby forces of tampering, whoever those might be.\"");
			text.addPara("They pause for a second to think. \"... You know, there's a good chance it'll just do nothing. Considering the state that the rest of the equipment is in.\"");
			text.addPara("You order the ops chief to prepare a plan to recover the disk, and after a short wait you are ready to begin.");

			options.addOption("Proceed with the operation", OptionId.A5);
			options.addOption("Leave", OptionId.LEAVE);
		}
		//a5
		if (optionData==OptionId.A5) {
			options.clearOptions();
			//unset ESC
			dialog.setOptionOnEscape("", null);
			text.addPara("A handful of volunteers are sent in hardsuits to recover the disk. You closely watch as the team tries their best not to prematurely trip the device while trying to recover the disk.");
			text.addPara("Finally a salvor picks up the disk before it's disconnected, and you get a clear look at it \"Project : Enigma\" is printed on it in faded red text.");
			text.addPara("The rest of the team hurries into the shielded shuttle as the last member prepares to disconnect the disk. Your ops chief is holding their breath at this point, as the tension at the bridge builds up.");

			options.addOption("Continue", OptionId.A6);
		}
		//a6
		if (optionData==OptionId.A6) {
			options.clearOptions();
			text.addPara("They take a quick look at the disk one last time and then unplug it, and... nothing. Your ops chief helps finish the operation without further incident.");
			text.addPara("Onboard your comms officer picks up the disk, so they can deliver an initial report.");

			options.addOption("Continue", OptionId.A7);
		}
		//final
		if (optionData==OptionId.A7) {
			options.clearOptions();
			if(stage>15)text.addPara("Your comms officer approaches you ready to tell more about this disk. \"This is no ordinary data drive, and it's not just the radiation shielding. It has a construction I've never seen before, it must be the disk we're looking for. As our intel suggested all the data is encrypted, oddly enough the disk uses centuries old Domain encryption.\"");
			//alt early recovery
			if(stage<=15)text.addPara("Your comms officer approaches you ready to tell more about this disk. \"This is no ordinary data drive, and it's not just the radiation shielding. It has a construction I've never seen before, and can't find any reference to in our databases. Although all the data is encrypted, oddly enough the disk uses centuries old Domain encryption. So we could break it if we had more data to work with, and the right equipment.\"");
			text.addPara("Suddenly you are interrupted by multiple alarms going off at once on the bridge. Seems like the anti-tampering device only malfunctioned temporarily. \"Captain, the satellite just fired a hyperwave ping alerting the whole damn constellation of our presence.\"");
			text.addPara("It's time to leave, quickly.",g,h,"","");

			setRecoveredSatelliteCount(1);
			dialog.getInteractionTarget().getMemory().set(ARTIFACT_EMPTY_KEY, true);
			questUtil.setDisksRecovered(questUtil.getDisksRecovered()+1);
			String key = "";
			String number = "";
			if (dialog.getInteractionTarget().getMemory().contains(questStageManager.ARTIFACT_KEY+3)){
				key = nskr_dormantSpawner.DORMANT_KEY;
				number = "3";
				//complete
				questUtil.setCompleted(true, RECOVERED_3_KEY);
			} else {
				key = questStageManager.JOB4_TARGET_KEY;
				number = "4";
				//complete
				questUtil.setCompleted(true, RECOVERED_4_KEY);
			}
			//remove important
			if (dialog.getInteractionTarget().getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_MISSION_IMPORTANT)){
				dialog.getInteractionTarget().getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
			}
			//ping
			Global.getSector().addPing(dialog.getInteractionTarget(), Pings.SENSOR_BURST);
			Global.getSector().addPing(dialog.getInteractionTarget(), Pings.INTERDICT);

			text.setFontSmallInsignia();
			//acquire text
			text.addPara("Acquired Data Disk #"+number,g,h,"Data Disk #"+number,"");
			text.setFontInsignia();

			Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

			makeHostile(dialog.getInteractionTarget().getContainingLocation(), key);

			dialog.setOptionOnEscape("Leave", OptionId.LEAVE_WITH_SOUND);
			options.addOption("Leave", OptionId.LEAVE_WITH_SOUND);
		}
		//second time
		//initial
		if (optionData==OptionId.B1){
			options.clearOptions();
			text.addPara("Your ops chief prepares the salvage operation, and after a short wait the salvor team is ready to go. They get in range without issue, and start looking for the same hidden compartment.");
			text.addPara("You watch the holofeed from multiple perspectives show the operation in progress, soon the team is in place to recover the disk.");

			options.addOption("Continue", OptionId.B2);
			options.addOption("Leave", OptionId.LEAVE);
		}
		//b2
		if (optionData==OptionId.B2){
			options.clearOptions();
			text.addPara("Both the disk and anti-tampering device seem to be intact. The sensors team runs multiple scans on the satellite, then showing projections that confirm its nearly identical to the last one.");
			text.addPara("The ops chief paces back and forth impatiently \"We're ready to go captain, just give us the word.\"");

			options.addOption("Order the disk to be retrieved", OptionId.B3);
			options.addOption("Leave", OptionId.LEAVE);
		}
		//b3
		if (optionData==OptionId.B3){
			//unset ESC
			dialog.setOptionOnEscape("", null);
			options.clearOptions();
			text.addPara("Before you can give the go order the satellite suddenly comes to life. Sensor readings go haywire on the bridge as your crew scrambles to figure out what's going on.");
			text.addPara("The ops chief quickly begins to bark orders to try and get a grip on the situation. " +
					"\"It's stuck on loop broadcasting some hyperwave broadcast.\"The sensors office reports. " +
					"\"It's harmless, well - as harmless as a hyperwave beam is, and seemingly unintentional. We should be able to capture it for later, maybe there's something of use here.\"");
			text.addPara("\"Well seems like the satellite is - mostly safe.\" The ops chief says. \"We should move forward with the operation ASAP.\"");

			options.addOption("Continue", OptionId.B4);
		}
		//final
		if (optionData==OptionId.B4){
			options.clearOptions();
			text.addPara("The salvor team hastily recovers the disk with the help of some shielded drones - to minimize hyperwave exposure. " +
					"The chiefs strict attention to protocol makes this seem like a routine operation. And in no time the shuttle is on its way back to your flagship.");
			text.addPara("During the wait the communications team has finished an early report on the broadcast. \"Not only is this broadcast encrypted, there's a heavy layer of corruption on top.\" " +
					"The comms officer says. \"But of course there's nothing a little elbow grease from our team can't solve.\"");
			text.addPara("\"With some cleanup, and a couple of decryption algorithms, we managed to extract a group of keywords that are repeated in the broadcast.\"");
			String constellation = questUtil.getJob5FrostTip().getConstellation().getName();
			text.addPara("Enigma, Glacier, Frozen, "+ nskr_frost.getName()+", Heart, "+constellation,g,h,"","");
			text.addPara("\"Your guess is as good as mine captain.\" They shrug. \"It's a miracle we got anything out from this data set.\"");
			text.addPara("The ops chief reports to you. \"With this mess solved we should get moving captain, anyone listening nearby knows that something's up.\"");

			setRecoveredSatelliteCount(2);
			dialog.getInteractionTarget().getMemory().set(ARTIFACT_EMPTY_KEY, true);
			questUtil.setDisksRecovered(questUtil.getDisksRecovered()+1);
			String key = "";
			String number = "";
			if (dialog.getInteractionTarget().getMemory().contains(questStageManager.ARTIFACT_KEY+3)){
				key = nskr_dormantSpawner.DORMANT_KEY;
				number = "3";
				//complete
				questUtil.setCompleted(true, RECOVERED_3_KEY);
			} else {
				key = questStageManager.JOB4_TARGET_KEY;
				number = "4";
				//complete
				questUtil.setCompleted(true, RECOVERED_4_KEY);
			}
			//remove important
			if (dialog.getInteractionTarget().getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_MISSION_IMPORTANT)){
				dialog.getInteractionTarget().getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
			}
			//ping
			Global.getSector().addPing(dialog.getInteractionTarget(), Pings.SENSOR_BURST);
			Global.getSector().addPing(dialog.getInteractionTarget(), Pings.INTERDICT);

			text.setFontSmallInsignia();
			//acquire text
			text.addPara("Acquired Data Disk #"+number,g,h,"Data Disk #"+number,"");
			text.setFontInsignia();

			Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

			makeHostile(dialog.getInteractionTarget().getContainingLocation(), key);

			dialog.setOptionOnEscape("Leave", OptionId.LEAVE_WITH_SOUND);
			options.addOption("Leave", OptionId.LEAVE_WITH_SOUND);
		}
		//leave
		if (optionData==OptionId.LEAVE) {
			options.clearOptions();
			dialog.dismiss();
		}
		//play sound
		if (optionData==OptionId.LEAVE_WITH_SOUND) {
			options.clearOptions();
			dialog.dismiss();

			Global.getSoundPlayer().playUISound("ui_sensor_burst_on",1f,1f);
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
		B4,
		B5,
		LEAVE,
		LEAVE_WITH_SOUND,
	}

	public void makeHostile(LocationAPI loc, String targetKey){

		for (SectorEntityToken e : loc.getAllEntities()){
			if (e.getMemoryWithoutUpdate()==null) continue;
			if (e.getMemoryWithoutUpdate().contains(targetKey)){
				CampaignFleetAPI f = ((CampaignFleetAPI)e);

				if (f.getMemoryWithoutUpdate().contains(nskr_dormantSpawner.DORMANT_KEY)){
					//un-dormant
					f.addAbility(Abilities.EMERGENCY_BURN);
					f.addAbility(Abilities.SENSOR_BURST);
					f.addAbility(Abilities.GO_DARK);

					f.setAI(Global.getFactory().createFleetAI(f));
					f.getMemory().clear();
					f.getMemoryWithoutUpdate().set(nskr_dormantSpawner.DORMANT_KEY, true);
					f.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE, true);

					List<fleetInfo> fleets = fleetUtil.getFleets(questStageManager.FLEET_ARRAY_KEY);
					fleets.add(new fleetInfo(f, null, f.getContainingLocation().createToken(f.getLocation())));
					fleetUtil.setFleets(fleets, questStageManager.FLEET_ARRAY_KEY);
				}
				f.clearAssignments();
				f.addAssignment(FleetAssignment.INTERCEPT, Global.getSector().getPlayerFleet(), Float.MAX_VALUE, "intercepting your fleet");
			}
		}
	}

	@Override
	public void advance(float amount) {
	}

	@Override
	public void optionMousedOver(String optionText, Object optionData) {

	}

	@Override
	public void backFromEngagement(EngagementResultAPI battleResult) {

	}

	@Override
	public Object getContext() {
		return null;
	}

	@Override
	public Map<String, MemoryAPI> getMemoryMap() {
		return null;
	}

	public static Random getRandom() {
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

			data.put(PERSISTENT_RANDOM_KEY, new Random(util.getSeedParsed()));
		}
		return (Random) data.get(PERSISTENT_RANDOM_KEY);
	}

	public static int getRecoveredSatelliteCount() {
		String id = RECOVERED_COUNT_KEY;

		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(id)) data.put(id, 0);

		return (int)data.get(id);
	}

	public static void setRecoveredSatelliteCount(int recovered) {

		Map<String, Object> data = Global.getSector().getPersistentData();
		data.put(RECOVERED_COUNT_KEY, recovered);
	}

}

