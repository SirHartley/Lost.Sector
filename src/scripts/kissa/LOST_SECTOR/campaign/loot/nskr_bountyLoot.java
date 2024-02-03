package scripts.kissa.LOST_SECTOR.campaign.loot;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_abyssSpawner;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_eternitySpawner;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_mothershipSpawner;
import scripts.kissa.LOST_SECTOR.campaign.fleets.bounties.nskr_rorqSpawner;
import scripts.kissa.LOST_SECTOR.campaign.quests.util.questUtil;

import java.util.Map;

public class nskr_bountyLoot extends BaseCampaignEventListener implements EveryFrameScript {

	public static final String DEFEATED_ABYSS_KEY = "$nskr_abyssDefeated";
	public static final String DEFEATED_RORQ_KEY = "$nskr_rorqDefeated";
	public static final String DEFEATED_UMBRA_KEY = "$nskr_umbraDefeated";
	public static final String DEFEATED_HELIOS_KEY = "$nskr_heliosDefeated";

	static void log(final String message) {
		Global.getLogger(nskr_bountyLoot.class).info(message);
	}

	public nskr_bountyLoot() {
		super(true);
	}

	@Override
	public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {
		CampaignFleetAPI loser = plugin.getLoser();
		if (loser == null) return;

		//mothership "bounty" loot
		if (loser.getMemoryWithoutUpdate().contains(nskr_mothershipSpawner.LOOT_KEY)){
			if (loser.getFlagship()==null) {
				//completed
				questUtil.setCompleted(true, DEFEATED_HELIOS_KEY);

				loot.addCommodity("alpha_core", 1);

				loser.getMemoryWithoutUpdate().unset(nskr_mothershipSpawner.LOOT_KEY);
				loser.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
				//mark as completed for other scripts
				nskr_mothershipSpawner.setBountyCompleted(true);

				log("Loot added mothership loot");
			}
		}

		//eternity "bounty" loot
		if (loser.getMemoryWithoutUpdate().contains(nskr_eternitySpawner.LOOT_KEY)){
			if (loser.getFlagship()==null) {
				//completed
				questUtil.setCompleted(true, DEFEATED_UMBRA_KEY);

				loot.addCommodity("alpha_core", 2);
				loot.addCommodity("nskr_electronics", 500);

				loser.getMemoryWithoutUpdate().unset(nskr_eternitySpawner.LOOT_KEY);
				loser.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
				log("Loot added eternity loot");
			}
		}

		//abyss "bounty" loot
		if (loser.getMemoryWithoutUpdate().contains(nskr_abyssSpawner.LOOT_KEY)){
			if (!nskr_abyssSpawner.hasBountyShips(loser)) {
				//completed
				questUtil.setCompleted(true, DEFEATED_ABYSS_KEY);

				loot.addCommodity("alpha_core", 1);
				//payout for not recovering
				if (!nskr_abyssSpawner.hasBountyShips(Global.getSector().getPlayerFleet())) {
					Global.getSector().getPlayerFleet().getCargo().getCredits().add(nskr_abyssSpawner.BOUNTY_PAYOUT);
					log("Loot added abyss payout");
				}
				loser.getMemoryWithoutUpdate().unset(nskr_abyssSpawner.LOOT_KEY);
				loser.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
				log("Loot added abyss loot");
			}
		}

		//rorq "bounty" loot
		if (loser.getMemoryWithoutUpdate().contains(nskr_rorqSpawner.LOOT_KEY)){
			if (loser.getFlagship()==null) {

				//completed
				questUtil.setCompleted(true, DEFEATED_RORQ_KEY);

				float paid = nskr_rorqSpawner.BOUNTY_PAYOUT * plugin.computePlayerContribFraction();
				setAmountPaid(paid, nskr_rorqSpawner.DEFEAT_ID_PAID);
				Global.getSector().getPlayerFleet().getCargo().getCredits().add(paid);

				setPlayerDefeated(true, nskr_rorqSpawner.DEFEAT_ID);
				//rep loss
				if (Global.getSector().getPlayerFaction().getRelationship(Factions.INDEPENDENT) > -0.5f) {
					Global.getSector().getPlayerFaction().adjustRelationship(Factions.INDEPENDENT, -0.10f);
					log("Loot added rorq rep penalty");
				}
				loser.getMemoryWithoutUpdate().unset(nskr_rorqSpawner.LOOT_KEY);
				loser.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
				log("Loot added rorq payout " + (int) paid);
			}
		}

	}

	@Override
	public void reportPlayerEngagement(EngagementResultAPI result) {
		CampaignFleetAPI loser = result.getLoserResult().getFleet();
		if (loser == null) return;
	}

	public static boolean getPlayerDefeated(String id) {

		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(id)) data.put(id, false);

		return (boolean)data.get(id);
	}

	public static void setPlayerDefeated(boolean playerDefeated, String id) {

		Map<String, Object> data = Global.getSector().getPersistentData();
		data.put(id, playerDefeated);
	}

	public static float getAmountPaid(String id) {
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(id))data.put(id, 0f);

		return (float)data.get(id);
	}

	public static float setAmountPaid(float value, String id) {

		Map<String, Object> data = Global.getSector().getPersistentData();
		data.put(id, value);

		return (float)data.get(id);
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
