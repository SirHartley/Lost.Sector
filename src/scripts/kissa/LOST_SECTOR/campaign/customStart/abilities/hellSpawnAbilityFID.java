package scripts.kissa.LOST_SECTOR.campaign.customStart.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import scripts.kissa.LOST_SECTOR.util.ids;

import java.util.ArrayList;
import java.util.List;

public class hellSpawnAbilityFID extends FleetInteractionDialogPluginImpl {

    //
    // Basically the whole SotfCourserFIDPluginImpl from Secrets of The Frontier
    // Original author Inventor Raccoon
    //

    public static float JOIN_RANGE = 2000f;
    // factions that will only fight if they are low/no rep impact (i.e acting unofficially)
    public static List<String> HELLSPAWN_AVOID = new ArrayList<>();
    static {
        HELLSPAWN_AVOID.add(Factions.OMEGA);
    }
    // factions that Courser will never fight (i.e player fleets)
    public static List<String> HELLSPAWN_NEVER_ATTACK = new ArrayList<>();
    static {
        HELLSPAWN_AVOID.add(Factions.PLAYER);
        HELLSPAWN_AVOID.add(ids.ENIGMA_FACTION_ID);
    }

    public hellSpawnAbilityFID() {
        this(null);
    }

    public hellSpawnAbilityFID(FIDConfig params) {
        this.config = params;

        if (origFlagship == null) {
            origFlagship = Global.getSector().getPlayerFleet().getFlagship();
        }
        if (origCaptains.isEmpty()) {
            for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
                origCaptains.put(member, member.getCaptain());
            }
            membersInOrderPreEncounter = new ArrayList<>(Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy());
        }
    }

    protected void pullInNearbyFleets() {
        BattleAPI b = context.getBattle();
        if (!ongoingBattle) {
            b.join(Global.getSector().getPlayerFleet());
        }

        BattleAPI.BattleSide playerSide = b.pickSide(Global.getSector().getPlayerFleet());

        boolean hostile = otherFleet.getAI() != null && otherFleet.getAI().isHostileTo(playerFleet);
        if (ongoingBattle) hostile = true;

        CampaignFleetAPI actualPlayer = Global.getSector().getPlayerFleet();
        CampaignFleetAPI actualOther = (CampaignFleetAPI) (dialog.getInteractionTarget());

        pulledIn.clear();

        if (config.pullInStations && !b.isStationInvolved()) {
            SectorEntityToken closestEntity = null;
            CampaignFleetAPI closest = null;
            Pair<SectorEntityToken, CampaignFleetAPI> p = Misc.getNearestStationInSupportRange(actualOther);
            if (p != null) {
                closestEntity = p.one;
                closest = p.two;
            }

            if (closest != null) {
                BattleAPI.BattleSide joiningSide = b.pickSide(closest, true);
                boolean canJoin = joiningSide != BattleAPI.BattleSide.NO_JOIN;
                if (!config.pullInAllies && joiningSide == playerSide) {
                    canJoin = false;
                }
                if (!config.pullInEnemies && joiningSide != playerSide) {
                    canJoin = false;
                }
                if (b == closest.getBattle()) {
                    canJoin = false;
                }
                if (closest.getBattle() != null) {
                    canJoin = false;
                }

                if (canJoin) {
                    if (closestEntity != null) {
                        closestEntity.getMarket().reapplyIndustries(); // need to pick up station CR value, in some cases
                    }
                    b.join(closest);
                    pulledIn.add(closest);

                    if (!config.straightToEngage && config.showPullInText) {
                        if (b.getSide(playerSide) == b.getSideFor(closest)) {
                            textPanel.addParagraph(
                                    Misc.ucFirst(closest.getNameWithFactionKeepCase()) + ": supporting your forces.");//, FRIEND_COLOR);
                        } else {
                            if (hostile) {
                                textPanel.addParagraph(Misc.ucFirst(closest.getNameWithFactionKeepCase()) + ": supporting the enemy.");//, ENEMY_COLOR);
                            } else {
                                textPanel.addParagraph(Misc.ucFirst(closest.getNameWithFactionKeepCase()) + ": supporting the opposing side.");
                            }
                        }
                        textPanel.highlightFirstInLastPara(closest.getNameWithFactionKeepCase() + ":", closest.getFaction().getBaseUIColor());
                    }
                }
            }
        }


        for (CampaignFleetAPI fleet : actualPlayer.getContainingLocation().getFleets()) {
            if (b == fleet.getBattle()) continue;
            if (fleet.getBattle() != null) continue;

            if (fleet.isStationMode()) continue;

            float dist = Misc.getDistance(actualOther.getLocation(), fleet.getLocation());
            dist -= actualOther.getRadius();
            dist -= fleet.getRadius();

            if (fleet.getFleetData().getNumMembers() <= 0) continue;

            float baseSensorRange = playerFleet.getBaseSensorRangeToDetect(fleet.getSensorProfile());
            boolean visible = fleet.isVisibleToPlayerFleet();
            SectorEntityToken.VisibilityLevel level = fleet.getVisibilityLevelToPlayerFleet();

            float joinRange = Misc.getBattleJoinRange();
            if (fleet.getFaction().isPlayerFaction() && !fleet.isStationMode()) {
                joinRange += Global.getSettings().getFloat("battleJoinRangePlayerFactionBonus");
            }

            if (fleet.getMemoryWithoutUpdate().contains(hellSpawnAbility.HELL_FLEET_KEY) && (dist < JOIN_RANGE)
                    && !HELLSPAWN_NEVER_ATTACK.contains(actualOther.getFaction().getId())
                    && (!HELLSPAWN_AVOID.contains(actualOther.getFaction().getId()) ||
                    actualOther.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_LOW_REP_IMPACT) ||
                    actualOther.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_NO_REP_IMPACT)) && !fleet.isHostileTo(playerFleet)) {
                BattleAPI.BattleSide joiningSide = b.pickSide(fleet, false);
                // ignore IBB's "do not pull in allies" because it's more confusing than anything
                if (!config.pullInAllies && !actualOther.getFaction().getId().equals("famous_bounty")) continue;
                if (joiningSide != playerSide) continue;

                b.join(fleet);
                pulledIn.add(fleet);
            }

            if (dist < joinRange &&
                    (dist < baseSensorRange || (visible && level != SectorEntityToken.VisibilityLevel.SENSOR_CONTACT)) &&
                    ((fleet.getAI() != null && fleet.getAI().wantsToJoin(b, true)) && !pulledIn.contains(fleet) || fleet.isStationMode())) {

                BattleAPI.BattleSide joiningSide = b.pickSide(fleet, true);
                if (!config.pullInAllies && joiningSide == playerSide) continue;
                if (!config.pullInEnemies && joiningSide != playerSide) continue;

                b.join(fleet);
                pulledIn.add(fleet);

                if (!config.straightToEngage && config.showPullInText) {
                    if (b.getSide(playerSide) == b.getSideFor(fleet)) {
                        textPanel.addParagraph(Misc.ucFirst(fleet.getNameWithFactionKeepCase()) + ": supporting your forces.");//, FRIEND_COLOR);
                    } else {
                        if (hostile) {
                            textPanel.addParagraph(Misc.ucFirst(fleet.getNameWithFactionKeepCase()) + ": joining the enemy.");//, ENEMY_COLOR);
                        } else {
                            textPanel.addParagraph(Misc.ucFirst(fleet.getNameWithFactionKeepCase()) + ": supporting the opposing side.");
                        }
                    }
                    textPanel.highlightFirstInLastPara(fleet.getNameWithFactionKeepCase() + ":", fleet.getFaction().getBaseUIColor());
                }
            }
        }

        if (otherFleet != null) otherFleet.inflateIfNeeded();
        for (CampaignFleetAPI curr : pulledIn) {
            curr.inflateIfNeeded();
        }

        if (!ongoingBattle) {
            b.genCombined();
            b.takeSnapshots();
            playerFleet = b.getPlayerCombined();
            otherFleet = b.getNonPlayerCombined();
            if (!config.straightToEngage) {
                showFleetInfo();
            }
        }
    }

    public static boolean hellSpawnInRange() {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf==null) return false;
        if (pf.getContainingLocation()==null) return false;

        for (SectorEntityToken e : pf.getContainingLocation().getAllEntities()){
            if (e.getMemoryWithoutUpdate()==null) continue;

            if (e.getMemoryWithoutUpdate().contains(hellSpawnAbility.HELL_FLEET_KEY)){
                if (MathUtils.getDistance(e.getLocation(), pf.getLocation()) < JOIN_RANGE){
                    return true;
                }
            }
        }
        return false;
    }
}
