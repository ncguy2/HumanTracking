package net.ncguy.skeleton;

import java.util.function.BiConsumer;

public class SKChain {

    public SKJoint start;
    public SKJoint end;

    public SKChain(SKJoint end) {
        this(end.FindRoot(), end);
    }

    public SKChain(SKJoint start, SKJoint end) {
        this.start = start;
        this.end = end;
    }

    public transient SKJoint ptr;

    public int Count() {
        SKJoint current = Current();

        int amt = 0;
        SetPtr(end);
        while(HasPrevious()) {
            Previous();
            amt++;
        }

        SetPtr(current);
        return amt;
    }

    public void Iterate(BiConsumer<SKJoint, SKJoint> func) {
        int count = Count();
        for(int i = 0; i < count - 1; i++) {
            SKJoint here = Get(i);
            SKJoint next = Get(i + 1);
            func.accept(here, next);
        }
    }

    public SKJoint Get(final int idx) {
        SKJoint current = Current();

        SetPtr(start);

        SKJoint selected = Current();
        int cnt = idx;
        while(cnt > 0) {
            selected = Next();
            cnt--;
        }


        SetPtr(current);
        return selected;
    }

    public boolean HasPrevious() {
        return !Current().IsOrphan();
    }

    public SKJoint Previous() {
       ptr = ptr.GetParentJoint();
       return ptr;
    }

    public SKJoint Current() {
        return ptr;
    }

    public SKJoint Next() {
        ptr = FindChildInChain();
        return ptr;
    }

    public SKJoint First() {
        return start;
    }

    public SKJoint Last() {
        return end;
    }

    public void SetPtr(SKJoint joint) {
        this.ptr = joint;
    }

    public float TotalLength() {
        float len = 0.f;
        SKJoint joint = Last();

        while(joint != null) {
            SKBone b = joint.parentBone;
            if(b != null)
                len += b.length;
            joint = SKBone.Other(b, joint);
        }

        return len;
    }

    protected SKJoint FindChildInChain() {
        return FindChildInChain(end, ptr);
    }

    protected SKJoint FindChildInChain(SKJoint joint) {
        return FindChildInChain(joint, ptr);
    }

    protected SKJoint FindChildInChain(SKJoint joint, SKJoint limit) {
        SKJoint parentJoint = joint.GetParentJoint();

        if(parentJoint == null || parentJoint.equals(limit))
            return joint;

        return FindChildInChain(parentJoint, limit);
    }

}
