package net.ncguy.tracking.display;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import javafx.beans.property.SimpleBooleanProperty;
import net.ncguy.api.ik.*;
import net.ncguy.api.loaders.LoaderHub;
import net.ncguy.skeleton.*;
import net.ncguy.tracking.Launcher;
import net.ncguy.tracking.TrackerPipeline;
import net.ncguy.tracking.display.shader.GridShader;
import net.ncguy.tracking.render.ParticleController;
import net.ncguy.tracking.render.ParticleEmitter;
import net.ncguy.tracking.render.ParticleFactory;
import net.ncguy.tracking.utils.ShaderPreprocessor;
import net.ncguy.tracking.utils.TransformStack;
import net.ncguy.tracking.world.Node;
import net.ncguy.ui.StatusBar;
import net.ncguy.ui.TaskWatcher;
import net.ncguy.ui.detachable.DetachableWindow;
import net.ncguy.ui.detachable.IDetachableContainer;
import net.ncguy.ui.detachable.Sidebar;
import net.ncguy.ui.scene3d.AnchoredLabel;
import net.ncguy.utils.*;
import net.ncguy.utils.task.Task;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import static com.badlogic.gdx.graphics.GL20.GL_DEPTH_TEST;
import static com.badlogic.gdx.graphics.GL20.GL_LINES;
import static net.ncguy.skeleton.TrackedBones.skeletalNode;
import static net.ncguy.tracking.AppListener.renderTasks;
import static net.ncguy.utils.Reference.Colour.RendererColours.BONE_LOCATION_ACTIVE_COLOUR;
import static net.ncguy.utils.Reference.Colour.RendererColours.BONE_LOCATION_COLOUR;

public class TrackingSpace extends AbstractScreen {

    public static boolean useCodedSkeleton = true;
    public static boolean modelReloadRequested = false;

    public static String currentLoadedModel = "";

    static Consumer<String> setModelFunc;
    public static void SelectModel(String modelPath) {
        if(setModelFunc != null)
            setModelFunc.accept(modelPath);
    }

    SpriteBatch batch;
    FrameBuffer shapeFbo;
    ShaderProgram shapeRenderShader;
    Texture texture;

    Environment environment;
    PerspectiveCamera camera;
    ModelBatch modelBatch;
    Model gridMesh;
    ShaderProgram gridShader;
    GridShader shader;;
    FirstPersonCameraController ctrlr;
    Node.ModularModelInstance skySphere;
    Node rootNode;
    StatusBar statusBar;
    TaskWatcher taskWatcher;

    TrackerPipeline trackerPipeline;

    String controlledBoneEffector = "";
    String controlledBoneStart = "";

    ShapeRenderer renderer;

    BoneNode boneStructure;
    BoneChain boneChain;
    Vector3 cursorBoneTarget = new Vector3();
    ParticleController particleController;
    ParticleEmitter destinationEffect;

    Node movingNode;
    Node movingNodeB;
    Node movingNodeC;

    PointLight pointLight;
    ModelInstance coneInstance;

    Sidebar leftSidebar;
    Sidebar rightSidebar;

    SKJoint skStructure;

    List<AnchoredLabel> labels = new ArrayList<>();

    public static SimpleBooleanProperty showBoneLocations = new SimpleBooleanProperty(true);
    public static SimpleBooleanProperty showBoneDirections = new SimpleBooleanProperty(false);
    public static SimpleBooleanProperty showBoneConnections = new SimpleBooleanProperty(true);

    private Launcher.FileDropListener onFilesDroppedFunc = this::OnFilesDropped;

    public void InitShapeRenderShader() {
        String shapeVert = ShaderPreprocessor.LoadShader(Gdx.files.internal("shaders/shape/shape.vert"));
        String shapeFrag = ShaderPreprocessor.LoadShader(Gdx.files.internal("shaders/shape/shape.frag"));
        shapeRenderShader = new ShaderProgram(shapeVert, shapeFrag);

        if(!shapeRenderShader.isCompiled()) {
            System.out.println(shapeRenderShader.getLog());
        }
    }

