package scripts.kissa.LOST_SECTOR.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;

public class nskr_emflakEffect implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    //new prox fuse code cause vanilla one is unmodifiable

    public static final float EXPLOSION_RADIUS = 300f;
    public static final float FUZE_RADIUS = 175f;
    public static final float FUZE_MIN_DMG = 100f;

    static void log(final String message) {
        Global.getLogger(nskr_emflakEffect.class).info(message);
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        ShipAPI ship = projectile.getSource();
        ship.addListener(new nskr_emFlak.emFlakListener(projectile));
    }
}


