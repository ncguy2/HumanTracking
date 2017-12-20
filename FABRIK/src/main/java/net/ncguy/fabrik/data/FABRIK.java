package net.ncguy.fabrik.data;

import com.badlogic.gdx.math.Vector3;
import net.ncguy.api.ik.Bone;
import net.ncguy.api.ik.BoneChain;
import net.ncguy.api.ik.BoneNode;
import net.ncguy.fabrik.sk.SKFABRIK;
import net.ncguy.skeleton.SKChain;
import net.ncguy.utils.MathsUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FABRIK {

    public float epsilon = 0.1f;
    public int maxEvaluations = 16;
    protected int currentEvaluations = 0;

    public boolean Evaluate(Bone effector, Vector3 goal) {
        currentEvaluations++;

        if(currentEvaluations >= maxEvaluations)
            return false;

        Vector3 vector3 = effector.GetPosition();
        float xDist = Math.abs(vector3.x - goal.x);
        float yDist = Math.abs(vector3.y - goal.y);
        float zDist = Math.abs(vector3.z - goal.z);

        return xDist > epsilon || yDist > epsilon || zDist > epsilon;
    }

    AtomicInteger targetSelector = new AtomicInteger(0);

    public void Solve(SKChain chain) {
        new SKFABRIK(chain).Solve();
    }

    public void Solve(BoneNode tree, List<Vector3> targets) {
        targetSelector.set(0);
        new BoneStructureSolver(tree, targets, targetSelector).Solve();
    }

    public void Solve(BoneChain boneChain, Vector3 target) {

        new SimpleFABRIKChain(boneChain, target).Solve();

//        Vector3 origin = boneChain.get(0).position;
//        currentEvaluations = 0;
//
//        boneChain.Last().position.set(target);
//
//        while(Evaluate(boneChain.First(), target)) {
//            TargetToRoot(boneChain, origin, target);
//            RootToTarget(boneChain, origin, target);
//        }
    }

    public void TargetToRoot(BoneChain chain, Vector3 root, Vector3 target) {
        Vector3 currentGoal = target.cpy();
        Bone currentBone = chain.Last();

        while(currentBone != null) {
            currentBone.SetDirection(MathsUtils.GetDirection(Vector3.Y, currentGoal.sub(currentBone.GetPosition())));
            currentGoal = currentBone.GetPosition();
            currentBone = chain.Prev(currentBone);
        }

    }

    public void RootToTarget(BoneChain chain, Vector3 root, Vector3 target) {
        Bone currentBone = chain.First();
        Vector3 currentPosition = currentBone.EndPosition();

        while(currentBone != null) {
            currentBone.SetPosition(currentPosition);
            currentPosition = currentBone.EndPosition();
            currentBone = chain.Next(currentBone);
        }
    }

}