    @Override
    public void show() {

        Launcher.fileDroppedListeners.add(onFilesDroppedFunc);

        super.show();

        batch = new SpriteBatch();

        shapeFbo = CreateFrameBuffer(Pixmap.Format.RGBA8888, 1, this.width, this.height, false);

        InitShapeRenderShader();

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(10, 10, 10);
        camera.lookAt(0, 0 ,0);
        camera.up.set(0, 1, 0);
        camera.near = 0.1f;
        camera.far = 1024.f;
        camera.update();

        shapeRenderShader.begin();
        shapeRenderShader.setUniformf("u_near", camera.near);
        shapeRenderShader.setUniformf("u_far", camera.far);
        shapeRenderShader.end();

        String vertSrc = ShaderPreprocessor.LoadShader(Gdx.files.internal("shaders/grid/grid.vert"));
        String fragSrc = ShaderPreprocessor.LoadShader(Gdx.files.internal("shaders/grid/grid.frag"));

        gridShader = new ShaderProgram(vertSrc, fragSrc);

        Material mtl = new Material(ColorAttribute.createDiffuse(new Color(0.3f, 0.3f, 0.3f, 1.f)));
        Material mtl2 = new Material(ColorAttribute.createDiffuse(Color.GREEN.cpy()));

        ModelBuilder mb = new ModelBuilder();
        gridMesh = mb.createLineGrid(1024, 1024, 1, 1, mtl, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        Model sphereModel = mb.createSphere(1, 1, 1, 8, 8, mtl, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        movingNode = new Node(sphereModel);
        movingNodeB = new Node(mb.createBox(1, 1, 1, mtl, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal));
        movingNodeC = new Node(mb.createCone(1, 1, 1,8, mtl, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal));

        Model cone = mb.createCone(1, 1, 1, 4, GL_LINES, mtl2, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        coneInstance = new ModelInstance(cone);

        modelBatch = new ModelBatch();

        rootNode = new Node(new Node.ModularModelInstance(mb.createXYZCoordinates(3, mtl, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked)));

        Material skyMtl = new Material();
        skySphere = new Node.ModularModelInstance(mb.createSphere(-64, -64, -64, 64, 64, skyMtl, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates));

        AssetUtils.instance().GetAsync("textures/houseSphere.jpg", Texture.class, t -> {
            texture = t;
            skyMtl.set(TextureAttribute.createDiffuse(texture));
        });

        skySphere.transform.setToRotation(1, 0, 0, 180);
        skySphere.transform.scale(10, 10, 10);

        rootNode.AddChild(new Node(gridMesh).Name("Grid").Translate(1, 0, 0));
        rootNode.AddChild(movingNode.Name("Sphere"));
        rootNode.AddChild(movingNodeB.Name("Box"));
        rootNode.AddChild(movingNodeC.Name("Cone"));
        Node skySphere = new Node(this.skySphere).Name("Sky sphere");
        skySphere.isVisible = false;
        rootNode.AddChild(skySphere);

        String modelPath = Launcher.modelPath;

        LoadModel(modelPath);

//
//        Model skeletonModel = FileUtils.LoadModel(modelPath);
//        Node skeletonNode = new Node(skeletonModel).Name("SkeletonMesh");
//        rootNode.AddChild(skeletonNode);

//        System.out.println(skeletonNode.GetInstance().nodes.first().id);


//        Scanner scanner = new Scanner(System.in);
//
//        Array<com.badlogic.gdx.graphics.g3d.model.Node> nodes = skeletonNode.GetInstance().nodes;
//        if(nodes.size > 0) {
//            com.badlogic.gdx.graphics.g3d.model.Node first = nodes.first();
//            com.badlogic.gdx.graphics.g3d.model.Node root = FindRootNode(first);
//
//            System.out.println(root != null ? root.id : "null");
//
//            if(root != null) {
//                PrintNode(root, "");
//                boneStructure = BuildNode(root, skeletonNode.GetInstance());
//
//                SKUtils.labels = labels;
//                SKUtils.font = font;
//
//                skStructure = SKUtils.FromNodeTree(root, skeletonNode.GetInstance());
//            }
//
//        }else{
//            System.out.println("No bones detected");
//            scanner.next();
//        }

//        skeletonNode.AddChild(new Node(sphereModel));

        stage.worldNodes.addAll(rootNode.children);


        ctrlr = new FirstPersonCameraController(camera);
        ctrlr.setVelocity(75);
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, ctrlr));

        AbstractScreen.rootNode.set(rootNode);

//        renderer = new ShapeRenderer(5000, shapeRenderShader);
        renderer = new ShapeRenderer();

        int desiredBoneCount = 256;
        float boneLength = .1f;

//        Bone[] bones = new Bone[desiredBoneCount];
//        for (int i = 0; i < desiredBoneCount; i++)
//            bones[i] = new Bone("bone"+i, 0, i * boneLength, boneLength);


        cursorBoneTarget.set(camera.position);

//        LoadBoneStructure();

        particleController = ParticleController.build(camera);
        particleController.addEmitter(new ParticleFactory.ParticleInfo());
        destinationEffect = particleController.getEmitter(0);
        destinationEffect.part.scaleScale.x = 0.05f;
        destinationEffect.part.scaleScale.y = 0.05f;
        destinationEffect.part.brownianScale.x = 0.f;
        destinationEffect.part.brownianScale.y = 0.f;

        leftSidebar = new Sidebar(true);
        rightSidebar = new Sidebar(false);

        statusBar = new StatusBar(true);
        stage.SetStatusBar(statusBar);
        stage.addActor(statusBar.GetRootTable());

        taskWatcher = new TaskWatcher(stage);
        statusBar.AddToRight(taskWatcher.GetRootTable());

        float menuBarHeight = stage.GetMenuBar().getTable().getHeight();

        stage.addActor(leftSidebar.Resize(stage, Gdx.graphics.getWidth(), (int) (Gdx.graphics.getHeight() - (statusBar.getHeight() + menuBarHeight))));
        stage.addActor(rightSidebar.Resize(stage, Gdx.graphics.getWidth(), (int) (Gdx.graphics.getHeight() - (statusBar.getHeight() + menuBarHeight))));

        stage.SetLeftSidebar(leftSidebar);
        stage.SetRightSidebar(rightSidebar);

        IDetachableContainer.Globals.handleDrop = (container, vector2) -> {
            Gdx.app.log("INFO", "Tab dropped at "+vector2.toString()+", spawning window");
            final Optional<DetachableWindow> window = container.To(DetachableWindow.class);
            window.ifPresent(w -> {
                stage.addActor(w.fadeIn());
                w.setPosition(vector2.x, vector2.y, Align.top);
            });
        };

        stage.SetBoneStructureSetter(newStructure -> boneStructure = newStructure);
        stage.SetSkySphereSetter(this::SetSkySphereTexture);

        pointLight = new PointLight();
        pointLight.color.set(.8f, .8f, .8f, 1.f);
        pointLight.intensity = 50.f;

        environment = new Environment();
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, -1.0f, -0.8f));
        environment.add(pointLight);
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
        environment.set(new ColorAttribute(ColorAttribute.Emissive, 1, 1, 1, 1));
//        environment.set(new DepthTestAttribute(GL20.GL_LESS, camera.near, camera.far));

