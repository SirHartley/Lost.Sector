package scripts.kissa.LOST_SECTOR.hullmods.exotica;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.renderUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;

public class spaceTimeAnchorListener implements DamageTakenModifier, AdvanceableListener {



    public ShipAPI ship;
    public int level;

    private Float mass = null;

    public spaceTimeAnchorListener(ShipAPI ship, int level) {
        this.ship = ship;
        this.level = level;
    }

    @Override
    public void advance(float amount) {

        if (!ship.isAlive()){
            ship.removeListener(this);
            return;
        }

        if (mass == null) {
            mass = ship.getMass();
        }

        if (ship.getMass() == mass) {
            ship.setMass(mass * (1f+((spaceTimeAnchor.MASS_PER_LEVEL*0.01f)*level)));
        }

        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        float chance = level*0.015f;
        if (Math.random()<chance * (Global.getCombatEngine().getElapsedInLastFrame()*60f)) {
            Vector2f particlePos, particleVel;

            particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), (float)Math.random()*(ship.getCollisionRadius()*2f));
            particleVel = Vector2f.sub(ship.getLocation(), particlePos, null);
            Global.getCombatEngine().addSmokeParticle(particlePos, mathUtil.scaleVector(particleVel, 0.1f), 4f, 0.7f, 1f,
                    new Color(99, 51, 238, 255));
        }


    }

    public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
        if (!shieldHit) {
            float dmg = damage.getDamage();
            if (!damage.isDps()) {
                if (dmg > spaceTimeAnchor.DAMAGE_THRESHOLD) {
                    float reduction = spaceTimeAnchor.REDUCTION_PER_LEVEL * level;
                    float amount = Math.max(dmg - reduction, spaceTimeAnchor.DAMAGE_THRESHOLD);

                    damage.setDamage(amount);
                }
            }
            //beam dmg is 10 times per second
            else {
                dmg /= 10f;
                if (dmg > spaceTimeAnchor.DAMAGE_THRESHOLD) {
                    float reduction = spaceTimeAnchor.REDUCTION_PER_LEVEL * level;
                    float amount = Math.max(dmg - reduction, spaceTimeAnchor.DAMAGE_THRESHOLD);

                    damage.setDamage(amount);
                }
            }

        }
        return null;
    }

}
