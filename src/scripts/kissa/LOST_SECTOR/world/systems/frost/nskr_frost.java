//////////////////////
//parts initially created by DarkRevenant and modified from Underworld
//////////////////////
package scripts.kissa.LOST_SECTOR.world.systems.frost;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.CoreLifecyclePluginImpl;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import indevo.industries.artillery.utils.ArtilleryStationPlacer;
import org.jetbrains.annotations.Nullable;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.util.ids;
import scripts.kissa.LOST_SECTOR.util.util;
import indevo.ids.Ids;
import indevo.industries.artillery.conditions.ArtilleryStationCondition;
import indevo.industries.artillery.entities.ArtilleryStationEntityPlugin;
import indevo.industries.artillery.entities.WatchtowerEntityPlugin;
import indevo.industries.artillery.scripts.ArtilleryStationScript;
import indevo.industries.artillery.scripts.CampaignAttackScript;
import indevo.utils.ModPlugin;
import indevo.utils.helper.Settings;


import java.awt.Color;
import java.util.*;


public class nskr_frost {

	//very scuffed, but it works

	public static final Color STAR_LIGHT_COLOR = new Color(163, 225, 255, 255);
	public static final Color GLACIER_COLOR = new Color(116, 201, 255, 255);

	public static final Random random = new Random();

	//Weights for the different types of locations our teasers can spawn in
	public static final LinkedHashMap<BaseThemeGenerator.LocationType, Float> WEIGHTS = new LinkedHashMap<>();
	static {
		WEIGHTS.put(BaseThemeGenerator.LocationType.IN_ASTEROID_BELT, 24f);
		WEIGHTS.put(BaseThemeGenerator.LocationType.IN_ASTEROID_FIELD, 8f);
		WEIGHTS.put(BaseThemeGenerator.LocationType.STAR_ORBIT, 12f);
		WEIGHTS.put(BaseThemeGenerator.LocationType.NEAR_STAR, 10f);
		WEIGHTS.put(BaseThemeGenerator.LocationType.IN_RING, 16f);
		WEIGHTS.put(BaseThemeGenerator.LocationType.OUTER_SYSTEM, 8f);
		WEIGHTS.put(BaseThemeGenerator.LocationType.PLANET_ORBIT, 6f);
	}
	public static final String NAME_KEY = "$nskr_frostName";
	public static final ArrayList<String> SYS_NAME_LIST = new ArrayList<>();
	static {
		SYS_NAME_LIST.add("Frostbite");
		SYS_NAME_LIST.add("Newfoundland");
		SYS_NAME_LIST.add("Greenland");
		SYS_NAME_LIST.add("Antarctica");
		SYS_NAME_LIST.add("Permafrost");
		SYS_NAME_LIST.add("Hailstone");
		SYS_NAME_LIST.add("Archangel");
		SYS_NAME_LIST.add("Inari");
	}
	public static final ArrayList<String> DERELICT_FACTIONS = new ArrayList<>();
	static {
		DERELICT_FACTIONS.add(Factions.HEGEMONY);
		DERELICT_FACTIONS.add(Factions.LUDDIC_CHURCH);
		DERELICT_FACTIONS.add(Factions.TRITACHYON);
		DERELICT_FACTIONS.add(Factions.PERSEAN);
		DERELICT_FACTIONS.add(Factions.INDEPENDENT);
	}
	public static final List<Pair<String, Float>> ROLES = new ArrayList<>();
	static {
		ROLES.add(new Pair<>(ShipRoles.COMBAT_SMALL, 12f));
		ROLES.add(new Pair<>(ShipRoles.COMBAT_MEDIUM, 10f));
		ROLES.add(new Pair<>(ShipRoles.COMBAT_LARGE, 8f));
		ROLES.add(new Pair<>(ShipRoles.COMBAT_CAPITAL, 6f));
		ROLES.add(new Pair<>(ShipRoles.CARRIER_MEDIUM, 8f));
		ROLES.add(new Pair<>(ShipRoles.CARRIER_LARGE, 4f));
		ROLES.add(new Pair<>(ShipRoles.FREIGHTER_LARGE, 6f));
		ROLES.add(new Pair<>(ShipRoles.FREIGHTER_MEDIUM, 4f));
	}
	public static final ArrayList<String> RUINS = new ArrayList<>();
	static {
		RUINS.add(Conditions.RUINS_VAST);
		RUINS.add(Conditions.RUINS_EXTENSIVE);
		RUINS.add(Conditions.RUINS_SCATTERED);
		RUINS.add(Conditions.RUINS_WIDESPREAD);
	}

