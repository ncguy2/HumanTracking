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
        SetDirection(this.bone.GetDirection());
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
}
