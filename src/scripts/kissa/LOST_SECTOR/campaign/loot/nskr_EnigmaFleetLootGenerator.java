//////////////////////
//From HMI originally by Histidine
//////////////////////
package scripts.kissa.LOST_SECTOR.campaign.loot;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.FleetMemberData;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import org.lazywizard.lazylib.MathUtils;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.ArrayList;
import java.util.List;

public class nskr_EnigmaFleetLootGenerator extends BaseCampaignEventListener implements EveryFrameScript {

	public static final float SLOOT_PER_HULL_POINT = 0.0010f;
	public static final float ALPHA_CHANCE = 0.10f;
	public static final float BETA_CHANCE = 0.15f;
	public static final float GAMMA_CHANCE = 0.20f;

	public static final float CHANCE_TO_REMOVE_WEAPON_DROP = 0.50f;

	static void log(final String message) {
		Global.getLogger(nskr_EnigmaFleetLootGenerator.class).info(message);
	}

	public nskr_EnigmaFleetLootGenerator() {
		super(true);
	}

	@Override
	public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {
		CampaignFleetAPI loser = plugin.getLoser();
		if (loser == null) return;

		int enigmaHull = 0;
		int gammaCount = 0;
		int betaCount = 0;
		int alphaCount = 0;
		List<FleetEncounterContextPlugin.FleetMemberData> casualties = plugin.getLoserData().getOwnCasualties();
		for (FleetMemberData memberData : casualties) {
			FleetEncounterContextPlugin.Status status = memberData.getStatus();
			if (status == FleetEncounterContextPlugin.Status.NORMAL) continue;
			FleetMemberAPI member = memberData.getMember();
			if (!util.isProtTech(member)) continue;

			float hull = member.getHullSpec().getHitpoints() + member.getHullSpec().getArmorRating() * 8;
			if (member.isFighterWing()) hull *= member.getNumFightersInWing();
			enigmaHull += hull;

			if (member.isFighterWing()) continue;
			PersonAPI officer = member.getCaptain();
			if (officer==null)continue;
			String aiType = officer.getAICoreId();
			if (aiType!=null) {
				if (aiType.equals("gamma_core")) {
					gammaCount++;
					//log("gamma_count "+gammaCount);
				}
				if (aiType.equals("beta_core")) {
					if (Math.random() < 0.50f) {
						gammaCount++;
					} else
						betaCount++;
					//log("beta_count "+betaCount);
				}
				if (aiType.equals("alpha_core")) {
					if (Math.random() < 0.50f) {
						betaCount++;
					} else
						alphaCount++;
					//log("alpha_count "+alphaCount);
				}
			}
		}

		float contrib = plugin.computePlayerContribFraction();
		enigmaHull *= contrib;

		if (enigmaHull != 0) {
			int numsloot = (int)(enigmaHull * SLOOT_PER_HULL_POINT * MathUtils.getRandomNumberInRange(0.70f, 1.30f));
			loot.addCommodity("nskr_electronics", numsloot);
		}
		//AI cores
		for (int x=0;x<gammaCount;x++) {
			if (Math.random()<GAMMA_CHANCE)loot.addCommodity("gamma_core", 1f);
		}
		for (int x=0;x<betaCount;x++) {
			if (Math.random()<BETA_CHANCE)loot.addCommodity("beta_core", 1f);
		}
		for (int x=0;x<alphaCount;x++) {
			if (Math.random()<ALPHA_CHANCE)loot.addCommodity("alpha_core", 1f);
		}

		//weapon drop fuckery
		//chance to remove a dropped weapon stack
		ArrayList<CargoAPI.CargoItemQuantity<String>> weapons = new ArrayList<>(1000);
		weapons.addAll(loot.getWeapons());
		for (CargoAPI.CargoItemQuantity<String> weapon : weapons){
			WeaponSpecAPI wep = Global.getSettings().getWeaponSpec(weapon.getItem());
			if (!wep.hasTag("enigma")) continue;
			if (Math.random()<CHANCE_TO_REMOVE_WEAPON_DROP){
				loot.removeWeapons(weapon.getItem(), weapon.getCount());

				log("Loot removed weapon drop " + weapon.getItem() + " count " + weapon.getCount());
			}
		}
	}

	@Override
	public boolean isDone() {
		return false;
	}

	@Override
	public boolean runWhilePaused() {
		return false;
	}

	@Override
	public void advance(float amount) {

	}

}

