//////////////////////
//TELEPORT by Tartiflette
//////////////////////
package scripts.kissa.LOST_SECTOR.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class nskr_teleporterPlugin extends BaseEveryFrameCombatPlugin {

    public static final Map<CombatEntityAPI, Vector2f> TELEPORT = new HashMap<>();
    private CombatEngineAPI engine;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        if (engine.isPaused()) {
            return;
        }

        //check teleportations timed
        if(!TELEPORT.isEmpty()){
            teleport(amount);
        }
    }
	
	//////////////////////////////
    //                          //
    //     TELEPORT EFFECT      //
    //                          //
    //////////////////////////////
    //by Tartiflette

    public static void addTeleportation(CombatEntityAPI target, Vector2f location){
        TELEPORT.put(target, location);
    }

    private void teleport(float amount){
        for (Iterator<CombatEntityAPI> iter = TELEPORT.keySet().iterator(); iter.hasNext();){
            CombatEntityAPI C = iter.next();

            if(C==null || MathUtils.isWithinRange(C.getLocation(), TELEPORT.get(C), 50f)){
                iter.remove();
            } else {
                float x = 10*amount*(TELEPORT.get(C).x-C.getLocation().x);
                float y = 10*amount*(TELEPORT.get(C).y-C.getLocation().y);
                Vector2f targetPos=new Vector2f(C.getLocation().x+x,C.getLocation().y+y);
                if (C instanceof ShipAPI && MagicRender.screenCheck(0.15f, targetPos)){
                    //afterimage
                    ((ShipAPI) C).addAfterimage(
                            new Color(255,255,255,35),
                            0,
                            0,
                            -5*x,
                            -5*y,
                            5f,
                            0.15f,
                            0.25f,
                            0.15f,
                            false,
                            true,
                            false
                    );
                }
                setLocation(C,targetPos);
            }
        }
    }

    public void setLocation(CombatEntityAPI entity, Vector2f location) {
        Vector2f dif = new Vector2f(location);
        Vector2f.sub(location, entity.getLocation(), dif);
        Vector2f.add(entity.getLocation(), dif, entity.getLocation());
    }
}
