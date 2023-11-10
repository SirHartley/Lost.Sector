package scripts.kissa.LOST_SECTOR.shipsystems;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.impl.combat.InterdictorArrayStats;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.FindShipFilter;
import org.lazywizard.lazylib.MathUtils;

public class nskr_animebadStats extends BaseShipSystemScript {

    //vanilla code mostly
    public static final Object SHIP_KEY = new Object();
    public static final Object TARGET_KEY = new Object();

    private final IntervalUtil timer = new IntervalUtil(2.5f, 2.5f);

    public static final float WING_EFFECT_RANGE = 200f;
    public static final float DMG_FACTOR = 2f;

    private static final float RANGE = 1200f;
    public static final Color EFFECT_COLOR = new Color(100,165,255,75);
    public static final Color EFFECT_COLOR2 = new Color(255, 36, 61,155);

    boolean text = true;
    boolean enginesb = true;

    private boolean updated = false;

    public static class TargetData {
        public ShipAPI target;
        public float sinceLastAfterimage = 0f;
        public boolean lastAbove = false;
        public TargetData(ShipAPI target) {
            this.target = target;
        }
    }

    //one liners
    public final ArrayList<String> AnimeBadList = new ArrayList<>();
    {
        AnimeBadList.add("You are already dead");
        AnimeBadList.add("Fate sealed!");
        AnimeBadList.add("Perish!");
        AnimeBadList.add("Die!");
        AnimeBadList.add("Cross the mortal plane!");
        AnimeBadList.add("Nothing personal");
        AnimeBadList.add("Seems like dying is the easy part");
        AnimeBadList.add("I'll give you the easy way out");
        AnimeBadList.add("Return to ashes!");
        AnimeBadList.add("Welcome to oblivion");
        AnimeBadList.add("Annihilation!");
        AnimeBadList.add("Wither away");
        AnimeBadList.add("Disintegrated!");
        AnimeBadList.add("Join the universe!");
        AnimeBadList.add("Past the event horizon");
        AnimeBadList.add("Be destroyed!");
        AnimeBadList.add("Have a nice life");
    }

    public void apply(MutableShipStatsAPI stats, final String id, State state, float effectLevel) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        final String targetDataKey = ship.getId() + "_nskr_interdictor_target_data";

        if (!updated) {
            text = false;

            updated = true;
        }

        Object targetDataObj = Global.getCombatEngine().getCustomData().get(targetDataKey);
        if (state == State.IN && targetDataObj == null) {
            ShipAPI target = findTarget(ship);
            Global.getCombatEngine().getCustomData().put(targetDataKey, new InterdictorArrayStats.TargetData(target));
        } else if (state == State.IDLE && targetDataObj != null) {
            Global.getCombatEngine().getCustomData().remove(targetDataKey);
        }
        if (targetDataObj == null || ((InterdictorArrayStats.TargetData) targetDataObj).target == null) return;

        final InterdictorArrayStats.TargetData targetData = (InterdictorArrayStats.TargetData) targetDataObj;

        List<ShipAPI> targets = new ArrayList<>();
        if (targetData.target.isFighter() || targetData.target.isDrone()) {
            CombatEngineAPI engine = Global.getCombatEngine();
            List<ShipAPI> ships = engine.getShips();
            for (ShipAPI other : ships) {
                if (other.isShuttlePod()) continue;
                if (other.isHulk()) continue;
                if (!other.isDrone() && !other.isFighter()) continue;
                if (other.getOriginalOwner() != targetData.target.getOriginalOwner()) continue;

                float dist = Misc.getDistance(other.getLocation(), targetData.target.getLocation());
                if (dist > WING_EFFECT_RANGE) continue;

                targets.add(other);
            }
        } else {
            targets.add(targetData.target);
        }

