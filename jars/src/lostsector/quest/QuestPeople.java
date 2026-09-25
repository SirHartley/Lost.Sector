package lostsector.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;

import java.util.ArrayList;
import java.util.function.Consumer;

// Generated quest people. Registration with ImportantPeopleAPI is what lets BeginConversation, ShowPersonVisual
// and ShowSecondPerson find them by id; ImportantPeople keys people by getId(), so the id is set before adding.
public final class QuestPeople {

    private final QuestManager.Run<?, ?> run;

    QuestPeople(QuestManager.Run<?, ?> run) {
        this.run = run;
    }

    // Returns the existing person when the key exists; null when the key is not declared with d.person or the faction is unknown.
    public PersonAPI create(String key, String factionId, Consumer<PersonAPI> setup) {
        return create(key, factionId, Gender.ANY, setup);
    }

    // Gender.ANY draws the gender from the person's random exactly as FactionAPI.createRandomPerson(Random) does, so
    // both overloads give the same person for the same key and seed.
    public PersonAPI create(String key, String factionId, Gender gender, Consumer<PersonAPI> setup) {
        QuestState<?> state = run.state();
        PersonAPI existing = state.people.get(key);
        if (existing != null) return existing;
        if (!run.quest.declarations().people().contains(key)) {
            QuestManager.logError(run.id(), "person " + key + " refused: not declared with d.person");
            return null;
        }
        FactionAPI faction = Global.getSector().getFaction(factionId);
        if (faction == null) {
            QuestManager.logError(run.id(), "person " + key + " refused: unknown faction " + factionId);
            return null;
        }
        PersonAPI person = faction.createRandomPerson(gender, QuestContext.random(state, "person:" + key));
        person.setId(id(key));
        if (setup != null) setup.accept(person);
        Global.getSector().getImportantPeople().addPerson(person);
        state.people.put(key, person);
        QuestManager.logInfo(run.id(), "person " + person.getId() + ": " + person.getNameString());
        return person;
    }

    // Null when absent.
    public PersonAPI get(String key) {
        return run.state().people.get(key);
    }

    public String id(String key) {
        return "nskr_" + run.id() + "_" + key;
    }

    public void release(String key) {
        PersonAPI person = run.state().people.remove(key);
        if (person == null) return;
        Global.getSector().getImportantPeople().removePerson(person);
        QuestManager.logInfo(run.id(), "release person " + person.getId());
    }

    void releaseAll() {
        for (String key : new ArrayList<>(run.state().people.keySet())) {
            release(key);
        }
    }
}
