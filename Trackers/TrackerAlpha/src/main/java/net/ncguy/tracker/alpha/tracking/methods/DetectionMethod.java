package net.ncguy.tracker.alpha.tracking.methods;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class DetectionMethod {

    protected List<Mat> matsToRelease = new ArrayList<>();

    protected void Clear() {
        matsToRelease.forEach(Mat::release);
        matsToRelease.clear();
    }

    protected Function<Boolean, Mat> NewFrameFunc(Mat mat) {
        return manage -> {
            Mat m = new Mat(mat.rows(), mat.cols(), mat.type());
            if (manage)
                matsToRelease.add(m);
            return m;
        };
    }

    public abstract Mat Process(Mat input);

}
