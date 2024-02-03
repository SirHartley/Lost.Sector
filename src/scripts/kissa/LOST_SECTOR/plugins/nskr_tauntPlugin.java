/////////
//based on combat chatter code by Histidine
/////////
package scripts.kissa.LOST_SECTOR.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.util;
import scripts.kissa.LOST_SECTOR.world.systems.cache.nskr_cache;

import java.awt.*;
import java.util.*;
import java.util.List;


public class nskr_tauntPlugin extends BaseEveryFrameCombatPlugin {

    //adds taunt text from boss ships

    private boolean boss = false;
    private boolean checked = false;
    public static final Vector2f ZERO = new Vector2f();

    public static final Set<String> BOSS_KEYS = new HashSet<>();
    static {
        BOSS_KEYS.add(nskr_cache.CACHE_FLEET_KEY);
    }
    public static final ArrayList<String> BOSS_LINES_CACHE = new ArrayList<>();
    static {
        BOSS_LINES_CACHE.add("Puny flesh-ling, prepare for the end.");
        BOSS_LINES_CACHE.add("You are about to discover how there is no god, feeble human.");
        BOSS_LINES_CACHE.add("Face the certainty of steel.");
        BOSS_LINES_CACHE.add("It's no use, just give up.");
        BOSS_LINES_CACHE.add("Burn, scorch, cinder, Scintillate!");
        BOSS_LINES_CACHE.add("This story's coming to a close.");
        BOSS_LINES_CACHE.add("Inferior, substandard, outdated, disposable!");
        BOSS_LINES_CACHE.add("You will be forgotten.");
        BOSS_LINES_CACHE.add("Incapable leader.");
        BOSS_LINES_CACHE.add("Perish for your sins, I will grant you hell.");
        BOSS_LINES_CACHE.add("We are absolute technological superiority.");
        BOSS_LINES_CACHE.add("Not worthy of the power.");
        BOSS_LINES_CACHE.add("Target will be destroyed according to mission perimeters.");
        BOSS_LINES_CACHE.add("Moloch has granted me a purpose.");
        BOSS_LINES_CACHE.add("Witness the devil's toys.");
        BOSS_LINES_CACHE.add("You should retreat, NOW.");
    }
    public static final ArrayList<String> BOSS_LINES_WINNING_CACHE = new ArrayList<>();
    static {
        BOSS_LINES_WINNING_CACHE.add("AHHAHAHAHAHAAH!");
        BOSS_LINES_WINNING_CACHE.add("Pathetic.");
        BOSS_LINES_WINNING_CACHE.add("Look at you, it's sad really.");
        BOSS_LINES_WINNING_CACHE.add("Have it writ upon thy meagre grave : Skill issue.");
        BOSS_LINES_WINNING_CACHE.add("You have to crack some eggs to make an omelette, captain.");
        BOSS_LINES_WINNING_CACHE.add("Is this autofit by chance?");
        BOSS_LINES_WINNING_CACHE.add("This is so easy, it's hilarious.");
        BOSS_LINES_WINNING_CACHE.add("The test says you suck, captain. Wow, I wasn't even testing for that.");
        BOSS_LINES_WINNING_CACHE.add("What a clown show.");
        BOSS_LINES_WINNING_CACHE.add("ha ha ha, this is a laugh. I'm laughing at you.");
    }
    public static final ArrayList<String> BOSS_LINES_LOSING_CACHE = new ArrayList<>();
    static {
        BOSS_LINES_LOSING_CACHE.add("What?! NO!");
        BOSS_LINES_LOSING_CACHE.add("NO, you will not.");
        BOSS_LINES_LOSING_CACHE.add("Impossible.");
        BOSS_LINES_LOSING_CACHE.add("Inconceivable.");
        BOSS_LINES_LOSING_CACHE.add("Noooooooooooooo");
        BOSS_LINES_LOSING_CACHE.add("ERROR #403 ...");
        BOSS_LINES_LOSING_CACHE.add("It's dumb luck!");
        BOSS_LINES_LOSING_CACHE.add("IMPROBABLE");
        BOSS_LINES_LOSING_CACHE.add("NO NO NO NO NO NO");
        BOSS_LINES_LOSING_CACHE.add("It's not over, not yet.");
        BOSS_LINES_LOSING_CACHE.add("AAAARRGGHHHHHHHHH");
    }
    private boolean music= false;

