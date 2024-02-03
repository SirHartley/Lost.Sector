//////////////////////
//Parts of this script initially created by DarkRevenant
//////////////////////
package scripts.kissa.LOST_SECTOR.shipsystems;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.util;

public class nskr_timewarpStats extends BaseShipSystemScript {

    //Time stuff
    public static final float MAX_TIME_MULT = 20f;
    public static final Color PROT_JITTER_COLOR = new Color(59, 129, 179, 255);
    public static final Color PROT_JITTER_UNDER_COLOR = new Color(59, 121, 179, 255);
    public static final Color PROT_AFTERIMAGE_COLOR = new Color(59, 141, 179, 50);
    public static final Color ENIGMA_JITTER_COLOR = new Color(179, 59, 81, 255);
    public static final Color ENIGMA_JITTER_UNDER_COLOR = new Color(179, 59, 87, 255);
    public static final Color ENIGMA_AFTERIMAGE_COLOR = new Color(179, 59, 81, 50);

    private final IntervalUtil interval = new IntervalUtil(0.01f, 0.01f);

    public Set<ShipAPI> getFighters(ShipAPI carrier) {
        Set<ShipAPI> result = new HashSet<>(20);

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter()) {
                continue;
            }
            if (ship.getWing() == null) {
                continue;
            }
            if (ship.getWing().getSourceShip() == carrier) {
                result.add(ship);
            }
        }
        return result;
    }

    public float getMaxTimeMult(MutableShipStatsAPI stats) {
        return 1f + (MAX_TIME_MULT - 1f);
    }

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        boolean player = false;
        String statId;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();
            statId = "timewarp_fighter" + "_" + ship.getId();
        } else {
            return;
        }

        if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
            return;
        }

        Color jColor,jUColor,aIColor;
        if (util.protOrEnigma(ship).equals("enigma")){
            jColor=ENIGMA_JITTER_COLOR;
            jUColor=ENIGMA_JITTER_UNDER_COLOR;
            aIColor=ENIGMA_AFTERIMAGE_COLOR;
        } else {
            jColor=PROT_JITTER_COLOR;
            jUColor=PROT_JITTER_UNDER_COLOR;
            aIColor=PROT_AFTERIMAGE_COLOR;
        }

        ship.setApplyExtraAlphaToEngines(true);

        float shipTimeMult = (1f + (getMaxTimeMult(stats) - 1f))*effectLevel;
        stats.getTimeMult().modifyMult(id, shipTimeMult);
        if (player) {
            Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
        } else {
            Global.getCombatEngine().getTimeMult().unmodify(id);
        }

        stats.getMaxSpeed().modifyFlat(id, 100f);
        stats.getAcceleration().modifyFlat(id, 200f);

        CombatEngineAPI engine = Global.getCombatEngine();

        //extra fx
        interval.advance(engine.getElapsedInLastFrame());

        ship.setJitter("nskr_timewarpstats", jColor, 2, 2, 4f);
        ship.setJitterUnder("nskr_timewarpstats", jUColor, 2, 10, 4f);
        ship.setJitterShields(false);
        if (interval.intervalElapsed()) {
            createWarpFx(ship, aIColor);
        }

        //make fighters go zoom too
            Set<ShipAPI> carriedFighters = getFighters(ship);
            for (ShipAPI fighter : carriedFighters) {
                float atten = 1f;

                if (!fighter.isAlive()) {
                    fighter.getMutableStats().getTimeMult().unmodify(statId);
                    continue;
                }

                fighter.setJitter("nskr_timewarpstats", jColor, 2, 2, 4f);
                fighter.setJitterUnder("nskr_timewarpstats", jUColor, 2, 10, 4f);
                fighter.setJitterShields(false);

                if (interval.intervalElapsed()) {
                    createWarpFx(fighter, aIColor);
                }

                float effectLevelSquared = effectLevel * effectLevel;
                float fighterEffectLevelSquared = effectLevelSquared * atten;

                float fighterTimeMult = 1f + (MAX_TIME_MULT - 1f) * fighterEffectLevelSquared;
                fighter.getMutableStats().getTimeMult().modifyMult(statId, fighterTimeMult);

                //time for HAX
                fighter.getMutableStats().getBallisticRoFMult().modifyMult(statId, 1f + (-1f * fighterEffectLevelSquared));
                fighter.getMutableStats().getEnergyRoFMult().modifyMult(statId, 1f + (-1f * fighterEffectLevelSquared));
                fighter.getMutableStats().getMissileRoFMult().modifyMult(statId, 1f + (-1f * fighterEffectLevelSquared));
            }


            /* Unapply for fighters that are not from the carrier - just in case */
            for (ShipAPI fighter : Global.getCombatEngine().getShips()) {
                if (!fighter.isFighter()) {
                    continue;
                }
                if (carriedFighters.contains(fighter)) {
                    continue;
                }
                fighter.getMutableStats().getTimeMult().unmodify(statId);
                fighter.getMutableStats().getBallisticRoFMult().unmodify(statId);
                fighter.getMutableStats().getEnergyRoFMult().unmodify(statId);
                fighter.getMutableStats().getMissileRoFMult().unmodify(statId);
            }

            //UNAPPLY PLS
            if (effectLevel < 0.2f) {
                stats.getMaxSpeed().unmodify(id);
                stats.getAcceleration().unmodify(id);
                //remove tidi again just to be safe
                stats.getTimeMult().unmodify(id);
                Global.getCombatEngine().getTimeMult().unmodify(id);

                Global.getCombatEngine().getTimeMult().unmodify(statId);
                stats.getTimeMult().unmodify(statId);

                for (ShipAPI fighter : Global.getCombatEngine().getShips()) {
                    if (!fighter.isFighter()) {
                        continue;
                    }
                    fighter.getMutableStats().getBallisticRoFMult().unmodify(statId);
                    fighter.getMutableStats().getEnergyRoFMult().unmodify(statId);
                    fighter.getMutableStats().getMissileRoFMult().unmodify(statId);
                    fighter.getMutableStats().getTimeMult().unmodify(statId);

                }
            }
    }

    private void createWarpFx(ShipAPI ship,  Color aIColor) {

        Vector2f loc = new Vector2f(ship.getLocation());
        loc.x -= 8f * FastTrig.cos(ship.getFacing() * Math.PI / 180f);
        loc.y -= 8f * FastTrig.sin(ship.getFacing() * Math.PI / 180f);

        SpriteAPI sprite = ship.getSpriteAPI();
        float offsetX = sprite.getWidth() / 2 - sprite.getCenterX();
        float offsetY = sprite.getHeight() / 2 - sprite.getCenterY();

        float trueOffsetX = (float) FastTrig.cos(Math.toRadians(ship.getFacing() - 90f)) * offsetX - (float) FastTrig.sin(Math.toRadians(ship.getFacing() - 90f)) * offsetY;
        float trueOffsetY = (float) FastTrig.sin(Math.toRadians(ship.getFacing() - 90f)) * offsetX + (float) FastTrig.cos(Math.toRadians(ship.getFacing() - 90f)) * offsetY;

        Vector2f trueLocation = new Vector2f(ship.getLocation().getX() + trueOffsetX, ship.getLocation().getY() + trueOffsetY);

        MagicRender.battlespace(
                Global.getSettings().getSprite(ship.getHullSpec().getSpriteName()),
                MathUtils.getRandomPointInCircle(trueLocation, MathUtils.getRandomNumberInRange(0f, 20f)),
                new Vector2f(0, 0),
                new Vector2f(ship.getSpriteAPI().getWidth(), ship.getSpriteAPI().getHeight()),
                new Vector2f(0, 0),
                ship.getFacing() - 90f,
                0f,
                aIColor,
                true,
                0f,
                0f,
                0f,
                0f,
                0f,
                0.1f,
                0.1f,
                0.1f,
                CombatEngineLayers.BELOW_SHIPS_LAYER);

    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship;
        String statId;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            id = id + "_" + ship.getId();
            statId = "timewarp_fighter" + "_" + ship.getId();
        } else {
            return;
        }

        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        //remove tidi again just to be safe
        stats.getTimeMult().unmodify(id);
        Global.getCombatEngine().getTimeMult().unmodify(id);

        Global.getCombatEngine().getTimeMult().unmodify(statId);
        stats.getTimeMult().unmodify(statId);

        for (ShipAPI fighter : Global.getCombatEngine().getShips()) {
            if (!fighter.isFighter()) {
                continue;
            }
            fighter.getMutableStats().getBallisticRoFMult().unmodify(statId);
            fighter.getMutableStats().getEnergyRoFMult().unmodify(statId);
            fighter.getMutableStats().getMissileRoFMult().unmodify(statId);
            fighter.getMutableStats().getTimeMult().unmodify(statId);
        }
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }
}























