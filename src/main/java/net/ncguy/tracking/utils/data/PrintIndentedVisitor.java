package net.ncguy.tracking.utils.data;

import java.util.function.Consumer;

public class PrintIndentedVisitor<T> implements IVisitor<T> {

    private final int indentDepth;
    private final Consumer<String> printAction;

    public PrintIndentedVisitor(int indentDepth, Consumer<String> printAction) {
        this.indentDepth = indentDepth;
        this.printAction = printAction;
    }

    @Override
    public IVisitor<T> Visit(IVisitable<T> visitable) {
        return new PrintIndentedVisitor<>(this.indentDepth + 2, this.printAction);
    }

    @Override
    public void VisitData(IVisitable<T> visitable, T data) {

        for (int i = 0; i < this.indentDepth; i++)
            this.printAction.accept(" ");

        if(data == null)
            this.printAction.accept("NULL\n");
        else
            this.printAction.accept(data.toString()+"\n");

    }
}
