package scripts.kissa.LOST_SECTOR.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lwjgl.input.Keyboard;
import scripts.kissa.LOST_SECTOR.shipsystems.ai.nskr_stasisAI;

public class nskr_stasisEffect  implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

        ShipAPI ship = projectile.getSource();
        ship.addListener(new nskr_stasis.stasisProjectileVisualListener(projectile, ship));

        //Flag for AI
        nskr_stasisAI.ShipSpecificData data = (nskr_stasisAI.ShipSpecificData) Global.getCombatEngine().getCustomData().get("STASIS_AI_DATA_KEY" + ship.getId());
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
