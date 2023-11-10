package scripts.kissa.LOST_SECTOR.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.shipsystems.ai.nskr_stasisAI;
import scripts.kissa.LOST_SECTOR.shipsystems.nskr_stasisStats;
import scripts.kissa.LOST_SECTOR.util.blastSpriteCreator;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class nskr_stasis {

    public static class stasisProjectileVisualListener implements AdvanceableListener {

        public static final Color NEBULA_COLOR = new Color(43, 103, 255, 70);
        public static final Color NEBULA_FRINGE_COLOR = new Color(43, 142, 255, 30);

        public DamagingProjectileAPI projectile;
        public ShipAPI ship;
        private final IntervalUtil nebulaInterval = new IntervalUtil(0.067f, 0.133f);

        public stasisProjectileVisualListener(DamagingProjectileAPI projectile, ShipAPI ship) {
            this.ship = ship;
            this.projectile = projectile;
        }

        @Override
        public void advance(float amount) {
            CombatEngineAPI engine = Global.getCombatEngine();
            //engine.addFloatingText(ship.getLocation(), "LMAO", 54f, Color.CYAN, null,1f,1f);
            if (projectile == null){
                //engine.addFloatingText(ship.getLocation(), "KEKW", 34f, Color.RED, null,1f,1f);
                ship.removeListener(this);
                return;
            }
            if (engine.isPaused()) {
                return;
            }
            if (projectile.didDamage()){
                ship.removeListener(this);
                return;
            }
            Vector2f point = projectile.getLocation();
            //reached max range
            if (projectile.isFading() || projectile.isExpired()){
                trigger(engine, point);
                return;
            }

            nebulaInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if (nebulaInterval.intervalElapsed()) {
                //FX
                engine.addNebulaParticle(point, new Vector2f(MathUtils.getRandomNumberInRange(-10f,10f),MathUtils.getRandomNumberInRange(-10f,10f)),
                        MathUtils.getRandomNumberInRange(67f,90f), 0.67f, 0.1f,0.5f, 1.5f, NEBULA_COLOR);
                engine.addSwirlyNebulaParticle(point, new Vector2f(MathUtils.getRandomNumberInRange(-7f,7f),MathUtils.getRandomNumberInRange(-7f,7f)),
                        MathUtils.getRandomNumberInRange(125f,150f), 0.67f, 0.1f,0.5f, 2f, NEBULA_FRINGE_COLOR, true);

            }

        }

        private void trigger(CombatEngineAPI engine, Vector2f point) {
            explode(point, ship);
            engine.removeEntity(projectile);
            ship.removeListener(this);
        }
    }

    public static class stasisEffectListener implements AdvanceableListener {

        public static final Color TEXT_COLOR = new Color(55, 95, 255,255);
        public static final Color JITTER_COLOR = new Color(0, 13, 150, 100);
        public static final Color COLOR = new Color(29, 135, 222, 175);

        public static final Vector2f ZERO = new Vector2f();
        public static final String ID = "nskr_stasis_projector";

        public static final float MAX_SPEED_MULT = 0.60f;
        public static final float MANEUVER_MULT = 0.50f;
        public static final float WEAPON_SPEED_MULT = 0.40f;

        public static final Map mag = new HashMap();
        static {
            mag.put(ShipAPI.HullSize.FIGHTER, 0.50f);
            mag.put(ShipAPI.HullSize.FRIGATE, 0.50f);
            mag.put(ShipAPI.HullSize.DESTROYER, 0.70f);
            mag.put(ShipAPI.HullSize.CRUISER, 0.85f);
            mag.put(ShipAPI.HullSize.CAPITAL_SHIP, 1.00f);
        }

        public ShipAPI ship;
        public ShipAPI source;

        private float elapsed = 0f;

        private boolean newHit;
        public stasisEffectListener(ShipAPI ship, ShipAPI source) {
            this.ship = ship;
            this.source = source;
            newHit = true;
        }

        @Override
        public void advance(float amount) {
            CombatEngineAPI engine = Global.getCombatEngine();
            if (!ship.isAlive()){
                ship.removeListener(this);
                return;
            }
            if (engine.isPaused()) {
                return;
            }
            elapsed += amount;
            if (elapsed>nskr_stasisStats.MAX_DURATION){
                ship.getMutableStats().getMaxSpeed().unmodify(ID);
                ship.getMutableStats().getAcceleration().unmodify(ID);
                ship.getMutableStats().getDeceleration().unmodify(ID);
                ship.getMutableStats().getMaxTurnRate().unmodify(ID);
                ship.getMutableStats().getTurnAcceleration().unmodify(ID);
                ship.getMutableStats().getWeaponTurnRateBonus().unmodify(ID);

                ship.removeListener(this);
                return;
            }

            float speedMod, agilityMod, weaponTurnMod, mod;
            if (mag.containsKey(ship.getHullSize())) {
                mod = (float) mag.get(ship.getHullSize());
            } else mod = 1f;
            speedMod = mathUtil.lerp(1f, MAX_SPEED_MULT, mod);
            agilityMod = mathUtil.lerp(1f, MANEUVER_MULT, mod);
            weaponTurnMod = mathUtil.lerp(1f, WEAPON_SPEED_MULT, mod);

            //debuff
            ship.getMutableStats().getMaxSpeed().modifyMult(ID, speedMod);
            ship.getMutableStats().getAcceleration().modifyMult(ID, agilityMod);
            ship.getMutableStats().getDeceleration().modifyMult(ID, agilityMod);
            ship.getMutableStats().getMaxTurnRate().modifyMult(ID, agilityMod);
            ship.getMutableStats().getTurnAcceleration().modifyMult(ID,agilityMod );
            ship.getMutableStats().getWeaponTurnRateBonus().modifyMult(ID, weaponTurnMod);

            if (ship == Global.getCombatEngine().getPlayerShip()) {
                Global.getCombatEngine().maintainStatusForPlayerShip(ID+"_tooltip",
                        source.getSystem().getSpecAPI().getIconSpriteName(),
                        source.getSystem().getDisplayName(),
                        "In Stasis! Speed, Maneuverability and weapon turn rate reduced.", true);
            }

            //FX
            if (newHit){
                if (ship.getHullSize()!= ShipAPI.HullSize.FIGHTER) {
                    Global.getCombatEngine().addFloatingText(ship.getLocation(), "In stasis", 24f, TEXT_COLOR, null, 1f, 1f);
                }
                //Flag for AI
                nskr_stasisAI.ShipSpecificData data = (nskr_stasisAI.ShipSpecificData) Global.getCombatEngine().getCustomData().get("STASIS_AI_DATA_KEY" + ship.getId());
                if (data != null) {
                    data.sinceEffected = MathUtils.getRandomNumberInRange(-1f, 0f);
                    Global.getCombatEngine().getCustomData().put("STASIS_AI_DATA_KEY" + ship.getId(), data);
                }
                newHit = false;
            }

            ship.setJitter(ship, JITTER_COLOR, 1f, 4, 0f, 15f);

            //particles
            if (Math.random()<1.00f * (engine.getElapsedInLastFrame()*60f)) {
                Vector2f particlePos, particleVel;
                particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), (ship.getCollisionRadius() + 50f) * (float)Math.random());
                particleVel = Vector2f.sub(ship.getLocation(), particlePos, null);
                Global.getCombatEngine().addSmokeParticle(particlePos, mathUtil.scaleVector(particleVel, 0.1f), 5f, 0.67f, 1f,
                        COLOR);
            }
        }
    }

    public static final String SPRITE_PATH_1 = "graphics/fx/explosion0.png";
    public static final String SPRITE_PATH_2 = "graphics/fx/nskr_blast_plasmal.png";

    public static final Color SHOCKWAVE_COLOR_1 = new Color(123, 179, 255, 50);
    public static final Color SHOCKWAVE_COLOR_2 = new Color(161, 200, 255, 15);
    public static final Color NEBULA_COLOR = new Color(52, 147, 255, 100);
    public static final Color PARTICLE_COLOR = new Color(54, 201, 255, 205);

    public static void explode(Vector2f point, ShipAPI source) {
        //actual effect
        for (ShipAPI ship : CombatUtils.getShipsWithinRange(point, nskr_stasisStats.MAX_ON_HIT_RANGE)){
            if (!ship.isAlive()) continue;
            if (ship.getOwner() == source.getOwner()) continue;
            if (ship.getCollisionClass()==CollisionClass.NONE) continue;
            if (MathUtils.getDistance(ship.getLocation(), point) > nskr_stasisStats.MAX_ON_HIT_RANGE) continue;
            //add
            ship.addListener(new stasisEffectListener(ship, source));
        }

        Global.getSoundPlayer().playSound("nskr_stasis_activate", 1.0f, 1.0f, point, new Vector2f());

        //FX
        blastSpriteCreator.blastSpriteListener shockwave1 = new blastSpriteCreator.blastSpriteListener(source, point, 1.25f, 350f, SHOCKWAVE_COLOR_1);
        shockwave1.customSpritePath = SPRITE_PATH_1;
        shockwave1.sizeEaseOutSine = true;
        shockwave1.alphaEaseInSine = true;
        shockwave1.endSizeMult = 1.1f;
        source.addListener(shockwave1);

        //blastSpriteCreator.blastSpriteListener shockwave2 = new blastSpriteCreator.blastSpriteListener(source, point, 1.75f, 450f, SHOCKWAVE_COLOR_2);
        //shockwave2.customSpritePath = SPRITE_PATH_2;
        //shockwave2.sizeEaseOutSine = true;
        //shockwave2.alphaEaseInSine = true;
        //shockwave2.endSizeMult = 1.1f;
        //source.addListener(shockwave2);

        Global.getCombatEngine().addSwirlyNebulaParticle(point, new Vector2f(),
                300f, 0.80f, 0.1f,0.5f, 1.0f, NEBULA_COLOR, true);

        //particle fx
        Vector2f particlePos, particleVel;
        Color color = util.randomiseColor(PARTICLE_COLOR, 25, 0,25,25,false);
        for (int x = 0; x < 150; x++) {
            particlePos = MathUtils.getRandomPointOnCircumference(point, (float) Math.random() * 400f);
            particleVel = Vector2f.sub(particlePos, point, null);
            Global.getCombatEngine().addSmokeParticle(particlePos, mathUtil.scaleVector(particleVel, 0.25f), 4f, 0.50f, 1.5f,
                    color);
        }
    }
}