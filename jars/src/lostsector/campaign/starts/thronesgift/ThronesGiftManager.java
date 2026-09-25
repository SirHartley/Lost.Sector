package lostsector.campaign.starts.thronesgift;

import lostsector.campaign.starts.GameModeManager;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import lostsector.helper.FleetHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ThronesGiftManager extends BaseCampaignEventListener implements EveryFrameScript {

    public static final float DEFAULT_DP = 20f;
    public static final float XP_PER_UNLOCK = 1000000f;
    public static final float DP_PER_UNLOCK = 10f;
    public static final String XP_KEY = "thronesGiftManagerXp";
    public static final String DP_KEY = "thronesGiftManagerDp";
    public static final String TOTAL_DP_KEY = "thronesGiftManagerTotalDp";

    private long xp = 0;
    private long oldXp = 0;
    private long lvl = 0;

    public ThronesGiftManager() {
        super(false);
        xp = Global.getSector().getPlayerStats().getXP();
        oldXp = xp;
        lvl = Global.getSector().getPlayerStats().getLevel();
    }

    @Override
    public boolean isDone() {
        return false;
    }

    // XP gain has no callback: CharacterStats.addXP only changes the stored XP and levels up, notifying no listener.
    // XP is often granted in dialogs, which pause the campaign, so the comparison also runs while paused and the
    // points do not wait for the player to unpause.
    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {

        if (GameModeManager.getMode() != GameModeManager.GameMode.THRONESGIFT) return;

        xp = Global.getSector().getPlayerStats().getXP();
        lvl = Global.getSector().getPlayerStats().getLevel();
        if (xp>oldXp){
            reportXpChanged(xp-oldXp);
        }
        //wrap around xp once you hit max lvl, 4x1 million xp per wrap
        if (xp<oldXp && lvl==15) {
            reportXpChanged((4000000-oldXp) + xp);
        }

        oldXp = xp;
    }

    @Override
    public void reportPlayerReputationChange(String faction, float delta) {

        if (GameModeManager.getMode() != GameModeManager.GameMode.THRONESGIFT) return;

        if (faction.equals(Factions.LUDDIC_PATH)) {
            if (Global.getSector().getPlayerFaction().getRelationship(faction) > -0.80f) {
                Global.getSector().getPlayerFaction().setRelationship(faction, -0.80f);
            }
        }

    }

    private void reportXpChanged(float delta) {

        float xp = getXpGained()+delta;
        setXpGained(xp);

        while (getXpGained()>=XP_PER_UNLOCK){
            float dp = getDpAvailable();
            dp += DP_PER_UNLOCK;
            setDpAvailable(dp);
            //total counter
            setTotalDp(getTotalDp() + DP_PER_UNLOCK);

            setXpGained(xp-XP_PER_UNLOCK);


            Global.getSector().getCampaignUI().addMessage("Gained "+(int)DP_PER_UNLOCK+" automation points.",
                    Global.getSettings().getColor("standardTextColor"),
                    (int)DP_PER_UNLOCK+"",
                    "",
                    Global.getSettings().getColor("yellowTextColor"),
                    Global.getSettings().getColor("yellowTextColor"));
            //update for loop
            xp = getXpGained();
        }

    }

    public static void setXpGained(float gained){

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(XP_KEY, gained);

    }

    public static float getXpGained(){

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (data.containsKey(XP_KEY)){
            return (float) data.get(XP_KEY);
        } else {
            data.put(XP_KEY, 0f);
            return (float) data.get(XP_KEY);
        }

    }

    public static void setDpAvailable(float dpAvailable){

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(DP_KEY, dpAvailable);

    }

    public static float getDpAvailable(){

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (data.containsKey(DP_KEY)){
            return (float) data.get(DP_KEY);
        } else {
            data.put(DP_KEY, DEFAULT_DP);
            return (float) data.get(DP_KEY);
        }

    }

    public static void setTotalDp(float dp){

        Map<String, Object> data = Global.getSector().getPersistentData();
        data.put(TOTAL_DP_KEY, dp);

    }

    public static float getTotalDp(){

        Map<String, Object> data = Global.getSector().getPersistentData();
        if (data.containsKey(TOTAL_DP_KEY)){
            return (float) data.get(TOTAL_DP_KEY);
        } else {
            data.put(TOTAL_DP_KEY, DEFAULT_DP);
            return (float) data.get(TOTAL_DP_KEY);
        }

    }

    // Automation

    public static List<FleetMemberAPI> getAutomatableShips() {
        List<FleetMemberAPI> validShips = new ArrayList<>();
        for (FleetMemberAPI f : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()){
            if (f.isFighterWing())continue;
            if (f.getVariant()==null)continue;
            if (f.getVariant().getHullMods().contains(HullMods.AUTOMATED) || f.getVariant().getHullMods().contains("sotf_sierrasconcord"))continue;
            validShips.add(f);
        }
        //can't automate last ship
        if (validShips.size()==1) return new ArrayList<>();

        return validShips;
    }

    public static float getAutomationCost(FleetMemberAPI member) {
        return member.getHullSpec().getSuppliesToRecover();
    }

    public static void automate(FleetMemberAPI member) {
        if (member.getCaptain()!=null){
            member.setCaptain(null);
        }
        //non-automated fighters go back to cargo
        int x = -1;
        for (String s : member.getVariant().getNonBuiltInWings()) {
            x++;
            FighterWingSpecAPI wing = member.getVariant().getWing(x);
            if (wing == null) continue;
            if (!wing.hasTag(Tags.AUTOMATED_FIGHTER)){
                Global.getSector().getPlayerFleet().getCargo().addItems(CargoAPI.CargoItemType.FIGHTER_CHIP, wing.getId(), 1);

                member.getVariant().setWingId(x, null);
            }
        }

        member.getVariant().addPermaMod(HullMods.AUTOMATED);
        member.getVariant().addTag(Tags.TAG_AUTOMATED_NO_PENALTY);

        //update the fleet IMPORTANT
        FleetHelper.updatePlayerFleet(true);

        setDpAvailable(getDpAvailable() - getAutomationCost(member));
    }

}
