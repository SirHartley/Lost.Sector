package lostsector.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import lostsector.util.CombatUtilsLS;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MassTargeting {

    public static class MassTargetingListener implements AdvanceableListener {

        public static final String MOD_ICON = "graphics/icons/hullsys/targeting_feed.png";
        public static final String MOD_BUFFID = "nskr_massTargeting";
        public static final String MOD_NAME = "Mass Targeting Array";

        public ShipAPI ship;
        public MassTargetingListener(ShipAPI ship) {
            this.ship = ship;
        }

        public void advance(float amount) {
            //remove when not active
            if (!ship.isAlive() || ship.getSystem().getState() != ShipSystemAPI.SystemState.ACTIVE){
                ship.removeListener(this);
                return;
            }

            if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
                return;
            }

            float range = MassTargetingStats.getMaxRange(ship);

            //bonus
            java.util.List<ShipAPI> ships = new ArrayList<>(100);
            List<ShipAPI> shipsTargeting = new ArrayList<>(100);
            ships.addAll(CombatUtilsLS.getShipsWithinRange(ship.getLocation(), range));
            for (ShipAPI possibleShip : ships) {
                if (possibleShip.getOwner() != ship.getOwner())
                    continue;
                shipsTargeting.add(possibleShip);
            }

            int buffCount = 0;
            for (ShipAPI buffTarget : shipsTargeting){
                ShipSpecificData buffData = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("MASSTARGETING_BUFF_DATA_KEY" + buffTarget.getId());
                if (buffData == null){
                    buffData = new ShipSpecificData();
                }

                buffData.buffed = true;
                buffCount++;

                if (!buffTarget.hasListenerOfClass(MassTargetingBuffListener.class)) buffTarget.addListener(new MassTargetingBuffListener(buffTarget, ship));

                Global.getCombatEngine().getCustomData().put("MASSTARGETING_BUFF_DATA_KEY" + buffTarget.getId(), buffData);
            }
            //tooltip
            if (ship == Global.getCombatEngine().getPlayerShip()){
                String target = "targets";
                if (buffCount==1)target = "target";
                Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, buffCount + " valid "+target+" in range", false);
            }
            if (buffCount <= 0) {
            }

        }

        //RECURSIVE SUBCLASSES
        public static class ShipSpecificData {
            public boolean buffed = false;
        }
    }

    //RECURSIVE LISTENERS
    public static class MassTargetingBuffListener implements AdvanceableListener {

        public static final Color JITTER_COLOR = new Color(50, 168, 78, 25);

        public static final String MOD_ICON = "graphics/icons/hullsys/entropy_amplifier.png";
        public static final String MOD_BUFFID = "nskr_massTargetingStats";
        public static final String MOD_NAME = "Mass Targeting Array";

        public ShipAPI ship;
        public ShipAPI source;

        public MassTargetingBuffListener(ShipAPI ship, ShipAPI source) {
            this.ship = ship;
            this.source = source;
        }

        public void advance(float amount) {
            if (!ship.isAlive()) ship.removeListener(this);

            if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
                return;
            }

            MassTargetingListener.ShipSpecificData buffData = (MassTargetingListener.ShipSpecificData) Global.getCombatEngine().getCustomData().get("MASSTARGETING_BUFF_DATA_KEY" + ship.getId());
            if (buffData == null){
                return;
            }
            if (buffData.buffed){

                ship.getMutableStats().getDamageToMissiles().modifyPercent(MassTargetingStats.BONUS_ID, (MassTargetingStats.TARGETING_BONUS - 1.5f) * 100f);
                ship.getMutableStats().getDamageToFighters().modifyPercent(MassTargetingStats.BONUS_ID, (MassTargetingStats.TARGETING_BONUS - 1.5f) * 100f);
                ship.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent(MassTargetingStats.BONUS_ID, ((MassTargetingStats.TARGETING_BONUS - 1f) / 5f) * 100f);
                ship.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent(MassTargetingStats.BONUS_ID, ((MassTargetingStats.TARGETING_BONUS - 1f) / 5) * 100f);

                ship.setJitter(ship, JITTER_COLOR, 1, 5, 0f, 2f);
                ship.setJitterShields(false);

                if (ship == Global.getCombatEngine().getPlayerShip()) {
                    Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, "targeting improved", false);
                }

                //Global.getCombatEngine().addFloatingText(ship.getLocation(),"BUFFED", 48f, Color.cyan, ship, 0.5f, 1.0f);

                //reset
                buffData.buffed = false;
            } else {
                ship.getMutableStats().getDamageToMissiles().unmodify(MassTargetingStats.BONUS_ID);
                ship.getMutableStats().getDamageToFighters().unmodify(MassTargetingStats.BONUS_ID);
                ship.getMutableStats().getBeamWeaponRangeBonus().unmodify(MassTargetingStats.BONUS_ID);
                ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify(MassTargetingStats.BONUS_ID);
                ship.getMutableStats().getBallisticWeaponRangeBonus().unmodify(MassTargetingStats.BONUS_ID);
            }

            Global.getCombatEngine().getCustomData().put("MASSTARGETING_BUFF_DATA_KEY" + ship.getId(), buffData);
        }
    }
}
