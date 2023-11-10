package scripts.kissa.LOST_SECTOR.world.systems.arcadia;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questFleets;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.util.util;
import scripts.kissa.LOST_SECTOR.world.systems.outpost.nskr_outpost;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class nskr_arcadia {

	public static final Color BELT_COLOR = new Color(116, 201, 255, 255);

	static void log(final String message) {
		Global.getLogger(nskr_arcadia.class).info(message);
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


	public static void generate(SectorAPI sector) {

		StarSystemAPI system = Global.getSector().getStarSystem("Arcadia");

		if (system != null) {

			PlanetAPI star = system.getStar();
			PlanetAPI asteria = system.addPlanet("nskr_asteria", star, "Asteria", "nskr_ice_desert", 180, 130, 3950, 140);
			asteria.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "sindria"));
			asteria.getSpec().setGlowColor(new Color(235,245,255,255));
			asteria.getSpec().setUseReverseLightForGlow(true);
			asteria.applySpecChanges();
			asteria.setInteractionImage("illustrations", "nskr_asteria");
			asteria.setCustomDescriptionId("nskr_asteria");

			system.addAsteroidBelt(asteria, 15, 400, 200f, 30f, 60f, Terrain.ASTEROID_BELT, "Frozen Belt");
			system.addRingBand(asteria, "misc", "rings_ice0", 256f, 1, BELT_COLOR, 256f, 500, 120f);

			SectorEntityToken asteriaStation = system.addCustomEntity("nskr_asteria_station",
					"Asteria Station", "station_side07", "kesteven");
			asteriaStation.setCircularOrbitPointingDown(system.getEntityById("nskr_asteria"), 45, 200, 30);
			asteriaStation.setCustomDescriptionId("nskr_asteria_station");

			//gravity well for asteria
			NascentGravityWellAPI well = Global.getSector().createNascentGravityWell(asteria, 50f);
			well.setColorOverride(new Color(135, 65, 255));
			LocationAPI hyper = Global.getSector().getHyperspace();
			hyper.addEntity(well);
			//note radius is the orbit distance in units in hyperspace (in system units / 10)
			well.autoUpdateHyperLocationBasedOnInSystemEntityAtRadius(asteria, 395f);

			MarketAPI asteriaMarket = addMarketplace("kesteven", asteria,
					new ArrayList<>(Arrays.asList(asteriaStation)),
					"Asteria", 6,
					new ArrayList<>(Arrays.asList(
							Conditions.POPULATION_6,
							Conditions.INDUSTRIAL_POLITY,
							Conditions.REGIONAL_CAPITAL,
							Conditions.ORE_MODERATE,
							Conditions.RARE_ORE_ABUNDANT,
							Conditions.POLLUTION,
							Conditions.COLD)),
					new ArrayList<>(Arrays.asList(
							new ArrayList<>(Arrays.asList(Industries.POPULATION)),
							new ArrayList<>(Arrays.asList(Industries.MEGAPORT, Items.FULLERENE_SPOOL)),
							new ArrayList<>(Arrays.asList(Industries.HIGHCOMMAND)),
							new ArrayList<>(Arrays.asList(Industries.ORBITALWORKS, Items.PRISTINE_NANOFORGE)), // Industry
							new ArrayList<>(Arrays.asList(Industries.REFINING)),
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
}
