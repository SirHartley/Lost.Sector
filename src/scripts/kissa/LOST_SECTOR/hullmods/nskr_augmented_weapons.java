package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;
import java.util.List;

public class nskr_augmented_weapons extends BaseHullMod {

    public static final Color ENERGY_BLUE = new Color(87, 224, 248, 255);
    public static final Color MISSILE_GREEN = new Color(154, 255, 79, 255);
    public static final Color BALLISTIC_YELLOW = new Color(255, 232, 79, 255);

    private float totalCount = 0f;
    private float energyCount = 0f;
    private float ballisticCount = 0f;
    private float missileCount = 0f;
    private float energyBonus = 0f;
    private float ballisticBonus = 0f;
    private float missileBonus = 0f;
    private float energyPart = 0f;
    private float ballisticPart = 0f;
    private float missilePart = 0f;
    public static final Vector2f ZERO = new Vector2f();

    static void log(final String message) {
        Global.getLogger(nskr_augmented_weapons.class).info(message);
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI)(stats.getEntity());

    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        MutableShipStatsAPI stats = ship.getMutableStats();

        // LOADOUT AUGMENT
        //reset
        calculateBonus(ship.getAllWeapons(), ship);

        ship.getMutableStats().getBallisticRoFMult().modifyPercent("loadout_ballistic_augment",ballisticBonus);
        ship.getMutableStats().getBallisticWeaponFluxCostMod().modifyPercent("loadout_ballistic_augment",-1f*ballisticBonus);

        ship.getMutableStats().getEnergyWeaponDamageMult().modifyPercent("loadout_energy_augment",energyBonus);

