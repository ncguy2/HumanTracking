package net.ncguy.fabrik.data;

import com.badlogic.gdx.math.Vector3;
import net.ncguy.api.ik.Bone;
import net.ncguy.api.ik.BoneChain;
import net.ncguy.api.ik.BoneNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BoneStructureSolver {

    public BoneNode rootNode;
    public List<Vector3> targets;
    AtomicInteger targetSelector;

    public BoneStructureSolver(BoneNode rootNode, List<Vector3> targets, AtomicInteger targetSelector) {
        this.rootNode = rootNode;
        this.targets = targets;
        this.targetSelector = targetSelector;
        rootNode.UpdateFlags();
    }

    public void Solve() {
        Solve(rootNode);
    }

    public void Solve(BoneNode node) {
        BoneNode lastNode = node.FindLastOfChain();
        BoneChain chainA = node.FindChain(lastNode.bone.name);

        List<SimpleFABRIKChain> subchains = new ArrayList<>();

        AtomicInteger i = new AtomicInteger();
        lastNode.children.forEach(child -> {
//            BoneNode last = child.FindLastOfChain();
//            BoneChain chain = lastNode.FindChain(last.bone.name);
//            SimpleFABRIKChain fabrikChain = new SimpleFABRIKChain(chain, targets.get(i.getAndIncrement()));
            subchains.add(GenerateChainFromNode(child));
        });

        SimpleFABRIKChain fabrikChainA = new SimpleFABRIKChain(chainA, lastNode.bone.GetTarget().cpy());

        SimpleFABRIKChain[] chains = new SimpleFABRIKChain[subchains.size()];
        subchains.toArray(chains);
        Solve(fabrikChainA, chains);
    }

    public SimpleFABRIKChain GenerateChainFromNode(BoneNode node) {
        return GenerateChainFromNode(node, null);
    }
    public SimpleFABRIKChain GenerateChainFromNode(BoneNode node, Vector3 target) {
        BoneNode last = node.FindLastOfChain();
        BoneChain chain = node.FindChain(last.bone.name, true);

        if(target == null)
            target = last.bone.GetPosition();

        return new SimpleFABRIKChain(chain, target);
    }

    public void SolveSingle(SimpleFABRIKChain chain) {
        chain.Solve();
    }

    public void Solve(SimpleFABRIKChain input, SimpleFABRIKChain... outputs) {

//        input.origin.set(rootNode.bone.GetPosition());

        if(outputs == null || outputs.length == 0) {
            input.target.set(input.bones.Last().GetTarget());
            SolveSingle(input);
            return;
        }


        for (SimpleFABRIKChain output : outputs)
            output.Backward();

        Vector3 centroid = new Vector3();
        for (SimpleFABRIKChain output : outputs) {
//            centroid.add(output.bones.get(1).position);
            centroid.add(GetCentroidFactor(output));
        }
        centroid.scl(.5f);

        input.target.set(centroid);
        input.Solve();

        for (SimpleFABRIKChain output : outputs)
            output.origin.set(input.bones.Last().GetPosition());

        for (SimpleFABRIKChain output : outputs)
            output.Forward();
    }

    public Vector3 GetCentroidFactor(SimpleFABRIKChain chain) {
        Bone last = chain.bones.Last();
        BoneNode node = rootNode.Find(last);
        if(node.Is(BoneNode.Flags.SUBBASE)) {

            List<SimpleFABRIKChain> subchains = new ArrayList<>();
            node.children.stream()
                    .map(this::GenerateChainFromNode)
                    .forEach(subchains::add);
            SimpleFABRIKChain[] chains = new SimpleFABRIKChain[subchains.size()];
            subchains.toArray(chains);
            Solve(chain, chains);

            Vector3 centroid = new Vector3();
            for (SimpleFABRIKChain output : chains)
                centroid.add(GetCentroidFactor(output));
            return centroid.scl(.5f);
        }else if(node.Is(BoneNode.Flags.EFFECTOR)) {
            return chain.bones.get(0).GetPosition();
        }

        return new Vector3();
    }

}
