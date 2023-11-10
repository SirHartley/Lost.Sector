//////////////////////
//Initially created by theDragn and modified from HTE
//////////////////////
package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class nskr_protocolStats extends BaseShipSystemScript {

    //the hullmod actually does all the stuff

        int mode = 1;
        boolean firstFrame = true;

        public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
            if (stats.getEntity() instanceof ShipAPI && this.firstFrame) {
                this.firstFrame = false;
                ShipAPI ship = (ShipAPI)stats.getEntity();
                ++this.mode;
                if (this.mode >= 4) {
                    this.mode = 1;
                }
                ship.getSystem().setAmmo(this.mode);
            }
        }

        public void unapply(MutableShipStatsAPI stats, String id) {
            this.firstFrame = true;
            if (stats.getEntity() instanceof ShipAPI) {
                ShipAPI ship = (ShipAPI)stats.getEntity();
                ship.getSystem().setAmmo(this.mode);
            }
        }

        public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
            switch (system.getAmmo()) {
                case 1: {
                    return "Relocation Protocol";
                }
                case 2: {
                    return "Precision Protocol";
                }
                case 3: {
                    return "Safeguard Protocol";
                }
            }
            return "bruh";
        }
}
