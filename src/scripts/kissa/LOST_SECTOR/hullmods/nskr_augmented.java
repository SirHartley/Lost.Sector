package scripts.kissa.LOST_SECTOR.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.ids;

import java.awt.*;
import java.util.*;

public class nskr_augmented extends BaseHullMod {

	public static final float OPERATIONS_CENTER_BONUS = 90f;
	public static final float OPERATIONS_CENTER_RANGE = 1600f;
	public static final float RECOVERY_SHUTTLES_BONUS = 15f;
	public static final float ACCELERATED_SHIELDS_BONUS = 5f;
	public static final float FRONT_CONVERSION_BONUS = 5f;
	public static final float OMNI_CONVERSION_BONUS = 150f;
	public static final float STABILIZED_SHIELDS_BONUS = 5f;
	public static final float SOLAR_SHIELDING_BONUS = 10f;
	public static final float EXTENDED_SHIELDS_BONUS = 5f;
	public static final float REPAIR_UNIT_BONUS = 50f;
	public static final float IPDAI_BONUS = 50f;
	public static final float HARDENED_SHIELDS_BONUS = 5f;
	public static final float HEAVY_ARMOR_BONUS = 5f;
	public static final float ECM_BONUS = 100f;
	public static final float TURRET_GYROS_BONUS = 10f;
	public static final float IEA_BONUS = 1f;
	public static final float SHIELD_SHUNT_BONUS = 150f;
	public static final float HIGH_RESOLUTION_SENSORS_BONUS = 75f;
	public static final float FLUX_COIL_ADJUNCT_BONUS = 0.75f;
	public static final float FLUX_DISTRIBUTOR_BONUS = 1.5f;
	public static final float ARMORED_WEAPONS_BONUS = 10f;
	public static final float BLAST_DOORS_BONUS = 50f;
	public static final float BLAST_DOORS_THRESHOLD = 50f;
	public static final float SAFETY_OVERRIDE_BONUS = 25f;
	public static final float SAFETY_OVERRIDE_THRESHOLD = 66.67f;
	public static final float UNSTABLE_INJECTOR_BONUS = 15f;
	public static final float NAV_RELAY_BONUS = 10f;
	public static final float NAV_RELAY_RANGE = 1500f;
	public static final int NAV_RELAY_MAX = 5;
	public static final float CH_BONUS = 50f;
	public static final float CRITICAL_POINT_BONUS = 10f;

	public static final String OPERATIONS_CENTER_ID = "operations_center";
	public static final String RECOVERY_SHUTTLES_ID = "recovery_shuttles";
	public static final String ACCELERATED_SHIELDS_ID = "advancedshieldemitter";
	public static final String FRONT_CONVERSION_ID = "frontemitter";
	public static final String OMNI_CONVERSION_ID = "adaptiveshields";
	public static final String STABILIZED_SHIELDS_ID = "stabilizedshieldemitter";
	public static final String EXTENDED_SHIELDS_ID = "extendedshieldemitter";
	public static final String REPAIR_UNIT_ID = "autorepair";
	public static final String IPDAI_ID = "pointdefenseai";
	public static final String IEA_ID = "insulatedengine";
	public static final String SOLAR_SHIELDING_ID = "solar_shielding";
	public static final String HARDENED_SHIELDS_ID = "hardenedshieldemitter";
	public static final String HEAVY_ARMOR_ID = "heavyarmor";
	public static final String ECM_ID = "ecm";
	public static final String SAFETY_OVERRIDE_ID = "safetyoverrides";
	public static final String SHIELD_SHUNT_ID = "shield_shunt";
	public static final String TURRET_GYROS_ID = "turretgyros";
	public static final String HIGH_RESOLUTION_SENSORS_ID = "hiressensors";
	public static final String FLUX_COIL_ADJUNCT_ID = "fluxcoil";
	public static final String FLUX_DISTRIBUTOR_ID = "fluxdistributor";
	public static final String BLAST_DOORS_ID = "blast_doors";
	public static final String UNSTABLE_INJECTOR_ID = "unstable_injector";
	public static final String NAV_RELAY_ID = "nav_relay";
	public static final String INERTIAL_ID = "nskr_inertial";
	public static final String VOLATILE_ID = "nskr_volatile";
	public static final String ARMORED_WEAPON_MOUNT_ID = "armoredweapons";
	public static final String CONVERTED_HANGAR_ID = "converted_hangar";

