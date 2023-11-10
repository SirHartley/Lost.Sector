package scripts.kissa.LOST_SECTOR.campaign.fleets.events;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import scripts.kissa.LOST_SECTOR.util.ids;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class blacksiteInfo {

    public SectorEntityToken entity;
    public String id;
    public float points;
    public int count;
    public String faction;
    public Random random;

    //
    public boolean active = false;
    public int activeFleets = 0;
    public float countdown = 20f;
    public float destroyCounter = 0f;
    public boolean destroyed = false;
    public boolean cleared = false;

    public blacksiteInfo(SectorEntityToken entity, Random random) {
        this.entity = entity;
        this.id = entity.getId();

        this.random = random;

        this.faction = pickFactionType();

        switch (faction){
            case Factions.PIRATES:
                this.points = mathUtil.getSeededRandomNumberInRange(150f, 175f, random);
                break;
            case Factions.LUDDIC_PATH:
                this.points = mathUtil.getSeededRandomNumberInRange(125f, 150f, random);
                break;
            case Factions.TRITACHYON:
                this.points = mathUtil.getSeededRandomNumberInRange(100f, 125f, random);
                break;
            case ids.KESTEVEN_FACTION_ID:
                this.points = mathUtil.getSeededRandomNumberInRange(100f, 125f, random);
                break;
            case ids.ENIGMA_FACTION_ID:
                this.points = mathUtil.getSeededRandomNumberInRange(75f, 100f, random);
                break;
            case Factions.REMNANTS:
                this.points = mathUtil.getSeededRandomNumberInRange(100f, 125f, random);
                break;
        }

        this.count = mathUtil.getSeededRandomNumberInRange(1, 4, random);
    }

    public static final List<Pair<String, Float>> FACTIONS = new ArrayList<>();
    static {
        FACTIONS.add(new Pair<>(Factions.LUDDIC_PATH, 4f));
        FACTIONS.add(new Pair<>(Factions.PIRATES, 4f));
        FACTIONS.add(new Pair<>(ids.ENIGMA_FACTION_ID, 5f));
        FACTIONS.add(new Pair<>(ids.KESTEVEN_FACTION_ID, 6f));
        FACTIONS.add(new Pair<>(Factions.TRITACHYON, 6f));
    }
    public String pickFactionType() {
        if (entity.getStarSystem().hasTag(Tags.THEME_REMNANT)){
            return Factions.REMNANTS;
        }

        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
        picker.setRandom(random);
        for (Pair<String,Float> s : FACTIONS){
            picker.add(s.one,s.two);
        }
        return picker.pick();
    }

}
