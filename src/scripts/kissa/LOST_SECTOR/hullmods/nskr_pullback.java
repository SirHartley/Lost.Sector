package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Pair;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.*;
import java.util.List;

public class nskr_pullback extends BaseHullMod {
    //
    //data collection and FX for pullback system
    //
    public static final int MOVE_PER_CHARGE = 25;
    public static final Vector2f ZERO = new Vector2f();

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }
        ShipSpecificData data = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("PULLBACK_DATA_KEY" + ship.getId());
        if (data == null) {
            data = new ShipSpecificData();
        }
        //data nonsense
        data.maxAmmo = ship.getSystem().getMaxAmmo();
        data.isOn = ship.getSystem().isOn();
        data.location = new Vector2f(ship.getLocation().getX(), ship.getLocation().getY());
        data.player = ship == Global.getCombatEngine().getPlayerShip();
        data.ammo = ship.getSystem().getAmmo();
        int maxSize = (data.maxAmmo+1) * MOVE_PER_CHARGE;

        for (Iterator<Pair<Vector2f, Integer>> iter = data.points.listIterator(); iter.hasNext(); ) {
            Pair<Vector2f, Integer> a = iter.next();
            if (a.two>=maxSize) iter.remove();
            if (data.activated && a.two<= MOVE_PER_CHARGE){
                iter.remove();
            } else if (data.activated) {
                a.two -= MOVE_PER_CHARGE;
            }
        }
        data.activated = false;

        if (!data.isOn) {
            data.countInterval.advance(amount);
            if (data.points.isEmpty()){
                //init
                data.points.add(new Pair<>(data.location, 0));
                data.tPoint1 = data.location;
                data.tPoint2 = data.location;
                data.tPoint3 = data.location;
            }
            if (data.countInterval.intervalElapsed()) {
                data.points.add(new Pair<>(data.location, 0));
                // FX
                if (data.player && data.ammo>0) {
                    Vector2f loc = new Vector2f(data.tPoint1);
                    loc.x -= 8f * FastTrig.cos(ship.getFacing() * Math.PI / 180f);
                    loc.y -= 8f * FastTrig.sin(ship.getFacing() * Math.PI / 180f);

                    SpriteAPI sprite = ship.getSpriteAPI();
                    float offsetX = sprite.getWidth() / 2 - sprite.getCenterX();
                    float offsetY = sprite.getHeight() / 2 - sprite.getCenterY();
                    float trueOffsetX = (float) FastTrig.cos(Math.toRadians(ship.getFacing() - 90f)) * offsetX - (float) FastTrig.sin(Math.toRadians(ship.getFacing() - 90f)) * offsetY;
                    float trueOffsetY = (float) FastTrig.sin(Math.toRadians(ship.getFacing() - 90f)) * offsetX + (float) FastTrig.cos(Math.toRadians(ship.getFacing() - 90f)) * offsetY;
                    Vector2f trueLocation = new Vector2f(data.tPoint1.getX() + trueOffsetX, data.tPoint1.getY() + trueOffsetY);

                    MagicRender.battlespace(
                            Global.getSettings().getSprite(ship.getHullSpec().getSpriteName()),
                            MathUtils.getRandomPointInCircle(trueLocation, MathUtils.getRandomNumberInRange(0f, 20f)),
                            new Vector2f(0, 0),
                            new Vector2f(ship.getSpriteAPI().getWidth(), ship.getSpriteAPI().getHeight()),
                            new Vector2f(0, 0),
                            ship.getFacing() - 90f,
                            0f,
                            new Color(255, 205, 40, 90),
                            false,
                            1f,
                            1f,
                            0f,
                            0f,
                            0f,
                            0.1f,
                            0.1f,
                            0.1f,
                            CombatEngineLayers.BELOW_SHIPS_LAYER);
                }
                for (Pair<Vector2f, Integer> p : data.points) {
                    p.two += 1;
                    if (p.two==MOVE_PER_CHARGE) {
                        data.tPoint1 = p.one;
                    }
                    if (p.two==MOVE_PER_CHARGE*2f) {
                        data.tPoint2 = p.one;
                    }
                    if (p.two==MOVE_PER_CHARGE*3f) {
                        data.tPoint3 = p.one;
                    }
                    if (data.points.size() < MOVE_PER_CHARGE && p.two == data.points.size()-1) {
                        data.tPoint1 = p.one;
                    }
                    if (data.points.size() < MOVE_PER_CHARGE*2f && p.two == data.points.size()-1) {
                        data.tPoint2 = p.one;
                    }
                    if (data.points.size() < MOVE_PER_CHARGE*3f && p.two == data.points.size()-1) {
                        data.tPoint3 = p.one;
                    }
                }
                //engine.addFloatingText(ship.getLocation(), ""+data.points.size(), 24f, Color.RED, ship, 1f,1f);
            }
        }

        Global.getCombatEngine().getCustomData().put("PULLBACK_DATA_KEY" + ship.getId(), data);
    }

    public static class ShipSpecificData {
        public List<Pair<Vector2f, Integer>> points = new ArrayList<>();
        public boolean activated = false;
        //main tele-point
        public Vector2f tPoint1;
        //for AI
        public Vector2f tPoint2;
        public Vector2f tPoint3;
        public int maxAmmo;
        public int ammo;
        public boolean isOn;
        public Vector2f location;
        public boolean player;
        public IntervalUtil countInterval = new IntervalUtil(0.1f,0.1f);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }
}