        trackerPipeline = new TrackerPipeline();

        setModelFunc = this::LoadModel;

        Load();

        LoaderHub.trackerLoader.Iterate(trackerPipeline::add);
        trackerPipeline.StartTracker();

        Launcher.PostNotification("Tracking Space", "Tracking space is ready for use", TrayIcon.MessageType.INFO);
    }

    final String[] validFileExtensions_conversion = new String[] {
            ".obj",
            ".fbx",
    };
    final String[] validFileExtensions_asis = new String[] {
            ".g3dj",
            ".g3db",
    };

    final List<String> validFileExtensions_convList = Arrays.asList(validFileExtensions_conversion);
    final List<String> validFileExtensions_asisList = Arrays.asList(validFileExtensions_asis);

    protected boolean OnFilesDropped(final String[] files, int screenX, int screenY) {
        if(files.length != 1) {
            System.err.println("Incorrect amount of files dropped, Only drop the model file to import a model");
            return false;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))
            return false;

        String file = files[0];
        if(!LoadModel(file))
            return false;
        Launcher.PostNotification("Imported new model", file, TrayIcon.MessageType.INFO);
        return true;
    }

    protected boolean LoadModel(String modelPath) {

        Task task = stage.AddTask("Loading model: " + modelPath);

        String ext = modelPath.substring(modelPath.lastIndexOf('.'));

        if(!(validFileExtensions_convList.contains(ext) || validFileExtensions_asisList.contains(ext))) {
            Launcher.PostNotification("Invalid file extension", ext + " is an unsupported file type", TrayIcon.MessageType.WARNING);
            task.progress = 5;
            return false;
        }

        Node node = rootNode.FindChild("SkeletonMesh");
        if(node != null) {
            rootNode.RemoveChild(node);
            stage.worldNodes.remove(node);
            skeletalNode.set(null);
        }

        currentLoadedModel = modelPath;

        FileUtils.LoadModelAsync(modelPath, !validFileExtensions_asisList.contains(ext), skeletonModel -> {
            Node skeletonNode = new Node(skeletonModel).Name("SkeletonMesh");

            rootNode.AddChild(skeletonNode);
            stage.worldNodes.add(skeletonNode);

            Array<com.badlogic.gdx.graphics.g3d.model.Node> nodes = skeletonNode.GetInstance().nodes;
            if(nodes.size > 0) {
                com.badlogic.gdx.graphics.g3d.model.Node first = nodes.first();
                com.badlogic.gdx.graphics.g3d.model.Node root = FindRootNode(first);
                skStructure = SKUtils.FromNodeTree(root, skeletonNode.GetInstance());

                TrackedBones.rootBone.set(skStructure);
            }

            skeletalNode.set(skeletonNode);

            stage.RemoveTask(task);
        });

        return true;


//        Model skeletonModel = FileUtils.LoadModel(modelPath, !validFileExtensions_asisList.contains(ext));
//        Node skeletonNode = new Node(skeletonModel).Name("SkeletonMesh");
//        rootNode.AddChild(skeletonNode);
//        stage.worldNodes.add(skeletonNode);
//
//        Array<com.badlogic.gdx.graphics.g3d.model.Node> nodes = skeletonNode.GetInstance().nodes;
//        if(nodes.size > 0) {
//            com.badlogic.gdx.graphics.g3d.model.Node first = nodes.first();
//            com.badlogic.gdx.graphics.g3d.model.Node root = FindRootNode(first);
//            skStructure = SKUtils.FromNodeTree(root, skeletonNode.GetInstance());
//
//            TrackedBones.rootBone.set(skStructure);
//        }
    }

    private BoneNode BuildNode(com.badlogic.gdx.graphics.g3d.model.Node root, ModelInstance instance) {
        return BuildNode(root, instance, null);
    }

    private BoneNode BuildNode(com.badlogic.gdx.graphics.g3d.model.Node node, ModelInstance instance, BoneNode parent) {
        BoneNode boneNode = new BoneNode(new MeshBone(node, instance), parent);

        Vector3 nodePos = ((MeshBone) boneNode.bone).GetRawPosition();
        Vector3 origin = Vector3.Zero;
        if(parent != null) {
            Vector3 parentPos = parent.bone.GetPosition();


            Vector3 forward = nodePos.cpy().sub(parentPos).nor();
            Vector3 right = forward.cpy().crs(Vector3.Y);
            Vector3 up = forward.cpy().crs(right);

            origin.set(nodePos.cpy().sub(right.cpy().scl(3)));
        }

//        AnchoredLabel anchoredLabel = new AnchoredLabel(node.id, nodePos, origin, font);
//        anchoredLabel.subTextSupplier = () -> String.valueOf(boneNode.bone.Length());
//        labels.add(anchoredLabel);

        if(node.hasChildren())
            node.getChildren().forEach(c -> BuildNode(c, instance, boneNode));

        return boneNode;
    }

    public com.badlogic.gdx.graphics.g3d.model.Node FindRootNode(com.badlogic.gdx.graphics.g3d.model.Node node){
        if(node == null)
            return null;
        if(node.hasParent())
            return FindRootNode(node.getParent());
        return node;
    }

    public void PrintNode(com.badlogic.gdx.graphics.g3d.model.Node node, String prefix) {
        System.out.printf("%s%s\n", prefix, node.id);

        if(node.hasChildren())
            node.getChildren().forEach(c -> PrintNode(c, prefix + "  "));
    }

    public void LoadBoneStructure() {
        Runnable task;
        if(useCodedSkeleton)
            task = this::LoadBoneStructure_Coded;
        else task = this::LoadBoneStructure_File;

        task.run();
    }

    public void LoadBoneStructure_File() {
        File sklFile = new File("skeleton/torso.skl");
        boneStructure = new SkeletonFactory(sklFile).Parse();
    }

    public void LoadBoneStructure_Coded() {

        BoneNode abdomen = new BoneNode(new Bone("Abdomen", 0, 0, 0, 2));
        BoneNode neck = new BoneNode(new Bone("Neck", 0, 2, 0, 1), abdomen);
        BoneNode head = new BoneNode(new Bone("Head", 0, 3, 0, 1), neck);

        BoneNode rShoulder = new BoneNode(new Bone("RShoulder", 1, 1, 0, 1), neck);
        BoneNode rElbow = new BoneNode(new Bone("RElbow", 2, 1, 0, 1), rShoulder);
        BoneNode rWrist = new BoneNode(new Bone("RWrist", 2, 2, 0, 1), rElbow);

        BoneNode lShoulder = new BoneNode(new Bone("LShoulder", -1, 1, 0, 1), neck);
        BoneNode lElbow = new BoneNode(new Bone("LElbow", -2, 1, 0, 1), lShoulder);
        BoneNode lWrist = new BoneNode(new Bone("LWrist", -2, 2, 0, 1), lElbow);

        boneStructure = abdomen;
    }

    public BoneChain BuildBoneChain(String namePrefix, Vector3 start, Vector3 end, int segments) {
        BoneChain rootChain = new BoneChain();

        Vector3 prevPos = start.cpy();

        for (int i = 0; i <= segments; i++) {
            float alpha = ((float) i) / ((float) segments);
            Vector3 pos = MathsUtils.Lerp(start, end, alpha);
            rootChain.add(new Bone(namePrefix+i, pos, prevPos.dst(pos)));
            prevPos.set(pos);
        }

        return rootChain;
    }

    public void RenderBoneConnection(Bone bone) {

        BoneNode boneNode = boneStructure.Find(bone);

        if(boneNode.parent != null)
            renderer.line(bone.GetPosition(), boneNode.parent.bone.GetPosition());

//        coneInstance.transform.setToLookAt(bone.position, bone.Extend(1), Vector3.Y);
//        coneInstance.transform.scale(bone.length, bone.length, bone.length);
//        modelBatch.render(coneInstance);
    }

    public void RenderBoneDirection(Bone bone) {

        if(bone.hasParent)
            renderer.setColor(Reference.Colour.RendererColours.BONE_DIRECTION_PARENTED_COLOUR);
        else renderer.setColor(Reference.Colour.RendererColours.BONE_DIRECTION_ORPHANED_COLOUR);

        renderer.line(bone.GetPosition(), bone.Extend(1));
    }

    public void RenderBoneLocation(Bone bone) {

        BoneNode start = boneStructure.Find(controlledBoneStart);
        BoneChain chain = start.FindChain(controlledBoneEffector, true);

        if(chain.contains(bone))
            renderer.setColor(BONE_LOCATION_ACTIVE_COLOUR);
        else renderer.setColor(BONE_LOCATION_COLOUR);

        renderer.box(bone.GetPosition().x - .05f, bone.GetPosition().y - .05f, bone.GetPosition().z + .05f, .1f, .1f, .1f);
    }

    public void RenderBones(BoneNode root) {

        Gdx.gl.glDepthFunc(GL20.GL_LESS);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);


        if(showBoneConnections.get()) {
            renderer.begin(ShapeRenderer.ShapeType.Line);
            renderer.setColor(Reference.Colour.RendererColours.BONE_CONNECTION_COLOUR);
//            modelBatch.begin(camera);
            root.Iterate(node -> RenderBoneConnection(node.bone));
//            modelBatch.end();
            renderer.end();
        }

        if(showBoneDirections.get()) {
            renderer.begin(ShapeRenderer.ShapeType.Line);
            root.Iterate(node -> RenderBoneDirection(node.bone));
            renderer.end();
        }


        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(BONE_LOCATION_COLOUR);

        if(showBoneLocations.get()) {
            root.Iterate(node -> RenderBoneLocation(node.bone));
        }

        renderer.setColor(Reference.Colour.RendererColours.IK_CURSOR_TARGET_COLOUR);
        renderer.box(cursorBoneTarget.x, cursorBoneTarget.y, cursorBoneTarget.z, .05f, .05f, .05f);

        renderer.end();

        destinationEffect.part.position.set(cursorBoneTarget.cpy().sub(.5f));
        destinationEffect.part.scaleScale.x = 0.05f;
        destinationEffect.part.scaleScale.y = 0.05f;
        destinationEffect.part.brownianScale.x = 0.f;
        destinationEffect.part.brownianScale.y = 0.f;
    }

    public void RenderBoneStructure(BoneNode node) {
        List<BoneChain> boneChains = node.GetChains(true);
        boneChains.forEach(chain -> {
            Bone first = chain.First();
            BoneNode firstNode = node.Find(first).parent;
            RenderBoneChain(chain, firstNode != null ? firstNode.bone : null);
        });
    }

    public void RenderBoneChain(BoneChain bones) {
        RenderBoneChain(bones, null);
    }
    public void RenderBoneChain(BoneChain bones, Bone rootParent) {

        if(showBoneConnections.get()) {
            renderer.begin(ShapeRenderer.ShapeType.Line);
            renderer.setColor(Reference.Colour.RendererColours.BONE_CONNECTION_COLOUR);
            bones.Iterate((here, next) -> {
                renderer.line(here.GetPosition(), next.GetPosition());
            });
            renderer.end();
        }

        if(showBoneDirections.get()) {
            renderer.begin(ShapeRenderer.ShapeType.Line);
            renderer.setColor(Color.CYAN);
            bones.Iterate(bone -> {
                renderer.line(bone.GetPosition(), bone.Extend(1));
            });
            renderer.end();
        }


        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Color.RED);

        if(showBoneLocations.get()) {
            bones.Iterate(bone -> {
                renderer.box(bone.GetPosition().x - .05f, bone.GetPosition().y - .05f, bone.GetPosition().z + .05f, .1f, .1f, .1f);
            });
        }

        renderer.setColor(Color.GOLD);
        renderer.box(cursorBoneTarget.x, cursorBoneTarget.y, cursorBoneTarget.z, .05f, .05f, .05f);

        renderer.end();

