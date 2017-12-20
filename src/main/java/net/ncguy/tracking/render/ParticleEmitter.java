package net.ncguy.tracking.render;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.*;
import com.badlogic.gdx.math.Vector3;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nick on 26/03/2015 at 22:45.
 * Project: Development
 * Package: net.ncguy.commonutil.particle
 */
public class ParticleEmitter {

    public ParticleController controller;
    public boolean isRunning;
    public ParticleFactory.ParticleTranslated part;
    public ParticleFactory.ParticleMovement moveType;

    // Influencers
    public RegionInfluencer.Single regionInfluencer;
    public SpawnInfluencer spawnInfluencer;
    public ScaleInfluencer scaleInfluencer;
    public ColorInfluencer.Single colourInfluencer;
    public DynamicsInfluencer dynamicsInfluencer;
    // Dynamics
    public DynamicsModifier.BrownianAcceleration brownian;
    // Emitter
    public RegularEmitter emitter;

    public ParticleEmitter(ParticleFactory.ParticleTranslated part, ParticleController controller) {
        this.part = part;
        this.controller = controller;
        this.isRunning = true;
        this.moveType = ParticleFactory.ParticleMovement.STATIC;
        // Influencers
        regionInfluencer = (RegionInfluencer.Single)controller.influencers.get(0);
        spawnInfluencer = (SpawnInfluencer)controller.influencers.get(1);
        scaleInfluencer = (ScaleInfluencer)controller.influencers.get(2);
        colourInfluencer = (ColorInfluencer.Single)controller.influencers.get(3);
        dynamicsInfluencer = (DynamicsInfluencer)controller.influencers.get(4);
        // Dynamics
        brownian = (DynamicsModifier.BrownianAcceleration)dynamicsInfluencer.velocities.get(0);
        // Emitter
        emitter = (RegularEmitter)controller.emitter;
    }

    public void move(float angle, float delta) {
        float x1 = 0, y1 = 0, z1 = 0;
        switch(moveType){
            case STATIC: break;
            case RADIAL:
                if(part.moveAxis.x) x1 = part.angleControllers.x.process(angle*part.moveSpeed)*part.radius;
                if(part.moveAxis.y) y1 = part.angleControllers.y.process(angle*part.moveSpeed)*part.radius;
                if(part.moveAxis.z) z1 = part.angleControllers.z.process(angle*part.moveSpeed)*part.radius;
                break;
            case SPLINE: break;
        }
        controller.setTranslation(new Vector3(part.position.x+x1, part.position.y+y1, part.position.z+z1));
        reload();
    }

    public void reload() {
        // Scale
        scaleInfluencer.value.setLow(part.scaleRange.x);
        scaleInfluencer.value.setHigh(part.scaleRange.y);
        scaleInfluencer.value.setTimeline(new float[]{part.scaleTimeline.x,part.scaleTimeline.y});
        scaleInfluencer.value.setScaling(new float[]{part.scaleScale.x,part.scaleScale.y});
        // Colour + Alpha
        colourInfluencer.colorValue.setColors(new float[]{
                part.particleColour.r,
                part.particleColour.g,
                part.particleColour.b,
                0, 0, 0
        });
        colourInfluencer.colorValue.setTimeline(new float[]{0, 1});
        colourInfluencer.alphaValue.setLow(part.alphaRange.x);
        colourInfluencer.alphaValue.setHigh(part.alphaRange.y);
        colourInfluencer.alphaValue.setTimeline(new float[]{part.alphaTimeline.x,part.alphaTimeline.y});
        colourInfluencer.alphaValue.setScaling(new float[]{part.alphaScale.x,part.alphaScale.y});
        // Dynamics
        brownian.strengthValue.setLow(part.brownianRange.x);
        brownian.strengthValue.setHigh(part.brownianRange.y);
//        System.out.println(part.brownianRange.y);
        brownian.strengthValue.setTimeline(new float[]{part.brownianTimeline.x, part.brownianTimeline.y});
        brownian.strengthValue.setScaling(new float[]{part.brownianScale.x,part.brownianScale.y});
        // Misc.
        emitter.getDuration().setLow(part.duration);
        emitter.getEmission().setLow(part.emissionRange.x);
        emitter.getEmission().setHigh(part.emissionRange.y);
        emitter.getLife().setLow(part.lifeRange.x);
        emitter.getLife().setHigh(part.lifeRange.y);
        emitter.setMaxParticleCount(part.maxCount);
    }

    public void update() {
        try{ reload(); }catch(Exception e) { e.printStackTrace(); }
        try { controller.update(); }catch(Exception e) { e.printStackTrace(); }
    }
    public void draw() {

        controller.draw();
    }
    public void render(){
        update();
        draw();
    }

    public boolean isRunning() {
        return isRunning;
    }
    public void setRunning(boolean run) {
        this.isRunning = run;
        switch(run+""){
            case "true":
                emitter.setEmissionMode(RegularEmitter.EmissionMode.Enabled); break;
            case "false":
                emitter.setEmissionMode(RegularEmitter.EmissionMode.EnabledUntilCycleEnd); break;
        }
    }
    public void start() { setRunning(true);  }
    public void stop()  { setRunning(false); }

    public static class AngleController {
        public static Map<String, AngleControllers> controllers = new HashMap<>();
        public static void init() {
//            Registry.registerAngleController(new SineController());
//            Registry.registerAngleController(new CosineController());
//            Registry.registerAngleController(new TangentController());
//            Registry.registerAngleController(new SpiralController());
        }

        public AngleController(String controller) {
            this.controller = controllers.get(controller);
            this.name = controller;
        }
        private AngleControllers controller;
        public String name;
        public float process(float angle) {
            return controller.process(angle);
        }
    }

    public static interface AngleControllers {
        public float process(float angle);
        public String id();
    }
    public static class SineController implements AngleControllers {
        public float process(float angle) {
            return (float)Math.sin(angle%360);
        }
        public String id() { return "SINE"; }
    }
    public static class CosineController implements AngleControllers {
        public float process(float angle) {
            return (float)Math.cos(angle%360);
        }
        public String id() { return "COSINE"; }
    }
    public static class TangentController implements AngleControllers {
        public float process(float angle) {
            return (float)Math.tan(angle%360);
        }
        public String id() { return "TANGENT"; }
    }
    public static class SpiralController implements AngleControllers {
        public float process(float angle) { return (float)Math.sin((angle/10)%360); }
        public String id() { return "SPIRAL"; }
    }
}
