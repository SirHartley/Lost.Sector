package lostsector.campaign.loot;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import lostsector.campaign.fleets.bounties.AbyssSpawner;
import lostsector.campaign.fleets.bounties.EternitySpawner;
import lostsector.campaign.fleets.bounties.MothershipSpawner;
import lostsector.campaign.fleets.bounties.RorqSpawner;
import lostsector.campaign.quests.util.QuestUtil;

import java.util.Map;

public class BountyLoot extends BaseCampaignEventListener implements EveryFrameScript {

	public static final String DEFEATED_ABYSS_KEY = "$nskr_abyssDefeated";
	public static final String DEFEATED_RORQ_KEY = "$nskr_rorqDefeated";
	public static final String DEFEATED_UMBRA_KEY = "$nskr_umbraDefeated";
	public static final String DEFEATED_HELIOS_KEY = "$nskr_heliosDefeated";

	static void log(final String message) {
		Global.getLogger(BountyLoot.class).info(message);
	}

	public BountyLoot() {
		super(true);
	}

	@Override
	public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {
		CampaignFleetAPI loser = plugin.getLoser();
		if (loser == null) return;

		//mothership "bounty" loot
		if (loser.getMemoryWithoutUpdate().contains(MothershipSpawner.LOOT_KEY)){
			if (loser.getFlagship()==null) {
				//completed
				QuestUtil.setCompleted(true, DEFEATED_HELIOS_KEY);

				loot.addCommodity("alpha_core", 1);

				loser.getMemoryWithoutUpdate().unset(MothershipSpawner.LOOT_KEY);
				loser.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
				//mark as completed for other scripts
				MothershipSpawner.setBountyCompleted(true);

				log("Loot added mothership loot");
			}
		}

		//eternity "bounty" loot
		if (loser.getMemoryWithoutUpdate().contains(EternitySpawner.LOOT_KEY)){
			if (loser.getFlagship()==null) {
				//completed
				QuestUtil.setCompleted(true, DEFEATED_UMBRA_KEY);

				loot.addCommodity("alpha_core", 2);
				loot.addCommodity("nskr_electronics", 500);

				loser.getMemoryWithoutUpdate().unset(EternitySpawner.LOOT_KEY);
				loser.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
				log("Loot added eternity loot");
			}
		}

		//abyss "bounty" loot
		if (loser.getMemoryWithoutUpdate().contains(AbyssSpawner.LOOT_KEY)){
			if (!AbyssSpawner.hasBountyShips(loser)) {
				//completed
				QuestUtil.setCompleted(true, DEFEATED_ABYSS_KEY);

				loot.addCommodity("alpha_core", 1);
				//payout for not recovering
				if (!AbyssSpawner.hasBountyShips(Global.getSector().getPlayerFleet())) {
					Global.getSector().getPlayerFleet().getCargo().getCredits().add(AbyssSpawner.BOUNTY_PAYOUT);
					log("Loot added abyss payout");
				}
				loser.getMemoryWithoutUpdate().unset(AbyssSpawner.LOOT_KEY);
				loser.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
				log("Loot added abyss loot");
			}
		}

		//rorq "bounty" loot
		if (loser.getMemoryWithoutUpdate().contains(RorqSpawner.LOOT_KEY)){
			if (loser.getFlagship()==null) {

				//completed
				QuestUtil.setCompleted(true, DEFEATED_RORQ_KEY);

				float paid = RorqSpawner.BOUNTY_PAYOUT * plugin.computePlayerContribFraction();
				setAmountPaid(paid, RorqSpawner.DEFEAT_ID_PAID);
				Global.getSector().getPlayerFleet().getCargo().getCredits().add(paid);

				setPlayerDefeated(true, RorqSpawner.DEFEAT_ID);
				//rep loss
				if (Global.getSector().getPlayerFaction().getRelationship(Factions.INDEPENDENT) > -0.5f) {
					Global.getSector().getPlayerFaction().adjustRelationship(Factions.INDEPENDENT, -0.10f);
					log("Loot added rorq rep penalty");
				}
				loser.getMemoryWithoutUpdate().unset(RorqSpawner.LOOT_KEY);
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