//        destinationEffect.part.scaleScale.x = 0.05f;
//        destinationEffect.part.scaleScale.y = 0.05f;
//        destinationEffect.part.brownianScale.x = 0.f;
//        destinationEffect.part.brownianScale.y = 0.f;
//        destinationEffect.part.emissionRange.x = 10.f;
//        destinationEffect.part.emissionRange.y = 250.f;
//        destinationEffect.part.maxCount = 500;
    }

    public void SetSkySphereTexture(int index) {

        String[] paths = new String[] {
                "textures/default.png",
                "textures/houseSphere.jpg",
                "textures/spaceSphere.jpg",
                "textures/townSphere.jpg",
                "textures/winterSphere.jpg",
        };

        TextureAttribute attr = (TextureAttribute) skySphere.materials.get(0).get(TextureAttribute.Diffuse);
        attr.textureDescription.texture = texture;

        String path = paths[index % paths.length];

        AssetUtils.instance().GetAsync(path, Texture.class, tex -> {
            attr.textureDescription.texture = tex;
        });
    }

    float runTime = 0;
    @Override
    protected void Render_Impl(float delta) {

        if(modelReloadRequested) {
            LoadModel(currentLoadedModel);
            modelReloadRequested = false;
        }

        delta *= Reference.runScalar.floatValue();

        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_0))
            SetSkySphereTexture(0);
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1))
            SetSkySphereTexture(1);
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2))
            SetSkySphereTexture(2);
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_3))
            SetSkySphereTexture(3);
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_4))
            SetSkySphereTexture(4);

