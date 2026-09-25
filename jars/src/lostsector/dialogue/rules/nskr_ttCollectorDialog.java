//////////////////////
//Initially created by Histidine and modified from Nexelerin
//////////////////////
package lostsector.dialogue.rules;

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
import lostsector.campaign.kesteven.quest.QuestStageManager;
import lostsector.campaign.kesteven.quest.KestevenFlag;
import lostsector.campaign.kesteven.quest.KestevenQuest;
import lostsector.campaign.kesteven.quest.KestevenState;
import lostsector.campaign.kesteven.quest.QuestHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class nskr_ttCollectorDialog extends PaginatedOptions {
	//
	//

	private static boolean paid = false;
	private float cargo = 0f;

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
				setPaid(booleanArg);
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

		paid = getPaid();
		cargo = playerFleet.getCargo().getCommodityQuantity("nskr_electronics");
	}
	
	@Override
	public void showOptions() {
		super.showOptions();
		for (String optId : disabledOpts)
		{
			dialog.getOptionPanel().setEnabled(optId, false);
		}
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
		setPaid(true);
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
		entity.getMemoryWithoutUpdate().set(QuestStageManager.TT_COLLECTOR_KEY, true);
	}

	//
	public static boolean validEntity(SectorEntityToken entity)
	{
		if (entity==null) return false;
		if (paid) return false;
		//pick correct fleet
		if (!entity.getMemory().contains(QuestStageManager.TT_COLLECTOR_KEY)) return false;

		return entity.getFaction().getId().equals(Factions.TRITACHYON);
	}

	public static Random getRandom() {
		return KestevenQuest.random(KestevenState.RANDOM_COLLECTOR);
	}

	public static boolean getPaid() {
		return QuestHelper.getCompleted(KestevenFlag.COLLECTOR_PAID);
	}

	public static void setPaid(boolean paid) {
		QuestHelper.setCompleted(paid, KestevenFlag.COLLECTOR_PAID);
	}

}

