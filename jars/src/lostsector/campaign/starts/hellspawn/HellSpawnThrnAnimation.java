package lostsector.campaign.starts.hellspawn;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import java.util.Random;

// Flickers THRN's portrait and name while one scene is on screen. A rules dialog pauses the campaign and
// RuleBasedInteractionDialogPluginImpl.advance is empty, so no callback runs per frame during the scene; the
// campaign engine still advances runWhilePaused scripts with the frame time while a dialog is open
// (CampaignState.advance, CampaignEngine.advance). The script is transient and ends when the dialog closes or
// hands over to another plugin, such as the judgement encounter. Like the Java scenes it replaces, it relies on
// the person card picking up the changed sprite and name without being shown again, which the API does not state.
final class HellSpawnThrnAnimation implements EveryFrameScript {

    private static final int FRAMES = 4;

    private final PersonAPI thrn;
    private final InteractionDialogAPI dialog;
    private final InteractionDialogPlugin plugin;
    private final Random random;
    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);
    private int frame = 0;
    private boolean done = false;

    HellSpawnThrnAnimation(PersonAPI thrn, InteractionDialogAPI dialog, Random random) {
        this.thrn = thrn;
        this.dialog = dialog;
        this.plugin = dialog.getPlugin();
        this.random = random;
    }

    static String portrait(int frame) {
        return "graphics/lostsector/portraits/nskr_thrn0" + frame + ".png";
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector().getCampaignUI().getCurrentInteractionDialog() != dialog || dialog.getPlugin() != plugin) {
            done = true;
            return;
        }
        interval.advance(amount);
        if (!interval.intervalElapsed()) return;
        if (random.nextFloat() < 0.10f) thrn.getName().setFirst("THRON");
        else if (random.nextFloat() < 0.10f) thrn.getName().setFirst("THRNE");
        else if (random.nextFloat() < 0.10f) thrn.getName().setFirst("DIE");
        else thrn.getName().setFirst("THRN");
        thrn.setPortraitSprite(portrait(frame));
        frame = (frame + 1) % FRAMES;
    }
}