	static void log(final String message) {
		Global.getLogger(nskr_frost.class).info(message);
	}

	public static String pickName() {
		String name = null;

		name = SYS_NAME_LIST.get(MathUtils.getRandomNumberInRange(0, SYS_NAME_LIST.size() - 1));
		log("Frost picked "+name);
		return name;
	}

	public static String getName()
	{
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(NAME_KEY)) data.put(NAME_KEY, pickName());

		return (String)data.get(NAME_KEY);
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

	public static void generatePt2(SectorAPI sector) {

		StarSystemAPI system = sector.getStarSystem(getName());
		SectorEntityToken heart = sector.getEntityById("nskr_heart");
		//MarketAPI heartmarket = heart.getMarket();

		MarketAPI heartmarket = addMarketplace("enigma", heart,
				null,
				"Frozen Heart", 5, // 3 industry limit
				null,
				new ArrayList<>(Arrays.asList(
						new ArrayList<>(Arrays.asList(Industries.POPULATION, Commodities.ALPHA_CORE)),
						new ArrayList<>(Arrays.asList(Industries.SPACEPORT, Commodities.ALPHA_CORE)),
						new ArrayList<>(Arrays.asList(Industries.HIGHCOMMAND, Commodities.ALPHA_CORE)),
						new ArrayList<>(Arrays.asList(Industries.ORBITALWORKS, Items.PRISTINE_NANOFORGE, Commodities.ALPHA_CORE)), // Industry
						new ArrayList<>(Arrays.asList(Industries.FUELPROD, Commodities.ALPHA_CORE)),
						new ArrayList<>(Arrays.asList(Industries.LIGHTINDUSTRY, Commodities.ALPHA_CORE)),
						new ArrayList<>(Arrays.asList(Industries.HEAVYBATTERIES, Commodities.ALPHA_CORE)),
						new ArrayList<>(Arrays.asList(Industries.STARFORTRESS, Commodities.ALPHA_CORE)))),
				new ArrayList<String>(),
				0.3f,
				false
		);

		heartmarket.setHidden(true);
		heartmarket.setInvalidMissionTarget(true);
		heartmarket.setEconGroup(heartmarket.getId());

		heartmarket.addCondition(Conditions.POPULATION_5);
		heartmarket.addCondition(Conditions.OUTPOST);
		heartmarket.addCondition("nskr_enigmaPop");
		heartmarket.addCondition(Conditions.FRONTIER);
		heartmarket.addCondition(Conditions.COLD);
		heartmarket.addCondition(Conditions.RUINS_VAST);

		heartmarket.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, true);
		heartmarket.getMemoryWithoutUpdate().set("$nex_uninvadable", true);
		heartmarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

		heartmarket.reapplyIndustries();

