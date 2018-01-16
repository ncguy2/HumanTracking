package net.ncguy.physics.sim;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import net.ncguy.skeleton.SKJoint;

public class JointMotionState extends btMotionState {

    transient SKJoint joint;

    public JointMotionState(SKJoint joint) {
        this.joint = joint;
    }

    @Override
    public void getWorldTransform(Matrix4 worldTrans) {
        joint.GetWorldTransform(worldTrans);
    }

    @Override
    public void setWorldTransform(Matrix4 worldTrans) {
        joint.SetWorldTransform(worldTrans);
    }
}
