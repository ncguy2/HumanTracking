package net.ncguy.tracker.python;

import org.opencv.core.Mat;

public abstract class FrameSource {

    Mat frame;

    public Mat GetFrame() {
        if(frame == null)
            SetFrame();
        return frame;
    }

    public void Invalidate() {
        if(frame != null) {
            frame.release();
            frame = null;
        }
    }

    public abstract void SetFrame();

}
