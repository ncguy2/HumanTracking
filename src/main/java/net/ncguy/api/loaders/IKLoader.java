package net.ncguy.api.loaders;

import javafx.beans.property.SimpleObjectProperty;
import net.ncguy.api.ik.IIKSolver;
import net.ncguy.api.log.ILogger;

public class IKLoader extends ModuleLoader<IIKSolver> {

    public SimpleObjectProperty<IIKSolver> activeSolver = new SimpleObjectProperty<>(null);

    public boolean HasActiveSolver() {
        return activeSolver.get() != null;
    }

    public IKLoader() {
        super(IIKSolver.class);

        activeSolver.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null) {
                Log(ILogger.LogLevel.INFO, "[IKLoader] " + oldValue.Name() + " shutting down");
                oldValue.ShutdownIK();
            }

            if(newValue != null) {
                Log(ILogger.LogLevel.INFO, "[IKLoader] " + newValue.Name() + " starting up");
                newValue.StartupIK();
            }

        });

    }

    public void TrySelect(IIKSolver solver) {
        if(HasActiveSolver())
            activeSolver.get().WhenAvailable(() -> activeSolver.set(solver));
        else activeSolver.set(solver);
    }

}
