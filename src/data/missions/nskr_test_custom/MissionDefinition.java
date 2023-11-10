//////////////////////
//Initially created by DarkRevenant and modified from Ship and Weapon Pack
//////////////////////
package data.missions.nskr_test_custom;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import scripts.kissa.LOST_SECTOR.missions.nskr_BaseRandomBattle;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;

public class MissionDefinition extends nskr_BaseRandomBattle {

    //copy pasted from SWP
    private static int enemyAdvantage;
    private static String enemyFaction;
    private static int enemyFactionIndex;
    private static String enemyFleet;
    private static double enemyOPBonus;
    private static int enemyQualityFactor;
    private static long enemySeed = 0L;
    private static int enemySMods;

    private static FleetSide escape;
    private static boolean first = true;

    private static String playerFaction;
    private static int playerFactionIndex;
    private static String playerFleet;
    private static double playerOPBonus;
    private static int playerQualityFactor;
    private static long playerSeed = 0L;
    private static int playerSMods;

    private static int size;
    private static float width;
    private static float height;
    private static int objectiveCount;
    private static boolean boostTime;
    private static boolean autoshit;

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        super.defineMission(api);

        boolean refreshPlayer = false;
        boolean refreshEnemy = false;

        if (first || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
            first = false;
            playerFaction = FACTIONS.pick();
            playerFactionIndex = FACTIONS.getItems().indexOf(playerFaction);
            enemyFaction = FACTIONS.pick();
            enemyFactionIndex = FACTIONS.getItems().indexOf(enemyFaction);
            double r = Math.random();
            if (r < 1.0) {
                escape = null;
            } else if (r < 0.9) {
                escape = FleetSide.PLAYER;
            } else {
                escape = FleetSide.ENEMY;
            }
            size = 5 + (int) ((float) Math.random() * 55);
            enemyAdvantage = 100;
            playerQualityFactor = 100;
            enemyQualityFactor = 100;
            playerOPBonus = 0.0;
            enemyOPBonus = 0.0;
            playerSMods = -1;
            enemySMods = -1;
            objectiveCount = (int) Math.floor(size * ((float) Math.random() * 0.75f + 0.5f) / 8f);
            if (escape != null) {
                width = (12000f + 10000f * (size / 40f)) * ((float) Math.random() * 0.6f + 0.4f);
                height = (12000f + 10000f * (size / 40f));
            } else {
                width = (12000f + 10000f * (size / 40f)) * ((float) Math.random() * 0.4f + 0.6f);
                height = (12000f + 10000f * (size / 40f)) * ((float) Math.random() * 0.4f + 0.6f);
            }
            width = (int) (width / 500f) * 500;
            height = (int) (height / 500f) * 500;
            boostTime = false;
            autoshit = false;
            refreshPlayer = true;
            refreshEnemy = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            refreshPlayer = true;
            refreshEnemy = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            playerFactionIndex = (playerFactionIndex + 1) % FACTIONS.getItems().size();
            playerFaction = FACTIONS.getItems().get(playerFactionIndex);
            refreshPlayer = true;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
            enemyFactionIndex = (enemyFactionIndex + 1) % FACTIONS.getItems().size();
            enemyFaction = FACTIONS.getItems().get(enemyFactionIndex);
            refreshEnemy = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_M)) {
            if (null == escape) {
                escape = FleetSide.PLAYER;
            } else {
                switch (escape) {
                    case PLAYER:
                        escape = FleetSide.ENEMY;
                        break;
                    default:
                        escape = null;
                        break;
                }
            }
            refreshPlayer = true;
            refreshEnemy = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            size = Math.max(size - 1, 1);
            refreshPlayer = true;
            refreshEnemy = true;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            size = Math.min(size + 1, 250);
            refreshPlayer = true;
            refreshEnemy = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
            enemyAdvantage = Math.max(enemyAdvantage - 5, 5);
            refreshEnemy = true;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
            enemyAdvantage = Math.min(enemyAdvantage + 5, 1000);
            refreshEnemy = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
            playerQualityFactor = Math.max(playerQualityFactor - 5, -50);
            refreshPlayer = true;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
            playerQualityFactor = Math.min(playerQualityFactor + 5, 150);
            refreshPlayer = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            enemyQualityFactor = Math.max(enemyQualityFactor - 5, -50);
            refreshEnemy = true;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
            enemyQualityFactor = Math.min(enemyQualityFactor + 5, 150);
            refreshEnemy = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_Y)) {
            playerOPBonus = Math.round(Math.max(playerOPBonus - 1, 0.0));
            refreshPlayer = true;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_U)) {
            playerOPBonus = Math.round(Math.min(playerOPBonus + 1, 50.0));
            refreshPlayer = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_H)) {
            enemyOPBonus = Math.round(Math.max(enemyOPBonus - 1, 0.0));
            refreshEnemy = true;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
            enemyOPBonus = Math.round(Math.min(enemyOPBonus + 1, 50.0));
            refreshEnemy = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LBRACKET)) {
            playerSMods++;
            if (playerSMods > 4) {
                playerSMods = -1;
            }
            refreshPlayer = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_RBRACKET)) {
            enemySMods++;
            if (enemySMods > 4) {
                enemySMods = -1;
            }
            refreshEnemy = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
            objectiveCount = Math.max(objectiveCount - 1, 0);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            objectiveCount = Math.min(objectiveCount + 1, 10);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
            width = Math.round(Math.max(width - 500f, 1000f) / 500f) * 500;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_X)) {
            width = Math.round(Math.min(width + 500f, 100000f) / 500f) * 500;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
            height = Math.round(Math.max(height - 500f, 1000f) / 500f) * 500;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_V)) {
            height = Math.round(Math.min(height + 500f, 100000f) / 500f) * 500;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_T)) {
            boostTime = !boostTime;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_GRAVE)) {
            autoshit = !autoshit;
            refreshPlayer = true;
            refreshEnemy = true;
        }

        if (null == escape) {
            api.addBriefingItem("Defeat all enemy forces");
        } else {
            switch (escape) {
                case PLAYER:
                    api.addBriefingItem("Escape from the enemy forces");
                    break;
                case ENEMY:
                default:
                    api.addBriefingItem("Prevent the enemy forces from escaping");
                    break;
            }
        }

        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        if (escape == FleetSide.PLAYER) {
            api.initFleet(FleetSide.PLAYER, Global.getSector().getFaction(playerFaction).getEntityNamePrefix(), FleetGoal.ESCAPE, false, size / 8);
        } else {
            api.initFleet(FleetSide.PLAYER, Global.getSector().getFaction(playerFaction).getEntityNamePrefix(), FleetGoal.ATTACK, false, size / 8);
        }
        if (escape == FleetSide.ENEMY) {
            api.initFleet(FleetSide.ENEMY, Global.getSector().getFaction(enemyFaction).getEntityNamePrefix(), FleetGoal.ESCAPE, true, size / 8);
        } else {
            api.initFleet(FleetSide.ENEMY, Global.getSector().getFaction(enemyFaction).getEntityNamePrefix(), FleetGoal.ATTACK, true, size / 8);
        }

        switch (objectiveCount) {
            case 0:
                api.addBriefingItem("Battle size: " + size + "  -  " + (int) width + "x" + (int) height);
                break;
            case 1:
                api.addBriefingItem("Battle size: " + size + "  -  " + objectiveCount + " objective" + "  -  " + (int) width + "x" + (int) height);
                break;
            default:
                api.addBriefingItem("Battle size: " + size + "  -  " + objectiveCount + " objectives" + "  -  " + (int) width + "x" + (int) height);
                break;
        }

        if (refreshPlayer || refreshEnemy) {
            WeightedRandomPicker<String> fleetTypes = new WeightedRandomPicker<>();
            if (escape == null) {
                if (size < 20) {
                    fleetTypes.add("raiders", 1f);
                }
                if (size < 30) {
                    fleetTypes.add("patrol fleet", 1f);
                }
                if (size < 30 && size >= 15) {
                    fleetTypes.add("hunter-killers", 1f);
                }
                if (size >= 25) {
                    fleetTypes.add("defense fleet", 2f);
                }
                if (size >= 30) {
                    fleetTypes.add("war fleet", 2f);
                }
            } else {
                if (size < 30) {
                    fleetTypes.add("convoy", 2f);
                }
                if (size >= 15 && size < 45) {
                    fleetTypes.add("blockade-runners", 1f);
                }
                if (size >= 30) {
                    fleetTypes.add("invasion fleet", 1f);
                }
            }

            if (refreshPlayer) {
                playerFleet = fleetTypes.pick();
            }
            if (refreshEnemy) {
                enemyFleet = fleetTypes.pick();
            }

            if (escape == FleetSide.PLAYER) {
                if (refreshEnemy) {
                    WeightedRandomPicker<String> enemyFleetTypes = new WeightedRandomPicker<>();
                    if (playerFleet.contentEquals("convoy")) {
                        if (size < 20) {
                            enemyFleetTypes.add("raiders", 1f);
                        }
                        if (size < 30) {
                            enemyFleetTypes.add("patrol fleet", 1f);
                        }
                        if (size < 30 && size >= 15) {
                            enemyFleetTypes.add("hunter-killers", 1f);
                        }
                        if (size >= 25) {
                            enemyFleetTypes.add("war fleet", 2f);
                        }
                    }
                    if (playerFleet.contentEquals("blockade-runners")) {
                        if (size < 30) {
                            enemyFleetTypes.add("patrol fleet", 1f);
                        }
                        if (size >= 25) {
                            enemyFleetTypes.add("defense fleet", 2f);
                        }
                    }
                    if (playerFleet.contentEquals("invasion fleet")) {
                        enemyFleetTypes.add("defense fleet", 2f);
                    }
                    enemyFleet = enemyFleetTypes.pick();
                }
            } else if (escape == FleetSide.ENEMY) {
                if (refreshPlayer) {
                    WeightedRandomPicker<String> playerFleetTypes = new WeightedRandomPicker<>();
                    if (enemyFleet.contentEquals("convoy")) {
                        if (size < 20) {
                            playerFleetTypes.add("raiders", 1f);
                        }
                        if (size < 30) {
                            playerFleetTypes.add("patrol fleet", 1f);
                        }
                        if (size < 30 && size >= 15) {
                            playerFleetTypes.add("hunter-killers", 1f);
                        }
                        if (size >= 25) {
                            playerFleetTypes.add("war fleet", 2f);
                        }
                    }
                    if (enemyFleet.contentEquals("blockade-runners")) {
                        if (size < 30) {
                            playerFleetTypes.add("patrol fleet", 1f);
                        }
                        if (size >= 25) {
                            playerFleetTypes.add("defense fleet", 2f);
                        }
                    }
                    if (enemyFleet.contentEquals("invasion fleet")) {
                        playerFleetTypes.add("defense fleet", 2f);
                    }
                    playerFleet = playerFleetTypes.pick();
                }
            }
        }

        int playerSize = (int) (size * 5f * (escape == FleetSide.PLAYER ? 0.5f : 1f));
        int enemySize = (int) (size * 5f * (escape == FleetSide.ENEMY ? 0.5f : 1f) * (enemyAdvantage / 100f));

        if (refreshPlayer) {
            playerSeed = rand.nextLong();
        }
        if (refreshEnemy) {
            enemySeed = rand.nextLong();
        }

        FleetDataAPI playerFleetData = generateFleet(playerSize, (float) playerQualityFactor / 100f, (float) playerOPBonus, playerSMods, FleetSide.PLAYER, playerFaction, playerFleet, api, playerSeed, autoshit);
        FleetDataAPI enemyFleetData = generateFleet(enemySize, (float) enemyQualityFactor / 100f, (float) enemyOPBonus, enemySMods, FleetSide.ENEMY, enemyFaction, enemyFleet, api, enemySeed, autoshit);

        String playerSModString = "";
        if (playerSMods >= 0) {
            switch (playerSMods) {
                case 0:
                    playerSModString = "0-1";
                    break;
                case 1:
                    playerSModString = "0-2";
                    break;
                case 2:
                    playerSModString = "1-3";
                    break;
                case 3:
                    playerSModString = "2-3";
                    break;
                case 4:
                    playerSModString = "3";
                    break;
                default:
                    break;
            }
        }
        String enemySModString = "";
        if (enemySMods >= 0) {
            switch (enemySMods) {
                case 0:
                    enemySModString = "0-1";
                    break;
                case 1:
                    enemySModString = "0-2";
                    break;
                case 2:
                    enemySModString = "1-3";
                    break;
                case 3:
                    enemySModString = "2-3";
                    break;
                case 4:
                    enemySModString = "3";
                    break;
                default:
                    break;
            }
        }

        if (playerOPBonus > 0.0) {
            if (playerSMods >= 0) {
                api.setFleetTagline(FleetSide.PLAYER, Global.getSector().getFaction(playerFaction).getDisplayName() + " " + playerFleet
                        + " (" + playerSize + " points)"
                        + " (QF " + String.format("%.2f", playerQualityFactor / 100f) + ")"
                        + " (" + String.format("%.0f%%", playerOPBonus + 100.0) + " OP, " + playerSModString + " s-mods)");
            } else {
                api.setFleetTagline(FleetSide.PLAYER, Global.getSector().getFaction(playerFaction).getDisplayName() + " " + playerFleet
                        + " (" + playerSize + " points)"
                        + " (QF " + String.format("%.2f", playerQualityFactor / 100f) + ")"
                        + " (" + String.format("%.0f%%", playerOPBonus + 100.0) + " OP)");
            }
        } else {
            if (playerSMods >= 0) {
                api.setFleetTagline(FleetSide.PLAYER, Global.getSector().getFaction(playerFaction).getDisplayName() + " " + playerFleet
                        + " (" + playerSize + " points)"
                        + " (QF " + String.format("%.2f", playerQualityFactor / 100f) + ")"
                        + " (" + playerSModString + " s-mods)");
            } else {
                api.setFleetTagline(FleetSide.PLAYER, Global.getSector().getFaction(playerFaction).getDisplayName() + " " + playerFleet
                        + " (" + playerSize + " points)"
                        + " (QF " + String.format("%.2f", playerQualityFactor / 100f) + ")");
            }
        }
        if (enemyOPBonus > 0.0) {
            if (enemySMods >= 0) {
                api.setFleetTagline(FleetSide.ENEMY, Global.getSector().getFaction(enemyFaction).getDisplayName() + " " + enemyFleet
                        + " (" + enemySize + " points)"
                        + " (QF " + String.format("%.2f", enemyQualityFactor / 100f) + ")"
                        + " (" + String.format("%.0f%%", enemyOPBonus + 100.0) + " OP, " + enemySModString + " s-mods)");
            } else {
                api.setFleetTagline(FleetSide.ENEMY, Global.getSector().getFaction(enemyFaction).getDisplayName() + " " + enemyFleet
                        + " (" + enemySize + " points)"
                        + " (QF " + String.format("%.2f", enemyQualityFactor / 100f) + ")"
                        + " (" + String.format("%.0f%%", enemyOPBonus + 100.0) + " OP)");
            }
        } else {
            if (enemySMods >= 0) {
                api.setFleetTagline(FleetSide.ENEMY, Global.getSector().getFaction(enemyFaction).getDisplayName() + " " + enemyFleet
                        + " (" + enemySize + " points)"
                        + " (QF " + String.format("%.2f", enemyQualityFactor / 100f) + ")"
                        + " (" + enemySModString + " s-mods)");
            } else {
                api.setFleetTagline(FleetSide.ENEMY, Global.getSector().getFaction(enemyFaction).getDisplayName() + " " + enemyFleet
                        + " (" + enemySize + " points)"
                        + " (QF " + String.format("%.2f", enemyQualityFactor / 100f) + ")");
            }
        }

        float friendlyDP = 0f;
        float friendlyFP = 0f;
        for (FleetMemberAPI member : playerFleetData.getMembersListCopy()) {
            friendlyDP += member.getDeploymentPointsCost();
            friendlyFP += member.getFleetPointCost();
        }

        float enemyDP = 0f;
        float enemyFP = 0f;
        for (FleetMemberAPI member : enemyFleetData.getMembersListCopy()) {
            enemyDP += member.getDeploymentPointsCost();
            enemyFP += member.getFleetPointCost();
        }

        float distance = Math.abs(enemyDP - friendlyDP) + Math.abs(enemyFP - friendlyFP);

        api.addBriefingItem("Match inequality: " + Math.round(distance));

        if (boostTime) {
            api.addBriefingItem("Time acceleration applied!");
        }

        if (autoshit) {
            api.addBriefingItem("Crappy autofit enabled!");
        }

        double r;
        int objectives = objectiveCount;
        while (objectives > 0) {
            String type = OBJECTIVE_TYPES[rand.nextInt(OBJECTIVE_TYPES.length)];
            int configuration;
            r = Math.random();
            if (r < 0.5) {
                configuration = 0;
            } else if (r < 0.75) {
                configuration = 1;
            } else {
                configuration = 2;
            }

            if (objectives == 1) {
                r = Math.random();
                if (r < 0.75) {
                    api.addObjective(0f, 0f, type);
                } else if (r < 0.875) {
                    float x = (width * 0.075f + width * 0.3f * (float) Math.random()) * (Math.random() > 0.5 ? 1f : -1f);
                    api.addObjective(x, 0f, type);
                } else {
                    float y = (height * 0.075f + height * 0.3f * (float) Math.random()) * (Math.random() > 0.5 ? 1f : -1f);
                    api.addObjective(0f, y, type);
                }

                objectives -= 1;
            } else {
                float x1, x2, y1, y2;
                r = Math.random();
                if ((r < 0.75 && configuration == 0) || (r < 0.5 && configuration == 1) || (r < 0.25 && configuration == 2)) {
                    float theta = (float) (Math.random() * Math.PI);
                    double radius = Math.min(width, height);
                    radius = radius * 0.1 + radius * 0.3 * Math.random();
                    x1 = (float) (Math.cos(theta) * radius);
                    y1 = (float) -(Math.sin(theta) * radius);
                    x2 = -x1;
                    y2 = -y1;
                } else if ((r < 0.875 && configuration == 0) || (r < 0.75 && configuration == 1) || (r < 0.625 && configuration == 2)) {
                    x1 = (width * 0.075f + width * 0.3f * (float) Math.random()) * (Math.random() > 0.5 ? 1f : -1f);
                    x2 = x1;
                    y1 = (height * 0.075f + height * 0.3f * (float) Math.random()) * (Math.random() > 0.5 ? 1f : -1f);
                    y2 = -y1;
                } else {
                    x1 = (width * 0.075f + width * 0.3f * (float) Math.random()) * (Math.random() > 0.5 ? 1f : -1f);
                    x2 = -x1;
                    y1 = (height * 0.075f + height * 0.3f * (float) Math.random()) * (Math.random() > 0.5 ? 1f : -1f);
                    y2 = y1;
                }

                r = Math.random();
                if (r < 0.75) {
                    api.addObjective(x1, y1, type);
                    api.addObjective(x2, y2, type);
                } else {
                    api.addObjective(x1, y1, type);
                    type = OBJECTIVE_TYPES[rand.nextInt(OBJECTIVE_TYPES.length)];
                    api.addObjective(x2, y2, type);
                }

                objectives -= 2;
            }
        }

        if (escape != null) {
            BattleCreationContext context = new BattleCreationContext(null, null, null, null);
            if (playerFleet.contentEquals("convoy") || enemyFleet.contentEquals("convoy")) {
                context.setInitialEscapeRange(height / 5f);
                api.addPlugin(new EscapeRevealPlugin(context));
                api.addPlugin(new Plugin(height / 3.5f));
            } else {
                context.setInitialEscapeRange(height / 8f);
                api.addPlugin(new EscapeRevealPlugin(context));
                api.addPlugin(new Plugin(height / 6f));
            }
        }

        int numNebula = 15;
        boolean inNebula = Math.random() > 0.5;
        if (inNebula) {
            numNebula = 100;
        }

        for (int i = 0; i < numNebula; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            if (inNebula) {
                radius += 100f + 500f * (float) Math.random();
            }
            api.addNebula(x, y, radius);
        }

        float numAsteroidsWithinRange = Math.max(0f, MathUtils.getRandomNumberInRange(-10f, 20f));
        int numAsteroids = Math.min(400, (int) ((numAsteroidsWithinRange + 1f) * 20f));

        api.addAsteroidField(0, 0, (float) Math.random() * 360f, width, 20f, 70f, numAsteroids);

        int numRings = 0;
        if (Math.random() > 0.75) {
            numRings++;
        }
        if (Math.random() > 0.75) {
            numRings++;
        }
        if (numRings > 0) {
            int numRingAsteroids = (int) (numRings * 300 + (numRings * 600f) * (float) Math.random());
            if (numRingAsteroids > 1500) {
                numRingAsteroids = 1500;
            }
            api.addRingAsteroids(0, 0, (float) Math.random() * 360f, width, 100f, 200f, numRingAsteroids);
        }

        String planet = PLANETS.get((int) (Math.random() * PLANETS.size()));
        float radius = 25f + (float) Math.random() * (float) Math.random() * 500f;

        api.addPlanet(0, 0, radius, planet, 0f, true);
        if (planet.contentEquals("wormholeUnder")) {
            api.addPlanet(0, 0, radius, "wormholeA", 0f, true);
            api.addPlanet(0, 0, radius, "wormholeB", 0f, true);
            api.addPlanet(0, 0, radius, "wormholeC", 0f, true);
        }

        if (escape == null) {
            api.getContext().aiRetreatAllowed = false;
        }
        api.getContext().enemyDeployAll = true;
        api.getContext().fightToTheLast = true;

        if (boostTime) {
            api.addPlugin(new BaseEveryFrameCombatPlugin() {
                @Override
                public void init(CombatEngineAPI engine) {
                    engine.getContext().aiRetreatAllowed = false;
                    engine.getContext().enemyDeployAll = true;
                    engine.getContext().fightToTheLast = true;
                }

                @Override
                public void advance(float amount, List<InputEventAPI> events) {
                    if (Global.getCombatEngine().isPaused()) {
                        return;
                    }

                    float trueFrameTime = Global.getCombatEngine().getElapsedInLastFrame();
                    float trueFPS = 1 / trueFrameTime;
                    float newTimeMult = Math.max(1f, trueFPS / 30f);
                    Global.getCombatEngine().getTimeMult().modifyMult("nskr_tester", newTimeMult);
                }
            });
        }
    }
}
