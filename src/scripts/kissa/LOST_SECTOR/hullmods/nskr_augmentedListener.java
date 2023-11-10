package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;
import scripts.kissa.LOST_SECTOR.util.combatUtil;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class nskr_augmentedListener {
    //
    //advanceInCombat bonuses for augmented ships
    //
    public static final String OP_CENTER_BUFF_KEY = "OP_CENTER_BUFF_AUGMENT";
    public static class nskr_fluxCoilAdjunctListener implements AdvanceableListener {
        public static final String MOD_ICON = "graphics/icons/hullsys/emp_emitter.png";
        public static final String MOD_BUFFID = nskr_augmented.FLUX_COIL_ADJUNCT_ID+"_augment";
        public static final String MOD_NAME = "Flux Coil Adjunct Augment";

        public ShipAPI ship;
        public nskr_fluxCoilAdjunctListener(ShipAPI ship) {
            this.ship = ship;
        }
        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
                return;
            }
            if (!ship.getVariant().hasHullMod(nskr_augmented.FLUX_COIL_ADJUNCT_ID)){
                ship.removeListener(this);
                return;
            }

            //bonus
            float adjunctBonus = ship.getFluxTracker().getCurrFlux() * (nskr_augmented.FLUX_COIL_ADJUNCT_BONUS*0.01f);
            ship.getMutableStats().getFluxDissipation().modifyFlat(nskr_augmented.FLUX_COIL_ADJUNCT_ID+"_augment", adjunctBonus);
            //tooltip
            if (ship==Global.getCombatEngine().getPlayerShip() && adjunctBonus>0f) {
                Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, "+" + (int)adjunctBonus + " flux dissipation", false);
            } else {
            }
        }
    }

    public static class nskr_blastDoorsListener implements AdvanceableListener {
        public static final String MOD_ICON = "graphics/icons/hullsys/damper_field.png";
        public static final String MOD_BUFFID = nskr_augmented.BLAST_DOORS_ID+"_augment";
        public static final String MOD_NAME = "Blast Doors Augment";

        public ShipAPI ship;
        public nskr_blastDoorsListener(ShipAPI ship) {
            this.ship = ship;
        }
        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
                return;
            }
            if (!ship.getVariant().hasHullMod(nskr_augmented.BLAST_DOORS_ID)){
                ship.removeListener(this);
                return;
            }

            //bonus
            if (ship.getHullLevel() < (nskr_augmented.BLAST_DOORS_THRESHOLD*0.01f)) {
                float blastDmg = -nskr_augmented.BLAST_DOORS_BONUS;
                ship.getMutableStats().getHullDamageTakenMult().modifyPercent(nskr_augmented.BLAST_DOORS_ID+"_augment", blastDmg);
            //tooltip
            if (ship==Global.getCombatEngine().getPlayerShip()) {
                Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, (int)blastDmg + "% hull dmg taken ", false);
            }
            } else{
                ship.getMutableStats().getHullDamageTakenMult().unmodify(nskr_augmented.BLAST_DOORS_ID+"_augment");
            }
        }
    }

    public static class nskr_safetyOverrideListener implements AdvanceableListener {
        public static final String MOD_ICON = "graphics/icons/hullsys/quantum_disruptor.png";
        public static final String MOD_BUFFID = nskr_augmented.SAFETY_OVERRIDE_ID+"_augment";
        public static final String MOD_NAME = "Safety Overrides Augment";

        public ShipAPI ship;

        public nskr_safetyOverrideListener(ShipAPI ship) {
            this.ship = ship;
        }

        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
                return;
            }
            if (!ship.getVariant().hasHullMod(nskr_augmented.SAFETY_OVERRIDE_ID)){
                ship.removeListener(this);
                return;
            }

            //bonus
            if (ship.getFluxLevel() > (nskr_augmented.SAFETY_OVERRIDE_THRESHOLD * 0.01f)) {
                float soDmg = nskr_augmented.SAFETY_OVERRIDE_BONUS;
                ship.getMutableStats().getEnergyWeaponDamageMult().modifyPercent(nskr_augmented.SAFETY_OVERRIDE_ID + "_augment", soDmg);
                ship.getMutableStats().getBallisticWeaponDamageMult().modifyPercent(nskr_augmented.SAFETY_OVERRIDE_ID + "_augment", soDmg);
                ship.getMutableStats().getMissileWeaponDamageMult().modifyPercent(nskr_augmented.SAFETY_OVERRIDE_ID + "_augment", soDmg);
                //tooltip
                if (ship == Global.getCombatEngine().getPlayerShip()) {
                    Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, "+" + (int)soDmg + "% weapon dmg", false);
                }
            } else{
                ship.getMutableStats().getEnergyWeaponDamageMult().unmodify(nskr_augmented.SAFETY_OVERRIDE_ID+"_augment");
                ship.getMutableStats().getBallisticWeaponDamageMult().unmodify(nskr_augmented.SAFETY_OVERRIDE_ID+"_augment");
                ship.getMutableStats().getMissileWeaponDamageMult().unmodify(nskr_augmented.SAFETY_OVERRIDE_ID+"_augment");
            }
        }
    }

    public static class nskr_unstableInjectorListener implements AdvanceableListener {
        public static final String MOD_ICON = "graphics/icons/hullsys/burn_drive.png";
        public static final String MOD_BUFFID = nskr_augmented.UNSTABLE_INJECTOR_ID+"_augment";
        public static final String MOD_NAME = "Unstable Injector Augment";

        public ShipAPI ship;

        public nskr_unstableInjectorListener(ShipAPI ship) {
            this.ship = ship;
        }

        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
                return;
            }
            if (!ship.getVariant().hasHullMod(nskr_augmented.UNSTABLE_INJECTOR_ID)){
                ship.removeListener(this);
                return;
            }

            //bonus
            Vector2f sVel = ship.getVelocity();
            if (sVel == null) sVel = Misc.ZERO;
            float sAngle = ship.getFacing();
            float vAngle = VectorUtils.getFacing(sVel);
            float dist = Objects.requireNonNull(sVel).length();
            if (dist == 0f) vAngle = sAngle;
            //angle fuckery
            float diff = vAngle - sAngle;
            if (diff < 0) diff *= -1f;
            //engine.addFloatingText(sLoc, "test " + (int)vAngle +","+ (int)sAngle +","+ (int)diff, 30f, Color.cyan, ship, 0.5f, 1.0f);
            if ((sVel.length() > 0f) && (diff <= 20) || sVel.length() > 0f && (diff >= 340)) {
                float speed = nskr_augmented.UNSTABLE_INJECTOR_BONUS;
                ship.getMutableStats().getMaxSpeed().modifyFlat(nskr_augmented.UNSTABLE_INJECTOR_ID+"_augment", speed);
                //tooltip
                if (ship == Global.getCombatEngine().getPlayerShip()){
                    Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, "+"+(int) speed + " top speed", false);
                }
            } else {
                ship.getMutableStats().getMaxSpeed().unmodify(nskr_augmented.UNSTABLE_INJECTOR_ID+"_augment");
            }
        }
    }

    public static class nskr_navRelayListener implements AdvanceableListener {
        public static final String MOD_ICON = "graphics/icons/hullsys/maneuvering_jets.png";
        public static final String MOD_BUFFID = nskr_augmented.NAV_RELAY_ID+"_augment";
        public static final String MOD_NAME = "Nav Relay Augment";
        public static final Color RENDER_COLOR = new Color(68, 133, 207, 15);
        public static final String SPRITE_PATH = "graphics/fx/nskr_circle.png";
        private SpriteAPI sprite = null;
        private boolean loaded = false;

        public ShipAPI ship;

        public nskr_navRelayListener(ShipAPI ship) {
            this.ship = ship;
        }

        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
                return;
            }
            if (!ship.getVariant().hasHullMod(nskr_augmented.NAV_RELAY_ID)){
                ship.removeListener(this);
                return;
            }
            //no CR
            if (ship.getCurrentCR()<=0f){
                ship.removeListener(this);
                return;
            }

            //bonus
            List<ShipAPI> ships = new ArrayList<>(100);
            List<ShipAPI> shipsNav = new ArrayList<>(100);
            ships.addAll(combatUtil.getShipsWithinRange(ship.getLocation(), nskr_augmented.NAV_RELAY_RANGE));
            for (ShipAPI possibleShip : ships) {
                if (possibleShip.getOwner() != ship.getOwner() || possibleShip.getHullSize() == ShipAPI.HullSize.FIGHTER || !possibleShip.getVariant().hasHullMod(nskr_augmented.NAV_RELAY_ID) || possibleShip == ship)
                    continue;
                if (possibleShip.getCurrentCR()<=0f) continue;
                shipsNav.add(possibleShip);
            }
            int navCount = shipsNav.size();
            //cap at max
            if (navCount > nskr_augmented.NAV_RELAY_MAX) navCount = nskr_augmented.NAV_RELAY_MAX;
            //engine.addFloatingText(ship.getLocation(), "test " + (int)navCount, 30f, Color.cyan, ship, 0.5f, 1.0f);
            ship.getMutableStats().getMaxSpeed().modifyFlat(nskr_augmented.NAV_RELAY_ID+"_augment", navCount * nskr_augmented.NAV_RELAY_BONUS);
            //tooltip
            if (ship == Global.getCombatEngine().getPlayerShip() && navCount > 0){
                Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, "+"+(int)(navCount * nskr_augmented.NAV_RELAY_BONUS) + " top speed", false);
            }
            if (navCount <= 0) {
                ship.getMutableStats().getMaxSpeed().unmodify(nskr_augmented.NAV_RELAY_ID+"_augment");
            }
            //render
            if (ship==Global.getCombatEngine().getPlayerShip() && !ship.getVariant().hasHullMod(nskr_augmented.OPERATIONS_CENTER_ID)) {
                sprite = getSprite();
                //visual part of the sprite is slightly smaller
                Vector2f size = new Vector2f((nskr_augmented.NAV_RELAY_RANGE*2.1f)+150f, (nskr_augmented.NAV_RELAY_RANGE*2.1f)+150f);
                if (Global.getCombatEngine().isUIShowingHUD()) {
                    MagicRender.singleframe(sprite, ship.getLocation(), size, ship.getFacing(), RENDER_COLOR, false);
                }
            }
        }

        public SpriteAPI getSprite(){
            if (sprite == null) {
                // Load sprite if it hasn't been loaded yet - not needed if you add it to settings.json
                if (!loaded) {
                    try {
                        Global.getSettings().loadTexture(SPRITE_PATH);
                    } catch (IOException ex) {
                        throw new RuntimeException("Failed to load sprite '" + SPRITE_PATH + "'!", ex);
                    }

                    loaded = true;
                }
                sprite = Global.getSettings().getSprite(SPRITE_PATH);
            }
            return sprite;
        }
    }
    public static class nskr_opCenterListener implements AdvanceableListener {
        public static final String MOD_ICON = "graphics/icons/hullsys/targeting_feed.png";
        public static final String MOD_BUFFID = nskr_augmented.OPERATIONS_CENTER_ID+"_augment";
        public static final String MOD_NAME = "Operations Center Augment";
        public static final Color RENDER_COLOR = new Color(68, 207, 100, 15);
        public static final String SPRITE_PATH = "graphics/fx/nskr_circle.png";
        private SpriteAPI sprite = null;
        private boolean loaded = false;

        public ShipAPI ship;

        public nskr_opCenterListener(ShipAPI ship) {
            this.ship = ship;
        }

        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
                return;
            }
            if (!ship.getVariant().hasHullMod(nskr_augmented.OPERATIONS_CENTER_ID)){
                ship.removeListener(this);
                return;
            }
            //no CR
            if (ship.getCurrentCR()<=0f){
                ship.removeListener(this);
                return;
            }

            //bonus
            List<ShipAPI> ships = new ArrayList<>(100);
            List<ShipAPI> shipsOpCenter = new ArrayList<>(100);
            ships.addAll(combatUtil.getShipsWithinRange(ship.getLocation(), nskr_augmented.OPERATIONS_CENTER_RANGE));
            for (ShipAPI possibleShip : ships) {
                if (possibleShip.getOwner() != ship.getOwner() || possibleShip.getHullSize() == ShipAPI.HullSize.FIGHTER)
                    continue;
                shipsOpCenter.add(possibleShip);
            }

            int buffCount = 0;
            for (ShipAPI buffTarget : shipsOpCenter){
                ShipSpecificData buffData = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("OPCENTER_BUFF_DATA_KEY" + buffTarget.getId());
                if (buffData == null){
                    buffData = new ShipSpecificData();
                }
                if (buffData.buffRemaining<=0f || buffTarget.getPeakTimeRemaining()<=0f){
                    continue;
                }
                buffData.buffed = true;
                buffCount++;

                if (!buffTarget.hasListenerOfClass(nskr_opCenterBuffListener.class)) buffTarget.addListener(new nskr_opCenterBuffListener(buffTarget, ship));

                Global.getCombatEngine().getCustomData().put("OPCENTER_BUFF_DATA_KEY" + buffTarget.getId(), buffData);
            }
            //tooltip
            if (ship == Global.getCombatEngine().getPlayerShip()){
                String target = "targets";
                if (buffCount==1)target = "target";
                Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, buffCount + " valid "+target+" in range", false);
            }
            if (buffCount <= 0) {
            }

            //render
            if (ship.getOwner()==Global.getCombatEngine().getPlayerShip().getOwner()) {
                sprite = getSprite();
                //visual part of the sprite is slightly smaller
                Vector2f size = new Vector2f((nskr_augmented.OPERATIONS_CENTER_RANGE*2.1f)+150f, (nskr_augmented.OPERATIONS_CENTER_RANGE*2.1f)+150f);
                if (Global.getCombatEngine().isUIShowingHUD()) {
                    MagicRender.singleframe(sprite, ship.getLocation(), size, ship.getFacing(), RENDER_COLOR, false);
                }
            }
        }

        public SpriteAPI getSprite(){
            if (sprite == null) {
                // Load sprite if it hasn't been loaded yet - not needed if you add it to settings.json
                if (!loaded) {
                    try {
                        Global.getSettings().loadTexture(SPRITE_PATH);
                    } catch (IOException ex) {
                        throw new RuntimeException("Failed to load sprite '" + SPRITE_PATH + "'!", ex);
                    }

                    loaded = true;
                }
                sprite = Global.getSettings().getSprite(SPRITE_PATH);
            }
            return sprite;
        }

        //RECURSIVE SUBCLASSES
        public static class ShipSpecificData {
            public float buffRemaining = nskr_augmented.OPERATIONS_CENTER_BONUS;
            public float modifiedBy = 0f;
            public boolean buffed = false;
        }
    }
    //RECURSIVE LISTENERS
    public static class nskr_opCenterBuffListener implements AdvanceableListener {
        public static final String MOD_ICON = "graphics/icons/hullsys/flare_launcher.png";
        public static final String MOD_BUFFID = OP_CENTER_BUFF_KEY;
        public static final String MOD_NAME = "Operations Center Augment";

        public ShipAPI ship;
        public ShipAPI source;

        public nskr_opCenterBuffListener(ShipAPI ship, ShipAPI source) {
            this.ship = ship;
            this.source = source;
        }

        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
                return;
            }
            if (!ship.isAlive()){
                ship.removeListener(this);
                return;
            }

            nskr_opCenterListener.ShipSpecificData buffData = (nskr_opCenterListener.ShipSpecificData) Global.getCombatEngine().getCustomData().get("OPCENTER_BUFF_DATA_KEY" + ship.getId());
            if (buffData == null){
                return;
            }
            if (buffData.buffed){

                buffData.modifiedBy += amount;
                ship.getMutableStats().getPeakCRDuration().modifyFlat(nskr_augmented.OPERATIONS_CENTER_ID+"_augment", buffData.modifiedBy);
                buffData.buffRemaining -= amount;

                //Global.getCombatEngine().addFloatingText(ship.getLocation(),""+buffData.buffRemaining, 48f, Color.cyan, ship, 0.5f, 1.0f);

                //text
                if (ship==Global.getCombatEngine().getPlayerShip()){
                    Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, (int)buffData.buffRemaining+" seconds of extra PPT remaining", false);
                }

                //reset
                buffData.buffed = false;
            }
            Global.getCombatEngine().getCustomData().put("OPCENTER_BUFF_DATA_KEY" + ship.getId(), buffData);
        }
    }

}

