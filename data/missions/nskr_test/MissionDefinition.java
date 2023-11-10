package data.missions.nskr_test;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {
		
		boolean dev = false;
		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "LOST", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "IBH", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "LOST_ Hunter Killers");
		api.setFleetTagline(FleetSide.ENEMY, "Independent Bounty Hunters");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Trash Them");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
		
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_sunburst_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_sunburst_boss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_prosperity_bal", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_prosperity_hbd", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_prosperity_nrg", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_prosperity_sup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_prosperity_agr", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_nighthawk_bal", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_nighthawk_hbd", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_nighthawk_nrg", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_nighthawk_sup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_nighthawk_agr", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_devilcatcher_bal", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_devilcatcher_hbd", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_devilcatcher_nrg", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_devilcatcher_sup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_devilcatcher_agr", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_blackbird_bal", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_blackbird_hbd", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_blackbird_nrg", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_blackbird_sup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_blackbird_agr", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_mercenary_bal", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_mercenary_hbd", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_mercenary_nrg", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_mercenary_sup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_mercenary_agr", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_dragontail_bal", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_dragontail_hbd", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_dragontail_nrg", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_dragontail_sup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_dragontail_agr", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_kingstork_bal", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_kingstork_hbd", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_kingstork_nrg", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_kingstork_sup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_kingstork_agr", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		
		api.addToFleet(FleetSide.PLAYER, "buffalo_kesteven_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		
		api.addToFleet(FleetSide.PLAYER, "nskr_kingslayer_coup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_kingslayer_rip", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_kingslayer_rat", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_kingslayer_mss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_rhea_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_rhea_sup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_rhea_elite", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_rhea_mss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		
		api.addToFleet(FleetSide.PLAYER, "nskr_borealis_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);		
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_borealis_beam", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_borealis_elite", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_borealis_fsup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);		
		
		api.addToFleet(FleetSide.PLAYER, "nskr_reverie_str", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_reverie_dsb", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_reverie_mss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_reverie_elite", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	

		api.addToFleet(FleetSide.PLAYER, "nskr_malediction_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_malediction_pre", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_malediction_sup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_malediction_elite", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		
		api.addToFleet(FleetSide.PLAYER, "nskr_stalwart_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_stalwart_sup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_stalwart_ast", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_stalwart_elite", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_stalwart_cb", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_stalwart_merc", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		api.addToFleet(FleetSide.PLAYER, "nskr_verity_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_verity_dsb", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_verity_cbt", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		if (dev)api.addToFleet(FleetSide.PLAYER, "nskr_verity_elite", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		
		api.addToFleet(FleetSide.PLAYER, "nskr_rorqual_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);		
		if (dev){					
		api.addToFleet(FleetSide.PLAYER, "nskr_minokawa_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_minokawa_tac", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_minokawa_ops", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_sovereign_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_sovereign_tac", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_sovereign_ops", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_nemesis_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		api.addToFleet(FleetSide.PLAYER, "nskr_nemesis_tac", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_nemesis_ops", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_muninn_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);		
		api.addToFleet(FleetSide.PLAYER, "nskr_muninn_tac", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_muninn_ops", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_warfare_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		api.addToFleet(FleetSide.PLAYER, "nskr_warfare_tac", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);		
		api.addToFleet(FleetSide.PLAYER, "nskr_warfare_ops", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		api.addToFleet(FleetSide.PLAYER, "nskr_eternity_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		api.addToFleet(FleetSide.PLAYER, "nskr_epochx_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_epochx_tac", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_epochx_ops", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_epoch_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_epoch_tac", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_epoch_ops", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_widow_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_widow_tac", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_widow_ops", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_torpor_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_torpor_tac", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_torpor_ops", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		
		api.addToFleet(FleetSide.PLAYER, "nskr_rorqual_boss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		api.addToFleet(FleetSide.PLAYER, "nskr_prosperity_boss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_onslaught_boss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_minokawa_e_boss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_eternity_e_boss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_minokawa_boss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_sovereign_boss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_eternity_boss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_nemesis_boss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_muninn_boss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_warfare_boss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		api.addToFleet(FleetSide.PLAYER, "nskr_epochx_boss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_epoch_boss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_widow_boss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_torpor_boss", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		
		api.addToFleet(FleetSide.PLAYER, "nskr_minokawa_empty", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_sovereign_empty", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_nemesis_empty", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		api.addToFleet(FleetSide.PLAYER, "nskr_muninn_empty", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);		
		api.addToFleet(FleetSide.PLAYER, "nskr_warfare_empty", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		api.addToFleet(FleetSide.PLAYER, "nskr_eternity_empty", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		api.addToFleet(FleetSide.PLAYER, "nskr_epochx_empty", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_epoch_empty", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_widow_empty", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_torpor_empty", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		
		api.addToFleet(FleetSide.PLAYER, "nskr_minokawa_e_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_minokawa_e_hev", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_minokawa_e_sup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_sovereign_e_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_sovereign_e_hev", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_sovereign_e_cbt", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_nemesis_e_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_nemesis_e_hev", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_nemesis_e_sup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_muninn_e_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_muninn_e_hev", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_muninn_e_sup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_warfare_e_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);		
		api.addToFleet(FleetSide.PLAYER, "nskr_warfare_e_cbt", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		api.addToFleet(FleetSide.PLAYER, "nskr_warfare_e_hev", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);	
		api.addToFleet(FleetSide.PLAYER, "nskr_eternity_e_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_eternity_e_hev", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_eternity_e_cbt", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_epochx_e_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_epochx_e_hev", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_epochx_e_sup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_epoch_e_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_epoch_e_hev", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_epoch_e_sup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_widow_e_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_widow_e_cbt", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_widow_e_hev", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_torpor_e_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_torpor_e_hev", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_torpor_e_sup", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_aed_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_aed_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_aed_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_aed_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_aed_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		
		api.addToFleet(FleetSide.PLAYER, "nskr_reverie_boss_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_reverie_boss_nor", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_harbinger_boss_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_harbinger_boss_nor", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_afflictor_boss_std", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.PLAYER, "nskr_afflictor_boss_nor", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		}
		// Set up the enemy fleet.
		
		api.addToFleet(FleetSide.ENEMY, "onslaught_Elite", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		
		api.addToFleet(FleetSide.ENEMY, "fury_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.ENEMY, "champion_Support", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
//		api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);

		
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.ENEMY, "hammerhead_Support", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Elite", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		
//		api.addToFleet(FleetSide.ENEMY, "heron_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.ENEMY, "drover_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		
		api.addToFleet(FleetSide.ENEMY, "lasher_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.ENEMY, "lasher_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.ENEMY, "brawler_Elite", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);				
		api.addToFleet(FleetSide.ENEMY, "brawler_Elite", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);

		// Set up the map.
		float width = 18000f;
		float height = 12000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// Add an asteroid field
		api.addAsteroidField(minX, minY + height / 2, 0, 8000f,
							 20f, 70f, 100);
		
		api.addObjective(minX + width * 0.15f + 7000, minY + height * 0.3f + 2800, "sensor_array");
		api.addObjective(minX + width * 0.8f - 8000, minY + height * 0.3f + 2600, "nav_buoy");
		
		api.addPlanet(0, 0, 15f, "star_white", 10f, true);	
	}
}
