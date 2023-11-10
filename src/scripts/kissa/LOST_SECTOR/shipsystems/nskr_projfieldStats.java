package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Pair;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.ListIterator;

public class nskr_projfieldStats extends BaseShipSystemScript {

    private static final float ACTIVE_RANGE = 650f;
    public static final float MIN_EFFECT_DMG = 50f;
    public static final float MAX_EFFECT_DMG = 1000f;
    public static final float MAX_SLOWDOWN = 0.50f;
    //per-frame
    public static final float MAX_TURNRATE = 5f;
    public static final float FIGHTER_SLOWDOWN = 0.50f;

    // sprite path - necessary if loaded here and not in settings.json
    public static final String SPRITE_PATH = "graphics/fx/shields256.png";
    //base
    public static final Color COLOR = new Color(68, 80, 207, 5);

    //In-script variables
    private final ArrayList<Pair<CombatEntityAPI, Vector2f>> affectedEntities = new ArrayList<>();
    private SpriteAPI sprite = null;
    private SpriteAPI sprite2 = null;
    private float angle = 0f;
    private float timer = 0f;
    public static final Vector2f ZERO = new Vector2f();

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        float range = getMaxRange(ship);

        CombatEngineAPI engine = Global.getCombatEngine();
        sprite = Global.getSettings().getSprite(SPRITE_PATH);
        sprite2 = Global.getSettings().getSprite(SPRITE_PATH);

        if (effectLevel <= 0.001f) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        float amount = engine.getElapsedInLastFrame();

        //jitter
        Color color1 = util.randomiseColor(COLOR, 10, 5, 15, 5, true);
        Color color2 = color1;

        Vector2f size = new Vector2f((range*2f)+50f, (range*2f)+50f);
        Vector2f size2 = size;

        //speen
        angle += amount;
        if (angle > 360) angle = 0f;
        //grow
        timer += amount;
        if (timer > 0.5f) timer = 0f;

        float alpha = color1.getAlpha()*3f*effectLevel;
        color1 = util.setAlpha(color1, (int)(color1.getAlpha()*effectLevel));
        float vSize = size2.getX();
        float nTimer = 0f;
        nTimer = mathUtil.normalize(timer,0f,0.5f);

        //engine.addFloatingText(ship.getLocation(), "timer " + nTimer, 48f, Color.cyan, ship, 0.5f, 1.0f);
        vSize = mathUtil.lerp(vSize*0.10f, vSize*1.50f, nTimer);
        size2 = new Vector2f(vSize, vSize);
        alpha = util.clamp255((int)mathUtil.lerp(alpha*1.0f, alpha*0.3f, nTimer));
        color2 = util.setAlpha(color1,(int)alpha);

        //base
        MagicRender.singleframe(sprite, ship.getLocation(), size, angle, color1, false);
        //second one that changes size
        MagicRender.singleframe(sprite2, ship.getLocation(), size2, angle, color2, false);

        for (CombatEntityAPI ent : CombatUtils.getEntitiesWithinRange(ship.getLocation(), range)){
            if (ent.getCollisionClass()==CollisionClass.NONE) continue;
            if (ent.getOwner()==ship.getOwner()) continue;

            //missile
            if (ent instanceof MissileAPI) {
                MissileAPI missile = (MissileAPI) ent;
                if (missile.getDamageAmount() < MIN_EFFECT_DMG) continue;
                Vector2f ogVelocity = missile.getVelocity();
                boolean isSame = false;
                for (Pair<CombatEntityAPI, Vector2f> c : affectedEntities) {
                    if (c.one == ent) {
                        ogVelocity = c.two;
                        isSame = true;
                        //engine.addFloatingText(ent.getLocation(), "SAME", 24, Color.RED, null, 0.5f, 1.0f);
                        break;
                    }
                }
                float damage = Math.min(missile.getDamageAmount()+ missile.getEmpAmount() * 0.25f, MAX_EFFECT_DMG);
                damage = mathUtil.normalize(damage, MIN_EFFECT_DMG, MAX_EFFECT_DMG);
                float mult = 0f;
                mult = mathUtil.lerp(1f, MAX_SLOWDOWN, damage);

                if (!isSame) {
                    affectedEntities.add(new Pair<>(ent, ogVelocity));
                    //engine.addFloatingText(ent.getLocation(), "ADDED", 24, Color.RED, null, 0.5f, 1.0f);
                }
                missile.getVelocity().set(mathUtil.scaleVector(ogVelocity, mult));
                //fx
                //engine.addFloatingText(ent.getLocation(), "SLOWED " + ogVelocity.length() + " VEL "+missile.getVelocity().length(), 24, Color.RED, null, 0.5f, 1.0f);
                continue;
            }
            //proj
            if (ent instanceof DamagingProjectileAPI) {
                DamagingProjectileAPI proj = (DamagingProjectileAPI) ent;
                if (proj.getDamageAmount() < MIN_EFFECT_DMG) continue;
                float damage = Math.min(proj.getDamageAmount()+ proj.getEmpAmount() * 0.25f, MAX_EFFECT_DMG);
                damage = mathUtil.normalize(damage, MIN_EFFECT_DMG, MAX_EFFECT_DMG);
                float mult = 0f;
                mult = mathUtil.lerp(MAX_TURNRATE/3f, MAX_TURNRATE, damage);
                //direction check
                float faceTo = MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(),ship.getLocation()));
                //inverse
                if (faceTo>0f) mult *= -1f;

