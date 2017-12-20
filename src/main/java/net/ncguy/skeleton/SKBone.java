package net.ncguy.skeleton;

public class SKBone {

    public SKJoint start;
    public SKJoint end;

    protected float length;

    public SKBone(SKJoint start) {
        this(start, null);
    }

    public SKBone(SKJoint start, SKJoint end) {
        this.start = start;

        if(this.start != null)
            this.start.childrenBones.add(this);

        SetEnd(end);
    }

    public void SetEnd(SKJoint joint) {

        if(this.end != null)
            this.end.DetachFromParent();

        this.end = joint;

        if(this.end == null) {
            this.length = 0;
        }else {
            this.end.AttachToParent(this);
            float dst = this.start.GetPosition().cpy().dst(this.end.GetPosition());
            float len = this.start.GetPosition().cpy().sub(this.end.GetPosition()).len();
            this.length = len;
        }
    }

    public SKJoint Other(SKJoint joint) {
        if(joint.equals(start))
            return end;

        if(joint.equals(end))
            return start;

        // Not connected
        return null;
    }

    public float Length() {
        return length;
    }

    /**
     * Null-safe variant of instance {@link net.ncguy.skeleton.SKBone#Other(net.ncguy.skeleton.SKJoint)}
     */
    public static SKJoint Other(SKBone b, SKJoint joint) {
        if(b == null)
            return null;
        return b.Other(joint);
    }
}