        boolean first = true;
        for (ShipAPI target : targets) {
            if (effectLevel >= 1) {
                Color color = EFFECT_COLOR2;
                color = Misc.setAlpha(color, 255);

                if (!text) {
                    target.getFluxTracker().showOverloadFloatyIfNeeded(AnimeBadList.get(MathUtils.getRandomNumberInRange(0, AnimeBadList.size() - 1)), color, 6f, true);
                    text = true;
                }

                targetData.target.getMutableStats().getHullDamageTakenMult().modifyMult(id, DMG_FACTOR);
                targetData.target.getMutableStats().getArmorDamageTakenMult().modifyMult(id, DMG_FACTOR);
                targetData.target.getMutableStats().getShieldDamageTakenMult().modifyMult(id, DMG_FACTOR);
                targetData.target.getMutableStats().getEmpDamageTakenMult().modifyMult(id, DMG_FACTOR);


                timer.advance(Global.getCombatEngine().getElapsedInLastFrame());
                if (timer.intervalElapsed()) {
                ShipEngineControllerAPI ec = target.getEngineController();
                float limit = ec.getFlameoutFraction();
                if (target.isDrone() || target.isFighter()) {
                    limit = 1f;
                }

                float disabledSoFar = 0f;
                boolean disabledAnEngine = false;
                List<ShipEngineAPI> engines = new ArrayList<>(ec.getShipEngines());
                Collections.shuffle(engines);

                for (ShipEngineAPI engine : engines) {
                    if (engine.isDisabled()) continue;
                    float contrib = engine.getContribution();
                    if (disabledSoFar + contrib <= limit) {
                        engine.disable();
                        disabledSoFar += contrib;
                        disabledAnEngine = true;
                    }
                }
                if (!disabledAnEngine) {
                    for (ShipEngineAPI engine : engines) {
                        if (engine.isDisabled()) continue;
                        engine.disable();
                        break;
                    }
                }

                ec.computeEffectiveStats(ship == Global.getCombatEngine().getPlayerShip());
                enginesb = false;
            }
        }

            if (effectLevel > 0) {
                float jitterLevel = effectLevel;
                float maxRangeBonus = 2f + target.getCollisionRadius() * 0.25f;
                float jitterRangeBonus = jitterLevel * maxRangeBonus;
                if (state == State.OUT) {
                    jitterRangeBonus = maxRangeBonus + (1f - jitterLevel) * maxRangeBonus;
                }
                target.setJitter(this,
                        EFFECT_COLOR2,
                        jitterLevel, 6, 0f, 0 + jitterRangeBonus);

                if (first) {
                    ship.setJitter(this,
                            EFFECT_COLOR2,
                            jitterLevel, 6, 0f, 0 + jitterRangeBonus);
                }
            }
            //unapply
            if (effectLevel == 0) {
                targetData.target.getMutableStats().getHullDamageTakenMult().unmodify(id);
                targetData.target.getMutableStats().getArmorDamageTakenMult().unmodify(id);
                targetData.target.getMutableStats().getShieldDamageTakenMult().unmodify(id);
                targetData.target.getMutableStats().getEmpDamageTakenMult().unmodify(id);

                updated = false;
            }
        }
    }

    //never called
    public void unapply(MutableShipStatsAPI stats, String id) {
    }

    protected ShipAPI findTarget(ShipAPI ship) {
        FindShipFilter filter = new FindShipFilter() {
            public boolean matches(ShipAPI ship) {
                return !ship.getEngineController().isFlamedOut();
            }
        };

        float range = getMaxRange(ship);
        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        ShipAPI target = ship.getShipTarget();
        if (target != null) {
            float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
            float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
            if (dist > range + radSum) target = null;
        } else {
            if (target == null || target.getOwner() == ship.getOwner()) {
                if (player) {
                    target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), HullSize.FRIGATE, range, true, filter);
                } else {
                    Object test = ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
                    if (test instanceof ShipAPI) {
                        target = (ShipAPI) test;
                        float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
                        float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
                        if (dist > range + radSum) target = null;
                    }
                }
            }
            if (target == null) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), HullSize.FRIGATE, range, true, filter);
            }
        }

        return target;
    }

    public static float getMaxRange(ShipAPI ship){
        if (ship==null) return 0f;
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(RANGE);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != SystemState.IDLE) return null;

        ShipAPI target = findTarget(ship);
        if (target != null && target != ship) {
            return "READY";
        }
        if (target == null && ship.getShipTarget() != null) {
            return "OUT OF RANGE";
        }
        return "NO TARGET";
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if (system.isActive()) return true;
        ShipAPI target = findTarget(ship);
        return target != null && target != ship;
    }

}
