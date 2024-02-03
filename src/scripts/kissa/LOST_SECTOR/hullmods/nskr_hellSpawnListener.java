package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.campaign.customStart.hellSpawnManager;

import java.awt.*;

public class nskr_hellSpawnListener implements DamageTakenModifier, AdvanceableListener {

    public static final Color TEXT_COLOR = new Color(95, 183, 80,255);

    public ShipAPI ship;
    public int level;

    public nskr_hellSpawnListener(ShipAPI ship, int level) {
        this.ship = ship;
        this.level = level;
    }

    @Override
    public void advance(float amount) {

        if (!ship.isAlive()) {
            ship.removeListener(this);
            return;
        }
        if (Global.getCombatEngine().isPaused()) {
            return;
        }


    }

    public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
        if (Math.random() < hellSpawnManager.DODGE_CHANCE/100f){

            if (!damage.isDps() && damage.getDamage() > 500f || damage.isDps() && damage.getDamage() > 5000f){

                Global.getSoundPlayer().playSound("nskr_hellspawn_dodge", 1.0f, 1.0f, point, new Vector2f());
                Global.getCombatEngine().addFloatingText(point, "Dodge!", 24f, TEXT_COLOR, null, 1f, 1f);

            }

            damage.setDamage(0f);
        }
        return null;
    }
}
