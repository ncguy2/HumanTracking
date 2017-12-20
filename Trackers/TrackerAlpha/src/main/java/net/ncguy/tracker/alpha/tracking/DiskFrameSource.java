package net.ncguy.tracker.alpha.tracking;

import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;


public class DiskFrameSource extends FrameSource {

    private File file;

    public DiskFrameSource(File file) {
        this.file = file;
    }

    @Override
    public void SetFrame() {
        String absolutePath = file.getAbsolutePath();
        frame = Imgcodecs.imread(absolutePath);
    }
}
