package scripts.kissa.LOST_SECTOR.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import exerelin.campaign.SectorManager;
import org.lazywizard.lazylib.MathUtils;
import scripts.kissa.LOST_SECTOR.nskr_modPlugin;

import java.util.List;

public class nskr_gen implements SectorGeneratorPlugin {

    static void log(final String message) {
        Global.getLogger(nskr_gen.class).info(message);
    }

    @Override
    public void generate(SectorAPI sector) {
    }

    public static void setEnigmaRelation(SectorAPI sector) {
        FactionAPI enigma = sector.getFaction("enigma");

        //default relation
        List <FactionAPI> allFactions = sector.getAllFactions();
        for (FactionAPI f : allFactions) {
            if (!SharedData.getData().getPersonBountyEventData().isParticipating(f.getId())) continue;
            enigma.setRelationship(f.getId(), RepLevel.VENGEFUL);
        }
        enigma.setRelationship("enigma", RepLevel.COOPERATIVE);
        //error
        enigma.setRelationship(Factions.OMEGA, RepLevel.NEUTRAL);
        //IBB
        if (Global.getSector().getFaction("famous_bounty")!=null) {
            enigma.setRelationship("famous_bounty", RepLevel.NEUTRAL);
        }
        //HVB
        if (Global.getSector().getFaction("hvb_hostile")!=null) {
            enigma.setRelationship("hvb_hostile", RepLevel.NEUTRAL);
        }
        //terminate
        enigma.setRelationship(Factions.PLAYER, RepLevel.VENGEFUL);
        enigma.setRelationship(Factions.REMNANTS, RepLevel.VENGEFUL);
        enigma.setRelationship(Factions.DERELICT, RepLevel.VENGEFUL);
    }
    public static void setKestevenRelation(SectorAPI sector) {
        FactionAPI kesteven = sector.getFaction("kesteven");

        FactionAPI omega = sector.getFaction(Factions.OMEGA);
        FactionAPI remnant = sector.getFaction(Factions.REMNANTS);
        FactionAPI derelict = sector.getFaction(Factions.DERELICT);
        FactionAPI pirates = sector.getFaction(Factions.PIRATES);
        FactionAPI indies = sector.getFaction(Factions.INDEPENDENT);
        FactionAPI LP = sector.getFaction(Factions.LUDDIC_PATH);
        FactionAPI hegemony = sector.getFaction(Factions.HEGEMONY);
        FactionAPI TT = sector.getFaction(Factions.TRITACHYON);

        List <FactionAPI> allFactions = sector.getAllFactions();
        for (FactionAPI f : allFactions) {
            //copy hege relations
            if (f.getRelationship(hegemony.getId())<=-0.5f) {
                kesteven.setRelationship(f.getId(), MathUtils.getRandomNumberInRange(-0.40f, -0.25f));
            }
            //not hostile, but..
            if (f.getRelationship(hegemony.getId())>-0.5f && f.getRelationship(hegemony.getId())<-0.15f) {
                kesteven.setRelationship(f.getId(), MathUtils.getRandomNumberInRange(-0.25f, -0.10f));
            }
            if (f.getRelationship(hegemony.getId())>=0.25f) {
                kesteven.setRelationship(f.getId(), MathUtils.getRandomNumberInRange(0.15f, 0.30f));
            }
            //randomise default relation with everyone else
            if (f.getRelationship("kesteven") == 0f && !f.getId().equals(Factions.PLAYER)) {
                if (!SharedData.getData().getPersonBountyEventData().isParticipating(f.getId())) continue;
                kesteven.setRelationship(f.getId(), MathUtils.getRandomNumberInRange(-0.10f, 0.15f));
            }
        }
        //baddies
        kesteven.setRelationship(omega.getId(), RepLevel.VENGEFUL);
        kesteven.setRelationship(remnant.getId(), RepLevel.VENGEFUL);
        kesteven.setRelationship(derelict.getId(), RepLevel.VENGEFUL);
        kesteven.setRelationship(pirates.getId(), RepLevel.HOSTILE);
        kesteven.setRelationship(LP.getId(), RepLevel.HOSTILE);

        //brothers!
        kesteven.setRelationship(indies.getId(), 0.20f);
        //you definitely wouldn't want to kill us right??
        kesteven.setRelationship(hegemony.getId(), 0.35f);
        //stinky
        kesteven.setRelationship(TT.getId(), -0.35f);
    }

    public static void genPeople() {
        MarketAPI heart = Global.getSector().getEconomy().getMarket("nskr_heart");
        if (heart != null) {
            //ENIGMA ADMIN
            PersonAPI admin = Global.getFactory().createPerson();
            admin.setFaction("enigma");
            admin.setId("nskr_enigmaAdmin");
            admin.setGender(FullName.Gender.FEMALE);
            admin.setPostId(Ranks.POST_FACTION_LEADER);
            admin.setRankId(Ranks.FACTION_LEADER);
            admin.getName().setFirst("Enigma");
            admin.getName().setLast("");
            admin.setPortraitSprite("graphics/portraits/nskr_enigma.png");

            admin.getStats().setSkillLevel(Skills.HYPERCOGNITION, 1);
            admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
            admin.setAICoreId(Commodities.ALPHA_CORE);

            heart.setAdmin(admin);
            heart.getCommDirectory().addPerson(admin, 0);
            heart.addPerson(admin);
            Global.getSector().getImportantPeople().addPerson(admin);
        }
        MarketAPI asteria = Global.getSector().getEconomy().getMarket("nskr_asteria");
        MarketAPI outpost = Global.getSector().getEconomy().getMarket("nskr_outpost");

        genMichael(asteria, 0);
        genJack(asteria, 1);
        genAlice(asteria, 2);
        //we do manual gen
        if (outpost!=null) {
            if (!nskr_modPlugin.IS_NEXELERIN || SectorManager.getManager().isCorvusMode()) {
                genNicholas(outpost, 2);
            }
        }

    }

