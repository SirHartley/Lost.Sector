package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.hullmods.nskr_absorption;

import java.awt.*;

public class nskr_bubbleStats extends BaseShipSystemScript {

    //basically a dummy, the hullmod does all the work

    //VARIABLES
    public static final String SOUND_ID = "nskr_bubble";
    public static final Vector2f ZERO = new Vector2f();

    private boolean activated;
    private boolean updated = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused() || stats.getEntity() == null) return;
        if (ship.getShield()==null) {
            engine.addFloatingText(ship.getLocation(), "you have no shield bruh", 36f, Color.RED, ship, 0.5f, 1.0f);
            return;
        }

        //make sure variables are correct
        nskr_absorption.ShipSpecificData data = (nskr_absorption.ShipSpecificData) Global.getCombatEngine().getCustomData().get("ABSORPTION_DATA_KEY" + ship.getId());
        if (!updated) {
            activated = false;

            updated = true;
        }
        if (state == State.OUT || state == State.ACTIVE) {
            if (!activated) {
                Global.getSoundPlayer().playSound(SOUND_ID,1f,1f, ship.getLocation(), ZERO);

                data.timer = nskr_absorption.SYS_TIME;
                activated = true;
                ship.getShield().toggleOn();
            }
        }
        Global.getCombatEngine().getCustomData().put("ABSORPTION_DATA_KEY" + ship.getId(), data);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();

        updated = false;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }

}
