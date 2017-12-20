package net.ncguy.fabrik.data;

import com.badlogic.gdx.math.Vector3;
import net.ncguy.api.ik.BoneChain;

public class SimpleFABRIKChain extends AbstractFABRIKChain<BoneChain> {

    public SimpleFABRIKChain(BoneChain bones, Vector3 target) {
        super(bones, target);
    }

    @Override
    public void Solve() {
        double distance = bones.First().GetPosition().cpy().sub(target).len();

        if(distance > totalLength) {
            bones.Iterate((here, next) -> {
                float r = target.cpy().sub(here.GetPosition()).len();
                float l = here.Length() / r;

                Vector3 pos = here.GetPosition().cpy();
                Vector3 tgt = target.cpy();

                pos.scl(1 - l);
                tgt.scl(l);

                next.SetPosition(pos.add(tgt));
            });
        }else{
            int count = 0;
            float dif = bones.Last().GetPosition().cpy().sub(target).len();
            while(dif > tolerance) {
                Backward();
                Forward();
                dif = bones.Last().GetPosition().cpy().sub(target).len();
                count++;
                if(count > maxCount) break;
            }
        }
    }

}
