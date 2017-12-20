package net.ncguy.ui.detachable;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;

import java.util.Optional;

public class DetachableTab extends Tab implements IDetachableContainer {

    protected IDetachable content;

    public DetachableTab() {
        super(false, false);
    }

    public DetachableTab(IDetachable content) {
        this();
        this.content = content;
    }

    @Override
    public String getTabTitle() {
        if(content != null)
            return content.GetTitle();
        return null;
    }

    @Override
    public Table getContentTable() {
        if(content != null && content.GetRootTable().isPresent())
            return content.GetRootTable().get();
        return null;
    }

    @Override
    public void OnRemove(IDetachable removed) {
        if(removed.equals(content))
            getPane().remove(this);
    }

    @Override
    public Optional<IDetachable> GetContents() {
        return Optional.ofNullable(content);
    }

    @Override
    public void SetContents(IDetachable contents) {
        this.content = contents;
        if(this.content != null)
            this.content.SetParent(this);
    }

    @Override
    public float GetMetric() {
        return getContentTable().getZIndex();
    }
}