	public static final Vector2f ZERO = new Vector2f();

	public static final Set<String> WEAPON_HULLMODS = new HashSet<>();
	static {
		// Hullmods with a weapon bonus
		WEAPON_HULLMODS.add(IPDAI_ID);
		WEAPON_HULLMODS.add(HARDENED_SHIELDS_ID);
		WEAPON_HULLMODS.add(HEAVY_ARMOR_ID);
		WEAPON_HULLMODS.add(ECM_ID);
		WEAPON_HULLMODS.add(SAFETY_OVERRIDE_ID);
		WEAPON_HULLMODS.add(INERTIAL_ID);
		WEAPON_HULLMODS.add(CONVERTED_HANGAR_ID);
	}
	public static final Set<String> RANGE_HULLMODS = new HashSet<>();
	static {
		// Hullmods with a range bonus
		RANGE_HULLMODS.add(SHIELD_SHUNT_ID);
		RANGE_HULLMODS.add(TURRET_GYROS_ID);
		RANGE_HULLMODS.add(HIGH_RESOLUTION_SENSORS_ID);
	}
	public static final Set<String> SHIELD_HULLMODS = new HashSet<>();
	static {
		// Hullmods with a shield bonus
		SHIELD_HULLMODS.add(ACCELERATED_SHIELDS_ID);
		SHIELD_HULLMODS.add(FRONT_CONVERSION_ID);
		SHIELD_HULLMODS.add(OMNI_CONVERSION_ID);
		SHIELD_HULLMODS.add(STABILIZED_SHIELDS_ID);
		SHIELD_HULLMODS.add(EXTENDED_SHIELDS_ID);
	}
	public static final Set<String> ENGINE_HULLMODS = new HashSet<>();
	static {
		// Hullmods with a speed bonus
		ENGINE_HULLMODS.add(IEA_ID);
		ENGINE_HULLMODS.add(UNSTABLE_INJECTOR_ID);
		ENGINE_HULLMODS.add(NAV_RELAY_ID);
	}
	public static final Set<String> STAT_HULLMODS = new HashSet<>();
	static {
		// Hullmods with a base stat bonus
		STAT_HULLMODS.add(OPERATIONS_CENTER_ID);
		STAT_HULLMODS.add(RECOVERY_SHUTTLES_ID);
		STAT_HULLMODS.add(FLUX_COIL_ADJUNCT_ID);
		STAT_HULLMODS.add(FLUX_DISTRIBUTOR_ID);
		STAT_HULLMODS.add(VOLATILE_ID);
		STAT_HULLMODS.add(ids.HIGH_CAPACITANCE_BANKS_HULLMOD_ID);
	}
	public static final Set<String> DURABILITY_HULLMODS = new HashSet<>();
	static {
		// Hullmods with a durability bonus
		DURABILITY_HULLMODS.add(REPAIR_UNIT_ID);
		DURABILITY_HULLMODS.add(BLAST_DOORS_ID);
		DURABILITY_HULLMODS.add(SOLAR_SHIELDING_ID);
		DURABILITY_HULLMODS.add(ARMORED_WEAPON_MOUNT_ID);
		DURABILITY_HULLMODS.add(ids.CRITICAL_POINT_PROTECTION_HULLMOD_ID);
	}

