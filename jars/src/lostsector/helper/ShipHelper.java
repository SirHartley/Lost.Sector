package lostsector.helper;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Skills;

import java.util.*;

public class ShipHelper {

    static void log(final String message) {
        Global.getLogger(ShipHelper.class).info(message);
    }

    // Identify prototype and Enigma ships by hullmod, because fleet members lose their tags on save and reload.
    public static boolean isProtTech(FleetMemberAPI member){
        if (member.getVariant()==null) return false;
        if (member.getVariant().getHullMods()==null || member.getVariant().getHullMods().isEmpty())  return false;
        boolean prot = false;
        for (String m : member.getVariant().getHullMods()){
            if (m.equals("nskr_focused_shield") || m.equals("nskr_kaboom")){
                prot = true;
                break;
            }
        }
        return prot;
    }

    public static boolean isProtTech(ShipAPI ship){
        if (ship.getVariant()==null) return false;
        if (ship.getVariant().getHullMods()==null || ship.getVariant().getHullMods().isEmpty())  return false;
        boolean prot = false;
        for (String m : ship.getVariant().getHullMods()){
            if (m.equals("nskr_focused_shield") || m.equals("nskr_kaboom")){
                prot = true;
                break;
            }
        }
        return prot;
    }

    public static String protOrEnigma(ShipAPI ship){
        if (ship.getVariant()==null) return null;
        if (ship.getVariant().getHullMods()==null || ship.getVariant().getHullMods().isEmpty())  return null;
        //prot
        for (String m : ship.getVariant().getHullMods()){
            if (m.equals("nskr_lost_prot")){
                return "prot";
            }
        }
        //enigma
        for (String m : ship.getVariant().getHullMods()){
            if (m.equals("nskr_domain_era")){
                return "enigma";
            }
        }
        return null;
    }

    public static String protOrEnigma(FleetMemberAPI member){
        if (member.getVariant()==null) return null;
        if (member.getVariant().getHullMods()==null || member.getVariant().getHullMods().isEmpty())  return null;
        //prot
        for (String m : member.getVariant().getHullMods()){
            if (m.equals("nskr_lost_prot")){
                return "prot";
            }
        }
        //enigma
        for (String m : member.getVariant().getHullMods()){
            if (m.equals("nskr_domain_era")){
                return "enigma";
            }
        }
        return null;
    }

    public static boolean hasCCBonus(){
        boolean CC = false;

        CampaignFleetAPI fleet =  Global.getSector().getPlayerFleet();
        if (fleet==null) return false;

        //fleet check
        for (FleetMemberAPI ship : fleet.getFleetData().getMembersListCopy()){
            Collection<String> mods = ship.getVariant().getHullMods();
            for (String mod : mods){
                if (mod.equals("CHM_kesteven")){
                    CC = true;
                    break;
                }
            }
            if (CC) break;
        }
        return CC;
    }

    public static float getDMods(ShipVariantAPI v){
       return DModManager.getNumDMods(v);
    }

    public static boolean isLogistics(EnumSet<ShipHullSpecAPI.ShipTypeHints> hints){
        boolean logi = false;
        for (ShipHullSpecAPI.ShipTypeHints hint : hints){
            if (hint == ShipHullSpecAPI.ShipTypeHints.CIVILIAN){
                logi = true;
                break;
            }
            if (hint == ShipHullSpecAPI.ShipTypeHints.FREIGHTER){
                logi = true;
                break;
            }
            if (hint == ShipHullSpecAPI.ShipTypeHints.TANKER){
                logi = true;
                break;
            }
            if (hint == ShipHullSpecAPI.ShipTypeHints.LINER){
                logi = true;
                break;
            }
            if (hint == ShipHullSpecAPI.ShipTypeHints.TRANSPORT){
                logi = true;
                break;
            }

        }
        return logi;
    }

    public static PersonAPI setOfficerSkills(PersonAPI officer, Map<String, Integer> skills){

        //reset
        List<MutableCharacterStatsAPI.SkillLevelAPI> ogSkills = officer.getStats().getSkillsCopy();
        for (MutableCharacterStatsAPI.SkillLevelAPI s : ogSkills){
            officer.getStats().setSkillLevel(s.getSkill().getId(), 0.0f);
        }
        //set
        for (String s : skills.keySet()) {
            officer.getStats().setSkillLevel(s, (float)skills.get(s));
        }

        officer.getStats().setLevel(skills.size());
        officer.getStats().refreshCharacterStatsEffects();

        return officer;
    }

    public static Map<String, Integer> createRandomSkills(int level, float eliteSkillChance, Random random){
        Set<String> allSkills = new HashSet<>();
        allSkills.add(Skills.POLARIZED_ARMOR);
        allSkills.add(Skills.ENERGY_WEAPON_MASTERY);
        allSkills.add(Skills.DAMAGE_CONTROL);
        allSkills.add(Skills.HELMSMANSHIP);
        allSkills.add(Skills.BALLISTIC_MASTERY);
        allSkills.add(Skills.ORDNANCE_EXPERTISE);
        allSkills.add(Skills.IMPACT_MITIGATION);
        allSkills.add(Skills.GUNNERY_IMPLANTS);
        allSkills.add(Skills.TARGET_ANALYSIS);
        allSkills.add(Skills.SYSTEMS_EXPERTISE);
        allSkills.add(Skills.MISSILE_SPECIALIZATION);
        allSkills.add(Skills.COMBAT_ENDURANCE);
        allSkills.add(Skills.FIELD_MODULATION);
        allSkills.add(Skills.POINT_DEFENSE);

        Map<String, Integer> skills = new HashMap<>();
        while (skills.size()<level) {
            float size = allSkills.size();
            for (String s : allSkills) {
                if (!skills.containsKey(s) && random.nextFloat()<1f/size){
                    skills.put(s, getEliteSkillChance(random, eliteSkillChance));
                    log("MiscLS added skill "+s);
                }
            }
        }
        return skills;
    }

    public static int getEliteSkillChance(Random random, float chance){
        if (random.nextFloat()<chance) return 1;
        return 2;
    }

    public static float getLinearMod(ShipAPI ship){
        return getLinearMod(ship, 1f);
    }

    public static float getLinearMod(ShipAPI ship, float mult){
        float mod = 1f;
        switch (ship.getHullSize()){
            case FIGHTER:
                mod = 0.5f;
                break;
            case FRIGATE:
                mod = 1.0f;
                break;
            case DESTROYER:
                mod = 2.0f;
                break;
            case CRUISER:
                mod = 4.0f;
                break;
            case CAPITAL_SHIP:
                mod = 8.0f;
                break;
        }
        return mod * mult;
    }
}
