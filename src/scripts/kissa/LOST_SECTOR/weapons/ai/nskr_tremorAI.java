//////////////////////
//Initially created by DarkRevenant and modified from Ship and Weapon Pack & Underworld
//////////////////////
package scripts.kissa.LOST_SECTOR.weapons.ai;

import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

import java.util.List;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class nskr_tremorAI extends nskr_baseMissile {

    //more magic idk whats going on..

    public static final float FIRE_INACCURACY = 50f;
    public static final float LEAD_GUIDANCE_FACTOR = 0.5f;
    public static final float LEAD_GUIDANCE_FACTOR_FROM_ECCM = 0.25f;
    public static final float VELOCITY_DAMPING_FACTOR = 0.25f;

    private final float inaccuracy;
    private float timeAccum = 0f;
    private float weaveSineAAmplitude = 32.5f; // degrees offset
    private final float weaveSineAPeriod;
    private final float weaveSineAPhase;
    private float weaveSineBAmplitude = 32.5f; // degrees offset
    private final float weaveSineBPeriod;
    private final float weaveSineBPhase;

    public nskr_tremorAI(MissileAPI missile, ShipAPI launchingShip) {
        super(missile, launchingShip);

        weaveSineAPhase = (float) (Math.random() * Math.PI * 2.0);
        weaveSineBPhase = (float) (Math.random() * Math.PI * 2.0);
        float totalAmp = weaveSineAAmplitude + weaveSineBAmplitude;
        weaveSineAAmplitude = MathUtils.getRandomNumberInRange(0f, totalAmp);
        weaveSineBAmplitude = totalAmp - weaveSineAAmplitude;
        weaveSineAPeriod = MathUtils.getRandomNumberInRange(2f, 4f);
        weaveSineBPeriod = MathUtils.getRandomNumberInRange(0.5f, 2f);
        inaccuracy = MathUtils.getRandomNumberInRange(-FIRE_INACCURACY, FIRE_INACCURACY);
    }

    @Override
    public void advance(float amount) {
        if (missile.isFizzling() || missile.isFading()) {
            return;
        }

        timeAccum += amount;

        if (!acquireTarget(amount)) {
            missile.giveCommand(ShipCommand.ACCELERATE);
            return;
        }

        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
        float guidance = LEAD_GUIDANCE_FACTOR;
        if (missile.getSource() != null) {
            guidance += Math.min(missile.getSource().getMutableStats().getMissileGuidance().getModifiedValue()
                    - missile.getSource().getMutableStats().getMissileGuidance().getBaseValue(), 1f)
                    * LEAD_GUIDANCE_FACTOR_FROM_ECCM;
        }
        Vector2f guidedTarget = intercept(missile.getLocation(), missile.getVelocity().length(), target.getLocation(),
                target.getVelocity());
        if (guidedTarget == null) {
            Vector2f projection = new Vector2f(target.getVelocity());
            float scalar = distance / (missile.getVelocity().length() + 1f);
            projection.scale(scalar);
            guidedTarget = Vector2f.add(target.getLocation(), projection, null);
        }
        Vector2f.sub(guidedTarget, target.getLocation(), guidedTarget);
        guidedTarget.scale(guidance);
        Vector2f.add(guidedTarget, target.getLocation(), guidedTarget);

        float adjustedDistance = MathUtils.getDistance(target, missile);
        float weaveLevel = 1f;
        if (adjustedDistance >= 1000f) {
            weaveLevel = 0f;
        } else if (adjustedDistance >= 500f) {
            weaveLevel = mathUtil.lerp(1f, 0f, (adjustedDistance - 500f) / 500f);
        }

        float weaveSineA = weaveSineAAmplitude * (float) FastTrig.sin((2.0 * Math.PI * timeAccum / weaveSineAPeriod)
                + weaveSineAPhase);
        float weaveSineB = weaveSineBAmplitude * (float) FastTrig.sin((2.0 * Math.PI * timeAccum / weaveSineBPeriod)
                + weaveSineBPhase);
        float weaveOffset = (weaveSineA + weaveSineB) * weaveLevel;

        float angularDistance = MathUtils.getShortestRotation(missile.getFacing(),
                MathUtils.clampAngle(VectorUtils.getAngleStrict(
                        missile.getLocation(), guidedTarget)
                        + weaveOffset + ((1f - weaveLevel) * inaccuracy)));
        float absDAng = Math.abs(angularDistance);

        missile.giveCommand(angularDistance < 0 ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);
        missile.giveCommand(ShipCommand.ACCELERATE);

        if (absDAng < Math.abs(missile.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR * (1f - weaveLevel)) {
            if ((1f - weaveLevel) > 0.01f) {
                missile.setAngularVelocity(angularDistance / (VELOCITY_DAMPING_FACTOR * (1f - weaveLevel)));
            }
        }
    }

    @Override
    protected ShipAPI findBestTarget() {
        ShipAPI best = null;
        float weight, bestWeight = 0f;
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        float maxDistance = getRemainingRange() + missile.getMaxSpeed() * 2f;
        int size = ships.size();
        for (int i = 0; i < size; i++) {
            ShipAPI tmp = ships.get(i);
            float mod;
            if (!isTargetValid(tmp)) {
                mod = 0f;
            } else {
                switch (tmp.getHullSize()) {
                    case FIGHTER:
                        mod = 1f;
                        break;
                    case FRIGATE:
                        mod = 15f;
                        break;
                    case DESTROYER:
                        mod = 13f;
                        break;
                    case CRUISER:
                        mod = 12f;
                        break;
                    case CAPITAL_SHIP:
                        mod = 10f;
                        break;
                    default:
                        mod = 0f;
                        break;
                }
            }
            float distance = MathUtils.getDistance(tmp, missile.getLocation());
            if (distance > maxDistance) {
                continue;
            }
            weight = (2500f / Math.max(distance, 250f)) * mod;
            if (weight > bestWeight) {
                best = tmp;
                bestWeight = weight;
            }
        }
        return best;
    }
}
