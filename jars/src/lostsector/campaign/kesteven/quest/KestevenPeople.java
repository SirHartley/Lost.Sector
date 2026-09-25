package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import lostsector.campaign.kesteven.ExileManager;
import lostsector.helper.SectorLookup;

public class KestevenPeople {

    public static PersonAPI getJack(){
        boolean exiled = ExileManager.getExiled(ExileManager.EXILE_KEY);
        if (SectorLookup.getAsteria()==null && !exiled) return null;
        if (SectorLookup.getOutpost()==null && exiled) return null;
        return Global.getSector().getImportantPeople().getPerson("nskr_opguy");
    }

    public static PersonAPI getAlice(){
        boolean exiled = ExileManager.getExiled(ExileManager.EXILE_KEY);
        if (SectorLookup.getAsteria()==null && !exiled) return null;
        if (SectorLookup.getOutpost()==null && exiled) return null;
        return Global.getSector().getImportantPeople().getPerson("nskr_researcher");
    }

    public static PersonAPI getNick(){
        if (SectorLookup.getOutpost()==null) return null;
        return Global.getSector().getImportantPeople().getPerson("nskr_intelligence");
    }

    public static PersonAPI getMichael(){
        boolean exiled = ExileManager.getExiled(ExileManager.EXILE_KEY);
        if (SectorLookup.getAsteria()==null && !exiled) return null;
        if (SectorLookup.getOutpost()==null && exiled) return null;
        return Global.getSector().getImportantPeople().getPerson("nskr_president");
    }

    public static PersonAPI getEliza(){
        return Global.getSector().getImportantPeople().getPerson("nskr_anarchist");
    }
}
