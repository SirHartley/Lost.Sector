package lostsector.combat.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class CausalityDummyEffect implements EveryFrameWeaponEffectPlugin {

    // Weapon effect plugins run only for mounted weapons, so ships that need this effect carry the weapon as a decorative mount.

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

        arcInterval.advance(engine.getElapsedInLastFrame());
        if (!arcInterval.intervalElapsed()) {
            return;
        }

        // CausalityStats fires these shots from a fake weapon, so they belong to this mount's ship, not to the mount.
        ShipAPI ship = weapon.getShip();
        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
            if (proj.getSource() != ship || !PROJ_IDS.contains(proj.getProjectileSpecId())) {
                continue;
            }

            Vector2f point = proj.getLocation();
            float angle = (float) Math.random() * 360f;
            float distance = (float) Math.random() * 75f + 25f;
            Vector2f point1 = MathUtils.getPointOnCircumference(point, distance, angle);

            engine.spawnEmpArcVisual(point, new SimpleEntity(point), point1, new SimpleEntity(point1), 10f, CORE_COLOR, FRINGE_COLOR);
        }
    }
}
