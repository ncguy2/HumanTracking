package net.ncguy.tracking.display;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import javafx.beans.property.SimpleBooleanProperty;
import net.ncguy.api.ik.*;
import net.ncguy.api.loaders.LoaderHub;
import net.ncguy.skeleton.SKChain;
import net.ncguy.skeleton.SKJoint;
import net.ncguy.skeleton.SKUtils;
import net.ncguy.skeleton.SkeletonFactory;
import net.ncguy.tracking.TrackerPipeline;
import net.ncguy.tracking.display.shader.GridShader;
import net.ncguy.tracking.render.ParticleController;
import net.ncguy.tracking.render.ParticleEmitter;
import net.ncguy.tracking.render.ParticleFactory;
import net.ncguy.tracking.utils.ShaderPreprocessor;
import net.ncguy.tracking.utils.TransformStack;
import net.ncguy.tracking.world.Node;
import net.ncguy.ui.detachable.DetachableWindow;
import net.ncguy.ui.detachable.IDetachableContainer;
import net.ncguy.ui.detachable.Sidebar;
import net.ncguy.ui.scene3d.AnchoredLabel;
import net.ncguy.utils.FileUtils;
import net.ncguy.utils.MathsUtils;
import net.ncguy.utils.Reference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static com.badlogic.gdx.graphics.GL20.GL_LINES;
import static net.ncguy.utils.Reference.Colour.RendererColours.BONE_LOCATION_ACTIVE_COLOUR;
import static net.ncguy.utils.Reference.Colour.RendererColours.BONE_LOCATION_COLOUR;

public class TrackingSpace extends AbstractScreen {

    public static boolean useCodedSkeleton = true;

    Environment environment;
    PerspectiveCamera camera;
    ModelBatch modelBatch;
    Model gridMesh;
    ShaderProgram gridShader;
    GridShader shader;
    Texture texture;
    FirstPersonCameraController ctrlr;
    Node.ModularModelInstance skySphere;
    Node rootNode;

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

    ModelInstance coneInstance;

    Sidebar leftSidebar;
    Sidebar rightSidebar;

    SKJoint skStructure;

    List<AnchoredLabel> labels = new ArrayList<>();

    public static SimpleBooleanProperty showBoneLocations = new SimpleBooleanProperty(true);
    public static SimpleBooleanProperty showBoneDirections = new SimpleBooleanProperty(false);
    public static SimpleBooleanProperty showBoneConnections = new SimpleBooleanProperty(true);

