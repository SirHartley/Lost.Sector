package lostsector.campaign.starts.hellspawn;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import lostsector.helper.Music;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;

// THRN and the presentation of its scenes that no vanilla command reproduces: PlayCustomMusic always fades in
// over one second and loops, and the scenes use a 12-second fade-in and a one-shot farewell track; no command
// animates a portrait (HellSpawnThrnAnimation).
final class HellSpawnThrnModule extends QuestModule<HellSpawnStage, HellSpawnState> {

    static final String PERSON_THRN = "thrn";
    static final String MUSIC_THRN = "nskr_thrn_theme";
    static final String MUSIC_FAREWELL = "nskr_peace";

    HellSpawnThrnModule() {
        super(HellSpawnStage.WARNING, HellSpawnStage.COUNTDOWN, HellSpawnStage.JUDGEMENT, HellSpawnStage.SPARED,
                HellSpawnStage.FIGHT, HellSpawnStage.JUDGED);
    }

    @Override
    protected void declare(Declarations<HellSpawnStage, HellSpawnState> d) {
        d.person(PERSON_THRN);
        d.action("musicStart", ctx -> Global.getSoundPlayer().playCustomMusic(0, 12, MUSIC_THRN, true));
        d.action("musicStop", ctx -> stopMusic());
        d.action("musicFarewell", ctx -> {
            stopMusic();
            Global.getSoundPlayer().playCustomMusic(0, 3, MUSIC_FAREWELL, false);
        });
        d.action("thrnAnimate", HellSpawnThrnModule::animate);
    }

    // createRandomPerson draws a name from the faction's name sets, which the neutral faction leaves empty,
    // so THRN is drawn as an independent and then set up as the neutral AI it is.
    @Override
    protected void onStart(QuestContext<HellSpawnStage, HellSpawnState> ctx) {
        ctx.people().create(PERSON_THRN, Factions.INDEPENDENT, thrn -> {
            thrn.setAICoreId(Commodities.OMEGA_CORE);
            thrn.setFaction(Factions.NEUTRAL);
            thrn.setGender(FullName.Gender.MALE);
            thrn.setImportance(PersonImportance.VERY_HIGH);
            thrn.setPostId(Ranks.POST_UNKNOWN);
            thrn.setRankId(Ranks.UNKNOWN);
            thrn.getName().setFirst("THRN");
            thrn.getName().setLast("");
            thrn.setPortraitSprite(HellSpawnThrnAnimation.portrait(0));
            thrn.getMemoryWithoutUpdate().unset("$voice");
        });
    }

    // The scenes are rules dialogs on the player fleet. Opening a dialog on an entity with a faction switches to
    // encounter music (CampaignState.showInteractionDialog) unless the entity's memory holds
    // $playLocationMusicDuringEnc, which leaves the music alone as the old null-target dialogs did; the scene then
    // starts its own track. The flag stays on the player fleet; vanilla opens no dialog on the player fleet.
    static void openScene(QuestContext<HellSpawnStage, HellSpawnState> ctx, String trigger) {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        player.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY, true);
        ctx.open(player, trigger);
    }

    static void stopMusic() {
        Music.stopIfPlaying(MUSIC_THRN);
    }

    private static void animate(QuestContext<HellSpawnStage, HellSpawnState> ctx) {
        InteractionDialogAPI dialog = ctx.dialog();
        PersonAPI thrn = ctx.people().get(PERSON_THRN);
        if (dialog == null || thrn == null) {
            ctx.log("thrnAnimate skipped: no dialog or no THRN");
            return;
        }
        Global.getSector().addTransientScript(new HellSpawnThrnAnimation(thrn, dialog, ctx.random(HellSpawnState.RANDOM_THRN_ANIMATION)));
    }
}