		heartmarket.getLocationInHyperspace().set(system.getLocation());

	}

	public static void pickLocation(SectorAPI sector, StarSystemAPI system) {
		float radius = system.getMaxRadiusInHyperspace() + 200f;
		try_again:
		for (int i = 0; i < 100; i++) {
			Vector2f loc = new Vector2f(MathUtils.getRandomNumberInRange(33000f, 40000f), 0f);
			VectorUtils.rotate(loc, MathUtils.getRandomNumberInRange(0, 360), loc);

			for (LocationAPI location : sector.getAllLocations()) {
				if (location instanceof StarSystemAPI) {
					float otherRadius = ((StarSystemAPI) location).getMaxRadiusInHyperspace();
					if (MathUtils.getDistance(location.getLocation(), loc) < radius + otherRadius) {
						continue try_again;
					}
				}
			}

			system.getLocation().set(loc.x, loc.y);
			break;
		}
	}

	public static void generate(SectorAPI sector) {
		random.setSeed(sector.getSeedString().hashCode());

		StarSystemAPI system = sector.createStarSystem(getName());

		system.setLightColor(STAR_LIGHT_COLOR);
		system.setDoNotShowIntelFromThisLocationOnMap(true);

		system.setBackgroundTextureFilename("graphics/backgrounds/nskr_frost.jpg");

		system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "nskr_frost_theme");

		PlanetAPI star = system.initStar("nskr_frost", StarTypes.RED_DWARF, 400f, 200f);

		PlanetAPI bleak = system.addPlanet("nskr_bleak", star, "Bleak", "barren", 120, 90, 2500, 100);
		bleak.getSpec().setTilt(10f);
		bleak.applySpecChanges();

		PlanetAPI glacier = system.addPlanet("nskr_glacier", star, "Glacier", "tundra", 250, 180, 5000, 300);
		glacier.getSpec().setAtmosphereColor(GLACIER_COLOR);
		glacier.getSpec().setTilt(25f);
		glacier.applySpecChanges();

		PlanetAPI siberia = system.addPlanet("nskr_siberia", star, "Siberia", "cryovolcanic", 10, 230, 8500, 750);
		//shiver.applySpecChanges();

		PlanetAPI shiver = system.addPlanet("nskr_shiver", star, "Shiver", "frozen", 80, 150, 11500, 1150);
		//shiver.applySpecChanges();

		PlanetAPI algor = system.addPlanet("nskr_algor", star, "Algor", "frozen", 300, 60, 15000, 2350);
		//algor.applySpecChanges();

		system.addAsteroidBelt(star, 150, 3500, 350, 450, 500, Terrain.ASTEROID_BELT, "Frozen Belt");
		system.addRingBand(star, "misc", "rings_ice0", 256f, 1, GLACIER_COLOR, 256f, 3500, 200f);
		system.addRingBand(star, "misc", "rings_ice0", 256f, 1, GLACIER_COLOR, 256f, 11000, 800f);

		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("nskr_frost_jumpPoint", getName() + " Jump-point");
		OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 230, 4500, 300);
		jumpPoint.setRelatedPlanet(glacier);
		jumpPoint.setOrbit(orbit);
		jumpPoint.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint);

		//SectorEntityToken frost_location = system.addCustomEntity(null,null, "stable_location", Factions.NEUTRAL);
		//rost_location.setCircularOrbitPointingDown( star, 90 + 60, 3000, 200);
		//gate
		SectorEntityToken gate = system.addCustomEntity("nskr_frost_gate", // unique id
				getName() + " Gate", // name - if null, defaultName from custom_entities.json will be used
				"inactive_gate", // type of object, defined in custom_entities.json
				null); // faction
		gate.setCircularOrbit(star, 200, 6200, 405);
		//relay
		SectorEntityToken relay = system.addCustomEntity("nskr_frost_relay", // unique id
				getName() + " Relay", // name - if null, defaultName from custom_entities.json will be used
				Entities.COMM_RELAY, // type of object, defined in custom_entities.json
				"enigma"); // faction
		relay.setCircularOrbit(star, 150, 3000, 200);

		//mission safe
		for (PlanetAPI p : system.getPlanets()){
			p.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
		}

		float minGap = 50f;
		//DORMANT
		for (int x = 0; x < 8; x++) {
			util.addDormant(util.getRandomLocationInSystem(system, false, true, new Random()), "enigma", 5f, 100f, 0.50f, 0.25f, 0.75f, 0f, 0, 0);
		}
		//////DERELICT TIME
		float recoveryChance = 0.25f;
		for (int x = 0; x < 30; x++) {
			addDerelict(system, pickRandomVariant(randomFaction()), randomPlaceToSpawn(system, minGap, WEIGHTS).orbit, randomCondition(), Math.random()<recoveryChance, null);
		}for (int x = 0; x < 2; x++) {
			addDerelict(system, "nskr_epoch_e_hev", randomPlaceToSpawn(system, minGap, WEIGHTS).orbit, randomCondition(), Math.random()<recoveryChance, null);
			addDerelict(system, "nskr_muninn_e_std", randomPlaceToSpawn(system, minGap, WEIGHTS).orbit, randomCondition(), Math.random()<recoveryChance, null);
			addDerelict(system, "nskr_minokawa_e_hev", randomPlaceToSpawn(system, minGap, WEIGHTS).orbit, randomCondition(), Math.random()<recoveryChance, null);
			addDerelict(system, "nskr_torpor_e_sup", randomPlaceToSpawn(system, minGap, WEIGHTS).orbit, randomCondition(), Math.random()<recoveryChance, null);
			addDerelict(system, "nskr_nemesis_e_std", randomPlaceToSpawn(system, minGap, WEIGHTS).orbit, randomCondition(), Math.random()<recoveryChance, null);
			addDerelict(system, "nskr_warfare_e_std", randomPlaceToSpawn(system, minGap, WEIGHTS).orbit, randomCondition(), Math.random()<recoveryChance, null);
		}
			addDerelict(system, "nskr_torpor_std", randomPlaceToSpawn(system, minGap, WEIGHTS).orbit, randomCondition(), Math.random()<recoveryChance, null);
			addDerelict(system, "nskr_widow_std", randomPlaceToSpawn(system, minGap, WEIGHTS).orbit, randomCondition(), Math.random()<recoveryChance, null);
			addDerelict(system, "nskr_epoch_std", randomPlaceToSpawn(system, minGap, WEIGHTS).orbit, randomCondition(), Math.random()<recoveryChance, null);
			addDerelict(system, "nskr_nemesis_std", randomPlaceToSpawn(system, minGap, WEIGHTS).orbit, randomCondition(), Math.random()<recoveryChance, null);
			addDerelict(system, "nskr_muninn_std", randomPlaceToSpawn(system, minGap, WEIGHTS).orbit, randomCondition(), Math.random()<recoveryChance, null);

		DebrisFieldTerrainPlugin.DebrisFieldParams params_frost_main = new DebrisFieldTerrainPlugin.DebrisFieldParams(
				350f, // field radius - should not go above 1000 for performance reasons
				1.2f, // density, visual - affects number of debris pieces
				10000000f, // duration in days
				0f); // days the field will keep generating glowing pieces
		params_frost_main.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
		params_frost_main.baseSalvageXP = 500; // base XP for scavenging in field

		SectorEntityToken frost_main1 = Misc.addDebrisField(system, params_frost_main, StarSystemGenerator.random);
		frost_main1.setSensorProfile(1000f);
		frost_main1.setDiscoverable(true);
		frost_main1.setCircularOrbit(star, 220f, 4800, 300f);
		frost_main1.setId("nskr_frost_main_debrisBelt");

		SectorEntityToken frost_main2 = Misc.addDebrisField(system, params_frost_main, StarSystemGenerator.random);
		frost_main2.setSensorProfile(1000f);
		frost_main2.setDiscoverable(true);
		frost_main2.setCircularOrbit(star, 110f, 5200, 330f);
		frost_main2.setId("nskr_frost_main_debrisBelt2");

		SectorEntityToken frost_main3 = Misc.addDebrisField(system, params_frost_main, StarSystemGenerator.random);
		frost_main3.setSensorProfile(1000f);
		frost_main3.setDiscoverable(true);
		frost_main3.setCircularOrbit(star, 200f, 6200, 405f);
		frost_main3.setId("nskr_frost_main_debrisBelt3");

		addStationDerelict("station_mining", system, star, 200, 3500, 365f);

		addStationDerelict("station_research", system, siberia, 100, 300, 65f);

		addStationDerelict("orbital_habitat", system, glacier, 150, 300, 35f);

		addStationDerelict("nskr_enigmabase", system, star, 150, 2000, 90f);
		addStationDerelict("nskr_enigmabase", system, shiver, 250, 400, 95f);

		//weapons cache
		SectorEntityToken stationDerelict4 = DerelictThemeGenerator.addSalvageEntity(system, "weapons_cache_high", Factions.NEUTRAL);
		stationDerelict4.setId("nskr_frost_derelict4");
		stationDerelict4.setCircularOrbit(algor, 150, 200, 95f);

		SectorEntityToken stationDerelict5 = DerelictThemeGenerator.addSalvageEntity(system, "weapons_cache_high", Factions.NEUTRAL);
		stationDerelict5.setId("nskr_frost_derelict5");
		stationDerelict5.setCircularOrbit(algor, 50, 200, 95f);

		WeightedRandomPicker<BaseThemeGenerator.EntityLocation> locs = BaseThemeGenerator.getLocations(null, system, null, 100f, WEIGHTS);
		BaseThemeGenerator.EntityLocation loc = locs.pick();

		BaseThemeGenerator.AddedEntity hearts = BaseThemeGenerator.addNonSalvageEntity(system, loc, "station_sporeship_derelict", "enigma");
		SectorEntityToken heart = hearts.entity;
		//station
		heart.setId("nskr_heart");
		heart.setName("Frozen Heart");
		heart.setCircularOrbitPointingDown(star, 90, 5400, 330);
		heart.setInteractionImage("illustrations", "nskr_heart");
		heart.setCustomDescriptionId("nskr_station_heart");
		heart.setSensorProfile(2500f);
		heart.setDiscoveryXP(25000f);
		heart.setDiscoverable(true);

		//set random loc
		pickLocation(sector, system);

		system.autogenerateHyperspaceJumpPoints(true, false);

		HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(plugin);

		float minRadius = plugin.getTileSize() * 2f;
		float radius = system.getMaxRadiusInHyperspace();
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);

		if (system.hasTag(Tags.THEME_CORE)) {
		system.removeTag(Tags.THEME_CORE);
		}
		if (system.hasTag(Tags.THEME_CORE_POPULATED)) {
		system.removeTag(Tags.THEME_CORE_POPULATED);
		}
		if (system.hasTag(Tags.THEME_CORE_UNPOPULATED)) {
		system.removeTag(Tags.THEME_CORE_UNPOPULATED);
		}
		system.addTag(Tags.THEME_SPECIAL);
		system.addTag(Tags.THEME_UNSAFE);
		system.addTag(Tags.THEME_HIDDEN);
		system.addTag("theme_enigma");
		system.getMemoryWithoutUpdate().set("$nex_do_not_colonize", true);

		//music mem key
		for(SectorEntityToken e : system.getAllEntities()){
			e.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY, true);
		}
    }

	public static void generateRuins(StarSystemAPI system){
		for (PlanetAPI planet : system.getPlanets()){
			if (planet.isStar() || planet.isGasGiant()) continue;
			if (planet.getMarket().hasCondition(Conditions.RUINS_SCATTERED) || planet.getMarket().hasCondition(Conditions.RUINS_WIDESPREAD) || planet.getMarket().hasCondition(Conditions.RUINS_EXTENSIVE) || planet.getMarket().hasCondition(Conditions.RUINS_VAST)){
				continue;
			}
			String ruins = randomRuins();
			planet.getMarket().addCondition(ruins);

			CoreLifecyclePluginImpl.addRuinsJunk(planet);

			planet.getMarket().setPlanetConditionMarketOnly(true);
			planet.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.NONE);
		}
	}
	public static String randomRuins() {
		String ruin = RUINS.get(MathUtils.getRandomNumberInRange(0, RUINS.size()-1));

		return ruin;
	}

	public static BaseThemeGenerator.EntityLocation randomPlaceToSpawn(StarSystemAPI system, float minGap, LinkedHashMap<BaseThemeGenerator.LocationType, Float> Weights){

		WeightedRandomPicker<BaseThemeGenerator.EntityLocation> validPoints = BaseThemeGenerator.getLocations(new Random(), system, minGap, Weights);

		return validPoints.pick();
	}

	public static String pickRandomVariant(String factionId) {
		FactionAPI faction = Global.getSector().getFaction(factionId);
		ArrayList<String> variants = new ArrayList<>();
		while (variants.isEmpty()) {
			variants = new ArrayList<>(faction.getVariantsForRole(randomRole()));
		}

		String variant = variants.get(MathUtils.getRandomNumberInRange(0,variants.size()-1));
		return variant;
	}

	public static String randomRole() {
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
		for (Pair<String,Float> s : ROLES){
			picker.add(s.one,s.two);
		}
		String role = picker.pick();

		return role;
	}

	public static String randomFaction() {
	String faction = DERELICT_FACTIONS.get(MathUtils.getRandomNumberInRange(0, DERELICT_FACTIONS.size()-1));

	return faction;
	}

	public static ShipRecoverySpecial.ShipCondition randomCondition() {
		float condition = (float) Math.random();
		ShipRecoverySpecial.ShipCondition shipCondition;
		if (condition < 0.3f) {
			shipCondition = ShipRecoverySpecial.ShipCondition.WRECKED;
		} else if (condition < 0.6f) {
			shipCondition = ShipRecoverySpecial.ShipCondition.BATTERED;
		} else if (condition < 0.9f) {
			shipCondition = ShipRecoverySpecial.ShipCondition.AVERAGE;
		} else {
			shipCondition = ShipRecoverySpecial.ShipCondition.GOOD;
		}
	return shipCondition;
	}

	public static void addDerelict(StarSystemAPI system, String variantId, OrbitAPI orbit,
								   ShipRecoverySpecial.ShipCondition condition, boolean recoverable,
								   @Nullable DefenderDataOverride defenders) {

		DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), false);
		SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
		ship.setDiscoverable(true);

		ship.setOrbit(orbit);

		if (recoverable) {
			SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
			Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
		}
		if (defenders != null) {
			Misc.setDefenderOverride(ship, defenders);
		}
	}

	private static SectorEntityToken addStationDerelict(String typeId, StarSystemAPI system, SectorEntityToken orbitFocus, float angle, float orbitRadius, float orbitDays){
		SectorEntityToken stationDerelict = DerelictThemeGenerator.addSalvageEntity(system, typeId, Factions.NEUTRAL);
		stationDerelict.setId("nskr_enigmabase_"+new Random().nextLong());
		stationDerelict.setCircularOrbitPointingDown(orbitFocus,angle,orbitRadius,orbitDays);
		Misc.setDefenderOverride(stationDerelict, new DefenderDataOverride("enigma", 0.75f, 50f, 120f));
		return stationDerelict;
	}
}
