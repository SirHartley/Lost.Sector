//////////////////////
//Parts initially created by theDragn and modified from HTE
//////////////////////
package scripts.kissa.LOST_SECTOR.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;

import java.util.ArrayList;
import java.util.List;

import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.combatUtil;


public class nskr_protocolAI implements ShipSystemAIScript {

    //long timer so we don't get ADHD
    private final IntervalUtil timer = new IntervalUtil(1.00f, 2.00f);

    private CombatEngineAPI engine = null;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private ShipwideAIFlags flags;
    private int desiredMode;
    private List<WeaponAPI> weapons=new ArrayList<>();
    private List<ShipEngineControllerAPI.ShipEngineAPI> engines=new ArrayList<>();
    public static final ArrayList<ShipwideAIFlags.AIFlags> AWAY = new ArrayList<>();
    public static final ArrayList<ShipwideAIFlags.AIFlags> CON = new ArrayList<>();
    public static final ArrayList<ShipwideAIFlags.AIFlags> PURSUE = new ArrayList<>();
    static {
        AWAY.add(ShipwideAIFlags.AIFlags.NEEDS_HELP);
        AWAY.add(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON);
        CON.add(ShipwideAIFlags.AIFlags.BACK_OFF);
        CON.add(ShipwideAIFlags.AIFlags.BACK_OFF_MIN_RANGE);
        CON.add(ShipwideAIFlags.AIFlags.BACKING_OFF);
        CON.add(ShipwideAIFlags.AIFlags.DO_NOT_PURSUE);
        PURSUE.add(ShipwideAIFlags.AIFlags.PURSUING);
        PURSUE.add(ShipwideAIFlags.AIFlags.HARASS_MOVE_IN);
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        if (engine.isPaused() || ship.getShipAI() == null) {
            return;
        }
        float activeWeapons = 0;
        if (weapons != null) {
            activeWeapons = weapons.size();
            for (WeaponAPI w : weapons) {
                if (w.isDisabled() || w.isPermanentlyDisabled()) {
                    activeWeapons--;
                }
            }
            activeWeapons /= weapons.size();
        }

        int currentMode = system.getAmmo();
        float analysisRange = getLongestRange(ship, weapons);
        float speedLevel = 25f;
        float atkLevel = 0f;
        float defLevel = 0f;

        boolean hasTakenHullDamage = ship.getHullLevel() < 0.97f;

        float activeEngines = 0;
        if (engines != null) {
            activeEngines = engines.size();
            for (ShipEngineControllerAPI.ShipEngineAPI e : engines) {
                if (e.isDisabled() || e.isPermanentlyDisabled()) {
                    activeEngines--;
                }
            }
            activeEngines /= engines.size();
        }

        timer.advance(amount);
        if (timer.intervalElapsed()) {

            if (engines != null && weapons != null) {
                weapons = ship.getAllWeapons();
                engines = ship.getEngineController().getShipEngines();
            }

            if (activeWeapons < 0.70f) {
                speedLevel += 40f;
            }

            speedLevel += 25f * ship.getFluxLevel();

            if (hasTakenHullDamage) {
                speedLevel += 25f;
            }


            List<ShipAPI> farships = new ArrayList<>(100);
            farships.addAll(AIUtils.getNearbyEnemies(ship, analysisRange));
            if (farships.size() <= 0) {
                speedLevel += 50f;
            }


            for (ShipwideAIFlags.AIFlags f : AWAY) {
                if (flags.hasFlag(f)) {
                    defLevel += 15f;
                }
            }
            for (ShipwideAIFlags.AIFlags f : CON) {
                if (flags.hasFlag(f)) {
                    defLevel += 15f;
                    speedLevel += 15f;
                }
            }
            for (ShipwideAIFlags.AIFlags f : PURSUE) {
                if (flags.hasFlag(f)) {
                    speedLevel += 25f;
                }
            }

            List<ShipAPI> ships = new ArrayList<>(100);
            ships.addAll(combatUtil.getShipsWithinRange(ship.getLocation(), analysisRange+150f));
            for (ShipAPI possibleship : ships) {
                if (possibleship.getOwner() != ship.getOwner() && possibleship.getHullSize() != ShipAPI.HullSize.FIGHTER) {

                    ShipAPI.HullSize size = ship.getHullSize();
                    ShipAPI.HullSize otherSize = possibleship.getHullSize();

                    if (otherSize.compareTo(size) >= 0) {
                        atkLevel += 75f;
                    } else if (otherSize.compareTo(size) < 0) {
                        atkLevel += 40f;
                    }
                }
            }

            if (ship.getFluxLevel() < 0.50f) {
                atkLevel += 25f;
            }

            defLevel += 50f * ship.getFluxLevel();

            if (hasTakenHullDamage) {
                defLevel += 25f;
            }

            List<ShipAPI> fighters = new ArrayList<>(100);
            fighters.addAll(combatUtil.getShipsWithinRange(ship.getLocation(), 400));
            for (ShipAPI possiblefighters : fighters) {
                if (possiblefighters.getOwner() != ship.getOwner() && possiblefighters.getHullSize() == ShipAPI.HullSize.FIGHTER) {
                    defLevel += 25f;
                }
            }

            List<DamagingProjectileAPI> possibleTargets = new ArrayList<>(200);
            possibleTargets.addAll(combatUtil.getMissilesWithinRange(ship.getLocation(), 600f));
            for (DamagingProjectileAPI possibleTarget : possibleTargets) {
                if (possibleTarget.getOwner() != ship.getOwner() && !possibleTarget.isFading() && possibleTarget.getCollisionClass() != CollisionClass.NONE) {

                    if (possibleTarget.getDamageType() == DamageType.FRAGMENTATION) {
                        defLevel += ((float) Math.sqrt(0.25f * possibleTarget.getDamageAmount() + possibleTarget.getEmpAmount() * 0.50f) / 2);
                    } else {
                        defLevel += ((float) Math.sqrt(possibleTarget.getDamageAmount() + possibleTarget.getEmpAmount() * 0.50f) / 2);
                    }
                }

                if (activeEngines < 0.70f) {
                    defLevel += 40f;
                }

            }

            if (atkLevel > 100f){
                atkLevel = 100f;
            }

            //macgyver debugger
            //engine.addFloatingText(ship.getLocation(), "speed" + Math.round(speedLevel) + "atk" + Math.round(atkLevel) + "def" + Math.round(defLevel), 32f, Color.cyan, ship, 0.5f, 1.0f);

            if (speedLevel >= atkLevel && speedLevel >= defLevel) {
                this.desiredMode = 1;
            }

            if (atkLevel >= speedLevel && atkLevel >= defLevel) {
                this.desiredMode = 2;
            }

            if (defLevel >= atkLevel && defLevel >= speedLevel) {
                this.desiredMode = 3;
            }
        }

            if (currentMode != this.desiredMode && this.system.getState() == ShipSystemAPI.SystemState.IDLE) {
                this.ship.useSystem();
            }
        }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
        this.system = system;
        this.flags = flags;
        this.desiredMode = 1;
    }

    public static float getLongestRange(ShipAPI ship, List<WeaponAPI> weapons) {
        float longestRange = 0.0f;
        WeaponAPI.WeaponSize largestWeaponSize = WeaponAPI.WeaponSize.SMALL;

        for (WeaponAPI weapon : weapons) {
            WeaponAPI.WeaponSize size = weapon.getSize();
            if (largestWeaponSize == WeaponAPI.WeaponSize.SMALL && weapon.getSize() != largestWeaponSize) {
                largestWeaponSize = weapon.getSize();
            }
            if (largestWeaponSize != WeaponAPI.WeaponSize.MEDIUM || weapon.getSize() != WeaponAPI.WeaponSize.LARGE) continue;
            largestWeaponSize = WeaponAPI.WeaponSize.LARGE;
        }

        for (WeaponAPI weapon : weapons) {
            float range;
            if (weapon.getType() == WeaponAPI.WeaponType.MISSILE || weapon.getSize() != largestWeaponSize || weapon.hasAIHint(WeaponAPI.AIHints.PD) || !((range = weapon.getRange()) > longestRange)) continue;
            longestRange = range;
        }
        if (longestRange < 100.0f) {
            longestRange = 600.0f * ship.getMutableStats().getEnergyWeaponRangeBonus().computeEffective(1.0f);
        }
        return longestRange;
    }
}
