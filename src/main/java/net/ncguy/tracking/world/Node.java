package net.ncguy.tracking.world;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import net.ncguy.tracking.utils.TransformStack;

import java.util.*;
import java.util.function.Consumer;

public class Node implements Disposable {

    ModularModelInstance instance;
    public Set<Node> children;

    public boolean isVisible = true;
    public SimpleBooleanProperty drawWireframe = new SimpleBooleanProperty(false);

    public List<Consumer<Node>> updateTasks;
    public Queue<Consumer<Node>> singleUpdateTasks;

    public String name;

    public Node() {
        children = new HashSet<>();
        updateTasks = new ArrayList<>();
        singleUpdateTasks = new LinkedList<>();
    }

    public ModelInstance GetInstance() {
        return instance;
    }

    public Node Name(String name) {
        this.name = name;
        return this;
    }

    public Node(Model model) {
        this(new ModularModelInstance(model));
    }

    public Node(ModularModelInstance instance) {
        this();
        this.instance = instance;
//        drawWireframe.addListener((observable, oldValue, newValue) -> instance.SetPrimitiveType(newValue ? PrimitiveType.LINES : null));
    }

    public Node FindChild(String name) {

        if(this.name != null && this.name.equalsIgnoreCase(name))
            return this;

        for (Node child : children) {
            Node node = child.FindChild(name);
            if(node != null) return node;
        }

        return null;
    }

    public Node Translate(float x, float y, float z) {
        this.instance.transform.translate(x, y, z);
        return this;
    }

    public Vector3 GetTranslation() {
        Vector3 vec = new Vector3();
        GetTranslation(vec);
        return vec;
    }

    public void GetTranslation(Vector3 trans) {
        instance.transform.getTranslation(trans);
    }

    public void OnUpdate(Consumer<Node> task, boolean persistent) {
        if(persistent)
            updateTasks.add(task);
        else singleUpdateTasks.add(task);
    }

    public void Update() {
        if(!singleUpdateTasks.isEmpty())
            singleUpdateTasks.remove().accept(this);

        if(!updateTasks.isEmpty())
            updateTasks.forEach(c -> c.accept(this));

        if(!children.isEmpty())
            children.forEach(Node::Update);
    }

    public void SetInstance(ModularModelInstance instance) {
        this.instance = instance;
    }

    public void AddChild(Node node) {
        children.add(node);
    }

    public void RemoveChild(Node node) {
        children.remove(node);
    }

    public void Render(ModelBatch batch, Environment env, TransformStack transforms) {
        transforms.push(instance.transform);
        instance.transform = transforms.Transform();

        if(isVisible) {
            instance.RevertPrimitiveType();
            batch.render(instance, env);

            children.forEach(c -> {
                c.Render(batch, env, transforms);
            });
        }

        if(drawWireframe.get()) {
            instance.SetPrimitiveType(PrimitiveType.LINE_STRIP);
            batch.render(instance, env);
        }

        instance.transform = transforms.pop();
    }

    @Override
    public void dispose() {}

    public Matrix4 GetTransform() {
        return instance.transform;
    }

    public void SetAlpha(float alpha) {
        Material mtl = instance.materials.get(0);
        BlendingAttribute attr;
        if(mtl.has(BlendingAttribute.Type)) {
            attr = (BlendingAttribute) mtl.get(BlendingAttribute.Type);
            attr.opacity = alpha;
        }else{
            attr = new BlendingAttribute(alpha);
            mtl.set(attr);
        }
    }

    public static class ModularModelInstance extends ModelInstance {

        public PrimitiveType defaultPrimitiveType = PrimitiveType.NONE;
        public SimpleObjectProperty<PrimitiveType> primitiveType = new SimpleObjectProperty<>(PrimitiveType.TRIANGLES);

        public ModularModelInstance(Model model) {
            super(model);
        }

        public ModularModelInstance(Model model, String nodeId, boolean mergeTransform) {
            super(model, nodeId, mergeTransform);
        }

