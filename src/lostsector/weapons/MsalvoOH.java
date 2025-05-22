package lostsector.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;


public class MsalvoOH implements OnHitEffectPlugin {

    public static final Color CORE_COLOR = new Color(50, 142, 81, 255);

    public static final Vector2f ZERO = new Vector2f();

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine){

        //Global.getSoundPlayer().playSound("Tremor_impact", 1.0f, 1.0f, point, ZERO);

        engine.addNegativeSwirlyNebulaParticle(point, ZERO, 15f, 10f, 0.5f, 0.5f, 1f, CORE_COLOR);
    }
}