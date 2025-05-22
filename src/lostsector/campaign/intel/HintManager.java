package lostsector.campaign.intel;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import lostsector.campaign.fleets.bounties.AbyssSpawner;
import lostsector.campaign.fleets.bounties.EternitySpawner;
import lostsector.campaign.fleets.bounties.MothershipSpawner;
import lostsector.Saved;
import lostsector.util.MathUtilLS;
import lostsector.util.MiscLS;
import lostsector.world.systems.frost.Frost;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class HintManager extends BaseCampaignEventListener implements EveryFrameScript  {
    //
    //creates hints for the player to go to systems on interest. has a chance to proc everytime we visit a new non-core system
    //
    public static final String PERSISTENT_RANDOM_KEY = "nskr_hintManagerKeyRandom";
    public static final float HINT_CHANCE = 0.04f;

    private boolean newSystem = false;
    CampaignFleetAPI pf;
    Saved<Float> counter;
    Saved<Boolean> newGame;
    Saved<ArrayList<StarSystemAPI>> sources;
    private final ArrayList<StarSystemAPI> cleanup = new ArrayList<>();

    public HintManager() {
        super(false);
        this.counter = new Saved<>("hintCounter", 0.0f);
        this.sources = new Saved<>("hintSources", new ArrayList<StarSystemAPI>());
        this.newGame = new Saved<>("hintNewGame", true);
    }

    static void log(final String message) {
        Global.getLogger(HintManager.class).info(message);
    }
    public boolean isDone() {
        return false;
    }
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector().isPaused()) return;
        this.pf = Global.getSector().getPlayerFleet();
        if (this.pf == null) return;

        if (Global.getSector().isInFastAdvance()) {
            counter.val += 2f*amount;
        } else{
            counter.val += amount;
        }

        if (sources.val.isEmpty() && newGame.val){
            //init locations
            sources.val.add(AbyssSpawner.getLoc().getStarSystem());
            sources.val.add(EternitySpawner.getLoc().getStarSystem());
            sources.val.add(MothershipSpawner.getMothershipBaseLocation().getStarSystem());
            sources.val.add(Global.getSector().getStarSystem(Frost.getName()));
            newGame.val = false;
            log("HINT added sources, size "+sources.val.size());
        }
        //we used all the hints
        if (sources.val.isEmpty()) return;

        if (counter.val>10f) {

            IntelManagerAPI manager = Global.getSector().getIntelManager();
            //abyss
            if (manager.hasIntelOfClass(AbyssIntel.class)){
                StarSystemAPI sys = AbyssSpawner.getLoc().getStarSystem();
                if (sources.val.contains(sys)) cleanup.add(sys);
            }
            //umbra
            if (manager.hasIntelOfClass(UmbraIntel.class)){
                StarSystemAPI sys = EternitySpawner.getLoc().getStarSystem();
                if (sources.val.contains(sys)) cleanup.add(sys);
            }
            //mothership
            if (manager.hasIntelOfClass(MothershipIntel.class)){
                StarSystemAPI sys = MothershipSpawner.getMothershipBaseLocation().getStarSystem();
                if (sources.val.contains(sys)) cleanup.add(sys);
            }
            //frost
            if (manager.hasIntelOfClass(FrostIntel.class)){
                StarSystemAPI sys = Global.getSector().getStarSystem(Frost.getName());
                if (sources.val.contains(sys)) cleanup.add(sys);
            }
            //clean the source hints list, do before checking if we add new intel
            clean();

            //newSystem check
            if(!pf.isInHyperspace()){
                StarSystemAPI sys = pf.getStarSystem();
                //check if we have been here
                if (!sys.isEnteredByPlayer()) {
                    //no core systems
                    if (!sys.hasTag(Tags.THEME_CORE) && !sys.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) {
                        newSystem = true;
                    }
                }
            }
            //add hint
            if (!sources.val.isEmpty()) {
                //rng check
                if (newSystem && getRandom().nextFloat()<HINT_CHANCE) {
                    StarSystemAPI source = sources.val.get(MathUtilLS.getSeededRandomNumberInRange(0, sources.val.size() - 1, getRandom()));
                    HintIntel intel = new HintIntel(source);
                    //Adds our intel
                    Global.getSector().getIntelManager().addIntel(intel, false);
                    log("HINT added INTEL for " + source.getName());

                    //clean up
                    cleanup.add(source);
                }
            }
            newSystem = false;

            //clean the source hints list, do again for gained intel
            clean();

            counter.val = 0f;
        }
    }

    private void clean() {
        if (!cleanup.isEmpty() && !sources.val.isEmpty()){
            for (StarSystemAPI s : cleanup){
                sources.val.remove(s);
            }
            cleanup.clear();
        }
    }

    public static void removeHintIntel() {
        if (Global.getSector().getIntelManager().hasIntelOfClass(HintIntel.class)){
            CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
            for (IntelInfoPlugin intel :  Global.getSector().getIntelManager().getIntel()){
                if (intel.getClass()==HintIntel.class){
                    StarSystemAPI sys = ((HintIntel) intel).system;
                    if (pf.getContainingLocation()== sys){
                        ((HintIntel) intel).endImmediately();
                    }
                }
            }
        }
    }

    public static Random getRandom() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

            data.put(PERSISTENT_RANDOM_KEY, new Random(MiscLS.getSeedParsed()));
        }
        return (Random) data.get(PERSISTENT_RANDOM_KEY);
    }
}
