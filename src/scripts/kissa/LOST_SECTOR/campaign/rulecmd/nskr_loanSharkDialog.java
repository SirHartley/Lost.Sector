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

import scripts.kissa.LOST_SECTOR.campaign.fleets.events.nskr_loanShark;
import scripts.kissa.LOST_SECTOR.util.util;

public class nskr_loanSharkDialog extends PaginatedOptions {
	//
	//Hack job of nex code, but it just worksTM
	//

	private static float relation = 0;
	public static final String PERSISTENT_KEY = "nskr_loanSharkDialogKey";
	private final String id = PERSISTENT_KEY;
	private boolean paid = false;
	private boolean poor = true;
	private int money = 0;
	private float credits = 0f;
	public static final String PERSISTENT_RANDOM_KEY = "nskr_loanSharkDialogRandom";

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
		Global.getLogger(nskr_loanSharkDialog.class).info(message);
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

		relation = Global.getSector().getPlayerFaction().getRelationship("kesteven");
		paid = getPaid(PERSISTENT_KEY);
		money = nskr_debt.getDebt();
		credits = playerCargo.getCredits().get();
		poor = credits<100000f;
	}
	
	@Override
	public void showOptions() {
		super.showOptions();
		for (String optId : disabledOpts)
		{
			dialog.getOptionPanel().setEnabled(optId, false);
		}
		//dialog.getOptionPanel().setShortcut("nskr_loanSharkDialogExit", Keyboard.KEY_ESCAPE, false, false, false, false);
		//dialog.getOptionPanel().setShortcut("nskr_loanSharkDialogExitFight", Keyboard.KEY_ESCAPE, false, false, false, false);
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

		//can pay check
		if (!paid) {
			//not enough
			if (money>credits && poor) {
				text.addPara("\"Seems like you don't have enough credits to pay us anything.\"");

				addOption("\"Yeah, uhhh... I Don't have any money.\"", "nskr_loanSharkDialogNoPay");
			}
			//pay some money
			if (money>credits && !poor) {
				text.addPara("\"Seems like you don't have enough credits to pay us everything.\"");
				text.addPara("\"Just give us all you have, so we don't need to use *other* measures.\"");

				addOption("Pay them " + Misc.getDGSCredits(credits), "nskr_loanSharkDialogPaySome");
				addOption("\"No, I don't think I will.\"", "nskr_loanSharkDialogExitFight");
			}
			//pay all money
			if (money<=credits) {
				text.addPara("\"Just give us all the owed money, and we don't need to use *other* measures.\"");

				addOption("Pay them " + Misc.getDGSCredits(money), "nskr_loanSharkDialogPayAll");
				addOption("\"No, I don't think I will.\"", "nskr_loanSharkDialogExitFight");
			}
		}
		text.setFontInsignia();
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
		if (money>credits){
			toPay = credits;
		} else toPay = money;

		//remove
		playerCargo.getCredits().subtract(toPay);
		nskr_debt.addDebt((int)-toPay);
		//helped
		setPaid(true, PERSISTENT_KEY);
		//relation
		Global.getSector().getFaction(Factions.PLAYER).adjustRelationship("kesteven",0.05f);
		person.getRelToPlayer().adjustRelationship(0.10f, RepLevel.COOPERATIVE);
		//completion text
		text.addPara("Lost "+Misc.getDGSCredits(toPay),g,r,Misc.getDGSCredits(toPay)+"","");
		text.addPara("Debt reduced to "+Misc.getDGSCredits(nskr_debt.getDebt()),g,h,Misc.getDGSCredits(nskr_debt.getDebt())+"","");
		text.addPara("Relationship with Kesteven improved by 5",g,gr,"5","");
		text.addPara("Relationship with "+person.getNameString()+" improved by 10",g,gr,"10","");
		//sound
		Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);
		text.setFontInsignia();
		//un aggro
		entity.getMemoryWithoutUpdate().clear();
		entity.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
	}

	//
	public static boolean validEntity(SectorEntityToken entity)
	{
		if (entity==null) return false;
		if (relation>-0.5f) return false;
		//pick correct fleet
		if (!entity.getMemory().contains(nskr_loanShark.COLLECTOR_KEY)) return false;

		return entity.getFaction().getId().equals("kesteven");
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

