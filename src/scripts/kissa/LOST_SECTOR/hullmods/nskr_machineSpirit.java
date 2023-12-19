package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.util;

import java.awt.*;

public class nskr_machineSpirit extends BaseHullMod {
    //

    public static final float DMG_THRESHOLD = 50f;
    public static final float EMP_CHANCE = 0.20f;

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.addListener(new spawnArcListener());
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        //CombatEngineAPI engine = Global.getCombatEngine();
        //if (engine == null) {
        //    return;
        //}
        //if (engine.isPaused()) {
        //    return;
        //}
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 12.0f;
        Color tc = Misc.getTextColor();
        Color y = Misc.getHighlightColor();
        Color g = Misc.interpolateColor(Misc.getGrayColor(), tc, 0.5f);
        Color bad = util.TT_ORANGE;

        tooltip.addSectionHeading("Stats", Alignment.MID, pad);
        tooltip.addPara("Weapons that deal "+(int)DMG_THRESHOLD+" points of base damage or more have a "+(int)(EMP_CHANCE*100f)+"%%"+" to spawn an EMP arc against automated vessels. " +
                "Hits on shields have a chance to generate a shield-penetrating arc based on the target's hard flux level.", pad, y, (int)DMG_THRESHOLD+"", (int)(EMP_CHANCE*100f)+"%");
        tooltip.addPara("The EMP arc deals 1x the original hits damage as energy and 2x of that as EMP.", pad, y, "1x", "energy", "2x", "EMP");

        //
        tooltip.addPara("\"For those are the most wicked of creations, it has to take the mask off. One has to do it to bear such a great sin against creation.\" Transcript of Pather sermon, location unknown.", pad*2f, g, g, "").italicize();
    }

    public static class spawnArcListener implements DamageDealtModifier {

        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            CombatEngineAPI engine = Global.getCombatEngine();
            if (engine == null) {
                return null;
            }
            if (engine.isPaused()) {
                return null;
            }
            if (damage == null || damage.getStats() == null || damage.getStats().getEntity() == null) return null;
            if (!(target instanceof ShipAPI)) return null;
            ShipAPI ship = (ShipAPI) target;

            if (damage.getBaseDamage() < DMG_THRESHOLD || damage.isDps() && damage.getBaseDamage()/10f < DMG_THRESHOLD) return null;
            //only automated
            if (!ship.getVariant().getHullMods().contains(HullMods.AUTOMATED) && !ship.getVariant().getHullMods().contains("sotf_sierrasconcord")) return null;

            float chance = ship.getFluxTracker().getMaxFlux() / ship.getFluxTracker().getHardFlux();
            //hardened reduces shield pierce
            if (ship.getVariant().getHullMods().contains(HullMods.HARDENED_SHIELDS)) chance /= 2f;

            if (!shieldHit){
                spawnEmpArc(ship,damage, point);

            } else if (Math.random() < chance){
                spawnEmpArc(ship,damage, point);

            }

            return null;
        }

        private void spawnEmpArc(ShipAPI ship, DamageAPI damage, Vector2f point){
            CombatEngineAPI engine = Global.getCombatEngine();

            if (Math.random() < EMP_CHANCE) {
                float emp = damage.getDamage() * 1.0f;
                float dam = damage.getDamage() * 2.0f;

                if (damage.isDps()) {
                    emp /= 10f;
                    dam /= 10f;
                }

                engine.spawnEmpArc((ShipAPI) damage.getStats().getEntity(), point, ship, ship,
                        DamageType.ENERGY,
                        dam,
                        emp, // emp
                        100000f, // max range
                        "tachyon_lance_emp_impact",
                        20f, // thickness
                        new Color(155, 248, 255, 255),
                        new Color(255, 255, 255, 255)
                );
            }
        }
    }

}
