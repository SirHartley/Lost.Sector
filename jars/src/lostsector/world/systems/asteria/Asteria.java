package lostsector.world.systems.asteria;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.bounties.HeliosSite;
import lostsector.helper.Ids;
import lostsector.helper.MathHelper;
import lostsector.helper.SectorLookup;
import lostsector.helper.SystemHelper;
import lostsector.helper.fleet.SystemPicker;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

public class Asteria {

	public static final Color BELT_COLOR = new Color(116, 201, 255, 255);
	public static final String STATION_ENTITY_ID = "nskr_asteria_station";
	public static final String RELAY_ENTITY_ID = "nskr_asteria_relay";

	// Vanilla places a gap orbit in the middle half of the gap, so this keeps Asteria's belt and
	// ring (out to about 630 su) clear of the neighbouring orbits.
	private static final float MIN_ORBIT_GAP = 3200f;
	private static final float[] SEARCH_DISTANCES = {20000f, 30000f, 45000f, Float.MAX_VALUE};

	static void log(final String message) {
		Global.getLogger(Asteria.class).info(message);
	}

	public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity,
										   ArrayList<SectorEntityToken> connectedEntities, String name, int size,
										   ArrayList<String> conditionList, ArrayList<ArrayList<String>> industryList, ArrayList<String> submarkets,
										   float tariff, boolean freePort) {

		EconomyAPI globalEconomy = Global.getSector().getEconomy();
		String planetID = primaryEntity.getId();
		String marketID = planetID;

		MarketAPI newMarket = Global.getFactory().createMarket(marketID, name, size);
		newMarket.setFactionId(factionID);
		newMarket.setPrimaryEntity(primaryEntity);
		newMarket.getTariff().modifyFlat("generator", tariff);
		newMarket.getLocationInHyperspace().set(primaryEntity.getLocationInHyperspace());

		if (null != submarkets) {
			for (String market : submarkets) {
				newMarket.addSubmarket(market);
			}
		}

		for (String condition : conditionList) {
			newMarket.addCondition(condition);
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

	// Corvus sectors: vanilla Arcadia. Asteria shares Syrinx's 200-day period and stays opposite it.
	public static void generate(SectorAPI sector) {
		StarSystemAPI system = sector.getStarSystem("Arcadia");
		if (system == null) return;

		addAsteria(system, system.getStar(), 10f, 3950f, 200f);
	}

	// Sectors without Arcadia, such as Nexerelin random sectors. Call it after Outpost.generate(): the
	// Outpost's system is then a core system with a market, which the picker skips.
	public static void generateInRandomSystemIfMissing(SectorAPI sector) {
		if (SectorLookup.getAsteria() != null) return;

		Random random = new Random(MathHelper.getSeedParsed());
		StarSystemAPI system = pickSystem(random);
		if (system == null) {
			log("no system for Asteria, Kesteven stays at the Outpost");
			return;
		}
		PlanetAPI star = system.getStar();

		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<>();
		weights.put(LocationType.STAR_ORBIT, 1f);
		List<EntityLocation> starOrbits = new ArrayList<>();
		for (EntityLocation loc : BaseThemeGenerator.getLocations(random, system, MIN_ORBIT_GAP, weights).getItems()) {
			if (loc.orbit != null && loc.orbit.getFocus() == star) starOrbits.add(loc);
		}

		float angle;
		float radius;
		float days;
		if (!starOrbits.isEmpty()) {
			OrbitAPI orbit = starOrbits.get(random.nextInt(starOrbits.size())).orbit;
			Vector2f point = orbit.computeCurrentLocation();
			angle = Misc.getAngleInDegrees(star.getLocation(), point);
			radius = Misc.getDistance(star.getLocation(), point);
			days = orbit.getOrbitalPeriod();
		} else {
			angle = random.nextFloat() * 360f;
			radius = BaseThemeGenerator.getOrbitalRadius(star) + MIN_ORBIT_GAP / 2f;
			days = radius / 20f;
		}
		addAsteria(system, star, angle, radius, days);

		if (SystemHelper.hasRelay(system)) {
			SystemHelper.getRelay(system).setFaction(Ids.KESTEVEN_FACTION_ID);
		} else {
			SectorEntityToken relay = system.addCustomEntity(RELAY_ENTITY_ID, null, Entities.COMM_RELAY_MAKESHIFT, Ids.KESTEVEN_FACTION_ID);
			relay.setCircularOrbitPointingDown(star, angle + 60f, radius, days);
		}

		system.addTag(Tags.THEME_CORE);
		system.addTag(Tags.THEME_CORE_POPULATED);
		system.addTag(Tags.THEME_SPECIAL);
		system.setProcgen(false);
		system.setEnteredByPlayer(true);
		Misc.setAllPlanetsSurveyed(system, true);

		log("Asteria placed in " + system.getName());
	}

	// Prefers a white dwarf near the core, as in the Asteria description, then widens the search.
	private static StarSystemAPI pickSystem(Random random) {
		List<String> banTags = new ArrayList<>();
		banTags.add(Tags.THEME_REMNANT);
		banTags.add(Tags.THEME_UNSAFE);
		banTags.add(Tags.PK_SYSTEM);

		List<String> banStars = new ArrayList<>();
		banStars.add(StarTypes.BLACK_HOLE);

		List<StarSystemType> banTypes = new ArrayList<>();
		banTypes.add(StarSystemType.BINARY_CLOSE);
		banTypes.add(StarSystemType.TRINARY_1CLOSE_1FAR);
		banTypes.add(StarSystemType.TRINARY_2CLOSE);

		List<String> banEntities = new ArrayList<>();
		banEntities.add(Ids.RD_FACILITY_ENTITY_ID);
		banEntities.add(Ids.BLACKSITE_ENTITY_ID);

		List<String> whiteDwarf = new ArrayList<>();
		whiteDwarf.add(StarTypes.WHITE_DWARF);

		SystemPicker picker = new SystemPicker(random, 2);
		picker.pickOnlyInProcgen = true;
		picker.blacklistTags = banTags;
		picker.blacklistStars = banStars;
		picker.blacklistSystemTypes = banTypes;
		picker.blacklistEntities = banEntities;
		// Nexerelin random sectors place the Mothership planets before Asteria.
		SectorEntityToken mothership = HeliosSite.base();
		if (mothership != null && mothership.getStarSystem() != null) {
			picker.blacklistSystems.add(mothership.getStarSystem());
		}

		for (float maxDistance : SEARCH_DISTANCES) {
			picker.maxDistance = maxDistance;
			for (boolean preferWhiteDwarf : new boolean[]{true, false}) {
				picker.pickStars = preferWhiteDwarf ? whiteDwarf : new ArrayList<String>();
				picker.enforceSystemStarType = preferWhiteDwarf;

				List<StarSystemAPI> systems = new ArrayList<>();
				for (StarSystemAPI system : picker.get()) {
					if (!hasEnigmaFleet(system)) systems.add(system);
				}
				if (!systems.isEmpty()) return systems.get(random.nextInt(systems.size()));
			}
		}
		return null;
	}

	// Dormant Enigma fleets are placed before this runs in Nexerelin random sectors.
	private static boolean hasEnigmaFleet(StarSystemAPI system) {
		for (CampaignFleetAPI fleet : system.getFleets()) {
			if (Ids.ENIGMA_FACTION_ID.equals(fleet.getFaction().getId())) return true;
		}
		return false;
	}

	private static void addAsteria(StarSystemAPI system, PlanetAPI star, float angle, float orbitRadius, float orbitDays) {
		PlanetAPI asteria = system.addPlanet(Ids.ASTERIA_ENTITY_ID, star, "Asteria", "nskr_ice_desert", angle, 130, orbitRadius, orbitDays);
		asteria.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "sindria"));
		asteria.getSpec().setGlowColor(new Color(235,245,255,255));
		asteria.getSpec().setUseReverseLightForGlow(true);
		asteria.applySpecChanges();
		asteria.setInteractionImage("illustrations", "nskr_asteria");
		asteria.setCustomDescriptionId("nskr_asteria");

		system.addAsteroidBelt(asteria, 15, 400, 200f, 30f, 60f, Terrain.ASTEROID_BELT, "Frozen Belt");
		system.addRingBand(asteria, "misc", "rings_ice0", 256f, 1, BELT_COLOR, 256f, 500, 120f);

		SectorEntityToken asteriaStation = system.addCustomEntity(STATION_ENTITY_ID,
				"Asteria Station", "station_side07", Ids.KESTEVEN_FACTION_ID);
		asteriaStation.setCircularOrbitPointingDown(asteria, 45, 200, 30);
		asteriaStation.setCustomDescriptionId(STATION_ENTITY_ID);

		NascentGravityWellAPI well = Global.getSector().createNascentGravityWell(asteria, 50f);
		well.setColorOverride(new Color(135, 65, 255));
		LocationAPI hyper = Global.getSector().getHyperspace();
		hyper.addEntity(well);
		// The radius is in hyperspace units: the in-system orbit radius / 10.
		well.autoUpdateHyperLocationBasedOnInSystemEntityAtRadius(asteria, orbitRadius / 10f);

		MarketAPI asteriaMarket = addMarketplace(Ids.KESTEVEN_FACTION_ID, asteria,
				new ArrayList<>(Arrays.asList(asteriaStation)),
				"Asteria", 6,
				new ArrayList<>(Arrays.asList(
						Conditions.POPULATION_6,
						Conditions.ORE_MODERATE,
						Conditions.RARE_ORE_ABUNDANT,
						Conditions.POLLUTION,
						Conditions.COLD)),
				new ArrayList<>(Arrays.asList(
						new ArrayList<>(Arrays.asList(Industries.POPULATION)),
						new ArrayList<>(Arrays.asList(Industries.MEGAPORT)),
						new ArrayList<>(Arrays.asList(Industries.MILITARYBASE)),
						new ArrayList<>(Arrays.asList(Industries.HEAVYINDUSTRY, Items.CORRUPTED_NANOFORGE)),
						new ArrayList<>(Arrays.asList(Industries.LIGHTINDUSTRY)),
						new ArrayList<>(Arrays.asList(Industries.HEAVYBATTERIES)),
						new ArrayList<>(Arrays.asList(Industries.STARFORTRESS_MID)))),
				new ArrayList<>(Arrays.asList(
						Submarkets.SUBMARKET_OPEN,
						Submarkets.GENERIC_MILITARY,
						Submarkets.SUBMARKET_BLACK,
						Submarkets.SUBMARKET_STORAGE)),
				0.3f,
				false
		);
		asteriaMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
	}
}
