package scripts.kissa.LOST_SECTOR.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class nskr_cdummy1Effect implements EveryFrameWeaponEffectPlugin {

    //ship needs weapon as deco to work (stupid)

    public static final Set<String> PROJ_IDS = new HashSet();
    static {
        PROJ_IDS.add("nskr_causality1_dummy_shot");
    }

    public static final Color CORE_COLOR = new Color(255, 84, 252, 120);
    public static final Color FRINGE_COLOR = new Color(255, 43, 128, 205);

    private final IntervalUtil arcInterval = new IntervalUtil(1.25f, 2.00f);

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
                for (int x = 0; x < 1; x++) {
                    float angle = (float) Math.random() * 360f;
                    float distance = (float) Math.random() * 75f + 25f;
                    Vector2f point1 = MathUtils.getPointOnCircumference(point, distance, angle);

                    engine.spawnEmpArcVisual(point, new SimpleEntity(point), point1, new SimpleEntity(point1), 10f, CORE_COLOR, FRINGE_COLOR);

                }
            }
        }
    }
}