        ship.getMutableStats().getMissileMaxSpeedBonus().modifyPercent("loadout_missile_augment",missileBonus);
        ship.getMutableStats().getMissileHealthBonus().modifyPercent("loadout_missile_augment",missileBonus);

    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
            return;
        }
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        MutableShipStatsAPI stats = ship.getMutableStats();
        //update
        calculateBonus(ship.getAllWeapons(), ship);

        // it just works
        if (ballisticPart<=1f)ballisticPart *= 10000f;
        if (ballisticPart>=100f)ballisticPart = Math.round(ballisticPart);
        if (ballisticPart>100f)ballisticPart /= 100f;
        if (energyPart<=1f)energyPart *= 10000f;
        if (energyPart>=100f)energyPart = Math.round(energyPart);
        if (energyPart>100f)energyPart /= 100f;
        if (missilePart<=1f)missilePart *= 10000f;
        if (missilePart>=100f)missilePart = Math.round(missilePart);
        if (missilePart>100f)missilePart /= 100f;

        Color tc = Misc.getHighlightColor();
        Color y = Misc.getHighlightColor();
        Color g = util.BON_GREEN;
        Color r = Misc.getNegativeHighlightColor();
        Color eBonusColor = Color.WHITE;
        Color bBonusColor = Color.WHITE;
        Color mBonusColor = Color.WHITE;

        if (ballisticBonus==20f) bBonusColor = g;
        if (ballisticBonus==10f) bBonusColor = y;
        if (ballisticBonus==-10f) bBonusColor = r;
        if (energyBonus==20f) eBonusColor = g;
        if (energyBonus==10f) eBonusColor = y;
        if (energyBonus==-10f) eBonusColor = r;
        if (missileBonus==20f) mBonusColor = g;
        String bPorB;
        String ePorB;
        if (energyBonus<0) ePorB = "penalty"; else ePorB = "bonus";
        if (ballisticBonus<0) bPorB = "penalty"; else bPorB = "bonus";

        tooltip.addSectionHeading("Weapon Augments", Alignment.MID, pad);
        tooltip.addPara("-Ballistic, energy and missile weapons gain a bonus based on relative OP spent.", pad, g, "");
        LabelAPI label = tooltip.addPara("-Total OP spent " + (int)totalCount +". Energy OP spent " + (int)energyCount + ". Ballistic OP spent " + (int)ballisticCount + ". Missile OP Spent " + (int)missileCount,
                0.0f, y, "" + (int)totalCount, "" + (int)energyCount, "" + (int)ballisticCount, "" + (int)missileCount);
        label.setHighlight("" + (int)totalCount, "" + (int)energyCount, "" + (int)ballisticCount, "" + (int)missileCount);
        label.setHighlightColors(r, ENERGY_BLUE, BALLISTIC_YELLOW , MISSILE_GREEN);

        tooltip.addPara("", 0.0f, y);
        TooltipMakerAPI wText1 = tooltip.beginImageWithText("graphics/icons/hullsys/ammo_feeder.png", 32.0f);
        wText1.addPara("-Ballistic weapons ratio " + ballisticPart + "%%", 0.0f, y, ballisticPart + "%");
        wText1.addPara("-Current " + bPorB + " to rate of fire and flux efficiency " + Math.abs((int)ballisticBonus) + "%%", 0.0f, bBonusColor, Math.abs((int)ballisticBonus) + "%");
        tooltip.addImageWithText(pad);
        TooltipMakerAPI wText2 = tooltip.beginImageWithText("graphics/icons/hullsys/emp_emitter.png", 32.0f);
        wText2.addPara("-Energy weapons ratio " + energyPart + "%%", 0.0f, y, energyPart + "%");
        wText2.addPara("-Current " + ePorB + " to damage " + Math.abs((int)energyBonus) + "%%", 0.0f, eBonusColor, Math.abs((int)energyBonus) + "%");
        tooltip.addImageWithText(pad);
        TooltipMakerAPI wText3 = tooltip.beginImageWithText("graphics/icons/hullsys/missile_racks.png", 32.0f);
        wText3.addPara("-Missile weapons ratio " + missilePart + "%%", 0.0f, y, missilePart + "%");
        wText3.addPara("-Current bonus to missile max speed and hitpoints " + (int)missileBonus + "%%", 0.0f, mBonusColor, (int)missileBonus + "%");
        tooltip.addImageWithText(pad);
        tooltip.addPara("", 0.0f, y);

        //weapon bonuses
        LabelAPI labelW = tooltip.addPara("-Ballistic and energy bonuses change at 33%%, 40%% and 66%% of OP spent on weapon type.", 0.0f, y, "");
        labelW.setHighlight("Ballistic", "energy");
        labelW.setHighlightColors(BALLISTIC_YELLOW, ENERGY_BLUE);
        tooltip.addPara("-Missile bonus gained at 25%% of OP spent.", 0.0f, MISSILE_GREEN, "Missile");
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }

    @Override
    public Color getNameColor() {
        return new Color(245, 193, 68,255);
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }

    private void calculateBonus(List<WeaponAPI> weapons, ShipAPI ship){
        totalCount = 0f;
        energyCount = 0f;
        ballisticCount = 0f;
        missileCount = 0f;
        energyBonus = 0f;
        ballisticBonus = 0f;
        missileBonus = 0f;
        energyPart = 0f;
        ballisticPart = 0f;
        missilePart = 0f;

        MutableCharacterStatsAPI stats = Global.getFactory().createPerson().getStats();
        FleetMemberAPI member = ship.getFleetMember();
        if (member!=null) {
            PersonAPI commander = member.getFleetCommander();
            if (commander!=null){
                stats = commander.getStats();
            }
        }
        for (WeaponAPI w : weapons) {
            if (w.getType() == WeaponAPI.WeaponType.BALLISTIC){
                ballisticCount += w.getSpec().getOrdnancePointCost(stats, ship.getMutableStats());
            }
            if (w.getType() == WeaponAPI.WeaponType.ENERGY){
                energyCount += w.getSpec().getOrdnancePointCost(stats, ship.getMutableStats());
            }
            if (w.getType() == WeaponAPI.WeaponType.MISSILE){
                missileCount += w.getSpec().getOrdnancePointCost(stats, ship.getMutableStats());
            }
        }

        totalCount = energyCount + ballisticCount + missileCount;
        if (totalCount != 0) {
            energyPart = energyCount / totalCount;
            ballisticPart = ballisticCount / totalCount;
            missilePart = missileCount / totalCount;
        } else {
            energyPart = 0;
            ballisticPart = 0;
            missilePart = 0;
        }

        if (energyPart>=0.4f) energyBonus = 10f;
        if (energyPart>=0.66666f) energyBonus = 20f;
        if (energyPart<0.4f) energyBonus = 0f;
        if (energyPart<0.33333f) energyBonus = -10f;
        if (ballisticPart>=0.4f) ballisticBonus = 10f;
        if (ballisticPart>=0.66666f) ballisticBonus = 20f;
        if (ballisticPart<0.4f) ballisticBonus = 0f;
        if (ballisticPart<0.33333f) ballisticBonus = -10f;
        if (missilePart>=0.25f) missileBonus = 20f;
    }
}
