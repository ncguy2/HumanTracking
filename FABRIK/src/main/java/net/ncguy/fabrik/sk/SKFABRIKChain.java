package net.ncguy.fabrik.sk;

import com.badlogic.gdx.math.Vector3;
import net.ncguy.skeleton.SKBone;
import net.ncguy.skeleton.SKChain;
import net.ncguy.skeleton.SKJoint;
import net.ncguy.utils.MathsUtils;

public class SKFABRIKChain {

    protected final SKChain boneChain;
    protected final Vector3 target;
    protected final Vector3 origin;
    protected double tolerance;
    protected float totalLength;

    protected boolean constrained;
    protected double left;
    protected double right;
    protected double up;
    protected double down;

    protected int maxCount = 16;

    public SKFABRIKChain(SKChain boneChain) {
        this.boneChain = boneChain;
        this.target = boneChain.Last().GetTarget();
        this.origin = boneChain.First().GetPosition();
        this.tolerance = 0.1;
        this.totalLength = boneChain.TotalLength();

        this.constrained = false;
        this.left = Math.toRadians(89);
        this.right = Math.toRadians(89);
        this.up = Math.toRadians(89);
        this.down = Math.toRadians(89);
    }

    public void Backward() {
        SKJoint last = boneChain.Last();
        last.SetPosition(target);
        int count = boneChain.Count();
        for(int i = count - 2; i >= 0; i--) {
            SKJoint here = boneChain.Get(i);
            SKJoint next = boneChain.Get(i + 1);
            SKBone joiningBone = next.parentBone;

            Vector3 r = next.GetPosition().cpy().sub(here.GetPosition());
            float length = r.len();
            if(length == 0.f)
                length += 0.0001f;
            float l = joiningBone.Length() / length;

            Vector3 tmpPos1 = next.GetPosition().cpy().scl(1 - l);
            Vector3 tmpPos2 = here.GetPosition().cpy().scl(l);

            here.SetPosition(tmpPos1.add(tmpPos2));

            Vector3 dir;

            Vector3 toNext = MathsUtils.GetDirection(here.GetPosition(), next.GetPosition());
            if(here.IsOrphan())
                dir = toNext;
            else {
                Vector3 toPrev = MathsUtils.GetDirection(here.GetPosition(), here.GetParentJoint().GetPosition());
                dir = MathsUtils.Lerp(toPrev, toNext, .5f);
            }
            here.SetDirection(dir);

//            here.SetDirection(MathsUtils.GetDirection(here.GetPosition(), next.GetPosition()));
        }
    }

    public void Forward() {
        boneChain.First().SetPosition(this.origin);
        int count = boneChain.Count();
        boneChain.Iterate((here, next) -> {
            SKBone bone = next.parentBone;

            Vector3 r = next.GetPosition().cpy().sub(here.GetPosition());
            float length = r.len();
            if(length == 0.f)
                length += 0.0001f;

            float l = bone.Length() / length;

            Vector3 tmpPos1 = here.GetPosition().cpy().scl(1 - l);
            Vector3 tmpPos2 = next.GetPosition().cpy().scl(l);

            next.SetPosition(tmpPos1.add(tmpPos2));

            Vector3 dir;

            Vector3 toNext = MathsUtils.GetDirection(here.GetPosition(), next.GetPosition());
            if(here.IsOrphan())
                dir = toNext;
            else {
                Vector3 toPrev = MathsUtils.GetDirection(here.GetPosition(), here.GetParentJoint().GetPosition());
                dir = MathsUtils.Lerp(toPrev, toNext, .5f);
            }
            here.SetDirection(dir);
        });
    }

    public void Solve() {
        double distance = DistanceToTarget(boneChain.First());

        if(distance > totalLength) {
            boneChain.Iterate((here, next) -> {
                float r = target.cpy().sub(here.GetPosition()).len();
                float l = next.parentBone.Length() / r;

                Vector3 pos = here.GetPosition().cpy().scl(1 - l);
                Vector3 tgt = target.cpy().scl(l);

                next.SetPosition(pos.add(tgt));
            });
        }else {
            int count = 0;
            float dif = DistanceToTarget(boneChain.Last());
            while(dif > tolerance) {
                Backward();
                Forward();
                dif = DistanceToTarget(boneChain.Last());
                if(++count > maxCount) break;
            }
        }
    }

    public float DistanceToTarget(SKJoint joint) {
        return joint.GetPosition().cpy().sub(target).len();
    }

}
