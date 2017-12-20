package net.ncguy.tracking.utils.data;

import java.util.HashSet;
import java.util.Set;

public class VisitableTree<T> implements IVisitable<T> {

    private Set<VisitableTree<T>> children = new HashSet<>();
    private T data;

    public VisitableTree(T data) {
        this.data = data;
    }

    @Override
    public void Accept(IVisitor<T> visitor) {
        visitor.VisitData(this, this.Data());
        children.forEach(c -> c.Accept(visitor.Visit(c)));
    }

    @Override
    public T Data() {
        return data;
    }

    public VisitableTree<T> Child(T data) {
        for (VisitableTree<T> child : children) {
            if(child.Data().equals(data)) return child;
        }
        return Child(new VisitableTree<>(data));
    }

    public VisitableTree<T> Child(VisitableTree<T> child) {
        children.add(child);
        return child;
    }

}
