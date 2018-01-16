package net.ncguy.skeleton;

import net.ncguy.api.IMiscModule;
import net.ncguy.skeleton.ui.MappingTreePanel;
import net.ncguy.skeleton.ui.MeshTreePanel;
import net.ncguy.skeleton.ui.SkeletonTreePanel;
import net.ncguy.tracking.display.ModularStage;
import net.ncguy.ui.detachable.IDetachable;

import java.util.ArrayList;
import java.util.List;

public class SkeletalModule implements IMiscModule {

    List<IDetachable> panels = new ArrayList<>();

    @Override
    public void AddToScene(ModularStage stage) {
        stage.AddPanel(new SkeletonTreePanel(), ModularStage.Sidebars.LEFT);
        stage.AddPanel(new MeshTreePanel(stage), ModularStage.Sidebars.LEFT);
//        AddPanel(new MappingPanel(), stage, ModularStage.Sidebars.RIGHT);
        stage.AddPanel(new MappingTreePanel(), ModularStage.Sidebars.RIGHT);
    }

    @Override
    public void RemoveFromScene(ModularStage stage) {
//        stage.RemoveTab(treePanel);
//        stage.RemoveTab(meshPanel);
//        stage.RemoveTab(mapPanel);
        panels.forEach(stage::RemoveTab);
        panels.clear();
    }

    @Override
    public void Startup() {

    }

    @Override
    public void Shutdown() {

    }
}
