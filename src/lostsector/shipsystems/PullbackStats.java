
package lostsector.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import lostsector.hullmods.Pullback;
import lostsector.util.MathUtilLS;
import org.lwjgl.util.vector.Vector2f;

public class PullbackStats extends BaseShipSystemScript {

    //VARIABLES
    public static final String SOUND_ID = "nskr_pullback";
    public static final Vector2f ZERO = new Vector2f();
    private boolean activated;
    private boolean updated = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused() || stats.getEntity() == null) return;
        //make sure variables are correct

        Pullback.ShipSpecificData data = (Pullback.ShipSpecificData) Global.getCombatEngine().getCustomData().get("PULLBACK_DATA_KEY" + ship.getId());

        if (!updated) {
            activated = false;

            updated = true;
        }

        if (state == State.ACTIVE) {
            //TELEPORT
            if (!activated) {
                //TeleporterPlugin.addTeleportation(ship, data.tPoint1);
                ship.getLocation().set(data.tPoint1);
                Vector2f vel = ship.getVelocity();
                ship.getVelocity().set(MathUtilLS.scaleVector(vel, 0.2f));

                Global.getSoundPlayer().playSound(SOUND_ID,1f,1f, ship.getLocation(), ZERO);
                data.activated = true;
                activated = true;
            }
        }
        if (state == State.OUT) {
        }
        Global.getCombatEngine().getCustomData().put("PULLBACK_DATA_KEY" + ship.getId(), data);
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
