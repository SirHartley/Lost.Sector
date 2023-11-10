package scripts.kissa.LOST_SECTOR.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class nskr_mfrocketOH implements OnHitEffectPlugin {

    public static final Color EXPLOSION_COLOR = new Color(123, 15, 182, 150);
    public static final Color PARTICLE_COLOR = new Color(43, 181, 255, 150);
    public static final Vector2f ZERO = new Vector2f();

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine){
        if (point == null) {
            return;
        }

        if (target instanceof ShipAPI) {

            float FLUX_DAMAGE = projectile.getDamageAmount();

            ShipAPI ship = (ShipAPI) target;
            if (projectile.getSource() == null) {
                if (shieldHit) {
                    ship.getFluxTracker().increaseFlux(FLUX_DAMAGE, true);
                } else {
                    ship.getFluxTracker().increaseFlux(FLUX_DAMAGE, true);
                }
            } else {
                if (shieldHit) {
                    ship.getFluxTracker().increaseFlux(FLUX_DAMAGE
                                    * projectile.getSource().getMutableStats().getEnergyWeaponDamageMult().getModifiedValue(),
                            true);
                } else {
                    ship.getFluxTracker().increaseFlux(FLUX_DAMAGE
                                    * projectile.getSource().getMutableStats().getEnergyWeaponDamageMult().getModifiedValue(),
                            true);
                }
            }
            float emp = projectile.getEmpAmount() * 0.50f;
            float dam = projectile.getDamageAmount() * 0.25f;
            for (int x = 0; x < 4; x++) {
                float pierceChance = ((ShipAPI) target).getHardFluxLevel() - 0.1f;
                pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);

                boolean piercedShield = shieldHit && (float) Math.random() < pierceChance;

                if (!shieldHit || piercedShield) {
                    EmpArcEntityAPI arc =  engine.spawnEmpArcPierceShields(projectile.getSource(), point, ship, ship,
                            DamageType.ENERGY, dam, emp, 100000f, "tachyon_lance_emp_impact", 20f,
                            PARTICLE_COLOR, EXPLOSION_COLOR);
                }
            }
        }
        if (shieldHit) {
            Global.getSoundPlayer().playSound("nskr_mfr_impact", 1.2f, 1.5f, point, ZERO);
        } else {
            Global.getSoundPlayer().playSound("nskr_mfr_impact", 0.8f, 1.8f, point, ZERO);
        }
        for (int x = 0; x < 3; x++) {
            float angle = (float) Math.random() * 360f;
            float distance = (float) Math.random() * 50f + 50f;
            Vector2f point1 = MathUtils.getPointOnCircumference(point, distance, angle);
            Vector2f point2 = new Vector2f(point);
            engine.spawnEmpArcVisual(point2, new SimpleEntity(point2), point1, new SimpleEntity(point1),
                    15f,
                    EXPLOSION_COLOR, PARTICLE_COLOR);

            engine.addNebulaParticle(projectile.getLocation(), ZERO,
                MathUtils.getRandomNumberInRange(50f,100f), MathUtils.getRandomNumberInRange(0.5f, 2.0f), 0.5f, 0.5f, MathUtils.getRandomNumberInRange(2.0f, 4.0f), EXPLOSION_COLOR);
        }
    }
}