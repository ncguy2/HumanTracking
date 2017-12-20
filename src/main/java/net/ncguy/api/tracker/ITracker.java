package net.ncguy.api.tracker;

import net.ncguy.api.BaseModuleInterface;
import net.ncguy.api.data.TrackedBuffer;
import net.ncguy.api.loaders.LoaderHub;

public interface ITracker extends BaseModuleInterface {

    String Name();

    default boolean IsActive() {
        return this.equals(LoaderHub.trackerLoader.activeTracker.get());
    }

    void StartupTracker();

    void Track(TrackedBuffer buffer);

    void ShutdownTracker();

}
