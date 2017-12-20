package net.ncguy.tracking.geometry;

import net.ncguy.tracking.display.SDFExperiment;
import net.ncguy.tracking.utils.data.IVisitable;
import net.ncguy.tracking.utils.data.IVisitor;
import net.ncguy.tracking.utils.data.helpers.TreeObjectWrapper;

import java.util.List;

public class GeometryFlattenVisitor implements IVisitor<TreeObjectWrapper<SDFExperiment.ModelWrapper>> {

    List<GeometryItem> items;
    GeometryItem parentItem;
    GeometryItem currentItem;

    public GeometryFlattenVisitor(GeometryItem parentItem, List<GeometryItem> items) {
        this.parentItem = parentItem;
        this.items = items;
        currentItem = new GeometryItem();
        currentItem.parent = parentItem;
        this.items.add(currentItem);
    }

    @Override
    public IVisitor<TreeObjectWrapper<SDFExperiment.ModelWrapper>> Visit(IVisitable<TreeObjectWrapper<SDFExperiment.ModelWrapper>> visitable) {
        return new GeometryFlattenVisitor(currentItem, items);
    }

    @Override
    public void VisitData(IVisitable<TreeObjectWrapper<SDFExperiment.ModelWrapper>> visitable, TreeObjectWrapper<SDFExperiment.ModelWrapper> data) {
        if(data.HasObject()) {
            currentItem.data = data.GetObject().model;
            currentItem.operation = data.GetObject().model.operation;
        }else currentItem.operation = Model.Operation.UNION;

        currentItem.path = data.GetLabel();

        if(parentItem != null)
            parentItem.SetNext(currentItem);
    }
}
