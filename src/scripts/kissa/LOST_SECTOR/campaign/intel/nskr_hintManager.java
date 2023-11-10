package scripts.kissa.LOST_SECTOR.campaign.intel;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_abyssSpawner;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_eternitySpawner;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_mothershipSpawner;
import scripts.kissa.LOST_SECTOR.campaign.nskr_enigmaBlowerUpper;
import scripts.kissa.LOST_SECTOR.nskr_saved;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;
import scripts.kissa.LOST_SECTOR.world.systems.frost.nskr_frost;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class nskr_hintManager extends BaseCampaignEventListener implements EveryFrameScript  {
    //
    //creates hints for the player to go to systems on interest. has a chance to proc everytime we visit a new non-core system
    //
    public static final String PERSISTENT_RANDOM_KEY = "nskr_hintManagerKeyRandom";
    public static final float HINT_CHANCE = 0.04f;

    private boolean newSystem = false;
    private nskr_hintIntel intel = null;
    CampaignFleetAPI pf;
    nskr_saved<Float> counter;
    nskr_saved<Boolean> newGame;
    nskr_saved<ArrayList<StarSystemAPI>> sources;

    public nskr_hintManager() {
        super(false);
        this.counter = new nskr_saved<>("hintCounter", 0.0f);
        this.sources = new nskr_saved<>("hintSources", new ArrayList<StarSystemAPI>());
        this.newGame = new nskr_saved<>("hintNewGame", true);
    }

    static void log(final String message) {
        Global.getLogger(nskr_hintManager.class).info(message);
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
            sources.val.add(nskr_abyssSpawner.getLoc().getStarSystem());
            sources.val.add(nskr_eternitySpawner.getLoc().getStarSystem());
            sources.val.add(nskr_mothershipSpawner.getMothershipBaseLocation().getStarSystem());
            sources.val.add(Global.getSector().getStarSystem(nskr_frost.getName()));
            newGame.val = false;
            log("HINT added sources, size "+sources.val.size());
        }
        //manually set up removal of hints if we already have the intel (dumb but i CBA to do it properly)
        //run this first (important)
        if (!sources.val.isEmpty()) {
            for (final Iterator<StarSystemAPI> iter = this.sources.val.listIterator(); iter.hasNext(); ) {
                StarSystemAPI a = iter.next();
                //FROST
                if (getIntelReceived(nskr_enigmaBlowerUpper.HINT_KEY)){
                    if (a == Global.getSector().getStarSystem(nskr_frost.getName())) {
                        iter.remove();
                        log("HINT removed hint for "+ a.getName());
                    }
                }
                //UMBRA
                if (getIntelReceived(nskr_eternitySpawner.HINT_KEY)){
                    if (a == nskr_eternitySpawner.getLoc().getStarSystem()) {
                        iter.remove();
                        log("HINT removed hint for "+ a.getName());
                    }
                }
                //ABYSS
                if (getIntelReceived(nskr_abyssSpawner.HINT_KEY)){
                    if (a == nskr_abyssSpawner.getLoc().getStarSystem()) {
                        iter.remove();
                        log("HINT removed hint for "+ a.getName());
                    }
                }
                //MOTHERSHIP
                if (getIntelReceived(nskr_mothershipSpawner.HINT_KEY)){
                    if (a == nskr_mothershipSpawner.getMothershipBaseLocation().getStarSystem()) {
                        iter.remove();
                        log("HINT removed hint for "+ a.getName());
                    }
                }
            }
        }
        //we used all the hints
        if (sources.val.isEmpty()) return;

        if (counter.val>10f) {

            //newSystem check
            if(!pf.isInHyperspace()){
                StarSystemAPI sys = pf.getStarSystem();
                //check if we have been here
                if (!sys.isEnteredByPlayer()) {
                    //no core systems
                    if (!sys.hasTag(Tags.THEME_CORE) && !sys.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) {
                        newSystem = true;
                        //log("Hint newSystem");
                    }
                }
            }
            //add hint
            if (!sources.val.isEmpty()) {
                //rng check
                if (newSystem && getRandom().nextFloat()<HINT_CHANCE) {
                    StarSystemAPI source = sources.val.get(mathUtil.getSeededRandomNumberInRange(0, sources.val.size() - 1, getRandom()));
                    nskr_hintIntel intel = new nskr_hintIntel(source);
                    //Adds our intel
                    this.intel = intel;
                    Global.getSector().getIntelManager().addIntel(intel, false);
                    log("HINT added INTEL for " + source.getName());

                    //clean up
                    for (final Iterator<StarSystemAPI> iter = this.sources.val.listIterator(); iter.hasNext(); ) {
                        StarSystemAPI a = iter.next();
                        if (a == source) {
                            iter.remove();
                            log("HINT REMOVED " + source.getName());
                        }
                    }
                }
            }
            newSystem = false;

            counter.val = 0f;
        }
    }

    public static void removeHintIntel() {
        if (Global.getSector().getIntelManager().hasIntelOfClass(nskr_hintIntel.class)){
            CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
            for (IntelInfoPlugin intel :  Global.getSector().getIntelManager().getIntel()){
                if (intel.getClass()==nskr_hintIntel.class){
                    StarSystemAPI sys = ((nskr_hintIntel) intel).system;
                    if (pf.getContainingLocation()== sys){
                        ((nskr_hintIntel) intel).endImmediately();
                    }
                }
            }
        }
    }

    public static Random getRandom() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {

            data.put(PERSISTENT_RANDOM_KEY, new Random(util.getSeedParsed()));
        }
        return (Random) data.get(PERSISTENT_RANDOM_KEY);
    }

    public static boolean getIntelReceived(String id) {
        //whether we should spawn hints for this system
        Map<String, Object> data = Global.getSector().getPersistentData();
        if (!data.containsKey(id)) data.put(id, false);

        return (boolean)data.get(id);
    }

    public static void setIntelReceived(boolean remove, String id) {
        //whether we should spawn hints for this system
        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(id, remove);
    }
}