    public nskr_tauntPlugin() {
    }

    static void log(final String message) {
        Global.getLogger(nskr_tauntPlugin.class).info(message);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();

        if (engine.isPaused())return;
        if (Global.getSector()==null)return;
        if (Global.getSector().getPlayerFleet()==null)return;
        BattleAPI battle = Global.getSector().getPlayerFleet().getBattle();
        if (battle == null) return;

        //check only once since this is an EFS
        if (checked && !boss) return;
        //boss fleet check
        CampaignFleetAPI fleet = getFleetFromBattle(battle);
        for (String key : fleet.getMemoryWithoutUpdate().getKeys()){
            if (BOSS_KEYS.contains(key)){
                boss = true;
                break;
            }
        }
        checked = true;
        if(!boss) return;
        //log("NAME "+fleet.getNameWithFaction());
        //log("FACTION "+fleet.getFaction().getId());

        CombatFleetManagerAPI enemy = engine.getFleetManager(FleetSide.ENEMY);
        if (enemy == null) return;
        PersonAPI commander = enemy.getFleetCommander();
        if (commander == null) return;

        //data
        FleetSpecificData data = (FleetSpecificData) Global.getCombatEngine().getCustomData().get("TAUNT_DATA_KEY" + fleet.getId());
        if (data == null) {
            data = new FleetSpecificData();
        }

        List<FleetMemberAPI> deployed = new ArrayList<>(engine.getFleetManager(FleetSide.ENEMY).getDeployedCopy());
        //cache
        if(fleet.getMemoryWithoutUpdate().contains(nskr_cache.CACHE_FLEET_KEY)) {
            Color textColorFirst = new Color(241, 49, 83);
            Color textColorSecond = new Color(241, 197, 208);

            //music
            if (!music && data.musicTimer <= 0f) {
                Global.getSoundPlayer().playCustomMusic(0, 8, "nskr_cache_boss_theme");
                music = true;
            }
            //final boss spawning
            if(!data.finalBoss && canSpawnBoss(deployed) && data.waitTimer <= 0f) {

                enemy.setCanForceShipsToEngageWhenBattleClearlyLost(true);

                Vector2f loc = new Vector2f(0f,0f);
                if (Global.getCombatEngine().getPlayerShip()!=null){
                    loc = MathUtils.getPointOnCircumference(Global.getCombatEngine().getPlayerShip().getLocation(), 1400f, VectorUtils.getAngle(Global.getCombatEngine().getPlayerShip().getLocation(), new Vector2f(0f,0f)));
                    //log("CORDS "+loc.getX()+" "+loc.getY());
                }
                ShipAPI boss = engine.getFleetManager(fleet.getFlagship().getOwner()).spawnShipOrWing(nskr_cache.SECONDARY_VARIANT_1, loc, 270f, 2f, nskr_cache.createSecondaryCaptain(2));
                boss.setName(nskr_cache.S1_NAME);

                Global.getCombatEngine().getCombatUI().addMessage(
                        1, boss, textColorFirst, boss.getCaptain().getName().getFullName(), textColorFirst, ": ", textColorSecond, " No more, this ends now.");
                Global.getCombatEngine().getCombatUI().addMessage(
                        1, Misc.getTextColor(),"unknown ",Misc.getNegativeHighlightColor(),"enemy ",Misc.getTextColor(),"signature detected.");
                Global.getSoundPlayer().playUISound("nskr_prot_warning", 1.0f, 0.8f);

                data.finalBoss = true;
            }
            //taunt
            for (FleetMemberAPI m : validMembersCache(deployed)){
                ShipAPI ship = getShipForMember(m);
                //losing taunt adder
                String messageLosing = BOSS_LINES_LOSING_CACHE.get(MathUtils.getRandomNumberInRange(0, BOSS_LINES_LOSING_CACHE.size() - 1));
                //taunt
                boolean can = canAddLTauntCache(ship, data);
                if (data.losingTimer <= 0f && can && !data.finalBoss) {
                    addLTaunt(deployed,textColorFirst, textColorSecond,messageLosing,data);
                }
            }
            if (validMembersCache(deployed).isEmpty()) return;
            //winning taunt adder
            String messageWinning = BOSS_LINES_WINNING_CACHE.get(MathUtils.getRandomNumberInRange(0, BOSS_LINES_WINNING_CACHE.size() - 1));
            ShipAPI playerShip = engine.getPlayerShip();
            //taunt
            boolean can = canAddWTauntCache(playerShip, data);
            if (data.winningTimer <= 0f && can) {
                addWTaunt(deployed,textColorFirst, textColorSecond,messageWinning,data);
            }
            //random taunt adder
            String messageRandom = BOSS_LINES_CACHE.get(MathUtils.getRandomNumberInRange(0, BOSS_LINES_CACHE.size() - 1));
            //taunt
            if (data.tauntTimer <= 0f) {
                addRandomTaunt(deployed,textColorFirst, textColorSecond,messageRandom,data);
            }
        }

        data.losingTimer -= amount;
        data.winningTimer -= amount;
        data.tauntTimer -= amount;
        data.musicTimer -= amount;
        data.waitTimer -= amount;

        Global.getCombatEngine().getCustomData().put("TAUNT_DATA_KEY" + fleet.getId(), data);
    }

