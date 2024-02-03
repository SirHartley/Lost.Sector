package scripts.kissa.LOST_SECTOR.campaign.customStart.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class hellSpawnEventFactors extends BaseOneTimeFactor {


    private final String desc;
    private final String tooltipText;
    private final String grayText;

    public hellSpawnEventFactors(int points, String desc, String tooltipText, String grayText) {
        super(points);
        this.desc = desc;
        this.tooltipText = tooltipText;
        this.grayText = grayText;

        MessageIntel intel = new MessageIntel("Gained "+points+" points of progress.", Misc.getTextColor(), new String[]{points + ""}, Misc.getHighlightColor());
        intel.setIcon("graphics/icons/intel/damage.png");
        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.INTEL_TAB);
    }

    @Override
    public String getDesc(BaseEventIntel intel) {
        return desc;
    }

    @Override
    public boolean isOneTime() {
        return true;
    }

    @Override
    public TooltipMakerAPI.TooltipCreator getMainRowTooltip() {
        return new BaseFactorTooltip() {
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                float opad = 10f;
                Color h = Misc.getHighlightColor();
                Color r = Misc.getNegativeHighlightColor();
                Color g = Misc.getGrayColor();

                tooltip.addPara(tooltipText, opad);
                if (grayText.length()>0)
                    tooltip.addPara(grayText, opad, g, h,"").italicize();
            }

        };
    }
}
