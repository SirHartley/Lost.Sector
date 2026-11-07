package lostsector.quest;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import lostsector.helper.fleet.FleetInfo;

// A view of one registered quest fleet, built when needed; the saved data is the FleetInfo and the fleet's memory.
public final class QuestFleet {

    private final FleetInfo info;

    QuestFleet(FleetInfo info) {
        this.info = info;
    }

    public CampaignFleetAPI fleet() {
        return info.fleet;
    }

    public FleetInfo info() {
        return info;
    }

    public String role() {
        return info.fleet.getMemoryWithoutUpdate().getString(QuestFleets.ROLE_KEY);
    }

    // Null outside record quests.
    public String record() {
        return info.fleet.getMemoryWithoutUpdate().getString(QuestFleets.RECORD_KEY);
    }

    String owner() {
        return info.fleet.getMemoryWithoutUpdate().getString(QuestFleets.OWNER_KEY);
    }

    public boolean isRole(String role) {
        return role != null && role.equals(role());
    }

    public boolean wasDestroyed(FleetDespawnReason reason) {
        return reason == FleetDespawnReason.DESTROYED_BY_BATTLE || reason == FleetDespawnReason.NO_MEMBERS;
    }

    // the encounter's loser is a combined copy that shares only the primary's memory, so casualties match by source fleet
    public boolean lostShips(FleetEncounterContextPlugin plugin) {
        BattleAPI battle = plugin.getBattle();
        FleetEncounterContextPlugin.DataForEncounterSide loser = plugin.getLoserData();
        if (battle == null || loser == null) return false;
        for (FleetEncounterContextPlugin.FleetMemberData member : loser.getOwnCasualties()) {
            if (member.getStatus() != FleetEncounterContextPlugin.Status.NORMAL && battle.getSourceFleet(member.getMember()) == info.fleet) return true;
        }
        return false;
    }
}