                float angle = Math.abs(MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(),ship.getLocation())));
                if (angle<=90f) {
                    proj.setFacing(proj.getFacing() + mult);
                    //proj.setAngularVelocity(mult);
                }
                //engine.addFloatingText(ent.getLocation(), "ANGLE " + angle, 24, Color.RED, null, 0.5f, 1.0f);

                //fx
                //engine.addFloatingText(ent.getLocation(), "TURNED " + mult, 24, Color.RED, null, 0.5f, 1.0f);
                continue;
            }
            //fighter
            if (ent instanceof ShipAPI){
                ShipAPI target = (ShipAPI) ent;
                if (target.getHullSize()!= ShipAPI.HullSize.FIGHTER) continue;
                Vector2f ogVelocity = target.getVelocity();
                boolean isSame = false;
                for (Pair<CombatEntityAPI, Vector2f> c : affectedEntities) {
                    if (c.one == ent) {
                        ogVelocity = c.two;
                        isSame = true;
                        //engine.addFloatingText(ent.getLocation(), "SAME", 24, Color.RED, null, 0.5f, 1.0f);
                        break;
                    }
                }
                if (!isSame) {
                    affectedEntities.add(new Pair<>(ent, ogVelocity));
                    //engine.addFloatingText(ent.getLocation(), "ADDED", 24, Color.RED, null, 0.5f, 1.0f);
                }
                target.getVelocity().set(mathUtil.scaleVector(ogVelocity, FIGHTER_SLOWDOWN));
                //fx
                //engine.addFloatingText(ent.getLocation(), "SLOWED " + ogVelocity.length() + " VEL "+ target.getVelocity().length(), 24, Color.RED, null, 0.5f, 1.0f);
            }
        }

        for (ListIterator<Pair<CombatEntityAPI, Vector2f>> iter = affectedEntities.listIterator(); iter.hasNext();) {
            Pair<CombatEntityAPI, Vector2f> a = iter.next();
            if(a.one==null || a.one.isExpired()){
                iter.remove();
                //engine.addFloatingText(a.one.getLocation(), "CLEARED", 32f, Color.RED, null, 0.5f, 1.0f);
                continue;
            }
            //out of reach, reset velocity
            if (MathUtils.getDistance(ship.getLocation(),a.one.getLocation())>range){
                a.one.getVelocity().set(a.two);
                iter.remove();
                //engine.addFloatingText(a.one.getLocation(), "CLEARED", 32f, Color.RED, null, 0.5f, 1.0f);
            }
        }
    }


    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            id = id + "_" + ship.getId();
        } else {
            return;
        }
        for (ListIterator<Pair<CombatEntityAPI, Vector2f>> iter = affectedEntities.listIterator(); iter.hasNext();) {
            Pair<CombatEntityAPI, Vector2f> a = iter.next();
            if(a.one==null || a.one.isExpired()){
                iter.remove();
                continue;
            }
            //reset
            a.one.getVelocity().set(a.two);
            iter.remove();
        }
        //empty
        affectedEntities.clear();

    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("deterrence field active", false);
        }
        return null;
    }

    public static float getMaxRange(ShipAPI ship){
        if (ship==null) return 0f;
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(ACTIVE_RANGE);
    }
}
