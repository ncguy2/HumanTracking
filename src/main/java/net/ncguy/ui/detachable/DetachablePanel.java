package net.ncguy.ui.detachable;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

import java.util.Optional;

public class DetachablePanel implements IDetachable {

    protected IDetachableContainer parent;
    protected IPanel panel;

    public DetachablePanel(IPanel panel) {
        this.panel = panel;
    }

    public IPanel GetPanel() {
        return panel;
    }

    @Override
    public void SetParent(IDetachableContainer parent) {
        this.parent = parent;
    }

    @Override
    public Optional<IDetachableContainer> GetParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public Optional<Table> GetRootTable() {
        return Optional.ofNullable(panel.GetRootTable());
    }

    @Override
    public String GetTitle() {
        return panel.GetTitle();
    }
}
