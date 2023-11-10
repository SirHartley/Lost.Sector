package scripts.kissa.LOST_SECTOR.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import scripts.kissa.LOST_SECTOR.util.mathUtil;

import java.awt.*;
import java.io.IOException;

public class nskr_zhonyaStats extends BaseShipSystemScript {

	public static final float FLUX_BONUS = 2.0f;

	public static final Color JITTER_COLOR = new Color(255, 200, 0, 255);
	public static final Color GOLD_COLOR = new Color(255, 193, 32, 255);
	public static final float DISTORTION_RADIUS = 275f;
	public static final Color OG_COLOR = new Color(255, 255, 255);

	public static final String SPRITE_PATH = "graphics/fx/nskr_epoch_golden.png";
	public static final String SPRITE_PATH_E = "graphics/fx/nskr_epoch_e_golden.png";
	public static final String SPRITE_PATH_X = "graphics/fx/nskr_epochx_golden.png";
	public static final String SPRITE_PATH_EX = "graphics/fx/nskr_epochx_e_golden.png";

	private final IntervalUtil sparkleInterval = new IntervalUtil(0.15f, 0.30f);
	private SpriteAPI ogSprite = null;
	private WaveDistortion wave;
	private boolean unapply = true;
	private boolean loaded = false;

	static void log(final String message) {
		Global.getLogger(nskr_zhonyaStats.class).info(message);
	}

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		effectLevel = 1f;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}

		if (Global.getCombatEngine().isPaused() || !ship.isAlive()) {
			return;
		}
		if (unapply){
			unapply = false;
			ogSprite = ship.getSpriteAPI();
		} else {
			CombatEngineAPI engine = Global.getCombatEngine();

			ship.setPhased(true);
			ship.setCollisionClass(CollisionClass.NONE);

			stats.getFluxDissipation().modifyMult(id, FLUX_BONUS);
			//freeze
			ship.getVelocity().set(new Vector2f(0f, 0f));
			ship.setAngularVelocity(0f);

			//FX

			//GOLDEN
			SpriteAPI newSprite;
			if (ship.getHullSpec().getBaseHullId().equals("nskr_epoch") || ship.getHullSpec().getBaseHullId().equals("nskr_epoch_e")) {
				if (ship.getHullSpec().hasTag("enigma")){
					newSprite = getSprite(SPRITE_PATH_E);
				} else {
					newSprite = getSprite(SPRITE_PATH);
				}
			} else {
				if (ship.getHullSpec().hasTag("enigma")) {
					newSprite = getSprite(SPRITE_PATH_EX);
				} else {
					newSprite = getSprite(SPRITE_PATH_X);
				}
			}
			ship.setSprite(newSprite);
			//required to make jitter work????
			ship.getSpriteAPI().setAngle(ship.getFacing()-90f);

			//weapons
			for (WeaponAPI wep : ship.getAllWeapons()) {
				if (wep.getType() == WeaponAPI.WeaponType.DECORATIVE) continue;
				SpriteAPI weapon = wep.getSprite();
				if (weapon == null) continue;
				weapon.setColor(GOLD_COLOR);
				//weapon anim
				if (wep.getAnimation() != null && wep.getAnimation().getNumFrames() > 0) {
					wep.getAnimation().pause();
				}
				//barrel
				SpriteAPI barrel = wep.getBarrelSpriteAPI();
				if (barrel != null) {
					barrel.setColor(GOLD_COLOR);
				}
				//missile
				if (wep.getMissileRenderData() != null && !wep.getMissileRenderData().isEmpty()) {
					for (MissileRenderDataAPI mis : wep.getMissileRenderData()) {
						SpriteAPI missile = mis.getSprite();
						missile.setColor(GOLD_COLOR);
					}
				}
			}
			//jitter
			ship.setJitterUnder(this, JITTER_COLOR, 10f, 8, 2f);
			ship.setJitterShields(false);

			sparkleInterval.advance(Global.getCombatEngine().getElapsedInLastFrame());
			if (sparkleInterval.intervalElapsed()) {
				for (int x = 0; x < 3; x++) {
					Vector2f point = ship.getLocation();
					float angle = (float) Math.random() * 360f;
					float distance = ((float) Math.random() * 200f) * effectLevel;
					float bias = mathUtil.BiasFunction(Math.random(), 0.2f);
					distance *= bias;
					distance += 75f;

					Vector2f point1 = MathUtils.getPointOnCircumference(point, distance, angle);
					engine.addSmoothParticle(
							point1,
							ship.getVelocity(),
							MathUtils.getRandomNumberInRange(20, 60),
							0.5f,
							0.50f,
							JITTER_COLOR
					);
				}
			}
			//distortion
			Vector2f loc = ship.getLocation();
			wave = new WaveDistortion();
			wave.setLocation(loc);
			wave.setSize(DISTORTION_RADIUS);
			wave.setIntensity(DISTORTION_RADIUS * 0.02f);
			wave.fadeInSize(0.3f);
			wave.fadeOutIntensity(0.3f);
			wave.setSize(DISTORTION_RADIUS * 0.5f);
			DistortionShader.addDistortion(wave);
		}
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}

		//unaplly gets called everyFrame I guess?
		if (!unapply) {

			ship.setPhased(false);

			ship.setCollisionClass(CollisionClass.SHIP);

			stats.getFluxDissipation().unmodify(id);

			if (ogSprite!=null) {
				unapply = true;
				//remove GOLDEN
				ship.setSprite(ogSprite);

				//weapons
				for (WeaponAPI wep : ship.getAllWeapons()) {
					if (wep.getType() == WeaponAPI.WeaponType.DECORATIVE) continue;
					SpriteAPI weapon = wep.getSprite();
					if (weapon == null) continue;
					weapon.setColor(OG_COLOR);
					//weapon anim
					if (wep.getAnimation() != null && wep.getAnimation().getNumFrames() > 0) {
						wep.getAnimation().play();
						wep.getAnimation().reset();
					}
					//barrel
					SpriteAPI barrel = wep.getBarrelSpriteAPI();
					if (barrel != null) {
						barrel.setColor(OG_COLOR);
					}
					//missile
					if (wep.getMissileRenderData() != null && !wep.getMissileRenderData().isEmpty()) {
						for (MissileRenderDataAPI mis : wep.getMissileRenderData()) {
							SpriteAPI missile = mis.getSprite();
							missile.setColor(OG_COLOR);
						}
					}
				}
			}
		}
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("invulnerable", false);
		}
		if (index == 1) {
			return new StatusData("movement halted", true);
		}
		if (index == 2) {
			return new StatusData("increased flux dissipation", false);
		}
		return null;
	}

	private SpriteAPI getSprite(String path){
		SpriteAPI sprite;
		// Load sprite if it hasn't been loaded yet - not needed if you add it to settings.json
		if (!loaded) {
			loaded = true;
			try {
				Global.getSettings().loadTexture(path);
			} catch (IOException ex) {
				throw new RuntimeException("Failed to load sprite '" + path + "'!", ex);
			}
		}
		sprite = Global.getSettings().getSprite(path);
		return sprite;
	}
}
