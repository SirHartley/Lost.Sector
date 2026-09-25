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

    // The registered fleet of any quest, or null.
    public QuestFleet registered(CampaignFleetAPI fleet) {
        FleetInfo info = find(fleet);
        return info == null ? null : new QuestFleet(info);
    }

    // Clears the fleet's whole memory, as waking a dormant fleet does, and writes back what registration keeps there
    // for a registered fleet of any quest: owner, role, record, role flag, config and defeat trigger. The FleetInfo
    // stays in the list, so the fleet keeps its orders. A fleet that is not registered is only cleared.
    public void clearMemory(CampaignFleetAPI fleet) {
        MemoryAPI memory = fleet.getMemoryWithoutUpdate();
        String owner = memory.getString(OWNER_KEY);
        String role = memory.getString(ROLE_KEY);
        String record = memory.getString(RECORD_KEY);
        fleet.getMemory().clear();
        if (owner == null || role == null || find(fleet) == null) return;
        QuestManager manager = QuestManager.get();
        writeKeys(fleet, owner, role, record, manager == null ? null : manager.declaredRole(owner, role));
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

    // The fleet takes the orders, flag, config and defeat trigger of another declared role of this quest; the old
    // role's flag, config and defeat trigger are removed. Owner, record and FleetInfo stay.
    public void reassign(QuestFleet fleet, String role) {
        FleetRole declared = declaredRole(role);
        if (declared == null || fleet == null) return;
        if (!run.id().equals(fleet.owner()) || find(fleet.fleet()) == null) {
            QuestManager.logError(run.id(), "reassign to role " + role + " refused: not a fleet of this quest");
            return;
        }
        String old = fleet.role();
        if (role.equals(old)) return;
        CampaignFleetAPI campaignFleet = fleet.fleet();
        MemoryAPI memory = campaignFleet.getMemoryWithoutUpdate();
        FleetRole oldDeclared = run.quest.declarations().roles().get(old);
        if (old != null) memory.unset(roleFlag(run.id(), old));
        if (oldDeclared != null && oldDeclared.configGen() != null) memory.unset(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN);
        if (oldDeclared != null && oldDeclared.defeatTriggerName() != null) Misc.removeDefeatTrigger(campaignFleet, oldDeclared.defeatTriggerName());
        memory.set(ROLE_KEY, role);
        memory.set(roleFlag(run.id(), role), true);
        if (declared.configGen() != null) memory.set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, declared.configGen());
        if (declared.defeatTriggerName() != null) Misc.addDefeatTrigger(campaignFleet, declared.defeatTriggerName());
        QuestManager.logInfo(run.id(), "reassign " + old + " -> " + role + ": " + campaignFleet.getName());
    }

    private FleetRole declaredRole(String role) {
        FleetRole declared = run.quest.declarations().roles().get(role);
        if (declared == null) QuestManager.logError(run.id(), "role " + role + " is not declared");
        return declared;
    }

    private void register(FleetInfo info, String role, String record, FleetRole declared) {
        CampaignFleetAPI fleet = info.fleet;
        writeKeys(fleet, run.id(), role, record, declared);

        List<FleetInfo> fleets = list();
        fleets.add(info);
        FleetHelper.setFleets(fleets, KEY);
        QuestManager.logInfo(run.id(), "spawn " + role + (record == null ? "" : " [" + record + "]") + ": " + fleet.getName());
    }

    // The fleet memory of a registered fleet; declared is null when the owning quest or role is unknown.
    private static void writeKeys(CampaignFleetAPI fleet, String owner, String role, String record, FleetRole declared) {
        MemoryAPI memory = fleet.getMemoryWithoutUpdate();
        memory.set(OWNER_KEY, owner);
        memory.set(ROLE_KEY, role);
        if (record != null) memory.set(RECORD_KEY, record);
        memory.set(roleFlag(owner, role), true);
        if (declared == null) return;
        if (declared.configGen() != null) memory.set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, declared.configGen());
        if (declared.defeatTriggerName() != null) Misc.addDefeatTrigger(fleet, declared.defeatTriggerName());
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
