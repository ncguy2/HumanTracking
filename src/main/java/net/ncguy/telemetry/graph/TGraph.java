package net.ncguy.telemetry.graph;

import java.util.function.BiConsumer;

public class TGraph extends TNode<TGroup> {

    public TGraph() {
        super("Telemetry Graph");
    }

    @Override
    protected TGroup New(String id) {
        return new TGroup(id);
    }

    public void Add(String source, String id, String jsonData) {
        Get(source).Add(id, jsonData);
    }

    public void ForAll(BiConsumer<TGroup, TSet> func) {
        sets.forEach((gId, group) ->
                group.ForEach((sId, set) ->
                        func.accept(group, set)));
    }



}
