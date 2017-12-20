package net.ncguy.tracker.python;

import com.kotcrab.vis.ui.widget.Menu;
import javafx.beans.property.SimpleBooleanProperty;
import net.ncguy.api.data.TrackedBuffer;
import net.ncguy.api.loaders.LoaderHub;
import net.ncguy.api.tracker.ITracker;
import net.ncguy.tracking.display.ModularStage;
import net.ncguy.ui.detachable.DetachablePanel;

public class TrackerPython implements ITracker {

    public static String jarFileLocation;
    private DetachablePanel detachablePanel;

    private SimpleBooleanProperty shouldTrack = new SimpleBooleanProperty(false);

    public TrackerPython() { }

    @Override
    public String Name() {
        return "Python Tracker";
    }

    @Override
    public void StartupTracker() {
    }

    @Override
    public void Track(TrackedBuffer buffer) {
        try {
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ShutdownTracker() {
    }

    @Override
    public void AddToScene(ModularStage stage) {

//        debugPanel = new DebugPanel();
//        detachablePanel = new DetachablePanel(debugPanel);
//        stage.AddTab(detachablePanel, ModularStage.Sidebars.RIGHT);

        String menuTitle = "Trackers";

        Menu menu = stage.RequestMenu(menuTitle);

        stage.AddSeparatorToMenu(menuTitle, "Python");
        stage.AddItemToMenu(menuTitle, "Activate", () -> LoaderHub.trackerLoader.TrySelect(this));
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

    @Override
    public void SetJarFileLocation(String loc) {
        jarFileLocation = loc;
    }
}
