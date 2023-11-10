package scripts.kissa.LOST_SECTOR.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class nskr_mfrocketEffect implements EveryFrameWeaponEffectPlugin {

    public static final Set<String> PROJ_IDS = new HashSet();
    static {
        PROJ_IDS.add("nskr_mfrocket");
    }
    public static final Color CORE_COLOR = new Color(195, 84, 255, 150);
    public static final Color FRINGE_COLOR = new Color(43, 181, 255, 250);
    public static final Color FLARE_CORE_COLOR = new Color(195, 84, 255, 100);
    public static final Color FLARE_FRINGE_COLOR = new Color(43, 181, 255, 50);
    private final IntervalUtil arcInterval = new IntervalUtil(0.40f, 1.00f);

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused() || weapon == null) {
            return;
        }

        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
            String spec = proj.getProjectileSpecId();

            if (!PROJ_IDS.contains(spec)) {
                continue;
            }

            ShipAPI ship = proj.getSource();
            Vector2f point = proj.getLocation();
            arcInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if (arcInterval.intervalElapsed()) {
                for (int x = 0; x < 3; x++) {
                    float angle = (float) Math.random() * 360f;
                    float distance = (float) Math.random() * 30f;
                    float bias = mathUtil.BiasFunction(Math.random(),0.4f);
                    distance *= bias;
                    distance += 5f;
                    Vector2f point1 = MathUtils.getPointOnCircumference(point, distance, angle);
                    if (proj != null) {
                        //MagicLensFlare.createSharpFlare(
                        //        engine, ship, point,
                        //        0.1f, MathUtils.getRandomNumberInRange(1f,5f), VectorUtils.getAngle(proj.getLocation(), point),
                        //        FLARE_FRINGE_COLOR, FLARE_CORE_COLOR);
                        engine.addSmoothParticle(
                                point1,
                                proj.getVelocity(),
                                MathUtils.getRandomNumberInRange(10, 20),
                                0.25f,
                                1,
                                CORE_COLOR
                        );
                        engine.addHitParticle(
                                point1,
                                proj.getVelocity(),
                                MathUtils.getRandomNumberInRange(5, 10),
                                1f,
                                0.1f,
                                FRINGE_COLOR
                        );
                    }
                }
            }
        }
    }
}
