package net.ncguy.skeleton;

import net.ncguy.api.IMiscModule;
import net.ncguy.skeleton.ui.MappingTreePanel;
import net.ncguy.skeleton.ui.MeshTreePanel;
import net.ncguy.skeleton.ui.SkeletonTreePanel;
import net.ncguy.tracking.display.ModularStage;
import net.ncguy.ui.detachable.DetachablePanel;
import net.ncguy.ui.detachable.IPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SkeletalModule implements IMiscModule {

    List<DetachablePanel> panels = new ArrayList<>();

    @Override
    public void AddToScene(ModularStage stage) {
        stage.AddPanel(Panel(new SkeletonTreePanel()), ModularStage.Sidebars.LEFT);
        stage.AddPanel(Panel(new MeshTreePanel(stage)), ModularStage.Sidebars.LEFT);
        stage.AddPanel(Panel(new MappingTreePanel()), ModularStage.Sidebars.RIGHT);
    }

    public DetachablePanel Panel(IPanel panel) {
        DetachablePanel d = new DetachablePanel(panel);
        panels.add(d);
        return d;
    }

    @Override
    public void RemoveFromScene(ModularStage stage) {
        panels.forEach(stage::RemoveTab);
        panels.clear();
    }

    @Override
    public <T extends IPanel> Optional<T> GetPanel(Class<T> cls) {
        return Get(cls, p -> p.GetPanel().getClass().equals(cls));
    }

    @Override
    public Optional<IPanel> GetPanel(String cls) {
        return Get(IPanel.class, p -> p.GetPanel().getClass().getCanonicalName().equalsIgnoreCase(cls));
    }

    protected <T extends IPanel> Optional<T> Get(Class<T> type, Predicate<DetachablePanel> filter) {
        return panels.stream()
                .filter(filter)
                .map(DetachablePanel::GetPanel)
                .map(p -> (T) p)
                .findFirst();
    }

    @Override
    public void Startup() {

    }

    @Override
    public void Shutdown() {

    }
}
