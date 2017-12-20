package net.ncguy.tracking.display;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import net.ncguy.tracking.geometry.*;
import net.ncguy.tracking.utils.ShaderPreprocessor;
import net.ncguy.tracking.utils.data.VisitableTree;
import net.ncguy.tracking.utils.data.helpers.TreeObjectWrapper;
import net.ncguy.tracking.xml.XMLParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SDFExperiment extends AbstractScreen {

    PerspectiveCamera camera;
    SpriteBatch batch;
    Sprite sprite;
    ShaderProgram shader;
    FirstPersonCameraController ctrlr;

    List<Model> models;
    VisitableTree<TreeObjectWrapper<ModelWrapper>> modelTree;
    List<GeometryItem.GLItem> glItems;
    int rootIndex;
    PingPongFBO fbo;
    Texture defaultTexture;


    @Override
    public void show() {
        super.show();

        DefaultGeometry boxGeometry = new DefaultGeometry(GeometryTypes.BOX, new BaseGeometry.Vec4(0.8f), new BaseGeometry.Vec4(), 1.f);

        models = new ArrayList<>();

        List<ModelWrapper> modelRefs = new ArrayList<>();


        float cylinderRad = .4f + (1.f - .4f) * (1.f + MathUtils.sin(1.7f * 0.f)) / 2.f;
        new DefaultGeometry(GeometryTypes.CYLINDER_CAPPED, new BaseGeometry.Vec4(2.f, cylinderRad, 0.f, 0.f), new BaseGeometry.Vec4(), 0.f);


        Model m1 = new Model(new DefaultGeometry(GeometryTypes.BOX, new BaseGeometry.Vec4(0.8f), new BaseGeometry.Vec4(), .5f), Model.Operation.NONE, Color.RED);
        Model m2 = new Model(new DefaultGeometry(GeometryTypes.SPHERE, new BaseGeometry.Vec4(), new BaseGeometry.Vec4(), 1.f), Model.Operation.INTERSECTION, Color.GREEN);
        Model m3 = new Model(new DefaultGeometry(GeometryTypes.BOX, new BaseGeometry.Vec4(0.8f), new BaseGeometry.Vec4(), 1.5f), Model.Operation.DIFFERENCE, Color.BLUE);
        Model m4 = new Model(new DefaultGeometry(GeometryTypes.CYLINDER_CAPPED, new BaseGeometry.Vec4(.2f, 2.f, 0.0f, 0.0f), new BaseGeometry.Vec4(), 2.f), Model.Operation.UNION, Color.CYAN);
        Model m5 = new Model(new DefaultGeometry(GeometryTypes.BOX, new BaseGeometry.Vec4(5.f), new BaseGeometry.Vec4(), 2.5f), Model.Operation.UNION, Color.WHITE);

        m3.isDynamic = true;
        m5.translation.set(0, -5.1f, 0);

        models.addAll(Arrays.asList(m1, m2, m3, m4, m5));

//        m4.rotation.setEulerAngles(90, 0, 0);
//        m5.rotation.setEulerAngles(0, 90, 0);

//        modelRefs.add(new ModelWrapper(m1, "l/l"));
//        modelRefs.add(new ModelWrapper(m2, "l/r"));
//
//        modelRefs.add(new ModelWrapper(m3, "r/l"));
//        modelRefs.add(new ModelWrapper(m4, "r/r/l"));
//        modelRefs.add(new ModelWrapper(m5, "r/r/r"));
//
//        modelTree = new VisitableTree<>(new TreeObjectWrapper<>("Root"));
//
//        TreePopulator.Populate(modelTree, modelRefs, '/', n -> n.path);
//
//        modelTree.Accept(new PrintIndentedVisitor<>(0, System.out::print));
//
        List<GeometryItem> items = new ArrayList<>();
//
//        modelTree.Accept(new GeometryFlattenVisitor(null, items));
//
//        GeometryItem rootItem = items.get(0);

        GeometryItem rootItem = XMLParser.ParseXML(new File("sdf.xml"), items);

        glItems = new ArrayList<>();

//        Flatten(glItems, rootItem, items, models);

//        rootIndex = glItems.indexOf(glItems.stream().filter(i -> i.parent <= -1).findFirst().get());

        batch = new SpriteBatch();

        defaultTexture = new Texture(Gdx.files.internal("textures/default.png"));
        defaultTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        sprite = new Sprite(defaultTexture);

        camera = new PerspectiveCamera(90, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.0f;
        camera.far = 100.f;
        camera.position.set(0, 0, 3);
        camera.lookAt(0, 0, 0);
        camera.update();

        fbo = new PingPongFBO(false);

        LoadShader();

        ctrlr = new FirstPersonCameraController(camera);
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, ctrlr));
    }

    public void LoadShader() {

        if(shader != null)
            shader.dispose();

        String vertSrc = ShaderPreprocessor.LoadShader(Gdx.files.internal("shaders/sdf.vert"), "vert.txt");
        String fragSrc = ShaderPreprocessor.LoadShader(Gdx.files.internal("shaders/sdf.old.frag"), "frag.txt");

        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(vertSrc, fragSrc);
        String log = shader.getLog();

        if(!log.isEmpty())
            System.err.println(log);

        shader.begin();
        shader.setUniformf("rayStart", camera.near);
        shader.setUniformf("rayEnd", camera.far);
        shader.setUniformf("screenSize", camera.viewportWidth, camera.viewportHeight);
        shader.setUniformf("fov", camera.fieldOfView);
        shader.setUniformf("epsilon", 0.0001f);
        shader.setUniformi("maxSteps", 255);
        ShaderPreprocessor.BindModelSetToShader(shader, "models", models, true);
        shader.end();
    }

    public void Flatten(List<GeometryItem.GLItem> items, GeometryItem item, List<GeometryItem> availableItems, List<Model> availableModels) {
        GeometryItem.GLItem glItem = new GeometryItem.GLItem();
        glItem.data = availableModels.indexOf(item.data);
        glItem.operation = item.operation.ordinal();
        glItem.parent = item.parent != null ? availableItems.indexOf(item.parent) : -1;
        glItem.path = item.path;

        if(item.left != null) {
            glItem.leftItem = availableItems.indexOf(item.left);
            Flatten(items, item.left, availableItems, availableModels);
        }else glItem.leftItem = -1;

        if(item.right != null) {
            glItem.rightItem = availableItems.indexOf(item.right);
            Flatten(items, item.right, availableItems, availableModels);
        }else glItem.rightItem = -1;

        items.add(glItem);

    }

    float runTime = 0.0f;
    @Override
    protected void Render_Impl(float delta) {

        if(Gdx.input.isKeyJustPressed(Input.Keys.R))
            LoadShader();

        float cylinderRad = .4f + (1.f - .4f) * (1.f + MathUtils.sin(1.7f * 0.f)) / 2.f;

        ctrlr.update(delta);


        long startTime = System.currentTimeMillis();

        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1))
            models.get(2).operation = Model.Operation.NONE;
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2))
            models.get(2).operation = Model.Operation.UNION;
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_3))
            models.get(2).operation = Model.Operation.INTERSECTION;
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_4))
            models.get(2).operation = Model.Operation.DIFFERENCE;

