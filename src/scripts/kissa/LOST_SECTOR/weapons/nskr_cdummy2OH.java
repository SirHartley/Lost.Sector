package scripts.kissa.LOST_SECTOR.weapons;

import java.awt.Color;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicLensFlare;
import org.lwjgl.util.vector.Vector2f;


public class nskr_cdummy2OH implements OnHitEffectPlugin {

    public static final Color CORE_COLOR = new Color(255, 182, 228, 255);
    public static final Color FRINGE_COLOR = new Color(255, 43, 85, 255);

    public static final Vector2f ZERO = new Vector2f();

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine){

        ShipAPI ship = projectile.getSource();

        //Global.getSoundPlayer().playSound("nskr_tremor_impact", 1.0f, 1.0f, point, ZERO);

        MagicLensFlare.createSharpFlare(engine, ship, point, 1f, 100f, projectile.getFacing()-90f, FRINGE_COLOR, CORE_COLOR);
    }
}