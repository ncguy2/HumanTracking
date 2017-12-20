package net.ncguy.api.ik;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BoneChain extends ArrayList<Bone> {

    public BoneChain() {
        super();
    }

    public BoneChain(Bone[] bones) {
        Collections.addAll(this, bones);
    }

    public Bone[] ToArray() {
        Bone[] arr = new Bone[size()];
        toArray(arr);
        return arr;
    }

    public void Iterate(Consumer<Bone> func) {
        forEach(func);
    }

    public void Iterate(BiConsumer<Bone, Bone> func) {
        int length = size() - 1;
        for (int i = 0; i < length; i++)
            func.accept(get(i), get(i + 1));
    }

    public Bone First() {
        return get(0);
    }

    public Bone Last() {
        return get(size() - 1);
    }

    public Bone Next(Bone bone) {
        int idx = indexOf(bone) + 1;
        if (idx >= size())
            return null;
        return get(idx);
    }

    public Bone Prev(Bone bone) {
        int idx = indexOf(bone) - 1;
        if (idx < 0)
            return null;
        return get(idx);
    }

    public float GetLength() {
        final float[] f = {0.f};
        Iterate(bone -> f[0] += bone.length);
        return f[0];
    }

    public void Flip() {
        Collections.reverse(this);
    }
}