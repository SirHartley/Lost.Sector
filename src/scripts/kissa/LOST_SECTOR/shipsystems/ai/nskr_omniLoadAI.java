package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.shipsystems.nskr_omniLoadStats;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

public class nskr_omniLoadAI implements ShipSystemAIScript {

    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipwideAIFlags flags;
    private final IntervalUtil timer = new IntervalUtil(0.20f, 0.40f);

    private float maxCooldown = 0f;
    private float maxFlux = 0f;
    private int maxAmmo = 0;
    private int ammoWeaponCount = 0;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
        this.flags = flags;
        timer.randomize();
        for (WeaponAPI w : ship.getAllWeapons()) {
            if (!nskr_omniLoadStats.validWeapon(w)) continue;
            maxCooldown += w.getCooldown();
            maxFlux += w.getFluxCostToFire();
            if (w.getMaxAmmo()<Integer.MAX_VALUE){
                maxAmmo += w.getMaxAmmo();
                ammoWeaponCount++;
            }
        }
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        if (engine.isPaused() || ship.getShipAI() == null) {
            return;
        }
        //no weapons
        if (maxCooldown == 0f) return;

        float decisionLevel = 0f;

        timer.advance(amount);
        if (timer.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }
            //pick bonuses based on loadout
            float ammoBonus, cooldownBonus;
            if (ammoWeaponCount>=2){
                ammoBonus = 60f;
                cooldownBonus = 20f;
            } else if (ammoWeaponCount==1) {
                ammoBonus = 30f;
                cooldownBonus = 50f;
            } else {
                ammoBonus = 0f;
                cooldownBonus = 80f;
            }

            //flux penalty based on total cost
            float fluxMod = mathUtil.normalize(Math.min(maxFlux, 1500f), 0f, 1500f);
            //fluxed
            decisionLevel -= mathUtil.lerp(0f,40f*fluxMod, ship.getFluxLevel());
            //high ammo
            if (ship.getSystem().getAmmo() >= 2) decisionLevel += 30f;

            float currCooldown = 0f;
            int currAmmo = 0;
            for (WeaponAPI w : ship.getAllWeapons()) {
                if (!nskr_omniLoadStats.validWeapon(w)) continue;
                currCooldown += w.getCooldownRemaining();
                if (w.getMaxAmmo()<Integer.MAX_VALUE) currAmmo += w.getAmmo();
            }
            //cooldown
            float norm = mathUtil.normalize(currCooldown, 0f, maxCooldown);
            decisionLevel += mathUtil.lerp(0f, cooldownBonus, norm);
            //ammo
            if (ammoBonus>0f) {
                norm = mathUtil.normalize(currAmmo, 0, maxAmmo);
                decisionLevel += mathUtil.lerp(ammoBonus, 0f, norm);
            }

            //enemy ship status
            for (ShipAPI t : CombatUtils.getShipsWithinRange(ship.getLocation(), 500f)){
                if (t.getOwner()==ship.getOwner()) continue;
                if (t.getHullSize()== ShipAPI.HullSize.FIGHTER) continue;

                float angle = VectorUtils.getAngle(ship.getLocation(), t.getLocation());
                // ignore everything outside of a y degree cone
                if (Math.abs(MathUtils.getShortestRotation(angle, ship.getFacing())) > 30f) continue;

                //+level for fluxed or dying
                decisionLevel += t.getFluxLevel() * 30f;
                decisionLevel += mathUtil.inverse(t.getHullLevel()) * 30f;
                if (t.getFluxTracker().isOverloadedOrVenting()) decisionLevel += 20f;
            }

            //macgyver debugger
            //engine.addFloatingText(ship.getLocation(), "test " + decisionLevel, 60f, Color.cyan, ship, 0.5f, 1.0f);

            if (decisionLevel>70f) {
                ship.useSystem();
            }
        }
    }
}