//        Node skeletonNode = rootNode.FindChild("SkeletonMesh");
//        if(skeletonNode != null) {
//            Matrix4 transform = skeletonNode.GetTransform();
//            transform.setToScaling(.1f, .1f, .1f);
//            transform.translate(0, 0, 0);
//        }

        runTime += delta;

        float x = (float) (Math.cos(runTime) * 10f);
        float y = (float) (Math.sin(runTime * 2.f) * 10f);

        movingNode.GetTransform().setTranslation(x, 5, y);
        movingNodeB.GetTransform().setTranslation(5, x, y);
        movingNodeC.GetTransform().setTranslation(y, x, 5);

        pointLight.intensity = 50.f;
        pointLight.color.g = .4f;
        pointLight.color.b = .7f;
        pointLight.position.set(camera.position.cpy().add(camera.direction.cpy().scl(10f)));

        camera.up.set(0, 1, 0);
        ctrlr.update(Gdx.graphics.getDeltaTime());

        fbo.begin();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(0, 0, 0, 1);

        modelBatch.begin(camera);
        rootNode.Update();
        rootNode.Render(modelBatch, environment, new TransformStack());
        modelBatch.render(particleController.render(0, delta));
        modelBatch.end();
        fbo.end();

        renderer.setProjectionMatrix(stageCamera.combined);

        if(!labels.isEmpty()) {
            stage.getBatch().begin();
            labels.forEach(label -> {
                label.Update(camera);
                label.RenderText((SpriteBatch) stage.getBatch());
            });
            stage.getBatch().end();
            renderer.begin(ShapeRenderer.ShapeType.Line);
            renderer.setColor(Color.WHITE);
            labels.forEach(label -> label.RenderLines(renderer, camera));
            renderer.end();
        }

        if (Gdx.input.isTouched() && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            camera.unproject(cursorBoneTarget.set(Gdx.input.getX(), Gdx.input.getY(), 0));
            cursorBoneTarget.add(camera.direction.cpy().scl(10));
            Gdx.graphics.setTitle(cursorBoneTarget.toString());
        }

        if(LoaderHub.trackerLoader.HasActiveTracker()) {
//            ITracker tracker = LoaderHub.trackerLoader.activeTracker.get();
//            tracker.TrackAndApply(boneStructure);
            if(trackerPipeline.ShouldApplyBuffer(delta))
                trackerPipeline.ApplyCurrentBuffer(skStructure);
        }

        if(LoaderHub.ikLoader.HasActiveSolver()) {
            IIKSolver solver = LoaderHub.ikLoader.activeSolver.get();
            if(solver.SupportsSKJoints()) {

//                SKJoint start = skStructure.Find(controlledBoneStart);
//                SKJoint end = skStructure.Find(controlledBoneEffector);
//                end.SetTarget(cursorBoneTarget);
                SKJoint start = TrackedBones.HairRootBone();
                SKJoint end = TrackedBones.HairTerminalBone();

                if(ArrayUtils.NotNull(start, end))
                    solver.Solve(new SimpleSKChain(start, end));

            }else if(solver.SupportsTree()) {
                BoneNode start = boneStructure.Find(controlledBoneStart);
                BoneChain bone = start.FindChain(controlledBoneEffector, true);
                Bone last = bone.Last();
                last.SetTarget(cursorBoneTarget);

                solver.Solve(BoneNode.FromBoneChain(bone), Collections.singletonList(cursorBoneTarget));

//                solver.Solve(ball_r, cursorBoneTarget);
            }else {
                if(boneChain != null)
                    solver.Solve(boneChain, cursorBoneTarget);
            }
        }