	static void log(final String message) {
		Global.getLogger(nskr_augmented.class).info(message);
	}

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipAPI ship = (ShipAPI)(stats.getEntity());
		// RECOVERY SHUTTLES
		if (stats.getVariant().hasHullMod(RECOVERY_SHUTTLES_ID)) {
			stats.getMaxCombatReadiness().modifyFlat(RECOVERY_SHUTTLES_ID+"_augment", RECOVERY_SHUTTLES_BONUS * 0.01f, "Recovery Shuttles Augment");
		}
		// ACCELERATED SHIELDS
		if (stats.getVariant().hasHullMod(ACCELERATED_SHIELDS_ID)) {
			stats.getShieldDamageTakenMult().modifyPercent(ACCELERATED_SHIELDS_ID+"_augment", -ACCELERATED_SHIELDS_BONUS);
		}
		// FRONT CONVERSION
		if (stats.getVariant().hasHullMod(FRONT_CONVERSION_ID)) {
			stats.getShieldDamageTakenMult().modifyPercent(FRONT_CONVERSION_ID+"_augment", -FRONT_CONVERSION_BONUS);
		}
		// OMNI CONVERSION
		if (stats.getVariant().hasHullMod(OMNI_CONVERSION_ID)) {
			stats.getShieldUnfoldRateMult().modifyPercent(OMNI_CONVERSION_ID+"_augment", OMNI_CONVERSION_BONUS);
		}
		// STABILIZED SHIELDS
		if (stats.getVariant().hasHullMod(STABILIZED_SHIELDS_ID)) {
			stats.getShieldDamageTakenMult().modifyPercent(STABILIZED_SHIELDS_ID+"_augment", -STABILIZED_SHIELDS_BONUS);
		}
		// EXTENDED SHIELDS
		if (stats.getVariant().hasHullMod(EXTENDED_SHIELDS_ID)) {
			stats.getShieldDamageTakenMult().modifyPercent(EXTENDED_SHIELDS_ID+"_augment", -EXTENDED_SHIELDS_BONUS);
		}
		// SOLAR SHIELDING
		if (stats.getVariant().hasHullMod(SOLAR_SHIELDING_ID)) {
			stats.getBeamDamageTakenMult().modifyPercent(SOLAR_SHIELDING_ID+"_augment", -SOLAR_SHIELDING_BONUS);
		}
		// REPAIR UNIT
		if (stats.getVariant().hasHullMod(REPAIR_UNIT_ID)) {
			stats.getCRLossPerSecondPercent().modifyPercent(REPAIR_UNIT_ID+"_augment", -REPAIR_UNIT_BONUS);
		}
		// IPDAI
		if (stats.getVariant().hasHullMod(IPDAI_ID)) {
			stats.getDamageToFighters().modifyPercent(IPDAI_ID+"_augment", IPDAI_BONUS);
		}
		// HARDENED SHIELDS
		if (stats.getVariant().hasHullMod(HARDENED_SHIELDS_ID)) {
			stats.getEnergyWeaponFluxCostMod().modifyPercent(HARDENED_SHIELDS_ID+"_augment", -HARDENED_SHIELDS_BONUS);
		}
		// HEAVY ARMOR
		if (stats.getVariant().hasHullMod(HEAVY_ARMOR_ID)) {
			stats.getBallisticRoFMult().modifyPercent(HEAVY_ARMOR_ID+"_augment", HEAVY_ARMOR_BONUS);
		}
		// ECM
		if (stats.getVariant().hasHullMod(ECM_ID)) {
			stats.getDamageToTargetEnginesMult().modifyPercent(ECM_ID+"_augment", ECM_BONUS);
			stats.getDamageToTargetWeaponsMult().modifyPercent(ECM_ID+"_augment", ECM_BONUS);
		}
		// TURRET GYROS
		if (stats.getVariant().hasHullMod(TURRET_GYROS_ID)) {
			stats.getBallisticWeaponRangeBonus().modifyPercent(TURRET_GYROS_ID+"_augment", TURRET_GYROS_BONUS);
			stats.getEnergyWeaponRangeBonus().modifyPercent(TURRET_GYROS_ID+"_augment", TURRET_GYROS_BONUS);
		}
		// IEA
		if (stats.getVariant().hasHullMod(IEA_ID)) {
			stats.getZeroFluxMinimumFluxLevel().modifyFlat(IEA_ID+"_augment", IEA_BONUS*0.01f);
		}
		// AWM
		if (stats.getVariant().hasHullMod(ARMORED_WEAPON_MOUNT_ID)) {
			stats.getHullBonus().modifyPercent(ARMORED_WEAPON_MOUNT_ID+"_augment", ARMORED_WEAPONS_BONUS);
		}
		// CONVERTED HANGAR
		if (stats.getVariant().hasHullMod(CONVERTED_HANGAR_ID)) {
			//stats.getFighterRefitTimeMult().modifyMult(CONVERTED_HANGAR_ID + "_augment", 1 / CH_BONUS);
			//stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(CONVERTED_HANGAR_ID + "_augment", CH_BONUS);
			//stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyMult(CONVERTED_HANGAR_ID + "_augment", CH_BONUS);

			stats.getDynamic().getMod(Stats.BOMBER_COST_MOD).modifyPercent(CONVERTED_HANGAR_ID +"_augment", -CH_BONUS);
			stats.getDynamic().getMod(Stats.FIGHTER_COST_MOD).modifyPercent(CONVERTED_HANGAR_ID +"_augment", -CH_BONUS);
			stats.getDynamic().getMod(Stats.INTERCEPTOR_COST_MOD).modifyPercent(CONVERTED_HANGAR_ID +"_augment", -CH_BONUS);
			stats.getDynamic().getMod(Stats.SUPPORT_COST_MOD).modifyPercent(CONVERTED_HANGAR_ID +"_augment", -CH_BONUS);
			if (hullSize!=HullSize.DESTROYER) stats.getNumFighterBays().modifyFlat(CONVERTED_HANGAR_ID +"_augment", 1f);
		}
		// CRITICAL POINT PROTECTION
		if (stats.getVariant().hasHullMod(ids.CRITICAL_POINT_PROTECTION_HULLMOD_ID)) {
			stats.getMinArmorFraction().modifyPercent(ids.CRITICAL_POINT_PROTECTION_HULLMOD_ID +"_augment", CRITICAL_POINT_BONUS);
		}
	}

	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		MutableShipStatsAPI stats = ship.getMutableStats();
		// FLUX DISTRIBUTOR
		if (stats.getVariant().hasHullMod(FLUX_DISTRIBUTOR_ID)) {
			stats.getFluxCapacity().modifyFlat(FLUX_DISTRIBUTOR_ID+"_augment", FLUX_DISTRIBUTOR_BONUS*stats.getFluxDissipation().modified);
		}
		//


		//LISTENERS

        // for base range
		ship.addListener(new AugmentRangeModifier());
		// FLUX COIL ADJUNCT
		if (stats.getVariant().hasHullMod(FLUX_COIL_ADJUNCT_ID)) {
			ship.addListener(new nskr_augmentedListener.nskr_fluxCoilAdjunctListener(ship));
		}
		// BLAST DOORS
		if (stats.getVariant().hasHullMod(BLAST_DOORS_ID)) {
			ship.addListener(new nskr_augmentedListener.nskr_blastDoorsListener(ship));
		}
		// SAFETY OVERRIDE
		if (stats.getVariant().hasHullMod(SAFETY_OVERRIDE_ID)) {
			ship.addListener(new nskr_augmentedListener.nskr_safetyOverrideListener(ship));
		}
		// UNSTABLE INJECTOR
		if (stats.getVariant().hasHullMod(UNSTABLE_INJECTOR_ID)) {
			ship.addListener(new nskr_augmentedListener.nskr_unstableInjectorListener(ship));
		}
		// NAV RELAY
		if (stats.getVariant().hasHullMod(NAV_RELAY_ID)) {
			ship.addListener(new nskr_augmentedListener.nskr_navRelayListener(ship));
		}
		//OPERATIONS CENTER
		if (stats.getVariant().hasHullMod(OPERATIONS_CENTER_ID)) {
			ship.addListener(new nskr_augmentedListener.nskr_opCenterListener(ship));
		}
	}

	public static class AugmentRangeModifier implements WeaponBaseRangeModifier {

		public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			return 0;
		}
		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1f;
		}
		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			if (weapon.getSlot() == null || weapon.getSlot().getWeaponType() != WeaponAPI.WeaponType.BALLISTIC && weapon.getSlot().getWeaponType() != WeaponAPI.WeaponType.ENERGY) {
				return 0f;
			}
			float bonus = 0f;
			// SHIELD SHUNT
			if (ship.getVariant().hasHullMod(SHIELD_SHUNT_ID)) {
				bonus += SHIELD_SHUNT_BONUS;
			}
			// HIGH RESOLUTION SENSORS
			if (ship.getVariant().hasHullMod(HIGH_RESOLUTION_SENSORS_ID)) {
				bonus += HIGH_RESOLUTION_SENSORS_BONUS;
			}
			return bonus;
		}
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

		Color tc = Misc.getHighlightColor();
		Color y = Misc.getHighlightColor();

		// HULLMODS
		tooltip.addSectionHeading("Current Augments", Alignment.MID, pad);
		// FLUX COIL ADJUNCT
		if (ship.getVariant().hasHullMod(FLUX_COIL_ADJUNCT_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/temporal_shell.png", 32.0f);
			text.addPara("FLUX COIL ADJUNCT", 0.0f, tc, "FLUX COIL ADJUNCT");
			text.addPara("Gain "+FLUX_COIL_ADJUNCT_BONUS+"%"+"% of current flux as dissipation.", 0.0f, y, FLUX_COIL_ADJUNCT_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// TURRET GYROS
		if (ship.getVariant().hasHullMod(TURRET_GYROS_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/ammo_feeder.png", 32.0f);
			text.addPara("ADVANCED TURRET GYROS", 0.0f, tc, "ADVANCED TURRET GYROS");
			text.addPara("Increases weapon range by "+(int)TURRET_GYROS_BONUS+"%"+"%.", 0.0f, y, (int)TURRET_GYROS_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// ECM
		if (ship.getVariant().hasHullMod(ECM_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/interdictor_array.png", 32.0f);
			text.addPara("ECM PACKAGE", 0.0f, tc, "ECM PACKAGE");
			text.addPara("Increases damage dealt to target weapons and engines by "+(int)ECM_BONUS+"%"+"%.", 0.0f, y, (int)ECM_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// HARDENED SHIELDS
		if (ship.getVariant().hasHullMod(HARDENED_SHIELDS_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/fortress_shield.png", 32.0f);
			text.addPara("HARDENED SHIELDS", 0.0f, tc, "HARDENED SHIELDS");
			text.addPara("Reduces energy weapon flux use by "+(int)HARDENED_SHIELDS_BONUS+"%"+"%.", 0.0f, y, (int)HARDENED_SHIELDS_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// HEAVY ARMOR
		if (ship.getVariant().hasHullMod(HEAVY_ARMOR_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/damper_field.png", 32.0f);
			text.addPara("HEAVY ARMOR", 0.0f, tc, "HEAVY ARMOR");
			text.addPara("Increases ballistic weapon rate of fire by "+(int)HEAVY_ARMOR_BONUS+"%"+"%.", 0.0f, y, (int)HEAVY_ARMOR_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// UNSTABLE INJECTOR
		if (ship.getVariant().hasHullMod(UNSTABLE_INJECTOR_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/burn_drive.png", 32.0f);
			text.addPara("UNSTABLE INJECTOR", 0.0f, tc, "UNSTABLE INJECTOR");
			text.addPara("Increases top speed by "+(int)UNSTABLE_INJECTOR_BONUS+"su/s when moving forwards.", 0.0f, y, (int)UNSTABLE_INJECTOR_BONUS+"su/s");
			tooltip.addImageWithText(pad);
		}
		// SOLAR SHIELDING
		if (ship.getVariant().hasHullMod(SOLAR_SHIELDING_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/damper_field.png", 32.0f);
			text.addPara("SOLAR SHIELDING", 0.0f, tc, "SOLAR SHIELDING");
			text.addPara("Reduces beam damage taken by "+(int)SOLAR_SHIELDING_BONUS+"%"+"%.", 0.0f, y, (int)SOLAR_SHIELDING_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// REPAIR UNIT
		if (ship.getVariant().hasHullMod(REPAIR_UNIT_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/active_flare_launcher.png", 32.0f);
			text.addPara("AUTOMATED REPAIR UNIT", 0.0f, tc, "AUTOMATED REPAIR UNIT");
			text.addPara("Reduces CR drain after peak performance time runs out by "+(int)REPAIR_UNIT_BONUS+"%"+"%.", 0.0f, y, (int)REPAIR_UNIT_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// IPDAI
		if (ship.getVariant().hasHullMod(IPDAI_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/drone_pd_mid.png", 32.0f);
			text.addPara("INTEGRATED POINT DEFENSE AI", 0.0f, tc, "INTEGRATED POINT DEFENSE AI");
			text.addPara("Increases damage dealt to fighters by "+(int)IPDAI_BONUS+"%"+"%.", 0.0f, y, (int)IPDAI_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// HIGH RESOLUTION SENSORS
		if (ship.getVariant().hasHullMod(HIGH_RESOLUTION_SENSORS_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/interdictor_array.png", 32.0f);
			text.addPara("HIGH RESOLUTION SENSORS", 0.0f, tc, "HIGH RESOLUTION SENSORS");
			text.addPara("Non missile weapon base range increased by "+(int)HIGH_RESOLUTION_SENSORS_BONUS+" units.", 0.0f, y, (int)HIGH_RESOLUTION_SENSORS_BONUS+"");
			tooltip.addImageWithText(pad);
		}
		// SHIELD SHUNT
		if (ship.getVariant().hasHullMod(SHIELD_SHUNT_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/quantum_disruptor.png", 32.0f);
			text.addPara("SHIELD SHUNT", 0.0f, tc, "SHIELD SHUNT");
			text.addPara("Non missile weapon base range increased by "+(int)SHIELD_SHUNT_BONUS+" units.", 0.0f, y, (int)SHIELD_SHUNT_BONUS+"");
			tooltip.addImageWithText(pad);
		}
		// ACCELERATED SHIELDS
		if (ship.getVariant().hasHullMod(ACCELERATED_SHIELDS_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/fortress_shield.png", 32.0f);
			text.addPara("ACCELERATED SHIELDS", 0.0f, tc, "ACCELERATED SHIELDS");
			text.addPara("Reduces shield damage taken by "+(int)ACCELERATED_SHIELDS_BONUS+"%"+"%.", 0.0f, y, (int)ACCELERATED_SHIELDS_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// STABILIZED SHIELDS
		if (ship.getVariant().hasHullMod(STABILIZED_SHIELDS_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/fortress_shield.png", 32.0f);
			text.addPara("STABILIZED SHIELDS", 0.0f, tc, "STABILIZED SHIELDS");
			text.addPara("Reduces shield damage taken by "+(int)STABILIZED_SHIELDS_BONUS+"%"+"%.", 0.0f, y, (int)STABILIZED_SHIELDS_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// EXTENDED SHIELDS
		if (ship.getVariant().hasHullMod(EXTENDED_SHIELDS_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/fortress_shield.png", 32.0f);
			text.addPara("EXTENDED SHIELDS", 0.0f, tc, "EXTENDED SHIELDS");
			text.addPara("Reduces shield damage taken by "+(int)EXTENDED_SHIELDS_BONUS+"%"+"%.", 0.0f, y, (int)EXTENDED_SHIELDS_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// FRONT CONVERSION
		if (ship.getVariant().hasHullMod(FRONT_CONVERSION_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/fortress_shield.png", 32.0f);
			text.addPara("SHIELD CONVERSION - FRONT", 0.0f, tc, "SHIELD CONVERSION - FRONT");
			text.addPara("Reduces shield damage taken by "+(int)FRONT_CONVERSION_BONUS+"%"+"%.", 0.0f, y, (int)FRONT_CONVERSION_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// OMNI CONVERSION
		if (ship.getVariant().hasHullMod(OMNI_CONVERSION_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/fortress_shield.png", 32.0f);
			text.addPara("SHIELD CONVERSION - OMNI", 0.0f, tc, "SHIELD CONVERSION - OMNI");
			text.addPara("Increases shield unfold rate by "+(int)OMNI_CONVERSION_BONUS+"%"+"%.", 0.0f, y, (int)OMNI_CONVERSION_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// BLAST DOORS
		if (ship.getVariant().hasHullMod(BLAST_DOORS_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/damper_field.png", 32.0f);
			text.addPara("BLAST DOORS", 0.0f, tc, "BLAST DOORS");
			text.addPara("Reduces hull damage taken by "+(int)BLAST_DOORS_BONUS+"%"+"% when below half hull.", 0.0f, y, (int)BLAST_DOORS_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// OPERATIONS CENTER
		if (ship.getVariant().hasHullMod(OPERATIONS_CENTER_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/targeting_feed.png", 32.0f);
			text.addPara("OPERATIONS CENTER", 0.0f, tc, "OPERATIONS CENTER");
			text.addPara("Prevents peak performance time from dropping by up to "+(int)OPERATIONS_CENTER_BONUS+" seconds, on any friendly ship that stays within "+(int)OPERATIONS_CENTER_RANGE+" units. " +
					"Does not stack.", 0.0f, y, (int)OPERATIONS_CENTER_BONUS+"");
			tooltip.addImageWithText(pad);
		}
		// RECOVERY SHUTTLES
		if (ship.getVariant().hasHullMod(RECOVERY_SHUTTLES_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/reserve_deployment.png", 32.0f);
			text.addPara("RECOVERY SHUTTLES", 0.0f, tc, "RECOVERY SHUTTLES");
			text.addPara("Increases maximum combat readiness by "+(int)RECOVERY_SHUTTLES_BONUS+"%"+"%.", 0.0f, y, (int)RECOVERY_SHUTTLES_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// SAFETY OVERRIDE
		if (ship.getVariant().hasHullMod(SAFETY_OVERRIDE_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/entropy_amplifier.png", 32.0f);
			text.addPara("SAFETY OVERRIDES", 0.0f, tc, "SAFETY OVERRIDES");
			text.addPara("Increases all weapon damage by "+(int)SAFETY_OVERRIDE_BONUS+"%"+"% when above two thirds flux capacity.", 0.0f, y, (int)SAFETY_OVERRIDE_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// NAV RELAY
		if (ship.getVariant().hasHullMod(NAV_RELAY_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/reserve_deployment.png", 32.0f);
			text.addPara("NAV RELAY", 0.0f, tc, "NAV RELAY");
			text.addPara("Increases top speed by "+(int)NAV_RELAY_BONUS+"su/s"+" for every allied ship with a nav relay within "+(int)NAV_RELAY_RANGE+" units, up to a maximum of "+(int)(NAV_RELAY_MAX*NAV_RELAY_BONUS)+"su/s.", 0.0f, y, (int)NAV_RELAY_BONUS+"su/s");
			tooltip.addImageWithText(pad);
		}
		// IEA
		if (ship.getVariant().hasHullMod(IEA_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/burn_drive.png", 32.0f);
			text.addPara("INSULATED ENGINE ASSEMBLY", 0.0f, tc, "INSULATED ENGINE ASSEMBLY");
			text.addPara("Increases the level at which the zero flux speed boost is active at by "+(int)IEA_BONUS+"%"+"%.", 0.0f, y, (int)IEA_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// FLUX DISTRIBUTOR
		if (ship.getVariant().hasHullMod(FLUX_DISTRIBUTOR_ID)) {
			float fdRounded = FLUX_DISTRIBUTOR_BONUS*10f;
			fdRounded = Math.round(fdRounded);
			fdRounded = fdRounded/10f;
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/emp_emitter.png", 32.0f);
			text.addPara("FLUX DISTRIBUTOR", 0.0f, tc, "FLUX DISTRIBUTOR");
			text.addPara("Gain "+fdRounded+"x"+" times the total dissipation as bonus flux capacity.", 0.0f, y, fdRounded+"x");
			tooltip.addImageWithText(pad);
		}
		// INERTIAL
		if (ship.getVariant().hasHullMod(INERTIAL_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/temporal_shell.png", 32.0f);
			text.addPara("INERTIAL SUPERCHARGER", 0.0f, tc, "INERTIAL SUPERCHARGER");
			text.addPara("The same bonus is also applied to projectile velocity.", 0.0f, y, "");
			tooltip.addImageWithText(pad);
		}
		// VOLATILE
		if (ship.getVariant().hasHullMod(VOLATILE_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/high_energy_focus.png", 32.0f);
			text.addPara("VOLATILE FLUX INJECTOR", 0.0f, tc, "VOLATILE FLUX INJECTOR");
			text.addPara("Penalty from high flux is halved.", 0.0f, y, "");
			tooltip.addImageWithText(pad);
		}
		// AWM
		if (ship.getVariant().hasHullMod(ARMORED_WEAPON_MOUNT_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/damper_field.png", 32.0f);
			text.addPara("ARMORED WEAPON MOUNTS", 0.0f, tc, "ARMORED WEAPON MOUNTS");
			text.addPara("Increases hull durability by "+(int)ARMORED_WEAPONS_BONUS+"%"+"%.", 0.0f, y, (int)ARMORED_WEAPONS_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// CONVERTED HANGAR
		if (ship.getVariant().hasHullMod(CONVERTED_HANGAR_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/damper_field.png", 32.0f);
			text.addPara("CONVERTED HANGAR", 0.0f, tc, "CONVERTED HANGAR");
			//text.addPara("Removes the penalty to fighter replacement time, and to replacement rate decrease and increase.", 0.0f, y, "Removes");
			if (ship.getHullSize()!=HullSize.DESTROYER) {
				text.addPara("grants an additional fighter bay and reduces all fighter OP costs by " + (int) CH_BONUS + "%%.", 0.0f, y, "additional", (int) CH_BONUS + "%");
			}else {
				text.addPara("Reduces all fighter OP costs by " + (int) CH_BONUS + "%%.", 0.0f, y, "additional", (int) CH_BONUS + "%");
			}
			tooltip.addImageWithText(pad);
		}
		// BIG BATS
		if (ship.getVariant().hasHullMod(ids.HIGH_CAPACITANCE_BANKS_HULLMOD_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/high_energy_focus.png", 32.0f);
			text.addPara("HIGH CAPACITANCE BANKS", 0.0f, tc, "HIGH CAPACITANCE BANKS");
			text.addPara("Increases system charge regen rate by "+(int)nskr_bigBats.AUGMENT_RECHARGE_BONUS+"%"+"%.", 0.0f, y, (int)nskr_bigBats.AUGMENT_RECHARGE_BONUS+"%");
			tooltip.addImageWithText(pad);
		}
		// CRITICAL POINT PROTECTION
		if (ship.getVariant().hasHullMod(ids.CRITICAL_POINT_PROTECTION_HULLMOD_ID)) {
			TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/damper_field.png", 32.0f);
			text.addPara("CRITICAL POINT PROTECTION", 0.0f, tc, "CRITICAL POINT PROTECTION");
			text.addPara("Increases minimum armor value by "+(int)CRITICAL_POINT_BONUS+"%"+"%.", 0.0f, y, (int)CRITICAL_POINT_BONUS+"%");
			tooltip.addImageWithText(pad);
		}

		//
		if (ship.getVariant().getUnusedOP(Global.getSector().getPlayerStats())>0) {
			// STAT
			String stMods = getHullmodSetString(ship, STAT_HULLMODS);
			tooltip.addSectionHeading("Available Stat Augments", Alignment.MID, pad);
			tooltip.addPara(stMods, pad, y, stMods);

			// WEAPONS
			String wMods = getHullmodSetString(ship, WEAPON_HULLMODS);
			tooltip.addSectionHeading("Available Weapon Augments", Alignment.MID, pad);
			tooltip.addPara(wMods, pad, y, wMods);

			// RANGE
			String rMods = getHullmodSetString(ship, RANGE_HULLMODS);
			tooltip.addSectionHeading("Available Range Augments", Alignment.MID, pad);
			tooltip.addPara(rMods, pad, y, rMods);

			// SHIELD
			String sMods = getHullmodSetString(ship, SHIELD_HULLMODS);
			tooltip.addSectionHeading("Available Shield Augments", Alignment.MID, pad);
			tooltip.addPara(sMods, pad, y, sMods);

			// ENGINE
			String eMods = getHullmodSetString(ship, ENGINE_HULLMODS);
			tooltip.addSectionHeading("Available Engine Augments", Alignment.MID, pad);
			tooltip.addPara(eMods, pad, y, eMods);

			// DURABILITY
			String dMods = getHullmodSetString(ship, DURABILITY_HULLMODS);
			tooltip.addSectionHeading("Available Durability Augments", Alignment.MID, pad);
			tooltip.addPara(dMods, pad, y, dMods);
		}
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

	private String getHullmodSetString(ShipAPI ship, Set<String> strings){
		Set<String> hullmods = new HashSet<>(strings);
		Set<String> textHullmods = new HashSet<>();
		for (String tmp : strings) {
			if (ship.getVariant().getHullMods().contains(tmp)) {
				hullmods.remove(tmp);
			}
		}
		if (Global.getSector() != null && Global.getSector().getPlayerFaction() != null && Global.getSettings().isInCampaignState()) {
			for (String tmp2 : strings) {
				if (!Global.getSector().getPlayerFaction().getKnownHullMods().contains(tmp2)) {
					hullmods.remove(tmp2);
				}
			}
		}
		String none = "-none";
		if (hullmods.isEmpty()) return none;
		//cursed
		for (String mod : hullmods){
			HullModSpecAPI h = Global.getSettings().getHullModSpec(mod);

			textHullmods.add(h.getDisplayName());
		}
		//PROGRAMMER
		String mods = textHullmods.toString();
		String mods2 = mods.replace("[","-");
		String mods3 = mods2.replace("]",".");

		return mods3;
	}
}
