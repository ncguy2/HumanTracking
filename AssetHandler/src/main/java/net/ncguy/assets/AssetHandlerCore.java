package net.ncguy.assets;

import net.ncguy.api.IMiscModule;
import net.ncguy.assets.ui.AssetTable;
import net.ncguy.tracking.display.ModularStage;
import net.ncguy.ui.detachable.DetachablePanel;

public class AssetHandlerCore implements IMiscModule {

    DetachablePanel assetTable;

    @Override
    public String Name() {
        return "Asset Handler";
    }

    @Override
    public void AddToScene(ModularStage stage) {
        assetTable = stage.AddTab(new AssetTable(), ModularStage.Sidebars.LEFT);
    }

    @Override
    public void RemoveFromScene(ModularStage stage) {
        stage.RemoveTab(assetTable);
        assetTable = null;
    }

    @Override
    public void Startup() {

    }

    @Override
    public void Shutdown() {

    }
}
