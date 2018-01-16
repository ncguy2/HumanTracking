package net.ncguy.skeleton;

import java.util.ArrayList;
import java.util.List;

public class SimpleSKChain extends SKChain {

    List<SKJoint> joints;

    public SimpleSKChain(SKJoint start, SKJoint end) {
        super(start, end);
        joints = new ArrayList<>();

        GetChild(start);
        joints.add(start);
    }

    SKJoint GetChild(SKJoint joint) {

        if(joint.equals(end))
            return joint;

        for (SKJoint child : joint.GetChildJoints()) {
            SKJoint skJoint = GetChild(child);
            if(skJoint != null) {
                joints.add(skJoint);
                return joint;
            }
        }

        if(joint.equals(start))
            return joint;

        return null;
    }

    @Override
    public int Count() {
        return joints.size();
    }

    @Override
    public SKJoint Get(int idx) {
        return joints.get(idx);
    }
}
