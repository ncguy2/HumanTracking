package net.ncguy.api;

import net.ncguy.tracking.display.ModularStage;

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

}