        public ModularModelInstance(Model model, Matrix4 transform, String nodeId, boolean mergeTransform) {
            super(model, transform, nodeId, mergeTransform);
        }

        public ModularModelInstance(Model model, String nodeId, boolean parentTransform, boolean mergeTransform) {
            super(model, nodeId, parentTransform, mergeTransform);
        }

        public ModularModelInstance(Model model, Matrix4 transform, String nodeId, boolean parentTransform, boolean mergeTransform) {
            super(model, transform, nodeId, parentTransform, mergeTransform);
        }

        public ModularModelInstance(Model model, String nodeId, boolean recursive, boolean parentTransform, boolean mergeTransform) {
            super(model, nodeId, recursive, parentTransform, mergeTransform);
        }

        public ModularModelInstance(Model model, Matrix4 transform, String nodeId, boolean recursive, boolean parentTransform, boolean mergeTransform) {
            super(model, transform, nodeId, recursive, parentTransform, mergeTransform);
        }

        public ModularModelInstance(Model model, Matrix4 transform, String nodeId, boolean recursive, boolean parentTransform, boolean mergeTransform, boolean shareKeyframes) {
            super(model, transform, nodeId, recursive, parentTransform, mergeTransform, shareKeyframes);
        }

        public ModularModelInstance(Model model, String... rootNodeIds) {
            super(model, rootNodeIds);
        }

        public ModularModelInstance(Model model, Matrix4 transform, String... rootNodeIds) {
            super(model, transform, rootNodeIds);
        }

        public ModularModelInstance(Model model, Array<String> rootNodeIds) {
            super(model, rootNodeIds);
        }

        public ModularModelInstance(Model model, Matrix4 transform, Array<String> rootNodeIds) {
            super(model, transform, rootNodeIds);
        }

        public ModularModelInstance(Model model, Matrix4 transform, Array<String> rootNodeIds, boolean shareKeyframes) {
            super(model, transform, rootNodeIds, shareKeyframes);
        }

        public ModularModelInstance(Model model, Vector3 position) {
            super(model, position);
        }

        public ModularModelInstance(Model model, float x, float y, float z) {
            super(model, x, y, z);
        }

        public ModularModelInstance(Model model, Matrix4 transform) {
            super(model, transform);
        }

        public ModularModelInstance(ModelInstance copyFrom) {
            super(copyFrom);
        }

        public ModularModelInstance(ModelInstance copyFrom, Matrix4 transform) {
            super(copyFrom, transform);
        }

        public ModularModelInstance(ModelInstance copyFrom, Matrix4 transform, boolean shareKeyframes) {
            super(copyFrom, transform, shareKeyframes);
        }

        @Override
        public Renderable getRenderable(Renderable out, com.badlogic.gdx.graphics.g3d.model.Node node, NodePart nodePart) {
            super.getRenderable(out, node, nodePart);

            if(defaultPrimitiveType.equals(PrimitiveType.NONE)) {
                defaultPrimitiveType = PrimitiveType.FromId(out.meshPart.primitiveType);
                primitiveType.set(defaultPrimitiveType);
            }

            out.meshPart.primitiveType = primitiveType.get().id;

            return out;
        }

        public void RevertPrimitiveType() {
            SetPrimitiveType(null);
        }

        public void SetPrimitiveType(PrimitiveType type) {
            if(type == null)
                type = defaultPrimitiveType;
            primitiveType.set(type);
        }

    }

    public static enum PrimitiveType {
        NONE(0),
        POINTS(GL20.GL_POINTS),
        LINES(GL20.GL_LINES),
        LINE_STRIP(GL20.GL_LINE_STRIP),
        TRIANGLES(GL20.GL_TRIANGLES),
        TRIANGLE_FAN(GL20.GL_TRIANGLE_FAN),
        TRIANGLE_STRIP(GL20.GL_TRIANGLE_STRIP),
        ;

        public final int id;
        PrimitiveType(int id) {
            this.id = id;
        }

        public static PrimitiveType FromId(int id) {
            for (PrimitiveType type : values()) {
                if(type.id == id)
                    return type;
            }
            return NONE;
        }
    }


}