    @Override
    public void show() {
        super.show();
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(10, 10, 10);
        camera.lookAt(0, 0 ,0);
        camera.up.set(0, 1, 0);
        camera.near = 0.1f;
        camera.far = 1024.f;
        camera.update();

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

        texture = new Texture(Gdx.files.internal("textures/houseSphere.jpg"));
        Material skyMtl = new Material(TextureAttribute.createDiffuse(texture));
        skySphere = new Node.ModularModelInstance(mb.createSphere(-64, -64, -64, 64, 64, skyMtl, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates));

        skySphere.transform.setToRotation(1, 0, 0, 180);
        skySphere.transform.scale(10, 10, 10);

        rootNode.AddChild(new Node(gridMesh).Name("Grid").Translate(1, 0, 0));
        rootNode.AddChild(movingNode.Name("Sphere"));
        rootNode.AddChild(movingNodeB.Name("Box"));
        rootNode.AddChild(movingNodeC.Name("Cone"));
        Node skySphere = new Node(this.skySphere).Name("Sky sphere");
        skySphere.isVisible = false;
        rootNode.AddChild(skySphere);

//        String modelPath = "models/Peggy/Peggy.fbx";
//        String modelPath = "models/PeasantGirl/peasant_girl.fbx";
//        String modelPath = "models/Arissa/Arissa.fbx";
//        String modelPath = "models/ArissaScaled/ArissaScaled.fbx";
//        String modelPath = "models/Hand/Hand.fbx";
        String modelPath = "models/Nightshade/nightshade_j_friedrich.fbx";
//        String modelPath = "models/Peggy/Peggy2.fbx";
//        String modelPath = "models/MH_Female/MH_female.fbx";
//        String modelPath = "models/BadBetty/BAd_Betty.fbx";
//        String modelPath = "models/Ship/ship.obj";
//        String modelPath = "models/Catwoman/catwoman.fbx";
//        String modelPath = "models/Female dress/dress.fbx";
//        String modelPath = "models/Girl/girl model.obj";

        Model skeletonModel = FileUtils.LoadModel(modelPath);
        Node skeletonNode = new Node(skeletonModel).Name("SkeletonMesh");
//        skeletonNode.GetTransform().scale(.1f, .1f, .1f);
//        skeletonNode.GetTransform().translate(5, 0, 0);
        rootNode.AddChild(skeletonNode);

//        System.out.println(skeletonNode.GetInstance().nodes.first().id);

        Array<com.badlogic.gdx.graphics.g3d.model.Node> nodes = skeletonNode.GetInstance().nodes;

        Scanner scanner = new Scanner(System.in);

        if(nodes.size > 0) {
            com.badlogic.gdx.graphics.g3d.model.Node first = nodes.first();
            com.badlogic.gdx.graphics.g3d.model.Node root = FindRootNode(first);

            System.out.println(root != null ? root.id : "null");

            if(root != null) {
                PrintNode(root, "");
                System.out.print("Root node: ");
                controlledBoneStart = scanner.nextLine();
                System.out.print("Target Effector: ");
                controlledBoneEffector = scanner.nextLine();
                boneStructure = BuildNode(root, skeletonNode.GetInstance());

                SKUtils.labels = labels;
                SKUtils.font = font;

                skStructure = SKUtils.FromNodeTree(root, skeletonNode.GetInstance());
                String print = skStructure.Print();
                try {
                    Files.write(new File("SKStructure.txt").toPath(), print.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }else{
            System.out.println("No bones detected");
            scanner.next();
        }

//        skeletonNode.AddChild(new Node(sphereModel));

        stage.worldNodes.addAll(rootNode.children);


        ctrlr = new FirstPersonCameraController(camera);
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, ctrlr));

        AbstractScreen.rootNode.set(rootNode);

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

        stage.addActor(leftSidebar.Resize(stage, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        stage.addActor(rightSidebar.Resize(stage, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        String s = boneStructure.ToString();
        try {
            Files.write(new File("bones.txt").toPath(), s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, -1.0f, -0.8f));

        trackerPipeline = new TrackerPipeline();

        Load();

        LoaderHub.trackerLoader.Iterate(trackerPipeline::add);
        trackerPipeline.StartTracker();
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

    Map<String, Texture> skySphereTextures = new HashMap<>();

    public void SetSkySphereTexture(int index) {
        TextureAttribute attr = (TextureAttribute) skySphere.materials.get(0).get(TextureAttribute.Diffuse);

        String[] paths = new String[] {
                "textures/default.png",
                "textures/houseSphere.jpg",
                "textures/spaceSphere.jpg",
                "textures/townSphere.jpg",
                "textures/winterSphere.jpg",
        };

        String path = paths[index % paths.length];

        if(skySphereTextures.containsKey(path))
            texture = skySphereTextures.get(path);
        else {
            texture = new Texture(Gdx.files.internal(path));
            skySphereTextures.put(path, texture);
        }
        attr.textureDescription.texture = texture;
    }

    float runTime = 0;
    @Override
    protected void Render_Impl(float delta) {

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

        camera.up.set(0, 1, 0);
        ctrlr.update(Gdx.graphics.getDeltaTime());
        modelBatch.begin(camera);
        rootNode.Update();
        rootNode.Render(modelBatch, environment, new TransformStack());
        modelBatch.render(particleController.render(0, delta));
        modelBatch.end();

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
            trackerPipeline.ApplyCurrentBuffer(skStructure);
        }

        if(LoaderHub.ikLoader.HasActiveSolver()) {
            IIKSolver solver = LoaderHub.ikLoader.activeSolver.get();
            if(solver.SupportsSKJoints()) {

                SKJoint start = skStructure.Find(controlledBoneStart);
                SKJoint end = skStructure.Find(controlledBoneEffector);
                end.SetTarget(cursorBoneTarget);

                solver.Solve(new SKChain(start, end));

            }else if(solver.SupportsTree()) {
                BoneNode start = boneStructure.Find(controlledBoneStart);
                BoneChain bone = start.FindChain(controlledBoneEffector, true);
                Bone last = bone.Last();
                last.SetTarget(cursorBoneTarget);

                solver.Solve(BoneNode.FromBoneChain(bone), Collections.singletonList(cursorBoneTarget));

//                solver.Solve(ball_r, cursorBoneTarget);
            }else solver.Solve(boneChain, cursorBoneTarget);
        }

        destinationEffect.part.position.set(cursorBoneTarget.cpy().sub(.5f));

        renderer.setProjectionMatrix(camera.combined);
//        RenderBoneChain(boneChain);
//        RenderBoneStructure(boneStructure);

//        RenderBones(boneStructure);

        boolean drawWithMesh = false;
        if(drawWithMesh) {
            modelBatch.begin(camera);
            SKUtils.RenderMesh(skStructure, modelBatch, environment);
            modelBatch.end();
        }else SKUtils.Render(skStructure, renderer);
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

        renderer = new ShapeRenderer();

        leftSidebar.Resize(stage, width, height);
        rightSidebar.Resize(stage, width, height);

    }
}
