package lostsector.campaign.events.blacksite;

import com.fs.starfarer.api.impl.campaign.ids.Factions;
import lostsector.helper.Ids;

// Saved by name in SiteRecord. Declaration order is the order of the faction picker, which the pick depends on;
// REMNANTS has no weight because only remnant-themed systems get it.
enum BlacksiteFaction {

    LUDDIC_PATH(Factions.LUDDIC_PATH, 4f, 125f, 150f, "Pather Stash", "Flotilla", false, "nskr_blacksite_pather", BlacksiteModule.ROLE_LOOTER),
    PIRATES(Factions.PIRATES, 4f, 150f, 175f, "Pirate Stash", "Gang", false, "nskr_blacksite_pirate", BlacksiteModule.ROLE_LOOTER),
    ENIGMA(Ids.ENIGMA_FACTION_ID, 5f, 75f, 100f, "Ancient Enigma Hangar", "Black Ops ", true, "nskr_blacksite_enigma", BlacksiteModule.ROLE_WRECKER),
    KESTEVEN(Ids.KESTEVEN_FACTION_ID, 6f, 100f, 125f, "Kesteven Blacksite", "Strike Force", false, "nskr_blacksite_kesteven", BlacksiteModule.ROLE_EVACUATOR),
    TRITACHYON(Factions.TRITACHYON, 6f, 100f, 125f, "Tri-Tachyon Blacksite", "Black Ops Group", false, "nskr_blacksite_tritachyon", BlacksiteModule.ROLE_EVACUATOR),
    REMNANTS(Factions.REMNANTS, 0f, 100f, 125f, "Ancient Remnant Hangar", "Sub-Ordo ", true, "nskr_blacksite_remnant", BlacksiteModule.ROLE_REMNANT);

    final String factionId;
    final float weight;
    final float minPoints;
    final float maxPoints;
    final String siteName;
    // Followed by a random capital Greek letter when greekSuffix is set.
    final String fleetName;
    final boolean greekSuffix;
    // The salvage entity from data/config/custom_entities.json that replaces the site when it is cleared.
    final String lootEntity;
    final String role;

    BlacksiteFaction(String factionId, float weight, float minPoints, float maxPoints, String siteName, String fleetName,
                     boolean greekSuffix, String lootEntity, String role) {
        this.factionId = factionId;
        this.weight = weight;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.siteName = siteName;
        this.fleetName = fleetName;
        this.greekSuffix = greekSuffix;
        this.lootEntity = lootEntity;
        this.role = role;
    }
}