//        boxModel.operation = Model.Operation.UNION;
        runTime += delta;
        float scale = MathUtils.sin(runTime * 0.75f);

        models.get(2).scale.set(scale, scale * 3, 5);
        batch.setShader(shader);

        batch.begin();

        ShaderPreprocessor.BindModelSetToShader(shader, "models", models);
        shader.setUniformMatrix("view", camera.view);
        shader.setUniformf("position", camera.position);
        shader.setUniformf("up", camera.up);
        shader.setUniformf("viewRayDirection", camera.direction);
        shader.setUniformf("time", runTime);

        batch.draw(defaultTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.end();

        batch.setShader(null);

        long endTime = System.currentTimeMillis();

//        System.out.printf("Time taken: %sms\n", endTime - startTime);

    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        camera.update(true);
        fbo.Resize(width, height);

        shader.begin();
        shader.setUniformf("rayStart", camera.near);
        shader.setUniformf("rayEnd", camera.far);
        shader.setUniformf("screenSize", camera.viewportWidth, camera.viewportHeight);
        shader.setUniformf("fov", camera.fieldOfView);
        shader.end();

    }

    public static class ModelWrapper {
        public Model model;
        public String path;

        public ModelWrapper(Model model, String path) {
            this.model = model;
            this.path = path;
        }

        @Override
        public String toString() {
            return path;
        }
    }

}