//        destinationEffect.part.position.set(cursorBoneTarget.cpy().sub(.5f));

//        RenderBoneChain(boneChain);
//        RenderBoneStructure(boneStructure);

//        RenderBones(boneStructure);

//        shapeFbo.begin();
//        screenShader.begin();
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//
//        Gdx.gl.glActiveTexture(GL_TEXTURE10);
//        Gdx.gl.glBindTexture(GL_TEXTURE_2D, fbo.getDepthBufferHandle());
//        screenShader.setUniformi("u_depthTex", 10);
//        Gdx.gl.glActiveTexture(GL_TEXTURE0);
//        screenShader.end();
//
//        ShaderProgram defShader = stage.getBatch().getShader();
//        stage.getBatch().setShader(screenShader);
//        stage.getBatch().begin();
//        stage.getBatch().draw(defTex, 0, 0, width, height);
//        stage.getBatch().end();
//        stage.getBatch().setShader(defShader);
//        shapeFbo.end();


//        shapeRenderShader.begin();
//        shapeRenderShader.setUniformi("u_depth", 10);
//        shapeFbo.getColorBufferTexture().bind(10);

//        Gdx.gl.glDepthMask(false);
//        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//        Gdx.gl.glDepthFunc(GL20.GL_ALWAYS);
//        shapeRenderShader.end();

        renderer.setProjectionMatrix(camera.combined);
        fbo.begin();
        Gdx.gl.glEnable(GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LESS);
        SKUtils.Render(skStructure, renderer, false);
        Gdx.gl.glDepthFunc(GL20.GL_GREATER);
        SKUtils.Render(skStructure, renderer, true);
        Gdx.gl.glDisable(GL_DEPTH_TEST);

        batch.setProjectionMatrix(camera.projection);
        batch.begin();
        renderTasks.forEach(t -> t.accept(batch, camera));
        batch.end();
        fbo.end();
    }

    @Override
    protected Texture GetShapeTexture() {
        return shapeFbo.getColorBufferTexture();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.up.set(0, 1, 0);
        camera.update();

        if(renderer != null) {
            renderer.dispose();
            renderer = null;
        }

        if(shapeFbo != null) {
            shapeFbo.dispose();
            shapeFbo = null;
        }

        shapeFbo = CreateFrameBuffer(Pixmap.Format.RGBA8888, 1, this.width, this.height, false);
//        renderer = new ShapeRenderer(5000, shapeRenderShader);
        renderer = new ShapeRenderer();

        StatusBar statusBar = stage.GetStatusBar();
        float menuBarHeight = stage.GetMenuBar().getTable().getHeight();
        float statusBarHeight = statusBar.getHeight();

        leftSidebar.setY(statusBarHeight);
        rightSidebar.setY(statusBarHeight);
        leftSidebar.Resize(stage, width, (int) (height - (menuBarHeight + statusBarHeight)));
        rightSidebar.Resize(stage, width, (int) (height - (menuBarHeight + statusBarHeight)));

        statusBar.setWidth(width);
        statusBar.setPosition(0, 0);
    }

    @Override
    public void hide() {
        super.hide();
        Launcher.fileDroppedListeners.remove(onFilesDroppedFunc);
        if(shapeRenderShader != null) {
            shapeRenderShader.dispose();
            shapeRenderShader = null;
        }

        if(shapeFbo != null) {
            shapeFbo.dispose();
            shapeFbo = null;
        }
    }
}
