package scripts.kissa.LOST_SECTOR.campaign.customStart.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class thronesGiftHolySpiritListener implements AdvanceableListener {

    public static final String MUSIC_ID = "nskr_holy_spirit_theme";
    public static final Color TEXT_COLOR = new Color(104, 220, 59, 255);
    public static final Color JITTER_COLOR = new Color(234, 187, 32, 255);

    public static final ArrayList<String> ENTRANCE_LINES = new ArrayList<>();
    static {
        ENTRANCE_LINES.add("Die");
        ENTRANCE_LINES.add("Return to ash");
        ENTRANCE_LINES.add("Perish");
        ENTRANCE_LINES.add("Hammer of justice crushes you");
        ENTRANCE_LINES.add("Welcome to the end");
        ENTRANCE_LINES.add("Pathetic");

    }

    public static final ArrayList<String> EXIT_LINES = new ArrayList<>();
    static {
        EXIT_LINES.add("I am eternal");
        EXIT_LINES.add("The victory is only temporary");
        EXIT_LINES.add("This changes nothing");
        EXIT_LINES.add("And this will happen again");
        EXIT_LINES.add("Not the end");

    }

    public ShipAPI ship;
    private boolean music = false;
    private boolean entrance = false;
    private float timer;
    private String ogPortrait = "";

    public thronesGiftHolySpiritListener(ShipAPI ship) {
        this.ship = ship;
        this.timer = MathUtils.getRandomNumberInRange(25f, 75f);
    }

    @Override
    public void advance(float amount) {

        //safety check
        if (ship.getFleetMember()==null) return;
        if (ship.getFleetMember().getFleetData()==null) return;
        if (ship.getFleetMember().getFleetData().getFleet()==null) return;
        if (ship.getFleetMember().getFleetData().getFleet()==Global.getSector().getPlayerFleet()) {
            ship.removeListener(this);
            return;
        }

        if (!ship.isAlive() && timer <= 0f) {

            Global.getCombatEngine().getCombatUI().addMessage(
                    1, ship, TEXT_COLOR, "HOLY SPIRIT", TEXT_COLOR, ":", TEXT_COLOR,
                    " "+EXIT_LINES.get(MathUtils.getRandomNumberInRange(0, EXIT_LINES.size() - 1)));
            Global.getCombatEngine().getCombatUI().addMessage(
                    1, TEXT_COLOR,"The HOLY SPIRIT has returned");

            //stop music
            if (Global.getSoundPlayer().getCurrentMusicId().equals("HOLY SPIRIT.ogg")) Global.getSoundPlayer().pauseCustomMusic();

            //set back og portrait
            if (ship.getCaptain()!=null) {
                ship.getCaptain().setPortraitSprite(ogPortrait);
            }

            ship.removeListener(this);
            return;
        } else if (!ship.isAlive()) {
            ship.removeListener(this);
            return;
        }
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        timer -= amount;

        //music
        if (!music && timer <= 0f) {
            Global.getSoundPlayer().playCustomMusic(0, 5, MUSIC_ID);
            music = true;
        }

        if (!entrance && timer <= 0f){
            addBonus(ship);

            Global.getCombatEngine().getCombatUI().addMessage(
                    1, TEXT_COLOR,"The HOLY SPIRIT has arrived");
            Global.getCombatEngine().getCombatUI().addMessage(
                    1, ship, TEXT_COLOR, "HOLY SPIRIT", TEXT_COLOR, ":", TEXT_COLOR,
                    " "+ENTRANCE_LINES.get(MathUtils.getRandomNumberInRange(0, ENTRANCE_LINES.size() - 1)));

            Global.getSoundPlayer().playUISound("nskr_prot_warning", 1.0f, 0.8f);

            //set portrait
            if (ship.getCaptain()!=null) {
                ogPortrait = ship.getCaptain().getPortraitSprite();
                ship.getCaptain().setPortraitSprite("graphics/portraits/nskr_thrn00.png");
            }

            entrance = true;
        }

        if (entrance){

            //FX
            ship.setJitterUnder("nskr_holySpirit", JITTER_COLOR, 12f, 12, 0.55f);
            ship.setJitterShields(false);

            float chance = 7.5f*0.015f;
            if (Math.random()<chance * (Global.getCombatEngine().getElapsedInLastFrame()*60f)) {
                Vector2f particlePos, particleVel;

                particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), (float)Math.random()*(ship.getCollisionRadius()*2f));
                particleVel = Vector2f.sub(ship.getLocation(), particlePos, null);
                Global.getCombatEngine().addSmokeParticle(particlePos, mathUtil.scaleVector(particleVel, 0.1f), 5f, 0.9f, 2f,
                        JITTER_COLOR);
            }

            //SOUND
            Global.getSoundPlayer().playLoop("nskr_eternity_loop", ship,1f, 0.30f, ship.getLocation(), new Vector2f());

        }

    }

    //copy of SO stats
    private static final Map speed = new HashMap();
    static {
        speed.put(ShipAPI.HullSize.FRIGATE, 50f);
        speed.put(ShipAPI.HullSize.DESTROYER, 30f);
        speed.put(ShipAPI.HullSize.CRUISER, 20f);
        speed.put(ShipAPI.HullSize.CAPITAL_SHIP, 10f);
    }
    private static final float PEAK_MULT = 0.33f;
    private static final float FLUX_DISSIPATION_MULT = 2f;
    private static final float ROF_MULT = 50f;

    private void addBonus(ShipAPI ship) {
        MutableShipStatsAPI stats = ship.getMutableStats();
        String id = "nskr_holySpirit";
        ShipAPI.HullSize hullSize = ship.getHullSize();

        stats.getMaxSpeed().modifyFlat(id, (Float) speed.get(hullSize));
        stats.getAcceleration().modifyFlat(id, (Float) speed.get(hullSize) * 2f);
        stats.getDeceleration().modifyFlat(id, (Float) speed.get(hullSize) * 2f);
        stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 2f); // set to two, meaning boost is always on

        stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);

        stats.getBallisticRoFMult().modifyPercent(id, ROF_MULT);
        stats.getMissileRoFMult().modifyPercent(id, ROF_MULT);

        if (!ship.getVariant().hasHullMod(HullMods.SAFETYOVERRIDES)) stats.getPeakCRDuration().modifyMult(id, PEAK_MULT);
        stats.getVentRateMult().modifyMult(id, 0f);

    }

}