    public static void genMichael(MarketAPI loc, int index) {
        if (loc==null){
            log("ERROR market is null");
            return;
        }
        //ADMIN ASTERIA
        PersonAPI admin = Global.getFactory().createPerson();
        admin.setFaction("kesteven");
        admin.setGender(FullName.Gender.MALE);
        admin.setPostId(Ranks.POST_FACTION_LEADER);
        admin.setRankId(Ranks.FACTION_LEADER);
        admin.setImportance(PersonImportance.VERY_HIGH);
        admin.getName().setFirst("Michael");
        admin.getName().setLast("Roux");
        admin.setId("nskr_president");
        admin.setPortraitSprite("graphics/portraits/nskr_president.png");
        if (nskr_modPlugin.IS_INDEVO) {
            admin.getStats().setSkillLevel("indevo_industrial_planning", 1);
        } else {
            admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
        }
        loc.setAdmin(admin);
        loc.getCommDirectory().addPerson(admin, index);
        loc.addPerson(admin);
        Global.getSector().getImportantPeople().addPerson(admin);
    }

    public static void genJack(MarketAPI loc, int index) {
        if (loc==null){
            log("ERROR market is null");
            return;
        }
        //QUEST GUY
        PersonAPI opguy = Global.getFactory().createPerson();
        opguy.setFaction("kesteven");
        opguy.setGender(FullName.Gender.MALE);
        opguy.setImportance(PersonImportance.MEDIUM);
        opguy.setPostId("kSpaceOperations");
        opguy.setRankId("kSpaceOperations");
        opguy.getName().setFirst("Jack");
        opguy.getName().setLast("Lapua");
        opguy.setPortraitSprite("graphics/portraits/nskr_jack.png");
        opguy.setId("nskr_opguy");
        opguy.addTag("k_quest");
        opguy.addTag(Tags.CONTACT_MILITARY);
        opguy.addTag("nskr_contracts");
        opguy.setVoice(Voices.OFFICIAL);
        loc.getCommDirectory().addPerson(opguy, index);
        loc.addPerson(opguy);
        Global.getSector().getImportantPeople().addPerson(opguy);
    }

    public static void genAlice(MarketAPI loc, int index) {
        if (loc==null){
            log("ERROR market is null");
            return;
        }
        //R&D
        PersonAPI researcher = Global.getFactory().createPerson();
        researcher.setFaction("kesteven");
        researcher.setGender(FullName.Gender.FEMALE);
        researcher.setImportance(PersonImportance.HIGH);
        researcher.setPostId("kResearch");
        researcher.setRankId("kResearch");
        researcher.getName().setFirst("Alice");
        researcher.getName().setLast("Lumi");
        researcher.setPortraitSprite("graphics/portraits/nskr_alice.png");
        researcher.setId("nskr_researcher");
        researcher.addTag("k_quest");
        researcher.addTag(Tags.CONTACT_MILITARY);
        researcher.addTag(Tags.CONTACT_TRADE);
        researcher.addTag("nskr_contracts");
        researcher.setVoice(Voices.SCIENTIST);
        loc.getCommDirectory().addPerson(researcher, index);
        loc.addPerson(researcher);
        Global.getSector().getImportantPeople().addPerson(researcher);
    }

    public static void genNicholas(MarketAPI loc, int index) {
        if (loc==null){
            log("ERROR market is null");
            return;
        }
        //outpost intelligence guy
        PersonAPI intelligence = Global.getSector().getFaction("kesteven").createRandomPerson(FullName.Gender.MALE);
        intelligence.setFaction("kesteven");
        intelligence.setImportance(PersonImportance.MEDIUM);
        intelligence.setPostId("kIntelligence");
        intelligence.setRankId("kIntelligence");
        intelligence.getName().setFirst("Nicholas");
        intelligence.getName().setLast("Antoine");
        intelligence.setPortraitSprite("graphics/portraits/nskr_nicholas.png");
        intelligence.setId("nskr_intelligence");
        intelligence.addTag("k_quest");
        intelligence.addTag(Tags.CONTACT_MILITARY);
        intelligence.setVoice(Voices.OFFICIAL);
        loc.getCommDirectory().addPerson(intelligence, index);
        loc.addPerson(intelligence);
        Global.getSector().getImportantPeople().addPerson(intelligence);
    }

    public static PersonAPI genEliza() {

        //Eliza
        PersonAPI anarchist = Global.getSector().getFaction(Factions.PIRATES).createRandomPerson(FullName.Gender.FEMALE);
        anarchist.setFaction(Factions.PIRATES);
        anarchist.setImportance(PersonImportance.VERY_HIGH);
        anarchist.setPostId("pAnarchist");
        anarchist.setRankId("pAnarchist");
        anarchist.getName().setFirst("Eliza");
        anarchist.getName().setLast("");
        anarchist.setPortraitSprite("graphics/portraits/nskr_eliza.png");
        anarchist.setId("nskr_anarchist");
        anarchist.addTag("k_anarchist");
        anarchist.addTag(Tags.CONTACT_UNDERWORLD);
        anarchist.addTag(Tags.CONTACT_MILITARY);
        anarchist.setVoice(Voices.OFFICIAL);
        Global.getSector().getImportantPeople().addPerson(anarchist);

        return anarchist;
    }
}