    private static class FleetSpecificData {
        int lostCount = 0;
        int killCount = 0;
        float waitTimer = 2f;
        float tauntTimer = 20f;
        float winningTimer = 0f;
        float losingTimer = 0f;
        float musicTimer = 40f;
        boolean finalBoss = false;
    }

    private void addRandomTaunt(List<FleetMemberAPI> deployed, Color textColorFirst, Color textColorSecond, String messageWinning, FleetSpecificData data){
        FleetMemberAPI memberRandom = pickRandomMemberCache(deployed);
        Global.getCombatEngine().getCombatUI().addMessage(1, memberRandom, textColorFirst, memberRandom.getCaptain().getName().getFullName(), textColorFirst, ": ", textColorSecond, messageWinning);
        Global.getSoundPlayer().playUISound("ui_noise_static", 1f, 1f);
        data.tauntTimer = MathUtils.getRandomNumberInRange(60f, 90f);
    }
    private void addLTaunt(List<FleetMemberAPI> deployed, Color textColorFirst, Color textColorSecond, String messageWinning, FleetSpecificData data){
        FleetMemberAPI memberRandom = pickRandomMemberCache(deployed);
        Global.getCombatEngine().getCombatUI().addMessage(1, memberRandom, textColorFirst, memberRandom.getCaptain().getName().getFullName(), textColorFirst, ": ", textColorSecond, messageWinning);
        Global.getSoundPlayer().playUISound("ui_noise_static", 1f, 1f);
        data.losingTimer = MathUtils.getRandomNumberInRange(15f, 20f);
    }
    private boolean canAddLTauntCache(ShipAPI ship, FleetSpecificData data){
        boolean lTaunt = false;
        if (data.killCount<validDeadMembersCache(getKilledPlayer()).size()){
            data.killCount = validDeadMembersCache(getKilledPlayer()).size();
            lTaunt = true;
            data.losingTimer = 0f;
        }
        if (ship.getFluxTracker()!=null){
            if (ship.getFluxTracker().isOverloaded()){
                lTaunt = true;
            }
        }
        return lTaunt;
    }
    private void addWTaunt(List<FleetMemberAPI> deployed, Color textColorFirst, Color textColorSecond, String messageWinning, FleetSpecificData data){
        FleetMemberAPI memberRandom = pickRandomMemberCache(deployed);
        Global.getCombatEngine().getCombatUI().addMessage(1, memberRandom, textColorFirst, memberRandom.getCaptain().getName().getFullName(), textColorFirst, ": ", textColorSecond, messageWinning);
        Global.getSoundPlayer().playUISound("ui_noise_static", 1f, 1f);
        data.winningTimer = MathUtils.getRandomNumberInRange(15f, 20f);
    }
    private boolean canAddWTauntCache(ShipAPI playerShip, FleetSpecificData data){
        boolean wTaunt = false;
        if (data.lostCount<getLostPlayer().size()){
            wTaunt = true;
            data.lostCount = getLostPlayer().size();
            data.winningTimer = 0f;
        }
        if (playerShip!=null && playerShip.getFluxTracker()!=null){
            if (playerShip.getFluxTracker().isOverloaded()){
                wTaunt = true;
            }
        }
        return wTaunt;
    }
    private List<FleetMemberAPI> validMembersCache(List<FleetMemberAPI> deployed){
        List<FleetMemberAPI> validMembers = new ArrayList<>();
        for (FleetMemberAPI member : deployed) {
            if (member == null) continue;
            if (member.isFighterWing()) continue;
            //boss ship check
            if (!util.isProtTech(member)) continue;
            validMembers.add(member);
        }
        return validMembers;
    }
    private List<FleetMemberAPI> validDeadMembersCache(List<FleetMemberAPI> dead){
        List<FleetMemberAPI> validMembers = new ArrayList<>();
        for (FleetMemberAPI member : dead) {
            if (member == null) continue;
            if (member.isFighterWing()) continue;
            //boss ship check
            if (!util.isProtTech(member)) continue;
            validMembers.add(member);
        }
        return validMembers;
    }
    private FleetMemberAPI pickRandomMemberCache(List<FleetMemberAPI> deployed){
        List<FleetMemberAPI> valid = validMembersCache(deployed);
        return valid.get(MathUtils.getRandomNumberInRange(0, valid.size() - 1));
    }
    private ShipAPI getShipForMember(FleetMemberAPI member)
    {
        ShipAPI ship = Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).getShipFor(member);
        if (ship == null) ship = Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getShipFor(member);
        return ship;
    }
    private CampaignFleetAPI getFleetFromBattle(BattleAPI battle) {
        CampaignFleetAPI bestFallback = null;
        List<Pair<CampaignFleetAPI, Float>> fleetsSorted = new ArrayList<>();
        for (CampaignFleetAPI fleet : battle.getNonPlayerSide()) {
            float strength = fleet.getEffectiveStrength();
            fleetsSorted.add(new Pair<>(fleet, strength));
        }
        Collections.sort(fleetsSorted, FLEET_COMPARE);
        for (Pair<CampaignFleetAPI, Float> entry : fleetsSorted) {
            return entry.one;
        }
        return bestFallback;
    }
    private final Comparator<Pair<CampaignFleetAPI, Float>> FLEET_COMPARE = new Comparator<Pair<CampaignFleetAPI, Float>>() {
        @Override
        public int compare(Pair<CampaignFleetAPI, Float> f1, Pair<CampaignFleetAPI, Float> f2) {
            return Float.compare(f1.two, f2.two);
        }
    };
    private List<FleetMemberAPI> getLostPlayer() {
        return Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).getDisabledCopy();
    }
    private List<FleetMemberAPI> getKilledPlayer() {
        return Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getDisabledCopy();
    }
    private boolean canSpawnBoss(List<FleetMemberAPI> deployed){
        if (validMembersCache(deployed).isEmpty()) return true;
        if (Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getDeployedCopy().size()<=1) return true;
        return false;
    }
}