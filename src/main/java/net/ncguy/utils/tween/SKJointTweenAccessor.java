package net.ncguy.utils.tween;

import aurelienribon.tweenengine.TweenAccessor;
import com.badlogic.gdx.math.Vector3;
import net.ncguy.skeleton.SKJoint;

public class SKJointTweenAccessor implements TweenAccessor<SKJoint> {

    public static final int POSITION = 1;

    @Override
    public int getValues(SKJoint skJoint, int i, float[] floats) {
        Vector3 pos = skJoint.GetPosition();
        int index = 0;

        floats[index++] = pos.x;
        floats[index++] = pos.y;
        floats[index++] = pos.z;

        return index;
    }

    @Override
    public void setValues(SKJoint skJoint, int i, float[] floats) {
        Vector3 pos = new Vector3();

        int index = 0;
        pos.x = floats[index++];
        pos.y = floats[index++];
        pos.z = floats[index++];

        skJoint.SetPosition(pos);
    }
}
