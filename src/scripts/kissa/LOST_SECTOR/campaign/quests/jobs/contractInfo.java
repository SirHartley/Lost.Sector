package scripts.kissa.LOST_SECTOR.campaign.quests.jobs;

import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;
import scripts.kissa.LOST_SECTOR.util.ids;
import scripts.kissa.LOST_SECTOR.util.mathUtil;
import scripts.kissa.LOST_SECTOR.util.powerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class contractInfo {

    //combat
    public static final int STANDARD_BASE = 50;
    public static final int STANDARD_BASE_REWARD = 2500;
    public static final int FRIGATE_BASE = 25;
    public static final int FRIGATE_BASE_REWARD = 4000;
    public static final int DESTROYER_BASE = 16;
    public static final int DESTROYER_BASE_REWARD = 6000;
    public static final int CRUISER_BASE = 12;
    public static final int CRUISER_BASE_REWARD = 10000;
    public static final int CAPITAL_BASE = 4;
    public static final int CAPITAL_BASE_REWARD = 20000;
    public static final int PHASE_BASE = 8;
    public static final int PHASE_BASE_REWARD = 12000;
    public static final int LOGISTICS_BASE = 20;
    public static final int LOGISTICS_BASE_REWARD = 6000;
    public static final int CARRIER_BASE = 10;
    public static final int CARRIER_BASE_REWARD = 7000;
    public static final int PATHER_BASE = 30;
    public static final int PATHER_BASE_REWARD = 3500;
    public static final int PIRATE_BASE = 35;
    public static final int PIRATE_BASE_REWARD = 2500;
    public static final int REMNANT_BASE = 25;
    public static final int REMNANT_BASE_REWARD = 7000;
    public static final int DERELICT_BASE = 25;
    public static final int DERELICT_BASE_REWARD = 4500;
    public static final int ENIGMA_BASE = 15;
    public static final int ENIGMA_BASE_REWARD = 10000;
    //tahlan
    public static final int LEGIO_BASE = 20;
    public static final int LEGIO_BASE_REWARD = 5000;

    //scav
    public static final int METALS_BASE = 2000;
    public static final int METALS_BASE_REWARD = 30;
    public static final int SUPPLIES_BASE = 1200;
    public static final int SUPPLIES_BASE_REWARD = 75;
    public static final int FUEL_BASE = 2000;
    public static final int FUEL_BASE_REWARD = 40;
    public static final int HEAVY_MACHINERY_BASE = 400;
    public static final int HEAVY_MACHINERY_BASE_REWARD = 150;
    public static final int ARTIFACT_ELECTRONICS_BASE = 200;
    public static final int ARTIFACT_ELECTRONICS_BASE_REWARD = 800;

    //indEvo
    public static final int SHIP_PARTS_BASE = 600;
    public static final int SHIP_PARTS_BASE_REWARD = 35;
    public static final int RARE_PARTS_BASE = 250;
    public static final int RARE_PARTS_BASE_REWARD = 300;

    public static final int GAMMA_BASE = 6;
    public static final int GAMMA_BASE_REWARD = 25000;
    public static final int BETA_BASE = 3;
    public static final int BETA_BASE_REWARD = 50000;
    public static final int ALPHA_BASE = 1;
    public static final int ALPHA_BASE_REWARD = 100000;

    //Tahlan
    public static final int DAEMON_BASE = 2;
    public static final int DAEMON_BASE_REWARD = 60000;
    public static final int ARCHDAEMON_BASE = 1;
    public static final int ARCHDAEMON__REWARD = 120000;

    public contractType type;
    public String subType;

    public int count;
    public int totalReward;
    public int rewardPer;

    public int completedCount = 0;
    public boolean failed = false;
    public boolean isFactionBounty = false;

    private Random random;
    public static final List<Pair<String, Float>> COMBAT_SUBTYPES = new ArrayList<>();
    static {
        COMBAT_SUBTYPES.add(new Pair<>("standard", 22.5f));
        COMBAT_SUBTYPES.add(new Pair<>("frigate", 7.5f));
        COMBAT_SUBTYPES.add(new Pair<>("destroyer", 10f));
        COMBAT_SUBTYPES.add(new Pair<>("cruiser", 7.5f));
        COMBAT_SUBTYPES.add(new Pair<>("capital", 5f));
        COMBAT_SUBTYPES.add(new Pair<>("phase", 5f));
        COMBAT_SUBTYPES.add(new Pair<>("logistics", 5f));
        COMBAT_SUBTYPES.add(new Pair<>("carrier", 5f));
        COMBAT_SUBTYPES.add(new Pair<>(Factions.LUDDIC_PATH, 3f));
        COMBAT_SUBTYPES.add(new Pair<>(Factions.PIRATES, 3f));
        COMBAT_SUBTYPES.add(new Pair<>(Factions.REMNANTS, 3f));
        //COMBAT_SUBTYPES.add(new Pair<>(Factions.DERELICT, 3f));
        COMBAT_SUBTYPES.add(new Pair<>(ids.ENIGMA_FACTION_ID, 3f));
    }
    public static final List<Pair<String, Float>> SCAV_SUBTYPES = new ArrayList<>();
    static {
        SCAV_SUBTYPES.add(new Pair<>("metals", 25f));
        SCAV_SUBTYPES.add(new Pair<>("supplies", 5f));
        SCAV_SUBTYPES.add(new Pair<>("fuel", 5f));
        SCAV_SUBTYPES.add(new Pair<>("heavy_machinery", 15f));
        SCAV_SUBTYPES.add(new Pair<>("artifact_electronics", 7.5f));
        SCAV_SUBTYPES.add(new Pair<>("gamma_core", 5f));
        SCAV_SUBTYPES.add(new Pair<>("beta_core", 2.5f));
        SCAV_SUBTYPES.add(new Pair<>("alpha_core", 1f));
    }

    public contractInfo(contractType type, Random random) {
        this.type = type;
        this.random = random;
        //add from mods
        if (nskr_modPlugin.IS_INDEVO){
            SCAV_SUBTYPES.add(new Pair<>("IndEvo_parts", 15f));
            SCAV_SUBTYPES.add(new Pair<>("IndEvo_rare_parts", 5f));
        }
        if (nskr_modPlugin.IS_TAHLAN){
            SCAV_SUBTYPES.add(new Pair<>("tahlan_daemoncore", 2f));
            SCAV_SUBTYPES.add(new Pair<>("tahlan_archdaemoncore", 1f));
            COMBAT_SUBTYPES.add(new Pair<>("tahlan_legioinfernalis", 3f));
        }

        create();
    }

    public void create(){
        float mult =  (3f*mathUtil.BiasFunction(random.nextDouble(), 0.50f)) * powerLevel.get(0.20f, 0f, 2f);
        float rewardMult = mathUtil.getSeededRandomNumberInRange(0.75f, 1.5f, random);
        float minRandom = 0.50f;
        float maxRandom = 1.50f;

        if (type==contractType.ELIMINATE){
            subType = randomSubType();

            switch (subType) {
                case ("standard"):
                    calc(STANDARD_BASE, minRandom, maxRandom, mult, STANDARD_BASE_REWARD, rewardMult);
                    break;
                case ("frigate"):
                    calc(FRIGATE_BASE, minRandom, maxRandom, mult, FRIGATE_BASE_REWARD, rewardMult);
                    break;
                case ("destroyer"):
                    calc(DESTROYER_BASE, minRandom, maxRandom, mult, DESTROYER_BASE_REWARD, rewardMult);
                    break;
                case ("cruiser"):
                    calc(CRUISER_BASE, minRandom, maxRandom, mult, CRUISER_BASE_REWARD, rewardMult);
                    break;
                case ("capital"):
                    calc(CAPITAL_BASE, minRandom, maxRandom, mult, CAPITAL_BASE_REWARD, rewardMult);
                    break;
                case ("phase"):
                    calc(PHASE_BASE, minRandom, maxRandom, mult, PHASE_BASE_REWARD, rewardMult);
                    break;
                case ("logistics"):
                    calc(LOGISTICS_BASE, minRandom, maxRandom, mult, LOGISTICS_BASE_REWARD, rewardMult);
                    break;
                case ("carrier"):
                    calc(CARRIER_BASE, minRandom, maxRandom, mult, CARRIER_BASE_REWARD, rewardMult);
                    break;
                case (Factions.LUDDIC_PATH):
                    calc(PATHER_BASE, minRandom, maxRandom, mult, PATHER_BASE_REWARD, rewardMult);
                    isFactionBounty = true;
                    break;
                case (Factions.PIRATES):
                    calc(PIRATE_BASE, minRandom, maxRandom, mult, PIRATE_BASE_REWARD, rewardMult);
                    isFactionBounty = true;
                    break;
                case (Factions.REMNANTS):
                    calc(REMNANT_BASE, minRandom, maxRandom, mult, REMNANT_BASE_REWARD, rewardMult);
                    isFactionBounty = true;
                    break;
                case (Factions.DERELICT):
                    calc(DERELICT_BASE, minRandom, maxRandom, mult, DERELICT_BASE_REWARD, rewardMult);
                    isFactionBounty = true;
                    break;
                case (ids.ENIGMA_FACTION_ID):
                    calc(ENIGMA_BASE, minRandom, maxRandom, mult, ENIGMA_BASE_REWARD, rewardMult);
                    isFactionBounty = true;
                    break;
                case ("tahlan_legioinfernalis"):
                    calc(LEGIO_BASE, minRandom, maxRandom, mult, LEGIO_BASE_REWARD, rewardMult);
                    isFactionBounty = true;
                    break;
            }
        } else {
            subType = randomSubType();
            switch (subType){
                case ("metals"):
                    calc(METALS_BASE, minRandom, maxRandom, mult, METALS_BASE_REWARD, rewardMult);
                    break;
                case ("supplies"):
                    calc(SUPPLIES_BASE, minRandom, maxRandom, mult, SUPPLIES_BASE_REWARD, rewardMult);
                    break;
                case ("fuel"):
                    calc(FUEL_BASE, minRandom, maxRandom, mult, FUEL_BASE_REWARD, rewardMult);
                    break;
                case ("heavy_machinery"):
                    calc(HEAVY_MACHINERY_BASE, minRandom, maxRandom, mult, HEAVY_MACHINERY_BASE_REWARD, rewardMult);
                    break;
                case ("artifact_electronics"):
                    calc(ARTIFACT_ELECTRONICS_BASE, minRandom, maxRandom, mult, ARTIFACT_ELECTRONICS_BASE_REWARD, rewardMult);
                    break;
                case ("IndEvo_parts"):
                    calc(SHIP_PARTS_BASE, minRandom, maxRandom, mult, SHIP_PARTS_BASE_REWARD, rewardMult);
                    break;
                case ("IndEvo_rare_parts"):
                    calc(RARE_PARTS_BASE, minRandom, maxRandom, mult, RARE_PARTS_BASE_REWARD, rewardMult);
                    break;
                case ("gamma_core"):
                    calc(GAMMA_BASE, minRandom, maxRandom, mult, GAMMA_BASE_REWARD, rewardMult);
                    break;
                case ("beta_core"):
                    calc(BETA_BASE, minRandom, maxRandom, mult, BETA_BASE_REWARD, rewardMult);
                    break;
                case ("alpha_core"):
                    calc(ALPHA_BASE, minRandom, maxRandom, mult, ALPHA_BASE_REWARD, rewardMult);
                    break;
                case ("tahlan_daemoncore"):
                    calc(DAEMON_BASE, minRandom, maxRandom, mult, DAEMON_BASE_REWARD, rewardMult);
                    break;
                case ("tahlan_archdaemoncore"):
                    calc(ARCHDAEMON_BASE, minRandom, maxRandom, mult, ARCHDAEMON__REWARD, rewardMult);
                    break;
            }
        }

        count = Math.max(count,1);
        rewardPer = Math.max(rewardPer,1);
        //round off
        if (type==contractType.ELIMINATE) {
            rewardPer = Math.round(rewardPer / 50f) * 50;
        } else {
            rewardPer = Math.round(rewardPer / 5f) * 5;
        }
        totalReward = rewardPer*count;
    }

    private void calc(int standardBase, float minRandom, float maxRandom, float mult, int standardBaseReward, float rewardMult) {
        float tempCount = mathUtil.getSeededRandomNumberInRange(standardBase * minRandom, standardBase * maxRandom, random);
        tempCount *= 1f + mult;
        count = Math.round(tempCount);
        rewardPer = Math.round(standardBaseReward * rewardMult);
    }

    public String randomSubType() {
        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
        picker.setRandom(random);
        List<Pair<String, Float>> toPick;
        if (type==contractType.ELIMINATE){
            toPick = COMBAT_SUBTYPES;
        } else {
            toPick = SCAV_SUBTYPES;
        }
        for (Pair<String,Float> s : toPick){
            picker.add(s.one,s.two);
        }
        String role = picker.pick();

        return role;
    }

    public enum contractType {
        ELIMINATE,
        SCAVENGE
    }
}
