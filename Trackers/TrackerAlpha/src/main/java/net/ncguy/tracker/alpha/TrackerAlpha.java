package net.ncguy.tracker.alpha;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.kotcrab.vis.ui.widget.Menu;
import javafx.beans.property.SimpleBooleanProperty;
import net.ncguy.api.data.TrackedBuffer;
import net.ncguy.api.loaders.LoaderHub;
import net.ncguy.api.tracker.ITracker;
import net.ncguy.tracker.alpha.tracking.CameraFrameSource;
import net.ncguy.tracker.alpha.tracking.TrackerCore;
import net.ncguy.tracker.alpha.ui.DebugPanel;
import net.ncguy.tracking.display.ModularStage;
import net.ncguy.ui.detachable.DetachablePanel;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGR;

public class TrackerAlpha implements ITracker {

    public static String jarFileLocation;
    private DetachablePanel detachablePanel;
    private DebugPanel debugPanel;
    private TrackerCore tracker;

    private SimpleBooleanProperty shouldTrack = new SimpleBooleanProperty(false);

    public TrackerAlpha() { }

    @Override
    public String Name() {
        return "Alpha Tracker";
    }

    @Override
    public void StartupTracker() {
//        tracker = new TrackerCore();

        shouldTrack.set(true);

        if(tracker != null) return;

        tracker = new TrackerCore();
//        tracker.FrameSource(new DiskFrameSource(new File("C:\\Users\\Guy\\Pictures\\Captur.PNG")));
        tracker.FrameSource(new CameraFrameSource(1));
        tracker.PreviewFunc(debugPanel::SetImage);
        tracker.MatToTexFunc(this::MatToTex);
    }

    @Override
    public void Track(TrackedBuffer buffer) {
        try {
            if(tracker != null)
                tracker.Track(buffer);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public Texture MatToTex(Mat mat) {
        ByteBuffer buffer = (ByteBuffer) MatBuffer(mat);
        buffer.position(0);
        Pixmap map = new Pixmap(mat.cols(), mat.rows(), Pixmap.Format.RGB888);
        ByteBuffer pixels = map.getPixels();
        pixels.put(buffer);
        pixels.position(0);
        Texture texture = new Texture(map);
        map.dispose();
        return texture;
    }

    public GLTextureData MatData(Mat mat) {

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);

        int channels = mat.channels();
        int format = GL_RGB;
        if(channels == 1)
            format = GL_LUMINANCE;

        ByteBuffer buffer = (ByteBuffer) MatBuffer(mat);
        buffer.position(0);

        return new GLTextureData(mat.cols(), mat.rows(), 0, GL_RGB, format, GL_FLOAT, buffer);
    }

    public Buffer MatBuffer(Mat mat) {

        int channels = Math.min(mat.channels(), 3);
        int rows = mat.rows();
        int cols = mat.cols();

        int size = (rows * cols) * channels;

        byte[] arr = new byte[size];
        mat.get(0, 0, arr);

        return ByteBuffer.wrap(arr);
    }

    public long MatToTexture(Mat mat) {
        int texHandle = Gdx.gl.glGenTexture();
        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, texHandle);

        Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
        Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);

        Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_REPEAT);
        Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_REPEAT);

        int channels = mat.channels();
        int format = GL_BGR;
        if(channels == 1)
            format = GL_LUMINANCE;
        int rows = mat.rows();
        int cols = mat.cols();

        Buffer buffer = MatBuffer(mat);

        Gdx.gl.glTexImage2D(Gdx.gl.GL_TEXTURE_2D, 0, GL20.GL_RGB, cols, rows, 0, format, GL20.GL_UNSIGNED_BYTE, buffer);

        return texHandle;
    }

    public float[] ToColour(double[] pixel, Mat mat) {
        float[] output = new float[4];
        switch(mat.channels()) {
            case 1:
                output[0] = output[1] = output[2] = (float) (pixel[0] / 255.f);
                output[3] = 1.f;
                break;
            case 3:
                output[0] = (float) (pixel[0] / 255.f);
                output[1] = (float) (pixel[1] / 255.f);
                output[2] = (float) (pixel[2] / 255.f);
                output[3] = 1.f;
                break;
            case 4:
                output[0] = (float) (pixel[0] / 255.f);
                output[1] = (float) (pixel[1] / 255.f);
                output[2] = (float) (pixel[2] / 255.f);
                output[3] = (float) (pixel[3] / 255.f);
                break;
        }
        return output;
    }

    public void SetColour(float[] pixel, int row, int col, int width, byte[] buffer) {
        int idx = (row * width) + col;
        idx *= 4;

        buffer[idx + 0] = Float.valueOf(pixel[0]).byteValue();
        buffer[idx + 1] = Float.valueOf(pixel[1]).byteValue();
        buffer[idx + 2] = Float.valueOf(pixel[2]).byteValue();
        buffer[idx + 3] = Float.valueOf(pixel[3]).byteValue();

//        buffer[idx] = Color.rgba8888(pixel[0], pixel[1], pixel[2], pixel[3]);

//        map.setColor(pixel[0], pixel[1], pixel[2], pixel[3]);
//        map.drawPixel(col, row);
    }

    @Override
    public void ShutdownTracker() {
//        tracker.Shutdown();
        shouldTrack.set(true);
    }

    @Override
    public void AddToScene(ModularStage stage) {

        debugPanel = new DebugPanel();
        detachablePanel = new DetachablePanel(debugPanel);
        stage.AddTab(detachablePanel, ModularStage.Sidebars.RIGHT);

        String menuTitle = "Trackers";

        Menu menu = stage.RequestMenu(menuTitle);

        stage.AddSeparatorToMenu(menuTitle, "Alpha");
        stage.AddItemToMenu(menuTitle, "Activate", () -> LoaderHub.trackerLoader.TrySelect(this));

//        stage.AddSeparatorToMenu(menuTitle, "Kinect");
//        StageHelpers.DropdownMenuItem("Frame", tracker.previewFrame, KinectFrameSource.KFrames.values(), menu);
    }

    @Override
    public void RemoveFromScene(ModularStage stage) {
        stage.RemoveTab(detachablePanel);
    }

    @Override
    public void Startup() {
        String cvLibLocation = Core.NATIVE_LIBRARY_NAME;
        String mitieLibLocation = "javamitie";
//        new SharedLibraryLoader(TrackerAlpha.jarFileLocation).load(nativeLibraryName);
//        System.loadLibrary(nativeLibraryName);

        String formatPattern = "libs/%s/%s.dll";

        String platformDir = "x86";
        if(SharedLibraryLoader.is64Bit)
            platformDir = "x64";

        File file = new File(String.format(formatPattern, platformDir, cvLibLocation));
//        NativeUtils.LoadLibrary(file);
        System.loadLibrary(cvLibLocation);
//        file = new File(String.format(formatPattern, platformDir, mitieLibLocation));
//        NativeUtils.LoadLibrary(file);

//        tracker = new KinectTrackerCore();

    }

    @Override
    public void Shutdown() {

    }

    @Override
    public void SetJarFileLocation(String loc) {
        jarFileLocation = loc;
    }
}
