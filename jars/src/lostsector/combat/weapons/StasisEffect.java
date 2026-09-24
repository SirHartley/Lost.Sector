package lostsector.combat.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import lostsector.combat.systems.ai.StasisAI;
import lostsector.settings.Setting;
import org.lwjgl.input.Keyboard;

public class StasisEffect  implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

        ShipAPI ship = projectile.getSource();
        ship.addListener(new Stasis.StasisProjectileVisualListener(projectile, ship));

        //Flag for AI
        StasisAI.ShipSpecificData data = (StasisAI.ShipSpecificData) Global.getCombatEngine().getCustomData().get("STASIS_AI_DATA_KEY" + ship.getId());
        if (data != null) {
            data.sinceEffected = 0f;
            Global.getCombatEngine().getCustomData().put("STASIS_AI_DATA_KEY" + ship.getId(), data);
        }
    }

    private boolean wasActive = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (weapon== null) return;
        if (engine.isPaused()) return;

        ShipAPI ship = weapon.getShip();
        //so the fire key has to be released and we don't immediately fire
        if (engine.getPlayerShip()==ship && ship.getSystem().isActive()) {
            wasActive = true;
        }
        //so we fire when pressing the fire key while system is cooling down
        if (engine.getPlayerShip()==ship && ship.getSystem().isCoolingDown()) {
            int fireKey = Setting.STASIS_FIRE_KEY.getKeycode();
            // KEY_NONE means unbound; LWJGL also reports unknown keys as KEY_NONE.
            if (fireKey != Keyboard.KEY_NONE && Keyboard.isKeyDown(fireKey)) {
                if (weapon.getCooldownRemaining() <= 0f && !wasActive) {
                    //fire
                    weapon.setForceFireOneFrame(true);
                }
            } else {
                wasActive = false;
            }
        }


    }
}
