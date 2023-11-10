//////////////////////
//Initially created by DarkRevenant and modified from Ship and Weapon Pack
//////////////////////
package scripts.kissa.LOST_SECTOR.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;

public interface nskr_FleetGenerator {

    //copy pasted from SWP
    static enum GeneratorFleetTypes {

        RAIDERS(new RaidersGen()),
        PATROL(new PatrolGen()),
        HUNTERS(new HuntersGen()),
        WAR(new WarGen()),
        DEFENSE(new DefenseGen()),
        CONVOY(new ConvoyGen()),
        BLOCKADE(new BlockadeGen()),
        INVASION(new InvasionGen());

        private final nskr_FleetGenerator gen;

        private GeneratorFleetTypes(nskr_FleetGenerator gen) {
            this.gen = gen;
        }

        FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            return gen.generate(api, side, faction, qf, opBonus, avgSMods, maxPts, seed, autoshit);
        }
    }

    FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit);

    static class RaidersGen implements nskr_FleetGenerator {

        @Override
        public FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 4);
            market.getStability().modifyFlat("fake", 10000);
            market.setFactionId(faction);
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
            FleetParamsV3 params = new FleetParamsV3(market,
                    new Vector2f(0, 0),
                    faction,
                    qf, // qualityOverride
                    "missionFleet",
                    maxPts * 0.75f, // combatPts
                    maxPts * 0.25f, // freighterPts
                    0f, // tankerPts
                    0f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f); // qualityMod
            params.withOfficers = false;
            params.ignoreMarketFleetSizeMult = true;
            params.forceAllowPhaseShipsEtc = true;
            params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
            params.random = new Random(seed);
            params.averageSMods = avgSMods;

            CampaignFleetAPI fleetEntity = FleetFactoryV3.createFleet(params);
            if (fleetEntity == null) {
                return null;
            }

            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = qf;
            p.seed = seed;
            p.mode = ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.averageSMods = avgSMods;
            p.factionId = faction;

            DefaultFleetInflater inflater = new DefaultFleetInflater(p);
            inflater.inflate(fleetEntity);

            return nskr_BaseRandomBattle.finishFleet(fleetEntity.getFleetData(), side, faction, api);
        }
    }

    static class PatrolGen implements nskr_FleetGenerator {

        @Override
        public FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 4);
            market.getStability().modifyFlat("fake", 10000);
            market.setFactionId(faction);
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
            FleetParamsV3 params = new FleetParamsV3(market,
                    new Vector2f(0, 0),
                    faction,
                    qf, // qualityOverride
                    "missionFleet",
                    maxPts, // combatPts
                    0f, // freighterPts
                    0f, // tankerPts
                    0f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f); // qualityMod
            params.withOfficers = false;
            params.ignoreMarketFleetSizeMult = true;
            params.forceAllowPhaseShipsEtc = true;
            params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
            params.random = new Random(seed);
            params.averageSMods = avgSMods;

            CampaignFleetAPI fleetEntity = FleetFactoryV3.createFleet(params);
            if (fleetEntity == null) {
                return null;
            }

            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = qf;
            p.seed = seed;
            p.mode = ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.averageSMods = avgSMods;
            p.factionId = faction;

            DefaultFleetInflater inflater = new DefaultFleetInflater(p);
            inflater.inflate(fleetEntity);

            return nskr_BaseRandomBattle.finishFleet(fleetEntity.getFleetData(), side, faction, api);
        }
    }

    static class HuntersGen implements nskr_FleetGenerator {

        @Override
        public FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 5);
            market.getStability().modifyFlat("fake", 10000);
            market.setFactionId(faction);
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
            FleetParamsV3 params = new FleetParamsV3(market,
                    new Vector2f(0, 0),
                    faction,
                    qf, // qualityOverride
                    "missionFleet",
                    maxPts, // combatPts
                    0f, // freighterPts
                    0f, // tankerPts
                    0f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f); // qualityMod
            params.withOfficers = false;
            params.ignoreMarketFleetSizeMult = true;
            params.forceAllowPhaseShipsEtc = true;
            params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
            params.random = new Random(seed);
            params.averageSMods = avgSMods;

            CampaignFleetAPI fleetEntity = FleetFactoryV3.createFleet(params);
            if (fleetEntity == null) {
                return null;
            }

            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = qf;
            p.seed = seed;
            p.mode = ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.averageSMods = avgSMods;
            p.factionId = faction;

            DefaultFleetInflater inflater = new DefaultFleetInflater(p);
            inflater.inflate(fleetEntity);

            return nskr_BaseRandomBattle.finishFleet(fleetEntity.getFleetData(), side, faction, api);
        }
    }

    static class WarGen implements nskr_FleetGenerator {

        @Override
        public FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 6);
            market.getStability().modifyFlat("fake", 10000);
            market.setFactionId(faction);
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
            FleetParamsV3 params = new FleetParamsV3(market,
                    new Vector2f(0, 0),
                    faction,
                    qf, // qualityOverride
                    "missionFleet",
                    maxPts, // combatPts
                    0f, // freighterPts
                    0f, // tankerPts
                    0f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f); // qualityMod
            params.withOfficers = false;
            params.ignoreMarketFleetSizeMult = true;
            params.forceAllowPhaseShipsEtc = true;
            params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
            params.random = new Random(seed);
            params.averageSMods = avgSMods;

            CampaignFleetAPI fleetEntity = FleetFactoryV3.createFleet(params);
            if (fleetEntity == null) {
                return null;
            }

            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = qf;
            p.seed = seed;
            p.mode = ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.averageSMods = avgSMods;
            p.factionId = faction;

            DefaultFleetInflater inflater = new DefaultFleetInflater(p);
            inflater.inflate(fleetEntity);

            return nskr_BaseRandomBattle.finishFleet(fleetEntity.getFleetData(), side, faction, api);
        }
    }

    static class DefenseGen implements nskr_FleetGenerator {

        @Override
        public FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 6);
            market.getStability().modifyFlat("fake", 10000);
            market.setFactionId(faction);
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
            FleetParamsV3 params = new FleetParamsV3(market,
                    new Vector2f(0, 0),
                    faction,
                    qf, // qualityOverride
                    "missionFleet",
                    maxPts, // combatPts
                    0f, // freighterPts
                    0f, // tankerPts
                    0f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f); // qualityMod
            params.withOfficers = false;
            params.ignoreMarketFleetSizeMult = true;
            params.forceAllowPhaseShipsEtc = true;
            params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
            params.random = new Random(seed);
            params.averageSMods = avgSMods;

            CampaignFleetAPI fleetEntity = FleetFactoryV3.createFleet(params);
            if (fleetEntity == null) {
                return null;
            }

            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = qf;
            p.seed = seed;
            p.mode = ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.averageSMods = avgSMods;
            p.factionId = faction;

            DefaultFleetInflater inflater = new DefaultFleetInflater(p);
            inflater.inflate(fleetEntity);

            return nskr_BaseRandomBattle.finishFleet(fleetEntity.getFleetData(), side, faction, api);
        }
    }

    static class ConvoyGen implements nskr_FleetGenerator {

        @Override
        public FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 4);
            market.getStability().modifyFlat("fake", 10000);
            market.setFactionId(faction);
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
            FleetParamsV3 params = new FleetParamsV3(market,
                    new Vector2f(0, 0),
                    faction,
                    qf, // qualityOverride
                    "missionFleet",
                    maxPts * 0.6f, // combatPts
                    maxPts * 0.1f, // freighterPts
                    maxPts * 0.1f, // tankerPts
                    maxPts * 0.1f, // transportPts
                    maxPts * 0.1f, // linerPts
                    0f, // utilityPts
                    0f); // qualityMod
            params.withOfficers = false;
            params.ignoreMarketFleetSizeMult = true;
            params.forceAllowPhaseShipsEtc = true;
            params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
            params.random = new Random(seed);
            params.averageSMods = avgSMods;

            CampaignFleetAPI fleetEntity = FleetFactoryV3.createFleet(params);
            if (fleetEntity == null) {
                return null;
            }

            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = qf;
            p.seed = seed;
            p.mode = ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.averageSMods = avgSMods;
            p.factionId = faction;

            DefaultFleetInflater inflater = new DefaultFleetInflater(p);
            inflater.inflate(fleetEntity);

            return nskr_BaseRandomBattle.finishFleet(fleetEntity.getFleetData(), side, faction, api);
        }
    }

    static class BlockadeGen implements nskr_FleetGenerator {

        @Override
        public FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 5);
            market.getStability().modifyFlat("fake", 10000);
            market.setFactionId(faction);
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
            FleetParamsV3 params = new FleetParamsV3(market,
                    new Vector2f(0, 0),
                    faction,
                    qf, // qualityOverride
                    "missionFleet",
                    maxPts * 0.8f, // combatPts
                    maxPts * 0.1f, // freighterPts
                    0f, // tankerPts
                    maxPts * 0.1f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f); // qualityMod
            params.withOfficers = false;
            params.ignoreMarketFleetSizeMult = true;
            params.forceAllowPhaseShipsEtc = true;
            params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
            params.random = new Random(seed);
            params.averageSMods = avgSMods;

            CampaignFleetAPI fleetEntity = FleetFactoryV3.createFleet(params);
            if (fleetEntity == null) {
                return null;
            }

            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = qf;
            p.seed = seed;
            p.mode = ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.averageSMods = avgSMods;
            p.factionId = faction;

            DefaultFleetInflater inflater = new DefaultFleetInflater(p);
            inflater.inflate(fleetEntity);

            return nskr_BaseRandomBattle.finishFleet(fleetEntity.getFleetData(), side, faction, api);
        }
    }

    static class InvasionGen implements nskr_FleetGenerator {

        @Override
        public FleetDataAPI generate(MissionDefinitionAPI api, FleetSide side, String faction, float qf, float opBonus, int avgSMods, int maxPts, long seed, boolean autoshit) {
            MarketAPI market = Global.getFactory().createMarket("fake", "fake", 6);
            market.getStability().modifyFlat("fake", 10000);
            market.setFactionId(faction);
            SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
            market.setPrimaryEntity(token);
            market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", FleetFactoryV3.BASE_QUALITY_WHEN_NO_MARKET);
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
            FleetParamsV3 params = new FleetParamsV3(market,
                    new Vector2f(0, 0),
                    faction,
                    qf, // qualityOverride
                    "missionFleet",
                    maxPts * 0.7f, // combatPts
                    0f, // freighterPts
                    0f, // tankerPts
                    maxPts * 0.3f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f); // qualityMod
            params.withOfficers = false;
            params.ignoreMarketFleetSizeMult = true;
            params.forceAllowPhaseShipsEtc = true;
            params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
            params.random = new Random(seed);
            params.averageSMods = avgSMods;

            CampaignFleetAPI fleetEntity = FleetFactoryV3.createFleet(params);
            if (fleetEntity == null) {
                return null;
            }

            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = qf;
            p.seed = seed;
            p.mode = ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.averageSMods = avgSMods;
            p.factionId = faction;

            DefaultFleetInflater inflater = new DefaultFleetInflater(p);
            inflater.inflate(fleetEntity);

            return nskr_BaseRandomBattle.finishFleet(fleetEntity.getFleetData(), side, faction, api);
        }
    }
}
