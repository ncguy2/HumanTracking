package net.ncguy.tracker.alpha.tracking;

import org.opencv.core.Mat;

import static org.opencv.videoio.Videoio.*;

public class KinectFrameSource extends CameraFrameSource {

    Mat kFrame;

    public KinectFrameSource(int camIndex) {
        super(camIndex);
        kFrame = new Mat(frameWidth, frameHeight, frameFormat);
    }

    public Mat GetKFrame(int id) {
        capture.retrieve(kFrame, id);
        return kFrame;
    }

    public enum KFrames {
        DEPTH_MAP(CAP_OPENNI_DEPTH_MAP),
        POINT_CLOUD_MAP(CAP_OPENNI_POINT_CLOUD_MAP),
        DISPARITY_MAP(CAP_OPENNI_DISPARITY_MAP),
        DISPARITY_MAP_32F(CAP_OPENNI_DISPARITY_MAP_32F),
        VALID_DEPTH_MASK(CAP_OPENNI_VALID_DEPTH_MASK),
        BGR_IMAGE(CAP_OPENNI_BGR_IMAGE),
        GRAY_IMAGE(CAP_OPENNI_GRAY_IMAGE),
        IR_IMAGE(CAP_OPENNI_IR_IMAGE),
        ;

        public final int id;
        KFrames(int id) {
            this.id = id;
        }
    }

}
