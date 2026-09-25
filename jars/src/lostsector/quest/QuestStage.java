package lostsector.quest;

public interface QuestStage {

    // The stage before this one on the story path; null for the start stage and for stages reachable from anywhere.
    QuestStage previous();
}
