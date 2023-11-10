package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.hullmods.nskr_ultracaliber;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

public class nskr_powersurgeStats extends BaseShipSystemScript {

    //VARIABLES
    public static final float AGILITY_PENALTY = -50f;
    public static final float TOP_SPEED = 200f;
    public static final float ACCELERATION = 400f;
    public static final String SOUND_ID = "system_ammo_feeder";
    public static final Vector2f ZERO = new Vector2f();
    private boolean activated;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused() || stats.getEntity() == null) return;

        //make sure variables are correct
        nskr_ultracaliber.ShipSpecificData data = (nskr_ultracaliber.ShipSpecificData) Global.getCombatEngine().getCustomData().get("CALIBER_DATA_KEY" + ship.getId());
        if (state == State.IN || state == State.ACTIVE) {
            activated = false;

            stats.getMaxSpeed().modifyFlat(id, TOP_SPEED * effectLevel);
            stats.getAcceleration().modifyFlat(id, ACCELERATION * effectLevel);

            stats.getDeceleration().modifyPercent(id, AGILITY_PENALTY * effectLevel);
            stats.getTurnAcceleration().modifyPercent(id, AGILITY_PENALTY * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, AGILITY_PENALTY * effectLevel);
        }
        if (state == State.OUT) {
            //unmod (important)
            stats.getMaxSpeed().unmodify(id);
            stats.getAcceleration().unmodify(id);
            stats.getDeceleration().unmodify(id);
            stats.getTurnAcceleration().unmodify(id);
            stats.getMaxTurnRate().unmodify(id);

            if (!activated) {
                //clamp speed
                Vector2f vel = ship.getVelocity();
                ship.getVelocity().set(mathUtil.scaleVector(vel, 0.50f));

                Global.getSoundPlayer().playSound(SOUND_ID,1f,0.5f, ship.getLocation(), ZERO);

                data.timer = nskr_ultracaliber.SYS_TIME;
                activated = true;
            }
        }
        Global.getCombatEngine().getCustomData().put("CALIBER_DATA_KEY" + ship.getId(), data);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();

        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }

}
