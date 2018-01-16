package net.ncguy.telemetry.graph;

public class TGroup extends TNode<TSet> {

    public TGroup(String name) {
        super(name);
    }

    @Override
    protected TSet New(String id) {
        return new TSet(id);
    }

    public void Add(String id, String jsonData) {
        Get(id).Add(jsonData);
    }

}
