package net.ncguy.fabrik.data;

import com.badlogic.gdx.math.Vector3;
import net.ncguy.api.ik.Bone;
import net.ncguy.api.ik.BoneChain;
import net.ncguy.utils.MathsUtils;

public abstract class AbstractFABRIKChain<T extends BoneChain> {

    protected final T bones;
    protected final Vector3 target;
    protected final Vector3 origin;
    public double tolerance;
    public float totalLength;

    public boolean constrained;
    public double left;
    public double right;
    public double up;
    public double down;

    public int maxCount = 16;

    public AbstractFABRIKChain(T bones, Vector3 target) {
        this.bones = bones;
        this.target = target;
        this.tolerance = 0.1;
        this.totalLength = bones.GetLength();
        this.origin = bones.First().GetPosition().cpy();

        this.constrained = false;
        this.left = Math.toRadians(89);
        this.right = Math.toRadians(89);
        this.up = Math.toRadians(89);
        this.down = Math.toRadians(89);
    }

    public abstract void Solve();

    public void Backward() {
        bones.Last().SetPosition(target);
        for(int i = bones.size() - 2; i > 0; i--) {

            Bone here = bones.get(i);
            Bone next = bones.get(i + 1);

            Vector3 r = next.GetPosition().cpy().sub(here.GetPosition());
            float length = r.len();
//            float length = bones.get(i).Length();
            if(length == 0.f)
                length += 0.0001f;
            float l = next.Length() / length;

            Vector3 tmpPos1 = next.GetPosition().cpy().scl(1 - l);
            Vector3 tmpPos2 = here.GetPosition().cpy().scl(l);
//            Vector3 tmpPos3 = MathsUtils.Lerp(here.GetPosition(), next.GetPosition(), l);

            here.SetPosition(tmpPos1.add(tmpPos2));
//            here.SetPosition(tmpPos3);
        }
    }

    public void Forward() {
        bones.First().SetPosition(this.origin);
        bones.Iterate((here, next) -> {
            Vector3 r = next.GetPosition().cpy().sub(here.GetPosition());

            float length = r.len();
//            float length = here.Length();
            if(length == 0.f)
                length += 0.0001f;

            float l = here.Length() / length;

            Vector3 tmpPos1 = here.GetPosition().cpy().scl(1 - l);
            Vector3 tmpPos2 = next.GetPosition().cpy().scl(l);
//            Vector3 tmpPos3 = MathsUtils.Lerp(here.GetPosition(), next.GetPosition(), l);

            next.SetPosition(tmpPos1.add(tmpPos2));
//            next.SetPosition(tmpPos3);

            here.SetDirection(MathsUtils.GetDirection(here.GetPosition(), next.GetPosition()));
        });
    }

}
