package net.ncguy.tracker.python;

import com.kotcrab.vis.ui.widget.Menu;
import javafx.beans.property.SimpleBooleanProperty;
import net.ncguy.api.data.TrackedBuffer;
import net.ncguy.api.loaders.LoaderHub;
import net.ncguy.api.tracker.ITracker;
import net.ncguy.tracker.python.net.NetHandler;
import net.ncguy.tracking.display.ModularStage;
import net.ncguy.ui.detachable.DetachablePanel;
import org.opencv.core.Mat;

import java.io.IOException;

public class TrackerPython implements ITracker {

    public static String jarFileLocation;
    private DetachablePanel detachablePanel;
    NetHandler handler;
    CameraFrameSource frameSource;

    private SimpleBooleanProperty shouldTrack = new SimpleBooleanProperty(false);

    public TrackerPython() { }

    @Override
    public String Name() {
        return "Python Tracker";
    }

    @Override
    public void StartupTracker() {

//        frameSource = new CameraFrameSource(1);

        try {
            handler = new NetHandler(null);
            handler.bufferSupplier = () -> new NetHandler.FrameWrapper(1, 1, new byte[]{1});
//            handler.bufferSupplier = () -> {
//                this.frameSource.Invalidate();
//                return new NetHandler.FrameWrapper(frameSource.frameWidth, frameSource.frameHeight, this.GetFrame());
//            };
//            new ProcessBuilder("python", "./Python/Host.py").start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] GetFrame() {
        if(frameSource == null)
            return null;

        Mat mat = frameSource.GetFrame();
        int channels = Math.min(mat.channels(), 3);
        int rows = mat.rows();
        int cols = mat.cols();

        int size = (rows * cols) * channels;

        byte[] arr = new byte[size];

        synchronized (mat) {
            mat.get(0, 0, arr);
        }

        return arr;
    }

    @Override
    public void Track(TrackedBuffer buffer) {

        if(handler == null) return;

        try {
            handler.buffer = buffer;
            handler.Accept();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ShutdownTracker() {
        handler = null;
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
