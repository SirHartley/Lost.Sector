//////////////////////
//Initially created by Histidine and modified from Nexelerin
//////////////////////
package scripts.kissa.LOST_SECTOR.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import scripts.kissa.LOST_SECTOR.campaign.quests.util.questStageManager;
import scripts.kissa.LOST_SECTOR.util.util;

public class nskr_ttCollectorDialog extends PaginatedOptions {
	//
	//Hack job of nex code, but it just worksTM
	//

	public static final String PERSISTENT_KEY = "nskr_ttCollectorDialogKey";
	private final String id = PERSISTENT_KEY;
	private static boolean paid = false;
	private float cargo = 0f;
	public static final String PERSISTENT_RANDOM_KEY = "nskr_ttCollectorDialogRandom";

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
		Global.getLogger(nskr_ttCollectorDialog.class).info(message);
	}
	
	@Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) 
	{
		String arg = params.get(0).getString(memoryMap);
		boolean booleanArg = false;
		if (params.size()>1) {
			booleanArg = Boolean.parseBoolean(params.get(1).getString(memoryMap));
		}
		setupVars(dialog, memoryMap);

		switch (arg)
		{
			case "init":
				break;
			case "hasOption":
				return validEntity(entity);
			case "setPaid":
				setPaid(booleanArg, id);
			case "canPay":
				canPay();
				showOptions();
				break;
			case "pay":
				pay();
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

		paid = getPaid(PERSISTENT_KEY);
		cargo = playerFleet.getCargo().getCommodityQuantity("nskr_electronics");
	}
	
	@Override
	public void showOptions() {
		super.showOptions();
		for (String optId : disabledOpts)
		{
			dialog.getOptionPanel().setEnabled(optId, false);
		}
		//dialog.getOptionPanel().setShortcut("nskr_ttCollectorDialogExit", Keyboard.KEY_ESCAPE, false, false, false, false);
		//dialog.getOptionPanel().setShortcut("nskr_ttCollectorDialogExitFight", Keyboard.KEY_ESCAPE, false, false, false, false);
	}

	protected void canPay(){
		String str = "";
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color gr = Misc.getPositiveHighlightColor();
		Color r = Misc.getNegativeHighlightColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		text.setFontInsignia();

		//can pay check
		if (!paid) {
			//give cargo
			if (cargo > 0f) {
				text.addPara("\"Our scans show you have "+(int)cargo+" units of Artifact Electronics.\"");
				text.addPara("\"Just give us all the cargo, and we can stay civilized about this.\"");

				addOption("Hand over the "+(int)cargo+" units of Artifact Electronics", "nskr_ttCollectorDialogPayAll");
				addOption("\"No, I don't think I will.\"", "nskr_ttCollectorDialogExitFight");

			} else {
				//0 cargo
				text.addPara("\"Seems like you don't have enough cargo to give us anything.\"");

				addOption("\"Yeah, uhhh... I Don't have any of that stuff.\"", "nskr_ttCollectorDialogNoPay");
			}
		}

	}

	protected void pay() {
		text.setFontSmallInsignia();
		String str = "";
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color gr = Misc.getPositiveHighlightColor();
		Color r = Misc.getNegativeHighlightColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		float toPay = 0f;
		toPay = cargo;

		//remove
		playerCargo.removeCommodity("nskr_electronics", cargo);
		//paid
		setPaid(true, PERSISTENT_KEY);
		//relation
		Global.getSector().getFaction(Factions.PLAYER).adjustRelationship(Factions.TRITACHYON,0.05f);
		person.getRelToPlayer().adjustRelationship(0.10f, RepLevel.COOPERATIVE);
		//completion text
		text.addPara("Lost "+(int)cargo+" units of Artifact Electronics",g,r,(int)cargo+" units of Artifact Electronics","");
		text.addPara("Relationship with Tri-tachyon improved by 5",g,gr,"5","");
		text.addPara("Relationship with "+person.getNameString()+" improved by 10",g,gr,"10","");
		//sound
		Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);
		text.setFontInsignia();
		//un aggro
		entity.getMemoryWithoutUpdate().clear();
		entity.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
		entity.getMemoryWithoutUpdate().set(questStageManager.TT_COLLECTOR_KEY, true);
	}

	//
	public static boolean validEntity(SectorEntityToken entity)
	{
		if (entity==null) return false;
		if (paid) return false;
		//pick correct fleet
		if (!entity.getMemory().contains(questStageManager.TT_COLLECTOR_KEY)) return false;

		return entity.getFaction().getId().equals(Factions.TRITACHYON);
	}

	public static Random getRandom() {
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

			data.put(PERSISTENT_RANDOM_KEY, new Random(util.getSeedParsed()));
		}
		return (Random) data.get(PERSISTENT_RANDOM_KEY);
	}

	public static boolean getPaid(String id) {

		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(id)) data.put(id, false);

		return (boolean)data.get(id);
	}

	public static void setPaid(boolean paid, String id) {

		Map<String, Object> data = Global.getSector().getPersistentData();
		data.put(id, paid);
	}

}

