package net.ncguy.skeleton;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import net.ncguy.api.ik.MeshBone;
import net.ncguy.tracking.display.TrackingSpace;
import net.ncguy.ui.scene3d.AnchoredLabel;
import net.ncguy.utils.Reference;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.*;

public class SKUtils {

    public static List<AnchoredLabel> labels;
    public static BitmapFont font;

    private static ModelInstance boneInstance;
    private static ModelInstance jointInstance;

    public static ModelInstance BoneInstance() {
        if (boneInstance == null) {
            ModelBuilder mb = new ModelBuilder();
            Material mtl = new Material(ColorAttribute.createDiffuse(Color.GREEN));
            Model cone = mb.createArrow(0, 0, 0, 0, 0, 1, .1f, .1f, 4, GL20.GL_LINES, mtl, Position | Normal | ColorPacked);
            boneInstance = new ModelInstance(cone);
        }
        return boneInstance;
    }

    public static ModelInstance JointInstance() {
        if (jointInstance == null) {
            ModelBuilder mb = new ModelBuilder();
            Material mtl = new Material(ColorAttribute.createDiffuse(Color.RED));
            jointInstance = new ModelInstance(mb.createSphere(1, 1, 1, 16, 16, GL20.GL_LINES, mtl, Position | Normal | ColorPacked));
        }
        return jointInstance;
    }

    public static SKJoint FromNodeTree(Node node, ModelInstance instance) {
        return FromNodeTree(node, instance, null);
    }

    public static SKJoint FromNodeTree(Node node, ModelInstance instance, SKJoint parent) {
        MeshBone meshBone = new MeshBone(node, instance);
        SKJoint joint;
        if(parent == null)
            joint = new BoneJoint(null, meshBone);
        else
            joint = new BoneJoint(new SKBone(parent), meshBone);

        if(labels != null && font != null) {
            Vector3 nodePos = meshBone.GetRawPosition();
            Vector3 origin = Vector3.Zero;
            if(parent != null) {
                Vector3 parentPos = parent.GetPosition();

                Vector3 forward = nodePos.cpy().sub(parentPos).nor();
                Vector3 right = forward.cpy().crs(Vector3.Y);
                Vector3 up = forward.cpy().crs(right);

                origin.set(nodePos.cpy().sub(right.cpy().scl(3)));
            }

            AnchoredLabel anchoredLabel = new AnchoredLabel(node.id, nodePos, origin, font);
            labels.add(anchoredLabel);
        }


        if(node.hasChildren()) {
            Iterable<Node> children = node.getChildren();
            List<Node> nodes = new ArrayList<>();
            children.forEach(nodes::add);
            System.out.println(node.id + ", children found: " + nodes.size());
            for (Node c : nodes) {
                System.out.println("  " + c.id);
                FromNodeTree(c, instance, joint);
            }
        }

        return joint;
    }

    public static void RenderMesh(SKJoint skStructure, ModelBatch batch, Environment env) {
        ModelInstance inst = JointInstance();
        inst.transform.idt();
        inst.transform.setTranslation(skStructure.GetPosition());
        inst.transform.scale(.1f, .1f, .1f);
        batch.render(inst, env);

        skStructure.childrenBones.forEach(bone -> RenderBoneMesh(bone, batch, env));
    }

    public static void RenderBoneMesh(SKBone bone, ModelBatch batch, Environment env) {
        ModelInstance inst = BoneInstance();
        inst.transform.idt();
//        inst.transform.setToLookAt(bone.start.GetPosition(), bone.end.GetPosition(), Vector3.Y);
        inst.transform.setToLookAt(bone.start.GetPosition(), bone.end.GetPosition(), Vector3.Y);
        inst.transform.setTranslation(bone.start.GetPosition());
        inst.transform.scale(1, 1, bone.Length());
//        inst.transform.rotate(MathsUtils.ToQuaternion(MathsUtils.GetDirection(bone.start.GetPosition(), bone.end.GetPosition())));

//        inst.transform.setToLookAt(bone.start.GetPosition(), bone.end.GetPosition(), Vector3.Y);
        batch.render(inst, env);
        RenderMesh(bone.end, batch, env);
    }

    public static void Render(SKJoint skStructure, ShapeRenderer renderer, boolean invertColour) {

        if(skStructure == null) return;

        renderer.begin(ShapeRenderer.ShapeType.Line);
        RenderBones(skStructure, renderer, invertColour);
        renderer.end();

        if(TrackingSpace.showBoneLocations.get()) {
            renderer.begin(ShapeRenderer.ShapeType.Filled);
            RenderJoints(skStructure, renderer, invertColour);
            renderer.end();
        }
    }

    private static void RenderJoints(SKJoint joint, ShapeRenderer renderer, boolean invertColour) {

        if(joint == null) return;

        Vector3 s = new Vector3(.25f, .25f, .25f);

        if(joint.equals(TrackedBones.SelectedBone())) {
            renderer.setColor(Reference.Colour.RendererColours.BONE_LOCATION_ACTIVE_COLOUR);
            s.scl(5f);
        }else renderer.setColor(Reference.Colour.RendererColours.BONE_LOCATION_COLOUR);

        if(invertColour)
            renderer.getColor().set(Color.WHITE.cpy().sub(renderer.getColor()));
        renderer.getColor().a = 1.f;

        Vector3 d = s.cpy().scl(.5f);
        Vector3 p = joint.GetPosition().cpy().sub(d.x, d.y, -d.z);
        renderer.box(p.x, p.y, p.z, s.x, s.y, s.z);


        joint.GetChildJoints().forEach(j -> RenderJoints(j, renderer, invertColour));
    }

    private static void RenderBones(SKJoint joint, ShapeRenderer renderer, boolean invertColour) {
        if(joint == null) return;

        if(TrackingSpace.showBoneDirections.get()) {
            renderer.setColor(Reference.Colour.RendererColours.BONE_DIRECTION_PARENTED_COLOUR);
            if(invertColour)
                renderer.getColor().set(Color.WHITE.cpy().sub(renderer.getColor()));
            renderer.getColor().a = 1.f;
            renderer.line(joint.GetPosition(), joint.GetDirection());
        }

        if(TrackingSpace.showBoneConnections.get()) {
            renderer.setColor(Reference.Colour.RendererColours.BONE_DIRECTION_PARENTED_COLOUR.cpy());
            if(invertColour)
                renderer.getColor().set(Color.WHITE.cpy().sub(renderer.getColor()));
            renderer.getColor().a = 1.f;
            joint.childrenBones.forEach(b -> RenderBone(b, renderer));
        }
        joint.GetChildJoints().forEach(j -> RenderBones(j, renderer, invertColour));
    }

    private static void RenderBone(SKBone bone, ShapeRenderer renderer) {
        if(bone == null) return;
        renderer.line(bone.start.GetPosition(), bone.end.GetPosition());
    }


}
