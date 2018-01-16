package net.ncguy.telemetry.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class TNode<T> {

    public String name;
    public Map<String, T> sets;

    public TNode(String name) {
        this.name = name;
        this.sets = new HashMap<>();
    }

    public T Get(String id) {
        if(!sets.containsKey(id))
            sets.put(id, New(id));
        return sets.get(id);
    }

    protected abstract T New(String id);

    public void ForEach(BiConsumer<String, T> func) {
        sets.forEach(func);
    }

}
