package net.ncguy.tracker.alpha.tracking;

import javafx.beans.property.SimpleObjectProperty;
import org.opencv.core.Mat;

public class KinectTrackerCore extends TrackerCore {

    public SimpleObjectProperty<KinectFrameSource.KFrames> previewFrame;

    public KinectTrackerCore() {
        previewFrame = new SimpleObjectProperty<>(KinectFrameSource.KFrames.BGR_IMAGE);
    }

    public void Track() {
//        frameSource.Invalidate();

        if(frameSource instanceof KinectFrameSource) {
            KinectFrameSource src = (KinectFrameSource) this.frameSource;
            Mat mat = src.GetKFrame(previewFrame.get().id);
            SetPreview(mat);
        }
    }
}
