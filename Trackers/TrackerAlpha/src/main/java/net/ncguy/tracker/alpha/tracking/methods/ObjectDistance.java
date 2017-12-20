package net.ncguy.tracker.alpha.tracking.methods;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.function.Function;

public class ObjectDistance extends DetectionMethod {

    @Override
    public Mat Process(Mat input) {

        Function<Boolean, Mat> newMat = NewFrameFunc(input);

        Mat binary = newMat.apply(true);
        Mat dst = newMat.apply(false);

//        dst.convertTo(dst, CvType.CV_32F);

        Mat grey = newMat.apply(true);
        Imgproc.cvtColor(input, grey, Imgproc.COLOR_BGR2GRAY);

        Imgproc.threshold(grey, binary, 0, 255, Imgproc.THRESH_OTSU);

        Imgproc.distanceTransform(binary, dst, Imgproc.DIST_L2, 3);


        dst.convertTo(dst, CvType.CV_8UC3);

        Imgproc.equalizeHist(dst, dst);
//        Imgproc.threshold(dst, dst, , 255, Imgproc.THRESH_BINARY);

        Clear();

        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_GRAY2BGR);

        return dst;
    }

}
