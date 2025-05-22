package lostsector.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.util.CombatUtilsLS;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AugmentedListener {
    //
    //advanceInCombat bonuses for augmented ships
    //
    public static final String OP_CENTER_BUFF_KEY = "OP_CENTER_BUFF_AUGMENT";
    public static class FluxCoilAdjunctListener implements AdvanceableListener {
        public static final String MOD_ICON = "graphics/icons/hullsys/emp_emitter.png";
        public static final String MOD_BUFFID = Augmented.FLUX_COIL_ADJUNCT_ID+"_augment";
        public static final String MOD_NAME = "Flux Coil Adjunct Augment";

        public ShipAPI ship;
        public FluxCoilAdjunctListener(ShipAPI ship) {
            this.ship = ship;
        }
        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
                return;
            }
            if (!ship.getVariant().hasHullMod(Augmented.FLUX_COIL_ADJUNCT_ID)){
                ship.removeListener(this);
                return;
            }

            //bonus
            float adjunctBonus = ship.getFluxTracker().getCurrFlux() * (Augmented.FLUX_COIL_ADJUNCT_BONUS*0.01f);
            ship.getMutableStats().getFluxDissipation().modifyFlat(Augmented.FLUX_COIL_ADJUNCT_ID+"_augment", adjunctBonus);
            //tooltip
            if (ship==Global.getCombatEngine().getPlayerShip() && adjunctBonus>0f) {
                Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, "+" + (int)adjunctBonus + " flux dissipation", false);
            } else {
            }
        }
    }

    public static class BlastDoorsListener implements AdvanceableListener {
        public static final String MOD_ICON = "graphics/icons/hullsys/damper_field.png";
        public static final String MOD_BUFFID = Augmented.BLAST_DOORS_ID+"_augment";
        public static final String MOD_NAME = "Blast Doors Augment";

        public ShipAPI ship;
        public BlastDoorsListener(ShipAPI ship) {
            this.ship = ship;
        }
        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
                return;
            }
            if (!ship.getVariant().hasHullMod(Augmented.BLAST_DOORS_ID)){
                ship.removeListener(this);
                return;
            }

            //bonus
            if (ship.getHullLevel() < (Augmented.BLAST_DOORS_THRESHOLD*0.01f)) {
                float blastDmg = -Augmented.BLAST_DOORS_BONUS;
                ship.getMutableStats().getHullDamageTakenMult().modifyPercent(Augmented.BLAST_DOORS_ID+"_augment", blastDmg);
            //tooltip
            if (ship==Global.getCombatEngine().getPlayerShip()) {
                Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, (int)blastDmg + "% hull dmg taken ", false);
            }
            } else{
                ship.getMutableStats().getHullDamageTakenMult().unmodify(Augmented.BLAST_DOORS_ID+"_augment");
            }
        }
    }

    public static class SafetyOverrideListener implements AdvanceableListener {
        public static final String MOD_ICON = "graphics/icons/hullsys/quantum_disruptor.png";
        public static final String MOD_BUFFID = Augmented.SAFETY_OVERRIDE_ID+"_augment";
        public static final String MOD_NAME = "Safety Overrides Augment";

        public ShipAPI ship;

        public SafetyOverrideListener(ShipAPI ship) {
            this.ship = ship;
        }

        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
                return;
            }
            if (!ship.getVariant().hasHullMod(Augmented.SAFETY_OVERRIDE_ID)){
                ship.removeListener(this);
                return;
            }

            //bonus
            if (ship.getFluxLevel() > (Augmented.SAFETY_OVERRIDE_THRESHOLD * 0.01f)) {
                float soDmg = Augmented.SAFETY_OVERRIDE_BONUS;
                ship.getMutableStats().getEnergyWeaponDamageMult().modifyPercent(Augmented.SAFETY_OVERRIDE_ID + "_augment", soDmg);
                ship.getMutableStats().getBallisticWeaponDamageMult().modifyPercent(Augmented.SAFETY_OVERRIDE_ID + "_augment", soDmg);
                ship.getMutableStats().getMissileWeaponDamageMult().modifyPercent(Augmented.SAFETY_OVERRIDE_ID + "_augment", soDmg);
                //tooltip
                if (ship == Global.getCombatEngine().getPlayerShip()) {
                    Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, "+" + (int)soDmg + "% weapon dmg", false);
                }
            } else{
                ship.getMutableStats().getEnergyWeaponDamageMult().unmodify(Augmented.SAFETY_OVERRIDE_ID+"_augment");
                ship.getMutableStats().getBallisticWeaponDamageMult().unmodify(Augmented.SAFETY_OVERRIDE_ID+"_augment");
                ship.getMutableStats().getMissileWeaponDamageMult().unmodify(Augmented.SAFETY_OVERRIDE_ID+"_augment");
            }
        }
    }

    public static class UnstableInjectorListener implements AdvanceableListener {
        public static final String MOD_ICON = "graphics/icons/hullsys/burn_drive.png";
        public static final String MOD_BUFFID = Augmented.UNSTABLE_INJECTOR_ID+"_augment";
        public static final String MOD_NAME = "Unstable Injector Augment";

        public ShipAPI ship;

        public UnstableInjectorListener(ShipAPI ship) {
            this.ship = ship;
        }

        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
                return;
            }
            if (!ship.getVariant().hasHullMod(Augmented.UNSTABLE_INJECTOR_ID)){
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
                float speed = Augmented.UNSTABLE_INJECTOR_BONUS;
                ship.getMutableStats().getMaxSpeed().modifyFlat(Augmented.UNSTABLE_INJECTOR_ID+"_augment", speed);
                //tooltip
                if (ship == Global.getCombatEngine().getPlayerShip()){
                    Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, "+"+(int) speed + " top speed", false);
                }
            } else {
                ship.getMutableStats().getMaxSpeed().unmodify(Augmented.UNSTABLE_INJECTOR_ID+"_augment");
            }
        }
    }

    public static class NavRelayListener implements AdvanceableListener {
        public static final String MOD_ICON = "graphics/icons/hullsys/maneuvering_jets.png";
        public static final String MOD_BUFFID = Augmented.NAV_RELAY_ID+"_augment";
        public static final String MOD_NAME = "Nav Relay Augment";
        public static final Color RENDER_COLOR = new Color(68, 133, 207, 15);
        public static final String SPRITE_PATH = "graphics/fx/nskr_circle.png";
        private SpriteAPI sprite = null;
        private boolean loaded = false;

        public ShipAPI ship;

        public NavRelayListener(ShipAPI ship) {
            this.ship = ship;
        }

        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
                return;
            }
            if (!ship.getVariant().hasHullMod(Augmented.NAV_RELAY_ID)){
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
            ships.addAll(CombatUtilsLS.getShipsWithinRange(ship.getLocation(), Augmented.NAV_RELAY_RANGE));
            for (ShipAPI possibleShip : ships) {
                if (possibleShip.getOwner() != ship.getOwner() || possibleShip.getHullSize() == ShipAPI.HullSize.FIGHTER || !possibleShip.getVariant().hasHullMod(Augmented.NAV_RELAY_ID) || possibleShip == ship)
                    continue;
                if (possibleShip.getCurrentCR()<=0f) continue;
                shipsNav.add(possibleShip);
            }
            int navCount = shipsNav.size();
            //cap at max
            if (navCount > Augmented.NAV_RELAY_MAX) navCount = Augmented.NAV_RELAY_MAX;
            //engine.addFloatingText(ship.getLocation(), "test " + (int)navCount, 30f, Color.cyan, ship, 0.5f, 1.0f);
            ship.getMutableStats().getMaxSpeed().modifyFlat(Augmented.NAV_RELAY_ID+"_augment", navCount * Augmented.NAV_RELAY_BONUS);
            //tooltip
            if (ship == Global.getCombatEngine().getPlayerShip() && navCount > 0){
                Global.getCombatEngine().maintainStatusForPlayerShip(MOD_BUFFID, MOD_ICON, MOD_NAME, "+"+(int)(navCount * Augmented.NAV_RELAY_BONUS) + " top speed", false);
            }
            if (navCount <= 0) {
                ship.getMutableStats().getMaxSpeed().unmodify(Augmented.NAV_RELAY_ID+"_augment");
            }
            //render
            if (ship==Global.getCombatEngine().getPlayerShip() && !ship.getVariant().hasHullMod(Augmented.OPERATIONS_CENTER_ID)) {
                sprite = getSprite();
                //visual part of the sprite is slightly smaller
                Vector2f size = new Vector2f((Augmented.NAV_RELAY_RANGE*2.1f)+150f, (Augmented.NAV_RELAY_RANGE*2.1f)+150f);
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
    public static class OpCenterListener implements AdvanceableListener {
        public static final String MOD_ICON = "graphics/icons/hullsys/targeting_feed.png";
        public static final String MOD_BUFFID = Augmented.OPERATIONS_CENTER_ID+"_augment";
        public static final String MOD_NAME = "Operations Center Augment";
        public static final Color RENDER_COLOR = new Color(68, 207, 100, 15);
        public static final String SPRITE_PATH = "graphics/fx/nskr_circle.png";
        private SpriteAPI sprite = null;
        private boolean loaded = false;

        public ShipAPI ship;

        public OpCenterListener(ShipAPI ship) {
            this.ship = ship;
        }

        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
                return;
            }
            if (!ship.getVariant().hasHullMod(Augmented.OPERATIONS_CENTER_ID)){
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
            ships.addAll(CombatUtilsLS.getShipsWithinRange(ship.getLocation(), Augmented.OPERATIONS_CENTER_RANGE));
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

                if (!buffTarget.hasListenerOfClass(OpCenterBuffListener.class)) buffTarget.addListener(new OpCenterBuffListener(buffTarget, ship));

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
                Vector2f size = new Vector2f((Augmented.OPERATIONS_CENTER_RANGE*2.1f)+150f, (Augmented.OPERATIONS_CENTER_RANGE*2.1f)+150f);
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
            public float buffRemaining = Augmented.OPERATIONS_CENTER_BONUS;
            public float modifiedBy = 0f;
            public boolean buffed = false;
        }
    }
    //RECURSIVE LISTENERS
    public static class OpCenterBuffListener implements AdvanceableListener {
        public static final String MOD_ICON = "graphics/icons/hullsys/flare_launcher.png";
        public static final String MOD_BUFFID = OP_CENTER_BUFF_KEY;
        public static final String MOD_NAME = "Operations Center Augment";

        public ShipAPI ship;
        public ShipAPI source;

        public OpCenterBuffListener(ShipAPI ship, ShipAPI source) {
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

            OpCenterListener.ShipSpecificData buffData = (OpCenterListener.ShipSpecificData) Global.getCombatEngine().getCustomData().get("OPCENTER_BUFF_DATA_KEY" + ship.getId());
            if (buffData == null){
                return;
            }
            if (buffData.buffed){

                buffData.modifiedBy += amount;
                ship.getMutableStats().getPeakCRDuration().modifyFlat(Augmented.OPERATIONS_CENTER_ID+"_augment", buffData.modifiedBy);
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

