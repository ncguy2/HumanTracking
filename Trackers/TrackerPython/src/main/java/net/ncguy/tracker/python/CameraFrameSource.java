package net.ncguy.tracker.python;


import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import static org.opencv.videoio.Videoio.*;

public class CameraFrameSource extends FrameSource {

    protected int camIndex;
    protected VideoCapture capture;

    protected int frameWidth;
    protected int frameHeight;
    protected int frameFormat;

    public CameraFrameSource(int camIndex) {
        this.camIndex = camIndex;
        capture = new VideoCapture(this.camIndex);

        while(!capture.isOpened()) {
            System.out.println("Waiting for camera...");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        capture.set(CV_CAP_PROP_FRAME_WIDTH, 1200);
        capture.set(CV_CAP_PROP_FRAME_HEIGHT, 900);

        frameWidth  = (int) capture.get(CV_CAP_PROP_FRAME_WIDTH);
        frameHeight = (int) capture.get(CV_CAP_PROP_FRAME_HEIGHT);
        frameFormat = (int) capture.get(CV_CAP_PROP_PREVIEW_FORMAT);

        Runtime.getRuntime().addShutdownHook(new Thread(this::Dispose));

    }

    public void Dispose() {
        if(capture != null) {
            capture.release();
            capture = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        Dispose();
        super.finalize();
    }

    @Override
    public void SetFrame() {

        if(frame != null) {
            frame.release();
            frame = null;
        }

        frame = new Mat(frameWidth, frameHeight, frameFormat);
        capture.read(frame);


//        Size size = new Size(800, 600);
//        Mat frame = Mat.zeros(size, f.type());
//        Imgproc.resize(f, frame, size);
//        this.frame = f;
//        f.release();
    }
}
