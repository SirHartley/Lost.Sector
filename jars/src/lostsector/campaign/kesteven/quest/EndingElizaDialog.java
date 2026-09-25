package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.starts.GameModeManager;
import lostsector.campaign.starts.hellspawn.HellSpawnEventFactors;
import lostsector.campaign.starts.hellspawn.HellSpawnEventIntel;
import lostsector.campaign.kesteven.quest.UnlimitedProductionChipCondition;
import lostsector.campaign.kesteven.quest.QuestHelper;
import lostsector.ModPlugin;
import lostsector.helper.MathHelper;
import lostsector.helper.SectorLookup;

import java.awt.*;
import java.util.Map;
import java.util.Random;

public class EndingElizaDialog implements InteractionDialogPlugin {
    //

    private boolean arrived = false;

    private InteractionDialogAPI dialog;
    private TextPanelAPI text;
    private OptionPanelAPI options;
    private VisualPanelAPI visual;

    static void log(final String message) {
        Global.getLogger(DataSatelliteDialog.class).info(message);
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;

        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();
        visual = dialog.getVisualPanel();

        dialog.getVisualPanel().showPlanetInfo(dialog.getInteractionTarget());
        if (dialog.getInteractionTarget().getCustomInteractionDialogImageVisual()!=null) {
            visual.showImageVisual(dialog.getInteractionTarget().getCustomInteractionDialogImageVisual());
        } else if (dialog.getInteractionTarget().getMarket().getPlanetEntity()!=null) visual.showPlanetInfo(dialog.getInteractionTarget().getMarket().getPlanetEntity());

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();

        text.setFontInsignia();

        int stage = QuestHelper.getStage();
        if (stage >= 19 && !QuestHelper.getCompleted(KestevenFlag.ELIZA_ENDING_DONE)) {
            text.addPara("On approach you receiver orders from the port authority to land at a specific dock. You then receive a comms call from Eliza.");

            options.addOption("Continue", OptionId.A1);
        }
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        Color h = Misc.getHighlightColor();
        Color b = Misc.getBasePlayerColor();
        Color bh = Misc.getBrightPlayerColor();
        Color g = Misc.getGrayColor();
        Color gr = Misc.getPositiveHighlightColor();
        Color s = Misc.getStoryBrightColor();
        Color r = Misc.getNegativeHighlightColor();
        Color tc = Misc.getTextColor();

        PersonAPI eliza = KestevenPeople.getEliza();

        text.addPara(optionText, b, h, "", "");

        String manOrWoman = "captain";
        if (Global.getSector().getPlayerPerson().getName().getGender()== FullName.Gender.MALE) manOrWoman = "man";
        if (Global.getSector().getPlayerPerson().getName().getGender()== FullName.Gender.FEMALE) manOrWoman = "woman";

        //initial
        if (optionData == OptionId.A1) {
            arrived = true;
            options.clearOptions();
            text.addPara("\"Ah, you're here captain. Lets get straight to business, shall we?\" She quickly straightens her posture.");
            text.addPara("\"I hope you understand that Kesteven won't take this lightly, they'll get the Hegemony involved too for sure. You'll be a wanted "+manOrWoman+", " +
                    "but with the Chip secured our victory shall be inevitable.\"");

            options.addOption("Continue", OptionId.A2);
        }
        //a2 pt2
        if (optionData == OptionId.A2) {
            options.clearOptions();
            text.addPara("\"I have your equipment ready, soon we will begin a new collapse. The days old are over, it's time for a new dawn.\" There is a devious smile on her face.");
            text.addPara("A tiny shuttle approaches your fleet, it delivers the equipment from Eliza - as promised.");

            options.addOption("Continue", OptionId.A3);
        }
        //a3 complete
        if (optionData == OptionId.A3) {
            options.clearOptions();
            text.addPara("\"Our time to strike at the Hegemony and Kesteven has come...\"");
            text.addPara("Eliza frantically finishes her speech. \"Now go captain.\" She raises her hands and looks towards the roof, in a vaguely fanatic gesture. \"watch the stations burn, empires fall, leaders flee like cowards. It is time to infest the rat's nest. Humanity will be free!\" The grin on her face is bone chilling.");
            text.addPara("Hope you made the right choice, captain.",g,h,"","");

            QuestHelper.setCompleted(true, KestevenFlag.ELIZA_ENDING_DONE);
            QuestHelper.setStage(20);
            QuestHelper.saveEnding();

            if((Misc.getCommissionFactionId()!=null)) {
                if (Misc.getCommissionFactionId().equals("kesteven") || Misc.getCommissionFactionId().equals(Factions.HEGEMONY)) {
                    QuestHelper.setCompleted(true, KestevenFlag.COMMISSION_RESTORE_PENDING);
                }
                if(ModPlugin.IS_IRONSHELL){
                    if (Misc.getCommissionFactionId().equals("ironshell")){
                        QuestHelper.setCompleted(true, KestevenFlag.COMMISSION_RESTORE_PENDING);
                    }
                }
            }
            //hellspawn
            if (GameModeManager.getMode() == GameModeManager.GameMode.HELLSPAWN) HellSpawnEventIntel.get().addFactor(
                    new HellSpawnEventFactors(100+ MathHelper.getSeededRandomNumberInRange(3,10, getRandom()),
                    "Gave the UPC to Eliza", "What could possibly go wrong?", ""));

            //rewards
            CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
            //BPs
            playerCargo.addSpecial(new SpecialItemData("nskr_prot_wp", null), 1);
            playerCargo.addSpecial(new SpecialItemData("nskr_prot_heavy", null), 1);
            //+rep
            // QuestStageManager re-applies these values when a commission ends with this ending.
            KestevenState state = KestevenQuest.state();
            float repPirates = MathHelper.getSeededRandomNumberInRange(0.25f, 0.30f, getRandom());
            state.commissionRepPirates = repPirates;
            //pirate rep
            if(Global.getSector().getFaction(Factions.PIRATES).getRelationship(Factions.PLAYER)<repPirates) Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.PIRATES, repPirates);
            eliza.getRelToPlayer().adjustRelationship(0.30f, RepLevel.COOPERATIVE);
            //hege & kesteven war
            float repKesteven = MathHelper.getSeededRandomNumberInRange(-0.90f, -0.80f, getRandom());
            state.commissionRepKesteven = repKesteven;
            float repHege = MathHelper.getSeededRandomNumberInRange(-0.70f, -0.65f, getRandom());
            state.commissionRepHegemony = repHege;
            //completion text
            text.setFontSmallInsignia();
            //add sp
            Global.getSector().getPlayerStats().setStoryPoints(Global.getSector().getPlayerStats().getStoryPoints()+2);
            text.setFontSmallInsignia();
            text.addPara("Gained 2 Story points",g,s,"2 Story points","");
            //acquire text
            text.addPara("Acquired Prototype Weapons blueprint chip",g,h,"Prototype Weapons","");
            text.addPara("Acquired Prototype Heavy Ships blueprint chip",g,h,"Prototype Heavy Ships","");

            repPirates = Math.round(repPirates*100f);
            text.addPara("Relationship with Pirates improved to "+(int)repPirates,g,gr,""+(int)repPirates,"");
            text.addPara("Relationship with Eliza improved by 30",g,gr,"30","");

            //kesteven rep
            if(Global.getSector().getFaction(Factions.PIRATES).getRelationship("kesteven")>repKesteven) Global.getSector().getFaction(Factions.PIRATES).setRelationship("kesteven", repKesteven);
            if(Global.getSector().getFaction(Factions.PLAYER).getRelationship("kesteven")>repKesteven){
                Global.getSector().getFaction(Factions.PLAYER).setRelationship("kesteven", repKesteven);
                repKesteven = Math.round(repKesteven*100f);
                text.addPara("Relationship with Kesteven reduced to "+(int)repKesteven,g,r,""+(int)repKesteven,"");
            }
            //hege rep
            if(Global.getSector().getFaction(Factions.PIRATES).getRelationship(Factions.HEGEMONY)>repHege) Global.getSector().getFaction(Factions.PIRATES).setRelationship(Factions.HEGEMONY, repHege);
            if(Global.getSector().getFaction(Factions.PLAYER).getRelationship(Factions.HEGEMONY)>repHege){
                Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.HEGEMONY, repHege);
                float repHegeText = Math.round(repHege*100f);
                text.addPara("Relationship with the Hegemony reduced to "+(int)repHegeText,g,r,""+(int)repHegeText,"");
            }
            //IS rep
            if(ModPlugin.IS_IRONSHELL) {
                if (Global.getSector().getFaction(Factions.PIRATES).getRelationship("ironshell") > repHege) Global.getSector().getFaction(Factions.PIRATES).setRelationship("ironshell", repHege);
                if (Global.getSector().getFaction(Factions.PLAYER).getRelationship("ironshell") > repHege) {
                    Global.getSector().getFaction(Factions.PLAYER).setRelationship("ironshell", repHege);
                    float repHegeText = Math.round(repHege*100f);
                    text.addPara("Relationship with the Iron Shell reduced to " + (int) repHegeText, g, r, "" + (int) repHegeText, "");
                }
            }
            text.addPara("They will not forget this anytime soon",g,r,"","");
            //remove important
            QuestHelper.getElizaLoc().getMemory().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
            if (SectorLookup.asteriaOrOutpost()!=null) SectorLookup.asteriaOrOutpost().getMemory().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);

            text.setFontInsignia();

            //CONTACT
            ContactIntel.addPotentialContact(1f, eliza, dialog.getInteractionTarget().getMarket(), text);

            if(KestevenPeople.getAlice()!=null) {
                KestevenPeople.getAlice().getRelToPlayer().adjustRelationship(-0.75f, RepLevel.VENGEFUL);
                if(ContactIntel.getContactIntel(KestevenPeople.getAlice())!=null) {
                    ContactIntel.getContactIntel(KestevenPeople.getAlice()).setState(ContactIntel.ContactState.SUSPENDED);
                }
            }
            if(KestevenPeople.getJack()!=null) {
                KestevenPeople.getJack().getRelToPlayer().adjustRelationship(-0.75f, RepLevel.VENGEFUL);
                if(ContactIntel.getContactIntel(KestevenPeople.getJack())!=null) {
                    ContactIntel.getContactIntel(KestevenPeople.getJack()).setState(ContactIntel.ContactState.SUSPENDED);
                }
            }

            MarketAPI market = dialog.getInteractionTarget().getMarket();
            //CONDITION
            market.addCondition(UnlimitedProductionChipCondition.ID);
            //industries
            if (!market.hasIndustry(Industries.HEAVYINDUSTRY) && !market.hasIndustry(Industries.ORBITALWORKS)){
                market.addIndustry(Industries.ORBITALWORKS);
            }else if (market.hasIndustry(Industries.HEAVYINDUSTRY)){
                market.getIndustry(Industries.HEAVYINDUSTRY).startUpgrading();
                market.getIndustry(Industries.HEAVYINDUSTRY).finishBuildingOrUpgrading();
            }

            if (!market.hasIndustry(Industries.MILITARYBASE) && !market.hasIndustry(Industries.HIGHCOMMAND)){
                if (market.hasIndustry(Industries.PATROLHQ)){
                    market.getIndustry(Industries.PATROLHQ).startUpgrading();
                    market.getIndustry(Industries.PATROLHQ).finishBuildingOrUpgrading();
                } else market.addIndustry(Industries.MILITARYBASE);
            }

            Global.getSoundPlayer().playUISound("ui_rep_raise",1f,1f);

            dialog.setOptionOnEscape("Leave", OptionId.LEAVE);

            options.addOption("Leave", OptionId.LEAVE);
        }

        if(arrived){
            dialog.getVisualPanel().showPersonInfo(eliza, false);
        }

        //leave
        if (optionData == OptionId.LEAVE) {
            options.clearOptions();
            dialog.dismiss();
        }
    }

    public enum OptionId {
        A1,
        A2,
        A3,
        LEAVE,
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {
    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        return null;
    }

    public static Random getRandom() {
        return KestevenQuest.random(KestevenState.RANDOM_ELIZA_ENDING);
    }

}