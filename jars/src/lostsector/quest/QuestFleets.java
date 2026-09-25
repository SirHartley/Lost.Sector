package lostsector.quest;

import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import lostsector.helper.FleetHelper;
import lostsector.helper.fleet.FleetInfo;
import lostsector.helper.fleet.SimpleFleet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

// The only fleet registry for quests: one FleetHelper list in sector memory, shared by every quest. Each fleet's
// owner, role and record live in its memory, so nothing but the FleetInfo list is saved (README "Fleets").
public final class QuestFleets {

    // FleetHelper list, in FleetHelper.FLEET_ARRAY_KEYS.
    public static final String KEY = "$nskr_questFleets";
    public static final String OWNER_KEY = "$nskr_questOwner";
    public static final String ROLE_KEY = "$nskr_questRole";
    // Record quests only.
    public static final String RECORD_KEY = "$nskr_questRecord";

    private final QuestManager.Run<?, ?> run;

    QuestFleets(QuestManager.Run<?, ?> run) {
        this.run = run;
    }

    public CampaignFleetAPI spawn(String role, SimpleFleet spec) {
        return spawn(role, null, spec);
    }

    // Builds the fleet only when the role is declared; null otherwise.
    public CampaignFleetAPI spawn(String role, String record, SimpleFleet spec) {
        FleetRole declared = declaredRole(role);
        if (declared == null) return null;
        if (spec == null) {
            QuestManager.logError(run.id(), "spawn of role " + role + " refused: no fleet spec");
            return null;
        }
        CampaignFleetAPI fleet = spec.create();
        FleetInfo info = new FleetInfo(fleet, null, spec.loc);
        info.flagshipSimpleMember = spec.getFlagshipInfo();
        info.secondaries = spec.getSecondaryMembers();
        register(info, role, record, declared);
        return fleet;
    }

    // Registers a fleet built elsewhere, such as a dormant guardian.
    public CampaignFleetAPI adopt(String role, CampaignFleetAPI fleet) {
        FleetRole declared = declaredRole(role);
        if (declared == null) return null;
        if (fleet == null) {
            QuestManager.logError(run.id(), "adopt of role " + role + " refused: no fleet");
            return null;
        }
        if (find(fleet) != null) {
            QuestManager.logError(run.id(), "adopt of role " + role + " refused: " + fleet.getName() + " is already a quest fleet");
            return fleet;
        }
        register(new FleetInfo(fleet, null, null), role, null, declared);
        return fleet;
    }

    public List<QuestFleet> get(String role) {
        List<QuestFleet> result = new ArrayList<>();
        for (FleetInfo info : list()) {
            QuestFleet fleet = new QuestFleet(info);
            if (run.id().equals(fleet.owner()) && fleet.isRole(role)) result.add(fleet);
        }
        return result;
    }

    // Null when none.
    public QuestFleet first(String role) {
        for (FleetInfo info : list()) {
            QuestFleet fleet = new QuestFleet(info);
            if (run.id().equals(fleet.owner()) && fleet.isRole(role)) return fleet;
        }
        return null;
    }

    public void despawn(String role) {
        despawnWhere(role::equals);
    }

    // Hands the open dialog to the fleet encounter with the quest's first fleet of the role, as vanilla's
    // SalvageDefenderInteraction does: target, then setPlugin, then init. The no-argument FleetInteractionDialogPluginImpl
    // reads its FIDConfig from the fleet's MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, which register() set
    // from the role, so the encounter is the one the player gets by clicking the fleet. Null on success, else the problem.
    String engage(String role, InteractionDialogAPI dialog) {
        if (declaredRole(role) == null) return "role " + role + " is not declared";
        QuestFleet fleet = first(role);
        if (fleet == null) return "no fleet of role " + role;
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (!fleet.fleet().isAlive() || player == null || fleet.fleet().getContainingLocation() != player.getContainingLocation()) {
            return fleet.fleet().getName() + " is not in the player's location";
        }
        dialog.setInteractionTarget(fleet.fleet());
        FleetInteractionDialogPluginImpl plugin = new FleetInteractionDialogPluginImpl();
        dialog.setPlugin(plugin);
        plugin.init(dialog);
        QuestManager.logInfo(run.id(), "engage " + role + ": " + fleet.fleet().getName());
        return null;
    }

    private FleetRole declaredRole(String role) {
        FleetRole declared = run.quest.declarations().roles().get(role);
        if (declared == null) QuestManager.logError(run.id(), "role " + role + " is not declared");
        return declared;
    }

    private void register(FleetInfo info, String role, String record, FleetRole declared) {
        CampaignFleetAPI fleet = info.fleet;
        MemoryAPI memory = fleet.getMemoryWithoutUpdate();
        memory.set(OWNER_KEY, run.id());
        memory.set(ROLE_KEY, role);
        if (record != null) memory.set(RECORD_KEY, record);
        memory.set(roleFlag(run.id(), role), true);
        if (declared.configGen() != null) memory.set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, declared.configGen());
        if (declared.defeatTriggerName() != null) Misc.addDefeatTrigger(fleet, declared.defeatTriggerName());

        List<FleetInfo> fleets = list();
        fleets.add(info);
        FleetHelper.setFleets(fleets, KEY);
        QuestManager.logInfo(run.id(), "spawn " + role + (record == null ? "" : " [" + record + "]") + ": " + fleet.getName());
    }

    // Removes matching fleets of this quest from the list first, so the despawn report does not reach onFleetGone.
    void despawnWhere(Predicate<String> roles) {
        for (FleetInfo info : new ArrayList<>(list())) {
            QuestFleet fleet = new QuestFleet(info);
            if (!run.id().equals(fleet.owner()) || !roles.test(fleet.role())) continue;
            remove(info);
            QuestManager.logInfo(run.id(), "despawn " + fleet.role() + ": " + info.fleet.getName());
            info.fleet.despawn(FleetDespawnReason.OTHER, null);
        }
    }

    static String roleFlag(String questId, String role) {
        return "$nskr_" + questId + "_" + role;
    }

    static List<FleetInfo> list() {
        return FleetHelper.getFleets(KEY);
    }

    static FleetInfo find(CampaignFleetAPI fleet) {
        for (FleetInfo info : list()) {
            if (info.fleet == fleet) return info;
        }
        return null;
    }

    static void remove(FleetInfo info) {
        List<FleetInfo> fleets = list();
        fleets.remove(info);
        FleetHelper.setFleets(fleets, KEY);
    }
}
