package net.ncguy.api.ik;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
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

        y += 1;
        Vector3 newPos = new Vector3(x, y, z).scl(10);
//        if(parent != null)
//            newPos.mul(parent.globalTransform.cpy().inv());

        node.translation.set(newPos);
        node.isAnimated = false;
        node.calculateTransforms(true);
        node.calculateBoneTransforms(true);
//
        if(this.instance != null)
            this.instance.calculateTransforms();
//        node.globalTransform.setTranslation(x, y, z);
    }

    @Override
    public void SetDirection(float x, float y, float z) {
        super.SetDirection(x, y, z);
//        if(true)
//            return;

//        Matrix3 rotMat = MathsUtils.CreateRotationMatrix(new Vector3(x, y, z));

        Vector3 dir = new Vector3(x, y, z);

        Quaternion orient = MathsUtils.ToQuaternion(dir);

        if(parent != null) {
            Matrix4 m = new Matrix4();
            m.rotate(orient);
            m.mul(parent.globalTransform);
            orient.setFromMatrix(m);
        }

        node.rotation.set(orient);
        node.isAnimated = true;
//        node.calculateTransforms(true);
//        node.calculateBoneTransforms(true);
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
}
