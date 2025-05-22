package lostsector.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import lostsector.shipsystems.ai.StasisAI;
import org.lwjgl.input.Keyboard;

public class StasisEffect  implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

        ShipAPI ship = projectile.getSource();
        ship.addListener(new Stasis.stasisProjectileVisualListener(projectile, ship));

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
        //so F hast to be released and we don't immediately fire
        if (engine.getPlayerShip()==ship && ship.getSystem().isActive()) {
            wasActive = true;
        }
        //so we fire when pressing f while system is cooling down
        if (engine.getPlayerShip()==ship && ship.getSystem().isCoolingDown()) {
            //fire when f is pressed
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F"))) {
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
