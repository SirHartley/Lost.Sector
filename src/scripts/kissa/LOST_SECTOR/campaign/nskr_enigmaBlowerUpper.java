package scripts.kissa.LOST_SECTOR.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.population.CoreImmigrationPluginImpl;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import scripts.kissa.LOST_SECTOR.campaign.intel.nskr_frostIntel;
import scripts.kissa.LOST_SECTOR.campaign.intel.nskr_hintManager;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.fleetUtil;
import scripts.kissa.LOST_SECTOR.util.ids;
import scripts.kissa.LOST_SECTOR.util.util;
import scripts.kissa.LOST_SECTOR.world.systems.frost.nskr_frost;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class nskr_enigmaBlowerUpper extends BaseCampaignEventListener implements EveryFrameScript  {

    nskr_saved<Boolean> firstTime;
    boolean doOnce = false;
    //CampaignFleetAPI pf;
    private nskr_frostIntel intel = null;
    private float counter = 0f;
    public static final String HINT_KEY = "HINT_FROST";
    public static final String PERSISTENT_RANDOM_KEY = "nskr_enigmaBlowerUpperRandom";
    public nskr_enigmaBlowerUpper() {
        super(false);
        //for intel
        this.firstTime = new nskr_saved<>("frostFirstTime", true);
    }
    static void log(final String message) {
        Global.getLogger(nskr_enigmaBlowerUpper.class).info(message);
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {

        //clean comm directory
        //needs to run while paused
        MarketAPI market = Global.getSector().getEconomy().getMarket("nskr_heart");
        if (market != null) {
            if (market.getFaction().getId().equals("enigma")) {
                //block hire-able persons from appearing
                CommDirectoryEntryAPI entry = market.getCommDirectory().getEntryForPerson(Global.getSector().getImportantPeople().getPerson("nskr_enigmaAdmin"));
                for (CommDirectoryEntryAPI comm : market.getCommDirectory().getEntriesCopy()) {
                    if (comm == entry) continue;
                    comm.setHidden(true);
                    market.getCommDirectory().removeEntry(comm);
                    //log("removed "+comm.getId()+" "+ comm.getTitle());
                }
            }
        }

        //PAUSE CHECK
        if (Global.getSector().isPaused()) return;

        if (Global.getSector().isInFastAdvance()) {
            counter += 2f*amount;
        } else{
            counter += amount;
        }
        //a slight delay just feels better than an EFS trigger
        if (counter>4f) {
            //INTEL LOGIC
            CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
            if (pf == null) return;
            final nskr_saved<Boolean> firstTime = this.firstTime;

            boolean visibleToPlayer = pf.getContainingLocation() == Global.getSector().getStarSystem(nskr_frost.getName());
            if (visibleToPlayer && firstTime.val) {
                nskr_frostIntel intel = new nskr_frostIntel();
                //Adds our intel
                this.intel = intel;
                Global.getSector().getIntelManager().addIntel(intel, true);
                log("Frost added INTEL");

                Global.getSector().getCampaignUI().addMessage("Your sensors officer is overwhelmed by the amount of active signals in this system, many of which are hostile. Further exploration will certainly yield results.",
                        Global.getSettings().getColor("standardTextColor"),
                        "hostile",
                        "",
                        Global.getSettings().getColor("yellowTextColor"),
                        Global.getSettings().getColor("yellowTextColor"));
                firstTime.val = false;
            }
            counter = 0f;
        }

        //kills frozen heart and spawns the wreckage (slightly hax)
        if (market != null) {
            if (market.getFaction().getId().equals("enigma")) {
                Industry fort = market.getIndustry(Industries.STARFORTRESS);

                if (fort.getDisruptedDays() > 88f && !doOnce) {
                    StarSystemAPI system = market.getStarSystem();
                    PlanetAPI star = system.getStar();
                    //explode pls
                    market.getMemoryWithoutUpdate().unset(DecivTracker.NO_DECIV_KEY);
                    market.setAdmin(null);
                    for (SectorEntityToken entity : market.getConnectedEntities()) {
                        entity.setFaction(Factions.NEUTRAL);
                    }
                    market.setPlanetConditionMarketOnly(true);
                    market.setFactionId(Factions.NEUTRAL);
                    market.getCommDirectory().clear();
                    for (PersonAPI person : market.getPeopleCopy()) {
                        market.removePerson(person);
                    }
                    market.clearCommodities();
                    for (MarketConditionAPI mc : new ArrayList<>(market.getConditions())) {
                        if (mc.getSpec().isDecivRemove()) {
                            market.removeSpecificCondition(mc.getIdForPluginModifications());
                        }
                    }
                    for (Industry ind : new ArrayList<>(market.getIndustries())) {
                        market.removeIndustry(ind.getId(), null, false);
                    }
                    market.setSize(1);
                    market.getPopulation().setWeight(CoreImmigrationPluginImpl.getWeightForMarketSizeStatic(market.getSize()));
                    market.getPopulation().normalize();
                    for (SubmarketAPI sub : market.getSubmarketsCopy()) {
                        market.removeSubmarket(sub.getSpecId());
                    }
                    Global.getSector().getEconomy().removeMarket(market);
                    Misc.removeRadioChatter(market);
                    SectorEntityToken heart = Global.getSector().getEntityById("nskr_heart");
                    float angle = heart.getCircularOrbitAngle();
                    float period = heart.getCircularOrbitPeriod();
                    float radius = heart.getCircularOrbitRadius();
                    //set to null and expired(important)
                    heart.setMarket(null);
                    heart.setFaction(Factions.NEUTRAL);
                    heart.setExpired(true);
                    //that should be enough??

                    //wreckage and debris
                    SectorEntityToken heartWreck = BaseThemeGenerator.addSalvageEntity(getRandom(), system.getStar().getContainingLocation(), "nskr_heart_wreckage", Factions.NEUTRAL);
                    //SectorEntityToken heartWreck = BaseThemeGenerator.addSalvageEntity(new Random(), system.getStar().getContainingLocation(), "nskr_heart_wreckage", Factions.NEUTRAL);
                    //replace
                    heartWreck.setId("nskr_heart_wreckage");
                    heartWreck.setCircularOrbitPointingDown(star, angle, radius, period);
                    heartWreck.setInteractionImage("illustrations", "space_wreckage");
                    heartWreck.setCustomDescriptionId("nskr_station_heart_d");
                    //salvage
                    DebrisFieldTerrainPlugin.DebrisFieldParams params_heart_main = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                            350f, // field radius - should not go above 1000 for performance reasons
                            1.2f, // density, visual - affects number of debris pieces
                            10000000f, // duration in days
                            14f); // days the field will keep generating glowing pieces
                    params_heart_main.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
                    params_heart_main.baseSalvageXP = 500; // base XP for scavenging in field
                    SectorEntityToken heart_main = Misc.addDebrisField(system, params_heart_main, StarSystemGenerator.random);
                    heart_main.setSensorProfile(1000f);
                    heart_main.setDiscoverable(true);
                    heart_main.setCircularOrbit(star, angle, radius, period);
                    heart_main.setId("nskr_heart_main_debris");
                    doOnce = true;
                }
            }
        }
    }

    public static Random getRandom() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

            data.put(PERSISTENT_RANDOM_KEY, new Random(util.getSeedParsed()));
        }
        return (Random)data.get(PERSISTENT_RANDOM_KEY);
    }
}
