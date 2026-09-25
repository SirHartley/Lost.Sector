//////////////////////
//Initially created by Histidine and modified from Nexelerin
//////////////////////
package lostsector.dialogue.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import lostsector.campaign.kesteven.quest.KestevenQuest;
import lostsector.helper.StringHelper;
import lostsector.helper.MathHelper;
import lostsector.helper.SectorLookup;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.List;
import java.util.*;

// TODO: replace this menu with a custom UI panel (docs/UI.md).
public class nskr_shipSwap extends BaseCommandPlugin {

	public static final String POINTS_KEY = "$nskr_shipSwapPoints";
	public static final String STOCK_ARRAY_KEY = "$nskr_shipSwapStock";
	public static final String TO_PURCHASE_KEY = "$nskr_shipSwapToPurchase";
	public static final float STOCK_KEEP_DAYS = 30;
	public static final int STOCK_COUNT_MIN = 6;
	public static final int STOCK_COUNT_MAX = 12;

	public static final String PERSISTENT_RANDOM_KEY = "nskr_shipSwapRandom";
	public static Logger log = Global.getLogger(nskr_shipSwap.class);

	// Local display values for rules text, written with expiry 0.
	public static final String POINTS_DISPLAY_KEY = "$nskr_shipSwap_points";
	public static final String POINTS_STR_KEY = "$nskr_shipSwap_pointsStr";

	public static final String SOLD_TRIGGER = "nskr_shipSold";
	public static final String RETURN_OPTION = "nskr_shipSwapMenuReturn";
	public static final String CONFIRM_OPTION = "nskr_shipSwapConfirmPurchase";
	
