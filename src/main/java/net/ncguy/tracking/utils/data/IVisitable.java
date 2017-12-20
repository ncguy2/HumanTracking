package net.ncguy.tracking.utils.data;

public interface IVisitable<T> {

    void Accept(IVisitor<T> visitor);
    T Data();

}
