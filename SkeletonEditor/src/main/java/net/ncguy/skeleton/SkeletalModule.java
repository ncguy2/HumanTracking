package net.ncguy.skeleton;

import net.ncguy.api.IMiscModule;
import net.ncguy.skeleton.ui.MainPanel;
import net.ncguy.tracking.display.ModularStage;
import net.ncguy.ui.detachable.DetachablePanel;

public class SkeletalModule implements IMiscModule {

    DetachablePanel detachablePanel;

    @Override
    public void AddToScene(ModularStage stage) {
        MainPanel panel = new MainPanel(stage);
        detachablePanel = new DetachablePanel(panel);
        stage.AddTab(detachablePanel, ModularStage.Sidebars.RIGHT);
    }

    @Override
    public void RemoveFromScene(ModularStage stage) {
        stage.RemoveTab(detachablePanel);
    }

    @Override
    public void Startup() {

    }

    @Override
    public void Shutdown() {

    }
}
