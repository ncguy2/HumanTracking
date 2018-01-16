package net.ncguy.skeleton;

import com.badlogic.gdx.math.Vector3;
import net.ncguy.api.ik.Bone;

public class BoneJoint extends SKJoint {

    public final Bone bone;

    public BoneJoint(SKBone parentBone, Bone bone) {
        super(parentBone);
        this.bone = bone;
        this.id = bone.name;
        SetPositionMem(this.bone.GetPosition());
        SetDirectionMem(this.bone.GetDirection());
        PropagateToParentBone();
    }

    @Override
    public Vector3 GetPosition() {
        if(bone == null)
            return position;
        return bone.GetPosition();
    }

    @Override
    public void SetPosition(Vector3 vec) {
        super.SetPosition(vec);
        bone.SetPosition(vec);
    }

    @Override
    public void SetDirection(Vector3 dir) {
        super.SetDirection(dir);
        bone.SetDirection(dir);
    }

    @Override
    public Vector3 GetDirection() {
        if(bone == null)
            return direction;
        return bone.GetDirection();
    }

    @Override
    public void SetScale(float x, float y, float z) {
        bone.SetScale(x, y, z);
    }

    @Override
    public Vector3 GetHomePosition() {
        return bone.TransformHomePosition(this.homePosition);
    }

    @Override
    public Vector3 GetHomeDirection() {
        return bone.TransformHomeDirection(this.homeDirection);
    }

    @Override
    public void SetHomePosition(Vector3 pos) {
        super.SetHomePosition(bone.InverseTransformHomePosition(pos));
    }

    @Override
    public void SetHomeDirection(Vector3 dir) {
        super.SetHomeDirection(bone.InverseTransformHomeDirection(dir));
    }

    @Override
    public Vector3 Transform(Vector3 vec) {
        return bone.Transform(vec);
    }

    @Override
    public Vector3 InvTransform(Vector3 vec) {
        return bone.InvTransform(vec);
    }

    @Override
    public Vector3 GetModelPosition() {
        return bone.GetModelPosition();
    }

    @Override
    public void SetModelPosition(Vector3 pos) {
        bone.SetModelPosition(pos);
    }
}
