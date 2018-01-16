package net.ncguy.api.ik;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import net.ncguy.utils.MathsUtils;

public class MeshBone extends Bone {

    public final Node node;
    Node parent;
    private final ModelInstance instance;


    public MeshBone(Node node) {
        this(node, null);
    }
    public MeshBone(Node node, ModelInstance instance) {
        super(node.id);
        this.node = node;
        this.instance = instance;

        Vector3 thisTrans = new Vector3();
        node.globalTransform.getTranslation(thisTrans);

        if(this.node.hasParent()) {
            parent = this.node.getParent();

            Vector3 parentTrans = new Vector3();
            parent.globalTransform.getTranslation(parentTrans);
            this.SetDirection(MathsUtils.GetDirection(thisTrans, parentTrans));
            this.length = thisTrans.dst(parentTrans);
        }else
            this.length = thisTrans.dst(0, 0, 0);

        node.globalTransform.getTranslation(this.position);

    }

    @Override
    public Vector3 GetPosition() {
        Vector3 translation = new Vector3();
        node.globalTransform.getTranslation(translation);
        return translation;
    }

    @Override
    public void SetPosition(float x, float y, float z) {
        super.SetPosition(x, y, z);

        Vector3 newPos = new Vector3(x, y, z);
        if(parent != null)
            newPos.mul(parent.globalTransform.cpy().inv());

        node.translation.set(newPos);
        node.isAnimated = false;

        if(!propagateToChildren) {
            node.getChildren().forEach(child -> {
                child.localTransform.translate(newPos.cpy());
            });
        }

        node.calculateTransforms(propagateToChildren);
        node.calculateBoneTransforms(propagateToChildren);
//
        if(this.instance != null)
            this.instance.calculateTransforms();
//        node.globalTransform.setTranslation(x, y, z);
    }

    @Override
    public void Update() {
        node.isAnimated = false;
        node.calculateTransforms(true);
        node.calculateBoneTransforms(true);
        if(this.instance != null)
            this.instance.calculateTransforms();
    }

    @Override
    public void SetDirection(float x, float y, float z) {
        super.SetDirection(x, y, z);
//        if(true)
//            return;

//        Matrix3 rotMat = MathsUtils.CreateRotationMatrix(new Vector3(x, y, z));

        Vector3 dir = new Vector3(-x, y, z);
        Quaternion orient = MathsUtils.ToQuaternion(dir);
//        node.rotation.set(orient);
//        Update();

//        Quaternion orient = new Quaternion().setEulerAnglesRad(x, y, z);
//
//        if(parent != null) {
//            Matrix4 m = new Matrix4();
//            m.rotate(orient);
//            m.mul(parent.globalTransform);
//            orient.setFromMatrix(m);
//        }

//        node.rotation.set(orient);
//        node.isAnimated = true;
//        node.calculateTransforms(propagateToChildren);
//        node.calculateBoneTransforms(propagateToChildren);
//        if(this.instance != null)
//            this.instance.calculateTransforms();
    }

    public Vector3 GetRawPosition() {
        return position;
    }

    @Override
    public float Length() {
        return super.Length();
    }

    @Override
    public void SetScale(float x, float y, float z) {

        Vector3 newScl = new Vector3(x, y, z);
        if(parent != null)
            newScl.mul(parent.globalTransform.cpy().inv());

        node.scale.set(newScl);
//        node.isAnimated = true;
        node.calculateTransforms(true);
        node.calculateBoneTransforms(true);
        if(this.instance != null)
            this.instance.calculateTransforms();
    }

    @Override
    public Vector3 GetDirection() {
        return MathsUtils.GetForwardVector(node.rotation);
    }


    @Override
    public Vector3 TransformHomePosition(Vector3 pos) {

        Vector3 newPos = pos.cpy();

        if(parent != null)
            newPos.mul(parent.globalTransform.cpy());

        return newPos;
    }

    @Override
    public Vector3 TransformHomeDirection(Vector3 dir) {
        return super.TransformHomeDirection(dir);
    }

    @Override
    public Vector3 InverseTransformHomePosition(Vector3 pos) {

        Vector3 newPos = pos.cpy();

        if(parent != null)
            newPos.mul(parent.globalTransform.cpy().inv());

        return newPos;
    }

    @Override
    public Vector3 InverseTransformHomeDirection(Vector3 dir) {
        return super.TransformHomeDirection(dir);
    }


    @Override
    public Vector3 Transform(Vector3 vec) {
        Vector3 newVec = vec.cpy();
        newVec.mul(parent.globalTransform);
        return newVec;
    }

    @Override
    public Vector3 InvTransform(Vector3 vec) {
        Vector3 newVec = vec.cpy();
        newVec.mul(parent.globalTransform.cpy().inv());
        return newVec;
    }

    @Override
    public Vector3 GetModelPosition() {
        Vector3 translation = new Vector3();
        node.localTransform.getTranslation(translation);
        return translation;
    }

    @Override
    public void SetModelPosition(Vector3 pos) {
        node.translation.set(pos);
        node.isAnimated = false;
        node.calculateTransforms(propagateToChildren);
        node.calculateBoneTransforms(propagateToChildren);
    }
}
