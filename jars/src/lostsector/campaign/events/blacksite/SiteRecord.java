package lostsector.campaign.events.blacksite;

import com.fs.starfarer.api.campaign.SectorEntityToken;

// One blacksite, saved inside BlacksiteState. entity stays the original station after the loot swap, as the fleets'
// target does, so the defenders keep orbiting its last position.
final class SiteRecord {

    enum Status {
        DORMANT,
        ACTIVE,
        CLEARED,
        DESTROYED
    }

    SectorEntityToken entity;
    String id;
    BlacksiteFaction faction;
    int count;
    float points;

    Status status = Status.DORMANT;
    // Summed days that defenders spent orbiting the site; the site is destroyed at BlacksiteModule.DESTROY_FLEET_DAYS.
    float destroyDays;

    SiteRecord(SectorEntityToken entity, BlacksiteFaction faction, int count, float points) {
        this.entity = entity;
        this.id = entity.getId();
        this.faction = faction;
        this.count = count;
        this.points = points;
    }
}
