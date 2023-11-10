package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

import java.awt.*;

public class nskr_harmonics {

    public static final Color JITTER_UNDER_COLOR = new Color(83, 255, 249, 250);

    public static class harmonicsSourceListener implements AdvanceableListener {

        public ShipAPI source;
        public ShipAPI target;
        private MutableShipStatsAPI sourceStats;
        private MutableShipStatsAPI targetStats;
        private float timer = 0f;
        private float maxSpeedTop;
        private float accelerationTop;
        private float decelerationTop;
        private float maxTurnRateTop;
        private float turnAccelerationTop;

        public harmonicsSourceListener(ShipAPI source, ShipAPI target) {
            this.source = source;
            this.target = target;
            this.sourceStats = source.getMutableStats();
            this.targetStats = target.getMutableStats();

            //cap at +250f
            this.maxSpeedTop = Math.min(Math.max(0f, targetStats.getMaxSpeed().modified - sourceStats.getMaxSpeed().modified), 250f);
            this.accelerationTop = Math.max(0f, targetStats.getAcceleration().modified - sourceStats.getAcceleration().modified);
            this.decelerationTop = Math.max(0f, targetStats.getDeceleration().modified - sourceStats.getDeceleration().modified);
            this.maxTurnRateTop = Math.max(0f, targetStats.getMaxTurnRate().modified - sourceStats.getMaxTurnRate().modified);
            this.turnAccelerationTop = Math.max(0f, targetStats.getTurnAcceleration().modified - sourceStats.getTurnAcceleration().modified);
        }

        @Override
        public void advance(float amount) {
            CombatEngineAPI engine = Global.getCombatEngine();
            if (engine.isPaused()) return;

            String id = "nskr_harmonics_sourceManeuver";

            timer += amount;

            if (!source.isAlive()){
                sourceStats.getMaxSpeed().unmodify(id);
                sourceStats.getAcceleration().unmodify(id);
                sourceStats.getDeceleration().unmodify(id);
                sourceStats.getMaxTurnRate().unmodify(id);
                sourceStats.getTurnAcceleration().unmodify(id);
                source.removeListener(this);
                return;
            }
            if (source.getFluxTracker().isOverloadedOrVenting()){
                sourceStats.getMaxSpeed().unmodify(id);
                sourceStats.getAcceleration().unmodify(id);
                sourceStats.getDeceleration().unmodify(id);
                sourceStats.getMaxTurnRate().unmodify(id);
                sourceStats.getTurnAcceleration().unmodify(id);
                source.removeListener(this);
                return;
            }
            if (timer > nskr_harmonicsStats.MAX_DURATION){
                sourceStats.getMaxSpeed().unmodify(id);
                sourceStats.getAcceleration().unmodify(id);
                sourceStats.getDeceleration().unmodify(id);
                sourceStats.getMaxTurnRate().unmodify(id);
                sourceStats.getTurnAcceleration().unmodify(id);
                source.removeListener(this);
                return;
            }

            float mod = mathUtil.inverse(mathUtil.normalize(timer, 0f, nskr_harmonicsStats.MAX_DURATION));

            //engine.addFloatingText(source.getLocation(), ""+mod, 24f, Color.RED, null, 1f,1f);

            sourceStats.getMaxSpeed().modifyFlat(id, mathUtil.lerp(0f, maxSpeedTop, mod));
            sourceStats.getAcceleration().modifyFlat(id, mathUtil.lerp(0f, accelerationTop, mod));
            sourceStats.getDeceleration().modifyFlat(id, mathUtil.lerp(0f, decelerationTop, mod));
            sourceStats.getMaxTurnRate().modifyFlat(id, mathUtil.lerp(0f, maxTurnRateTop, mod));
            sourceStats.getTurnAcceleration().modifyFlat(id, mathUtil.lerp(0f, turnAccelerationTop, mod));

            if (source == Global.getCombatEngine().getPlayerShip()) {
                Global.getCombatEngine().maintainStatusForPlayerShip(id+"_tooltip",
                        source.getSystem().getSpecAPI().getIconSpriteName(),
                        source.getSystem().getDisplayName(),
                        "In Harmonics, Top speed set to " + (int)sourceStats.getMaxSpeed().getModifiedValue(), false);
            }

            //fx
            source.setJitterUnder(id, JITTER_UNDER_COLOR, 9f, 9, 0.55f);
            source.setJitterShields(false);

            //sound
            Global.getSoundPlayer().playLoop("system_temporalshell_loop", source, 0.8f, 0.65f, source.getLocation(), source.getVelocity());
        }
    }

    public static class harmonicsTargetListener implements AdvanceableListener {

        public ShipAPI source;
        public ShipAPI target;
        private MutableShipStatsAPI stats;
        private float timer = 0f;

        public harmonicsTargetListener(ShipAPI source, ShipAPI target) {
            this.source = source;
            this.target = target;
            this.stats = target.getMutableStats();
        }

        @Override
        public void advance(float amount) {
            CombatEngineAPI engine = Global.getCombatEngine();
            if (engine.isPaused()) return;

            String id = "nskr_harmonics_targetTimeMult";

            //account for timeMult
            timer += amount / stats.getTimeMult().getModifiedValue();

            if (!target.isAlive()){
                stats.getTimeMult().unmodify(id);
                target.removeListener(this);
                return;
            }
            if (target.getFluxTracker().isOverloadedOrVenting()){
                stats.getTimeMult().unmodify(id);
                target.removeListener(this);
                return;
            }
            if (timer > nskr_harmonicsStats.MAX_DURATION){
                stats.getTimeMult().unmodify(id);
                target.removeListener(this);
                return;
            }

            float mod = mathUtil.inverse(mathUtil.normalize(timer, 0f, nskr_harmonicsStats.MAX_DURATION));

            stats.getTimeMult().modifyPercent(id, mod * nskr_harmonicsStats.MAX_TIMEFLOW);

            if (target == Global.getCombatEngine().getPlayerShip()) {
                Global.getCombatEngine().maintainStatusForPlayerShip(id+"_tooltip",
                        source.getSystem().getSpecAPI().getIconSpriteName(),
                        source.getSystem().getDisplayName(),
                        "In Harmonics, Timeflow set to " + mathUtil.roundToTwoDecimals(stats.getTimeMult().getModifiedValue()), false);
            }

            //fx
            target.setJitterUnder(id, JITTER_UNDER_COLOR, 9f, 9, 0.55f);
            target.setJitterShields(false);

            //sound
            Global.getSoundPlayer().playLoop("system_temporalshell_loop", target, 1.2f, 0.65f, target.getLocation(), target.getVelocity());
        }
    }

}
