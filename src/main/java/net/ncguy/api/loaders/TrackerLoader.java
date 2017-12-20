package net.ncguy.api.loaders;

import javafx.beans.property.SimpleObjectProperty;
import net.ncguy.api.log.ILogger;
import net.ncguy.api.tracker.ITracker;

import java.util.function.Consumer;

public class TrackerLoader extends ModuleLoader<ITracker> {

    public SimpleObjectProperty<ITracker> activeTracker = new SimpleObjectProperty<>(null);

    public boolean HasActiveTracker() {
        return activeTracker.get() != null;
    }

    public TrackerLoader() {
        super(ITracker.class);

        activeTracker.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null) {
                Log(ILogger.LogLevel.INFO, "[TrackerLoader] " + oldValue.Name() + " shutting down");
                oldValue.ShutdownTracker();
            }

            if(newValue != null) {
                Log(ILogger.LogLevel.INFO, "[TrackerLoader] " + newValue.Name() + " starting up");
                newValue.StartupTracker();
            }

        });

    }

    public void TrySelect(ITracker tracker) {
        if(HasActiveTracker())
            activeTracker.get().WhenAvailable(() -> activeTracker.set(tracker));
        else activeTracker.set(tracker);
    }

    public void Iterate(Consumer<ITracker> func) {
        loaded.values().forEach(l -> l.forEach(func));
    }

}