	// Things that count for trade-in
	public static final Set<String> ALLOWED_IDS = new HashSet<>(Arrays.asList("nskr_electronics"));
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;
	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected MarketAPI market;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected CargoAPI playerCargo;
	protected PersonAPI person;
	protected FactionAPI faction;
	protected ShipAPI ship;
	protected float points;

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
			case "getShipForSale":
				setToPurchase(new ArrayList<String>());
				if (hasStock(PurchaseType.SHIP)) {
					showShipPicker();
					dialog.getVisualPanel().showPersonInfo(person, false);
				} else {
					showNoStock("\"Looks like we don't have any ships in stock at the moment.\"");
				}
				break;
			case "getGunForSale":
				setToPurchase(new ArrayList<String>());
				if (hasStock(PurchaseType.WEAPON)) {
					showGunPicker();
				} else {
					showNoStock("\"Looks like we don't have any weapons in stock at the moment.\"");
				}
				break;
			case "sell":
				selectArtifacts();
				break;
			case "confirmPurchase":
				purchase();
				break;
		}
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
		
		person = dialog.getInteractionTarget().getActivePerson();
		faction = person.getFaction();
		
		updatePointsInMemory(getPoints());
	}
	
	/**
	 * Updates available points in local memory.
	 * @param newPoints
	 */
	protected void updatePointsInMemory(float newPoints)
	{
		points = newPoints;
		memoryMap.get(MemKeys.LOCAL).set(POINTS_DISPLAY_KEY, points, 0);
		memoryMap.get(MemKeys.LOCAL).set(POINTS_STR_KEY, Misc.getWithDGS((int)points) + "", 0);
	}

	protected boolean hasStock(PurchaseType type) {
		for (PurchaseInfo item : getStock(market.getMemoryWithoutUpdate())) {
			if (item.type == type) return true;
		}
		return false;
	}

	// The handler rows of the purchase screens have no options of their own, so the command replaces the menu.
	protected void setOptions(String... labelsAndIds) {
		OptionPanelAPI options = dialog.getOptionPanel();
		options.clearOptions();
		for (int i = 0; i < labelsAndIds.length; i += 2) {
			options.addOption(labelsAndIds[i], labelsAndIds[i + 1]);
		}
		options.setShortcut(RETURN_OPTION, Keyboard.KEY_ESCAPE, false, false, false, true);
	}

	protected void showNoStock(String line) {
		text.addParagraph(line);
		setOptions("Back", RETURN_OPTION);
	}

	protected void showPickCancelled() {
		text.addParagraph("\"Don't want anything? Shame.\"");
		setOptions("Go back", RETURN_OPTION);
	}
	
	protected void selectArtifacts() {
		final CargoAPI copy = Global.getFactory().createCargo(false);
		
		for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
			if (isValid(stack)) {
				copy.addFromStack(stack);
			}
		}
		copy.sort();
		
		final float width = 310f;
		
		dialog.showCargoPickerDialog(StringHelper.getString("nskr_misc", "shipSwapSelect"),
				Misc.ucFirst("confirm"),
				Misc.ucFirst("cancel"),
						true, width, copy, new CargoPickerListener() {
			public void pickedCargo(CargoAPI cargo) {
				cargo.sort();
				for (CargoStackAPI stack : cargo.getStacksCopy()) {
					playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
					if (stack.isCommodityStack()) { // should be always, but just in case
						AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), (int) stack.getSize(), text);
					}
				}

				// put back in player cargo the ones we didn't sell
				int points = (int)getPointValue(cargo);
				
				if (points > 0) {
					float newPoints = addPoints(points);
					text.setFontSmallInsignia();
					String str = StringHelper.getStringAndSubstituteToken("nskr_misc", "shipSwapGainedPoints", "$points", Misc.getWithDGS(points) + "");
					text.addPara(str, Misc.getPositiveHighlightColor(), Misc.getHighlightColor(), Misc.getWithDGS(points) + "");
					text.setFontInsignia();

					Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

					updatePointsInMemory(newPoints);
				}
				
				FireBest.fire(null, dialog, memoryMap, SOLD_TRIGGER);
			}
			
			@Override
			public void cancelledCargoSelection() {
			}
			
			@Override
			public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {
			
				int points = (int)getPointValue(cargo);
				
				float pad = 3f;
				float small = 5f;
				float opad = 10f;

				panel.setParaOrbitronLarge();
				panel.addPara(Misc.ucFirst(faction.getDisplayName()), faction.getBaseUIColor(), opad);
				panel.setParaFontDefault();
				
				panel.addImage(faction.getLogo(), width * 1f, pad);

				String str = StringHelper.getStringAndSubstituteToken("nskr_misc", "shipSwapMsg", "$points", Misc.getWithDGS(points) + "");
				panel.addPara(str, 	opad * 1f, Misc.getHighlightColor(), Misc.getWithDGS(points) + "");
			}
		});

	}
	
	protected void showShipPicker() {
		List<FleetMemberAPI> stock = new ArrayList<>();
		for (PurchaseInfo item : getStock(market.getMemoryWithoutUpdate())) {
			if (item.type!=PurchaseType.SHIP) continue;
			stock.add(createHull(item.id));
		}

		dialog.showFleetMemberPickerDialog("Choose from the stock",
				Misc.ucFirst("Confirm"),
				Misc.ucFirst("Cancel"),
				5, 6, 120,true,true, stock, new FleetMemberPickerListener() {
					@Override
					public void pickedFleetMembers(List<FleetMemberAPI> members) {
						List<String> toPurchase = new ArrayList<>();
						for (FleetMemberAPI m : members){
							toPurchase.add(m.getHullSpec().getHullId());
						}
						showPicked(toPurchase);
					}

					@Override
					public void cancelledFleetMemberPicking() {
						showPickCancelled();
					}
				});
	}

	protected void showGunPicker() {
		final Color g = Misc.getGrayColor();

		final CargoAPI fakeCargo = Global.getFactory().createCargo(false);
		for (PurchaseInfo item : getStock(market.getMemoryWithoutUpdate())) {
			if (item.type != PurchaseType.WEAPON) continue;
			fakeCargo.addItems(CargoAPI.CargoItemType.WEAPONS, item.itemId, 1);
		}

		final float width = 310f;

		dialog.showCargoPickerDialog("Choose from the stock",
				Misc.ucFirst("Confirm"),
				Misc.ucFirst("Cancel"),
				true, width, fakeCargo, new CargoPickerListener() {

					public void pickedCargo(CargoAPI cargo) {
						List<String> toPurchase = new ArrayList<>();
						cargo.sort();
						for (CargoStackAPI stack : cargo.getStacksCopy()) {
							// The picker can return non-weapon stacks; they have no weapon spec and cost nothing.
							if (!stack.isWeaponStack()) continue;
							toPurchase.add(stack.getWeaponSpecIfWeapon().getWeaponId());
						}
						showPicked(toPurchase);
					}
					@Override
					public void cancelledCargoSelection() {
						showPickCancelled();
					}

					@Override
					public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {
						int points = 0;

						for (CargoStackAPI stack : cargo.getStacksCopy()) {
							if (!stack.isWeaponStack()) continue;
							points+=stack.getBaseValuePerUnit();
						}
						float pad = 3f;
						float small = 5f;
						float opad = 10f;

						panel.setParaOrbitronLarge();
						panel.addPara(Misc.ucFirst(faction.getDisplayName()), faction.getBaseUIColor(), opad);
						panel.setParaFontDefault();

						panel.addImage(faction.getLogo(), width * 1f, pad);

						panel.addPara("Cost "+Misc.getWithDGS(points)+" points. You have "+Misc.getWithDGS(getPoints()) + " points",
								opad * 1f, Misc.getHighlightColor(), Misc.getWithDGS(points), "");
						panel.addPara("Result "+Misc.getWithDGS(getPoints()-points) + " points",
								opad * 1f, g, Misc.getHighlightColor(), Misc.getWithDGS(getPoints()-points), "");
					}
				});
	}

	// Picker callbacks run after the command has returned, so they print the result and set the options themselves.
	protected void showPicked(List<String> toPurchase) {
		setToPurchase(toPurchase);
		if (toPurchase.isEmpty()) {
			showPickCancelled();
			return;
		}
		Color g = Misc.getGrayColor();
		text.setFontSmallInsignia();
		for (String id : toPurchase) {
			WeaponSpecAPI weapon = getWeaponSpec(id);
			if (weapon != null) {
				text.addParagraph(weapon.getWeaponName(), g);
			} else {
				text.addParagraph(createHull(id).getHullSpec().getHullName() + "-Class", g);
			}
		}
		String costStr = Misc.getWithDGS(getCost(toPurchase)) + "";
		text.addParagraph("This will cost " + costStr + " points.");
		text.highlightInLastPara(costStr);
		String pointsStr = Misc.getWithDGS((int) points) + "";
		text.addParagraph("You have " + pointsStr + " points.");
		text.highlightInLastPara(pointsStr);
		text.setFontInsignia();

		if (canAfford(toPurchase)) {
			setOptions("Make the purchase", CONFIRM_OPTION, "\"Never mind.\"", RETURN_OPTION);
		} else {
			text.addParagraph("You can not afford this transaction.");
			setOptions("Go back", RETURN_OPTION);
		}
	}

	protected boolean canAfford(List<String> toPurchase) {
		int cost = getCost(toPurchase);
		if (containsWeapon(toPurchase)) return points > cost;
		return points >= cost;
	}

	protected void purchase() {
		final Color h = Misc.getHighlightColor();
		final Color g = Misc.getGrayColor();

		MemoryAPI mem = market.getMemoryWithoutUpdate();
		List<PurchaseInfo> stock = getStock(mem);
		List<PurchaseInfo> toRemove = new ArrayList<>();
		List<String> toPurchase = getToPurchase();
		int cost = getCost(toPurchase);
		text.setFontSmallInsignia();
		for (String id : toPurchase) {
			//remove stock
			for (PurchaseInfo info : stock){
				if (info.getItemId().equals(id)){
					toRemove.add(info);
					break;
				}
			}
			//add items
			WeaponSpecAPI weapon = getWeaponSpec(id);
			if (weapon != null) {
				text.addPara("Purchased "+weapon.getWeaponName(),g,h,weapon.getWeaponName(),"");

				playerCargo.addItems(CargoAPI.CargoItemType.WEAPONS, weapon.getWeaponId(), 1);
				continue;
			}
			FleetMemberAPI member = createHull(id);
			text.addPara("Purchased "+member.getHullSpec().getHullName()+"-Class",g,h,member.getHullSpec().getHullName(),"");

			playerCargo.getFleetData().addFleetMember(member);
		}
		text.setFontInsignia();

		float newPoints = addPoints(-cost);
		updatePointsInMemory(newPoints);
		
		text.setFontSmallInsignia();

		String costStr = Misc.getWithDGS(cost) + "";
		String str = StringHelper.getStringAndSubstituteToken("nskr_misc", "shipSwapLostPoints", "$points", costStr);
		text.addPara(str, Misc.getNegativeHighlightColor(), Misc.getHighlightColor(), costStr);
		text.setFontInsignia();

		dialog.getVisualPanel().showPersonInfo(person, false);

		Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);
		
		// remove purchased from array
		stock.removeAll(toRemove);
		setStock(mem, stock, false);

		setOptions("Go back", RETURN_OPTION);
	}

	/**
	 * Purchase ids are hull ids or weapon ids; a weapon spec lookup tells them apart.
	 */
	protected static WeaponSpecAPI getWeaponSpec(String id) {
		try {
			return Global.getSettings().getWeaponSpec(id);
		} catch (RuntimeException ex) {
			return null;
		}
	}

	protected static FleetMemberAPI createHull(String hullId) {
		return Global.getFactory().createFleetMember(FleetMemberType.SHIP, hullId + "_empty");
	}

	protected static boolean containsWeapon(List<String> ids) {
		for (String id : ids) {
			if (getWeaponSpec(id) != null) return true;
		}
		return false;
	}

	protected static int getCost(List<String> ids) {
		int cost = 0;
		for (String id : ids) {
			WeaponSpecAPI weapon = getWeaponSpec(id);
			if (weapon != null) {
				cost += weapon.getBaseValue();
			} else {
				cost += createHull(id).getHullSpec().getBaseValue();
			}
		}
		return cost;
	}

	public static List<PurchaseInfo> getStock(MemoryAPI mem) {
		if (mem.contains(STOCK_ARRAY_KEY)) return (List<PurchaseInfo>)mem.get(STOCK_ARRAY_KEY);
		
		List<PurchaseInfo> ships = generateStock();
		setStock(mem, ships, true);
		return ships;
	}
	
	public static void setStock(MemoryAPI mem, List<PurchaseInfo> stock, boolean refreshTime) {
		float time = STOCK_KEEP_DAYS;
		if (!refreshTime && mem.contains(STOCK_ARRAY_KEY))
			time = mem.getExpire(STOCK_ARRAY_KEY);
		
		mem.set(STOCK_ARRAY_KEY, stock, time);
	}

	private ArrayList<String> getToPurchase() {
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(TO_PURCHASE_KEY)) {
			data.put(TO_PURCHASE_KEY, new ArrayList<String>());
		}
		return (ArrayList<String>) data.get(TO_PURCHASE_KEY);
	}

	private ArrayList<String>  setToPurchase(List<String> ids) {
		Map<String, Object> data = Global.getSector().getPersistentData();

		data.put(TO_PURCHASE_KEY, ids);
		return (ArrayList<String>) data.get(TO_PURCHASE_KEY);
	}
	
	public static List<PurchaseInfo> generateStock() {
		List<PurchaseInfo> ships = new ArrayList<>();
		Random random = getRandom();
		WeightedRandomPicker<PurchaseInfo> picker = new WeightedRandomPicker<>(random);
		boolean questFinished = KestevenQuest.kestevenEndingDone();
		for (ShipHullSpecAPI hull : Global.getSettings().getAllShipHullSpecs()) {
			//prot hulls
			if (!hull.hasTag("prot_light") && !hull.hasTag("prot_heavy")) continue;
			//heavy ships after quest line
			if (!questFinished && hull.hasTag("prot_heavy")) continue;
			
			String hullId = hull.getHullId();
			
			PurchaseInfo info = new PurchaseInfo(hullId, PurchaseType.SHIP,
					hull.getNameWithDesignationWithDashClass(), 
					hull.getBaseValue(),
					CargoAPI.CargoItemType.SPECIAL);
			
			info.itemId = hullId;
						
			
			picker.add(info, 1 * hull.getRarity());
		}
		for (FighterWingSpecAPI wing : Global.getSettings().getAllFighterWingSpecs()) {
			if (!wing.hasTag("prot")) continue;
			String wingId = wing.getId();
			
			PurchaseInfo info = new PurchaseInfo(wingId, PurchaseType.FIGHTER,
					wing.getWingName(), 
					wing.getBaseValue(),
					CargoAPI.CargoItemType.FIGHTER_CHIP);
			info.itemId = wingId;

			
			picker.add(info, 4 * wing.getRarity());
		}
		for (WeaponSpecAPI wep : Global.getSettings().getAllWeaponSpecs()) {
			if (!wep.hasTag("prot_wp")) continue;
			String weaponId = wep.getWeaponId();
			
			PurchaseInfo info = new PurchaseInfo(weaponId, PurchaseType.WEAPON,
					wep.getWeaponName(), 
					wep.getBaseValue(),
					CargoAPI.CargoItemType.WEAPONS);
			info.itemId = weaponId;
			picker.add(info, 4 * wep.getRarity());
		}
		
		int num = STOCK_COUNT_MIN + random.nextInt(STOCK_COUNT_MAX - STOCK_COUNT_MIN + 1);
		if (questFinished) num*=4f;
		for (int i = 0; i < num; i++)
		{
			if (picker.isEmpty()) continue;
			ships.add(picker.pickAndRemove());
		}
		Collections.sort(ships);
		return ships;
	}
	
	public static boolean isValid(CargoStackAPI stack)
	{
		String spec = stack.getCommodityId();
		if (spec == null) return false;
		String id = spec;
		return ALLOWED_IDS.contains(id);
	}
	
	public static float getPointValue(CargoAPI cargo)
	{
		float totalPoints = 0;
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			if (!isValid(stack)) continue;
			
			float points = getPointValue(stack);
			
			totalPoints += points;
		}
		return totalPoints;
	}
	
	public static float getPointValue(CargoStackAPI stack)
	{
		float points = 0, base = 0;

		base = stack.getBaseValuePerUnit();
		points = base;
		
		points *= stack.getSize();
		
		return points;
	}


	public static float addPoints(float points)
	{
		points += getPoints();
		Global.getSector().getPersistentData().put(POINTS_KEY, points);
		
		return points;
	}
	
	public static float getPoints()
	{
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(POINTS_KEY))
			data.put(POINTS_KEY, 0f);
		
		return (float)data.get(POINTS_KEY);
	}

	//pick the specific market
	public static boolean validMarket(MarketAPI market)
	{
		if (market==null) return false;
		if (!market.getFaction().getId().equals("kesteven")) return false;
		if (Global.getSector().getPlayerFaction().getRelationship("kesteven")<=-0.5f) return false;
		if (SectorLookup.asteriaOrOutpost()==null) return false;
		if (KestevenQuest.researchServicesClosed()) return false;
		return market.getId().equals(SectorLookup.asteriaOrOutpost().getId());
	}
	
	public static Random getRandom() {
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

			data.put(PERSISTENT_RANDOM_KEY, new Random(MathHelper.getSeedParsed()));
		}

		return (Random)data.get(PERSISTENT_RANDOM_KEY);
	}
	
	public static class PurchaseInfo implements Comparable<PurchaseInfo> {
		public String id;
		public String itemId;
		public PurchaseType type;
		public String name;
		public float cost;
		public ShipAPI ship;
		public CargoAPI.CargoItemType itemType;

		public PurchaseInfo(String id, PurchaseType type, String name, float cost, CargoAPI.CargoItemType itemType) {
			this.id = id;				
			this.type = type;
			this.name = name;
			this.cost = cost;
			this.itemType = itemType;
		}

		public String getItemId()
		{
			if (itemId != null) return itemId;
			switch (type) {
				case SHIP:
					return CargoAPI.CargoItemType.SPECIAL.toString();
				case FIGHTER:
					return CargoAPI.CargoItemType.FIGHTER_CHIP.toString();
				case WEAPON:
					return CargoAPI.CargoItemType.WEAPONS.toString();
			}
			return null;
		}

		@Override
		public int compareTo(PurchaseInfo other) {
			// ships first, then fighters, then weapons
			if (type != other.type)
				return type.compareTo(other.type);
			
			// descending cost order
			if (cost != other.cost) return Float.compare(other.cost, cost);
			
			return name.compareTo(other.name);
		}
	}
	
	public enum PurchaseType {
		SHIP, FIGHTER, WEAPON
	}
}
