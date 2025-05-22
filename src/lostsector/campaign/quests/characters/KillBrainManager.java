package lostsector.campaign.quests.characters;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import lostsector.campaign.loot.BountyLoot;
import lostsector.campaign.quests.util.QuestUtil;
import lostsector.campaign.quests.util.SimpleSystem;
import lostsector.campaign.util.CampaignTimer;
import lostsector.util.LSIds;
import lostsector.util.MathUtilLS;
import lostsector.util.MiscLS;
import org.lazywizard.lazylib.MathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KillBrainManager extends BaseCampaignEventListener implements EveryFrameScript {

    public static final float SPAWN_CHANCE_PER_DAY = 0.01f;

    public static final String STARTED_KEY = "killBrainManagerStarted";
    public static final String ENDED_KEY = "killBrainManagerEnded";
    public static final String STAGE_KEY = "killBrainManagerStage";
    public static final String LOCATION_KEY = "killBrainManagerLocation";
    public static final String STATION_KEY = "$killBrainStation";
    public static final String PERSISTENT_RANDOM_KEY = "killBrainManagerRandom";

    private CampaignTimer timer;

    static void log(final String message) {
        Global.getLogger(KillBrainManager.class).info(message);
    }
    public KillBrainManager() {
        super(false);
        this.timer = new CampaignTimer(this.getClass().getName(), 1f);
        //init
        QuestUtil.getRandom(PERSISTENT_RANDOM_KEY);
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {

        //PAUSE CHECK
        if (Global.getSector().isPaused()) return;

        timer.advance(amount);
        if (timer.onTimeout()) {
            //Void group or Helios
            if (QuestUtil.getCompleted(BountyLoot.DEFEATED_ABYSS_KEY) || QuestUtil.getCompleted(BountyLoot.DEFEATED_HELIOS_KEY)) {
                //rng check
                Random random = QuestUtil.getRandom(PERSISTENT_RANDOM_KEY);
                if (random.nextFloat() < SPAWN_CHANCE_PER_DAY) {
                    //start
                    if (!QuestUtil.getCompleted(STARTED_KEY)) {
                        CampaignUIAPI ui = Global.getSector().getCampaignUI();
                        if (!ui.isShowingDialog() && !ui.isShowingMenu()) {
                            //set the loc
                            QuestUtil.setLocation(MiscLS.getRandomLocationInSystem(getRandomSystemWithBlacklist(), false, true, random), LOCATION_KEY);
                            spawnStation();

                            Global.getSector().getCampaignUI().showInteractionDialog(new KillBrainStartDialog(), null);

                            QuestUtil.setCompleted(true, STARTED_KEY);
                        }
                    }

                }


            }

        }

    }

    private void spawnStation(){
        Random random = QuestUtil.getRandom(PERSISTENT_RANDOM_KEY);
        SectorEntityToken loc = QuestUtil.getLocation(LOCATION_KEY);
        LocationAPI containing = loc.getContainingLocation();

        BaseThemeGenerator.EntityLocation createLoc = DerelictThemeGenerator.createLocationAtRandomGap(random, loc, 0f);
        SectorEntityToken artifact = DerelictThemeGenerator.addNonSalvageEntity(containing, createLoc, LSIds.ANOMALOUS_STATION_ENTITY_ID, Factions.NEUTRAL).entity;
        artifact.setDiscoverable(true);
        artifact.setSensorProfile(2000f);

        artifact.setCircularOrbitPointingDown(
                loc, random.nextFloat()*360f, MathUtils.getDistance(artifact.getLocation(), loc.getLocation()), MathUtilLS.getSeededRandomNumberInRange(30f,60f, random));

        artifact.getMemory().set(STATION_KEY, true);

        //makes sure we are not in a star
        QuestUtil.spawnAwayFromStarFixer(artifact, 2.0f);
    }

    private static StarSystemAPI getRandomSystemWithBlacklist() {

        //pick tags
        List<String> pickTags = new ArrayList<>();
        pickTags.add(Tags.THEME_REMNANT);

        SimpleSystem simpleSystem = new SimpleSystem(new Random(), 2);
        simpleSystem.pickTags = pickTags;
        simpleSystem.pickOnlyInProcgen = true;

        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("picking any system");
        //reset tags
        simpleSystem.pickTags = new ArrayList<>();
        //try again
        if (!simpleSystem.get().isEmpty()) {
            StarSystemAPI pick = simpleSystem.pick();
            log("picked "+pick.getName());
            return pick;
        }
        log("ERROR no valid system");
        return MiscLS.getRandomNonCoreSystem(new Random());
    }
}
