//////////////////////
//Initially created by Histidine and modified from Nexelerin
//////////////////////
package scripts.kissa.LOST_SECTOR.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;
import scripts.kissa.LOST_SECTOR.util.util;
import scripts.kissa.LOST_SECTOR.util.nskr_stringHelper;

public class nskr_job4FleetDialog extends PaginatedOptions {
	//
	//Hack job of nex code, but it just worksTM
	//

	private int stage = 0;
	private boolean helped = false;
	private boolean foundTarget = false;
	private static float relation = 0;
	public static final String PERSISTENT_KEY = "nskr_job4FleetDialogKey";
	private final String id = PERSISTENT_KEY;
	public static final String PERSISTENT_RANDOM_KEY = "nskr_job4FleetDialogRandom";


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
		Global.getLogger(nskr_job4FleetDialog.class).info(message);
	}
	
	@Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) 
	{
		String arg = params.get(0).getString(memoryMap);
		int intArg = 0;
		if (params.size()>1) {
			intArg = Integer.parseInt(params.get(1).getString(memoryMap));
		}
		setupVars(dialog, memoryMap);

		switch (arg)
		{
			case "init":
				break;
			case "hasOption":
				return validEntity(entity);
			case "isDialogStage":
				return getDialogStage(id)==intArg;
			case "isAtmostDialogStage":
				return getDialogStage(id)<=intArg;
			case "isAtleastDialogStage":
				return getDialogStage(id)>=intArg;
			case "setDialogStage":
				setDialogStage(intArg, id);
				break;
			case "displayDialogInitial":
				displayDialogInitial();
				break;
			case "help":
				help();
				showOptions();
				break;
			case "confirmHelp":
				confirmHelp();
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
		text = dialog.getTextPanel();
		
		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();
		
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();

		player = Global.getSector().getPlayerPerson();
		person = dialog.getInteractionTarget().getActivePerson();
		//faction = person.getFaction();

		foundTarget = questUtil.getCompleted(questStageManager.JOB4_FOUND_TARGET_KEY);

		stage = getDialogStage(id);
		relation = Global.getSector().getPlayerFaction().getRelationship("kesteven");
		helped = questUtil.getCompleted(questStageManager.JOB4_HELPED_KEY);
	}
	
	@Override
	public void showOptions() {
		super.showOptions();
		for (String optId : disabledOpts)
		{
			dialog.getOptionPanel().setEnabled(optId, false);
		}
		dialog.getOptionPanel().setShortcut("nskr_job4FleetDialogExit", Keyboard.KEY_ESCAPE, false, false, false, false);
	}

	protected void help(){
		text.setFontInsignia();
		String str = "";
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color gr = Misc.getPositiveHighlightColor();
		Color r = Misc.getNegativeHighlightColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		boolean supplies = playerCargo.getSupplies()>=(float) questStageManager.JOB4_HELP_SUPPLIES;
		boolean fuel = playerCargo.getFuel()>=(float) questStageManager.JOB4_HELP_FUEL;
		text.setFontInsignia();
		//helped check
		if (!questUtil.getCompleted(questStageManager.JOB4_HELPED_KEY)) {
			if (!supplies || !fuel) {
				text.addPara("\"Seems like you don't have enough resources to help us.\"");
				text.setFontSmallInsignia();
				text.addPara("You have "+(int)playerCargo.getSupplies()+" units of supplies and "+(int)playerCargo.getFuel()+" units of fuel.",
						g,h,(int)playerCargo.getSupplies()+"",(int)playerCargo.getFuel()+"");
				text.setFontInsignia();
			} else {
				text.addPara("Are you sure you ready to hand over the supplies and fuel?",g);
				text.setFontSmallInsignia();
				text.addPara("You have "+(int)playerCargo.getSupplies()+" units of supplies and "+(int)playerCargo.getFuel()+" units of fuel.",
						g,h,(int)playerCargo.getSupplies()+"",(int)playerCargo.getFuel()+"");
				text.setFontInsignia();
				addOption("Confirm", "nskr_job4FleetDialogHelpConfirm");

			}
		}
		text.setFontInsignia();
		addOptionAllPages("Leave", "nskr_job4FleetDialogExit");
	}

	protected void confirmHelp() {
		text.setFontInsignia();
		String  str = "";
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color gr = Misc.getPositiveHighlightColor();
		Color r = Misc.getNegativeHighlightColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		float supplies = (float) questStageManager.JOB4_HELP_SUPPLIES;
		float fuel = (float) questStageManager.JOB4_HELP_FUEL;

		//remove
		playerCargo.removeCommodity(Commodities.SUPPLIES, supplies);
		playerCargo.removeCommodity(Commodities.FUEL, fuel);
		//helped
		questUtil.setCompleted(true, questStageManager.JOB4_HELPED_KEY);
		//relation
		Global.getSector().getFaction(Factions.PLAYER).adjustRelationship("kesteven",0.05f);
		person.getRelToPlayer().adjustRelationship(0.10f, RepLevel.COOPERATIVE);
		//completion text
		text.setFontSmallInsignia();
		text.addPara("Lost "+(int)supplies+" units of supplies",g,r,(int)supplies+" units of supplies","");
		text.addPara("Lost "+(int)fuel+" units of fuel",g,r,(int)fuel+" units of fuel","");
		text.addPara("Relationship with Kesteven improved by 5",g,gr,"5","");
		text.addPara("Relationship with "+person.getNameString()+" improved by 10",g,gr,"10","");
		text.setFontInsignia();
		//sound
		Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

		text.setFontInsignia();
		//text.addPara(str);
	}

	//
	public static boolean validEntity(SectorEntityToken entity)
	{
		if (entity==null) return false;
		if (relation<=-0.5f) return false;
		if (questUtil.getStage()>=14) return false;
		//pick correct fleet
		if (!entity.getMemory().contains(questStageManager.JOB4_FRIENDLY_KEY)) return false;

		return entity.getFaction().getId().equals("kesteven");
	}

	protected void displayDialogInitial() {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color gr = Misc.getPositiveHighlightColor();
		Color r = Misc.getNegativeHighlightColor();
		Color tc = Misc.getTextColor();

		String HeOrShe = "He";
		String hisOrHer = "his";
		if(person.getGender()== FullName.Gender.FEMALE){
			HeOrShe = "She";
			hisOrHer = "her";
		}

		text.addPara("\"We were in the process of recovering a certain Enigma artifact, when suddenly the location turned hot. A swarm of those AI fleets appeared out of nowhere. We managed to escape the initial ambush after some heavy fighting, but many losses were suffered...\" "+HeOrShe+" pauses for a moment as "+hisOrHer+" face turns grim.");
		if(!foundTarget) {
			text.addPara(HeOrShe+" collects themself back together. \"I'll send the location of the initial incident over to you, maybe you can do something to them. There isn't much fight left in us.\"");
			//acquire text
			text.setFontSmallInsignia();
			text.addPara("Acquired Strike Group coordinates", g, h, "Strike Group", "");
			text.setFontInsignia();
		}

		text.addPara("\"Most of our supplies and fuel were lost in the fighting, we are a sitting duck here. You could help us by handing over 250 supplies and 400 fuel, or by heading back home and telling them to send a rescue fleet.\"",tc,h,"250 supplies","400 fuel");

		Global.getSoundPlayer().playUISound("ui_noise_static",1f,1f);

		text.setFontInsignia();
	}

	public static Random getRandom() {
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

			data.put(PERSISTENT_RANDOM_KEY, new Random(util.getSeedParsed()));
		}
		return (Random) data.get(PERSISTENT_RANDOM_KEY);
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

}

