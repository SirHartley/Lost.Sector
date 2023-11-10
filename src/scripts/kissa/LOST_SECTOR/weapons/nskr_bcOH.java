package scripts.kissa.LOST_SECTOR.weapons;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.combat.entities.Ship;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.combatUtil;

public class nskr_bcOH implements OnHitEffectPlugin {

    public static final float PERCENTAGE_HP_DMG = 0.10f;
    public static final Color PARTICLE_COLOR = new Color(255, 112, 9, 255);
    public static final Vector2f ZERO = new Vector2f();

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine){
        ShipAPI ship = projectile.getSource();

        if (!shieldHit && target instanceof ShipAPI) {
            Global.getSoundPlayer().playSound("nskr_bc_impact", 1.0f, 1.0f, point, ZERO);

            combatUtil.createHitParticles(point, projectile.getFacing(),
                    20f, PARTICLE_COLOR, 120f, MathUtils.getRandomNumberInRange(2f,4f), 0.90f, 2.00f, 75f, 4.0f);
            float dmg = 0f;
            //module shenans
            ShipAPI station = (ShipAPI)target;
            if (station.isShipWithModules() || station.isStationModule()){
                //pick parent
                ShipAPI tmp = null;
                if (station.isStationModule()){
                    tmp = station.getParentStation();
                } else tmp = station;
                //get health
                float health = station.getMaxHitpoints();
                if (!tmp.getChildModulesCopy().isEmpty()) health = getHighestHealthModule(tmp);
                dmg = health*PERCENTAGE_HP_DMG;
                //engine.addFloatingText(point, ""+health, 24f, Color.CYAN, null,1f,1f);
            }
            //default
            else {
                dmg = target.getMaxHitpoints()*PERCENTAGE_HP_DMG;
            }
            engine.applyDamage(target, point, dmg, DamageType.HIGH_EXPLOSIVE, 0f, false, false, ship);
        }
    }

    private float getHighestHealthModule(ShipAPI target) {
        float highest = 0f;
        if (target.isShipWithModules()) highest = target.getMaxHitpoints();
        for (ShipAPI module : target.getChildModulesCopy()){
            if (module==null)continue;
            if (module.getMaxHitpoints()>highest){
                highest = module.getMaxHitpoints();
            }
        }
        return highest;
    }
}