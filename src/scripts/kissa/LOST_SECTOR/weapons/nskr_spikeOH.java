package scripts.kissa.LOST_SECTOR.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class nskr_spikeOH implements OnHitEffectPlugin {

    public static final Vector2f ZERO = new Vector2f();
    public static final int NUM_SPLINTERS = 7;
    public static final String SPLINTER_WEAPON_ID = "nskr_spike_dummy";

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine){

        Global.getSoundPlayer().playSound("nskr_spike_impact", 1.0f, 1.0f, point, ZERO);

        ShipAPI ship = projectile.getSource();

        // This spawns the frag, also distributing them in a nice even 360 degree arc
        for (int i = 0; i < NUM_SPLINTERS; i++)
        {
            float sangle2 = (float) Math.random() * 360f;
            float sdistance = (float) Math.random() * 15f + 0f;
            Vector2f sPoint = MathUtils.getPointOnCircumference(projectile.getLocation(), sdistance, sangle2);
            float sangle = ship.getFacing() + i * 360f / NUM_SPLINTERS + (float) Math.random() * 180f / NUM_SPLINTERS;
            Vector2f location = MathUtils.getPointOnCircumference(sPoint, 15f, sangle);
            WeaponAPI wep;
            wep = engine.createFakeWeapon(ship, SPLINTER_WEAPON_ID);

            DamagingProjectileAPI newProj = (DamagingProjectileAPI)Global.getCombatEngine().spawnProjectile(ship, wep, SPLINTER_WEAPON_ID, location, sangle, new Vector2f(0,0));
            Vector2f newVel = new Vector2f(newProj.getVelocity());
            newVel.scale((float)Math.random());
            newProj.getVelocity().set(newVel.x,newVel.y);
        }
    }
}