package scripts.kissa.LOST_SECTOR.campaign.quests.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import scripts.kissa.LOST_SECTOR.util.util;

import java.util.Map;

public class simpleCaptain {

    //base
    public Map<String, Integer> skills;
    public String faction;
    public String id;

    //custom
    public boolean isAiCore = false;
    public String aiCoreID = null;
    public String personality = Personalities.STEADY;
    public String portraitSpritePath = null;
    public String postId = null;
    public String rankId = null;
    public String firstName = "";
    public String lastName = "";
    public FullName.Gender gender = FullName.Gender.ANY;

    public simpleCaptain(String id, String faction, Map<String, Integer> skills) {
        this.id = id;
        this.faction = faction;
        this.skills = skills;
    }

    public PersonAPI create(){
        PersonAPI captain = Global.getSector().getFaction(faction).createRandomPerson();

        captain.setId(id);
        if (isAiCore && aiCoreID!=null) captain.setAICoreId(aiCoreID);
        captain.setPersonality(personality);
        if (portraitSpritePath!=null)captain.setPortraitSprite(portraitSpritePath);
        if (postId!=null)captain.setPostId(postId);
        if (rankId!=null)captain.setRankId(rankId);
        if (firstName.length()>0 || lastName.length()>0) {
            FullName name = new FullName(firstName, lastName, gender);
            captain.setName(name);
        }
        util.setOfficerSkills(captain, skills);

        return captain;
    }
}
