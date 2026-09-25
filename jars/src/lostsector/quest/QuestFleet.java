package lostsector.quest;

import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
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
}
