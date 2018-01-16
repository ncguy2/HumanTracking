package net.ncguy.api;

import net.ncguy.tracking.display.ModularStage;
import net.ncguy.ui.detachable.IPanel;

import java.util.Optional;

public abstract interface BaseModuleInterface {

    default String Name() {
        return getClass().getSimpleName();
    }

    default boolean IsBusy() { return false; }
    default void WhenAvailable(Runnable task) { task.run(); }

    void AddToScene(ModularStage stage);
    void RemoveFromScene(ModularStage stage);

    void Startup();
    void Shutdown();

    default void SetJarFileLocation(String loc) {}

    default Class<? extends BaseModuleInterface>[] Dependencies() {
        return new Class[0];
    }

    default public <T extends IPanel> Optional<T> GetPanel(Class<T> cls) {
        return Optional.empty();
    }

    default public Optional<IPanel> GetPanel(String cls) {
        return Optional.empty();
    }

}
