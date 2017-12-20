package net.ncguy.api.ik;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BoneNode {

    public Bone bone;

    public BoneNode parent;
    public List<BoneNode> children;

    public transient int flagMask;

    public BoneNode(Bone bone) {
        this(bone, null);
        bone.setStructure(FindRoot());
    }

    public BoneNode(Bone bone, BoneNode parent) {
        this.bone = bone;
        this.parent = parent;
        this.children = new ArrayList<>();

        this.bone.hasParent = parent != null;

        if(this.bone.hasParent) {
            parent.children.add(this);
//            this.bone.SetDirection(MathsUtils.GetDirection(this.bone.GetPosition(), parent.bone.GetPosition()));
        }
    }

    public BoneNode FindRoot() {
        if(parent == null)
            return this;

        return parent.FindRoot();
    }

    public BoneNode FindLastOfChain() {
        if(children.isEmpty() || children.size() >= 2)
            return this;

        // If has 1 child

        return children.get(0).FindLastOfChain();
    }

    public BoneNode FindStartOfChain() {
        if(parent == null)
            return this;

        if(children.size() >= 2)
            return this;

        return parent.FindStartOfChain();
    }

    public List<BoneChain> GetChains() {
        return GetChains(true);
    }

    public List<BoneChain> GetChains(boolean includeSelf) {
        List<BoneChain> chains = new ArrayList<>();
        GetChains(chains, includeSelf);
        return chains;
    }

    public void GetChains(List<BoneChain> chains, boolean includeSelf) {
        BoneNode last = FindLastOfChain();
        chains.add(FindChain(last.bone.name, includeSelf));
        last.children.forEach(child -> child.GetChains(chains, includeSelf));
    }

    public BoneChain IsolateToChain() {
        BoneNode last = FindLastOfChain();
        return FindChain(last.bone.name);
    }

    public boolean IsRootNode() {
        return parent == null;
    }

    public void Iterate(Consumer<BoneNode> func) {
        func.accept(this);
        children.forEach(c -> c.Iterate(func));
    }

    public BoneNode Find(String name) {
        if(this.bone.name.equalsIgnoreCase(name))
            return this;

        if(children.isEmpty())
            return null;

        for (BoneNode child : children) {
            BoneNode find = child.Find(name);
            if(find != null) return find;
        }

        return null;
    }

    public BoneNode Find(Bone bone) {
        if(this.bone.equals(bone))
            return this;

        if(children.isEmpty())
            return null;


        for (BoneNode child : children) {
            BoneNode find = child.Find(bone);
            if(find != null) return find;
        }

        return null;
    }

    public BoneChain FindChain(String targetName) {
        return FindChain(targetName, false);
    }

    public BoneChain FindChain(String targetName, boolean includeSelf) {
        BoneChain chain = new BoneChain();
        FindChain(targetName, chain);

        if (!includeSelf && chain.Last().equals(this.bone))
            chain.remove(this.bone);

        chain.Flip();
        return chain;
    }

    public boolean FindChain(String targetName, BoneChain chain) {
        if(this.bone.name.equalsIgnoreCase(targetName)) {
            chain.add(this.bone);
            return true;
        }

        for (BoneNode child : children) {
            if(child.FindChain(targetName, chain)) {
                chain.add(this.bone);
                return true;
            }
        }

        return false;
    }

    public void UpdateFlags() {

        flagMask = 0;

        // Rules:
        //  Effector is at the end of a chain
        //  Sub-base is at an intersection

        if(parent != null) {
            if(children.isEmpty() || children.size() >= 2) {
                flagMask |= Flags.EFFECTOR.Bit();
                if(!children.isEmpty())
                    flagMask |= Flags.SUBBASE.Bit();
            }
        }

        children.forEach(BoneNode::UpdateFlags);
    }

    public static BoneNode FromBoneChain(BoneChain chain) {
        BoneNode root = null;
        BoneNode current = null;
        for (Bone bone : chain) {
            current = new BoneNode(bone, current);
            if(root == null)
                root = current;
        }
        return root;
    }

    public void Add(BoneNode boneNode) {
        children.add(boneNode);
        boneNode.parent = this;
    }

    public String ToString() {
        StringBuilder sb = new StringBuilder();
        ToString(sb, "");
        return sb.toString();
    }

    public void ToString(StringBuilder sb, String prefix) {
        sb.append(prefix).append(bone.name).append("\n");
        for (BoneNode child : children)
            child.ToString(sb, prefix + "  ");
    }

    public boolean Is(Flags flag) {
        int bit = flag.Bit();
        return (flagMask & bit) == bit;
    }

    public static enum Flags {
        EFFECTOR,
        SUBBASE,
        ;

        public int Bit() {
            return (int) Math.pow(2, ordinal());
        }

    }

}
