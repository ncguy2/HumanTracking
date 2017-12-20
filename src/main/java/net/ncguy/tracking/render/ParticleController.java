package net.ncguy.tracking.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.*;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.particles.values.PointSpawnShapeValue;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class ParticleController {
    public static final String DEFAULT_PARTICLE = "textures/pre_particle.png";
    public static Texture tex;
    public static ParticleController instance;

    Quaternion tmpQuaternion = new Quaternion();
    Matrix4 tmpMatrix = new Matrix4(), tmpMatrix4 = new Matrix4();
    Vector3 tmpVector = new Vector3();

    //Simulation
    Array<ParticleEmitter> emitters;

    //Rendering
    BillboardParticleBatch batch;

    public static ParticleController build(Camera cam) {
        if (instance == null) instance = new ParticleController(cam);
        return instance;
    }

    public Array<ParticleEmitter> emitters() {
        return emitters;
    }

    private ParticleController(Camera cam) {
        emitters = new Array<>();
        batch = new BillboardParticleBatch();
        batch.setCamera(cam);
        tex = new Texture(Gdx.files.internal(DEFAULT_PARTICLE));
        batch.setTexture(tex);
        batch.setUseGpu(true);
    }

    public ParticleEmitter getEmitter(int index) {
        index = MathUtils.clamp(index, 0, emitters.size - 1);
        return emitters.get(index);
    }

    public ParticleEmitter getEmitter(String name) {
        for (ParticleEmitter em : emitters)
            if (em.part.id.equalsIgnoreCase(name)) return em;
        return null;
    }

    public void addEmitter(String name, String colHex) {
        addEmitter(name, Color.valueOf(colHex));
    }

    public void addEmitter(String name, Color colour) {
        addEmitter(name, new float[]{colour.r, colour.g, colour.b}, null);
    }

    public void addEmitter(String name, float[] colours, ParticleFactory.ParticleInfo info) {
//        com.badlogic.gdx.graphics.g3d.particles.ParticleController controller = createController(info.translate());
//        controller.init();
//        controller.start();
//        emitters.add(new ParticleEmitter(name, controller));
//        controller.translate(Vector3.X);
    }

    public void addEmitter(ParticleFactory.ParticleInfo info) {
        addEmitter(info.translate());
    }

    public void addEmitter(ParticleFactory.ParticleTranslated info) {
        com.badlogic.gdx.graphics.g3d.particles.ParticleController controller = createController(info);
        controller.init();
        controller.start();
        emitters.add(new ParticleEmitter(info, controller));
    }

    public com.badlogic.gdx.graphics.g3d.particles.ParticleController createController(ParticleFactory.ParticleTranslated info) {
        //Emission
        RegularEmitter emitter = new RegularEmitter();
        emitter.getDuration().setLow(info.duration);
        emitter.getEmission().setLow(info.emissionRange.x);
        emitter.getEmission().setHigh(info.emissionRange.y);
        emitter.getLife().setLow(info.lifeRange.x);
        emitter.getLife().setHigh(info.lifeRange.y);
        emitter.setMaxParticleCount(info.maxCount);
        if (isRunning) emitter.setEmissionMode(RegularEmitter.EmissionMode.Enabled);
        else emitter.setEmissionMode(RegularEmitter.EmissionMode.Disabled);

        //Spawn
        PointSpawnShapeValue pssv = new PointSpawnShapeValue();
        pssv.xOffsetValue.setLow(0, 1);
        pssv.xOffsetValue.setActive(true);
        pssv.yOffsetValue.setLow(0, 1);
        pssv.yOffsetValue.setActive(true);
        pssv.zOffsetValue.setLow(0, 1);
        pssv.zOffsetValue.setActive(true);
        SpawnInfluencer spawnSrc = new SpawnInfluencer(pssv);

        //Scale
        ScaleInfluencer si = new ScaleInfluencer();
        si.value.setTimeline(new float[]{info.scaleTimeline.x, info.scaleTimeline.y});
        si.value.setScaling(new float[]{info.scaleScale.x, info.scaleScale.y});
        si.value.setLow(info.scaleRange.x);
        si.value.setHigh(info.scaleRange.y);

        //Colour
        ColorInfluencer.Single cis = new ColorInfluencer.Single();
        cis.colorValue.setColors(new float[]{info.particleColour.r, info.particleColour.g, info.particleColour.b, 0, 0, 0});
        cis.colorValue.setTimeline(new float[]{0, 1});
        cis.alphaValue.setLow(info.alphaRange.x);
        cis.alphaValue.setHigh(info.alphaRange.y);
        cis.alphaValue.setTimeline(new float[]{info.alphaTimeline.x, info.alphaTimeline.y});
        cis.alphaValue.setScaling(new float[]{info.alphaScale.x, info.alphaScale.y});

        //Dynamics
        DynamicsInfluencer di = new DynamicsInfluencer();
        DynamicsModifier.BrownianAcceleration brownian = new DynamicsModifier.BrownianAcceleration();
        brownian.strengthValue.setTimeline(new float[]{info.brownianTimeline.x, info.brownianTimeline.y});
        brownian.strengthValue.setScaling(new float[]{info.brownianScale.x, info.brownianScale.y});
        brownian.strengthValue.setHigh(info.brownianRange.y);
        brownian.strengthValue.setLow(info.brownianRange.x);
        di.velocities.add(brownian);

        return new com.badlogic.gdx.graphics.g3d.particles.ParticleController(info.id,
                emitter, new BillboardRenderer(batch), new RegionInfluencer.Single(tex),
                spawnSrc, si, cis, di);
    }

    public BillboardParticleBatch render(float angle, float delta) {
        batch.begin();
        for (ParticleEmitter cont : emitters) {
            cont.move(angle, delta);
            cont.render();
        }
        batch.end();
        return batch;
    }

    static boolean isRunning = true;

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean run) {
        isRunning = run;
        switch (run + "") {
            case "true":
                for (ParticleEmitter e : emitters)
                    e.start();
                break;
            case "false":
                for (ParticleEmitter e : emitters)
                    e.stop();
                break;
        }
    }

    public void start() {
        setRunning(true);
    }

    public void stop() {
        setRunning(false);
    }

    public void toggle() {
        setRunning(!isRunning);
    }

}