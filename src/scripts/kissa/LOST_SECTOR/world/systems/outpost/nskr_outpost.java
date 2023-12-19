package scripts.kissa.LOST_SECTOR.world.systems.outpost;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.simpleSystem;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.*;


public class nskr_outpost {

	//generates the outpost market, procedural

	public static final Random random = new Random();

	//Weights for the different types of locations our teasers can spawn in
	public static final LinkedHashMap<BaseThemeGenerator.LocationType, Float> WEIGHTS = new LinkedHashMap<>();
	static {
		WEIGHTS.put(BaseThemeGenerator.LocationType.IN_ASTEROID_BELT, 24f);
		WEIGHTS.put(BaseThemeGenerator.LocationType.IN_ASTEROID_FIELD, 24f);
		WEIGHTS.put(BaseThemeGenerator.LocationType.STAR_ORBIT, 8f);
		WEIGHTS.put(BaseThemeGenerator.LocationType.IN_RING, 16f);
		WEIGHTS.put(BaseThemeGenerator.LocationType.OUTER_SYSTEM, 8f);
		WEIGHTS.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 16f);
		WEIGHTS.put(BaseThemeGenerator.LocationType.GAS_GIANT_ORBIT, 16f);
	}
	public static final ArrayList<String> OUTPOST_NAME_LIST = new ArrayList<>();
	static {
		OUTPOST_NAME_LIST.add("Verge");
		OUTPOST_NAME_LIST.add("Fringe");
		OUTPOST_NAME_LIST.add("Boundary");
		OUTPOST_NAME_LIST.add("Perimeter");
		OUTPOST_NAME_LIST.add("Threshold");
		OUTPOST_NAME_LIST.add("Land's End");
		OUTPOST_NAME_LIST.add("Monitor");
		OUTPOST_NAME_LIST.add("Brink");
	}

	static void log(final String message) {
		Global.getLogger(nskr_outpost.class).info(message);
	}


	public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity,
										   ArrayList<SectorEntityToken> connectedEntities, String name, int size,
										   ArrayList<String> conditionList, ArrayList<ArrayList<String>> industryList, ArrayList<String> submarkets,
										   float tarrif, boolean freePort) {
		EconomyAPI globalEconomy = Global.getSector().getEconomy();
		String planetID = primaryEntity.getId();
		String marketID = planetID/* + "_market"*/;

		MarketAPI newMarket = Global.getFactory().createMarket(marketID, name, size);
		newMarket.setFactionId(factionID);
		newMarket.setPrimaryEntity(primaryEntity);
		newMarket.getTariff().modifyFlat("generator", tarrif);
		newMarket.getLocationInHyperspace().set(primaryEntity.getLocationInHyperspace());

		if (null != submarkets) {
			for (String market : submarkets) {
				newMarket.addSubmarket(market);
			}
		}

		if (null != conditionList) {
			for (String condition : conditionList) {
				newMarket.addCondition(condition);
			}
		}

		for (ArrayList<String> industryWithParam : industryList) {
			String industry = industryWithParam.get(0);
			if (industryWithParam.size() == 1) {
				newMarket.addIndustry(industry);
			} else {
				newMarket.addIndustry(industry, industryWithParam.subList(1, industryWithParam.size()));
			}
		}

		if (null != connectedEntities) {
			for (SectorEntityToken entity : connectedEntities) {
				newMarket.getConnectedEntities().add(entity);
			}
		}

		newMarket.setFreePort(freePort);
		globalEconomy.addMarket(newMarket, false);
		primaryEntity.setMarket(newMarket);
		primaryEntity.setFaction(factionID);

		if (null != connectedEntities) {
			for (SectorEntityToken entity : connectedEntities) {
				entity.setMarket(newMarket);
				entity.setFaction(factionID);
			}
		}
		return newMarket;
	}

	public static void generate(SectorAPI sector) {
		random.setSeed(sector.getSeedString().hashCode());

		StarSystemAPI system = getRandomSystemNearCore(random);
		system.addTag(Tags.THEME_CORE);
		system.addTag(Tags.THEME_CORE_POPULATED);
		system.addTag(Tags.THEME_SPECIAL);
		system.setProcgen(false);
		PlanetAPI star = system.getStar();

		//gate
		boolean hasGate = util.hasGate(system);
		if (!hasGate) {
			SectorEntityToken gate = system.addCustomEntity("nskr_outpost_gate", // unique id
					star.getName()+ " Gate", // name - if null, defaultName from custom_entities.json will be used
					"inactive_gate", // type of object, defined in custom_entities.json
					null); // faction
			gate.setCircularOrbit(star, (float)Math.random()*360f, MathUtils.getRandomNumberInRange(5000f,8000f), 450f);
		}
		//relay
		boolean hasRelay = util.hasRelay(system);
		if (!hasRelay) {
			SectorEntityToken relay = system.addCustomEntity("nskr_outpost_relay", // unique id
					null, // name - if null, defaultName from custom_entities.json will be used
					Entities.COMM_RELAY_MAKESHIFT, // type of object, defined in custom_entities.json
					"kesteven"); // faction
			relay.setCircularOrbit(star, (float) Math.random() * 360f, MathUtils.getRandomNumberInRange(5000f, 8000f), 450f);
		} else util.getRelay(system).setFaction("kesteven");

		WeightedRandomPicker<BaseThemeGenerator.EntityLocation> locs = BaseThemeGenerator.getLocations(null, system, null, 100f, WEIGHTS);
		//pick
		BaseThemeGenerator.EntityLocation loc = locs.pick();
		String name = OUTPOST_NAME_LIST.get(MathUtils.getRandomNumberInRange(0, OUTPOST_NAME_LIST.size() - 1));

		//station
		SectorEntityToken outpost = system.addCustomEntity("nskr_outpost", null, "station_side06", "kesteven");

		while (loc.orbit==null || loc.orbit.getFocus()==null){
			loc = locs.pick();
			//log("ERROR null orbit or focus");
		}
		OrbitAPI orb = loc.orbit;
		outpost.setCircularOrbitPointingDown(orb.getFocus(), (float)Math.random()*360f, MathUtils.getDistance(orb.computeCurrentLocation(),orb.getFocus().getLocation()), orb.getOrbitalPeriod());
		outpost.setName(name+" Outpost");
		outpost.setInteractionImage("illustrations", "space_bar");
		outpost.setCustomDescriptionId("nskr_outpost");
		//outpost.setId(new Random().nextLong()+"");

		//market
		MarketAPI outpostMarket = addMarketplace("kesteven", outpost,
				null,
				name+" Outpost", 4, // 2 industry limit
				new ArrayList<>(Arrays.asList(
						Conditions.POPULATION_4,
						Conditions.OUTPOST,
						Conditions.FRONTIER)),
				new ArrayList<>(Arrays.asList(
						new ArrayList<>(Arrays.asList(Industries.POPULATION)),
						new ArrayList<>(Arrays.asList(Industries.SPACEPORT)),
						new ArrayList<>(Arrays.asList(Industries.MILITARYBASE)),
						//new ArrayList<>(Arrays.asList(Industries.ORBITALWORKS, Items.PRISTINE_NANOFORGE)), // Industry
						//new ArrayList<>(Arrays.asList(Industries.FUELPROD)),
						//new ArrayList<>(Arrays.asList(Industries.MINING)),
						new ArrayList<>(Arrays.asList(Industries.HEAVYINDUSTRY)),
						new ArrayList<>(Arrays.asList(Industries.HEAVYBATTERIES)),
						new ArrayList<>(Arrays.asList(Industries.ORBITALSTATION_MID)))),
				new ArrayList<>(Arrays.asList(
						Submarkets.SUBMARKET_OPEN,
						Submarkets.GENERIC_MILITARY,
						Submarkets.SUBMARKET_BLACK,
						Submarkets.SUBMARKET_STORAGE)),
				0.3f,
				true
		);
		outpostMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		outpostMarket.reapplyIndustries();
		outpostMarket.getLocationInHyperspace().set(system.getLocation());

		PersonAPI admin = Global.getSector().getFaction("kesteven").createRandomPerson(random);
		if (nskr_modPlugin.IS_INDEVO){
			admin.getStats().setSkillLevel("indevo_planetary_operations", 1);
		} else admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
		admin.setRankId(Ranks.SPACE_ADMIRAL);
		admin.setPostId(Ranks.POST_STATION_COMMANDER);
		outpostMarket.setAdmin(admin);
		outpostMarket.getCommDirectory().addPerson(admin, 0);
		outpostMarket.addPerson(admin);

		system.addTag("theme_outpost");

		system.setEnteredByPlayer(true);
		Misc.setAllPlanetsSurveyed(system, true);
	}

	//no neutron stars, or close binaries
	//outpost gen
	public static StarSystemAPI getRandomSystemNearCore(Random random) {
		//ban tags
		List<String> banTags = new ArrayList<>();
		banTags.add(Tags.THEME_REMNANT_DESTROYED);
		banTags.add(Tags.THEME_REMNANT_RESURGENT);
		banTags.add(Tags.THEME_REMNANT_SUPPRESSED);
		banTags.add(Tags.THEME_UNSAFE);
		banTags.add(Tags.PK_SYSTEM);

		//pick types
		List<StarSystemGenerator.StarSystemType> banTypes = new ArrayList<>();
		banTypes.add(StarSystemGenerator.StarSystemType.BINARY_CLOSE);
		banTypes.add(StarSystemGenerator.StarSystemType.TRINARY_1CLOSE_1FAR);
		banTypes.add(StarSystemGenerator.StarSystemType.TRINARY_2CLOSE);

		simpleSystem simpleSystem = new simpleSystem(random, 1);
		simpleSystem.maxDistance = 20000f;
		simpleSystem.allowMarkets = false;
		simpleSystem.blacklistTags = banTags;
		simpleSystem.blacklistSystemTypes = banTypes;
		simpleSystem.pickOnlyInProcgen = true;

		StarSystemAPI pick = null;
		while (pick == null) {
			if (!simpleSystem.get().isEmpty()) {
				pick = simpleSystem.pick();
				log("Picked "+pick.getName());
			} else simpleSystem.maxDistance += 1000f;
		}
		return pick;
	}
}
